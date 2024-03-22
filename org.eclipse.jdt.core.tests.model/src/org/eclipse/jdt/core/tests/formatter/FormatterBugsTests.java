/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Ray V. (voidstar@gmail.com) - Contribution for bug 282988
 *     Robin Stocker - Bug 49619 - [formatting] comment formatter leaves whitespace in comments
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] IndexOutOfBoundsException in TokenManager - https://bugs.eclipse.org/462945
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] follow up bug for comments - https://bugs.eclipse.org/458208
 *     Mateusz Matela <mateusz.matela@gmail.com> - NPE in WrapExecutor during Java text formatting  - https://bugs.eclipse.org/465669
 *     Till Brychcy - Bug 471090 - Java Code Formatter breaks code if single line comments contain unicode escape - https://bugs.eclipse.org/471090
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions.Alignment;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FormatterBugsTests extends FormatterRegressionTests {

public static Test suite() {
	return buildModelTestSuite(FormatterBugsTests.class);
}

public FormatterBugsTests(String name) {
	super(name);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#setUp()
 */
private void setUpBracesPreferences(String braces) {
	if (braces != null) {
	 	assertTrue("Invalid value for braces preferences: "+braces,
			braces.equals(DefaultCodeFormatterConstants.END_OF_LINE) ||
	 		braces.equals(DefaultCodeFormatterConstants.NEXT_LINE) ||
	 		braces.equals(DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP) ||
	 		braces.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED));
		this.formatterPrefs.brace_position_for_annotation_type_declaration = braces;
		this.formatterPrefs.brace_position_for_anonymous_type_declaration = braces;
		this.formatterPrefs.brace_position_for_array_initializer = braces;
		this.formatterPrefs.brace_position_for_block = braces;
		this.formatterPrefs.brace_position_for_block_in_case = braces;
		this.formatterPrefs.brace_position_for_constructor_declaration = braces;
		this.formatterPrefs.brace_position_for_enum_constant = braces;
		this.formatterPrefs.brace_position_for_enum_declaration = braces;
		this.formatterPrefs.brace_position_for_method_declaration = braces;
		this.formatterPrefs.brace_position_for_switch = braces;
		this.formatterPrefs.brace_position_for_type_declaration = braces;
	}
}

/**
 * Create project and set the jar placeholder.
 */
@Override
public void setUpSuite() throws Exception {
	if (JAVA_PROJECT == null) {
		JAVA_PROJECT = setUpJavaProject("FormatterBugs", "1.5"); //$NON-NLS-1$
	}
	super.setUpSuite();
}

/**
 * bug 27079: [formatter] Tags for disabling/enabling code formatter (feature)
 * test Ensure that the formatter does not format code between specific javadoc comments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=27079"
 */
public void testBug027079a() throws JavaModelException {
	String source =
		"""
		public class X01 {
		
		/* disable-formatter */
		void     foo(    )      {\t
						//      unformatted       comment
		}
		/* enable-formatter */
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
				/* disable-formatter */
				void foo() {
					// unformatted comment
				}
			
				/* enable-formatter */
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug027079a1() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X01 {
		
		/* disable-formatter */
		void     foo(    )      {\t
						//      unformatted       comment
		}
		/* enable-formatter */
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
			/* disable-formatter */
			void     foo(    )      {\t
							//      unformatted       comment
			}
			/* enable-formatter */
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug027079a2() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X01 {
		
		/** disable-formatter */
		void     foo(    )      {\t
						//      unformatted       comment
		}
		/** enable-formatter */
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
			/** disable-formatter */
			void     foo(    )      {\t
							//      unformatted       comment
			}
			/** enable-formatter */
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug027079a3() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X01 {
		
		// disable-formatter
		void     foo(    )      {\t
						//      unformatted       comment
		}
		// enable-formatter
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
			// disable-formatter
			void     foo(    )      {\t
							//      unformatted       comment
			}
			// enable-formatter
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug027079a4() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X01 {
		
		// disable-formatter
		void     foo(    )      {\t
						//      unformatted       comment  	 \s
		}
		// enable-formatter\s
		void     bar(    )      {\t
						//      formatted       comment  	 \s
						/* disable-formatter *//*      unformatted		comment  	  *//* enable-formatter */
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
			// disable-formatter
			void     foo(    )      {\t
							//      unformatted       comment  	 \s
			}
			// enable-formatter\s
				void bar() {
					// formatted comment
					/* disable-formatter *//*      unformatted		comment  	  *//* enable-formatter */
				}
			}
			"""
	);
}
public void testBug027079b() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X02 {
		void foo() {
		/* disable-formatter */
						/*       unformatted		comment  	  */
			String test1= "this"+
							"is"+
					"a specific"+
				"line wrapping ";
		
		/* enable-formatter */
						/*       formatted		comment  	  */
			String test2= "this"+
							"is"+
					"a specific"+
				"line wrapping ";
		}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				void foo() {
			/* disable-formatter */
							/*       unformatted		comment  	  */
				String test1= "this"+
								"is"+
						"a specific"+
					"line wrapping ";
			
			/* enable-formatter */
					/* formatted comment */
					String test2 = "this" + "is" + "a specific" + "line wrapping ";
				}
			}
			"""
	);
}
public void testBug027079c() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X03 {
		void foo() {
		/* disable-formatter */
			bar(
						/**       unformatted		comment  	  */
						"this"  ,
							"is",
					"a specific",
				"line wrapping "
			);
		
		/* enable-formatter */
			bar(
						/**       formatted		comment  	  */
						"this"  ,
							"is",
					"a specific",
				"line wrapping "
			);
		}
		void bar(String... str) {}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				void foo() {
			/* disable-formatter */
				bar(
							/**       unformatted		comment  	  */
							"this"  ,
								"is",
						"a specific",
					"line wrapping "
				);
			
			/* enable-formatter */
					bar(
							/** formatted comment */
							"this", "is", "a specific", "line wrapping ");
				}
			
				void bar(String... str) {
				}
			}
			"""
	);
}
public void testBug027079c2() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X03b {
		void foo() {
			bar(
		// disable-formatter
						/**       unformatted		comment  	  */
						"this"  ,
							"is",
					"a specific",
				"line wrapping "
		// enable-formatter
			);
			bar(
						/**       formatted		comment  	  */
						"this"  ,
							"is",
					"a specific",
				"line wrapping "
			);
		}
		void bar(String... str) {}
		}
		""";
	formatSource(source,
		"""
			public class X03b {
				void foo() {
					bar(
			// disable-formatter
							/**       unformatted		comment  	  */
							"this"  ,
								"is",
						"a specific",
					"line wrapping "
			// enable-formatter
					);
					bar(
							/** formatted comment */
							"this", "is", "a specific", "line wrapping ");
				}
			
				void bar(String... str) {
				}
			}
			"""
	);
}
public void testBug027079d() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X04 {
		
		/* disable-formatter */
		void     foo(    )      {\t
						//      unformatted       comment  	 \s
		}
		/* enable-formatter */
		void     bar(    )      {\t
						//      formatted       comment  	 \s
		}
		}
		""";
	formatSource(source,
		"""
			public class X04 {
			
			/* disable-formatter */
			void     foo(    )      {\t
							//      unformatted       comment  	 \s
			}
			/* enable-formatter */
				void bar() {
					// formatted comment
				}
			}
			""",
		CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
		0 /* indentation level */,
		0 /* offset */,
		-1 /* length (all) */,
		"\n",
		true/*repeat*/);
}
public void testBug027079d2() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X04b {
		
		/* disable-formatter */
		void     foo(    )      {\t
						//      unformatted       comment  	 \s
		}
		/* enable-formatter */
		void     bar(    )      {\t
						//      formatted       comment  	 \s
		}
		}
		""";
	formatSource(source,
		"""
			public class X04b {
			
			/* disable-formatter */
			void     foo(    )      {\t
							//      unformatted       comment  	 \s
			}
			/* enable-formatter */
				void bar() {
					// formatted comment
				}
			}
			""",
		CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
		0 /* indentation level */,
		0 /* offset */,
		-1 /* length (all) */,
		"\n",
		true/*repeat*/);
}
public void testBug027079d3() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X04c {
		
		/* disable-formatter */
		void     foo(    )      {\t
						//      unformatted       comment  	 \s
		}
		/* enable-formatter */
		void     bar(    )      {\t
						//      formatted       comment  	 \s
		}
		}
		""";
	formatSource(source,
		"""
			public class X04c {
			
			/* disable-formatter */
			void     foo(    )      {\t
							//      unformatted       comment  	 \s
			}
			/* enable-formatter */
				void bar() {
					// formatted comment
				}
			}
			""",
		CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
		0 /* indentation level */,
		0 /* offset */,
		-1 /* length (all) */,
		"\n",
		true/*repeat*/);
}
public void testBug027079d4() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X04d {
		
		/* disable-formatter */
		void     foo(    )      {\t
						//      unformatted       comment  	 \s
		}
		/* enable-formatter */
		void     bar(    )      {\t
						//      formatted       comment  	 \s
		}
		}
		""";
	formatSource(source,
		"""
			public class X04d {
			
			/* disable-formatter */
			void     foo(    )      {\t
							//      unformatted       comment  	 \s
			}
			/* enable-formatter */
				void bar() {
					// formatted comment
				}
			}
			""",
		CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
		0 /* indentation level */,
		0 /* offset */,
		-1 /* length (all) */,
		"\n",
		true/*repeat*/);
}
public void testBug027079e() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "format: off".toCharArray();
	this.formatterPrefs.enabling_tag = "format: on".toCharArray();
	String source =
		"""
		public class X05 {
		
		/* format: off */
		void     foo(    )      {\t
						//      unformatted       comment
		}
		/* format: on */
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X05 {
			
			/* format: off */
			void     foo(    )      {\t
							//      unformatted       comment
			}
			/* format: on */
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug027079f() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "format: off".toCharArray();
	this.formatterPrefs.enabling_tag = "format: on".toCharArray();
	String source =
		"""
		public class X06 {
		
		// format: off
		void     foo(    )      {\t
						//      unformatted       comment
		}
		// format: on
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X06 {
			
			// format: off
			void     foo(    )      {\t
							//      unformatted       comment
			}
			// format: on
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug027079f2() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "format: off".toCharArray();
	this.formatterPrefs.enabling_tag = "format: on".toCharArray();
	String source =
		"""
		public class X06b {
		
		/** format: off */
		void     foo(    )      {\t
						//      unformatted       comment
		}
		/** format: on */
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X06b {
			
			/** format: off */
			void     foo(    )      {\t
							//      unformatted       comment
			}
			/** format: on */
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug027079f3() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "    format:  	  off    ".toCharArray();
	this.formatterPrefs.enabling_tag = "	format:	  	on	".toCharArray();
	String source =
		"""
		public class X06c {
		
		/*    format:  	  off    */
		void     foo(    )      {\t
						//      unformatted       comment
		}
		// 	format:	  	on\t
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X06c {
			
			/*    format:  	  off    */
			void     foo(    )      {\t
							//      unformatted       comment
			}
			// 	format:	  	on\t
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug027079f4() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "    format:  	  off    ".toCharArray();
	this.formatterPrefs.enabling_tag = "	format:	  	on	".toCharArray();
	String source =
		"""
		public class X06d {
		
		/* format: off */
		void     foo(    )      {\t
						//      formatted       comment
		}
		/* format: on */
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X06d {
			
				/* format: off */
				void foo() {
					// formatted comment
				}
			
				/* format: on */
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}

/**
 * bug 59891: [formatter] the code formatter doesn't respect my new lines
 * test Ensure that the formatter keep line breaks wrapping set by users in the code
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=59891"
 */
public void testBug059891_01() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	String source =
		"""
		public class X01 {
			void test() {
				foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8));
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				void test() {
					foo(bar(1, 2, 3, 4),
							bar(5, 6, 7, 8));
				}
			}
			"""
	);
}
public void testBug059891_01b() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class X01 {
			void test() {
				foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8));
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				void test() {
					foo(bar(1, 2, 3, 4),
						bar(5, 6, 7, 8));
				}
			}
			"""
	);
}
public void testBug059891_02() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	String source =
		"""
		public class X02 {
			void test() {
				foo(bar(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), bar(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));
			}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				void test() {
					foo(bar(1, 2, 3, 4, 5, 6, 7, 8,
							9, 10),
							bar(11, 12, 13, 14, 15,
									16, 17, 18, 19,
									20));
				}
			}
			"""
	);
}
public void testBug059891_02b() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class X02 {
			void test() {
				foo(bar(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), bar(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));
			}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				void test() {
					foo(bar(1, 2, 3, 4, 5, 6, 7, 8,
							9, 10),
						bar(11, 12, 13, 14, 15, 16,
							17, 18, 19, 20));
				}
			}
			"""
	);
}
public void testBug059891_03() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	String source =
		"""
		public class X03 {
			void test() {
				foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8), bar(9, 10, 11, 12));
			}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				void test() {
					foo(bar(1, 2, 3, 4),
							bar(5, 6, 7, 8),
							bar(9, 10, 11, 12));
				}
			}
			"""
	);
}
public void testBug059891_03b() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class X03 {
			void test() {
				foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8), bar(9, 10, 11, 12));
			}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				void test() {
					foo(bar(1, 2, 3, 4),
						bar(5, 6, 7, 8),
						bar(9, 10, 11, 12));
				}
			}
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146175
public void testBug059891_146175() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class FormatterDemo {
		
		    public void fooBar() {
		        SomeOtherClass instanceOfOtherClass = new SomeOtherClass();
		
		        /* The following statement demonstrates the formatter issue */
		        SomeOtherClass.someMethodInInnerClass(
		            instanceOfOtherClass.anotherMethod("Value of paramter 1"),
		            instanceOfOtherClass.anotherMethod("Value of paramter 2"));
		
		    }
		
		    private static class SomeOtherClass {
		        public static void someMethodInInnerClass(
		            String param1,
		            String param2) {
		        }
		        public String anotherMethod(String par) {
		            return par;
		        }
		    }
		}
		""";
	formatSource(source,
		"""
			public class FormatterDemo {
			
				public void fooBar() {
					SomeOtherClass instanceOfOtherClass = new SomeOtherClass();
			
					/* The following statement demonstrates the formatter issue */
					SomeOtherClass.someMethodInInnerClass(
							instanceOfOtherClass.anotherMethod("Value of paramter 1"),
							instanceOfOtherClass.anotherMethod("Value of paramter 2"));
			
				}
			
				private static class SomeOtherClass {
					public static void someMethodInInnerClass(String param1,
							String param2) {
					}
			
					public String anotherMethod(String par) {
						return par;
					}
				}
			}
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=164093
public void testBug059891_164093_01() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "30");
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class Test {
		    int someLongMethodName(int foo,  boolean bar, String yetAnotherArg) {
		        return 0;
		    }
		}
		""";
	formatSource(source,
		"""
			public class Test {
				int someLongMethodName(	int foo,
										boolean bar,
										String yetAnotherArg) {
					return 0;
				}
			}
			"""
	);
}
public void testBug059891_164093_02() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "55");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_SELECTOR_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class X01 {
		    void foo() {
		           someIdentifier(someArg).someMethodName().someMethodName(foo, bar).otherMethod(arg0, arg1);
		    }
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			    void foo() {
			        someIdentifier(someArg).someMethodName()
			                               .someMethodName(foo,
			                                       bar)
			                               .otherMethod(arg0,
			                                       arg1);
			    }
			}
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203588
public void testBug059891_203588() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class Test {
		public void a()
		{
		  if(true)
		  {
		    allocation.add(idx_ta + 1, Double.valueOf(allocation.get(idx_ta).doubleValue() + q));
		  }
		}
		}
		""";
	formatSource(source,
		"""
			public class Test {
				public void a() {
					if (true) {
						allocation.add(idx_ta + 1,
								Double.valueOf(allocation.get(idx_ta).doubleValue() + q));
					}
				}
			}
			"""
	);
}
// wksp1
public void testBug059891_wksp1_01() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X01 {
			private void reportError(String name) throws ParseError {
				throw new ParseError(MessageFormat.format(AntDTDSchemaMessages.getString("NfmParser.Ambiguous"), new String[]{name})); //$NON-NLS-1$
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				private void reportError(String name) throws ParseError {
					throw new ParseError(MessageFormat.format(
							AntDTDSchemaMessages.getString("NfmParser.Ambiguous"), //$NON-NLS-1$
							new String[] { name }));
				}
			}
			"""
	);
}
public void testBug059891_wksp1_02() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X02 {
			private void parseBuildFile(Project project) {
				if (!buildFile.exists()) {
					throw new BuildException(MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Buildfile__{0}_does_not_exist_!_1"), //$NON-NLS-1$
								 new String[]{buildFile.getAbsolutePath()}));
				}
				if (!buildFile.isFile()) {
					throw new BuildException(MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Buildfile__{0}_is_not_a_file_1"), //$NON-NLS-1$
									new String[]{buildFile.getAbsolutePath()}));
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				private void parseBuildFile(Project project) {
					if (!buildFile.exists()) {
						throw new BuildException(MessageFormat.format(
								InternalAntMessages.getString(
										"InternalAntRunner.Buildfile__{0}_does_not_exist_!_1"), //$NON-NLS-1$
								new String[] { buildFile.getAbsolutePath() }));
					}
					if (!buildFile.isFile()) {
						throw new BuildException(MessageFormat.format(
								InternalAntMessages.getString(
										"InternalAntRunner.Buildfile__{0}_is_not_a_file_1"), //$NON-NLS-1$
								new String[] { buildFile.getAbsolutePath() }));
					}
				}
			}
			"""
	);
}
public void testBug059891_wksp1_03() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X03 {
		
			protected void foo() {
				printTargets(project, subNames, null, InternalAntMessages.getString("InternalAntRunner.Subtargets__5"), 0); //$NON-NLS-1$
			}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
			
				protected void foo() {
					printTargets(project, subNames, null, InternalAntMessages
							.getString("InternalAntRunner.Subtargets__5"), 0); //$NON-NLS-1$
				}
			}
			"""
	);
}
public void testBug059891_wksp1_04() throws JavaModelException {
	String source =
		"""
		public class X04 {
			void foo() {
				if (AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {
					synchronizeOutlinePage(node, true);
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X04 {
				void foo() {
					if (AntUIPlugin.getDefault().getPreferenceStore()
							.getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {
						synchronizeOutlinePage(node, true);
					}
				}
			}
			"""
	);
}
public void testBug059891_wksp1_05() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X05 {
		void foo() {
				if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {
				}
		}
		}
		""";
	formatSource(source,
		"""
			public class X05 {
				void foo() {
					if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(
							AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {
					}
				}
			}
			"""
	);
}
public void testBug059891_wksp1_06() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X06 {
			public void launch() {
				try {
					if ((javaProject == null) || !javaProject.exists()) {
						abort(PDEPlugin________.getResourceString("JUnitLaunchConfig_____"), null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
					}
				} catch (CoreException e) {
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X06 {
				public void launch() {
					try {
						if ((javaProject == null) || !javaProject.exists()) {
							abort(PDEPlugin________
									.getResourceString("JUnitLaunchConfig_____"), null,
									IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
						}
					} catch (CoreException e) {
					}
				}
			}
			"""
	);
}
public void testBug059891_wksp1_07() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X07 {
			void foo() {
				if (true) {
					configureAntObject(result, element, task, task.getTaskName(), InternalCoreAntMessages.getString("AntCorePreferences.No_library_for_task")); //$NON-NLS-1$
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X07 {
				void foo() {
					if (true) {
						configureAntObject(result, element, task, task.getTaskName(),
								InternalCoreAntMessages.getString(
										"AntCorePreferences.No_library_for_task")); //$NON-NLS-1$
					}
				}
			}
			"""
	);
}
public void testBug059891_wksp1_08() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X08 {
			public void foo() {
				if (true) {
					IStatus status= new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_RUNNING_BUILD, MessageFormat.format(InternalCoreAntMessages.getString("AntRunner.Already_in_progess"), new String[]{buildFileLocation}), null); //$NON-NLS-1$
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X08 {
				public void foo() {
					if (true) {
						IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE,
								AntCorePlugin.ERROR_RUNNING_BUILD,
								MessageFormat.format(
										InternalCoreAntMessages
												.getString("AntRunner.Already_in_progess"), //$NON-NLS-1$
										new String[] { buildFileLocation }),
								null);
					}
				}
			}
			"""
	);
}
public void testBug059891_wksp1_09() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X09 {
			void foo() {
				if (true) {
					String secondFileName = secondDirectoryAbsolutePath + File.separator + currentFile.substring(firstDirectoryAbsolutePath.length() + 1);
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X09 {
				void foo() {
					if (true) {
						String secondFileName = secondDirectoryAbsolutePath + File.separator
								+ currentFile
										.substring(firstDirectoryAbsolutePath.length() + 1);
					}
				}
			}
			"""
	);
}
public void testBug059891_wksp1_10() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X10 {
			void foo() {
				if (true) {
					if (true) {
						throw new BuildException(InternalAntMessages.getString("InternalAntRunner.Could_not_load_the_version_information._10")); //$NON-NLS-1$
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X10 {
				void foo() {
					if (true) {
						if (true) {
							throw new BuildException(InternalAntMessages.getString(
									"InternalAntRunner.Could_not_load_the_version_information._10")); //$NON-NLS-1$
						}
					}
				}
			}
			"""
	);
}
public void testBug059891_wksp1_11() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X11 {
			private void antFileNotFound() {
				reportError(AntLaunchConfigurationMessages.getString("AntLaunchShortcut.Unable"), null); //$NON-NLS-1$\t
			}
		}
		""";
	formatSource(source,
		"""
			public class X11 {
				private void antFileNotFound() {
					reportError(AntLaunchConfigurationMessages
							.getString("AntLaunchShortcut.Unable"), null); //$NON-NLS-1$
				}
			}
			"""
	);
}
public void testBug059891_wksp1_12() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class X12 {
			void foo() {
		        if (this.fTests.size() == 0) {
		            this.addTest(TestSuite
		                    .warning("No tests found in " + theClass.getName())); //$NON-NLS-1$
		        }
			}
		}
		""";
	formatSource(source,
		"""
			public class X12 {
				void foo() {
					if (this.fTests.size() == 0) {
						this.addTest(TestSuite
								.warning("No tests found in " + theClass.getName())); //$NON-NLS-1$
					}
				}
			}
			"""
	);
}

/**
 * bug 198074: [formatter] the code formatter doesn't respect my new lines
 * test Ensure that the formatter keep line breaks wrapping set by users in the code
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=198074"
 */
public void testBug198074() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
		
			void foo() {
		String x = "select x "
		         + "from y "
		         + "where z=a";
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					String x = "select x "
							+ "from y "
							+ "where z=a";
				}
			}
			"""
	);
}
public void testBug198074b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		public class Test {
		
			void foo() {
		String x = "select x "
		         + "from y "
		         + "where z=a";
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
			    void foo() {
			        String x = "select x "
			                + "from y "
			                + "where z=a";
			    }
			}
			"""
	);
}
// another test case put in bug's comment 1
public void testBug198074_c1() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
		
			String foo(boolean enabled) {
		if (enabled)
		{
		   // we need x
		   // we need a select
		   return "select x "
		   + "from X";}
			return null;}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				String foo(boolean enabled) {
					if (enabled) {
						// we need x
						// we need a select
						return "select x "
								+ "from X";
					}
					return null;
				}
			}
			"""
	);
}
public void testBug198074_c1b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		public class Test {
		
			String foo(boolean enabled) {
		if (enabled)
		{
		   // we need x
		   // we need a select
		   return "select x "
		        + "from X";}
			return null;}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
			    String foo(boolean enabled) {
			        if (enabled) {
			            // we need x
			            // we need a select
			            return "select x "
			                    + "from X";
			        }
			        return null;
			    }
			}
			"""
	);
}
// another test case put in bug's comment 3
public void testBug198074_c3() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
		
		public String toString() {
		        return "YAD01: "
		        + " nommbr=\'"+getName()+"\'"
		        + " nomgrp=\'"+getService().getArgtbl()+"\'"
		        + " typmbr=\'"+getMemberType().getArgument()+"\'"
		        + " srcpat=\'"+getPhysicalPath()+"\'"
		        + " nommdl=\'"+getModel()+"\'"
		        ;
		}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				public String toString() {
					return "YAD01: "
							+ " nommbr=\'" + getName() + "\'"
							+ " nomgrp=\'" + getService().getArgtbl() + "\'"
							+ " typmbr=\'" + getMemberType().getArgument() + "\'"
							+ " srcpat=\'" + getPhysicalPath() + "\'"
							+ " nommdl=\'" + getModel() + "\'";
				}
			}
			"""
	);
}
public void testBug198074_c3b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		public class Test {
		
		public String toString() {
		        return "YAD01: "
		                + " nommbr=\'"+getName()+"\'"
		                + " nomgrp=\'"+getService().getArgtbl()+"\'"
		                + " typmbr=\'"+getMemberType().getArgument()+"\'"
		                + " srcpat=\'"+getPhysicalPath()+"\'"
		                + " nommdl=\'"+getModel()+"\'"
		        ;
		}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
			    public String toString() {
			        return "YAD01: "
			                + " nommbr=\'" + getName() + "\'"
			                + " nomgrp=\'" + getService().getArgtbl() + "\'"
			                + " typmbr=\'" + getMemberType().getArgument() + "\'"
			                + " srcpat=\'" + getPhysicalPath() + "\'"
			                + " nommdl=\'" + getModel() + "\'";
			    }
			}
			"""
	);
}
public void testBug198074_comments() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"""
		public class Test {
		
			void foo() {
		String x = "select x "
		         + "from y "
		         + "where z=a";
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					String x = "select x " + "from y " + "where z=a";
				}
			}
			"""
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=201022
// see also bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=287462
public void testBug198074_dup201022() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
		
			void foo() {
		    String sQuery =
		        "select * " +
		        "from person p, address a " +
		        "where p.person_id = a.person_id " +
		        "and p.person_id = ?";
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					String sQuery = "select * " +
							"from person p, address a " +
							"where p.person_id = a.person_id " +
							"and p.person_id = ?";
				}
			}
			"""
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=213700
public void testBug198074_dup213700() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
		
			void foo() {
				int a=0, b=0, c=0, d=0, e=0, f=0, g=0, h=0, i=0;
		if( (a == b && b == c) &&
		    (d == e) &&
		    (f == g && h == i)\s
		    ){
		}
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0, i = 0;
					if ((a == b && b == c) &&
							(d == e) &&
							(f == g && h == i)) {
					}
				}
			}
			"""
	);
}

/**
 * bug 199265: [formatter] 3.3 Code Formatter mis-places commented-out import statements
 * test Ensure that the formatter keep commented import declarations on their lines
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=199265"
 */
public void testBug199265a() throws JavaModelException {
	String source =
		"""
		import java.util.List;
		//import java.util.HashMap;
		import java.util.Set;
		
		public class X01 {
		}
		""";
	formatSource(source);
}
public void testBug199265b() throws JavaModelException {
	String source =
		"""
		import java.util.List;
		import java.util.Set;
		//import java.util.HashMap;
		
		public class X02 {
		}
		""";
	formatSource(source,
		"""
			import java.util.List;
			import java.util.Set;
			//import java.util.HashMap;
			
			public class X02 {
			}
			"""
	);
}
public void testBug199265c1() throws JavaModelException {
	String source =
		"""
		import java.util.List;
		//            CU         snippet
		public class X03 {
			List field;
		}
		""";
	formatSource(source,
		"""
			import java.util.List;
			
			//            CU         snippet
			public class X03 {
				List field;
			}
			"""
	);
}
public void testBug199265c2() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.comment_format_header = true;
	String source =
		"""
		import java.util.List;
		//            CU         snippet
		public class X03 {
			List field;
		}
		""";
	formatSource(source,
		"""
			import java.util.List;
			
			// CU snippet
			public class X03 {
				List field;
			}
			"""
	);
}
public void testBug199265c3() throws JavaModelException {
	String source =
		"""
		import java.util.List;
		
		// line comment
		public class X03 {
			List field;
		}
		""";
	formatSource(source);
}
public void testBug199265d1() throws JavaModelException {
	String source =
		"""
		import java.util.Set; // trailing comment
		// line comment
		import java.util.Map; // trailing comment
		// line comment
		public class X04 {
		
		}
		""";
	formatSource(source,
		"""
			import java.util.Set; // trailing comment
			// line comment
			import java.util.Map; // trailing comment
			// line comment
			
			public class X04 {
			
			}
			"""
	);
}
public void testBug199265d2() throws JavaModelException {
	String source =
		"""
		import java.util.Set; // trailing comment
		// line comment
		import java.util.Map; // trailing comment
		// line comment
		
		public class X04 {
		
		}
		""";
	formatSource(source);
}
public void testBug199265d3() throws JavaModelException {
	String source =
		"""
		import java.util.Set; // trailing comment
			// line comment
		import java.util.Map; // trailing comment
			// line comment
		public class X04 {
		
		}
		""";
	formatSource(source,
		"""
			import java.util.Set; // trailing comment
			// line comment
			import java.util.Map; // trailing comment
			// line comment
			
			public class X04 {
			
			}
			"""
	);
}
public void testBug199265_wksp1a() throws JavaModelException {
	String source =
		"""
		package wksp1;
		
		import java.util.*;
		import java.util.List; // line comment
		
		/**
		 * Javadoc comment
		 */
		public class X01 {
		
		}
		""";
	formatSource(source);
}
public void testBug199265_wksp1b() throws JavaModelException {
	String source =
		"""
		package wksp1;
		
		import java.util.Map;
		
		//==========================
		// Line comment
		//==========================
		
		/**
		 * Javadoc comment
		 */
		public class X02 {
		
		}
		""";
	formatSource(source);
}
public void testBug199265_wksp2a() throws JavaModelException {
	String source =
		"""
		package wksp2;
		
		import java.util.Map;
		
		//#if defined(TEST)
		import java.util.Vector;
		//#else
		//##import java.util.Set;
		//#endif
		
		public class X01 {
		
		}
		""";
	formatSource(source);
}
public void testBug199265_wksp3a() throws JavaModelException {
	String source =
		"""
		package wksp3;
		
		import java.util.Set;	// comment 1
		import java.util.Map;	// comment 2
		import java.util.List;	// comment 3
		
		public class X01 {
		
		}
		""";
	formatSource(source,
		"""
			package wksp3;
			
			import java.util.Set; // comment 1
			import java.util.Map; // comment 2
			import java.util.List; // comment 3
			
			public class X01 {
			
			}
			"""
	);
}

/**
 * bug 208541: [formatter] Formatter does not format whole region/selection
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=208541"
 */
public void testBug208541() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class MyTest {
		
		    public void testname() throws Exception {
		        int i = 5, j = 6, k = 7;
		        if (new String().length() != 0\s
		              &&  (i < j && j < k)) {
		
		        }
		    }
		}
		""";
	formatSource(source,
		"""
			public class MyTest {
			
				public void testname() throws Exception {
					int i = 5, j = 6, k = 7;
					if (new String().length() != 0
							&& (i < j && j < k)) {
			
					}
				}
			}
			"""
	);
}

/**
 * bug 203588: [formatter] Qualified invocation + binary expressions excessive wrap
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=203588"
 */
public void testBug203588() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class Test {
		void foo() {
			while (true) {
				if (patternChar
					!= (isCaseSensitive
						? name[iName]
						: Character.toLowerCase(name[iName]))
					&& patternChar != \'?\') {
					return;
				}
			}
		}
		}
		""";
	formatSource(source,
		"""
			public class Test {
				void foo() {
					while (true) {
						if (patternChar != (isCaseSensitive ? name[iName]
								: Character.toLowerCase(name[iName]))
								&& patternChar != \'?\') {
							return;
						}
					}
				}
			}
			"""
	);
}

/**
 * bug 252556: [formatter] Spaces removed before formatted region of a compilation unit.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=252556"
 */
public void testBug252556() {
	String source =
		"""
		package a;
		
		public class Test {
		
			private int field;
		\t
			[#/**
			 * fds\s
			 */#]
			public void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			package a;
			
			public class Test {
			
				private int field;
			\t
				/**
				 * fds
				 */
				public void foo() {
				}
			}
			"""
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556a() {
	String source =
		"""
		public class Test {
		
		int foo() {[#
		return 0;
		#]}
		void bar(){}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
			int foo() {
				return 0;
			}
			void bar(){}
			}
			"""
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556b() {
	String source =
		"""
		public class Test {
		
		int [#foo() {
		return 0;
		#]}
		void bar(){}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
			int foo() {
				return 0;
			}
			void bar(){}
			}
			"""
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556c() {
	String source =
		"""
		public class Test {
		
		[#int foo() {
		return 0;
		#]}
		void bar(){}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				int foo() {
					return 0;
				}
			void bar(){}
			}
			"""
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556d() {
	String source =
		"""
		public class Test {
		
		[#int foo() {
		return 0;
		}#]
		void bar(){}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				int foo() {
					return 0;
				}
			void bar(){}
			}
			"""
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556e() {
	String source =
		"""
		public class Test {
		
		[#int foo() {
		return 0;
		}
		#]void bar(){}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				int foo() {
					return 0;
				}
			
				void bar(){}
			}
			"""
	);
}
// see org.eclipse.jdt.ui.tests.core.CodeFormatterUtilTest.testFormatSubstring()
public void testBug252556f() {
	String source =
		"""
		package test1;
		
		import java.util.Vector;
		
		public class A {
		    public void foo() {
		    [#Runnable runnable= new Runnable() {};#]
		    runnable.toString();
		    }
		}
		""";
	formatSource(source,
		"""
			package test1;
			
			import java.util.Vector;
			
			public class A {
			    public void foo() {
			    	Runnable runnable = new Runnable() {
					};
			    runnable.toString();
			    }
			}
			"""
	);
}
// Adding a test case impacted by the fix for bug 252556 got from massive tests
public void testBug252556_wksp3a() {
	String source =
		"""
		package wksp3;
		
		/**
		 * <pre>import java.net.*;
		 * import org.xml.sax.*;
		 * </pre>
		 */
		public class X01 {
		
		}
		""";
	formatSource(source,
		"""
			package wksp3;
			
			/**
			 * <pre>
			 * import java.net.*;
			 * import org.xml.sax.*;
			 * </pre>
			 */
			public class X01 {
			
			}
			"""
	);
}

/**
 * bug 281655: [formatter] "Never join lines" does not work for annotations.
 * test Verify that "Never join lines" now works for annotations and also that
 * 		element-value pairs are well wrapped using the new formatter option
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=281655"
 */
public void testBug281655() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_SPLIT;
	String source =
		"""
		@MessageDriven(mappedName = "filiality/SchedulerMQService",\s
		        activationConfig = {\s
		            @ActivationConfigProperty(propertyName = "cronTrigger",
		propertyValue = "0/10 * * * * ?")\s
		        })
		@RunAs("admin")
		@ResourceAdapter("quartz-ra.rar")
		@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MessageDriven(mappedName = "filiality/SchedulerMQService",
					activationConfig = {
							@ActivationConfigProperty(propertyName = "cronTrigger",
									propertyValue = "0/10 * * * * ?")
					})
			@RunAs("admin")
			@ResourceAdapter("quartz-ra.rar")
			@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
			public class X {
			}
			"""
	);
}
public void testBug281655a() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NO_ALIGNMENT;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_NO_ALIGNMENT;
	String source =
		"""
		@MessageDriven(mappedName = "filiality/SchedulerMQService",\s
		        activationConfig = {\s
		            @ActivationConfigProperty(propertyName = "cronTrigger",
		propertyValue = "0/10 * * * * ?")\s
		        })
		@RunAs("admin")
		@ResourceAdapter("quartz-ra.rar")
		@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MessageDriven(mappedName = "filiality/SchedulerMQService", activationConfig = { @ActivationConfigProperty(propertyName = "cronTrigger", propertyValue = "0/10 * * * * ?") })
			@RunAs("admin")
			@ResourceAdapter("quartz-ra.rar")
			@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
			public class X {
			}
			"""
	);
}
public void testBug281655b() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"""
		@MessageDriven(mappedName = "filiality/SchedulerMQService",\s
		        activationConfig = {\s
		            @ActivationConfigProperty(propertyName = "cronTrigger",
		propertyValue = "0/10 * * * * ?")\s
		        })
		@RunAs("admin")
		@ResourceAdapter("quartz-ra.rar")
		@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MessageDriven(mappedName = "filiality/SchedulerMQService",
					activationConfig = {
							@ActivationConfigProperty(propertyName = "cronTrigger",
									propertyValue = "0/10 * * * * ?") })
			@RunAs("admin")
			@ResourceAdapter("quartz-ra.rar")
			@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
			public class X {
			}
			"""
	);
}
public void testBug281655c() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	setPageWidth80();
	String source =
		"""
		@MessageDriven(mappedName = "filiality/SchedulerMQService",\s
		        activationConfig = {\s
		            @ActivationConfigProperty(propertyName = "cronTrigger",
		propertyValue = "0/10 * * * * ?")\s
		        })
		@RunAs("admin")
		@ResourceAdapter("quartz-ra.rar")
		@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MessageDriven(
					mappedName = "filiality/SchedulerMQService",
					activationConfig = { @ActivationConfigProperty(
							propertyName = "cronTrigger",
							propertyValue = "0/10 * * * * ?") })
			@RunAs("admin")
			@ResourceAdapter("quartz-ra.rar")
			@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
			public class X {
			}
			"""
	);
}
public void testBug281655d() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"""
		@MessageDriven(mappedName = "filiality/SchedulerMQService",\s
		        activationConfig = {\s
		            @ActivationConfigProperty(propertyName = "cronTrigger",
		propertyValue = "0/10 * * * * ?")\s
		        })
		@RunAs("admin")
		@ResourceAdapter("quartz-ra.rar")
		@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MessageDriven(
					mappedName = "filiality/SchedulerMQService",
					activationConfig = { @ActivationConfigProperty(
							propertyName = "cronTrigger",
							propertyValue = "0/10 * * * * ?") })
			@RunAs("admin")
			@ResourceAdapter("quartz-ra.rar")
			@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
			public class X {
			}
			"""
	);
}
public void testBug281655e() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NEXT_SHIFTED_SPLIT;
	setPageWidth80();
	String source =
		"""
		@MessageDriven(mappedName = "filiality/SchedulerMQService",\s
		        activationConfig = {\s
		            @ActivationConfigProperty(propertyName = "cronTrigger",
		propertyValue = "0/10 * * * * ?")\s
		        })
		@RunAs("admin")
		@ResourceAdapter("quartz-ra.rar")
		@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MessageDriven(
					mappedName = "filiality/SchedulerMQService",
						activationConfig = { @ActivationConfigProperty(
								propertyName = "cronTrigger",
									propertyValue = "0/10 * * * * ?") })
			@RunAs("admin")
			@ResourceAdapter("quartz-ra.rar")
			@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
			public class X {
			}
			"""
	);
}
public void testBug281655f() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NEXT_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"""
		@MessageDriven(mappedName = "filiality/SchedulerMQService",\s
		        activationConfig = {\s
		            @ActivationConfigProperty(propertyName = "cronTrigger",
		propertyValue = "0/10 * * * * ?")\s
		        })
		@RunAs("admin")
		@ResourceAdapter("quartz-ra.rar")
		@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MessageDriven(mappedName = "filiality/SchedulerMQService",
					activationConfig = {
							@ActivationConfigProperty(propertyName = "cronTrigger",
									propertyValue = "0/10 * * * * ?") })
			@RunAs("admin")
			@ResourceAdapter("quartz-ra.rar")
			@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
			public class X {
			}
			"""
	);
}

/**
 * bug 282030: [formatter] Java annotation formatting
 * test Verify that element-value pairs are well wrapped using the new formatter option
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=282030"
 */
public void testBug282030() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"""
		@DeclareParents(value =
		"com.apress.springrecipes.calculator.ArithmeticCalculatorImpl", defaultImpl =
		MaxCalculatorImpl.class)\s
		public class X {
		}
		""";
	formatSource(source,
		"""
			@DeclareParents(
					value = "com.apress.springrecipes.calculator.ArithmeticCalculatorImpl",
					defaultImpl = MaxCalculatorImpl.class)
			public class X {
			}
			"""
	);
}
public void testBug282030a() throws JavaModelException {
	String source =
		"""
		@MyAnnot(value1 = "this is an example", value2 = "of an annotation", value3 = "with several arguments", value4 = "which may need to be wrapped")
		public class Test {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot(value1 = "this is an example", value2 = "of an annotation", value3 = "with several arguments", value4 = "which may need to be wrapped")
			public class Test {
			}
			"""
	);
}
public void testBug282030b() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"""
		@MyAnnot(value1 = "this is an example", value2 = "of an annotation", value3 = "with several arguments", value4 = "which may need to be wrapped")
		public class Test {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot(value1 = "this is an example", value2 = "of an annotation",
					value3 = "with several arguments",
					value4 = "which may need to be wrapped")
			public class Test {
			}
			"""
	);
}
public void testBug282030c() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	setPageWidth80();
	String source =
		"""
		@MyAnnot(value1 = "this is an example", value2 = "of an annotation", value3 = "with several arguments", value4 = "which may need to be wrapped")
		public class Test {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot(
					value1 = "this is an example", value2 = "of an annotation",
					value3 = "with several arguments",
					value4 = "which may need to be wrapped")
			public class Test {
			}
			"""
	);
}
public void testBug282030d() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT;
	String source =
		"""
		@MyAnnot(value1 = "this is an example", value2 = "of an annotation", value3 = "with several arguments", value4 = "which may need to be wrapped")
		public class Test {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot(
					value1 = "this is an example",
					value2 = "of an annotation",
					value3 = "with several arguments",
					value4 = "which may need to be wrapped")
			public class Test {
			}
			"""
	);
}
public void testBug282030e() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NEXT_SHIFTED_SPLIT;
	String source =
		"""
		@MyAnnot(value1 = "this is an example", value2 = "of an annotation", value3 = "with several arguments", value4 = "which may need to be wrapped")
		public class Test {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot(
					value1 = "this is an example",
						value2 = "of an annotation",
						value3 = "with several arguments",
						value4 = "which may need to be wrapped")
			public class Test {
			}
			"""
	);
}
public void testBug282030f() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NEXT_PER_LINE_SPLIT;
	String source =
		"""
		@MyAnnot(value1 = "this is an example", value2 = "of an annotation", value3 = "with several arguments", value4 = "which may need to be wrapped")
		public class Test {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot(value1 = "this is an example",
					value2 = "of an annotation",
					value3 = "with several arguments",
					value4 = "which may need to be wrapped")
			public class Test {
			}
			"""
	);
}
public void testBug282030g1() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT;
	String source =
		"""
		@MyAnnot1(member1 = "sample1", member2 = "sample2")
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot1(member1 = "sample1", member2 = "sample2")
			public class X {
			}
			"""
	);
}
public void testBug282030g2() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_FORCE;
	String source =
		"""
		@MyAnnot1(member1 = "sample1", member2 = "sample2")
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot1(
					member1 = "sample1",
					member2 = "sample2")
			public class X {
			}
			"""
	);
}
public void testBug282030h1() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"""
		@MyAnnot1(name = "sample1",\s
		                value = {\s
		                        @MyAnnot2(name = "sample2",
		value = "demo")\s
		                })
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot1(
					name = "sample1",
					value = { @MyAnnot2(name = "sample2", value = "demo") })
			public class X {
			}
			"""
	);
}
public void testBug282030h2() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_FORCE;
	String source =
		"""
		@MyAnnot1(name = "sample1",\s
		                value = {\s
		                        @MyAnnot2(name = "sample2",
		value = "demo")\s
		                })
		public class X {
		}
		""";
	formatSource(source,
		"""
			@MyAnnot1(
					name = "sample1",
					value = { @MyAnnot2(
							name = "sample2",
							value = "demo") })
			public class X {
			}
			"""
	);
}

/**
 * bug 283467: [formatter] wrong indentation with 'Never join lines' selected
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=283467"
 */
public void testBug283467() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class TestFormatter {
		
		        public static void main(String[] args) {
		                int variable = TestFormatter.doInCallback(new Runnable() {
		                        public void run() {
		                                // Some comments or code here
		                        }
		                });
		                System.out.println(variable);
		        }
		
		        public static int doInCallback(Runnable r) {
		                return 0;
		        }
		}
		""";
	formatSource(source,
		"""
			public class TestFormatter {
			
				public static void main(String[] args) {
					int variable = TestFormatter.doInCallback(new Runnable() {
						public void run() {
							// Some comments or code here
						}
					});
					System.out.println(variable);
				}
			
				public static int doInCallback(Runnable r) {
					return 0;
				}
			}
			"""
	);
}

/**
 * bug 284789: [formatter] Does not line-break method declaration exception with parameters
 * test Verify that the new preference to split method declaration works properly
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=284789"
 */
public void testBug284789() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"""
		public class Test {
		public synchronized List<FantasticallyWonderfulContainer<FantasticallyWonderfulClass>> getMeTheFantasticContainer() {
			return null;
		}
		}
		""";
	formatSource(source,
		"""
			public class Test {
				public synchronized
						List<FantasticallyWonderfulContainer<FantasticallyWonderfulClass>>
						getMeTheFantasticContainer() {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_01a() throws JavaModelException {
	// default is no wrapping for method declaration
	String source =
		"""
		class X01 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name() {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X01 {
				public final synchronized java.lang.String a_method_which_have_a_very_long_name() {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_01b() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X01 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name() {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X01 {
				public final synchronized java.lang.String
						a_method_which_have_a_very_long_name() {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_01c() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X01 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name() {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X01 {
				public final synchronized
						java.lang.String a_method_which_have_a_very_long_name() {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_01d() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_ONE_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X01 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name() {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X01 {
				public final synchronized
						java.lang.String
						a_method_which_have_a_very_long_name() {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_01e() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_NEXT_SHIFTED_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X01 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name() {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X01 {
				public final synchronized
						java.lang.String
							a_method_which_have_a_very_long_name() {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_01f() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_NEXT_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X01 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name() {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X01 {
				public final synchronized java.lang.String
						a_method_which_have_a_very_long_name() {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_02a() throws JavaModelException {
	// default is no wrapping for method declaration
	setPageWidth80();
	String source =
		"""
		class X02 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X02 {
				public final synchronized java.lang.String a_method_which_have_a_very_long_name(
						String first, String second, String third) {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_02b() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X02 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X02 {
				public final synchronized java.lang.String
						a_method_which_have_a_very_long_name(String first, String second,
								String third) {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_02c() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X02 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X02 {
				public final synchronized
						java.lang.String a_method_which_have_a_very_long_name(
								String first, String second, String third) {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_02d() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_ONE_PER_LINE_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_ONE_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X02 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X02 {
				public final synchronized
						java.lang.String
						a_method_which_have_a_very_long_name(
								String first,
								String second,
								String third) {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_02e() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_NEXT_SHIFTED_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_NEXT_SHIFTED_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X02 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X02 {
				public final synchronized
						java.lang.String
							a_method_which_have_a_very_long_name(
									String first,
										String second,
										String third) {
					return null;
				}
			}
			"""
	);
}
public void testBug284789_02f() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_NEXT_PER_LINE_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_NEXT_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"""
		class X02 {
			public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {
				return null;
			}
		}
		""";
	formatSource(source,
		"""
			class X02 {
				public final synchronized java.lang.String
						a_method_which_have_a_very_long_name(String first,
								String second,
								String third) {
					return null;
				}
			}
			"""
	);
}

/**
 * bug 285565: [formatter] wrong indentation with 'Never join lines' selected
 * test Test to make sure that use either formatter or {@link IndentManipulation}
 * 	API methods an indentation set to zero does not thrown any exception.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=285565"
 */
public void testBug285565a() {
	try {
		assertEquals("Should be 0", 0, IndentManipulation.measureIndentInSpaces("", 0));
		assertEquals("Should be 0", 0, IndentManipulation.measureIndentInSpaces("\t", 0));
		assertEquals("Should be 1", 1, IndentManipulation.measureIndentInSpaces("\t ", 0));
		assertEquals("Should be blank", "\t", IndentManipulation.extractIndentString("\tabc", 0, 0));
	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}
public void testBug285565b() {
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.tab_size = 0;
	String source = """
		public class test {
		    public static void main(String[] args) {
		        int B= 12;
		        int C= B - 1;
		        int K= 99;
		        int f1= K - 1 - C;
		        int f2= K - C - C - C;
		    }
		}
		""";
	formatSource(source, """
		public class test {
		public static void main(String[] args) {
		int B = 12;
		int C = B - 1;
		int K = 99;
		int f1 = K - 1 - C;
		int f2 = K - C - C - C;
		}
		}
		""");
}
public void testBug285565c() {
	String result = """
		int B = 12;
		 int C = B - 1;
		 int K = 99;
		 int f1 = K - 1 - C;
		 int f2 = K - C - C - C;""" ;

	try {
		assertEquals("Should be as shown", result, IndentManipulation.changeIndent("""
			int B = 12;
			int C = B - 1;
			int K = 99;
			int f1 = K - 1 - C;
			int f2 = K - C - C - C;""" ,0,0,0, " ","\n"));

	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}
public void testBug285565d() {
	String result = """
		int B = 12;
		int C = B - 1;
		int K = 99;
		int f1 = K - 1 - C;
		int f2 = K - C - C - C;""" ;

	try {
		assertEquals("Should be as shown", result, IndentManipulation.trimIndent("""
			int B = 12;
			int C = B - 1;
			int K = 99;
			int f1 = K - 1 - C;
			int f2 = K - C - C - C;""" , 0, 0, 0));

	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}
public void testBug285565e() {
	try {
		IndentManipulation.getChangeIndentEdits("""
			int B = 12;
			int C = B - 1;
			int K = 99;
			int f1 = K - 1 - C;
			int f2 = K - C - C - C;""", 0, 0, 0, " ");

	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}

/**
 * bug 286601: [formatter] Code formatter formats anonymous inner classes wrongly when 'Never join lines' is on
 * test Test to make sure that indentation is correct in anonymous inner class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286601"
 */
public void testBug286601() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test
		{
		    public void aMethod()
		    {
		        Object anObject = new Object()
		        {
		            boolean aVariable;
		        };
		    }
		}
		""";
	formatSource(source,
		"""
			public class Test {
				public void aMethod() {
					Object anObject = new Object() {
						boolean aVariable;
					};
				}
			}
			"""
	);
}
public void testBug286601b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
		
			void foo() {
		long x1 = 100000000
		        + 200000000
		        + 300000000;
		long x2 = 100000000
		        + 200000000
		        + 300000000
		        + 400000000;
		long x3 = 100000000
		        + 200000000
		        + 300000000
		        + 400000000
		        + 500000000;
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					long x1 = 100000000
							+ 200000000
							+ 300000000;
					long x2 = 100000000
							+ 200000000
							+ 300000000
							+ 400000000;
					long x3 = 100000000
							+ 200000000
							+ 300000000
							+ 400000000
							+ 500000000;
				}
			}
			"""
	);
}
public void testBug286601c() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.brace_position_for_anonymous_type_declaration= DefaultCodeFormatterConstants.NEXT_LINE;
	String source =
		"""
		public class Test
		{
		    public void aMethod()
		    {
		        Object anObject = new Object()
		        {
		            boolean aVariable;
		            void foo()
		            {
		            }
		        };
		    }
		}
		""";
	formatSource(source,
		"""
			public class Test {
				public void aMethod() {
					Object anObject = new Object()
					{
						boolean aVariable;
			
						void foo() {
						}
					};
				}
			}
			"""
	);
}
public void testBug286601d() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.brace_position_for_anonymous_type_declaration= DefaultCodeFormatterConstants.NEXT_LINE;
	String source =
		"""
		public class Test
		{
		    public void aMethod()
		    {
		        Object anObject = new Object() /* comment */
		        {
		            boolean aVariable;
		            void foo() /* comment */\s
		            {
		            }
		        };
		    }
		}
		""";
	formatSource(source,
		"""
			public class Test {
				public void aMethod() {
					Object anObject = new Object() /* comment */
					{
						boolean aVariable;
			
						void foo() /* comment */
						{
						}
					};
				}
			}
			"""
	);
}
public void testBug286601_massive_01() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"""
		package massive;
		public class X01 {
		    public void build(String href) {
		        // set the href on the related topic
		        if (href == null)
		            setHref(""); //$NON-NLS-1$
		        else {
		            if (!href.equals("") // no empty link //$NON-NLS-1$
		                    && !href.startsWith("/") // no help url //$NON-NLS-1$
		                    && href.indexOf(\':\') == -1) // no other protocols
		            {
		                setHref("/test/" + href); //$NON-NLS-1$ //$NON-NLS-2$
		            }
		        }
		    }
		
		    private void setHref(String string)
		    {
		       \s
		    }
		}
		""";
	formatSource(source,
		"""
			package massive;
			
			public class X01
			{
				public void build(String href)
				{
					// set the href on the related topic
					if (href == null)
						setHref(""); //$NON-NLS-1$
					else
					{
						if (!href.equals("") // no empty link //$NON-NLS-1$
								&& !href.startsWith("/") // no help url //$NON-NLS-1$
								&& href.indexOf(\':\') == -1) // no other protocols
						{
							setHref("/test/" + href); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			
				private void setHref(String string)
				{
			
				}
			}
			"""
	);
}
public void testBug286601_massive_02() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"""
		package massive;
		
		public class X02
		{
		   \s
		    private AntModel getAntModel(final File buildFile) {
		        AntModel model= new AntModel(XMLCore.getDefault(), doc, null, new LocationProvider(null) {
		            /* (non-Javadoc)
		             * @see org.eclipse.ant.internal.ui.editor.outline.ILocationProvider#getLocation()
		             */
		            public IPath getLocation() {
		                return new Path(buildFile.getAbsolutePath());
		            }
		        });
		        model.reconcile(null);
		        return model;
		    }
		}
		""";
	formatSource(source,
		"""
			package massive;
			
			public class X02
			{
			
				private AntModel getAntModel(final File buildFile)
				{
					AntModel model = new AntModel(XMLCore.getDefault(), doc, null,
							new LocationProvider(null)
							{
								/*
								 * (non-Javadoc)
								 *\s
								 * @see org.eclipse.ant.internal.ui.editor.outline.
								 * ILocationProvider#getLocation()
								 */
								public IPath getLocation()
								{
									return new Path(buildFile.getAbsolutePath());
								}
							});
					model.reconcile(null);
					return model;
				}
			}
			"""
	);
}
public void testBug286601_massive_03() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package massive;
		
		public class X03
		{
		
		    public void foo() throws NullPointerException {
		
		        Object body = new Object() {
		            public void run(StringBuffer monitor) throws IllegalArgumentException {
		                IResourceVisitor visitor = new IResourceVisitor() {
		                    public boolean visit(String resource) throws IllegalArgumentException {
		                        return true;
		                    }
		                };
		            }
		        };
		    }
		
		}
		interface IResourceVisitor {
		}
		""";
	formatSource(source,
		"""
			package massive;
			
			public class X03 {
			
				public void foo() throws NullPointerException {
			
					Object body = new Object() {
						public void run(StringBuffer monitor)
								throws IllegalArgumentException {
							IResourceVisitor visitor = new IResourceVisitor() {
								public boolean visit(String resource)
										throws IllegalArgumentException {
									return true;
								}
							};
						}
					};
				}
			
			}
			
			interface IResourceVisitor {
			}
			"""
	);
}
public void testBug286601_wksp_03b() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"""
		package massive;
		
		public class X03
		{
		
		    public void foo() throws NullPointerException {
		
		        Object body = new Object() {
		            public void run(StringBuffer monitor) throws IllegalArgumentException {
		                IResourceVisitor visitor = new IResourceVisitor() {
		                    public boolean visit(String resource) throws IllegalArgumentException {
		                        return true;
		                    }
		                };
		            }
		        };
		    }
		
		}
		interface IResourceVisitor {
		}
		""";
	formatSource(source,
		"""
			package massive;
			
			public class X03
			{
			
				public void foo() throws NullPointerException
				{
			
					Object body = new Object()
					{
						public void run(StringBuffer monitor)
								throws IllegalArgumentException
						{
							IResourceVisitor visitor = new IResourceVisitor()
							{
								public boolean visit(String resource)
										throws IllegalArgumentException
								{
									return true;
								}
							};
						}
					};
				}
			
			}
			
			interface IResourceVisitor
			{
			}
			"""
	);
}

/**
 * bug 286668: [formatter] 'Never Join Lines' joins lines that are split on method invocation
 * test Test to make sure that lines are joined when using 'Never Join Lines' preference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286668"
 */
public void testBug286668() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		public class Test {
		
			void foo() {
				StringBuilder builder = new StringBuilder();
				builder.append("abc").append("def").append("ghi").append("jkl").append("mno")
				.append("pqr").append("stu").append("vwx").append("yz");
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					StringBuilder builder = new StringBuilder();
					builder.append("abc").append("def").append("ghi").append("jkl")
							.append("mno")
							.append("pqr").append("stu").append("vwx").append("yz");
				}
			}
			"""
	);
}
public void testBug286668b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
		
			void foo() {
				StringBuilder builder = new StringBuilder();
				builder.append("abc").append("def")
				.append("ghi").append("jkl").append("mno")
				.append("pqr").append("stu").append("vwx").append("yz");
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					StringBuilder builder = new StringBuilder();
					builder.append("abc").append("def")
							.append("ghi").append("jkl").append("mno")
							.append("pqr").append("stu").append("vwx").append("yz");
				}
			}
			"""
	);
}
public void testBug286668c() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
		
			void foo() {
				StringBuilder builder = new StringBuilder();
				builder.append("abc").append("def")
				.append("ghi").append("jkl").append("mno")
				.append("pqr").append("stu").append("vwx")
				.append("yz");
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					StringBuilder builder = new StringBuilder();
					builder.append("abc").append("def")
							.append("ghi").append("jkl").append("mno")
							.append("pqr").append("stu").append("vwx")
							.append("yz");
				}
			}
			"""
	);
}
public void testBug286668_40w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 40;
	String source =
		"""
		public class Test {
		
			void foo() {
				StringBuilder builder = new StringBuilder();
				builder.append("abc").append("def").append("ghi").append("jkl").append("mno")
				.append("pqr").append("stu").append("vwx").append("yz");
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					StringBuilder builder = new StringBuilder();
					builder.append("abc")
							.append("def")
							.append("ghi")
							.append("jkl")
							.append("mno")
							.append("pqr")
							.append("stu")
							.append("vwx")
							.append("yz");
				}
			}
			"""
	);
}
public void testBug286668b_40w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 40;
	String source =
		"""
		public class Test {
		
			void foo() {
				StringBuilder builder = new StringBuilder();
				builder.append("abc").append("def")
				.append("ghi").append("jkl").append("mno")
				.append("pqr").append("stu").append("vwx").append("yz");
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					StringBuilder builder = new StringBuilder();
					builder.append("abc")
							.append("def")
							.append("ghi")
							.append("jkl")
							.append("mno")
							.append("pqr")
							.append("stu")
							.append("vwx")
							.append("yz");
				}
			}
			"""
	);
}
public void testBug286668c_40w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 40;
	String source =
		"""
		public class Test {
		
			void foo() {
				StringBuilder builder = new StringBuilder();
				builder.append("abc").append("def")
				.append("ghi").append("jkl").append("mno")
				.append("pqr").append("stu").append("vwx")
				.append("yz");
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					StringBuilder builder = new StringBuilder();
					builder.append("abc")
							.append("def")
							.append("ghi")
							.append("jkl")
							.append("mno")
							.append("pqr")
							.append("stu")
							.append("vwx")
							.append("yz");
				}
			}
			"""
	);
}
public void testBug286668_60w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 60;
	String source =
		"""
		public class Test {
		
			void foo() {
				StringBuilder builder = new StringBuilder();
				builder.append("abc").append("def").append("ghi").append("jkl").append("mno")
				.append("pqr").append("stu").append("vwx").append("yz");
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					StringBuilder builder = new StringBuilder();
					builder.append("abc").append("def").append("ghi")
							.append("jkl").append("mno")
							.append("pqr").append("stu").append("vwx")
							.append("yz");
				}
			}
			"""
	);
}
public void testBug286668b_60w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 60;
	String source =
		"""
		public class Test {
		
			void foo() {
				StringBuilder builder = new StringBuilder();
				builder.append("abc").append("def")
				.append("ghi").append("jkl").append("mno")
				.append("pqr").append("stu").append("vwx").append("yz");
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				void foo() {
					StringBuilder builder = new StringBuilder();
					builder.append("abc").append("def")
							.append("ghi").append("jkl").append("mno")
							.append("pqr").append("stu").append("vwx")
							.append("yz");
				}
			}
			"""
	);
}
public void testBug286668c_60w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 60;
	String source =
		"""
		public class Test {
		
			void foo() {
				StringBuilder builder = new StringBuilder();
				builder.append("abc").append("def")
						.append("ghi").append("jkl").append("mno")
						.append("pqr").append("stu").append("vwx")
						.append("yz");
			}
		}
		""";
	formatSource(source);
}

/**
 * bug 290905: [formatter] Certain formatter pref constellation cause endless loop ==> OOME
 * test Verify that there's endless loop when setting tab length to zero.
 * 	As the fix finalize bug 285565 implementation, added tests address only
 * 	missed test cases.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=290905"
 */
public void testBug290905a() throws JavaModelException {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 2;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		/**
		 * Test mixed, tab size = 0, indent size = 2, use tabs to indent
		 */
		public class Test {
		void foo() throws Exception { if (true) return; else throw new Exception(); }
		}
		""";
	formatSource(source,
		"""
			/**
			 * Test mixed, tab size = 0, indent size = 2, use tabs to indent
			 */
			public class Test {
			  void foo() throws Exception {
			    if (true)
			      return;
			    else
			      throw new Exception();
			  }
			}
			"""
	);
}
public void testBug290905b() throws JavaModelException {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 2;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = false;
	String source =
		"""
		/**
		 * Test mixed, tab size = 0, indent size = 2, use spaces to indent
		 */
		public class Test {
		void foo() throws Exception { if (true) return; else throw new Exception(); }
		}
		""";
	formatSource(source,
		"""
			/**
			 * Test mixed, tab size = 0, indent size = 2, use spaces to indent
			 */
			public class Test {
			  void foo() throws Exception {
			    if (true)
			      return;
			    else
			      throw new Exception();
			  }
			}
			"""
	);
}
public void testBug290905c() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		/**
		 * Test mixed, tab size = 0, indent size = 0, use tabs to indent
		 */
		public class Test {
		int i; // this is a long comment which should be split into two lines as the format line comment preference is activated
		}
		""";
	formatSource(source,
		"""
			/**
			 * Test mixed, tab size = 0, indent size = 0, use tabs to indent
			 */
			public class Test {
			int i; // this is a long comment which should be split into two lines as the
			       // format line comment preference is activated
			}
			""",
		false /* do not repeat */
	);
}
public void testBug290905d() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = false;
	String source =
		"""
		/**
		 * Test mixed, tab size = 0, indent size = 0, use spaces to indent
		 */
		public class Test {
		int i; // this is a long comment which should be split into two lines as the format line comment preference is activated
		}
		""";
	formatSource(source,
		"""
			/**
			 * Test mixed, tab size = 0, indent size = 0, use spaces to indent
			 */
			public class Test {
			int i; // this is a long comment which should be split into two lines as the
			       // format line comment preference is activated
			}
			""",
		false /* do not repeat */
	);
}
public void testBug290905e() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		/**
		 * Test tab char = TAB, tab size = 0, indent size = 0, use tabs to indent
		 */
		public class Test {
		int i; // this is a long comment which should be split into two lines as the format line comment preference is activated
		}
		""";
	formatSource(source,
		"""
			/**
			 * Test tab char = TAB, tab size = 0, indent size = 0, use tabs to indent
			 */
			public class Test {
			int i; // this is a long comment which should be split into two lines as the
			       // format line comment preference is activated
			}
			""",
		false /* do not repeat */
	);
}
public void testBug290905f() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = false;
	String source =
		"""
		/**
		 * Test tab char = TAB, tab size = 0, indent size = 0, use spaces to indent
		 */
		public class Test {
		int i; // this is a long comment which should be split into two lines as the format line comment preference is activated
		}
		""";
	formatSource(source,
		"""
			/**
			 * Test tab char = TAB, tab size = 0, indent size = 0, use spaces to indent
			 */
			public class Test {
			int i; // this is a long comment which should be split into two lines as the
			// format line comment preference is activated
			}
			""",
		false /* do not repeat */
	);
}

/**
 * bug 293496:  [formatter] 'insert_space_before_opening_brace_in_array_initializer' preference may be reset in certain circumstances
 * test Verify that a realigned annotation keep the 'insert_space_before_opening_brace_in_array_initializer'
 * 		preference initial value.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293496"
 */
public void testBug293240() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	setPageWidth80();
	String source =
		"""
		public class Test {
		  public static <A, B> Function<A, B> forMap(
		      Map<? super A, ? extends B> map, @Nullable final B defaultValue) {
		    if (defaultValue == null) {
		      return forMap(map);
		    }
		    return new ForMapWithDefault<A, B>(map, defaultValue);
		  }
		  public Object[] bar() {
			  return new Object[] { null };
		  }
		}
		""";
	formatSource(source,
		"""
			public class Test {
			    public static <A, B> Function<A, B> forMap(Map<? super A, ? extends B> map,
			            @Nullable final B defaultValue) {
			        if (defaultValue == null) {
			            return forMap(map);
			        }
			        return new ForMapWithDefault<A, B>(map, defaultValue);
			    }
			
			    public Object[] bar() {
			        return new Object[] { null };
			    }
			}
			"""
	);
}

/**
 * bug 293300: [formatter] The formatter is still unstable in certain circumstances
 * test Verify that formatting twice a compilation unit does not produce different output
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293300"
 */
public void testBug293300_wksp1_01() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp1;
		
		public class X01 {
		
			boolean foo(int test, int value) {
				// This comment may also be impacted after having been split in several lines. Furthermore, it\'s also important to verify that the algorithm works when the comment is split into several lines. It\'s a common use case that it may works for 1, 2 but not for 3 iterations...
				if (test == 0) {
					// skip
				} else if (Math.sqrt(Math.pow(test, 2)) > 10) // This is the offending comment after having been split into several lines
					return false;
				return true;
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X01 {
			
				boolean foo(int test, int value) {
					// This comment may also be impacted after having been split in several
					// lines. Furthermore, it\'s also important to verify that the algorithm
					// works when the comment is split into several lines. It\'s a common use
					// case that it may works for 1, 2 but not for 3 iterations...
					if (test == 0) {
						// skip
					} else if (Math.sqrt(Math.pow(test, 2)) > 10) // This is the offending
																	// comment after having
																	// been split into
																	// several lines
						return false;
					return true;
				}
			}
			"""
	);
}
public void testBug293300_wkps1_02() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp1;
		
		public class X02 {
			String field;
			 public X02(String test) {
				field= test.toLowerCase();
				try {
					testWhetherItWorksOrNot(test); // This comment will be split and should not involve instability
				} catch (Exception e) {
					return;
				}
			 }
			private void testWhetherItWorksOrNot(String test) {
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X02 {
				String field;
			
				public X02(String test) {
					field = test.toLowerCase();
					try {
						testWhetherItWorksOrNot(test); // This comment will be split and
														// should not involve instability
					} catch (Exception e) {
						return;
					}
				}
			
				private void testWhetherItWorksOrNot(String test) {
				}
			}
			"""
	);
}
public void testBug293300_wkps1_03() {
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X03 {
		public static final native int foo(
			int firstParameter,
			int secondParameter,
			int[] param3);        //When a long comment is placed here with at least one line to follow,
								  //    the second line may be difficult to be formatted correctly
		public static final native int bar();
		
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X03 {
				public static final native int foo(int firstParameter, int secondParameter,
						int[] param3); // When a long comment is placed here with at least
										// one line to follow,
										// the second line may be difficult to be formatted
										// correctly
			
				public static final native int bar();
			
			}
			"""
	);
}
public void testBug293300_wkps1_04() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp1;
		
		interface Y04_____________________________ {
		}
		
		public interface X04 extends Y04_____________________________ { // modifier constant
			// those constants are depending upon ClassFileConstants (relying that classfiles only use the 16 lower bits)
			final int AccDefault = 0;
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			interface Y04_____________________________ {
			}
			
			public interface X04 extends Y04_____________________________ { // modifier
																			// constant
				// those constants are depending upon ClassFileConstants (relying that
				// classfiles only use the 16 lower bits)
				final int AccDefault = 0;
			}
			"""
	);
}
public void testBug293300_wkps1_05() {
	String source =
		"""
		package wksp1;
		
		public class X05 {
			private final static String[] TEST_BUG = {"a", //$NON-NLS-1$
					"b", //$NON-NLS-1$
					"c", //$NON-NLS-1$
			};
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X05 {
				private final static String[] TEST_BUG = { "a", //$NON-NLS-1$
						"b", //$NON-NLS-1$
						"c", //$NON-NLS-1$
				};
			}
			"""
	);
}
public void testBug293300_wkps1_05_JoinLinesComments_BracesNextLine() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"""
		package wksp1;
		
		public class X05 {
			private final static String[] TEST_BUG = {"a", //$NON-NLS-1$
					"b", //$NON-NLS-1$
					"c", //$NON-NLS-1$
			};
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X05
			{
				private final static String[] TEST_BUG =
				{ "a", //$NON-NLS-1$
						"b", //$NON-NLS-1$
						"c", //$NON-NLS-1$
				};
			}
			"""
	);
}
public void testBug293300_wksp2_01() {
	String source =
		"""
		package wksp2;
		
		public class X01 {
		
			protected String foo(String[] tests) {
				String result = null;
				for (int i = 0; i < tests.length; i++) {
					String test = tests[i];
					if (test.startsWith("test")) { //$NON-NLS-1$
						//we got the malformed tree exception here
						result = test;
					}
				}
				return result;
			}
		
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X01 {
			
				protected String foo(String[] tests) {
					String result = null;
					for (int i = 0; i < tests.length; i++) {
						String test = tests[i];
						if (test.startsWith("test")) { //$NON-NLS-1$
							// we got the malformed tree exception here
							result = test;
						}
					}
					return result;
				}
			
			}
			"""
	);
}
public void testBug293300_wksp2_02() {
	String source =
		"""
		package wksp2;
		
		public class X02 {
		
		
			public void foo(int kind) {
				switch (kind) {
					case 0 :
						break;
					case 1 :
						//the first formatting looks strange on this already splitted
						// comment
						if (true)
							return;
					//fall through
					default:
						if (kind < 0)
							return;
						break;
				}
			}
		
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X02 {
			
				public void foo(int kind) {
					switch (kind) {
					case 0:
						break;
					case 1:
						// the first formatting looks strange on this already splitted
						// comment
						if (true)
							return;
						// fall through
					default:
						if (kind < 0)
							return;
						break;
					}
				}
			
			}
			"""
	);
}
public void testBug293300_wksp2_03() {
	String source =
		"""
		package wksp2;
		
		public class X03 {
			public byte[] foo(byte value) {
				byte[] result = new byte[10];
				int valTest = 0;
				switch (value) {
					case 1 :
						for (int j = 10; j >= 0; j--) {
							result[j] = (byte) (valTest & 0xff); // Bottom 8
							// bits
							valTest = valTest >>> 2;
						}
						break;
				}
				return result;
			}
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X03 {
				public byte[] foo(byte value) {
					byte[] result = new byte[10];
					int valTest = 0;
					switch (value) {
					case 1:
						for (int j = 10; j >= 0; j--) {
							result[j] = (byte) (valTest & 0xff); // Bottom 8
							// bits
							valTest = valTest >>> 2;
						}
						break;
					}
					return result;
				}
			}
			"""
	);
}
public void testBug293300_wksp2_04() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp2;
		
		public class X04 {
		
			void foo() {
				int lastDiagonal[]= new int[1000000 + 1]; // this line comments configuration
				// may screw up the formatter to know which one
				int origin= 1000000 / 2; // needs to stay at its current indentation or not
			}
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X04 {
			
				void foo() {
					int lastDiagonal[] = new int[1000000 + 1]; // this line comments
																// configuration
					// may screw up the formatter to know which one
					int origin = 1000000 / 2; // needs to stay at its current indentation or
												// not
				}
			}
			"""
	);
}
private static final String EXPECTED_OUTPUT_WKSP2E1 =
	"""
	package wksp2;
	
	public class X05 {
		void foo(int val) {
			try {
				loop: for (int i = 0; i < 10; i++) {
					switch (val) {
					case 1:
						if (i == 0) {
							if (true) {
								val++;
							} // these comments
								// may be wrongly
								// realigned
								// by the formatter
	
							// other comment
							val--;
							continue loop;
						}
					default:
						throw new IllegalArgumentException();
					}
				}
			} finally {
			}
		}
	}
	""";
public void testBug293300_wksp2_05() {
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
									// may be wrongly
									// realigned
									// by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1);
}
public void testBug293300_wksp2_05b() {
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
									 // may be wrongly
									 // realigned
									 // by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1);
}
private static final String EXPECTED_OUTPUT_WKSP2E3 =
	"""
	package wksp2;
	
	public class X05 {
		void foo(int val) {
			try {
				loop: for (int i = 0; i < 10; i++) {
					switch (val) {
					case 1:
						if (i == 0) {
							if (true) {
								val++;
							} // these comments
								// may be wrongly
								// realigned
								// by the formatter
	
							// other comment
							val--;
							continue loop;
						}
					default:
						throw new IllegalArgumentException();
					}
				}
			} finally {
			}
		}
	}
	""";
public void testBug293300_wksp2_05c() {
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
									  // may be wrongly
									  // realigned
									  // by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3);
}
public void testBug293300_wksp2_05d() {
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
									   // may be wrongly
									   // realigned
									   // by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3);
}
public void testBug293300_wksp2_05e() {
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
										// may be wrongly
										// realigned
										// by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3);
}
private static final String EXPECTED_OUTPUT_WKSP2E1_SPACES =
	"""
	package wksp2;
	
	public class X05 {
	    void foo(int val) {
	        try {
	            loop: for (int i = 0; i < 10; i++) {
	                switch (val) {
	                case 1:
	                    if (i == 0) {
	                        if (true) {
	                            val++;
	                        } // these comments
	                          // may be wrongly
	                          // realigned
	                          // by the formatter
	
	                        // other comment
	                        val--;
	                        continue loop;
	                    }
	                default:
	                    throw new IllegalArgumentException();
	                }
	            }
	        } finally {
	        }
	    }
	}
	""";
public void testBug293300_wksp2_05_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
									// may be wrongly
									// realigned
									// by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1_SPACES);
}
public void testBug293300_wksp2_05b_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
									 // may be wrongly
									 // realigned
									 // by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1_SPACES);
}
private static final String EXPECTED_OUTPUT_WKSP2E3_SPACES =
	"""
	package wksp2;
	
	public class X05 {
	    void foo(int val) {
	        try {
	            loop: for (int i = 0; i < 10; i++) {
	                switch (val) {
	                case 1:
	                    if (i == 0) {
	                        if (true) {
	                            val++;
	                        } // these comments
	                          // may be wrongly
	                          // realigned
	                          // by the formatter
	
	                        // other comment
	                        val--;
	                        continue loop;
	                    }
	                default:
	                    throw new IllegalArgumentException();
	                }
	            }
	        } finally {
	        }
	    }
	}
	""";
public void testBug293300_wksp2_05c_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
									  // may be wrongly
									  // realigned
									  // by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3_SPACES);
}
public void testBug293300_wksp2_05d_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
									   // may be wrongly
									   // realigned
									   // by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3_SPACES);
}
public void testBug293300_wksp2_05e_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		package wksp2;
		
		public class X05 {
			void foo(int val) {
				try {
					loop: for (int i=0; i<10; i++) {
						switch (val) {
							case 1 :
								if (i==0) {
									if (true) {
										val++;
									} //these comments
										// may be wrongly
										// realigned
										// by the formatter
		
									// other comment
									val--;
									continue loop;
								}
							default :
								throw new IllegalArgumentException();
						}
					}
				}
				finally {
				}
			}
		}
		""";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3_SPACES);
}
public void testBug293300_wksp_06() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp2;
		
		public class X06 {
		public static final native int foo(
			String field,        //First field
			int[] array);        //This comment may cause trouble for the formatter, especially if there\'s another
								  //    line below \s
		public static final native int bar();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X06 {
				public static final native int foo(String field, // First field
						int[] array); // This comment may cause trouble for the formatter,
										// especially if there\'s another
										// line below
			
				public static final native int bar();
			}
			"""
	);
}
public void testBug293300_wksp_07() {
	String source =
		"""
		package wksp2;
		
		public class X07 {
			void foo(boolean test) {
				if (test) {
					while (true) {
						try {
							try {
							} finally {
								if (true) {
									try {
										toString();
									} catch (Exception e) {
									} // nothing
								}
							} // first comment which does not move
		
							// second comment which should not move
							toString();
						} catch (Exception e) {
						}
		
					} // last comment
		
				}
		
				return;
			}
		}
		""";
	formatSource(source);
}
public void testBug293300_wksp2_08() {
	String source =
		"""
		package wksp2;
		
		public class X08 {
		int foo(int x) {
		    while (x < 0) {
		        switch (x) {
		       \s
		        }
		    } // end while
		
		        // fill in output parameter
		    if(x > 10)
		        x = 1;
		
		        // return the value
		    return x;
		    }
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X08 {
				int foo(int x) {
					while (x < 0) {
						switch (x) {
			
						}
					} // end while
			
					// fill in output parameter
					if (x > 10)
						x = 1;
			
					// return the value
					return x;
				}
			}
			"""
	);
}
public void testBug293300_wksp2_08b() {
	String source =
		"""
		package wksp2;
		
		public class X08 {
		int foo(int x) {
		    while (x < 0) {
		        switch (x) {
		       \s
		        }
		    } /* end while */
		
		        // fill in output parameter
		    if(x > 10)
		        x = 1;
		
		        // return the value
		    return x;
		    }
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X08 {
				int foo(int x) {
					while (x < 0) {
						switch (x) {
			
						}
					} /* end while */
			
					// fill in output parameter
					if (x > 10)
						x = 1;
			
					// return the value
					return x;
				}
			}
			"""
	);
}
public void testBug293300_wksp2_08c() {
	String source =
		"""
		package wksp2;
		
		public class X08 {
		int foo(int x) {
		    while (x < 0) {
		        switch (x) {
		       \s
		        }
		    } /** end while */
		
		        // fill in output parameter
		    if(x > 10)
		        x = 1;
		
		        // return the value
		    return x;
		    }
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X08 {
				int foo(int x) {
					while (x < 0) {
						switch (x) {
			
						}
					} /** end while */
			
					// fill in output parameter
					if (x > 10)
						x = 1;
			
					// return the value
					return x;
				}
			}
			"""
	);
}
public void testBug293300_wksp2_09() {
	String source =
		"""
		package wksp2;
		
		public class X09 {
		void foo(int param) {
		        int local = param - 10000; // first comment
		                                    // on several lines
		        // following unrelated comment
		        // also on several lines
		        int value = param + 10000;
		}
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X09 {
				void foo(int param) {
					int local = param - 10000; // first comment
												// on several lines
					// following unrelated comment
					// also on several lines
					int value = param + 10000;
				}
			}
			"""
	);
}
public void testBug293300_wksp2_10() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp2;
		
		public class X10 {
		
		    private  String           field;          //  Trailing comment of the field
		                                               //  This comment was not well formatted
		                                               //  as an unexpected line was inserted after the first one
		
		    // -------------------------------
		    X10()  {}
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X10 {
			
				private String field; // Trailing comment of the field
										// This comment was not well formatted
										// as an unexpected line was inserted after the
										// first one
			
				// -------------------------------
				X10() {
				}
			}
			"""
	);
}
public void testBug293300_wksp2_11() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		package wksp2;
		
		public abstract class X11 {
		
		    // [NEW]\s
		    /**
		     * Comment foo
		     */
		    public abstract StringBuffer foo();
		//#if defined(TEST)
		//#else
		//#endif
		
		    // [NEW]
		    /**
		     * Comment foo2
		     */
		    public abstract StringBuffer foo2();
		    // [NEW]
		    /**
		     * Comment foo3
		     */
		    public abstract StringBuffer foo3();
		
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public abstract class X11 {
			
				// [NEW]
				/**
				 * Comment foo
				 */
				public abstract StringBuffer foo();
				// #if defined(TEST)
				// #else
				// #endif
			
				// [NEW]
				/**
				 * Comment foo2
				 */
				public abstract StringBuffer foo2();
			
				// [NEW]
				/**
				 * Comment foo3
				 */
				public abstract StringBuffer foo3();
			
			}
			"""
	);
}
public void testBug293300_wksp2_12a() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp2;
		
		public class X12 {
		
		
			private boolean sampleField = false;   //trailing comment of the field which
		 	                                      //was wrongly formatted in previous
			                                      //version as an unexpected empty lines was
			                                      //inserted after the second comment line...
		
		
			/**
			    Javadoc comment
			*/
			public X12() {}
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X12 {
			
				private boolean sampleField = false; // trailing comment of the field which
														// was wrongly formatted in previous
														// version as an unexpected empty
														// lines was
														// inserted after the second comment
														// line...
			
				/**
				 * Javadoc comment
				 */
				public X12() {
				}
			}
			"""
	);
}
public void testBug293300_wksp2_12b() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp2;
		
		public class X12 {
		
		
			private boolean sampleField = false;   //trailing comment of the field which
		 	                                       //was wrongly formatted in previous
			                                       //version as an unexpected empty lines was
			                                       //inserted after the second comment line...
		
		
			/**
			    Javadoc comment
			*/
			public X12() {}
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X12 {
			
				private boolean sampleField = false; // trailing comment of the field which
														// was wrongly formatted in previous
														// version as an unexpected empty
														// lines was
														// inserted after the second comment
														// line...
			
				/**
				 * Javadoc comment
				 */
				public X12() {
				}
			}
			"""
	);
}
public void testBug293300_wksp2_13() {
	useOldCommentWidthCounting();
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		package wksp2;
		
		public class X13 {
		void foo(int x) {
			switch (x) {
				default : // regular object ref
		//				if (compileTimeType.isRawType() && runtimeTimeType.isBoundParameterizedType()) {
		//				    scope.problemReporter().unsafeRawExpression(this, compileTimeType, runtimeTimeType);
		//				}
			}
		}
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X13 {
				void foo(int x) {
					switch (x) {
					default: // regular object ref
						// if (compileTimeType.isRawType() &&
						// runtimeTimeType.isBoundParameterizedType()) {
						// scope.problemReporter().unsafeRawExpression(this,
						// compileTimeType, runtimeTimeType);
						// }
					}
				}
			}
			"""
	);
}
public void testBug293300_wksp2_14() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		package wksp2;
		
		public interface X14 {
		void foo();
		// line 1
		// line 2
		void bar();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface X14 {
				void foo();
			
				// line 1
				// line 2
				void bar();
			}
			"""
	);
}
// TODO (frederic) try to fix the formatter instability in the following test case
public void _testBug293300_wksp2_15a() {
	String source =
		"""
		package wksp2;
		
		public class X15 {
			void foo(int[] params) {
				if (params.length > 0) { // trailing comment formatted in several lines...
		//			int length = params == null ? : 0 params.length; // this commented lined causes troubles for the formatter but only if the comment starts at column 1...
					for (int i=0; i<params.length; i++) {
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			
			public class X15 {
				void foo(int[] params) {
					if (params.length > 0) { // trailing comment formatted in several
												// lines...
						// int length = params == null ? : 0 params.length; // this
						// commented
						// lined causes troubles for the formatter but only if the comment
						// starts at column 1...
						for (int i = 0; i < params.length; i++) {
						}
					}
				}
			}
			"""
	);
}
public void testBug293300_wksp2_15b() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp2;
		
		public class X15 {
			void foo(int[] params) {
				if (params.length > 0) { // trailing comment formatted in several lines...
					// int length = params == null ? : 0 params.length; // this commented lined does not cause troubles for the formatter when the comments is not on column 1...
					for (int i=0; i<params.length; i++) {
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X15 {
				void foo(int[] params) {
					if (params.length > 0) { // trailing comment formatted in several
												// lines...
						// int length = params == null ? : 0 params.length; // this
						// commented lined does not cause troubles for the formatter when
						// the comments is not on column 1...
						for (int i = 0; i < params.length; i++) {
						}
					}
				}
			}
			"""
	);
}
public void testBug293300_wksp3_01() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		package wksp3;
		
		public class X01 {
		static String[] constant = {
		// comment
		"first",
		// comment
		"second",
		};
		}
		""";
	formatSource(source,
		"""
			package wksp3;
			
			public class X01 {
				static String[] constant = {
						// comment
						"first",
						// comment
						"second", };
			}
			"""
	);
}

/**
 * bug 293496:  [formatter] 'insert_space_before_opening_brace_in_array_initializer' preference may be reset in certain circumstances
 * test Verify that non ArithmeticException occurs when using tab size = 0
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293496"
 */
public void testBug293496() {
	final Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
	options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
	options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "0");
	options.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "0");
	DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
	assertEquals("wrong indentation string", org.eclipse.jdt.internal.compiler.util.Util.EMPTY_STRING, codeFormatter.createIndentationString(0));
}

/**
 * bug 294500: [formatter] MalformedTreeException when formatting an invalid sequence of {@code <code>} tags in a javadoc comment
 * test Verify that no MalformedTreeException occurs while formatting bug test cases
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294500"
 */
public void testBug294500a() {
	String source =
		"""
		package wkps3;
		/**
		 * This sample produce an MalformedTreeException
		 * when formatted.
		 *
		 * <p> First paragraph
		 * {@link java.lang.String </code>a simple
		 * string<code>}.
		 *
		 * <p> Second paragraph.
		 *
		 * <p> Third paragraph. </p>
		 *
		 */
		public class X01 {
		
		}
		""";
	formatSource(source,
		"""
			package wkps3;
			
			/**
			 * This sample produce an MalformedTreeException when formatted.
			 *
			 * <p>
			 * First paragraph {@link java.lang.String </code>a simple string<code>}.
			 *
			 * <p>
			 * Second paragraph.
			 *
			 * <p>
			 * Third paragraph.
			 * </p>
			 *
			 */
			public class X01 {
			
			}
			"""
	);
}
public void testBug294500b() {
	String source =
		"""
		package wkps3;
		/**
		 * This sample produce an AIIOBE when formatting.
		 *
		 * <p> First paragraph
		 * {@link java.lang.String </code>a simple
		 * string<code>}.
		 */
		public class X02 {
		
		}
		""";
	formatSource(source,
		"""
			package wkps3;
			
			/**
			 * This sample produce an AIIOBE when formatting.
			 *
			 * <p>
			 * First paragraph {@link java.lang.String </code>a simple string<code>}.
			 */
			public class X02 {
			
			}
			"""
	);
}

/**
 * bug 294618: [formatter] The formatter takes two passes to format a common sequence of html tags
 * test Verify that the specific sequence of html tags is well formatted in one pass
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294618"
 */
public void testBug294618a() {
	String source =
		"""
		package wkps3;
		
		/**
		 * The formatter was not able to format the current comment:
		 *\s
		 * <ol>
		 *   <li><p> First item
		 *
		 *   <li><p> Second item
		 *
		 *   <li><p> First paragraph of third item
		 *
		 *   <p> Second paragraph of third item
		 *
		 *   <blockquote><table cellpadding=0 cellspacing=0 summary="layout">
		 *   <tr><td><tt>::255.255.0.d</tt><td></tr>
		 *   </table></blockquote>
		 *   </li>
		 * </ol>
		 */
		public class X01 {
		
		}
		""";
	formatSource(source,
		"""
			package wkps3;
			
			/**
			 * The formatter was not able to format the current comment:
			 *\s
			 * <ol>
			 * <li>
			 * <p>
			 * First item
			 *
			 * <li>
			 * <p>
			 * Second item
			 *
			 * <li>
			 * <p>
			 * First paragraph of third item
			 *
			 * <p>
			 * Second paragraph of third item
			 *
			 * <blockquote>
			 * <table cellpadding=0 cellspacing=0 summary="layout">
			 * <tr>
			 * <td><tt>::255.255.0.d</tt>
			 * <td>
			 * </tr>
			 * </table>
			 * </blockquote></li>
			 * </ol>
			 */
			public class X01 {
			
			}
			"""
	);
}
public void testBug294618b() {
	String source =
		"""
		/**
		 * Verify deep html tag nesting:
		 *\s
		 * <ol>
		 *   <li><p> First item
		 *   <li><p> Second item
		 *   <ul>
		 *     <li><p> First item of second item
		 *       <blockquote><table cellpadding=0 cellspacing=0 summary="layout">
		 *       <tr><td><tt><i><b>::255.255.0.d</b></i></tt></td></tr>
		 *       </table></blockquote>
		 *     </li>
		 *   </ul>
		 *   </li>
		 * </ol>
		 */
		public class X02 {
		
		}
		""";
	formatSource(source,
		"""
			/**
			 * Verify deep html tag nesting:
			 *\s
			 * <ol>
			 * <li>
			 * <p>
			 * First item
			 * <li>
			 * <p>
			 * Second item
			 * <ul>
			 * <li>
			 * <p>
			 * First item of second item <blockquote>
			 * <table cellpadding=0 cellspacing=0 summary="layout">
			 * <tr>
			 * <td><tt><i><b>::255.255.0.d</b></i></tt></td>
			 * </tr>
			 * </table>
			 * </blockquote></li>
			 * </ul>
			 * </li>
			 * </ol>
			 */
			public class X02 {
			
			}
			"""
	);
}

/**
 * bug 294631: [formatter] The formatter takes two passes to format a common sequence of html tags
 * test Verify that the specific sequence of html tags is well formatted in one pass
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294631"
 */
public void testBug294631() {
	String source =
		"""
		package wkps3;
		
		/**
		 * This comment makes the formatter unstable:
		 *\s
		 * <ol>
		 *   <li><p> first line
		 *   second line</li>
		 * </ol>
		 */
		public class X {
		
		}
		""";
	formatSource(source,
		"""
			package wkps3;
			
			/**
			 * This comment makes the formatter unstable:
			 *\s
			 * <ol>
			 * <li>
			 * <p>
			 * first line second line</li>
			 * </ol>
			 */
			public class X {
			
			}
			"""
	);
}

/**
 * bug 295175: [formatter] Missing space before a string at the beginning of a javadoc comment
 * test Verify that space is well inserted before the leading string
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295175"
 */
public void testBug295175a() {
	String source =
		"""
		public class X {
		/**
		 * <p>
		 * "String", this string may be not well formatted in certain circumstances,
		 * typically after bug 294529 has been fixed...
		 */
		void foo() {}
		}
		""";
	formatSource(source,
		"""
			public class X {
				/**
				 * <p>
				 * "String", this string may be not well formatted in certain circumstances,
				 * typically after bug 294529 has been fixed...
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug295175b() {
	String source =
		"""
		package wksp2;
		
		public interface X {
		
		    /**
		     * <P>
		     * <BR>
			 *<B>NOTE</B><BR>
			 * Formatter can miss a space before the previous B tag...
		     **/
			void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface X {
			
				/**
				 * <P>
				 * <BR>
				 * <B>NOTE</B><BR>
				 * Formatter can miss a space before the previous B tag...
				 **/
				void foo();
			}
			"""
	);
}
public void testBug295175c() {
	String source =
		"""
		package wksp2;
		
		public interface X {
		
		    /**
		     * <P>Following p tag can miss a space before after formatting
		     *<p>
		     * end of comment.
		     **/
			void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface X {
			
				/**
				 * <P>
				 * Following p tag can miss a space before after formatting
				 * <p>
				 * end of comment.
				 **/
				void foo();
			}
			"""
	);
}
public void testBug295175d() {
	String source =
		"""
		package wksp2;
		
		public interface X {
		
		    /**
		     * <p>Following p tag can miss a space before after formatting
		     *
		     *<p>
		     * <BR>
			 *<B>NOTE</B><BR>
			 * Formatter can miss a space before the previous B tag...
		     **/
			void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface X {
			
				/**
				 * <p>
				 * Following p tag can miss a space before after formatting
				 *
				 * <p>
				 * <BR>
				 * <B>NOTE</B><BR>
				 * Formatter can miss a space before the previous B tag...
				 **/
				void foo();
			}
			"""
	);
}
public void testBug295175e() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp3;
		
		public class X01 {
		    /**\s
		     * In this peculiar config <code>true</code>, the comment is not___\s
		     * really well formatted. The problem is that the first_ code tag
		     * here_______ <code>/*</code> and <code>*&#47;</code> go at the end of the previous line
		     * instead of staying on the 3rd one...\s
		     */
		    void foo() {}
		}
		""";
	formatSource(source,
		"""
			package wksp3;
			
			public class X01 {
				/**
				 * In this peculiar config <code>true</code>, the comment is not___ really
				 * well formatted. The problem is that the first_ code tag here_______
				 * <code>/*</code> and <code>*&#47;</code> go at the end of the previous
				 * line instead of staying on the 3rd one...
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug295175f() {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp1;
		
		public class X01 {
		
			/**
			 * Finds the deepest <code>IJavaElement</code> in the hierarchy of
			 * <code>elt</elt>'s children (including <code>elt</code> itself)
			 * which has a source range that encloses <code>position</code>
			 * according to <code>mapper</code>.
			 */
			void foo() {}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X01 {
			
				/**
				 * Finds the deepest <code>IJavaElement</code> in the hierarchy of
				 * <code>elt</elt>\'s children (including <code>elt</code> itself) which has
				 * a source range that encloses <code>position</code> according to
				 * <code>mapper</code>.
				 */
				void foo() {
				}
			}
			"""
	);
}

/**
 * bug 295238: [formatter] The comment formatter add an unexpected new line in block comment
 * test Verify that formatting a block comment with a tag does not add an unexpected new line
 * 		when the 'Never join lines' option is set
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295238"
 */
public void testBug295238() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"""
		package wksp1;
		
		public interface X03 {
		\t
			class Inner {
			\t
				/* (non-Javadoc)
				 * @see org.eclipse.jface.text.TextViewer#customizeDocumentCommand(org.eclipse.jface.text.DocumentCommand)
				 */
				protected void foo() {
				}
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public interface X03 {
			
				class Inner {
			
					/*
					 * (non-Javadoc)
					 *\s
					 * @see org.eclipse.jface.text.TextViewer#customizeDocumentCommand(org.
					 * eclipse.jface.text.DocumentCommand)
					 */
					protected void foo() {
					}
				}
			}
			"""
	);
}
// the following test already passed with v_A21, but failed with first version of the patch
public void testBug295238b1() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"""
		package wksp1;
		
		public class X02 {
		
			void foo() {
		/*		if ((operatorSignature & CompareMASK) == (alternateOperatorSignature & CompareMASK)) { // same promotions and result
					scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4));\s
				}
		*/	\t
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X02 {
			
				void foo() {
					/*
					 * if ((operatorSignature & CompareMASK) == (alternateOperatorSignature
					 * & CompareMASK)) { // same promotions and result
					 * scope.problemReporter().unnecessaryCastForArgument((CastExpression)
					 * expression, TypeBinding.wellKnownType(scope,
					 * expression.implicitConversion >> 4));
					 * }
					 */
				}
			}
			"""
	);
}
// the following test failed with v_A21 and with the version v00 of the patch
public void testBug295238b2() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"""
		package wksp1;
		
		public class X02 {
		
			void foo() {
		/*			scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4));\s
		*/	\t
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X02 {
			
				void foo() {
					/*
					 * scope.problemReporter().unnecessaryCastForArgument((CastExpression)
					 * expression, TypeBinding.wellKnownType(scope,
					 * expression.implicitConversion >> 4));
					 */
				}
			}
			"""
	);
}
// the following test failed with v_A21 and with the version v00 of the patch
public void testBug295238b3() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"""
		package wksp1;
		
		public class X02 {
		
			void foo() {
		/*
					scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4));\s
		*/	\t
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X02 {
			
				void foo() {
					/*
					 * scope.problemReporter().unnecessaryCastForArgument((CastExpression)
					 * expression, TypeBinding.wellKnownType(scope,
					 * expression.implicitConversion >> 4));
					 */
				}
			}
			"""
	);
}

