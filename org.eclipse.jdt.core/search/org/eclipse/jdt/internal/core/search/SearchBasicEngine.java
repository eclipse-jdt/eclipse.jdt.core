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
package org.eclipse.jdt.internal.core.search;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.search.matching.*;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Search basic engine. Public search engine (see {@link org.eclipse.jdt.core.search.SearchEngine}
 * for detailed comment), now uses basic engine functionalities.
 * Note that serch basic engine does not implement deprecated functionalities...
 */
public class SearchBasicEngine {

	/*
	 * A default parser to parse non-reconciled working copies
	 */
	private Parser parser;
	private CompilerOptions compilerOptions;
		
	/*
	 * A list of working copies that take precedence over their original 
	 * compilation units.
	 */
	private ICompilationUnit[] workingCopies;
	
	/*
	 * A working copy owner whose working copies will take precedent over 
	 * their original compilation units.
	 */
	private WorkingCopyOwner workingCopyOwner;

	/**
	 * For tracing purpose.
	 */	
	public static boolean VERBOSE = false;	

	/*
	 * Creates a new search basic engine.
	 */
	public SearchBasicEngine() {
		// will use working copies of PRIMARY owner
	}
	
	/**
	 * @see org.eclipse.jdt.core.search.SearchEngine#SearchEngine(ICompilationUnit[]) for detailed comment.
	 */
	public SearchBasicEngine(ICompilationUnit[] workingCopies) {
		this.workingCopies = workingCopies;
	}
	
	/**
	 * @see org.eclipse.jdt.core.search.SearchEngine#SearchEngine(WorkingCopyOwner) for detailed comment.
	 */
	public SearchBasicEngine(WorkingCopyOwner workingCopyOwner) {
		this.workingCopyOwner = workingCopyOwner;
	}

	/**
	 * @see org.eclipse.jdt.core.search.SearchEngine#createHierarchyScope(IType) for detailed comment.
	 */
	public static IJavaSearchScope createHierarchyScope(IType type) throws JavaModelException {
		return createHierarchyScope(type, DefaultWorkingCopyOwner.PRIMARY);
	}
	
	/**
	 * @see org.eclipse.jdt.core.search.SearchEngine#createHierarchyScope(IType,WorkingCopyOwner) for detailed comment.
	 */
	public static IJavaSearchScope createHierarchyScope(IType type, WorkingCopyOwner owner) throws JavaModelException {
		return new HierarchyScope(type, owner);
	}

	/**
	 * @see org.eclipse.jdt.core.search.SearchEngine#createJavaSearchScope(IJavaElement[]) for detailed comment.
	 */
	public static IJavaSearchScope createJavaSearchScope(IJavaElement[] elements) {
		return createJavaSearchScope(elements, true);
	}

	/**
	 * @see org.eclipse.jdt.core.search.SearchEngine#createJavaSearchScope(IJavaElement[], boolean) for detailed comment.
	 */
	public static IJavaSearchScope createJavaSearchScope(IJavaElement[] elements, boolean includeReferencedProjects) {
		int includeMask = IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SYSTEM_LIBRARIES;
		if (includeReferencedProjects) {
			includeMask |= IJavaSearchScope.REFERENCED_PROJECTS;
		}
		return createJavaSearchScope(elements, includeMask);
	}

	/**
	 * @see org.eclipse.jdt.core.search.SearchEngine#createJavaSearchScope(IJavaElement[], int) for detailed comment.
	 */
	public static IJavaSearchScope createJavaSearchScope(IJavaElement[] elements, int includeMask) {
		JavaSearchScope scope = new JavaSearchScope();
		HashSet visitedProjects = new HashSet(2);
		try {
			for (int i = 0, length = elements.length; i < length; i++) {
				IJavaElement element = elements[i];
				if (element != null) {
					if (element instanceof JavaProject) {
						scope.add((JavaProject)element, includeMask, visitedProjects);
					} else {
						scope.add(element);
					}
				}
			}
		} catch (JavaModelException e) {
			// ignore
		}
		return scope;
	}
	
	/**
	 * Returns a Java search scope with the workspace as the only limit.
	 *
	 * @return a new workspace scope
	 */
	public static IJavaSearchScope createWorkspaceScope() {
		return new JavaWorkspaceScope();
	}
	
