/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;

/**
 * Moved from JavaSearchBugs13Tests
 * All preview option disabled
 * @author vikchand
 */
public class JavaSearchBugs14SwitchExpressionTests extends AbstractJavaSearchTests {

	static {
//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"testBug542559_001"};
}

public JavaSearchBugs14SwitchExpressionTests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchBugs14SwitchExpressionTests.class, BYTECODE_DECLARATION_ORDER);
}
class TestCollector extends JavaSearchResultCollector {
	public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
		super.acceptSearchMatch(searchMatch);
	}
}
class ReferenceCollector extends JavaSearchResultCollector {
	protected void writeLine() throws CoreException {
		super.writeLine();
		ReferenceMatch refMatch = (ReferenceMatch) this.match;
		IJavaElement localElement = refMatch.getLocalElement();
		if (localElement != null) {
			this.line.append("+[");
			if (localElement.getElementType() == IJavaElement.ANNOTATION) {
				this.line.append('@');
				this.line.append(localElement.getElementName());
				this.line.append(" on ");
				this.line.append(localElement.getParent().getElementName());
			} else {
				this.line.append(localElement.getElementName());
			}
			this.line.append(']');
		}
	}

}
class TypeReferenceCollector extends ReferenceCollector {
	protected void writeLine() throws CoreException {
		super.writeLine();
		TypeReferenceMatch typeRefMatch = (TypeReferenceMatch) this.match;
		IJavaElement[] others = typeRefMatch.getOtherElements();
		int length = others==null ? 0 : others.length;
		if (length > 0) {
			this.line.append("+[");
			for (int i=0; i<length; i++) {
				IJavaElement other = others[i];
				if (i>0) this.line.append(',');
				if (other.getElementType() == IJavaElement.ANNOTATION) {
					this.line.append('@');
					this.line.append(other.getElementName());
					this.line.append(" on ");
					this.line.append(other.getParent().getElementName());
				} else {
					this.line.append(other.getElementName());
				}
			}
			this.line.append(']');
		}
	}
}

IJavaSearchScope getJavaSearchScope() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
}
IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
	if (packageName == null) return getJavaSearchScope();
	return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
}
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	if (this.wcOwner == null) {
		this.wcOwner = new WorkingCopyOwner() {};
	}
	return getWorkingCopy(path, source, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "14");
}
public void tearDownSuite() throws Exception {
	deleteProject("JavaSearchBugs");
	super.tearDownSuite();
}
protected void setUp () throws Exception {
	super.setUp();
	this.resultCollector = new TestCollector();
	this.resultCollector.showAccuracy(true);
}

//copy from JavaSearchBugs12Tests starts ( after deleting break with value tests since that is discarded)

public void testBug542559_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
			"	int switch_expr_field = 10;\n" +
			"	int twice(int i) {\n" +
			"		int tw = switch (i) {\n" +
			"			case 0 -> switch_expr_field * 0;\n" +
			"			case 1 -> 2;\n" +
			"			default -> 3;\n" +
			"		};\n" +
			"		return tw;\n" +
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		System.out.print(new X().twice(3));\n" +
			"	}\n" +
			"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("switch_expr_field", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.twice(int) [switch_expr_field] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


/*
 * java search reference for an integer in default block of switch expression
 */
public void testBug542559_004() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
			"	int switch_expr_field = 10;\n" +
			"	int twice(int i) {\n" +
			"		int tw = switch (i) {\n" +
			"			case 0 -> switch_expr_field * 0;\n" +
			"			case 1 -> 2;\n" +
			"			default ->{ \n" +
			"			switch_expr_field*9; \n" +
			"		}};\n" +
			"		return tw;\n" +
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		System.out.print(new X().twice(3));\n" +
			"	}\n" +
			"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("switch_expr_field", FIELD, REFERENCES);
		assertSearchResults(
				"src/X.java int X.twice(int) [switch_expr_field] EXACT_MATCH\n" +
				"src/X.java int X.twice(int) [switch_expr_field] POTENTIAL_MATCH" // altered recovery from syntax error results in potential match
		);
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

