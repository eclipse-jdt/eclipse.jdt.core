package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.core.tests.compiler.regression.*;
import org.eclipse.jdt.internal.compiler.util.*;
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

public static Class testClass() {
	return UtilTest.class;
}
}
