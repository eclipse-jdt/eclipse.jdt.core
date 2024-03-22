/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contribution for
 *								bug 382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
 *								Bug 454031 - [compiler][null][loop] bug in null analysis; wrong "dead code" detection
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/* See also NullReferenceTests for general null reference tests */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullReferenceTestAsserts extends AbstractRegressionTest {

// class libraries including org.eclipse.equinox.common
String[] assertLib = null;
public NullReferenceTestAsserts(String name) {
	super(name);
}

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
// Only the highest compliance level is run; add the VM argument
// -Dcompliance=1.4 (for example) to lower it if needed
static {
//		TESTS_NAMES = new String[] { "testBug382069" };
//		TESTS_NUMBERS = new int[] { 561 };
//		TESTS_RANGE = new int[] { 1, 2049 };
}

static final String JUNIT_ASSERT_NAME = "junit/framework/Assert.java";
static final String JUNIT_ASSERT_CONTENT = """
	package junit.framework;
	public class Assert {
	    static public void assertNull(Object object) {}
	    static public void assertNull(String message, Object object) {}
	    static public void assertNotNull(Object object) {}
	    static public void assertNotNull(String message, Object object) {}
	    static public void assertTrue(boolean expression) {}
	    static public void assertTrue(String message, boolean expression) {}
	    static public void assertFalse(boolean expression) {}
	    static public void assertFalse(String message, boolean expression) {}
	}
	""";

static final String ORG_JUNIT_ASSERT_NAME = "org/junit/Assert.java";
static final String ORG_JUNIT_ASSERT_CONTENT = """
	package org.junit;
	public class Assert {
	    static public void assertNull(Object object) {}
	    static public void assertNull(String message, Object object) {}
	    static public void assertNotNull(Object object) {}
	    static public void assertNotNull(String message, Object object) {}
	    static public void assertTrue(boolean expression) {}
	    static public void assertTrue(String message, boolean expression) {}
	    static public void assertFalse(boolean expression) {}
	    static public void assertFalse(String message, boolean expression) {}
	}
	""";
static final String ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME = "org/junit/jupiter/api/Assertions.java";
static final String ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT = """
	package org.junit.jupiter.api;
	import java.util.function.Supplier;
	import java.util.function.BooleanSupplier;
	public class Assertions {
	    static public void assertNull(Object object) {}
	    static public void assertNull(Object object, String message) {}
	    static public void assertNull(Object object, Supplier<String> messageSupplier) {}
	    static public void assertNotNull(Object object) {}
	    static public void assertNotNull(Object object, String message) {}
	    static public void assertNotNull(Object object, Supplier<String> messageSupplier) {}
	    static public void assertTrue(boolean expression) {}
	    static public void assertTrue(boolean expression, String message) {}
	    static public void assertTrue(boolean expression, Supplier<String> messageSupplier) {}
	    static public void assertTrue(BooleanSupplier booleanSupplier) {}
	    static public void assertTrue(BooleanSupplier booleanSupplier, String message) {}
	    static public void assertTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {}
	    static public void assertFalse(boolean expression) {}
	    static public void assertFalse(boolean expression, String message) {}
	    static public void assertFalse(boolean expression, Supplier<String> messageSupplier) {}
	    static public void assertFalse(BooleanSupplier booleanSupplier) {}
	    static public void assertFalse(BooleanSupplier booleanSupplier, String message) {}
	    static public void assertFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {}
	}
	""";

static final String APACHE_VALIDATE_NAME = "org/apache/commons/lang/Validate.java";
static final String APACHE_VALIDATE_CONTENT = """
	package org.apache.commons.lang;
	public class Validate {
	    static public void notNull(Object object) {}
	    static public void notNull(Object object, String message) {}
	    static public void isTrue(boolean expression) {}
	    static public void isTrue(boolean expression, String message) {}
	    static public void isTrue(boolean expression, String message, double value) {}
	    static public void isTrue(boolean expression, String message, long value) {}
	    static public void isTrue(boolean expression, String message, Object value) {}
	}
	""";

static final String APACHE_3_VALIDATE_NAME = "org/apache/commons/lang3/Validate.java";
static final String APACHE_3_VALIDATE_CONTENT = """
	package org.apache.commons.lang3;
	public class Validate {
	    static public <T> T notNull(T object) { return object; }
	    static public <T> T notNull(T object, String message, Object... values) { return object; }
	    static public void isTrue(boolean expression) {}
	    static public void isTrue(boolean expression, String message, double value) {}
	    static public void isTrue(boolean expression, String message, long value) {}
	    static public void isTrue(boolean expression, String message, Object value) {}
	}
	""";

static final String GOOGLE_PRECONDITIONS_NAME = "com/google/common/base/Preconditions.java";
static final String GOOGLE_PRECONDITIONS_CONTENT = """
	package com.google.common.base;
	public class Preconditions {
	    static public <T> T checkNotNull(T object) { return object; }
	    static public <T> T checkNotNull(T object, Object message) { return object; }
	    static public <T> T checkNotNull(T object, String message, Object... values) { return object; }
	    static public void checkArgument(boolean expression) {}
	    static public void checkArgument(boolean expression, Object message) {}
	    static public void checkArgument(boolean expression, String msgTmpl, Object... messageArgs) {}
	    static public void checkState(boolean expression) {}
	    static public void checkState(boolean expression, Object message) {}
	    static public void checkState(boolean expression, String msgTmpl, Object... messageArgs) {}
	}
	""";

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
	return NullReferenceTestAsserts.class;
}

