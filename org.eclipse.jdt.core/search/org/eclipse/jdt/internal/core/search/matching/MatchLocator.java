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
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
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
import org.eclipse.jdt.internal.compiler.util.HashtableOfIntValues;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.*;
import org.eclipse.jdt.internal.core.search.HierarchyScope;
import org.eclipse.jdt.internal.core.search.IndexSelector;
import org.eclipse.jdt.internal.core.search.JavaSearchDocument;
import org.eclipse.jdt.internal.core.util.HandleFactory;
import org.eclipse.jdt.internal.core.util.SimpleSet;
import org.eclipse.jdt.internal.core.util.Util;

public class MatchLocator implements ITypeRequestor {

public static final int MAX_AT_ONCE = 500;

// permanent state
public SearchPattern pattern;
public PatternLocator patternLocator;
public int matchContainer;
public SearchRequestor requestor;
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

// management of PossibleMatch to be processed
public int numberOfMatches; // (numberOfMatches - 1) is the last unit in matchesToProcess
public PossibleMatch[] matchesToProcess;
public PossibleMatch currentPossibleMatch;

/*
 * Time spent in the IJavaSearchResultCollector
 */
public long resultCollectorTime = 0;

/**
 * An ast visitor that visits local type declarations.
 */
public class LocalDeclarationVisitor extends ASTVisitor {
	IJavaElement enclosingElement;
	MatchingNodeSet nodeSet;
	HashtableOfIntValues occurrencesCounts = new HashtableOfIntValues(); // key = class name (char[]), value = occurrenceCount (int)
	public LocalDeclarationVisitor(IJavaElement enclosingElement, MatchingNodeSet nodeSet) {
		this.enclosingElement = enclosingElement;
		this.nodeSet = nodeSet;
	}
	public boolean visit(TypeDeclaration typeDeclaration, BlockScope unused) {
		try {
			char[] simpleName;
			if ((typeDeclaration.bits & ASTNode.IsAnonymousTypeMASK) != 0) {				
				simpleName = CharOperation.NO_CHAR;
			} else {
				simpleName = typeDeclaration.name;
			}
			int occurrenceCount = occurrencesCounts.get(simpleName);
			if (occurrenceCount == HashtableOfIntValues.NO_VALUE) {
				occurrenceCount = 1;
			} else {
				occurrenceCount = occurrenceCount + 1;
			}
			occurrencesCounts.put(simpleName, occurrenceCount);
			if ((typeDeclaration.bits & ASTNode.IsAnonymousTypeMASK) != 0) {				
				reportMatching(typeDeclaration, enclosingElement, -1, nodeSet, occurrenceCount);
			} else {
				Integer level = (Integer) nodeSet.matchingNodes.removeKey(typeDeclaration);
				reportMatching(typeDeclaration, enclosingElement, level != null ? level.intValue() : -1, nodeSet, occurrenceCount);
			}
			return false; // don't visit members as this was done during reportMatching(...)
		} catch (CoreException e) {
			throw new WrappedCoreException(e);
		}
	}
}

public static class WorkingCopyDocument extends JavaSearchDocument {
	public org.eclipse.jdt.core.ICompilationUnit workingCopy;
	WorkingCopyDocument(org.eclipse.jdt.core.ICompilationUnit workingCopy, SearchParticipant participant) {
		super(workingCopy.getPath().toString(), participant);
		this.charContents = ((CompilationUnit)workingCopy).getContents();
		this.workingCopy = workingCopy;
	}
	public String toString() {
		return "WorkingCopyDocument for " + getPath(); //$NON-NLS-1$
	}
}
	
public class WrappedCoreException extends RuntimeException {
	public CoreException coreException;
	public WrappedCoreException(CoreException coreException) {
		this.coreException = coreException;
	}
}

public static SearchDocument[] addWorkingCopies(InternalSearchPattern pattern, SearchDocument[] indexMatches, org.eclipse.jdt.core.ICompilationUnit[] copies, SearchParticipant participant) {
	// working copies take precedence over corresponding compilation units
	HashMap workingCopyDocuments = workingCopiesThatCanSeeFocus(copies, pattern.focus, pattern.isPolymorphicSearch(), participant);
	SearchDocument[] matches = null;
	int length = indexMatches.length;
	for (int i = 0; i < length; i++) {
		SearchDocument searchDocument = indexMatches[i];
		if (searchDocument.getParticipant() == participant) {
			SearchDocument workingCopyDocument = (SearchDocument) workingCopyDocuments.remove(searchDocument.getPath());
			if (workingCopyDocument != null) {
				if (matches == null) {
					System.arraycopy(indexMatches, 0, matches = new SearchDocument[length], 0, length);
				}
				matches[i] = workingCopyDocument;
			}
		}
	}
	if (matches == null) { // no working copy
		matches = indexMatches;
	}
	int remainingWorkingCopiesSize = workingCopyDocuments.size();
	if (remainingWorkingCopiesSize != 0) {
		System.arraycopy(matches, 0, matches = new SearchDocument[length+remainingWorkingCopiesSize], 0, length);
		Iterator iterator = workingCopyDocuments.values().iterator();
		int index = length;
		while (iterator.hasNext()) {
			matches[index++] = (SearchDocument) iterator.next();
		}
	}
	return matches;
}

public static void setFocus(InternalSearchPattern pattern, IJavaElement focus) {
	pattern.focus = focus;
}

/*
 * Returns the working copies that can see the given focus.
 */
private static HashMap workingCopiesThatCanSeeFocus(org.eclipse.jdt.core.ICompilationUnit[] copies, IJavaElement focus, boolean isPolymorphicSearch, SearchParticipant participant) {
	if (copies == null) return new HashMap();
	if (focus != null) {
		while (!(focus instanceof IJavaProject) && !(focus instanceof JarPackageFragmentRoot)) {
			focus = focus.getParent();
		}
	}
	HashMap result = new HashMap();
	for (int i=0, length = copies.length; i<length; i++) {
		org.eclipse.jdt.core.ICompilationUnit workingCopy = copies[i];
		IPath projectOrJar = MatchLocator.getProjectOrJar(workingCopy).getPath();
		if (focus == null || IndexSelector.canSeeFocus(focus, isPolymorphicSearch, projectOrJar)) {
			result.put(
				workingCopy.getPath().toString(),
				new WorkingCopyDocument(workingCopy, participant)
			);
		}
	}
	return result;
}

public static ClassFileReader classFileReader(IType type) {
	IClassFile classFile = type.getClassFile(); 
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	if (classFile.isOpen())
		return (ClassFileReader) manager.getInfo(type);

	IPackageFragment pkg = type.getPackageFragment();
	IPackageFragmentRoot root = (IPackageFragmentRoot) pkg.getParent();
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
		// invalid class file: return null
	} catch (CoreException e) {
		// cannot read class file: return null
	} catch (IOException e) {
		// cannot read class file: return null
	}
	return null;
}