/*
 * java search reference for an integer in default block of switch expression
 */
public void testBug542559_004_1() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
			"	int switch_expr_field = 10;\n" +
			"	int twice(int i) {\n" +
			"		int tw = switch (i) {\n" +
			"			case 0 -> switch_expr_field * 0;\n" +
			"			case 1 -> 2;\n" +
			"			default ->{ \n" +
			"			yield switch_expr_field*9; \n" +
			"		}};\n" +
			"		return tw;\n" +
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		System.out.print(new X().twice(3));\n" +
			"	}\n" +
			"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("switch_expr_field", FIELD, REFERENCES);
		assertSearchResults(
				"src/X.java int X.twice(int) [switch_expr_field] EXACT_MATCH\n" +
				"src/X.java int X.twice(int) [switch_expr_field] EXACT_MATCH"
		);
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

/*
 * java search reference for an integer in default of switch expression
 */
public void testBug542559_005() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
			"	int switch_expr_field = 10;\n" +
			"	int twice(int i) {\n" +
			"		int tw = switch (i) {\n" +
			"			case 0 -> switch_expr_field * 0;\n" +
			"			case 1 -> 2;\n" +
			"			default -> switch_expr_field*9;\n" +
			"		};\n" +
			"		return tw;\n" +
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		System.out.print(new X().twice(3));\n" +
			"	}\n" +
			"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("switch_expr_field", FIELD, REFERENCES);
		assertSearchResults(
				"src/X.java int X.twice(int) [switch_expr_field] EXACT_MATCH\n" +
				"src/X.java int X.twice(int) [switch_expr_field] EXACT_MATCH"
		);
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

/*
 * java search reference for simple multi constant case statement for enum
 */
public void testBug542559_006() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
					"enum Day { SATURDAY, SUNDAY, MONDAY;}\n" +
					"public static void bar(Day day) {\n" +
					"		switch (day) {\n" +
					"		case SATURDAY, SUNDAY: \n" +
					"			System.out.println(Day.SUNDAY);\n" +
					"			break;\n" +
					"		case MONDAY : System.out.println(Day.MONDAY);\n" +
					"					break;\n" +
					"		}\n" +
					"	}" +
					"	public static void main(String[] args) {\n" +
					"		bar(Day.SATURDAY);\n" +
					"	}\n"
					+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("SATURDAY", FIELD, REFERENCES);
		assertSearchResults(
				"src/X.java void X.bar(Day) [SATURDAY] EXACT_MATCH\n"+
				"src/X.java void X.main(String[]) [SATURDAY] EXACT_MATCH"
		);
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}
/*
 * java search reference for simple multi constant case statement for enum, 2nd case
 */
public void testBug542559_007() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
					"enum Day { SATURDAY, SUNDAY, MONDAY;}\n" +
					"public static void bar(Day day) {\n" +
					"		switch (day) {\n" +
					"		case SATURDAY, SUNDAY: \n" +
					"			System.out.println(Day.SUNDAY);\n" +
					"			break;\n" +
					"		case MONDAY : System.out.println(Day.MONDAY);\n" +
					"					break;\n" +
					"		}\n" +
					"	}" +
					"	public static void main(String[] args) {\n" +
					"		bar(Day.SATURDAY);\n" +
					"	}\n"
					+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("SUNDAY", FIELD, REFERENCES);
		assertSearchResults(
				"src/X.java void X.bar(Day) [SUNDAY] EXACT_MATCH\n"+
				"src/X.java void X.bar(Day) [SUNDAY] EXACT_MATCH"
		);
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

/*
 * java search reference for class file reference in switch expression
 */
