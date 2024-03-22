/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ForStatementTest extends AbstractRegressionTest {

public ForStatementTest(String name) {
	super(name);
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 45, 46 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public void test001() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
						public static Object m(int[] arg) {
							yyLoop: for (int i = 0;; ++i) {
								yyInner: for (;;) {
									switch (arg[i]) {
										case 0:
											break;
										case 1:
											continue yyInner;
									}
									if (i == 32)
										return arg;
									if (i == 12)
										break;
									continue yyLoop;
								}
								if (i == 32)
									return null;
								if (i > 7)
									continue yyLoop;
							}
						}
					
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}
					""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo2(int[] array) {
						for (int i = 0; i < array.length; i++) {
							System.out.println(i);
							break;
						}
					}
				}
				""", // =================
		},
		"");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([I)V
		  // Stack: 2, Locals: 3
		  void foo2(int[] array);
		     0  iconst_0
		     1  istore_2 [i]
		     2  iload_2 [i]
		     3  aload_1 [array]
		     4  arraylength
		     5  if_icmpge 15
		     8  getstatic java.lang.System.out : java.io.PrintStream [16]
		    11  iload_2 [i]
		    12  invokevirtual java.io.PrintStream.println(int) : void [22]
		    15  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 8, line: 4]
		        [pc: 15, line: 7]
		      Local variable table:
		        [pc: 0, pc: 16] local: this index: 0 type: X
		        [pc: 0, pc: 16] local: array index: 1 type: int[]
		        [pc: 2, pc: 15] local: i index: 2 type: int
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471 - variation
public void test003() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo4(int[] array) {
						do {
							System.out.println();
							break;
						} while (array.length > 0);
					}
				}
				""", // =================
		},
		"");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([I)V
		  // Stack: 1, Locals: 2
		  void foo4(int[] array);
		    0  getstatic java.lang.System.out : java.io.PrintStream [16]
		    3  invokevirtual java.io.PrintStream.println() : void [22]
		    6  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 6, line: 7]
		      Local variable table:
		        [pc: 0, pc: 7] local: this index: 0 type: X
		        [pc: 0, pc: 7] local: array index: 1 type: int[]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471 - variation