	/**
	 * Searches for matches to a given query. Search queries can be created using helper
	 * methods (from a String pattern or a Java element) and encapsulate the description of what is
	 * being searched (for example, search method declarations in a case sensitive way).
	 *
	 * @param scope the search result has to be limited to the given scope
	 * @param requestor a callback object to which each match is reported
	 */
	public void findMatches(SearchPattern pattern, SearchParticipant[] participants, IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	
		/* initialize progress monitor */
		if (monitor != null)
			monitor.beginTask(Util.bind("engine.searching"), 100); //$NON-NLS-1$
		if (VERBOSE)
			System.out.println("Searching for " + this + " in " + scope); //$NON-NLS-1$//$NON-NLS-2$
	
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		try {
			requestor.beginReporting();
			for (int i = 0, l = participants == null ? 0 : participants.length; i < l; i++) {
				if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	
				SearchParticipant participant = participants[i];
				SubProgressMonitor subMonitor= monitor==null ? null : new SubProgressMonitor(monitor, 1000);
				if (subMonitor != null) subMonitor.beginTask("", 1000); //$NON-NLS-1$
				try {
					if (subMonitor != null) subMonitor.subTask(Util.bind("engine.searching.indexing", participant.getDescription())); //$NON-NLS-1$
					participant.beginSearching();
					requestor.enterParticipant(participant);
					PathCollector pathCollector = new PathCollector();
					indexManager.performConcurrentJob(
						new PatternSearchJob(pattern, participant, scope, pathCollector),
						IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
						subMonitor);
					if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	
					// locate index matches if any (note that all search matches could have been issued during index querying)
					if (subMonitor != null) subMonitor.subTask(Util.bind("engine.searching.matching", participant.getDescription())); //$NON-NLS-1$
					String[] indexMatchPaths = pathCollector.getPaths();
					pathCollector = null; // release
					int indexMatchLength = indexMatchPaths == null ? 0 : indexMatchPaths.length;
					SearchDocument[] indexMatches = new SearchDocument[indexMatchLength];
					for (int j = 0; j < indexMatchLength; j++)
						indexMatches[j] = participant.getDocument(indexMatchPaths[j]);
					SearchDocument[] matches = MatchLocator.addWorkingCopies(pattern, indexMatches, getWorkingCopies(), participant);
					participant.locateMatches(matches, pattern, scope, requestor, subMonitor);
				} finally {		
					requestor.exitParticipant(participant);
					participant.doneSearching();
				}
			}
		} finally {
			requestor.endReporting();
			if (monitor != null)
				monitor.done();
		}
	}
	/**
	 * Returns a new default Java search participant.
	 * 
	 * @return a new default Java search participant
	 * @since 3.0
	 */
	public static SearchParticipant getDefaultSearchParticipant() {
		return new JavaSearchParticipant();
	}

