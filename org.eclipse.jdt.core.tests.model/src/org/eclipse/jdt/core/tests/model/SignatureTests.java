/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
protected void assertInvalidTypeSignature(String typeName, boolean isResolved, String expected) {
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
		
	// tests with 1.5-specific elements
	assertEquals(
		"Signature#createArraySignature not correct", 
		"[[[[Qlist<Qstring;>;",
		Signature.createArraySignature("Qlist<Qstring;>;", 4));
		
}
/**
 * @see Signature
 */
public void testCreateMethodSignature() {
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
public void testCreateTypeSignature() {
	assertEquals("Signature#createTypeSignature is not correct1", "I",
			Signature.createTypeSignature("int".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct2", "Ljava.lang.String;",
			Signature.createTypeSignature("java.lang.String".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct3", "QString;", 
			Signature.createTypeSignature("String".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct4", "Qjava.lang.String;", 
			Signature.createTypeSignature("java.lang.String".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct5", "[I",
			Signature.createTypeSignature("int []".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct6", "[QString;",
			Signature.createTypeSignature("String []".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct7", "[Ljava.util.Vector;",
			Signature.createTypeSignature("java.util.Vector []".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct8", "[[Ljava.util.Vector;",
			Signature.createTypeSignature("java .\n util  .  Vector[  ][]".toCharArray(), true));
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=41019
	assertEquals("Signature#createTypeSignature is not correct9", "Linteration.test.MyData;",
			Signature.createTypeSignature("interation.test.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct10", "Llongtest.MyData;",
			Signature.createTypeSignature("longtest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct11", "Lbooleantest.MyData;",
			Signature.createTypeSignature("booleantest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct12", "Lbytetest.MyData;",
			Signature.createTypeSignature("bytetest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct13", "Lchartest.MyData;",
			Signature.createTypeSignature("chartest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct14", "Lshorttest.MyData;",
			Signature.createTypeSignature("shorttest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct15", "Ldoubletest.MyData;",
			Signature.createTypeSignature("doubletest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct16", "Lfloattest.MyData;",
			Signature.createTypeSignature("floattest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct17", "Lvoidtest.MyData;",
			Signature.createTypeSignature("voidtest.MyData".toCharArray(), true));
}
/**
 * Ensures that creating an invalid type signature throws an IllegalArgumentException.
 */
public void testCreateInvalidTypeSignature() {
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
	assertEquals("Signature#getArrayCount is not correct", 4,
			Signature.getArrayCount("[[[[QString;"));
	try {
		Signature.getArrayCount("");
		assertTrue("Signature#getArrayCount is not correct, exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}

	// tests with 1.5-specific elements
	assertEquals(
		"Signature#getArrayCount not correct", 4,
		Signature.getArrayCount("[[[[Qlist<Qstring;>;"));
}

/**
 * @see Signature
 */
public void testGetElementType() {
	assertEquals("Signature#getElementType is not correct1", "QString;",
			Signature.getElementType("[[[[QString;"));
	assertEquals("Signature#getElementType is not correct2", "QString;",
			Signature.getElementType("QString;"));
	assertEquals("Signature#getElementType is not correct2", "I",
			Signature.getElementType("[[I"));
	try {
		Signature.getElementType("");
		assertTrue("Signature#getArrayCount is not correct, exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}
	
	// tests with 1.5-specific elements
	assertEquals(
		"Signature#getElementType not correct", "Qlist<Qstring;>;",
		Signature.getElementType("[[[[Qlist<Qstring;>;"));
}
/**
 * @see Signature
 */
public void testGetParameterCount() {
	String methodSig = "(QString;QObject;I)I";
	assertEquals("Signature#getParameterCount is not correct1", 3,
			Signature.getParameterCount(methodSig));
	try {
		Signature.getParameterCount("");
		assertTrue("Signature#getParameterCount is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}

	// tests with 1.5-specific elements
	methodSig = "<X:Qlist<Qstring;>;>(IQlist;Tww;)Qlist<Qxxx;>;^Qexception;^Qerror;";
	assertEquals("Signature#getParameterCount is not correct3", 3,
			Signature.getParameterCount(methodSig));
	methodSig = "<X:Qlist<Qstring;>;>(IQlist<Qstring;>;Tww;)Qlist<Qxxx;>;^Qexception;^Qerror;";
	assertEquals("Signature#getParameterCount is not correct4", 3,
			Signature.getParameterCount(methodSig));
}
/**
 * @see Signature
 */
public void testGetParameterTypes() {
	String methodSig = "(QString;QObject;I)I";
	String[] types= Signature.getParameterTypes(methodSig);
	assertEquals("Signature#getParameterTypes is not correct1", 3, types.length);
	assertEquals("Signature#getParameterTypes is not correct2", "QObject;", types[1]);
	try {
		Signature.getParameterTypes("");
		assertTrue("Signature#getParameterTypes is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}

	// primitive types
	methodSig = "(BCDFIJSVZ)V";
	assertEquals("Signature#getParameterTypes 4", 9,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 4", "B",
			Signature.getParameterTypes(methodSig)[0]);
	assertEquals("Signature#getParameterTypes 4", "C",
			Signature.getParameterTypes(methodSig)[1]);
	assertEquals("Signature#getParameterTypes 4", "D",
			Signature.getParameterTypes(methodSig)[2]);
	assertEquals("Signature#getParameterTypes 4", "F",
			Signature.getParameterTypes(methodSig)[3]);
	assertEquals("Signature#getParameterTypes 4", "I",
			Signature.getParameterTypes(methodSig)[4]);
	assertEquals("Signature#getParameterTypes 4", "J",
			Signature.getParameterTypes(methodSig)[5]);
	assertEquals("Signature#getParameterTypes 4", "S",
			Signature.getParameterTypes(methodSig)[6]);
	assertEquals("Signature#getParameterTypes 4", "V",
			Signature.getParameterTypes(methodSig)[7]);
	assertEquals("Signature#getParameterTypes 4", "Z",
			Signature.getParameterTypes(methodSig)[8]);

	// array types
	methodSig = "([I[[J[[[B[Qstring;[Tv;[Lstring;)V";
	assertEquals("Signature#getParameterTypes 5", 6,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 5", "[I",
			Signature.getParameterTypes(methodSig)[0]);
	assertEquals("Signature#getParameterTypes 5", "[[J",
			Signature.getParameterTypes(methodSig)[1]);
	assertEquals("Signature#getParameterTypes 5", "[[[B",
			Signature.getParameterTypes(methodSig)[2]);
	assertEquals("Signature#getParameterTypes 5", "[Qstring;",
			Signature.getParameterTypes(methodSig)[3]);
	assertEquals("Signature#getParameterTypes 5", "[Tv;",
			Signature.getParameterTypes(methodSig)[4]);
	assertEquals("Signature#getParameterTypes 5", "[Lstring;",
			Signature.getParameterTypes(methodSig)[5]);
	
	// resolved types
	methodSig = "(La;)V";
	assertEquals("Signature#getParameterTypes 6", 1,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 6", "La;",
			Signature.getParameterTypes(methodSig)[0]);
	methodSig = "(La<TE;>;)V";
	assertEquals("Signature#getParameterTypes 6", 1,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 6", "La<TE;>;",
			Signature.getParameterTypes(methodSig)[0]);
	methodSig = "(La/b/c<TE;>.d<TF;>;)V";
	assertEquals("Signature#getParameterTypes 6", 1,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 6", "La/b/c<TE;>.d<TF;>;",
			Signature.getParameterTypes(methodSig)[0]);

}
/**
 * @see Signature
 */
public void testGetQualifier() {
	assertEquals("Signature#getQualifier is not correct1", "java.lang",
			Signature.getQualifier("java.lang.Object"));
	assertEquals("Signature#getQualifier is not correct2",  "",
			Signature.getQualifier(""));
	
}
/**
 * @see Signature
 */
public void testGetReturnType() {
	String methodSig = "(QString;QObject;I)I";
	assertEquals("Signature#getReturnType is not correct1", "I",
			Signature.getReturnType(methodSig));
	try {
		Signature.getReturnType("");
		assertTrue("Signature#getReturnType is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}
	
	// tests with 1.5-specific elements
	methodSig = "<X:Qlist<Qstring;>;>(Qstring;Qobject;I)I^Qexception;^Qerror;";
	assertEquals("Signature#getReturnType is not correct2", "I",
			Signature.getReturnType(methodSig));
	methodSig = "<X:Qlist<Qstring;>;>(Qlist<Qstring;>;)Qlist<Qxxx;>;^Qexception;^Qerror;";
	assertEquals("Signature#getReturnType is not correct3", "Qlist<Qxxx;>;",
			Signature.getReturnType(methodSig));
}

/**
 * @see Signature
 * @since 3.0
 */
public void testGetTypeVariable() {
	// tests with 1.5-specific elements
	String formalTypeParameterSignature = "Hello:";
	assertEquals("Signature#getTypeVariable is not correct1", "Hello",
			Signature.getTypeVariable(formalTypeParameterSignature));
	formalTypeParameterSignature = "Hello::Qi1;:Qi2;";
	assertEquals("Signature#getTypeVariable is not correct2", "Hello",
			Signature.getTypeVariable(formalTypeParameterSignature));
	formalTypeParameterSignature = "Hello:Qlist<Qstring;>;:Qi1;:Qi2;";
	assertEquals("Signature#getTypeVariable is not correct3", "Hello",
			Signature.getTypeVariable(formalTypeParameterSignature));
	try {
		Signature.getTypeVariable("");
		assertTrue("Signature#getTypeVariable is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}
}

/**
 * @see Signature
 * @since 3.0
 */
public void testGetTypeParameterBounds() {
	// tests with 1.5-specific elements
	String formalTypeParameterSignature = "Hello:";
	assertEquals("Signature#getTypeParameterBounds is not correct1", 0,
			Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
	formalTypeParameterSignature = "Hello::Qi1;:Qi2;";
	assertEquals("Signature#getTypeParameterBounds is not correct2", 2,
			Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
	assertEquals("Signature#getTypeParameterBounds is not correct2a", "Qi1;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[0]);
	assertEquals("Signature#getTypeParameterBounds is not correct2b", "Qi2;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[1]);
	formalTypeParameterSignature = "Hello:Qlist<Qstring;>;:Qi1;:Qi2;";
	assertEquals("Signature#getTypeParameterBounds is not correct3", 3,
			Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
	assertEquals("Signature#getTypeParameterBounds is not correct3a", "Qlist<Qstring;>;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[0]);
	assertEquals("Signature#getTypeParameterBounds is not correct3b", "Qi1;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[1]);
	assertEquals("Signature#getTypeParameterBounds is not correct3c", "Qi2;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[2]);
	formalTypeParameterSignature = "Hello:Qi1;";
	assertEquals("Signature#getTypeParameterBounds is not correct4", 1,
			Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
	assertEquals("Signature#getTypeParameterBounds is not correct4a", "Qi1;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[0]);
	try {
		Signature.getTypeParameterBounds("");
		assertTrue("Signature#getTypeParameterBounds is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}
}

/**
 * @see Signature
 */
public void testGetSimpleName() {
	assertEquals("Signature#getSimpleName is not correct1", "Object",
			Signature.getSimpleName("java.lang.Object"));
	assertEquals("Signature#getSimpleName is not correct2", "",
			Signature.getSimpleName(""));
}
/**
 * @see Signature
 */
public void testGetSimpleNames() {
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
public void testToQualifiedName() {
	assertEquals("Signature#toQualifiedName is not correct1", "java.lang.Object",
			Signature.toQualifiedName(new String[] {"java", "lang", "Object"}));
	assertEquals("Signature#toQualifiedName is not correct2", "Object",
			Signature.toQualifiedName(new String[] {"Object"}));
	assertEquals("Signature#toQualifiedName is not correct3", "",
			Signature.toQualifiedName(new String[0]));
}
/**
 * @see Signature.toString(String)
 */
public void testToString1() {
	assertEquals(
			"Signature#toString is not correct 1", 
			"java/lang/String",
			Signature.toString("Ljava/lang/String;"));
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
	
	// signatures with 1.5 elements
	assertEquals(
		"Signature#toString is not correct 15", 
		"VAR",
		Signature.toString("TVAR;"));
	assertEquals(
		"Signature#toString is not correct 16", 
		"A<B>",
		Signature.toString("QA<QB;>;"));
	assertEquals(
		"Signature#toString is not correct 17", 
		"A<?>",
		Signature.toString("QA<*>;"));
	assertEquals(
		"Signature#toString is not correct 18", 
		"A<? extends B>",
		Signature.toString("QA<+QB;>;"));
	assertEquals(
		"Signature#toString is not correct 19", 
		"A<? super B>",
		Signature.toString("QA<-QB;>;"));
	assertEquals(
		"Signature#toString is not correct 20", 
		"A<?,?,?,?,?>",
		Signature.toString("LA<*****>;"));
	assertEquals(
		"Signature#toString is not correct 21", 
		"a<V>.b<W>.c<X>",
		Signature.toString("La<TV;>.b<QW;>.c<LX;>;"));
	
}
/**
 * @see Signature.toString(String, String, String[], boolean, boolean)
 */
public void testToString2() {
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
public void testToStringInnerType() {
	assertEquals(
		"Signature#toString is not correct", 
		"x.y.A.Inner",
		Signature.toString("Lx.y.A$Inner;"));
}

/**
 * @see Signature.getTypeSignatureKind(String)
 */
public void testGetTypeSignatureKind() {
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 1", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("Ljava.lang.String;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 2", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[Ljava.lang.String;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 3", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QString;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 4", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[[QString;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 5", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("Z"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 6", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("B"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 7", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("C"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 8", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("D"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 9", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("F"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 10", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("I"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 11", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("J"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 12", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("S"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 13", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("V"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 14", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[[[I"));
	
	// signatures with 1.5 elements
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 15", 
		Signature.TYPE_VARIABLE_SIGNATURE,
		Signature.getTypeSignatureKind("TVAR;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 16", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<QB;>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 17", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<*>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 18", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<+QB;>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 19", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<-QB;>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 20", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("LA<*****>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 21", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("La<TV;>.b<QW;>.c<LX;>;"));
}

/**
 * @see Signature.getTypeSignatureKind(char[])
 */
public void testGetTypeSignatureKind2() {
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 1", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("Ljava.lang.String;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 2", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[Ljava.lang.String;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 3", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QString;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 4", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[[QString;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 5", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("Z".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 6", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("B".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 7", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("C".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 8", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("D".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 9", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("F".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 10", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("I".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 11", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("J".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 12", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("S".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 13", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("V".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 14", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[[[I".toCharArray()));
	
	// signatures with 1.5 elements
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 15", 
		Signature.TYPE_VARIABLE_SIGNATURE,
		Signature.getTypeSignatureKind("TVAR;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 16", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<QB;>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 17", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<*>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 18", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<+QB;>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 19", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<-QB;>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 20", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("LA<*****>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 21", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("La<TV;>.b<QW;>.c<LX;>;".toCharArray()));
}

}
