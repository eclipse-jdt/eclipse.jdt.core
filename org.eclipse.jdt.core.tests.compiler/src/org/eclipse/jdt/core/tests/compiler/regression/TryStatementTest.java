/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *								bug 387612 - Unreachable catch block...exception is never thrown from the try
 *     Jesper Steen Moller - Contribution for
 *								bug 404146 - [1.7][compiler] nested try-catch-finally-blocks leads to unrunnable Java byte code
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TryStatementTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testBug387612" };
//	TESTS_NUMBERS = new int[] { 74, 75 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryStatementTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
@Override
protected Map getCompilerOptions() {
	Map compilerOptions = super.getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ShareCommonFinallyBlocks, CompilerOptions.ENABLED);
	return compilerOptions;
}
public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  public class X1 {
			    public X1() throws Exception {
			    }
			  }
			  public void method1(){
			    try {
			      new X1() {
			      };
			    } catch(Exception e){
			    }
			  }
			}
			""",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.io.*;
			import java.util.zip.*;
			class X {
			  void bar() throws ZipException, IOException {}
			  void foo() {
			    try {
			      bar();
			    } catch (ZipException e) {
			    } catch (IOException e) {
			    }
			  }
			}
			""",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  public class A1 {
			    public A1() throws Exception {
			    }
			  }
			  public void method1(){
			    try {
			      new A1() {
			      };
			    } catch(Exception e){
			    }
			  }
			}
			""",
	});
}

public void test004() {
	this.runConformTest(new String[] {
		"p/ATC.java",
		"""
			package p;
			public class ATC {
			   \s
			    public class B extends Exception {
			      public B(String msg) { super(msg); }
			    }
			   \s
			    void foo() throws ATC.B {
			      Object hello$1 = null;
			      try {
			        throw new B("Inside foo()");
			      } catch(B e) {
			        System.out.println("Caught B");
			      }   \s
			    }      \s
			}
			""",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/A.java",
		"""
			package p;
			import java.io.IOException;
			import java.util.Vector;
			/**
			 * This test0 should run without producing a java.lang.ClassFormatError
			 */
			public class A {
			  public Vector getComponents () {
			    try{
			      throw new IOException();
			    }
			    catch (IOException ioe) {
			    }
			    return null;
			  }
			  public static void main(String[] args) {
			    new A().getComponents();
			  }
			}
			""",
	});
}

public void test006() {
	this.runConformTest(new String[] {
		"p/T.java",
		"""
			package p;
			import java.lang.reflect.*;
			public class T extends InvocationTargetException {
			  public static void main(String[] args) {
			    T ct = new T();
			    ct.getTargetException();
			  }
			  public Throwable getTargetException() {
			    Runnable runnable = new Runnable() {
			      public void run() {
			        System.out.println("we got here");
			        T.super.getTargetException();
			      }
			    };
			    runnable.run();
			    return new Throwable();
			  }
			}
			""",
	});
}
public void test007() {
	this.runConformTest(new String[] {
		"TryFinally.java",
		"""
			class TryFinally {\t
				public int readFile(String filename) throws Exception {\t
					int interfaceID = -1;\t
					int iNdx = 0;\t
					try {\t
						try {\t
							return interfaceID;\t
						} // end try\t
						finally {\t
							iNdx = 1;\t
						} // end finally\t
					} // end try\t
					catch (Exception ex) {\t
						throw new Exception("general exception " + ex.getMessage() + " on processing file " + filename);\t
					} // end catch\t
					finally {\t
					} // end finally\t
				} // end readFile method\t
			}\t
			"""
});
}
/*
 * 1FZR1TO: IVJCOM:WIN - Class does not compile in VAJava 3.02-Java2
 */
public void test008() {
	this.runConformTest(
		new String[] {
			"RedundantException.java",
			"""
				import java.io.*;
				public class RedundantException {
					/**
					     * Runs the class as an application.
					     */
					public static void main(String[] args) {
						RedundantException re = new RedundantException();
						re.catchIt();
						System.out.println("SUCCESS");
					}
					/**
					     * Defines a method that lists an exception twice.
					     * This can be buried in a much longer list.
					     */
					void throwIt() throws IOException, IOException {
						throw new IOException();
					}
					/**
					     * Catches the redundantly defined exception.
					     */
					void catchIt() {
						try {
							throwIt(); // compile error here
						} catch (IOException e) {
							System.out.println("Caught.");
						}
					}
				}"""
		},
		"Caught.\n" +
		"SUCCESS");
}
public void test009() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				public void save() {
					int a = 3;
					try {
						Object warnings = null;
				      	try {
				         	Object contexts = null;
				         	try {
				            	System.out.println(warnings);
							 	return;
				      	 	} catch (NullPointerException npe) {
								System.out.println(contexts);
				               return;
				       	}
						} catch (Exception e) {
				 			return;
				   	}
					} finally {
				     	int b = 4;
				       System.out.println("#save -> " + b + a);
				    }
				}
				public static void main(String[] args) {
					new Test().save();
				}
				}"""
		},
		"null\n" +
		"#save -> 43");
}
public void test010() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				public void save() {
					int a = 3;
					try {
						Object warnings = null;
				      	try {
				         	Object contexts = null;
				         	try {
				            	System.out.println(warnings);
							 	return;
				      	 	} catch (NullPointerException npe) {
								System.out.println(contexts);
				               return;
				       	}
						} catch (Exception e) {
				 			return;
				   	}
					} catch(Exception e){
						Object dummy1 = null;
						System.out.println(dummy1);
						Object dummy2 = null;
						System.out.println(dummy2);
						return;
					} finally {
				     	int b = 4;
				       System.out.println("#save -> " + b + a);
				    }
				}
				public static void main(String[] args) {
					new Test().save();
				}
				}"""
		},
		"null\n" +
		"#save -> 43");
}

public void test011() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				public void save() {
					int a = 3;
					try {
						Object warnings = null;
				      	try {
				         	Object contexts = null;
				         	try {
				            	System.out.println(warnings);
							 	return;
				      	 	} catch (NullPointerException npe) {
								System.out.println(contexts);
				               return;
				       	}
						} catch (Exception e) {
				 			return;
				   	}
					} catch(Exception e){
						int dummy1 = 11;
						System.out.println(dummy1);
						int dummy2 = 12;
						System.out.println(dummy2);
						return;
					} finally {
				     	int b = 4;
				       System.out.println("#save -> " + b + a);
				    }
				}
				public static void main(String[] args) {
					new Test().save();
				}
				}"""
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
			"""
				import java.io.*;
				public class X {
					public static void main(String[] args) {
						try {
							new X().delete(args);
							System.out.println("success");
						} catch (Exception e) {
						}
					}
					void bar(int i) {
					}
					public Object delete(String[] resources) throws IOException {
						try {
							int totalWork = 3;
							Object result = "aaa";
							try {
								return result;
							} catch (Exception e) {
								throw new IOException();
							} finally {
								bar(totalWork);
							}
						} finally {
							bar(0);
						}
					}
				}
				"""
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
			"""
				import java.io.*;
				public class X {
					public static void main(String[] args) {
						try {
							new X().delete(args);
							System.out.println("success");
						} catch (Exception e) {
						}
					}
					void bar(int i) {
					}
					public Object delete(String[] resources) throws IOException {
						try {
							int totalWork = 3;
							Object result = "aaa";
							try {
								return result;
							} catch (Exception e) {
								throw new IOException();
							} finally {
								bar(totalWork);
							}
						} finally {
							int totalWork = 4;
							bar(totalWork);
						}
					}
				}
				"""
		},
		"success");
}
public void test014() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				public void save() {
					int a = 3;
					try {
						Object warnings = null;
				      	try {
				         	int contexts = 17;
				         	try {
								Object dummy = null;
				            	System.out.println(warnings);
				            	System.out.println(dummy);
							 	return;
				      	 	} catch (NullPointerException npe) {
								System.out.println(contexts);
				               return;
				       	}
						} catch (Exception e) {
				 			return;
				   	} finally {\s
							int c = 34;\s
							System.out.println("#inner-finally ->" + a + c);
				       }
					} finally {
				     	int b = 4;
				       System.out.println("#save -> " + b + a);
				    }
				}
				public static void main(String[] args) {
					new Test().save();
				}
				}"""
		},
		"""
			null
			null
			#inner-finally ->334
			#save -> 43""");
}

public void test015() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				import java.io.IOException;\t
				public class X {\t
					public static void main(String args[]) {\t
						try { \t
							new Object(){\t
								{\t
									if (true) throw new IOException();\t
									if (true) throw new Exception();\t
								}\t
							};\t
							System.out.println("FAILED");\t
						} catch(Exception e){\t
							System.out.println("SUCCESS");\t
						}\t
					}\t
				}\t
				""",
		},
		"SUCCESS");
}
public void test016() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				import java.io.IOException;\t
				public class X {\t
					public static void main(String args[]) {\t
						class SomeClass {\t
							SomeClass () throws IOException {\t
							}\t
						}\t
						try { \t
							new Object(){\t
								{\t
									if (true) throw new IOException();\t
									if (true) throw new Exception();\t
								}\t
							};\t
							System.out.println("FAILED");\t
						} catch(Exception e){\t
							System.out.println("SUCCESS");\t
						}\t
					}\t
				}\t
				""",
		},
		"SUCCESS");
}
public void test017() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					public static void main(String args[]) {\t
						try { \t
							new Object(){\t
								{\t
									foo();\t
								}\t
							};\t
							System.out.println("FAILED");\t
						} catch(Exception e){\t
							System.out.println("SUCCESS");\t
						}\t
					}\t
					static class AEx extends Exception {}\s
					static class BEx extends Exception {}\s
					static void foo() throws AEx, BEx {\t
						throw new AEx();\t
					}\t
				}\t
				""",
		},
		"SUCCESS");
}

// 8773 verification error
public void test018() {
	this.runConformTest(
		new String[] {
			"VerifyEr.java",
			"""
				public class VerifyEr {\t
				  protected boolean err(boolean b) {\t
				     try {\t
				          System.out.print("SUCC");\t
				     } catch (Throwable t) {\t
				          return b;\t
				     } finally {\t
				          try {\t
				               if (b) {\t
				                    return b;\t
				               }\t
				          } finally {\t
				          		System.out.println("ESS");\t
				          }\t
				     }\t
				     return false;\t
				  }\t
				  public static void main(String[] args) {\t
				     new VerifyEr().err(false);\t
				  }\t
				}\t
				""",
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
			"""
				public class X {\t
					String logger;\t
				  public static void main(String[] args) {\t
				    new X().foo();\t
					}\t
					public void foo() {\t
						try {\t
							System.out.println("SUCCESS");\t
						} catch (Exception ce) {\t
							String s = null;\t
							try {\t
								return;\t
							} catch (Exception ex) {\t
							}\t
							s.hashCode();\t
						} finally {\t
							if (this.logger == null) {\t
								String loggerManager = null;\t
								System.out.println(loggerManager);\t
							}\t
						}\t
					}\t
				}\t
				"""
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
			"""
				public class X {\t
					String logger;\t
				  public static void main(String[] args) {\t
				    new X().foo();\t
					}\t
					public void foo() {\t
						try {\t
							System.out.println("try1");\t
							try {\t
								System.out.println("try2");\t
							} finally {\t
								System.out.println("finally2");\t
							}\t
						} catch (Exception ce) {\t
							String s = null;\t
							try {\t
								return;\t
							} catch (Exception ex) {\t
							}\t
							s.hashCode();\t
						} finally {\t
							System.out.println("finally1");\t
							try {\t
								System.out.println("try3");\t
								if (this.logger == null) {\t
									String loggerManager = null;\t
								}\t
							} finally {\t
								System.out.println("finally3");\t
							}\t
						}\t
						int i1 = 0;\t
						int i2 = 0;\t
						int i3 = 0;\t
						int i4 = 0;\t
						int i5 = 0;\t
						int i6 = 0;\t
						int i7 = 0;\t
						int i8 = 0;\t
						int i9 = 0;\t
					}\t
				}\t
				"""
		},
		"""
			try1
			try2
			finally2
			finally1
			try3
			finally3""");
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=21116
 * protected type visibility check
 */
public void test021() {
	this.runConformTest(
		new String[] {
			"pa/A.java",
			"""
				package pa;\t
				public abstract class A {\t
				  public static void main(String[] args) {\t
				    System.out.println("SUCCESS");\t
					}\t
					protected AIC memberA;\t
					protected class AIC {\t
						public void methodAIC(String parameter) {\t
						  // ....do something\t
						}\t
					}\t
				}\t
				""",
			"pb/B.java",
			"""
				package pb;\t
				public class B extends pa.A {\t
					private class BIC {\t
						public void methodBIC(String param) {\t
							memberA.methodAIC(param);\t
						}\t
					}\t
				}\t
				"""
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
			"""
				package pa;\t
				public class A {\t
				  public static void main(String[] args) {\t
					 new A().f();\t
				    System.out.println("SUCCESS");\t
					}\t
					boolean b = false;\t
					private Integer f() {\t
						while (true) {\t
							try {\t
								int x = 3;\t
								synchronized (this) {\t
									return null;\t
								}\t
							} finally {\t
								if (b)\t
									synchronized (this) {\t
									int y = 3;\t
								}\t
							}\t
						}\t
					}\t
				}\t
				"""
		},
		"SUCCESS");
}

public void test023() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new BX();
						} catch(BX e) {
						} catch(AX e) {
						}
					}
				}\s
				class AX extends Exception {}
				class BX extends AX {}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	} catch(AX e) {\n" +
		"	        ^^\n" +
		"Unreachable catch block for AX. Only more specific exceptions are thrown and they are handled by previous catch block(s).\n" +
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
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

 /*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21203
 * NPE in ExceptionFlowContext
 */
public void test024() {

	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;\t
				public class X {\t
					public void myMethod() {\t
					    System.out.println("starting");\t
					    try {\t
					        if (true) throw new LookupException();\t
					    } catch(DataException de) {\t
					       	System.out.println("DataException occurred");\t
					    } catch(LookupException le) {\t
					       	System.out.println("LookupException occurred");\t
					    } catch(Throwable t) {\t
					       	System.out.println("Throwable occurred");\t
					    }\t
					    System.out.println("SUCCESS");\t
					}\t
				}\t
				class DataException extends Throwable {\t
				} \t
				class LookupException extends DataException {\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 9)
				} catch(LookupException le) {\t
				        ^^^^^^^^^^^^^^^
			Unreachable catch block for LookupException. It is already handled by the catch block for DataException
			----------
			2. WARNING in p\\X.java (at line 17)
				class DataException extends Throwable {\t
				      ^^^^^^^^^^^^^
			The serializable class DataException does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in p\\X.java (at line 19)
				class LookupException extends DataException {\t
				      ^^^^^^^^^^^^^^^
			The serializable class LookupException does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// 60081
public void test025() {

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X
				{
				    {
				        String licenseFileName = "C:/Program Files/Jatt/bin/license.key";
				        File licenseFile = new File(licenseFileName);
				        try {
				            BufferedReader licenseReader = new BufferedReader(
				                new FileReader(licenseFile));
				            StringBuffer buf = new StringBuffer();
				            String line = null;
				            while ((line = licenseReader.readLine()) != null) {
				                char[] chars = line.toCharArray();
				                for (int i = 0; i < line.length(); i++) {
				                    if (!Character.isSpace(line.charAt(i))) {
				                        buf.append(line.charAt(i));
				                    }
				                }
				            }
				           \s
				        } catch (FileNotFoundException e) {
				            throw new Error("License file not found", e);
				        } catch (IOException e) {
				            throw new Error("License file cannot be read", e);
				        }
				    }
				  public X()
				  {
				  }
				 \s
				  public X(X r)\s
				  {
				  }   \s
				  public static void main(String[] args) {
				        System.out.println("SUCCESS");
				    }
				}
				"""
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=89710
public void test026() throws Exception {

	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				
				public class X {
				\t
					static private ResourceBundle bundle = null;
					static {
						int i = 0;
						try {
							bundle = foo();
						} catch(Throwable e) {
							e.printStackTrace();
						}
					}
				
					static ResourceBundle foo() {
						return null;
					}
				}
				""",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null); // custom requestor

	String expectedOutput =
		"""
		      Local variable table:
		        [pc: 6, pc: 20] local: i index: 0 type: int
		        [pc: 16, pc: 20] local: e index: 1 type: java.lang.Throwable
		""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=89710 - variation
public void test027() throws Exception {

	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				
				public class X {
				\t
					void bar(boolean b) {
						if (b) {
							try {
								int i = 0;
							} catch(Exception e) {
								e.printStackTrace();
							}
						} else {
							int j = 0;
						}
					}
				}
				""",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null); // custom requestor

	String expectedOutput =
		"""
		      Local variable table:
		        [pc: 0, pc: 20] local: this index: 0 type: X
		        [pc: 0, pc: 20] local: b index: 1 type: boolean
		        [pc: 10, pc: 14] local: e index: 2 type: java.lang.Exception
		""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98892
public void test028() {

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				    public static void main(String[] args) {
				    	try {
					        new X().start();
				    	} catch(Exception e) {
				            System.out.println("SUCCESS");
				    	}
				    }
				    public Object start() {
				        try {
				            return null;
				        } finally {
				            System.out.print("ONCE:");
				            foo();
				        }
				    }
				
				    private void foo() {
				        throw new IllegalStateException("Gah!");
				    }       \s
				}
				""",
		},
		"ONCE:SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98892 - variation
public void test029() {

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				    public static void main(String[] args) {
				    	try {
					        new X().start();
				    	} catch(Exception e) {
				            System.out.println("SUCCESS");
				    	}
				    }
				    public Object start() {
				        try {
				            return null;
				        } finally {
				            System.out.print("ONCE:");
				            foo();
				            return this;
				        }
				    }
				
				    private void foo() {
				        throw new IllegalStateException("Gah!");
				    }       \s
				}
				""",
		},
		"ONCE:SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98892 - variation
public void test030() {

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				    public static void main(String[] args) {
				    	try {
					        new X().start();
				    	} catch(Exception e) {
				            System.out.println("SUCCESS");
				    	}
				    }
				    public Object start() {
				        try {
				            Object o = null;
				            o.toString();
				            return null;
				        } catch(Exception e) {
				            System.out.print("EXCEPTION:");
							return e;        \t
				        } finally {
				            System.out.print("ONCE:");
				            foo();
				        }
				    }
				
				    private void foo() {
				        throw new IllegalStateException("Gah!");
				    }       \s
				}
				""",
		},
		"EXCEPTION:ONCE:SUCCESS");
}
/*
 * Try block is never reached
 */
public void test031() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
					static void foo(Object o) {}
				\t
				    public static void main(String[] args) {
				    	try {
				    		foo(new Object() {
				    			public void bar() throws IOException {
				    				bar1();
				    			}
				    		});
				    	} catch(IOException e) {
				    		e.printStackTrace();
				    	}
				    }
				   \s
				    static void bar1() throws IOException {}
				}"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 13)\n" +
		"	} catch(IOException e) {\n" +
		"	        ^^^^^^^^^^^\n" +
		"Unreachable catch block for IOException. This exception is never thrown from the try statement body\n" +
		"----------\n",
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=114855
 */
public void test032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X
				{
				  static int except_count;
				
				  static boolean test_result = true;
				 \s
				  static Throwable all_except[] =
				  {
				    new AbstractMethodError(),             //  0
				    new ArithmeticException(),             //  1
				    new ArrayIndexOutOfBoundsException(),  //  2
				    new ArrayStoreException(),             //  3
				    new ClassCastException(),              //  4
				    new ClassCircularityError(),           //  5
				    new ClassFormatError(),                //  6
				    new ClassNotFoundException(),          //  7
				    new CloneNotSupportedException(),      //  8
				    new Error(),                           //  9
				    new Exception(),                       // 10
				    new IllegalAccessError(),              // 11
				    new IllegalAccessException(),          // 12
				    new IllegalArgumentException(),        // 13
				    new IllegalMonitorStateException(),    // 14
				    new IllegalThreadStateException(),     // 15
				    new IncompatibleClassChangeError(),    // 16
				    new IndexOutOfBoundsException(),       // 17
				    new InstantiationError(),              // 18
				    new InstantiationException(),          // 19
				    new InternalError(),                   // 20
				    new InterruptedException(),            // 21
				    new LinkageError(),                    // 22
				    new NegativeArraySizeException(),      // 23
				    new NoClassDefFoundError(),            // 24
				    new NoSuchFieldError(),                // 25
				    new NoSuchMethodError(),               // 26
				    new NoSuchMethodException(),           // 27
				    new NullPointerException(),            // 28
				    new NumberFormatException(),           // 29
				    new OutOfMemoryError(),                // 30
				    new StackOverflowError(),              // 31
				    new RuntimeException(),                // 32
				    new SecurityException(),               // 33
				    new StringIndexOutOfBoundsException(), // 34
				    new ThreadDeath(),                     // 35
				    new UnknownError(),                    // 36
				    new UnsatisfiedLinkError(),            // 37
				    new VerifyError(),                     // 38
				  };
				
				  private static void check_except(int i)
				    throws Throwable
				  {
				    if (except_count != i)
				    {
				      System.out.println("Error "+except_count+" != "+i+";");
				      test_result=false;
				    }
				    throw all_except[++except_count];
				  }
				
				  public static void main(String[] args) throws Throwable
				  {
				    try {
				      except_count = 0;
				      throw all_except[except_count];
				    } catch (AbstractMethodError e0) {
				      try {
				        check_except(0);
				      } catch (ArithmeticException e1) {
				        try {
				          check_except(1);
				        } catch (ArrayIndexOutOfBoundsException e2) {
				          try {
				            check_except(2);
				          } catch (ArrayStoreException e3) {
				            try {
				              check_except(3);
				            } catch (ClassCastException e4) {
				              try {
				                check_except(4);
				              } catch (ClassCircularityError e5) {
				                try {
				                  check_except(5);
				                } catch (ClassFormatError e6) {
				                  try {
				                    check_except(6);
				                  } catch (ClassNotFoundException e7) {
				                    try {
				                      check_except(7);
				                    } catch (CloneNotSupportedException e8) {
				                      try {
				                        check_except(8);
				                      } catch (Error e9) {
				                        try {
				                          check_except(9);
				                        } catch (Exception e10) {
				                          try {
				                            check_except(10);
				                          } catch (IllegalAccessError e11) {
				                            try {
				                              check_except(11);
				                            } catch (IllegalAccessException e12) {
				                              try {
				                                check_except(12);
				                              } catch (IllegalArgumentException e13) {
				                                try {
				                                  check_except(13);
				                                } catch (IllegalMonitorStateException e14) {
				                                  try {
				                                    check_except(14);
				                                  } catch (IllegalThreadStateException e15) {
				                                    try {
				                                      check_except(15);
				                                    } catch (IncompatibleClassChangeError e16) {
				                                      try {
				                                        check_except(16);
				                                      } catch (IndexOutOfBoundsException e17) {
				                                        try {
				                                          check_except(17);
				                                        } catch (InstantiationError e18) {
				                                          try {
				                                            check_except(18);
				                                          } catch (InstantiationException e19) {
				                                            try {
				                                              check_except(19);
				                                            } catch (InternalError e20) {
				                                              try {
				                                                check_except(20);
				                                              } catch (InterruptedException\s
				e21) {
				                                                try {
				                                                  check_except(21);
				                                                } catch (LinkageError e22) {
				                                                  try {
				                                                    check_except(22);
				                                                  } catch\s
				(NegativeArraySizeException e23) {
				                                                    try {
				                                                      check_except(23);
				                                                    } catch\s
				(NoClassDefFoundError e24) {
				                                                      try {
				                                                        check_except(24);
				                                                      } catch (NoSuchFieldError\s
				e25) {
				                                                        try {
				                                                          check_except(25);
				                                                        } catch\s
				(NoSuchMethodError e26) {
				                                                          try {
				                                                            check_except(26);
				                                                          } catch\s
				(NoSuchMethodException e27) {
				                                                            try {
				                                                              check_except(27);
				                                                            } catch\s
				(NullPointerException e28) {
				                                                              try {
				                                                                check_except
				(28);
				                                                              } catch\s
				(NumberFormatException e29) {
				                                                                try {
				                                                                  check_except
				(29);
				                                                                } catch\s
				(OutOfMemoryError e30) {
				                                                                  try {
				                                                                    check_except
				(30);
				                                                                  } catch\s
				(StackOverflowError e31) {
				                                                                    try {
				                                                                     \s
				check_except(31);
				                                                                    } catch\s
				(RuntimeException e32) {
				                                                                      try {
				                                                                       \s
				check_except(32);
				                                                                      } catch\s
				(SecurityException e33) {
				                                                                        try {
				                                                                         \s
				check_except(33);
				                                                                        } catch\s
				(StringIndexOutOfBoundsException e34) {
				                                                                          try {
				                                                                           \s
				check_except(34);
				                                                                          }\s
				catch (ThreadDeath e35) {
				                                                                            try\s
				{
				                                                                             \s
				check_except(35);
				                                                                            }\s
				catch (UnknownError e36) {
				                                                                             \s
				try {
				                                                                               \s
				check_except(36);
				                                                                              }\s
				catch (UnsatisfiedLinkError e37) {
				                                                                               \s
				try {
				                                                                               \s
				  check_except(37);
				                                                                               \s
				} catch (VerifyError e38) {
				                                                                               \s
				  ++except_count;
				                                                                               \s
				}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
				    System.out.print(test_result & (except_count == all_except.length));
				  }
				}""",
		},
		"true");
}
public void test033() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						interface IActionSetContributionItem {
							String getActionSetId();
						}
						public interface IAction {
						}
						interface IContributionItem {
							String getId();
							boolean isSeparator();
							boolean isGroupMarker();
						}
					    public static void findInsertionPoint(String startId,
					            String sortId, IContributionItem[] items) {
					        // Find the reference item.
					        try {
						        int insertIndex = 0;
						        while (insertIndex < items.length) {
						            if (startId.equals(items[insertIndex].getId()))
						                break;
						            ++insertIndex;
						        }
						        if (insertIndex >= items.length)
						            return;
					\t
						        int compareMetric = 0;
					\t
						        // Find the insertion point for the new item.
						        // We do this by iterating through all of the previous
						        // action set contributions define within the current group.
						        for (int nX = insertIndex + 1; nX < items.length; nX++) {
						            IContributionItem item = items[nX];
						            if (item.isSeparator() || item.isGroupMarker()) {
						                // Fix for bug report 18357
						                break;
						            }
						            if (item instanceof IActionSetContributionItem) {
						                if (sortId != null) {
						                    String testId = ((IActionSetContributionItem) item)
						                            .getActionSetId();
						                    if (sortId.compareTo(testId) < compareMetric)
						                        break;
						                }
						                insertIndex = nX;
						            } else {
						                break;
						            }
						        }
						    } catch(Exception e) {}
					    }
					   \s
					    public static void main(String[] args) {
							findInsertionPoint("", "", null);
						}
					}""",
			},
			"");
	String expectedOutput =
		"""
		  // Method descriptor #15 (Ljava/lang/String;Ljava/lang/String;[LX$IContributionItem;)V
		  // Stack: 3, Locals: 8
		  public static void findInsertionPoint(java.lang.String startId, java.lang.String sortId, X.IContributionItem[] items);
		      0  iconst_0
		      1  istore_3 [insertIndex]
		      2  goto 26
		      5  aload_0 [startId]
		      6  aload_2 [items]
		      7  iload_3 [insertIndex]
		      8  aaload
		      9  invokeinterface X$IContributionItem.getId() : java.lang.String [16] [nargs: 1]
		     14  invokevirtual java.lang.String.equals(java.lang.Object) : boolean [22]
		     17  ifeq 23
		     20  goto 32
		     23  iinc 3 1 [insertIndex]
		     26  iload_3 [insertIndex]
		     27  aload_2 [items]
		     28  arraylength
		     29  if_icmplt 5
		     32  iload_3 [insertIndex]
		     33  aload_2 [items]
		     34  arraylength
		     35  if_icmplt 39
		     38  return
		     39  iconst_0
		     40  istore 4 [compareMetric]
		     42  iload_3 [insertIndex]
		     43  iconst_1
		     44  iadd
		     45  istore 5 [nX]
		     47  goto 123
		     50  aload_2 [items]
		     51  iload 5 [nX]
		     53  aaload
		     54  astore 6 [item]
		     56  aload 6 [item]
		     58  invokeinterface X$IContributionItem.isSeparator() : boolean [28] [nargs: 1]
		     63  ifne 134
		     66  aload 6 [item]
		     68  invokeinterface X$IContributionItem.isGroupMarker() : boolean [32] [nargs: 1]
		     73  ifeq 79
		     76  goto 134
		     79  aload 6 [item]
		     81  instanceof X$IActionSetContributionItem [35]
		     84  ifeq 134
		     87  aload_1 [sortId]
		     88  ifnull 117
		     91  aload 6 [item]
		     93  checkcast X$IActionSetContributionItem [35]
		     96  invokeinterface X$IActionSetContributionItem.getActionSetId() : java.lang.String [37] [nargs: 1]
		    101  astore 7 [testId]
		    103  aload_1 [sortId]
		    104  aload 7 [testId]
		    106  invokevirtual java.lang.String.compareTo(java.lang.String) : int [40]
		    109  iload 4 [compareMetric]
		    111  if_icmpge 117
		    114  goto 134
		    117  iload 5 [nX]
		    119  istore_3 [insertIndex]
		    120  iinc 5 1 [nX]
		    123  iload 5 [nX]
		    125  aload_2 [items]
		    126  arraylength
		    127  if_icmplt 50
		    130  goto 134
		    133  astore_3
		    134  return
		""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853
