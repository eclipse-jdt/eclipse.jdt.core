/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.jdt.core.jdom.DOMFactory;
import org.eclipse.jdt.core.jdom.IDOMFactory;
import org.eclipse.jdt.core.jdom.IDOMMethod;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * @deprecated JDOM is obsolete
 */
public class CreateMethodSourceExamplesTests extends AbstractJavaModelTests {

	IDOMFactory domFactory;

public CreateMethodSourceExamplesTests(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	this.domFactory = new DOMFactory();
}

public static Test suite() {
	return buildModelTestSuite(CreateMethodSourceExamplesTests.class);
}
/**
 * Example of creating source for a method in an interface.
 */
public void testCreateEmptyInterfaceMethod() {
	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setBody(";\n");
	assertSourceEquals(
		"source code incorrect",
		"public void foo();\n",
		method.getContents());
}
/**
 * Example of creating a default empty method.
 */
public void testCreateEmptyMethod() {
	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	assertSourceEquals(
		"source code incorrect",
		"public void foo() {\n" +
		"}\n",
		method.getContents());
}
/**
 * Example of creating an abstract method.
 */
public void testCreateMethodWithAbstractModifier() {

	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setFlags(ClassFileConstants.AccPublic| ClassFileConstants.AccAbstract);
	assertSourceEquals(
		"source code incorrect",
		"public abstract void foo() {\n" +
		"}\n",
		method.getContents());

}
/**
 * Example of creating a method body.
 */
public void testCreateMethodWithBody() {

	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setBody(
		" {\n" +
		"\t...method body...\n" +
		"}\n");
	assertSourceEquals(
		"source code incorrect",
		"public void foo() {\n" +
		"\t...method body...\n" +
		"}\n",
		method.getContents());

}
/**
 * Example of creating a method with public and static modifiers.
 */
public void testCreateMethodWithModifiers() {

	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setFlags(ClassFileConstants.AccPublic| ClassFileConstants.AccStatic);
	assertSourceEquals(
		"source code incorrect",
		"public static void foo() {\n" +
		"}\n",
		method.getContents());

}
/**
 * Example of creating a method with modifiers and exceptions
 */
public void testCreateMethodWithModifiersAndExceptions() {

	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setFlags(ClassFileConstants.AccPrivate);
	method.setExceptions(new String[]
		{"java.lang.IllegalArgumentException",
		 "java.io.FileNotFoundExcpetion"});
	assertSourceEquals(
		"source code incorrect",
		"private void foo() throws java.lang.IllegalArgumentException, java.io.FileNotFoundExcpetion {\n" +
		"}\n",
		method.getContents());

}
/**
 * Example of creating a method with parameters
 */
public void testCreateMethodWithParameters() {

	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setParameters(
		new String[] {"String", "int", "char[]"},
		new String[] {"name", "number", "buffer"});
	assertSourceEquals(
		"source code incorrect",
		"public void foo(String name, int number, char[] buffer) {\n" +
		"}\n",
		method.getContents());
}
/**
 * Example of creating a method with a return type
 */
public void testCreateMethodWithReturnType() {

	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setReturnType("String");
	assertSourceEquals(
		"source code incorrect",
		"public String foo() {\n" +
		"}\n",
		method.getContents());
}
}
