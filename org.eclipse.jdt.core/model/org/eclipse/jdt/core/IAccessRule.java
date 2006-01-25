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
package org.eclipse.jdt.core;

import org.eclipse.core.runtime.IPath;

/**
 * Describes an access rule to source and class files on a classpath entry.
 * An access rule is composed of a file pattern and a kind (accessible,
 * non accessible, or discouraged).
 * <p>
 * On a given classpath entry, the access rules are considered in the order given
 * when the entry was created. When a source or class file matches an access 
 * rule's pattern, the access rule's kind define whether the file is considered
 * accessible, non accessible, or its access is discouraged. If the source or class
 * file doesn't match any accessible rule, it is considered accessible. A source or class 
 * file that is not accessible or discouraged can still be refered to but it is tagged as being not 
 * accessible - the Java builder will create a problem marker for example. 
 * The severity of the marker created from a non accessible rule is controled through
 * the {@link JavaCore#COMPILER_PB_FORBIDDEN_REFERENCE} compiler option.
 * The severity of the marker created from a discouraged rule is controled through
 * the {@link JavaCore#COMPILER_PB_DISCOURAGED_REFERENCE} compiler option.
 * Note this is different from inclusion and exclusion patterns on source classpath entries, 
 * where a source file that is excluded is not even compiled.
 * Files patterns look like relative file paths with wildcards and are interpreted relative 
 * to each entry's path.
 * File patterns are case-sensitive and they can contain '**', '*' or '?' wildcards (see 
 * {@link IClasspathEntry#getExclusionPatterns()} for the full description
 * of their syntax and semantics).
 * Note that file patterns must not include the file extension. 
 * <code>com/xyz/tests/MyClass</code> is a valid file pattern, whereas 
 * <code>com/xyz/tests/MyClass.class</code> is not valid.
 * </p>
 * <p>
 * For example, if one of the entry path is <code>/Project/someLib.jar</code>, 
 * there are no accessible rules, and there is one non accessible rule whith pattern 
 * <code>com/xyz/tests/&#42;&#42;</code>, then class files
 * like <code>/Project/someLib.jar/com/xyz/Foo.class</code>
 * and <code>/Project/someLib.jar/com/xyz/utils/Bar.class</code> would be accessible,
 * whereas <code>/Project/someLib.jar/com/xyz/tests/T1.class</code>
 * and <code>/Project/someLib.jar/com/xyz/tests/quick/T2.class</code> would not be
 * accessible. 
 * </p>
 * 
 * @since 3.1
 */
public interface IAccessRule {
	
	/**
	 * Constant indicating that files matching the rule's pattern are accessible.
	 */
	int K_ACCESSIBLE = 0;
	
	/**
	 * Constant indicating that files matching the rule's pattern are non accessible.
	 */
	int K_NON_ACCESSIBLE = 1;

	/**
	 * Constant indicating that access to the files matching the rule's pattern is discouraged.
	 */
	int K_DISCOURAGED = 2;

	/**
	 * Flag indicating that the rule should be ignored if a better rule is found on 
	 * another classpath entry.
	 * E.g. if a rule K_NON_ACCESSIBLE | IGNORE_IF_BETTER matches type p.X on
	 * a library entry 'lib1' and a rule K_DISCOURAGED that also matches p.X is 
	 * found on library entry 'lib2' - 'lib2' being after 'lib1' on the classpath,
	 * then p.X will be reported as discouraged.
	 * 
	 * @since 3.2
	 */
	int IGNORE_IF_BETTER = 0x100;

	/**
	 * Returns the file pattern for this access rule.
	 * 
	 * @return the file pattern for this access rule
	 */
	IPath getPattern();
	
	/**
	 * Returns the kind of this access rule (one of {@link #K_ACCESSIBLE}, {@link #K_NON_ACCESSIBLE}
	 * or {@link #K_DISCOURAGED}).
	 * 
	 * @return the kind of this access rule
	 */
	int getKind();
	
	/**
	 * Returns whether the rule should be ignored if a better rule is found on 
	 * another classpath entry.
	 * E.g. if a rule K_NON_ACCESSIBLE | IGNORE_IF_BETTER matches type p.X on
	 * a library entry 'lib1' and a rule K_DISCOURAGED that also matches p.X is 
	 * found on library entry 'lib2' - 'lib2' being after 'lib1' on the classpath,
	 * then p.X will be reported as discouraged.
	 * 
	 * @return whether the rule should be ignored if a better rule is found on
	 *         another classpath entry
	 * @since 3.2
	 */
	boolean ignoreIfBetter();

}
