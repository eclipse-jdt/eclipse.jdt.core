/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.HierarchyScope;

public class MatchLocator implements ITypeRequestor {

public static final int MAX_AT_ONCE = 500;

// permanent state
public SearchPattern pattern;
public int detailLevel;
public IJavaSearchResultCollector collector;
public IJavaSearchScope scope;
public IProgressMonitor progressMonitor;

public org.eclipse.jdt.core.ICompilationUnit[] workingCopies;
public HandleFactory handleFactory;

// cache of all super type names if scope is hierarchy scope
public char[][][] allSuperTypeNames;

// the following is valid for the current project
public MatchLocatorParser parser;
private Parser basicParser;
public INameEnvironment nameEnvironment;
public NameLookup nameLookup;
public LookupEnvironment lookupEnvironment;
public HierarchyResolver hierarchyResolver;

public CompilerOptions options;

// management of PotentialMatch to be processed
public int numberOfMatches; // (numberOfMatches - 1) is the last unit in matchesToProcess
public PotentialMatch[] matchesToProcess;
public PotentialMatch currentPotentialMatch;

/*
 * Time spent in the IJavaSearchResultCollector
 */
public long resultCollectorTime = 0;

public static IType getTopLevelType(IType binaryType) {
	// ensure it is not a local or anoymous type (see bug 28752  J Search resports non-existent Java element)
	String typeName = binaryType.getElementName();
	int lastDollar = typeName.lastIndexOf('$');
	int length = typeName.length();
	if (lastDollar != -1 && lastDollar < length-1) {
		if (Character.isDigit(typeName.charAt(lastDollar+1))) {
			// local or anonymous type
			typeName = typeName.substring(0, lastDollar);
			IClassFile classFile = binaryType.getPackageFragment().getClassFile(typeName + SuffixConstants.SUFFIX_STRING_class);
			try {
				binaryType = classFile.getType();
			} catch (JavaModelException e) {
				// ignore as implementation of getType() cannot throw this exception
			}
		}
	}

	// ensure it is a top level type
	IType declaringType = binaryType.getDeclaringType();
	while (declaringType != null) {
		binaryType = declaringType;
		declaringType = binaryType.getDeclaringType();
	}
	return binaryType;
}

public MatchLocator(
	SearchPattern pattern,
	int detailLevel,
	IJavaSearchResultCollector collector,
	IJavaSearchScope scope,
	IProgressMonitor progressMonitor) {
		
	this.pattern = pattern;
	this.detailLevel = detailLevel;
	this.collector = collector;
	this.scope = scope;
	this.progressMonitor = progressMonitor;
}
/**
 * Add an additional binary type
 */
public void accept(IBinaryType binaryType, PackageBinding packageBinding) {
	this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
}
/**
 * Add an additional compilation unit into the loop
 *  ->  build compilation unit declarations, their bindings and record their results.
 */
public void accept(ICompilationUnit sourceUnit) {
	// Switch the current policy and compilation result for this unit to the requested one.
	CompilationResult unitResult = new CompilationResult(sourceUnit, 1, 1, this.options.maxProblemsPerUnit);
	try {
		CompilationUnitDeclaration parsedUnit = basicParser().dietParse(sourceUnit, unitResult);
		lookupEnvironment.buildTypeBindings(parsedUnit);
		lookupEnvironment.completeTypeBindings(parsedUnit);
	} catch (AbortCompilationUnit e) {
		// at this point, currentCompilationUnitResult may not be sourceUnit, but some other
		// one requested further along to resolve sourceUnit.
		if (unitResult.compilationUnit == sourceUnit) { // only report once
			//requestor.acceptResult(unitResult.tagAsAccepted());
		} else {
			throw e; // want to abort enclosing request to compile
		}
	}
}
/**
 * Add additional source types
 */
public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding) {
	// case of SearchableEnvironment of an IJavaProject is used
	ISourceType sourceType = sourceTypes[0];
	while (sourceType.getEnclosingType() != null)
		sourceType = sourceType.getEnclosingType();
	if (sourceType instanceof SourceTypeElementInfo) {
		// get source
		SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) sourceType;
		IType type = elementInfo.getHandle();
		ICompilationUnit sourceUnit = (ICompilationUnit) type.getCompilationUnit();
		accept(sourceUnit);
	} else {
		CompilationResult result = new CompilationResult(sourceType.getFileName(), 1, 1, 0);
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,
				true, // need field and methods
				true, // need member types
				false, // no need for field initialization
				lookupEnvironment.problemReporter,
				result);
		this.lookupEnvironment.buildTypeBindings(unit);
		this.lookupEnvironment.completeTypeBindings(unit, true);
	}
}	
protected Parser basicParser() {
	if (this.basicParser == null) {
		ProblemReporter problemReporter =
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				this.options,
				new DefaultProblemFactory());
		this.basicParser = new Parser(problemReporter, false);
	}
	return this.basicParser;
}
/**
 * Add the potentialMatch to the loop
 *  ->  build compilation unit declarations, their bindings and record their results.
 */
