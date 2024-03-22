/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
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
 *     Jesper Steen Moller - bug 404146 nested try-catch-finally-blocks leads to unrunnable Java byte code
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;
@SuppressWarnings({ "rawtypes" })
public class TryStatement17Test extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test061" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryStatement17Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				
				public class X {
					public static void main(String[] args) {
						try {
							System.out.println();
							Reader r = new FileReader(args[0]);
							r.read();
						} catch(IOException | FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				} catch(IOException | FileNotFoundException e) {
				                      ^^^^^^^^^^^^^^^^^^^^^
			The exception FileNotFoundException is already caught by the alternative IOException
			----------
			""");
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				
				public class X {
					public static void main(String[] args) {
						try {
							System.out.println();
							Reader r = new FileReader(args[0]);
							r.read();
						} catch(FileNotFoundException | FileNotFoundException | IOException e) {
							e.printStackTrace();
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				} catch(FileNotFoundException | FileNotFoundException | IOException e) {
				        ^^^^^^^^^^^^^^^^^^^^^
			The exception FileNotFoundException is already caught by the alternative FileNotFoundException
			----------
			2. ERROR in X.java (at line 9)
				} catch(FileNotFoundException | FileNotFoundException | IOException e) {
				        ^^^^^^^^^^^^^^^^^^^^^
			The exception FileNotFoundException is already caught by the alternative IOException
			----------
			3. ERROR in X.java (at line 9)
				} catch(FileNotFoundException | FileNotFoundException | IOException e) {
				                                ^^^^^^^^^^^^^^^^^^^^^
			The exception FileNotFoundException is already caught by the alternative IOException
			----------
			""");
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				
				public class X {
					public static void main(String[] args) {
						try {
							System.out.println();
							Reader r = new FileReader(args[0]);
							r.read();
						} catch(FileNotFoundException e) {\
							e.printStackTrace();
						} catch(FileNotFoundException | IOException e) {
							e.printStackTrace();
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				} catch(FileNotFoundException | IOException e) {
				        ^^^^^^^^^^^^^^^^^^^^^
			The exception FileNotFoundException is already caught by the alternative IOException
			----------
			""");
}
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				
				public class X {
					public static void main(String[] args) {
						try {
							System.out.println();
							Reader r = new FileReader(args[0]);
							r.read();
						} catch(RuntimeException | Exception e) {\
							e.printStackTrace();
						} catch(FileNotFoundException | IOException e) {
							e.printStackTrace();
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				} catch(RuntimeException | Exception e) {			e.printStackTrace();
				        ^^^^^^^^^^^^^^^^
			The exception RuntimeException is already caught by the alternative Exception
			----------
			2. ERROR in X.java (at line 10)
				} catch(FileNotFoundException | IOException e) {
				        ^^^^^^^^^^^^^^^^^^^^^
			The exception FileNotFoundException is already caught by the alternative IOException
			----------
			""");
}
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				
				public class X {
					public static void main(String[] args) {
						try {
							System.out.println();
							Reader r = new FileReader("Zork");
							r.read();
						} catch(NumberFormatException | RuntimeException e) {
							e.printStackTrace();
						} catch(FileNotFoundException | IOException e) {
							// ignore
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				} catch(NumberFormatException | RuntimeException e) {
				        ^^^^^^^^^^^^^^^^^^^^^
			The exception NumberFormatException is already caught by the alternative RuntimeException
			----------
			2. ERROR in X.java (at line 11)
				} catch(FileNotFoundException | IOException e) {
				        ^^^^^^^^^^^^^^^^^^^^^
			The exception FileNotFoundException is already caught by the alternative IOException
			----------
			""");
}
//Test that lub is not used for checking for checking the exceptions
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",

			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new Foo();
						} catch(SonOfFoo | DaughterOfFoo e) {
							e.printStackTrace();
						}
					}
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				throw new Foo();
				^^^^^^^^^^^^^^^^
			Unhandled exception type Foo
			----------
			2. WARNING in X.java (at line 10)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 11)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			4. WARNING in X.java (at line 12)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",

			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new Foo();
						} catch(SonOfFoo | DaughterOfFoo e) {
							System.out.println("Caught lub");
						} catch(Foo e) {
				           System.out.println("Caught Foo");
				        }
					}
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		"Caught Foo");
}
// test that lub is not used for precise rethrow
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try {
							if (args.length == 0) throw new SonOfFoo();
							throw new DaughterOfFoo();
						} catch(SonOfFoo | DaughterOfFoo e) {
							try {
								throw e;
							} catch(SonOfFoo | DaughterOfFoo e1) {}
						}
					}
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 13)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 14)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 15)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				
				public class X {
					public static void main(String[] args) {
						try {
							throw new IOException();
						} catch(IOException | RuntimeException e) {
							e = new IOException();
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				e = new IOException();
				^
			The parameter e of a multi-catch block cannot be assigned
			----------
			""");
}
//Test that union type checks are done for a precise throw too
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",

			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new DaughterOfFoo();
						} catch(SonOfFoo | DaughterOfFoo e) {
							e.printStackTrace();
						}
					}
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				} catch(SonOfFoo | DaughterOfFoo e) {
				        ^^^^^^^^
			Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body
			----------
			2. WARNING in X.java (at line 10)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 11)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			4. WARNING in X.java (at line 12)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// Test that a rethrow is precisely computed
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new DaughterOfFoo();
						} catch(Foo e) {
							try {
								throw e;
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
		"""
			----------
			1. ERROR in X.java (at line 8)
				} catch (SonOfFoo e1) {
				         ^^^^^^^^
			Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body
			----------
			2. WARNING in X.java (at line 14)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 15)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			4. WARNING in X.java (at line 16)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}
//Test that a rethrow is precisely computed
public void test012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new DaughterOfFoo();
						} catch(Foo e) {
							try {
								throw e;
							} catch (SonOfFoo e1) {
							 	e1.printStackTrace();
							} catch (Foo e1) {}
							finally {\
								System.out.println("");}
						}
					}
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				} catch (SonOfFoo e1) {
				         ^^^^^^^^
			Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body
			----------
			2. WARNING in X.java (at line 15)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 16)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			4. WARNING in X.java (at line 17)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// Test that if the rethrow argument is modified (not effectively final), then it is not precisely
// computed
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new DaughterOfFoo();
						} catch(Foo e) {
							try {
								e = new Foo();
								throw e;
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
		"""
			----------
			1. WARNING in X.java (at line 15)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 16)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 17)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}

