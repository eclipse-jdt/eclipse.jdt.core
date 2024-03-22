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


	static {
//		 TESTS_NAMES = new String[]{"test012"};
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
				"""
					@SuppressWarnings("preview")\
					public class X {
					  public static void printLowerRight(Rectangle r) {
					    int res = switch(r) {
					       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
					                               ColoredPoint lr)  -> {
					        		yield 1; \s
					        }\s
					        default -> 0;
					    };\s
					    fals\s
					  }
					  public static void main(String[] args) {
					    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\s
					        new ColoredPoint(new Point(30, 10), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
				"""
					@SuppressWarnings("preview")\
					public class X {
					  public static void printLowerRight(Rectangle r) {
					    int res = switch(r) {
					       case /*here*/ Rectangl (ColoredPoint(Point(int x, int y), Color c),
					                               ColoredPoint lr)  -> {
					        		yield 1; \s
					        }\s
					        default -> 0;
					    };\s
					  }
					  public static void main(String[] args) {
					    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\s
					        new ColoredPoint(new Point(30, 10), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
				"""
					@SuppressWarnings("preview")\
					public class X {
					  public static void printLowerRight(Rectangle r) {
					    int res = switch(r) {
					       case  Rectangle(/*here*/ ColoredPoin(Point(int x, int y), Color c),
					                               ColoredPoint lr)  -> {
					        		yield 1; \s
					        }\s
					        default -> 0;
					    };\s
					  }
					  public static void main(String[] args) {
					    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\s
					        new ColoredPoint(new Point(30, 10), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
				"""
					@SuppressWarnings("preview")\
					public class X {
					  public static void printLowerRight(Rectangle r) {
					    int res = switch(r) {
					       case  Rectangle(ColoredPoint(/*here*/ Poin(int x, int y), Color c),
					                               ColoredPoint lr)  -> {
					        		yield 1; \s
					        }\s
					        default -> 0;
					    };\s
					  }
					  public static void main(String[] args) {
					    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\s
					        new ColoredPoint(new Point(30, 10), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
				"""
					@SuppressWarnings("preview")\
					public class X {
					  public static void printLowerRight(Rectangle r) {
					    int res = switch(r) {
					       case  Rectangle(ColoredPoint( Point(int x, int y), /*here*/ Colo c),
					                               ColoredPoint lr)  -> {
					        		yield 1; \s
					        }\s
					        default -> 0;
					    };\s
					  }
					  public static void main(String[] args) {
					    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\s
					        new ColoredPoint(new Point(30, 10), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
				"""
					@SuppressWarnings("preview")\
					public class X {
					  public static void printLowerRight(Rectangle r) {
					    int res = switch(r) {
					       case  Rectangle(ColoredPoint( Point(int x, int y),  Color c),
					                               ColoredPoint lr)  -> {
					        		lr.toStrin ;\
									yield 1;
					        }\s
					        default -> 0;
					    };\s
					  }
					  public static void main(String[] args) {
					    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\s
					        new ColoredPoint(new Point(30, 10), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
				"""
					@SuppressWarnings("preview")\
					public class X {
					  static void print(Rectangle r) {
					    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),
					                              /*here*/ ColoredPoin lr) r1)) {
					        System.out.println("Upper-left corner: " + r1);
					    }
					  }
					  public static void main(String[] obj) {
					    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),\s
					                               new ColoredPoint(new Point(10, 15), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
					"""
						@SuppressWarnings("preview")\
						public class X {
						  static void print(Rectangle r) {
						    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                              /*here*/ ColoredPoin lr) r1)) {
						        System.out.println("Upper-left corner: " + r1);
						    }
						  }
						  public static void main(String[] obj) {
						    print(new Rectangle(new /*here*/ ColoredPoin(new Point(0, 0), Color.BLUE),\s
						                               new ColoredPoint(new Point(10, 15), Color.RED)));
						  }
						}
						record Point(int x, int y) {}
						enum Color { RED, GREEN, BLUE }
						record ColoredPoint(Point p, Color c) {}
						record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
					"""
						@SuppressWarnings("preview")\
						public class X {
						  public static void printLowerRight(Rectangle r) {
						    int res = switch(r) {
						       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                               ColoredPoint lr) whe x > 0 -> {
						        		yield 1; \s
						        }\s
						       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                               ColoredPoint lr) when x <= 0 -> {
						        		yield -1; \s
						        }\s
						        default -> 0;
						    };\s
						    System.out.println("Returns: " + res);
						  }
						  public static void main(String[] args) {
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),\s
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),\s
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						  }
						}
						record Point(int x, int y) {}
						enum Color { RED, GREEN, BLUE }
						record ColoredPoint(Point p, Color c) {}
						record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
					"""
						@SuppressWarnings("preview")\
						public class X {
						  public static void printLowerRight(Rectangle r) {
						    int res = switch(r) {
						       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                               ColoredPoint lr)  -> {
						    			/*here*/lr.co	\
						System.out.println("x= " + x);
						    				System.out.println("y= " + y);
						    				System.out.println("lr= " + lr);
						    				System.out.println("lr.c()= " + lr.c());
						    				System.out.println("lr.p()= " + lr.p());
						    				System.out.println("lr.p().x()= " + lr.p().x());
						    				System.out.println("lr.p().y()= " + lr.p().y());
						    				System.out.println("c= " + c);
						    				System.out.println("r1= " + r1);
						        		yield x; \s
						        }\s
						        default -> 0;
						    };\s
						    System.out.println("Returns: " + res);
						  }
						  public static void main(String[] args) {
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\s
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						  }
						}
						record Point(int x, int y) {}
						enum Color { RED, GREEN, BLUE }
						record ColoredPoint(Point p, Color color) {}
						record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
					"""
						@SuppressWarnings("preview")\
						public class X {
						  public static void printLowerRight(Rectangle r) {
						    int res = switch(r) {
						       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                               ColoredPoint lr)  -> {
						                               fals \
						        		yield 1; \s
						        }\s
						        default -> 0;
						    };\s
						    \s
						  }
						  public static void main(String[] args) {
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\s
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						  }
						}
						record Point(int x, int y) {}
						enum Color { RED, GREEN, BLUE }
						record ColoredPoint(Point p, Color c) {}
						record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
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
					"""
						public class X {
						    public static boolean foo(Object o) {
						        boolean ret = false;
						        R[] recArray = {new R(0)};
						        for (R(int x_1) : recArray) {
						            System.out.println(x_); \s
						            ret = true;
						        }
						        return ret;
						    }
						}
						record R(int i) {}"""
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
					"""
						public class X {
						    public static void foo(ColoredRectangle[] array) {
						       for(ColoredRectangle(int x_1, int y_1, Color col) : array) {
						    	  int per = 2 * x_ + 2 * y_1;
						       }
						    }
						}
						record ColoredRectangle(int length, int width, Color color) {}
						enum Color {
							RED, GREEN, BLUE;
						}"""
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
					"""
						public class X {
						    public static void foo(ColoredRectangle[] array) {
						       for(ColoredRectangle(int x_1, int y_1, Color col) : array) {
						    	  int per = 2 * x_1 + 2 * y_;
						       }
						    }
						}
						record ColoredRectangle(int length, int width, Color color) {}
						enum Color {
							RED, GREEN, BLUE;
						}"""
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
					"""
						public class X {
						    public static void foo(ColoredRectangle[] ar_ray) {
						       for(ColoredRectangle(int x_1, int y_1, Color col) : ar_) {
						    	  int per = 2 * x_1 + 2 * y_1;
						       }
						    }
						}
						record ColoredRectangle(int length, int width, Color color) {}
						enum Color {
							RED, GREEN, BLUE;
						}"""
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
}
