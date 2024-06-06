/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Robin Stocker - Bug 49619 - [formatting] comment formatter leaves whitespace in comments
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Bad line breaking in Eclipse javadoc comments - https://bugs.eclipse.org/348338
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.util.Hashtable;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

import junit.framework.Test;

public class FormatterCommentsBugsTest extends FormatterCommentsTests {

	private static final IPath OUTPUT_FOLDER = new Path("out");

public static Test suite() {
	return buildModelTestSuite(FormatterCommentsBugsTest.class);
}
static {
	//TESTS_NAMES = new String[] { "testBug287833b" } ;
}
public FormatterCommentsBugsTest(String name) {
    super(name);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterCommentsTests#getOutputFolder()
 */
@Override
IPath getOutputFolder() {
	return OUTPUT_FOLDER;
}

/**
 * bug 196308: [formatter] Don't escape entity when formatting in {@code <pre>} tags within javadoc comments
 * test Ensure that entity are not escaped when formatting in {@code <pre>} tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=196308"
 */
public void testBug196308() throws JavaModelException {
	String source =
		"""
		public class X {
		
			/**
			 * <pre>
			 * &at;MyAnnotation
			 * </pre>
			 */
		}
		""";
	formatSource(source,
		"""
			public class X {
			
				/**
				 * <pre>
				 * &at;MyAnnotation
				 * </pre>
				 */
			}
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=204257
public void testBug196308b() throws JavaModelException {
	String source =
		"""
		public class A
		{
		  /**
		   * <pre>
		   *   &#92;u
		   * </pre>
		   */
		  public void a()
		  {
		  }
		}
		""";
	formatSource(source,
		"""
			public class A {
				/**
				 * <pre>
				 *   &#92;u
				 * </pre>
				 */
				public void a() {
				}
			}
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=238547
public void testBug196308c() throws JavaModelException {
	String source =
		"""
		/**
		 * &#x01;&#x20;&#x21;&#x40;&#x41;&#233;
		 * <pre>&#x01;&#x20;&#x21;&#x40;&#x41;&#233;</pre>
		 */
		public class TestClass {}
		""";
	formatSource(source,
		"""
			/**
			 * &#x01;&#x20;&#x21;&#x40;&#x41;&#233;
			 *\s
			 * <pre>
			 * &#x01;&#x20;&#x21;&#x40;&#x41;&#233;
			 * </pre>
			 */
			public class TestClass {
			}
			"""
	);
}

/**
 * bug 198963: [formatter] 3.3 Code Formatter repeatedly indents block comment
 * test Ensure that no the formatter indents the block comment only once
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=198963"
 */
public void testBug198963_Tabs01() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"""
		public class Test {
		
		    int x = 0; /*
		    * XXXX
		    */
		}""";
	formatSource(source,
		"""
			public class Test {
			
				int x = 0; /*
							* XXXX
							*/
			}"""
	);
}
public void testBug198963_Tabs02() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"""
		public class Test {
		
		    int x = 10; /*
		    * XXXX
		    */
		}""";
	formatSource(source,
		"""
			public class Test {
			
				int x = 10; /*
							* XXXX
							*/
			}"""
	);
}
public void testBug198963_Tabs03() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"""
		public class Test {
		
		    int x = 100; /*
		    * XXXX
		    */
		}""";
	formatSource(source,
		"""
			public class Test {
			
				int x = 100; /*
								* XXXX
								*/
			}"""
	);
}
public void testBug198963_Tabs04() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"""
		public class Test {
		
		    int x = 0; /*
		                      * XXXX
		                        */
		}""";
	formatSource(source,
		"""
			public class Test {
			
				int x = 0; /*
							       * XXXX
							         */
			}"""
	);
}
public void testBug198963_Tabs05() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"""
		public class Test {
		
		        /*
		             * XXXX
		               */
		    int x = 0;
		}""";
	formatSource(source,
		"""
			public class Test {
			
				/*
				     * XXXX
				       */
				int x = 0;
			}"""
	);
}
public void testBug198963_Tabs06() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"""
		public class Test {
		
		            /*
		         * XXXX
		       */
		    int x = 0;
		}""";
	formatSource(source,
		"""
			public class Test {
			
				/*
				* XXXX
				*/
				int x = 0;
			}"""
	);
}
public void testBug198963_Spaces01() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		public class Test {
		
		    int x = 0; /*
		    * XXXX
		    */
		}""";
	formatSource(source,
		"""
			public class Test {
			
			    int x = 0; /*
			               * XXXX
			               */
			}"""
	);
}
public void testBug198963_Spaces02() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		public class Test {
		
		    int x = 10; /*
		    * XXXX
		    */
		}""";
	formatSource(source,
		"""
			public class Test {
			
			    int x = 10; /*
			                * XXXX
			                */
			}"""
	);
}
public void testBug198963_Spaces03() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		public class Test {
		
		    int x = 100; /*
		    * XXXX
		    */
		}""";
	formatSource(source,
		"""
			public class Test {
			
			    int x = 100; /*
			                 * XXXX
			                 */
			}"""
	);
}
public void testBug198963_Spaces04() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		public class Test {
		
		    int x = 0; /*
		                      * XXXX
		                        */
		}""";
	formatSource(source,
		"""
			public class Test {
			
			    int x = 0; /*
			                      * XXXX
			                        */
			}"""
	);
}
public void testBug198963_Spaces05() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		public class Test {
		
		        /*
		             * XXXX
		               */
		    int x = 0;
		}""";
	formatSource(source,
		"""
			public class Test {
			
			    /*
			         * XXXX
			           */
			    int x = 0;
			}"""
	);
}
public void testBug198963_Spaces06() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"""
		public class Test {
		
		            /*
		         * XXXX
		       */
		    int x = 0;
		}""";
	formatSource(source,
		"""
			public class Test {
			
			    /*
			    * XXXX
			    */
			    int x = 0;
			}"""
	);
}
public void testBug198963_Mixed01() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"""
		public class Test {
		
		    int x = 0; /*
		    * XXXX
		    */
		}""";
	formatSource(source,
		"""
			public class Test {
			
				int x = 0; /*
						   * XXXX
						   */
			}"""
	);
}
public void testBug198963_Mixed02() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"""
		public class Test {
		
		    int x = 10; /*
		    * XXXX
		    */
		}""";
	formatSource(source,
		"""
			public class Test {
			
				int x = 10; /*
							* XXXX
							*/
			}"""
	);
}
public void testBug198963_Mixed03() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"""
		public class Test {
		
		    int x = 100; /*
		    * XXXX
		    */
		}""";
	formatSource(source,
		"""
			public class Test {
			
				int x = 100; /*
							 * XXXX
							 */
			}"""
	);
}
public void testBug198963_Mixed04() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"""
		public class Test {
		
		    int x = 0; /*
		                      * XXXX
		                        */
		}""";
	formatSource(source,
		"""
			public class Test {
			
				int x = 0; /*
						          * XXXX
						            */
			}"""
	);
}
public void testBug198963_Mixed05() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"""
		public class Test {
		
		        /*
		             * XXXX
		               */
		    int x = 0;
		}""";
	formatSource(source,
		"""
			public class Test {
			
				/*
				     * XXXX
				       */
				int x = 0;
			}"""
	);
}
public void testBug198963_Mixed06() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"""
		public class Test {
		
		            /*
		         * XXXX
		       */
		    int x = 0;
		}""";
	formatSource(source,
		"""
			public class Test {
			
				/*
				* XXXX
				*/
				int x = 0;
			}"""
	);
}

/**
 * bug 204091: [formatter] format region in comment introduces comment start/end tokens
 * test Ensure that a region inside a javadoc comment is well formatted
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=204091"
 */
// NOT_FIXED_YET
public void _testBug204091() {
	String source =
		"""
		public class Test {\r
			/**\r
			 * Don't format this:\r
			 *    it has been formatted by the user!\r
			 * \r
			 * [#@param    param   format   this comment    #]\r
			 */\r
			public void foo() {\r
			}\r
		}""";
	formatSource(source,
		"""
			public class Test {\r
				/**\r
				 * Don't format this:\r
				 *    it has been formatted by the user!\r
				 * \r
				 * @param param
				 *            format this comment
				 */\r
				public void foo() {\r
				}\r
			}"""
	);
}

/**
 * bug 217108: [formatter] deletes blank lines between comments
 * test Ensure that blank lines are preserved
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=217108"
 */
public void testBug217108a() {
	String source =
		"""
		public class Test {
		
		    /* a */
		    // b
		
		    int i;
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/* a */
				// b
			
				int i;
			
			}
			"""
	);
}
public void testBug217108b() {
	String source =
		"""
		public class Test {
		
		    /* a */
		
		    // b
		
		    int i;
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/* a */
			
				// b
			
				int i;
			
			}
			"""
	);
}
public void testBug217108c() {
	String source =
		"""
		public class Test {
		
		    // b
		    /* a */
		
		    int i;
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				// b
				/* a */
			
				int i;
			
			}
			"""
	);
}
public void testBug217108d() {
	String source =
		"""
		public class Test {
		
		    // b
		
		    /* a */
		
		    int i;
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				// b
			
				/* a */
			
				int i;
			
			}
			"""
	);
}
public void testBug217108e() {
	String source =
		"""
		public class Test {
		
		    // a
		
		    // b
		
		    int i;
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				// a
			
				// b
			
				int i;
			
			}
			"""
	);
}
public void testBug217108f() {
	String source =
		"""
		public class Test {
		
		    // a
		    // b
		
		    int i;
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				// a
				// b
			
				int i;
			
			}
			"""
	);
}
public void testBug217108g() {
	String source =
		"""
		public class Test {
		
		    /** a */
		    // b
		
		    int i;
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/** a */
				// b
			
				int i;
			
			}
			"""
	);
}
public void testBug217108h() {
	String source =
		"""
		public class Test {
		
		    /** a */
		
		    // b
		
		    int i;
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/** a */
			
				// b
			
				int i;
			
			}
			"""
	);
}

/**
 * bug 228652: [formatter] New line inserted while formatting a region of a compilation unit.
 * test Ensure that no new line is inserted before the formatted region
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228652"
 */
public void testBug228652() {
	String source =
		"""
		package a;\r
		\r
		public class Test {\r
		\r
			private int field;\r
			\r
			[#/**\r
			 * fds \r
			 */#]\r
			public void foo() {\r
			}\r
		}""";
	formatSource(source,
		"""
			package a;\r
			\r
			public class Test {\r
			\r
				private int field;\r
				\r
				/**\r
				 * fds\r
				 */\r
				public void foo() {\r
				}\r
			}"""
	);
}

/**
 * bug 230944: [formatter] Formatter does not respect /*-
 * test Ensure that new formatter does not format block comment starting with '/*-'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=230944"
 */
public void testBug230944a() throws JavaModelException {
	formatUnit("bugs.b230944", "X01.java");
}
public void testBug230944b() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b230944", "X02.java");
}

/**
 * bug 231263: [formatter] New JavaDoc formatter wrongly indent tags description
 * test Ensure that new formatter indent tags description as the old one
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=231263"
 */
public void testBug231263() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("bugs.b231263", "BadFormattingSample.java");
}
public void testBug231263a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	useOldJavadocTagsFormatting();
	formatUnit("bugs.b231263", "X.java");
}

/**
 * bug 231297: [formatter] New JavaDoc formatter wrongly split inline tags before reference
 * test Ensure that new formatter do not split reference in inline tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=231297"
 */
public void testBug231297() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b231297", "X.java");
}
public void testBug231297a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 30;
	formatUnit("bugs.b231297", "X01.java");
}
public void testBug231297b() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed non formatted inline tag description
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b231297", "X02.java");
}
public void testBug231297c() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed non formatted inline tag description
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b231297", "X03.java");
}
public void testBug231297d() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed non formatted inline tag description
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b231297", "X03b.java");
}

/**
 * bug 232285: [formatter] New comment formatter wrongly formats javadoc header/footer with several contiguous stars
 * test Ensure that new formatter do not add/remove stars in header and footer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232285"
 */
public void testBug232285a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01.java");
}
public void testBug232285b() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01b.java");
}
public void testBug232285c() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01c.java");
}
public void testBug232285d() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01d.java");
}
public void testBug232285e() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01e.java");
}
public void testBug232285f() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01f.java");
}
public void testBug232285g() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X02.java");
}
public void testBug232285h() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X03.java");
}
public void testBug232285i() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X04.java");
}
public void testBug232285j() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X04b.java");
}

/**
 * bug 232488: [formatter] Code formatter scrambles JavaDoc of Generics
 * test Ensure that comment formatter format properly generic param tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232488"
 */
public void testBug232488() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	useOldJavadocTagsFormatting();
	formatUnit("bugs.b232488", "X01.java");
}

/**
 * bug 232466: [formatter] References of inlined tags are still split in certain circumstances
 * test Ensure that new formatter do not add/remove stars in header and footer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232466"
 */
public void testBug232466a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232466", "X01.java");
}
public void testBug232466b() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	setPageWidth80();
	formatUnit("bugs.b232466", "X02.java");
}

/**
 * bug 232768: [formatter] does not format block and single line comment if too much selected
 * test Ensure that the new comment formatter formats comments touched by the selection
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232768"
 */
public void testBug232768a() throws JavaModelException {
	String source = "public class A {\r\n" +
			"[#\r\n" +
			"        /*\r\n" +
			"         * A block comment \r\n" +
			"         * on two lines\r\n" +
			"         */\r\n" +
			"\r\n#]" +
			"}\r\n" +
			"";
	formatSource(source,
		"""
			public class A {
			
				/*
				 * A block comment on two lines
				 */
			
			}
			"""
	);
}
public void testBug232768b() throws JavaModelException {
	String source = """
		public class B {\r
		[#\r
		        public void \r
		        foo() {}\r
		#]\r
		        /*\r
		         * A block comment \r
		         * on two lines\r
		         */\r
		\r
		}\r
		""";
	formatSource(source,
		"""
			public class B {
			
				public void foo() {
				}
			
			        /*\r
			         * A block comment\s
			         * on two lines
			         */
			
			}
			"""
	);
}
public void testBug232768_Javadoc01() throws JavaModelException {
	// Selection starts before and ends after the javadoc comment
	String source = """
		public class C {
		\t
		[#        /**
		         * a
		         * b
		         * c
		         * d
		         * .
		         */
		        void		m1  (   )   {
		\t
		        }    \s
		#]
		
		}""";
	formatSource(source,
		"""
			public class C {
			\t
				/**
				 * a b c d .
				 */
				void m1() {
			
				}
			
			}"""
	);
}
public void testBug232768_Javadoc02() throws JavaModelException {
	// Selection starts at javadoc comment begin and ends after it
	String source = """
		public class C {
		\t
		        [#/**
		         * a
		         * b
		         * c
		         * d
		         * .
		         */
		        void		m1  (   )   {
			#]
		        }    \s
		
		
		}""";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"""
			public class C {
			\t
			        /**
				 * a b c d .
				 */
				void m1() {
			
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Javadoc03() throws JavaModelException {
	// Selection starts inside the javadoc comment and ends after it
	String source = """
		public class C {
		\t
		        /**
		         * a
		         * b
		         * [#c
		         * d
		         * .
		         */
		        void		#]m1  (   )   {
		\t
		        }    \s
		
		
		}""";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"""
			public class C {
			\t
			        /**
					 * a b c d .
					 */
					void m1  (   )   {
			\t
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Javadoc04() throws JavaModelException {
	// Selection starts before the javadoc comment and ends at its end
	String source = """
		public class C {
		[#\t
		        /**
		         * a
		         * b
		         * c
		         * d
		         * .
		         */#]
		        void		m1  (   )   {
		\t
		        }    \s
		
		
		}""";
	formatSource(source,
		"""
			public class C {
			
				/**
				 * a b c d .
				 */
			        void		m1  (   )   {
			\t
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Javadoc05() throws JavaModelException {
	// Selection starts before the javadoc comment and ends inside it
	String source = """
		[#   public     class			C{
		\t
		        /**
		         * a
		         * b
		         * c#]
		         * d
		         * .
		         */
		        void		m1  (   )   {
		\t
		        }    \s
		
		
		}""";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"""
			public class C {
			
				/**
				 * a b c d .
				 */
			        void		m1  (   )   {
			\t
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Javadoc06() throws JavaModelException {
	// Selection starts and ends inside the javadoc comment
	String source = """
		   public     class			C{   \s
		\t
		        /**
		         * a
		         * b
		         * [#c#]
		         * d
		         * .
		         */
		        void		m1  (   )   {
		\t
		        }    \s
		
		
		}""";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"""
			   public     class			C{   \s
			\t
			        /**
					 * a b c d .
					 */
			        void		m1  (   )   {
			\t
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Block01() throws JavaModelException {
	// Selection starts before and ends after the block comment
	String source = """
		public class D {
		\t
		[#        /*
		         * a
		         * b
		         * c
		         * d
		         * .
		         */
		        void		m2  (   )   {
		\t
		        }    \s
		#]
		
		}""";
	formatSource(source,
		"""
			public class D {
			\t
				/*
				 * a b c d .
				 */
				void m2() {
			
				}
			
			}"""
	);
}
public void testBug232768_Block02() throws JavaModelException {
	// Selection starts at block comment begin and ends after it
	String source = """
		public class D {
		\t
		        [#/*
		         * a
		         * b
		         * c
		         * d
		         * .
		         */
		        void		m2  (   )   {
			#]
		        }    \s
		
		
		}""";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"""
			public class D {
			\t
			        /*
				 * a b c d .
				 */
				void m2() {
			
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Block03() throws JavaModelException {
	// Selection starts inside the block comment and ends after it
	String source = """
		public class D {
		\t
		        /*
		         * a
		         * b
		         * [#c
		         * d
		         * .
		         */
		        void		#]m2  (   )   {
		\t
		        }    \s
		
		
		}""";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"""
			public class D {
			\t
			        /*
					 * a b c d .
					 */
					void m2  (   )   {
			\t
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Block04() throws JavaModelException {
	// Selection starts before the block comment and ends at its end
	String source = """
		public class D {
		[#\t
		        /*
		         * a
		         * b
		         * c
		         * d
		         * .
		         */#]
		        void		m2  (   )   {
		\t
		        }    \s
		
		
		}""";
	formatSource(source,
		"""
			public class D {
			
				/*
				 * a b c d .
				 */
			        void		m2  (   )   {
			\t
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Block05() throws JavaModelException {
	// Selection starts before the block comment and ends inside it
	String source = """
		[#   public     class			D{
		\t
		        /*
		         * a
		         * b
		         * c#]
		         * d
		         * .
		         */
		        void		m2  (   )   {
		\t
		        }    \s
		
		
		}""";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"""
			public class D {
			
				/*
				 * a b c d .
				 */
			        void		m2  (   )   {
			\t
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Block06() throws JavaModelException {
	// Selection starts and ends inside the block comment
	String source = """
		   public     class			D{   \s
		\t
		        /*
		         * a
		         * b
		         * [#c#]
		         * d
		         * .
		         */
		        void		m2  (   )   {
		\t
		        }    \s
		
		
		}""";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"""
			   public     class			D{   \s
			\t
			        /*
					 * a b c d .
					 */
			        void		m2  (   )   {
			\t
			        }    \s
			
			
			}"""
	);
}
public void testBug232768_Line01() throws JavaModelException {
	// Selection starts before and ends after the line comment
	String source = """
		public class E {
		\t
		
		[#        void            m3()         { // this        is        a    bug
		
		        }
		#]  \s
		}""";
	formatSource(source,
		"""
			public class E {
			\t
			
				void m3() { // this is a bug
			
				}
			  \s
			}"""
	);
}
public void testBug232768_Line02() throws JavaModelException {
	// Selection starts at line comment begin and ends after it
	String source = """
		public class E {
		\t
		
		        void            m3()         { [#// this        is        a    bug
		
		#]\
		        }
		  \s
		}""";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"""
			public class E {
			\t
			
			        void            m3()         { // this is a bug
			
			        }
			  \s
			}"""
	);
}
public void testBug232768_Line03() throws JavaModelException {
	// Selection starts inside line comment and ends after it
	String source = """
		public class E {
		\t
		
		        void            m3()         { // this        [#is        a    bug
		
		        }
		  \s
		              }#]""";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"""
			public class E {
			\t
			
			        void            m3()         { // this        is a bug
			
					}
			
				}"""
	);
}
public void testBug232768_Line04() throws JavaModelException {
	// Selection starts before the line comment and ends at its end
	String source = """
		public class E {[#      \s
		\t
		
		        void            m3()         { // this        is        a    bug#]
		
		        }
		  \s
				}""";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"""
			public class E {
			
				void m3() { // this is a bug
			
			        }
			  \s
					}"""
	);
}
public void testBug232768_Line05() throws JavaModelException {
	// Selection starts before the line comment and ends inside it
	String source = """
		public class E {      \s
		\t
		
		[#\
		        void            m3()         { // this   #]     is        a    bug
		
		        }
		  \s
				}""";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"""
			public class E {      \s
			\t
			
				void m3() { // this     is        a    bug
			
			        }
			  \s
					}"""
	);
}
public void testBug232768_Line06() throws JavaModelException {
	// Selection starts and ends inside the line comment
	String source = """
		public class E {      \s
		\t
		
		        void            m3()         { // this        is        [#a#]    bug
		
		        }
		  \s
		              }""";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"""
			public class E {      \s
			\t
			
			        void            m3()         { // this        is        a    bug
			
			        }
			  \s
			              }"""
	);
}
public void testBug232768_Line07() throws JavaModelException {
	// Selection starts and ends inside the line comment
	String source = """
		public class F {
		\t
		
		        void            m3()         {    \s
		[#        	// this        is        a    bug
		#]
		        }
		  \s
		}""";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"""
			public class F {
			\t
			
			        void            m3()         {    \s
						// this is a bug
			
			        }
			  \s
			}"""
	);
}
public void testBug232768_Line08() throws JavaModelException {
	// Selection starts and ends inside the line comment
	String source = """
		public class G {
			void foo() {
		\t
		        // Now we parse one of 'CustomActionTagDependent',
		        // 'CustomActionJSPContent', or 'CustomActionScriptlessContent'.
		        // depending on body-content in TLD.
			}
		}""";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"""
			public class G {
				void foo() {
			
					// Now we parse one of 'CustomActionTagDependent',
					// 'CustomActionJSPContent', or 'CustomActionScriptlessContent'.
					// depending on body-content in TLD.
				}
			}"""
	);
}

/**
 * bug 232788: [formatter] Formatter misaligns stars when formatting block comments
 * test Ensure that block comment formatting is correct even with indentation size=1
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232788"
 */
public void testBug232788_Tabs01() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X01_tabs.java");
}
public void testBug232788_Spaces01() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X01_spaces.java");
}
public void testBug232788_Mixed01() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X01_mixed.java");
}
public void testBug232788_Tabs02() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	formatUnit("bugs.b232788", "X02_tabs.java");
}
public void testBug232788_Spaces02() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	formatUnit("bugs.b232788", "X02_spaces.java");
}
public void testBug232788_Mixed02() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	formatUnit("bugs.b232788", "X02_mixed.java");
}
public void testBug232788_Tabs03() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X03_tabs.java");
}
public void testBug232788_Spaces03() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X03_spaces.java");
}
public void testBug232788_Mixed03() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X03_mixed.java");
}

/**
 * bug 233011: [formatter] Formatting edited lines has problems (esp. with comments)
 * test Ensure that new comment formatter format all comments concerned by selections
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233011"
 */
public void testBug233011() throws JavaModelException {
	String source = """
		
		public class E01 {
		        /**\s
		         * Javadoc      [# #]           \s
		         * comment
		         */
		        /*
		         * block           [# #]           \s
		         * comment
		         */
		        // [#single                       line#] comment
		}""";
	formatSource(source,
		"""
			
			public class E01 {
			        /**
					 * Javadoc comment
					 */
			        /*
					 * block comment
					 */
			        // single line comment
			}"""
	);
}

/**
 * bug 233228: [formatter] line comments which contains \\u are not correctly formatted
 * test Ensure that the new formatter is not screwed up by invalid unicode value inside comments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233228"
 */
public void testBug233228a() throws JavaModelException {
	formatUnit("bugs.b233228", "X01.java");
}
public void testBug233228b() throws JavaModelException {
	formatUnit("bugs.b233228", "X01b.java");
}
public void testBug233228c() throws JavaModelException {
	formatUnit("bugs.b233228", "X01c.java");
}
public void testBug233228d() throws JavaModelException {
	formatUnit("bugs.b233228", "X02.java");
}
public void testBug233228e() throws JavaModelException {
	formatUnit("bugs.b233228", "X03.java");
}

/**
 * bug 233224: [formatter] Xdoclet tags looses @ on format
 * test Ensure that doclet tags are preserved while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233224"
 */
public void testBug233224() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b233224", "X01.java");
}

/**
 * bug 233259: [formatter] html tag should not be split by formatter
 * test Ensure that html tag is not split by the new comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233259"
 */
public void testBug233259a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"""
		public class X {
		        /**
		         * @see <a href="http://0">Test</a>
		         */
		        void foo() {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
				/**
				 * @see <a href="http://0">Test</a>
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug233259b() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.comment_line_length = 40;
	// difference with 3.3 formatter:
	// split html reference as this allow not to go over the max line width
	String source =
		"""
		public class X {
		        /**
		         * @see <a href="http://0123">Test</a>
		         */
		        void foo() {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
				/**
				 * @see <a href=
				 *      "http://0123">Test</a>
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug233259c() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"""
		public class X {
		        /**
		         * @see <a href="http://012346789">Test</a>
		         */
		        void foo() {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
				/**
				 * @see <a href=
				 *      "http://012346789">Test</a>
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug233259d() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"""
		public class X {
		        /**
		         * @see <a href="http://012346789012346789012346789">Test</a>
		         */
		        void foo() {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
				/**
				 * @see <a href=
				 *      "http://012346789012346789012346789">Test</a>
				 */
				void foo() {
				}
			}
			"""
	);
}

/**
 * bug 237942: [formatter] String references are put on next line when over the max line length
 * test Ensure that string reference is not put on next line when over the max line width
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237942"
 */
public void testBug237942a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"""
		public class X {
		        /**
		         * @see "string reference: 01234567"
		         */
		        void foo() {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
				/**
				 * @see "string reference: 01234567"
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug237942b() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	// difference with 3.3 formatter:
	// do not split string reference as this can lead to javadoc syntax error
	String source =
		"""
		public class X {
		        /**
		         * @see "string reference: 012345678"
		         */
		        void foo() {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
				/**
				 * @see "string reference: 012345678"
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug237942c() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	// difference with 3.3 formatter:
	// do not split string reference as this can lead to javadoc syntax error
	String source =
		"""
		public class X {
		        /**
		         * @see "string reference: 01234567 90"
		         */
		        void foo() {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
				/**
				 * @see "string reference: 01234567 90"
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug237942d() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	// difference with 3.3 formatter:
	// do not split string reference as this can lead to javadoc syntax error
	String source =
		"""
		public class X {
		        /**
		         * @see "string reference: 01234567890123"
		         */
		        void foo() {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
				/**
				 * @see "string reference: 01234567890123"
				 */
				void foo() {
				}
			}
			"""
	);
}

/**
 * bug 234336: [formatter] JavaDocTestCase.testMultiLineCommentIndent* tests fail in I20080527-2000 build
 * test Ensure that new comment formatter format all comments concerned by selections
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=234336"
 */
public void testBug234336() throws JavaModelException {
	String source =
		"""
		public class Test {
			[#/**
					 * test test
						 */#]
		}
		""";
	formatSource(source,
		"""
			public class Test {
				/**
				 * test test
				 */
			}
			""",
		CodeFormatter.K_JAVA_DOC,
		1, /* indentation level */
		true /* formatting twice */
	);
}

//static { TESTS_PREFIX = "testBug234583"; }
/**
 * bug 234583: [formatter] Code formatter should adapt edits instead of regions
 * test Ensure that selected region(s) are correctly formatted
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=234583"
 */
public void testBug234583a() throws JavaModelException {
	String source =
		"""
		public class X {
		[#                        int i= 1;               #]
		}
		""";
	formatSource(source,
		"""
			public class X {
				int i = 1;
			}
			"""
	);
}
public void testBug234583b() throws JavaModelException {
	String source =
		"""
		public class X {     \s
		
		
		
		[#                        int i= 1;               #]
		
		
		
		
		     }
		""";
	formatSource(source,
		"""
			public class X {     \s
			
			
			
				int i = 1;
			
			
			
			
			     }
			"""
	);
}
public void testBug234583c() throws JavaModelException {
	String source =
		"""
		public class X {     \s
		
		
		
		[#                        int i= 1;              \s
		#]
		
		
		
		     }
		""";
	formatSource(source,
		"""
			public class X {     \s
			
			
			
				int i = 1;
			
			
			
			     }
			"""
	);
}
public void testBug234583d() throws JavaModelException {
	String source =
		"""
		public class X {     \s
		
		
		[#
		                        int i= 1;              \s
		
		#]
		
		
		     }
		""";
	formatSource(source,
		"""
			public class X {     \s
			
			
				int i = 1;
			
			
			     }
			"""
	);
}
public void testBug234583e() throws JavaModelException {
	String source =
		"""
		public class X {     \s
		
		[#
		
		                        int i= 1;              \s
		
		
		#]
		
		     }
		""";
	formatSource(source,
		"""
			public class X {     \s
			
				int i = 1;
			
			     }
			"""
	);
}
public void testBug234583f() throws JavaModelException {
	String source =
		"""
		public class X {     \s
		[#
		
		
		                        int i= 1;              \s
		
		
		
		#]
		     }
		""";
	formatSource(source,
		"""
			public class X {     \s
			
				int i = 1;
			
			     }
			"""
	);
}
public void testBug234583g() throws JavaModelException {
	String source =
		"""
		public class X {      [#
		
		
		
		                        int i= 1;              \s
		
		
		
		
		#]     }
		""";
	formatSource(source,
		"""
			public class X {     \s
			
				int i = 1;
			
			     }
			"""
	);
}
public void testBug234583h() throws JavaModelException {
	String source =
		"""
		public class X {   [#  \s
		
		
		
		                        int i= 1;              \s
		
		
		
		
		   #]  }
		""";
	formatSource(source,
		"""
			public class X {  \s
			
				int i = 1;
			
			  }
			"""
	);
}
public void testBug234583i() throws JavaModelException {
	String source =
		"""
		public class X {[#     \s
		
		
		
		                        int i= 1;              \s
		
		
		
		
		     #]}
		""";
	formatSource(source,
		"""
			public class X {
			
				int i = 1;
			
			}
			"""
	);
}
// duplicate https://bugs.eclipse.org/bugs/show_bug.cgi?id=239447
public void testBug234583_Bug239447() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public class Bug239447 {
			private static final String CONTENT = "test.ObjectB {\\n"
		[#			     + "     multiEle = { name=\\"Foo\\" }\\n"#]
					+ "     multiEle = :x { name=\\"Bar\\" }\\n" + "   singleEle = x;\\n"
					+ "}";
		
		}
		""";
	formatSource(source,
		"""
			public class Bug239447 {
				private static final String CONTENT = "test.ObjectB {\\n"
						+ "     multiEle = { name=\\"Foo\\" }\\n"
						+ "     multiEle = :x { name=\\"Bar\\" }\\n" + "   singleEle = x;\\n"
						+ "}";
			
			}
			"""
	);
}
// duplicate https://bugs.eclipse.org/bugs/show_bug.cgi?id=237592
public void testBug234583_Bug237592() throws JavaModelException {
	String source =
		"""
		package test;
		
		public class Test {
		
			void foo() {
			}
		
		[#	  #]
		\t
		\t
		\t
		\t
		\t
		[#	 #]
		\t
		\t
		\t
		\t
		\t
			void bar() {
			}
		
		}
		""";
	formatSource(source,
		"""
			package test;
			
			public class Test {
			
				void foo() {
				}
			
			
			\t
			\t
			\t
			\t
			\t
			
			\t
			\t
			\t
			\t
			\t
				void bar() {
				}
			
			}
			"""
	);
}

/**
 * bug 236230: [formatter] SIOOBE while formatting a compilation unit.
 * test Ensure that no exception occurs while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=236230"
 */
public void testBug236230() throws JavaModelException {
	String source =
		"""
		/**
		 * Need a javadoc comment before to get the exception.
		 */
		public class Test {
		
		  /**
		   * <p>If there is an authority, it is:
		   * <pre>
		   *   //authority/device/pathSegment1/pathSegment2...</pre>
		   */
		  public String devicePath() {
			  return null;
		  }
		}
		""";
	formatSource(source,
		"""
			/**
			 * Need a javadoc comment before to get the exception.
			 */
			public class Test {
			
				/**
				 * <p>
				 * If there is an authority, it is:
				 *\s
				 * <pre>
				 * // authority/device/pathSegment1/pathSegment2...
				 * </pre>
				 */
				public String devicePath() {
					return null;
				}
			}
			"""
	);
}
public void testBug236230b() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		/**
		 * Need a javadoc comment before to get the exception.
		 */
		public class Test {
		
		  /**
		   * <p>If there is an authority, it is:
		   * <pre>//authority/device/pathSegment1/pathSegment2...</pre>
		   */
		  public String devicePath() {
			  return null;
		  }
		}
		""";
	formatSource(source,
		"""
			/**
			 * Need a javadoc comment before to get the exception.
			 */
			public class Test {
			
				/**
				 * <p>
				 * If there is an authority, it is:
				 *\s
				 * <pre>
				 * // authority/device/pathSegment1/pathSegment2...
				 * </pre>
				 */
				public String devicePath() {
					return null;
				}
			}
			"""
	);
}
public void testBug236230c() throws JavaModelException {
	this.formatterPrefs.comment_format_header = true;
	String source =
		"""
		/**
		 * Need a javadoc comment before to get the exception.
		 */
		public class Test {
		
		  /**
		   * <p>If there is an authority, it is:
		   * <pre>
					import java.util.List;
					//            CU         snippet
					public class X implements List {}
				</pre>
		   */
		  public String devicePath() {
			  return null;
		  }
		}
		""";
	formatSource(source,
		"""
			/**
			 * Need a javadoc comment before to get the exception.
			 */
			public class Test {
			
				/**
				 * <p>
				 * If there is an authority, it is:
				 *\s
				 * <pre>
				 * import java.util.List;
				 *\s
				 * // CU snippet
				 * public class X implements List {
				 * }
				 * </pre>
				 */
				public String devicePath() {
					return null;
				}
			}
			"""
	);
}
public void testBug236230d() throws JavaModelException {
	String source =
		"""
		/**
		 * Need a javadoc comment before to get the exception.
		 */
		public class Test {
		
		  /**
		   * <p>If there is an authority, it is:
		   * <pre>
					//class	body		snippet
					public class X {}
				</pre>
		   */
		  public String devicePath() {
			  return null;
		  }
		}
		""";
	formatSource(source,
		"""
			/**
			 * Need a javadoc comment before to get the exception.
			 */
			public class Test {
			
				/**
				 * <p>
				 * If there is an authority, it is:
				 *\s
				 * <pre>
				 * // class body snippet
				 * public class X {
				 * }
				 * </pre>
				 */
				public String devicePath() {
					return null;
				}
			}
			"""
	);
}
// Following tests showed possible regressions while implementing the fix...
public void testBug236230e() throws JavaModelException {
	String source =
		"""
		public class X02 {
		
		
			/**
			/**
			 * Removes the Java nature from the project.
			 */
			void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
			
				/**
				 * /** Removes the Java nature from the project.
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug236230f() throws JavaModelException {
	String source =
		"""
		public class X03 {
		  /** The value of <tt>System.getProperty("java.version")<tt>. **/
		  static final String JAVA_VERSION = System.getProperty("java.version");
		
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				/** The value of <tt>System.getProperty("java.version")<tt>. **/
				static final String JAVA_VERSION = System.getProperty("java.version");
			
			}
			"""
	);
}

/**
 * bug 236406: [formatter] Formatting qualified invocations can be broken when the Line Wrapping policy forces element to be on a new line
 * test Verify that wrapping policies forcing the first element to be on a new line are working again...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=236406"
 */
public void testBug236406_CDB1() {
	String source =
		"""
		/**        Javadoc		comment    	    */void foo1() {System.out.println();}
		//        Line		comment    	   \s
		void foo2() {System.out.println();}
		/*        Block		comment    	    */
		void foo3() {
		/*        statement Block		comment    	    */
		System.out.println();}
		""";
	formatSource(source,
		"""
			/**        Javadoc		comment    	    */
			void foo1() {
				System.out.println();
			}
			
			//        Line		comment    	   \s
			void foo2() {
				System.out.println();
			}
			
			/*        Block		comment    	    */
			void foo3() {
				/*        statement Block		comment    	    */
				System.out.println();
			}
			""",
		CodeFormatter.K_CLASS_BODY_DECLARATIONS
	);
}
public void testBug236406_CDB2() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		/**        Javadoc		comment    	    */void foo1() {System.out.println();}
		//        Line		comment    	   \s
		void foo2() {System.out.println();}
		/*        Block		comment    	    */
		void foo3() {
		/*        statement Block		comment    	    */
		System.out.println();}
		""";
	formatSource(source,
		"""
			/** Javadoc comment */
			void foo1() {
				System.out.println();
			}
			
			// Line comment
			void foo2() {
				System.out.println();
			}
			
			/* Block comment */
			void foo3() {
				/* statement Block comment */
				System.out.println();
			}
			""",
		CodeFormatter.K_CLASS_BODY_DECLARATIONS | CodeFormatter.F_INCLUDE_COMMENTS
	);
}
public void testBug236406_EX1() {
	String source =
		"""
		//        Line		comment    	   \s
		i =\s
		/**        Javadoc		comment    	    */
		1     +     (/*      Block		comment*/++a)
		""";
	formatSource(source,
		"""
			//        Line		comment    	   \s
			i =
					/**        Javadoc		comment    	    */
					1 + (/*      Block		comment*/++a)
			""",
		CodeFormatter.K_EXPRESSION
	);
}
public void testBug236406_EX2() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		//        Line		comment    	   \s
		i =\s
		/**        Javadoc		comment    	    */
		1     +     (/*      Block		comment*/++a)
		""";
	formatSource(source,
		"""
			// Line comment
			i =
					/** Javadoc comment */
					1 + (/* Block comment */++a)
			""",
		CodeFormatter.K_EXPRESSION | CodeFormatter.F_INCLUDE_COMMENTS
	);
}
public void testBug236406_ST1() {
	String source =
		"""
		/**        Javadoc		comment    	    */foo1();
		//        Line		comment    	   \s
		foo2();
		/*        Block		comment    	    */
		foo3(); {
		/*        indented Block		comment    	    */
		System.out.println();}
		""";
	formatSource(source,
		"""
			/**        Javadoc		comment    	    */
			foo1();
			//        Line		comment    	   \s
			foo2();
			/*        Block		comment    	    */
			foo3();
			{
				/*        indented Block		comment    	    */
				System.out.println();
			}
			""",
		CodeFormatter.K_STATEMENTS
	);
}
public void testBug236406_ST2() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		/**        Javadoc		comment    	    */foo1();
		//        Line		comment    	   \s
		foo2();
		/*        Block		comment    	    */
		foo3(); {
		/*        indented Block		comment    	    */
		System.out.println();}
		""";
	formatSource(source,
		"""
			/** Javadoc comment */
			foo1();
			// Line comment
			foo2();
			/* Block comment */
			foo3();
			{
				/* indented Block comment */
				System.out.println();
			}
			""",
		CodeFormatter.K_STATEMENTS | CodeFormatter.F_INCLUDE_COMMENTS
	);
}

/**
 * bug 237051: [formatter] Formatter insert blank lines after javadoc if javadoc contains Commons Attributes @@ annotations
 * test Ensure that Commons Attributes @@ annotations do not screw up the comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237051"
 */
public void testBug237051() throws JavaModelException {
	String source =
		"""
		public interface Test {
		/**
		 * foo
		 *\s
		 * @@Foo("foo")
		 */
		Object doSomething(Object object) throws Exception;
		}
		
		""";
	formatSource(source,
		"""
			public interface Test {
				/**
				 * foo
				 *\s
				 * @@Foo("foo")
				 */
				Object doSomething(Object object) throws Exception;
			}
			"""
	);
}
public void testBug237051b() throws JavaModelException {
	String source =
		"""
		public interface Test {
		/**
		 * foo
		 * @@Foo("foo")
		 */
		Object doSomething(Object object) throws Exception;
		}
		
		""";
	formatSource(source,
		"""
			public interface Test {
				/**
				 * foo
				 *\s
				 * @@Foo("foo")
				 */
				Object doSomething(Object object) throws Exception;
			}
			"""
	);
}
public void testBug237051c() throws JavaModelException {
	String source =
		"""
		public class X {
		
			/**
			 * Returns the download rate in bytes per second.  If the rate is unknown,
			 * @{link {@link #UNKNOWN_RATE}} is returned.
			 * @return the download rate in bytes per second
			 */
			public long getTransferRate() {
				return -1;
			}
		}
		""";
	formatSource(source,
		"""
			public class X {
			
				/**
				 * Returns the download rate in bytes per second. If the rate is unknown,
				 *\s
				 * @{link {@link #UNKNOWN_RATE}} is returned.
				 * @return the download rate in bytes per second
				 */
				public long getTransferRate() {
					return -1;
				}
			}
			"""
	);
}
public void testBug237051d() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		public class X {
		
		\t
			/**
			 * Copies specified input stream to the output stream. Neither stream
			 * is closed as part of this operation.
			 *\s
			 * @param is input stream
			 * @param os output stream
			 * @param monitor progress monitor
		     * @param expectedLength - if > 0, the number of bytes from InputStream will be verified
			 * @@return the offset in the input stream where copying stopped. Returns -1 if end of input stream is reached.
			 * @since 2.0
			 */
			public static long foo() {
				return -1;
			}
		}
		""";
	formatSource(source,
		"""
			public class X {
			
				/**
				 * Copies specified input stream to the output stream. Neither stream is
				 * closed as part of this operation.
				 *\s
				 * @param is
				 *            input stream
				 * @param os
				 *            output stream
				 * @param monitor
				 *            progress monitor
				 * @param expectedLength
				 *            - if > 0, the number of bytes from InputStream will be
				 *            verified
				 * @@return the offset in the input stream where copying stopped. Returns -1
				 *          if end of input stream is reached.
				 * @since 2.0
				 */
				public static long foo() {
					return -1;
				}
			}
			"""
	);
}

/**
 * bug 237453: [formatter] Save actions fails to remove excess new lines when set to "format edited lines"
 * test Ensure that empty lines/spaces selection is well formatted
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237453"
 */
public void testBug237453a() throws JavaModelException {
	String source =
		"""
		package test1;
		
		public class E1 {
		 	void foo() {
			}
		 	[#
			#]
		 	void bar() {
			}
		}""";
	formatSource(source,
		"""
			package test1;
			
			public class E1 {
			 	void foo() {
				}
			 \t
			 	void bar() {
				}
			}"""
	);
}
public void testBug237453b() throws JavaModelException {
	String source =
		"""
		package test1;
		
		public class E1 {
		 	void foo() {
			}
		 \t
		[#	#]
		 	void bar() {
			}
		}""";
	formatSource(source,
		"""
			package test1;
			
			public class E1 {
			 	void foo() {
				}
			 \t
			
			 	void bar() {
				}
			}"""
	);
}
public void testBug237453c() throws JavaModelException {
	String source =
		"""
		package test1;
		
		public class E1 {
		 	void foo() {
			}
		 \t
		[#\t
		#] 	void bar() {
			}
		}""";
	formatSource(source,
		"""
			package test1;
			
			public class E1 {
			 	void foo() {
				}
			 \t
			 	void bar() {
				}
			}"""
	);
}
public void testBug237453d() throws JavaModelException {
	String source =
		"""
		package test1;
		
		public class E1 {
		 	void foo() {
			}
		 \t
		[#\t
		 #]	void bar() {
			}
		}""";
	formatSource(source,
		"""
			package test1;
			
			public class E1 {
			 	void foo() {
				}
			 \t
				void bar() {
				}
			}"""
	);
}
public void testBug237453e() throws JavaModelException {
	String source =
		"""
		package test1;
		
		public class E1 {
		 	void foo() {
			}
		 \t
		[#\t
		 	#]void bar() {
			}
		}""";
	formatSource(source,
		"""
			package test1;
			
			public class E1 {
			 	void foo() {
				}
			 \t
				void bar() {
				}
			}"""
	);
}
public void testBug237453f() throws JavaModelException {
	String source =
		"""
		package test1;
		
		public class E1 {
		 	void foo() {
			}
		 \t
		\t
		[# #]	void bar() {
			}
		}""";
	formatSource(source,
		"""
			package test1;
			
			public class E1 {
			 	void foo() {
				}
			 \t
			\t
				void bar() {
				}
			}"""
	);
}
public void testBug237453g() throws JavaModelException {
	String source =
		"""
		package test1;
		
		public class E1 {
		 	void foo() {
			}
		 \t
		\t
		[# #] void bar() {
			}
		}""";
	formatSource(source,
		"""
			package test1;
			
			public class E1 {
			 	void foo() {
				}
			 \t
			\t
			 void bar() {
				}
			}"""
	);
}
public void testBug237453h() throws JavaModelException {
	String source =
		"""
		package test1;
		
		public class E1 {
		 	void foo() {
			}
		 \t
		\t
		[# 	#]void bar() {
			}
		}""";
	formatSource(source,
		"""
			package test1;
			
			public class E1 {
			 	void foo() {
				}
			 \t
			\t
				void bar() {
				}
			}"""
	);
}
public void testBug237453i() throws JavaModelException {
	String source =
		"""
		package test1;
		
		public class E1 {
		 	void foo() {
			}
		 \t
		\t
		[#  #]void bar() {
			}
		}""";
	formatSource(source,
		"""
			package test1;
			
			public class E1 {
			 	void foo() {
				}
			 \t
			\t
				void bar() {
				}
			}"""
	);
}

/**
 * bug 238090: [formatter] Formatter insert blank lines after javadoc if javadoc contains Commons Attributes @@ annotations
 * test Ensure that no unexpected empty new lines are added while formatting a reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=238090"
 */
public void testBug238090() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"""
		package test.bugs;
		public class LongNameClass {
		/**
		 * @see test.bugs.
		 * LongNameClass#longNameMethod(java.lang.String)
		 */
		public void foo() {
		}
		
		void longNameMethod(String str) {
		}
		}
		""";
	formatSource(source,
		"""
			package test.bugs;
			
			public class LongNameClass {
				/**
				 * @see test.bugs.
				 *      LongNameClass#longNameMethod(java.lang.String)
				 */
				public void foo() {
				}
			
				void longNameMethod(String str) {
				}
			}
			"""
	);
}

/**
 * bug 238210: [formatter] CodeFormatter wraps line comments without whitespaces
 * test Ensure that line without spaces are not wrapped by the comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=238210"
 */
public void testBug238210() throws JavaModelException {
	String source =
		"""
		/**
		 * LineCommentTestCase
		 *\s
		 * Formatting this compilation unit with line comment enabled and comment line width set to 100 or
		 * lower will result in both protected region comments to be wrapped although they do not contain
		 * any whitespace (excluding leading whitespace which should be / is being ignored altogether)
		 *\s
		 * @author Axel Faust, PRODYNA AG
		 */
		public class LineCommentTestCase {
		
		    public void someGeneratedMethod() {
		        //protected-region-start_[id=_14_0_1_3dd20592_1202209856234_914658_24183_someGeneratedMethod]
		        // some manually written code
		        // protected-region-end_[id=_14_0_1_3dd20592_1202209856234_914658_24183_someGeneratedMethod]
		    }
		}
		""";
	formatSource(source,
		"""
			/**
			 * LineCommentTestCase
			 *\s
			 * Formatting this compilation unit with line comment enabled and comment line
			 * width set to 100 or lower will result in both protected region comments to be
			 * wrapped although they do not contain any whitespace (excluding leading
			 * whitespace which should be / is being ignored altogether)
			 *\s
			 * @author Axel Faust, PRODYNA AG
			 */
			public class LineCommentTestCase {
			
				public void someGeneratedMethod() {
					// protected-region-start_[id=_14_0_1_3dd20592_1202209856234_914658_24183_someGeneratedMethod]
					// some manually written code
					// protected-region-end_[id=_14_0_1_3dd20592_1202209856234_914658_24183_someGeneratedMethod]
				}
			}
			"""
	);
}
// possible side effects detected while running massive tests
public void testBug238210_X01() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		package eclipse30;
		
		public class X01 {
		
			void foo() {
			\t
				binding = new LocalVariableBinding(this, tb, modifiers, false); // argument decl, but local var  (where isArgument = false)
			}
		
			public class LocalVariableBinding {
		
				public LocalVariableBinding(X01 x01, Object tb, Object modifiers,
						boolean b) {
				}
		
			}
		
			Object modifiers;
			Object tb;
			LocalVariableBinding binding;
		}
		""";
	formatSource(source,
		"""
			package eclipse30;
			
			public class X01 {
			
				void foo() {
			
					binding = new LocalVariableBinding(this, tb, modifiers, false); // argument
																					// decl,
																					// but
																					// local
																					// var
																					// (where
																					// isArgument
																					// =
																					// false)
				}
			
				public class LocalVariableBinding {
			
					public LocalVariableBinding(X01 x01, Object tb, Object modifiers,
							boolean b) {
					}
			
				}
			
				Object modifiers;
				Object tb;
				LocalVariableBinding binding;
			}
			""",
		false /*do not formatting twice*/
	);
}
public void testBug238210_X02() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"""
		package eclipse30;
		
		public class X02 {
			//private static short[] randomArray = {213, 231, 37, 85, 211, 29, 161, 175, 187, 3, 147, 246, 170, 30, 202, 183, 242, 47, 254, 189, 25, 248, 193, 2};
		}
		""";
	formatSource(source,
		"""
			package eclipse30;
			
			public class X02 {
				// private static short[] randomArray = {213, 231, 37, 85, 211, 29, 161,
				// 175, 187, 3, 147, 246, 170, 30, 202, 183, 242, 47, 254, 189, 25, 248,
				// 193, 2};
			}
			"""
	);
}
public void testBug238210_X03() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"""
		package eclipse30;
		
		public class X03 {
		
		\t
			/**
			 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#handleBreakpointEvent(com.sun.jdi.event.Event, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget, org.eclipse.jdt.internal.debug.core.model.JDIThread)
			 *\s
			 * (From referenced JavaDoc:
			 * 	Returns whethers the thread should be resumed
			 */
			void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			package eclipse30;
			
			public class X03 {
			
				/**
				 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#handleBreakpointEvent(com.sun.jdi.event.Event,
				 *      org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget,
				 *      org.eclipse.jdt.internal.debug.core.model.JDIThread)
				 *\s
				 *      (From referenced JavaDoc: Returns whethers the thread should be
				 *      resumed
				 */
				void foo() {
				}
			}
			"""
	);
}

/**
 * bug 238853: [formatter] Code Formatter does not properly format valid xhtml in javadoc.
 * test Ensure that xhtml valid tags are taken into account by the comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=238853"
 */
public void testBug238853() throws JavaModelException {
	String source =
		"""
		public class Test {
		
		/**
		 * This is a test comment.\s
		 * <p />\s
		 * Another comment. <br />\s
		 * Another comment.
		 */
		public void testMethod1()
		{
		}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * This is a test comment.
				 * <p />
				 * Another comment. <br />
				 * Another comment.
				 */
				public void testMethod1() {
				}
			}
			"""
	);
}

/**
 * bug 238920: [formatter] Code Formatter removes javadoc status if @category present
 * test Ensure that line without spaces are not wrapped by the comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=238920"
 */
public void testBug238920() throws JavaModelException {
	String source =
		"""
		public class X01 {
		/**
		 * @category test
		 */
		void foo() {
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				/**
				 * @category test
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug238920b() throws JavaModelException {
	String source =
		"""
		public class X02 {
		/**
		 * Test for bug 238920
		 * @category test
		 */
		void foo() {
		}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				/**
				 * Test for bug 238920
				 *\s
				 * @category test
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug238920c() throws JavaModelException {
	String source =
		"""
		public class X03 {
		/**
		 * @category test
		 * @return zero
		 */
		int foo() {
			return 0;
		}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				/**
				 * @category test
				 * @return zero
				 */
				int foo() {
					return 0;
				}
			}
			"""
	);
}

/**
 * bug 239130: [formatter] problem formatting block comments
 * test Ensure that the comment formatter preserve line breaks when specified
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=239130"
 */
public void testBug239130_default() throws JavaModelException {
	String source =
		"""
		public class X {
		
			/**
			 * @see java.lang.String
			 *\s
			 * Formatter should keep empty line above
			 */
			void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class X {
			
				/**
				 * @see java.lang.String
				 *\s
				 *      Formatter should keep empty line above
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug239130_clearBlankLines() throws JavaModelException {
	this.formatterPrefs.comment_clear_blank_lines_in_javadoc_comment = true;
	String source =
		"""
		public class X {
		
			/**
			 * @see java.lang.String
			 *\s
			 * Formatter should keep empty line above
			 */
			void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class X {
			
				/**
				 * @see java.lang.String Formatter should keep empty line above
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug239130_preserveLineBreaks() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"""
		public class X {
		
			/**
			 * @see java.lang.String
			 *\s
			 * Formatter should keep empty line above
			 */
			void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class X {
			
				/**
				 * @see java.lang.String
				 *\s
				 *      Formatter should keep empty line above
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug239130_clearBlankLines_preserveLineBreaks() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	this.formatterPrefs.comment_clear_blank_lines_in_javadoc_comment = true;
	String source =
		"""
		public class X {
		
			/**
			 * @see java.lang.String
			 *\s
			 * Formatter should keep empty line above
			 */
			void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class X {
			
				/**
				 * @see java.lang.String
				 *      Formatter should keep empty line above
				 */
				void foo() {
				}
			}
			"""
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=196124
public void testBug239130_196124_default() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		public class X {
		
		        /**
		         * The foo method.
		         * foo is a substitute for bar.
		         *\s
		         * @param param1 The first parameter
		         * @param param2
		         *            The second parameter.
		         *            If <b>null</b>the first parameter is used
		         */
		        public void foo(Object param1, Object param2) {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
			
				/**
				 * The foo method. foo is a substitute for bar.
				 *\s
				 * @param param1
				 *            The first parameter
				 * @param param2
				 *            The second parameter. If <b>null</b>the first parameter is
				 *            used
				 */
				public void foo(Object param1, Object param2) {
				}
			}
			"""
	);
}
public void testBug239130_196124() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	useOldJavadocTagsFormatting();
	String source =
		"""
		public class X {
		
		        /**
		         * The foo method.
		         * foo is a substitute for bar.
		         *\s
		         * @param param1 The first parameter
		         * @param param2
		         *            The second parameter.
		         *            If <b>null</b>the first parameter is used
		         */
		        public void foo(Object param1, Object param2) {
		        }
		}
		""";
	formatSource(source,
		"""
			public class X {
			
				/**
				 * The foo method.
				 * foo is a substitute for bar.
				 *\s
				 * @param param1
				 *            The first parameter
				 * @param param2
				 *            The second parameter.
				 *            If <b>null</b>the first parameter is used
				 */
				public void foo(Object param1, Object param2) {
				}
			}
			"""
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=96696
public void testBug239130_96696_block_default() throws JavaModelException {
	String source =
		"""
		public class Test {
		
			/*
			 * Conceptually, all viewers perform two primary tasks:
			 *\s
			 * - They help adapt your domain objects into viewable entities
			 *\s
			 * - They provide notifications when the viewable entities are selected or
			 * changed through the UI
			 */
			public void foo() {
			}
		}
		""";
	formatSource(source, source);
}
public void testBug239130_96696_block_clearBlankLines() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.comment_clear_blank_lines_in_block_comment = true;
	String source =
		"""
		public class Test {
		
			/*
			 * Conceptually, all viewers perform two primary tasks:
			 *\s
			 * - They help adapt your domain objects into viewable entities
			 *\s
			 * - They provide notifications when the viewable entities are selected or
			 * changed through the UI
			 */
			public void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/*
				 * Conceptually, all viewers perform two primary tasks: - They help adapt
				 * your domain objects into viewable entities - They provide notifications
				 * when the viewable entities are selected or changed through the UI
				 */
				public void foo() {
				}
			}
			"""
	);
}
public void testBug239130_96696_block_clearBlankLines_preserveLineBreaks() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	this.formatterPrefs.comment_clear_blank_lines_in_block_comment = true;
	String source =
		"""
		public class Test {
		
			/*
			 * Conceptually, all viewers perform two primary tasks:
			 *\s
			 * - They help adapt your domain objects into viewable entities
			 *\s
			 * - They provide notifications when the viewable entities are selected or
			 * changed through the UI
			 */
			public void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/*
				 * Conceptually, all viewers perform two primary tasks:
				 * - They help adapt your domain objects into viewable entities
				 * - They provide notifications when the viewable entities are selected or
				 * changed through the UI
				 */
				public void foo() {
				}
			}
			"""
	);
}
public void testBug239130_96696_javadoc_default() throws JavaModelException {
	String source =
		"""
		public class Test {
		
			/**
			 * Conceptually, all viewers perform two primary tasks:
			 *\s
			 * - They help adapt your domain objects into viewable entities
			 *\s
			 * - They provide notifications when the viewable entities are selected or
			 * changed through the UI
			 */
			public void foo() {
			}
		}
		""";
	formatSource(source, source);
}
public void testBug239130_96696_javadoc_clearBlankLines() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.comment_clear_blank_lines_in_javadoc_comment = true;
	String source =
		"""
		public class Test {
		
			/**
			 * Conceptually, all viewers perform two primary tasks:
			 *\s
			 * - They help adapt your domain objects into viewable entities
			 *\s
			 * - They provide notifications when the viewable entities are selected or
			 * changed through the UI
			 */
			public void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * Conceptually, all viewers perform two primary tasks: - They help adapt
				 * your domain objects into viewable entities - They provide notifications
				 * when the viewable entities are selected or changed through the UI
				 */
				public void foo() {
				}
			}
			"""
	);
}
public void testBug239130_96696_javadoc_clearBlankLines_preserveLineBreaks() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	this.formatterPrefs.comment_clear_blank_lines_in_javadoc_comment = true;
	String source =
		"""
		public class Test {
		
			/**
			 * Conceptually, all viewers perform two primary tasks:
			 *\s
			 * - They help adapt your domain objects into viewable entities
			 *\s
			 * - They provide notifications when the viewable entities are selected or
			 * changed through the UI
			 */
			public void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * Conceptually, all viewers perform two primary tasks:
				 * - They help adapt your domain objects into viewable entities
				 * - They provide notifications when the viewable entities are selected or
				 * changed through the UI
				 */
				public void foo() {
				}
			}
			"""
	);
}

/**
 * bug 239719: [formatter] Code formatter destroys pre formatted javadoc comments
 * test Ensure that annotations inside <pre>...</pre> tags are not considered as javadoc tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=239719"
 */
public void testBug239719() throws JavaModelException {
	String source =
		"""
		/**
		 * <pre>
		 *  public class Test implements Runnable
		 *  {
		 *    @Override
		 *    public void run()
		 *    {\s
		 *      // Hello really bad Ganymede formatter !!!
		 *      // Shit happens when somebody tries to change a running system
		 *      System.out.println("Press Shift+Ctrl+F to format");
		 *    }
		 *  }</pre>
		 */
		 public class Test\s
		 {
		 }
		""";
	formatSource(source,
		"""
			/**
			 * <pre>
			 * public class Test implements Runnable {
			 * 	@Override
			 * 	public void run() {
			 * 		// Hello really bad Ganymede formatter !!!
			 * 		// Shit happens when somebody tries to change a running system
			 * 		System.out.println("Press Shift+Ctrl+F to format");
			 * 	}
			 * }
			 * </pre>
			 */
			public class Test {
			}
			"""
	);
}
public void testBug239719b() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"""
		public class X01 {
		\t
			private int fLength;
			private int fOffset;
		
			/**
			 * Returns the inclusive end position of this edit. The inclusive end
			 * position denotes the last character of the region manipulated by
			 * this edit. The returned value is the result of the following
			 * calculation:
			 * <pre>
			 *   getOffset() + getLength() - 1;
			 * <pre>
			 *\s
			 * @return the inclusive end position
			 */
			public final int getInclusiveEnd() {
				return fOffset + fLength - 1;
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
				private int fLength;
				private int fOffset;
			
				/**
				 * Returns the inclusive end position of this edit. The inclusive end
				 * position denotes the last character of the region manipulated by this
				 * edit. The returned value is the result of the following calculation:
				 *\s
				 * <pre>
				 * getOffset() + getLength() - 1;
				 *\s
				 * <pre>
				 *\s
				 * @return the inclusive end position
				 */
				public final int getInclusiveEnd() {
					return fOffset + fLength - 1;
				}
			}
			"""
	);
}

/**
 * bug 239941: [formatter] Unclosed html tags make the formatter to produce incorrect outputs
 * test Ensure that unclosed html tags do not screw up the formatter in following javadoc comments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=239941"
 */
public void testBug239941() throws JavaModelException {
	String source =
		"""
		public class X01 {
		
			/**
			 * <pre>
			 * Unclosed pre tag
			 */
			int foo;
		
		    /**
		     * Gets the signers of this class.
		     *
		     * @return  the signers of this class, or null if there are no signers.  In
		     * 		particular, this method returns null if this object represents
		     * 		a primitive type or void.
		     * @since 	JDK1.1
		     */
		    public native Object[] getSigners();
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
				/**
				 * <pre>
				 * Unclosed pre tag
				 */
				int foo;
			
				/**
				 * Gets the signers of this class.
				 *
				 * @return the signers of this class, or null if there are no signers. In
				 *         particular, this method returns null if this object represents a
				 *         primitive type or void.
				 * @since JDK1.1
				 */
				public native Object[] getSigners();
			}
			"""
	);
}

/**
 * bug 240686: [formatter] Formatter do unexpected things
 * test Ensure that open brace are well taken into account just after the HTML tag opening
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=240686"
 */
public void testBug240686() throws JavaModelException {
	String source =
		"""
		public class Test {
		
		/**\s
		 * <pre>{ }</pre>
		 *\s
		 * <table>
		 * <tr>{ "1",
		 * "2"}
		 * </tr>
		 * </table>
		 */
		void foo() {}
		}
		""";
	// output is different than 3.3 one: <tr> is considered as new line tag
	// hence the text inside the tag is put on a new line
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * <pre>
				 * {}
				 * </pre>
				 *\s
				 * <table>
				 * <tr>
				 * { "1", "2"}
				 * </tr>
				 * </table>
				 */
				void foo() {
				}
			}
			"""
	);
}

/**
 * bug 241345: [formatter] Didn't Format HTML tags is unavailable
 * test Ensure that unset 'Format HTML tags' preference format HTML tags like simple text
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=241345"
 */
public void testBug241345() throws JavaModelException {
	this.formatterPrefs.comment_format_html = false;
	String source =
		"""
		/**
		 * <p>Should not format HTML paragraph</p>
		 */
		public interface Test {
			/**
			 *\s
			 * These possibilities include: <ul><li>Formatting of header
			 * comments.</li><li>Formatting of Javadoc tags</li></ul>
			 */
			int bar();
		
		}
		""";
	formatSource(source,
		"""
			/**
			 * <p>Should not format HTML paragraph</p>
			 */
			public interface Test {
				/**
				 *\s
				 * These possibilities include: <ul><li>Formatting of header
				 * comments.</li><li>Formatting of Javadoc tags</li></ul>
				 */
				int bar();
			
			}
			"""
	);
}

/**
 * bug 241687: [formatter] problem formatting block comments
 * test Ensure that the comment formatter always honors the tacit contract of not modifying block comments starting with '/*-'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=241687"
 */
public void testBug241687() throws JavaModelException {
	String source =
		"""
		public interface Test {
		
		/*---------------------
		 * END OF SETS AND GETS
		 * test test test test test test test
		test test test test test test\s
		 *\s
		*
		 *---------------------*/
		void foo();
		}
		""";
	formatSource(source,
		"""
			public interface Test {
			
				/*---------------------
				 * END OF SETS AND GETS
				 * test test test test test test test
				test test test test test test\s
				 *\s
				*
				 *---------------------*/
				void foo();
			}
			"""
	);
}

/**
 * bug 251133: [formatter] Automatic formatting single line comments is incoherent among tools
 * test Test the new formatter capability to completely ignore line comments starting at first column
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=251133"
 */
public void testBug251133() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		public class X01 {
		//		int		a    =  	  1;
		//    int     b	=	  	2;
		}""";
	formatSource(source,
		"""
			public class X01 {
				// int a = 1;
				// int b = 2;
			}"""
	);
}
public void testBug251133a() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = true;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = false;
	String source =
		"""
		public class X {
		//		first	  	column  	  comment	\t
		}""";
	formatSource(source,
		"""
			public class X {
				// first column comment
			}"""
	);
}
public void testBug251133b() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = true;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"""
		public class X {
		//		first	  	column  	  comment	\t
		}""";
	formatSource(source,
		"""
			public class X {
			// first column comment
			}"""
	);
}
public void testBug251133c() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = true;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = false;
	this.formatterPrefs.never_indent_line_comments_on_first_column = false;
	String source =
		"""
		public class X {
		//		first	  	column  	  comment	\t
		}""";
	formatSource(source);
}
public void testBug251133d() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = true;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = false;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"""
		public class X {
		//		first	  	column  	  comment	\t
		}""";
	formatSource(source);
}
public void testBug251133e() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = false;
	String source =
		"""
		public class X {
		//		first	  	column  	  comment	\t
		}""";
	formatSource(source,
		"""
			public class X {
				//		first	  	column  	  comment	\t
			}"""
	);
}
public void testBug251133f() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"""
		public class X {
		//		first	  	column  	  comment	\t
		}""";
	formatSource(source,
		"""
			public class X {
			//		first	  	column  	  comment	\t
			}"""
	);
}
public void testBug251133g() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = false;
	this.formatterPrefs.never_indent_line_comments_on_first_column = false;
	String source =
		"""
		public class X {
		//		first	  	column  	  comment	\t
		}""";
	formatSource(source,
		"""
			public class X {
				//		first	  	column  	  comment	\t
			}"""
	);
}
public void testBug251133h() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = false;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"""
		public class X {
		//		first	  	column  	  comment	\t
		}""";
	formatSource(source);
}

