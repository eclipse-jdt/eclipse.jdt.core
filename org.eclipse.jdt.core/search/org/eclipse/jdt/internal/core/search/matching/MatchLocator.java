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
	public LookupEnvironment lookupEnvironment;
	public HashtableOfObject parsedUnits;
	private MatchingOpenableSet matchingOpenables;
	private MatchingOpenable currentMatchingOpenable;
	public HandleFactory handleFactory;

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
	final IFile file = (IFile)compilationUnit.getUnderlyingResource();
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
	final char[] source = Util.getResourceContentsAsCharArray(file);
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
					if (sameParameters) return methodHandle;
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

	/**
	 * Locate the matches in the given files and report them using the search requestor. 
	 */
	public void locateMatches(String[] filePaths, IWorkspace workspace)
		throws JavaModelException {
		Util.sort(filePaths); // sort by projects
		if (this.handleFactory == null) {
			this.handleFactory = new HandleFactory(workspace);
		}
		JavaProject previousJavaProject = null;
		int length = filePaths.length;
		double increment = 100.0 / length;
		double totalWork = 0;
		int lastProgress = 0;
		this.matchingOpenables = new MatchingOpenableSet();
		for (int i = 0; i < length; i++) {
			IProgressMonitor monitor = this.collector.getProgressMonitor();
			if (monitor != null && monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			String pathString = filePaths[i];
			Openable openable = this.handleFactory.createOpenable(pathString);
			if (openable == null)
				continue; // match is outside classpath

			// create new parser and lookup environment if this is a new project
			IResource resource = null;
			JavaProject javaProject = null;
			try {
				javaProject = (JavaProject) openable.getJavaProject();
				resource = openable.getUnderlyingResource();
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
				String pkgName = new String(pkgPattern.pkgName);
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
	public void report(
		int sourceStart,
		int sourceEnd,
		IJavaElement element,
		int accuracy)
		throws CoreException {
		if (this.scope.encloses(element)) {
			this.collector.accept(
				this.getCurrentResource(),
				sourceStart,
				sourceEnd + 1,
				element,
				accuracy);
		}
	}

	public void reportBinaryMatch(
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
		this.report(startIndex, endIndex, binaryMember, accuracy);
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
	private void createParser(JavaProject project) throws JavaModelException {
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
		this.pattern.initializePolymorphicSearch(this, project, this.collector.getProgressMonitor());
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
					throw (JavaModelException) e;
				} else {
					throw new JavaModelException(e);
				}
			}
		}
		this.currentMatchingOpenable = null;
	}
}