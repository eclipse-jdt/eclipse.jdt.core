/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfPackage;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public class LookupEnvironment implements BaseTypes, ProblemReasons, TypeConstants {
	public CompilerOptions options;
	public ProblemReporter problemReporter;
	public ITypeRequestor typeRequestor;

	PackageBinding defaultPackage;
	ImportBinding[] defaultImports;
	HashtableOfPackage knownPackages;
	static final ProblemPackageBinding TheNotFoundPackage = new ProblemPackageBinding(CharOperation.NO_CHAR, NotFound);
	static final ProblemReferenceBinding TheNotFoundType = new ProblemReferenceBinding(CharOperation.NO_CHAR, NotFound);

	public INameEnvironment nameEnvironment;
	private MethodVerifier verifier;
	private ArrayBinding[][] uniqueArrayBindings;
	private SimpleLookupTable uniqueParameterizedTypeBindings;
	private SimpleLookupTable uniqueRawTypeBindings;
	private SimpleLookupTable uniqueWildcardBindings;

	private CompilationUnitDeclaration[] units = new CompilationUnitDeclaration[4];
	private int lastUnitIndex = -1;
	private int lastCompletedUnitIndex = -1;
	public CompilationUnitDeclaration unitBeingCompleted = null; // only set while completing units

	// indicate in which step on the compilation we are.
	// step 1 : build the reference binding
	// step 2 : conect the hierarchy (connect bindings)
	// step 3 : build fields and method bindings.
	private int stepCompleted;
	final static int BUILD_TYPE_HIERARCHY = 1;
	final static int CHECK_AND_SET_IMPORTS = 2;
	final static int CONNECT_TYPE_HIERARCHY = 3;
	final static int BUILD_FIELDS_AND_METHODS = 4;

	// shared byte[]'s used by ClassFile to avoid allocating MBs during a build
	public boolean sharedArraysUsed = true; // set to false once actual arrays are allocated
	public byte[] sharedClassFileHeader = null;
	public byte[] sharedClassFileContents = null;

public LookupEnvironment(ITypeRequestor typeRequestor, CompilerOptions options, ProblemReporter problemReporter, INameEnvironment nameEnvironment) {
	this.typeRequestor = typeRequestor;
	this.options = options;
	this.problemReporter = problemReporter;
	this.defaultPackage = new PackageBinding(this); // assume the default package always exists
	this.defaultImports = null;
	this.nameEnvironment = nameEnvironment;
	this.knownPackages = new HashtableOfPackage();
	this.uniqueArrayBindings = new ArrayBinding[5][];
	this.uniqueArrayBindings[0] = new ArrayBinding[50]; // start off the most common 1 dimension array @ 50
	this.uniqueParameterizedTypeBindings = new SimpleLookupTable(3);
	this.uniqueRawTypeBindings = new SimpleLookupTable(3);
	this.uniqueWildcardBindings = new SimpleLookupTable(3);
}
/* Ask the oracle for a type which corresponds to the compoundName.
* Answer null if the name cannot be found.
*/

public ReferenceBinding askForType(char[][] compoundName) {
	NameEnvironmentAnswer answer = nameEnvironment.findType(compoundName);
	if (answer == null)
		return null;

	if (answer.isBinaryType())
		// the type was found as a .class file
		typeRequestor.accept(answer.getBinaryType(), computePackageFrom(compoundName));
	else if (answer.isCompilationUnit())
		// the type was found as a .java file, try to build it then search the cache
		typeRequestor.accept(answer.getCompilationUnit());
	else if (answer.isSourceType())
		// the type was found as a source model
		typeRequestor.accept(answer.getSourceTypes(), computePackageFrom(compoundName));

	return getCachedType(compoundName);
}
/* Ask the oracle for a type named name in the packageBinding.
* Answer null if the name cannot be found.
*/

ReferenceBinding askForType(PackageBinding packageBinding, char[] name) {
	if (packageBinding == null) {
		if (defaultPackage == null)
			return null;
		packageBinding = defaultPackage;
	}
	NameEnvironmentAnswer answer = nameEnvironment.findType(name, packageBinding.compoundName);
	if (answer == null)
		return null;

	if (answer.isBinaryType())
		// the type was found as a .class file
		typeRequestor.accept(answer.getBinaryType(), packageBinding);
	else if (answer.isCompilationUnit())
		// the type was found as a .java file, try to build it then search the cache
		typeRequestor.accept(answer.getCompilationUnit());
	else if (answer.isSourceType())
		// the type was found as a source model
		typeRequestor.accept(answer.getSourceTypes(), packageBinding);

	return packageBinding.getType0(name);
}
/* Create the initial type bindings for the compilation unit.
*
* See completeTypeBindings() for a description of the remaining steps
*
* NOTE: This method can be called multiple times as additional source files are needed
*/

public void buildTypeBindings(CompilationUnitDeclaration unit) {
	CompilationUnitScope scope = new CompilationUnitScope(unit, this);
	scope.buildTypeBindings();

	int unitsLength = units.length;
	if (++lastUnitIndex >= unitsLength)
		System.arraycopy(units, 0, units = new CompilationUnitDeclaration[2 * unitsLength], 0, unitsLength);
	units[lastUnitIndex] = unit;
}
/* Cache the binary type since we know it is needed during this compile.
*
* Answer the created BinaryTypeBinding or null if the type is already in the cache.
*/

public BinaryTypeBinding cacheBinaryType(IBinaryType binaryType) {
	return cacheBinaryType(binaryType, true);
}
/* Cache the binary type since we know it is needed during this compile.
*
* Answer the created BinaryTypeBinding or null if the type is already in the cache.
*/

public BinaryTypeBinding cacheBinaryType(IBinaryType binaryType, boolean needFieldsAndMethods) {
	char[][] compoundName = CharOperation.splitOn('/', binaryType.getName());
	ReferenceBinding existingType = getCachedType(compoundName);

	if (existingType == null || existingType instanceof UnresolvedReferenceBinding)
		// only add the binary type if its not already in the cache
		return createBinaryTypeFrom(binaryType, computePackageFrom(compoundName), needFieldsAndMethods);
	return null; // the type already exists & can be retrieved from the cache
}
/*
* 1. Connect the type hierarchy for the type bindings created for parsedUnits.
* 2. Create the field bindings
* 3. Create the method bindings
*/

/* We know each known compilationUnit is free of errors at this point...
*
* Each step will create additional bindings unless a problem is detected, in which
* case either the faulty import/superinterface/field/method will be skipped or a
* suitable replacement will be substituted (such as Object for a missing superclass)
*/

public void completeTypeBindings() {
	stepCompleted = BUILD_TYPE_HIERARCHY;
	
	for (int i = this.lastCompletedUnitIndex + 1; i <= this.lastUnitIndex; i++) {
	    (this.unitBeingCompleted = this.units[i]).scope.checkAndSetImports();
	}
	stepCompleted = CHECK_AND_SET_IMPORTS;

	for (int i = this.lastCompletedUnitIndex + 1; i <= this.lastUnitIndex; i++) {
	    (this.unitBeingCompleted = this.units[i]).scope.connectTypeHierarchy();
	}
	stepCompleted = CONNECT_TYPE_HIERARCHY;

	for (int i = this.lastCompletedUnitIndex + 1; i <= this.lastUnitIndex; i++) {
		(this.unitBeingCompleted = this.units[i]).scope.buildFieldsAndMethods();
		this.units[i] = null; // release unnecessary reference to the parsed unit
	}
	stepCompleted = BUILD_FIELDS_AND_METHODS;
	this.lastCompletedUnitIndex = this.lastUnitIndex;
	this.unitBeingCompleted = null;
}
/*
* 1. Connect the type hierarchy for the type bindings created for parsedUnits.
* 2. Create the field bindings
* 3. Create the method bindings
*/

/*
* Each step will create additional bindings unless a problem is detected, in which
* case either the faulty import/superinterface/field/method will be skipped or a
* suitable replacement will be substituted (such as Object for a missing superclass)
*/

public void completeTypeBindings(CompilationUnitDeclaration parsedUnit) {
	if (stepCompleted == BUILD_FIELDS_AND_METHODS) {
		// This can only happen because the original set of units are completely built and
		// are now being processed, so we want to treat all the additional units as a group
		// until they too are completely processed.
		completeTypeBindings();
	} else {
		if (parsedUnit.scope == null) return; // parsing errors were too severe
		
		if (stepCompleted >= CHECK_AND_SET_IMPORTS)
			(this.unitBeingCompleted = parsedUnit).scope.checkAndSetImports();

		if (stepCompleted >= CONNECT_TYPE_HIERARCHY)
			(this.unitBeingCompleted = parsedUnit).scope.connectTypeHierarchy();
		
		this.unitBeingCompleted = null;
	}
}
/*
* Used by other compiler tools which do not start by calling completeTypeBindings().
*
* 1. Connect the type hierarchy for the type bindings created for parsedUnits.
* 2. Create the field bindings
* 3. Create the method bindings
*/

public void completeTypeBindings(CompilationUnitDeclaration parsedUnit, boolean buildFieldsAndMethods) {
	if (parsedUnit.scope == null) return; // parsing errors were too severe

	(this.unitBeingCompleted = parsedUnit).scope.checkAndSetImports();
	parsedUnit.scope.connectTypeHierarchy();
	if (buildFieldsAndMethods)
		parsedUnit.scope.buildFieldsAndMethods();
	this.unitBeingCompleted = null;
}
private PackageBinding computePackageFrom(char[][] constantPoolName) {
	if (constantPoolName.length == 1)
		return defaultPackage;

	PackageBinding packageBinding = getPackage0(constantPoolName[0]);
	if (packageBinding == null || packageBinding == TheNotFoundPackage) {
		packageBinding = new PackageBinding(constantPoolName[0], this);
		knownPackages.put(constantPoolName[0], packageBinding);
	}

	for (int i = 1, length = constantPoolName.length - 1; i < length; i++) {
		PackageBinding parent = packageBinding;
		if ((packageBinding = parent.getPackage0(constantPoolName[i])) == null || packageBinding == TheNotFoundPackage) {
			packageBinding = new PackageBinding(CharOperation.subarray(constantPoolName, 0, i + 1), parent, this);
			parent.addPackage(packageBinding);
		}
	}
	return packageBinding;
}
/* Used to guarantee array type identity.
*/

ArrayBinding createArrayType(TypeBinding type, int dimensionCount) {
	if (type instanceof LocalTypeBinding) // cache local type arrays with the local type itself
		return ((LocalTypeBinding) type).createArrayType(dimensionCount);

	// find the array binding cache for this dimension
	int dimIndex = dimensionCount - 1;
	int length = uniqueArrayBindings.length;
	ArrayBinding[] arrayBindings;
	if (dimIndex < length) {
		if ((arrayBindings = uniqueArrayBindings[dimIndex]) == null)
			uniqueArrayBindings[dimIndex] = arrayBindings = new ArrayBinding[10];
	} else {
		System.arraycopy(
			uniqueArrayBindings, 0, 
			uniqueArrayBindings = new ArrayBinding[dimensionCount][], 0, 
			length); 
		uniqueArrayBindings[dimIndex] = arrayBindings = new ArrayBinding[10];
	}

	// find the cached array binding for this leaf component type (if any)
	int index = -1;
	length = arrayBindings.length;
	while (++index < length) {
		ArrayBinding currentBinding = arrayBindings[index];
		if (currentBinding == null) // no matching array, but space left
			return arrayBindings[index] = new ArrayBinding(type, dimensionCount, this);
		if (currentBinding.leafComponentType == type)
			return currentBinding;
	}

	// no matching array, no space left
	System.arraycopy(
		arrayBindings, 0,
		(arrayBindings = new ArrayBinding[length * 2]), 0,
		length); 
	uniqueArrayBindings[dimIndex] = arrayBindings;
	return arrayBindings[length] = new ArrayBinding(type, dimensionCount, this);
}
public BinaryTypeBinding createBinaryTypeFrom(IBinaryType binaryType, PackageBinding packageBinding) {
	return createBinaryTypeFrom(binaryType, packageBinding, true);
}
public BinaryTypeBinding createBinaryTypeFrom(IBinaryType binaryType, PackageBinding packageBinding, boolean needFieldsAndMethods) {
	BinaryTypeBinding binaryBinding = new BinaryTypeBinding(packageBinding, binaryType, this);

	// resolve any array bindings which reference the unresolvedType
	ReferenceBinding cachedType = packageBinding.getType0(binaryBinding.compoundName[binaryBinding.compoundName.length - 1]);
	if (cachedType != null) {
		if (cachedType.isBinaryBinding()) // sanity check before the cast... at this point the cache should ONLY contain unresolved types
			return (BinaryTypeBinding) cachedType;

		((UnresolvedReferenceBinding) cachedType).setResolvedType(binaryBinding, this);
	}

	packageBinding.addType(binaryBinding);
	binaryBinding.cachePartsFrom(binaryType, needFieldsAndMethods);
	return binaryBinding;
}
/* Used to create packages from the package statement.
*/

PackageBinding createPackage(char[][] compoundName) {
	PackageBinding packageBinding = getPackage0(compoundName[0]);
	if (packageBinding == null || packageBinding == TheNotFoundPackage) {
		packageBinding = new PackageBinding(compoundName[0], this);
		knownPackages.put(compoundName[0], packageBinding);
	}

	for (int i = 1, length = compoundName.length; i < length; i++) {
		// check to see if it collides with a known type...
		// this case can only happen if the package does not exist as a directory in the file system
		// otherwise when the source type was defined, the correct error would have been reported
		// unless its an unresolved type which is referenced from an inconsistent class file
		ReferenceBinding type = packageBinding.getType0(compoundName[i]);
		if (type != null && type != TheNotFoundType && !(type instanceof UnresolvedReferenceBinding))
			return null;

		PackageBinding parent = packageBinding;
		if ((packageBinding = parent.getPackage0(compoundName[i])) == null || packageBinding == TheNotFoundPackage) {
			// if the package is unknown, check to see if a type exists which would collide with the new package
			// catches the case of a package statement of: package java.lang.Object;
			// since the package can be added after a set of source files have already been compiled, we need
			// whenever a package statement is encountered
			if (nameEnvironment.findType(compoundName[i], parent.compoundName) != null)
				return null;

			packageBinding = new PackageBinding(CharOperation.subarray(compoundName, 0, i + 1), parent, this);
			parent.addPackage(packageBinding);
		}
	}
	return packageBinding;
}

public ParameterizedTypeBinding createParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType) {

	// cached info is array of already created parameterized types for this type
	ParameterizedTypeBinding[] cachedInfo = (ParameterizedTypeBinding[])this.uniqueParameterizedTypeBindings.get(genericType);
	int argLength = typeArguments == null ? 0: typeArguments.length;
	boolean needToGrow = false;
	if (cachedInfo != null){
		nextCachedType : 
			// iterate existing parameterized for reusing one with same type arguments if any
			for (int i = 0, max = cachedInfo.length; i < max; i++){
			    ParameterizedTypeBinding cachedType = cachedInfo[i];
			    if (cachedType.type != genericType) continue nextCachedType; // remain of unresolved type
			    if (cachedType.enclosingType() != enclosingType) continue nextCachedType;
				TypeBinding[] cachedArguments = cachedType.arguments;
				int cachedArgLength = cachedArguments == null ? 0 : cachedArguments.length;
				if (argLength != cachedArgLength) continue nextCachedType; // would be an error situation (from unresolved binaries)
				for (int j = 0; j < cachedArgLength; j++){
					if (typeArguments[j] != cachedArguments[j]) continue nextCachedType;
				}
				// all arguments match, reuse current
				return cachedType;
		}
		needToGrow = true;
	} else {
		cachedInfo = new ParameterizedTypeBinding[1];
		this.uniqueParameterizedTypeBindings.put(genericType, cachedInfo);
	}
	// grow cache ?
	if (needToGrow){
		int length = cachedInfo.length;
		System.arraycopy(cachedInfo, 0, cachedInfo = new ParameterizedTypeBinding[length+1], 0, length);
		this.uniqueParameterizedTypeBindings.put(genericType, cachedInfo);
	}
	// add new binding
	ParameterizedTypeBinding parameterizedType = new ParameterizedTypeBinding(genericType,typeArguments, enclosingType, this);
	cachedInfo[cachedInfo.length-1] = parameterizedType;
	return parameterizedType;
}

