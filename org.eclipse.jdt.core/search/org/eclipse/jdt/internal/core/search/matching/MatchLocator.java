package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import org.eclipse.core.resources.*;

import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.*;

import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;

/**
 * Locate matches in compilation units.
 */
public class MatchLocator implements ITypeRequestor {
	public SearchPattern pattern;
	public int detailLevel;
	public IJavaSearchResultCollector collector;
	public IJavaSearchScope scope;

	private MatchLocatorParser parser;
	private LookupEnvironment lookupEnvironment;
	private IResource currentResource;
	private Openable currentOpenable;
public MatchLocator(
	SearchPattern pattern,
	int detailLevel,
	IJavaSearchResultCollector collector, 
	IJavaSearchScope scope) {
		
	this.pattern = pattern;
	this.detailLevel = detailLevel;
	this.collector = collector;
	this.scope = scope;
}
/**
 * Add an additional binary type
 */
public void accept(IBinaryType binaryType, PackageBinding packageBinding) {
	this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
}
/**
 * Add an additional compilation unit.
 */
public void accept(ICompilationUnit sourceUnit) {
	CompilationResult result = new CompilationResult(sourceUnit, 1, 1);
	CompilationUnitDeclaration parsedUnit = this.parser.dietParse(sourceUnit, result);

	this.lookupEnvironment.buildTypeBindings(parsedUnit);
	this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
}
/**
 * Add an additional source type
 */
public void accept(ISourceType sourceType, PackageBinding packageBinding) {
	while (sourceType.getEnclosingType() != null) sourceType = sourceType.getEnclosingType();
	CompilationResult result = new CompilationResult(sourceType.getFileName(), 1, 1); // need to hold onto this
	CompilationUnitDeclaration unit =
		SourceTypeConverter.buildCompilationUnit(sourceType, true, true, lookupEnvironment.problemReporter, result);

	if (unit != null) {
		this.lookupEnvironment.buildTypeBindings(unit);
		this.lookupEnvironment.completeTypeBindings(unit, true);
	}
}
/**
 * Creates an IField from the given field declaration and simple type names. 
 */
private IField createFieldHandle(FieldDeclaration field, char[][] definingTypeNames) {
	IType type = this.createTypeHandle(definingTypeNames);
	return type.getField(new String(field.name));
}
/**
 * Creates an IImportDeclaration from the given import statement
 */
private IImportDeclaration createImportHandle(ImportReference importRef) {
	char[] importName = CharOperation.concatWith(importRef.getImportName(), '.');
	if (importRef.onDemand) {
		importName = CharOperation.concat(importName, ".*"/*nonNLS*/.toCharArray());
	}
	return ((CompilationUnit)this.currentOpenable).getImport(
			new String(importName));
}
/**
 * Creates an IInitializer from the given field declaration and simple type names. 
 */
private IInitializer createInitializerHandle(TypeDeclaration typeDecl, FieldDeclaration initializer, char[][] definingTypeNames) {
	IType type = this.createTypeHandle(definingTypeNames);

	// find occurence count of the given initializer in its type declaration
	int occurrenceCount = 0;
	FieldDeclaration[] fields = typeDecl.fields;
	for (int i = 0, length = fields.length; i < length; i++) {
		FieldDeclaration field = fields[i];
		if (!field.isField()) {
			occurrenceCount++;
			if (field.equals(initializer)) {
				break;
			}
		}
	}
	
	return type.getInitializer(occurrenceCount);
}
/**
 * Creates an IMethod from the given method declaration and simple type names. 
 */
private IMethod createMethodHandle(AbstractMethodDeclaration method, char[][] definingTypeNames) {
	IType type = this.createTypeHandle(definingTypeNames);
	Argument[] arguments = method.arguments;
	int length = arguments == null ? 0 : arguments.length;
	String[] parameterTypeSignatures = new String[length];
	for (int i = 0; i < length; i++) {
		TypeReference parameterType = arguments[i].type;
		char[] typeName = CharOperation.concatWith(parameterType.getTypeName(), '.');
		for (int j = 0; j < parameterType.dimensions(); j++) {
			typeName = CharOperation.concat(typeName, "[]"/*nonNLS*/.toCharArray());
		}
		parameterTypeSignatures[i] = Signature.createTypeSignature(typeName, false);
	}
	return type.getMethod(
		new String(method.selector), 
		parameterTypeSignatures);
}
/**
 * Creates an IType from the given simple type names. 
 */
private IType createTypeHandle(char[][] simpleTypeNames) {
	// creates compilation unit
	CompilationUnit unit = (CompilationUnit) this.currentOpenable;

	// create type
	int length = simpleTypeNames.length;
	IType type = unit.getType(new String(simpleTypeNames[0]));
	for (int i = 1; i < length; i++) {
		type = type.getType(new String(simpleTypeNames[i]));
	}
	return type;
}
private char[] getContents(IFile file) {
	BufferedInputStream input = null;
	try {
		input = new BufferedInputStream(file.getContents(true));
		StringBuffer buffer= new StringBuffer();
		int nextChar = input.read();
		while (nextChar != -1) {
			buffer.append( (char)nextChar );
			nextChar = input.read();
		}
		int length = buffer.length();
		char[] result = new char[length];
		buffer.getChars(0, length, result, 0);
		return result;
	} catch (IOException e) {
		return null;
	} catch (CoreException e) {
		return null;
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				// nothing can be done if the file cannot be closed
			}
		}
	}
}
protected IResource getCurrentResource() {
	return this.currentResource;
}
protected Scanner getScanner() {
	return this.parser == null ? null : this.parser.scanner;
}
/**
 * Locate the matches in the given files and report them using the search requestor. 
 */