/**
 * bug 256799: [formatter] Formatter wrongly adds space to //$FALL-THROUGH$
 * test Ensure that the comment formatter preserve $FALL-THROUGH$ tag leading spaces
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=256799"
 */
public void testBug256799_Line01() throws JavaModelException {
	String source =
		"""
		public class X01 {
			int foo(int value) {
				int test = 0;
				switch (value) {
				case 1:
					test = value;
					//$FALL-THROUGH$
				case 2:
					test = value;
					// $FALL-THROUGH$
				case 3:
					test = value;
					//    	   $FALL-THROUGH$
				case 4:
					test = value;
					//		$FALL-THROUGH$                 \s
				default:
					test = -1;
					break;
				}
				return test;
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				int foo(int value) {
					int test = 0;
					switch (value) {
					case 1:
						test = value;
						//$FALL-THROUGH$
					case 2:
						test = value;
						// $FALL-THROUGH$
					case 3:
						test = value;
						// $FALL-THROUGH$
					case 4:
						test = value;
						// $FALL-THROUGH$
					default:
						test = -1;
						break;
					}
					return test;
				}
			}
			"""
	);
}
public void testBug256799_Line02() throws JavaModelException {
	String source =
		"""
		public class X01 {
			int foo(int value) {
				int test = 0;
				switch (value) {
				case 1:
					test = value;
					//$FALL-THROUGH$     with	text   	   after       \s
				case 2:
					test = value;
					// $FALL-THROUGH$		with	text   	   after        	\t
				case 3:
					test = value;
					//    	   $FALL-THROUGH$  		   with	text   	   after	       \s
				case 4:
					test = value;
					//		$FALL-THROUGH$		             		with	text   	   after		\t
				default:
					test = -1;
					break;
				}
				return test;
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				int foo(int value) {
					int test = 0;
					switch (value) {
					case 1:
						test = value;
						//$FALL-THROUGH$ with text after
					case 2:
						test = value;
						// $FALL-THROUGH$ with text after
					case 3:
						test = value;
						// $FALL-THROUGH$ with text after
					case 4:
						test = value;
						// $FALL-THROUGH$ with text after
					default:
						test = -1;
						break;
					}
					return test;
				}
			}
			"""
	);
}
public void testBug256799_Block01() throws JavaModelException {
	String source =
		"""
		public class X01 {
			int foo(int value) {
				int test = 0;
				switch (value) {
				case 1:
					test = value;
					/*$FALL-THROUGH$*/
				case 2:
					test = value;
					/* $FALL-THROUGH$*/
				case 3:
					test = value;
					/*$FALL-THROUGH$ */
				case 4:
					test = value;
					/* $FALL-THROUGH$ */
				case 5:
					test = value;
					/*    	   $FALL-THROUGH$*/
				case 6:
					test = value;
					/*		$FALL-THROUGH$                  */
				case 7:
					test = value;
					/*$FALL-THROUGH$			*/
				case 8:
					test = value;
					/*		     		     $FALL-THROUGH$	    	    	*/
				default:
					test = -1;
					break;
				}
				return test;
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				int foo(int value) {
					int test = 0;
					switch (value) {
					case 1:
						test = value;
						/* $FALL-THROUGH$ */
					case 2:
						test = value;
						/* $FALL-THROUGH$ */
					case 3:
						test = value;
						/* $FALL-THROUGH$ */
					case 4:
						test = value;
						/* $FALL-THROUGH$ */
					case 5:
						test = value;
						/* $FALL-THROUGH$ */
					case 6:
						test = value;
						/* $FALL-THROUGH$ */
					case 7:
						test = value;
						/* $FALL-THROUGH$ */
					case 8:
						test = value;
						/* $FALL-THROUGH$ */
					default:
						test = -1;
						break;
					}
					return test;
				}
			}
			"""
	);
}
public void testBug256799_Block02() throws JavaModelException {
	String source =
		"""
		public class X01 {
			int foo(int value) {
				int test = 0;
				switch (value) {
				case 1:
					test = value;
					/*$FALL-THROUGH$with    text    after*/
				case 2:
					test = value;
					/* $FALL-THROUGH$with  		  text	after*/
				case 3:
					test = value;
					/*$FALL-THROUGH$    with	   	text   	after	    */
				case 4:
					test = value;
					/* $FALL-THROUGH$     with	   	text   	after	    */
				case 5:
					test = value;
					/*    	   $FALL-THROUGH$	with  		  text	after*/
				case 6:
					test = value;
					/*		$FALL-THROUGH$         	with  		  text	after        */
				case 7:
					test = value;
					/*$FALL-THROUGH$			with  		  text	after	*/
				case 8:
					test = value;
					/*		     		     $FALL-THROUGH$	    		with  		  text	after    	*/
				default:
					test = -1;
					break;
				}
				return test;
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				int foo(int value) {
					int test = 0;
					switch (value) {
					case 1:
						test = value;
						/* $FALL-THROUGH$with text after */
					case 2:
						test = value;
						/* $FALL-THROUGH$with text after */
					case 3:
						test = value;
						/* $FALL-THROUGH$ with text after */
					case 4:
						test = value;
						/* $FALL-THROUGH$ with text after */
					case 5:
						test = value;
						/* $FALL-THROUGH$ with text after */
					case 6:
						test = value;
						/* $FALL-THROUGH$ with text after */
					case 7:
						test = value;
						/* $FALL-THROUGH$ with text after */
					case 8:
						test = value;
						/* $FALL-THROUGH$ with text after */
					default:
						test = -1;
						break;
					}
					return test;
				}
			}
			"""
	);
}