public RawTypeBinding createRawType(ReferenceBinding genericType, ReferenceBinding enclosingType) {

	// cached info is array of already created raw types for this type
	RawTypeBinding[] cachedInfo = (RawTypeBinding[])this.uniqueRawTypeBindings.get(genericType);
	boolean needToGrow = false;
	if (cachedInfo != null){
		nextCachedType : 
			// iterate existing parameterized for reusing one with same type arguments if any
			for (int i = 0, max = cachedInfo.length; i < max; i++){
			    RawTypeBinding cachedType = cachedInfo[i];
			    if (cachedType.type != genericType) continue nextCachedType; // remain of unresolved type
			    if (cachedType.enclosingType() != enclosingType) continue nextCachedType;
				// all enclosing type match, reuse current
				return cachedType;
		}
		needToGrow = true;
	} else {
		cachedInfo = new RawTypeBinding[1];
		this.uniqueRawTypeBindings.put(genericType, cachedInfo);
	}
	// grow cache ?
	if (needToGrow){
		int length = cachedInfo.length;
		System.arraycopy(cachedInfo, 0, cachedInfo = new RawTypeBinding[length+1], 0, length);
		this.uniqueRawTypeBindings.put(genericType, cachedInfo);
	}
	// add new binding
	RawTypeBinding rawType = new RawTypeBinding(genericType, enclosingType, this);
	cachedInfo[cachedInfo.length-1] = rawType;
	return rawType;
	
}

