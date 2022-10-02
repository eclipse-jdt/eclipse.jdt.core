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

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions.Alignment;

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
 * @bug 27079: [formatter] Tags for disabling/enabling code formatter (feature)
 * @test Ensure that the formatter does not format code between specific javadoc comments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=27079"
 */
public void testBug027079a() throws JavaModelException {
	String source =
		"public class X01 {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"	/* disable-formatter */\n" +
		"	void foo() {\n" +
		"		// unformatted comment\n" +
		"	}\n" +
		"\n" +
		"	/* enable-formatter */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079a1() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"public class X01 {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079a2() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"public class X01 {\n" +
		"\n" +
		"/** disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/** enable-formatter */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"/** disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/** enable-formatter */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079a3() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"public class X01 {\n" +
		"\n" +
		"// disable-formatter\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"// enable-formatter\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"// disable-formatter\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"// enable-formatter\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079a4() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"public class X01 {\n" +
		"\n" +
		"// disable-formatter\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"// enable-formatter \n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment  	  \n" +
		"				/* disable-formatter *//*      unformatted		comment  	  *//* enable-formatter */\n" +
		"}\n" + 		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"// disable-formatter\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"// enable-formatter \n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"		/* disable-formatter *//*      unformatted		comment  	  *//* enable-formatter */\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079b() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"public class X02 {\n" +
		"void foo() {\n" +
		"/* disable-formatter */\n" +
		"				/*       unformatted		comment  	  */\n" +
		"	String test1= \"this\"+\n" +
		"					\"is\"+\n" +
		"			\"a specific\"+\n" +
		"		\"line wrapping \";\n" +
		"\n" +
		"/* enable-formatter */\n" +
		"				/*       formatted		comment  	  */\n" +
		"	String test2= \"this\"+\n" +
		"					\"is\"+\n" +
		"			\"a specific\"+\n" +
		"		\"line wrapping \";\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	void foo() {\n" +
		"/* disable-formatter */\n" +
		"				/*       unformatted		comment  	  */\n" +
		"	String test1= \"this\"+\n" +
		"					\"is\"+\n" +
		"			\"a specific\"+\n" +
		"		\"line wrapping \";\n" +
		"\n" +
		"/* enable-formatter */\n" +
		"		/* formatted comment */\n" +
		"		String test2 = \"this\" + \"is\" + \"a specific\" + \"line wrapping \";\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079c() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"public class X03 {\n" +
		"void foo() {\n" +
		"/* disable-formatter */\n" +
		"	bar(\n" +
		"				/**       unformatted		comment  	  */\n" +
		"				\"this\"  ,\n" +
		"					\"is\",\n" +
		"			\"a specific\",\n" +
		"		\"line wrapping \"\n" +
		"	);\n" +
		"\n" +
		"/* enable-formatter */\n" +
		"	bar(\n" +
		"				/**       formatted		comment  	  */\n" +
		"				\"this\"  ,\n" +
		"					\"is\",\n" +
		"			\"a specific\",\n" +
		"		\"line wrapping \"\n" +
		"	);\n" +
		"}\n" +
		"void bar(String... str) {}\n" +
		"}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	void foo() {\n" +
		"/* disable-formatter */\n" +
		"	bar(\n" +
		"				/**       unformatted		comment  	  */\n" +
		"				\"this\"  ,\n" +
		"					\"is\",\n" +
		"			\"a specific\",\n" +
		"		\"line wrapping \"\n" +
		"	);\n" +
		"\n" +
		"/* enable-formatter */\n" +
		"		bar(\n" +
		"				/** formatted comment */\n" +
		"				\"this\", \"is\", \"a specific\", \"line wrapping \");\n" +
		"	}\n" +
		"\n" +
		"	void bar(String... str) {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079c2() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"public class X03b {\n" +
		"void foo() {\n" +
		"	bar(\n" +
		"// disable-formatter\n" +
		"				/**       unformatted		comment  	  */\n" +
		"				\"this\"  ,\n" +
		"					\"is\",\n" +
		"			\"a specific\",\n" +
		"		\"line wrapping \"\n" +
		"// enable-formatter\n" +
		"	);\n" +
		"	bar(\n" +
		"				/**       formatted		comment  	  */\n" +
		"				\"this\"  ,\n" +
		"					\"is\",\n" +
		"			\"a specific\",\n" +
		"		\"line wrapping \"\n" +
		"	);\n" +
		"}\n" +
		"void bar(String... str) {}\n" +
		"}\n";
	formatSource(source,
		"public class X03b {\n" +
		"	void foo() {\n" +
		"		bar(\n" +
		"// disable-formatter\n" +
		"				/**       unformatted		comment  	  */\n" +
		"				\"this\"  ,\n" +
		"					\"is\",\n" +
		"			\"a specific\",\n" +
		"		\"line wrapping \"\n" +
		"// enable-formatter\n" +
		"		);\n" +
		"		bar(\n" +
		"				/** formatted comment */\n" +
		"				\"this\", \"is\", \"a specific\", \"line wrapping \");\n" +
		"	}\n" +
		"\n" +
		"	void bar(String... str) {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079d() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"public class X04 {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment  	  \n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X04 {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n",
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
		"public class X04b {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment  	  \n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X04b {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n",
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
		"public class X04c {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment  	  \n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X04c {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n",
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
		"public class X04d {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment  	  \n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X04d {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment  	  \n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n",
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
		"public class X05 {\n" +
		"\n" +
		"/* format: off */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/* format: on */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X05 {\n" +
		"\n" +
		"/* format: off */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/* format: on */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079f() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "format: off".toCharArray();
	this.formatterPrefs.enabling_tag = "format: on".toCharArray();
	String source =
		"public class X06 {\n" +
		"\n" +
		"// format: off\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"// format: on\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X06 {\n" +
		"\n" +
		"// format: off\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"// format: on\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079f2() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "format: off".toCharArray();
	this.formatterPrefs.enabling_tag = "format: on".toCharArray();
	String source =
		"public class X06b {\n" +
		"\n" +
		"/** format: off */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/** format: on */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X06b {\n" +
		"\n" +
		"/** format: off */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/** format: on */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079f3() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "    format:  	  off    ".toCharArray();
	this.formatterPrefs.enabling_tag = "	format:	  	on	".toCharArray();
	String source =
		"public class X06c {\n" +
		"\n" +
		"/*    format:  	  off    */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"// 	format:	  	on	\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X06c {\n" +
		"\n" +
		"/*    format:  	  off    */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"// 	format:	  	on	\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug027079f4() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "    format:  	  off    ".toCharArray();
	this.formatterPrefs.enabling_tag = "	format:	  	on	".toCharArray();
	String source =
		"public class X06d {\n" +
		"\n" +
		"/* format: off */\n" +
		"void     foo(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"/* format: on */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X06d {\n" +
		"\n" +
		"	/* format: off */\n" +
		"	void foo() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"\n" +
		"	/* format: on */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 59891: [formatter] the code formatter doesn't respect my new lines
 * @test Ensure that the formatter keep line breaks wrapping set by users in the code
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=59891"
 */
public void testBug059891_01() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	String source =
		"public class X01 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4),\n" +
		"				bar(5, 6, 7, 8));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_01b() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"public class X01 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4),\n" +
		"			bar(5, 6, 7, 8));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_02() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	String source =
		"public class X02 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), bar(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4, 5, 6, 7, 8,\n" +
		"				9, 10),\n" +
		"				bar(11, 12, 13, 14, 15,\n" +
		"						16, 17, 18, 19,\n" +
		"						20));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_02b() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"public class X02 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), bar(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4, 5, 6, 7, 8,\n" +
		"				9, 10),\n" +
		"			bar(11, 12, 13, 14, 15, 16,\n" +
		"				17, 18, 19, 20));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_03() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	String source =
		"public class X03 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8), bar(9, 10, 11, 12));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4),\n" +
		"				bar(5, 6, 7, 8),\n" +
		"				bar(9, 10, 11, 12));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_03b() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
			DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	String source =
		"public class X03 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8), bar(9, 10, 11, 12));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4),\n" +
		"			bar(5, 6, 7, 8),\n" +
		"			bar(9, 10, 11, 12));\n" +
		"	}\n" +
		"}\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146175
public void testBug059891_146175() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class FormatterDemo {\n" +
		"\n" +
		"    public void fooBar() {\n" +
		"        SomeOtherClass instanceOfOtherClass = new SomeOtherClass();\n" +
		"\n" +
		"        /* The following statement demonstrates the formatter issue */\n" +
		"        SomeOtherClass.someMethodInInnerClass(\n" +
		"            instanceOfOtherClass.anotherMethod(\"Value of paramter 1\"),\n" +
		"            instanceOfOtherClass.anotherMethod(\"Value of paramter 2\"));\n" +
		"\n" +
		"    }\n" +
		"\n" +
		"    private static class SomeOtherClass {\n" +
		"        public static void someMethodInInnerClass(\n" +
		"            String param1,\n" +
		"            String param2) {\n" +
		"        }\n" +
		"        public String anotherMethod(String par) {\n" +
		"            return par;\n" +
		"        }\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class FormatterDemo {\n" +
		"\n" +
		"	public void fooBar() {\n" +
		"		SomeOtherClass instanceOfOtherClass = new SomeOtherClass();\n" +
		"\n" +
		"		/* The following statement demonstrates the formatter issue */\n" +
		"		SomeOtherClass.someMethodInInnerClass(\n" +
		"				instanceOfOtherClass.anotherMethod(\"Value of paramter 1\"),\n" +
		"				instanceOfOtherClass.anotherMethod(\"Value of paramter 2\"));\n" +
		"\n" +
		"	}\n" +
		"\n" +
		"	private static class SomeOtherClass {\n" +
		"		public static void someMethodInInnerClass(String param1,\n" +
		"				String param2) {\n" +
		"		}\n" +
		"\n" +
		"		public String anotherMethod(String par) {\n" +
		"			return par;\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
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
		"public class Test {\n" +
		"    int someLongMethodName(int foo,  boolean bar, String yetAnotherArg) {\n" +
		"        return 0;\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	int someLongMethodName(	int foo,\n" +
		"							boolean bar,\n" +
		"							String yetAnotherArg) {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n"
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
		"public class X01 {\n" +
		"    void foo() {\n" +
		"           someIdentifier(someArg).someMethodName().someMethodName(foo, bar).otherMethod(arg0, arg1);\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"    void foo() {\n" +
		"        someIdentifier(someArg).someMethodName()\n" +
		"                               .someMethodName(foo,\n" +
		"                                       bar)\n" +
		"                               .otherMethod(arg0,\n" +
		"                                       arg1);\n" +
		"    }\n" +
		"}\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203588
public void testBug059891_203588() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class Test {\n" +
		"public void a()\n" +
		"{\n" +
		"  if(true)\n" +
		"  {\n" +
		"    allocation.add(idx_ta + 1, Double.valueOf(allocation.get(idx_ta).doubleValue() + q));\n" +
		"  }\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	public void a() {\n" +
		"		if (true) {\n" +
		"			allocation.add(idx_ta + 1,\n" +
		"					Double.valueOf(allocation.get(idx_ta).doubleValue() + q));\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// wksp1
public void testBug059891_wksp1_01() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X01 {\n" +
		"	private void reportError(String name) throws ParseError {\n" +
		"		throw new ParseError(MessageFormat.format(AntDTDSchemaMessages.getString(\"NfmParser.Ambiguous\"), new String[]{name})); //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	private void reportError(String name) throws ParseError {\n" +
		"		throw new ParseError(MessageFormat.format(\n" +
		"				AntDTDSchemaMessages.getString(\"NfmParser.Ambiguous\"), //$NON-NLS-1$\n" +
		"				new String[] { name }));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_02() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X02 {\n" +
		"	private void parseBuildFile(Project project) {\n" +
		"		if (!buildFile.exists()) {\n" +
		"			throw new BuildException(MessageFormat.format(InternalAntMessages.getString(\"InternalAntRunner.Buildfile__{0}_does_not_exist_!_1\"), //$NON-NLS-1$\n" +
		"						 new String[]{buildFile.getAbsolutePath()}));\n" +
		"		}\n" +
		"		if (!buildFile.isFile()) {\n" +
		"			throw new BuildException(MessageFormat.format(InternalAntMessages.getString(\"InternalAntRunner.Buildfile__{0}_is_not_a_file_1\"), //$NON-NLS-1$\n" +
		"							new String[]{buildFile.getAbsolutePath()}));\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	private void parseBuildFile(Project project) {\n" +
		"		if (!buildFile.exists()) {\n" +
		"			throw new BuildException(MessageFormat.format(\n" +
		"					InternalAntMessages.getString(\n" +
		"							\"InternalAntRunner.Buildfile__{0}_does_not_exist_!_1\"), //$NON-NLS-1$\n" +
		"					new String[] { buildFile.getAbsolutePath() }));\n" +
		"		}\n" +
		"		if (!buildFile.isFile()) {\n" +
		"			throw new BuildException(MessageFormat.format(\n" +
		"					InternalAntMessages.getString(\n" +
		"							\"InternalAntRunner.Buildfile__{0}_is_not_a_file_1\"), //$NON-NLS-1$\n" +
		"					new String[] { buildFile.getAbsolutePath() }));\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_03() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X03 {\n" +
		"\n" +
		"	protected void foo() {\n" +
		"		printTargets(project, subNames, null, InternalAntMessages.getString(\"InternalAntRunner.Subtargets__5\"), 0); //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X03 {\n" +
		"\n" +
		"	protected void foo() {\n" +
		"		printTargets(project, subNames, null, InternalAntMessages\n" +
		"				.getString(\"InternalAntRunner.Subtargets__5\"), 0); //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_04() throws JavaModelException {
	String source =
		"public class X04 {\n" +
		"	void foo() {\n" +
		"		if (AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {\n" +
		"			synchronizeOutlinePage(node, true);\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X04 {\n" +
		"	void foo() {\n" +
		"		if (AntUIPlugin.getDefault().getPreferenceStore()\n" +
		"				.getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {\n" +
		"			synchronizeOutlinePage(node, true);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_05() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X05 {\n" +
		"void foo() {\n" +
		"		if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {\n" +
		"		}\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X05 {\n" +
		"	void foo() {\n" +
		"		if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(\n" +
		"				AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_06() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X06 {\n" +
		"	public void launch() {\n" +
		"		try {\n" +
		"			if ((javaProject == null) || !javaProject.exists()) {\n" +
		"				abort(PDEPlugin________.getResourceString(\"JUnitLaunchConfig_____\"), null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);\n" +
		"			}\n" +
		"		} catch (CoreException e) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X06 {\n" +
		"	public void launch() {\n" +
		"		try {\n" +
		"			if ((javaProject == null) || !javaProject.exists()) {\n" +
		"				abort(PDEPlugin________\n" +
		"						.getResourceString(\"JUnitLaunchConfig_____\"), null,\n" +
		"						IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);\n" +
		"			}\n" +
		"		} catch (CoreException e) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_07() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X07 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			configureAntObject(result, element, task, task.getTaskName(), InternalCoreAntMessages.getString(\"AntCorePreferences.No_library_for_task\")); //$NON-NLS-1$\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X07 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			configureAntObject(result, element, task, task.getTaskName(),\n" +
		"					InternalCoreAntMessages.getString(\n" +
		"							\"AntCorePreferences.No_library_for_task\")); //$NON-NLS-1$\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_08() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X08 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			IStatus status= new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_RUNNING_BUILD, MessageFormat.format(InternalCoreAntMessages.getString(\"AntRunner.Already_in_progess\"), new String[]{buildFileLocation}), null); //$NON-NLS-1$\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X08 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE,\n" +
		"					AntCorePlugin.ERROR_RUNNING_BUILD,\n" +
		"					MessageFormat.format(\n" +
		"							InternalCoreAntMessages\n" +
		"									.getString(\"AntRunner.Already_in_progess\"), //$NON-NLS-1$\n" +
		"							new String[] { buildFileLocation }),\n" +
		"					null);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_09() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X09 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			String secondFileName = secondDirectoryAbsolutePath + File.separator + currentFile.substring(firstDirectoryAbsolutePath.length() + 1);\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X09 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			String secondFileName = secondDirectoryAbsolutePath + File.separator\n" +
		"					+ currentFile\n" +
		"							.substring(firstDirectoryAbsolutePath.length() + 1);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_10() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X10 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			if (true) {\n" +
		"				throw new BuildException(InternalAntMessages.getString(\"InternalAntRunner.Could_not_load_the_version_information._10\")); //$NON-NLS-1$\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X10 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			if (true) {\n" +
		"				throw new BuildException(InternalAntMessages.getString(\n" +
		"						\"InternalAntRunner.Could_not_load_the_version_information._10\")); //$NON-NLS-1$\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_11() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X11 {\n" +
		"	private void antFileNotFound() {\n" +
		"		reportError(AntLaunchConfigurationMessages.getString(\"AntLaunchShortcut.Unable\"), null); //$NON-NLS-1$	\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X11 {\n" +
		"	private void antFileNotFound() {\n" +
		"		reportError(AntLaunchConfigurationMessages\n" +
		"				.getString(\"AntLaunchShortcut.Unable\"), null); //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug059891_wksp1_12() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class X12 {\n" +
		"	void foo() {\n" +
		"        if (this.fTests.size() == 0) {\n" +
		"            this.addTest(TestSuite\n" +
		"                    .warning(\"No tests found in \" + theClass.getName())); //$NON-NLS-1$\n" +
		"        }\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X12 {\n" +
		"	void foo() {\n" +
		"		if (this.fTests.size() == 0) {\n" +
		"			this.addTest(TestSuite\n" +
		"					.warning(\"No tests found in \" + theClass.getName())); //$NON-NLS-1$\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 198074: [formatter] the code formatter doesn't respect my new lines
 * @test Ensure that the formatter keep line breaks wrapping set by users in the code
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=198074"
 */
public void testBug198074() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"String x = \"select x \"\n" +
		"         + \"from y \"\n" +
		"         + \"where z=a\";\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		String x = \"select x \"\n" +
		"				+ \"from y \"\n" +
		"				+ \"where z=a\";\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug198074b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"String x = \"select x \"\n" +
		"         + \"from y \"\n" +
		"         + \"where z=a\";\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    void foo() {\n" +
		"        String x = \"select x \"\n" +
		"                + \"from y \"\n" +
		"                + \"where z=a\";\n" +
		"    }\n" +
		"}\n"
	);
}
// another test case put in bug's comment 1
public void testBug198074_c1() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	String foo(boolean enabled) {\n" +
		"if (enabled)\n" +
		"{\n" +
		"   // we need x\n" +
		"   // we need a select\n" +
		"   return \"select x \"\n" +
		"   + \"from X\";}\n" +
		"	return null;}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	String foo(boolean enabled) {\n" +
		"		if (enabled) {\n" +
		"			// we need x\n" +
		"			// we need a select\n" +
		"			return \"select x \"\n" +
		"					+ \"from X\";\n" +
		"		}\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug198074_c1b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"	String foo(boolean enabled) {\n" +
		"if (enabled)\n" +
		"{\n" +
		"   // we need x\n" +
		"   // we need a select\n" +
		"   return \"select x \"\n" +
		"        + \"from X\";}\n" +
		"	return null;}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    String foo(boolean enabled) {\n" +
		"        if (enabled) {\n" +
		"            // we need x\n" +
		"            // we need a select\n" +
		"            return \"select x \"\n" +
		"                    + \"from X\";\n" +
		"        }\n" +
		"        return null;\n" +
		"    }\n" +
		"}\n"
	);
}
// another test case put in bug's comment 3
public void testBug198074_c3() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"public String toString() {\n" +
		"        return \"YAD01: \"\n" +
		"        + \" nommbr=\'\"+getName()+\"\'\"\n" +
		"        + \" nomgrp=\'\"+getService().getArgtbl()+\"\'\"\n" +
		"        + \" typmbr=\'\"+getMemberType().getArgument()+\"\'\"\n" +
		"        + \" srcpat=\'\"+getPhysicalPath()+\"\'\"\n" +
		"        + \" nommdl=\'\"+getModel()+\"\'\"\n" +
		"        ;\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	public String toString() {\n" +
		"		return \"YAD01: \"\n" +
		"				+ \" nommbr=\'\" + getName() + \"\'\"\n" +
		"				+ \" nomgrp=\'\" + getService().getArgtbl() + \"\'\"\n" +
		"				+ \" typmbr=\'\" + getMemberType().getArgument() + \"\'\"\n" +
		"				+ \" srcpat=\'\" + getPhysicalPath() + \"\'\"\n" +
		"				+ \" nommdl=\'\" + getModel() + \"\'\";\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug198074_c3b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"public String toString() {\n" +
		"        return \"YAD01: \"\n" +
		"                + \" nommbr=\'\"+getName()+\"\'\"\n" +
		"                + \" nomgrp=\'\"+getService().getArgtbl()+\"\'\"\n" +
		"                + \" typmbr=\'\"+getMemberType().getArgument()+\"\'\"\n" +
		"                + \" srcpat=\'\"+getPhysicalPath()+\"\'\"\n" +
		"                + \" nommdl=\'\"+getModel()+\"\'\"\n" +
		"        ;\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    public String toString() {\n" +
		"        return \"YAD01: \"\n" +
		"                + \" nommbr=\'\" + getName() + \"\'\"\n" +
		"                + \" nomgrp=\'\" + getService().getArgtbl() + \"\'\"\n" +
		"                + \" typmbr=\'\" + getMemberType().getArgument() + \"\'\"\n" +
		"                + \" srcpat=\'\" + getPhysicalPath() + \"\'\"\n" +
		"                + \" nommdl=\'\" + getModel() + \"\'\";\n" +
		"    }\n" +
		"}\n"
	);
}
public void testBug198074_comments() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"String x = \"select x \"\n" +
		"         + \"from y \"\n" +
		"         + \"where z=a\";\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		String x = \"select x \" + \"from y \" + \"where z=a\";\n" +
		"	}\n" +
		"}\n"
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=201022
// see also bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=287462
public void testBug198074_dup201022() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"    String sQuery =\n" +
		"        \"select * \" +\n" +
		"        \"from person p, address a \" +\n" +
		"        \"where p.person_id = a.person_id \" +\n" +
		"        \"and p.person_id = ?\";\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		String sQuery = \"select * \" +\n" +
		"				\"from person p, address a \" +\n" +
		"				\"where p.person_id = a.person_id \" +\n" +
		"				\"and p.person_id = ?\";\n" +
		"	}\n" +
		"}\n"
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=213700
public void testBug198074_dup213700() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		int a=0, b=0, c=0, d=0, e=0, f=0, g=0, h=0, i=0;\n" +
		"if( (a == b && b == c) &&\n" +
		"    (d == e) &&\n" +
		"    (f == g && h == i) \n" +
		"    ){\n" +
		"}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0, i = 0;\n" +
		"		if ((a == b && b == c) &&\n" +
		"				(d == e) &&\n" +
		"				(f == g && h == i)) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 199265: [formatter] 3.3 Code Formatter mis-places commented-out import statements
 * @test Ensure that the formatter keep commented import declarations on their lines
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=199265"
 */
public void testBug199265a() throws JavaModelException {
	String source =
		"import java.util.List;\n" +
		"//import java.util.HashMap;\n" +
		"import java.util.Set;\n" +
		"\n" +
		"public class X01 {\n" +
		"}\n";
	formatSource(source);
}
public void testBug199265b() throws JavaModelException {
	String source =
		"import java.util.List;\n" +
		"import java.util.Set;\n" +
		"//import java.util.HashMap;\n" +
		"\n" +
		"public class X02 {\n" +
		"}\n";
	formatSource(source,
		"import java.util.List;\n" +
		"import java.util.Set;\n" +
		"//import java.util.HashMap;\n" +
		"\n" +
		"public class X02 {\n" +
		"}\n"
	);
}
public void testBug199265c1() throws JavaModelException {
	String source =
		"import java.util.List;\n" +
		"//            CU         snippet\n" +
		"public class X03 {\n" +
		"	List field;\n" +
		"}\n";
	formatSource(source,
		"import java.util.List;\n" +
		"\n" +
		"//            CU         snippet\n" +
		"public class X03 {\n" +
		"	List field;\n" +
		"}\n"
	);
}
public void testBug199265c2() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.comment_format_header = true;
	String source =
		"import java.util.List;\n" +
		"//            CU         snippet\n" +
		"public class X03 {\n" +
		"	List field;\n" +
		"}\n";
	formatSource(source,
		"import java.util.List;\n" +
		"\n" +
		"// CU snippet\n" +
		"public class X03 {\n" +
		"	List field;\n" +
		"}\n"
	);
}
public void testBug199265c3() throws JavaModelException {
	String source =
		"import java.util.List;\n" +
		"\n" +
		"// line comment\n" +
		"public class X03 {\n" +
		"	List field;\n" +
		"}\n";
	formatSource(source);
}
public void testBug199265d1() throws JavaModelException {
	String source =
		"import java.util.Set; // trailing comment\n" +
		"// line comment\n" +
		"import java.util.Map; // trailing comment\n" +
		"// line comment\n" +
		"public class X04 {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"import java.util.Set; // trailing comment\n" +
		"// line comment\n" +
		"import java.util.Map; // trailing comment\n" +
		"// line comment\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"}\n"
	);
}
public void testBug199265d2() throws JavaModelException {
	String source =
		"import java.util.Set; // trailing comment\n" +
		"// line comment\n" +
		"import java.util.Map; // trailing comment\n" +
		"// line comment\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"}\n";
	formatSource(source);
}
public void testBug199265d3() throws JavaModelException {
	String source =
		"import java.util.Set; // trailing comment\n" +
		"	// line comment\n" +
		"import java.util.Map; // trailing comment\n" +
		"	// line comment\n" +
		"public class X04 {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"import java.util.Set; // trailing comment\n" +
		"// line comment\n" +
		"import java.util.Map; // trailing comment\n" +
		"// line comment\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"}\n"
	);
}
public void testBug199265_wksp1a() throws JavaModelException {
	String source =
		"package wksp1;\n" +
		"\n" +
		"import java.util.*;\n" +
		"import java.util.List; // line comment\n" +
		"\n" +
		"/**\n" +
		" * Javadoc comment\n" +
		" */\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n";
	formatSource(source);
}
public void testBug199265_wksp1b() throws JavaModelException {
	String source =
		"package wksp1;\n" +
		"\n" +
		"import java.util.Map;\n" +
		"\n" +
		"//==========================\n" +
		"// Line comment\n" +
		"//==========================\n" +
		"\n" +
		"/**\n" +
		" * Javadoc comment\n" +
		" */\n" +
		"public class X02 {\n" +
		"\n" +
		"}\n";
	formatSource(source);
}
public void testBug199265_wksp2a() throws JavaModelException {
	String source =
		"package wksp2;\n" +
		"\n" +
		"import java.util.Map;\n" +
		"\n" +
		"//#if defined(TEST)\n" +
		"import java.util.Vector;\n" +
		"//#else\n" +
		"//##import java.util.Set;\n" +
		"//#endif\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n";
	formatSource(source);
}
public void testBug199265_wksp3a() throws JavaModelException {
	String source =
		"package wksp3;\n" +
		"\n" +
		"import java.util.Set;	// comment 1\n" +
		"import java.util.Map;	// comment 2\n" +
		"import java.util.List;	// comment 3\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp3;\n" +
		"\n" +
		"import java.util.Set; // comment 1\n" +
		"import java.util.Map; // comment 2\n" +
		"import java.util.List; // comment 3\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n"
	);
}

/**
 * @bug 208541: [formatter] Formatter does not format whole region/selection
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=208541"
 */
public void testBug208541() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class MyTest {\n" +
		"\n" +
		"    public void testname() throws Exception {\n" +
		"        int i = 5, j = 6, k = 7;\n" +
		"        if (new String().length() != 0 \n" +
		"              &&  (i < j && j < k)) {\n" +
		"\n" +
		"        }\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class MyTest {\n" +
		"\n" +
		"	public void testname() throws Exception {\n" +
		"		int i = 5, j = 6, k = 7;\n" +
		"		if (new String().length() != 0\n" +
		"				&& (i < j && j < k)) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 203588: [formatter] Qualified invocation + binary expressions excessive wrap
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=203588"
 */
public void testBug203588() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class Test {\n" +
		"void foo() {\n" +
		"	while (true) {\n" +
		"		if (patternChar\n" +
		"			!= (isCaseSensitive\n" +
		"				? name[iName]\n" +
		"				: Character.toLowerCase(name[iName]))\n" +
		"			&& patternChar != \'?\') {\n" +
		"			return;\n" +
		"		}\n" +
		"	}\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	void foo() {\n" +
		"		while (true) {\n" +
		"			if (patternChar != (isCaseSensitive ? name[iName]\n" +
		"					: Character.toLowerCase(name[iName]))\n" +
		"					&& patternChar != \'?\') {\n" +
		"				return;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 252556: [formatter] Spaces removed before formatted region of a compilation unit.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=252556"
 */
public void testBug252556() {
	String source =
		"package a;\n" +
		"\n" +
		"public class Test {\n" +
		"\n" +
		"	private int field;\n" +
		"	\n" +
		"	[#/**\n" +
		"	 * fds \n" +
		"	 */#]\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package a;\n" +
		"\n" +
		"public class Test {\n" +
		"\n" +
		"	private int field;\n" +
		"	\n" +
		"	/**\n" +
		"	 * fds\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556a() {
	String source =
		"public class Test {\n" +
		"\n" +
		"int foo() {[#\n" +
		"return 0;\n" +
		"#]}\n" +
		"void bar(){}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"int foo() {\n" +
		"	return 0;\n" +
		"}\n" +
		"void bar(){}\n" +
		"}\n"
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556b() {
	String source =
		"public class Test {\n" +
		"\n" +
		"int [#foo() {\n" +
		"return 0;\n" +
		"#]}\n" +
		"void bar(){}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"int foo() {\n" +
		"	return 0;\n" +
		"}\n" +
		"void bar(){}\n" +
		"}\n"
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556c() {
	String source =
		"public class Test {\n" +
		"\n" +
		"[#int foo() {\n" +
		"return 0;\n" +
		"#]}\n" +
		"void bar(){}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"void bar(){}\n" +
		"}\n"
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556d() {
	String source =
		"public class Test {\n" +
		"\n" +
		"[#int foo() {\n" +
		"return 0;\n" +
		"}#]\n" +
		"void bar(){}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"void bar(){}\n" +
		"}\n"
	);
}
// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95340
public void testBug252556e() {
	String source =
		"public class Test {\n" +
		"\n" +
		"[#int foo() {\n" +
		"return 0;\n" +
		"}\n" +
		"#]void bar(){}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"\n" +
		"	void bar(){}\n" +
		"}\n"
	);
}
// see org.eclipse.jdt.ui.tests.core.CodeFormatterUtilTest.testFormatSubstring()
public void testBug252556f() {
	String source =
		"package test1;\n" +
		"\n" +
		"import java.util.Vector;\n" +
		"\n" +
		"public class A {\n" +
		"    public void foo() {\n" +
		"    [#Runnable runnable= new Runnable() {};#]\n" +
		"    runnable.toString();\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"import java.util.Vector;\n" +
		"\n" +
		"public class A {\n" +
		"    public void foo() {\n" +
		"    	Runnable runnable = new Runnable() {\n" +
		"		};\n" +
		"    runnable.toString();\n" +
		"    }\n" +
		"}\n"
	);
}
// Adding a test case impacted by the fix for bug 252556 got from massive tests
public void testBug252556_wksp3a() {
	String source =
		"package wksp3;\n" +
		"\n" +
		"/**\n" +
		" * <pre>import java.net.*;\n" +
		" * import org.xml.sax.*;\n" +
		" * </pre>\n" +
		" */\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp3;\n" +
		"\n" +
		"/**\n" +
		" * <pre>\n" +
		" * import java.net.*;\n" +
		" * import org.xml.sax.*;\n" +
		" * </pre>\n" +
		" */\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n"
	);
}

/**
 * @bug 281655: [formatter] "Never join lines" does not work for annotations.
 * @test Verify that "Never join lines" now works for annotations and also that
 * 		element-value pairs are well wrapped using the new formatter option
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=281655"
 */
public void testBug281655() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_SPLIT;
	String source =
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\", \n" +
		"        activationConfig = { \n" +
		"            @ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"propertyValue = \"0/10 * * * * ?\") \n" +
		"        })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\",\n" +
		"		activationConfig = {\n" +
		"				@ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"						propertyValue = \"0/10 * * * * ?\")\n" +
		"		})\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug281655a() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NO_ALIGNMENT;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_NO_ALIGNMENT;
	String source =
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\", \n" +
		"        activationConfig = { \n" +
		"            @ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"propertyValue = \"0/10 * * * * ?\") \n" +
		"        })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\", activationConfig = { @ActivationConfigProperty(propertyName = \"cronTrigger\", propertyValue = \"0/10 * * * * ?\") })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug281655b() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\", \n" +
		"        activationConfig = { \n" +
		"            @ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"propertyValue = \"0/10 * * * * ?\") \n" +
		"        })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\",\n" +
		"		activationConfig = {\n" +
		"				@ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"						propertyValue = \"0/10 * * * * ?\") })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug281655c() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	setPageWidth80();
	String source =
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\", \n" +
		"        activationConfig = { \n" +
		"            @ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"propertyValue = \"0/10 * * * * ?\") \n" +
		"        })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MessageDriven(\n" +
		"		mappedName = \"filiality/SchedulerMQService\",\n" +
		"		activationConfig = { @ActivationConfigProperty(\n" +
		"				propertyName = \"cronTrigger\",\n" +
		"				propertyValue = \"0/10 * * * * ?\") })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug281655d() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\", \n" +
		"        activationConfig = { \n" +
		"            @ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"propertyValue = \"0/10 * * * * ?\") \n" +
		"        })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MessageDriven(\n" +
		"		mappedName = \"filiality/SchedulerMQService\",\n" +
		"		activationConfig = { @ActivationConfigProperty(\n" +
		"				propertyName = \"cronTrigger\",\n" +
		"				propertyValue = \"0/10 * * * * ?\") })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug281655e() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NEXT_SHIFTED_SPLIT;
	setPageWidth80();
	String source =
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\", \n" +
		"        activationConfig = { \n" +
		"            @ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"propertyValue = \"0/10 * * * * ?\") \n" +
		"        })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MessageDriven(\n" +
		"		mappedName = \"filiality/SchedulerMQService\",\n" +
		"			activationConfig = { @ActivationConfigProperty(\n" +
		"					propertyName = \"cronTrigger\",\n" +
		"						propertyValue = \"0/10 * * * * ?\") })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug281655f() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NEXT_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\", \n" +
		"        activationConfig = { \n" +
		"            @ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"propertyValue = \"0/10 * * * * ?\") \n" +
		"        })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MessageDriven(mappedName = \"filiality/SchedulerMQService\",\n" +
		"		activationConfig = {\n" +
		"				@ActivationConfigProperty(propertyName = \"cronTrigger\",\n" +
		"						propertyValue = \"0/10 * * * * ?\") })\n" +
		"@RunAs(\"admin\")\n" +
		"@ResourceAdapter(\"quartz-ra.rar\")\n" +
		"@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)\n" +
		"public class X {\n" +
		"}\n"
	);
}

/**
 * @bug 282030: [formatter] Java annotation formatting
 * @test Verify that element-value pairs are well wrapped using the new formatter option
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=282030"
 */
public void testBug282030() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"@DeclareParents(value =\n" +
		"\"com.apress.springrecipes.calculator.ArithmeticCalculatorImpl\", defaultImpl =\n" +
		"MaxCalculatorImpl.class) \n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@DeclareParents(\n" +
		"		value = \"com.apress.springrecipes.calculator.ArithmeticCalculatorImpl\",\n" +
		"		defaultImpl = MaxCalculatorImpl.class)\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug282030a() throws JavaModelException {
	String source =
		"@MyAnnot(value1 = \"this is an example\", value2 = \"of an annotation\", value3 = \"with several arguments\", value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot(value1 = \"this is an example\", value2 = \"of an annotation\", value3 = \"with several arguments\", value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n"
	);
}
public void testBug282030b() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"@MyAnnot(value1 = \"this is an example\", value2 = \"of an annotation\", value3 = \"with several arguments\", value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot(value1 = \"this is an example\", value2 = \"of an annotation\",\n" +
		"		value3 = \"with several arguments\",\n" +
		"		value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n"
	);
}
public void testBug282030c() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	setPageWidth80();
	String source =
		"@MyAnnot(value1 = \"this is an example\", value2 = \"of an annotation\", value3 = \"with several arguments\", value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot(\n" +
		"		value1 = \"this is an example\", value2 = \"of an annotation\",\n" +
		"		value3 = \"with several arguments\",\n" +
		"		value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n"
	);
}
public void testBug282030d() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT;
	String source =
		"@MyAnnot(value1 = \"this is an example\", value2 = \"of an annotation\", value3 = \"with several arguments\", value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot(\n" +
		"		value1 = \"this is an example\",\n" +
		"		value2 = \"of an annotation\",\n" +
		"		value3 = \"with several arguments\",\n" +
		"		value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n"
	);
}
public void testBug282030e() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NEXT_SHIFTED_SPLIT;
	String source =
		"@MyAnnot(value1 = \"this is an example\", value2 = \"of an annotation\", value3 = \"with several arguments\", value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot(\n" +
		"		value1 = \"this is an example\",\n" +
		"			value2 = \"of an annotation\",\n" +
		"			value3 = \"with several arguments\",\n" +
		"			value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n"
	);
}
public void testBug282030f() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_NEXT_PER_LINE_SPLIT;
	String source =
		"@MyAnnot(value1 = \"this is an example\", value2 = \"of an annotation\", value3 = \"with several arguments\", value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot(value1 = \"this is an example\",\n" +
		"		value2 = \"of an annotation\",\n" +
		"		value3 = \"with several arguments\",\n" +
		"		value4 = \"which may need to be wrapped\")\n" +
		"public class Test {\n" +
		"}\n"
	);
}
public void testBug282030g1() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT;
	String source =
		"@MyAnnot1(member1 = \"sample1\", member2 = \"sample2\")\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot1(member1 = \"sample1\", member2 = \"sample2\")\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug282030g2() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_FORCE;
	String source =
		"@MyAnnot1(member1 = \"sample1\", member2 = \"sample2\")\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot1(\n" +
		"		member1 = \"sample1\",\n" +
		"		member2 = \"sample2\")\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug282030h1() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"@MyAnnot1(name = \"sample1\", \n" +
		"                value = { \n" +
		"                        @MyAnnot2(name = \"sample2\",\n" +
		"value = \"demo\") \n" +
		"                })\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot1(\n" +
		"		name = \"sample1\",\n" +
		"		value = { @MyAnnot2(name = \"sample2\", value = \"demo\") })\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug282030h2() throws JavaModelException {
	this.formatterPrefs.alignment_for_arguments_in_annotation = Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_FORCE;
	String source =
		"@MyAnnot1(name = \"sample1\", \n" +
		"                value = { \n" +
		"                        @MyAnnot2(name = \"sample2\",\n" +
		"value = \"demo\") \n" +
		"                })\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"@MyAnnot1(\n" +
		"		name = \"sample1\",\n" +
		"		value = { @MyAnnot2(\n" +
		"				name = \"sample2\",\n" +
		"				value = \"demo\") })\n" +
		"public class X {\n" +
		"}\n"
	);
}