@Override
protected void setUp() throws Exception {
	super.setUp();
	if (this.assertLib == null) {
		String[] defaultLibs = getDefaultClassPaths();
		int len = defaultLibs.length;
		this.assertLib = new String[len+1];
		System.arraycopy(defaultLibs, 0, this.assertLib, 0, len);
		File bundleFile = FileLocator.getBundleFileLocation(Platform.getBundle("org.eclipse.equinox.common")).get();
		if (bundleFile.isDirectory())
			this.assertLib[len] = bundleFile.getPath()+"/bin";
		else
			this.assertLib[len] = bundleFile.getPath();
	}
}

// Conditionally augment problem detection settings
static boolean setNullRelatedOptions = true;
@Override
protected Map getCompilerOptions() {
    Map defaultOptions = super.getCompilerOptions();
    if (setNullRelatedOptions) {
	    defaultOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	    defaultOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	    defaultOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
		defaultOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.ENABLED);
    }
    return defaultOptions;
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575a() throws IOException {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object o) {
					    boolean b = o != null;
					    org.eclipse.core.runtime.Assert.isLegal(o != null);
					    o.toString();
					  }
					}
					"""},
			"",
			this.assertLib,
			true,
			null);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575b() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object o) {
					    org.eclipse.core.runtime.Assert.isLegal(o == null);
					    o.toString();
					  }
					}
					"""};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""";
	runner.classLibraries =
		this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575c() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object o, boolean b) {
					    org.eclipse.core.runtime.Assert.isLegal(o != null || b, "FAIL");
					    o.toString();
					  }
					}
					"""};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575d() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object o1, Object o2) {
					    org.eclipse.core.runtime.Assert.isLegal(o1 != null && o2 == null);
					    if (o1 == null) { };
					    if (o2 == null) { };
					  }
					}
					"""};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 4)
					if (o1 == null) { };
					    ^^
				Null comparison always yields false: The variable o1 cannot be null at this location
				----------
				2. WARNING in X.java (at line 4)
					if (o1 == null) { };
					                ^^^
				Dead code
				----------
				3. ERROR in X.java (at line 5)
					if (o2 == null) { };
					    ^^
				Redundant null check: The variable o2 can only be null at this location
				----------
				""";
	runner.classLibraries =
		    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575e() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object o) {
					    org.eclipse.core.runtime.Assert.isLegal(false && o != null);
					    if (o == null) { };
					  }
					}
					"""};
	runner.expectedCompilerLog =
				"""
					----------
					1. WARNING in X.java (at line 3)
						org.eclipse.core.runtime.Assert.isLegal(false && o != null);
						                                                 ^^^^^^^^^
					Dead code
					----------
					""";
	runner.classLibraries =
				this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575e_1() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object o) {
						 o = null;
					    org.eclipse.core.runtime.Assert.isLegal(false && o != null);
					    if (o == null) { };
					  }
					}
					"""};
	runner.expectedCompilerLog =
				"""
					----------
					1. WARNING in X.java (at line 4)
						org.eclipse.core.runtime.Assert.isLegal(false && o != null);
						                                                 ^^^^^^^^^
					Dead code
					----------
					2. ERROR in X.java (at line 5)
						if (o == null) { };
						    ^
					Redundant null check: The variable o can only be null at this location
					----------
					""";
	runner.classLibraries =
				this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575e_2() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object o) {
					    org.eclipse.core.runtime.Assert.isLegal(true || o != null);
					    if (o == null) { };
					  }
					}
					"""};
	runner.expectedCompilerLog =
				"""
					----------
					1. WARNING in X.java (at line 3)
						org.eclipse.core.runtime.Assert.isLegal(true || o != null);
						                                                ^^^^^^^^^
					Dead code
					----------
					""";
	runner.classLibraries =
				this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575f() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object o) {
					    org.eclipse.core.runtime.Assert.isLegal(false || o != null);
					    if (o == null) { };
					  }
					}
					"""};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o == null) { };
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (o == null) { };
				               ^^^
			Dead code
			----------
			""";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// do warn always false comparisons even inside org.eclipse.core.runtime.Assert.isLegal
public void testBug127575g() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo() {
					    Object o = null;
					    org.eclipse.core.runtime.Assert.isLegal(o != null);
					    if (o == null) { };
					  }
					}
					"""};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				org.eclipse.core.runtime.Assert.isLegal(o != null);
				                                        ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 5)
				if (o == null) { };
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			3. WARNING in X.java (at line 5)
				if (o == null) { };
				               ^^^
			Dead code
			----------
			""";
	runner.classLibraries =
		this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings,
// but this doesn't affect the downstream info.
public void testBug127575h() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void m() {
					    X foo = new X();
						 org.eclipse.core.runtime.Assert.isLegal (foo != null);
						 if (foo == null) {}
					    X foo2 = new X();
						 org.eclipse.core.runtime.Assert.isLegal (foo2 == null);
						 if (foo2 == null) {}
					    X bar = null;
						 org.eclipse.core.runtime.Assert.isLegal (bar == null);
						 if (bar == null) {}
					    X bar2 = null;
						 org.eclipse.core.runtime.Assert.isLegal (bar2 != null);
						 if (bar2 == null) {}
					  }
					}
					"""};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (foo == null) {}
				    ^^^
			Null comparison always yields false: The variable foo cannot be null at this location
			----------
			2. WARNING in X.java (at line 5)
				if (foo == null) {}
				                 ^^
			Dead code
			----------
			3. ERROR in X.java (at line 7)
				org.eclipse.core.runtime.Assert.isLegal (foo2 == null);
				                                         ^^^^
			Null comparison always yields false: The variable foo2 cannot be null at this location
			----------
			4. ERROR in X.java (at line 8)
				if (foo2 == null) {}
				    ^^^^
			Redundant null check: The variable foo2 can only be null at this location
			----------
			5. ERROR in X.java (at line 11)
				if (bar == null) {}
				    ^^^
			Redundant null check: The variable bar can only be null at this location
			----------
			6. ERROR in X.java (at line 13)
				org.eclipse.core.runtime.Assert.isLegal (bar2 != null);
				                                         ^^^^
			Null comparison always yields false: The variable bar2 can only be null at this location
			----------
			7. ERROR in X.java (at line 14)
				if (bar2 == null) {}
				    ^^^^
			Null comparison always yields false: The variable bar2 cannot be null at this location
			----------
			8. WARNING in X.java (at line 14)
				if (bar2 == null) {}
				                  ^^
			Dead code
			----------
			""";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings,
// but this doesn't affect the downstream info.
public void testBug127575i() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void m() {
						 X bar = null;
					    X foo = getX();
					    if (foo == null) {
						 	foo = new X();
						 }
						 org.eclipse.core.runtime.Assert.isTrue (foo != null && bar == null);
						 if (foo != null) {}
						 if (bar == null) {}
					  }
					  public X getX() { return new X();}
					}
					"""};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 9)
				if (foo != null) {}
				    ^^^
			Redundant null check: The variable foo cannot be null at this location
			----------
			2. ERROR in X.java (at line 10)
				if (bar == null) {}
				    ^^^
			Redundant null check: The variable bar can only be null at this location
			----------
			""";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings in a looping context,
// but this doesn't affect the downstream info.
public void testBug127575j() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void m() {
					    X foo = new X();
					    X foo2 = new X();
					    X bar = null;
					    X bar2 = null;
						 while (true) {
						 	org.eclipse.core.runtime.Assert.isLegal (foo != null);
						 	if (foo == null) {}
						 	org.eclipse.core.runtime.Assert.isLegal (foo2 == null);
						 	if (foo2 == null) {}
						 	org.eclipse.core.runtime.Assert.isLegal (bar == null);
						 	if (bar == null) {}
						 	org.eclipse.core.runtime.Assert.isLegal (bar2 != null);
						 	if (bar2 == null) {}
						 }
					  }
					}
					"""};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 9)
				if (foo == null) {}
				    ^^^
			Null comparison always yields false: The variable foo cannot be null at this location
			----------
			2. WARNING in X.java (at line 9)
				if (foo == null) {}
				                 ^^
			Dead code
			----------
			3. ERROR in X.java (at line 10)
				org.eclipse.core.runtime.Assert.isLegal (foo2 == null);
				                                         ^^^^
			Null comparison always yields false: The variable foo2 cannot be null at this location
			----------
			4. ERROR in X.java (at line 11)
				if (foo2 == null) {}
				    ^^^^
			Redundant null check: The variable foo2 can only be null at this location
			----------
			5. ERROR in X.java (at line 13)
				if (bar == null) {}
				    ^^^
			Redundant null check: The variable bar can only be null at this location
			----------
			6. ERROR in X.java (at line 14)
				org.eclipse.core.runtime.Assert.isLegal (bar2 != null);
				                                         ^^^^
			Null comparison always yields false: The variable bar2 can only be null at this location
			----------
			7. ERROR in X.java (at line 15)
				if (bar2 == null) {}
				    ^^^^
			Null comparison always yields false: The variable bar2 cannot be null at this location
			----------
			8. WARNING in X.java (at line 15)
				if (bar2 == null) {}
				                  ^^
			Dead code
			----------
			""";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings in a finally context,
// but this doesn't affect the downstream info.
public void testBug127575k() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void m() {
					    X foo = new X();
					    X foo2 = new X();
					    X bar = null;
					    X bar2 = null;
						 try {
							System.out.println("Inside try");
						 }
						 finally {
						 	org.eclipse.core.runtime.Assert.isLegal (foo != null);
						 	if (foo == null) {}
						 	org.eclipse.core.runtime.Assert.isLegal (foo2 == null);
						 	if (foo2 == null) {}
						 	org.eclipse.core.runtime.Assert.isLegal (bar == null);
						 	if (bar == null) {}
						 	org.eclipse.core.runtime.Assert.isLegal (bar2 != null);
						 	if (bar2 == null) {}
						 }
					  }
					}
					"""};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 12)
				if (foo == null) {}
				    ^^^
			Null comparison always yields false: The variable foo cannot be null at this location
			----------
			2. WARNING in X.java (at line 12)
				if (foo == null) {}
				                 ^^
			Dead code
			----------
			3. ERROR in X.java (at line 13)
				org.eclipse.core.runtime.Assert.isLegal (foo2 == null);
				                                         ^^^^
			Null comparison always yields false: The variable foo2 cannot be null at this location
			----------
			4. ERROR in X.java (at line 14)
				if (foo2 == null) {}
				    ^^^^
			Redundant null check: The variable foo2 can only be null at this location
			----------
			5. ERROR in X.java (at line 16)
				if (bar == null) {}
				    ^^^
			Redundant null check: The variable bar can only be null at this location
			----------
			6. ERROR in X.java (at line 17)
				org.eclipse.core.runtime.Assert.isLegal (bar2 != null);
				                                         ^^^^
			Null comparison always yields false: The variable bar2 can only be null at this location
			----------
			7. ERROR in X.java (at line 18)
				if (bar2 == null) {}
				    ^^^^
			Null comparison always yields false: The variable bar2 cannot be null at this location
			----------
			8. WARNING in X.java (at line 18)
				if (bar2 == null) {}
				                  ^^
			Dead code
			----------
			""";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// The condition of org.eclipse.core.runtime.Assert.isLegal is considered always true
// and alters the following analysis suitably.
public void testBug127575l() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"Test.java",
				"""
					public class Test {
						void foo(Object a, Object b, Object c) {
							org.eclipse.core.runtime.Assert.isLegal( a == null);
					 \
							if (a!=null) {
								System.out.println("a is not null");
							 } else{
								System.out.println("a is null");
							 }
							a = null;
							if (a== null) {}
							org.eclipse.core.runtime.Assert.isLegal(b != null);
					 \
							if (b!=null) {
								System.out.println("b is not null");
							 } else{
								System.out.println("b is null");
							 }
							org.eclipse.core.runtime.Assert.isLegal(c == null);
							if (c.equals(a)) {
								System.out.println("");
							 } else{
								System.out.println("");
							 }
						}
						public static void main(String[] args){
							Test test = new Test();
							test.foo(null,null, null);
						}
					}
					"""};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in Test.java (at line 4)
					if (a!=null) {
					    ^
				Null comparison always yields false: The variable a can only be null at this location
				----------
				2. WARNING in Test.java (at line 4)
					if (a!=null) {
							System.out.println("a is not null");
						 } else{
					             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				3. ERROR in Test.java (at line 9)
					a = null;
					^
				Redundant assignment: The variable a can only be null at this location
				----------
				4. ERROR in Test.java (at line 10)
					if (a== null) {}
					    ^
				Redundant null check: The variable a can only be null at this location
				----------
				5. ERROR in Test.java (at line 12)
					if (b!=null) {
					    ^
				Redundant null check: The variable b cannot be null at this location
				----------
				6. WARNING in Test.java (at line 14)
					} else{
							System.out.println("b is null");
						 }
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				7. ERROR in Test.java (at line 18)
					if (c.equals(a)) {
					    ^
				Null pointer access: The variable c can only be null at this location
				----------
				""";
	runner.classLibraries =
			this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// NPE warnings should be given inside org.eclipse.core.runtime.Assert.isLegal too
