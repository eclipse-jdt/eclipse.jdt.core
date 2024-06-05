/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation and others.
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
 *     Stephan Herrmann - Contributions for
 *     							bug 358827 - [1.7] exception analysis for t-w-r spoils null analysis
 *     							bug 349326 - [1.7] new warning for missing try-with-resources
 *     							bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TryWithResourcesStatementTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test380112e"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryWithResourcesStatementTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
// Test resource type related errors
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (int i = 0) {
							System.out.println();
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (int i = 0) {
				     ^^^
			The resource type int does not implement java.lang.AutoCloseable
			----------
			""");
}
// Test resource type related errors
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (int[] tab = {}) {
							System.out.println();
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (int[] tab = {}) {
				     ^^^^^
			The resource type int[] does not implement java.lang.AutoCloseable
			----------
			""");
}
// Test that resource type could be interface type.
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable{
					public void method1(){
						try (AutoCloseable a = new X()) {
							System.out.println();
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X implements AutoCloseable{
				             ^
			The type X must implement the inherited abstract method AutoCloseable.close()
			----------
			2. ERROR in X.java (at line 3)
				try (AutoCloseable a = new X()) {
				                   ^
			Unhandled exception type Exception thrown by automatic close() invocation on a
			----------
			""");
}
// Type resource type related errors
public void test003a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y y = new Y()) {\s
							System.out.println();
						} catch (Exception e) {
						} finally {
				           Zork z;
						}
					}
				}
				class Y implements Managed {
				    public void close () throws Exception {
				    }
				}
				interface Managed extends AutoCloseable {}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// Scope, visibility related tests.
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
					public static void main(String[] args) throws IOException {
						int i = 0;
						try (LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {
							String s;
							int i = 0;
							while ((s = reader.readLine()) != null) {
								System.out.println(s);
								i++;
							}
							System.out.println("" + i + " lines");
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				int i = 0;
				    ^
			Duplicate local variable i
			----------
			""");
}
//Scope, visibility related tests.
public void test004a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
					public static void main(String[] args) throws IOException {
						try (LineNumberReader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {
							String s;
							int r = 0;
							while ((s = r.readLine()) != null) {
								System.out.println(s);
								r++;
							}
							System.out.println("" + r + " lines");
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				int r = 0;
				    ^
			Duplicate local variable r
			----------
			2. ERROR in X.java (at line 7)
				while ((s = r.readLine()) != null) {
				            ^^^^^^^^^^^^
			Cannot invoke readLine() on the primitive type int
			----------
			""");
}
// check that resources are implicitly final
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
					public static void main(String[] args) throws IOException {
						try (Reader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {
							r = new FileReader(args[0]);
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				r = new FileReader(args[0]);
				^
			The resource r of a try-with-resources statement cannot be assigned
			----------
			""");
}
//check that try statement can be empty
public void test006() {
	this.runNegativeTest( // cannot be a conform test as this triggers an AIOOB.
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
					public static void main(String[] args) throws IOException {
						try (Reader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {
						} catch(Zork z) {\
				       }
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				} catch(Zork z) {       }
				        ^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//check that resources are implicitly final but they can be explicitly final
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
					public static void main(String[] args) throws IOException {
						try (final Reader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {
							r = new FileReader(args[0]);
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				r = new FileReader(args[0]);
				^
			The resource r of a try-with-resources statement cannot be assigned
			----------
			""");
}
// resource type tests
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y [] i = null) {
							System.out.println();
						}
					}
				}
				class Y implements AutoCloseable {
				    public void close () {}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Y [] i = null) {
				     ^^^^
			The resource type Y[] does not implement java.lang.AutoCloseable
			----------
			""");
}
// Resource Type tests
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y i [] = null) {
							System.out.println();
						}
					}
				}
				class Y implements AutoCloseable {
				    public void close () {}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Y i [] = null) {
				     ^
			The resource type Y[] does not implement java.lang.AutoCloseable
			----------
			""");
}
// Scope, visibility tests
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(int p){
				       int k;
						try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {
							System.out.println();
						}
					}
				}
				class Y implements AutoCloseable {
				    public void close () {}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {
				                      ^
			Duplicate local variable i
			----------
			2. ERROR in X.java (at line 4)
				try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {
				                                     ^
			Duplicate local variable p
			----------
			3. ERROR in X.java (at line 4)
				try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {
				                                                    ^
			Duplicate local variable k
			----------
			""");
}
// Scope, visibility tests
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y i = new Y(); Y p = new Y(); Y k = new Y();) {
							System.out.println();
						}
				       catch (Exception e) {
				           System.out.println(i);
				       }
				       finally {
				           System.out.println(p);
				       }
					}
				}
				class Y implements AutoCloseable {
				    public void close () {}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				System.out.println(i);
				                   ^
			i cannot be resolved to a variable
			----------
			2. ERROR in X.java (at line 10)
				System.out.println(p);
				                   ^
			p cannot be resolved to a variable
			---\
			-------
			""");
}
// Scope, visibility related tests.
public void test012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y i = new Y(); Y p = new Y(); Y k = new Y();) {
				           try {
							    System.out.println();
				           } catch (Exception i) {
				           }
						}
				       catch (Exception e) {
				           System.out.println(i);
				       }
				       finally {
				           System.out.println(p);
				       }
					}
				}
				class Y implements AutoCloseable {
				    public void close () {}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				} catch (Exception i) {
				                   ^
			Duplicate parameter i
			----------
			2. ERROR in X.java (at line 10)
				System.out.println(i);
				                   ^
			i cannot be resolved to a variable
			----------
			3. ERROR in X.java (at line 13)
				System.out.println(p);
				                   ^
			p cannot be resolved to a variable
			----------
			""");
}
// Shadowing behavior tests
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String [] args) {
					try (Y y = new Y(); Y p = new Y()) {
					    X x = new X() {
						      public void foo(int p) {
				                         try {
						             System.out.println();
						          } catch (Exception y) {
						          }
						       }
					           };
					} finally {
				            System.out.println(y);
					}
				   }
				}
				
				class Y implements AutoCloseable {
					public void close() {
						    System.out.println();
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				public void foo(int p) {
				                    ^
			The parameter p is hiding another local variable defined in an enclosing scope
			----------
			2. WARNING in X.java (at line 8)
				} catch (Exception y) {
				                   ^
			The parameter y is hiding another local variable defined in an enclosing scope
			----------
			3. ERROR in X.java (at line 13)
				System.out.println(y);
				                   ^
			y cannot be resolved to a variable
			----------
			""");
}
// Test for unhandled exceptions
public void test014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {   \s
						try (Y y = new Y();) {
				           if (y == null) {}
				           Y why = new Y();
						    System.out.println("Try block");
						} finally {
						    System.out.println("Finally block");
						}
					}
				}\s
				
				class Y implements AutoCloseable {
					public Y() throws WeirdException {
						throw new WeirdException();
					}
					public void close() {
						    System.out.println("Closing resource");
					}
				}
				
				class WeirdException extends Throwable {}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Y y = new Y();) {
				           ^^^^^^^
			Unhandled exception type WeirdException
			----------
			2. WARNING in X.java (at line 4)
				if (y == null) {}
				               ^^
			Dead code
			----------
			3. WARNING in X.java (at line 5)
				Y why = new Y();
				  ^^^
			Resource leak: 'why' is never closed
			----------
			4. ERROR in X.java (at line 5)
				Y why = new Y();
				        ^^^^^^^
			Unhandled exception type WeirdException
			----------
			5. WARNING in X.java (at line 22)
				class WeirdException extends Throwable {}
				      ^^^^^^^^^^^^^^
			The serializable class WeirdException does not declare a static final serialVersionUID field of type long
			----------
			""",
		null, true, options);
}
// Resource nullness tests
public void test015() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {   \s
						try (Y y = new Y();) {
				           if (y == null)
								{}
						}
					}
				}\s
				
				class Y implements AutoCloseable {
					public void close() {
					}
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in X.java (at line 5)
				{}
				^^
			Dead code
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// Dead code tests, resource nullness, unhandled exception tests
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {   \s
						try (Y y = new Y();) {
				           if (y == null) {}
				           Y why = new Y();
						    System.out.println("Try block");
						}
					}
				}\s
				
				class Y implements AutoCloseable {
					public Y() throws WeirdException {
						throw new WeirdException();
					}
					public void close() {
						    System.out.println("Closing resource");
					}
				}
				
				class WeirdException extends Throwable {}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Y y = new Y();) {
				           ^^^^^^^
			Unhandled exception type WeirdException
			----------
			2. WARNING in X.java (at line 4)
				if (y == null) {}
				               ^^
			Dead code
			----------
			3. WARNING in X.java (at line 5)
				Y why = new Y();
				  ^^^
			Resource leak: 'why' is never closed
			----------
			4. ERROR in X.java (at line 5)
				Y why = new Y();
				        ^^^^^^^
			Unhandled exception type WeirdException
			----------
			5. WARNING in X.java (at line 20)
				class WeirdException extends Throwable {}
				      ^^^^^^^^^^^^^^
			The serializable class WeirdException does not declare a static final serialVersionUID field of type long
			----------
			""",
		null,
		true,
		options);
}
// Dead code tests
public void test017() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {   \s
						try (Y y = new Y();) {
				           if (y == null)
								{}
						} finally {
				       }
					}
				}\s
				
				class Y implements AutoCloseable {
					public void close() {
					}
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in X.java (at line 5)
				{}
				^^
			Dead code
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// Syntax error tests
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {   \s
						try () {
						} finally {
				       }
					}
				}\s
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try () {
				    ^
			Syntax error on token "(", Resources expected after this token
			----------
			""");
}
// Unhandled exception tests
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
					public static void main(String [] args) {
				            try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				            throw new XXException();
				            } catch (XException x) {
					 		 } catch (YException y) {
				            } catch (ZException z) {
					    	 } finally {
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
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				       ^
			Unhandled exception type XXException thrown by automatic close() invocation on x
			----------
			2. ERROR in X.java (at line 3)
				try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				                      ^
			Unhandled exception type YYException thrown by automatic close() invocation on y
			----------
			3. ERROR in X.java (at line 3)
				try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				                                     ^
			Unhandled exception type ZZException thrown by automatic close() invocation on z
			----------
			4. ERROR in X.java (at line 4)
				throw new XXException();
				^^^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type XXException
			----------
			5. WARNING in X.java (at line 34)
				class XException extends Exception {}
				      ^^^^^^^^^^
			The serializable class XException does not declare a static final serialVersionUID field of type long
			----------
			6. WARNING in X.java (at line 35)
				class XXException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class XXException does not declare a static final serialVersionUID field of type long
			----------
			7. WARNING in X.java (at line 36)
				class YException extends Exception {}
				      ^^^^^^^^^^
			The serializable class YException does not declare a static final serialVersionUID field of type long
			----------
			8. WARNING in X.java (at line 37)
				class YYException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class YYException does not declare a static final serialVersionUID field of type long
			----------
			9. WARNING in X.java (at line 38)
				class ZException extends Exception {}
				      ^^^^^^^^^^
			The serializable class ZException does not declare a static final serialVersionUID field of type long
			----------
			10. WARNING in X.java (at line 39)
				class ZZException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class ZZException does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// Resource type test
public void test021() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y i = null) {
							System.out.println();
						}
					}
				}
				class Y {
				    public void close () {}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Y i = null) {
				     ^
			The resource type Y does not implement java.lang.AutoCloseable
			----------
			""");
}
// Interface method return type compatibility test
public void test022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y i = null) {
							System.out.println();
						}
					}
				}
				class Y implements AutoCloseable {
				    public int close () { return 0; }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				public int close () { return 0; }
				       ^^^
			The return type is incompatible with AutoCloseable.close()
			----------
			""");
}
// Exception handling, compatibility tests
public void test023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y i = null) {
							System.out.println();
						}
					}
				}
				class Y implements AutoCloseable {
				    public void close () throws Blah {}
				}
				class Blah extends Throwable {}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Y i = null) {
				       ^
			Unhandled exception type Blah thrown by automatic close() invocation on i
			----------
			2. ERROR in X.java (at line 9)
				public void close () throws Blah {}
				            ^^^^^^^^^^^^^^^^^^^^
			Exception Blah is not compatible with throws clause in AutoCloseable.close()
			----------
			3. WARNING in X.java (at line 11)
				class Blah extends Throwable {}
				      ^^^^
			The serializable class Blah does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// Exception handling tests
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
					public static void main(String [] args) {
				            try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				            throw new XXException();
				            } catch (XException x) {
					 		 } catch (YException y) {
				            } catch (ZException z) {
				            } catch (XXException x) {
					 		 } catch (YYException y) {
				            } catch (ZZException z) {
					    	 } finally {
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
		"""
			----------
			1. WARNING in X.java (at line 37)
				class XException extends Exception {}
				      ^^^^^^^^^^
			The serializable class XException does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 38)
				class XXException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class XXException does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 39)
				class YException extends Exception {}
				      ^^^^^^^^^^
			The serializable class YException does not declare a static final serialVersionUID field of type long
			----------
			4. WARNING in X.java (at line 40)
				class YYException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class YYException does not declare a static final serialVersionUID field of type long
			----------
			5. WARNING in X.java (at line 41)
				class ZException extends Exception {}
				      ^^^^^^^^^^
			The serializable class ZException does not declare a static final serialVersionUID field of type long
			----------
			6. WARNING in X.java (at line 42)
				class ZZException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class ZZException does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// Unhandled exception tests
public void test025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
					public static void main(String [] args) {
				            try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				            throw new XXException();
				            } catch (XException x) {
					 		 } catch (YException y) {
				            } catch (ZException z) {
				           \s
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
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				       ^
			Unhandled exception type XXException thrown by automatic close() invocation on x
			----------
			2. ERROR in X.java (at line 3)
				try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				                      ^
			Unhandled exception type YYException thrown by automatic close() invocation on y
			----------
			3. ERROR in X.java (at line 3)
				try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				                                     ^
			Unhandled exception type ZZException thrown by automatic close() invocation on z
			----------
			4. ERROR in X.java (at line 4)
				throw new XXException();
				^^^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type XXException
			----------
			5. WARNING in X.java (at line 34)
				class XException extends Exception {}
				      ^^^^^^^^^^
			The serializable class XException does not declare a static final serialVersionUID field of type long
			----------
			6. WARNING in X.java (at line 35)
				class XXException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class XXException does not declare a static final serialVersionUID field of type long
			----------
			7. WARNING in X.java (at line 36)
				class YException extends Exception {}
				      ^^^^^^^^^^
			The serializable class YException does not declare a static final serialVersionUID field of type long
			----------
			8. WARNING in X.java (at line 37)
				class YYException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class YYException does not declare a static final serialVersionUID field of type long
			----------
			9. WARNING in X.java (at line 38)
				class ZException extends Exception {}
				      ^^^^^^^^^^
			The serializable class ZException does not declare a static final serialVersionUID field of type long
			----------
			10. WARNING in X.java (at line 39)
				class ZZException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class ZZException does not declare a static final serialVersionUID field of type long
			----------
			""");
}
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
					public static void main(String [] args) {
				            try (X x = new X(); Y y = new Y(); Z z = new Z()) {
				            throw new XXException();
				            } catch (XException x) {
					 		 } catch (YException y) {
				            } catch (ZException z) {
				            } catch (XXException x) {
					 		 } catch (YYException y) {
				            } catch (ZZException z) {
				
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
		"""
			----------
			1. WARNING in X.java (at line 37)
				class XException extends Exception {}
				      ^^^^^^^^^^
			The serializable class XException does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 38)
				class XXException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class XXException does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 39)
				class YException extends Exception {}
				      ^^^^^^^^^^
			The serializable class YException does not declare a static final serialVersionUID field of type long
			----------
			4. WARNING in X.java (at line 40)
				class YYException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class YYException does not declare a static final serialVersionUID field of type long
			----------
			5. WARNING in X.java (at line 41)
				class ZException extends Exception {}
				      ^^^^^^^^^^
			The serializable class ZException does not declare a static final serialVersionUID field of type long
			----------
			6. WARNING in X.java (at line 42)
				class ZZException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class ZZException does not declare a static final serialVersionUID field of type long
			----------
			""");
}
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
				    public static void main(String [] args) throws Exception {
				        try (X x = new X(); Y y = new Y()) {
				            System.out.println("Body");
				            throw new Exception("Body");
				        } catch (Exception e) {
				            System.out.println(e);
				            Throwable [] suppressed = e.getSuppressed();
				            for (int i = 0; i < suppressed.length; i++) {
				                System.out.println("Suppressed:" + suppressed[i]);
				            }
				        } finally {
				            int finallyVar = 10;
				            System.out.println(finallyVar);
				        }
				    }
				    public X() {
				        System.out.println("X CTOR");
				    }
				    public void close() throws Exception {
				        System.out.println("X Close");
				        throw new Exception("X Close");
				    }
				}
				class Y implements AutoCloseable {
				    public Y() {
				        System.out.println("Y CTOR");
				    }
				    public void close() throws Exception {
				        System.out.println("Y Close");
				        throw new Exception("Y Close");
				    }
				}
				"""
		},
		"""
			X CTOR
			Y CTOR
			Body
			Y Close
			X Close
			java.lang.Exception: Body
			Suppressed:java.lang.Exception: Y Close
			Suppressed:java.lang.Exception: X Close
			10""");
}
public void test028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
				    public static void main(String [] args) throws Exception {
				        try (X x = new X(); Y y = new Y()) {
				            System.out.println("Body");
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
				    }
				    public X() {
				        System.out.println("X CTOR");
				    }
				    public void close() {
				        System.out.println("X DTOR");
				    }
				}
				class Y implements AutoCloseable {
				    public Y() {
				        System.out.println("Y CTOR");
				    }
				    public void close() {
				        System.out.println("Y DTOR");
				    }
				}
				"""
		},
		"""
			X CTOR
			Y CTOR
			Body
			Y DTOR
			X DTOR""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338881
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() {
				        File file = new File("somefile");
				        try(FileReader fileReader = new FileReader(file);) {
				            char[] in = new char[50];
				            fileReader.read(in);
				        } catch (IOException e) {
				            System.out.println("Got IO exception");
				        } finally{
				        }
				    }
				    public static void main(String[] args) {
				        new X().foo();
				    }
				}
				"""
		},
		"Got IO exception");
}
public void test030() {  // test return + resources
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
				    public static void main(String [] args) throws Exception {\s
				    	final boolean getOut = true;
				    	System.out.println("Main");
				    	try (X x1 = new X(); X x2 = new X()) {
				            System.out.println("Outer Try");
				            while (true) {
				            	try (Y y1 = new Y(); Y y2 = new Y()) {
				            		System.out.println("Middle Try");
				            		try (Z z1 = new Z(); Z z2 = new Z()) {
				            			System.out.println("Inner Try");
				            			if (getOut)\s
				            				return;
				            			else
				            				break;
				            		}
				            	}
				            }
				            System.out.println("Out of while");
				        }
				    }
				    public X() {
				        System.out.println("X::X");
				    }
				    public void close() throws Exception {
				        System.out.println("X::~X");
				    }
				}
				class Y implements AutoCloseable {
				    public Y() {
				        System.out.println("Y::Y");
				    }
				    public void close() throws Exception {
				        System.out.println("Y::~Y");
				    }
				}
				class Z implements AutoCloseable {
				    public Z() {
				        System.out.println("Z::Z");
				    }
				    public void close() throws Exception {
				        System.out.println("Z::~Z");
				    }
				}
				"""
		},
		"""
			Main
			X::X
			X::X
			Outer Try
			Y::Y
			Y::Y
			Middle Try
			Z::Z
			Z::Z
			Inner Try
			Z::~Z
			Z::~Z
			Y::~Y
			Y::~Y
			X::~X
			X::~X""");
}
public void test030a() {  // test return + resources + with exceptions being thrown by close()
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
				    public static void main(String [] args) throws Exception {\s
				    	final boolean getOut = true;
				    	System.out.println("Main");
				    	try (X x1 = new X(); X x2 = new X()) {
				            System.out.println("Outer Try");
				            while (true) {
				            	try (Y y1 = new Y(); Y y2 = new Y()) {
				            		System.out.println("Middle Try");
				            		try (Z z1 = new Z(); Z z2 = new Z()) {
				            			System.out.println("Inner Try");
				            			if (getOut)\s
				            				return;
				            			else
				            				break;
				            		}
				            	}
				            }
				            System.out.println("Out of while");
				        } catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
				        }
				    }
				    public X() {
				        System.out.println("X::X");
				    }
				    public void close() throws Exception {
				        System.out.println("X::~X");
				        throw new Exception("X::~X");
				    }
				}
				class Y implements AutoCloseable {
				    public Y() {
				        System.out.println("Y::Y");
				    }
				    public void close() throws Exception {
				        System.out.println("Y::~Y");
				        throw new Exception("Y::~Y");
				    }
				}
				class Z implements AutoCloseable {
				    public Z() {
				        System.out.println("Z::Z");
				    }
				    public void close() throws Exception {
				        System.out.println("Z::~Z");
				        throw new Exception("Z::~Z");
				    }
				}
				"""
		},
		"""
			Main
			X::X
			X::X
			Outer Try
			Y::Y
			Y::Y
			Middle Try
			Z::Z
			Z::Z
			Inner Try
			Z::~Z
			Z::~Z
			Y::~Y
			Y::~Y
			X::~X
			X::~X
			java.lang.Exception: Z::~Z
			Suppressed: java.lang.Exception: Z::~Z
			Suppressed: java.lang.Exception: Y::~Y
			Suppressed: java.lang.Exception: Y::~Y
			Suppressed: java.lang.Exception: X::~X
			Suppressed: java.lang.Exception: X::~X""");
}
public void test031() { // test break + resources
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
				    public static void main(String [] args) throws Exception {\s
				    	final boolean getOut = false;
				    	System.out.println("Main");
				    	try (X x1 = new X(); X x2 = new X()) {
				            System.out.println("Outer Try");
				            while (true) {
				            	try (Y y1 = new Y(); Y y2 = new Y()) {
				            		System.out.println("Middle Try");
				            		try (Z z1 = new Z(); Z z2 = new Z()) {
				            			System.out.println("Inner Try");
				            			if (getOut)\s
				            				return;
				            			else
				            				break;
				            		}
				            	}
				            }
				            System.out.println("Out of while");
				        }
				    }
				    public X() {
				        System.out.println("X::X");
				    }
				    public void close() throws Exception {
				        System.out.println("X::~X");
				    }
				}
				class Y implements AutoCloseable {
				    public Y() {
				        System.out.println("Y::Y");
				    }
				    public void close() throws Exception {
				        System.out.println("Y::~Y");
				    }
				}
				class Z implements AutoCloseable {
				    public Z() {
				        System.out.println("Z::Z");
				    }
				    public void close() throws Exception {
				        System.out.println("Z::~Z");
				    }
				}
				"""
		},
		"""
			Main
			X::X
			X::X
			Outer Try
			Y::Y
			Y::Y
			Middle Try
			Z::Z
			Z::Z
			Inner Try
			Z::~Z
			Z::~Z
			Y::~Y
			Y::~Y
			Out of while
			X::~X
			X::~X""");
}
public void test032() { // test continue + resources
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
				    public static void main(String [] args) throws Exception {\s
				    	final boolean getOut = false;
				    	System.out.println("Main");
				    	try (X x1 = new X(); X x2 = new X()) {
				            System.out.println("Outer Try");
				            boolean more = true;
				            while (more) {
				            	try (Y y1 = new Y(); Y y2 = new Y()) {
				            		System.out.println("Middle Try");
				            		try (Z z1 = new Z(); Z z2 = new Z()) {
				            			System.out.println("Inner Try");
				                       more = false;
				                       continue;
				            		} finally {\s
				                       System.out.println("Inner Finally");
				                   }
				            	} finally {
				                   System.out.println("Middle Finally");
				               }
				            }
				            System.out.println("Out of while");
				        } finally {
				            System.out.println("Outer Finally");
				        }
				    }
				    public X() {
				        System.out.println("X::X");
				    }
				    public void close() throws Exception {
				        System.out.println("X::~X");
				    }
				}
				class Y implements AutoCloseable {
				    public Y() {
				        System.out.println("Y::Y");
				    }
				    public void close() throws Exception {
				        System.out.println("Y::~Y");
				    }
				}
				class Z implements AutoCloseable {
				    public Z() {
				        System.out.println("Z::Z");
				    }
				    public void close() throws Exception {
				        System.out.println("Z::~Z");
				    }
				}
				"""
		},
		"""
			Main
			X::X
			X::X
			Outer Try
			Y::Y
			Y::Y
			Middle Try
			Z::Z
			Z::Z
			Inner Try
			Z::~Z
			Z::~Z
			Inner Finally
			Y::~Y
			Y::~Y
			Middle Finally
			Out of while
			X::~X
			X::~X
			Outer Finally""");
}
public void test033() { // test null resources
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
				    public static void main(String [] args) throws Exception {\s
				    	final boolean getOut = false;
				    	System.out.println("Main");
				    	try (X x1 = null; Y y = new Y(); Z z = null) {
				            System.out.println("Body");
				        } finally {
				            System.out.println("Outer Finally");
				        }
				    }
				    public X() {
				        System.out.println("X::X");
				    }
				    public void close() throws Exception {
				        System.out.println("X::~X");
				    }
				}
				class Y implements AutoCloseable {
				    public Y() {
				        System.out.println("Y::Y");
				    }
				    public void close() throws Exception {
				        System.out.println("Y::~Y");
				    }
				}
				class Z implements AutoCloseable {
				    public Z() {
				        System.out.println("Z::Z");
				    }
				    public void close() throws Exception {
				        System.out.println("Z::~Z");
				    }
				}
				"""
		},
		"""
			Main
			Y::Y
			Body
			Y::~Y
			Outer Finally""");
}
public void test034() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
									throw new Exception("Body");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println(suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
						throw new Exception ("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
						throw new Exception ("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
						throw new Exception ("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
						throw new Exception ("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
						throw new Exception ("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
						throw new Exception ("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
						throw new Exception ("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			java.lang.Exception: A::A
			All done""");
}
public void test035() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
									throw new Exception("Body");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
						throw new Exception ("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
						throw new Exception ("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
						throw new Exception ("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
						throw new Exception ("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
						throw new Exception ("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
						throw new Exception ("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			A::~A
			java.lang.Exception: B::B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test036() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
									throw new Exception("Body");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
						throw new Exception ("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
						throw new Exception ("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
						throw new Exception ("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
						throw new Exception ("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
						throw new Exception ("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			B::~B
			A::~A
			java.lang.Exception: C::C
			Suppressed: java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test037() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
									throw new Exception("Body");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
						throw new Exception ("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
						throw new Exception ("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
						throw new Exception ("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
						throw new Exception ("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: D::D
			Suppressed: java.lang.Exception: C::~C
			Suppressed: java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
									throw new Exception("Body");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
						throw new Exception ("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
						throw new Exception ("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
						throw new Exception ("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			D::~D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: E::E
			Suppressed: java.lang.Exception: D::~D
			Suppressed: java.lang.Exception: C::~C
			Suppressed: java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
									throw new Exception("Body");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
						throw new Exception ("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
						throw new Exception ("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			F::F
			E::~E
			D::~D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: F::F
			Suppressed: java.lang.Exception: E::~E
			Suppressed: java.lang.Exception: D::~D
			Suppressed: java.lang.Exception: C::~C
			Suppressed: java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test040() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
									throw new Exception("Body");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
						throw new Exception ("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			F::F
			Inner try
			F::~F
			E::~E
			D::~D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: Body
			Suppressed: java.lang.Exception: F::~F
			Suppressed: java.lang.Exception: E::~E
			Suppressed: java.lang.Exception: D::~D
			Suppressed: java.lang.Exception: C::~C
			Suppressed: java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test041() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
						throw new Exception ("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			F::F
			Inner try
			F::~F
			E::~E
			D::~D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: F::~F
			Suppressed: java.lang.Exception: E::~E
			Suppressed: java.lang.Exception: D::~D
			Suppressed: java.lang.Exception: C::~C
			Suppressed: java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test042() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			F::F
			Inner try
			F::~F
			E::~E
			D::~D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: E::~E
			Suppressed: java.lang.Exception: D::~D
			Suppressed: java.lang.Exception: C::~C
			Suppressed: java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test043() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
						throw new Exception ("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			F::F
			Inner try
			F::~F
			E::~E
			D::~D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: D::~D
			Suppressed: java.lang.Exception: C::~C
			Suppressed: java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test044() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			F::F
			Inner try
			F::~F
			E::~E
			D::~D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: C::~C
			Suppressed: java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						throw new Exception ("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			F::F
			Inner try
			F::~F
			E::~E
			D::~D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
public void test046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						throw new Exception ("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			F::F
			Inner try
			F::~F
			E::~E
			D::~D
			C::~C
			B::~B
			A::~A
			java.lang.Exception: A::~A
			All done""");
}
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A(); B b = new B()) {
							System.out.println("Outer try");
							try (C c = new C(); D d = new D();) {
								System.out.println("Middle try");
								try (E e = new E(); F f = new F()) {
									System.out.println("Inner try");
								}\s
							}
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
					}
				}
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
					}
				}
				class D implements AutoCloseable {
					public D () throws Exception {
						System.out.println("D::D");
					}
					public void close() throws Exception {
						System.out.println("D::~D");
					}
				}
				class E implements AutoCloseable {
					public E () throws Exception {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
					}
				}
				class F implements AutoCloseable {
					public F () throws Exception {
						System.out.println("F::F");
					}
					public void close() throws Exception {
						System.out.println("F::~F");
					}
				}
				class G implements AutoCloseable {
					public G () throws Exception {
						System.out.println("G::G");
						throw new Exception ("G::G");
					}
					public void close() throws Exception {
						System.out.println("G::~G");
						throw new Exception ("G::~G");
					}
				}
				"""
		},
		"""
			Main
			A::A
			B::B
			Outer try
			C::C
			D::D
			Middle try
			E::E
			F::F
			Inner try
			F::~F
			E::~E
			D::~D
			C::~C
			B::~B
			A::~A
			All done""");
}
public void test048() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (A a = new A()) {
							System.out.println("X::Try");
							throw new Exception("X::Main");
						} catch (Exception e) {
								System.out.println(e);
								Throwable suppressed [] = e.getSuppressed();
								for (int i = 0; i < suppressed.length; ++i) {
									System.out.println("Suppressed: " + suppressed[i]);
								}
						} finally {
							System.out.println("All done");
						}
					}
				}
				
				class A implements AutoCloseable {
					public A () throws Exception {
						System.out.println("A::A");
					}
					public void close() throws Exception {
						System.out.println("A::~A");
						try (B b = new B()) {
							System.out.println("A::~A::Try");
							throw new Exception("A::~A");
						} catch (Exception e) {
								System.out.println(e);
								Throwable suppressed [] = e.getSuppressed();
								for (int i = 0; i < suppressed.length; ++i) {
									System.out.println("Suppressed: " + suppressed[i]);
								}
								throw e;
						} \t
					}
				}
				
				class B implements AutoCloseable {
					public B () throws Exception {
						System.out.println("B::B");
					}
					public void close() throws Exception {
						System.out.println("B::~B");
						try (C c = new C()) {
							System.out.println("B::~B::Try");
							throw new Exception ("B::~B");
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
							throw e;
					} \t
					}
				}
				class C implements AutoCloseable {
					public C () throws Exception {
						System.out.println("C::C");
					}
					public void close() throws Exception {
						System.out.println("C::~C");
						throw new Exception ("C::~C");
					}\s
				}
				"""
		},
		"""
			Main
			A::A
			X::Try
			A::~A
			B::B
			A::~A::Try
			B::~B
			C::C
			B::~B::Try
			C::~C
			java.lang.Exception: B::~B
			Suppressed: java.lang.Exception: C::~C
			java.lang.Exception: A::~A
			Suppressed: java.lang.Exception: B::~B
			java.lang.Exception: X::Main
			Suppressed: java.lang.Exception: A::~A
			All done""");
}
//ensure that it doesn't completely fail when using TWR and 1.5 mode
public void test049() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	runner.customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	runner.customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.FileReader;
				import java.io.IOException;
				public class X {
				    void foo() {
				        File file = new File("somefile");
				        try(FileReader fileReader = new FileReader(file);) {
				            char[] in = new char[50];
				            fileReader.read(in);
				        } catch (IOException e) {
				            System.out.println("Got IO exception");
				        } finally{
				        }
				    }
				    public static void main(String[] args) {
				        new X().foo();
				    }
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 7)
				try(FileReader fileReader = new FileReader(file);) {
				    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource specification not allowed here for source level below 1.7
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.forRelease("5");
	runner.runNegativeTest();
}
public void test050() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						System.out.println("Main");
						try (E e = E.CONST) {
							System.out.println("Outer try");
						} catch (Exception e) {
							System.out.println(e);
							Throwable suppressed [] = e.getSuppressed();
							for (int i = 0; i < suppressed.length; ++i) {
								System.out.println("Suppressed: " + suppressed[i]);
							}
						} finally {
							System.out.println("All done");
						}
					}
				}""",
			"E.java",
			"""
				public enum E implements AutoCloseable {
					CONST;
					private E () {
						System.out.println("E::E");
					}
					public void close() throws Exception {
						System.out.println("E::~E");
						throw new Exception ("E::~E");
					}
				}"""
		},
		"""
			Main
			E::E
			Outer try
			E::~E
			java.lang.Exception: E::~E
			All done""");
}
public void test051() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) throws Throwable {
				        try (Test t = new Test()) {
				            for (int i = 0; i < 10; i++) {
				            }
				
				
				        }\s
				
				        catch (Exception e) {
				            StackTraceElement t = e.getStackTrace()[1];
				            String file = t.getFileName();
				            int line = t.getLineNumber();
				            System.out.println("File = " + file + " " + "line = " + line);
				        }
				    }
				}
				class Test implements AutoCloseable {
				    public void close() throws Exception {
				        throw new Exception();
				    }
				}
				"""
		},
		"File = X.java line = 8");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348406
public void test052() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) throws Throwable {
				        try (Test t = new Test()) {
				        }\s
				    }
				}
				class Test {
				    public void close() throws Exception {
				        throw new Exception();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Test t = new Test()) {
				     ^^^^^^^^^^^^^^^^^^^
			Resource specification not allowed here for source level below 1.7
			----------
			""",
		null,
		true,
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348705
// Unhandled exception due to autoclose should be reported separately
public void test053() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y y = new Y()) {\s
							y.close();
							System.out.println();
						} catch (RuntimeException e) {
						}
					}
				}
				class Y implements Managed {
					 public Y() throws CloneNotSupportedException {}
				    public void close () throws ClassNotFoundException, java.io.IOException {
				    }
				}
				interface Managed extends AutoCloseable {}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Y y = new Y()) {\s
				       ^
			Unhandled exception type ClassNotFoundException thrown by automatic close() invocation on y
			----------
			2. ERROR in X.java (at line 3)
				try (Y y = new Y()) {\s
				       ^
			Unhandled exception type IOException thrown by automatic close() invocation on y
			----------
			3. ERROR in X.java (at line 3)
				try (Y y = new Y()) {\s
				           ^^^^^^^
			Unhandled exception type CloneNotSupportedException
			----------
			4. ERROR in X.java (at line 4)
				y.close();
				^^^^^^^^^
			Unhandled exception type ClassNotFoundException
			----------
			5. ERROR in X.java (at line 4)
				y.close();
				^^^^^^^^^
			Unhandled exception type IOException
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348705
// Variant of the above, witness for https://bugs.eclipse.org/358827#c6
public void test053a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void method1(){
						try (Y y = new Y()) {\s
							y.close();
							System.out.println();
						} catch (RuntimeException e) {
				       } finally {
				           System.out.println();
						}
					}
				}
				class Y implements Managed {
					 public Y() throws CloneNotSupportedException {}
				    public void close () throws ClassNotFoundException, java.io.IOException {
				    }
				}
				interface Managed extends AutoCloseable {}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Y y = new Y()) {\s
				       ^
			Unhandled exception type ClassNotFoundException thrown by automatic close() invocation on y
			----------
			2. ERROR in X.java (at line 3)
				try (Y y = new Y()) {\s
				       ^
			Unhandled exception type IOException thrown by automatic close() invocation on y
			----------
			3. ERROR in X.java (at line 3)
				try (Y y = new Y()) {\s
				           ^^^^^^^
			Unhandled exception type CloneNotSupportedException
			----------
			4. ERROR in X.java (at line 4)
				y.close();
				^^^^^^^^^
			Unhandled exception type ClassNotFoundException
			----------
			5. ERROR in X.java (at line 4)
				y.close();
				^^^^^^^^^
			Unhandled exception type IOException
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349862 (NPE when union type is used in the resource section.)
public void test054() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo() {
				        try (Object | Integer res = null) {
				        } catch (Exception e) {
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Object | Integer res = null) {
				            ^
			Syntax error on token "|", . expected
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349862 (NPE when union type is used in the resource section.)
public void test054a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo() {
				        try (Object.Integer res = null) {
				        } catch (Exception e) {
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (Object.Integer res = null) {
				     ^^^^^^^^^^^^^^
			Object.Integer cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353535 (verify error with try with resources)
public void test055() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.ByteArrayInputStream;
				import java.io.InputStream;
				public class X {
				public static void main(String[] args) throws Exception {
				  int b;
				  try (final InputStream in = new ByteArrayInputStream(new byte[] { 42 })) {
				    b = in.read();
				  }
				  System.out.println("Done");
				}
				}
				""",
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353535 (verify error with try with resources)
public void test055a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) throws Throwable {
				        int tmp;
				        try (A a = null) {
				            try (A b = null) {
				                tmp = 0;
				            }
				        }
				        System.out.println("Done");
				    }
				}
				class A implements AutoCloseable {
				    @Override
				    public void close() {
				    }
				}
				""",
		},
		"Done");
}

// Note: test056* have been moved to ResourceLeakTests.java

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361053
public void test057() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
					@Override
					public void close() throws Exception {
						throw new Exception();
					}
					public static void main(String[] args) {
						final boolean foo;
						try (X a = new X(); X b = new X()) {
							foo = true;
						} catch (final Exception exception) {
							return;
						}
					}
				}
				"""
		},  "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=364008
public void test058() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.ByteArrayOutputStream;
				import java.io.FileOutputStream;
				import java.io.IOException;
				
				public class X {
				
				  public static void main(final String[] args) throws IOException {
				    byte[] data;
				    try (final ByteArrayOutputStream os = new ByteArrayOutputStream();
				         final FileOutputStream out = new FileOutputStream("test.dat")) {
				      data = os.toByteArray();
				    }
				  }
				}
				"""
		},  "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367566 - In try-with-resources statement close() method of resource is not called
public void test059() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X implements java.lang.AutoCloseable {
				  static boolean isOpen = true;
				  public static void main(final String[] args) throws IOException {
				    foo();
				    System.out.println(isOpen);
				  }
				  static boolean foo() {
				    try (final X x = new X()) {
				      return x.num() >= 1;
				    }
				  }
				  int num() { return 2; }
				  public void close() {
				    isOpen = false;
				  }
				}
				"""
		},
		"false");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367566 - In try-with-resources statement close() method of resource is not called
public void test060() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements AutoCloseable {
					static int num = 10 ;
				    public static void main(String [] args) throws Exception {\s
				    	System.out.println(foo(1));
				    	System.out.println(foo(2));
				    	System.out.println(foo(3));
				    }
					private static boolean foo(int where) throws Exception {
						final boolean getOut = true;
				    	System.out.println("Main");
				    	try (X x1 = new X(); X x2 = new X()) {
				    		if (where == 1) {
				    			return where == 1;
				    		}
				            System.out.println("Outer Try");
				            while (true) {
				            	try (Y y1 = new Y(); Y y2 = new Y()) {\s
				            		if (where == 2) {
				            			return where == 2;
				            		}	\t
				            		System.out.println("Middle Try");
				            		try (Z z1 = new Z(); Z z2 = new Z()) {
				            			System.out.println("Inner Try");
				            			if (getOut)\s
				            				return num >= 10;
				            			else
				            				break;\s
				            		}
				            	}
				            }
				            System.out.println("Out of while");
				        }
						return false;
					}
				    public X() {
				        System.out.println("X::X");
				    }
				    @Override
					public void close() throws Exception {
				        System.out.println("X::~X");
				    }
				}
				class Y implements AutoCloseable {
				    public Y() {
				        System.out.println("Y::Y");
				    }
				    @Override
					public void close() throws Exception {
				        System.out.println("Y::~Y");
				    }
				}
				class Z implements AutoCloseable {
				    public Z() {
				        System.out.println("Z::Z");
				    }
				    @Override
					public void close() throws Exception {
				        System.out.println("Z::~Z");
				    }
				}
				"""
		},
		"""
			Main
			X::X
			X::X
			X::~X
			X::~X
			true
			Main
			X::X
			X::X
			Outer Try
			Y::Y
			Y::Y
			Y::~Y
			Y::~Y
			X::~X
			X::~X
			true
			Main
			X::X
			X::X
			Outer Try
			Y::Y
			Y::Y
			Middle Try
			Z::Z
			Z::Z
			Inner Try
			Z::~Z
			Z::~Z
			Y::~Y
			Y::~Y
			X::~X
			X::~X
			true""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.io.InputStream;
				import java.net.MalformedURLException;
				import java.net.URL;
				
				public class X {
				    public static void main(String[] args) throws Exception {
				      System.out.println("Done");
				    }
				    public void foo() throws MalformedURLException {
				        URL url = new URL("dummy"); //$NON-NLS-1$
				        try (InputStream is = url.openStream()) {
				        } catch (IOException e) {
				             return;
				        } finally {
				            try {
				                java.nio.file.Files.delete(null);
				            } catch (IOException e1) {
				            }
				        }
				    }
				}
				""",
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.IOException;
				import java.io.InputStream;
				import java.net.MalformedURLException;
				import java.net.URL;
				import java.nio.file.Path;
				import java.nio.file.StandardCopyOption;
				
				public class X {
				    public static void main(String[] args) throws Exception {
				      System.out.println("Done");
				    }
				    public void executeImports() throws MalformedURLException {
				        for (int i = 0; i < 3; i++) {
				            URL url = new URL("dummy"); //$NON-NLS-1$
				            if (url != null) {
				                Path target = new File("dummy").toPath();
				                try (InputStream is = url.openStream()) {
				                    java.nio.file.Files.copy(is, target,
				                            StandardCopyOption.REPLACE_EXISTING);
				                } catch (IOException e) {
				                     break;
				                } finally {
				                    try {
				                        java.nio.file.Files.delete(target);
				                    } catch (IOException e1) {
				
				                    }
				                }
				            }
				        }
				    }
				}
				""",
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.IOException;
				import java.io.InputStream;
				import java.net.MalformedURLException;
				import java.net.URL;
				import java.nio.file.Path;
				import java.nio.file.StandardCopyOption;
				
				public class X {
				    public static void main(String[] args) throws Exception {
				      System.out.println("Done");
				    }
				    public void executeImports() throws MalformedURLException {
				        for (int i = 0; i < 3; i++) {
				            URL url = new URL("dummy"); //$NON-NLS-1$
				            if (url != null) {
				                Path target = new File("dummy").toPath();
				                try (InputStream is = url.openStream()) {
				                    java.nio.file.Files.copy(is, target,
				                            StandardCopyOption.REPLACE_EXISTING);
				                } catch (IOException e) {
				                     continue;
				                } finally {
				                    try {
				                        java.nio.file.Files.delete(target);
				                    } catch (IOException e1) {
				
				                    }
				                }
				            }
				        }
				    }
				}
				""",
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.IOException;
				import java.io.InputStream;
				import java.net.MalformedURLException;
				import java.net.URL;
				import java.nio.file.Path;
				import java.nio.file.StandardCopyOption;
				
				public class X implements AutoCloseable {
					public void foo()  {
				        try (X x = new X()) {
					     System.out.println("Try");
					     throw new Exception();
				        } catch (Exception e) {
					     System.out.println("Catch");
				             return;
				        } finally {
				        	System.out.println("Finally");
				        }
				    }
					public void close() {
						System.out.println("Close");
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}
				"""
		},
		"""
			Try
			Close
			Catch
			Finally""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248d() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.io.IOException;
				import java.io.InputStream;
				import java.net.MalformedURLException;
				import java.net.URL;
				import java.nio.file.Path;
				import java.nio.file.StandardCopyOption;
				
				public class X implements AutoCloseable {
					public void foo()  {
				        try (X x = new X()) {
					     System.out.println("Try");
				        } catch (Exception e) {
					     System.out.println("Catch");
				             return;
				        } finally {
				        	System.out.println("Finally");
				           return;
				        }
				    }
					public void close() {
						System.out.println("Close");
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}
				"""
		},
		"""
			Try
			Close
			Finally""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) throws Exception {
						HasAutoCloseable a;
						try(AutoCloseable b=(a=new HasAutoCloseable()).a) {
						}
						System.out.println(a);
					}
					public static class AutoCloseableA implements AutoCloseable {
						@Override
						public void close() {
							// TODO Auto-generated method stub
						}
					}
					public static class HasAutoCloseable {
						AutoCloseable a = new AutoCloseableA();
						public String toString() {
							return "SUCCESS";
						}
					}
				}"""
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String [] args) throws Exception {
				        HasAutoCloseable aLocal;
				        try(AutoCloseable b=(new HasAutoCloseable()).a){
				        	aLocal = new HasAutoCloseable();
				        }
				        catch (Throwable e) {
				        }
				       System.out.println(aLocal.toString());      \s
				    }\s
				    public static class AutoCloseableA implements AutoCloseable{
				        @Override
				        public void close() {
				        }
				    }
				    public static class HasAutoCloseable{
				        AutoCloseable a=new AutoCloseableA();\s
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				System.out.println(aLocal.toString());      \s
				                   ^^^^^^
			The local variable aLocal may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String [] args) throws Exception {
				        HasAutoCloseable aLocal;
				        try(AutoCloseable b=(aLocal = new HasAutoCloseable()).a){
				        \t
				        }
				        catch (Throwable e) {
				        }
				       System.out.println(aLocal.toString());      \s
				    }\s
				    public static class AutoCloseableA implements AutoCloseable{
				        @Override
				        public void close() {
				        }
				    }
				    public static class HasAutoCloseable{
				        AutoCloseable a=new AutoCloseableA();\s
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				System.out.println(aLocal.toString());      \s
				                   ^^^^^^
			The local variable aLocal may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) throws Exception {
						HasAutoCloseable a;
						try(AutoCloseable b=(a=new HasAutoCloseable()).a) {
				       } finally {
				            System.out.println("Finally");
				        }
						System.out.println(a);
					}
					public static class AutoCloseableA implements AutoCloseable {
						@Override
						public void close() {
							// TODO Auto-generated method stub
						}
					}
					public static class HasAutoCloseable {
						AutoCloseable a = new AutoCloseableA();
						public String toString() {
							return "SUCCESS";
						}
					}
				}"""
		},
		"Finally\n" +
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326d() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String [] args) throws Exception {
				        HasAutoCloseable aLocal;
				        try(AutoCloseable b=(new HasAutoCloseable()).a){
				        	aLocal = new HasAutoCloseable();
				        }
				        catch (Throwable e) {
				        } finally {
				            System.out.println("Finally");
				        }
				       System.out.println(aLocal.toString());      \s
				    }\s
				    public static class AutoCloseableA implements AutoCloseable{
				        @Override
				        public void close() {
				        }
				    }
				    public static class HasAutoCloseable{
				        AutoCloseable a=new AutoCloseableA();\s
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				System.out.println(aLocal.toString());      \s
				                   ^^^^^^
			The local variable aLocal may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326e() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String [] args) throws Exception {
				        HasAutoCloseable aLocal;
				        try(AutoCloseable b=(aLocal = new HasAutoCloseable()).a){
				        \t
				        }
				        catch (Throwable e) {
				        } finally {
				            System.out.println("Finally");
				        }
				       System.out.println(aLocal.toString());      \s
				    }\s
				    public static class AutoCloseableA implements AutoCloseable{
				        @Override
				        public void close() {
				        }
				    }
				    public static class HasAutoCloseable{
				        AutoCloseable a=new AutoCloseableA();\s
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				System.out.println(aLocal.toString());      \s
				                   ^^^^^^
			The local variable aLocal may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326f() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void testWithResourcesAssignment() throws Exception{
				        HasAutoCloseable a;
				        try(AutoCloseable b=(a=new HasAutoCloseable()).a){
				        } finally {
				        	System.out.println(a);
				        }
				    }
				    public class AutoCloseableA implements AutoCloseable{
				        @Override
				        public void close() {
				        }
				    }
				    public class HasAutoCloseable{
				        AutoCloseable a=new AutoCloseableA();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				System.out.println(a);
				                   ^
			The local variable a may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326g() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class CheckedException extends Throwable {}
				public class X {
				    public void testWithResourcesAssignment() throws Exception{
				        HasAutoCloseable a;
				        try(AutoCloseable b=(a=new HasAutoCloseable()).a){
				            throw new CheckedException();
				        } catch (CheckedException e) {
				            System.out.println(a);
				        } finally {
				        	System.out.println(a);
				        }
				    }
				    public class AutoCloseableA implements AutoCloseable{
				        @Override
				        public void close() {
				        }
				    }
				    public class HasAutoCloseable{
				        AutoCloseable a=new AutoCloseableA();
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 1)
				class CheckedException extends Throwable {}
				      ^^^^^^^^^^^^^^^^
			The serializable class CheckedException does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in X.java (at line 8)
				System.out.println(a);
				                   ^
			The local variable a may not have been initialized
			----------
			3. ERROR in X.java (at line 10)
				System.out.println(a);
				                   ^
			The local variable a may not have been initialized
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
public void test380112a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.*;
					interface I extends Closeable, Serializable {}
					public class X {
					    public static void main(String [] args) {
					        try (I i = getX()) {
					        } catch (IOException x) {
					        }
					        System.out.println("Done");
					    }
					    public static I getX() { return null;}
					    public X(){}
					}
					"""
			},
			"Done");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
//variant with finally
public void test380112b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.*;
					interface I extends Closeable, Serializable {}
					public class X {
					    public static void main(String [] args) {
					        try (I i = getX()) {
					        } catch (IOException x) {
					        } finally {
					          System.out.println("Done");
					        }
					    }
					    public static I getX() { return null;}
					    public X(){}
					}
					"""
			},
			"Done");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
//variant with two methods throwing different Exceptions (one subtype of other)
//subtype should be the one to be caught
public void test380112c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.*;
					interface I2 { public void close() throws FileNotFoundException; }
					interface I extends Closeable, I2 {}
					public class X {
					    public static void main(String [] args) {
					        try (I i = getX()) {
					        } catch (FileNotFoundException x) {
					        }
					        System.out.println("Done");
					    }
					    public static I getX() { return null;}
					    public X(){}
					}
					"""
			},
			"Done");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
//test380112c's variant with finally
public void test380112d() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.*;
					interface I2 { public void close() throws FileNotFoundException; }
					interface I extends Closeable, I2 {}
					public class X {
					    public static void main(String [] args) {
					        try (I i = getX()) {
					        } catch (FileNotFoundException x) {
					        } finally {
					          System.out.println("Done");
					        }
					    }
					    public static I getX() { return null;}
					    public X(){}
					}
					"""
			},
			"Done");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
//test380112a variant moving the Interface into a binary
public void test380112e() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test380112.jar";
	String[] defaultLibs = getDefaultClassPaths();
	String[] libs = new String[defaultLibs.length + 1];
	System.arraycopy(defaultLibs, 0, libs, 0, defaultLibs.length);
	libs[defaultLibs.length] = path;
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.*;
					import pkg380112.I;
					public class X {
					    public static void main(String [] args) {
					        try (I i = getX()) {
					        } catch (IOException x) {
					        }
					        System.out.println("Done");
					    }
					    public static I getX() { return null;}
					    public X(){}
					}
					"""
			}, "Done", libs, true, new String[] {"-cp", "."+File.pathSeparator+path});
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=394780
public void test394780() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<R extends Resource> {
					    public static void main(String[] args) {
					        X<Resource> m = new X<>();
					        m.tryWithResource(new ResourceImpl());
					    }
					    public void tryWithResource(R resource) {
					        try (R r = resource) {
					            r.compute();
					        }
					    }
					}""",
				"Resource.java",
				"""
					public interface Resource extends AutoCloseable {
					    void compute();
					    @Override
					    public void close();
					}""",
				"ResourceImpl.java",
				"""
					public class ResourceImpl implements Resource {
					    @Override
					    public void close() {
					        System.out.print("close");
					    }
					    @Override
					    public void compute() {
					        System.out.print("compute");
					    }
					}"""
			},
			"computeclose");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=533187
public void testBug533187() {
	this.runConformTest(
			true,
			new String[] {
				"Stuck.java",
				"""
					public class Stuck {
					    public static void main(String[] args) {
					        System.out.println(snippet1());
					    }
					    public static String snippet1() {
					        try {
					            synchronized (String.class) {
					                try (AutoCloseable scope = null) {\s
					                    return "RETURN";
					                } catch (Throwable t) {
					                    return t.toString();
					                }
					            }
					        } finally {
					            raise();
					        }
					    }
					    public static void raise() {
					        throw new RuntimeException();
					    }
					}"""
			},
			null,
			null,
			null,
			null,
			"""
				java.lang.RuntimeException
					at Stuck.raise(Stuck.java:19)
					at Stuck.snippet1(Stuck.java:15)
					at Stuck.main(Stuck.java:3)
				""",
			null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=467230
public void testBug467230() {
	this.runConformTest(
			true,
			new String[] {
				"Test.java",
				"""
					public class Test {
						static class C implements AutoCloseable {
							@Override
							public void close() {
								System.out.println("close");
							}
						}
						public static void main(String[] args) {
							try (C c = new C()) {
								return;
							} catch (Exception e) {
								System.out.println("catch");
							} finally {
								f();
							}
						}
						private static void f() {
							System.out.println("finally");
							throw new RuntimeException();
						}
					}"""
			},
			null,
			null,
			null,
			"close\n" +
			"finally",
			"""
				java.lang.RuntimeException
					at Test.f(Test.java:19)
					at Test.main(Test.java:14)
				""",
			null);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/934
public void _testGHIssue934() {
	this.runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					public class X {
						static class Y implements AutoCloseable {
							RuntimeException e;
					
							public Y(RuntimeException e) {
								this.e = e;
							}
					
							@Override
							public void close() {
								throw e;
							}
						}
					    public static void main(String[] args) {
					        RuntimeException e = new RuntimeException("My Exception");
					        try {
					            try (Y A = new Y(e)) {
					                throw e;
					            }
					        } catch (IllegalArgumentException iae) {
					            if (iae.getCause() == e)\s
					                System.out.println("OK!");
					        }
					    }
					}
					"""

			},
			null,
			null,
			null,
			"OK!",
			"",
			null);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1063
// Regression in code generation for try with resources with the fix for Issue # 934
public void testGHIssue1063() {
	this.runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					import java.io.Closeable;
					import java.io.IOException;
					
					public class X {
						public static void main(String[] args) throws IOException {
							try (DummyClosable closable = new DummyClosable()) {
								throw new IOException("OMG!!!");
							} catch (IOException e) {
								throw e;
							}
						}
					
						static class DummyClosable implements Closeable {
							@Override
							public void close() throws IOException {
								System.out.println("Closed!");
							}
						}
					}
					"""
			},
			null,
			null,
			null,
			"Closed!",
			"java.io.IOException: OMG!!!\n" +
			"	at X.main(X.java:7)\n",
			null);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/pull/1495
public void testGHissue1495() {
    this.runConformTest(
        new String[] {
                "X.java",
                """
					import java.io.*;
					interface I extends Closeable {}
					public class X {
					   public static void main(String[] args) {
					       try (I i = i()) {
					         return;
					       } finally {
					         return;
					       }\
					   }\
					   public static I i() { return null; }
					}
					"""
    });
}
public static Class testClass() {
	return TryWithResourcesStatementTest.class;
}
}
