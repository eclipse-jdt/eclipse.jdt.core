/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

/**
 * Completion is expected to be a ClassLiteralAccess.
 */
public class ClassLiteralAccessCompletionTest extends AbstractCompletionTest {
public ClassLiteralAccessCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(ClassLiteralAccessCompletionTest.class);
}
/*
 * Completion on the keyword 'class' on an array type
 */
public void testArrayType() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				void foo() {								\t
					String[].;								\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"String[].",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:String[].>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnClassLiteralAccess:String[].>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"String[].",
		// test name
		"<complete on array type member>"
	);
}
/*
 * Test access to the keyword 'class' on an array type
 * where the keyword is non empty.
 */
public void testArrayTypeWithNonEmptyIdentifier() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				void foo() {								\t
					String[].class;							\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"String[].cl",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:String[].cl>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnClassLiteralAccess:String[].cl>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"cl",
		// expectedReplacedSource:
		"String[].class",
		// test name
		"<complete on array type member with non empty identifier>"
	);
}
/*
 * Completion on the keyword 'class' on a primitive array type
 */
public void testPrimitiveArrayType() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				void foo() {								\t
					int[].;									\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"int[].",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int[].>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnClassLiteralAccess:int[].>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"int[].",
		// test name
		"<complete on primitive array type member>"
	);
}
/*
 * Completion on the keyword 'class' on a primitive array type where the
 * keyword is non empty
 */
public void testPrimitiveArrayTypeWithNonEmptyIdentifier() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				void foo() {								\t
					int[].class;							\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"int[].cl",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int[].cl>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnClassLiteralAccess:int[].cl>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"cl",
		// expectedReplacedSource:
		"int[].class",
		// test name
		"<complete on primitive array type member with non empty identifier>"
	);
}
/*
 * Completion on the keyword 'class' on a primitive type
 */
public void testPrimitiveType() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				void foo() {								\t
					int.;									\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"int.",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int.>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnClassLiteralAccess:int.>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"int.",
		// test name
		"<complete on primitive type member>"
	);
}
/*
 * Completion on the keyword 'class' on a primitive type where the
 * keyword is non empty
 */
public void testPrimitiveTypeWithNonEmptyIdentifier() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				void foo() {								\t
					int.class;								\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"int.cl",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int.cl>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnClassLiteralAccess:int.cl>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"cl",
		// expectedReplacedSource:
		"int.class",
		// test name
		"<complete on primitive type member with non empty identifier>"
	);
}
}
