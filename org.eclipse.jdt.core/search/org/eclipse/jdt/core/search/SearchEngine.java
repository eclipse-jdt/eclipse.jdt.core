package org.eclipse.jdt.core.search;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.util.CharOperation;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.HierarchyScope;
import org.eclipse.jdt.internal.core.search.IndexSearchAdapter;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.IInfoConstants;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.core.search.PatternSearchJob;
import org.eclipse.jdt.internal.core.search.PathCollector;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.search.matching.*;

import java.util.*;

/**
 * A <code>SearchEngine</code> searches for java elements following a search pattern.
 * The search can be limited to a search scope.
 * <p>
 * Various search patterns can be created using the factory methods 
 * <code>createSearchPattern(String, int, int, boolean)</code>, <code>createSearchPattern(IJavaElement, int)</code>,
 * <code>createOrSearchPattern(ISearchPattern, ISearchPattern)</code>.
 * </p>
 * <p>For example, one can search for references to a method in the hierarchy of a type, 
 * or one can search for the declarations of types starting with "Abstract" in a project.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class SearchEngine {
/**
 * Returns a java search scope limited to the hierarchy of the given type.
 * The java elements resulting from a search with this scope will
 * be types in this hierarchy, or members of the types in this hierarchy.
 *
 * @param type the focus of the hierarchy scope
 * @return a new hierarchy scope
 * @exception JavaModelException if the hierarchy could not be computed on the given type
 */
public static IJavaSearchScope createHierarchyScope(IType type) throws JavaModelException {
	return new HierarchyScope(type);
}
/**
 * Returns a java search scope limited to the given resources.
 * The java elements resulting from a search with this scope will
 * have their underlying resource included in or equals to one of the given
 * resources.
 * <p>
 * Resources must not overlap, e.g. one cannot include a folder and its children.
 * </p>
 *
 * @param resources the resources the scope is limited to
 * @return a new java search scope
 */
public static IJavaSearchScope createJavaSearchScope(IResource[] resources) {
	JavaSearchScope scope = new JavaSearchScope();
	for (int i = 0, length = resources.length; i < length; i++) {
		scope.add(resources[i]);
	}
	return scope;
}
/**
 * Returns a search pattern that combines the given two patterns into a "or" pattern.
 * The search result will match either the left pattern or the right pattern.
 *
 * @param leftPattern the left pattern
 * @param rightPattern the right pattern
 * @return a "or" pattern
 */
public static ISearchPattern createOrSearchPattern(ISearchPattern leftPattern, ISearchPattern rightPattern) {
	return new OrPattern((SearchPattern)leftPattern, (SearchPattern)rightPattern);
}
/**
 * Returns a search pattern based on a given string pattern. The string patterns support '*' wild-cards.
 * The remaining parameters are used to narrow down the type of expected results.
 *
 * <p>
 *	Examples:
 *	<ul>
 * 		<li>search for case insensitive references to <code>Object</code>:
 *			<code>createSearchPattern("Object", TYPE, REFERENCES, false);</code>
 *  	<li>search for case sensitive references to exact <code>Object()</code> constructor:
 *			<code>createSearchPattern("java.lang.Object()", CONSTRUCTOR, REFERENCES, true);</code>
 *  	<li>search for implementors of <code>java.lang.Runnable</code>:
 *			<code>createSearchPattern("java.lang.Runnable", TYPE, IMPLEMENTORS, true);</code>
 * @param searchFor determines the nature of the searched elements
 *	<ul>
 * 		<li><code>IJavaSearchConstants.CLASS</code>: only look for classes
 *		<li><code>IJavaSearchConstants.INTERFACE</code>: only look for interfaces
 * 		<li><code>IJavaSearchConstants.TYPE</code>: look for both classes and interfaces
 *		<li><code>IJavaSearchConstants.FIELD</code>: look for fields
 *		<li><code>IJavaSearchConstants.METHOD</code>: look for methods
 *		<li><code>IJavaSearchConstants.CONSTRUCTOR</code>: look for constructors
 *		<li><code>IJavaSearchConstants.PACKAGE</code>: look for packages
 *	</ul>
 * @param limitTo determines the nature of the expected matches
 *	<ul>
 * 		<li><code>IJavaSearchConstants.DECLARATIONS</code>: will search declarations matching with the corresponding
 * 			element. In case the element is a method, declarations of matching methods in subtypes will also
 *  		be found, allowing to find declarations of abstract methods, etc.
 *
 *		 <li><code>IJavaSearchConstants.REFERENCES</code>: will search references to the given element.
 *
 *		 <li><code>IJavaSearchConstants.ALL_OCCURRENCES</code>: will search for either declarations or references as specified
 *  		above.
 *
 *		 <li><code>IJavaSearchConstants.IMPLEMENTORS</code>: for interface, will find all types which implements a given interface.
 *	</ul>
 *
 * @param isCaseSensitive indicates whether the search is case sensitive or not.
 * @return a search pattern on the given string pattern, or <code>null</code> if the string pattern is ill-formed.
 */
