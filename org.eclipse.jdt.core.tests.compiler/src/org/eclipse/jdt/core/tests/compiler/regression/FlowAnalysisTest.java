/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import junit.framework.Test;

public class FlowAnalysisTest extends AbstractRegressionTest {
	
public FlowAnalysisTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	this.runNegativeTest(new String[] {
		"X.java", // =================
		"public class X {\n" + 
		"	public String foo(int i) {\n" + 
		"		if (true) {\n" + 
		"			return null;\n" + 
		"		}\n" + 
		"		if (i > 0) {\n" + 
		"			return null;\n" + 
		"		}\n" + 
		"	}	\n" + 
		"}\n",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 2)\n" + 
	"	public String foo(int i) {\n" + 
	"	              ^^^^^^^^^^\n" + 
	"This method must return a result of type String\n" + 
	"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test002() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test() {\n" + 
			"        int c1, c2;\n" + 
			"        while ((char) (c1 = 0) == 1) {}\n" + 
			"        if (c1 == 0) {} // silent\n" + 
			"        if (c2 == 0) {} // complain\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	if (c2 == 0) {} // complain\n" + 
		"	    ^^\n" + 
		"The local variable c2 may not have been initialized\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test() {\n" + 
			"        int c1, c2;\n" + 
			"        while ((char) (c1 = 0) == 1) ;\n" + 
			"        if (c1 == 0) {} // silent\n" + 
			"        if (c2 == 0) {} // complain\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	if (c2 == 0) {} // complain\n" + 
		"	    ^^\n" + 
		"The local variable c2 may not have been initialized\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test004() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test() {\n" + 
			"        int c1, c2;\n" + 
			"        for (;(char) (c1 = 0) == 1;) ;\n" + 
			"        if (c1 == 0) {} // silent\n" + 
			"        if (c2 == 0) {} // complain\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	if (c2 == 0) {} // complain\n" + 
		"	    ^^\n" + 
		"The local variable c2 may not have been initialized\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test005() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test() {\n" + 
			"        int c1, c2;\n" + 
			"        do ; while ((char) (c1 = 0) == 1);\n" + 
			"        if (c1 == 0) {} // silent\n" + 
			"        if (c2 == 0) {} // complain\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	if (c2 == 0) {} // complain\n" + 
		"	    ^^\n" + 
		"The local variable c2 may not have been initialized\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// basic scenario
public void test006() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0); // silent because first case\n" + 
			"        case 1:\n" + 
			"            System.out.println(1); // complain: possible fall-through\n" + 
			"            break;\n" + 
			"        case 2:\n" + 
			"            System.out.println(3); // silent because of break\n" + 
			"            return;\n" + 
			"        case 3:                            // silent because of return\n" + 
			"        case 4:                            // silent because grouped cases\n" + 
			"        default:\n" + 
			"            System.out.println(\"default\"); //$NON-NLS-1$\n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	case 1:\n" + 
		"	^^^^^^\n" + 
		"Switch case may be entered by falling through previous case\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// SuppressWarnings effect - explicit fallthrough token
public void test007() {
	if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.WARNING);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    @SuppressWarnings(\"fallthrough\")\n" + 
				"    public void test(int p) {\n" + 
				"        switch (p) {\n" + 
				"        case 0:\n" + 
				"            System.out.println(0); // silent because first case\n" + 
				"        case 1:\n" + 
				"            System.out.println(1); // silent because of SuppressWarnings\n" + 
				"        }\n" + 
				"    }\n" +
				"	Zork z;\n" + // complain on Zork (unknown type)
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null, true, options);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// deep return (1) - fake reachable is seen as reachable
public void test008() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0);\n" +
			"            if (true) {\n" +
			"              return;\n" +
			"            }\n" + 
			"        case 1:\n" + 
			"            System.out.println(1);\n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	case 1:\n" + 
		"	^^^^^^\n" + 
		"Switch case may be entered by falling through previous case\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// deep return (2)
public void test009() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p, boolean b) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0);\n" +
			"            if (b) {\n" +
			"              return;\n" +
			"            }\n" + 
			"            else {\n" +
			"              return;\n" +
			"            }\n" + 
			"        case 1:\n" + 
			"            System.out.println(1);\n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// deep return (3), limit: cannot recognize that we won't return
