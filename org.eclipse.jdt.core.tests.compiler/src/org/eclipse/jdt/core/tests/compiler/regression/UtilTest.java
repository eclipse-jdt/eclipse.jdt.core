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
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.core.compiler.CharOperation;
import junit.framework.Test;

public class UtilTest extends AbstractRegressionTest {
public UtilTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
public void test01() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("X".toCharArray(), "Xyz".toCharArray(), true));
}
public void test02() {

	assertTrue("Pattern matching failure",
		CharOperation.match("X*".toCharArray(), "Xyz".toCharArray(), true));
}
public void test03() {

	assertTrue("Pattern matching failure",
		CharOperation.match("X".toCharArray(), "X".toCharArray(), true));
}
public void test04() {

	assertTrue("Pattern matching failure",
		CharOperation.match("X*X".toCharArray(), "XYX".toCharArray(), true));
}
public void test05() {

	assertTrue("Pattern matching failure",
		CharOperation.match("XY*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test06() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*XY*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test07() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test08() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("a*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test09() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("abc".toCharArray(), "XYZ".toCharArray(), true));
}
public void test10() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("ab*c".toCharArray(), "abX".toCharArray(), true));
}
public void test11() {

	assertTrue("Pattern matching failure",
		CharOperation.match("a*b*c".toCharArray(), "aXXbYYc".toCharArray(), true));
}
public void test12() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("*a*bc".toCharArray(), "aXXbYYc".toCharArray(), true));
}
public void test13() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("*foo*bar".toCharArray(), "".toCharArray(), true));
}
public void test14() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*foo*bar".toCharArray(), "ffoobabar".toCharArray(), true));
}
public void test15() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("*fol*bar".toCharArray(), "ffoobabar".toCharArray(), true));
}
public void test16() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*X*Y*".toCharArray(), "XY".toCharArray(), true));
}
public void test17() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*X*Y*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test18() {

	assertTrue("Pattern matching failure",
		CharOperation.match("main(*)".toCharArray(), "main(java.lang.String[] argv)".toCharArray(), true));
}
public void test19() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*rr*".toCharArray(), "ARRAY".toCharArray(), false));
}

public void test20() {

	assertTrue("Pattern matching failure",
		CharOperation.match("hello*World".toCharArray(), "helloWorld".toCharArray(), true));
}

public void test21() {
	assertEquals("Trim failure", "hello", new String(CharOperation.trim("hello".toCharArray())));
}
public void test22() {
	assertEquals("Trim failure", "hello", new String(CharOperation.trim("   hello".toCharArray())));
}
public void test23() {
	assertEquals("Trim failure", "hello", new String(CharOperation.trim("   hello   ".toCharArray())));
}
public void test24() {
	assertEquals("Trim failure", "hello", new String(CharOperation.trim("hello   ".toCharArray())));
}
public void test25() {
	assertEquals("Trim failure", "", new String(CharOperation.trim("   ".toCharArray())));
}
public void test26() {
	assertEquals("Trim failure", "hello world", new String(CharOperation.trim(" hello world  ".toCharArray())));
}
public void test27() {
	char [][] tokens = CharOperation.splitAndTrimOn(','," hello,world".toCharArray());
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[hello][world]", buffer.toString());
}
public void test28() {
	char [][] tokens = CharOperation.splitAndTrimOn(','," hello , world".toCharArray());
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[hello][world]", buffer.toString());
}
public void test29() {
	char [][] tokens = CharOperation.splitAndTrimOn(','," hello, world   ".toCharArray());
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[hello][world]", buffer.toString());
}
public void test30() {
	char [][] tokens = CharOperation.splitAndTrimOn(','," hello, world   ,zork/, aaa bbb".toCharArray());
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[hello][world][zork/][aaa bbb]", buffer.toString());
}
public void test31() {
	char [][] tokens = CharOperation.splitAndTrimOn(',',"  ,  ".toCharArray());
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[][]", buffer.toString());
}
public void test32() {
	char [][] tokens = CharOperation.splitAndTrimOn(',',"   ".toCharArray());
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[]", buffer.toString());
}
public void test33() {
	char [][] tokens = CharOperation.splitAndTrimOn(',',"  , hello  ".toCharArray());
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[][hello]", buffer.toString());
}

public void test34() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("hello/*/World".toCharArray(), "hello/zzz/World".toCharArray(), true, '/'));
}

public void test35() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("hello/**/World".toCharArray(), "hello/x/y/z/World".toCharArray(), true, '/'));
}

public void test36() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("hello/**/World/**/*.java".toCharArray(), "hello/x/y/z/World/X.java".toCharArray(), true, '/'));
}

public void test37() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("**/World/**/*.java".toCharArray(), "hello/x/y/z/World/X.java".toCharArray(), true, '/'));
}

public void test38() {

	assertTrue("Path pattern matching failure",
		!CharOperation.pathMatch("/*.java".toCharArray(), "/hello/x/y/z/World/X.java".toCharArray(), true, '/'));
}

/*
 * From Ant pattern set examples
 */
