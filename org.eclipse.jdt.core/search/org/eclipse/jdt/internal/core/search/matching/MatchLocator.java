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
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
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
import org.eclipse.jdt.internal.core.WorkingCopy;

import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

/**
 * Locate matches in compilation units.
 */
public class MatchLocator implements ITypeRequestor {
	public SearchPattern pattern;
	public int detailLevel;
	public IJavaSearchResultCollector collector;
	public IJavaSearchScope scope;

	public MatchLocatorParser parser;
	public NameLookup nameLookup;
	public LookupEnvironment lookupEnvironment;
	public HashtableOfObject parsedUnits;
	private MatchingOpenableSet matchingOpenables;
	private MatchingOpenable currentMatchingOpenable;
	public HandleFactory handleFactory;
	public IWorkingCopy[] workingCopies;

	private static char[] EMPTY_FILE_NAME = new char[0];

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
		BinaryTypeBinding binaryBinding =  new BinaryTypeBinding(packageBinding, binaryType, this.lookupEnvironment);
		ReferenceBinding cachedType = this.lookupEnvironment.getCachedType(binaryBinding.compoundName);
		if (cachedType == null || cachedType instanceof UnresolvedReferenceBinding) { // NB: cachedType is not null if already cached as a source type
			this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
		}
	}

	/**
	 * Add an additional compilation unit.
	 */
	public void accept(ICompilationUnit sourceUnit) {
		CompilationResult result = new CompilationResult(sourceUnit, 1, 1);
		CompilationUnitDeclaration parsedUnit =
			this.parser.dietParse(sourceUnit, result);

		this.lookupEnvironment.buildTypeBindings(parsedUnit);
		this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
	}

	/**
	 * Add an additional source type
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding) {
		ISourceType sourceType = sourceTypes[0];
		while (sourceType.getEnclosingType() != null)
			sourceType = sourceType.getEnclosingType();
		if (sourceType instanceof SourceTypeElementInfo) {
			// get source
			SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) sourceType;
			IType type = elementInfo.getHandle();
			try {
				this.buildBindings(type.getCompilationUnit());
			} catch (JavaModelException e) {
				// nothing we can do here: ignore
			}
		} else {
			CompilationResult result =
				new CompilationResult(sourceType.getFileName(), 0, 0);
			CompilationUnitDeclaration unit =
				SourceTypeConverter.buildCompilationUnit(
					sourceTypes,
					true,
					true,
					lookupEnvironment.problemReporter,
					result);
			this.lookupEnvironment.buildTypeBindings(unit);
			this.lookupEnvironment.completeTypeBindings(unit, true);
			this.parsedUnits.put(sourceType.getQualifiedName(), unit);
		}
	}

/*
 * Parse the given compiation unit and build its type bindings.
 * Remember the parsed unit.
 */