/**
 * @bug 283467: [formatter] wrong indentation with 'Never join lines' selected
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=283467"
 */
public void testBug283467() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class TestFormatter {\n" +
		"\n" +
		"        public static void main(String[] args) {\n" +
		"                int variable = TestFormatter.doInCallback(new Runnable() {\n" +
		"                        public void run() {\n" +
		"                                // Some comments or code here\n" +
		"                        }\n" +
		"                });\n" +
		"                System.out.println(variable);\n" +
		"        }\n" +
		"\n" +
		"        public static int doInCallback(Runnable r) {\n" +
		"                return 0;\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class TestFormatter {\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"		int variable = TestFormatter.doInCallback(new Runnable() {\n" +
		"			public void run() {\n" +
		"				// Some comments or code here\n" +
		"			}\n" +
		"		});\n" +
		"		System.out.println(variable);\n" +
		"	}\n" +
		"\n" +
		"	public static int doInCallback(Runnable r) {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 284789: [formatter] Does not line-break method declaration exception with parameters
 * @test Verify that the new preference to split method declaration works properly
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=284789"
 */
public void testBug284789() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"public class Test {\n" +
		"public synchronized List<FantasticallyWonderfulContainer<FantasticallyWonderfulClass>> getMeTheFantasticContainer() {\n" +
		"	return null;\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	public synchronized\n" +
		"			List<FantasticallyWonderfulContainer<FantasticallyWonderfulClass>>\n" +
		"			getMeTheFantasticContainer() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_01a() throws JavaModelException {
	// default is no wrapping for method declaration
	String source =
		"class X01 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X01 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_01b() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"class X01 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X01 {\n" +
		"	public final synchronized java.lang.String\n" +
		"			a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_01c() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	setPageWidth80();
	String source =
		"class X01 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X01 {\n" +
		"	public final synchronized\n" +
		"			java.lang.String a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_01d() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_ONE_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"class X01 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X01 {\n" +
		"	public final synchronized\n" +
		"			java.lang.String\n" +
		"			a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_01e() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_NEXT_SHIFTED_SPLIT;
	setPageWidth80();
	String source =
		"class X01 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X01 {\n" +
		"	public final synchronized\n" +
		"			java.lang.String\n" +
		"				a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_01f() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_NEXT_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"class X01 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X01 {\n" +
		"	public final synchronized java.lang.String\n" +
		"			a_method_which_have_a_very_long_name() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_02a() throws JavaModelException {
	// default is no wrapping for method declaration
	setPageWidth80();
	String source =
		"class X02 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X02 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name(\n" +
		"			String first, String second, String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_02b() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"class X02 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X02 {\n" +
		"	public final synchronized java.lang.String\n" +
		"			a_method_which_have_a_very_long_name(String first, String second,\n" +
		"					String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_02c() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	setPageWidth80();
	String source =
		"class X02 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X02 {\n" +
		"	public final synchronized\n" +
		"			java.lang.String a_method_which_have_a_very_long_name(\n" +
		"					String first, String second, String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_02d() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_ONE_PER_LINE_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_ONE_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"class X02 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X02 {\n" +
		"	public final synchronized\n" +
		"			java.lang.String\n" +
		"			a_method_which_have_a_very_long_name(\n" +
		"					String first,\n" +
		"					String second,\n" +
		"					String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_02e() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_NEXT_SHIFTED_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_NEXT_SHIFTED_SPLIT;
	setPageWidth80();
	String source =
		"class X02 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X02 {\n" +
		"	public final synchronized\n" +
		"			java.lang.String\n" +
		"				a_method_which_have_a_very_long_name(\n" +
		"						String first,\n" +
		"							String second,\n" +
		"							String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug284789_02f() throws JavaModelException {
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_NEXT_PER_LINE_SPLIT;
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_NEXT_PER_LINE_SPLIT;
	setPageWidth80();
	String source =
		"class X02 {\n" +
		"	public final synchronized java.lang.String a_method_which_have_a_very_long_name(String first, String second, String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"class X02 {\n" +
		"	public final synchronized java.lang.String\n" +
		"			a_method_which_have_a_very_long_name(String first,\n" +
		"					String second,\n" +
		"					String third) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 285565: [formatter] wrong indentation with 'Never join lines' selected
 * @test Test to make sure that use either formatter or {@link IndentManipulation}
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
	String source = "public class test {\n"
			+ "    public static void main(String[] args) {\n"
			+ "        int B= 12;\n"
			+ "        int C= B - 1;\n"
			+ "        int K= 99;\n"
			+ "        int f1= K - 1 - C;\n"
			+ "        int f2= K - C - C - C;\n"
			+ "    }\n" + "}\n";
	formatSource(source, "public class test {\n"
			+ "public static void main(String[] args) {\n"
			+ "int B = 12;\n"
			+ "int C = B - 1;\n"
			+ "int K = 99;\n"
			+ "int f1 = K - 1 - C;\n"
			+ "int f2 = K - C - C - C;\n"
			+ "}\n"
			+ "}\n");
}
public void testBug285565c() {
	String result = "int B = 12;\n"
		+ " int C = B - 1;\n"
		+ " int K = 99;\n"
		+ " int f1 = K - 1 - C;\n"
		+ " int f2 = K - C - C - C;" ;

	try {
		assertEquals("Should be as shown", result, IndentManipulation.changeIndent("int B = 12;\n"
			+ "int C = B - 1;\n"
			+ "int K = 99;\n"
			+ "int f1 = K - 1 - C;\n"
			+ "int f2 = K - C - C - C;" ,0,0,0, " ","\n"));

	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}
public void testBug285565d() {
	String result = "int B = 12;\n"
		+ "int C = B - 1;\n"
		+ "int K = 99;\n"
		+ "int f1 = K - 1 - C;\n"
		+ "int f2 = K - C - C - C;" ;

	try {
		assertEquals("Should be as shown", result, IndentManipulation.trimIndent("int B = 12;\n"
			+ "int C = B - 1;\n"
			+ "int K = 99;\n"
			+ "int f1 = K - 1 - C;\n"
			+ "int f2 = K - C - C - C;" , 0, 0, 0));

	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}
public void testBug285565e() {
	try {
		IndentManipulation.getChangeIndentEdits("int B = 12;\n"
			+ "int C = B - 1;\n"
			+ "int K = 99;\n"
			+ "int f1 = K - 1 - C;\n"
			+ "int f2 = K - C - C - C;", 0, 0, 0, " ");

	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}

/**
 * @bug 286601: [formatter] Code formatter formats anonymous inner classes wrongly when 'Never join lines' is on
 * @test Test to make sure that indentation is correct in anonymous inner class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286601"
 */
public void testBug286601() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test\n" +
		"{\n" +
		"    public void aMethod()\n" +
		"    {\n" +
		"        Object anObject = new Object()\n" +
		"        {\n" +
		"            boolean aVariable;\n" +
		"        };\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	public void aMethod() {\n" +
		"		Object anObject = new Object() {\n" +
		"			boolean aVariable;\n" +
		"		};\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"long x1 = 100000000\n" +
		"        + 200000000\n" +
		"        + 300000000;\n" +
		"long x2 = 100000000\n" +
		"        + 200000000\n" +
		"        + 300000000\n" +
		"        + 400000000;\n" +
		"long x3 = 100000000\n" +
		"        + 200000000\n" +
		"        + 300000000\n" +
		"        + 400000000\n" +
		"        + 500000000;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		long x1 = 100000000\n" +
		"				+ 200000000\n" +
		"				+ 300000000;\n" +
		"		long x2 = 100000000\n" +
		"				+ 200000000\n" +
		"				+ 300000000\n" +
		"				+ 400000000;\n" +
		"		long x3 = 100000000\n" +
		"				+ 200000000\n" +
		"				+ 300000000\n" +
		"				+ 400000000\n" +
		"				+ 500000000;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601c() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.brace_position_for_anonymous_type_declaration= DefaultCodeFormatterConstants.NEXT_LINE;
	String source =
		"public class Test\n" +
		"{\n" +
		"    public void aMethod()\n" +
		"    {\n" +
		"        Object anObject = new Object()\n" +
		"        {\n" +
		"            boolean aVariable;\n" +
		"            void foo()\n" +
		"            {\n" +
		"            }\n" +
		"        };\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	public void aMethod() {\n" +
		"		Object anObject = new Object()\n" +
		"		{\n" +
		"			boolean aVariable;\n" +
		"\n" +
		"			void foo() {\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601d() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.brace_position_for_anonymous_type_declaration= DefaultCodeFormatterConstants.NEXT_LINE;
	String source =
		"public class Test\n" +
		"{\n" +
		"    public void aMethod()\n" +
		"    {\n" +
		"        Object anObject = new Object() /* comment */\n" +
		"        {\n" +
		"            boolean aVariable;\n" +
		"            void foo() /* comment */ \n" +
		"            {\n" +
		"            }\n" +
		"        };\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	public void aMethod() {\n" +
		"		Object anObject = new Object() /* comment */\n" +
		"		{\n" +
		"			boolean aVariable;\n" +
		"\n" +
		"			void foo() /* comment */\n" +
		"			{\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601_massive_01() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package massive;\n" +
		"public class X01 {\n" +
		"    public void build(String href) {\n" +
		"        // set the href on the related topic\n" +
		"        if (href == null)\n" +
		"            setHref(\"\"); //$NON-NLS-1$\n" +
		"        else {\n" +
		"            if (!href.equals(\"\") // no empty link //$NON-NLS-1$\n" +
		"                    && !href.startsWith(\"/\") // no help url //$NON-NLS-1$\n" +
		"                    && href.indexOf(\':\') == -1) // no other protocols\n" +
		"            {\n" +
		"                setHref(\"/test/\" + href); //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"            }\n" +
		"        }\n" +
		"    }\n" +
		"\n" +
		"    private void setHref(String string)\n" +
		"    {\n" +
		"        \n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X01\n" +
		"{\n" +
		"	public void build(String href)\n" +
		"	{\n" +
		"		// set the href on the related topic\n" +
		"		if (href == null)\n" +
		"			setHref(\"\"); //$NON-NLS-1$\n" +
		"		else\n" +
		"		{\n" +
		"			if (!href.equals(\"\") // no empty link //$NON-NLS-1$\n" +
		"					&& !href.startsWith(\"/\") // no help url //$NON-NLS-1$\n" +
		"					&& href.indexOf(\':\') == -1) // no other protocols\n" +
		"			{\n" +
		"				setHref(\"/test/\" + href); //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"	private void setHref(String string)\n" +
		"	{\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601_massive_02() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"package massive;\n" +
		"\n" +
		"public class X02\n" +
		"{\n" +
		"    \n" +
		"    private AntModel getAntModel(final File buildFile) {\n" +
		"        AntModel model= new AntModel(XMLCore.getDefault(), doc, null, new LocationProvider(null) {\n" +
		"            /* (non-Javadoc)\n" +
		"             * @see org.eclipse.ant.internal.ui.editor.outline.ILocationProvider#getLocation()\n" +
		"             */\n" +
		"            public IPath getLocation() {\n" +
		"                return new Path(buildFile.getAbsolutePath());\n" +
		"            }\n" +
		"        });\n" +
		"        model.reconcile(null);\n" +
		"        return model;\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X02\n" +
		"{\n" +
		"\n" +
		"	private AntModel getAntModel(final File buildFile)\n" +
		"	{\n" +
		"		AntModel model = new AntModel(XMLCore.getDefault(), doc, null,\n" +
		"				new LocationProvider(null)\n" +
		"				{\n" +
		"					/*\n" +
		"					 * (non-Javadoc)\n" +
		"					 * \n" +
		"					 * @see org.eclipse.ant.internal.ui.editor.outline.\n" +
		"					 * ILocationProvider#getLocation()\n" +
		"					 */\n" +
		"					public IPath getLocation()\n" +
		"					{\n" +
		"						return new Path(buildFile.getAbsolutePath());\n" +
		"					}\n" +
		"				});\n" +
		"		model.reconcile(null);\n" +
		"		return model;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601_massive_03() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package massive;\n" +
		"\n" +
		"public class X03\n" +
		"{\n" +
		"\n" +
		"    public void foo() throws NullPointerException {\n" +
		"\n" +
		"        Object body = new Object() {\n" +
		"            public void run(StringBuffer monitor) throws IllegalArgumentException {\n" +
		"                IResourceVisitor visitor = new IResourceVisitor() {\n" +
		"                    public boolean visit(String resource) throws IllegalArgumentException {\n" +
		"                        return true;\n" +
		"                    }\n" +
		"                };\n" +
		"            }\n" +
		"        };\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		"interface IResourceVisitor {\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X03 {\n" +
		"\n" +
		"	public void foo() throws NullPointerException {\n" +
		"\n" +
		"		Object body = new Object() {\n" +
		"			public void run(StringBuffer monitor)\n" +
		"					throws IllegalArgumentException {\n" +
		"				IResourceVisitor visitor = new IResourceVisitor() {\n" +
		"					public boolean visit(String resource)\n" +
		"							throws IllegalArgumentException {\n" +
		"						return true;\n" +
		"					}\n" +
		"				};\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"\n" +
		"}\n" +
		"\n" +
		"interface IResourceVisitor {\n" +
		"}\n"
	);
}
public void testBug286601_wksp_03b() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"package massive;\n" +
		"\n" +
		"public class X03\n" +
		"{\n" +
		"\n" +
		"    public void foo() throws NullPointerException {\n" +
		"\n" +
		"        Object body = new Object() {\n" +
		"            public void run(StringBuffer monitor) throws IllegalArgumentException {\n" +
		"                IResourceVisitor visitor = new IResourceVisitor() {\n" +
		"                    public boolean visit(String resource) throws IllegalArgumentException {\n" +
		"                        return true;\n" +
		"                    }\n" +
		"                };\n" +
		"            }\n" +
		"        };\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		"interface IResourceVisitor {\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X03\n" +
		"{\n" +
		"\n" +
		"	public void foo() throws NullPointerException\n" +
		"	{\n" +
		"\n" +
		"		Object body = new Object()\n" +
		"		{\n" +
		"			public void run(StringBuffer monitor)\n" +
		"					throws IllegalArgumentException\n" +
		"			{\n" +
		"				IResourceVisitor visitor = new IResourceVisitor()\n" +
		"				{\n" +
		"					public boolean visit(String resource)\n" +
		"							throws IllegalArgumentException\n" +
		"					{\n" +
		"						return true;\n" +
		"					}\n" +
		"				};\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"\n" +
		"}\n" +
		"\n" +
		"interface IResourceVisitor\n" +
		"{\n" +
		"}\n"
	);
}

/**
 * @bug 286668: [formatter] 'Never Join Lines' joins lines that are split on method invocation
 * @test Test to make sure that lines are joined when using 'Never Join Lines' preference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286668"
 */
public void testBug286668() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\").append(\"jkl\")\n" +
		"				.append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"				.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668c() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"		.append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"				.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668_40w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 40;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\")\n" +
		"				.append(\"def\")\n" +
		"				.append(\"ghi\")\n" +
		"				.append(\"jkl\")\n" +
		"				.append(\"mno\")\n" +
		"				.append(\"pqr\")\n" +
		"				.append(\"stu\")\n" +
		"				.append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668b_40w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 40;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\")\n" +
		"				.append(\"def\")\n" +
		"				.append(\"ghi\")\n" +
		"				.append(\"jkl\")\n" +
		"				.append(\"mno\")\n" +
		"				.append(\"pqr\")\n" +
		"				.append(\"stu\")\n" +
		"				.append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668c_40w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 40;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"		.append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\")\n" +
		"				.append(\"def\")\n" +
		"				.append(\"ghi\")\n" +
		"				.append(\"jkl\")\n" +
		"				.append(\"mno\")\n" +
		"				.append(\"pqr\")\n" +
		"				.append(\"stu\")\n" +
		"				.append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668_60w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 60;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\")\n" +
		"				.append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668b_60w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 60;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"				.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668c_60w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 60;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"				.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source);
}

/**
 * @bug 290905: [formatter] Certain formatter pref constellation cause endless loop ==> OOME
 * @test Verify that there's endless loop when setting tab length to zero.
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
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 2, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"void foo() throws Exception { if (true) return; else throw new Exception(); }\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 2, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"  void foo() throws Exception {\n" +
		"    if (true)\n" +
		"      return;\n" +
		"    else\n" +
		"      throw new Exception();\n" +
		"  }\n" +
		"}\n"
	);
}
public void testBug290905b() throws JavaModelException {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 2;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = false;
	String source =
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 2, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"void foo() throws Exception { if (true) return; else throw new Exception(); }\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 2, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"  void foo() throws Exception {\n" +
		"    if (true)\n" +
		"      return;\n" +
		"    else\n" +
		"      throw new Exception();\n" +
		"  }\n" +
		"}\n"
	);
}
public void testBug290905c() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 0, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the format line comment preference is activated\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 0, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the\n" +
		"       // format line comment preference is activated\n" +
		"}\n",
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
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 0, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the format line comment preference is activated\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 0, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the\n" +
		"       // format line comment preference is activated\n" +
		"}\n",
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
		"/**\n" +
		" * Test tab char = TAB, tab size = 0, indent size = 0, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the format line comment preference is activated\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test tab char = TAB, tab size = 0, indent size = 0, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the\n" +
		"       // format line comment preference is activated\n" +
		"}\n",
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
		"/**\n" +
		" * Test tab char = TAB, tab size = 0, indent size = 0, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the format line comment preference is activated\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test tab char = TAB, tab size = 0, indent size = 0, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the\n" +
		"// format line comment preference is activated\n" +
		"}\n",
		false /* do not repeat */
	);
}

/**
 * @bug 293496:  [formatter] 'insert_space_before_opening_brace_in_array_initializer' preference may be reset in certain circumstances
 * @test Verify that a realigned annotation keep the 'insert_space_before_opening_brace_in_array_initializer'
 * 		preference initial value.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293496"
 */
public void testBug293240() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	setPageWidth80();
	String source =
		"public class Test {\n" +
		"  public static <A, B> Function<A, B> forMap(\n" +
		"      Map<? super A, ? extends B> map, @Nullable final B defaultValue) {\n" +
		"    if (defaultValue == null) {\n" +
		"      return forMap(map);\n" +
		"    }\n" +
		"    return new ForMapWithDefault<A, B>(map, defaultValue);\n" +
		"  }\n" +
		"  public Object[] bar() {\n" +
		"	  return new Object[] { null };\n" +
		"  }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"    public static <A, B> Function<A, B> forMap(Map<? super A, ? extends B> map,\n" +
		"            @Nullable final B defaultValue) {\n" +
		"        if (defaultValue == null) {\n" +
		"            return forMap(map);\n" +
		"        }\n" +
		"        return new ForMapWithDefault<A, B>(map, defaultValue);\n" +
		"    }\n" +
		"\n" +
		"    public Object[] bar() {\n" +
		"        return new Object[] { null };\n" +
		"    }\n" +
		"}\n"
	);
}

/**
 * @bug 293300: [formatter] The formatter is still unstable in certain circumstances
 * @test Verify that formatting twice a compilation unit does not produce different output
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293300"
 */
public void testBug293300_wksp1_01() {
	useOldCommentWidthCounting();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	boolean foo(int test, int value) {\n" +
		"		// This comment may also be impacted after having been split in several lines. Furthermore, it\'s also important to verify that the algorithm works when the comment is split into several lines. It\'s a common use case that it may works for 1, 2 but not for 3 iterations...\n" +
		"		if (test == 0) {\n" +
		"			// skip\n" +
		"		} else if (Math.sqrt(Math.pow(test, 2)) > 10) // This is the offending comment after having been split into several lines\n" +
		"			return false;\n" +
		"		return true;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	boolean foo(int test, int value) {\n" +
		"		// This comment may also be impacted after having been split in several\n" +
		"		// lines. Furthermore, it\'s also important to verify that the algorithm\n" +
		"		// works when the comment is split into several lines. It\'s a common use\n" +
		"		// case that it may works for 1, 2 but not for 3 iterations...\n" +
		"		if (test == 0) {\n" +
		"			// skip\n" +
		"		} else if (Math.sqrt(Math.pow(test, 2)) > 10) // This is the offending\n" +
		"														// comment after having\n" +
		"														// been split into\n" +
		"														// several lines\n" +
		"			return false;\n" +
		"		return true;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wkps1_02() {
	useOldCommentWidthCounting();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"	String field;\n" +
		"	 public X02(String test) {\n" +
		"		field= test.toLowerCase();\n" +
		"		try {\n" +
		"			testWhetherItWorksOrNot(test); // This comment will be split and should not involve instability\n" +
		"		} catch (Exception e) {\n" +
		"			return;\n" +
		"		}\n" +
		"	 }\n" +
		"	private void testWhetherItWorksOrNot(String test) {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"	String field;\n" +
		"\n" +
		"	public X02(String test) {\n" +
		"		field = test.toLowerCase();\n" +
		"		try {\n" +
		"			testWhetherItWorksOrNot(test); // This comment will be split and\n" +
		"											// should not involve instability\n" +
		"		} catch (Exception e) {\n" +
		"			return;\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"	private void testWhetherItWorksOrNot(String test) {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wkps1_03() {
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X03 {\n" +
		"public static final native int foo(\n" +
		"	int firstParameter,\n" +
		"	int secondParameter,\n" +
		"	int[] param3);        //When a long comment is placed here with at least one line to follow,\n" +
		"						  //    the second line may be difficult to be formatted correctly\n" +
		"public static final native int bar();\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X03 {\n" +
		"	public static final native int foo(int firstParameter, int secondParameter,\n" +
		"			int[] param3); // When a long comment is placed here with at least\n" +
		"							// one line to follow,\n" +
		"							// the second line may be difficult to be formatted\n" +
		"							// correctly\n" +
		"\n" +
		"	public static final native int bar();\n" +
		"\n" +
		"}\n"
	);
}
public void testBug293300_wkps1_04() {
	useOldCommentWidthCounting();
	String source =
		"package wksp1;\n" +
		"\n" +
		"interface Y04_____________________________ {\n" +
		"}\n" +
		"\n" +
		"public interface X04 extends Y04_____________________________ { // modifier constant\n" +
		"	// those constants are depending upon ClassFileConstants (relying that classfiles only use the 16 lower bits)\n" +
		"	final int AccDefault = 0;\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"interface Y04_____________________________ {\n" +
		"}\n" +
		"\n" +
		"public interface X04 extends Y04_____________________________ { // modifier\n" +
		"																// constant\n" +
		"	// those constants are depending upon ClassFileConstants (relying that\n" +
		"	// classfiles only use the 16 lower bits)\n" +
		"	final int AccDefault = 0;\n" +
		"}\n"
	);
}
public void testBug293300_wkps1_05() {
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X05 {\n" +
		"	private final static String[] TEST_BUG = {\"a\", //$NON-NLS-1$\n" +
		"			\"b\", //$NON-NLS-1$\n" +
		"			\"c\", //$NON-NLS-1$\n" +
		"	};\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X05 {\n" +
		"	private final static String[] TEST_BUG = { \"a\", //$NON-NLS-1$\n" +
		"			\"b\", //$NON-NLS-1$\n" +
		"			\"c\", //$NON-NLS-1$\n" +
		"	};\n" +
		"}\n"
	);
}
public void testBug293300_wkps1_05_JoinLinesComments_BracesNextLine() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X05 {\n" +
		"	private final static String[] TEST_BUG = {\"a\", //$NON-NLS-1$\n" +
		"			\"b\", //$NON-NLS-1$\n" +
		"			\"c\", //$NON-NLS-1$\n" +
		"	};\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X05\n" +
		"{\n" +
		"	private final static String[] TEST_BUG =\n" +
		"	{ \"a\", //$NON-NLS-1$\n" +
		"			\"b\", //$NON-NLS-1$\n" +
		"			\"c\", //$NON-NLS-1$\n" +
		"	};\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_01() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	protected String foo(String[] tests) {\n" +
		"		String result = null;\n" +
		"		for (int i = 0; i < tests.length; i++) {\n" +
		"			String test = tests[i];\n" +
		"			if (test.startsWith(\"test\")) { //$NON-NLS-1$\n" +
		"				//we got the malformed tree exception here\n" +
		"				result = test;\n" +
		"			}\n" +
		"		}\n" +
		"		return result;\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	protected String foo(String[] tests) {\n" +
		"		String result = null;\n" +
		"		for (int i = 0; i < tests.length; i++) {\n" +
		"			String test = tests[i];\n" +
		"			if (test.startsWith(\"test\")) { //$NON-NLS-1$\n" +
		"				// we got the malformed tree exception here\n" +
		"				result = test;\n" +
		"			}\n" +
		"		}\n" +
		"		return result;\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_02() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"\n" +
		"	public void foo(int kind) {\n" +
		"		switch (kind) {\n" +
		"			case 0 :\n" +
		"				break;\n" +
		"			case 1 :\n" +
		"				//the first formatting looks strange on this already splitted\n" +
		"				// comment\n" +
		"				if (true)\n" +
		"					return;\n" +
		"			//fall through\n" +
		"			default:\n" +
		"				if (kind < 0)\n" +
		"					return;\n" +
		"				break;\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	public void foo(int kind) {\n" +
		"		switch (kind) {\n" +
		"		case 0:\n" +
		"			break;\n" +
		"		case 1:\n" +
		"			// the first formatting looks strange on this already splitted\n" +
		"			// comment\n" +
		"			if (true)\n" +
		"				return;\n" +
		"			// fall through\n" +
		"		default:\n" +
		"			if (kind < 0)\n" +
		"				return;\n" +
		"			break;\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_03() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X03 {\n" +
		"	public byte[] foo(byte value) {\n" +
		"		byte[] result = new byte[10];\n" +
		"		int valTest = 0;\n" +
		"		switch (value) {\n" +
		"			case 1 :\n" +
		"				for (int j = 10; j >= 0; j--) {\n" +
		"					result[j] = (byte) (valTest & 0xff); // Bottom 8\n" +
		"					// bits\n" +
		"					valTest = valTest >>> 2;\n" +
		"				}\n" +
		"				break;\n" +
		"		}\n" +
		"		return result;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X03 {\n" +
		"	public byte[] foo(byte value) {\n" +
		"		byte[] result = new byte[10];\n" +
		"		int valTest = 0;\n" +
		"		switch (value) {\n" +
		"		case 1:\n" +
		"			for (int j = 10; j >= 0; j--) {\n" +
		"				result[j] = (byte) (valTest & 0xff); // Bottom 8\n" +
		"				// bits\n" +
		"				valTest = valTest >>> 2;\n" +
		"			}\n" +
		"			break;\n" +
		"		}\n" +
		"		return result;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_04() {
	useOldCommentWidthCounting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		int lastDiagonal[]= new int[1000000 + 1]; // this line comments configuration\n" +
		"		// may screw up the formatter to know which one\n" +
		"		int origin= 1000000 / 2; // needs to stay at its current indentation or not\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		int lastDiagonal[] = new int[1000000 + 1]; // this line comments\n" +
		"													// configuration\n" +
		"		// may screw up the formatter to know which one\n" +
		"		int origin = 1000000 / 2; // needs to stay at its current indentation or\n" +
		"									// not\n" +
		"	}\n" +
		"}\n"
	);
}
private static final String EXPECTED_OUTPUT_WKSP2E1 =
	"package wksp2;\n" +
	"\n" +
	"public class X05 {\n" +
	"	void foo(int val) {\n" +
	"		try {\n" +
	"			loop: for (int i = 0; i < 10; i++) {\n" +
	"				switch (val) {\n" +
	"				case 1:\n" +
	"					if (i == 0) {\n" +
	"						if (true) {\n" +
	"							val++;\n" +
	"						} // these comments\n" +
	"							// may be wrongly\n" +
	"							// realigned\n" +
	"							// by the formatter\n" +
	"\n" +
	"						// other comment\n" +
	"						val--;\n" +
	"						continue loop;\n" +
	"					}\n" +
	"				default:\n" +
	"					throw new IllegalArgumentException();\n" +
	"				}\n" +
	"			}\n" +
	"		} finally {\n" +
	"		}\n" +
	"	}\n" +
	"}\n";
public void testBug293300_wksp2_05() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"							// may be wrongly\n" +
		"							// realigned\n" +
		"							// by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1);
}
public void testBug293300_wksp2_05b() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"							 // may be wrongly\n" +
		"							 // realigned\n" +
		"							 // by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1);
}
private static final String EXPECTED_OUTPUT_WKSP2E3 =
	"package wksp2;\n" +
	"\n" +
	"public class X05 {\n" +
	"	void foo(int val) {\n" +
	"		try {\n" +
	"			loop: for (int i = 0; i < 10; i++) {\n" +
	"				switch (val) {\n" +
	"				case 1:\n" +
	"					if (i == 0) {\n" +
	"						if (true) {\n" +
	"							val++;\n" +
	"						} // these comments\n" +
	"							// may be wrongly\n" +
	"							// realigned\n" +
	"							// by the formatter\n" +
	"\n" +
	"						// other comment\n" +
	"						val--;\n" +
	"						continue loop;\n" +
	"					}\n" +
	"				default:\n" +
	"					throw new IllegalArgumentException();\n" +
	"				}\n" +
	"			}\n" +
	"		} finally {\n" +
	"		}\n" +
	"	}\n" +
	"}\n";
public void testBug293300_wksp2_05c() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"							  // may be wrongly\n" +
		"							  // realigned\n" +
		"							  // by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3);
}
public void testBug293300_wksp2_05d() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"							   // may be wrongly\n" +
		"							   // realigned\n" +
		"							   // by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3);
}
public void testBug293300_wksp2_05e() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"								// may be wrongly\n" +
		"								// realigned\n" +
		"								// by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3);
}
private static final String EXPECTED_OUTPUT_WKSP2E1_SPACES =
	"package wksp2;\n" +
	"\n" +
	"public class X05 {\n" +
	"    void foo(int val) {\n" +
	"        try {\n" +
	"            loop: for (int i = 0; i < 10; i++) {\n" +
	"                switch (val) {\n" +
	"                case 1:\n" +
	"                    if (i == 0) {\n" +
	"                        if (true) {\n" +
	"                            val++;\n" +
	"                        } // these comments\n" +
	"                          // may be wrongly\n" +
	"                          // realigned\n" +
	"                          // by the formatter\n" +
	"\n" +
	"                        // other comment\n" +
	"                        val--;\n" +
	"                        continue loop;\n" +
	"                    }\n" +
	"                default:\n" +
	"                    throw new IllegalArgumentException();\n" +
	"                }\n" +
	"            }\n" +
	"        } finally {\n" +
	"        }\n" +
	"    }\n" +
	"}\n";
public void testBug293300_wksp2_05_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"							// may be wrongly\n" +
		"							// realigned\n" +
		"							// by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1_SPACES);
}
public void testBug293300_wksp2_05b_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"							 // may be wrongly\n" +
		"							 // realigned\n" +
		"							 // by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1_SPACES);
}
private static final String EXPECTED_OUTPUT_WKSP2E3_SPACES =
	"package wksp2;\n" +
	"\n" +
	"public class X05 {\n" +
	"    void foo(int val) {\n" +
	"        try {\n" +
	"            loop: for (int i = 0; i < 10; i++) {\n" +
	"                switch (val) {\n" +
	"                case 1:\n" +
	"                    if (i == 0) {\n" +
	"                        if (true) {\n" +
	"                            val++;\n" +
	"                        } // these comments\n" +
	"                          // may be wrongly\n" +
	"                          // realigned\n" +
	"                          // by the formatter\n" +
	"\n" +
	"                        // other comment\n" +
	"                        val--;\n" +
	"                        continue loop;\n" +
	"                    }\n" +
	"                default:\n" +
	"                    throw new IllegalArgumentException();\n" +
	"                }\n" +
	"            }\n" +
	"        } finally {\n" +
	"        }\n" +
	"    }\n" +
	"}\n";
