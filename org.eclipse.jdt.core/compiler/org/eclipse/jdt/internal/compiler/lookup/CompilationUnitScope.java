package org.eclipse.jdt.internal.compiler.lookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class CompilationUnitScope extends Scope {
	public LookupEnvironment environment;
	public CompilationUnitDeclaration referenceContext;
	public char[][] currentPackageName;
	public PackageBinding fPackage;
	public ImportBinding[] imports;
	public SourceTypeBinding[] topLevelTypes;

	private CompoundNameVector qualifiedReferences;
	private SimpleNameVector simpleNameReferences;
	private ObjectVector referencedTypes;

	private ObjectVector namespaceDependencies;
	private ObjectVector typeDependencies;

public CompilationUnitScope(CompilationUnitDeclaration unit, LookupEnvironment environment) {
	super(COMPILATION_UNIT_SCOPE, null);
	this.environment = environment;
	this.referenceContext = unit;
	unit.scope = this;
	this.currentPackageName = unit.currentPackage == null ? NoCharChar : unit.currentPackage.tokens;

	if (environment.options.produceReferenceInfo) {
		this.qualifiedReferences = new CompoundNameVector();
		this.simpleNameReferences = new SimpleNameVector();
		this.referencedTypes = new ObjectVector();

		this.namespaceDependencies = new ObjectVector();
		this.typeDependencies = new ObjectVector();
	} else {
		this.qualifiedReferences = null; // used to test if dependencies should be recorded
		this.simpleNameReferences = null;
		this.referencedTypes = null;

		this.namespaceDependencies = null; // used to test if dependencies should be recorded
		this.typeDependencies = null;
	}
}
public void addNamespaceReference(PackageBinding packageBinding) {
	if (namespaceDependencies == null) return; // we're not recording dependencies

	if (packageBinding.isValidBinding()) {
		if (!namespaceDependencies.containsIdentical(packageBinding))
			namespaceDependencies.add(packageBinding);
	} else {
		for (int i = namespaceDependencies.size; --i >= 0;) {
			PackageBinding next = (PackageBinding) namespaceDependencies.elementAt(i);
			if (!next.isValidBinding() && CharOperation.equals(packageBinding.compoundName, next.compoundName))
				return;
		}
		namespaceDependencies.add(packageBinding);
	}
}
public void addTypeReference(TypeBinding type) {
	if (typeDependencies == null) return; // we're not recording dependencies

	if (type.isArrayType())
		type = ((ArrayBinding) type).leafComponentType;
	if (!type.isBaseType()) {
		ReferenceBinding actualType = (ReferenceBinding) type;
		if (!typeDependencies.containsIdentical(actualType))
			typeDependencies.add(actualType);
	}
}
public void addTypeReferences(TypeBinding[] types) {
	if (typeDependencies == null) return; // we're not recording dependencies
	if (types == null || types == NoExceptions) return;

	for (int i = 0, max = types.length; i < max; i++) addTypeReference(types[i]);
}
void buildFieldsAndMethods() {
	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].scope.buildFieldsAndMethods();
}
void buildTypeBindings() {
	topLevelTypes = new SourceTypeBinding[0]; // want it initialized if the package cannot be resolved
	if (referenceContext.compilationResult.compilationUnit != null) {
		char[][] expectedPackageName = referenceContext.compilationResult.compilationUnit.getPackageName();
		if (expectedPackageName != null && !CharOperation.equals(currentPackageName, expectedPackageName)) {
			problemReporter().packageIsNotExpectedPackage(referenceContext);
			currentPackageName = expectedPackageName.length == 0 ? NoCharChar : expectedPackageName;
		}
	}
	if (currentPackageName == NoCharChar) {
		if ((fPackage = environment.defaultPackage) == null) {
			problemReporter().mustSpecifyPackage(referenceContext);
			return;
		}
	} else {
		if ((fPackage = environment.createPackage(currentPackageName)) == null) {
			problemReporter().packageCollidesWithType(referenceContext);
			return;
		}
	}

	// Skip typeDeclarations which know of previously reported errors
	TypeDeclaration[] types = referenceContext.types;
	int typeLength = (types == null) ? 0 : types.length;
	topLevelTypes = new SourceTypeBinding[typeLength];
	int count = 0;
	nextType: for (int i = 0; i < typeLength; i++) {
		TypeDeclaration typeDecl = types[i];
		ReferenceBinding typeBinding = fPackage.getType0(typeDecl.name);
		if (typeBinding != null && !(typeBinding instanceof UnresolvedReferenceBinding)) {
			// if a type exists, it must be a valid type - cannot be a NotFound problem type
			// unless its an unresolved type which is now being defined
			problemReporter().duplicateTypes(referenceContext, typeDecl);
			continue nextType;
		}
		if (currentPackageName != NoCharChar) {
			if ((fPackage.getPackage0(typeDecl.name)) != null || environment.isPackage(currentPackageName, typeDecl.name)) {
				// if a package exists, it must be a valid package - cannot be a NotFound problem package
				problemReporter().typeCollidesWithPackage(referenceContext, typeDecl);
				continue nextType;
			}
		}

		if ((typeDecl.modifiers & AccPublic) != 0) {
			char[] mainTypeName;
			if ((mainTypeName = referenceContext.getMainTypeName()) != null // mainTypeName == null means that implementor of ICompilationUnit decided to return null
					&& !CharOperation.equals(mainTypeName, typeDecl.name)) {
				problemReporter().publicClassMustMatchFileName(referenceContext, typeDecl);
				continue nextType;
			}
		}

		ClassScope child = new ClassScope(this, typeDecl);
		topLevelTypes[count++] = child.buildType(null, fPackage);
	}

	// shrink topLevelTypes... only happens if an error was reported
	if (count != topLevelTypes.length)
		System.arraycopy(topLevelTypes, 0, topLevelTypes = new SourceTypeBinding[count], 0, count);
}
void checkAndSetImports() {
	// initialize the default imports if necessary... share the default java.lang.* import
	if (environment.defaultImports == null) {
		Binding importBinding = environment.getTopLevelPackage(JAVA);
		if (importBinding != null)
			importBinding = ((PackageBinding) importBinding).getTypeOrPackage(JAVA_LANG[1]);

		// abort if java.lang cannot be found...
		if (importBinding == null || !importBinding.isValidBinding())
			problemReporter().isClassPathCorrect(JAVA_LANG_OBJECT, referenceCompilationUnit());

		environment.defaultImports = new ImportBinding[] {new ImportBinding(JAVA_LANG, true, importBinding)};
	}
	if (referenceContext.imports == null) {
		imports = environment.defaultImports;
		return;
	}

	// allocate the import array, add java.lang.* by default
	int numberOfStatements = referenceContext.imports.length;
	int numberOfImports = numberOfStatements + 1;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		if (importReference.onDemand && CharOperation.equals(JAVA_LANG, importReference.tokens)) {
			numberOfImports--;
			break;
		}
	}
	ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
	resolvedImports[0] = environment.defaultImports[0];
	int index = 1;

	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		char[][] compoundName = importReference.tokens;

		// skip duplicates or imports of the current package
		for (int j = 0; j < index; j++)
			if (resolvedImports[j].onDemand == importReference.onDemand)
				if (CharOperation.equals(compoundName, resolvedImports[j].compoundName))
					continue nextImport;
		if (importReference.onDemand == true)
			if (CharOperation.equals(compoundName, currentPackageName))
				continue nextImport;

		if (importReference.onDemand) {
			Binding importBinding = findOnDemandImport(compoundName);
			if (!importBinding.isValidBinding())
				continue nextImport;	// we report all problems in faultInImports()
			resolvedImports[index++] = new ImportBinding(compoundName, true, importBinding);
		} else {
			resolvedImports[index++] = new ImportBinding(compoundName, false, null);
		}
	}

	// shrink resolvedImports... only happens if an error was reported
	if (resolvedImports.length > index)
		System.arraycopy(resolvedImports, 0, resolvedImports = new ImportBinding[index], 0, index);
	imports = resolvedImports;
}
void connectTypeHierarchy() {
	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].scope.connectTypeHierarchy();
}
void faultInImports() {
	if (referenceContext.imports == null)
		return;

	// collect the top level type names if a single type import exists
	int numberOfStatements = referenceContext.imports.length;
	HashtableOfType typesBySimpleNames = null;
	for (int i = 0; i < numberOfStatements; i++) {
		if (!referenceContext.imports[i].onDemand) {
			typesBySimpleNames = new HashtableOfType(topLevelTypes.length + numberOfStatements);
			for (int j = 0, length = topLevelTypes.length; j < length; j++)
				typesBySimpleNames.put(topLevelTypes[j].sourceName, topLevelTypes[j]);
			break;
		}
	}

	// allocate the import array, add java.lang.* by default
	int numberOfImports = numberOfStatements + 1;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		if (importReference.onDemand && CharOperation.equals(JAVA_LANG, importReference.tokens)) {
			numberOfImports--;
			break;
		}
	}
	ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
	resolvedImports[0] = environment.defaultImports[0];
	int index = 1;

	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		char[][] compoundName = importReference.tokens;

		// skip duplicates or imports of the current package
		for (int j = 0; j < index; j++)
			if (resolvedImports[j].onDemand == importReference.onDemand)
				if (CharOperation.equals(compoundName, resolvedImports[j].compoundName))
					continue nextImport;
		if (importReference.onDemand == true)
			if (CharOperation.equals(compoundName, currentPackageName))
				continue nextImport;

		if (importReference.onDemand) {
			Binding importBinding = findOnDemandImport(compoundName);
			if (!importBinding.isValidBinding()) {
				problemReporter().importProblem(importReference, importBinding);
				continue nextImport;
			}
			resolvedImports[index++] = new ImportBinding(compoundName, true, importBinding);
		} else {
			Binding typeBinding = findSingleTypeImport(compoundName);
			if (!typeBinding.isValidBinding()) {
				problemReporter().importProblem(importReference, typeBinding);
				continue nextImport;
			}
			if (typeBinding instanceof PackageBinding) {
				problemReporter().cannotImportPackage(importReference);
				continue nextImport;
			}
			ReferenceBinding existingType = typesBySimpleNames.get(compoundName[compoundName.length - 1]);
			if (existingType != null) {
				// duplicate test above should have caught this case, but make sure
				if (existingType == typeBinding)
					continue nextImport;

				// either the type collides with a top level type or another imported type
				for (int j = 0, length = topLevelTypes.length; j < length; j++) {
					if (CharOperation.equals(topLevelTypes[j].sourceName, existingType.sourceName)) {
						problemReporter().conflictingImport(importReference);
						continue nextImport;
					}
				}
				problemReporter().duplicateImport(importReference);
				continue nextImport;
			}
			resolvedImports[index++] = new ImportBinding(compoundName, false, typeBinding);
			typesBySimpleNames.put(compoundName[compoundName.length - 1], (ReferenceBinding) typeBinding);
		}
	}

	// shrink resolvedImports... only happens if an error was reported
	if (resolvedImports.length > index)
		System.arraycopy(resolvedImports, 0, resolvedImports = new ImportBinding[index], 0, index);
	imports = resolvedImports;
}
public void faultInTypes() {
	faultInImports();

	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].faultInTypesForFieldsAndMethods();
}
private Binding findOnDemandImport(char[][] compoundName) {
	// replaces the 3 calls to addNamespaceReference & 2 to addTypeReference
	compilationUnitScope().recordQualifiedReference(compoundName);

	Binding binding = environment.getPackage0(compoundName[0]);
	if (binding == null) {
		if (environment.isPackage(null, compoundName[0]))
			binding = environment.getTopLevelPackage(compoundName[0]);
		else // hold onto a problem package since a real one will never be created
			addNamespaceReference(new ProblemPackageBinding(compoundName[0], NotFound));
	} else {
		if (binding == environment.theNotFoundPackage)
			binding = null; // forget the NotFound package
	}
	int i = 1;
	int length = compoundName.length;
	foundNothingOrType: if (binding != null) {
		PackageBinding packageBinding = (PackageBinding) binding;
		addNamespaceReference(packageBinding);

		while (i < length) {
			binding = packageBinding.getTypeOrPackage(compoundName[i++]);
			if (binding == null || !binding.isValidBinding()) {
				binding = null;
				break foundNothingOrType;
			}
			if (!(binding instanceof PackageBinding))
				break foundNothingOrType;

			packageBinding = (PackageBinding) binding;
			addNamespaceReference(packageBinding);
		}
		return packageBinding;
	}

	ReferenceBinding type;
	if (binding == null) {
		if (environment.defaultPackage == null
				|| environment.options.complianceLevel >= CompilerOptions.JDK1_4){
			return new ProblemReferenceBinding(compoundName, NotFound);
		}
		type = findType(compoundName[0], environment.defaultPackage, environment.defaultPackage);
		if (type == null || !type.isValidBinding())
			return new ProblemReferenceBinding(compoundName, NotFound);
		i = 1; // reset to look for member types inside the default package type
	} else {
		type = (ReferenceBinding) binding;
	}

	for (; i < length; i++) {
		addTypeReference(type);
		// does not look for inherited member types on purpose
		if ((type = type.getMemberType(compoundName[i])) == null)
			return new ProblemReferenceBinding(compoundName, NotFound);
	}
	addTypeReference(type);
	if (!type.canBeSeenBy(fPackage))
		return new ProblemReferenceBinding(compoundName, type, NotVisible);
	return type;
}
private Binding findSingleTypeImport(char[][] compoundName) {
	if (compoundName.length == 1) {
		// findType records the reference
		// the name cannot be a package
		if (environment.defaultPackage == null 
			|| environment.options.complianceLevel >= CompilerOptions.JDK1_4)
			return new ProblemReferenceBinding(compoundName, NotFound);
		ReferenceBinding typeBinding = findType(compoundName[0], environment.defaultPackage, fPackage);
		if (typeBinding == null)
			return new ProblemReferenceBinding(compoundName, NotFound);
		else
			return typeBinding;
	}
	return findOnDemandImport(compoundName);
}
/* Answer the problem reporter to use for raising new problems.
*
* Note that as a side-effect, this updates the current reference context
* (unit, type or method) in case the problem handler decides it is necessary
* to abort.
*/