/**
 * bug 254998: [formatter] wrong type comment format during code generation
 * test Ensure that the comment formatter works well on the given test case
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=254998"
 */
public void testBug254998() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_header = true;
	String source =
		"""
		/**
		 * Test for
		 * bug 254998
		 */
		package javadoc;
		
		/**
		 * Test for
		 * bug 254998
		 */
		public class Test {
		
		}
		""";
	formatSource(source,
		"""
			/**
			 * Test for bug 254998
			 */
			package javadoc;
			
			/**
			 * Test for
			 * bug 254998
			 */
			public class Test {
			
			}
			"""
	);
}
public void testBug254998b() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_header = true;
	String source =
		"""
		/*
		 * Test for
		 * bug 254998
		 */
		package block;
		
		/*
		 * Test for
		 * bug 254998
		 */
		public class Test {
		/*
		 * Test for
		 * bug 254998
		 */
		}
		""";
	formatSource(source,
		"""
			/*
			 * Test for bug 254998
			 */
			package block;
			
			/*
			 * Test for bug 254998
			 */
			public class Test {
				/*
				 * Test for
				 * bug 254998
				 */
			}
			"""
	);
}
public void testBug254998c() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_header = true;
	String source =
		"""
		//		Test		for		bug		254998
		package line;
		
		//		Test		for		bug		254998
		public class Test {
		//		Test		for		bug		254998
		}
		""";
	formatSource(source,
		"""
			// Test for bug 254998
			package line;
			
			// Test for bug 254998
			public class Test {
				//		Test		for		bug		254998
			}
			"""
	);
}

