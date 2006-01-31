/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;
public class TryStatementTest extends AbstractRegressionTest {
	
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 34 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryStatementTest(String name) {
	super(name);
}
public static Test suite() {
	return buildTestSuite(testClass());
}
public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public class X1 {\n" + 
		"    public X1() throws Exception {\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public void method1(){\n" + 
		"    try {\n" + 
		"      new X1() {\n" + 
		"      };\n" + 
		"    } catch(Exception e){\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"import java.io.*;\n" + 
		"import java.util.zip.*;\n" + 
		"class X {\n" + 
		"  void bar() throws ZipException, IOException {}\n" + 
		"  void foo() {\n" + 
		"    try {\n" + 
		"      bar();\n" + 
		"    } catch (ZipException e) {\n" + 
		"    } catch (IOException e) {\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public class A1 {\n" + 
		"    public A1() throws Exception {\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public void method1(){\n" + 
		"    try {\n" + 
		"      new A1() {\n" + 
		"      };\n" + 
		"    } catch(Exception e){\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test004() {
	this.runConformTest(new String[] {
		"p/ATC.java",
		"package p;\n" + 
		"public class ATC {\n" + 
		"    \n" + 
		"    public class B extends Exception {\n" + 
		"      public B(String msg) { super(msg); }\n" + 
		"    }\n" + 
		"    \n" + 
		"    void foo() throws ATC.B {\n" + 
		"      Object hello$1 = null;\n" + 
		"      try {\n" + 
		"        throw new B(\"Inside foo()\");\n" + 
		"      } catch(B e) {\n" + 
		"        System.out.println(\"Caught B\");\n" + 
		"      }    \n" + 
		"    }       \n" + 
		"}\n",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/A.java",
		"package p;\n" + 
		"import java.io.IOException;\n" + 
		"import java.util.Vector;\n" + 
		"/**\n" + 
		" * This test0 should run without producing a java.lang.ClassFormatError\n" + 
		" */\n" + 
		"public class A {\n" + 
		"  public Vector getComponents () {\n" + 
		"    try{\n" + 
		"      throw new IOException();\n" + 
		"    }\n" + 
		"    catch (IOException ioe) {\n" + 
		"    }\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    new A().getComponents();\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test006() {
	this.runConformTest(new String[] {
		"p/T.java",
		"package p;\n" + 
		"import java.lang.reflect.*;\n" + 
		"public class T extends InvocationTargetException {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    T ct = new T();\n" + 
		"    ct.getTargetException();\n" + 
		"  }\n" + 
		"  public Throwable getTargetException() {\n" + 
		"    Runnable runnable = new Runnable() {\n" + 
		"      public void run() {\n" + 
		"        System.out.println(\"we got here\");\n" + 
		"        T.super.getTargetException();\n" + 
		"      }\n" + 
		"    };\n" + 
		"    runnable.run();\n" + 
		"    return new Throwable();\n" + 
		"  }\n" + 
		"}\n",
	});
}
public void test007() {
	this.runConformTest(new String[] {
		"TryFinally.java", 
		"class TryFinally {	\n"+
		"	public int readFile(String filename) throws Exception {	\n"+
		"		int interfaceID = -1;	\n"+
		"		int iNdx = 0;	\n"+
		"		try {	\n"+
		"			try {	\n"+
		"				return interfaceID;	\n"+
		"			} // end try	\n"+
		"			finally {	\n"+
		"				iNdx = 1;	\n"+
		"			} // end finally	\n"+
		"		} // end try	\n"+
		"		catch (Exception ex) {	\n"+
		"			throw new Exception(\"general exception \" + ex.getMessage() + \" on processing file \" + filename);	\n"+
		"		} // end catch	\n"+
		"		finally {	\n"+
		"		} // end finally	\n"+
		"	} // end readFile method	\n"+
		"}	\n"
});
}
/*
 * 1FZR1TO: IVJCOM:WIN - Class does not compile in VAJava 3.02-Java2
 */
public void test008() {
	this.runConformTest(
		new String[] {
			"RedundantException.java",
			"import java.io.*;\n" + 
			"public class RedundantException {\n" + 
			"	/**\n" + 
			"	     * Runs the class as an application.\n" + 
			"	     */\n" + 
			"	public static void main(String[] args) {\n" + 
			"		RedundantException re = new RedundantException();\n" + 
			"		re.catchIt();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" + 
			"	/**\n" + 
			"	     * Defines a method that lists an exception twice.\n" + 
			"	     * This can be buried in a much longer list.\n" + 
			"	     */\n" + 
			"	void throwIt() throws IOException, IOException {\n" + 
			"		throw new IOException();\n" + 
			"	}\n" + 
			"	/**\n" + 
			"	     * Catches the redundantly defined exception.\n" + 
			"	     */\n" + 
			"	void catchIt() {\n" + 
			"		try {\n" + 
			"			throwIt(); // compile error here\n" + 
			"		} catch (IOException e) {\n" + 
			"			System.out.println(\"Caught.\");\n" + 
			"		}\n" + 
			"	}\n" + 
			"}"
		},
		"Caught.\n" + 
		"SUCCESS");
}
public void test009() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"public void save() {\n" +
			"	int a = 3;\n" +
			"	try {\n" +
			"		Object warnings = null;\n" +
			"      	try {\n" +
			"         	Object contexts = null;\n" +
			"         	try {\n" +
			"            	System.out.println(warnings);\n" +
			"			 	return;\n" +
			"      	 	} catch (NullPointerException npe) {\n" +
			"				System.out.println(contexts);\n" +
			"               return;\n" +
			"       	}\n" +
			"		} catch (Exception e) {\n" +
			" 			return;\n" +
			"   	}\n" +
			"	} finally {\n" +
			"     	int b = 4;\n" +
			"       System.out.println(\"#save -> \" + b + a);\n" +
			"    }\n" +
			"}\n" +
			"public static void main(String[] args) {\n" +
			"	new Test().save();\n"+
			"}\n" +
			"}"
		},
		"null\n" + 
		"#save -> 43");
}
public void test010() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"public void save() {\n" +
			"	int a = 3;\n" +
			"	try {\n" +
			"		Object warnings = null;\n" +
			"      	try {\n" +
			"         	Object contexts = null;\n" +
			"         	try {\n" +
			"            	System.out.println(warnings);\n" +
			"			 	return;\n" +
			"      	 	} catch (NullPointerException npe) {\n" +
			"				System.out.println(contexts);\n" +
			"               return;\n" +
			"       	}\n" +
			"		} catch (Exception e) {\n" +
			" 			return;\n" +
			"   	}\n" +
			"	} catch(Exception e){\n"+
			"		Object dummy1 = null;\n" +
			"		System.out.println(dummy1);\n" +
			"		Object dummy2 = null;\n" +
			"		System.out.println(dummy2);\n" +
			"		return;\n"+
			"	} finally {\n" +
			"     	int b = 4;\n" +
			"       System.out.println(\"#save -> \" + b + a);\n" +
			"    }\n" +
			"}\n" +
			"public static void main(String[] args) {\n" +
			"	new Test().save();\n"+
			"}\n" +
			"}"
		},
		"null\n" + 
		"#save -> 43");
}

