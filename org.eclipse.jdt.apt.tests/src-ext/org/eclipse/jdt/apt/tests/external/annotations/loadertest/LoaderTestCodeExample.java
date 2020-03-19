/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.external.annotations.loadertest;

/**
 * Code example used to test the annotation processor factory loader.
 */
public class LoaderTestCodeExample {
	public static final String CODE_PACKAGE = "loadertestpackage";
	public static final String CODE_CLASS_NAME = "LoaderTestClass";
	public static final String CODE_FULL_NAME = CODE_PACKAGE + "." + CODE_CLASS_NAME;

	public static final String CODE =
		"package loadertestpackage;\n" +
		"import org.eclipse.jdt.apt.tests.external.annotations.loadertest.LoaderTestAnnotation;" + "\n" +
		"@LoaderTestAnnotation" + "\n" +
		"public class LoaderTestClass {" + "\n" +
		"    public static void SayHello() {" + "\n" +
		"        System.out.println(\"hello\");" + "\n" +
		"    }" + "\n" +
		"}";
}