/**
 * bug 260011: [formatter] Formatting of html in javadoc comments doesn't work with style attributes
 * test Ensure that the comment formatter understand <p> html tag with attributes
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260011"
 */
public void testBug260011() throws JavaModelException {
	String source =
		"""
		public class Test {
		    /**
		     * some comment text here
		     * <p style="font-variant:small-caps;">
		     * some text to be styled a certain way
		     * </p>
		     */
		    void foo() {}
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
				/**
				 * some comment text here
				 * <p style="font-variant:small-caps;">
				 * some text to be styled a certain way
				 * </p>
				 */
				void foo() {
				}
			
			}
			"""
	);
}
public void testBug260011_01() throws JavaModelException {
	String source =
		"""
		public class Test {
		    /**
		     * some comment text here
		     * <ul style="font-variant:small-caps;"><li style="font-variant:small-caps;">
		     * some text to be styled a certain way</li></ul>
		     * end of comment
		     */
		    void foo() {}
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
				/**
				 * some comment text here
				 * <ul style="font-variant:small-caps;">
				 * <li style="font-variant:small-caps;">some text to be styled a certain
				 * way</li>
				 * </ul>
				 * end of comment
				 */
				void foo() {
				}
			
			}
			"""
	);
}
public void testBug260011_02() throws JavaModelException {
	String source =
		"""
		public class Test {
		    /**
		     * some comment text here
		     * <pre style="font-variant:small-caps;">
		     *      some text
		     *           to be styled
		     *                 a certain way
		     *     \s
		     * </pre>
		     * end of comment
		     */
		    void foo() {}
		
		}
		""";
	formatSource(source,
		"""
			public class Test {
				/**
				 * some comment text here
				 *\s
				 * <pre style="font-variant:small-caps;">
				 *      some text
				 *           to be styled
				 *                 a certain way
				 *\s
				 * </pre>
				 *\s
				 * end of comment
				 */
				void foo() {
				}
			
			}
			"""
	);
}
public void testBug260011_03() throws JavaModelException {
	String source =
		"""
		public class Test {
		
			/**
			 * Indent char is a space char but not a line delimiters.
			 * <code>== Character.isWhitespace(ch) && ch != '\\n' && ch != '\\r'</code>
			 */
			public void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * Indent char is a space char but not a line delimiters.
				 * <code>== Character.isWhitespace(ch) && ch != '\\n' && ch != '\\r'</code>
				 */
				public void foo() {
				}
			}
			"""
	);
}
public void testBug260011_04() throws JavaModelException {
	String source =
		"""
		public class Test {
		
			/**
			 * The list of variable declaration fragments (element type:\s
			 * <code VariableDeclarationFragment</code>).  Defaults to an empty list.
			 */
			int field;
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * The list of variable declaration fragments (element type: <code
				 * VariableDeclarationFragment</code>). Defaults to an empty list.
				 */
				int field;
			}
			"""
	);
}
public void testBug260011_05() throws JavaModelException {
	String source =
		"""
		public class Test {
		
			/**
			 * Compares version strings.
			 *\s
			 * @return result of comparison, as integer;
			 * <code><0 if left is less than right </code>
			 * <code>0 if left is equals to right</code>
			 * <code>>0 if left is greater than right</code>
			 */
			int foo() {
				return 0;
			}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * Compares version strings.
				 *\s
				 * @return result of comparison, as integer;
				 *         <code><0 if left is less than right </code>
				 *         <code>0 if left is equals to right</code>
				 *         <code>>0 if left is greater than right</code>
				 */
				int foo() {
					return 0;
				}
			}
			"""
	);
}
public void testBug260011_06() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		public interface Test {
		
			/**
			 * Returns the length of this array.
			 *\s
			 * @return the length of this array
			 * @exception DebugException if this method fails. Reasons include:<ul>
			 * <li>Failure communicating with the VM.  The DebugException's
			 * status code contains the underlying exception responsible for
			 * the failure.</li>
			 * </ul
			 */
			public int getLength();
		}
		""";
	formatSource(source,
		"""
			public interface Test {
			
				/**
				 * Returns the length of this array.
				 *\s
				 * @return the length of this array
				 * @exception DebugException
				 *                if this method fails. Reasons include:
				 *                <ul>
				 *                <li>Failure communicating with the VM. The
				 *                DebugException's status code contains the underlying
				 *                exception responsible for the failure.</li> </ul
				 */
				public int getLength();
			}
			"""
	);
}
public void testBug260011_07() throws JavaModelException {
	String source =
		"""
		public interface Test {
		
		\t
			/**
			 * Returns the change directly associated with this change element or <code
			 * null</code> if the element isn't associated with a change.
			 *\s
			 * @return the change or <code>null</code>
			 */
			public String getChange();
		}
		""";
	formatSource(source,
		"""
			public interface Test {
			
				/**
				 * Returns the change directly associated with this change element or <code
				 * null</code> if the element isn't associated with a change.
				 *\s
				 * @return the change or <code>null</code>
				 */
				public String getChange();
			}
			"""
	);
}
public void testBug260011_08() throws JavaModelException {
	String source =
		"""
		public interface Test {
		
			/**
			 * Answer the element factory for an id, or <code>null</code. if not found.
			 * @param targetID
			 * @return
			 */
			public int foo(String targetID);
		}
		""";
	formatSource(source,
		"""
			public interface Test {
			
				/**
				 * Answer the element factory for an id, or <code>null</code. if not found.
				 *\s
				 * @param targetID
				 * @return
				 */
				public int foo(String targetID);
			}
			"""
	);
}
public void testBug260011_09() throws JavaModelException {
	String source =
		"""
		public class Test {
		
			/**
		     * o   Example: baseCE < a << b <<< q << c < d < e * nextCE(X,1)\s
			 */
			int field;
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * o Example: baseCE < a << b <<< q << c < d < e * nextCE(X,1)
				 */
				int field;
			}
			"""
	);
}
public void testBug260011_09b() throws JavaModelException {
	String source =
		"""
		public class Test {
		
			/**
		     * o   Example: baseCE < a < b < q < c < p < e * nextCE(X,1)\s
			 */
			int field;
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * o Example: baseCE < a < b < q < c < p < e * nextCE(X,1)
				 */
				int field;
			}
			"""
	);
}
public void testBug260011_10() throws JavaModelException {
	String source =
		"""
		public interface Test {
		
			/**
			 * Creates and opens a dialog to edit the given template.
			 * <p
			 * Subclasses may override this method to provide a custom dialog.</p>
			 */
			void foo();
		}
		""";
	formatSource(source,
		"""
			public interface Test {
			
				/**
				 * Creates and opens a dialog to edit the given template. <p Subclasses may
				 * override this method to provide a custom dialog.
				 * </p>
				 */
				void foo();
			}
			"""
	);
}
public void testBug260011_11() throws JavaModelException {
	String source =
		"""
		public class Test {
		
		    /**\s
		     * <p>Binary property IDS_Trinary_Operator (new).</p>\s
		     * <p?For programmatic determination of Ideographic Description\s
		     * Sequences.</p>\s
		     * @stable ICU 2.6
		     */\s
		    public static final int IDS_TRINARY_OPERATOR = 19;\s
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * <p>
				 * Binary property IDS_Trinary_Operator (new).
				 * </p>
				 * <p?For programmatic determination of Ideographic Description Sequences.
				 * </p>
				 *\s
				 * @stable ICU 2.6
				 */
				public static final int IDS_TRINARY_OPERATOR = 19;
			}
			"""
	);
}

/**
 * bug 260274: [formatter] * character is removed while formatting block comments
 * test Ensure that the comment formatter keep '*' characters while formatting block comments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260274"
 */
public void testBug260274() throws JavaModelException {
	String source =
		"""
		class X {
		/*
		 * The formatter should NOT remove * character
		 * in block comments!
		 */
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * The formatter should NOT remove * character in block comments!
				 */
			}
			"""
	);
}
public void testBug260274b() throws JavaModelException {
	String source =
		"""
		class X {
		/*
		 * The formatter should keep '*' characters
		 * in block comments!
		 */
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * The formatter should keep '*' characters in block comments!
				 */
			}
			"""
	);
}
public void testBug260274c() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"""
		class X {
		/* *********************************************
		 * Test\s
		 */
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * *********************************************
				 * Test
				 */
			}
			"""
	);
}
public void testBug260274d() throws JavaModelException {
	String source =
		"""
		class X {
		/* *********************************************
		 * Test\s
		 */
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * ********************************************* Test
				 */
			}
			"""
	);
}
public void testBug260274e() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"""
		class X {
		/*
		 * **************************************************
		 * **********  Test  **********  Test  **************
		 * **************************************************
		 */
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * ************************************************** ********** Test
				 * ********** Test **************
				 * **************************************************
				 */
			}
			"""
	);
}
public void testBug260274f() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"""
		class X {
		/* *****************************************************************************
		 * Action that allows changing the model providers sort order.
		 */
		void foo() {
		}
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * *************************************************************************
				 * **** Action that allows changing the model providers sort order.
				 */
				void foo() {
				}
			}
			"""
	);
}
public void testBug260274g() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"""
		class X {
		/*
		 * **********************************************************************************
		 * **********************************************************************************
		 * **********************************************************************************
		 * The code below was added to track the view with focus
		 * in order to support save actions from a view. Remove this
		 * experimental code if the decision is to not allow views to\s
		 * participate in save actions (see bug 10234)\s
		 */
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * *************************************************************************
				 * *********
				 * *************************************************************************
				 * *********
				 * *************************************************************************
				 * ********* The code below was added to track the view with focus in order
				 * to support save actions from a view. Remove this experimental code if the
				 * decision is to not allow views to participate in save actions (see bug
				 * 10234)
				 */
			}
			"""
	);
}
public void testBug260274h() throws JavaModelException {
	String source =
		"""
		class X {
		    /**
			 * @see #spacing(Point)
			 * * @see #spacing(int, int)
			 */
		    public void foo() {
		    }
		}
		""";
	formatSource(source,
		"""
			class X {
				/**
				 * @see #spacing(Point) * @see #spacing(int, int)
				 */
				public void foo() {
				}
			}
			"""
	);
}
public void testBug260274i() throws JavaModelException {
	String source =
		"""
		class X {
		/***********************************************
		 * Test\s
		 */
		}
		""";
	formatSource(source,
		"""
			class X {
				/***********************************************
				 * Test
				 */
			}
			"""
	);
}

/**
 * bug 260276: [formatter] Inconsistent formatting of one-line block comment
 * test Ensure that the comment formatter has a consistent behavior while formatting one-line block comment
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260276"
 */
public void testBug260276() throws JavaModelException {
	String source =
		"""
		class X {
		/* a
		comment */
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * a comment
				 */
			}
			"""
	);
}
public void testBug260276b() throws JavaModelException {
	String source =
		"""
		class X {
		/* a
		 comment */
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * a comment
				 */
			}
			"""
	);
}
public void testBug260276c() throws JavaModelException {
	String source =
		"""
		class X {
		/* a
		 * comment */
		}
		""";
	formatSource(source,
		"""
			class X {
				/*
				 * a comment
				 */
			}
			"""
	);
}

/**
 * bug 260381: [formatter] Javadoc formatter breaks {@code ...} tags.
 * test Ensure that the @code tag is similar to {@code <code>} HTML tag
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260381"
 */
public void testBug260381() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * @author Myself
		 * @version {@code            The  			  text		here     should     not			be   		    			     formatted....   	   }
		 */
		public class X01 {
		}
		""";
	formatSource(source);
}
public void testBug260381a() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * @author Myself
		 * @version {@code          \
		 *            The  			  text		here     should     not			be   		    			     formatted....   	   }
		 */
		public class X01a {
		}
		""";
	formatSource(source);
}
public void testBug260381b() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * @author Myself
		 *  @version {@code
		 * The  			  text		here     should     not			be   		    			     formatted....   	   }
		 */
		public class X01b {
		}
		""";
	formatSource(source,
		"""
			/**
			 * Comments that can be formated in several lines...
			 *\s
			 * @author Myself
			 * @version {@code
			 * The  			  text		here     should     not			be   		    			     formatted....   	   }
			 */
			public class X01b {
			}
			"""
	);
}
public void testBug260381c() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * @author Myself
		 *  @version {@code    \s
		 *         \s
		           \s
		 *          The  			  text		here     should     not			be   		    			     formatted....   	   }
		 */
		public class X01c {
		}
		""";
	formatSource(source,
		"""
			/**
			 * Comments that can be formated in several lines...
			 *\s
			 * @author Myself
			 * @version {@code    \s
			 *         \s
			           \s
			 *          The  			  text		here     should     not			be   		    			     formatted....   	   }
			 */
			public class X01c {
			}
			"""
	);
}
public void testBug260381d() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * @author Myself
		 * @version <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>
		 */
		public class X02 {
		}
		""";
	formatSource(source);
}
public void testBug260381e() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * @author Myself
		 * @version
		 *          <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>
		 */
		public class X02b {
		}
		""";
	formatSource(source,
		"""
			/**
			 * Comments that can be formated in several lines...
			 *\s
			 * @author Myself
			 * @version <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>
			 */
			public class X02b {
			}
			"""
	);
}
public void testBug260381f() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * @author Myself
		 *  @see Object <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>
		 */
		public class X02c {
		}
		""";
	formatSource(source,
		"""
			/**
			 * Comments that can be formated in several lines...
			 *\s
			 * @author Myself
			 * @see Object
			 *      <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>
			 */
			public class X02c {
			}
			"""
	);
}
public void testBug260381g() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * {@code            The  			  text		here     should     not			be   		    			formatted....   	   }
		 */
		public class X03 {
		}
		""";
	formatSource(source);
}
public void testBug260381h() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * <code>            The  			  text		here     should    \s
		 * not			be   		    			formatted....   	   </code>
		 */
		public class X03b {
		}
		""";
	formatSource(source);
}
public void testBug260381i() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * {@code            The  			  text		here     should
		 * not			be   		    			formatted....   	   }
		 */
		public class X03c {
		}
		""";
	formatSource(source,
		"""
			/**
			 * Comments that can be formated in several lines...
			 *\s
			 * {@code            The  			  text		here     should
			 * not			be   		    			formatted....   	   }
			 */
			public class X03c {
			}
			"""
	);
}
public void testBug260381j() throws JavaModelException {
	String source =
		"""
		/**
		 * Comments that can be formated in several lines...
		 *\s
		 * {@code     \s
		 *       The  			  text		here     should
		 *       not		\t
		 *       be   		    			formatted....   	   }
		 */
		public class X03d {
		}
		""";
	formatSource(source,
		"""
			/**
			 * Comments that can be formated in several lines...
			 *\s
			 * {@code     \s
			 *       The  			  text		here     should
			 *       not		\t
			 *       be   		    			formatted....   	   }
			 */
			public class X03d {
			}
			"""
	);
}
public void testBug260381k() throws JavaModelException {
	String source =
		"""
		/**
		 * Literal inline tag should also be untouched by the formatter
		 *\s
		 * @version {@literal            The  			  text		here     should     not			be   		    			     formatted....   	   }
		 */
		public class X04 {
		
		}
		""";
	formatSource(source);
}
public void testBug260381_wksp1_01() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		package wksp1;
		
		public interface I01 {
		
			/**
			 * Returns all configured content types for the given source viewer. This list
			 * tells the caller which content types must be configured for the given source\s
			 * viewer, i.e. for which content types the given source viewer's functionalities
			 * must be specified. This implementation always returns <code>
			 * new String[] { IDocument.DEFAULT_CONTENT_TYPE }</code>.
			 *
			 * @param source the source viewer to be configured by this configuration
			 * @return the configured content types for the given viewer
			 */
			public String[] getConfiguredContentTypes(String source);
		}
		""";
	formatSource(source,
		"""
			package wksp1;
			
			public interface I01 {
			
				/**
				 * Returns all configured content types for the given source viewer. This
				 * list tells the caller which content types must be configured for the
				 * given source viewer, i.e. for which content types the given source
				 * viewer's functionalities must be specified. This implementation always
				 * returns <code>
				 * new String[] { IDocument.DEFAULT_CONTENT_TYPE }</code>.
				 *
				 * @param source
				 *            the source viewer to be configured by this configuration
				 * @return the configured content types for the given viewer
				 */
				public String[] getConfiguredContentTypes(String source);
			}
			"""
	);
}
public void testBug260381_wksp2_01() throws JavaModelException {
	useOldJavadocTagsFormatting();
	String source =
		"""
		package wksp2;
		public interface I01 {
			/**
			 * Returns the composition of two functions. For {@code f: A->B} and
			 * {@code g: B->C}, composition is defined as the function h such that
			 * {@code h(a) == g(f(a))} for each {@code a}.
			 *
			 * @see <a href="//en.wikipedia.org/wiki/Function_composition">
			 * function composition</a>
			 *
			 * @param g the second function to apply
			 * @param f the first function to apply
			 * @return the composition of {@code f} and {@code g}
			 */
			void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I01 {
				/**
				 * Returns the composition of two functions. For {@code f: A->B} and
				 * {@code g: B->C}, composition is defined as the function h such that
				 * {@code h(a) == g(f(a))} for each {@code a}.
				 *
				 * @see <a href="//en.wikipedia.org/wiki/Function_composition"> function
				 *      composition</a>
				 *
				 * @param g
				 *            the second function to apply
				 * @param f
				 *            the first function to apply
				 * @return the composition of {@code f} and {@code g}
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_01b() throws JavaModelException {
	useOldJavadocTagsFormatting();
	String source =
		"""
		package wksp2;
		public interface I01b {
		  /**
		   * Returns the composition of two functions. For <code> f: A->B</code> and
		   * <code> g: B->C</code>, composition is defined as the function h such that
		   * <code> h(a) == g(f(a))</code> for each <code> a</code>.
		   *
		   * @see <a href="//en.wikipedia.org/wiki/Function_composition">
		   * function composition</a>
		   *
		   * @param g the second function to apply
		   * @param f the first function to apply
		   * @return the composition of <code> f</code> and <code> g</code>
		   */
		  void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I01b {
				/**
				 * Returns the composition of two functions. For <code> f: A->B</code> and
				 * <code> g: B->C</code>, composition is defined as the function h such that
				 * <code> h(a) == g(f(a))</code> for each <code> a</code>.
				 *
				 * @see <a href="//en.wikipedia.org/wiki/Function_composition"> function
				 *      composition</a>
				 *
				 * @param g
				 *            the second function to apply
				 * @param f
				 *            the first function to apply
				 * @return the composition of <code> f</code> and <code> g</code>
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_01c() throws JavaModelException {
	useOldJavadocTagsFormatting();
	String source =
		"""
		package wksp2;
		public interface I01c {
		  /**
		   * Returns the composition of two functions. For <code> f: A->B</code> and
		   * <code>
		   * g: B->C
		   * </code>,
		   * composition is defined as the function h such that
		   * <code>
		   *  h(a) == g(f(a))
		   *  </code>
		   *  for each
		   *  <code>
		   *  a
		   *  </code>.
		   *
		   * @see <a href="//en.wikipedia.org/wiki/Function_composition">
		   * function composition</a>
		   *
		   * @param g the second function to apply
		   * @param f the first function to apply
		   * @return the composition of <code> f</code> and <code> g</code>
		   */
		  void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I01c {
				/**
				 * Returns the composition of two functions. For <code> f: A->B</code> and
				 * <code>
				 * g: B->C
				 * </code>, composition is defined as the function h such that <code>
				 *  h(a) == g(f(a))
				 *  </code> for each <code>
				 *  a
				 *  </code>.
				 *
				 * @see <a href="//en.wikipedia.org/wiki/Function_composition"> function
				 *      composition</a>
				 *
				 * @param g
				 *            the second function to apply
				 * @param f
				 *            the first function to apply
				 * @return the composition of <code> f</code> and <code> g</code>
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_02() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp2;
		
		public interface I02 {
		
		  /**
		   * Implementations of {@code computeNext} <b>must</b> invoke this method when
		   * there are no elements left in the iteration.
		   *
		   * @return {@code null}; a convenience so your {@link #computeNext}
		   *     implementation can use the simple statement {@code return endOfData();}
		   */
		  void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I02 {
			
				/**
				 * Implementations of {@code computeNext} <b>must</b> invoke this method
				 * when there are no elements left in the iteration.
				 *
				 * @return {@code null}; a convenience so your {@link #computeNext}
				 *         implementation can use the simple statement
				 *         {@code return endOfData();}
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_03() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp2;
		
		public interface I03 {
		  /**
		   * A builder for creating immutable bimap instances, especially {@code public
		   * static final} bimaps ("constant bimaps"). Example: <pre>   {@code
		   *
		   *   static final ImmutableBiMap<String, Integer> WORD_TO_INT =
		   *       new ImmutableBiMap.Builder<String, Integer>()
		   *           .put("one", 1)
		   *           .put("two", 2)
		   *           .put("three", 3)
		   *           .build();}</pre>
		   *
		   * For <i>small</i> immutable bimaps, the {@code ImmutableBiMap.of()} methods
		   * are even more convenient.
		   *
		   * <p>Builder instances can be reused - it is safe to call {@link #build}
		   * multiple times to build multiple bimaps in series. Each bimap is a superset
		   * of the bimaps created before it.
		   */
		  void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I03 {
				/**
				 * A builder for creating immutable bimap instances, especially
				 * {@code public
				 * static final} bimaps ("constant bimaps"). Example:
				 *\s
				 * <pre>   {@code
				 *
				 * static final ImmutableBiMap<String, Integer> WORD_TO_INT = new ImmutableBiMap.Builder<String, Integer>()
				 * 		.put("one", 1).put("two", 2).put("three", 3).build();
				 * }</pre>
				 *
				 * For <i>small</i> immutable bimaps, the {@code ImmutableBiMap.of()}
				 * methods are even more convenient.
				 *
				 * <p>
				 * Builder instances can be reused - it is safe to call {@link #build}
				 * multiple times to build multiple bimaps in series. Each bimap is a
				 * superset of the bimaps created before it.
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_03b() throws JavaModelException {
	String source =
		"""
		package wksp2;
		
		public interface I03b {
			/**
			 * A builder for creating immutable bimap instances, xxxxxxxx {@code public
			 * static final} bimaps ("constant bimaps").
			 */
			void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I03b {
				/**
				 * A builder for creating immutable bimap instances, xxxxxxxx {@code public
				 * static final} bimaps ("constant bimaps").
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_04() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		package wksp2;
		
		public interface I04 {
		
		  /**
		   * Returns an immutable multiset containing the given elements.
		   *\s
		   * <p>The multiset is ordered by the first occurrence of each element. For
		   * example, {@code ImmutableMultiset.copyOf(Arrays.asList(2, 3, 1, 3))} yields
		   * a multiset with elements in the order {@code 2, 3, 3, 1}.
		   *
		   * <p>Note that if {@code c} is a {@code Collection<String>}, then {@code
		   * ImmutableMultiset.copyOf(c)} returns an {@code ImmutableMultiset<String>}
		   * containing each of the strings in {@code c}, while
		   * {@code ImmutableMultiset.of(c)} returns an
		   * {@code ImmutableMultiset<Collection<String>>} containing one element
		   * (the given collection itself).
		   *
		   * <p><b>Note:</b> Despite what the method name suggests, if {@code elements}
		   * is an {@code ImmutableMultiset}, no copy will actually be performed, and
		   * the given multiset itself will be returned.
		   *
		   * @throws NullPointerException if any of {@code elements} is null
		   */
		  void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I04 {
			
				/**
				 * Returns an immutable multiset containing the given elements.
				 *\s
				 * <p>
				 * The multiset is ordered by the first occurrence of each element. For
				 * example, {@code ImmutableMultiset.copyOf(Arrays.asList(2, 3, 1, 3))}
				 * yields a multiset with elements in the order {@code 2, 3, 3, 1}.
				 *
				 * <p>
				 * Note that if {@code c} is a {@code Collection<String>}, then {@code
				 * ImmutableMultiset.copyOf(c)} returns an {@code ImmutableMultiset<String>}
				 * containing each of the strings in {@code c}, while
				 * {@code ImmutableMultiset.of(c)} returns an
				 * {@code ImmutableMultiset<Collection<String>>} containing one element (the
				 * given collection itself).
				 *
				 * <p>
				 * <b>Note:</b> Despite what the method name suggests, if {@code elements}
				 * is an {@code ImmutableMultiset}, no copy will actually be performed, and
				 * the given multiset itself will be returned.
				 *
				 * @throws NullPointerException
				 *             if any of {@code elements} is null
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_05() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		package wksp2;
		
		public interface I05 {
		
		  /**
		   * Indexes the specified values into a {@code Multimap} by applying a
		   * specified function to each item in an {@code Iterable} of values. Each
		   * value will be stored as a value in the specified multimap. The key used to
		   * store that value in the multimap will be the result of calling the function
		   * on that value. Depending on the multimap implementation, duplicate entries
		   * (equal keys and equal values) may be collapsed.
		   *
		   * <p>For example,
		   *
		   * <pre class="code">
		   * List&lt;String> badGuys =
		   *   Arrays.asList("Inky", "Blinky", "Pinky", "Pinky", "Clyde");
		   * Function&lt;String, Integer> stringLengthFunction = ...;
		   * Multimap&lt;Integer, String> index = Multimaps.newHashMultimap();
		   * Multimaps.index(badGuys, stringLengthFunction, index);
		   * System.out.println(index); </pre>
		   *
		   * prints
		   *
		   * <pre class="code">
		   * {4=[Inky], 5=[Pinky, Clyde], 6=[Blinky]} </pre>
		   *
		   * The {@link HashMultimap} collapses the duplicate occurrence of
		   * {@code (5, "Pinky")}.
		   *
		   * @param values the values to add to the multimap
		   * @param keyFunction the function used to produce the key for each value
		   * @param multimap the multimap to store the key value pairs
		   */
		  void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I05 {
			
				/**
				 * Indexes the specified values into a {@code Multimap} by applying a
				 * specified function to each item in an {@code Iterable} of values. Each
				 * value will be stored as a value in the specified multimap. The key used
				 * to store that value in the multimap will be the result of calling the
				 * function on that value. Depending on the multimap implementation,
				 * duplicate entries (equal keys and equal values) may be collapsed.
				 *
				 * <p>
				 * For example,
				 *
				 * <pre class="code">
				 * List&lt;String> badGuys =
				 *   Arrays.asList("Inky", "Blinky", "Pinky", "Pinky", "Clyde");
				 * Function&lt;String, Integer> stringLengthFunction = ...;
				 * Multimap&lt;Integer, String> index = Multimaps.newHashMultimap();
				 * Multimaps.index(badGuys, stringLengthFunction, index);
				 * System.out.println(index);
				 * </pre>
				 *
				 * prints
				 *
				 * <pre class="code">
				 * {4=[Inky], 5=[Pinky, Clyde], 6=[Blinky]}
				 * </pre>
				 *
				 * The {@link HashMultimap} collapses the duplicate occurrence of
				 * {@code (5, "Pinky")}.
				 *
				 * @param values
				 *            the values to add to the multimap
				 * @param keyFunction
				 *            the function used to produce the key for each value
				 * @param multimap
				 *            the multimap to store the key value pairs
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_06() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		package wksp2;
		
		public interface I06 {
		
		  /**
		   * Adds a number of occurrences of an element to this multiset. Note that if
		   * {@code occurrences == 1}, this method has the identical effect to {@link
		   * #add(Object)}. This method is functionally equivalent (except in the case
		   * of overflow) to the call {@code addAll(Collections.nCopies(element,
		   * occurrences))}, which would presumably perform much more poorly.
		   *
		   * @param element the element to add occurrences of; may be {@code null} only
		   *     if explicitly allowed by the implementation
		   * @param occurrences the number of occurrences of this element to add. May
		   *     be zero, in which case no change will be made.
		   * @return the previous count of this element before the operation; possibly
		   *     zero - TODO: make this the actual behavior!
		   * @throws IllegalArgumentException if {@code occurrences} is negative, or if
		   *     this operation would result in more than {@link Integer#MAX_VALUE}
		   *     occurrences of the element\s
		   * @throws NullPointerException if {@code element} is null and this
		   *     implementation does not permit null elements. Note that if {@code
		   *     occurrences} is zero, the implementation may opt to return normally.
		   */
		  boolean /*int*/ add(E element, int occurrences);
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I06 {
			
				/**
				 * Adds a number of occurrences of an element to this multiset. Note that if
				 * {@code occurrences == 1}, this method has the identical effect to
				 * {@link #add(Object)}. This method is functionally equivalent (except in
				 * the case of overflow) to the call
				 * {@code addAll(Collections.nCopies(element,
				 * occurrences))}, which would presumably perform much more poorly.
				 *
				 * @param element
				 *            the element to add occurrences of; may be {@code null} only if
				 *            explicitly allowed by the implementation
				 * @param occurrences
				 *            the number of occurrences of this element to add. May be zero,
				 *            in which case no change will be made.
				 * @return the previous count of this element before the operation; possibly
				 *         zero - TODO: make this the actual behavior!
				 * @throws IllegalArgumentException
				 *             if {@code occurrences} is negative, or if this operation
				 *             would result in more than {@link Integer#MAX_VALUE}
				 *             occurrences of the element
				 * @throws NullPointerException
				 *             if {@code element} is null and this implementation does not
				 *             permit null elements. Note that if {@code
				 *     occurrences} is zero, the implementation may opt to return normally.
				 */
				boolean /* int */ add(E element, int occurrences);
			}
			"""
	);
}
public void testBug260381_wksp2_07() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		package wksp2;
		
		public interface I07 {
		
		  /**
		   * Constructs a new, empty multiset, sorted according to the specified
		   * comparator. All elements inserted into the multiset must be <i>mutually
		   * comparable</i> by the specified comparator: {@code comparator.compare(e1,
		   * e2)} must not throw a {@code ClassCastException} for any elements {@code
		   * e1} and {@code e2} in the multiset. If the user attempts to add an element
		   * to the multiset that violates this constraint, the {@code add(Object)} call
		   * will throw a {@code ClassCastException}.
		   *
		   * @param comparator the comparator that will be used to sort this multiset. A
		   *     null value indicates that the elements' <i>natural ordering</i> should
		   *     be used.
		   */
		  void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I07 {
			
				/**
				 * Constructs a new, empty multiset, sorted according to the specified
				 * comparator. All elements inserted into the multiset must be <i>mutually
				 * comparable</i> by the specified comparator: {@code comparator.compare(e1,
				 * e2)} must not throw a {@code ClassCastException} for any elements {@code
				 * e1} and {@code e2} in the multiset. If the user attempts to add an
				 * element to the multiset that violates this constraint, the
				 * {@code add(Object)} call will throw a {@code ClassCastException}.
				 *
				 * @param comparator
				 *            the comparator that will be used to sort this multiset. A null
				 *            value indicates that the elements' <i>natural ordering</i>
				 *            should be used.
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_08() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"""
		package wksp2;
		
		public interface I08 {
		
			  /**
			   * Returns the composition of a function and a predicate. For every {@code x},
			   * the generated predicate returns {@code predicate(function(x))}.
			   *
			   * @return the composition of the provided function and predicate
			   */
			void foo();
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			public interface I08 {
			
				/**
				 * Returns the composition of a function and a predicate. For every
				 * {@code x}, the generated predicate returns
				 * {@code predicate(function(x))}.
				 *
				 * @return the composition of the provided function and predicate
				 */
				void foo();
			}
			"""
	);
}
public void testBug260381_wksp2_09() throws JavaModelException {
	String source =
		"""
		package wksp2;
		
		/**
			A Conditional represents an if/then/else block.
			When this is created the code  will already have
			the conditional check code. The code is optimized for branch
			offsets that fit in 2 bytes, though will handle 4 byte offsets.
		<code>
		     if condition
			 then code
			 else code
		</code>
		     what actually gets built is
		<code>
		     if !condition branch to eb:
			  then code
			  goto end:  // skip else
			 eb:
			  else code
			 end:
		</code>
		*/
		public class X09 {
		
		}
		""";
	formatSource(source,
		"""
			package wksp2;
			
			/**
			 * A Conditional represents an if/then/else block. When this is created the code
			 * will already have the conditional check code. The code is optimized for
			 * branch offsets that fit in 2 bytes, though will handle 4 byte offsets. <code>
			     if condition
				 then code
				 else code
			</code> what actually gets built is <code>
			     if !condition branch to eb:
				  then code
				  goto end:  // skip else
				 eb:
				  else code
				 end:
			</code>
			 */
			public class X09 {
			
			}
			"""
	);
}

/**
 * bug 260798: [formatter] Strange behavior of never join lines
 * test Ensure that the formatter indents lines correctly when never join lines pref is activated
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260798"
 */
public void testBug260798() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		class X {
		    @Override
		    public void addSelectionListener(SelectionListener listener) {
		        super.addSelectionListener(new SelectionListener() {
		            @Override
		            public void widgetSelected(SelectionEvent e) {
		            }
		
		            @Override
		            public void widgetDefaultSelected(SelectionEvent e) {
		            };
		        });
		    }
		}
		""";
	formatSource(source,
		"""
			class X {
				@Override
				public void addSelectionListener(SelectionListener listener) {
					super.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent e) {
						}
			
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						};
					});
				}
			}
			"""
	);
}
public void testBug260798b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		class X {
		
		    void foo() {
		        this.bar(new Object() {
		            @Override
		            public String toString() {
		                return "";
		            }
		        });
		    }
		}
		""";
	formatSource(source,
		"""
			class X {
			
				void foo() {
					this.bar(new Object() {
						@Override
						public String toString() {
							return "";
						}
					});
				}
			}
			"""
	);
}
public void testBug260798c() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		class X {
		
		{
		    this.bar(new Object() {
		        @Override
		        public String toString() {
		            return "";
		        }
		    });
		}
		    void bar(Object object) {
		       \s
		    }
		
		}
		""";
	formatSource(source,
		"""
			class X {
			
				{
					this.bar(new Object() {
						@Override
						public String toString() {
							return "";
						}
					});
				}
			
				void bar(Object object) {
			
				}
			
			}
			"""
	);
}

/**
 * bug 267551: [formatter] Wrong spacing in default array parameter for annotation type
 * test Ensure that no space is inserted before the array initializer when used inside annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=267551"
 */
public void testBug267551() throws JavaModelException {
	String source =
		"""
		import java.lang.annotation.*;
		
		@Target({ ElementType.ANNOTATION_TYPE })
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Foo { }
		""";
	formatSource(source,
		"""
			import java.lang.annotation.*;
			
			@Target({ ElementType.ANNOTATION_TYPE })
			@Retention(RetentionPolicy.RUNTIME)
			public @interface Foo {
			}
			"""
	);
}

/**
 * bug 267658: [formatter] Javadoc comments may be still formatted as block comments
 * test Ensure that javadoc comment are formatted as block comment with certain
 * 	options configuration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=267658"
 */
public void testBug267658() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = true;
	this.formatterPrefs.comment_format_line_comment = false;
	String source =
		"""
		/**
		 * Test for
		 * bug 267658
		 */
		package javadoc;
		
		/**
		 * Test for
		 * bug 267658
		 */
		public class Test {
		/**
		 * Test for
		 * bug 267658
		 */
		int field;
		}
		""";
	formatSource(source,
		"""
			/**
			 * Test for
			 * bug 267658
			 */
			package javadoc;
			
			/**
			 * Test for
			 * bug 267658
			 */
			public class Test {
				/**
				 * Test for
				 * bug 267658
				 */
				int field;
			}
			"""
	);
}
public void testBug267658b() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = true;
	this.formatterPrefs.comment_format_line_comment = false;
	String source =
		"""
		public class Test {
		/**
		 * test bug
		 */
		int field;
		}
		""";
	formatSource(source,
		"""
			public class Test {
				/**
				 * test bug
				 */
				int field;
			}
			"""
	);
}

/**
 * bug 270209: [format] Condensed block comment formatting
 * test Verify that block and javadoc comments are formatted in condensed
 * 		mode when the corresponding preferences is set
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=270209"
 */
public void testBug270209_Block01() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_block_boundaries = false;
	String source =
		"""
		public interface X01 {
		
		/* Instead of like this.  I use these a lot and
		 * this can take up a lot of space. */
		void foo();
		}
		""";
	formatSource(source,
		"""
			public interface X01 {
			
				/* Instead of like this. I use these a lot and this can take up a lot of
				 * space. */
				void foo();
			}
			"""
	);
}
public void testBug270209_Block02() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_block_boundaries = false;
	String source =
		"""
		public interface X02 {
		
		/*
		 * Instead of like this.  I use these a lot and
		 * this can take up a lot of space.
		 */
		void foo();
		}
		""";
	formatSource(source,
		"""
			public interface X02 {
			
				/* Instead of like this. I use these a lot and this can take up a lot of
				 * space. */
				void foo();
			}
			"""
	);
}
public void testBug270209_Block03() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_block_boundaries = false;
	String source =
		"""
		public interface X03 {
		
		/*
		 *\s
		 * Instead of like this.  I use these a lot and
		 * this can take up a lot of space.
		 *\s
		 */
		void foo();
		}
		""";
	formatSource(source,
		"""
			public interface X03 {
			
				/* Instead of like this. I use these a lot and this can take up a lot of
				 * space. */
				void foo();
			}
			"""
	);
}
public void testBug270209_Javadoc01() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_javadoc_boundaries = false;
	String source =
		"""
		public interface X01 {
		
		/** Instead of like this.  I use these a lot and
		 * this can take up a lot of space. */
		void foo();
		}
		""";
	formatSource(source,
		"""
			public interface X01 {
			
				/** Instead of like this. I use these a lot and this can take up a lot of
				 * space. */
				void foo();
			}
			"""
	);
}
public void testBug270209_Javadoc02() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_javadoc_boundaries = false;
	String source =
		"""
		public interface X02 {
		
		/**
		 * Instead of like this.  I use these a lot and
		 * this can take up a lot of space.
		 */
		void foo();
		}
		""";
	formatSource(source,
		"""
			public interface X02 {
			
				/** Instead of like this. I use these a lot and this can take up a lot of
				 * space. */
				void foo();
			}
			"""
	);
}
public void testBug270209_Javadoc03() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_javadoc_boundaries = false;
	String source =
		"""
		public interface X03 {
		
		/**
		 *\s
		 * Instead of like this.  I use these a lot and
		 * this can take up a lot of space.
		 *\s
		 */
		void foo();
		}
		""";
	formatSource(source,
		"""
			public interface X03 {
			
				/** Instead of like this. I use these a lot and this can take up a lot of
				 * space. */
				void foo();
			}
			"""
	);
}