public WildcardBinding createWildcard(ReferenceBinding genericType, int rank, TypeBinding bound, int kind) {
	
	// cached info is array of already created wildcard  types for this type
	WildcardBinding[] cachedInfo = (WildcardBinding[])this.uniqueWildcardBindings.get(genericType);
	boolean needToGrow = false;
	if (cachedInfo != null){
		nextCachedType : 
			// iterate existing wildcards for reusing one with same information if any
			for (int i = 0, max = cachedInfo.length; i < max; i++){
			    WildcardBinding cachedType = cachedInfo[i];
			    if (cachedType.genericType != genericType) continue nextCachedType; // remain of unresolved type
			    if (cachedType.rank != rank) continue nextCachedType;
			    if (cachedType.kind != kind) continue nextCachedType;
			    if (cachedType.bound != bound) continue nextCachedType;
				// all match, reuse current
				return cachedType;
		}
		needToGrow = true;
	} else {
		cachedInfo = new WildcardBinding[1];
		this.uniqueWildcardBindings.put(genericType, cachedInfo);
	}
	// grow cache ?
	if (needToGrow){
		int length = cachedInfo.length;
		System.arraycopy(cachedInfo, 0, cachedInfo = new WildcardBinding[length+1], 0, length);
		this.uniqueWildcardBindings.put(genericType, cachedInfo);
	}
	// add new binding
	WildcardBinding wildcard = new WildcardBinding(genericType, rank, bound, kind, this);
	cachedInfo[cachedInfo.length-1] = wildcard;
	return wildcard;
}

