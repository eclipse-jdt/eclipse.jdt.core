/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;

import junit.framework.Test;

public class JavaSearchBugs12Tests extends AbstractJavaSearchTests {

	static {
//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"testBug542559_001"};
}

public JavaSearchBugs12Tests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchBugs12Tests.class, BYTECODE_DECLARATION_ORDER);
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
	JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "12");
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
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		search("switch_expr_field", FIELD, REFERENCES);
		assertSearchResults("src/X.java int X.twice(int) [switch_expr_field] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}
public void testBug542559_002() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
			"	int switch_expr_field = 10;\n" +
			"	int twice(int i) {\n" +
			"		int tw = switch (i) {\n" +
			"			case 0 -> switch_expr_field * 0;\n" +
			"			case 1 -> { break switch_expr_field; }\n" +
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
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		search("switch_expr_field", FIELD, REFERENCES);
		assertSearchResults(
				"src/X.java int X.twice(int) [switch_expr_field] EXACT_MATCH\n" +
				"src/X.java int X.twice(int) [switch_expr_field] EXACT_MATCH"
		);
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}
public void testBug542559_003() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
			"        int switch_expr_field = 10; \n" +
			"        int twice(int i) throws Exception {      \n" +
			"                int tw = switch (i) {   \n" +
			"                        case 0 -> { break switch_expr_field; }  \n" +
			"                        case 4 -> throw new MyException();  \n" +
			"                        default -> 3;           \n" +
			"                };      \n" +
			"                return tw;              \n" +
			"        }               \n" +
			"        public static void main(String[] args) {\n" +
			"                try {\n" +
			"                                       System.out.print(new X().twice(3));\n" +
			"                               } catch (Exception e) {\n" +
			"                                       // TODO Auto-generated catch block\n" +
			"                                       e.printStackTrace();\n" +
			"                               }\n" +
			"        }               \n" +
			"}\n" +
			"class MyException extends Exception {\n" +
			"       private static final long serialVersionUID = 3461899582505930474L;\n" +
			"       \n" +
			"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		IType type = this.workingCopies[0].getType("MyException");
		search(type, REFERENCES);
		assertSearchResults(
				"src/X.java int X.twice(int) [MyException] EXACT_MATCH"
		);
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
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
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
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
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
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
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
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
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
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		search("X2", CLASS, REFERENCES);
		assertSearchResults(
		"src/X2.java int X2.twice(int) [X2] EXACT_MATCH\n"+
		"src/X2.java void X2.main(String ...) [X2] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

/*
 * java search reference for class file reference in case and default blocks
 */
public void testBug542559_009() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
					"	Integer abcd\n" +
					"	public static int foo(int i) {\n" +
					"		final int k;\n" +
					"\n" +
					"		int it = switch (i) { \n" +
					"		case 1  ->   {\n" +
					"			k = 1;\n" +
					"			abcd.toString();\n" +
					"			break k ;\n" +
					"		}\n" +
					"		case 2  ->   {\n" +
					"			abcd.toString();\n" +
					"			break k ;\n" +
					"		}\n" +
					"		default -> {\n" +
					"			k = 3;\n" +
					"			abcd.toString();\n" +
					"			break k;\n" +
					"		}\n" +
					"		};\n" +
					"		return k;\n" +
					"	}\n" +
					"\n" +
					"	public boolean bar() {\n" +
					"		return true;\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(foo(3));\n" +
					"	}\n" +
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		search("abcd", FIELD, REFERENCES);
		assertSearchResults(
				"src/X.java int X.foo(int) [abcd] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [abcd] EXACT_MATCH\n" +
				"src/X.java int X.foo(int) [abcd] EXACT_MATCH"
		);
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

/*
 * java search reference for string constant in switch expression and switch statement
 */
public void testBug542559_0010() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
					"	static final String MONDAY = \"MONDAY\";\n" +
					"	static final String TUESDAY = \"TUESDAY\";\n" +
					"	static final String WEDNESDAY = \"WEDNESDAY\";\n" +
					"	static final String THURSDAY = \"THURSDAY\";\n" +
					"	static final String FRIDAY = \"FRIDAY\";\n" +
					"	static final String SATURDAY = \"SATURDAY\";\n" +
					"	static final String SUNDAY = \"SUNDAY\"; \n" +
					"	@SuppressWarnings(\"preview\")\n" +
					"	public static void main(String[] args) {\n" +
					"		String day = \"MONDAY\";\n" +
					"		switch (day) {\n" +
					"		    case MONDAY, FRIDAY, SUNDAY  -> System.out.println(6);\n" +
					"		    case TUESDAY           		 -> System.out.println(7);\n" +
					"		    case THURSDAY, SATURDAY     -> System.out.println(8);\n" +
					"		    case WEDNESDAY              -> System.out.println(9);\n" +
					"		}\n" +
					"		int k = switch (day) {\n" +
					"	    case SATURDAY  -> throw new NullPointerException();\n" +
					"	    case TUESDAY -> 1;\n" +
					"	    case WEDNESDAY -> {break 10;}\n" +
					"	    default      -> {\n" +
					"	        int g = day.h();\n" +
					"	        int result = f(g);\n" +
					"	        break result;\n" +
					"	    }};\n" +
					"	}\n" +
					"	static int f(int k) {\n" +
					"		return k*k;\n" +
					"	}\n" +
					"}\n"
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		search("SATURDAY", FIELD, REFERENCES);
		assertSearchResults(
				"src/X.java void X.main(String[]) [SATURDAY] EXACT_MATCH\n" +
				"src/X.java void X.main(String[]) [SATURDAY] EXACT_MATCH"
		);
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}
/*
 * java search a javatype reference from another java project if that is in switch expression
 */
