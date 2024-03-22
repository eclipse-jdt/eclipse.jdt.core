/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
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
public class LambdaShapeTests extends AbstractRegressionTest {
static {
//		TESTS_NAMES = new String[] { "test016"};
//		TESTS_NUMBERS = new int[] { 50 };
//		TESTS_RANGE = new int[] { 11, -1 };
}
public LambdaShapeTests(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface VoidI {
					void foo(String s);
				}
				class Test {
					public String gooVoid(VoidI i){return "";}
				}
				public class X {
					public static void main(String[] args) {
						Test test = new Test();
						test.gooVoid((x) -> {
							if (false) {
								x += "a";
							}
						});
						test.gooVoid((x) -> {
							if (true);
						});
						test.gooVoid((x) -> {
							if (true) {
								x += "a";
							}
						});
						test.gooVoid((x) -> {
							final boolean val = true;
							if (val) {
								x += "a";
							}
						});
						test.gooVoid((x) -> {
							final boolean val = true;
							if (val);
						});
						test.gooVoid((x) -> {
							final boolean val = false;
							if (val) {
								x += "a";
							}
						});
						test.gooVoid((x) -> {
							if (x != null) {
								x += "a";
							}
						});
						test.gooVoid((x) -> {
							final boolean val = true;
							if (x != null);
						});
						test.gooVoid((x) -> {
							if (false) {
								x += "a";
							} else {
								x += "b";
							}
						});
						test.gooVoid((x) -> {
							if (false) {
								x += "a";
							} else;
						});
						test.gooVoid((x) -> {
							final boolean val = false;
							if (val) {
								x += "a";
							} else {
								x += "b";
							}
						});
						test.gooVoid((x) -> {
							final boolean val = false;
							if (val) {
								x += "a";
							} else;
						});
						test.gooVoid((x) -> {
							if (x != null) {
								x += "a";
							} else {
								x += "b";
							}
						});
						test.gooVoid((x) -> {
							if (x != null) {
								x += "a";
							} else;
						});
					}
				}
				""",
		});
}
public void test002() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x);\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						final boolean FALSE = false;
						goo((x) -> {
							if(true) return "";
							else return null;
						});
						goo((x) -> {
							if(false) return "";
							else return null;
						});
						goo((x) -> {
							if(x > 0) return "";
							else return null;
						});
						goo((x) -> {
							if(FALSE) return "";
							else return null;
						});
						goo((x) -> {
							if(!FALSE) return "";
							else return null;
						});
						goo((x) -> {
							if(!FALSE) return "";
							else return null;
						});
					}
				}
				"""
		});
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface VoidI {
					void foo(String s);
				}
				class Test {
					public String gooVoid(VoidI i){return "";}
				}
				public class X {
					public static void main(String[] args) {
						Test test = new Test();
						test.gooVoid((x) -> {
							if (true) {
								return 0;
							}
						});
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				test.gooVoid((x) -> {
				     ^^^^^^^
			The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})
			----------
			2. ERROR in X.java (at line 12)
				return 0;
				^^^^^^^^^
			Void methods cannot return a value
			----------
			""");
}
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface VoidI {
					void foo(String s);
				}
				class Test {
					public String gooVoid(VoidI i){return "";}
				}
				public class X {
					public static void main(String[] args) {
						Test test = new Test();
						test.gooVoid((x) -> {
							final boolean val = true;
							if (val) {
								return x;
							}
						});
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				test.gooVoid((x) -> {
				     ^^^^^^^
			The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})
			----------
			2. ERROR in X.java (at line 13)
				return x;
				^^^^^^^^^
			Void methods cannot return a value
			----------
			""");
}
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface VoidI {
					void foo(String s);
				}
				class Test {
					public String gooVoid(VoidI i){return "";}
				}
				public class X {
					public static void main(String[] args) {
						Test test = new Test();
						test.gooVoid((x) -> {
							if (x != null) {
								return 0;
							}
						});
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				test.gooVoid((x) -> {
				     ^^^^^^^
			The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})
			----------
			2. ERROR in X.java (at line 12)
				return 0;
				^^^^^^^^^
			Void methods cannot return a value
			----------
			""");
}
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface VoidI {
					void foo(String s);
				}
				class Test {
					public String gooVoid(VoidI i){return "";}
				}
				public class X {
					public static void main(String[] args) {
						Test test = new Test();
						test.gooVoid((x) -> {
							if (false) {
								x += "a";
							} else {
								return 0;
							}
						});
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				test.gooVoid((x) -> {
				     ^^^^^^^
			The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})
			----------
			2. ERROR in X.java (at line 14)
				return 0;
				^^^^^^^^^
			Void methods cannot return a value
			----------
			""");
}
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface VoidI {
					void foo(String s);
				}
				class Test {
					public String gooVoid(VoidI i){return "";}
				}
				public class X {
					public static void main(String[] args) {
						Test test = new Test();
						test.gooVoid((x) -> {
							final boolean val = false;
							if (val) {
								x += "a";
							} else {
								return 0;
							}
						});
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				test.gooVoid((x) -> {
				     ^^^^^^^
			The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})
			----------
			2. ERROR in X.java (at line 15)
				return 0;
				^^^^^^^^^
			Void methods cannot return a value
			----------
			""");
}
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface VoidI {
					void foo(String s);
				}
				class Test {
					public String gooVoid(VoidI i){return "";}
				}
				public class X {
					public static void main(String[] args) {
						Test test = new Test();
						test.gooVoid((x) -> {
							if (x != null) {
								x += "a";
							} else {
								return 0;
							}
						});
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				test.gooVoid((x) -> {
				     ^^^^^^^
			The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})
			----------
			2. ERROR in X.java (at line 14)
				return 0;
				^^^^^^^^^
			Void methods cannot return a value
			----------
			""");
}
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x);\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						final boolean FALSE = false;
						goo((x) -> {
							if(FALSE) return "";
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x);\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							if(true);
							else return "";
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 9)
				else return "";
				     ^^^^^^^^^^
			Dead code
			----------
			""");
}
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x);\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							if(false) return null;
							else;
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 8)
				if(false) return null;
				          ^^^^^^^^^^^^
			Dead code
			----------
			3. WARNING in X.java (at line 9)
				else;
				    ^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""");
}
public void test012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x);\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							if(x > 0) return "";
							else;
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 9)
				else;
				    ^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""");
}
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x);\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							if(x > 0);
							else return "";
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x);\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							if(x < 0) return null;
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x);\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						final boolean FALSE = false;
						goo((x) -> {
							if(!FALSE) return "";
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						final boolean FALSE = false;
						goo((x) -> {while (FALSE) throw new Exception();});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				goo((x) -> {while (FALSE) throw new Exception();});
				    ^^^^^^
			This lambda expression must return a result of type String
			----------
			""");
}
public void test017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {while (false) return "";});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {while (false) return "";});
				    ^^^^^^
			This method must return a result of type String
			----------
			2. ERROR in X.java (at line 7)
				goo((x) -> {while (false) return "";});
				                          ^^^^^^^^^^
			Unreachable code
			----------
			""");
}
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {while (x > 0) {
							if(x > 0) {return "";} else {break;}
							}});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {while (x > 0) {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 8)
				if(x > 0) {return "";} else {break;}
				                            ^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""");
}
public void test019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {while (x > 0) {
							if(x > 0) {return "";}
						}});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {while (x > 0) {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						final boolean TRUE = true;
						goo((x) -> {while (TRUE) {
							if(x > 0) {System.out.println();}
							}});
						goo((x) -> {while (true) {
							if(x > 0) {System.out.println();}
							}});
					}
				}
				"""
		});
}
public void test021() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							int i = 100;
							outer: while(x > 0) {
								inner: while(i > 0) {
								if(--i > 50) {
									return "";
								}
								if(i > 90) {
									break outer;
								}
								return "";
								}
							}});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 10)
				inner: while(i > 0) {
				^^^^^
			The label inner is never explicitly referenced
			----------
			""");
}
public void test022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					void foo(String s) throws Exception;
				}
				public class X {
					void zoo(I i) {}
					void test() {
						final boolean FALSE = false;
						final boolean TRUE = true;
						zoo((x) -> {while (TRUE) throw new Exception();});
						zoo((x) -> {while (!FALSE) return ;});
						zoo((x) -> {while (x.length() > 0) {
							if(x.length() > 0) {return ;} else {break;}
							}});
						zoo((x) -> {while (x.length() > 0) {
							if(x.length() > 0) {return ;}
							}});
						zoo((x) -> {while (true) {
							if(x.length() > 0) {System.out.println();}
							}});
						zoo((x) -> {while (TRUE) {
							if(x.length() > 0) {System.out.println();}
							}});
						zoo((x) -> {
							int i = 100;
							outer: while(x.length() > 0) {
								inner: while(i > 0) {
								if(--i > 50) {
									break inner ;
								}
								if(i > 90) {
									break outer;
								}
								return ;
								}
							}});
					}
				}
				"""
		});
}
public void test023() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						final boolean FALSE = false;
						final boolean TRUE = true;
						goo((x) -> {do {throw new Exception();}while (FALSE);});
						goo((x) -> {do { return "";}while (false);});
						goo((x) -> {do {
							if(x > 0) {System.out.println();}
							}while (true);});
						goo((x) -> {do {
							if(x > 0) {System.out.println();}
							}while (TRUE);});
					}
				}
				"""
		});
}
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {do {
							if(x > 0) {return "";} else {break;}
							}while (x > 0);});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {do {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 8)
				if(x > 0) {return "";} else {break;}
				                            ^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""");
}
public void test025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {do {
							if(x > 0) {return "";}
							}while (x > 0);});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {do {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(int x) throws Exception;
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							int i = 100;
							outer: do {
								inner: do {
								if(--i > 50) {
									return "";
								}
								if(i > 90) {
									break outer;
								}
								return "";
								}while(i > 0);
							}while(x > 0);});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 10)
				inner: do {
				^^^^^
			The label inner is never explicitly referenced
			----------
			""");
}
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					void foo(String s) throws Exception;
				}
				public class X {
					void zoo(I i) {}
					void test() {
						zoo((x) -> {do {
							if(x.length() > 0) {System.out.println();}
							}while (true);});
						zoo((x) -> {do {throw new Exception();}while (false);});
						zoo((x) -> {do { return ;}while (false);});
						zoo((x) -> {do { continue ;}while (true);});
						zoo((x) -> {do {
							if(x.length() > 0) {return ;} else {break;}
							}while (x.length() > 0);
						});
						zoo((x) -> {do {
							if(x.length() > 0) {return ;}
							}while (x.length() > 0);
						});
						zoo((x) -> {
						int i = 100;
						outer: do {
							inner: do {
							if(--i > 50) {
								break inner ;
							}
							if(i > 90) {
								break outer;
							}
							return ;
							}while(i > 0);
						}while(x.length() > 0);});
					}
				}
				"""
		});
}
public void test028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						final boolean FALSE = false;\s
						final boolean TRUE = true;\s
						goo((x) -> {
							for(;TRUE;){
							}});
						goo((x) -> {
							for(int i = 0;i < 100; i+= 10){
								switch(i) {
								case 90: {
									System.out.println();
									break;
								}
								case 80: {
									if(x > 10) return null;
									break;
								}
								default:
									return "";
								}
							}
							return "";
						});
					\t
						goo((x) -> {
							for(;TRUE;){
								if(x < 100) return "";
								else return null;
						}});
						goo((x) -> {
							for(;x > 0;){
								if(x < 100) return "";
								else return null;
							}
							return null;
						});
					}
				}
				"""
		});
}
public void test029() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						final boolean FALSE = false;\s
						goo((x) -> {
							for(;FALSE;){
							}});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				goo((x) -> {
				    ^^^^^^
			This lambda expression must return a result of type String
			----------
			""");
}
public void test030() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							for(;x > 0;){
								if(x < 100) return "";
								else return null;
						}});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 10)
				else return null;
				     ^^^^^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""");
}
public void test031() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							for(int i = 0;i < 100; i+= 10){
								switch(i) {
								case 90: {
									System.out.println();
									break;
								}
								case 80: {
									if(x > 10) return null;
									break;
								}
								default:
									return "";
								}
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test() {
						goo((x) -> {
							outer: for(int i = 0;i < 100; i+= 10){
								inner : for(int j = x; j > 0; j--) {
									switch(i) {
									case 90: {
										System.out.println();
										break inner;
									}
									case 80: {
										if(x > 10) return null;
										break outer;
									}
									default:
										return "";
									}
								}
							\t
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 9)
				inner : for(int j = x; j > 0; j--) {
				                              ^^^
			Dead code
			----------
			""");
}
public void test033() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							for(String str : strs){
								if(str.length() > 0) {
									return "yes";
								} else {
									return "no";
								}
							}
							return null;
						});
						goo((x) -> {
							for(String str : strs){
								return "no";
							}
							return "";
						});
					\t
						goo((x) -> {
							for(String str : strs){
								if(str.length() > 0) break;
								System.out.println();
							}
							return "";
						});
					}
				}
				"""
		});
}
public void test034() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							for(String str : strs){
								if(str.length() > 0) {
									return "yes";
								} else {
									return "no";
								}
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 11)
				} else {
								return "no";
							}
				       ^^^^^^^^^^^^^^^^^^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""");
}
public void test035() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							for(String str : strs){
								return "no";
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							for(String str : strs){
								switch(str.length()) {
								case 9: {
									System.out.println();
									return "nine";
								}
								case 1: {
									if(x > 10) return null;
									return "one";
								}
								default:
									return "";
								}
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							outer: for(String str : strs){
								inner : for(int j = x; j > 0; j--) {
									switch(str.length()) {
									case 9: {
										System.out.println();
										break inner;
									}
									case 8: {
										if(x > 10) return null;
										break outer;
									}
									default:
										return "";
									}
								}
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 9)
				inner : for(int j = x; j > 0; j--) {
				                              ^^^
			Dead code
			----------
			""");
}
public void test038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							switch(x) {
							case 0 : if(x > 10) return ">10";
							case 1: return "1";
							default: return "-1";
							}
						});
						goo((x) -> {
							String str = "";
							switch(x) {
							case 0 : if(x > 10) break; else {str = "0"; break;}
							case 1: str = "1";break;
							default: break;
							}
							return str;
						});
						goo((x) -> {
							String str = "";
							switch(x){}
							return str;
						});
					}
				}
				"""
		});
}
public void test039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							switch(x) {
							case 0 : if(x > 10) return ">10";
							case 1: return "1";
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							String str = "";
							switch(x) {
							case 0 : if(x > 10) break; else {str = "0"; break;}
							case 1: str = "1";break;
							default: break;
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {})
			----------
			""");
}
public void test041() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							try {
								return "";
							} finally {
							\t
							}
						});
						goo((x) -> {
								try {
									throw new Exception();
								} finally {
								}
						});
						goo((x) -> {
								try {
									if(x > 0)\s
										throw new RuntimeException();
								} catch (NullPointerException e) {return null;}\s
								catch(ClassCastException c) {
								}
								finally {
									return "";
								}
						});
					\t
					}
				}
				"""
		});
}
public void test042() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							try {
								if(x > 0) {
									return "";
								}
							} finally {}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							try {
								return "";
							}catch (Exception e) {}
							finally {
							\t
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							try {
								//if(x > 0)\s
									throw new RuntimeException();
							} catch (NullPointerException e) {return null;}\s
							catch(ClassCastException c) {
							}
						});
						goo((x) -> {
							try {
								if(x > 0)\s
									throw new RuntimeException();
							} catch (NullPointerException e) {return null;}\s
							catch(ClassCastException c) {
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. ERROR in X.java (at line 15)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							try {
								if(x > 0)\s
									throw new RuntimeException();
							} catch (NullPointerException e) {return null;}\s
							catch(ClassCastException c) {
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			""");
}
public void test046() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {\s
					String foo(int x) throws Exception;\s
				}
				public class X {
					void goo(I i) {}
					void test(String[] strs) {
						goo((x) -> {
							if (true) {
								try {
									if(x > 0)
										throw new Exception();
								} finally {
									return "";
								}
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo((x) -> {
				    ^^^^^^
			This method must return a result of type String
			----------
			2. WARNING in X.java (at line 12)
				} finally {
								return "";
							}
				          ^^^^^^^^^^^^^^^^^^^^^^^
			finally block does not complete normally
			----------
			""");
}
public void testSwitch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							switch (args.length) {
							case 0:
								System.out.println(0);
								throw new RuntimeException();
							case 1:
								System.out.println(1);
								throw new RuntimeException();
							case 2:
								System.out.println(2);
								throw new RuntimeException();
							default:\s
								System.out.println("default");
								throw new RuntimeException();
							}
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testSwitch2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							switch (args.length) {
							case 0:
								System.out.println(0);
								break;
							case 1:
								System.out.println(1);
								throw new RuntimeException();
							case 2:
								System.out.println(2);
								throw new RuntimeException();
							default:\s
								System.out.println("default");
								throw new RuntimeException();
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testSwitch3() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							switch (args.length) {
							case 0:
								System.out.println(0);
								throw new RuntimeException();
							case 1:
								System.out.println(1);
								throw new RuntimeException();
							case 2:
								System.out.println(2);
								throw new RuntimeException();
							default:\s
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testSwitch4() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							switch (args.length) {
							case 0:
								System.out.println(0);
								throw new RuntimeException();
							case 1:
								System.out.println(1);
								throw new RuntimeException();
							case 2:
								System.out.println(2);
								throw new RuntimeException();
							default:\s
							    break;
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testSwitch5() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				public class X {
					static void goo(I i) {}
					public static void main(String[] args) {
						goo(() -> {
							switch (args.length){
							case 1:
								if (args == null)
									break;
								else
									break;
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testSwitch6() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				public class X {
					static void goo(I i) {}
					public static void main(String[] args) {
						goo(() -> {
							switch (args.length){
							case 1:
								if (args == null)
									break;
				           throw new RuntimeException();
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testWhileThis() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							boolean t = true;
							while (t) {
								System.out.println();
								throw new RuntimeException();
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testWhile2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							while (t) {
								System.out.println();
								throw new RuntimeException();
							}
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testWhile3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							while (t && !!t) {
								System.out.println();
								throw new RuntimeException();
							}
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testWhile4() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							while (t && !!!t) {
								System.out.println();
								throw new RuntimeException();
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							boolean t = true;
							do {
								System.out.println();
								throw new RuntimeException();
							} while (t);
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testDo2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							do {
								System.out.println();
								throw new RuntimeException();
							} while (t);
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testDo3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							do {\s
								System.out.println();
								throw new RuntimeException();
							} while (t && !!t);
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testDo4() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							do {
								System.out.println();
							} while (t && !!!t);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo5() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							do {
								break;
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo6() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							do {
								if (args == null) break;
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo7() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							do {
								if (args == null) throw new RuntimeException();
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo8() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							do {
								throw new RuntimeException();
							} while (false);
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testDo9() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
								continue;
							} while (false);
						});
					}
				}
				"""
		},
		"J");
}
public void testDo10() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
				               if (true)\s
								    continue;
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo11() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
				               if (true)\s
								    continue;
				               else\s
				                   continue;
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo12() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
				               if (true)\s
								    continue;
				               else\s
				                   throw new RuntimeException();
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo13() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
				               if (true)\s
				                   throw new RuntimeException();
				               else\s
				                   throw new RuntimeException();
							} while (false);
						});
					}
				}
				"""
		},
		"I");
}
public void testDo14() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
				               if (true) {\s
				                   System.out.println();
								    continue;
				               }
				               else {
				                   continue;
				               }
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo15() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					public static void main(String[] args) {
						goo(() -> {
							label:
							do {
								continue label;
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo16() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
								blah:
								continue;
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo17() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
								synchronized(args) {
								    continue;
				               }
							} while (false);
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testDo18() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
								try {
									continue;
								} finally {
									throw new RuntimeException();
								}
							} while (false);
						});
					}
				}
				"""
		},
		"I");
}
public void testDo19() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
								try {
									continue;
								} finally {
								}
							} while (false);\t
						});
					}
				}
				"""
		},
		"J");
}
public void testDo20() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
								switch (args.length){
								default:
									continue;
								}
							} while (false);\t
						});
					}
				}
				"""
		},
		"J");
}
public void testDo21() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
								while (true) {
									continue;
								}
							} while (false);\t
						});
					}
				}
				"""
		},
		"I");
}
public void testDo22() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							label:
							do {
								while (true) {
									continue label;
								}
							} while (false);\t
						});
					}
				}
				"""
		},
		"J");
}
public void testDo23() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							label:
							while (true) {
								while (true) {
									continue label;
								}
							}\t
						});
					}
				}
				"""
		},
		"I");
}
public void testDo24() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							label:
							do {
								for (;;) {
									continue label;
								}
							} while (false);\t
						});
					}
				}
				"""
		},
		"J");
}
public void testDo25() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							label:
							do {
								do {
									continue label;
								} while (true);
							} while (false);\t
						});
					}
				}
				"""
		},
		"J");
}
public void testDo26() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							do {
								label:
									while (true) {
										continue label;
									}
							} while (false);
						});
					}
				}
				"""
		},
		"I");
}
public void testForeach() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							for (String s: args) {
								System.out.println();
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testForeach2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							for (String s: args) {
								System.out.println();
							do {
								System.out.println();
								switch (args.length) {
								case 0:
									System.out.println(0);
									break;
								case 1:
									System.out.println(1);
									throw new RuntimeException();
								case 2:
									System.out.println(2);
									throw new RuntimeException();
								default:\s
									System.out.println("default");
									throw new RuntimeException();
								}
							} while (t);
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testForeach3() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							for (String s: args) {
								System.out.println();
							do {
								System.out.println();
								switch (args.length) {
								case 0:
									System.out.println(0);
									throw new RuntimeException();
								case 1:
									System.out.println(1);
									throw new RuntimeException();
								case 2:
									System.out.println(2);
									throw new RuntimeException();
								default:\s
									System.out.println("default");
									throw new RuntimeException();
								}
							} while (t);
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testForeach4() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							for (String s: args) {
								System.out.println();
								throw new RuntimeException();
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testIf() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							if (t)\s
				               throw new RuntimeException();
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testIf2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							if (true)\s
				               throw new RuntimeException();
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testIf3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							if (true)\s
				               throw new RuntimeException();
				           else\s
				               throw new RuntimeException();
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testCFor() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							boolean t = true;
							for (; t ;) {\s
				               throw new RuntimeException();
				           }
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testCFor2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
							final boolean t = true;
							for (; t ;) {\s
				               throw new RuntimeException();
				           }
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testTry() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
				           try {
				           } finally {
				               throw new RuntimeException();
				           }
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testTry2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
				           try {
				           } finally {
				           }
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testTry3() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
				           try {
				           } catch (RuntimeException e) {
				               throw new RuntimeException();
				           }
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testTry4() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
				   static void goo(I i) {
						System.out.println("goo(I)");
				   }
					public static void main(String[] args) {
						goo(() -> {
				           try {
				               throw new RuntimeException();
				           } catch (RuntimeException e) {
				               throw new RuntimeException();
				           }
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testWhileTrue() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
					static void goo(I i) {
				            System.out.println("goo(I)");
				        }
					public static void main(String[] args) {
						goo(() -> {
							while (true) {
							}
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testWhileTrue2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
					static void goo(I i) {
				            System.out.println("goo(I)");
				        }
					public static void main(String[] args) {
						goo(() -> {
							while (true) {
							    while (true) {
				                   if (args == null) break;
							    }
							}
						});
					}
				}
				"""
		},
		"goo(I)");
}
public void testWhileTrue3() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int foo();
				}
				public class X {
					static void goo(I i) {
				            System.out.println("goo(I)");
				        }
					public static void main(String[] args) {
						goo(() -> {
							while (true) {
				                   if (args == null) break;
							}
						});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				goo(() -> {
				^^^
			The method goo(I) in the type X is not applicable for the arguments (() -> {})
			----------
			""");
}
public void testLabeledStatement() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							label:\s
							while (true) {
								while (true) {
									break label;
								}
							}
						});
					}
				}
				"""
		},
		"J");
}
public void testLabeledStatement2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							outerlabel:\s
							label:\s
							while (true) {
								while (true) {
									break outerlabel;
								}
							}
						});
					}
				}
				"""
		},
		"J");
}
public void testLabeledStatement3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							outerlabel:\s
							label:\s
							while (true) {
								while (true) {
									break outerlabel;
								}
							}
						});
					}
				}
				"""
		},
		"J");
}
public void testLabeledStatement4() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							outerlabel:\s
							label:\s
							while (true) {
								while (true) {
									break label;
								}
							}
						});
					}
				}
				"""
		},
		"J");
}
public void testLabeledStatement5() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo();
				}
				interface J {
					void foo();
				}
				public class X {
					static void goo(I i) {
						System.out.println("I");
					}
					static void goo(J i) {
						System.out.println("J");
					}
					public static void main(String[] args) {
						goo(() -> {
							outerlabel:\s
							label:\s
							while (true) {
								while (true) {
									break;
								}
							}
						});
					}
				}
				"""
		},
		"I");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=470232 NPE at org.eclipse.jdt.internal.compiler.ast.WhileStatement.doesNotCompleteNormally
public void testBug470232_While() {
	this.runConformTest(
		new String[] {
			"While.java",
			"""
				import java.util.function.Consumer;
				class While {
				    void m() {
				        t(Long.class, value -> {
				            int x = 1;
				            while (--x >= 0)
				                ;
				        });
				    }
				    <T> void t(Class<T> clazz, Consumer<T> object) {
				    }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=470232 NPE at org.eclipse.jdt.internal.compiler.ast.WhileStatement.doesNotCompleteNormally
public void testBug470232_Do() {
	this.runConformTest(
		new String[] {
			"While.java",
			"""
				import java.util.function.Consumer;
				class While {
				    void m() {
				        t(Long.class, value -> {
				            int x = 1;
				            do {
				            }while (--x >= 0);
				        });
				    }
				    <T> void t(Class<T> clazz, Consumer<T> object) {
				    }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=470232 NPE at org.eclipse.jdt.internal.compiler.ast.WhileStatement.doesNotCompleteNormally
public void testBug470232_For() {
	this.runConformTest(
		new String[] {
			"While.java",
			"""
				import java.util.function.Consumer;
				class While {
				    void m() {
				        t(Long.class, value -> {
				            int x = 1;
				            for(;--x >= 0;)
				            	;
				        });
				    }
				    <T> void t(Class<T> clazz, Consumer<T> object) {
				    }
				}
				"""
		});
}
public static Class testClass() {
	return LambdaShapeTests.class;
}
}
