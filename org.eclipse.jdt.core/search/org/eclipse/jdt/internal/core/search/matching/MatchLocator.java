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
	private PotentialMatch[] potentialMatches;
	private int potentialMatchesIndex;
	private int potentialMatchesLength;

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
		CompilationUnitDeclaration unit;
		if (sourceType instanceof SourceTypeElementInfo) {
			// get source
			SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) sourceType;
			IType type = elementInfo.getHandle();
			try {
				final IFile file = (IFile) type.getUnderlyingResource();
				final char[] source = PotentialMatch.getContents(file);

				// get main type name
				final String fileName = file.getFullPath().lastSegment();
				final char[] mainTypeName =
					fileName.substring(0, fileName.length() - 5).toCharArray();

				// source unit
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
				CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0);
				unit = this.parser.dietParse(sourceUnit, compilationResult);
			} catch (JavaModelException e) {
				unit = null;
			}
		} else {
			CompilationResult result =
				new CompilationResult(sourceType.getFileName(), 0, 0);
			unit =
				SourceTypeConverter.buildCompilationUnit(
					sourceTypes,
					true,
					true,
					lookupEnvironment.problemReporter,
					result);
		}

		if (unit != null) {
			this.lookupEnvironment.buildTypeBindings(unit);
			this.lookupEnvironment.completeTypeBindings(unit, true);
			this.parsedUnits.put(sourceType.getQualifiedName(), unit);
		}
	}

	/**
	 * Creates an IField from the given field declaration and simple type names. 
	 */
	private IField createFieldHandle(
		FieldDeclaration field,
		char[][] definingTypeNames) {
		IType type = this.createTypeHandle(definingTypeNames);
		return type.getField(new String(field.name));
	}

	/**
	 * Creates an IImportDeclaration from the given import statement
	 */
	private IImportDeclaration createImportHandle(ImportReference importRef) {
		char[] importName = CharOperation.concatWith(importRef.getImportName(), '.');
		if (importRef.onDemand) {
			importName = CharOperation.concat(importName, ".*" .toCharArray()); //$NON-NLS-1$
		}
		return ((CompilationUnit) this.getCurrentOpenable()).getImport(
			new String(importName));
	}

	/**
	 * Creates an IInitializer from the given field declaration and simple type names. 
	 */
	private IInitializer createInitializerHandle(
		TypeDeclaration typeDecl,
		FieldDeclaration initializer,
		char[][] definingTypeNames) {
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
	private IMethod createMethodHandle(
		AbstractMethodDeclaration method,
		char[][] definingTypeNames) {
		IType type = this.createTypeHandle(definingTypeNames);
		Argument[] arguments = method.arguments;
		int length = arguments == null ? 0 : arguments.length;
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

	/**
	 * Creates an IType from the given simple type names. 
	 */
	private IType createTypeHandle(char[][] simpleTypeNames) {
		// creates compilation unit
		CompilationUnit unit = (CompilationUnit) this.getCurrentOpenable();

		// create type
		int length = simpleTypeNames.length;
		IType type = unit.getType(new String(simpleTypeNames[0]));
		for (int i = 1; i < length; i++) {
			type = type.getType(new String(simpleTypeNames[i]));
		}
		return type;
	}
	protected IResource getCurrentResource() {
		return this.potentialMatches[this.potentialMatchesIndex].resource;
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
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		HandleFactory factory = new HandleFactory(workspace.getRoot(), manager);
		JavaProject previousJavaProject = null;
		int length = filePaths.length;
		double increment = 100.0 / length;
		double totalWork = 0;
		int lastProgress = 0;
		boolean couldInitializePattern = false;
		this.potentialMatches = new PotentialMatch[10];
		this.potentialMatchesLength = 0;
		for (int i = 0; i < length; i++) {
			IProgressMonitor monitor = this.collector.getProgressMonitor();
			if (monitor != null && monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			String pathString = filePaths[i];
			Openable openable = factory.createOpenable(pathString);
			if (openable == null)
				continue; // match is outside classpath

			// create new parser and lookup environment if this is a new project
			IResource resource = null;
			try {
				JavaProject javaProject = (JavaProject) openable.getJavaProject();
				resource = openable.getUnderlyingResource();
				if (resource == null) { // case of a file in an external jar
					resource = javaProject.getProject();
				}
				if (!javaProject.equals(previousJavaProject)) {
					// locate matches in previous project
					if (previousJavaProject != null) {
						this.locateMatches();
						this.potentialMatchesLength = 0;
					}

					// create parser for this project
					couldInitializePattern = this.createParser(javaProject);
					previousJavaProject = javaProject;
				}
				if (!couldInitializePattern)
					continue;
				// the pattern could not be initialized: the match cannot be in this project
			} catch (JavaModelException e) {
				// file doesn't exist -> skip it
				continue;
			}

			// add potential match
			this.addPotentialMatch(resource, openable);

			if (monitor != null) {
				totalWork = totalWork + increment;
				int worked = (int) totalWork - lastProgress;
				monitor.worked(worked);
				lastProgress = (int) totalWork;
			}
		}

		// last project
		if (previousJavaProject != null) {
			this.locateMatches();
			this.potentialMatchesLength = 0;
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
							IJavaElement pkg = pkgs[k];
							if (pkgPattern
								.matchesName(pkgPattern.pkgName, pkg.getElementName().toCharArray())) {
								IResource resource = pkg.getUnderlyingResource();
								if (resource == null) { // case of a file in an external jar
									resource = javaProject.getProject();
								}
								this.potentialMatchesIndex = 0;
								this.potentialMatches =
									new PotentialMatch[] { new PotentialMatch(this, resource, null)};
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
	 * Its defining types have the given simple names.
	 */
	public void reportFieldDeclaration(
		FieldDeclaration fieldDeclaration,
		char[][] definingTypeNames,
		int accuracy)
		throws CoreException {

		// create field handle
		IType type = this.createTypeHandle(definingTypeNames);
		IField field = type.getField(new String(fieldDeclaration.name));

		// accept field declaration
		this.report(
			fieldDeclaration.sourceStart,
			fieldDeclaration.sourceEnd,
			field,
			accuracy);
	}

	/**
	 * Reports the given import to the search requestor.
	 */
	public void reportImport(ImportReference reference, int accuracy)
		throws CoreException {

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
		int accuracy)
		throws CoreException {

		// create method handle
		IMethod method = this.createMethodHandle(methodDeclaration, definingTypeNames);

		// compute source positions of the selector 
		Scanner scanner = parser.scanner;
		int nameSourceStart = methodDeclaration.sourceStart;
		scanner.setSourceBuffer(
			this.potentialMatches[this.potentialMatchesIndex].getSource());
		scanner.resetTo(nameSourceStart, methodDeclaration.sourceEnd);
		try {
			scanner.getNextToken();
		} catch (InvalidInputException e) {
		}
		int nameSourceEnd = scanner.currentPosition - 1;

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
		int accuracy)
		throws CoreException {

		// compute source positions of the qualified reference 
		Scanner scanner = parser.scanner;
		scanner.setSourceBuffer(
			this.potentialMatches[this.potentialMatchesIndex].getSource());
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
				while (i < tokenNumber
					&& !CharOperation.equals(currentTokenSource, qualifiedName[i++])) {
				}
				if (CharOperation.equals(currentTokenSource, qualifiedName[i - 1])
					&& (previousValid == -1 || previousValid == i - 2)) {
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
		}
		while (token != TerminalSymbols.TokenNameEOF && i < tokenNumber);

		// accept reference
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
		int accuracy)
		throws CoreException {

		// create defining method handle
		IMethod method = this.createMethodHandle(methodDeclaration, definingTypeNames);

		// accept reference
		if (reference instanceof QualifiedNameReference
			|| reference instanceof QualifiedTypeReference) {
			this.pattern.matchReportReference((AstNode) reference, method, accuracy, this);
		} else
			if (reference instanceof MessageSend) {
				// message ref are starting at the selector start
				this.report(
					(int) (((MessageSend) reference).nameSourcePosition >> 32),
					reference.sourceEnd,
					method,
					accuracy);
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
		int accuracy)
		throws CoreException {

		if (fieldDeclaration.isField()) {
			// create defining field handle
			IField field = this.createFieldHandle(fieldDeclaration, definingTypeNames);

			// accept reference
			if (reference instanceof QualifiedNameReference
				|| reference instanceof QualifiedTypeReference) {
				this.pattern.matchReportReference((AstNode) reference, field, accuracy, this);
			} else
				if (reference instanceof MessageSend) {
					// message ref are starting at the selector start
					this.report(
						(int) (((MessageSend) reference).nameSourcePosition >> 32),
						reference.sourceEnd,
						field,
						accuracy);
				} else {
					this.report(reference.sourceStart, reference.sourceEnd, field, accuracy);
				}
		} else { // initializer
			// create defining initializer
			IInitializer initializer =
				this.createInitializerHandle(
					typeDeclaration,
					fieldDeclaration,
					definingTypeNames);

			// accept reference
			if (reference instanceof QualifiedNameReference
				|| reference instanceof QualifiedTypeReference) {
				this.pattern.matchReportReference(
					(AstNode) reference,
					initializer,
					accuracy,
					this);
			} else
				if (reference instanceof MessageSend) {
					// message ref are starting at the selector start
					this.report(
						(int) (((MessageSend) reference).nameSourcePosition >> 32),
						reference.sourceEnd,
						initializer,
						accuracy);
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
		int accuracy)
		throws CoreException {

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
		int accuracy)
		throws CoreException {

		// create type handle
		IType type = this.createTypeHandle(simpleTypeNames);

		// accept class or interface declaration
		this.report(
			typeDeclaration.sourceStart,
			typeDeclaration.sourceEnd,
			type,
			accuracy);
	}

	private void addPotentialMatch(IResource resource, Openable openable)
		throws JavaModelException {
		try {
			if (this.potentialMatchesLength == this.potentialMatches.length) {
				System.arraycopy(
					this.potentialMatches,
					0,
					this.potentialMatches = new PotentialMatch[this.potentialMatchesLength * 2],
					0,
					this.potentialMatchesLength);
			}
			this.potentialMatches[this.potentialMatchesLength++] =
				new PotentialMatch(this, resource, openable);
		} catch (AbortCompilation e) {
			// problem with class path: it could not find base classes
			throw new JavaModelException(
				e,
				IJavaModelStatusConstants.BUILDER_INITIALIZATION_ERROR);
		}
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
		this.lookupEnvironment =
			new LookupEnvironment(this, options, problemReporter, nameEnvironment);
		this.parser = new MatchLocatorParser(problemReporter);
		this.parsedUnits = new HashtableOfObject(10);
		return this.pattern.initializeFromLookupEnvironment(this.lookupEnvironment);
	}

	protected Openable getCurrentOpenable() {
		return this.potentialMatches[this.potentialMatchesIndex].openable;
	}

	/**
	 * Locate the matches amongst the potential matches.
	 */
	private void locateMatches() throws JavaModelException {
		// binding resolution
		this.lookupEnvironment.completeTypeBindings();

		// potential match resolution
		for (this.potentialMatchesIndex = 0;
			this.potentialMatchesIndex < this.potentialMatchesLength;
			this.potentialMatchesIndex++) {
			try {
				PotentialMatch potentialMatch =
					this.potentialMatches[this.potentialMatchesIndex];
				potentialMatch.locateMatches();
				potentialMatch.reset();
			} catch (AbortCompilation e) {
				// problem with class path: it could not find base classes
				throw new JavaModelException(
					e,
					IJavaModelStatusConstants.BUILDER_INITIALIZATION_ERROR);
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