public static SearchPattern createAndPattern(final SearchPattern leftPattern, final SearchPattern rightPattern) {
	return new AndPattern(0/*no kind*/, 0/*no rule*/) {
		SearchPattern current = leftPattern;
		public SearchPattern currentPattern() {
			return current;
		}
		protected boolean hasNextQuery() {
			if (current == leftPattern) {
				current = rightPattern;
				return true;
			}
			return false; 
		}
		protected void resetQuery() {
			current = leftPattern;
		}
	};
}

/**
 * Query a given index for matching entries. Assumes the sender has opened the index and will close when finished.
 */
public static void findIndexMatches(InternalSearchPattern pattern, Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor monitor) throws IOException {
	pattern.findIndexMatches(index, requestor, participant, scope, monitor);
}

public static IJavaElement getProjectOrJar(IJavaElement element) {
	while (!(element instanceof IJavaProject) && !(element instanceof JarPackageFragmentRoot)) {
		element = element.getParent();
	}
	return element;
}

public static boolean isPolymorphicSearch(InternalSearchPattern pattern) {
	return pattern.isPolymorphicSearch();
}

public static IJavaElement projectOrJarFocus(InternalSearchPattern pattern) {
	return pattern == null || pattern.focus == null ? null : getProjectOrJar(pattern.focus);
}

