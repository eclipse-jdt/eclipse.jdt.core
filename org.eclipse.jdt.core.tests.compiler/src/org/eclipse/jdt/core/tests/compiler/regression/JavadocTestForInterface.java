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

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTestForInterface extends JavadocTest {
	public JavadocTestForInterface(String name) {
		super(name);
	}
	public static Class javadocTestClass() {
		return JavadocTestForInterface.class;
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(javadocTestClass());
	}
	static { // Use this static to initialize testNames (String[]) , testRange (int[2]), testNumbers (int[])
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

	/*
	 * (non-Javadoc)
	 * Javadoc comment of Interface
	 */
	// Unexpected tag
	public void test001() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
						/**
						 * Valid class javadoc
						 * @author ffr
						 */
					public interface IX {
						public void foo();
					}
					""" });
	}

	public void test002() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
						/**
						 * Invalid class javadoc
						 * @param x Invalid tag
						 */
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 3)
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
				"IX.java",
				"""
						/**
						 * Invalid class javadoc
						 * @throws NullPointerException Invalid tag
						 */
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 3)
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
				"IX.java",
				"""
						/**
						 * Invalid class javadoc
						 * @exception NullPointerException Invalid tag
						 */
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 3)
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
				"IX.java",
				"""
						/**
						 * Invalid class javadoc
						 * @return Invalid tag
						 */
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 3)
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
				"IX.java",
				"""
						/**
						 * Invalid class javadoc
						 * @exception NullPointerException Invalid tag
						 * @throws NullPointerException Invalid tag
						 * @return Invalid tag
						 * @param x Invalid tag
						 */
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 3)
					* @exception NullPointerException Invalid tag
					   ^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in IX.java (at line 4)
					* @throws NullPointerException Invalid tag
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				3. ERROR in IX.java (at line 5)
					* @return Invalid tag
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				4. ERROR in IX.java (at line 6)
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
				"IX.java",
				"""
						/**
						 * Valid class javadoc
						 * @author ffr
						 */
					public interface IX {
						public void foo();
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
				"IX.java",
				"""
					public interface IX {
						public void foo();
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
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid javadoc comment
						 */
					}
					""" }
			);
	}


	// @see tag
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
						/**
						 * Invalid string references\s
						 *
						 * @see "unterminated string
						 * @see "invalid" no text allowed after the string
						 */
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 4)
					* @see "unterminated string
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid reference
				----------
				2. ERROR in IX.java (at line 5)
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
				"IX.java",
				"""
						/**
						 * Valid string references\s
						 *
						 * @see "Valid normal string"
						 * @see "Valid \\"string containing\\" \\"double-quote\\""
						 */
					public interface IX {
						public void foo();
					}
					""" });
	}

	public void test012() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
						/**
						 * Invalid URL link references\s
						 *
						 * @see <a href="invalid">invalid</a
						 * @see <a href="invalid">invalid</a> no text allowed after the href
						 */
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 4)
					* @see <a href="invalid">invalid</a
					                                ^^^
				Javadoc: Malformed link reference
				----------
				2. ERROR in IX.java (at line 5)
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
				"IX.java",
				"""
						/**
						 * Valid URL link references\s
						 *
						 * @see <a hReF = "http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Valid URL link reference</A>
						 */
					public interface IX {
						public void foo();
					}
					""" });
	}

	// @see Classes references
	public void test020() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
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
					public interface IX {
						public void foo();
					}
					""" });
	}

	public void test021() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
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
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 5)
					* @see Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				2. ERROR in test\\IX.java (at line 6)
					* @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				3. ERROR in test\\IX.java (at line 7)
					* @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.Visibility.VcPrivate is not visible
				----------
				4. ERROR in test\\IX.java (at line 8)
					* @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.Visibility.AvcPrivate is not visible
				----------
				5. ERROR in test\\IX.java (at line 9)
					* @see Unknown Invalid ref: unknown class\s
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				""");
	}

	public void test022() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.*;
						/**
						 * Valid external classes references\s
						 *
						 * @see VisibilityPublic Valid ref: visible class through import => no warning on import
						 */
					public interface IX {
						public void foo();
					}
					"""
			}
		);
	}

	public void test023() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.*;
						/**
						 * Invalid external classes references\s
						 *
						 * @see VisibilityPackage Invalid ref: non visible class\s
						 * @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
						 */
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 6)
					* @see VisibilityPackage Invalid ref: non visible class\s
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				2. ERROR in test\\IX.java (at line 7)
					* @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				""");
	}

	public void test024() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
						/**
						 * Valid external classes references\s
						 *
						 * @see test.copy.VisibilityPublic Valid ref: visible class through import => no warning on import
						 */
					public interface IX {
						public void foo();
					}
					"""
			}
		);
	}

	// @see Field references
	public void test030() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
						/**
						 * Valid local class field references
						 *
						 * @see Visibility#vf_public Valid ref: visible field
						 * @see Visibility.VcPublic#vf_public Valid ref: visible field in visible inner class
						 */
					public interface IX {
						public void foo();
					}
					""" });
	}

	public void test031() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
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
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 5)
					* @see #x Invalid ref: non existent field
					        ^
				Javadoc: x cannot be resolved or is not a field
				----------
				2. ERROR in test\\IX.java (at line 6)
					* @see Visibility#unknown Invalid ref: non existent field
					                  ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				3. ERROR in test\\IX.java (at line 7)
					* @see Visibility#vf_private Invalid ref: non visible field
					                  ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				4. ERROR in test\\IX.java (at line 8)
					* @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				5. ERROR in test\\IX.java (at line 9)
					* @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class
					                           ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				6. ERROR in test\\IX.java (at line 10)
					* @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				""");
	}

	public void test032() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.*;
						/**
						 * Valid other package visible class fields references
						 *
						 * @see VisibilityPublic#vf_public Valid ref to not visible field of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class
						 */
					public interface IX {
						public void foo();
					}
					"""
			}
		);
	}

	public void test033() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
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
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 6)
					* @see VisibilityPackage#unknown Invalid ref to non existent field of other package non visible class
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				2. ERROR in test\\IX.java (at line 7)
					* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class
					                        ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				3. ERROR in test\\IX.java (at line 8)
					* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class
					                        ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				4. ERROR in test\\IX.java (at line 9)
					* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				5. ERROR in test\\IX.java (at line 10)
					* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
					                                 ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				6. ERROR in test\\IX.java (at line 11)
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
				"IX.java",
				"""
					import java.util.Vector;
						/**
						 * Valid local methods references with array
						 *\s
						 * @see #smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) Valid local method reference
						 * @see #smr_foo(char[], int[][], String[][][], Vector[][][][]) Valid local method reference
						 * @see #smr_foo(char[],int[][],java.lang.String[][][],java.util.Vector[][][][]) Valid local method reference
						 */ \s
					public interface IX {
						public void foo();
					
						// Empty methods definition for reference
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra);
					}
					""" });
	}

	public void test041() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					import java.util.Vector;
						/**
						 * Invalid local methods references with array (non applicable arrays)
						 *\s
						 * @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 */ \s
					public interface IX {
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra);
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					* @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type IX is not applicable for the arguments (char[], int[][], String[][], Vector[][][][])
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test042() {
		this.runConformTest(
			new String[] {
				"IX.java",
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
					public interface IX {
						public void foo();
					
						// Empty methods definition for reference
						public void smr_foo();
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);
						public void smr_foo(String str1, java.lang.String str2, int i);
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);
					}
					""" });
	}

	public void test043() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
						/**
						 * Invalid local methods references
						 *\s
						 * @see #unknown() Invalid ref: undefined local method reference
						 */ \s
					public interface IX {
						public void foo();
					
						// Empty methods definition for reference
						public void smr_foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 4)
					* @see #unknown() Invalid ref: undefined local method reference
					        ^^^^^^^
				Javadoc: The method unknown() is undefined for the type IX
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test044() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
						/**
						 * Invalid local methods references
						 *\s
						 * @see #smr_foo(int) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable
						 * @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean) Invalid ref: local method not applicable
						 * @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
						 */ \s
					public interface IX {
						public void foo();
					
						// Empty methods definition for reference
						public void smr_foo();
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);
						public void smr_foo(String str1, java.lang.String str2, int i);
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 4)
					* @see #smr_foo(int) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo() in the type IX is not applicable for the arguments (int)
				----------
				2. ERROR in IX.java (at line 5)
					* @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type IX is not applicable for the arguments (float, long, char, short, byte, int, boolean)
				----------
				3. ERROR in IX.java (at line 6)
					* @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type IX is not applicable for the arguments (String, String, int, String)
				----------
				4. ERROR in IX.java (at line 7)
					* @see #smr_foo(boolean) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type IX is not applicable for the arguments (boolean)
				----------
				5. ERROR in IX.java (at line 8)
					* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
					                ^^^^^^^^^
				Javadoc: Hashtable cannot be resolved to a type
				----------
				6. ERROR in IX.java (at line 8)
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
				"IX.java",
				"""
					import java.util.Vector;
						/**
						 * Valid local methods references
						 *\s
						 * @see IX#smr_foo() Valid local method reference
						 * @see IX#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference
						 * @see IX#smr_foo(String x, java.lang.String y, int z) Valid local method reference  \s
						 * @see IX#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
					public interface IX {
						public void smr_foo();
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);
						public void smr_foo(String str1, java.lang.String str2, int i);
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);
					}
					""" });
	}

	public void test046() {
		this.runConformTest(
			new String[] {
				"test/deep/qualified/name/p/IX.java",
				"""
					package test.deep.qualified.name.p;
					import java.util.Vector;
						/**
						 * Valid local methods references
						 *\s
						 * @see test.deep.qualified.name.p.IX#smr_foo() Valid local method reference
						 * @see test.deep.qualified.name.p.IX#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference
						 * @see test.deep.qualified.name.p.IX#smr_foo(String x, java.lang.String y, int z) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.IX#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
					public interface IX {
						public void smr_foo();
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);
						public void smr_foo(String str1, java.lang.String str2, int i);
						public void smr_foo(java.util.Hashtable h, Vector v, boolean b);
					}
					""" });
	}

	public void test047() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
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
					public interface IX {
						public void foo();
					}
					""" });
	}

	public void test048() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
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
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 5)
					* @see Visibility#unknown() Invalid ref: non-existent method
					                  ^^^^^^^
				Javadoc: The method unknown() is undefined for the type Visibility
				----------
				2. ERROR in test\\IX.java (at line 6)
					* @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class
					                           ^^^^^^^
				Javadoc: The method unknown() is undefined for the type Visibility.VcPublic
				----------
				3. ERROR in test\\IX.java (at line 7)
					* @see Unknown#vm_public() Invalid ref: non-existent class
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				4. ERROR in test\\IX.java (at line 8)
					* @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class
					       ^^^^^^^^^^^^^^^^^^
				Javadoc: Visibility.Unknown cannot be resolved to a type
				----------
				""");
	}

	public void test049() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
						/**
						 * Invalid package class methods references (non-visible)
						 *\s
						 * @see Visibility#vm_private() Invalid ref: non-visible method
						 * @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)
						 * @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class
						 */ \s
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 5)
					* @see Visibility#vm_private() Invalid ref: non-visible method
					                  ^^^^^^^^^^
				Javadoc: The method vm_private() from the type Visibility is not visible
				----------
				2. ERROR in test\\IX.java (at line 6)
					* @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				3. ERROR in test\\IX.java (at line 7)
					* @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The method vm_private() from the type Visibility.VcPublic is not visible
				----------
				""");
	}

	public void test050() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
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
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 5)
					* @see Visibility#vm_private(int) Invalid ref: non-applicable method
					                  ^^^^^^^^^^
				Javadoc: The method vm_private() in the type Visibility is not applicable for the arguments (int)
				----------
				2. ERROR in test\\IX.java (at line 6)
					* @see Visibility#vm_public(String) Invalid ref: non-applicable method
					                  ^^^^^^^^^
				Javadoc: The method vm_public() in the type Visibility is not applicable for the arguments (String)
				----------
				3. ERROR in test\\IX.java (at line 7)
					* @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The method vm_private() in the type Visibility.VcPublic is not applicable for the arguments (Integer, byte)
				----------
				4. ERROR in test\\IX.java (at line 8)
					* @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class
					                           ^^^^^^^^^
				Javadoc: The method vm_public() in the type Visibility.VcPublic is not applicable for the arguments (Double, Boolean)
				----------
				""");
	}

	public void test051() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.*;
						/**
						 * Invalid other package non visible class methods references (non existent/visible arguments)
						 *\s
						 * @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
						 * @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
						 */
					public interface IX {
						public void foo();
					}
					""" },
			"""
				----------
				1. WARNING in test\\IX.java (at line 2)
					import test.copy.*;
					       ^^^^^^^^^
				The import test.copy is never used
				----------
				2. ERROR in test\\IX.java (at line 6)
					* @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				3. ERROR in test\\IX.java (at line 7)
					* @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				""");
	}

	public void test052() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.VisibilityPublic;
						/**
						 * Valid other package visible class methods references\s
						 *\s
						 * @see VisibilityPublic#vm_public() Valid ref to not visible method of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class
						 */
					public interface IX {
						public void foo();
					}
					"""
			}
		);
	}

	public void test053() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
						/**
						 * Valid other package visible class methods references\s
						 *\s
						 * @see test.copy.VisibilityPublic#vm_public() Valid ref to not visible method of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class
						 */
					public interface IX {
						public void foo();
					}
					""" });
	}

	/*
	 * (non-Javadoc)
	 * Javadoc method comment in Interface
	 */
	// @deprecated tag
	public void test060() {
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo(IX x) {
						 x.foo();
						}
					}
					""",
				"IX.java",
				"""
					public interface IX {
					  /**\s
					   *\s
					   * **   ** ** ** @deprecated */
						public void foo();
					}
					""",
				},
			"""
				----------
				1. WARNING in X.java (at line 3)
					x.foo();
					  ^^^^^
				The method foo() from the type IX is deprecated
				----------
				""",
				null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}

	public void test061() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/** @deprecated */
						int x=0;
						/**
						 * @see #x
						 */
						void foo();
					}
					""",
				"IY.java",
				"""
					/** @deprecated */
					public interface IY {
						int y=0;
						/**
						 * @see IX#x
						 * @see IY
						 * @see IY#y
						 */
						void foo();
					}
					""",
				"X.java",
				"""
					public class X {
						int x;
						/**
						 * @see IX#x
						 * @see IY
						 * @see IY#y
						 */
						void foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @see IX#x
					          ^
				Javadoc: The field IX.x is deprecated
				----------
				2. ERROR in X.java (at line 5)
					* @see IY
					       ^^
				Javadoc: The type IY is deprecated
				----------
				3. ERROR in X.java (at line 6)
					* @see IY#y
					       ^^
				Javadoc: The type IY is deprecated
				----------
				4. ERROR in X.java (at line 6)
					* @see IY#y
					          ^
				Javadoc: The field IY.y is deprecated
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test062() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						void foo(IX x) {
							x.foo(2);
						}
					}
					""",
				"IX.java",
				"""
					public interface IX {
					  /**\s
					   * Valid tags with deprecation
					   *
					   * @param x Valid param tag
					   * @return Valid return tag
					   * @throws NullPointerException Valid throws tag
					   * @exception IllegalArgumentException Valid throws tag
					   * @see X Valid see tag
					   * @deprecated
					   */
						public String foo(int x);
					}
					""",
				},
			"""
				----------
				1. WARNING in X.java (at line 4)
					x.foo(2);
					  ^^^^^^
				The method foo(int) from the type IX is deprecated
				----------
				""");
	}

	public void test063() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						void foo(IX x) {
							x.foo(2);
						}
					}
					""",
				"IX.java",
				"""
					public interface IX {
					  /**\s
					   * Invalid javadoc tags with valid deprecation
					   *
					   * @param
					   * @return String
					   * @throws Unknown
					   * @see "Invalid
					   * @see Unknown
					   * @param x
					   * @deprecated
					   */
						public String foo(int x);
					}
					""",
				},
			"""
				----------
				1. WARNING in X.java (at line 4)
					x.foo(2);
					  ^^^^^^
				The method foo(int) from the type IX is deprecated
				----------
				----------
				1. ERROR in IX.java (at line 5)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				2. ERROR in IX.java (at line 7)
					* @throws Unknown
					          ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				3. ERROR in IX.java (at line 8)
					* @see "Invalid
					       ^^^^^^^^
				Javadoc: Invalid reference
				----------
				4. ERROR in IX.java (at line 9)
					* @see Unknown
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				5. ERROR in IX.java (at line 10)
					* @param x
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				6. ERROR in IX.java (at line 13)
					public String foo(int x);
					                      ^
				Javadoc: Missing tag for parameter x
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @param tag
	public void test064() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Valid @param: no tags, no args
						 * Valid @throws/@exception: no tags, no thrown exception
						 */
						public void foo();
					}
					""" });
	}

	public void test065() {
		this.runConformTest(new String[] {
				"IX.java",
				"""
					public interface IX {
						public void foo();
					}
					""" });
	}

	public void test066() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid @param declaration: no arguments, 2 declared tags
						 * @param x
						 * 			Invalid param: not an argument on 2 lines
						 * @param x Invalid param: not an argument
						 */
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 4)
					* @param x
					         ^
				Javadoc: Parameter x is not declared
				----------
				2. ERROR in IX.java (at line 6)
					* @param x Invalid param: not an argument
					         ^
				Javadoc: Parameter x is not declared
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test067() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
					\t
						/**
						 * Valid @param declaration: 3 arguments, 3 tags in right order
						 * @param a Valid param
						 * @param b Valid param\s
						 * @param c Valid param
						 */
						public void foo(int a, int b, int c);
					}
					""" });
	}

	public void test068() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid @param declaration: 3 arguments, 3 correct tags in right order + 2 additional
						 * @param a Valid param
						 * @param x Invalid param: not an argument
						 * @param b Valid param\s
						 * @param y Invalid param: not an argument
						 * @param c Valid param
						 */
						public void foo(char a, char b, char c);
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					* @param x Invalid param: not an argument
					         ^
				Javadoc: Parameter x is not declared
				----------
				2. ERROR in IX.java (at line 7)
					* @param y Invalid param: not an argument
					         ^
				Javadoc: Parameter y is not declared
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test069() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid @param: all arguments are not documented
						 */
						public void foo(double a, double b, double c);
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					public void foo(double a, double b, double c);
					                       ^
				Javadoc: Missing tag for parameter a
				----------
				2. ERROR in IX.java (at line 5)
					public void foo(double a, double b, double c);
					                                 ^
				Javadoc: Missing tag for parameter b
				----------
				3. ERROR in IX.java (at line 5)
					public void foo(double a, double b, double c);
					                                           ^
				Javadoc: Missing tag for parameter c
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test070() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid @param: mix of all possible errors (missing a, not argument tag and duplicated)
						 * @param c Valid param
						 * @param x Invalid param: not an argument
						 * @param b Valid param
						 * @param c Invalid param: duplicated
						 * @param
						 */
						public void foo(double a, long b, int c);
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					* @param x Invalid param: not an argument
					         ^
				Javadoc: Parameter x is not declared
				----------
				2. ERROR in IX.java (at line 7)
					* @param c Invalid param: duplicated
					         ^
				Javadoc: Duplicate tag for parameter
				----------
				3. ERROR in IX.java (at line 8)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				4. ERROR in IX.java (at line 10)
					public void foo(double a, long b, int c);
					                       ^
				Javadoc: Missing tag for parameter a
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @throws/@exception tag
	public void test071() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Valid @throws tags: documented exception are unchecked
						 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)
						 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)
						 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)
						 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)
						 */
						public void foo();
					}
					""" });
	}

	public void test072() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * @throws java.awt.AWTexception Invalid exception: unknown type
						 * @throws IOException Invalid exception: unknown type
						 */
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 3)
					* @throws java.awt.AWTexception Invalid exception: unknown type
					          ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: java.awt.AWTexception cannot be resolved to a type
				----------
				2. ERROR in IX.java (at line 4)
					* @throws IOException Invalid exception: unknown type
					          ^^^^^^^^^^^
				Javadoc: IOException cannot be resolved to a type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test073() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					import java.io.FileNotFoundException;
					public interface IX {
						/**
						 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
						 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
						 */
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 4)
					* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
					          ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception EOFException is not declared
				----------
				2. ERROR in IX.java (at line 5)
					* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
					          ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception FileNotFoundException is not declared
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test074() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					import java.io.FileNotFoundException;
					public interface IX {
						/**
						 * Invalid @throws tags: documented exception are unchecked but some thrown exception are invalid
						 * @throws IllegalAccessException Valid unchecked exception (java.lang.Runtime subclass)
						 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)
						 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)
						 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)
						 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)
						 */
						public void foo() throws
							IllegalAccessException,\s
							InvalidException,\s
							String,\s
							java.io.EOFException,\s
							FileNotFoundException,\s
							IOException;
					}
					""" },
					"""
						----------
						1. ERROR in IX.java (at line 13)
							InvalidException,\s
							^^^^^^^^^^^^^^^^
						InvalidException cannot be resolved to a type
						----------
						2. ERROR in IX.java (at line 14)
							String,\s
							^^^^^^
						No exception of type String can be thrown; an exception type must be a subclass of Throwable
						----------
						3. ERROR in IX.java (at line 15)
							java.io.EOFException,\s
							^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception EOFException
						----------
						4. ERROR in IX.java (at line 16)
							FileNotFoundException,\s
							^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception FileNotFoundException
						----------
						5. ERROR in IX.java (at line 17)
							IOException;
							^^^^^^^^^^^
						IOException cannot be resolved to a type
						----------
						""");
	}

	// @return tag
	public void test080() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Valid return declaration
						 *
						 * @return Return an int
						 */
						public int foo();
					}
					""" });
	}

	public void test081() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Valid empty return declaration
						 *
						 * @return string
						 */
						public String foo();
					}
					""" });
	}

	public void test082() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Missing return declaration
						 */
						public Object[] foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					public Object[] foo();
					       ^^^^^^^^
				Javadoc: Missing tag for return type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test083() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid return declaration
						 *
						 * @return Dimension
						 * @return Duplicated
						 */
						public double foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 6)
					* @return Duplicated
					   ^^^^^^
				Javadoc: Duplicate tag for return type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test084() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid return declaration
						 *
						 * @return Invalid return on void method
						 */
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					* @return Invalid return on void method
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @see tag: string
	public void test090() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid string references\s
						 *
						 * @see "unterminated string
						 * @see "invalid" no text allowed after the string
						 */
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					* @see "unterminated string
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid reference
				----------
				2. ERROR in IX.java (at line 6)
					* @see "invalid" no text allowed after the string
					                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Unexpected text
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test091() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Valid string references\s
						 *
						 * @see "Valid normal string"
						 * @see "Valid \\"string containing\\" \\"double-quote\\""
						 */
						public void foo();
					}
					""" });
	}

	// @see tag: URL
	public void test092() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid URL link references\s
						 *
						 * @see <a
						 * @see <a href="invalid">invalid</a> no text allowed after the href
						 */
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					* @see <a
					       ^^
				Javadoc: Malformed link reference
				----------
				2. ERROR in IX.java (at line 6)
					* @see <a href="invalid">invalid</a> no text allowed after the href
					                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Unexpected text
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @see tag: class references
	public void test095() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
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
						public void foo();
					}
					""" });
	}

	public void test096() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
						/**
						 * Invalid local classes references\s
						 *
						 * @see Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
						 * @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
						 * @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
						 * @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
						 * @see Unknown Invalid ref: unknown class\s
						 */
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 6)
					* @see Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				2. ERROR in test\\IX.java (at line 7)
					* @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				3. ERROR in test\\IX.java (at line 8)
					* @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.Visibility.VcPrivate is not visible
				----------
				4. ERROR in test\\IX.java (at line 9)
					* @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.Visibility.AvcPrivate is not visible
				----------
				5. ERROR in test\\IX.java (at line 10)
					* @see Unknown Invalid ref: unknown class\s
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				""");
	}

	public void test097() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.*;
					public interface IX {
						/**
						 * Valid external classes references\s
						 *
						 * @see VisibilityPublic Valid ref: visible class through import => no warning on import
						 */
						public void foo();
					}
					"""
				}
			);
	}

	public void test098() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.*;
					public interface IX {
						/**
						 * Invalid external classes references\s
						 *
						 * @see VisibilityPackage Invalid ref: non visible class\s
						 * @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
						 */
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 7)
					* @see VisibilityPackage Invalid ref: non visible class\s
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				2. ERROR in test\\IX.java (at line 8)
					* @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				""");
	}

	public void test099() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
						/**
						 * Valid external classes references\s
						 *
						 * @see test.copy.VisibilityPublic Valid ref: visible class through import => no warning on import
						 */
						public void foo();
					}
					"""
			}
		);
	}

	// @see tag: field references
	public void test105() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
						/**
						 * Valid local class field references
						 *
						 * @see Visibility#vf_public Valid ref: visible field
						 * @see Visibility.VcPublic#vf_public Valid ref: visible field in visible inner class
						 */
						public void foo();
					}
					""" });
	}

	public void test106() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
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
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 6)
					* @see #x Invalid ref: non existent field
					        ^
				Javadoc: x cannot be resolved or is not a field
				----------
				2. ERROR in test\\IX.java (at line 7)
					* @see Visibility#unknown Invalid ref: non existent field
					                  ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				3. ERROR in test\\IX.java (at line 8)
					* @see Visibility#vf_private Invalid ref: non visible field
					                  ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				4. ERROR in test\\IX.java (at line 9)
					* @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				5. ERROR in test\\IX.java (at line 10)
					* @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class
					                           ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				6. ERROR in test\\IX.java (at line 11)
					* @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				""");
	}

	public void test107() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.*;
					public interface IX {
						/**
						 * Invalid other package non visible class fields references
						 *
						 * @see VisibilityPublic#vf_public Valid ref to not visible field of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class
						 */
						public void foo();
					}
					"""
			}
		);
	}

	public void test108() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.*;
					public interface IX {
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
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 7)
					* @see VisibilityPackage#unknown Invalid ref to non existent field of other package non visible class
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				2. ERROR in test\\IX.java (at line 8)
					* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class
					                        ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				3. ERROR in test\\IX.java (at line 9)
					* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class
					                        ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				4. ERROR in test\\IX.java (at line 10)
					* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				5. ERROR in test\\IX.java (at line 11)
					* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
					                                 ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				6. ERROR in test\\IX.java (at line 12)
					* @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class
					                                 ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				""");
	}

	// @see method references
	public void test110() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
					import java.util.Vector;
					public interface IX {
						/**
						 * Valid local methods references with array
						 *\s
						 * @see #smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) Valid local method reference
						 * @see #smr_foo(char[], int[][], String[][][], Vector[][][][]) Valid local method reference
						 * @see #smr_foo(char[],int[][],java.lang.String[][][],java.util.Vector[][][][]) Valid local method reference
						 */ \s
						public void foo();
					
						// Empty methods definition for reference
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra);
					}
					""" });
	}

	public void test111() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					import java.util.Vector;
					public interface IX {
						/**
						 * Invalid local methods references with array (non applicable arrays)
						 *\s
						 * @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 */ \s
						public void foo();
					
						// Empty methods definition for reference
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra);
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 6)
					* @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type IX is not applicable for the arguments (char[], int[][], String[][], Vector[][][][])
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test112() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
					import java.util.Vector;
					public interface IX {
						/**
						 * Valid local methods references
						 *\s
						 * @see #smr_foo() Valid local method reference
						 * @see #smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference
						 * @see #smr_foo(java.lang.String, java.lang.String, int) Valid local method reference  \s
						 * @see #smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
						public void foo();
					
						// Empty methods definition for reference
						public void smr_foo();
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);
						public void smr_foo(String str1, java.lang.String str2, int i);
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);
					}
					""" });
	}

	public void test113() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid local methods references
						 *\s
						 * @see #unknown() Invalid ref: undefined local method reference
						 */ \s
						public void foo();
					
						// Empty methods definition for reference
						public void smr_foo();
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					* @see #unknown() Invalid ref: undefined local method reference
					        ^^^^^^^
				Javadoc: The method unknown() is undefined for the type IX
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test114() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"""
					public interface IX {
						/**
						 * Invalid local methods references
						 *\s
						 * @see #smr_foo(int) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable
						 * @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean) Invalid ref: local method not applicable
						 * @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
						 */ \s
						public void foo();
					
						// Empty methods definition for reference
						public void smr_foo();
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);
						public void smr_foo(String str1, java.lang.String str2, int i);
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);
					}
					""" },
			"""
				----------
				1. ERROR in IX.java (at line 5)
					* @see #smr_foo(int) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo() in the type IX is not applicable for the arguments (int)
				----------
				2. ERROR in IX.java (at line 6)
					* @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type IX is not applicable for the arguments (float, long, char, short, byte, int, boolean)
				----------
				3. ERROR in IX.java (at line 7)
					* @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type IX is not applicable for the arguments (String, String, int, String)
				----------
				4. ERROR in IX.java (at line 8)
					* @see #smr_foo(boolean) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type IX is not applicable for the arguments (boolean)
				----------
				5. ERROR in IX.java (at line 9)
					* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
					                ^^^^^^^^^
				Javadoc: Hashtable cannot be resolved to a type
				----------
				6. ERROR in IX.java (at line 9)
					* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
					                             ^^^^^^
				Javadoc: Vector cannot be resolved to a type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test115() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"""
					import java.util.Vector;
						/**
						 * Valid local methods references
						 *\s
						 * @see IX#smr_foo() Valid local method reference
						 * @see IX#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference
						 * @see IX#smr_foo(String x, java.lang.String y, int z) Valid local method reference  \s
						 * @see IX#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
					public interface IX {
						public void smr_foo();
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);
						public void smr_foo(String str1, java.lang.String str2, int i);
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);
					}
					""" });
	}

	public void test116() {
		this.runConformTest(
			new String[] {
				"test/deep/qualified/name/p/IX.java",
				"""
					package test.deep.qualified.name.p;
					import java.util.Vector;
					public interface IX {
						/**
						 * Valid local methods references
						 *\s
						 * @see test.deep.qualified.name.p.IX#smr_foo() Valid local method reference
						 * @see test.deep.qualified.name.p.IX#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference
						 * @see test.deep.qualified.name.p.IX#smr_foo(String x, java.lang.String y, int z) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.IX#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
						public void smr_foo();
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);
						public void smr_foo(String str1, java.lang.String str2, int i);
						public void smr_foo(java.util.Hashtable h, Vector v, boolean b);
					}
					""" });
	}

	public void test117() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
						/**
						 * Valid package class methods references
						 *\s
						 * @see Visibility#vm_public() Valid ref: visible method
						 * @see Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class
						 * @see test.Visibility#vm_public() Valid ref: visible method
						 * @see test.Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class
						 */ \s
						public void foo();
					}
					""" });
	}

	public void test118() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
						/**
						 * Invalid package class methods references (non-existence)
						 *\s
						 * @see Visibility#unknown() Invalid ref: non-existent method
						 * @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class
						 * @see Unknown#vm_public() Invalid ref: non-existent class
						 * @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class
						 */ \s
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 6)
					* @see Visibility#unknown() Invalid ref: non-existent method
					                  ^^^^^^^
				Javadoc: The method unknown() is undefined for the type Visibility
				----------
				2. ERROR in test\\IX.java (at line 7)
					* @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class
					                           ^^^^^^^
				Javadoc: The method unknown() is undefined for the type Visibility.VcPublic
				----------
				3. ERROR in test\\IX.java (at line 8)
					* @see Unknown#vm_public() Invalid ref: non-existent class
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				4. ERROR in test\\IX.java (at line 9)
					* @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class
					       ^^^^^^^^^^^^^^^^^^
				Javadoc: Visibility.Unknown cannot be resolved to a type
				----------
				""");
	}

	public void test119() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
						/**
						 * Invalid package class methods references (non-visible)
						 *\s
						 * @see Visibility#vm_private() Invalid ref: non-visible method
						 * @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)
						 * @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class
						 */ \s
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 6)
					* @see Visibility#vm_private() Invalid ref: non-visible method
					                  ^^^^^^^^^^
				Javadoc: The method vm_private() from the type Visibility is not visible
				----------
				2. ERROR in test\\IX.java (at line 7)
					* @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				3. ERROR in test\\IX.java (at line 8)
					* @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The method vm_private() from the type Visibility.VcPublic is not visible
				----------
				""");
	}

	public void test120() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
						/**
						 * Invalid package class methods references (non-applicable)
						 *\s
						 * @see Visibility#vm_private(int) Invalid ref: non-applicable method
						 * @see Visibility#vm_public(String) Invalid ref: non-applicable method
						 * @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class
						 * @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class
						 */ \s
						public void foo();
					}
					""" },
			"""
				----------
				1. ERROR in test\\IX.java (at line 6)
					* @see Visibility#vm_private(int) Invalid ref: non-applicable method
					                  ^^^^^^^^^^
				Javadoc: The method vm_private() in the type Visibility is not applicable for the arguments (int)
				----------
				2. ERROR in test\\IX.java (at line 7)
					* @see Visibility#vm_public(String) Invalid ref: non-applicable method
					                  ^^^^^^^^^
				Javadoc: The method vm_public() in the type Visibility is not applicable for the arguments (String)
				----------
				3. ERROR in test\\IX.java (at line 8)
					* @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The method vm_private() in the type Visibility.VcPublic is not applicable for the arguments (Integer, byte)
				----------
				4. ERROR in test\\IX.java (at line 9)
					* @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class
					                           ^^^^^^^^^
				Javadoc: The method vm_public() in the type Visibility.VcPublic is not applicable for the arguments (Double, Boolean)
				----------
				""");
	}

	public void test121() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.*;
					public interface IX {
						/**
						 * Invalid other package non visible class methods references (non existent/visible arguments)
						 *\s
						 * @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
						 * @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
						 */
						public void foo();
					}
					""" },
			"""
				----------
				1. WARNING in test\\IX.java (at line 2)
					import test.copy.*;
					       ^^^^^^^^^
				The import test.copy is never used
				----------
				2. ERROR in test\\IX.java (at line 7)
					* @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				3. ERROR in test\\IX.java (at line 8)
					* @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				""");
	}

	public void test122() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					import test.copy.VisibilityPublic;
					public interface IX {
						/**
						 * Valid other package visible class methods references\s
						 *\s
						 * @see VisibilityPublic#vm_public() Valid ref to not visible method of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class
						 */
						public void foo();
					}
					"""
			}
		);
	}

	public void test123() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"""
					package test;
					public interface IX {
						/**
						 * Valid other package visible class methods references\s
						 *\s
						 * @see test.copy.VisibilityPublic#vm_public() Valid ref to not visible method of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class
						 */
						public void foo();
					}
					""" });
	}
}
