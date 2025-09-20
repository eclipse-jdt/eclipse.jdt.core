/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.formatter;

import junit.framework.Test;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class FormatterMarkdownCommentsTests extends FormatterCommentsTests {

	public static Test suite() {
		return buildModelTestSuite(FormatterMarkdownCommentsTests.class);
	}

	public FormatterMarkdownCommentsTests(String name) {
		super(name);
	}

	public void testMarkdownSpacingFormat() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				class Mark {
					/// @param 		param
					///  @return 			int
					public int sample(String param) {
						return 0;
					}
				}
				""";
		String expected = """
				class Mark {
					/// @param param
					/// @return int
					public int sample(String param) {
						return 0;
					}
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownEmptyLinesBtwnDiffTags() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		this.formatterPrefs.comment_insert_empty_line_between_different_tags = true;
		String input = """
				class Mark {
					/// @param param1
					/// @return int
					public int sample(String param1) {
						return 0;
					}
				}
				""";
		String expected = """
				class Mark {
					/// @param param1
					///\s
					/// @return int
					public int sample(String param1) {
						return 0;
					}
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownUnorderedListTags() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// ## Unordered Lists
				/// - Item 1
				/// 	- Subitem  2.1
				///     	 - Subitem   2.2
				///   -  Item3
				/// -  sdd
				class Mark {
				}
				""";
		String expected = """
				/// ## Unordered Lists
				/// - Item 1
				///   - Subitem 2.1
				///     - Subitem 2.2
				///   - Item3
				/// - sdd
				class Mark {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownUnorderedListWithInvalidSpaces() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// Invalid space
				/// - Item A
				/// 	- Item B
				///     	 - Item C
				///   - Item D
				class RexTest {}
				""";
		String expected = """
				/// Invalid space
				/// - Item A
				///   - Item B
				///     - Item C
				///   - Item D
				class RexTest {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownUnorderedListDifferentTags() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// ## Unordered Lists
				/// +    Item 1
				///   * Item 2
				///   	-     Subitem 2.1
				/// +  Subitem 2.2
				/// 	 *    Item 3
				class Mark {
				}
				""";
		String expected = """
				/// ## Unordered Lists
				/// + Item 1
				///\s
				/// * Item 2
				///   -       Subitem 2.1
				///\s
				/// + Subitem 2.2
				///   * Item 3
				class Mark {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownOrderedListTags() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// ## Ordered Lists
				/// 1. First item
				/// 2. 	Second item
				///      1. Subitem 2.1
				///     2. Subitem 2.2
				///          * Sub Sub
				/// 3. Third item
				class Mark {
				}
				""";
		String expected = """
				/// ## Ordered Lists
				/// 1. First item
				/// 2. Second item
				///    1. Subitem 2.1
				///    2. Subitem 2.2
				///       * Sub Sub
				/// 3. Third item
				class Mark {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownHeadings() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// ====
				/// # Heading 1 ## Heading 2
				/// ### Heading 3 #### Heading 4
				/// ##### Heading 	5
				/// ###### 				Heading 6
				///
				/// Heading 1
				/// =========
				///
				/// Heading 2 ---------
				class Mark {
				}
				""";
		String expected = """
				/// ====
				///\s
				/// # Heading 1 ## Heading 2
				///\s
				/// ### Heading 3 #### Heading 4
				///\s
				/// ##### Heading 5
				///\s
				/// ###### Heading 6
				///
				/// Heading 1
				/// =========
				///
				/// Heading 2 ---------
				class Mark {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownHeadingWithBody() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// # heading
				/// body 	content
				class Mark {
				}
				""";
		String expected = """
				/// # heading
				/// body content
				class Mark {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownSnippetComments() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// Markdown Snippet
				/// ```
				/// public class HelloWorld2 { public static void main(String... args) {
				///         System.out.println("Hello World!"); // the traditional example
				///     }
				/// }
				/// ```
				class Test2 {
				}
				""";
		String expected = """
				/// Markdown Snippet
				/// ```
				/// public class HelloWorld2 {
				/// 	public static void main(String... args) {
				/// 		System.out.println("Hello World!"); // the traditional example
				/// 	}
				/// }
				/// ```
				class Test2 {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownMultiSnippetComments() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// ```
				/// public class HelloWorld {
				/// 	public static void main(String   args) { System.out.println("Hello World!");// the traditional example
				/// 	}
				/// }
				/// ```
				///
				/// ````````````
				/// public class HelloWorld2 {public static void main(String args) { System.out.println("ssdd World!");// the traditional example
				/// 	}
				/// }
				/// ````````````
				class Test3 {
				}
				""";
		String expected = """
				/// ```
				/// public class HelloWorld {
				/// 	public static void main(String args) {
				/// 		System.out.println("Hello World!");// the traditional example
				/// 	}
				/// }
				/// ```
				///
				/// ````````````
				/// public class HelloWorld2 {
				/// 	public static void main(String args) {
				/// 		System.out.println("ssdd World!");// the traditional example
				/// 	}
				/// }
				/// ````````````
				class Test3 {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownMultiSnippetCommentsWithoutCode() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// ```
				/// This is a random sentence
				/// inside snippet
				/// ```
				class Test20i {
				}
				""";
		String expected = """
				/// ```
				/// This is a random sentence
				/// inside snippet
				/// ```
				class Test20i {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownFencedCodeBlock() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// A markdown comment, with a codeblock.
				/// ```java
				/// void foo() {
				///   System.out.println("Hello, World!"); // 2 spaces indented
				/// }
				/// ```
				class Foo { }
				""";
		String expected = """
				/// A markdown comment, with a codeblock.
				/// ```java
				/// void foo() {
				/// 	System.out.println("Hello, World!"); // 2 spaces indented
				/// }
				/// ```
				class Foo {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownFencedCodeBlockWithTilde() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// Markdown tilde codeblock.
				/// ~~~java
				/// void foo() {
				///   System.out.println("Hello, World!");
				/// }
				/// ~~~
				class Foo { }
				""";
		String expected = """
				/// Markdown tilde codeblock.
				/// ~~~java
				/// void foo() {
				/// 	System.out.println("Hello, World!");
				/// }
				/// ~~~
				class Foo {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownFencedCodeFormatForValidOnes() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// Markdown Snippets 1
				/// ```
				/// public class HelloWorld {
				/// 	public static void main(String args) {
				/// 			System.out.println("Hello World!");
				/// 	}
				/// }
				/// ```
				///
				/// Markdown Snippets 2
				///
				/// ~~~
				/// public class HelloWorld2 {
				/// 	public static void main(String args) { ```
				/// 				System.out.println("ssdd World!");
				/// 	}
				/// }
				/// ~~~
				///
				/// Markdown Snippets 3
				///
				/// ```
				/// public class HelloWorld3 {
				/// 	public static void main(String args) {
				/// 				System.out.println("ssdd World!");
				/// 	}
				/// }
				/// ```
				class Test3 {
				}
				""";
		String expected = """
				/// Markdown Snippets 1
				/// ```
				/// public class HelloWorld {
				/// 	public static void main(String args) {
				/// 		System.out.println("Hello World!");
				/// 	}
				/// }
				/// ```
				///
				/// Markdown Snippets 2
				///
				/// ~~~
				/// public class HelloWorld2 {
				/// 	public static void main(String args) { ```
				/// 				System.out.println("ssdd World!");
				/// 	}
				/// }
				/// ~~~
				///
				/// Markdown Snippets 3
				///
				/// ```
				/// public class HelloWorld3 {
				/// 	public static void main(String args) {
				/// 		System.out.println("ssdd World!");
				/// 	}
				/// }
				/// ```
				class Test3 {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownTagLength() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// hello
				//// world
				class m22 {
				}
				""";
		String expected = """
				/// hello / world
				class m22 {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownDoNotBreakSingleElementList() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// - item A - item B - item  C
				class m22 {
				}
				""";
		String expected = """
				/// - item A - item B - item C
				class m22 {
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownDoNotBreakTableOutsideClass() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				/// | Latin | Greek |
				/// |-------|-------|
				/// | a     | alpha |
				/// | b     | beta  |
				/// | c     | gamma |
				class Main {}
				""";
		String expected = """
				/// | Latin | Greek |
				/// |-------|-------|
				/// | a     | alpha |
				/// | b     | beta  |
				/// | c     | gamma |
				class Main {
				}
				""";
		formatSource(input, expected);
	}
	public void testMarkdownDoNotBreakMultipleTableInsideClass() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
				class Main {
				  /// | Latin | Greek |
				  /// |-------|-------|
				  /// | a     | alpha |
				  ///
				  /// Hello		Eclipse
				  ///
  				  /// | Latin | Greek |
				  /// |-------|-------|
				  /// | a     | alpha |
				  public void sample(String param1) {}}
				""";
		String expected = """
				class Main {
					/// | Latin | Greek |
					/// |-------|-------|
					/// | a     | alpha |
					///
					/// Hello Eclipse
					///
					/// | Latin | Greek |
					/// |-------|-------|
					/// | a     | alpha |
					public void sample(String param1) {
					}
				}
				""";
		formatSource(input, expected);
	}

	public void testMarkdownDoNotBreakTwoListOfSameLevel() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// 1. one
					/// 1. two
					class Mark62 {}
					""";
		String expected = """
					/// 1. one
					/// 1. two
					class Mark62 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownDoNotFormatInvalidSerialNumberLists() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// something
					/// 2. one
					/// 2. two
					class Mark62 {
					}
					""";
		String expected = """
					/// something 2. one 2. two
					class Mark62 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownInvalidNestedSerialNumberedLists() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// 1. one
					/// 	2. two
					class Mark62 {
					}
					""";
		String expected = """
					/// 1. one 2. two
					class Mark62 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownInvalidLargeIndentation() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// 1. one
					///        1. two
					/// - one
					///       - two
					class Mark62 {
					}
					""";
		String expected = """
					/// 1. one 1. two
					///\s
					/// - one - two
					class Mark62 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownOrderedListWithParanthesis() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// ## Ordered Lists
					/// 1) 	First  item
					/// 2) Second item
					///      1) 	Subitem 2.1
					///     2) Subitem 2.2
					///			2) Subitem 2.2
					///         * Sub Sub
					/// 3) Third item
					class Mark62 {
					}
					""";
		String expected = """
					/// ## Ordered Lists
					/// 1)     First  item
					///\s
					/// 2) Second item
					///    1) Subitem 2.1
					///    2) Subitem 2.2 2) Subitem 2.2
					///       * Sub Sub
					/// 3) Third item
					class Mark62 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownOrderedListWithLineBreaksOnDifferentChars() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// ## Ordered Lists
					/// 1. First item
					/// 	1) Second item
					///     1. Subitem 2.1
					///     2. Subitem 2.2
					/// 3) Third item
					class Mark62 {
					}
					""";
		String expected = """
					/// ## Ordered Lists
					/// 1. First item
					///    1) Second item
					///\s
					///    1. Subitem 2.1
					///    2. Subitem 2.2
					/// 3) Third item
					class Mark62 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownListResetNestingOrderOnDIfferentTagInBetween() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// 1) Item 1
					///     1) Sub Item 1
					///    2) Sub Item 1
					/// # OR
					/// 1. Item 1
					///     1) Sub Item 1
					///      2) Sub Item 1
					class Mark62 {
					}
					""";
		String expected = """
					/// 1) Item 1
					///    1) Sub Item 1
					///    2) Sub Item 1
					///\s
					/// # OR
					/// 1. Item 1
					///    1) Sub Item 1
					///    2) Sub Item 1
					class Mark62 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownWrappedListItemIndentation() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		this.formatterPrefs.comment_line_length = 25;
		String input = """
					/// 1) Item 1 aaa bbb ccc ddd eee fff
					///     1) Sub Item 1 aaa bbb ccc ddd eee fff
					/// 1) Item 2 aaa bbb ccc ddd eee fff
					class Mark62 {
					}
					""";
		String expected = """
					/// 1) Item 1 aaa bbb ccc
					///    ddd eee fff
					///    1) Sub Item 1 aaa
					///       bbb ccc ddd eee
					///       fff
					/// 1) Item 2 aaa bbb ccc
					///    ddd eee fff
					class Mark62 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownIndentedCodeBlocks() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		this.formatterPrefs.comment_line_length = 25;
		String input = """
					/// Example: simple indented code block
					///
					///     a simple
					///       indented code block
					class Test1 { }
					""";
		String expected = """
					/// Example: simple
					/// indented code block
					///
					///     a simple
					///       indented code block
					class Test1 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownIndentedCodeBlocksMultiLineJava() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		this.formatterPrefs.comment_line_length = 25;
		String input = """
					/// Example: multi-line java
					///
					///     for (int i = 0; i < 3; i++) {
					///         System.out.println(i);
					///     }
					class Test3 { }
					""";
		String expected = """
					/// Example: multi-line
					/// java
					///
					///     for (int i = 0; i < 3; i++) {
					///         System.out.println(i);
					///     }
					class Test3 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownIndentedCodeBlocksFakeFences() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		this.formatterPrefs.comment_line_length = 25;
		String input = """
					/// Example: fake fenced inside indented
					///
					///     ```		java
					///     int x = 10;
					///     System.out.println	(x);
					///     ```
					class TestFakeFences { }
					""";
		String expected = """
					/// Example: fake fenced
					/// inside indented
					///
					///     ```		java
					///     int x = 10;
					///     System.out.println	(x);
					///     ```
					class TestFakeFences {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownUnclosedCodeFences() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// ```
					/// public class HelloWorld3 {
					/// 	public static void main(String args) {
					/// 			System.out.println("ssdd World!");
					/// 	}
					/// }
					class Mark61 {
					}
					""";
		String expected = """
					/// ```
					/// public class HelloWorld3 {
					/// 	public static void main(String args) {
					/// 		System.out.println("ssdd World!");
					/// 	}
					/// }
					class Mark61 {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownFencedCodeInList() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// 1. three    four
					/// 2. ```
					///     hello  there
					///       ```
					/// 2. one     two
					class Test {
					}
					""";
		String expected = """
					/// 1. three four
					/// 2. ```
					///     hello  there
					///    ```
					/// 2. one two
					class Test {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownFencedCodeInListWithClosingFenceTooFar() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// 1. three    four
					/// 2. ```
					///     hello  there
					///        ```
					///    2. one     two
					class Test {
					}
					""";
		String expected = """
					/// 1. three four
					/// 2. ```
					///     hello  there
					///        ```
					///    2. one     two
					class Test {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownFencedCodeInListEndedByNextListItem() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// 1. three    four
					/// 2. ```
					///     hello  there
					///        ```
					/// 2. one     two
					class Test {
					}
					""";
		String expected = """
					/// 1. three four
					/// 2. ```
					///     hello  there
					///        ```
					/// 2. one two
					class Test {
					}
					""";
		formatSource(input, expected);
	}

	public void testMarkdownWithEmptyLastLine() throws JavaModelException {
		setComplianceLevel(CompilerOptions.VERSION_23);
		String input = """
					/// aaa
					/// bbb
					///\s
					class Mark61 {
					}
					""";
		String expected = """
					/// aaa bbb
					///\s
					class Mark61 {
					}
					""";
		formatSource(input, expected);
	}

}