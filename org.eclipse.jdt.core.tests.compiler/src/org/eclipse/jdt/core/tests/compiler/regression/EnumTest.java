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

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class EnumTest extends AbstractComparisonTest {
	
	String reportMissingJavadocComments = null;

	public EnumTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 0 };
//		TESTS_RANGE = new int[] { 21, 50 };
//	}
	public static Test suite() {
		return buildTestSuite(testClass());
	}

	public static Class testClass() {  
		return EnumTest.class;
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		if (reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportMissingJavadocComments);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		reportMissingJavadocComments = null;
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
					"    	T oldest = null;\n" +
					"    	int maxAge = Integer.MIN_VALUE;\n" +
					"    	for (T t : T.values()) {\n" + 
					"            if (t == YODA) continue;// skip YODA\n" +
					"            t.setRole(t.isManager());\n" + 
					"			 if (t.age() > maxAge) {\n" +
					"               oldest = t;\n" +
					"               maxAge = t.age();\n" +
					"            }\n" +
					"            System.out.print(\" \"+ t + ':'+t.age()+':'+location(t)+':'+t.role);\n" + 
					"        }\n" + 
					"        System.out.println(\" WINNER is:\" + T.valueOf(oldest.name()));\n" +
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
					"	YODA(41),\n" +
					"	FREDERIC;\n" + 
					"\n" + 
					"   enum Role { M, D }\n" + 
					"\n" + 
					"   int age;\n" + 
					"	Role role;\n" + 
					"\n" + 
					"	T() { this(YODA.age()); }\n" + 
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
			"JDTCore team: PHILIPPE:37:SNZ:M DAVID:27:SNZ:D JEROME:33:SNZ:D OLIVIER:35:OTT:D KENT:40:OTT:D FREDERIC:41:SNZ:D WINNER is:FREDERIC"
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
	
	// 77244 - cannot declare final enum
	public void test024() {
		this.runNegativeTest(
			new String[] {
				"X.java",	
				"public final enum X {\n" +
				"	FOO() {}\n" +
				"}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public final enum X {\n" + 
			"	                  ^\n" + 
			"Illegal modifier for the enum X; only public & abstract are permitted\n" + 
			"----------\n");
	}	
	
	// values is using arraycopy instead of clone 
	public void test025() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	SUC, CESS;\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for (X x : values()) {\n" + 
				"			System.out.print(x.name());\n" + 
				"		}\n" + 
				"	}\n" + 
				"}",
			},
			"SUCCESS");
	}
	
	// check enum name visibility
	public void test026() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { BLEU, BLANC, ROUGE }\n" + 
				"}\n" + 
				"\n" + 
				"class Y {\n" + 
				"	void foo(Couleur c) {\n" + 
				"		switch (c) {\n" + 
				"			case BLEU :\n" + 
				"				break;\n" + 
				"			case BLANC :\n" + 
				"				break;\n" + 
				"			case ROUGE :\n" + 
				"				break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	void foo(Couleur c) {\n" + 
			"	         ^^^^^^^\n" + 
			"Couleur cannot be resolved to a type\n" + 
			"----------\n");
	}	
	// check enum name visibility
	public void _test027() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { BLEU, BLANC, ROUGE }\n" + 
				"	class Y {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n",
			},
			"");
	}		
	// check enum name visibility
	public void _test028() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { \n" + 
				"		BLEU, BLANC, ROUGE;\n" + 
				"		static int C = 0;\n" + 
				"		static void FOO() {}\n" + 
				"	}\n" + 
				"	class Y {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"			FOO();\n" + 
				"			C++;\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 17)\n" + 
			"	FOO();\n" + 
			"	^^^\n" + 
			"The method FOO() is undefined for the type X.Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 18)\n" + 
			"	C++;\n" + 
			"	^\n" + 
			"C cannot be resolved\n" + 
			"----------\n");
	}		
	// check enum name visibility
	public void _test029() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { \n" + 
				"		BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type\n" + 
				"	}\n" + 
				"	class Y {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class BLEU {}\n",
			},
			"");
	}		
	// check enum name visibility
	public void _test030() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { \n" + 
				"		BLEU, BLANC, ROUGE; // take precedence over sibling constant from Color\n" + 
				"	}\n" + 
				"	enum Color { \n" + 
				"		BLEU, BLANC, ROUGE;\n" + 
				"	}\n" + 
				"	class Y {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class BLEU {}\n",
			},
			"");
	}		
	// check enum name visibility
	public void _test031() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { \n" + 
				"		BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type\n" + 
				"	}\n" + 
				"	class Y implements IX, JX {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"interface IX {\n" + 
				"	int BLEU = 1;\n" + 
				"}\n" + 
				"interface JX {\n" + 
				"	int BLEU = 2;\n" + 
				"}\n" + 
				"class BLEU {}\n" + 
				"\n",
			},
			"");
	}	
	
	// check Enum cannot be used as supertype (explicitly)
	public void test032() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Enum {\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X extends Enum {\n" + 
			"	                       ^^^^\n" + 
			"The type X may not subclass Enum explicitly\n" + 
			"----------\n");
	}		

	// Javadoc in enum (see bug 78018)
	public void test033() {
		this.runConformTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * Valid javadoc\n" +
					"	 * @author ffr\n" +
					"	 */\n" +
					"public enum E {\n" +
					"	/** Valid javadoc */\n" +
					"	TEST,\n" +
					"	/** Valid javadoc */\n" +
					"	VALID;\n" +
					"	/** Valid javadoc */\n" +
					"	public void foo() {}\n" +
					"}\n"
			}
		);
	}
	public void test034() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * Invalid javadoc\n" +
					"	 * @exception NullPointerException Invalid tag\n" +
					"	 * @throws NullPointerException Invalid tag\n" +
					"	 * @return Invalid tag\n" +
					"	 * @param x Invalid tag\n" +
					"	 */\n" +
					"public enum E { TEST, VALID }\n"
			},
			"----------\n" +
				"1. ERROR in E.java (at line 3)\n" +
				"	* @exception NullPointerException Invalid tag\n" +
				"	   ^^^^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"2. ERROR in E.java (at line 4)\n" +
				"	* @throws NullPointerException Invalid tag\n" +
				"	   ^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"3. ERROR in E.java (at line 5)\n" +
				"	* @return Invalid tag\n" +
				"	   ^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"4. ERROR in E.java (at line 6)\n" +
				"	* @param x Invalid tag\n" +
				"	   ^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n"
		);
	}
	public void test035() {
		this.runConformTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * @see \"Valid normal string\"\n" +
					"	 * @see <a href=\"http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/javadoc.html\">Valid URL link reference</a>\n" +
					"	 * @see Object\n" +
					"	 * @see #TEST\n" +
					"	 * @see E\n" +
					"	 * @see E#TEST\n" +
					"	 */\n" +
					"public enum E { TEST, VALID }\n"
			}
		);
	}
	public void test036() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * @see \"invalid\" no text allowed after the string\n" +
					"	 * @see <a href=\"invalid\">invalid</a> no text allowed after the href\n" +
					"	 * @see\n" +
					"	 * @see #VALIDE\n" +
					"	 */\n" +
					"public enum E { TEST, VALID }\n"
			},
			"----------\n" +
				"1. ERROR in E.java (at line 2)\n" + 
				"	* @see \"invalid\" no text allowed after the string\n" + 
				"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n" + 
				"2. ERROR in E.java (at line 3)\n" + 
				"	* @see <a href=\"invalid\">invalid</a> no text allowed after the href\n" + 
				"	                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n" + 
				"3. ERROR in E.java (at line 4)\n" + 
				"	* @see\n" + 
				"	   ^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n" + 
				"4. ERROR in E.java (at line 5)\n" + 
				"	* @see #VALIDE\n" + 
				"	        ^^^^^^\n" + 
				"Javadoc: VALIDE cannot be resolved or is not a field\n" + 
				"----------\n"
		);
	}
	public void test037() {
		this.runConformTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * Value test: {@value #TEST}\n" +
					"	 * or: {@value E#TEST}\n" +
					"	 */\n" +
					"public enum E { TEST, VALID }\n"
			}
		);
	}
	public void test038() {
		reportMissingJavadocComments = CompilerOptions.ERROR;
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public enum E { TEST, VALID;\n" +
				"	public void foo() {}\n" +
				"}"
			},
			"----------\n" + 
				"1. ERROR in E.java (at line 1)\n" + 
				"	public enum E { TEST, VALID;\n" + 
				"	            ^\n" + 
				"Javadoc: Missing comment for public declaration\n" + 
				"----------\n" + 
				"2. ERROR in E.java (at line 1)\n" + 
				"	public enum E { TEST, VALID;\n" + 
				"	                ^^^^\n" + 
				"Javadoc: Missing comment for public declaration\n" + 
				"----------\n" + 
				"3. ERROR in E.java (at line 1)\n" + 
				"	public enum E { TEST, VALID;\n" + 
				"	                      ^^^^^\n" + 
				"Javadoc: Missing comment for public declaration\n" + 
				"----------\n" + 
				"4. ERROR in E.java (at line 2)\n" + 
				"	public void foo() {}\n" + 
				"	            ^^^^^\n" + 
				"Javadoc: Missing comment for public declaration\n" + 
				"----------\n"
		);
	}
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public enum E {\n" +
					"	/**\n" +
					"	 * @exception NullPointerException Invalid tag\n" +
					"	 * @throws NullPointerException Invalid tag\n" +
					"	 * @return Invalid tag\n" +
					"	 * @param x Invalid tag\n" +
					"	 */\n" +
					"	TEST,\n" +
					"	VALID;\n" +
					"}\n"
			},
			"----------\n" +
				"1. ERROR in E.java (at line 3)\n" +
				"	* @exception NullPointerException Invalid tag\n" +
				"	   ^^^^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"2. ERROR in E.java (at line 4)\n" +
				"	* @throws NullPointerException Invalid tag\n" +
				"	   ^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"3. ERROR in E.java (at line 5)\n" +
				"	* @return Invalid tag\n" +
				"	   ^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"4. ERROR in E.java (at line 6)\n" +
				"	* @param x Invalid tag\n" +
				"	   ^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n"
		);
	}
	public void test040() {
		this.runConformTest(
			new String[] {
				"E.java",
				"public enum E {\n" +
					"	/**\n" +
					"	 * @see E\n" +
					"	 * @see #VALID\n" +
					"	 */\n" +
					"	TEST,\n" +
					"	/**\n" +
					"	 * @see E#TEST\n" +
					"	 * @see E\n" +
					"	 */\n" +
					"	VALID;\n" +
					"	/**\n" +
					"	 * @param x the object\n" +
					"	 * @return String\n" +
					"	 * @see Object\n" +
					"	 */\n" +
					"	public String val(Object x) { return x.toString(); }\n" +
					"}\n"
			}
		);
	}
	public void test041() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public enum E {\n" +
					"	/**\n" +
					"	 * @see e\n" +
					"	 * @see #VALIDE\n" +
					"	 */\n" +
					"	TEST,\n" +
					"	/**\n" +
					"	 * @see E#test\n" +
					"	 * @see EUX\n" +
					"	 */\n" +
					"	VALID;\n" +
					"	/**\n" +
					"	 * @param obj the object\n" +
					"	 * @return\n" +
					"	 * @see Objet\n" +
					"	 */\n" +
					"	public String val(Object x) { return x.toString(); }\n" +
					"}\n"
			},
			"----------\n" +
				"1. ERROR in E.java (at line 3)\n" + 
				"	* @see e\n" + 
				"	       ^\n" + 
				"Javadoc: e cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in E.java (at line 4)\n" + 
				"	* @see #VALIDE\n" + 
				"	        ^^^^^^\n" + 
				"Javadoc: VALIDE cannot be resolved or is not a field\n" + 
				"----------\n" + 
				"3. ERROR in E.java (at line 8)\n" + 
				"	* @see E#test\n" + 
				"	         ^^^^\n" + 
				"Javadoc: test cannot be resolved or is not a field\n" + 
				"----------\n" + 
				"4. ERROR in E.java (at line 9)\n" + 
				"	* @see EUX\n" + 
				"	       ^^^\n" + 
				"Javadoc: EUX cannot be resolved to a type\n" + 
				"----------\n" + 
				"5. ERROR in E.java (at line 13)\n" + 
				"	* @param obj the object\n" + 
				"	         ^^^\n" + 
				"Javadoc: Parameter obj is not declared\n" + 
				"----------\n" + 
				"6. ERROR in E.java (at line 14)\n" + 
				"	* @return\n" + 
				"	   ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
				"----------\n" + 
				"7. ERROR in E.java (at line 15)\n" + 
				"	* @see Objet\n" + 
				"	       ^^^^^\n" + 
				"Javadoc: Objet cannot be resolved to a type\n" + 
				"----------\n" + 
				"8. ERROR in E.java (at line 17)\n" + 
				"	public String val(Object x) { return x.toString(); }\n" + 
				"	                         ^\n" + 
				"Javadoc: Missing tag for parameter x\n" + 
				"----------\n"
		);
	}
	public void test042() {
		this.runConformTest(
			new String[] {
				"E.java",
				"public enum E {\n" +
					"	/**\n" +
					"	 * Test value: {@value #TEST}\n" +
					"	 */\n" +
					"	TEST,\n" +
					"	/**\n" +
					"	 * Valid value: {@value E#VALID}\n" +
					"	 */\n" +
					"	VALID;\n" +
					"	/**\n" +
					"	 * Test value: {@value #TEST}\n" +
					"	 * Valid value: {@value E#VALID}\n" +
					"	 * @param x the object\n" +
					"	 * @return String\n" +
					"	 */\n" +
					"	public String val(Object x) { return x.toString(); }\n" +
					"}\n"
			}
		);
	}
	
	// External javadoc references to enum
	public void test043() {
		this.runConformTest(
			new String[] {
				"test/E.java",
				"package test;\n" +
					"public enum E { TEST, VALID }\n",
				"test/X.java",
				"import static test.E.TEST;\n" +
					"	/**\n" +
					"	 * @see test.E\n" +
					"	 * @see test.E#VALID\n" +
					"	 * @see #TEST\n" +
					"	 */\n" +
					"public class X {}\n"
			}
		);
	}
	public void test044() {
		this.runConformTest(
			new String[] {
				"test/E.java",
				"package test;\n" +
					"public enum E { TEST, VALID }\n",
				"test/X.java",
				"import static test.E.TEST;\n" +
					"	/**\n" +
					"	 * Valid value = {@value test.E#VALID}\n" +
					"	 * Test value = {@value #TEST}\n" +
					"	 */\n" +
					"public class X {}\n"
			}
		);
	}
	
	public void test045() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X\n" + 
				"{\n" + 
				"  FIRST,\n" + 
				"  SECOND,\n" + 
				"  THIRD;\n" + 
				"\n" + 
				"  static {\n" + 
				"    for (X t : values()) {\n" + 
				"      System.out.print(t.name());\n" + 
				"    }\n" + 
				"  }\n" + 
				"\n" + 
				"  X() {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"  }\n" + 
				"}"
			},
			"FIRSTSECONDTHIRD"
		);
	}
	// enum cannot be declared as local type
	
	// check abstract conditions
	
	// check one cannot redefine Enum incorrectly
	
	// check binary compatibility (removing referenced enum constants in switch)
	
	// check warning when switch doesn't use all enum constants
	
	// check enum syntax recovery
}