/**
 * bug 273619: [formatter] Formatting repeats *} in javadoc
 * test Ensure that *} is not repeated while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=273619"
 */
public void testBug273619a() throws JavaModelException {
	String source =
		"""
		/**
		 * <ul>
		 * <li>{@code *}</li>
		 * </ul>
		 */
		public class X {
		}
		""";
	formatSource(source,
		"""
			/**
			 * <ul>
			 * <li>{@code *}</li>
			 * </ul>
			 */
			public class X {
			}
			"""
	);
}
public void testBug273619b() throws JavaModelException {
	String source =
		"""
		/**
		 * <p>
		 * {@link *}
		 * </p>
		 */
		public class X {
		}
		""";
	formatSource(source,
		"""
			/**
			 * <p>
			 * {@link *}
			 * </p>
			 */
			public class X {
			}
			"""
	);
}

/**
 * bug 279359: [formatter] Formatter with 'never join lines' produces extra level of indent
 * test Ensure that no extra indentation is produced at the end of a body when
 * 	'never join lines' preference is set.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=279359"
 */
public void testBug279359() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"""
		public class Formatter {
		
		        public static void main(String[] args) {
		
		                Executors.newCachedThreadPool().execute(new Runnable() {
		
		                        public void run() {
		                                throw new UnsupportedOperationException("stub");
		                        }
		
		                });
		
		        }
		}
		""";
	formatSource(source,
		"""
			public class Formatter {
			
				public static void main(String[] args) {
			
					Executors.newCachedThreadPool().execute(new Runnable() {
			
						public void run() {
							throw new UnsupportedOperationException("stub");
						}
			
					});
			
				}
			}
			"""
	);
}

