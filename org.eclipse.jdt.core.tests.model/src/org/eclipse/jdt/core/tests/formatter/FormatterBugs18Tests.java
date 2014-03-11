/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public void testBug425040() throws JavaModelException {
	String source =
			"import java.lang.annotation.*;\n" +
			"\n" +
			"public class X extends @Annot1 Object {\n" +
			"	@Deprecated	@Annot3 public @Annot2	int b;\n" +
			"\n" +
			"	@SuppressWarnings(\"unused\")\n" +
			"	public @Annot3() int foo(@Annot4 C<@Annot5() Object> a) {\n" +
			"		@Annot1 int @Annot2 [] i;\n" +
			"		return 0;\n" +
			"	}\n" +
			"}\n" +
			"class C<T> {}\n" +
			"@Documented\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot1 {}\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot2 {}\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot3 {}\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot4 {}\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot5 {}\n";
	formatSource(source,
			"import java.lang.annotation.*;\n" +
			"\n" +
			"public class X extends @Annot1 Object {\n" +
			"	@Deprecated\n" +
			"	@Annot3\n" +
			"	public @Annot2 int b;\n" +
			"\n" +
			"	@SuppressWarnings(\"unused\")\n" +
			"	public @Annot3() int foo(@Annot4 C<@Annot5() Object> a) {\n" +
			"		@Annot1\n" +
			"		int @Annot2 [] i;\n" +
			"		return 0;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class C<T> {\n" +
			"}\n" +
			"\n" +
			"@Documented\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot1 {\n" +
			"}\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot2 {\n" +
			"}\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot3 {\n" +
			"}\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot4 {\n" +
			"}\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot5 {\n" +
			"}\n"
			);
}

}