/* Answer the type for the compoundName if it exists in the cache.
* Answer theNotFoundType if it could not be resolved the first time
* it was looked up, otherwise answer null.
*
* NOTE: Do not use for nested types... the answer is NOT the same for a.b.C or a.b.C.D.E
* assuming C is a type in both cases. In the a.b.C.D.E case, null is the answer.
*/

public ReferenceBinding getCachedType(char[][] compoundName) {
	if (compoundName.length == 1) {
		if (defaultPackage == null)
			return null;
		return defaultPackage.getType0(compoundName[0]);
	}

	PackageBinding packageBinding = getPackage0(compoundName[0]);
	if (packageBinding == null || packageBinding == TheNotFoundPackage)
		return null;

	for (int i = 1, packageLength = compoundName.length - 1; i < packageLength; i++)
		if ((packageBinding = packageBinding.getPackage0(compoundName[i])) == null || packageBinding == TheNotFoundPackage)
			return null;
	return packageBinding.getType0(compoundName[compoundName.length - 1]);
}
/* Answer the top level package named name if it exists in the cache.
* Answer theNotFoundPackage if it could not be resolved the first time
* it was looked up, otherwise answer null.
*
* NOTE: Senders must convert theNotFoundPackage into a real problem
* package if its to returned.
*/

