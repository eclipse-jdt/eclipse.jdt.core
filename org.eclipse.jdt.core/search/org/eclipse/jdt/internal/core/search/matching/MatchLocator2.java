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
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
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
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.HierarchyScope;

// TODO: (jerome) remove extends
public class MatchLocator2 extends MatchLocator implements ITypeRequestor {
	public static final int MAX_AT_ONCE = 500;
	public static final PotentialMatch[] NO_POTENTIAL_MATH = new PotentialMatch[0];
	
/*	// permanent state
	public SearchPattern pattern;
	public int detailLevel;
	public IJavaSearchResultCollector collector;
	public IJavaSearchScope scope;
	public IProgressMonitor progressMonitor;

	public IWorkingCopy[] workingCopies;
	public HandleFactory handleFactory;

	// the following is valid for the current project
	public MatchLocatorParser parser;
	public INameEnvironment nameEnvironment;
	public NameLookup nameLookup;
	public LookupEnvironment lookupEnvironment;
	public HierarchyResolver hierarchyResolver;
	boolean compilationAborted;
*/
	public PotentialMatchSet potentialMatches;
	
	public int parseThreshold = -1;
	public CompilerOptions options;
	
	// management of unit to be processed
	public CompilationUnitDeclaration[] unitsToProcess;
	public PotentialMatch[] matchesToProcess;
	public int totalUnits; // (totalUnits-1) gives the last unit in unitToProcess
	
	public PotentialMatch currentPotentialMatch;
	
	/*
	 * Time spent in the IJavaSearchResultCollector
	 */
	public long resultCollectorTime = 0;