public void test034() throws Exception {
	String builder = this.complianceLevel >= ClassFileConstants.JDK1_5 ? "StringBuilder" : "StringBuffer";
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	private static int scenario(){\n" +
				"		try {\n" +
				"			int i = 1;\n" +
				"			System.out.print(new " + builder + "(\"[i: \").append(i).append(\"]\").toString());\n" +
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
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			scenario();\n" +
				"		} catch(Exception e){\n" +
				"			System.out.println(\"[end]\");\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"}\n",
			},
			"[i: 1][finally][clean][end]");

//	if (this.complianceLevel.compareTo(COMPLIANCE_1_6) >= 0) return;
	String expectedOutput = new CompilerOptions(getCompilerOptions()).inlineJsrBytecode
		?	"""
			  // Method descriptor #15 ()I
			  // Stack: 4, Locals: 4
			  private static int scenario();
			      0  iconst_1
			      1  istore_0 [i]
			      2  getstatic java.lang.System.out : java.io.PrintStream [16]
			      5  new java.lang.StringBuilder [22]
			      8  dup
			      9  ldc <String "[i: "> [24]
			     11  invokespecial java.lang.StringBuilder(java.lang.String) [26]
			     14  iload_0 [i]
			     15  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [29]
			     18  ldc <String "]"> [33]
			     20  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [35]
			     23  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
			     26  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
			     29  iload_0 [i]
			     30  iconst_5
			     31  if_icmple 61
			     34  iload_0 [i]
			     35  istore_2
			     36  getstatic java.lang.System.out : java.io.PrintStream [16]
			     39  ldc <String "[finally]"> [47]
			     41  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
			     44  invokestatic X.throwRuntime() : void [49]
			     47  goto 56
			     50  astore_3
			     51  invokestatic X.clean() : void [52]
			     54  aload_3
			     55  athrow
			     56  invokestatic X.clean() : void [52]
			     59  iload_2
			     60  ireturn
			     61  iload_0 [i]
			     62  ineg
			     63  istore_2
			     64  getstatic java.lang.System.out : java.io.PrintStream [16]
			     67  ldc <String "[finally]"> [47]
			     69  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
			     72  invokestatic X.throwRuntime() : void [49]
			     75  goto 84
			     78  astore_3
			     79  invokestatic X.clean() : void [52]
			     82  aload_3
			     83  athrow
			     84  invokestatic X.clean() : void [52]
			     87  iload_2
			     88  ireturn
			     89  astore_0 [e]
			     90  getstatic java.lang.System.out : java.io.PrintStream [16]
			     93  ldc <String "[WRONG CATCH]"> [55]
			     95  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
			     98  getstatic java.lang.System.out : java.io.PrintStream [16]
			    101  ldc <String "[finally]"> [47]
			    103  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
			    106  invokestatic X.throwRuntime() : void [49]
			    109  goto 118
			    112  astore_3
			    113  invokestatic X.clean() : void [52]
			    116  aload_3
			    117  athrow
			    118  invokestatic X.clean() : void [52]
			    121  iconst_2
			    122  ireturn
			    123  astore_1
			    124  getstatic java.lang.System.out : java.io.PrintStream [16]
			    127  ldc <String "[finally]"> [47]
			    129  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
			    132  invokestatic X.throwRuntime() : void [49]
			    135  goto 144
			    138  astore_3
			    139  invokestatic X.clean() : void [52]
			    142  aload_3
			    143  athrow
			    144  invokestatic X.clean() : void [52]
			    147  aload_1
			    148  athrow
			      Exception Table:
			        [pc: 44, pc: 50] -> 50 when : any
			        [pc: 72, pc: 78] -> 78 when : any
			        [pc: 0, pc: 36] -> 89 when : java.lang.Exception
			        [pc: 61, pc: 64] -> 89 when : java.lang.Exception
			        [pc: 106, pc: 112] -> 112 when : any
			        [pc: 0, pc: 36] -> 123 when : any
			        [pc: 61, pc: 64] -> 123 when : any
			        [pc: 89, pc: 98] -> 123 when : any
			        [pc: 132, pc: 138] -> 138 when : any
			"""
	: 		"""
		  // Method descriptor #15 ()I
		  // Stack: 4, Locals: 6
		  private static int scenario();
		      0  iconst_1
		      1  istore_0 [i]
		      2  getstatic java.lang.System.out : java.io.PrintStream [16]
		      5  new java.lang.StringBuffer [22]
		      8  dup
		      9  ldc <String "[i: "> [24]
		     11  invokespecial java.lang.StringBuffer(java.lang.String) [26]
		     14  iload_0 [i]
		     15  invokevirtual java.lang.StringBuffer.append(int) : java.lang.StringBuffer [29]
		     18  ldc <String "]"> [33]
		     20  invokevirtual java.lang.StringBuffer.append(java.lang.String) : java.lang.StringBuffer [35]
		     23  invokevirtual java.lang.StringBuffer.toString() : java.lang.String [38]
		     26  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
		     29  iload_0 [i]
		     30  iconst_5
		     31  if_icmple 41
		     34  iload_0 [i]
		     35  istore_3
		     36  jsr 69
		     39  iload_3
		     40  ireturn
		     41  iload_0 [i]
		     42  ineg
		     43  istore_3
		     44  jsr 69
		     47  iload_3
		     48  ireturn
		     49  astore_0 [e]
		     50  getstatic java.lang.System.out : java.io.PrintStream [16]
		     53  ldc <String "[WRONG CATCH]"> [47]
		     55  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
		     58  jsr 69
		     61  iconst_2
		     62  ireturn
		     63  astore_2
		     64  jsr 69
		     67  aload_2
		     68  athrow
		     69  astore_1
		     70  getstatic java.lang.System.out : java.io.PrintStream [16]
		     73  ldc <String "[finally]"> [49]
		     75  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
		     78  invokestatic X.throwRuntime() : void [51]
		     81  goto 99
		     84  astore 5
		     86  jsr 92
		     89  aload 5
		     91  athrow
		     92  astore 4
		     94  invokestatic X.clean() : void [54]
		     97  ret 4
		     99  jsr 92
		    102  ret 1
		      Exception Table:
		        [pc: 0, pc: 39] -> 49 when : java.lang.Exception
		        [pc: 41, pc: 47] -> 49 when : java.lang.Exception
		        [pc: 0, pc: 39] -> 63 when : any
		        [pc: 41, pc: 47] -> 63 when : any
		        [pc: 49, pc: 61] -> 63 when : any
		        [pc: 78, pc: 84] -> 84 when : any
		        [pc: 99, pc: 102] -> 84 when : any
		""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853 - variation
public void test035() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							try {
								new X().bar();
							} catch(Exception e){
								System.out.println("[end]");
							}
						}
						Object bar() {
							try {
								System.out.print("[try]");
								return this;
							} catch(Exception e){
								System.out.print("[WRONG CATCH]");
							} finally {
								System.out.print("[finally]");
								foo();
							}
							return this;
						}
						Object foo() {
							throw new RuntimeException();
						}
					}
					""",
			},
			"[try][finally][end]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853 - variation
public void test036() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							try {
								new X().bar();
							} catch(Exception e){
								System.out.println("[end]");
							}
						}
						Object bar() {
							try {
								System.out.print("[try]");
								throw new RuntimeException();
							} catch(Exception e){
								System.out.print("[catch]");
								return this;
							} finally {
								System.out.print("[finally]");
								foo();
							}
						}
						Object foo() {
							throw new RuntimeException();
						}
					}
					""",
			},
			"[try][catch][finally][end]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853 - variation
public void test037() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
							try {
								scenario();
							} catch(Exception e){
								System.out.println("[end]");
							}
						}
					
						private static void scenario() throws Exception {
							try {
								System.out.print("[try1]");
								try {
									System.out.print("[try2]");
									return;
								} catch(Exception e) {
									System.out.print("[catch2]");
								} finally {
									System.out.print("[finally2]");
									throwRuntime();
								}
							} catch(Exception e) {
								System.out.print("[catch1]");
								throw e;
							} finally {
								System.out.print("[finally1]");
							}
						}
					
						private static void throwRuntime() {
							throw new RuntimeException("error");
						}
					}
					""",
			},
			"[try1][try2][finally2][catch1][finally1][end]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87423