public void test004() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo1(int[] array) {
						while (array.length > 0) {
							System.out.println();
							break;
						}
					}
				}
				""", // =================
		},
		"");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([I)V
		  // Stack: 1, Locals: 2
		  void foo1(int[] array);
		     0  aload_1 [array]
		     1  arraylength
		     2  ifle 11
		     5  getstatic java.lang.System.out : java.io.PrintStream [16]
		     8  invokevirtual java.io.PrintStream.println() : void [22]
		    11  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 5, line: 4]
		        [pc: 11, line: 7]
		      Local variable table:
		        [pc: 0, pc: 12] local: this index: 0 type: X
		        [pc: 0, pc: 12] local: array index: 1 type: int[]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=195317
public void test005() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int mode = 1;
						loop: for (;;) {
							switch (mode) {
								case 2 :
									return;
								case 1:
									mode = 2;
									continue loop;
							}
						}
					}
				}""",
		},
		"");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 1, Locals: 2
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  istore_1 [mode]
		     2  iload_1 [mode]
		     3  tableswitch default: 27
		          case 1: 25
		          case 2: 24
		    24  return
		    25  iconst_2
		    26  istore_1 [mode]
		    27  goto 2
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 2, line: 5]
		        [pc: 24, line: 7]
		        [pc: 25, line: 9]
		        [pc: 27, line: 4]
		      Local variable table:
		        [pc: 0, pc: 30] local: args index: 0 type: java.lang.String[]
		        [pc: 2, pc: 30] local: mode index: 1 type: int
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test006() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    int[][][] intArray = new int[size1][size2][size3];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          intArray[i][j][k] = on ? 0 : 1;
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);

	String expectedOutput =
			"""
		  public void show();
		       0  ldc <String ""> [15]
		       2  astore_1 [s1]
		       3  ldc <String ""> [15]
		       5  astore_2 [s2]
		       6  ldc <String ""> [15]
		       8  astore_3 [s3]
		       9  ldc <String ""> [15]
		      11  astore 4 [s4]
		      13  ldc <String ""> [15]
		      15  astore 5 [s5]
		      17  ldc <String ""> [15]
		      19  astore 6 [s6]
		      21  ldc <String ""> [15]
		      23  astore 7 [s7]
		      25  ldc <String ""> [15]
		      27  astore 8 [s8]
		      29  ldc <String ""> [15]
		      31  astore 9 [s9]
		      33  ldc <String ""> [15]
		      35  astore 10 [s10]
		      37  ldc <String ""> [15]
		      39  astore 11 [s11]
		      41  ldc <String ""> [15]
		      43  astore 12 [s12]
		      45  ldc <String ""> [15]
		      47  astore 13 [s13]
		      49  ldc <String ""> [15]
		      51  astore 14 [s14]
		      53  ldc <String ""> [15]
		      55  astore 15 [s15]
		      57  ldc <String ""> [15]
		      59  astore 16 [s16]
		      61  ldc <String ""> [15]
		      63  astore 17 [s17]
		      65  ldc <String ""> [15]
		      67  astore 18 [s18]
		      69  ldc <String ""> [15]
		      71  astore 19 [s19]
		      73  ldc <String ""> [15]
		      75  astore 20 [s20]
		      77  ldc <String ""> [15]
		      79  astore 21 [s21]
		      81  ldc <String ""> [15]
		      83  astore 22 [s22]
		      85  ldc <String ""> [15]
		      87  astore 23 [s23]
		      89  ldc <String ""> [15]
		      91  astore 24 [s24]
		      93  ldc <String ""> [15]
		      95  astore 25 [s25]
		      97  ldc <String ""> [15]
		      99  astore 26 [s26]
		     101  ldc <String ""> [15]
		     103  astore 27 [s27]
		     105  ldc <String ""> [15]
		     107  astore 28 [s28]
		     109  ldc <String ""> [15]
		     111  astore 29 [s29]
		     113  ldc <String ""> [15]
		     115  astore 30 [s30]
		     117  ldc <String ""> [15]
		     119  astore 31 [s31]
		     121  ldc <String ""> [15]
		     123  astore 32 [s32]
		     125  ldc <String ""> [15]
		     127  astore 33 [s33]
		     129  ldc <String ""> [15]
		     131  astore 34 [s34]
		     133  ldc <String ""> [15]
		     135  astore 35 [s35]
		     137  ldc <String ""> [15]
		     139  astore 36 [s36]
		     141  ldc <String ""> [15]
		     143  astore 37 [s37]
		     145  ldc <String ""> [15]
		     147  astore 38 [s38]
		     149  ldc <String ""> [15]
		     151  astore 39 [s39]
		     153  ldc <String ""> [15]
		     155  astore 40 [s40]
		     157  ldc <String ""> [15]
		     159  astore 41 [s41]
		     161  ldc <String ""> [15]
		     163  astore 42 [s42]
		     165  ldc <String ""> [15]
		     167  astore 43 [s43]
		     169  ldc <String ""> [15]
		     171  astore 44 [s44]
		     173  ldc <String ""> [15]
		     175  astore 45 [s45]
		     177  ldc <String ""> [15]
		     179  astore 46 [s46]
		     181  ldc <String ""> [15]
		     183  astore 47 [s47]
		     185  ldc <String ""> [15]
		     187  astore 48 [s48]
		     189  ldc <String ""> [15]
		     191  astore 49 [s49]
		     193  ldc <String ""> [15]
		     195  astore 50 [s50]
		     197  ldc <String ""> [15]
		     199  astore 51 [s51]
		     201  ldc <String ""> [15]
		     203  astore 52 [s52]
		     205  ldc <String ""> [15]
		     207  astore 53 [s53]
		     209  ldc <String ""> [15]
		     211  astore 54 [s54]
		     213  ldc <String ""> [15]
		     215  astore 55 [s55]
		     217  ldc <String ""> [15]
		     219  astore 56 [s56]
		     221  ldc <String ""> [15]
		     223  astore 57 [s57]
		     225  ldc <String ""> [15]
		     227  astore 58 [s58]
		     229  ldc <String ""> [15]
		     231  astore 59 [s59]
		     233  ldc <String ""> [15]
		     235  astore 60 [s60]
		     237  ldc <String ""> [15]
		     239  astore 61 [s61]
		     241  ldc <String ""> [15]
		     243  astore 62 [s62]
		     245  ldc <String ""> [15]
		     247  astore 63 [s63]
		     249  ldc <String ""> [15]
		     251  astore 64 [s64]
		     253  ldc <String ""> [15]
		     255  astore 65 [s65]
		     257  ldc <String ""> [15]
		     259  astore 66 [s66]
		     261  ldc <String ""> [15]
		     263  astore 67 [s67]
		     265  ldc <String ""> [15]
		     267  astore 68 [s68]
		     269  ldc <String ""> [15]
		     271  astore 69 [s69]
		     273  ldc <String ""> [15]
		     275  astore 70 [s70]
		     277  ldc <String ""> [15]
		     279  astore 71 [s71]
		     281  ldc <String ""> [15]
		     283  astore 72 [s72]
		     285  ldc <String ""> [15]
		     287  astore 73 [s73]
		     289  ldc <String ""> [15]
		     291  astore 74 [s74]
		     293  ldc <String ""> [15]
		     295  astore 75 [s75]
		     297  ldc <String ""> [15]
		     299  astore 76 [s76]
		     301  ldc <String ""> [15]
		     303  astore 77 [s77]
		     305  ldc <String ""> [15]
		     307  astore 78 [s78]
		     309  ldc <String ""> [15]
		     311  astore 79 [s79]
		     313  ldc <String ""> [15]
		     315  astore 80 [s80]
		     317  ldc <String ""> [15]
		     319  astore 81 [s81]
		     321  ldc <String ""> [15]
		     323  astore 82 [s82]
		     325  ldc <String ""> [15]
		     327  astore 83 [s83]
		     329  ldc <String ""> [15]
		     331  astore 84 [s84]
		     333  ldc <String ""> [15]
		     335  astore 85 [s85]
		     337  ldc <String ""> [15]
		     339  astore 86 [s86]
		     341  ldc <String ""> [15]
		     343  astore 87 [s87]
		     345  ldc <String ""> [15]
		     347  astore 88 [s88]
		     349  ldc <String ""> [15]
		     351  astore 89 [s89]
		     353  ldc <String ""> [15]
		     355  astore 90 [s90]
		     357  ldc <String ""> [15]
		     359  astore 91 [s91]
		     361  ldc <String ""> [15]
		     363  astore 92 [s92]
		     365  ldc <String ""> [15]
		     367  astore 93 [s93]
		     369  ldc <String ""> [15]
		     371  astore 94 [s94]
		     373  ldc <String ""> [15]
		     375  astore 95 [s95]
		     377  ldc <String ""> [15]
		     379  astore 96 [s96]
		     381  ldc <String ""> [15]
		     383  astore 97 [s97]
		     385  ldc <String ""> [15]
		     387  astore 98 [s98]
		     389  ldc <String ""> [15]
		     391  astore 99 [s99]
		     393  ldc <String ""> [15]
		     395  astore 100 [s100]
		     397  ldc <String ""> [15]
		     399  astore 101 [s101]
		     401  ldc <String ""> [15]
		     403  astore 102 [s102]
		     405  ldc <String ""> [15]
		     407  astore 103 [s103]
		     409  ldc <String ""> [15]
		     411  astore 104 [s104]
		     413  ldc <String ""> [15]
		     415  astore 105 [s105]
		     417  ldc <String ""> [15]
		     419  astore 106 [s106]
		     421  ldc <String ""> [15]
		     423  astore 107 [s107]
		     425  ldc <String ""> [15]
		     427  astore 108 [s108]
		     429  ldc <String ""> [15]
		     431  astore 109 [s109]
		     433  ldc <String ""> [15]
		     435  astore 110 [s110]
		     437  ldc <String ""> [15]
		     439  astore 111 [s111]
		     441  ldc <String ""> [15]
		     443  astore 112 [s112]
		     445  ldc <String ""> [15]
		     447  astore 113 [s113]
		     449  ldc <String ""> [15]
		     451  astore 114 [s114]
		     453  ldc <String ""> [15]
		     455  astore 115 [s115]
		     457  ldc <String ""> [15]
		     459  astore 116 [s116]
		     461  ldc <String ""> [15]
		     463  astore 117 [s117]
		     465  ldc <String ""> [15]
		     467  astore 118 [s118]
		     469  ldc <String ""> [15]
		     471  astore 119 [s119]
		     473  ldc <String ""> [15]
		     475  astore 120 [s120]
		     477  ldc <String ""> [15]
		     479  astore 121 [s121]
		     481  ldc <String ""> [15]
		     483  astore 122 [s122]
		     485  ldc <String ""> [15]
		     487  astore 123 [s123]
		     489  ldc <String ""> [15]
		     491  astore 124 [s124]
		     493  ldc <String ""> [15]
		     495  astore 125 [s125]
		     497  ldc <String ""> [15]
		     499  astore 126 [s126]
		     501  ldc <String ""> [15]
		     503  astore 127 [s127]
		     505  ldc <String ""> [15]
		     507  astore 128 [s128]
		     509  ldc <String ""> [15]
		     511  astore 129 [s129]
		     513  ldc <String ""> [15]
		     515  astore 130 [s130]
		     517  ldc <String ""> [15]
		     519  astore 131 [s131]
		     521  ldc <String ""> [15]
		     523  astore 132 [s132]
		     525  ldc <String ""> [15]
		     527  astore 133 [s133]
		     529  ldc <String ""> [15]
		     531  astore 134 [s134]
		     533  ldc <String ""> [15]
		     535  astore 135 [s135]
		     537  ldc <String ""> [15]
		     539  astore 136 [s136]
		     541  ldc <String ""> [15]
		     543  astore 137 [s137]
		     545  ldc <String ""> [15]
		     547  astore 138 [s138]
		     549  ldc <String ""> [15]
		     551  astore 139 [s139]
		     553  ldc <String ""> [15]
		     555  astore 140 [s140]
		     557  ldc <String ""> [15]
		     559  astore 141 [s141]
		     561  ldc <String ""> [15]
		     563  astore 142 [s142]
		     565  ldc <String ""> [15]
		     567  astore 143 [s143]
		     569  ldc <String ""> [15]
		     571  astore 144 [s144]
		     573  ldc <String ""> [15]
		     575  astore 145 [s145]
		     577  ldc <String ""> [15]
		     579  astore 146 [s146]
		     581  ldc <String ""> [15]
		     583  astore 147 [s147]
		     585  ldc <String ""> [15]
		     587  astore 148 [s148]
		     589  ldc <String ""> [15]
		     591  astore 149 [s149]
		     593  ldc <String ""> [15]
		     595  astore 150 [s150]
		     597  ldc <String ""> [15]
		     599  astore 151 [s151]
		     601  ldc <String ""> [15]
		     603  astore 152 [s152]
		     605  ldc <String ""> [15]
		     607  astore 153 [s153]
		     609  ldc <String ""> [15]
		     611  astore 154 [s154]
		     613  ldc <String ""> [15]
		     615  astore 155 [s155]
		     617  ldc <String ""> [15]
		     619  astore 156 [s156]
		     621  ldc <String ""> [15]
		     623  astore 157 [s157]
		     625  ldc <String ""> [15]
		     627  astore 158 [s158]
		     629  ldc <String ""> [15]
		     631  astore 159 [s159]
		     633  ldc <String ""> [15]
		     635  astore 160 [s160]
		     637  ldc <String ""> [15]
		     639  astore 161 [s161]
		     641  ldc <String ""> [15]
		     643  astore 162 [s162]
		     645  ldc <String ""> [15]
		     647  astore 163 [s163]
		     649  ldc <String ""> [15]
		     651  astore 164 [s164]
		     653  ldc <String ""> [15]
		     655  astore 165 [s165]
		     657  ldc <String ""> [15]
		     659  astore 166 [s166]
		     661  ldc <String ""> [15]
		     663  astore 167 [s167]
		     665  ldc <String ""> [15]
		     667  astore 168 [s168]
		     669  ldc <String ""> [15]
		     671  astore 169 [s169]
		     673  ldc <String ""> [15]
		     675  astore 170 [s170]
		     677  ldc <String ""> [15]
		     679  astore 171 [s171]
		     681  ldc <String ""> [15]
		     683  astore 172 [s172]
		     685  ldc <String ""> [15]
		     687  astore 173 [s173]
		     689  ldc <String ""> [15]
		     691  astore 174 [s174]
		     693  ldc <String ""> [15]
		     695  astore 175 [s175]
		     697  ldc <String ""> [15]
		     699  astore 176 [s176]
		     701  ldc <String ""> [15]
		     703  astore 177 [s177]
		     705  ldc <String ""> [15]
		     707  astore 178 [s178]
		     709  ldc <String ""> [15]
		     711  astore 179 [s179]
		     713  ldc <String ""> [15]
		     715  astore 180 [s180]
		     717  ldc <String ""> [15]
		     719  astore 181 [s181]
		     721  ldc <String ""> [15]
		     723  astore 182 [s182]
		     725  ldc <String ""> [15]
		     727  astore 183 [s183]
		     729  ldc <String ""> [15]
		     731  astore 184 [s184]
		     733  ldc <String ""> [15]
		     735  astore 185 [s185]
		     737  ldc <String ""> [15]
		     739  astore 186 [s186]
		     741  ldc <String ""> [15]
		     743  astore 187 [s187]
		     745  ldc <String ""> [15]
		     747  astore 188 [s188]
		     749  ldc <String ""> [15]
		     751  astore 189 [s189]
		     753  ldc <String ""> [15]
		     755  astore 190 [s190]
		     757  ldc <String ""> [15]
		     759  astore 191 [s191]
		     761  ldc <String ""> [15]
		     763  astore 192 [s192]
		     765  ldc <String ""> [15]
		     767  astore 193 [s193]
		     769  ldc <String ""> [15]
		     771  astore 194 [s194]
		     773  ldc <String ""> [15]
		     775  astore 195 [s195]
		     777  ldc <String ""> [15]
		     779  astore 196 [s196]
		     781  ldc <String ""> [15]
		     783  astore 197 [s197]
		     785  ldc <String ""> [15]
		     787  astore 198 [s198]
		     789  ldc <String ""> [15]
		     791  astore 199 [s199]
		     793  ldc <String ""> [15]
		     795  astore 200 [s200]
		     797  ldc <String ""> [15]
		     799  astore 201 [s201]
		     801  ldc <String ""> [15]
		     803  astore 202 [s202]
		     805  ldc <String ""> [15]
		     807  astore 203 [s203]
		     809  ldc <String ""> [15]
		     811  astore 204 [s204]
		     813  ldc <String ""> [15]
		     815  astore 205 [s205]
		     817  ldc <String ""> [15]
		     819  astore 206 [s206]
		     821  ldc <String ""> [15]
		     823  astore 207 [s207]
		     825  ldc <String ""> [15]
		     827  astore 208 [s208]
		     829  ldc <String ""> [15]
		     831  astore 209 [s209]
		     833  ldc <String ""> [15]
		     835  astore 210 [s210]
		     837  ldc <String ""> [15]
		     839  astore 211 [s211]
		     841  ldc <String ""> [15]
		     843  astore 212 [s212]
		     845  ldc <String ""> [15]
		     847  astore 213 [s213]
		     849  ldc <String ""> [15]
		     851  astore 214 [s214]
		     853  ldc <String ""> [15]
		     855  astore 215 [s215]
		     857  ldc <String ""> [15]
		     859  astore 216 [s216]
		     861  ldc <String ""> [15]
		     863  astore 217 [s217]
		     865  ldc <String ""> [15]
		     867  astore 218 [s218]
		     869  ldc <String ""> [15]
		     871  astore 219 [s219]
		     873  ldc <String ""> [15]
		     875  astore 220 [s220]
		     877  ldc <String ""> [15]
		     879  astore 221 [s221]
		     881  ldc <String ""> [15]
		     883  astore 222 [s222]
		     885  ldc <String ""> [15]
		     887  astore 223 [s223]
		     889  ldc <String ""> [15]
		     891  astore 224 [s224]
		     893  ldc <String ""> [15]
		     895  astore 225 [s225]
		     897  ldc <String ""> [15]
		     899  astore 226 [s226]
		     901  ldc <String ""> [15]
		     903  astore 227 [s227]
		     905  ldc <String ""> [15]
		     907  astore 228 [s228]
		     909  ldc <String ""> [15]
		     911  astore 229 [s229]
		     913  ldc <String ""> [15]
		     915  astore 230 [s230]
		     917  ldc <String ""> [15]
		     919  astore 231 [s231]
		     921  ldc <String ""> [15]
		     923  astore 232 [s232]
		     925  ldc <String ""> [15]
		     927  astore 233 [s233]
		     929  ldc <String ""> [15]
		     931  astore 234 [s234]
		     933  ldc <String ""> [15]
		     935  astore 235 [s235]
		     937  ldc <String ""> [15]
		     939  astore 236 [s236]
		     941  ldc <String ""> [15]
		     943  astore 237 [s237]
		     945  ldc <String ""> [15]
		     947  astore 238 [s238]
		     949  ldc <String ""> [15]
		     951  astore 239 [s239]
		     953  ldc <String ""> [15]
		     955  astore 240 [s240]
		     957  ldc <String ""> [15]
		     959  astore 241 [s241]
		     961  ldc <String ""> [15]
		     963  astore 242 [s242]
		     965  ldc <String ""> [15]
		     967  astore 243 [s243]
		     969  ldc <String ""> [15]
		     971  astore 244 [s244]
		     973  ldc <String ""> [15]
		     975  astore 245 [s245]
		     977  ldc <String ""> [15]
		     979  astore 246 [s246]
		     981  ldc <String ""> [15]
		     983  astore 247 [s247]
		     985  ldc <String ""> [15]
		     987  astore 248 [s248]
		     989  ldc <String ""> [15]
		     991  astore 249 [s249]
		     993  ldc <String ""> [15]
		     995  astore 250 [s250]
		     997  ldc <String ""> [15]
		     999  astore 251 [s251]
		    1001  ldc <String ""> [15]
		    1003  astore 252 [s252]
		    1005  iconst_1
		    1006  istore 253 [size1]
		    1008  iconst_2
		    1009  istore 254 [size2]
		    1011  iconst_3
		    1012  istore 255 [size3]
		    1014  iload 253 [size1]
		    1016  iload 254 [size2]
		    1018  iload 255 [size3]
		    1020  multianewarray int[][][] [17]
		    1024  wide
		    1025  astore 256 [intArray]
		    1028  iconst_0
		    1029  wide
		    1030  istore 257 [i]
		    1033  goto 1124
		    1036  iconst_0
		    1037  wide
		    1038  istore 258 [j]
		    1041  goto 1109
		    1044  iconst_0
		    1045  wide
		    1046  istore 259 [on]
		    1049  iconst_0
		    1050  wide
		    1051  istore 260 [k]
		    1054  goto 1094
		    1057  wide
		    1058  aload 256 [intArray]
		    1061  wide
		    1062  iload 257 [i]
		    1065  aaload
		    1066  wide
		    1067  iload 258 [j]
		    1070  aaload
		    1071  wide
		    1072  iload 260 [k]
		    1075  wide
		    1076  iload 259 [on]
		    1079  ifeq 1086
		    1082  iconst_0
		    1083  goto 1087
		    1086  iconst_1
		    1087  iastore
		    1088  wide
		    1089  iinc 260 1 [k]
		    1094  wide
		    1095  iload 260 [k]
		    1098  iload 255 [size3]
		    1100  if_icmplt 1057
		    1103  wide
		    1104  iinc 258 1 [j]
		    1109  wide
		    1110  iload 258 [j]
		    1113  iload 254 [size2]
		    1115  if_icmplt 1044
		    1118  wide
		    1119  iinc 257 1 [i]
		    1124  wide
		    1125  iload 257 [i]
		    1128  iload 253 [size1]
		    1130  if_icmplt 1036
		    1133  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 37, line: 5]
		        [pc: 77, line: 6]
		        [pc: 117, line: 7]
		        [pc: 157, line: 8]
		        [pc: 197, line: 9]
		        [pc: 237, line: 10]
		        [pc: 277, line: 11]
		        [pc: 317, line: 12]
		        [pc: 357, line: 13]
		        [pc: 397, line: 14]
		        [pc: 437, line: 15]
		        [pc: 477, line: 16]
		        [pc: 517, line: 17]
		        [pc: 557, line: 18]
		        [pc: 597, line: 19]
		        [pc: 637, line: 20]
		        [pc: 677, line: 21]
		        [pc: 717, line: 22]
		        [pc: 757, line: 23]
		        [pc: 797, line: 24]
		        [pc: 837, line: 25]
		        [pc: 877, line: 26]
		        [pc: 917, line: 27]
		        [pc: 957, line: 28]
		        [pc: 997, line: 29]
		        [pc: 1005, line: 31]
		        [pc: 1008, line: 32]
		        [pc: 1011, line: 33]
		        [pc: 1014, line: 35]
		        [pc: 1028, line: 37]
		        [pc: 1036, line: 38]
		        [pc: 1044, line: 39]
		        [pc: 1049, line: 40]
		        [pc: 1057, line: 41]
		        [pc: 1088, line: 40]
		        [pc: 1103, line: 38]
		        [pc: 1118, line: 37]
		        [pc: 1133, line: 46]
		      Local variable table:
		        [pc: 0, pc: 1134] local: this index: 0 type: X
		        [pc: 3, pc: 1134] local: s1 index: 1 type: java.lang.String
		        [pc: 6, pc: 1134] local: s2 index: 2 type: java.lang.String
		        [pc: 9, pc: 1134] local: s3 index: 3 type: java.lang.String
		        [pc: 13, pc: 1134] local: s4 index: 4 type: java.lang.String
		        [pc: 17, pc: 1134] local: s5 index: 5 type: java.lang.String
		        [pc: 21, pc: 1134] local: s6 index: 6 type: java.lang.String
		        [pc: 25, pc: 1134] local: s7 index: 7 type: java.lang.String
		        [pc: 29, pc: 1134] local: s8 index: 8 type: java.lang.String
		        [pc: 33, pc: 1134] local: s9 index: 9 type: java.lang.String
		        [pc: 37, pc: 1134] local: s10 index: 10 type: java.lang.String
		        [pc: 41, pc: 1134] local: s11 index: 11 type: java.lang.String
		        [pc: 45, pc: 1134] local: s12 index: 12 type: java.lang.String
		        [pc: 49, pc: 1134] local: s13 index: 13 type: java.lang.String
		        [pc: 53, pc: 1134] local: s14 index: 14 type: java.lang.String
		        [pc: 57, pc: 1134] local: s15 index: 15 type: java.lang.String
		        [pc: 61, pc: 1134] local: s16 index: 16 type: java.lang.String
		        [pc: 65, pc: 1134] local: s17 index: 17 type: java.lang.String
		        [pc: 69, pc: 1134] local: s18 index: 18 type: java.lang.String
		        [pc: 73, pc: 1134] local: s19 index: 19 type: java.lang.String
		        [pc: 77, pc: 1134] local: s20 index: 20 type: java.lang.String
		        [pc: 81, pc: 1134] local: s21 index: 21 type: java.lang.String
		        [pc: 85, pc: 1134] local: s22 index: 22 type: java.lang.String
		        [pc: 89, pc: 1134] local: s23 index: 23 type: java.lang.String
		        [pc: 93, pc: 1134] local: s24 index: 24 type: java.lang.String
		        [pc: 97, pc: 1134] local: s25 index: 25 type: java.lang.String
		        [pc: 101, pc: 1134] local: s26 index: 26 type: java.lang.String
		        [pc: 105, pc: 1134] local: s27 index: 27 type: java.lang.String
		        [pc: 109, pc: 1134] local: s28 index: 28 type: java.lang.String
		        [pc: 113, pc: 1134] local: s29 index: 29 type: java.lang.String
		        [pc: 117, pc: 1134] local: s30 index: 30 type: java.lang.String
		        [pc: 121, pc: 1134] local: s31 index: 31 type: java.lang.String
		        [pc: 125, pc: 1134] local: s32 index: 32 type: java.lang.String
		        [pc: 129, pc: 1134] local: s33 index: 33 type: java.lang.String
		        [pc: 133, pc: 1134] local: s34 index: 34 type: java.lang.String
		        [pc: 137, pc: 1134] local: s35 index: 35 type: java.lang.String
		        [pc: 141, pc: 1134] local: s36 index: 36 type: java.lang.String
		        [pc: 145, pc: 1134] local: s37 index: 37 type: java.lang.String
		        [pc: 149, pc: 1134] local: s38 index: 38 type: java.lang.String
		        [pc: 153, pc: 1134] local: s39 index: 39 type: java.lang.String
		        [pc: 157, pc: 1134] local: s40 index: 40 type: java.lang.String
		        [pc: 161, pc: 1134] local: s41 index: 41 type: java.lang.String
		        [pc: 165, pc: 1134] local: s42 index: 42 type: java.lang.String
		        [pc: 169, pc: 1134] local: s43 index: 43 type: java.lang.String
		        [pc: 173, pc: 1134] local: s44 index: 44 type: java.lang.String
		        [pc: 177, pc: 1134] local: s45 index: 45 type: java.lang.String
		        [pc: 181, pc: 1134] local: s46 index: 46 type: java.lang.String
		        [pc: 185, pc: 1134] local: s47 index: 47 type: java.lang.String
		        [pc: 189, pc: 1134] local: s48 index: 48 type: java.lang.String
		        [pc: 193, pc: 1134] local: s49 index: 49 type: java.lang.String
		        [pc: 197, pc: 1134] local: s50 index: 50 type: java.lang.String
		        [pc: 201, pc: 1134] local: s51 index: 51 type: java.lang.String
		        [pc: 205, pc: 1134] local: s52 index: 52 type: java.lang.String
		        [pc: 209, pc: 1134] local: s53 index: 53 type: java.lang.String
		        [pc: 213, pc: 1134] local: s54 index: 54 type: java.lang.String
		        [pc: 217, pc: 1134] local: s55 index: 55 type: java.lang.String
		        [pc: 221, pc: 1134] local: s56 index: 56 type: java.lang.String
		        [pc: 225, pc: 1134] local: s57 index: 57 type: java.lang.String
		        [pc: 229, pc: 1134] local: s58 index: 58 type: java.lang.String
		        [pc: 233, pc: 1134] local: s59 index: 59 type: java.lang.String
		        [pc: 237, pc: 1134] local: s60 index: 60 type: java.lang.String
		        [pc: 241, pc: 1134] local: s61 index: 61 type: java.lang.String
		        [pc: 245, pc: 1134] local: s62 index: 62 type: java.lang.String
		        [pc: 249, pc: 1134] local: s63 index: 63 type: java.lang.String
		        [pc: 253, pc: 1134] local: s64 index: 64 type: java.lang.String
		        [pc: 257, pc: 1134] local: s65 index: 65 type: java.lang.String
		        [pc: 261, pc: 1134] local: s66 index: 66 type: java.lang.String
		        [pc: 265, pc: 1134] local: s67 index: 67 type: java.lang.String
		        [pc: 269, pc: 1134] local: s68 index: 68 type: java.lang.String
		        [pc: 273, pc: 1134] local: s69 index: 69 type: java.lang.String
		        [pc: 277, pc: 1134] local: s70 index: 70 type: java.lang.String
		        [pc: 281, pc: 1134] local: s71 index: 71 type: java.lang.String
		        [pc: 285, pc: 1134] local: s72 index: 72 type: java.lang.String
		        [pc: 289, pc: 1134] local: s73 index: 73 type: java.lang.String
		        [pc: 293, pc: 1134] local: s74 index: 74 type: java.lang.String
		        [pc: 297, pc: 1134] local: s75 index: 75 type: java.lang.String
		        [pc: 301, pc: 1134] local: s76 index: 76 type: java.lang.String
		        [pc: 305, pc: 1134] local: s77 index: 77 type: java.lang.String
		        [pc: 309, pc: 1134] local: s78 index: 78 type: java.lang.String
		        [pc: 313, pc: 1134] local: s79 index: 79 type: java.lang.String
		        [pc: 317, pc: 1134] local: s80 index: 80 type: java.lang.String
		        [pc: 321, pc: 1134] local: s81 index: 81 type: java.lang.String
		        [pc: 325, pc: 1134] local: s82 index: 82 type: java.lang.String
		        [pc: 329, pc: 1134] local: s83 index: 83 type: java.lang.String
		        [pc: 333, pc: 1134] local: s84 index: 84 type: java.lang.String
		        [pc: 337, pc: 1134] local: s85 index: 85 type: java.lang.String
		        [pc: 341, pc: 1134] local: s86 index: 86 type: java.lang.String
		        [pc: 345, pc: 1134] local: s87 index: 87 type: java.lang.String
		        [pc: 349, pc: 1134] local: s88 index: 88 type: java.lang.String
		        [pc: 353, pc: 1134] local: s89 index: 89 type: java.lang.String
		        [pc: 357, pc: 1134] local: s90 index: 90 type: java.lang.String
		        [pc: 361, pc: 1134] local: s91 index: 91 type: java.lang.String
		        [pc: 365, pc: 1134] local: s92 index: 92 type: java.lang.String
		        [pc: 369, pc: 1134] local: s93 index: 93 type: java.lang.String
		        [pc: 373, pc: 1134] local: s94 index: 94 type: java.lang.String
		        [pc: 377, pc: 1134] local: s95 index: 95 type: java.lang.String
		        [pc: 381, pc: 1134] local: s96 index: 96 type: java.lang.String
		        [pc: 385, pc: 1134] local: s97 index: 97 type: java.lang.String
		        [pc: 389, pc: 1134] local: s98 index: 98 type: java.lang.String
		        [pc: 393, pc: 1134] local: s99 index: 99 type: java.lang.String
		        [pc: 397, pc: 1134] local: s100 index: 100 type: java.lang.String
		        [pc: 401, pc: 1134] local: s101 index: 101 type: java.lang.String
		        [pc: 405, pc: 1134] local: s102 index: 102 type: java.lang.String
		        [pc: 409, pc: 1134] local: s103 index: 103 type: java.lang.String
		        [pc: 413, pc: 1134] local: s104 index: 104 type: java.lang.String
		        [pc: 417, pc: 1134] local: s105 index: 105 type: java.lang.String
		        [pc: 421, pc: 1134] local: s106 index: 106 type: java.lang.String
		        [pc: 425, pc: 1134] local: s107 index: 107 type: java.lang.String
		        [pc: 429, pc: 1134] local: s108 index: 108 type: java.lang.String
		        [pc: 433, pc: 1134] local: s109 index: 109 type: java.lang.String
		        [pc: 437, pc: 1134] local: s110 index: 110 type: java.lang.String
		        [pc: 441, pc: 1134] local: s111 index: 111 type: java.lang.String
		        [pc: 445, pc: 1134] local: s112 index: 112 type: java.lang.String
		        [pc: 449, pc: 1134] local: s113 index: 113 type: java.lang.String
		        [pc: 453, pc: 1134] local: s114 index: 114 type: java.lang.String
		        [pc: 457, pc: 1134] local: s115 index: 115 type: java.lang.String
		        [pc: 461, pc: 1134] local: s116 index: 116 type: java.lang.String
		        [pc: 465, pc: 1134] local: s117 index: 117 type: java.lang.String
		        [pc: 469, pc: 1134] local: s118 index: 118 type: java.lang.String
		        [pc: 473, pc: 1134] local: s119 index: 119 type: java.lang.String
		        [pc: 477, pc: 1134] local: s120 index: 120 type: java.lang.String
		        [pc: 481, pc: 1134] local: s121 index: 121 type: java.lang.String
		        [pc: 485, pc: 1134] local: s122 index: 122 type: java.lang.String
		        [pc: 489, pc: 1134] local: s123 index: 123 type: java.lang.String
		        [pc: 493, pc: 1134] local: s124 index: 124 type: java.lang.String
		        [pc: 497, pc: 1134] local: s125 index: 125 type: java.lang.String
		        [pc: 501, pc: 1134] local: s126 index: 126 type: java.lang.String
		        [pc: 505, pc: 1134] local: s127 index: 127 type: java.lang.String
		        [pc: 509, pc: 1134] local: s128 index: 128 type: java.lang.String
		        [pc: 513, pc: 1134] local: s129 index: 129 type: java.lang.String
		        [pc: 517, pc: 1134] local: s130 index: 130 type: java.lang.String
		        [pc: 521, pc: 1134] local: s131 index: 131 type: java.lang.String
		        [pc: 525, pc: 1134] local: s132 index: 132 type: java.lang.String
		        [pc: 529, pc: 1134] local: s133 index: 133 type: java.lang.String
		        [pc: 533, pc: 1134] local: s134 index: 134 type: java.lang.String
		        [pc: 537, pc: 1134] local: s135 index: 135 type: java.lang.String
		        [pc: 541, pc: 1134] local: s136 index: 136 type: java.lang.String
		        [pc: 545, pc: 1134] local: s137 index: 137 type: java.lang.String
		        [pc: 549, pc: 1134] local: s138 index: 138 type: java.lang.String
		        [pc: 553, pc: 1134] local: s139 index: 139 type: java.lang.String
		        [pc: 557, pc: 1134] local: s140 index: 140 type: java.lang.String
		        [pc: 561, pc: 1134] local: s141 index: 141 type: java.lang.String
		        [pc: 565, pc: 1134] local: s142 index: 142 type: java.lang.String
		        [pc: 569, pc: 1134] local: s143 index: 143 type: java.lang.String
		        [pc: 573, pc: 1134] local: s144 index: 144 type: java.lang.String
		        [pc: 577, pc: 1134] local: s145 index: 145 type: java.lang.String
		        [pc: 581, pc: 1134] local: s146 index: 146 type: java.lang.String
		        [pc: 585, pc: 1134] local: s147 index: 147 type: java.lang.String
		        [pc: 589, pc: 1134] local: s148 index: 148 type: java.lang.String
		        [pc: 593, pc: 1134] local: s149 index: 149 type: java.lang.String
		        [pc: 597, pc: 1134] local: s150 index: 150 type: java.lang.String
		        [pc: 601, pc: 1134] local: s151 index: 151 type: java.lang.String
		        [pc: 605, pc: 1134] local: s152 index: 152 type: java.lang.String
		        [pc: 609, pc: 1134] local: s153 index: 153 type: java.lang.String
		        [pc: 613, pc: 1134] local: s154 index: 154 type: java.lang.String
		        [pc: 617, pc: 1134] local: s155 index: 155 type: java.lang.String
		        [pc: 621, pc: 1134] local: s156 index: 156 type: java.lang.String
		        [pc: 625, pc: 1134] local: s157 index: 157 type: java.lang.String
		        [pc: 629, pc: 1134] local: s158 index: 158 type: java.lang.String
		        [pc: 633, pc: 1134] local: s159 index: 159 type: java.lang.String
		        [pc: 637, pc: 1134] local: s160 index: 160 type: java.lang.String
		        [pc: 641, pc: 1134] local: s161 index: 161 type: java.lang.String
		        [pc: 645, pc: 1134] local: s162 index: 162 type: java.lang.String
		        [pc: 649, pc: 1134] local: s163 index: 163 type: java.lang.String
		        [pc: 653, pc: 1134] local: s164 index: 164 type: java.lang.String
		        [pc: 657, pc: 1134] local: s165 index: 165 type: java.lang.String
		        [pc: 661, pc: 1134] local: s166 index: 166 type: java.lang.String
		        [pc: 665, pc: 1134] local: s167 index: 167 type: java.lang.String
		        [pc: 669, pc: 1134] local: s168 index: 168 type: java.lang.String
		        [pc: 673, pc: 1134] local: s169 index: 169 type: java.lang.String
		        [pc: 677, pc: 1134] local: s170 index: 170 type: java.lang.String
		        [pc: 681, pc: 1134] local: s171 index: 171 type: java.lang.String
		        [pc: 685, pc: 1134] local: s172 index: 172 type: java.lang.String
		        [pc: 689, pc: 1134] local: s173 index: 173 type: java.lang.String
		        [pc: 693, pc: 1134] local: s174 index: 174 type: java.lang.String
		        [pc: 697, pc: 1134] local: s175 index: 175 type: java.lang.String
		        [pc: 701, pc: 1134] local: s176 index: 176 type: java.lang.String
		        [pc: 705, pc: 1134] local: s177 index: 177 type: java.lang.String
		        [pc: 709, pc: 1134] local: s178 index: 178 type: java.lang.String
		        [pc: 713, pc: 1134] local: s179 index: 179 type: java.lang.String
		        [pc: 717, pc: 1134] local: s180 index: 180 type: java.lang.String
		        [pc: 721, pc: 1134] local: s181 index: 181 type: java.lang.String
		        [pc: 725, pc: 1134] local: s182 index: 182 type: java.lang.String
		        [pc: 729, pc: 1134] local: s183 index: 183 type: java.lang.String
		        [pc: 733, pc: 1134] local: s184 index: 184 type: java.lang.String
		        [pc: 737, pc: 1134] local: s185 index: 185 type: java.lang.String
		        [pc: 741, pc: 1134] local: s186 index: 186 type: java.lang.String
		        [pc: 745, pc: 1134] local: s187 index: 187 type: java.lang.String
		        [pc: 749, pc: 1134] local: s188 index: 188 type: java.lang.String
		        [pc: 753, pc: 1134] local: s189 index: 189 type: java.lang.String
		        [pc: 757, pc: 1134] local: s190 index: 190 type: java.lang.String
		        [pc: 761, pc: 1134] local: s191 index: 191 type: java.lang.String
		        [pc: 765, pc: 1134] local: s192 index: 192 type: java.lang.String
		        [pc: 769, pc: 1134] local: s193 index: 193 type: java.lang.String
		        [pc: 773, pc: 1134] local: s194 index: 194 type: java.lang.String
		        [pc: 777, pc: 1134] local: s195 index: 195 type: java.lang.String
		        [pc: 781, pc: 1134] local: s196 index: 196 type: java.lang.String
		        [pc: 785, pc: 1134] local: s197 index: 197 type: java.lang.String
		        [pc: 789, pc: 1134] local: s198 index: 198 type: java.lang.String
		        [pc: 793, pc: 1134] local: s199 index: 199 type: java.lang.String
		        [pc: 797, pc: 1134] local: s200 index: 200 type: java.lang.String
		        [pc: 801, pc: 1134] local: s201 index: 201 type: java.lang.String
		        [pc: 805, pc: 1134] local: s202 index: 202 type: java.lang.String
		        [pc: 809, pc: 1134] local: s203 index: 203 type: java.lang.String
		        [pc: 813, pc: 1134] local: s204 index: 204 type: java.lang.String
		        [pc: 817, pc: 1134] local: s205 index: 205 type: java.lang.String
		        [pc: 821, pc: 1134] local: s206 index: 206 type: java.lang.String
		        [pc: 825, pc: 1134] local: s207 index: 207 type: java.lang.String
		        [pc: 829, pc: 1134] local: s208 index: 208 type: java.lang.String
		        [pc: 833, pc: 1134] local: s209 index: 209 type: java.lang.String
		        [pc: 837, pc: 1134] local: s210 index: 210 type: java.lang.String
		        [pc: 841, pc: 1134] local: s211 index: 211 type: java.lang.String
		        [pc: 845, pc: 1134] local: s212 index: 212 type: java.lang.String
		        [pc: 849, pc: 1134] local: s213 index: 213 type: java.lang.String
		        [pc: 853, pc: 1134] local: s214 index: 214 type: java.lang.String
		        [pc: 857, pc: 1134] local: s215 index: 215 type: java.lang.String
		        [pc: 861, pc: 1134] local: s216 index: 216 type: java.lang.String
		        [pc: 865, pc: 1134] local: s217 index: 217 type: java.lang.String
		        [pc: 869, pc: 1134] local: s218 index: 218 type: java.lang.String
		        [pc: 873, pc: 1134] local: s219 index: 219 type: java.lang.String
		        [pc: 877, pc: 1134] local: s220 index: 220 type: java.lang.String
		        [pc: 881, pc: 1134] local: s221 index: 221 type: java.lang.String
		        [pc: 885, pc: 1134] local: s222 index: 222 type: java.lang.String
		        [pc: 889, pc: 1134] local: s223 index: 223 type: java.lang.String
		        [pc: 893, pc: 1134] local: s224 index: 224 type: java.lang.String
		        [pc: 897, pc: 1134] local: s225 index: 225 type: java.lang.String
		        [pc: 901, pc: 1134] local: s226 index: 226 type: java.lang.String
		        [pc: 905, pc: 1134] local: s227 index: 227 type: java.lang.String
		        [pc: 909, pc: 1134] local: s228 index: 228 type: java.lang.String
		        [pc: 913, pc: 1134] local: s229 index: 229 type: java.lang.String
		        [pc: 917, pc: 1134] local: s230 index: 230 type: java.lang.String
		        [pc: 921, pc: 1134] local: s231 index: 231 type: java.lang.String
		        [pc: 925, pc: 1134] local: s232 index: 232 type: java.lang.String
		        [pc: 929, pc: 1134] local: s233 index: 233 type: java.lang.String
		        [pc: 933, pc: 1134] local: s234 index: 234 type: java.lang.String
		        [pc: 937, pc: 1134] local: s235 index: 235 type: java.lang.String
		        [pc: 941, pc: 1134] local: s236 index: 236 type: java.lang.String
		        [pc: 945, pc: 1134] local: s237 index: 237 type: java.lang.String
		        [pc: 949, pc: 1134] local: s238 index: 238 type: java.lang.String
		        [pc: 953, pc: 1134] local: s239 index: 239 type: java.lang.String
		        [pc: 957, pc: 1134] local: s240 index: 240 type: java.lang.String
		        [pc: 961, pc: 1134] local: s241 index: 241 type: java.lang.String
		        [pc: 965, pc: 1134] local: s242 index: 242 type: java.lang.String
		        [pc: 969, pc: 1134] local: s243 index: 243 type: java.lang.String
		        [pc: 973, pc: 1134] local: s244 index: 244 type: java.lang.String
		        [pc: 977, pc: 1134] local: s245 index: 245 type: java.lang.String
		        [pc: 981, pc: 1134] local: s246 index: 246 type: java.lang.String
		        [pc: 985, pc: 1134] local: s247 index: 247 type: java.lang.String
		        [pc: 989, pc: 1134] local: s248 index: 248 type: java.lang.String
		        [pc: 993, pc: 1134] local: s249 index: 249 type: java.lang.String
		        [pc: 997, pc: 1134] local: s250 index: 250 type: java.lang.String
		        [pc: 1001, pc: 1134] local: s251 index: 251 type: java.lang.String
		        [pc: 1005, pc: 1134] local: s252 index: 252 type: java.lang.String
		        [pc: 1008, pc: 1134] local: size1 index: 253 type: int
		        [pc: 1011, pc: 1134] local: size2 index: 254 type: int
		        [pc: 1014, pc: 1134] local: size3 index: 255 type: int
		        [pc: 1028, pc: 1134] local: intArray index: 256 type: int[][][]
		        [pc: 1033, pc: 1133] local: i index: 257 type: int
		        [pc: 1041, pc: 1118] local: j index: 258 type: int
		        [pc: 1049, pc: 1103] local: on index: 259 type: boolean
		        [pc: 1054, pc: 1103] local: k index: 260 type: int
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test007() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    int[][][] intArray = new int[size1][size2][];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        intArray[i][j] = new int[size3];
				        for (int k = 0; k < size3; k++) {
				          intArray[i][j][k] = on ? 0 : 1;
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test008() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    String[][][] array = new String[size1][size2][size3];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? "true" : "false";
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test009() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    boolean[][][] array = new boolean[size1][size2][size3];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? true : false;
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test010() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    long[][][] array = new long[size1][size2][size3];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? 234L : 12345L;
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test011() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    double[][][] array = new double[size1][size2][size3];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? 2.0 : 3.0;
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test012() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    float[][][] array = new float[size1][size2][size3];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? 2.0f : 3.0f;
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test013() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    char[][][] array = new char[size1][size2][size3];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? 'c' : 'd';
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test014() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    byte[][][] array = new byte[size1][size2][size3];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? (byte) 1 : (byte) 0;
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test015() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				
				  public void show() {
				    String s1 = "";   String s2 = "";   String s3 = "";   String s4 = "";   String s5 = "";   String s6 = "";   String s7 = "";   String s8 = "";   String s9 = "";   String s10 = "";
				    String s11 = "";  String s12 = "";  String s13 = "";  String s14 = "";  String s15 = "";  String s16 = "";  String s17 = "";  String s18 = "";  String s19 = "";  String s20 = "";
				    String s21 = "";  String s22 = "";  String s23 = "";  String s24 = "";  String s25 = "";  String s26 = "";  String s27 = "";  String s28 = "";  String s29 = "";  String s30 = "";
				    String s31 = "";  String s32 = "";  String s33 = "";  String s34 = "";  String s35 = "";  String s36 = "";  String s37 = "";  String s38 = "";  String s39 = "";  String s40 = "";
				    String s41 = "";  String s42 = "";  String s43 = "";  String s44 = "";  String s45 = "";  String s46 = "";  String s47 = "";  String s48 = "";  String s49 = "";  String s50 = "";
				    String s51 = "";  String s52 = "";  String s53 = "";  String s54 = "";  String s55 = "";  String s56 = "";  String s57 = "";  String s58 = "";  String s59 = "";  String s60 = "";
				    String s61 = "";  String s62 = "";  String s63 = "";  String s64 = "";  String s65 = "";  String s66 = "";  String s67 = "";  String s68 = "";  String s69 = "";  String s70 = "";
				    String s71 = "";  String s72 = "";  String s73 = "";  String s74 = "";  String s75 = "";  String s76 = "";  String s77 = "";  String s78 = "";  String s79 = "";  String s80 = "";
				    String s81 = "";  String s82 = "";  String s83 = "";  String s84 = "";  String s85 = "";  String s86 = "";  String s87 = "";  String s88 = "";  String s89 = "";  String s90 = "";
				    String s91 = "";  String s92 = "";  String s93 = "";  String s94 = "";  String s95 = "";  String s96 = "";  String s97 = "";  String s98 = "";  String s99 = "";  String s100 = "";
				    String s101 = ""; String s102 = ""; String s103 = ""; String s104 = ""; String s105 = ""; String s106 = ""; String s107 = ""; String s108 = ""; String s109 = ""; String s110 = "";
				    String s111 = ""; String s112 = ""; String s113 = ""; String s114 = ""; String s115 = ""; String s116 = ""; String s117 = ""; String s118 = ""; String s119 = ""; String s120 = "";
				    String s121 = ""; String s122 = ""; String s123 = ""; String s124 = ""; String s125 = ""; String s126 = ""; String s127 = ""; String s128 = ""; String s129 = ""; String s130 = "";
				    String s131 = ""; String s132 = ""; String s133 = ""; String s134 = ""; String s135 = ""; String s136 = ""; String s137 = ""; String s138 = ""; String s139 = ""; String s140 = "";
				    String s141 = ""; String s142 = ""; String s143 = ""; String s144 = ""; String s145 = ""; String s146 = ""; String s147 = ""; String s148 = ""; String s149 = ""; String s150 = "";
				    String s151 = ""; String s152 = ""; String s153 = ""; String s154 = ""; String s155 = ""; String s156 = ""; String s157 = ""; String s158 = ""; String s159 = ""; String s160 = "";
				    String s161 = ""; String s162 = ""; String s163 = ""; String s164 = ""; String s165 = ""; String s166 = ""; String s167 = ""; String s168 = ""; String s169 = ""; String s170 = "";
				    String s171 = ""; String s172 = ""; String s173 = ""; String s174 = ""; String s175 = ""; String s176 = ""; String s177 = ""; String s178 = ""; String s179 = ""; String s180 = "";
				    String s181 = ""; String s182 = ""; String s183 = ""; String s184 = ""; String s185 = ""; String s186 = ""; String s187 = ""; String s188 = ""; String s189 = ""; String s190 = "";
				    String s191 = ""; String s192 = ""; String s193 = ""; String s194 = ""; String s195 = ""; String s196 = ""; String s197 = ""; String s198 = ""; String s199 = ""; String s200 = "";
				    String s201 = ""; String s202 = ""; String s203 = ""; String s204 = ""; String s205 = ""; String s206 = ""; String s207 = ""; String s208 = ""; String s209 = ""; String s210 = "";
				    String s211 = ""; String s212 = ""; String s213 = ""; String s214 = ""; String s215 = ""; String s216 = ""; String s217 = ""; String s218 = ""; String s219 = ""; String s220 = "";
				    String s221 = ""; String s222 = ""; String s223 = ""; String s224 = ""; String s225 = ""; String s226 = ""; String s227 = ""; String s228 = ""; String s229 = ""; String s230 = "";
				    String s231 = ""; String s232 = ""; String s233 = ""; String s234 = ""; String s235 = ""; String s236 = ""; String s237 = ""; String s238 = ""; String s239 = ""; String s240 = "";
				    String s241 = ""; String s242 = ""; String s243 = ""; String s244 = ""; String s245 = ""; String s246 = ""; String s247 = ""; String s248 = ""; String s249 = ""; String s250 = "";
				    String s251 = ""; String s252 = "";
				
				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;
				
				    short[][][] array = new short[size1][size2][size3];
				   \s
				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? (short) 1 : (short) 0;
				        }
				      }
				    }
				
				  }
				
				  public static void main(String[] args) {
				    new X().show();
				  }
				}""",
		},
		"",
		settings);
}
public static Class testClass() {
	return ForStatementTest.class;
}
}
