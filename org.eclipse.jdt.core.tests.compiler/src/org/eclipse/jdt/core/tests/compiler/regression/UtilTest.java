/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

//import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.eclipse.jdt.core.compiler.CharOperation;

//import junit.framework.AssertionFailedError;
import junit.framework.Test;

public class UtilTest extends AbstractRegressionTest {
	
public UtilTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
public boolean checkPathMatch(char[] pattern, char[] path, boolean isCaseSensitive) {
	
	CharOperation.replace(pattern, '/', File.separatorChar);
	CharOperation.replace(pattern, '\\', File.separatorChar);
	CharOperation.replace(path, '/', File.separatorChar);
	CharOperation.replace(path, '\\', File.separatorChar);
	
	boolean result = CharOperation.pathMatch(pattern, path, isCaseSensitive, File.separatorChar);

//	boolean antResult = SelectorUtils.matchPath(new String(pattern), new String(path), isCaseSensitive);
//	if (antResult != result) {
//		new AssertionFailedError("WARNING : Ant expectation for patchMatch(\""+new String(pattern)+"\", \""+new String(path)+"\", ...) is: "+antResult).printStackTrace();
//	}

	return result;
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
		checkPathMatch("hello/*/World".toCharArray(), "hello/zzz/World".toCharArray(), true));
}

public void test35() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("hello/**/World".toCharArray(), "hello/x/y/z/World".toCharArray(), true));
}

public void test36() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("hello/**/World/**/*.java".toCharArray(), "hello/x/y/z/World/X.java".toCharArray(), true));
}

public void test37() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("**/World/**/*.java".toCharArray(), "hello/x/y/z/World/X.java".toCharArray(), true));
}

public void test38() {

	assertTrue("Path pattern matching failure",
		!checkPathMatch("/*.java".toCharArray(), "/hello/x/y/z/World/X.java".toCharArray(), true));
}

/*
 * From Ant pattern set examples
 */
