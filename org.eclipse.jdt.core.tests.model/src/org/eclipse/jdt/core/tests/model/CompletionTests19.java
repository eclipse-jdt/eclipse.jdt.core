/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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

public class CompletionTests19 extends AbstractJavaModelCompletionTests {


	static {
		// TESTS_NAMES = new String[]{"test034"};
	}

	public CompletionTests19(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null) {

			COMPLETION_PROJECT = setUpJavaProject("Completion", "19");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "19");
		}
		super.setUpSuite();
		COMPLETION_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTests19.class);
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
				+ "                               ColoredPoint lr) r1  -> {\n"
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
				+ "                               ColoredPoint lr) r1  -> {\n"
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
		assertResults("Rectangle[TYPE_REF]{Rectangle, , LRectangle;, null, null, 52}",
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
				+ "                               ColoredPoint lr) r1  -> {\n"
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
				+ "                               ColoredPoint lr) r1  -> {\n"
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
				+ "                               ColoredPoint lr) r1  -> {\n"
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
				+ "                               ColoredPoint lr) r1  -> {\n"
				+ "        		r1.toStrin ;yield 1;  \n"
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
		String completeBehind = "r1.toStrin";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("toString[METHOD_REF]{toString(), LRectangle;, ()Ljava.lang.String;, toString, null, 60}",
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
							+ "                               ColoredPoint lr) r1 whe x > 0 -> {\n"
							+ "        		yield 1;  \n"
							+ "        } \n"
							+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
							+ "                               ColoredPoint lr) r1 when x <= 0 -> {\n"
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
					+ "                               ColoredPoint lr) r1  -> {\n"
					+ "    			/*here*/r1.	"
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
					+ "record ColoredPoint(Point p, Color c) {}\n"
					+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
					);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "/*here*/r1.";
			int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("equals[METHOD_REF]{equals(), LRectangle;, (Ljava.lang.Object;)Z, equals, (arg0), 60}\n"
					+ "hashCode[METHOD_REF]{hashCode(), LRectangle;, ()I, hashCode, null, 60}\n"
					+ "lowerRight[FIELD_REF]{lowerRight, LRectangle;, LColoredPoint;, lowerRight, null, 60}\n"
					+ "lowerRight[METHOD_REF]{lowerRight(), LRectangle;, ()LColoredPoint;, lowerRight, null, 60}\n"
					+ "toString[METHOD_REF]{toString(), LRectangle;, ()Ljava.lang.String;, toString, null, 60}\n"
					+ "upperLeft[FIELD_REF]{upperLeft, LRectangle;, LColoredPoint;, upperLeft, null, 60}\n"
					+ "upperLeft[METHOD_REF]{upperLeft(), LRectangle;, ()LColoredPoint;, upperLeft, null, 60}",
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
					+ "                               ColoredPoint lr) r1  -> {\n"
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
}