public void test011() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"public void save() {\n" +
			"	int a = 3;\n" +
			"	try {\n" +
			"		Object warnings = null;\n" +
			"      	try {\n" +
			"         	Object contexts = null;\n" +
			"         	try {\n" +
			"            	System.out.println(warnings);\n" +
			"			 	return;\n" +
			"      	 	} catch (NullPointerException npe) {\n" +
			"				System.out.println(contexts);\n" +
			"               return;\n" +
			"       	}\n" +
			"		} catch (Exception e) {\n" +
			" 			return;\n" +
			"   	}\n" +
			"	} catch(Exception e){\n"+
			"		int dummy1 = 11;\n" +
			"		System.out.println(dummy1);\n" +
			"		int dummy2 = 12;\n" +
			"		System.out.println(dummy2);\n" +
			"		return;\n"+
			"	} finally {\n" +
			"     	int b = 4;\n" +
			"       System.out.println(\"#save -> \" + b + a);\n" +
			"    }\n" +
			"}\n" +
			"public static void main(String[] args) {\n" +
			"	new Test().save();\n"+
			"}\n" +
			"}"
		},
		"null\n" + 
		"#save -> 43");
}
/*
 * 4943  Verification error
 */
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			new X().delete(args);\n" +
			"			System.out.println(\"success\");\n" +
			"		} catch (Exception e) {\n" +
			"		}\n" +
			"	}\n" +
			"	void bar(int i) {\n" +
			"	}\n" +
			"	public Object delete(String[] resources) throws IOException {\n" +
			"		try {\n" +
			"			int totalWork = 3;\n" +
			"			Object result = \"aaa\";\n" +
			"			try {\n" +
			"				return result;\n" +
			"			} catch (Exception e) {\n" +
			"				throw new IOException();\n" +
			"			} finally {\n" +
			"				bar(totalWork);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			bar(0);\n" +
			"		}\n" +
			"	}\n" +
			"}\n" 
		},
		"success");
}
 
/*
 * 4943  Verification error
 */
public void test013() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			new X().delete(args);\n" +
			"			System.out.println(\"success\");\n" +
			"		} catch (Exception e) {\n" +
			"		}\n" +
			"	}\n" +
			"	void bar(int i) {\n" +
			"	}\n" +
			"	public Object delete(String[] resources) throws IOException {\n" +
			"		try {\n" +
			"			int totalWork = 3;\n" +
			"			Object result = \"aaa\";\n" +
			"			try {\n" +
			"				return result;\n" +
			"			} catch (Exception e) {\n" +
			"				throw new IOException();\n" +
			"			} finally {\n" +
			"				bar(totalWork);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			int totalWork = 4;\n" +
			"			bar(totalWork);\n" +
			"		}\n" +
			"	}\n" +
			"}\n" 
		},
		"success");
}
public void test014() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"public void save() {\n" +
			"	int a = 3;\n" +
			"	try {\n" +
			"		Object warnings = null;\n" +
			"      	try {\n" +
			"         	int contexts = 17;\n" +
			"         	try {\n" +
			"				Object dummy = null;\n" +
			"            	System.out.println(warnings);\n" +
			"            	System.out.println(dummy);\n" +
			"			 	return;\n" +
			"      	 	} catch (NullPointerException npe) {\n" +
			"				System.out.println(contexts);\n" +
			"               return;\n" +
			"       	}\n" +
			"		} catch (Exception e) {\n" +
			" 			return;\n" +
			"   	} finally { \n" +
			"			int c = 34; \n"+
			"			System.out.println(\"#inner-finally ->\" + a + c);\n"+
			"       }\n" +
			"	} finally {\n" +
			"     	int b = 4;\n" +
			"       System.out.println(\"#save -> \" + b + a);\n" +
			"    }\n" +
			"}\n" +
			"public static void main(String[] args) {\n" +
			"	new Test().save();\n"+
			"}\n" +
			"}"
		},
		"null\n" + 
		"null\n" + 
		"#inner-finally ->334\n" + 
		"#save -> 43");
}

public void test015() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"import java.io.IOException;	\n" +
			"public class X {	\n" +
			"	public static void main(String args[]) {	\n" +
			"		try { 	\n" +
			"			new Object(){	\n" +
			"				{	\n" +
			"					if (true) throw new IOException();	\n" +
			"					if (true) throw new Exception();	\n" +
			"				}	\n" +
			"			};	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(Exception e){	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n",
		},
		"SUCCESS");
}
public void test016() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"import java.io.IOException;	\n" +
			"public class X {	\n" +
			"	public static void main(String args[]) {	\n" +
			"		class SomeClass {	\n" +
			"			SomeClass () throws IOException {	\n" +
			"			}	\n" +
			"		}	\n" +
			"		try { 	\n" +
			"			new Object(){	\n" +
			"				{	\n" +
			"					if (true) throw new IOException();	\n" +
			"					if (true) throw new Exception();	\n" +
			"				}	\n" +
			"			};	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(Exception e){	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n",
		},
		"SUCCESS");
}
public void test017() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"public class X {	\n" +
			"	public static void main(String args[]) {	\n" +
			"		try { 	\n" +
			"			new Object(){	\n" +
			"				{	\n" +
			"					foo();	\n" +
			"				}	\n" +
			"			};	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(Exception e){	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"	static class AEx extends Exception {} \n" +
			"	static class BEx extends Exception {} \n" +
			"	static void foo() throws AEx, BEx {	\n" +
			"		throw new AEx();	\n"+
			"	}	\n" +
			"}	\n",
		},
		"SUCCESS");
}

// 8773 verification error
public void test018() {
	this.runConformTest(
		new String[] {
			"VerifyEr.java",
			"public class VerifyEr {	\n" +
			"  protected boolean err(boolean b) {	\n" +
			"     try {	\n" +
			"          System.out.print(\"SUCC\");	\n" +
			"     } catch (Throwable t) {	\n" +
			"          return b;	\n" +
			"     } finally {	\n" +
			"          try {	\n" +
			"               if (b) {	\n" +
			"                    return b;	\n" +
			"               }	\n" +
			"          } finally {	\n" +
			"          		System.out.println(\"ESS\");	\n" +
			"          }	\n" +
			"     }	\n" +
			"     return false;	\n" +
			"  }	\n" +
			"  public static void main(String[] args) {	\n" +
			"     new VerifyEr().err(false);	\n" +
			"  }	\n" +
			"}	\n",
		},
		"SUCCESS");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=16279
 */
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"	String logger;	\n" +
			"  public static void main(String[] args) {	\n" +
			"    new X().foo();	\n" +
			"	}	\n"+
			"	public void foo() {	\n" +
			"		try {	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		} catch (Exception ce) {	\n" +
			"			String s = null;	\n" +
			"			try {	\n" +
			"				return;	\n" +
			"			} catch (Exception ex) {	\n" +
			"			}	\n" +
			"			s.hashCode();	\n" +
			"		} finally {	\n" +
			"			if (this.logger == null) {	\n" +
			"				String loggerManager = null;	\n" +
			"				System.out.println(loggerManager);	\n" +
			"			}	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS\n" + 
		"null");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=16279
 * shifting of finaly scopes against try/catch ones makes the custom ret address shifting
 * unnecessary.
 */