public void testBug542559_008() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X2.java",
			"public class X2 {\n" +
			"   String s = new String();        \n" +
			"	int switch_expr_field = 10;\n" +
			"	int twice(int i) {\n" +
			"		int tw = switch (i) {\n" +
			"			case 0 -> switch_expr_field * 0;\n" +
			"			case 1 -> 2;\n" +
			"			default -> new X2().toString().length();\n" +
			"		};\n" +
			"		return tw;\n" +
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		System.out.print(new X2().twice(3));\n" +
			"	}\n" +
			"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("X2", CLASS, REFERENCES);
		assertSearchResults(
		"src/X2.java int X2.twice(int) [X2] EXACT_MATCH\n"+
		"src/X2.java void X2.main(String ...) [X2] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}



/*
 * java search a method reference in switch expression
 */
public void testBug542559_0012() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"interface I0 { void i(); }\n" +
			"interface I1 extends I0 {}\n" +
			"interface I2 extends I0 {}\n" +
			"public interface Supplier<T> {\n" +
			"    T get();\n" +
			"}\n" +
			"public class X {\n" +
			"	I1 n1() { return null; }\n" +
			"	<I extends I2> I n2() { return null; }\n" +
			"	<M> M m(Supplier<M> m) { return m.get(); }\n" +
			"	void test(int i, boolean b) {\n" +
			"		m(switch (i) {\n" +
			"			case 1 -> this::n1;\n" +
			"			default -> this::n2;\n" +
			"		}).i(); \n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			new X().test(1, true);\n" +
			"		} catch (NullPointerException e) {\n" +
			"			System.out.println(\"NPE as expected\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject();//assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("n1", METHOD, REFERENCES);
		assertSearchResults("src/X.java void X.test(int, boolean) [n1] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

// copy from JavaSearchBugs12Tests ends

public void testBug549413_001() throws CoreException {
	// field reference in yield
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
			"int switch_expr_field = 10; \n" +
			"public static int foo(int val) {\n" +
			"int k = switch (val) {\n" +
			"case 1 -> { yield switch_expr_field; }\n" +
			"default -> { yield 2; }\n" +
			"};\n" +
			"return k;\n" +
			"}\n" +
			"	public static void main(String... args) {\n" +
			"		System.out.println(X.foo(2));\n" +
			"	}\n" +
			"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("switch_expr_field", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [switch_expr_field] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

public void testBug549413_002() throws CoreException {
	//field all occurrences in yield
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
			"int switch_expr_field = 10; \n" +
			"public static int foo(int val) {\n" +
			"int k = switch (val) {\n" +
			"case 1 -> { yield switch_expr_field; }\n" +
			"default -> { yield 2; }\n" +
			"};\n" +
			"return k;\n" +
			"}\n" +
			"	public static void main(String... args) {\n" +
			"		System.out.println(X.foo(2));\n" +
			"	}\n" +
			"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("switch_expr_field", FIELD, ALL_OCCURRENCES);
		assertSearchResults("src/X.java X.switch_expr_field [switch_expr_field] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [switch_expr_field] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_003() throws CoreException {
	//METHOD named yield -  all occurrences in yield
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"\n"+
					"	public static int yield() {\n"+
					"		return 1;\n"+
					"	}\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public static int foo(int val) {\n"+
					"		int k = switch (val) {\n"+
					"		case 1 -> { yield 1; }\n"+
					"		default -> { yield 2; }\n"+
					"		};\n"+
					"		return k;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(1));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", METHOD, ALL_OCCURRENCES);
		assertSearchResults("src/X.java int X.yield() [yield] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

public void testBug549413_004() throws CoreException {
	//METHOD yield - references in yield
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"\n"+
					"	public static int yield() {\n"+
					"		return 1;\n"+
					"	}\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public static int foo(int val) {\n"+
					"		int k = switch (val) {\n"+
					"		case 1 -> { yield X.yield(); }\n"+
					"		default -> { yield 2; }\n"+
					"		};\n"+
					"		return k;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(1));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", METHOD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [yield()] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_005() throws CoreException {
	//field yield - references in yield
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"	public static int yield;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public static int foo(int val) {\n"+
					"		int k = switch (val) {\n"+
					"		case 1 -> { yield yield; }\n"+
					"		default -> { yield 2; }\n"+
					"		};\n"+
					"		return k;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(1));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [yield] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

public void testBug549413_006() throws CoreException {
	//field yield - all occurrence in yield
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"	public static int yield;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public static int foo(int val) {\n"+
					"		int k = switch (val) {\n"+
					"		case 1 -> { yield yield; }\n"+
					"		default -> { yield 2; }\n"+
					"		};\n"+
					"		return k;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(1));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, ALL_OCCURRENCES);
		assertSearchResults("src/X.java X.yield [yield] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [yield] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

public void testBug549413_007() throws CoreException {
	//field yield - all reference of identifier in yield statement
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"	public static int yield;\n"+
					"	public static int abc;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public static int foo(int val) {\n"+
					"		int k = switch (val) {\n"+
					"		case 1 -> { abc=0;yield yield; }\n"+
					"		default -> { yield 2; }\n"+
					"		};\n"+
					"		return k;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(1));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("abc", FIELD, ALL_OCCURRENCES);
		assertSearchResults("src/X.java X.abc [abc] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [abc] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

public void testBug549413_008() throws CoreException {
	//field yield - all reference of identifier in yield statement ( yield -1 without braces)
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"	static int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int r = switch(i) {\n"+
					"			default -> yield - 1;\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public  int yield() {\n"+
					"		return 0;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [yield] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_009() throws CoreException {
	//field yield - all reference of identifier in yield statement ( yield -1 with braces)
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"	int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int r = switch(i) {\n"+
					"			default ->{ yield - 1;}\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public  int yield() {\n"+
					"		return 0;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_010() throws CoreException {
	//method - break label
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"\n"+
					"	static int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"	int r = switch(i) {\n"+
					"			default -> X.yield();\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public static  int yield() {\n"+
					"		yield: while (X.yield == 100) {\n"+
					"			yield = 256;\n"+
					"			break yield;\n"+
					"		}\n"+
					"		return yield;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", METHOD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [yield()] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

public void testBug549413_011() throws CoreException {
	//break label in combination with yield field
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"\n"+
					"	static int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"	int r = switch(i) {\n"+
					"			default -> X.yield();\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public static  int yield() {\n"+
					"		yield: while (X.yield == 100) {\n"+
					"			yield = 256;\n"+
					"			break yield;\n"+
					"		}\n"+
					"		return yield;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.yield() [yield] EXACT_MATCH\n" +
				"src/X.java int X.yield() [yield] EXACT_MATCH\n" +
				"src/X.java int X.yield() [yield] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_012() throws CoreException {
	//field yield - multiple yield references
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"\n"+
					"	static	int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int r = switch(i) {\n"+
					"			default -> {yield yield + yield + yield * yield;}\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [yield] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [yield] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [yield] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [yield] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_013() throws CoreException {
	//field yield -another yield field test
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"\n"+
					"	static	int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int r = switch(i) {\n"+
					"			default ->0 + yield + 10;\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [yield] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_014() throws CoreException {
	//field yield - ternary operator
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"	int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public   int foo(int i) {\n"+
					"		int r = switch(i) {\n"+
					"			 case 0 : yield 100;\n"+
					"			 case 1 : yield yield;\n"+
					"			 default: yield 0;\n"+
					"		};\n"+
					"		return r > 100 ? yield + 1 : yield + 200;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [yield] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [yield] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [yield] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_015() throws CoreException {
	//field yield - another test case
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"	int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public   int foo(int i) {\n"+
					"		int r = switch(i) {\n"+
					"			 case 0 : yield 100;\n"+
					"			 case 1 : yield yield;\n"+
					"			 default: yield 0;\n"+
					"		};\n"+
					"		return r > 100 ? yield() + 1 : yield + 200;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"	}\n"+
					"	public static int yield() {\n"+
					"		return 1;\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [yield] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [yield] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_016() throws CoreException {
	//method yield -method references
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n"+
					"	int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public   int foo(int i) {\n"+
					"		int r = switch(i) {\n"+
					"			 case 0 : yield 100;\n"+
					"			 case 1 : yield yield;\n"+
					"			 default: yield 0;\n"+
					"		};\n"+
					"		return r > 100 ? yield() + 1 : yield + 200;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"	}\n"+
					"	public static int yield() {\n"+
					"		return 1;\n"+
					"	}\n"+
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", METHOD, REFERENCES);
		assertSearchResults("src/X.java int X.foo(int) [yield()] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

public void testBug549413_017() throws CoreException {
	//select local variable and find the declaration
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public class X {\n"+
					"\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int yield = 100;\n"+
					"		int r = switch(i) {\n"+
					"			default -> {yield /* here*/ yield + yield + yield * yield;}\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
	);

	String str = this.workingCopies[0].getSource();
	String selection = "/* here*/ yield";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	ILocalVariable local = (ILocalVariable) elements[0];
	search(local, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"src/X.java int X.foo(int).yield [yield] EXACT_MATCH");
}


public void testBug549413_018() throws CoreException {
	//select local variable and find the declaration
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public class X {\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int localVar = 100;\n"+
					"		int r = switch(i) {\n"+
					"			default -> {yield /* here*/ localVar + localVar + localVar * localVar;}\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
	);

	String str = this.workingCopies[0].getSource();
	String selection = "/* here*/ localVar";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	ILocalVariable local = (ILocalVariable) elements[0];
	search(local, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"src/X.java int X.foo(int).localVar [localVar] EXACT_MATCH");
}
// add non-preview stuff involving yield field and method

public void testBug549413_019() throws CoreException {
	//old style switch case without preview search for yield field.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"public class X {\n"+
		 " static int  yield;\n"+
		 " public static int   yield() { \n"+
		 "	  return 7; \n"+
		 " } \n"+
		 "  public static void main(String[] args) { \n"+
		 "     int week = 1;	\n"+
		 "    switch (week) { \n"+
		 "      case 1:       \n"+
		 "     	 yield = 88; \n"+
		 "    	 break; \n"+
		 "     case 2:  \n"+
		 "   	 yield = yield();\n"+
		 "   	 break; \n"+
		 "    default:  \n"+
		 "  	 yield = 88; \n"+
		 "     break; \n" +
		 " } \n" +
		 " System.out.println(yield); \n"+
		 "	}\n"+
		 "}\n"
);


	try {
		search("yield", FIELD, REFERENCES);
		assertSearchResults("src/X.java void X.main(String[]) [yield] EXACT_MATCH\n" +
				"src/X.java void X.main(String[]) [yield] EXACT_MATCH\n" +
				"src/X.java void X.main(String[]) [yield] EXACT_MATCH\n" +
				"src/X.java void X.main(String[]) [yield] EXACT_MATCH");
	} finally {
	}
}

public void testBug549413_020() throws CoreException {
	//old style switch case without preview search for yield method.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"public class X {\n"+
		 " static int  yield;\n"+
		 " public static int   yield() { \n"+
		 "	  return 7; \n"+
		 " } \n"+
		 "  public static void main(String[] args) { \n"+
		 "     int week = 1;	\n"+
		 "    switch (week) { \n"+
		 "      case 1:       \n"+
		 "     	 yield = 88; \n"+
		 "    	 break; \n"+
		 "     case 2:  \n"+
		 "   	 yield = yield();\n"+
		 "   	 break; \n"+
		 "    default:  \n"+
		 "  	 yield = 88; \n"+
		 "     break; \n" +
		 " } \n" +
		 " System.out.println(yield); \n"+
		 "	}\n"+
		 "}\n"
);


	try {
		search("yield", METHOD, REFERENCES);
		assertSearchResults("src/X.java void X.main(String[]) [yield()] EXACT_MATCH");
	} finally {
	}
}

}
