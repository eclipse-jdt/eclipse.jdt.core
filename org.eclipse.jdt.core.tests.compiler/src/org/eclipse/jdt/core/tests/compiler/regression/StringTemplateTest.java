/*******************************************************************************
 * Copyright (c) 2023, 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class StringTemplateTest extends AbstractRegressionTest9 {

	static {
//		TESTS_NAMES = new String[] { "test003" };
	}
	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 22");
	private static final String[] VMARGS = new String[] {"--enable-preview"};
	public static Class<?> testClass() {
		return StringTemplateTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_22);
	}
	public StringTemplateTest(String testName){
		super(testName);
	}
	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean previewFlag) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_22);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_22);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_22);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, previewFlag ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		return defaultOptions;
	}
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, null, VMARGS, new JavacTestOptions("-source 22 --enable-preview"));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE22Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, VMARGS, JAVAC_OPTIONS);
	}
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Map<String, String> customOptions = getCompilerOptions(true);
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}
	public void test001() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String s = STR."A simple String";
							    System.out.println(s);
							  }
							}"""
				},
				"A simple String");
	}
	public void test001a() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String s = STR."";
							    System.out.println(s);
							  }
							}"""
				},
				"");
	}
	public void test002() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String s = STR.\"""
							        A simple Text block\""";
							    System.out.println(s);
							  }
							}"""
				},
				"A simple Text block");
	}
	public void test002a() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String s = STR.\"""
							        \""";
							    System.out.println(s);
							  }
							}"""
				},
				"");
	}
	public void test003() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String firstName = "Bill";
							    String lastName = "Duck";
							    String fullName = STR."Name: \\{firstName} \\{lastName}";
							    System.out.println(fullName);
							  }
							}"""
				},
				"Name: Bill Duck");
	}
	public void test004() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    int x = 10, y = 20;
							    String s = STR."\\{x} + \\{y} = \\{x + y}";
							    System.out.println(s);
							  }
							}"""
				},
				"10 + 20 = 30");
	}
	public void test005() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String[] fruit = { "apples", "oranges", "peaches" };
							    String s = STR."\\{fruit[0]}, \\{STR."\\{fruit[1]}, \\{fruit[2]}"}\\u002e";
							    System.out.println(s);
							  }
							}"""
				},
				"apples, oranges, peaches.");
	}
	public void test006() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String[] fruit = { "apples", "oranges", "peaches" };
							    String tmp = STR."\\{fruit[1]}, \\{fruit[2]}";\
							    String s = STR."\\{fruit[0]}, \\{tmp}";\
							    System.out.println(s);
							  }
							}"""
				},
				"apples, oranges, peaches");
	}
	public void test007() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String[] fruit = { "apples", "oranges", "peaches" };
							    String tmp = STR."\\{fruit[1]}, \\{fruit[2]}";\
							    String s = STR."\\{fruit[0]}, \\{tmp}";\
							    System.out.println(s);
							  }
							}"""
				},
				"apples, oranges, peaches");
	}
	// Simple text block with embedded expressions
	public void test008() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								@SuppressWarnings("nls")
								public static void main(String[] args) {
									String name    = "Joan Smith";
									String phone   = "555-123-4567";
									String address = "1 Maple Drive, Anytown";
									String doc = STR.\"""
									    {
									        "name":    "\\{name}",
									        "phone":   "\\{phone}",
									        "address": "\\{address}"
									    };\""";
									System.out.println(doc);
								}
							}"""
				},
				"""
					{
					    "name":    "Joan Smith",
					    "phone":   "555-123-4567",
					    "address": "1 Maple Drive, Anytown"
					};""");
	}
	// Simple text block with a string literal as an embedded expression
	public void test008a() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X<R> {
							@SuppressWarnings("nls")
							public static void main(String[] args) {
							  String name    = "Joan Smith";
							  String phone   = "555-123-4567";
							  String address = "1 Maple Drive, Anytown";
							  String doc = STR.\"""
							    {
							        "name":    "\\{STR."\\{name}"}",
							        "phone":   "\\{phone}",
							        "address": "\\{address}"\s
							    };\""";
							  System.out.println(doc);
							  }  \s
							}\s"""
				},
				"""
					{
					    "name":    "Joan Smith",
					    "phone":   "555-123-4567",
					    "address": "1 Maple Drive, Anytown"
					};""");
	}
	// Simple text block with a nested text block as an embedded expression
	public void test008b() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X<R> {
							  @SuppressWarnings("nls")
							  public static void main(String[] args) {
							    String name    = "Joan Smith";
							    String phone   = "555-123-4567";
							    String address = "1 Maple Drive, Anytown";
							    String doc = STR.\"""
							      {
							          "name":    "\\{
							          STR.\"""
							            \\{name}\"""}",
							          "phone":   "\\{phone}",
							          "address": "\\{address}"\s
							      };\""";
							    System.out.println(doc);
							  }  \s
							}\s"""
				},
				"""
					{
					    "name":    "Joan Smith",
					    "phone":   "555-123-4567",
					    "address": "1 Maple Drive, Anytown"
					};""");
	}
	// Same as above, but the nested text block has a smaller indentation than the outer
	// But this should not influence the formatting of the outer block
	public void test008c() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X<R> {
							  @SuppressWarnings("nls")
							  public static void main(String[] args) {
							    String name    = "Joan Smith";
							    String phone   = "555-123-4567";
							    String address = "1 Maple Drive, Anytown";
							    String doc = STR.\"""
							    {
							        "name":    "\\{
							      STR.\"""
							\\{name}\"""}",
							        "phone":   "\\{phone}",
							        "address": "\\{address}"\s
							    };\""";
							    System.out.println(doc);
							  }  \s
							}\s"""
				},
				"""
					{
					    "name":    "Joan Smith",
					    "phone":   "555-123-4567",
					    "address": "1 Maple Drive, Anytown"
					};""");
	}
	public void test009() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String last = "Duck";
							    foo(STR."\\{first} \\{last}");
							  }
							  public static void foo(String s) {
							    System.out.println(s);
							  }
							}"""
				},
				"Bill Duck");
	}
	public void test010() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String last = "Duck";
							    String s = STR."\\{first}" + " " + STR."\\{last}";
							    System.out.println(s);
							  }
							}"""
				},
				"Bill Duck");
	}
	public void test011() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String last = "Duck";
							    foo(STR."\\{first}" + " " + STR."\\{last}");
							  }
							  public static void foo(String s) {
							    System.out.println(s);
							  }
							}"""
				},
				"Bill Duck");
	}
	// Template Expresion with an empty string fragment
	public void test012() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String s = STR."\\{first + STR.""}";
							    System.out.println(s);
							  }
							}"""
				},
				"Bill");
	}
	// Template Expression with an empty embedded expression
	public void test013() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String s = STR."\\{}";
							    System.out.println(s);
							  }
							}"""
				},
				"null");
	}
	// Template Expression with an null literal as expression
	public void test013a() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String s = STR."\\{null}";
							    System.out.println(s);
							  }
							}"""
				},
				"null");
	}
	public void test014() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String s = STR."\\{STR."\\{STR."abc"}"}";
							    System.out.println(s);
							  }
							}"""
				},
				"abc");
	}
	public void test015() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String s = "Name is \\{first}";
							    System.out.println(s);
							  }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						String s = "Name is \\{first}";
						           ^^^^^^^^^^^^^^^^^^
					Syntax error on token "StringTemplate", invalid Expression
					----------
					""");
	}
	public void test015a() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String s = \"""
							Name is \\{first}\""";
							    System.out.println(s);
							  }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						String s = \"""
					Name is \\{first}\""";
						           ^^^^^^^^^^^^^^^^^^^^^^^
					Syntax error on token "TextBlockTemplate", invalid Expression
					----------
					""");
	}
	public void test016() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String s = \"""
							      \\{first}
							    \"""
							    System.out.println(s);
							  }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						String s = \"""
					      \\{first}
					    \"""
						           ^^^^^^^^^^^^^^^^^^^^^^^^^^
					Syntax error on token "TextBlockTemplate", delete this token
					----------
					""");
	}
	public void test017() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String s = STR1.\"""
											 \\{first}\s
											\""";
							    System.out.println(s);
							  }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						String s = STR1.\"""
						           ^^^^
					STR1 cannot be resolved
					----------
					""");
	}
	public void test018() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  static final StringTemplate.Processor<String, RuntimeException> PROC = null;
							  public static void main(String[] args) {
							    String first = "Bill";
							    boolean isNull = false;
							    try {
							      String s = PROC."\\{first}";
							      } catch (NullPointerException e) {
							        isNull = true;
							      }\
							      System.out.println(isNull);
							  }
							}"""
				},
				"true");
	}
	public void test019() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							import static java.lang.StringTemplate.STR;
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String s = STR."\\{first}";
							    System.out.println(s1);
							  }
							}"""
				},
				"""
					----------
					1. WARNING in X.java (at line 1)
						import static java.lang.StringTemplate.STR;
						              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The import java.lang.StringTemplate.STR is never used
					----------
					2. ERROR in X.java (at line 6)
						System.out.println(s1);
						                   ^^
					s1 cannot be resolved to a variable
					----------
					""");
	}
	public void test020() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.StringTemplate.STR;
							public class X {
							  public static void main(String[] args) {
							    String first = "Bill";
							    String s = STR."\\{first}";
							    System.out.println(s);
							  }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						import java.lang.StringTemplate.STR;
						       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The import java.lang.StringTemplate.STR cannot be resolved
					----------
					""");
	}
	// Tests that use a non-name expression for processors
	public void test021() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X  {
							  public static void main(String argv[]) {
							    int i1 = 1; int i2 = 2; int i3 = 3; int i4 = 4; int i5 = 5; int i6 = 6; int i7 = 7; int i8 = 8; int i9 = 9; int i10 = 10; int i11 = 11; int i12 = 12; int i13 = 13; int i14 = 14; int i15 = 15; int i16 = 16; int i17 = 17; int i18 = 18; int i19 = 19; int i20 = 20; int i21 = 21; int i22 = 22; int i23 = 23; int i24 = 24; int i25 = 25; int i26 = 26; int i27 = 27; int i28 = 28; int i29 = 29; int i30 = 30; int i31 = 31; int i32 = 32; int i33 = 33; int i34 = 34; int i35 = 35; int i36 = 36; int i37 = 37; int i38 = 38; int i39 = 39; int i40 = 40; int i41 = 41; int i42 = 42; int i43 = 43; int i44 = 44; int i45 = 45; int i46 = 46; int i47 = 47; int i48 = 48; int i49 = 49; int i50 = 50; int i51 = 51; int i52 = 52; int i53 = 53; int i54 = 54; int i55 = 55; int i56 = 56; int i57 = 57; int i58 = 58; int i59 = 59; int i60 = 60; int i61 = 61; int i62 = 62; int i63 = 63; int i64 = 64; int i65 = 65; int i66 = 66; int i67 = 67; int i68 = 68; int i69 = 69; int i70 = 70; int i71 = 71; int i72 = 72; int i73 = 73; int i74 = 74; int i75 = 75; int i76 = 76; int i77 = 77; int i78 = 78; int i79 = 79; int i80 = 80; int i81 = 81; int i82 = 82; int i83 = 83; int i84 = 84; int i85 = 85; int i86 = 86; int i87 = 87; int i88 = 88; int i89 = 89; int i90 = 90; int i91 = 91; int i92 = 92; int i93 = 93; int i94 = 94; int i95 = 95; int i96 = 96; int i97 = 97; int i98 = 98; int i99 = 99; int i100 = 100;\s
							    String s = (new MyProcessor())."s1\\{i1}s2\\{i2}s3\\{i3}s4\\{i4}s5\\{i5}s6\\{i6}s7\\{i7}s8\\{i8}s9\\{i9}s10\\{i10}s11\\{i11}s12\\{i12}s13\\{i13}s14\\{i14}s15\\{i15}s16\\{i16}s17\\{i17}s18\\{i18}s19\\{i19}s20\\{i20}s21\\{i21}s22\\{i22}s23\\{i23}s24\\{i24}s25\\{i25}s26\\{i26}s27\\{i27}s28\\{i28}s29\\{i29}s30\\{i30}s31\\{i31}s32\\{i32}s33\\{i33}s34\\{i34}s35\\{i35}s36\\{i36}s37\\{i37}s38\\{i38}s39\\{i39}s40\\{i40}s41\\{i41}s42\\{i42}s43\\{i43}s44\\{i44}s45\\{i45}s46\\{i46}s47\\{i47}s48\\{i48}s49\\{i49}s50\\{i50}s51\\{i51}s52\\{i52}s53\\{i53}s54\\{i54}s55\\{i55}s56\\{i56}s57\\{i57}s58\\{i58}s59\\{i59}s60\\{i60}s61\\{i61}s62\\{i62}s63\\{i63}s64\\{i64}s65\\{i65}s66\\{i66}s67\\{i67}s68\\{i68}s69\\{i69}s70\\{i70}s71\\{i71}s72\\{i72}s73\\{i73}s74\\{i74}s75\\{i75}s76\\{i76}s77\\{i77}s78\\{i78}s79\\{i79}s80\\{i80}s81\\{i81}s82\\{i82}s83\\{i83}s84\\{i84}s85\\{i85}s86\\{i86}s87\\{i87}s88\\{i88}s89\\{i89}s90\\{i90}s91\\{i91}s92\\{i92}s93\\{i93}s94\\{i94}s95\\{i95}s96\\{i96}s97\\{i97}s98\\{i98}s99\\{i99}s100\\{i100}s101";
							    System.out.println(s.equals("\\"s11s22s33s44s55s66s77s88s99s1010s1111s1212s1313s1414s1515s1616s1717s1818s1919s2020s2121s2222s2323s2424s2525s2626s2727s2828s2929s3030s3131s3232s3333s3434s3535s3636s3737s3838s3939s4040s4141s4242s4343s4444s4545s4646s4747s4848s4949s5050s5151s5252s5353s5454s5555s5656s5757s5858s5959s6060s6161s6262s6363s6464s6565s6666s6767s6868s6969s7070s7171s7272s7373s7474s7575s7676s7777s7878s7979s8080s8181s8282s8383s8484s8585s8686s8787s8888s8989s9090s9191s9292s9393s9494s9595s9696s9797s9898s9999s100100s101\\""));
							  }
							}
							class MyProcessor implements StringTemplate.Processor<String, RuntimeException>  {
							  public String process(StringTemplate st) {
							    return "\\"" + STR.process(st) + "\\"";
							  };
							}"""
				},
				"true");
	}
	public void test021a() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String argv[]) {
							      String s = getProc()."\\{"pass"}";
							      System.out.println(s);
							  }
							  public static StringTemplate.Processor<String, RuntimeException> getProc() {
							      return StringTemplate.STR;
							  }
							}"""
				},
				"pass");
	}
	public void test022() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String argv[]) {
							    int i1 = 1; int i2 = 2; int i3 = 3; int i4 = 4; int i5 = 5; int i6 = 6; int i7 = 7; int i8 = 8; int i9 = 9; int i10 = 10; int i11 = 11; int i12 = 12; int i13 = 13; int i14 = 14; int i15 = 15; int i16 = 16; int i17 = 17; int i18 = 18; int i19 = 19; int i20 = 20; int i21 = 21; int i22 = 22; int i23 = 23; int i24 = 24; int i25 = 25; int i26 = 26; int i27 = 27; int i28 = 28; int i29 = 29; int i30 = 30; int i31 = 31; int i32 = 32; int i33 = 33; int i34 = 34; int i35 = 35; int i36 = 36; int i37 = 37; int i38 = 38; int i39 = 39; int i40 = 40; int i41 = 41; int i42 = 42; int i43 = 43; int i44 = 44; int i45 = 45; int i46 = 46; int i47 = 47; int i48 = 48; int i49 = 49; int i50 = 50; int i51 = 51; int i52 = 52; int i53 = 53; int i54 = 54; int i55 = 55; int i56 = 56; int i57 = 57; int i58 = 58; int i59 = 59; int i60 = 60; int i61 = 61; int i62 = 62; int i63 = 63; int i64 = 64; int i65 = 65; int i66 = 66; int i67 = 67; int i68 = 68; int i69 = 69; int i70 = 70; int i71 = 71; int i72 = 72; int i73 = 73; int i74 = 74; int i75 = 75; int i76 = 76; int i77 = 77; int i78 = 78; int i79 = 79; int i80 = 80; int i81 = 81; int i82 = 82; int i83 = 83; int i84 = 84; int i85 = 85; int i86 = 86; int i87 = 87; int i88 = 88; int i89 = 89; int i90 = 90; int i91 = 91; int i92 = 92; int i93 = 93; int i94 = 94; int i95 = 95; int i96 = 96; int i97 = 97; int i98 = 98; int i99 = 99; int i100 = 100;\s
							    MyProcessor tProcessor = new MyProcessor();
							    String s = tProcessor."s1\\{i1}s2\\{i2}s3\\{i3}s4\\{i4}s5\\{i5}s6\\{i6}s7\\{i7}s8\\{i8}s9\\{i9}s10\\{i10}s11\\{i11}s12\\{i12}s13\\{i13}s14\\{i14}s15\\{i15}s16\\{i16}s17\\{i17}s18\\{i18}s19\\{i19}s20\\{i20}s21\\{i21}s22\\{i22}s23\\{i23}s24\\{i24}s25\\{i25}s26\\{i26}s27\\{i27}s28\\{i28}s29\\{i29}s30\\{i30}s31\\{i31}s32\\{i32}s33\\{i33}s34\\{i34}s35\\{i35}s36\\{i36}s37\\{i37}s38\\{i38}s39\\{i39}s40\\{i40}s41\\{i41}s42\\{i42}s43\\{i43}s44\\{i44}s45\\{i45}s46\\{i46}s47\\{i47}s48\\{i48}s49\\{i49}s50\\{i50}s51\\{i51}s52\\{i52}s53\\{i53}s54\\{i54}s55\\{i55}s56\\{i56}s57\\{i57}s58\\{i58}s59\\{i59}s60\\{i60}s61\\{i61}s62\\{i62}s63\\{i63}s64\\{i64}s65\\{i65}s66\\{i66}s67\\{i67}s68\\{i68}s69\\{i69}s70\\{i70}s71\\{i71}s72\\{i72}s73\\{i73}s74\\{i74}s75\\{i75}s76\\{i76}s77\\{i77}s78\\{i78}s79\\{i79}s80\\{i80}s81\\{i81}s82\\{i82}s83\\{i83}s84\\{i84}s85\\{i85}s86\\{i86}s87\\{i87}s88\\{i88}s89\\{i89}s90\\{i90}s91\\{i91}s92\\{i92}s93\\{i93}s94\\{i94}s95\\{i95}s96\\{i96}s97\\{i97}s98\\{i98}s99\\{i99}s100\\{i100}s101";
							    System.out.println(s.equals("\\"s11s22s33s44s55s66s77s88s99s1010s1111s1212s1313s1414s1515s1616s1717s1818s1919s2020s2121s2222s2323s2424s2525s2626s2727s2828s2929s3030s3131s3232s3333s3434s3535s3636s3737s3838s3939s4040s4141s4242s4343s4444s4545s4646s4747s4848s4949s5050s5151s5252s5353s5454s5555s5656s5757s5858s5959s6060s6161s6262s6363s6464s6565s6666s6767s6868s6969s7070s7171s7272s7373s7474s7575s7676s7777s7878s7979s8080s8181s8282s8383s8484s8585s8686s8787s8888s8989s9090s9191s9292s9393s9494s9595s9696s9797s9898s9999s100100s101\\""));
							  }
							}
							class MyProcessor implements StringTemplate.Processor<String, RuntimeException> {
							  public String process(StringTemplate st) {
							    return "\\"" + STR.process(st) + "\\"";
							  };
							}"""
				},
				"true");
	}
	public void test023() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String s = STR."\\{STR."\\{STR."}abc{"}"}";
							    System.out.println(s);
							  }
							}"""
				},
				"}abc{");
	}
	public void test024() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args) {
							    String s = STR."\\{STR."}\\{STR."}abc{"}"}{";
							    System.out.println(s);
							  }
							}"""
				},
				"}}abc{{");
	}
	// String template
	// embedded expression contains an instantiation and method invocation
	// closing } is unicode
	public void test025() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    static String value1 = "1";
							    private String getValue() {
							        return value1;
							    }
							
							    public static void main(String argv[]) {
							        String str = STR."{}\\{(new X()).getValue()\\u007d";
							        System.out.println(str.equals("{}1"));
							    }
							}"""
				},
				"true");
	}
	// Same as above, but the invoked metod is absent
	public void test025a() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    static String value1 = "1";
							    public static void main(String argv[]) {
							        String str = STR."{}\\{(new X()).getValue()\\u007d";
							        System.out.println(str.equals("{}1"));
							    }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						String str = STR."{}\\{(new X()).getValue()\\u007d";
						                                ^^^^^^^^
					The method getValue() is undefined for the type X
					----------
					""");
	}
	// Text block template
	// embedded expression contains an instantiation and method invocation
	// closing } is unicode
	public void test026() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    static String value1 = "1";
							    private String getValue() {
							        return value1;
							    }
							    public static void main(String argv[]) {
							        String str = STR.\"""
										{}\\{(new X()).getValue()\\u007d\""";
							        System.out.println(str.equals("{}1"));
							    }
							}"""
				},
				"true");
	}
	public void test026a() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    static String value1 = "1";
							    private String getValue() {
							        return value1;
							    }
							    public static void main(String argv[]) {
							        String str = STR.\"""
										{}\\{(new X()).getValue()}\""";
							        System.out.println(str.equals("{}1"));
							    }
							}"""
				},
				"true");
	}
	// Same as above, but the invoked method is absent
	public void test026b() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    static String value1 = "1";
							    public static void main(String argv[]) {
							        String str = STR.\"""
										{}\\{(new X()).getValue()\\u007d\""";
							        System.out.println(str.equals("{}1"));
							    }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						{}\\{(new X()).getValue()\\u007d\""";
						              ^^^^^^^^
					The method getValue() is undefined for the type X
					----------
					""");
	}
	// Flow analysis related tests
	public void test027() {
		Map<String,String> options = getCompilerOptions(true);
		String old1 = options.get(CompilerOptions.OPTION_ReportUnusedLocal);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
		try {
			runConformTest(
					new String[] {
							"X.java",
							"""
								public class X {
								  public static void main(String[] args) {
								    String abc = "abc";
								    String s = STR."A simple String: \\{abc}";
								    System.out.println(s);
								  }
								}"""
					},
					"A simple String: abc",
					options);
		} finally {
			options.put(CompilerOptions.OPTION_ReportUnusedLocal, old1);
		}
	}
	public void test027a() {
		Map<String,String> options = getCompilerOptions(true);
		String old1 = options.get(CompilerOptions.OPTION_ReportUnusedLocal);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
		try {
			runNegativeTest(
					new String[] {
							"X.java",
							"""
								public class X {
								  public static void main(String[] args) {
								    String abc = "abc"; // unused
								    String def = "def";
								    String s = STR."A simple String \\{def}";
								    System.out.println(s);
								  }
								}"""
					},
					"""
						----------
						1. ERROR in X.java (at line 3)
							String abc = "abc"; // unused
							       ^^^
						The value of the local variable abc is not used
						----------
						""",
					null,
					false,
					options);
		} finally {
			options.put(CompilerOptions.OPTION_ReportUnusedLocal, old1);
		}
	}
	public void test028() {
		Map<String,String> options = getCompilerOptions(true);
		String old1 = options.get(CompilerOptions.OPTION_ReportUnusedPrivateMember);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
		try {
			runNegativeTest(
					new String[] {
							"X.java",
							"""
								public class X {
								  private String abc = "abc"; // unused
								  private String def = "def"; // unused
								  public void main(String[] args) {
								    String s = STR."A simple String \\{def}";
								    System.out.println(s);
								  }
								}"""
					},
					"""
						----------
						1. ERROR in X.java (at line 2)
							private String abc = "abc"; // unused
							               ^^^
						The value of the field X.abc is not used
						----------
						""",
					null,
					false,
					options);
		} finally {
			options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, old1);
		}
	}
	public void test028a() {
		Map<String,String> options = getCompilerOptions(true);
		String old1 = options.get(CompilerOptions.OPTION_ReportUnusedPrivateMember);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
		try {
			runNegativeTest(
					new String[] {
							"X.java",
							"""
								public class X {
								  private String abc = "abc"; // unused
								  private String def = "def"; // unused
								  public void main(String[] args) {
								    String s = STR."A simple String \\{clone(def)}";
								    System.out.println(s);
								  }
								  public String clone(String s) {
								    return s;
								  }
								}"""
					},
					"""
						----------
						1. ERROR in X.java (at line 2)
							private String abc = "abc"; // unused
							               ^^^
						The value of the field X.abc is not used
						----------
						""",
					null,
					false,
					options);
		} finally {
			options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, old1);
		}
	}
	public void test028b() {
		Map<String,String> options = getCompilerOptions(true);
		String old1 = options.get(CompilerOptions.OPTION_ReportUnusedPrivateMember);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
		try {
			runConformTest(
					new String[] {
							"X.java",
							"""
								public class X {
								  private String abc = "abc"; // unused
								  public void main(String[] args) {
								    String s = STR."A simple String \\{clone(abc)}";
								    System.out.println(s);
								  }
								  public String clone(String s) {
								    return "clone";
								  }
								}"""
					},
					"A simple String clone",
					options);
		} finally {
			options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, old1);
		}
	}
	public void test029() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  static int CONST = 0;
							    private static int foo() {
							    return CONST;
							  }
							  public static void main(String argv[]) {
							    String str = STR."{\\{new Object() { class Test { int i; Test() { i = foo();}}}.new Test().i\\u007d}";
							    System.out.println(str.equals("{0}"));
							  }
							}"""
				},
				"true");
	}
	// Same as above with a text block
	public void test030() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  static int CONST = 0;
							    private static int foo() {
							    return CONST;
							  }
							  public static void main(String argv[]) {
							    String str = STR.\"""
							      {\\{new Object() { class Test { int i; Test() { i = foo();}}}.new Test().i\\u007d}\""";
							    System.out.println(str.equals("{0}"));
							  }
							}"""
				},
				"true");
	}
	public void test031() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    public static void main(String argv[]) {
							        String abc;
							        String temp = STR."\\{abc = "abc"}\\{abc}";
							        System.out.println(temp);
							    }
							}"""
				},
				"abcabc");
	}
	public void test032() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    public static void main(String argv[]) {
							        String abc;
							        String temp = STR."\\{}\\{abc}";
							        System.out.println(temp);
							    }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						String temp = STR."\\{}\\{abc}";
						                        ^^^
					The local variable abc may not have been initialized
					----------
					""");
	}
	// Test that newlines and such are allowed in strings if they are inside embedded expressions
	public void test033() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
    public boolean compare(String s1, String s2) {
        return s1.equals(s2);
    }
    public static void main(String args[]) {
        X t = new X();
        System.out.println(t.compare(STR."\\{ 10 +
        10/10 +
        args.length }", "11"));
    }
}"""
				},
				"true");
	}
	// Test that leading whitespace in the line preceding expressions
	// are removed
	public void test034() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
    private final static int LF  = (char) 0x000A;
    private static boolean compare(String s) {
        char[] chars = new char[] {LF,'a','b','c','d'};
        if (chars.length != s.length())
            return false;
        for (int i = 0; i < chars.length; i++) {
            if(chars[i] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    public static void main(String argv[]) {
        String str = \"abcd\"; //$NON-NLS-1$
        String textBlock = STR.\"""
\s\s\s
\\{str}\""";//$NON-NLS-1$
        System.out.println(compare(textBlock));
    }
}"""
				},
				"true");
	}
	// Test that leading whitespace in the line preceding expressions
	// are removed
	// Using regular strings instead of text blocks for testcase because
	// the formatter removes the whitespaces
	public void test035() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    private final static int LF  = (char) 0x000A;
							    private static boolean compare(String s) {
							        char[] chars = new char[] {LF,'a','b','c','d'};
							        if (chars.length != s.length())
							            return false;
							        for (int i = 0; i < s.length(); i++) {
							            if(chars[i] != s.charAt(i)) {
							                return false;
							            }
							        }
							        return true;
							    }
							    public static void main(String argv[]) {
							        String abcd = "abcd"; //$NON-NLS-1$
							        String textBlock = STR.\"""
							  \s
							\\{abcd}\""";//$NON-NLS-1$
							        System.out.println(compare(textBlock));
							    }
							}"""
				},
				"true");
	}
	// Test that leading whitespace in the line preceding expressions
	// are removed
	// Using regular strings instead of text blocks for testcase because of a bug
	// that doesn't allow the sequence \40\40\40 at the end of a line
	public void test036() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    private final static int LF  = (char) 0x000A;
							    private static boolean compare(String s) {
							        char[] chars = new char[] {' ', ' ', ' ', LF, 'a','b','c','d'};
							        if (chars.length != s.length())
							            return false;
							        for (int i = 0; i < s.length(); i++) {
							            if(chars[i] != s.charAt(i)) {
							                return false;
							            }
							        }
							        return true;
							    }
							    public static void main(String argv[]) {
							        String abcd = "abcd"; //$NON-NLS-1$
							        String textBlock = STR.\"""
							\\40\\40\\40
							\\{abcd}\""";//$NON-NLS-1$
							        System.out.println(compare(textBlock));
							    }
							}"""
				},
				"true");
	}
	// Same as above, but with a simple text block
	public void test037() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    private final static int LF  = (char) 0x000A;
							    private static boolean compare(String s) {
							        char[] chars = new char[] {' ', ' ', ' ', LF, 'a','b','c','d'};
							        if (chars.length != s.length())
							            return false;
							        for (int i = 0; i < s.length(); i++) {
							            if(chars[i] != s.charAt(i)) {
							                return false;
							            }
							        }
							        return true;
							    }
							    public static void main(String argv[]) {
							        String abcd = "abcd"; //$NON-NLS-1$
							        String textBlock = \"""
							\\40\\40\\40
							abcd\""";//$NON-NLS-1$
							        System.out.println(compare(textBlock));
							    }
							}"""
				},
				"true");
	}
	public void test038() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
    private static boolean compare(String s) {
        return s.equals(\"""
abc def
ghij.\""");
    }
    public static void main(String args[]) {
        String text = \"""
\
abc \
def
ghij\
.\""";

        System.out.println(compare(text));
    }
}"""
				},
				"true");
	}
	public void test039() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
    public boolean compare() {
        String val = "Test";
        String str = STR.\"""
         Color  Shape
              Red    Circle
              Green  Square
              Blue   Triangle One \\{val} Two \\{val} One "String" \""";
        return (str.equals(\"""
         Color  Shape
              Red    Circle
              Green  Square
              Blue   Triangle One Test Two Test One "String" \"""));
    }
    public static void main(String argv[]) {
        X t = new X();
        System.out.println(t.compare());
    }
}"""
				},
				"true");
	}
	public void test040() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
    public static void main(String argv[]) {
String s = STR.\"""
  xyz
  \\{
\"""
   abc
  def\"""}123
    \""";
      System.out.println(s);
    }
}"""
				},
				"""
					xyz
					 abc
					def123""");
	}
	public void test041() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
    public static void main(String argv[]) {
String s = STR.\"""
  xyz
  \\{
\"""
   abc
   def\"""}123
    \""";
      System.out.println(s);
    }
}"""
				},
				"""
					xyz
					abc
					def123""");
	}
	public void test0041() {
		runConformTest(new String[]{
						"X.java",
						"""
public class X {
  static final String s = STR.\"""
    1
    \\{
\"""
    2
    3
    4
    5\"""}
    \""";
      public static void main(String[] args)  {
        try {
          throw new RuntimeException(\"This is line 13.\");
        } catch(Exception e) {
          e.printStackTrace(System.out);
        }
      }
}"""
				},
				"java.lang.RuntimeException: This is line 13.\n" +
				"	at X.main(X.java:13)");
	}
	public void test042() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
    private static boolean compare(String textBlock) {
        return textBlock.equals("abc def.");
    }
    public static void main(String argv[]) {
        String textBlock = \"""
\
abc \\
def\\
.\""";
        System.out.println(compare(textBlock));
    }
}"""
				},
				"true",
				getCompilerOptions());
	}
	public void test043() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
  static final String s = STR.\"""
    1\\u005c{
"." +
    2 +
    3 +
    4 +
    "5"}\""";
      public static void main(String[] args)  {
       System.out.println(s.equals("1.2345"));
      }
}"""
				},
				"true",
				getCompilerOptions());
	}
	// 3 levels of nested test blocks with string templates and a method invocation
	public void test044() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
    public static void main(String argv[]) {
String s = STR.\"""
  \\{
  STR.\"""
\\{
    clone(\"""
        abcdefg
        \""")
}\"""

  }\""";
      System.out.println(s.equals("clone"));
    }
    public static String clone(String s) {
        return "clone"; //$NON-NLS-1$
    }
}
"""
				},
				"true",
				getCompilerOptions());
	}
	// Tests with RAW template processor
	public void test045() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  public static void main(String argv[]) {
				      String name = "Jay";
				      StringTemplate template = StringTemplate.RAW."\\{name}";
				      String s = StringTemplate.STR.process(template);
				      System.out.println(s);
				  }
				  public static StringTemplate.Processor<String, RuntimeException> getProc() {
				      return StringTemplate.STR;
				  }
				}
"""
			},
			"Jay"
);
	}
	public void test046() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  public static void main(String argv[]) {
				      String name = "Jay";
				      String s = StringTemplate.STR.process(StringTemplate.RAW."\\{name}");
				      System.out.println(s);
				  }
				  public static StringTemplate.Processor<String, RuntimeException> getProc() {
				      return StringTemplate.STR;
				  }
				}
"""
			},
			"Jay"
);
	}
	public void test047() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  public static void main(String argv[]) {
				      String name = "Jay";
				      String template = StringTemplate.RAW."\\{name}";
				  }
				  public static StringTemplate.Processor<String, RuntimeException> getProc() {
				      return StringTemplate.STR;
				  }
				}
"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					String template = StringTemplate.RAW."\\{name}";
					                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from StringTemplate to String
				----------
				"""
);
	}
	// pass string template with embedded expression as argument to a method invocation
	public void test048() {
		runConformTest(
			new String[] {
				"X.java",
				"""
public class X {
  public static void main(String argv[]) {
      String name = "Jay";
      System.out.println(stringify(StringTemplate.RAW."\\{name}"));
  }
  public static String stringify(StringTemplate template) {
      System.out.println(template.toString());
      return STR.process(template);
  }
}
"""
			},
			"StringTemplate{ fragments = [ \"\", \"\" ], values = [Jay] }\n" +
			"Jay"
);
	}
	public void test049() {
		runConformTest(
				new String[] {
					"X.java",
					"""
public class X {
    public static void main(String argv[]) {
      String str = "\\0\\1\\2\\3\\4\\5\\6\\7\\10\\11\\12\\13\\14\\15\\16\\17\\20\\21\\22\\23\\24\\25\\26\\27\\30\\31\\32\\33\\34\\35\\36\\37\\40\\41\\42\\43\\44\\45\\46\\47\\50\\51\\52\\53\\54\\55\\56\\57\\60\\61\\62\\63\\64\\65\\66\\67\\70\\71\\72\\73\\74\\75\\76\\77\\100\\101\\102\\103\\104\\105\\106\\107\\110\\111\\112\\113\\114\\115\\116\\117\\120\\121\\122\\123\\124\\125\\126\\127\\130\\131\\132\\133\\134\\135\\136\\137\\140\\141\\142\\143\\144\\145\\146\\147\\150\\151\\152\\153\\154\\155\\156\\157\\160\\161\\162\\163\\164\\165\\166\\167\\170\\171\\172\\173\\174\\175\\176\\177\\200\\201\\202\\203\\204\\205\\206\\207\\210\\211\\212\\213\\214\\215\\216\\217\\220\\221\\222\\223\\224\\225\\226\\227\\230\\231\\232\\233\\234\\235\\236\\237\\240\\241\\242\\243\\244\\245\\246\\247\\250\\251\\252\\253\\254\\255\\256\\257\\260\\261\\262\\263\\264\\265\\266\\267\\270\\271\\272\\273\\274\\275\\276\\277\\300\\301\\302\\303\\304\\305\\306\\307\\310\\311\\312\\313\\314\\315\\316\\317\\320\\321\\322\\323\\324\\325\\326\\327\\330\\331\\332\\333\\334\\335\\336\\337\\340\\341\\342\\343\\344\\345\\346\\347\\350\\351\\352\\353\\354\\355\\356\\357\\360\\361\\362\\363\\364\\365\\366\\367\\370\\371\\372\\373\\374\\375\\376\\377";
      System.out.println(str.length());
      for (int i=0; i<=0xFF; i++) {
        if (i != (int)str.charAt(i)) {
          System.out.println("Error in octal escape :" + i);
        }
      }
    }
}"""
				},
				"256");
	}
	// Test String template with backslash and { in unicode form
	public void test050() {
		runConformTest(
				new String[] {
					"X.java",
					"""
	public class X {
	    public static void main(String argv[]) {
	        String name = "Jay";
	        String s1 = STR.\"Hello \\u005c\\u007bname}\\{"!"}";
	       System.out.println(s1);
	      }
	}
	"""
				},
				"Hello Jay!"
		);
	}
	//Test text block template with backslash and { in unicode form
	public void test051() {
		runConformTest(
			new String[] {
				"X.java",
				"""
public class X {
    public static void main(String argv[]) {
        String name = "Jay";
        String s1 = STR.\"""
            Hello \\u005c\\u007bname}\\{"!"}
\""";
       System.out.println(s1);
      }
}
"""
			},
			"Hello Jay!"
	);
	}
	//Same as above, but check for correct positioning of expressions
	public void test052() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
public class X {
    public static void main(String argv[]) {
        String name = "Jay";
        String s1 = STR.\"""
            Hello \\u005c\\u007bnames}\\{"!"}
\""";
       System.out.println(s1);
      }
}
"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					Hello \\u005c\\u007bnames}\\{"!"}
					                  ^^^^^
				names cannot be resolved to a variable
				----------
				"""
	);
	}
	public void test053() {
		runConformTest(
			new String[] {
				"X.java",
				"""
public class X {
    public static void main(String argv[]) {
        String name = "Jay";
        String s1 = STR.\"""
            Hello Jay!\""";
       System.out.println(s1);
      }
}
"""
			},
			"Hello Jay!"
	);
	}
	public void test0054() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
	public class X {
	    public static void main(String argv[]) {
        String s;
        String s1 = STR.\"""
Hello \\{
s
}
\\{
s
}

!\""";
        String s2 = "" + s + "";
        System.out.println(s1);
	      }
	}
"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						s
						^
					The local variable s may not have been initialized
					----------
					2. ERROR in X.java (at line 9)
						s
						^
					The local variable s may not have been initialized
					----------
					3. ERROR in X.java (at line 13)
						String s2 = "" + s + "";
						                 ^
					The local variable s may not have been initialized
					----------
					"""
		);
	}
	public void test0055() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
					public class X {
						public static void main(String argv[]) {
					        String s;
					        String s1 = STR.\"""
					Hello \\{
					s
					}
					\\{
					s="Jay"
					}

					!\""";
					        String s2 = "" + s + "";
					        System.out.println(s1);
						      }
						}
					"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						s
						^
					The local variable s may not have been initialized
					----------
					"""
		);
	}
	public void test0056() {
		runConformTest(
				new String[] {
					"X.java",
					"""
	public class X {
	    public static void main(String argv[]) {
        String s = "Jay";
        String s1 = STR.\"""
						Hello\\{
						s=""
						} \\{
						s="Jay"}!\""";
						        String s2 = "" + s + "";
						        System.out.println(s1);
							      }
							}
						"""
				},
				"Hello Jay!"
		);
	}
	public void test0057() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
	public class X {
	    public static void main(String argv[]) {
        String s = "Jay";
        String s1 = STR.\"""
						Hello \\{
						s="Jay";
						}
						\\{
						s="Jay"
						}!\""";
						        String s2 = "" + s + "";
						        System.out.println(s1);
							      }
							}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						s="Jay";
						       ^
					Syntax error on token ";", delete this token
					----------
					"""
		);
	}
	// Test with assignment of the outer string literal inside the template expression
	public void test0058() {
		runConformTest(
				new String[] {
					"X.java",
					"""
	public class X {
	    public static void main(String argv[]) {
        String first = "Jay";
        String last = "A";
        String greet = STR."Hello \\{
                greet = "Changed"
                } \\{last}!";
        System.out.println(greet);
						      }
						}
					"""
				},
				"Hello Changed A!"
		);
	}
	public void test0059() {
		runConformTest(
				new String[] {
					"X.java",
					"""
	public class X {
	    public static void main(String argv[]) {
        String first = "Jay";
        String last = "A";
        String greet = STR."Hello \\{
                greet = "Changed"
                } \\{greet}!";
        System.out.println(greet);
						      }
						}
					"""
				},
				"Hello Changed Changed!"
		);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2125
	// [String Templates] Compilation error when Template Processor used in unconditional for loop
	public void testIssue2125() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					import java.lang.StringTemplate.Processor;

					public class X implements Processor<String, RuntimeException> {
					  public static void main(String [] args) {
					    for(;;) {
					      System.out.println(new X()."SELECT * FROM");
					      break;
					    }
					  }

					  @Override
					  public String process(StringTemplate stringTemplate) throws RuntimeException {
					    return STR.process(stringTemplate);
					  }
					}
					"""
				},
				"SELECT * FROM"
		);
	}
	public void testIssue2125_2() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					import java.lang.StringTemplate.Processor;

					public class X implements Processor<String, RuntimeException> {
					  public static void main(String [] args) {
					    for (int i = 0; i < 3; i++) {
					      System.out.println(new X()."SELECT * FROM");
					    }
					  }

					  @Override
					  public String process(StringTemplate stringTemplate) throws RuntimeException {
					    return STR.process(stringTemplate);
					  }
					}
					"""
				},
				"""
					SELECT * FROM
					SELECT * FROM
					SELECT * FROM"""
		);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2125
	// [String Templates] Compilation error when Template Processor used in unconditional for loop
	public void testIssue2125_3() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					import java.lang.StringTemplate.Processor;

					public class X implements Processor<String, RuntimeException> {
					  public static void main(String [] args) {
					    for(;;) {
					      System.out.println(STR."SELECT * FROM");
					      break;
					    }
					  }

					  @Override
					  public String process(StringTemplate stringTemplate) throws RuntimeException {
					    return STR.process(stringTemplate);
					  }
					}
					"""
				},
				"SELECT * FROM"
		);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2125
	// [String Templates] Compilation error when Template Processor used in unconditional for loop
	public void testIssue2125_4() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					import java.lang.StringTemplate.Processor;

					public class X implements Processor<String, RuntimeException> {
					  public static void main(String [] args) {
					    for (int i = 0; i < 3; i++) {
					      System.out.println(STR."SELECT * FROM");
					    }
					  }

					  @Override
					  public String process(StringTemplate stringTemplate) throws RuntimeException {
					    return STR.process(stringTemplate);
					  }
					}
					"""
				},
				"""
					SELECT * FROM
					SELECT * FROM
					SELECT * FROM"""
		);
	}
	// Test with read of the outer string literal inside the template expression
	public void test0060() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
	public class X {
	    public static void main(String argv[]) {
        String first = "Jay";
        String last = "A";
        String greet = STR."Hello \\{
                greet
                } \\{last}!";
        System.out.println(greet);
						      }
						}
					"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						greet
						^^^^^
					The local variable greet may not have been initialized
					----------
					"""
		);
	}
	public void test0061() {
		Map<String, String> options = getCompilerOptions(false);
		runNegativeTest(
				new String[] {
					"X.java",
					"""
					public class X {
						 public static void main(String argv[]) {
							String greet = STR."Hello!";
							System.out.println(greet);
						}
					}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						String greet = STR."Hello!";
						               ^^^^^^^^^^^^
					String Template is a preview feature and disabled by default. Use --enable-preview to enable
					----------
					""",
				"",
				null,
				false,
				options);
	}
	public void test0062() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
					public class X {
						 public static void main(String argv[]) {
						 	String name = "Jay";
							String greet = STR."Hello \\{name + foo())}!";
							System.out.println(greet);
						}
						private static String foo() {
							return "A";
						}
					}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						String greet = STR."Hello \\{name + foo())}!";
						                                        ^
					Syntax error on token ")", delete this token
					----------
					""");
	}
	public void test0063() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
					public class X {
						 public static void main(String argv[]) {
						 	String name = "Jay";
							String greet = STR."Hello \\{name + foo())}!";
							System.out.println(greet);
						}
						private static String foo() {
							return "A";
						}
					}
					interface Intf {
					}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						String greet = STR."Hello \\{name + foo())}!";
						                                        ^
					Syntax error on token ")", delete this token
					----------
					""");
	}
	public void testIssue1719() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					  public static void main(String[] args) {
					    String name = "Bill";
					    System.out.println(STR."\\{name}");
					    String html = STR.\"""
					         \\{name}
					      \""";
					  }
					}"""
				},
				"Bill");
	}
	public void testIssue1722() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					  public static void main(String[] args) {
					    String firstName = "Bill", lastName = "Duck";
					    System.out.println(STR."\\{firstName} \\{lastName}");

					    String title = "My Web Page";

					    String html = STR.\"""
					        </head>
					      \""";
					    System.out.println(html);
					  }
					}"""
				},
				"Bill Duck\n" +
				"  </head>");
	}
	public void testJEP430Examples() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					import java.io.File;
					import java.time.LocalTime;
					import java.time.format.DateTimeFormatter;
					import java.util.Date;
					import java.util.List;
					public class X {
					  @SuppressWarnings("preview")
					  public static void main(String[] args) {
						  String firstName = "Bill";
						  String lastName  = "Duck";
						  String fullName  = STR."\\{firstName} \\{lastName}";
						  System.out.println(fullName); // "Bill Duck"
						  String sortName  = STR."\\{lastName}, \\{firstName}";
						  System.out.println(sortName); // "Duck, Bill"
						  int x = 10, y = 20;
						  String s = STR."\\{x} + \\{y} = \\{x + y}";
						  System.out.println(s); // "10 + 20 = 30"
						  s = STR."You have a \\{getOfferType()} waiting for you!";
						  System.out.println(s); // "You have a gift waiting for you!"
						  Request req = new Request();
						  String t = STR."Access at \\{req.date} \\{req.time} from \\{req.ipAddress}";

						  String filePath = "tmp.dat";
						  File file = new File(filePath);
						  String old = "The file " + filePath + " " + (file.exists() ? "does" : "does not") + " exist";
						  String msg = STR."The file \\{filePath} \\{file.exists() ? "does" : "does not"} exist";
						  System.out.println(msg); // "The file tmp.dat does exist" or "The file tmp.dat does not exist"
						  String time = STR."The time is \\{
								    // The java.time.format package is very useful
								    DateTimeFormatter
								      .ofPattern("HH:mm:ss")
								      .format(LocalTime.NOON)
								} right now";
					  		System.out.println(time); // "The time is 12:34:56 right now"
							int index = 0;
							String data = STR."\\{index++}, \\{index++}, \\{index++}, \\{index++}";
							System.out.println(data); //"0, 1, 2, 3"
							String[] fruit = { "apples", "oranges", "peaches" };
							s = STR."\\{fruit[0]}, \\{STR."\\{fruit[1]}, \\{fruit[2]}"}";
							System.out.println(s); //"apples, oranges, peaches"
							s = STR."\\{fruit[0]}, \\{
								    STR."\\{fruit[1]}, \\{fruit[2]}"
								}";
							System.out.println(s);
							String tmp = STR."\\{fruit[1]}, \\{fruit[2]}";
							s = STR."\\{fruit[0]}, \\{tmp}";
							System.out.println(s);
							Rectangle[] zone = new Rectangle[] {
								    new Rectangle("Alfa", 17.8, 31.4),
								    new Rectangle("Bravo", 9.6, 12.4),
								    new Rectangle("Charlie", 7.1, 11.23),
								};
								String table = STR.\"""
								    Description  Width  height()  Area
								    \\{zone[0].name()}  \\{zone[0].width()}  \\{zone[0].height()}     \\{zone[0].area()}
								    \\{zone[1].name()}  \\{zone[1].width()}  \\{zone[1].height()}     \\{zone[1].area()}
								    \\{zone[2].name()}  \\{zone[2].width()}  \\{zone[2].height()}     \\{zone[2].area()}
								    Total \\{zone[0].area() + zone[1].area() + zone[2].area()}
								    \""";
								System.out.println(table);
								x = 10;
								y = 20;
								StringTemplate st = StringTemplate.RAW."\\{x} plus \\{y} equals \\{x + y}";
								List<String> fragments = st.fragments();
								String result = String.join("\\\\{}", fragments);
								System.out.println(result); //"\\{} plus \\{} equals \\{}"
								List<Object> values = st.values();
								System.out.println(values);//[10, 20, 30]
							}
					  static String getOfferType() {
						  return "gift";
					  }
					}
					record Rectangle(String name, double width, double height) {
					    double area() {
					        return width * height;
					    }
					}
					class Request {
						Date date = new Date(3, 5, 2022);
						String time = "15:34";
						String ipAddress = "0.0.0.0";
					}"""
				},
				"""
				Bill Duck
				Duck, Bill
				10 + 20 = 30
				You have a gift waiting for you!
				The file tmp.dat does not exist
				The time is 12:00:00 right now
				0, 1, 2, 3
				apples, oranges, peaches
				apples, oranges, peaches
				apples, oranges, peaches
				Description  Width  height()  Area
				Alfa  17.8  31.4     558.92
				Bravo  9.6  12.4     119.03999999999999
				Charlie  7.1  11.23     79.733
				Total 757.693

				\\{} plus \\{} equals \\{}
				[10, 20, 30]""");
	}
	public void testIssue2121_01() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						interface TemplateProcessor extends java.lang.StringTemplate.Processor<Boolean, RuntimeException> {
						    Boolean process(StringTemplate st);
						}

						public class X {
						    public static void main(String argv[]) {
						        TemplateProcessor STR = st -> st.interpolate().equals("abc");
						        if (STR."abc") {
						        	System.out.println("hello");
						        }
						    }
						}
					"""
				},
				"hello"
		);
	}
	public void testIssue2121_02() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						interface TemplateProcessor extends java.lang.StringTemplate.Processor<Boolean, RuntimeException> {
						    Boolean process(StringTemplate st);
						}

						public class X {
						    public static void main(String argv[]) {
						        TemplateProcessor STR = st -> st.interpolate().equals("abc");
					   			int  i = 0;
					   			while ((STR."abc") && i < 1) {
					   				i++;
						        	System.out.println("hello");
						        }
						    }
						}
					"""
				},
				"hello"
		);
	}
	public void testIssue2121_03() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						interface TemplateProcessor extends java.lang.StringTemplate.Processor<Boolean, RuntimeException> {
						    Boolean process(StringTemplate st);
						}

						public class X {
						    public static void main(String argv[]) {
						        TemplateProcessor STR = st -> st.interpolate().equals("abc");
					   			for ( int i = 0; (STR."abc") && i < 1; i++) {
						        	System.out.println("hello");
						        }
						    }
						}
					"""
				},
				"hello"
		);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2062
	// VerifyError with String Templates
	public void testIssue2062() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					import java.lang.StringTemplate.Processor;
					import java.util.Map;

					public class X {

					  public static void main(String[] args) throws Exception {
					    Map<String, String> stuff = Map.of("a", "b");
					    Transaction tx = new Transaction();

					    for(Map.Entry<String, String> entry : stuff.entrySet()) {
					      String name = entry.getKey();

					      String s = tx.\"""
					        INSERT INTO settings (a, b) VALUES (\\{name}, \\{entry.getValue()})\""";
					      System.out.println(s);
					    }
					  }

					  static class Transaction implements Processor<String, Exception> {
					    @Override
					    public String process(StringTemplate stringTemplate) throws Exception {
					      return STR.process(stringTemplate);
					    }
					  }
					}
					"""
				},
				"INSERT INTO settings (a, b) VALUES (a, b)");
	}
}
