/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.HashtableOfIntValues;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.BinaryMember;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.*;
import org.eclipse.jdt.internal.core.util.HandleFactory;
import org.eclipse.jdt.internal.core.util.SimpleSet;
import org.eclipse.jdt.internal.core.util.Util;

public class MatchLocator implements ITypeRequestor {

public static final int MAX_AT_ONCE = 400;

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

// Progress information
int progressStep;
int progressWorked;

// Binding resolution and cache
CompilationUnitScope unitScope;
SimpleLookupTable bindings;

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
			if (typeDeclaration.allocation == null || typeDeclaration.allocation.enumConstant == null) {
				if ((typeDeclaration.bits & ASTNode.IsAnonymousTypeMASK) != 0) {				
					reportMatching(typeDeclaration, enclosingElement, -1, nodeSet, occurrenceCount);
				} else {
					Integer level = (Integer) nodeSet.matchingNodes.removeKey(typeDeclaration);
					reportMatching(typeDeclaration, enclosingElement, level != null ? level.intValue() : -1, nodeSet, occurrenceCount);
				}
			} else {
				Integer level = (Integer) nodeSet.matchingNodes.removeKey(typeDeclaration);
				if (level != null) {
					FieldDeclaration enumConstant = typeDeclaration.allocation.enumConstant;
					int offset = enumConstant.sourceStart;
					SearchMatch match = newDeclarationMatch(enclosingElement, level.intValue(), offset, enumConstant.sourceEnd-offset+1);
					report(match);
				}
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
	private static final long serialVersionUID = 8354329870126121212L; // backward compatible
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

	PackageFragment pkg = (PackageFragment) type.getPackageFragment();
	IPackageFragmentRoot root = (IPackageFragmentRoot) pkg.getParent();
	try {
		if (!root.isArchive())
			return ClassFileReader.read(type.getResource().getLocation().toOSString());

		IPath zipPath = root.isExternal() ? root.getPath() : root.getResource().getLocation();
		if (zipPath == null) return null; // location is null
		ZipFile zipFile = null;
		try {
			if (JavaModelManager.ZIP_ACCESS_VERBOSE)
				System.out.println("(" + Thread.currentThread() + ") [MatchLocator.classFileReader()] Creating ZipFile on " + zipPath); //$NON-NLS-1$	//$NON-NLS-2$
			zipFile = manager.getZipFile(zipPath);
			String classFileName = classFile.getElementName();
			String path = Util.concatWith(pkg.names, classFileName, '/');
			return ClassFileReader.read(zipFile, path);
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
			return this.current;
		}
		protected boolean hasNextQuery() {
			if (this.current == leftPattern) {
				this.current = rightPattern;
				return true;
			}
			return false; 
		}
		protected void resetQuery() {
			this.current = leftPattern;
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
public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
	this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
}
/**
 * Add an additional compilation unit into the loop
 *  ->  build compilation unit declarations, their bindings and record their results.
 */
public void accept(ICompilationUnit sourceUnit, AccessRestriction accessRestriction) {
	// Switch the current policy and compilation result for this unit to the requested one.
	CompilationResult unitResult = new CompilationResult(sourceUnit, 1, 1, this.options.maxProblemsPerUnit);
	try {
		CompilationUnitDeclaration parsedUnit = basicParser().dietParse(sourceUnit, unitResult);
		this.lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
		this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
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
public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
	// case of SearchableEnvironment of an IJavaProject is used
	ISourceType sourceType = sourceTypes[0];
	while (sourceType.getEnclosingType() != null)
		sourceType = sourceType.getEnclosingType();
	if (sourceType instanceof SourceTypeElementInfo) {
		// get source
		SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) sourceType;
		IType type = elementInfo.getHandle();
		ICompilationUnit sourceUnit = (ICompilationUnit) type.getCompilationUnit();
		accept(sourceUnit, accessRestriction);
	} else {
		CompilationResult result = new CompilationResult(sourceType.getFileName(), 1, 1, 0);
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,
				SourceTypeConverter.FIELD_AND_METHOD // need field and methods
				| SourceTypeConverter.MEMBER_TYPE, // need member types
				// no need for field initialization
				this.lookupEnvironment.problemReporter,
				result);
		this.lookupEnvironment.buildTypeBindings(unit, accessRestriction);
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
		this.basicParser.reportOnlyOneSyntaxError = true;
	}
	return this.basicParser;
}
/**
 * Add the possibleMatch to the loop
 *  ->  build compilation unit declarations, their bindings and record their results.
 */
protected void parseAndBuildBindings(PossibleMatch possibleMatch, boolean mustResolve) {
	if (this.progressMonitor != null && this.progressMonitor.isCanceled())
		throw new OperationCanceledException();

	try {
		if (BasicSearchEngine.VERBOSE)
			System.out.println("Parsing " + possibleMatch.openable.toStringWithAncestors()); //$NON-NLS-1$

		this.parser.nodeSet = possibleMatch.nodeSet;
		CompilationResult unitResult = new CompilationResult(possibleMatch, 1, 1, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = this.parser.dietParse(possibleMatch, unitResult);
		if (parsedUnit != null) {
			if (mustResolve && !parsedUnit.isEmpty())
				this.lookupEnvironment.buildTypeBindings(parsedUnit, null /*no access restriction*/);

			// add the possibleMatch with its parsedUnit to matchesToProcess
			possibleMatch.parsedUnit = parsedUnit;
			int size = this.matchesToProcess.length;
			if (this.numberOfMatches == size)
				System.arraycopy(this.matchesToProcess, 0, this.matchesToProcess = new PossibleMatch[size == 0 ? 1 : size * 2], 0, this.numberOfMatches);
			this.matchesToProcess[this.numberOfMatches++] = possibleMatch;
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
protected BinaryTypeBinding cacheBinaryType(IType type, IBinaryType binaryType) throws JavaModelException {
	IType enclosingType = type.getDeclaringType();
	if (enclosingType != null)
		cacheBinaryType(enclosingType, null); // cache enclosing types first, so that binary type can be found in lookup enviroment
	if (binaryType == null) {
		ClassFile classFile = (ClassFile) type.getClassFile();
		try {
			binaryType = getBinaryInfo(classFile, classFile.getResource());
		} catch (CoreException e) {
			if (e instanceof JavaModelException) {
				throw (JavaModelException) e;
			} else {
				throw new JavaModelException(e);
			}
		}
	}
	BinaryTypeBinding binding = this.lookupEnvironment.cacheBinaryType(binaryType, null /*no access restriction*/);
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
				if (reader.isMember() && method.isConstructor() && !Flags.isStatic(reader.getModifiers())) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=48261
					firstIsSynthetic = true;
					argCount++;
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
		char[] typeName = CharOperation.concatWith(typeRef.getParameterizedTypeName(), '.');
//		for (int j = 0, dim = typeRef.dimensions(); j < dim; j++)
//			typeName = CharOperation.concat(typeName, new char[] {'[', ']'});
		parameterTypeSignatures[i] = Signature.createTypeSignature(typeName, false);
	}
	return type.getMethod(new String(method.selector), parameterTypeSignatures);
}
/**
 * Creates an IField from the given field declaration and type. 
 */
protected IJavaElement createHandle(FieldDeclaration fieldDeclaration, TypeDeclaration typeDeclaration, IJavaElement parent) {
	if (!(parent instanceof IType)) return parent;

	switch (fieldDeclaration.getKind()) {
		case AbstractVariableDeclaration.FIELD :
		case AbstractVariableDeclaration.ENUM_CONSTANT :
			return ((IType) parent).getField(new String(fieldDeclaration.name));
	}
	// find occurence count of the given initializer in its type declaration
	int occurrenceCount = 0;
	FieldDeclaration[] fields = typeDeclaration.fields;
	for (int i = 0, length = fields.length; i < length; i++) {
		if (fields[i].getKind() == AbstractVariableDeclaration.INITIALIZER) {
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
				cacheBinaryType(focusType, null);
			} catch (JavaModelException e) {
				return false;
			}
		} else {
			// cache all types in the focus' compilation unit (even secondary types)
			accept((ICompilationUnit) focusType.getCompilationUnit(), null /*TODO no access restriction*/);
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
/* (non-Javadoc)
 * Return info about last type argument of a parameterized type reference.
 * These info are made of concatenation of 2 int values which are respectively
 *  depth and end position of the last type argument.
 * For example, this method will return 0x300000020 for type ref List<List<List<String>>>
 * if end position of type reference "String" equals 32.
 */
private long findLastTypeArgumentInfo(TypeReference typeRef) {
	// Get last list of type arguments for parameterized qualified type reference
	TypeReference lastTypeArgument = typeRef;
	int depth = 0;
	while (true) {
		TypeReference[] lastTypeArguments = null;
		if (lastTypeArgument instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference pqtRef = (ParameterizedQualifiedTypeReference) lastTypeArgument;
			for (int i=pqtRef.typeArguments.length-1; i>=0 && lastTypeArguments==null; i--) {
				lastTypeArguments = pqtRef.typeArguments[i];
			}
		}
		// Get last type argument for single type reference of last list of argument of parameterized qualified type reference
		TypeReference last = null;
		if (lastTypeArgument instanceof ParameterizedSingleTypeReference || lastTypeArguments != null) {
			if (lastTypeArguments == null) {
				lastTypeArguments = ((ParameterizedSingleTypeReference)lastTypeArgument).typeArguments;
			}
			for (int i=lastTypeArguments.length-1; i>=0 && last==null; i++) {
				last = lastTypeArguments[i];
			}
		}
		if (last == null) break;
		depth++;
		lastTypeArgument = last;
	}
	// Current type reference is not parameterized. So, it is the last type argument
	return (((long) depth) << 32) + lastTypeArgument.sourceEnd;
}
protected IBinaryType getBinaryInfo(ClassFile classFile, IResource resource) throws CoreException {
	BinaryType binaryType = (BinaryType) classFile.getType();
	if (classFile.isOpen())
		return (IBinaryType) binaryType.getElementInfo(); // reuse the info from the java model cache

	// create a temporary info
	IBinaryType info;
	try {
		PackageFragment pkg = (PackageFragment) classFile.getParent();
		PackageFragmentRoot root = (PackageFragmentRoot) pkg.getParent();
		if (root.isArchive()) {
			// class file in a jar
			String classFileName = classFile.getElementName();
			String classFilePath = Util.concatWith(pkg.names, classFileName, '/');
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
		if (info == null) throw binaryType.newNotPresentException();
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
		CompilationResult compilationResult = unit.compilationResult;
		this.parser.scanner.setSource(compilationResult);

		if (this.parser.javadocParser.checkDocComment) {
			char[] contents = compilationResult.compilationUnit.getContents();
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
protected TypeBinding getType(Object typeKey, char[] typeName) {
	if (this.unitScope == null || typeName == null || typeName.length == 0) return null;
	// Try to get binding from cache
	Binding binding = (Binding) this.bindings.get(typeKey);
	if (binding != null) {
		if (binding instanceof TypeBinding && binding.isValidBinding())
			return (TypeBinding) binding;
		return null;
	}
	// Get binding from unit scope
	char[][] compoundName = CharOperation.splitOn('.', typeName);
	TypeBinding typeBinding = this.unitScope.getType(compoundName, compoundName.length);
	this.bindings.put(typeKey, typeBinding);
	return typeBinding.isValidBinding() ? typeBinding : null;
}
public MethodBinding getMethodBinding(IMethod method) {
	if (this.unitScope == null) return null;
	// Try to get binding from cache
	Binding binding = (Binding) this.bindings.get(method);
	if (binding != null) {
		if (binding instanceof MethodBinding && binding.isValidBinding())
			return (MethodBinding) binding;
		return null;
	}
	//	Get binding from unit scope
	String typeName = method.getDeclaringType().getElementName();
	TypeBinding declaringTypeBinding = getType(typeName, typeName.toCharArray());
	if (declaringTypeBinding != null) {
		if (declaringTypeBinding.isArrayType()) {
			declaringTypeBinding = declaringTypeBinding.leafComponentType();
		}
		if (!declaringTypeBinding.isBaseType()) {
			String[] parameterTypes = method.getParameterTypes();
			int paramTypeslength = parameterTypes.length;
			char[][] paramTypesChars = new char[paramTypeslength][];
			for (int i=0;  i<paramTypeslength; i++) {
				paramTypesChars[i] = Signature.toCharArray(parameterTypes[i].toCharArray());
			}
			ReferenceBinding referenceBinding = (ReferenceBinding) declaringTypeBinding;
			MethodBinding[] methods = referenceBinding.getMethods(method.getElementName().toCharArray());
			int methodsLength = methods.length;
			TypeVariableBinding[] refTypeVariables = referenceBinding.typeVariables();
			int typeVarLength = refTypeVariables==null ? 0 : refTypeVariables.length;
			for (int i=0; i<methodsLength; i++) {
				TypeBinding[] methodParameters = methods[i].parameters;
				int paramLength = methodParameters==null ? 0 : methodParameters.length;
				TypeVariableBinding[] methodTypeVariables = methods[i].typeVariables;
				int methTypeVarLength = methodTypeVariables==null ? 0 : methodTypeVariables.length;
				boolean found = paramLength == paramTypeslength;
				if (found) {
					for (int p=0; found && p<paramLength; p++) {
						if (CharOperation.equals(methodParameters[p].erasure().shortReadableName(), paramTypesChars[p])) {
							// param erasure match
						} else {
							// type variable
							boolean foundVar = true;
							for (int v=0; foundVar && v<typeVarLength; v++) {
								if (!CharOperation.equals(refTypeVariables[v].sourceName, paramTypesChars[p])) {
									foundVar = false;
								}
							}
							if (!foundVar) {
								foundVar = true;
								for (int v=0; foundVar && v<methTypeVarLength; v++) {
									if (!CharOperation.equals(methodTypeVariables[v].sourceName, paramTypesChars[p])) {
										foundVar = false;
									}
								}
								if (!foundVar) found = false;
							}
						}
					}
				}
				if (found) {
					this.bindings.put(method, methods[i]);
					return methods[i];
				}
			}
//			methodBinding = referenceBinding.getExactMethod(method.getElementName().toCharArray(), parameters);
//			this.bindings.put(method, methodBinding);
//			return methodBinding;
		}
	}
	this.bindings.put(method, new ProblemMethodBinding(method.getElementName().toCharArray(), null, ProblemReasons.NotFound));
	return null;
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
	// clean up name environment only if there are several possible match as it is reused
	// when only one possible match (bug 58581)
	if (this.nameEnvironment != null && possibleMatchSize != 1)
		this.nameEnvironment.cleanup();

	SearchableEnvironment searchableEnvironment = project.newSearchableNameEnvironment(this.workingCopies);
	
	// if only one possible match, a file name environment costs too much,
	// so use the existing searchable  environment which will populate the java model
	// only for this possible match and its required types.
	this.nameEnvironment = possibleMatchSize == 1
		? (INameEnvironment) searchableEnvironment
		: (INameEnvironment) new JavaSearchNameEnvironment(project, this.workingCopies);

	// create lookup environment
	Map map = project.getOptions(true);
	map.put(CompilerOptions.OPTION_TaskTags, ""); //$NON-NLS-1$
	this.options = new CompilerOptions(map);
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
	boolean mustResolve = ((InternalSearchPattern)this.pattern).mustResolve;
	boolean bindingsWereCreated = mustResolve;
	try {
		for (int i = start, maxUnits = start + length; i < maxUnits; i++) {
			PossibleMatch possibleMatch = possibleMatches[i];
			try {
				parseAndBuildBindings(possibleMatch, mustResolve);
				if (!mustResolve) {
					if (this.progressMonitor != null) {
						this.progressWorked++;
						if ((this.progressWorked%this.progressStep)==0) this.progressMonitor.worked(this.progressStep);
					}
					process(possibleMatch, bindingsWereCreated);
				}
			} finally {
				if (!mustResolve)
					possibleMatch.cleanUp();
			}
		}
		if (mustResolve)
			this.lookupEnvironment.completeTypeBindings();

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

	if (!mustResolve) {
		return;
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
			if (this.progressMonitor != null) {
				this.progressWorked++;
				if ((this.progressWorked%this.progressStep)==0) this.progressMonitor.worked(this.progressStep);
			}
			if (this.options.verbose)
				System.out.println(Util.bind("compilation.done", //$NON-NLS-1$
					new String[] {
						String.valueOf(i + 1),
						String.valueOf(this.numberOfMatches),
						new String(possibleMatch.parsedUnit.getFileName())}));
			// cleanup compilation unit result
			possibleMatch.cleanUp();
		}
	}
}
/**
 * Locate the matches amongst the possible matches.
 */
protected void locateMatches(JavaProject javaProject, PossibleMatchSet matchSet, int expected) throws CoreException {
	PossibleMatch[] possibleMatches = matchSet.getPossibleMatches(javaProject.getPackageFragmentRoots());
	int length = possibleMatches.length;
	// increase progress from duplicate matches not stored in matchSet while adding...
	if (this.progressMonitor != null && expected>length) {
		this.progressWorked += expected-length;
		this.progressMonitor.worked( expected-length);
	}
	// locate matches (processed matches are limited to avoid problem while using VM default memory heap size)
	for (int index = 0; index < length;) {
		int max = Math.min(MAX_AT_ONCE, length - index);
		locateMatches(javaProject, possibleMatches, index, max);
		index += max;
	}
}
/**
 * Locate the matches in the given files and report them using the search requestor. 
 */
public void locateMatches(SearchDocument[] searchDocuments) throws CoreException {
	int docsLength = searchDocuments.length;
	if (BasicSearchEngine.VERBOSE) {
		System.out.println("Locating matches in documents ["); //$NON-NLS-1$
		for (int i = 0; i < docsLength; i++)
			System.out.println("\t" + searchDocuments[i]); //$NON-NLS-1$
		System.out.println("]"); //$NON-NLS-1$
	}

	// init infos for progress increasing
	int n = docsLength<1000 ? Math.min(Math.max(docsLength/200+1, 2),4) : 5 *(docsLength/1000);
	this.progressStep = docsLength < n ? 1 : docsLength / n; // step should not be 0
	this.progressWorked = 0;

	// extract working copies
	ArrayList copies = new ArrayList();
	for (int i = 0; i < docsLength; i++) {
		SearchDocument document = searchDocuments[i];
		if (document instanceof WorkingCopyDocument) {
			copies.add(((WorkingCopyDocument)document).workingCopy);
		}
	}
	int copiesLength = copies.size();
	this.workingCopies = new org.eclipse.jdt.core.ICompilationUnit[copiesLength];
	copies.toArray(this.workingCopies);

	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	this.bindings = new SimpleLookupTable();
	try {
		// optimize access to zip files during search operation
		manager.cacheZipFiles();

		// initialize handle factory (used as a cache of handles so as to optimize space)
		if (this.handleFactory == null)
			this.handleFactory = new HandleFactory();

		if (this.progressMonitor != null) {
			this.progressMonitor.beginTask("", searchDocuments.length); //$NON-NLS-1$
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
		int displayed = 0; // progress worked displayed
		for (int i = 0; i < docsLength; i++) {
			if (this.progressMonitor != null && this.progressMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			// skip duplicate paths
			SearchDocument searchDocument = searchDocuments[i];
			String pathString = searchDocument.getPath();
			if (i > 0 && pathString.equals(searchDocuments[i - 1].getPath())) {
				if (this.progressMonitor != null) {
					this.progressWorked++;
					if ((this.progressWorked%this.progressStep)==0) this.progressMonitor.worked(this.progressStep);
				}
				displayed++;
				continue;
			}

			Openable openable;
			org.eclipse.jdt.core.ICompilationUnit workingCopy = null;
			if (searchDocument instanceof WorkingCopyDocument) {
				workingCopy = ((WorkingCopyDocument)searchDocument).workingCopy;
				openable = (Openable) workingCopy;
			} else {
				openable = this.handleFactory.createOpenable(pathString, this.scope);
				if (openable == null) {
					if (this.progressMonitor != null) {
						this.progressWorked++;
						if ((this.progressWorked%this.progressStep)==0) this.progressMonitor.worked(this.progressStep);
					}
					displayed++;
					continue; // match is outside classpath
				}
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
						locateMatches(previousJavaProject, matchSet, i-displayed);
						displayed = i;
					} catch (JavaModelException e) {
						// problem with classpath in this project -> skip it
					}
					matchSet.reset();
				}
				previousJavaProject = javaProject;
			}
			matchSet.add(new PossibleMatch(this, resource, openable, searchDocument));
		}

		// last project
		if (previousJavaProject != null) {
			try {
				locateMatches(previousJavaProject, matchSet, docsLength-displayed);
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
		this.bindings = null;
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
			SearchDocument document = participant.getDocument(focus.getPath().toString());
			this.currentPossibleMatch = new PossibleMatch(this, focus.getResource(), null, document);
			if (encloses(focus)) {
				SearchMatch match = newDeclarationMatch(focus.getAncestor(IJavaElement.PACKAGE_FRAGMENT), SearchMatch.A_ACCURATE, -1, -1);
				report(match);
			}
			return;
		}
		PackageDeclarationPattern pkgPattern = (PackageDeclarationPattern) searchPattern;
		IJavaProject[] projects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject javaProject = projects[i];
			IPackageFragmentRoot[] roots = null;
			try {
				roots = javaProject.getPackageFragmentRoots();
			} catch (JavaModelException e) {
				// java project doesn't exist -> continue with next project (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=75561)
				continue;
			}
			for (int j = 0, rootsLength = roots.length; j < rootsLength; j++) {
				IJavaElement[] pkgs = null;
				try {
					pkgs = roots[j].getChildren();
				} catch (JavaModelException e) {
					// pkg fragment root doesn't exist -> continue with next root (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=75561)
					continue;
				}
				for (int k = 0, pksLength = pkgs.length; k < pksLength; k++) {
					IPackageFragment pkg = (IPackageFragment) pkgs[k];
					IJavaElement[] children = null;
					try {
						children = pkg.getChildren();
					} catch (JavaModelException e) {
						// package doesn't exist -> continue with next package (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=75561)
						continue;
					}
					if (children.length > 0 
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
	int acceptFlag = 0;
	if (typeBinding.isAnnotationType()) {
		acceptFlag = NameLookup.ACCEPT_ANNOTATIONS;
	} else if (typeBinding.isEnum()) {
		acceptFlag = NameLookup.ACCEPT_ENUMS;
	} else if (typeBinding.isInterface()) {
		acceptFlag = NameLookup.ACCEPT_INTERFACES;
	} else if (typeBinding.isClass()) {
		acceptFlag = NameLookup.ACCEPT_CLASSES;
	}
	for (int i = 0, length = pkgs == null ? 0 : pkgs.length; i < length; i++) {
		IType type = this.nameLookup.findType(typeName, pkgs[i],  false,  acceptFlag);
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
		case IJavaElement.PACKAGE_DECLARATION:
			return new PackageDeclarationMatch(element, accuracy, offset, length, participant, resource);
		case IJavaElement.TYPE_PARAMETER:
			return new TypeParameterDeclarationMatch(element, accuracy, offset, length, participant, resource);
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
		boolean isConstructor,
		boolean isSynthetic,
		ASTNode reference) {
	SearchParticipant participant = getParticipant(); 
	IResource resource = this.currentPossibleMatch.resource;
	boolean insideDocComment = (reference.bits & ASTNode.InsideJavadoc) != 0;
	return new MethodReferenceMatch(enclosingElement, accuracy, offset, length, isConstructor, isSynthetic, insideDocComment, participant, resource);
}

public SearchMatch newPackageReferenceMatch(
		IJavaElement enclosingElement,
		int accuracy,
		int offset,  
		int length,
		ASTNode reference) {
	SearchParticipant participant = getParticipant(); 
	IResource resource = this.currentPossibleMatch.resource;
	boolean insideDocComment = (reference.bits & ASTNode.InsideJavadoc) != 0;
	return new PackageReferenceMatch(enclosingElement, accuracy, offset, length, insideDocComment, participant, resource);
}

public SearchMatch newTypeParameterReferenceMatch(
		IJavaElement enclosingElement,
		int accuracy,
		int offset,  
		int length,
		ASTNode reference) {
	int bits = reference.bits;
	boolean insideDocComment = (bits & ASTNode.InsideJavadoc) != 0;
	SearchParticipant participant = getParticipant(); 
	IResource resource = this.currentPossibleMatch.resource;
	return new TypeParameterReferenceMatch(enclosingElement, accuracy, offset, length, insideDocComment, participant, resource);
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

public SearchMatch newTypeReferenceMatch(
		IJavaElement enclosingElement,
		int accuracy,
		ASTNode reference) {
	return newTypeReferenceMatch(enclosingElement, accuracy, reference.sourceStart, reference.sourceEnd-reference.sourceStart+1, reference);
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
			if (BasicSearchEngine.VERBOSE)
				System.out.println("Resolving " + this.currentPossibleMatch.openable.toStringWithAncestors()); //$NON-NLS-1$

			reduceParseTree(unit);

			if (unit.scope != null) {
				// fault in fields & methods
				unit.scope.faultInTypes();
			}
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
	if (BasicSearchEngine.VERBOSE) {
		start = System.currentTimeMillis();
		System.out.println("Reporting match"); //$NON-NLS-1$
		System.out.println("\tResource: " + match.getResource()); //$NON-NLS-2$//$NON-NLS-1$
		System.out.println("\tPositions: [offset=" + match.getOffset() + ", length=" + match.getLength() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (this.parser != null && match.getOffset() > 0 && match.getLength() > 0 && !(match.getElement() instanceof BinaryMember)) {
			String selection = new String(this.parser.scanner.source, match.getOffset(), match.getLength());
			System.out.println("\tSelection: -->" + selection + "<--"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		System.out.println("\tJava element: " + ((JavaElement)match.getElement()).toStringWithAncestors()); //$NON-NLS-1$
		System.out.println(match.getAccuracy() == SearchMatch.A_ACCURATE
			? "\tAccuracy: EXACT_MATCH" //$NON-NLS-1$
			: "\tAccuracy: POTENTIAL_MATCH"); //$NON-NLS-1$
		System.out.print("\tRule: "); //$NON-NLS-1$
		if (match.isExact()) {
			System.out.println("EXACT"); //$NON-NLS-1$
		} else if (match.isEquivalent()) {
			System.out.println("EQUIVALENT"); //$NON-NLS-1$
		} else if (match.isErasure()) {
			System.out.println("ERASURE"); //$NON-NLS-1$
		} else {
			System.out.println("INVALID RULE"); //$NON-NLS-1$
		}
		System.out.println("\tRaw: "+match.isRaw()); //$NON-NLS-1$
	}
	this.requestor.acceptSearchMatch(match);
	if (BasicSearchEngine.VERBOSE)
		this.resultCollectorTime += System.currentTimeMillis()-start;
}
/**
 * Finds the accurate positions of the sequence of tokens given by qualifiedName
 * in the source and reports a reference to this this qualified name
 * to the search requestor.
 */
protected void reportAccurateTypeReference(SearchMatch match, ASTNode typeRef, char[] name) throws CoreException {
	if (match.getRule() == 0) return;
	if (!encloses((IJavaElement)match.getElement())) return;
	
	// Compute source positions of the qualified reference 
	int sourceStart = typeRef.sourceStart;
	int sourceEnd = typeRef.sourceEnd;
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
			match.setOffset(currentPosition);
			match.setLength(length);
			report(match);
			return;
		}
	} while (token != TerminalTokens.TokenNameEOF);

	//	Report match
	match.setOffset(sourceStart);
	match.setLength(sourceEnd-sourceStart+1);
	report(match);
}

/**
 * Finds the accurate positions of the sequence of tokens given by qualifiedName
 * in the source and reports a reference to this parameterized type name
 * to the search requestor.
 * @since 3.1
 */
protected void reportAccurateParameterizedMethodReference(SearchMatch match, ASTNode statement, TypeReference[] typeArguments) throws CoreException {
	if (match.getRule() == 0) return;
	if (!encloses((IJavaElement)match.getElement())) return;

	// If there's type arguments, look for end (ie. char '>') of last one.
	int start = match.getOffset();
	if (typeArguments != null && typeArguments.length > 0) {
		boolean isErasureMatch= (pattern instanceof OrPattern) ? ((OrPattern)pattern).isErasureMatch() : ((JavaSearchPattern)pattern).isErasureMatch();
		if (!isErasureMatch) {
			
			// Initialize scanner
			Scanner scanner = this.parser.scanner;
			char[] source = this.currentPossibleMatch.getContents();
			scanner.setSource(source);

			// Search previous opening '<'
			start = typeArguments[0].sourceStart;
			int end = statement.sourceEnd;
			scanner.resetTo(start, end);
			int lineStart = start;
			try {
				linesUp: while (true) {
					while (scanner.source[scanner.currentPosition] != '\n') {
						scanner.currentPosition--;
						if (scanner.currentPosition == 0) break linesUp;
					}
					lineStart = scanner.currentPosition+1;
					scanner.resetTo(lineStart, end);
					while (!scanner.atEnd()) {
						if (scanner.getNextToken() == TerminalTokens.TokenNameLESS) {
							start = scanner.getCurrentTokenStartPosition();
							break linesUp;
						}
					}
					end = lineStart - 2;
					scanner.currentPosition = end;
				}
			}
			catch (InvalidInputException ex) {
				// give up
			}
	 	}
	}
	
	// Report match
	match.setOffset(start);
	match.setLength(statement.sourceEnd-start+1);
	report(match);
}

/**
 * Finds the accurate positions of the sequence of tokens given by qualifiedName
 * in the source and reports a reference to this parameterized type name
 * to the search requestor.
 * @since 3.1
 */
protected void reportAccurateParameterizedTypeReference(SearchMatch match, TypeReference typeRef, int index, TypeReference[] typeArguments) throws CoreException {
	if (match.getRule() == 0) return;
	if (!encloses((IJavaElement)match.getElement())) return;

	// If there's type arguments, look for end (ie. char '>') of last one.
	int end = typeRef.sourceEnd;
	if (typeArguments != null) {
		// Initialize scanner
		Scanner scanner = this.parser.scanner;
		char[] source = this.currentPossibleMatch.getContents();
		scanner.setSource(source);

		boolean shouldMatchErasure= (pattern instanceof OrPattern) ? ((OrPattern)pattern).isErasureMatch() : ((JavaSearchPattern)pattern).isErasureMatch();
		boolean hasSignatures = (pattern instanceof OrPattern) ? ((OrPattern)pattern).hasSignatures() : ((JavaSearchPattern)pattern).hasSignatures();
		if (shouldMatchErasure || !hasSignatures) {
			// if pattern is erasure only, then select the end of the reference
			if (typeRef instanceof QualifiedTypeReference && index >= 0) {
				long[] positions = ((QualifiedTypeReference) typeRef).sourcePositions;
				end = (int) positions[index];
			} else if (typeRef instanceof ArrayTypeReference) {
				end = ((ArrayTypeReference) typeRef).originalSourceEnd;
			}
		}  else {
			// Set scanner position at end of last type argument
			scanner.resetTo(end, source.length-1);
			int depth = 0;
			for (int i=typeArguments.length-1; i>=0; i--) {
				if (typeArguments[i] != null) {
					long lastTypeArgInfo = findLastTypeArgumentInfo(typeArguments[i]);
					depth = (int) (lastTypeArgInfo >>> 32)+1;
					scanner.resetTo(((int)lastTypeArgInfo)+1, scanner.eofPosition-1);
					break;
				}
			}
	
			// Now, scan to search next closing '>'
			while (depth-- > 0) {
				while (!scanner.atEnd()) {
					if (scanner.getNextChar() == '>') {
						end = scanner.currentPosition - 1;
						break;
					}
				}
			}
	 	}
	}
	
	// Report match
	match.setLength(end-match.getOffset()+1);
	report(match);
}
/**
 * Finds the accurate positions of each valid token in the source and
 * reports a reference to this token to the search requestor.
 * A token is valid if it has an accuracy which is not -1.
 */
protected void reportAccurateFieldReference(SearchMatch[] matches, QualifiedNameReference qNameRef) throws CoreException {
	int matchesLength = matches == null ? 0 : matches.length;

	int sourceStart = qNameRef.sourceStart;
	int sourceEnd = qNameRef.sourceEnd;
	char[][] tokens = qNameRef.tokens;
	
	// compute source positions of the qualified reference 
	Scanner scanner = this.parser.scanner;
	scanner.setSource(this.currentPossibleMatch.getContents());
	scanner.resetTo(sourceStart, sourceEnd);
	int sourceLength = sourceEnd-sourceStart+1;

	int refSourceStart = -1, refSourceEnd = -1;
	int length = tokens.length;
	int token = -1;
	int previousValid = -1;
	int i = 0;
	int index = 0;
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
		SearchMatch match = matches[index];
		if (match != null && match.getRule() != 0) {
			if (!encloses((IJavaElement)match.getElement())) return;
			// accept reference
			if (refSourceStart != -1) {
				match.setOffset(refSourceStart);
				match.setLength(refSourceEnd-refSourceStart+1);
				report(match);
			} else {
				match.setOffset(sourceStart);
				match.setLength(sourceLength);
				report(match);
			}
			i = 0;
		}
		refSourceStart = -1;
		previousValid = -1;
		if (index < matchesLength - 1) {
			index++;
		}
	} while (token != TerminalTokens.TokenNameEOF);

}
protected void reportBinaryMemberDeclaration(IResource resource, IMember binaryMember, IBinaryType info, int accuracy) throws CoreException {
	ClassFile classFile = (ClassFile) binaryMember.getClassFile();
	ISourceRange range = classFile.isOpen() ? binaryMember.getNameRange() : SourceMapper.fgUnknownRange;
	if (range.getOffset() == -1) {
		BinaryType type = (BinaryType) classFile.getType();
		String sourceFileName = type.sourceFileName(info);
		if (sourceFileName != null) {
			SourceMapper mapper = classFile.getSourceMapper();
			if (mapper != null) {
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
				SearchMatch match = this.patternLocator.newDeclarationMatch(method, enclosingElement, accuracy, length, this);
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

	// report annotations
	if (method.annotations != null) {
		for (int i=0, al=method.annotations.length; i<al; i++) {
			TypeReference typeRef = method.annotations[i].type;
			Integer level = (Integer) nodeSet.matchingNodes.removeKey(typeRef);
			if (level != null) {
				if (enclosingElement == null)
					enclosingElement = createHandle(method, parent);
				this.patternLocator.matchReportReference(typeRef, enclosingElement, level.intValue(), this);
			}
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
		this.unitScope= unit.scope.compilationUnitScope();
		// move the possible matching nodes that exactly match the search pattern to the matching nodes set
		Object[] nodes = nodeSet.possibleMatchingNodesSet.values;
		for (int i = 0, l = nodes.length; i < l; i++) {
			ASTNode node = (ASTNode) nodes[i];
			if (node == null) continue;
			if (node instanceof ImportReference) {
				// special case for import refs: they don't know their binding
				// import ref cannot be in the hierarchy of a type
				if (this.hierarchyResolver != null) continue;

				ImportReference importRef = (ImportReference) node;
				Binding binding = importRef.onDemand
					? unitScope.getImport(CharOperation.subarray(importRef.tokens, 0, importRef.tokens.length), true, importRef.isStatic())
					: unitScope.getImport(importRef.tokens, false, importRef.isStatic());
				this.patternLocator.matchLevelAndReportImportRef(importRef, binding, this);
			}
			nodeSet.addMatch(node, this.patternLocator.resolveLevel(node));
		}
		nodeSet.possibleMatchingNodesSet = new SimpleSet(3);
	} else {
		this.unitScope = null;
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

	// report annotations
	if (field.annotations != null) {
		for (int i=0, al=field.annotations.length; i<al; i++) {
			TypeReference typeRef = field.annotations[i].type;
			Integer level = (Integer) nodeSet.matchingNodes.removeKey(typeRef);
			if (level != null) {
				if (enclosingElement == null)
					enclosingElement = createHandle(field, type, parent);
				this.patternLocator.matchReportReference(typeRef, enclosingElement, level.intValue(), this);
			}
		}
	}

	if (typeInHierarchy) {
		// limit scan to end part position for multiple fields declaration (see bug 73112)
		int end = field.endPart2Position==0 ? field.declarationSourceEnd : field.endPart2Position;
		ASTNode[] nodes = nodeSet.matchingNodes(field.declarationSourceStart, end);
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
		SearchMatch match = this.patternLocator.newDeclarationMatch(type, enclosingElement, accuracy, type.sourceEnd-offset+1, this);
		report(match);
	}

	boolean matchedClassContainer = (this.matchContainer & PatternLocator.CLASS_CONTAINER) != 0;
	
	// report the type parameters
	if (type.typeParameters != null) {
		for (int i=0, l=type.typeParameters.length; i<l; i++) {
			TypeParameter typeParameter = type.typeParameters[i];
			if (typeParameter != null) {
				Integer level = (Integer) nodeSet.matchingNodes.removeKey(typeParameter);
				if (level != null && matchedClassContainer) {
					if (level.intValue() > -1 && encloses(enclosingElement)) {
						int offset = typeParameter.sourceStart;
						SearchMatch match = this.patternLocator.newDeclarationMatch(typeParameter, enclosingElement, level.intValue(), typeParameter.sourceEnd-offset+1, this);
						report(match);
					}
				}
				if (typeParameter.type != null) {
					level = (Integer) nodeSet.matchingNodes.removeKey(typeParameter.type);
					if (level != null && matchedClassContainer) {
						this.patternLocator.matchReportReference(typeParameter.type, enclosingElement, level.intValue(), this);
					}
				}
				if (typeParameter.bounds != null) {
					for (int j=0, b=typeParameter.bounds.length; j<b; j++) {
						level = (Integer) nodeSet.matchingNodes.removeKey(typeParameter.bounds[j]);
						if (level != null && matchedClassContainer) {
							this.patternLocator.matchReportReference(typeParameter.bounds[j], enclosingElement, level.intValue(), this);
						}
					}
				}
			}
		}
	}

	// report annotations
	if (type.annotations != null) {
		for (int i=0, al=type.annotations.length; i<al; i++) {
			TypeReference typeRef = type.annotations[i].type;
			Integer level = (Integer) nodeSet.matchingNodes.removeKey(typeRef);
			if (level != null && matchedClassContainer) {
				this.patternLocator.matchReportReference(typeRef, enclosingElement, level.intValue(), this);
			}
		}
	}

	// report references in javadoc
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
			reportMatchingSuper(superClass, enclosingElement, nodeSet, matchedClassContainer);
		}
		TypeReference[] superInterfaces = type.superInterfaces;
		if (superInterfaces != null) {
			for (int i = 0, l = superInterfaces.length; i < l; i++) {
				reportMatchingSuper(superInterfaces[i], enclosingElement, nodeSet, matchedClassContainer);
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
protected void reportMatchingSuper(TypeReference superReference, IJavaElement enclosingElement, MatchingNodeSet nodeSet, boolean matchedClassContainer) throws CoreException {
	ASTNode[] nodes = null;
	if (superReference instanceof ParameterizedSingleTypeReference || superReference instanceof ParameterizedQualifiedTypeReference) {
		long lastTypeArgumentInfo = findLastTypeArgumentInfo(superReference);
		nodes = nodeSet.matchingNodes(superReference.sourceStart, (int)lastTypeArgumentInfo);
	}
	if (nodes != null) {
		if ((this.matchContainer & PatternLocator.CLASS_CONTAINER) == 0) {
			for (int i = 0, l = nodes.length; i < l; i++)
				nodeSet.matchingNodes.removeKey(nodes[i]);
		} else {
			if (encloses(enclosingElement))
				for (int i = 0, l = nodes.length; i < l; i++) {
					ASTNode node = nodes[i];
					Integer level = (Integer) nodeSet.matchingNodes.removeKey(node);
					this.patternLocator.matchReportReference(node, enclosingElement, level.intValue(), this);
				}
		}
	} else {
		Integer level = (Integer) nodeSet.matchingNodes.removeKey(superReference);
		if (level != null && matchedClassContainer)
			this.patternLocator.matchReportReference(superReference, enclosingElement, level.intValue(), this);
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