public void test020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"	String logger;	\n" +
			"  public static void main(String[] args) {	\n" +
			"    new X().foo();	\n" +
			"	}	\n"+
			"	public void foo() {	\n" +
			"		try {	\n" +
			"			System.out.println(\"try1\");	\n" +
			"			try {	\n" +
			"				System.out.println(\"try2\");	\n" +
			"			} finally {	\n" +
			"				System.out.println(\"finally2\");	\n" +
			"			}	\n" +
			"		} catch (Exception ce) {	\n" +
			"			String s = null;	\n" +
			"			try {	\n" +
			"				return;	\n" +
			"			} catch (Exception ex) {	\n" +
			"			}	\n" +
			"			s.hashCode();	\n" +
			"		} finally {	\n" +
			"			System.out.println(\"finally1\");	\n" +
			"			try {	\n" +
			"				System.out.println(\"try3\");	\n" +
			"				if (this.logger == null) {	\n" +
			"					String loggerManager = null;	\n" +
			"				}	\n" +
			"			} finally {	\n" +
			"				System.out.println(\"finally3\");	\n" +
			"			}	\n" +
			"		}	\n" +
			"		int i1 = 0;	\n" +
			"		int i2 = 0;	\n" +
			"		int i3 = 0;	\n" +
			"		int i4 = 0;	\n" +
			"		int i5 = 0;	\n" +
			"		int i6 = 0;	\n" +
			"		int i7 = 0;	\n" +
			"		int i8 = 0;	\n" +
			"		int i9 = 0;	\n" +
			"	}	\n" +
			"}	\n"
		},
		"try1\n" + 
		"try2\n" + 
		"finally2\n" + 
		"finally1\n" + 
		"try3\n" + 
		"finally3");
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=21116
 * protected type visibility check
 */