public ProblemReporter problemReporter() {
	ProblemReporter problemReporter = referenceContext.problemReporter;
	problemReporter.referenceContext = referenceContext;
	return problemReporter;
}

/*
What do we hold onto:

1. when we resolve 'a.b.c', say we keep only 'a.b.c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b.c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b.c'
-> This approach fails because every type is resolved in every onDemand import to
 detect collision cases... so the references could be 10 times bigger than necessary.

2. when we resolve 'a.b.c', lets keep 'a.b' & 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b' & 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
-> This approach does not have a space problem but fails to handle collision cases.
 What happens if a type is added named 'a.b'? We would search for 'a' & 'b' but
 would not find a match.

3. when we resolve 'a.b.c', lets keep 'a', 'a.b' & 'a', 'b', 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a', 'a.b' & 'a', 'b', 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
OR 'a.b' -> 'a' & 'b'
OR 'a' -> '' & 'a'
-> As long as each single char[] is interned, we should not have a space problem
 and can handle collision cases.

4. when we resolve 'a.b.c', lets keep 'a.b' & 'a', 'b', 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b' & 'a', 'b', 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
OR 'a.b' -> 'a' & 'b' in the simple name collection
OR 'a' -> 'a' in the simple name collection
-> As long as each single char[] is interned, we should not have a space problem
 and can handle collision cases.
*/
void recordQualifiedReference(char[][] qualifiedName) {
	if (qualifiedReferences == null) return; // not recording dependencies

	int length = qualifiedName.length;
	if (length > 1) {
		while (!qualifiedReferences.contains(qualifiedName)) {
			qualifiedReferences.add(qualifiedName);
			if (length == 2) {
				recordSimpleReference(qualifiedName[0]);
				recordSimpleReference(qualifiedName[1]);
				return;
			}
			length--;
			recordSimpleReference(qualifiedName[length]);
			System.arraycopy(qualifiedName, 0, qualifiedName = new char[length][], 0, length);
		}
	} else if (length == 1) {
		recordSimpleReference(qualifiedName[0]);
	}
}
void recordReference(char[][] qualifiedEnclosingName, char[] simpleName) {
	recordQualifiedReference(qualifiedEnclosingName);
	recordSimpleReference(simpleName);
}
void recordSimpleReference(char[] simpleName) {
	if (simpleNameReferences == null) return; // not recording dependencies

	if (!simpleNameReferences.contains(simpleName))
		simpleNameReferences.add(simpleName);
}
void recordTypeReference(TypeBinding type) {
	if (referencedTypes == null) return; // not recording dependencies

	if (type.isArrayType())
		type = ((ArrayBinding) type).leafComponentType;
	if (!type.isBaseType() && !referencedTypes.containsIdentical(type))
		referencedTypes.add(type);
}
void recordTypeReferences(TypeBinding[] types) {
	if (qualifiedReferences == null) return; // not recording dependencies
	if (types == null || types.length == 0) return;

	for (int i = 0, max = types.length; i < max; i++) {
		// No need to record supertypes of method arguments & thrown exceptions, just the compoundName
		// If a field/method is retrieved from such a type then a separate call does the job
		TypeBinding type = types[i];
		if (type.isArrayType())
			type = ((ArrayBinding) type).leafComponentType;
		if (!type.isBaseType()) {
			ReferenceBinding actualType = (ReferenceBinding) type;
			if (!actualType.isLocalType())
				recordQualifiedReference(actualType.isMemberType()
					? CharOperation.splitOn('.', actualType.readableName())
					: actualType.compoundName);
		}
	}
}
Binding resolveSingleTypeImport(ImportBinding importBinding) {
	if (importBinding.resolvedImport == null) {
		importBinding.resolvedImport = findSingleTypeImport(importBinding.compoundName);
		if (!importBinding.resolvedImport.isValidBinding() || importBinding.resolvedImport instanceof PackageBinding) {
			ImportBinding[] newImports = new ImportBinding[imports.length - 1];
			for (int i = 0, n = 0, max = imports.length; i < max; i++)
				if (imports[i] != importBinding)
					newImports[n++] = imports[i];
			imports = newImports;
			return null;
		}
	}
	return importBinding.resolvedImport;
}
public void storeDependencyInfo() {
	// add the type hierarchy of each referenced type
	// cannot do early since the hierarchy may not be fully resolved
	for (int i = 0; i < referencedTypes.size; i++) { // grows as more types are added
		ReferenceBinding type = (ReferenceBinding) referencedTypes.elementAt(i);
		if (!type.isLocalType()) {
			recordQualifiedReference(type.isMemberType()
				? CharOperation.splitOn('.', type.readableName())
				: type.compoundName);
			ReferenceBinding enclosing = type.enclosingType();
			if (enclosing != null && !referencedTypes.containsIdentical(enclosing))
				referencedTypes.add(enclosing); // to record its supertypes
		}
		ReferenceBinding superclass = type.superclass();
		if (superclass != null && !referencedTypes.containsIdentical(superclass))
				referencedTypes.add(superclass); // to record its supertypes
		ReferenceBinding[] interfaces = type.superInterfaces();
		if (interfaces != null && interfaces.length > 0)
			for (int j = 0, length = interfaces.length; j < length; j++)
				if (!referencedTypes.containsIdentical(interfaces[j]))
					referencedTypes.add(interfaces[j]); // to record its supertypes
	}

	int size = qualifiedReferences.size;
	char[][][] qualifiedRefs = new char[size][][];
	for (int i = 0; i < size; i++)
		qualifiedRefs[i] = qualifiedReferences.elementAt(i);
	referenceContext.compilationResult.qualifiedReferences = qualifiedRefs;

	size = simpleNameReferences.size;
	char[][] simpleRefs = new char[size][];
	for (int i = 0; i < size; i++)
		simpleRefs[i] = simpleNameReferences.elementAt(i);
	referenceContext.compilationResult.simpleNameReferences = simpleRefs;

// Old code to be removed
	for (int i = 0; i < typeDependencies.size; i++) { // grows as more types are added
		// add all the supertypes & associated packages
		ReferenceBinding type = (ReferenceBinding) typeDependencies.elementAt(i);

		addNamespaceReference(type.fPackage); // is this necessary? If so what about a & a.b from a.b.c?
		if (type.enclosingType() != null)
			addTypeReference(type.enclosingType());
		if (type.superclass() != null)
			addTypeReference(type.superclass());
		ReferenceBinding[] interfaces = type.superInterfaces();
		for (int j = 0, length = interfaces.length; j < length; j++)
			addTypeReference(interfaces[j]);
	}

	int length = namespaceDependencies.size;
	char[][] namespaceNames = new char[length][];
	for (int i = 0; i < length; i++)
		namespaceNames[i] = ((PackageBinding) namespaceDependencies.elementAt(i)).readableName();
	referenceContext.compilationResult.namespaceDependencies = namespaceNames;

	length = typeDependencies.size;
	int toplevelTypeCount = 0;
	for (int i = 0; i < length; i++)
		if (!((ReferenceBinding) typeDependencies.elementAt(i)).isNestedType())
			toplevelTypeCount++;
	char[][] fileNames = new char[toplevelTypeCount][];
	for (int i = 0; i < length; i++)
		if (!((ReferenceBinding) typeDependencies.elementAt(i)).isNestedType())
			fileNames[--toplevelTypeCount] = ((ReferenceBinding) typeDependencies.elementAt(i)).getFileName();

	// eliminate duplicates
	int unique = 0;
	char[] ownFileName = referenceContext.getFileName();
	next : for (int i = 0, l = fileNames.length; i < l; i++) {
		char[] fileName = fileNames[i];
		if (CharOperation.equals(fileName, ownFileName)) {
			fileNames[i] = null;
			continue next;
		}
		for (int j = i + 1; j < l; j++) {
			if (CharOperation.equals(fileName, fileNames[j])) {
				fileNames[i] = null;
				continue next;
			}
		}
		unique++;
	}
	if (unique < fileNames.length) {
		char[][] uniqueFileNames = new char[unique][];
		for (int i = fileNames.length; --i >= 0;)
			if (fileNames[i] != null)
				uniqueFileNames[--unique] = fileNames[i];
		fileNames = uniqueFileNames;
	}
	referenceContext.compilationResult.fileDependencies = fileNames;
}
public String toString() {
	return "--- CompilationUnit Scope : " + new String(referenceContext.getFileName()); //$NON-NLS-1$
}
public void verifyMethods(MethodVerifier verifier) {
	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].verifyMethods(verifier);
}
}