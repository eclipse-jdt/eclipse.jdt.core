/*******************************************************************************
 * Copyright (c) 2001, 2017 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *     						bug 185682 - Increment/decrement operators mark local variables as read
 *     						bug 328281 - visibility leaks not detected when analyzing unused field in private class
 *							Bug 410218 - Optional warning for arguments of "unexpected" types to Map#get(Object), Collection#remove(Object) et al.
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

/* Collects potential programming problems tests that are not segregated in a
 * dedicated test class (aka NullReferenceTest). */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ProgrammingProblemsTest extends AbstractRegressionTest {

public ProgrammingProblemsTest(String name) {
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test0055" };
//		TESTS_NUMBERS = new int[] { 56 };
//  	TESTS_RANGE = new int[] { 1, -1 };
  	}

public static Test suite() {
    return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
    return ProgrammingProblemsTest.class;
}
@Override
protected Map getCompilerOptions() {
	Map compilerOptions = super.getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal,  CompilerOptions.OPTIMIZE_OUT);
	return compilerOptions;
}
void runTest(
		String[] testFiles,
		String[] errorOptions,
		String[] warningOptions,
		String[] ignoreOptions,
		boolean expectingCompilerErrors,
		String expectedCompilerLog,
		String expectedOutputString,
		boolean forceExecution,
		String[] classLib,
		boolean shouldFlushOutputDirectory,
		String[] vmArguments,
		Map customOptions,
		ICompilerRequestor clientRequestor,
		boolean skipJavac) {
	Map compilerOptions = customOptions;
	if (errorOptions != null || warningOptions != null ||
			ignoreOptions != null) {
		if (compilerOptions == null) {
			compilerOptions = new HashMap();
		}
		if (errorOptions != null) {
		    for (int i = 0; i < errorOptions.length; i++) {
		    	compilerOptions.put(errorOptions[i], CompilerOptions.ERROR);
		    }
		}
		if (warningOptions != null) {
		    for (int i = 0; i < warningOptions.length; i++) {
		    	compilerOptions.put(warningOptions[i], CompilerOptions.WARNING);
		    }
		}
		if (ignoreOptions != null) {
		    for (int i = 0; i < ignoreOptions.length; i++) {
		    	compilerOptions.put(ignoreOptions[i], CompilerOptions.IGNORE);
		    }
		}
	}
	runTest(testFiles,
		expectingCompilerErrors,
		expectedCompilerLog,
		expectedOutputString,
		"" /* expectedErrorString */,
		forceExecution,
		classLib,
		shouldFlushOutputDirectory,
		vmArguments,
		compilerOptions,
		clientRequestor,
		skipJavac);
}

