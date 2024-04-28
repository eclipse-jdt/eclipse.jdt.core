/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class CompletionTestsForRecordPattern extends AbstractJavaModelCompletionTests {
	private static int UNQUALIFIED_REL = R_DEFAULT + R_RESOLVED + R_CASE + R_INTERESTING + R_UNQUALIFIED
			+ R_NON_RESTRICTED;


	static {
		 //TESTS_NAMES = new String[]{"testGH2299_SwitchStatement"};
	}

	public CompletionTestsForRecordPattern(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null) {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "21");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "21");
		}
		super.setUpSuite();
		COMPLETION_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTestsForRecordPattern.class);
	}
	//content assist just record pattern usage
	public void test001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr)  -> {\n"
				+ "        		yield 1;  \n"
				+ "        } \n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "    fals \n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "fals";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("false[KEYWORD]{false, null, null, false, null, 52}",
				requestor.getResults());

	}
	//content assist in record pattern switch case just after case
	public void test002() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case /*here*/ Rectangl (ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr)  -> {\n"
				+ "        		yield 1;  \n"
				+ "        } \n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/ Rectangl";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("Rectangle[TYPE_REF]{Rectangle, , LRectangle;, null, null, 82}",
				requestor.getResults());

	}
	//content assist in record pattern switch case - 1st level nested
	public void test003() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case  Rectangle(/*here*/ ColoredPoin(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr)  -> {\n"
				+ "        		yield 1;  \n"
				+ "        } \n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/ ColoredPoin";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("ColoredPoint[TYPE_REF]{ColoredPoint, , LColoredPoint;, null, null, 52}",
				requestor.getResults());

	}
	//content assist in record pattern switch case - 2nd level nested
	public void test004() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case  Rectangle(ColoredPoint(/*here*/ Poin(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr)  -> {\n"
				+ "        		yield 1;  \n"
				+ "        } \n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/ Poin";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("Point[TYPE_REF]{Point, , LPoint;, null, null, 52}",
				requestor.getResults());

	}
	//content assist in record pattern switch case - 2nd param in nested
	public void test005() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case  Rectangle(ColoredPoint( Point(int x, int y), /*here*/ Colo c),\n"
				+ "                               ColoredPoint lr)  -> {\n"
				+ "        		yield 1;  \n"
				+ "        } \n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/ Colo";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("Color[TYPE_REF]{Color, , LColor;, null, null, 52}\n"
				+ "ColoredPoint[TYPE_REF]{ColoredPoint, , LColoredPoint;, null, null, 52}",
				requestor.getResults());

	}
	//content assist in record pattern switch case - use the variable in case statement
	public void test006() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case  Rectangle(ColoredPoint( Point(int x, int y),  Color c),\n"
				+ "                               ColoredPoint lr)  -> {\n"
				+ "        		lr.toStrin ;"
				+ "				yield 1;\n"
				+ "        } \n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "lr.toStrin";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("toString[METHOD_REF]{toString(), LColoredPoint;, ()Ljava.lang.String;, toString, null, 60}",
				requestor.getResults());

	}
	//content assist in record pattern -instanceof record pattern - 2nd param in nested
	public void test007() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                              /*here*/ ColoredPoin lr) r1)) {\n"
				+ "        System.out.println(\"Upper-left corner: \" + r1);\n"
				+ "    }\n"
				+ "  }\n"
				+ "  public static void main(String[] obj) {\n"
				+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
				+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/ ColoredPoin";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("ColoredPoint[TYPE_REF]{ColoredPoint, , LColoredPoint;, null, null, 52}",
				requestor.getResults());

	}
	//content assist in record pattern -instanceof record pattern - record creation
		public void test008() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/Completion/src/X.java",
					"@SuppressWarnings(\"preview\")"
					+ "public class X {\n"
					+ "  static void print(Rectangle r) {\n"
					+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
					+ "                              /*here*/ ColoredPoin lr) r1)) {\n"
					+ "        System.out.println(\"Upper-left corner: \" + r1);\n"
					+ "    }\n"
					+ "  }\n"
					+ "  public static void main(String[] obj) {\n"
					+ "    print(new Rectangle(new /*here*/ ColoredPoin(new Point(0, 0), Color.BLUE), \n"
					+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
					+ "  }\n"
					+ "}\n"
					+ "record Point(int x, int y) {}\n"
					+ "enum Color { RED, GREEN, BLUE }\n"
					+ "record ColoredPoint(Point p, Color c) {}\n"
					+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
					);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/ ColoredPoin";
			int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("ColoredPoint[TYPE_REF]{ColoredPoint, , LColoredPoint;, null, null, 52}",
					requestor.getResults());

		}
		//content assist in record pattern -check for when
		public void test009() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/Completion/src/X.java",
					"@SuppressWarnings(\"preview\")"
							+ "public class X {\n"
							+ "  public static void printLowerRight(Rectangle r) {\n"
							+ "    int res = switch(r) {\n"
							+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
							+ "                               ColoredPoint lr) whe x > 0 -> {\n"
							+ "        		yield 1;  \n"
							+ "        } \n"
							+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
							+ "                               ColoredPoint lr) when x <= 0 -> {\n"
							+ "        		yield -1;  \n"
							+ "        } \n"
							+ "        default -> 0;\n"
							+ "    }; \n"
							+ "    System.out.println(\"Returns: \" + res);\n"
							+ "  }\n"
							+ "  public static void main(String[] args) {\n"
							+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
							+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
							+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE), \n"
							+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
							+ "  }\n"
							+ "}\n"
							+ "record Point(int x, int y) {}\n"
							+ "enum Color { RED, GREEN, BLUE }\n"
							+ "record ColoredPoint(Point p, Color c) {}\n"
							+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
					);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "whe";
			int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("when[KEYWORD]{when, null, null, when, null, 49}",
					requestor.getResults());

		}
		//content assist in record pattern switch case - use the variable in case statement - CCE
		public void test010() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/Completion/src/X.java",
					"@SuppressWarnings(\"preview\")"
					+ "public class X {\n"
					+ "  public static void printLowerRight(Rectangle r) {\n"
					+ "    int res = switch(r) {\n"
					+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
					+ "                               ColoredPoint lr)  -> {\n"
					+ "    			/*here*/lr.co	"
					+ "System.out.println(\"x= \" + x);\n"
					+ "    				System.out.println(\"y= \" + y);\n"
					+ "    				System.out.println(\"lr= \" + lr);\n"
					+ "    				System.out.println(\"lr.c()= \" + lr.c());\n"
					+ "    				System.out.println(\"lr.p()= \" + lr.p());\n"
					+ "    				System.out.println(\"lr.p().x()= \" + lr.p().x());\n"
					+ "    				System.out.println(\"lr.p().y()= \" + lr.p().y());\n"
					+ "    				System.out.println(\"c= \" + c);\n"
					+ "    				System.out.println(\"r1= \" + r1);\n"
					+ "        		yield x;  \n"
					+ "        } \n"
					+ "        default -> 0;\n"
					+ "    }; \n"
					+ "    System.out.println(\"Returns: \" + res);\n"
					+ "  }\n"
					+ "  public static void main(String[] args) {\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "  }\n"
					+ "}\n"
					+ "record Point(int x, int y) {}\n"
					+ "enum Color { RED, GREEN, BLUE }\n"
					+ "record ColoredPoint(Point p, Color color) {}\n"
					+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
					);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/lr.co";
			int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("color[FIELD_REF]{color, LColoredPoint;, LColor;, color, null, 60}\n"
					+ "color[METHOD_REF]{color(), LColoredPoint;, ()LColor;, color, null, 60}",
					requestor.getResults());

		}
		//content assist ArrayStoreException fix
		//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/345
		public void test011() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/Completion/src/X.java",
					"@SuppressWarnings(\"preview\")"
					+ "public class X {\n"
					+ "  public static void printLowerRight(Rectangle r) {\n"
					+ "    int res = switch(r) {\n"
					+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
					+ "                               ColoredPoint lr)  -> {\n"
					+ "                               fals "
					+ "        		yield 1;  \n"
					+ "        } \n"
					+ "        default -> 0;\n"
					+ "    }; \n"
					+ "     \n"
					+ "  }\n"
					+ "  public static void main(String[] args) {\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "  }\n"
					+ "}\n"
					+ "record Point(int x, int y) {}\n"
					+ "enum Color { RED, GREEN, BLUE }\n"
					+ "record ColoredPoint(Point p, Color c) {}\n"
					+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
					);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "fals";
			int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("false[KEYWORD]{false, null, null, false, null, 52}",
					requestor.getResults());

		}
		public void test012() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/Completion/src/X.java",
					"public class X {\n"
					+ "    public static boolean foo(Object o) {\n"
					+ "        boolean ret = false;\n"
					+ "        R[] recArray = {new R(0)};\n"
					+ "        for (R(int x_1) : recArray) {\n"
					+ "            System.out.println(x_);  \n"
					+ "            ret = true;\n"
					+ "        }\n"
					+ "        return ret;\n"
					+ "    }\n"
					+ "}\n"
					+ "record R(int i) {}"
					);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "x_";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("x_1[LOCAL_VARIABLE_REF]{x_1, null, I, x_1, null, 82}",
					requestor.getResults());
		}
		public void test013() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/Completion/src/X.java",
					"public class X {\n"
					+ "    public static void foo(ColoredRectangle[] array) {\n"
					+ "       for(ColoredRectangle(int x_1, int y_1, Color col) : array) {\n"
					+ "    	  int per = 2 * x_ + 2 * y_1;\n"
					+ "       }\n"
					+ "    }\n"
					+ "}\n"
					+ "record ColoredRectangle(int length, int width, Color color) {}\n"
					+ "enum Color {\n"
					+ "	RED, GREEN, BLUE;\n"
					+ "}"
					);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "x_";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("x_1[LOCAL_VARIABLE_REF]{x_1, null, I, x_1, null, 82}",
					requestor.getResults());
		}
		public void test014() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/Completion/src/X.java",
					"public class X {\n"
					+ "    public static void foo(ColoredRectangle[] array) {\n"
					+ "       for(ColoredRectangle(int x_1, int y_1, Color col) : array) {\n"
					+ "    	  int per = 2 * x_1 + 2 * y_;\n"
					+ "       }\n"
					+ "    }\n"
					+ "}\n"
					+ "record ColoredRectangle(int length, int width, Color color) {}\n"
					+ "enum Color {\n"
					+ "	RED, GREEN, BLUE;\n"
					+ "}"
					);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "y_";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("y_1[LOCAL_VARIABLE_REF]{y_1, null, I, y_1, null, 82}",
					requestor.getResults());
		}
		public void test015() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/Completion/src/X.java",
					"public class X {\n"
					+ "    public static void foo(ColoredRectangle[] ar_ray) {\n"
					+ "       for(ColoredRectangle(int x_1, int y_1, Color col) : ar_) {\n"
					+ "    	  int per = 2 * x_1 + 2 * y_1;\n"
					+ "       }\n"
					+ "    }\n"
					+ "}\n"
					+ "record ColoredRectangle(int length, int width, Color color) {}\n"
					+ "enum Color {\n"
					+ "	RED, GREEN, BLUE;\n"
					+ "}"
					);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "ar_";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("ar_ray[LOCAL_VARIABLE_REF]{ar_ray, null, [LColoredRectangle;, ar_ray, null, 52}",
					requestor.getResults());
		}

		public void testGH2299_SwitchStatement() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
					public class SwitchRecordPattern {
						public void foo(java.io.Serializable o) {
							switch(o) {
								case Person(var name, var age) : {
									/*here*/nam
								}
							}
						}
					}\
					""");
			this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
					public record Person(String name, int age) implements java.io.Serializable  {}\
					""");
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/nam";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, " + UNQUALIFIED_REL + "}",
					requestor.getResults());
		}

		public void testGH2299_SwitchExpression() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
					public class SwitchRecordPattern {
						public void foo(java.io.Serializable o) {
							String result = switch(o) {
								case Person(var name, var age) -> {
									/*here*/nam
								}
							};
						}
					}\
					""");
			this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
					public record Person(String name, int age) implements java.io.Serializable  {}\
					""");
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/nam";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, " + UNQUALIFIED_REL + "}",
					requestor.getResults());
		}

		public void testGH2299_Switch_StatementsBeforeCompletion() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
					public class SwitchRecordPattern {
						public void foo(java.io.Serializable o) {
							switch(o) {
								case Person(var name, var age): {
									System.out.println(age);
									/*here*/nam
								}
							};
						}
					}\
					""");
			this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
					public record Person(String name, int age) implements java.io.Serializable  {}\
					""");
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/nam";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, " + UNQUALIFIED_REL + "}",
					requestor.getResults());
		}

		public void testGH2299_Switch_CompletionInSideDifferentControlBlock() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
					public class SwitchRecordPattern {
						public void foo(java.io.Serializable o) {
							switch(o) {
								case Person(var name, var age): {
									if (age > 10) {
										/*here*/nam
									}
								}
							};
						}
					}\
					""");
			this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
					public record Person(String name, int age) implements java.io.Serializable  {}\
					""");
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/nam";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, " + UNQUALIFIED_REL + "}",
					requestor.getResults());
		}

		public void testGH2299_Switch_CompletionInsideLambda() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
					interface List<T> { Stream<T> stream(); }
					interface Stream<T> { Stream<T> filter(Predicate<T> pred); }
					interface Predicate<T> { boolean test(T t); }
					public class SwitchRecordPattern {
						public void foo(java.io.Serializable o, List<String> col) {
							switch(o) {
								case Person(var name, var age): {
									col.stream().filter(el -> el.equals(/*here*/nam))
								}
							};
						}
					}\
					""");
			this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
					public record Person(String name, int age) implements java.io.Serializable  {}\
					""");
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/nam";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, " + (UNQUALIFIED_REL + R_PACKAGE_EXPECTED_TYPE) + "}",
					requestor.getResults());
		}

		public void testGH2299_Switch_SwitchInsideLambda() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
					interface List<T> { Stream<T> stream(); }
					interface Stream<T> { Stream<T> filter(Predicate<T> pred); }
					interface Predicate<T> { boolean test(T t); }
					public class SwitchRecordPattern {
						public void foo(List<java.io.Serializable> col) {
							col.stream().filter(el -> {
								switch(el) {
									case Person(var name, var age): {
										/*here*/nam
									}
								};
							})
						}
					}\
					""");
			this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
					public record Person(String name, int age) implements java.io.Serializable  {}\
					""");
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/nam";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, " + UNQUALIFIED_REL + "}",
					requestor.getResults());
		}

		public void testGH2299_SwitchInsideAnotherControlStatement() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
					public class SwitchRecordPattern {
						public void foo(java.util.List<java.io.Serializable> col) {
							if(col.size() > 1) {
								switch(col.get(0)) {
									case Person(var name, var age): {
										/*here*/nam
									}
								};
							}
						}
					}\
					""");
			this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
					public record Person(String name, int age) implements java.io.Serializable  {}\
					""");
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/nam";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, " + UNQUALIFIED_REL + "}",
					requestor.getResults());
		}

		public void testGH2299_NestedSwitchRecordPatternsCompletionVariableOnParentSwitch() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
					public class SwitchRecordPattern {
						public void foo(java.io.Serializable parent, java.io.Serializable child) {
							switch(parent) {
								case Person(var name, var age): {
									switch(child) {
										case Person(var childName, var childAge): {
											/*here*/nam
										}
									}
								}
							};
						}
					}\
					""");
			this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
					public record Person(String name, int age) implements java.io.Serializable  {}\
					""");
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/nam";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, " + UNQUALIFIED_REL + "}",
					requestor.getResults());
		}

		public void testGH2299_NestedSwitchRecordPatternsCompletionVariableOnChildSwitch() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
					public class SwitchRecordPattern {
						public void foo(java.io.Serializable parent, java.io.Serializable child) {
							switch(parent) {
								case Person(var name, var age): {
									switch(child) {
										case Person(var childName, var childAge): {
											/*here*/childNa
										}
									}
								}
							};
						}
					}\
					""");
			this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
					public record Person(String name, int age) implements java.io.Serializable  {}\
					""");
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/childNa";
			int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("childName[LOCAL_VARIABLE_REF]{childName, null, Ljava.lang.String;, childName, null, " + UNQUALIFIED_REL + "}",
					requestor.getResults());
		}
}
