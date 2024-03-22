/*******************************************************************************
 * Copyright (c) 2003, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.problem.ShouldNotImplement;

@SuppressWarnings({ "rawtypes" })
public class ConstantTest extends AbstractRegressionTest {

public ConstantTest(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_PREFIX = "testBug95521";
//	TESTS_NAMES = new String[] { "testBug566332_01" };
//	TESTS_NUMBERS = new int[] { 21 };
//	TESTS_RANGE = new int[] { 23, -1 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class  X {\s
			public static void main (String args []) {
			  foo();\s
			}
			public static void foo() {
			  if(55f!=00000000000000000000055F)      // HERE VA/Java detects an unexpected error
			  {
			System.out.println("55f!=00000000000000000000055F");
			  }
			  else
			  {
			System.out.println("55f==00000000000000000000055F");
			  }
			 }     \s
			}
			""",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  public static void main (String args []) {
			    foo();
			  }
			  public static void foo() {
			    if(55f!=00000000000000000000055F)      // HERE VA/Java detects an unexpected error
			      {
			      System.out.println("55f!=00000000000000000000055F");
			    }
			    else
			    {
			      System.out.println("55f==00000000000000000000055F");
			    }
			  }     \s
			}
			""",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/Z.java",
		"""
			package p;
			public class Z {
			  public static void main(String[] cargs) throws Exception {
			    System.out.println(System.getProperty("java.vm.info", "J9"));
			    System.out.write((byte) 0x89);
			    System.out.println();
			    System.out.println("\u00E2?\u00B0");
			    System.out.println(Integer.toHexString("\u00E2?\u00B0".charAt(0)));
			  }
			}
			""",
	});
}

public void test004() {
	this.runConformTest(
		new String[] {
			"TempClassFormat.java",
			"""
				/**
				 * Insert the type's description here.
				 * Creation date: (02/28/01 2:58:07 PM)
				 * @author: Administrator
				 */
				public class TempClassFormat {
						// ERROR NUMBERS
				
					// Blank field error numbers
					private static final String PERSON_ID_BLANK = "2";
					private static final String DEMOGRAPHIC_TYPE_BLANK = "3";
					private static final String EMPLOYEE_NUMBER_BLANK = "23";
					private static final String WORK_PHONE_AREA_CODE_BLANK = "25";
					private static final String WORK_PHONE1_BLANK = "26";
					private static final String WORK_PHONE2_BLANK = "27";
					private static final String WORK_ADDRESS1_BLANK = "28";
					private static final String WORK_CITY_BLANK = "29";
					private static final String WORK_STATE_BLANK = "30";
					private static final String WORK_ZIP5_BLANK = "31";
					private static final String BENEFITS_SALARY_BLANK = "32";
					private static final String TRUE_SALARY_BLANK = "33";
					private static final String PAY_FREQUENCY_BLANK = "34";
					private static final String WORK_HOURS_BLANK = "35";
					private static final String LOCATION_ID_BLANK = "36";
					private static final String SALARY_GRADE_BLANK = "37";
					private static final String DATE_OF_HIRE_BLANK = "38";
					private static final String RETIRE_VEST_PERCENT_BLANK = "39";
					private static final String JOB_CODE_BLANK = "40";
					private static final String UNION_FLAG_BLANK = "41";
					private static final String OFFICER_FLAG_BLANK = "42";
					private static final String PIN_USER_ID_BLANK = "43";
					private static final String VENDOR_EMPLOYEE_ID_BLANK = "44";
					private static final String MODIFIED_BY_BLANK = "8";
					private static final String MODIFIED_DATE_BLANK = "9";
				\t
				\t
					// Invalid field error numbers
					private static final String DEMOGRAPHIC_TYPE_INVALID = "54";
					private static final String EMPLOYER_ID_INVALID = "22";
					private static final String WORK_STATE_INVALID = "70";
					private static final String PAY_FREQUENCY_INVALID = "138";
					private static final String WORK_HOURS_TOO_SMALL = "140";
					private static final String DATE_OF_HIRE_INVALID = "75";
					private static final String DATE_OF_HIRE_AFTER_TODAY = "137";
					private static final String RETIRE_VEST_PERCENT_TOO_LARGE = "77";
					private static final String RETIRE_VEST_PERCENT_TOO_SMALL = "139";
					private static final String UNION_FLAG_INVALID = "78";
					private static final String OFFICER_FLAG_INVALID = "79";
					private static final String BENEFIT_GROUP_ID_INVALID = "45";
					private static final String LAST_PERSON_SEQ_NUMBER_INVALID = "80";
				
					// Field not numeric error numbers
					private static final String WORK_PHONE_AREA_CODE_NOT_NUMERIC = "67";
					private static final String WORK_PHONE1_NOT_NUMERIC = "68";
					private static final String WORK_PHONE2_NOT_NUMERIC = "69";
					private static final String WORK_PHONE_EXTENSION_NOT_NUMERIC = "109";
					private static final String WORK_ZIP5_NOT_NUMERIC = "71";
					private static final String WORK_ZIP4_NOT_NUMERIC = "46";
					private static final String BENEFITS_SALARY_NOT_NUMERIC = "72";
					private static final String TRUE_SALARY_NOT_NUMERIC = "73";
					private static final String WORK_HOURS_NOT_NUMERIC = "74";
					private static final String RETIRE_VEST_PERCENT_NOT_NUMERIC = "76";
				\t
					// Field too short error numbers
					private static final String WORK_PHONE_AREA_CODE_TOO_SHORT = "110";
					private static final String WORK_PHONE1_TOO_SHORT = "111";
					private static final String WORK_PHONE2_TOO_SHORT = "112";
					private static final String WORK_STATE_TOO_SHORT = "113";
					private static final String WORK_ZIP5_TOO_SHORT = "114";
					private static final String WORK_ZIP4_TOO_SHORT = "115";
				
					// Field too long error numbers
					private static final String PERSON_ID_TOO_LONG = "82";
					private static final String EMPLOYEE_NUMBER_TOO_LONG = "116";
					private static final String WORK_PHONE_AREA_CODE_TOO_LONG = "117";
					private static final String WORK_PHONE1_TOO_LONG = "118";
					private static final String WORK_PHONE2_TOO_LONG = "119";
					private static final String WORK_PHONE_EXTENSION_TOO_LONG = "120";
					private static final String WORK_ADDRESS1_TOO_LONG = "121";
					private static final String WORK_ADDRESS2_TOO_LONG = "122";
					private static final String WORK_CITY_TOO_LONG = "123";
					private static final String WORK_STATE_TOO_LONG = "124";
					private static final String WORK_ZIP5_TOO_LONG = "125";
					private static final String WORK_ZIP4_TOO_LONG = "126";
					private static final String BENEFITS_SALARY_TOO_LONG = "127";
					private static final String TRUE_SALARY_TOO_LONG = "128";
					private static final String WORK_HOURS_TOO_LONG = "129";
					private static final String LOCATION_ID_TOO_LONG = "130";
					private static final String SALARY_GRADE_TOO_LONG = "131";
					private static final String RETIRE_VEST_PERCENT_TOO_LONG = "132";
					private static final String JOB_CODE_TOO_LONG = "133";
					private static final String PIN_USER_ID_TOO_LONG = "134";
					private static final String VENDOR_EMPLOYEE_ID_TOO_LONG = "135";
					private static final String MODIFIED_BY_TOO_LONG = "86";
				
					// Administrator approval error numbers
					private static final String EMPLOYER_ID_REQ_APPR = "623";
					private static final String EMPLOYEE_NUMBER_REQ_APPR = "624";
					private static final String STATUS_FLAG_REQ_APPR = "625";
					private static final String WORK_PHONE_AREA_CODE_REQ_APPR = "626";
					private static final String WORK_PHONE1_REQ_APPR = "627";
					private static final String WORK_PHONE2_REQ_APPR = "628";
					private static final String WORK_PHONE_EXTENSION_REQ_APPR = "629";
					private static final String WORK_ADDRESS1_REQ_APPR = "630";
					private static final String WORK_ADDRESS2_REQ_APPR = "631";
					private static final String WORK_CITY_REQ_APPR = "632";
					private static final String WORK_STATE_REQ_APPR = "633";
					private static final String WORK_ZIP5_REQ_APPR = "634";
					private static final String WORK_ZIP4_REQ_APPR = "635";
					private static final String BENEFITS_SALARY_REQ_APPR = "636";
					private static final String TRUE_SALARY_REQ_APPR = "637";
					private static final String PAY_FREQUENCY_REQ_APPR = "638";
					private static final String WORK_HOURS_REQ_APPR = "639";
					private static final String LOCATION_ID_REQ_APPR = "640";
					private static final String SALARY_GRADE_REQ_APPR = "641";
					private static final String DATE_OF_HIRE_REQ_APPR = "642";
					private static final String RETIRE_VEST_PERCENT_REQ_APPR = "643";
					private static final String JOB_CODE_REQ_APPR = "644";
					private static final String UNION_FLAG_REQ_APPR = "645";
					private static final String OFFICER_FLAG_REQ_APPR = "646";
					private static final String PIN_USER_ID_REQ_APPR = "647";
					private static final String VENDOR_EMPLOYEE_ID_REQ_APPR = "648";
					private static final String BENEFIT_GROUP_ID_REQ_APPR = "649";
					private static final String LAST_PERSON_SEQ_NBR_REQ_APPR = "650";
				\t
				public static void main(String[] args) {
						System.out.println("Success");
				}
				}"""
		},
		"Success");
}

public void test005() {
	this.runConformTest(
		new String[] {
			"Code.java",
			"""
				public class Code {
				  public static final String s = "<clinit>";
				  public static final String s2 = "()V";
				  public Code(int i) {
				  }
				public static void main(String[] args) {
				  System.out.print(s.length());
				  System.out.println(s2.length());
				}
				}"""
		},
		"83");
}

public void test006() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					X otherX;\t
					static String STR = "SUCCESS";\t
					public static void main(String args[]) {\t
						try {\t
							System.out.println(new X().otherX.STR);\t
						} catch(NullPointerException e){\t
							System.out.println("FAILED");\t
						}\t
					}\t
				}\t
				""",
		},
		"SUCCESS");
}

