package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.jdom.*;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CreateMethodSourceExamplesTests extends AbstractJavaModelTests {
	
	IDOMFactory domFactory;

	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	
public CreateMethodSourceExamplesTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	this.domFactory = new DOMFactory();
}

public static Test suite() {
	return new Suite(CreateMethodSourceExamplesTests.class);
}
/**
 * Example of creating source for a method in an interface.
 */
public void testCreateEmptyInterfaceMethod() {
	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setBody(";" + LINE_SEPARATOR);
	assertEquals(
		"source code incorrect", 
		"public void foo();" + LINE_SEPARATOR,
		method.getContents());
}
/**
 * Example of creating a default empty method.
 */
public void testCreateEmptyMethod() {
	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	assertEquals(
		"source code incorrect", 
		"public void foo() {" + LINE_SEPARATOR + 
		"}" + LINE_SEPARATOR,
		method.getContents());
}
/**
 * Example of creating an abstract method.
 */
public void testCreateMethodWithAbstractModifier() {
	
	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setFlags(IConstants.AccPublic| IConstants.AccAbstract);
	assertEquals(
		"source code incorrect", 
		"public abstract void foo() {" + LINE_SEPARATOR + 
		"}" + LINE_SEPARATOR,
		method.getContents());
	
}
/**
 * Example of creating a method body.
 */
public void testCreateMethodWithBody() {

	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setBody(
		" {" + LINE_SEPARATOR + 
		"\t...method body..." + LINE_SEPARATOR + 
		"}" + LINE_SEPARATOR);
	assertEquals(
		"source code incorrect", 
		"public void foo() {" + LINE_SEPARATOR + 
		"\t...method body..." + LINE_SEPARATOR + 
		"}" + LINE_SEPARATOR,
		method.getContents());

}
/**
 * Example of creating a method with public and static modifiers.
 */
public void testCreateMethodWithModifiers() {

	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setFlags(IConstants.AccPublic| IConstants.AccStatic);
	assertEquals(
		"source code incorrect", 
		"public static void foo() {" + LINE_SEPARATOR + 
		"}" + LINE_SEPARATOR,
		method.getContents());

}
/**
 * Example of creating a method with modifiers and exceptions
 */
public void testCreateMethodWithModifiersAndExceptions() {
	
	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setFlags(IConstants.AccPrivate);
	method.setExceptions(new String[]
		{"java.lang.IllegalArgumentException",
		 "java.io.FileNotFoundExcpetion"});
	assertEquals(
		"source code incorrect", 
		"private void foo() throws java.lang.IllegalArgumentException, java.io.FileNotFoundExcpetion {" + LINE_SEPARATOR + 
		"}" + LINE_SEPARATOR,
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
	assertEquals(
		"source code incorrect", 
		"public void foo(String name, int number, char[] buffer) {" + LINE_SEPARATOR + 
		"}" + LINE_SEPARATOR,
		method.getContents());
}
/**
 * Example of creating a method with a return type
 */
public void testCreateMethodWithReturnType() {

	IDOMMethod method= this.domFactory.createMethod();
	method.setName("foo");
	method.setReturnType("String");
	assertEquals(
		"source code incorrect", 
		"public String foo() {" + LINE_SEPARATOR + 
		"}" + LINE_SEPARATOR,
		method.getContents());
}
}