public void test038() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						int hasLoop() {
							int l, m, n;
							for (m = 0; m < 10; m++) {
								n = 2;
								try {
									n = 3;
									try {
										n = 4;
									} catch (ArithmeticException e1) {
										n = 11;
									} finally {
										for (l = 0; l < 10; l++) {
											n++;
										}
										if (n == 12) {
											n = 13;
											break;
										}
										n = 15;
									}
								} catch (OutOfMemoryError e2) {
									n = 18;
								}
							}
							return 0;
						}
					
						public static void main(String args[]) {
					      System.out.println("Loaded fine");
					   }
					}
					""",
			},
			"Loaded fine");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127603
public void test039() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void someMethod() {
							int count = 0;
							int code = -1;
							while (count < 2 && (code == -1 || code == 2)) {
								count++;
								try {
									{
										System.out.print("[Try:" + count + ";" + code+"]");
									}
									code = 0;
					
								} finally {
									System.out.print("[Finally" + count + ";" + code+"]");
								}
							}
							System.out.print("[Outering");
					
							if (code == 0) {
								System.out.print("[Return:" + count + ";" + code+"]");
								return;
							}
							throw new RuntimeException(null + "a");
						}
					
						public static void main(String[] args) throws Exception {
							for (int i = 0; i < 1; i++) {
								someMethod();
								System.out.println();
							}
						}
					}
					""",
			},
			"[Try:1;-1][Finally1;0][Outering[Return:1;0]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128705
