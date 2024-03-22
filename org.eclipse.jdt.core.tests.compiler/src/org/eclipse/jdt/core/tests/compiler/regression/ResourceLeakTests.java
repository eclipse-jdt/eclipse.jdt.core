/*******************************************************************************
 * Copyright (c) 2011, 2020 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     Nikolay Metchev (nikolaymetchev@gmail.com) - Contributions for
 *								bug 411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ResourceLeakTests extends AbstractRegressionTest {

// well-known helper classes:
private static final String GUAVA_CLOSEABLES_JAVA = "com/google/common/io/Closeables.java";
private static final String GUAVA_CLOSEABLES_CONTENT = """
	package com.google.common.io;
	public class Closeables {
	    public static void closeQuietly(java.io.Closeable closeable) {}
	    public static void close(java.io.Closeable closeable, boolean flag) {}
	}
	""";
private static final String APACHE_DBUTILS_JAVA = "org/apache/commons/dbutils/DbUtils.java";
private static final String APACHE_DBUTILS_CONTENT = """
	package org.apache.commons.dbutils;
	import java.sql.*;
	public class DbUtils {
	    public static void close(Connection connection) {}
	    public static void close(ResultSet resultSet) {}
	    public static void close(Statement statement) {}
	    public static void closeQuietly(Connection connection) {}
	    public static void closeQuietly(ResultSet resultSet) {}
	    public static void closeQuietly(Statement statement) {}
	    public static void closeQuietly(Connection conn, Statement stmt, ResultSet rs) {}
	}
	""";

// one.util.streamex.StreamEx stub
private static final String STREAMEX_JAVA = "one/util/streamex/StreamEx.java";
private static final String STREAMEX_CONTENT = """
	package one.util.streamex;
	import java.util.stream.*;
	public abstract class StreamEx<T> implements Stream<T> {
	    public static <T> StreamEx<T> create() { return null; }
	}
	""";

static {
//	TESTS_NAMES = new String[] { "testBug463320" };
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public ResourceLeakTests(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(ResourceLeakTests.class);
}

void runTestsExpectingErrorsOnlyIn17(String[] testFiles, String errorsIn17, Map options) {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		runLeakTest(testFiles, errorsIn17, options);
	else
		runConformTest(testFiles, "", null, true, null, options, null);
}

protected void runLeakTest(String[] testFiles, String expectedCompileError, Map options) {
	runNegativeTest(testFiles, expectedCompileError, null, true, options, null, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

protected void runLeakTest(String[] testFiles, String expectedCompileError, Map options, boolean shouldFlushOutput) {
	runNegativeTest(testFiles, expectedCompileError, null, shouldFlushOutput, options, null, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

protected void runLeakWarningTest(String[] testFiles, String expectedCompileError, Map options) {
	runNegativeTest(testFiles, expectedCompileError, null, true, options, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

// === hooks for ResourceLeaksAnnotatedTests: ===
/** Problem message originally complaining about a potential leak. */
protected String potentialLeakOrCloseNotShown(String resourceName) {
	return "Potential resource leak: '"+resourceName+"' may not be closed\n";
}
protected String potentialLeakOrCloseNotShownAtExit(String resourceName) {
	return "Potential resource leak: '"+resourceName+"' may not be closed at this location\n";
}
protected String potentialOrDefiniteLeak(String string) {
	return "Potential resource leak: '"+string+"' may not be closed\n";
}

// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without ever closing it.
public void test056() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				FileReader fileReader = new FileReader(file);
				           ^^^^^^^^^^
			Resource leak: 'fileReader' is never closed
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable and closes it but not protected by t-w-r nor regular try-finally
public void test056a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				        char[] in = new char[50];
				        fileReader.read(in);
						 fileReader.close();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				FileReader fileReader = new FileReader(file);
				           ^^^^^^^^^^
			Resource 'fileReader' should be managed by try-with-resource
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable and closes it properly in a finally block
public void test056b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				        try {
				            char[] in = new char[50];
				            fileReader.read(in);
				        } finally {
						     fileReader.close();
				        }
				    }
				    public static void main(String[] args) {
				        try {
				            new X().foo();
				        } catch (IOException ioex) {
				            System.out.println("caught");
				        }
				    }
				}
				"""
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable properly within try-with-resources.
public void test056c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        try (FileReader fileReader = new FileReader(file)) {
				            char[] in = new char[50];
				            fileReader.read(in);
						 }
				    }
				    public static void main(String[] args) {
				        try {
				            new X().foo();
				        } catch (IOException ioex) {
				            System.out.println("caught");
				        }
				    }
				}
				"""
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses two AutoCloseables (testing independent analysis)
//- one closeable may be unclosed at a conditional return
//- the other is only conditionally closed
public void test056d() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo(boolean flag1, boolean flag2) throws IOException {
				        File file = new File("somefile");
				        char[] in = new char[50];
				        FileReader fileReader1 = new FileReader(file);
				        fileReader1.read(in);
				        FileReader fileReader2 = new FileReader(file);
				        fileReader2.read(in);
				        if (flag1) {
				            fileReader2.close();
				            return;
				        } else if (flag2) {
				            fileReader2.close();
				        }
				        fileReader1.close();
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo(false, true);
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 10)
				FileReader fileReader2 = new FileReader(file);
				           ^^^^^^^^^^^
			Potential resource leak: 'fileReader2' may not be closed
			----------
			2. ERROR in X.java (at line 14)
				return;
				^^^^^^^
			Resource leak: 'fileReader1' is not closed at this location
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses two AutoCloseables (testing independent analysis)
//- one closeable may be unclosed at a conditional return
//- the other is only conditionally closed
public void test056d_suppress() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // annotations used
	Map options = getCompilerOptions();
	enableAllWarningsForIrritants(options, IrritantSet.RESOURCE);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo(boolean flag1, boolean flag2) throws IOException {
				        @SuppressWarnings("resource") File file = new File("somefile"); // unnecessary suppress
				        char[] in = new char[50];
				        FileReader fileReader1 = new FileReader(file);
				        fileReader1.read(in);
				        @SuppressWarnings("resource") FileReader fileReader2 = new FileReader(file); // useful suppress
				        fileReader2.read(in);
				        if (flag1) {
				            fileReader2.close();
				            return; // not suppressed
				        } else if (flag2) {
				            fileReader2.close();
				        }
				        fileReader1.close();
				    }
				    @SuppressWarnings("resource") // useful suppress
				    void bar() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				        char[] in = new char[50];
				        fileReader.read(in);
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo(false, true);
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				@SuppressWarnings("resource") File file = new File("somefile"); // unnecessary suppress
				                  ^^^^^^^^^^
			Unnecessary @SuppressWarnings("resource")
			----------
			2. ERROR in X.java (at line 14)
				return; // not suppressed
				^^^^^^^
			Resource leak: 'fileReader1' is not closed at this location
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// one method returns an AutoCleasble, a second method uses this object without ever closing it.
public void test056e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    FileReader getReader(String filename) throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				        return fileReader;
				    }
				    void foo() throws IOException {
				        FileReader reader = getReader("somefile");
				        char[] in = new char[50];
				        reader.read(in);
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		getTest056e_log(),
		options);
}
protected String getTest056e_log() {
	return """
		----------
		1. ERROR in X.java (at line 11)
			FileReader reader = getReader("somefile");
			           ^^^^^^
		Potential resource leak: \'reader\' may not be closed
		----------
		""";
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method explicitly closes its AutoCloseable rather than using t-w-r
public void test056f() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = null;
				        try {
				            fileReader = new FileReader(file);
				            char[] in = new char[50];
				            fileReader.read(in);
				        } finally {
				            fileReader.close();
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				FileReader fileReader = null;
				           ^^^^^^^^^^
			Resource 'fileReader' should be managed by try-with-resource
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// an AutoCloseable local is re-assigned
public void test056g() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				        char[] in = new char[50];
				        fileReader.read(in);
				        fileReader = new FileReader(file);
				        fileReader.read(in);
				        fileReader.close();
				        fileReader = null;
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				fileReader = new FileReader(file);
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: 'fileReader' is not closed at this location
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// an AutoCloseable local is re-assigned after null-assigned
public void test056g2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				        char[] in = new char[50];
				        fileReader.read(in);
				        fileReader = null;
				        fileReader = new FileReader(file);
				        fileReader.read(in);
				        fileReader.close();
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				fileReader = null;
				^^^^^^^^^^^^^^^^^
			Resource leak: 'fileReader' is not closed at this location
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// two AutoCloseables at different nesting levels (anonymous local type)
public void test056h() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        final File file = new File("somefile");
				        final FileReader fileReader = new FileReader(file);
				        char[] in = new char[50];
				        fileReader.read(in);
				        new Runnable() {
				          public void run() {
				            try {
				                fileReader.close();
				                FileReader localReader = new FileReader(file);
				            } catch (IOException ex) { /* nop */ }
				        }}.run();
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 7)
				final FileReader fileReader = new FileReader(file);
				                 ^^^^^^^^^^
			Potential resource leak: 'fileReader' may not be closed
			----------
			2. ERROR in X.java (at line 14)
				FileReader localReader = new FileReader(file);
				           ^^^^^^^^^^^
			Resource leak: 'localReader' is never closed
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// three AutoCloseables in different blocks of the same method
public void test056i() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo(boolean f1, boolean f2) throws IOException {
				        File file = new File("somefile");
				        if (f1) {
				            FileReader fileReader = new FileReader(file); // err: not closed
				            char[] in = new char[50];
				            fileReader.read(in);
				            while (true) {
				                 FileReader loopReader = new FileReader(file); // don't warn, properly closed
				                 loopReader.close();\
				                 break;
				            }
				        } else {
				            FileReader fileReader = new FileReader(file); // warn: not closed on all paths
				            if (f2)
				                fileReader.close();
				        }
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo(true, true);
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				FileReader fileReader = new FileReader(file); // err: not closed
				           ^^^^^^^^^^
			Resource leak: 'fileReader' is never closed
			----------
			2. WARNING in X.java (at line 16)
				FileReader fileReader = new FileReader(file); // warn: not closed on all paths
				           ^^^^^^^^^^
			Potential resource leak: 'fileReader' may not be closed
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// three AutoCloseables in different blocks of the same method - problems ignored
public void test056i_ignore() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.IGNORE);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.IGNORE);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo(boolean f1, boolean f2) throws IOException {
				        File file = new File("somefile");
				        if (f1) {
				            FileReader fileReader = new FileReader(file); // err: not closed
				            char[] in = new char[50];
				            fileReader.read(in);
				            while (true) {
				                 FileReader loopReader = new FileReader(file); // don't warn, properly closed
				                 loopReader.close();\
				                 break;
				            }
				        } else {
				            FileReader fileReader = new FileReader(file); // warn: not closed on all paths
				            if (f2)
				                fileReader.close();
				        }
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// three AutoCloseables in different blocks of the same method
public void test056i2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo(boolean f1, boolean f2) throws IOException {
				        File file = new File("somefile");
				        if (f1) {
				            FileReader fileReader = new FileReader(file); // properly closed
				            char[] in = new char[50];
				            fileReader.read(in);
				            while (true) {
				                  fileReader.close();
				                  FileReader loopReader = new FileReader(file); // don't warn, properly closed
				                  loopReader.close();
				                  break;
				            }
				        } else {
				            FileReader fileReader = new FileReader(file); // warn: not closed on all paths
				            if (f2)
				                fileReader.close();
				        }
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo(true, true);
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 18)
				FileReader fileReader = new FileReader(file); // warn: not closed on all paths
				           ^^^^^^^^^^
			Potential resource leak: 'fileReader' may not be closed
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without closing it locally but passing as arg to another method
public void test056j() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				        read(fileReader);
				    }
				    void read(FileReader reader) { }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileReader fileReader = new FileReader(file);\n" +
		"	           ^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("fileReader") +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without closing it locally but passing as arg to another method