public void testBug127575m() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				"Info.java",
				"""
					public class Info {
						public void test(Info[] infos) {
							for (final Info info : infos) {
					 \
								if (info != null) {
									org.eclipse.core.runtime.Assert.isLegal( info.checkSomething());
							 		info.doSomething();
								}
							 }
							for (final Info info : infos) {
					 \
								if (info == null) {
									org.eclipse.core.runtime.Assert.isLegal(info.checkSomething());
							 		info.doSomething();
								}
							 }
						}
						void doSomething()  {}
						boolean checkSomething() {return true;}
					}
					"""};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in Info.java (at line 11)
						org.eclipse.core.runtime.Assert.isLegal(info.checkSomething());
						                                        ^^^^
					Null pointer access: The variable info can only be null at this location
					----------
					""";
		runner.classLibraries =
			this.assertLib;
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// always false comparison in Assert.isLegal in loop should be warned against
public void testBug127575n() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
		"DoWhileBug.java",
				"""
					public class DoWhileBug {
						void test(boolean b1) {
							Object o1 = null;
							Object o2 = null;
							do {
					           if (b1)
									o1 = null;
					           org.eclipse.core.runtime.Assert.isLegal (o1 != null);
							} while (true);
						}
					}"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in DoWhileBug.java (at line 7)
					o1 = null;
					^^
				Redundant assignment: The variable o1 can only be null at this location
				----------
				2. ERROR in DoWhileBug.java (at line 8)
					org.eclipse.core.runtime.Assert.isLegal (o1 != null);
					                                         ^^
				Null comparison always yields false: The variable o1 can only be null at this location
				----------
				""";
		runner.classLibraries =
			this.assertLib;
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// "redundant null check" in Assert.isLegal in loop should not be warned against
public void testBug127575o() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
		"DoWhileBug.java",
				"""
					public class DoWhileBug {
						void test(boolean b1) {
							Object o1 = null;
							Object o2 = null;
							do {
					           if (b1)
									o1 = null;
					           org.eclipse.core.runtime.Assert.isLegal ((o2 = o1) == null);
							} while (true);
						}
					}"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in DoWhileBug.java (at line 7)
					o1 = null;
					^^
				Redundant assignment: The variable o1 can only be null at this location
				----------
				""";
		runner.classLibraries =
			this.assertLib;
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=373953
public void testBug373953() throws IOException {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object o) {
					    boolean b = o != null;
					    java.eclipse.core.runtime.Assert.isLegal(o != null);
					    o.toString();
					  }
					  void foo1(Object o) {
					    boolean b = o != null;
					    org.lang.core.runtime.Assert.isLegal(o != null);
					    o.toString();
					  }
					}
					""",
				"java.eclipse.core.runtime/Assert.java",
				"""
					package java.eclipse.core.runtime;
					public class Assert {
					  public static void isLegal(boolean b) {
					  }
					}
					""",
				"org.lang.core.runtime/Assert.java",
				"""
					package org.lang.core.runtime;
					public class Assert {
					  public static void isLegal(boolean b) {
					  }
					}
					"""};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 5)
					o.toString();
					^
				Potential null pointer access: The variable o may be null at this location
				----------
				2. ERROR in X.java (at line 10)
					o.toString();
					^
				Potential null pointer access: The variable o may be null at this location
				----------
				""";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertNotNull
public void testBug382069a() throws IOException {
	this.runConformTest(
		new String[] {
			JUNIT_ASSERT_NAME,
			JUNIT_ASSERT_CONTENT,
			"X.java",
			"""
				public class X {
				  void foo(Object o1, String o2) {
				    boolean b = o1 != null;
				    junit.framework.Assert.assertNotNull(o1);
				    o1.toString();
				    b = o2 != null;
				    junit.framework.Assert.assertNotNull("msg", o2);
				    o2.toString();
				  }
				}
				"""},
		"");
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// org.eclipse.core.runtime.Assert.isNotNull
public void testBug382069b() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
		"X.java",
				"""
					public class X {
					  void foo(Object o1, String o2) {
					    boolean b = o1 != null;
					    org.eclipse.core.runtime.Assert.isNotNull(o1);
					    o1.toString();
					    b = o2 != null;
					    org.eclipse.core.runtime.Assert.isNotNull(o2, "msg");
					    o2.toString();
					  }
					}"""
			},
			"",
			this.assertLib,
			true,
			null);
	}
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertNull and dead code analysis
public void testBug382069c() throws IOException {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			JUNIT_ASSERT_NAME,
			JUNIT_ASSERT_CONTENT,
			"X.java",
			"""
				public class X {
				  boolean foo(String o1, String o2) {
				    junit.framework.Assert.assertNull("something's wrong", o1);
				    if (o2 == null)
				        return o1 != null;
				    junit.framework.Assert.assertNull(o2);
				    return false; // dead code
				  }
				  void bar(X x) {
				    if (x == null) {
				      junit.framework.Assert.assertNotNull(x);
				      return; // dead code
				    }
				  }
				}
				"""};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 5)
					return o1 != null;
					       ^^
				Null comparison always yields false: The variable o1 can only be null at this location
				----------
				2. WARNING in X.java (at line 7)
					return false; // dead code
					^^^^^^^^^^^^^
				Dead code
				----------
				3. WARNING in X.java (at line 12)
					return; // dead code
					^^^^^^^
				Dead code
				----------
				""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// various asserts from org.apache.commons.lang.Validate
public void testBug382069d() throws IOException {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			APACHE_VALIDATE_NAME,
			APACHE_VALIDATE_CONTENT,
			"X.java",
			"""
				import org.apache.commons.lang.Validate;
				public class X {
				  void foo(Object o1, String o2, X x) {
				    boolean b = o1 != null;
				    Validate.notNull(o1);
				    o1.toString();
				    b = o2 != null;
				    Validate.notNull(o2, "msg");
				    o2.toString();
				    Validate.isTrue(x == null, "ups", x);
				    x.foo(null, null, null); // definite NPE
				  }
				}
				"""};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 11)
					x.foo(null, null, null); // definite NPE
					^
				Null pointer access: The variable x can only be null at this location
				----------
				""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// various asserts from org.apache.commons.lang3Validate
public void testBug382069e() throws IOException {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				APACHE_3_VALIDATE_NAME,
				APACHE_3_VALIDATE_CONTENT,
				"X.java",
				"""
					import org.apache.commons.lang3.Validate;
					public class X {
					  void foo(Object o1, String o2, X x) {
					    boolean b = o1 != null;
					    Validate.notNull(o1);
					    o1.toString();
					    b = o2 != null;
					    Validate.notNull(o2, "msg");
					    o2.toString();
					    Validate.isTrue(x == null, "ups", x);
					    x.foo(null, null, null); // definite NPE
					  }
					}
					"""};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 11)
						x.foo(null, null, null); // definite NPE
						^
					Null pointer access: The variable x can only be null at this location
					----------
					""";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}
// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// various asserts from com.google.common.base.Preconditions
public void testBug382069f() throws IOException {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				GOOGLE_PRECONDITIONS_NAME,
				GOOGLE_PRECONDITIONS_CONTENT,
				"X.java",
				"""
					import com.google.common.base.Preconditions;
					public class X {
					  void foo(Object o1, String o2, X x) {
					    boolean b = o1 != null;
					    Preconditions.checkNotNull(o1);
					    o1.toString();
					    b = o2 != null;
					    Preconditions.checkNotNull(o2, "msg {0}.{1}", o1, o2);
					    o2.toString();
					    Preconditions.checkArgument(x == null, "ups");
					    x.foo(null, null, null); // definite NPE
					  }
					}
					"""};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 11)
						x.foo(null, null, null); // definite NPE
						^
					Null pointer access: The variable x can only be null at this location
					----------
					""";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}
// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// java.util.Objects#requireNonNull
public void testBug382069g() throws IOException {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.util.Objects.requireNonNull;
					public class X {
					  void foo(Object o1, String o2, X x) {
					    boolean b = o1 != null;
					    requireNonNull(o1);
					    o1.toString();
					    b = o2 != null;
					    requireNonNull(o2, "msg");
					    o2.toString();
					  }
					}
					"""},
				"");
	}
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertTrue / assertFalse
public void testBug382069h() throws IOException {
	this.runConformTest(
		new String[] {
			JUNIT_ASSERT_NAME,
			JUNIT_ASSERT_CONTENT,
			"X.java",
			"""
				public class X {
				  void foo(Object o1, String o2) {
				    boolean b = o1 != null;
				    junit.framework.Assert.assertTrue(o1 != null);
				    o1.toString();
				    b = o2 != null;
				    junit.framework.Assert.assertFalse("msg", o2 == null);
				    o2.toString();
				  }
				}
				"""},
		"");
}
// Bug 401159 - [null] Respect org.junit.Assert for control flow
// various asserts from org.junit.Assert
public void testBug401159() throws IOException {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			ORG_JUNIT_ASSERT_NAME,
			ORG_JUNIT_ASSERT_CONTENT,
			"X.java",
			"""
				import org.junit.Assert;
				public class X {
				  void foo(Object o1, String o2, X x) {
				    boolean b = o1 != null;
				    Assert.assertNotNull(o1);
				    o1.toString();
				    b = o2 != null;
				    Assert.assertNotNull("msg", o2);
				    o2.toString();
				    Assert.assertTrue("ups", x == null);
				    x.foo(null, null, null); // definite NPE
				  }
				}
				"""};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 11)
					x.foo(null, null, null); // definite NPE
					^
				Null pointer access: The variable x can only be null at this location
				----------
				""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
}