public MatchLocator(
	SearchPattern pattern,
	SearchRequestor requestor,
	IJavaSearchScope scope,
	IProgressMonitor progressMonitor) {
		
	this.pattern = pattern;
	this.patternLocator = PatternLocator.patternLocator(this.pattern);
	this.matchContainer = this.patternLocator.matchContainer();
	this.requestor = requestor;
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
		lookupEnvironment.completeTypeBindings(parsedUnit, true);
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
				SourceTypeConverter.FIELD_AND_METHOD // need field and methods
				| SourceTypeConverter.MEMBER_TYPE, // need member types
				// no need for field initialization
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
 * Add the possibleMatch to the loop
 *  ->  build compilation unit declarations, their bindings and record their results.
 */
protected void buildBindings(PossibleMatch possibleMatch) {
	if (this.progressMonitor != null && this.progressMonitor.isCanceled())
		throw new OperationCanceledException();

	try {
		if (SearchEngine.VERBOSE)
			System.out.println("Parsing " + possibleMatch.openable.toStringWithAncestors()); //$NON-NLS-1$

		this.parser.nodeSet = possibleMatch.nodeSet;
		CompilationResult unitResult = new CompilationResult(possibleMatch, 1, 1, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = this.parser.dietParse(possibleMatch, unitResult);
		if (parsedUnit != null) {
			if (!parsedUnit.isEmpty())
				this.lookupEnvironment.buildTypeBindings(parsedUnit);

			// add the possibleMatch with its parsedUnit to matchesToProcess
			possibleMatch.parsedUnit = parsedUnit;
			int size = this.matchesToProcess.length;
			if (this.numberOfMatches == size)
				System.arraycopy(this.matchesToProcess, 0, this.matchesToProcess = new PossibleMatch[size == 0 ? 1 : size * 2], 0, this.numberOfMatches);
			this.matchesToProcess[this.numberOfMatches++] = possibleMatch;

			if (this.progressMonitor != null)
				this.progressMonitor.worked(4);
		}
	} finally {
		this.parser.nodeSet = null;
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
			new MatchLocator(this.pattern, this.requestor, this.scope, this.progressMonitor), // clone MatchLocator so that it has no side effect
			focusType, 
			this.progressMonitor);
	try {
		this.allSuperTypeNames = superTypeNamesCollector.collect();
	} catch (JavaModelException e) {
		// problem collecting super type names: leave it null
	}
	return this.allSuperTypeNames;
}
/**
 * Creates an IMethod from the given method declaration and type. 
 */
protected IJavaElement createHandle(AbstractMethodDeclaration method, IJavaElement parent) {
	if (!(parent instanceof IType)) return parent;

	IType type = (IType) parent;
	Argument[] arguments = method.arguments;
	int argCount = arguments == null ? 0 : arguments.length;
	if (type.isBinary()) {
		// don't cache the methods of the binary type
		// fall thru if its a constructor with a synthetic argument... find it the slower way
		ClassFileReader reader = classFileReader(type);
		if (reader != null) {
			IBinaryMethod[] methods = reader.getMethods();
			if (methods != null) {
				boolean firstIsSynthetic = false;
				try {
					if (type.isMember() && method.isConstructor() && !Flags.isStatic(type.getFlags())) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=48261
						firstIsSynthetic = true;
						argCount++;
					}
				} catch (JavaModelException e) {
					// ignored
				}
				nextMethod : for (int i = 0, methodsLength = methods.length; i < methodsLength; i++) {
					IBinaryMethod binaryMethod = methods[i];
					char[] selector = binaryMethod.isConstructor() ? type.getElementName().toCharArray() : binaryMethod.getSelector();
					if (CharOperation.equals(selector, method.selector)) {
						char[][] parameterTypes = Signature.getParameterTypes(binaryMethod.getMethodDescriptor());
						if (argCount != parameterTypes.length) continue nextMethod;
						for (int j = 0; j < argCount; j++) {
							char[] typeName;
							if (j == 0 && firstIsSynthetic) {
								typeName = type.getDeclaringType().getFullyQualifiedName().toCharArray();
							} else {
								TypeReference typeRef = arguments[firstIsSynthetic ? j - 1 : j].type;
								typeName = CharOperation.concatWith(typeRef.getTypeName(), '.');
								for (int k = 0, dim = typeRef.dimensions(); k < dim; k++)
									typeName = CharOperation.concat(typeName, new char[] {'[', ']'});
							}
							char[] parameterTypeName = ClassFileMatchLocator.convertClassFileFormat(parameterTypes[j]);
							if (!CharOperation.endsWith(Signature.toCharArray(parameterTypeName), typeName))
								continue nextMethod;
							parameterTypes[j] = parameterTypeName;
						}
						return type.getMethod(new String(selector), CharOperation.toStrings(parameterTypes));
					}
				}
			}
		}
		return null;
	}

	String[] parameterTypeSignatures = new String[argCount];
	for (int i = 0; i < argCount; i++) {
		TypeReference typeRef = arguments[i].type;
		char[] typeName = CharOperation.concatWith(typeRef.getTypeName(), '.');
		for (int j = 0, dim = typeRef.dimensions(); j < dim; j++)
			typeName = CharOperation.concat(typeName, new char[] {'[', ']'});
		parameterTypeSignatures[i] = Signature.createTypeSignature(typeName, false);
	}
	return type.getMethod(new String(method.selector), parameterTypeSignatures);
}
/**
 * Creates an IField from the given field declaration and type. 
 */
protected IJavaElement createHandle(FieldDeclaration fieldDeclaration, TypeDeclaration typeDeclaration, IJavaElement parent) {
	if (!(parent instanceof IType)) return parent;

	if (fieldDeclaration.isField())
		return ((IType) parent).getField(new String(fieldDeclaration.name));

	// find occurence count of the given initializer in its type declaration
	int occurrenceCount = 0;
	FieldDeclaration[] fields = typeDeclaration.fields;
	for (int i = 0, length = fields.length; i < length; i++) {
		if (!fields[i].isField()) {
			occurrenceCount++;
			if (fields[i].equals(fieldDeclaration)) break;
		}
	}
	return ((IType) parent).getInitializer(occurrenceCount);
}
/*
 * Creates hierarchy resolver if needed. 
 * Returns whether focus is visible.
 */
protected boolean createHierarchyResolver(IType focusType, PossibleMatch[] possibleMatches) {
	// cache focus type if not a possible match
	char[][] compoundName = CharOperation.splitOn('.', focusType.getFullyQualifiedName().toCharArray());
	boolean isPossibleMatch = false;
	for (int i = 0, length = possibleMatches.length; i < length; i++) {
		if (CharOperation.equals(possibleMatches[i].compoundName, compoundName)) {
			isPossibleMatch = true;
			break;
		}
	}
	if (!isPossibleMatch) {
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
protected IJavaElement createImportHandle(ImportReference importRef) {
	char[] importName = CharOperation.concatWith(importRef.getImportName(), '.');
	if (importRef.onDemand)
		importName = CharOperation.concat(importName, ".*" .toCharArray()); //$NON-NLS-1$
	Openable openable = this.currentPossibleMatch.openable;
	if (openable instanceof CompilationUnit)
		return ((CompilationUnit) openable).getImport(new String(importName));

	// binary types do not contain import statements so just answer the top-level type as the element
	IType binaryType = ((ClassFile) openable).getType();
	String typeName = binaryType.getElementName();
	int lastDollar = typeName.lastIndexOf('$');
	if (lastDollar == -1) return binaryType;
	return createTypeHandle(typeName.substring(0, lastDollar));
}
/**
 * Creates an IType from the given simple top level type name. 
 */
protected IType createTypeHandle(String simpleTypeName) {
	Openable openable = this.currentPossibleMatch.openable;
	if (openable instanceof CompilationUnit)
		return ((CompilationUnit) openable).getType(simpleTypeName);

	IType binaryType = ((ClassFile) openable).getType();
	if (simpleTypeName.equals(binaryType.getTypeQualifiedName()))
		return binaryType; // answer only top-level types, sometimes the classFile is for a member/local type

	try {
		IClassFile classFile = binaryType.getPackageFragment().getClassFile(simpleTypeName + SuffixConstants.SUFFIX_STRING_class);
		return classFile.getType();
	} catch (JavaModelException e) {
		// ignore as implementation of getType() cannot throw this exception
	}
	return null;
}
protected boolean encloses(IJavaElement element) {
	return element != null && this.scope.encloses(element);
}
protected IBinaryType getBinaryInfo(ClassFile classFile, IResource resource) throws CoreException {
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
protected void getMethodBodies(CompilationUnitDeclaration unit) {
	if (unit.ignoreMethodBodies) {
		unit.ignoreFurtherInvestigation = true;
		return; // if initial diet parse did not work, no need to dig into method bodies.
	}

	// save existing values to restore them at the end of the parsing process
	// see bug 47079 for more details
	int[] oldLineEnds = this.parser.scanner.lineEnds;
	int oldLinePtr = this.parser.scanner.linePtr;
	
	try {
		char[] contents = unit.compilationResult.compilationUnit.getContents();
		this.parser.scanner.setSource(contents);

		// inline old setLineEnds
		final int[] lineSeparatorPositions = unit.compilationResult.lineSeparatorPositions;
		this.parser.scanner.lineEnds = lineSeparatorPositions;
		this.parser.scanner.linePtr = lineSeparatorPositions.length - 1;

		if (this.parser.javadocParser.checkDocComment) {
			this.parser.javadocParser.scanner.setSource(contents);
		}
		this.parser.nodeSet = this.currentPossibleMatch.nodeSet;
		this.parser.parseBodies(unit);
	} finally {
		this.parser.nodeSet = null;
		// this is done to prevent any side effects on the compilation unit result
		// line separator positions array.
		this.parser.scanner.lineEnds = oldLineEnds;
		this.parser.scanner.linePtr = oldLinePtr;
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
public void initialize(JavaProject project, int possibleMatchSize) throws JavaModelException {
	if (this.nameEnvironment != null)
		this.nameEnvironment.cleanup();

	SearchableEnvironment searchableEnvironment = (SearchableEnvironment) project.newSearchableNameEnvironment(this.workingCopies);
	
	// if only one possible match, a file name environment costs too much,
	// so use the existing searchable  environment which will populate the java model
	// only for this possible match and its required types.
	this.nameEnvironment = possibleMatchSize == 1
		? (INameEnvironment) searchableEnvironment
		: (INameEnvironment) new JavaSearchNameEnvironment(project, this.workingCopies);

	// create lookup environment
	this.options = new CompilerOptions(project.getOptions(true));
	ProblemReporter problemReporter =
		new ProblemReporter(
			DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			this.options,
			new DefaultProblemFactory());
	this.lookupEnvironment = new LookupEnvironment(this, this.options, problemReporter, this.nameEnvironment);

	this.parser = MatchLocatorParser.createParser(problemReporter, this);

	// remember project's name lookup
	this.nameLookup = searchableEnvironment.nameLookup;

	// initialize queue of units
	this.numberOfMatches = 0;
	this.matchesToProcess = new PossibleMatch[possibleMatchSize];
}
protected void locateMatches(JavaProject javaProject, PossibleMatch[] possibleMatches, int start, int length) throws CoreException {
	initialize(javaProject, length);

	// create and resolve binding (equivalent to beginCompilation() in Compiler)
	boolean bindingsWereCreated = true;
	try {
		for (int i = start, maxUnits = start + length; i < maxUnits; i++)
			buildBindings(possibleMatches[i]);
		lookupEnvironment.completeTypeBindings();

		// create hierarchy resolver if needed
		IType focusType = getFocusType();
		if (focusType == null) {
			this.hierarchyResolver = null;
		} else if (!createHierarchyResolver(focusType, possibleMatches)) {
			// focus type is not visible, use the super type names instead of the bindings
			if (computeSuperTypeNames(focusType) == null) return;
		}
	} catch (AbortCompilation e) {
		bindingsWereCreated = false;
	}

	// possible match resolution
	for (int i = 0; i < this.numberOfMatches; i++) {
		if (this.progressMonitor != null && this.progressMonitor.isCanceled())
			throw new OperationCanceledException();
		PossibleMatch possibleMatch = this.matchesToProcess[i];
		this.matchesToProcess[i] = null; // release reference to processed possible match
		try {
			process(possibleMatch, bindingsWereCreated);
		} catch (AbortCompilation e) {
			// problem with class path: it could not find base classes
			// continue and try next matching openable reporting innacurate matches (since bindings will be null)
			bindingsWereCreated = false;
		} catch (JavaModelException e) {
			// problem with class path: it could not find base classes
			// continue and try next matching openable reporting innacurate matches (since bindings will be null)
			bindingsWereCreated = false;
		} finally {
			if (this.options.verbose)
				System.out.println(Util.bind("compilation.done", //$NON-NLS-1$
					new String[] {
						String.valueOf(i + 1),
						String.valueOf(numberOfMatches),
						new String(possibleMatch.parsedUnit.getFileName())}));
			// cleanup compilation unit result
			possibleMatch.parsedUnit.cleanUp();
			possibleMatch.parsedUnit = null;
		}
		if (this.progressMonitor != null)
			this.progressMonitor.worked(5);
	}
}
/**
 * Locate the matches amongst the possible matches.
 */
protected void locateMatches(JavaProject javaProject, PossibleMatchSet matchSet) throws CoreException {
	PossibleMatch[] possibleMatches = matchSet.getPossibleMatches(javaProject.getPackageFragmentRoots());
	for (int index = 0, length = possibleMatches.length; index < length;) {
		int max = Math.min(MAX_AT_ONCE, length - index);
		locateMatches(javaProject, possibleMatches, index, max);
		index += max;
	}
}
/**
 * Locate the matches in the given files and report them using the search requestor. 
 */
public void locateMatches(SearchDocument[] searchDocuments) throws CoreException {
	if (SearchEngine.VERBOSE) {
		System.out.println("Locating matches in documents ["); //$NON-NLS-1$
		for (int i = 0, length = searchDocuments.length; i < length; i++)
			System.out.println("\t" + searchDocuments[i]); //$NON-NLS-1$
		System.out.println("]"); //$NON-NLS-1$
	}
	
	// extract working copies
	ArrayList copies = new ArrayList();
	for (int i = 0, length = searchDocuments.length; i < length; i++) {
		SearchDocument document = searchDocuments[i];
		if (document instanceof WorkingCopyDocument) {
			copies.add(((WorkingCopyDocument)document).workingCopy);
		}
	}
	int copiesLength = copies.size();
	this.workingCopies = new org.eclipse.jdt.core.ICompilationUnit[copiesLength];
	copies.toArray(this.workingCopies);

	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	try {
		// optimize access to zip files during search operation
		manager.cacheZipFiles();

		// initialize handle factory (used as a cache of handles so as to optimize space)
		if (this.handleFactory == null)
			this.handleFactory = new HandleFactory();

		if (this.progressMonitor != null) {
			// 1 for file path, 4 for parsing and binding creation, 5 for binding resolution? //$NON-NLS-1$
			this.progressMonitor.beginTask("", searchDocuments.length * (((InternalSearchPattern)this.pattern).mustResolve ? 10 : 5)); //$NON-NLS-1$
		}

		// initialize pattern for polymorphic search (ie. method reference pattern)
		this.patternLocator.initializePolymorphicSearch(this);

		JavaProject previousJavaProject = null;
		PossibleMatchSet matchSet = new PossibleMatchSet();
		Util.sort(searchDocuments, new Util.Comparer() {
			public int compare(Object a, Object b) {
				return ((SearchDocument)a).getPath().compareTo(((SearchDocument)b).getPath());
			}
		}); 
		for (int i = 0, l = searchDocuments.length; i < l; i++) {
			if (this.progressMonitor != null && this.progressMonitor.isCanceled())
				throw new OperationCanceledException();

			// skip duplicate paths
			SearchDocument searchDocument = searchDocuments[i];
			String pathString = searchDocument.getPath();
			if (i > 0 && pathString.equals(searchDocuments[i - 1].getPath())) continue;

			Openable openable;
			org.eclipse.jdt.core.ICompilationUnit workingCopy = null;
			if (searchDocument instanceof WorkingCopyDocument) {
				workingCopy = ((WorkingCopyDocument)searchDocument).workingCopy;
				openable = (Openable) workingCopy;
			} else {
				openable = this.handleFactory.createOpenable(pathString, this.scope);
				if (openable == null) continue; // match is outside classpath
			}

			// create new parser and lookup environment if this is a new project
			IResource resource = null;
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
						// problem with classpath in this project -> skip it
					}
					matchSet.reset();
				}
				previousJavaProject = javaProject;
			}
			matchSet.add(new PossibleMatch(this, resource, openable, searchDocument));

			if (this.progressMonitor != null)
				this.progressMonitor.worked(1);
		}

		// last project
		if (previousJavaProject != null) {
			try {
				locateMatches(previousJavaProject, matchSet);
			} catch (JavaModelException e) {
				// problem with classpath in last project -> ignore
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
public void locatePackageDeclarations(SearchParticipant participant) throws CoreException {
	locatePackageDeclarations(this.pattern, participant);
}
/**
 * Locates the package declarations corresponding to the search pattern. 
 */
protected void locatePackageDeclarations(SearchPattern searchPattern, SearchParticipant participant) throws CoreException {
	if (searchPattern instanceof OrPattern) {
		SearchPattern[] patterns = ((OrPattern) searchPattern).patterns;
		for (int i = 0, length = patterns.length; i < length; i++)
			locatePackageDeclarations(patterns[i], participant);
	} else if (searchPattern instanceof PackageDeclarationPattern) {
		IJavaElement focus = ((InternalSearchPattern) searchPattern).focus;
		if (focus != null) {
			IResource resource = focus.getResource();
			SearchDocument document = participant.getDocument(resource.getFullPath().toString());
			this.currentPossibleMatch = new PossibleMatch(this, resource, null, document);
			if (encloses(focus)) {
				SearchMatch match = newDeclarationMatch(focus, SearchMatch.A_ACCURATE, -1, -1);
				report(match);
			}
			return;
		}
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
						SearchDocument document = participant.getDocument(resource.getFullPath().toString());
						this.currentPossibleMatch = new PossibleMatch(this, resource, null, document);
						try {
							if (encloses(pkg)) {
								SearchMatch match = newDeclarationMatch(pkg, SearchMatch.A_ACCURATE, -1, -1);
								report(match);
							}
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
protected IType lookupType(ReferenceBinding typeBinding) {
	if (typeBinding == null) return null;

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

	IType type = createTypeHandle(new String(qualifiedName[0])); // find the top-level type
	if (type == null) return null;

	for (int i = 1; i < length; i++) {
		type = type.getType(new String(qualifiedName[i]));
		if (type == null) return null;
	}
	if (type.exists()) return type;
	return null;
}
public SearchMatch newDeclarationMatch(
		IJavaElement element,
		int accuracy,
		int offset,  
		int length) {
	SearchParticipant participant = getParticipant(); 
	IResource resource = this.currentPossibleMatch.resource;
	return newDeclarationMatch(element, accuracy, offset, length, participant, resource);
}

public SearchMatch newDeclarationMatch(
		IJavaElement element,
		int accuracy,
		int offset,  
		int length,
		SearchParticipant participant, 
		IResource resource) {
	switch (element.getElementType()) {
		case IJavaElement.PACKAGE_FRAGMENT:
			return new PackageDeclarationMatch(element, accuracy, offset, length, participant, resource);
		case IJavaElement.TYPE:
			return new TypeDeclarationMatch(element, accuracy, offset, length, participant, resource);
		case IJavaElement.FIELD:
			return new FieldDeclarationMatch(element, accuracy, offset, length, participant, resource);
		case IJavaElement.METHOD:
			return new MethodDeclarationMatch(element, accuracy, offset, length, participant, resource);
		case IJavaElement.LOCAL_VARIABLE:
			return new LocalVariableDeclarationMatch(element, accuracy, offset, length, participant, resource);
		default:
			return null;
	}
}

public SearchMatch newFieldReferenceMatch(
		IJavaElement enclosingElement,
		int accuracy,
		int offset,  
		int length,
		ASTNode reference) {
	int bits = reference.bits;
	boolean isCoupoundAssigned = (bits & ASTNode.IsCompoundAssignedMASK) != 0;
	boolean isReadAccess = isCoupoundAssigned || (bits & ASTNode.IsStrictlyAssignedMASK) == 0;
	boolean isWriteAccess = isCoupoundAssigned || (bits & ASTNode.IsStrictlyAssignedMASK) != 0;
	boolean insideDocComment = (bits & ASTNode.InsideJavadoc) != 0;
	SearchParticipant participant = getParticipant(); 
	IResource resource = this.currentPossibleMatch.resource;
	return new FieldReferenceMatch(enclosingElement, accuracy, offset, length, isReadAccess, isWriteAccess, insideDocComment, participant, resource);
}

public SearchMatch newLocalVariableReferenceMatch(
		IJavaElement enclosingElement,
		int accuracy,
		int offset,  
		int length,
		ASTNode reference) {
	int bits = reference.bits;
	boolean isCoupoundAssigned = (bits & ASTNode.IsCompoundAssignedMASK) != 0;
	boolean isReadAccess = isCoupoundAssigned || (bits & ASTNode.IsStrictlyAssignedMASK) == 0;
	boolean isWriteAccess = isCoupoundAssigned || (bits & ASTNode.IsStrictlyAssignedMASK) != 0;
	boolean insideDocComment = (bits & ASTNode.InsideJavadoc) != 0;
	SearchParticipant participant = getParticipant(); 
	IResource resource = this.currentPossibleMatch.resource;
	return new LocalVariableReferenceMatch(enclosingElement, accuracy, offset, length, isReadAccess, isWriteAccess, insideDocComment, participant, resource);
}

public SearchMatch newMethodReferenceMatch(
		IJavaElement enclosingElement,
		int accuracy,
		int offset,  
		int length,
		ASTNode reference) {
	SearchParticipant participant = getParticipant(); 
	IResource resource = this.currentPossibleMatch.resource;
	boolean insideDocComment = (reference.bits & ASTNode.InsideJavadoc) != 0;
	return new MethodReferenceMatch(enclosingElement, accuracy, offset, length, insideDocComment, participant, resource);
}

public SearchMatch newPackageReferenceMatch(
		IJavaElement enclosingElement,
		int accuracy,
		int offset,  
		int length,
		ASTNode reference) {
	SearchParticipant participant = getParticipant(); 
	IResource resource = this.currentPossibleMatch.resource;
	return new PackageReferenceMatch(enclosingElement, accuracy, offset, length, participant, resource);
}

public SearchMatch newTypeReferenceMatch(
		IJavaElement enclosingElement,
		int accuracy,
		int offset,  
		int length,
		ASTNode reference) {
	SearchParticipant participant = getParticipant(); 
	IResource resource = this.currentPossibleMatch.resource;
	boolean insideDocComment = (reference.bits & ASTNode.InsideJavadoc) != 0;
	return new TypeReferenceMatch(enclosingElement, accuracy, offset, length, insideDocComment, participant, resource);
}

/*
 * Process a compilation unit already parsed and build.
 */
protected void process(PossibleMatch possibleMatch, boolean bindingsWereCreated) throws CoreException {
	this.currentPossibleMatch = possibleMatch;
	CompilationUnitDeclaration unit = possibleMatch.parsedUnit;
	try {
		if (unit.isEmpty()) {
			if (this.currentPossibleMatch.openable instanceof ClassFile) {
				ClassFile classFile = (ClassFile) this.currentPossibleMatch.openable;
				IBinaryType info = getBinaryInfo(classFile, this.currentPossibleMatch.resource);
				if (info != null)
					new ClassFileMatchLocator().locateMatches(this, classFile, info);
			}
			return;
		}
		if (hasAlreadyDefinedType(unit)) return; // skip type has it is hidden so not visible

		getMethodBodies(unit);

		if (bindingsWereCreated && ((InternalSearchPattern)this.pattern).mustResolve && unit.types != null) {
			if (SearchEngine.VERBOSE)
				System.out.println("Resolving " + this.currentPossibleMatch.openable.toStringWithAncestors()); //$NON-NLS-1$

			reduceParseTree(unit);

			if (unit.scope != null)
				unit.scope.faultInTypes(); // fault in fields & methods
			unit.resolve();

			reportMatching(unit, true);
		} else {
			reportMatching(unit, ((InternalSearchPattern)this.pattern).mustResolve);
		}
	} catch (AbortCompilation e) {
		// could not resolve: report innacurate matches
		reportMatching(unit, true); // was partially resolved
		if (!(e instanceof AbortCompilationUnit)) {
			// problem with class path
			throw e;
		}
	} finally {
		this.currentPossibleMatch.cleanUp();
		this.currentPossibleMatch = null;
	}
}
protected void purgeMethodStatements(TypeDeclaration type, boolean checkEachMethod) {
	checkEachMethod = checkEachMethod
		&& this.currentPossibleMatch.nodeSet.hasPossibleNodes(type.declarationSourceStart, type.declarationSourceEnd);
	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		if (checkEachMethod) {
			for (int j = 0, length = methods.length; j < length; j++) {
				AbstractMethodDeclaration method = methods[j];
				if (!this.currentPossibleMatch.nodeSet.hasPossibleNodes(method.declarationSourceStart, method.declarationSourceEnd)) {
					method.statements = null;
					method.javadoc = null;
				}
			}
		} else {
			for (int j = 0, length = methods.length; j < length; j++) {
				methods[j].statements = null;
				methods[j].javadoc = null;
			}
		}
	}

	TypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null)
		for (int i = 0, l = memberTypes.length; i < l; i++)
			purgeMethodStatements(memberTypes[i], checkEachMethod);
}
/**
 * Called prior to the unit being resolved. Reduce the parse tree where possible.
 */
protected void reduceParseTree(CompilationUnitDeclaration unit) {
	// remove statements from methods that have no possible matching nodes
	TypeDeclaration[] types = unit.types;
	for (int i = 0, l = types.length; i < l; i++)
		purgeMethodStatements(types[i], true); 
}
public SearchParticipant getParticipant() {
	return this.currentPossibleMatch.document.getParticipant();
}

protected void report(SearchMatch match) throws CoreException {
	long start = -1;
	if (SearchEngine.VERBOSE) {
		start = System.currentTimeMillis();
		System.out.println("Reporting match"); //$NON-NLS-1$
		System.out.println("\tResource: " + match.getResource()); //$NON-NLS-2$//$NON-NLS-1$
		System.out.println("\tPositions: [offset=" + match.getOffset() + ", length=" + match.getLength() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		System.out.println("\tJava element: " + ((JavaElement)match.getElement()).toStringWithAncestors()); //$NON-NLS-1$
		System.out.println(match.getAccuracy() == SearchMatch.A_ACCURATE
			? "\tAccuracy: EXACT_MATCH" //$NON-NLS-1$
			: "\tAccuracy: POTENTIAL_MATCH"); //$NON-NLS-1$
	}
	this.requestor.acceptSearchMatch(match);
	if (SearchEngine.VERBOSE)
		this.resultCollectorTime += System.currentTimeMillis()-start;
}
/**
 * Finds the accurate positions of the sequence of tokens given by qualifiedName
 * in the source and reports a reference to this this qualified name
 * to the search requestor.
 */
protected void reportAccurateTypeReference(ASTNode typeRef, char[] name, IJavaElement element, int accuracy) throws CoreException {
	if (accuracy == -1) return;
	if (!encloses(element)) return;

	int sourceStart = typeRef.sourceStart;
	int sourceEnd = typeRef.sourceEnd;
	
	// compute source positions of the qualified reference 
	Scanner scanner = this.parser.scanner;
	scanner.setSource(this.currentPossibleMatch.getContents());
	scanner.resetTo(sourceStart, sourceEnd);

	int token = -1;
	int currentPosition;
	do {
		currentPosition = scanner.currentPosition;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			// ignore
		}
		if (token == TerminalTokens.TokenNameIdentifier && this.pattern.matchesName(name, scanner.getCurrentTokenSource())) {
			int length = scanner.currentPosition-currentPosition;
			SearchMatch match = newTypeReferenceMatch(element, accuracy, currentPosition, length, typeRef);
			report(match);
			return;
		}
	} while (token != TerminalTokens.TokenNameEOF);
	SearchMatch match = newTypeReferenceMatch(element, accuracy, sourceStart, sourceEnd-sourceStart+1, typeRef);
	report(match);
}
/**
 * Finds the accurate positions of each valid token in the source and
 * reports a reference to this token to the search requestor.
 * A token is valid if it has an accuracy which is not -1.
 */
protected void reportAccurateFieldReference(QualifiedNameReference qNameRef, IJavaElement element, int[] accuracies) throws CoreException {
	if (!encloses(element)) return;
	
	int sourceStart = qNameRef.sourceStart;
	int sourceEnd = qNameRef.sourceEnd;
	char[][] tokens = qNameRef.tokens;
	
	// compute source positions of the qualified reference 
	Scanner scanner = this.parser.scanner;
	scanner.setSource(this.currentPossibleMatch.getContents());
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
			//ignore
		}
		if (token != TerminalTokens.TokenNameEOF) {
			char[] currentTokenSource = scanner.getCurrentTokenSource();
			boolean equals = false;
			while (i < length && !(equals = this.pattern.matchesName(tokens[i++], currentTokenSource))){/*empty*/}
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
				// ignore
			}
		}
		if (accuracies[accuracyIndex] != -1) {
			// accept reference
			if (refSourceStart != -1) {
				SearchMatch match = newFieldReferenceMatch(element, accuracies[accuracyIndex], refSourceStart, refSourceEnd-refSourceStart+1, qNameRef);
				report(match);
			} else {
				SearchMatch match = newFieldReferenceMatch(element, accuracies[accuracyIndex], sourceStart, sourceEnd-sourceStart+1, qNameRef);
				report(match);
			}
			i = 0;
		}
		refSourceStart = -1;
		previousValid = -1;
		if (accuracyIndex < accuracies.length - 1)
			accuracyIndex++;
	} while (token != TerminalTokens.TokenNameEOF);

}
protected void reportBinaryMemberDeclaration(IResource resource, IMember binaryMember, IBinaryType info, int accuracy) throws CoreException {
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
	if (resource == null) resource =  this.currentPossibleMatch.resource;
	SearchMatch match = newDeclarationMatch(binaryMember, accuracy, range.getOffset(), range.getLength(), getParticipant(), resource);
	report(match);
}
/**
 * Visit the given method declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 * Note that the method declaration has already been checked.
 */
protected void reportMatching(AbstractMethodDeclaration method, IJavaElement parent, int accuracy, boolean typeInHierarchy, MatchingNodeSet nodeSet) throws CoreException {
	IJavaElement enclosingElement = null;
	if (accuracy > -1) {
		enclosingElement = createHandle(method, parent);
		if (enclosingElement != null) { // skip if unable to find method
			// compute source positions of the selector 
			Scanner scanner = parser.scanner;
			int nameSourceStart = method.sourceStart;
			scanner.setSource(this.currentPossibleMatch.getContents());
			scanner.resetTo(nameSourceStart, method.sourceEnd);
			try {
				scanner.getNextToken();
			} catch (InvalidInputException e) {
				// ignore
			}
			if (encloses(enclosingElement)) {
				int length = scanner.currentPosition - nameSourceStart;
				SearchMatch match = newDeclarationMatch(enclosingElement, accuracy, nameSourceStart, length);
				report(match);
			}
		}
	}

	// handle nodes for the local type first
	if ((method.bits & ASTNode.HasLocalTypeMASK) != 0) {
		if (enclosingElement == null)
			enclosingElement = createHandle(method, parent);
		LocalDeclarationVisitor localDeclarationVisitor = new LocalDeclarationVisitor(enclosingElement, nodeSet);
		try {
			method.traverse(localDeclarationVisitor, (ClassScope) null);
		} catch (WrappedCoreException e) {
			throw e.coreException;
		}
	}

	// references in this method
	if (typeInHierarchy) {
		ASTNode[] nodes = nodeSet.matchingNodes(method.declarationSourceStart, method.declarationSourceEnd);
		if (nodes != null) {
			if ((this.matchContainer & PatternLocator.METHOD_CONTAINER) != 0) {
				if (enclosingElement == null)
					enclosingElement = createHandle(method, parent);
				if (encloses(enclosingElement)) {
					for (int i = 0, l = nodes.length; i < l; i++) {
						ASTNode node = nodes[i];
						Integer level = (Integer) nodeSet.matchingNodes.removeKey(node);
						this.patternLocator.matchReportReference(node, enclosingElement, level.intValue(), this);
					}
					return;
				}
			}
			for (int i = 0, l = nodes.length; i < l; i++)
				nodeSet.matchingNodes.removeKey(nodes[i]);
		}
	}
}
/**
 * Visit the given resolved parse tree and report the nodes that match the search pattern.
 */
protected void reportMatching(CompilationUnitDeclaration unit, boolean mustResolve) throws CoreException {
	MatchingNodeSet nodeSet = this.currentPossibleMatch.nodeSet;
	if (mustResolve) {
		// move the possible matching nodes that exactly match the search pattern to the matching nodes set
		Object[] nodes = nodeSet.possibleMatchingNodesSet.values;
		for (int i = 0, l = nodes.length; i < l; i++) {
			ASTNode node = (ASTNode) nodes[i];
			if (node == null) continue;
			if (node instanceof ImportReference) {
				// special case for import refs: they don't know their binding
				// import ref cannot be in the hirarchy of a type
				if (this.hierarchyResolver != null) continue;

				ImportReference importRef = (ImportReference) node;
				Binding binding = importRef.onDemand
					? unit.scope.getTypeOrPackage(CharOperation.subarray(importRef.tokens, 0, importRef.tokens.length))
					: unit.scope.getTypeOrPackage(importRef.tokens);
				this.patternLocator.matchLevelAndReportImportRef(importRef, binding, this);
			} else {
				nodeSet.addMatch(node, this.patternLocator.resolveLevel(node));
			}
		}
		nodeSet.possibleMatchingNodesSet = new SimpleSet(3);
	}

	if (nodeSet.matchingNodes.elementSize == 0) return; // no matching nodes were found

	boolean matchedUnitContainer = (this.matchContainer & PatternLocator.COMPILATION_UNIT_CONTAINER) != 0;
	if (matchedUnitContainer) {
// Currently a no-op
//	ImportReference pkg = unit.currentPackage;
//	if (pkg != null && nodeSet.matchingNodes.removeKey(pkg) != null)
//		reportPackageDeclaration(pkg);

		ImportReference[] imports = unit.imports;
		if (imports != null) {
			for (int i = 0, l = imports.length; i < l; i++) {
				ImportReference importRef = imports[i];
				Integer level = (Integer) nodeSet.matchingNodes.removeKey(importRef);
				if (level != null)
					this.patternLocator.matchReportImportRef(importRef, null, createImportHandle(importRef), level.intValue(), this);
			}
		}
	}

	TypeDeclaration[] types = unit.types;
	if (types != null) {
		for (int i = 0, l = types.length; i < l; i++) {
			if (nodeSet.matchingNodes.elementSize == 0) return; // reported all the matching nodes
			TypeDeclaration type = types[i];
			Integer level = (Integer) nodeSet.matchingNodes.removeKey(type);
			int accuracy = (level != null && matchedUnitContainer) ? level.intValue() : -1;
			reportMatching(type, null, accuracy, nodeSet, 1);
		}
	}
}
/**
 * Visit the given field declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 */
protected void reportMatching(FieldDeclaration field, TypeDeclaration type, IJavaElement parent, int accuracy, boolean typeInHierarchy, MatchingNodeSet nodeSet) throws CoreException {
	IJavaElement enclosingElement = null;
	if (accuracy > -1) {
		enclosingElement = createHandle(field, type, parent);
		if (encloses(enclosingElement)) {
			int offset = field.sourceStart;
			SearchMatch match = newDeclarationMatch(enclosingElement, accuracy, offset, field.sourceEnd-offset+1);
			report(match);
		}
	}

	// handle the nodes for the local type first
	if ((field.bits & ASTNode.HasLocalTypeMASK) != 0) {
		if (enclosingElement == null)
			enclosingElement = createHandle(field, type, parent);
		LocalDeclarationVisitor localDeclarationVisitor = new LocalDeclarationVisitor(enclosingElement, nodeSet);
		try {
			field.traverse(localDeclarationVisitor, null);
		} catch (WrappedCoreException e) {
			throw e.coreException;
		}
	}

	if (typeInHierarchy) {
		ASTNode[] nodes = nodeSet.matchingNodes(field.declarationSourceStart, field.declarationSourceEnd);
		if (nodes != null) {
			if ((this.matchContainer & PatternLocator.FIELD_CONTAINER) == 0) {
				for (int i = 0, l = nodes.length; i < l; i++)
					nodeSet.matchingNodes.removeKey(nodes[i]);
			} else {
				if (enclosingElement == null)
					enclosingElement = createHandle(field, type, parent);
				if (encloses(enclosingElement))
					for (int i = 0, l = nodes.length; i < l; i++) {
						ASTNode node = nodes[i];
						Integer level = (Integer) nodeSet.matchingNodes.removeKey(node);
						this.patternLocator.matchReportReference(node, enclosingElement, level.intValue(), this);
					}
			}
		}
	}
}
/**
 * Visit the given type declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 */
protected void reportMatching(TypeDeclaration type, IJavaElement parent, int accuracy, MatchingNodeSet nodeSet, int occurrenceCount) throws CoreException {
	// create type handle
	IJavaElement enclosingElement = parent;
	if (enclosingElement == null) {
		enclosingElement = createTypeHandle(new String(type.name));
	} else if (enclosingElement instanceof IType) {
		enclosingElement = ((IType) parent).getType(new String(type.name));
	} else if (enclosingElement instanceof IMember) {
	    IMember member = (IMember) parent;
	    if (member.isBinary()) 
	        enclosingElement = parent;
	    else
			enclosingElement = member.getType(new String(type.name), occurrenceCount);
	}
	if (enclosingElement == null) return;

	// report the type declaration
	if (accuracy > -1 && encloses(enclosingElement)) {
		int offset = type.sourceStart;
		SearchMatch match = newDeclarationMatch(enclosingElement, accuracy, offset, type.sourceEnd-offset+1);
		report(match);
	}

	boolean matchedClassContainer = (this.matchContainer & PatternLocator.CLASS_CONTAINER) != 0;

	// javadoc
	if (type.javadoc != null) {
		ASTNode[] nodes = nodeSet.matchingNodes(type.declarationSourceStart, type.sourceStart);
		if (nodes != null) {
			if (!matchedClassContainer) {
				for (int i = 0, l = nodes.length; i < l; i++)
					nodeSet.matchingNodes.removeKey(nodes[i]);
			} else {
				for (int i = 0, l = nodes.length; i < l; i++) {
					ASTNode node = nodes[i];
					Integer level = (Integer) nodeSet.matchingNodes.removeKey(node);
					if (encloses(enclosingElement))
						this.patternLocator.matchReportReference(node, enclosingElement, level.intValue(), this);
				}
			}
		}
	}
	
	// super types
	if ((type.bits & ASTNode.IsAnonymousTypeMASK) != 0) {
		TypeReference superType =type.allocation.type;
		if (superType != null) {
			Integer level = (Integer) nodeSet.matchingNodes.removeKey(superType);
			if (level != null && matchedClassContainer)
				this.patternLocator.matchReportReference(superType, enclosingElement, level.intValue(), this);
		}
	} else {
		TypeReference superClass = type.superclass;
		if (superClass != null) {
			Integer level = (Integer) nodeSet.matchingNodes.removeKey(superClass);
			if (level != null && matchedClassContainer)
				this.patternLocator.matchReportReference(superClass, enclosingElement, level.intValue(), this);
		}
		TypeReference[] superInterfaces = type.superInterfaces;
		if (superInterfaces != null) {
			for (int i = 0, l = superInterfaces.length; i < l; i++) {
				TypeReference superInterface = superInterfaces[i];
				Integer level = (Integer) nodeSet.matchingNodes.removeKey(superInterface);
				if (level != null && matchedClassContainer)
					this.patternLocator.matchReportReference(superInterface, enclosingElement, level.intValue(), this);
			}
		}
	}

	// filter out element not in hierarchy scope
	boolean typeInHierarchy = type.binding == null || typeInHierarchy(type.binding);
	matchedClassContainer = matchedClassContainer && typeInHierarchy; 

	FieldDeclaration[] fields = type.fields;
	if (fields != null) {
		if (nodeSet.matchingNodes.elementSize == 0) return; // reported all the matching nodes
		for (int i = 0, l = fields.length; i < l; i++) {
			FieldDeclaration field = fields[i];
			Integer level = (Integer) nodeSet.matchingNodes.removeKey(field);
			int value = (level != null && matchedClassContainer) ? level.intValue() : -1;
			reportMatching(field, type, enclosingElement, value, typeInHierarchy, nodeSet);
		}
	}

	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		if (nodeSet.matchingNodes.elementSize == 0) return; // reported all the matching nodes
		for (int i = 0, l = methods.length; i < l; i++) {
			AbstractMethodDeclaration method = methods[i];
			Integer level = (Integer) nodeSet.matchingNodes.removeKey(method);
			int value = (level != null && matchedClassContainer) ? level.intValue() : -1;
			reportMatching(method, enclosingElement, value, typeInHierarchy, nodeSet);
		}
	}

	TypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null) {
		for (int i = 0, l = memberTypes.length; i < l; i++) {
			if (nodeSet.matchingNodes.elementSize == 0) return; // reported all the matching nodes
			TypeDeclaration memberType = memberTypes[i];
			Integer level = (Integer) nodeSet.matchingNodes.removeKey(memberType);
			int value = (level != null && matchedClassContainer) ? level.intValue() : -1;
			reportMatching(memberType, enclosingElement, value, nodeSet, 1);
		}
	}
}
protected boolean typeInHierarchy(ReferenceBinding binding) {
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