public void test056jconditional() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo(boolean b) throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				        synchronized (b ? this : new X()) {
				            new ReadDelegator(fileReader);
				        }
				    }
				    class ReadDelegator { ReadDelegator(FileReader reader) { } }
				    public static void main(String[] args) throws IOException {
				        new X().foo(true);
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileReader fileReader = new FileReader(file);\n" +
		"	           ^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("fileReader") +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// many locals, some are AutoCloseable.
// Unfortunately analysis cannot respect how exception exits may affect ra3 and rb3,
// doing so would create false positives.
public void test056k() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	String expectedProblems = this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"""
					----------
					1. ERROR in X.java (at line 15)
						ra2 = new FileReader(file);
						^^^^^^^^^^^^^^^^^^^^^^^^^^
					Resource leak: \'ra2\' is never closed
					----------
					2. ERROR in X.java (at line 28)
						rb2 = new FileReader(file);
						^^^^^^^^^^^^^^^^^^^^^^^^^^
					Resource leak: \'rb2\' is never closed
					----------
					"""
			:
				"""
					----------
					1. ERROR in X.java (at line 12)
						FileReader ra1 = null, ra2 = null;
						           ^^^
					Resource 'ra1' should be managed by try-with-resource
					----------
					2. ERROR in X.java (at line 15)
						ra2 = new FileReader(file);
						^^^^^^^^^^^^^^^^^^^^^^^^^^
					Resource leak: 'ra2' is never closed
					----------
					3. ERROR in X.java (at line 16)
						FileReader ra3 = new FileReader(file);
						           ^^^
					Resource 'ra3' should be managed by try-with-resource
					----------
					4. ERROR in X.java (at line 25)
						FileReader rb1 = null, rb2 = null;
						           ^^^
					Resource 'rb1' should be managed by try-with-resource
					----------
					5. ERROR in X.java (at line 28)
						rb2 = new FileReader(file);
						^^^^^^^^^^^^^^^^^^^^^^^^^^
					Resource leak: 'rb2' is never closed
					----------
					6. ERROR in X.java (at line 29)
						FileReader rb3 = new FileReader(file);
						           ^^^
					Resource 'rb3' should be managed by try-with-resource
					----------
					""";
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        int i01, i02, i03, i04, i05, i06, i07, i08, i09,
				            i11, i12, i13, i14, i15, i16, i17, i18, i19,
				            i21, i22, i23, i24, i25, i26, i27, i28, i29,
				            i31, i32, i33, i34, i35, i36, i37, i38, i39,
				            i41, i42, i43, i44, i45, i46, i47, i48, i49;
				        File file = new File("somefile");
				        FileReader ra1 = null, ra2 = null;
				        try {
				            ra1 = new FileReader(file);
				            ra2 = new FileReader(file);
				            FileReader ra3 = new FileReader(file);
				            char[] in = new char[50];
				            ra1.read(in);
				            ra2.read(in);
				            ra3.close();
				        } finally {
				            ra1.close();
				        }
				        int i51, i52, i53, i54, i55, i56, i57, i58, i59, i60;
				        FileReader rb1 = null, rb2 = null;
				        try {
				            rb1 = new FileReader(file);
				            rb2 = new FileReader(file);
				            FileReader rb3 = new FileReader(file);
				            char[] in = new char[50];
				            rb1.read(in);
				            rb2.read(in);
				            rb3.close();
				        } finally {
				            rb1.close();
				        }
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		expectedProblems,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// various non-problems
public void test056l() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	String expectedProblems = this.complianceLevel >= ClassFileConstants.JDK1_7 ?
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	FileReader fileReader = getReader();\n" +
				"	           ^^^^^^^^^^\n" +
				"Resource 'fileReader' should be managed by try-with-resource\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
				"	FileReader r3 = getReader();\n" +
				"	           ^^\n" +
				"Resource 'r3' should be managed by try-with-resource\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 24)\n" +
				"	FileReader r2 = new FileReader(new File(\"inexist\")); // only potential problem: ctor X below might close r2\n" +
				"	           ^^\n" +
				potentialLeakOrCloseNotShown("r2") +
				"----------\n" +
				"4. ERROR in X.java (at line 25)\n" +
				"	new X(r2).foo(new FileReader(new File(\"notthere\"))); // potential problem: foo may/may not close the new FileReader\n" +
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
				"----------\n"
			:
				"""
					----------
					1. ERROR in X.java (at line 24)
						FileReader r2 = new FileReader(new File("inexist")); // only potential problem: ctor X below might close r2
						           ^^
					Potential resource leak: 'r2' may not be closed
					----------
					2. ERROR in X.java (at line 25)
						new X(r2).foo(new FileReader(new File("notthere"))); // potential problem: foo may/may not close the new FileReader
						              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Potential resource leak: \'<unassigned Closeable value>\' may not be closed
					----------
					""";
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    X(FileReader r0) {}
				    FileReader getReader() { return null; }
				    void foo(FileReader r1) throws IOException {
				        FileReader fileReader = getReader();
				        if (fileReader == null)
				            return;
				        FileReader r3 = getReader();
				        if (r3 == null)
				            r3 = new FileReader(new File("absent"));
				        try {
				            char[] in = new char[50];
				            fileReader.read(in);
				            r1.read(in);
				        } finally {
				            fileReader.close();
				            r3.close();
				        }
				    }
				    public static void main(String[] args) throws IOException {
				        FileReader r2 = new FileReader(new File("inexist")); // only potential problem: ctor X below might close r2
				        new X(r2).foo(new FileReader(new File("notthere"))); // potential problem: foo may/may not close the new FileReader
				    }
				}
				"""
		},
		expectedProblems,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested try with early exit
public void test056m() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() {
				        File file = new File("somefile");\
				        try {
				            FileReader fileReader = new FileReader(file);
				            try {
				                char[] in = new char[50];
				                if (fileReader.read(in)==0)
				                    return;
				            } finally {
						         fileReader.close();
				            }
				        } catch (IOException e) {
				            System.out.println("caught");
				        }
				    }
				    public static void main(String[] args) {
				        new X().foo();
				    }
				}
				"""
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested try should not interfere with earlier analysis.
public void test056n() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				import java.io.FileNotFoundException;
				public class X {
				    void foo(File someFile, char[] buf) throws IOException {
						FileReader fr1 = new FileReader(someFile);
						try {
							fr1.read(buf);
						} finally {
							fr1.close();
						}
						try {
							FileReader fr3 = new FileReader(someFile);
							try {
							} finally {
								fr3.close();
							}
						} catch (IOException e) {
						}
					 }
				    public static void main(String[] args) throws IOException {
				        try {
				            new X().foo(new File("missing"), new char[100]);
				        } catch (FileNotFoundException e) {
				            System.out.println("caught");
				        }
				    }
				}
				"""
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// if close is guarded by null check this should still be recognized as definitely closed
public void test056o() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				import java.io.FileNotFoundException;
				public class X {
				    void foo(File someFile, char[] buf) throws IOException {
						FileReader fr1 = null;
						try {
				           fr1 = new FileReader(someFile);\
							fr1.read(buf);
						} finally {
							if (fr1 != null)
				               try {
				                   fr1.close();
				               } catch (IOException e) { /*do nothing*/ }
						}
					 }
				    public static void main(String[] args) throws IOException {
				        try {
				            new X().foo(new File("missing"), new char[100]);
				        } catch (FileNotFoundException e) {
				            System.out.println("caught");
				        }
				    }
				}
				"""
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a method uses an AutoCloseable without ever closing it, type from a type variable
public void test056p() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // generics used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.Reader;
				import java.io.IOException;
				public abstract class X <T extends Reader> {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        T fileReader = newReader(file);
				        char[] in = new char[50];
				        fileReader.read(in);
				    }
				    abstract T newReader(File file) throws IOException;
				    public static void main(String[] args) throws IOException {
				        new X<FileReader>() {
				            FileReader newReader(File f) throws IOException { return new FileReader(f); }
				        }.foo();
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	T fileReader = newReader(file);\n" +
		"	  ^^^^^^^^^^\n" +
		potentialOrDefiniteLeak("fileReader") +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// closed in dead code
public void test056q() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				        char[] in = new char[50];
				        fileReader.read(in);
				        if (2*2 == 4)
				        	return;
				        fileReader.close();
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				FileReader fileReader = new FileReader(file);
				           ^^^^^^^^^^
			Resource leak: 'fileReader' is never closed
			----------
			2. WARNING in X.java (at line 10)
				if (2*2 == 4)
				    ^^^^^^^^
			Comparing identical expressions
			----------
			3. WARNING in X.java (at line 12)
				fileReader.close();
				^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// properly closed, dead code in between
public void test056r() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fr = new FileReader(file);
				  		 Object b = null;
				        fr.close();
				        if (b != null) {
				            fr = new FileReader(file);
				            return;
				        } else {
				            System.out.print(42);
				        }
				        return;     // Should not complain about fr
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				if (b != null) {
			            fr = new FileReader(file);
			            return;
			        } else {
				               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			2. WARNING in X.java (at line 13)
				} else {
			            System.out.print(42);
			        }
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource inside t-w-r is re-assigned, shouldn't even record an errorLocation
public void test056s() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        try (FileReader fileReader = new FileReader(file);) {
				            char[] in = new char[50];
				            fileReader.read(in);
				            fileReader = new FileReader(file);  // debug here
				            fileReader.read(in);
				        }
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				fileReader = new FileReader(file);  // debug here
				^^^^^^^^^^
			The resource fileReader of a try-with-resources statement cannot be assigned
			----------
			""",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource is closed, dead code follows
public void test056t() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo31() throws IOException {
				        FileReader reader = new FileReader("file"); //warning
				        if (reader != null) {
				            reader.close();
				        } else {
				            // nop
				        }
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo31();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				} else {
			            // nop
			        }
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource is reassigned within t-w-r with different resource
// was initially broken due to https://bugs.eclipse.org/358827
public void test056u() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo() throws Exception {
				        FileReader reader1 = new FileReader("file1");
				        FileReader reader2 = new FileReader("file2");
				        reader2 = reader1;// this disconnects reader 2
				        try (FileReader reader3 = new FileReader("file3")) {
				            int ch;
				            while ((ch = reader2.read()) != -1) {
				                System.out.println(ch);
				                reader1.read();
				            }
				            reader2 = reader1; // warning 1 regarding original reader1
				            reader2 = reader1; // warning 2 regarding original reader1
				        } finally {
				            if (reader2 != null) {
				                reader2.close();
				            } else {
				                System.out.println();
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				FileReader reader2 = new FileReader("file2");
				           ^^^^^^^
			Resource leak: 'reader2' is never closed
			----------
			2. ERROR in X.java (at line 13)
				reader2 = reader1; // warning 1 regarding original reader1
				^^^^^^^^^^^^^^^^^
			Resource leak: 'reader1' is not closed at this location
			----------
			3. ERROR in X.java (at line 14)
				reader2 = reader1; // warning 2 regarding original reader1
				^^^^^^^^^^^^^^^^^
			Resource leak: 'reader1' is not closed at this location
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// scope-related pbs reported in https://bugs.eclipse.org/349326#c70 and https://bugs.eclipse.org/349326#c82
public void test056v() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	String expectedProblems = this.complianceLevel >= ClassFileConstants.JDK1_7 ?
				"""
					----------
					1. ERROR in X.java (at line 4)
						FileReader reader = new FileReader("file");
						           ^^^^^^
					Resource leak: 'reader' is never closed
					----------
					2. WARNING in X.java (at line 19)
						FileReader reader111 = new FileReader("file2");
						           ^^^^^^^^^
					Resource 'reader111' should be managed by try-with-resource
					----------
					3. ERROR in X.java (at line 42)
						return;
						^^^^^^^
					Resource leak: 'reader2' is not closed at this location
					----------
					"""
			:
				"""
					----------
					1. ERROR in X.java (at line 4)
						FileReader reader = new FileReader("file");
						           ^^^^^^
					Resource leak: 'reader' is never closed
					----------
					2. ERROR in X.java (at line 42)
						return;
						^^^^^^^
					Resource leak: 'reader2' is not closed at this location
					----------
					""";
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    boolean foo1() throws Exception {
				        FileReader reader = new FileReader("file");
				        try {
				            int ch;
				            while ((ch = reader.read()) != -1) {
				                System.out.println(ch);
				                reader.read();
				            }
				            if (ch > 10) {
				                return true;
				            }
				            return false;
				        } finally {
				        }
				    }
				    void foo111() throws Exception {
				        FileReader reader111 = new FileReader("file2");
				        try {
				            int ch;
				            while ((ch = reader111.read()) != -1) {
				                System.out.println(ch);
				                reader111.read();
				            }
				            return;
				        } finally {
				            if (reader111 != null) {
				                reader111.close();
				            }
				        }
				    }
				    void foo2() throws Exception {
				        FileReader reader2 = new FileReader("file");
				        try {
				            int ch;
				            while ((ch = reader2.read()) != -1) {
				                System.out.println(ch);
				                reader2.read();
				            }
				            if (ch > 10) {
				                return;
				            }
				        } finally {
				        }
				        reader2.close();
				    }
				}
				"""
		},
		expectedProblems,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// end of method is dead end, but before we have both a close() and an early return
