/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.*;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SignatureTests extends AbstractJavaModelTests {
public SignatureTests(String name) {
	super(name);
}
/**
 * Ensures that creating an invalid type signature throws an IllegalArgumentException or return the expected signature.
 */
protected void assertInvalidTypeSignature(String typeName, boolean isResolved, String expected) throws JavaModelException {
	String actual;
	try {
		actual = Signature.createTypeSignature(typeName, isResolved);
	} catch (IllegalArgumentException e) {
		return;
	}
	assertEquals(expected, actual);
}
public static Test suite() {
	return new TestSuite(SignatureTests.class);
}
/**
 * @see Signature
 */
public void testCreateArraySignature() {
	assertEquals(
		"Signature#createArraySignature not correct", 
		"[[[[QString",
		Signature.createArraySignature("QString", 4));
}
/**
 * @see Signature
 */
public void testCreateMethodSignature() throws JavaModelException {
	assertEquals(
		"Signature#createMethodSignature is not correct 1", 
		"(QString;QObject;I)I", 
		Signature.createMethodSignature(new String[] {"QString;", "QObject;", "I"}, "I"));
	assertEquals(
		"Signature#createMethodSignature is not correct 2", 
		"()Ljava.lang.String;", 
		Signature.createMethodSignature(new String[] {}, "Ljava.lang.String;"));

}
/**
 * @see Signature
 */
public void testCreateTypeSignature() throws JavaModelException {
	assertEquals("Signature#createTypeSignature is not correct1", "I", Signature.createTypeSignature("int".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct2", "Ljava.lang.String;", Signature.createTypeSignature("java.lang.String".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct3", "QString;", Signature.createTypeSignature("String".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct4", "Qjava.lang.String;", Signature.createTypeSignature("java.lang.String".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct5", "[I", Signature.createTypeSignature("int []".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct6", "[QString;", Signature.createTypeSignature("String []".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct7", "[Ljava.util.Vector;", Signature.createTypeSignature("java.util.Vector []".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct8", "[[Ljava.util.Vector;", Signature.createTypeSignature("java .\n util  .  Vector[  ][]".toCharArray(), true));
}
/**
 * Ensures that creating an invalid type signature throws an IllegalArgumentException.
 */
public void testCreateInvalidTypeSignature() throws JavaModelException {
	assertInvalidTypeSignature(null, false, null);
	assertInvalidTypeSignature("", false, "");
	assertInvalidTypeSignature("int.Y", false, "I");
	assertInvalidTypeSignature("Y [].X", false, "[QY;");
	assertInvalidTypeSignature("X[[]", true, "[[LX;");
}
/**
 * @see Signature
 */
public void testGetArrayCount() {
	assertTrue("Signature#getArrayCount is not correct", Signature.getArrayCount("[[[[QString;") == 4);
	try {
		Signature.getArrayCount("");
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Signature#getArrayCount is not correct, exception", false);
}
/**
 * @see Signature
 */
public void testGetElementType() {
	assertTrue("Signature#getElementType is not correct1", Signature.getElementType("[[[[QString;").equals("QString;"));
	assertTrue("Signature#getElementType is not correct2", Signature.getElementType("QString;").equals("QString;"));
	assertTrue("Signature#getElementType is not correct2", Signature.getElementType("[[I").equals("I"));
	try {
		Signature.getElementType("");
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Signature#getArrayCount is not correct, exception", false);
}
/**
 * @see Signature
 */
public void testGetParameterCount() throws JavaModelException {
	String methodSig = "(QString;QObject;I)I";
	assertTrue("Signature#getParameterCount is not correct1", Signature.getParameterCount(methodSig) == 3);
	try {
		Signature.getParameterCount("");
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Signature#getParameterCount is not correct: exception", false);
}
/**
 * @see Signature
 */
public void testGetParameterTypes() throws JavaModelException {
	String methodSig = "(QString;QObject;I)I";
	String[] types= Signature.getParameterTypes(methodSig);
	assertTrue("Signature#getParameterTypes is not correct1", types.length == 3);
	assertTrue("Signature#getParameterTypes is not correct2", types[1].equals("QObject;"));
	try {
		Signature.getParameterTypes("");
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Signature#getParameterTypes is not correct: exception", false);
}
/**
 * @see Signature
 */
public void testGetQualifier() throws JavaModelException {
	assertTrue("Signature#getQualifier is not correct1", Signature.getQualifier("java.lang.Object").equals("java.lang"));
	assertTrue("Signature#getQualifier is not correct2",  Signature.getQualifier("").equals(""));
	
}
/**
 * @see Signature
 */
public void testGetReturnType() throws JavaModelException {
	String methodSig = "(QString;QObject;I)I";
	assertTrue("Signature#getReturnType is not correct1", Signature.getReturnType(methodSig).equals("I"));
	try {
		Signature.getReturnType("");
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Signature#getReturnType is not correct: exception", false);
}
/**
 * @see Signature
 */
public void testGetSimpleName() throws JavaModelException {
	assertTrue("Signature#getSimpleName is not correct1", Signature.getSimpleName("java.lang.Object").equals("Object"));
	assertTrue("Signature#getSimpleName is not correct2",  Signature.getSimpleName("").equals(""));
}
/**
 * @see Signature
 */
public void testGetSimpleNames() throws JavaModelException {
	String[] simpleNames = Signature.getSimpleNames("java.lang.Object");
	assertTrue("Signature#getSimpleNames is not correct1", simpleNames.length == 3 && simpleNames[2].equals("Object"));
	simpleNames = Signature.getSimpleNames("");
	assertTrue("Signature#getSimpleNames is not correct2", simpleNames.length == 0);
	simpleNames = Signature.getSimpleNames("Object");
	assertTrue(" Signature #getSimpleNames is not correct3 ", simpleNames.length == 1 && simpleNames[0].equals("Object"));
}
/**
 * @see Signature
 */
public void testToQualifiedName() throws JavaModelException {
	assertTrue("Signature#toQualifiedName is not correct1", Signature.toQualifiedName(new String[] {"java", "lang", "Object"}).equals("java.lang.Object"));
	assertTrue("Signature#toQualifiedName is not correct2", Signature.toQualifiedName(new String[] {"Object"}).equals("Object"));
	assertTrue("Signature#toQualifiedName is not correct3", Signature.toQualifiedName(new String[0]).equals(""));
}
/**
 * @see Signature.toString(String)
 */
public void testToString1() throws JavaModelException {
	assertEquals(
		"Signature#toString is not correct 1", 
		"java.lang.String",
		Signature.toString("Ljava.lang.String;"));
	assertEquals(
		"Signature#toString is not correct 2", 
		"java.lang.String[]",
		Signature.toString("[Ljava.lang.String;"));
	assertEquals(
		"Signature#toString is not correct 3", 
		"String",
		Signature.toString("QString;"));
	assertEquals(
		"Signature#toString is not correct 4", 
		"String[][]",
		Signature.toString("[[QString;"));
	assertEquals(
		"Signature#toString is not correct 5", 
		"boolean",
		Signature.toString("Z"));
	assertEquals(
		"Signature#toString is not correct 6", 
		"byte",
		Signature.toString("B"));
	assertEquals(
		"Signature#toString is not correct 7", 
		"char",
		Signature.toString("C"));
	assertEquals(
		"Signature#toString is not correct 8", 
		"double",
		Signature.toString("D"));
	assertEquals(
		"Signature#toString is not correct 9", 
		"float",
		Signature.toString("F"));
	assertEquals(
		"Signature#toString is not correct 10", 
		"int",
		Signature.toString("I"));
	assertEquals(
		"Signature#toString is not correct 11", 
		"long",
		Signature.toString("J"));
	assertEquals(
		"Signature#toString is not correct 12", 
		"short",
		Signature.toString("S"));
	assertEquals(
		"Signature#toString is not correct 13", 
		"void",
		Signature.toString("V"));
	assertEquals(
		"Signature#toString is not correct 14", 
		"int[][][]",
		Signature.toString("[[[I"));
}
/**
 * @see Signature.toString(String, String, String[], boolean, boolean)
 */
public void testToString2() throws JavaModelException {
	assertEquals(
		"Signature#toString is not correct 1", 
		"void main(String[] args)",
		Signature.toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, true));
	assertEquals(
		"Signature#toString is not correct 2", 
		"main(String[] args)",
		Signature.toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, false));
	assertEquals(
		"Signature#toString is not correct 3", 
		"main(java.lang.String[] args)",
		Signature.toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, true, false));
	assertEquals(
		"Signature#toString is not correct 4", 
		"(java.lang.String[])",
		Signature.toString("([Ljava.lang.String;)V", null, null, true, false));
	assertEquals(
		"Signature#toString is not correct 5", 
		"String main(String[] args)",
		Signature.toString("([Ljava.lang.String;)Ljava.lang.String;", "main", new String[] {"args"}, false, true));
	assertEquals(
		"Signature#toString is not correct 6", 
		"java.lang.String main(java.lang.String[] args)",
		Signature.toString("([Ljava.lang.String;)Ljava.lang.String;", "main", new String[] {"args"}, true, true));
	assertEquals(
		"Signature#toString is not correct 7", 
		"java.lang.String main(java.lang.String[] args)",
		Signature.toString("main([Ljava.lang.String;)Ljava.lang.String;", "main", new String[] {"args"}, true, true));
	assertEquals(
		"Signature#toString is not correct 8", 
		"java.lang.String[] foo()",
		Signature.toString("()[Ljava.lang.String;", "foo", null, true, true));
	assertEquals(
		"Signature#toString is not correct 9", 
		"I foo(C, L)",
		Signature.toString("(LC;LL;)LI;", "foo", null, true, true));
	assertEquals(
		"Signature#toString is not correct 10", 
		"char[][] foo()",
		Signature.toString("()[[C", "foo", null, true, true));
	assertEquals(
		"Signature#toString is not correct 11", 
		"void foo(java.lang.Object, String[][], boolean, byte, char, double, float, int, long, short)",
		Signature.toString("(Ljava.lang.Object;[[QString;ZBCDFIJS)V", "foo", null, true, true));
	try {
		Signature.toString("([Ljava.lang.String;V", null, null, true, false);
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Signature#toString is not correct 12: should get an exception", false);
}

/**
 * Test the toString() signature of an inner type.
 */
public void testToStringInnerType() throws JavaModelException {
	assertEquals(
		"Signature#toString is not correct", 
		"x.y.A.Inner",
		Signature.toString("Lx.y.A$Inner;"));
}
}
