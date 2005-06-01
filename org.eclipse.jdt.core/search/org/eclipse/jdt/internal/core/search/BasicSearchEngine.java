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
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.search.matching.*;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 * Search basic engine. Public search engine (see {@link org.eclipse.jdt.core.search.SearchEngine}
 * for detailed comment), now uses basic engine functionalities.
 * Note that serch basic engine does not implement deprecated functionalities...
 */
public class BasicSearchEngine {

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
	public BasicSearchEngine() {
		// will use working copies of PRIMARY owner
	}
	
	/**
	 * @see org.eclipse.jdt.core.search.SearchEngine#SearchEngine(ICompilationUnit[]) for detailed comment.
	 */
	public BasicSearchEngine(ICompilationUnit[] workingCopies) {
		this.workingCopies = workingCopies;
	}

	char convertTypeKind(int typeDeclarationKind) {
		switch(typeDeclarationKind) {
			case IGenericType.CLASS_DECL : return IIndexConstants.CLASS_SUFFIX;
			case IGenericType.INTERFACE_DECL : return IIndexConstants.INTERFACE_SUFFIX;
			case IGenericType.ENUM_DECL : return IIndexConstants.ENUM_SUFFIX;
			case IGenericType.ANNOTATION_TYPE_DECL : return IIndexConstants.ANNOTATION_TYPE_SUFFIX;
			default : return IIndexConstants.TYPE_SUFFIX;
		}
	}
	/**
	 * @see org.eclipse.jdt.core.search.SearchEngine#SearchEngine(WorkingCopyOwner) for detailed comment.
	 */
	public BasicSearchEngine(WorkingCopyOwner workingCopyOwner) {
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
		for (int i = 0, length = elements.length; i < length; i++) {
			IJavaElement element = elements[i];
			if (element != null) {
				try {
					if (element instanceof JavaProject) {
						scope.add((JavaProject)element, includeMask, visitedProjects);
					} else {
						scope.add(element);
					}
				} catch (JavaModelException e) {
					// ignore
				}
			}
		}
		return scope;
	}
	