public void test021() {
	this.runConformTest(
		new String[] {
			"pa/A.java",
			"package pa;	\n" +
			"public abstract class A {	\n" +
			"  public static void main(String[] args) {	\n" +
			"    System.out.println(\"SUCCESS\");	\n" +
			"	}	\n"+
			"	protected AIC memberA;	\n" +
			"	protected class AIC {	\n" +
			"		public void methodAIC(String parameter) {	\n" +
			"		  // ....do something	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n",
			"pb/B.java",
			"package pb;	\n" +
			"public class B extends pa.A {	\n" +
			"	private class BIC {	\n" +
			"		public void methodBIC(String param) {	\n" +
			"			memberA.methodAIC(param);	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS");
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=19916
 * nested try/synchronized statements (local var index alloc)
 */
public void test022() {
	this.runConformTest(
		new String[] {
			"pa/A.java",
			"package pa;	\n" +
			"public class A {	\n" +
			"  public static void main(String[] args) {	\n" +
			"	 new A().f();	\n" +
			"    System.out.println(\"SUCCESS\");	\n" +
			"	}	\n"+
			"	boolean b = false;	\n" +
			"	private Integer f() {	\n" +
			"		while (true) {	\n" +
			"			try {	\n" +
			"				int x = 3;	\n" +
			"				synchronized (this) {	\n" +
			"					return null;	\n" +
			"				}	\n" +
			"			} finally {	\n" +
			"				if (b)	\n" +
			"					synchronized (this) {	\n" +
			"					int y = 3;	\n" +
			"				}	\n" +
			"			}	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS");
}

public void test023() { 
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		try {\n" + 
			"			throw new BX();\n" + 
			"		} catch(BX e) {\n" + 
			"		} catch(AX e) {\n" + 
			"		}\n" + 
			"	}\n" + 
			"} \n" + 
			"class AX extends Exception {}\n" + 
			"class BX extends AX {}\n"		
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	} catch(AX e) {\n" + 
		"	        ^^\n" + 
		"Unreachable catch block for AX. Only more specific exceptions are thrown and handled by previous catch block(s).\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 10)\n" + 
		"	class AX extends Exception {}\n" + 
		"	      ^^\n" + 
		"The serializable class AX does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 11)\n" + 
		"	class BX extends AX {}\n" + 
		"	      ^^\n" + 
		"The serializable class BX does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}

 /*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21203
 * NPE in ExceptionFlowContext
 */
public void test024() {
	
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X {	\n" +
			"	public void myMethod() {	\n" +
			"	    System.out.println(\"starting\");	\n" +
			"	    try {	\n" +
			"	        if (true) throw new LookupException();	\n" +
			"	    } catch(DataException de) {	\n" +
			"	       	System.out.println(\"DataException occurred\");	\n" +
			"	    } catch(LookupException le) {	\n" +
			"	       	System.out.println(\"LookupException occurred\");	\n" +
			"	    } catch(Throwable t) {	\n" +
			"	       	System.out.println(\"Throwable occurred\");	\n" +
			"	    }	\n" +
			"	    System.out.println(\"SUCCESS\");	\n" +
			"	}	\n" +
			"}	\n" +
			"class DataException extends Throwable {	\n" +
			"} 	\n" +
			"class LookupException extends DataException {	\n" +
			"}	\n" 
		},
		"----------\n" + 
		"1. ERROR in p\\X.java (at line 9)\n" + 
		"	} catch(LookupException le) {	\n" + 
		"	        ^^^^^^^^^^^^^^^\n" + 
		"Unreachable catch block for LookupException. It is already handled by the catch block for DataException\n" + 
		"----------\n" + 
		"2. WARNING in p\\X.java (at line 17)\n" + 
		"	class DataException extends Throwable {	\n" + 
		"	      ^^^^^^^^^^^^^\n" + 
		"The serializable class DataException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"3. WARNING in p\\X.java (at line 19)\n" + 
		"	class LookupException extends DataException {	\n" + 
		"	      ^^^^^^^^^^^^^^^\n" + 
		"The serializable class LookupException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n");
}
// 60081
public void test025() {
	
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"public class X\n" + 
			"{\n" + 
			"    {\n" + 
			"        String licenseFileName = \"C:/Program Files/Jatt/bin/license.key\";\n" + 
			"        File licenseFile = new File(licenseFileName);\n" + 
			"        try {\n" + 
			"            BufferedReader licenseReader = new BufferedReader(\n" + 
			"                new FileReader(licenseFile));\n" + 
			"            StringBuffer buf = new StringBuffer();\n" + 
			"            String line = null;\n" + 
			"            while ((line = licenseReader.readLine()) != null) {\n" + 
			"                char[] chars = line.toCharArray();\n" + 
			"                for (int i = 0; i < line.length(); i++) {\n" + 
			"                    if (!Character.isSpace(line.charAt(i))) {\n" + 
			"                        buf.append(line.charAt(i));\n" + 
			"                    }\n" + 
			"                }\n" + 
			"            }\n" + 
			"            \n" + 
			"        } catch (FileNotFoundException e) {\n" + 
			"            throw new Error(\"License file not found\", e);\n" + 
			"        } catch (IOException e) {\n" + 
			"            throw new Error(\"License file cannot be read\", e);\n" + 
			"        }\n" + 
			"    }\n" + 
			"  public X()\n" + 
			"  {\n" + 
			"  }\n" + 
			"  \n" + 
			"  public X(X r) \n" + 
			"  {\n" + 
			"  }    \n" + 
			"  public static void main(String[] args) {\n" + 
			"        System.out.println(\"SUCCESS\");\n" + 
			"    }\n" + 
			"}\n"
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=89710
public void test026() {

	Map customOptions = this.getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	\n" + 
			"	static private ResourceBundle bundle = null;\n" + 
			"	static {\n" + 
			"		int i = 0;\n" + 
			"		try {\n" + 
			"			bundle = foo();\n" + 
			"		} catch(Throwable e) {\n" + 
			"			e.printStackTrace();\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	static ResourceBundle foo() {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}\n",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null); // custom requestor
	
	String expectedOutput =
		"      Local variable table:\n" + 
		"        [pc: 6, pc: 21] local: i index: 0 type: int\n" + 
		"        [pc: 16, pc: 20] local: e index: 1 type: java.lang.Throwable\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue(false);
	} catch (IOException e) {
		assertTrue(false);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=89710 - variation
public void test027() {

	Map customOptions = this.getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	\n" + 
			"	void bar(boolean b) {\n" + 
			"		if (b) {\n" + 
			"			try {\n" + 
			"				int i = 0;\n" + 
			"			} catch(Exception e) {\n" + 
			"				e.printStackTrace();\n" + 
			"			}\n" + 
			"		} else {\n" + 
			"			int j = 0;\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null); // custom requestor
	
	String expectedOutput =
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 20] local: this index: 0 type: X\n" + 
		"        [pc: 0, pc: 20] local: b index: 1 type: boolean\n" + 
		"        [pc: 6, pc: 9] local: i index: 2 type: int\n" + 
		"        [pc: 10, pc: 14] local: e index: 2 type: java.lang.Exception\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue(false);
	} catch (IOException e) {
		assertTrue(false);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98892
public void test028() {

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"\n" + 
			"    public static void main(String[] args) {\n" + 
			"    	try {\n" + 
			"	        new X().start();\n" + 
			"    	} catch(Exception e) {\n" + 
			"            System.out.println(\"SUCCESS\");\n" + 
			"    	}\n" + 
			"    }\n" + 
			"    public Object start() {\n" + 
			"        try {\n" + 
			"            return null;\n" + 
			"        } finally {\n" + 
			"            System.out.print(\"ONCE:\");\n" + 
			"            foo();\n" + 
			"        }\n" + 
			"    }\n" + 
			"\n" + 
			"    private void foo() {\n" + 
			"        throw new IllegalStateException(\"Gah!\");\n" + 
			"    }        \n" + 
			"}\n",
		},
		"ONCE:SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98892 - variation
public void test029() {

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"\n" + 
			"    public static void main(String[] args) {\n" + 
			"    	try {\n" + 
			"	        new X().start();\n" + 
			"    	} catch(Exception e) {\n" + 
			"            System.out.println(\"SUCCESS\");\n" + 
			"    	}\n" + 
			"    }\n" + 
			"    public Object start() {\n" + 
			"        try {\n" + 
			"            return null;\n" + 
			"        } finally {\n" + 
			"            System.out.print(\"ONCE:\");\n" + 
			"            foo();\n" + 
			"            return this;\n" + 
			"        }\n" + 
			"    }\n" + 
			"\n" + 
			"    private void foo() {\n" + 
			"        throw new IllegalStateException(\"Gah!\");\n" + 
			"    }        \n" + 
			"}\n",
		},
		"ONCE:SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98892 - variation
public void test030() {

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"\n" + 
			"    public static void main(String[] args) {\n" + 
			"    	try {\n" + 
			"	        new X().start();\n" + 
			"    	} catch(Exception e) {\n" + 
			"            System.out.println(\"SUCCESS\");\n" + 
			"    	}\n" + 
			"    }\n" + 
			"    public Object start() {\n" + 
			"        try {\n" + 
			"            Object o = null;\n" + 
			"            o.toString();\n" + 
			"            return null;\n" + 
			"        } catch(Exception e) {\n" + 
			"            System.out.print(\"EXCEPTION:\");\n" + 
			"			return e;        	\n" + 
			"        } finally {\n" + 
			"            System.out.print(\"ONCE:\");\n" + 
			"            foo();\n" + 
			"        }\n" + 
			"    }\n" + 
			"\n" + 
			"    private void foo() {\n" + 
			"        throw new IllegalStateException(\"Gah!\");\n" + 
			"    }        \n" + 
			"}\n",
		},
		"EXCEPTION:ONCE:SUCCESS");
}
/*
 * Try block is never reached
 */
public void test031() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"public class X {\n" +
			"	static void foo(Object o) {}\n" +
			"	\n" +
			"    public static void main(String[] args) {\n" +
			"    	try {\n" +
			"    		foo(new Object() {\n" +
			"    			public void bar() throws IOException {\n" +
			"    				bar1();\n" +
			"    			}\n" +
			"    		});\n" +
			"    	} catch(IOException e) {\n" +
			"    		e.printStackTrace();\n" +
			"    	}\n" +
			"    }\n" +
			"    \n" +
			"    static void bar1() throws IOException {}\n" +
			"}" 
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 13)\n" + 
		"	} catch(IOException e) {\n" + 
		"	        ^^^^^^^^^^^\n" + 
		"Unreachable catch block for IOException. This exception is never thrown from the try statement body\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=114855
 */
public void test032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X\n" + 
			"{\n" + 
			"  static int except_count;\n" + 
			"\n" + 
			"  static boolean test_result = true;\n" + 
			"  \n" + 
			"  static Throwable all_except[] =\n" + 
			"  {\n" + 
			"    new AbstractMethodError(),             //  0\n" + 
			"    new ArithmeticException(),             //  1\n" + 
			"    new ArrayIndexOutOfBoundsException(),  //  2\n" + 
			"    new ArrayStoreException(),             //  3\n" + 
			"    new ClassCastException(),              //  4\n" + 
			"    new ClassCircularityError(),           //  5\n" + 
			"    new ClassFormatError(),                //  6\n" + 
			"    new ClassNotFoundException(),          //  7\n" + 
			"    new CloneNotSupportedException(),      //  8\n" + 
			"    new Error(),                           //  9\n" + 
			"    new Exception(),                       // 10\n" + 
			"    new IllegalAccessError(),              // 11\n" + 
			"    new IllegalAccessException(),          // 12\n" + 
			"    new IllegalArgumentException(),        // 13\n" + 
			"    new IllegalMonitorStateException(),    // 14\n" + 
			"    new IllegalThreadStateException(),     // 15\n" + 
			"    new IncompatibleClassChangeError(),    // 16\n" + 
			"    new IndexOutOfBoundsException(),       // 17\n" + 
			"    new InstantiationError(),              // 18\n" + 
			"    new InstantiationException(),          // 19\n" + 
			"    new InternalError(),                   // 20\n" + 
			"    new InterruptedException(),            // 21\n" + 
			"    new LinkageError(),                    // 22\n" + 
			"    new NegativeArraySizeException(),      // 23\n" + 
			"    new NoClassDefFoundError(),            // 24\n" + 
			"    new NoSuchFieldError(),                // 25\n" + 
			"    new NoSuchMethodError(),               // 26\n" + 
			"    new NoSuchMethodException(),           // 27\n" + 
			"    new NullPointerException(),            // 28\n" + 
			"    new NumberFormatException(),           // 29\n" + 
			"    new OutOfMemoryError(),                // 30\n" + 
			"    new StackOverflowError(),              // 31\n" + 
			"    new RuntimeException(),                // 32\n" + 
			"    new SecurityException(),               // 33\n" + 
			"    new StringIndexOutOfBoundsException(), // 34\n" + 
			"    new ThreadDeath(),                     // 35\n" + 
			"    new UnknownError(),                    // 36\n" + 
			"    new UnsatisfiedLinkError(),            // 37\n" + 
			"    new VerifyError(),                     // 38\n" + 
			"  };\n" + 
			"\n" + 
			"  private static void check_except(int i)\n" + 
			"    throws Throwable\n" + 
			"  {\n" + 
			"    if (except_count != i)\n" + 
			"    {\n" + 
			"      System.out.println(\"Error \"+except_count+\" != \"+i+\";\");\n" + 
			"      test_result=false;\n" + 
			"    }\n" + 
			"    throw all_except[++except_count];\n" + 
			"  }\n" + 
			"\n" + 
			"  public static void main(String[] args) throws Throwable\n" + 
			"  {\n" + 
			"    try {\n" + 
			"      except_count = 0;\n" + 
			"      throw all_except[except_count];\n" + 
			"    } catch (AbstractMethodError e0) {\n" + 
			"      try {\n" + 
			"        check_except(0);\n" + 
			"      } catch (ArithmeticException e1) {\n" + 
			"        try {\n" + 
			"          check_except(1);\n" + 
			"        } catch (ArrayIndexOutOfBoundsException e2) {\n" + 
			"          try {\n" + 
			"            check_except(2);\n" + 
			"          } catch (ArrayStoreException e3) {\n" + 
			"            try {\n" + 
			"              check_except(3);\n" + 
			"            } catch (ClassCastException e4) {\n" + 
			"              try {\n" + 
			"                check_except(4);\n" + 
			"              } catch (ClassCircularityError e5) {\n" + 
			"                try {\n" + 
			"                  check_except(5);\n" + 
			"                } catch (ClassFormatError e6) {\n" + 
			"                  try {\n" + 
			"                    check_except(6);\n" + 
			"                  } catch (ClassNotFoundException e7) {\n" + 
			"                    try {\n" + 
			"                      check_except(7);\n" + 
			"                    } catch (CloneNotSupportedException e8) {\n" + 
			"                      try {\n" + 
			"                        check_except(8);\n" + 
			"                      } catch (Error e9) {\n" + 
			"                        try {\n" + 
			"                          check_except(9);\n" + 
			"                        } catch (Exception e10) {\n" + 
			"                          try {\n" + 
			"                            check_except(10);\n" + 
			"                          } catch (IllegalAccessError e11) {\n" + 
			"                            try {\n" + 
			"                              check_except(11);\n" + 
			"                            } catch (IllegalAccessException e12) {\n" + 
			"                              try {\n" + 
			"                                check_except(12);\n" + 
			"                              } catch (IllegalArgumentException e13) {\n" + 
			"                                try {\n" + 
			"                                  check_except(13);\n" + 
			"                                } catch (IllegalMonitorStateException e14) {\n" + 
			"                                  try {\n" + 
			"                                    check_except(14);\n" + 
			"                                  } catch (IllegalThreadStateException e15) {\n" + 
			"                                    try {\n" + 
			"                                      check_except(15);\n" + 
			"                                    } catch (IncompatibleClassChangeError e16) {\n" + 
			"                                      try {\n" + 
			"                                        check_except(16);\n" + 
			"                                      } catch (IndexOutOfBoundsException e17) {\n" + 
			"                                        try {\n" + 
			"                                          check_except(17);\n" + 
			"                                        } catch (InstantiationError e18) {\n" + 
			"                                          try {\n" + 
			"                                            check_except(18);\n" + 
			"                                          } catch (InstantiationException e19) {\n" + 
			"                                            try {\n" + 
			"                                              check_except(19);\n" + 
			"                                            } catch (InternalError e20) {\n" + 
			"                                              try {\n" + 
			"                                                check_except(20);\n" + 
			"                                              } catch (InterruptedException \n" + 
			"e21) {\n" + 
			"                                                try {\n" + 
			"                                                  check_except(21);\n" + 
			"                                                } catch (LinkageError e22) {\n" + 
			"                                                  try {\n" + 
			"                                                    check_except(22);\n" + 
			"                                                  } catch \n" + 
			"(NegativeArraySizeException e23) {\n" + 
			"                                                    try {\n" + 
			"                                                      check_except(23);\n" + 
			"                                                    } catch \n" + 
			"(NoClassDefFoundError e24) {\n" + 
			"                                                      try {\n" + 
			"                                                        check_except(24);\n" + 
			"                                                      } catch (NoSuchFieldError \n" + 
			"e25) {\n" + 
			"                                                        try {\n" + 
			"                                                          check_except(25);\n" + 
			"                                                        } catch \n" + 
			"(NoSuchMethodError e26) {\n" + 
			"                                                          try {\n" + 
			"                                                            check_except(26);\n" + 
			"                                                          } catch \n" + 
			"(NoSuchMethodException e27) {\n" + 
			"                                                            try {\n" + 
			"                                                              check_except(27);\n" + 
			"                                                            } catch \n" + 
			"(NullPointerException e28) {\n" + 
			"                                                              try {\n" + 
			"                                                                check_except\n" + 
			"(28);\n" + 
			"                                                              } catch \n" + 
			"(NumberFormatException e29) {\n" + 
			"                                                                try {\n" + 
			"                                                                  check_except\n" + 
			"(29);\n" + 
			"                                                                } catch \n" + 
			"(OutOfMemoryError e30) {\n" + 
			"                                                                  try {\n" + 
			"                                                                    check_except\n" + 
			"(30);\n" + 
			"                                                                  } catch \n" + 
			"(StackOverflowError e31) {\n" + 
			"                                                                    try {\n" + 
			"                                                                      \n" + 
			"check_except(31);\n" + 
			"                                                                    } catch \n" + 
			"(RuntimeException e32) {\n" + 
			"                                                                      try {\n" + 
			"                                                                        \n" + 
			"check_except(32);\n" + 
			"                                                                      } catch \n" + 
			"(SecurityException e33) {\n" + 
			"                                                                        try {\n" + 
			"                                                                          \n" + 
			"check_except(33);\n" + 
			"                                                                        } catch \n" + 
			"(StringIndexOutOfBoundsException e34) {\n" + 
			"                                                                          try {\n" + 
			"                                                                            \n" + 
			"check_except(34);\n" + 
			"                                                                          } \n" + 
			"catch (ThreadDeath e35) {\n" + 
			"                                                                            try \n" + 
			"{\n" + 
			"                                                                              \n" + 
			"check_except(35);\n" + 
			"                                                                            } \n" + 
			"catch (UnknownError e36) {\n" + 
			"                                                                              \n" + 
			"try {\n" + 
			"                                                                                \n" + 
			"check_except(36);\n" + 
			"                                                                              } \n" + 
			"catch (UnsatisfiedLinkError e37) {\n" + 
			"                                                                                \n" + 
			"try {\n" + 
			"                                                                                \n" + 
			"  check_except(37);\n" + 
			"                                                                                \n" + 
			"} catch (VerifyError e38) {\n" + 
			"                                                                                \n" + 
			"  ++except_count;\n" + 
			"                                                                                \n" + 
			"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}\n" + 
			"    System.out.print(test_result & (except_count == all_except.length));\n" + 
			"  }\n" + 
			"}",
		},
		"true");
}
public void test033() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	interface IActionSetContributionItem {\n" + 
				"		String getActionSetId();\n" + 
				"	}\n" + 
				"	public interface IAction {\n" + 
				"	}\n" + 
				"	interface IContributionItem {\n" + 
				"		String getId();\n" + 
				"		boolean isSeparator();\n" + 
				"		boolean isGroupMarker();\n" + 
				"	}\n" + 
				"    public static void findInsertionPoint(String startId,\n" + 
				"            String sortId, IContributionItem[] items) {\n" + 
				"        // Find the reference item.\n" + 
				"        try {\n" + 
				"	        int insertIndex = 0;\n" + 
				"	        while (insertIndex < items.length) {\n" + 
				"	            if (startId.equals(items[insertIndex].getId()))\n" + 
				"	                break;\n" + 
				"	            ++insertIndex;\n" + 
				"	        }\n" + 
				"	        if (insertIndex >= items.length)\n" + 
				"	            return;\n" + 
				"	\n" + 
				"	        int compareMetric = 0;\n" + 
				"	\n" + 
				"	        // Find the insertion point for the new item.\n" + 
				"	        // We do this by iterating through all of the previous\n" + 
				"	        // action set contributions define within the current group.\n" + 
				"	        for (int nX = insertIndex + 1; nX < items.length; nX++) {\n" + 
				"	            IContributionItem item = items[nX];\n" + 
				"	            if (item.isSeparator() || item.isGroupMarker()) {\n" + 
				"	                // Fix for bug report 18357\n" + 
				"	                break;\n" + 
				"	            }\n" + 
				"	            if (item instanceof IActionSetContributionItem) {\n" + 
				"	                if (sortId != null) {\n" + 
				"	                    String testId = ((IActionSetContributionItem) item)\n" + 
				"	                            .getActionSetId();\n" + 
				"	                    if (sortId.compareTo(testId) < compareMetric)\n" + 
				"	                        break;\n" + 
				"	                }\n" + 
				"	                insertIndex = nX;\n" + 
				"	            } else {\n" + 
				"	                break;\n" + 
				"	            }\n" + 
				"	        }\n" + 
				"	    } catch(Exception e) {}\n" + 
				"    }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"		findInsertionPoint(\"\", \"\", null);\n" + 
				"	}\n" + 
				"}",
			},
			"");
	String expectedOutput =
		"  public static void findInsertionPoint(java.lang.String startId, java.lang.String sortId, X.IContributionItem[] items);\n" + 
		"      0  iconst_0\n" + 
		"      1  istore_3 [insertIndex]\n" + 
		"      2  goto 26\n" + 
		"      5  aload_0 [startId]\n" + 
		"      6  aload_2 [items]\n" + 
		"      7  iload_3 [insertIndex]\n" + 
		"      8  aaload\n" + 
		"      9  invokeinterface X$IContributionItem.getId() : java.lang.String [16] [nargs: 1]\n" + 
		"     14  invokevirtual java.lang.String.equals(java.lang.Object) : boolean [22]\n" + 
		"     17  ifeq 23\n" + 
		"     20  goto 32\n" + 
		"     23  iinc 3 1 [insertIndex]\n" + 
		"     26  iload_3 [insertIndex]\n" + 
		"     27  aload_2 [items]\n" + 
		"     28  arraylength\n" + 
		"     29  if_icmplt 5\n" + 
		"     32  iload_3 [insertIndex]\n" + 
		"     33  aload_2 [items]\n" + 
		"     34  arraylength\n" + 
		"     35  if_icmplt 39\n" + 
		"     38  return\n" + 
		"     39  iconst_0\n" + 
		"     40  istore 4 [compareMetric]\n" + 
		"     42  iload_3 [insertIndex]\n" + 
		"     43  iconst_1\n" + 
		"     44  iadd\n" + 
		"     45  istore 5 [nX]\n" + 
		"     47  goto 129\n" + 
		"     50  aload_2 [items]\n" + 
		"     51  iload 5 [nX]\n" + 
		"     53  aaload\n" + 
		"     54  astore 6 [item]\n" + 
		"     56  aload 6 [item]\n" + 
		"     58  invokeinterface X$IContributionItem.isSeparator() : boolean [28] [nargs: 1]\n" + 
		"     63  ifne 140\n" + 
		"     66  aload 6 [item]\n" + 
		"     68  invokeinterface X$IContributionItem.isGroupMarker() : boolean [32] [nargs: 1]\n" + 
		"     73  ifeq 79\n" + 
		"     76  goto 140\n" + 
		"     79  aload 6 [item]\n" + 
		"     81  instanceof X$IActionSetContributionItem [35]\n" + 
		"     84  ifeq 140\n" + 
		"     87  aload_1 [sortId]\n" + 
		"     88  ifnull 117\n" + 
		"     91  aload 6 [item]\n" + 
		"     93  checkcast X$IActionSetContributionItem [35]\n" + 
		"     96  invokeinterface X$IActionSetContributionItem.getActionSetId() : java.lang.String [37] [nargs: 1]\n" + 
		"    101  astore 7 [testId]\n" + 
		"    103  aload_1 [sortId]\n" + 
		"    104  aload 7 [testId]\n" + 
		"    106  invokevirtual java.lang.String.compareTo(java.lang.String) : int [40]\n" + 
		"    109  iload 4 [compareMetric]\n" + 
		"    111  if_icmpge 117\n" + 
		"    114  goto 140\n" + 
		"    117  iload 5 [nX]\n" + 
		"    119  istore_3 [insertIndex]\n" + 
		"    120  goto 126\n" + 
		"    123  goto 140\n" + 
		"    126  iinc 5 1 [nX]\n" + 
		"    129  iload 5 [nX]\n" + 
		"    131  aload_2 [items]\n" + 
		"    132  arraylength\n" + 
		"    133  if_icmplt 50\n" + 
		"    136  goto 140\n" + 
		"    139  astore_3\n" + 
		"    140  return\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue(false);
	} catch (IOException e) {
		assertTrue(false);
	}	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853
