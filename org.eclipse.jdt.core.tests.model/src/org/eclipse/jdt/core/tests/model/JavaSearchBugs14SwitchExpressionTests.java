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

import junit.framework.Test;

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
			"""
				public class X {
					int switch_expr_field = 10;
					int twice(int i) {
						int tw = switch (i) {
							case 0 -> switch_expr_field * 0;
							case 1 -> 2;
							default -> 3;
						};
						return tw;
					}
					public static void main(String... args) {
						System.out.print(new X().twice(3));
					}
				}
				"""
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
			"""
				public class X {
					int switch_expr_field = 10;
					int twice(int i) {
						int tw = switch (i) {
							case 0 -> switch_expr_field * 0;
							case 1 -> 2;
							default ->{\s
							switch_expr_field*9;\s
						}};
						return tw;
					}
					public static void main(String... args) {
						System.out.print(new X().twice(3));
					}
				}
				"""
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
			"""
				public class X {
					int switch_expr_field = 10;
					int twice(int i) {
						int tw = switch (i) {
							case 0 -> switch_expr_field * 0;
							case 1 -> 2;
							default -> switch_expr_field*9;
						};
						return tw;
					}
					public static void main(String... args) {
						System.out.print(new X().twice(3));
					}
				}
				"""
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
			"""
				public class X {
				enum Day { SATURDAY, SUNDAY, MONDAY;}
				public static void bar(Day day) {
						switch (day) {
						case SATURDAY, SUNDAY:\s
							System.out.println(Day.SUNDAY);
							break;
						case MONDAY : System.out.println(Day.MONDAY);
									break;
						}
					}\
					public static void main(String[] args) {
						bar(Day.SATURDAY);
					}
				}
				"""
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
			"""
				public class X {
				enum Day { SATURDAY, SUNDAY, MONDAY;}
				public static void bar(Day day) {
						switch (day) {
						case SATURDAY, SUNDAY:\s
							System.out.println(Day.SUNDAY);
							break;
						case MONDAY : System.out.println(Day.MONDAY);
									break;
						}
					}\
					public static void main(String[] args) {
						bar(Day.SATURDAY);
					}
				}
				"""
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
			"""
				public class X2 {
				   String s = new String();       \s
					int switch_expr_field = 10;
					int twice(int i) {
						int tw = switch (i) {
							case 0 -> switch_expr_field * 0;
							case 1 -> 2;
							default -> new X2().toString().length();
						};
						return tw;
					}
					public static void main(String... args) {
						System.out.print(new X2().twice(3));
					}
				}
				"""
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
			"""
				import java.util.function.Supplier;
				interface I0 { void i(); }
				interface I1 extends I0 {}
				interface I2 extends I0 {}
				public class X {
					I1 n1() { return null; }
					<I extends I2> I n2() { return null; }
					<M> M m(Supplier<M> m) { return m.get(); }
					void test(int i, boolean b) {
						m(switch (i) {
							case 1 -> this::n1;
							default -> this::n2;
						}).i();\s
					}
					public static void main(String[] args) {
						try {
							new X().test(1, true);
						} catch (NullPointerException e) {
							System.out.println("NPE as expected");
						}
					}
				}
				"""
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
			"""
				public class X {
				int switch_expr_field = 10;\s
				public static int foo(int val) {
				int k = switch (val) {
				case 1 -> { yield switch_expr_field; }
				default -> { yield 2; }
				};
				return k;
				}
					public static void main(String... args) {
						System.out.println(X.foo(2));
					}
				}
				"""
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
			"""
				public class X {
				int switch_expr_field = 10;\s
				public static int foo(int val) {
				int k = switch (val) {
				case 1 -> { yield switch_expr_field; }
				default -> { yield 2; }
				};
				return k;
				}
					public static void main(String... args) {
						System.out.println(X.foo(2));
					}
				}
				"""
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
			"""
				public class X {
				
					public static int yield() {
						return 1;
					}
					@SuppressWarnings("preview")
					public static int foo(int val) {
						int k = switch (val) {
						case 1 -> { yield 1; }
						default -> { yield 2; }
						};
						return k;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(1));
					}
				}
				"""
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
			"""
				public class X {
				
					public static int yield() {
						return 1;
					}
					@SuppressWarnings("preview")
					public static int foo(int val) {
						int k = switch (val) {
						case 1 -> { yield X.yield(); }
						default -> { yield 2; }
						};
						return k;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(1));
					}
				}
				"""
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
			"""
				public class X {
					public static int yield;
					@SuppressWarnings("preview")
					public static int foo(int val) {
						int k = switch (val) {
						case 1 -> { yield yield; }
						default -> { yield 2; }
						};
						return k;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(1));
					}
				}
				"""
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
			"""
				public class X {
					public static int yield;
					@SuppressWarnings("preview")
					public static int foo(int val) {
						int k = switch (val) {
						case 1 -> { yield yield; }
						default -> { yield 2; }
						};
						return k;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(1));
					}
				}
				"""
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
			"""
				public class X {
					public static int yield;
					public static int abc;
					@SuppressWarnings("preview")
					public static int foo(int val) {
						int k = switch (val) {
						case 1 -> { abc=0;yield yield; }
						default -> { yield 2; }
						};
						return k;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(1));
					}
				}
				"""
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
			"""
				public class X {
					static int yield = 100;
					@SuppressWarnings("preview")
					public  static int foo(int i) {
						int r = switch(i) {
							default -> yield - 1;
						};
						return r;
					}
					public  int yield() {
						return 0;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(0));
					}
				}
				"""
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
			"""
				public class X {
					int yield = 100;
					@SuppressWarnings("preview")
					public  static int foo(int i) {
						int r = switch(i) {
							default ->{ yield - 1;}
						};
						return r;
					}
					public  int yield() {
						return 0;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(0));
					}
				}
				"""
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
			"""
				public class X {
				
					static int yield = 100;
					@SuppressWarnings("preview")
					public  static int foo(int i) {
					int r = switch(i) {
							default -> X.yield();
						};
						return r;
					}
					public static  int yield() {
						yield: while (X.yield == 100) {
							yield = 256;
							break yield;
						}
						return yield;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(0));
					}
				}
				"""
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
			"""
				public class X {
				
					static int yield = 100;
					@SuppressWarnings("preview")
					public  static int foo(int i) {
					int r = switch(i) {
							default -> X.yield();
						};
						return r;
					}
					public static  int yield() {
						yield: while (X.yield == 100) {
							yield = 256;
							break yield;
						}
						return yield;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(0));
					}
				}
				"""
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("""
			src/X.java int X.yield() [yield] EXACT_MATCH
			src/X.java int X.yield() [yield] EXACT_MATCH
			src/X.java int X.yield() [yield] EXACT_MATCH""");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_012() throws CoreException {
	//field yield - multiple yield references
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				public class X {
				
					static	int yield = 100;
					@SuppressWarnings("preview")
					public  static int foo(int i) {
						int r = switch(i) {
							default -> {yield yield + yield + yield * yield;}
						};
						return r;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(0));
					}
				}
				"""
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("""
			src/X.java int X.foo(int) [yield] EXACT_MATCH
			src/X.java int X.foo(int) [yield] EXACT_MATCH
			src/X.java int X.foo(int) [yield] EXACT_MATCH
			src/X.java int X.foo(int) [yield] EXACT_MATCH""");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_013() throws CoreException {
	//field yield -another yield field test
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				public class X {
				
					static	int yield = 100;
					@SuppressWarnings("preview")
					public  static int foo(int i) {
						int r = switch(i) {
							default ->0 + yield + 10;
						};
						return r;
					}
					public static void main(String[] args) {
						System.out.println(X.foo(0));
					}
				}
				"""
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
			"""
				public class X {
					int yield = 100;
					@SuppressWarnings("preview")
					public   int foo(int i) {
						int r = switch(i) {
							 case 0 : yield 100;
							 case 1 : yield yield;
							 default: yield 0;
						};
						return r > 100 ? yield + 1 : yield + 200;
					}
					public static void main(String[] args) {
					}
				}
				"""
			);
	IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
	String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		search("yield", FIELD, REFERENCES);
		assertSearchResults("""
			src/X.java int X.foo(int) [yield] EXACT_MATCH
			src/X.java int X.foo(int) [yield] EXACT_MATCH
			src/X.java int X.foo(int) [yield] EXACT_MATCH""");
	} finally {
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
	}
}