public void locateMatches(String[] filePaths, IWorkspace workspace) throws JavaModelException {
	Util.sort(filePaths); // sort by projects
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	HandleFactory factory = new HandleFactory(workspace.getRoot(), manager);
	JavaProject previousJavaProject = null;
	int length = filePaths.length;
	double increment = 100.0 / length;
	double totalWork = 0;
	int lastProgress = 0;
	boolean couldInitializePattern = false;
	for (int i = 0; i < length; i++) {
		IProgressMonitor monitor = this.collector.getProgressMonitor();
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		String pathString = filePaths[i];
		this.currentOpenable = factory.createOpenable(pathString);
		if (this.currentOpenable == null) continue;  // match is outside classpath

		// create new parser and lookup environment if this is a new project
		try {
			JavaProject javaProject = (JavaProject)this.currentOpenable.getJavaProject();
			this.currentResource = this.currentOpenable.getUnderlyingResource();
			if (this.currentResource == null) { // case of a file in an external jar
				this.currentResource = javaProject.getProject();
			}
			if (!javaProject.equals(previousJavaProject)) {
				// create parser for this project
				couldInitializePattern = this.createParser(javaProject);
				previousJavaProject = javaProject;
			}
			if (!couldInitializePattern) continue; // the pattern could not be initialized: the match cannot be in this project
		} catch (JavaModelException e) {
			// file doesn't exist -> skip it
			continue;
		}

		// locate matches in current file and report them
		try {
			if (this.currentOpenable instanceof CompilationUnit) {
				this.locateMatchesInCompilationUnit();
			} else if (this.currentOpenable instanceof org.eclipse.jdt.internal.core.ClassFile) {
				this.locateMatchesInClassFile();
			}
		} catch (AbortCompilation e) {
			// problem with class path: it could not find base classes
			throw new JavaModelException(e, IJavaModelStatusConstants.BUILDER_INITIALIZATION_ERROR);
		} catch (CoreException e) {
			if (e instanceof JavaModelException) {
				throw (JavaModelException)e;
			} else {
				throw new JavaModelException(e);
			}
		}
		if (monitor != null) {
			totalWork = totalWork + increment;
			int worked = (int)totalWork - lastProgress;
			monitor.worked(worked);
			lastProgress = (int)totalWork;
		}
	}
}
/**
 * Locate declaration in the current class file. This class file is always in a jar.
 */