protected void buildBindings(PotentialMatch potentialMatch) {
	if (this.progressMonitor != null && this.progressMonitor.isCanceled())
		throw new OperationCanceledException();

	try {
		if (SearchEngine.VERBOSE)
			System.out.println("Parsing " + potentialMatch.openable.toStringWithAncestors()); //$NON-NLS-1$

		this.parser.matchSet = potentialMatch.matchingNodeSet;
		CompilationResult unitResult = new CompilationResult(potentialMatch, 1, 1, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = this.parser.dietParse(potentialMatch, unitResult);
		if (parsedUnit != null) {
			if (!parsedUnit.isEmpty())
				this.lookupEnvironment.buildTypeBindings(parsedUnit);

			// add the potentialMatch with its parsedUnit to matchesToProcess
			potentialMatch.parsedUnit = parsedUnit;
			int size = this.matchesToProcess.length;
			if (this.numberOfMatches == size)
				System.arraycopy(this.matchesToProcess, 0, this.matchesToProcess = new PotentialMatch[size == 0 ? 1 : size * 2], 0, this.numberOfMatches);
			this.matchesToProcess[this.numberOfMatches++] = potentialMatch;

			if (this.progressMonitor != null)
				this.progressMonitor.worked(4);
		}
	} finally {
		this.parser.matchSet = null;
	}
}
/*
 * Caches the given binary type in the lookup environment and returns it.
 * Returns the existing one if already cached.
 * Returns null if source type binding was cached.
 */
protected BinaryTypeBinding cacheBinaryType(IType type) throws JavaModelException {
	IType enclosingType = type.getDeclaringType();
	if (enclosingType != null)
		cacheBinaryType(enclosingType); // cache enclosing types first, so that binary type can be found in lookup enviroment
	IBinaryType binaryType = (IBinaryType) ((BinaryType) type).getElementInfo();
	BinaryTypeBinding binding = this.lookupEnvironment.cacheBinaryType(binaryType);
	if (binding == null) { // it was already cached as a result of a previous query
		char[][] compoundName = CharOperation.splitOn('.', type.getFullyQualifiedName().toCharArray());
		ReferenceBinding referenceBinding = this.lookupEnvironment.getCachedType(compoundName);
		if (referenceBinding != null && (referenceBinding instanceof BinaryTypeBinding))
			binding = (BinaryTypeBinding) referenceBinding; // if the binding could be found and if it comes from a binary type
	}
	return binding;
}
protected  ClassFileReader classFileReader(IType type) {
	IClassFile classFile = type.getClassFile(); 
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	if (classFile.isOpen())
		return (ClassFileReader) manager.getInfo(type);

	IPackageFragment pkg = type.getPackageFragment();
	IPackageFragmentRoot root = (IPackageFragmentRoot)pkg.getParent();
	try {
		if (!root.isArchive())
			return ClassFileReader.read(type.getPath().toOSString());

		IPath zipPath = root.isExternal() ? root.getPath() : root.getResource().getLocation();
		if (zipPath == null) return null; // location is null
		ZipFile zipFile = null;
		try {
			if (JavaModelManager.ZIP_ACCESS_VERBOSE)
				System.out.println("(" + Thread.currentThread() + ") [MatchLocator.classFileReader()] Creating ZipFile on " + zipPath); //$NON-NLS-1$	//$NON-NLS-2$
			zipFile = manager.getZipFile(zipPath);
			char[] pkgPath = pkg.getElementName().toCharArray();
			CharOperation.replace(pkgPath, '.', '/');
			char[] classFileName = classFile.getElementName().toCharArray();
			char[] path = pkgPath.length == 0 ? classFileName : CharOperation.concat(pkgPath, classFileName, '/');
			return ClassFileReader.read(zipFile, new String(path));
		} finally {
			manager.closeZipFile(zipFile);
		}
	} catch (ClassFormatException e) {
	} catch (CoreException e) {
	} catch (IOException e) {
	}
	return null;
}
/*
 * Computes the super type names of the focus type if any.
 */
protected char[][][] computeSuperTypeNames(IType focusType) {
	String fullyQualifiedName = focusType.getFullyQualifiedName();
	int lastDot = fullyQualifiedName.lastIndexOf('.');
	char[] qualification = lastDot == -1 ? CharOperation.NO_CHAR : fullyQualifiedName.substring(0, lastDot).toCharArray();
	char[] simpleName = focusType.getElementName().toCharArray();

	SuperTypeNamesCollector superTypeNamesCollector = 
		new SuperTypeNamesCollector(
			this.pattern, 
			simpleName,
			qualification,
			new MatchLocator(this.pattern, this.detailLevel, this.collector, this.scope, this.progressMonitor), // clone MatchLocator so that it has no side effect
			focusType, 
			this.progressMonitor);
	try {
		this.allSuperTypeNames = superTypeNamesCollector.collect();
	} catch (JavaModelException e) {
	}
	return this.allSuperTypeNames;
}
/**
 * Creates an IField from the given field declaration and type. 
 */
public IField createFieldHandle(FieldDeclaration field, IType type) {
	if (type == null) return null;
	return type.getField(new String(field.name));
}
/*
 * Creates hierarchy resolver if needed. 
 * Returns whether focus is visible.
 */
protected boolean createHierarchyResolver(IType focusType, PotentialMatch[] potentialMatches) {
	// cache focus type if not a potential match
	char[][] compoundName = CharOperation.splitOn('.', focusType.getFullyQualifiedName().toCharArray());
	boolean isPotentialMatch = false;
	for (int i = 0, length = potentialMatches.length; i < length; i++) {
		if (CharOperation.equals(potentialMatches[i].compoundName, compoundName)) {
			isPotentialMatch = true;
			break;
		}
	}
	if (!isPotentialMatch) {
		if (focusType.isBinary()) {
			try {
				cacheBinaryType(focusType);
			} catch (JavaModelException e) {
				return false;
			}
		} else {
			// cache all types in the focus' compilation unit (even secondary types)
			accept((ICompilationUnit) focusType.getCompilationUnit());
		}
	}

	// resolve focus type
	this.hierarchyResolver = new HierarchyResolver(this.lookupEnvironment, null/*hierarchy is not going to be computed*/);
	ReferenceBinding binding = this.hierarchyResolver.setFocusType(compoundName);
	return binding != null && binding.isValidBinding() && (binding.tagBits & TagBits.HierarchyHasProblems) == 0;
}
/**
 * Creates an IImportDeclaration from the given import statement
 */
public IJavaElement createImportHandle(ImportReference importRef) {
	char[] importName = CharOperation.concatWith(importRef.getImportName(), '.');
	if (importRef.onDemand)
		importName = CharOperation.concat(importName, ".*" .toCharArray()); //$NON-NLS-1$
	Openable currentOpenable = this.currentPotentialMatch.openable;
	if (currentOpenable instanceof CompilationUnit)
		return ((CompilationUnit) currentOpenable).getImport(new String(importName));

	try {
		return ((ClassFile) currentOpenable).getType();
	} catch (JavaModelException e) {
		return null;
	}
}
/**
 * Creates an IInitializer from the given field declaration and type. 
 */
public IInitializer createInitializerHandle(TypeDeclaration typeDecl, FieldDeclaration initializer, IType type) {
	if (type == null) return null;

	// find occurence count of the given initializer in its type declaration
	int occurrenceCount = 0;
	FieldDeclaration[] fields = typeDecl.fields;
	for (int i = 0, length = fields.length; i < length; i++) {
		FieldDeclaration field = fields[i];
		if (!field.isField()) {
			occurrenceCount++;
			if (field.equals(initializer)) break;
		}
	}
	return type.getInitializer(occurrenceCount);
}
/**
 * Creates an IMethod from the given method declaration and type. 
 */
public IMethod createMethodHandle(AbstractMethodDeclaration method, IType type) {
	if (type == null) return null;
	Argument[] arguments = method.arguments;
	int length = arguments == null ? 0 : arguments.length;
	if (type.isBinary()) {
		// don't cache the methods of the binary type
		ClassFileReader reader = classFileReader(type);
		if (reader == null) return null;
		IBinaryMethod[] methods = reader.getMethods();

		if (methods != null) {
			for (int i = 0, methodsLength = methods.length; i < methodsLength; i++) {
				IBinaryMethod binaryMethod = methods[i];
				char[] selector = binaryMethod.isConstructor() ? type.getElementName().toCharArray() : binaryMethod.getSelector();
				if (CharOperation.equals(selector, method.selector)) {
					String[] parameterTypes = Signature.getParameterTypes(new String(binaryMethod.getMethodDescriptor()));
					if (length != parameterTypes.length) continue;
					boolean sameParameters = true;
					for (int j = 0; j < length; j++) {
						TypeReference parameterType = arguments[j].type;
						char[] typeName = CharOperation.concatWith(parameterType.getTypeName(), '.');
						for (int k = 0; k < parameterType.dimensions(); k++)
							typeName = CharOperation.concat(typeName, "[]" .toCharArray()); //$NON-NLS-1$
						String parameterTypeName = parameterTypes[j].replace('/', '.');
						if (!Signature.toString(parameterTypeName).endsWith(new String(typeName))) {
							sameParameters = false;
							break;
						} else {
							parameterTypes[j] = parameterTypeName;
						}
					}
					if (sameParameters)
						return type.getMethod(new String(selector), parameterTypes);
				}
			}
		}
		return null;
	}

	String[] parameterTypeSignatures = new String[length];
	for (int i = 0; i < length; i++) {
		TypeReference parameterType = arguments[i].type;
		char[] typeName = CharOperation.concatWith(parameterType.getTypeName(), '.');
		for (int j = 0; j < parameterType.dimensions(); j++)
			typeName = CharOperation.concat(typeName, "[]" .toCharArray()); //$NON-NLS-1$
		parameterTypeSignatures[i] = Signature.createTypeSignature(typeName, false);
	}
	return type.getMethod(new String(method.selector), parameterTypeSignatures);
}
/**
 * Creates an IType from the given simple top level type name. 
 */
public IType createTypeHandle(char[] simpleTypeName) {
	Openable currentOpenable = this.currentPotentialMatch.openable;
	if (currentOpenable instanceof CompilationUnit)
		return ((CompilationUnit) currentOpenable).getType(new String(simpleTypeName));

	try {
		// ensure this is a top level type (see bug 20011  Searching for Inner Classes gives bad search results)
		return getTopLevelType(((ClassFile) currentOpenable).getType());
	} catch (JavaModelException e) {
		return null;
	}
}
/**
 * Creates an IType from the given simple inner type name and parent type. 
 */
public IType createTypeHandle(IType parent, char[] simpleTypeName) {
	return parent.getType(new String(simpleTypeName));
}
public IBinaryType getBinaryInfo(ClassFile classFile, IResource resource) throws CoreException {
	BinaryType binaryType = (BinaryType) classFile.getType();
	if (classFile.isOpen())
		return (IBinaryType) binaryType.getElementInfo(); // reuse the info from the java model cache

	// create a temporary info
	IBinaryType info;
	try {
		IJavaElement pkg = classFile.getParent();
		PackageFragmentRoot root = (PackageFragmentRoot) pkg.getParent();
		if (root.isArchive()) {
			// class file in a jar
			String pkgPath = pkg.getElementName().replace('.', '/');
			String classFilePath = pkgPath.length() > 0
				? pkgPath + "/" + classFile.getElementName() //$NON-NLS-1$
				: classFile.getElementName();
			ZipFile zipFile = null;
			try {
				zipFile = ((JarPackageFragmentRoot) root).getJar();
				info = ClassFileReader.read(zipFile, classFilePath);
			} finally {
				JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
			}
		} else {
			// class file in a directory
			String osPath = resource.getLocation().toOSString();
			info = ClassFileReader.read(osPath);
		}
		return info;
	} catch (ClassFormatException e) {
		//e.printStackTrace();
		return null;
	} catch (java.io.IOException e) {
		throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
	}
}
protected IType getFocusType() {
	return this.scope instanceof HierarchyScope ? ((HierarchyScope) this.scope).focusType : null;
}
protected void getMethodBodies(CompilationUnitDeclaration unit, MatchingNodeSet matchingNodeSet) {
	if (unit.ignoreMethodBodies) {
		unit.ignoreFurtherInvestigation = true;
		return; // if initial diet parse did not work, no need to dig into method bodies.
	}

	try {
		this.parser.scanner.setSource(unit.compilationResult.compilationUnit.getContents());
		this.parser.matchSet = matchingNodeSet;
		this.parser.parseBodies(unit);
	} finally {
		this.parser.matchSet = null;
	}
}
protected boolean hasAlreadyDefinedType(CompilationUnitDeclaration parsedUnit) {
	CompilationResult result = parsedUnit.compilationResult;
	if (result == null) return false;
	for (int i = 0; i < result.problemCount; i++)
		if (result.problems[i].getID() == IProblem.DuplicateTypes)
			return true;
	return false;
}	
/**
 * Create a new parser for the given project, as well as a lookup environment.
 */
public void initialize(JavaProject project, int potentialMatchSize) throws JavaModelException {
	if (this.nameEnvironment != null)
		this.nameEnvironment.cleanup();

	// if only one potential match, a file name environment costs too much,
	// so use the existing searchable  environment which will populate the java model
	// only for this potential match and its required types.
	this.nameEnvironment = potentialMatchSize == 1
		? (INameEnvironment) project.getSearchableNameEnvironment()
		: (INameEnvironment) new JavaSearchNameEnvironment(project);

	// create lookup environment
	this.options = new CompilerOptions(project.getOptions(true));
	ProblemReporter problemReporter =
		new ProblemReporter(
			DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			this.options,
			new DefaultProblemFactory());
	this.lookupEnvironment = new LookupEnvironment(this, this.options, problemReporter, this.nameEnvironment);

	this.parser = new MatchLocatorParser(problemReporter);

	// remember project's name lookup
	this.nameLookup = project.getNameLookup();

	// initialize queue of units
	this.numberOfMatches = 0;
	this.matchesToProcess = new PotentialMatch[potentialMatchSize];
}
protected void locateMatches(JavaProject javaProject, PotentialMatch[] potentialMatches, int start, int length) throws JavaModelException {
	
	initialize(javaProject, length);
	try {
		this.nameLookup.setUnitsToLookInside(this.workingCopies);
	
		// create and resolve binding (equivalent to beginCompilation() in Compiler)
		boolean bindingsWereCreated = true;
		try {
			for (int i = start, maxUnits = start + length; i < maxUnits; i++)
				buildBindings(potentialMatches[i]);
			lookupEnvironment.completeTypeBindings();
	
			// create hierarchy resolver if needed
			IType focusType = getFocusType();
			if (focusType == null) {
				this.hierarchyResolver = null;
			} else if (!createHierarchyResolver(focusType, potentialMatches)) {
				// focus type is not visible, use the super type names instead of the bindings
				if (computeSuperTypeNames(focusType) == null) return;
			}
		} catch (AbortCompilation e) {
			bindingsWereCreated = false;
		}
	
		// potential match resolution
		for (int i = 0; i < this.numberOfMatches; i++) {
			if (this.progressMonitor != null && this.progressMonitor.isCanceled())
				throw new OperationCanceledException();
			PotentialMatch potentialMatch = this.matchesToProcess[i];
			this.matchesToProcess[i] = null; // release reference to processed potential match
			try {
				process(potentialMatch, bindingsWereCreated);
			} catch (AbortCompilation e) {
				// problem with class path: it could not find base classes
				// continue and try next matching openable reporting innacurate matches (since bindings will be null)
				bindingsWereCreated = false;
			} catch (JavaModelException e) {
				// problem with class path: it could not find base classes
				// continue and try next matching openable reporting innacurate matches (since bindings will be null)
				bindingsWereCreated = false;
			} catch (CoreException e) {
				// core exception thrown by client's code: let it through
				throw new JavaModelException(e);
			} finally {
				if (this.options.verbose)
					System.out.println(Util.bind("compilation.done", //$NON-NLS-1$
						new String[] {
							String.valueOf(i + 1),
							String.valueOf(numberOfMatches),
							new String(potentialMatch.parsedUnit.getFileName())}));
				// cleanup compilation unit result
				potentialMatch.parsedUnit.cleanUp();
				potentialMatch.parsedUnit = null;
			}
			if (this.progressMonitor != null)
				this.progressMonitor.worked(5);
		}
	} finally {
		this.nameLookup.setUnitsToLookInside(null);
	}
}
/**
 * Locate the matches amongst the potential matches.
 */
protected void locateMatches(JavaProject javaProject, PotentialMatchSet matchSet) throws JavaModelException {
	PotentialMatch[] potentialMatches = matchSet.getPotentialMatches(javaProject.getPackageFragmentRoots());
	for (int index = 0, length = potentialMatches.length; index < length;) {
		int max = Math.min(MAX_AT_ONCE, length - index);
		locateMatches(javaProject, potentialMatches, index, max);
		index += max;
	}
}
/**
 * Locate the matches in the given files and report them using the search requestor. 
 */
public void locateMatches(String[] filePaths, IWorkspace workspace, org.eclipse.jdt.core.ICompilationUnit[] copies) throws JavaModelException {
	if (SearchEngine.VERBOSE) {
		System.out.println("Locating matches in files ["); //$NON-NLS-1$
		for (int i = 0, length = filePaths.length; i < length; i++)
			System.out.println("\t" + filePaths[i]); //$NON-NLS-1$
		System.out.println("]"); //$NON-NLS-1$
		if (copies != null) {
			 System.out.println("and working copies ["); //$NON-NLS-1$
			for (int i = 0, length = copies.length; i < length; i++)
				System.out.println("\t" + ((JavaElement) copies[i]).toStringWithAncestors()); //$NON-NLS-1$
			System.out.println("]"); //$NON-NLS-1$
		}
	}

	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	try {
		// optimize access to zip files during search operation
		manager.cacheZipFiles();
			
		// initialize handle factory (used as a cache of handles so as to optimize space)
		if (this.handleFactory == null)
			this.handleFactory = new HandleFactory(workspace);
		
		// substitute compilation units with working copies
		HashMap wcPaths = new HashMap(); // a map from path to working copies
		if ((this.workingCopies = copies) != null) {
			int wcLength = this.workingCopies.length;
			if (wcLength > 0) {
				String[] newPaths = new String[wcLength];
				for (int i = 0; i < wcLength; i++) {
					org.eclipse.jdt.core.ICompilationUnit workingCopy = this.workingCopies[i];
					String path = workingCopy.getPath().toString();
					wcPaths.put(path, workingCopy);
					newPaths[i] = path;
				}
				int filePathsLength = filePaths.length;
				System.arraycopy(filePaths, 0, filePaths = new String[filePathsLength + wcLength], 0, filePathsLength);
				System.arraycopy(newPaths, 0, filePaths, filePathsLength, wcLength);
			}
		}

		if (this.progressMonitor != null) {
			// 1 for file path, 4 for parsing and binding creation, 5 for binding resolution? //$NON-NLS-1$
			this.progressMonitor.beginTask("", filePaths.length * (this.pattern.mustResolve ? 10 : 5)); //$NON-NLS-1$
		}

		// initialize pattern for polymorphic search (ie. method reference pattern)
		this.pattern.initializePolymorphicSearch(this, this.progressMonitor);

		JavaProject previousJavaProject = null;
		PotentialMatchSet matchSet = new PotentialMatchSet();
		Util.sort(filePaths); 
		for (int i = 0, l = filePaths.length; i < l; i++) {
			if (this.progressMonitor != null && this.progressMonitor.isCanceled())
				throw new OperationCanceledException();

			// skip duplicate paths
			String pathString = filePaths[i];
			if (i > 0 && pathString.equals(filePaths[i - 1])) continue;
			
			Openable openable;
			org.eclipse.jdt.core.ICompilationUnit workingCopy = (org.eclipse.jdt.core.ICompilationUnit) wcPaths.get(pathString);
			if (workingCopy != null) {
				openable = (Openable) workingCopy;
			} else {
				openable = this.handleFactory.createOpenable(pathString, this.scope);
				if (openable == null) continue; // match is outside classpath
			}

			// create new parser and lookup environment if this is a new project
			IResource resource = null;
			try {
				JavaProject javaProject = (JavaProject) openable.getJavaProject();
				resource = workingCopy != null ? workingCopy.getResource() : openable.getResource();
				if (resource == null)
					resource = javaProject.getProject(); // case of a file in an external jar
				if (!javaProject.equals(previousJavaProject)) {
					// locate matches in previous project
					if (previousJavaProject != null) {
						try {
							locateMatches(previousJavaProject, matchSet);
						} catch (JavaModelException e) {
							if (e.getException() instanceof CoreException) throw e;
							// problem with classpath in this project -> skip it
						}
						matchSet.reset();
					}
					previousJavaProject = javaProject;
				}
			} catch (JavaModelException e) {
				// file doesn't exist -> skip it
				continue;
			}
			matchSet.add(new PotentialMatch(this, resource, openable));

			if (this.progressMonitor != null)
				this.progressMonitor.worked(1);
		}
		
		// last project
		if (previousJavaProject != null) {
			try {
				locateMatches(previousJavaProject, matchSet);
			} catch (JavaModelException e) {
				if (e.getException() instanceof CoreException) throw e;
				// problem with classpath in last project -> skip it
			}
		} 

		if (this.progressMonitor != null)
			this.progressMonitor.done();
	} finally {
		if (this.nameEnvironment != null)
			this.nameEnvironment.cleanup();
		manager.flushZipFiles();
	}	
}
/**
 * Locates the package declarations corresponding to this locator's pattern. 
 */
public void locatePackageDeclarations(IWorkspace workspace) throws JavaModelException {
	locatePackageDeclarations(this.pattern, workspace);
}
/**
 * Locates the package declarations corresponding to the search pattern. 
 */
protected void locatePackageDeclarations(SearchPattern searchPattern, IWorkspace workspace) throws JavaModelException {
	if (searchPattern instanceof OrPattern) {
		SearchPattern[] patterns = ((OrPattern) searchPattern).patterns;
		for (int i = 0, length = patterns.length; i < length; i++)
			locatePackageDeclarations(patterns[i], workspace);
	} else if (searchPattern instanceof PackageDeclarationPattern) {
		PackageDeclarationPattern pkgPattern = (PackageDeclarationPattern) searchPattern;
		IJavaProject[] projects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject javaProject = projects[i];
			IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
			for (int j = 0, rootsLength = roots.length; j < rootsLength; j++) {
				IJavaElement[] pkgs = roots[j].getChildren();
				for (int k = 0, pksLength = pkgs.length; k < pksLength; k++) {
					IPackageFragment pkg = (IPackageFragment) pkgs[k];
					if (pkg.getChildren().length > 0 
							&& pkgPattern.matchesName(pkgPattern.pkgName, pkg.getElementName().toCharArray())) {
						IResource resource = pkg.getResource();
						if (resource == null) // case of a file in an external jar
							resource = javaProject.getProject();
						this.currentPotentialMatch = new PotentialMatch(this, resource, null);
						try {
							report(-1, -2, pkg, IJavaSearchResultCollector.EXACT_MATCH);
						} catch (JavaModelException e) {
							throw e;
						} catch (CoreException e) {
							throw new JavaModelException(e);
						}
					}
				}
			}
		}
	}
}
public IType lookupType(TypeBinding typeBinding) {
	char[] packageName = typeBinding.qualifiedPackageName();
	IPackageFragment[] pkgs = this.nameLookup.findPackageFragments(
		(packageName == null || packageName.length == 0)
			? IPackageFragment.DEFAULT_PACKAGE_NAME
			: new String(packageName), 
		false);

	// iterate type lookup in each package fragment
	char[] sourceName = typeBinding.qualifiedSourceName();
	String typeName = new String(sourceName);
	for (int i = 0, length = pkgs == null ? 0 : pkgs.length; i < length; i++) {
		IType type = this.nameLookup.findType(
			typeName,
			pkgs[i], 
			false, 
			typeBinding.isClass() ? NameLookup.ACCEPT_CLASSES : NameLookup.ACCEPT_INTERFACES);
		if (type != null) return type;	
	}

	// search inside enclosing element
	char[][] qualifiedName = CharOperation.splitOn('.', sourceName);
	int length = qualifiedName.length;
	if (length == 0) return null;
	IType type = createTypeHandle(qualifiedName[0]);
	if (type == null) return null;
	for (int i = 1; i < length; i++) {
		type = createTypeHandle(type, qualifiedName[i]);
		if (type == null) return null;
	}
	if (type.exists()) return type;
	return null;
}
/*
 * Process a compilation unit already parsed and build.
 */
