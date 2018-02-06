/*******************************************************************************
 * Copyright (c) 2018 Jesper Steen Møller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.junit.Assert;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JEP286Test extends AbstractRegressionTest {

public static Class testClass() {
	return JEP286Test.class;
}
public void initialize(CompilerTestSetup setUp) {
	super.initialize(setUp);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_10);
}

public JEP286Test(String testName){
	super(testName);
}
static {
//	TESTS_NAMES = new String[] { "test0018_project_variable_types" };
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_10);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_10);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_10);
	return options;
}
private static final Map<String, String> simpleTypeNames = new HashMap<>();
static {
	simpleTypeNames.put("String", "java.lang.String");
	simpleTypeNames.put("Object", "java.lang.Object");
	simpleTypeNames.put("Bar", "X.Bar");
}

static void assertInferredType(LocalDeclaration varDecl) {
	String varName = new String(varDecl.name);
	int underscoreIndex = varName.indexOf('_');
	Assert.assertNotEquals(-1, underscoreIndex);
	String typeNamePart = varName.substring(underscoreIndex+1);
	typeNamePart = typeNamePart.replaceAll("ARRAY", "[]"); // So that we assume that x_intARRAY is of type int[]
	System.out.println("For " + varName + ", the type was be: " +  varDecl.binding.type.debugName());
	Assert.assertEquals(simpleTypeNames.getOrDefault(typeNamePart, typeNamePart), varDecl.binding.type.debugName());
}

// This visitor visits the 'testXxx' method in the visited classes, checking for expected types of local variables (using the debug name)
private final static class InferredTypeVerifier extends ASTVisitor {
		public int localsChecked = 0;
		public InferredTypeVerifier() { }

		@Override
		public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
			return false; // Don't check Foo itself
		}

		@Override
		public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
			if (! new String(methodDeclaration.selector).startsWith("test")) return false;
			return super.visit(methodDeclaration, scope);
		}

		@Override
		public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
			assertInferredType(localDeclaration);
			this.localsChecked++;
			return super.visit(localDeclaration, scope);
		}
	}

public void test0001_local_variable_inference() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        var x = \"SUCCESS\";\n" +
				"        System.out.println(x);\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS");
}
public void test0002_inferred_for() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"		int sum = 0;\n" +	
				"		for(var n = 1; n <= 2; ++n) {\n" +
				"			sum += n;\n" +	
				"       }\n" +
				"		System.out.println(\"SUCCESS \" + sum);\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS 3");
}
public void test0003_inferred_enhanced_for() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"		int sum = 0;\n" +	
				"		for(var n : java.util.List.of(1, 2)) {\n" +
				"			sum += n;\n" +	
				"       }\n" +
				"		System.out.println(\"SUCCESS \" + sum);\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS 3");
}
public void test0004_try_with_resources() throws IOException {
	try(java.io.Writer w = new java.io.StringWriter()) {
		w.write("SUCCESS!\n");
		System.out.println(w.toString());
	}
	
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) throws Exception {\n" +
				"		try(var w = new java.io.StringWriter()) {\n" +	
				"			w.write(\"SUCCESS\\n\");" +
				"			System.out.println(w.toString());\n" +
				"       }\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS");
}
public void test0005_no_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] argv) {\n" + 
				"		var a;\n" + 
				"		for(var b;;);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a;\n" +
			"	    ^\n" +
			"Cannot use 'var' on variable without initializer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	for(var b;;);\n" +
			"	        ^\n" +
			"Cannot use 'var' on variable without initializer\n" +
			"----------\n");
}
public void test0006_multiple_declarators() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] argv) {\n" + 
				"		var a = 1, b = 2;\n" + 
				"		for(var c = 1, d = 20; c<d; c++);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = 1, b = 2;\n" +
			"	           ^\n" +
			"'var' is not allowed in a compound declaration\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	for(var c = 1, d = 20; c<d; c++);\n" + 
			"	               ^\n" +
			"'var' is not allowed in a compound declaration\n" +
			"----------\n");
}
public void test0007_var_in_wrong_place() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	private var someField = 0;\n" +
				"	public var method() {\n" + 
				"		return null;\n" +
				"	}\n" + 
				"	public void main(var arg) {\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	private var someField = 0;\n" +
			"	        ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	public var method() {\n" + 
			"	       ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	public void main(var arg) {\n" + 
			"	                 ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n");
}
public void test0008_null_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String[] arg) {\n" +
				"		var notMuch = null;\n" +	
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var notMuch = null;\n" +
			"	    ^^^^^^^\n" +
			"Cannot infer type for local variable initialized to 'null'\n" +
			"----------\n");
}
public void test0008_void_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void foo() {\n" +
				"	}\n" +
				"\n" +
				"	public void baz() {\n" +
				"		var nothingHere = foo();\n" +	
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" + 
			"	var nothingHere = foo();\n" + 
			"	    ^^^^^^^^^^^\n" + 
			"Variable initializer is 'void' -- cannot infer variable type\n" + 
			"----------\n");
}
public void test0009_var_as_type_name() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public enum var { V, A, R };\n" +	
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public enum var { V, A, R };\n" +
			"	            ^^^\n" +
			"'var' is not a valid type name\n" +
			"----------\n");
}
public void test0010_array_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var myArray = { 1, 2, 3 };\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var myArray = { 1, 2, 3 };\n" +
			"	    ^^^^^^^\n" +
			"Array initializer needs an explicit target-type\n" +
			"----------\n");
}
public void test0011_array_type() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var myArray[] = new int[42];\n" +
				"		var[] moreArray = new int[1337];\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var myArray[] = new int[42];\n" +
			"	    ^^^^^^^\n" +
			"'var' is not allowed as an element type of an array\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	var[] moreArray = new int[1337];\n" +
			"	      ^^^^^^^^^\n" +
			"'var' is not allowed as an element type of an array\n" +
			"----------\n");
}
public void test0012_self_reference() throws IOException {
	
	// BTW: This will give a VerifyError: int a = ((java.util.concurrent.Callable<Integer>)(() -> true ? 1 : a)).call();
	// The cases are tested. a is a simple case, with plain usage in the same scope. b is used in a nested scope.
	// c and d are shadowed by the nested definitions.
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var a = 42 + a;\n" +
				"		var b = ((java.util.concurrent.Callable<Integer>)(() -> true ? 1 : b)).call();\n" +
				"       var c = new java.util.concurrent.Callable<Integer>() {\n" + 
				"           public Integer call() {\n" + 
				"               int c = 42; return c;\n" + 
				"           }\n" + 
				"       }.call();" +
				"       var d = new java.util.concurrent.Callable<Integer>() {\n" +
				"           int d = 42;\n" +
				"           public Integer call() {\n" + 
				"               return d;\n" + 
				"           }\n" + 
				"       }.call();" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = 42 + a;\n" +
			"	    ^\n" +
			"Declaration using 'var' may not contain references to itself\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	var b = ((java.util.concurrent.Callable<Integer>)(() -> true ? 1 : b)).call();\n" +
			"	    ^\n" +
			"Declaration using 'var' may not contain references to itself\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 7)\n" +
			"	int c = 42; return c;\n" +
			"	    ^\n" +
		    "The local variable c is hiding another local variable defined in an enclosing scope\n" +
		    	"----------\n"+
			"3. WARNING in X.java (at line 10)\n" +
			"	int d = 42;\n" +
			"	    ^\n" +
		    "The field new Callable<Integer>(){}.d is hiding another local variable defined in an enclosing scope\n" +
		    	"----------\n");
}
public void test0013_lambda() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var a = (int i) -> 42;\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = (int i) -> 42;\n" +
			"	    ^\n" +
			"Lambda expression needs an explicit target-type\n" +
			"----------\n");
}
public void test0014_method_reference() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var a = X::main;\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = X::main;\n" +
			"	    ^\n" +
			"Method reference needs an explicit target-type\n" +
			"----------\n");
}
public void test0015_complain_over_first_poly_encountered() throws Exception {
	
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var a = args.length > 1 ? X::main : (int i) -> 42;\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = args.length > 1 ? X::main : (int i) -> 42;\n" +
			"	    ^\n" +
			"Method reference needs an explicit target-type\n" +
			"----------\n");
}
public void test0016_dont_capture_deep_poly_expressions() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String [] args) throws Exception {\n" +
				"		var z = ((java.util.concurrent.Callable<String>)(() -> \"SUCCESS\"));\n" + 		
				"		var x = args.length > 1 ? \"FAILURE\" : z.call();\n" +
				"		System.out.println(x);\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
}
//static <T extends List<? super E>, E extends List<? super Integer>> void doSomething(T[] e) {
//	e[0] = null;
//}

public void test0017_simple_variable_types() throws Exception {
	InferredTypeVerifier typeVerifier = new InferredTypeVerifier();
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" + 
				"\n" + 
				"public class X {\n" + 
				"    void test() {\n" + 
				"        var i_String = \"\";\n" + 
				"        for (var i2_String = \"\" ; ; ) { break; }\n" + 
				"        for (var i2_String : iterableOfString()) { break; }\n" + 
				"        for (var i2_String : arrayOfString()) { break; }\n" + 
				"        try (var i2_Bar = new Bar()) { } finally { }\n" + 
				"        try (var i2_Bar = new Bar(); var i3_Bar = new Bar()) { } finally { }\n" + 
				"    }\n" + 
				"\n" + 
				"    Iterable<String> iterableOfString() { return null; }\n" + 
				"    String[] arrayOfString() { return null; }\n" + 
				"\n" + 
				"    static class Bar implements AutoCloseable {\n" + 
				"        @Override\n" + 
				"        public void close() { }\n" + 
				"    }\n" + 
				"}\n" + 
				"\n"
			},
			typeVerifier);
	Assert.assertEquals(7, typeVerifier.localsChecked);
}
public void test0018_primitive_variable_types() throws Exception {
	InferredTypeVerifier typeVerifier = new InferredTypeVerifier();
	this.runConformTest(
			new String[] {
				"Y.java",
				"class Y {\n" + 
				"    boolean[] booleanArray = { true };\n" + 
				"    byte[] byteArray = { 1 };\n" + 
				"    char[] charArray = { 'c' };\n" + 
				"    short[] shortArray = { 42 };\n" + 
				"    int[] intArray = { 42 };\n" + 
				"    long[] longArray = { 42L };\n" + 
				"    float[] floatArray = { 0.1f };\n" + 
				"    double[] doubleArray = { 0.2d };\n" + 
				"\n" + 
				"    void testBuiltins() {\n" + 
				"        var z_boolean = false;\n" + 
				"        var b_byte = (byte)0xff;\n" + 
				"        var c_char = 'c';\n" + 
				"        var s_short = (short)42;\n" + 
				"        var i_int = 42;\n" + 
				"        var l_long = 42L;\n" + 
				"        var f_float = 0.25f;\n" + 
				"        var d_double = 0.35d;\n" + 
				"    }\n" + 
				"\n" + 
				"    void testBuiltinsForEach() {\n" + 
				"        for (var z_boolean : booleanArray) { System.out.print(\".\"); }\n" + 
				"        for (var b_byte : byteArray) { System.out.print(\".\"); }\n" + 
				"        for (var c_char : charArray) { System.out.print(\".\"); }\n" + 
				"        for (var s_short : shortArray) { System.out.print(\".\"); }\n" + 
				"        for (var i_int : intArray) { System.out.print(\".\"); }\n" + 
				"        for (var l_long : longArray) { System.out.print(\".\"); }\n" + 
				"        for (var f_float : floatArray) { System.out.print(\".\"); }\n" + 
				"        for (var d_double : doubleArray) { System.out.print(\".\"); }\n" + 
				"    }\n" + 
				"    void testBuiltinsArray() {\n" + 
				"        var z_booleanARRAY = booleanArray;\n" + 
				"        var b_byteARRAY = byteArray;\n" + 
				"        var c_charARRAY = charArray;\n" + 
				"        var s_shortARRAY = shortArray;\n" + 
				"        var i_intARRAY = intArray;\n" + 
				"        var l_longARRAY = longArray;\n" + 
				"        var f_floatARRAY = floatArray;\n" + 
				"        var d_doubleARRAY = doubleArray;\n" + 
				"    }\n" + 
				"\n" + 
				"}\n"
			},
			typeVerifier);
	Assert.assertEquals(24, typeVerifier.localsChecked);
}
}
