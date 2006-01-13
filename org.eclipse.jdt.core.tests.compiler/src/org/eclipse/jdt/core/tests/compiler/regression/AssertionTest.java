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
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AssertionTest extends AbstractRegressionTest {

	/*
	 * Toggle compiler in mode -1.4
	 */
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
		return options;
	}

	public AssertionTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(testClass().getName());
		if (highestComplianceLevels().compareTo(COMPLIANCE_1_4) < 0) {
			System.err.println("Cannot run "+testClass().getName()+" at compliance "+highestComplianceLevels()+"!");
			return suite;
		}
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			suite.addTest(buildTestSuite(testClass(), COMPLIANCE_1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			suite.addTest(buildTestSuite(testClass(), COMPLIANCE_1_5));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			suite.addTest(buildTestSuite(testClass(), COMPLIANCE_1_6));
		}
		return suite;
	}
	
	public static Class testClass() {
		return AssertionTest.class;
	}

	public void test001() {
		this.runNegativeTest(
			new String[] {
				"assert.java",
				"public class assert {}\n",
			},
			"----------\n" + 
			"1. ERROR in assert.java (at line 1)\n" + 
			"	public class assert {}\n" + 
			"	             ^^^^^^\n" + 
			"Syntax error on token \"assert\", Identifier expected\n" + 
			"----------\n");
	}
	
	public void test002() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "    int i = 4;\n"
			+ "    assert i != 4;\n"
			+ "	   System.out.println(i);\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(\"SUCCESS\");	\n"
			+ "	  } \n"			
			+ "	} \n"
			+ "} \n" },
		"SUCCESS", //expected display 
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	
	public void test003() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "    int i = 4;\n"
			+ "    assert i != 4;\n"
			+ "	   System.out.println(i);\n"
			+ "	} \n"
			+ "} \n" },
		"4", 
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-da"});
	}
	public void test004() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : \"SUC\";	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		assert false : new Object(){ public String toString(){ return \"CESS\";}};	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"SUCCESS", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test005() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		int i = 2;	\n"
			+ "		assert false : i;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"12", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test006() {
		this.runNegativeTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "	  try {	\n"
			+ "		assert false : unbound;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"----------\n" + 
		"1. ERROR in A4.java (at line 4)\n" + 
		"	assert false : unbound;	\n" + 
		"	               ^^^^^^^\n" + 
		"unbound cannot be resolved\n" + 
		"----------\n");
	}
	public void test007() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1L;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "   try {	\n"
			+ "		assert false : 0L;	\n" // 0L isn't 0
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		long l = 2L;	\n"
			+ "		assert false : l;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"102", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test008() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1.0f;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		float f = 2.0f;	\n"
			+ "		assert false : f;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"1.02.0", //expected display
		null, // use default class-path
		true, // do not flush previous output dir content
		new String[] {"-ea"});
	}
	public void test009() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1.0;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		double d = 2.0;	\n"
			+ "		assert false : d;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"1.02.0", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	// http://dev.eclipse.org/bugs/show_bug.cgi?id=22334
	public void test010() {
		this.runConformTest(new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) { \n" +
			"		I.Inner inner = new I.Inner(); \n" +
			"		try { \n" +
			"			inner.test(); \n" +
			"			System.out.println(\"FAILED\"); \n" +
			"		} catch(AssertionError e){ \n" +
			"			System.out.println(\"SUCCESS\"); \n" +
			"		} \n" +
			"	} \n" +
			"} \n" +
			"interface I { \n" +
			"  public static class Inner { \n" +
			"    public void test() { \n" +
			"      assert false; \n" +
			"    } \n" +
			"  } \n" +
			"} \n" },
		"SUCCESS",
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	} 	
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28750
	 */
	public void test011() {
		this.runConformTest(
			new String[] {
				"AssertTest.java",
				"public class AssertTest {\n" +
				"   public AssertTest() {}\n" +
				"   public class InnerClass {\n" +
				"      InnerClass() {\n" +
				"        assert(false);\n" +
				"      }\n" +
				"   }\n" +
				"   \n" +
				"   public static void main(String[] args) {	\n" +
				"        System.out.print(\"SUCCESS\");	\n" +
				"	}	\n" +
				"}"
			},
			"SUCCESS"); // expected output
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=57743
	 */
	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public static void main( String[] args ) {\n" + 
				"        try {\n" + 
				"            throw new Throwable( \"This is a test\");\n" + 
				"        }\n" + 
				"        catch( Throwable ioe ) {\n" + 
				"            assert false : ioe;\n" + 
				"        }\n" + 
				"        System.out.print(\"SUCCESS\");	\n" +
				"    }\n" + 
				"}\n"
			},
			"SUCCESS"); // expected output
	}

}
