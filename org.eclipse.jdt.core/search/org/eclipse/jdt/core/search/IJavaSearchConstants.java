/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.search;

import org.eclipse.jdt.internal.core.search.processing.*;

/**
 * <p>
 * This interface defines the constants used by the search engine.
 * </p>
 * <p>
 * This interface declares constants only; it is not intended to be implemented.
 * </p>
 * @see org.eclipse.jdt.core.search.SearchEngine
 */
public interface IJavaSearchConstants {

	/**
	 * The nature of searched element or the nature
	 * of match in unknown.
	 */
	int UNKNOWN = -1;
	
	/* Nature of searched element */
	
	/**
	 * The searched element is a type, which may include classes, interfaces,
	 * enums, and annotation types.
	 */
	int TYPE= 0;

	/**
	 * The searched element is a method.
	 */
	int METHOD= 1;

	/**
	 * The searched element is a package.
	 */
	int PACKAGE= 2;

	/**
	 * The searched element is a constructor.
	 */
	int CONSTRUCTOR= 3;

	/**
	 * The searched element is a field.
	 */
	int FIELD= 4;

	/**
	 * The searched element is a class. 
	 * More selective than using {@link #TYPE}.
	 */
	int CLASS= 5;

	/**
	 * The searched element is an interface.
	 * More selective than using {@link #TYPE}.
	 */
	int INTERFACE= 6;

	/**
	 * The searched element is an enum.
	 * More selective than using {@link #TYPE}.
	 * @since 3.1
	 */
	int ENUM= 7;

	/**
	 * The searched element is an annotation type.
	 * More selective than using {@link #TYPE}.
	 * @since 3.1
	 */
	int ANNOTATION_TYPE= 8;

	/**
	 * The searched element is a class or enum type.
	 * More selective than using {@link #TYPE}.
	 * @since 3.1
	 */
	int CLASS_AND_ENUM= 9;

	/**
	 * The searched element is a class or interface type.
	 * More selective than using {@link #TYPE}.
	 * @since 3.1
	 */
	int CLASS_AND_INTERFACE= 10;
	
	/**
	 * The searched element is an interface or annotation type.
	 * More selective than using {@link #TYPE}.
	 * @since 3.3
	 */
	int INTERFACE_AND_ANNOTATION= 11;

	/* Nature of match */

	/**
	 * The search result is a declaration.
	 * Can be used in conjunction with any of the nature of searched elements
	 * so as to better narrow down the search.
	 */
	int DECLARATIONS= 0;

	/**
	 * The search result is a type that implements an interface or extends a class. 
	 * Used in conjunction with either TYPE or CLASS or INTERFACE, it will
	 * respectively search for any type implementing/extending a type,
	 * or rather exclusively search for classes implementing/extending the type, or
	 * interfaces extending the type.
	 */
	int IMPLEMENTORS= 1;

	/**
	 * The search result is a reference.
	 * Can be used in conjunction with any of the nature of searched elements
	 * so as to better narrow down the search.
	 * References can contain implementers since they are more generic kind
	 * of matches.
	 */
	int REFERENCES= 2;

	/**
	 * The search result is a declaration, a reference, or an implementer 
	 * of an interface.
	 * Can be used in conjunction with any of the nature of searched elements
	 * so as to better narrow down the search.
	 */
	int ALL_OCCURRENCES= 3;

	/**
	 * When searching for field matches, it will exclusively find read accesses, as
	 * opposed to write accesses. Note that some expressions are considered both
	 * as field read/write accesses: for example, x++; x+= 1;
	 * 
	 * @since 2.0
	 */
	int READ_ACCESSES = 4;
	
	/**
	 * When searching for field matches, it will exclusively find write accesses, as
	 * opposed to read accesses. Note that some expressions are considered both
	 * as field read/write accesses: for example,  x++; x+= 1;
	 * 
	 * @since 2.0
	 */
	int WRITE_ACCESSES = 5;

	/**
	 * Ignore declaring type while searching result.
	 * Can be used in conjunction with any of the nature of match.
	 * @since 3.1
	 */
	int IGNORE_DECLARING_TYPE = 0x10;

	/**
	 * Ignore return type while searching result.
	 * Can be used in conjunction with any of the nature of match.
	 * Note that:
	 * <ul>
	 * 	<li>for fields search, pattern will ignore field type</li>
	 * 	<li>this flag will have no effect for types search</li>
	 *	</ul>
	 * @since 3.1
	 */
	int IGNORE_RETURN_TYPE = 0x20;
	
	/* Syntactic match modes */
	
	/**
	 * The search pattern matches exactly the search result,
	 * that is, the source of the search result equals the search pattern.
	 * @deprecated Use {@link SearchPattern#R_EXACT_MATCH} instead.
	 */
	int EXACT_MATCH = 0;
	/**
	 * The search pattern is a prefix of the search result.
	 * @deprecated Use {@link SearchPattern#R_PREFIX_MATCH} instead.
	 */
	int PREFIX_MATCH = 1;
	/**
	 * The search pattern contains one or more wild cards ('*') where a 
	 * wild-card can replace 0 or more characters in the search result.
	 * @deprecated Use {@link SearchPattern#R_PATTERN_MATCH} instead.
	 */
	int PATTERN_MATCH = 2;


	/* Case sensitivity */
	
	/**
	 * The search pattern matches the search result only
	 * if cases are the same.
	 * @deprecated Use the methods that take the matchMode
	 *   with {@link SearchPattern#R_CASE_SENSITIVE} as a matchRule instead.
	 */
	boolean CASE_SENSITIVE = true;
	/**
	 * The search pattern ignores cases in the search result.
	 * @deprecated Use the methods that take the matchMode
	 *   without {@link SearchPattern#R_CASE_SENSITIVE} as a matchRule instead.
	 */
	boolean CASE_INSENSITIVE = false;
	

	/* Waiting policies */
	
	/**
	 * The search operation starts immediately, even if the underlying indexer
	 * has not finished indexing the workspace. Results will more likely
	 * not contain all the matches.
	 */
	int FORCE_IMMEDIATE_SEARCH = IJob.ForceImmediate;
	/**
	 * The search operation throws an <code>org.eclipse.core.runtime.OperationCanceledException</code>
	 * if the underlying indexer has not finished indexing the workspace.
	 */
	int CANCEL_IF_NOT_READY_TO_SEARCH = IJob.CancelIfNotReady;
	/**
	 * The search operation waits for the underlying indexer to finish indexing 
	 * the workspace before starting the search.
	 */
	int WAIT_UNTIL_READY_TO_SEARCH = IJob.WaitUntilReady;
	
	
}