protected void process(PotentialMatch potentialMatch, boolean bindingsWereCreated) throws CoreException {
	this.currentPotentialMatch = potentialMatch;
	CompilationUnitDeclaration unit = potentialMatch.parsedUnit;
	MatchingNodeSet matchingNodeSet = null;
	try {
		if (unit.isEmpty()) {
			if (this.currentPotentialMatch.openable instanceof ClassFile)
				this.currentPotentialMatch.locateMatchesInClassFile();
			return;
		}
		if (hasAlreadyDefinedType(unit)) return; // skip type has it is hidden so not visible

		matchingNodeSet = this.currentPotentialMatch.matchingNodeSet;
		getMethodBodies(unit, matchingNodeSet);

		if (bindingsWereCreated && this.pattern.mustResolve && unit.types != null) {
			if (SearchEngine.VERBOSE)
				System.out.println("Resolving " + this.currentPotentialMatch.openable.toStringWithAncestors()); //$NON-NLS-1$

			matchingNodeSet.reduceParseTree(unit);

			if (unit.scope != null)
				unit.scope.faultInTypes(); // fault in fields & methods
			unit.resolve();

			matchingNodeSet.reportMatching(unit, true);
		} else {
			matchingNodeSet.reportMatching(unit, this.pattern.mustResolve);
		}
	} catch (AbortCompilation e) {
		// could not resolve: report innacurate matches
		if (matchingNodeSet != null)
			matchingNodeSet.reportMatching(unit, true); // was partially resolved
		if (!(e instanceof AbortCompilationUnit)) {
			// problem with class path
			throw e;
		}
	} finally {
		this.currentPotentialMatch = null;
	}
}
public void report(int sourceStart, int sourceEnd, IJavaElement element, int accuracy) throws CoreException {
	if (this.scope.encloses(element)) {
		if (SearchEngine.VERBOSE) {
			IResource res = this.currentPotentialMatch.resource;
			System.out.println("Reporting match"); //$NON-NLS-1$
			System.out.println("\tResource: " + (res == null ? " <unknown> " : res.getFullPath().toString())); //$NON-NLS-2$//$NON-NLS-1$
			System.out.println("\tPositions: [" + sourceStart + ", " + sourceEnd + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			System.out.println("\tJava element: " + ((JavaElement)element).toStringWithAncestors()); //$NON-NLS-1$
			System.out.println(accuracy == IJavaSearchResultCollector.EXACT_MATCH
				? "\tAccuracy: EXACT_MATCH" //$NON-NLS-1$
				: "\tAccuracy: POTENTIAL_MATCH"); //$NON-NLS-1$
		}
		report(this.currentPotentialMatch.resource, sourceStart, sourceEnd, element, accuracy);
	}
}
public void report(IResource resource, int sourceStart, int sourceEnd, IJavaElement element, int accuracy) throws CoreException {
	long start = -1;
	if (SearchEngine.VERBOSE)
		start = System.currentTimeMillis();
	this.collector.accept(resource, sourceStart, sourceEnd + 1, element, accuracy);
	if (SearchEngine.VERBOSE)
		this.resultCollectorTime += System.currentTimeMillis()-start;
}
/**
 * Finds the accurate positions of the sequence of tokens given by qualifiedName
 * in the source and reports a reference to this this qualified name
 * to the search requestor.
 */
public void reportAccurateReference(int sourceStart, int sourceEnd, char[][] qualifiedName, IJavaElement element, int accuracy) throws CoreException {
	if (accuracy == -1) return;

	// compute source positions of the qualified reference 
	Scanner scanner = this.parser.scanner;
	scanner.setSource(this.currentPotentialMatch.getContents());
	scanner.resetTo(sourceStart, sourceEnd);

	int refSourceStart = -1, refSourceEnd = -1;
	int tokenNumber = qualifiedName.length;
	int token = -1;
	int previousValid = -1;
	int i = 0;
	int currentPosition;
	do {
		// find first token that is an identifier (parenthesized expressions include parenthesises in source range - see bug 20693 - Finding references to variables does not find all occurrences  )
		do {
			currentPosition = scanner.currentPosition;
			try {
				token = scanner.getNextToken();
			} catch (InvalidInputException e) {
			}
		} while (token !=  TerminalTokens.TokenNameIdentifier && token !=  TerminalTokens.TokenNameEOF);

		if (token != TerminalTokens.TokenNameEOF) {
			char[] currentTokenSource = scanner.getCurrentTokenSource();
			boolean equals = false;
			while (i < tokenNumber && !(equals = this.pattern.matchesName(qualifiedName[i++], currentTokenSource)));
			if (equals && (previousValid == -1 || previousValid == i - 2)) {
				previousValid = i - 1;
				if (refSourceStart == -1)
					refSourceStart = currentPosition;
				refSourceEnd = scanner.currentPosition - 1;
			} else {
				i = 0;
				refSourceStart = -1;
				previousValid = -1;
			}
			// read '.'
			try {
				token = scanner.getNextToken();
			} catch (InvalidInputException e) {
			}
		}
		if (i == tokenNumber) {
			// accept reference
			if (refSourceStart != -1) {
				report(refSourceStart, refSourceEnd, element, accuracy);
			} else {
				report(sourceStart, sourceEnd, element, accuracy);
			}
			return;
		}
	} while (token != TerminalTokens.TokenNameEOF);

}
/**
 * Finds the accurate positions of each valid token in the source and
 * reports a reference to this token to the search requestor.
 * A token is valid if it has an accurracy which is not -1.
 */
public void reportAccurateReference(int sourceStart, int sourceEnd, char[][] tokens, IJavaElement element, int[] accuracies) throws CoreException {
	// compute source positions of the qualified reference 
	Scanner scanner = this.parser.scanner;
	scanner.setSource(this.currentPotentialMatch.getContents());
	scanner.resetTo(sourceStart, sourceEnd);

	int refSourceStart = -1, refSourceEnd = -1;
	int length = tokens.length;
	int token = -1;
	int previousValid = -1;
	int i = 0;
	int accuracyIndex = 0;
	do {
		int currentPosition = scanner.currentPosition;
		// read token
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
		}
		if (token != TerminalTokens.TokenNameEOF) {
			char[] currentTokenSource = scanner.getCurrentTokenSource();
			boolean equals = false;
			while (i < length && !(equals = this.pattern.matchesName(tokens[i++], currentTokenSource))) {
			}
			if (equals && (previousValid == -1 || previousValid == i - 2)) {
				previousValid = i - 1;
				if (refSourceStart == -1)
					refSourceStart = currentPosition;
				refSourceEnd = scanner.currentPosition - 1;
			} else {
				i = 0;
				refSourceStart = -1;
				previousValid = -1;
			}
			// read '.'
			try {
				token = scanner.getNextToken();
			} catch (InvalidInputException e) {
			}
		}
		if (accuracies[accuracyIndex] != -1) {
			// accept reference
			if (refSourceStart != -1) {
				report(refSourceStart, refSourceEnd, element, accuracies[accuracyIndex]);
			} else {
				report(sourceStart, sourceEnd, element, accuracies[accuracyIndex]);
			}
			i = 0;
		}
		refSourceStart = -1;
		previousValid = -1;
		if (accuracyIndex < accuracies.length - 1)
			accuracyIndex++;
	} while (token != TerminalTokens.TokenNameEOF);

}
public void reportBinaryMatch(IMember binaryMember, IBinaryType info, int accuracy) throws CoreException {
	reportBinaryMatch(null, binaryMember, info, accuracy);
}
public void reportBinaryMatch(IResource resource, IMember binaryMember, IBinaryType info, int accuracy) throws CoreException {
	ISourceRange range = binaryMember.getNameRange();
	if (range.getOffset() == -1) {
		ClassFile classFile = (ClassFile) binaryMember.getClassFile();
		SourceMapper mapper = classFile.getSourceMapper();
		if (mapper != null) {
			IType type = classFile.getType();
			String sourceFileName = mapper.findSourceFileName(type, info);
			if (sourceFileName != null) {
				char[] contents = mapper.findSource(type, sourceFileName);
				if (contents != null)
					range = mapper.mapSource(type, contents, binaryMember);
			}
		}
	}
	int startIndex = range.getOffset();
	int endIndex = startIndex + range.getLength() - 1;
	if (resource == null)
		report(startIndex, endIndex, binaryMember, accuracy);
	else
		report(resource, startIndex, endIndex, binaryMember, accuracy);
}
/**
 * Reports the given field declaration to the search requestor.
 */