PackageBinding getPackage0(char[] name) {
	return knownPackages.get(name);
}
/* Answer the top level package named name.
* Ask the oracle for the package if its not in the cache.
* Answer null if the package cannot be found.
*/

PackageBinding getTopLevelPackage(char[] name) {
	PackageBinding packageBinding = getPackage0(name);
	if (packageBinding != null) {
		if (packageBinding == TheNotFoundPackage)
			return null;
		return packageBinding;
	}

	if (nameEnvironment.isPackage(null, name)) {
		knownPackages.put(name, packageBinding = new PackageBinding(name, this));
		return packageBinding;
	}

	knownPackages.put(name, TheNotFoundPackage); // saves asking the oracle next time
	return null;
}
/* Answer the type corresponding to the compoundName.
* Ask the oracle for the type if its not in the cache.
* Answer null if the type cannot be found... likely a fatal error.
*/

public ReferenceBinding getType(char[][] compoundName) {
	ReferenceBinding referenceBinding;

	if (compoundName.length == 1) {
		if (defaultPackage == null)
			return null;

		if ((referenceBinding = defaultPackage.getType0(compoundName[0])) == null) {
			PackageBinding packageBinding = getPackage0(compoundName[0]);
			if (packageBinding != null && packageBinding != TheNotFoundPackage)
				return null; // collides with a known package... should not call this method in such a case
			referenceBinding = askForType(defaultPackage, compoundName[0]);
		}
	} else {
		PackageBinding packageBinding = getPackage0(compoundName[0]);
		if (packageBinding == TheNotFoundPackage)
			return null;

		if (packageBinding != null) {
			for (int i = 1, packageLength = compoundName.length - 1; i < packageLength; i++) {
				if ((packageBinding = packageBinding.getPackage0(compoundName[i])) == null)
					break;
				if (packageBinding == TheNotFoundPackage)
					return null;
			}
		}

		if (packageBinding == null)
			referenceBinding = askForType(compoundName);
		else if ((referenceBinding = packageBinding.getType0(compoundName[compoundName.length - 1])) == null)
			referenceBinding = askForType(packageBinding, compoundName[compoundName.length - 1]);
	}

	if (referenceBinding == null || referenceBinding == TheNotFoundType)
		return null;
	referenceBinding = BinaryTypeBinding.resolveType(referenceBinding, this, false); // no raw conversion for now

	// compoundName refers to a nested type incorrectly (for example, package1.A$B)
	if (referenceBinding.isNestedType())
		return new ProblemReferenceBinding(compoundName, InternalNameProvided);
	return referenceBinding;
}
private TypeBinding[] getTypeArgumentsFromSignature(SignatureWrapper wrapper, TypeVariableBinding[] staticVariables, ReferenceBinding enclosingType, ReferenceBinding genericType) {
	java.util.ArrayList args = new java.util.ArrayList(2);
	int rank = 0;
	do {
		args.add(getTypeFromVariantTypeSignature(wrapper, staticVariables, enclosingType, genericType, rank++));
	} while (wrapper.signature[wrapper.start] != '>');
	wrapper.start++; // skip '>'
	TypeBinding[] typeArguments = new TypeBinding[args.size()];
	args.toArray(typeArguments);
	return typeArguments;
}
/* Answer the type corresponding to the name from the binary file.
* Does not ask the oracle for the type if its not found in the cache... instead an
* unresolved type is returned which must be resolved before used.
*
* NOTE: Does NOT answer base types nor array types!
*
* NOTE: Aborts compilation if the class file cannot be found.
*/

