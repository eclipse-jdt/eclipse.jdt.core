/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTest_18 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocDescription = CompilerOptions.RETURN_TAG;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;
	String reportDeprecation = CompilerOptions.ERROR;
	String reportJavadocDeprecation = null;
	String processAnnotations = null;

public JavadocTest_18(String name) {
	super(name);
}

public static Class javadocTestClass() {
	return JavadocTest_18.class;
}

// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {

}

public static Test suite() {
	return buildMinimalComplianceTestSuite(javadocTestClass(), F_18);
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
	options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
	if (!CompilerOptions.IGNORE.equals(this.reportInvalidJavadoc)) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocVisibility);
	}
	if (this.reportJavadocDeprecation != null) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, this.reportJavadocDeprecation);
	}
	if (this.reportMissingJavadocComments != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, CompilerOptions.ENABLED);
		if (this.reportMissingJavadocCommentsVisibility != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, this.reportMissingJavadocCommentsVisibility);
		}
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocTags != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, CompilerOptions.ENABLED);
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocDescription != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagDescription, this.reportMissingJavadocDescription);
	}
	if (this.processAnnotations != null) {
		options.put(CompilerOptions.OPTION_Process_Annotations, this.processAnnotations);
	}
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportDeprecation, this.reportDeprecation);
	options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	return options;
}
/* (non-Javadoc)
 * @see junit.framework.TestCase#setUp()
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.docCommentSupport = CompilerOptions.ENABLED;
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
	this.reportDeprecation = CompilerOptions.ERROR;
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
}



public void test001() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet : public static void main(String... args) {
					 *       for (var arg : args) {                \s
					 *           if (!arg.isBlank()) {
					 *               System.out.println(arg);
					 *           }
					 *       }                                     \s
					 *   }
					 *   }
					 */
					public class X {
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				* {@snippet : public static void main(String... args) {
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Snippet content should be on a new line
			----------
			""",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test002() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet : \
					 * public static void main(String... args) {
					 *       for (var arg : args) {                \s
					 *           if (!arg.isBlank()) {
					 *               System.out.println(arg);
					 *           }
					 *       }                                     \s
					 *   }
					 *   }
					 */
					public class X {
					}""",
		},
		null,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test003() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet:
					 *abcd                 \s
					 */
					public class X {
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				* {@snippet:
				  ^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			2. ERROR in X.java (at line 2)
				* {@snippet:
				    ^^^^^^^^
			Javadoc: Space required after snippet tag
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test004() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet :
					 *abcd   }              \s
					 */
					public class X {
					}""",
		}
	);
}

public void test005() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet :
					 * while(true){{{            \s
					 * }            \s
					 */
					public class X {
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				* {@snippet :
				  ^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			"""
	,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test006() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet\s
					 *             \s
					 * }            \s
					 */
					public class X {
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				* }            \s
				   ^^^^^^^^^^^^^
			Javadoc: Snippet is invalid due to missing colon
			----------
			""",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test007() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet\s
					 *             \s
					 * }            \s
					 */
					public class X {
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				* }            \s
				   ^^^^^^^^^^^^^
			Javadoc: Snippet is invalid due to missing colon
			----------
			""",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test008() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet :\s
					 *     abc // @replace substring='a'  regex='a'      \s
					 * }            \s
					 */
					public class X {
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				*     abc // @replace substring='a'  regex='a'      \s
				          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Attribute regex and substring used simulataneously
			----------
			""",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test009() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet :\s
					 *     abc // @highlight substring='a'  region='abcd'     \s
					 *      //@end region='abc'       \s
					 * }            \s
					 */
					public class X {
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				* }            \s
				   ^^^^^^^^^^^^^
			Javadoc: Region in the snippet is not closed
			----------
			""",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test010() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				"""
					 /**
					 * {@snippet :\s
					 *     abc // @highlight substring='a'  region='abcd'     \s
					 *     abc // @highlight substring='a'  region='abcd'     \s
					 *      //@end region='abcd'       \s
					 *      //@end region='abcd'       \s
					 * }            \s
					 */
					public class X {
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				*     abc // @highlight substring='a'  region='abcd'     \s
				                                       ^^^^^^
			Javadoc: Duplicate region
			----------
			""",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
}