public void testBug549413_015() throws CoreException {
	//field yield - another test case
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				public class X {
					int yield = 100;
					@SuppressWarnings("preview")
					public   int foo(int i) {
						int r = switch(i) {
							 case 0 : yield 100;
							 case 1 : yield yield;
							 default: yield 0;
						};
						return r > 100 ? yield() + 1 : yield + 200;
					}
					public static void main(String[] args) {
					}
					public static int yield() {
						return 1;
					}
				}
				"""
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
			"""
				public class X {
					int yield = 100;
					@SuppressWarnings("preview")
					public   int foo(int i) {
						int r = switch(i) {
							 case 0 : yield 100;
							 case 1 : yield yield;
							 default: yield 0;
						};
						return r > 100 ? yield() + 1 : yield + 200;
					}
					public static void main(String[] args) {
					}
					public static int yield() {
						return 1;
					}
				}
				"""
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
					"""
						public class X {
						
							@SuppressWarnings("preview")
							public  static int foo(int i) {
								int yield = 100;
								int r = switch(i) {
									default -> {yield /* here*/ yield + yield + yield * yield;}
								};
								return r;
							}
							public static void main(String[] args) {
								System.out.println(X.foo(0));
							}
						}
						"""
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
					"""
						public class X {
							@SuppressWarnings("preview")
							public  static int foo(int i) {
								int localVar = 100;
								int r = switch(i) {
									default -> {yield /* here*/ localVar + localVar + localVar * localVar;}
								};
								return r;
							}
							public static void main(String[] args) {
								System.out.println(X.foo(0));
							}
						}
						"""
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
		"""
			public class X {
			 static int  yield;
			 public static int   yield() {\s
				  return 7;\s
			 }\s
			  public static void main(String[] args) {\s
			     int week = 1;\t
			    switch (week) {\s
			      case 1:      \s
			     	 yield = 88;\s
			    	 break;\s
			     case 2: \s
			   	 yield = yield();
			   	 break;\s
			    default: \s
			  	 yield = 88;\s
			     break;\s
			 }\s
			 System.out.println(yield);\s
				}
			}
			"""
);


	try {
		search("yield", FIELD, REFERENCES);
		assertSearchResults("""
			src/X.java void X.main(String[]) [yield] EXACT_MATCH
			src/X.java void X.main(String[]) [yield] EXACT_MATCH
			src/X.java void X.main(String[]) [yield] EXACT_MATCH
			src/X.java void X.main(String[]) [yield] EXACT_MATCH""");
	} finally {
	}
}

public void testBug549413_020() throws CoreException {
	//old style switch case without preview search for yield method.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			public class X {
			 static int  yield;
			 public static int   yield() {\s
				  return 7;\s
			 }\s
			  public static void main(String[] args) {\s
			     int week = 1;\t
			    switch (week) {\s
			      case 1:      \s
			     	 yield = 88;\s
			    	 break;\s
			     case 2: \s
			   	 yield = yield();
			   	 break;\s
			    default: \s
			  	 yield = 88;\s
			     break;\s
			 }\s
			 System.out.println(yield);\s
				}
			}
			"""
);


	try {
		search("yield", METHOD, REFERENCES);
		assertSearchResults("src/X.java void X.main(String[]) [yield()] EXACT_MATCH");
	} finally {
	}
}

}