private void locateMatchesInClassFile() throws CoreException, JavaModelException {
	org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.currentOpenable;
	BinaryType binaryType = (BinaryType)classFile.getType();
	IBinaryType info;
	if (classFile.isOpen()) {
		// reuse the info from the java model cache
		info = (IBinaryType)binaryType.getRawInfo();
	} else {
		// create a temporary info
		try {
			IJavaElement pkg = classFile.getParent();
			PackageFragmentRoot root = (PackageFragmentRoot)pkg.getParent();
			if (root.isArchive()) {
				// class file in a jar
				String pkgPath = pkg.getElementName().replace('.', '/');
				String classFilePath = 
					(pkgPath.length() > 0) ?
						pkgPath + "/"/*nonNLS*/ + classFile.getElementName() :
						classFile.getElementName();
				ZipFile zipFile = null;
				try {
					zipFile = ((JarPackageFragmentRoot)root).getJar();
					info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(
						zipFile,
						classFilePath);
				} finally {
					if (zipFile != null) {
						try {
							zipFile.close();
						} catch (IOException e) {
							// ignore 
						}
					}
				}
			} else {
				// class file in a directory
				String osPath = this.currentResource.getFullPath().toOSString();
				info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(osPath);
			}
		} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
			e.printStackTrace();
			return;
		} catch (java.io.IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
		
	}

	// check class definition
	if (this.pattern.matchesBinary(info, null)) {
		this.reportBinaryMatch(binaryType, info, IJavaSearchResultCollector.EXACT_MATCH);
	}

	boolean compilationAborted = false;
	if (this.pattern.needsResolve) {
		// resolve
		BinaryTypeBinding binding = null;
		try {
			binding = this.lookupEnvironment.cacheBinaryType(info);
			if (binding == null) { // it was already cached as a result of a previous query
				char[][] compoundName = CharOperation.splitOn('.', binaryType.getFullyQualifiedName().toCharArray());
				ReferenceBinding referenceBinding = this.lookupEnvironment.getCachedType(compoundName);
				if (referenceBinding != null && (referenceBinding instanceof BinaryTypeBinding)) {
					// if the binding could be found and if it comes from a source type,
					binding = (BinaryTypeBinding)referenceBinding;
				}
			}

			// check methods
			if (binding != null) {
				MethodBinding[] methods = binding.methods();
				for (int i = 0; i < methods.length; i++) {
					MethodBinding method = methods[i];
					if (this.pattern.matches(method)) {
						IMethod methodHandle = 
							binaryType.getMethod(
								new String(method.isConstructor() ? binding.compoundName[binding.compoundName.length-1] : method.selector),
								Signature.getParameterTypes(new String(method.signature()).replace('/', '.'))
							);
						this.reportBinaryMatch(methodHandle, info, IJavaSearchResultCollector.EXACT_MATCH);
					}
				}
			}
		
			// check fields
			if (binding != null) {
				FieldBinding[] fields = binding.fields();
				for (int i = 0; i < fields.length; i++) {
					FieldBinding field = fields[i];
					if (this.pattern.matches(field)) {
						IField fieldHandle = binaryType.getField(new String(field.name));
						this.reportBinaryMatch(fieldHandle, info, IJavaSearchResultCollector.EXACT_MATCH);
					}
				}
			}
		} catch (AbortCompilation e) {
			binding = null;
		}

		// no need to check binary info if resolve was successful
		compilationAborted = binding == null;
		if (!compilationAborted) return;
	}

	// if compilation was aborted it is a problem with the class path: 
	// report as a potential match if binary info matches the pattern
	int accuracy = compilationAborted ? IJavaSearchResultCollector.POTENTIAL_MATCH : IJavaSearchResultCollector.EXACT_MATCH;
	
	// check methods
	IBinaryMethod[] methods = info.getMethods();
	int length = methods == null ? 0 : methods.length;
	for (int i = 0; i < length; i++) {
		IBinaryMethod method = methods[i];
		if (this.pattern.matchesBinary(method, info)) {
			IMethod methodHandle = 
				binaryType.getMethod(
					new String(method.isConstructor() ? info.getName() : method.getSelector()),
					Signature.getParameterTypes(new String(method.getMethodDescriptor()).replace('/', '.'))
				);
			this.reportBinaryMatch(methodHandle, info, accuracy);
		}
	}

	// check fields
	IBinaryField[] fields = info.getFields();
	length = fields == null ? 0 : fields.length;
	for (int i = 0; i < length; i++) {
		IBinaryField field = fields[i];
		if (this.pattern.matchesBinary(field, info)) {
			IField fieldHandle = binaryType.getField(new String(field.getName()));
			this.reportBinaryMatch(fieldHandle, info, accuracy);
		}
	}
}
private void locateMatchesInCompilationUnit() throws CoreException {
	// get source
	final char[] source = getContents((IFile)this.currentResource);

	// get main type name
	String pathString = this.currentResource.toString();
	int lastDot = pathString.lastIndexOf('/');
	// remove folder path and extension ".java"
	final char[] mainTypeName = pathString.substring(lastDot+1, pathString.length()-5).toCharArray(); 

	// parse
	ICompilationUnit sourceUnit = new ICompilationUnit() {
		public char[] getContents() {
			return source;
		}
		public char[] getFileName() {
			return MatchLocator.this.currentResource.getName().toCharArray();
		}
		public char[] getMainTypeName() {
			return mainTypeName;
		}
	};
	MatchSet set = new MatchSet(this);
	this.parser.matchSet = set;
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0);  
	CompilationUnitDeclaration parsedUnit = this.parser.parse(sourceUnit, compilationResult);

	if (parsedUnit != null) {
		// report matches that don't need resolve
		set.cuHasBeenResolved = false;
		set.accuracy = IJavaSearchResultCollector.EXACT_MATCH;
		set.reportMatching(parsedUnit);
		
		// resolve if needed
		if (set.needsResolve()) {
			if (parsedUnit.types != null) {
				/**
				 * First approximation: reset the lookup environment -> this will recreate the bindings for the current CU
				 * Optimization: the binding resolution should be done for all compilation units at once
				 */
				this.lookupEnvironment.reset();

				try {
					lookupEnvironment.buildTypeBindings(parsedUnit);
					if (parsedUnit.scope != null) {
						lookupEnvironment.completeTypeBindings(parsedUnit, true);
						parsedUnit.scope.faultInTypes();
						parsedUnit.resolve();
						//this.pattern.initializeFromLookupEnvironment(this.lookupEnvironment);
					}
					// report matches that needed resolve
					set.cuHasBeenResolved = true;
					set.accuracy = IJavaSearchResultCollector.EXACT_MATCH;
					set.reportMatching(parsedUnit);
				} catch (AbortCompilation e) {
					// could not resolve (reasons include "could not find library class") -> ignore and report the unresolved nodes
					set.cuHasBeenResolved = false;
					set.accuracy = IJavaSearchResultCollector.POTENTIAL_MATCH;
					set.reportMatching(parsedUnit);
				}
			}
		}
	}
}
/**
 * Locates the package declarations corresponding to this locator's pattern. 
 */
