/*******************************************************************************
 * Copyright (c) 2006, 2007 BEA Systems, Inc.
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
 *
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests.annotations;

/**
 * Processing this annotation will produce a class whose name is the value of
 * <code>clazz</code>, with a method whose name is the value of
 * <code>method</code> and whose return type is <code>String</code>.
 * <p>
 * If {@code warn == true}, the processor produces source code that will cause a
 * warning about an unused variable when compiled (if the warning is enabled).
 * </p>
 * 
 * @see org.eclipse.jdt.compiler.apt.tests.processors.genclass.GenClassProc
 */
public @interface GenClass {
	String clazz();
	String method();
	boolean warn() default false;
}