public void test39() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("**/CVS/*".toCharArray(), "CVS/Repository".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("**/CVS/*".toCharArray(), "org/apache/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		checkPathMatch("**/CVS/*".toCharArray(), "org/apache/jakarta/tools/ant/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-4",
		!checkPathMatch("**/CVS/*".toCharArray(), "org/apache/CVS/foo/bar/Entries".toCharArray(), true));
}

/*
 * From Ant pattern set examples
 */
public void test40() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("org/apache/jakarta/**".toCharArray(), "org/apache/jakarta/tools/ant/docs/index.html".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("org/apache/jakarta/**".toCharArray(), "org/apache/jakarta/test.xml".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		!checkPathMatch("org/apache/jakarta/**".toCharArray(), "org/apache/xyz.java".toCharArray(), true));
}

/*
 * From Ant pattern set examples
 */
public void test41() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("org/apache/**/CVS/*".toCharArray(), "org/apache/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("org/apache/**/CVS/*".toCharArray(), "org/apache/jakarta/tools/ant/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		!checkPathMatch("org/apache/**/CVS/*".toCharArray(), "org/apache/CVS/foo/bar/Entries".toCharArray(), true));
}

/*
 * From Ant pattern set examples
 */
public void test42() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("**/test/**".toCharArray(), "org/apache/test/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("**/test/**".toCharArray(), "test".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		checkPathMatch("**/test/**".toCharArray(), "a/test".toCharArray(), true));
	assertTrue("Path pattern matching failure-4",
		checkPathMatch("**/test/**".toCharArray(), "test/a.java".toCharArray(), true));
	assertTrue("Path pattern matching failure-5",
		!checkPathMatch("**/test/**".toCharArray(), "org/apache/test.java".toCharArray(), true));
}
/*
 * Corner cases
 */
public void test43() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("/test/".toCharArray(), "/test/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("/test/**".toCharArray(), "/test/CVS/Entries".toCharArray(), true));
}
/*
 * Corner cases
 */
public void test44() {
		
	assertTrue("Path pattern matching failure-1",
		!checkPathMatch("test".toCharArray(), "test/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		!checkPathMatch("**/test".toCharArray(), "test/CVS/Entries".toCharArray(), true));
}
/*
 * Corner cases
 */
public void test45() {
		
	assertTrue("Path pattern matching failure-1",
		checkPathMatch("/test/test1/".toCharArray(), "/test/test1/test/test1".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		!checkPathMatch("/test/test1".toCharArray(), "/test/test1/test/test1".toCharArray(), true));
}
public void test46() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("hello/**/World".toCharArray(), "hello/World".toCharArray(), true));
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
		checkPathMatch("**/hello".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test50() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("**/hello/**".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test51() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("**/hello/".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test52() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("hello/".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test53() {

	assertTrue("Path pattern matching failure",
		!checkPathMatch("/".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test54() {

	assertTrue("Path pattern matching failure-1",
		!checkPathMatch("x/".toCharArray(), "hello/x".toCharArray(), true)); // 29761

	assertTrue("Path pattern matching failure-2",
		checkPathMatch("**/x/".toCharArray(), "hello/x".toCharArray(), true)); 

	assertTrue("Path pattern matching failure-3",
		!checkPathMatch("/x/".toCharArray(), "hello/x".toCharArray(), true));
}
public void test56() {

	assertTrue("Path pattern matching failure",
		!checkPathMatch("/**".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test57() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("/".toCharArray(), "/hello/hello".toCharArray(), true));
}
public void test58() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("/**".toCharArray(), "/hello/hello".toCharArray(), true));
}
public void test59() {

	assertTrue("Path pattern matching failure",
		!checkPathMatch("**".toCharArray(), "/hello/hello".toCharArray(), true));
}
public void test60() {

	assertTrue("Path pattern matching failure-1",
		!checkPathMatch("/P/src".toCharArray(), "/P/src/X".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		!checkPathMatch("/P/**/src".toCharArray(), "/P/src/X".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		checkPathMatch("/P/src".toCharArray(), "/P/src".toCharArray(), true));
	assertTrue("Path pattern matching failure-4",
		!checkPathMatch("A.java".toCharArray(), "/P/src/A.java".toCharArray(), true));		
}
public void test61() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("/P/src/**/CVS".toCharArray(), "/P/src/CVS".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("/P/src/**/CVS/".toCharArray(), "/P/src/CVS".toCharArray(), true));
}
public void test62() {
	assertTrue("Camel pattern matching failure-1",
			CharOperation.camelCaseMatch("NPE".toCharArray(), "NullPointerException".toCharArray()));
	assertTrue("Camel pattern matching failure-2",
			CharOperation.camelCaseMatch("NPExc".toCharArray(), "NullPointerException".toCharArray()));
	assertTrue("Camel pattern matching failure-3",
			!CharOperation.camelCaseMatch("NPoE".toCharArray(), "NullPointerException".toCharArray()));
	assertTrue("Camel pattern matching failure-4",
			!CharOperation.camelCaseMatch("NuPExc".toCharArray(), "NullPointerException".toCharArray()));
}
public void test63() {
	assertTrue("Camel pattern matching failure-1",
			!CharOperation.camelCaseMatch("NPEX".toCharArray(), "NullPointerException".toCharArray()));
	assertTrue("Camel pattern matching failure-2",
			!CharOperation.camelCaseMatch("NPex".toCharArray(), "NullPointerException".toCharArray()));
	assertTrue("Camel pattern matching failure-3",
			!CharOperation.camelCaseMatch("npe".toCharArray(), "NullPointerException".toCharArray()));
	assertTrue("Camel pattern matching failure-4",
			!CharOperation.camelCaseMatch("npe".toCharArray(), "NPException".toCharArray()));
	assertTrue("Camel pattern matching failure-5",
			CharOperation.camelCaseMatch("NPointerE".toCharArray(), "NullPointerException".toCharArray()));
}
public void test64() {
	assertTrue("Camel pattern matching failure-1",
			CharOperation.camelCaseMatch("IAE".toCharArray(), "IgnoreAllErrorHandler".toCharArray()));
	assertTrue("Camel pattern matching failure-2",
			CharOperation.camelCaseMatch("IAE".toCharArray(), "IAnchorElement".toCharArray()));
	assertTrue("Camel pattern matching failure-3",
			CharOperation.camelCaseMatch("IAnchorEleme".toCharArray(), "IAnchorElement".toCharArray()));
	assertTrue("Camel pattern matching failure-4",
			!CharOperation.camelCaseMatch("".toCharArray(), "IAnchorElement".toCharArray()));
	assertTrue("Camel pattern matching failure-5",
			CharOperation.camelCaseMatch(null, "IAnchorElement".toCharArray()));
	assertTrue("Camel pattern matching failure-6",
			CharOperation.camelCaseMatch("".toCharArray(), "".toCharArray()));
	assertTrue("Camel pattern matching failure-7",
			!CharOperation.camelCaseMatch("IAnchor".toCharArray(), null));
}
public void test65() {
	assertTrue("Camel pattern matching failure-1",
			CharOperation.camelCaseMatch("iSCDCo".toCharArray(), "invokeStringConcatenationDefaultConstructor".toCharArray()));
	assertTrue("Camel pattern matching failure-2",
			!CharOperation.camelCaseMatch("inVOke".toCharArray(), "invokeStringConcatenationDefaultConstructor".toCharArray()));
	assertTrue("Camel pattern matching failure-3",
			CharOperation.camelCaseMatch("i".toCharArray(), "invokeStringConcatenationDefaultConstructor".toCharArray()));
	assertTrue("Camel pattern matching failure-4",
			!CharOperation.camelCaseMatch("I".toCharArray(), "invokeStringConcatenationDefaultConstructor".toCharArray()));
	assertTrue("Camel pattern matching failure-5",
			!CharOperation.camelCaseMatch("iStringCD".toCharArray(), "invokeStringConcatenationDefaultConstructor".toCharArray()));
	assertTrue("Camel pattern matching failure-6",
			CharOperation.camelCaseMatch("NPE".toCharArray(), "NullPointerException/java.lang".toCharArray()));
	assertTrue("Camel pattern matching failure-6",
			!CharOperation.camelCaseMatch("NPE".toCharArray(), "NullPointer/lang.Exception".toCharArray()));
	assertTrue("Camel pattern matching failure-7",
			CharOperation.camelCaseMatch("NPE".toCharArray(), "Null_Pointer$Exception".toCharArray()));
}
public static Class testClass() {
	return UtilTest.class;
}
}