	private Parser getParser() {
		if (this.parser == null) {
			this.compilerOptions = new CompilerOptions(JavaCore.getOptions());
			ProblemReporter problemReporter =
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					this.compilerOptions,
					new DefaultProblemFactory());
			this.parser = new Parser(problemReporter, true);
		}
		return this.parser;
	}
	
	/**
	 * Returns the underlying resource of the given element.
	 * @param element an IJavaElement
	 * @return an IResource
	 */
	private IResource getResource(IJavaElement element) {
		if (element instanceof IMember) {
			ICompilationUnit cu = ((IMember)element).getCompilationUnit();
			if (cu != null) {
				return cu.getResource();
			} 
		} 
		return element.getResource();
	}
	
	/*
	 * Returns the list of working copies used by this search engine.
	 * Returns null if none.
	 */
	private ICompilationUnit[] getWorkingCopies() {
		ICompilationUnit[] copies;
		if (this.workingCopies != null) {
			if (this.workingCopyOwner == null) {
				copies = JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false/*don't add primary WCs a second time*/);
				if (copies == null) {
					copies = this.workingCopies;
				} else {
					HashMap pathToCUs = new HashMap();
					for (int i = 0, length = copies.length; i < length; i++) {
						ICompilationUnit unit = copies[i];
						pathToCUs.put(unit.getPath(), unit);
					}
					for (int i = 0, length = this.workingCopies.length; i < length; i++) {
						ICompilationUnit unit = this.workingCopies[i];
						pathToCUs.put(unit.getPath(), unit);
					}
					int length = pathToCUs.size();
					copies = new ICompilationUnit[length];
					pathToCUs.values().toArray(copies);
				}
			} else {
				copies = this.workingCopies;
			}
		} else if (this.workingCopyOwner != null) {
			copies = JavaModelManager.getJavaModelManager().getWorkingCopies(this.workingCopyOwner, true/*add primary WCs*/);
		} else {
			copies = JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false/*don't add primary WCs a second time*/);
		}
		if (copies == null) return null;
		
		// filter out primary working copies that are saved
		ICompilationUnit[] result = null;
		int length = copies.length;
		int index = 0;
		for (int i = 0; i < length; i++) {
			CompilationUnit copy = (CompilationUnit)copies[i];
			try {
				if (!copy.isPrimary()
						|| copy.hasUnsavedChanges()
						|| copy.hasResourceChanged()) {
					if (result == null) {
						result = new ICompilationUnit[length];
					}
					result[index++] = copy;
				}
			}  catch (JavaModelException e) {
				// copy doesn't exist: ignore
			}
		}
		if (index != length && result != null) {
			System.arraycopy(result, 0, result = new ICompilationUnit[index], 0, index);
		}
		return result;
	}
	
	/**
	 * Returns the list of working copies used to do the search on the given Java element.
	 * @param element an IJavaElement
	 * @return an array of ICompilationUnit
	 */
	private ICompilationUnit[] getWorkingCopies(IJavaElement element) {
		if (element instanceof IMember) {
			ICompilationUnit cu = ((IMember)element).getCompilationUnit();
			if (cu != null && cu.isWorkingCopy()) {
				ICompilationUnit[] copies = getWorkingCopies();
				int length = copies == null ? 0 : copies.length;
				if (length > 0) {
					ICompilationUnit[] newWorkingCopies = new ICompilationUnit[length+1];
					System.arraycopy(copies, 0, newWorkingCopies, 0, length);
					newWorkingCopies[length] = cu;
					return newWorkingCopies;
				} 
				return new ICompilationUnit[] {cu};
			}
		}
		return getWorkingCopies();
	}

	boolean match(char patternTypeSuffix, char[] patternPkg, char[] patternTypeName, int matchRule, int typeKind, char[] pkg, char[] typeName) {
		switch(patternTypeSuffix) {
			case IIndexConstants.CLASS_SUFFIX :
				if (typeKind != IGenericType.CLASS) return false;
				break;
			case IIndexConstants.INTERFACE_SUFFIX :
				if (typeKind != IGenericType.INTERFACE) return false;
				break;
			case IIndexConstants.ENUM_SUFFIX :
				if (typeKind != IGenericType.ENUM) return false;
				break;
			case IIndexConstants.ANNOTATION_TYPE_SUFFIX :
				if (typeKind != IGenericType.ANNOTATION_TYPE) return false;
				break;
			case IIndexConstants.TYPE_SUFFIX : // nothing
		}
	
		boolean isCaseSensitive = (matchRule & SearchPattern.R_CASE_SENSITIVE) != 0;
		if (patternPkg != null && !CharOperation.equals(patternPkg, pkg, isCaseSensitive))
				return false;
		
		if (patternTypeName != null) {
			int matchMode = matchRule - (isCaseSensitive ? SearchPattern.R_CASE_SENSITIVE : 0);
			switch(matchMode) {
				case SearchPattern.R_EXACT_MATCH :
					return CharOperation.equals(patternTypeName, typeName, isCaseSensitive);
				case SearchPattern.R_PREFIX_MATCH :
					return CharOperation.prefixEquals(patternTypeName, typeName, isCaseSensitive);
				case SearchPattern.R_PATTERN_MATCH :
					return CharOperation.match(patternTypeName, typeName, isCaseSensitive);
			}
		}
		return true;
	
	}	
	
	/**
	 * Searches for matches of a given search pattern. Search patterns can be created using helper
	 * methods (from a String pattern or a Java element) and encapsulate the description of what is
	 * being searched (for example, search method declarations in a case sensitive way).
	 *
	 * @param pattern the pattern to search
	 * @param participants the particpants in the search
	 * @param scope the search scope
	 * @param requestor the requestor to report the matches to
	 * @param monitor the progress monitor used to report progress
	 * @exception CoreException if the search failed. Reasons include:
	 *	<ul>
	 *		<li>the classpath is incorrectly set</li>
	 *	</ul>
	 *@since 3.0
	 */
	public void search(SearchPattern pattern, SearchParticipant[] participants, IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
		findMatches(pattern, participants, scope, requestor, monitor);
	}

	/**
	 * Searches for all top-level types and member types in the given scope.
	 * The search can be selecting specific types (given a package or a type name
	 * prefix and match modes). 
	 * 
	 * @param packageName the full name of the package of the searched types, or a prefix for this
	 *						package, or a wild-carded string for this package.
	 * @param typeName the dot-separated qualified name of the searched type (the qualification include
	 *					the enclosing types if the searched type is a member type), or a prefix
	 *					for this type, or a wild-carded string for this type.
	 * @param matchRule one of
	 * <ul>
	 *		<li><code>SearchPattern.R_EXACT_MATCH</code> if the package name and type name are the full names
	 *			of the searched types.</li>
	 *		<li><code>SearchPattern.R_PREFIX_MATCH</code> if the package name and type name are prefixes of the names
	 *			of the searched types.</li>
	 *		<li><code>SearchPattern.R_PATTERN_MATCH</code> if the package name and type name contain wild-cards.</li>
	 * </ul>
	 * combined with <code>SearchPattern.R_CASE_SENSITIVE</code>,
	 *   e.g. <code>R_EXACT_MATCH | R_CASE_SENSITIVE</code> if an exact and case sensitive match is requested, 
	 *   or <code>R_PREFIX_MATCH</code> if a prefix non case sensitive match is requested.
	 * @param searchFor one of
	 * <ul>
	 * 		<li><code>IJavaSearchConstants.CLASS</code> if searching for classes only</li>
	 * 		<li><code>IJavaSearchConstants.INTERFACE</code> if searching for interfaces only</li>
	 * 		<li><code>IJavaSearchConstants.TYPE</code> if searching for both classes and interfaces</li>
	 * </ul>
	 * @param scope the scope to search in
	 * @param nameRequestor the requestor that collects the results of the search
	 * @param waitingPolicy one of
	 * <ul>
	 *		<li><code>IJavaSearchConstants.FORCE_IMMEDIATE_SEARCH</code> if the search should start immediately</li>
	 *		<li><code>IJavaSearchConstants.CANCEL_IF_NOT_READY_TO_SEARCH</code> if the search should be cancelled if the
	 *			underlying indexer has not finished indexing the workspace</li>
	 *		<li><code>IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH</code> if the search should wait for the
	 *			underlying indexer to finish indexing the workspace</li>
	 * </ul>
	 * @param progressMonitor the progress monitor to report progress to, or <code>null</code> if no progress
	 *							monitor is provided
	 * @exception JavaModelException if the search failed. Reasons include:
	 *	<ul>
	 *		<li>the classpath is incorrectly set</li>
	 *	</ul>
	 * @since 3.0
	 */
	public void searchAllTypeNames(
		final char[] packageName, 
		final char[] typeName,
		final int matchRule, 
		int searchFor, 
		IJavaSearchScope scope, 
		final IRestrictedAccessTypeRequestor nameRequestor,
		int waitingPolicy,
		IProgressMonitor progressMonitor)  throws JavaModelException {

		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
			
		final char typeSuffix;
		switch(searchFor){
			case IJavaSearchConstants.CLASS :
				typeSuffix = IIndexConstants.CLASS_SUFFIX;
				break;
			case IJavaSearchConstants.INTERFACE :
				typeSuffix = IIndexConstants.INTERFACE_SUFFIX;
				break;
			case IJavaSearchConstants.ENUM :
				typeSuffix = IIndexConstants.ENUM_SUFFIX;
				break;
			case IJavaSearchConstants.ANNOTATION_TYPE :
				typeSuffix = IIndexConstants.ANNOTATION_TYPE_SUFFIX;
				break;
			default : 
				typeSuffix = IIndexConstants.TYPE_SUFFIX;
				break;
		}
		final TypeDeclarationPattern pattern = new TypeDeclarationPattern(
			packageName,
			null, // do find member types
			typeName,
			typeSuffix,
			matchRule);
		
		final HashSet workingCopyPaths = new HashSet();
		ICompilationUnit[] copies = getWorkingCopies();
		if (copies != null) {
			for (int i = 0, length = copies.length; i < length; i++) {
				ICompilationUnit workingCopy = copies[i];
				workingCopyPaths.add(workingCopy.getPath().toString());
			}
		}
	
		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
			public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRestriction access) {
				TypeDeclarationPattern record = (TypeDeclarationPattern)indexRecord;
				if (record.enclosingTypeNames != IIndexConstants.ONE_ZERO_CHAR  // filter out local and anonymous classes
						&& !workingCopyPaths.contains(documentPath)) { // filter out working copies
					if (access != null) {
						access = access.getViolatedRestriction(CharOperation.concat(record.pkg, record.simpleName, '/'), null);					
					}
					switch (record.typeSuffix) {
						case IIndexConstants.CLASS_SUFFIX :
							nameRequestor.acceptClass(record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, access);
							break;
						case IIndexConstants.INTERFACE_SUFFIX :
							nameRequestor.acceptInterface(record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, access);
							break;
						case IIndexConstants.ENUM_SUFFIX :
							// TODO (frederic) hack to get enum while getting all type names...
							nameRequestor.acceptClass(record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, access);
							break;
						case IIndexConstants.ANNOTATION_TYPE_SUFFIX :
							// TODO (frederic) need support
							break;
					}
				}
				return true;
			}
		};
	
		try {
			if (progressMonitor != null) {
				progressMonitor.beginTask(Util.bind("engine.searching"), 100); //$NON-NLS-1$
			}
			// add type names from indexes
			indexManager.performConcurrentJob(
				new PatternSearchJob(
					pattern, 
					getDefaultSearchParticipant(), // Java search only
					scope, 
					searchRequestor),
				waitingPolicy,
				progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 100));	
				
			// add type names from working copies
			if (copies != null) {
				for (int i = 0, length = copies.length; i < length; i++) {
					ICompilationUnit workingCopy = copies[i];
					final String path = workingCopy.getPath().toString();
					if (workingCopy.isConsistent()) {
						IPackageDeclaration[] packageDeclarations = workingCopy.getPackageDeclarations();
						char[] packageDeclaration = packageDeclarations.length == 0 ? CharOperation.NO_CHAR : packageDeclarations[0].getElementName().toCharArray();
						IType[] allTypes = workingCopy.getAllTypes();
						for (int j = 0, allTypesLength = allTypes.length; j < allTypesLength; j++) {
							IType type = allTypes[j];
							IJavaElement parent = type.getParent();
							char[][] enclosingTypeNames;
							if (parent instanceof IType) {
								char[] parentQualifiedName = ((IType)parent).getTypeQualifiedName('.').toCharArray();
								enclosingTypeNames = CharOperation.splitOn('.', parentQualifiedName);
							} else {
								enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
							}
							char[] simpleName = type.getElementName().toCharArray();
							int kind;
							if (type.isClass()) {
								kind = IGenericType.CLASS;
							} else if (type.isInterface()) {
								kind = IGenericType.INTERFACE;
							} else if (type.isEnum()) {
								kind = IGenericType.ENUM;
							} else /*if (type.isAnnotation())*/ {
								kind = IGenericType.ANNOTATION_TYPE;
							}
							if (match(typeSuffix, packageName, typeName, matchRule, kind, packageDeclaration, simpleName)) {
								switch(kind) {
									case IGenericType.CLASS:
										nameRequestor.acceptClass(packageDeclaration, simpleName, enclosingTypeNames, path, null);
										break;
									case IGenericType.INTERFACE:
										nameRequestor.acceptInterface(packageDeclaration, simpleName, enclosingTypeNames, path, null);
										break;
									case IGenericType.ENUM:
										// TODO need support
										break;
									case IGenericType.ANNOTATION_TYPE:
										// TODO need support
										break;
								}
							}
						}
					} else {
						Parser basicParser = getParser();
						final char[] contents = workingCopy.getBuffer().getCharacters();
						org.eclipse.jdt.internal.compiler.env.ICompilationUnit unit = new org.eclipse.jdt.internal.compiler.env.ICompilationUnit() {
							public char[] getContents() {
								return contents;
							}
							public char[] getMainTypeName() {
								return null;
							}
							public char[][] getPackageName() {
								return null;
							}
							public char[] getFileName() {
								return null;
							}
						};
						CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.compilerOptions.maxProblemsPerUnit);
						CompilationUnitDeclaration parsedUnit = basicParser.dietParse(unit, compilationUnitResult);
						if (parsedUnit != null) {
							final char[] packageDeclaration = parsedUnit.currentPackage == null ? CharOperation.NO_CHAR : CharOperation.concatWith(parsedUnit.currentPackage.getImportName(), '.');
							class AllTypeDeclarationsVisitor extends ASTVisitor {
								public boolean visit(TypeDeclaration typeDeclaration, BlockScope blockScope) {
									return false; // no local/anonymous type
								}
								public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope compilationUnitScope) {
									if (match(typeSuffix, packageName, typeName, matchRule, typeDeclaration.getKind(), packageDeclaration, typeDeclaration.name)) {
										switch(typeDeclaration.getKind()) {
											case IGenericType.CLASS:
												nameRequestor.acceptClass(packageDeclaration, typeDeclaration.name, CharOperation.NO_CHAR_CHAR, path, null);
												break;
											case IGenericType.INTERFACE:
												nameRequestor.acceptInterface(packageDeclaration, typeDeclaration.name, CharOperation.NO_CHAR_CHAR, path, null);
												break;
											case IGenericType.ENUM:
												// TODO need support
												break;
											case IGenericType.ANNOTATION_TYPE:
												// TODO need support
												break;
										}
									}
									return true;
								}
								public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope classScope) {
									if (match(typeSuffix, packageName, typeName, matchRule, memberTypeDeclaration.getKind(), packageDeclaration, memberTypeDeclaration.name)) {
										// compute encloising type names
										TypeDeclaration enclosing = memberTypeDeclaration.enclosingType;
										char[][] enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
										while (enclosing != null) {
											enclosingTypeNames = CharOperation.arrayConcat(new char[][] {enclosing.name}, enclosingTypeNames);
											if ((enclosing.bits & ASTNode.IsMemberTypeMASK) != 0) {
												enclosing = enclosing.enclosingType;
											} else {
												enclosing = null;
											}
										}
										// report
										switch(memberTypeDeclaration.getKind()) {
											case IGenericType.CLASS:
												nameRequestor.acceptClass(packageDeclaration, memberTypeDeclaration.name, enclosingTypeNames, path, null);
												break;
											case IGenericType.INTERFACE:
												nameRequestor.acceptInterface(packageDeclaration, memberTypeDeclaration.name, enclosingTypeNames, path, null);
												break;
											case IGenericType.ENUM:
												// TODO need support
												break;
											case IGenericType.ANNOTATION_TYPE:
												// TODO need support
												break;
										}
									}
									return true;
								}
							}
							parsedUnit.traverse(new AllTypeDeclarationsVisitor(), parsedUnit.scope);
						}
					}
				}
			}	
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}
	}
	
	public void searchDeclarations(IJavaElement enclosingElement, SearchRequestor requestor, SearchPattern pattern, IProgressMonitor monitor) throws JavaModelException {
		IJavaSearchScope scope = createJavaSearchScope(new IJavaElement[] {enclosingElement});
		IResource resource = this.getResource(enclosingElement);
		try {
			if (resource instanceof IFile) {
				try {
					requestor.beginReporting();
					if (VERBOSE) {
						System.out.println("Searching for " + pattern + " in " + resource.getFullPath()); //$NON-NLS-1$//$NON-NLS-2$
					}
					SearchParticipant participant = getDefaultSearchParticipant();
					SearchDocument[] documents = MatchLocator.addWorkingCopies(
						pattern,
						new SearchDocument[] {new JavaSearchDocument(enclosingElement.getPath().toString(), participant)},
						getWorkingCopies(enclosingElement),
						participant);
					participant.locateMatches(
						documents, 
						pattern, 
						scope, 
						requestor, 
						monitor);
				} finally {
					requestor.endReporting();
				}
			} else {
				search(
					pattern, 
					new SearchParticipant[] {getDefaultSearchParticipant()}, 
					scope, 
					requestor, 
					monitor);
			}
		} catch (CoreException e) {
			if (e instanceof JavaModelException)
				throw (JavaModelException) e;
			throw new JavaModelException(e);
		}
	}

	/**
	 * Searches for all declarations of the fields accessed in the given element.
	 * The element can be a compilation unit, a source type, or a source method.
	 * Reports the field declarations using the given requestor.
	 * <p>
	 * Consider the following code:
	 * <code>
	 * <pre>
	 *		class A {
	 *			int field1;
	 *		}
	 *		class B extends A {
	 *			String value;
	 *		}
	 *		class X {
	 *			void test() {
	 *				B b = new B();
	 *				System.out.println(b.value + b.field1);
	 *			};
	 *		}
	 * </pre>
	 * </code>
	 * then searching for declarations of accessed fields in method 
	 * <code>X.test()</code> would collect the fields
	 * <code>B.value</code> and <code>A.field1</code>.
	 * </p>
	 *
	 * @param enclosingElement the method, type, or compilation unit to be searched in
	 * @param requestor a callback object to which each match is reported
	 * @param monitor the progress monitor used to report progress
	 * @exception JavaModelException if the search failed. Reasons include:
	 *	<ul>
	 *		<li>the element doesn't exist</li>
	 *		<li>the classpath is incorrectly set</li>
	 *	</ul>
	 * @since 3.0
	 */	
	public void searchDeclarationsOfAccessedFields(IJavaElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws JavaModelException {
		SearchPattern pattern = new DeclarationOfAccessedFieldsPattern(enclosingElement);
		searchDeclarations(enclosingElement, requestor, pattern, monitor);
	}
	
	/**
	 * Searches for all declarations of the types referenced in the given element.
	 * The element can be a compilation unit, a source type, or a source method.
	 * Reports the type declarations using the given requestor.
	 * <p>
	 * Consider the following code:
	 * <code>
	 * <pre>
	 *		class A {
	 *		}
	 *		class B extends A {
	 *		}
	 *		interface I {
	 *		  int VALUE = 0;
	 *		}
	 *		class X {
	 *			void test() {
	 *				B b = new B();
	 *				this.foo(b, I.VALUE);
	 *			};
	 *		}
	 * </pre>
	 * </code>
	 * then searching for declarations of referenced types in method <code>X.test()</code>
	 * would collect the class <code>B</code> and the interface <code>I</code>.
	 * </p>
	 *
	 * @param enclosingElement the method, type, or compilation unit to be searched in
	 * @param requestor a callback object to which each match is reported
	 * @param monitor the progress monitor used to report progress
	 * @exception JavaModelException if the search failed. Reasons include:
	 *	<ul>
	 *		<li>the element doesn't exist</li>
	 *		<li>the classpath is incorrectly set</li>
	 *	</ul>
	 * @since 3.0
	 */	
	public void searchDeclarationsOfReferencedTypes(IJavaElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws JavaModelException {
		SearchPattern pattern = new DeclarationOfReferencedTypesPattern(enclosingElement);
		searchDeclarations(enclosingElement, requestor, pattern, monitor);
	}
	
	/**
	 * Searches for all declarations of the methods invoked in the given element.
	 * The element can be a compilation unit, a source type, or a source method.
	 * Reports the method declarations using the given requestor.
	 * <p>
	 * Consider the following code:
	 * <code>
	 * <pre>
	 *		class A {
	 *			void foo() {};
	 *			void bar() {};
	 *		}
	 *		class B extends A {
	 *			void foo() {};
	 *		}
	 *		class X {
	 *			void test() {
	 *				A a = new B();
	 *				a.foo();
	 *				B b = (B)a;
	 *				b.bar();
	 *			};
	 *		}
	 * </pre>
	 * </code>
	 * then searching for declarations of sent messages in method 
	 * <code>X.test()</code> would collect the methods
	 * <code>A.foo()</code>, <code>B.foo()</code>, and <code>A.bar()</code>.
	 * </p>
	 *
	 * @param enclosingElement the method, type, or compilation unit to be searched in
	 * @param requestor a callback object to which each match is reported
	 * @param monitor the progress monitor used to report progress
	 * @exception JavaModelException if the search failed. Reasons include:
	 *	<ul>
	 *		<li>the element doesn't exist</li>
	 *		<li>the classpath is incorrectly set</li>
	 *	</ul>
	 * @since 3.0
	 */	
	public void searchDeclarationsOfSentMessages(IJavaElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws JavaModelException {
		SearchPattern pattern = new DeclarationOfReferencedMethodsPattern(enclosingElement);
		searchDeclarations(enclosingElement, requestor, pattern, monitor);
	}
}
