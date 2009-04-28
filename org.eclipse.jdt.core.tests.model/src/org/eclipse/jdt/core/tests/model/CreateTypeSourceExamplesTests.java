/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.core.jdom.*;

import junit.framework.Test;

/**
 * @deprecated JDOM is obsolete
 */
public class CreateTypeSourceExamplesTests extends AbstractJavaModelTests {
	IDOMFactory domFactory;

public CreateTypeSourceExamplesTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(CreateTypeSourceExamplesTests.class);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	this.domFactory = new DOMFactory();
}
/**
 * Example of creating a class with an extends clause
 */
public void testCreateClassWithExtends() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	type.setSuperclass("Bar");
	assertSourceEquals(
		"source code incorrect",
		"public class Foo extends Bar {\n" +
		"}\n",
		type.getContents());
}
/**
 * Example of creating a class with an implements clause.
 */
public void testCreateClassWithImplements() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	type.setSuperInterfaces(new String[] {"ISomething", "IOtherwise"});
	assertSourceEquals(
		"source code incorrect",
		"public class Foo implements ISomething, IOtherwise {\n" +
		"}\n",
		type.getContents());
}

/**
 * Example of creating a class with an implements clause.
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10979
 */
public void testCreateClassWithImplements2() {
	IDOMType type= this.domFactory.createType("class A implements I1 {\n}");
	type.addSuperInterface("I2");
	assertSourceEquals(
		"source code incorrect",
		"class A implements I1, I2 {\n}",
		type.getContents());
}

/**
 * Example of creating a class with an implements clause.
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10979
 */
public void testCreateClassWithImplements3() {
	IDOMType type= this.domFactory.createType("class A {\n}");
	type.setSuperInterfaces(new String[] {"I1", "I2"});
	assertSourceEquals(
		"source code incorrect",
		"class A implements I1, I2 {\n}",
		type.getContents());
}

/**
 * Example of creating a class with an implements clause.
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10979
 */
public void testCreateClassWithImplements4() {
	IDOMType type= this.domFactory.createType("class A implements I1{\n}");
	type.addSuperInterface("I2");
	assertSourceEquals(
		"source code incorrect",
		"class A implements I1, I2{\n}",
		type.getContents());
}

/**
 * Example of creating a class with modifiers
 */
public void testCreateClassWithModifiers() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	type.setFlags(ClassFileConstants.AccPublic | ClassFileConstants.AccFinal);
	assertSourceEquals(
		"source code incorrect",
		"public final class Foo {\n" +
		"}\n",
		type.getContents());
}
/**
 * Example of creating a default class
 */
public void testCreateEmptyClass() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	assertSourceEquals(
		"source code incorrect",
		"public class Foo {\n" +
		"}\n",
		type.getContents());
}
/**
 * Ensures that an interface is created using
 * <code>CreateTypeSourceOperation</code> and that the source
 * of the created interface is correct.
 */
public void testCreateEmptyInterface() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	type.setClass(false);
	assertSourceEquals(
		"source code incorrect",
		"public interface Foo {\n" +
		"}\n",
		type.getContents());
}
}