// default behavior upon unread parameters
public void test0001_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo(boolean b) {
				  }
				}
				"""
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// reporting unread paramaters as warning
public void test0002_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo(boolean b) {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 2)
				public void foo(boolean b) {
				                        ^
			The value of the parameter b is not used
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using the Javadoc
// @param disables by default
public void test0003_unread_parameters() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				/** @param b mute warning **/
				  public void foo(boolean b) {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using the Javadoc
// @param disabling can be disabled
public void test0004_unread_parameters() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				/** @param b mute warning **/
				  public void foo(boolean b) {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo(boolean b) {
				                        ^
			The value of the parameter b is not used
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using SuppressWarnings
public void test0005_unread_parameters() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runTest(
			new String[] {
				"X.java",
				"""
					public class X {
					@SuppressWarnings("unused")
					  public void foo(boolean b) {
					  }
					@SuppressWarnings("all")
					  public void foo(int i) {
					  }
					}
					"""
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedParameter
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}

// reporting unread paramaters as error
public void test0006_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo(boolean b) {
				  }
				}
				"""
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo(boolean b) {
				                        ^
			The value of the parameter b is not used
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// default behavior upon unnecessary declaration of thrown checked exceptions
public void test0007_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public void foo() throws IOException {
				  }
				}
				"""
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// reporting unnecessary declaration of thrown checked exceptions as warning
public void test0008_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public void foo() throws IOException {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo() throws IOException {
				                         ^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disables by default
public void test0009_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				/** @throws IOException mute warning **/
				  public void foo() throws IOException {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disabling can be disabled
public void test0010_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionIncludeDocCommentReference,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				/** @throws IOException mute warning **/
				  public void foo() throws IOException {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 4)
				public void foo() throws IOException {
				                         ^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using SuppressWarnings
public void test0011_declared_thrown_checked_exceptions() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runTest(
			new String[] {
				"X.java",
				"""
					import java.io.IOException;
					public class X {
					@SuppressWarnings("all")
					  public void foo() throws IOException {
					  }
					}
					"""
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}

// reporting unnecessary declaration of thrown checked exceptions as error
public void test0012_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public void foo() throws IOException {
				  }
				}
				"""
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo() throws IOException {
				                         ^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disables by default, but only exact matches work
public void test0013_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.io.EOFException;
				public class X {
				/** @throws EOFException does not mute warning for IOException **/
				  public void foo() throws IOException {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 5)
				public void foo() throws IOException {
				                         ^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// interaction between errors and warnings
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203721
public void test0014_declared_thrown_checked_exceptions_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  void foo(int unused) throws IOException {}
				}
				"""
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 3)
				void foo(int unused) throws IOException {}
				             ^^^^^^
			The value of the parameter unused is not used
			----------
			2. ERROR in X.java (at line 3)
				void foo(int unused) throws IOException {}
				                            ^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// interaction between errors and warnings
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203721
// variant: both warnings show up
public void test0015_declared_thrown_checked_exceptions_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  void foo(int unused) throws IOException {}
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException,
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 3)
				void foo(int unused) throws IOException {}
				             ^^^^^^
			The value of the parameter unused is not used
			----------
			2. WARNING in X.java (at line 3)
				void foo(int unused) throws IOException {}
				                            ^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// reporting unread paramaters as error on a constructor
public void test0016_unread_parameters_constructor() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public X(boolean b) {
				  }
				}
				"""
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"""
			----------
			1. ERROR in X.java (at line 2)
				public X(boolean b) {
				                 ^
			The value of the parameter b is not used
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=208001
public void test0017_shadowing_package_visible_methods() {
	runTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				public class X {
				  void foo() {
				  }
				}
				""",
			"q/Y.java",
			"""
				package q;
				public class Y extends p.X {
				  void foo() {
				  }
				}
				""",
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in q\\Y.java (at line 3)
				void foo() {
				     ^^^^^
			The method Y.foo() does not override the inherited method from X since it is private to a different package
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		new ICompilerRequestor() {
			public void acceptResult(CompilationResult result) {
				if (result.compilationUnit.getFileName()[0] == 'Y') {
					assertEquals("unexpected problems count", 1, result.problemCount);
					assertEquals("unexpected category", CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT, result.problems[0].getCategoryID());
				}
			}
		} /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of thrown unchecked exceptions
public void test0018_declared_thrown_unchecked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws ArithmeticException {
				  }
				}
				"""
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of thrown unchecked exceptions
public void test0019_declared_thrown_unchecked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws RuntimeException {
				  }
				}
				"""
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of Exception
public void test0020_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws Exception {
				  }
				}
				"""
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of Throwable
public void test0021_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws Throwable {
				  }
				}
				"""
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning
public void test0022_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws ArithmeticException {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// the external API uses another string literal - had it wrong in first attempt
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0023_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_EXEMPT_EXCEPTION_AND_THROWABLE,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws Exception {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 2)
				public void foo() throws Exception {
				                         ^^^^^^^^^
			The declared exception Exception is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning
public void test0024_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws RuntimeException {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// focused on Exception and Throwable, which are not unchecked but can catch
// unchecked exceptions
public void test0025_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws Exception {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 2)
				public void foo() throws Exception {
				                         ^^^^^^^^^
			The declared exception Exception is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// focused on Exception and Throwable, which are not unchecked but can catch
// unchecked exceptions
public void test0026_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws Throwable {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 2)
				public void foo() throws Throwable {
				                         ^^^^^^^^^
			The declared exception Throwable is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using the Javadoc
// @throws disables by default
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0027_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				/** @throws Exception mute warning **/
				  public void foo() throws Exception {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using the Javadoc
// @throws disabling can be disabled
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0028_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionIncludeDocCommentReference,
			CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				/** @throws Exception mute warning **/
				  public void foo() throws Exception {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo() throws Exception {
				                         ^^^^^^^^^
			The declared exception Exception is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using SuppressWarnings
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0029_declared_thrown_checked_exceptions() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map customOptions = new HashMap();
		customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
				CompilerOptions.DISABLED);
		runTest(
			new String[] {
				"X.java",
				"""
					public class X {
					@SuppressWarnings("all")
					  public void foo() throws Exception {
					  }
					}
					"""
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			customOptions,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as error
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the error for unchecked exceptions, using Exception instead
public void test0030_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws Exception {
				  }
				}
				"""
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo() throws Exception {
				                         ^^^^^^^^^
			The declared exception Exception is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using the Javadoc
// @throws disables by default, but only exact matches work
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0031_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				/** @throws Throwable does not mute warning for Exception **/
				  public void foo() throws Exception {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo() throws Exception {
				                         ^^^^^^^^^
			The declared exception Exception is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions
public void test0032_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws Error {
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0033_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() throws Exception {
				    if (bar()) {
				      throw new Exception();
				    }
				  }
				  boolean bar() {
				    return true;
				  }
				}
				"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216897
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
public void test0034_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static final class MyError extends Error {
				    private static final long serialVersionUID = 1L;
				  }
				  public void foo() throws Throwable {
				    try {
				      bar();
				    } catch (MyError e) {
				    }
				  }
				  private void bar() {}
				}"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 5)
				public void foo() throws Throwable {
				                         ^^^^^^^^^
			The declared exception Throwable is not actually thrown by the method foo() from type X
			----------
			""",
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
public void test0035_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static final class MyError extends Error {
				    private static final long serialVersionUID = 1L;
				  }
				  public void foo() throws Throwable {
				    throw new MyError();
				  }
				}"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
public void test0036_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static class E1 extends Exception {
				    private static final long serialVersionUID = 1L;
				  }
				  public static class E2 extends E1 {
				    private static final long serialVersionUID = 1L;
				  }
				  public void foo() throws E1 {
				    throw new E2();
				  }
				}"""
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=115814
public void test0037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						boolean b1 = args == args;
						boolean b2 = args != args;
						boolean b3 = b1 == b1;
						boolean b4 = b1 != b1;
						boolean b5 = b1 && b1;
						boolean b6 = b1 || b1;
					\t
						boolean b7 = foo() == foo();
						boolean b8 = foo() != foo();
						boolean b9 = foo() && foo();
						boolean b10 = foo() || foo();
					}
					static boolean foo() { return true; }
					Zork z;
				}
				"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					boolean b1 = args == args;
					             ^^^^^^^^^^^^
				Comparing identical expressions
				----------
				2. WARNING in X.java (at line 4)
					boolean b2 = args != args;
					             ^^^^^^^^^^^^
				Comparing identical expressions
				----------
				3. WARNING in X.java (at line 5)
					boolean b3 = b1 == b1;
					             ^^^^^^^^
				Comparing identical expressions
				----------
				4. WARNING in X.java (at line 6)
					boolean b4 = b1 != b1;
					             ^^^^^^^^
				Comparing identical expressions
				----------
				5. WARNING in X.java (at line 7)
					boolean b5 = b1 && b1;
					             ^^^^^^^^
				Comparing identical expressions
				----------
				6. WARNING in X.java (at line 8)
					boolean b6 = b1 || b1;
					             ^^^^^^^^
				Comparing identical expressions
				----------
				7. ERROR in X.java (at line 16)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}

/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=276740"
 */
public void test0038() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						boolean b1 = 1 == 1;
						boolean b2 = 1 != 1;
						boolean b3 = 1 == 1.0;
						boolean b4 = 1 != 1.0;
						boolean b5 = 1 == 2;
						boolean b6 = 1 != 2;
						boolean b7 = 1 == 2.0;
						boolean b8 = 1 != 2.0;
				       final short s1 = 1;
				       final short s2 = 2;
				       boolean b9 = 1 == s1;
				       boolean b10 = 1 == s2;
				       boolean b91 = 1 != s1;
				       boolean b101 = 1 != s2;
				       final long l1 = 1;
				       final long l2 = 2;
				       boolean b11 = 1 == l1;
				       boolean b12 = 1 == l2;
				       boolean b111 = 1 != l1;
				       boolean b121 = 1 != l2;
				       boolean b13 = s1 == l1;
				       boolean b14 = s1 == l2;
				       boolean b15 = s1 != l1;
				       boolean b16 = s1 != l2;
					}
					Zork z;
				}
				"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					boolean b1 = 1 == 1;
					             ^^^^^^
				Comparing identical expressions
				----------
				2. WARNING in X.java (at line 4)
					boolean b2 = 1 != 1;
					             ^^^^^^
				Comparing identical expressions
				----------
				3. WARNING in X.java (at line 5)
					boolean b3 = 1 == 1.0;
					             ^^^^^^^^
				Comparing identical expressions
				----------
				4. WARNING in X.java (at line 6)
					boolean b4 = 1 != 1.0;
					             ^^^^^^^^
				Comparing identical expressions
				----------
				5. WARNING in X.java (at line 13)
					boolean b9 = 1 == s1;
					             ^^^^^^^
				Comparing identical expressions
				----------
				6. WARNING in X.java (at line 15)
					boolean b91 = 1 != s1;
					              ^^^^^^^
				Comparing identical expressions
				----------
				7. WARNING in X.java (at line 19)
					boolean b11 = 1 == l1;
					              ^^^^^^^
				Comparing identical expressions
				----------
				8. WARNING in X.java (at line 21)
					boolean b111 = 1 != l1;
					               ^^^^^^^
				Comparing identical expressions
				----------
				9. WARNING in X.java (at line 23)
					boolean b13 = s1 == l1;
					              ^^^^^^^^
				Comparing identical expressions
				----------
				10. WARNING in X.java (at line 25)
					boolean b15 = s1 != l1;
					              ^^^^^^^^
				Comparing identical expressions
				----------
				11. ERROR in X.java (at line 28)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}

/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=276741"
 */
public void test0039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void gain(String[] args) {
						boolean b1 = this == this;
						boolean b2 = this != this;
						boolean b3 = this != new X();
						boolean b4 = this == new X();
					}
					Zork z;
				}
				"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					boolean b1 = this == this;
					             ^^^^^^^^^^^^
				Comparing identical expressions
				----------
				2. WARNING in X.java (at line 4)
					boolean b2 = this != this;
					             ^^^^^^^^^^^^
				Comparing identical expressions
				----------
				3. ERROR in X.java (at line 8)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
/**
 * see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=281776"
 * We now tolerate comparison of float and double entities against
 * themselves as a legitimate idiom for NaN checking.
 */
public void test0040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        double var = Double.NaN;
				            if(var != var) {
				                  System.out.println("NaN");
				            }
				            float varf = 10;
				            if(varf != varf) {
				            	System.out.println("NaN");
				            }
				   }
					Zork z;
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=251227
public void test0041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						System.out.println(1.0 == 1.0);
						System.out.println(1.0f == 1.0f);
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				System.out.println(1.0 == 1.0);
				                   ^^^^^^^^^^
			Comparing identical expressions
			----------
			2. WARNING in X.java (at line 4)
				System.out.println(1.0f == 1.0f);
				                   ^^^^^^^^^^^^
			Comparing identical expressions
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=248897
public void test0042() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	runTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
					    public static void main(String[]  args) {
					        final String var = "Hello";
					        final int local = 10;
					        @ZAnn(var + local)
					        class X {}
					        new X();
					    }
					}
					@interface ZAnn {
					    String value();
					}
					"""
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedLocal
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=313825
public void test0043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
					void foo(int i) {
						foo((a));
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				foo((a));
				     ^
			a cannot be resolved to a variable
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310264
public void test0044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				   volatile int x;
				   int nvx;
					void foo(int i) {
						x = x;
				       nvx = nvx;
					}
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				nvx = nvx;
				^^^^^^^^^
			The assignment to variable nvx has no effect
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310264
public void test0045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				   volatile int x = this.x;
				   int nvx = this.nvx;
					void foo(int i) {
					}
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				volatile int x = this.x;
				             ^^^^^^^^^^
			The assignment to variable x has no effect
			----------
			2. WARNING in X.java (at line 3)
				int nvx = this.nvx;
				    ^^^^^^^^^^^^^^
			The assignment to variable nvx has no effect
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
public void test0046() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    int foo() {
					        int i=1;
					        boolean b=false;
					        b|=true;
					        int k = 2;
					        --k;
					        k+=3;
					        Integer j = 3;
					        j++;
					        i++;
					        return i++;
					    }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					boolean b=false;
					        ^
				The value of the local variable b is not used
				----------
				2. WARNING in X.java (at line 6)
					int k = 2;
					    ^
				The value of the local variable k is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with private fields instead of locals
public void test0046_field() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    private int i=1;
					    private boolean b=false;
					    private int k = 2;
					    private Integer j = 3;
					    int foo() {
					        b|=true;
					        --k;
					        k+=3;
					        j++;
					        return i++;
					    }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					private boolean b=false;
					                ^
				The value of the field X.b is not used
				----------
				2. WARNING in X.java (at line 4)
					private int k = 2;
					            ^
				The value of the field X.k is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with private fields instead of locals - this-qualified access
public void test0046_field_this_qualified() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    private int i=1;
					    private boolean b=false;
					    private int k = 2;
					    private Integer j = 3;
					    int foo() {
					        this.b|=true;
					        --this.k;
					        getThis().k+=3;
					        this.j++;
					        return this.i++;
					    }
					    X getThis() { return this; }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					private boolean b=false;
					                ^
				The value of the field X.b is not used
				----------
				2. WARNING in X.java (at line 4)
					private int k = 2;
					            ^
				The value of the field X.k is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with private fields instead of locals - regular qualified access
public void test0046_field_qualified() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    private int i=1;
					    private boolean b=false;
					    private int k = 2;
					    private Integer j = 3;
					    int foo(X that) {
					        that.b|=true;
					        --that.k;
					        that.k+=3;
					        that.j++;
					        that.i++;
					        return that.i++;
					    }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					private boolean b=false;
					                ^
				The value of the field X.b is not used
				----------
				2. WARNING in X.java (at line 4)
					private int k = 2;
					            ^
				The value of the field X.k is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with fields inside a private type
public void test0046_field_in_private_type() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    private class Y {
					        int i=1;
					        public boolean b=false;
					        protected int k = 2;
					        Integer j = 3;
					    }
					    int foo(Y y) {
					        y.b|=true;
					        --y.k;
					        y.k+=3;
					        y.j++;
					        int result = y.i++;
					        y.i++;
					        return result;
					    }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					public boolean b=false;
					               ^
				The value of the field X.Y.b is not used
				----------
				2. WARNING in X.java (at line 5)
					protected int k = 2;
					              ^
				The value of the field X.Y.k is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
public void test0047() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    void foo(int param1, int param2, Integer param3) {
					        boolean b=false;
					        b|=true;
					        param1++;
					        {
					            int val=23;
					            param2 += val;
					        }
					        param3++;
					    }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					void foo(int param1, int param2, Integer param3) {
					             ^^^^^^
				The value of the parameter param1 is not used
				----------
				2. WARNING in X.java (at line 2)
					void foo(int param1, int param2, Integer param3) {
					                         ^^^^^^
				The value of the parameter param2 is not used
				----------
				3. WARNING in X.java (at line 3)
					boolean b=false;
					        ^
				The value of the local variable b is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that unused parameter warning is not shown for an implementing method's parameter when
// CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract is disabled
public void test0048() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X extends A implements Y{
					   public void foo(int param1, int param2, Integer param3) {
					        boolean b=false;
					        b|=true;
					        param1++;
					        param2 += 1;
					        param3++;
					    }
					   public void foo(int param1, int param2) {
					        boolean b=false;
					        b|=true;
					        param1++;
					        param2 += 1;
					    }
					   public void bar(int param1, int param2, Integer param3) {
					        param1++;
					        param2 += 1;
					        param3++;
					    }
					}
					interface Y{
						public void foo(int param1, int param2, Integer param3);\
					}
					abstract class A{
						public abstract void bar(int param1, int param2, Integer param3);\
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					boolean b=false;
					        ^
				The value of the local variable b is not used
				----------
				2. WARNING in X.java (at line 9)
					public void foo(int param1, int param2) {
					                    ^^^^^^
				The value of the parameter param1 is not used
				----------
				3. WARNING in X.java (at line 9)
					public void foo(int param1, int param2) {
					                                ^^^^^^
				The value of the parameter param2 is not used
				----------
				4. WARNING in X.java (at line 10)
					boolean b=false;
					        ^
				The value of the local variable b is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that unused parameter warning is not shown for an overriding method's parameter when
// CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete is disabled
public void test0049() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X extends A {
					   public void foo(int param1, int param2, Integer param3) {
					        boolean b=false;
					        b|=true;
					        param1++;
					        param2 += 1;
					        param3++;
					    }
					   public void foo(int param1, Integer param3) {
					        param1++;
					        param3++;
					    }
					}
					class A{
					   public void foo(int param1, int param2, Integer param3) {
					        param1 -=1;
					        param2--;
					        param3--;
					    }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					boolean b=false;
					        ^
				The value of the local variable b is not used
				----------
				2. WARNING in X.java (at line 9)
					public void foo(int param1, Integer param3) {
					                    ^^^^^^
				The value of the parameter param1 is not used
				----------
				3. WARNING in X.java (at line 15)
					public void foo(int param1, int param2, Integer param3) {
					                    ^^^^^^
				The value of the parameter param1 is not used
				----------
				4. WARNING in X.java (at line 15)
					public void foo(int param1, int param2, Integer param3) {
					                                ^^^^^^
				The value of the parameter param2 is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that unused local warning is not shown for locals declared in unreachable code
public void test0050() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    int foo() {
					        int i=1;
							 if (false) {
					        	boolean b=false;
					        	b|=true;
							 }
					        int k = 2;
					        --k;
					        k+=3;
					        Integer j = 3;
					        j++;
					        return i++;
					    }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					if (false) {
				        	boolean b=false;
				        	b|=true;
						 }
					           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				2. WARNING in X.java (at line 8)
					int k = 2;
					    ^
				The value of the local variable k is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that a constructor argument is handled correctly
public void test0051() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						class X {
						    X(int abc) {
						        abc++;
						    }
						}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					X(int abc) {
					      ^^^
				The value of the parameter abc is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
public void test0052() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						    Y y = new Y();
						    private class Y {
						        int abc;
						        Y() {
						            abc++;
						        }
						    }
						    class Z extends Y {}
						}"""
			},
			"",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			null/*vmArguments*/,
			customOptions,
			null/*requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
// multi-level inheritance
public void test0052a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
			new String[] {
					"Outer.java",
					"""
						class Outer {
						    private class Inner1 {
						        int foo;
						    }
						    private class Inner2 extends Inner1 { }
						    class Inner3 extends Inner2 { }
						}
						"""
			},
			"",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			null/*vmArguments*/,
			customOptions,
			null/*requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
// member type of private
public void test0052b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
			new String[] {
					"Outer.java",
					"""
						class Outer {
						    private class Inner1 {
						        class Foo{}
						    }
						    private class Inner2 extends Inner1 { }
						    class Inner3 extends Inner2 { }
						}
						"""
			},
			"",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			null/*vmArguments*/,
			customOptions,
			null/*requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0053() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				    int foo() {
				        int i=1;
				        i++;
				        return 0;
				    }
				}"""
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
	String expectedOutput =
		"""
		  // Method descriptor #15 ()I
		  // Stack: 1, Locals: 1
		  int foo();
		    0  iconst_0
		    1  ireturn
		      Line numbers:
		        [pc: 0, line: 5]
		      Local variable table:
		        [pc: 0, pc: 2] local: this index: 0 type: X
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0054() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				    int foo() {
				        int i=1;
				        return i+=1;
				    }
				}"""
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
	String expectedOutput =
		"""
		  // Method descriptor #15 ()I
		  // Stack: 1, Locals: 2
		  int foo();
		    0  iconst_1
		    1  istore_1 [i]
		    2  iinc 1 1 [i]
		    5  iload_1 [i]
		    6  ireturn
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 2, line: 4]
		      Local variable table:
		        [pc: 0, pc: 7] local: this index: 0 type: X
		        [pc: 2, pc: 7] local: i index: 1 type: int
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=329613
// regression caused by https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0055() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runNegativeTest(
			new String[] {
					"test1/E.java",
					"""
						package test1;
						public class E {
						    private void foo() {
						        int a= 10;
						        a++;
						        a--;
						        --a;
						        ++a;
						        for ( ; ; a++) {
						        }
						    }
						}"""
			},
			"""
				----------
				1. WARNING in test1\\E.java (at line 4)
					int a= 10;
					    ^
				The value of the local variable a is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0056() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    static int foo() {
				        int i = 2;
				        int j = 3;
				        return (i += j *= 3);
				    }
				    public static void main(String[] args) {
				        System.out.println(foo());
				    }
				}"""
		},
		"11",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0057() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main (String args[]) {
				        int i = 0;
				        i += 4 + foo();
				    }
				    public static int foo() {
				    	System.out.println("OK");
				    	return 0;
				    }
				}"""
		},
		"OK",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336648
public void _test0058() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    void foo(String m) {
					        final String message= m;
					        new Runnable() {
					            public void run() {
					                if ("x".equals(message)) {
					                    bug(); // undefined method
					                }
					            }
					        }.run();
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					bug(); // undefined method
					^^^
				The method bug() is undefined for the type new Runnable(){}
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339139
// Issue local variable not used warning inside deadcode
public void test0059() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				    	Object a = null;
				    	if (a != null){
				        	int j = 3;
				        	j++;
				    	}
				    	System.out.println("OK");
				    }
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				if (a != null){
			        	int j = 3;
			        	j++;
			    	}
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			2. WARNING in X.java (at line 5)
				int j = 3;
				    ^
			The value of the local variable j is not used
			----------
			""",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0060() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUninternedIdentityComparison, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"org/eclipse/jdt/internal/compiler/lookup/X.java",
			"""
				package org.eclipse.jdt.internal.compiler.lookup;
				class TypeBinding {
				}
				public class X {
					public static void main(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) {
								}
							}
						}
					}
					public static void gain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) {
								}
							}
						}
					}
					public static void vain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						//$IDENTITY-COMPARISON$
						//$IDENTITY-COMPARISON$
						//$IDENTITY-COMPARISON$
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) { //$IDENTITY-COMPARISON$
								}
							}
						}
					}
					public static void cain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) { //$IDENTITY-COMPARISON$
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) { //$IDENTITY-COMPARISON$
								}
							}
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 7)
				if (t1 == t2) {\s
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			2. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 9)
				if (t1 == t2) {
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			3. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 16)
				if (t1 == t2) {\s
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			4. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 18)
				if (t1 == t2) {
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			5. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 28)
				if (t1 == t2) {\s
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			""",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0061() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUninternedIdentityComparison, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"org/eclipse/nonjdt/internal/compiler/lookup/X.java",
			"""
				package org.eclipse.nonjdt.internal.compiler.lookup;
				class TypeBinding {
				}
				public class X {
					public static void main(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) {
								}
							}
						}
					}
					public static void gain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) {
								}
							}
						}
					}
					public static void vain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						//$IDENTITY-COMPARISON$
						//$IDENTITY-COMPARISON$
						//$IDENTITY-COMPARISON$
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) { //$IDENTITY-COMPARISON$
								}
							}
						}
					}
					public static void cain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) { //$IDENTITY-COMPARISON$
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) { //$IDENTITY-COMPARISON$
								}
							}
						}
					}
				}
				"""
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0062() throws Exception {
	Map customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"org/eclipse/jdt/internal/compiler/lookup/X.java",
			"""
				package org.eclipse.jdt.internal.compiler.lookup;
				class TypeBinding {
				}
				public class X {
					public static void main(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) {
								}
							}
						}
					}
					public static void gain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) {
								}
							}
						}
					}
					public static void vain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						//$IDENTITY-COMPARISON$
						//$IDENTITY-COMPARISON$
						//$IDENTITY-COMPARISON$
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) { //$IDENTITY-COMPARISON$
								}
							}
						}
					}
					public static void cain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) { //$IDENTITY-COMPARISON$
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) { //$IDENTITY-COMPARISON$
								}
							}
						}
					}
				}
				"""
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0063() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUninternedIdentityComparison, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"org/eclipse/jdt/core/dom/X.java",
			"""
				package org.eclipse.jdt.core.dom;
				interface ITypeBinding {
				}
				class TypeBinding implements ITypeBinding {
				}
				public class X {
					public static void main(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) {
								}
							}
						}
					}
					public static void gain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) {
								}
							}
						}
					}
					public static void vain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						//$IDENTITY-COMPARISON$
						//$IDENTITY-COMPARISON$
						//$IDENTITY-COMPARISON$
						if (t1 == t2) {\s
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) { //$IDENTITY-COMPARISON$
								}
							}
						}
					}
					public static void cain(String[] args) {
						TypeBinding t1 = null, t2 = null;
						if (t1 == t2) { //$IDENTITY-COMPARISON$
							if (t2 == t1) {  //$IDENTITY-COMPARISON$
								if (t1 == t2) { //$IDENTITY-COMPARISON$
								}
							}
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 9)
				if (t1 == t2) {\s
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			2. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 11)
				if (t1 == t2) {
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			3. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 18)
				if (t1 == t2) {\s
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			4. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 20)
				if (t1 == t2) {
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			5. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 30)
				if (t1 == t2) {\s
				    ^^^^^^^^
			The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.
			----------
			""",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// Collection: contains & remove & get
public void testBug410218a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				class X {
				  void test() {
					Set<Short> set = new HashSet<Short>();
					short one = 1;
					set.add(one);
				
					if (set.contains("ONE")) // bad
						set.remove("ONE"); // bad
					if (set.contains(1)) // bad
						set.remove(1); // bad (tries to remove "Integer 1")
					System.out.println(set); // shows that the "Short 1" is still in!
				
					if (set.contains(one)) // ok
						set.remove(one); // ok
					if (set.contains(Short.valueOf(one))) // ok
						set.remove(Short.valueOf(one)); // ok
					System.out.println(set);
				  }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 8)
				if (set.contains("ONE")) // bad
				                 ^^^^^
			Unlikely argument type String for contains(Object) on a Collection<Short>
			----------
			2. WARNING in X.java (at line 9)
				set.remove("ONE"); // bad
				           ^^^^^
			Unlikely argument type String for remove(Object) on a Collection<Short>
			----------
			3. WARNING in X.java (at line 10)
				if (set.contains(1)) // bad
				                 ^
			Unlikely argument type int for contains(Object) on a Collection<Short>
			----------
			4. WARNING in X.java (at line 11)
				set.remove(1); // bad (tries to remove "Integer 1")
				           ^
			Unlikely argument type int for remove(Object) on a Collection<Short>
			----------
			""");
}
// HashSet vs. TreeSet
public void testBug410218b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				class X {
				  <T> void test(Set<HashSet<T>> hss, TreeSet<T> ts, LinkedHashSet<T> lhs) {
					if (hss.contains(ts)) // bad
						hss.remove(ts); // bad
					if (hss.contains((Set<T>)ts)) // ok
						hss.remove((Set<T>)ts); // ok
					if (hss.contains(lhs)) // ok
						hss.remove(lhs); // ok
				  }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				if (hss.contains(ts)) // bad
				                 ^^
			Unlikely argument type TreeSet<T> for contains(Object) on a Collection<HashSet<T>>
			----------
			2. WARNING in X.java (at line 5)
				hss.remove(ts); // bad
				           ^^
			Unlikely argument type TreeSet<T> for remove(Object) on a Collection<HashSet<T>>
			----------
			""");
}
// HashSet vs. TreeSet or: strict
public void testBug410218b2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE_STRICT, JavaCore.ENABLED);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				class X {
				  <T> void test(Set<HashSet<T>> hss, TreeSet<T> ts, LinkedHashSet<T> lhs) {
					if (hss.contains(ts)) // bad
						hss.remove(ts); // bad
					if (hss.contains((Set<T>)ts)) // bad (because of strict check)
						hss.remove((Set<T>)ts); // bad (because of strict check)
					if (hss.contains(lhs)) // ok
						hss.remove(lhs); // ok
				  }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				if (hss.contains(ts)) // bad
				                 ^^
			Unlikely argument type TreeSet<T> for contains(Object) on a Collection<HashSet<T>>
			----------
			2. WARNING in X.java (at line 5)
				hss.remove(ts); // bad
				           ^^
			Unlikely argument type TreeSet<T> for remove(Object) on a Collection<HashSet<T>>
			----------
			3. WARNING in X.java (at line 6)
				if (hss.contains((Set<T>)ts)) // bad (because of strict check)
				                 ^^^^^^^^^^
			Unlikely argument type Set<T> for contains(Object) on a Collection<HashSet<T>>
			----------
			4. WARNING in X.java (at line 7)
				hss.remove((Set<T>)ts); // bad (because of strict check)
				           ^^^^^^^^^^
			Unlikely argument type Set<T> for remove(Object) on a Collection<HashSet<T>>
			----------
			""",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// Map: contains* & remove & get
public void testBug410218c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				class X {
				  Number test(Map<? extends Number, Number> m, boolean f) {
					if (m.containsKey("ONE")) // bad
						m.remove("ONE"); // bad
					if (m.containsValue("ONE")) // bad
						m.remove("ONE"); // bad
					short one = 1;
					if (m.containsKey(one)) // almost ok
						m.remove(one); // almost ok
					if (m.containsValue(Short.valueOf(one))) // ok
						m.remove(Short.valueOf(one)); // almost ok
					if (f)
						return m.get("ONE"); // bad
					return m.get(one);
				 // almost ok
				  }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				if (m.containsKey("ONE")) // bad
				                  ^^^^^
			Unlikely argument type String for containsKey(Object) on a Map<capture#1-of ? extends Number,Number>
			----------
			2. WARNING in X.java (at line 5)
				m.remove("ONE"); // bad
				         ^^^^^
			Unlikely argument type String for remove(Object) on a Map<capture#2-of ? extends Number,Number>
			----------
			3. WARNING in X.java (at line 6)
				if (m.containsValue("ONE")) // bad
				                    ^^^^^
			Unlikely argument type String for containsValue(Object) on a Map<capture#3-of ? extends Number,Number>
			----------
			4. WARNING in X.java (at line 7)
				m.remove("ONE"); // bad
				         ^^^^^
			Unlikely argument type String for remove(Object) on a Map<capture#4-of ? extends Number,Number>
			----------
			5. WARNING in X.java (at line 14)
				return m.get("ONE"); // bad
				             ^^^^^
			Unlikely argument type String for get(Object) on a Map<capture#9-of ? extends Number,Number>
			----------
			""");
}
// Collection: {contains,remove,retain}All, non-generic sub type of Collection, configured to be ERROR
public void testBug410218d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE, JavaCore.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				interface NumberCollection extends Collection<Number> {}
				class X {
				  void test(NumberCollection numbers, List<Integer> ints, Set<String> stringSet) {
					if (numbers.containsAll(ints)) // ok
						numbers.removeAll(ints); // ok
					else
						numbers.retainAll(ints); // ok
				
					numbers.removeAll(stringSet); // bad
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				numbers.removeAll(stringSet); // bad
				                  ^^^^^^^^^
			Unlikely argument type Set<String> for removeAll(Collection<?>) on a Collection<Number>
			----------
			""",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// List.indexOf: w/ and w/o @SuppressWarnings
public void testBug410218e() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE, JavaCore.WARNING);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				class X {
				  int test1(List<Integer> ints, Object o) {
					return ints.indexOf("ONE"); // bad
				  }
				  @SuppressWarnings("unlikely-arg-type")
				  int test2(List<Integer> ints, boolean f, Object o) {
					if (f)
						return ints.indexOf("ONE"); // bad but suppressed
					return ints.indexOf(o); // supertype
				  }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				return ints.indexOf("ONE"); // bad
				                    ^^^^^
			Unlikely argument type String for indexOf(Object) on a List<Integer>
			----------
			""",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}

// Method references, equals, wildcards
public void testBug410218f() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_EQUALS_ARGUMENT_TYPE, JavaCore.INFO);
	runNegativeTest(
		new String[] {
			"test/TestUnlikely.java",
			"package test;\n" +
			"\n" +
			"import java.util.Collection;\n" +
			"import java.util.Iterator;\n" +
			"import java.util.List;\n" +
			"import java.util.Map;\n" +
			"import java.util.Objects;\n" +
			"import java.util.Set;\n" +
			"import java.util.function.BiPredicate;\n" +
			"import java.util.function.Predicate;\n" +
			"\n" +
			"public class TestUnlikely {\n" +
			"	interface Interface {\n" +
			"	}\n" +
			"\n" +
			"	interface OtherInterface {\n" +
			"	}\n" +
			"\n" +
			"	static class NonFinal implements Interface {\n" +
			"	}\n" +
			"\n" +
			"	static class Sub extends NonFinal implements OtherInterface {\n" +
			"	}\n" +
			"\n" +
			"	static final class Final implements Interface {\n" +
			"	}\n" +
			"\n" +
			"	void f1(List<Interface> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i);\n" +
			"		c.remove(o); // warning: unrelated interface\n" +
			"		c.remove(f);\n" +
			"		c.remove(nf);\n" +
			"		c.remove(s);\n" +
			"	}\n" +
			"\n" +
			"	void f2(List<OtherInterface> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i); // warning: unrelated interface\n" +
			"		c.remove(o);\n" +
			"		c.remove(f); // warning: impossible\n" +
			"		c.remove(nf); // warning: castable, but not supertype\n" +
			"		c.remove(s);\n" +
			"	}\n" +
			"\n" +
			"	void f3(List<Final> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i); // supertype\n" +
			"		c.remove(o); // warning: impossible\n" +
			"		c.remove(f);\n" +
			"		c.remove(nf); // warning: impossible\n" +
			"		c.remove(s); // warning: impossible\n" +
			"	}\n" +
			"\n" +
			"	void f4(List<NonFinal> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i); // supertype\n" +
			"		c.remove(o); // warning: unrelated interface\n" +
			"		c.remove(f); // warning: impossible\n" +
			"		c.remove(nf);\n" +
			"		c.remove(s);\n" +
			"	}\n" +
			"\n" +
			"	void f5(List<Sub> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i); // supertype\n" +
			"		c.remove(o); // supertype\n" +
			"		c.remove(f); // warning: impossible\n" +
			"		c.remove(nf); // supertype\n" +
			"		c.remove(s);\n" +
			"	}\n" +
			"\n" +
			"	<K, V> void map(Map<K, V> map, K key, V value) {\n" +
			"		map.containsKey(key);\n" +
			"		map.containsKey(value); // warning\n" +
			"		map.containsValue(key); // warning\n" +
			"		map.containsValue(value);\n" +
			"	}\n" +
			"\n" +
			"	boolean wildcards(Collection<?> c, Iterable<?> s) {\n" +
			"		for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {\n" +
			"			if (c.contains(iterator.next())) {\n" +
			"				return true;\n" +
			"			}\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	<T, U extends T> boolean relatedTypeVariables(Collection<T> c, Iterable<U> s) {\n" +
			"		for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {\n" +
			"			if (c.contains(iterator.next())) {\n" +
			"				return true;\n" +
			"			}\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	<T, U> boolean unrelatedTypeVariables(Collection<T> c, Iterable<U> s) {\n" +
			"		for (Iterator<U> iterator = s.iterator(); iterator.hasNext();) {\n" +
			"			if (c.contains(iterator.next())) { // warning\n" +
			"				return true;\n" +
			"			}\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	void all(List<NonFinal> c, Collection<Sub> s, Set<Final> other) {\n" +
			"		c.removeAll(s);\n" +
			"		s.removeAll(c);\n" +
			"		c.removeAll(other); // warning\n" +
			"	}\n" +
			"\n" +
			"	void methodRef(Set<Interface> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		Predicate<Interface> p1 = c::contains;\n" +
			"		BiPredicate<Collection<Interface>, Interface> bp1 = Collection<Interface>::contains;\n" +
			"		Predicate<OtherInterface> p2 = c::contains; // warning\n" +
			"		BiPredicate<Collection<Interface>, OtherInterface> bp2 = Collection<Interface>::contains; // warning\n" +
			"		p1.test(i);\n" +
			"		bp1.test(c, i);\n" +
			"		p2.test(o);\n" +
			"		bp2.test(c, o);\n" +
			"	}\n" +
			"\n" +
			"	void equals(String s, Integer i, Number n) {\n" +
			"		s.equals(i); // info\n" +
			"		i.equals(s); // info\n" +
			"		i.equals(n);\n" +
			"		n.equals(i);\n" +
			"\n" +
			"		Predicate<String> p1 = i::equals; // info\n" +
			"		p1.test(s);\n" +
			"\n" +
			"		BiPredicate<String, Integer> bp2 = Object::equals; // info\n" +
			"		bp2.test(s, i);\n" +
			"\n" +
			"		Objects.equals(s, i); // info\n" +
			"		Objects.equals(i, s); // info\n" +
			"		Objects.equals(n, i);\n" +
			"		Objects.equals(i, n);\n" +
			"\n" +
			"		BiPredicate<String, Integer> bp3 = Objects::equals; // info\n" +
			"		bp3.test(s, i);\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		"""
			----------
			1. WARNING in test\\TestUnlikely.java (at line 30)
				c.remove(o); // warning: unrelated interface
				         ^
			Unlikely argument type TestUnlikely.OtherInterface for remove(Object) on a Collection<TestUnlikely.Interface>
			----------
			2. WARNING in test\\TestUnlikely.java (at line 37)
				c.remove(i); // warning: unrelated interface
				         ^
			Unlikely argument type TestUnlikely.Interface for remove(Object) on a Collection<TestUnlikely.OtherInterface>
			----------
			3. WARNING in test\\TestUnlikely.java (at line 39)
				c.remove(f); // warning: impossible
				         ^
			Unlikely argument type TestUnlikely.Final for remove(Object) on a Collection<TestUnlikely.OtherInterface>
			----------
			4. WARNING in test\\TestUnlikely.java (at line 40)
				c.remove(nf); // warning: castable, but not supertype
				         ^^
			Unlikely argument type TestUnlikely.NonFinal for remove(Object) on a Collection<TestUnlikely.OtherInterface>
			----------
			5. WARNING in test\\TestUnlikely.java (at line 46)
				c.remove(o); // warning: impossible
				         ^
			Unlikely argument type TestUnlikely.OtherInterface for remove(Object) on a Collection<TestUnlikely.Final>
			----------
			6. WARNING in test\\TestUnlikely.java (at line 48)
				c.remove(nf); // warning: impossible
				         ^^
			Unlikely argument type TestUnlikely.NonFinal for remove(Object) on a Collection<TestUnlikely.Final>
			----------
			7. WARNING in test\\TestUnlikely.java (at line 49)
				c.remove(s); // warning: impossible
				         ^
			Unlikely argument type TestUnlikely.Sub for remove(Object) on a Collection<TestUnlikely.Final>
			----------
			8. WARNING in test\\TestUnlikely.java (at line 54)
				c.remove(o); // warning: unrelated interface
				         ^
			Unlikely argument type TestUnlikely.OtherInterface for remove(Object) on a Collection<TestUnlikely.NonFinal>
			----------
			9. WARNING in test\\TestUnlikely.java (at line 55)
				c.remove(f); // warning: impossible
				         ^
			Unlikely argument type TestUnlikely.Final for remove(Object) on a Collection<TestUnlikely.NonFinal>
			----------
			10. WARNING in test\\TestUnlikely.java (at line 63)
				c.remove(f); // warning: impossible
				         ^
			Unlikely argument type TestUnlikely.Final for remove(Object) on a Collection<TestUnlikely.Sub>
			----------
			11. WARNING in test\\TestUnlikely.java (at line 70)
				map.containsKey(value); // warning
				                ^^^^^
			Unlikely argument type V for containsKey(Object) on a Map<K,V>
			----------
			12. WARNING in test\\TestUnlikely.java (at line 71)
				map.containsValue(key); // warning
				                  ^^^
			Unlikely argument type K for containsValue(Object) on a Map<K,V>
			----------
			13. WARNING in test\\TestUnlikely.java (at line 95)
				if (c.contains(iterator.next())) { // warning
				               ^^^^^^^^^^^^^^^
			Unlikely argument type U for contains(Object) on a Collection<T>
			----------
			14. WARNING in test\\TestUnlikely.java (at line 105)
				c.removeAll(other); // warning
				            ^^^^^
			Unlikely argument type Set<TestUnlikely.Final> for removeAll(Collection<?>) on a Collection<TestUnlikely.NonFinal>
			----------
			15. WARNING in test\\TestUnlikely.java (at line 111)
				Predicate<OtherInterface> p2 = c::contains; // warning
				                               ^^^^^^^^^^^
			Unlikely argument type TestUnlikely.OtherInterface for contains(Object) on a Collection<TestUnlikely.Interface>
			----------
			16. WARNING in test\\TestUnlikely.java (at line 112)
				BiPredicate<Collection<Interface>, OtherInterface> bp2 = Collection<Interface>::contains; // warning
				                                                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unlikely argument type TestUnlikely.OtherInterface for contains(Object) on a Collection<TestUnlikely.Interface>
			----------
			17. INFO in test\\TestUnlikely.java (at line 120)
				s.equals(i); // info
				         ^
			Unlikely argument type for equals(): Integer seems to be unrelated to String
			----------
			18. INFO in test\\TestUnlikely.java (at line 121)
				i.equals(s); // info
				         ^
			Unlikely argument type for equals(): String seems to be unrelated to Integer
			----------
			19. INFO in test\\TestUnlikely.java (at line 125)
				Predicate<String> p1 = i::equals; // info
				                       ^^^^^^^^^
			Unlikely argument type for equals(): String seems to be unrelated to Integer
			----------
			20. INFO in test\\TestUnlikely.java (at line 128)
				BiPredicate<String, Integer> bp2 = Object::equals; // info
				                                   ^^^^^^^^^^^^^^
			Unlikely argument type for equals(): Integer seems to be unrelated to String
			----------
			21. INFO in test\\TestUnlikely.java (at line 131)
				Objects.equals(s, i); // info
				                  ^
			Unlikely argument type for equals(): Integer seems to be unrelated to String
			----------
			22. INFO in test\\TestUnlikely.java (at line 132)
				Objects.equals(i, s); // info
				                  ^
			Unlikely argument type for equals(): String seems to be unrelated to Integer
			----------
			23. INFO in test\\TestUnlikely.java (at line 136)
				BiPredicate<String, Integer> bp3 = Objects::equals; // info
				                                   ^^^^^^^^^^^^^^^
			Unlikely argument type for equals(): Integer seems to be unrelated to String
			----------
			"""
		,
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
public void testBug514956a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.ERROR);
	runConformTest(
		new String[] {
			"Unlikely.java",
			"""
				import java.util.Map;
				
				interface MApplicationElement {}
				interface EObject {}
				public class Unlikely {
					void m(Map<MApplicationElement, MApplicationElement> map, EObject key) {
						map.get((MApplicationElement)key);
					}
				}
				"""
		},
		customOptions);
}
public void testBug514956b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_EQUALS_ARGUMENT_TYPE, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.ERROR);
	runConformTest(
		new String[] {
			"Unlikely.java",
			"""
				interface EObject {}
				public class Unlikely {
					boolean m(EObject key) {
						return this.equals((Unlikely)key);
					}
				}
				"""
		},
		customOptions);
}
public void testBug514956c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_EQUALS_ARGUMENT_TYPE, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.ERROR);
	runNegativeTest(
		new String[] {
			"Unlikely.java",
			"""
				interface I1 {}
				interface I2 {}
				interface I3 {}
				public class Unlikely implements I1 {
					boolean m1(I1 i1) {
						return i1.equals((I1)this);
					}
					boolean m2(I1 i1, I2 i2) {
						return i1.equals((I3)i2);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Unlikely.java (at line 6)
				return i1.equals((I1)this);
				                 ^^^^^^^^
			Unnecessary cast from Unlikely to I1
			----------
			2. ERROR in Unlikely.java (at line 9)
				return i1.equals((I3)i2);
				                 ^^^^^^
			Unnecessary cast from I2 to I3
			----------
			3. WARNING in Unlikely.java (at line 9)
				return i1.equals((I3)i2);
				                 ^^^^^^
			Unlikely argument type for equals(): I3 seems to be unrelated to I1
			----------
			""",
		null, // classlibs
		false, // flush output dir
		customOptions);
}
// mixture of raw type an parametrized type
public void testBug513310() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runConformTest(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import java.util.List;\n" +
			"import java.util.Set;\n" +
			"\n" +
			"public class Test {\n" +
			"	void f(List dependencyList, Set<Object> set) {\n" +
			"		dependencyList.removeAll(set);\n" +
			"	}\n" +
			"}\n" +
			"",
		}
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/567
// Report unused variable for variables declared in instanceof pattern
public void testGH567() {
	if (this.complianceLevel < ClassFileConstants.JDK21)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    record Point (int x, int y) {}
					    void foo(Object o) {
					        if (o instanceof String s) { int x; }
					        if (o instanceof Point (int xVal, int yVal)) {}
					        switch (o) {
										case String c :\s
											break;
										default :
												break;
										}\
					        if (o instanceof String str) {  str.length();  }
					    }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					if (o instanceof String s) { int x; }
					                        ^
				The value of the local variable s is not used
				----------
				2. WARNING in X.java (at line 4)
					if (o instanceof String s) { int x; }
					                                 ^
				The value of the local variable x is not used
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
}