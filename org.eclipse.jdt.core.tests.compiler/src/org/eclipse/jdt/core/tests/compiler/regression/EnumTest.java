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
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class EnumTest extends AbstractComparisonTest {
	
	public EnumTest(String name) {
		super(name);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "test000" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 0 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 21, 50 };
//		TESTS_RANGE = new int[] { -1, 1 }; // run all tests with a number less or equals to 50
//		TESTS_RANGE = new int[] { 10, -1 }; // run all tests with a number greater or equals to 10
	}
	public static Test suite() {
		if (TESTS_PREFIX != null || TESTS_NAMES != null || TESTS_NUMBERS!=null || TESTS_RANGE!=null) {
			return new RegressionTestSetup(buildTestSuite(testClass()), highestComplianceLevels());
		}
		return setupSuite(testClass());
	}

	public static Class testClass() {  
		return EnumTest.class;
	}

	// test simple valid enum and its usage
	public void test000() {
		runConformTest(
			new String[] {
				"e/X.java",
				"package e;\n" + 
					"import e.T;\n" + 
					"import static e.T.*;\n" + 
					"\n" + 
					"public class X {\n" + 
					"    public static void main(String[] args) {\n" + 
					"    	System.out.print(\"JDTCore team:\");\n" + 
					"        for (T t : T.values()) {\n" + 
					"            t.setRole(t.isManager());\n" + 
					"            System.out.print(\" \"+ t + ':'+t.age()+':'+location(t)+':'+t.role);\n" + 
					"        }\n" + 
					"    }\n" + 
					"\n" + 
					"   private enum Location { SNZ, OTT }\n" + 
					"\n" + 
					"    private static Location location(T t) {\n" + 
					"        switch(t) {\n" + 
					"          case PHILIPPE:  \n" + 
					"          case DAVID:\n" + 
					"          case JEROME:\n" + 
					"          case FREDERIC:\n" + 
					"          	return Location.SNZ;\n" + 
					"          case OLIVIER:\n" + 
					"          case KENT:\n" + 
					"            return Location.OTT;\n" + 
					"          default:\n" + 
					"            throw new AssertionError(\"Unknown team member: \" + t);\n" + 
					"        }\n" + 
					"    }\n" + 
					"}\n",
				"e/T.java",
				"package e;\n" + 
					"public enum T {\n" + 
					"	PHILIPPE(37) {\n" + 
					"		public boolean isManager() {\n" + 
					"			return true;\n" + 
					"		}\n" + 
					"	},\n" + 
					"	DAVID(27),\n" + 
					"	JEROME(33),\n" + 
					"	OLIVIER(35),\n" + 
					"	KENT(40),\n" + 
					"	FREDERIC;\n" + 
					"\n" + 
					"   enum Role { M, D }\n" + 
					"\n" + 
					"   int age;\n" + 
					"	Role role;\n" + 
					"\n" + 
					"	T() {}\n" + 
					"	T(int age) {\n" + 
					"		this.age = age;\n" + 
					"	}\n" + 
					"	public int age() { return this.age; }\n" + 
					"	public boolean isManager() { return false; }\n" + 
					"	void setRole(boolean mgr) {\n" + 
					"		this.role = mgr ? Role.M : Role.D;\n" + 
					"	}\n" + 
					"}\n"
			},
			"JDTCore team: PHILIPPE:37:SNZ:M DAVID:27:SNZ:D JEROME:33:SNZ:D OLIVIER:35:OTT:D KENT:40:OTT:D FREDERIC:0:SNZ:D"
		);
	}
	// check assignment to enum constant is disallowed
	public void test001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	BLEU, \n" + 
				"	BLANC, \n" + 
				"	ROUGE;\n" + 
				"	{\n" + 
				"		BLEU = null;\n" + 
				"	}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	BLEU = null;\n" + 
			"	^^^^\n" + 
			"The final field X.BLEU cannot be assigned\n" + 
			"----------\n");
	}
	// check diagnosis for duplicate enum constants
	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU, \n" + 
				"	BLANC, \n" + 
				"	ROUGE,\n" + 
				"	BLEU;\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	BLEU, \n" + 
			"	^^^^\n" + 
			"Duplicate field X.BLEU\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	BLEU;\n" + 
			"	^^^^\n" + 
			"Duplicate field X.BLEU\n" + 
			"----------\n");
	}
	// check properly rejecting enum constant modifiers
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	public BLEU, \n" + 
				"	transient BLANC, \n" + 
				"	ROUGE\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	public BLEU, \n" + 
			"	       ^^^^\n" + 
			"Illegal modifier for the enum constant BLEU; no modifier is allowed\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	transient BLANC, \n" + 
			"	          ^^^^^\n" + 
			"Illegal modifier for the enum constant BLANC; no modifier is allowed\n" + 
			"----------\n");
	}
	// check using an enum constant
	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(BLEU);\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"BLEU");
	}
	// check method override diagnosis (with no enum constants)
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	;\n" + 
				"	protected Object clone() { return this; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	protected Object clone() { return this; }\n" + 
			"	                 ^^^^^^^\n" + 
			"Cannot override the final method from Enum<X>\n" + 
			"----------\n");
	}	
	// check generated #values() method
	public void test006() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for(X x: X.values()) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"BLEUBLANCROUGE");
	}	
	// tolerate user definition for $VALUES
	public void test007() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"   int $VALUES;\n" +
				"	public static void main(String[] args) {\n" + 
				"		for(X x: X.values()) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"BLEUBLANCROUGE");
	}	
	// reject user definition for #values()
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"   void dup() {} \n" +
				"   void values() {} \n" +
				"   void dup() {} \n" +
				"   void values() {} \n" +
				"   Missing dup() {} \n" +
				"	public static void main(String[] args) {\n" + 
				"		for(X x: X.values()) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	void dup() {} \n" + 
			"	     ^^^^^\n" + 
			"Duplicate method dup() in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	void values() {} \n" + 
			"	     ^^^^^^^^\n" + 
			"The enum X already defines the method values() implicitly\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	void dup() {} \n" + 
			"	     ^^^^^\n" + 
			"Duplicate method dup() in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	void values() {} \n" + 
			"	     ^^^^^^^^\n" + 
			"The enum X already defines the method values() implicitly\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	Missing dup() {} \n" + 
			"	^^^^^^^\n" + 
			"Missing cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 11)\n" + 
			"	Missing dup() {} \n" + 
			"	        ^^^^^\n" + 
			"Duplicate method dup() in type X\n" + 
			"----------\n");
	}		
	// switch on enum
	public void test009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"	//void values() {}\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		X x = BLEU;\n" + 
				"		switch(x) {\n" + 
				"			case BLEU :\n" + 
				"				System.out.println(\"SUCCESS\");\n" + 
				"				break;\n" + 
				"			case BLANC :\n" + 
				"			case ROUGE :\n" + 
				"				System.out.println(\"FAILED\");\n" + 
				"				break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}"
			},
			"SUCCESS");
	}		
	// duplicate switch case 
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"	//void values() {}\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		X x = BLEU;\n" + 
				"		switch(x) {\n" + 
				"			case BLEU :\n" + 
				"				break;\n" + 
				"			case BLEU :\n" + 
				"			case BLANC :\n" + 
				"			case ROUGE :\n" + 
				"				System.out.println(\"FAILED\");\n" + 
				"				break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\r\n" + 
			"	case BLEU :\r\n" + 
			"	^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\r\n" + 
			"	case BLEU :\r\n" + 
			"	^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n");
	}
	// reject user definition for #values()
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"   void values() {} \n" +
				"   void values() {} \n" +
				"	public static void main(String[] args) {\n" + 
				"		for(X x: X.values()) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	void values() {} \n" + 
			"	     ^^^^^^^^\n" + 
			"The enum X already defines the method values() implicitly\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	void values() {} \n" + 
			"	     ^^^^^^^^\n" + 
			"The enum X already defines the method values() implicitly\n" + 
			"----------\n");
	}	
	// check abstract method diagnosis
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X implements Runnable { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public enum X implements Runnable { \n" + 
			"	            ^\n" + 
			"Class must implement the inherited abstract method Runnable.run()\n" + 
			"----------\n");
	}
	// check enum constants with wrong arguments
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU(10),\n" + 
				"	BLANC(20),\n" + 
				"	ROUGE(30);\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	BLEU(10),\n" + 
			"	^^^^\n" + 
			"The constructor X(int) is undefined\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	BLANC(20),\n" + 
			"	^^^^^\n" + 
			"The constructor X(int) is undefined\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	ROUGE(30);\n" + 
			"	^^^^^\n" + 
			"The constructor X(int) is undefined\n" + 
			"----------\n");
	}
	// check enum constants with extra arguments
	public void test014() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU(10),\n" + 
				"	BLANC(20),\n" + 
				"	ROUGE(30);\n" + 
				"\n" + 
				"	int val;\n" + 
				"	X(int val) {\n" + 
				"		this.val = val;\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for(X x: values()) {\n" + 
				"			System.out.print(x.val);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"102030");
	}	
	// check enum constants with wrong arguments
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU(10),\n" + 
				"	BLANC(),\n" + 
				"	ROUGE(30);\n" + 
				"\n" + 
				"	int val;\n" + 
				"	X(int val) {\n" + 
				"		this.val = val;\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for(X x: values()) {\n" + 
				"			System.out.print(x.val);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\r\n" + 
			"	BLANC(),\r\n" + 
			"	^^^^^\n" + 
			"The constructor X() is undefined\n" + 
			"----------\n");
	}		
	// check enum constants with wrong arguments
	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	\n" + 
				"	BLEU(10) {\n" + 
				"		String foo() { // inner\n" + 
				"			return super.foo() + this.val;\n" + 
				"		}\n" + 
				"	},\n" + 
				"	BLANC(20),\n" + 
				"	ROUGE(30);\n" + 
				"\n" + 
				"	int val;\n" + 
				"	X(int val) {\n" + 
				"		this.val = val;\n" + 
				"	}\n" + 
				"	String foo() {  // outer\n" + 
				"		return this.name();\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for(X x: values()) {\n" + 
				"			System.out.print(x.foo());\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"BLEU10BLANCROUGE");
	}			
	// check enum constants with empty arguments
	public void test017() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	\n" + 
				"	BLEU()\n" + 
				"}\n"
			},
			"");
	}
	// cannot extend enums
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	BLEU()\n" + 
				"}\n" + 
				"\n" + 
				"class XX extends X implements X {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	class XX extends X implements X {\n" + 
			"	                 ^\n" + 
			"The type X cannot be the superclass of XX; a superclass must be a class\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	class XX extends X implements X {\n" + 
			"	                              ^\n" + 
			"The type X cannot be a superinterface of XX; a superinterface must be an interface\n" + 
			"----------\n");
	}		
	// 74851
	public void test019() {
		this.runConformTest(
			new String[] {
				"MonthEnum.java",
				"public enum MonthEnum {\n" + 
				"    JANUARY   (30),\n" + 
				"    FEBRUARY  (28),\n" + 
				"    MARCH     (31),\n" + 
				"    APRIL     (30),\n" + 
				"    MAY       (31),\n" + 
				"    JUNE      (30),\n" + 
				"    JULY      (31),\n" + 
				"    AUGUST    (31),\n" + 
				"    SEPTEMBER (31),\n" + 
				"    OCTOBER   (31),\n" + 
				"    NOVEMBER  (30),\n" + 
				"    DECEMBER  (31);\n" + 
				"    \n" + 
				"    private final int days;\n" + 
				"    \n" + 
				"    MonthEnum(int days) {\n" + 
				"        this.days = days;\n" + 
				"    }\n" + 
				"    \n" + 
				"    public int getDays() {\n" + 
				"    	boolean leapYear = true;\n" + 
				"    	switch(this) {\n" + 
				"    		case FEBRUARY: if(leapYear) return days+1;\n" + 
				"    	}\n" + 
				"    	return days;\n" + 
				"    }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"    	System.out.println(JANUARY.getDays());\n" + 
				"    }\n" + 
				"    \n" + 
				"}\n",
			},
			"30");
	}
	// 74226
	public void test020() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public class Foo{\n" + 
				"    public enum Rank {FIRST,SECOND,THIRD}\n" + 
				"    public void setRank(Rank rank){}\n" + 
				"}\n",
			},
			"");
	}	
	// 74226 variation - check nested enum is implicitly static
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public class Foo {\n" + 
				"    public static enum Rank {FIRST,SECOND,THIRD;\n" + 
				"            void bar() { foo(); } \n" + 
				"    }\n" + 
				"    public void setRank(Rank rank){}\n" + 
				"    void foo() {}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in Foo.java (at line 3)\n" + 
			"	void bar() { foo(); } \n" + 
			"	             ^^^\n" + 
			"Cannot make a static reference to the non-static method foo() from the type Foo\n" + 
			"----------\n");
	}		
	// 77151 - cannot use qualified name to denote enum constants in switch case label
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	\n" + 
				"	enum MX { BLEU, BLANC, ROUGE }\n" + 
				"	\n" + 
				"	void foo(MX e) {\n" + 
				"		switch(e) {\n" + 
				"			case MX.BLEU : break;\n" + 
				"			case MX.BLANC : break;\n" + 
				"			case MX.ROUGE : break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	case MX.BLEU : break;\n" + 
			"	     ^^^^^^^\n" + 
			"Cannot qualify the name of the enum constant BLEU in a case label\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	case MX.BLANC : break;\n" + 
			"	     ^^^^^^^^\n" + 
			"Cannot qualify the name of the enum constant BLANC in a case label\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	case MX.ROUGE : break;\n" + 
			"	     ^^^^^^^^\n" + 
			"Cannot qualify the name of the enum constant ROUGE in a case label\n" + 
			"----------\n");
	}
	
	// 77212 
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public enum RuleType{ SUCCESS, FAILURE }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(RuleType.valueOf(RuleType.SUCCESS.name()));\n" + 
				"	}\n" + 
				"}",
			},
			"SUCCESS");
	}
	// enum cannot be declared as local type
	
	// check abstract conditions
	
	// check one cannot redefine Enum incorrectly
	
	// check one cannot extend Enum explicitly
	
	// check one cannot allocate an enum type
	
	// check binary compatibility (removing referenced enum constants in switch)
	
	// check warning when switch doesn't use all enum constants
	
	// check enum syntax recovery
}