public void test034() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		try {\n" + 
				"			scenario();\n" + 
				"		} catch(Exception e){\n" + 
				"			System.out.println(\"[end]\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"\n" + 
				"	private static int scenario(){\n" + 
				"		try {\n" + 
				"			int i = 1;\n" + 
				"			System.out.print(\"[i: \" + i+\"]\");\n" + 
				"			if (i > 5) {\n" + 
				"				return i;\n" + 
				"			}\n" + 
				"			return -i;\n" + 
				"		} catch (Exception e) {\n" + 
				"			System.out.print(\"[WRONG CATCH]\");\n" + 
				"			return 2;\n" + 
				"		} finally {\n" + 
				"			System.out.print(\"[finally]\");\n" + 
				"			try {\n" + 
				"				throwRuntime();\n" + 
				"			} finally {\n" + 
				"				clean();\n" + 
				"			}\n" + 
				"		}\n" + 
				"	}\n" + 
				"\n" + 
				"	private static void throwRuntime() {\n" + 
				"		throw new RuntimeException(\"error\");\n" + 
				"	}\n" + 
				"\n" + 
				"	private static void clean() {\n" + 
				"		System.out.print(\"[clean]\");\n" + 
				"	}\n" + 
				"}\n",
			},
			"[i: 1][finally][clean][end]");
	
	String expectedOutput = new CompilerOptions(this.getCompilerOptions()).inlineJsrBytecode
		?	"  // Method descriptor #19 ()I\n" + 
			"  // Stack: 4, Locals: 4\n" + 
			"  private static int scenario();\n" + 
			"      0  iconst_1\n" + 
			"      1  istore_0 [i]\n" + 
			"      2  getstatic java.lang.System.out : java.io.PrintStream [20]\n" + 
			"      5  new java.lang.StringBuilder [40]\n" + 
			"      8  dup\n" + 
			"      9  ldc <String \"[i: \"> [42]\n" + 
			"     11  invokespecial java.lang.StringBuilder(java.lang.String) [44]\n" + 
			"     14  iload_0 [i]\n" + 
			"     15  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [46]\n" + 
			"     18  ldc <String \"]\"> [50]\n" + 
			"     20  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [52]\n" + 
			"     23  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [55]\n" + 
			"     26  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" + 
			"     29  iload_0 [i]\n" + 
			"     30  iconst_5\n" + 
			"     31  if_icmple 61\n" + 
			"     34  iload_0 [i]\n" + 
			"     35  istore_2\n" + 
			"     36  getstatic java.lang.System.out : java.io.PrintStream [20]\n" + 
			"     39  ldc <String \"[finally]\"> [62]\n" + 
			"     41  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" + 
			"     44  invokestatic X.throwRuntime() : void [64]\n" + 
			"     47  goto 56\n" + 
			"     50  astore_3\n" + 
			"     51  invokestatic X.clean() : void [67]\n" + 
			"     54  aload_3\n" + 
			"     55  athrow\n" + 
			"     56  invokestatic X.clean() : void [67]\n" + 
			"     59  iload_2\n" + 
			"     60  ireturn\n" + 
			"     61  iload_0 [i]\n" + 
			"     62  ineg\n" + 
			"     63  istore_2\n" + 
			"     64  getstatic java.lang.System.out : java.io.PrintStream [20]\n" + 
			"     67  ldc <String \"[finally]\"> [62]\n" + 
			"     69  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" + 
			"     72  invokestatic X.throwRuntime() : void [64]\n" + 
			"     75  goto 84\n" + 
			"     78  astore_3\n" + 
			"     79  invokestatic X.clean() : void [67]\n" + 
			"     82  aload_3\n" + 
			"     83  athrow\n" + 
			"     84  invokestatic X.clean() : void [67]\n" + 
			"     87  iload_2\n" + 
			"     88  ireturn\n" + 
			"     89  astore_0 [e]\n" + 
			"     90  getstatic java.lang.System.out : java.io.PrintStream [20]\n" + 
			"     93  ldc <String \"[WRONG CATCH]\"> [70]\n" + 
			"     95  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" + 
			"     98  getstatic java.lang.System.out : java.io.PrintStream [20]\n" + 
			"    101  ldc <String \"[finally]\"> [62]\n" + 
			"    103  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" + 
			"    106  invokestatic X.throwRuntime() : void [64]\n" + 
			"    109  goto 118\n" + 
			"    112  astore_3\n" + 
			"    113  invokestatic X.clean() : void [67]\n" + 
			"    116  aload_3\n" + 
			"    117  athrow\n" + 
			"    118  invokestatic X.clean() : void [67]\n" + 
			"    121  iconst_2\n" + 
			"    122  ireturn\n" + 
			"    123  astore_1\n" + 
			"    124  getstatic java.lang.System.out : java.io.PrintStream [20]\n" + 
			"    127  ldc <String \"[finally]\"> [62]\n" + 
			"    129  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" + 
			"    132  invokestatic X.throwRuntime() : void [64]\n" + 
			"    135  goto 144\n" + 
			"    138  astore_3\n" + 
			"    139  invokestatic X.clean() : void [67]\n" + 
			"    142  aload_3\n" + 
			"    143  athrow\n" + 
			"    144  invokestatic X.clean() : void [67]\n" + 
			"    147  aload_1\n" + 
			"    148  athrow\n" + 
			"      Exception Table:\n" + 
			"        [pc: 44, pc: 50] -> 50 when : any\n" + 
			"        [pc: 72, pc: 78] -> 78 when : any\n" + 
			"        [pc: 0, pc: 36] -> 89 when : java.lang.Exception\n" + 
			"        [pc: 59, pc: 64] -> 89 when : java.lang.Exception\n" + 
			"        [pc: 87, pc: 89] -> 89 when : java.lang.Exception\n" + 
			"        [pc: 106, pc: 112] -> 112 when : any\n" + 
			"        [pc: 0, pc: 36] -> 123 when : any\n" + 
			"        [pc: 61, pc: 64] -> 123 when : any\n" + 
			"        [pc: 89, pc: 98] -> 123 when : any\n" + 
			"        [pc: 132, pc: 138] -> 138 when : any\n"
	: 		"  // Method descriptor #19 ()I\n" + 
			"  // Stack: 4, Locals: 6\n" + 
			"  private static int scenario();\n" + 
			"      0  iconst_1\n" + 
			"      1  istore_0 [i]\n" + 
			"      2  getstatic java.lang.System.out : java.io.PrintStream [20]\n" + 
			"      5  new java.lang.StringBuffer [40]\n" + 
			"      8  dup\n" + 
			"      9  ldc <String \"[i: \"> [42]\n" + 
			"     11  invokespecial java.lang.StringBuffer(java.lang.String) [44]\n" + 
			"     14  iload_0 [i]\n" + 
			"     15  invokevirtual java.lang.StringBuffer.append(int) : java.lang.StringBuffer [46]\n" + 
			"     18  ldc <String \"]\"> [50]\n" + 
			"     20  invokevirtual java.lang.StringBuffer.append(java.lang.String) : java.lang.StringBuffer [52]\n" + 
			"     23  invokevirtual java.lang.StringBuffer.toString() : java.lang.String [55]\n" + 
			"     26  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" + 
			"     29  iload_0 [i]\n" + 
			"     30  iconst_5\n" + 
			"     31  if_icmple 41\n" + 
			"     34  iload_0 [i]\n" + 
			"     35  istore_3\n" + 
			"     36  jsr 69\n" + 
			"     39  iload_3\n" + 
			"     40  ireturn\n" + 
			"     41  iload_0 [i]\n" + 
			"     42  ineg\n" + 
			"     43  istore_3\n" + 
			"     44  jsr 69\n" + 
			"     47  iload_3\n" + 
			"     48  ireturn\n" + 
			"     49  astore_0 [e]\n" + 
			"     50  getstatic java.lang.System.out : java.io.PrintStream [20]\n" + 
			"     53  ldc <String \"[WRONG CATCH]\"> [62]\n" + 
			"     55  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" + 
			"     58  jsr 69\n" + 
			"     61  iconst_2\n" + 
			"     62  ireturn\n" + 
			"     63  astore_2\n" + 
			"     64  jsr 69\n" + 
			"     67  aload_2\n" + 
			"     68  athrow\n" + 
			"     69  astore_1\n" + 
			"     70  getstatic java.lang.System.out : java.io.PrintStream [20]\n" + 
			"     73  ldc <String \"[finally]\"> [64]\n" + 
			"     75  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" + 
			"     78  invokestatic X.throwRuntime() : void [66]\n" + 
			"     81  goto 99\n" + 
			"     84  astore 5\n" + 
			"     86  jsr 92\n" + 
			"     89  aload 5\n" + 
			"     91  athrow\n" + 
			"     92  astore 4\n" + 
			"     94  invokestatic X.clean() : void [69]\n" + 
			"     97  ret 4\n" + 
			"     99  jsr 92\n" + 
			"    102  ret 1\n" + 
			"      Exception Table:\n" + 
			"        [pc: 0, pc: 49] -> 49 when : java.lang.Exception\n" + 
			"        [pc: 0, pc: 39] -> 63 when : any\n" + 
			"        [pc: 41, pc: 47] -> 63 when : any\n" + 
			"        [pc: 49, pc: 61] -> 63 when : any\n" + 
			"        [pc: 78, pc: 84] -> 84 when : any\n" + 
			"        [pc: 99, pc: 102] -> 84 when : any\n";

	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue(false);
	} catch (IOException e) {
		assertTrue(false);
	}	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853 - variation
