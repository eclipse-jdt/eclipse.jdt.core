/*******************************************************************************
 * Copyright (c) 2017, 2018 GK Software AG, and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.BasicModule;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

import junit.framework.Test;

import static org.eclipse.jdt.core.tests.util.Util.createJar;

public class NullAnnotationTests9 extends AbstractNullAnnotationTest {

	public NullAnnotationTests9(String name) {
		super(name);
	}

	static {
//			TESTS_NAMES = new String[] { "testBug456497" };
//			TESTS_NUMBERS = new int[] { 001 };
//			TESTS_RANGE = new int[] { 1, 12 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class<?> testClass() {
		return NullAnnotationTests9.class;
	}

	@Deprecated // super method is deprecated
	@Override
	protected void setUpAnnotationLib() throws IOException {
		if (this.LIBS == null) {
			String[] defaultLibs = getDefaultClassPaths();
			int len = defaultLibs.length;
			this.LIBS = new String[len+1];
			System.arraycopy(defaultLibs, 0, this.LIBS, 0, len);
			this.LIBS[len] = createAnnotation_2_2_jar(Util.getOutputDirectory() + File.separator, null);
		}
	}

	public static String createAnnotation_2_2_jar(String dirName, String jcl9Path) throws IOException {
		// role our own annotation library as long as o.e.j.annotation is still at BREE 1.8:
		String jarFileName = dirName + "org.eclipse.jdt.annotation_2.2.0.jar";
		createJar(new String[] {
				"module-info.java",
				"""
					module org.eclipse.jdt.annotation {
						exports org.eclipse.jdt.annotation;
					}
					""",

				"org/eclipse/jdt/annotation/DefaultLocation.java",
				"""
					package org.eclipse.jdt.annotation;
					
					public enum DefaultLocation {
					\t
						PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS
					}
					""",

				"org/eclipse/jdt/annotation/NonNullByDefault.java",
				"""
					package org.eclipse.jdt.annotation;
					
					import java.lang.annotation.ElementType;
					import static org.eclipse.jdt.annotation.DefaultLocation.*;
					
					import java.lang.annotation.*;
					\s
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ ElementType.MODULE, ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE })
					public @interface NonNullByDefault {
						DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };
					}""",

				"org/eclipse/jdt/annotation/NonNull.java",
				"""
					package org.eclipse.jdt.annotation;
					import static java.lang.annotation.ElementType.TYPE_USE;
					
					import java.lang.annotation.*;
					\s
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ TYPE_USE })
					public @interface NonNull {
						// marker annotation with no members
					}
					""",

				"org/eclipse/jdt/annotation/Nullable.java",
				"""
					package org.eclipse.jdt.annotation;
					
					import static java.lang.annotation.ElementType.TYPE_USE;
					
					import java.lang.annotation.*;
					\s
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ TYPE_USE })
					public @interface Nullable {
						// marker annotation with no members
					}
					"""
			},
			null,
			jarFileName,
			jcl9Path != null ? new String[] { jcl9Path } : null,
			"9");
		return jarFileName;
	}

	// -------- internal infrastructure ------------

	Map<String,IModule> moduleMap = new HashMap<>(); // by name
	Map<String,String> file2module = new HashMap<>();

	@Override
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		INameEnvironment[] classLibs = getClassLibs(classPaths == null, options);
		for (INameEnvironment nameEnvironment : classLibs) {
			((FileSystem) nameEnvironment).scanForModules(createParser());
		}
		return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
	}

	// --- same as AbstractRegressionTest9, just in a different inheritance hierarchy:

	@Override
	protected CompilationUnit[] getCompilationUnits(String[] testFiles) {
		Map<String,char[]> moduleFiles= new HashMap<>(); // filename -> modulename

		// scan for all module-info.java:
		for (int i = 0; i < testFiles.length; i+=2) {
			IModule module = extractModuleDesc(testFiles[i], testFiles[i+1]);
			if (module != null) {
				this.moduleMap.put(String.valueOf(module.name()), module);
				moduleFiles.put(testFiles[0], module.name());
			}
		}
		// record module information in CUs:
		CompilationUnit[] compilationUnits = Util.compilationUnits(testFiles);
		for (int i = 0; i < compilationUnits.length; i++) {
			char[] fileName = compilationUnits[i].getFileName();
			String fileNameString = String.valueOf(compilationUnits[i].getFileName());
			if (CharOperation.endsWith(fileName, TypeConstants.MODULE_INFO_FILE_NAME)) {
				compilationUnits[i].module = moduleFiles.get(fileNameString.replace(File.separator, "/"));
			} else {
				String modName = this.file2module.get(fileNameString.replace(File.separator, "/"));
				if (modName != null) {
					compilationUnits[i].module = modName.toCharArray();
				}
			}
		}
		return compilationUnits;
	}

	IModule extractModuleDesc(String fileName, String fileContent) {
		if (fileName.toLowerCase().endsWith(IModule.MODULE_INFO_JAVA)) {
			Parser parser = createParser();

			ICompilationUnit cu = new CompilationUnit(fileContent.toCharArray(), fileName, null);
			CompilationResult compilationResult = new CompilationResult(cu, 0, 1, 10);
			CompilationUnitDeclaration unit = parser.parse(cu, compilationResult);
			if (unit.isModuleInfo() && unit.moduleDeclaration != null) {
				return new BasicModule(unit.moduleDeclaration, null);
			}
		}
		return null;
	}

	Parser createParser() {
		Map<String,String> opts = new HashMap<>();
		opts.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_9);
		return new Parser(
				new ProblemReporter(getErrorHandlingPolicy(), new CompilerOptions(opts), getProblemFactory()),
				false);
	}

	// ------------------------------------------------------

	/** Use in tests to associate the CU in file 'fileName' to the module of the given name. */
	void associateToModule(String moduleName, String... fileNames) {
		for (String fileName : fileNames)
			this.file2module.put(fileName, moduleName);
	}

	private Runner getDefaultRunner() {
		Runner runner = new Runner();
		runner.classLibraries = this.LIBS;
		runner.libsOnModulePath = true;
		runner.javacTestOptions =
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		return runner;
	}

	public void test_nnbd_in_module_01() {
		associateToModule("my.mod", "my.mod/p/X.java");
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"my.mod/module-info.java",
				  """
					import org.eclipse.jdt.annotation.*;
					@NonNullByDefault
					module my.mod {
							requires static org.eclipse.jdt.annotation;
					}
					""",
				"my.mod/p/X.java",
				  """
					package p;
					public class X {
							String f; // missing nn init
					    	void foo(String s) {
					        this.f = s; // OK
					    	}
					}
					"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in my.mod\\p\\X.java (at line 3)
					String f; // missing nn init
					       ^
				The @NonNull field f may not have been initialized
				----------
				""";
		runner.runNegativeTest();
	}

	public void test_nnbd_in_module_02() throws IOException {

		String jarPath = OUTPUT_DIR+"/mod.one.jar";
		createJar(
			new String[] {
				"module-info.java",
				"""
					@org.eclipse.jdt.annotation.NonNullByDefault
					module mod.one {
						requires org.eclipse.jdt.annotation;
						exports p.q;
					}
					""",
				"p/q/API.java",
				"""
					package p.q;
					public class API {
						public String id(String in) { return in; }
					}
					"""
			},
			null, // extra path & content
			jarPath,
			this.LIBS,
			"9");

		associateToModule("my.mod", "my.mod/p/X.java");
		Runner runner = new Runner();
		runner.shouldFlushOutputDirectory = false;
		runner.classLibraries = Arrays.copyOf(this.LIBS, this.LIBS.length+1);
		runner.classLibraries[runner.classLibraries.length-1] = jarPath;
		runner.libsOnModulePath = true;
		runner.testFiles = new String[] {
				"my.mod/module-info.java",
				  """
					import org.eclipse.jdt.annotation.*;
					@NonNullByDefault
					module my.mod {
							requires static org.eclipse.jdt.annotation;
							requires mod.one;
					}
					""",
				"my.mod/p/X.java",
				  """
					package p;
					import p.q.API;
					public class X {
					    	void foo(API api) {
					        api.id(api.id("")); // OK
					        api.id(null); // NOK
					    	}
					}
					"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in my.mod\\p\\X.java (at line 6)
					api.id(null); // NOK
					       ^^^^
				Null type mismatch: required '@NonNull String' but the provided value is null
				----------
				""";
		runner.javacTestOptions =
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}

	public void test_redundant_nnbd_vs_module() {
		associateToModule("my.mod", "my.mod/p/X.java", "my.mod/p2/package-info.java");
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"my.mod/module-info.java",
				  """
					import org.eclipse.jdt.annotation.*;
					@NonNullByDefault
					module my.mod {
							requires static org.eclipse.jdt.annotation;
					}
					""",
				"my.mod/p/X.java",
				  """
					package p;
					@org.eclipse.jdt.annotation.NonNullByDefault
					public class X {
							String f; // missing nn init
					    	void foo(String s) {
					        this.f = s; // OK
					    	}
					}
					""",
				"my.mod/p/Y.java",
				  "package p;\n" +
				  "import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
				  "@org.eclipse.jdt.annotation.NonNullByDefault(PARAMETER)\n" + // not: FIELD, due to details not redundant
				  "public class Y {\n" +
				  "		String f; // missing init is NOT a problem\n" +
				  "    	void foo(String s) {\n" +
				  "        this.f = s; // OK\n" +
				  "    	}\n" +
				  "}\n",
				"my.mod/p2/package-info.java",
				  "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				  "package p2;\n"
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in my.mod\\p\\X.java (at line 2)
					@org.eclipse.jdt.annotation.NonNullByDefault
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Nullness default is redundant with a default specified for the enclosing module my.mod
				----------
				2. ERROR in my.mod\\p\\X.java (at line 4)
					String f; // missing nn init
					       ^
				The @NonNull field f may not have been initialized
				----------
				----------
				1. WARNING in my.mod\\p2\\package-info.java (at line 1)
					@org.eclipse.jdt.annotation.NonNullByDefault
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Nullness default is redundant with a default specified for the enclosing module my.mod
				----------
				""";
		runner.runNegativeTest();
	}
	public void testBug536037a() {
		if (this.complianceLevel < ClassFileConstants.JDK10) return;
		runConformTestWithLibs(
			new String[] {
				"Bar.java",
				"""
					@org.eclipse.jdt.annotation.NonNullByDefault
					public class Bar {
					    static void bar(Iterable<String> list) {
					        for(var s : list);
					    }
					}
					"""
			},
			null,
			"");
		this.verifier.shutDown();
	}
	public void testBug536037b() {
		// tests combination of declaration null-annotations & 'var':
		if (this.complianceLevel < ClassFileConstants.JDK10) return;
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "test.NonNull");
		options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "test.Nullable");
		runNegativeTestWithLibs(
			new String[] {
				"test/NonNull.java",
				"""
					package test;
					import java.lang.annotation.*;
					@Target({ElementType.LOCAL_VARIABLE,ElementType.PARAMETER}) public @interface NonNull {}
					""",
				"test/Nullable.java",
				"""
					package test;
					import java.lang.annotation.*;
					@Target({ElementType.LOCAL_VARIABLE,ElementType.PARAMETER}) public @interface Nullable {}
					""",
				"Bar.java",
				"""
					import test.*;
					public class Bar {
					    static void bar1(@Nullable String s1, Iterable<String> list) {
							@NonNull var s2 = s1;
							for (@NonNull var s : list);
						 }
					    static void bar2(int[] array) {
							@NonNull var i1 = 3;
							for (@NonNull var s : array);
						 }
					}
					"""
			},
			options,
			"""
				----------
				1. ERROR in Bar.java (at line 4)
					@NonNull var s2 = s1;
					                  ^^
				Null type mismatch: required '@NonNull String' but the provided value is specified as @Nullable
				----------
				2. WARNING in Bar.java (at line 5)
					for (@NonNull var s : list);
					                      ^^^^
				Null type safety: The expression of type 'String' needs unchecked conversion to conform to '@NonNull String'
				----------
				3. ERROR in Bar.java (at line 8)
					@NonNull var i1 = 3;
					^^^^^^^^
				The nullness annotation @NonNull is not applicable for the primitive type int
				----------
				4. ERROR in Bar.java (at line 9)
					for (@NonNull var s : array);
					     ^^^^^^^^
				The nullness annotation @NonNull is not applicable for the primitive type int
				----------
				""");
	}

	public void testGH1152() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"bug/NonNullBug.java",
				"""
				package bug;
				import org.eclipse.jdt.annotation.NonNull;
				public class NonNullBug {
					interface Interface<@NonNull T> { }

					class Class<T> implements Interface<@NonNull T> {} // 4.28 Null constraint mismatch: The type 'T' is not a valid substitute for the type parameter '@NonNull T'
				}
				"""
			};
		runner.runConformTest();
	}
}
