/*******************************************************************************
 * Copyright (c) 2007-2009 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.annotations;

/**
 * This annotation may be processed to generate a
 * specified Java class.
 */
public @interface GenClass6 {
	/**
	 * Name of class to generate
	 */
	String name();

	/**
	 * Name of package in which to place class
	 * (empty for default package)
	 */
	String pkg() default "";

	/**
	 * Name of method to create
	 * (empty for no method creation)
	 */
	String method() default "";

	/**
	 * Produce a summary .txt file which contains the names of all generated classes
	 * that specify this flag.
	 */
	boolean summary() default false;

	/**
	 * Number of additional rounds to induce. If the number is greater than one, a GenClass6
	 * annotation will be added to the produced class, and given a rounds() value one less than the
	 * value specified here and an appropriately suffixed name() value. Thus, specifying rounds=2
	 * and name=Foo will produce Foo, which will be annotated with rounds=1 and name=Foo1, which
	 * will produce Foo1, which will not be annotated.
	 */
	int rounds() default 1;

	/**
	 * Processor-specific options
	 */
	String[] options() default {};
}