/**
 * bug 280061: [formatter] AIOOBE while formatting javadoc comment
 * test Ensure that no exception occurs while formatting 1.5 snippet
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=280061"
 */
public void testBug280061() throws JavaModelException {
	this.formatterOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
	String source =
		"""
		public interface X {
		/**
		 * <pre>
		 *   void solve(Executor e,
		 *              Collection&lt;Callable&lt;Result&gt;&gt; solvers)
		 *     throws InterruptedException, ExecutionException {
		 *       CompletionService&lt;Result&gt; ecs
		 *           = new ExecutorCompletionService&lt;Result&gt;(e);
		 *       for (Callable&lt;Result&gt; s : solvers)
		 *           ecs.submit(s);
		 *       int n = solvers.size();
		 *       for (int i = 0; i &lt; n; ++i) {
		 *           Result r = ecs.take().get();
		 *           if (r != null)
		 *               use(r);
		 *       }
		 *   }
		 * </pre>
		 */
		 void foo();
		}
		""";
	formatSource(source,
		"""
			public interface X {
				/**
				 * <pre>
				 *   void solve(Executor e,
				 *              Collection&lt;Callable&lt;Result&gt;&gt; solvers)
				 *     throws InterruptedException, ExecutionException {
				 *       CompletionService&lt;Result&gt; ecs
				 *           = new ExecutorCompletionService&lt;Result&gt;(e);
				 *       for (Callable&lt;Result&gt; s : solvers)
				 *           ecs.submit(s);
				 *       int n = solvers.size();
				 *       for (int i = 0; i &lt; n; ++i) {
				 *           Result r = ecs.take().get();
				 *           if (r != null)
				 *               use(r);
				 *       }
				 *   }
				 * </pre>
				 */
				void foo();
			}
			"""
	);
}

