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
package org.eclipse.jdt.core.compiler;

/**
 * Richer description of a Java problem, as detected by the compiler or some of the underlying
 * technology reusing the compiler. With the introduction of <code>CompilationParticipant</code>,
 * the simpler problem interface <code>IProblem</code> did not carry enough information to better
 * separate and categorize Java problems. In order to minimize impact on existing API, Java problems
 * are still passed around as <code>IProblem</code>, though actual implementations should explicitly
 * extend <code>CategorizedProblem</code>. Participants can produce their own problem definitions,
 * and given these are categorized problems, they can be better handled by clients (such as user
 * interface).
 * 
 * A categorized problem provides access to:
 * <ul>
 * <li> its location (originating source file name, source position, line number), </li>
 * <li> its message description and a predicate to check its severity (warning or error). </li>
 * <li> its ID : a number identifying the very nature of this problem. All possible IDs for standard Java 
 * problems are listed as constants on <code>IProblem</code>, </li>
 * <li> its marker type : a string identfying the problem creator. It corresponds to the marker type
 * chosen if this problem was to be persisted. Standard Java problems are associated to marker
 * type "org.eclipse.jdt.core.problem"), standard tasks are associated to marker type 
 * "org.eclipse.jdt.core.task", </li>
 * <li> its category ID : a number identifying the category this problem belongs to. All possible IDs for 
 * standard Java problem categories are listed in this class. </li>
 * </ul>
 * 
 * Note: the compiler produces IProblems internally, which are turned into markers by the JavaBuilder
 * so as to persist problem descriptions. This explains why there is no API allowing to reach IProblem detected
 * when compiling. However, the Java problem markers carry equivalent information to IProblem, in particular
 * their ID (attribute "id") is set to one of the IDs defined on this interface.
 * 
 * Note: Standard Java problems produced by Java default tooling will be subclasses of this class. Technically, most
 * API methods dealing with problems are referring to <code>IProblem</code> for backward compatibility reason.
 * It is intended that <code>CategorizedProblem</code> will be subclassed for custom problem implementation when
 * participating in compilation operations, so as to allow participant to contribute their own marker types, and thus
 * defining their own domain specific problem/category IDs.
 * 
 * @see CompilationParticipant
 * @since 3.2
 */
public abstract class CategorizedProblem implements IProblem {
	
	/**
	 * List of standard category IDs used by Java problems, more categories will be added 
	 * in the future.
	 */
	public static final int CAT_UNSPECIFIED = 0;
	public static final int CAT_BUILDPATH = 10;
	public static final int CAT_SYNTAX = 20;
	public static final int CAT_IMPORT = 30;
	public static final int CAT_TYPE = 40;
	public static final int CAT_MEMBER = 50;
	public static final int CAT_JAVADOC = 60;
	public static final int CAT_CODE_STYLE = 70;
	public static final int CAT_POTENTIAL_PROGRAMMING_PROBLEM = 80;
	public static final int CAT_NAME_SHADOWING_CONFLICT = 90;
	public static final int CAT_DEPRECATION = 100;
	public static final int CAT_UNNECESSARY_CODE = 110;
	public static final int CAT_UNCHECKED_RAW = 120;
	public static final int CAT_NLS = 130;
	public static final int CAT_RESTRICTION = 140;	
	
	
/** 
 * Returns an integer identifying the category of this problem. Categories, like problem IDs are
 * defined in the context of some marker type. Custom implementations of <code>CategorizedProblem</code>
 * may choose arbitrary values for problem/category IDs, as long as they are associated with a different
 * marker type.
 * Standard Java problem markers (i.e. marker type is "org.eclipse.jdt.core.problem") carry an
 * attribute "categoryId" persisting the originating problem category ID as defined by this method).
 * @return id - an integer identifying the category of this problem
 */
public abstract int getCategoryID();

/**
 * Returns the marker type associated to this problem, if it was persisted into a marker by the JavaBuilder
 * Standard Java problems are associated to marker type "org.eclipse.jdt.core.problem"), standard tasks 
 * are associated to marker type "org.eclipse.jdt.core.task".
 * 
 * @return the type of the marker which would be associated to the problem
 */
public abstract String getMarkerType();
}