public void locatePackageDeclarations(IWorkspace workspace) throws JavaModelException {
	this.locatePackageDeclarations(this.pattern, workspace);
}
/**
 * Locates the package declarations corresponding to the search pattern. 
 */
private void locatePackageDeclarations(SearchPattern searchPattern, IWorkspace workspace) throws JavaModelException {
	if (searchPattern instanceof OrPattern) {
		OrPattern orPattern = (OrPattern)searchPattern;
		this.locatePackageDeclarations(orPattern.leftPattern, workspace);
		this.locatePackageDeclarations(orPattern.rightPattern, workspace);
	} else if (searchPattern instanceof PackageDeclarationPattern) {
		PackageDeclarationPattern pkgPattern = (PackageDeclarationPattern)searchPattern;
		String pkgName = new String(pkgPattern.pkgName);
		IJavaProject[] projects = JavaModelManager.getJavaModel(workspace).getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject javaProject = projects[i];
			IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
			for (int j = 0, rootsLength = roots.length; j < rootsLength; j++) {
				IJavaElement[] pkgs = roots[j].getChildren();
				for (int k = 0, pksLength = pkgs.length; k < pksLength; k++) {
					IJavaElement pkg = pkgs[k];
					if (pkgPattern.matchesName(pkgPattern.pkgName, pkg.getElementName().toCharArray())) {
						this.currentResource = pkg.getUnderlyingResource();
						if (this.currentResource == null) { // case of a file in an external jar
							this.currentResource = javaProject.getProject();
						}
						try {
							this.report(-1, -2, pkg, IJavaSearchResultCollector.EXACT_MATCH);
						} catch (CoreException e) {
							if (e instanceof JavaModelException) {
								throw (JavaModelException)e;
							} else {
								throw new JavaModelException(e);
							}
						}
					}
				}
			}
		}
	}
}
public void report(int sourceStart, int sourceEnd, IJavaElement element, int accuracy) throws CoreException {
	if (this.scope.encloses(element)) {
		this.collector.accept(
			this.currentResource,
			sourceStart,
			sourceEnd + 1,
			element, 
			accuracy
		);
	}
}
private void reportBinaryMatch(IMember binaryMember, IBinaryType info, int accuracy) throws CoreException, JavaModelException {
	ISourceRange range = binaryMember.getNameRange();
	if (range.getOffset() == -1) {
		ClassFile classFile = (ClassFile)binaryMember.getClassFile();
		SourceMapper mapper = classFile.getSourceMapper();
		if (mapper != null) {
			IType type = classFile.getType();
			char[] contents = mapper.findSource(type, info);
			if (contents != null) {
				range = mapper.mapSource(type, contents, binaryMember);
			}
		}
	}
	int startIndex = range.getOffset();
	int endIndex = startIndex + range.getLength() - 1;
	this.report(startIndex, endIndex, binaryMember, accuracy);
}
/**
 * Reports the given field declaration to the search requestor.
 * Its defining types have the given simple names.
 */