public static ISearchPattern createSearchPattern(String stringPattern, int searchFor, int limitTo, boolean isCaseSensitive) {

	return SearchPattern.createPattern(stringPattern, searchFor, limitTo, IJavaSearchConstants.PATTERN_MATCH, isCaseSensitive);
}
/**
 * Returns a search pattern based on a given Java element. 
 * The pattern is used to trigger the appropriate search, and can be parameterized as follows:
 *
 * @param element the java element the search pattern is based on
 * @param limitTo determines the nature of the expected matches
 * 	<ul>
 * 		<li><code>IJavaSearchConstants.DECLARATIONS</code>: will search declarations matching with the corresponding
 * 			element. In case the element is a method, declarations of matching methods in subtypes will also
 *  		be found, allowing to find declarations of abstract methods, etc.
 *
 *		 <li><code>IJavaSearchConstants.REFERENCES</code>: will search references to the given element.
 *
 *		 <li><code>IJavaSearchConstants.ALL_OCCURRENCES</code>: will search for either declarations or references as specified
 *  		above.
 *
 *		 <li><code>IJavaSearchConstants.IMPLEMENTORS</code>: for interface, will find all types which implements a given interface.
 *	</ul>
 * @return a search pattern for a java element or <code>null</code> if the given element is ill-formed
 */
public static ISearchPattern createSearchPattern(IJavaElement element, int limitTo) {

	return SearchPattern.createPattern(element, limitTo);
}
/**
 * Returns a java search scope with the workspace as the only limit.
 *
 * @return a new workspace scope
 */
public static IJavaSearchScope createWorkspaceScope() {
	return new JavaWorkspaceScope();
}
/**
 * Searches for the Java element determined by the given signature. The signature
 * can be incomplete. For example, a call like 
 * <code>search(ws,"run()",METHOD,REFERENCES,col)</code>
 * searchs for all references to the method <code>run</code>.
 *
 * @param workspace the workspace
 * @param pattern the pattern to be searched for
 * @param searchFor a hint what kind of Java element the string pattern represents.
 *  Look into <code>IJavaSearchConstants</code> for valid values
 * @param limitTo one of the following values:
 *	<ul>
 *	  <li><code>IJavaSearchConstants.DECLARATIONS</code>: search 
 *		  for declarations only </li>
 *	  <li><code>IJavaSearchConstants.REFERENCES</code>: search 
 *		  for all references </li>
 *	  <li><code>IJavaSearchConstants.ALL_OCCURENCES</code>: search 
 *		  for both declarations and all references </li>
 *	  <li><code>IJavaSearchConstants.IMPLEMENTORS</code>: search for
 *		  all implementors of an interface; the value is only valid if
 *		  the Java element represents an interface
 * 	</ul>
 * @param scope the search result has to be limited to the given scope
 * @param resultCollector a callback object to which each match is reported	 
 * @exception JavaModelException if the search failed. Reasons include:
 *	<ul>
 *		<li>the classpath is incorrectly set
 *	</ul>
 */
