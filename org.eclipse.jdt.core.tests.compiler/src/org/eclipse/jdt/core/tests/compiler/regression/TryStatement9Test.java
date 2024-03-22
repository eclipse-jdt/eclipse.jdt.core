/*******************************************************************************
 * Copyright (c) 2016, 2022 IBM corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
@SuppressWarnings({ "rawtypes" })
public class TryStatement9Test extends AbstractRegressionTest {

static {
///	TESTS_NAMES = new String[] { "testBug488569_019" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryStatement9Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_9);
}

public void testBug488569_001() { // vanilla test case
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Closeable;
					import java.io.IOException;
					
					class Y implements Closeable {
					        @Override
					        public void close() throws IOException {
					                // nothing
					        }
					}
					public class X {
					
					        public void foo() throws IOException {
					             final Y y1 = new Y();
					             try (y1) {\s
					            	 //
					             }
					        }\s
					        public static void main(String[] args) {
								System.out.println("Done");
							}
					}\s
					"""
			},
			"Done");
}

// vanilla with a delimiter
public void testBug488569_002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Closeable;
					import java.io.IOException;
					
					class Y implements Closeable {
					        @Override
					        public void close() throws IOException {
					                // nothing
					        }
					}
					public class X {
					
					        public void foo() throws IOException {
					             final Y y1 = new Y();
					             try (y1;) {\s
					            	 //
					             }
					        }\s
					        public static void main(String[] args) {
								System.out.println("Done");
							}
					}\s
					"""
			},
			"Done");
}

public void testBug488569_003() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Closeable;
					import java.io.IOException;
					
					class Y implements Closeable {
					        @Override
					        public void close() throws IOException {
					                // nothing
					        }
					}
					public class X {
					
					        public void foo() throws IOException {
					             final Y y1 = new Y();
					             final Y y2 = new Y();
					             try (y1; y2) {\s
					            	 //
					             }
					        }\s
					        public static void main(String[] args) {
								System.out.println("Done");
							}
					}\s
					"""
			},
			"Done");
}
public void testBug488569_004() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Closeable;
					import java.io.IOException;
					
					class Y implements Closeable {
					        @Override
					        public void close() throws IOException {
					                // nothing
					        }
					}
					public class X {
					
					        public void foo() throws IOException {
					             final Y y1 = new Y();
					             try (y1; final Y y2 = new Y()) {\s
					            	 //
					             }
					        }\s
					        public static void main(String[] args) {
								System.out.println("Done");
							}
					}\s
					"""
			},
		"Done");
}

public void testBug488569_005() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Closeable;
					import java.io.IOException;
					
					class Y implements Closeable {
					        @Override
					        public void close() throws IOException {
					                // nothing
					        }
					}
					public class X {
					
					        public void foo() throws IOException {
					             final Y y1 = new Y();
					             try (final Y y = new Y(); y1; final Y y2 = new Y()) {\s
					            	 //
					             }
					        }\s
					        public static void main(String[] args) {
								System.out.println("Done");
							}
					}\s
					"""
			},
		"Done");
}
public void testBug488569_006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				public class X {\s
				    public void foo() throws IOException {
				         Y y1 = new Y();
				         try(y1) {\s
				             return;
				         }
				    }\s
				} \s
				
				class Y implements Closeable {
						final int x = 10;
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}""",
		},
		"");
}

// check for the error for non-effectively final variable.
public void testBug488569_007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				class Y implements Closeable {
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}
				public class X {
				
				        public void foo() throws IOException {
				             Y y1 = new Y();
				             y1 = new Y();
				             try (y1) {\s
				            	 //
				             }
				        }\s
				        public static void main(String[] args) {
							System.out.println("Done");
						}
				}""",
		},
		"""
			----------
			1. WARNING in X.java (at line 14)
				y1 = new Y();
				^^^^^^^^^^^^
			Resource leak: \'y1\' is not closed at this location
			----------
			2. ERROR in X.java (at line 15)
				try (y1) {\s
				     ^^
			Local variable y1 defined in an enclosing scope must be final or effectively final
			----------
			""");
}
//check for the error for combination of NameRef and LocalVarDecl.
public void testBug488569_008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				class Y implements Closeable {
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}
				public class X {
				
				        public void foo() throws IOException {
				             try (y1; Y y1 = new Y()) {\s
				            	 //
				             }
				        }\s
				        public static void main(String[] args) {
							System.out.println("Done");
						}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 13)
				try (y1; Y y1 = new Y()) {\s
				     ^^
			y1 cannot be resolved
			----------
			""");
}

//check for the warning for combination of LocalVarDecl and NameRef.
public void testBug488569_009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				class Y implements Closeable {
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}
				public class X {
				
				        public void foo() throws IOException {
				             try (Y y1 = new Y(); y1) {\s
				            	 //
				             }
				        }\s
				        public static void main(String[] args) {
							System.out.println("Done");
						}
				}""",
		},
		"""
			----------
			1. WARNING in X.java (at line 13)
				try (Y y1 = new Y(); y1) {\s
				                     ^^
			Duplicate resource reference y1
			----------
			""");
}
//check for the warning for combination of NameRef and NameRef.
public void testBug488569_010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				class Y implements Closeable {
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}
				public class X {
				
				        public void foo() throws IOException {
				             Y y1 = new Y();
				             try (y1; y1) {\s
				            	 //
				             }
				        }\s
				        public static void main(String[] args) {
							System.out.println("Done");
						}
				}""",
		},
		"""
			----------
			1. WARNING in X.java (at line 14)
				try (y1; y1) {\s
				         ^^
			Duplicate resource reference y1
			----------
			""");
}
public void testBug488569_011() {
	this.runConformTest(
			new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				class Y implements Closeable {
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}
				public class X {
				
				        public void foo() throws IOException {
				             try (Y y1 = new Y();y1) {\s
				            	 //
				             }
				        }\s
				        public static void main(String[] args) {
							System.out.println("Done");
						}
				}\s
				"""
			},
			"Done");
}

public void testBug488569_012() {
	this.runConformTest(
			new String[] {
			"X.java",
			"""
				import java.io.Closeable;
				import java.io.IOException;
				
				class Y implements Closeable {
				        @Override
				        public void close() throws IOException {
				                // nothing
				        }
				}
				public class X {
				
				        public void foo() throws IOException {
				             Y y = new Y();
				             try (Y y1 = new Y();y;y1) {\s
				            	 //
				             }
				        }\s
				        public static void main(String[] args) {
							System.out.println("Done");
						}
				}\s
				"""
			},
			"Done");
}

// Confirm the behavior as described in https://bugs.eclipse.org/bugs/show_bug.cgi?id=338402#c16 even with the
// presence of a duplicate variable in-line with javac9.
public void testBug488569_013() {
	this.runConformTest(
			new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String [] args) throws Exception {
				    	Z z1 = new Z();
				        try (Y y = new Y();z1;y) {
				        }
				    } \s
				}
				class Y implements AutoCloseable {
					public void close() throws Exception {
						System.out.println("Y CLOSE");
					}
				}
				
				class Z implements AutoCloseable {
					public void close() throws Exception {
						System.out.println("Z CLOSE");
					}
				}
				"""
			},
			"""
				Y CLOSE
				Z CLOSE
				Y CLOSE"""
			);
}

// check for unhandled-exception error
public void testBug488569_014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
					public static void main(String[] args) {
						Y y1 = new Y();		 		\s
						try (y1)  {
							System.out.println("In Try");
						} finally {
						}
					}
				}
				  \s
				class Y implements AutoCloseable {
					public void close() throws IOException {
						System.out.println("Closed");
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				try (y1)  {
				     ^^
			Unhandled exception type IOException thrown by automatic close() invocation on y1
			----------
			""");
}

// field to be legal
public void testBug488569_015(){
	this.runConformTest(
			new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
					final Y y = new Y();
					public static void main(String[] args) {
						new X().foo();
					}
					public void foo() {
						try (y)  {
							System.out.println("In Try");
						} catch (IOException e) {
							e.printStackTrace();
						}
						finally { \s
						} \s
						//y1 = new Y();	\s
					}\s
				}\s
				  \s
				class Y implements AutoCloseable {
					public void close() throws IOException {
						System.out.println("Closed");
					}
				}
				"""
			},
			"In Try\n" +
			"Closed"
			);
}
//field to be legal - but null field not to be called for close
public void testBug488569_016(){
	this.runConformTest(
			new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
					final Y y = null;
					public static void main(String[] args) {
						new X().foo();
					}
					public void foo() {
						try (y)  {
							System.out.println("In Try");
						} catch (IOException e) {
							e.printStackTrace();
						}
						finally { \s
						} \s
					}\s
				}\s
				  \s
				class Y implements AutoCloseable {
					public void close() throws IOException {
						System.out.println("Closed");
					}
				}
				"""
			},
			"In Try"
			);
}

// field in various avatars
public void testBug488569_017(){
	this.runConformTest(
			new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				class Z {
					final Y yz = new Y();
				}
				public class X extends Z {
					final Y y2 = new Y();
				\t
					public void foo() {
						try (super.yz; y2)  {
							System.out.println("In Try");
						} catch (IOException e) {
						\t
						}finally {\s
						}
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
						System.out.println("Closed");
					}\s
				} \s
				"""
			},
			"""
				In Try
				Closed
				Closed"""
			);
}

// negative tests: non-final fields
public void testBug488569_018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				class Z {
					 Y yz = new Y();
				}
				public class X extends Z {
					 Y y2 = new Y();
				\t
					public void foo() {
						try (this.y2; super.yz;y2)  { \s
							System.out.println("In Try");
						} catch (IOException e) {			 \s
						}
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
						System.out.println("Closed");
					}\s
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				try (this.y2; super.yz;y2)  { \s
				          ^^
			Field y2 must be final
			----------
			2. ERROR in X.java (at line 10)
				try (this.y2; super.yz;y2)  { \s
				                    ^^
			Field yz must be final
			----------
			3. ERROR in X.java (at line 10)
				try (this.y2; super.yz;y2)  { \s
				                       ^^
			Local variable y2 defined in an enclosing scope must be final or effectively final
			----------
			""");
}
//negative tests: duplicate fields
public void testBug488569_019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				class Z {
					 final Y yz = new Y();
				}
				public class X extends Z {
					final  Y y2 = new Y();
				\t
					 Y bar() {
						 return new Y();
					 }
					public void foo() {
						Y y3 = new Y();
						try (y3; y3;super.yz;super.yz;this.y2;)  { \s
							System.out.println("In Try");
						} catch (IOException e) {			 \s
						}\s
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}
				class Y implements AutoCloseable {
					@Override
					public void close() throws IOException {
						System.out.println("Closed");
					} \s
				} \s
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 14)
				try (y3; y3;super.yz;super.yz;this.y2;)  { \s
				         ^^
			Duplicate resource reference y3
			----------
			2. WARNING in X.java (at line 14)
				try (y3; y3;super.yz;super.yz;this.y2;)  { \s
				                     ^^^^^^^^
			Duplicate resource reference super.yz
			----------
			""");
}

public void testBug488569_020() { // vanilla test case
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.IOException;
					
					public class X {
					     final Z y2 = new Z();
					     public static void main(String[] args) throws Exception {
					          X t = new X();
					          try (t.y2) {    \s
					          }         \s
					     } \s
					}
					
					class Z implements AutoCloseable {
					     @Override
					     public void close() throws IOException {
					          System.out.println("Done");
					     }
					}\s
					"""
			},
			"Done");
}

//negative tests: duplicate fields
public void testBug488569_021() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				public class X {
				     final Z z = new Z();
				     public X() {
				          try(this.z) {
				              \s
				          }
				     }
				}
				
				class Z implements AutoCloseable {
				     @Override
				     public void close() throws IOException {
				          System.out.println("Closed");
				     }\s
				} \s
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				try(this.z) {
				    ^^^^^^
			Unhandled exception type IOException thrown by automatic close() invocation on z
			----------
			""");
}
public void testBug577128_1() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X implements AutoCloseable {
					private void release() {
						X cl = new X();
						try (this;cl) {}\s
				        	catch (IOException e) {
				        }
					}
					public static void main(String[] args) {
						X cl = new X();
						cl.release();
					}
					@Override
					public void close() throws IOException {
						System.out.println("close() call");
					}
				}
				""",
		},
		"close() call\n" +
		"close() call");
}

public static Class testClass() {
	return TryStatement9Test.class;
}
}
