/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.external.annotations.loadertest;

/**
 * Code example used to test the annotation processor factory loader.
 */
@SuppressWarnings("nls")
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