	public MatchLocator2(
		SearchPattern pattern,
		int detailLevel,
		IJavaSearchResultCollector collector,
		IJavaSearchScope scope,
		IProgressMonitor progressMonitor) {
			
		super(pattern, detailLevel, collector, scope, progressMonitor);
		
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
		CompilationResult unitResult =
			new CompilationResult(sourceUnit, totalUnits, totalUnits, this.options.maxProblemsPerUnit);
		try {
			// diet parsing for large collection of unit
			CompilationUnitDeclaration parsedUnit;
			MatchSet originalMatchSet = this.parser.matchSet;
			try {
				this.parser.matchSet = new MatchingNodeSet(this);
				if (totalUnits < parseThreshold) {
					parsedUnit = parser.parse(sourceUnit, unitResult);
				} else {
					parsedUnit = parser.dietParse(sourceUnit, unitResult);
				}
			} finally {
				this.parser.matchSet = originalMatchSet;
			}
		
			// initial type binding creation
			lookupEnvironment.buildTypeBindings(parsedUnit);
			this.addCompilationUnit(sourceUnit, parsedUnit);
	
			// binding resolution
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
			ICompilationUnit sourceUnit = (ICompilationUnit)type.getCompilationUnit();
			this.accept(sourceUnit);
		} else {
			CompilationResult result =
				new CompilationResult(sourceType.getFileName(), 0, 0, 0);
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
	protected void addCompilationUnit(
		ICompilationUnit sourceUnit,
		CompilationUnitDeclaration parsedUnit) {

		// append the unit to the list of ones to process later on
		int size = this.unitsToProcess.length;
		if (this.totalUnits == size) {
			// when growing reposition units starting at position 0
			int newSize = size == 0 ? 1 : size * 2;
			System.arraycopy(
				this.unitsToProcess,
				0,
				(this.unitsToProcess = new CompilationUnitDeclaration[newSize]),
				0,
				this.totalUnits);
			System.arraycopy(
				this.matchesToProcess,
				0,
				(this.matchesToProcess = new PotentialMatch[newSize]),
				0,
				this.totalUnits);
		}
		if (sourceUnit instanceof PotentialMatch) {
			this.matchesToProcess[this.totalUnits] = (PotentialMatch)sourceUnit;
		}
		this.unitsToProcess[this.totalUnits] = parsedUnit;
		this.totalUnits++;
	}	
	void addPotentialMatch(IResource resource, Openable openable) {
		PotentialMatch potentialMatch = new PotentialMatch(this, resource, openable);
		this.potentialMatches.add(potentialMatch);
	}
	/*
	 * Caches the given binary type in the lookup environment and returns it.
	 * Returns the existing one if already cached.
	 * Returns null if source type binding was cached.
	 */
	BinaryTypeBinding cacheBinaryType(IType type) throws JavaModelException {
		IType enclosingType = type.getDeclaringType();
		if (enclosingType != null) {
			// force caching of enclosing types first, so that binary type can be found in lookup enviroment
			this.cacheBinaryType(enclosingType);
		}
		IBinaryType binaryType = (IBinaryType)((BinaryType)type).getElementInfo();
		BinaryTypeBinding binding = this.lookupEnvironment.cacheBinaryType(binaryType);
		if (binding == null) { // it was already cached as a result of a previous query
			char[][] compoundName = CharOperation.splitOn('.', type.getFullyQualifiedName().toCharArray());
			ReferenceBinding referenceBinding = this.lookupEnvironment.getCachedType(compoundName);
			if (referenceBinding != null && (referenceBinding instanceof BinaryTypeBinding)) {
				// if the binding could be found and if it comes from a binary type,
				binding = (BinaryTypeBinding)referenceBinding;
			}
		}
		return binding;
	}
	public  ClassFileReader classFileReader(IType type) {
		IClassFile classFile = type.getClassFile(); 
		if (((IOpenable)classFile).isOpen()) {
			JavaModelManager manager = JavaModelManager.getJavaModelManager();
			synchronized(manager){
				return (ClassFileReader)manager.getInfo(type);
			}
		} else {
			IPackageFragment pkg = type.getPackageFragment();
			IPackageFragmentRoot root = (IPackageFragmentRoot)pkg.getParent();
			try {
				if (root.isArchive()) {
					IPath zipPath = root.isExternal() ? root.getPath() : root.getResource().getLocation();
					if (zipPath == null) return null; // location is null
					ZipFile zipFile = null;
					try {
						if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
							System.out.println("(" + Thread.currentThread() + ") [MatchLocator.classFileReader()] Creating ZipFile on " + zipPath); //$NON-NLS-1$	//$NON-NLS-2$
						}
						// TODO: (jerome) should use JavaModelManager.getZipFile(...) instead
						zipFile = new ZipFile(zipPath.toOSString());
						char[] pkgPath = pkg.getElementName().toCharArray();
						CharOperation.replace(pkgPath, '.', '/');
						char[] classFileName = classFile.getElementName().toCharArray();
						char[] path = pkgPath.length == 0 ? classFileName : CharOperation.concat(pkgPath, classFileName, '/');
						return ClassFileReader.read(zipFile, new String(path));
					} finally {
						if (zipFile != null) {
							try {
								zipFile.close();
							} catch (IOException e) {
							}
						}
					}
				} else {
					return ClassFileReader.read(type.getPath().toOSString());
				}
			} catch (ClassFormatException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
		}
	}
	/**
	 * Add the initial set of compilation units into the loop
	 *  ->  build compilation unit declarations, their bindings and record their results.
	 */
	protected void createAndResolveBindings(PotentialMatch[] potentialMatches, int start, int length) {

		for (int i = start, maxUnits = start+length; i < maxUnits; i++) {
			if (this.progressMonitor != null && this.progressMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			PotentialMatch potentialMatch = potentialMatches[i];
			try {
				if (potentialMatch != null) {
					this.parser.matchSet = potentialMatch.matchingNodeSet;
				}
				CompilationResult unitResult =
					new CompilationResult(potentialMatch, i, maxUnits, this.options.maxProblemsPerUnit);
					
				if (SearchEngine.VERBOSE) {
					System.out.println("Parsing " + potentialMatch.openable.toStringWithAncestors()); //$NON-NLS-1$
				}

				// diet parsing for large collection of units
				CompilationUnitDeclaration parsedUnit;
				if (totalUnits < parseThreshold) {
					parsedUnit = this.parser.parse(potentialMatch, unitResult);
				} else {
					parsedUnit = this.parser.dietParse(potentialMatch, unitResult);
				}
								
				// initial type binding creation
				if (parsedUnit != null && !parsedUnit.isEmpty()) {
					this.lookupEnvironment.buildTypeBindings(parsedUnit);
				}
				
				this.addCompilationUnit(potentialMatch, parsedUnit);
				
				// progress reporting
				if (this.progressMonitor != null) {
					this.progressMonitor.worked(4);
				}
			} finally {
				this.parser.matchSet = null;
				potentialMatches[i] = null; // no longer hold onto the unit
			}
		}
		// binding resolution
		lookupEnvironment.completeTypeBindings();
	}
	/**
	 * Creates an IField from the given field declaration and type. 
	 */
	public IField createFieldHandle(
		FieldDeclaration field,
		IType type) {
		if (type == null) return null;
		return type.getField(new String(field.name));
	}
	/*
	 * Creates hierarchy resolver if needed. 
	 * Returns whether focus is visible.
	 */
	protected boolean createHierarchyResolver(PotentialMatch[] potentialMatches) {
		// create hierarchy resolver if scope is a hierarchy scope
		IType focusType = getFocusType();
		if (focusType != null) {
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
					// cache binary type
					try {
						this.cacheBinaryType(focusType);
					} catch (JavaModelException e) {
						return false;
					}
				} else {
					// cache all types in the focus' compilation unit (even secondary types)
					this.accept((ICompilationUnit)focusType.getCompilationUnit());
				}
			}
			
			// resolve focus type
			this.hierarchyResolver = new HierarchyResolver(this.lookupEnvironment, null/*hierarchy is not going to be computed*/);
			if (this.hierarchyResolver.setFocusType(compoundName) == null) {
				// focus type is not visible from this project
				return false;
			}
		} else {
			this.hierarchyResolver = null;
		}
		return true;
	}
	/**
	 * Creates an IImportDeclaration from the given import statement
	 */
	public IJavaElement createImportHandle(ImportReference importRef) {
		char[] importName = CharOperation.concatWith(importRef.getImportName(), '.');
		if (importRef.onDemand) {
			importName = CharOperation.concat(importName, ".*" .toCharArray()); //$NON-NLS-1$
		}
		Openable currentOpenable = this.getCurrentOpenable();
		if (currentOpenable instanceof CompilationUnit) {
			return ((CompilationUnit)currentOpenable).getImport(
				new String(importName));
		} else {
			try {
				return ((org.eclipse.jdt.internal.core.ClassFile)currentOpenable).getType();
			} catch (JavaModelException e) {
				return null;
			}
		}
	}
	/**
	 * Creates an IInitializer from the given field declaration and type. 
	 */
	public IInitializer createInitializerHandle(
		TypeDeclaration typeDecl,
		FieldDeclaration initializer,
		IType type) {
		if (type == null) return null;

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
	 * Creates an IMethod from the given method declaration and type. 
	 */
	public IMethod createMethodHandle(
		AbstractMethodDeclaration method,
		IType type) {
		if (type == null) return null;
		Argument[] arguments = method.arguments;
		int length = arguments == null ? 0 : arguments.length;
		if (type.isBinary()) {
			// don't cache the methods of the binary type
			ClassFileReader reader = this.classFileReader(type);
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
							for (int k = 0; k < parameterType.dimensions(); k++) {
								typeName = CharOperation.concat(typeName, "[]" .toCharArray()); //$NON-NLS-1$
							}
							String parameterTypeName = parameterTypes[j].replace('/', '.');
							if (!Signature.toString(parameterTypeName).endsWith(new String(typeName))) {
								sameParameters = false;
								break;
							} else {
								parameterTypes[j] = parameterTypeName;
							}
						}
						if (sameParameters) {
							return type.getMethod(new String(selector), parameterTypes);
						}
					}
				}
			}
			return null;
		} else {
			String[] parameterTypeSignatures = new String[length];
			for (int i = 0; i < length; i++) {
				TypeReference parameterType = arguments[i].type;
				char[] typeName = CharOperation.concatWith(parameterType.getTypeName(), '.');
				for (int j = 0; j < parameterType.dimensions(); j++) {
					typeName = CharOperation.concat(typeName, "[]" .toCharArray()); //$NON-NLS-1$
				}
				parameterTypeSignatures[i] = Signature.createTypeSignature(typeName, false);
			}
			return type.getMethod(new String(method.selector), parameterTypeSignatures);
		}
	}
	/**
	 * Creates an IType from the given simple top level type name. 
	 */
	public IType createTypeHandle(char[] simpleTypeName) {
		Openable currentOpenable = this.getCurrentOpenable();
		if (currentOpenable instanceof CompilationUnit) {
			// creates compilation unit
			CompilationUnit unit = (CompilationUnit)currentOpenable;
	
			// create type
			return unit.getType(new String(simpleTypeName));
		} else {
			IType type; 
			try {
				type = ((org.eclipse.jdt.internal.core.ClassFile)currentOpenable).getType();
			} catch (JavaModelException e) {
				return null;
			}
			// ensure this is a top level type (see bug 20011  Searching for Inner Classes gives bad search results)
			return MatchingOpenable.getTopLevelType(type);
		}
	}
	/**
	 * Creates an IType from the given simple inner type name and parent type. 
	 */
	public IType createTypeHandle(IType parent, char[] simpleTypeName) {
		return parent.getType(new String(simpleTypeName));
	}
	protected Openable getCurrentOpenable() {
		return this.currentPotentialMatch.openable;
	}
	protected IResource getCurrentResource() {
		return this.currentPotentialMatch.resource;
	}
	protected IType getFocusType() {
		return this.scope instanceof HierarchyScope ? ((HierarchyScope)this.scope).focusType : null;
	}
	public IBinaryType getBinaryInfo(org.eclipse.jdt.internal.core.ClassFile classFile, IResource resource) throws CoreException {
		BinaryType binaryType = (BinaryType)classFile.getType();
		if (classFile.isOpen()) {
			// reuse the info from the java model cache
			return (IBinaryType)binaryType.getElementInfo();
		} else {
			// create a temporary info
			IBinaryType info;
			try {
				IJavaElement pkg = classFile.getParent();
				PackageFragmentRoot root = (PackageFragmentRoot)pkg.getParent();
				if (root.isArchive()) {
					// class file in a jar
					String pkgPath = pkg.getElementName().replace('.', '/');
					String classFilePath = 
						(pkgPath.length() > 0) ?
							pkgPath + "/" + classFile.getElementName() : //$NON-NLS-1$
							classFile.getElementName();
					ZipFile zipFile = null;
					try {
						zipFile = ((JarPackageFragmentRoot)root).getJar();
						info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(
							zipFile,
							classFilePath);
					} finally {
						JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
					}
				} else {
					// class file in a directory
					String osPath = resource.getFullPath().toOSString();
					info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(osPath);
				}
				return info;
			} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
				//e.printStackTrace();
				return null;
			} catch (java.io.IOException e) {
				throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
			}
		}
	}
	protected void getMethodBodies(CompilationUnitDeclaration unit, int place) {
		//fill the methods bodies in order for the code to be generated

		if (unit.ignoreMethodBodies) {
			unit.ignoreFurtherInvestigation = true;
			return;
			// if initial diet parse did not work, no need to dig into method bodies.
		}

		if (place < parseThreshold)
			return; //work already done ...

		//real parse of the method....
		this.parser.scanner.setSource(unit.compilationResult.compilationUnit.getContents());
		this.parser.parseBodies(unit);
	}
	/**
	 * Create a new parser for the given project, as well as a lookup environment.
	 */
	public void initialize(JavaProject project) throws JavaModelException {
		initialize(project, NO_POTENTIAL_MATH);
	}
	/**
	 * Create a new parser for the given project, as well as a lookup environment.
	 */
	public void initialize(JavaProject project, PotentialMatch[] potentialMatches) throws JavaModelException {
		// create name environment
		if (this.nameEnvironment != null) { // cleanup
			this.nameEnvironment.cleanup();
		}
		if (potentialMatches.length == 1) {
			// if only one potential match, a file name environment costs too much,
			// so use the existing searchable  environment which will populate the java model
			// only for this potential match and its required types.
			this.nameEnvironment = project.getSearchableNameEnvironment();
		} else {
			this.nameEnvironment = new JavaSearchNameEnvironment2(project);
		}

		// create lookup environment
		this.options = new CompilerOptions(project.getOptions(true));
		ProblemReporter problemReporter =
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				this.options,
				new DefaultProblemFactory());
		this.lookupEnvironment =
			new LookupEnvironment(this, this.options, problemReporter, this.nameEnvironment);
			
		// create parser
		this.parser = new MatchLocatorParser(problemReporter, this.options.sourceLevel);
				
		// remember project's name lookup
		this.nameLookup = project.getNameLookup();
		
		// initialize queue of units
		this.totalUnits = 0;
		int maxUnits = potentialMatches.length;
		this.unitsToProcess = new CompilationUnitDeclaration[maxUnits];
		this.matchesToProcess = new PotentialMatch[maxUnits];

	}
	public boolean hasAlreadyDefinedType(CompilationUnitDeclaration parsedUnit) {
		if (parsedUnit == null) return false;
		CompilationResult result = parsedUnit.compilationResult;
		if (result == null) return false;
		for (int i = 0; i < result.problemCount; i++) {
			IProblem problem = result.problems[i];
			if (problem.getID() == IProblem.DuplicateTypes) {
				return true;
			}
		}
		return false;
	}	
	/**
	 * Locate the matches amongst the potential matches.
	 */
	private void locateMatches(JavaProject javaProject) throws JavaModelException {
		PotentialMatch[] potentialMatches = 
			this.potentialMatches.getPotentialMatches(
				getFocusType() == null ?
				javaProject.getPackageFragmentRoots() :
				javaProject.getAllPackageFragmentRoots()); // all potential matches are resolved in the focus' project context
		
		int length = potentialMatches.length;
		int index = 0;
		while (index < length) {
			int max = Math.min(MAX_AT_ONCE, length-index);
			locateMatches(javaProject, potentialMatches, index, max);
			index += max;
		}
	}
	private void locateMatches(JavaProject javaProject, PotentialMatch[] potentialMatches, int start, int length) throws JavaModelException {
		
		// copy array because elements  from the original are removed below
		PotentialMatch[] copy = new PotentialMatch[length];
		System.arraycopy(potentialMatches, start, copy, 0, length);
		this.initialize(javaProject, copy);
		
		this.compilationAborted = false;
		
		// create and resolve binding (equivalent to beginCompilation() in Compiler)
		try {
			this.createAndResolveBindings(potentialMatches, start, length);
		} catch (AbortCompilation e) {
			this.compilationAborted = true;
		}
		
		// create hierarchy resolver if needed
		try {
			if (!this.compilationAborted && !this.createHierarchyResolver(copy)) {
				return;
			}
		} catch (AbortCompilation e) {
			this.compilationAborted = true;
		}
		
		// free memory
		copy = null;
		potentialMatches = null;
		
		// potential match resolution
		try {
			CompilationUnitDeclaration unit = null;
			for (int i = 0; i < this.totalUnits; i++) {
				if (this.progressMonitor != null && this.progressMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				unit = this.unitsToProcess[i];
				try {
					process(unit, i);
				} catch (AbortCompilation e) {
					// problem with class path: it could not find base classes
					// continue and try next matching openable reporting innacurate matches (since bindings will be null)
					this.compilationAborted = true;
				} catch (CoreException e) {
					if (e instanceof JavaModelException) {
						// problem with class path: it could not find base classes
						// continue and try next matching openable reporting innacurate matches (since bindings will be null)
						this.compilationAborted = true;
					} else {
						// core exception thrown by client's code: let it through
						throw new JavaModelException(e);
					}
				} finally {
					// cleanup compilation unit result
					unit.cleanUp();
					if (this.options.verbose)
						System.out.println(Util.bind("compilation.done", //$NON-NLS-1$
					new String[] {
						String.valueOf(i + 1),
						String.valueOf(totalUnits),
						new String(unitsToProcess[i].getFileName())}));
				}
				this.unitsToProcess[i] = null; // release reference to processed unit declaration
				this.matchesToProcess[i] = null; // release reference to processed potential match
				if (this.progressMonitor != null) {
					this.progressMonitor.worked(5);
				}
			}
		} catch (AbortCompilation e) {
			this.compilationAborted = true;
		}		
	}
	
	/**
	 * Locate the matches in the given files and report them using the search requestor. 
	 */
	public void locateMatches(
		String[] filePaths, 
		IWorkspace workspace,
		IWorkingCopy[] workingCopies)
		throws JavaModelException {
			
		if (SearchEngine.VERBOSE) {
			System.out.println("Locating matches in files ["); //$NON-NLS-1$
			for (int i = 0, length = filePaths.length; i < length; i++) {
				String path = filePaths[i];
				System.out.println("\t" + path); //$NON-NLS-1$
			}
			System.out.println("]"); //$NON-NLS-1$
			if (workingCopies != null) {
				 System.out.println("and working copies ["); //$NON-NLS-1$
				for (int i = 0, length = workingCopies.length; i < length; i++) {
					IWorkingCopy wc = workingCopies[i];
					System.out.println("\t" + ((JavaElement)wc).toStringWithAncestors()); //$NON-NLS-1$
				}
				System.out.println("]"); //$NON-NLS-1$
			}
		}
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			// optimize access to zip files during search operation
			manager.cacheZipFiles();
				
			// initialize handle factory (used as a cache of handles so as to optimize space)
			if (this.handleFactory == null) {
				this.handleFactory = new HandleFactory(workspace);
			}
			
			// initialize locator with working copies
			this.workingCopies = workingCopies;
			
			// substitute compilation units with working copies
			HashMap wcPaths = new HashMap(); // a map from path to working copies
			int wcLength;
			if (workingCopies != null && (wcLength = workingCopies.length) > 0) {
				String[] newPaths = new String[wcLength];
				for (int i = 0; i < wcLength; i++) {
					IWorkingCopy workingCopy = workingCopies[i];
					String path = workingCopy.getOriginalElement().getPath().toString();
					wcPaths.put(path, workingCopy);
					newPaths[i] = path;
				}
				int filePathsLength = filePaths.length;
				System.arraycopy(filePaths, 0, filePaths = new String[filePathsLength+wcLength], 0, filePathsLength);
				System.arraycopy(newPaths, 0, filePaths, filePathsLength, wcLength);
			}
			
			int length = filePaths.length;
			if (progressMonitor != null) {
				if (this.pattern.needsResolve) {
					progressMonitor.beginTask("", length * 10); // 1 for file path, 4 for parsing and binding creation, 5 for binding resolution //$NON-NLS-1$
				} else {
					progressMonitor.beginTask("", length * 5); // 1 for file path, 4 for parsing and binding creation //$NON-NLS-1$
				}
			}
	
			// sort file paths projects
			Util.sort(filePaths); 
			
			// initialize pattern for polymorphic search (ie. method reference pattern)
			this.potentialMatches = new PotentialMatchSet();
			this.pattern.initializePolymorphicSearch(this, progressMonitor);
			
			IType focusType = getFocusType();
			JavaProject previousJavaProject = focusType == null ? null : (JavaProject)focusType.getJavaProject();
			for (int i = 0; i < length; i++) {
				if (progressMonitor != null && progressMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				String pathString = filePaths[i];
				
				// skip duplicate paths
				if (i > 0 && pathString.equals(filePaths[i-1])) continue;
				
				Openable openable;
				IWorkingCopy workingCopy = (IWorkingCopy)wcPaths.get(pathString);
				if (workingCopy != null) {
					openable = (Openable)workingCopy;
				} else {
					openable = this.handleFactory.createOpenable(pathString, this.scope);
					if (openable == null)
						continue; // match is outside classpath
				}
	
				// create new parser and lookup environment if this is a new project
				IResource resource = null;
				JavaProject javaProject = null;
				try {
					javaProject = (JavaProject) openable.getJavaProject();
					if (workingCopy != null) {
						resource = workingCopy.getOriginalElement().getResource();
					} else {
						resource = openable.getResource();
					}
					if (resource == null) { // case of a file in an external jar
						resource = javaProject.getProject();
					}
					if (focusType == null // when searching in hierarchy, all potential matches are resolved in the focus project context
							&& !javaProject.equals(previousJavaProject)) {
						// locate matches in previous project
						if (previousJavaProject != null) {
							try {
								this.locateMatches(previousJavaProject);
							} catch (JavaModelException e) {
								if (e.getException() instanceof CoreException) {
									throw e;
								} else {
									// problem with classpath in this project -> skip it
								}
							}
							this.potentialMatches = new PotentialMatchSet();
						}
	
						previousJavaProject = javaProject;
					}
				} catch (JavaModelException e) {
					// file doesn't exist -> skip it
					continue;
				}
	
				// add potential match
				this.addPotentialMatch(resource, openable);
	
				if (progressMonitor != null) {
					progressMonitor.worked(1);
				}
			}
			
			// last project
			if (previousJavaProject != null) {
				try {
					this.locateMatches(previousJavaProject);
				} catch (JavaModelException e) {
					if (e.getException() instanceof CoreException) {
						throw e;
					} else {
						// problem with classpath in last project -> skip it
					}
				}
				this.potentialMatches = new PotentialMatchSet();
			} 
			
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		} finally {
			if (this.nameEnvironment != null) {
				this.nameEnvironment.cleanup();
			}
			manager.flushZipFiles();
		}	
	}
	/**
	 * Locates the package declarations corresponding to this locator's pattern. 
	 */
	public void locatePackageDeclarations(IWorkspace workspace)
		throws JavaModelException {
		this.locatePackageDeclarations(this.pattern, workspace);
	}

	/**
	 * Locates the package declarations corresponding to the search pattern. 
	 */
	private void locatePackageDeclarations(
		SearchPattern searchPattern,
		IWorkspace workspace)
		throws JavaModelException {
		if (searchPattern instanceof OrPattern) {
			OrPattern orPattern = (OrPattern) searchPattern;
			this.locatePackageDeclarations(orPattern.leftPattern, workspace);
			this.locatePackageDeclarations(orPattern.rightPattern, workspace);
		} else
			if (searchPattern instanceof PackageDeclarationPattern) {
				PackageDeclarationPattern pkgPattern =
					(PackageDeclarationPattern) searchPattern;
				IJavaProject[] projects =
					JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
				for (int i = 0, length = projects.length; i < length; i++) {
					IJavaProject javaProject = projects[i];
					IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
					for (int j = 0, rootsLength = roots.length; j < rootsLength; j++) {
						IJavaElement[] pkgs = roots[j].getChildren();
						for (int k = 0, pksLength = pkgs.length; k < pksLength; k++) {
							IPackageFragment pkg = (IPackageFragment)pkgs[k];
							if (pkg.getChildren().length > 0 
									&& pkgPattern.matchesName(pkgPattern.pkgName, pkg.getElementName().toCharArray())) {
								IResource resource = pkg.getResource();
								if (resource == null) { // case of a file in an external jar
									resource = javaProject.getProject();
								}
								this.currentPotentialMatch = new PotentialMatch(this, resource, null);
								try {
									this.report(-1, -2, pkg, IJavaSearchResultCollector.EXACT_MATCH);
								} catch (CoreException e) {
									if (e instanceof JavaModelException) {
										throw (JavaModelException) e;
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
	/*
	 * Process a compilation unit already parsed and build.
	 */
	public void process(CompilationUnitDeclaration unit, int i) throws CoreException {
		MatchingNodeSet matchingNodeSet = null;
		try {
			this.currentPotentialMatch = this.matchesToProcess[i];
			if (this.currentPotentialMatch == null) return;
			matchingNodeSet = this.currentPotentialMatch.matchingNodeSet;
			
			if (unit == null || unit.isEmpty()) {
				if (this.currentPotentialMatch.openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
					this.currentPotentialMatch.locateMatchesInClassFile();
				}
				return;
			}
			if (hasAlreadyDefinedType(unit)) {
				// skip type has it is hidden so not visible
				return;
			}
	
			this.parser.matchSet = this.currentPotentialMatch.matchingNodeSet;
			getMethodBodies(unit, i);
						
			// report matches that don't need resolve
			matchingNodeSet.cuHasBeenResolved = this.compilationAborted;
			matchingNodeSet.reportMatching(unit);

			if ((this.pattern.needsResolve || matchingNodeSet.needsResolve()/* TODO: do not need this check any longer */) 
					&& unit.types != null 
					&& !this.compilationAborted) {

				if (SearchEngine.VERBOSE) {
					System.out.println("Resolving " + this.currentPotentialMatch.openable.toStringWithAncestors()); //$NON-NLS-1$
				}

				// fault in fields & methods
				if (unit.scope != null)
					unit.scope.faultInTypes();
		
				// verify inherited methods
				if (unit.scope != null)
					unit.scope.verifyMethods(this.lookupEnvironment.methodVerifier());
		
				// type checking
				unit.resolve();
		
				// refresh the total number of units known at this stage
				unit.compilationResult.totalUnitsKnown = totalUnits;

				// report matches that needed resolve
				matchingNodeSet.cuHasBeenResolved = true;
				matchingNodeSet.reportMatching(unit);
			}
		} catch (AbortCompilation e) {
			// could not resolve: report innacurate matches
			if (matchingNodeSet != null) {
				matchingNodeSet.cuHasBeenResolved = true;
				matchingNodeSet.reportMatching(unit);
			}
			if (!(e instanceof AbortCompilationUnit)) {
				// problem with class path
				throw e;
			}
		} finally {
			this.parser.matchSet = null;
			this.currentPotentialMatch = null;
		}
	}
	public void report(
		int sourceStart,
		int sourceEnd,
		IJavaElement element,
		int accuracy)
		throws CoreException {

		if (this.scope.encloses(element)) {
			if (SearchEngine.VERBOSE) {
				IResource res = this.getCurrentResource();
				System.out.println("Reporting match"); //$NON-NLS-1$
				System.out.println("\tResource: " + (res == null ? " <unknown> " : res.getFullPath().toString())); //$NON-NLS-2$//$NON-NLS-1$
				System.out.println("\tPositions: [" + sourceStart + ", " + sourceEnd + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				System.out.println("\tJava element: " + ((JavaElement)element).toStringWithAncestors()); //$NON-NLS-1$
				if (accuracy == IJavaSearchResultCollector.EXACT_MATCH) {
					System.out.println("\tAccuracy: EXACT_MATCH"); //$NON-NLS-1$
				} else {
					System.out.println("\tAccuracy: POTENTIAL_MATCH"); //$NON-NLS-1$
				}
			}
			this.report(
				this.getCurrentResource(),
				sourceStart,
				sourceEnd,
				element,
				accuracy);
		}
	}
	public void report(
		IResource resource,
		int sourceStart,
		int sourceEnd,
		IJavaElement element,
		int accuracy)
		throws CoreException {

		long start = -1;
		if (SearchEngine.VERBOSE) {
			start = System.currentTimeMillis();
		}
		this.collector.accept(
			resource,
			sourceStart,
			sourceEnd + 1,
			element,
			accuracy);
		if (SearchEngine.VERBOSE) {
			this.resultCollectorTime += System.currentTimeMillis()-start;
		}
	}
	/**
	 * Finds the accurate positions of the sequence of tokens given by qualifiedName
	 * in the source and reports a reference to this this qualified name
	 * to the search requestor.
	 */
	public void reportAccurateReference(
		int sourceStart,
		int sourceEnd,
		char[][] qualifiedName,
		IJavaElement element,
		int accuracy)
		throws CoreException {
	
		if (accuracy == -1) return;
	
		// compute source positions of the qualified reference 
		Scanner scanner = this.parser.scanner;
		scanner.setSource(
			this.currentPotentialMatch.getContents());
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
				while (i < tokenNumber
					&& !(equals = this.pattern.matchesName(qualifiedName[i++], currentTokenSource))) {
				}
				if (equals && (previousValid == -1 || previousValid == i - 2)) {
					previousValid = i - 1;
					if (refSourceStart == -1) {
						refSourceStart = currentPosition;
					}
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
					this.report(refSourceStart, refSourceEnd, element, accuracy);
				} else {
					this.report(sourceStart, sourceEnd, element, accuracy);
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
	public void reportAccurateReference(
		int sourceStart,
		int sourceEnd,
		char[][] tokens,
		IJavaElement element,
		int[] accuracies)
		throws CoreException {

		// compute source positions of the qualified reference 
		Scanner scanner = this.parser.scanner;
		scanner.setSource(
			this.currentPotentialMatch.getContents());
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
				while (i < length
					&& !(equals = this.pattern.matchesName(tokens[i++], currentTokenSource))) {
				}
				if (equals && (previousValid == -1 || previousValid == i - 2)) {
					previousValid = i - 1;
					if (refSourceStart == -1) {
						refSourceStart = currentPosition;
					}
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
					this.report(refSourceStart, refSourceEnd, element, accuracies[accuracyIndex]);
				} else {
					this.report(sourceStart, sourceEnd, element, accuracies[accuracyIndex]);
				}
				i = 0;
			}
			refSourceStart = -1;
			previousValid = -1;
			if (accuracyIndex < accuracies.length-1) {
				accuracyIndex++;
			}
		} while (token != TerminalTokens.TokenNameEOF);

	}
	public void reportBinaryMatch(
		IMember binaryMember,
		IBinaryType info,
		int accuracy)
		throws CoreException, JavaModelException {
			
		this.reportBinaryMatch(null, binaryMember, info, accuracy);
	}
	public void reportBinaryMatch(
		IResource resource,
		IMember binaryMember,
		IBinaryType info,
		int accuracy)
		throws CoreException, JavaModelException {
		ISourceRange range = binaryMember.getNameRange();
		if (range.getOffset() == -1) {
			ClassFile classFile = (ClassFile) binaryMember.getClassFile();
			SourceMapper mapper = classFile.getSourceMapper();
			if (mapper != null) {
				IType type = classFile.getType();
				String sourceFileName = mapper.findSourceFileName(type, info);
				if (sourceFileName != null) {
					char[] contents = mapper.findSource(type, sourceFileName);
					if (contents != null) {
						range = mapper.mapSource(type, contents, binaryMember);
					}
				}
			}
		}
		int startIndex = range.getOffset();
		int endIndex = startIndex + range.getLength() - 1;
		if (resource == null) {
			this.report(startIndex, endIndex, binaryMember, accuracy);
		} else {
			this.report(resource, startIndex, endIndex, binaryMember, accuracy);
		}
	}
	/**
	 * Reports the given field declaration to the search requestor.
	 */
	public void reportFieldDeclaration(
		FieldDeclaration fieldDeclaration,
		IJavaElement parent,
		int accuracy)
		throws CoreException {

		// accept field declaration
		this.report(
			fieldDeclaration.sourceStart,
			fieldDeclaration.sourceEnd,
			(parent instanceof IType) ?
				((IType)parent).getField(new String(fieldDeclaration.name)) :
				parent,
			accuracy);
	}
	/**
	 * Reports the given import to the search requestor.
	 */
	public void reportImport(ImportReference reference, int accuracy)
		throws CoreException {

		// create defining import handle
		IJavaElement importHandle = this.createImportHandle(reference);

		// accept reference
		this.pattern.matchReportImportRef(reference, null, importHandle, accuracy, this);
	}
	/**
	 * Reports the given method declaration to the search requestor.
	 */
	public void reportMethodDeclaration(
		AbstractMethodDeclaration methodDeclaration,
		IJavaElement parent,
		int accuracy)
		throws CoreException {

		IJavaElement enclosingElement;
		if (parent instanceof IType) {
			// create method handle
			enclosingElement = this.createMethodHandle(methodDeclaration, (IType)parent);
			if (enclosingElement == null) return;
		} else {
			enclosingElement = parent;
		}

		// compute source positions of the selector 
		Scanner scanner = parser.scanner;
		int nameSourceStart = methodDeclaration.sourceStart;
		scanner.setSource(
			this.currentPotentialMatch.getContents());
		scanner.resetTo(nameSourceStart, methodDeclaration.sourceEnd);
		try {
			scanner.getNextToken();
		} catch (InvalidInputException e) {
		}
		int nameSourceEnd = scanner.currentPosition - 1;

		// accept method declaration
		this.report(nameSourceStart, nameSourceEnd, enclosingElement, accuracy);
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
			// create defining method handle
			enclosingElement = this.createMethodHandle(methodDeclaration, (IType)parent);
			if (enclosingElement == null) return; // case of a match found in a type other than the current class file
		} else {
			enclosingElement = parent;
		}

		// accept reference
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
		if (fieldDeclaration.isField()) {
			if (parent instanceof IType) {
				// create defining field handle
				enclosingElement = this.createFieldHandle(fieldDeclaration, (IType)parent);
				if (enclosingElement == null) return;
			} else {
				enclosingElement = parent;
			}

			// accept reference
			this.pattern.matchReportReference(reference, enclosingElement, accuracy, this);
		} else { // initializer
			if (parent instanceof IType) {
				// create defining initializer
				enclosingElement =
					this.createInitializerHandle(
						typeDeclaration,
						fieldDeclaration,
						(IType)parent);
				if (enclosingElement == null) return;
			} else {
				enclosingElement = parent;
			}

			// accept reference
			this.pattern.matchReportReference(reference, enclosingElement, accuracy, this);
		}
	}
	/**
	 * Reports the given super type reference to the search requestor.
	 * It is done in the given defining type (with the given simple names).
	 */
	public void reportSuperTypeReference(
		TypeReference typeRef,
		IJavaElement type,
		int accuracy)
		throws CoreException {

		// accept type reference
		this.pattern.matchReportReference(typeRef, type, accuracy, this);
	}
	/**
	 * Reports the given type declaration to the search requestor.
	 */
	public void reportTypeDeclaration(
		TypeDeclaration typeDeclaration,
		IJavaElement parent,
		int accuracy)
		throws CoreException {

		// accept class or interface declaration
		this.report(
			typeDeclaration.sourceStart,
			typeDeclaration.sourceEnd,
			(parent == null) ?
				this.createTypeHandle(typeDeclaration.name) :
				(parent instanceof IType) ?
					this.createTypeHandle((IType)parent, typeDeclaration.name) :
					parent,
			accuracy);
	}
}