public void reportFieldDeclaration(FieldDeclaration fieldDeclaration, IJavaElement parent, int accuracy) throws CoreException {
	report(
		fieldDeclaration.sourceStart,
		fieldDeclaration.sourceEnd,
		(parent instanceof IType)
			? ((IType) parent).getField(new String(fieldDeclaration.name))
			: parent,
		accuracy);
}
/**
 * Reports the given import to the search requestor.
 */
public void reportImport(ImportReference reference, int accuracy) throws CoreException {
	IJavaElement importHandle = createImportHandle(reference);
	this.pattern.matchReportImportRef(reference, null, importHandle, accuracy, this);
}
/**
 * Reports the given method declaration to the search requestor.
 */
public void reportMethodDeclaration(AbstractMethodDeclaration methodDeclaration, IJavaElement parent, int accuracy) throws CoreException {
	IJavaElement enclosingElement;
	if (parent instanceof IType) {
		// create method handle
		enclosingElement = createMethodHandle(methodDeclaration, (IType)parent);
		if (enclosingElement == null) return;
	} else {
		enclosingElement = parent;
	}

	// compute source positions of the selector 
	Scanner scanner = parser.scanner;
	int nameSourceStart = methodDeclaration.sourceStart;
	scanner.setSource(this.currentPotentialMatch.getContents());
	scanner.resetTo(nameSourceStart, methodDeclaration.sourceEnd);
	try {
		scanner.getNextToken();
	} catch (InvalidInputException e) {
	}
	int nameSourceEnd = scanner.currentPosition - 1;

	// accept method declaration
	report(nameSourceStart, nameSourceEnd, enclosingElement, accuracy);
}
/**
 * Reports the given package declaration to the search requestor.
 */