	/**
	 * Returns a Java search scope with the workspace as the only limit.
	 *
	 * @return a new workspace scope
	 */
	public static IJavaSearchScope createWorkspaceScope() {
		return JavaModelManager.getJavaModelManager().getWorkspaceScope();
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
			monitor.beginTask(Messages.engine_searching, 100); 
		if (BasicSearchEngine.VERBOSE) {
			System.out.println("Searching for pattern: " + pattern.toString()); //$NON-NLS-1$
			System.out.println(scope); //$NON-NLS-1$
		}
	
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		try {
			requestor.beginReporting();
			for (int i = 0, l = participants == null ? 0 : participants.length; i < l; i++) {
				if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	
				SearchParticipant participant = participants[i];
				SubProgressMonitor subMonitor= monitor==null ? null : new SubProgressMonitor(monitor, 1000);
				if (subMonitor != null) subMonitor.beginTask("", 1000); //$NON-NLS-1$
				try {
					if (subMonitor != null) subMonitor.subTask(Messages.bind(Messages.engine_searching_indexing, new String[] {participant.getDescription()})); 
					participant.beginSearching();
					requestor.enterParticipant(participant);
					PathCollector pathCollector = new PathCollector();
					indexManager.performConcurrentJob(
						new PatternSearchJob(pattern, participant, scope, pathCollector),
						IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
						subMonitor);
					if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	
					// locate index matches if any (note that all search matches could have been issued during index querying)
					if (subMonitor != null) subMonitor.subTask(Messages.bind(Messages.engine_searching_matching, new String[] {participant.getDescription()})); 
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

	boolean match(char patternTypeSuffix, int modifiers) {
		switch(patternTypeSuffix) {
			case IIndexConstants.CLASS_SUFFIX :
				return (modifiers & (Flags.AccAnnotation | Flags.AccInterface | Flags.AccEnum)) == 0;
			case IIndexConstants.CLASS_AND_INTERFACE_SUFFIX:
				return (modifiers & (Flags.AccAnnotation | Flags.AccEnum)) == 0;
			case IIndexConstants.CLASS_AND_ENUM_SUFFIX:
				return (modifiers & (Flags.AccAnnotation | Flags.AccInterface)) == 0;
			case IIndexConstants.INTERFACE_SUFFIX :
				return (modifiers & Flags.AccInterface) != 0;
			case IIndexConstants.ENUM_SUFFIX :
				return (modifiers & Flags.AccEnum) != 0;
			case IIndexConstants.ANNOTATION_TYPE_SUFFIX :
				return (modifiers & Flags.AccAnnotation) != 0;
		}
		return true;
	}

	boolean match(char patternTypeSuffix, char[] patternPkg, char[] patternTypeName, int matchRule, int typeKind, char[] pkg, char[] typeName) {
		switch(patternTypeSuffix) {
			case IIndexConstants.CLASS_SUFFIX :
				if (typeKind != IGenericType.CLASS_DECL) return false;
				break;
			case IIndexConstants.CLASS_AND_INTERFACE_SUFFIX:
				if (typeKind != IGenericType.CLASS_DECL && typeKind != IGenericType.INTERFACE_DECL) return false;
				break;
			case IIndexConstants.CLASS_AND_ENUM_SUFFIX:
				if (typeKind != IGenericType.CLASS_DECL && typeKind != IGenericType.ENUM_DECL) return false;
				break;
			case IIndexConstants.INTERFACE_SUFFIX :
				if (typeKind != IGenericType.INTERFACE_DECL) return false;
				break;
			case IIndexConstants.ENUM_SUFFIX :
				if (typeKind != IGenericType.ENUM_DECL) return false;
				break;
			case IIndexConstants.ANNOTATION_TYPE_SUFFIX :
				if (typeKind != IGenericType.ANNOTATION_TYPE_DECL) return false;
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
		if (VERBOSE) {
			System.out.println("BasicSearchEngine.search(SearchPattern, SearchParticipant[], IJavaSearchScope, SearchRequestor, IProgressMonitor)"); //$NON-NLS-1$
		}
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
	 * @param searchFor determines the nature of the searched elements
	 *	<ul>
	 * 	<li>{@link IJavaSearchConstants#CLASS}: only look for classes</li>
	 *		<li>{@link IJavaSearchConstants#INTERFACE}: only look for interfaces</li>
	 * 	<li>{@link IJavaSearchConstants#ENUM}: only look for enumeration</li>
	 *		<li>{@link IJavaSearchConstants#ANNOTATION_TYPE}: only look for annotation type</li>
	 * 	<li>{@link IJavaSearchConstants#CLASS_AND_ENUM}: only look for classes and enumerations</li>
	 *		<li>{@link IJavaSearchConstants#CLASS_AND_INTERFACE}: only look for classes and interfaces</li>
	 * 	<li>{@link IJavaSearchConstants#TYPE}: look for all types (ie. classes, interfaces, enum and annotation types)</li>
	 *	</ul>
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

		if (VERBOSE) {
			System.out.println("BasicSearchEngine.searchAllTypeNames(char[], char[], int, int, IJavaSearchScope, IRestrictedAccessTypeRequestor, int, IProgressMonitor)"); //$NON-NLS-1$
			System.out.println("	- package name: "+(packageName==null?"null":new String(packageName))); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("	- type name: "+(typeName==null?"null":new String(typeName))); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("	- match rule: "+matchRule); //$NON-NLS-1$
			System.out.println("	- search for: "+searchFor); //$NON-NLS-1$
			System.out.println("	- scope: "+scope); //$NON-NLS-1$
		}

		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
			
		final char typeSuffix;
		switch(searchFor){
			case IJavaSearchConstants.CLASS :
				typeSuffix = IIndexConstants.CLASS_SUFFIX;
				break;
			case IJavaSearchConstants.CLASS_AND_INTERFACE :
				typeSuffix = IIndexConstants.CLASS_AND_INTERFACE_SUFFIX;
				break;
			case IJavaSearchConstants.CLASS_AND_ENUM :
				typeSuffix = IIndexConstants.CLASS_AND_ENUM_SUFFIX;
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
			public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
				TypeDeclarationPattern record = (TypeDeclarationPattern)indexRecord;
				AccessRestriction accessRestriction = null;
				if (record.enclosingTypeNames != IIndexConstants.ONE_ZERO_CHAR  // filter out local and anonymous classes
						&& !workingCopyPaths.contains(documentPath)) { // filter out working copies
					if (access != null) {
						// Compute document relative path
						int pkgLength = record.pkg==null ? 0 : record.pkg.length+1;
						int nameLength = record.simpleName==null ? 0 : record.simpleName.length;
						char[] path = new char[pkgLength+nameLength];
						int pos = 0;
						if (pkgLength > 0) {
							System.arraycopy(record.pkg, 0, path, pos, pkgLength-1);
							CharOperation.replace(path, '.', '/');
							path[pkgLength-1] = '/';
							pos += pkgLength;
						}
						if (nameLength > 0) {
							System.arraycopy(record.simpleName, 0, path, pos, nameLength);
							pos += nameLength;
						}
						// Update access restriction if path is not empty
						if (pos > 0) {
							accessRestriction = access.getViolatedRestriction(path);
						}
					}
					if (match(record.typeSuffix, record.modifiers)) {
						nameRequestor.acceptType(record.modifiers, record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, accessRestriction);
					}
				}
				return true;
			}
		};
	
		try {
			if (progressMonitor != null) {
				progressMonitor.beginTask(Messages.engine_searching, 100); 
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
							if (type.isEnum()) {
								kind = IGenericType.ENUM_DECL;
							} else if (type.isAnnotation()) {
								kind = IGenericType.ANNOTATION_TYPE_DECL;
							}	else if (type.isClass()) {
								kind = IGenericType.CLASS_DECL;
							} else /*if (type.isInterface())*/ {
								kind = IGenericType.INTERFACE_DECL;
							}
							if (match(typeSuffix, packageName, typeName, matchRule, kind, packageDeclaration, simpleName)) {
								nameRequestor.acceptType(type.getFlags(), packageDeclaration, simpleName, enclosingTypeNames, path, null);
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
									if (match(typeSuffix, packageName, typeName, matchRule, typeDeclaration.kind(), packageDeclaration, typeDeclaration.name)) {
										nameRequestor.acceptType(typeDeclaration.modifiers, packageDeclaration, typeDeclaration.name, CharOperation.NO_CHAR_CHAR, path, null);
									}
									return true;
								}
								public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope classScope) {
									if (match(typeSuffix, packageName, typeName, matchRule, memberTypeDeclaration.kind(), packageDeclaration, memberTypeDeclaration.name)) {
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
										nameRequestor.acceptType(memberTypeDeclaration.modifiers, packageDeclaration, memberTypeDeclaration.name, enclosingTypeNames, path, null);
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

	/**
	 * Searches for all top-level types and member types in the given scope using  a case sensitive exact match
	 * with the given qualified names and type names.
	 * 
	 * @param qualifications the qualified name of the package/enclosing type of the searched types
	 * @param typeNames the simple names of the searched types
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
		final char[][] qualifications, 
		final char[][] typeNames,
		final int matchRule, 
		int searchFor, 
		IJavaSearchScope scope, 
		final IRestrictedAccessTypeRequestor nameRequestor,
		int waitingPolicy,
		IProgressMonitor progressMonitor)  throws JavaModelException {

		if (VERBOSE) {
			System.out.println("BasicSearchEngine.searchAllTypeNames(char[][], char[][], int, int, IJavaSearchScope, IRestrictedAccessTypeRequestor, int, IProgressMonitor)"); //$NON-NLS-1$
			System.out.println("	- package name: "+(qualifications==null?"null":new String(CharOperation.concatWith(qualifications, ',')))); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("	- type name: "+(typeNames==null?"null":new String(CharOperation.concatWith(typeNames, ',')))); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("	- match rule: "+matchRule); //$NON-NLS-1$
			System.out.println("	- search for: "+searchFor); //$NON-NLS-1$
			System.out.println("	- scope: "+scope); //$NON-NLS-1$
		}
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();

		final char typeSuffix;
		switch(searchFor){
			case IJavaSearchConstants.CLASS :
				typeSuffix = IIndexConstants.CLASS_SUFFIX;
				break;
			case IJavaSearchConstants.CLASS_AND_INTERFACE :
				typeSuffix = IIndexConstants.CLASS_AND_INTERFACE_SUFFIX;
				break;
			case IJavaSearchConstants.CLASS_AND_ENUM :
				typeSuffix = IIndexConstants.CLASS_AND_ENUM_SUFFIX;
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
		final MultiTypeDeclarationPattern pattern = new MultiTypeDeclarationPattern(qualifications, typeNames, typeSuffix, matchRule);

		final HashSet workingCopyPaths = new HashSet();
		ICompilationUnit[] copies = getWorkingCopies();
		if (copies != null) {
			for (int i = 0, length = copies.length; i < length; i++) {
				ICompilationUnit workingCopy = copies[i];
				workingCopyPaths.add(workingCopy.getPath().toString());
			}
		}

		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
			public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
				if (!workingCopyPaths.contains(documentPath)) { // filter out working copies
					QualifiedTypeDeclarationPattern record = (QualifiedTypeDeclarationPattern) indexRecord;
					AccessRestriction accessRestriction = null;
					if (access != null) {
						// Compute document relative path
						int qualificationLength = record.qualification == null ? 0 : record.qualification.length + 1;
						int nameLength = record.simpleName == null ? 0 : record.simpleName.length;
						char[] path = new char[qualificationLength + nameLength];
						int pos = 0;
						if (qualificationLength > 0) {
							System.arraycopy(record.qualification, 0, path, pos, qualificationLength - 1);
							CharOperation.replace(path, '.', '/');
							path[qualificationLength-1] = '/';
							pos += qualificationLength;
						}
						if (nameLength > 0) {
							System.arraycopy(record.simpleName, 0, path, pos, nameLength);
							pos += nameLength;
						}
						// Update access restriction if path is not empty
						if (pos > 0) {
							accessRestriction = access.getViolatedRestriction(path);
						}
					}
					nameRequestor.acceptType(record.modifiers, record.getPackageName(), record.simpleName, record.getEnclosingTypeNames(), documentPath, accessRestriction);
				}
				return true;
			}
		};
	
		try {
			if (progressMonitor != null) {
				progressMonitor.beginTask(Messages.engine_searching, 100); 
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
							char[] qualification = packageDeclaration;
							if (parent instanceof IType) {
								char[] parentQualifiedName = ((IType)parent).getTypeQualifiedName('.').toCharArray();
								enclosingTypeNames = CharOperation.splitOn('.', parentQualifiedName);
								qualification = CharOperation.concat(qualification, parentQualifiedName);
							} else {
								enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
							}
							char[] simpleName = type.getElementName().toCharArray();
							char suffix = IIndexConstants.TYPE_SUFFIX;
							if (type.isClass()) {
								suffix = IIndexConstants.CLASS_SUFFIX;
							} else if (type.isInterface()) {
								suffix = IIndexConstants.INTERFACE_SUFFIX;
							} else if (type.isEnum()) {
								suffix = IIndexConstants.ENUM_SUFFIX;
							} else if (type.isAnnotation()) {
								suffix = IIndexConstants.ANNOTATION_TYPE_SUFFIX;
							}
							if (pattern.matchesDecodedKey(new QualifiedTypeDeclarationPattern(qualification, simpleName, suffix, matchRule))) {
								nameRequestor.acceptType(type.getFlags(), packageDeclaration, simpleName, enclosingTypeNames, path, null);
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
							final char[] packageDeclaration = parsedUnit.currentPackage == null
								? CharOperation.NO_CHAR
								: CharOperation.concatWith(parsedUnit.currentPackage.getImportName(), '.');
							class AllTypeDeclarationsVisitor extends ASTVisitor {
								public boolean visit(TypeDeclaration typeDeclaration, BlockScope blockScope) {
									return false; // no local/anonymous type
								}
								public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope compilationUnitScope) {
									SearchPattern decodedPattern =
										new QualifiedTypeDeclarationPattern(packageDeclaration, typeDeclaration.name, convertTypeKind(typeDeclaration.kind()), matchRule);
									if (pattern.matchesDecodedKey(decodedPattern)) {
										nameRequestor.acceptType(typeDeclaration.modifiers, packageDeclaration, typeDeclaration.name, CharOperation.NO_CHAR_CHAR, path, null);
									}
									return true;
								}
								public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope classScope) {
									// compute encloising type names
									char[] qualification = packageDeclaration;
									TypeDeclaration enclosing = memberTypeDeclaration.enclosingType;
									char[][] enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
									while (enclosing != null) {
										qualification = CharOperation.concat(qualification, enclosing.name, '.');
										enclosingTypeNames = CharOperation.arrayConcat(new char[][] {enclosing.name}, enclosingTypeNames);
										if ((enclosing.bits & ASTNode.IsMemberTypeMASK) != 0) {
											enclosing = enclosing.enclosingType;
										} else {
											enclosing = null;
										}
									}
									SearchPattern decodedPattern =
										new QualifiedTypeDeclarationPattern(qualification, memberTypeDeclaration.name, convertTypeKind(memberTypeDeclaration.kind()), matchRule);
									if (pattern.matchesDecodedKey(decodedPattern)) {
										nameRequestor.acceptType(memberTypeDeclaration.modifiers, packageDeclaration, memberTypeDeclaration.name, enclosingTypeNames, path, null);
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
		if (VERBOSE) {
			System.out.println("	- java element: "+enclosingElement); //$NON-NLS-1$
		}
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
		if (VERBOSE) {
			System.out.println("BasicSearchEngine.searchDeclarationsOfAccessedFields(IJavaElement, SearchRequestor, SearchPattern, IProgressMonitor)"); //$NON-NLS-1$
		}
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
		if (VERBOSE) {
			System.out.println("BasicSearchEngine.searchDeclarationsOfReferencedTypes(IJavaElement, SearchRequestor, SearchPattern, IProgressMonitor)"); //$NON-NLS-1$
		}
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
		if (VERBOSE) {
			System.out.println("BasicSearchEngine.searchDeclarationsOfSentMessages(IJavaElement, SearchRequestor, SearchPattern, IProgressMonitor)"); //$NON-NLS-1$
		}
		SearchPattern pattern = new DeclarationOfReferencedMethodsPattern(enclosingElement);
		searchDeclarations(enclosingElement, requestor, pattern, monitor);
	}
}