public void search(IWorkspace workspace, String patternString, int searchFor, int limitTo, IJavaSearchScope scope, IJavaSearchResultCollector resultCollector) throws JavaModelException {

	search(workspace, createSearchPattern(patternString, searchFor, limitTo, true), scope, resultCollector);
}
/**
 * Searches for the given Java element.
 *
 * @param workspace the workspace
 * @param element the Java element to be searched for
 * @param limitTo one of the following values:
 *	<ul>
 *	  <li><code>IJavaSearchConstants.DECLARATIONS</code>: search 
 *		  for declarations only </li>
 *	  <li><code>IJavaSearchConstants.REFERENCES</code>: search 
 *		  for all references </li>
 *	  <li><code>IJavaSearchConstants.ALL_OCCURENCES</code>: search 
 *		  for both declarations and all references </li>
 *	  <li><code>IJavaSearchConstants.IMPLEMENTORS</code>: search for
 *		  all implementors of an interface; the value is only valid if
 *		  the Java element represents an interface
 * 	</ul>
 * @param scope the search result has to be limited to the given scope
 * @param resultCollector a callback object to which each match is reported
 * @exception JavaModelException if the search failed. Reasons include:
 *	<ul>
 *		<li>the element doesn't exist
 *		<li>the classpath is incorrectly set
 *	</ul>
 */
public void search(IWorkspace workspace, IJavaElement element, int limitTo, IJavaSearchScope scope, IJavaSearchResultCollector resultCollector) throws JavaModelException {

	search(workspace, createSearchPattern(element, limitTo), scope, resultCollector);
}
/**
 * Searches for matches of a given search pattern. Search patterns can be created using helper
 * methods (from a String pattern or a Java element) and encapsulate the description of what is
 * being searched (e.g. search method declarations in a case sensitive way).
 *
 * @param workspace the workspace
 * @param searchPattern the pattern to be searched for
 * @param scope the search result has to be limited to the given scope
 * @param resultCollector a callback object to which each match is reported
 * @exception JavaModelException if the search failed. Reasons include:
 *	<ul>
 *		<li>the classpath is incorrectly set
 *	</ul>
 */