/**
 * bug 264112: [Formatter] Wrap when necessary too aggressive on short qualifiers
 * test
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=264112"
 */
// Max line width = 24
public void testBug264112_w24_S1() {
	this.formatterPrefs.page_width = 24;
	String source =
		"""
		class Sample1 {void foo() {Other.bar( 100,
		200,
		300,
		400,
		500,
		600,
		700,
		800,
		900 );}}
		""";
	formatSource(source,
		"""
			class Sample1 {
				void foo() {
					Other.bar(100,
							200,
							300,
							400,
							500,
							600,
							700,
							800,
							900);
				}
			}
			"""
	);
}
public void testBug264112_w24_S2() {
	this.formatterPrefs.page_width = 24;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"""
			class Sample2 {
				int foo(Some a) {
					return a.getFirst();
				}
			}
			"""
	);
}
// Max line width = 25
public void testBug264112_w25_S1() {
	this.formatterPrefs.page_width = 25;
	String source =
		"""
		class Sample1 {void foo() {Other.bar( 100,
		200,
		300,
		400,
		500,
		600,
		700,
		800,
		900 );}}
		""";
	formatSource(source,
		"""
			class Sample1 {
				void foo() {
					Other.bar(100,
							200, 300,
							400, 500,
							600, 700,
							800,
							900);
				}
			}
			"""
	);
}
public void testBug264112_w25_S2() {
	this.formatterPrefs.page_width = 25;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"""
			class Sample2 {
				int foo(Some a) {
					return a.getFirst();
				}
			}
			"""
	);
}
// Max line width = 26
public void testBug264112_w26_S1() {
	this.formatterPrefs.page_width = 26;
	String source =
		"""
		class Sample1 {void foo() {Other.bar( 100,
		200,
		300,
		400,
		500,
		600,
		700,
		800,
		900 );}}
		""";
	formatSource(source,
		"""
			class Sample1 {
				void foo() {
					Other.bar(100,
							200, 300,
							400, 500,
							600, 700,
							800, 900);
				}
			}
			"""
	);
}
public void testBug264112_w26_S2() {
	this.formatterPrefs.page_width = 26;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"""
			class Sample2 {
				int foo(Some a) {
					return a.getFirst();
				}
			}
			"""
	);
}
public void testBug264112_wksp1_01() {
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X01 {
		
			public Object foo(Object scope) {
				if (scope != null) {
					if (true) {
						for (int i = 0; i < 10; i++) {
							if (i == 0) {
							} else if (i < 5) {
							} else {
								scope.problemReporter().typeMismatchErrorActualTypeExpectedType(expression, expressionTb, expectedElementsTb);
								return null;
							}
						}
					}
					return null;
				}
			}
		
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X01 {
			
				public Object foo(Object scope) {
					if (scope != null) {
						if (true) {
							for (int i = 0; i < 10; i++) {
								if (i == 0) {
								} else if (i < 5) {
								} else {
									scope.problemReporter()
											.typeMismatchErrorActualTypeExpectedType(
													expression, expressionTb,
													expectedElementsTb);
									return null;
								}
							}
						}
						return null;
					}
				}
			
			}
			"""
	);
}
public void testBug264112_wksp1_02() {
	String source =
		"""
		package wksp1;
		
		public class X02 {
		
			public String toString() {
				StringBuffer buffer = new StringBuffer();
				if (true) {
					buffer.append("- possible values:	["); //$NON-NLS-1$\s
					buffer.append("]\\n"); //$NON-NLS-1$\s
					buffer.append("- curr. val. index:	").append(currentValueIndex).append("\\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				buffer.append("- description:		").append(description).append("\\n"); //$NON-NLS-1$ //$NON-NLS-2$
				return buffer.toString();
			}
		
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X02 {
			
				public String toString() {
					StringBuffer buffer = new StringBuffer();
					if (true) {
						buffer.append("- possible values:	["); //$NON-NLS-1$
						buffer.append("]\\n"); //$NON-NLS-1$
						buffer.append("- curr. val. index:	").append(currentValueIndex).append("\\n"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					buffer.append("- description:		").append(description).append("\\n"); //$NON-NLS-1$ //$NON-NLS-2$
					return buffer.toString();
				}
			
			}
			"""
	);
}
public void testBug264112_wksp2_01() {
	setPageWidth80();
	String source =
		"""
		package wksp2;
		
		public class X01 {
		
		    private static final String PATH_SMOOTH_QUAD_TO = "SMOOTH";
		    private static final String XML_SPACE = " ";
		    private static final String PATH_CLOSE = "CLOSE";
		
			String foo(Point point, Point point_plus1) {
		        StringBuffer sb = new StringBuffer();
		        while (true) {
		            if (point != null) {
		                // Following message send was unnecessarily split
		                sb.append(PATH_SMOOTH_QUAD_TO)
		                .append(String.valueOf(midValue(point.x, point_plus1.x)))
		                .append(XML_SPACE)
		                .append(String.valueOf(midValue(point.y, point_plus1.y)));
		            } else {
		                break;
		            }
		        }
		        sb.append(PATH_CLOSE);
		
		        return sb.toString();
		    }
		
		    private int midValue(int x1, int x2) {
		        return (x1 + x2) / 2;
		    }
		
		}
		class Point {
			int x,y;
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X01 {
			
				private static final String PATH_SMOOTH_QUAD_TO = "SMOOTH";
				private static final String XML_SPACE = " ";
				private static final String PATH_CLOSE = "CLOSE";
			
				String foo(Point point, Point point_plus1) {
					StringBuffer sb = new StringBuffer();
					while (true) {
						if (point != null) {
							// Following message send was unnecessarily split
							sb.append(PATH_SMOOTH_QUAD_TO)
									.append(String
											.valueOf(midValue(point.x, point_plus1.x)))
									.append(XML_SPACE).append(String
											.valueOf(midValue(point.y, point_plus1.y)));
						} else {
							break;
						}
					}
					sb.append(PATH_CLOSE);
			
					return sb.toString();
				}
			
				private int midValue(int x1, int x2) {
					return (x1 + x2) / 2;
				}
			
			}
			
			class Point {
				int x, y;
			}
			"""
	);
}
public void testBug264112_wksp2_02() {
	String source =
		"""
		package wksp2;
		
		public class X02 {
		\t
			void test(X02 indexsc) {
				if (indexsc == null) {
				} else {
		
					indexsc.reopenScan(
								searchRow,                      	// startKeyValue
								ScanController.GE,            		// startSearchOp
								null,                         		// qualifier
								null, 		                        // stopKeyValue
								ScanController.GT             		// stopSearchOp\s
								);
				}
			\t
			}
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X02 {
			
				void test(X02 indexsc) {
					if (indexsc == null) {
					} else {
			
						indexsc.reopenScan(searchRow, // startKeyValue
								ScanController.GE, // startSearchOp
								null, // qualifier
								null, // stopKeyValue
								ScanController.GT // stopSearchOp
						);
					}
			
				}
			}
			"""
	);
}

/**
 * bug 297225: [formatter] Indentation may be still wrong in certain circumstances after formatting
 * test Verify that comment indentation is correct when there's a mix of tab and spaces in
 * 		existing indentation and all comments formatting is off.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297225"
 */
public void testBug297225() {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.comment_format_javadoc_comment = false;
	String source =
		"""
		public class X01 {
		   \t
		   	/**
		   	 * The foo method
		   	 */
			void foo() {}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
				/**
				 * The foo method
				 */
				void foo() {
				}
			}
			"""
	);
}

/**
 * bug 297546: [formatter] Formatter removes blank after @see if reference is wrapped
 * test Verify that space after the @see tag is not removed while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297546"
 */
public void testBug297546() {
	String source =
		"""
		package org.eclipse.jdt.core;
		public class TestClass implements TestInterface {
		
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.TestInterface#testMethod(org.eclipse.jdt.core.TestInterface)
			 */
			public void testMethod(TestInterface aLongNameForAParam) {
				// do nothing
			}
		
		\t
		}
		interface TestInterface {
			void testMethod(TestInterface aLongNameForAParam);
		}
		""";
	formatSource(source,
		"""
			package org.eclipse.jdt.core;
			
			public class TestClass implements TestInterface {
			
				/*
				 * (non-Javadoc)
				 *\s
				 * @see org.eclipse.jdt.core.TestInterface#testMethod(org.eclipse.jdt.core.
				 * TestInterface)
				 */
				public void testMethod(TestInterface aLongNameForAParam) {
					// do nothing
				}
			
			}
			
			interface TestInterface {
				void testMethod(TestInterface aLongNameForAParam);
			}
			"""
	);
}

/**
 * bug 298243: [formatter] Removing empty lines between import groups
 * test Verify that space after the @see tag is not removed while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=298243"
 */
public void testBug298243() {
	this.formatterPrefs.number_of_empty_lines_to_preserve = 0;
	String source =
		"""
		package test;
		
		import java.util.concurrent.atomic.AtomicInteger;
		
		import org.xml.sax.SAXException;
		
		public class Test {
			public static void main(String[] args) {
				SAXException e;
				AtomicInteger w;
			}
		}
		""";
	formatSource(source);
}

/**
 * bug 298844: [formatter] New lines in empty method body wrong behavior
 * test Verify that comment is well indented inside empty constructor and method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=298844"
 */
public void testBug298844a() {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.keep_method_body_on_one_line = DefaultCodeFormatterConstants.ONE_LINE_IF_EMPTY;
	String source =
		"""
		public class X01 {
		public X01() {
		// TODO Auto-generated constructor stub
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				public X01() {
					// TODO Auto-generated constructor stub
				}
			}
			"""
	);
}
public void testBug298844b() {
	this.formatterPrefs.keep_method_body_on_one_line = DefaultCodeFormatterConstants.ONE_LINE_IF_EMPTY;
	String source =
		"""
		public class X02 {
		public void foo() {
			// TODO Auto-generated constructor stub
		}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				public void foo() {
					// TODO Auto-generated constructor stub
				}
			}
			"""
	);
}

/**
 * bug 302123: [formatter] AssertionFailedException occurs while formatting a source containing the specific javadoc comment...
 * test Verify that no exception occurs while formatting source including the specific comment
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=302123"
 */
public void testBug302123() {
	String source =
		"""
		package test;
		public class Test {
			public static void main(String[] args) {
				String s="X"+/** ***/"Y";
			}
		
		}
		""";
	formatSource(source,
		"""
			package test;
			
			public class Test {
				public static void main(String[] args) {
					String s = "X" + /** ***/
							"Y";
				}
			
			}
			"""
	);
}
public void testBug302123b() {
	String source =
		"""
		package test;
		public class Test {
			public static void main(String[] args) {
				String s="X"+/**    XXX   ***/"Y";
			}
		
		}
		""";
	formatSource(source,
		"""
			package test;
			
			public class Test {
				public static void main(String[] args) {
					String s = "X" + /** XXX ***/
							"Y";
				}
			
			}
			"""
	);
}
public void testBug302123c() {
	String source =
		"""
		package test;
		public class Test {
			public static void main(String[] args) {
				String s="X"+/**    **  XXX  **    ***/"Y";
			}
		
		}
		""";
	formatSource(source,
		"""
			package test;
			
			public class Test {
				public static void main(String[] args) {
					String s = "X" + /** ** XXX ** ***/
							"Y";
				}
			
			}
			"""
	);
}
public void testBug302123d() {
	String source =
		"""
		package test;
		public class Test {
			public static void main(String[] args) {
				String s="X"+/**AAA   *** BBB ***   CCC***/"Y";
			}
		
		}
		""";
	formatSource(source,
		"""
			package test;
			
			public class Test {
				public static void main(String[] args) {
					String s = "X" + /** AAA *** BBB *** CCC ***/
							"Y";
				}
			
			}
			"""
	);
}

/**
 * bug 302552: [formatter] Formatting qualified invocations can be broken when the Line Wrapping policy forces element to be on a new line
 * test Verify that wrapping policies forcing the first element to be on a new line are working again...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=302552"
 */
public void testBug302552_LW0() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_NO_ALIGNMENT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"""
			class Sample2 {
				int foo(Some a) {
					return a.getFirst();
				}
			}
			"""
	);
}
public void testBug302552_LW1() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_COMPACT_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"""
			class Sample2 {
				int foo(Some a) {
					return a.getFirst();
				}
			}
			"""
	);
}
public void testBug302552_LW2() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"""
			class Sample2 {
				int foo(Some a) {
					return a
							.getFirst();
				}
			}
			"""
	);
}
public void testBug302552_LW3() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_ONE_PER_LINE_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"""
			class Sample2 {
				int foo(Some a) {
					return a
							.getFirst();
				}
			}
			"""
	);
}
public void testBug302552_LW4() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_NEXT_SHIFTED_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"""
			class Sample2 {
				int foo(Some a) {
					return a
							.getFirst();
				}
			}
			"""
	);
}
public void testBug302552_LW5() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_NEXT_PER_LINE_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"""
			class Sample2 {
				int foo(Some a) {
					return a.getFirst();
				}
			}
			"""
	);
}

/**
 * bug 304529: [formatter] NPE when either the disabling or the enabling tag is not defined
 * test Verify that having an empty disabling or enabling is now accepted by the formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=304529"
 */
public void testBug304529() {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "off".toCharArray();
	this.formatterPrefs.enabling_tag = null;
	String source =
		"""
		/* off */
		public class X01 {
		void     foo(    )      {\t
						//      unformatted       area
		}
		}
		""";
	formatSource(source);
}
public void testBug304529b() {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = null;
	this.formatterPrefs.enabling_tag = "on".toCharArray();
	String source =
		"""
		/* on */
		public class X01 {
		void     foo(    )      {\t
						//      formatted       area
		}
		}
		""";
	formatSource(source,
		"""
			/* on */
			public class X01 {
				void foo() {
					// formatted area
				}
			}
			"""
	);
}
public void testBug304529c() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "off");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "");
	String source =
		"""
		/* off */
		public class X01 {
		void     foo(    )      {\t
						//      unformatted       area
		}
		}
		""";
	formatSource(source);
}
public void testBug304529d() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "on");
	String source =
		"""
		/* on */
		public class X01 {
		void     foo(    )      {\t
						//      formatted       area
		}
		}
		""";
	formatSource(source,
		"""
			/* on */
			public class X01 {
				void foo() {
					// formatted area
				}
			}
			"""
	);
}
public void testBug304529e() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "off");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "on");
	String source =
		"""
		public class X01 {
		/* off */
		void     foo(    )      {\t
						//      unformatted       area
		}
		/* on */
		void     bar(    )      {\t
						//      formatted       area
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			/* off */
			void     foo(    )      {\t
							//      unformatted       area
			}
			/* on */
				void bar() {
					// formatted area
				}
			}
			"""
	);
}

/**
 * bug 309706: [formatter] doesn't work when code has three semicolons side by side
 * test Verify that formatter does get puzzled by three consecutive semicolons
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=309706"
 */
public void testBug309706() {
	String source =
		"""
		public class Test {
		
		    private int id;;;
		
		    private void dummy() {
		
		        if (true) {
		                    System.out.println("bla");
		        }
		    }
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				private int id;;;
			
				private void dummy() {
			
					if (true) {
						System.out.println("bla");
					}
				}
			}
			"""
	);
}
public void testBug309706b() {
	String source =
		"""
		    private int id;;;
		
		    private void dummy() {
		
		        if (true) {
		                    System.out.println("bla");
		        }
			}
		""";
	formatSource(source,
		"""
			private int id;;;
			
			private void dummy() {
			
				if (true) {
					System.out.println("bla");
				}
			}
			""",
		CodeFormatter.K_CLASS_BODY_DECLARATIONS
	);
}

/**
 * bug 311578: [formatter] Enable/disable tag detection should include comment start/end tokens
 * test Ensure that the formatter now accepts tags with comment start/end tokens
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=311578"
 */
public void testBug311578a() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "//J-".toCharArray();
	this.formatterPrefs.enabling_tag = "//J+".toCharArray();
	String source =
		"""
		package a;
		public class Bug {
		int a      =  -     1  +    42;
		
		//J-
		int b      =  -     1  +    42;
		//J+
		
		char                       x;
		
		////J-
		int c      =  -     1  +    42;
		////J+
		
		char                       y;
		
		/* J- */
		int d      =  -     1  +    42;
		/* J+ */
		
		char                       z;
		
		/* //J- */
		int e      =  -     1  +    42;
		/* //J+ */
		
		/** J-1 blabla */
		char                       t;
		}
		""";
	formatSource(source,
		"""
			package a;
			
			public class Bug {
				int a = -1 + 42;
			
			//J-
			int b      =  -     1  +    42;
			//J+
			
				char x;
			
			////J-
			int c      =  -     1  +    42;
			////J+
			
				char y;
			
				/* J- */
				int d = -1 + 42;
				/* J+ */
			
				char z;
			
			/* //J- */
			int e      =  -     1  +    42;
			/* //J+ */
			
				/** J-1 blabla */
				char t;
			}
			"""
	);
}
public void testBug311578b() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "/* J- */".toCharArray();
	this.formatterPrefs.enabling_tag = "/* J+ */".toCharArray();
	String source =
		"""
		package a;
		public class Bug {
		int a      =  -     1  +    42;
		
		//J-
		int b      =  -     1  +    42;
		//J+
		
		char                       x;
		
		////J-
		int c      =  -     1  +    42;
		////J+
		
		char                       y;
		
		/* J- */
		int d      =  -     1  +    42;
		/* J+ */
		
		char                       z;
		
		/* //J- */
		int e      =  -     1  +    42;
		/* //J+ */
		
		/** J-1 blabla */
		char                       t;
		}
		""";
	formatSource(source,
		"""
			package a;
			
			public class Bug {
				int a = -1 + 42;
			
				// J-
				int b = -1 + 42;
				// J+
			
				char x;
			
				//// J-
				int c = -1 + 42;
				//// J+
			
				char y;
			
			/* J- */
			int d      =  -     1  +    42;
			/* J+ */
			
				char z;
			
				/* //J- */
				int e = -1 + 42;
				/* //J+ */
			
				/** J-1 blabla */
				char t;
			}
			"""
	);
}
public void testBug311578c() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "//F--");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "//F++");
	String source =
		"""
		package a;
		public class Bug {
		int a      =  -     1  +    42;
		
		//F--
		int b      =  -     1  +    42;
		//F++
		
		char                       x;
		
		////F--
		int c      =  -     1  +    42;
		////F++
		
		char                       y;
		
		/* F-- */
		int d      =  -     1  +    42;
		/* F++ */
		
		char                       z;
		
		/* //F-- */
		int e      =  -     1  +    42;
		/* //F++ */
		
		/** F--1 blabla */
		char                       t;
		}
		""";
	formatSource(source,
		"""
			package a;
			
			public class Bug {
				int a = -1 + 42;
			
			//F--
			int b      =  -     1  +    42;
			//F++
			
				char x;
			
			////F--
			int c      =  -     1  +    42;
			////F++
			
				char y;
			
				/* F-- */
				int d = -1 + 42;
				/* F++ */
			
				char z;
			
			/* //F-- */
			int e      =  -     1  +    42;
			/* //F++ */
			
				/** F--1 blabla */
				char t;
			}
			"""
	);
}
public void testBug311578d() throws JavaModelException {
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN,
			DefaultCodeFormatterConstants.TRUE);
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "/*F--*/");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "/*F++*/");
	String source =
		"""
		package a;
		public class Bug {
		int a      =  -     1  +    42;
		
		//F--
		int b      =  -     1  +    42;
		//F++
		
		char                       x;
		
		////F--
		int c      =  -     1  +    42;
		////F++
		
		char                       y;
		
		/* F-- */
		int d      =  -     1  +    42;
		/* F++ */
		
		char                       y2;
		
		/*F--*/
		int d2      =  -     1  +    42;
		/*F++*/
		
		char                       z;
		
		/* //F-- */
		int e      =  -     1  +    42;
		/* //F++ */
		
		/** F--1 blabla */
		char                       t;
		}
		""";
	formatSource(source,
		"""
			package a;
			
			public class Bug {
				int a = -1 + 42;
			
				// F--
				int b = -1 + 42;
				// F++
			
				char x;
			
				//// F--
				int c = -1 + 42;
				//// F++
			
				char y;
			
				/* F-- */
				int d = -1 + 42;
				/* F++ */
			
				char y2;
			
			/*F--*/
			int d2      =  -     1  +    42;
			/*F++*/
			
				char z;
			
				/* //F-- */
				int e = -1 + 42;
				/* //F++ */
			
				/** F--1 blabla */
				char t;
			}
			"""
	);
}
public void testBug311578e() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "//J-".toCharArray();
	this.formatterPrefs.enabling_tag = "//J+".toCharArray();
	String source =
		"""
		package a;
		public class Bug {
		char                       z2;
		
		//J-1
		int f      =  -     1  +    42;
		//J+2
		
		char                       z3;
		
		//J- 1
		int g      =  -     1  +    42;
		//J+ 2
		
		char                       z4;
		
		  //J-
		int h      =  -     1  +    42;
		  //J+
		
		char                       z5;
		
		/*
		//J-
		*/
		int i      =  -     1  +    42;
		/*
		 //J+
		 */
		
		char                       z6;\
		}
		""";
	formatSource(source,
		"""
			package a;
			
			public class Bug {
				char z2;
			
			//J-1
			int f      =  -     1  +    42;
			//J+2
			
				char z3;
			
			//J- 1
			int g      =  -     1  +    42;
			//J+ 2
			
				char z4;
			
				//J-
			int h      =  -     1  +    42;
			  //J+
			
				char z5;
			
			/*
			//J-
			*/
			int i      =  -     1  +    42;
			/*
			 //J+
			 */
			
				char z6;
			}
			"""
	);
}
public void testBug311578_320754a() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "//J-".toCharArray();
	this.formatterPrefs.enabling_tag = "//J+".toCharArray();
	String source =
		"""
		//J-
		@MyAnnot (
		  testAttribute = {"test1", "test2", "test3"}
		)
		//J+
		public class X
		{
		    public void foo()
		    {
		    }
		}
		""";
	formatSource(source,
		"""
			//J-
			@MyAnnot (
			  testAttribute = {"test1", "test2", "test3"}
			)
			//J+
			public class X {
				public void foo() {
				}
			}
			"""
	);
}
public void testBug311578_320754b() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "/*J-*/".toCharArray();
	this.formatterPrefs.enabling_tag = "/*J+*/".toCharArray();
	String source =
		"""
		/*J-*/
		@MyAnnot (
		  testAttribute = {"test1", "test2", "test3"}
		)
		/*J+*/
		public class X
		{
		    public void foo()
		    {
		    }
		}
		""";
	formatSource(source,
		"""
			/*J-*/
			@MyAnnot (
			  testAttribute = {"test1", "test2", "test3"}
			)
			/*J+*/
			public class X {
				public void foo() {
				}
			}
			"""
	);
}

/**
 * bug 311582: [formatter] Master switch to enable/disable on/off tags
 * test Ensure that the formatter does take care of formatting tags by default
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=311582"
 */
public void testBug311582a() throws JavaModelException {
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"""
		public class X01 {
		
		/* disable-formatter */
		void     foo(    )      {\t
						//      unformatted       comment
		}
		/* enable-formatter */
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
			/* disable-formatter */
			void     foo(    )      {\t
							//      unformatted       comment
			}
			/* enable-formatter */
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug311582b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "off");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "");
	String source =
		"""
		/* off */
		public class X01 {
		void     foo(    )      {\t
						//      unformatted       area
		}
		}
		""";
	formatSource(source);
}

/**
 * bug 311617: [formatter] Master switch to enable/disable on/off tags
 * test Ensure that the formatter does not take care of formatting tags by default
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=311617"
 */
public void testBug311617() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		public class X01 {
		
		/* @formatter:off */
		void     foo(    )      {\t
						//      unformatted       comment
		}
		/* @formatter:on */
		void     bar(    )      {\t
						//      formatted       comment
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
			/* @formatter:off */
			void     foo(    )      {\t
							//      unformatted       comment
			}
			/* @formatter:on */
				void bar() {
					// formatted comment
				}
			}
			"""
	);
}
public void testBug311617b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	String source =
		"""
		/* @formatter:off */
		public class X01 {
		void     foo(    )      {\t
						//      unformatted       area
		}
		}
		""";
	formatSource(source);
}

/**
 * bug 313524: [formatter] Add preference for improved lines wrapping in nested method calls
 * test Ensure that the formatter keep previous eclipse versions behavior when
 * 		the "Try to keep nested expressions on one line" preference is set.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=313524"
 */
public void testBug313524_01() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	String source =
		"""
		public class X01 {
			void test() {
				foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8));
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				void test() {
					foo(bar(1, 2, 3, 4), bar(5, 6,
							7, 8));
				}
			}
			"""
	);
}
public void testBug313524_01b() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_WRAP_OUTER_EXPRESSIONS_WHEN_NESTED, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class X01 {
			void test() {
				foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8));
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				void test() {
					foo(bar(1, 2, 3, 4), bar(	5, 6,
												7,
												8));
				}
			}
			"""
	);
}
public void testBug313524_02() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	String source =
		"""
		public class X02 {
			void test() {
				foo(bar(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), bar(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));
			}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				void test() {
					foo(bar(1, 2, 3, 4, 5, 6, 7, 8,
							9, 10), bar(11, 12, 13,
									14, 15, 16, 17,
									18, 19, 20));
				}
			}
			"""
	);
}
public void testBug313524_02b() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_WRAP_OUTER_EXPRESSIONS_WHEN_NESTED, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class X02 {
			void test() {
				foo(bar(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), bar(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));
			}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				void test() {
					foo(bar(1, 2, 3, 4, 5, 6, 7, 8,
							9, 10), bar(11, 12, 13,
										14, 15, 16,
										17, 18, 19,
										20));
				}
			}
			"""
	);
}
public void testBug313524_03() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	String source =
		"""
		public class X03 {
			void test() {
				foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8), bar(9, 10, 11, 12));
			}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				void test() {
					foo(bar(1, 2, 3, 4), bar(5, 6,
							7, 8), bar(9, 10, 11,
									12));
				}
			}
			"""
	);
}
public void testBug313524_03b() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_WRAP_OUTER_EXPRESSIONS_WHEN_NESTED, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class X03 {
			void test() {
				foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8), bar(9, 10, 11, 12));
			}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				void test() {
					foo(bar(1, 2, 3, 4), bar(	5, 6,
												7,
												8),
						bar(9, 10, 11, 12));
				}
			}
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146175
public void testBug313524_146175() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class FormatterDemo {
		
		    public void fooBar() {
		        SomeOtherClass instanceOfOtherClass = new SomeOtherClass();
		
		        /* The following statement demonstrates the formatter issue */
		        SomeOtherClass.someMethodInInnerClass(
		            instanceOfOtherClass.anotherMethod("Value of paramter 1"),
		            instanceOfOtherClass.anotherMethod("Value of paramter 2"));
		
		    }
		
		    private static class SomeOtherClass {
		        public static void someMethodInInnerClass(
		            String param1,
		            String param2) {
		        }
		        public String anotherMethod(String par) {
		            return par;
		        }
		    }
		}
		""";
	formatSource(source,
		"""
			public class FormatterDemo {
			
				public void fooBar() {
					SomeOtherClass instanceOfOtherClass = new SomeOtherClass();
			
					/* The following statement demonstrates the formatter issue */
					SomeOtherClass.someMethodInInnerClass(instanceOfOtherClass
							.anotherMethod("Value of paramter 1"), instanceOfOtherClass
									.anotherMethod("Value of paramter 2"));
			
				}
			
				private static class SomeOtherClass {
					public static void someMethodInInnerClass(String param1,
							String param2) {
					}
			
					public String anotherMethod(String par) {
						return par;
					}
				}
			}
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=164093
public void testBug313524_164093_01() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "30");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_WRAP_OUTER_EXPRESSIONS_WHEN_NESTED, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class Test {
		    int someLongMethodName(int foo,  boolean bar, String yetAnotherArg) {
		        return 0;
		    }
		}
		""";
	formatSource(source,
		"""
			public class Test {
				int someLongMethodName(	int foo,
										boolean bar,
										String yetAnotherArg) {
					return 0;
				}
			}
			"""
	);
}
public void testBug313524_164093_02() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "55");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_WRAP_OUTER_EXPRESSIONS_WHEN_NESTED, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_SELECTOR_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"""
		public class X01 {
		    void foo() {
		           someIdentifier(someArg).someMethodName().someMethodName(foo, bar).otherMethod(arg0, arg1);
		    }
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			    void foo() {
			        someIdentifier(someArg).someMethodName()
			                               .someMethodName(foo,
			                                       bar)
			                               .otherMethod(arg0,
			                                       arg1);
			    }
			}
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203588
public void testBug313524_203588() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class Test {
		public void a()
		{
		  if(true)
		  {
		    allocation.add(idx_ta + 1, Double.valueOf(allocation.get(idx_ta).doubleValue() + q));
		  }
		}
		}
		""";
	formatSource(source,
		"""
			public class Test {
				public void a() {
					if (true) {
						allocation.add(idx_ta + 1, Double.valueOf(allocation.get(idx_ta)
								.doubleValue() + q));
					}
				}
			}
			"""
	);
}
// wksp1
public void testBug313524_wksp1_01() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X01 {
			private void reportError(String name) throws ParseError {
				throw new ParseError(MessageFormat.format(AntDTDSchemaMessages.getString("NfmParser.Ambiguous"), new String[]{name})); //$NON-NLS-1$
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				private void reportError(String name) throws ParseError {
					throw new ParseError(MessageFormat.format(AntDTDSchemaMessages
							.getString("NfmParser.Ambiguous"), new String[] { name })); //$NON-NLS-1$
				}
			}
			"""
	);
}
public void testBug313524_wksp1_02() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X02 {
			private void parseBuildFile(Project project) {
				if (!buildFile.exists()) {
					throw new BuildException(MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Buildfile__{0}_does_not_exist_!_1"), //$NON-NLS-1$
								 new String[]{buildFile.getAbsolutePath()}));
				}
				if (!buildFile.isFile()) {
					throw new BuildException(MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Buildfile__{0}_is_not_a_file_1"), //$NON-NLS-1$
									new String[]{buildFile.getAbsolutePath()}));
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				private void parseBuildFile(Project project) {
					if (!buildFile.exists()) {
						throw new BuildException(MessageFormat.format(InternalAntMessages
								.getString(
										"InternalAntRunner.Buildfile__{0}_does_not_exist_!_1"), //$NON-NLS-1$
								new String[] { buildFile.getAbsolutePath() }));
					}
					if (!buildFile.isFile()) {
						throw new BuildException(MessageFormat.format(InternalAntMessages
								.getString(
										"InternalAntRunner.Buildfile__{0}_is_not_a_file_1"), //$NON-NLS-1$
								new String[] { buildFile.getAbsolutePath() }));
					}
				}
			}
			"""
	);
}
public void testBug313524_wksp1_03() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X03 {
		
			protected void foo() {
				printTargets(project, subNames, null, InternalAntMessages.getString("InternalAntRunner.Subtargets__5"), 0); //$NON-NLS-1$
			}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
			
				protected void foo() {
					printTargets(project, subNames, null, InternalAntMessages.getString(
							"InternalAntRunner.Subtargets__5"), 0); //$NON-NLS-1$
				}
			}
			"""
	);
}
public void testBug313524_wksp1_04() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X04 {
			void foo() {
				if (AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {
					synchronizeOutlinePage(node, true);
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X04 {
				void foo() {
					if (AntUIPlugin.getDefault().getPreferenceStore().getBoolean(
							IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {
						synchronizeOutlinePage(node, true);
					}
				}
			}
			"""
	);
}
public void testBug313524_wksp1_05() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	String source =
		"""
		public class X05 {
		void foo() {
				if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {
				}
		}
		}
		""";
	formatSource(source,
		"""
			public class X05 {
				void foo() {
					if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(
							AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {
					}
				}
			}
			"""
	);
}
// TODO Improve this formatting as it let the message send argument in one line over the max width
public void testBug313524_wksp1_06() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X06 {
			public void launch() {
				try {
					if ((javaProject == null) || !javaProject.exists()) {
						abort(PDEPlugin________.getResourceString("JUnitLaunchConfig_____"), null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
					}
				} catch (CoreException e) {
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X06 {
				public void launch() {
					try {
						if ((javaProject == null) || !javaProject.exists()) {
							abort(PDEPlugin________.getResourceString(
									"JUnitLaunchConfig_____"), null,
									IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
						}
					} catch (CoreException e) {
					}
				}
			}
			"""
	);
}
public void testBug313524_wksp1_07() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X07 {
			void foo() {
				if (true) {
					configureAntObject(result, element, task, task.getTaskName(), InternalCoreAntMessages.getString("AntCorePreferences.No_library_for_task")); //$NON-NLS-1$
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X07 {
				void foo() {
					if (true) {
						configureAntObject(result, element, task, task.getTaskName(),
								InternalCoreAntMessages.getString(
										"AntCorePreferences.No_library_for_task")); //$NON-NLS-1$
					}
				}
			}
			"""
	);
}
public void testBug313524_wksp1_08() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X08 {
			public void foo() {
				if (true) {
					IStatus status= new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_RUNNING_BUILD, MessageFormat.format(InternalCoreAntMessages.getString("AntRunner.Already_in_progess"), new String[]{buildFileLocation}), null); //$NON-NLS-1$
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X08 {
				public void foo() {
					if (true) {
						IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE,
								AntCorePlugin.ERROR_RUNNING_BUILD, MessageFormat.format(
										InternalCoreAntMessages.getString(
												"AntRunner.Already_in_progess"), //$NON-NLS-1$
										new String[] { buildFileLocation }), null);
					}
				}
			}
			"""
	);
}
public void testBug313524_wksp1_09() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X09 {
			void foo() {
				if (true) {
					String secondFileName = secondDirectoryAbsolutePath + File.separator + currentFile.substring(firstDirectoryAbsolutePath.length() + 1);
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X09 {
				void foo() {
					if (true) {
						String secondFileName = secondDirectoryAbsolutePath + File.separator
								+ currentFile.substring(firstDirectoryAbsolutePath.length()
										+ 1);
					}
				}
			}
			"""
	);
}
public void testBug313524_wksp1_10() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X10 {
			void foo() {
				if (true) {
					if (true) {
						throw new BuildException(InternalAntMessages.getString("InternalAntRunner.Could_not_load_the_version_information._10")); //$NON-NLS-1$
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X10 {
				void foo() {
					if (true) {
						if (true) {
							throw new BuildException(InternalAntMessages.getString(
									"InternalAntRunner.Could_not_load_the_version_information._10")); //$NON-NLS-1$
						}
					}
				}
			}
			"""
	);
}
public void testBug313524_wksp1_11() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X11 {
			private void antFileNotFound() {
				reportError(AntLaunchConfigurationMessages.getString("AntLaunchShortcut.Unable"), null); //$NON-NLS-1$\t
			}
		}
		""";
	formatSource(source,
		"""
			public class X11 {
				private void antFileNotFound() {
					reportError(AntLaunchConfigurationMessages.getString(
							"AntLaunchShortcut.Unable"), null); //$NON-NLS-1$
				}
			}
			"""
	);
}
public void testBug313524_wksp1_12() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"""
		public class X12 {
			void foo() {
		        if (this.fTests.size() == 0) {
		            this.addTest(TestSuite
		                    .warning("No tests found in " + theClass.getName())); //$NON-NLS-1$
		        }
			}
		}
		""";
	formatSource(source,
		"""
			public class X12 {
				void foo() {
					if (this.fTests.size() == 0) {
						this.addTest(TestSuite.warning("No tests found in " + theClass //$NON-NLS-1$
								.getName()));
					}
				}
			}
			"""
	);
}

/**
 * bug 317039: [formatter] Code Formatter fails on inner class source indentation
 * test Ensure formatter is stable when 'Never Join Lines' preference is checked
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=317039"
 */
public void testBug317039_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class X01
		  {
		
		    public void innerThread()
		      {
		
		        new Thread(new Runnable()
		          {
		            @Override
		            public void run()
		              {
		                // TODO Auto-generated method stub
		                }
		            }).start();
		        }
		    }
		""";
	formatSource(source,
		"""
			public class X01 {
			
				public void innerThread() {
			
					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
						}
					}).start();
				}
			}
			"""
	);
}

/**
 * bug 320754: [formatter] formatter:off/on tags does not work correctly
 * test Ensure disabling/enabling tags work properly around annotations
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=320754"
 */
public void testBug320754_00() throws JavaModelException {
	String source =
		"""
		public class X00
		{
		    public static void main(String[] args)
		    {
		        int a=0;int b;
		
		        System.out.println(a);
		
		    }
		}
		""";
	formatSource(source,
		"""
			public class X00 {
				public static void main(String[] args) {
					int a = 0;
					int b;
			
					System.out.println(a);
			
				}
			}
			"""
	);
}
public void testBug320754_01a() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		//@formatter:off
		//@formatter:on
		public class X01a
		{
		    public static void main(String[] args)
		    {
		        int a=0;int b;
		
		        System.out.println(a);
		
		    }
		}
		""";
	formatSource(source,
		"""
			//@formatter:off
			//@formatter:on
			public class X01a {
				public static void main(String[] args) {
					int a = 0;
					int b;
			
					System.out.println(a);
			
				}
			}
			"""
	);
}
public void testBug320754_01b() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		/* @formatter:off */
		/* @formatter:on */
		public class X01b
		{
		    public static void main(String[] args)
		    {
		        int a=0;int b;
		
		        System.out.println(a);
		
		    }
		}
		""";
	formatSource(source,
		"""
			/* @formatter:off */
			/* @formatter:on */
			public class X01b {
				public static void main(String[] args) {
					int a = 0;
					int b;
			
					System.out.println(a);
			
				}
			}
			"""
	);
}
public void testBug320754_01c() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		/** @formatter:off */
		/** @formatter:on */
		public class X01c
		{
		    public static void main(String[] args)
		    {
		        int a=0;int b;
		
		        System.out.println(a);
		
		    }
		}
		""";
	formatSource(source,
		"""
			/** @formatter:off */
			/** @formatter:on */
			public class X01c {
				public static void main(String[] args) {
					int a = 0;
					int b;
			
					System.out.println(a);
			
				}
			}
			"""
	);
}
public void testBug320754_02a() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		//@formatter:off
		@MyAnnot (
		  testAttribute = {"test1", "test2", "test3"}
		)
		//@formatter:on
		public class X02
		{
		    public static void main(String[] args)
		    {
		        int a=0;int b;
		
		        System.out.println(a);
		
		    }
		}
		""";
	formatSource(source,
		"""
			//@formatter:off
			@MyAnnot (
			  testAttribute = {"test1", "test2", "test3"}
			)
			//@formatter:on
			public class X02 {
				public static void main(String[] args) {
					int a = 0;
					int b;
			
					System.out.println(a);
			
				}
			}
			"""
	);
}
public void testBug320754_02b() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		/* @formatter:off */
		@MyAnnot (
		  testAttribute = {"test1", "test2", "test3"}
		)
		/* @formatter:on */
		public class X02b
		{
		    public static void main(String[] args)
		    {
		        int a=0;int b;
		
		        System.out.println(a);
		
		    }
		}
		""";
	formatSource(source,
		"""
			/* @formatter:off */
			@MyAnnot (
			  testAttribute = {"test1", "test2", "test3"}
			)
			/* @formatter:on */
			public class X02b {
				public static void main(String[] args) {
					int a = 0;
					int b;
			
					System.out.println(a);
			
				}
			}
			"""
	);
}
public void testBug320754_02c() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		/** @formatter:off */
		@MyAnnot (
		  testAttribute = {"test1", "test2", "test3"}
		)
		/** @formatter:on */
		public class X02c
		{
		    public static void main(String[] args)
		    {
		        int a=0;int b;
		
		        System.out.println(a);
		
		    }
		}
		""";
	formatSource(source,
		"""
			/** @formatter:off */
			@MyAnnot (
			  testAttribute = {"test1", "test2", "test3"}
			)
			/** @formatter:on */
			public class X02c {
				public static void main(String[] args) {
					int a = 0;
					int b;
			
					System.out.println(a);
			
				}
			}
			"""
	);
}
public void testBug320754_02d() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		//@formatter:off
		@MyAnnot (
		  testAttribute = {"test1", "test2", "test3"}
		)
		
		//@formatter:on
		public class X02d
		{
		    public static void main(String[] args)
		    {
		        int a=0;int b;
		
		        System.out.println(a);
		
		    }
		}
		""";
	formatSource(source,
		"""
			//@formatter:off
			@MyAnnot (
			  testAttribute = {"test1", "test2", "test3"}
			)
			
			//@formatter:on
			public class X02d {
				public static void main(String[] args) {
					int a = 0;
					int b;
			
					System.out.println(a);
			
				}
			}
			"""
	);
}
public void testBug320754_03() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		//@formatter:off
		
		//@formatter:on
		public class X03
		{
		    public static void main(String[] args)
		    {
		        int a=0;int b;
		
		        System.out.println(a);
		
		    }
		}
		""";
	formatSource(source,
		"""
			//@formatter:off
			
			//@formatter:on
			public class X03 {
				public static void main(String[] args) {
					int a = 0;
					int b;
			
					System.out.println(a);
			
				}
			}
			"""
	);
}

/**
 * bug 328240: org.eclipse.text.edits.MalformedTreeException: Overlapping text edits
 * test Ensure that no exception occurs while formatting the given sample
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=328240"
 */
public void testBug328240() {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		package com.example;
		
		public class FormatterError {
		
			/**
			 * Create a paragraph element suited to be a header of the report. Headers
			 * are the elements such as "created by" on "created on" that appear
			 * underneath the title.
			 *\s
			 * @param reportHeader
			 *            a <code>String</coe> value that will be the text of
		
		* the paragraph.
			 * @return a <code>Paragraph</code> containing the the text passed as the
			 *         reportHeader parameter.
			 */
		
			public static String createReportHeader(String reportHeader) {
				return reportHeader;
			}
		}
		""";
	formatSource(source,
		"""
			package com.example;
			
			public class FormatterError {
			
				/**
				 * Create a paragraph element suited to be a header of the report. Headers
				 * are the elements such as "created by" on "created on" that appear
				 * underneath the title.
				 *\s
				 * @param reportHeader
				 *            a <code>String</coe> value that will be the text of
			\t
				* the paragraph.
				 * @return a <code>Paragraph</code> containing the the text passed as the
				 *            reportHeader parameter.
				 */
			
				public static String createReportHeader(String reportHeader) {
					return reportHeader;
				}
			}
			"""
	);
}