// Test that if the rethrow argument is modified in a different flow (not effectively final), then also precise throw
// should not be computed
public void test014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new DaughterOfFoo();
						} catch(Foo e) {
							try {
								boolean DEBUG = true;
								if (DEBUG) {
									throw e;
								}\
								e = new Foo();
								e.printStackTrace();
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
		"""
			----------
			1. WARNING in X.java (at line 18)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 19)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 20)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}

// test015 moved into org.eclipse.jdt.core.tests.compiler.regression.TryStatementTest.test070()

// Test precise rethrow works good even in nested try catch block
public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new DaughterOfFoo();
						} catch(Foo e) {
							try {
								throw new Foo();
							} catch (Foo e1) {
								try {
									throw e;
								} catch (SonOfFoo e2) {
							 		e1.printStackTrace();
								} catch (Foo e3) {}
							}
						}
					}
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				} catch (SonOfFoo e2) {
				         ^^^^^^^^
			Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body
			----------
			2. WARNING in X.java (at line 18)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 19)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			4. WARNING in X.java (at line 20)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// Test lub computation.
public void test017() {
	this.runNegativeTest(
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
				        } catch(SonOfFoo | DaughterOfFoo e) {
				        	SonOfFoo s = e;
				        	e.callableOnBothGenders();
				        	e.callableOnlyOnMales();
				        	e.callableOnlyOnFemales();
				        }
				    }
				}
				class Foo extends Exception {
					void callableOnBothGenders () {
					}
				}
				class SonOfFoo extends Foo {
					void callableOnlyOnMales() {
					}
				}
				class GrandSonOfFoo extends SonOfFoo {}
				class DaughterOfFoo extends Foo {
					void callableOnlyOnFemales() {
					}
				}
				class GrandDaughterOfFoo extends DaughterOfFoo {}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				SonOfFoo s = e;
				             ^
			Type mismatch: cannot convert from Foo to SonOfFoo
			----------
			2. ERROR in X.java (at line 14)
				e.callableOnlyOnMales();
				  ^^^^^^^^^^^^^^^^^^^
			The method callableOnlyOnMales() is undefined for the type Foo
			----------
			3. ERROR in X.java (at line 15)
				e.callableOnlyOnFemales();
				  ^^^^^^^^^^^^^^^^^^^^^
			The method callableOnlyOnFemales() is undefined for the type Foo
			----------
			4. WARNING in X.java (at line 19)
				class Foo extends Exception {
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			5. WARNING in X.java (at line 23)
				class SonOfFoo extends Foo {
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			6. WARNING in X.java (at line 27)
				class GrandSonOfFoo extends SonOfFoo {}
				      ^^^^^^^^^^^^^
			The serializable class GrandSonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			7. WARNING in X.java (at line 28)
				class DaughterOfFoo extends Foo {
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			8. WARNING in X.java (at line 32)
				class GrandDaughterOfFoo extends DaughterOfFoo {}
				      ^^^^^^^^^^^^^^^^^^
			The serializable class GrandDaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// Test explicit final modifiers
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo(boolean bool) throws Foo {
						try {
							if (bool)\s
							    throw new DaughterOfFoo();
							else
							    throw new SonOfFoo();
						} catch (final SonOfFoo | DaughterOfFoo e){
							throw e;
						}
					}
					public static void main(String[] args) {
						try {
							foo(true);
						} catch(Foo e) {}\s
					}
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 7)
				throw new SonOfFoo();
				^^^^^^^^^^^^^^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			2. WARNING in X.java (at line 18)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 19)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			4. WARNING in X.java (at line 20)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// Test explicit final modifiers
public void test019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo(boolean bool) throws Foo {
						try {
							if (bool)\s
							    throw new DaughterOfFoo();
							else
							    throw new SonOfFoo();
						} catch (final SonOfFoo | final DaughterOfFoo e){
							throw e;
						}
					}
					public static void main(String[] args) {
						try {
							foo(true);
						} catch(Foo e) {}\s
					}
				}
				class Foo extends Exception {}
				class SonOfFoo extends Foo {}
				class DaughterOfFoo extends Foo {}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				} catch (final SonOfFoo | final DaughterOfFoo e){
				                          ^^^^^
			Syntax error on token "final", delete this token
			----------
			2. WARNING in X.java (at line 18)
				class Foo extends Exception {}
				      ^^^
			The serializable class Foo does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 19)
				class SonOfFoo extends Foo {}
				      ^^^^^^^^
			The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
			----------
			4. WARNING in X.java (at line 20)
				class DaughterOfFoo extends Foo {}
				      ^^^^^^^^^^^^^
			The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// Test that for unchecked exceptions, we don't do any precise analysis.
public void test020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args)  {
						try {
						} catch (NullPointerException s) {
							try {
								throw s;
							} catch (ArithmeticException e) {
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				"""
		},
		"All done");
}
// Test multicatch behavior.
public void test021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String[] exceptions = { "NullPointerException", "ArithmeticException",
								"ArrayStoreException", "ArrayIndexOutOfBoundsException" };
				
						for (String exception : exceptions) {
							try {
								switch (exception) {
								case "NullPointerException":
									throw new NullPointerException();
								case "ArithmeticException":
									throw new ArithmeticException();
								case "ArrayStoreException":
									throw new ArrayStoreException();
								case "ArrayIndexOutOfBoundsException":
									throw new ArrayIndexOutOfBoundsException();
								}
							} catch (NullPointerException | ArithmeticException | ArrayStoreException | ArrayIndexOutOfBoundsException e) {
								System.out.println(e);
							}
						}
					}
				}
				"""
		},
		"""
			java.lang.NullPointerException
			java.lang.ArithmeticException
			java.lang.ArrayStoreException
			java.lang.ArrayIndexOutOfBoundsException""");
}
public void test022() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T extends Exception> {
					public void foo(boolean bool) throws Exception {
						try {
						if (bool)
							throw new Exception();
						else
							throw new NullPointerException();
						} catch (T | NullPointerException e) {}
					}
					}
					"""},
	        	"""
					----------
					1. ERROR in X.java (at line 8)
						} catch (T | NullPointerException e) {}
						         ^
					Cannot use the type parameter T in a catch block
					----------
					"""
			);
}
public void test023() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> extends Exception {
					public void foo(boolean bool) throws Exception {
						try {
						if (bool)
							throw new Exception();
						else
							throw new NullPointerException();
						} catch (X<String> | NullPointerException e) {}
					}
					}
					"""},
	        	"""
					----------
					1. WARNING in X.java (at line 1)
						public class X<T> extends Exception {
						             ^
					The serializable class X does not declare a static final serialVersionUID field of type long
					----------
					2. ERROR in X.java (at line 1)
						public class X<T> extends Exception {
						                          ^^^^^^^^^
					The generic class X<T> may not subclass java.lang.Throwable
					----------
					3. ERROR in X.java (at line 8)
						} catch (X<String> | NullPointerException e) {}
						         ^
					Cannot use the parameterized type X<String> either in catch block or throws clause
					----------
					"""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340486
public void test024() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.FileNotFoundException;
					import java.io.IOException;
					public class X {
					    public static void main(String [] args) {
					        try {
					            if (args.length == 0)
					                throw new FileNotFoundException();
					            throw new IOException();
					        } catch(IOException | FileNotFoundException e) {
					        }
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					} catch(IOException | FileNotFoundException e) {
					                      ^^^^^^^^^^^^^^^^^^^^^
				The exception FileNotFoundException is already caught by the alternative IOException
				----------
				""");
}
public void test024a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.FileNotFoundException;
					import java.io.IOException;
					public class X {
					    public static void main(String [] args) {
					        try {
					            if (args.length == 0)
					                throw new FileNotFoundException();
					            throw new IOException();
					        } catch(FileNotFoundException | IOException e) {
					        }
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					} catch(FileNotFoundException | IOException e) {
					        ^^^^^^^^^^^^^^^^^^^^^
				The exception FileNotFoundException is already caught by the alternative IOException
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=344824
public void test025() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
					        try {
					            throw new D();
					        } catch (F e) {
					            try {
					                throw e;
					            } catch (F f) {
					            } catch (RuntimeException | S f) {
					            }
					        }
					    }
					}
					class F extends Exception {}
					class S extends F {}
					class D extends F {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					} catch (RuntimeException | S f) {
					                            ^
				Unreachable catch block for S. It is already handled by the catch block for F
				----------
				2. WARNING in X.java (at line 14)
					class F extends Exception {}
					      ^
				The serializable class F does not declare a static final serialVersionUID field of type long
				----------
				3. WARNING in X.java (at line 15)
					class S extends F {}
					      ^
				The serializable class S does not declare a static final serialVersionUID field of type long
				----------
				4. WARNING in X.java (at line 16)
					class D extends F {}
					      ^
				The serializable class D does not declare a static final serialVersionUID field of type long
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345522
public void test026() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.EOFException;
					import java.io.FileNotFoundException;
					public class X {
					    X() {\s
					        try {
					            zoo();
					        } catch (EOFException ea) {
					        } catch (FileNotFoundException eb) {
					        } catch (Exception ec) {
					            throw ec;
					        }
					    }
					    void zoo() throws FileNotFoundException, EOFException {
					    }
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345522
public void test026a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.EOFException;
					import java.io.FileNotFoundException;
					public class X {
					    X() {\s
					        try {
					            zoo();
					            throw new Exception();
					        } catch (EOFException ea) {
					        } catch (FileNotFoundException eb) {
					        } catch (Exception ec) {
					            throw ec;
					        }
					    }
					    void zoo() throws FileNotFoundException, EOFException {
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					throw ec;
					^^^^^^^^^
				Unhandled exception type Exception
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=345579
public void test027() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    X() throws Exception {
					        try {
					            throw (Throwable) new Exception();
					        } catch (Exception e) {
					            throw e;
					        } catch (Throwable e) {
					        }
					    }
					}
					"""
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=350361
public void test028() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public void foo () {
					        try {
					            throw new Exception();\s
					        } catch (Exception e) {
					            if (e instanceof RuntimeException)\s
					            	throw (RuntimeException) e;\s
					        }\s
					    }
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350579
public void test029() { // with finally
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X implements AutoCloseable {
					    public static void main(String[] args) {
					        try (X x = new X();) {
					        } catch (Exception x) {
					        } catch (Throwable y) {
					        }\s
					        finally {
					            System.out.println("Done");
					        }
					    }
					    public void close() {
					    }
					}
					"""
			},
			"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350579
public void test030() { // no finally
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X implements AutoCloseable {
					    public static void main(String[] args) {
					        try (X x = new X();) {
					        } catch (Exception x) {
					        } catch (Throwable y) {
					        }\s
					        System.out.println("Done");
					    }
					    public void close() {
					    }
					}
					"""
			},
			"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350579
public void test031() { // with finally
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X implements AutoCloseable {
					    public static void main(String [] args) throws XXException, YYException, ZZException {
					        try (X x = new X(); Y y = new Y(); Z z = new Z()) {
					        } catch (XException x) {
					        } catch (YException y) {
					        } catch (ZException z) {
					        } finally {
					            System.out.println("Done");
					        }
					    }
					    public X() throws XException {
					        throw new XException();
					    }
					    public void close() throws XXException {
					        throw new XXException();
					    }
					}
					class Y implements AutoCloseable {
					    public Y() throws YException {
					        throw new YException();
					    }
					    public void close() throws YYException {
					        throw new YYException();
					    }
					}
					class Z implements AutoCloseable {
					    public Z() throws ZException {
					        throw new ZException();
					    }
					    public void close() throws ZZException {
					        throw new ZZException();
					    }
					}
					class XException extends Exception {}
					class XXException extends Exception {}
					class YException extends Exception {}
					class YYException extends Exception {}
					class ZException extends Exception {}
					class ZZException extends Exception {}
					"""
			},
			"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350579
public void test032() { // no finally
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X implements AutoCloseable {
					    public static void main(String [] args) throws XXException, YYException, ZZException {
					        try (X x = new X(); Y y = new Y(); Z z = new Z()) {
					        } catch (XException x) {
					        } catch (YException y) {
					        } catch (ZException z) {
					        }
					        System.out.println("Done");
					    }
					    public X() throws XException {
					        throw new XException();
					    }
					    public void close() throws XXException {
					        throw new XXException();
					    }
					}
					class Y implements AutoCloseable {
					    public Y() throws YException {
					        throw new YException();
					    }
					    public void close() throws YYException {
					        throw new YYException();
					    }
					}
					class Z implements AutoCloseable {
					    public Z() throws ZException {
					        throw new ZException();
					    }
					    public void close() throws ZZException {
					        throw new ZZException();
					    }
					}
					class XException extends Exception {}
					class XXException extends Exception {}
					class YException extends Exception {}
					class YYException extends Exception {}
					class ZException extends Exception {}
					class ZZException extends Exception {}
					"""
			},
			"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391092
public void testBug391092() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						try {
						} catch (NullPointerException  | ArrayIndexOutOfBoundsException  e []) {
						} catch (ClassCastException [] c) {
						} catch (ArrayStoreException a[]) {
						} catch (ArithmeticException | NegativeArraySizeException b[][] ) {
						} catch (ClassCastException[][] | ClassNotFoundException[] g) {
						}
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				} catch (NullPointerException  | ArrayIndexOutOfBoundsException  e []) {
				         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Illegal attempt to create arrays of union types
			----------
			2. ERROR in X.java (at line 5)
				} catch (ClassCastException [] c) {
				         ^^^^^^^^^^^^^^^^^^^^^
			No exception of type ClassCastException[] can be thrown; an exception type must be a subclass of Throwable
			----------
			3. ERROR in X.java (at line 6)
				} catch (ArrayStoreException a[]) {
				         ^^^^^^^^^^^^^^^^^^^^^^^
			No exception of type ArrayStoreException[] can be thrown; an exception type must be a subclass of Throwable
			----------
			4. ERROR in X.java (at line 7)
				} catch (ArithmeticException | NegativeArraySizeException b[][] ) {
				         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Illegal attempt to create arrays of union types
			----------
			5. ERROR in X.java (at line 8)
				} catch (ClassCastException[][] | ClassNotFoundException[] g) {
				         ^^^^^^^^^^^^^^^^^^^^^^
			No exception of type ClassCastException[][] can be thrown; an exception type must be a subclass of Throwable
			----------
			6. ERROR in X.java (at line 8)
				} catch (ClassCastException[][] | ClassNotFoundException[] g) {
				                                  ^^^^^^^^^^^^^^^^^^^^^^^^
			No exception of type ClassNotFoundException[] can be thrown; an exception type must be a subclass of Throwable
			----------
			""");
	}

//Bug 404146 - nested try-catch-finally-blocks leads to unrunnable Java byte code
public void testBug404146() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import javax.naming.NamingException;
				
				public final class X {
				
				    public static final void illegalStackMap() {
				        try {
				          try {
				            Y.decoy1();
				          } finally {
				            try {
				                Y.decoy2();
				            } catch (final IOException e) {
				              return;
				            }
				          }
				        } finally {
				          try {
				            Y.decoy3();
				          } catch (final NamingException e) {
				            return;
				          }
				        }
				    }
				}
				""",
			"Y.java",
				"""
					import java.io.IOException;
					import javax.naming.NamingException;
					public final class Y {
					
					    public static void decoy1() {}
					    public static void decoy2() throws IOException {}
					    public static void decoy3() throws NamingException {}
					}
					"""
		});
}
public void testBug488569_001() {
	if (this.complianceLevel < ClassFileConstants.JDK9) {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String [] args) throws Exception {
						    	Z z1 = new Z();
						        try (Y y1 = new Y(); z1;) {
						        } \s
						    } \s
						}
						class Y implements AutoCloseable {
							public void close() throws Exception {
							}
						}
						
						class Z implements AutoCloseable {
							public void close() throws Exception {
							}  \s
						}
						
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					try (Y y1 = new Y(); z1;) {
					                     ^^
				Variable resource not allowed here for source level below 9
				----------
				""");
	} else {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) throws Exception {
					    	Z z1 = new Z();
					        try (Y y1 = new Y(); z1;) {
					        } \s
					    } \s
					}
					class Y implements AutoCloseable {
						public void close() throws Exception {
						}
					}
					
					class Z implements AutoCloseable {
						public void close() throws Exception {
						}  \s
					}
					
					"""
			},
			"");

	}
}

public static Class testClass() {
	return TryStatement17Test.class;
}
}