public void test040() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo(boolean b) {
							try {\s
								if (b){\s
									int i = 0;
									return;
								} else {
									Object o = null;
									return;
								}
							} finally {
								System.out.println("done");
							}
						}
						public static void main(String[] args) {
							new X().foo(true);
						}
					}
					""",
			},
			"done");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"""
			  // Method descriptor #15 (Z)V
			  // Stack: 2, Locals: 5
			  public void foo(boolean b);
			     0  iload_1 [b]
			     1  ifeq 10
			     4  iconst_0
			     5  istore_2 [i]
			     6  jsr 23
			     9  return
			    10  aconst_null
			    11  astore_2 [o]
			    12  goto 6
			    15  astore 4
			    17  jsr 23
			    20  aload 4
			    22  athrow
			    23  astore_3
			    24  getstatic java.lang.System.out : java.io.PrintStream [16]
			    27  ldc <String "done"> [22]
			    29  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
			    32  ret 3
			      Exception Table:
			        [pc: 0, pc: 9] -> 15 when : any
			        [pc: 10, pc: 15] -> 15 when : any
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 4, line: 5]
			        [pc: 6, line: 6]
			        [pc: 10, line: 8]
			        [pc: 12, line: 9]
			        [pc: 15, line: 11]
			        [pc: 20, line: 13]
			        [pc: 23, line: 11]
			        [pc: 24, line: 12]
			        [pc: 32, line: 13]
			      Local variable table:
			        [pc: 0, pc: 34] local: this index: 0 type: X
			        [pc: 0, pc: 34] local: b index: 1 type: boolean
			        [pc: 6, pc: 10] local: i index: 2 type: int
			        [pc: 12, pc: 15] local: o index: 2 type: java.lang.Object
			"""
	: 		null;
	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = """
				  // Method descriptor #15 (Z)V
				  // Stack: 2, Locals: 4
				  public void foo(boolean b);
				     0  iload_1 [b]
				     1  ifeq 15
				     4  iconst_0
				     5  istore_2 [i]
				     6  getstatic java.lang.System.out : java.io.PrintStream [16]
				     9  ldc <String "done"> [22]
				    11  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    14  return
				    15  aconst_null
				    16  astore_2 [o]
				    17  getstatic java.lang.System.out : java.io.PrintStream [16]
				    20  ldc <String "done"> [22]
				    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    25  return
				    26  astore_3
				    27  getstatic java.lang.System.out : java.io.PrintStream [16]
				    30  ldc <String "done"> [22]
				    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    35  aload_3
				    36  athrow
				      Exception Table:
				        [pc: 0, pc: 6] -> 26 when : any
				        [pc: 15, pc: 17] -> 26 when : any
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 4, line: 5]
				        [pc: 6, line: 12]
				        [pc: 14, line: 6]
				        [pc: 15, line: 8]
				        [pc: 17, line: 12]
				        [pc: 25, line: 9]
				        [pc: 26, line: 11]
				        [pc: 27, line: 12]
				        [pc: 35, line: 13]
				      Local variable table:
				        [pc: 0, pc: 37] local: this index: 0 type: X
				        [pc: 0, pc: 37] local: b index: 1 type: boolean
				        [pc: 6, pc: 15] local: i index: 2 type: int
				        [pc: 17, pc: 26] local: o index: 2 type: java.lang.Object
				""";
		} else {
			expectedOutput = """
				  // Method descriptor #15 (Z)V
				  // Stack: 2, Locals: 4
				  public void foo(boolean b);
				     0  iload_1 [b]
				     1  ifeq 15
				     4  iconst_0
				     5  istore_2 [i]
				     6  getstatic java.lang.System.out : java.io.PrintStream [16]
				     9  ldc <String "done"> [22]
				    11  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    14  return
				    15  aconst_null
				    16  astore_2 [o]
				    17  getstatic java.lang.System.out : java.io.PrintStream [16]
				    20  ldc <String "done"> [22]
				    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    25  return
				    26  astore_3
				    27  getstatic java.lang.System.out : java.io.PrintStream [16]
				    30  ldc <String "done"> [22]
				    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    35  aload_3
				    36  athrow
				      Exception Table:
				        [pc: 0, pc: 6] -> 26 when : any
				        [pc: 15, pc: 17] -> 26 when : any
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 4, line: 5]
				        [pc: 6, line: 12]
				        [pc: 14, line: 6]
				        [pc: 15, line: 8]
				        [pc: 17, line: 12]
				        [pc: 25, line: 9]
				        [pc: 26, line: 11]
				        [pc: 27, line: 12]
				        [pc: 35, line: 13]
				      Local variable table:
				        [pc: 0, pc: 37] local: this index: 0 type: X
				        [pc: 0, pc: 37] local: b index: 1 type: boolean
				        [pc: 6, pc: 15] local: i index: 2 type: int
				        [pc: 17, pc: 26] local: o index: 2 type: java.lang.Object
				      Stack map table: number of frames 2
				        [pc: 15, same]
				        [pc: 26, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				""";
		}
	}

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128705 - variation
public void test041() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo(boolean b) {
							try {
								int i = 0;
								return;
							} catch(Exception e) {
								return;
							} finally {
								System.out.println("done");
							}
						}
						public static void main(String[] args) {
							new X().foo(true);
						}
					}
					""",
			},
			"done");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"""
			  // Method descriptor #15 (Z)V
			  // Stack: 2, Locals: 5
			  public void foo(boolean b);
			     0  iconst_0
			     1  istore_2 [i]
			     2  jsr 18
			     5  return
			     6  astore_2 [e]
			     7  goto 2
			    10  astore 4
			    12  jsr 18
			    15  aload 4
			    17  athrow
			    18  astore_3
			    19  getstatic java.lang.System.out : java.io.PrintStream [16]
			    22  ldc <String "done"> [22]
			    24  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
			    27  ret 3
			      Exception Table:
			        [pc: 0, pc: 5] -> 6 when : java.lang.Exception
			        [pc: 0, pc: 5] -> 10 when : any
			        [pc: 6, pc: 10] -> 10 when : any
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 2, line: 5]
			        [pc: 6, line: 6]
			        [pc: 7, line: 7]
			        [pc: 10, line: 8]
			        [pc: 15, line: 10]
			        [pc: 18, line: 8]
			        [pc: 19, line: 9]
			        [pc: 27, line: 10]
			      Local variable table:
			        [pc: 0, pc: 29] local: this index: 0 type: X
			        [pc: 0, pc: 29] local: b index: 1 type: boolean
			        [pc: 2, pc: 6] local: i index: 2 type: int
			        [pc: 7, pc: 10] local: e index: 2 type: java.lang.Exception
			"""
		: null;
	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = """
				  // Method descriptor #15 (Z)V
				  // Stack: 2, Locals: 4
				  public void foo(boolean b);
				     0  iconst_0
				     1  istore_2 [i]
				     2  getstatic java.lang.System.out : java.io.PrintStream [16]
				     5  ldc <String "done"> [22]
				     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    10  return
				    11  astore_2 [e]
				    12  getstatic java.lang.System.out : java.io.PrintStream [16]
				    15  ldc <String "done"> [22]
				    17  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    20  return
				    21  astore_3
				    22  getstatic java.lang.System.out : java.io.PrintStream [16]
				    25  ldc <String "done"> [22]
				    27  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    30  aload_3
				    31  athrow
				      Exception Table:
				        [pc: 0, pc: 2] -> 11 when : java.lang.Exception
				        [pc: 0, pc: 2] -> 21 when : any
				        [pc: 11, pc: 12] -> 21 when : any
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 2, line: 9]
				        [pc: 10, line: 5]
				        [pc: 11, line: 6]
				        [pc: 12, line: 9]
				        [pc: 20, line: 7]
				        [pc: 21, line: 8]
				        [pc: 22, line: 9]
				        [pc: 30, line: 10]
				      Local variable table:
				        [pc: 0, pc: 32] local: this index: 0 type: X
				        [pc: 0, pc: 32] local: b index: 1 type: boolean
				        [pc: 2, pc: 11] local: i index: 2 type: int
				        [pc: 12, pc: 21] local: e index: 2 type: java.lang.Exception
				""";
		} else {
			expectedOutput = """
				  // Method descriptor #15 (Z)V
				  // Stack: 2, Locals: 4
				  public void foo(boolean b);
				     0  iconst_0
				     1  istore_2 [i]
				     2  getstatic java.lang.System.out : java.io.PrintStream [16]
				     5  ldc <String "done"> [22]
				     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    10  return
				    11  astore_2 [e]
				    12  getstatic java.lang.System.out : java.io.PrintStream [16]
				    15  ldc <String "done"> [22]
				    17  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    20  return
				    21  astore_3
				    22  getstatic java.lang.System.out : java.io.PrintStream [16]
				    25  ldc <String "done"> [22]
				    27  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    30  aload_3
				    31  athrow
				      Exception Table:
				        [pc: 0, pc: 2] -> 11 when : java.lang.Exception
				        [pc: 0, pc: 2] -> 21 when : any
				        [pc: 11, pc: 12] -> 21 when : any
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 2, line: 9]
				        [pc: 10, line: 5]
				        [pc: 11, line: 6]
				        [pc: 12, line: 9]
				        [pc: 20, line: 7]
				        [pc: 21, line: 8]
				        [pc: 22, line: 9]
				        [pc: 30, line: 10]
				      Local variable table:
				        [pc: 0, pc: 32] local: this index: 0 type: X
				        [pc: 0, pc: 32] local: b index: 1 type: boolean
				        [pc: 2, pc: 11] local: i index: 2 type: int
				        [pc: 12, pc: 21] local: e index: 2 type: java.lang.Exception
				      Stack map table: number of frames 2
				        [pc: 11, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 21, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				""";
		}
	}

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128705 - variation
public void test042() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					 public class X {
					 public static void main(String[] args) {
							System.out.println(new X().foo(args));
						}
						String foo(String[] args) {
							try {
								if (args == null) return "KO";
								switch(args.length) {
								case 0:
									return "OK";
								case 1:
									return "KO";
								case 3:
									return "OK";
								default:
									return "KO";
								}
							} finally {
								System.out.print("FINALLY:");
							}
						}
					}
					""",
			},
			"FINALLY:OK");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).inlineJsrBytecode
		?	"""
			  // Method descriptor #26 ([Ljava/lang/String;)Ljava/lang/String;
			  // Stack: 2, Locals: 3
			  java.lang.String foo(java.lang.String[] args);
			     0  aload_1 [args]
			     1  ifnonnull 15
			     4  getstatic java.lang.System.out : java.io.PrintStream [16]
			     7  ldc <String "FINALLY:"> [35]
			     9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]
			    12  ldc <String "KO"> [40]
			    14  areturn
			    15  aload_1 [args]
			    16  arraylength
			    17  tableswitch default: 65
			          case 0: 48
			          case 1: 59
			          case 2: 65
			          case 3: 62
			    48  getstatic java.lang.System.out : java.io.PrintStream [16]
			    51  ldc <String "FINALLY:"> [35]
			    53  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]
			    56  ldc <String "OK"> [42]
			    58  areturn
			    59  goto 4
			    62  goto 48
			    65  goto 4
			    68  astore_2
			    69  getstatic java.lang.System.out : java.io.PrintStream [16]
			    72  ldc <String "FINALLY:"> [35]
			    74  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]
			    77  aload_2
			    78  athrow
			      Exception Table:
			        [pc: 0, pc: 4] -> 68 when : any
			        [pc: 15, pc: 48] -> 68 when : any
			        [pc: 59, pc: 68] -> 68 when : any
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 4, line: 19]
			        [pc: 12, line: 7]
			        [pc: 15, line: 8]
			        [pc: 48, line: 19]
			        [pc: 56, line: 10]
			        [pc: 59, line: 12]
			        [pc: 62, line: 14]
			        [pc: 65, line: 16]
			        [pc: 68, line: 18]
			        [pc: 69, line: 19]
			        [pc: 77, line: 20]
			      Local variable table:
			        [pc: 0, pc: 79] local: this index: 0 type: X
			        [pc: 0, pc: 79] local: args index: 1 type: java.lang.String[]
			"""
	: 		"""
		  // Method descriptor #26 ([Ljava/lang/String;)Ljava/lang/String;
		  // Stack: 2, Locals: 4
		  java.lang.String foo(java.lang.String[] args);
		     0  aload_1 [args]
		     1  ifnonnull 10
		     4  jsr 65
		     7  ldc <String "KO"> [35]
		     9  areturn
		    10  aload_1 [args]
		    11  arraylength
		    12  tableswitch default: 56
		          case 0: 44
		          case 1: 50
		          case 2: 56
		          case 3: 53
		    44  jsr 65
		    47  ldc <String "OK"> [37]
		    49  areturn
		    50  goto 4
		    53  goto 44
		    56  goto 4
		    59  astore_3
		    60  jsr 65
		    63  aload_3
		    64  athrow
		    65  astore_2
		    66  getstatic java.lang.System.out : java.io.PrintStream [16]
		    69  ldc <String "FINALLY:"> [39]
		    71  invokevirtual java.io.PrintStream.print(java.lang.String) : void [41]
		    74  ret 2
		      Exception Table:
		        [pc: 0, pc: 7] -> 59 when : any
		        [pc: 10, pc: 47] -> 59 when : any
		        [pc: 50, pc: 59] -> 59 when : any
		      Line numbers:
		        [pc: 0, line: 7]
		        [pc: 10, line: 8]
		        [pc: 44, line: 10]
		        [pc: 50, line: 12]
		        [pc: 53, line: 14]
		        [pc: 56, line: 16]
		        [pc: 59, line: 18]
		        [pc: 63, line: 20]
		        [pc: 65, line: 18]
		        [pc: 66, line: 19]
		        [pc: 74, line: 20]
		      Local variable table:
		        [pc: 0, pc: 76] local: this index: 0 type: X
		        [pc: 0, pc: 76] local: args index: 1 type: java.lang.String[]
		""";

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
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=404146 - variation without sharing of inlined escaping finally-blocks
public void test042_not_shared() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ShareCommonFinallyBlocks, CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_InlineJsr, CompilerOptions.ENABLED);

	this.runConformTest(
			new String[] {
				"X.java",
				"""
					 public class X {
					 public static void main(String[] args) {
							System.out.println(new X().foo(args));
						}
						String foo(String[] args) {
							try {
								if (args == null) return "KO";
								switch(args.length) {
								case 0:
									return "OK";
								case 1:
									return "KO";
								case 3:
									return "OK";
								default:
									return "KO";
								}
							} finally {
								System.out.print("FINALLY:");
							}
						}
					}
					""",
			},
			"FINALLY:OK",
			null,
			true,
			null,
			customOptions,
			null);

	String expectedOutput =
			"""
		  // Method descriptor #26 ([Ljava/lang/String;)Ljava/lang/String;
		  // Stack: 2, Locals: 3
		  java.lang.String foo(java.lang.String[] args);
		      0  aload_1 [args]
		      1  ifnonnull 15
		      4  getstatic java.lang.System.out : java.io.PrintStream [16]
		      7  ldc <String "FINALLY:"> [35]
		      9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]
		     12  ldc <String "KO"> [40]
		     14  areturn
		     15  aload_1 [args]
		     16  arraylength
		     17  tableswitch default: 81
		          case 0: 48
		          case 1: 59
		          case 2: 81
		          case 3: 70
		     48  getstatic java.lang.System.out : java.io.PrintStream [16]
		     51  ldc <String "FINALLY:"> [35]
		     53  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]
		     56  ldc <String "OK"> [42]
		     58  areturn
		     59  getstatic java.lang.System.out : java.io.PrintStream [16]
		     62  ldc <String "FINALLY:"> [35]
		     64  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]
		     67  ldc <String "KO"> [40]
		     69  areturn
		     70  getstatic java.lang.System.out : java.io.PrintStream [16]
		     73  ldc <String "FINALLY:"> [35]
		     75  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]
		     78  ldc <String "OK"> [42]
		     80  areturn
		     81  getstatic java.lang.System.out : java.io.PrintStream [16]
		     84  ldc <String "FINALLY:"> [35]
		     86  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]
		     89  ldc <String "KO"> [40]
		     91  areturn
		     92  astore_2
		     93  getstatic java.lang.System.out : java.io.PrintStream [16]
		     96  ldc <String "FINALLY:"> [35]
		     98  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]
		    101  aload_2
		    102  athrow
		      Exception Table:
		        [pc: 0, pc: 4] -> 92 when : any
		        [pc: 15, pc: 48] -> 92 when : any
		      Line numbers:
		        [pc: 0, line: 7]
		        [pc: 4, line: 19]
		        [pc: 12, line: 7]
		        [pc: 15, line: 8]
		        [pc: 48, line: 19]
		        [pc: 56, line: 10]
		        [pc: 59, line: 19]
		        [pc: 67, line: 12]
		        [pc: 70, line: 19]
		        [pc: 78, line: 14]
		        [pc: 81, line: 19]
		        [pc: 89, line: 16]
		        [pc: 92, line: 18]
		        [pc: 93, line: 19]
		        [pc: 101, line: 20]
		      Local variable table:
		        [pc: 0, pc: 103] local: this index: 0 type: X
		        [pc: 0, pc: 103] local: args index: 1 type: java.lang.String[]
		""";

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
}



//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128705 - variation
public void test043() throws Exception {
	String builder = this.complianceLevel >= ClassFileConstants.JDK1_5 ? "StringBuilder" : "StringBuffer";
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void save() {\n" +
				"		int a = 3;\n" +
				"		try {\n" +
				"			Object warnings = null;\n" +
				"			Object contexts = null;\n" +
				"			try {\n" +
				"				System.out.print(warnings);\n" +
				"				return;\n" +
				"			} catch (NullPointerException npe) {\n" +
				"				System.out.print(contexts);\n" +
				"				return;\n" +
				"			} finally {\n" +
				"				System.out.print(new " + builder + "(\"#inner -> \").append(a).toString());\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			return;\n" +
				"		} finally {\n" +
				"			int var = 0;\n" +
				"			System.out.println(new " + builder + "(\"#save -> \").append(a).toString());\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().save();\n" +
				"	}\n" +
				"}\n",
			},
			"null#inner -> 3#save -> 3");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"""
			  // Method descriptor #6 ()V
			  // Stack: 4, Locals: 10
			  public void save();
			      0  iconst_3
			      1  istore_1 [a]
			      2  aconst_null
			      3  astore_2 [warnings]
			      4  aconst_null
			      5  astore_3 [contexts]
			      6  getstatic java.lang.System.out : java.io.PrintStream [15]
			      9  aload_2 [warnings]
			     10  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]
			     13  jsr 40
			     16  jsr 78
			     19  return
			     20  astore 4 [npe]
			     22  getstatic java.lang.System.out : java.io.PrintStream [15]
			     25  aload_3 [contexts]
			     26  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]
			     29  goto 13
			     32  astore 6
			     34  jsr 40
			     37  aload 6
			     39  athrow
			     40  astore 5
			     42  getstatic java.lang.System.out : java.io.PrintStream [15]
			     45  new java.lang.StringBuffer [27]
			     48  dup
			     49  ldc <String "#inner -> "> [29]
			     51  invokespecial java.lang.StringBuffer(java.lang.String) [31]
			     54  iload_1 [a]
			     55  invokevirtual java.lang.StringBuffer.append(int) : java.lang.StringBuffer [34]
			     58  invokevirtual java.lang.StringBuffer.toString() : java.lang.String [38]
			     61  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
			     64  ret 5
			     66  astore_2 [e]
			     67  goto 16
			     70  astore 8
			     72  jsr 78
			     75  aload 8
			     77  athrow
			     78  astore 7
			     80  iconst_0
			     81  istore 9 [var]
			     83  getstatic java.lang.System.out : java.io.PrintStream [15]
			     86  new java.lang.StringBuffer [27]
			     89  dup
			     90  ldc <String "#save -> "> [44]
			     92  invokespecial java.lang.StringBuffer(java.lang.String) [31]
			     95  iload_1 [a]
			     96  invokevirtual java.lang.StringBuffer.append(int) : java.lang.StringBuffer [34]
			     99  invokevirtual java.lang.StringBuffer.toString() : java.lang.String [38]
			    102  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
			    105  ret 7
			      Exception Table:
			        [pc: 6, pc: 16] -> 20 when : java.lang.NullPointerException
			        [pc: 6, pc: 16] -> 32 when : any
			        [pc: 20, pc: 32] -> 32 when : any
			        [pc: 2, pc: 19] -> 66 when : java.lang.Exception
			        [pc: 20, pc: 66] -> 66 when : java.lang.Exception
			        [pc: 2, pc: 19] -> 70 when : any
			        [pc: 20, pc: 70] -> 70 when : any
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 5]
			        [pc: 4, line: 6]
			        [pc: 6, line: 8]
			        [pc: 13, line: 9]
			        [pc: 20, line: 10]
			        [pc: 22, line: 11]
			        [pc: 29, line: 12]
			        [pc: 32, line: 13]
			        [pc: 37, line: 15]
			        [pc: 40, line: 13]
			        [pc: 42, line: 14]
			        [pc: 64, line: 15]
			        [pc: 66, line: 16]
			        [pc: 67, line: 17]
			        [pc: 70, line: 18]
			        [pc: 75, line: 21]
			        [pc: 78, line: 18]
			        [pc: 80, line: 19]
			        [pc: 83, line: 20]
			        [pc: 105, line: 21]
			      Local variable table:
			        [pc: 0, pc: 107] local: this index: 0 type: X
			        [pc: 2, pc: 107] local: a index: 1 type: int
			        [pc: 4, pc: 66] local: warnings index: 2 type: java.lang.Object
			        [pc: 6, pc: 66] local: contexts index: 3 type: java.lang.Object
			        [pc: 22, pc: 32] local: npe index: 4 type: java.lang.NullPointerException
			        [pc: 67, pc: 70] local: e index: 2 type: java.lang.Exception
			        [pc: 83, pc: 105] local: var index: 9 type: int
			"""
		: null;

	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = """
				  // Method descriptor #6 ()V
				  // Stack: 4, Locals: 8
				  public void save();
				      0  iconst_3
				      1  istore_1 [a]
				      2  aconst_null
				      3  astore_2 [warnings]
				      4  aconst_null
				      5  astore_3 [contexts]
				      6  getstatic java.lang.System.out : java.io.PrintStream [15]
				      9  aload_2 [warnings]
				     10  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]
				     13  getstatic java.lang.System.out : java.io.PrintStream [15]
				     16  new java.lang.StringBuilder [27]
				     19  dup
				     20  ldc <String "#inner -> "> [29]
				     22  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				     25  iload_1 [a]
				     26  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				     29  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				     32  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
				     35  iconst_0
				     36  istore 7 [var]
				     38  getstatic java.lang.System.out : java.io.PrintStream [15]
				     41  new java.lang.StringBuilder [27]
				     44  dup
				     45  ldc <String "#save -> "> [44]
				     47  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				     50  iload_1 [a]
				     51  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				     54  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				     57  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
				     60  return
				     61  astore 4 [npe]
				     63  getstatic java.lang.System.out : java.io.PrintStream [15]
				     66  aload_3 [contexts]
				     67  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]
				     70  getstatic java.lang.System.out : java.io.PrintStream [15]
				     73  new java.lang.StringBuilder [27]
				     76  dup
				     77  ldc <String "#inner -> "> [29]
				     79  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				     82  iload_1 [a]
				     83  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				     86  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				     89  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
				     92  iconst_0
				     93  istore 7 [var]
				     95  getstatic java.lang.System.out : java.io.PrintStream [15]
				     98  new java.lang.StringBuilder [27]
				    101  dup
				    102  ldc <String "#save -> "> [44]
				    104  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				    107  iload_1 [a]
				    108  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				    111  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				    114  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
				    117  return
				    118  astore 5
				    120  getstatic java.lang.System.out : java.io.PrintStream [15]
				    123  new java.lang.StringBuilder [27]
				    126  dup
				    127  ldc <String "#inner -> "> [29]
				    129  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				    132  iload_1 [a]
				    133  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				    136  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				    139  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
				    142  aload 5
				    144  athrow
				    145  astore_2 [e]
				    146  iconst_0
				    147  istore 7 [var]
				    149  getstatic java.lang.System.out : java.io.PrintStream [15]
				    152  new java.lang.StringBuilder [27]
				    155  dup
				    156  ldc <String "#save -> "> [44]
				    158  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				    161  iload_1 [a]
				    162  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				    165  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				    168  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
				    171  return
				    172  astore 6
				    174  iconst_0
				    175  istore 7 [var]
				    177  getstatic java.lang.System.out : java.io.PrintStream [15]
				    180  new java.lang.StringBuilder [27]
				    183  dup
				    184  ldc <String "#save -> "> [44]
				    186  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				    189  iload_1 [a]
				    190  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				    193  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				    196  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
				    199  aload 6
				    201  athrow
				      Exception Table:
				        [pc: 6, pc: 13] -> 61 when : java.lang.NullPointerException
				        [pc: 6, pc: 13] -> 118 when : any
				        [pc: 61, pc: 70] -> 118 when : any
				        [pc: 2, pc: 35] -> 145 when : java.lang.Exception
				        [pc: 61, pc: 92] -> 145 when : java.lang.Exception
				        [pc: 118, pc: 145] -> 145 when : java.lang.Exception
				        [pc: 2, pc: 35] -> 172 when : any
				        [pc: 61, pc: 92] -> 172 when : any
				        [pc: 118, pc: 146] -> 172 when : any
				      Line numbers:
				        [pc: 0, line: 3]
				        [pc: 2, line: 5]
				        [pc: 4, line: 6]
				        [pc: 6, line: 8]
				        [pc: 13, line: 14]
				        [pc: 35, line: 19]
				        [pc: 38, line: 20]
				        [pc: 60, line: 9]
				        [pc: 61, line: 10]
				        [pc: 63, line: 11]
				        [pc: 70, line: 14]
				        [pc: 92, line: 19]
				        [pc: 95, line: 20]
				        [pc: 117, line: 12]
				        [pc: 118, line: 13]
				        [pc: 120, line: 14]
				        [pc: 142, line: 15]
				        [pc: 145, line: 16]
				        [pc: 146, line: 19]
				        [pc: 149, line: 20]
				        [pc: 171, line: 17]
				        [pc: 172, line: 18]
				        [pc: 174, line: 19]
				        [pc: 177, line: 20]
				        [pc: 199, line: 21]
				      Local variable table:
				        [pc: 0, pc: 202] local: this index: 0 type: X
				        [pc: 2, pc: 202] local: a index: 1 type: int
				        [pc: 4, pc: 145] local: warnings index: 2 type: java.lang.Object
				        [pc: 6, pc: 145] local: contexts index: 3 type: java.lang.Object
				        [pc: 63, pc: 118] local: npe index: 4 type: java.lang.NullPointerException
				        [pc: 146, pc: 172] local: e index: 2 type: java.lang.Exception
				        [pc: 38, pc: 60] local: var index: 7 type: int
				        [pc: 95, pc: 117] local: var index: 7 type: int
				        [pc: 149, pc: 171] local: var index: 7 type: int
				        [pc: 177, pc: 199] local: var index: 7 type: int
				""";
		} else {
			expectedOutput = """
				  // Method descriptor #6 ()V
				  // Stack: 4, Locals: 8
				  public void save();
				      0  iconst_3
				      1  istore_1 [a]
				      2  aconst_null
				      3  astore_2 [warnings]
				      4  aconst_null
				      5  astore_3 [contexts]
				      6  getstatic java.lang.System.out : java.io.PrintStream [15]
				      9  aload_2 [warnings]
				     10  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]
				     13  getstatic java.lang.System.out : java.io.PrintStream [15]
				     16  new java.lang.StringBuilder [27]
				     19  dup
				     20  ldc <String "#inner -> "> [29]
				     22  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				     25  iload_1 [a]
				     26  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				     29  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				     32  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
				     35  iconst_0
				     36  istore 7 [var]
				     38  getstatic java.lang.System.out : java.io.PrintStream [15]
				     41  new java.lang.StringBuilder [27]
				     44  dup
				     45  ldc <String "#save -> "> [44]
				     47  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				     50  iload_1 [a]
				     51  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				     54  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				     57  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
				     60  return
				     61  astore 4 [npe]
				     63  getstatic java.lang.System.out : java.io.PrintStream [15]
				     66  aload_3 [contexts]
				     67  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]
				     70  getstatic java.lang.System.out : java.io.PrintStream [15]
				     73  new java.lang.StringBuilder [27]
				     76  dup
				     77  ldc <String "#inner -> "> [29]
				     79  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				     82  iload_1 [a]
				     83  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				     86  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				     89  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
				     92  iconst_0
				     93  istore 7 [var]
				     95  getstatic java.lang.System.out : java.io.PrintStream [15]
				     98  new java.lang.StringBuilder [27]
				    101  dup
				    102  ldc <String "#save -> "> [44]
				    104  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				    107  iload_1 [a]
				    108  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				    111  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				    114  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
				    117  return
				    118  astore 5
				    120  getstatic java.lang.System.out : java.io.PrintStream [15]
				    123  new java.lang.StringBuilder [27]
				    126  dup
				    127  ldc <String "#inner -> "> [29]
				    129  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				    132  iload_1 [a]
				    133  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				    136  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				    139  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]
				    142  aload 5
				    144  athrow
				    145  astore_2 [e]
				    146  iconst_0
				    147  istore 7 [var]
				    149  getstatic java.lang.System.out : java.io.PrintStream [15]
				    152  new java.lang.StringBuilder [27]
				    155  dup
				    156  ldc <String "#save -> "> [44]
				    158  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				    161  iload_1 [a]
				    162  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				    165  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				    168  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
				    171  return
				    172  astore 6
				    174  iconst_0
				    175  istore 7 [var]
				    177  getstatic java.lang.System.out : java.io.PrintStream [15]
				    180  new java.lang.StringBuilder [27]
				    183  dup
				    184  ldc <String "#save -> "> [44]
				    186  invokespecial java.lang.StringBuilder(java.lang.String) [31]
				    189  iload_1 [a]
				    190  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]
				    193  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]
				    196  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
				    199  aload 6
				    201  athrow
				      Exception Table:
				        [pc: 6, pc: 13] -> 61 when : java.lang.NullPointerException
				        [pc: 6, pc: 13] -> 118 when : any
				        [pc: 61, pc: 70] -> 118 when : any
				        [pc: 2, pc: 35] -> 145 when : java.lang.Exception
				        [pc: 61, pc: 92] -> 145 when : java.lang.Exception
				        [pc: 118, pc: 145] -> 145 when : java.lang.Exception
				        [pc: 2, pc: 35] -> 172 when : any
				        [pc: 61, pc: 92] -> 172 when : any
				        [pc: 118, pc: 146] -> 172 when : any
				      Line numbers:
				        [pc: 0, line: 3]
				        [pc: 2, line: 5]
				        [pc: 4, line: 6]
				        [pc: 6, line: 8]
				        [pc: 13, line: 14]
				        [pc: 35, line: 19]
				        [pc: 38, line: 20]
				        [pc: 60, line: 9]
				        [pc: 61, line: 10]
				        [pc: 63, line: 11]
				        [pc: 70, line: 14]
				        [pc: 92, line: 19]
				        [pc: 95, line: 20]
				        [pc: 117, line: 12]
				        [pc: 118, line: 13]
				        [pc: 120, line: 14]
				        [pc: 142, line: 15]
				        [pc: 145, line: 16]
				        [pc: 146, line: 19]
				        [pc: 149, line: 20]
				        [pc: 171, line: 17]
				        [pc: 172, line: 18]
				        [pc: 174, line: 19]
				        [pc: 177, line: 20]
				        [pc: 199, line: 21]
				      Local variable table:
				        [pc: 0, pc: 202] local: this index: 0 type: X
				        [pc: 2, pc: 202] local: a index: 1 type: int
				        [pc: 4, pc: 145] local: warnings index: 2 type: java.lang.Object
				        [pc: 6, pc: 145] local: contexts index: 3 type: java.lang.Object
				        [pc: 63, pc: 118] local: npe index: 4 type: java.lang.NullPointerException
				        [pc: 146, pc: 172] local: e index: 2 type: java.lang.Exception
				        [pc: 38, pc: 60] local: var index: 7 type: int
				        [pc: 95, pc: 117] local: var index: 7 type: int
				        [pc: 149, pc: 171] local: var index: 7 type: int
				        [pc: 177, pc: 199] local: var index: 7 type: int
				      Stack map table: number of frames 4
				        [pc: 61, full, stack: {java.lang.NullPointerException}, locals: {X, int, java.lang.Object, java.lang.Object}]
				        [pc: 118, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				        [pc: 145, full, stack: {java.lang.Exception}, locals: {X, int}]
				        [pc: 172, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				""";
		}
	}

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=129305
public void test044() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							foo();
						} \s
						static Object foo() {
							try {
								return null;
							} catch(Exception e) {
								return null;
							} finally {
								System.out.println("SUCCESS");
							}
						}
					}
					""",
			},
			"SUCCESS");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode ?
			"""
				  // Method descriptor #19 ()Ljava/lang/Object;
				  // Stack: 2, Locals: 3
				  static java.lang.Object foo();
				     0  jsr 15
				     3  aconst_null
				     4  areturn
				     5  astore_0 [e]
				     6  goto 0
				     9  astore_2
				    10  jsr 15
				    13  aload_2
				    14  athrow
				    15  astore_1
				    16  getstatic java.lang.System.out : java.io.PrintStream [22]
				    19  ldc <String "SUCCESS"> [28]
				    21  invokevirtual java.io.PrintStream.println(java.lang.String) : void [30]
				    24  ret 1
				      Exception Table:
				        [pc: 0, pc: 3] -> 5 when : java.lang.Exception
				        [pc: 0, pc: 3] -> 9 when : any
				        [pc: 5, pc: 9] -> 9 when : any
				      Line numbers:
				        [pc: 0, line: 7]
				        [pc: 5, line: 8]
				        [pc: 6, line: 9]
				        [pc: 9, line: 10]
				        [pc: 13, line: 12]
				        [pc: 15, line: 10]
				        [pc: 16, line: 11]
				        [pc: 24, line: 12]
				      Local variable table:
				        [pc: 6, pc: 9] local: e index: 0 type: java.lang.Exception
				"""
		: null;

	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput =
				"""
					  // Method descriptor #19 ()Ljava/lang/Object;
					  // Stack: 2, Locals: 0
					  static java.lang.Object foo();
					     0  getstatic java.lang.System.out : java.io.PrintStream [22]
					     3  ldc <String "SUCCESS"> [28]
					     5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [30]
					     8  aconst_null
					     9  areturn
					      Line numbers:
					        [pc: 0, line: 11]
					        [pc: 8, line: 7]
					""";
		} else {
			expectedOutput = """
				  // Method descriptor #19 ()Ljava/lang/Object;
				  // Stack: 2, Locals: 0
				  static java.lang.Object foo();
				     0  getstatic java.lang.System.out : java.io.PrintStream [22]
				     3  ldc <String "SUCCESS"> [28]
				     5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [30]
				     8  aconst_null
				     9  areturn
				      Line numbers:
				        [pc: 0, line: 11]
				        [pc: 8, line: 7]
				""";
		}
	}
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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=129306
public void test045() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void save() {
							try {
								Object warnings = null;
								Object contexts = null;
								try {
									System.out.print("[try]");
									System.out.print(warnings);\s
									return;
								} catch (NullPointerException npe) {
									System.out.print("[npe]");
									System.out.print(contexts);\s
									return;
								}
							} catch (Exception e) {
								System.out.print("[e]");
								return;
							} finally {\s
								int var = 0;
								System.out.print("[finally]");
								Object o = null;
								o.toString();
							}
						}
						public static void main(String[] args) {
							try {
								new X().save();
							} catch(NullPointerException e) {
								System.out.println("[caught npe]");
							}
						}
					}
					""",
			},
			"[try]null[finally][caught npe]");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 8
			  public void save();
			     0  aconst_null
			     1  astore_1 [warnings]
			     2  aconst_null
			     3  astore_2 [contexts]
			     4  getstatic java.lang.System.out : java.io.PrintStream [15]
			     7  ldc <String "[try]"> [21]
			     9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
			    12  getstatic java.lang.System.out : java.io.PrintStream [15]
			    15  aload_1 [warnings]
			    16  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]
			    19  jsr 62
			    22  return
			    23  astore_3 [npe]
			    24  getstatic java.lang.System.out : java.io.PrintStream [15]
			    27  ldc <String "[npe]"> [32]
			    29  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
			    32  getstatic java.lang.System.out : java.io.PrintStream [15]
			    35  aload_2 [contexts]
			    36  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]
			    39  goto 19
			    42  astore_1 [e]
			    43  getstatic java.lang.System.out : java.io.PrintStream [15]
			    46  ldc <String "[e]"> [34]
			    48  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
			    51  goto 19
			    54  astore 5
			    56  jsr 62
			    59  aload 5
			    61  athrow
			    62  astore 4
			    64  iconst_0
			    65  istore 6 [var]
			    67  getstatic java.lang.System.out : java.io.PrintStream [15]
			    70  ldc <String "[finally]"> [36]
			    72  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
			    75  aconst_null
			    76  astore 7 [o]
			    78  aload 7 [o]
			    80  invokevirtual java.lang.Object.toString() : java.lang.String [38]
			    83  pop
			    84  ret 4
			      Exception Table:
			        [pc: 4, pc: 19] -> 23 when : java.lang.NullPointerException
			        [pc: 0, pc: 22] -> 42 when : java.lang.Exception
			        [pc: 23, pc: 42] -> 42 when : java.lang.Exception
			        [pc: 0, pc: 22] -> 54 when : any
			        [pc: 23, pc: 54] -> 54 when : any
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 2, line: 5]
			        [pc: 4, line: 7]
			        [pc: 12, line: 8]
			        [pc: 19, line: 9]
			        [pc: 23, line: 10]
			        [pc: 24, line: 11]
			        [pc: 32, line: 12]
			        [pc: 39, line: 13]
			        [pc: 42, line: 15]
			        [pc: 43, line: 16]
			        [pc: 51, line: 17]
			        [pc: 54, line: 18]
			        [pc: 59, line: 23]
			        [pc: 62, line: 18]
			        [pc: 64, line: 19]
			        [pc: 67, line: 20]
			        [pc: 75, line: 21]
			        [pc: 78, line: 22]
			        [pc: 84, line: 23]
			      Local variable table:
			        [pc: 0, pc: 86] local: this index: 0 type: X
			        [pc: 2, pc: 42] local: warnings index: 1 type: java.lang.Object
			        [pc: 4, pc: 42] local: contexts index: 2 type: java.lang.Object
			        [pc: 24, pc: 42] local: npe index: 3 type: java.lang.NullPointerException
			        [pc: 43, pc: 54] local: e index: 1 type: java.lang.Exception
			        [pc: 67, pc: 84] local: var index: 6 type: int
			        [pc: 78, pc: 84] local: o index: 7 type: java.lang.Object
			"""
		: null;

	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = """
				  // Method descriptor #6 ()V
				  // Stack: 2, Locals: 7
				  public void save();
				      0  aconst_null
				      1  astore_1 [warnings]
				      2  aconst_null
				      3  astore_2 [contexts]
				      4  getstatic java.lang.System.out : java.io.PrintStream [15]
				      7  ldc <String "[try]"> [21]
				      9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     12  getstatic java.lang.System.out : java.io.PrintStream [15]
				     15  aload_1 [warnings]
				     16  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]
				     19  iconst_0
				     20  istore 5 [var]
				     22  getstatic java.lang.System.out : java.io.PrintStream [15]
				     25  ldc <String "[finally]"> [32]
				     27  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     30  aconst_null
				     31  astore 6 [o]
				     33  aload 6 [o]
				     35  invokevirtual java.lang.Object.toString() : java.lang.String [34]
				     38  pop
				     39  return
				     40  astore_3 [npe]
				     41  getstatic java.lang.System.out : java.io.PrintStream [15]
				     44  ldc <String "[npe]"> [38]
				     46  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     49  getstatic java.lang.System.out : java.io.PrintStream [15]
				     52  aload_2 [contexts]
				     53  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]
				     56  iconst_0
				     57  istore 5 [var]
				     59  getstatic java.lang.System.out : java.io.PrintStream [15]
				     62  ldc <String "[finally]"> [32]
				     64  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     67  aconst_null
				     68  astore 6 [o]
				     70  aload 6 [o]
				     72  invokevirtual java.lang.Object.toString() : java.lang.String [34]
				     75  pop
				     76  return
				     77  astore_1 [e]
				     78  getstatic java.lang.System.out : java.io.PrintStream [15]
				     81  ldc <String "[e]"> [40]
				     83  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     86  iconst_0
				     87  istore 5 [var]
				     89  getstatic java.lang.System.out : java.io.PrintStream [15]
				     92  ldc <String "[finally]"> [32]
				     94  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     97  aconst_null
				     98  astore 6 [o]
				    100  aload 6 [o]
				    102  invokevirtual java.lang.Object.toString() : java.lang.String [34]
				    105  pop
				    106  return
				    107  astore 4
				    109  iconst_0
				    110  istore 5 [var]
				    112  getstatic java.lang.System.out : java.io.PrintStream [15]
				    115  ldc <String "[finally]"> [32]
				    117  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				    120  aconst_null
				    121  astore 6 [o]
				    123  aload 6 [o]
				    125  invokevirtual java.lang.Object.toString() : java.lang.String [34]
				    128  pop
				    129  aload 4
				    131  athrow
				      Exception Table:
				        [pc: 4, pc: 19] -> 40 when : java.lang.NullPointerException
				        [pc: 0, pc: 19] -> 77 when : java.lang.Exception
				        [pc: 40, pc: 56] -> 77 when : java.lang.Exception
				        [pc: 0, pc: 19] -> 107 when : any
				        [pc: 40, pc: 56] -> 107 when : any
				        [pc: 77, pc: 86] -> 107 when : any
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 2, line: 5]
				        [pc: 4, line: 7]
				        [pc: 12, line: 8]
				        [pc: 19, line: 19]
				        [pc: 22, line: 20]
				        [pc: 30, line: 21]
				        [pc: 33, line: 22]
				        [pc: 39, line: 9]
				        [pc: 40, line: 10]
				        [pc: 41, line: 11]
				        [pc: 49, line: 12]
				        [pc: 56, line: 19]
				        [pc: 59, line: 20]
				        [pc: 67, line: 21]
				        [pc: 70, line: 22]
				        [pc: 76, line: 13]
				        [pc: 77, line: 15]
				        [pc: 78, line: 16]
				        [pc: 86, line: 19]
				        [pc: 89, line: 20]
				        [pc: 97, line: 21]
				        [pc: 100, line: 22]
				        [pc: 106, line: 17]
				        [pc: 107, line: 18]
				        [pc: 109, line: 19]
				        [pc: 112, line: 20]
				        [pc: 120, line: 21]
				        [pc: 123, line: 22]
				        [pc: 129, line: 23]
				      Local variable table:
				        [pc: 0, pc: 132] local: this index: 0 type: X
				        [pc: 2, pc: 77] local: warnings index: 1 type: java.lang.Object
				        [pc: 4, pc: 77] local: contexts index: 2 type: java.lang.Object
				        [pc: 41, pc: 77] local: npe index: 3 type: java.lang.NullPointerException
				        [pc: 78, pc: 107] local: e index: 1 type: java.lang.Exception
				        [pc: 22, pc: 39] local: var index: 5 type: int
				        [pc: 59, pc: 76] local: var index: 5 type: int
				        [pc: 89, pc: 106] local: var index: 5 type: int
				        [pc: 112, pc: 129] local: var index: 5 type: int
				        [pc: 33, pc: 39] local: o index: 6 type: java.lang.Object
				        [pc: 70, pc: 76] local: o index: 6 type: java.lang.Object
				        [pc: 100, pc: 106] local: o index: 6 type: java.lang.Object
				        [pc: 123, pc: 129] local: o index: 6 type: java.lang.Object
				""";
		} else {
			expectedOutput = """
				  // Method descriptor #6 ()V
				  // Stack: 2, Locals: 7
				  public void save();
				      0  aconst_null
				      1  astore_1 [warnings]
				      2  aconst_null
				      3  astore_2 [contexts]
				      4  getstatic java.lang.System.out : java.io.PrintStream [15]
				      7  ldc <String "[try]"> [21]
				      9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     12  getstatic java.lang.System.out : java.io.PrintStream [15]
				     15  aload_1 [warnings]
				     16  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]
				     19  iconst_0
				     20  istore 5 [var]
				     22  getstatic java.lang.System.out : java.io.PrintStream [15]
				     25  ldc <String "[finally]"> [32]
				     27  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     30  aconst_null
				     31  astore 6 [o]
				     33  aload 6 [o]
				     35  invokevirtual java.lang.Object.toString() : java.lang.String [34]
				     38  pop
				     39  return
				     40  astore_3 [npe]
				     41  getstatic java.lang.System.out : java.io.PrintStream [15]
				     44  ldc <String "[npe]"> [38]
				     46  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     49  getstatic java.lang.System.out : java.io.PrintStream [15]
				     52  aload_2 [contexts]
				     53  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]
				     56  iconst_0
				     57  istore 5 [var]
				     59  getstatic java.lang.System.out : java.io.PrintStream [15]
				     62  ldc <String "[finally]"> [32]
				     64  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     67  aconst_null
				     68  astore 6 [o]
				     70  aload 6 [o]
				     72  invokevirtual java.lang.Object.toString() : java.lang.String [34]
				     75  pop
				     76  return
				     77  astore_1 [e]
				     78  getstatic java.lang.System.out : java.io.PrintStream [15]
				     81  ldc <String "[e]"> [40]
				     83  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     86  iconst_0
				     87  istore 5 [var]
				     89  getstatic java.lang.System.out : java.io.PrintStream [15]
				     92  ldc <String "[finally]"> [32]
				     94  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				     97  aconst_null
				     98  astore 6 [o]
				    100  aload 6 [o]
				    102  invokevirtual java.lang.Object.toString() : java.lang.String [34]
				    105  pop
				    106  return
				    107  astore 4
				    109  iconst_0
				    110  istore 5 [var]
				    112  getstatic java.lang.System.out : java.io.PrintStream [15]
				    115  ldc <String "[finally]"> [32]
				    117  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]
				    120  aconst_null
				    121  astore 6 [o]
				    123  aload 6 [o]
				    125  invokevirtual java.lang.Object.toString() : java.lang.String [34]
				    128  pop
				    129  aload 4
				    131  athrow
				      Exception Table:
				        [pc: 4, pc: 19] -> 40 when : java.lang.NullPointerException
				        [pc: 0, pc: 19] -> 77 when : java.lang.Exception
				        [pc: 40, pc: 56] -> 77 when : java.lang.Exception
				        [pc: 0, pc: 19] -> 107 when : any
				        [pc: 40, pc: 56] -> 107 when : any
				        [pc: 77, pc: 86] -> 107 when : any
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 2, line: 5]
				        [pc: 4, line: 7]
				        [pc: 12, line: 8]
				        [pc: 19, line: 19]
				        [pc: 22, line: 20]
				        [pc: 30, line: 21]
				        [pc: 33, line: 22]
				        [pc: 39, line: 9]
				        [pc: 40, line: 10]
				        [pc: 41, line: 11]
				        [pc: 49, line: 12]
				        [pc: 56, line: 19]
				        [pc: 59, line: 20]
				        [pc: 67, line: 21]
				        [pc: 70, line: 22]
				        [pc: 76, line: 13]
				        [pc: 77, line: 15]
				        [pc: 78, line: 16]
				        [pc: 86, line: 19]
				        [pc: 89, line: 20]
				        [pc: 97, line: 21]
				        [pc: 100, line: 22]
				        [pc: 106, line: 17]
				        [pc: 107, line: 18]
				        [pc: 109, line: 19]
				        [pc: 112, line: 20]
				        [pc: 120, line: 21]
				        [pc: 123, line: 22]
				        [pc: 129, line: 23]
				      Local variable table:
				        [pc: 0, pc: 132] local: this index: 0 type: X
				        [pc: 2, pc: 77] local: warnings index: 1 type: java.lang.Object
				        [pc: 4, pc: 77] local: contexts index: 2 type: java.lang.Object
				        [pc: 41, pc: 77] local: npe index: 3 type: java.lang.NullPointerException
				        [pc: 78, pc: 107] local: e index: 1 type: java.lang.Exception
				        [pc: 22, pc: 39] local: var index: 5 type: int
				        [pc: 59, pc: 76] local: var index: 5 type: int
				        [pc: 89, pc: 106] local: var index: 5 type: int
				        [pc: 112, pc: 129] local: var index: 5 type: int
				        [pc: 33, pc: 39] local: o index: 6 type: java.lang.Object
				        [pc: 70, pc: 76] local: o index: 6 type: java.lang.Object
				        [pc: 100, pc: 106] local: o index: 6 type: java.lang.Object
				        [pc: 123, pc: 129] local: o index: 6 type: java.lang.Object
				      Stack map table: number of frames 3
				        [pc: 40, full, stack: {java.lang.NullPointerException}, locals: {X, java.lang.Object, java.lang.Object}]
				        [pc: 77, full, stack: {java.lang.Exception}, locals: {X}]
				        [pc: 107, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				""";
		}
	}

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108180
public void test046() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static Object sanityCheckBug() {
					        Object obj;
					        try {
					            obj = new Object();
					            return obj;
					        } finally {
					             obj = null;
					        }
					    }
					    public static void main(String[] arguments) {
							X.sanityCheckBug();
					    }
					}
					""",
			},
			"");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"""
			  // Method descriptor #15 ()Ljava/lang/Object;
			  // Stack: 2, Locals: 4
			  public static java.lang.Object sanityCheckBug();
			     0  new java.lang.Object [3]
			     3  dup
			     4  invokespecial java.lang.Object() [8]
			     7  astore_0 [obj]
			     8  aload_0 [obj]
			     9  astore_3
			    10  jsr 21
			    13  aload_3
			    14  areturn
			    15  astore_2
			    16  jsr 21
			    19  aload_2
			    20  athrow
			    21  astore_1
			    22  aconst_null
			    23  astore_0 [obj]
			    24  ret 1
			      Exception Table:
			        [pc: 0, pc: 13] -> 15 when : any
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 8, line: 6]
			        [pc: 15, line: 7]
			        [pc: 19, line: 9]
			        [pc: 21, line: 7]
			        [pc: 22, line: 8]
			        [pc: 24, line: 9]
			      Local variable table:
			        [pc: 8, pc: 15] local: obj index: 0 type: java.lang.Object
			        [pc: 24, pc: 26] local: obj index: 0 type: java.lang.Object
			"""
		: null;

	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = """
				  // Method descriptor #15 ()Ljava/lang/Object;
				  // Stack: 2, Locals: 3
				  public static java.lang.Object sanityCheckBug();
				     0  new java.lang.Object [3]
				     3  dup
				     4  invokespecial java.lang.Object() [8]
				     7  astore_0 [obj]
				     8  aload_0 [obj]
				     9  astore_2
				    10  aconst_null
				    11  astore_0 [obj]
				    12  aload_2
				    13  areturn
				    14  astore_1
				    15  aconst_null
				    16  astore_0 [obj]
				    17  aload_1
				    18  athrow
				      Exception Table:
				        [pc: 0, pc: 10] -> 14 when : any
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 8, line: 6]
				        [pc: 10, line: 8]
				        [pc: 12, line: 6]
				        [pc: 14, line: 7]
				        [pc: 15, line: 8]
				        [pc: 17, line: 9]
				      Local variable table:
				        [pc: 8, pc: 14] local: obj index: 0 type: java.lang.Object
				        [pc: 17, pc: 19] local: obj index: 0 type: java.lang.Object
				""";
		} else {
			expectedOutput = """
				  // Method descriptor #15 ()Ljava/lang/Object;
				  // Stack: 2, Locals: 3
				  public static java.lang.Object sanityCheckBug();
				     0  new java.lang.Object [3]
				     3  dup
				     4  invokespecial java.lang.Object() [8]
				     7  astore_0 [obj]
				     8  aload_0 [obj]
				     9  astore_2
				    10  aconst_null
				    11  astore_0 [obj]
				    12  aload_2
				    13  areturn
				    14  astore_1
				    15  aconst_null
				    16  astore_0 [obj]
				    17  aload_1
				    18  athrow
				      Exception Table:
				        [pc: 0, pc: 10] -> 14 when : any
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 8, line: 6]
				        [pc: 10, line: 8]
				        [pc: 12, line: 6]
				        [pc: 14, line: 7]
				        [pc: 15, line: 8]
				        [pc: 17, line: 9]
				      Local variable table:
				        [pc: 8, pc: 14] local: obj index: 0 type: java.lang.Object
				        [pc: 17, pc: 19] local: obj index: 0 type: java.lang.Object
				""";
		}
	}

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
}
public void test047() {
	if (new CompilerOptions(getCompilerOptions()).complianceLevel <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						        public static void main(String[] args) {
						                try {
											if (false) throw null;
											throw new Object();
						                } catch(Object o) {
						                }
						        }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						if (false) throw null;
						                 ^^^^
					Cannot throw null as an exception
					----------
					2. ERROR in X.java (at line 5)
						throw new Object();
						      ^^^^^^^^^^^^
					No exception of type Object can be thrown; an exception type must be a subclass of Throwable
					----------
					3. ERROR in X.java (at line 6)
						} catch(Object o) {
						        ^^^^^^
					No exception of type Object can be thrown; an exception type must be a subclass of Throwable
					----------
					""");
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args) {
				                try {
									if (false) throw null;
									throw new Object();
				                } catch(Object o) {
				                }
				        }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				throw new Object();
				      ^^^^^^^^^^^^
			No exception of type Object can be thrown; an exception type must be a subclass of Throwable
			----------
			2. ERROR in X.java (at line 6)
				} catch(Object o) {
				        ^^^^^^
			No exception of type Object can be thrown; an exception type must be a subclass of Throwable
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894
public void test048() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						boolean bool() { return true; }
						void foo() {
							try {
								if (bool()) {
									return;
								}
							} catch (Exception e) {
							}
						}
						int foo2() {
							try {
								while (bool()) {
									return 0;
								}
							} catch (Exception e) {
							}
							return 1;
						}
						long foo3() {
							try {
								do {
									if (true) return 0L;
								} while (bool());
							} catch (Exception e) {
							}
							return 1L;
						}\t
						float foo4() {
							try {
								for (int i  = 0; bool(); i++) {
									return 0.0F;
								}
							} catch (Exception e) {
							}
							return 1.0F;
						}	\t
						double bar() {
							if (bool()) {
								if (bool())
									return 0.0;
							} else {
								if (bool()) {
									throw new NullPointerException();
								}
							}
							return 1.0;
						}
						void baz(int i) {
							if (bool()) {
								switch(i) {
									case 0 : return;
									default : break;
								}
							} else {
								bool();
							}
						}
					}
					""",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"""
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 2
			  void foo();
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 9
			     7  return
			     8  astore_1
			     9  return
			      Exception Table:
			        [pc: 0, pc: 7] -> 8 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 7, line: 6]
			        [pc: 8, line: 8]
			        [pc: 9, line: 10]
			      Local variable table:
			        [pc: 0, pc: 10] local: this index: 0 type: X
			 \s
			  // Method descriptor #22 ()I
			  // Stack: 1, Locals: 2
			  int foo2();
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 10
			     7  iconst_0
			     8  ireturn
			     9  astore_1
			    10  iconst_1
			    11  ireturn
			      Exception Table:
			        [pc: 0, pc: 7] -> 9 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 13]
			        [pc: 7, line: 14]
			        [pc: 9, line: 16]
			        [pc: 10, line: 18]
			      Local variable table:
			        [pc: 0, pc: 12] local: this index: 0 type: X
			 \s
			  // Method descriptor #24 ()J
			  // Stack: 2, Locals: 1
			  long foo3();
			    0  lconst_0
			    1  lreturn
			    2  lconst_1
			    3  lreturn
			      Line numbers:
			        [pc: 0, line: 23]
			        [pc: 2, line: 27]
			      Local variable table:
			        [pc: 0, pc: 4] local: this index: 0 type: X
			 \s
			  // Method descriptor #26 ()F
			  // Stack: 1, Locals: 2
			  float foo4();
			     0  iconst_0
			     1  istore_1 [i]
			     2  aload_0 [this]
			     3  invokevirtual X.bool() : boolean [17]
			     6  ifeq 12
			     9  fconst_0
			    10  freturn
			    11  astore_1
			    12  fconst_1
			    13  freturn
			      Exception Table:
			        [pc: 0, pc: 9] -> 11 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 31]
			        [pc: 9, line: 32]
			        [pc: 11, line: 34]
			        [pc: 12, line: 36]
			      Local variable table:
			        [pc: 0, pc: 14] local: this index: 0 type: X
			        [pc: 2, pc: 11] local: i index: 1 type: int
			 \s
			  // Method descriptor #30 ()D
			  // Stack: 2, Locals: 1
			  double bar();
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 16
			     7  aload_0 [this]
			     8  invokevirtual X.bool() : boolean [17]
			    11  ifeq 31
			    14  dconst_0
			    15  dreturn
			    16  aload_0 [this]
			    17  invokevirtual X.bool() : boolean [17]
			    20  ifeq 31
			    23  new java.lang.NullPointerException [31]
			    26  dup
			    27  invokespecial java.lang.NullPointerException() [33]
			    30  athrow
			    31  dconst_1
			    32  dreturn
			      Line numbers:
			        [pc: 0, line: 39]
			        [pc: 7, line: 40]
			        [pc: 14, line: 41]
			        [pc: 16, line: 43]
			        [pc: 23, line: 44]
			        [pc: 31, line: 47]
			      Local variable table:
			        [pc: 0, pc: 33] local: this index: 0 type: X
			 \s
			  // Method descriptor #35 (I)V
			  // Stack: 1, Locals: 2
			  void baz(int i);
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 32
			     7  iload_1 [i]
			     8  tableswitch default: 29
			          case 0: 28
			    28  return
			    29  goto 37
			    32  aload_0 [this]
			    33  invokevirtual X.bool() : boolean [17]
			    36  pop
			    37  return
			      Line numbers:
			        [pc: 0, line: 50]
			        [pc: 7, line: 51]
			        [pc: 28, line: 52]
			        [pc: 29, line: 55]
			        [pc: 32, line: 56]
			        [pc: 37, line: 58]
			      Local variable table:
			        [pc: 0, pc: 38] local: this index: 0 type: X
			        [pc: 0, pc: 38] local: i index: 1 type: int
			"""
		:
			"""
				  // Method descriptor #6 ()V
				  // Stack: 1, Locals: 2
				  void foo();
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 9
				     7  return
				     8  astore_1
				     9  return
				      Exception Table:
				        [pc: 0, pc: 7] -> 8 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 7, line: 6]
				        [pc: 8, line: 8]
				        [pc: 9, line: 10]
				      Local variable table:
				        [pc: 0, pc: 10] local: this index: 0 type: X
				      Stack map table: number of frames 2
				        [pc: 8, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 9, same]
				 \s
				  // Method descriptor #23 ()I
				  // Stack: 1, Locals: 2
				  int foo2();
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 10
				     7  iconst_0
				     8  ireturn
				     9  astore_1
				    10  iconst_1
				    11  ireturn
				      Exception Table:
				        [pc: 0, pc: 7] -> 9 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 13]
				        [pc: 7, line: 14]
				        [pc: 9, line: 16]
				        [pc: 10, line: 18]
				      Local variable table:
				        [pc: 0, pc: 12] local: this index: 0 type: X
				      Stack map table: number of frames 2
				        [pc: 9, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 10, same]
				 \s
				  // Method descriptor #25 ()J
				  // Stack: 2, Locals: 1
				  long foo3();
				    0  lconst_0
				    1  lreturn
				    2  lconst_1
				    3  lreturn
				      Line numbers:
				        [pc: 0, line: 23]
				        [pc: 2, line: 27]
				      Local variable table:
				        [pc: 0, pc: 4] local: this index: 0 type: X
				      Stack map table: number of frames 1
				        [pc: 2, same]
				 \s
				  // Method descriptor #27 ()F
				  // Stack: 1, Locals: 2
				  float foo4();
				     0  iconst_0
				     1  istore_1 [i]
				     2  aload_0 [this]
				     3  invokevirtual X.bool() : boolean [17]
				     6  ifeq 12
				     9  fconst_0
				    10  freturn
				    11  astore_1
				    12  fconst_1
				    13  freturn
				      Exception Table:
				        [pc: 0, pc: 9] -> 11 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 31]
				        [pc: 9, line: 32]
				        [pc: 11, line: 34]
				        [pc: 12, line: 36]
				      Local variable table:
				        [pc: 0, pc: 14] local: this index: 0 type: X
				        [pc: 2, pc: 11] local: i index: 1 type: int
				      Stack map table: number of frames 2
				        [pc: 11, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 12, same]
				 \s
				  // Method descriptor #31 ()D
				  // Stack: 2, Locals: 1
				  double bar();
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 16
				     7  aload_0 [this]
				     8  invokevirtual X.bool() : boolean [17]
				    11  ifeq 31
				    14  dconst_0
				    15  dreturn
				    16  aload_0 [this]
				    17  invokevirtual X.bool() : boolean [17]
				    20  ifeq 31
				    23  new java.lang.NullPointerException [32]
				    26  dup
				    27  invokespecial java.lang.NullPointerException() [34]
				    30  athrow
				    31  dconst_1
				    32  dreturn
				      Line numbers:
				        [pc: 0, line: 39]
				        [pc: 7, line: 40]
				        [pc: 14, line: 41]
				        [pc: 16, line: 43]
				        [pc: 23, line: 44]
				        [pc: 31, line: 47]
				      Local variable table:
				        [pc: 0, pc: 33] local: this index: 0 type: X
				      Stack map table: number of frames 2
				        [pc: 16, same]
				        [pc: 31, same]
				 \s
				  // Method descriptor #36 (I)V
				  // Stack: 1, Locals: 2
				  void baz(int i);
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 32
				     7  iload_1 [i]
				     8  tableswitch default: 29
				          case 0: 28
				    28  return
				    29  goto 37
				    32  aload_0 [this]
				    33  invokevirtual X.bool() : boolean [17]
				    36  pop
				    37  return
				      Line numbers:
				        [pc: 0, line: 50]
				        [pc: 7, line: 51]
				        [pc: 28, line: 52]
				        [pc: 29, line: 55]
				        [pc: 32, line: 56]
				        [pc: 37, line: 58]
				      Local variable table:
				        [pc: 0, pc: 38] local: this index: 0 type: X
				        [pc: 0, pc: 38] local: i index: 1 type: int
				      Stack map table: number of frames 4
				        [pc: 28, same]
				        [pc: 29, same]
				        [pc: 32, same]
				        [pc: 37, same]
				""";
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
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test049() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						boolean bool() { return true; }
						void foo() {
							try {
								if (bool()) {
									throw new NullPointerException();
								}
							} catch (Exception e) {
							}
						}
						void foo2() {
							try {
								while (bool()) {
									throw new NullPointerException();
								}
							} catch (Exception e) {
							}
						}
						void foo3() {
							try {
								do {
									if (true) throw new NullPointerException();
								} while (bool());
							} catch (Exception e) {
							}
						}\t
						void foo4() {
							try {
								for (int i  = 0; bool(); i++) {
									throw new NullPointerException();
								}
							} catch (Exception e) {
							}
						}	\t
						void bar() {
							if (bool()) {
								if (bool())
									throw new NullPointerException();
							} else {
								if (bool()) {
									throw new NullPointerException();
								}
							}
						}
						void baz(int i) {
							if (bool()) {
								switch(i) {
									case 0 : throw new NullPointerException();
									default : break;
								}
							} else {
								bool();
							}
						}
					}
					""",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 2
			  void foo();
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 16
			     7  new java.lang.NullPointerException [19]
			    10  dup
			    11  invokespecial java.lang.NullPointerException() [21]
			    14  athrow
			    15  astore_1
			    16  return
			      Exception Table:
			        [pc: 0, pc: 15] -> 15 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 7, line: 6]
			        [pc: 15, line: 8]
			        [pc: 16, line: 10]
			      Local variable table:
			        [pc: 0, pc: 17] local: this index: 0 type: X
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 2
			  void foo2();
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 16
			     7  new java.lang.NullPointerException [19]
			    10  dup
			    11  invokespecial java.lang.NullPointerException() [21]
			    14  athrow
			    15  astore_1
			    16  return
			      Exception Table:
			        [pc: 0, pc: 15] -> 15 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 13]
			        [pc: 7, line: 14]
			        [pc: 15, line: 16]
			        [pc: 16, line: 18]
			      Local variable table:
			        [pc: 0, pc: 17] local: this index: 0 type: X
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 2
			  void foo3();
			     0  new java.lang.NullPointerException [19]
			     3  dup
			     4  invokespecial java.lang.NullPointerException() [21]
			     7  athrow
			     8  astore_1
			     9  return
			      Exception Table:
			        [pc: 0, pc: 8] -> 8 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 22]
			        [pc: 8, line: 24]
			        [pc: 9, line: 26]
			      Local variable table:
			        [pc: 0, pc: 10] local: this index: 0 type: X
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 2
			  void foo4();
			     0  iconst_0
			     1  istore_1 [i]
			     2  aload_0 [this]
			     3  invokevirtual X.bool() : boolean [17]
			     6  ifeq 18
			     9  new java.lang.NullPointerException [19]
			    12  dup
			    13  invokespecial java.lang.NullPointerException() [21]
			    16  athrow
			    17  astore_1
			    18  return
			      Exception Table:
			        [pc: 0, pc: 17] -> 17 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 29]
			        [pc: 9, line: 30]
			        [pc: 17, line: 32]
			        [pc: 18, line: 34]
			      Local variable table:
			        [pc: 0, pc: 19] local: this index: 0 type: X
			        [pc: 2, pc: 17] local: i index: 1 type: int
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  void bar();
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 22
			     7  aload_0 [this]
			     8  invokevirtual X.bool() : boolean [17]
			    11  ifeq 37
			    14  new java.lang.NullPointerException [19]
			    17  dup
			    18  invokespecial java.lang.NullPointerException() [21]
			    21  athrow
			    22  aload_0 [this]
			    23  invokevirtual X.bool() : boolean [17]
			    26  ifeq 37
			    29  new java.lang.NullPointerException [19]
			    32  dup
			    33  invokespecial java.lang.NullPointerException() [21]
			    36  athrow
			    37  return
			      Line numbers:
			        [pc: 0, line: 36]
			        [pc: 7, line: 37]
			        [pc: 14, line: 38]
			        [pc: 22, line: 40]
			        [pc: 29, line: 41]
			        [pc: 37, line: 44]
			      Local variable table:
			        [pc: 0, pc: 38] local: this index: 0 type: X
			 \s
			  // Method descriptor #31 (I)V
			  // Stack: 2, Locals: 2
			  void baz(int i);
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 39
			     7  iload_1 [i]
			     8  tableswitch default: 36
			          case 0: 28
			    28  new java.lang.NullPointerException [19]
			    31  dup
			    32  invokespecial java.lang.NullPointerException() [21]
			    35  athrow
			    36  goto 44
			    39  aload_0 [this]
			    40  invokevirtual X.bool() : boolean [17]
			    43  pop
			    44  return
			      Line numbers:
			        [pc: 0, line: 46]
			        [pc: 7, line: 47]
			        [pc: 28, line: 48]
			        [pc: 36, line: 51]
			        [pc: 39, line: 52]
			        [pc: 44, line: 54]
			      Local variable table:
			        [pc: 0, pc: 45] local: this index: 0 type: X
			        [pc: 0, pc: 45] local: i index: 1 type: int
			"""
		:
			"""
				  // Method descriptor #6 ()V
				  // Stack: 2, Locals: 2
				  void foo();
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 16
				     7  new java.lang.NullPointerException [19]
				    10  dup
				    11  invokespecial java.lang.NullPointerException() [21]
				    14  athrow
				    15  astore_1
				    16  return
				      Exception Table:
				        [pc: 0, pc: 15] -> 15 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 7, line: 6]
				        [pc: 15, line: 8]
				        [pc: 16, line: 10]
				      Local variable table:
				        [pc: 0, pc: 17] local: this index: 0 type: X
				      Stack map table: number of frames 2
				        [pc: 15, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 16, same]
				 \s
				  // Method descriptor #6 ()V
				  // Stack: 2, Locals: 2
				  void foo2();
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 16
				     7  new java.lang.NullPointerException [19]
				    10  dup
				    11  invokespecial java.lang.NullPointerException() [21]
				    14  athrow
				    15  astore_1
				    16  return
				      Exception Table:
				        [pc: 0, pc: 15] -> 15 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 13]
				        [pc: 7, line: 14]
				        [pc: 15, line: 16]
				        [pc: 16, line: 18]
				      Local variable table:
				        [pc: 0, pc: 17] local: this index: 0 type: X
				      Stack map table: number of frames 2
				        [pc: 15, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 16, same]
				 \s
				  // Method descriptor #6 ()V
				  // Stack: 2, Locals: 2
				  void foo3();
				     0  new java.lang.NullPointerException [19]
				     3  dup
				     4  invokespecial java.lang.NullPointerException() [21]
				     7  athrow
				     8  astore_1
				     9  return
				      Exception Table:
				        [pc: 0, pc: 8] -> 8 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 22]
				        [pc: 8, line: 24]
				        [pc: 9, line: 26]
				      Local variable table:
				        [pc: 0, pc: 10] local: this index: 0 type: X
				      Stack map table: number of frames 1
				        [pc: 8, same_locals_1_stack_item, stack: {java.lang.Exception}]
				 \s
				  // Method descriptor #6 ()V
				  // Stack: 2, Locals: 2
				  void foo4();
				     0  iconst_0
				     1  istore_1 [i]
				     2  aload_0 [this]
				     3  invokevirtual X.bool() : boolean [17]
				     6  ifeq 18
				     9  new java.lang.NullPointerException [19]
				    12  dup
				    13  invokespecial java.lang.NullPointerException() [21]
				    16  athrow
				    17  astore_1
				    18  return
				      Exception Table:
				        [pc: 0, pc: 17] -> 17 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 29]
				        [pc: 9, line: 30]
				        [pc: 17, line: 32]
				        [pc: 18, line: 34]
				      Local variable table:
				        [pc: 0, pc: 19] local: this index: 0 type: X
				        [pc: 2, pc: 17] local: i index: 1 type: int
				      Stack map table: number of frames 2
				        [pc: 17, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 18, same]
				 \s
				  // Method descriptor #6 ()V
				  // Stack: 2, Locals: 1
				  void bar();
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 22
				     7  aload_0 [this]
				     8  invokevirtual X.bool() : boolean [17]
				    11  ifeq 37
				    14  new java.lang.NullPointerException [19]
				    17  dup
				    18  invokespecial java.lang.NullPointerException() [21]
				    21  athrow
				    22  aload_0 [this]
				    23  invokevirtual X.bool() : boolean [17]
				    26  ifeq 37
				    29  new java.lang.NullPointerException [19]
				    32  dup
				    33  invokespecial java.lang.NullPointerException() [21]
				    36  athrow
				    37  return
				      Line numbers:
				        [pc: 0, line: 36]
				        [pc: 7, line: 37]
				        [pc: 14, line: 38]
				        [pc: 22, line: 40]
				        [pc: 29, line: 41]
				        [pc: 37, line: 44]
				      Local variable table:
				        [pc: 0, pc: 38] local: this index: 0 type: X
				      Stack map table: number of frames 2
				        [pc: 22, same]
				        [pc: 37, same]
				 \s
				  // Method descriptor #32 (I)V
				  // Stack: 2, Locals: 2
				  void baz(int i);
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 39
				     7  iload_1 [i]
				     8  tableswitch default: 36
				          case 0: 28
				    28  new java.lang.NullPointerException [19]
				    31  dup
				    32  invokespecial java.lang.NullPointerException() [21]
				    35  athrow
				    36  goto 44
				    39  aload_0 [this]
				    40  invokevirtual X.bool() : boolean [17]
				    43  pop
				    44  return
				      Line numbers:
				        [pc: 0, line: 46]
				        [pc: 7, line: 47]
				        [pc: 28, line: 48]
				        [pc: 36, line: 51]
				        [pc: 39, line: 52]
				        [pc: 44, line: 54]
				      Local variable table:
				        [pc: 0, pc: 45] local: this index: 0 type: X
				        [pc: 0, pc: 45] local: i index: 1 type: int
				      Stack map table: number of frames 4
				        [pc: 28, same]
				        [pc: 36, same]
				        [pc: 39, same]
				        [pc: 44, same]
				""";
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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test050() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						boolean bool() { return true; }
						void foo() {
							check: try {
								if (bool()) {
									break check;
								}
							} catch (Exception e) {
							}
						}
						void foo2() {
							check: try {
								while (bool()) {
									break check;
								}
							} catch (Exception e) {
							}
						}
						void foo3() {
							check: try {
								do {
									if (true) break check;
								} while (bool());
							} catch (Exception e) {
							}
						}\t
						void foo4() {
							check: try {
								for (int i  = 0; bool(); i++) {
									break check;
								}
							} catch (Exception e) {
							}
						}
						void bar() {
							check: if (bool()) {
								if (bool())
									break check;
							} else {
								if (bool()) {
									break check;
								}
							}
						}
						void baz(int i) {
							check: if (bool()) {
								switch(i) {
									case 0 : break check;
									default : break;
								}
							} else {
								bool();
							}
						}
					}
					""",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"""
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 2
			  void foo();
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 11
			     7  goto 11
			    10  astore_1
			    11  return
			      Exception Table:
			        [pc: 0, pc: 7] -> 10 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 7, line: 6]
			        [pc: 10, line: 8]
			        [pc: 11, line: 10]
			      Local variable table:
			        [pc: 0, pc: 12] local: this index: 0 type: X
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 2
			  void foo2();
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 11
			     7  goto 11
			    10  astore_1
			    11  return
			      Exception Table:
			        [pc: 0, pc: 7] -> 10 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 13]
			        [pc: 7, line: 14]
			        [pc: 10, line: 16]
			        [pc: 11, line: 18]
			      Local variable table:
			        [pc: 0, pc: 12] local: this index: 0 type: X
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  void foo3();
			    0  return
			      Line numbers:
			        [pc: 0, line: 26]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 2
			  void foo4();
			     0  iconst_0
			     1  istore_1 [i]
			     2  aload_0 [this]
			     3  invokevirtual X.bool() : boolean [17]
			     6  ifeq 13
			     9  goto 13
			    12  astore_1
			    13  return
			      Exception Table:
			        [pc: 0, pc: 9] -> 12 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 29]
			        [pc: 9, line: 30]
			        [pc: 12, line: 32]
			        [pc: 13, line: 34]
			      Local variable table:
			        [pc: 0, pc: 14] local: this index: 0 type: X
			        [pc: 2, pc: 12] local: i index: 1 type: int
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  void bar();
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 17
			     7  aload_0 [this]
			     8  invokevirtual X.bool() : boolean [17]
			    11  ifeq 24
			    14  goto 24
			    17  aload_0 [this]
			    18  invokevirtual X.bool() : boolean [17]
			    21  ifeq 24
			    24  return
			      Line numbers:
			        [pc: 0, line: 36]
			        [pc: 7, line: 37]
			        [pc: 14, line: 38]
			        [pc: 17, line: 40]
			        [pc: 24, line: 44]
			      Local variable table:
			        [pc: 0, pc: 25] local: this index: 0 type: X
			 \s
			  // Method descriptor #28 (I)V
			  // Stack: 1, Locals: 2
			  void baz(int i);
			     0  aload_0 [this]
			     1  invokevirtual X.bool() : boolean [17]
			     4  ifeq 34
			     7  iload_1 [i]
			     8  tableswitch default: 31
			          case 0: 28
			    28  goto 39
			    31  goto 39
			    34  aload_0 [this]
			    35  invokevirtual X.bool() : boolean [17]
			    38  pop
			    39  return
			      Line numbers:
			        [pc: 0, line: 46]
			        [pc: 7, line: 47]
			        [pc: 28, line: 48]
			        [pc: 31, line: 51]
			        [pc: 34, line: 52]
			        [pc: 39, line: 54]
			      Local variable table:
			        [pc: 0, pc: 40] local: this index: 0 type: X
			        [pc: 0, pc: 40] local: i index: 1 type: int
			"""
		:
			"""
				  // Method descriptor #6 ()V
				  // Stack: 1, Locals: 2
				  void foo();
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 11
				     7  goto 11
				    10  astore_1
				    11  return
				      Exception Table:
				        [pc: 0, pc: 7] -> 10 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 7, line: 6]
				        [pc: 10, line: 8]
				        [pc: 11, line: 10]
				      Local variable table:
				        [pc: 0, pc: 12] local: this index: 0 type: X
				      Stack map table: number of frames 2
				        [pc: 10, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 11, same]
				 \s
				  // Method descriptor #6 ()V
				  // Stack: 1, Locals: 2
				  void foo2();
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 11
				     7  goto 11
				    10  astore_1
				    11  return
				      Exception Table:
				        [pc: 0, pc: 7] -> 10 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 13]
				        [pc: 7, line: 14]
				        [pc: 10, line: 16]
				        [pc: 11, line: 18]
				      Local variable table:
				        [pc: 0, pc: 12] local: this index: 0 type: X
				      Stack map table: number of frames 2
				        [pc: 10, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 11, same]
				 \s
				  // Method descriptor #6 ()V
				  // Stack: 0, Locals: 1
				  void foo3();
				    0  return
				      Line numbers:
				        [pc: 0, line: 26]
				      Local variable table:
				        [pc: 0, pc: 1] local: this index: 0 type: X
				 \s
				  // Method descriptor #6 ()V
				  // Stack: 1, Locals: 2
				  void foo4();
				     0  iconst_0
				     1  istore_1 [i]
				     2  aload_0 [this]
				     3  invokevirtual X.bool() : boolean [17]
				     6  ifeq 13
				     9  goto 13
				    12  astore_1
				    13  return
				      Exception Table:
				        [pc: 0, pc: 9] -> 12 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 29]
				        [pc: 9, line: 30]
				        [pc: 12, line: 32]
				        [pc: 13, line: 34]
				      Local variable table:
				        [pc: 0, pc: 14] local: this index: 0 type: X
				        [pc: 2, pc: 12] local: i index: 1 type: int
				      Stack map table: number of frames 2
				        [pc: 12, same_locals_1_stack_item, stack: {java.lang.Exception}]
				        [pc: 13, same]
				 \s
				  // Method descriptor #6 ()V
				  // Stack: 1, Locals: 1
				  void bar();
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 17
				     7  aload_0 [this]
				     8  invokevirtual X.bool() : boolean [17]
				    11  ifeq 24
				    14  goto 24
				    17  aload_0 [this]
				    18  invokevirtual X.bool() : boolean [17]
				    21  ifeq 24
				    24  return
				      Line numbers:
				        [pc: 0, line: 36]
				        [pc: 7, line: 37]
				        [pc: 14, line: 38]
				        [pc: 17, line: 40]
				        [pc: 24, line: 44]
				      Local variable table:
				        [pc: 0, pc: 25] local: this index: 0 type: X
				      Stack map table: number of frames 2
				        [pc: 17, same]
				        [pc: 24, same]
				 \s
				  // Method descriptor #29 (I)V
				  // Stack: 1, Locals: 2
				  void baz(int i);
				     0  aload_0 [this]
				     1  invokevirtual X.bool() : boolean [17]
				     4  ifeq 34
				     7  iload_1 [i]
				     8  tableswitch default: 31
				          case 0: 28
				    28  goto 39
				    31  goto 39
				    34  aload_0 [this]
				    35  invokevirtual X.bool() : boolean [17]
				    38  pop
				    39  return
				      Line numbers:
				        [pc: 0, line: 46]
				        [pc: 7, line: 47]
				        [pc: 28, line: 48]
				        [pc: 31, line: 51]
				        [pc: 34, line: 52]
				        [pc: 39, line: 54]
				      Local variable table:
				        [pc: 0, pc: 40] local: this index: 0 type: X
				        [pc: 0, pc: 40] local: i index: 1 type: int
				      Stack map table: number of frames 4
				        [pc: 28, same]
				        [pc: 31, same]
				        [pc: 34, same]
				        [pc: 39, same]
				""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test051() throws Exception {
	String builder = this.complianceLevel >= ClassFileConstants.JDK1_5 ? "StringBuilder" : "StringBuffer";
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String argv[]) {\n" +
				"		System.out.print(new " + builder + "(\"[count=\").append(count()).append(\"]\").toString());\n" +
				"	}\n" +
				"	static int count() {\n" +
				"		int count = 0;\n" +
				"		try {\n" +
				"			for (int i = 0;;) {\n" +
				"				count++;\n" +
				"				if (i++ > 10) \n" +
				"					break; \n" +
				"			}\n" +
				"		} catch(Exception e) {\n" +
				"		}\n" +
				"		return count;\n" +
				"	}\n" +
				"}\n",
			},
			"[count=12]");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"""
			  // Method descriptor #32 ()I
			  // Stack: 2, Locals: 2
			  static int count();
			     0  iconst_0
			     1  istore_0 [count]
			     2  iconst_0
			     3  istore_1 [i]
			     4  iinc 0 1 [count]
			     7  iload_1 [i]
			     8  iinc 1 1 [i]
			    11  bipush 10
			    13  if_icmple 4
			    16  goto 20
			    19  astore_1
			    20  iload_0 [count]
			    21  ireturn
			      Exception Table:
			        [pc: 2, pc: 16] -> 19 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 2, line: 8]
			        [pc: 4, line: 9]
			        [pc: 7, line: 10]
			        [pc: 16, line: 13]
			        [pc: 20, line: 15]
			      Local variable table:
			        [pc: 2, pc: 22] local: count index: 0 type: int
			        [pc: 4, pc: 16] local: i index: 1 type: int
			"""
		:
			"""
				  // Method descriptor #32 ()I
				  // Stack: 2, Locals: 2
				  static int count();
				     0  iconst_0
				     1  istore_0 [count]
				     2  iconst_0
				     3  istore_1 [i]
				     4  iinc 0 1 [count]
				     7  iload_1 [i]
				     8  iinc 1 1 [i]
				    11  bipush 10
				    13  if_icmple 4
				    16  goto 20
				    19  astore_1
				    20  iload_0 [count]
				    21  ireturn
				      Exception Table:
				        [pc: 2, pc: 16] -> 19 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 6]
				        [pc: 2, line: 8]
				        [pc: 4, line: 9]
				        [pc: 7, line: 10]
				        [pc: 16, line: 13]
				        [pc: 20, line: 15]
				      Local variable table:
				        [pc: 2, pc: 22] local: count index: 0 type: int
				        [pc: 4, pc: 16] local: i index: 1 type: int
				      Stack map table: number of frames 3
				        [pc: 4, append: {int, int}]
				        [pc: 19, full, stack: {java.lang.Exception}, locals: {int}]
				        [pc: 20, same]
				""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test052() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String argv[]) {
							try {
								for (int i = 0; i < 0; i++)
									do ;  while (true);
							} catch(Exception e) {
							}
						}\s
					}
					""",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] argv);
			     0  iconst_0
			     1  istore_1 [i]
			     2  iload_1 [i]
			     3  ifge 10
			     6  goto 6
			     9  astore_1
			    10  return
			      Exception Table:
			        [pc: 0, pc: 9] -> 9 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 6, line: 5]
			        [pc: 9, line: 6]
			        [pc: 10, line: 8]
			      Local variable table:
			        [pc: 0, pc: 11] local: argv index: 0 type: java.lang.String[]
			        [pc: 2, pc: 9] local: i index: 1 type: int
			"""
		:
			"""
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 1, Locals: 2
				  public static void main(java.lang.String[] argv);
				     0  iconst_0
				     1  istore_1 [i]
				     2  iload_1 [i]
				     3  ifge 10
				     6  goto 6
				     9  astore_1
				    10  return
				      Exception Table:
				        [pc: 0, pc: 9] -> 9 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 6, line: 5]
				        [pc: 9, line: 6]
				        [pc: 10, line: 8]
				      Local variable table:
				        [pc: 0, pc: 11] local: argv index: 0 type: java.lang.String[]
				        [pc: 2, pc: 9] local: i index: 1 type: int
				      Stack map table: number of frames 3
				        [pc: 6, append: {int}]
				        [pc: 9, full, stack: {java.lang.Exception}, locals: {java.lang.String[]}]
				        [pc: 10, same]
				""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test053() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							try {
								final int val;
								for (val = 7; val > 0;) break;
								System.out.println(val);
							} catch(Exception e) {
							}
						}\t
					}
					""",
			},
			"7");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 7
			     2  istore_1 [val]
			     3  iload_1 [val]
			     4  ifle 7
			     7  getstatic java.lang.System.out : java.io.PrintStream [16]
			    10  iload_1 [val]
			    11  invokevirtual java.io.PrintStream.println(int) : void [22]
			    14  goto 18
			    17  astore_1
			    18  return
			      Exception Table:
			        [pc: 0, pc: 14] -> 17 when : java.lang.Exception
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 7, line: 6]
			        [pc: 14, line: 7]
			        [pc: 18, line: 9]
			      Local variable table:
			        [pc: 0, pc: 19] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 14] local: val index: 1 type: int
			"""
		:
			"""
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 2, Locals: 2
				  public static void main(java.lang.String[] args);
				     0  bipush 7
				     2  istore_1 [val]
				     3  iload_1 [val]
				     4  ifle 7
				     7  getstatic java.lang.System.out : java.io.PrintStream [16]
				    10  iload_1 [val]
				    11  invokevirtual java.io.PrintStream.println(int) : void [22]
				    14  goto 18
				    17  astore_1
				    18  return
				      Exception Table:
				        [pc: 0, pc: 14] -> 17 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 7, line: 6]
				        [pc: 14, line: 7]
				        [pc: 18, line: 9]
				      Local variable table:
				        [pc: 0, pc: 19] local: args index: 0 type: java.lang.String[]
				        [pc: 3, pc: 14] local: val index: 1 type: int
				      Stack map table: number of frames 3
				        [pc: 7, append: {int}]
				        [pc: 17, full, stack: {java.lang.Exception}, locals: {java.lang.String[]}]
				        [pc: 18, same]
				""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test054() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 X parent;
					 int kind;
					 static boolean F = false;
					 public static void main(String[] args) {
					  X x = new X();
					  x.kind = 2;\s
					  try {
					   x.foo();
					  } catch(NullPointerException e) {\s
					   System.out.println("SUCCESS");
					   return;
					  }
					  System.out.println("FAILED"); \s
					 }
					 void foo() {
					  X x = this;
					  done : while (true) {
					   switch (x.kind) {
					    case 2 :
					     if (F) {
					      return;
					     }
					     break;
					    case 3 :
					     break done;
					   }
					   x = x.parent; // should throw npe
					  }
					 }\s
					}
					""",
			},
			"SUCCESS");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"""
			  // Method descriptor #12 ()V
			  // Stack: 1, Locals: 2
			  void foo();
			     0  aload_0 [this]
			     1  astore_1 [x]
			     2  aload_1 [x]
			     3  getfield X.kind : int [25]
			     6  tableswitch default: 38
			          case 2: 28
			          case 3: 35
			    28  getstatic X.F : boolean [14]
			    31  ifeq 38
			    34  return
			    35  goto 46
			    38  aload_1 [x]
			    39  getfield X.parent : X [53]
			    42  astore_1 [x]
			    43  goto 2
			    46  return
			      Line numbers:
			        [pc: 0, line: 17]
			        [pc: 2, line: 19]
			        [pc: 28, line: 21]
			        [pc: 34, line: 22]
			        [pc: 35, line: 26]
			        [pc: 38, line: 27]
			        [pc: 39, line: 28]
			        [pc: 43, line: 18]
			        [pc: 46, line: 30]
			      Local variable table:
			        [pc: 0, pc: 47] local: this index: 0 type: X
			        [pc: 2, pc: 47] local: x index: 1 type: X
			"""
		:
			"""
				  // Method descriptor #12 ()V
				  // Stack: 1, Locals: 2
				  void foo();
				     0  aload_0 [this]
				     1  astore_1 [x]
				     2  aload_1 [x]
				     3  getfield X.kind : int [25]
				     6  tableswitch default: 38
				          case 2: 28
				          case 3: 35
				    28  getstatic X.F : boolean [14]
				    31  ifeq 38
				    34  return
				    35  goto 46
				    38  aload_1 [x]
				    39  getfield X.parent : X [55]
				    42  astore_1 [x]
				    43  goto 2
				    46  return
				      Line numbers:
				        [pc: 0, line: 17]
				        [pc: 2, line: 19]
				        [pc: 28, line: 21]
				        [pc: 34, line: 22]
				        [pc: 35, line: 26]
				        [pc: 38, line: 27]
				        [pc: 39, line: 28]
				        [pc: 43, line: 18]
				        [pc: 46, line: 30]
				      Local variable table:
				        [pc: 0, pc: 47] local: this index: 0 type: X
				        [pc: 2, pc: 47] local: x index: 1 type: X
				      Stack map table: number of frames 5
				        [pc: 2, append: {X}]
				        [pc: 28, same]
				        [pc: 35, same]
				        [pc: 38, same]
				        [pc: 46, same]
				""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test055() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
					void foo5() {
					  L : for (;;) {
					    continue L; // good
					  }
					}
					}
					""",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  void foo5();
			    0  goto 0
			      Line numbers:
			        [pc: 0, line: 4]
			      Local variable table:
			        [pc: 0, pc: 3] local: this index: 0 type: X
			"""
		:
			"""
				  // Method descriptor #6 ()V
				  // Stack: 0, Locals: 1
				  void foo5();
				    0  goto 0
				      Line numbers:
				        [pc: 0, line: 4]
				      Local variable table:
				        [pc: 0, pc: 3] local: this index: 0 type: X
				      Stack map table: number of frames 1
				        [pc: 0, same]
				""";

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
}
public void _test056() {
	this.runNegativeTest(
		new String[] {
			"p/BytecodeA.java",
			"""
				package p;
				class BytecodeA {
				 \s
				int foo() { // good
				  boolean b = true;
				  if (b) {
				    if (true)
				      return 0;
				  } else {
				    if (true)
				      return 1;
				  }
				  return 5;
				}
				int foo10() {
				  try {
				    //if (true)
				      return 0;
				  } catch (Exception e) {
				    if (true)
				      return 1;
				  } finally {
				    if (true)
				      return 2;
				  };
				  return 1;
				}  \s
				int foo11() {
				  synchronized (this) {
				    if (true)
				      return 1;
				  };
				  return 2;
				}\s
				int foo12() {
				  for (;;)
				    return 1;
				}
				int foo13() {
				  for (;;)
				    if (true)
				      return 1;
				}
				int foo14() {
				  for (int i = 1; i < 10; i++)
				    if (true)
				      return 1;
				  return 2;
				}\s
				int foo15() {
				  for (int i = 1; i < 10; i++)
				    return 1;
				  return 2;
				}
				int foo16() {
				  final int i;
				  while (true) {
				    i = 1;
				    if (true)
				      break;
				  };
				  return 1;
				}             \s
				int foo17() {
				  final int i;
				  for (;;) {
				    i = 1;
				    if (true)
				      break;
				  };
				  return 1;
				}\s
				void foo2() {
				  L1 :;  // good
				}
				void foo20() {
				  if (true)
				    return;
				}\s
				void foo3() {
				  L : if (true) {
				    for (;;) {
				      continue L; // bad
				    }
				  }
				}  \s
				void foo4() {
				  L : if (true) {
				    try {
				      for (;;) {
				        continue L; // bad
				      }
				    } finally {
				      return;
				    }
				  }\s
				}
				void foo5() {
				  L : for (;;) {
				    continue L; // good
				  }
				}
				void foo5bis() {
				  L : K : for (;;) {
				    continue L; // good
				  }
				}
				void foo6(){
				  int i;
				  boolean a[] = new boolean[5];
				  a[i=1] = i > 0; // good
				}   \s
				void foo7(){
				  Object x[];
				  x [1] = (x = new Object[5]); // bad
				}   \s
				void foo8() {
				  try {
				  } catch (java.io.IOException e) {
				    foo(); // unreachable
				  }
				}
				void foo9() {
				  try {
				  } catch (NullPointerException e) {
				    foo(); // ok
				  }
				}
				    public static void main(String args[]) {
				      BytecodeA a = new BytecodeA();
				      a.foo10();
				    }
				}""",
		},
		"""
			----------
			1. WARNING in p\\BytecodeA.java (at line 74)
				L1 :;  // good
				^^
			The label L1 is never explicitly referenced
			----------
			2. ERROR in p\\BytecodeA.java (at line 83)
				continue L; // bad
				^^^^^^^^^^
			continue cannot be used outside of a loop
			----------
			3. ERROR in p\\BytecodeA.java (at line 91)
				continue L; // bad
				^^^^^^^^^^
			continue cannot be used outside of a loop
			----------
			4. WARNING in p\\BytecodeA.java (at line 93)
				} finally {
			      return;
			    }
				          ^^^^^^^^^^^^^^^^^^^^^
			finally block does not complete normally
			----------
			5. WARNING in p\\BytecodeA.java (at line 104)
				L : K : for (;;) {
				    ^
			The label K is never explicitly referenced
			----------
			6. ERROR in p\\BytecodeA.java (at line 105)
				continue L; // good
				^^^^^^^^^^
			continue cannot be used outside of a loop
			----------
			7. ERROR in p\\BytecodeA.java (at line 115)
				x [1] = (x = new Object[5]); // bad
				^
			The local variable x may not have been initialized
			----------
			8. ERROR in p\\BytecodeA.java (at line 119)
				} catch (java.io.IOException e) {
				         ^^^^^^^^^^^^^^^^^^^
			Unreachable catch block for IOException. This exception is never thrown from the try statement body
			----------
			""");
}