public void test39() {

	assertTrue("Path pattern matching failure-1",
		CharOperation.pathMatch("**/CVS/*".toCharArray(), "CVS/Repository".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-2",
		CharOperation.pathMatch("**/CVS/*".toCharArray(), "org/apache/CVS/Entries".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-3",
		CharOperation.pathMatch("**/CVS/*".toCharArray(), "org/apache/jakarta/tools/ant/CVS/Entries".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-4",
		!CharOperation.pathMatch("**/CVS/*".toCharArray(), "org/apache/CVS/foo/bar/Entries".toCharArray(), true, '/'));
}

/*
 * From Ant pattern set examples
 */
public void test40() {

	assertTrue("Path pattern matching failure-1",
		CharOperation.pathMatch("org/apache/jakarta/**".toCharArray(), "org/apache/jakarta/tools/ant/docs/index.html".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-2",
		CharOperation.pathMatch("org/apache/jakarta/**".toCharArray(), "org/apache/jakarta/test.xml".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-3",
		!CharOperation.pathMatch("org/apache/jakarta/**".toCharArray(), "org/apache/xyz.java".toCharArray(), true, '/'));
}

/*
 * From Ant pattern set examples
 */
public void test41() {

	assertTrue("Path pattern matching failure-1",
		CharOperation.pathMatch("org/apache/**/CVS/*".toCharArray(), "org/apache/CVS/Entries".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-2",
		CharOperation.pathMatch("org/apache/**/CVS/*".toCharArray(), "org/apache/jakarta/tools/ant/CVS/Entries".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-3",
		!CharOperation.pathMatch("org/apache/**/CVS/*".toCharArray(), "org/apache/CVS/foo/bar/Entries".toCharArray(), true, '/'));
}

/*
 * From Ant pattern set examples
 */
public void test42() {

	assertTrue("Path pattern matching failure-1",
		CharOperation.pathMatch("**/test/**".toCharArray(), "org/apache/test/CVS/Entries".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-2",
		CharOperation.pathMatch("**/test/**".toCharArray(), "test".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-3",
		CharOperation.pathMatch("**/test/**".toCharArray(), "a/test".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-4",
		CharOperation.pathMatch("**/test/**".toCharArray(), "test/a.java".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-5",
		!CharOperation.pathMatch("**/test/**".toCharArray(), "org/apache/test.java".toCharArray(), true, '/'));
}
/*
 * Corner cases
 */
public void test43() {

	assertTrue("Path pattern matching failure-1",
		CharOperation.pathMatch("/test/".toCharArray(), "/test/CVS/Entries".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-2",
		CharOperation.pathMatch("/test/**".toCharArray(), "/test/CVS/Entries".toCharArray(), true, '/'));
}
/*
 * Corner cases
 */
public void test44() {
		
	assertTrue("Path pattern matching failure-1",
		!CharOperation.pathMatch("test".toCharArray(), "test/CVS/Entries".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-2",
		!CharOperation.pathMatch("**/test".toCharArray(), "test/CVS/Entries".toCharArray(), true, '/'));
}
/*
 * Corner cases
 */
public void test45() {
		
	assertTrue("Path pattern matching failure-1",
		CharOperation.pathMatch("/test/test1/".toCharArray(), "/test/test1/test/test1".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-2",
		!CharOperation.pathMatch("/test/test1".toCharArray(), "/test/test1/test/test1".toCharArray(), true, '/'));
}
public void test46() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("hello/**/World".toCharArray(), "hello/World".toCharArray(), true, '/'));
}
/*
 * Regression test for 28316 Missing references to constructor 
 */
public void test47() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*x".toCharArray(), "x.X".toCharArray(), false));
}
public void test48() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*a*".toCharArray(), "abcd".toCharArray(), false));
}
public void test49() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("**/hello".toCharArray(), "hello/hello".toCharArray(), true, '/'));
}
public void test50() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("**/hello/**".toCharArray(), "hello/hello".toCharArray(), true, '/'));
}
public void test51() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("**/hello/".toCharArray(), "hello/hello".toCharArray(), true, '/'));
}
public void test52() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("hello/".toCharArray(), "hello/hello".toCharArray(), true, '/'));
}
public void test53() {

	assertTrue("Path pattern matching failure",
		!CharOperation.pathMatch("/".toCharArray(), "hello/hello".toCharArray(), true, '/'));
}
public void test54() {

	assertTrue("Path pattern matching failure-1",
		CharOperation.pathMatch("x/".toCharArray(), "hello/x".toCharArray(), true, '/'));

	assertTrue("Path pattern matching failure-2",
		!CharOperation.pathMatch("/x/".toCharArray(), "hello/x".toCharArray(), true, '/'));
}
public void test56() {

	assertTrue("Path pattern matching failure",
		!CharOperation.pathMatch("/**".toCharArray(), "hello/hello".toCharArray(), true, '/'));
}
public void test57() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("/".toCharArray(), "/hello/hello".toCharArray(), true, '/'));
}
public void test58() {

	assertTrue("Path pattern matching failure",
		CharOperation.pathMatch("/**".toCharArray(), "/hello/hello".toCharArray(), true, '/'));
}
public void test59() {

	assertTrue("Path pattern matching failure",
		!CharOperation.pathMatch("**".toCharArray(), "/hello/hello".toCharArray(), true, '/'));
}
public void test60() {

	assertTrue("Path pattern matching failure-1",
		!CharOperation.pathMatch("/P/src".toCharArray(), "/P/src/X".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-2",
		!CharOperation.pathMatch("/P/**/src".toCharArray(), "/P/src/X".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-3",
		CharOperation.pathMatch("/P/src".toCharArray(), "/P/src".toCharArray(), true, '/'));
	assertTrue("Path pattern matching failure-4",
		!CharOperation.pathMatch("A.java".toCharArray(), "/P/src/A.java".toCharArray(), true, '/'));		
}

public static Class testClass() {
	return UtilTest.class;
}
}