public void test035() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		try {\n" + 
				"			new X().bar();\n" + 
				"		} catch(Exception e){\n" + 
				"			System.out.println(\"[end]\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"	Object bar() {\n" + 
				"		try {\n" + 
				"			System.out.print(\"[try]\");\n" + 
				"			return this;\n" + 
				"		} catch(Exception e){\n" + 
				"			System.out.print(\"[WRONG CATCH]\");\n" + 
				"		} finally {\n" + 
				"			System.out.print(\"[finally]\");\n" + 
				"			foo();\n" + 
				"		}\n" + 
				"		return this;\n" + 
				"	}\n" + 
				"	Object foo() {\n" + 
				"		throw new RuntimeException();\n" + 
				"	}\n" + 
				"}\n",
			},
			"[try][finally][end]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853 - variation
public void test036() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		try {\n" + 
				"			new X().bar();\n" + 
				"		} catch(Exception e){\n" + 
				"			System.out.println(\"[end]\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"	Object bar() {\n" + 
				"		try {\n" + 
				"			System.out.print(\"[try]\");\n" + 
				"			throw new RuntimeException();\n" + 
				"		} catch(Exception e){\n" + 
				"			System.out.print(\"[catch]\");\n" + 
				"			return this;\n" + 
				"		} finally {\n" + 
				"			System.out.print(\"[finally]\");\n" + 
				"			foo();\n" + 
				"		}\n" + 
				"	}\n" + 
				"	Object foo() {\n" + 
				"		throw new RuntimeException();\n" + 
				"	}\n" + 
				"}\n",
			},
			"[try][catch][finally][end]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853 - variation
public void test037() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		try {\n" + 
				"			scenario();\n" + 
				"		} catch(Exception e){\n" + 
				"			System.out.println(\"[end]\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"\n" + 
				"	private static void scenario() throws Exception {\n" + 
				"		try {\n" + 
				"			System.out.print(\"[try1]\");\n" + 
				"			try {\n" + 
				"				System.out.print(\"[try2]\");\n" + 
				"				return;\n" + 
				"			} catch(Exception e) {\n" + 
				"				System.out.print(\"[catch2]\");\n" + 
				"			} finally {\n" + 
				"				System.out.print(\"[finally2]\");\n" + 
				"				throwRuntime();\n" + 
				"			}\n" + 
				"		} catch(Exception e) {\n" + 
				"			System.out.print(\"[catch1]\");\n" + 
				"			throw e;\n" +
				"		} finally {\n" + 
				"			System.out.print(\"[finally1]\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"\n" + 
				"	private static void throwRuntime() {\n" + 
				"		throw new RuntimeException(\"error\");\n" + 
				"	}\n" + 
				"}\n",
			},
			"[try1][try2][finally2][catch1][finally1][end]");
}
public static Class testClass() {
	return TryStatementTest.class;
}
}