// was Compliance_1_x#test007
public void test057() {
	String[] sources = new String[] {
		"p1/Test.java",
		"""
			package p1;\s
			public class Test {\s
				public static void main(String[] arguments) {\s
					try {\t
						throw null;\s
					} catch(NullPointerException e){ \t
						System.out.println("SUCCESS");\t
					}\t
				}\s
			}\s
			"""
	};
	if (this.complianceLevel == ClassFileConstants.JDK1_3) {
		runNegativeTest(
			sources,
			"""
				----------
				1. ERROR in p1\\Test.java (at line 5)
					throw null;\s
					      ^^^^
				Cannot throw null as an exception
				----------
				""");
	} else {
		runConformTest(
			sources,
			"SUCCESS");
	}
}
//https://bugs.eclpse.org/bugs/show_bug.cgi?id=3184
public void test058() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String args[]) {\n" +
				"		try {\n" +
				"			try {\n" +
				"				System.out.print(\"SU\");\n" +
				"			} finally {\n" +
				"				System.out.print(\"CC\");\n" +
				"			}\n" +
				"		} finally {\n" +
				"			System.out.println(\"ESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			"SUCCESS");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel <= ClassFileConstants.JDK1_4
		?	"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 5
			  public static void main(java.lang.String[] args);
			     0  getstatic java.lang.System.out : java.io.PrintStream [16]
			     3  ldc <String "SU"> [22]
			     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
			     8  goto 28
			    11  astore_2
			    12  jsr 17
			    15  aload_2
			    16  athrow
			    17  astore_1
			    18  getstatic java.lang.System.out : java.io.PrintStream [16]
			    21  ldc <String "CC"> [30]
			    23  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
			    26  ret 1
			    28  jsr 17
			    31  goto 53
			    34  astore 4
			    36  jsr 42
			    39  aload 4
			    41  athrow
			    42  astore_3
			    43  getstatic java.lang.System.out : java.io.PrintStream [16]
			    46  ldc <String "ESS"> [32]
			    48  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]
			    51  ret 3
			    53  jsr 42
			    56  return
			      Exception Table:
			        [pc: 0, pc: 11] -> 11 when : any
			        [pc: 28, pc: 31] -> 11 when : any
			        [pc: 0, pc: 34] -> 34 when : any
			        [pc: 53, pc: 56] -> 34 when : any
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 8, line: 6]
			        [pc: 15, line: 8]
			        [pc: 17, line: 6]
			        [pc: 18, line: 7]
			        [pc: 26, line: 8]
			        [pc: 31, line: 9]
			        [pc: 39, line: 11]
			        [pc: 42, line: 9]
			        [pc: 43, line: 10]
			        [pc: 51, line: 11]
			        [pc: 56, line: 12]
			      Local variable table:
			        [pc: 0, pc: 57] local: args index: 0 type: java.lang.String[]
			"""
		:
			"""
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 2, Locals: 3
				  public static void main(java.lang.String[] args);
				     0  getstatic java.lang.System.out : java.io.PrintStream [16]
				     3  ldc <String "SU"> [22]
				     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
				     8  goto 22
				    11  astore_1
				    12  getstatic java.lang.System.out : java.io.PrintStream [16]
				    15  ldc <String "CC"> [30]
				    17  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
				    20  aload_1
				    21  athrow
				    22  getstatic java.lang.System.out : java.io.PrintStream [16]
				    25  ldc <String "CC"> [30]
				    27  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
				    30  goto 44
				    33  astore_2
				    34  getstatic java.lang.System.out : java.io.PrintStream [16]
				    37  ldc <String "ESS"> [32]
				    39  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]
				    42  aload_2
				    43  athrow
				    44  getstatic java.lang.System.out : java.io.PrintStream [16]
				    47  ldc <String "ESS"> [32]
				    49  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]
				    52  return
				      Exception Table:
				        [pc: 0, pc: 11] -> 11 when : any
				        [pc: 0, pc: 33] -> 33 when : any
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 8, line: 6]
				        [pc: 12, line: 7]
				        [pc: 20, line: 8]
				        [pc: 22, line: 7]
				        [pc: 30, line: 9]
				        [pc: 34, line: 10]
				        [pc: 42, line: 11]
				        [pc: 44, line: 10]
				        [pc: 52, line: 12]
				      Local variable table:
				        [pc: 0, pc: 53] local: args index: 0 type: java.lang.String[]
				""";

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
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183395
public void test059() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				    	try {
				    		System.out.println(args.length);
				    	} catch(Exception[] e) {
				    		// ignore
				    	}
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				} catch(Exception[] e) {
				        ^^^^^^^^^^^
			No exception of type Exception[] can be thrown; an exception type must be a subclass of Throwable
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=183395
public void test060() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				    	try {
				    		System.out.println(args.length);
				    	} catch(int e) {
				    		// ignore
				    	}
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				} catch(int e) {
				        ^^^
			No exception of type int can be thrown; an exception type must be a subclass of Throwable
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190209 - variation
public void test062() throws Exception {
	if (new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_5) return; // need autoboxing
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					final public class X {
						final class MyClass {
							/** @param s */
							void foo(final String s) {
								 /* do nothing */
							}
						}
						Object bar() {
							try {
								final MyClass myClass = new MyClass();
								try {
									return 0;
								} catch (final Throwable ex) {
									myClass.foo(this == null ? "" : "");
								}
					\t
								return this;
							} finally {
								{ /* do nothing */ }
							}
						}
						public static void main(String[] args) {
							new X().bar();
							System.out.print("SUCCESS");
						}
					}
					""",
			},
			"SUCCESS");

	String expectedOutput =
			"""
		  // Method descriptor #15 ()Ljava/lang/Object;
		  // Stack: 3, Locals: 5
		  java.lang.Object bar();
		     0  new X$MyClass [16]
		     3  dup
		     4  aload_0 [this]
		     5  invokespecial X$MyClass(X) [18]
		     8  astore_1 [myClass]
		     9  iconst_0
		    10  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [21]
		    13  astore 4
		    15  aload 4
		    17  areturn
		    18  astore_2 [ex]
		    19  aload_1 [myClass]
		    20  aload_0 [this]
		    21  ifnonnull 29
		    24  ldc <String ""> [27]
		    26  goto 31
		    29  ldc <String ""> [27]
		    31  invokevirtual X$MyClass.foo(java.lang.String) : void [29]
		    34  aload_0 [this]
		    35  astore 4
		    37  aload 4
		    39  areturn
		    40  astore_3
		    41  aload_3
		    42  athrow
		      Exception Table:
		        [pc: 9, pc: 15] -> 18 when : java.lang.Throwable
		        [pc: 0, pc: 15] -> 40 when : any
		        [pc: 18, pc: 37] -> 40 when : any
		""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190209 - variation
public void test063() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					final public class X {
						final class MyClass {
							/** @param s */
							void foo(final String s) {
								 /* do nothing */
							}
						}
						void bar() {
							try {
								final MyClass myClass = new MyClass();
								try {
									return;
								} catch (final Throwable ex) {
									myClass.foo(this == null ? "" : "");
								}
								return;
							} finally {
								{ /* do nothing */ }
							}
						}
						public static void main(String[] args) {
							new X().bar();
							System.out.print("SUCCESS");
						}
					}
					""",
			},
			"SUCCESS");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel <= ClassFileConstants.JDK1_4
		?	"""
			  // Method descriptor #6 ()V
			  // Stack: 3, Locals: 5
			  void bar();
			     0  new X$MyClass [15]
			     3  dup
			     4  aload_0 [this]
			     5  invokespecial X$MyClass(X) [17]
			     8  astore_1 [myClass]
			     9  jsr 21
			    12  return
			    13  astore 4
			    15  jsr 21
			    18  aload 4
			    20  athrow
			    21  astore_3
			    22  ret 3
			      Exception Table:
			        [pc: 0, pc: 12] -> 13 when : any
			"""
		:
			"""
				  // Method descriptor #6 ()V
				  // Stack: 3, Locals: 4
				  void bar();
				     0  new X$MyClass [15]
				     3  dup
				     4  aload_0 [this]
				     5  invokespecial X$MyClass(X) [17]
				     8  astore_1 [myClass]
				     9  return
				    10  return
				    11  astore_3
				    12  aload_3
				    13  athrow
				      Exception Table:
				        [pc: 0, pc: 9] -> 11 when : any
				""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190209 - variation
public void test064() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					final public class X {
						final class MyClass {
							/** @param s */
							void foo(final String s) {
								 /* do nothing */
							}
						}
						Object bar() {
							try {
								final MyClass myClass = new MyClass();
								try {
									return null;
								} catch (final Throwable ex) {
									myClass.foo(this == null ? "" : "");
								}
								return null;
							} finally {
								{ /* do nothing */ }
							}
						}
						public static void main(String[] args) {
							new X().bar();
							System.out.print("SUCCESS");
						}
					}
					""",
			},
			"SUCCESS");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel <= ClassFileConstants.JDK1_4
		?	"""
			  // Method descriptor #15 ()Ljava/lang/Object;
			  // Stack: 3, Locals: 5
			  java.lang.Object bar();
			     0  new X$MyClass [16]
			     3  dup
			     4  aload_0 [this]
			     5  invokespecial X$MyClass(X) [18]
			     8  astore_1 [myClass]
			     9  jsr 22
			    12  aconst_null
			    13  areturn
			    14  astore 4
			    16  jsr 22
			    19  aload 4
			    21  athrow
			    22  astore_3
			    23  ret 3
			      Exception Table:
			        [pc: 0, pc: 12] -> 14 when : any
			"""
		:	"""
			  // Method descriptor #15 ()Ljava/lang/Object;
			  // Stack: 3, Locals: 4
			  java.lang.Object bar();
			     0  new X$MyClass [16]
			     3  dup
			     4  aload_0 [this]
			     5  invokespecial X$MyClass(X) [18]
			     8  astore_1 [myClass]
			     9  aconst_null
			    10  areturn
			    11  aconst_null
			    12  areturn
			    13  astore_3
			    14  aload_3
			    15  athrow
			      Exception Table:
			        [pc: 0, pc: 9] -> 13 when : any
			""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191865
public void test065() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						try {
							System.out.println("Hello");
						} finally {
							if (true)
								return;
						}
						return;
					}
					void bar() {
						try {
							System.out.println("Hello");
						} finally {
							return;
						}
						return;
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 14)
				} finally {
						return;
					}
				          ^^^^^^^^^^^^^^^^
			finally block does not complete normally
			----------
			2. ERROR in X.java (at line 17)
				return;
				^^^^^^^
			Unreachable code
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196653
public void test066() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void bar() {
						try {
							Zork z = null;
							z.foo();
						} catch(Zork z) {
							z.foo();
						}	\t
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				Zork z = null;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 6)
				} catch(Zork z) {
				        ^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=248319
public void test067() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(final String[] args) {\n" +
				"		System.out.println(new X().verifyError());\n" +
				"	}\n" +
				"	private Object verifyError() {\n" +
				"		try {\n" +
				"			if (someBooleanMethod()) {\n" +
				"				return null;\n" +
				"			}\n" +
				"			return getStuff();\n" +
				"		} catch (final Exception ex) {\n" +
				"			return null;\n" +
				"		} finally {\n" +
				"			while (someBooleanMethod()) {\n" +
				"				anyMethod();\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"	private void anyMethod() { /*empty*/ }\n" +
				"	private Object getStuff() { return null; }\n" +
				"	private boolean someBooleanMethod() { return false; }\n" +
				"}\n" +
				"",
			},
			"null");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340485
public void test068() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String [] args) {
				        doSomething(false);
				    }
				    public static void doSomething (boolean bool) {
				        try {
				            if (bool)
				                throw new GrandSonOfFoo();
				            else\s
				                throw new GrandDaughterOfFoo();
				        } catch(Foo e) {
				            try {\s
				                    throw e;\s
				            } catch (SonOfFoo e1) {
				                 e1.printStackTrace();
				            } catch (DaughterOfFoo e1) {
				                System.out.println("caught a daughter of foo");
				            } catch (Foo f) {}
				        }
				    }
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class GrandSonOfFoo extends SonOfFoo {}
				class DaughterOfFoo extends Foo {}
				class GrandDaughterOfFoo extends DaughterOfFoo {}
				"""
		},
		"caught a daughter of foo");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340484