/**
 * bug 280255: [formatter] Format edited lines adds two new lines on each save
 * test Ensure that no new line is added while formatting edited lines
 * 	options configuration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=280255"
 */
public void testBug280255() throws JavaModelException {
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"""
		public class X {
			private void foo(int val) {
				switch (val) {
					case 0:
					{
		
		
		[#				return ;#]
					}
				}
			}
		}
		""";
	formatSource(source,
		"""
			public class X {
				private void foo(int val) {
					switch (val) {
						case 0:
						{
			
			
							return;
						}
					}
				}
			}
			"""
	);
}
public void testBug280255b() throws JavaModelException {
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"""
		public class X {\r
			private void foo(int val) {\r
				switch (val) {\r
					case 0:\r
					{\r
		\r
		\r
		[#				return ;#]\r
					}\r
				}\r
			}\r
		}\r
		""";
	formatSource(source,
		"""
			public class X {\r
				private void foo(int val) {\r
					switch (val) {\r
						case 0:\r
						{\r
			\r
			\r
							return;\r
						}\r
					}\r
				}\r
			}\r
			"""
	);
}

/**
 * bug 280616: [formatter] Valid 1.5 code is not formatted inside {@code <pre>} tag
 * test Ensure that 1.5 snippet is formatted when source level is 1.5
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=280616"
 */
public void testBug280616() throws JavaModelException {
	setPageWidth80();
	String source =
		"""
		public interface X {
		/**
		 * <pre>
		 *   void solve(Executor e,
		 *              Collection&lt;Callable&lt;Result&gt;&gt; solvers)
		 *     throws InterruptedException, ExecutionException {
		 *       CompletionService&lt;Result&gt; ecs
		 *           = new ExecutorCompletionService&lt;Result&gt;(e);
		 *       for (Callable&lt;Result&gt; s : solvers)
		 *           ecs.submit(s);
		 *       int n = solvers.size();
		 *       for (int i = 0; i &lt; n; ++i) {
		 *           Result r = ecs.take().get();
		 *           if (r != null)
		 *               use(r);
		 *       }
		 *   }
		 * </pre>
		 */
		 void foo();
		}
		""";
	formatSource(source,
		"""
			public interface X {
				/**
				 * <pre>
				 * void solve(Executor e, Collection&lt;Callable&lt;Result&gt;&gt; solvers)
				 * 		throws InterruptedException, ExecutionException {
				 * 	CompletionService&lt;Result&gt; ecs = new ExecutorCompletionService&lt;Result&gt;(
				 * 			e);
				 * 	for (Callable&lt;Result&gt; s : solvers)
				 * 		ecs.submit(s);
				 * 	int n = solvers.size();
				 * 	for (int i = 0; i &lt; n; ++i) {
				 * 		Result r = ecs.take().get();
				 * 		if (r != null)
				 * 			use(r);
				 * 	}
				 * }
				 * </pre>
				 */
				void foo();
			}
			"""
	);
}

/**
 * bug 287833: [formatter] Formatter removes the first character after the * in the {@code <pre>} tag
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=287833"
 */
public void testBug287833a() {
	String source =
		"""
		public class test1 {
		/**
		* <pre>
		*void foo() {
		*}
		* </pre>
		*/
		void foo() {
		}
		}
		""";

	formatSource(source,
		"""
			public class test1 {
				/**
				 * <pre>
				 * void foo() {
				 * }
				 * </pre>
				 */
				void foo() {
				}
			}
			""");
}
public void testBug287833b() {
	String source =
		"""
		public class test1 {
		/**
		* <pre>
		* void foo() {
		*\r
		* }
		* </pre>
		*/\s
		void foo() {
		}
		}
		""";

	formatSource(source,
		"""
			public class test1 {
				/**
				 * <pre>
				 * void foo() {
				 *\r
				 * }
				 * </pre>
				 */
				void foo() {
				}
			}
			""");
}
public void testBug287833c() {
	String source =
		"""
		public class test1 {
		/**
		* <pre>
		* void foo() {
		*
		* }
		* </pre>
		*/\s
		void foo() {
		}
		}
		""";

	formatSource(source,
		"""
			public class test1 {
				/**
				 * <pre>
				 * void foo() {
				 *
				 * }
				 * </pre>
				 */
				void foo() {
				}
			}
			""");
}

/**
 * bug 295825: [formatter] Commentaries are running away after formatting are used
 * test Verify that block comment stay unchanged when text starts with a star
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295825"
 */
public void testBug295825() {
	String source =
		"""
		public class A {
			/* * command */
			void method() {
			}
		}
		""";
	formatSource(source);
}

/**
 * bug 300379: [formatter] Fup of bug 287833
 * test Verify that the leading '{' is not deleted while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=300379"
 */
public void testBug300379() {
	String source =
		"""
		public class X {
		    /**
		     * <pre>   {@code
		     *\s
		     *   public class X {
		     *   }}</pre>
		     */
		    public void foo() {}
		}
		""";

	formatSource(source,
		"""
			public class X {
				/**
				 * <pre>   {@code
				 *\s
				 * public class X {
				 * }
				 * }</pre>
				 */
				public void foo() {
				}
			}
			""");
}
public void testBug300379b() {
	String source =
		"""
		public class X {
		    /**
		     * <pre>   {@code
		     *\s
		     *   public class X {}}</pre>
		     */
		    public void foo() {}
		}
		""";

	formatSource(source,
		"""
			public class X {
				/**
				 * <pre>   {@code
				 *\s
				 * public class X {
				 * }
				 * }</pre>
				 */
				public void foo() {
				}
			}
			""");
}

/**
 * bug 304705: [formatter] Unexpected indentation of wrapped line comments when 'Never indent line comment on first column' preference is checked
 * test Verify that wrapped line comments are also put at first column
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=304705"
 */
public void testBug304705() {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"""
		public interface Example {
		// This is a long comment    with	whitespace     that should be split in multiple line comments in case the line comment formatting is enabled
			int foo();
		}
		""";
	formatSource(source,
		"""
			public interface Example {
			// This is a long comment with whitespace that should be split in multiple line
			// comments in case the line comment formatting is enabled
				int foo();
			}
			""");
}
public void testBug304705b() {
	this.formatterPrefs.never_indent_block_comments_on_first_column = true;
	String source =
		"""
		public interface Example {
		/* This is a long comment    with	whitespace     that should be split in multiple line comments in case the line comment formatting is enabled */
			int foo();
		}
		""";
	formatSource(source,
		"""
			public interface Example {
			/*
			 * This is a long comment with whitespace that should be split in multiple line
			 * comments in case the line comment formatting is enabled
			 */
				int foo();
			}
			""");
}

/**
 * bug 305281: [formatter] Turning off formatting changes comment's formatting
 * test Verify that turning off formatting in a javadoc does not screw up its indentation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305281"
 */
public void testBug305281() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "format: OFF");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "format: ON");
	String source =
		"""
		public class test {
		
		    /**
		     * @param args
		     * format: OFF
		     */
		    public static void main(String[] args) {
		        do {
		            } while (false);
		        for (;;) {
		        }
		        // format: ON
		    }
		}
		""";
	formatSource(source,
		"""
			public class test {
			
				/**
			     * @param args
			     * format: OFF
			     */
			    public static void main(String[] args) {
			        do {
			            } while (false);
			        for (;;) {
			        }
			        // format: ON
				}
			}
			""");
}

/**
 * bug 305371: [formatter] Unexpected indentation of line comment
 * test Verify that comments with too different indentation are not considered as contiguous
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305371"
 */
public void testBug305371() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	String source =
		"""
		class X01 {
		//  unformatted    comment    !
		        //  formatted    comment    !
		}
		""";
	formatSource(source,
		"""
			class X01 {
			//  unformatted    comment    !
				// formatted comment !
			}
			""");
}
public void testBug305371b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	String source =
		"""
		class X02 {
		        //  formatted    comment    !
		//  unformatted    comment    !
		}
		""";
	formatSource(source,
		"""
			class X02 {
				// formatted comment !
			//  unformatted    comment    !
			}
			""");
}
public void testBug305371c() {
	String source =
		"""
		class X03 {
		        //  formatted    comment    1
		    //  formatted    comment    2
		}
		""";
	formatSource(source,
		"""
			class X03 {
				// formatted comment 1
				// formatted comment 2
			}
			""");
}
public void testBug305371d() {
	String source =
		"""
		class X04 {
		    //  formatted    comment    1
		        //  formatted    comment    2
		}
		""";
	formatSource(source,
		"""
			class X04 {
				// formatted comment 1
				// formatted comment 2
			}
			""");
}

/**
 * bug 305518: [formatter] Line inside &lt;pre&gt; tag is wrongly indented by one space when starting just after the star
 * test Verify formatting of a &lt;pre&gt; tag section keep lines right indented
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305518"
 */
public void testBug305518() {
	String source =
		"""
		public interface Test {
		/**
		 * <pre>
		 *    A
		 *   / \\
		 *  B   C
		 * / \\ / \\
		 *D  E F  G
		 * </pre>
		 */
		public void foo();
		}
		""";
	formatSource(source,
		"""
			public interface Test {
				/**
				 * <pre>
				 *    A
				 *   / \\
				 *  B   C
				 * / \\ / \\
				 *D  E F  G
				 * </pre>
				 */
				public void foo();
			}
			""");
}
public void testBug305518_wksp2_01() {
	useOldCommentWidthCounting();
	String source =
		"""
		public class X01 {
		/**
			<P> This is an example of starting and shutting down the Network Server in the example
			above with the API.
			<PRE>
		\t
			NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName("myhost"),1621)
		
			serverControl.shutdown();
			</PRE>
		
		\t
		*/
		public void foo() {}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				/**
				 * <P>
				 * This is an example of starting and shutting down the Network Server in
				 * the example above with the API.
				 *\s
				 * <PRE>
			\t
				NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName("myhost"),1621)
			\t
				serverControl.shutdown();
				 * </PRE>
				 *\s
				 *\s
				 */
				public void foo() {
				}
			}
			""");
}
public void testBug305518_wksp2_02() {
	String source =
		"""
		public class X02 {
		/**
		 * Represents namespace name:
		 * <pre>e.g.<pre>MyNamespace;
		 *MyProject\\Sub\\Level;
		 *namespace\\MyProject\\Sub\\Level;
		 */
		public void foo() {}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				/**
				 * Represents namespace name:
				 *\s
				 * <pre>
				 * e.g.
				 *\s
				 * <pre>
				 * MyNamespace; MyProject\\Sub\\Level; namespace\\MyProject\\Sub\\Level;
				 */
				public void foo() {
				}
			}
			""");
}
public void testBug305518_wksp2_03() {
	String source =
		"""
		public class X03 {
		/**
		* <PRE>
		*  String s = ... ; // get string from somewhere
		*  byte [] compressed = UnicodeCompressor.compress(s);
		* </PRE>
		 */
		public void foo() {}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				/**
				 * <PRE>
				*  String s = ... ; // get string from somewhere
				*  byte [] compressed = UnicodeCompressor.compress(s);
				 * </PRE>
				 */
				public void foo() {
				}
			}
			""");
}

/**
 * bug 305830: [formatter] Turning off formatting changes comment's formatting
 * test Verify that turning off formatting in a javadoc does not screw up its indentation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305830"
 */
public void testBug305830() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "40");
	String source =
		"""
		public class X01 {
		void foo() {
		bar("a non-nls string", 0 /*a    comment*/); //$NON-NLS-1$
		}
		void bar(String string, int i) {
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				void foo() {
					bar("a non-nls string", //$NON-NLS-1$
							0 /* a comment */);
				}
			
				void bar(String string, int i) {
				}
			}
			"""
	);
}
public void testBug305830b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "40");
	String source =
		"""
		public class X02 {
		void foo() {
		bar("str", 0 /*a    comment*/); //$NON-NLS-1$
		}
		void bar(String string, int i) {
		}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				void foo() {
					bar("str", 0 /* a comment */); //$NON-NLS-1$
				}
			
				void bar(String string, int i) {
				}
			}
			"""
	);
}
public void testBug305830c() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "40");
	String source =
		"""
		public class X03 {
		void foo() {
		bar("str", 0 /*              a						comment                            */); //$NON-NLS-1$
		}
		void bar(String string, int i) {
		}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				void foo() {
					bar("str", 0 /* a comment */); //$NON-NLS-1$
				}
			
				void bar(String string, int i) {
				}
			}
			"""
	);
}
public void testBug305830d() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "40");
	String source =
		"""
		public class X01 {
		void foo() {
		bar("a non-nls string" /*a    comment*/, 0); //$NON-NLS-1$
		}
		void bar(String string, int i) {
		}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				void foo() {
					bar("a non-nls string" /* a comment */, //$NON-NLS-1$
							0);
				}
			
				void bar(String string, int i) {
				}
			}
			"""
	);
}

/**
 * bug 309835: [formatter] adds blank lines on each run
 * test Test that no line is added when the word exceeds the line width
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=309835"
 */
public void testBug309835() {
	this.formatterPrefs.join_lines_in_comments = false;
	this.formatterPrefs.comment_line_length = 120;
	String source =
		"""
		package org.eclipse.bug.test;
		
		/**
		 * @author Bug Reporter
		 *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____
		 *\s
		 *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long,
		 *         a blank line is inserted on each formating.
		 *         Check project preferences to see the format settings
		 *         (max. line length 80 chars, max. comment line length 120 chars)
		 */
		public class FormatterBug {
		}
		""";
	formatSource(source);
}
public void testBug309835b() {
	this.formatterPrefs.comment_line_length = 120;
	String source =
		"""
		package org.eclipse.bug.test;
		
		/**
		 * @author Bug Reporter
		 *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____
		 *\s
		 *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long,
		 *         a blank line is inserted on each formating.
		 *         Check project preferences to see the format settings
		 *         (max. line length 80 chars, max. comment line length 120 chars)
		 */
		public class FormatterBug {
		}
		""";
	formatSource(source,
		"""
			package org.eclipse.bug.test;
			
			/**
			 * @author Bug Reporter
			 *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____
			 *\s
			 *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long, a blank
			 *         line is inserted on each formating. Check project preferences to see the format settings (max. line length 80
			 *         chars, max. comment line length 120 chars)
			 */
			public class FormatterBug {
			}
			"""
	);
}
public void testBug309835c() {
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"""
		package org.eclipse.bug.test;
		
		/**
		 * @author Bug Reporter
		 *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____
		 *\s
		 *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long,
		 *         a blank line is inserted on each formating.
		 *         Check project preferences to see the format settings
		 *         (max. line length 80 chars, max. comment line length 120 chars)
		 */
		public class FormatterBug {
		}
		""";
	formatSource(source,
		"""
			package org.eclipse.bug.test;
			
			/**
			 * @author Bug Reporter
			 *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____
			 *\s
			 *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line
			 *         is exactly 121 characters long,
			 *         a blank line is inserted on each formating.
			 *         Check project preferences to see the format settings
			 *         (max. line length 80 chars, max. comment line length 120 chars)
			 */
			public class FormatterBug {
			}
			"""
	);
}
public void testBug309835d() {
	String source =
		"""
		package org.eclipse.bug.test;
		
		/**
		 * @author Bug Reporter
		 *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____
		 *\s
		 *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long,
		 *         a blank line is inserted on each formating.
		 *         Check project preferences to see the format settings
		 *         (max. line length 80 chars, max. comment line length 120 chars)
		 */
		public class FormatterBug {
		}
		""";
	formatSource(source,
		"""
			package org.eclipse.bug.test;
			
			/**
			 * @author Bug Reporter
			 *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____
			 *\s
			 *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line
			 *         is exactly 121 characters long, a blank line is inserted on each
			 *         formating. Check project preferences to see the format settings (max.
			 *         line length 80 chars, max. comment line length 120 chars)
			 */
			public class FormatterBug {
			}
			"""
	);
}
public void testBug309835_wksp1_01() {
	useOldJavadocTagsFormatting();
	String source =
		"""
		public class X01 {
		
			/**
			 * @param severity the severity to search for. Must be one of <code>FATAL
			 *  </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
			 */
			public void foo(int severity) {
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
				/**
				 * @param severity
				 *            the severity to search for. Must be one of <code>FATAL
				 *  </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
				 */
				public void foo(int severity) {
				}
			}
			"""
	);
}
public void testBug309835_wksp1_02() {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"""
		public class X02 {
		
			/**
			 * INTERNAL USE-ONLY
			 * Generate the byte for a problem method info that correspond to a boggus method.
			 *
			 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
			 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
			 */
			public void foo(int severity) {
			}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
			
				/**
				 * INTERNAL USE-ONLY Generate the byte for a problem method info that
				 * correspond to a boggus method.
				 *
				 * @param method
				 *            org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
				 * @param methodBinding
				 *            org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
				 */
				public void foo(int severity) {
				}
			}
			"""
	);
}
public void testBug309835_wksp2_01() {
	String source =
		"""
		public class X01 {
		
			/**
		     * Given a jar file, get the names of any AnnotationProcessorFactory
		     * implementations it offers.  The information is based on the Sun
		     * <a href="http://download.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider">
		     * Jar Service Provider spec</a>: the jar file contains a META-INF/services
		     */
			public void foo() {
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
			
				/**
				 * Given a jar file, get the names of any AnnotationProcessorFactory
				 * implementations it offers. The information is based on the Sun <a href=
				 * "http://download.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider">
				 * Jar Service Provider spec</a>: the jar file contains a META-INF/services
				 */
				public void foo() {
				}
			}
			"""
	);
}

/**
 * bug 311864: [formatter] NPE with empty {@code }
 * test Ensure that no NPE occurs while formatting an empty code inline tag.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=311864"
 */