public void reportFieldDeclaration(
	FieldDeclaration fieldDeclaration, 
	char[][] definingTypeNames, 
	int accuracy) throws CoreException {
		
	// create field handle
	IType type = this.createTypeHandle(definingTypeNames);
	IField field = type.getField(new String(fieldDeclaration.name));
	
	// accept field declaration
	this.report(fieldDeclaration.sourceStart, fieldDeclaration.sourceEnd, field, accuracy);
}
/**
 * Reports the given import to the search requestor.
 */
public void reportImport(ImportReference reference, int accuracy) throws CoreException {

	// create defining import handle
	IImportDeclaration importHandle = this.createImportHandle(reference);
	
	// accept reference
	this.pattern.matchReportReference(reference, importHandle, accuracy, this);	
}
/**
 * Reports the given method declaration to the search requestor.
 * Its defining types have the given simple names.
 */
public void reportMethodDeclaration(
	AbstractMethodDeclaration methodDeclaration, 
	char[][] definingTypeNames,
	int accuracy) throws CoreException {
		
	// create method handle
	IMethod method = this.createMethodHandle(methodDeclaration, definingTypeNames);

	// compute source positions of the selector 
	Scanner scanner = parser.scanner;
	int nameSourceStart = methodDeclaration.sourceStart;
	scanner.resetTo(nameSourceStart, methodDeclaration.sourceEnd);
	try {
		scanner.getNextToken();
	} catch(InvalidInputException e) {
	}
	int nameSourceEnd = scanner.currentPosition-1;

	// accept method declaration
	this.report(nameSourceStart, nameSourceEnd, method, accuracy);
}
/**
 * Reports the given package declaration to the search requestor.
 */
public void reportPackageDeclaration(ImportReference node) {
	// TBD
}
/**
 * Reports the given package reference to the search requestor.
 */
public void reportPackageReference(ImportReference node) {
	// TBD
}
/**
 * Reports the given qualified reference to the search requestor.
 */
public void reportQualifiedReference(
	int sourceStart,
	int sourceEnd,
	char[][] qualifiedName,
	IJavaElement element,
	int accuracy) throws CoreException {
		
	// compute source positions of the qualified reference 
	Scanner scanner = parser.scanner;
	scanner.resetTo(sourceStart, sourceEnd);

	int refSourceStart = -1, refSourceEnd = -1;
	int tokenNumber = qualifiedName.length;
	int token = -1;
	int previousValid = -1;
	int i = 0;
	do {
		int currentPosition = scanner.currentPosition;
		// read token
		try {
			token = scanner.getNextToken();
		} catch(InvalidInputException e) {
		}
		if (token != TerminalSymbols.TokenNameEOF) {
			char[] currentTokenSource = scanner.getCurrentTokenSource();
			while (i < tokenNumber && !CharOperation.equals(currentTokenSource, qualifiedName[i++])) {
			}
			if (CharOperation.equals(currentTokenSource, qualifiedName[i-1]) && (previousValid == -1 || previousValid == i-2)) {
				previousValid = i-1;
				if (refSourceStart == -1) {
					refSourceStart = currentPosition;
				}
				refSourceEnd = scanner.currentPosition-1;
			} else {
				i = 0;
				refSourceStart = -1;
				previousValid = -1;
			}
			// read '.'
			try {
				token = scanner.getNextToken();
			} catch(InvalidInputException e) {
			}
		} 
	} while (token != TerminalSymbols.TokenNameEOF && i < tokenNumber);

	// accept method declaration
	if (refSourceStart != -1) {
		this.report(refSourceStart, refSourceEnd, element, accuracy);
	} else {
		this.report(sourceStart, sourceEnd, element, accuracy);
	}
}
/**
 * Reports the given reference to the search requestor.
 * It is done in the given method and the method's defining types 
 * have the given simple names.
 */
