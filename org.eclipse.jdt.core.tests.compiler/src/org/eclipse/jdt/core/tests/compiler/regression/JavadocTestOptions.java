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
 *     Stephan Herrmann - Contribution for
 *								Bug 425721 - [1.8][compiler] Nondeterministic results in GenericsRegressionTest_1_8.testBug424195a
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Tests to verify that Compiler options work well for Javadoc.
 * This class does not tests syntax error option as it's considered already
 * tested by other JavadocTest* classes.
 *
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=46854"
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=46976"
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTestOptions extends JavadocTest {

	/**
	 * When failures occur in this file due to changes in error messages,
	 * then uncomment following static initializer.
	 * This will output in the console the changes which must be done
	 * on {@link #CLASSES_ERRORS} and/or {@link #METHODS_ERRORS}
	 * arrays to make the failing tests pass.
	 */
//	static {
//		TESTS_NAMES = new String[] {
//			"testInvalidTagsClassErrorTagsPrivate",
//			"testInvalidTagsFieldErrorTagsPrivate",
//			"testInvalidTagsMethodErrorTagsPrivate",
//			"testInvalidTagsConstructorErrorTagsPrivate",
//		};
//	}

	String docCommentSupport = null;
	String reportInvalidJavadoc = null;
	String reportInvalidJavadocTagsVisibility = null;
	String reportInvalidJavadocTags = null;
	String reportInvalidJavadocTagsDeprecatedRef= null;
	String reportInvalidJavadocTagsNotVisibleRef = null;
	String reportMissingJavadocTags = null;
	String reportMissingJavadocTagsVisibility = null;
	String reportMissingJavadocTagsOverriding = null;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;
	String reportMissingJavadocCommentsOverriding = null;

	private static final int PUBLIC_VISIBILITY = 0;
	private static final int PROTECTED_VISIBILITY = 1;
	private static final int DEFAULT_VISIBILITY = 2;
	private static final int PRIVATE_VISIBILITY = 3;

	private static final String INVALID_CLASS_JAVADOC_REF = """
			/**
			 * @see X_dep
			 * @see X.X_priv
			 * @see X.Unknown
			 * @see X#X(int)
			 * @see X#X(String)
			 * @see X#X()
			 * @see X#x_dep
			 * @see X#x_priv
			 * @see X#unknown
			 * @see X#foo_dep()
			 * @see X#foo_priv()
			 * @see X#foo_dep(String)
			 * @see X#unknown()
			 */
		""";
	private static final String INVALID_METHOD_JAVADOC_REF = """
			/**
			 * @param str
			 * @param str
			 * @param xxx
			 * @throws IllegalArgumentException
			 * @throws IllegalArgumentException
			 * @throws java.io.IOException
			 * @throws Unknown
			 * @see X_dep
			 * @see X.X_priv
			 * @see X.Unknown
			 * @see X#X(int)
			 * @see X#X(String)
			 * @see X#X()
			 * @see X#x_dep
			 * @see X#x_priv
			 * @see X#unknown
			 * @see X#foo_dep()
			 * @see X#foo_priv()
			 * @see X#foo_dep(String)
			 * @see X#unknown()
			 */
		""";
	private static final String DEP_CLASS =
		"/** @deprecated */\n" +
		"public class X_dep {}\n";
	private static final String REF_CLASS =
		"public class X {\n" +
// Deprecated class must be a top level to avoid visibility issue
//		"	/** @deprecated */\n" +
//		"	class X_dep{}\n" +
		"	private class X_priv{}\n" +
		"	/** @deprecated */\n" +
		"	public int x_dep;\n" +
		"	private int x_priv;\n" +
		"	/** @deprecated */\n" +
		"	public X() {}\n" +
		"	private X(int x) {}\n" +
		"	/** @deprecated */\n" +
		"	public void foo_dep() {}\n" +
		"	private void foo_priv() {}\n" +
		"	}\n";
	private static final String[] CLASSES_INVALID_COMMENT = {
		"X.java",
		REF_CLASS,
		"X_dep.java",
		DEP_CLASS,
		"Y.java",
		"public class Y {\n" +
			INVALID_CLASS_JAVADOC_REF +
			"	public class X_pub {}\n" +
			INVALID_CLASS_JAVADOC_REF +
			"	protected class X_prot {}\n" +
			INVALID_CLASS_JAVADOC_REF +
			"	class X_pack {}\n" +
			INVALID_CLASS_JAVADOC_REF +
			"	private class X_priv {}\n" +
			"}\n" +
			"\n"
	};
	private static final String[] FIELDS_INVALID_COMMENT = {
		"X.java",
		REF_CLASS,
		"X_dep.java",
		DEP_CLASS,
		"Y.java",
		"public class Y {\n" +
			INVALID_CLASS_JAVADOC_REF +
			"	public int x_pub;\n" +
			INVALID_CLASS_JAVADOC_REF +
			"	protected int x_prot;\n" +
			INVALID_CLASS_JAVADOC_REF +
			"	int x_pack;\n" +
			INVALID_CLASS_JAVADOC_REF+
			"	private int x_priv;\n" +
			"}\n" +
			"\n"
	};
	private static final String[] METHODS_INVALID_COMMENT = {
		"X.java",
		REF_CLASS,
		"X_dep.java",
		DEP_CLASS,
		"Y.java",
		"public class Y {\n" +
			INVALID_METHOD_JAVADOC_REF +
			"	public void foo_pub(String str) throws IllegalArgumentException {}\n" +
			INVALID_METHOD_JAVADOC_REF +
			"	protected void foo_pro(String str) throws IllegalArgumentException {}\n" +
			INVALID_METHOD_JAVADOC_REF +
			"	void foo_pack(String str) throws IllegalArgumentException {}\n" +
			INVALID_METHOD_JAVADOC_REF +
			"	private void foo_priv(String str) throws IllegalArgumentException {}\n" +
			"}\n" +
			"\n"
	};
	private static final String[] CONSTRUCTORS_INVALID_COMMENT = {
		"X.java",
		REF_CLASS,
		"X_dep.java",
		DEP_CLASS,
		"Y.java",
		"public class Y {\n" +
			INVALID_METHOD_JAVADOC_REF +
			"	public Y(int str) {}\n" +
			INVALID_METHOD_JAVADOC_REF +
			"	protected Y(long str) {}\n" +
			INVALID_METHOD_JAVADOC_REF +
			"	Y(float str) {}\n" +
			INVALID_METHOD_JAVADOC_REF +
			"	private Y(double str) {}\n" +
			"}\n" +
			"\n"
	};
	private static final String[] MISSING_TAGS = {
		"X.java",
		"""
			public class X {
				// public
				/** */
				public class PublicClass {}
				/** */
				public int publicField;
				/** */
				public X(int i) {}
				/** */
				public int publicMethod(long l) { return 0;}
				// protected
				/** */
				protected class ProtectedClass {}
				/** */
				protected int protectedField;
				/** */
				protected X(long l) {}
				/** */
				protected int protectedMethod(long l) { return 0; }
				// default
				/** */
				class PackageClass {}
				/** */
				int packageField;
				/** */
				X(float f) {}
				/** */
				int packageMethod(long l) { return 0;}
				// private
				/** */
				private class PrivateClass {}
				/** */
				private int privateField;
				/** */
				private X(double d) {}
				/** */
				private int privateMethod(long l) { return 0;}
			}
			
			""",
		"Y.java",
		"""
			/** */
			public class Y extends X {
				public Y(int i) { super(i); }
				//methods
				/** */
				public int publicMethod(long l) { return 0;}
				/** */
				protected int protectedMethod(long l) { return 0;}
				/** */
				int packageMethod(long l) { return 0;}
				/** */
				private int privateMethod(long l) { return 0;}
			}
			"""
	};
	private static final String[] MISSING_COMMENTS = {
		"X.java",
		"""
			/** */
			public class X {
				// public
				public class PublicClass {}
				public int publicField;
				public X(int i) {}
				public int publicMethod(long l) { return 0;}
				// protected
				protected class ProtectedClass {}
				protected int protectedField;
				protected X(long l) {}
				protected int protectedMethod(long l) { return 0; }
				// default
				class PackageClass {}
				int packageField;
				X(float f) {}
				int packageMethod(long l) { return 0;}
				// private
				private class PrivateClass {}
				private int privateField;
				private X(double d) {}
				private int privateMethod(long l) { return 0;}
			}
			
			""",
		"Y.java",
		"""
			/** */
			public class Y extends X {
				/** */
				public Y(int i) { super(i); }
				public int publicMethod(long l) { return 0;}
				protected int protectedMethod(long l) { return 0;}
				int packageMethod(long l) { return 0;}
				private int privateMethod(long l) { return 0;}
			}
			"""
	};

	private static final String[] CLASSES_ERRORS = {
		"""
			1. ERROR in Y.java (at line 3)
				* @see X_dep
				       ^^^^^
			Javadoc: The type X_dep is deprecated
			----------
			2. ERROR in Y.java (at line 4)
				* @see X.X_priv
				       ^^^^^^^^
			Javadoc: The type X.X_priv is not visible
			----------
			3. ERROR in Y.java (at line 5)
				* @see X.Unknown
				       ^^^^^^^^^
			Javadoc: X.Unknown cannot be resolved to a type
			----------
			4. ERROR in Y.java (at line 6)
				* @see X#X(int)
				         ^^^^^^
			Javadoc: The constructor X(int) is not visible
			----------
			5. ERROR in Y.java (at line 7)
				* @see X#X(String)
				         ^^^^^^^^^
			Javadoc: The constructor X(String) is undefined
			----------
			6. ERROR in Y.java (at line 8)
				* @see X#X()
				         ^^^
			Javadoc: The constructor X() is deprecated
			----------
			7. ERROR in Y.java (at line 9)
				* @see X#x_dep
				         ^^^^^
			Javadoc: The field X.x_dep is deprecated
			----------
			8. ERROR in Y.java (at line 10)
				* @see X#x_priv
				         ^^^^^^
			Javadoc: The field x_priv is not visible
			----------
			9. ERROR in Y.java (at line 11)
				* @see X#unknown
				         ^^^^^^^
			Javadoc: unknown cannot be resolved or is not a field
			----------
			10. ERROR in Y.java (at line 12)
				* @see X#foo_dep()
				         ^^^^^^^^^
			Javadoc: The method foo_dep() from the type X is deprecated
			----------
			11. ERROR in Y.java (at line 13)
				* @see X#foo_priv()
				         ^^^^^^^^
			Javadoc: The method foo_priv() from the type X is not visible
			----------
			12. ERROR in Y.java (at line 14)
				* @see X#foo_dep(String)
				         ^^^^^^^
			Javadoc: The method foo_dep() in the type X is not applicable for the arguments (String)
			----------
			13. ERROR in Y.java (at line 15)
				* @see X#unknown()
				         ^^^^^^^
			Javadoc: The method unknown() is undefined for the type X
			----------
			""",
		"""
			14. ERROR in Y.java (at line 19)
				* @see X_dep
				       ^^^^^
			Javadoc: The type X_dep is deprecated
			----------
			15. ERROR in Y.java (at line 20)
				* @see X.X_priv
				       ^^^^^^^^
			Javadoc: The type X.X_priv is not visible
			----------
			16. ERROR in Y.java (at line 21)
				* @see X.Unknown
				       ^^^^^^^^^
			Javadoc: X.Unknown cannot be resolved to a type
			----------
			17. ERROR in Y.java (at line 22)
				* @see X#X(int)
				         ^^^^^^
			Javadoc: The constructor X(int) is not visible
			----------
			18. ERROR in Y.java (at line 23)
				* @see X#X(String)
				         ^^^^^^^^^
			Javadoc: The constructor X(String) is undefined
			----------
			19. ERROR in Y.java (at line 24)
				* @see X#X()
				         ^^^
			Javadoc: The constructor X() is deprecated
			----------
			20. ERROR in Y.java (at line 25)
				* @see X#x_dep
				         ^^^^^
			Javadoc: The field X.x_dep is deprecated
			----------
			21. ERROR in Y.java (at line 26)
				* @see X#x_priv
				         ^^^^^^
			Javadoc: The field x_priv is not visible
			----------
			22. ERROR in Y.java (at line 27)
				* @see X#unknown
				         ^^^^^^^
			Javadoc: unknown cannot be resolved or is not a field
			----------
			23. ERROR in Y.java (at line 28)
				* @see X#foo_dep()
				         ^^^^^^^^^
			Javadoc: The method foo_dep() from the type X is deprecated
			----------
			24. ERROR in Y.java (at line 29)
				* @see X#foo_priv()
				         ^^^^^^^^
			Javadoc: The method foo_priv() from the type X is not visible
			----------
			25. ERROR in Y.java (at line 30)
				* @see X#foo_dep(String)
				         ^^^^^^^
			Javadoc: The method foo_dep() in the type X is not applicable for the arguments (String)
			----------
			26. ERROR in Y.java (at line 31)
				* @see X#unknown()
				         ^^^^^^^
			Javadoc: The method unknown() is undefined for the type X
			----------
			""",
		"""
			27. ERROR in Y.java (at line 35)
				* @see X_dep
				       ^^^^^
			Javadoc: The type X_dep is deprecated
			----------
			28. ERROR in Y.java (at line 36)
				* @see X.X_priv
				       ^^^^^^^^
			Javadoc: The type X.X_priv is not visible
			----------
			29. ERROR in Y.java (at line 37)
				* @see X.Unknown
				       ^^^^^^^^^
			Javadoc: X.Unknown cannot be resolved to a type
			----------
			30. ERROR in Y.java (at line 38)
				* @see X#X(int)
				         ^^^^^^
			Javadoc: The constructor X(int) is not visible
			----------
			31. ERROR in Y.java (at line 39)
				* @see X#X(String)
				         ^^^^^^^^^
			Javadoc: The constructor X(String) is undefined
			----------
			32. ERROR in Y.java (at line 40)
				* @see X#X()
				         ^^^
			Javadoc: The constructor X() is deprecated
			----------
			33. ERROR in Y.java (at line 41)
				* @see X#x_dep
				         ^^^^^
			Javadoc: The field X.x_dep is deprecated
			----------
			34. ERROR in Y.java (at line 42)
				* @see X#x_priv
				         ^^^^^^
			Javadoc: The field x_priv is not visible
			----------
			35. ERROR in Y.java (at line 43)
				* @see X#unknown
				         ^^^^^^^
			Javadoc: unknown cannot be resolved or is not a field
			----------
			36. ERROR in Y.java (at line 44)
				* @see X#foo_dep()
				         ^^^^^^^^^
			Javadoc: The method foo_dep() from the type X is deprecated
			----------
			37. ERROR in Y.java (at line 45)
				* @see X#foo_priv()
				         ^^^^^^^^
			Javadoc: The method foo_priv() from the type X is not visible
			----------
			38. ERROR in Y.java (at line 46)
				* @see X#foo_dep(String)
				         ^^^^^^^
			Javadoc: The method foo_dep() in the type X is not applicable for the arguments (String)
			----------
			39. ERROR in Y.java (at line 47)
				* @see X#unknown()
				         ^^^^^^^
			Javadoc: The method unknown() is undefined for the type X
			----------
			""",
		"""
			40. ERROR in Y.java (at line 51)
				* @see X_dep
				       ^^^^^
			Javadoc: The type X_dep is deprecated
			----------
			41. ERROR in Y.java (at line 52)
				* @see X.X_priv
				       ^^^^^^^^
			Javadoc: The type X.X_priv is not visible
			----------
			42. ERROR in Y.java (at line 53)
				* @see X.Unknown
				       ^^^^^^^^^
			Javadoc: X.Unknown cannot be resolved to a type
			----------
			43. ERROR in Y.java (at line 54)
				* @see X#X(int)
				         ^^^^^^
			Javadoc: The constructor X(int) is not visible
			----------
			44. ERROR in Y.java (at line 55)
				* @see X#X(String)
				         ^^^^^^^^^
			Javadoc: The constructor X(String) is undefined
			----------
			45. ERROR in Y.java (at line 56)
				* @see X#X()
				         ^^^
			Javadoc: The constructor X() is deprecated
			----------
			46. ERROR in Y.java (at line 57)
				* @see X#x_dep
				         ^^^^^
			Javadoc: The field X.x_dep is deprecated
			----------
			47. ERROR in Y.java (at line 58)
				* @see X#x_priv
				         ^^^^^^
			Javadoc: The field x_priv is not visible
			----------
			48. ERROR in Y.java (at line 59)
				* @see X#unknown
				         ^^^^^^^
			Javadoc: unknown cannot be resolved or is not a field
			----------
			49. ERROR in Y.java (at line 60)
				* @see X#foo_dep()
				         ^^^^^^^^^
			Javadoc: The method foo_dep() from the type X is deprecated
			----------
			50. ERROR in Y.java (at line 61)
				* @see X#foo_priv()
				         ^^^^^^^^
			Javadoc: The method foo_priv() from the type X is not visible
			----------
			51. ERROR in Y.java (at line 62)
				* @see X#foo_dep(String)
				         ^^^^^^^
			Javadoc: The method foo_dep() in the type X is not applicable for the arguments (String)
			----------
			52. ERROR in Y.java (at line 63)
				* @see X#unknown()
				         ^^^^^^^
			Javadoc: The method unknown() is undefined for the type X
			----------
			"""
	};

	private static final String[] METHODS_ERRORS = {
			"""
				1. ERROR in Y.java (at line 4)
					* @param str
					         ^^^
				Javadoc: Duplicate tag for parameter
				----------
				2. ERROR in Y.java (at line 5)
					* @param xxx
					         ^^^
				Javadoc: Parameter xxx is not declared
				----------
				3. ERROR in Y.java (at line 8)
					* @throws java.io.IOException
					          ^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception IOException is not declared
				----------
				4. ERROR in Y.java (at line 9)
					* @throws Unknown
					          ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				5. ERROR in Y.java (at line 10)
					* @see X_dep
					       ^^^^^
				Javadoc: The type X_dep is deprecated
				----------
				6. ERROR in Y.java (at line 11)
					* @see X.X_priv
					       ^^^^^^^^
				Javadoc: The type X.X_priv is not visible
				----------
				7. ERROR in Y.java (at line 12)
					* @see X.Unknown
					       ^^^^^^^^^
				Javadoc: X.Unknown cannot be resolved to a type
				----------
				8. ERROR in Y.java (at line 13)
					* @see X#X(int)
					         ^^^^^^
				Javadoc: The constructor X(int) is not visible
				----------
				9. ERROR in Y.java (at line 14)
					* @see X#X(String)
					         ^^^^^^^^^
				Javadoc: The constructor X(String) is undefined
				----------
				10. ERROR in Y.java (at line 15)
					* @see X#X()
					         ^^^
				Javadoc: The constructor X() is deprecated
				----------
				11. ERROR in Y.java (at line 16)
					* @see X#x_dep
					         ^^^^^
				Javadoc: The field X.x_dep is deprecated
				----------
				12. ERROR in Y.java (at line 17)
					* @see X#x_priv
					         ^^^^^^
				Javadoc: The field x_priv is not visible
				----------
				13. ERROR in Y.java (at line 18)
					* @see X#unknown
					         ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				14. ERROR in Y.java (at line 19)
					* @see X#foo_dep()
					         ^^^^^^^^^
				Javadoc: The method foo_dep() from the type X is deprecated
				----------
				15. ERROR in Y.java (at line 20)
					* @see X#foo_priv()
					         ^^^^^^^^
				Javadoc: The method foo_priv() from the type X is not visible
				----------
				16. ERROR in Y.java (at line 21)
					* @see X#foo_dep(String)
					         ^^^^^^^
				Javadoc: The method foo_dep() in the type X is not applicable for the arguments (String)
				----------
				17. ERROR in Y.java (at line 22)
					* @see X#unknown()
					         ^^^^^^^
				Javadoc: The method unknown() is undefined for the type X
				----------
				""",
			"""
				18. ERROR in Y.java (at line 27)
					* @param str
					         ^^^
				Javadoc: Duplicate tag for parameter
				----------
				19. ERROR in Y.java (at line 28)
					* @param xxx
					         ^^^
				Javadoc: Parameter xxx is not declared
				----------
				20. ERROR in Y.java (at line 31)
					* @throws java.io.IOException
					          ^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception IOException is not declared
				----------
				21. ERROR in Y.java (at line 32)
					* @throws Unknown
					          ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				22. ERROR in Y.java (at line 33)
					* @see X_dep
					       ^^^^^
				Javadoc: The type X_dep is deprecated
				----------
				23. ERROR in Y.java (at line 34)
					* @see X.X_priv
					       ^^^^^^^^
				Javadoc: The type X.X_priv is not visible
				----------
				24. ERROR in Y.java (at line 35)
					* @see X.Unknown
					       ^^^^^^^^^
				Javadoc: X.Unknown cannot be resolved to a type
				----------
				25. ERROR in Y.java (at line 36)
					* @see X#X(int)
					         ^^^^^^
				Javadoc: The constructor X(int) is not visible
				----------
				26. ERROR in Y.java (at line 37)
					* @see X#X(String)
					         ^^^^^^^^^
				Javadoc: The constructor X(String) is undefined
				----------
				27. ERROR in Y.java (at line 38)
					* @see X#X()
					         ^^^
				Javadoc: The constructor X() is deprecated
				----------
				28. ERROR in Y.java (at line 39)
					* @see X#x_dep
					         ^^^^^
				Javadoc: The field X.x_dep is deprecated
				----------
				29. ERROR in Y.java (at line 40)
					* @see X#x_priv
					         ^^^^^^
				Javadoc: The field x_priv is not visible
				----------
				30. ERROR in Y.java (at line 41)
					* @see X#unknown
					         ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				31. ERROR in Y.java (at line 42)
					* @see X#foo_dep()
					         ^^^^^^^^^
				Javadoc: The method foo_dep() from the type X is deprecated
				----------
				32. ERROR in Y.java (at line 43)
					* @see X#foo_priv()
					         ^^^^^^^^
				Javadoc: The method foo_priv() from the type X is not visible
				----------
				33. ERROR in Y.java (at line 44)
					* @see X#foo_dep(String)
					         ^^^^^^^
				Javadoc: The method foo_dep() in the type X is not applicable for the arguments (String)
				----------
				34. ERROR in Y.java (at line 45)
					* @see X#unknown()
					         ^^^^^^^
				Javadoc: The method unknown() is undefined for the type X
				----------
				""",
			"""
				35. ERROR in Y.java (at line 50)
					* @param str
					         ^^^
				Javadoc: Duplicate tag for parameter
				----------
				36. ERROR in Y.java (at line 51)
					* @param xxx
					         ^^^
				Javadoc: Parameter xxx is not declared
				----------
				37. ERROR in Y.java (at line 54)
					* @throws java.io.IOException
					          ^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception IOException is not declared
				----------
				38. ERROR in Y.java (at line 55)
					* @throws Unknown
					          ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				39. ERROR in Y.java (at line 56)
					* @see X_dep
					       ^^^^^
				Javadoc: The type X_dep is deprecated
				----------
				40. ERROR in Y.java (at line 57)
					* @see X.X_priv
					       ^^^^^^^^
				Javadoc: The type X.X_priv is not visible
				----------
				41. ERROR in Y.java (at line 58)
					* @see X.Unknown
					       ^^^^^^^^^
				Javadoc: X.Unknown cannot be resolved to a type
				----------
				42. ERROR in Y.java (at line 59)
					* @see X#X(int)
					         ^^^^^^
				Javadoc: The constructor X(int) is not visible
				----------
				43. ERROR in Y.java (at line 60)
					* @see X#X(String)
					         ^^^^^^^^^
				Javadoc: The constructor X(String) is undefined
				----------
				44. ERROR in Y.java (at line 61)
					* @see X#X()
					         ^^^
				Javadoc: The constructor X() is deprecated
				----------
				45. ERROR in Y.java (at line 62)
					* @see X#x_dep
					         ^^^^^
				Javadoc: The field X.x_dep is deprecated
				----------
				46. ERROR in Y.java (at line 63)
					* @see X#x_priv
					         ^^^^^^
				Javadoc: The field x_priv is not visible
				----------
				47. ERROR in Y.java (at line 64)
					* @see X#unknown
					         ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				48. ERROR in Y.java (at line 65)
					* @see X#foo_dep()
					         ^^^^^^^^^
				Javadoc: The method foo_dep() from the type X is deprecated
				----------
				49. ERROR in Y.java (at line 66)
					* @see X#foo_priv()
					         ^^^^^^^^
				Javadoc: The method foo_priv() from the type X is not visible
				----------
				50. ERROR in Y.java (at line 67)
					* @see X#foo_dep(String)
					         ^^^^^^^
				Javadoc: The method foo_dep() in the type X is not applicable for the arguments (String)
				----------
				51. ERROR in Y.java (at line 68)
					* @see X#unknown()
					         ^^^^^^^
				Javadoc: The method unknown() is undefined for the type X
				----------
				""",
			"""
				52. ERROR in Y.java (at line 73)
					* @param str
					         ^^^
				Javadoc: Duplicate tag for parameter
				----------
				53. ERROR in Y.java (at line 74)
					* @param xxx
					         ^^^
				Javadoc: Parameter xxx is not declared
				----------
				54. ERROR in Y.java (at line 77)
					* @throws java.io.IOException
					          ^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception IOException is not declared
				----------
				55. ERROR in Y.java (at line 78)
					* @throws Unknown
					          ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				56. ERROR in Y.java (at line 79)
					* @see X_dep
					       ^^^^^
				Javadoc: The type X_dep is deprecated
				----------
				57. ERROR in Y.java (at line 80)
					* @see X.X_priv
					       ^^^^^^^^
				Javadoc: The type X.X_priv is not visible
				----------
				58. ERROR in Y.java (at line 81)
					* @see X.Unknown
					       ^^^^^^^^^
				Javadoc: X.Unknown cannot be resolved to a type
				----------
				59. ERROR in Y.java (at line 82)
					* @see X#X(int)
					         ^^^^^^
				Javadoc: The constructor X(int) is not visible
				----------
				60. ERROR in Y.java (at line 83)
					* @see X#X(String)
					         ^^^^^^^^^
				Javadoc: The constructor X(String) is undefined
				----------
				61. ERROR in Y.java (at line 84)
					* @see X#X()
					         ^^^
				Javadoc: The constructor X() is deprecated
				----------
				62. ERROR in Y.java (at line 85)
					* @see X#x_dep
					         ^^^^^
				Javadoc: The field X.x_dep is deprecated
				----------
				63. ERROR in Y.java (at line 86)
					* @see X#x_priv
					         ^^^^^^
				Javadoc: The field x_priv is not visible
				----------
				64. ERROR in Y.java (at line 87)
					* @see X#unknown
					         ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				65. ERROR in Y.java (at line 88)
					* @see X#foo_dep()
					         ^^^^^^^^^
				Javadoc: The method foo_dep() from the type X is deprecated
				----------
				66. ERROR in Y.java (at line 89)
					* @see X#foo_priv()
					         ^^^^^^^^
				Javadoc: The method foo_priv() from the type X is not visible
				----------
				67. ERROR in Y.java (at line 90)
					* @see X#foo_dep(String)
					         ^^^^^^^
				Javadoc: The method foo_dep() in the type X is not applicable for the arguments (String)
				----------
				68. ERROR in Y.java (at line 91)
					* @see X#unknown()
					         ^^^^^^^
				Javadoc: The method unknown() is undefined for the type X
				----------
				""",
	};

	private String resultForInvalidTagsClassOrField(int visibility) {
		String[] errors = errorsForInvalidTagsClassOrField(visibility);
		int length = errors.length;
		StringBuilder buffer = new StringBuilder("----------\n");
		for (int i=0; i<length; i++) {
			buffer.append(errors[i]);
		}
		return buffer.toString();
	}

	private String resultForInvalidTagsMethodOrConstructor(int visibility) {
		String[] errors = errorsForInvalidTagsMethodOrConstructor(visibility);
		int length = errors.length;
		StringBuilder buffer = new StringBuilder("----------\n");
		for (int i=0; i<length; i++) {
			buffer.append(errors[i]);
		}
		return buffer.toString();
	}

	private String[] errorsForInvalidTagsClassOrField(int visibility) {
		int length = CLASSES_ERRORS.length;
		int size = visibility+1;
		if (this.reportInvalidJavadocTagsDeprecatedRef == null && this.reportInvalidJavadocTagsNotVisibleRef == null) {
			if (size == length) return CLASSES_ERRORS;
			String[] errors = new String[size];
			System.arraycopy(CLASSES_ERRORS, 0, errors, 0, size);
			return errors;
		}
		String[] errors = new String[size];
		for (int i=0, count=1; i<size; i++) {
			StringBuilder buffer = new StringBuilder();
			StringTokenizer tokenizer = new StringTokenizer(CLASSES_ERRORS[i], "\n");
			while (tokenizer.hasMoreTokens()) {
				StringBuilder error = new StringBuilder();
				boolean add = true;
				for (int j=0; j<5; j++) {
					String token = tokenizer.nextToken();
					switch (j) {
						case 0:
							error.append(count);
							error.append(token.substring(token.indexOf('.')));
							break;
						case 3:
							// may not want to add the error message in certain circumstances
							if (CompilerOptions.DISABLED.equals(this.reportInvalidJavadocTagsDeprecatedRef)) {
								add = token.indexOf("is deprecated") == -1;
							}
							if (add && CompilerOptions.DISABLED.equals(this.reportInvalidJavadocTagsNotVisibleRef)) {
								add = token.indexOf("is not visible") == -1 && token.indexOf("visibility for malformed doc comments") == -1;
							}
							// $FALL-THROUGH$ - fall through next case to append the token to the error message
						default:
							error.append(token);
					}
					error.append('\n');
				}
				if (add) {
					count++;
					buffer.append(error);
				}
			}
			errors[i] = buffer.toString();
		}
		return errors;
	}

	private String[] errorsForInvalidTagsMethodOrConstructor(int visibility) {
		int length = METHODS_ERRORS.length;
		int size = visibility+1;
		if (this.reportInvalidJavadocTagsDeprecatedRef == null && this.reportInvalidJavadocTagsNotVisibleRef == null) {
			if (size == length) return METHODS_ERRORS;
			String[] errors = new String[size];
			System.arraycopy(METHODS_ERRORS, 0, errors, 0, size);
			return errors;
		}
		String[] errors = new String[size];
		for (int i=0, count=1; i<size; i++) {
			StringBuilder buffer = new StringBuilder();
			StringTokenizer tokenizer = new StringTokenizer(METHODS_ERRORS[i], "\n");
			while (tokenizer.hasMoreTokens()) {
				StringBuilder error = new StringBuilder();
				boolean add = true;
				for (int j=0; j<5; j++) {
					String token = tokenizer.nextToken();
					switch (j) {
						case 0:
							error.append(count);
							error.append(token.substring(token.indexOf('.')));
							break;
						case 3:
							// may not want to add the error message in certain circumstances
							if (CompilerOptions.DISABLED.equals(this.reportInvalidJavadocTagsDeprecatedRef)) {
								add = token.indexOf("is deprecated") == -1;
							}
							if (add && CompilerOptions.DISABLED.equals(this.reportInvalidJavadocTagsNotVisibleRef)) {
								add = token.indexOf("is not visible") == -1 && token.indexOf("visibility for malformed doc comments") == -1;
							}
							// $FALL-THROUGH$ - fall through next case to append the token to the error message
						default:
							error.append(token);
					}
					error.append('\n');
				}
				if (add) {
					count++;
					buffer.append(error);
				}
			}
			errors[i] = buffer.toString();
		}
		return errors;
	}

	private void runErrorsTest(String[] testFiles, int visibility, boolean isMethod) {
		String[] errors = isMethod
			? errorsForInvalidTagsMethodOrConstructor(visibility)
			: errorsForInvalidTagsClassOrField(visibility);
		int length = errors.length;
		StringBuilder expectedProblemLog = new StringBuilder(isMethod?"M":"C");
		expectedProblemLog.append(errors.length);
		expectedProblemLog.append("----------\n");
		for (int i=0; i<length; i++) {
			expectedProblemLog.append(errors[i]);
		}
		runNegativeTest(testFiles, expectedProblemLog.toString(),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
    }

	@Override
    protected void checkCompilerLog(String[] testFiles, Requestor requestor,
    		String[] alternatePlatformIndependantExpectedLogs, Throwable exception) {
    	String platformIndependantExpectedLog = alternatePlatformIndependantExpectedLogs[0];
    	char firstChar = platformIndependantExpectedLog.charAt(0);
    	boolean isMethod;
    	switch (firstChar) {
    		case 'M':
    			isMethod = true;
    			break;
    		case 'C':
    			isMethod = false;
    			break;
    		default:
    			super.checkCompilerLog(testFiles, requestor,
					alternatePlatformIndependantExpectedLogs, exception);
    		return;
    	}
    	int level = platformIndependantExpectedLog.charAt(1) - '0';
	    String computedProblemLog = Util.convertToIndependantLineDelimiter(requestor.problemLog.toString());
		String expectedLog = platformIndependantExpectedLog.substring(2);
	    if (!expectedLog.equals(computedProblemLog)) {
	    	System.out.println(getClass().getName() + '#' + getName());
			System.out.println("Following static variable should be updated as follow to make this test green:");
			System.out.print("	private static final String[] ");
			if (isMethod) {
				System.out.print("METHODS_ERRORS");
			} else {
				System.out.print("CLASSES_ERRORS");
			}
			System.out.print(" = {\n");
			String[] errors = computedProblemLog.split("----------\n");
			int length = errors.length;
			int max = length / level;
			for (int i=0, idx=1; i<level; i++) {
				StringBuilder buffer = new StringBuilder();
				for (int j=0; j<max; j++) {
					if (j > 0) {
						buffer.append("----------\n");
					}
					buffer.append(errors[idx++]);
				}
				buffer.append("----------\n");
				System.out.print(Util.displayString(buffer.toString(), INDENT, true));
				System.out.println(',');
			}
		    for (int k = 0; k < INDENT-1; k++) System.out.print('\t');
			System.out.println("};");
	    }
		if (exception == null) {
			assertEquals("Invalid problem log ", expectedLog, computedProblemLog);
		}
    }

	private static final String[] X_MISSING_TAGS_ERRORS = {
		"""
			1. ERROR in X.java (at line 8)
				public X(int i) {}
				             ^
			Javadoc: Missing tag for parameter i
			----------
			2. ERROR in X.java (at line 10)
				public int publicMethod(long l) { return 0;}
				       ^^^
			Javadoc: Missing tag for return type
			----------
			3. ERROR in X.java (at line 10)
				public int publicMethod(long l) { return 0;}
				                             ^
			Javadoc: Missing tag for parameter l
			----------
			""",
		"""
			4. ERROR in X.java (at line 17)
				protected X(long l) {}
				                 ^
			Javadoc: Missing tag for parameter l
			----------
			5. ERROR in X.java (at line 19)
				protected int protectedMethod(long l) { return 0; }
				          ^^^
			Javadoc: Missing tag for return type
			----------
			6. ERROR in X.java (at line 19)
				protected int protectedMethod(long l) { return 0; }
				                                   ^
			Javadoc: Missing tag for parameter l
			----------
			""",
		"""
			7. ERROR in X.java (at line 26)
				X(float f) {}
				        ^
			Javadoc: Missing tag for parameter f
			----------
			8. ERROR in X.java (at line 28)
				int packageMethod(long l) { return 0;}
				^^^
			Javadoc: Missing tag for return type
			----------
			9. ERROR in X.java (at line 28)
				int packageMethod(long l) { return 0;}
				                       ^
			Javadoc: Missing tag for parameter l
			----------
			""",
		"""
			10. ERROR in X.java (at line 35)
				private X(double d) {}
				                 ^
			Javadoc: Missing tag for parameter d
			----------
			11. ERROR in X.java (at line 37)
				private int privateMethod(long l) { return 0;}
				        ^^^
			Javadoc: Missing tag for return type
			----------
			12. ERROR in X.java (at line 37)
				private int privateMethod(long l) { return 0;}
				                               ^
			Javadoc: Missing tag for parameter l
			----------
			"""
	};
	private static final String[] Y_MISSING_TAGS_ERRORS = {
		"""
			1. ERROR in Y.java (at line 6)
				public int publicMethod(long l) { return 0;}
				       ^^^
			Javadoc: Missing tag for return type
			----------
			2. ERROR in Y.java (at line 6)
				public int publicMethod(long l) { return 0;}
				                             ^
			Javadoc: Missing tag for parameter l
			----------
			""",
		"""
			3. ERROR in Y.java (at line 8)
				protected int protectedMethod(long l) { return 0;}
				          ^^^
			Javadoc: Missing tag for return type
			----------
			4. ERROR in Y.java (at line 8)
				protected int protectedMethod(long l) { return 0;}
				                                   ^
			Javadoc: Missing tag for parameter l
			----------
			""",
		"""
			5. ERROR in Y.java (at line 10)
				int packageMethod(long l) { return 0;}
				^^^
			Javadoc: Missing tag for return type
			----------
			6. ERROR in Y.java (at line 10)
				int packageMethod(long l) { return 0;}
				                       ^
			Javadoc: Missing tag for parameter l
			----------
			""",
		"""
			7. ERROR in Y.java (at line 12)
				private int privateMethod(long l) { return 0;}
				        ^^^
			Javadoc: Missing tag for return type
			----------
			8. ERROR in Y.java (at line 12)
				private int privateMethod(long l) { return 0;}
				                               ^
			Javadoc: Missing tag for parameter l
			----------
			""",
		"""
			----------
			1. ERROR in Y.java (at line 12)
				private int privateMethod(long l) { return 0;}
				        ^^^
			Javadoc: Missing tag for return type
			----------
			2. ERROR in Y.java (at line 12)
				private int privateMethod(long l) { return 0;}
				                               ^
			Javadoc: Missing tag for parameter l
			----------
			"""
	};

	private String resultForMissingTags(int visibility) {
		StringBuilder result = new StringBuilder("----------\n");
		for (int i=0; i<=visibility; i++) {
			result.append(X_MISSING_TAGS_ERRORS[i]);
		}
		if (CompilerOptions.ENABLED.equals(this.reportMissingJavadocTagsOverriding)) {
			result.append("----------\n");
			result.append(Y_MISSING_TAGS_ERRORS[JavadocTestOptions.PUBLIC_VISIBILITY]);
			if (visibility >= JavadocTestOptions.PROTECTED_VISIBILITY) {
				result.append(Y_MISSING_TAGS_ERRORS[JavadocTestOptions.PROTECTED_VISIBILITY]);
				if (visibility >= JavadocTestOptions.DEFAULT_VISIBILITY) {
					result.append(Y_MISSING_TAGS_ERRORS[JavadocTestOptions.DEFAULT_VISIBILITY]);
					if (visibility == JavadocTestOptions.PRIVATE_VISIBILITY) {
						result.append(Y_MISSING_TAGS_ERRORS[JavadocTestOptions.PRIVATE_VISIBILITY]);
					}
				}
			}
		}
		// Private level is always reported as it never overrides...
		else if (visibility == JavadocTestOptions.PRIVATE_VISIBILITY) {
			result.append(Y_MISSING_TAGS_ERRORS[JavadocTestOptions.PRIVATE_VISIBILITY+1]);
		}
		return result.toString();
	}

	private static final String[] X_MISSING_COMMENTS_ERRORS = {
		"""
			1. ERROR in X.java (at line 4)
				public class PublicClass {}
				             ^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			2. ERROR in X.java (at line 5)
				public int publicField;
				           ^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			3. ERROR in X.java (at line 6)
				public X(int i) {}
				       ^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			4. ERROR in X.java (at line 7)
				public int publicMethod(long l) { return 0;}
				           ^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			""",
		"""
			5. ERROR in X.java (at line 9)
				protected class ProtectedClass {}
				                ^^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			6. ERROR in X.java (at line 10)
				protected int protectedField;
				              ^^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			7. ERROR in X.java (at line 11)
				protected X(long l) {}
				          ^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			8. ERROR in X.java (at line 12)
				protected int protectedMethod(long l) { return 0; }
				              ^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			""",
		"""
			9. ERROR in X.java (at line 14)
				class PackageClass {}
				      ^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			10. ERROR in X.java (at line 15)
				int packageField;
				    ^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			11. ERROR in X.java (at line 16)
				X(float f) {}
				^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			12. ERROR in X.java (at line 17)
				int packageMethod(long l) { return 0;}
				    ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			""",
		"""
			13. ERROR in X.java (at line 19)
				private class PrivateClass {}
				              ^^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			14. ERROR in X.java (at line 20)
				private int privateField;
				            ^^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			15. ERROR in X.java (at line 21)
				private X(double d) {}
				        ^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			16. ERROR in X.java (at line 22)
				private int privateMethod(long l) { return 0;}
				            ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			"""
	};
	private static final String[] Y_MISSING_COMMENTS_ERRORS = {
		"""
			1. ERROR in Y.java (at line 5)
				public int publicMethod(long l) { return 0;}
				           ^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			""",
		"""
			2. ERROR in Y.java (at line 6)
				protected int protectedMethod(long l) { return 0;}
				              ^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			""",
		"""
			3. ERROR in Y.java (at line 7)
				int packageMethod(long l) { return 0;}
				    ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			""",
		"""
			4. ERROR in Y.java (at line 8)
				private int privateMethod(long l) { return 0;}
				            ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			""",
		"""
			----------
			1. ERROR in Y.java (at line 8)
				private int privateMethod(long l) { return 0;}
				            ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			"""
	};

	private String resultForMissingComments(int visibility) {
		StringBuilder result = new StringBuilder("----------\n");
		for (int i=0; i<=visibility; i++) {
			result.append(X_MISSING_COMMENTS_ERRORS[i]);
		}
		if (CompilerOptions.ENABLED.equals(this.reportMissingJavadocCommentsOverriding)) {
			result.append("----------\n");
			result.append(Y_MISSING_COMMENTS_ERRORS[JavadocTestOptions.PUBLIC_VISIBILITY]);
			if (visibility >= JavadocTestOptions.PROTECTED_VISIBILITY) {
				result.append(Y_MISSING_COMMENTS_ERRORS[JavadocTestOptions.PROTECTED_VISIBILITY]);
				if (visibility >= JavadocTestOptions.DEFAULT_VISIBILITY) {
					result.append(Y_MISSING_COMMENTS_ERRORS[JavadocTestOptions.DEFAULT_VISIBILITY]);
					if (visibility == JavadocTestOptions.PRIVATE_VISIBILITY) {
						result.append(Y_MISSING_COMMENTS_ERRORS[JavadocTestOptions.PRIVATE_VISIBILITY]);
					}
				}
			}
		}
		// Private level is always reported for as it never overrides...
		else if (visibility == JavadocTestOptions.PRIVATE_VISIBILITY) {
			result.append(Y_MISSING_COMMENTS_ERRORS[JavadocTestOptions.PRIVATE_VISIBILITY+1]);
		}
		return result.toString();
	}

	public JavadocTestOptions(String name) {
		super(name);
	}
	public static Class javadocTestClass() {
		return JavadocTestOptions.class;
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(javadocTestClass());
	}
	static { // Use this static to initialize testNames (String[]) , testRange (int[2]), testNumbers (int[])
	}
	/**
	 * @return Returns the docCommentSupport.
	 *
	public String getNamePrefix() {
		if (this.localDocCommentSupport == null) {
			return super.getNamePrefix();
		} else {
			return this.localDocCommentSupport;
		}
	}
	*/

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		// Set javadoc options if non null
		if (this.docCommentSupport != null)
			options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		if (this.reportInvalidJavadoc != null)
			options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
		if (this.reportInvalidJavadocTagsVisibility != null)
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocTagsVisibility);
		if (this.reportInvalidJavadocTags != null)
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, this.reportInvalidJavadocTags);
		if (this.reportInvalidJavadocTagsDeprecatedRef != null)
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, this.reportInvalidJavadocTagsDeprecatedRef);
		if (this.reportInvalidJavadocTagsNotVisibleRef!= null)
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsNotVisibleRef, this.reportInvalidJavadocTagsNotVisibleRef);
		if (this.reportMissingJavadocTags != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		if (this.reportMissingJavadocTagsVisibility != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, this.reportMissingJavadocTagsVisibility);
		if (this.reportMissingJavadocTagsOverriding != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, this.reportMissingJavadocTagsOverriding);
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		if (this.reportMissingJavadocCommentsVisibility != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, this.reportMissingJavadocCommentsVisibility);
		if (this.reportMissingJavadocCommentsOverriding != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, this.reportMissingJavadocCommentsOverriding);

		// Ignore other options to avoid polluting warnings
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.reportInvalidJavadoc = null;
		this.reportInvalidJavadocTagsVisibility = null;
		this.reportInvalidJavadocTags = null;
		this.reportInvalidJavadocTagsDeprecatedRef = null;
		this.reportInvalidJavadocTagsNotVisibleRef = null;
		this.reportMissingJavadocTags = null;
		this.reportMissingJavadocTagsVisibility = null;
		this.reportMissingJavadocTagsOverriding = null;
		this.reportMissingJavadocComments = null;
		this.reportMissingJavadocCommentsVisibility = null;
		this.reportMissingJavadocCommentsOverriding = null;
	}

	/*
	 * Tests for 'invalid javadoc' options when no doc support is set
	 */
	public void testInvalidTagsClassNoSupport() {
		this.docCommentSupport = CompilerOptions.DISABLED;
		runConformTest(CLASSES_INVALID_COMMENT);
	}
	public void testInvalidTagsFieldNoSupport() {
		this.docCommentSupport = CompilerOptions.DISABLED;
		runConformTest(FIELDS_INVALID_COMMENT);
	}
	public void testInvalidTagsMethodNoSupport() {
		this.docCommentSupport = CompilerOptions.DISABLED;
		runConformTest(METHODS_INVALID_COMMENT);
	}
	public void testInvalidTagsConstructorNoSupport() {
		this.docCommentSupport = CompilerOptions.DISABLED;
		runConformTest(CONSTRUCTORS_INVALID_COMMENT);
	}

	/*
	 * Tests for 'invalid javadoc' options
	 */
	// Test default invalid javadoc (means "ignore" with tags"disabled" and visibility "public")
	public void testInvalidTagsClassDefaults() {
		runConformTest(CLASSES_INVALID_COMMENT);
	}
	public void testInvalidTagsFieldDefaults() {
		runConformTest(FIELDS_INVALID_COMMENT);
	}
	public void testInvalidTagsMethodDefaults() {
		runConformTest(METHODS_INVALID_COMMENT);
	}
	public void testInvalidTagsConstructorDefaults() {
		runConformTest(CONSTRUCTORS_INVALID_COMMENT);
	}

	// Test invalid javadoc "error" + tags "disabled" and visibility "public"
	public void testInvalidTagsClassErrorNotags() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.DISABLED;
		runConformTest(CLASSES_INVALID_COMMENT);
	}
	public void testInvalidTagsFieldErrorNotags() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.DISABLED;
		runConformTest(FIELDS_INVALID_COMMENT);
	}
	public void testInvalidTagsMethodErrorNotags() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.DISABLED;
		runConformTest(METHODS_INVALID_COMMENT);
	}
	public void testInvalidTagsConstructorErrorNotags() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.DISABLED;
		runConformTest(CONSTRUCTORS_INVALID_COMMENT);
	}

	// Test invalid javadoc "error" + tags "enabled" and visibility "public"
	public void testInvalidTagsClassErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsFieldErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsMethodErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsConstructorErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" and visibility "protected"
	public void testInvalidTagsClassErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsFieldErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsMethodErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsConstructorErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" and visibility "default"
	public void testInvalidTagsClassErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsFieldErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsMethodErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsConstructorErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" and visibility "private"
	public void testInvalidTagsClassErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		runErrorsTest(CLASSES_INVALID_COMMENT, JavadocTestOptions.PRIVATE_VISIBILITY, false);
	}
	public void testInvalidTagsFieldErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		runErrorsTest(FIELDS_INVALID_COMMENT, JavadocTestOptions.PRIVATE_VISIBILITY, false);
	}
	public void testInvalidTagsMethodErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		runErrorsTest(METHODS_INVALID_COMMENT, JavadocTestOptions.PRIVATE_VISIBILITY, true);
	}
	public void testInvalidTagsConstructorErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		runErrorsTest(CONSTRUCTORS_INVALID_COMMENT, JavadocTestOptions.PRIVATE_VISIBILITY, true);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid deprecated references "disabled" and visibility "public"
	public void testInvalidTagsDeprecatedRefClassErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefFieldErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefMethodErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefConstructorErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid deprecated references "disabled" visibility "protected"
	public void testInvalidTagsDeprecatedRefClassErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefFieldErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefMethodErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefConstructorErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid deprecated references "disabled" and visibility "default"
	public void testInvalidTagsDeprecatedRefClassErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefFieldErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefMethodErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefConstructorErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid deprecated references "disabled" and visibility "private"
	public void testInvalidTagsDeprecatedRefClassErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefFieldErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefMethodErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedRefConstructorErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid not visible references "disabled" and visibility "public"
	public void testInvalidTagsNotVisibleRefClassErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefFieldErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefMethodErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefConstructorErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid not visible references "disabled" visibility "protected"
	public void testInvalidTagsNotVisibleRefClassErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefFieldErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefMethodErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefConstructorErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid not visible references "disabled" and visibility "default"
	public void testInvalidTagsNotVisibleRefClassErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefFieldErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefMethodErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefConstructorErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid not visible references "disabled" and visibility "private"
	public void testInvalidTagsNotVisibleRefClassErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefFieldErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefMethodErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsNotVisibleRefConstructorErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid deprecated or not visible references "disabled" and visibility "public"
	public void testInvalidTagsDeprecatedAndNotVisibleRefClassErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefFieldErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefMethodErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefConstructorErrorTagsPublic() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid deprecated or not visible references "disabled" visibility "protected"
	public void testInvalidTagsDeprecatedAndNotVisibleRefClassErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefFieldErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefMethodErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefConstructorErrorTagsProtected() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PROTECTED;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid deprecated or not visible references "disabled" and visibility "default"
	public void testInvalidTagsDeprecatedAndNotVisibleRefClassErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefFieldErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefMethodErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefConstructorErrorTagsPackage() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test invalid javadoc "error" + tags "enabled" but invalid deprecated or not visible references "disabled" and visibility "private"
	public void testInvalidTagsDeprecatedAndNotVisibleRefClassErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		runNegativeTest(CLASSES_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefFieldErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		runNegativeTest(FIELDS_INVALID_COMMENT, resultForInvalidTagsClassOrField(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefMethodErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		runNegativeTest(METHODS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testInvalidTagsDeprecatedAndNotVisibleRefConstructorErrorTagsPrivate() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTagsDeprecatedRef = CompilerOptions.DISABLED;
		this.reportInvalidJavadocTagsNotVisibleRef = CompilerOptions.DISABLED;
		runNegativeTest(CONSTRUCTORS_INVALID_COMMENT, resultForInvalidTagsMethodOrConstructor(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/*
	 * Tests for 'missing javadoc tags' options
	 */
	// Test default missing javadoc tags (means "ignore" with visibility "public" and overriding="enabled")
	public void testMissingTagsDefaults() {
		runConformTest(MISSING_TAGS);
	}

	// Test missing javadoc tags "error" + "public" visibility + "enabled" overriding
	public void testMissingTagsErrorPublicOverriding() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PUBLIC;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
		runNegativeTest(MISSING_TAGS, resultForMissingTags(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc tags "error" + "public" visibility + "disabled" overriding
	public void testMissingTagsErrorPublic() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PUBLIC;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.DISABLED;
		runNegativeTest(MISSING_TAGS, resultForMissingTags(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc tags "error" + "protected" visibility + "enabled" overriding
	public void testMissingTagsErrorProtectedOverriding() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PROTECTED;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
		runNegativeTest(MISSING_TAGS, resultForMissingTags(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc tags "error" + "protected" visibility + "disabled" overriding
	public void testMissingTagsErrorProtected() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PROTECTED;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.DISABLED;
		runNegativeTest(MISSING_TAGS, resultForMissingTags(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc tags "error" + "default" visibility + "enabled" overriding
	public void testMissingTagsErrorPackageOverriding() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.DEFAULT;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
		runNegativeTest(MISSING_TAGS, resultForMissingTags(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc tags "error" + "default" visibility + "disabled" overriding
	public void testMissingTagsErrorPackage() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.DEFAULT;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.DISABLED;
		runNegativeTest(MISSING_TAGS, resultForMissingTags(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc tags "error" + "private" visibility + "enabled" overriding
	public void testMissingTagsErrorPrivateOverriding() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PRIVATE;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
		runNegativeTest(MISSING_TAGS, resultForMissingTags(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc tags "error" + "private" visibility + "disabled" overriding
	public void testMissingTagsErrorPrivate() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PRIVATE;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.DISABLED;
		runNegativeTest(MISSING_TAGS, resultForMissingTags(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/*
	 * Tests for 'missing javadoc comments' options
	 */
	// Test default missing javadoc comments (means "ignore" with visibility "public" and overriding="enabled")
	public void testMissingCommentsDefaults() {
		runConformTest(MISSING_COMMENTS);
	}

	// Test missing javadoc comments "error" + "public" visibility + "enabled" overriding
	public void testMissingCommentsErrorPublicOverriding() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
		this.reportMissingJavadocCommentsOverriding = CompilerOptions.ENABLED;
		runNegativeTest(MISSING_COMMENTS, resultForMissingComments(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc comments "error" + "public" visibility + "disabled" overriding
	public void testMissingCommentsErrorPublic() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
		this.reportMissingJavadocCommentsOverriding = CompilerOptions.DISABLED;
		runNegativeTest(MISSING_COMMENTS, resultForMissingComments(JavadocTestOptions.PUBLIC_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc comments "error" + "protected" visibility + "enabled" overriding
	public void testMissingCommentsErrorProtectedOverriding() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PROTECTED;
		this.reportMissingJavadocCommentsOverriding = CompilerOptions.ENABLED;
		runNegativeTest(MISSING_COMMENTS, resultForMissingComments(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc comments "error" + "protected" visibility + "disabled" overriding
	public void testMissingCommentsErrorProtected() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PROTECTED;
		this.reportMissingJavadocCommentsOverriding = CompilerOptions.DISABLED;
		runNegativeTest(MISSING_COMMENTS, resultForMissingComments(JavadocTestOptions.PROTECTED_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc comments "error" + "default" visibility + "enabled" overriding
	public void testMissingCommentsErrorPackageOverriding() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.DEFAULT;
		this.reportMissingJavadocCommentsOverriding = CompilerOptions.ENABLED;
		runNegativeTest(MISSING_COMMENTS, resultForMissingComments(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc comments "error" + "default" visibility + "disabled" overriding
	public void testMissingCommentsErrorPackage() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.DEFAULT;
		this.reportMissingJavadocCommentsOverriding = CompilerOptions.DISABLED;
		runNegativeTest(MISSING_COMMENTS, resultForMissingComments(JavadocTestOptions.DEFAULT_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc comments "error" + "private" visibility + "enabled" overriding
	public void testMissingCommentsErrorPrivateOverriding() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PRIVATE;
		this.reportMissingJavadocCommentsOverriding = CompilerOptions.ENABLED;
		runNegativeTest(MISSING_COMMENTS, resultForMissingComments(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// Test missing javadoc comments "error" + "private" visibility + "disabled" overriding
	public void testMissingCommentsErrorPrivate() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PRIVATE;
		this.reportMissingJavadocCommentsOverriding = CompilerOptions.DISABLED;
		runNegativeTest(MISSING_COMMENTS, resultForMissingComments(JavadocTestOptions.PRIVATE_VISIBILITY),
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/*
	 * Crossed tests
	 */
	public void testInvalidTagsClassWithMissingTagsOption() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PRIVATE;
		runConformTest(CLASSES_INVALID_COMMENT);
	}
	public void testInvalidTagsFieldWithMissingTagsOption() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PRIVATE;
		runConformTest(FIELDS_INVALID_COMMENT);
	}
	public void testInvalidTagsMethodWithMissingTagsOption() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PRIVATE;
		runConformTest(METHODS_INVALID_COMMENT);
	}
	public void testInvalidTagsConstructorWithMissingTagsOption() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsVisibility = CompilerOptions.PRIVATE;
		runConformTest(CONSTRUCTORS_INVALID_COMMENT);
	}
	public void testMissingTagsWithInvalidTagsOption() {
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocTagsVisibility = CompilerOptions.PRIVATE;
		runConformTest(MISSING_TAGS);
	}

	/**
	 * Test fix for bug 52264.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=52264">52264</a>
	 */
	// Test invalid javadoc "error" with javadoc comment support disabled
	public void testInvalidTagsJavadocSupportDisabled() {
		this.docCommentSupport = CompilerOptions.DISABLED;
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ERROR;
		runConformTest(CLASSES_INVALID_COMMENT);
		runConformTest(FIELDS_INVALID_COMMENT);
		runConformTest(METHODS_INVALID_COMMENT);
		runConformTest(CONSTRUCTORS_INVALID_COMMENT);
	}

	// Test missing javadoc comments "error" with javadoc comment support disabled
	public void testMissingCommentsJavadocSupportDisabled() {
		this.docCommentSupport = CompilerOptions.DISABLED;
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		runConformReferenceTest(MISSING_COMMENTS);
	}

	// Test missing javadoc tags "error" with javadoc comment support disabled
	public void testMissingTagsJavadocSupportDisabled() {
		this.docCommentSupport = CompilerOptions.DISABLED;
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		runConformReferenceTest(MISSING_TAGS);
	}

}
