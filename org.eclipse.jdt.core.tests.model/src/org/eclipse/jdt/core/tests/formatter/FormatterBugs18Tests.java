/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import junit.framework.Test;
import org.eclipse.jdt.core.JavaModelException;

public class FormatterBugs18Tests extends FormatterRegressionTests {

public static Test suite() {
	return buildModelTestSuite(FormatterBugs18Tests.class);
}

public FormatterBugs18Tests(String name) {
	super(name);
}

/**
 * Create project and set the jar placeholder.
 */
public void setUpSuite() throws Exception {
	if (JAVA_PROJECT == null) {
		JAVA_PROJECT = setUpJavaProject("FormatterBugs", "1.8"); //$NON-NLS-1$
	}
	super.setUpSuite();
}

/**
 * @bug 426520: [1.8][formatter] inserts spaces into annotated qualified type
 * @test Ensure that formatting does not change the qualified type formatting for c and it
 * it removes the spaces for s. 
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=426520"
 */
public void testBug426520a() throws JavaModelException {
	String source =
		"import java.lang.annotation.*;" +
		"@Target(ElementType.TYPE_USE)\n" +
		"@interface T {}\n" +
		"public class X {\n" +
		"	@SuppressWarnings(\"rawtypes\")\n" +
		"	java.util.concurrent.@T Callable c;\n" +
		"	java.  util.  @T Set<java.lang.@T String> s;\n" +
		"}\n";
	formatSource(source,
			"import java.lang.annotation.*;\n\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface T {\n}\n\n" +
			"public class X {\n" +
			"	@SuppressWarnings(\"rawtypes\")\n" +
			"	java.util.concurrent.@T Callable c;\n" +
			"	java.util.@T Set<java.lang.@T String> s;\n" +
			"}\n");
}
public void testBug426520b() throws JavaModelException {
	String source =
		"import java.lang.annotation.*;" +
		"@Target(ElementType.TYPE_USE)\n" +
		"@interface T {}\n" +
		"public class X {\n" +
		"	@SuppressWarnings(\"rawtypes\")\n" +
		"	java.util.concurrent.@T()Callable c;\n" +
		"	java.util.@T()Set<java.lang.@T()String> s;\n" +
		"}\n";
	formatSource(source,
			"import java.lang.annotation.*;\n\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface T {\n}\n\n" +
			"public class X {\n" +
			"	@SuppressWarnings(\"rawtypes\")\n" +
			"	java.util.concurrent.@T() Callable c;\n" +
			"	java.util.@T() Set<java.lang.@T() String> s;\n" +
			"}\n");
}

}