public void reportReference(
	AstNode reference, 
	AbstractMethodDeclaration methodDeclaration, 
	char[][] definingTypeNames, 
	int accuracy) throws CoreException {
		
	// create defining method handle
	IMethod method = this.createMethodHandle(methodDeclaration, definingTypeNames);
	
	// accept reference
	if (reference instanceof QualifiedNameReference || reference instanceof QualifiedTypeReference) {
		this.pattern.matchReportReference((AstNode)reference, method, accuracy, this);
	} else if (reference instanceof MessageSend) { // message ref are starting at the selector start
		this.report((int)(((MessageSend)reference).nameSourcePosition >> 32), reference.sourceEnd, method, accuracy);
	} else {
		this.report(reference.sourceStart, reference.sourceEnd, method, accuracy);
	}
}
/**
 * Reports the given reference to the search requestor.
 * It is done in the given field and given type.
 * The field's defining types have the given simple names.
 */
public void reportReference(
	AstNode reference,
	TypeDeclaration typeDeclaration,
	FieldDeclaration fieldDeclaration, 
	char[][] definingTypeNames, 
	int accuracy) throws CoreException {

	if (fieldDeclaration.isField()) {
		// create defining field handle
		IField field = this.createFieldHandle(fieldDeclaration, definingTypeNames);
	
		// accept reference
		if (reference instanceof QualifiedNameReference || reference instanceof QualifiedTypeReference) {
			this.pattern.matchReportReference((AstNode)reference, field, accuracy, this);
		} else if (reference instanceof MessageSend) { // message ref are starting at the selector start
			this.report((int)(((MessageSend)reference).nameSourcePosition >> 32), reference.sourceEnd, field, accuracy);
		} else {
			this.report(reference.sourceStart, reference.sourceEnd, field, accuracy);
		}
	} else { // initializer
		// create defining initializer
		IInitializer initializer = this.createInitializerHandle(typeDeclaration, fieldDeclaration, definingTypeNames);
		
		// accept reference
		if (reference instanceof QualifiedNameReference || reference instanceof QualifiedTypeReference) {
			this.pattern.matchReportReference((AstNode)reference, initializer, accuracy, this);
		} else if (reference instanceof MessageSend) { // message ref are starting at the selector start
			this.report((int)(((MessageSend)reference).nameSourcePosition >> 32), reference.sourceEnd, initializer, accuracy);
		} else {
			this.report(reference.sourceStart, reference.sourceEnd, initializer, accuracy);
		}
	}
}
/**
 * Reports the given super type reference to the search requestor.
 * It is done in the given defining type (with the given simple names).
 */
public void reportSuperTypeReference(
	TypeReference typeRef, 
	char[][] definingTypeNames, 
	int accuracy) throws CoreException {
		
	// create defining type handle
	IType type = this.createTypeHandle(definingTypeNames);
	
	// accept type reference
	this.pattern.matchReportReference(typeRef, type, accuracy, this);
}
/**
 * Reports the given type declaration to the search requestor.
 * Its simple names are the names of its outer most type to this type.
 */
public void reportTypeDeclaration(
	TypeDeclaration typeDeclaration, 
	char[][] simpleTypeNames, 
	int accuracy) throws CoreException {
		
	// create type handle
	IType type = this.createTypeHandle(simpleTypeNames);
	
	// accept class or interface declaration
	this.report(typeDeclaration.sourceStart, typeDeclaration.sourceEnd, type, accuracy);
}

/**
 * Create a new parser for the given project, as well as a lookup environment.
 * Asks the pattern to initialize itself from the lookup environment.
 * Returns whether it was able to initialize the pattern.
 */
private boolean createParser(JavaProject project) throws JavaModelException {
	INameEnvironment nameEnvironment = project.getSearchableNameEnvironment();
	IProblemFactory problemFactory = new DefaultProblemFactory();

	CompilerOptions options = new CompilerOptions(null);
	ProblemReporter problemReporter =
		new ProblemReporter(
			DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			options,
			problemFactory);
	this.lookupEnvironment = new LookupEnvironment(this, options, problemReporter, nameEnvironment);
	this.parser = new MatchLocatorParser(problemReporter);
	return this.pattern.initializeFromLookupEnvironment(this.lookupEnvironment);
}
}