public void testBug293300_wksp2_05c_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"							  // may be wrongly\n" +
		"							  // realigned\n" +
		"							  // by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3_SPACES);
}
public void testBug293300_wksp2_05d_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"							   // may be wrongly\n" +
		"							   // realigned\n" +
		"							   // by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3_SPACES);
}
public void testBug293300_wksp2_05e_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X05 {\n" +
		"	void foo(int val) {\n" +
		"		try {\n" +
		"			loop: for (int i=0; i<10; i++) {\n" +
		"				switch (val) {\n" +
		"					case 1 :\n" +
		"						if (i==0) {\n" +
		"							if (true) {\n" +
		"								val++;\n" +
		"							} //these comments\n" +
		"								// may be wrongly\n" +
		"								// realigned\n" +
		"								// by the formatter\n" +
		"\n" +
		"							// other comment\n" +
		"							val--;\n" +
		"							continue loop;\n" +
		"						}\n" +
		"					default :\n" +
		"						throw new IllegalArgumentException();\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"		finally {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3_SPACES);
}
public void testBug293300_wksp_06() {
	useOldCommentWidthCounting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X06 {\n" +
		"public static final native int foo(\n" +
		"	String field,        //First field\n" +
		"	int[] array);        //This comment may cause trouble for the formatter, especially if there\'s another\n" +
		"						  //    line below  \n" +
		"public static final native int bar();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X06 {\n" +
		"	public static final native int foo(String field, // First field\n" +
		"			int[] array); // This comment may cause trouble for the formatter,\n" +
		"							// especially if there\'s another\n" +
		"							// line below\n" +
		"\n" +
		"	public static final native int bar();\n" +
		"}\n"
	);
}
public void testBug293300_wksp_07() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X07 {\n" +
		"	void foo(boolean test) {\n" +
		"		if (test) {\n" +
		"			while (true) {\n" +
		"				try {\n" +
		"					try {\n" +
		"					} finally {\n" +
		"						if (true) {\n" +
		"							try {\n" +
		"								toString();\n" +
		"							} catch (Exception e) {\n" +
		"							} // nothing\n" +
		"						}\n" +
		"					} // first comment which does not move\n" +
		"\n" +
		"					// second comment which should not move\n" +
		"					toString();\n" +
		"				} catch (Exception e) {\n" +
		"				}\n" +
		"\n" +
		"			} // last comment\n" +
		"\n" +
		"		}\n" +
		"\n" +
		"		return;\n" +
		"	}\n" +
		"}\n";
	formatSource(source);
}
public void testBug293300_wksp2_08() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X08 {\n" +
		"int foo(int x) {\n" +
		"    while (x < 0) {\n" +
		"        switch (x) {\n" +
		"        \n" +
		"        }\n" +
		"    } // end while\n" +
		"\n" +
		"        // fill in output parameter\n" +
		"    if(x > 10)\n" +
		"        x = 1;\n" +
		"\n" +
		"        // return the value\n" +
		"    return x;\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X08 {\n" +
		"	int foo(int x) {\n" +
		"		while (x < 0) {\n" +
		"			switch (x) {\n" +
		"\n" +
		"			}\n" +
		"		} // end while\n" +
		"\n" +
		"		// fill in output parameter\n" +
		"		if (x > 10)\n" +
		"			x = 1;\n" +
		"\n" +
		"		// return the value\n" +
		"		return x;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_08b() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X08 {\n" +
		"int foo(int x) {\n" +
		"    while (x < 0) {\n" +
		"        switch (x) {\n" +
		"        \n" +
		"        }\n" +
		"    } /* end while */\n" +
		"\n" +
		"        // fill in output parameter\n" +
		"    if(x > 10)\n" +
		"        x = 1;\n" +
		"\n" +
		"        // return the value\n" +
		"    return x;\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X08 {\n" +
		"	int foo(int x) {\n" +
		"		while (x < 0) {\n" +
		"			switch (x) {\n" +
		"\n" +
		"			}\n" +
		"		} /* end while */\n" +
		"\n" +
		"		// fill in output parameter\n" +
		"		if (x > 10)\n" +
		"			x = 1;\n" +
		"\n" +
		"		// return the value\n" +
		"		return x;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_08c() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X08 {\n" +
		"int foo(int x) {\n" +
		"    while (x < 0) {\n" +
		"        switch (x) {\n" +
		"        \n" +
		"        }\n" +
		"    } /** end while */\n" +
		"\n" +
		"        // fill in output parameter\n" +
		"    if(x > 10)\n" +
		"        x = 1;\n" +
		"\n" +
		"        // return the value\n" +
		"    return x;\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X08 {\n" +
		"	int foo(int x) {\n" +
		"		while (x < 0) {\n" +
		"			switch (x) {\n" +
		"\n" +
		"			}\n" +
		"		} /** end while */\n" +
		"\n" +
		"		// fill in output parameter\n" +
		"		if (x > 10)\n" +
		"			x = 1;\n" +
		"\n" +
		"		// return the value\n" +
		"		return x;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_09() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X09 {\n" +
		"void foo(int param) {\n" +
		"        int local = param - 10000; // first comment\n" +
		"                                    // on several lines\n" +
		"        // following unrelated comment\n" +
		"        // also on several lines\n" +
		"        int value = param + 10000;\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X09 {\n" +
		"	void foo(int param) {\n" +
		"		int local = param - 10000; // first comment\n" +
		"									// on several lines\n" +
		"		// following unrelated comment\n" +
		"		// also on several lines\n" +
		"		int value = param + 10000;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_10() {
	useOldCommentWidthCounting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X10 {\n" +
		"\n" +
		"    private  String           field;          //  Trailing comment of the field\n" +
		"                                               //  This comment was not well formatted\n" +
		"                                               //  as an unexpected line was inserted after the first one\n" +
		"\n" +
		"    // -------------------------------\n" +
		"    X10()  {}\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X10 {\n" +
		"\n" +
		"	private String field; // Trailing comment of the field\n" +
		"							// This comment was not well formatted\n" +
		"							// as an unexpected line was inserted after the\n" +
		"							// first one\n" +
		"\n" +
		"	// -------------------------------\n" +
		"	X10() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_11() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public abstract class X11 {\n" +
		"\n" +
		"    // [NEW] \n" +
		"    /**\n" +
		"     * Comment foo\n" +
		"     */\n" +
		"    public abstract StringBuffer foo();\n" +
		"//#if defined(TEST)\n" +
		"//#else\n" +
		"//#endif\n" +
		"\n" +
		"    // [NEW]\n" +
		"    /**\n" +
		"     * Comment foo2\n" +
		"     */\n" +
		"    public abstract StringBuffer foo2();\n" +
		"    // [NEW]\n" +
		"    /**\n" +
		"     * Comment foo3\n" +
		"     */\n" +
		"    public abstract StringBuffer foo3();\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public abstract class X11 {\n" +
		"\n" +
		"	// [NEW]\n" +
		"	/**\n" +
		"	 * Comment foo\n" +
		"	 */\n" +
		"	public abstract StringBuffer foo();\n" +
		"	// #if defined(TEST)\n" +
		"	// #else\n" +
		"	// #endif\n" +
		"\n" +
		"	// [NEW]\n" +
		"	/**\n" +
		"	 * Comment foo2\n" +
		"	 */\n" +
		"	public abstract StringBuffer foo2();\n" +
		"\n" +
		"	// [NEW]\n" +
		"	/**\n" +
		"	 * Comment foo3\n" +
		"	 */\n" +
		"	public abstract StringBuffer foo3();\n" +
		"\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_12a() {
	useOldCommentWidthCounting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X12 {\n" +
		"\n" +
		"\n" +
		"	private boolean sampleField = false;   //trailing comment of the field which\n" +
		" 	                                      //was wrongly formatted in previous\n" +
		"	                                      //version as an unexpected empty lines was\n" +
		"	                                      //inserted after the second comment line...\n" +
		"\n" +
		"\n" +
		"	/**\n" +
		"	    Javadoc comment\n" +
		"	*/\n" +
		"	public X12() {}\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X12 {\n" +
		"\n" +
		"	private boolean sampleField = false; // trailing comment of the field which\n" +
		"											// was wrongly formatted in previous\n" +
		"											// version as an unexpected empty\n" +
		"											// lines was\n" +
		"											// inserted after the second comment\n" +
		"											// line...\n" +
		"\n" +
		"	/**\n" +
		"	 * Javadoc comment\n" +
		"	 */\n" +
		"	public X12() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_12b() {
	useOldCommentWidthCounting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X12 {\n" +
		"\n" +
		"\n" +
		"	private boolean sampleField = false;   //trailing comment of the field which\n" +
		" 	                                       //was wrongly formatted in previous\n" +
		"	                                       //version as an unexpected empty lines was\n" +
		"	                                       //inserted after the second comment line...\n" +
		"\n" +
		"\n" +
		"	/**\n" +
		"	    Javadoc comment\n" +
		"	*/\n" +
		"	public X12() {}\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X12 {\n" +
		"\n" +
		"	private boolean sampleField = false; // trailing comment of the field which\n" +
		"											// was wrongly formatted in previous\n" +
		"											// version as an unexpected empty\n" +
		"											// lines was\n" +
		"											// inserted after the second comment\n" +
		"											// line...\n" +
		"\n" +
		"	/**\n" +
		"	 * Javadoc comment\n" +
		"	 */\n" +
		"	public X12() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_13() {
	useOldCommentWidthCounting();
	setFormatLineCommentOnFirstColumn();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X13 {\n" +
		"void foo(int x) {\n" +
		"	switch (x) {\n" +
		"		default : // regular object ref\n" +
		"//				if (compileTimeType.isRawType() && runtimeTimeType.isBoundParameterizedType()) {\n" +
		"//				    scope.problemReporter().unsafeRawExpression(this, compileTimeType, runtimeTimeType);\n" +
		"//				}\n" +
		"	}\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X13 {\n" +
		"	void foo(int x) {\n" +
		"		switch (x) {\n" +
		"		default: // regular object ref\n" +
		"			// if (compileTimeType.isRawType() &&\n" +
		"			// runtimeTimeType.isBoundParameterizedType()) {\n" +
		"			// scope.problemReporter().unsafeRawExpression(this,\n" +
		"			// compileTimeType, runtimeTimeType);\n" +
		"			// }\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_14() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface X14 {\n" +
		"void foo();\n" +
		"// line 1\n" +
		"// line 2\n" +
		"void bar();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface X14 {\n" +
		"	void foo();\n" +
		"\n" +
		"	// line 1\n" +
		"	// line 2\n" +
		"	void bar();\n" +
		"}\n"
	);
}
// TODO (frederic) try to fix the formatter instability in the following test case
public void _testBug293300_wksp2_15a() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X15 {\n" +
		"	void foo(int[] params) {\n" +
		"		if (params.length > 0) { // trailing comment formatted in several lines...\n" +
		"//			int length = params == null ? : 0 params.length; // this commented lined causes troubles for the formatter but only if the comment starts at column 1...\n" +
		"			for (int i=0; i<params.length; i++) {\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"\n" +
		"public class X15 {\n" +
		"	void foo(int[] params) {\n" +
		"		if (params.length > 0) { // trailing comment formatted in several\n" +
		"									// lines...\n" +
		"			// int length = params == null ? : 0 params.length; // this\n" +
		"			// commented\n" +
		"			// lined causes troubles for the formatter but only if the comment\n" +
		"			// starts at column 1...\n" +
		"			for (int i = 0; i < params.length; i++) {\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp2_15b() {
	useOldCommentWidthCounting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X15 {\n" +
		"	void foo(int[] params) {\n" +
		"		if (params.length > 0) { // trailing comment formatted in several lines...\n" +
		"			// int length = params == null ? : 0 params.length; // this commented lined does not cause troubles for the formatter when the comments is not on column 1...\n" +
		"			for (int i=0; i<params.length; i++) {\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X15 {\n" +
		"	void foo(int[] params) {\n" +
		"		if (params.length > 0) { // trailing comment formatted in several\n" +
		"									// lines...\n" +
		"			// int length = params == null ? : 0 params.length; // this\n" +
		"			// commented lined does not cause troubles for the formatter when\n" +
		"			// the comments is not on column 1...\n" +
		"			for (int i = 0; i < params.length; i++) {\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug293300_wksp3_01() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"package wksp3;\n" +
		"\n" +
		"public class X01 {\n" +
		"static String[] constant = {\n" +
		"// comment\n" +
		"\"first\",\n" +
		"// comment\n" +
		"\"second\",\n" +
		"};\n" +
		"}\n";
	formatSource(source,
		"package wksp3;\n" +
		"\n" +
		"public class X01 {\n" +
		"	static String[] constant = {\n" +
		"			// comment\n" +
		"			\"first\",\n" +
		"			// comment\n" +
		"			\"second\", };\n" +
		"}\n"
	);
}

/**
 * @bug 293496:  [formatter] 'insert_space_before_opening_brace_in_array_initializer' preference may be reset in certain circumstances
 * @test Verify that non ArithmeticException occurs when using tab size = 0
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
 * @bug 294500: [formatter] MalformedTreeException when formatting an invalid sequence of <code> tags in a javadoc comment
 * @test Verify that no MalformedTreeException occurs while formatting bug test cases
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294500"
 */
public void testBug294500a() {
	String source =
		"package wkps3;\n" +
		"/**\n" +
		" * This sample produce an MalformedTreeException\n" +
		" * when formatted.\n" +
		" *\n" +
		" * <p> First paragraph\n" +
		" * {@link java.lang.String </code>a simple\n" +
		" * string<code>}.\n" +
		" *\n" +
		" * <p> Second paragraph.\n" +
		" *\n" +
		" * <p> Third paragraph. </p>\n" +
		" *\n" +
		" */\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wkps3;\n" +
		"\n" +
		"/**\n" +
		" * This sample produce an MalformedTreeException when formatted.\n" +
		" *\n" +
		" * <p>\n" +
		" * First paragraph {@link java.lang.String </code>a simple string<code>}.\n" +
		" *\n" +
		" * <p>\n" +
		" * Second paragraph.\n" +
		" *\n" +
		" * <p>\n" +
		" * Third paragraph.\n" +
		" * </p>\n" +
		" *\n" +
		" */\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n"
	);
}
public void testBug294500b() {
	String source =
		"package wkps3;\n" +
		"/**\n" +
		" * This sample produce an AIIOBE when formatting.\n" +
		" *\n" +
		" * <p> First paragraph\n" +
		" * {@link java.lang.String </code>a simple\n" +
		" * string<code>}.\n" +
		" */\n" +
		"public class X02 {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wkps3;\n" +
		"\n" +
		"/**\n" +
		" * This sample produce an AIIOBE when formatting.\n" +
		" *\n" +
		" * <p>\n" +
		" * First paragraph {@link java.lang.String </code>a simple string<code>}.\n" +
		" */\n" +
		"public class X02 {\n" +
		"\n" +
		"}\n"
	);
}

/**
 * @bug 294618: [formatter] The formatter takes two passes to format a common sequence of html tags
 * @test Verify that the specific sequence of html tags is well formatted in one pass
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294618"
 */
public void testBug294618a() {
	String source =
		"package wkps3;\n" +
		"\n" +
		"/**\n" +
		" * The formatter was not able to format the current comment:\n" +
		" * \n" +
		" * <ol>\n" +
		" *   <li><p> First item\n" +
		" *\n" +
		" *   <li><p> Second item\n" +
		" *\n" +
		" *   <li><p> First paragraph of third item\n" +
		" *\n" +
		" *   <p> Second paragraph of third item\n" +
		" *\n" +
		" *   <blockquote><table cellpadding=0 cellspacing=0 summary=\"layout\">\n" +
		" *   <tr><td><tt>::255.255.0.d</tt><td></tr>\n" +
		" *   </table></blockquote>\n" +
		" *   </li>\n" +
		" * </ol>\n" +
		" */\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wkps3;\n" +
		"\n" +
		"/**\n" +
		" * The formatter was not able to format the current comment:\n" +
		" * \n" +
		" * <ol>\n" +
		" * <li>\n" +
		" * <p>\n" +
		" * First item\n" +
		" *\n" +
		" * <li>\n" +
		" * <p>\n" +
		" * Second item\n" +
		" *\n" +
		" * <li>\n" +
		" * <p>\n" +
		" * First paragraph of third item\n" +
		" *\n" +
		" * <p>\n" +
		" * Second paragraph of third item\n" +
		" *\n" +
		" * <blockquote>\n" +
		" * <table cellpadding=0 cellspacing=0 summary=\"layout\">\n" +
		" * <tr>\n" +
		" * <td><tt>::255.255.0.d</tt>\n" +
		" * <td>\n" +
		" * </tr>\n" +
		" * </table>\n" +
		" * </blockquote></li>\n" +
		" * </ol>\n" +
		" */\n" +
		"public class X01 {\n" +
		"\n" +
		"}\n"
	);
}
public void testBug294618b() {
	String source =
		"/**\n" +
		" * Verify deep html tag nesting:\n" +
		" * \n" +
		" * <ol>\n" +
		" *   <li><p> First item\n" +
		" *   <li><p> Second item\n" +
		" *   <ul>\n" +
		" *     <li><p> First item of second item\n" +
		" *       <blockquote><table cellpadding=0 cellspacing=0 summary=\"layout\">\n" +
		" *       <tr><td><tt><i><b>::255.255.0.d</b></i></tt></td></tr>\n" +
		" *       </table></blockquote>\n" +
		" *     </li>\n" +
		" *   </ul>\n" +
		" *   </li>\n" +
		" * </ol>\n" +
		" */\n" +
		"public class X02 {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Verify deep html tag nesting:\n" +
		" * \n" +
		" * <ol>\n" +
		" * <li>\n" +
		" * <p>\n" +
		" * First item\n" +
		" * <li>\n" +
		" * <p>\n" +
		" * Second item\n" +
		" * <ul>\n" +
		" * <li>\n" +
		" * <p>\n" +
		" * First item of second item <blockquote>\n" +
		" * <table cellpadding=0 cellspacing=0 summary=\"layout\">\n" +
		" * <tr>\n" +
		" * <td><tt><i><b>::255.255.0.d</b></i></tt></td>\n" +
		" * </tr>\n" +
		" * </table>\n" +
		" * </blockquote></li>\n" +
		" * </ul>\n" +
		" * </li>\n" +
		" * </ol>\n" +
		" */\n" +
		"public class X02 {\n" +
		"\n" +
		"}\n"
	);
}

/**
 * @bug 294631: [formatter] The formatter takes two passes to format a common sequence of html tags
 * @test Verify that the specific sequence of html tags is well formatted in one pass
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294631"
 */
public void testBug294631() {
	String source =
		"package wkps3;\n" +
		"\n" +
		"/**\n" +
		" * This comment makes the formatter unstable:\n" +
		" * \n" +
		" * <ol>\n" +
		" *   <li><p> first line\n" +
		" *   second line</li>\n" +
		" * </ol>\n" +
		" */\n" +
		"public class X {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wkps3;\n" +
		"\n" +
		"/**\n" +
		" * This comment makes the formatter unstable:\n" +
		" * \n" +
		" * <ol>\n" +
		" * <li>\n" +
		" * <p>\n" +
		" * first line second line</li>\n" +
		" * </ol>\n" +
		" */\n" +
		"public class X {\n" +
		"\n" +
		"}\n"
	);
}

/**
 * @bug 295175: [formatter] Missing space before a string at the beginning of a javadoc comment
 * @test Verify that space is well inserted before the leading string
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295175"
 */