ReferenceBinding getTypeFromConstantPoolName(char[] signature, int start, int end, boolean isParameterized) {
	if (end == -1)
		end = signature.length;

	char[][] compoundName = CharOperation.splitOn('/', signature, start, end);
	ReferenceBinding binding = getCachedType(compoundName);
	if (binding == null) {
		PackageBinding packageBinding = computePackageFrom(compoundName);
		binding = new UnresolvedReferenceBinding(compoundName, packageBinding);
		packageBinding.addType(binding);
	} else if (binding == TheNotFoundType) {
		problemReporter.isClassPathCorrect(compoundName, null);
		return null; // will not get here since the above error aborts the compilation
	} else if (!isParameterized && binding.isGenericType()) {
	    // check raw type, only for resolved types
        binding = createRawType(binding, null);
	}
	return binding;
}
/* Answer the type corresponding to the signature from the binary file.
* Does not ask the oracle for the type if its not found in the cache... instead an
* unresolved type is returned which must be resolved before used.
*
* NOTE: Does answer base types & array types.
*
* NOTE: Aborts compilation if the class file cannot be found.
*/

TypeBinding getTypeFromSignature(char[] signature, int start, int end, boolean isParameterized, TypeBinding enclosingType) {
	int dimension = 0;
	while (signature[start] == '[') {
		start++;
		dimension++;
	}
	if (end == -1)
		end = signature.length - 1;

	// Just switch on signature[start] - the L case is the else
	TypeBinding binding = null;
	if (start == end) {
		switch (signature[start]) {
			case 'I' :
				binding = IntBinding;
				break;
			case 'Z' :
				binding = BooleanBinding;
				break;
			case 'V' :
				binding = VoidBinding;
				break;
			case 'C' :
				binding = CharBinding;
				break;
			case 'D' :
				binding = DoubleBinding;
				break;
			case 'B' :
				binding = ByteBinding;
				break;
			case 'F' :
				binding = FloatBinding;
				break;
			case 'J' :
				binding = LongBinding;
				break;
			case 'S' :
				binding = ShortBinding;
				break;
			default :
				problemReporter.corruptedSignature(enclosingType, signature, start);
				// will never reach here, since error will cause abort
		}
	} else {
		binding = getTypeFromConstantPoolName(signature, start + 1, end, isParameterized);
	}

	if (dimension == 0)
		return binding;
	return createArrayType(binding, dimension);
}
TypeBinding getTypeFromTypeSignature(SignatureWrapper wrapper, TypeVariableBinding[] staticVariables, ReferenceBinding enclosingType) {
	// TypeVariableSignature = 'T' Identifier ';'
	// ArrayTypeSignature = '[' TypeSignature
	// ClassTypeSignature = 'L' Identifier TypeArgs(optional) ';'
	//   or ClassTypeSignature '.' 'L' Identifier TypeArgs(optional) ';'
	// TypeArgs = '<' VariantTypeSignature VariantTypeSignatures '>'
	int dimension = 0;
	while (wrapper.signature[wrapper.start] == '[') {
		wrapper.start++;
		dimension++;
	}

	if (wrapper.signature[wrapper.start] == 'T') {
	    int varStart = wrapper.start + 1;
	    int varEnd = wrapper.computeEnd();
		for (int i = staticVariables.length; --i >= 0;)
			if (CharOperation.equals(staticVariables[i].sourceName, wrapper.signature, varStart, varEnd))
				return dimension == 0 ? (TypeBinding) staticVariables[i] : createArrayType(staticVariables[i], dimension);
	    ReferenceBinding initialType = enclosingType;
		do {
		    if (enclosingType instanceof BinaryTypeBinding) { // per construction can only be binary type binding
				TypeVariableBinding[] enclosingVariables = ((BinaryTypeBinding)enclosingType).typeVariables; // do not trigger resolution of variables
				for (int i = enclosingVariables.length; --i >= 0;)
					if (CharOperation.equals(enclosingVariables[i].sourceName, wrapper.signature, varStart, varEnd))
						return dimension == 0 ? (TypeBinding) enclosingVariables[i] : createArrayType(enclosingVariables[i], dimension);
		    }
		} while ((enclosingType = enclosingType.enclosingType()) != null);
		problemReporter.undefinedTypeVariableSignature(CharOperation.subarray(wrapper.signature, varStart, varEnd), initialType);
		return null; // cannot reach this, since previous problem will abort compilation
	}

	TypeBinding type = getTypeFromSignature(wrapper.signature, wrapper.start, wrapper.computeEnd(), true, enclosingType);
	if (wrapper.end != wrapper.bracket)
		return dimension == 0 ? type : createArrayType(type, dimension);

	// type must be a ReferenceBinding at this point, cannot be a BaseTypeBinding or ArrayTypeBinding
	ReferenceBinding actualType = (ReferenceBinding) type;
	TypeBinding[] typeArguments = getTypeArgumentsFromSignature(wrapper, staticVariables, enclosingType, actualType);
	ParameterizedTypeBinding parameterizedType = createParameterizedType(actualType, typeArguments, null);

	while (wrapper.signature[wrapper.start] == '.') {
		wrapper.start++; // skip '.'
		char[] memberName = wrapper.nextWord();
		BinaryTypeBinding.resolveType(parameterizedType, this, false);
		ReferenceBinding memberType = parameterizedType.type.getMemberType(memberName);
		if (wrapper.signature[wrapper.start] == '<') {
			wrapper.start++; // skip '<'
			typeArguments = getTypeArgumentsFromSignature(wrapper, staticVariables, enclosingType, memberType);
		} else {
			typeArguments = null;
		}
		parameterizedType = createParameterizedType(memberType, typeArguments, parameterizedType);
	}
	wrapper.start++; // skip ';'
	return dimension == 0 ? (TypeBinding) parameterizedType : createArrayType(parameterizedType, dimension);
}
TypeBinding getTypeFromVariantTypeSignature(
	SignatureWrapper wrapper,
	TypeVariableBinding[] staticVariables,
	ReferenceBinding enclosingType,
	ReferenceBinding genericType,
	int rank) {
	// VariantTypeSignature = '-' TypeSignature
	//   or '+' TypeSignature
	//   or TypeSignature
	//   or '*'
	switch (wrapper.signature[wrapper.start]) {
		case '-' :
			// ? super aType
			wrapper.start++;
			TypeBinding bound = getTypeFromTypeSignature(wrapper, staticVariables, enclosingType);
			return createWildcard(genericType, rank, bound, Wildcard.SUPER);
		case '+' :
			// ? extends aType
			wrapper.start++;
			bound = getTypeFromTypeSignature(wrapper, staticVariables, enclosingType);
			return createWildcard(genericType, rank, bound, Wildcard.EXTENDS);
		case '*' :
			// ?
			wrapper.start++;
			return createWildcard(genericType, rank, null, Wildcard.UNBOUND);
		default :
			return getTypeFromTypeSignature(wrapper, staticVariables, enclosingType);
	}
}
/* Ask the oracle if a package exists named name in the package named compoundName.
*/