public void search(IWorkspace workspace, ISearchPattern searchPattern, IJavaSearchScope scope, IJavaSearchResultCollector resultCollector) throws JavaModelException {

	/* search is starting */
	resultCollector.aboutToStart();

	try {	
		if (searchPattern == null) return;

		/* initialize progress monitor */
		IProgressMonitor progressMonitor = resultCollector.getProgressMonitor();
		if (progressMonitor != null) {
			progressMonitor.beginTask("Searching...", 105); // 5 for getting paths, 100 for locating matches
		}

		/* index search */
		PathCollector pathCollector = new PathCollector();

		IndexManager indexManager = ((JavaModelManager)JavaModelManager.getJavaModelManager())
										.getIndexManager();
		int detailLevel = IInfoConstants.PathInfo | IInfoConstants.PositionInfo;
		MatchLocator matchLocator = new MatchLocator((SearchPattern)searchPattern, detailLevel, resultCollector, scope);
		if (indexManager != null) {
			indexManager.performConcurrentJob(
				new PatternSearchJob(
					(SearchPattern)searchPattern, 
					scope, 
					detailLevel, 
					pathCollector, 
					indexManager, 
					progressMonitor),
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				progressMonitor);

			if (progressMonitor != null) {
				progressMonitor.worked(5);
			}
				
			/* eliminating false matches and locating them */
			if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
			matchLocator.locateMatches(pathCollector.getPaths(), workspace);
		}

		if (progressMonitor != null) {
			progressMonitor.done();
		}

		matchLocator.locatePackageDeclarations(workspace);
	} finally {
		/* search has ended */
		resultCollector.done();
	}
}
/**
 * Searches for all top-level types and member types in the given scope.
 * The search can be selecting specific types (given a package or a type name
 * prefix and match modes). 
 * 
 * @param workspace the workspace to search in
 * @param packageName the full name of the package of the searched types, or a prefix for this
 *						package, or a wild-carded string for this package.
 * @param typeName the dot-separated qualied name of the searched type (the qualification include
 *					the enclosing types if the searched type is a member type), or a prefix
 *					for this type, or a wild-carded string for this type.
 * @param matchMode one of
 * <ul>
 *		<li><code>IJavaSearchConstants.EXACT_MATCH</code> if the package name and type name are the full names
 *			of the searched types.
 *		<li><code>IJavaSearchConstants.PREFIX_MATCH</code> if the package name and type name are prefixes of the names
 *			of the searched types.
 *		<li><code>IJavaSearchConstants.PATTERN_MATCH</code> if the package name and type name contain wild-cards.
 * </ul>
 * @param isCaseSensitive whether the search should be case senistive
 * @param searchFor one of
 * <ul>
 * 		<li><code>IJavaSearchConstants.CLASS</code> if searching for classes only
 * 		<li><code>IJavaSearchConstants.INTERFACE</code> if searching for interfaces only
 * 		<li><code>IJavaSearchConstants.TYPE</code> if searching for both classes and interfaces
 * </ul>
 * @param scope the scope to search in
 * @param nameRequestor the requestor that collects the results of the search
 * @param waitingPolicy one of
 * <ul>
 *		<li><code>IJavaSearchConstants.FORCE_IMMEDIATE_SEARCH</code> if the search should start immediately
 *		<li><code>IJavaSearchConstants.CANCEL_IF_NOT_READY_TO_SEARCH</code> if the search should be canceled if the
 *			underlying indexer has not finished indexing the workspace
 *		<li><code>IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH</code> if the search should wait for the
 *			underlying indexer to finish indexing the workspace
 * </ul>
 * @param progressMonitor the progress monitor to report progress to, or <code>null</code> if no progress
 *							monitor is provided
 * @exception JavaModelException if the search failed. Reasons include:
 *	<ul>
 *		<li>the classpath is incorrectly set
 *	</ul>
 */
public void searchAllTypeNames(
	IWorkspace workspace,
	char[] packageName, 
	char[] typeName,
	int matchMode, 
	boolean isCaseSensitive,
	int searchFor, 
	IJavaSearchScope scope, 
	final ITypeNameRequestor nameRequestor,
	int waitingPolicy,
	IProgressMonitor progressMonitor)  throws JavaModelException {

	IndexManager indexManager = ((JavaModelManager)JavaModelManager.getJavaModelManager()).getIndexManager();
	if (indexManager == null) return;
		
	char classOrInterface;
	switch(searchFor){
		case IJavaSearchConstants.CLASS :
			classOrInterface = IIndexConstants.CLASS_SUFFIX;
			break;
		case IJavaSearchConstants.INTERFACE :
			classOrInterface = IIndexConstants.INTERFACE_SUFFIX;
			break;
		default : 
			classOrInterface = IIndexConstants.TYPE_SUFFIX;
			break;
	}
	SearchPattern pattern = new TypeDeclarationPattern(
		packageName,
		null, // do find member types
		typeName,
		classOrInterface,
		matchMode, 
		isCaseSensitive);
	
	IIndexSearchRequestor searchRequestor = new IndexSearchAdapter(){
		public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
			nameRequestor.acceptClass(packageName, simpleTypeName, enclosingTypeNames, resourcePath);
		}		
		public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
			nameRequestor.acceptInterface(packageName, simpleTypeName, enclosingTypeNames, resourcePath);
		}		
	};

	indexManager.performConcurrentJob(
		new PatternSearchJob(pattern, scope, IInfoConstants.NameInfo | IInfoConstants.PathInfo, searchRequestor, indexManager, progressMonitor),
		waitingPolicy,
		progressMonitor);	
}
/**
 * Searches for all declarations of the fields accessed in the given element.
 * The element can be a compilation unit, a type, or a method.
 * Reports the field declarations using the given collector.
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
 * @param workspace the workspace
 * @param enclosingElement the method, type, or compilation unit to be searched in
 * @param resultCollector a callback object to which each match is reported
 * @exception JavaModelException if the search failed. Reasons include:
 *	<ul>
 *		<li>the element doesn't exist
 *		<li>the classpath is incorrectly set
 *	</ul>
 */	