public void testBug295175a() {
	String source =
		"public class X {\n" +
		"/**\n" +
		" * <p>\n" +
		" * \"String\", this string may be not well formatted in certain circumstances,\n" +
		" * typically after bug 294529 has been fixed...\n" +
		" */\n" +
		"void foo() {}\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * <p>\n" +
		"	 * \"String\", this string may be not well formatted in certain circumstances,\n" +
		"	 * typically after bug 294529 has been fixed...\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug295175b() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface X {\n" +
		"\n" +
		"    /**\n" +
		"     * <P>\n" +
		"     * <BR>\n" +
		"	 *<B>NOTE</B><BR>\n" +
		"	 * Formatter can miss a space before the previous B tag...\n" +
		"     **/\n" +
		"	void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface X {\n" +
		"\n" +
		"	/**\n" +
		"	 * <P>\n" +
		"	 * <BR>\n" +
		"	 * <B>NOTE</B><BR>\n" +
		"	 * Formatter can miss a space before the previous B tag...\n" +
		"	 **/\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug295175c() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface X {\n" +
		"\n" +
		"    /**\n" +
		"     * <P>Following p tag can miss a space before after formatting\n" +
		"     *<p>\n" +
		"     * end of comment.\n" +
		"     **/\n" +
		"	void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface X {\n" +
		"\n" +
		"	/**\n" +
		"	 * <P>\n" +
		"	 * Following p tag can miss a space before after formatting\n" +
		"	 * <p>\n" +
		"	 * end of comment.\n" +
		"	 **/\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug295175d() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface X {\n" +
		"\n" +
		"    /**\n" +
		"     * <p>Following p tag can miss a space before after formatting\n" +
		"     *\n" +
		"     *<p>\n" +
		"     * <BR>\n" +
		"	 *<B>NOTE</B><BR>\n" +
		"	 * Formatter can miss a space before the previous B tag...\n" +
		"     **/\n" +
		"	void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface X {\n" +
		"\n" +
		"	/**\n" +
		"	 * <p>\n" +
		"	 * Following p tag can miss a space before after formatting\n" +
		"	 *\n" +
		"	 * <p>\n" +
		"	 * <BR>\n" +
		"	 * <B>NOTE</B><BR>\n" +
		"	 * Formatter can miss a space before the previous B tag...\n" +
		"	 **/\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug295175e() {
	useOldCommentWidthCounting();
	String source =
		"package wksp3;\n" +
		"\n" +
		"public class X01 {\n" +
		"    /** \n" +
		"     * In this peculiar config <code>true</code>, the comment is not___ \n" +
		"     * really well formatted. The problem is that the first_ code tag\n" +
		"     * here_______ <code>/*</code> and <code>*&#47;</code> go at the end of the previous line\n" +
		"     * instead of staying on the 3rd one... \n" +
		"     */\n" +
		"    void foo() {}\n" +
		"}\n";
	formatSource(source,
		"package wksp3;\n" +
		"\n" +
		"public class X01 {\n" +
		"	/**\n" +
		"	 * In this peculiar config <code>true</code>, the comment is not___ really\n" +
		"	 * well formatted. The problem is that the first_ code tag here_______\n" +
		"	 * <code>/*</code> and <code>*&#47;</code> go at the end of the previous\n" +
		"	 * line instead of staying on the 3rd one...\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug295175f() {
	useOldCommentWidthCounting();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Finds the deepest <code>IJavaElement</code> in the hierarchy of\n" +
		"	 * <code>elt</elt>'s children (including <code>elt</code> itself)\n" +
		"	 * which has a source range that encloses <code>position</code>\n" +
		"	 * according to <code>mapper</code>.\n" +
		"	 */\n" +
		"	void foo() {}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Finds the deepest <code>IJavaElement</code> in the hierarchy of\n" +
		"	 * <code>elt</elt>\'s children (including <code>elt</code> itself) which has\n" +
		"	 * a source range that encloses <code>position</code> according to\n" +
		"	 * <code>mapper</code>.\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 295238: [formatter] The comment formatter add an unexpected new line in block comment
 * @test Verify that formatting a block comment with a tag does not add an unexpected new line
 * 		when the 'Never join lines' option is set
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295238"
 */
public void testBug295238() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public interface X03 {\n" +
		"	\n" +
		"	class Inner {\n" +
		"		\n" +
		"		/* (non-Javadoc)\n" +
		"		 * @see org.eclipse.jface.text.TextViewer#customizeDocumentCommand(org.eclipse.jface.text.DocumentCommand)\n" +
		"		 */\n" +
		"		protected void foo() {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public interface X03 {\n" +
		"\n" +
		"	class Inner {\n" +
		"\n" +
		"		/*\n" +
		"		 * (non-Javadoc)\n" +
		"		 * \n" +
		"		 * @see org.eclipse.jface.text.TextViewer#customizeDocumentCommand(org.\n" +
		"		 * eclipse.jface.text.DocumentCommand)\n" +
		"		 */\n" +
		"		protected void foo() {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// the following test already passed with v_A21, but failed with first version of the patch
public void testBug295238b1() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	void foo() {\n" +
		"/*		if ((operatorSignature & CompareMASK) == (alternateOperatorSignature & CompareMASK)) { // same promotions and result\n" +
		"			scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4)); \n" +
		"		}\n" +
		"*/		\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		/*\n" +
		"		 * if ((operatorSignature & CompareMASK) == (alternateOperatorSignature\n" +
		"		 * & CompareMASK)) { // same promotions and result\n" +
		"		 * scope.problemReporter().unnecessaryCastForArgument((CastExpression)\n" +
		"		 * expression, TypeBinding.wellKnownType(scope,\n" +
		"		 * expression.implicitConversion >> 4));\n" +
		"		 * }\n" +
		"		 */\n" +
		"	}\n" +
		"}\n"
	);
}
// the following test failed with v_A21 and with the version v00 of the patch
public void testBug295238b2() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	void foo() {\n" +
		"/*			scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4)); \n" +
		"*/		\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		/*\n" +
		"		 * scope.problemReporter().unnecessaryCastForArgument((CastExpression)\n" +
		"		 * expression, TypeBinding.wellKnownType(scope,\n" +
		"		 * expression.implicitConversion >> 4));\n" +
		"		 */\n" +
		"	}\n" +
		"}\n"
	);
}
// the following test failed with v_A21 and with the version v00 of the patch
public void testBug295238b3() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	void foo() {\n" +
		"/*\n" +
		"			scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4)); \n" +
		"*/		\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		/*\n" +
		"		 * scope.problemReporter().unnecessaryCastForArgument((CastExpression)\n" +
		"		 * expression, TypeBinding.wellKnownType(scope,\n" +
		"		 * expression.implicitConversion >> 4));\n" +
		"		 */\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 264112: [Formatter] Wrap when necessary too aggressive on short qualifiers
 * @test
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=264112"
 */
// Max line width = 24
public void testBug264112_w24_S1() {
	this.formatterPrefs.page_width = 24;
	String source =
		"class Sample1 {void foo() {Other.bar( 100,\n" +
		"200,\n" +
		"300,\n" +
		"400,\n" +
		"500,\n" +
		"600,\n" +
		"700,\n" +
		"800,\n" +
		"900 );}}\n";
	formatSource(source,
		"class Sample1 {\n" +
		"	void foo() {\n" +
		"		Other.bar(100,\n" +
		"				200,\n" +
		"				300,\n" +
		"				400,\n" +
		"				500,\n" +
		"				600,\n" +
		"				700,\n" +
		"				800,\n" +
		"				900);\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug264112_w24_S2() {
	this.formatterPrefs.page_width = 24;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" +
		"	int foo(Some a) {\n" +
		"		return a.getFirst();\n" +
		"	}\n" +
		"}\n"
	);
}
// Max line width = 25
public void testBug264112_w25_S1() {
	this.formatterPrefs.page_width = 25;
	String source =
		"class Sample1 {void foo() {Other.bar( 100,\n" +
		"200,\n" +
		"300,\n" +
		"400,\n" +
		"500,\n" +
		"600,\n" +
		"700,\n" +
		"800,\n" +
		"900 );}}\n";
	formatSource(source,
		"class Sample1 {\n" +
		"	void foo() {\n" +
		"		Other.bar(100,\n" +
		"				200, 300,\n" +
		"				400, 500,\n" +
		"				600, 700,\n" +
		"				800,\n" +
		"				900);\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug264112_w25_S2() {
	this.formatterPrefs.page_width = 25;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" +
		"	int foo(Some a) {\n" +
		"		return a.getFirst();\n" +
		"	}\n" +
		"}\n"
	);
}
// Max line width = 26
public void testBug264112_w26_S1() {
	this.formatterPrefs.page_width = 26;
	String source =
		"class Sample1 {void foo() {Other.bar( 100,\n" +
		"200,\n" +
		"300,\n" +
		"400,\n" +
		"500,\n" +
		"600,\n" +
		"700,\n" +
		"800,\n" +
		"900 );}}\n";
	formatSource(source,
		"class Sample1 {\n" +
		"	void foo() {\n" +
		"		Other.bar(100,\n" +
		"				200, 300,\n" +
		"				400, 500,\n" +
		"				600, 700,\n" +
		"				800, 900);\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug264112_w26_S2() {
	this.formatterPrefs.page_width = 26;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" +
		"	int foo(Some a) {\n" +
		"		return a.getFirst();\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug264112_wksp1_01() {
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	public Object foo(Object scope) {\n" +
		"		if (scope != null) {\n" +
		"			if (true) {\n" +
		"				for (int i = 0; i < 10; i++) {\n" +
		"					if (i == 0) {\n" +
		"					} else if (i < 5) {\n" +
		"					} else {\n" +
		"						scope.problemReporter().typeMismatchErrorActualTypeExpectedType(expression, expressionTb, expectedElementsTb);\n" +
		"						return null;\n" +
		"					}\n" +
		"				}\n" +
		"			}\n" +
		"			return null;\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	public Object foo(Object scope) {\n" +
		"		if (scope != null) {\n" +
		"			if (true) {\n" +
		"				for (int i = 0; i < 10; i++) {\n" +
		"					if (i == 0) {\n" +
		"					} else if (i < 5) {\n" +
		"					} else {\n" +
		"						scope.problemReporter()\n" +
		"								.typeMismatchErrorActualTypeExpectedType(\n" +
		"										expression, expressionTb,\n" +
		"										expectedElementsTb);\n" +
		"						return null;\n" +
		"					}\n" +
		"				}\n" +
		"			}\n" +
		"			return null;\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug264112_wksp1_02() {
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	public String toString() {\n" +
		"		StringBuffer buffer = new StringBuffer();\n" +
		"		if (true) {\n" +
		"			buffer.append(\"- possible values:	[\"); //$NON-NLS-1$ \n" +
		"			buffer.append(\"]\\n\"); //$NON-NLS-1$ \n" +
		"			buffer.append(\"- curr. val. index:	\").append(currentValueIndex).append(\"\\n\"); //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"		}\n" +
		"		buffer.append(\"- description:		\").append(description).append(\"\\n\"); //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"		return buffer.toString();\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	public String toString() {\n" +
		"		StringBuffer buffer = new StringBuffer();\n" +
		"		if (true) {\n" +
		"			buffer.append(\"- possible values:	[\"); //$NON-NLS-1$\n" +
		"			buffer.append(\"]\\n\"); //$NON-NLS-1$\n" +
		"			buffer.append(\"- curr. val. index:	\").append(currentValueIndex).append(\"\\n\"); //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"		}\n" +
		"		buffer.append(\"- description:		\").append(description).append(\"\\n\"); //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"		return buffer.toString();\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug264112_wksp2_01() {
	setPageWidth80();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"    private static final String PATH_SMOOTH_QUAD_TO = \"SMOOTH\";\n" +
		"    private static final String XML_SPACE = \" \";\n" +
		"    private static final String PATH_CLOSE = \"CLOSE\";\n" +
		"\n" +
		"	String foo(Point point, Point point_plus1) {\n" +
		"        StringBuffer sb = new StringBuffer();\n" +
		"        while (true) {\n" +
		"            if (point != null) {\n" +
		"                // Following message send was unnecessarily split\n" +
		"                sb.append(PATH_SMOOTH_QUAD_TO)\n" +
		"                .append(String.valueOf(midValue(point.x, point_plus1.x)))\n" +
		"                .append(XML_SPACE)\n" +
		"                .append(String.valueOf(midValue(point.y, point_plus1.y)));\n" +
		"            } else {\n" +
		"                break;\n" +
		"            }\n" +
		"        }\n" +
		"        sb.append(PATH_CLOSE);\n" +
		"\n" +
		"        return sb.toString();\n" +
		"    }\n" +
		"\n" +
		"    private int midValue(int x1, int x2) {\n" +
		"        return (x1 + x2) / 2;\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		"class Point {\n" +
		"	int x,y;\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	private static final String PATH_SMOOTH_QUAD_TO = \"SMOOTH\";\n" +
		"	private static final String XML_SPACE = \" \";\n" +
		"	private static final String PATH_CLOSE = \"CLOSE\";\n" +
		"\n" +
		"	String foo(Point point, Point point_plus1) {\n" +
		"		StringBuffer sb = new StringBuffer();\n" +
		"		while (true) {\n" +
		"			if (point != null) {\n" +
		"				// Following message send was unnecessarily split\n" +
		"				sb.append(PATH_SMOOTH_QUAD_TO)\n" +
		"						.append(String\n" +
		"								.valueOf(midValue(point.x, point_plus1.x)))\n" +
		"						.append(XML_SPACE).append(String\n" +
		"								.valueOf(midValue(point.y, point_plus1.y)));\n" +
		"			} else {\n" +
		"				break;\n" +
		"			}\n" +
		"		}\n" +
		"		sb.append(PATH_CLOSE);\n" +
		"\n" +
		"		return sb.toString();\n" +
		"	}\n" +
		"\n" +
		"	private int midValue(int x1, int x2) {\n" +
		"		return (x1 + x2) / 2;\n" +
		"	}\n" +
		"\n" +
		"}\n" +
		"\n" +
		"class Point {\n" +
		"	int x, y;\n" +
		"}\n"
	);
}
public void testBug264112_wksp2_02() {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X02 {\n" +
		"	\n" +
		"	void test(X02 indexsc) {\n" +
		"		if (indexsc == null) {\n" +
		"		} else {\n" +
		"\n" +
		"			indexsc.reopenScan(\n" +
		"						searchRow,                      	// startKeyValue\n" +
		"						ScanController.GE,            		// startSearchOp\n" +
		"						null,                         		// qualifier\n" +
		"						null, 		                        // stopKeyValue\n" +
		"						ScanController.GT             		// stopSearchOp \n" +
		"						);\n" +
		"		}\n" +
		"		\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X02 {\n" +
		"\n" +
		"	void test(X02 indexsc) {\n" +
		"		if (indexsc == null) {\n" +
		"		} else {\n" +
		"\n" +
		"			indexsc.reopenScan(searchRow, // startKeyValue\n" +
		"					ScanController.GE, // startSearchOp\n" +
		"					null, // qualifier\n" +
		"					null, // stopKeyValue\n" +
		"					ScanController.GT // stopSearchOp\n" +
		"			);\n" +
		"		}\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 297225: [formatter] Indentation may be still wrong in certain circumstances after formatting
 * @test Verify that comment indentation is correct when there's a mix of tab and spaces in
 * 		existing indentation and all comments formatting is off.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297225"
 */
public void testBug297225() {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.comment_format_javadoc_comment = false;
	String source =
		"public class X01 {\n" +
		"   	\n" +
		"   	/**\n" +
		"   	 * The foo method\n" +
		"   	 */\n" +
		"	void foo() {}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * The foo method\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 297546: [formatter] Formatter removes blank after @see if reference is wrapped
 * @test Verify that space after the @see tag is not removed while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297546"
 */
public void testBug297546() {
	String source =
		"package org.eclipse.jdt.core;\n" +
		"public class TestClass implements TestInterface {\n" +
		"\n" +
		"	/* (non-Javadoc)\n" +
		"	 * @see org.eclipse.jdt.core.TestInterface#testMethod(org.eclipse.jdt.core.TestInterface)\n" +
		"	 */\n" +
		"	public void testMethod(TestInterface aLongNameForAParam) {\n" +
		"		// do nothing\n" +
		"	}\n" +
		"\n" +
		"	\n" +
		"}\n" +
		"interface TestInterface {\n" +
		"	void testMethod(TestInterface aLongNameForAParam);\n" +
		"}\n";
	formatSource(source,
		"package org.eclipse.jdt.core;\n" +
		"\n" +
		"public class TestClass implements TestInterface {\n" +
		"\n" +
		"	/*\n" +
		"	 * (non-Javadoc)\n" +
		"	 * \n" +
		"	 * @see org.eclipse.jdt.core.TestInterface#testMethod(org.eclipse.jdt.core.\n" +
		"	 * TestInterface)\n" +
		"	 */\n" +
		"	public void testMethod(TestInterface aLongNameForAParam) {\n" +
		"		// do nothing\n" +
		"	}\n" +
		"\n" +
		"}\n" +
		"\n" +
		"interface TestInterface {\n" +
		"	void testMethod(TestInterface aLongNameForAParam);\n" +
		"}\n"
	);
}

/**
 * @bug 298243: [formatter] Removing empty lines between import groups
 * @test Verify that space after the @see tag is not removed while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=298243"
 */
public void testBug298243() {
	this.formatterPrefs.number_of_empty_lines_to_preserve = 0;
	String source =
		"package test;\n" +
		"\n" +
		"import java.util.concurrent.atomic.AtomicInteger;\n" +
		"\n" +
		"import org.xml.sax.SAXException;\n" +
		"\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) {\n" +
		"		SAXException e;\n" +
		"		AtomicInteger w;\n" +
		"	}\n" +
		"}\n";
	formatSource(source);
}

/**
 * @bug 298844: [formatter] New lines in empty method body wrong behavior
 * @test Verify that comment is well indented inside empty constructor and method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=298844"
 */
public void testBug298844a() {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.keep_method_body_on_one_line = DefaultCodeFormatterConstants.ONE_LINE_IF_EMPTY;
	String source =
		"public class X01 {\n" +
		"public X01() {\n" +
		"// TODO Auto-generated constructor stub\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	public X01() {\n" +
		"		// TODO Auto-generated constructor stub\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug298844b() {
	this.formatterPrefs.keep_method_body_on_one_line = DefaultCodeFormatterConstants.ONE_LINE_IF_EMPTY;
	String source =
		"public class X02 {\n" +
		"public void foo() {\n" +
		"	// TODO Auto-generated constructor stub\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	public void foo() {\n" +
		"		// TODO Auto-generated constructor stub\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 302123: [formatter] AssertionFailedException occurs while formatting a source containing the specific javadoc comment...
 * @test Verify that no exception occurs while formatting source including the specific comment
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=302123"
 */
public void testBug302123() {
	String source =
		"package test;\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) {\n" +
		"		String s=\"X\"+/** ***/\"Y\";\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package test;\n" +
		"\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) {\n" +
		"		String s = \"X\" + /** ***/\n" +
		"				\"Y\";\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug302123b() {
	String source =
		"package test;\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) {\n" +
		"		String s=\"X\"+/**    XXX   ***/\"Y\";\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package test;\n" +
		"\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) {\n" +
		"		String s = \"X\" + /** XXX ***/\n" +
		"				\"Y\";\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug302123c() {
	String source =
		"package test;\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) {\n" +
		"		String s=\"X\"+/**    **  XXX  **    ***/\"Y\";\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package test;\n" +
		"\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) {\n" +
		"		String s = \"X\" + /** ** XXX ** ***/\n" +
		"				\"Y\";\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug302123d() {
	String source =
		"package test;\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) {\n" +
		"		String s=\"X\"+/**AAA   *** BBB ***   CCC***/\"Y\";\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package test;\n" +
		"\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) {\n" +
		"		String s = \"X\" + /** AAA *** BBB *** CCC ***/\n" +
		"				\"Y\";\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}

/**
 * @bug 302552: [formatter] Formatting qualified invocations can be broken when the Line Wrapping policy forces element to be on a new line
 * @test Verify that wrapping policies forcing the first element to be on a new line are working again...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=302552"
 */
public void testBug302552_LW0() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_NO_ALIGNMENT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" +
		"	int foo(Some a) {\n" +
		"		return a.getFirst();\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug302552_LW1() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_COMPACT_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" +
		"	int foo(Some a) {\n" +
		"		return a.getFirst();\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug302552_LW2() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" +
		"	int foo(Some a) {\n" +
		"		return a\n" +
		"				.getFirst();\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug302552_LW3() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_ONE_PER_LINE_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" +
		"	int foo(Some a) {\n" +
		"		return a\n" +
		"				.getFirst();\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug302552_LW4() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_NEXT_SHIFTED_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" +
		"	int foo(Some a) {\n" +
		"		return a\n" +
		"				.getFirst();\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug302552_LW5() {
	this.formatterPrefs.page_width = 20;
	this.formatterPrefs.alignment_for_selector_in_method_invocation = Alignment.M_NEXT_PER_LINE_SPLIT;
	String source =
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" +
		"	int foo(Some a) {\n" +
		"		return a.getFirst();\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 304529: [formatter] NPE when either the disabling or the enabling tag is not defined
 * @test Verify that having an empty disabling or enabling is now accepted by the formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=304529"
 */
public void testBug304529() {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "off".toCharArray();
	this.formatterPrefs.enabling_tag = null;
	String source =
		"/* off */\n" +
		"public class X01 {\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       area\n" +
		"}\n" +
		"}\n";
	formatSource(source);
}
public void testBug304529b() {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = null;
	this.formatterPrefs.enabling_tag = "on".toCharArray();
	String source =
		"/* on */\n" +
		"public class X01 {\n" +
		"void     foo(    )      {	\n" +
		"				//      formatted       area\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"/* on */\n" +
		"public class X01 {\n" +
		"	void foo() {\n" +
		"		// formatted area\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug304529c() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "off");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "");
	String source =
		"/* off */\n" +
		"public class X01 {\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       area\n" +
		"}\n" +
		"}\n";
	formatSource(source);
}
public void testBug304529d() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "on");
	String source =
		"/* on */\n" +
		"public class X01 {\n" +
		"void     foo(    )      {	\n" +
		"				//      formatted       area\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"/* on */\n" +
		"public class X01 {\n" +
		"	void foo() {\n" +
		"		// formatted area\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug304529e() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "off");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "on");
	String source =
		"public class X01 {\n" +
		"/* off */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       area\n" +
		"}\n" +
		"/* on */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       area\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"/* off */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       area\n" +
		"}\n" +
		"/* on */\n" +
		"	void bar() {\n" +
		"		// formatted area\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 309706: [formatter] doesn't work when code has three semicolons side by side
 * @test Verify that formatter does get puzzled by three consecutive semicolons
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=309706"
 */
public void testBug309706() {
	String source =
		"public class Test {\n" +
		"\n" +
		"    private int id;;;\n" +
		"\n" +
		"    private void dummy() {\n" +
		"\n" +
		"        if (true) {\n" +
		"                    System.out.println(\"bla\");\n" +
		"        }\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	private int id;;;\n" +
		"\n" +
		"	private void dummy() {\n" +
		"\n" +
		"		if (true) {\n" +
		"			System.out.println(\"bla\");\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug309706b() {
	String source =
		"    private int id;;;\n" +
		"\n" +
		"    private void dummy() {\n" +
		"\n" +
		"        if (true) {\n" +
		"                    System.out.println(\"bla\");\n" +
		"        }\n" +
		"	}\n";
	formatSource(source,
		"private int id;;;\n" +
		"\n" +
		"private void dummy() {\n" +
		"\n" +
		"	if (true) {\n" +
		"		System.out.println(\"bla\");\n" +
		"	}\n" +
		"}\n",
		CodeFormatter.K_CLASS_BODY_DECLARATIONS
	);
}

/**
 * @bug 311578: [formatter] Enable/disable tag detection should include comment start/end tokens
 * @test Ensure that the formatter now accepts tags with comment start/end tokens
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=311578"
 */
public void testBug311578a() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "//J-".toCharArray();
	this.formatterPrefs.enabling_tag = "//J+".toCharArray();
	String source =
		"package a;\n" +
		"public class Bug {\n" +
		"int a      =  -     1  +    42;\n" +
		"\n" +
		"//J-\n" +
		"int b      =  -     1  +    42;\n" +
		"//J+\n" +
		"\n" +
		"char                       x;\n" +
		"\n" +
		"////J-\n" +
		"int c      =  -     1  +    42;\n" +
		"////J+\n" +
		"\n" +
		"char                       y;\n" +
		"\n" +
		"/* J- */\n" +
		"int d      =  -     1  +    42;\n" +
		"/* J+ */\n" +
		"\n" +
		"char                       z;\n" +
		"\n" +
		"/* //J- */\n" +
		"int e      =  -     1  +    42;\n" +
		"/* //J+ */\n" +
		"\n" +
		"/** J-1 blabla */\n" +
		"char                       t;\n" +
		"}\n";
	formatSource(source,
		"package a;\n" +
		"\n" +
		"public class Bug {\n" +
		"	int a = -1 + 42;\n" +
		"\n" +
		"//J-\n" +
		"int b      =  -     1  +    42;\n" +
		"//J+\n" +
		"\n" +
		"	char x;\n" +
		"\n" +
		"////J-\n" +
		"int c      =  -     1  +    42;\n" +
		"////J+\n" +
		"\n" +
		"	char y;\n" +
		"\n" +
		"	/* J- */\n" +
		"	int d = -1 + 42;\n" +
		"	/* J+ */\n" +
		"\n" +
		"	char z;\n" +
		"\n" +
		"/* //J- */\n" +
		"int e      =  -     1  +    42;\n" +
		"/* //J+ */\n" +
		"\n" +
		"	/** J-1 blabla */\n" +
		"	char t;\n" +
		"}\n"
	);
}
public void testBug311578b() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "/* J- */".toCharArray();
	this.formatterPrefs.enabling_tag = "/* J+ */".toCharArray();
	String source =
		"package a;\n" +
		"public class Bug {\n" +
		"int a      =  -     1  +    42;\n" +
		"\n" +
		"//J-\n" +
		"int b      =  -     1  +    42;\n" +
		"//J+\n" +
		"\n" +
		"char                       x;\n" +
		"\n" +
		"////J-\n" +
		"int c      =  -     1  +    42;\n" +
		"////J+\n" +
		"\n" +
		"char                       y;\n" +
		"\n" +
		"/* J- */\n" +
		"int d      =  -     1  +    42;\n" +
		"/* J+ */\n" +
		"\n" +
		"char                       z;\n" +
		"\n" +
		"/* //J- */\n" +
		"int e      =  -     1  +    42;\n" +
		"/* //J+ */\n" +
		"\n" +
		"/** J-1 blabla */\n" +
		"char                       t;\n" +
		"}\n";
	formatSource(source,
		"package a;\n" +
		"\n" +
		"public class Bug {\n" +
		"	int a = -1 + 42;\n" +
		"\n" +
		"	// J-\n" +
		"	int b = -1 + 42;\n" +
		"	// J+\n" +
		"\n" +
		"	char x;\n" +
		"\n" +
		"	//// J-\n" +
		"	int c = -1 + 42;\n" +
		"	//// J+\n" +
		"\n" +
		"	char y;\n" +
		"\n" +
		"/* J- */\n" +
		"int d      =  -     1  +    42;\n" +
		"/* J+ */\n" +
		"\n" +
		"	char z;\n" +
		"\n" +
		"	/* //J- */\n" +
		"	int e = -1 + 42;\n" +
		"	/* //J+ */\n" +
		"\n" +
		"	/** J-1 blabla */\n" +
		"	char t;\n" +
		"}\n"
	);
}
public void testBug311578c() throws JavaModelException {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "//F--");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "//F++");
	String source =
		"package a;\n" +
		"public class Bug {\n" +
		"int a      =  -     1  +    42;\n" +
		"\n" +
		"//F--\n" +
		"int b      =  -     1  +    42;\n" +
		"//F++\n" +
		"\n" +
		"char                       x;\n" +
		"\n" +
		"////F--\n" +
		"int c      =  -     1  +    42;\n" +
		"////F++\n" +
		"\n" +
		"char                       y;\n" +
		"\n" +
		"/* F-- */\n" +
		"int d      =  -     1  +    42;\n" +
		"/* F++ */\n" +
		"\n" +
		"char                       z;\n" +
		"\n" +
		"/* //F-- */\n" +
		"int e      =  -     1  +    42;\n" +
		"/* //F++ */\n" +
		"\n" +
		"/** F--1 blabla */\n" +
		"char                       t;\n" +
		"}\n";
	formatSource(source,
		"package a;\n" +
		"\n" +
		"public class Bug {\n" +
		"	int a = -1 + 42;\n" +
		"\n" +
		"//F--\n" +
		"int b      =  -     1  +    42;\n" +
		"//F++\n" +
		"\n" +
		"	char x;\n" +
		"\n" +
		"////F--\n" +
		"int c      =  -     1  +    42;\n" +
		"////F++\n" +
		"\n" +
		"	char y;\n" +
		"\n" +
		"	/* F-- */\n" +
		"	int d = -1 + 42;\n" +
		"	/* F++ */\n" +
		"\n" +
		"	char z;\n" +
		"\n" +
		"/* //F-- */\n" +
		"int e      =  -     1  +    42;\n" +
		"/* //F++ */\n" +
		"\n" +
		"	/** F--1 blabla */\n" +
		"	char t;\n" +
		"}\n"
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
		"package a;\n" +
		"public class Bug {\n" +
		"int a      =  -     1  +    42;\n" +
		"\n" +
		"//F--\n" +
		"int b      =  -     1  +    42;\n" +
		"//F++\n" +
		"\n" +
		"char                       x;\n" +
		"\n" +
		"////F--\n" +
		"int c      =  -     1  +    42;\n" +
		"////F++\n" +
		"\n" +
		"char                       y;\n" +
		"\n" +
		"/* F-- */\n" +
		"int d      =  -     1  +    42;\n" +
		"/* F++ */\n" +
		"\n" +
		"char                       y2;\n" +
		"\n" +
		"/*F--*/\n" +
		"int d2      =  -     1  +    42;\n" +
		"/*F++*/\n" +
		"\n" +
		"char                       z;\n" +
		"\n" +
		"/* //F-- */\n" +
		"int e      =  -     1  +    42;\n" +
		"/* //F++ */\n" +
		"\n" +
		"/** F--1 blabla */\n" +
		"char                       t;\n" +
		"}\n";
	formatSource(source,
		"package a;\n" +
		"\n" +
		"public class Bug {\n" +
		"	int a = -1 + 42;\n" +
		"\n" +
		"	// F--\n" +
		"	int b = -1 + 42;\n" +
		"	// F++\n" +
		"\n" +
		"	char x;\n" +
		"\n" +
		"	//// F--\n" +
		"	int c = -1 + 42;\n" +
		"	//// F++\n" +
		"\n" +
		"	char y;\n" +
		"\n" +
		"	/* F-- */\n" +
		"	int d = -1 + 42;\n" +
		"	/* F++ */\n" +
		"\n" +
		"	char y2;\n" +
		"\n" +
		"/*F--*/\n" +
		"int d2      =  -     1  +    42;\n" +
		"/*F++*/\n" +
		"\n" +
		"	char z;\n" +
		"\n" +
		"	/* //F-- */\n" +
		"	int e = -1 + 42;\n" +
		"	/* //F++ */\n" +
		"\n" +
		"	/** F--1 blabla */\n" +
		"	char t;\n" +
		"}\n"
	);
}
public void testBug311578e() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "//J-".toCharArray();
	this.formatterPrefs.enabling_tag = "//J+".toCharArray();
	String source =
		"package a;\n" +
		"public class Bug {\n" +
		"char                       z2;\n" +
		"\n" +
		"//J-1\n" +
		"int f      =  -     1  +    42;\n" +
		"//J+2\n" +
		"\n" +
		"char                       z3;\n" +
		"\n" +
		"//J- 1\n" +
		"int g      =  -     1  +    42;\n" +
		"//J+ 2\n" +
		"\n" +
		"char                       z4;\n" +
		"\n" +
		"  //J-\n" +
		"int h      =  -     1  +    42;\n" +
		"  //J+\n" +
		"\n" +
		"char                       z5;\n" +
		"\n" +
		"/*\n" +
		"//J-\n" +
		"*/\n" +
		"int i      =  -     1  +    42;\n" +
		"/*\n" +
		" //J+\n" +
		" */\n" +
		"\n" +
		"char                       z6;" +
		"}\n";
	formatSource(source,
		"package a;\n" +
		"\n" +
		"public class Bug {\n" +
		"	char z2;\n" +
		"\n" +
		"//J-1\n" +
		"int f      =  -     1  +    42;\n" +
		"//J+2\n" +
		"\n" +
		"	char z3;\n" +
		"\n" +
		"//J- 1\n" +
		"int g      =  -     1  +    42;\n" +
		"//J+ 2\n" +
		"\n" +
		"	char z4;\n" +
		"\n" +
		"	//J-\n" +
		"int h      =  -     1  +    42;\n" +
		"  //J+\n" +
		"\n" +
		"	char z5;\n" +
		"\n" +
		"/*\n" +
		"//J-\n" +
		"*/\n" +
		"int i      =  -     1  +    42;\n" +
		"/*\n" +
		" //J+\n" +
		" */\n" +
		"\n" +
		"	char z6;\n" +
		"}\n"
	);
}
public void testBug311578_320754a() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "//J-".toCharArray();
	this.formatterPrefs.enabling_tag = "//J+".toCharArray();
	String source =
		"//J-\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"//J+\n" +
		"public class X\n" +
		"{\n" +
		"    public void foo()\n" +
		"    {\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"//J-\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"//J+\n" +
		"public class X {\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug311578_320754b() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	this.formatterPrefs.disabling_tag = "/*J-*/".toCharArray();
	this.formatterPrefs.enabling_tag = "/*J+*/".toCharArray();
	String source =
		"/*J-*/\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"/*J+*/\n" +
		"public class X\n" +
		"{\n" +
		"    public void foo()\n" +
		"    {\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"/*J-*/\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"/*J+*/\n" +
		"public class X {\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 311582: [formatter] Master switch to enable/disable on/off tags
 * @test Ensure that the formatter does take care of formatting tags by default
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=311582"
 */
public void testBug311582a() throws JavaModelException {
	this.formatterPrefs.disabling_tag = "disable-formatter".toCharArray();
	this.formatterPrefs.enabling_tag = "enable-formatter".toCharArray();
	String source =
		"public class X01 {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"/* disable-formatter */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/* enable-formatter */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug311582b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "off");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "");
	String source =
		"/* off */\n" +
		"public class X01 {\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       area\n" +
		"}\n" +
		"}\n";
	formatSource(source);
}

/**
 * @bug 311617: [formatter] Master switch to enable/disable on/off tags
 * @test Ensure that the formatter does not take care of formatting tags by default
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=311617"
 */
public void testBug311617() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"public class X01 {\n" +
		"\n" +
		"/* @formatter:off */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/* @formatter:on */\n" +
		"void     bar(    )      {	\n" +
		"				//      formatted       comment\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"/* @formatter:off */\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       comment\n" +
		"}\n" +
		"/* @formatter:on */\n" +
		"	void bar() {\n" +
		"		// formatted comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug311617b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	String source =
		"/* @formatter:off */\n" +
		"public class X01 {\n" +
		"void     foo(    )      {	\n" +
		"				//      unformatted       area\n" +
		"}\n" +
		"}\n";
	formatSource(source);
}

/**
 * @bug 313524: [formatter] Add preference for improved lines wrapping in nested method calls
 * @test Ensure that the formatter keep previous eclipse versions behavior when
 * 		the "Try to keep nested expressions on one line" preference is set.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=313524"
 */
public void testBug313524_01() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	String source =
		"public class X01 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6,\n" +
		"				7, 8));\n" +
		"	}\n" +
		"}\n"
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
		"public class X01 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(	5, 6,\n" +
		"									7,\n" +
		"									8));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_02() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	String source =
		"public class X02 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), bar(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4, 5, 6, 7, 8,\n" +
		"				9, 10), bar(11, 12, 13,\n" +
		"						14, 15, 16, 17,\n" +
		"						18, 19, 20));\n" +
		"	}\n" +
		"}\n"
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
		"public class X02 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), bar(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4, 5, 6, 7, 8,\n" +
		"				9, 10), bar(11, 12, 13,\n" +
		"							14, 15, 16,\n" +
		"							17, 18, 19,\n" +
		"							20));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_03() throws JavaModelException {
	this.formatterPrefs.page_width = 40;
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	String source =
		"public class X03 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8), bar(9, 10, 11, 12));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6,\n" +
		"				7, 8), bar(9, 10, 11,\n" +
		"						12));\n" +
		"	}\n" +
		"}\n"
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
		"public class X03 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(5, 6, 7, 8), bar(9, 10, 11, 12));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	void test() {\n" +
		"		foo(bar(1, 2, 3, 4), bar(	5, 6,\n" +
		"									7,\n" +
		"									8),\n" +
		"			bar(9, 10, 11, 12));\n" +
		"	}\n" +
		"}\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146175
public void testBug313524_146175() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class FormatterDemo {\n" +
		"\n" +
		"    public void fooBar() {\n" +
		"        SomeOtherClass instanceOfOtherClass = new SomeOtherClass();\n" +
		"\n" +
		"        /* The following statement demonstrates the formatter issue */\n" +
		"        SomeOtherClass.someMethodInInnerClass(\n" +
		"            instanceOfOtherClass.anotherMethod(\"Value of paramter 1\"),\n" +
		"            instanceOfOtherClass.anotherMethod(\"Value of paramter 2\"));\n" +
		"\n" +
		"    }\n" +
		"\n" +
		"    private static class SomeOtherClass {\n" +
		"        public static void someMethodInInnerClass(\n" +
		"            String param1,\n" +
		"            String param2) {\n" +
		"        }\n" +
		"        public String anotherMethod(String par) {\n" +
		"            return par;\n" +
		"        }\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class FormatterDemo {\n" +
		"\n" +
		"	public void fooBar() {\n" +
		"		SomeOtherClass instanceOfOtherClass = new SomeOtherClass();\n" +
		"\n" +
		"		/* The following statement demonstrates the formatter issue */\n" +
		"		SomeOtherClass.someMethodInInnerClass(instanceOfOtherClass\n" +
		"				.anotherMethod(\"Value of paramter 1\"), instanceOfOtherClass\n" +
		"						.anotherMethod(\"Value of paramter 2\"));\n" +
		"\n" +
		"	}\n" +
		"\n" +
		"	private static class SomeOtherClass {\n" +
		"		public static void someMethodInInnerClass(String param1,\n" +
		"				String param2) {\n" +
		"		}\n" +
		"\n" +
		"		public String anotherMethod(String par) {\n" +
		"			return par;\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
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
		"public class Test {\n" +
		"    int someLongMethodName(int foo,  boolean bar, String yetAnotherArg) {\n" +
		"        return 0;\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	int someLongMethodName(	int foo,\n" +
		"							boolean bar,\n" +
		"							String yetAnotherArg) {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n"
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
		"public class X01 {\n" +
		"    void foo() {\n" +
		"           someIdentifier(someArg).someMethodName().someMethodName(foo, bar).otherMethod(arg0, arg1);\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"    void foo() {\n" +
		"        someIdentifier(someArg).someMethodName()\n" +
		"                               .someMethodName(foo,\n" +
		"                                       bar)\n" +
		"                               .otherMethod(arg0,\n" +
		"                                       arg1);\n" +
		"    }\n" +
		"}\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203588
public void testBug313524_203588() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class Test {\n" +
		"public void a()\n" +
		"{\n" +
		"  if(true)\n" +
		"  {\n" +
		"    allocation.add(idx_ta + 1, Double.valueOf(allocation.get(idx_ta).doubleValue() + q));\n" +
		"  }\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	public void a() {\n" +
		"		if (true) {\n" +
		"			allocation.add(idx_ta + 1, Double.valueOf(allocation.get(idx_ta)\n" +
		"					.doubleValue() + q));\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// wksp1
public void testBug313524_wksp1_01() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X01 {\n" +
		"	private void reportError(String name) throws ParseError {\n" +
		"		throw new ParseError(MessageFormat.format(AntDTDSchemaMessages.getString(\"NfmParser.Ambiguous\"), new String[]{name})); //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	private void reportError(String name) throws ParseError {\n" +
		"		throw new ParseError(MessageFormat.format(AntDTDSchemaMessages\n" +
		"				.getString(\"NfmParser.Ambiguous\"), new String[] { name })); //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_02() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X02 {\n" +
		"	private void parseBuildFile(Project project) {\n" +
		"		if (!buildFile.exists()) {\n" +
		"			throw new BuildException(MessageFormat.format(InternalAntMessages.getString(\"InternalAntRunner.Buildfile__{0}_does_not_exist_!_1\"), //$NON-NLS-1$\n" +
		"						 new String[]{buildFile.getAbsolutePath()}));\n" +
		"		}\n" +
		"		if (!buildFile.isFile()) {\n" +
		"			throw new BuildException(MessageFormat.format(InternalAntMessages.getString(\"InternalAntRunner.Buildfile__{0}_is_not_a_file_1\"), //$NON-NLS-1$\n" +
		"							new String[]{buildFile.getAbsolutePath()}));\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	private void parseBuildFile(Project project) {\n" +
		"		if (!buildFile.exists()) {\n" +
		"			throw new BuildException(MessageFormat.format(InternalAntMessages\n" +
		"					.getString(\n" +
		"							\"InternalAntRunner.Buildfile__{0}_does_not_exist_!_1\"), //$NON-NLS-1$\n" +
		"					new String[] { buildFile.getAbsolutePath() }));\n" +
		"		}\n" +
		"		if (!buildFile.isFile()) {\n" +
		"			throw new BuildException(MessageFormat.format(InternalAntMessages\n" +
		"					.getString(\n" +
		"							\"InternalAntRunner.Buildfile__{0}_is_not_a_file_1\"), //$NON-NLS-1$\n" +
		"					new String[] { buildFile.getAbsolutePath() }));\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_03() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X03 {\n" +
		"\n" +
		"	protected void foo() {\n" +
		"		printTargets(project, subNames, null, InternalAntMessages.getString(\"InternalAntRunner.Subtargets__5\"), 0); //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X03 {\n" +
		"\n" +
		"	protected void foo() {\n" +
		"		printTargets(project, subNames, null, InternalAntMessages.getString(\n" +
		"				\"InternalAntRunner.Subtargets__5\"), 0); //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_04() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X04 {\n" +
		"	void foo() {\n" +
		"		if (AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {\n" +
		"			synchronizeOutlinePage(node, true);\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X04 {\n" +
		"	void foo() {\n" +
		"		if (AntUIPlugin.getDefault().getPreferenceStore().getBoolean(\n" +
		"				IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {\n" +
		"			synchronizeOutlinePage(node, true);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_05() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	String source =
		"public class X05 {\n" +
		"void foo() {\n" +
		"		if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {\n" +
		"		}\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X05 {\n" +
		"	void foo() {\n" +
		"		if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(\n" +
		"				AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// TODO Improve this formatting as it let the message send argument in one line over the max width
public void testBug313524_wksp1_06() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X06 {\n" +
		"	public void launch() {\n" +
		"		try {\n" +
		"			if ((javaProject == null) || !javaProject.exists()) {\n" +
		"				abort(PDEPlugin________.getResourceString(\"JUnitLaunchConfig_____\"), null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);\n" +
		"			}\n" +
		"		} catch (CoreException e) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X06 {\n" +
		"	public void launch() {\n" +
		"		try {\n" +
		"			if ((javaProject == null) || !javaProject.exists()) {\n" +
		"				abort(PDEPlugin________.getResourceString(\n" +
		"						\"JUnitLaunchConfig_____\"), null,\n" +
		"						IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);\n" +
		"			}\n" +
		"		} catch (CoreException e) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_07() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X07 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			configureAntObject(result, element, task, task.getTaskName(), InternalCoreAntMessages.getString(\"AntCorePreferences.No_library_for_task\")); //$NON-NLS-1$\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X07 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			configureAntObject(result, element, task, task.getTaskName(),\n" +
		"					InternalCoreAntMessages.getString(\n" +
		"							\"AntCorePreferences.No_library_for_task\")); //$NON-NLS-1$\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_08() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X08 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			IStatus status= new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_RUNNING_BUILD, MessageFormat.format(InternalCoreAntMessages.getString(\"AntRunner.Already_in_progess\"), new String[]{buildFileLocation}), null); //$NON-NLS-1$\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X08 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE,\n" +
		"					AntCorePlugin.ERROR_RUNNING_BUILD, MessageFormat.format(\n" +
		"							InternalCoreAntMessages.getString(\n" +
		"									\"AntRunner.Already_in_progess\"), //$NON-NLS-1$\n" +
		"							new String[] { buildFileLocation }), null);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_09() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X09 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			String secondFileName = secondDirectoryAbsolutePath + File.separator + currentFile.substring(firstDirectoryAbsolutePath.length() + 1);\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X09 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			String secondFileName = secondDirectoryAbsolutePath + File.separator\n" +
		"					+ currentFile.substring(firstDirectoryAbsolutePath.length()\n" +
		"							+ 1);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_10() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X10 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			if (true) {\n" +
		"				throw new BuildException(InternalAntMessages.getString(\"InternalAntRunner.Could_not_load_the_version_information._10\")); //$NON-NLS-1$\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X10 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			if (true) {\n" +
		"				throw new BuildException(InternalAntMessages.getString(\n" +
		"						\"InternalAntRunner.Could_not_load_the_version_information._10\")); //$NON-NLS-1$\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_11() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X11 {\n" +
		"	private void antFileNotFound() {\n" +
		"		reportError(AntLaunchConfigurationMessages.getString(\"AntLaunchShortcut.Unable\"), null); //$NON-NLS-1$	\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X11 {\n" +
		"	private void antFileNotFound() {\n" +
		"		reportError(AntLaunchConfigurationMessages.getString(\n" +
		"				\"AntLaunchShortcut.Unable\"), null); //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug313524_wksp1_12() throws JavaModelException {
	this.formatterPrefs.wrap_outer_expressions_when_nested = false;
	setPageWidth80();
	String source =
		"public class X12 {\n" +
		"	void foo() {\n" +
		"        if (this.fTests.size() == 0) {\n" +
		"            this.addTest(TestSuite\n" +
		"                    .warning(\"No tests found in \" + theClass.getName())); //$NON-NLS-1$\n" +
		"        }\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X12 {\n" +
		"	void foo() {\n" +
		"		if (this.fTests.size() == 0) {\n" +
		"			this.addTest(TestSuite.warning(\"No tests found in \" + theClass //$NON-NLS-1$\n" +
		"					.getName()));\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 317039: [formatter] Code Formatter fails on inner class source indentation
 * @test Ensure formatter is stable when 'Never Join Lines' preference is checked
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=317039"
 */
public void testBug317039_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class X01\n" +
		"  {\n" +
		"\n" +
		"    public void innerThread()\n" +
		"      {\n" +
		"\n" +
		"        new Thread(new Runnable()\n" +
		"          {\n" +
		"            @Override\n" +
		"            public void run()\n" +
		"              {\n" +
		"                // TODO Auto-generated method stub\n" +
		"                }\n" +
		"            }).start();\n" +
		"        }\n" +
		"    }\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"	public void innerThread() {\n" +
		"\n" +
		"		new Thread(new Runnable() {\n" +
		"			@Override\n" +
		"			public void run() {\n" +
		"				// TODO Auto-generated method stub\n" +
		"			}\n" +
		"		}).start();\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 320754: [formatter] formatter:off/on tags does not work correctly
 * @test Ensure disabling/enabling tags work properly around annotations
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=320754"
 */
public void testBug320754_00() throws JavaModelException {
	String source =
		"public class X00\n" +
		"{\n" +
		"    public static void main(String[] args)\n" +
		"    {\n" +
		"        int a=0;int b;\n" +
		"\n" +
		"        System.out.println(a);\n" +
		"\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class X00 {\n" +
		"	public static void main(String[] args) {\n" +
		"		int a = 0;\n" +
		"		int b;\n" +
		"\n" +
		"		System.out.println(a);\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug320754_01a() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"//@formatter:off\n" +
		"//@formatter:on\n" +
		"public class X01a\n" +
		"{\n" +
		"    public static void main(String[] args)\n" +
		"    {\n" +
		"        int a=0;int b;\n" +
		"\n" +
		"        System.out.println(a);\n" +
		"\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"//@formatter:off\n" +
		"//@formatter:on\n" +
		"public class X01a {\n" +
		"	public static void main(String[] args) {\n" +
		"		int a = 0;\n" +
		"		int b;\n" +
		"\n" +
		"		System.out.println(a);\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug320754_01b() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"/* @formatter:off */\n" +
		"/* @formatter:on */\n" +
		"public class X01b\n" +
		"{\n" +
		"    public static void main(String[] args)\n" +
		"    {\n" +
		"        int a=0;int b;\n" +
		"\n" +
		"        System.out.println(a);\n" +
		"\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"/* @formatter:off */\n" +
		"/* @formatter:on */\n" +
		"public class X01b {\n" +
		"	public static void main(String[] args) {\n" +
		"		int a = 0;\n" +
		"		int b;\n" +
		"\n" +
		"		System.out.println(a);\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug320754_01c() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"/** @formatter:off */\n" +
		"/** @formatter:on */\n" +
		"public class X01c\n" +
		"{\n" +
		"    public static void main(String[] args)\n" +
		"    {\n" +
		"        int a=0;int b;\n" +
		"\n" +
		"        System.out.println(a);\n" +
		"\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"/** @formatter:off */\n" +
		"/** @formatter:on */\n" +
		"public class X01c {\n" +
		"	public static void main(String[] args) {\n" +
		"		int a = 0;\n" +
		"		int b;\n" +
		"\n" +
		"		System.out.println(a);\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug320754_02a() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"//@formatter:off\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"//@formatter:on\n" +
		"public class X02\n" +
		"{\n" +
		"    public static void main(String[] args)\n" +
		"    {\n" +
		"        int a=0;int b;\n" +
		"\n" +
		"        System.out.println(a);\n" +
		"\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"//@formatter:off\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"//@formatter:on\n" +
		"public class X02 {\n" +
		"	public static void main(String[] args) {\n" +
		"		int a = 0;\n" +
		"		int b;\n" +
		"\n" +
		"		System.out.println(a);\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug320754_02b() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"/* @formatter:off */\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"/* @formatter:on */\n" +
		"public class X02b\n" +
		"{\n" +
		"    public static void main(String[] args)\n" +
		"    {\n" +
		"        int a=0;int b;\n" +
		"\n" +
		"        System.out.println(a);\n" +
		"\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"/* @formatter:off */\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"/* @formatter:on */\n" +
		"public class X02b {\n" +
		"	public static void main(String[] args) {\n" +
		"		int a = 0;\n" +
		"		int b;\n" +
		"\n" +
		"		System.out.println(a);\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug320754_02c() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"/** @formatter:off */\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"/** @formatter:on */\n" +
		"public class X02c\n" +
		"{\n" +
		"    public static void main(String[] args)\n" +
		"    {\n" +
		"        int a=0;int b;\n" +
		"\n" +
		"        System.out.println(a);\n" +
		"\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"/** @formatter:off */\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"/** @formatter:on */\n" +
		"public class X02c {\n" +
		"	public static void main(String[] args) {\n" +
		"		int a = 0;\n" +
		"		int b;\n" +
		"\n" +
		"		System.out.println(a);\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug320754_02d() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"//@formatter:off\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"\n" +
		"//@formatter:on\n" +
		"public class X02d\n" +
		"{\n" +
		"    public static void main(String[] args)\n" +
		"    {\n" +
		"        int a=0;int b;\n" +
		"\n" +
		"        System.out.println(a);\n" +
		"\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"//@formatter:off\n" +
		"@MyAnnot (\n" +
		"  testAttribute = {\"test1\", \"test2\", \"test3\"}\n" +
		")\n" +
		"\n" +
		"//@formatter:on\n" +
		"public class X02d {\n" +
		"	public static void main(String[] args) {\n" +
		"		int a = 0;\n" +
		"		int b;\n" +
		"\n" +
		"		System.out.println(a);\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug320754_03() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"//@formatter:off\n" +
		"\n" +
		"//@formatter:on\n" +
		"public class X03\n" +
		"{\n" +
		"    public static void main(String[] args)\n" +
		"    {\n" +
		"        int a=0;int b;\n" +
		"\n" +
		"        System.out.println(a);\n" +
		"\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"//@formatter:off\n" +
		"\n" +
		"//@formatter:on\n" +
		"public class X03 {\n" +
		"	public static void main(String[] args) {\n" +
		"		int a = 0;\n" +
		"		int b;\n" +
		"\n" +
		"		System.out.println(a);\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 328240: org.eclipse.text.edits.MalformedTreeException: Overlapping text edits
 * @test Ensure that no exception occurs while formatting the given sample
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=328240"
 */
public void testBug328240() {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"package com.example;\n" +
		"\n" +
		"public class FormatterError {\n" +
		"\n" +
		"	/**\n" +
		"	 * Create a paragraph element suited to be a header of the report. Headers\n" +
		"	 * are the elements such as \"created by\" on \"created on\" that appear\n" +
		"	 * underneath the title.\n" +
		"	 * \n" +
		"	 * @param reportHeader\n" +
		"	 *            a <code>String</coe> value that will be the text of\n" +
		"\n" +
		"* the paragraph.\n" +
		"	 * @return a <code>Paragraph</code> containing the the text passed as the\n" +
		"	 *         reportHeader parameter.\n" +
		"	 */\n" +
		"\n" +
		"	public static String createReportHeader(String reportHeader) {\n" +
		"		return reportHeader;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package com.example;\n" +
		"\n" +
		"public class FormatterError {\n" +
		"\n" +
		"	/**\n" +
		"	 * Create a paragraph element suited to be a header of the report. Headers\n" +
		"	 * are the elements such as \"created by\" on \"created on\" that appear\n" +
		"	 * underneath the title.\n" +
		"	 * \n" +
		"	 * @param reportHeader\n" +
		"	 *            a <code>String</coe> value that will be the text of\n" +
		"	\n" +
		"	* the paragraph.\n" +
		"	 * @return a <code>Paragraph</code> containing the the text passed as the\n" +
		"	 *            reportHeader parameter.\n" +
		"	 */\n" +
		"\n" +
		"	public static String createReportHeader(String reportHeader) {\n" +
		"		return reportHeader;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 328362: [formatter] Format regions does not format as expected
 * @test Ensure that the given regions are well formatted
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=328362"
 */
public void testBug328362() throws Exception {
	String source =
		"package test1;\n" +
		"\n" +
		"[#    class  A {#]\n" +
		"\n" +
		"[#        int  i;#]\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"class A {\n" +
		"\n" +
		"	int i;\n" +
		"\n" +
		"}\n"
	);
}

/**
 * @bug 330313: [formatter] 'Never join already wrapped lines' formatter option does correctly indent
 * @test Ensure that indentation is correct when 'Never join already wrapped lines' is set
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=330313"
 */
public void testBug330313() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"	private void helper2(\n" +
		"			 boolean[] booleans) {\n" +
		"		if (booleans[0]) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	private void helper2(\n" +
		"			boolean[] booleans) {\n" +
		"		if (booleans[0]) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313a() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"	private void helper2(\n" +
		"			boolean[] booleans) {\n" +
		"		if (booleans[0]) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source);
}
public void testBug330313b() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"	private void helper2(\n" +
		"                boolean[] booleans) {\n" +
		"		if (booleans[0]) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	private void helper2(\n" +
		"			boolean[] booleans) {\n" +
		"		if (booleans[0]) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313c() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"	private void helper2(\n" +
		"boolean[] booleans) {\n" +
		"		if (booleans[0]) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	private void helper2(\n" +
		"			boolean[] booleans) {\n" +
		"		if (booleans[0]) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313d() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"	private void helper2(\n" +
		"			boolean[] booleans) {\n" +
		"		if (booleans[0]) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	private void helper2(\n" +
		"			boolean[] booleans) {\n" +
		"		if (booleans[0]) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_regression_187a() {
	setPageWidth80();
	String source =
		"import java.io.File;\n" +
		"\n" +
		"public class RegressionTest_187 {\n" +
		"\n" +
		"	private String createC42PDFCommandLine(String documentName) {\n" +
		"		return (Registry.getConvertToolPath() + File.separator +\n" +
		"			   Registry.getConvertToolName() +\n" +
		"			   \" -o \" + _workingDir + File.separator + documentName +\n" +
		"			   \" -l \" + _workingDir + File.separator + _fileList);\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"import java.io.File;\n" +
		"\n" +
		"public class RegressionTest_187 {\n" +
		"\n" +
		"	private String createC42PDFCommandLine(String documentName) {\n" +
		"		return (Registry.getConvertToolPath() + File.separator\n" +
		"				+ Registry.getConvertToolName() + \" -o \" + _workingDir\n" +
		"				+ File.separator + documentName + \" -l \" + _workingDir\n" +
		"				+ File.separator + _fileList);\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_regression_187b() {
	setPageWidth80();
	String source =
		"import java.io.File;\n" +
		"\n" +
		"public class RegressionTest_187 {\n" +
		"\n" +
		"	private String createC42PDFCommandLine(String documentName) {\n" +
		"		return (Registry.getConvertToolPath() + File.separator +\n" +
		"			   Registry.getConvertToolName() +\n" +
		"			   (\" -o \" + _workingDir + File.separator + documentName +\n" +
		"			   (\" -l \" + _workingDir + File.separator + _fileList)));\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"import java.io.File;\n" +
		"\n" +
		"public class RegressionTest_187 {\n" +
		"\n" +
		"	private String createC42PDFCommandLine(String documentName) {\n" +
		"		return (Registry.getConvertToolPath() + File.separator\n" +
		"				+ Registry.getConvertToolName()\n" +
		"				+ (\" -o \" + _workingDir + File.separator + documentName\n" +
		"						+ (\" -l \" + _workingDir + File.separator + _fileList)));\n" +
		"	}\n" +
		"}\n"
	);
}
//static { TESTS_PREFIX = "testBug330313_wksp1"; }
public void testBug330313_wksp1_01_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"    protected String getPrefixFromDocument(String aDocumentText, int anOffset) {\n" +
		"        int startOfWordToken = anOffset;\n" +
		"        \n" +
		"        char token= \'a\';\n" +
		"        if (startOfWordToken > 0) {\n" +
		"			token= aDocumentText.charAt(startOfWordToken - 1);\n" +
		"        }\n" +
		"        \n" +
		"        while (startOfWordToken > 0 \n" +
		"                && (Character.isJavaIdentifierPart(token) \n" +
		"                    || \'.\' == token\n" +
		"					|| \'-\' == token\n" +
		"        			|| \';\' == token)\n" +
		"                && !(\'$\' == token)) {\n" +
		"            startOfWordToken--;\n" +
		"            if (startOfWordToken == 0) {\n" +
		"            	break; //word goes right to the beginning of the doc\n" +
		"            }\n" +
		"			token= aDocumentText.charAt(startOfWordToken - 1);\n" +
		"        }\n" +
		"        return \"\";\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	protected String getPrefixFromDocument(String aDocumentText, int anOffset) {\n" +
		"		int startOfWordToken = anOffset;\n" +
		"\n" +
		"		char token = \'a\';\n" +
		"		if (startOfWordToken > 0) {\n" +
		"			token = aDocumentText.charAt(startOfWordToken - 1);\n" +
		"		}\n" +
		"\n" +
		"		while (startOfWordToken > 0\n" +
		"				&& (Character.isJavaIdentifierPart(token)\n" +
		"						|| \'.\' == token\n" +
		"						|| \'-\' == token\n" +
		"						|| \';\' == token)\n" +
		"				&& !(\'$\' == token)) {\n" +
		"			startOfWordToken--;\n" +
		"			if (startOfWordToken == 0) {\n" +
		"				break; // word goes right to the beginning of the doc\n" +
		"			}\n" +
		"			token = aDocumentText.charAt(startOfWordToken - 1);\n" +
		"		}\n" +
		"		return \"\";\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_02_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"  public void testMethod(String currentTokenVal,\n" +
		"                         int[][] expectedTokenSequencesVal,\n" +
		"                         String[] tokenImageVal\n" +
		"                        )\n" +
		"  {\n" +
		"  }\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X02 {\n" +
		"	public void testMethod(String currentTokenVal,\n" +
		"			int[][] expectedTokenSequencesVal,\n" +
		"			String[] tokenImageVal) {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_03_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X03 {\n" +
		"\n" +
		"	void foo() {\n" +
		"			if (declaringClass.isNestedType()){\n" +
		"				NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;\n" +
		"				this.scope.extraSyntheticArguments = nestedType.syntheticOuterLocalVariables();\n" +
		"				scope.computeLocalVariablePositions(// consider synthetic arguments if any\n" +
		"					nestedType.enclosingInstancesSlotSize + 1,\n" +
		"					codeStream);\n" +
		"				argSlotSize += nestedType.enclosingInstancesSlotSize;\n" +
		"				argSlotSize += nestedType.outerLocalVariablesSlotSize;\n" +
		"			} else {\n" +
		"				scope.computeLocalVariablePositions(1,  codeStream);\n" +
		"			}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X03 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		if (declaringClass.isNestedType()) {\n" +
		"			NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;\n" +
		"			this.scope.extraSyntheticArguments = nestedType\n" +
		"					.syntheticOuterLocalVariables();\n" +
		"			scope.computeLocalVariablePositions(// consider synthetic arguments\n" +
		"												// if any\n" +
		"					nestedType.enclosingInstancesSlotSize + 1,\n" +
		"					codeStream);\n" +
		"			argSlotSize += nestedType.enclosingInstancesSlotSize;\n" +
		"			argSlotSize += nestedType.outerLocalVariablesSlotSize;\n" +
		"		} else {\n" +
		"			scope.computeLocalVariablePositions(1, codeStream);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_04() {
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		for (;;) {\n" +
		"			if (act <= NUM_RULES) {               // reduce action\n" +
		"				tempStackTop--;\n" +
		"			} else if (act < ACCEPT_ACTION ||     // shift action\n" +
		"					 act > ERROR_ACTION) {        // shift-reduce action\n" +
		"				if (indx == MAX_DISTANCE)\n" +
		"					return indx;\n" +
		"				indx++;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		for (;;) {\n" +
		"			if (act <= NUM_RULES) { // reduce action\n" +
		"				tempStackTop--;\n" +
		"			} else if (act < ACCEPT_ACTION || // shift action\n" +
		"					act > ERROR_ACTION) { // shift-reduce action\n" +
		"				if (indx == MAX_DISTANCE)\n" +
		"					return indx;\n" +
		"				indx++;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_04_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		for (;;) {\n" +
		"			if (act <= NUM_RULES) {               // reduce action\n" +
		"				tempStackTop--;\n" +
		"			} else if (act < ACCEPT_ACTION ||     // shift action\n" +
		"					 act > ERROR_ACTION) {        // shift-reduce action\n" +
		"				if (indx == MAX_DISTANCE)\n" +
		"					return indx;\n" +
		"				indx++;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		for (;;) {\n" +
		"			if (act <= NUM_RULES) { // reduce action\n" +
		"				tempStackTop--;\n" +
		"			} else if (act < ACCEPT_ACTION || // shift action\n" +
		"					act > ERROR_ACTION) { // shift-reduce action\n" +
		"				if (indx == MAX_DISTANCE)\n" +
		"					return indx;\n" +
		"				indx++;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_05_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X05 {\n" +
		"\n" +
		"	private void foo() {\n" +
		"		setBuildFileLocation.invoke(runner, new Object[] { buildFileLocation });\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X05\n" +
		"{\n" +
		"\n" +
		"	private void foo()\n" +
		"	{\n" +
		"		setBuildFileLocation.invoke(runner, new Object[]\n" +
		"		{ buildFileLocation });\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_06_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X06 {\n" +
		"\n" +
		"	public void foo(Object index) {\n" +
		"\n" +
		"		try {\n" +
		"			index = this.manager.getIndexForUpdate(this.containerPath, true, /*reuse index file*/ true /*create if none*/);\n" +
		"		}\n" +
		"		finally {}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X06\n" +
		"{\n" +
		"\n" +
		"	public void foo(Object index)\n" +
		"	{\n" +
		"\n" +
		"		try\n" +
		"		{\n" +
		"			index = this.manager.getIndexForUpdate(this.containerPath, true,\n" +
		"					/* reuse index file */ true /* create if none */);\n" +
		"		} finally\n" +
		"		{\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_07() {
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X07 {\n" +
		"\n" +
		"static final long[] jjtoToken = {\n" +
		"   0x7fbfecffL, \n" +
		"};\n" +
		"static final long[] jjtoSkip = {\n" +
		"   0x400000L, \n" +
		"};\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X07 {\n" +
		"\n" +
		"	static final long[] jjtoToken = { 0x7fbfecffL, };\n" +
		"	static final long[] jjtoSkip = { 0x400000L, };\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_07_bnl() {
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X07 {\n" +
		"\n" +
		"static final long[] jjtoToken = {\n" +
		"   0x7fbfecffL, \n" +
		"};\n" +
		"static final long[] jjtoSkip = {\n" +
		"   0x400000L, \n" +
		"};\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X07\n" +
		"{\n" +
		"\n" +
		"	static final long[] jjtoToken =\n" +
		"	{ 0x7fbfecffL, };\n" +
		"	static final long[] jjtoSkip =\n" +
		"	{ 0x400000L, };\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_07_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X07 {\n" +
		"\n" +
		"static final long[] jjtoToken = {\n" +
		"   0x7fbfecffL, \n" +
		"};\n" +
		"static final long[] jjtoSkip = {\n" +
		"   0x400000L, \n" +
		"};\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X07 {\n" +
		"\n" +
		"	static final long[] jjtoToken = {\n" +
		"			0x7fbfecffL,\n" +
		"	};\n" +
		"	static final long[] jjtoSkip = {\n" +
		"			0x400000L,\n" +
		"	};\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_07_njl_bnl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X07 {\n" +
		"\n" +
		"static final long[] jjtoToken = {\n" +
		"   0x7fbfecffL, \n" +
		"};\n" +
		"static final long[] jjtoSkip = {\n" +
		"   0x400000L, \n" +
		"};\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X07\n" +
		"{\n" +
		"\n" +
		"	static final long[] jjtoToken =\n" +
		"	{\n" +
		"			0x7fbfecffL,\n" +
		"	};\n" +
		"	static final long[] jjtoSkip =\n" +
		"	{\n" +
		"			0x400000L,\n" +
		"	};\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_08_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_assignment = Alignment.M_COMPACT_SPLIT;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X08 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		MinimizedFileSystemElement dummyParent =\n" +
		"			new MinimizedFileSystemElement(\"\", null, true);//$NON-NLS-1$\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X08 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		MinimizedFileSystemElement dummyParent =\n" +
		"				new MinimizedFileSystemElement(\"\", null, true);//$NON-NLS-1$\n" +
		"	}\n" +
		"}\n"
	);
}
// testCompare1159_1: org.eclipse.debug.internal.ui.DebugUIPropertiesAdapterFactory
public void testBug330313_wksp1_09_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X09 {\n" +
		"	public Class[] getAdapterList() {\n" +
		"		return new Class[] {\n" +
		"			IWorkbenchAdapter.class\n" +
		"		};\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X09 {\n" +
		"	public Class[] getAdapterList() {\n" +
		"		return new Class[] {\n" +
		"				IWorkbenchAdapter.class\n" +
		"		};\n" +
		"	}\n" +
		"}\n"
	);
}
// testCompare1723_1: org.eclipse.jdt.internal.compiler.ast.DoubleLiteral
public void testBug330313_wksp1_10_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X10 {\n" +
		"\n" +
		"public void computeConstant() {\n" +
		"\n" +
		"	if (true)\n" +
		"	{	//only a true 0 can be made of zeros\n" +
		"		//2.00000000000000000e-324 is illegal .... \n" +
		"	}}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X10 {\n" +
		"\n" +
		"	public void computeConstant() {\n" +
		"\n" +
		"		if (true) { // only a true 0 can be made of zeros\n" +
		"					// 2.00000000000000000e-324 is illegal ....\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// testCompare1794_1: org.eclipse.jdt.internal.compiler.ast.ClassFile
public void testBug330313_wksp1_11_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X11 {\n" +
		"	X11() {\n" +
		"		accessFlags\n" +
		"			&= ~(\n" +
		"				AccStrictfp\n" +
		"					| AccProtected\n" +
		"					| AccPrivate\n" +
		"					| AccStatic\n" +
		"					| AccSynchronized\n" +
		"					| AccNative);\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X11 {\n" +
		"	X11() {\n" +
		"		accessFlags &= ~(AccStrictfp\n" +
		"				| AccProtected\n" +
		"				| AccPrivate\n" +
		"				| AccStatic\n" +
		"				| AccSynchronized\n" +
		"				| AccNative);\n" +
		"	}\n" +
		"}\n"
	);
}
// rg.eclipse.ant.ui/Ant Editor/org/eclipse/ant/internal/ui/editor/formatter/XmlFormatter.java
public void testBug330313_wksp1_12() {
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X12 {\n" +
		"\n" +
		"    private static Document createDocument(String string, Position[] positions) throws IllegalArgumentException {\n" +
		"		Document doc= new Document(string);\n" +
		"		try {\n" +
		"			if (positions != null) {\n" +
		"				doc.addPositionUpdater(new DefaultPositionUpdater(POS_CATEGORY) {\n" +
		"					protected boolean notDeleted() {\n" +
		"						if (fOffset < fPosition.offset && (fPosition.offset + fPosition.length < fOffset + fLength)) {\n" +
		"							return false;\n" +
		"						}\n" +
		"						return true;\n" +
		"					}\n" +
		"				});\n" +
		"			}\n" +
		"		} catch (BadPositionCategoryException cannotHappen) {\n" +
		"			// can not happen: category is correctly set up\n" +
		"		}\n" +
		"		return doc;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X12 {\n" +
		"\n" +
		"	private static Document createDocument(String string, Position[] positions)\n" +
		"			throws IllegalArgumentException {\n" +
		"		Document doc = new Document(string);\n" +
		"		try {\n" +
		"			if (positions != null) {\n" +
		"				doc.addPositionUpdater(\n" +
		"						new DefaultPositionUpdater(POS_CATEGORY) {\n" +
		"							protected boolean notDeleted() {\n" +
		"								if (fOffset < fPosition.offset\n" +
		"										&& (fPosition.offset\n" +
		"												+ fPosition.length < fOffset\n" +
		"														+ fLength)) {\n" +
		"									return false;\n" +
		"								}\n" +
		"								return true;\n" +
		"							}\n" +
		"						});\n" +
		"			}\n" +
		"		} catch (BadPositionCategoryException cannotHappen) {\n" +
		"			// can not happen: category is correctly set up\n" +
		"		}\n" +
		"		return doc;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_12_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X12 {\n" +
		"\n" +
		"    private static Document createDocument(String string, Position[] positions) throws IllegalArgumentException {\n" +
		"		Document doc= new Document(string);\n" +
		"		try {\n" +
		"			if (positions != null) {\n" +
		"				doc.addPositionUpdater(new DefaultPositionUpdater(POS_CATEGORY) {\n" +
		"					protected boolean notDeleted() {\n" +
		"						if (fOffset < fPosition.offset && (fPosition.offset + fPosition.length < fOffset + fLength)) {\n" +
		"							return false;\n" +
		"						}\n" +
		"						return true;\n" +
		"					}\n" +
		"				});\n" +
		"			}\n" +
		"		} catch (BadPositionCategoryException cannotHappen) {\n" +
		"			// can not happen: category is correctly set up\n" +
		"		}\n" +
		"		return doc;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X12 {\n" +
		"\n" +
		"	private static Document createDocument(String string, Position[] positions)\n" +
		"			throws IllegalArgumentException {\n" +
		"		Document doc = new Document(string);\n" +
		"		try {\n" +
		"			if (positions != null) {\n" +
		"				doc.addPositionUpdater(\n" +
		"						new DefaultPositionUpdater(POS_CATEGORY) {\n" +
		"							protected boolean notDeleted() {\n" +
		"								if (fOffset < fPosition.offset\n" +
		"										&& (fPosition.offset\n" +
		"												+ fPosition.length < fOffset\n" +
		"														+ fLength)) {\n" +
		"									return false;\n" +
		"								}\n" +
		"								return true;\n" +
		"							}\n" +
		"						});\n" +
		"			}\n" +
		"		} catch (BadPositionCategoryException cannotHappen) {\n" +
		"			// can not happen: category is correctly set up\n" +
		"		}\n" +
		"		return doc;\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from org.eclipse.ant.ui/org/eclipse/core/internal/dtree/NodeInfo.java
public void testBug330313_wksp1_13() {
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X13 {\n" +
		"\n" +
		"	public boolean isEmptyDelta() {\n" +
		"		return (this.getType() == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE && this.getNamesOfChildren().length == 0 && this.getNamesOfDeletedChildren().length == 0);\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X13 {\n" +
		"\n" +
		"	public boolean isEmptyDelta() {\n" +
		"		return (this.getType() == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE\n" +
		"				&& this.getNamesOfChildren().length == 0\n" +
		"				&& this.getNamesOfDeletedChildren().length == 0);\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_13_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X13 {\n" +
		"\n" +
		"	public boolean isEmptyDelta() {\n" +
		"		return (this.getType() == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE && this.getNamesOfChildren().length == 0 && this.getNamesOfDeletedChildren().length == 0);\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X13 {\n" +
		"\n" +
		"	public boolean isEmptyDelta() {\n" +
		"		return (this.getType() == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE\n" +
		"				&& this.getNamesOfChildren().length == 0\n" +
		"				&& this.getNamesOfDeletedChildren().length == 0);\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from org.eclipse.jdt.core/org/eclipse/jdt/internal/compiler/ast/SingleNameReference.java
public void testBug330313_wksp1_14() {
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X14 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			if (((bits & DepthMASK) != 0)\n" +
		"				&& (fieldBinding.isPrivate() // private access\n" +
		"					|| (fieldBinding.isProtected() // implicit protected access\n" +
		"							&& fieldBinding.declaringClass.getPackage() \n" +
		"								!= currentScope.enclosingSourceType().getPackage()))) {\n" +
		"				return;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X14 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			if (((bits & DepthMASK) != 0) && (fieldBinding.isPrivate() // private\n" +
		"																		// access\n" +
		"					|| (fieldBinding.isProtected() // implicit protected access\n" +
		"							&& fieldBinding.declaringClass\n" +
		"									.getPackage() != currentScope\n" +
		"											.enclosingSourceType()\n" +
		"											.getPackage()))) {\n" +
		"				return;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_14_njl() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X14 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			if (((bits & DepthMASK) != 0)\n" +
		"				&& (fieldBinding.isPrivate() // private access\n" +
		"					|| (fieldBinding.isProtected() // implicit protected access\n" +
		"							&& fieldBinding.declaringClass.getPackage() \n" +
		"								!= currentScope.enclosingSourceType().getPackage()))) {\n" +
		"				return;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X14 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			if (((bits & DepthMASK) != 0)\n" +
		"					&& (fieldBinding.isPrivate() // private access\n" +
		"							|| (fieldBinding.isProtected() // implicit protected\n" +
		"															// access\n" +
		"									&& fieldBinding.declaringClass.getPackage() != currentScope.enclosingSourceType()\n" +
		"											.getPackage()))) {\n" +
		"				return;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from org.eclipse.jdt.core/org/eclipse/jdt/internal/compiler/ast/SingleNameReference.java
public void testBug330313_wksp1_15_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X15 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			if (fieldBinding.declaringClass != this.actualReceiverType\n" +
		"				&& !this.actualReceiverType.isArrayType()	\n" +
		"				&& fieldBinding.declaringClass != null\n" +
		"				&& fieldBinding.constant == NotAConstant\n" +
		"				&& ((currentScope.environment().options.targetJDK >= ClassFileConstants.JDK1_2 \n" +
		"						&& !fieldBinding.isStatic()\n" +
		"						&& fieldBinding.declaringClass.id != T_Object) // no change for Object fields (if there was any)\n" +
		"					|| !fieldBinding.declaringClass.canBeSeenBy(currentScope))){\n" +
		"				this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)this.actualReceiverType);\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X15 {\n" +
		"	public void foo() {\n" +
		"		if (true) {\n" +
		"			if (fieldBinding.declaringClass != this.actualReceiverType\n" +
		"					&& !this.actualReceiverType.isArrayType()\n" +
		"					&& fieldBinding.declaringClass != null\n" +
		"					&& fieldBinding.constant == NotAConstant\n" +
		"					&& ((currentScope\n" +
		"							.environment().options.targetJDK >= ClassFileConstants.JDK1_2\n" +
		"							&& !fieldBinding.isStatic()\n" +
		"							&& fieldBinding.declaringClass.id != T_Object) // no\n" +
		"																			// change\n" +
		"																			// for\n" +
		"																			// Object\n" +
		"																			// fields\n" +
		"																			// (if\n" +
		"																			// there\n" +
		"																			// was\n" +
		"																			// any)\n" +
		"							|| !fieldBinding.declaringClass\n" +
		"									.canBeSeenBy(currentScope))) {\n" +
		"				this.codegenBinding = currentScope.enclosingSourceType()\n" +
		"						.getUpdatedFieldBinding(fieldBinding,\n" +
		"								(ReferenceBinding) this.actualReceiverType);\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case 1941_1 (extracted from org.eclipse.jdt.core/org/eclipse/jdt/internal/compiler/ast/Parser.java)
public void testBug330313_wksp1_16() {
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X16 {\n" +
		"void foo() {\n" +
		"	// recovery\n" +
		"	if (this.currentElement != null) {\n" +
		"		if (!(this.currentElement instanceof RecoveredType)\n" +
		"			&& (this.currentToken == TokenNameDOT\n" +
		"				//|| declaration.modifiers != 0\n" +
		"				|| (this.scanner.getLineNumber(declaration.type.sourceStart)\n" +
		"						!= this.scanner.getLineNumber((int) (namePosition >>> 32))))){\n" +
		"			return;\n" +
		"		}\n" +
		"	}\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X16 {\n" +
		"	void foo() {\n" +
		"		// recovery\n" +
		"		if (this.currentElement != null) {\n" +
		"			if (!(this.currentElement instanceof RecoveredType)\n" +
		"					&& (this.currentToken == TokenNameDOT\n" +
		"							// || declaration.modifiers != 0\n" +
		"							|| (this.scanner.getLineNumber(\n" +
		"									declaration.type.sourceStart) != this.scanner\n" +
		"											.getLineNumber(\n" +
		"													(int) (namePosition >>> 32))))) {\n" +
		"				return;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_16_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X16 {\n" +
		"void foo() {\n" +
		"	// recovery\n" +
		"	if (this.currentElement != null) {\n" +
		"		if (!(this.currentElement instanceof RecoveredType)\n" +
		"			&& (this.currentToken == TokenNameDOT\n" +
		"				//|| declaration.modifiers != 0\n" +
		"				|| (this.scanner.getLineNumber(declaration.type.sourceStart)\n" +
		"						!= this.scanner.getLineNumber((int) (namePosition >>> 32))))){\n" +
		"			return;\n" +
		"		}\n" +
		"	}\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public class X16 {\n" +
		"	void foo() {\n" +
		"		// recovery\n" +
		"		if (this.currentElement != null) {\n" +
		"			if (!(this.currentElement instanceof RecoveredType)\n" +
		"					&& (this.currentToken == TokenNameDOT\n" +
		"							// || declaration.modifiers != 0\n" +
		"							|| (this.scanner.getLineNumber(\n" +
		"									declaration.type.sourceStart) != this.scanner\n" +
		"											.getLineNumber(\n" +
		"													(int) (namePosition >>> 32))))) {\n" +
		"				return;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case 1872_1 (extracted from org.eclipse.jdt.core/org/eclipse/jdt/internal/compiler/lookup/BlockScope.java)
public void testBug330313_wksp1_17_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X17 {\n" +
		"	void foo() {\n" +
		"		if ((currentMethodScope = this.methodScope())\n" +
		"			!= outerLocalVariable.declaringScope.methodScope()) {\n" +
		"			return;\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X17 {\n" +
		"	void foo() {\n" +
		"		if ((currentMethodScope = this.methodScope()) != outerLocalVariable.declaringScope.methodScope()) {\n" +
		"			return;\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case 1964_1 (extracted from org.eclipse.jdt.core/org/eclipse/jdt/core/dom/ASTMatcher.java)
public void testBug330313_wksp1_18_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X18 {\n" +
		"	public boolean foo() {\n" +
		"		return (\n" +
		"			safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())\n" +
		"				&& safeSubtreeListMatch(node.modifiers(), o.modifiers())\n" +
		"				&& safeSubtreeMatch(node.getName(), o.getName())\n" +
		"				&& safeSubtreeListMatch(node.arguments(), o.arguments())\n" +
		"				&& safeSubtreeListMatch(\n" +
		"					node.bodyDeclarations(),\n" +
		"					o.bodyDeclarations()));\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X18 {\n" +
		"	public boolean foo() {\n" +
		"		return (safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())\n" +
		"				&& safeSubtreeListMatch(node.modifiers(), o.modifiers())\n" +
		"				&& safeSubtreeMatch(node.getName(), o.getName())\n" +
		"				&& safeSubtreeListMatch(node.arguments(), o.arguments())\n" +
		"				&& safeSubtreeListMatch(\n" +
		"						node.bodyDeclarations(),\n" +
		"						o.bodyDeclarations()));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_19_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X19 {\n" +
		"	public boolean foo() {\n" +
		"		return (\n" +
		"			safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())\n" +
		"				&& safeSubtreeListMatch(node.modifiers(), o.modifiers())\n" +
		"				&& safeSubtreeMatch(node.getName(), o.getName())\n" +
		"				&& safeSubtreeListMatch(node.superInterfaceTypes(), o.superInterfaceTypes())\n" +
		"				&& safeSubtreeListMatch(\n" +
		"					node.bodyDeclarations(),\n" +
		"					o.bodyDeclarations()));\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X19 {\n" +
		"	public boolean foo() {\n" +
		"		return (safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())\n" +
		"				&& safeSubtreeListMatch(node.modifiers(), o.modifiers())\n" +
		"				&& safeSubtreeMatch(node.getName(), o.getName())\n" +
		"				&& safeSubtreeListMatch(node.superInterfaceTypes(),\n" +
		"						o.superInterfaceTypes())\n" +
		"				&& safeSubtreeListMatch(\n" +
		"						node.bodyDeclarations(),\n" +
		"						o.bodyDeclarations()));\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from org.eclipse.debug.ui/ui/org/eclipse/debug/ui/AbstractDebugView.java
public void testBug330313_wksp1_20_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_assignment = Alignment.M_COMPACT_SPLIT;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X20 {\n" +
		"\n" +
		"  static final String decode(String entity) {\n" +
		"    if (true) {\n" +
		"      if (entity.charAt(2) == \'X\' || entity.charAt(2) == \'x\') {\n" +
		"      }\n" +
		"      Character c =\n" +
		"	new Character((char)Integer.parseInt(entity.substring(start), radix));\n" +
		"      return c.toString();\n" +
		"    }\n" +
		"	return \"\";\n" +
		"  }\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X20 {\n" +
		"\n" +
		"	static final String decode(String entity) {\n" +
		"		if (true) {\n" +
		"			if (entity.charAt(2) == \'X\' || entity.charAt(2) == \'x\') {\n" +
		"			}\n" +
		"			Character c =\n" +
		"					new Character((char) Integer\n" +
		"							.parseInt(entity.substring(start), radix));\n" +
		"			return c.toString();\n" +
		"		}\n" +
		"		return \"\";\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from org.apache.lucene/src/org/apache/lucene/demo/html/Entities.java
public void testBug330313_wksp1_21_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X21 {\n" +
		"	public boolean isAvailable() {\n" +
		"		return !(getViewer() == null || getViewer().getControl() == null || getViewer().getControl().isDisposed());\n" +
		"	}	\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X21 {\n" +
		"	public boolean isAvailable() {\n" +
		"		return !(getViewer() == null || getViewer().getControl() == null\n" +
		"				|| getViewer().getControl().isDisposed());\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from differences noticed with patch v27.txt
public void testBug330313_wksp1_22_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X22 {\n" +
		"	public boolean foo() {\n" +
		"		return (\n" +
		"				(node.isInterface() == o.isInterface())\n" +
		"				&& safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())\n" +
		"				&& safeSubtreeMatch(node.getName(), o.getName())\n" +
		"				&& safeSubtreeListMatch(node.bodyDeclarations(), o.bodyDeclarations()));\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X22 {\n" +
		"	public boolean foo() {\n" +
		"		return ((node.isInterface() == o.isInterface())\n" +
		"				&& safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())\n" +
		"				&& safeSubtreeMatch(node.getName(), o.getName())\n" +
		"				&& safeSubtreeListMatch(node.bodyDeclarations(),\n" +
		"						o.bodyDeclarations()));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_23_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X23 {\n" +
		"	void foo() {\n" +
		"		boolean wasError = IMarker.SEVERITY_ERROR == pb.getAttribute(IMarker.SEVERITY,\n" +
		"				IMarker.SEVERITY_ERROR);\n" +
		"	}\n" +
		"}\n";
	formatSource(source	);
}
public void testBug330313_wksp1_24_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X24 {\n" +
		"\n" +
		"	protected boolean canRunEvaluation() {\n" +
		"		// NOTE similar to #canStep, except a quiet suspend state is OK\n" +
		"		try {\n" +
		"			return isSuspendedQuiet() || (isSuspended()\n" +
		"					&& !(isPerformingEvaluation() || isInvokingMethod())\n" +
		"					&& !isStepping()\n" +
		"					&& getTopStackFrame() != null\n" +
		"					&& !getJavaDebugTarget().isPerformingHotCodeReplace());\n" +
		"		} catch (DebugException e) {\n" +
		"			return false;\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source	);
}
public void testBug330313_wksp1_25_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X25 {\n" +
		"\n" +
		"	void unloadIcon(ImageData icon) {\n" +
		"		int sizeImage = (((icon.width * icon.depth + 31) / 32 * 4) +\n" +
		"				((icon.width + 31) / 32 * 4)) * icon.height;\n" +
		"	}\n" +
		"}\n";
	formatSource(source	);
}
public void testBug330313_wksp1_26_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X26 {\n" +
		"\n" +
		"void foo() {\n" +
		"	for (int i = 0; i < data.length; i++) {\n" +
		"		byte s = data[i];\n" +
		"		sourceData[i] = (byte)(((s & 0x80) >> 7) |\n" +
		"			((s & 0x40) >> 5) |\n" +
		"			((s & 0x20) >> 3) |\n" +
		"			((s & 0x10) >> 1) |\n" +
		"			((s & 0x08) << 1) |\n" +
		"			((s & 0x04) << 3) |\n" +
		"			((s & 0x02) << 5) |\n" +
		"			((s & 0x01) << 7));\n" +
		"	}\n" +
		"}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X26 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		for (int i = 0; i < data.length; i++) {\n" +
		"			byte s = data[i];\n" +
		"			sourceData[i] = (byte) (((s & 0x80) >> 7) |\n" +
		"					((s & 0x40) >> 5) |\n" +
		"					((s & 0x20) >> 3) |\n" +
		"					((s & 0x10) >> 1) |\n" +
		"					((s & 0x08) << 1) |\n" +
		"					((s & 0x04) << 3) |\n" +
		"					((s & 0x02) << 5) |\n" +
		"					((s & 0x01) << 7));\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_27_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X27 {\n" +
		"	private void foo() {\n" +
		"\n" +
		"		if (_VerificationResult.getVerificationCode()\n" +
		"			== IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED\n" +
		"			|| _VerificationResult.getVerificationCode()\n" +
		"				== IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED) {\n" +
		"			// Group box\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X27 {\n" +
		"	private void foo() {\n" +
		"\n" +
		"		if (_VerificationResult.getVerificationCode() == IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED\n" +
		"				|| _VerificationResult.getVerificationCode() == IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED) {\n" +
		"			// Group box\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_28_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X28 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		if (fieldBinding.declaringClass != lastReceiverType\n" +
		"			&& !lastReceiverType.isArrayType()			\n" +
		"			&& fieldBinding.declaringClass != null\n" +
		"			&& fieldBinding.constant == NotAConstant\n" +
		"			&& ((currentScope.environment().options.targetJDK >= ClassFileConstants.JDK1_2\n" +
		"					&& (fieldBinding != this.binding || this.indexOfFirstFieldBinding > 1 || !fieldBinding.isStatic())\n" +
		"					&& fieldBinding.declaringClass.id != T_Object)\n" +
		"				|| !(useDelegate\n" +
		"						? new CodeSnippetScope(currentScope).canBeSeenByForCodeSnippet(fieldBinding.declaringClass, (ReferenceBinding) this.delegateThis.type)\n" +
		"						: fieldBinding.declaringClass.canBeSeenBy(currentScope)))){\n" +
		"			// code\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X28 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		if (fieldBinding.declaringClass != lastReceiverType\n" +
		"				&& !lastReceiverType.isArrayType()\n" +
		"				&& fieldBinding.declaringClass != null\n" +
		"				&& fieldBinding.constant == NotAConstant\n" +
		"				&& ((currentScope\n" +
		"						.environment().options.targetJDK >= ClassFileConstants.JDK1_2\n" +
		"						&& (fieldBinding != this.binding\n" +
		"								|| this.indexOfFirstFieldBinding > 1\n" +
		"								|| !fieldBinding.isStatic())\n" +
		"						&& fieldBinding.declaringClass.id != T_Object)\n" +
		"						|| !(useDelegate\n" +
		"								? new CodeSnippetScope(currentScope)\n" +
		"										.canBeSeenByForCodeSnippet(\n" +
		"												fieldBinding.declaringClass,\n" +
		"												(ReferenceBinding) this.delegateThis.type)\n" +
		"								: fieldBinding.declaringClass\n" +
		"										.canBeSeenBy(currentScope)))) {\n" +
		"			// code\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_29_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X29 {\n" +
		"\n" +
		"	boolean foo() {\n" +
		"		return (pack != null && otherpack != null && isSamePackage(pack, otherpack));\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X29 {\n" +
		"\n" +
		"	boolean foo() {\n" +
		"		return (pack != null && otherpack != null\n" +
		"				&& isSamePackage(pack, otherpack));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_30_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X30 {\n" +
		"	private boolean isInTypeNestedInInputType(ASTNode node, TypeDeclaration inputType){\n" +
		"		return (isInAnonymousTypeInsideInputType(node, inputType) ||\n" +
		"				isInLocalTypeInsideInputType(node, inputType) ||\n" +
		"				isInNonStaticMemberTypeInsideInputType(node, inputType));\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X30 {\n" +
		"	private boolean isInTypeNestedInInputType(ASTNode node,\n" +
		"			TypeDeclaration inputType) {\n" +
		"		return (isInAnonymousTypeInsideInputType(node, inputType) ||\n" +
		"				isInLocalTypeInsideInputType(node, inputType) ||\n" +
		"				isInNonStaticMemberTypeInsideInputType(node, inputType));\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_31_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X31 {\n" +
		"	void foo(int i) {\n" +
		"		if (true) {\n" +
		"			switch (i) {\n" +
		"				case 0:\n" +
		"					if (!((offset == (hashable.length - 1)) && !has95 && hasOneOf(meta63, hashable, offset - 2, 2) && !hasOneOf(meta64, hashable, offset - 4, 2)))\n" +
		"						buffer.append(\'R\');\n" +
		"					break;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X31 {\n" +
		"	void foo(int i) {\n" +
		"		if (true) {\n" +
		"			switch (i) {\n" +
		"			case 0:\n" +
		"				if (!((offset == (hashable.length - 1)) && !has95\n" +
		"						&& hasOneOf(meta63, hashable, offset - 2, 2)\n" +
		"						&& !hasOneOf(meta64, hashable, offset - 4, 2)))\n" +
		"					buffer.append(\'R\');\n" +
		"				break;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_32_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X32 {\n" +
		"	public boolean equals(Object object) {\n" +
		"		TextAttribute a= (TextAttribute) object;\n" +
		"		return (a.style == style && equals(a.foreground, foreground) && equals(a.background, background));\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X32 {\n" +
		"	public boolean equals(Object object) {\n" +
		"		TextAttribute a = (TextAttribute) object;\n" +
		"		return (a.style == style && equals(a.foreground, foreground)\n" +
		"				&& equals(a.background, background));\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from differences noticed with patch v29.txt
public void testBug330313_wksp1_33() {
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X33 {\n" +
		"	void foo() {\n" +
		"        if(inMetaTag &&\n" +
		"                        (  t1.image.equalsIgnoreCase(\"name\") ||\n" +
		"                           t1.image.equalsIgnoreCase(\"HTTP-EQUIV\")\n" +
		"                        )\n" +
		"           && t2 != null)\n" +
		"        {\n" +
		"                currentMetaTag=t2.image.toLowerCase();\n" +
		"        }\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X33 {\n" +
		"	void foo() {\n" +
		"		if (inMetaTag\n" +
		"				&& (t1.image.equalsIgnoreCase(\"name\")\n" +
		"						|| t1.image.equalsIgnoreCase(\"HTTP-EQUIV\"))\n" +
		"				&& t2 != null) {\n" +
		"			currentMetaTag = t2.image.toLowerCase();\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_33_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X33 {\n" +
		"	void foo() {\n" +
		"        if(inMetaTag &&\n" +
		"                        (  t1.image.equalsIgnoreCase(\"name\") ||\n" +
		"                           t1.image.equalsIgnoreCase(\"HTTP-EQUIV\")\n" +
		"                        )\n" +
		"           && t2 != null)\n" +
		"        {\n" +
		"                currentMetaTag=t2.image.toLowerCase();\n" +
		"        }\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X33 {\n" +
		"	void foo() {\n" +
		"		if (inMetaTag &&\n" +
		"				(t1.image.equalsIgnoreCase(\"name\") ||\n" +
		"						t1.image.equalsIgnoreCase(\"HTTP-EQUIV\"))\n" +
		"				&& t2 != null) {\n" +
		"			currentMetaTag = t2.image.toLowerCase();\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_34_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X34 {\n" +
		"	private boolean compareMarkers(ResourceInfo oldElement, ResourceInfo newElement) {\n" +
		"		boolean bothNull = oldElement.getMarkers(false) == null && newElement.getMarkers(false) == null;\n" +
		"		return bothNull || oldElement.getMarkerGenerationCount() == newElement.getMarkerGenerationCount();\n" +
		"	}\n" +
		"	private boolean compareSync(ResourceInfo oldElement, ResourceInfo newElement) {\n" +
		"		return oldElement.getSyncInfoGenerationCount() == newElement.getSyncInfoGenerationCount();\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X34 {\n" +
		"	private boolean compareMarkers(ResourceInfo oldElement,\n" +
		"			ResourceInfo newElement) {\n" +
		"		boolean bothNull = oldElement.getMarkers(false) == null\n" +
		"				&& newElement.getMarkers(false) == null;\n" +
		"		return bothNull || oldElement.getMarkerGenerationCount() == newElement\n" +
		"				.getMarkerGenerationCount();\n" +
		"	}\n" +
		"\n" +
		"	private boolean compareSync(ResourceInfo oldElement,\n" +
		"			ResourceInfo newElement) {\n" +
		"		return oldElement.getSyncInfoGenerationCount() == newElement\n" +
		"				.getSyncInfoGenerationCount();\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from differences noticed with patch v30.txt
public void testBug330313_wksp1_35_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X35 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			if (20+lineNum*printGC.getFontMetrics().getHeight() > printer.getClientArea().height) {\n" +
		"				//\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X35 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			if (20 + lineNum * printGC.getFontMetrics().getHeight() > printer\n" +
		"					.getClientArea().height) {\n" +
		"				//\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from differences noticed with patch v32.txt
public void testBug330313_wksp1_36_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X36 {\n" +
		"	public static boolean isRuntimeException(ITypeBinding thrownException) {\n" +
		"		if (thrownException == null || thrownException.isPrimitive() || thrownException.isArray())\n" +
		"			return false;\n" +
		"		return findTypeInHierarchy(thrownException, \"java.lang.RuntimeException\") != null; //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X36 {\n" +
		"	public static boolean isRuntimeException(ITypeBinding thrownException) {\n" +
		"		if (thrownException == null || thrownException.isPrimitive()\n" +
		"				|| thrownException.isArray())\n" +
		"			return false;\n" +
		"		return findTypeInHierarchy(thrownException,\n" +
		"				\"java.lang.RuntimeException\") != null; //$NON-NLS-1$\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_37_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X37 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			if (ignoreQuickDiffPrefPage && (info.getAnnotationType().equals(\"org.eclipse.ui.workbench.texteditor.quickdiffChange\") //$NON-NLS-1$\n" +
		"					|| (info.getAnnotationType().equals(\"org.eclipse.ui.workbench.texteditor.quickdiffAddition\")) //$NON-NLS-1$\n" +
		"					|| (info.getAnnotationType().equals(\"org.eclipse.ui.workbench.texteditor.quickdiffDeletion\")) //$NON-NLS-1$\n" +
		"				)) \n" +
		"				continue;\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X37 {\n" +
		"	void foo() {\n" +
		"		if (true) {\n" +
		"			if (ignoreQuickDiffPrefPage && (info.getAnnotationType().equals(\n" +
		"					\"org.eclipse.ui.workbench.texteditor.quickdiffChange\") //$NON-NLS-1$\n" +
		"					|| (info.getAnnotationType().equals(\n" +
		"							\"org.eclipse.ui.workbench.texteditor.quickdiffAddition\")) //$NON-NLS-1$\n" +
		"					|| (info.getAnnotationType().equals(\n" +
		"							\"org.eclipse.ui.workbench.texteditor.quickdiffDeletion\")) //$NON-NLS-1$\n" +
		"			))\n" +
		"				continue;\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// Test case extracted from differences noticed with patch v33.txt
public void testBug330313_wksp1_38_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X38 {\n" +
		"	void foo(boolean condition) {\n" +
		"		if (condition)\n" +
		"		{\n" +
		"			// block 1\n" +
		"		}\n" +
		"		else\n" +
		"		{\n" +
		"			// block 2\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
			"package wksp1;\n" +
			"\n" +
			"public class X38 {\n" +
			"	void foo(boolean condition) {\n" +
			"		if (condition) {\n" +
			"			// block 1\n" +
			"		} else {\n" +
			"			// block 2\n" +
			"		}\n" +
			"	}\n" +
			"}\n");
}
public void testBug330313_wksp1_39_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X39 {\n" +
		"/**\n" +
		" * <pre>\n" +
		" *		RadioGroupFieldEditor editor= new RadioGroupFieldEditor(\n" +
		" *			\"GeneralPage.DoubleClick\", resName, 1,\n" +
		" *			new String[][] {\n" +
		" *				{\"Open Browser\", \"open\"},\n" +
		" *				{\"Expand Tree\", \"expand\"}\n" +
		" *			},\n" +
		" *          parent);	\n" +
		" * </pre>\n" +
		" */\n" +
		"public void foo() {\n" +
		"}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X39 {\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 * RadioGroupFieldEditor editor = new RadioGroupFieldEditor(\n" +
		"	 * 		\"GeneralPage.DoubleClick\", resName, 1,\n" +
		"	 * 		new String[][] {\n" +
		"	 * 				{ \"Open Browser\", \"open\" },\n" +
		"	 * 				{ \"Expand Tree\", \"expand\" }\n" +
		"	 * 		},\n" +
		"	 * 		parent);\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_40_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X40 {\n" +
		"	protected final static String[][] TABLE= new String[][] {\n" +
		"								/*INACTIVE*/	/*PARTLY_ACTIVE */	/*ACTIVE */\n" +
		"		/* INACTIVE */		{	\"INACTIVE\",		\"PARTLY_ACTIVE\",		\"PARTLY_ACTIVE\" },\n" +
		"		/* PARTLY_ACTIVE*/	{	\"PARTLY_ACTIVE\", 	\"PARTLY_ACTIVE\",		\"PARTLY_ACTIVE\" },\n" +
		"		/* ACTIVE */		{	\"PARTLY_ACTIVE\", 	\"PARTLY_ACTIVE\",		\"ACTIVE\"}\n" +
		"	};\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X40 {\n" +
		"	protected final static String[][] TABLE = new String[][] {\n" +
		"			/* INACTIVE */ /* PARTLY_ACTIVE */ /* ACTIVE */\n" +
		"			/* INACTIVE */ { \"INACTIVE\", \"PARTLY_ACTIVE\", \"PARTLY_ACTIVE\" },\n" +
		"			/* PARTLY_ACTIVE */ { \"PARTLY_ACTIVE\", \"PARTLY_ACTIVE\",\n" +
		"					\"PARTLY_ACTIVE\" },\n" +
		"			/* ACTIVE */ { \"PARTLY_ACTIVE\", \"PARTLY_ACTIVE\", \"ACTIVE\" }\n" +
		"	};\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_41_njl() {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X41 {\n" +
		"	static final int [][] TABLE = {\n" +
		"		\n" +
		"		/* First */\n" +
		"		{1,	2},\n" +
		"		{3,	4},\n" +
		"		{5,	6},\n" +
		"//		{7????,	8},\n" +
		"\n" +
		"		/* Second */		\n" +
		"//		{11, 12},\n" +
		"//		{13, 14},\n" +
		"//		{15, 16},\n" +
		"		\n" +
		"		\n" +
		"		/* Third */\n" +
		"		{21,	22},\n" +
		"		{23,	24},\n" +
		"		{25,	26},\n" +
		"//		{27????,	28},\n" +
		"\n" +
		"		/* Others */\n" +
		"		{31,	32},\n" +
		"		{33,	34},\n" +
		"		{35,	36},\n" +
		"//		{37????,	38},\n" +
		"		\n" +
		"	};\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X41 {\n" +
		"	static final int[][] TABLE = {\n" +
		"\n" +
		"			/* First */\n" +
		"			{ 1, 2 },\n" +
		"			{ 3, 4 },\n" +
		"			{ 5, 6 },\n" +
		"			// {7????, 8},\n" +
		"\n" +
		"			/* Second */\n" +
		"			// {11, 12},\n" +
		"			// {13, 14},\n" +
		"			// {15, 16},\n" +
		"\n" +
		"			/* Third */\n" +
		"			{ 21, 22 },\n" +
		"			{ 23, 24 },\n" +
		"			{ 25, 26 },\n" +
		"			// {27????, 28},\n" +
		"\n" +
		"			/* Others */\n" +
		"			{ 31, 32 },\n" +
		"			{ 33, 34 },\n" +
		"			{ 35, 36 },\n" +
		"			// {37????, 38},\n" +
		"\n" +
		"	};\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_42_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X42 {\n" +
		"	static final byte[][] DashList = {\n" +
		"		{ },                   // SWT.LINE_SOLID\n" +
		"		{ 10, 4 },             // SWT.LINE_DASH\n" +
		"		{ 2, 2 },              // SWT.LINE_DOT\n" +
		"		{ 10, 4, 2, 4 },       // SWT.LINE_DASHDOT\n" +
		"		{ 10, 4, 2, 4, 2, 4 }  // SWT.LINE_DASHDOTDOT\n" +
		"	};\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X42 {\n" +
		"	static final byte[][] DashList = {\n" +
		"			{}, // SWT.LINE_SOLID\n" +
		"			{ 10, 4 }, // SWT.LINE_DASH\n" +
		"			{ 2, 2 }, // SWT.LINE_DOT\n" +
		"			{ 10, 4, 2, 4 }, // SWT.LINE_DASHDOT\n" +
		"			{ 10, 4, 2, 4, 2, 4 } // SWT.LINE_DASHDOTDOT\n" +
		"	};\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_43_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X43 {\n" +
		"	Cloneable clone;\n" +
		"X43() {\n" +
		"	this.clone = new Cloneable() {\n" +
		"		void foo(int x) {\n" +
		"			switch (x) {\n" +
		"				case 1:\n" +
		"				case 2:\n" +
		"					if (true) break;\n" +
		"					// FALL THROUGH\n" +
		"				case 3:\n" +
		"				case 4:\n" +
		"					break;\n" +
		"			}\n" +
		"		}\n" +
		"	};\n" +
		"}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X43 {\n" +
		"	Cloneable clone;\n" +
		"\n" +
		"	X43() {\n" +
		"		this.clone = new Cloneable() {\n" +
		"			void foo(int x) {\n" +
		"				switch (x) {\n" +
		"				case 1:\n" +
		"				case 2:\n" +
		"					if (true)\n" +
		"						break;\n" +
		"					// FALL THROUGH\n" +
		"				case 3:\n" +
		"				case 4:\n" +
		"					break;\n" +
		"				}\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_44_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X44 {\n" +
		"	String foo() {\n" +
		"		return Policy.bind(\"CVSAnnotateBlock.6\", new Object[] { //$NON-NLS-1$\n" +
		"			user,\n" +
		"			revision,\n" +
		"			String.valueOf(delta),\n" +
		"			line\n" +
		"		});\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X44 {\n" +
		"	String foo() {\n" +
		"		return Policy.bind(\"CVSAnnotateBlock.6\", new Object[] { //$NON-NLS-1$\n" +
		"				user,\n" +
		"				revision,\n" +
		"				String.valueOf(delta),\n" +
		"				line\n" +
		"		});\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_45_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X45 {\n" +
		"		private String[][] TABLE  = {\n" +
		"			{\"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\", \"COL_COMMENT\", \"COL_TAGS\"},	/* revision */ \n" +
		"			{\"COL_TAGS\", \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\", \"COL_COMMENT\"},	/* tags */\n" +
		"			{\"COL_DATE\", \"COL_REVISION\", \"COL_AUTHOR\", \"COL_COMMENT\", \"COL_TAGS\"},	/* date */\n" +
		"			{\"COL_AUTHOR\", \"COL_REVISION\", \"COL_DATE\", \"COL_COMMENT\", \"COL_TAGS\"},	/* author */\n" +
		"			{\"COL_COMMENT\", \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\", \"COL_TAGS\"}		/* comment */\n" +
		"		};\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X45 {\n" +
		"	private String[][] TABLE = {\n" +
		"			{ \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\", \"COL_COMMENT\",\n" +
		"					\"COL_TAGS\" }, /* revision */\n" +
		"			{ \"COL_TAGS\", \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\",\n" +
		"					\"COL_COMMENT\" }, /* tags */\n" +
		"			{ \"COL_DATE\", \"COL_REVISION\", \"COL_AUTHOR\", \"COL_COMMENT\",\n" +
		"					\"COL_TAGS\" }, /* date */\n" +
		"			{ \"COL_AUTHOR\", \"COL_REVISION\", \"COL_DATE\", \"COL_COMMENT\",\n" +
		"					\"COL_TAGS\" }, /* author */\n" +
		"			{ \"COL_COMMENT\", \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\",\n" +
		"					\"COL_TAGS\" } /* comment */\n" +
		"	};\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_45b_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
			"package wksp1;\n" +
					"\n" +
					"public class X45 {\n" +
					"		private String[][] TABLE  = {\n" +
					"			{\"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\", \"COL_COMMENT\", \"COL_TAGS\"},	// revision \n" +
					"			{\"COL_TAGS\", \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\", \"COL_COMMENT\"},	// tags \n" +
					"			{\"COL_DATE\", \"COL_REVISION\", \"COL_AUTHOR\", \"COL_COMMENT\", \"COL_TAGS\"},	// date \n" +
					"			{\"COL_AUTHOR\", \"COL_REVISION\", \"COL_DATE\", \"COL_COMMENT\", \"COL_TAGS\"},	// author \n" +
					"			{\"COL_COMMENT\", \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\", \"COL_TAGS\"}		// comment \n" +
					"		};\n" +
					"}\n";
	formatSource(source	,
			"package wksp1;\n" +
					"\n" +
					"public class X45 {\n" +
					"	private String[][] TABLE = {\n" +
					"			{ \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\", \"COL_COMMENT\",\n" +
					"					\"COL_TAGS\" }, // revision\n" +
					"			{ \"COL_TAGS\", \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\",\n" +
					"					\"COL_COMMENT\" }, // tags\n" +
					"			{ \"COL_DATE\", \"COL_REVISION\", \"COL_AUTHOR\", \"COL_COMMENT\",\n" +
					"					\"COL_TAGS\" }, // date\n" +
					"			{ \"COL_AUTHOR\", \"COL_REVISION\", \"COL_DATE\", \"COL_COMMENT\",\n" +
					"					\"COL_TAGS\" }, // author\n" +
					"			{ \"COL_COMMENT\", \"COL_REVISION\", \"COL_DATE\", \"COL_AUTHOR\",\n" +
					"					\"COL_TAGS\" } // comment\n" +
					"	};\n" +
					"}\n"
			);
}
public void testBug330313_wksp1_46_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X46 {\n" +
		"	void foo() {\n" +
		"	    if (getActive() == StackPresentation.AS_ACTIVE_NOFOCUS) {\n" +
		"	        drawGradient(\n" +
		"	                colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR), \n" +
		"	                new Color [] {\n" +
		"	                        colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START) \n" +
		"	                }, \n" +
		"	                new int [0],\n" +
		"	                true);	        \n" +
		"	    }\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X46 {\n" +
		"	void foo() {\n" +
		"		if (getActive() == StackPresentation.AS_ACTIVE_NOFOCUS) {\n" +
		"			drawGradient(\n" +
		"					colorRegistry.get(\n" +
		"							IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR),\n" +
		"					new Color[] {\n" +
		"							colorRegistry.get(\n" +
		"									IWorkbenchThemeConstants.INACTIVE_TAB_BG_START)\n" +
		"					},\n" +
		"					new int[0],\n" +
		"					true);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_47_njl() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X47 {\n" +
		"	void foo(int x) {\n" +
		"		switch (x) {\n" +
		"			case 0 :\n" +
		"				// case 0\n" +
		"				break;\n" +
		"			case 3 :\n" +
		"				// case 3\n" +
		"				break;\n" +
		"			//case -1 :\n" +
		"			// internal failure: trying to load variable not supposed to be generated\n" +
		"			//	break;\n" +
		"			default :\n" +
		"				// default\n" +
		"		}\n" +
		"		// last comment\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X47 {\n" +
		"	void foo(int x) {\n" +
		"		switch (x) {\n" +
		"		case 0:\n" +
		"			// case 0\n" +
		"			break;\n" +
		"		case 3:\n" +
		"			// case 3\n" +
		"			break;\n" +
		"		// case -1 :\n" +
		"		// internal failure: trying to load variable not supposed to be\n" +
		"		// generated\n" +
		"		// break;\n" +
		"		default:\n" +
		"			// default\n" +
		"		}\n" +
		"		// last comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_48_njl() {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X48 {\n" +
		"	void foo(int x) {\n" +
		"		switch (x) {\n" +
		"			case 0 :\n" +
		"				// case 0\n" +
		"				break;\n" +
		"			case 3 :\n" +
		"				// case 3\n" +
		"				break;\n" +
		"			//case -1 :\n" +
		"			// internal failure: trying to load variable not supposed to be generated\n" +
		"			//	break;\n" +
		"		}\n" +
		"		// last comment\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X48 {\n" +
		"	void foo(int x) {\n" +
		"		switch (x) {\n" +
		"		case 0:\n" +
		"			// case 0\n" +
		"			break;\n" +
		"		case 3:\n" +
		"			// case 3\n" +
		"			break;\n" +
		"		// case -1 :\n" +
		"		// internal failure: trying to load variable not supposed to be\n" +
		"		// generated\n" +
		"		// break;\n" +
		"		}\n" +
		"		// last comment\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_49_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X49 {\n" +
		"	void foo(int i) {\n" +
		"		if (true) {\n" +
		"			if (true) {\n" +
		"				this.foundTaskPositions[this.foundTaskCount] = new int[] { i, i + tagLength - 1 };\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X49 {\n" +
		"	void foo(int i) {\n" +
		"		if (true) {\n" +
		"			if (true) {\n" +
		"				this.foundTaskPositions[this.foundTaskCount] = new int[] { i,\n" +
		"						i + tagLength - 1 };\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_50_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X50 {\n" +
		"private void deployCodeSnippetClassIfNeeded(IRequestor requestor) {\n" +
		"	if (this.codeSnippetBinary == null) {\n" +
		"		// Deploy CodeSnippet class (only once)\n" +
		"		requestor.acceptClassFiles(\n" +
		"			new ClassFile[] {\n" +
		"				new ClassFile() {\n" +
		"					public byte[] getBytes() {\n" +
		"						return getCodeSnippetBytes();\n" +
		"					}\n" +
		"					public char[][] getCompoundName() {\n" +
		"						return EvaluationConstants.ROOT_COMPOUND_NAME;\n" +
		"					}\n" +
		"				}\n" +
		"			}, \n" +
		"			null);\n" +
		"	}\n" +
		"}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X50 {\n" +
		"	private void deployCodeSnippetClassIfNeeded(IRequestor requestor) {\n" +
		"		if (this.codeSnippetBinary == null) {\n" +
		"			// Deploy CodeSnippet class (only once)\n" +
		"			requestor.acceptClassFiles(\n" +
		"					new ClassFile[] {\n" +
		"							new ClassFile() {\n" +
		"								public byte[] getBytes() {\n" +
		"									return getCodeSnippetBytes();\n" +
		"								}\n" +
		"\n" +
		"								public char[][] getCompoundName() {\n" +
		"									return EvaluationConstants.ROOT_COMPOUND_NAME;\n" +
		"								}\n" +
		"							}\n" +
		"					},\n" +
		"					null);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_51_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X51 {\n" +
		"\n" +
		"protected void addAllSourceFiles(final ArrayList sourceFiles) throws CoreException {\n" +
		"	for (int i = 0, l = sourceLocations.length; i < l; i++) {\n" +
		"		sourceLocation.sourceFolder.accept(\n" +
		"			new IResourceProxyVisitor() {\n" +
		"				public boolean visit(IResourceProxy proxy) throws CoreException {\n" +
		"					IResource resource = null;\n" +
		"					switch(proxy.getType()) {\n" +
		"						case IResource.FILE :\n" +
		"							if (exclusionPatterns != null || inclusionPatterns != null) {\n" +
		"								resource = proxy.requestResource();\n" +
		"								if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns)) return false;\n" +
		"							}\n" +
		"							if (org.eclipse.jdt.internal.compiler.util.Util.isJavaFileName(proxy.getName())) {\n" +
		"								if (resource == null)\n" +
		"									resource = proxy.requestResource();\n" +
		"								sourceFiles.add(new SourceFile((IFile) resource, sourceLocation));\n" +
		"							}\n" +
		"							return false;\n" +
		"						case IResource.FOLDER :\n" +
		"							if (exclusionPatterns != null && inclusionPatterns == null) {\n" +
		"								// if there are inclusion patterns then we must walk the children\n" +
		"								resource = proxy.requestResource();\n" +
		"								if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns)) return false;\n" +
		"							}\n" +
		"							if (isAlsoProject && isExcludedFromProject(proxy.requestFullPath())) return false;\n" +
		"					}\n" +
		"					return true;\n" +
		"				}\n" +
		"			},\n" +
		"			IResource.NONE);\n" +
		"		notifier.checkCancel();\n" +
		"	}\n" +
		"}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X51 {\n" +
		"\n" +
		"	protected void addAllSourceFiles(final ArrayList sourceFiles)\n" +
		"			throws CoreException {\n" +
		"		for (int i = 0, l = sourceLocations.length; i < l; i++) {\n" +
		"			sourceLocation.sourceFolder.accept(\n" +
		"					new IResourceProxyVisitor() {\n" +
		"						public boolean visit(IResourceProxy proxy)\n" +
		"								throws CoreException {\n" +
		"							IResource resource = null;\n" +
		"							switch (proxy.getType()) {\n" +
		"							case IResource.FILE:\n" +
		"								if (exclusionPatterns != null\n" +
		"										|| inclusionPatterns != null) {\n" +
		"									resource = proxy.requestResource();\n" +
		"									if (Util.isExcluded(resource,\n" +
		"											inclusionPatterns,\n" +
		"											exclusionPatterns))\n" +
		"										return false;\n" +
		"								}\n" +
		"								if (org.eclipse.jdt.internal.compiler.util.Util\n" +
		"										.isJavaFileName(proxy.getName())) {\n" +
		"									if (resource == null)\n" +
		"										resource = proxy.requestResource();\n" +
		"									sourceFiles.add(new SourceFile(\n" +
		"											(IFile) resource, sourceLocation));\n" +
		"								}\n" +
		"								return false;\n" +
		"							case IResource.FOLDER:\n" +
		"								if (exclusionPatterns != null\n" +
		"										&& inclusionPatterns == null) {\n" +
		"									// if there are inclusion patterns then we\n" +
		"									// must walk the children\n" +
		"									resource = proxy.requestResource();\n" +
		"									if (Util.isExcluded(resource,\n" +
		"											inclusionPatterns,\n" +
		"											exclusionPatterns))\n" +
		"										return false;\n" +
		"								}\n" +
		"								if (isAlsoProject && isExcludedFromProject(\n" +
		"										proxy.requestFullPath()))\n" +
		"									return false;\n" +
		"							}\n" +
		"							return true;\n" +
		"						}\n" +
		"					},\n" +
		"					IResource.NONE);\n" +
		"			notifier.checkCancel();\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_52_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setPageWidth80();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X52 {\n" +
		"	protected FastSyncInfoFilter getKnownFailureCases() {\n" +
		"		return new OrSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"			// Conflicting additions of files will fail\n" +
		"			new AndSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"				FastSyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.ADDITION),\n" +
		"				new FastSyncInfoFilter() {\n" +
		"					public boolean select(SyncInfo info) {\n" +
		"						return info.getLocal().getType() == IResource.FILE;\n" +
		"					}\n" +
		"				}\n" +
		"			}),\n" +
		"			// Conflicting changes of files will fail if the local is not managed\n" +
		"			// or is an addition\n" +
		"			new AndSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"				FastSyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.CHANGE),\n" +
		"				new FastSyncInfoFilter() {\n" +
		"					public boolean select(SyncInfo info) {\n" +
		"						if (info.getLocal().getType() == IResource.FILE) {\n" +
		"							try {\n" +
		"								ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)info.getLocal());\n" +
		"								byte[] syncBytes = cvsFile.getSyncBytes();\n" +
		"								return (syncBytes == null || ResourceSyncInfo.isAddition(syncBytes));\n" +
		"							} catch (CVSException e) {\n" +
		"								CVSUIPlugin.log(e);\n" +
		"								// Fall though and try to update\n" +
		"							}\n" +
		"						}\n" +
		"						return false;\n" +
		"					}\n" +
		"				}\n" +
		"			}),\n" +
		"			// Conflicting changes involving a deletion on one side will aways fail\n" +
		"			new AndSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"				FastSyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.CHANGE),\n" +
		"				new FastSyncInfoFilter() {\n" +
		"					public boolean select(SyncInfo info) {\n" +
		"						IResourceVariant remote = info.getRemote();\n" +
		"						IResourceVariant base = info.getBase();\n" +
		"						if (info.getLocal().exists()) {\n" +
		"							// local != base and no remote will fail\n" +
		"							return (base != null && remote == null);\n" +
		"						} else {\n" +
		"							// no local and base != remote\n" +
		"							return (base != null && remote != null && !base.equals(remote));\n" +
		"						}\n" +
		"					}\n" +
		"				}\n" +
		"			}),\n" +
		"			// Conflicts where the file type is binary will work but are not merged\n" +
		"			// so they should be skipped\n" +
		"			new AndSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"				FastSyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.CHANGE),\n" +
		"				new FastSyncInfoFilter() {\n" +
		"					public boolean select(SyncInfo info) {\n" +
		"						IResource local = info.getLocal();\n" +
		"						if (local.getType() == IResource.FILE) {\n" +
		"							try {\n" +
		"								ICVSFile file = CVSWorkspaceRoot.getCVSFileFor((IFile)local);\n" +
		"								byte[] syncBytes = file.getSyncBytes();\n" +
		"								if (syncBytes != null) {\n" +
		"									return ResourceSyncInfo.isBinary(syncBytes);\n" +
		"								}\n" +
		"							} catch (CVSException e) {\n" +
		"								// There was an error obtaining or interpreting the sync bytes\n" +
		"								// Log it and skip the file\n" +
		"								CVSProviderPlugin.log(e);\n" +
		"								return true;\n" +
		"							}\n" +
		"						}\n" +
		"						return false;\n" +
		"					}\n" +
		"				}\n" +
		"			}),\n" +
		"			// Outgoing changes may not fail but they are skipped as well\n" +
		"			new SyncInfoDirectionFilter(SyncInfo.OUTGOING)\n" +
		"		});\n" +
		"	}\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X52 {\n" +
		"	protected FastSyncInfoFilter getKnownFailureCases() {\n" +
		"		return new OrSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"				// Conflicting additions of files will fail\n" +
		"				new AndSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"						FastSyncInfoFilter.getDirectionAndChangeFilter(\n" +
		"								SyncInfo.CONFLICTING, SyncInfo.ADDITION),\n" +
		"						new FastSyncInfoFilter() {\n" +
		"							public boolean select(SyncInfo info) {\n" +
		"								return info.getLocal()\n" +
		"										.getType() == IResource.FILE;\n" +
		"							}\n" +
		"						}\n" +
		"				}),\n" +
		"				// Conflicting changes of files will fail if the local is not\n" +
		"				// managed\n" +
		"				// or is an addition\n" +
		"				new AndSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"						FastSyncInfoFilter.getDirectionAndChangeFilter(\n" +
		"								SyncInfo.CONFLICTING, SyncInfo.CHANGE),\n" +
		"						new FastSyncInfoFilter() {\n" +
		"							public boolean select(SyncInfo info) {\n" +
		"								if (info.getLocal()\n" +
		"										.getType() == IResource.FILE) {\n" +
		"									try {\n" +
		"										ICVSFile cvsFile = CVSWorkspaceRoot\n" +
		"												.getCVSFileFor((IFile) info\n" +
		"														.getLocal());\n" +
		"										byte[] syncBytes = cvsFile\n" +
		"												.getSyncBytes();\n" +
		"										return (syncBytes == null\n" +
		"												|| ResourceSyncInfo\n" +
		"														.isAddition(syncBytes));\n" +
		"									} catch (CVSException e) {\n" +
		"										CVSUIPlugin.log(e);\n" +
		"										// Fall though and try to update\n" +
		"									}\n" +
		"								}\n" +
		"								return false;\n" +
		"							}\n" +
		"						}\n" +
		"				}),\n" +
		"				// Conflicting changes involving a deletion on one side will\n" +
		"				// aways fail\n" +
		"				new AndSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"						FastSyncInfoFilter.getDirectionAndChangeFilter(\n" +
		"								SyncInfo.CONFLICTING, SyncInfo.CHANGE),\n" +
		"						new FastSyncInfoFilter() {\n" +
		"							public boolean select(SyncInfo info) {\n" +
		"								IResourceVariant remote = info.getRemote();\n" +
		"								IResourceVariant base = info.getBase();\n" +
		"								if (info.getLocal().exists()) {\n" +
		"									// local != base and no remote will fail\n" +
		"									return (base != null && remote == null);\n" +
		"								} else {\n" +
		"									// no local and base != remote\n" +
		"									return (base != null && remote != null\n" +
		"											&& !base.equals(remote));\n" +
		"								}\n" +
		"							}\n" +
		"						}\n" +
		"				}),\n" +
		"				// Conflicts where the file type is binary will work but are not\n" +
		"				// merged\n" +
		"				// so they should be skipped\n" +
		"				new AndSyncInfoFilter(new FastSyncInfoFilter[] {\n" +
		"						FastSyncInfoFilter.getDirectionAndChangeFilter(\n" +
		"								SyncInfo.CONFLICTING, SyncInfo.CHANGE),\n" +
		"						new FastSyncInfoFilter() {\n" +
		"							public boolean select(SyncInfo info) {\n" +
		"								IResource local = info.getLocal();\n" +
		"								if (local.getType() == IResource.FILE) {\n" +
		"									try {\n" +
		"										ICVSFile file = CVSWorkspaceRoot\n" +
		"												.getCVSFileFor((IFile) local);\n" +
		"										byte[] syncBytes = file.getSyncBytes();\n" +
		"										if (syncBytes != null) {\n" +
		"											return ResourceSyncInfo\n" +
		"													.isBinary(syncBytes);\n" +
		"										}\n" +
		"									} catch (CVSException e) {\n" +
		"										// There was an error obtaining or\n" +
		"										// interpreting the sync bytes\n" +
		"										// Log it and skip the file\n" +
		"										CVSProviderPlugin.log(e);\n" +
		"										return true;\n" +
		"									}\n" +
		"								}\n" +
		"								return false;\n" +
		"							}\n" +
		"						}\n" +
		"				}),\n" +
		"				// Outgoing changes may not fail but they are skipped as well\n" +
		"				new SyncInfoDirectionFilter(SyncInfo.OUTGOING)\n" +
		"		});\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug330313_wksp1_53_njl_bnl() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package wksp1;\n" +
		"\n" +
		"public class X53 {\n" +
		"	static final short[][][] BLACK_CODE = {\n" +
		"		/* 9 bits  */\n" +
		"		{{24, 15}},\n" +
		"		/* 10 bits */\n" +
		"		{{8, 18}, {15, 64}, {23, 16}, {24, 17}, {55, 0}},\n" +
		"		/* 11 bits */\n" +
		"		{/* EOL */{0, -1}, {8, 1792}, {23, 24}, {24, 25}, {40, 23}, {55, 22}, {103, 19},\n" +
		"		{104, 20}, {108, 21}, {12, 1856}, {13, 1920}},\n" +
		"	};\n" +
		"}\n";
	formatSource(source	,
		"package wksp1;\n" +
		"\n" +
		"public class X53\n" +
		"{\n" +
		"	static final short[][][] BLACK_CODE =\n" +
		"	{\n" +
		"			/* 9 bits */\n" +
		"			{\n" +
		"					{ 24, 15 } },\n" +
		"			/* 10 bits */\n" +
		"			{\n" +
		"					{ 8, 18 },\n" +
		"					{ 15, 64 },\n" +
		"					{ 23, 16 },\n" +
		"					{ 24, 17 },\n" +
		"					{ 55, 0 } },\n" +
		"			/* 11 bits */\n" +
		"			{\n" +
		"					/* EOL */{ 0, -1 },\n" +
		"					{ 8, 1792 },\n" +
		"					{ 23, 24 },\n" +
		"					{ 24, 25 },\n" +
		"					{ 40, 23 },\n" +
		"					{ 55, 22 },\n" +
		"					{ 103, 19 },\n" +
		"					{ 104, 20 },\n" +
		"					{ 108, 21 },\n" +
		"					{ 12, 1856 },\n" +
		"					{ 13, 1920 } },\n" +
		"	};\n" +
		"}\n"
	);
}
public void testBug330313_wksp2_01 () {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"    static final Object[][] contents = {\n" +
		"        // comment\n" +
		"        { \"STR1\",\n" +
		"        	// comment\n" +
		"            new String[] { \"STR\",     // comment\n" +
		"                           \"STR\",     // comment\n" +
		"                           \"STR\"}     // comment\n" +
		"        }\n" +
		"\n" +
		"    };\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	static final Object[][] contents = {\n" +
		"			// comment\n" +
		"			{ \"STR1\",\n" +
		"					// comment\n" +
		"					new String[] { \"STR\", // comment\n" +
		"							\"STR\", // comment\n" +
		"							\"STR\" } // comment\n" +
		"			}\n" +
		"\n" +
		"	};\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_wksp3_X01_njl() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package wksp3;\n" +
		"\n" +
		"public class X01 {\n" +
		"    private static final String foo[][] = {\n" +
		"        // line 1\n" +
		"        // line 2\n" +
		"        {\"A\", \"B\", \"C\", \"D\", \"E\"} // comment\n" +
		"    };\n" +
		"}\n";
	formatSource(source	,
		"package wksp3;\n" +
		"\n" +
		"public class X01 {\n" +
		"	private static final String foo[][] = {\n" +
		"			// line 1\n" +
		"			// line 2\n" +
		"			{ \"A\", \"B\", \"C\", \"D\", \"E\" } // comment\n" +
		"	};\n" +
		"}\n"
	);
}
// Test cases added from bug 286601
public void testBug330313_b286601_04() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"package b286601;\n" +
		"\n" +
		"public class X04 {\n" +
		"\n" +
		"    \n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]). \n" +
		"    private static final int[][] ACCESS_MODE_CONDITIONAL_TABLE= {\n" +
		"    /*                        UNUSED           READ             READ_POTENTIAL   WRTIE            WRITE_POTENTIAL  UNKNOWN */\n" +
		"    /* UNUSED */            { UNUSED,          READ_POTENTIAL,  READ_POTENTIAL,  WRITE_POTENTIAL, WRITE_POTENTIAL, UNKNOWN },\n" +
		"    /* READ */              { READ_POTENTIAL,  READ,            READ_POTENTIAL,  UNKNOWN,         UNKNOWN,         UNKNOWN },\n" +
		"    /* READ_POTENTIAL */    { READ_POTENTIAL,  READ_POTENTIAL,  READ_POTENTIAL,  UNKNOWN,         UNKNOWN,         UNKNOWN },\n" +
		"    /* WRITE */             { WRITE_POTENTIAL, UNKNOWN,         UNKNOWN,         WRITE,           WRITE_POTENTIAL, UNKNOWN },\n" +
		"    /* WRITE_POTENTIAL */   { WRITE_POTENTIAL, UNKNOWN,         UNKNOWN,         WRITE_POTENTIAL, WRITE_POTENTIAL, UNKNOWN },\n" +
		"    /* UNKNOWN */           { UNKNOWN,         UNKNOWN,         UNKNOWN,         UNKNOWN,         UNKNOWN,         UNKNOWN }\n" +
		"    };\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package b286601;\n" +
		"\n" +
		"public class X04\n" +
		"{\n" +
		"\n" +
		"	// Table to merge access modes for condition statements (e.g branch[x] ||\n" +
		"	// branch[y]).\n" +
		"	private static final int[][] ACCESS_MODE_CONDITIONAL_TABLE =\n" +
		"	{\n" +
		"			/* UNUSED READ READ_POTENTIAL WRTIE WRITE_POTENTIAL UNKNOWN */\n" +
		"			/* UNUSED */ {\n" +
		"					UNUSED,\n" +
		"					READ_POTENTIAL,\n" +
		"					READ_POTENTIAL,\n" +
		"					WRITE_POTENTIAL,\n" +
		"					WRITE_POTENTIAL,\n" +
		"					UNKNOWN },\n" +
		"			/* READ */ {\n" +
		"					READ_POTENTIAL,\n" +
		"					READ,\n" +
		"					READ_POTENTIAL,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN },\n" +
		"			/* READ_POTENTIAL */ {\n" +
		"					READ_POTENTIAL,\n" +
		"					READ_POTENTIAL,\n" +
		"					READ_POTENTIAL,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN },\n" +
		"			/* WRITE */ {\n" +
		"					WRITE_POTENTIAL,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN,\n" +
		"					WRITE,\n" +
		"					WRITE_POTENTIAL,\n" +
		"					UNKNOWN },\n" +
		"			/* WRITE_POTENTIAL */ {\n" +
		"					WRITE_POTENTIAL,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN,\n" +
		"					WRITE_POTENTIAL,\n" +
		"					WRITE_POTENTIAL,\n" +
		"					UNKNOWN },\n" +
		"			/* UNKNOWN */ {\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN,\n" +
		"					UNKNOWN }\n" +
		"	};\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_b286601_05() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"package b286601;\n" +
		"\n" +
		"public class X05 {\n" +
		"\n" +
		"    \n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]). \n" +
		"    static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {\n" +
		"    { \"UNUSED\",          \"READ_POTENTIAL\",  \"READ_POTENTIAL\",  \"WRITE_POTENTIAL\", \"WRITE_POTENTIAL\", \"UNKNOWN\" },\n" +
		"    { \"READ_POTENTIAL\",  \"READ\",            \"READ_POTENTIAL\",  \"UNKNOWN\",         \"UNKNOWN\",         \"UNKNOWN\" },\n" +
		"    };\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package b286601;\n" +
		"\n" +
		"public class X05\n" +
		"{\n" +
		"\n" +
		"	// Table to merge access modes for condition statements (e.g branch[x] ||\n" +
		"	// branch[y]).\n" +
		"	static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =\n" +
		"	{\n" +
		"			{\n" +
		"					\"UNUSED\",\n" +
		"					\"READ_POTENTIAL\",\n" +
		"					\"READ_POTENTIAL\",\n" +
		"					\"WRITE_POTENTIAL\",\n" +
		"					\"WRITE_POTENTIAL\",\n" +
		"					\"UNKNOWN\" },\n" +
		"			{\n" +
		"					\"READ_POTENTIAL\",\n" +
		"					\"READ\",\n" +
		"					\"READ_POTENTIAL\",\n" +
		"					\"UNKNOWN\",\n" +
		"					\"UNKNOWN\",\n" +
		"					\"UNKNOWN\" },\n" +
		"	};\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_b286601_06() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"package b286601;\n" +
		"\n" +
		"public class X06 {\n" +
		"\n" +
		"    \n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]). \n" +
		"    static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {\n" +
		"    /* Comment 1 */\n" +
		"    /* Comment 2 */ { \"UNUSED\",          \"READ_POTENTIAL\",  \"READ_POTENTIAL\",  \"WRITE_POTENTIAL\", \"WRITE_POTENTIAL\", \"UNKNOWN\" },\n" +
		"    /* Comment 3 */ { \"READ_POTENTIAL\",  \"READ\",            \"READ_POTENTIAL\",  \"UNKNOWN\",         \"UNKNOWN\",         \"UNKNOWN\" },\n" +
		"    };\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package b286601;\n" +
		"\n" +
		"public class X06\n" +
		"{\n" +
		"\n" +
		"	// Table to merge access modes for condition statements (e.g branch[x] ||\n" +
		"	// branch[y]).\n" +
		"	static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =\n" +
		"	{\n" +
		"			/* Comment 1 */\n" +
		"			/* Comment 2 */ {\n" +
		"					\"UNUSED\",\n" +
		"					\"READ_POTENTIAL\",\n" +
		"					\"READ_POTENTIAL\",\n" +
		"					\"WRITE_POTENTIAL\",\n" +
		"					\"WRITE_POTENTIAL\",\n" +
		"					\"UNKNOWN\" },\n" +
		"			/* Comment 3 */ {\n" +
		"					\"READ_POTENTIAL\",\n" +
		"					\"READ\",\n" +
		"					\"READ_POTENTIAL\",\n" +
		"					\"UNKNOWN\",\n" +
		"					\"UNKNOWN\",\n" +
		"					\"UNKNOWN\" },\n" +
		"	};\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_b286601_07() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"package b286601;\n" +
		"\n" +
		"public class X07 {\n" +
		"\n" +
		"    \n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]). \n" +
		"    static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {\n" +
		"    /* Comment 1 */\n" +
		"    /* Comment 2 */ { \"1234567890123456789012345678901234567890\", \"1234567890123456789012345678901234567890\" },\n" +
		"    /* Comment 3 */ { \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"    };\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package b286601;\n" +
		"\n" +
		"public class X07\n" +
		"{\n" +
		"\n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] ||\n" +
		"    // branch[y]).\n" +
		"    static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =\n" +
		"    {\n" +
		"            /* Comment 1 */\n" +
		"            /* Comment 2 */ {\n" +
		"                    \"1234567890123456789012345678901234567890\",\n" +
		"                    \"1234567890123456789012345678901234567890\" },\n" +
		"            /* Comment 3 */ {\n" +
		"                    \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\",\n" +
		"                    \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"    };\n" +
		"\n" +
		"}\n"
	);
}
public void testBug330313_b286601_08() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_ONE_PER_LINE_SPLIT;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	setPageWidth80();
	String source =
		"package b286601;\n" +
		"\n" +
		"public class X08 {\n" +
		"    private MinimizedFileSystemElement selectFiles(final Object rootFileSystemObject, final IImportStructureProvider structureProvider) {\n" +
		"\n" +
		"        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {\n" +
		"            public void run() {\n" +
		"                //Create the root element from the supplied file system object\n" +
		"            }\n" +
		"        });\n" +
		"\n" +
		"        return null;\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"package b286601;\n" +
		"\n" +
		"public class X08\n" +
		"{\n" +
		"	private MinimizedFileSystemElement selectFiles(\n" +
		"			final Object rootFileSystemObject,\n" +
		"			final IImportStructureProvider structureProvider)\n" +
		"	{\n" +
		"\n" +
		"		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable()\n" +
		"		{\n" +
		"			public void run()\n" +
		"			{\n" +
		"				// Create the root element from the supplied file system object\n" +
		"			}\n" +
		"		});\n" +
		"\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 332818: [formatter] Java formatter, Blank Lines tab, only 1st line indented when multiple lines is set
 * @test Ensure that the indentation is set on all blank lines
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=332818"
 */
public void testBug332818() throws Exception {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.number_of_empty_lines_to_preserve = 99;
	String source =
		"public class Test {\n" +
		"\n" +
		" \n" +
		"  \n" +
		"   \n" +
		"    \n" +
		"	\n" +
		"    private String f1;\n" +
		"    \n" +
		"   \n" +
		"  \n" +
		" \n" +
		"\n" +
		"	\n" +
		"	private String f2;\n" +
		"	\n" +
		"		\n" +
		"			\n" +
		"	\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	private String f1;\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	private String f2;\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"}\n"
	);
}

/**
 * @bug 332877: [formatter] line comment wrongly put on a new line
 * @test Ensure that the comment on last enum constant is not wrongly put on a new line
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=332877"
 */
public void testBug332877() throws Exception {
	String source =
		"public enum Environment {\n" +
		"    PROD,       // Production level environments\n" +
		"    STAGING    // Staging\n" +
		"}\n";
	formatSource(source,
		"public enum Environment {\n" +
		"	PROD, // Production level environments\n" +
		"	STAGING // Staging\n" +
		"}\n"
	);
}

/**
 * @bug 282988: [formatter] Option to align single-line comments in a column
 * @test Ensure that with line comment formatting turned off comment alignment doesn't change
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=282988"
 */
public void testBug282988() throws Exception {
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"package test;\n" +
		"\n" +
		"public class FormatterError {\n" +
		"	public void storeSomething(java.nio.ByteBuffer buffer) throws Exception {\n" +
		"		buffer.clear();\n" +
		"		buffer.putLong(0);     // backlink to previous version of this object\n" +
		"		buffer.putInt(1);      // version identifier\n" +
		"		buffer.flip();         // prepare to write\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package test;\n" +
		"\n" +
		"public class FormatterError {\n" +
		"	public void storeSomething(java.nio.ByteBuffer buffer) throws Exception {\n" +
		"		buffer.clear();\n" +
		"		buffer.putLong(0);     // backlink to previous version of this object\n" +
		"		buffer.putInt(1);      // version identifier\n" +
		"		buffer.flip();         // prepare to write\n" +
		"	}\n" +
		"}\n"
    );
}
public void testBug356851() throws Exception {
	String source =
		"public class X {\n" +
		"	public X LongMethodName(X x) {\n" +
		"		return x;\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		X x = new X();\n" +
		"		x = new X().LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x);\n" +
		"		System.out.println(x.hashCode());\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	public X LongMethodName(X x) {\n" +
		"		return x;\n" +
		"	}\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"		X x = new X();\n" +
		"		x = new X().LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x)\n" +
		"				.LongMethodName(x).LongMethodName(x).LongMethodName(x).LongMethodName(x);\n" +
		"		System.out.println(x.hashCode());\n" +
		"	}\n" +
		"}\n"
    );
}
/**
 * @bug 437639: [formatter] ArrayIndexOutOfBoundsException while formatting source code
 * @test test that the AIOOB is not generated
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=437639"
 */
public void testBug437639() throws Exception {
	this.formatterPrefs.blank_lines_between_import_groups = ~0;
	String source =
		"package com.test;\n" +
		"\n" +
		"import java.math.BigDecimal;\n" +
		"import java.math.BigInteger;\n" +
		"import java.util.ArrayList;\n" +
		"\n" +
		"\n" +
		"\n" +
		"//import java.util.Arrays;\n" +
		"import java.util.Date;\n" +
		"import java.util.List;\n" +
		"\n" +
		"public class Test {\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"		BigDecimal big = new BigDecimal(1);\n" +
		"		BigInteger bigI = 	new BigInteger(\"1\");\n" +
		"		Date d = new Date();\n" +
		"		List list = new ArrayList<>();\n" +
		"	}\n" +
		"}\n"
		;
	formatSource(source,
		"package com.test;\n" +
		"\n" +
		"import java.math.BigDecimal;\n" +
		"import java.math.BigInteger;\n" +
		"import java.util.ArrayList;\n" +
		"//import java.util.Arrays;\n" +
		"import java.util.Date;\n" +
		"import java.util.List;\n" +
		"\n" +
		"public class Test {\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"		BigDecimal big = new BigDecimal(1);\n" +
		"		BigInteger bigI = new BigInteger(\"1\");\n" +
		"		Date d = new Date();\n" +
		"		List list = new ArrayList<>();\n" +
		"	}\n" +
		"}\n"
    );
}
/**
 * @bug 460008: [formatter] Inserts wrong line breaks on ASTRewrite (Extract Constant, Extract Local Variable)
 * @test test line break is not added at end of expression
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
 * @bug 462945 - [formatter] IndexOutOfBoundsException in TokenManager
 * @test no exception is thrown for malformed code
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=462945"
 */
public void testBug462945() throws Exception {
	String source =
		"package p1;\n" +
		"enum ReviewResult {\n" +
		"	Good{, Bad\n" +
		"}\n";
	formatSource(source,
		"package p1;\n" +
		"\n" +
		"enum ReviewResult {\n" +
		"	Good{, Bad\n" +
		"}\n"
	);
}
public void testBug407629() throws Exception {
	String source =
			"public class X {\n" +
		"	/**\n" +
		"	 * Builds a {@link Level}.\n" +
		"	 * <p>\n" +
		"	 * Does <b>not</b> set :\n" +
		"	 * <ul>\n" +
		"	 * <li>{@link Level#setA(Boolean)</li>\n" +
		"	 * <li>{@link Level#setB(Long)}</li>\n" +
		"	 * <li>{@link Level#setC(Integer)}</li>\n" +
		"	 * </ul>\n" +
		"	 * </p>\n" +
		"	 */\n" +
		"	public static Level buildLevel() {\n" +
		"		return null;\n" +
		"	}\n" +
		"	 \n" +
		"}\n" +
		"\n" +
		"class Level {\n" +
		"	void setA(Boolean b) {}\n" +
		"	void setB(Long l) {}\n" +
		"	void setC(Integer i){}\n" +
		"}\n";
	String expected = "public class X {\n" +
			"	/**\n" +
			"	 * Builds a {@link Level}.\n" +
			"	 * <p>\n" +
			"	 * Does <b>not</b> set :\n" +
			"	 * <ul>\n" +
			"	 * <li>{@link Level#setA(Boolean)</li>\n" +
			"	 * <li>{@link Level#setB(Long)}</li>\n" +
			"	 * <li>{@link Level#setC(Integer)}</li>\n" +
			"	 * </ul>\n" +
			"	 * </p>\n" +
			"	 */\n" +
			"	public static Level buildLevel() {\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"\n" +
			"class Level {\n" +
			"	void setA(Boolean b) {\n" +
			"	}\n" +
			"\n" +
			"	void setB(Long l) {\n" +
			"	}\n" +
			"\n" +
			"	void setC(Integer i) {\n" +
			"	}\n" +
			"}\n";
	formatSource(source, expected);
}

public void testBug464312() throws Exception {
	String source = "/**/int f;";
	formatSource(source, source, CodeFormatter.K_STATEMENTS);
}
/**
 * @bug 458208: [formatter] follow up bug for comments
 * @test test a space is not added after a lambda expression in parenthesis
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=458208#c2"
 */
public void testBug458208() throws Exception {
	String source =
		"package p;\n" +
		"import java.util.function.IntConsumer;\n" +
		"class TestInlineLambda1 {\n" +
		"	{\n" +
		"		IntConsumer op = (x -> {}    );\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package p;\n" +
		"\n" +
		"import java.util.function.IntConsumer;\n" +
		"\n" +
		"class TestInlineLambda1 {\n" +
		"	{\n" +
		"		IntConsumer op = (x -> {\n" +
		"		});\n" +
		"	}\n" +
		"}\n"
	);
}
/**
 * @bug 458208: [formatter] follow up bug for comments
 * @test test that comments in switch statements are properly indented
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=458208#c21"
 */
public void testBug458208b() throws Exception {
	formatSource(
		"package p;\n" +
		"\n" +
		"public class C1 {\n" +
		"	void foo(int x) {\n" +
		"		switch (x) {\n" +
		"		// case 1\n" +
		"		case 1:\n" +
		"			break;\n" +
		"		// case 2\n" +
		"		case 2:\n" +
		"			break;\n" +
		"		// no more cases\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"	int bar(int x) {\n" +
		"		while (true) {\n" +
		"			int y = 9;\n" +
		"			switch (x) {\n" +
		"			// case 1\n" +
		"			case 1:\n" +
		"				// should return\n" +
		"				return y;\n" +
		"			// case 2\n" +
		"			case 2:\n" +
		"				// should break\n" +
		"				break;\n" +
		"			case 3:\n" +
		"				// TODO\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
/**
 * @bug 458208: [formatter] follow up bug for comments
 * @test test that elements separated with empty lines are properly indented
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=458208#c18"
 */
public void testBug458208c() throws Exception {
	final int wrapAllOnColumn = Alignment.M_NEXT_PER_LINE_SPLIT + Alignment.M_INDENT_ON_COLUMN + Alignment.M_FORCE;
	this.formatterPrefs.alignment_for_enum_constants = wrapAllOnColumn;
	this.formatterPrefs.alignment_for_arguments_in_enum_constant = wrapAllOnColumn;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = wrapAllOnColumn;
	String source =
		"package p;\n" +
		"\n" +
		"public enum TestEnum {\n" +
		"	FIRST_ENUM(\"first type\",\n" +
		"	           new SomeClass(),\n" +
		"	           new OtherEnumType[] { OtherEnumType.FOO }),\n" +
		"\n" +
		"	SECOND_ENUM(\"second type\",\n" +
		"	            new SomeClassOtherClass(),\n" +
		"	            new OtherEnumType[] { OtherEnumType.BAR }),\n" +
		"\n" +
		"	THIRD_ENUM(\"third type\",\n" +
		"	            new YetAnotherClass(),\n" +
		"	            new OtherEnumType[] { OtherEnumType.FOOBAR,\n" +
		"	                                  OtherEnumType.FOOBARBAZ,\n" +
		"\n" +
		"	                                  OtherEnumType.LONGERFOOBARBAZ,\n" +
		"	                                  OtherEnumType.MORELETTERSINHERE });\n" +
		"\n" +
		"	/* data members and methods go here */\n" +
		"	TestEnum(String s, Cls s1, OtherEnumType[] e) {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package p;\n" +
		"\n" +
		"public enum TestEnum {\n" +
		"						FIRST_ENUM(	\"first type\",\n" +
		"									new SomeClass(),\n" +
		"									new OtherEnumType[] { OtherEnumType.FOO }),\n" +
		"\n" +
		"						SECOND_ENUM(\"second type\",\n" +
		"									new SomeClassOtherClass(),\n" +
		"									new OtherEnumType[] { OtherEnumType.BAR }),\n" +
		"\n" +
		"						THIRD_ENUM(	\"third type\",\n" +
		"									new YetAnotherClass(),\n" +
		"									new OtherEnumType[] {	OtherEnumType.FOOBAR,\n" +
		"															OtherEnumType.FOOBARBAZ,\n" +
		"\n" +
		"															OtherEnumType.LONGERFOOBARBAZ,\n" +
		"															OtherEnumType.MORELETTERSINHERE });\n" +
		"\n" +
		"	/* data members and methods go here */\n" +
		"	TestEnum(String s, Cls s1, OtherEnumType[] e) {\n" +
		"	}\n" +
		"}"
	);
}
/**
 * @bug 458208: [formatter] follow up bug for comments
 * @test test that enum constants are not indented with spaces when "Use spaces to indent wrapped lines" is on
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=458208#c24"
 */
public void testBug458208d() throws Exception {
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_COMPACT_SPLIT;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	setPageWidth80();
	String source =
		"package p;\n" +
		"\n" +
		"public enum TestEnum {\n" +
		"	ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELWE, THIRTEEN, FOURTEEN, FIFTEEN;\n" +
		"}";
	formatSource(source,
		"package p;\n" +
		"\n" +
		"public enum TestEnum {\n" +
		"	ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELWE,\n" +
		"	THIRTEEN, FOURTEEN, FIFTEEN;\n" +
		"}"
	);
}
/**
 * @bug 465669: NPE in WrapExecutor during Java text formatting
 * @test test that no NPE is thrown
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=465669"
 */
public void testBug465669() throws Exception {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.indentation_size = 2;
	setPageWidth80();
	String source =
		"public class ffffffffffffffffff\r\n" +
		"{\r\n" +
		"  private static void test(String s)\r\n" +
		"  {\r\n" +
		"    dddd = (aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff)new dddddddddddddddd()\r\n" +
		"  .ttt(null, aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.class)\r\n" +
		"      .ttt(\"bbbbbbb\", xxxxxxxxx.class)\r\n" +
		"      .ttt(\"sssssssvvvvvvv\", new fffffffffff(\"xxxx\")\r\n" +
		"           .add(\"eeeeeeee\", aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.ssssssssssssss.class)\r\n" +
		"           .add(\"cccccccccc\", aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.wwwwwwwwwwwwwwww.class)\r\n" +
		"           )\r\n" +
		"      .bbbbbbbbbbb(s);\r\n" +
		"  }\r\n" +
		"  \r\n" +
		"}";
	formatSource(source,
		"public class ffffffffffffffffff {\r\n" +
		"  private static void test(String s) {\r\n" +
		"    dddd = (aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff) new dddddddddddddddd()\r\n" +
		"        .ttt(null, aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.class)\r\n" +
		"        .ttt(\"bbbbbbb\", xxxxxxxxx.class)\r\n" +
		"        .ttt(\"sssssssvvvvvvv\", new fffffffffff(\"xxxx\")\r\n" +
		"            .add(\"eeeeeeee\",\r\n" +
		"                aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.ssssssssssssss.class)\r\n" +
		"            .add(\"cccccccccc\",\r\n" +
		"                aaaaaaaaaaaaaaaaaaaaaaa.ffffffffffffffffff.wwwwwwwwwwwwwwww.class))\r\n" +
		"        .bbbbbbbbbbb(s);\r\n" +
		"  }\r\n" +
		"\r\n" +
		"}"
	);
}
public void testBug471090() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.indentation_size = 2;
	String source =
		"class FormatterBug {\n" +
		"// \\u00C4\n" +
		"}\n";
	formatSource(source,
		"class FormatterBug {\n" +
		"  // \\u00C4\n" +
		"}\n"
	);
}
/**
 * @bug 471364: [formatter] Method declarations in interfaces are sometimes indented incorrectly
 * @test test that methods without modifiers are properly indented
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=471364"
 */
public void testBug471364() throws JavaModelException {
	this.formatterPrefs.blank_lines_before_abstract_method = 0;
	this.formatterPrefs.alignment_for_method_declaration = Alignment.M_COMPACT_SPLIT;
	String source =
		"interface Example {\r\n" +
		"\r\n" +
		"	void method2();\r\n" +
		"	void method2();\r\n" +
		"\r\n" +
		"	void method3();\r\n" +
		"\r\n" +
		"	/**\r\n" +
		"	 * \r\n" +
		"	 */\r\n" +
		"	void method4();\r\n" +
		"\r\n" +
		"}";
	formatSource(source);

	source =
		"public class Example {\r\n" +
		"\r\n" +
		"	void method2();\r\n" +
		"	void method2();\r\n" +
		"\r\n" +
		"	void method3();\r\n" +
		"\r\n" +
		"	/**\r\n" +
		"	 * \r\n" +
		"	 */\r\n" +
		"	void method4();\r\n" +
		"\r\n" +
		"}";
	formatSource(source);
}
/**
 * @bug 471145: [Formatter] doesn't remove space before "{" on the if line
 * @test test that no unnecessary space is added
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=471145"
 */
public void testBug471145() throws JavaModelException {
	this.formatterPrefs.insert_space_before_opening_brace_in_block = false;
	this.formatterPrefs.keep_simple_if_on_one_line = true;
	String source =
		"class C {\r\n" +
		"	void method() {\r\n" +
		"		if (condition) {\r\n" +
		"			operation();\r\n" +
		"		}\r\n" +
		"		if (condition)// don't add space before comment\r\n" +
		"			operation();\r\n" +
		"		if (condition)operation();\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"class C {\r\n" +
		"	void method() {\r\n" +
		"		if (condition){\r\n" +
		"			operation();\r\n" +
		"		}\r\n" +
		"		if (condition)// don't add space before comment\r\n" +
		"			operation();\r\n" +
		"		if (condition) operation();\r\n" +
		"	}\r\n" +
		"}");
}
/**
 * @Bug 469438: ArrayIndexOutOfBoundsException in TokenManager.applyFormatOff (443)
 * @test test that no exception is thrown
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=469438"
 */
public void testBug469438() {
	this.formatterPrefs.use_tags = true;
	String source =
			"public class C1 {\r\n" +
			"	int     b;\r\n" +
			"\r\n" +
			"	/** @formatter:off */\r\n" +
			"	private void  a() {\r\n" +
			"		// @formatter:on\r\n" +
			"		if ()\r\n" +
			"	}\r\n" +
			"}";
	formatSource(source,
			"public class C1 {\r\n" +
			"	int b;\r\n" +
			"\r\n" +
			"	/** @formatter:off */\r\n" +
			"	private void  a() {\r\n" +
			"		// @formatter:on\r\n" +
			"		if ()\r\n" +
			"	}\r\n" +
			"}"
			);
}
/**
 * @bug 471883: NullPointerException in TokenManager.firstIndexIn (188)
 * @test test that no NPE is thrown
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=471883"
 */
public void testBug471883() throws Exception {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.indentation_size = 2;
	setPageWidth80();
	String source =
			"/**\r\n" +
			" * <pre>\r\n" +
			" * isInEncoding(char ch);\r\n" +
			" * </pre>\r\n" +
			" */\r\n" +
			"public class Try {\r\n" +
			"}";
	formatSource(source);
}
/**
 * @bug 470977: [formatter] Whitespace removed between assert and unary operator or primary expression
 * @test test that spaces after assert are correct
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=470977"
 */
public void testBug470977() throws Exception {
	String source =
		"public class TestFormat {\r\n" +
		"	public static void main(String[] args) {\r\n" +
		"		assert \"\".length() == 0;\r\n" +
		"		assert (!false);\r\n" +
		"\r\n" +
		"		assert !false;\r\n" +
		"		assert +0 == 0;\r\n" +
		"		assert -0 == 0;\r\n" +
		"\r\n" +
		"		int i = 0;\r\n" +
		"		assert ++i == 1;\r\n" +
		"		assert --i == 0;\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * @bug 472962: [formatter] Missing whitespace after >, ] in annotation type declaration
 * @test test that there is whitespace before element identifiers
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=472962"
 */
public void testBug472962() {
	String source =
		"public @interface A {\r\n" +
		"	String[] strings();\r\n" +
		"\r\n" +
		"	Class<String> stringClasses();\r\n" +
		"}";
	formatSource(source);
}
/**
 * @bug 470506: formatter option "align field in columns" changed in Mars
 * @test test that fields separated by extra blank lines are not considered separate groups when aligning
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=470506"
 */
public void testBug470506() {
	this.formatterPrefs.align_type_members_on_columns = true;
	String source =
		"class C {\r\n" +
		"	private int						iii;\r\n" +
		"	String							sss;\r\n" +
		"\r\n" +
		"	protected ArrayList<Integer>	aaa;\r\n" +
		"\r\n" +
		"}";
	formatSource(source);
}

/**
 * @bug 472205: Class extends generic type and implements another type, missing space after ">"
 */
public void testBug472205() {
	String source =
		"public class Test<E> extends ArrayList<String> implements Callable<String> {\n" +
		"}\n" +
		"\n" +
		"class A extends B<ClientListener> implements C {\n" +
		"}\n" +
		"\n" +
		"class D extends E<ClientListener> {\n" +
		"}\n" +
		"\n" +
		"class F implements G<ClientListener> {\n" +
		"}\n" +
		"\n" +
		"interface H extends I<ClientListener> {\n" +
		"}\n";
	formatSource(source);
}
/**
 * @bug 471780 - [formatter] Regression in enum value Javadoc formatting
 */
public void testBug471780() {
	String source =
		"public enum MyEnum {\r\n" +
		"	/** A. */\r\n" +
		"	A,\r\n" +
		"	/** B. */\r\n" +
		"	B\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/472009 - Formatter does not respect "keep else if on one line"
 */
public void testBug472009() {
	this.formatterPrefs.alignment_for_compact_if |= Alignment.M_FORCE;
	String source =
		"public class A {\r\n" +
		"	void a() {\r\n" +
		"		if (a == b) {\r\n" +
		"\r\n" +
		"		} else if (c == d) {\r\n" +
		"\r\n" +
		"		} else if (e == f) {\r\n" +
		"\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"}";
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
		"public enum E2 {\r\n" +
		"\r\n" +
		"	FOOBAR,\r\n" +
		"\r\n" +
		"	FOOBARBAZ,\r\n" +
		"\r\n" +
		"	FOO;\r\n" +
		"}";
	formatSource(source,
		"public enum E2 {\r\n" +
		"\r\n" +
		"				FOOBAR,\r\n" +
		"\r\n" +
		"				FOOBARBAZ,\r\n" +
		"\r\n" +
		"				FOO;\r\n" +
		"}"
	);
}
/**
 * @bug 474916: [formatter] Formatting GridBagLayout from Java 8 takes too long
 * @test test that formatting finishes in reasonable time
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=474916"
 */
public void testBug474916() {
	String source =
			"/**\r\n" +
			" * <                                                           \r\n" +
			" * >  <p style='color:red'> Test    </p>\r\n" +
			" *  <a title=\"I like to 'quote' it\" \r\n" +
			"href = 'http://www.eclipse.org'>Toast</a> */\r\n" +
			"class A {}";
	formatSource(source,
			"/**\r\n" +
			" * < >\r\n" +
			" * <p style='color:red'>\r\n" +
			" * Test\r\n" +
			" * </p>\r\n" +
			" * <a title=\"I like to 'quote' it\" href = 'http://www.eclipse.org'>Toast</a>\r\n" +
			" */\r\n" +
			"class A {\r\n" +
			"}"
	);
}
/**
 * https://bugs.eclipse.org/474918 - [formatter] doesn't align fields in declarations of annotations, enums and anonymous classes
 */
public void testBug474918() {
	useOldCommentWidthCounting();
	this.formatterPrefs.align_type_members_on_columns = true;
	String source =
		"import java.util.function.Function;\r\n" +
		"\r\n" +
		"public class A {\r\n" +
		"	private Function mapper = (Object a) -> {\r\n" +
		"		return a.toString().equals(\"test\");\r\n" +
		"	};\r\n" +
		"	String ssssssssssssssss = \"dsadaaaaaaaaaaaaaaaaaaaaaaaaa\";   //$NON-NLS-1$ // B // A\r\n" +
		"\r\n" +
		"	int bb = 4;\r\n" +
		"\r\n" +
		"	Object c = new Object() {\r\n" +
		"		int a = 55;\r\n" +
		"		Object cdddddddddddd = null;\r\n" +
		"	};\r\n" +
		"\r\n" +
		"	private enum E {\r\n" +
		"		AAA, BBB;\r\n" +
		"		int a = 55;\r\n" +
		"		String sssss = \"ssssss\";\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	private @interface II {\r\n" +
		"		int aaaaaa = 1;\r\n" +
		"		String bbbbbbbbb = \"default\";\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"import java.util.function.Function;\r\n" +
		"\r\n" +
		"public class A {\r\n" +
		"	private Function	mapper				= (Object a) -> {\r\n" +
		"												return a.toString().equals(\"test\");\r\n" +
		"											};\r\n" +
		"	String				ssssssssssssssss	= \"dsadaaaaaaaaaaaaaaaaaaaaaaaaa\";		//$NON-NLS-1$ //\r\n" +
		"																					// B\r\n" +
		"																					// //\r\n" +
		"																					// A\r\n" +
		"\r\n" +
		"	int					bb					= 4;\r\n" +
		"\r\n" +
		"	Object				c					= new Object() {\r\n" +
		"												int		a				= 55;\r\n" +
		"												Object	cdddddddddddd	= null;\r\n" +
		"											};\r\n" +
		"\r\n" +
		"	private enum E {\r\n" +
		"		AAA, BBB;\r\n" +
		"\r\n" +
		"		int		a		= 55;\r\n" +
		"		String	sssss	= \"ssssss\";\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	private @interface II {\r\n" +
		"		int		aaaaaa		= 1;\r\n" +
		"		String	bbbbbbbbb	= \"default\";\r\n" +
		"	}\r\n" +
		"}"
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
		"import java.util.function.Function;\r\n" +
		"\r\n" +
		"public class A {\r\n" +
		"	private Function mapper = (Object a) -> {\r\n" +
		"		return a.toString().equals(\"test\");\r\n" +
		"	};\r\n" +
		"	String ssssssssssssssss = \"dsadaaaaaaaaaaaaaaaaaaaaaaaaa\";   //$NON-NLS-1$ // B // A\r\n" +
		"\r\n" +
		"	int bb = 4;\r\n" +
		"\r\n" +
		"	Object c = new Object() {\r\n" +
		"		int a = 55;\r\n" +
		"		Object cdddddddddddd = null;\r\n" +
		"	};\r\n" +
		"\r\n" +
		"	private enum E {\r\n" +
		"		AAA, BBB;\r\n" +
		"		int a = 55;\r\n" +
		"		String sssss = \"ssssss\";\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	private @interface II {\r\n" +
		"		int aaaaaa = 1;\r\n" +
		"		String bbbbbbbbb = \"default\";\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"import java.util.function.Function;\r\n" +
		"\r\n" +
		"public class A {\r\n" +
		"    private Function mapper           = (Object a) -> {\r\n" +
		"                                          return a.toString().equals(\"test\");\r\n" +
		"                                      };\r\n" +
		"    String           ssssssssssssssss = \"dsadaaaaaaaaaaaaaaaaaaaaaaaaa\";     //$NON-NLS-1$ //\r\n" +
		"                                                                             // B\r\n" +
		"                                                                             // //\r\n" +
		"                                                                             // A\r\n" +
		"\r\n" +
		"    int              bb               = 4;\r\n" +
		"\r\n" +
		"    Object           c                = new Object() {\r\n" +
		"                                          int    a             = 55;\r\n" +
		"                                          Object cdddddddddddd = null;\r\n" +
		"                                      };\r\n" +
		"\r\n" +
		"    private enum E {\r\n" +
		"        AAA, BBB;\r\n" +
		"\r\n" +
		"        int    a     = 55;\r\n" +
		"        String sssss = \"ssssss\";\r\n" +
		"    }\r\n" +
		"\r\n" +
		"    private @interface II {\r\n" +
		"        int    aaaaaa    = 1;\r\n" +
		"        String bbbbbbbbb = \"default\";\r\n" +
		"    }\r\n" +
		"}"
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
		"import java.util.function.Function;\r\n" +
		"\r\n" +
		"public class A {\r\n" +
		"	private Function mapper = (Object a) -> {\r\n" +
		"		return a.toString().equals(\"test\");\r\n" +
		"	};\r\n" +
		"	String ssssssssssssssss = \"dsadaaaaaaaaaaaaaaaaaaaaaaaaa\";   //$NON-NLS-1$ // B // A\r\n" +
		"\r\n" +
		"	int bb = 4;\r\n" +
		"\r\n" +
		"	Object c = new Object() {\r\n" +
		"		int a = 55;\r\n" +
		"		Object cdddddddddddd = null;\r\n" +
		"	};\r\n" +
		"\r\n" +
		"	private enum E {\r\n" +
		"		AAA, BBB;\r\n" +
		"		int a = 55;\r\n" +
		"		String sssss = \"ssssss\";\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	private @interface II {\r\n" +
		"		int aaaaaa = 1;\r\n" +
		"		String bbbbbbbbb = \"default\";\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"import java.util.function.Function;\r\n" +
		"\r\n" +
		"public class A {\r\n" +
		"	private Function	mapper				= (Object a) -> {\r\n" +
		"												return a.toString().equals(\"test\");\r\n" +
		"											};\r\n" +
		"	String				ssssssssssssssss	= \"dsadaaaaaaaaaaaaaaaaaaaaaaaaa\";		//$NON-NLS-1$ //\r\n" +
		"	                                                                                // B\r\n" +
		"	                                                                                // //\r\n" +
		"	                                                                                // A\r\n" +
		"\r\n" +
		"	int					bb					= 4;\r\n" +
		"\r\n" +
		"	Object				c					= new Object() {\r\n" +
		"												int		a				= 55;\r\n" +
		"												Object	cdddddddddddd	= null;\r\n" +
		"											};\r\n" +
		"\r\n" +
		"	private enum E {\r\n" +
		"		AAA, BBB;\r\n" +
		"\r\n" +
		"		int		a		= 55;\r\n" +
		"		String	sssss	= \"ssssss\";\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	private @interface II {\r\n" +
		"		int		aaaaaa		= 1;\r\n" +
		"		String	bbbbbbbbb	= \"default\";\r\n" +
		"	}\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/475865 - JDT deletes code
 */
public void testBug475865() {
	String source =
		"public class Snippet {\r\n" +
		"\r\n" +
		"	Runnable disposeRunnable = this::dispose();\r\n" +
		"\r\n" +
		"	void dispose() {\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/435241 - [1.8][lambda][formatter] if/else within lambda is incorrectly formatted
 */
public void testBug435241() {
	this.formatterPrefs.brace_position_for_block = DefaultCodeFormatterConstants.NEXT_LINE;
	this.formatterPrefs.insert_new_line_before_else_in_if_statement = true;
	String source =
		"public class Snippet {\r\n" +
		"	public static void main(String[] args) {\r\n" +
		"		Executors.newSingleThreadExecutor().execute(() -> {\r\n" +
		"			if (true)\r\n" +
		"			{\r\n" +
		"				System.err.println(\"foo\");\r\n" +
		"			}\r\n" +
		"			else\r\n" +
		"			{\r\n" +
		"				System.err.println(\"bar\");\r\n" +
		"			}\r\n" +
		"		});\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/472815 - [formatter] 'Indent Empty lines' option doesn't work inside empty blocks
 */
public void testBug472815() {
	this.formatterPrefs.number_of_empty_lines_to_preserve = 2;
	String source =
		"public class Snippet {\r\n" +
		"\r\n" +
		"	int[] a1 = { };\r\n" +
		"	int[] a2 = {\r\n" +
		"	};\r\n" +
		"	int[] a3 = {\r\n" +
		"\r\n" +
		"	};\r\n" +
		"	int[] a4 = {\r\n" +
		"\r\n" +
		"\r\n" +
		"	};\r\n" +
		"	int[] a5 = {\r\n" +
		"\r\n" +
		"\r\n" +
		"\r\n" +
		"	};\r\n" +
		"\r\n" +
		"	void f1() { }\r\n" +
		"	void f2() {\r\n" +
		"	}\r\n" +
		"	void f3() {\r\n" +
		"\r\n" +
		"	}\r\n" +
		"	void f4() {\r\n" +
		"\r\n" +
		"\r\n" +
		"	}\r\n" +
		"	void f5() {\r\n" +
		"\r\n" +
		"\r\n" +
		"\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"public class Snippet {\r\n" +
		"\r\n" +
		"	int[] a1 = {};\r\n" +
		"	int[] a2 = {};\r\n" +
		"	int[] a3 = {\r\n" +
		"\r\n" +
		"	};\r\n" +
		"	int[] a4 = {\r\n" +
		"\r\n" +
		"\r\n" +
		"	};\r\n" +
		"	int[] a5 = {\r\n" +
		"\r\n" +
		"\r\n" +
		"	};\r\n" +
		"\r\n" +
		"	void f1() {\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	void f2() {\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	void f3() {\r\n" +
		"\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	void f4() {\r\n" +
		"\r\n" +
		"\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	void f5() {\r\n" +
		"\r\n" +
		"\r\n" +
		"	}\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/472815 - [formatter] 'Indent Empty lines' option doesn't work inside empty blocks
 */
public void testBug472815b() {
	this.formatterPrefs.number_of_empty_lines_to_preserve = 2;
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"public class Snippet {\r\n" +
		"\r\n" +
		"	int[] a1 = { };\r\n" +
		"	int[] a2 = {\r\n" +
		"	};\r\n" +
		"	int[] a3 = {\r\n" +
		"\r\n" +
		"	};\r\n" +
		"	int[] a4 = {\r\n" +
		"\r\n" +
		"\r\n" +
		"	};\r\n" +
		"	int[] a5 = {\r\n" +
		"\r\n" +
		"\r\n" +
		"\r\n" +
		"	};\r\n" +
		"\r\n" +
		"	void f1() { }\r\n" +
		"	void f2() {\r\n" +
		"	}\r\n" +
		"	void f3() {\r\n" +
		"\r\n" +
		"	}\r\n" +
		"	void f4() {\r\n" +
		"\r\n" +
		"\r\n" +
		"	}\r\n" +
		"	void f5() {\r\n" +
		"\r\n" +
		"\r\n" +
		"\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"public class Snippet {\r\n" +
		"	\r\n" +
		"	int[] a1 = {};\r\n" +
		"	int[] a2 = {};\r\n" +
		"	int[] a3 = {\r\n" +
		"			\r\n" +
		"	};\r\n" +
		"	int[] a4 = {\r\n" +
		"			\r\n" +
		"			\r\n" +
		"	};\r\n" +
		"	int[] a5 = {\r\n" +
		"			\r\n" +
		"			\r\n" +
		"	};\r\n" +
		"	\r\n" +
		"	void f1() {\r\n" +
		"	}\r\n" +
		"	\r\n" +
		"	void f2() {\r\n" +
		"	}\r\n" +
		"	\r\n" +
		"	void f3() {\r\n" +
		"		\r\n" +
		"	}\r\n" +
		"	\r\n" +
		"	void f4() {\r\n" +
		"		\r\n" +
		"		\r\n" +
		"	}\r\n" +
		"	\r\n" +
		"	void f5() {\r\n" +
		"		\r\n" +
		"		\r\n" +
		"	}\r\n" +
		"}"
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
		"class Snippet {\r\n" +
		"\r\n" +
		"	void foo1() {\r\n" +
		"		Other.bar(\r\n" +
		"			100,\r\n" +
		"			nestedMethod2Arg(\r\n" +
		"				nestedMethod1Arg(\r\n" +
		"					nestedMethod2Arg(nestedMethod1Arg(nestedMethod2Arg(\r\n" +
		"						nestedMethod1Arg(nestedMethod1Arg(nestedMethod1Arg(\r\n" +
		"							nested(200, 300, 400, 500, 600, 700, 800, 900)))),\r\n" +
		"						null)), null)),\r\n" +
		"				null),\r\n" +
		"			100);\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	void foo2() {\r\n" +
		"		nestedMethodAAAA(\r\n" +
		"			nestedMethodBBBB(\r\n" +
		"				nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r\n" +
		"				null));\r\n" +
		"		nestedMethodAAAA(nestedMethodBBBB(\r\n" +
		"			nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r\n" +
		"			null));\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	void foo3() {\r\n" +
		"		nestedMethodAAAA(\r\n" +
		"			nestedMethodBBBB(\r\n" +
		"				nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r\n" +
		"				null),\r\n" +
		"			null);\r\n" +
		"		nestedMethodAAAA(nestedMethodBBBB(\r\n" +
		"			nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r\n" +
		"			null), null);\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"class Snippet {\r\n" +
		"\r\n" +
		"	void foo1() {\r\n" +
		"		Other.bar(\r\n" +
		"			100,\r\n" +
		"			nestedMethod2Arg(\r\n" +
		"				nestedMethod1Arg(\r\n" +
		"					nestedMethod2Arg(\r\n" +
		"						nestedMethod1Arg(\r\n" +
		"							nestedMethod2Arg(\r\n" +
		"								nestedMethod1Arg(\r\n" +
		"									nestedMethod1Arg(\r\n" +
		"										nestedMethod1Arg(\r\n" +
		"											nested(\r\n" +
		"												200,\r\n" +
		"												300,\r\n" +
		"												400,\r\n" +
		"												500,\r\n" +
		"												600,\r\n" +
		"												700,\r\n" +
		"												800,\r\n" +
		"												900)))),\r\n" +
		"								null)),\r\n" +
		"						null)),\r\n" +
		"				null),\r\n" +
		"			100);\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	void foo2() {\r\n" +
		"		nestedMethodAAAA(\r\n" +
		"			nestedMethodBBBB(\r\n" +
		"				nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r\n" +
		"				null));\r\n" +
		"		nestedMethodAAAA(\r\n" +
		"			nestedMethodBBBB(\r\n" +
		"				nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r\n" +
		"				null));\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	void foo3() {\r\n" +
		"		nestedMethodAAAA(\r\n" +
		"			nestedMethodBBBB(\r\n" +
		"				nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r\n" +
		"				null),\r\n" +
		"			null);\r\n" +
		"		nestedMethodAAAA(\r\n" +
		"			nestedMethodBBBB(\r\n" +
		"				nestedMethodCCC(dddddd(200, 300, 400, 500, 600, 700, 800, 900)),\r\n" +
		"				null),\r\n" +
		"			null);\r\n" +
		"	}\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/475793 - [formatter] Incorrect whitespace after lambda block
 */
public void testBug475793() {
	this.formatterPrefs.keep_lambda_body_block_on_one_line = DefaultCodeFormatterConstants.ONE_LINE_IF_EMPTY;
	String source =
		"public class C {\r\n" +
		"	public void f() {\r\n" +
		"		Foo.bar(() -> {} , IllegalArgumentException.class);\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"public class C {\r\n" +
		"	public void f() {\r\n" +
		"		Foo.bar(() -> {}, IllegalArgumentException.class);\r\n" +
		"	}\r\n" +
		"}"
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
		"import java.awt.*;\r\n" +
		"\r\n" +
		"public class MyClass {\r\n" +
		"\r\n" +
		"	@Annotation( Arrays.asList( \"\" ))\r\n" +
		"	static Point p = new Point( x, y) {\r\n" +
		"		@Override\r\n" +
		"		public int hashCode( ) {\r\n" +
		"			return 42;\r\n" +
		"		}\r\n" +
		"	};\r\n" +
		"\r\n" +
		"	MyClass( @Annotation( \"annotationVal\" ) String s)\r\n" +
		"	{\r\n" +
		"		Foo.bar( ( @Annotation( \"annotationVal\" ) int a) -> { } , IllegalArgumentException.class );\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	public interface I {\r\n" +
		"		void m(int a);\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"import java.awt.*;\r\n" +
		"\r\n" +
		"public class MyClass {\r\n" +
		"\r\n" +
		"	@Annotation( Arrays.asList( \"\" ) )\r\n" +
		"	static Point p = new Point( x, y ) {\r\n" +
		"		@Override\r\n" +
		"		public int hashCode() {\r\n" +
		"			return 42;\r\n" +
		"		}\r\n" +
		"	};\r\n" +
		"\r\n" +
		"	MyClass( @Annotation( \"annotationVal\" ) String s ) {\r\n" +
		"		Foo.bar( ( @Annotation( \"annotationVal\" ) int a ) -> {}, IllegalArgumentException.class );\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	public interface I {\r\n" +
		"		void m( int a );\r\n" +
		"	}\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/477005 - [formatter] NullPointerException when first line is empty and indented
 */
public void testBug477005() {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.blank_lines_before_package = 2;
	String source =
		"\r\n" +
		"\r\n" +
		"package test;\r\n" +
		"\r\n" +
		"public class MyClass {\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/471202 - [formatter] Extra line break after annotation default expression
 */
public void testBug471202() {
	String source =
		"public @interface MyAnnotation {\r\n" +
		"	Attributes attributes() default @Attributes()\r\n" +
		"	;\r\n" +
		"\r\n" +
		"	@MyAnnotation(attributes = @Attributes() )\r\n" +
		"	String test();\r\n" +
		"}";
	formatSource(source,
		"public @interface MyAnnotation {\r\n" +
		"	Attributes attributes() default @Attributes();\r\n" +
		"\r\n" +
		"	@MyAnnotation(attributes = @Attributes())\r\n" +
		"	String test();\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/475791 - [formatter] Additional blank line before static initializer
 */
public void testBug475791() {
	this.formatterPrefs.blank_lines_before_new_chunk = 0;
	String source =
		"public class Example {\r\n" +
		"	static String staticField;\r\n" +
		"	static {}\r\n" +
		"	String field;\r\n" +
		"	{}\r\n" +
		"	static String staticField2;\r\n" +
		"	{}\r\n" +
		"	String field2;\r\n" +
		"	static {}\r\n" +
		"	static void staticMethod() {};\r\n" +
		"	static {}\r\n" +
		"	void method() {}\r\n" +
		"	static{}\r\n" +
		"	{}\r\n" +
		"	static class staticClass {};\r\n" +
		"	{}\r\n" +
		"	static{}\r\n" +
		"}";
	formatSource(source,
		"public class Example {\r\n" +
		"	static String staticField;\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"	String field;\r\n" +
		"	{\r\n" +
		"	}\r\n" +
		"	static String staticField2;\r\n" +
		"	{\r\n" +
		"	}\r\n" +
		"	String field2;\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"	static void staticMethod() {\r\n" +
		"	};\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"	void method() {\r\n" +
		"	}\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"	{\r\n" +
		"	}\r\n" +
		"	static class staticClass {\r\n" +
		"	};\r\n" +
		"	{\r\n" +
		"	}\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/475791 - [formatter] Additional blank line before static initializer
 */
public void testBug475791b() {
	this.formatterPrefs.blank_lines_before_new_chunk = 2;
	String source =
		"public class Example {\r\n" +
		"	static String staticField;\r\n" +
		"	static {}\r\n" +
		"	String field;\r\n" +
		"	{}\r\n" +
		"	static String staticField2;\r\n" +
		"	{}\r\n" +
		"	String field2;\r\n" +
		"	static {}\r\n" +
		"	static void staticMethod() {};\r\n" +
		"	static {}\r\n" +
		"	void method() {}\r\n" +
		"	static{}\r\n" +
		"	{}\r\n" +
		"	static class staticClass {};\r\n" +
		"	{}\r\n" +
		"	static{}\r\n" +
		"}";
	formatSource(source,
		"public class Example {\r\n" +
		"	static String staticField;\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"	String field;\r\n" +
		"	{\r\n" +
		"	}\r\n" +
		"	static String staticField2;\r\n" +
		"	{\r\n" +
		"	}\r\n" +
		"	String field2;\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"\r\n" +
		"\r\n" +
		"	static void staticMethod() {\r\n" +
		"	};\r\n" +
		"\r\n" +
		"\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"\r\n" +
		"\r\n" +
		"	void method() {\r\n" +
		"	}\r\n" +
		"\r\n" +
		"\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"	{\r\n" +
		"	}\r\n" +
		"\r\n" +
		"\r\n" +
		"	static class staticClass {\r\n" +
		"	};\r\n" +
		"\r\n" +
		"\r\n" +
		"	{\r\n" +
		"	}\r\n" +
		"	static {\r\n" +
		"	}\r\n" +
		"}"
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
		"public class Example {\r\n" +
		"	void foo() {\r\n" +
		"		Object o = new AbstractRegistryConfiguration() {\r\n" +
		"			public void configureRegistry() {\r\n" +
		"				registerConfigAttribute(\r\n" +
		"						new IExportFormatter() {\r\n" +
		"							public Object formatForExport() {\r\n" +
		"								return null;\r\n" +
		"							}\r\n" +
		"						},\r\n" +
		"						null);\r\n" +
		"			}\r\n" +
		"		};\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/480074 - [formatter] Wrong indentation on column for enum constants with javadoc
 */
public void testBug480074() {
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_NEXT_PER_LINE_SPLIT + Alignment.M_INDENT_ON_COLUMN;
	String source =
		"public class Example {\n" +
		"	private enum Something {\n" +
		"							/** hello */\n" +
		"							AAA,\n" +
		"							/** hello */\n" +
		"							BBB\n" +
		"	}\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/479959 - [formatter] indented empty lines after ifs and loops without braces
 */
public void testBug479959() {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.number_of_empty_lines_to_preserve = 2;
	String source =
		"public class Example {\r\n" +
		"	\r\n" +
		"	\r\n" +
		"	public boolean foo() {\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		if (foo())\r\n" +
		"			\r\n" +
		"			\r\n" +
		"			return foo();\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		while (foo())\r\n" +
		"			\r\n" +
		"			\r\n" +
		"			foo();\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		do\r\n" +
		"			\r\n" +
		"			\r\n" +
		"			foo();\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		while (foo());\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		if (foo()) {\r\n" +
		"			\r\n" +
		"			\r\n" +
		"			foo();\r\n" +
		"			\r\n" +
		"			\r\n" +
		"		}\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		if (foo())\r\n" +
		"			\r\n" +
		"			\r\n" +
		"			foo();\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		else\r\n" +
		"			\r\n" +
		"			\r\n" +
		"			foo();\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		for (int i = 0; i < 5; i++)\r\n" +
		"			\r\n" +
		"			\r\n" +
		"			foo();\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		switch (4) {\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		case 4:\r\n" +
		"			\r\n" +
		"			\r\n" +
		"			foo();\r\n" +
		"			break;\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		case 5: {\r\n" +
		"			\r\n" +
		"			\r\n" +
		"			break;\r\n" +
		"		}\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		case 6:\r\n" +
		"		}\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		return false;\r\n" +
		"		\r\n" +
		"		\r\n" +
		"	}\r\n" +
		"	\r\n" +
		"	\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/480086 - [formatter] unwanted spaces in generic diamond
 */
public void testBug480086() {
	this.formatterPrefs.insert_space_after_opening_angle_bracket_in_parameterized_type_reference = true;
	this.formatterPrefs.insert_space_before_closing_angle_bracket_in_parameterized_type_reference = true;
	String source =
		"public class Test {\r\n" +
		"	private ArrayList< String > ss = new ArrayList<>();\r\n" +
		"}";
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
		"public class Test {\r\n" +
		"	public enum Enum0 {\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	public enum Enum1 {\r\n" +
		"		;\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	public enum Enum2 {\r\n" +
		"		,;\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	public enum Enum3 {\r\n" +
		"		,\r\n" +
		"		;\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	public enum Enum4 {\r\n" +
		"		AAA,;\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	public enum Enum5 {\r\n" +
		"		AAA,\r\n" +
		"		;\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/481221 - [formatter] New formatter incorrectly formats ", ;" in enum declaration
 */
public void testBug481221b() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_COMPACT_SPLIT + Alignment.M_INDENT_ON_COLUMN;
	String source =
		"public class Test {\r\n" +
		"	public enum Enum1 {\r\n" +
		"		,\r\n" +
		"		;\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	public enum Enum2 {\r\n" +
		"						AAA,\r\n" +
		"						;\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/483922 - [formatter] Wrong indentation base for wrapped "throws" elements in method declaration
 */
public void testBug483922a() {
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT + Alignment.M_FORCE + Alignment.M_INDENT_ON_COLUMN;
	this.formatterPrefs.alignment_for_throws_clause_in_method_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT + Alignment.M_FORCE;
	String source =
		"public class Test {\r\n" +
		"	public void foo(\r\n" +
		"					int a, int b)\r\n" +
		"			throws Exception {\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/483922 - [formatter] [formatter] Wrong indentation base for wrapped "throws" elements in method declaration
 */
public void testBug483922b() {
	this.formatterPrefs.alignment_for_parameters_in_constructor_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT + Alignment.M_FORCE + Alignment.M_INDENT_ON_COLUMN;
	this.formatterPrefs.alignment_for_throws_clause_in_constructor_declaration = Alignment.M_COMPACT_FIRST_BREAK_SPLIT + Alignment.M_FORCE;
	String source =
			"public class Test {\r\n" +
			"	public Test(\r\n" +
			"				int a, int b)\r\n" +
			"			throws Exception {\r\n" +
			"	}\r\n" +
			"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/485163 - [formatter] Incorrect indentation after line wrap
 */
public void testBug485163() {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.blank_lines_before_field = 1;
	String source =
		"public class Test {\r\n" +
		"\r\n" +
		"	public String sssss1 = \".................................................\" + \"...........................................\";\r\n" +
		"	public String sssss2 = \".................................................\" + \"...........................................\";\r\n" +
		"\r\n" +
		"	public String sssss3 = \".................................................\" + \"...........................................\";\r\n" +
		"\r\n" +
		"	public void foo() {\r\n" +
		"\r\n" +
		"		String sssss = \".................................................\" + \"...........................................\";\r\n" +
		"\r\n" +
		"		Object o =\r\n" +
		"\r\n" +
		"		new Object() {\r\n" +
		"\r\n" +
		"			int a;\r\n" +
		"\r\n" +
		"			void foo() {\r\n" +
		"\r\n" +
		"				String sssss1 = \".................................................\" + \"...........................................\";\r\n" +
		"\r\n" +
		"				String sssss2 = \".................................................\" + \"...........................................\";\r\n" +
		"\r\n" +
		"			}\r\n" +
		"\r\n" +
		"		};\r\n" +
		"\r\n" +
		"		new Object() {\r\n" +
		"\r\n" +
		"			int a;\r\n" +
		"\r\n" +
		"			void foo() {\r\n" +
		"\r\n" +
		"				String sssss1 = \".................................................\" + \"...........................................\";\r\n" +
		"\r\n" +
		"				String sssss2 = \".................................................\" + \"...........................................\";\r\n" +
		"\r\n" +
		"			}\r\n" +
		"\r\n" +
		"		};\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"public class Test {\r\n" +
		"	\r\n" +
		"	public String sssss1 = \".................................................\"\r\n" +
		"			+ \"...........................................\";\r\n" +
		"	\r\n" +
		"	public String sssss2 = \".................................................\"\r\n" +
		"			+ \"...........................................\";\r\n" +
		"	\r\n" +
		"	public String sssss3 = \".................................................\"\r\n" +
		"			+ \"...........................................\";\r\n" +
		"	\r\n" +
		"	public void foo() {\r\n" +
		"		\r\n" +
		"		String sssss = \".................................................\"\r\n" +
		"				+ \"...........................................\";\r\n" +
		"		\r\n" +
		"		Object o =\r\n" +
		"				\r\n" +
		"				new Object() {\r\n" +
		"					\r\n" +
		"					int a;\r\n" +
		"					\r\n" +
		"					void foo() {\r\n" +
		"						\r\n" +
		"						String sssss1 = \".................................................\"\r\n" +
		"								+ \"...........................................\";\r\n" +
		"						\r\n" +
		"						String sssss2 = \".................................................\"\r\n" +
		"								+ \"...........................................\";\r\n" +
		"						\r\n" +
		"					}\r\n" +
		"					\r\n" +
		"				};\r\n" +
		"		\r\n" +
		"		new Object() {\r\n" +
		"			\r\n" +
		"			int a;\r\n" +
		"			\r\n" +
		"			void foo() {\r\n" +
		"				\r\n" +
		"				String sssss1 = \".................................................\"\r\n" +
		"						+ \"...........................................\";\r\n" +
		"				\r\n" +
		"				String sssss2 = \".................................................\"\r\n" +
		"						+ \"...........................................\";\r\n" +
		"				\r\n" +
		"			}\r\n" +
		"			\r\n" +
		"		};\r\n" +
		"	}\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/479898 - [formatter] removes whitespace between final and first exception in multi-line multi-catch
 */
public void testBug479898() {
	this.formatterPrefs.alignment_for_union_type_in_multicatch = Alignment.M_COMPACT_SPLIT + Alignment.M_INDENT_ON_COLUMN;
	String source =
		"public class FormattingTest {\r\n" +
		"	public void formatterTest() {\r\n" +
		"		try {\r\n" +
		"		} catch (final	InstantiationException | IllegalAccessException | IllegalArgumentException\r\n" +
		"						| NoSuchMethodException e) {\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/485276 - [formatter] another ArrayIndexOutOfBoundsException while formatting code
 */
public void testBug485276() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_COMPACT_SPLIT + Alignment.M_INDENT_BY_ONE;
	String source =
		"public class PostSaveListenerCleanUpExceptionTest {\r\n" +
		"	public Object[][] dataProvider() {\r\n" +
		"		return new Object[][] { { new String() // comment 1\r\n" +
		"				}, { new String() } };\r\n" +
		"	}\r\n" +
		"\r\n" +
		"	Object o = new Object() {\r\n" +
		"		public Object[][] dataProvider() {\r\n" +
		"			return new Object[][] { { new String() // comment 1\r\n" +
		"					}, { new String() } };\r\n" +
		"		}\r\n" +
		"	};\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/487375 - [formatter] block comment in front of method signature effects too much indentation
 */
public void testBug487375() {
	String source =
		"public class Test {\r\n" +
		"	/* public */ void foo() {\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/489797 - [formatter] 'Indent empty lines' sometimes doesn't work with 'format edited lines' save action
 */
public void testBug489797a() {
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"public class Example {\r\n" +
		"	public void foo() {\r\n" +
		"		if (true)\r\n" +
		"			return;\r\n" +
		"[##]\r\n" +
		"		return;\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"public class Example {\r\n" +
		"	public void foo() {\r\n" +
		"		if (true)\r\n" +
		"			return;\r\n" +
		"		\r\n" +
		"		return;\r\n" +
		"	}\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/489797 - [formatter] 'Indent empty lines' sometimes doesn't work with 'format edited lines' save action
 */
public void testBug489797b() {
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"public class Example {\r\n" +
		"	public void foo() {\r\n" +
		"		if (true)\r\n" +
		"			return;\r\n" +
		"[#		#]\r\n" +
		"		return;\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"public class Example {\r\n" +
		"	public void foo() {\r\n" +
		"		if (true)\r\n" +
		"			return;\r\n" +
		"		\r\n" +
		"		return;\r\n" +
		"	}\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/489797 - [formatter] 'Indent empty lines' sometimes doesn't work with 'format edited lines' save action
 */
public void testBug489797c() {
	this.formatterPrefs.indent_empty_lines = true;
	this.formatterPrefs.number_of_empty_lines_to_preserve = 5;
	String source =
		"public class Example {\r\n" +
		"	public void foo() {\r\n" +
		"		if (true)\r\n" +
		"			return;\r\n" +
		"[#\r\n" +
		"#]\r\n" +
		"		return;\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"public class Example {\r\n" +
		"	public void foo() {\r\n" +
		"		if (true)\r\n" +
		"			return;\r\n" +
		"		\r\n" +
		"		\r\n" +
		"		return;\r\n" +
		"	}\r\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/488898 - [formatter] Disabled options still have effect
 */
public void testBug488898() {
	this.formatterPrefs.alignment_for_parameters_in_method_declaration = Alignment.M_NO_ALIGNMENT + Alignment.M_FORCE;
	String source =
		"class Example {\r\n" +
		"	void foo(int bar) {\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/492735 - [formatter] Excessive wrapping in a complex expression
 */
public void testBug492735() {
	this.formatterPrefs.alignment_for_assignment = Alignment.M_COMPACT_SPLIT;
	this.formatterPrefs.page_width = 60;
	String source =
		"class FormatterIssue {\r\n" +
		"	String[] S = new String[] {\r\n" +
		"			foo(\"first line  xxxxxxxxxxx\", \"y\", \"z\"),\r\n" +
		"			foo(\"second line xxxxxxxxxxxxxxxxxxx\", \"b\",\r\n" +
		"					\"c\"), };\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/494831 - Formatter ignores whitespace rules for diamond operator
 */
public void testBug494831() {
	this.formatterPrefs.insert_space_before_opening_angle_bracket_in_parameterized_type_reference = true;
	String source =
		"class Example {\r\n" +
		"	List <String> list = new ArrayList <>();\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/497245 - [formatter] Comment between "if" and statement breaks formatting
 */
public void testBug497245a() {
	String source =
		"public class Test {\r\n" +
		"	void method() {\r\n" +
		"		if (true)\r\n" +
		"			// comment\r\n" +
		"			if (false)\r\n" +
		"				method();\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/497245 - [formatter] Comment between "if" and statement breaks formatting
 */
public void testBug497245b() {
	this.formatterPrefs.keep_then_statement_on_same_line = true;
	String source =
		"public class Test {\r\n" +
		"	void method() {\r\n" +
		"		if (true)\r\n" +
		"			// comment\r\n" +
		"			if (false) method();\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500443 - [formatter] NPE on block comment before 'force-wrap' element
 */
public void testBug500443() {
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_ONE_PER_LINE_SPLIT + Alignment.M_FORCE;
	this.formatterPrefs.alignment_for_superclass_in_type_declaration = Alignment.M_ONE_PER_LINE_SPLIT + Alignment.M_FORCE;
	String source =
		"public class SomeClass\n" +
		"		/* */ extends\n" +
		"		Object {\n" +
		"	enum MyEnum {\n" +
		"		/* 1 */ ONE\n" +
		"	}\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500092 - [formatter] Blank lines at beginning of method body doesn't work in constructors
 */
public void testBug500092() {
	this.formatterPrefs.blank_lines_at_beginning_of_method_body = 1;
	String source =
		"public class Test {\n" +
		"	public Test() { int a; }\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"	public Test() {\n" +
		"\n" +
		"		int a;\n" +
		"	}\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/500135 - [formatter] 'Parenthesis positions' ignores single member annotations
 */
public void testBug500135() {
	this.formatterPrefs.parenthesis_positions_in_annotation = DefaultCodeFormatterConstants.SEPARATE_LINES;
	String source =
		"@SomeAnnotation(\n" +
		"	\"some value\"\n" +
		")\n" +
		"public class Test {\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500096 - [formatter] Indent declarations within enum declaration doesn't affect enum constants
 */
public void testBug500096a() {
	this.formatterPrefs.indent_body_declarations_compare_to_enum_declaration_header = false;
	String source =
		"public enum Test {\n" +
		"AAA, BBB;\n" +
		"\n" +
		"Test() {\n" +
		"}\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500096 - [formatter] Indent declarations within enum declaration doesn't affect enum constants
 */
public void testBug500096b() {
	this.formatterPrefs.indent_body_declarations_compare_to_enum_declaration_header = false;
	this.formatterPrefs.alignment_for_enum_constants = Alignment.M_COMPACT_SPLIT + Alignment.M_INDENT_BY_ONE;
	String source =
		"public enum Test {\n" +
		"	AAA, BBB;\n" +
		"\n" +
		"Test() {\n" +
		"}\n" +
		"}";
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
		"public class SomeClass {\n" +
		"	void foo() {\n" +
		"		Arrays.asList(new String[] { \"ddd\", \"eee\", \"fff\" });\n" +
		"		Arrays.asList(new String[] { \"a\", \"b\", \"c\" },\n" +
		"		        new String[]\n" +
		"		        { \"a\", \"b\", \"c\", });\n" +
		"		Arrays.asList(//\n" +
		"		        new String[]\n" +
		"		        { \"ddd\", \"eee\", \"fff\" });\n" +
		"		Arrays.asList(\n" +
		"		        new String[]\n" +
		"		        { \"eedd\", \"eee\", \"fff\" });\n" +
		"		Arrays.asList(\n" +
		"		        new String[]\n" +
		"		        { \"aa\", \"bb\", \"cc\", \"dd\", \"ee\", \"ff\", \"gg\",\n" +
		"		                \"hh\", \"ii\" });\n" +
		"		String[][] test = { { \"aaaaaa\", \"bbbbb\", \"ccccc\" },\n" +
		"		        { \"aaaa\", \"bb\", \"ccc\" } };\n" +
		"		test[123456 //\n" +
		"		        * (234567 + 345678 + 456789 - 567890\n" +
		"		                - 678901)] = new String[]\n" +
		"		                { \"a\", \"b\", \"c\" };\n" +
		"	}\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500853 - [Formatter] java code formatter doesn't honour new parentheses settings
 */
public void testBug500853() {
	this.formatterPrefs.parenthesis_positions_in_method_declaration = new String(DefaultCodeFormatterConstants.PRESERVE_POSITIONS);
	String source =
		"public class SomeClass {\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500853 - [formatter] Errors around formatter:off regions with "use space to indent wrapped lines"
 * @test no {@code IndexOutOfBoundsException} is thrown
 */
public void testBug512791a() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void f() {\n" +
		"		int a = 1 + 2 + 3 + 4;\n" +
		"		f  (   ;\n" +
		"	}\n" +
		"\n" +
		"	Object o = new Object() {\n" +
		"	};\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/500853 - [formatter] Errors around formatter:off regions with "use space to indent wrapped lines"
 * @test formatter doesn't get stuck in an infinite loop
 */
public void testBug512791b() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void f() {\n" +
		"		f  (   ;\n" +
		"	}\n" +
		"\n" +
		"	Object o = new Object() {\n" +
		"		int a;\n" +
		"	};\n" +
		"}";
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
		"public class Test {\n" +
		"\n" +
		"	String s = new StringBuilder()\n" +
		"// .append(\"aa\")\n" +
		"	        .toString();\n" +
		"}";
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
		"public class Test {\n" +
		"\n" +
		"	String s = new StringBuilder()\n" +
		"/* .append(\"aa\") */\n" +
		"	        .toString();\n" +
		"}";
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
		"class Test {\n" +
		"	String s = aaaaaaa()\n" +
		"			.bbbbbbbb()\n" +
		"			.ccccccccc()\n" +
		"			.ddddddddddddd(\"eeeeeeee\" + \"fffffff\");\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/526992 - [formatter] Never indent line comments
 * on first column - crash in anonymous class inside array declaration
 */
public void testBug526992a() {
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"public class Test {\n" +
		"	Object o = new Object[] { new Object() {\n" +
		"//\n" +
		"	} };\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/526992 - [formatter] Never indent line comments
 * on first column - crash in anonymous class inside array declaration
 */
public void testBug526992b() {
	this.formatterPrefs.never_indent_block_comments_on_first_column = true;
	String source =
		"public class Test {\n" +
		"	Object o = new Object[] { new Object() {\n" +
		"/**/\n" +
		"	} };\n" +
		"}";
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
		"class Test {\n" +
		"	boolean someVariable\n" +
		"			// comment\n" +
		"			= true;\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/531981 - [formatter] Error on &lt;code&gt;
 * spanning multiple Javadoc tags
 */
public void testBug531981() {
	this.formatterPrefs.comment_indent_parameter_description = true;
	String source =
		"/**\n" +
		" * <code>a<code>\n" +
		" *\n" +
		" * @param   b\n" +
		" *               c\n" +
		" *            d</code>\n" +
		" */\n" +
		"class Test {\n" +
		"}";
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
		"class C {\n" +
		"	int		a	= 1; // comment1\n" +
		"	String	bb	= null;   // comment2\n" +
		"}";
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
		"class C {\n" +
		"	int		a	= 1; /* comment1 */\n" +
		"	String	bb	= \"\";   //$NON-NLS-1$\n" +
		"}";
	formatSource(source,
		"class C {\n" +
		"	int		a	= 1;	/* comment1 */\n" +
		"	String	bb	= \"\";   //$NON-NLS-1$\n" +
		"}"
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
		"/**\n" +
		" * @param args a b c d e f\n" +
		" */\n" +
		"public class C {\n" +
		"}";
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
		"class C {\n" +
		"	String ssssssssssss = fffffffffffffffff(\"aaaaaaaaaaaaaaaaa\", bbbbbbbbbbbbbbbbbb); //$NON-NLS-1$\n" +
		"}";
	formatSource(source,
		"class C {\n" +
		"	String ssssssssssss = fffffffffffffffff(\"aaaaaaaaaaaaaaaaa\", //$NON-NLS-1$\n" +
		"			bbbbbbbbbbbbbbbbbb);\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/536322 - Java formatter misses one level of
 * indentation in enum declaration if Javadoc is present
 */
public void testBug536322() {
	String source =
		"class C {\n" +
		"	/** */\n" +
		"	enum E {\n" +
		"		enum1;\n" +
		"	}\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/536552 - Freeze when formatting Java source code
 */
public void testBug536552a() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"// comment\n" +
		"class C {\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/536552 - Freeze when formatting Java source code
 */
public void testBug536552b() {
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	this.formatterPrefs.never_indent_block_comments_on_first_column = true;
	String source =
		"/* comment */\n" +
		"class C {\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/542625 - Formatter fails with OOM when parentheses for if statements are preserving positions
 */
public void testBug542625() {
	this.formatterPrefs.parenthesis_positions_in_if_while_statement = DefaultCodeFormatterConstants.PRESERVE_POSITIONS;
	String source =
		"class C {\n" +
		"	void m() {\n" +
		"		//\n" +
		"		//\n" +
		"		if (\n" +
		"			true)\n" +
		"			;\n" +
		"	}\n" +
		"}";
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
		"class Example {\n" +
		"	int foo(int argument) {\n" +
		"		if (argument == 0)\n" +
		"		    return 0;\n" +
		"		if (argument == 1)\n" +
		"		    return 42;\n" +
		"		else\n" +
		"		    return 43;\n" +
		"	}\n" +
		"}";
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
		"public enum TestEnum {\n" +
		"\n" +
		"\n" +
		"	ONE, TWO, THREE;\n" +
		"\n" +
		"\n" +
		"\n" +
		"\n" +
		"	public int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"\n" +
		"\n" +
		"\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/413193 - [formatter] Blank lines before the first declarations and declarations of same kind not respected in enums
 */
public void testBug413193b() {
	this.formatterPrefs.blank_lines_before_first_class_body_declaration = 2;
	this.formatterPrefs.blank_lines_after_last_class_body_declaration = 3;
	this.formatterPrefs.blank_lines_before_new_chunk = 4;
	formatSource(
		"public enum TestEnum {\n" +
		"	ONE, TWO, THREE;\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/413193 - [formatter] Blank lines before the first declarations and declarations of same kind not respected in enums
 */
public void testBug413193c() {
	this.formatterPrefs.blank_lines_before_first_class_body_declaration = ~0;
	this.formatterPrefs.blank_lines_after_last_class_body_declaration = ~0;
	this.formatterPrefs.blank_lines_before_new_chunk = ~0;
	String source =
		"public enum TestEnum {\n" +
		"\n" +
		"	ONE, TWO, THREE;\n" +
		"\n" +
		"	public int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"\n" +
		"}";
	formatSource(source,
		"public enum TestEnum {\n" +
		"	ONE, TWO, THREE;\n" +
		"	public int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/551189 - Consistent ArrayIndexOutOfBounds when saving an incorrect Java file when code clean-up is enabled
 */
public void testBug551189() {
	formatSource(
		"public class AAA {\n" +
		"\n" +
		"import java.awt.*;\n" +
		"\n" +
		"public class BBB {\n" +
		"    int a;\n" +
		"\n" +
		"}}");
}
/**
 * https://bugs.eclipse.org/220713 - [formatter] Formatting of array initializers in method calls
 */
public void testBug220713() {
	this.formatterPrefs.alignment_for_arguments_in_method_invocation = Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_INDENT_ON_COLUMN | Alignment.M_FORCE;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = Alignment.M_NEXT_SHIFTED_SPLIT | Alignment.M_INDENT_ON_COLUMN | Alignment.M_FORCE;
	this.formatterPrefs.insert_new_line_before_closing_brace_in_array_initializer = true;
	formatSource(
		"public class A {\n" +
		"	void f() {\n" +
		"		methodWithArrays(	new Object[] {\n" +
		"											null,\n" +
		"							},\n" +
		"							new Object[] {\n" +
		"											null,\n" +
		"							});\n" +
		"	}\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/558421 [formatter] Generate getter/setter creates unnecessary blank line
 */
public void testBug558421() {
	this.formatterPrefs.blank_lines_after_last_class_body_declaration = 1;
	String source =
		"public int getA() {\n" +
		"	return a;\n" +
		"}";
	formatSource(source, source, CodeFormatter.K_CLASS_BODY_DECLARATIONS);
}
/**
 * https://bugs.eclipse.org/250656 - [formatter] Formatting selection destroys correct indentation
 */
public void testBug250656() {
	this.formatterPrefs.page_width = 50;
	formatSource(
		"class C {\n" +
		"	void f() {\n" +
		"		doSomething(aaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbb\n" +
		"[#				+ ccccccccccccccccccc);#]\n" +
		"	}\n" +
		"}",
		"class C {\n" +
		"	void f() {\n" +
		"		doSomething(aaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbb\n" +
		"				+ ccccccccccccccccccc);\n" +
		"	}\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/559006 - [formatter] Wrong indentation in region after wrapped line
 */
public void testBug559006() {
	this.formatterPrefs.page_width = 50;
	formatSource(
		"class C {\n" +
		"	void f() {\n" +
		"		doSomething(aaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbb,\n" +
		"				+ ccccccccccccccccccc);\n" +
		"[#		doSomethingElse();#]\n" +
		"	}\n" +
		"}",
		"class C {\n" +
		"	void f() {\n" +
		"		doSomething(aaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbb,\n" +
		"				+ ccccccccccccccccccc);\n" +
		"		doSomethingElse();\n" +
		"	}\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/560889 - [formatter] Unneeded wraps with "Format edited lines" save action
 */
public void testBug560889() {
	this.formatterPrefs.page_width = 50;
	formatSource(
		"class C {\n" +
		"	void f() {\n" +
		"[#		doSomething(aaaaaaaaaaaaaaaaaa)#]\n" +
		"				.andThen(ccccccccccccccccccc);\n" +
		"	}\n" +
		"}",
		"class C {\n" +
		"	void f() {\n" +
		"		doSomething(aaaaaaaaaaaaaaaaaa)\n" +
		"				.andThen(ccccccccccccccccccc);\n" +
		"	}\n" +
		"}");
}
public void testBug563487a() {
	formatSource(
		"class A {\n" +
		"	protected void f() {\n" +
		"		cccccccccccccc\n" +
		"				// \n" +
		"				.forEach(c -> {\n" +
		"					aaaaaa();\n" +
		"[#					bbbbbb();#]\n" +
		"				});\n" +
		"	}\n" +
		"}",
		"class A {\n" +
		"	protected void f() {\n" +
		"		cccccccccccccc\n" +
		"				// \n" +
		"				.forEach(c -> {\n" +
		"					aaaaaa();\n" +
		"					bbbbbb();\n" +
		"				});\n" +
		"	}\n" +
		"}");
}
public void testBug563487b() {
	formatSource(
		"class A {\n" +
		"	protected void f() {\n" +
		"		cccccccccccccc\n" +
		"				// \n" +
		"					.forEach(c -> {\n" +
		"						aaaaaa();\n" +
		"[#					bbbbbb();#]\n" +
		"					});\n" +
		"	}\n" +
		"}",
		"class A {\n" +
		"	protected void f() {\n" +
		"		cccccccccccccc\n" +
		"				// \n" +
		"					.forEach(c -> {\n" +
		"						aaaaaa();\n" +
		"					bbbbbb();\n" +
		"					});\n" +
		"	}\n" +
		"}");
}
public void testBug563487c() {
	formatSource(
		"class A {\n" +
		"protected void f() {\n" +
		"cccccccccccccc\n" +
		"		// \n" +
		"		.forEach(c -> {\n" +
		"			aaaaaa();\n" +
		"[#			bbbbbb();#]\n" +
		"		});\n" +
		"}\n" +
		"}",
		"class A {\n" +
		"protected void f() {\n" +
		"cccccccccccccc\n" +
		"		// \n" +
		"		.forEach(c -> {\n" +
		"			aaaaaa();\n" +
		"			bbbbbb();\n" +
		"		});\n" +
		"}\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/565053 - [formatter] Parenthesis in "separate lines if wrapped": wrapping disruptions
 */
public void testBug565053a() {
	this.formatterPrefs.parenthesis_positions_in_method_invocation = DefaultCodeFormatterConstants.SEPARATE_LINES_IF_WRAPPED;
	this.formatterPrefs.page_width = 92;
	formatSource(
		"class Example {\n" +
		"\n" +
		"	List SUPPORTED_THINGS = asList(\n" +
		"			new Thing(\n" +
		"					\"rocodileaaadasgasgasgasgasgasgaaaaasgsgasgasgasgasfafghasfaa aaadad\"\n" +
		"			), \"new Thing()\"\n" +
		"	);\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/565053 - [formatter] Parenthesis in "separate lines if wrapped": wrapping disruptions
 */
public void testBug565053b() {
	this.formatterPrefs.parenthesis_positions_in_method_invocation = DefaultCodeFormatterConstants.SEPARATE_LINES_IF_WRAPPED;
	this.formatterPrefs.page_width = 100;
	formatSource(
		"class Example {\n" +
		"\n" +
		"	List SUPPORTED_THINGS = asList(\n" +
		"			new Thing(\"rocodileaaadasgasgasgasgasgasgaaaaasgsgasgasgasgasfafghasfaa aaadad\")\n" +
		"			\"new Thing()\"\n" +
		"	);\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/567714 - [15] Formatting record file moves annotation to the line of record declaration
 */
public void testBug567714() {
	formatSource(
		"@SuppressWarnings(\"preview\")\n" +
		"@Deprecated\n" +
		"public record X(int i) {\n" +
		"	public X(int i) {\n" +
		"		this.i = i;\n" +
		"	}\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/569798 - [formatter] Brace position - next line indented: bug for array within annotation
 */
public void testBug569798() {
	this.formatterPrefs.brace_position_for_array_initializer = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
	formatSource(
		"class Test {\n" +
		"	@Nullable\n" +
		"	@SuppressWarnings(\n" +
		"		{ \"\" })\n" +
		"	@Something(a =\n" +
		"		{ \"\" })\n" +
		"	void f() {\n" +
		"	}\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/569964 - [formatter] Keep braced code on one line: problem with comments after javadoc
 */
public void testBug569964() {
	this.formatterPrefs.keep_method_body_on_one_line = DefaultCodeFormatterConstants.ONE_LINE_IF_EMPTY;
	formatSource(
		"class Test {\n" +
		"	/**\n" +
		"	 * More Java doc comment\n" +
		"	 */\n" +
		"	// A line comment\n" +
		"	/* package */ void nothing() {}\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/570220 - [formatter] Bug for 'if' open parenthesis inside lambda body preceded by comment line
 */
public void testBug570220() {
	this.formatterPrefs.brace_position_for_block = DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP;
	formatSource(
		"class C {\n" +
		"	Runnable r = () -> {\n" +
		"		//\n" +
		"		if (true) {\n" +
		"		}\n" +
		"	};\n" +
		"}");
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
		"class C {\n" +
		"	void foo(Object o) {\n" +
		"		if ((o) instanceof String s)\n" +
		"			bar(s);\n" +
		"	}\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/576954 - [formatter] Switch expression formatting broken in a method chain with lambdas
 */
public void testBug576954() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.indent_switchstatements_compare_to_switch = true;
	formatSource(
		"public class C {\n" +
		"	void f() {\n" +
		"		Stream.of(1, 2)\n" +
		"				.map(it -> switch (it) {\n" +
		"					case 1 -> \"one\";\n" +
		"					case 2 -> \"two\";\n" +
		"					default -> \"many\";\n" +
		"				}).forEach(System.out::println);\n" +
		"	}\n" +
		"}");
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
		"class MyClass<A> extends AnotherClass {\n" +
		"}\n" +
		"\n" +
		"sealed interface Expr<A> permits MathExpr {\n" +
		"}");
}
/**
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/369
 */
public void testIssue369() {
	setComplianceLevel(CompilerOptions.VERSION_17);
	this.formatterPrefs.insert_space_after_opening_paren_in_record_declaration = true;
	this.formatterPrefs.insert_space_before_closing_paren_in_record_declaration = true;
	formatSource(
		"@JsonPropertyOrder({ \"position\", \"value\" })\n" +
		"public record ValueWithPosition( String position, String value ) {\n" +
		"}");
}
}