public void testBug542559_0011() throws CoreException {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs12", new String[] {"src"}, new String[] {"JCL12_LIB"}, "bin", "12");
		project1.open(null);
		createFolder("/JavaSearchBugs12/src/pack1");
		createFile("/JavaSearchBugs12/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 { \n" +
				"	static final String MONDAY = \"MONDAY\";\n" +
				"	static final String TUESDAY = \"TUESDAY\";\n" +
				"	static final String WEDNESDAY = \"WEDNESDAY\";\n" +
				"	static final String THURSDAY = \"THURSDAY\";\n" +
				"	static final String FRIDAY = \"FRIDAY\";\n" +
				"	static final String SATURDAY = \"SATURDAY\";\n" +
				"	static final String SUNDAY = \"SUNDAY\"; \n" +
				"	@SuppressWarnings(\"preview\")\n" +
				"	public static void main(String[] args) {\n" +
				"		String day = \"MONDAY\";\n" +
				"		switch (day) {\n" +
				"		    case MONDAY, FRIDAY, SUNDAY  -> System.out.println(6);\n" +
				"		    case TUESDAY           		 -> System.out.println(7);\n" +
				"		    case THURSDAY, SATURDAY     -> System.out.println(8);\n" +
				"		    case WEDNESDAY              -> System.out.println(9);\n" +
				"		}\n" +
				"		int k = switch (day) {\n" +
				"	    case SATURDAY  -> throw new NullPointerException();\n" +
				"	    case TUESDAY -> 1;\n" +
				"	    case WEDNESDAY -> {break 10;}\n" +
				"	    default      -> {\n" +
				"	        pack2.X22.a2=99;\n" +
				"	        int result = f(g);\n" +
				"	        break result;\n" +
				"	    }};\n" +
				"	}\n" +
				"	static int f(int k) {\n" +
				"		return k*k;\n" +
				"	}\n" +
				 "}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"},new String[] {"JCL12_LIB"}, "bin", "12");
		project2.open(null);
		createFolder("/second/src/pack2");
		createFile("/second/src/pack2/X22.java",
				"package pack2;\n" +
				"public class X22 {public static int a2=9;}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		IPackageFragment pkg = getPackageFragment("second", "src", "pack2");

		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs12")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		String exp = "src/pack1/X11.java void pack1.X11.main(String[]) [pack2] EXACT_MATCH\n"
		+"src/pack2 pack2 EXACT_MATCH";
		assertSearchResults(
			exp,
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs12");
		deleteProject("second");
	}

}
/*
 * java search a method reference in switch expression
 */
public void testBug542559_0012() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"import java.util.function.Supplier;\n" +
			"interface I0 { void i(); }\n" +
			"interface I1 extends I0 {}\n" +
			"interface I2 extends I0 {}\n" +
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
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		search("n1", METHOD, REFERENCES);
		assertSearchResults("src/X.java void X.test(int, boolean) [n1] EXACT_MATCH");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}

}