public void test069() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        try {
				            throw new DaughterOfFoo();
				        } catch(Foo e) {
				            try {\s
				                while (true) {
				                    throw e;\s
				                }
				            } catch (SonOfFoo e1) {
				                 e1.printStackTrace();
				            } catch (Foo e1) {}
				        }
				    }
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"""
					----------
					1. WARNING in X.java (at line 16)
						class Foo extends Exception {}
						      ^^^
					The serializable class Foo does not declare a static final serialVersionUID field of type long
					----------
					2. WARNING in X.java (at line 17)
						class SonOfFoo extends Foo {}
						      ^^^^^^^^
					The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
					----------
					3. WARNING in X.java (at line 18)
						class DaughterOfFoo extends Foo {}
						      ^^^^^^^^^^^^^
					The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
					----------
					""" :
				"""
					----------
					1. ERROR in X.java (at line 10)
						} catch (SonOfFoo e1) {
						         ^^^^^^^^
					Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body
					----------
					2. WARNING in X.java (at line 16)
						class Foo extends Exception {}
						      ^^^
					The serializable class Foo does not declare a static final serialVersionUID field of type long
					----------
					3. WARNING in X.java (at line 17)
						class SonOfFoo extends Foo {}
						      ^^^^^^^^
					The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
					----------
					4. WARNING in X.java (at line 18)
						class DaughterOfFoo extends Foo {}
						      ^^^^^^^^^^^^^
					The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
					----------
					""");
}
// precise throw computation should also take care of throws clause in 1.7. 1.6- should continue to behave as it always has.
public void test070() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() throws DaughterOfFoo {
						try {
							throw new DaughterOfFoo();
						} catch (Foo e){
							throw e;
				           foo();
						}
					}
					public static void main(String[] args) {
						try {
							foo();
						} catch(Foo e) {}
					}
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"""
					----------
					1. ERROR in X.java (at line 6)
						throw e;
						^^^^^^^^
					Unhandled exception type Foo
					----------
					2. ERROR in X.java (at line 7)
						foo();
						^^^^^^
					Unreachable code
					----------
					3. WARNING in X.java (at line 16)
						class Foo extends Exception {}
						      ^^^
					The serializable class Foo does not declare a static final serialVersionUID field of type long
					----------
					4. WARNING in X.java (at line 17)
						class SonOfFoo extends Foo {}
						      ^^^^^^^^
					The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
					----------
					5. WARNING in X.java (at line 18)
						class DaughterOfFoo extends Foo {}
						      ^^^^^^^^^^^^^
					The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
					----------
					""":

				"""
					----------
					1. ERROR in X.java (at line 7)
						foo();
						^^^^^^
					Unreachable code
					----------
					2. WARNING in X.java (at line 16)
						class Foo extends Exception {}
						      ^^^
					The serializable class Foo does not declare a static final serialVersionUID field of type long
					----------
					3. WARNING in X.java (at line 17)
						class SonOfFoo extends Foo {}
						      ^^^^^^^^
					The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
					----------
					4. WARNING in X.java (at line 18)
						class DaughterOfFoo extends Foo {}
						      ^^^^^^^^^^^^^
					The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348369
public void test071() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						try {
						} catch (Exception [][][][][]  e [][][][]) {
						}
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				} catch (Exception [][][][][]  e [][][][]) {
				         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			No exception of type Exception[][][][][][][][][] can be thrown; an exception type must be a subclass of Throwable
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348369
public void test072() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						try {
						} catch (Exception e []) {
						}
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				} catch (Exception e []) {
				         ^^^^^^^^^^^^^^
			No exception of type Exception[] can be thrown; an exception type must be a subclass of Throwable
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348369
public void test073() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						try {
						} catch (Exception [] e) {
						}
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				} catch (Exception [] e) {
				         ^^^^^^^^^^^^
			No exception of type Exception[] can be thrown; an exception type must be a subclass of Throwable
			----------
			""");
}
// test for regression during work on bug 345305
// saw "The local variable name may not have been initialized" against last code line
public void test074() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					Class test(String name) throws ClassNotFoundException {
						Class c= findClass(name);
						if (c != null)
							return c;
						if (isExcluded(name)) {
							try {
								c= findClass(name);
								return c;
							} catch (ClassNotFoundException e) {
								// keep searching
							}
						}
						return findClass(name);
				    }
				    boolean isExcluded(String name) { return false; }
				    Class findClass(String name) throws ClassNotFoundException { return null; }
				}
				"""
		});
}

// Bug 387612 - Unreachable catch block...exception is never thrown from the try
// redundant exception in throws must not confuse downstream analysis
public void testBug387612() {
	String serialUID = "private static final long serialVersionUID=1L;";
	runNegativeTest(
		new String[] {
			"E.java",
			"public class E extends Exception {"+serialUID+"}\n",
			"E1.java",
			"public class E1 extends E {"+serialUID+"}\n",
			"E2.java",
			"public class E2 extends E {"+serialUID+"}\n",
			"E3.java",
			"public class E3 extends E {"+serialUID+"}\n",
			"A.java",
			"""
				interface A {
				    void foo(String a1, String a2) throws E1, E;
				}
				""",
			"B.java",
			"""
				interface B extends A {
				    void foo(String a1, String a2) throws E;
				}
				""",
			"Client.java",
			"""
				public class Client {
				    void test() {
				        B b = new B() {
				            public void foo(String a1, String a2) {}
				        };
				        try {
				            b.foo(null, null);
				        }
				        catch (E1 e) {}
				        catch (E2 e) {}
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in Client.java (at line 7)
				b.foo(null, null);
				^^^^^^^^^^^^^^^^^
			Unhandled exception type E
			----------
			""");
}

// Bug 387612 - Unreachable catch block...exception is never thrown from the try
// - changed order in redundant 'throws' clause.
public void testBug387612b() {
	String serialUID = "private static final long serialVersionUID=1L;";
	runNegativeTest(
		new String[] {
			"E.java",
			"public class E extends Exception {"+serialUID+"}\n",
			"E1.java",
			"public class E1 extends E {"+serialUID+"}\n",
			"E2.java",
			"public class E2 extends E {"+serialUID+"}\n",
			"E3.java",
			"public class E3 extends E {"+serialUID+"}\n",
			"A.java",
			"""
				interface A {
				    void foo(String a1, String a2) throws E, E1;
				}
				""",
			"B.java",
			"""
				interface B extends A {
				    void foo(String a1, String a2) throws E;
				}
				""",
			"Client.java",
			"""
				public class Client {
				    void test() {
				        B b = new B() {
				            public void foo(String a1, String a2) {}
				        };
				        try {
				            b.foo(null, null);
				        }
				        catch (E1 e) {}
				        catch (E2 e) {}
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in Client.java (at line 7)
				b.foo(null, null);
				^^^^^^^^^^^^^^^^^
			Unhandled exception type E
			----------
			""");
}

// Bug 387612 - Unreachable catch block...exception is never thrown from the try
// interface with redundant exceptions in throws is read from class file.
public void testBug387612c() {
	String serialUID = "private static final long serialVersionUID=1L;";
	runConformTest(
		new String[] {
			"E.java",
			"public class E extends Exception {"+serialUID+"}\n",
			"E1.java",
			"public class E1 extends E {"+serialUID+"}\n",
			"E2.java",
			"public class E2 extends E {"+serialUID+"}\n",
			"A.java",
			"""
				interface A {
				    void foo(String a1, String a2) throws E1, E;
				}
				""",
			"B.java",
			"""
				interface B extends A {
				    void foo(String a1, String a2) throws E;
				}
				"""
		});
	runNegativeTest(
		new String[] {
			"Client.java",
			"""
				public class Client {
				    void test() {
				        B b = new B() {
				            public void foo(String a1, String a2) {}
				        };
				        try {
				            b.foo(null, null);
				        }
				        catch (E1 e) {}
				        catch (E2 e) {}
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in Client.java (at line 7)
				b.foo(null, null);
				^^^^^^^^^^^^^^^^^
			Unhandled exception type E
			----------
			""",
		null,
		false/*shouldFlush*/);
}

public static Class testClass() {
	return TryStatementTest.class;
}
}