boolean isPackage(char[][] compoundName, char[] name) {
	if (compoundName == null || compoundName.length == 0)
		return nameEnvironment.isPackage(null, name);
	return nameEnvironment.isPackage(compoundName, name);
}
// The method verifier is lazily initialized to guarantee the receiver, the compiler & the oracle are ready.

public MethodVerifier methodVerifier() {
	if (verifier == null)
		verifier = this.options.sourceLevel < ClassFileConstants.JDK1_5
			? new MethodVerifier(this)
			: new MethodVerifier15(this);
	return verifier;
}
public void reset() {
	this.defaultPackage = new PackageBinding(this); // assume the default package always exists
	this.defaultImports = null;
	this.knownPackages = new HashtableOfPackage();

	this.verifier = null;
	for (int i = this.uniqueArrayBindings.length; --i >= 0;) {
		ArrayBinding[] arrayBindings = this.uniqueArrayBindings[i];
		if (arrayBindings != null)
			for (int j = arrayBindings.length; --j >= 0;)
				arrayBindings[j] = null;
	}
	this.uniqueParameterizedTypeBindings = new SimpleLookupTable(3);
	this.uniqueRawTypeBindings = new SimpleLookupTable(3);
	this.uniqueWildcardBindings = new SimpleLookupTable(3);

	for (int i = this.units.length; --i >= 0;)
		this.units[i] = null;
	this.lastUnitIndex = -1;
	this.lastCompletedUnitIndex = -1;
	this.unitBeingCompleted = null; // in case AbortException occurred

	// name environment has a longer life cycle, and must be reset in
	// the code which created it.
}
void updateCaches(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType) {
	// walk all the unique collections & replace the unresolvedType with the resolvedType
	// must prevent 2 entries so == still works (1 containing the unresolvedType and the other containing the resolvedType)
	if (uniqueParameterizedTypeBindings.get(unresolvedType) != null) { // update the key
		Object[] keys = uniqueParameterizedTypeBindings.keyTable;
		for (int i = 0, l = keys.length; i < l; i++) {
			if (keys[i] == unresolvedType) {
				keys[i] = resolvedType; // hashCode is based on compoundName so this works - cannot be raw since type of parameterized type
				break;
			}
		}
	}

	if (uniqueWildcardBindings.get(unresolvedType) != null) { // update the key
		Object[] keys = uniqueWildcardBindings.keyTable;
		for (int i = 0, l = keys.length; i < l; i++) {
			if (keys[i] == unresolvedType) {
				keys[i] = resolvedType.isGenericType() ? createRawType(resolvedType, null) : resolvedType; // hashCode is based on compoundName so this works
				break;
			}
		}
	}
}
}