public void searchDeclarationsOfAccessedFields(IWorkspace workspace, IJavaElement enclosingElement, IJavaSearchResultCollector resultCollector) throws JavaModelException {
	MatchLocator locator = new MatchLocator(
		(SearchPattern)createSearchPattern("*", IJavaSearchConstants.FIELD, IJavaSearchConstants.REFERENCES, true),
		IInfoConstants.DeclarationInfo,
		resultCollector,
		new JavaWorkspaceScope());
	// TBD: limit search to type or method by passing start and end of enclosing element
	locator.locateMatches(
		new String[] {enclosingElement.getUnderlyingResource().getFullPath().toString()}, 
		workspace);
}
/**
 * Searches for all declarations of the types referenced in the given element.
 * The element can be a compilation unit, a type, or a method.
 * Reports the type declarations using the given collector.
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
 * <code>
 * then searching for declarations of referenced types in method <code>X.test()</code>
 * would collect the class <code>B</code> and the interface <code>I</code>.
 * </p>
 *
 * @param workspace the workspace
 * @param enclosingElement the method, type, or compilation unit to be searched in
 * @param resultCollector a callback object to which each match is reported
 * @exception JavaModelException if the search failed. Reasons include:
 *	<ul>
 *		<li>the element doesn't exist
 *		<li>the classpath is incorrectly set
 *	</ul>
 */	
public void searchDeclarationsOfReferencedTypes(IWorkspace workspace, IJavaElement enclosingElement, IJavaSearchResultCollector resultCollector) throws JavaModelException {
	MatchLocator locator = new MatchLocator(
		(SearchPattern)createSearchPattern("*", IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, true),
		IInfoConstants.DeclarationInfo,
		resultCollector,
		new JavaWorkspaceScope());
	// TBD: limit search to type or method by passing start and end of enclosing element
	locator.locateMatches(
		new String[] {enclosingElement.getUnderlyingResource().getFullPath().toString()}, 
		workspace);
}
/**
 * Searches for all declarations of the methods invoked in the given element.
 * The element can be a compilation unit, a type, or a method.
 * Reports the method declarations using the given collector.
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
 * @param workspace the workspace
 * @param enclosingElement the method, type, or compilation unit to be searched in
 * @param resultCollector a callback object to which each match is reported
 * @exception JavaModelException if the search failed. Reasons include:
 *	<ul>
 *		<li>the element doesn't exist
 *		<li>the classpath is incorrectly set
 *	</ul>
 */	
public void searchDeclarationsOfSentMessages(IWorkspace workspace, IJavaElement enclosingElement, IJavaSearchResultCollector resultCollector) throws JavaModelException {
	MatchLocator locator = new MatchLocator(
		(SearchPattern)createSearchPattern("*", IJavaSearchConstants.METHOD, IJavaSearchConstants.REFERENCES, true),
		IInfoConstants.DeclarationInfo,
		resultCollector,
		new JavaWorkspaceScope());
	// TBD: limit search to type or method by passing start and end of enclosing element
	locator.locateMatches(
		new String[] {enclosingElement.getUnderlyingResource().getFullPath().toString()}, 
		workspace);
}
}