public void reportPackageDeclaration(ImportReference node) {
	// TBD
}
/**
 * Reports the given reference to the search requestor.
 * It is done in the given method and the method's defining types 
 * have the given simple names.
 */
public void reportReference(
	AstNode reference,
	AbstractMethodDeclaration methodDeclaration,
	IJavaElement parent,
	int accuracy)
	throws CoreException {

	IJavaElement enclosingElement;
	if (parent instanceof IType) {
		enclosingElement = createMethodHandle(methodDeclaration, (IType) parent);
		if (enclosingElement == null) return; // case of a match found in a type other than the current class file
	} else {
		enclosingElement = parent;
	}
	this.pattern.matchReportReference(reference, enclosingElement, accuracy, this);
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
	IJavaElement parent,
	int accuracy)
	throws CoreException {

	IJavaElement enclosingElement;
	if (parent instanceof IType) {
		enclosingElement = fieldDeclaration.isField()
			? (IJavaElement) createFieldHandle(fieldDeclaration, (IType) parent)
			: (IJavaElement) createInitializerHandle(typeDeclaration, fieldDeclaration, (IType) parent);
		if (enclosingElement == null) return;
	} else {
		enclosingElement = parent;
	}
	this.pattern.matchReportReference(reference, enclosingElement, accuracy, this);
}
/**
 * Reports the given super type reference to the search requestor.
 * It is done in the given defining type (with the given simple names).
 */
public void reportSuperTypeReference(TypeReference typeRef, IJavaElement type, int accuracy) throws CoreException {
	this.pattern.matchReportReference(typeRef, type, accuracy, this);
}
/**
 * Reports the given type declaration to the search requestor.
 */
public void reportTypeDeclaration(TypeDeclaration typeDeclaration, IJavaElement parent, int accuracy) throws CoreException {
	report(
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd,
		(parent == null)
			? createTypeHandle(typeDeclaration.name)
			: ((parent instanceof IType)
				? createTypeHandle((IType) parent, typeDeclaration.name)
				: parent),
		accuracy);
}
public boolean typeInHierarchy(ReferenceBinding binding) {
	if (this.hierarchyResolver == null) return true; // not a hierarchy scope
	if (this.hierarchyResolver.subOrSuperOfFocus(binding)) return true;

	if (this.allSuperTypeNames != null) {
		char[][] compoundName = binding.compoundName;
		for (int i = 0, length = this.allSuperTypeNames.length; i < length; i++)
			if (CharOperation.equals(compoundName, this.allSuperTypeNames[i]))
				return true;
	}
	return false;
}
}