public void testBug311864() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		public class Test {
		
		/**
		* Compares two property values. For font or color the <i>description</i> of
		* the resource, {@link FontData} or {@link RGB}, is used for comparison.
		*
		* @param value1
		* first property value
		* @param value2
		* second property value
		* @return {@code true} if the values are equals; otherwise {@code}
		*/
		boolean foo(int value1, int value2) {
			return value1 > value2;
		}
		}
		""";
	formatSource(source,
		"""
			public class Test {
			
				/**
				 * Compares two property values. For font or color the <i>description</i> of
				 * the resource, {@link FontData} or {@link RGB}, is used for comparison.
				 *
				 * @param value1
				 *            first property value
				 * @param value2
				 *            second property value
				 * @return {@code true} if the values are equals; otherwise {@code}
				 */
				boolean foo(int value1, int value2) {
					return value1 > value2;
				}
			}
			"""
	);
}

/**
 * bug 315577: [formatter] NullPointerException (always) on inserting a custom template proposal into java code when "Use code formatter" is on
 * test Ensure that no NPE occurs when inserting the custom template
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=315577"
 */
public void testBug315577() throws JavaModelException {
	String source =
		"""
		public class C {
		
			/**
			 * aaaa aaa aaa.<br>
			 * {@link C}: aaaa.<br>
			 * {@link C}: aaaa.<br>
			 * aaa {@link C}: aaaa.<br>
			 * {@link C}: aaaa<br>
			 * {@link C}: aaaa.<br>
			 */
			public C() {
			}
		}
		""";
	formatSource(source);
}

/**
 * bug 315732: [formatter] NullPointerException (always) on inserting a custom template proposal into java code when "Use code formatter" is on
 * test Ensure that no NPE occurs when inserting the custom template
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=315732"
 */
public void testBug315732() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"""
		// ============================================================================\r
		// /*-*/\r
		// ============================================================================\r
		""";
	formatSource(source,
		"""
				// ============================================================================
				// /*-*/
				// ============================================================================
			""",
		CodeFormatter.K_UNKNOWN,
		1,
		true
	);
}

/**
 * bug 313651: [formatter] Unexpected indentation of line comment
 * test Verify that comments with too different indentation are not considered as contiguous
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=313651"
 */
public void testBug313651_01() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"""
		public class X01 {
			public void testMethod() {
				// Comment 1
				System.out.println("start");
		//		System.out.println("next");
				// Comment 1
				System.out.println("end");
			}
		}
		""";
	formatSource(source);
}
public void testBug313651_01b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"""
		public class X01 {
			public void testMethod() {
				// Comment 1
				System.out.println("start");
		//		System.out.println("next");
				// Comment 1
				System.out.println("end");
			}
		}
		""";
	formatSource(source);
}
public void testBug313651_01c() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		public class X01 {
			public void testMethod() {
				// Comment 1
				System.out.println("start");
		//		System.out.println("next");
				// Comment 1
				System.out.println("end");
			}
		}
		""";
	formatSource(source,
		"""
			public class X01 {
				public void testMethod() {
					// Comment 1
					System.out.println("start");
					// System.out.println("next");
					// Comment 1
					System.out.println("end");
				}
			}
			"""
	);
}
public void testBug313651_02() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"""
		public class X02 {
			public void testMethod() {
				// Comment 1
				System.out.println("start");
				System.out.println("next");
				// Comment 1
		//		System.out.println("end");
			}
		}
		""";
	formatSource(source);
}
public void testBug313651_02b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"""
		public class X02 {
			public void testMethod() {
				// Comment 1
				System.out.println("start");
				System.out.println("next");
				// Comment 1
		//		System.out.println("end");
			}
		}
		""";
	formatSource(source);
}
public void testBug313651_02c() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		public class X02 {
			public void testMethod() {
				// Comment 1
				System.out.println("start");
				System.out.println("next");
				// Comment 1
		//		System.out.println("end");
			}
		}
		""";
	formatSource(source,
		"""
			public class X02 {
				public void testMethod() {
					// Comment 1
					System.out.println("start");
					System.out.println("next");
					// Comment 1
					// System.out.println("end");
				}
			}
			"""
	);
}
public void testBug313651_03() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"""
		public class X03 {
			public void testMethod() {
				// Comment 1
				System.out.println("start");
		//		System.out.println("next");
				// Comment 1
		//		System.out.println("end");
			}
		}
		""";
	formatSource(source);
}
public void testBug313651_03b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"""
		public class X03 {
			public void testMethod() {
				// Comment 1
				System.out.println("start");
		//		System.out.println("next");
				// Comment 1
		//		System.out.println("end");
			}
		}
		""";
	formatSource(source);
}
public void testBug313651_03c() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		public class X03 {
			public void testMethod() {
				// Comment 1
				System.out.println("start");
		//		System.out.println("next");
				// Comment 1
		//		System.out.println("end");
			}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				public void testMethod() {
					// Comment 1
					System.out.println("start");
					// System.out.println("next");
					// Comment 1
					// System.out.println("end");
				}
			}
			"""
	);
}
public void testBug313651_wksp3_01() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		package wksp3;
		public class X01 implements
		// start of comment
		// MyFirstInterface {
			MySecondInterface {
		// end of comment
		}
		""";
	formatSource(source,
		"""
			package wksp3;
			
			public class X01 implements
					// start of comment
					// MyFirstInterface {
					MySecondInterface {
				// end of comment
			}
			"""
	);
}
public void testBug313651_wksp3_02() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"""
		package wksp3;
		public class X02 implements MyOtherInterface,\s
		// start of comment
		// MyFirstInterface {
			MySecondInterface {
		// end of comment
		}
		""";
	formatSource(source,
		"""
			package wksp3;
			
			public class X02 implements MyOtherInterface,
					// start of comment
					// MyFirstInterface {
					MySecondInterface {
				// end of comment
			}
			"""
	);
}
public void testBug348338() {
	useOldCommentWidthCounting();
	String source =
		"""
		public class X03 {
			/**
			 * Check wrapping of javadoc tags surrounded wit punctuation [{@code marks}].
			 * <p>
			 * Check wrapping of string literals surrounded with punctuation marks ("e.g. in parenthesis" wraps).
			 * <p>
			 * {@code Sometimes wrapping on punctuation is necessary because line is too}. long otherwise.
			 */
			public void test() {
		
				/*
				 * Check wrapping of string literals surrounded with punctuation marks ("e.g. in parenthesis" wraps).
				 *\s
				 * The dot at the end of this sentence is beyond the line "length limit".
				 *\s
				 * But this sentence should fit in the line length limit "with the dot".
				 */
			}
		}
		""";
	formatSource(source,
		"""
			public class X03 {
				/**
				 * Check wrapping of javadoc tags surrounded wit punctuation
				 * [{@code marks}].
				 * <p>
				 * Check wrapping of string literals surrounded with punctuation marks
				 * ("e.g. in parenthesis" wraps).
				 * <p>
				 * {@code Sometimes wrapping on punctuation is necessary because line is too}.
				 * long otherwise.
				 */
				public void test() {
			
					/*
					 * Check wrapping of string literals surrounded with punctuation marks
					 * ("e.g. in parenthesis" wraps).
					 *\s
					 * The dot at the end of this sentence is beyond the line
					 * "length limit".
					 *\s
					 * But this sentence should fit in the line length limit "with the dot".
					 */
				}
			}
			"""
	);
}
public void testBug470986() {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"""
		class Example {  	 // test
		
			void method1() {   	  // test
				int a = 1; // test
			}// test
		
		}""";
	formatSource(source);
}
public void testBug471062() {
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"""
		class C {\r
			void method() {\r
				Arrays.asList(1, 2,   // test\r
						3, 4);\r
				if (condition)        // test\r
					operation();\r
			}\r
		}""";
	formatSource(source);
}
public void testBug471918() {
	String source =
		"""
		class C {
		
			/** Returns a new foo instance. */
			public Foo createFoo1() {
			}
		
			/** @return a new foo instance. */
			public Foo createFoo2() {
			}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/474011 - [formatter] non-nls strings are duplicated by formatter
 */
public void testBug474011() {
	useOldCommentWidthCounting();
	String source =
		"""
		class A {
			String aaaaaaaaaaaaaaaa = "11111111111111111111111111111111111111"; //$NON-NLS-1$ aaa bbb ccc
			String bbbbbbbbbbbbbbbb = "22222222222222222222222222222222222222"; //$NON-NLS-1$ //$NON-NLS-1$
			String cccccccccccccccc = "33333333333333333333333333333333333333"; //$NON-NLS-1$ //$NON-NLS-2$
			String dddddddddddddddd = "44444444444444444444444444444444444444"; //$NON-NLS-1$ // $NON-NLS-2$
			String eeeeeeeeeeeeeeee = "55555555555555555555555555555555555555"; //$NON-NLS-1$ // aaa // bbb
		}""";
	formatSource(source,
		"""
			class A {
				String aaaaaaaaaaaaaaaa = "11111111111111111111111111111111111111"; //$NON-NLS-1$ aaa
																					// bbb
																					// ccc
				String bbbbbbbbbbbbbbbb = "22222222222222222222222222222222222222"; //$NON-NLS-1$
				String cccccccccccccccc = "33333333333333333333333333333333333333"; //$NON-NLS-1$ //$NON-NLS-2$
				String dddddddddddddddd = "44444444444444444444444444444444444444"; //$NON-NLS-1$ //
																					// $NON-NLS-2$
				String eeeeeeeeeeeeeeee = "55555555555555555555555555555555555555"; //$NON-NLS-1$ //
																					// aaa
																					// //
																					// bbb
			}""");
}
/**
 * https://bugs.eclipse.org/475294 - [formatter] "Preserve whitespace..." problems with wrapped line comments
 */
public void testBug475294() {
	useOldCommentWidthCounting();
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"""
		public class A {
			void a() {
				System.out.println();// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println(); // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();   // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println(); 	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();  	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();   	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();    	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();  	  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();		// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
			}
		}""";
	formatSource(source,
		"""
			public class A {
				void a() {
					System.out.println();// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
											// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println(); // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
											 // ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
											  // ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();   // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
											   // ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
												// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println(); 	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
											 	// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();  	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
											  	// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();   	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
											   	// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();    	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
											    	// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();  	  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
											  	  // ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();		// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
													// ddddddddddddddd eeeeeeeeeeeeeee
				}
			}""");
}
/**
 * https://bugs.eclipse.org/475294 - [formatter] "Preserve whitespace..." problems with wrapped line comments
 */
public void testBug475294b() {
	useOldCommentWidthCounting();
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"""
		public class A {
			void a() {
				System.out.println();// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println(); // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();   // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println(); 	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();  	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();   	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();    	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();  	  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
				System.out.println();		// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee
			}
		}""";
	formatSource(source,
		"""
			public class A {
				void a() {
					System.out.println();// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                     // ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println(); // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                      // ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                       // ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();   // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                        // ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                     	// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println(); 	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                      	// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();  	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                       	// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();   	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                        	// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();    	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                         	// ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();  	  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                       	  // ddddddddddddddd eeeeeeeeeeeeeee
					System.out.println();		// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc
					                     		// ddddddddddddddd eeeeeeeeeeeeeee
				}
			}""");
}
/**
 * https://bugs.eclipse.org/479292 - [formatter] Header comment formatting for package-info.java occurs even when "Format header comment" is unchecked
 */
public void testBug479292() {
	String source =
		"""
		/** This   is   a   header   comment */
		
		/** This   is   a   package   javadoc */
		package test;""";
	formatSource(source,
		"""
			/** This   is   a   header   comment */
			
			/** This is a package javadoc */
			package test;"""
	);
}
/**
 * https://bugs.eclipse.org/479292 - [formatter] Header comment formatting for package-info.java occurs even when "Format header comment" is unchecked
 */
public void testBug479292b() {
	this.formatterPrefs.comment_format_header = true;
	String source =
		"""
		/** This   is   a   header   comment */
		
		/** This   is   a   package   javadoc */
		package test;""";
	formatSource(source,
		"""
			/** This is a header comment */
			
			/** This is a package javadoc */
			package test;"""
	);
}
/**
 * https://bugs.eclipse.org/121728 - [formatter] Code formatter thinks <P> generic class parameter is a HTML <p> tag
 */
public void testBug121728() {
	String source =
			"""
		/**
		 * Test Class
		 *
		 * @param <P> Some generic class parameter
		 */
		public class Test<P> {
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/479469 - [formatter] Line wrap for long @see references
 */
public void testBug479469() {
	String source =
		"""
		/**
		 * Test Class
		 * @see a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
		 *
		 * @see a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa Label can be wrapped
		 *
		 * @see <a href="a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa">Label can be wrapped</a>
		 */
		public class Test {
		}""";
	formatSource(source,
		"""
			/**
			 * Test Class
			 *\s
			 * @see a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
			 *
			 * @see a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
			 *      Label can be wrapped
			 *
			 * @see <a href=
			 *      "a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa">Label
			 *      can be wrapped</a>
			 */
			public class Test {
			}"""
	);
}
/**
 * https://bugs.eclipse.org/480029 - [formatter] Comments indentation error in javadoc @return statements
 */
public void testBug480029() {
	String source =
		"""
		public class JavadocCommentIssue
		{
			/** @return <ul><li>Case 1</b></li></ul> */
			public int foo() {return 0;}
		}
		""";
	formatSource(source,
		"""
			public class JavadocCommentIssue {
				/**
				 * @return
				 *         <ul>
				 *         <li>Case 1</b></li>
				 *         </ul>
				 */
				public int foo() {
					return 0;
				}
			}
			"""
	);
}
/**
 * https://bugs.eclipse.org/480030 - [formatter] Comments indentation error in switch statements
 */
public void testBug480030() {
	String source =
		"""
		public class SwitchCommentIssue {
			public void switchIssue(int a) {
				while (a > 0) {
					switch (a) {
					// Test
					case 1:
						break;
					// Test
					case 2:
						continue;
					// Test
					case 3:
						return;
					// Test
					case 4: {
						return;
					}
					// test
					}
				}
			}
		}
		""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/479474 - [formatter] Problems when doc.comment.support=disabled
 */
public void testBug479474() {
	Hashtable<String, String> parserOptions = JavaCore.getOptions();
	try {
		Hashtable<String, String> newParserOptions = JavaCore.getOptions();
		newParserOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.DISABLED);
		JavaCore.setOptions(newParserOptions);
		String source =
			"""
			/**
			 * Test
			 * @author mr.awesome
			 */
			public class Test {
			}""";
		formatSource(source,
			"""
				/**
				 * Test
				 *\s
				 * @author mr.awesome
				 */
				public class Test {
				}"""
		);
	} finally {
		JavaCore.setOptions(parserOptions);
	}
}
/**
 * https://bugs.eclipse.org/484957 - [formatter] Extra blank lines between consecutive javadoc comments
 */
public void testBug484957() {
	String source =
		"""
		import java.io.Serializable;
		
		/**********/
		/*** A ****/
		/**********/
		
		public class MyClass implements Serializable {
			private int field1;
		
			/**********/
			/*** B ****/
			/**********/
			public void foo() {
			}
		
			/**********/
			/*** C ****/
			/**********/
			private int field2;
		
			/**********/
			/*** D ****/
			/**********/
			private class NestedType {
			}
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/93459 - [formatter] Javadoc formatter does not break URLs
 */
public void testBug93459() {
	this.formatterPrefs.comment_line_length = 120;
	String source =
		"""
		class Example {
			/**
			 * This is similar to <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html#isSupported(java.lang.String)">java.nio.charset.Charset.isSupported(String)</a>
			 */
			int a;
		}""";
	formatSource(source,
		"""
			class Example {
				/**
				 * This is similar to <a href=
				 * "http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html#isSupported(java.lang.String)">java.nio.charset.Charset.isSupported(String)</a>
				 */
				int a;
			}"""
	);
}
/**
 * https://bugs.eclipse.org/510995 - NPE at CommentsPreparator.translateFormattedTokens when using $NON-NLS-1$ in Javadoc
 */
public void testBug510995() {
	String source =
		"""
		/**
		 * <pre>
		 * NAME = &quot;org.test....&quot; //$NON-NLS-1$
		 * </pre>
		 */
		class Test {
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/510995 - NPE at CommentsPreparator.translateFormattedTokens when using $NON-NLS-1$ in Javadoc
 */
public void testBug510995b() {
	String source =
		"""
		/**
		 * <pre>
		 * NAME = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; //$NON-NLS-1$ //$NON-NLS-2$
		 * </pre>
		 */
		class Test {
		}""";
	formatSource(source,
		"""
			/**
			 * <pre>
			 * NAME = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" //$NON-NLS-1$
			 * 		+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; //$NON-NLS-1$
			 * </pre>
			 */
			class Test {
			}"""
	);
}
/**
 * https://bugs.eclipse.org/512095 - [formatter] Unstable wrap on a line with wrapped code and wrapped block comment
 */
public void testBug512095() {
	useOldCommentWidthCounting();
	String source =
		"""
		class Test1 {
			void f() {
				String c = "aaaaaaaaaaaaaaaa" + "aaaaaaaaaaaaaaa"; /* 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 */
			}
		}""";
	formatSource(source,
		"""
			class Test1 {
				void f() {
					String c = "aaaaaaaaaaaaaaaa"
							+ "aaaaaaaaaaaaaaa"; /*
													 * 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
													 * 16 17
													 */
				}
			}"""
	);
}
/**
 * https://bugs.eclipse.org/545898 - [formatter] IOOBE with empty javadoc @param
 */
public void testBug545898() {
	useOldCommentWidthCounting();
	String source =
		"""
		/**
		 * @param
		 */
		void foo() {
		}""";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/552012 - Javadoc formatting breaks {@code} code block.
 */
public void testBug552012a() {
	setComplianceLevel(CompilerOptions.VERSION_17);
	String source =
		"""
		/**
		 * Test1
		 * <pre>{@code
		 * public class X {@Deprecated int    a    ;
		 * }
		 * }</pre>
		 * Test2
		 * <pre>{@code int    a = 1   ;}</pre>
		 * Test3
		 * <pre>
		 * code sample: {@code public   void foo( ){}  }
		 * literal sample: {@literal public   void foo( ){}  }
		 * the end
		 * </pre>
		 */
		public class MyTest {
		}""";
	formatSource(source,
		"""
			/**
			 * Test1
			 *\s
			 * <pre>{@code
			 * public class X {
			 * 	@Deprecated
			 * 	int a;
			 * }
			 * }</pre>
			 *\s
			 * Test2
			 *\s
			 * <pre>{@code
			 * int a = 1;
			 * }</pre>
			 *\s
			 * Test3
			 *\s
			 * <pre>
			 * code sample: {@code
			 * public void foo() {
			 * }
			 * }
			 * literal sample: {@literal public   void foo( ){}  }
			 * the end
			 * </pre>
			 */
			public class MyTest {
			}""");
}
/**
 * https://bugs.eclipse.org/552012 - Javadoc formatting breaks {@code} code block.
 */
public void testBug552012b() {
	String source =
		"""
		/**
		 * <pre>{@code
		 * public class X {@Deprecated int    a    ;
		 * }
		 * }</pre>
		 */
		public class MyTest {
		}""";
	formatSource(source,
		"""
			/**
			 * <pre>{@code
			 * public class X { @Deprecated
			 * 	int a;
			 * }
			 * }</pre>
			 */
			public class MyTest {
			}""");
}
/**
 * https://bugs.eclipse.org/552012 - Javadoc formatting breaks {@code} code block.
 */
public void testBug552012c() {
	String source =
		"""
		/**
		 * <pre>@something something</pre>
		 */
		public class MyTest {
		}""";
	formatSource(source,
		"""
			/**
			 * <pre>
			 * &#64;something something
			 * </pre>
			 */
			public class MyTest {
			}""");
}
/**
 * https://bugs.eclipse.org/552012 - Javadoc formatting breaks {@code} code block.
 */
public void testBug552012d() {
	String source =
		"""
		/**
		 * <pre>
		 * @something something
		 * </pre>
		 */
		public class MyTest {
		}""";
	formatSource(source);
}
/**
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2127
 */
public void testIssue2127a() {
	setComplianceLevel(CompilerOptions.VERSION_9);
	String source = """
		/** This   is   a   header   comment */

		/** This   is   a   module   javadoc */
		module test{}
		""";
	formatSource(source, """
		/** This   is   a   header   comment */

		/** This is a module javadoc */
		module test {
		}
		""",
		CodeFormatter.K_MODULE_INFO | CodeFormatter.F_INCLUDE_COMMENTS);
}
/**
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2127
 */
public void testIssue2127b() {
	setComplianceLevel(CompilerOptions.VERSION_9);
	this.formatterPrefs.comment_format_header = true;
	String source = """
		/** This   is   a   header   comment */

		/** This   is   a   module   javadoc */
		module test{}
		""";
	formatSource(source, """
		/** This is a header comment */

		/** This is a module javadoc */
		module test {
		}
		""",
		CodeFormatter.K_MODULE_INFO | CodeFormatter.F_INCLUDE_COMMENTS);
}
}