// https://bugs.eclipse.org/472618 - [compiler][null] assertNotNull vs. Assert.assertNotNull
// junit's assertNotNull
public void testBug472618() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses auto-unboxing
	this.runConformTest(
		new String[] {
			JUNIT_ASSERT_NAME,
			JUNIT_ASSERT_CONTENT,
			"AssertionTest.java",
			"""
				import junit.framework.Assert;
				
				public class AssertionTest extends Assert
				{
				    void test()
				    {
				        Long test = null;
				
				        if(Boolean.TRUE)
				        {
				            test = 0L;
				        }
				
				        assertNotNull(test);
				
				        test.longValue();  // <- potential null pointer access
				    }
				}
				"""},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=568542
// junit 5's assertNotNull
public void testBug568542a() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses Supplier
	this.runConformTest(
		new String[] {
			ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME,
			ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT,
			"X.java",
			"""
				import static org.junit.jupiter.api.Assertions.assertNotNull;
				public class X {
				    void test(Long test1, Long test2, Long test3) {
				        boolean b = (test1 != null | test2 != null | test3 != null);
				        assertNotNull(test1);
				        test1.longValue();
				        assertNotNull(test2, "message");
				        test2.longValue();
				        assertNotNull(test3, () -> "message");
				        test3.longValue();
				    }
				}
				"""},
		"");
}
// junit 5's assertNull
public void testBug568542b() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses Supplier
	runNegativeTest(
		new String[] {
			ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME,
			ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT,
			"X.java",
			"""
				import static org.junit.jupiter.api.Assertions.assertNull;
				public class X {
				    void test(Long test1, Long test2, Long test3) {
				        boolean b = (test1 != null | test2 != null | test3 != null);
				        assertNull(test1);
				        test1.longValue();
				        assertNull(test2, "message");
				        test2.longValue();
				        assertNull(test3, () -> "message");
				        test3.longValue();
				    }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				test1.longValue();
				^^^^^
			Null pointer access: The variable test1 can only be null at this location
			----------
			2. ERROR in X.java (at line 8)
				test2.longValue();
				^^^^^
			Null pointer access: The variable test2 can only be null at this location
			----------
			3. ERROR in X.java (at line 10)
				test3.longValue();
				^^^^^
			Null pointer access: The variable test3 can only be null at this location
			----------
			"""
	);
}
// junit 5's assertTrue
public void testBug568542c() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses Supplier
	this.runConformTest(
		new String[] {
			ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME,
			ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT,
			"X.java",
			"""
				import static org.junit.jupiter.api.Assertions.assertTrue;
				public class X {
				    void test(Long test1, Long test2, Long test3) {
				        boolean b = (test1 != null | test2 != null | test3 != null);
				        assertTrue(test1 != null);
				        test1.longValue();
				        assertTrue(test2 != null, "message");
				        test2.longValue();
				        assertTrue(test3 != null, () -> "message");
				        test3.longValue();
				    }
				}
				"""},
		"");
}
// junit 5's assertFalse
public void testBug568542d() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses Supplier
	runNegativeTest(
		new String[] {
			ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME,
			ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT,
			"X.java",
			"""
				import static org.junit.jupiter.api.Assertions.assertFalse;
				public class X {
				    void test(Long test1, Long test2, Long test3) {
				        boolean b = (test1 != null | test2 != null | test3 != null);
				        assertFalse(test1 != null);
				        test1.longValue();
				        assertFalse(test2 != null, "message");
				        test2.longValue();
				        assertFalse(test3 != null, () -> "message");
				        test3.longValue();
				    }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				test1.longValue();
				^^^^^
			Null pointer access: The variable test1 can only be null at this location
			----------
			2. ERROR in X.java (at line 8)
				test2.longValue();
				^^^^^
			Null pointer access: The variable test2 can only be null at this location
			----------
			3. ERROR in X.java (at line 10)
				test3.longValue();
				^^^^^
			Null pointer access: The variable test3 can only be null at this location
			----------
			"""
	);
}
}