/*
 * null is not a constant
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26585
 */
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static final boolean F = false;\t
				    public static final String Str = F ? "dummy" : null;\t
				    public static void main(String[] args) {\t
				        if (Str == null)\t
				        	System.out.println("SUCCESS");\t
				       	else\t
				        	System.out.println("FAILED");\t
				    }\t
				}\t
				""",
		},
		"SUCCESS");
}

/*
 * null is not a constant
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static void main(String[] args) {\t
				      	System.out.println("SUCCESS");\t
					} \t
					void foo(){\t
						while (null == null);	//not an inlinable constant
						System.out.println("unreachable but shouldn't be flagged");\t
					}\t
				}\t
				""",
		},
		"SUCCESS");
}

/*
 * null is not a constant
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
/*
 * null is not a constant
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
public void test009() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static void main(String[] args) {\t
				        if (null == null) System.out.print("1");\t
				        if ((null==null ? null:null) == (null==null ? null:null))\t
				        	System.out.print("2");\t
						boolean b = ("[" + null + "]") == "[null]";  // cannot inline\t
						System.out.print("3");\t
						final String s = (String) null;\t
						if (s == null) System.out.print("4");\t
						final String s2 = (String) "aaa";\t
						if (s2 == "aaa") System.out.println("5");\t
				    }\t
				}""",
		},
		"12345");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	String substring1 = this.complianceLevel < ClassFileConstants.JDK1_5 ?
								"StringBuffer" : "StringBuilder";
	String substring2 = this.complianceLevel < ClassFileConstants.JDK9 ?
								"    21  new java.lang." + substring1 + " [32]\n"
								+ "    24  dup\n"
								+ "    25  ldc <String \"[\"> [34]\n"
								+ "    27  invokespecial java.lang." + substring1 + "(java.lang.String) [36]\n"
								+ "    30  aconst_null\n"
								+ "    31  invokevirtual java.lang." + substring1 + ".append(java.lang.Object) : java.lang." + substring1 + " [38]\n"
								+ "    34  ldc <String \"]\"> [42]\n"
								+ "    36  invokevirtual java.lang." + substring1 + ".append(java.lang.String) : java.lang." + substring1 + " [44]\n"
								+ "    39  invokevirtual java.lang." + substring1 + ".toString() : java.lang.String [47]\n"
									:
									"    21  aconst_null\n" +
									"    22  invokedynamic 0 makeConcatWithConstants(java.lang.String) : java.lang.String [32]\n";
	String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 3, Locals: 4\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     3  ldc <String \"1\"> [22]\n" +
			"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"     8  aconst_null\n" +
			"     9  aconst_null\n" +
			"    10  if_acmpne 21\n" +
			"    13  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    16  ldc <String \"2\"> [30]\n" +
			"    18  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			substring2 +
			"    42  ldc <String \"[null]\"> [51]\n" +
			"    44  if_acmpne 51\n" +
			"    47  iconst_1\n" +
			"    48  goto 52\n" +
			"    51  iconst_0\n" +
			"    52  istore_1 [b]\n" +
			"    53  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    56  ldc <String \"3\"> [53]\n" +
			"    58  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"    61  aconst_null\n" +
			"    62  astore_2 [s]\n" +
			"    63  aload_2 [s]\n" +
			"    64  ifnonnull 75\n" +
			"    67  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    70  ldc <String \"4\"> [55]\n" +
			"    72  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"    75  ldc <String \"aaa\"> [57]\n" +
			"    77  astore_3 [s2]\n" +
			"    78  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    81  ldc <String \"5\"> [59]\n" +
			"    83  invokevirtual java.io.PrintStream.println(java.lang.String) : void [61]\n" +
			"    86  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 8, line: 4]\n" +
			"        [pc: 13, line: 5]\n" +
			"        [pc: 21, line: 6]\n" +
			"        [pc: 53, line: 7]\n" +
			"        [pc: 61, line: 8]\n" +
			"        [pc: 63, line: 9]\n" +
			"        [pc: 75, line: 10]\n" +
			"        [pc: 78, line: 11]\n" +
			"        [pc: 86, line: 12]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 87] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 53, pc: 87] local: b index: 1 type: boolean\n" +
			"        [pc: 63, pc: 87] local: s index: 2 type: java.lang.String\n" +
			"        [pc: 78, pc: 87] local: s2 index: 3 type: java.lang.String\n";

	String expectedOutput9OrLater =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     3  ldc <String \"1\"> [22]\n" +
			"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"     8  aconst_null\n" +
			"     9  aconst_null\n" +
			"    10  if_acmpne 21\n" +
			"    13  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    16  ldc <String \"2\"> [30]\n" +
			"    18  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			substring2 +
			"    27  ldc <String \"[null]\"> [36]\n" +
			"    29  if_acmpne 36\n" +
			"    32  iconst_1\n" +
			"    33  goto 37\n" +
			"    36  iconst_0\n" +
			"    37  istore_1 [b]\n" +
			"    38  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    41  ldc <String \"3\"> [38]\n" +
			"    43  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"    46  aconst_null\n" +
			"    47  astore_2 [s]\n" +
			"    48  aload_2 [s]\n" +
			"    49  ifnonnull 60\n" +
			"    52  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    55  ldc <String \"4\"> [40]\n" +
			"    57  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"    60  ldc <String \"aaa\"> [42]\n" +
			"    62  astore_3 [s2]\n" +
			"    63  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    66  ldc <String \"5\"> [44]\n" +
			"    68  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"    71  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 8, line: 4]\n" +
			"        [pc: 13, line: 5]\n" +
			"        [pc: 21, line: 6]\n" +
			"        [pc: 38, line: 7]\n" +
			"        [pc: 46, line: 8]\n" +
			"        [pc: 48, line: 9]\n" +
			"        [pc: 60, line: 10]\n" +
			"        [pc: 63, line: 11]\n" +
			"        [pc: 71, line: 12]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 72] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 38, pc: 72] local: b index: 1 type: boolean\n" +
			"        [pc: 48, pc: 72] local: s index: 2 type: java.lang.String\n" +
			"        [pc: 63, pc: 72] local: s2 index: 3 type: java.lang.String\n";
	if (this.complianceLevel >= ClassFileConstants.JDK9) {
		int index = actualOutput.indexOf(expectedOutput9OrLater);
		if (index == -1 || expectedOutput9OrLater.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput9OrLater, actualOutput);
		}
	} else {
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
}

/*
 * null is not a constant
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
public void test010() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static void main(String[] args) {\t
				       if (null == null) {
							System.out.print("SUCCESS");\t
							return;\t
						}\t
						System.out.print("SHOULDN'T BE GENERATED");\t
				    }\t
				}\t
				""",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		     0  getstatic java.lang.System.out : java.io.PrintStream [16]
		     3  ldc <String "SUCCESS"> [22]
		     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		     8  return
		     9  getstatic java.lang.System.out : java.io.PrintStream [16]
		    12  ldc <String "SHOULDN\'T BE GENERATED"> [30]
		    14  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		    17  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 8, line: 5]
		        [pc: 9, line: 7]
		        [pc: 17, line: 8]
		      Local variable table:
		        [pc: 0, pc: 18] local: args index: 0 type: java.lang.String[]
		""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

//http://bugs.eclipse.org/bugs/show_bug.cgi?id=30704
public void test011() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
				    public static void main(String[] args) {
						System.out.print((01.f == 1) && (01e0f == 1));\t
				    }
				}""",
		},
		"true");
}

//http://bugs.eclipse.org/bugs/show_bug.cgi?id=79545
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static String C = "" + +\' \';
				    public static String I = "" + +32;
				
				    public static void main(String[] args) {
				        System.out.print(C);
				        System.out.print(I);
				    }
				}""",
		},
		"3232");
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=97190
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						System.out.println(-9223372036854775809L); // KO
						System.out.println(9223372036854775809L); // KO
						System.out.println(9223372036854775808L); // KO
						System.out.println(23092395825689123986L); // KO
						System.out.println(-9223372036854775808L); // OK
						System.out.println(9223372036854775807L); // OK
						System.out.println(2309239582568912398L); // OK
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				System.out.println(-9223372036854775809L); // KO
				                    ^^^^^^^^^^^^^^^^^^^^
			The literal 9223372036854775809L of type long is out of range\s
			----------
			2. ERROR in X.java (at line 4)
				System.out.println(9223372036854775809L); // KO
				                   ^^^^^^^^^^^^^^^^^^^^
			The literal 9223372036854775809L of type long is out of range\s
			----------
			3. ERROR in X.java (at line 5)
				System.out.println(9223372036854775808L); // KO
				                   ^^^^^^^^^^^^^^^^^^^^
			The literal 9223372036854775808L of type long is out of range\s
			----------
			4. ERROR in X.java (at line 6)
				System.out.println(23092395825689123986L); // KO
				                   ^^^^^^^^^^^^^^^^^^^^^
			The literal 23092395825689123986L of type long is out of range\s
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110182
public void test014() throws Exception {
	if (this.complianceLevel > ClassFileConstants.JDK1_5) return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X fx;
					final static boolean DBG = false;
					void foo1(X x) {
						if (x.DBG) {
							boolean b = x.DBG;
						}
						boolean bb;
						if (bb = x.DBG) {
							boolean b = x.DBG;
						}
					}
					void foo2(X x) {
						while (x.DBG) {
							boolean b = x.DBG;
						}
					}
					void foo3(X x) {
						for (;x.DBG;) {
							boolean b = x.DBG;
						}
					}
					void foo4(X x) {
						boolean b = x.DBG ? x == null : x.DBG;
					}
					void foo5() {
						if (this.fx.DBG) {
							boolean b = this.fx.DBG;
						}
					}
					void foo6() {
						while (this.fx.DBG) {
							boolean b = this.fx.DBG;
						}
					}
					void foo7() {
						for (;this.fx.DBG;) {
							boolean b = this.fx.DBG;
						}
					}
					void foo8() {
						boolean b = this.fx.DBG ? this.fx == null : this.fx.DBG;
					}
				}
				""",
		},
		"");
	// ensure boolean codegen got optimized (optimizedBooleanConstant)
	String expectedOutput =
		"""
		  // Method descriptor #20 (LX;)V
		  // Stack: 2, Locals: 4
		  void foo1(X x);
		    0  iconst_0
		    1  dup
		    2  istore_2 [bb]
		    3  ifeq 8
		    6  iconst_0
		    7  istore_3
		    8  return
		      Line numbers:
		        [pc: 0, line: 9]
		        [pc: 6, line: 10]
		        [pc: 8, line: 12]
		      Local variable table:
		        [pc: 0, pc: 9] local: this index: 0 type: X
		        [pc: 0, pc: 9] local: x index: 1 type: X
		        [pc: 3, pc: 9] local: bb index: 2 type: boolean
		 \s
		  // Method descriptor #20 (LX;)V
		  // Stack: 0, Locals: 2
		  void foo2(X x);
		    0  return
		      Line numbers:
		        [pc: 0, line: 17]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		        [pc: 0, pc: 1] local: x index: 1 type: X
		 \s
		  // Method descriptor #20 (LX;)V
		  // Stack: 0, Locals: 2
		  void foo3(X x);
		    0  return
		      Line numbers:
		        [pc: 0, line: 22]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		        [pc: 0, pc: 1] local: x index: 1 type: X
		 \s
		  // Method descriptor #20 (LX;)V
		  // Stack: 1, Locals: 3
		  void foo4(X x);
		    0  iconst_0
		    1  istore_2 [b]
		    2  return
		      Line numbers:
		        [pc: 0, line: 24]
		        [pc: 2, line: 25]
		      Local variable table:
		        [pc: 0, pc: 3] local: this index: 0 type: X
		        [pc: 0, pc: 3] local: x index: 1 type: X
		        [pc: 2, pc: 3] local: b index: 2 type: boolean
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 0, Locals: 1
		  void foo5();
		    0  return
		      Line numbers:
		        [pc: 0, line: 30]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 0, Locals: 1
		  void foo6();
		    0  return
		      Line numbers:
		        [pc: 0, line: 35]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 0, Locals: 1
		  void foo7();
		    0  return
		      Line numbers:
		        [pc: 0, line: 40]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 1, Locals: 2
		  void foo8();
		    0  iconst_0
		    1  istore_1 [b]
		    2  return
		      Line numbers:
		        [pc: 0, line: 42]
		        [pc: 2, line: 43]
		      Local variable table:
		        [pc: 0, pc: 3] local: this index: 0 type: X
		        [pc: 2, pc: 3] local: b index: 1 type: boolean
		""";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}

}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110182 - variation
public void test015() throws Exception {
	if(this.complianceLevel > ClassFileConstants.JDK1_5) return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X fx;
					final static boolean DBG = false;
					void foo1(X x) {
						if (x.DBG) {
							boolean b = x.DBG;
						}
						boolean bb;
						if (bb = x.DBG) {
							boolean b = x.DBG;
						}
					}
					void foo2(X x) {
						while (x.DBG) {
							boolean b = x.DBG;
						}
					}
					void foo3(X x) {
						for (;x.DBG;) {
							boolean b = x.DBG;
						}
					}
					void foo4(X x) {
						boolean b = x.DBG ? x == null : x.DBG;
					}
					void foo5() {
						if (this.fx.DBG) {
							boolean b = this.fx.DBG;
						}
					}
					void foo6() {
						while (this.fx.DBG) {
							boolean b = this.fx.DBG;
						}
					}
					void foo7() {
						for (;this.fx.DBG;) {
							boolean b = this.fx.DBG;
						}
					}
					void foo8() {
						boolean b = this.fx.DBG ? this.fx == null : this.fx.DBG;
					}
				}
				""",
		},
		"");
	// ensure boolean codegen got optimized (optimizedBooleanConstant)
	String expectedOutput =
		"""
		  // Method descriptor #20 (LX;)V
		  // Stack: 2, Locals: 4
		  void foo1(X x);
		    0  iconst_0
		    1  dup
		    2  istore_2 [bb]
		    3  ifeq 8
		    6  iconst_0
		    7  istore_3
		    8  return
		      Line numbers:
		        [pc: 0, line: 9]
		        [pc: 6, line: 10]
		        [pc: 8, line: 12]
		      Local variable table:
		        [pc: 0, pc: 9] local: this index: 0 type: X
		        [pc: 0, pc: 9] local: x index: 1 type: X
		        [pc: 3, pc: 9] local: bb index: 2 type: boolean
		 \s
		  // Method descriptor #20 (LX;)V
		  // Stack: 0, Locals: 2
		  void foo2(X x);
		    0  return
		      Line numbers:
		        [pc: 0, line: 17]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		        [pc: 0, pc: 1] local: x index: 1 type: X
		 \s
		  // Method descriptor #20 (LX;)V
		  // Stack: 0, Locals: 2
		  void foo3(X x);
		    0  return
		      Line numbers:
		        [pc: 0, line: 22]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		        [pc: 0, pc: 1] local: x index: 1 type: X
		 \s
		  // Method descriptor #20 (LX;)V
		  // Stack: 1, Locals: 3
		  void foo4(X x);
		    0  iconst_0
		    1  istore_2 [b]
		    2  return
		      Line numbers:
		        [pc: 0, line: 24]
		        [pc: 2, line: 25]
		      Local variable table:
		        [pc: 0, pc: 3] local: this index: 0 type: X
		        [pc: 0, pc: 3] local: x index: 1 type: X
		        [pc: 2, pc: 3] local: b index: 2 type: boolean
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 0, Locals: 1
		  void foo5();
		    0  return
		      Line numbers:
		        [pc: 0, line: 30]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 0, Locals: 1
		  void foo6();
		    0  return
		      Line numbers:
		        [pc: 0, line: 35]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 0, Locals: 1
		  void foo7();
		    0  return
		      Line numbers:
		        [pc: 0, line: 40]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 1, Locals: 2
		  void foo8();
		    0  iconst_0
		    1  istore_1 [b]
		    2  return
		      Line numbers:
		        [pc: 0, line: 42]
		        [pc: 2, line: 43]
		      Local variable table:
		        [pc: 0, pc: 3] local: this index: 0 type: X
		        [pc: 2, pc: 3] local: b index: 1 type: boolean
		""";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110182 - variation
public void test016() throws Exception {
	if(this.complianceLevel > ClassFileConstants.JDK1_5) return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X fx;
					final static boolean DBG = false;
					void foo1(X x) {
						boolean b;
						if (false ? false : x.DBG) {
							boolean bb = x.DBG;
						}
					}
					void foo2(X x) {
						boolean b;
						while (x == null ? x.DBG : x.DBG) {
							boolean bb = x.DBG;
						}
					}
					void foo3(X x) {
						boolean b;
						for (;x == null ? x.DBG : x.DBG;) {
							boolean bb = x.DBG;
						}
					}
					void foo4(X x) {
						boolean bb = (x == null ? x.DBG :  x.DBG) ? x == null : x.DBG;
					}
				}
				""",
		},
		"");
	// ensure boolean codegen got optimized (optimizedBooleanConstant)
	String expectedOutput =
		"""
		  // Method descriptor #20 (LX;)V
		  // Stack: 0, Locals: 2
		  void foo1(X x);
		    0  return
		      Line numbers:
		        [pc: 0, line: 9]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		        [pc: 0, pc: 1] local: x index: 1 type: X
		 \s
		  // Method descriptor #20 (LX;)V
		  // Stack: 1, Locals: 2
		  void foo2(X x);
		    0  aload_1 [x]
		    1  ifnonnull 4
		    4  return
		      Line numbers:
		        [pc: 0, line: 12]
		        [pc: 4, line: 15]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X
		        [pc: 0, pc: 5] local: x index: 1 type: X
		 \s
		  // Method descriptor #20 (LX;)V
		  // Stack: 1, Locals: 2
		  void foo3(X x);
		    0  aload_1 [x]
		    1  ifnonnull 4
		    4  return
		      Line numbers:
		        [pc: 0, line: 18]
		        [pc: 4, line: 21]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X
		        [pc: 0, pc: 5] local: x index: 1 type: X
		 \s
		  // Method descriptor #20 (LX;)V
		  // Stack: 1, Locals: 3
		  void foo4(X x);
		    0  aload_1 [x]
		    1  ifnonnull 4
		    4  iconst_0
		    5  istore_2 [bb]
		    6  return
		      Line numbers:
		        [pc: 0, line: 23]
		        [pc: 6, line: 24]
		      Local variable table:
		        [pc: 0, pc: 7] local: this index: 0 type: X
		        [pc: 0, pc: 7] local: x index: 1 type: X
		        [pc: 6, pc: 7] local: bb index: 2 type: boolean
		""";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=117495
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
						int x = 2;
				       System.out.println("n: "+(x > 1  ? 2 : 1.0));
				    }
				}""",
		},
		"n: 2.0");
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=117495
public void test018() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
						System.out.println("n: "+(true ? 2 : 1.0));
				    }
				}""",
		},
		"n: 2.0");
}

// http://bugs.eclipse.org/bugs/show_bug.cgi?id=154822
// null is not a constant - again
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    static class Enclosed {
						 static final String constant = "";
						 static final String notAConstant;
				        static {
						     notAConstant = null;
				        }
				    }
				}""",
		},
		"");
}

// http://bugs.eclipse.org/bugs/show_bug.cgi?id=154822
// null is not a constant - again
public void test020() {
	if (this.complianceLevel >= ClassFileConstants.JDK16) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    class Inner {
						 static final String constant = "";
						 static final String notAConstant = null;
				    }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				static final String notAConstant = null;
				                    ^^^^^^^^^^^^
			The field notAConstant cannot be declared static in a non-static inner type, unless initialized with a constant expression
			----------
			""");
}
public void testAllConstants() {
	Constant byteConstant = ByteConstant.fromValue((byte) 1);
	Constant byteConstant2 = ByteConstant.fromValue((byte) 2);
	Constant byteConstant3 = ByteConstant.fromValue((byte) 1);
	Constant charConstant = CharConstant.fromValue('c');
	Constant charConstant2 = CharConstant.fromValue('d');
	Constant charConstant3 = CharConstant.fromValue('c');
	Constant booleanConstant = BooleanConstant.fromValue(true);
	Constant booleanConstant2 = BooleanConstant.fromValue(false);
	Constant booleanConstant3 = BooleanConstant.fromValue(true);
	Constant doubleConstant = DoubleConstant.fromValue(1.0);
	Constant doubleConstant2 = DoubleConstant.fromValue(2.0);
	Constant doubleConstant3 = DoubleConstant.fromValue(1.0);
	Constant floatConstant = FloatConstant.fromValue(1.0f);
	Constant floatConstant2 =  FloatConstant.fromValue(2.0f);
	Constant floatConstant3 =  FloatConstant.fromValue(1.0f);
	Constant intConstant = IntConstant.fromValue(20);
	Constant intConstant2 = IntConstant.fromValue(30);
	Constant intConstant3 = IntConstant.fromValue(20);
	Constant longConstant =  LongConstant.fromValue(3L);
	Constant longConstant2 =  LongConstant.fromValue(4L);
	Constant longConstant3 =  LongConstant.fromValue(3L);
	Constant shortConstant = ShortConstant.fromValue((short) 4);
	Constant shortConstant2 = ShortConstant.fromValue((short) 3);
	Constant shortConstant3 = ShortConstant.fromValue((short) 4);
	Constant stringConstant = StringConstant.fromValue("test");
	Constant stringConstant2 = StringConstant.fromValue("test2");
	Constant stringConstant3 = StringConstant.fromValue("test");
	Constant stringConstant4 = StringConstant.fromValue(null);
	Constant stringConstant5 = StringConstant.fromValue(null);
	ClassSignature classSignature = new ClassSignature("java.lang.Object".toCharArray());
	ClassSignature classSignature2 = new ClassSignature("java.lang.String".toCharArray());
	ClassSignature classSignature3 = new ClassSignature("java.lang.Object".toCharArray());
	EnumConstantSignature enumConstantSignature = new EnumConstantSignature("myEnum".toCharArray(), "C".toCharArray());
	EnumConstantSignature enumConstantSignature2 = new EnumConstantSignature("myEnum".toCharArray(), "A".toCharArray());
	EnumConstantSignature enumConstantSignature3 = new EnumConstantSignature("myEnum".toCharArray(), "C".toCharArray());
	EnumConstantSignature enumConstantSignature4 = new EnumConstantSignature("myEnum2".toCharArray(), "A".toCharArray());

	verifyConstantEqualsAndHashcode(byteConstant, byteConstant2, byteConstant3, intConstant);
	verifyConstantEqualsAndHashcode(charConstant, charConstant2, charConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(booleanConstant, booleanConstant2, booleanConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(doubleConstant, doubleConstant2, doubleConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(floatConstant, floatConstant2, floatConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(intConstant, intConstant2, intConstant3, stringConstant);
	verifyConstantEqualsAndHashcode(longConstant, longConstant2, longConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(shortConstant, shortConstant2, shortConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(stringConstant, stringConstant2, stringConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(stringConstant, stringConstant4, stringConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(stringConstant4, stringConstant3, stringConstant5, byteConstant);
	verifyConstantEqualsAndHashcode(classSignature, classSignature2, classSignature3, byteConstant);
	verifyConstantEqualsAndHashcode(enumConstantSignature, enumConstantSignature2, enumConstantSignature3, byteConstant);
	verifyConstantEqualsAndHashcode(enumConstantSignature, enumConstantSignature4, enumConstantSignature3, byteConstant);
	assertNotNull(Constant.NotAConstant.toString());

	verifyValues(byteConstant, charConstant, booleanConstant, doubleConstant, floatConstant, intConstant, longConstant, shortConstant, stringConstant);
	// check equals between to null string constants
	assertTrue(stringConstant4.equals(stringConstant5));
}
private void verifyValues(
		Constant byteConstant,
		Constant charConstant,
		Constant booleanConstant,
		Constant doubleConstant,
		Constant floatConstant,
		Constant intConstant,
		Constant longConstant,
		Constant shortConstant,
		Constant stringConstant) {

	// byteValue()
	byteConstant.byteValue();
	charConstant.byteValue();
	try {
		booleanConstant.byteValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.byteValue();
	floatConstant.byteValue();
	intConstant.byteValue();
	longConstant.byteValue();
	shortConstant.byteValue();
	try {
		stringConstant.byteValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// booleanValue()
	try {
		byteConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		charConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	booleanConstant.booleanValue();
	try {
		doubleConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		floatConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		intConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		longConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		shortConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		stringConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// charValue()
	byteConstant.charValue();
	charConstant.charValue();
	try {
		booleanConstant.charValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.charValue();
	floatConstant.charValue();
	intConstant.charValue();
	longConstant.charValue();
	shortConstant.charValue();
	try {
		stringConstant.charValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// doubleValue()
	byteConstant.doubleValue();
	charConstant.doubleValue();
	try {
		booleanConstant.doubleValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.doubleValue();
	floatConstant.doubleValue();
	intConstant.doubleValue();
	longConstant.doubleValue();
	shortConstant.doubleValue();
	try {
		stringConstant.doubleValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// floatValue()
	byteConstant.floatValue();
	charConstant.floatValue();
	try {
		booleanConstant.floatValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.floatValue();
	floatConstant.floatValue();
	intConstant.floatValue();
	longConstant.floatValue();
	shortConstant.floatValue();
	try {
		stringConstant.floatValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// intValue()
	byteConstant.intValue();
	charConstant.intValue();
	try {
		booleanConstant.intValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.intValue();
	floatConstant.intValue();
	intConstant.intValue();
	longConstant.intValue();
	shortConstant.intValue();
	try {
		stringConstant.intValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// longValue()
	byteConstant.longValue();
	charConstant.longValue();
	try {
		booleanConstant.longValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.longValue();
	floatConstant.longValue();
	intConstant.longValue();
	longConstant.longValue();
	shortConstant.longValue();
	try {
		stringConstant.longValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// shortValue()
	byteConstant.shortValue();
	charConstant.shortValue();
	try {
		booleanConstant.shortValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.shortValue();
	floatConstant.shortValue();
	intConstant.shortValue();
	longConstant.shortValue();
	shortConstant.shortValue();
	try {
		stringConstant.shortValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// stringValue()
	byteConstant.stringValue();
	charConstant.stringValue();
	booleanConstant.stringValue();
	doubleConstant.stringValue();
	floatConstant.stringValue();
	intConstant.stringValue();
	longConstant.stringValue();
	shortConstant.stringValue();
	stringConstant.stringValue();
}
private void verifyConstantEqualsAndHashcode(
		Object o,
		Object o2,
		Object o3,
		Object o4) {
	assertTrue(o.equals(o));
	assertTrue(o.equals(o3));
	assertFalse(o.equals(o2));
	assertFalse(o.equals(o4));
	assertFalse(o.equals(null));
	assertFalse(o.hashCode() == o2.hashCode());
	assertNotNull(o.toString());

	if (o instanceof Constant) {
		assertTrue("Not the same values", ((Constant) o).hasSameValue((Constant) o3));
		assertFalse("Have same values", ((Constant) o).hasSameValue((Constant) o2));
		assertFalse("Have same values", ((Constant) o).hasSameValue((Constant) o4));
	}
}
//test corner values (max, min, -1) for longs
public void test021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						System.out.println(0x0L); // OK
						System.out.println(0x8000000000000000L); // OK
						System.out.println(0x8000000000000000l); // OK
						System.out.println(01000000000000000000000L); // OK
						System.out.println(01000000000000000000000l); // OK
						System.out.println(-9223372036854775808L); // OK
						System.out.println(-9223372036854775808l); // OK
						System.out.println(0x7fffffffffffffffL); // OK
						System.out.println(0x7fffffffffffffffl); // OK
						System.out.println(0777777777777777777777L); // OK
						System.out.println(0777777777777777777777l); // OK
						System.out.println(9223372036854775807L); // OK
						System.out.println(9223372036854775807l); // OK
						System.out.println(0xffffffffffffffffL); // OK
						System.out.println(0x0000000000000ffffffffffffffffL); // OK
						System.out.println(0xffffffffffffffffl); // OK
						System.out.println(01777777777777777777777L); // OK
						System.out.println(01777777777777777777777l); // OK
						System.out.println(-0x1L); // OK
						System.out.println(-0x1l); // OK
						System.out.println(0677777777777777777777L);
						System.out.println(0677777777777777777777l);
						System.out.println(0x0000000000000L); // OK
						System.out.println(0L); // OK
					}
				}""",
		},
		"""
			0
			-9223372036854775808
			-9223372036854775808
			-9223372036854775808
			-9223372036854775808
			-9223372036854775808
			-9223372036854775808
			9223372036854775807
			9223372036854775807
			9223372036854775807
			9223372036854775807
			9223372036854775807
			9223372036854775807
			-1
			-1
			-1
			-1
			-1
			-1
			-1
			8070450532247928831
			8070450532247928831
			0
			0""");
}
//test corner values (max, min, -1) for ints
public void test022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						System.out.println(0x0); // OK
						System.out.println(0x80000000); // OK
						System.out.println(020000000000); // OK
						System.out.println(-2147483648); // OK
						System.out.println(0x7fffffff); // OK
						System.out.println(017777777777); // OK
						System.out.println(2147483647); // OK
						System.out.println(0xffffffff); // OK
						System.out.println(0x0000000000000ffffffff); // OK
						System.out.println(037777777777); // OK
						System.out.println(-0x1); // OK
						System.out.println(0xDADACAFE);
						System.out.println(0x0000000000000); // OK
					}
				}""",
		},
		"""
			0
			-2147483648
			-2147483648
			-2147483648
			2147483647
			2147483647
			2147483647
			-1
			-1
			-1
			-1
			-623195394
			0""");
}
public void testBug566332_01() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
					        String switchVar = "abc";
					        final String caseStr =  true ? "abc" : "def";
					        switch (switchVar) {
					            case caseStr: System.out.println("Pass");
					        }
					    }
					} """,
			},
			"Pass");
}
public void testBug566332_02() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
					        String switchVar = "abc";
					        final String caseStr =  false ? "abc" : "def";
					        switch (switchVar) {
					            case caseStr: System.out.println("Pass");
					        }
					    }
					} """,
			},
			"");
}
public void testBug566332_03() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
					        boolean b = true;
					        String switchVar = "abc";
					        final String caseStr =  b ? "abc" : "def";
					        switch (switchVar) {
					            case caseStr: System.out.println("Pass");
					        }
					    }
					} """,
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case caseStr: System.out.println("Pass");
					     ^^^^^^^
				case expressions must be constant expressions
				----------
				""");
}
// Same as testBug566332_01(), but without the variable being final
public void testBug566332_04() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
					        String switchVar = "abc";
					        String caseStr =  true ? "abc" : "def";
					        switch (switchVar) {
					            case caseStr: System.out.println("Pass");
					        }
					    }
					} """,
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					String caseStr =  true ? "abc" : "def";
					                                 ^^^^^
				Dead code
				----------
				2. ERROR in X.java (at line 6)
					case caseStr: System.out.println("Pass");
					     ^^^^^^^
				case expressions must be constant expressions
				----------
				""");
}
public void testBug569498() {
	if (this.complianceLevel < ClassFileConstants.JDK11) {
		return;
	}
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						final String s1 = "";
						public void m(Object s) {
							final boolean b = false;
							final String s2 = "";
							m(b? s1 : s2);
						}
					    public static void main(String[] args) {}
					}""",
			},
			"");
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1256
public void testGH1256() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String value = "12345";
						value.equalsIgnoreCase("" + null);
						System.out.println(value.substring(1));
					}
				}""",
		 },
	"2345");
}
public static Class testClass() {
	return ConstantTest.class;
}
}
