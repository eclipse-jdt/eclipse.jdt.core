package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import javax.lang.model.SourceVersion;

import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class MultiReleaseJarTests extends AbstractBatchCompilerTest {

	static {
//		 TESTS_NAMES = new String[] { "test001" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	private boolean isJRE10 = false;
	public MultiReleaseJarTests(String name) {
		super(name);
		try {
			SourceVersion valueOf = SourceVersion.valueOf("RELEASE_10");
			if (valueOf != null) this.isJRE10 = true;
		} catch(Exception e) {

		}
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class<?> testClass() {
		return MultiReleaseJarTests.class;
	}
	public void test001() {
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		runNegativeTest(
			new String[] {
				"src/X.java",
				  """
					import a.b.c.MultiVersion1.Inner;
					import p.q.r.MultiVersion2.Inner;
					public class X {
					}
					"""},
			"\"" + OUTPUT_DIR +  File.separator + "src/X.java\"" +
			" -classpath " + path + " --release 8 ",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 1)
					import a.b.c.MultiVersion1.Inner;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				The import a.b.c.MultiVersion1.Inner cannot be resolved
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 2)
					import p.q.r.MultiVersion2.Inner;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				The import p.q.r.MultiVersion2.Inner cannot be resolved
				----------
				2 problems (2 errors)
				""",
			false
		   );
	}
	public void test002() {
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		runNegativeTest(
			new String[] {
				"src/X.java",
				  """
					import a.b.c.MultiVersion1.Inner;
					import p.q.r.MultiVersion2.Inner;
					public class X {
					}
					"""},
			"\"" + OUTPUT_DIR +  File.separator + "src/X.java\"" +
			" -classpath " + path + " --release 9 ",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 1)
					import a.b.c.MultiVersion1.Inner;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				The type a.b.c.MultiVersion1.Inner is not visible
				----------
				1 problem (1 error)
				""",
			false
		   );
	}
	public void test003() {
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		runConformTest(
			new String[] {
				"src/X.java",
				  """
					import p.q.r.MultiVersion3.Inner;
					public class X {
					  Inner i = null;
					  p.q.r.MultiVersion2.Inner i2 = null;
					}
					"""},
			"\"" + OUTPUT_DIR +  File.separator + "src/X.java\"" +
			" -classpath " + path + " --release 9 ",
			"",
			"",
			false
		   );
	}
	public void test004() {
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		runNegativeTest(
			new String[] {
				"src/X.java",
				  """
					import p.q.r.MultiVersion3.Inner;
					import p.q.r.MultiVersion2.Inner;
					public class X {
					  Inner i = null;
					}
					"""},
			"\"" + OUTPUT_DIR +  File.separator + "src/X.java\"" +
			" -classpath " + path + " --release 9 ",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 2)
					import p.q.r.MultiVersion2.Inner;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				The import p.q.r.MultiVersion2.Inner collides with another import statement
				----------
				1 problem (1 error)
				""",
			false
		   );
	}
	public void test005() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		File directory = new File(OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" );
		File out = new File(OUTPUT_DIR +  File.separator + "out" );
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + directory.toString());
				return;
			}
		}
		if (!out.exists()) {
			if (!out.mkdirs()) {
				System.out.println("Could not create " + directory.toString());
				return;
			}
		}
		runNegativeTest(
			new String[] {
				"src/MyModule/module-info.java",
				"""
					module MyModule {
					  requires Version9;
					}""",
				"src/MyModule/p/X.java",
				"""
					package p;
					public class X {
					  java.sql.Connection con = null;
					}
					"""},
			"  -d \"" + out.toString() + "\" " +
			" --module-source-path \"" + directory.toString() +  "\" " +
			" \"" + OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" + File.separator + "module-info.java\"" +
			" \"" + OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" + File.separator + "p" + File.separator + "X.java\" "  +
			" --module-path " + path + " --release 9 ",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/MyModule/p/X.java (at line 3)
					java.sql.Connection con = null;
					^^^^^^^^^^^^^^^^^^^
				The type java.sql.Connection is not accessible
				----------
				1 problem (1 error)
				""",
			false
		   );
	}
	public void test006() {
		if (!this.isJRE10) return;
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		File directory = new File(OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" );
		File out = new File(OUTPUT_DIR +  File.separator + "out" );
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + directory.toString());
				return;
			}
		}
		if (!out.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + directory.toString());
				return;
			}
		}
		runConformTest(
			new String[] {
				"src/MyModule/module-info.java",
				"""
					module MyModule {
					  requires Version10;
					}""",
				"src/MyModule/p/X.java",
				"""
					package p;
					public class X {
					  java.sql.Connection con = null;
					}
					"""},
			"  -d \"" + out.toString() + "\" " +
			" --module-source-path \"" + directory.toString() +  "\" " +
			" \"" + OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" + File.separator + "module-info.java\"" +
			" \"" + OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" + File.separator + "p" + File.separator + "X.java\" "  +
			" --module-path " + path + " --release 10 ",
			"",
			"",
			false
		   );
	}
}
