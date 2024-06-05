/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTestForClass extends JavadocTest {

	public static Test suite() {
		return buildAllCompliancesTestSuite(javadocTestClass());
	}
	public JavadocTestForClass(String name) {
		super(name);
	}
	public static Class javadocTestClass() {
		return JavadocTestForClass.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		return options;
	}

	/* (non-Javadoc)
	 * Test unexpected tags
	 */
	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Valid class javadoc
						 * @author ffr
						 */
					public class X {
						public void foo() {
						}
					}
					""" });
	}

	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Invalid class javadoc
						 * @param x Invalid tag
						 */
					public class X {
						public void foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param x Invalid tag
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Invalid class javadoc
						 * @throws NullPointerException Invalid tag
						 */
					public class X {
						public void foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @throws NullPointerException Invalid tag
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Invalid class javadoc
						 * @exception NullPointerException Invalid tag
						 */
					public class X {
						public void foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @exception NullPointerException Invalid tag
					   ^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Invalid class javadoc
						 * @return Invalid tag
						 */
					public class X {
						public void foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @return Invalid tag
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Invalid class javadoc
						 * @exception NullPointerException Invalid tag
						 * @throws NullPointerException Invalid tag
						 * @return Invalid tag
						 * @param x Invalid tag
						 */
					public class X {
						public void foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @exception NullPointerException Invalid tag
					   ^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in X.java (at line 4)
					* @throws NullPointerException Invalid tag
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				3. ERROR in X.java (at line 5)
					* @return Invalid tag
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				4. ERROR in X.java (at line 6)
					* @param x Invalid tag
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test007() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Valid class javadoc
						 * @author ffr
						 */
					public class X {
						public void foo() {}
						/**
						 * Invalid javadoc comment
						 */
					}
					""" }
			);
	}

	public void test008() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo() {}
						/**
						 * Invalid javadoc comment
						 */
					}
					""" }
			);
	}

	public void test009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid javadoc comment
						 */
					}
					""" }
			);
	}

	/*
	 * (non-Javadoc) Test @see tag
	 */
	// String references
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Invalid string references\s
						 *
						 * @see "
						 * @see "unterminated string
						 * @see "invalid string""
						 * @see "invalid" no text allowed after the string
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @see "
					       ^
				Javadoc: Invalid reference
				----------
				2. ERROR in X.java (at line 5)
					* @see "unterminated string
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid reference
				----------
				3. ERROR in X.java (at line 6)
					* @see "invalid string""
					                       ^
				Javadoc: Unexpected text
				----------
				4. ERROR in X.java (at line 7)
					* @see "invalid" no text allowed after the string
					                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Unexpected text
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Valid string references\s
						 *
						 * @see "Valid normal string"
						 * @see "Valid \\"string containing\\" \\"double-quote\\""
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" });
	}

	// URL Link references
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Invalid URL link references\s
						 *
						 * @see <a href=
						 * @see <a href="invalid">invalid</a> no text allowed after the href
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @see <a href=
					       ^^^^^^^^
				Javadoc: Malformed link reference
				----------
				2. ERROR in X.java (at line 5)
					* @see <a href="invalid">invalid</a> no text allowed after the href
					                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Unexpected text
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test013() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Valid URL link references\s
						 *
						 * @see <a href="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Valid URL link reference</a>
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" });
	}

	// @see Classes references
	public void test020() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
						/**
						 * Valid local classes references\s
						 *
						 * @see Visibility Valid ref: local class\s
						 * @see Visibility.VcPublic Valid ref: visible inner class of local class\s
						 * @see AbstractVisibility.AvcPublic Valid ref: visible inner class of local class\s
						 * @see test.Visibility Valid ref: local class\s
						 * @see test.Visibility.VcPublic Valid ref: visible inner class of local class\s
						 * @see test.AbstractVisibility.AvcPublic Valid ref: visible inner class of local class\s
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" });
	}

	public void test021() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
						/**
						 * Invalid local classes references\s
						 *
						 * @see Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
						 * @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
						 * @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
						 * @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
						 * @see Unknown Invalid ref: unknown class\s
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 5)
					* @see Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				2. ERROR in test\\X.java (at line 6)
					* @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				3. ERROR in test\\X.java (at line 7)
					* @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.Visibility.VcPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 8)
					* @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.Visibility.AvcPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 9)
					* @see Unknown Invalid ref: unknown class\s
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				""");
	}

	public void test022() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				// Inner classes are not visible in generated documentation
				//"	 * @see VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"public class X {\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	public void test023() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
						/**
						 * Invalid external classes references\s
						 *
						 * @see VisibilityPackage Invalid ref: non visible class\s
						 * @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see VisibilityPackage Invalid ref: non visible class\s
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				""");
	}

	public void test024() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see test.copy.VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				// Inner classes are not visible in generated documentation
				//"	 * @see test.copy.VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"public class X {\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
				}
		);
	}

	// @see Field references
	public void test030() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
						/**
						 * Valid local class field references
						 *
						 * @see #x Valid ref: accessible field
						 * @see Visibility#vf_public Valid ref: visible field
						 * @see Visibility.VcPublic#vf_public Valid ref: visible field in visible inner class
						 */
					public class X {
						int x;
						public void s_foo() {
						}
					}
					""" });
	}

	public void test031() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
						/**
						 * Invalid local class field references
						 *
						 * @see #x Invalid ref: non existent field
						 * @see Visibility#unknown Invalid ref: non existent field
						 * @see Visibility#vf_private Invalid ref: non visible field
						 * @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)
						 * @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class
						 * @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 5)
					* @see #x Invalid ref: non existent field
					        ^
				Javadoc: x cannot be resolved or is not a field
				----------
				2. ERROR in test\\X.java (at line 6)
					* @see Visibility#unknown Invalid ref: non existent field
					                  ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				3. ERROR in test\\X.java (at line 7)
					* @see Visibility#vf_private Invalid ref: non visible field
					                  ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				4. ERROR in test\\X.java (at line 8)
					* @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 9)
					* @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class
					                           ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				6. ERROR in test\\X.java (at line 10)
					* @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				""");
	}

	public void test032() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
						/**
						 * Valid other package visible class fields references
						 *
						 * @see VisibilityPublic#vf_public Valid ref to visible field of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vf_public Valid ref to visible field of other package public inner class
						 */
					public class X {
						public void s_foo() {
						}
					}
					"""
			}
		);
	}

	public void test033() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
						/**
						 * Invalid other package non visible class fields references
						 *
						 * @see VisibilityPackage#unknown Invalid ref to non existent field of other package non visible class
						 * @see VisibilityPublic#unknown Invalid ref to non existent field of other package class
						 * @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class
						 * @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
						 * @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
						 * @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see VisibilityPackage#unknown Invalid ref to non existent field of other package non visible class
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class
					                        ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class
					                        ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
					                                 ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class
					                                 ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				""");
	}

	// @see method references
	public void test040() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
						/**
						 * Valid local methods references with array
						 *\s
						 * @see #smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) Valid local method reference
						 * @see #smr_foo(char[], int[][], String[][][], Vector[][][][]) Valid local method reference
						 * @see #smr_foo(char[],int[][],java.lang.String[][][],java.util.Vector[][][][]) Valid local method reference
						 */ \s
					public class X {
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {
						}
					}
					""" });
	}

	public void test041() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
						/**
						 * Invalid local methods references with array (non applicable arrays)
						 *\s
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][]) Invalid ref: invalid arguments declaration
						 */ \s
					public class X {
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector[][][])
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test042() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
						/**
						 * Valid local methods references
						 *\s
						 * @see #smr_foo() Valid local method reference
						 * @see #smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference
						 * @see #smr_foo(java.lang.String, java.lang.String, int) Valid local method reference  \s
						 * @see #smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
					public class X {
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo() {
						}
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" });
	}

	public void test043() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Invalid local methods references
						 *\s
						 * @see #unknown() Invalid ref: undefined local method reference
						 */ \s
					public class X {
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @see #unknown() Invalid ref: undefined local method reference
					        ^^^^^^^
				Javadoc: The method unknown() is undefined for the type X
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test044() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
						/**
						 * Invalid local methods references
						 *\s
						 * @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable
						 * @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean) Invalid ref: local method not applicable
						 * @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
						 */ \s
					public class X {
						// Empty methods definition for reference
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (float, long, char, short, byte, int, boolean)
				----------
				2. ERROR in X.java (at line 5)
					* @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, String, int, String)
				----------
				3. ERROR in X.java (at line 6)
					* @see #smr_foo(boolean) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean)
				----------
				4. ERROR in X.java (at line 7)
					* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
					                ^^^^^^^^^
				Javadoc: Hashtable cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 7)
					* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
					                             ^^^^^^
				Javadoc: Vector cannot be resolved to a type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test045() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
						/**
						 * Valid local methods references
						 *\s
						 * @see X#smr_foo() Valid local method reference
						 * @see X#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference
						 * @see X#smr_foo(String x, java.lang.String y, int z) Valid local method reference  \s
						 * @see X#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
					public class X {
						public void smr_foo() {
						}
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" });
	}

	public void test046() {
		this.runConformTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"""
					package test.deep.qualified.name.p;
					import java.util.Vector;
						/**
						 * Valid local methods references
						 *\s
						 * @see test.deep.qualified.name.p.X#smr_foo() Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(String x, java.lang.String y, int z) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
					public class X {
						public void smr_foo() {
						}
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" });
	}

	public void test047() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
						/**
						 * Valid package class methods references
						 *\s
						 * @see Visibility#vm_public() Valid ref: visible method
						 * @see Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class
						 * @see test.Visibility#vm_public() Valid ref: visible method
						 * @see test.Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class
						 */ \s
					public class X {
						public void s_foo() {
						}
					}
					""" });
	}

	public void test048() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
						/**
						 * Invalid package class methods references (non-existence)
						 *\s
						 * @see Visibility#unknown() Invalid ref: non-existent method
						 * @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class
						 * @see Unknown#vm_public() Invalid ref: non-existent class
						 * @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class
						 */ \s
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 5)
					* @see Visibility#unknown() Invalid ref: non-existent method
					                  ^^^^^^^
				Javadoc: The method unknown() is undefined for the type Visibility
				----------
				2. ERROR in test\\X.java (at line 6)
					* @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class
					                           ^^^^^^^
				Javadoc: The method unknown() is undefined for the type Visibility.VcPublic
				----------
				3. ERROR in test\\X.java (at line 7)
					* @see Unknown#vm_public() Invalid ref: non-existent class
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				4. ERROR in test\\X.java (at line 8)
					* @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class
					       ^^^^^^^^^^^^^^^^^^
				Javadoc: Visibility.Unknown cannot be resolved to a type
				----------
				""");
	}

	public void test049() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
						/**
						 * Invalid package class methods references (non-visible)
						 *\s
						 * @see Visibility#vm_private() Invalid ref: non-visible method
						 * @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)
						 * @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class
						 */ \s
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 5)
					* @see Visibility#vm_private() Invalid ref: non-visible method
					                  ^^^^^^^^^^
				Javadoc: The method vm_private() from the type Visibility is not visible
				----------
				2. ERROR in test\\X.java (at line 6)
					* @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				3. ERROR in test\\X.java (at line 7)
					* @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The method vm_private() from the type Visibility.VcPublic is not visible
				----------
				""");
	}

	public void test050() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
						/**
						 * Invalid package class methods references (non-applicable)
						 *\s
						 * @see Visibility#vm_private(int) Invalid ref: non-applicable method
						 * @see Visibility#vm_public(String) Invalid ref: non-applicable method
						 * @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class
						 * @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class
						 */ \s
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 5)
					* @see Visibility#vm_private(int) Invalid ref: non-applicable method
					                  ^^^^^^^^^^
				Javadoc: The method vm_private() in the type Visibility is not applicable for the arguments (int)
				----------
				2. ERROR in test\\X.java (at line 6)
					* @see Visibility#vm_public(String) Invalid ref: non-applicable method
					                  ^^^^^^^^^
				Javadoc: The method vm_public() in the type Visibility is not applicable for the arguments (String)
				----------
				3. ERROR in test\\X.java (at line 7)
					* @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The method vm_private() in the type Visibility.VcPublic is not applicable for the arguments (Integer, byte)
				----------
				4. ERROR in test\\X.java (at line 8)
					* @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class
					                           ^^^^^^^^^
				Javadoc: The method vm_public() in the type Visibility.VcPublic is not applicable for the arguments (Double, Boolean)
				----------
				""");
	}

	public void test051() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
						/**
						 * Invalid other package non visible class methods references (non existent/visible arguments)
						 *\s
						 * @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
						 * @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
						 */ \s
					public class X {
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. WARNING in test\\X.java (at line 2)
					import test.copy.*;
					       ^^^^^^^^^
				The import test.copy is never used
				----------
				2. ERROR in test\\X.java (at line 6)
					* @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				3. ERROR in test\\X.java (at line 7)
					* @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				""");
	}

	public void test052() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.VisibilityPublic;
						/**
						 * Valid other package visible class methods references\s
						 *\s
						 * @see VisibilityPublic#vm_public() Valid ref to not visible method of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class
						 */
					public class X {
						public void s_foo() {
						}
					}
					"""
			}
		);
	}

	public void test053() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
						/**
						 * Valid other package visible class methods references\s
						 *\s
						 * @see test.copy.VisibilityPublic#vm_public() Valid ref to not visible method of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class
						 */
					public class X {
						public void s_foo() {
						}
					}
					""" });
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253750
	public void test054() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) {
			runNegativeTest(
					new String[] {
						"X.java",
						"""
							import java.util.Map;
							import java.util.Map.Entry;
							
							/**
							 * <ul>
							 * <li> {@link Entry} </li>
							 * </ul>
							 */
							public interface X extends Map {
								int i = 0;
							}
							""",
						},
						"""
							----------
							1. WARNING in X.java (at line 2)
								import java.util.Map.Entry;
								       ^^^^^^^^^^^^^^^^^^^
							The import java.util.Map.Entry is never used
							----------
							2. ERROR in X.java (at line 6)
								* <li> {@link Entry} </li>
								              ^^^^^
							Javadoc: Invalid member type qualification
							----------
							""");
			return;
		}
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					import java.util.Map.Entry;
					
					/**
					 * <ul>
					 * <li> {@link Entry} </li>
					 * </ul>
					 */
					public interface X extends Map {
						int i;
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						int i;
						    ^
					The blank final field i may not have been initialized
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253750 - variation
	public void test055() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					
					/**
					 * <ul>
					 * <li> {@link Entry} </li>
					 * </ul>
					 */
					public interface X extends Map {
						Entry e = null;
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						* <li> {@link Entry} </li>
						              ^^^^^
					Javadoc: Invalid member type qualification
					----------
					""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253750
	public void test056() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) {
			runNegativeTest(
					new String[] {
						"X.java",
						"""
							import java.util.Map;
							import java.util.Map.Entry;
							
							/**
							 * <ul>
							 * <li> {@link Entry} </li>
							 * </ul>
							 */
							public interface X extends Map {
								Entry e = null;
							}
							""",
						},
						"""
							----------
							1. WARNING in X.java (at line 2)
								import java.util.Map.Entry;
								       ^^^^^^^^^^^^^^^^^^^
							The import java.util.Map.Entry is never used
							----------
							2. ERROR in X.java (at line 6)
								* <li> {@link Entry} </li>
								              ^^^^^
							Javadoc: Invalid member type qualification
							----------
							""");
			return;
		}
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					import java.util.Map.Entry;
					
					/**
					 * <ul>
					 * <li> {@link Entry} </li>
					 * </ul>
					 */
					public interface X extends Map {
						Entry e;
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						Entry e;
						      ^
					The blank final field e may not have been initialized
					----------
					""");
	}
}