public void test056w() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    boolean foo1() throws Exception {
				        FileReader reader = new FileReader("file");
				        try {
				            int ch;
				            while ((ch = reader.read()) != -1) {
				                System.out.println(ch);
				                reader.read();
				            }
				            if (ch > 10) {
								 reader.close();
				                return true;
				            }
				            return false;
				        } finally {
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 15)
				return false;
				^^^^^^^^^^^^^
			Resource leak: 'reader' is not closed at this location
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// different early exits, if no close seen report as definitely unclosed
public void test056x() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo31(boolean b) throws Exception {
				        FileReader reader = new FileReader("file");
				        if (b) {
				            reader.close();
				        } else {
				            return; // warning
				        }
				    }
				    void foo32(boolean b) throws Exception {
				        FileReader reader = new FileReader("file"); // warn here
				        return;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				return; // warning
				^^^^^^^
			Resource leak: 'reader' is not closed at this location
			----------
			2. ERROR in X.java (at line 12)
				FileReader reader = new FileReader("file"); // warn here
				           ^^^^^^
			Resource leak: 'reader' is never closed
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested method passes the resource to outside code
public void test056y() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakWarningTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo31(boolean b) throws Exception {
				        final FileReader reader31 = new FileReader("file");
				        new Runnable() {
				            public void run() {
				                foo18(reader31);
				            }
				        }.run();
				    }
				    void foo18(FileReader r18) {
				        // could theoretically close r18;
				    }
				    abstract class ResourceProvider {
				        abstract FileReader provide();\
				    }
				    ResourceProvider provider;\
				    void foo23() throws Exception {
				        final FileReader reader23 = new FileReader("file");
				        provider = new ResourceProvider() {
				            public FileReader provide() {
				                return reader23;
				            }
				        };
				    }
				}
				"""
		},
		getTest056y_log(),
		options);
}
protected String getTest056y_log() {
	return """
		----------
		1. WARNING in X.java (at line 4)
			final FileReader reader31 = new FileReader("file");
			                 ^^^^^^^^
		Potential resource leak: 'reader31' may not be closed
		----------
		""";
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource assigned to second local and is (potentially) closed on the latter
public void test056z() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo17() throws Exception {
				        FileReader reader17 = new FileReader("file");
				        final FileReader readerCopy = reader17;
				        readerCopy.close();
				    }
				    void foo17a() throws Exception {
				        FileReader reader17a = new FileReader("file");
				        FileReader readerCopya;\
						 readerCopya = reader17a;
				        bar(readerCopya);
				    }
				    void bar(FileReader r) {}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	FileReader reader17a = new FileReader(\"file\");\n" +
		"	           ^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("reader17a") +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// multiple early exists from nested scopes (always closed)
public void test056zz() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo16() throws Exception {
				        FileReader reader16 = new FileReader("file");
				        try {
				            reader16.close();
				 \
				            return;
				        } catch (RuntimeException re) {
				            return;
				        } catch (Error e) {
				            return;
				        } finally {
				            reader16.close();
				 \
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				FileReader reader16 = new FileReader("file");
				           ^^^^^^^^
			Resource 'reader16' should be managed by try-with-resource
			----------
			""",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// multiple early exists from nested scopes (never closed)
public void test056zzz() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo16() throws Exception {
				        FileReader reader16 = new FileReader("file");
				        try {
				            return;
				        } catch (RuntimeException re) {
				            return;
				        } catch (Error e) {
				            return;
				        } finally {
				            System.out.println();
				 \
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				FileReader reader16 = new FileReader("file");
				           ^^^^^^^^
			Resource leak: 'reader16' is never closed
			----------
			""",
		options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// explicit throw is a true method exit here
public void test056throw1() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo2(boolean a, boolean b, boolean c) throws Exception {
				        FileReader reader = new FileReader("file");
				        if(a)
				            throw new Exception();    //warning 1
				        else if (b)
				            reader.close();
				        else if(c)
				            throw new Exception();    //warning 2
				        reader.close();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				throw new Exception();    //warning 1
				^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: 'reader' is not closed at this location
			----------
			2. ERROR in X.java (at line 10)
				throw new Exception();    //warning 2
				^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: 'reader' is not closed at this location
			----------
			""",
		options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// close() within finally provides protection for throw
public void test056throw2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo1() throws Exception {
				        FileReader reader = new FileReader("file"); // propose t-w-r
				        try {
				            reader.read();
				            return;
				        } catch (Exception e) {
				            throw new Exception();
				        } finally {
				            reader.close();
				        }
				    }
				
				    void foo2() throws Exception {
				        FileReader reader = new FileReader("file"); // propose t-w-r
				        try {
				            reader.read();
				            throw new Exception(); // should not warn here
				        } catch (Exception e) {
				            throw new Exception();
				        } finally {
				            reader.close();
				        }
				    }
				
				    void foo3() throws Exception {
				        FileReader reader = new FileReader("file"); // propose t-w-r
				        try {
				            reader.read();
				            throw new Exception();
				        } finally {
				            reader.close();
				        }
				    }
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					FileReader reader = new FileReader("file"); // propose t-w-r
					           ^^^^^^
				Resource 'reader' should be managed by try-with-resource
				----------
				2. ERROR in X.java (at line 16)
					FileReader reader = new FileReader("file"); // propose t-w-r
					           ^^^^^^
				Resource 'reader' should be managed by try-with-resource
				----------
				3. ERROR in X.java (at line 28)
					FileReader reader = new FileReader("file"); // propose t-w-r
					           ^^^^^^
				Resource 'reader' should be managed by try-with-resource
				----------
				""",
			options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// close() nested within finally provides protection for throw
public void test056throw3() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo2x() throws Exception {
				        FileReader reader = new FileReader("file"); // propose t-w-r
				        try {
				            reader.read();
				            throw new Exception(); // should not warn here
				        } catch (Exception e) {
				            throw new Exception();
				        } finally {
				            if (reader != null)
				                 try {
				                     reader.close();
				                 } catch (java.io.IOException io) {}
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				FileReader reader = new FileReader("file"); // propose t-w-r
				           ^^^^^^
			Resource 'reader' should be managed by try-with-resource
			----------
			""",
		options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// additional boolean should shed doubt on whether we reach the close() call
public void test056throw4() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo2x(boolean b) throws Exception {
				        FileReader reader = new FileReader("file");
				        try {
				            reader.read();
				            throw new Exception(); // should warn here
				        } catch (Exception e) {
				            throw new Exception(); // should warn here
				        } finally {
				            if (reader != null && b)
				                 try {
				                     reader.close();
				                 } catch (java.io.IOException io) {}
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				throw new Exception(); // should warn here
				^^^^^^^^^^^^^^^^^^^^^^
			Potential resource leak: 'reader' may not be closed at this location
			----------
			2. ERROR in X.java (at line 9)
				throw new Exception(); // should warn here
				^^^^^^^^^^^^^^^^^^^^^^
			Potential resource leak: 'reader' may not be closed at this location
			----------
			""",
		options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// similar to test056throw3() but indirectly calling close(), so doubts remain.
public void test056throw5() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				    void foo2x() throws Exception {
				        FileReader reader = new FileReader("file");
				        try {
				            reader.read();
				            throw new Exception(); // should warn 'may not' here
				        } catch (Exception e) {
				            throw new Exception(); // should warn 'may not' here
				        } finally {
				            doClose(reader);
				        }
				    }
				    void doClose(FileReader r) { try { r.close(); } catch (java.io.IOException ex) {}}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	throw new Exception(); // should warn \'may not\' here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShownAtExit("reader") +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	throw new Exception(); // should warn \'may not\' here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShownAtExit("reader") +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 360908 - Avoid resource leak warning when the underlying/chained resource is closed explicitly
// a resource wrapper is not closed but the underlying resource is
public void test061a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.BufferedInputStream;
				import java.io.FileInputStream;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileInputStream fileStream  = new FileInputStream(file);
				        BufferedInputStream bis = new BufferedInputStream(fileStream);
				        BufferedInputStream doubleWrap = new BufferedInputStream(bis);
				        System.out.println(bis.available());
				        fileStream.close();
				    }
				    void inline() throws IOException {
				        File file = new File("somefile");
				        FileInputStream fileStream;
				        BufferedInputStream bis = new BufferedInputStream(fileStream = new FileInputStream(file));
				        System.out.println(bis.available());
				        fileStream.close();
				    }
				    public static void main(String[] args) throws IOException {
				        try {
				            new X().foo();
				        } catch (IOException ex) {\
				            System.out.println("Got IO Exception");
				        }
				    }
				}
				"""
		},
		"Got IO Exception",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a closeable without OS resource is not closed
public void test061b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.StringReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        StringReader string  = new StringReader("content");
				        System.out.println(string.read());
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"99", // character 'c'
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is not closed but the underlying closeable is resource-free
public void test061c() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.BufferedReader;
				import java.io.StringReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        StringReader input = new StringReader("content");
				        BufferedReader br = new BufferedReader(input);
				        BufferedReader doubleWrap = new BufferedReader(br);
				        System.out.println(br.read());
				    }
				    void inline() throws IOException {
				        BufferedReader br = new BufferedReader(new StringReader("content"));
				        System.out.println(br.read());
				    }
				    public static void main(String[] args) throws IOException {
				        new X().foo();
				    }
				}
				"""
		},
		"99",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is not closed neither is the underlying resource
public void test061d() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.BufferedInputStream;
				import java.io.FileInputStream;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileInputStream fileStream  = new FileInputStream(file);
				        BufferedInputStream bis = new BufferedInputStream(fileStream);
				        BufferedInputStream doubleWrap = new BufferedInputStream(bis);
				        System.out.println(bis.available());
				    }
				    void inline() throws IOException {
				        File file = new File("somefile");
				        BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(file));
				        System.out.println(bis2.available());
				    }
				    public static void main(String[] args) throws IOException {
				        try {
				            new X().foo();
				        } catch (IOException ex) {\
				            System.out.println("Got IO Exception");
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				BufferedInputStream doubleWrap = new BufferedInputStream(bis);
				                    ^^^^^^^^^^
			Resource leak: \'doubleWrap\' is never closed
			----------
			2. ERROR in X.java (at line 15)
				BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(file));
				                    ^^^^
			Resource leak: \'bis2\' is never closed
			----------
			""",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// a resource wrapper is closed closing also the underlying resource
public void test061e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.BufferedInputStream;
				import java.io.FileInputStream;
				import java.io.IOException;
				public class X {
				    FileInputStream fis;\
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileInputStream fileStream  = new FileInputStream(file);
				        BufferedInputStream bis = new BufferedInputStream(fileStream);
				        BufferedInputStream doubleWrap = new BufferedInputStream(bis);
				        System.out.println(bis.available());
				        bis.close();
				    }
				    void inline() throws IOException {
				        File file = new File("somefile");
				        BufferedInputStream bis2 = new BufferedInputStream(fis = new FileInputStream(file));
				        System.out.println(bis2.available());
				        bis2.close();
				        FileInputStream fileStream  = null;
				        BufferedInputStream bis3 = new BufferedInputStream(fileStream = new FileInputStream(file));
				        System.out.println(bis3.available());
				        bis3.close();
				    }
				    public static void main(String[] args) throws IOException {
				        try {
				            new X().foo();
				        } catch (IOException ex) {\
				            System.out.println("Got IO Exception");
				        }
				    }
				}
				"""
		},
		"Got IO Exception",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// a resource wrapper is closed closing also the underlying resource - original test case
public void test061f() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	URL url = FileLocator.toFileURL(FileLocator.find(Platform.getBundle("org.eclipse.jdt.core.tests.compiler"), new Path("META-INF/MANIFEST.MF"), null));
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.InputStream;\n" +
			"import java.io.InputStreamReader;\n" +
			"import java.io.BufferedReader;\n" +
			"import java.io.IOException;\n" +
			"import java.net.URL;\n" +
			"public class X {\n" +
			"    boolean loadURL(final URL url) throws IOException {\n" +
			"        InputStream stream = null;\n" +
			"        BufferedReader reader = null;\n" +
			"        try {\n" +
			"            stream = url.openStream();\n" +
			"            reader = new BufferedReader(new InputStreamReader(stream));\n" +
			"            System.out.println(reader.readLine());\n" +
			"        } finally {\n" +
			"            try {\n" +
			"                if (reader != null)\n" +
			"                    reader.close();\n" +
			"            } catch (IOException x) {\n" +
			"            }\n" +
			"        }\n" +
			"        return false; // 'stream' may not be closed at this location\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        try {\n" +
			"            new X().loadURL(new URL(\""+url.toString()+"\"));\n" +
			"        } catch (IOException ex) {\n" +
			"            System.out.println(\"Got IO Exception\"+ex);\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"Manifest-Version: 1.0",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// a resource wrapper is closed closing also the underlying resource - from a real-world example
public void test061f2() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.OutputStream;
				import java.io.FileOutputStream;
				import java.io.BufferedOutputStream;
				import java.io.IOException;
				public class X {
				    void zork() throws IOException {
						try {
							OutputStream os = null;
							try {
								os = new BufferedOutputStream(new FileOutputStream("somefile"));
								String externalForm = "externalPath";
							} finally {
								if (os != null)
									os.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// a resource wrapper is sent to another method affecting also the underlying resource - from a real-world example
public void test061f3() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.FileNotFoundException;
				import java.io.InputStream;
				import java.io.BufferedInputStream;
				public class X {
				    String loadProfile(File profileFile) {
						try {
							InputStream stream = new BufferedInputStream(new FileInputStream(profileFile));
							return loadProfile(stream);
						} catch (FileNotFoundException e) {
							//null
						}
						return null;
					}
					private String loadProfile(InputStream stream) {
						return null;
					}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	return loadProfile(stream);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShownAtExit("stream") +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 360908 - Avoid resource leak warning when the underlying/chained resource is closed explicitly
// Different points in a resource chain are closed
public void test061g() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.BufferedInputStream;
				import java.io.FileInputStream;
				import java.io.IOException;
				public class X {
				    void closeMiddle() throws IOException {
				        File file = new File("somefile");
				        FileInputStream fileStream  = new FileInputStream(file);
				        BufferedInputStream bis = new BufferedInputStream(fileStream);
				        BufferedInputStream doubleWrap = new BufferedInputStream(bis);
				        System.out.println(bis.available());
				        bis.close();
				    }
				    void closeOuter() throws IOException {
				        File file2 = new File("somefile");
				        FileInputStream fileStream2  = new FileInputStream(file2);
				        BufferedInputStream bis2 = new BufferedInputStream(fileStream2);
				        BufferedInputStream doubleWrap2 = new BufferedInputStream(bis2);
				        System.out.println(bis2.available());
				        doubleWrap2.close();
				    }
				    void neverClosed() throws IOException {
				        File file3 = new File("somefile");
				        FileInputStream fileStream3  = new FileInputStream(file3);
				        BufferedInputStream bis3 = new BufferedInputStream(fileStream3);
				        BufferedInputStream doubleWrap3 = new BufferedInputStream(bis3);
				        System.out.println(doubleWrap3.available());
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 26)
				BufferedInputStream doubleWrap3 = new BufferedInputStream(bis3);
				                    ^^^^^^^^^^^
			Resource leak: \'doubleWrap3\' is never closed
			----------
			""",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 360908 - Avoid resource leak warning when the underlying/chained resource is closed explicitly
// Different points in a resource chain are potentially closed
public void test061h() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.BufferedInputStream;
				import java.io.FileInputStream;
				import java.io.IOException;
				public class X {
				    void closeMiddle(boolean b) throws IOException {
				        File file = new File("somefile");
				        FileInputStream fileStream  = new FileInputStream(file);
				        BufferedInputStream bis = new BufferedInputStream(fileStream);
				        BufferedInputStream doubleWrap = new BufferedInputStream(bis);
				        System.out.println(bis.available());
				        if (b)
				            bis.close();
				    }
				    void closeOuter(boolean b) throws IOException {
				        File file2 = new File("somefile");
				        FileInputStream fileStream2  = new FileInputStream(file2);
				        BufferedInputStream dummy;
				        BufferedInputStream bis2 = (dummy = new BufferedInputStream(fileStream2));
				        BufferedInputStream doubleWrap2 = new BufferedInputStream(bis2);
				        System.out.println(bis2.available());
				        if (b)
				            doubleWrap2.close();
				    }
				    void potAndDef(boolean b) throws IOException {
				        File file3 = new File("somefile");
				        FileInputStream fileStream3  = new FileInputStream(file3);
				        BufferedInputStream bis3 = new BufferedInputStream(fileStream3);
				        BufferedInputStream doubleWrap3 = new BufferedInputStream(bis3);
				        System.out.println(doubleWrap3.available());
				        if (b) bis3.close();
				        fileStream3.close();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				BufferedInputStream doubleWrap = new BufferedInputStream(bis);
				                    ^^^^^^^^^^
			Potential resource leak: \'doubleWrap\' may not be closed
			----------
			2. ERROR in X.java (at line 20)
				BufferedInputStream doubleWrap2 = new BufferedInputStream(bis2);
				                    ^^^^^^^^^^^
			Potential resource leak: \'doubleWrap2\' may not be closed
			----------
			""",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// local var is re-used for two levels of wrappers
public void test061i() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.InputStream;
				import java.io.BufferedInputStream;
				import java.io.FileInputStream;
				import java.io.IOException;
				public class X {
				    void closeMiddle() throws IOException {
				        File file = new File("somefile");
				        InputStream stream  = new FileInputStream(file);
				        stream = new BufferedInputStream(stream);
				        InputStream middle;
				        stream = new BufferedInputStream(middle = stream);
				        System.out.println(stream.available());
				        middle.close();
				    }
				    void closeOuter() throws IOException {
				        File file = new File("somefile");
				        InputStream stream2  = new FileInputStream(file);
				        stream2 = new BufferedInputStream(stream2);
				        stream2 = new BufferedInputStream(stream2);
				        System.out.println(stream2.available());
				        stream2.close();
				    }
				    void neverClosed() throws IOException {
				        File file = new File("somefile");
				        InputStream stream3  = new FileInputStream(file);
				        stream3 = new BufferedInputStream(stream3);
				        stream3 = new BufferedInputStream(stream3);
				        System.out.println(stream3.available());
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 26)
				InputStream stream3  = new FileInputStream(file);
				            ^^^^^^^
			Resource leak: \'stream3\' is never closed
			----------
			""",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// self-wrapping a method argument (caused NPE UnconditionalFlowInfo.markAsDefinitelyNull(..)).
public void test061j() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.InputStream;
				import java.io.BufferedInputStream;
				import java.io.IOException;
				public class X {
				    void foo(InputStream stream) throws IOException {
				        stream = new BufferedInputStream(stream);
				        System.out.println(stream.available());
				        stream.close();
				    }
				    void boo(InputStream stream2) throws IOException {
				        stream2 = new BufferedInputStream(stream2);
				        System.out.println(stream2.available());
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a wrapper is created in a return statement
public void test061k() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.BufferedInputStream;
				import java.io.IOException;
				public class X {
				    BufferedInputStream getReader(File file) throws IOException {
				        FileInputStream stream = new FileInputStream(file);
				        return new BufferedInputStream(stream);
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a closeable is assigned to a field
public void test061l() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.BufferedInputStream;
				import java.io.IOException;
				public class X {
				    BufferedInputStream stream;
				    void foo(File file) throws IOException {
				        FileInputStream s = new FileInputStream(file);
				        stream = new BufferedInputStream(s);
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 361407 - Resource leak warning when resource is assigned to a field outside of constructor
// a closeable is assigned to a field - constructor vs. method
public void test061l2() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"xy/Leaks.java",
			"""
				package xy;
				
				import java.io.FileInputStream;
				import java.io.IOException;
				
				public class Leaks {
				    private FileInputStream fInput;
				
				    Leaks(String name) throws IOException {
				        FileInputStream fileInputStream= new FileInputStream(name);
				        fInput= fileInputStream;
				        Objects.hashCode(fInput);
				       \s
				        init(name);
				    }
				   \s
				    Leaks() throws IOException {
				        this(new FileInputStream("default")); // potential problem
				    }
				   \s
				    Leaks(FileInputStream fis) throws IOException {
				        fInput= fis;
				    }
				    void init(String name) throws IOException {
				        FileInputStream fileInputStream= new FileInputStream(name);
				        fInput= fileInputStream;
				        Objects.hashCode(fInput);
				    }
				   \s
				    public void dispose() throws IOException {
				        fInput.close();
				    }
				}
				class Objects {
				    static int hashCode(Object o) { return 13; }
				}
				"""
		},
		getTest061l2_log(),
		options);
}
protected String getTest061l2_log() {
	return """
		----------
		1. ERROR in xy\\Leaks.java (at line 18)
			this(new FileInputStream("default")); // potential problem
			     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Potential resource leak: '<unassigned Closeable value>' may not be closed
		----------
		""";
}

// Bug 361407 - Resource leak warning when resource is assigned to a field outside of constructor
// a closeable is not assigned to a field - constructor vs. method
public void test061l3() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"xy/Leaks.java",
			"""
				package xy;
				
				import java.io.FileInputStream;
				import java.io.IOException;
				
				public class Leaks {
				
				    Leaks(String name) throws IOException {
				        FileInputStream fileInputStream= new FileInputStream(name);
				        Objects.hashCode(fileInputStream);
				       \s
				        init(name);
				    }
				    void init(String name) throws IOException {
				        FileInputStream fileInputStream= new FileInputStream(name);
				        Objects.hashCode(fileInputStream);
				    }
				}
				class Objects {
				    static int hashCode(Object o) { return 13; }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in xy\\Leaks.java (at line 9)\n" +
		"	FileInputStream fileInputStream= new FileInputStream(name);\n" +
		"	                ^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("fileInputStream") +
		"----------\n" +
		"2. ERROR in xy\\Leaks.java (at line 15)\n" +
		"	FileInputStream fileInputStream= new FileInputStream(name);\n" +
		"	                ^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("fileInputStream") +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a closeable is passed to another method in a return statement
// example constructed after org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository#getArtifact(..)
public void test061m() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.BufferedInputStream;
				import java.io.InputStream;
				import java.io.IOException;
				public class X {
				    BufferedInputStream stream;
				    BufferedInputStream foo(File file) throws IOException {
				        FileInputStream s = new FileInputStream(file);
				        return check(new BufferedInputStream(s));
				    }
				    BufferedInputStream foo2(FileInputStream s, File file) throws IOException {
				        s = new FileInputStream(file);
				        return check(s);
				    }
				    BufferedInputStream foo3(InputStream s) throws IOException {
				        s = check(s);
				        return check(s);
				    }
				    BufferedInputStream check(InputStream s) { return null; }
				}
				"""
		},
		// TODO: also these warnings *might* be avoidable by detecting check(s) as a wrapper creation??
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	return check(new BufferedInputStream(s));\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	return check(s);\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShownAtExit("s") +
		"----------\n" +
		"3. ERROR in X.java (at line 18)\n" +
		"	return check(s);\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShownAtExit("s") +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper does not wrap any provided resource
public void test061n() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.PrintWriter;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        PrintWriter writer = new PrintWriter("filename");
				        writer.write(1);
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				PrintWriter writer = new PrintWriter("filename");
				            ^^^^^^
			Resource leak: \'writer\' is never closed
			----------
			""",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is closed only in its local block, underlying resource may leak
public void test061o() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.BufferedInputStream;
				import java.io.IOException;
				public class X {
				    void foo(boolean bar) throws IOException {
				        File file = new File("somefil");
				        FileInputStream fileStream  = new FileInputStream(file);
				        BufferedInputStream bis = new BufferedInputStream(fileStream);  \s
				        if (bar) {
				            BufferedInputStream doubleWrap = new BufferedInputStream(bis);
				            doubleWrap.close();
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				BufferedInputStream bis = new BufferedInputStream(fileStream);  \s
				                    ^^^
			Potential resource leak: \'bis\' may not be closed
			----------
			""",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is conditionally allocated but not closed - from a real-world example
public void test061f4() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.FileNotFoundException;
				import java.io.InputStream;
				import java.io.BufferedInputStream;
				public class X {
				    	void foo(File location, String adviceFilePath) throws FileNotFoundException {
						InputStream stream = null;
						if (location.isDirectory()) {
							File adviceFile = new File(location, adviceFilePath);
							stream = new BufferedInputStream(new FileInputStream(adviceFile));
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				stream = new BufferedInputStream(new FileInputStream(adviceFile));
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Potential resource leak: \'stream\' may not be closed
			----------
			""",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a t-w-r wraps an existing resource
public void test061p() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.PrintWriter;
				import java.io.BufferedWriter;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        PrintWriter writer = new PrintWriter("filename");
				        try (BufferedWriter bw = new BufferedWriter(writer)) {
				            bw.write(1);
				        }
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a t-w-r potentially wraps an existing resource
// DISABLED, fails because we currently don't include t-w-r managed resources in the analysis
public void _test061q() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.PrintWriter;
				import java.io.BufferedWriter;
				import java.io.IOException;
				public class X {
				    void foo(boolean b) throws IOException {
				        PrintWriter writer = new PrintWriter("filename");
				        if (b)
				            try (BufferedWriter bw = new BufferedWriter(writer)) {
				                bw.write(1);
				            }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				PrintWriter writer = new PrintWriter(\\"filename\\");
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Potential resource leak: \'writer\' may not be closed
			----------
			""",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// the inner from a wrapper is returned
public void test061r() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileInputStream;
				import java.io.File;
				import java.io.BufferedInputStream;
				import java.io.IOException;
				public class X {
				    FileInputStream foo() throws IOException {
				        File file = new File("somefil");
				        FileInputStream fileStream  = new FileInputStream(file);
				        BufferedInputStream bis = new BufferedInputStream(fileStream);  \s
				        return fileStream;
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a wrapper is forgotten, the inner is closed afterwards
public void test061s() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileInputStream;
				import java.io.File;
				import java.io.BufferedInputStream;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefil");
				        FileInputStream fileStream  = new FileInputStream(file);
				        BufferedInputStream bis = new BufferedInputStream(fileStream);
				        bis = null;
				        fileStream.close();
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 362331 - Resource leak not detected when closeable not assigned to variable
// a resource is never assigned
public void test062a() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileOutputStream;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        new FileOutputStream(new File("C:\\temp\\foo.txt")).write(1);
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				new FileOutputStream(new File("C:\\temp\\foo.txt")).write(1);
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			""",
		options);
}
// Bug 362331 - Resource leak not detected when closeable not assigned to variable
// a freshly allocated resource is immediately closed
public void test062b() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileOutputStream;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        new FileOutputStream(new File("C:\\temp\\foo.txt")).close();
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 362331 - Resource leak not detected when closeable not assigned to variable
// a resource is directly passed to another method
public void test062c() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileOutputStream;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        writeIt(new FileOutputStream(new File("C:\\temp\\foo.txt")));
				    }
				    void writeIt(FileOutputStream fos) throws IOException {
				        fos.write(1);
				        fos.close();
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	writeIt(new FileOutputStream(new File(\"C:\\temp\\foo.txt\")));\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n",
		options);
}
// Bug 362331 - Resource leak not detected when closeable not assigned to variable
// a resource is not used
public void test062d() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileOutputStream;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        new FileOutputStream(new File("C:\\temp\\foo.txt"));
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				new FileOutputStream(new File("C:\\temp\\foo.txt"));
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			""",
		options);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a wrapper is obtained from another method
public void test063a() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.BufferedInputStream;
				import java.io.IOException;
				public class X {
				    void read(File file) throws IOException {
				        FileInputStream stream = new FileInputStream(file);
				        BufferedInputStream bis = new BufferedInputStream(stream); // never since reassigned
				        FileInputStream stream2 = new FileInputStream(file); // unsure since passed to method
				        bis = getReader(stream2); // unsure since obtained from method
				        bis.available();
				    }
				    BufferedInputStream getReader(FileInputStream stream) throws IOException {
				        return new BufferedInputStream(stream);
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileInputStream stream = new FileInputStream(file);\n" +
		"	                ^^^^^^\n" +
		"Resource leak: \'stream\' is never closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	FileInputStream stream2 = new FileInputStream(file); // unsure since passed to method\n" +
		"	                ^^^^^^^\n" +
		potentialLeakOrCloseNotShown("stream2") +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	bis = getReader(stream2); // unsure since obtained from method\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialOrDefiniteLeak("bis") +
		"----------\n",
		options);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a wrapper is obtained from a field read
public void test063b() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileInputStream;
				import java.io.BufferedInputStream;
				import java.io.IOException;
				public class X {
				    FileInputStream stream;
				    void read() throws IOException {
				        FileInputStream s = this.stream;
				        BufferedInputStream bis = new BufferedInputStream(s); // don't complain since s is obtained from a field
				        bis.available();
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a wrapper is assigned to a field
public void test063c() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileInputStream;
				import java.io.BufferedInputStream;
				import java.io.IOException;
				public class X {
				    BufferedInputStream stream;
				    void read() throws IOException {
				        FileInputStream s = new FileInputStream("somefile");
				        BufferedInputStream bis = new BufferedInputStream(s);
				        this.stream = bis;
				    }
				}
				"""
		},
		getTest063c_log(),
		options);
}
protected String getTest063c_log() {
	return "";
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a resource is obtained as a method argument and/or assigned with a cast
public void test063d() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"""
				import java.io.FileInputStream;
				import java.io.BufferedInputStream;
				import java.io.InputStream;
				import java.io.IOException;
				public class X {
				    void foo( InputStream input) throws IOException {
				        FileInputStream input1  = (FileInputStream)input;
				        System.out.println(input1.read());
				        input.close();
				    }
				    void foo() throws IOException {
				        InputStream input = new FileInputStream("somefile");
				        FileInputStream input1  = (FileInputStream)input;
				        System.out.println(input1.read());
				        input.close();
				    }
				    void foo3( InputStream input, InputStream input2) throws IOException {
				        FileInputStream input1  = (FileInputStream)input;
				        System.out.println(input1.read());
				        BufferedInputStream bis = new BufferedInputStream(input2);
				        System.out.println(bis.read());
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				InputStream input = new FileInputStream("somefile");
				            ^^^^^
			Resource \'input\' should be managed by try-with-resource
			----------
			""",
		options);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a resource is obtained from a field read, then re-assigned
public void test063e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileInputStream;
				import java.io.IOException;
				public class X {
				    FileInputStream input1;
				    public void foo() throws IOException {
				        FileInputStream input = input1;
				        input = new FileInputStream("adfafd");
				        input.close();
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 368709 - Endless loop in FakedTrackingVariable.markPassedToOutside
// original test case from jgit
public void testBug368709a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				import java.util.zip.*;
				public class X {
				  Object db, pack;
				  int objectOffset, headerLength, type, size;
				  public ObjectStream openStream() throws MissingObjectException, IOException {
				    WindowCursor wc = new WindowCursor(db);
				    InputStream in;
				    try
				      {
				        in = new PackInputStream(pack, (objectOffset + headerLength), wc);
				      }
				    catch (IOException packGone)
				      {
				        return wc.open(getObjectId(), type).openStream();
				      }
				    in = new BufferedInputStream(new InflaterInputStream(in, wc.inflater(), 8192), 8192);
				    return new ObjectStream.Filter(type, size, in);
				  }
				  String getObjectId() { return ""; }
				}
				class WindowCursor {
				    WindowCursor(Object db) {}
				    ObjectStream open(String id, int type) { return null; }
				    Inflater inflater() { return null; }
				}
				class MissingObjectException extends Exception {
				    public static final long serialVersionUID = 13L;
				    MissingObjectException() { super();}
				}
				class PackInputStream extends InputStream {
				    PackInputStream(Object pack, int offset, WindowCursor wc) throws IOException {}
				    public int read() { return 0; }
				}
				class ObjectStream extends InputStream {
				    static class Filter extends ObjectStream {
				        Filter(int type, int size, InputStream in) { }
				    }
				    ObjectStream openStream() { return this; }
				    public int read() { return 0; }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	return wc.open(getObjectId(), type).openStream();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialOrDefiniteLeak("<unassigned Closeable value>") +
		"----------\n" +
		"2. ERROR in X.java (at line 18)\n" +
		"	return new ObjectStream.Filter(type, size, in);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShownAtExit("in") +
		"----------\n",
		options);
}
// Bug 368709 - Endless loop in FakedTrackingVariable.markPassedToOutside
// minimal test case: constructing an indirect self-wrapper
public void testBug368709b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				import java.util.zip.*;
				public class X {
				  void doit() throws IOException {
				    InputStream in = new FileInputStream("somefile");
				    in = new BufferedInputStream(new InflaterInputStream(in, inflater(), 8192), 8192);
				    process(in);
				  }
				  Inflater inflater() { return null; }
				  void process(InputStream is) { }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	InputStream in = new FileInputStream(\"somefile\");\n" +
		"	            ^^\n" +
		potentialLeakOrCloseNotShown("in") +
		"----------\n",
		options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 3
public void test064() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(new String[] {
		"Test064.java",
		"""
			import java.io.*;
			public class Test064 {
			    void foo(File outfile) {
			        OutputStream out= System.out;
			        if (outfile != null) {
			            try {
			                out = new FileOutputStream(outfile);
			            } catch (java.io.IOException e) {
			                throw new RuntimeException(e);
			            }
			        }
			        setOutput(out);
			    }
			    private void setOutput(OutputStream out) { }
			}
			"""
	},
	"""
		----------
		1. ERROR in Test064.java (at line 7)
			out = new FileOutputStream(outfile);
			^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Potential resource leak: \'out\' may not be closed
		----------
		""",
	options);
}
// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 10
// disabled, because basic null-analysis machinery doesn't support this pattern
// see also Bug 370424 - [compiler][null] throw-catch analysis for null flow could be more precise
public void _test065() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test065.java",
		"""
			import java.io.*;
			class MyException extends Exception{}
			public class Test065 {
				void foo(String fileName) throws IOException, MyException {
					FileReader       fileRead   = new FileReader(fileName);
					BufferedReader   bufRead    = new BufferedReader(fileRead);
					LineNumberReader lineReader = new LineNumberReader(bufRead);
					try {
					while (lineReader.readLine() != null) {
						bufRead.close();
						callSome();  // only this can throw MyException
					}
					} catch (MyException e) {
						throw e;  // Pot. leak reported here
					}
					bufRead.close();\s
				}
				private void callSome() throws MyException
				{
				\t
				}
			}
			"""
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 11
public void test066() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test066.java",
		"""
			import java.io.*;
			class MyException extends Exception{}
			public class Test066 {
			    void countFileLines(String fileName) throws IOException {
					FileReader       fileRead   = new FileReader(fileName);
					BufferedReader   bufRead    = new BufferedReader(fileRead);
					LineNumberReader lineReader = new LineNumberReader(bufRead);
					while (lineReader.readLine() != null) {
						if (lineReader.markSupported())
			               throw new IOException();
						bufRead.close();
					}
					bufRead.close();
				}
			}
			"""
	},
	"""
		----------
		1. ERROR in Test066.java (at line 10)
			throw new IOException();
			^^^^^^^^^^^^^^^^^^^^^^^^
		Potential resource leak: \'lineReader\' may not be closed at this location
		----------
		""",
	options);
}
// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 11 - variant with closing top-level resource
public void test066b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test066.java",
		"""
			import java.io.*;
			class MyException extends Exception{}
			public class Test066 {
			    void countFileLines(String fileName) throws IOException {
					FileReader       fileRead   = new FileReader(fileName);
					BufferedReader   bufRead    = new BufferedReader(fileRead);
					LineNumberReader lineReader = new LineNumberReader(bufRead);
					while (lineReader.readLine() != null) {
						if (lineReader.markSupported())
			               throw new IOException();
						lineReader.close();
					}
					lineReader.close();
				}
			}
			"""
	},
	"""
		----------
		1. ERROR in Test066.java (at line 10)
			throw new IOException();
			^^^^^^^^^^^^^^^^^^^^^^^^
		Potential resource leak: \'lineReader\' may not be closed at this location
		----------
		""",
	options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 12
// Red herring (disabled): warning says "potential" because in the exception case no resource
// would actually be allocated.
public void _test067() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test067.java",
		"""
			import java.io.*;
			public class Test067 {
				public void comment12() throws IOException {
			    	LineNumberReader o = null;
			    	try {
			    		o = new LineNumberReader(null);    	\t
			    	} catch (NumberFormatException e) {    	\t
			    	}
			    }
			}
			"""
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 12
public void test067b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test067.java",
		"""
			import java.io.*;
			public class Test067 {
				public void comment12b() throws IOException {
					LineNumberReader o = new LineNumberReader(null);
			    	try {
			    		o.close();
			    	} catch (NumberFormatException e) {
			    	}
			    }
			}
			"""
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 13
public void test068() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test068.java",
		"""
			import java.io.*;
			public class Test068 {
				class ProcessingStep extends OutputStream {
					public void write(int b) throws IOException {}
					public OutputStream getDestination() { return null; }
				}
				class ArtifactOutputStream  extends OutputStream {
					public void write(int b) throws IOException {}
				}\
				ArtifactOutputStream comment13(OutputStream stream) {
					OutputStream current = stream;
					while (current instanceof ProcessingStep)
						current = ((ProcessingStep) current).getDestination();
					if (current instanceof ArtifactOutputStream)
						return (ArtifactOutputStream) current;
					return null;
				}
			}
			"""
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 16
public void test069() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // generics used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test069.java",
		"""
			import java.io.*;
			import java.util.Collection;
			public class Test069 {
				class Profile {}
				class CoreException extends Exception {}
				void writeProfilesToStream(Collection<Profile> p, OutputStream s, String enc) {}
				CoreException createException(IOException ioex, String message) { return new CoreException(); }
				public void comment16(Collection<Profile> profiles, File file, String encoding) throws CoreException {
					final OutputStream stream;
					try {
						stream= new FileOutputStream(file);
						try {
							writeProfilesToStream(profiles, stream, encoding);
						} finally {
							try { stream.close(); } catch (IOException e) { /* ignore */ }
						}
					} catch (IOException e) {
						throw createException(e, "message"); // should not shout here
					}
				}
			}
			"""
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// referenced in array initializer
public void test070() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test070.java",
		"""
			import java.io.*;
			public class Test070 {
			    void storeInArray(String fileName) throws IOException {
					FileReader       fileRead   = new FileReader(fileName);
					closeThemAll(new FileReader[] { fileRead });
				}
			   void closeThemAll(FileReader[] readers) { }
			}
			"""
	},
	"""
		----------
		1. ERROR in Test070.java (at line 4)
			FileReader       fileRead   = new FileReader(fileName);
			                 ^^^^^^^^
		Potential resource leak: \'fileRead\' may not be closed
		----------
		""",
	options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// referenced in array initializer
public void test071() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test071.java",
		"""
			import java.io.*;
			public class Test071 {
			    class ReaderHolder {
					FileReader reader;
				}
				private FileReader getReader() {
					return null;
				}
				void invokeCompiler(ReaderHolder readerHolder, boolean flag) throws FileNotFoundException {
					FileReader reader = readerHolder.reader;
					if (reader == null)
						reader = getReader();
					try {
						return;
					} finally {
						try {
							if (flag)
								reader.close();
						} catch (IOException e) {
							// nop
						}
					}
				}
			}
			"""
	},
	"""
		----------
		1. ERROR in Test071.java (at line 14)
			return;
			^^^^^^^
		Potential resource leak: \'reader\' may not be closed at this location
		----------
		""",
	options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// referenced in array initializer
// disabled because it would require correlation analysis between the tracking variable and its original
// need to pass to downstream: either (nonnull & open) or (null)
public void _test071b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test071b.java",
		"""
			import java.io.*;
			public class Test071b {
			   private FileReader getReader() {
					return null;
				}
				void invokeCompiler(boolean flag) throws FileNotFoundException {
					FileReader reader = null;
					if (flag)
						reader = new FileReader("file");
					if (reader == null)
						reader = getReader();
					try {
						return;
					} finally {
						try {
							if (flag)
								reader.close();
						} catch (IOException e) {
							// nop
						}
					}
				}
			}
			"""
	},
	"""
		----------
		1. ERROR in Test071b.java (at line 13)
			return;
			^^^^^^^
		Potential resource leak: \'reader\' may not be closed at this location
		----------
		""",
	options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// throw inside loop inside try - while closed in finally
public void test072() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test072.java",
		"""
			import java.io.*;
			public class Test072 {
			   void readState(File file) {
					DataInputStream in = null;
					try {
						in= new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
						int sizeOfFlags = in.readInt();
						for (int i = 0; i < sizeOfFlags; ++i) {
							String childPath = in.readUTF();
							if (childPath.length() == 0)
								throw new IOException();
						}
					}
					catch (IOException ioe) { /* nop */ }
					finally {
						if (in != null) {
							try {in.close();} catch (IOException ioe) {}
						}
					}
				}
			}
			"""
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// unspecific parameter is casted into a resource, yet need to mark as OWNED_BY_OUTSIDE
public void test073() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test073.java",
		"""
			import java.io.*;
			public class Test073 {
			   String getEncoding(Object reader) {
					if (reader instanceof FileReader) {
						final FileReader fr = (FileReader) reader;
						return fr.getEncoding();
					}
					return null;
				}
			}
			"""
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// status after nested try-finally
public void test074() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test074.java",
		"""
			import java.io.*;
			public class Test074 {
			   void foo() throws FileNotFoundException {
					FileOutputStream out = null;
					try {
						out = new FileOutputStream("outfile");
					} finally {
						try {
							out.flush();
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						out = null;
					}
				}
			}
			"""
	},
	"""
		----------
		1. ERROR in Test074.java (at line 14)
			out = null;
			^^^^^^^^^^
		Potential resource leak: \'out\' may not be closed at this location
		----------
		""",
	options);
}
// Bug 370639 - [compiler][resource] restore the default for resource leak warnings
// check that the default is warning
public void test075() {
	runLeakWarningTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() throws IOException {
				        File file = new File("somefile");
				        FileReader fileReader = new FileReader(file);
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 7)
				FileReader fileReader = new FileReader(file);
				           ^^^^^^^^^^
			Resource leak: 'fileReader' is never closed
			----------
			""",
		getCompilerOptions());
}
// Bug 385415 - Incorrect resource leak detection
public void testBug385415() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
				    void foo() throws FileNotFoundException {
				        FileReader fileReader = new FileReader("somefile");
				        try {
				            fileReader.close();
				        } catch (Exception e) {
				            e.printStackTrace();
				            return;
				        }
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// test case from comment 7
// Duplicate of Bug 385415 - Incorrect resource leak detection
public void testBug361073c7() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
				  public void test() {
				    BufferedReader br = null;
				    try {
				        br = new BufferedReader(new FileReader("blah"));
				        String line = null;
				        while ( (line = br.readLine()) != null ) {
				            if ( line.startsWith("error") )
				                throw new Exception("error"); //Resource leak: 'br' is not closed at this location
				        }
				    } catch (Throwable t) {
				        t.printStackTrace();
				    } finally {
				        if ( br != null ) {
				            try { br.close(); }
				            catch (Throwable e) { br = null; }
				        }
				    }
				  }
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// Bug 386534 - "Potential resource leak" false positive warning
// DISABLED
public void _testBug386534() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug.java",
			"""
				import java.io.FileNotFoundException;
				import java.io.IOException;
				import java.io.OutputStream;
				
				public class Bug {
					private static final String DETAILS_FILE_NAME = null;
					private static final String LOG_TAG = null;
					private static Context sContext;
					static void saveDetails(byte[] detailsData) {
						OutputStream os = null;
						try {
							os = sContext.openFileOutput(DETAILS_FILE_NAME,
									Context.MODE_PRIVATE);
							os.write(detailsData);
						} catch (IOException e) {
							Log.w(LOG_TAG, "Unable to save details", e);
						} finally {
							if (os != null) {
								try {
									os.close();
								} catch (IOException ignored) {
								}
							}
						}
					}
					static class Context {
						public static final String MODE_PRIVATE = null;
						public OutputStream openFileOutput(String detailsFileName,
								String modePrivate) throws FileNotFoundException{
							return null;
						}
					}
					static class Log {
						public static void w(String logTag, String string, IOException e) {
						}
					}
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// https://bugs.eclipse.org/388996 - [compiler][resource] Incorrect 'potential resource leak'
public void testBug388996() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug.java",
			"""
				import java.io.*;
				public class Bug {
					public void processRequest(ResponseContext responseContext) throws IOException {
						OutputStream bao = null;
				
						try {
							HttpServletResponse response = responseContext.getResponse();
				
							bao = response.getOutputStream(); // <<<<
						} finally {
							if(bao != null) {
								bao.close();
							}
						}
					}\
				}
				class ResponseContext {
					public HttpServletResponse getResponse() {
						return null;
					}
				}
				class HttpServletResponse {
					public OutputStream getOutputStream() {
						return null;
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// https://bugs.eclipse.org/386534 -  [compiler][resource] "Potential resource leak" false positive warning
public void testBug386534() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug386534.java",
			"""
				import java.io.FileNotFoundException;
				import java.io.IOException;
				import java.io.OutputStream;
				
				public class Bug386534 {
					private static final String DETAILS_FILE_NAME = null;
					private static final String LOG_TAG = null;
					private static Context sContext;
					static void saveDetails(byte[] detailsData) {
						OutputStream os = null;
						try {
							os = sContext.openFileOutput(DETAILS_FILE_NAME,
									Context.MODE_PRIVATE);
							os.write(detailsData);
						} catch (IOException e) {
							Log.w(LOG_TAG, "Unable to save details", e);
						} finally {
							if (os != null) {
								try {
									os.close();
								} catch (IOException ignored) {
								}
							}
						}
					}
					static class Context {
						public static final String MODE_PRIVATE = null;
						public OutputStream openFileOutput(String detailsFileName,
								String modePrivate) throws FileNotFoundException{
							return null;
						}
					}
					static class Log {
						public static void w(String logTag, String string, IOException e) {
						}
					}
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

//https://bugs.eclipse.org/386534 -  [compiler][resource] "Potential resource leak" false positive warning
public void testBug394768() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug394768.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.InputStream;
				
				public class Bug394768 {
					public void readFile(String path) throws Exception {
						InputStream stream = null;
						File file = new File(path);
				
						if (file.exists())
							stream = new FileInputStream(path);
						else
							stream = getClass().getClassLoader().getResourceAsStream(path);
				
						if (stream == null)
							return;
				
						try {
							// Use the opened stream here
							stream.read();
						} finally {
							stream.close();
						}
					}
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// https://bugs.eclipse.org/386534 -  [compiler][resource] "Potential resource leak" false positive warning
// variation: 2nd branch closes and nulls the newly acquired resource
public void testBug394768_1() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug394768.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.InputStream;
				
				public class Bug394768 {
					public void readFile(String path) throws Exception {
						InputStream stream = null;
						File file = new File(path);
				
						if (file.exists()) {
							stream = new FileInputStream(path);
						} else {
							stream = getClass().getClassLoader().getResourceAsStream(path);\
				           stream.close();
				           stream = null;
				       }
				
						if (stream == null)
							return;
				
						try {
							// Use the opened stream here
							stream.read();
						} finally {
							stream.close();
						}
					}
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// Bug 381445 - [compiler][resource] Can the resource leak check be made aware of Closeables.closeQuietly?
// A resource is closed using various known close helpers
public void testBug381445_1() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			GUAVA_CLOSEABLES_JAVA,
			GUAVA_CLOSEABLES_CONTENT,
			"org/apache/commons/io/IOUtils.java",
			"""
				package org.apache.commons.io;
				public class IOUtils {
				    public static void closeQuietly(java.io.Closeable closeable) {}
				}
				""",
			"Bug381445.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.InputStream;
				
				public class Bug381445 {
					public void readFile(String path) throws Exception {
						File file = new File(path);
						InputStream stream1 = new FileInputStream(path);
						InputStream stream2 = new FileInputStream(path);
						InputStream stream3 = new FileInputStream(path);
						InputStream stream4 = new FileInputStream(path);
						try {
							// Use the opened streams here
							stream1.read();
							stream2.read();
							stream3.read();
							stream4.read();
						} finally {
							com.google.common.io.Closeables.closeQuietly(stream1);
							com.google.common.io.Closeables.close(stream2, false);
							org.apache.commons.io.IOUtils.closeQuietly(stream3);
							Closeables.closeQuietly(stream4);
						}
					}
				}
				class Closeables {
					public static void closeQuietly(java.io.Closeable closeable) {}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in Bug381445.java (at line 11)\n" +
		"	InputStream stream4 = new FileInputStream(path);\n" +
		"	            ^^^^^^^\n" +
		potentialLeakOrCloseNotShown("stream4") +
		"----------\n",
		options);
}

// Bug 405569 - Resource leak check false positive when using DbUtils.closeQuietly
// A resource is closed using more known close helpers
public void testBug381445_1b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // need AutoCloseable in apache's DbUtils
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			APACHE_DBUTILS_JAVA,
			APACHE_DBUTILS_CONTENT,
			"Bug381445.java",
			"""
				import java.sql.*;
				
				public class Bug381445 {
					public void performQuery1(String url, String q1, String q2) throws Exception {
						Connection conn = DriverManager.getConnection(url);
						Statement stat = conn.createStatement();
						ResultSet rset = stat.executeQuery(q1);
						ResultSet rset2 = stat.executeQuery(q2);
						try {
							// empty
						} finally {
							org.apache.commons.dbutils.DbUtils.closeQuietly(conn);
							org.apache.commons.dbutils.DbUtils.close(stat);
							org.apache.commons.dbutils.DbUtils.closeQuietly(rset);
							Closeables.closeQuietly(rset2);
						}
					}
					public void performQuery2(String url, String q1, String q2) throws Exception {
						Connection conn = DriverManager.getConnection(url);
						Statement stat = conn.createStatement();
						ResultSet rset = stat.executeQuery(q1);
						try {
							// empty
						} finally {
							org.apache.commons.dbutils.DbUtils.closeQuietly(conn, stat, rset);
						}
					}
				}
				class Closeables {
					public static void closeQuietly(java.lang.AutoCloseable closeable) {}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in Bug381445.java (at line 8)\n" +
		"	ResultSet rset2 = stat.executeQuery(q2);\n" +
		"	          ^^^^^\n" +
		potentialLeakOrCloseNotShown("rset2") +
		"----------\n",
		options);
}

// Bug 381445 - [compiler][resource] Can the resource leak check be made aware of Closeables.closeQuietly?
// A resource is closed in different places of the flow
public void testBug381445_2() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			GUAVA_CLOSEABLES_JAVA,
			GUAVA_CLOSEABLES_CONTENT,
			"Bug381445.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.InputStream;
				import com.google.common.io.Closeables;
				
				public class Bug381445 {
					public void readFile(String path) throws Exception {
						File file = new File(path);
						InputStream stream1 = new FileInputStream(path);
						InputStream stream2 = new FileInputStream(path);
						InputStream stream3 = new FileInputStream(path);
						try {
							// Use the opened streams here
							stream1.read();
							Closeables.closeQuietly(stream1);
							stream2.read();
							if (path.length() > 2)
								Closeables.closeQuietly(stream2);
							stream3.read();
						} finally {
						}
						Closeables.closeQuietly(stream3);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Bug381445.java (at line 10)
				InputStream stream2 = new FileInputStream(path);
				            ^^^^^^^
			Potential resource leak: \'stream2\' may not be closed
			----------
			""",
		options);
}

// Bug 381445 - [compiler][resource] Can the resource leak check be made aware of Closeables.closeQuietly?
// A close helper is referenced in various ways:
public void testBug381445_3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // using static import
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			GUAVA_CLOSEABLES_JAVA,
			GUAVA_CLOSEABLES_CONTENT,
			"Bug381445a.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.InputStream;
				import static com.google.common.io.Closeables.closeQuietly;
				
				public class Bug381445a {
					public void readFile(String path) throws Exception {
						File file = new File(path);
						InputStream stream = new FileInputStream(path);
						try {
							// Use the opened stream here
							stream.read();
						} finally {
							closeQuietly(stream);
						}
					}
				}
				""",
			"Bug381445b.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.InputStream;
				import com.google.common.io.Closeables;
				
				public class Bug381445b extends Closeables {
					public void readFile(String path) throws Exception {
						File file = new File(path);
						InputStream stream = new FileInputStream(path);
						try {
							// Use the opened streams here
							stream.read();
						} finally {
							closeQuietly(stream);
						}
					}
				}
				""",
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
// original test case
public void testBug395977() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"WriterTest.java",
			"""
				import java.io.*;
				
				public class WriterTest implements Runnable
				{
				   private BufferedWriter m_Writer;
				  \s
				   public void run()
				   {
				      try
				      {
				         initializeWriter();
				        \s
				         m_Writer.write("string");
				         m_Writer.newLine();
				        \s
				         closeWriter();
				      }
				      catch (IOException ioe)
				      {
				         ioe.printStackTrace();
				      }
				   }
				  \s
				   private void initializeWriter()
				      throws UnsupportedEncodingException, FileNotFoundException
				   {
				      m_Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("file"), "UTF-8"))
				      {
				         /**
				          * Writes an LF character on all platforms, to avoid constantly flipping the line terminator style.
				          */
				         public void newLine() throws IOException
				         {
				            write('\\n');
				         }
				      };
				   }
				  \s
				   private void closeWriter()
				      throws IOException
				   {
				      m_Writer.close();
				   }
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

//Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
//variant with named local class - accept as a secure resource wrapper since no close method
public void testBug395977_1() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"WriterTest.java",
			"""
				import java.io.*;
				
				public class WriterTest implements Runnable
				{
				   private BufferedWriter m_Writer;
				  \s
				   public void run()
				   {
				      try
				      {
				         initializeWriter();
				        \s
				         m_Writer.write("string");
				         m_Writer.newLine();
				        \s
				         closeWriter();
				      }
				      catch (IOException ioe)
				      {
				         ioe.printStackTrace();
				      }
				   }
				  \s
				   private void initializeWriter()
				      throws UnsupportedEncodingException, FileNotFoundException
				   {
				      class MyBufferedWriter extends BufferedWriter
				      {
				         MyBufferedWriter(OutputStreamWriter writer) { super(writer); }
				         /**
				          * Writes an LF character on all platforms, to avoid constantly flipping the line terminator style.
				          */
				         public void newLine() throws IOException
				         {
				            write('\\n');
				         }
				      };\
				      m_Writer = new MyBufferedWriter(new OutputStreamWriter(new FileOutputStream("file"), "UTF-8"));
				   }
				  \s
				   private void closeWriter()
				      throws IOException
				   {
				      m_Writer.close();
				   }
				}"""
		},
		"",
		options);
}
//Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
//variant with named local class - don't accept as a secure resource wrapper since close() method exist
public void testBug395977_1a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"WriterTest.java",
			"""
				import java.io.*;
				
				public class WriterTest implements Runnable
				{
				   private BufferedWriter m_Writer;
				  \s
				   public void run()
				   {
				      try
				      {
				         initializeWriter();
				        \s
				         m_Writer.write("string");
				         m_Writer.newLine();
				        \s
				         closeWriter();
				      }
				      catch (IOException ioe)
				      {
				         ioe.printStackTrace();
				      }
				   }
				  \s
				   private void initializeWriter()
				      throws UnsupportedEncodingException, FileNotFoundException
				   {
				      class MyBufferedWriter extends BufferedWriter
				      {
				         MyBufferedWriter(OutputStreamWriter writer) { super(writer); }
				         /**
				          * Writes an LF character on all platforms, to avoid constantly flipping the line terminator style.
				          */
				         public void newLine() throws IOException
				         {
				            write('\\n');
				         }
				         public void close() {}
				      };\
				      m_Writer = new MyBufferedWriter(new OutputStreamWriter(new FileOutputStream("file"), "UTF-8"));
				   }
				  \s
				   private void closeWriter()
				      throws IOException
				   {
				      m_Writer.close();
				   }
				}"""
		},
		"----------\n" +
		"1. ERROR in WriterTest.java (at line 38)\n" +
		"	};      m_Writer = new MyBufferedWriter(new OutputStreamWriter(new FileOutputStream(\"file\"), \"UTF-8\"));\n" +
		"	                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n",
		options);
}

// Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
// anonymous class tries to "cheat" by overriding close()
public void testBug395977_2() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"WriterTest.java",
			"""
				import java.io.*;
				
				public class WriterTest implements Runnable
				{
				   private BufferedWriter m_Writer;
				  \s
				   public void run()
				   {
				      try
				      {
				         initializeWriter();
				        \s
				         m_Writer.write("string");
				         m_Writer.newLine();
				        \s
				         closeWriter();
				      }
				      catch (IOException ioe)
				      {
				         ioe.printStackTrace();
				      }
				   }
				  \s
				   private void initializeWriter()
				      throws UnsupportedEncodingException, FileNotFoundException
				   {
				      m_Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("file"), "UTF-8"))
				      {
				         public void close() { /* nop */}
				      };
				   }
				  \s
				   private void closeWriter()
				      throws IOException
				   {
				      m_Writer.close();
				   }
				}"""
		},
		"----------\n" +
		"1. ERROR in WriterTest.java (at line 27)\n" +
		"	m_Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(\"file\"), \"UTF-8\"))\n" +
		"	                              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n",
		options);
}

// Bug 376053 - [compiler][resource] Strange potential resource leak problems
// include line number when reporting against <unassigned Closeable value>
// UPDATE: never complain 'at this location' against unassigned Closeable,
// hence no line number is needed.
public void testBug376053() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"Try.java",
			"""
				package xy;
				
				import java.io.FileNotFoundException;
				import java.io.PrintStream;
				
				public class Try {
				    public static void main(String[] args) throws FileNotFoundException {
				        System.setOut(new PrintStream("log.txt"));
				       \s
				        if (Math.random() > .5) {
				            return;
				        }
				        System.out.println("Hello World");
				        return;
				    }
				}"""
		},
		"----------\n" +
		"1. ERROR in Try.java (at line 8)\n" +
		"	System.setOut(new PrintStream(\"log.txt\"));\n" +
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n",
		options);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test1() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.io.*;
				
				class A {
				  void a(boolean b) throws Exception {
				    try(FileInputStream in = b ? new FileInputStream("a") : null){}
				  }
				}"""
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"A.java",
			"""
				import java.io.*;
				class A {
				  void a(boolean b) throws Exception {
				    try(FileInputStream in = create(new FileInputStream("a"))){}
				  }
				  FileInputStream create(FileInputStream ignored) throws IOException {
				    return new FileInputStream("b");\s
				  }
				}"""
		},
		"----------\n" +
		"1. ERROR in A.java (at line 4)\n" +
		"	try(FileInputStream in = create(new FileInputStream(\"a\"))){}\n" +
		"	                                ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n",
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"A.java",
			"""
				import java.io.*;
				class A {
					void m() throws IOException {
						try (FileInputStream a = new FileInputStream("A") {{
								FileInputStream b = new FileInputStream("B");
								b.hashCode();
							}}){
						}
					}
				}"""
		},
		"""
			----------
			1. ERROR in A.java (at line 5)
				FileInputStream b = new FileInputStream("B");
				                ^
			Resource leak: 'b' is never closed
			----------
			""",
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.io.FileInputStream;
				class A {
					void testB(boolean b) throws Exception {
						FileInputStream in = null;
						try {
							in = b ? new FileInputStream("a") : null;
						} finally {
						in.close();
						}
					}
				}"""
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test5() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.io.FileInputStream;
				class A {
				  void testA(boolean b) throws Exception {
				    FileInputStream in = b ? new FileInputStream("a") : null;
				    in.close();
				  }
				}"""
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test6() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.io.FileInputStream;
				class A {
				  void testA(boolean b) throws Exception {
				    FileInputStream in = b ? new FileInputStream("a") : new FileInputStream("b");
				    in.close();
				  }
				}"""
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
// challenge nested resource allocations
public void testBug411098_test7() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.io.*;
				class A {
				  void testA(boolean b) throws Exception {
				    BufferedReader in = b ? new BufferedReader(new FileReader("a")) : new BufferedReader(new FileReader("b"));
				    in.close();
				  }
				}"""
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
// field read should not trigger a warning.
public void testBug411098_comment19() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.io.PrintWriter;
				public class A {
					PrintWriter fWriter;
					void bug(boolean useField) {
						PrintWriter bug= useField ? fWriter : null;
						System.out.println(bug);
					}
				}"""
		},
		"",
		options
		);
}
// normal java.util.stream.Stream doesn't hold on to any resources
public void testStream1() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.util.*;
				import java.util.stream.Stream;
				class A {
				  long test(List<String> ss) {
				    Stream<String> stream = ss.stream();
				    return stream.count();
				  }
				}"""
		},
		options
		);
}
// normal java.util.stream.IntStream doesn't hold on to any resources
public void testStream1_Int() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.util.stream.*;
				class A {
				    public void f(Stream<Object> s) {
				        IntStream n = s.mapToInt(Object::hashCode);
				        IntStream n2 = IntStream.range(23, 42);
				        n.forEach(i -> System.out.println(i));
				        n2.forEach(i -> System.out.println(i));
				    }
				}"""
		},
		options
		);
}
// normal java.util.stream.{Double,Long}Stream doesn't hold on to any resources
public void testStream1_Double_Long() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.util.stream.*;
				class A {
				    public void f(Stream<Object> s) {
				        DoubleStream n = s.mapToDouble(o -> 0.2);
				        LongStream n2 = LongStream.range(23, 42);
				        n.forEach(i -> System.out.println(i));
				        n2.forEach(i -> System.out.println(i));
				    }
				}"""
		},
		options
		);
}
// normal java.util.stream.{Double,Long}Stream doesn't hold on to any resources
public void testStreamEx_572707() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses JRE 8 API

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			STREAMEX_JAVA,
			STREAMEX_CONTENT,
			"Bug572707.java",
			"""
				import one.util.streamex.*;
				
				public class Bug572707 {
					public void m() {
						System.out.println(StreamEx.create());
					}
				}
				"""
		},
		options);
}
// Functions java.nio.file.Files.x() returning *Stream* do produce a resource needing closing
public void testStream2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"A.java",
			"""
				import java.util.stream.Stream;
				import java.nio.file.*;
				class A {
				  long test(Path start, FileVisitOption... options) throws java.io.IOException {
				    Stream<Path> stream = Files.walk(start, options);
				    return stream.count();
				  }
				}"""
		},
		"""
			----------
			1. ERROR in A.java (at line 5)
				Stream<Path> stream = Files.walk(start, options);
				             ^^^^^^
			Resource leak: \'stream\' is never closed
			----------
			""",
		options
		);
}
// closeable, but Stream, but produced by Files.m, but only potentially closed:
public void testStream3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"A.java",
			"""
				import java.util.stream.Stream;
				import java.nio.file.*;
				class A {
				  void test(Path file) throws java.io.IOException {
				    Stream<String> lines = Files.lines(file);
				    if (lines.count() > 0)\
				    	lines.close();
				  }
				}"""
		},
		"""
			----------
			1. ERROR in A.java (at line 5)
				Stream<String> lines = Files.lines(file);
				               ^^^^^
			Potential resource leak: \'lines\' may not be closed
			----------
			""",
		options
		);
}
// special stream from Files.m is properly handled by t-w-r
public void testStream4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.util.stream.Stream;
				import java.nio.file.*;
				class A {
				  void test(Path dir) throws java.io.IOException {
				    try (Stream<Path> list = Files.list(dir)) {
				    	list.forEach(child -> System.out.println(child));
				    }
				  }
				}"""
		},
		options
		);
}
public void testBug415790_ex2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return; // uses foreach
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"    public void example2() throws IOException {\n" +
			"        for (final File file : new File[] { new File(\"/\") }) {\n" +
			"            BufferedReader reader = null;\n" +
			"            try {\n" +
			"                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));\n" +
			"            }\n" +
			"            finally {\n" +
			"                try {\n" +
			"                    reader.close();\n" +
			"                }\n" +
			"                catch (IOException e) {\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"" +
			"}\n"
		},
		options);
}
public void testBug415790_ex4() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"    public void example2(File[] files) throws IOException {\n" +
			"        for (int i = 0; i < files.length; i++) {\n" +
			"            File file = files[i];\n" +
			"            BufferedReader reader = null;\n" +
			"            try {\n" +
			"                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));\n" +
			"            }\n" +
			"            finally {\n" +
			"                try {\n" +
			"                    reader.close();\n" +
			"                }\n" +
			"                catch (IOException e) {\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"" +
			"}\n"
		},
		options);
}
public void testBug371614_comment0() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"C.java",
			"""
				import java.io.FileInputStream;
				import java.io.IOException;
				import java.io.InputStream;
				
				public class C {
					public static void main(String[] args) {
						FileInputStream fileInputStream= null;
						try {
							fileInputStream = new FileInputStream(args[0]);
							while (true) {
								if (fileInputStream.read() == -1) {
									System.out.println("done");
				// Resource leak: 'fileInputStream' is not closed at this location
									return;
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
							return;
						} finally {
							closeStream(fileInputStream);
						}
					}
				\t
					private static void closeStream(InputStream stream) {
						if (stream != null) {
							try {
								stream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				
				"""
		},
		"----------\n" +
		"1. ERROR in C.java (at line 14)\n" +
		"	return;\n" +
		"	^^^^^^^\n" +
		potentialLeakOrCloseNotShownAtExit("fileInputStream") +
		"----------\n",
		options);
}
public void testBug371614_comment2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"ResourceLeak.java",
			"""
				import java.io.FileInputStream;
				import java.io.IOException;
				import java.io.InputStreamReader;
				import java.io.Reader;
				
				public class ResourceLeak {
				
				  boolean check(final Reader r) throws IOException {
				    final int i = r.read();
				    return (i != -1);
				  }
				
				  public void test1() throws IOException {
				    try (Reader r = new InputStreamReader(System.in);) {
				      while (check(r)) {
				        if (check(r))
				          throw new IOException("fail");
				        if (!check(r))
				          throw new IOException("fail");
				      }
				    }
				  }
				
				  public void test2() throws IOException {
				    try (Reader r = new InputStreamReader(new FileInputStream("test.txt"));) {
				      while (check(r)) {
				        if (check(r))
				          throw new IOException("fail");
				        if (!check(r))
				          throw new IOException("fail");
				      }
				    }
				  }
				}
				"""
		},
		options);
}
public void testBug371614_comment8() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				import java.net.*;
				public class X {
					Socket fSocket;
					void test() {
				    try (InputStreamReader socketIn = new InputStreamReader(fSocket.getInputStream())) {
				         while (true) {
				             if (socketIn.read(new char[1024]) < 0)
				                 throw new IOException("Error");
				         }          \s
				     } catch (IOException e) {
				     }\
					}
				}
				"""
		},
		options);
}
public void testBug462371_orig() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				interface IFile {
					InputStream getContents();
					boolean exists();
				}
				public class X {
					public static String getAnnotatedSignature(String typeName, IFile file, String selector, String originalSignature) {
						if (file.exists()) {
							try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()))) {
								reader.readLine();
								while (true) {
									String line = reader.readLine();\s
									// selector:
									if (selector.equals(line)) {
										// original signature:
										line = reader.readLine();
										if (originalSignature.equals("")) {
											// annotated signature:
											return reader.readLine();
										}
									}
									if (line == null)
										break;
								}
							} catch (IOException e) {
								return null;
							}
						}
						return null;
					}
				}
				"""
		},
		options);
}
public void _testBug462371_shouldWarn() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				interface IFile {
					InputStream getContents();
					boolean exists();
				}
				public class X {
					public static String getAnnotatedSignature(String typeName, IFile file, String selector, String originalSignature) {
						if (file.exists()) {
							try  {
								BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));\s
								reader.readLine();
								while (true) {
									String line = reader.readLine();\s
									// selector:
									if (selector.equals(line)) {
										// original signature:
										line = reader.readLine();
										if (originalSignature.equals("")) {
											// annotated signature:
											return reader.readLine();
										}
									}
									if (line == null)
										break;
								}
							} catch (IOException e) {
								return null;
							}
						}
						return null;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in C.java (at line 14)
				return;
				^^^^^^^
			Potential resource leak: \'fileInputStream\' may not be closed at this location
			----------
			""",
		options);
}
public void testBug421035() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.io.BufferedReader;
				import java.io.FileNotFoundException;
				import java.io.FileReader;
				import java.io.IOException;
				import java.io.Reader;
				
				public class Test {
				  void test() throws FileNotFoundException {
				    Reader a = (Reader)new BufferedReader(new FileReader("a"));
				    try {
						a.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				  }
				}
				"""
		},
		options);
}
public void testBug444964() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug444964.java",
			"""
				import java.io.*;
				
				public class Bug444964 {
				  void wrong() {
				    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				      for (;;) {
				        return;
				      }
				    } catch (Exception e) {
				    }
				  }
				  void right() {
				    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				      while (true) {
				        return;
				      }
				    } catch (Exception e) {
				    }
				  }
				
				}
				"""
		},
		options);
}
public void testBug397204() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"HostIdTest.java",
			"""
				import java.io.*;
				import java.net.InetAddress;
				import java.net.NetworkInterface;
				import java.util.Enumeration;
				import java.util.Formatter;
				import java.util.Locale;
				
				
				public class HostIdTest {
				
				    public final void primaryNetworkInterface() throws IOException {
				        System.out.println(InetAddress.getLocalHost());
				        System.out.println(InetAddress.getLocalHost().getHostName());
				        System.out.println(hostId());
				    }
				
				    String hostId() throws IOException {
				        try (StringWriter s = new StringWriter(); PrintWriter p = new PrintWriter(s)) {
				            p.print(InetAddress.getLocalHost().getHostName());
				            p.print('/');
				            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
				            while (e.hasMoreElements()) {
				                NetworkInterface i = e.nextElement();
				                System.out.println(i);
				                if (i.getHardwareAddress() == null || i.getHardwareAddress().length == 0)
				                    continue;
				                for (byte b : i.getHardwareAddress())
				                    p.printf("%02x", b);
				                return s.toString();
				            }
				            throw new RuntimeException("Unable to determine Host ID");
				        }
				    }
				
				    public void otherHostId() throws Exception {
				        InetAddress addr = InetAddress.getLocalHost();
				        byte[] ipaddr = addr.getAddress();
				        if (ipaddr.length == 4) {
				            int hostid = ipaddr[1] << 24 | ipaddr[0] << 16 | ipaddr[3] << 8 | ipaddr[2];
				            StringBuilder sb = new StringBuilder();
				            try (Formatter formatter = new Formatter(sb, Locale.US)) {
				                formatter.format("%08x", hostid);
				                System.out.println(sb.toString());
				            }
				        } else {
				            throw new Exception("hostid for IPv6 addresses not implemented yet");
				        }
				    }
				   \s
				}
				"""
		},
		options);
}
public void testBug397204_comment4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"HostIdTest.java",
			"""
				import java.io.*;
				
				public class HostIdTest {
				
				  void simple() throws Exception {
				    try (InputStream x = new ByteArrayInputStream(null)) {
				      while (Math.abs(1) == 1)
				        if (Math.abs(1) == 1)
				            return;
				    }
				  }
				}
				"""
		},
		options);
}
public void testBug433510() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug433510.java",
			"""
				import java.io.*;
				
				public class Bug433510 {
				
					void test() throws Exception {
						try (Reader r = new StringReader("Hello World!")) {
							int c;
							while ((c = r.read()) != -1) {
								if (c == ' ')
									throw new IOException("Unexpected space");
							}
						}
					}
				}
				"""
		},
		options);
}
public void testBug440282() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"ResourceLeakFalseNegative.java",
			"""
				import java.io.FileInputStream;
				import java.io.IOException;
				import java.io.InputStreamReader;
				
				public final class ResourceLeakFalseNegative {
				
				  private static final class Foo implements AutoCloseable {
				    final InputStreamReader reader;
				
				    Foo(final InputStreamReader reader) {
				      this.reader = reader;
				    }
				   \s
				    public int read() throws IOException {
				      return reader.read();
				    }
				
				    public void close() throws IOException {
				      reader.close();
				    }
				  }
				
				  private static final class Bar {
				    final int read;
				
				    Bar(final InputStreamReader reader) throws IOException {
				      read = reader.read();
				    }
				   \s
				    public int read() {
				      return read;
				    }
				  }
				
				  public final static int foo() throws IOException {
				    final FileInputStream in = new FileInputStream("/dev/null");
				    final InputStreamReader reader = new InputStreamReader(in);
				    try {
				      return new Foo(reader).read();
				    } finally {
				      // even though Foo is not closed, no potential resource leak is reported.
				    }
				  }
				
				  public final static int bar() throws IOException {
				    final FileInputStream in = new FileInputStream("/dev/null");
				    final InputStreamReader reader = new InputStreamReader(in);
				    try {
				      final Bar bar = new Bar(reader);
				      return bar.read();
				    } finally {
				      // Removing the close correctly reports potential resource leak as a warning,
				      // because Bar does not implement AutoCloseable.
				      reader.close();
				    }
				  }
				
				  public static void main(String[] args) throws IOException {
				    for (;;) {
				      foo();
				      bar();
				    }
				  }
				}
				"""
		},
		getTestBug440282_log(),
		options);
}
protected String getTestBug440282_log() {
	return
		"""
		----------
		1. ERROR in ResourceLeakFalseNegative.java (at line 39)
			return new Foo(reader).read();
			^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Potential resource leak: 'reader' may not be closed at this location
		----------
		2. ERROR in ResourceLeakFalseNegative.java (at line 39)
			return new Foo(reader).read();
			       ^^^^^^^^^^^^^^^
		Resource leak: \'<unassigned Closeable value>\' is never closed
		----------
		""";
}
public void testBug390064() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // generics used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"Redundant.java",
			"""
				public class Redundant
				{
				   private static class A<T> implements AutoCloseable
				   {
				      public void close()
				      {
				      }
				   }
				
				   private static class B extends A<Object>
				   {
				     \s
				   }
				  \s
				   private static class C implements AutoCloseable
				   {
				      public void close()
				      {
				      }
				   }
				  \s
				   private static class D extends C
				   {
				     \s
				   }
				  \s
				   public static void main(String[] args)
				   {
				      new B();
				     \s
				      new D();
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in Redundant.java (at line 29)
				new B();
				^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			2. ERROR in Redundant.java (at line 31)
				new D();
				^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			""",
		options);
}
public void testBug396575() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"Bug396575.java",
			"""
				import java.io.*;
				
				public class Bug396575 {
				  void test1(File myFile) {
				   OutputStream out = null;
				   BufferedWriter bw = null;
				   try {
				       // code...
				       out = new FileOutputStream(myFile);
				       OutputStreamWriter writer = new OutputStreamWriter(out);
				       bw = new BufferedWriter(writer);
				       // more code...
				   } catch (Exception e) {
				       try {
				           bw.close(); // WARN: potential null pointer access
				       } catch (Exception ignored) {}
				       return;  // WARN: resource leak - bw may not be closed
				   }
				  }
				 \s
				  void test2(File myFile) {
				       BufferedWriter bw = null;
				   try {
				       // code...
				                                                       // declare "out" here inside try-catch as a temp variable
				       OutputStream out = new FileOutputStream(myFile); // WARN: out is never closed.
				       OutputStreamWriter writer = new OutputStreamWriter(out);
				       bw = new BufferedWriter(writer);
				       // more code...
				   } catch (Exception e) {
				       try {
				           bw.close(); // WARN: potential null pointer access
				       } catch (Exception ignored) {}
				       return;  // WARN: resource leak - bw may not be closed
				   }
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in Bug396575.java (at line 11)
				bw = new BufferedWriter(writer);
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'bw\' is never closed
			----------
			2. ERROR in Bug396575.java (at line 28)
				bw = new BufferedWriter(writer);
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'bw\' is never closed
			----------
			""",
		options);
}
public void testBug473317() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // using diamond
	Map<String, String> compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_SYNTHETIC_ACCESS_EMULATION, JavaCore.IGNORE);
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"AutoCloseableEnhancedForTest.java",
			"""
				import java.util.Iterator;
				
				public class AutoCloseableEnhancedForTest
				{
				   private static class MyIterator<T> implements Iterator<T>
				   {
				      private T value;
				     \s
				      public MyIterator(T value)
				      {
				         this.value = value;
				      }
				     \s
				      @Override
				      public boolean hasNext()
				      {
				         return false;
				      }
				
				      @Override
				      public T next()
				      {
				         return value;
				      }
				   }
				  \s
				   private static class MyIterable<T> implements Iterable<T>, AutoCloseable
				   {
				      @Override
				      public Iterator<T> iterator()
				      {
				         return new MyIterator<>(null);
				      }
				     \s
				      @Override
				      public void close() throws Exception
				      {
				      }
				   }
				  \s
				   public static void main(String[] args)
				   {
				      // Not flagged as "never closed."
				      for (Object value : new MyIterable<>())
				      {
				         System.out.println(String.valueOf(value));
				        \s
				         break;
				      }
				     \s
				      // Flagged as "never closed."
				      MyIterable<Object> iterable = new MyIterable<>();
				     \s
				      for (Object value : iterable)
				      {
				         System.out.println(String.valueOf(value));
				        \s
				         break;
				      }
				   }
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in AutoCloseableEnhancedForTest.java (at line 44)
				for (Object value : new MyIterable<>())
				                    ^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			2. WARNING in AutoCloseableEnhancedForTest.java (at line 52)
				MyIterable<Object> iterable = new MyIterable<>();
				                   ^^^^^^^^
			Resource leak: \'iterable\' is never closed
			----------
			""";
	runner.customOptions = compilerOptions;
	runner.runWarningTest(); // javac warns about exception thrown from close() method
}
public void testBug541705() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses diamond
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runner.testFiles = new String[] {
		"Test.java",
		"""
			import java.util.*;
			import java.util.zip.*;
			import java.io.*;
			public class Test {
				private static HashMap<String, ZipFile> fgZipFileCache = new HashMap<>(5);
				public static void closeArchives() {
					synchronized (fgZipFileCache) {
						for (ZipFile file : fgZipFileCache.values()) {
							synchronized (file) {
								try {
									file.close();
								} catch (IOException e) {
									System.out.println(e);
								}
							}
						}
						fgZipFileCache.clear();
					}
				}
			}
			"""
	};
	runner.runConformTest();
}
public void testBug541705b() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return; // variable used in t-w-r
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runner.testFiles = new String[] {
		"Test.java",
		"""
			import java.util.*;
			import java.util.zip.*;
			import java.io.*;
			public class Test {
				private static HashMap<String, ZipFile> fgZipFileCache = new HashMap<>(5);
				public static void closeArchives() {
					synchronized (fgZipFileCache) {
						for (ZipFile file : fgZipFileCache.values()) {
							synchronized (file) {
								try (file) {
								} catch (IOException e) {
									System.out.println(e);
								}
							}
						}
						fgZipFileCache.clear();
					}
				}
			}
			"""
	};
	runner.runConformTest();
}
public void testBug542707_001() {
	if (!checkPreviewAllowed()) return; // uses switch expression
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				public class X implements Closeable{
					public static int foo(int i) throws IOException {
						int k = 0;
						X x = null;
						try {
							x = new X();
							x  = switch (i) {\s
							  case 1  ->   {
								 yield x;
							  }
							  default -> x;
							};
						} finally {
							x.close();
						}
						return k ;
					}
				
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// do nothing
						}
					}
					@Override
					public void close() throws IOException {
						Zork();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 31)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			""",
		options);
}
public void testBug542707_002() {
	if (this.complianceLevel < ClassFileConstants.JDK15) return; // uses switch expression
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				public class X implements Closeable{
					public static int foo(int i) throws IOException {
						int k = 0;
						X x = null;
						try {
							x = new X();
							x  = switch (i) {\s
							  case 1  ->   {
								 x = new X();
								 yield x;
							  }
							  default -> x;
							};
						} finally {
							x.close();
						}
						return k ;
					}
				
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// do nothing
						}
					}
					@Override
					public void close() throws IOException {
						Zork();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				x  = switch (i) {\s
						  case 1  ->   {
							 x = new X();
							 yield x;
						  }
						  default -> x;
						};
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'x\' is not closed at this location
			----------
			2. ERROR in X.java (at line 12)
				x = new X();
				^^^^^^^^^^^
			Resource leak: \'x\' is not closed at this location
			----------
			3. ERROR in X.java (at line 32)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			""",
		options);
}
public void testBug542707_003() {
	if (!checkPreviewAllowed()) return; // uses switch expression
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				public class X implements Closeable{
					public static int foo(int i) throws IOException {
						int k = 0;
						X x = null;
						try {
							x = new X();
							x  = switch (i) {\s
							  case 1  ->   {
								 yield new X();
							  }
							  default -> x;
							};
						} finally {
							x.close();
						}
						return k ;
					}
				
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// do nothing
						}
					}
					@Override
					public void close() throws IOException {
						Zork();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				x  = switch (i) {\s
						  case 1  ->   {
							 yield new X();
						  }
						  default -> x;
						};
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'x\' is not closed at this location
			----------
			2. ERROR in X.java (at line 31)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			""",
		options);
}
public void testBug486506() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses switch expression
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"LogMessage.java",
			"""
				import java.util.stream.*;
				import java.io.*;
				import java.nio.file.*;
				import java.nio.charset.*;
				class LogMessage {
				  LogMessage(Path path, String message) {}
				  public static Stream<LogMessage> streamSingleLineLogMessages(Path path) {
				    try {
				        Stream<String> lineStream = Files.lines(path, StandardCharsets.ISO_8859_1);
				        Stream<LogMessage> logMessageStream =
				                lineStream.map(message -> new LogMessage(path, message));
				        logMessageStream.onClose(lineStream::close);
				        return logMessageStream;
				    } catch (IOException e) {
				        throw new RuntimeException(e);
				    }
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in LogMessage.java (at line 13)
				return logMessageStream;
				^^^^^^^^^^^^^^^^^^^^^^^^
			Potential resource leak: \'lineStream\' may not be closed at this location
			----------
			""",
		options);
}
public void testBug463320() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"Try17.java",
			"""
				import java.util.zip.*;
				import java.io.*;
				public class Try17 {
				    void potential() throws IOException {
				        String name= getZipFile().getName();
				        System.out.println(name);
				    }
				    void definite() throws IOException {
				        String name= new ZipFile("bla.jar").getName();
				        System.out.println(name);
				    }
					 void withLocal() throws IOException {
						 ZipFile zipFile = getZipFile();
				        String name= zipFile.getName();
				        System.out.println(name);
					 }
				
				    ZipFile getZipFile() throws IOException {
				        return new ZipFile("bla.jar");
				    }
				}"""
		},
		"----------\n" +
		"1. ERROR in Try17.java (at line 5)\n" +
		"	String name= getZipFile().getName();\n" +
		"	             ^^^^^^^^^^^^\n" +
		potentialOrDefiniteLeak("<unassigned Closeable value>") +
		"----------\n" +
		"2. ERROR in Try17.java (at line 9)\n" +
		"	String name= new ZipFile(\"bla.jar\").getName();\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"2. ERROR in Try17.java (at line 13)\n" +
		"	ZipFile zipFile = getZipFile();\n" +
		"	        ^^^^^^^\n" +
		potentialOrDefiniteLeak("zipFile") +
		"----------\n",
		options);
}
public void testBug463320_comment8() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // required version of java.nio.file.*
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"Try17.java",
			"""
				import java.io.*;
				import java.nio.file.*;
				import java.net.*;
				public class Try17 {
				   public InputStream openInputStream(URI uri) {
						try {
							System.out.println(FileSystems.getFileSystem(uri));
							return Files.newInputStream(Paths.get(uri));
						} catch (FileSystemNotFoundException e) {
							throw new IllegalArgumentException(e);
						} catch (IOException e) {
							throw new IllegalStateException(e);
						}
					}
					public InputStream delegateGet(URI uri) {
						return openInputStream(uri);
					}
				}"""
		},
		"----------\n" +
		"1. ERROR in Try17.java (at line 7)\n" +
		"	System.out.println(FileSystems.getFileSystem(uri));\n" +
		"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n",
		options);
}
public void testBug558574() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses varargs signatures

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);

	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
					void m1() throws FileNotFoundException {
						PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("/tmp/out")));
						pw.printf("%d", 42).close();
					}
					void m2(PrintWriter pw) throws FileNotFoundException {
						pw.printf("%d", 42).append("end").close();
					}
					void m3() throws FileNotFoundException {
						new PrintWriter(new OutputStreamWriter(new FileOutputStream("/tmp/out")))
							.format("%d", 42)
							.append("end")
							.close();
					}
					void m4(PrintWriter pw) throws FileNotFoundException {
						pw.printf("%d", 42).append("end");
					}
				}
				"""
		},
		"",
		options);
}
public void testBug560460() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses try-with-resources
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X {
					Scanner m(String source) {
						return new Scanner(source).useDelimiter("foobar");
					}
				}
				"""
		},
		options);
}
public void testBug463320_comment19() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);

	runConformTest(
		new String[] {
			"Try17.java",
			"""
				import java.util.zip.*;
				import java.io.*;
				public class Try17 {
					void withLocal() throws IOException {
						ZipFile zipFile = null;
						if (zipFile != null)\
							zipFile = getZipFile();
						String name= zipFile.getName();
						System.out.println(name);
					 }
				
				    ZipFile getZipFile() throws IOException {
				        return new ZipFile("bla.jar");
				    }
				}"""
		},
		options);
}
public void testBug552521() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses try-with-resources

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakTest(
		new String[] {
			"EclipseBug552521getChannel.java",
			"""
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.FileOutputStream;
				import java.nio.channels.FileChannel;
				
				public class EclipseBug552521getChannel {
				
					@SuppressWarnings("unused")
					public void copyFile(final File srcFile, final File dstFile) throws Exception {
						/*
						 * TODO Eclipse Setting: Window/Preferences/Java/Compiler/Errors-Warnings/
						 * Resource not managed via try-with-resource = Ignore (default)
						 */
				        try (
				        		final FileInputStream srcStream  = new FileInputStream (srcFile);
				        		final FileChannel     srcChannel =                      srcStream.getChannel();
								final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				        		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  TODO Warning ok
				        		)
				        {
				    		srcChannel.transferTo(0, srcChannel.size(), dstChannel);
				        }
				
				        if (srcFile.isFile()) { // "if" (resolved at runtime) -> Warning suppressed
				            try (
				            		final FileInputStream srcStream  = new FileInputStream (srcFile);
				            		final FileChannel     srcChannel =                      srcStream.getChannel();
				    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FIXME Warning missing!
				            		)
				            {
				        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);
				            }
				        } else { // "else" (resolved at runtime) -> Warning suppressed
				            try (
				            		final FileInputStream srcStream  = new FileInputStream (srcFile);
				            		final FileChannel     srcChannel =                      srcStream.getChannel();
				    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FIXME Warning missing!
				            		)
				            {
				        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);
				            }
				        }
				
				        if (true) { // Dummy "if" (= constant true) -> Warning
				            try (
				            		final FileInputStream srcStream  = new FileInputStream (srcFile);
				            		final FileChannel     srcChannel =                      srcStream.getChannel();
				    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  TODO Warning ok
				            		)
				            {
				        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);
				            }
				        } else { // Dummy "else" (= constant false) -> Warning suppressed
				            try (
				            		final FileInputStream srcStream  = new FileInputStream (srcFile);
				            		final FileChannel     srcChannel =                      srcStream.getChannel();
				    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FIXME Warning missing!
				            		)
				            {
				        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);
				            }
				        }
				
				        if (false) { // Dummy "if" (= constant false) -> Warning suppressed
				            try (
				            		final FileInputStream srcStream  = new FileInputStream (srcFile);
				            		final FileChannel     srcChannel =                      srcStream.getChannel();
				    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FIXME Warning missing!
				            		)
				            {
				        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);
				            }
				        } else { // Dummy "else" (= constant true) -> Warning
				            try (
				            		final FileInputStream srcStream  = new FileInputStream (srcFile);
				            		final FileChannel     srcChannel =                      srcStream.getChannel();
				    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  TODO Warning ok
				            		)
				            {
				        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);
				            }
				        }
				        /*
				         * Following test-case differs from all the above as follows:
				         * FileInputStream is unassigned, instead of FileOutputStream
				         */
				        try (
				        		final FileChannel      srcChannel = new FileInputStream (srcFile) .getChannel();
				        		//                                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  TODO Warning ok
				        		final FileOutputStream dstStream  = new FileOutputStream(srcFile);
								final FileChannel      dstChannel =                      dstStream.getChannel();
				        		)
				        {
				    		srcChannel.transferTo(0, srcChannel.size(), dstChannel);
				        }
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in EclipseBug552521getChannel.java (at line 17)
				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			2. ERROR in EclipseBug552521getChannel.java (at line 28)
				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			3. ERROR in EclipseBug552521getChannel.java (at line 38)
				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			4. ERROR in EclipseBug552521getChannel.java (at line 50)
				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			5. ERROR in EclipseBug552521getChannel.java (at line 60)
				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			6. ERROR in EclipseBug552521getChannel.java (at line 72)
				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			7. ERROR in EclipseBug552521getChannel.java (at line 82)
				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();
				                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			8. ERROR in EclipseBug552521getChannel.java (at line 94)
				final FileChannel      srcChannel = new FileInputStream (srcFile) .getChannel();
				                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: \'<unassigned Closeable value>\' is never closed
			----------
			""",
		options);
}
public void testBug552521_comment14() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				import java.util.*;
				public class X {
					List<String> process(InputStream is) throws IOException {
						is.close();
						return Collections.emptyList();
					}
					void test(String fileName) throws IOException {
						for (String string : process(new FileInputStream(fileName))) {
							System.out.println(string);
						}
					}
					void test2(String fileName) throws IOException {
						for (String string : process(new FileInputStream(fileName)))
							System.out.println(string);
					}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	for (String string : process(new FileInputStream(fileName))) {\n" +
		"	                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	for (String string : process(new FileInputStream(fileName)))\n" +
		"	                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n",
		options);
}
public void testBug552521_comment14b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
					boolean check(InputStream is) throws IOException {
						is.close();
						return true;
					}
					void test1(String fileName) throws IOException {
						while (check(new FileInputStream(fileName)))
							System.out.println("while");
					}
					void test2(String fileName) throws IOException {
						do {
							System.out.println("while");
						} while (check(new FileInputStream(fileName)));
					}
					void test3(String fileName) throws IOException {
						for (int i=0;check(new FileInputStream(fileName));i++)
							System.out.println(i);
					}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	while (check(new FileInputStream(fileName)))\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	} while (check(new FileInputStream(fileName)));\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n" +
		"3. ERROR in X.java (at line 17)\n" +
		"	for (int i=0;check(new FileInputStream(fileName));i++)\n" +
		"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n",
		options);
}
public void testBug519740() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses try-with-resources

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Snippet.java",
			"""
				class Snippet {
				  static void foo() throws Exception {
				    try (java.util.Scanner scanner = new java.util.Scanner(new java.io.FileInputStream("abc"))) {
				      while (scanner.hasNext())\s
				        if (scanner.hasNextInt())
				          throw new RuntimeException();  /* Potential resource leak: 'scanner' may not be closed at this location */
				    }
				  }
				}
				"""
		},
		options);
}
public void testBug552441() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses try-with-resources

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);

	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.io.BufferedOutputStream;
				import java.io.FileOutputStream;
				import java.io.IOException;
				import java.io.OutputStream;
				import java.util.concurrent.atomic.AtomicLong;
				
				public class Test {
				    public static class CountingBufferedOutputStream extends BufferedOutputStream {
				        private final AtomicLong bytesWritten;
				
				        public CountingBufferedOutputStream(OutputStream out, AtomicLong bytesWritten) throws IOException {
				            super(out);
				            this.bytesWritten = bytesWritten;
				        }
				
				        @Override
				        public void write(byte[] b) throws IOException {
				            super.write(b);
				            bytesWritten.addAndGet(b.length);
				        }
				
				        @Override
				        public void write(byte[] b, int off, int len) throws IOException {
				            super.write(b, off, len);
				            bytesWritten.addAndGet(len);
				        }
				
				        @Override
				        public synchronized void write(int b) throws IOException {
				            super.write(b);
				            bytesWritten.incrementAndGet();
				        }
				    }
				
				    public static void test(String[] args) throws IOException {
				        AtomicLong uncompressedBytesOut = new AtomicLong();
				        int val = 0;
				        try (CountingBufferedOutputStream out = new CountingBufferedOutputStream(
				                new FileOutputStream("outputfile"), uncompressedBytesOut)) {
				
				            for (int i = 0; i < 1; i++) {
				                if (val > 2) {
				                    throw new RuntimeException("X");
				                }
				            }
				            if (val > 2) {
				                throw new RuntimeException("Y");
				            }
				            throw new RuntimeException("Z");
				        }
				    }
				}
				"""
		},
		options);
}
public void testBug400523() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);

	runConformTest(
		new String[] {
			"LeakWarning.java",
			"""
				import java.sql.Connection;
				import java.sql.PreparedStatement;
				import java.sql.ResultSet;
				import java.sql.SQLException;
				
				public class LeakWarning {
					String value = null;
				\t
				    public void setValue(Connection conn)
					{       \s
				        PreparedStatement stmt = null;
				        ResultSet rs = null;
				        try {           \s
				            stmt = conn.prepareStatement("SELECT 'value'");  /* marked as potential resource leak */
				            rs = stmt.executeQuery();                        /* marked as potential resource leak */
				            if (rs.next()) value = rs.getString(1);
				        } catch(SQLException e) {
				        }
				        finally {
				        	if (null != rs)   try { rs.close();   } catch (SQLException e) {} finally { rs = null;   }
				        	if (null != stmt) try { stmt.close(); } catch (SQLException e) {} finally { stmt = null; }
				        }
				    }
				   \s
				    public void setValueReturn(Connection conn)
					{       \s
				        PreparedStatement stmt = null;
				        ResultSet rs = null;
				        try {           \s
				            stmt = conn.prepareStatement("SELECT 'value'");
				            rs = stmt.executeQuery();
				            if (rs.next()) value = rs.getString(1);
				        } catch(SQLException e) {
				        }
				        finally {
				        	if (null != rs)   try { rs.close();   } catch (SQLException e) {} finally { rs = null;   }
				        	if (null != stmt) try { stmt.close(); } catch (SQLException e) {} finally { stmt = null; }
				        }
				        return; /* no warning now */
				    }
				}
				"""
		},
		options);
}
public void testBug527761() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				class BAOSWrapper extends java.io.ByteArrayOutputStream {}
				public class X {
					public static void warningCauser() {
						BAOSWrapper baos = new BAOSWrapper();
						//WARNING HAS BEEN CAUSED
						baos.write(0);
					}
				}
				"""
		},
		options);
}
public void testBug527761_otherClose() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses generics
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	String xSource =
			"""
		public class X {
			public static void warningCauser() {
				BAOSWrapper<String> baos = new BAOSWrapper<String>();
				//WARNING HAS BEEN CAUSED
				baos.write(0);
			}
		}
		""";
	runConformTest(
		new String[] {
			"BAOSWrapper.java",
			"""
				class BAOSWrapper<T> extends java.io.ByteArrayOutputStream {
					public void close(java.util.List<?> l) {}
				}
				""",
			"X.java",
			xSource
		},
		options);
	// consume BAOSWrapper from .class:
	runConformTest(false,
			new String[] { "X.java", xSource },
			"", "", "", null);
}
public void testBug527761_neg() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				class BAOSWrapper extends java.io.ByteArrayOutputStream {
					public void close() {}
				}
				public class X {
					public static void warningCauser() {
						BAOSWrapper baos = new BAOSWrapper();
						//WARNING HAS BEEN CAUSED
						baos.write(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				BAOSWrapper baos = new BAOSWrapper();
				            ^^^^
			Resource leak: \'baos\' is never closed
			----------
			""",
		options);
}
// regression caused by Bug 527761
public void testBug558759() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses generics
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	String ySource =
		"""
		public class Y {
			class YInner extends X<I> {}
		}
		""";
	runConformTest(
		new String[] {
			"I.java",
			"""
				import java.io.Closeable;
				public interface I extends Closeable {
					interface Location {}
					void m(Location l);
				}
				""",
			"X0.java",
			"""
				public abstract class X0<T extends I> implements I {
					public void close() {}
				}
				""",
			"X.java",
			"""
				public class X<T extends I> extends X0<T> implements I {
					public void m(Location l) {}
				}
				""",
			"Y.java",
			ySource
		},
		options);
	runConformTest(false,
			new String[] { "Y.java", ySource },
			"", "", "", null);
}
public void testBug559119() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses @Override
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakWarningTest(
		new String[] {
			"Sequencer.java",
			"""
				interface Sequencer extends AutoCloseable {
					void close(); // no exception
				}
				""",
			"SequencerControl.java",
			"""
				public abstract class SequencerControl {
					public abstract Sequencer getSequencer();
					@Override
					public boolean equals(Object obj) {
						if (obj != null) {
							if (getClass().equals(obj.getClass())) {
								return ((SequencerControl)obj).getSequencer().equals(getSequencer());
							}
						}
						return false;
					}
				}
				"""
		},
		"----------\n" +
		"1. WARNING in SequencerControl.java (at line 7)\n" +
		"	return ((SequencerControl)obj).getSequencer().equals(getSequencer());\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		potentialOrDefiniteLeak("<unassigned Closeable value>") +
		"----------\n" +
		"2. WARNING in SequencerControl.java (at line 7)\n" +
		"	return ((SequencerControl)obj).getSequencer().equals(getSequencer());\n" +
		"	                                                     ^^^^^^^^^^^^^^\n" +
		potentialLeakOrCloseNotShown("<unassigned Closeable value>") +
		"----------\n",
		options);
}
public void testBug560610() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses enum
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.util.EnumSet;
				public abstract class A<T> extends B<T> implements C<D> {
					void m(EnumSet<EN> en) {}
				}
				""",
			"B.java",
			"public abstract class B<U> implements AutoCloseable {}\n", // this causes A to be seen as a resource requiring closer inspection
			"C.java",
			"public interface C<T> {}\n", // just so we can read D as a type argument during hierarchy connecting for A
			"D.java",
			"public abstract class D extends A<String> {}\n", // extends A causes searching A for a close method, A seen as a PTB
			"EN.java",
			"""
				public enum EN {
					One, Two;
				}
				"""
		},
		"",
		options);
}
public void testBug560671() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses t-w-r
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Scanner;
				public class X {
					void m(String source) {
						try (Scanner s = new Scanner(source).useDelimiter("foobar")) {
							System.out.println(s.next());
						}
					}
				}
				"""
		},
		options);
}
public void testBug560671b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Scanner;
				public class X {
					void m(String source) throws java.io.IOException {
						Scanner s = null;\
						try {
							s = new Scanner(source).useDelimiter("foobar");
							System.out.println(s.next());
						} finally {
							if (s != null) s.close();
						}
					}
				}
				"""
		},
		options);
}
public void testBug561259() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
					  protected String m(String charset) throws IOException
					  {
						InputStream contents = new FileInputStream("/tmp/f");
					    BufferedReader reader = new BufferedReader(new InputStreamReader(contents, charset));
					    CharArrayWriter writer = new CharArrayWriter();
					    int c;
					    while ((c = reader.read()) != -1)
					    {
					      writer.write(c);
					    }
					    contents.close();
					    return writer.toString();
					  }
				}
				"""
		},
		options);
}
public void testBug560076() {
	runNegativeTest(
		new String[] {
			"org/sqlite/database/sqlite/SQLiteOpenHelper.java",
			"""
				package org.sqlite.database.sqlite;
				
				public abstract class SQLiteOpenHelper {
				    private void getDatabaseLocked(String name, SQLiteDatabase mDatabase) {
				        SQLiteDatabase sQLiteDatabase4 = mDatabase;
				        try {
				            sQLiteDatabase4 = name == null ? null : openDatabase();
				        } catch (Throwable e) {
				            sQLiteDatabase4 = openDatabase();
				        }
				    }
				
				    public static SQLiteDatabase openDatabase() {
				    }
				}
				
				final class SQLiteDatabase implements java.io.Closeable {
				}
				"""
		},
		"""
			----------
			1. WARNING in org\\sqlite\\database\\sqlite\\SQLiteOpenHelper.java (at line 4)
				private void getDatabaseLocked(String name, SQLiteDatabase mDatabase) {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method getDatabaseLocked(String, SQLiteDatabase) from the type SQLiteOpenHelper is never used locally
			----------
			2. ERROR in org\\sqlite\\database\\sqlite\\SQLiteOpenHelper.java (at line 13)
				public static SQLiteDatabase openDatabase() {
				                             ^^^^^^^^^^^^^^
			This method must return a result of type SQLiteDatabase
			----------
			3. ERROR in org\\sqlite\\database\\sqlite\\SQLiteOpenHelper.java (at line 17)
				final class SQLiteDatabase implements java.io.Closeable {
				            ^^^^^^^^^^^^^^
			The type SQLiteDatabase must implement the inherited abstract method Closeable.close()
			----------
			""");
}
public void testBug499037_001_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				class Y implements Closeable {
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}
				public class X {
				
				        public void foo() throws IOException {
				             final Y y1 = new Y();
				             try (y1) {\s
				            	 //
				             }
				        }\s
				        public static void main(String[] args) {
							System.out.println("Done");
						}
				}\s
				"""
		},
		"",
		options);
}
public void testBug499037_002_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				class Y implements Closeable {
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}
				public class X {
				
				        public void foo() throws IOException {
				             Y y1 = new Y();
				             try (y1; final Y y2 = new Y()) {\s
				            	 //
				             }
				        }\s
				        public static void main(String[] args) {
							System.out.println("Done");
						}
				}\s
				"""
		},
		"",
		options);
}
public void testBug499037_003_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				public class X {\s
				    public void foo() throws IOException {
				         Y y1 = new Y();
				         try(y1) {\s
				             return;
				         }
				    }\s
				} \s
				
				class Y implements Closeable {
						final int x = 10;
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}"""
		},
		"",
		options);
}
public void testBug499037_004_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				class Z {
					final Y yz = new Y();
				}
				public class X extends Z {
					final Y y2 = new Y();
				\t
					public void foo() {
						try (super.yz; y2)  {
							System.out.println("In Try");
						} catch (IOException e) {
						\t
						}finally {\s
						}
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
						System.out.println("Closed");
					}\s
				} \s
				"""
		},
		"",
		options);
}
public void testBug499037_005_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
					void test(boolean b) throws IOException {
						Y y = new Y();
						if (b) {
							try (y) {}
						}
					}
				}
				
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				Y y = new Y();
				  ^
			Potential resource leak: \'y\' may not be closed
			----------
			""",
		options);
}
// non-empty finally block - takes a different route
public void testBug499037_006_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
					void test(boolean b) throws IOException {
						Y y = new Y();
						if (b) {
							try (y;Y y2 = new Y();) {\s
							} finally {
							  System.out.println("hello");
							}
						}
					}
				}
				
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				Y y = new Y();
				  ^
			Potential resource leak: \'y\' may not be closed
			----------
			""",
		options);
}
public void testBug499037_007_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
					void test(boolean b) throws IOException {
						Y y = new Y();
						if (b) {
							try (y) {\s
							    // nothing\s
							}
						}
						else {
							y.close();
						}
					}
				}
				
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
					}
				}
				"""
		},
		"",
		options);
}
public void testBug499037_008_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
					void test(boolean b, Y yDash) throws IOException {
						Y y = new Y();
						if (b) {
							try (y; yDash) {\s
							    // nothing\s
							}
						}
						else {
						}
					}
				}
				
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				Y y = new Y();
				  ^
			Potential resource leak: \'y\' may not be closed
			----------
			""",
		options);
}
public void testBug499037_009_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
				private void foo(Y y) {}
					void test(boolean b) throws IOException {
						Y y = new Y();
						if (b) {
							try (y) {\s
							    // nothing\s
							}
						}
						else {
							foo(y);
						}
					}
				}
				
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				Y y = new Y();
				  ^
			Potential resource leak: \'y\' may not be closed
			----------
			""",
		options);
}
public void testBug499037_010_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
				private Y foo(Y y) {return y;}
					void test(boolean b) throws IOException {
						Y y = new Y();
						Y yy = foo(y);
						if (b) {
							try (y;yy) {\s
							    // do nothing\s
							}
						}
						else {
							// do nothing
						}
					}
				}
				
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				Y y = new Y();
				  ^
			Potential resource leak: \'y\' may not be closed
			----------
			2. ERROR in X.java (at line 7)
				Y yy = foo(y);
				  ^^
			Potential resource leak: \'yy\' may not be closed
			----------
			""",
		options);
}
public void testGH1762() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return; // uses t-w-r
	runLeakTest(
		new String[] {
			"X.java",
			"""
			import java.io.*;
			public class X {
				void m(String path, ClassLoader loader) throws IOException {
					try (InputStream input = loader == null ? new FileInputStream(path) : loader.getResourceAsStream(path)) {
						// read
					}
				}
			}
			"""
		},
		"",
		null);

}
public void testGH1867() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"""
			class RC implements AutoCloseable {
				void m() {}
				public void close() {}
			}
			public class X {
				RC get() { return null; }
				void test(int sw) {
					if (sw != -1) {
						switch(sw) {
						case 1:
							get().m();
							break;
						case 2:
							get().m();
							System.out.println();
							return;
						case 3:
							get().m();
							break;
						}
						System.out.println();
					}
				}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	get().m();\n" +
		"	^^^^^\n" +
		potentialOrDefiniteLeak("<unassigned Closeable value>") +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	get().m();\n" +
		"	^^^^^\n" +
		potentialOrDefiniteLeak("<unassigned Closeable value>") +
		"----------\n" +
		"3. ERROR in X.java (at line 18)\n" +
		"	get().m();\n" +
		"	^^^^^\n" +
		potentialOrDefiniteLeak("<unassigned Closeable value>") +
		"----------\n",
		options);
}
}