public CompilationUnitDeclaration buildBindings(org.eclipse.jdt.core.ICompilationUnit compilationUnit) throws JavaModelException {
	final IFile file = 
		compilationUnit.isWorkingCopy() ?
			(IFile)compilationUnit.getOriginalElement().getUnderlyingResource() :
			(IFile)compilationUnit.getUnderlyingResource();
	CompilationUnitDeclaration unit = null;
	
	// get main type name
	final String fileName = file.getFullPath().lastSegment();
	final char[] mainTypeName =
		fileName.substring(0, fileName.length() - 5).toCharArray();
	
	// find out if unit is already known
	char[] qualifiedName = compilationUnit.getType(new String(mainTypeName)).getFullyQualifiedName().toCharArray();
	unit = (CompilationUnitDeclaration)this.parsedUnits.get(qualifiedName);
	if (unit != null) return unit;

	// source unit
	final char[] source = 
		compilationUnit.isWorkingCopy() ?
			compilationUnit.getBuffer().getCharacters() :
			Util.getResourceContentsAsCharArray(file);
	ICompilationUnit sourceUnit = new ICompilationUnit() {
		public char[] getContents() {
			return source;
		}
		public char[] getFileName() {
			return fileName.toCharArray();
		}
		public char[] getMainTypeName() {
			return mainTypeName;
		}
		public char[][] getPackageName() {
			return null;
		}
	};
	
	// diet parse
	MatchSet originalMatchSet = this.parser.matchSet;
	try {
		this.parser.matchSet = new MatchSet(this);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0);
		unit = this.parser.dietParse(sourceUnit, compilationResult);
	} finally {
		if (originalMatchSet == null) {
			if (!this.parser.matchSet.isEmpty() 
					&& unit != null) {
				// potential matches were found while initializing the search pattern
				// from the lookup environment: add the corresponding openable in the list
				MatchingOpenable matchingOpenable = 
					new MatchingOpenable(
						this,
						file, 
						(CompilationUnit)compilationUnit, 
						unit,
						this.parser.matchSet);
				this.matchingOpenables.add(matchingOpenable);
			}
			this.parser.matchSet = null;
		} else {
			this.parser.matchSet = originalMatchSet;
		}
	}
	if (unit != null) {
		this.lookupEnvironment.buildTypeBindings(unit);
		this.lookupEnvironment.completeTypeBindings(unit, true);
		this.parsedUnits.put(qualifiedName, unit);
	}
	return unit;
}

	/**
	 * Creates an IField from the given field declaration and type. 
	 */
	public IField createFieldHandle(
		FieldDeclaration field,
		IType type) {
		if (type == null) return null;
		if (type.isBinary()) {
			IField fieldHandle = type.getField(new String(field.name));
			if (fieldHandle.exists()) {
				return fieldHandle;
			} else {
				return null;
			}
		} else {
			return type.getField(new String(field.name));
		}
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
			String selector = new String(method.selector);
			IMethod[] methods;
			try {
				methods = type.getMethods();
			} catch (JavaModelException e) {
				return null;
			}
			for (int i = 0; i < methods.length; i++) {
				IMethod methodHandle = methods[i];
				if (methodHandle.getElementName().equals(selector) && length == methodHandle.getNumberOfParameters()) {
					boolean sameParameters = true;
					String[] parameterTypes = methodHandle.getParameterTypes();
					for (int j = 0; j < length; j++) {
						TypeReference parameterType = arguments[j].type;
						char[] typeName = CharOperation.concatWith(parameterType.getTypeName(), '.');
						for (int k = 0; k < parameterType.dimensions(); k++) {
							typeName = CharOperation.concat(typeName, "[]" .toCharArray()); //$NON-NLS-1$
						}
						String parameterTypeName = parameterTypes[j];
						if (!Signature.toString(parameterTypeName).endsWith(new String(typeName))) {
							sameParameters = false;
							break;
						}
					}
					if (sameParameters) {
						IJavaProject project = type.getJavaProject();
						// check if the method's project is the same as the type's project
						// they could be different in the case of a jar shared by several projects
						// (the handles are equals and thus the java model cache contains only one of them)
						// see bug 7945 Search results not selected in external jar  
						if (!project.equals(methodHandle.getJavaProject())) {
							return type.getMethod(selector, parameterTypes);
						} else {
							return methodHandle;
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
			try {
				return ((org.eclipse.jdt.internal.core.ClassFile)currentOpenable).getType();
			} catch (JavaModelException e) {
				return null;
			}
		}
	}
	/**
	 * Creates an IType from the given simple inner type name and parent type. 
	 */
	public IType createTypeHandle(IType parent, char[] simpleTypeName) {
		return parent.getType(new String(simpleTypeName));
	}
	protected IResource getCurrentResource() {
		return this.currentMatchingOpenable.resource;
	}

	protected Scanner getScanner() {
		return this.parser == null ? null : this.parser.scanner;
	}
/*
 * Creates a new set of matching openables and initializes it with the given
 * working copies.
 */
private void initializeMatchingOpenables(IWorkingCopy[] workingCopies) {
	this.matchingOpenables = new MatchingOpenableSet();
	if (workingCopies != null) {
		for (int i = 0, length = workingCopies.length; i < length; i++) {
			IWorkingCopy workingCopy = workingCopies[i];
			try {
				IResource res = workingCopy.getOriginalElement().getUnderlyingResource();
				this.addMatchingOpenable(res, (Openable)workingCopy);
			} catch (JavaModelException e) {
				// continue with next working copy
			}
		}
	}
}

	/**
	 * Locate the matches in the given files and report them using the search requestor. 
	 */
	public void locateMatches(String[] filePaths, IWorkspace workspace, IWorkingCopy[] workingCopies)
		throws JavaModelException {
			
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
				try {
					IResource res = workingCopy.getOriginalElement().getUnderlyingResource();
					String path = res.getFullPath().toString();
					wcPaths.put(path, workingCopy);
					newPaths[i] = path;
				} catch (JavaModelException e) {
					// continue with next working copy
				}
			}
			int filePathsLength = filePaths.length;
			System.arraycopy(filePaths, 0, filePaths = new String[filePathsLength+wcLength], 0, filePathsLength);
			System.arraycopy(newPaths, 0, filePaths, filePathsLength, wcLength);
		}
		
		// sort file paths projects
		Util.sort(filePaths); 
		
		// initialize pattern for polymorphic search (ie. method reference pattern)
		this.matchingOpenables = new MatchingOpenableSet();
		this.pattern.initializePolymorphicSearch(this, this.collector.getProgressMonitor());
		
		JavaProject previousJavaProject = null;
		int length = filePaths.length;
		double increment = 100.0 / length;
		double totalWork = 0;
		int lastProgress = 0;
		for (int i = 0; i < length; i++) {
			IProgressMonitor monitor = this.collector.getProgressMonitor();
			if (monitor != null && monitor.isCanceled()) {
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
				openable = this.handleFactory.createOpenable(pathString);
				if (openable == null)
					continue; // match is outside classpath
			}

			// create new parser and lookup environment if this is a new project
			IResource resource = null;
			JavaProject javaProject = null;
			try {
				javaProject = (JavaProject) openable.getJavaProject();
				if (workingCopy != null) {
					resource = workingCopy.getOriginalElement().getUnderlyingResource();
				} else {
					resource = openable.getUnderlyingResource();
				}
				if (resource == null) { // case of a file in an external jar
					resource = javaProject.getProject();
				}
				if (!javaProject.equals(previousJavaProject)) {
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
						this.matchingOpenables = new MatchingOpenableSet();
					}

					// create parser for this project
					this.createParser(javaProject);
					previousJavaProject = javaProject;
				}
			} catch (JavaModelException e) {
				// file doesn't exist -> skip it
				continue;
			}

			// add matching openable
			this.addMatchingOpenable(resource, openable);

			if (monitor != null) {
				totalWork = totalWork + increment;
				int worked = (int) totalWork - lastProgress;
				monitor.worked(worked);
				lastProgress = (int) totalWork;
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
			this.matchingOpenables = new MatchingOpenableSet();
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
					JavaModelManager.getJavaModel(workspace).getJavaProjects();
				for (int i = 0, length = projects.length; i < length; i++) {
					IJavaProject javaProject = projects[i];
					IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
					for (int j = 0, rootsLength = roots.length; j < rootsLength; j++) {
						IJavaElement[] pkgs = roots[j].getChildren();
						for (int k = 0, pksLength = pkgs.length; k < pksLength; k++) {
							IPackageFragment pkg = (IPackageFragment)pkgs[k];
							if (pkg.getChildren().length > 0 
									&& pkgPattern.matchesName(pkgPattern.pkgName, pkg.getElementName().toCharArray())) {
								IResource resource = pkg.getUnderlyingResource();
								if (resource == null) { // case of a file in an external jar
									resource = javaProject.getProject();
								}
								this.currentMatchingOpenable = new MatchingOpenable(this, resource, null);
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
public IType lookupType(TypeBinding typeBinding) {
	char[] packageName = typeBinding.qualifiedPackageName();
	char[] typeName = typeBinding.qualifiedSourceName();
	
	// find package fragments
	IPackageFragment[] pkgs = 
		this.nameLookup.findPackageFragments(
			(packageName == null || packageName.length == 0) ? 
				IPackageFragment.DEFAULT_PACKAGE_NAME : 
				new String(packageName), 
			false);
			
	// iterate type lookup in each package fragment
	for (int i = 0, length = pkgs == null ? 0 : pkgs.length; i < length; i++) {
		IType type = 
			this.nameLookup.findType(
				new String(typeName), 
				pkgs[i], 
				false, 
				typeBinding.isClass() ?
					NameLookup.ACCEPT_CLASSES:
					NameLookup.ACCEPT_INTERFACES);
		if (type != null) return type;	
	}
	
	// search inside enclosing element
	char[][] qualifiedName = CharOperation.splitOn('.', typeName);
	int length = qualifiedName.length;
	if (length == 0) return null;
	IType type = this.createTypeHandle(qualifiedName[0]);
	if (type == null) return null;
	for (int i = 1; i < length; i++) {
		type = this.createTypeHandle(type, qualifiedName[i]);
		if (type == null) return null;
	}
	if (type.exists()) return type;	
	
	return null;
}
	public void report(
		int sourceStart,
		int sourceEnd,
		IJavaElement element,
		int accuracy)
		throws CoreException {

		if (this.scope.encloses(element)) {
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

		this.collector.accept(
			resource,
			sourceStart,
			sourceEnd + 1,
			element,
			accuracy);
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
				char[] contents = mapper.findSource(type, info);
				if (contents != null) {
					range = mapper.mapSource(type, contents, binaryMember);
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
		this.pattern.matchReportReference(reference, importHandle, accuracy, this);
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
		scanner.setSourceBuffer(
			this.currentMatchingOpenable.getSource());
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
	 * Reports the given package reference to the search requestor.
	 */
	public void reportPackageReference(ImportReference node) {
		// TBD
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
		Scanner scanner = parser.scanner;
		scanner.setSourceBuffer(
			this.currentMatchingOpenable.getSource());
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
			} catch (InvalidInputException e) {
			}
			if (token != TerminalSymbols.TokenNameEOF) {
				char[] currentTokenSource = scanner.getCurrentTokenSource();
				boolean equals = false;
				while (i < tokenNumber
					&& !(equals = CharOperation.equals(qualifiedName[i++], currentTokenSource))) {
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
		} while (token != TerminalSymbols.TokenNameEOF);

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
		Scanner scanner = parser.scanner;
		scanner.setSourceBuffer(
			this.currentMatchingOpenable.getSource());
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
			if (token != TerminalSymbols.TokenNameEOF) {
				char[] currentTokenSource = scanner.getCurrentTokenSource();
				boolean equals = false;
				while (i < length
					&& !(equals = CharOperation.equals(tokens[i++], currentTokenSource))) {
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
		} while (token != TerminalSymbols.TokenNameEOF);

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

private MatchingOpenable newMatchingOpenable(IResource resource, Openable openable) {
	MatchingOpenable matchingOpenable;
	try {
		matchingOpenable = new MatchingOpenable(this, resource, openable);
	} catch (AbortCompilation e) {
		// problem with class path: ignore this matching openable
		return null;
	}
	return matchingOpenable;
}

private void addMatchingOpenable(IResource resource, Openable openable)
		throws JavaModelException {
		
	MatchingOpenable matchingOpenable = this.newMatchingOpenable(resource, openable);
	if (matchingOpenable != null) {
		this.matchingOpenables.add(matchingOpenable);
	}
}


	/**
	 * Create a new parser for the given project, as well as a lookup environment.
	 * Asks the pattern to initialize itself for polymorphic search.
	 */
	public void createParser(JavaProject project) throws JavaModelException {
		INameEnvironment nameEnvironment = project.getSearchableNameEnvironment();
		IProblemFactory problemFactory = new DefaultProblemFactory();

		CompilerOptions options = new CompilerOptions(JavaCore.getOptions());
		ProblemReporter problemReporter =
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options,
				problemFactory);
		this.lookupEnvironment =
			new LookupEnvironment(this, options, problemReporter, nameEnvironment);
		this.parser = new MatchLocatorParser(problemReporter, options.assertMode);
		this.parsedUnits = new HashtableOfObject(10);
		this.nameLookup = project.getNameLookup();
	}

	public CompilationUnitDeclaration dietParse(final char[] source) {
		// source unit
		ICompilationUnit sourceUnit = new ICompilationUnit() {
			public char[] getContents() {
				return source;
			}
			public char[] getFileName() {
				return EMPTY_FILE_NAME; // not used
			}
			public char[] getMainTypeName() {
				return null; // don't need to check if main type name == compilation unit name
			}
			public char[][] getPackageName() {
				return null;
			}
		};
		
		// diet parse
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0);  
		return this.parser.dietParse(sourceUnit, compilationResult);
	}
	
	public char[] findSource(ClassFile classFile) {
		char[] source = null; 
		try {
			SourceMapper sourceMapper = classFile.getSourceMapper();
			if (sourceMapper != null) {
				source = sourceMapper.findSource(classFile.getType());
			}
			if (source == null) {
				// default to opening the class file
				String sourceFromBuffer = classFile.getSource();
				if (sourceFromBuffer != null) {
					source = sourceFromBuffer.toCharArray();
				}
			}
		} catch (JavaModelException e) {
		}
		return source;
	}
public IBinaryType getBinaryInfo(org.eclipse.jdt.internal.core.ClassFile classFile, IResource resource) throws CoreException {
	BinaryType binaryType = (BinaryType)classFile.getType();
	if (classFile.isOpen()) {
		// reuse the info from the java model cache
		return (IBinaryType)binaryType.getRawInfo();
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
	protected Openable getCurrentOpenable() {
		return this.currentMatchingOpenable.openable;
	}

	/**
	 * Locate the matches amongst the matching openables.
	 */
	private void locateMatches(JavaProject javaProject) throws JavaModelException {
		MatchingOpenable[] openables = this.matchingOpenables.getMatchingOpenables(javaProject.getPackageFragmentRoots());
	
		// binding creation
		for (int i = 0, length = openables.length; i < length; i++) { 
			openables[i].buildTypeBindings();
		}

		// binding resolution
		boolean shouldResolve = true;
		try {
			this.lookupEnvironment.completeTypeBindings();
		} catch (AbortCompilation e) {
			// problem with class path: it could not find base classes
			// continue reporting innacurate matches (since bindings will be null)
			shouldResolve = false;
		}

		// matching openable resolution
		for (int i = 0, length = openables.length; i < length; i++) { 
			IProgressMonitor monitor = this.collector.getProgressMonitor();
			if (monitor != null && monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			try {
				this.currentMatchingOpenable = openables[i];
				
				// skip type has it is hidden so not visible
				if (this.currentMatchingOpenable.hasAlreadyDefinedType()) {
					continue;
				}
				
				this.currentMatchingOpenable.shouldResolve = shouldResolve;
				this.currentMatchingOpenable.locateMatches();
				this.currentMatchingOpenable.reset();
			} catch (AbortCompilation e) {
				// problem with class path: it could not find base classes
				// continue and try next matching openable
			} catch (CoreException e) {
				if (e instanceof JavaModelException) {
					// problem with class path: it could not find base classes
					// continue and try next matching openable
				} else {
					// core exception thrown by client's code: let it through
					throw new JavaModelException(e);
				}
			}
		}
		this.currentMatchingOpenable = null;
	}
}