/**
 * bug 328362: [formatter] Format regions does not format as expected
 * test Ensure that the given regions are well formatted
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=328362"
 */
public void testBug328362() throws Exception {
	String source =
		"""
		package test1;
		
		[#    class  A {#]
		
		[#        int  i;#]
		
		}
		""";
	formatSource(source,
		"""
			package test1;
			
			class A {
			
				int i;
			
			}
			"""
	);
}

/**
 * bug 330313: [formatter] 'Never join already wrapped lines' formatter option does correctly indent
 * test Ensure that indentation is correct when 'Never join already wrapped lines' is set
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=330313"
 */
public void testBug330313() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
			private void helper2(
					 boolean[] booleans) {
				if (booleans[0]) {
		
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
				private void helper2(
						boolean[] booleans) {
					if (booleans[0]) {
			
					}
				}
			}
			"""
	);
}
public void testBug330313a() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
			private void helper2(
					boolean[] booleans) {
				if (booleans[0]) {
		
				}
			}
		}
		""";
	formatSource(source);
}
public void testBug330313b() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
			private void helper2(
		                boolean[] booleans) {
				if (booleans[0]) {
		
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
				private void helper2(
						boolean[] booleans) {
					if (booleans[0]) {
			
					}
				}
			}
			"""
	);
}
public void testBug330313c() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
			private void helper2(
		boolean[] booleans) {
				if (booleans[0]) {
		
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
				private void helper2(
						boolean[] booleans) {
					if (booleans[0]) {
			
					}
				}
			}
			"""
	);
}
public void testBug330313d() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {
			private void helper2(
					boolean[] booleans) {
				if (booleans[0]) {
		
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
				private void helper2(
						boolean[] booleans) {
					if (booleans[0]) {
			
					}
				}
			}
			"""
	);
}
public void testBug330313_regression_187a() {
	setPageWidth80();
	String source =
		"""
		import java.io.File;
		
		public class RegressionTest_187 {
		
			private String createC42PDFCommandLine(String documentName) {
				return (Registry.getConvertToolPath() + File.separator +
					   Registry.getConvertToolName() +
					   " -o " + _workingDir + File.separator + documentName +
					   " -l " + _workingDir + File.separator + _fileList);
			}
		}
		""";
	formatSource(source,
		"""
			import java.io.File;
			
			public class RegressionTest_187 {
			
				private String createC42PDFCommandLine(String documentName) {
					return (Registry.getConvertToolPath() + File.separator
							+ Registry.getConvertToolName() + " -o " + _workingDir
							+ File.separator + documentName + " -l " + _workingDir
							+ File.separator + _fileList);
				}
			}
			"""
	);
}
public void testBug330313_regression_187b() {
	setPageWidth80();
	String source =
		"""
		import java.io.File;
		
		public class RegressionTest_187 {
		
			private String createC42PDFCommandLine(String documentName) {
				return (Registry.getConvertToolPath() + File.separator +
					   Registry.getConvertToolName() +
					   (" -o " + _workingDir + File.separator + documentName +
					   (" -l " + _workingDir + File.separator + _fileList)));
			}
		}
		""";
	formatSource(source,
		"""
			import java.io.File;
			
			public class RegressionTest_187 {
			
				private String createC42PDFCommandLine(String documentName) {
					return (Registry.getConvertToolPath() + File.separator
							+ Registry.getConvertToolName()
							+ (" -o " + _workingDir + File.separator + documentName
									+ (" -l " + _workingDir + File.separator + _fileList)));
				}
			}
			"""
	);
}
//static { TESTS_PREFIX = "testBug330313_wksp1"; }
public void testBug330313_wksp1_01_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X01 {
		
		    protected String getPrefixFromDocument(String aDocumentText, int anOffset) {
		        int startOfWordToken = anOffset;
		       \s
		        char token= \'a\';
		        if (startOfWordToken > 0) {
					token= aDocumentText.charAt(startOfWordToken - 1);
		        }
		       \s
		        while (startOfWordToken > 0\s
		                && (Character.isJavaIdentifierPart(token)\s
		                    || \'.\' == token
							|| \'-\' == token
		        			|| \';\' == token)
		                && !(\'$\' == token)) {
		            startOfWordToken--;
		            if (startOfWordToken == 0) {
		            	break; //word goes right to the beginning of the doc
		            }
					token= aDocumentText.charAt(startOfWordToken - 1);
		        }
		        return "";
		    }
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X01 {
			
				protected String getPrefixFromDocument(String aDocumentText, int anOffset) {
					int startOfWordToken = anOffset;
			
					char token = \'a\';
					if (startOfWordToken > 0) {
						token = aDocumentText.charAt(startOfWordToken - 1);
					}
			
					while (startOfWordToken > 0
							&& (Character.isJavaIdentifierPart(token)
									|| \'.\' == token
									|| \'-\' == token
									|| \';\' == token)
							&& !(\'$\' == token)) {
						startOfWordToken--;
						if (startOfWordToken == 0) {
							break; // word goes right to the beginning of the doc
						}
						token = aDocumentText.charAt(startOfWordToken - 1);
					}
					return "";
				}
			}
			"""
	);
}
public void testBug330313_wksp1_02_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X02 {
		  public void testMethod(String currentTokenVal,
		                         int[][] expectedTokenSequencesVal,
		                         String[] tokenImageVal
		                        )
		  {
		  }
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X02 {
				public void testMethod(String currentTokenVal,
						int[][] expectedTokenSequencesVal,
						String[] tokenImageVal) {
				}
			}
			"""
	);
}
public void testBug330313_wksp1_03_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X03 {
		
			void foo() {
					if (declaringClass.isNestedType()){
						NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;
						this.scope.extraSyntheticArguments = nestedType.syntheticOuterLocalVariables();
						scope.computeLocalVariablePositions(// consider synthetic arguments if any
							nestedType.enclosingInstancesSlotSize + 1,
							codeStream);
						argSlotSize += nestedType.enclosingInstancesSlotSize;
						argSlotSize += nestedType.outerLocalVariablesSlotSize;
					} else {
						scope.computeLocalVariablePositions(1,  codeStream);
					}
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X03 {
			
				void foo() {
					if (declaringClass.isNestedType()) {
						NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;
						this.scope.extraSyntheticArguments = nestedType
								.syntheticOuterLocalVariables();
						scope.computeLocalVariablePositions(// consider synthetic arguments
															// if any
								nestedType.enclosingInstancesSlotSize + 1,
								codeStream);
						argSlotSize += nestedType.enclosingInstancesSlotSize;
						argSlotSize += nestedType.outerLocalVariablesSlotSize;
					} else {
						scope.computeLocalVariablePositions(1, codeStream);
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_04() {
	String source =
		"""
		package wksp1;
		
		public class X04 {
		
			void foo() {
				for (;;) {
					if (act <= NUM_RULES) {               // reduce action
						tempStackTop--;
					} else if (act < ACCEPT_ACTION ||     // shift action
							 act > ERROR_ACTION) {        // shift-reduce action
						if (indx == MAX_DISTANCE)
							return indx;
						indx++;
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X04 {
			
				void foo() {
					for (;;) {
						if (act <= NUM_RULES) { // reduce action
							tempStackTop--;
						} else if (act < ACCEPT_ACTION || // shift action
								act > ERROR_ACTION) { // shift-reduce action
							if (indx == MAX_DISTANCE)
								return indx;
							indx++;
						}
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_04_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X04 {
		
			void foo() {
				for (;;) {
					if (act <= NUM_RULES) {               // reduce action
						tempStackTop--;
					} else if (act < ACCEPT_ACTION ||     // shift action
							 act > ERROR_ACTION) {        // shift-reduce action
						if (indx == MAX_DISTANCE)
							return indx;
						indx++;
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X04 {
			
				void foo() {
					for (;;) {
						if (act <= NUM_RULES) { // reduce action
							tempStackTop--;
						} else if (act < ACCEPT_ACTION || // shift action
								act > ERROR_ACTION) { // shift-reduce action
							if (indx == MAX_DISTANCE)
								return indx;
							indx++;
						}
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_05_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"""
		package wksp1;
		
		public class X05 {
		
			private void foo() {
				setBuildFileLocation.invoke(runner, new Object[] { buildFileLocation });
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X05
			{
			
				private void foo()
				{
					setBuildFileLocation.invoke(runner, new Object[]
					{ buildFileLocation });
				}
			}
			"""
	);
}
public void testBug330313_wksp1_06_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X06 {
		
			public void foo(Object index) {
		
				try {
					index = this.manager.getIndexForUpdate(this.containerPath, true, /*reuse index file*/ true /*create if none*/);
				}
				finally {}
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X06
			{
			
				public void foo(Object index)
				{
			
					try
					{
						index = this.manager.getIndexForUpdate(this.containerPath, true,
								/* reuse index file */ true /* create if none */);
					} finally
					{
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_07() {
	String source =
		"""
		package wksp1;
		
		public class X07 {
		
		static final long[] jjtoToken = {
		   0x7fbfecffL,\s
		};
		static final long[] jjtoSkip = {
		   0x400000L,\s
		};
		
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X07 {
			
				static final long[] jjtoToken = { 0x7fbfecffL, };
				static final long[] jjtoSkip = { 0x400000L, };
			
			}
			"""
	);
}
public void testBug330313_wksp1_07_bnl() {
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"""
		package wksp1;
		
		public class X07 {
		
		static final long[] jjtoToken = {
		   0x7fbfecffL,\s
		};
		static final long[] jjtoSkip = {
		   0x400000L,\s
		};
		
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X07
			{
			
				static final long[] jjtoToken =
				{ 0x7fbfecffL, };
				static final long[] jjtoSkip =
				{ 0x400000L, };
			
			}
			"""
	);
}
public void testBug330313_wksp1_07_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X07 {
		
		static final long[] jjtoToken = {
		   0x7fbfecffL,\s
		};
		static final long[] jjtoSkip = {
		   0x400000L,\s
		};
		
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X07 {
			
				static final long[] jjtoToken = {
						0x7fbfecffL,
				};
				static final long[] jjtoSkip = {
						0x400000L,
				};
			
			}
			"""
	);
}
public void testBug330313_wksp1_07_njl_bnl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"""
		package wksp1;
		
		public class X07 {
		
		static final long[] jjtoToken = {
		   0x7fbfecffL,\s
		};
		static final long[] jjtoSkip = {
		   0x400000L,\s
		};
		
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X07
			{
			
				static final long[] jjtoToken =
				{
						0x7fbfecffL,
				};
				static final long[] jjtoSkip =
				{
						0x400000L,
				};
			
			}
			"""
	);
}
public void testBug330313_wksp1_08_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_assignment = Alignment.M_COMPACT_SPLIT;
	String source =
		"""
		package wksp1;
		
		public class X08 {
		
			void foo() {
				MinimizedFileSystemElement dummyParent =
					new MinimizedFileSystemElement("", null, true);//$NON-NLS-1$
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X08 {
			
				void foo() {
					MinimizedFileSystemElement dummyParent =
							new MinimizedFileSystemElement("", null, true);//$NON-NLS-1$
				}
			}
			"""
	);
}
// testCompare1159_1: org.eclipse.debug.internal.ui.DebugUIPropertiesAdapterFactory
public void testBug330313_wksp1_09_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X09 {
			public Class[] getAdapterList() {
				return new Class[] {
					IWorkbenchAdapter.class
				};
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X09 {
				public Class[] getAdapterList() {
					return new Class[] {
							IWorkbenchAdapter.class
					};
				}
			}
			"""
	);
}
// testCompare1723_1: org.eclipse.jdt.internal.compiler.ast.DoubleLiteral
public void testBug330313_wksp1_10_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X10 {
		
		public void computeConstant() {
		
			if (true)
			{	//only a true 0 can be made of zeros
				//2.00000000000000000e-324 is illegal ....\s
			}}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X10 {
			
				public void computeConstant() {
			
					if (true) { // only a true 0 can be made of zeros
								// 2.00000000000000000e-324 is illegal ....
					}
				}
			}
			"""
	);
}
// testCompare1794_1: org.eclipse.jdt.internal.compiler.ast.ClassFile
public void testBug330313_wksp1_11_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X11 {
			X11() {
				accessFlags
					&= ~(
						AccStrictfp
							| AccProtected
							| AccPrivate
							| AccStatic
							| AccSynchronized
							| AccNative);
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X11 {
				X11() {
					accessFlags &= ~(AccStrictfp
							| AccProtected
							| AccPrivate
							| AccStatic
							| AccSynchronized
							| AccNative);
				}
			}
			"""
	);
}
// rg.eclipse.ant.ui/Ant Editor/org/eclipse/ant/internal/ui/editor/formatter/XmlFormatter.java
public void testBug330313_wksp1_12() {
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X12 {
		
		    private static Document createDocument(String string, Position[] positions) throws IllegalArgumentException {
				Document doc= new Document(string);
				try {
					if (positions != null) {
						doc.addPositionUpdater(new DefaultPositionUpdater(POS_CATEGORY) {
							protected boolean notDeleted() {
								if (fOffset < fPosition.offset && (fPosition.offset + fPosition.length < fOffset + fLength)) {
									return false;
								}
								return true;
							}
						});
					}
				} catch (BadPositionCategoryException cannotHappen) {
					// can not happen: category is correctly set up
				}
				return doc;
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X12 {
			
				private static Document createDocument(String string, Position[] positions)
						throws IllegalArgumentException {
					Document doc = new Document(string);
					try {
						if (positions != null) {
							doc.addPositionUpdater(
									new DefaultPositionUpdater(POS_CATEGORY) {
										protected boolean notDeleted() {
											if (fOffset < fPosition.offset
													&& (fPosition.offset
															+ fPosition.length < fOffset
																	+ fLength)) {
												return false;
											}
											return true;
										}
									});
						}
					} catch (BadPositionCategoryException cannotHappen) {
						// can not happen: category is correctly set up
					}
					return doc;
				}
			}
			"""
	);
}
public void testBug330313_wksp1_12_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X12 {
		
		    private static Document createDocument(String string, Position[] positions) throws IllegalArgumentException {
				Document doc= new Document(string);
				try {
					if (positions != null) {
						doc.addPositionUpdater(new DefaultPositionUpdater(POS_CATEGORY) {
							protected boolean notDeleted() {
								if (fOffset < fPosition.offset && (fPosition.offset + fPosition.length < fOffset + fLength)) {
									return false;
								}
								return true;
							}
						});
					}
				} catch (BadPositionCategoryException cannotHappen) {
					// can not happen: category is correctly set up
				}
				return doc;
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X12 {
			
				private static Document createDocument(String string, Position[] positions)
						throws IllegalArgumentException {
					Document doc = new Document(string);
					try {
						if (positions != null) {
							doc.addPositionUpdater(
									new DefaultPositionUpdater(POS_CATEGORY) {
										protected boolean notDeleted() {
											if (fOffset < fPosition.offset
													&& (fPosition.offset
															+ fPosition.length < fOffset
																	+ fLength)) {
												return false;
											}
											return true;
										}
									});
						}
					} catch (BadPositionCategoryException cannotHappen) {
						// can not happen: category is correctly set up
					}
					return doc;
				}
			}
			"""
	);
}
// Test case extracted from org.eclipse.ant.ui/org/eclipse/core/internal/dtree/NodeInfo.java
public void testBug330313_wksp1_13() {
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X13 {
		
			public boolean isEmptyDelta() {
				return (this.getType() == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE && this.getNamesOfChildren().length == 0 && this.getNamesOfDeletedChildren().length == 0);
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X13 {
			
				public boolean isEmptyDelta() {
					return (this.getType() == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE
							&& this.getNamesOfChildren().length == 0
							&& this.getNamesOfDeletedChildren().length == 0);
				}
			}
			"""
	);
}
public void testBug330313_wksp1_13_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X13 {
		
			public boolean isEmptyDelta() {
				return (this.getType() == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE && this.getNamesOfChildren().length == 0 && this.getNamesOfDeletedChildren().length == 0);
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X13 {
			
				public boolean isEmptyDelta() {
					return (this.getType() == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE
							&& this.getNamesOfChildren().length == 0
							&& this.getNamesOfDeletedChildren().length == 0);
				}
			}
			"""
	);
}
// Test case extracted from org.eclipse.jdt.core/org/eclipse/jdt/internal/compiler/ast/SingleNameReference.java
public void testBug330313_wksp1_14() {
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X14 {
			public void foo() {
				if (true) {
					if (((bits & DepthMASK) != 0)
						&& (fieldBinding.isPrivate() // private access
							|| (fieldBinding.isProtected() // implicit protected access
									&& fieldBinding.declaringClass.getPackage()\s
										!= currentScope.enclosingSourceType().getPackage()))) {
						return;
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X14 {
				public void foo() {
					if (true) {
						if (((bits & DepthMASK) != 0) && (fieldBinding.isPrivate() // private
																					// access
								|| (fieldBinding.isProtected() // implicit protected access
										&& fieldBinding.declaringClass
												.getPackage() != currentScope
														.enclosingSourceType()
														.getPackage()))) {
							return;
						}
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_14_njl() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X14 {
			public void foo() {
				if (true) {
					if (((bits & DepthMASK) != 0)
						&& (fieldBinding.isPrivate() // private access
							|| (fieldBinding.isProtected() // implicit protected access
									&& fieldBinding.declaringClass.getPackage()\s
										!= currentScope.enclosingSourceType().getPackage()))) {
						return;
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X14 {
				public void foo() {
					if (true) {
						if (((bits & DepthMASK) != 0)
								&& (fieldBinding.isPrivate() // private access
										|| (fieldBinding.isProtected() // implicit protected
																		// access
												&& fieldBinding.declaringClass.getPackage() != currentScope.enclosingSourceType()
														.getPackage()))) {
							return;
						}
					}
				}
			}
			"""
	);
}
// Test case extracted from org.eclipse.jdt.core/org/eclipse/jdt/internal/compiler/ast/SingleNameReference.java
public void testBug330313_wksp1_15_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X15 {
			public void foo() {
				if (true) {
					if (fieldBinding.declaringClass != this.actualReceiverType
						&& !this.actualReceiverType.isArrayType()\t
						&& fieldBinding.declaringClass != null
						&& fieldBinding.constant == NotAConstant
						&& ((currentScope.environment().options.targetJDK >= ClassFileConstants.JDK1_2\s
								&& !fieldBinding.isStatic()
								&& fieldBinding.declaringClass.id != T_Object) // no change for Object fields (if there was any)
							|| !fieldBinding.declaringClass.canBeSeenBy(currentScope))){
						this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)this.actualReceiverType);
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X15 {
				public void foo() {
					if (true) {
						if (fieldBinding.declaringClass != this.actualReceiverType
								&& !this.actualReceiverType.isArrayType()
								&& fieldBinding.declaringClass != null
								&& fieldBinding.constant == NotAConstant
								&& ((currentScope
										.environment().options.targetJDK >= ClassFileConstants.JDK1_2
										&& !fieldBinding.isStatic()
										&& fieldBinding.declaringClass.id != T_Object) // no
																						// change
																						// for
																						// Object
																						// fields
																						// (if
																						// there
																						// was
																						// any)
										|| !fieldBinding.declaringClass
												.canBeSeenBy(currentScope))) {
							this.codegenBinding = currentScope.enclosingSourceType()
									.getUpdatedFieldBinding(fieldBinding,
											(ReferenceBinding) this.actualReceiverType);
						}
					}
				}
			}
			"""
	);
}
// Test case 1941_1 (extracted from org.eclipse.jdt.core/org/eclipse/jdt/internal/compiler/ast/Parser.java)
public void testBug330313_wksp1_16() {
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X16 {
		void foo() {
			// recovery
			if (this.currentElement != null) {
				if (!(this.currentElement instanceof RecoveredType)
					&& (this.currentToken == TokenNameDOT
						//|| declaration.modifiers != 0
						|| (this.scanner.getLineNumber(declaration.type.sourceStart)
								!= this.scanner.getLineNumber((int) (namePosition >>> 32))))){
					return;
				}
			}
		}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X16 {
				void foo() {
					// recovery
					if (this.currentElement != null) {
						if (!(this.currentElement instanceof RecoveredType)
								&& (this.currentToken == TokenNameDOT
										// || declaration.modifiers != 0
										|| (this.scanner.getLineNumber(
												declaration.type.sourceStart) != this.scanner
														.getLineNumber(
																(int) (namePosition >>> 32))))) {
							return;
						}
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_16_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X16 {
		void foo() {
			// recovery
			if (this.currentElement != null) {
				if (!(this.currentElement instanceof RecoveredType)
					&& (this.currentToken == TokenNameDOT
						//|| declaration.modifiers != 0
						|| (this.scanner.getLineNumber(declaration.type.sourceStart)
								!= this.scanner.getLineNumber((int) (namePosition >>> 32))))){
					return;
				}
			}
		}
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public class X16 {
				void foo() {
					// recovery
					if (this.currentElement != null) {
						if (!(this.currentElement instanceof RecoveredType)
								&& (this.currentToken == TokenNameDOT
										// || declaration.modifiers != 0
										|| (this.scanner.getLineNumber(
												declaration.type.sourceStart) != this.scanner
														.getLineNumber(
																(int) (namePosition >>> 32))))) {
							return;
						}
					}
				}
			}
			"""
	);
}
// Test case 1872_1 (extracted from org.eclipse.jdt.core/org/eclipse/jdt/internal/compiler/lookup/BlockScope.java)
public void testBug330313_wksp1_17_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X17 {
			void foo() {
				if ((currentMethodScope = this.methodScope())
					!= outerLocalVariable.declaringScope.methodScope()) {
					return;
				}
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X17 {
				void foo() {
					if ((currentMethodScope = this.methodScope()) != outerLocalVariable.declaringScope.methodScope()) {
						return;
					}
				}
			}
			"""
	);
}
// Test case 1964_1 (extracted from org.eclipse.jdt.core/org/eclipse/jdt/core/dom/ASTMatcher.java)
public void testBug330313_wksp1_18_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X18 {
			public boolean foo() {
				return (
					safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
						&& safeSubtreeListMatch(node.modifiers(), o.modifiers())
						&& safeSubtreeMatch(node.getName(), o.getName())
						&& safeSubtreeListMatch(node.arguments(), o.arguments())
						&& safeSubtreeListMatch(
							node.bodyDeclarations(),
							o.bodyDeclarations()));
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X18 {
				public boolean foo() {
					return (safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
							&& safeSubtreeListMatch(node.modifiers(), o.modifiers())
							&& safeSubtreeMatch(node.getName(), o.getName())
							&& safeSubtreeListMatch(node.arguments(), o.arguments())
							&& safeSubtreeListMatch(
									node.bodyDeclarations(),
									o.bodyDeclarations()));
				}
			}
			"""
	);
}
public void testBug330313_wksp1_19_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X19 {
			public boolean foo() {
				return (
					safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
						&& safeSubtreeListMatch(node.modifiers(), o.modifiers())
						&& safeSubtreeMatch(node.getName(), o.getName())
						&& safeSubtreeListMatch(node.superInterfaceTypes(), o.superInterfaceTypes())
						&& safeSubtreeListMatch(
							node.bodyDeclarations(),
							o.bodyDeclarations()));
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X19 {
				public boolean foo() {
					return (safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
							&& safeSubtreeListMatch(node.modifiers(), o.modifiers())
							&& safeSubtreeMatch(node.getName(), o.getName())
							&& safeSubtreeListMatch(node.superInterfaceTypes(),
									o.superInterfaceTypes())
							&& safeSubtreeListMatch(
									node.bodyDeclarations(),
									o.bodyDeclarations()));
				}
			}
			"""
	);
}
// Test case extracted from org.eclipse.debug.ui/ui/org/eclipse/debug/ui/AbstractDebugView.java
public void testBug330313_wksp1_20_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_assignment = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X20 {
		
		  static final String decode(String entity) {
		    if (true) {
		      if (entity.charAt(2) == \'X\' || entity.charAt(2) == \'x\') {
		      }
		      Character c =
			new Character((char)Integer.parseInt(entity.substring(start), radix));
		      return c.toString();
		    }
			return "";
		  }
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X20 {
			
				static final String decode(String entity) {
					if (true) {
						if (entity.charAt(2) == \'X\' || entity.charAt(2) == \'x\') {
						}
						Character c =
								new Character((char) Integer
										.parseInt(entity.substring(start), radix));
						return c.toString();
					}
					return "";
				}
			}
			"""
	);
}
// Test case extracted from org.apache.lucene/src/org/apache/lucene/demo/html/Entities.java
public void testBug330313_wksp1_21_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X21 {
			public boolean isAvailable() {
				return !(getViewer() == null || getViewer().getControl() == null || getViewer().getControl().isDisposed());
			}\t
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X21 {
				public boolean isAvailable() {
					return !(getViewer() == null || getViewer().getControl() == null
							|| getViewer().getControl().isDisposed());
				}
			}
			"""
	);
}
// Test case extracted from differences noticed with patch v27.txt
public void testBug330313_wksp1_22_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X22 {
			public boolean foo() {
				return (
						(node.isInterface() == o.isInterface())
						&& safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
						&& safeSubtreeMatch(node.getName(), o.getName())
						&& safeSubtreeListMatch(node.bodyDeclarations(), o.bodyDeclarations()));
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X22 {
				public boolean foo() {
					return ((node.isInterface() == o.isInterface())
							&& safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
							&& safeSubtreeMatch(node.getName(), o.getName())
							&& safeSubtreeListMatch(node.bodyDeclarations(),
									o.bodyDeclarations()));
				}
			}
			"""
	);
}
public void testBug330313_wksp1_23_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X23 {
			void foo() {
				boolean wasError = IMarker.SEVERITY_ERROR == pb.getAttribute(IMarker.SEVERITY,
						IMarker.SEVERITY_ERROR);
			}
		}
		""";
	formatSource(source	);
}
public void testBug330313_wksp1_24_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X24 {
		
			protected boolean canRunEvaluation() {
				// NOTE similar to #canStep, except a quiet suspend state is OK
				try {
					return isSuspendedQuiet() || (isSuspended()
							&& !(isPerformingEvaluation() || isInvokingMethod())
							&& !isStepping()
							&& getTopStackFrame() != null
							&& !getJavaDebugTarget().isPerformingHotCodeReplace());
				} catch (DebugException e) {
					return false;
				}
			}
		}
		""";
	formatSource(source	);
}
public void testBug330313_wksp1_25_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X25 {
		
			void unloadIcon(ImageData icon) {
				int sizeImage = (((icon.width * icon.depth + 31) / 32 * 4) +
						((icon.width + 31) / 32 * 4)) * icon.height;
			}
		}
		""";
	formatSource(source	);
}
public void testBug330313_wksp1_26_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X26 {
		
		void foo() {
			for (int i = 0; i < data.length; i++) {
				byte s = data[i];
				sourceData[i] = (byte)(((s & 0x80) >> 7) |
					((s & 0x40) >> 5) |
					((s & 0x20) >> 3) |
					((s & 0x10) >> 1) |
					((s & 0x08) << 1) |
					((s & 0x04) << 3) |
					((s & 0x02) << 5) |
					((s & 0x01) << 7));
			}
		}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X26 {
			
				void foo() {
					for (int i = 0; i < data.length; i++) {
						byte s = data[i];
						sourceData[i] = (byte) (((s & 0x80) >> 7) |
								((s & 0x40) >> 5) |
								((s & 0x20) >> 3) |
								((s & 0x10) >> 1) |
								((s & 0x08) << 1) |
								((s & 0x04) << 3) |
								((s & 0x02) << 5) |
								((s & 0x01) << 7));
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_27_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X27 {
			private void foo() {
		
				if (_VerificationResult.getVerificationCode()
					== IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED
					|| _VerificationResult.getVerificationCode()
						== IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED) {
					// Group box
				}
			}
		
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X27 {
				private void foo() {
			
					if (_VerificationResult.getVerificationCode() == IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED
							|| _VerificationResult.getVerificationCode() == IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED) {
						// Group box
					}
				}
			
			}
			"""
	);
}
public void testBug330313_wksp1_28_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X28 {
		
			void foo() {
				if (fieldBinding.declaringClass != lastReceiverType
					&& !lastReceiverType.isArrayType()		\t
					&& fieldBinding.declaringClass != null
					&& fieldBinding.constant == NotAConstant
					&& ((currentScope.environment().options.targetJDK >= ClassFileConstants.JDK1_2
							&& (fieldBinding != this.binding || this.indexOfFirstFieldBinding > 1 || !fieldBinding.isStatic())
							&& fieldBinding.declaringClass.id != T_Object)
						|| !(useDelegate
								? new CodeSnippetScope(currentScope).canBeSeenByForCodeSnippet(fieldBinding.declaringClass, (ReferenceBinding) this.delegateThis.type)
								: fieldBinding.declaringClass.canBeSeenBy(currentScope)))){
					// code
				}
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X28 {
			
				void foo() {
					if (fieldBinding.declaringClass != lastReceiverType
							&& !lastReceiverType.isArrayType()
							&& fieldBinding.declaringClass != null
							&& fieldBinding.constant == NotAConstant
							&& ((currentScope
									.environment().options.targetJDK >= ClassFileConstants.JDK1_2
									&& (fieldBinding != this.binding
											|| this.indexOfFirstFieldBinding > 1
											|| !fieldBinding.isStatic())
									&& fieldBinding.declaringClass.id != T_Object)
									|| !(useDelegate
											? new CodeSnippetScope(currentScope)
													.canBeSeenByForCodeSnippet(
															fieldBinding.declaringClass,
															(ReferenceBinding) this.delegateThis.type)
											: fieldBinding.declaringClass
													.canBeSeenBy(currentScope)))) {
						// code
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_29_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X29 {
		
			boolean foo() {
				return (pack != null && otherpack != null && isSamePackage(pack, otherpack));
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X29 {
			
				boolean foo() {
					return (pack != null && otherpack != null
							&& isSamePackage(pack, otherpack));
				}
			}
			"""
	);
}
public void testBug330313_wksp1_30_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X30 {
			private boolean isInTypeNestedInInputType(ASTNode node, TypeDeclaration inputType){
				return (isInAnonymousTypeInsideInputType(node, inputType) ||
						isInLocalTypeInsideInputType(node, inputType) ||
						isInNonStaticMemberTypeInsideInputType(node, inputType));
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X30 {
				private boolean isInTypeNestedInInputType(ASTNode node,
						TypeDeclaration inputType) {
					return (isInAnonymousTypeInsideInputType(node, inputType) ||
							isInLocalTypeInsideInputType(node, inputType) ||
							isInNonStaticMemberTypeInsideInputType(node, inputType));
				}
			}
			"""
	);
}
public void testBug330313_wksp1_31_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X31 {
			void foo(int i) {
				if (true) {
					switch (i) {
						case 0:
							if (!((offset == (hashable.length - 1)) && !has95 && hasOneOf(meta63, hashable, offset - 2, 2) && !hasOneOf(meta64, hashable, offset - 4, 2)))
								buffer.append(\'R\');
							break;
					}
				}
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X31 {
				void foo(int i) {
					if (true) {
						switch (i) {
						case 0:
							if (!((offset == (hashable.length - 1)) && !has95
									&& hasOneOf(meta63, hashable, offset - 2, 2)
									&& !hasOneOf(meta64, hashable, offset - 4, 2)))
								buffer.append(\'R\');
							break;
						}
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_32_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X32 {
			public boolean equals(Object object) {
				TextAttribute a= (TextAttribute) object;
				return (a.style == style && equals(a.foreground, foreground) && equals(a.background, background));
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X32 {
				public boolean equals(Object object) {
					TextAttribute a = (TextAttribute) object;
					return (a.style == style && equals(a.foreground, foreground)
							&& equals(a.background, background));
				}
			}
			"""
	);
}
// Test case extracted from differences noticed with patch v29.txt
public void testBug330313_wksp1_33() {
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X33 {
			void foo() {
		        if(inMetaTag &&
		                        (  t1.image.equalsIgnoreCase("name") ||
		                           t1.image.equalsIgnoreCase("HTTP-EQUIV")
		                        )
		           && t2 != null)
		        {
		                currentMetaTag=t2.image.toLowerCase();
		        }
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X33 {
				void foo() {
					if (inMetaTag
							&& (t1.image.equalsIgnoreCase("name")
									|| t1.image.equalsIgnoreCase("HTTP-EQUIV"))
							&& t2 != null) {
						currentMetaTag = t2.image.toLowerCase();
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_33_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X33 {
			void foo() {
		        if(inMetaTag &&
		                        (  t1.image.equalsIgnoreCase("name") ||
		                           t1.image.equalsIgnoreCase("HTTP-EQUIV")
		                        )
		           && t2 != null)
		        {
		                currentMetaTag=t2.image.toLowerCase();
		        }
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X33 {
				void foo() {
					if (inMetaTag &&
							(t1.image.equalsIgnoreCase("name") ||
									t1.image.equalsIgnoreCase("HTTP-EQUIV"))
							&& t2 != null) {
						currentMetaTag = t2.image.toLowerCase();
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_34_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X34 {
			private boolean compareMarkers(ResourceInfo oldElement, ResourceInfo newElement) {
				boolean bothNull = oldElement.getMarkers(false) == null && newElement.getMarkers(false) == null;
				return bothNull || oldElement.getMarkerGenerationCount() == newElement.getMarkerGenerationCount();
			}
			private boolean compareSync(ResourceInfo oldElement, ResourceInfo newElement) {
				return oldElement.getSyncInfoGenerationCount() == newElement.getSyncInfoGenerationCount();
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X34 {
				private boolean compareMarkers(ResourceInfo oldElement,
						ResourceInfo newElement) {
					boolean bothNull = oldElement.getMarkers(false) == null
							&& newElement.getMarkers(false) == null;
					return bothNull || oldElement.getMarkerGenerationCount() == newElement
							.getMarkerGenerationCount();
				}
			
				private boolean compareSync(ResourceInfo oldElement,
						ResourceInfo newElement) {
					return oldElement.getSyncInfoGenerationCount() == newElement
							.getSyncInfoGenerationCount();
				}
			}
			"""
	);
}
// Test case extracted from differences noticed with patch v30.txt
public void testBug330313_wksp1_35_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X35 {
			void foo() {
				if (true) {
					if (20+lineNum*printGC.getFontMetrics().getHeight() > printer.getClientArea().height) {
						//
					}
				}
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X35 {
				void foo() {
					if (true) {
						if (20 + lineNum * printGC.getFontMetrics().getHeight() > printer
								.getClientArea().height) {
							//
						}
					}
				}
			}
			"""
	);
}
// Test case extracted from differences noticed with patch v32.txt
public void testBug330313_wksp1_36_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X36 {
			public static boolean isRuntimeException(ITypeBinding thrownException) {
				if (thrownException == null || thrownException.isPrimitive() || thrownException.isArray())
					return false;
				return findTypeInHierarchy(thrownException, "java.lang.RuntimeException") != null; //$NON-NLS-1$
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X36 {
				public static boolean isRuntimeException(ITypeBinding thrownException) {
					if (thrownException == null || thrownException.isPrimitive()
							|| thrownException.isArray())
						return false;
					return findTypeInHierarchy(thrownException,
							"java.lang.RuntimeException") != null; //$NON-NLS-1$
				}
			}
			"""
	);
}
public void testBug330313_wksp1_37_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X37 {
			void foo() {
				if (true) {
					if (ignoreQuickDiffPrefPage && (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffChange") //$NON-NLS-1$
							|| (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffAddition")) //$NON-NLS-1$
							|| (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffDeletion")) //$NON-NLS-1$
						))\s
						continue;
				}
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X37 {
				void foo() {
					if (true) {
						if (ignoreQuickDiffPrefPage && (info.getAnnotationType().equals(
								"org.eclipse.ui.workbench.texteditor.quickdiffChange") //$NON-NLS-1$
								|| (info.getAnnotationType().equals(
										"org.eclipse.ui.workbench.texteditor.quickdiffAddition")) //$NON-NLS-1$
								|| (info.getAnnotationType().equals(
										"org.eclipse.ui.workbench.texteditor.quickdiffDeletion")) //$NON-NLS-1$
						))
							continue;
					}
				}
			}
			"""
	);
}
// Test case extracted from differences noticed with patch v33.txt
public void testBug330313_wksp1_38_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X38 {
			void foo(boolean condition) {
				if (condition)
				{
					// block 1
				}
				else
				{
					// block 2
				}
			}
		}
		""";
	formatSource(source	,
			"""
				package wksp1;
				
				public class X38 {
					void foo(boolean condition) {
						if (condition) {
							// block 1
						} else {
							// block 2
						}
					}
				}
				""");
}
public void testBug330313_wksp1_39_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X39 {
		/**
		 * <pre>
		 *		RadioGroupFieldEditor editor= new RadioGroupFieldEditor(
		 *			"GeneralPage.DoubleClick", resName, 1,
		 *			new String[][] {
		 *				{"Open Browser", "open"},
		 *				{"Expand Tree", "expand"}
		 *			},
		 *          parent);\t
		 * </pre>
		 */
		public void foo() {
		}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X39 {
				/**
				 * <pre>
				 * RadioGroupFieldEditor editor = new RadioGroupFieldEditor(
				 * 		"GeneralPage.DoubleClick", resName, 1,
				 * 		new String[][] {
				 * 				{ "Open Browser", "open" },
				 * 				{ "Expand Tree", "expand" }
				 * 		},
				 * 		parent);
				 * </pre>
				 */
				public void foo() {
				}
			}
			"""
	);
}
public void testBug330313_wksp1_40_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X40 {
			protected final static String[][] TABLE= new String[][] {
										/*INACTIVE*/	/*PARTLY_ACTIVE */	/*ACTIVE */
				/* INACTIVE */		{	"INACTIVE",		"PARTLY_ACTIVE",		"PARTLY_ACTIVE" },
				/* PARTLY_ACTIVE*/	{	"PARTLY_ACTIVE", 	"PARTLY_ACTIVE",		"PARTLY_ACTIVE" },
				/* ACTIVE */		{	"PARTLY_ACTIVE", 	"PARTLY_ACTIVE",		"ACTIVE"}
			};
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X40 {
				protected final static String[][] TABLE = new String[][] {
						/* INACTIVE */ /* PARTLY_ACTIVE */ /* ACTIVE */
						/* INACTIVE */ { "INACTIVE", "PARTLY_ACTIVE", "PARTLY_ACTIVE" },
						/* PARTLY_ACTIVE */ { "PARTLY_ACTIVE", "PARTLY_ACTIVE",
								"PARTLY_ACTIVE" },
						/* ACTIVE */ { "PARTLY_ACTIVE", "PARTLY_ACTIVE", "ACTIVE" }
				};
			}
			"""
	);
}
public void testBug330313_wksp1_41_njl() {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X41 {
			static final int [][] TABLE = {
			\t
				/* First */
				{1,	2},
				{3,	4},
				{5,	6},
		//		{7????,	8},
		
				/* Second */	\t
		//		{11, 12},
		//		{13, 14},
		//		{15, 16},
			\t
			\t
				/* Third */
				{21,	22},
				{23,	24},
				{25,	26},
		//		{27????,	28},
		
				/* Others */
				{31,	32},
				{33,	34},
				{35,	36},
		//		{37????,	38},
			\t
			};
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X41 {
				static final int[][] TABLE = {
			
						/* First */
						{ 1, 2 },
						{ 3, 4 },
						{ 5, 6 },
						// {7????, 8},
			
						/* Second */
						// {11, 12},
						// {13, 14},
						// {15, 16},
			
						/* Third */
						{ 21, 22 },
						{ 23, 24 },
						{ 25, 26 },
						// {27????, 28},
			
						/* Others */
						{ 31, 32 },
						{ 33, 34 },
						{ 35, 36 },
						// {37????, 38},
			
				};
			}
			"""
	);
}
public void testBug330313_wksp1_42_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X42 {
			static final byte[][] DashList = {
				{ },                   // SWT.LINE_SOLID
				{ 10, 4 },             // SWT.LINE_DASH
				{ 2, 2 },              // SWT.LINE_DOT
				{ 10, 4, 2, 4 },       // SWT.LINE_DASHDOT
				{ 10, 4, 2, 4, 2, 4 }  // SWT.LINE_DASHDOTDOT
			};
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X42 {
				static final byte[][] DashList = {
						{}, // SWT.LINE_SOLID
						{ 10, 4 }, // SWT.LINE_DASH
						{ 2, 2 }, // SWT.LINE_DOT
						{ 10, 4, 2, 4 }, // SWT.LINE_DASHDOT
						{ 10, 4, 2, 4, 2, 4 } // SWT.LINE_DASHDOTDOT
				};
			}
			"""
	);
}
public void testBug330313_wksp1_43_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X43 {
			Cloneable clone;
		X43() {
			this.clone = new Cloneable() {
				void foo(int x) {
					switch (x) {
						case 1:
						case 2:
							if (true) break;
							// FALL THROUGH
						case 3:
						case 4:
							break;
					}
				}
			};
		}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X43 {
				Cloneable clone;
			
				X43() {
					this.clone = new Cloneable() {
						void foo(int x) {
							switch (x) {
							case 1:
							case 2:
								if (true)
									break;
								// FALL THROUGH
							case 3:
							case 4:
								break;
							}
						}
					};
				}
			}
			"""
	);
}
public void testBug330313_wksp1_44_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X44 {
			String foo() {
				return Policy.bind("CVSAnnotateBlock.6", new Object[] { //$NON-NLS-1$
					user,
					revision,
					String.valueOf(delta),
					line
				});
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X44 {
				String foo() {
					return Policy.bind("CVSAnnotateBlock.6", new Object[] { //$NON-NLS-1$
							user,
							revision,
							String.valueOf(delta),
							line
					});
				}
			}
			"""
	);
}
public void testBug330313_wksp1_45_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X45 {
				private String[][] TABLE  = {
					{"COL_REVISION", "COL_DATE", "COL_AUTHOR", "COL_COMMENT", "COL_TAGS"},	/* revision */\s
					{"COL_TAGS", "COL_REVISION", "COL_DATE", "COL_AUTHOR", "COL_COMMENT"},	/* tags */
					{"COL_DATE", "COL_REVISION", "COL_AUTHOR", "COL_COMMENT", "COL_TAGS"},	/* date */
					{"COL_AUTHOR", "COL_REVISION", "COL_DATE", "COL_COMMENT", "COL_TAGS"},	/* author */
					{"COL_COMMENT", "COL_REVISION", "COL_DATE", "COL_AUTHOR", "COL_TAGS"}		/* comment */
				};
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X45 {
				private String[][] TABLE = {
						{ "COL_REVISION", "COL_DATE", "COL_AUTHOR", "COL_COMMENT",
								"COL_TAGS" }, /* revision */
						{ "COL_TAGS", "COL_REVISION", "COL_DATE", "COL_AUTHOR",
								"COL_COMMENT" }, /* tags */
						{ "COL_DATE", "COL_REVISION", "COL_AUTHOR", "COL_COMMENT",
								"COL_TAGS" }, /* date */
						{ "COL_AUTHOR", "COL_REVISION", "COL_DATE", "COL_COMMENT",
								"COL_TAGS" }, /* author */
						{ "COL_COMMENT", "COL_REVISION", "COL_DATE", "COL_AUTHOR",
								"COL_TAGS" } /* comment */
				};
			}
			"""
	);
}
public void testBug330313_wksp1_45b_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
			"""
		package wksp1;
		
		public class X45 {
				private String[][] TABLE  = {
					{"COL_REVISION", "COL_DATE", "COL_AUTHOR", "COL_COMMENT", "COL_TAGS"},	// revision\s
					{"COL_TAGS", "COL_REVISION", "COL_DATE", "COL_AUTHOR", "COL_COMMENT"},	// tags\s
					{"COL_DATE", "COL_REVISION", "COL_AUTHOR", "COL_COMMENT", "COL_TAGS"},	// date\s
					{"COL_AUTHOR", "COL_REVISION", "COL_DATE", "COL_COMMENT", "COL_TAGS"},	// author\s
					{"COL_COMMENT", "COL_REVISION", "COL_DATE", "COL_AUTHOR", "COL_TAGS"}		// comment\s
				};
		}
		""";
	formatSource(source	,
			"""
				package wksp1;
				
				public class X45 {
					private String[][] TABLE = {
							{ "COL_REVISION", "COL_DATE", "COL_AUTHOR", "COL_COMMENT",
									"COL_TAGS" }, // revision
							{ "COL_TAGS", "COL_REVISION", "COL_DATE", "COL_AUTHOR",
									"COL_COMMENT" }, // tags
							{ "COL_DATE", "COL_REVISION", "COL_AUTHOR", "COL_COMMENT",
									"COL_TAGS" }, // date
							{ "COL_AUTHOR", "COL_REVISION", "COL_DATE", "COL_COMMENT",
									"COL_TAGS" }, // author
							{ "COL_COMMENT", "COL_REVISION", "COL_DATE", "COL_AUTHOR",
									"COL_TAGS" } // comment
					};
				}
				"""
			);
}
public void testBug330313_wksp1_46_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X46 {
			void foo() {
			    if (getActive() == StackPresentation.AS_ACTIVE_NOFOCUS) {
			        drawGradient(
			                colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR),\s
			                new Color [] {
			                        colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START)\s
			                },\s
			                new int [0],
			                true);	       \s
			    }
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X46 {
				void foo() {
					if (getActive() == StackPresentation.AS_ACTIVE_NOFOCUS) {
						drawGradient(
								colorRegistry.get(
										IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR),
								new Color[] {
										colorRegistry.get(
												IWorkbenchThemeConstants.INACTIVE_TAB_BG_START)
								},
								new int[0],
								true);
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_47_njl() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X47 {
			void foo(int x) {
				switch (x) {
					case 0 :
						// case 0
						break;
					case 3 :
						// case 3
						break;
					//case -1 :
					// internal failure: trying to load variable not supposed to be generated
					//	break;
					default :
						// default
				}
				// last comment
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X47 {
				void foo(int x) {
					switch (x) {
					case 0:
						// case 0
						break;
					case 3:
						// case 3
						break;
					// case -1 :
					// internal failure: trying to load variable not supposed to be
					// generated
					// break;
					default:
						// default
					}
					// last comment
				}
			}
			"""
	);
}
public void testBug330313_wksp1_48_njl() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X48 {
			void foo(int x) {
				switch (x) {
					case 0 :
						// case 0
						break;
					case 3 :
						// case 3
						break;
					//case -1 :
					// internal failure: trying to load variable not supposed to be generated
					//	break;
				}
				// last comment
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X48 {
				void foo(int x) {
					switch (x) {
					case 0:
						// case 0
						break;
					case 3:
						// case 3
						break;
					// case -1 :
					// internal failure: trying to load variable not supposed to be
					// generated
					// break;
					}
					// last comment
				}
			}
			"""
	);
}
public void testBug330313_wksp1_49_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X49 {
			void foo(int i) {
				if (true) {
					if (true) {
						this.foundTaskPositions[this.foundTaskCount] = new int[] { i, i + tagLength - 1 };
					}
				}
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X49 {
				void foo(int i) {
					if (true) {
						if (true) {
							this.foundTaskPositions[this.foundTaskCount] = new int[] { i,
									i + tagLength - 1 };
						}
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_50_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp1;
		
		public class X50 {
		private void deployCodeSnippetClassIfNeeded(IRequestor requestor) {
			if (this.codeSnippetBinary == null) {
				// Deploy CodeSnippet class (only once)
				requestor.acceptClassFiles(
					new ClassFile[] {
						new ClassFile() {
							public byte[] getBytes() {
								return getCodeSnippetBytes();
							}
							public char[][] getCompoundName() {
								return EvaluationConstants.ROOT_COMPOUND_NAME;
							}
						}
					},\s
					null);
			}
		}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X50 {
				private void deployCodeSnippetClassIfNeeded(IRequestor requestor) {
					if (this.codeSnippetBinary == null) {
						// Deploy CodeSnippet class (only once)
						requestor.acceptClassFiles(
								new ClassFile[] {
										new ClassFile() {
											public byte[] getBytes() {
												return getCodeSnippetBytes();
											}
			
											public char[][] getCompoundName() {
												return EvaluationConstants.ROOT_COMPOUND_NAME;
											}
										}
								},
								null);
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_51_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X51 {
		
		protected void addAllSourceFiles(final ArrayList sourceFiles) throws CoreException {
			for (int i = 0, l = sourceLocations.length; i < l; i++) {
				sourceLocation.sourceFolder.accept(
					new IResourceProxyVisitor() {
						public boolean visit(IResourceProxy proxy) throws CoreException {
							IResource resource = null;
							switch(proxy.getType()) {
								case IResource.FILE :
									if (exclusionPatterns != null || inclusionPatterns != null) {
										resource = proxy.requestResource();
										if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns)) return false;
									}
									if (org.eclipse.jdt.internal.compiler.util.Util.isJavaFileName(proxy.getName())) {
										if (resource == null)
											resource = proxy.requestResource();
										sourceFiles.add(new SourceFile((IFile) resource, sourceLocation));
									}
									return false;
								case IResource.FOLDER :
									if (exclusionPatterns != null && inclusionPatterns == null) {
										// if there are inclusion patterns then we must walk the children
										resource = proxy.requestResource();
										if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns)) return false;
									}
									if (isAlsoProject && isExcludedFromProject(proxy.requestFullPath())) return false;
							}
							return true;
						}
					},
					IResource.NONE);
				notifier.checkCancel();
			}
		}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X51 {
			
				protected void addAllSourceFiles(final ArrayList sourceFiles)
						throws CoreException {
					for (int i = 0, l = sourceLocations.length; i < l; i++) {
						sourceLocation.sourceFolder.accept(
								new IResourceProxyVisitor() {
									public boolean visit(IResourceProxy proxy)
											throws CoreException {
										IResource resource = null;
										switch (proxy.getType()) {
										case IResource.FILE:
											if (exclusionPatterns != null
													|| inclusionPatterns != null) {
												resource = proxy.requestResource();
												if (Util.isExcluded(resource,
														inclusionPatterns,
														exclusionPatterns))
													return false;
											}
											if (org.eclipse.jdt.internal.compiler.util.Util
													.isJavaFileName(proxy.getName())) {
												if (resource == null)
													resource = proxy.requestResource();
												sourceFiles.add(new SourceFile(
														(IFile) resource, sourceLocation));
											}
											return false;
										case IResource.FOLDER:
											if (exclusionPatterns != null
													&& inclusionPatterns == null) {
												// if there are inclusion patterns then we
												// must walk the children
												resource = proxy.requestResource();
												if (Util.isExcluded(resource,
														inclusionPatterns,
														exclusionPatterns))
													return false;
											}
											if (isAlsoProject && isExcludedFromProject(
													proxy.requestFullPath()))
												return false;
										}
										return true;
									}
								},
								IResource.NONE);
						notifier.checkCancel();
					}
				}
			}
			"""
	);
}
public void testBug330313_wksp1_52_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"""
		package wksp1;
		
		public class X52 {
			protected FastSyncInfoFilter getKnownFailureCases() {
				return new OrSyncInfoFilter(new FastSyncInfoFilter[] {
					// Conflicting additions of files will fail
					new AndSyncInfoFilter(new FastSyncInfoFilter[] {
						FastSyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.ADDITION),
						new FastSyncInfoFilter() {
							public boolean select(SyncInfo info) {
								return info.getLocal().getType() == IResource.FILE;
							}
						}
					}),
					// Conflicting changes of files will fail if the local is not managed
					// or is an addition
					new AndSyncInfoFilter(new FastSyncInfoFilter[] {
						FastSyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.CHANGE),
						new FastSyncInfoFilter() {
							public boolean select(SyncInfo info) {
								if (info.getLocal().getType() == IResource.FILE) {
									try {
										ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)info.getLocal());
										byte[] syncBytes = cvsFile.getSyncBytes();
										return (syncBytes == null || ResourceSyncInfo.isAddition(syncBytes));
									} catch (CVSException e) {
										CVSUIPlugin.log(e);
										// Fall though and try to update
									}
								}
								return false;
							}
						}
					}),
					// Conflicting changes involving a deletion on one side will aways fail
					new AndSyncInfoFilter(new FastSyncInfoFilter[] {
						FastSyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.CHANGE),
						new FastSyncInfoFilter() {
							public boolean select(SyncInfo info) {
								IResourceVariant remote = info.getRemote();
								IResourceVariant base = info.getBase();
								if (info.getLocal().exists()) {
									// local != base and no remote will fail
									return (base != null && remote == null);
								} else {
									// no local and base != remote
									return (base != null && remote != null && !base.equals(remote));
								}
							}
						}
					}),
					// Conflicts where the file type is binary will work but are not merged
					// so they should be skipped
					new AndSyncInfoFilter(new FastSyncInfoFilter[] {
						FastSyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.CHANGE),
						new FastSyncInfoFilter() {
							public boolean select(SyncInfo info) {
								IResource local = info.getLocal();
								if (local.getType() == IResource.FILE) {
									try {
										ICVSFile file = CVSWorkspaceRoot.getCVSFileFor((IFile)local);
										byte[] syncBytes = file.getSyncBytes();
										if (syncBytes != null) {
											return ResourceSyncInfo.isBinary(syncBytes);
										}
									} catch (CVSException e) {
										// There was an error obtaining or interpreting the sync bytes
										// Log it and skip the file
										CVSProviderPlugin.log(e);
										return true;
									}
								}
								return false;
							}
						}
					}),
					// Outgoing changes may not fail but they are skipped as well
					new SyncInfoDirectionFilter(SyncInfo.OUTGOING)
				});
			}
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X52 {
				protected FastSyncInfoFilter getKnownFailureCases() {
					return new OrSyncInfoFilter(new FastSyncInfoFilter[] {
							// Conflicting additions of files will fail
							new AndSyncInfoFilter(new FastSyncInfoFilter[] {
									FastSyncInfoFilter.getDirectionAndChangeFilter(
											SyncInfo.CONFLICTING, SyncInfo.ADDITION),
									new FastSyncInfoFilter() {
										public boolean select(SyncInfo info) {
											return info.getLocal()
													.getType() == IResource.FILE;
										}
									}
							}),
							// Conflicting changes of files will fail if the local is not
							// managed
							// or is an addition
							new AndSyncInfoFilter(new FastSyncInfoFilter[] {
									FastSyncInfoFilter.getDirectionAndChangeFilter(
											SyncInfo.CONFLICTING, SyncInfo.CHANGE),
									new FastSyncInfoFilter() {
										public boolean select(SyncInfo info) {
											if (info.getLocal()
													.getType() == IResource.FILE) {
												try {
													ICVSFile cvsFile = CVSWorkspaceRoot
															.getCVSFileFor((IFile) info
																	.getLocal());
													byte[] syncBytes = cvsFile
															.getSyncBytes();
													return (syncBytes == null
															|| ResourceSyncInfo
																	.isAddition(syncBytes));
												} catch (CVSException e) {
													CVSUIPlugin.log(e);
													// Fall though and try to update
												}
											}
											return false;
										}
									}
							}),
							// Conflicting changes involving a deletion on one side will
							// aways fail
							new AndSyncInfoFilter(new FastSyncInfoFilter[] {
									FastSyncInfoFilter.getDirectionAndChangeFilter(
											SyncInfo.CONFLICTING, SyncInfo.CHANGE),
									new FastSyncInfoFilter() {
										public boolean select(SyncInfo info) {
											IResourceVariant remote = info.getRemote();
											IResourceVariant base = info.getBase();
											if (info.getLocal().exists()) {
												// local != base and no remote will fail
												return (base != null && remote == null);
											} else {
												// no local and base != remote
												return (base != null && remote != null
														&& !base.equals(remote));
											}
										}
									}
							}),
							// Conflicts where the file type is binary will work but are not
							// merged
							// so they should be skipped
							new AndSyncInfoFilter(new FastSyncInfoFilter[] {
									FastSyncInfoFilter.getDirectionAndChangeFilter(
											SyncInfo.CONFLICTING, SyncInfo.CHANGE),
									new FastSyncInfoFilter() {
										public boolean select(SyncInfo info) {
											IResource local = info.getLocal();
											if (local.getType() == IResource.FILE) {
												try {
													ICVSFile file = CVSWorkspaceRoot
															.getCVSFileFor((IFile) local);
													byte[] syncBytes = file.getSyncBytes();
													if (syncBytes != null) {
														return ResourceSyncInfo
																.isBinary(syncBytes);
													}
												} catch (CVSException e) {
													// There was an error obtaining or
													// interpreting the sync bytes
													// Log it and skip the file
													CVSProviderPlugin.log(e);
													return true;
												}
											}
											return false;
										}
									}
							}),
							// Outgoing changes may not fail but they are skipped as well
							new SyncInfoDirectionFilter(SyncInfo.OUTGOING)
					});
				}
			}
			"""
	);
}
public void testBug330313_wksp1_53_njl_bnl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"""
		package wksp1;
		
		public class X53 {
			static final short[][][] BLACK_CODE = {
				/* 9 bits  */
				{{24, 15}},
				/* 10 bits */
				{{8, 18}, {15, 64}, {23, 16}, {24, 17}, {55, 0}},
				/* 11 bits */
				{/* EOL */{0, -1}, {8, 1792}, {23, 24}, {24, 25}, {40, 23}, {55, 22}, {103, 19},
				{104, 20}, {108, 21}, {12, 1856}, {13, 1920}},
			};
		}
		""";
	formatSource(source	,
		"""
			package wksp1;
			
			public class X53
			{
				static final short[][][] BLACK_CODE =
				{
						/* 9 bits */
						{
								{ 24, 15 } },
						/* 10 bits */
						{
								{ 8, 18 },
								{ 15, 64 },
								{ 23, 16 },
								{ 24, 17 },
								{ 55, 0 } },
						/* 11 bits */
						{
								/* EOL */{ 0, -1 },
								{ 8, 1792 },
								{ 23, 24 },
								{ 24, 25 },
								{ 40, 23 },
								{ 55, 22 },
								{ 103, 19 },
								{ 104, 20 },
								{ 108, 21 },
								{ 12, 1856 },
								{ 13, 1920 } },
				};
			}
			"""
	);
}
public void testBug330313_wksp2_01 () {
	String source =
		"""
		package wksp2;
		
		public class X01 {
		
		    static final Object[][] contents = {
		        // comment
		        { "STR1",
		        	// comment
		            new String[] { "STR",     // comment
		                           "STR",     // comment
		                           "STR"}     // comment
		        }
		
		    };
		
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public class X01 {
			
				static final Object[][] contents = {
						// comment
						{ "STR1",
								// comment
								new String[] { "STR", // comment
										"STR", // comment
										"STR" } // comment
						}
			
				};
			
			}
			"""
	);
}
public void testBug330313_wksp3_X01_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		package wksp3;
		
		public class X01 {
		    private static final String foo[][] = {
		        // line 1
		        // line 2
		        {"A", "B", "C", "D", "E"} // comment
		    };
		}
		""";
	formatSource(source	,
		"""
			package wksp3;
			
			public class X01 {
				private static final String foo[][] = {
						// line 1
						// line 2
						{ "A", "B", "C", "D", "E" } // comment
				};
			}
			"""
	);
}
// Test cases added from bug 286601
public void testBug330313_b286601_04() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"""
		package b286601;
		
		public class X04 {
		
		   \s
		    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]).\s
		    private static final int[][] ACCESS_MODE_CONDITIONAL_TABLE= {
		    /*                        UNUSED           READ             READ_POTENTIAL   WRTIE            WRITE_POTENTIAL  UNKNOWN */
		    /* UNUSED */            { UNUSED,          READ_POTENTIAL,  READ_POTENTIAL,  WRITE_POTENTIAL, WRITE_POTENTIAL, UNKNOWN },
		    /* READ */              { READ_POTENTIAL,  READ,            READ_POTENTIAL,  UNKNOWN,         UNKNOWN,         UNKNOWN },
		    /* READ_POTENTIAL */    { READ_POTENTIAL,  READ_POTENTIAL,  READ_POTENTIAL,  UNKNOWN,         UNKNOWN,         UNKNOWN },
		    /* WRITE */             { WRITE_POTENTIAL, UNKNOWN,         UNKNOWN,         WRITE,           WRITE_POTENTIAL, UNKNOWN },
		    /* WRITE_POTENTIAL */   { WRITE_POTENTIAL, UNKNOWN,         UNKNOWN,         WRITE_POTENTIAL, WRITE_POTENTIAL, UNKNOWN },
		    /* UNKNOWN */           { UNKNOWN,         UNKNOWN,         UNKNOWN,         UNKNOWN,         UNKNOWN,         UNKNOWN }
		    };
		
		}
		""";
	formatSource(source,
		"""
			package b286601;
			
			public class X04
			{
			
				// Table to merge access modes for condition statements (e.g branch[x] ||
				// branch[y]).
				private static final int[][] ACCESS_MODE_CONDITIONAL_TABLE =
				{
						/* UNUSED READ READ_POTENTIAL WRTIE WRITE_POTENTIAL UNKNOWN */
						/* UNUSED */ {
								UNUSED,
								READ_POTENTIAL,
								READ_POTENTIAL,
								WRITE_POTENTIAL,
								WRITE_POTENTIAL,
								UNKNOWN },
						/* READ */ {
								READ_POTENTIAL,
								READ,
								READ_POTENTIAL,
								UNKNOWN,
								UNKNOWN,
								UNKNOWN },
						/* READ_POTENTIAL */ {
								READ_POTENTIAL,
								READ_POTENTIAL,
								READ_POTENTIAL,
								UNKNOWN,
								UNKNOWN,
								UNKNOWN },
						/* WRITE */ {
								WRITE_POTENTIAL,
								UNKNOWN,
								UNKNOWN,
								WRITE,
								WRITE_POTENTIAL,
								UNKNOWN },
						/* WRITE_POTENTIAL */ {
								WRITE_POTENTIAL,
								UNKNOWN,
								UNKNOWN,
								WRITE_POTENTIAL,
								WRITE_POTENTIAL,
								UNKNOWN },
						/* UNKNOWN */ {
								UNKNOWN,
								UNKNOWN,
								UNKNOWN,
								UNKNOWN,
								UNKNOWN,
								UNKNOWN }
				};
			
			}
			"""
	);
}
public void testBug330313_b286601_05() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"""
		package b286601;
		
		public class X05 {
		
		   \s
		    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]).\s
		    static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {
		    { "UNUSED",          "READ_POTENTIAL",  "READ_POTENTIAL",  "WRITE_POTENTIAL", "WRITE_POTENTIAL", "UNKNOWN" },
		    { "READ_POTENTIAL",  "READ",            "READ_POTENTIAL",  "UNKNOWN",         "UNKNOWN",         "UNKNOWN" },
		    };
		
		}
		""";
	formatSource(source,
		"""
			package b286601;
			
			public class X05
			{
			
				// Table to merge access modes for condition statements (e.g branch[x] ||
				// branch[y]).
				static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =
				{
						{
								"UNUSED",
								"READ_POTENTIAL",
								"READ_POTENTIAL",
								"WRITE_POTENTIAL",
								"WRITE_POTENTIAL",
								"UNKNOWN" },
						{
								"READ_POTENTIAL",
								"READ",
								"READ_POTENTIAL",
								"UNKNOWN",
								"UNKNOWN",
								"UNKNOWN" },
				};
			
			}
			"""
	);
}
public void testBug330313_b286601_06() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"""
		package b286601;
		
		public class X06 {
		
		   \s
		    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]).\s
		    static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {
		    /* Comment 1 */
		    /* Comment 2 */ { "UNUSED",          "READ_POTENTIAL",  "READ_POTENTIAL",  "WRITE_POTENTIAL", "WRITE_POTENTIAL", "UNKNOWN" },
		    /* Comment 3 */ { "READ_POTENTIAL",  "READ",            "READ_POTENTIAL",  "UNKNOWN",         "UNKNOWN",         "UNKNOWN" },
		    };
		
		}
		""";
	formatSource(source,
		"""
			package b286601;
			
			public class X06
			{
			
				// Table to merge access modes for condition statements (e.g branch[x] ||
				// branch[y]).
				static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =
				{
						/* Comment 1 */
						/* Comment 2 */ {
								"UNUSED",
								"READ_POTENTIAL",
								"READ_POTENTIAL",
								"WRITE_POTENTIAL",
								"WRITE_POTENTIAL",
								"UNKNOWN" },
						/* Comment 3 */ {
								"READ_POTENTIAL",
								"READ",
								"READ_POTENTIAL",
								"UNKNOWN",
								"UNKNOWN",
								"UNKNOWN" },
				};
			
			}
			"""
	);
}
public void testBug330313_b286601_07() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"""
		package b286601;
		
		public class X07 {
		
		   \s
		    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]).\s
		    static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {
		    /* Comment 1 */
		    /* Comment 2 */ { "1234567890123456789012345678901234567890", "1234567890123456789012345678901234567890" },
		    /* Comment 3 */ { "ABCDEFGHIJKLMNOPQRSTUVWXYZ______________", "ABCDEFGHIJKLMNOPQRSTUVWXYZ______________" },
		    };
		
		}
		""";
	formatSource(source,
		"""
			package b286601;
			
			public class X07
			{
			
			    // Table to merge access modes for condition statements (e.g branch[x] ||
			    // branch[y]).
			    static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =
			    {
			            /* Comment 1 */
			            /* Comment 2 */ {
			                    "1234567890123456789012345678901234567890",
			                    "1234567890123456789012345678901234567890" },
			            /* Comment 3 */ {
			                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ______________",
			                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ______________" },
			    };
			
			}
			"""
	);
}
public void testBug330313_b286601_08() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"""
		package b286601;
		
		public class X08 {
		    private MinimizedFileSystemElement selectFiles(final Object rootFileSystemObject, final IImportStructureProvider structureProvider) {
		
		        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
		            public void run() {
		                //Create the root element from the supplied file system object
		            }
		        });
		
		        return null;
		    }
		}
		""";
	formatSource(source,
		"""
			package b286601;
			
			public class X08
			{
				private MinimizedFileSystemElement selectFiles(
						final Object rootFileSystemObject,
						final IImportStructureProvider structureProvider)
				{
			
					BusyIndicator.showWhile(getShell().getDisplay(), new Runnable()
					{
						public void run()
						{
							// Create the root element from the supplied file system object
						}
					});
			
					return null;
				}
			}
			"""
	);
}

/**
 * bug 332818: [formatter] Java formatter, Blank Lines tab, only 1st line indented when multiple lines is set
 * test Ensure that the indentation is set on all blank lines
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=332818"
 */
public void testBug332818() throws Exception {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.number_of_empty_lines_to_preserve = 99;
	String source =
		"""
		public class Test {
		
		\s
		 \s
		  \s
		   \s
		\t
		    private String f1;
		   \s
		  \s
		 \s
		\s
		
		\t
			private String f2;
		\t
			\t
				\t
		\t
		}
		""";
	formatSource(source,
		"""
			public class Test {
			\t
			\t
			\t
			\t
			\t
			\t
				private String f1;
			\t
			\t
			\t
			\t
			\t
			\t
				private String f2;
			\t
			\t
			\t
			\t
			}
			"""
	);
}

/**
 * bug 332877: [formatter] line comment wrongly put on a new line
 * test Ensure that the comment on last enum constant is not wrongly put on a new line
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=332877"
 */
public void testBug332877() throws Exception {
	String source =
		"""
		public enum Environment {
		    PROD,       // Production level environments
		    STAGING    // Staging
		}
		""";
	formatSource(source,
		"""
			public enum Environment {
				PROD, // Production level environments
				STAGING // Staging
			}
			"""
	);
}

/**
 * bug 282988: [formatter] Option to align single-line comments in a column
 * test Ensure that with line comment formatting turned off comment alignment doesn't change
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=282988"
 */
public void testBug282988() throws Exception {
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"""
		package test;
		
		public class FormatterError {
			public void storeSomething(java.nio.ByteBuffer buffer) throws Exception {
				buffer.clear();
				buffer.putLong(0);     // backlink to previous version of this object
				buffer.putInt(1);      // version identifier
				buffer.flip();         // prepare to write
			}
		}
		""";
	formatSource(source,
		"""
			package test;
			
			public class FormatterError {
				public void storeSomething(java.nio.ByteBuffer buffer) throws Exception {
					buffer.clear();
					buffer.putLong(0);     // backlink to previous version of this object
					buffer.putInt(1);      // version identifier
					buffer.flip();         // prepare to write
				}
			}
			"""
    );
}
public void testBug356851() throws Exception {
	String source =
		"""
		public class X {
			public X LongMethodName(X x) {
				return x;
			}
			public static void main(String[] args) {
				X x = new X();
				x = new X().LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x);
				System.out.println(x.hashCode());
			}
		}
		""";
	formatSource(source,
		"""
			public class X {
				public X LongMethodName(X x) {
					return x;
				}
			
				public static void main(String[] args) {
					X x = new X();
					x = new X().LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x)
							.LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x);
					System.out.println(x.hashCode());
				}
			}
			"""
    );
}
/**
 * bug 437639: [formatter] ArrayIndexOutOfBoundsException while formatting source code
 * test test that the AIOOB is not generated
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=437639"
 */
public void testBug437639() throws Exception {
	this.formatterPrefs.blank_lines_between_import_groups = ~0;
	String source =
		"""
		package com.test;
		
		import java.math.BigDecimal;
		import java.math.BigInteger;
		import java.util.ArrayList;
		
		
		
		//import java.util.Arrays;
		import java.util.Date;
		import java.util.List;
		
		public class Test {
		
			public static void main(String[] args) {
				BigDecimal big = new BigDecimal(1);
				BigInteger bigI = 	new BigInteger("1");
				Date d = new Date();
				List list = new ArrayList<>();
			}
		}
		"""
		;
	formatSource(source,
		"""
			package com.test;
			
			import java.math.BigDecimal;
			import java.math.BigInteger;
			import java.util.ArrayList;
			//import java.util.Arrays;
			import java.util.Date;
			import java.util.List;
			
			public class Test {
			
				public static void main(String[] args) {
					BigDecimal big = new BigDecimal(1);
					BigInteger bigI = new BigInteger("1");
					Date d = new Date();
					List list = new ArrayList<>();
				}
			}
			"""
    );
}
/**
 * bug 460008: [formatter] Inserts wrong line breaks on ASTRewrite (Extract Constant, Extract Local Variable)
 * test test line break is not added at end of expression
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=460008"
 */
public void testBug460008() throws Exception {
	this.formatterPrefs.insert_new_line_at_end_of_file_if_missing = true;
	String source = "name";

	formatSource(source, source, CodeFormatter.K_EXPRESSION);
	formatSource(source, source, CodeFormatter.K_UNKNOWN);

	source = "public int field = 1;";
	formatSource(source, source, CodeFormatter.K_STATEMENTS | CodeFormatter.F_INCLUDE_COMMENTS);

	source = "/**Javadoc*/public int field=1;";
	String result = "/** Javadoc */\n" +
		"public int field = 1;";
	formatSource(source, result, CodeFormatter.K_CLASS_BODY_DECLARATIONS | CodeFormatter.F_INCLUDE_COMMENTS);

	// K_COMPILATION_UNIT is tested by FormatterRegressionTests#test512() and #test643()
}
/**
 * bug 462945 - [formatter] IndexOutOfBoundsException in TokenManager
 * test no exception is thrown for malformed code
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=462945"
 */
public void testBug462945() throws Exception {
	String source =
		"""
		package p1;
		enum ReviewResult {
			Good{, Bad
		}
		""";
	formatSource(source,
		"""
			package p1;
			
			enum ReviewResult {
				Good{, Bad
			}
			"""
	);
}
public void testBug407629() throws Exception {
	String source =
			"""
		public class X {
			/**
			 * Builds a {@link Level}.
			 * <p>
			 * Does <b>not</b> set :
			 * <ul>
			 * <li>{@link Level#setA(Boolean)</li>
			 * <li>{@link Level#setB(Long)}</li>
			 * <li>{@link Level#setC(Integer)}</li>
			 * </ul>
			 * </p>
			 */
			public static Level buildLevel() {
				return null;
			}
			\s
		}
		
		class Level {
			void setA(Boolean b) {}
			void setB(Long l) {}
			void setC(Integer i){}
		}
		""";
	String expected = """
		public class X {
			/**
			 * Builds a {@link Level}.
			 * <p>
			 * Does <b>not</b> set :
			 * <ul>
			 * <li>{@link Level#setA(Boolean)</li>
			 * <li>{@link Level#setB(Long)}</li>
			 * <li>{@link Level#setC(Integer)}</li>
			 * </ul>
			 * </p>
			 */
			public static Level buildLevel() {
				return null;
			}
		
		}
		
		class Level {
			void setA(Boolean b) {
			}
		
			void setB(Long l) {
			}
		
			void setC(Integer i) {
			}
		}
		""";
	formatSource(source, expected);
}

public void testBug464312() throws Exception {
	String source = "/**/int f;";
	formatSource(source, source, CodeFormatter.K_STATEMENTS);
}
/**
 * bug 458208: [formatter] follow up bug for comments
 * test test a space is not added after a lambda expression in parenthesis
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=458208#c2"
 */
public void testBug458208() throws Exception {
	String source =
		"""
		package p;
		import java.util.function.IntConsumer;
		class TestInlineLambda1 {
			{
				IntConsumer op = (x -> {}    );
			}
		}
		""";
	formatSource(source,
		"""
			package p;
			
			import java.util.function.IntConsumer;
			
			class TestInlineLambda1 {
				{
					IntConsumer op = (x -> {
					});
				}
			}
			"""
	);
}
/**
 * bug 458208: [formatter] follow up bug for comments
 * test test that comments in switch statements are properly indented
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=458208#c21"
 */
public void testBug458208b() throws Exception {
	formatSource(
		"""
			package p;
			
			public class C1 {
				void foo(int x) {
					switch (x) {
					// case 1
					case 1:
						break;
					// case 2
					case 2:
						break;
					// no more cases
					}
				}
			
				int bar(int x) {
					while (true) {
						int y = 9;
						switch (x) {
						// case 1
						case 1:
							// should return
							return y;
						// case 2
						case 2:
							// should break
							break;
						case 3:
							// TODO
						}
					}
				}
			}
			"""
	);
}
/**
 * bug 458208: [formatter] follow up bug for comments
 * test test that elements separated with empty lines are properly indented
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=458208#c18"
 */
public void testBug458208c() throws Exception {
	final int wrapAllOnColumn = Alignment.M_NEXT_PER_LINE_SPLIT + Alignment.M_INDENT_ON_COLUMN + Alignment.M_FORCE;
	this.formatterPrefs.alignment_for_enum_constants = wrapAllOnColumn;
	this.formatterPrefs.alignment_for_arguments_in_enum_constant = wrapAllOnColumn;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = wrapAllOnColumn;
	String source =
		"""
		package p;
		
		public enum TestEnum {
			FIRST_ENUM("first type",
			           new SomeClass(),
			           new OtherEnumType[] { OtherEnumType.FOO }),
		
			SECOND_ENUM("second type",
			            new SomeClassOtherClass(),
			            new OtherEnumType[] { OtherEnumType.BAR }),
		
			THIRD_ENUM("third type",
			            new YetAnotherClass(),
			            new OtherEnumType[] { OtherEnumType.FOOBAR,
			                                  OtherEnumType.FOOBARBAZ,
		
			                                  OtherEnumType.LONGERFOOBARBAZ,
			                                  OtherEnumType.MORELETTERSINHERE });
		
			/* data members and methods go here */
			TestEnum(String s, Cls s1, OtherEnumType[] e) {
			}
		}""";
	formatSource(source,
		"""
			package p;
			
			public enum TestEnum {
									FIRST_ENUM(	"first type",
												new SomeClass(),
												new OtherEnumType[] { OtherEnumType.FOO }),
			
									SECOND_ENUM("second type",
												new SomeClassOtherClass(),
												new OtherEnumType[] { OtherEnumType.BAR }),
			
									THIRD_ENUM(	"third type",
												new YetAnotherClass(),
												new OtherEnumType[] {	OtherEnumType.FOOBAR,
																		OtherEnumType.FOOBARBAZ,
			
																		OtherEnumType.LONGERFOOBARBAZ,
																		OtherEnumType.MORELETTERSINHERE });
			
				/* data members and methods go here */
				TestEnum(String s, Cls s1, OtherEnumType[] e) {
				}
			}"""
	);
}
/**
 * bug 458208: [formatter] follow up bug for comments
 * test test that enum constants are not indented with spaces when "Use spaces to indent wrapped lines" is on
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=458208#c24"
 */
public void testBug458208d() throws Exception {
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_COMPACT_SPLIT;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	setPageWidth80();
	String source =
		"""
		package p;
		
		public enum TestEnum {
			ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELWE, THIRTEEN, FOURTEEN, FIFTEEN;
		}""";
	formatSource(source,
		"""
			package p;
			
			public enum TestEnum {
				ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELWE,
				THIRTEEN, FOURTEEN, FIFTEEN;
			}"""
	);
}
/**
 * bug 465669: NPE in WrapExecutor during Java text formatting
 * test test that no NPE is thrown
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=465669"
 */
public void testBug465669() throws Exception {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.indentation_size = 2;
	setPageWidth80();
	String source =
		"""
		public class ffffffffffffffffff\r
		{\r
		  private static void test(String s)\r
		  {\r
		    dddd = (aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff)new dddddddddddddddd()\r
		  .ttt(null, aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.class)\r
		      .ttt("bbbbbbb", xxxxxxxxx.class)\r
		      .ttt("sssssssvvvvvvv", new fffffffffff("xxxx")\r
		           .add("eeeeeeee", aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.ssssssssssssss.class)\r
		           .add("cccccccccc", aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.wwwwwwwwwwwwwwww.class)\r
		           )\r
		      .bbbbbbbbbbb(s);\r
		  }\r
		  \r
		}""";
	formatSource(source,
		"""
			public class ffffffffffffffffff {\r
			  private static void test(String s) {\r
			    dddd = (aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff) new dddddddddddddddd()\r
			        .ttt(null, aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.class)\r
			        .ttt("bbbbbbb", xxxxxxxxx.class)\r
			        .ttt("sssssssvvvvvvv", new fffffffffff("xxxx")\r
			            .add("eeeeeeee",\r
			                aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.ssssssssssssss.class)\r
			            .add("cccccccccc",\r
			                aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.wwwwwwwwwwwwwwww.class))\r
			        .bbbbbbbbbbb(s);\r
			  }\r
			\r
			}"""
	);
}
public void testBug471090() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.indentation_size = 2;
	String source =
		"""
		class FormatterBug {
		// \\u00C4
		}
		""";
	formatSource(source,
		"""
			class FormatterBug {
			  // \\u00C4
			}
			"""
	);
}
/**
 * bug 471364: [formatter] Method declarations in interfaces are sometimes indented incorrectly
 * test test that methods without modifiers are properly indented
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=471364"
 */
public void testBug471364() throws JavaModelException {
	this.formatterPrefs.blank_lines_before_abstract_method = 0;
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_SPLIT;
	String source =
		"""
		interface Example {\r
		\r
			void method2();\r
			void method2();\r
		\r
			void method3();\r
		\r
			/**\r
			 * \r
			 */\r
			void method4();\r
		\r
		}""";
	formatSource(source);

	source =
		"""
			public class Example {\r
			\r
				void method2();\r
				void method2();\r
			\r
				void method3();\r
			\r
				/**\r
				 * \r
				 */\r
				void method4();\r
			\r
			}""";
	formatSource(source);
}
/**
 * bug 471145: [Formatter] doesn't remove space before "{" on the if line
 * test test that no unnecessary space is added
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=471145"
 */
public void testBug471145() throws JavaModelException {
	this.formatterPrefs.insert_space_before_opening_brace_in_block = false;
	this.formatterPrefs.keep_simple_if_on_one_line = true;
	String source =
		"""
		class C {\r
			void method() {\r
				if (condition) {\r
					operation();\r
				}\r
				if (condition)// don't add space before comment\r
					operation();\r
				if (condition)operation();\r
			}\r
		}""";
	formatSource(source,
		"""
			class C {\r
				void method() {\r
					if (condition){\r
						operation();\r
					}\r
					if (condition)// don't add space before comment\r
						operation();\r
					if (condition) operation();\r
				}\r
			}""");
}
/**
 * bug 469438: ArrayIndexOutOfBoundsException in TokenManager.applyFormatOff (443)
 * test test that no exception is thrown
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=469438"
 */
public void testBug469438() {
	this.formatterPrefs.use_tags = true;
	String source =
			"""
		public class C1 {\r
			int     b;\r
		\r
			/** @formatter:off */\r
			private void  a() {\r
				// @formatter:on\r
				if ()\r
			}\r
		}""";
	formatSource(source,
			"""
				public class C1 {\r
					int b;\r
				\r
					/** @formatter:off */\r
					private void  a() {\r
						// @formatter:on\r
						if ()\r
					}\r
				}"""
			);
}
/**
 * bug 471883: NullPointerException in TokenManager.firstIndexIn (188)
 * test test that no NPE is thrown
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=471883"
 */
public void testBug471883() throws Exception {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.indentation_size = 2;
	setPageWidth80();
	String source =
			"""
		/**\r
		 * <pre>\r
		 * isInEncoding(char ch);\r
		 * </pre>\r
		 */\r
		public class Try {\r
		}""";
	formatSource(source);
}
/**
 * bug 470977: [formatter] Whitespace removed between assert and unary operator or primary expression
 * test test that spaces after assert are correct
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=470977"
 */
public void testBug470977() throws Exception {
	String source =
		"""
		public class TestFormat {\r
			public static void main(String[] args) {\r
				assert "".length() == 0;\r
				assert (!false);\r
		\r
				assert !false;\r
				assert +0 == 0;\r
				assert -0 == 0;\r
		\r
				int i = 0;\r
				assert ++i == 1;\r
				assert --i == 0;\r
			}\r
		}""";
	formatSource(source);
}
/**
 * bug 472962: [formatter] Missing whitespace after >, ] in annotation type declaration
 * test test that there is whitespace before element identifiers
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=472962"
 */
public void testBug472962() {
	String source =
		"""
		public @interface A {\r
			String[] strings();\r
		\r
			Class<String> stringClasses();\r
		}""";
	formatSource(source);
}
/**
 * bug 470506: formatter option "align field in columns" changed in Mars
 * test test that fields separated by extra blank lines are not considered separate groups when aligning
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=470506"
 */
public void testBug470506() {
	this.formatterPrefs.align_type_members_on_columns = true;
	String source =
		"""
		class C {\r
			private int						iii;\r
			String							sss;\r
		\r
			protected ArrayList<Integer>	aaa;\r
		\r
		}""";
	formatSource(source);
}

/**
 * bug 472205: Class extends generic type and implements another type, missing space after ">"
 */
public void testBug472205() {
	String source =
		"""
		public class Test<E> extends ArrayList<String> implements Callable<String> {
		}
		
		class A extends B<ClientListener> implements C {
		}
		
		class D extends E<ClientListener> {
		}
		
		class F implements G<ClientListener> {
		}
		
		interface H extends I<ClientListener> {
		}
		""";
	formatSource(source);
}
/**
 * bug 471780 - [formatter] Regression in enum value Javadoc formatting
 */
public void testBug471780() {
	String source =
		"""
		public enum MyEnum {\r
			/** A. */\r
			A,\r
			/** B. */\r
			B\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/472009 - Formatter does not respect "keep else if on one line"
 */
public void testBug472009() {
	this.formatterPrefs.alignment_for_compact_if |= Alignment.M_FORCE;
	String source =
		"""
		public class A {\r
			void a() {\r
				if (a == b) {\r
		\r
				} else if (c == d) {\r
		\r
				} else if (e == f) {\r
		\r
				}\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/474629 - [save actions][clean up] Exceptions thrown
 */
public void testBug474629() {
	this.formatterPrefs.alignment_for_additive_operator |= Alignment.M_INDENT_ON_COLUMN;
	String source = "aaaaa + bbbb";
	formatSource(source, source, CodeFormatter.K_EXPRESSION, 0, true);
}
/**
 * https://bugs.eclipse.org/467618 - [formatter] Empty lines should not affect indentation of wrapped elements
 */
public void testBug467618() {
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_NEXT_PER_LINE_SPLIT + Alignment.M_INDENT_ON_COLUMN + Alignment.M_FORCE;
	String source =
		"""
		public enum E2 {\r
		\r
			FOOBAR,\r
		\r
			FOOBARBAZ,\r
		\r
			FOO;\r
		}""";
	formatSource(source,
		"""
			public enum E2 {\r
			\r
							FOOBAR,\r
			\r
							FOOBARBAZ,\r
			\r
							FOO;\r
			}"""
	);
}
/**
 * bug 474916: [formatter] Formatting GridBagLayout from Java 8 takes too long
 * test test that formatting finishes in reasonable time
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=474916"
 */
public void testBug474916() {
	String source =
			"""
		/**\r
		 * <                                                           \r
		 * >  <p style='color:red'> Test    </p>\r
		 *  <a title="I like to 'quote' it" \r
		href = 'http://www.eclipse.org'>Toast</a> */\r
		class A {}""";
	formatSource(source,
			"""
				/**\r
				 * < >\r
				 * <p style='color:red'>\r
				 * Test\r
				 * </p>\r
				 * <a title="I like to 'quote' it" href = 'http://www.eclipse.org'>Toast</a>\r
				 */\r
				class A {\r
				}"""
	);
}
/**
 * https://bugs.eclipse.org/474918 - [formatter] doesn't align fields in declarations of annotations, enums and anonymous classes
 */
public void testBug474918() {
	useOldCommentWidthCounting();
	this.formatterPrefs.align_type_members_on_columns = true;
	String source =
		"""
		import java.util.function.Function;\r
		\r
		public class A {\r
			private Function mapper = (Object a) -> {\r
				return a.toString().equals("test");\r
			};\r
			String ssssssssssssssss = "dsadaaaaaaaaaaaaaaaaaaaaaaaaa";   //$NON-NLS-1$ // B // A\r
		\r
			int bb = 4;\r
		\r
			Object c = new Object() {\r
				int a = 55;\r
				Object cdddddddddddd = null;\r
			};\r
		\r
			private enum E {\r
				AAA, BBB;\r
				int a = 55;\r
				String sssss = "ssssss";\r
			}\r
		\r
			private @interface II {\r
				int aaaaaa = 1;\r
				String bbbbbbbbb = "default";\r
			}\r
		}""";
	formatSource(source,
		"""
			import java.util.function.Function;\r
			\r
			public class A {\r
				private Function	mapper				= (Object a) -> {\r
															return a.toString().equals("test");\r
														};\r
				String				ssssssssssssssss	= "dsadaaaaaaaaaaaaaaaaaaaaaaaaa";		//$NON-NLS-1$ //\r
																								// B\r
																								// //\r
																								// A\r
			\r
				int					bb					= 4;\r
			\r
				Object				c					= new Object() {\r
															int		a				= 55;\r
															Object	cdddddddddddd	= null;\r
														};\r
			\r
				private enum E {\r
					AAA, BBB;\r
			\r
					int		a		= 55;\r
					String	sssss	= "ssssss";\r
				}\r
			\r
				private @interface II {\r
					int		aaaaaa		= 1;\r
					String	bbbbbbbbb	= "default";\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/474918 - [formatter] doesn't align fields in declarations of annotations, enums and anonymous classes
 */
public void testBug474918b() {
	useOldCommentWidthCounting();
	this.formatterPrefs.align_type_members_on_columns = true;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		import java.util.function.Function;\r
		\r
		public class A {\r
			private Function mapper = (Object a) -> {\r
				return a.toString().equals("test");\r
			};\r
			String ssssssssssssssss = "dsadaaaaaaaaaaaaaaaaaaaaaaaaa";   //$NON-NLS-1$ // B // A\r
		\r
			int bb = 4;\r
		\r
			Object c = new Object() {\r
				int a = 55;\r
				Object cdddddddddddd = null;\r
			};\r
		\r
			private enum E {\r
				AAA, BBB;\r
				int a = 55;\r
				String sssss = "ssssss";\r
			}\r
		\r
			private @interface II {\r
				int aaaaaa = 1;\r
				String bbbbbbbbb = "default";\r
			}\r
		}""";
	formatSource(source,
		"""
			import java.util.function.Function;\r
			\r
			public class A {\r
			    private Function mapper           = (Object a) -> {\r
			                                          return a.toString().equals("test");\r
			                                      };\r
			    String           ssssssssssssssss = "dsadaaaaaaaaaaaaaaaaaaaaaaaaa";     //$NON-NLS-1$ //\r
			                                                                             // B\r
			                                                                             // //\r
			                                                                             // A\r
			\r
			    int              bb               = 4;\r
			\r
			    Object           c                = new Object() {\r
			                                          int    a             = 55;\r
			                                          Object cdddddddddddd = null;\r
			                                      };\r
			\r
			    private enum E {\r
			        AAA, BBB;\r
			\r
			        int    a     = 55;\r
			        String sssss = "ssssss";\r
			    }\r
			\r
			    private @interface II {\r
			        int    aaaaaa    = 1;\r
			        String bbbbbbbbb = "default";\r
			    }\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/474918 - [formatter] doesn't align fields in declarations of annotations, enums and anonymous classes
 */
public void testBug474918c() {
	useOldCommentWidthCounting();
	this.formatterPrefs.align_type_members_on_columns = true;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		import java.util.function.Function;\r
		\r
		public class A {\r
			private Function mapper = (Object a) -> {\r
				return a.toString().equals("test");\r
			};\r
			String ssssssssssssssss = "dsadaaaaaaaaaaaaaaaaaaaaaaaaa";   //$NON-NLS-1$ // B // A\r
		\r
			int bb = 4;\r
		\r
			Object c = new Object() {\r
				int a = 55;\r
				Object cdddddddddddd = null;\r
			};\r
		\r
			private enum E {\r
				AAA, BBB;\r
				int a = 55;\r
				String sssss = "ssssss";\r
			}\r
		\r
			private @interface II {\r
				int aaaaaa = 1;\r
				String bbbbbbbbb = "default";\r
			}\r
		}""";
	formatSource(source,
		"""
			import java.util.function.Function;\r
			\r
			public class A {\r
				private Function	mapper				= (Object a) -> {\r
															return a.toString().equals("test");\r
														};\r
				String				ssssssssssssssss	= "dsadaaaaaaaaaaaaaaaaaaaaaaaaa";		//$NON-NLS-1$ //\r
				                                                                                // B\r
				                                                                                // //\r
				                                                                                // A\r
			\r
				int					bb					= 4;\r
			\r
				Object				c					= new Object() {\r
															int		a				= 55;\r
															Object	cdddddddddddd	= null;\r
														};\r
			\r
				private enum E {\r
					AAA, BBB;\r
			\r
					int		a		= 55;\r
					String	sssss	= "ssssss";\r
				}\r
			\r
				private @interface II {\r
					int		aaaaaa		= 1;\r
					String	bbbbbbbbb	= "default";\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/475865 - JDT deletes code
 */
public void testBug475865() {
	String source =
		"""
		public class Snippet {\r
		\r
			Runnable disposeRunnable = this::dispose();\r
		\r
			void dispose() {\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/435241 - [1.8][lambda][formatter] if/else within lambda is incorrectly formatted
 */
public void testBug435241() {
	this.formatterPrefs.brace_position_for_block = DefaultCodeFormatterConstants.NEXT_LINE;
	this.formatterPrefs.insert_new_line_before_else_in_if_statement = true;
	String source =
		"""
		public class Snippet {\r
			public static void main(String[] args) {\r
				Executors.newSingleThreadExecutor().execute(() -> {\r
					if (true)\r
					{\r
						System.err.println("foo");\r
					}\r
					else\r
					{\r
						System.err.println("bar");\r
					}\r
				});\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/472815 - [formatter] 'Indent Empty lines' option doesn't work inside empty blocks
 */
public void testBug472815() {
	this.formatterPrefs.number_of_empty_lines_to_preserve = 2;
	String source =
		"""
		public class Snippet {\r
		\r
			int[] a1 = { };\r
			int[] a2 = {\r
			};\r
			int[] a3 = {\r
		\r
			};\r
			int[] a4 = {\r
		\r
		\r
			};\r
			int[] a5 = {\r
		\r
		\r
		\r
			};\r
		\r
			void f1() { }\r
			void f2() {\r
			}\r
			void f3() {\r
		\r
			}\r
			void f4() {\r
		\r
		\r
			}\r
			void f5() {\r
		\r
		\r
		\r
			}\r
		}""";
	formatSource(source,
		"""
			public class Snippet {\r
			\r
				int[] a1 = {};\r
				int[] a2 = {};\r
				int[] a3 = {\r
			\r
				};\r
				int[] a4 = {\r
			\r
			\r
				};\r
				int[] a5 = {\r
			\r
			\r
				};\r
			\r
				void f1() {\r
				}\r
			\r
				void f2() {\r
				}\r
			\r
				void f3() {\r
			\r
				}\r
			\r
				void f4() {\r
			\r
			\r
				}\r
			\r
				void f5() {\r
			\r
			\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/472815 - [formatter] 'Indent Empty lines' option doesn't work inside empty blocks
 */
public void testBug472815b() {
	this.formatterPrefs.number_of_empty_lines_to_preserve = 2;
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"""
		public class Snippet {\r
		\r
			int[] a1 = { };\r
			int[] a2 = {\r
			};\r
			int[] a3 = {\r
		\r
			};\r
			int[] a4 = {\r
		\r
		\r
			};\r
			int[] a5 = {\r
		\r
		\r
		\r
			};\r
		\r
			void f1() { }\r
			void f2() {\r
			}\r
			void f3() {\r
		\r
			}\r
			void f4() {\r
		\r
		\r
			}\r
			void f5() {\r
		\r
		\r
		\r
			}\r
		}""";
	formatSource(source,
		"""
			public class Snippet {\r
				\r
				int[] a1 = {};\r
				int[] a2 = {};\r
				int[] a3 = {\r
						\r
				};\r
				int[] a4 = {\r
						\r
						\r
				};\r
				int[] a5 = {\r
						\r
						\r
				};\r
				\r
				void f1() {\r
				}\r
				\r
				void f2() {\r
				}\r
				\r
				void f3() {\r
					\r
				}\r
				\r
				void f4() {\r
					\r
					\r
				}\r
				\r
				void f5() {\r
					\r
					\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/472413 - [formatter] Wrap all arguments on new lines and prefer outer expressions result is inconsistent
 */
public void testBug472413() {
	this.formatterPrefs.alignment_for_arguments_in_method_invocation =
		DefaultCodeFormatterOptions.Alignment.M_ONE_PER_LINE_SPLIT
		+ DefaultCodeFormatterOptions.Alignment.M_INDENT_BY_ONE;
	this.formatterPrefs.page_width = 80;
	String source =
		"""
		class Snippet {\r
		\r
			void foo1() {\r
				Other.bar(\r
					100,\r
					nestedMethod2Arg(\r
						nestedMethod1Arg(\r
							nestedMethod2Arg(nestedMethod1Arg(nestedMethod2Arg(\r
								nestedMethod1Arg(nestedMethod1Arg(nestedMethod1Arg(\r
									nested(200, 300, 400, 500, 600, 700, 800, 900)))),\r
								null)), null)),\r
						null),\r
					100);\r
			}\r
		\r
			void foo2() {\r
				nestedMethodAAAA(\r
					nestedMethodBBBB(\r
						nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r
						null));\r
				nestedMethodAAAA(nestedMethodBBBB(\r
					nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r
					null));\r
			}\r
		\r
			void foo3() {\r
				nestedMethodAAAA(\r
					nestedMethodBBBB(\r
						nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r
						null),\r
					null);\r
				nestedMethodAAAA(nestedMethodBBBB(\r
					nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r
					null), null);\r
			}\r
		}""";
	formatSource(source,
		"""
			class Snippet {\r
			\r
				void foo1() {\r
					Other.bar(\r
						100,\r
						nestedMethod2Arg(\r
							nestedMethod1Arg(\r
								nestedMethod2Arg(\r
									nestedMethod1Arg(\r
										nestedMethod2Arg(\r
											nestedMethod1Arg(\r
												nestedMethod1Arg(\r
													nestedMethod1Arg(\r
														nested(\r
															200,\r
															300,\r
															400,\r
															500,\r
															600,\r
															700,\r
															800,\r
															900)))),\r
											null)),\r
									null)),\r
							null),\r
						100);\r
				}\r
			\r
				void foo2() {\r
					nestedMethodAAAA(\r
						nestedMethodBBBB(\r
							nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r
							null));\r
					nestedMethodAAAA(\r
						nestedMethodBBBB(\r
							nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r
							null));\r
				}\r
			\r
				void foo3() {\r
					nestedMethodAAAA(\r
						nestedMethodBBBB(\r
							nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r
							null),\r
						null);\r
					nestedMethodAAAA(\r
						nestedMethodBBBB(\r
							nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r
							null),\r
						null);\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/475793 - [formatter] Incorrect whitespace after lambda block
 */
public void testBug475793() {
	this.formatterPrefs.keep_lambda_body_block_on_one_line = DefaultCodeFormatterConstants.ONE_LINE_IF_EMPTY;
	String source =
		"""
		public class C {\r
			public void f() {\r
				Foo.bar(() -> {} , IllegalArgumentException.class);\r
			}\r
		}""";
	formatSource(source,
		"""
			public class C {\r
				public void f() {\r
					Foo.bar(() -> {}, IllegalArgumentException.class);\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/475746 - [formatter] insert-space rules sometimes ignored with anonymous subclass or when Annotations present
 */
public void testBug475746() {
	this.formatterPrefs.keep_lambda_body_block_on_one_line = DefaultCodeFormatterConstants.ONE_LINE_IF_EMPTY;
	this.formatterPrefs.insert_space_after_opening_paren_in_method_invocation = true;
	this.formatterPrefs.insert_space_before_closing_paren_in_method_invocation = true;
	this.formatterPrefs.insert_space_after_opening_paren_in_method_declaration = true;
	this.formatterPrefs.insert_space_before_closing_paren_in_method_declaration = true;
	this.formatterPrefs.insert_space_after_opening_paren_in_constructor_declaration = true;
	this.formatterPrefs.insert_space_before_closing_paren_in_constructor_declaration = true;
	this.formatterPrefs.insert_space_after_opening_paren_in_annotation = true;
	this.formatterPrefs.insert_space_before_closing_paren_in_annotation = true;
	String source =
		"""
		import java.awt.*;\r
		\r
		public class MyClass {\r
		\r
			@Annotation( Arrays.asList( "" ))\r
			static Point p = new Point( x, y) {\r
				@Override\r
				public int hashCode( ) {\r
					return 42;\r
				}\r
			};\r
		\r
			MyClass( @Annotation( "annotationVal" ) String s)\r
			{\r
				Foo.bar( ( @Annotation( "annotationVal" ) int a) -> { } , IllegalArgumentException.class );\r
			}\r
		\r
			public interface I {\r
				void m(int a);\r
			}\r
		}""";
	formatSource(source,
		"""
			import java.awt.*;\r
			\r
			public class MyClass {\r
			\r
				@Annotation( Arrays.asList( "" ) )\r
				static Point p = new Point( x, y ) {\r
					@Override\r
					public int hashCode() {\r
						return 42;\r
					}\r
				};\r
			\r
				MyClass( @Annotation( "annotationVal" ) String s ) {\r
					Foo.bar( ( @Annotation( "annotationVal" ) int a ) -> {}, IllegalArgumentException.class );\r
				}\r
			\r
				public interface I {\r
					void m( int a );\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/477005 - [formatter] NullPointerException when first line is empty and indented
 */
public void testBug477005() {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.blank_lines_before_package = 2;
	String source =
		"""
		\r
		\r
		package test;\r
		\r
		public class MyClass {\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/471202 - [formatter] Extra line break after annotation default expression
 */
public void testBug471202() {
	String source =
		"""
		public @interface MyAnnotation {\r
			Attributes attributes() default @Attributes()\r
			;\r
		\r
			@MyAnnotation(attributes = @Attributes() )\r
			String test();\r
		}""";
	formatSource(source,
		"""
			public @interface MyAnnotation {\r
				Attributes attributes() default @Attributes();\r
			\r
				@MyAnnotation(attributes = @Attributes())\r
				String test();\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/475791 - [formatter] Additional blank line before static initializer
 */
public void testBug475791() {
	this.formatterPrefs.blank_lines_before_new_chunk = 0;
	String source =
		"""
		public class Example {\r
			static String staticField;\r
			static {}\r
			String field;\r
			{}\r
			static String staticField2;\r
			{}\r
			String field2;\r
			static {}\r
			static void staticMethod() {};\r
			static {}\r
			void method() {}\r
			static{}\r
			{}\r
			static class staticClass {};\r
			{}\r
			static{}\r
		}""";
	formatSource(source,
		"""
			public class Example {\r
				static String staticField;\r
				static {\r
				}\r
				String field;\r
				{\r
				}\r
				static String staticField2;\r
				{\r
				}\r
				String field2;\r
				static {\r
				}\r
				static void staticMethod() {\r
				};\r
				static {\r
				}\r
				void method() {\r
				}\r
				static {\r
				}\r
				{\r
				}\r
				static class staticClass {\r
				};\r
				{\r
				}\r
				static {\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/475791 - [formatter] Additional blank line before static initializer
 */
public void testBug475791b() {
	this.formatterPrefs.blank_lines_before_new_chunk = 2;
	String source =
		"""
		public class Example {\r
			static String staticField;\r
			static {}\r
			String field;\r
			{}\r
			static String staticField2;\r
			{}\r
			String field2;\r
			static {}\r
			static void staticMethod() {};\r
			static {}\r
			void method() {}\r
			static{}\r
			{}\r
			static class staticClass {};\r
			{}\r
			static{}\r
		}""";
	formatSource(source,
		"""
			public class Example {\r
				static String staticField;\r
				static {\r
				}\r
				String field;\r
				{\r
				}\r
				static String staticField2;\r
				{\r
				}\r
				String field2;\r
				static {\r
				}\r
			\r
			\r
				static void staticMethod() {\r
				};\r
			\r
			\r
				static {\r
				}\r
			\r
			\r
				void method() {\r
				}\r
			\r
			\r
				static {\r
				}\r
				{\r
				}\r
			\r
			\r
				static class staticClass {\r
				};\r
			\r
			\r
				{\r
				}\r
				static {\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/477430 - [formatter] wrong indentation when nesting anonymous classes
 */
public void testBug477430() {
	this.formatterPrefs.alignment_for_arguments_in_method_invocation =
		DefaultCodeFormatterOptions.Alignment.M_ONE_PER_LINE_SPLIT
		+ DefaultCodeFormatterOptions.Alignment.M_FORCE;
	String source =
		"""
		public class Example {\r
			void foo() {\r
				Object o = new AbstractRegistryConfiguration() {\r
					public void configureRegistry() {\r
						registerConfigAttribute(\r
								new IExportFormatter() {\r
									public Object formatForExport() {\r
										return null;\r
									}\r
								},\r
								null);\r
					}\r
				};\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/480074 - [formatter] Wrong indentation on column for enum constants with javadoc
 */
public void testBug480074() {
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_NEXT_PER_LINE_SPLIT + Alignment.M_INDENT_ON_COLUMN;
	String source =
		"""
		public class Example {
			private enum Something {
									/** hello */
									AAA,
									/** hello */
									BBB
			}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/479959 - [formatter] indented empty lines after ifs and loops without braces
 */
public void testBug479959() {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.number_of_empty_lines_to_preserve = 2;
	String source =
		"""
		public class Example {\r
			\r
			\r
			public boolean foo() {\r
				\r
				\r
				if (foo())\r
					\r
					\r
					return foo();\r
				\r
				\r
				while (foo())\r
					\r
					\r
					foo();\r
				\r
				\r
				do\r
					\r
					\r
					foo();\r
				\r
				\r
				while (foo());\r
				\r
				\r
				if (foo()) {\r
					\r
					\r
					foo();\r
					\r
					\r
				}\r
				\r
				\r
				if (foo())\r
					\r
					\r
					foo();\r
				\r
				\r
				else\r
					\r
					\r
					foo();\r
				\r
				\r
				for (int i = 0; i < 5; i++)\r
					\r
					\r
					foo();\r
				\r
				\r
				switch (4) {\r
				\r
				\r
				case 4:\r
					\r
					\r
					foo();\r
					break;\r
				\r
				\r
				case 5: {\r
					\r
					\r
					break;\r
				}\r
				\r
				\r
				case 6:\r
				}\r
				\r
				\r
				return false;\r
				\r
				\r
			}\r
			\r
			\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/480086 - [formatter] unwanted spaces in generic diamond
 */
public void testBug480086() {
	this.formatterPrefs.insert_space_after_opening_angle_bracket_in_parameterized_type_reference = true;
	this.formatterPrefs.insert_space_before_closing_angle_bracket_in_parameterized_type_reference = true;
	String source =
		"""
		public class Test {\r
			private ArrayList< String > ss = new ArrayList<>();\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/480735 - [formatter] whitespace after comma in enum declaration is removed
 */
public void testBug480735() {
	String source =
		"public enum Example implements Serializable, Cloneable {\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/481221 - [formatter] New formatter incorrectly formats ", ;" in enum declaration
 */
public void testBug481221a() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Test {\r
			public enum Enum0 {\r
			}\r
		\r
			public enum Enum1 {\r
				;\r
			}\r
		\r
			public enum Enum2 {\r
				,;\r
			}\r
		\r
			public enum Enum3 {\r
				,\r
				;\r
			}\r
		\r
			public enum Enum4 {\r
				AAA,;\r
			}\r
		\r
			public enum Enum5 {\r
				AAA,\r
				;\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/481221 - [formatter] New formatter incorrectly formats ", ;" in enum declaration
 */
public void testBug481221b() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_COMPACT_SPLIT + Alignment.M_INDENT_ON_COLUMN;
	String source =
		"""
		public class Test {\r
			public enum Enum1 {\r
				,\r
				;\r
			}\r
		\r
			public enum Enum2 {\r
								AAA,\r
								;\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/483922 - [formatter] Wrong indentation base for wrapped "throws" elements in method declaration
 */
public void testBug483922a() {
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT + Alignment.M_FORCE + Alignment.M_INDENT_ON_COLUMN;
	this.formatterPrefs.alignment_for_throws_clause_in_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT + Alignment.M_FORCE;
	String source =
		"""
		public class Test {\r
			public void foo(\r
							int a, int b)\r
					throws Exception {\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/483922 - [formatter] [formatter] Wrong indentation base for wrapped "throws" elements in method declaration
 */
public void testBug483922b() {
	this.formatterPrefs.alignment_for_parameters_in_constructor_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT + Alignment.M_FORCE + Alignment.M_INDENT_ON_COLUMN;
	this.formatterPrefs.alignment_for_throws_clause_in_constructor_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT + Alignment.M_FORCE;
	String source =
			"""
		public class Test {\r
			public Test(\r
						int a, int b)\r
					throws Exception {\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/485163 - [formatter] Incorrect indentation after line wrap
 */
public void testBug485163() {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.blank_lines_before_field = 1;
	String source =
		"""
		public class Test {\r
		\r
			public String sssss1 = "................................................." + "...........................................";\r
			public String sssss2 = "................................................." + "...........................................";\r
		\r
			public String sssss3 = "................................................." + "...........................................";\r
		\r
			public void foo() {\r
		\r
				String sssss = "................................................." + "...........................................";\r
		\r
				Object o =\r
		\r
				new Object() {\r
		\r
					int a;\r
		\r
					void foo() {\r
		\r
						String sssss1 = "................................................." + "...........................................";\r
		\r
						String sssss2 = "................................................." + "...........................................";\r
		\r
					}\r
		\r
				};\r
		\r
				new Object() {\r
		\r
					int a;\r
		\r
					void foo() {\r
		\r
						String sssss1 = "................................................." + "...........................................";\r
		\r
						String sssss2 = "................................................." + "...........................................";\r
		\r
					}\r
		\r
				};\r
			}\r
		}""";
	formatSource(source,
		"""
			public class Test {\r
				\r
				public String sssss1 = "................................................."\r
						+ "...........................................";\r
				\r
				public String sssss2 = "................................................."\r
						+ "...........................................";\r
				\r
				public String sssss3 = "................................................."\r
						+ "...........................................";\r
				\r
				public void foo() {\r
					\r
					String sssss = "................................................."\r
							+ "...........................................";\r
					\r
					Object o =\r
							\r
							new Object() {\r
								\r
								int a;\r
								\r
								void foo() {\r
									\r
									String sssss1 = "................................................."\r
											+ "...........................................";\r
									\r
									String sssss2 = "................................................."\r
											+ "...........................................";\r
									\r
								}\r
								\r
							};\r
					\r
					new Object() {\r
						\r
						int a;\r
						\r
						void foo() {\r
							\r
							String sssss1 = "................................................."\r
									+ "...........................................";\r
							\r
							String sssss2 = "................................................."\r
									+ "...........................................";\r
							\r
						}\r
						\r
					};\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/479898 - [formatter] removes whitespace between final and first exception in multi-line multi-catch
 */
public void testBug479898() {
	this.formatterPrefs.alignment_for_union_type_in_multicatch = Alignment.M_COMPACT_SPLIT + Alignment.M_INDENT_ON_COLUMN;
	String source =
		"""
		public class FormattingTest {\r
			public void formatterTest() {\r
				try {\r
				} catch (final	InstantiationException | IllegalAccessException | IllegalArgumentException\r
								| NoSuchMethodException e) {\r
				}\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/485276 - [formatter] another ArrayIndexOutOfBoundsException while formatting code
 */
public void testBug485276() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_COMPACT_SPLIT + Alignment.M_INDENT_BY_ONE;
	String source =
		"""
		public class PostSaveListenerCleanUpExceptionTest {\r
			public Object[][] dataProvider() {\r
				return new Object[][] { { new String() // comment 1\r
						}, { new String() } };\r
			}\r
		\r
			Object o = new Object() {\r
				public Object[][] dataProvider() {\r
					return new Object[][] { { new String() // comment 1\r
							}, { new String() } };\r
				}\r
			};\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/487375 - [formatter] block comment in front of method signature effects too much indentation
 */
public void testBug487375() {
	String source =
		"""
		public class Test {\r
			/* public */ void foo() {\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/489797 - [formatter] 'Indent empty lines' sometimes doesn't work with 'format edited lines' save action
 */
public void testBug489797a() {
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"""
		public class Example {\r
			public void foo() {\r
				if (true)\r
					return;\r
		[##]\r
				return;\r
			}\r
		}""";
	formatSource(source,
		"""
			public class Example {\r
				public void foo() {\r
					if (true)\r
						return;\r
					\r
					return;\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/489797 - [formatter] 'Indent empty lines' sometimes doesn't work with 'format edited lines' save action
 */
public void testBug489797b() {
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"""
		public class Example {\r
			public void foo() {\r
				if (true)\r
					return;\r
		[#		#]\r
				return;\r
			}\r
		}""";
	formatSource(source,
		"""
			public class Example {\r
				public void foo() {\r
					if (true)\r
						return;\r
					\r
					return;\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/489797 - [formatter] 'Indent empty lines' sometimes doesn't work with 'format edited lines' save action
 */
public void testBug489797c() {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.number_of_empty_lines_to_preserve = 5;
	String source =
		"""
		public class Example {\r
			public void foo() {\r
				if (true)\r
					return;\r
		[#\r
		#]\r
				return;\r
			}\r
		}""";
	formatSource(source,
		"""
			public class Example {\r
				public void foo() {\r
					if (true)\r
						return;\r
					\r
					\r
					return;\r
				}\r
			}"""
	);
}
/**
 * https://bugs.eclipse.org/488898 - [formatter] Disabled options still have effect
 */
public void testBug488898() {
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_NO_ALIGNMENT + Alignment.M_FORCE;
	String source =
		"""
		class Example {\r
			void foo(int bar) {\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/492735 - [formatter] Excessive wrapping in a complex expression
 */
public void testBug492735() {
	this.formatterPrefs.alignment_for_assignment = Alignment.M_COMPACT_SPLIT;
	this.formatterPrefs.page_width = 60;
	String source =
		"""
		class FormatterIssue {\r
			String[] S = new String[] {\r
					foo("first line  xxxxxxxxxxx", "y", "z"),\r
					foo("second line xxxxxxxxxxxxxxxxxxx", "b",\r
							"c"), };\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/494831 - Formatter ignores whitespace rules for diamond operator
 */
public void testBug494831() {
	this.formatterPrefs.insert_space_before_opening_angle_bracket_in_parameterized_type_reference = true;
	String source =
		"""
		class Example {\r
			List <String> list = new ArrayList <>();\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/497245 - [formatter] Comment between "if" and statement breaks formatting
 */
public void testBug497245a() {
	String source =
		"""
		public class Test {\r
			void method() {\r
				if (true)\r
					// comment\r
					if (false)\r
						method();\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/497245 - [formatter] Comment between "if" and statement breaks formatting
 */
public void testBug497245b() {
	this.formatterPrefs.keep_then_statement_on_same_line = true;
	String source =
		"""
		public class Test {\r
			void method() {\r
				if (true)\r
					// comment\r
					if (false) method();\r
			}\r
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500443 - [formatter] NPE on block comment before 'force-wrap' element
 */
public void testBug500443() {
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_ONE_PER_LINE_SPLIT + Alignment.M_FORCE;
	this.formatterPrefs.alignment_for_superclass_in_type_declaration = Alignment.M_ONE_PER_LINE_SPLIT + Alignment.M_FORCE;
	String source =
		"""
		public class SomeClass
				/* */ extends
				Object {
			enum MyEnum {
				/* 1 */ ONE
			}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500092 - [formatter] Blank lines at beginning of method body doesn't work in constructors
 */
public void testBug500092() {
	this.formatterPrefs.blank_lines_at_beginning_of_method_body = 1;
	String source =
		"""
		public class Test {
			public Test() { int a; }
		}""";
	formatSource(source,
		"""
			public class Test {
				public Test() {
			
					int a;
				}
			}"""
	);
}
/**
 * https://bugs.eclipse.org/500135 - [formatter] 'Parenthesis positions' ignores single member annotations
 */
public void testBug500135() {
	this.formatterPrefs.parenthesis_positions_in_annotation = DefaultCodeFormatterConstants.SEPARATE_LINES;
	String source =
		"""
		@SomeAnnotation(
			"some value"
		)
		public class Test {
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500096 - [formatter] Indent declarations within enum declaration doesn't affect enum constants
 */
public void testBug500096a() {
	this.formatterPrefs.indent_body_declarations_compare_to_enum_declaration_header = false;
	String source =
		"""
		public enum Test {
		AAA, BBB;
		
		Test() {
		}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500096 - [formatter] Indent declarations within enum declaration doesn't affect enum constants
 */
public void testBug500096b() {
	this.formatterPrefs.indent_body_declarations_compare_to_enum_declaration_header = false;
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_COMPACT_SPLIT + Alignment.M_INDENT_BY_ONE;
	String source =
		"""
		public enum Test {
			AAA, BBB;
		
		Test() {
		}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500093 - [formatter] AssertionError with 'Next line on wrap' for array initializers
 */
public void testBug500093() {
	this.formatterPrefs.brace_position_for_array_initializer = DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP;
	this.formatterPrefs.page_width = 60;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		public class SomeClass {
			void foo() {
				Arrays.asList(new String[] { "ddd", "eee", "fff" });
				Arrays.asList(new String[] { "a", "b", "c" },
				        new String[]
				        { "a", "b", "c", });
				Arrays.asList(//
				        new String[]
				        { "ddd", "eee", "fff" });
				Arrays.asList(
				        new String[]
				        { "eedd", "eee", "fff" });
				Arrays.asList(
				        new String[]
				        { "aa", "bb", "cc", "dd", "ee", "ff", "gg",
				                "hh", "ii" });
				String[][] test = { { "aaaaaa", "bbbbb", "ccccc" },
				        { "aaaa", "bb", "ccc" } };
				test[123456 //
				        * (234567 + 345678 + 456789 - 567890
				                - 678901)] = new String[]
				                { "a", "b", "c" };
			}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500853 - [Formatter] java code formatter doesn't honour new parentheses settings
 */
public void testBug500853() {
	this.formatterPrefs.parenthesis_positions_in_method_declaration = new String(DefaultCodeFormatterConstants.PRESERVE_POSITIONS);
	String source =
		"""
		public class SomeClass {
			void foo() {
			}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500853 - [formatter] Errors around formatter:off regions with "use space to indent wrapped lines"
 * test no {@code IndexOutOfBoundsException} is thrown
 */
public void testBug512791a() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		public class Test {
		
			void f() {
				int a = 1 + 2 + 3 + 4;
				f  (   ;
			}
		
			Object o = new Object() {
			};
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500853 - [formatter] Errors around formatter:off regions with "use space to indent wrapped lines"
 * test formatter doesn't get stuck in an infinite loop
 */
public void testBug512791b() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		public class Test {
		
			void f() {
				f  (   ;
			}
		
			Object o = new Object() {
				int a;
			};
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/514591 - [formatter] NegativeArraySizeException with "Never indent line comments on first column"
 * + "Use spaces to indent wrapped lines"
 */
public void testBug514591a() {
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		public class Test {
		
			String s = new StringBuilder()
		// .append("aa")
			        .toString();
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/514591 - [formatter] NegativeArraySizeException with "Never indent line comments on first column"
 * + "Use spaces to indent wrapped lines"
 */
public void testBug514591b() {
	this.formatterPrefs.never_indent_block_comments_on_first_column = true;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		public class Test {
		
			String s = new StringBuilder()
		/* .append("aa") */
			        .toString();
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/525611 - [formatter] 'Wrap all...' policies for chained method invocations:
 * wraps inside the last element instead
 */
public void testBug525611() {
	setPageWidth80();
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_ONE_PER_LINE_SPLIT;
	String source =
		"""
		class Test {
			String s = aaaaaaa()
					.bbbbbbbb()
					.ccccccccc()
					.ddddddddddddd("eeeeeeee" + "fffffff");
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/526992 - [formatter] Never indent line comments
 * on first column - crash in anonymous class inside array declaration
 */
public void testBug526992a() {
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"""
		public class Test {
			Object o = new Object[] { new Object() {
		//
			} };
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/526992 - [formatter] Never indent line comments
 * on first column - crash in anonymous class inside array declaration
 */
public void testBug526992b() {
	this.formatterPrefs.never_indent_block_comments_on_first_column = true;
	String source =
		"""
		public class Test {
			Object o = new Object[] { new Object() {
		/**/
			} };
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/530066 - Formatter completely hangs Eclipse on
 * line comments that split variable initialization
 */
public void testBug530066() {
	this.formatterPrefs.alignment_for_assignment = Alignment.M_COMPACT_SPLIT;
	this.formatterPrefs.wrap_before_assignment_operator = true;
	String source =
		"""
		class Test {
			boolean someVariable
					// comment
					= true;
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/531981 - [formatter] Error on &lt;code&gt;
 * spanning multiple Javadoc tags
 */
public void testBug531981() {
	this.formatterPrefs.comment_indent_parameter_description = true;
	String source =
		"""
		/**
		 * <code>a<code>
		 *
		 * @param   b
		 *               c
		 *            d</code>
		 */
		class Test {
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/373625 - [formatter] preserve whitespace between
 * code and comments fails when aligning fields in columns
 */
public void testBug373625a() {
	this.formatterPrefs.align_type_members_on_columns = true;
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"""
		class C {
			int		a	= 1; // comment1
			String	bb	= null;   // comment2
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/373625 - [formatter] preserve whitespace between
 * code and comments fails when aligning fields in columns
 */
public void testBug373625b() {
	this.formatterPrefs.align_type_members_on_columns = true;
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"""
		class C {
			int		a	= 1; /* comment1 */
			String	bb	= "";   //$NON-NLS-1$
		}""";
	formatSource(source,
		"""
			class C {
				int		a	= 1;	/* comment1 */
				String	bb	= "";   //$NON-NLS-1$
			}"""
	);
}
/**
 * https://bugs.eclipse.org/534225 - [formatter] Align Javadoc tags in
 * columns option causes extra spaces
 */
public void testBug534225() {
	this.formatterPrefs.comment_align_tags_descriptions_grouped = true;
	this.formatterPrefs.comment_indent_parameter_description = true;
	String source =
		"""
		/**
		 * @param args a b c d e f
		 */
		public class C {
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/534742 - Error on save file due to formatter:
 * IndexOutOfBoundsException in CommentWrapExecutor
 */
public void testBug534742() {
	setPageWidth80();
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"""
		class C {
			String ssssssssssss = fffffffffffffffff("aaaaaaaaaaaaaaaaa", bbbbbbbbbbbbbbbbbb); //$NON-NLS-1$
		}""";
	formatSource(source,
		"""
			class C {
				String ssssssssssss = fffffffffffffffff("aaaaaaaaaaaaaaaaa", //$NON-NLS-1$
						bbbbbbbbbbbbbbbbbb);
			}"""
	);
}
/**
 * https://bugs.eclipse.org/536322 - Java formatter misses one level of
 * indentation in enum declaration if Javadoc is present
 */
public void testBug536322() {
	String source =
		"""
		class C {
			/** */
			enum E {
				enum1;
			}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/536552 - Freeze when formatting Java source code
 */
public void testBug536552a() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"""
		// comment
		class C {
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/536552 - Freeze when formatting Java source code
 */
public void testBug536552b() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	this.formatterPrefs.never_indent_block_comments_on_first_column = true;
	String source =
		"""
		/* comment */
		class C {
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/542625 - Formatter fails with OOM when parentheses for if statements are preserving positions
 */
public void testBug542625() {
	this.formatterPrefs.parenthesis_positions_in_if_while_statement = DefaultCodeFormatterConstants.PRESERVE_POSITIONS;
	String source =
		"""
		class C {
			void m() {
				//
				//
				if (
					true)
					;
			}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/543780 - [formatter] Compact 'if else': can't wrap before else statement
 */
public void testBug543780() {
	this.formatterPrefs.keep_then_statement_on_same_line = true;
	this.formatterPrefs.keep_else_statement_on_same_line = true;
	this.formatterPrefs.alignment_for_compact_if = Alignment.M_ONE_PER_LINE_SPLIT + Alignment.M_FORCE;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		class Example {
			int foo(int argument) {
				if (argument == 0)
				    return 0;
				if (argument == 1)
				    return 42;
				else
				    return 43;
			}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/413193 - [formatter] Blank lines before the first declarations and declarations of same kind not respected in enums
 */
public void testBug413193a() {
	this.formatterPrefs.blank_lines_before_first_class_body_declaration = 2;
	this.formatterPrefs.blank_lines_after_last_class_body_declaration = 3;
	this.formatterPrefs.blank_lines_before_new_chunk = 4;
	formatSource(
		"""
			public enum TestEnum {
			
			
				ONE, TWO, THREE;
			
			
			
			
				public int foo() {
					return 0;
				}
			
			
			
			}""");
}
/**
 * https://bugs.eclipse.org/413193 - [formatter] Blank lines before the first declarations and declarations of same kind not respected in enums
 */
public void testBug413193b() {
	this.formatterPrefs.blank_lines_before_first_class_body_declaration = 2;
	this.formatterPrefs.blank_lines_after_last_class_body_declaration = 3;
	this.formatterPrefs.blank_lines_before_new_chunk = 4;
	formatSource(
		"""
			public enum TestEnum {
				ONE, TWO, THREE;
			}""");
}
/**
 * https://bugs.eclipse.org/413193 - [formatter] Blank lines before the first declarations and declarations of same kind not respected in enums
 */
public void testBug413193c() {
	this.formatterPrefs.blank_lines_before_first_class_body_declaration = ~0;
	this.formatterPrefs.blank_lines_after_last_class_body_declaration = ~0;
	this.formatterPrefs.blank_lines_before_new_chunk = ~0;
	String source =
		"""
		public enum TestEnum {
		
			ONE, TWO, THREE;
		
			public int foo() {
				return 0;
			}
		
		}""";
	formatSource(source,
		"""
			public enum TestEnum {
				ONE, TWO, THREE;
				public int foo() {
					return 0;
				}
			}""");
}
/**
 * https://bugs.eclipse.org/551189 - Consistent ArrayIndexOutOfBounds when saving an incorrect Java file when code clean-up is enabled
 */
public void testBug551189() {
	formatSource(
		"""
			public class AAA {
			
			import java.awt.*;
			
			public class BBB {
			    int a;
			
			}}""");
}
/**
 * https://bugs.eclipse.org/220713 - [formatter] Formatting of array initializers in method calls
 */
public void testBug220713() {
	this.formatterPrefs.alignment_for_arguments_in_method_invocation = Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_INDENT_ON_COLUMN | Alignment.M_FORCE;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_NEXT_SHIFTED_SPLIT | Alignment.M_INDENT_ON_COLUMN | Alignment.M_FORCE;
	this.formatterPrefs.insert_new_line_before_closing_brace_in_array_initializer = true;
	formatSource(
		"""
			public class A {
				void f() {
					methodWithArrays(	new Object[] {
														null,
										},
										new Object[] {
														null,
										});
				}
			}""");
}
/**
 * https://bugs.eclipse.org/558421 [formatter] Generate getter/setter creates unnecessary blank line
 */
public void testBug558421() {
	this.formatterPrefs.blank_lines_after_last_class_body_declaration = 1;
	String source =
		"""
		public int getA() {
			return a;
		}""";
	formatSource(source, source, CodeFormatter.K_CLASS_BODY_DECLARATIONS);
}
/**
 * https://bugs.eclipse.org/250656 - [formatter] Formatting selection destroys correct indentation
 */
public void testBug250656() {
	this.formatterPrefs.page_width = 50;
	formatSource(
		"""
			class C {
				void f() {
					doSomething(aaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbb
			[#				+ ccccccccccccccccccc);#]
				}
			}""",
		"""
			class C {
				void f() {
					doSomething(aaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbb
							+ ccccccccccccccccccc);
				}
			}""");
}
/**
 * https://bugs.eclipse.org/559006 - [formatter] Wrong indentation in region after wrapped line
 */
public void testBug559006() {
	this.formatterPrefs.page_width = 50;
	formatSource(
		"""
			class C {
				void f() {
					doSomething(aaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbb,
							+ ccccccccccccccccccc);
			[#		doSomethingElse();#]
				}
			}""",
		"""
			class C {
				void f() {
					doSomething(aaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbb,
							+ ccccccccccccccccccc);
					doSomethingElse();
				}
			}""");
}
/**
 * https://bugs.eclipse.org/560889 - [formatter] Unneeded wraps with "Format edited lines" save action
 */
public void testBug560889() {
	this.formatterPrefs.page_width = 50;
	formatSource(
		"""
			class C {
				void f() {
			[#		doSomething(aaaaaaaaaaaaaaaaaa)#]
							.andThen(ccccccccccccccccccc);
				}
			}""",
		"""
			class C {
				void f() {
					doSomething(aaaaaaaaaaaaaaaaaa)
							.andThen(ccccccccccccccccccc);
				}
			}""");
}
public void testBug563487a() {
	formatSource(
		"""
			class A {
				protected void f() {
					cccccccccccccc
							//\s
							.forEach(c -> {
								aaaaaa();
			[#					bbbbbb();#]
							});
				}
			}""",
		"""
			class A {
				protected void f() {
					cccccccccccccc
							//\s
							.forEach(c -> {
								aaaaaa();
								bbbbbb();
							});
				}
			}""");
}
public void testBug563487b() {
	formatSource(
		"""
			class A {
				protected void f() {
					cccccccccccccc
							//\s
								.forEach(c -> {
									aaaaaa();
			[#					bbbbbb();#]
								});
				}
			}""",
		"""
			class A {
				protected void f() {
					cccccccccccccc
							//\s
								.forEach(c -> {
									aaaaaa();
								bbbbbb();
								});
				}
			}""");
}
public void testBug563487c() {
	formatSource(
		"""
			class A {
			protected void f() {
			cccccccccccccc
					//\s
					.forEach(c -> {
						aaaaaa();
			[#			bbbbbb();#]
					});
			}
			}""",
		"""
			class A {
			protected void f() {
			cccccccccccccc
					//\s
					.forEach(c -> {
						aaaaaa();
						bbbbbb();
					});
			}
			}""");
}
/**
 * https://bugs.eclipse.org/565053 - [formatter] Parenthesis in "separate lines if wrapped": wrapping disruptions
 */
public void testBug565053a() {
	this.formatterPrefs.parenthesis_positions_in_method_invocation = DefaultCodeFormatterConstants.SEPARATE_LINES_IF_WRAPPED;
	this.formatterPrefs.page_width = 92;
	formatSource(
		"""
			class Example {
			
				List SUPPORTED_THINGS = asList(
						new Thing(
								"rocodileaaadasgasgasgasgasgasgaaaaasgsgasgasgasgasfafghasfaa aaadad"
						), "new Thing()"
				);
			}""");
}
/**
 * https://bugs.eclipse.org/565053 - [formatter] Parenthesis in "separate lines if wrapped": wrapping disruptions
 */
public void testBug565053b() {
	this.formatterPrefs.parenthesis_positions_in_method_invocation = DefaultCodeFormatterConstants.SEPARATE_LINES_IF_WRAPPED;
	this.formatterPrefs.page_width = 100;
	formatSource(
		"""
			class Example {
			
				List SUPPORTED_THINGS = asList(
						new Thing("rocodileaaadasgasgasgasgasgasgaaaaasgsgasgasgasgasfafghasfaa aaadad")
						"new Thing()"
				);
			}""");
}
/**
 * https://bugs.eclipse.org/567714 - [15] Formatting record file moves annotation to the line of record declaration
 */
public void testBug567714() {
	formatSource(
		"""
			@SuppressWarnings("preview")
			@Deprecated
			public record X(int i) {
				public X(int i) {
					this.i = i;
				}
			}""");
}
/**
 * https://bugs.eclipse.org/569798 - [formatter] Brace position - next line indented: bug for array within annotation
 */
public void testBug569798() {
	this.formatterPrefs.brace_position_for_array_initializer = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
	formatSource(
		"""
			class Test {
				@Nullable
				@SuppressWarnings(
					{ "" })
				@Something(a =
					{ "" })
				void f() {
				}
			}"""
	);
}
/**
 * https://bugs.eclipse.org/569964 - [formatter] Keep braced code on one line: problem with comments after javadoc
 */
public void testBug569964() {
	this.formatterPrefs.keep_method_body_on_one_line = DefaultCodeFormatterConstants.ONE_LINE_IF_EMPTY;
	formatSource(
		"""
			class Test {
				/**
				 * More Java doc comment
				 */
				// A line comment
				/* package */ void nothing() {}
			}"""
	);
}
/**
 * https://bugs.eclipse.org/570220 - [formatter] Bug for 'if' open parenthesis inside lambda body preceded by comment line
 */
public void testBug570220() {
	this.formatterPrefs.brace_position_for_block = DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP;
	formatSource(
		"""
			class C {
				Runnable r = () -> {
					//
					if (true) {
					}
				};
			}""");
}

public void _testBug562818() {
	String source = "public Record   {}\n";
	formatSource(source,
		"public Record {\n" +
		"}",
		CodeFormatter.K_CLASS_BODY_DECLARATIONS);
}
/**
 * https://bugs.eclipse.org/574437 - Incorrect formatting in pattern instanceof
 */
public void testBug574437() {
	formatSource(
		"""
			class C {
				void foo(Object o) {
					if ((o) instanceof String s)
						bar(s);
				}
			}""");
}
/**
 * https://bugs.eclipse.org/576954 - [formatter] Switch expression formatting broken in a method chain with lambdas
 */
public void testBug576954() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.indent_switchstatements_compare_to_switch = true;
	formatSource(
		"""
			public class C {
				void f() {
					Stream.of(1, 2)
							.map(it -> switch (it) {
								case 1 -> "one";
								case 2 -> "two";
								default -> "many";
							}).forEach(System.out::println);
				}
			}""");
}
/**
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/443
 */
public void testIssue443a() {
	setComplianceLevel(CompilerOptions.VERSION_17);
	this.formatterPrefs.insert_space_after_closing_angle_bracket_in_type_parameters = true;
	formatSource(
		"record MyRecord<A>() {\n" +
		"}");
}
public void testIssue443b() {
	setComplianceLevel(CompilerOptions.VERSION_17);
	this.formatterPrefs.insert_space_after_closing_angle_bracket_in_type_parameters = false;
	formatSource(
		"""
			class MyClass<A> extends AnotherClass {
			}
			
			sealed interface Expr<A> permits MathExpr {
			}""");
}
/**
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/369
 */
public void testIssue369() {
	setComplianceLevel(CompilerOptions.VERSION_17);
	this.formatterPrefs.insert_space_after_opening_paren_in_record_declaration = true;
	this.formatterPrefs.insert_space_before_closing_paren_in_record_declaration = true;
	formatSource(
		"""
			@JsonPropertyOrder({ "position", "value" })
			public record ValueWithPosition( String position, String value ) {
			}""");
}
/**
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1510
 */
public void testIssue1510() {
	setComplianceLevel(CompilerOptions.VERSION_15);
	this.formatterPrefs.comment_format_line_comment = true;
	String source =
			"""
		class A {
			public void foo() {
				String x=\"""
			abc
			\"""; //$NON-NLS-1$
			}
		}""";

	formatSource(source,
		"""
			class A {
				public void foo() {
					String x = \"""
							abc
							\"""; //$NON-NLS-1$
				}
			}""");
}
/**
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1624
 */
public void testIssue1624() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	this.formatterPrefs.alignment_for_logical_operator = Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_FORCE;
	String source =
		"""
		class A {
			private void foo(String a) {
				boolean b = "GET".equals(a) || "POST".equals(a); //$NON-NLS-1$//$NON-NLS-2$
				if (a == null)
					a = "W"; //$NON-NLS-1$
				// foo
				// bar
			}
		}
		""";
	formatSource(source,
		"""
		class A {
			private void foo(String a) {
				boolean b = "GET".equals(a) //$NON-NLS-1$
				        || "POST".equals(a); //$NON-NLS-1$
				if (a == null)
					a = "W"; //$NON-NLS-1$
				// foo
				// bar
			}
		}
		""");
}
}