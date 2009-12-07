/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.IOException;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;

public class TypeAnnotationSyntaxTest extends AbstractTypeAnnotationSyntaxTest {

	public static Class testClass() {
		return TypeAnnotationSyntaxTest.class;
	}
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}
public TypeAnnotationSyntaxTest(String testName){
	super(testName);
}

static {
//	TESTS_NAMES = new String[] { "test0038", "test0039", "test0040a" };
//	TESTS_NUMBERS = new int[] { 561 };
}

public void test0001() throws IOException {
	String source = "@Marker class A extends String {}\n;" +
					"@Marker class B extends @Marker String {}\n" +
					"@Marker class C extends @Marker @SingleMember(0) String {}\n" +
					"@Marker class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {}\n" +
					"@Marker class E extends String {}\n;";

	String expectedUnitToString = 
		"@Marker class A extends String {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"@Marker class B extends @Marker String {\n" + 
		"  B() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"@Marker class C extends @Marker @SingleMember(0) String {\n" + 
		"  C() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"@Marker class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {\n" + 
		"  D() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"@Marker class E extends String {\n" + 
		"  E() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0001", expectedUnitToString);
}
public void test0001a() throws IOException {
	String source = "class A extends String {}\n;" +
					"class B extends @Marker String {}\n" +
					"class C extends @Marker @SingleMember(0) String {}\n" +
					"class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {}\n" +
					"class E extends String {}\n;";
    
	String expectedUnitToString = 
		"class A extends String {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"class B extends @Marker String {\n" + 
		"  B() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"class C extends @Marker @SingleMember(0) String {\n" + 
		"  C() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {\n" + 
		"  D() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"class E extends String {\n" + 
		"  E() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0001a", expectedUnitToString);
}
public void test0002() throws IOException {
	String source = "@Marker class A implements Comparable, " +
					"                   @Marker Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A implements Comparable, @Marker Serializable, Cloneable {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0002", expectedUnitToString);
}
public void test0002a() throws IOException {
	String source = "@Marker class A implements Comparable, " +
					"                   @Marker @SingleMember(0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A implements Comparable, @Marker @SingleMember(0) Serializable, Cloneable {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0002a", expectedUnitToString);
}
public void test0002b() throws IOException {
	String source = "@Marker class A implements Comparable, " +
					"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0002b", expectedUnitToString);
}
public void test0002c() throws IOException {
	String source = "@Marker class A implements @Marker Comparable, " +
					"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
					"                   @Marker Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A implements @Marker Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, @Marker Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0002c", expectedUnitToString);
}
public void test0003() throws IOException {
	String source = "@Marker class A extends Object implements Comparable, " +
					"                   @Marker @SingleMember(10) @Normal(Value=0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A extends Object implements Comparable, @Marker @SingleMember(10) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0003", expectedUnitToString);
}
public void test0003a() throws IOException {
	String source = "@Marker class A extends @Marker Object implements Comparable, " +
					"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A extends @Marker Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0003a", expectedUnitToString);
}
public void test0003b() throws IOException {
	String source = "@Marker class A extends @Marker @SingleMember(0) Object implements Comparable, " +
	"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
	"                   Cloneable {\n" +
	"}\n";
	String expectedUnitToString = 
		"@Marker class A extends @Marker @SingleMember(0) Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0003b", expectedUnitToString);
}
public void test0003c() throws IOException {
	String source = "@Marker class A extends @Marker @SingleMember(0) @Normal(Value=0) Object implements Comparable, " +
	"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
	"                   Cloneable {\n" +
	"}\n";
	String expectedUnitToString = 
		"@Marker class A extends @Marker @SingleMember(0) @Normal(Value = 0) Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0003c", expectedUnitToString);
}
public void test0004() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker String[] @Marker[][] s[] @SingleMember(0)[][] @Normal(Value = 0)[][];\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker String[] @Marker [][][] @SingleMember(0) [][] @Normal(Value = 0) [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0004", expectedUnitToString);
}
public void test0005() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static void main(String args[]) {\n" +
					"    @Readonly String @Nullable[] @NonNull[] s;\n" +
					"    s = new @Readonly String @NonNull[5] @Nullable[];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  int[][] f;\n" + 
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    @Readonly String @Nullable [] @NonNull [] s;\n" + 
		"    s = new @Readonly String @NonNull [5] @Nullable [];\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0005", expectedUnitToString);
}
public void test0005a() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static void main(String args[]) {\n" +
					"    @Readonly String s;\n" +
					"	 s = new @Readonly String @NonNull[] @Nullable[] { {\"Hello\"}, {\"World\"}} [0][0];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  int[][] f;\n" + 
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    @Readonly String s;\n" + 
		"    s = new @Readonly String @NonNull [] @Nullable []{{\"Hello\"}, {\"World\"}}[0][0];\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0005a", expectedUnitToString);
}
public void test0006() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static int main(String args[])[] @Marker[][] @Marker @SingleMember(0) @Normal(Value=0)[][] @Marker {\n" +
					"    @Readonly String @Nullable[] @NonNull[] s;\n" +
					"    s = new @Readonly String @NonNull[5] @Nullable[];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  int[][] f;\n" + 
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static int[] @Marker [][] @Marker @SingleMember(0) @Normal(Value = 0) [][] main(String[] args) @Marker {\n" +
		"    @Readonly String @Nullable [] @NonNull [] s;\n" + 
		"    s = new @Readonly String @NonNull [5] @Nullable [];\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0006", expectedUnitToString);

}
public void test0007() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static int main(String args[])[] @Marker[][] @Marker @SingleMember(0) @Normal(Value=0)[][] @Marker {\n" +
					"    @Readonly String @Nullable[] @NonNull[] s;\n" +
					"    s = new @Readonly String @NonNull[5] @Nullable[];\n" +
					"}\n" +
					"@Marker public A () @Marker @SingleMember(0) @Normal(Value=10) {}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  int[][] f;\n" + 
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public static int[] @Marker [][] @Marker @SingleMember(0) @Normal(Value = 0) [][] main(String[] args) @Marker {\n" + 
		"    @Readonly String @Nullable [] @NonNull [] s;\n" + 
		"    s = new @Readonly String @NonNull [5] @Nullable [];\n" + 
		"  }\n" + 
		"  public @Marker A() @Marker @SingleMember(0) @Normal(Value = 10) {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0007", expectedUnitToString);
}
// parameters
public void test0008() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(int[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(int[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008", expectedUnitToString);
}
public void test0008a() throws IOException  {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(String[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(String[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008a", expectedUnitToString);
}
public void test0008b() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(HashMap<String, Object>[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<String, Object>[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008b", expectedUnitToString);
}
public void test0008c() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker [][] main(HashMap<String, Object>.Iterator[] @SingleMember(10) [][] args[] @Normal(Value = 10) [][])[] @Marker [][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<String, Object>.Iterator[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008c", expectedUnitToString);
}
// varargs annotation
public void test0008d() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(int[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(int[] @SingleMember(10) [][] @Marker ... args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008d", expectedUnitToString);
}
public void test0008e() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(String[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(String[] @SingleMember(10) [][] @Marker ... args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008e", expectedUnitToString);
}
public void test0008f() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(HashMap<Integer,String>[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<Integer, String>[] @SingleMember(10) [][] @Marker ... args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008f", expectedUnitToString);
}
public void test0008g() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(HashMap<Integer,String>.Iterator[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<Integer, String>.Iterator[] @SingleMember(10) [][] @Marker ... args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008g", expectedUnitToString);
}
// local variables
public void test0009() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"public static void main(String args[]) {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int[][] f;\n" + 
		"    @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"    float[][] p;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0009", expectedUnitToString);
}
// type parameter
public void test0010() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> void foo() {\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" +
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>void foo() {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0010", expectedUnitToString);
}
// Type
public void test0011() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker int foo() @Marker {\n" +
					"    return 0;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> int bar() @Marker{\n" +
					"    return 0;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker int foo() @Marker {\n" + 
		"    return 0;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>int bar() @Marker {\n" + 
		"    return 0;\n" +
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011", expectedUnitToString);
}
// Type
public void test0011a() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker String foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> String bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker String foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>String bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011a", expectedUnitToString);
}
//Type
public void test0011b() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object> foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object> bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker HashMap<@Readonly String, Object> foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>HashMap<String, @NonNull Object> bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011b", expectedUnitToString);
}
// Type
public void test0011c() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>.Iterator bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>HashMap<String, @NonNull Object>.Iterator bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011c", expectedUnitToString);
}
//Type
public void test0011d() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator[] @NonEmpty[][] foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>.Iterator[] @NonEmpty[][] bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator[] @NonEmpty [][] foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>HashMap<String, @NonNull Object>.Iterator[] @NonEmpty [][] bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011d", expectedUnitToString);
}
//Type
public void test0011e() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker int[] @NonEmpty[][] foo() @Marker {\n" +
					"    return 0;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> int[] @NonEmpty[][] bar() @Marker{\n" +
					"    return 0;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker int[] @NonEmpty [][] foo() @Marker {\n" + 
		"    return 0;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>int[] @NonEmpty [][] bar() @Marker {\n" + 
		"    return 0;\n" +
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011e", expectedUnitToString);
}
// Type
public void test0011f() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker String[]@NonEmpty[][] foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> String[]@NonEmpty[][] bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker String[] @NonEmpty [][] foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>String[] @NonEmpty [][] bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011f", expectedUnitToString);
}
//Type
public void test0011g() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>[] @NonEmpty[][] foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>[]@NonEmpty[][] bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker HashMap<@Readonly String, Object>[] @NonEmpty [][] foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>HashMap<String, @NonNull Object>[] @NonEmpty [][] bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011g", expectedUnitToString);
}
// Type0 field declaration.
public void test0012() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker int k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker int k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012", expectedUnitToString);
}
//Type0 field declaration.
public void test0012a() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker String k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker String k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012a", expectedUnitToString);
}
//Type0 field declaration.
public void test0012b() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer> k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker HashMap<@Positive Integer, @Negative Integer> k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012b", expectedUnitToString);
}
//Type0 field declaration.
public void test0012c() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012c", expectedUnitToString);
}
//Type0 field declaration.
public void test0012d() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker int[] @NonEmpty[][] k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker int[] @NonEmpty [][] k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012d", expectedUnitToString);
}
//Type0 field declaration.
public void test0012e() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker String[] @NonEmpty[][]k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker String[] @NonEmpty [][] k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012e", expectedUnitToString);
}
//Type0 field declaration.
public void test0012f() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty[][] k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012f", expectedUnitToString);
}
//Type0 field declaration.
public void test0012g() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012g", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker int foo() { return 0; }\n" +
					"    public int bar() { return 0; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int foo() {\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"  public int bar() {\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013a() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker String foo() { return null; }\n" +
					"    public String bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker String foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public String bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013a", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013b() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer> foo() { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer>  bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker HashMap<@Positive Integer, @Negative Integer> foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public HashMap<@Positive Integer, @Negative Integer> bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013b", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013c() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator foo() { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer>.Iterator  bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public HashMap<@Positive Integer, @Negative Integer>.Iterator bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013c", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013d() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker int[] foo() @NonEmpty[][] { return 0; }\n" +
					"    public int[] @NonEmpty[][] bar() { return 0; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @NonEmpty [][] foo() {\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"  public int[] @NonEmpty [][] bar() {\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013d", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013e() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker String[]  foo() @NonEmpty[][] { return null; }\n" +
					"    public String[] @NonEmpty[][] bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker String[] @NonEmpty [][] foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public String[] @NonEmpty [][] bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013e", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013f() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer>[] foo() @NonEmpty[][] { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer> [] @NonEmpty[][] bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013f", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013g() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[]  foo() @NonEmpty[][] { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013g", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker int p;\n" +
					"        int q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker int p;\n" + 
		"    int q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014a() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker String p;\n" +
					"        String q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker String p;\n" + 
		"    String q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014a", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014b() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer> p;\n" +
					"        HashMap<@Positive Integer, @Negative Integer> q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker HashMap<@Positive Integer, @Negative Integer> p;\n" + 
		"    HashMap<@Positive Integer, @Negative Integer> q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014b", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014c() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator p;\n" +
					"        HashMap<@Positive Integer, @Negative Integer>.Iterator q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator p;\n" + 
		"    HashMap<@Positive Integer, @Negative Integer>.Iterator q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014c", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014d() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker int[] @NonNull[] p @NonEmpty[][];\n" +
					"        int[] @NonNull[] q @NonEmpty[][];\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker int[] @NonNull [] @NonEmpty [][] p;\n" + 
		"    int[] @NonNull [] @NonEmpty [][] q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014d", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014e() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker String[] @NonNull[] p @NonEmpty[][];\n" +
					"        String[] @NonNull[] q @NonEmpty[][];\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker String[] @NonNull [] @NonEmpty [][] p;\n" + 
		"    String[] @NonNull [] @NonEmpty [][] q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014e", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014f() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonNull[] p @NonEmpty[][];\n" +
					"        HashMap<@Positive Integer, @Negative Integer>[] @NonNull[] q @NonEmpty[][];\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonNull [] @NonEmpty [][] p;\n" + 
		"    HashMap<@Positive Integer, @Negative Integer>[] @NonNull [] @NonEmpty [][] q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014f", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014g() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull[] p @NonEmpty[][];\n" +
					"        HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull[] @NonEmpty[][] q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull [] @NonEmpty [][] p;\n" + 
		"    HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull [] @NonEmpty [][] q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014g", expectedUnitToString);
}
//Type0 foreach
public void test0015() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        String @NonNull[] @Marker[] s @Readonly[];\n" +
					"    	 for (@Readonly String @NonNull[] si @Marker[] : s) {}\n" +
					"    	 for (String @NonNull[] sii @Marker[] : s) {}\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    String @NonNull [] @Marker [] @Readonly [] s;\n" + 
		"    for (@Readonly String @NonNull [] @Marker [] si : s) \n" + 
		"      {\n" + 
		"      }\n" + 
		"    for (String @NonNull [] @Marker [] sii : s) \n" + 
		"      {\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0015", expectedUnitToString);
}
//Type0 foreach
public void test0015a() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        int @NonNull[] @Marker[] s @Readonly[];\n" +
					"    	 for (@Readonly int @NonNull[] si @Marker[] : s) {}\n" +
					"    	 for (int @NonNull[] sii @Marker[] : s) {}\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    int @NonNull [] @Marker [] @Readonly [] s;\n" + 
		"    for (@Readonly int @NonNull [] @Marker [] si : s) \n" + 
		"      {\n" + 
		"      }\n" + 
		"    for (int @NonNull [] @Marker [] sii : s) \n" + 
		"      {\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0015a", expectedUnitToString);
}
// cast expression
public void test0016() throws IOException {
	String source = "public class Clazz {\n" +
					"public static void main(String[] args) {\n" +
					"int x;\n" +
					"x = (Integer)\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Normal(Value=0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Normal(Value=0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly String[] @Normal(Value=0)[][] )\n" +
					"(@Readonly String[] @SingleMember(0)[][] )\n" +
					"(@Readonly String[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly int[] @Normal(Value=0)[][] )\n" +
					"(@Readonly int[] @SingleMember(0)[][] )\n" +
					"(@Readonly int[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator)\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>)\n" +
					"(@Readonly Object)\n" +
					"(@ReadOnly String)\n" +
					"(@Readonly Object)\n" +
					"(@Readonly int) 10;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class Clazz {\n" + 
		"  public Clazz() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int x;\n" + 
		"    x = (Integer) (@Readonly Object) ( @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Normal(Value = 0) [][]) ( @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @SingleMember(0) [][]) ( @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Marker [][]) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Normal(Value = 0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Marker [][]) (@Readonly Object) (@Readonly String[] @Normal(Value = 0) [][]) (@Readonly String[] @SingleMember(0) [][]) (@Readonly String[] @Marker [][]) (@Readonly Object) (@Readonly int[] @Normal(Value = 0) [][]) (@Readonly int[] @SingleMember(0) [][]) (@Readonly int[] @Marker [][]) (@Readonly Object) ( @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>) (@Readonly Object) (@ReadOnly String) (@Readonly Object) (@Readonly int) 10;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0016", expectedUnitToString);
}
//cast expression
public void test0016a() throws IOException {
	String source = "public class Clazz {\n" +
					"public static void main(String[] args) {\n" +
					"int x;\n" +
					"x = (Integer)\n" +
					"(Object)\n" +
					"(@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Normal(Value=0)[][] )\n" +
					"(HashMap<@Positive Integer, Integer>.Iterator[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly HashMap<@Positive Integer, Integer>[] @Normal(Value=0)[][] )\n" +
					"(HashMap<Integer, @Negative Integer>[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, Integer>[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly String[] @Normal(Value=0)[][] )\n" +
					"(String[] @SingleMember(0)[][] )\n" +
					"(@Readonly String[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly int[] @Normal(Value=0)[][] )\n" +
					"(int[] @SingleMember(0)[][] )\n" +
					"(@Readonly int[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly HashMap<Integer, @Negative Integer>.Iterator)\n" +
					"(Object)\n" +
					"(@Readonly HashMap<@Positive Integer, Integer>)\n" +
					"(Object)\n" +
					"(@ReadOnly String)\n" +
					"(Object)\n" +
					"(@Readonly int) 10;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class Clazz {\n" + 
		"  public Clazz() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int x;\n" + 
		"    x = (Integer) (Object) ( @Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Normal(Value = 0) [][]) (HashMap<@Positive Integer, Integer>.Iterator[] @SingleMember(0) [][]) ( @Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Marker [][]) (Object) (@Readonly HashMap<@Positive Integer, Integer>[] @Normal(Value = 0) [][]) (HashMap<Integer, @Negative Integer>[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, Integer>[] @Marker [][]) (Object) (@Readonly String[] @Normal(Value = 0) [][]) (String[] @SingleMember(0) [][]) (@Readonly String[] @Marker [][]) (Object) (@Readonly int[] @Normal(Value = 0) [][]) (int[] @SingleMember(0) [][]) (@Readonly int[] @Marker [][]) (Object) ( @Readonly HashMap<Integer, @Negative Integer>.Iterator) (Object) (@Readonly HashMap<@Positive Integer, Integer>) (Object) (@ReadOnly String) (Object) (@Readonly int) 10;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0016a", expectedUnitToString);
}
// instanceof checks 
public void test0017() throws IOException {
	String source = "public class Clazz {\n" +
					"public static void main(Object o) {\n" +
					"if (o instanceof @Readonly String) {\n" +
					"} else if (o instanceof @Readonly int[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly String[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly HashMap<?,?>[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] ) {\n" +	
					"} else if (o instanceof @Readonly HashMap<?,?>) {\n" +
					"} else if (o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator) {\n" +
					"}\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class Clazz {\n" + 
		"  public Clazz() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(Object o) {\n" + 
		"    if ((o instanceof @Readonly String))\n" + 
		"        {\n" + 
		"        }\n" + 
		"    else\n" + 
		"        if ((o instanceof @Readonly int[] @NonEmpty [][]))\n" + 
		"            {\n" + 
		"            }\n" + 
		"        else\n" + 
		"            if ((o instanceof @Readonly String[] @NonEmpty [][]))\n" + 
		"                {\n" + 
		"                }\n" + 
		"            else\n" + 
		"                if ((o instanceof @Readonly HashMap<?, ?>[] @NonEmpty [][]))\n" + 
		"                    {\n" + 
		"                    }\n" + 
		"                else\n" + 
		"                    if ((o instanceof  @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][]))\n" + 
		"                        {\n" + 
		"                        }\n" + 
		"                    else\n" + 
		"                        if ((o instanceof @Readonly HashMap<?, ?>))\n" + 
		"                            {\n" + 
		"                            }\n" + 
		"                        else\n" + 
		"                            if ((o instanceof  @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator))\n" + 
		"                                {\n" + 
		"                                }\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0017", expectedUnitToString);
}
// assorted unclassified 
public void test0018() throws IOException {
	String source = "import java.util.HashMap;\n" +
					"import java.util.Map; \n" +  
					"\n" +
					"public class Clazz <@A M extends @B String, @C N extends @D Comparable> extends\n" +
					"								@E Object implements @F Comparable <@G Object> {\n" +
					"	\n" +
					"  Clazz(char[] ...args) @H { \n" +   
					"   }\n" +
					"   \n" +
					"  int @I[] f @J[], g, h[], i@K[];\n" +
					"  int @L[][]@M[] f2; \n" +
					"   \n" +
					"  Clazz (int @N[] @O... a) @Q {}\n" +
					" int @R[]@S[] aa() {}\n" +
					" \n" +
					" int @T[]@U[]@V[] a () @W[]@X[]@Y[] @Z { return null; }\n" +
					"   \n" +
					"  public void main(String @A[] @B ... args) @C throws @D Exception {\n" +
					"  	\n" +
					"       HashMap<@E String, @F String> b1;\n" +
					"      \n" +
					"     int b; b = (@G int) 10;\n" +
					"      \n" +
					"     char @H[]@I[] ch; ch = (@K char @L[]@M[])(@N char @O[]@P[]) null;\n" +
					"      \n" +
					"      int[] i; i = new @Q int @R[10];\n" +
					"       \n" +
					"      \n" +
					"   Integer w; w = new X<@S String, @T Integer>().get(new @U Integer(12));\n" +
					"    throw new @V Exception(\"test\");\n" +
					"    boolean c; c  = null instanceof @W String;\n" +
					"	} \n" +
					" public <@X X, @Y Y> void foo(X x, Y @Z... y) {  \n" +
					"	\n" +
					"}\n" +
					" \n" +
					" void foo(Map<? super @A Object, ? extends @B String> m){}\n" +
					" public int compareTo(Object arg0) {\n" +
					"     return 0;\n" +
					" }\n" +
					"\n" +
					"}\n" +
					"class X<@C K, @D T extends @E Object & @F Comparable<? super @G T>> {\n" +
					"	\n" +
					"  public Integer get(Integer integer) {\n" +
					"       return null;\n" +
					"   }\n" +
					"}\n";
					
					
	String expectedUnitToString = "import java.util.HashMap;\n" + 
								  "import java.util.Map;\n" + 
								  "public class Clazz<@A M extends @B String, @C N extends @D Comparable> extends @E Object implements @F Comparable<@G Object> {\n" + 
								  "  int @I [] @J [] f;\n" + 
								  "  int @I [] g;\n" + 
								  "  int @I [][] h;\n" + 
								  "  int @I [] @K [] i;\n" + 
								  "  int @L [][] @M [] f2;\n" + 
								  "  Clazz(char[]... args) @H {\n" + 
								  "    super();\n" + 
								  "  }\n" + 
								  "  Clazz(int @N [] @O ... a) @Q {\n" + 
								  "    super();\n" + 
								  "  }\n" + 
								  "  int @R [] @S [] aa() {\n" + 
								  "  }\n" + 
								  "  int @T [] @U [] @V [] @W [] @X [] @Y [] a() @Z {\n" + 
								  "    return null;\n" + 
								  "  }\n" + 
								  "  public void main(String @A [] @B ... args) @C throws @D Exception {\n" + 
								  "    HashMap<@E String, @F String> b1;\n" + 
								  "    int b;\n" +
								  "    b = (@G int) 10;\n" + 
								  "    char @H [] @I [] ch;\n" +
								  "    ch = (@K char @L [] @M []) (@N char @O [] @P []) null;\n" + 
								  "    int[] i;\n" +
								  "    i = new @Q int @R [10];\n" + 
								  "    Integer w;\n" +
								  "    w = new X<@S String, @T Integer>().get(new @U Integer(12));\n" + 
								  "    throw new @V Exception(\"test\");\n" + 
								  "    boolean c;\n" +
								  "    c = (null instanceof @W String);\n" + 
								  "  }\n" + 
								  "  public <@X X, @Y Y>void foo(X x, Y @Z ... y) {\n" + 
								  "  }\n" + 
								  "  void foo(Map<? super @A Object, ? extends @B String> m) {\n" + 
								  "  }\n" + 
								  "  public int compareTo(Object arg0) {\n" + 
								  "    return 0;\n" + 
								  "  }\n" + 
								  "}\n" + 
								  "class X<@C K, @D T extends @E Object & @F Comparable<? super @G T>> {\n" + 
								  "  X() {\n" + 
								  "    super();\n" + 
								  "  }\n" + 
								  "  public Integer get(Integer integer) {\n" + 
								  "    return null;\n" + 
								  "  }\n" + 
								  "}\n";
	// indexing parser avoids creating lots of nodes, so parse tree comes out incorrectly.
	// this is not bug, but intended behavior - see IndexingParser.newSingleNameReference(char[], long)
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0018", expectedUnitToString);
}
//assorted unclassified 
public void test0019() throws IOException {
	String source = "class X<T extends @E Object & @F Comparable<? super T>> {}\n";
	String expectedUnitToString = "class X<T extends @E Object & @F Comparable<? super T>> {\n" + 
								  "  X() {\n" + 
								  "    super();\n" + 
								  "  }\n" + 
								  "}\n";
	// indexing parser avoids creating lots of nodes, so parse tree comes out incorrectly.
	// this is not bug, but intended behavior - see IndexingParser.newSingleNameReference(char[], long)
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test019", expectedUnitToString);
}
//type class literal expression
public void test0020() throws IOException {
	String source = "public class Clazz {\n" +
					"public static void main(String[] args) {\n" +
					"Class x;\n" +
					"x = Integer.class;\n" +
					"x = @Readonly Object.class;\n" +
					"x = HashMap.Iterator[] @Normal(Value=0)[][].class;\n" +
					"x = @Readonly HashMap.Iterator[] @SingleMember(0)[][].class;\n" +
					"x = @Readonly HashMap.Iterator @Normal(Value=1)[] @Marker[] @Normal(Value=2)[].class;\n" +
					"x = @Readonly Object.class;\n" +
					"x = @Readonly String[] @Normal(Value=0)[][].class;\n" +
					"x = @Readonly String[] @SingleMember(0)[][].class;\n" +
					"x = @Readonly String[] @Marker[][].class;\n" +
					"x = @Readonly Object.class;\n" +
					"x = @Readonly int[][] @Normal(Value=0)[].class;\n" +
					"x = @Readonly int @SingleMember(0)[][][].class;\n" +
					"x = @Readonly int[] @Marker[][].class;\n" +
					"x = @Readonly int.class;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class Clazz {\n" + 
		"  public Clazz() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    Class x;\n" + 
		"    x = Integer.class;\n" + 
		"    x = @Readonly Object.class;\n" + 
		"    x = HashMap.Iterator[] @Normal(Value = 0) [][].class;\n" + 
		"    x = @Readonly HashMap.Iterator[] @SingleMember(0) [][].class;\n" + 
		"    x = @Readonly HashMap.Iterator @Normal(Value = 1) [] @Marker [] @Normal(Value = 2) [].class;\n" + 
		"    x = @Readonly Object.class;\n" + 
		"    x = @Readonly String[] @Normal(Value = 0) [][].class;\n" + 
		"    x = @Readonly String[] @SingleMember(0) [][].class;\n" + 
		"    x = @Readonly String[] @Marker [][].class;\n" + 
		"    x = @Readonly Object.class;\n" + 
		"    x = @Readonly int[][] @Normal(Value = 0) [].class;\n" + 
		"    x = @Readonly int @SingleMember(0) [][][].class;\n" + 
		"    x = @Readonly int[] @Marker [][].class;\n" + 
		"    x = @Readonly int.class;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0020", expectedUnitToString);
}
//type class literal expression
public void test0021() throws IOException {
	String source = "public class X {\n" + 
			"	<T extends Y<@A String @C[][]@B[]> & Cloneable> void foo(T t) {}\n" + 
			"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  <T extends Y<@A String @C [][] @B []> & Cloneable>void foo(T t) {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0021", expectedUnitToString);
}
//type class literal expression
public void test0022() throws IOException {
	String source = 
	"public class X {\n" + 
	"	public boolean foo(String s) {\n" + 
	"		return (s instanceof @C('_') Object[]);\n" + 
	"	}\n" + 
	"	public Object foo1(String s) {\n" + 
	"		return new @B(3) @A(\"new Object\") Object[] {};\n" + 
	"	}\n" + 
	"	public Class foo2(String s) {\n" + 
	"		return @B(4) Object[].class;\n" + 
	"	}\n" + 
	"	public Class foo3(String s) {\n" + 
	"		return @A(\"int class literal\")  @B(5) int[].class;\n" + 
	"	}\n" + 
	"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public boolean foo(String s) {\n" + 
		"    return (s instanceof @C(\'_\') Object[]);\n" + 
		"  }\n" + 
		"  public Object foo1(String s) {\n" + 
		"    return new @B(3) @A(\"new Object\") Object[]{};\n" + 
		"  }\n" + 
		"  public Class foo2(String s) {\n" + 
		"    return @B(4) Object[].class;\n" + 
		"  }\n" + 
		"  public Class foo3(String s) {\n" + 
		"    return @A(\"int class literal\") @B(5) int[].class;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0022", expectedUnitToString );
}
//check locations
public void test0023() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@H String @E[] @F[] @G[] field;\n" + 
		"	@A Map<@B String, @C List<@D Object>> field2;\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @H String @E [] @F [] @G [] field;\n" + 
		"  @A Map<@B String, @C List<@D Object>> field2;\n" + 
		"  @A Map<@B String, @H String @E [] @F [] @G []> field3;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0023", expectedUnitToString);
}
//check locations
public void test0024() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@H String @E[] @F[] @G[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @H String @E [] @F [] @G [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0024", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@E"));
	assertEquals("Wrong location", "{0}", locations.get("@F"));
	assertEquals("Wrong location", "{1}", locations.get("@G"));
	assertEquals("Wrong location", "{2}", locations.get("@H"));
}
//check locations
public void test0025() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @H String> field3;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @H String> field3;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0025", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 3, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "{0}", locations.get("@B"));
	assertEquals("Wrong location", "{1}", locations.get("@H"));
}
//check locations
public void test0026() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @H String @E [] @F [] @G []> field3;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0026", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 6, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "{0}", locations.get("@B"));
	assertEquals("Wrong location", "{1}", locations.get("@E"));
	assertEquals("Wrong location", "{1,0}", locations.get("@F"));
	assertEquals("Wrong location", "{1,1}", locations.get("@G"));
	assertEquals("Wrong location", "{1,2}", locations.get("@H"));
}
//check locations
public void test0027() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @C List<@H String @E[][] @G[]>> field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @C List<@H String @E [][] @G []>> field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0027", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 6, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "{0}", locations.get("@B"));
	assertEquals("Wrong location", "{1}", locations.get("@C"));
	assertEquals("Wrong location", "{1,0,2}", locations.get("@H"));
	assertEquals("Wrong location", "{1,0}", locations.get("@E"));
	assertEquals("Wrong location", "{1,0,1}", locations.get("@G"));
}
//check locations
public void test0028() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @C List<@H String @E[][] @G[]>>[] @I[] @J[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @C List<@H String @E [][] @G []>>[] @I [] @J [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0028", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", "{0}", locations.get("@I"));
	assertEquals("Wrong location", "{1}", locations.get("@J"));
	assertEquals("Wrong location", "{2}", locations.get("@A"));
	assertEquals("Wrong location", "{2,0}", locations.get("@B"));
	assertEquals("Wrong location", "{2,1}", locations.get("@C"));
	assertEquals("Wrong location", "{2,1,0,2}", locations.get("@H"));
	assertEquals("Wrong location", "{2,1,0}", locations.get("@E"));
	assertEquals("Wrong location", "{2,1,0,1}", locations.get("@G"));
}
//check locations
public void test0029() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @C List<@H String @E[][] @G[]>> @I[][] @J[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @C List<@H String @E [][] @G []>> @I [][] @J [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0029", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", null, locations.get("@I"));
	assertEquals("Wrong location", "{1}", locations.get("@J"));
	assertEquals("Wrong location", "{2}", locations.get("@A"));
	assertEquals("Wrong location", "{2,0}", locations.get("@B"));
	assertEquals("Wrong location", "{2,1}", locations.get("@C"));
	assertEquals("Wrong location", "{2,1,0,2}", locations.get("@H"));
	assertEquals("Wrong location", "{2,1,0}", locations.get("@E"));
	assertEquals("Wrong location", "{2,1,0,1}", locations.get("@G"));
}
//check locations
public void test0030() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@C List<@H String @E[][] @G[]>, String @B[] @D[]> @I[] @F[] @J[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@C List<@H String @E [][] @G []>, String @B [] @D []> @I [] @F [] @J [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0030", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 10, locations.size());
	assertEquals("Wrong location", null, locations.get("@I"));
	assertEquals("Wrong location", "{0}", locations.get("@F"));
	assertEquals("Wrong location", "{1}", locations.get("@J"));
	assertEquals("Wrong location", "{2}", locations.get("@A"));
	assertEquals("Wrong location", "{2,0}", locations.get("@C"));
	assertEquals("Wrong location", "{2,0,0}", locations.get("@E"));
	assertEquals("Wrong location", "{2,0,0,1}", locations.get("@G"));
	assertEquals("Wrong location", "{2,0,0,2}", locations.get("@H"));
	assertEquals("Wrong location", "{2,1,0}", locations.get("@D"));
	assertEquals("Wrong location", "{2,1}", locations.get("@B"));
}
//check locations
public void test0031() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@C List<@H String @E[][] @G[]>, @B List<String [] @D[]>> [] @I[] @F[] @J[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@C List<@H String @E [][] @G []>, @B List<String[] @D []>>[] @I [] @F [] @J [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0030", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 10, locations.size());
	assertEquals("Wrong location", "{0}", locations.get("@I"));
	assertEquals("Wrong location", "{1}", locations.get("@F"));
	assertEquals("Wrong location", "{2}", locations.get("@J"));
	assertEquals("Wrong location", "{3}", locations.get("@A"));
	assertEquals("Wrong location", "{3,0}", locations.get("@C"));
	assertEquals("Wrong location", "{3,0,0}", locations.get("@E"));
	assertEquals("Wrong location", "{3,0,0,1}", locations.get("@G"));
	assertEquals("Wrong location", "{3,0,0,2}", locations.get("@H"));
	assertEquals("Wrong location", "{3,1}", locations.get("@B"));
	assertEquals("Wrong location", "{3,1,0,0}", locations.get("@D"));
}
//check locations
public void test0032() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @C List<@D Object>> field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @C List<@D Object>> field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0030", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "{0}", locations.get("@B"));
	assertEquals("Wrong location", "{1}", locations.get("@C"));
	assertEquals("Wrong location", "{1,0}", locations.get("@D"));
}
//check locations
public void test0033() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@H String @E[] @F[] @G[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @H String @E [] @F [] @G [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0030", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@E"));
	assertEquals("Wrong location", "{0}", locations.get("@F"));
	assertEquals("Wrong location", "{1}", locations.get("@G"));
	assertEquals("Wrong location", "{2}", locations.get("@H"));
}
//check locations
public void test0034() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B Comparable<@C Object @D[] @E[] @F[]>, @G List<@H Document>> field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B Comparable<@C Object @D [] @E [] @F []>, @G List<@H Document>> field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0030", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "{0}", locations.get("@B"));
	assertEquals("Wrong location", "{0,0,2}", locations.get("@C"));
	assertEquals("Wrong location", "{0,0}", locations.get("@D"));
	assertEquals("Wrong location", "{0,0,0}", locations.get("@E"));
	assertEquals("Wrong location", "{0,0,1}", locations.get("@F"));
	assertEquals("Wrong location", "{1}", locations.get("@G"));
	assertEquals("Wrong location", "{1,0}", locations.get("@H"));
}
//check locations
public void test0036() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A java.util.Map<@B Comparable<@C Object @D[] @E[] @F[]>, @G List<@H Document>> field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A java.util.Map<@B Comparable<@C Object @D [] @E [] @F []>, @G List<@H Document>> field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0030", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "{0}", locations.get("@B"));
	assertEquals("Wrong location", "{0,0,2}", locations.get("@C"));
	assertEquals("Wrong location", "{0,0}", locations.get("@D"));
	assertEquals("Wrong location", "{0,0,0}", locations.get("@E"));
	assertEquals("Wrong location", "{0,0,1}", locations.get("@F"));
	assertEquals("Wrong location", "{1}", locations.get("@G"));
	assertEquals("Wrong location", "{1,0}", locations.get("@H"));
}
//check locations
public void test0035() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@B Map<? extends Z, ? extends @A Z> field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @B Map<? extends Z, ? extends @A Z> field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0030", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 2, locations.size());
	assertEquals("Wrong location", null, locations.get("@B"));
	assertEquals("Wrong location", "{1}", locations.get("@A"));
}
//check locations
public void test0037() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@H java.lang.String @E[] @F[] @G[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @H java.lang.String @E [] @F [] @G [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0024", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@E"));
	assertEquals("Wrong location", "{0}", locations.get("@F"));
	assertEquals("Wrong location", "{1}", locations.get("@G"));
	assertEquals("Wrong location", "{2}", locations.get("@H"));
}
//check locations
public void test0038() throws IOException {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B java.lang.String, @H java.lang.String @E[] @F[] @G[]> field3;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B java.lang.String, @H java.lang.String @E [] @F [] @G []> field3;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0026", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 6, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "{0}", locations.get("@B"));
	assertEquals("Wrong location", "{1}", locations.get("@E"));
	assertEquals("Wrong location", "{1,0}", locations.get("@F"));
	assertEquals("Wrong location", "{1,1}", locations.get("@G"));
	assertEquals("Wrong location", "{1,2}", locations.get("@H"));
}
public void test0039() throws IOException {
	String source =
		"@Marker class A {}\n;" +
		"@Marker class B extends @Marker A {}\n" +
		"@Marker class C extends @Marker @SingleMember(0) A {}\n" +
		"@Marker class D extends @Marker @SingleMember(0) @Normal(value = 0) A {}\n" +
		"@Marker class E extends B {}\n;";

	String expectedUnitToString =
		"@Marker class A {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class B extends @Marker A {\n" +
		"  B() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class C extends @Marker @SingleMember(0) A {\n" +
		"  C() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class D extends @Marker @SingleMember(0) @Normal(value = 0) A {\n" +
		"  D() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class E extends B {\n" +
		"  E() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}

// To test Parser.consumeAdditionalBound() with Type annotations
public void test0040a() throws IOException {
	String source =
		"@Marker interface I<@Negative T> {}\n" +
		"@SingleMember(0) interface J<@Positive T> {}\n" +
		"@Marker class A implements I<@SingleMember(0) A>, J<@Marker A> {}\n" +
		"@Normal(value = 1) class X<E extends @Positive A & @Marker I<A> & @Marker @SingleMember(1) J<@Readonly A>>  {\n" +
		"}";
	String expectedUnitToString =
		"@Marker interface I<@Negative T> {\n" +
		"}\n" +
		"@SingleMember(0) interface J<@Positive T> {\n" +
		"}\n" +
		"@Marker class A implements I<@SingleMember(0) A>, J<@Marker A> {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Normal(value = 1) class X<E extends @Positive A & @Marker I<A> & @Marker @SingleMember(1) J<@Readonly A>> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeAdditionalBound() with Type annotations
public void test0040b() throws IOException {
	String source =
		"import java.io.Serializable;\n" +
		"\n" +
		"@SingleMember(10) class X<T extends @Marker Serializable & @Normal(value = 10) Runnable, V extends @Marker T> {\n" +
		"	@Negative T t;\n" +
		"	@Marker X(@Readonly T t) {\n" +
		"		this.t = t;\n" +
		"	}\n" +
		"	void foo() @Marker {\n" +
		"		(this == null ? t : t).run();\n" +
		"		((@Marker V) t).run();\n" +
		"	}\n" +
		"	public static void main(@Readonly String @Marker [] args) {\n" +
		"		new @Marker  X<@Marker A, @Negative A>(new @Marker A()).foo();\n" +
		"	}\n" +
		"}\n" +
		"@Marker class A implements @Marker Serializable, @SingleMember(1) Runnable {\n" +
		"	public void run() {\n" +
		"		System.out.print(\"AA\");\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"import java.io.Serializable;\n" +
		"@SingleMember(10) class X<T extends @Marker Serializable & @Normal(value = 10) Runnable, V extends @Marker T> {\n" +
		"  @Negative T t;\n" +
		"  @Marker X(@Readonly T t) {\n" +
		"    super();\n" +
		"    this.t = t;\n" +
		"  }\n" +
		"  void foo() @Marker {\n" +
		"    ((this == null) ? t : t).run();\n" +
		"    (@Marker V) t.run();\n" +
		"  }\n" +
		"  public static void main(@Readonly String @Marker [] args) {\n" +
		"    new @Marker X<@Marker A, @Negative A>(new @Marker A()).foo();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class A implements @Marker Serializable, @SingleMember(1) Runnable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void run() {\n" +
		"    System.out.print(\"AA\");\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0041a() throws IOException {
	String source =
		"class X {\n" +
		"	@Marker X() {\n" +
		"		System.out.print(\"new X created\");\n" +
		"	}\n" +
		"  	void f() throws @Marker InstantiationException {\n" +
		"       X testX;\n" +
		"		testX = new @Readonly @Negative X();\n" +
		"		Double d;\n" +
		"		d = new @Marker @Positive Double(1.1);\n" +
		"     	throw new @Positive @Normal(value = 10) InstantiationException(\"test\");\n" +
		"   }\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  @Marker X() {\n" +
		"    super();\n" +
		"    System.out.print(\"new X created\");\n" +
		"  }\n" +
		"  void f() throws @Marker InstantiationException {\n" +
		"    X testX;\n" +
		"    testX = new @Readonly @Negative X();\n" +
		"    Double d;\n" +
		"    d = new @Marker @Positive Double(1.1);\n" +
		"    throw new @Positive @Normal(value = 10) InstantiationException(\"test\");\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0041b() throws IOException {
	String source =
		"class X {\n" +
		"	@Marker X() {\n" +
		"		System.out.print(\"new X created\");\n" +
		"	}\n" +
		"	@Marker class Inner {\n" +
		"		@Normal(value = 10) Inner(){\n" +
		"			System.out.print(\"X.Inner created\");\n" +
		"		}\n" +
		"	}\n" +
		"	public String getString(){\n" +
		"		return \"hello\";\n" +
		"	}\n" +
		"  	void f() @Marker {\n" +
		"       String testString;\n" +
		"		testString = new @Readonly @Negative X().getString();\n" +
		"		X.Inner testInner;\n" +
		"		testInner = new @Readonly X.Inner();\n" +
		"		int i;\n" +
		"		for(i = 0; i < 10; i++)\n" +
		"			System.out.print(\"test\");\n" +
		"   }\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  @Marker class Inner {\n" +
		"    @Normal(value = 10) Inner() {\n" +
		"      super();\n" +
		"      System.out.print(\"X.Inner created\");\n" +
		"    }\n" +
		"  }\n" +
		"  @Marker X() {\n" +
		"    super();\n" +
		"    System.out.print(\"new X created\");\n" +
		"  }\n" +
		"  public String getString() {\n" +
		"    return \"hello\";\n" +
		"  }\n" +
		"  void f() @Marker {\n" +
		"    String testString;\n" +
		"    testString = new @Readonly @Negative X().getString();\n" +
		"    X.Inner testInner;\n" +
		"    testInner = new @Readonly X.Inner();\n" +
		"    int i;\n" +
		"    for (i = 0; (i < 10); i ++) \n" +
		"      System.out.print(\"test\");\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0041c() throws IOException {
	String source =
		"import java.io.Serializable;\n" +
		"class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		new @Marker Serializable() {\n" +
		"		};\n" +
		"		new @Positive @Marker Serializable() {\n" +
		"			public long serialVersion;\n" +
		"		};\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.io.Serializable;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new @Marker Serializable() {\n" +
		"    };\n" +
		"    new @Positive @Marker Serializable() {\n" +
		"      public long serialVersion;\n" +
		"    };\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0041d() throws IOException {
	String source =
		"import java.io.Serializable;\n" +
		"class X<T>{\n" +
		"	public void f() {\n" +
		"		X testX;\n" +
		"		testX = new @Marker @SingleMember(10) X<@Negative Integer>();\n" +
		"		System.out.print(\"object created\");\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.io.Serializable;\n" +
		"class X<T> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void f() {\n" +
		"    X testX;\n" +
		"    testX = new @Marker @SingleMember(10) X<@Negative Integer>();\n" +
		"    System.out.print(\"object created\");\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0041e() throws IOException {
	String source =
		"class X <@Marker T extends @Readonly String> {\n" +
		"    T foo(T t) {\n" +
		"        return t;\n" +
		"    }\n" +
		"    public static void main(String[] args) {\n" +
		"        new @Readonly X<String>().baz(\"SUCCESS\");\n" +	// Parser.classInstanceCreation called
		"    }\n" +
		"    void baz(final T t) {\n" +
		"        new @Readonly @Marker Object() {\n" +	// Parser.classInstanceCreation called
		"            void print() {\n" +
		"            }\n" +
		"        }.print();\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@Marker T extends @Readonly String> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  T foo(T t) {\n" +
		"    return t;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new @Readonly X<String>().baz(\"SUCCESS\");\n" +
		"  }\n" +
		"  void baz(final T t) {\n" +
		"    new @Readonly @Marker Object() {\n" +
		"  void print() {\n" +
		"  }\n" +
		"}.print();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0042a() throws IOException {
	String source =
		"class X <@Marker T extends @Readonly String> {\n" +
		"    public static void main(String[] args) {\n" +
		"		int [] x1;\n" +
		"		x1 = new int @Marker @SingleMember(2) [] {-1, -2};\n" +
		"       Integer [][] x2;\n" +
		"		x2 = new @Positive Integer @Marker @SingleMember(3) [] @SingleMember(3) [] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@Marker T extends @Readonly String> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    int[] x1;\n" +
		"    x1 = new int @Marker @SingleMember(2) []{(- 1), (- 2)};\n" +
		"    Integer[][] x2;\n" +
		"    x2 = new @Positive Integer @Marker @SingleMember(3) [] @SingleMember(3) []{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0042b() throws IOException {
	String source =
		"class X {\n" +
		"	static class T {\n" +
		"		public @Readonly Object @Normal(value = 10) [] f() @Marker {\n" +
		"			return new @Readonly Object @Normal(value = 10) [] {this, T.this};\n" +
		"		}\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  static class T {\n" +
		"    T() {\n" +
		"      super();\n" +
		"    }\n" +
		"    public @Readonly Object @Normal(value = 10) [] f() @Marker {\n" +
		"      return new @Readonly Object @Normal(value = 10) []{this, T.this};\n" +
		"    }\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0042c() throws IOException {
	String source =
		"class X {\n" +
		"    public static void main(String[] args) {\n" +
		"        java.util.Arrays.asList(new @Readonly Object @SingleMember(1) [] {\"1\"});\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    java.util.Arrays.asList(new @Readonly Object @SingleMember(1) []{\"1\"});\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0042d() throws IOException {
	String source =
		"class X {\n" +
		"	public boolean test() {\n" +
		"		String[] s;\n" +
		"		s = foo(new @Marker String @SingleMember(1) []{\"hello\"});\n" +
		"		return s != null;\n" +
		"	}\n" +
		"	public <@Marker F> F @SingleMember(1) [] foo(F[] f) {\n" +
		"		return f;\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public boolean test() {\n" +
		"    String[] s;\n" +
		"    s = foo(new @Marker String @SingleMember(1) []{\"hello\"});\n" +
		"    return (s != null);\n" +
		"  }\n" +
		"  public <@Marker F>F @SingleMember(1) [] foo(F[] f) {\n" +
		"    return f;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0042e() throws IOException {
	String source =
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"@Marker class Deejay {\n" +
		"	@Marker class Counter<@Marker T> {}\n" +
		"	public void f(String[] args) {\n" +
		"		Counter<@Positive Integer> songCounter;\n" +
		"		songCounter = new Counter<@Positive Integer>();\n" +
		"		Counter<@Readonly String> genre;\n" +
		"		genre = new Counter<@Readonly String>();\n" +
		"		List<@Marker Counter<?>> list1;\n" +
		"		list1 = Arrays.asList(new @Marker Counter<?> @Normal(value = 2) @Marker [] {songCounter, genre});\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"@Marker class Deejay {\n" +
		"  @Marker class Counter<@Marker T> {\n" +
		"    Counter() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  Deejay() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void f(String[] args) {\n" +
		"    Counter<@Positive Integer> songCounter;\n" +
		"    songCounter = new Counter<@Positive Integer>();\n" +
		"    Counter<@Readonly String> genre;\n" +
		"    genre = new Counter<@Readonly String>();\n" +
		"    List<@Marker Counter<?>> list1;\n" +
		"    list1 = Arrays.asList(new @Marker Counter<?> @Normal(value = 2) @Marker []{songCounter, genre});\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0043a() throws IOException {
	String source =
		"class X <@Marker T extends @Readonly String> {\n" +
		"    public static void main(String[] args) {\n" +
		"		int [] x1;\n" +
		"		x1 = new int @Marker @SingleMember(10) [10];\n" +
		"       Integer [][] x2;\n" +
		"		x2 = new @Positive Integer @Marker [10] @Normal(value = 10) [10];\n" +
		"		char[][] tokens;\n" +
		"		tokens = new char @SingleMember(0) [0] @Normal(value = 10) @Marker [];\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@Marker T extends @Readonly String> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    int[] x1;\n" +
		"    x1 = new int @Marker @SingleMember(10) [10];\n" +
		"    Integer[][] x2;\n" +
		"    x2 = new @Positive Integer @Marker [10] @Normal(value = 10) [10];\n" +
		"    char[][] tokens;\n" +
		"    tokens = new char @SingleMember(0) [0] @Normal(value = 10) @Marker [];\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0043b() throws IOException {
	String source =
		"class X {\n" +
		"	public @Readonly Object @Normal(value = 10) [] f() @Marker {\n" +
		"		return new @Readonly Object @Normal(value = 10) [10];\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Readonly Object @Normal(value = 10) [] f() @Marker {\n" +
		"    return new @Readonly Object @Normal(value = 10) [10];\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0043c() throws IOException {
	String source =
		"class X {\n" +
		"	public boolean test() {\n" +
		"		String[] s;\n" +
		"		s = foo(new @Marker String @SingleMember(1) [10]);\n" +
		"		return s != null;\n" +
		"	}\n" +
		"	public <@Marker F> F @SingleMember(1) [] foo(F[] f) {\n" +
		"		return f;\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public boolean test() {\n" +
		"    String[] s;\n" +
		"    s = foo(new @Marker String @SingleMember(1) [10]);\n" +
		"    return (s != null);\n" +
		"  }\n" +
		"  public <@Marker F>F @SingleMember(1) [] foo(F[] f) {\n" +
		"    return f;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0043d() throws IOException {
	String source =
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"class X<@Marker T> {\n" +
		"	public void test() {\n" +
		"		List<@Marker X<?>> a;\n" +
		"		a = Arrays.asList(new @Marker X<?> @SingleMember(0) [0]);\n" +
		"		String @Marker [] @SingleMember(1) [] x;\n" +
		"		x = new @Readonly String @Normal(value = 5) [5] @SingleMember(1) [1];\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"class X<@Marker T> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test() {\n" +
		"    List<@Marker X<?>> a;\n" +
		"    a = Arrays.asList(new @Marker X<?> @SingleMember(0) [0]);\n" +
		"    String @Marker [] @SingleMember(1) [] x;\n" +
		"    x = new @Readonly String @Normal(value = 5) [5] @SingleMember(1) [1];\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0043e() throws IOException {
	String source =
		"import java.util.*;\n" +
		"class X {\n" +
		"    public Integer[] getTypes() {\n" +
		"        List<@Positive Integer> list;\n" +
		"		 list = new ArrayList<@Positive Integer>();\n" +
		"        return list == null \n" +
		"            ? new @Positive Integer @SingleMember(0) [0] \n" +
		"            : list.toArray(new @Positive Integer @Marker [list.size()]);\n" +
		"    }\n" +
		"}";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public Integer[] getTypes() {\n" +
		"    List<@Positive Integer> list;\n" +
		"    list = new ArrayList<@Positive Integer>();\n" +
		"    return ((list == null) ? new @Positive Integer @SingleMember(0) [0] : list.toArray(new @Positive Integer @Marker [list.size()]));\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
public void test0044a() throws IOException {
	String source =
		"import java.util.*;\n" +
		"\n" +
		"@Marker class X {\n" +
		"    Vector<Object> data;\n" +
		"    public void t() {\n" +
		"        Vector<@Readonly Object> v;\n" +
		" 		 v = (@Marker @SingleMember(0) Vector<@Readonly Object>) data.elementAt(0);\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"@Marker class X {\n" +
		"  Vector<Object> data;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void t() {\n" +
		"    Vector<@Readonly Object> v;\n" +
		"    v = (@Marker @SingleMember(0) Vector<@Readonly Object>) data.elementAt(0);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
// To test Parser.consumeClassHeaderExtends() with Type Annotations
public void test0044b() throws IOException {
	String source =
		"class X<E> {\n" +
		"    X<@Readonly String> bar() {\n" +
		"    	return (@Marker AX<@Readonly String>) new X<@Readonly String>();\n" +
		"    }\n" +
		"    X<@Readonly String> bar(Object o) {\n" +
		"    	return (@Marker AX<@Readonly String>) o;\n" +
		"    }\n" +
		"    X<@Negative E> foo(Object o) {\n" +
		"    	return (@Marker @Normal(value = 10) AX<@Negative E>) o;\n" +
		"    }    \n" +
		"    X<E> baz(Object o) {\n" +
		"    	return (@Marker AX<E>) null;\n" +
		"    }\n" +
		"    X<String> baz2(BX bx) {\n" +
		"    	return (@Marker @SingleMember(10) X<String>) bx;\n" +
		"    }\n" +
		"}\n" +
		"@Normal(value = 1) class AX<@Marker F> extends @Marker X<@SingleMember(10)F> {}\n" +
		"@Normal(value = 2) class BX extends @Marker @SingleMember(1) AX<@Readonly String> {}\n";
	String expectedUnitToString =
		"class X<E> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  X<@Readonly String> bar() {\n" +
		"    return (@Marker AX<@Readonly String>) new X<@Readonly String>();\n" +
		"  }\n" +
		"  X<@Readonly String> bar(Object o) {\n" +
		"    return (@Marker AX<@Readonly String>) o;\n" +
		"  }\n" +
		"  X<@Negative E> foo(Object o) {\n" +
		"    return (@Marker @Normal(value = 10) AX<@Negative E>) o;\n" +
		"  }\n" +
		"  X<E> baz(Object o) {\n" +
		"    return (@Marker AX<E>) null;\n" +
		"  }\n" +
		"  X<String> baz2(BX bx) {\n" +
		"    return (@Marker @SingleMember(10) X<String>) bx;\n" +
		"  }\n" +
		"}\n" +
		"@Normal(value = 1) class AX<@Marker F> extends @Marker X<@SingleMember(10) F> {\n" +
		"  AX() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Normal(value = 2) class BX extends @Marker @SingleMember(1) AX<@Readonly String> {\n" +
		"  BX() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
public void test0045a() throws IOException {
	String source =
		"import java.lang.reflect.Array;\n" +
		"@Marker class X<@Readonly T> {\n" +
		"	T @SingleMember(0) [] theArray;\n" +
		"	public X(Class<T> clazz) {\n" +
		"		theArray = (@Marker @SingleMember(0) T @Normal(value = 10) []) Array.newInstance(clazz, 10); // Compiler warning\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.lang.reflect.Array;\n" +
		"@Marker class X<@Readonly T> {\n" +
		"  T @SingleMember(0) [] theArray;\n" +
		"  public X(Class<T> clazz) {\n" +
		"    super();\n" +
		"    theArray = (@Marker @SingleMember(0) T @Normal(value = 10) []) Array.newInstance(clazz, 10);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
public void test0045b() throws IOException {
	String source =
		"import java.util.*;\n" +
		"class X {\n" +
		"    void method(Object o) {\n" +
		"		 if (o instanceof String[]){\n" +
		"			 String[] s;\n" +
		"			 s = (@Marker @Readonly String @Marker []) o;\n" +
		"		 }\n" +
		"        if (o instanceof @Readonly List<?>[]) {\n" +
		"            List<?>[] es;\n" +
		"			 es = (@Marker List<?> @SingleMember(0) []) o;\n" +
		"        }\n" +
		"    }\n" +
		"}";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void method(Object o) {\n" +
		"    if ((o instanceof String[]))\n" +
		"        {\n" +
		"          String[] s;\n" +
		"          s = (@Marker @Readonly String @Marker []) o;\n" +
		"        }\n" +
		"    if ((o instanceof @Readonly List<?>[]))\n" +
		"        {\n" +
		"          List<?>[] es;\n" +
		"          es = (@Marker List<?> @SingleMember(0) []) o;\n" +
		"        }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}


// To test Parser.consumeCastExpressionWithPrimitiveType() with Type Annotations
public void test0046a() throws IOException {
	String source =
		"import java.util.HashMap;\n" +
		"class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		HashMap<Byte, Byte> subst;\n" +
		"		subst = new HashMap<Byte, Byte>();\n" +
		"		subst.put((@Marker byte)1, (@Positive byte)1);\n" +
		"		if (1 + subst.get((@Positive @Normal(value = 10) byte)1) > 0.f) {\n" +
		"			System.out.println(\"SUCCESS\");\n" +
		"		}		\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"import java.util.HashMap;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    HashMap<Byte, Byte> subst;\n" +
		"    subst = new HashMap<Byte, Byte>();\n" +
		"    subst.put((@Marker byte) 1, (@Positive byte) 1);\n" +
		"    if (((1 + subst.get((@Positive @Normal(value = 10) byte) 1)) > 0.f))\n" +
		"        {\n" +
		"          System.out.println(\"SUCCESS\");\n" +
		"        }\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithPrimitiveType() with Type Annotations
public void test0046b() throws IOException {
	String source =
		"class X{\n" +
		"	private float x, y, z;\n" +
		"	float magnitude () {\n" +
		"		return (@Marker @Positive float) Math.sqrt((x*x) + (y*y) + (z*z));\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class X {\n" +
		"  private float x;\n" +
		"  private float y;\n" +
		"  private float z;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  float magnitude() {\n" +
		"    return (@Marker @Positive float) Math.sqrt((((x * x) + (y * y)) + (z * z)));\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithQualifiedGenericsArray() with Type Annotations
// Javac version b76 crashes on type annotations on type arguments to parameterized classes
// in a qualified generic reference
public void test0047() throws IOException {
	String source =
		"class C1<T> {\n" +
		"	class C11 {	}\n" +
		"	@Marker class C12 {\n" +
		"		T t;\n" +
		"		C1<@Readonly T>.C11 m() {\n" +
		"			C1<@Readonly T>.C11[] ts;\n" +
		"			ts = (@Marker C1<@Readonly T>.C11[]) new @Marker C1<?>.C11 @Normal(value = 5) [5];\n" +
		"			return ts;\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class C1<T> {\n" +
		"  class C11 {\n" +
		"    C11() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  @Marker class C12 {\n" +
		"    T t;\n" +
		"    C12() {\n" +
		"      super();\n" +
		"    }\n" +
		"    C1<@Readonly T>.C11 m() {\n" +
		"      C1<@Readonly T>.C11[] ts;\n" +
		"      ts = ( @Marker C1<@Readonly T>.C11[]) new  @Marker C1<?>.C11 @Normal(value = 5) [5];\n" +
		"      return ts;\n" +
		"    }\n" +
		"  }\n" +
		"  C1() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeFormalParameter() with Type Annotations
public void test0048a() throws IOException {
	String source =
		"class X {\n" +
		"	int field;" +
		"	public void test(@Marker X x,@Positive int i){\n" +
		"		x.field = i;\n" +
		"	}\n" +
		"	public static void main(@Readonly String args @Normal(10) []){" +
		"		System.exit(0);\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class X {\n" +
		"  int field;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test(@Marker X x, @Positive int i) {\n" +
		"    x.field = i;\n" +
		"  }\n" +
		"  public static void main(@Readonly String @Normal(10) [] args) {\n" +
		"    System.exit(0);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeFormalParameter() with Type Annotations
public void test0048b() throws IOException {
	String source =
		"class X<@Marker T> {\n" +
		"	T field;" +
		"	public void test(@Marker @SingleMember(1) X<? extends @Marker Object> x,@Positive T i){\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@Marker T> {\n" +
		"  T field;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test(@Marker @SingleMember(1) X<? extends @Marker Object> x, @Positive T i) {\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments()
// with Type Annotations
// Javac b76 crashes with type annotations in qualified class instance creation expression
public void test0049() throws IOException {
	String source =
		"class X {\n" +
		"	class MX {\n" +
		"		@Marker <T> MX(T t){\n" +
		"			System.out.println(t);\n" +
		"		}\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		new @Marker @SingleMember(10) X().new <@Readonly String> @Marker MX(\"SUCCESS\");\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class X {\n" +
		"  class MX {\n" +
		"    @Marker <T>MX(T t) {\n" +
		"      super();\n" +
		"      System.out.println(t);\n" +
		"    }\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new @Marker @SingleMember(10) X().new <@Readonly String>@Marker MX(\"SUCCESS\");\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeClassInstanceCreationExpressionWithTypeArguments()
// with Type Annotations
public void test0050() throws IOException {
	String source =
		"class X {\n" +
		"	public <T> X(T t){\n" +
		"		System.out.println(t);\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		new <@Readonly String> @Marker @SingleMember(0) X(\"SUCCESS\");\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class X {\n" +
		"  public <T>X(T t) {\n" +
		"    super();\n" +
		"    System.out.println(t);\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new <@Readonly String>@Marker @SingleMember(0) X(\"SUCCESS\");\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeEnhancedForStatementHeaderInit() with Type Annotations
public void test0051() throws IOException {
	String source =
		"import java.util.*;\n" +
		"class X {\n" +
		"   List list() { return null; }\n" +
		"   void m2() { for (@SingleMember(10) Iterator<@Marker X> i = list().iterator(); i.hasNext();); }\n" +
		"	void m3() {\n" +
		"		Integer [] array;\n" +
		"		array = new Integer [] {1, 2, 3};\n" +
		"		List<List<X>> xList;\n" +
		"		xList = null;\n" +
		"		for(@Positive @SingleMember(10) Integer i: array) {}\n" +
		"		for(@Marker @Normal(value = 5) List<@Readonly X> x: xList) {}\n" +
		"	}" +
		"}\n";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  List list() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  void m2() {\n" +
		"    for (@SingleMember(10) Iterator<@Marker X> i = list().iterator();; i.hasNext(); ) \n" +
		"      ;\n" +
		"  }\n" +
		"  void m3() {\n" +
		"    Integer[] array;\n" +
		"    array = new Integer[]{1, 2, 3};\n" +
		"    List<List<X>> xList;\n" +
		"    xList = null;\n" +
		"    for (@Positive @SingleMember(10) Integer i : array) \n" +
		"      {\n" +
		"      }\n" +
		"    for (@Marker @Normal(value = 5) List<@Readonly X> x : xList) \n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_COMPLETION_PARSER & ~CHECK_SELECTION_PARSER & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
	expectedUnitToString =
		"import java.util.*;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  List list() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  void m2() {\n" +
		"    for (@SingleMember(10) Iterator<@Marker X> i;; i.hasNext(); ) \n" +
		"      ;\n" +
		"  }\n" +
		"  void m3() {\n" +
		"    Integer[] array;\n" +
		"    array = new Integer[]{1, 2, 3};\n" +
		"    List<List<X>> xList;\n" +
		"    xList = null;\n" +
		"    for (@Positive @SingleMember(10) Integer i : array) \n" +
		"      {\n" +
		"      }\n" +
		"    for (@Marker @Normal(value = 5) List<@Readonly X> x : xList) \n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_COMPLETION_PARSER & CHECK_SELECTION_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeEnterAnonymousClassBody() with Type Annotations
public void test0052a() throws IOException {
	String source =
		"@Marker class X {\n" +
		"  void f() @Normal(value = 5) {\n" +
		"    new @Marker @SingleMember(10) Object() {\n" +
		"      void foo(){\n" +
		"        System.out.println(\"test\");\n" +
		"      }\n" +
		"    }.foo();\n" +
		"  }\n" +
		"}";
	String expectedUnitToString =
		"@Marker class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void f() @Normal(value = 5) {\n" +
		"    new @Marker @SingleMember(10) Object() {\n" +
		"  void foo() {\n" +
		"    System.out.println(\"test\");\n" +
		"  }\n" +
		"}.foo();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeEnterAnonymousClassBody() with Type Annotations
public void test0052b() throws IOException {
	String source =
		"class Toplevel2{\n" +
		"    public boolean foo(){\n" +
		"    Toplevel2 o;\n" +
		"	 o = new @Marker @Normal(value = 5) Toplevel2() { \n" +
		"              public boolean foo() {  return false; }  // no copy in fact\n" +
		"              };\n" +
		"    return o.foo();\n" +
		"  }\n" +
		"}";
	String expectedUnitToString =
		"class Toplevel2 {\n" +
		"  Toplevel2() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public boolean foo() {\n" +
		"    Toplevel2 o;\n" +
		"    o = new @Marker @Normal(value = 5) Toplevel2() {\n" +
		"  public boolean foo() {\n" +
		"    return false;\n" +
		"  }\n" +
		"};\n" +
		"    return o.foo();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeEnterAnonymousClassBody() with Type Annotations
public void test0052c() throws IOException {
	String source =
		"class X <T> {\n" +
		"    T foo(T t) {\n" +
		"        System.out.println(t);\n" +
		"        return t;\n" +
		"    }\n" +
		"    public static void main(String @Normal(value =  5) [] args) {\n" +
		"        new @Marker X<@SingleMember(10) @Normal(value = 5) XY>() {\n" +
		"            void run() {\n" +
		"                foo(new @Marker XY());\n" +
		"            }\n" +
		"        }.run();\n" +
		"    }\n" +
		"}\n" +
		"@Marker class XY {\n" +
		"    public String toString() {\n" +
		"        return \"SUCCESS\";\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X<T> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  T foo(T t) {\n" +
		"    System.out.println(t);\n" +
		"    return t;\n" +
		"  }\n" +
		"  public static void main(String @Normal(value = 5) [] args) {\n" +
		"    new @Marker X<@SingleMember(10) @Normal(value = 5) XY>() {\n" +
		"  void run() {\n" +
		"    foo(new @Marker XY());\n" +
		"  }\n" +
		"}.run();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class XY {\n" +
		"  XY() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public String toString() {\n" +
		"    return \"SUCCESS\";\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeInsideCastExpressionLL1() with Type Annotations
public void test0053() throws IOException {
	String source =
		"class X{\n" +
		"  public void test1(){\n" +
		"    throw (@Marker Error) null; \n" +
		"  }  \n" +
		"  public void test2(){\n" +
		"    String s;\n" +
		"	 s = (@Marker @SingleMember(10) String) null;\n" +
		"	 byte b;\n" +
		"	 b = 0;\n" +
		"	 Byte i;\n" +
		"	 i = (@Positive Byte) b;\n" +
		"  }  \n" +
		"  public void test3(java.io.Serializable name) {\n" +
		"     Object temp;\n" +
		"	  temp = (Object)name;\n" +
		"     System.out.println( (String)temp );\n" +
		"  }\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test1() {\n" +
		"    throw (@Marker Error) null;\n" +
		"  }\n" +
		"  public void test2() {\n" +
		"    String s;\n" +
		"    s = (@Marker @SingleMember(10) String) null;\n" +
		"    byte b;\n" +
		"    b = 0;\n" +
		"    Byte i;\n" +
		"    i = (@Positive Byte) b;\n" +
		"  }\n" +
		"  public void test3(java.io.Serializable name) {\n" +
		"    Object temp;\n" +
		"    temp = (Object) name;\n" +
		"    System.out.println((String) temp);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeInstanceOfExpression() with Type Annotations
public void test0054() throws IOException {
	String source =
		"import java.util.*;\n" +
		"class X <@NonNull T>{\n" +
		" 	public void test1(Object obj) @Marker {\n" +
		"   	if(obj instanceof @Marker @NonNull X) {\n" +
		"		 	X newX;\n" +
		"		 	newX = (@NonNull X) obj;\n" +
		"	 }\n" +
		"   }\n" +
		"	@NonNull T foo(@NonNull T t) @Marker {\n" +
		"       if (t instanceof @NonNull @Marker List<?> @Normal(value = 10) []) {\n" +
		"           List<?> @SingleMember (10) [] es;\n" +
		"			es = (@Marker List<?> @SingleMember(10) []) t;\n" +
		"       }\n" +
		"		if (t instanceof @Marker @Normal(value = 5) X<?>) {\n" +
		"			return t;\n" +
		"		}\n" +
		"		return t;\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"class X<@NonNull T> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test1(Object obj) @Marker {\n" +
		"    if ((obj instanceof @Marker @NonNull X))\n" +
		"        {\n" +
		"          X newX;\n" +
		"          newX = (@NonNull X) obj;\n" +
		"        }\n" +
		"  }\n" +
		"  @NonNull T foo(@NonNull T t) @Marker {\n" +
		"    if ((t instanceof @NonNull @Marker List<?> @Normal(value = 10) []))\n" +
		"        {\n" +
		"          List<?> @SingleMember(10) [] es;\n" +
		"          es = (@Marker List<?> @SingleMember(10) []) t;\n" +
		"        }\n" +
		"    if ((t instanceof @Marker @Normal(value = 5) X<?>))\n" +
		"        {\n" +
		"          return t;\n" +
		"        }\n" +
		"    return t;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER , source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeInstanceOfExpressionWithName() with Type Annotations
public void test0055() throws IOException {
	String source =
		"class Outer<E> {\n" +
		"  Inner inner;\n" +
		"  class Inner {\n" +
		"    E e;\n" +
		"    @NonNull E getOtherElement(Object other) @Marker {\n" +
		"      if (!(other instanceof @Marker @SingleMember(10) Outer<?>.Inner))\n" +
		"       throw new @Marker IllegalArgumentException(String.valueOf(other));\n" +
		"      Inner that;\n" +
		"	   that = (@Marker Inner) other;\n" +
		"      return that.e;\n" +
		"    }\n" +
		"  }\n" +
		"}";
	String expectedUnitToString =
		"class Outer<E> {\n" +
		"  class Inner {\n" +
		"    E e;\n" +
		"    Inner() {\n" +
		"      super();\n" +
		"    }\n" +
		"    @NonNull E getOtherElement(Object other) @Marker {\n" +
		"      if ((! (other instanceof  @Marker @SingleMember(10) Outer<?>.Inner)))\n" +
		"          throw new @Marker IllegalArgumentException(String.valueOf(other));\n" +
		"      Inner that;\n" +
		"      that = (@Marker Inner) other;\n" +
		"      return that.e;\n" +
		"    }\n" +
		"  }\n" +
		"  Inner inner;\n" +
		"  Outer() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER , source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeTypeArgument() with Type Annotations
public void test0056a() throws IOException {
	String source =
		"class X<@SingleMember(1) Xp1 extends @Readonly String, @NonNull Xp2 extends @NonNull Comparable>  extends @Marker XS<@SingleMember(10) Xp2> {\n" +
		"\n" +
		"    public static void main(String @Marker [] args) {\n" +
		"        Integer w;\n" +
		"        w = new @Marker X<@Readonly @SingleMember(10) String,@Positive Integer>().get(new @Positive Integer(12));\n" +
		"        System.out.println(\"SUCCESS\");\n" +
		"	 }\n" +
		"    Xp2 get(Xp2 t) @Marker {\n" +
		"        System.out.print(\"{X::get}\");\n" +
		"        return super.get(t);\n" +
		"    }\n" +
		"}\n" +
		"@Marker class XS <@NonNull XSp1> {\n" +
		"    XSp1 get(XSp1 t) @Marker {\n" +
		"		 @NonNull @SingleMember(10) Y.M mObject;\n" +
		"		 mObject = new @SingleMember(10) @NonNull Y.M();\n" +
		"        System.out.print(\"{XS::get}\");\n" +
		"        return t;\n" +
		"    }\n" +
		"}\n" +
		"class X2<T,E>{}\n" +
		"@Marker class Y extends @Marker X2<@NonNull Y.M, @NonNull @SingleMember(1) Y.N> {\n" +
		"	static class M{}\n" +
		"	static class N extends M{}\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@SingleMember(1) Xp1 extends @Readonly String, @NonNull Xp2 extends @NonNull Comparable> extends @Marker XS<@SingleMember(10) Xp2> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String @Marker [] args) {\n" +
		"    Integer w;\n" +
		"    w = new @Marker X<@Readonly @SingleMember(10) String, @Positive Integer>().get(new @Positive Integer(12));\n" +
		"    System.out.println(\"SUCCESS\");\n" +
		"  }\n" +
		"  Xp2 get(Xp2 t) @Marker {\n" +
		"    System.out.print(\"{X::get}\");\n" +
		"    return super.get(t);\n" +
		"  }\n" +
		"}\n" +
		"@Marker class XS<@NonNull XSp1> {\n" +
		"  XS() {\n" +
		"    super();\n" +
		"  }\n" +
		"  XSp1 get(XSp1 t) @Marker {\n" +
		"    @NonNull @SingleMember(10) Y.M mObject;\n" +
		"    mObject = new @SingleMember(10) @NonNull Y.M();\n" +
		"    System.out.print(\"{XS::get}\");\n" +
		"    return t;\n" +
		"  }\n" +
		"}\n" +
		"class X2<T, E> {\n" +
		"  X2() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class Y extends @Marker X2<@NonNull Y.M, @NonNull @SingleMember(1) Y.N> {\n" +
		"  static class M {\n" +
		"    M() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  static class N extends M {\n" +
		"    N() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  Y() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeTypeArgument() with Type Annotations
public void test0056b() throws IOException {
	String source =
		"class X<A1, A2, A3, A4, A5, A6, A7, A8> {\n" +
		"}\n" +
		"class Y {\n" +
		"	@Marker X<int @Marker [], short @SingleMember(1) [] @Marker [], long[] @NonNull [][], float[] @Marker [] @Normal(value = 5) [][], double[][]@Marker [] @SingleMember(10) [][], boolean[][][][][][], char[] @Marker [][][][][][], Object[][]@Marker [] @SingleMember(10) [] @Normal(value = 5) [][][][][]> x;\n" +
		"}\n";
	String expectedUnitToString =
		"class X<A1, A2, A3, A4, A5, A6, A7, A8> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"class Y {\n" +
		"  @Marker X<int @Marker [], short @SingleMember(1) [] @Marker [], long[] @NonNull [][], float[] @Marker [] @Normal(value = 5) [][], double[][] @Marker [] @SingleMember(10) [][], boolean[][][][][][], char[] @Marker [][][][][][], Object[][] @Marker [] @SingleMember(10) [] @Normal(value = 5) [][][][][]> x;\n" +
		"  Y() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeTypeArgumentReferenceType1() with Type Annotations
public void test0057() throws IOException {
	String source =
		"@Marker class X <@NonNull T> {\n" +
		"    protected T t;\n" +
		"    @Marker X(@NonNull T t) {\n" +
		"        this.t = t;\n" +
		"    }\n" +
		"    public static void main(String[] args) {\n" +
		"	  X<@Marker X<@Readonly @NonNull String>> xs;\n" +
		"	  xs = new @Marker X<@Marker X<@Readonly @NonNull String>>(new @Marker X<@Readonly @NonNull @SingleMember(10) String>(\"SUCCESS\"));\n" +
		"	  System.out.println(xs.t.t);\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"@Marker class X<@NonNull T> {\n" +
		"  protected T t;\n" +
		"  @Marker X(@NonNull T t) {\n" +
		"    super();\n" +
		"    this.t = t;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    X<@Marker X<@Readonly @NonNull String>> xs;\n" +
		"    xs = new @Marker X<@Marker X<@Readonly @NonNull String>>(new @Marker X<@Readonly @NonNull @SingleMember(10) String>(\"SUCCESS\"));\n" +
		"    System.out.println(xs.t.t);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeTypeParameter1WithExtendsAndBounds() and Parser.consumeWildcardBoundsSuper() with
// Type Annotations
public void test0058() throws IOException {
	String source =
		"@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable<@Marker Foo1> {\n" +
		"	public int compareTo(Foo1 arg0) {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n" +
		"class Foo1 {}\n" +
		"@Marker class X<@NonNull T extends @NonNull @Normal (value = 5) Object & @Marker Comparable<? super @NonNull T>> {\n" +
		"    public static void main(String[] args) {\n" +
		"        new @Marker @SingleMember(10) X<@Marker Foo>();\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable<@Marker Foo1> {\n" +
		"  Foo() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public int compareTo(Foo1 arg0) {\n" +
		"    return 0;\n" +
		"  }\n" +
		"}\n" +
		"class Foo1 {\n" +
		"  Foo1() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class X<@NonNull T extends @NonNull @Normal(value = 5) Object & @Marker Comparable<? super @NonNull T>> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new @Marker @SingleMember(10) X<@Marker Foo>();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test Parser.consumeTypeParameter1WithExtendsAndBounds() with Type Annotations
public void test0059() throws IOException {
	String source =
		"@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable {\n" +
		"	public int compareTo(Object arg0) {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n" +
		"class Foo1 {}\n" +
		"@Marker class X<@NonNull T extends @NonNull @Normal (value = 5) Object & @Marker Comparable, @NonNull V extends @Readonly Object> {\n" +
		"    public static void main(String[] args) {\n" +
		"        new @Marker @SingleMember(10) X<@Marker Foo, @SingleMember(0) Foo1>();\n" +
		"		 Class <@NonNull Foo> c;\n" +
		"		 c = @Readonly @NonNull Foo.class;\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable {\n" +
		"  Foo() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public int compareTo(Object arg0) {\n" +
		"    return 0;\n" +
		"  }\n" +
		"}\n" +
		"class Foo1 {\n" +
		"  Foo1() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class X<@NonNull T extends @NonNull @Normal(value = 5) Object & @Marker Comparable, @NonNull V extends @Readonly Object> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new @Marker @SingleMember(10) X<@Marker Foo, @SingleMember(0) Foo1>();\n" +
		"    Class<@NonNull Foo> c;\n" +
		"    c = @Readonly @NonNull Foo.class;\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString );
}

// To test type annotations on static class member access in a declaration
public void test0060() throws IOException {
	String source =
		"@Marker class Foo {\n" +
		"	static @Marker @SingleMember(0) class Foo1 {}\n" +
		"   public static void main(String[] args) {\n" +
		"       @Marker @Normal(value = 5) Foo.Foo1 foo1Object;\n" +
		"		foo1Object = new @Marker @Normal(value = 5) Foo.Foo1();\n" +
		"		Class <@NonNull Foo.Foo1> c;\n" +
		"		c = @Readonly @NonNull Foo.Foo1.class;\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"@Marker class Foo {\n" +
		"  static @Marker @SingleMember(0) class Foo1 {\n" +
		"    Foo1() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  Foo() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    @Marker @Normal(value = 5) Foo.Foo1 foo1Object;\n" +
		"    foo1Object = new @Marker @Normal(value = 5) Foo.Foo1();\n" +
		"    Class<@NonNull Foo.Foo1> c;\n" +
		"    c = @Readonly @NonNull Foo.Foo1.class;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test001", expectedUnitToString );
}
}
