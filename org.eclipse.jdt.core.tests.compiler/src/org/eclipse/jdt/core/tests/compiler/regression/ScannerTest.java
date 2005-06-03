/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
public class ScannerTest extends AbstractRegressionTest {

	public ScannerTest(String name) {
		super(name);
	}
	public static Test suite() {
		return setupSuite(testClass());
	}

	public static Class testClass() {
		return ScannerTest.class;
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test001() {
		String sourceA001 = "\\u003b";
		IScanner scanner = ToolFactory.createScanner(false, true, false, false);
		scanner.setSource(sourceA001.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong token type", ITerminalSymbols.TokenNameSEMICOLON, token);
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test002() {
		String sourceA002 = "// tests\n  ";
		IScanner scanner = ToolFactory.createScanner(false, true, false, false);
		scanner.setSource(sourceA002.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameWHITESPACE, token);
			assertEquals("Wrong size", 2, scanner.getCurrentTokenSource().length);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test003() {
		String sourceA003 = "// tests\n  ";
		IScanner scanner = ToolFactory.createScanner(true, true, false, false);
		scanner.setSource(sourceA003.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameCOMMENT_LINE, token);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameWHITESPACE, token);
			assertEquals("Wrong size", 2, scanner.getCurrentTokenSource().length);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}

	/**
	 * float constant can have exponent part without dot: 01e0f
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=30704
	 */
	public void test004() {
		String source = "01e0f";
		IScanner scanner = ToolFactory.createScanner(false, false, false, false);
		scanner.setSource(source.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameFloatingPointLiteral, token);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43437
	 */
	public void test005() {
		StringBuffer buf = new StringBuffer();
		buf.append("\"Hello\"");
		String str = buf.toString();
		IScanner scanner = ToolFactory.createScanner(true, false, false, false);
		scanner.setSource(str.toCharArray());
		scanner.resetTo(0, str.length() - 1);
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameStringLiteral, token);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43485
	 */
	public void test006() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, false);
		try {
			scanner.setSource(null);
		} catch (NullPointerException e) {
			assertTrue(false);
		}
	}

	/*
	 * Check that bogus resetTo issues EOFs
	 */
	public void test007() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, false);
		char[] source = "int i = 0;".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(source.length + 50, source.length - 1);
		int token = -1;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Expecting EOF", ITerminalSymbols.TokenNameEOF, token);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test008() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x11aa.aap-3333f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int token = -1;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameFloatingPointLiteral, token);
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Expecting EOF", ITerminalSymbols.TokenNameEOF, token);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test009() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_4);
		char[] source = "0x11aa.aap-3333f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 5, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test010() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x11aa.aap-3333f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test011() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x.aap-3333f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test012() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaap3f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test013() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaapaf".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test014() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaap.1f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test015() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaa.p1f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test016() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaa.p1F".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test017() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaa.p1D".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test018() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaa.p1d".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}
	
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74934
	 */
	public void test019() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_4);
		char[] source = "0x".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}
	
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74934
	 */
	public void test020() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}
	
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74934
	 */
	public void test021() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_4);
		char[] source = "0x1".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}
	
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74934
	 */
	public void test022() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x1".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}
	
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78905 
	 */
	public void test023() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x.p-2".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
			}
			assertTrue(false);
		} catch (InvalidInputException e) {
			assertTrue(true);
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=84398
	 */
	public void test024() {
		IScanner scanner = ToolFactory.createScanner(false, false, true, JavaCore.VERSION_1_5);
		char[] source = "public class X {\n\n}".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
		}
		
		assertEquals("wrong number of tokens", 5, counter);
		int[] lineEnds = scanner.getLineEnds();
		assertNotNull("No line ends", lineEnds);
		assertEquals("wrong length", 2, lineEnds.length);
		source = "public class X {}".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		lineEnds = scanner.getLineEnds();
		assertNotNull("No line ends", lineEnds);
		assertEquals("wrong length", 0, lineEnds.length);
		
		counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
		}
		
		assertEquals("wrong number of tokens", 5, counter);
		lineEnds = scanner.getLineEnds();
		assertNotNull("No line ends", lineEnds);
		assertEquals("wrong length", 0, lineEnds.length);
	}
	
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=84398
	 */
	public void test025() {
		IScanner scanner = ToolFactory.createScanner(true, true, false, true);
		scanner.setSource("String\r\nwith\r\nmany\r\nmany\r\nline\r\nbreaks".toCharArray());
		
		try {
			while(scanner.getNextToken()!=ITerminalSymbols.TokenNameEOF){}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		
		assertEquals("Wrong size", 5, scanner.getLineEnds().length);
		
		scanner.setSource("No line breaks here".toCharArray()); // expecting line breaks to reset
		assertEquals("Wrong size", 0, scanner.getLineEnds().length);
	}
	
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=86611 
	 */
	public void test026() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_4);
		char[] source = "0x.p-2".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
			}
			assertTrue(false);
		} catch (InvalidInputException e) {
			assertTrue(true);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test027() {
		char[] source = ("class Test {\n" +
				"  char  C = \"\\u005Cn\";\n" +
				"}").toCharArray();
		Scanner scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_4, null, null, false);
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			StringBuffer buffer = new StringBuffer();
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				try {
					switch(token) {
						case TerminalTokens.TokenNameEOF :
							break;
						default :
							buffer.append(scanner.getCurrentTokenSource());
							break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			assertEquals("Wrong contents", "classTest{charC=\"\n\";}", String.valueOf(buffer));
		} catch (InvalidInputException e) {
			assertTrue(false);
		}		
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test028() {
		char[] source = ("class Test {\n" +
				"  char  C = \'\\u005Cn\';\n" +
				"}").toCharArray();
		Scanner scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_4, null, null, false);
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			StringBuffer buffer = new StringBuffer();
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				try {
					switch(token) {
						case TerminalTokens.TokenNameStringLiteral :
							buffer.append(new String(scanner.getCurrentTokenSourceString()));
							break;
						case TerminalTokens.TokenNameEOF :
							break;
						default :
							buffer.append(scanner.getCurrentTokenSource());
							break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			assertEquals("Wrong contents", "classTest{charC=\'\\n\';}", String.valueOf(buffer));
		} catch (InvalidInputException e) {
			assertTrue(false);
		}		
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test029() {
		char[] source = ("class Test {\n" +
				"  char  C = \"\\n\";\n" +
				"}").toCharArray();
		Scanner scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_4, null, null, false);
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			StringBuffer buffer = new StringBuffer();
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				try {
					switch(token) {
						case TerminalTokens.TokenNameEOF :
							break;
						default :
							buffer.append(scanner.getCurrentTokenSource());
							break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			assertEquals("Wrong contents", "classTest{charC=\"\n\";}", String.valueOf(buffer));
		} catch (InvalidInputException e) {
			assertTrue(false);
		}		
	}
	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test030() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test {\n" +
					"  static String C = \"\\n\";\n" +
					"  \n" +
					"  public static void main(String[] args) {\n" +
					"  	System.out.print(C.length());\n" +
					"  	System.out.print(C.charAt(0) == \'\\n\');\n" +
					"  }\n" +
					"}"
				},
				"1true");
	}
	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test031() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test {\n" +
					"  static String C = \"\\u005Cn\";\n" +
					"  \n" +
					"  public static void main(String[] args) {\n" +
					"  	System.out.print(C.length());\n" +
					"  	System.out.print(C.charAt(0) == \'\\n\');\n" +
					"  }\n" +
					"}"
				},
				"1true");
	}
	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test032() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test {\n" +
					"  static char C = \'\\u005Cn\';\n" +
					"  \n" +
					"  public static void main(String[] args) {\n" +
					"  	System.out.print(C == \'\\n\');\n" +
					"  }\n" +
					"}"
				},
				"true");
	}
	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test033() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test {\n" +
					"  static char C = \\u0027\\u005Cn\\u0027;\n" +
					"  \n" +
					"  public static void main(String[] args) {\n" +
					"  	System.out.print(C == \'\\n\');\n" +
					"  }\n" +
					"}"
				},
				"true");
	}
	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test034() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test {\n" +
					"  static String C = \"\u0043\\n\\u0043\";\n" +
					"  \n" +
					"  public static void main(String[] args) {\n" +
					"  	System.out.print(C.length());\n" +
					"  	System.out.print(C.charAt(1) == \'\\n\');\n" +
					"  }\n" +
					"}"
				},
				"3true");
	}
	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test035() {
		/*
		 * Corresponding source:
		 * 
		 * public class Test {
		 * 	  static String C = "\n";
		 *    public static void main(String[] args) {
		 * 	  	System.out.print(C.length());
		 * 		  	System.out.print(C.charAt(0) == '\n');
		 *    }
		 * }
		 */
		this.runConformTest(
				new String[] {
					"Test.java",
					"\\u0070\\u0075\\u0062\\u006c\\u0069\\u0063\\u0020\\u0063\\u006c\\u0061\\u0073\\u0073\\u0020\\u0054\\u0065\\u0073\\u0074\\u0020\\u007b\\u000A\n" +
					"\\u0020\\u0020\\u0073\\u0074\\u0061\\u0074\\u0069\\u0063\\u0020\\u0053\\u0074\\u0072\\u0069\\u006e\\u0067\\u0020\\u0043\\u0020\\u003d\\u0020\\u0022\\u005c\\u006e\\u0022\\u003b\\u000A\n" +
					"\\u0020\\u0020\\u000A\n" +
					"\\u0020\\u0020\\u0070\\u0075\\u0062\\u006c\\u0069\\u0063\\u0020\\u0073\\u0074\\u0061\\u0074\\u0069\\u0063\\u0020\\u0076\\u006f\\u0069\\u0064\\u0020\\u006d\\u0061\\u0069\\u006e\\u0028\\u0053\\u0074\\u0072\\u0069\\u006e\\u0067\\u005b\\u005d\\u0020\\u0061\\u0072\\u0067\\u0073\\u0029\\u0020\\u007b\\u000A\n" +
					"\\u0020\\u0020\\u0009\\u0053\\u0079\\u0073\\u0074\\u0065\\u006d\\u002e\\u006f\\u0075\\u0074\\u002e\\u0070\\u0072\\u0069\\u006e\\u0074\\u0028\\u0043\\u002e\\u006c\\u0065\\u006e\\u0067\\u0074\\u0068\\u0028\\u0029\\u0029\\u003b\\u000A\n" +
					"\\u0020\\u0020\\u0009\\u0053\\u0079\\u0073\\u0074\\u0065\\u006d\\u002e\\u006f\\u0075\\u0074\\u002e\\u0070\\u0072\\u0069\\u006e\\u0074\\u0028\\u0043\\u002e\\u0063\\u0068\\u0061\\u0072\\u0041\\u0074\\u0028\\u0030\\u0029\\u0020\\u003d\\u003d\\u0020\\u0027\\u005c\\u006e\\u0027\\u0029\\u003b\\u000A\n" +
					"\\u0020\\u0020\\u007d\\u0020\\u0009\\u000A\n" +
					"\\u007d"
				},
				"1true");
	}
}