public void test010() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p, boolean b) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.exit(0);\n" +
			"        case 1:\n" + // complain 
			"            System.out.println(1);\n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	case 1:\n" + 
		"	^^^^^^\n" + 
		"Switch case may be entered by falling through previous case\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// SuppressWarnings effect - implicit, using all token
public void test011() {
	if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.WARNING);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    @SuppressWarnings(\"all\")\n" + 
				"    public void test(int p) {\n" + 
				"        switch (p) {\n" + 
				"        case 0:\n" + 
				"            System.out.println(0); // silent because first case\n" + 
				"        case 1:\n" + 
				"            System.out.println(1); // silent because of SuppressWarnings\n" + 
				"        }\n" + 
				"    }\n" +
				"	Zork z;\n" + // complain on Zork (unknown type)
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null, true, options);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment
public void _test012() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0); // silent because first case\n" + 
			"            // on purpose fall-through\n" + 
			"        case 1:\n" + 
			"            System.out.println(1); // silent because of comment alone on its line above \n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment - default label
public void _test013() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0); // silent because first case\n" + 
			"            // on purpose fall-through\n" + 
			"        default:\n" + 
			"            System.out.println(1); // silent because of comment alone on its line above \n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// basic scenario: default label
public void test014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0); // silent because first case\n" +
						// note: the comment above is not alone on its line, hence it does not
						// protect against fall-through diagnostic
			"        default:\n" + 
			"            System.out.println(1); // complain: possible fall-through\n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	default:\n" + 
		"	^^^^^^^\n" + 
		"Switch case may be entered by falling through previous case\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// skip because of comment - variants
public void test015() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0); // silent because first case\n" + 
			"            // on purpose fall-through\n" +
			"\n" + // extraneous line breaks fall-through protection 
			"        case 1:\n" + 
			"            System.out.println(1); // silent because of comment alone on its line above \n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	case 1:\n" + 
		"	^^^^^^\n" + 
		"Switch case may be entered by falling through previous case\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// skip because of comment - variants
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0); // silent because first case\n" + 
			"            // on purpose fall-through\n" +
			"            /* other comment */\n" + // non-single line comment breaks fall-through protection 
			"        case 1:\n" + 
			"            System.out.println(1); // silent because of comment alone on its line above \n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	case 1:\n" + 
		"	^^^^^^\n" + 
		"Switch case may be entered by falling through previous case\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment - variants
public void _test017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0);\n" + 
			"// on purpose fall-through\n" + // very beginning of line
			"        case 1:\n" + 
			"            System.out.println(1); // silent because of comment alone on its line above \n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment - variants
public void _test018() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0);\n" + 
			"            //\n" + // empty line comment alone upon its line
			"        case 1:\n" + 
			"            System.out.println(1); // silent because of comment alone on its line above \n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// conditioned break
public void test019() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p, boolean b) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            if (b) {\n" +
			"              break;\n" +
			"            }\n" +
			"        case 1:\n" + 
			"            System.out.println(1); // silent because of comment alone on its line above \n" + 
			"        }\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	case 1:\n" + 
		"	^^^^^^\n" + 
		"Switch case may be entered by falling through previous case\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// default reporting is ignore
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0); // silent because first case\n" + 
			"        case 1:\n" + 
			"            System.out.println(1); // silent because default level is ignore\n" + 
			"        }\n" + 
			"    }\n" +
			"	Zork z;\n" + // complain on Zork (unknown type)
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// problem category
public void test021() {
	if (ProblemReporter.getProblemCategory(ProblemSeverities.Warning, IProblem.FallthroughCase) != 
			CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM) {
		fail("bad category for fall-through case problem");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128840
public void test022() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		if (true)\n" + 
			"            ;\n" + 
			"        else\n" + 
			"            ;\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	;\n" + 
		"	^\n" + 
		"Empty control-flow statement\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	;\n" + 
		"	^\n" + 
		"Empty control-flow statement\n" + 
		"----------\n",
		null, true, options);
}
public void test023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		final X x;\n" + 
			"		while (true) {\n" + 
			"			if (true) {\n" + 
			"				break;\n" + 
			"			}\n" + 
			"			x = new X();\n" + 
			"		}\n" + 
			"		x.foo();\n" + 
			"	}\n" + 
			"	public void foo() {\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	x.foo();\n" + 
		"	^\n" + 
		"The local variable x may not have been initialized\n" + 
		"----------\n");
}
public static Class testClass() {
	return FlowAnalysisTest.class;
}
}

