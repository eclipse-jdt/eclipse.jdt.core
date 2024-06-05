/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *								bug 185682 - Increment/decrement operators mark local variables as read
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AssignmentTest extends AbstractRegressionTest {

public AssignmentTest(String name) {
	super(name);
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.ERROR);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 69 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public static Test suite() {
	Test suite = buildAllCompliancesTestSuite(testClass());
	return suite;
}
/*
 * no effect assignment bug
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=27235
 */
public void test001() {
	this.runConformTest(		new String[] {
			"X.java",
			"""
				public class X {\t
				    int i;\t
				    X(int j) {\t
				    	i = j;\t
				    }\t
				    X() {\t
				    }\t
				    class B extends X {\t
				        B() {\t
				            this.i = X.this.i;\t
				        }\t
				    }\t
				    public static void main(String[] args) {\t
				        X a = new X(3);\t
				        System.out.print(a.i + " ");\t
				        System.out.print(a.new B().i);\t
					}\t
				}\t
				""",
		},
		"3 3");
}

public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					int a;\t
					X next;\t
					public void foo(int arg){\t
				\t
						zork = zork;\t
						arg = zork;\t
				\t
						arg = arg;  // noop\t
						a = a;  // noop\t
						this.next = this.next; // noop\t
						this.next = next; // noop\t
				\t
						next.a = next.a; // could raise NPE\t
						this.next.next.a = next.next.a; // could raise NPE\t
						a = next.a; // could raise NPE\t
						this. a = next.a; \t
					}\t
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				zork = zork;\t
				^^^^
			zork cannot be resolved to a variable
			----------
			2. ERROR in X.java (at line 6)
				zork = zork;\t
				       ^^^^
			zork cannot be resolved to a variable
			----------
			3. ERROR in X.java (at line 7)
				arg = zork;\t
				      ^^^^
			zork cannot be resolved to a variable
			----------
			4. ERROR in X.java (at line 9)
				arg = arg;  // noop\t
				^^^^^^^^^
			The assignment to variable arg has no effect
			----------
			5. ERROR in X.java (at line 10)
				a = a;  // noop\t
				^^^^^
			The assignment to variable a has no effect
			----------
			6. ERROR in X.java (at line 11)
				this.next = this.next; // noop\t
				^^^^^^^^^^^^^^^^^^^^^
			The assignment to variable next has no effect
			----------
			7. ERROR in X.java (at line 12)
				this.next = next; // noop\t
				^^^^^^^^^^^^^^^^
			The assignment to variable next has no effect
			----------
			""");
}
public void test003() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					int portNumber;
					public static void main(String[] args) {
						X x = new X();
						x.portNumber = Integer.parseInt("12");
						x.run();
					}
					private void run() {
						System.out.println(portNumber);
					}
				}""", // =================

		},
		"12");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=151787
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    // correctly passes compilation
				    static class Test1 {
				        private final Object o;
				       \s
				        Test1() {
				            o = new Object();
				        }
				    }
				   \s
				    // correctly passes compilation
				    static class Test2 {
				        private final Object o;
				       \s
				        Test2() {
				            this.o = new Object();
				        }
				    }
				   \s
				    // correctly fails compilation
				    static class Test3 {
				        private final Object o;
				       \s
				        Test3() {
				            System.out.println(o); // illegal; o is not definitely assigned
				            o = new Object();
				        }
				    }
				   \s
				    // correctly passes compilation
				    static class Test4 {
				        private final Object o;
				       \s
				        Test4() {
				            System.out.println(this.o); // legal
				            o = new Object();
				        }
				    }
				   \s
				    // incorrectly passes compilation
				    static class Test5 {
				        private final Object o;
				       \s
				        Test5() {
				            Test5 other = this;
				            other.o = new Object(); // illegal!  other.o is not assignable
				        } // error: this.o is not definitely assigned
				    }
				   \s
				    // flags wrong statement as error
				    static class Test6 {
				        private final Object o;
				        static Test6 initing;
				       \s
				       Test6() {
				           initing = this;
				           System.out.println("greetings");
				           Test6 other = initing;
				           other.o = new Object(); // illegal!  other.o is not assignable
				           o = new Object(); // legal
				       }
				    }
				}
				""", // =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	private final Object o;\n" +
		"	                     ^\n" +
		"The value of the field X.Test1.o is not used\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 13)\n" +
		"	private final Object o;\n" +
		"	                     ^\n" +
		"The value of the field X.Test2.o is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 25)\n" +
		"	System.out.println(o); // illegal; o is not definitely assigned\n" +
		"	                   ^\n" +
		"The blank final field o may not have been initialized\n" +
		"----------\n" +
		(this.complianceLevel >= ClassFileConstants.JDK1_7 ?
		"4. ERROR in X.java (at line 35)\n" +
		"	System.out.println(this.o); // legal\n" +
		"	                        ^\n" +
		"The blank final field o may not have been initialized\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 42)\n" +
		"	private final Object o;\n" +
		"	                     ^\n" +
		"The value of the field X.Test5.o is not used\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 44)\n" +
		"	Test5() {\n" +
		"	^^^^^^^\n" +
		"The blank final field o may not have been initialized\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 46)\n" +
		"	other.o = new Object(); // illegal!  other.o is not assignable\n" +
		"	      ^\n" +
		"The final field X.Test5.o cannot be assigned\n" +
		"----------\n" +
		"8. WARNING in X.java (at line 52)\n" +
		"	private final Object o;\n" +
		"	                     ^\n" +
		"The value of the field X.Test6.o is not used\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 59)\n" +
		"	other.o = new Object(); // illegal!  other.o is not assignable\n" +
		"	      ^\n" +
		"The final field X.Test6.o cannot be assigned\n" +
		"----------\n"
		:
		"4. WARNING in X.java (at line 42)\n" +
		"	private final Object o;\n" +
		"	                     ^\n" +
		"The value of the field X.Test5.o is not used\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 44)\n" +
		"	Test5() {\n" +
		"	^^^^^^^\n" +
		"The blank final field o may not have been initialized\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 46)\n" +
		"	other.o = new Object(); // illegal!  other.o is not assignable\n" +
		"	      ^\n" +
		"The final field X.Test5.o cannot be assigned\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 52)\n" +
		"	private final Object o;\n" +
		"	                     ^\n" +
		"The value of the field X.Test6.o is not used\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 59)\n" +
		"	other.o = new Object(); // illegal!  other.o is not assignable\n" +
		"	      ^\n" +
		"The final field X.Test6.o cannot be assigned\n" +
		"----------\n"));
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190391
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					final int contents;
				\t
					X() {
						contents = 3;
					}
					X(X other) {
						other.contents = 5;
					}
				\t
					public static void main(String[] args) {
						X one = new X();
						System.out.println("one.contents: " + one.contents);
						X two = new X(one);
						System.out.println("one.contents: " + one.contents);
						System.out.println("two.contents: " + two.contents);
					}
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				X(X other) {
				^^^^^^^^^^
			The blank final field contents may not have been initialized
			----------
			2. ERROR in X.java (at line 8)
				other.contents = 5;
				      ^^^^^^^^
			The final field X.contents cannot be assigned
			----------
			""");
}
// final multiple assignment
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						final int v;
						for (int i = 0; i < 10; i++) {
							v = i;
						}
						v = 0;
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				v = i;
				^
			The final local variable v may already have been assigned
			----------
			2. ERROR in X.java (at line 7)
				v = 0;
				^
			The final local variable v may already have been assigned
			----------
			""");
}

// null part has been repeated into NullReferenceTest#test1033
public void test033() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					void foo() {
						String a,b;
						do{
						   a="Hello ";
						}while(a!=null);
							\t
						if(a!=null)
						{
						   b="World!";
						}
						System.out.println(a+b);
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				}while(a!=null);
				       ^
			Redundant null check: The variable a cannot be null at this location
			----------
			2. ERROR in X.java (at line 9)
				if(a!=null)
				   ^
			Null comparison always yields false: The variable a can only be null at this location
			----------
			3. ERROR in X.java (at line 13)
				System.out.println(a+b);
				                     ^
			The local variable b may not have been initialized
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84215
//TODO (philippe) should move to InitializationTest suite
public void test034() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public final class X\s
				{
					public static String vdg;
					public static final String aa = null;
					public static final int a = 14;
					public static final int b = 3;
					private static final int c = 12;
					private static final int d = 2;\s
					private static final int e = 3;\s
					private static final int f = 34;\s
					private static final int g = 35;\s
					private static final int h = 36;\s
					private static final int j = 4;
					private static final int k = 1;
					public static final int aba = 1;
					public static final int as = 11;
					public static final int ad = 12;
					public static final int af = 13;
					public static final int ag = 2;
					public static final int ah = 21;
					public static final int aj = 22;
					public static final int ak = 3;
					public static final String aaad = null;
					public static final int aaaf = 1;
					public static final int aaag = 2;
					public static final int aaha = 2;
					static int cxvvb = 1;
					static int z = a;
					String asdff;
					public static String ppfp;
					public static int ppfpged;
					boolean asfadf;
					boolean cbxbx;
					private static long tyt, rrky;
					private static int dgjt, ykjr6y;
					private static final int krykr = 1;
					protected static int rykr5;
					protected static int dhfg;
					private static int dthj;
					private static int fkffy;
					private static String fhfy;
					protected static String fhmf;
					protected String ryur6;
					protected String dhdthd;
					protected String dth5;
					protected String kfyk;
					private String ntd;
					public int asdasdads;
					public static final int dntdr = 7;
					public static final int asys = 1;
					public static final int djd5rwas = 11;
					public static final int dhds45rjd = 12;
					public static final int srws4jd = 13;
					public static final int s4ts = 2;
					public static final int dshes4 = 21;
					public static final int drthed56u = 22;
					public static final int drtye45 = 23;
					public static final int xxbxrb = 3;
					public static final int xfbxr = 31;
					public static final int asgw4y = 32;
					public static final int hdtrhs5r = 33;
					public static final int dshsh = 34;
					public static final int ds45yuwsuy = 4;
					public static final int astgs45rys = 5;
					public static final int srgs4y = 6;
					public static final int srgsryw45 = -6;
					public static final int srgdtgjd45ry = -7;
					public static final int srdjs43t = 1;
					public static final int sedteued5y = 2;
					public static int jrfd6u;
					public static int udf56u;
					private String jf6tu;
					private String jf6tud;
					String bsrh;
					protected X(String a)
					{
					}
					private long sfhdsrhs;
					private boolean qaafasdfs;
					private int sdgsa;
					private long dgse4;
					long sgrdsrg;
					public void gdsthsr()
					{
					}
					private int hsrhs;
					private void hsrhsdsh()
					{
					}
					private String dsfhshsr;
					protected void sfhsh4rsrh()
					{
					}
					protected void shsrhsh()
					{
					}
					protected void sfhstuje56u()
					{
					}
					public void dhdrt6u()
					{
					}
					public void hdtue56u()
					{
					}
					private void htdws4()
					{
					}
					String mfmgf;
					String mgdmd;
					String mdsrh;
					String nmdr;
					private void oyioyio()
					{
					}
					protected static long oyioyreye()
					{
						return 0;
					}
					protected static long etueierh()
					{
						return 0;
					}
					protected static void sdfgsgs()
					{
					}
					protected static void fhsrhsrh()
					{
					}
				
					long dcggsdg;
					int ssssssgsfh;
					long ssssssgae;
					long ssssssfaseg;
					public void zzzdged()
					{
					}
				\t
					String t;
					protected void xxxxxcbsg()
					{
					}
				
				\t
					public void vdg()
					{
					}
				\t
					private int[] fffcvffffffasdfaef;
					private int[] fffcffffffasdfaef;
					private long[] ffcvfffffffasdfaef;
					private int fffffghffffasdfaef;\s
					private int fffffdffffasdfaef;\s
					private String ffafffffffasdfaef;
				\t
					private void fffffffffasdfaef()
					{
					}
				\t
					private boolean aaaadgasrg;
					private void ddddgaergnj()
					{
					}
				
					private void aaaadgaeg()
					{
					}
				\t
					private void aaaaaaefadfgh()
					{
					}
				\t
					private void addddddddafge()
					{
					}
				\t
					static boolean aaaaaaaefae;
					protected void aaaaaaefaef()
					{
					}
				
					private void ggggseae()
					{
					}
				
					private static void ggggggsgsrg()
					{
					}
				
					private static synchronized void ggggggfsfgsr()
					{
					}
				
					private void aaaaaadgaeg()
					{
					}
				\t
					private void aaaaadgaerg()
					{
					}
				\t
					private void bbbbbbsfryghs()
					{
					}
				\t
					private void bfbbbbbbfssreg()
					{
					}
				
					private void bbbbbbfssfb()
					{
					}
				
					private void bbbbbbfssb()
					{
					}
				
					private void bbbbfdssb()
					{
					}
				\t
					boolean dggggggdsg;
				
					public void hdfhdr()
					{
					}
				\t
					private void dhdrtdrs()
					{
					}
				\t
					private void dghdthtdhd()
					{
					}
				\t
					private void dhdhdtdh()
					{
					}
				\t
					private void fddhdsh()
					{
					}
				\t
					private boolean sdffgsdg()
					{
						return true;
					}
						\t
					private static boolean sdgsdg()
					{
						return false;
					}
				\t
					protected static final void sfdgsg()
					{
					}
				
					static int[] fghtys;
				
					protected static final int sdsst = 1;
					private static X asdfahnr;
					private static int ssdsdbrtyrtdfhd, ssdsrtyrdbdfhd;
					protected static int ssdsrtydbdfhd, ssdsrtydffbdfhd;
					protected static int ssdrtyhrtysdbdfhd, ssyeghdsdbdfhd;
					private static int ssdsdrtybdfhd, ssdsdehebdfhd;
					protected static int ssdthrtsdbdfhd, ssdshethetdbdfhd;
					private static String sstrdrfhdsdbdfhd;
					protected static int ssdsdbdfhd, ssdsdethbdfhd;
					private static long ssdshdfhchddbdfhd;
					private static long ssdsdvbbdfhd;
				\t
				\t
					protected static long ssdsdbdfhd()
					{
						return 0;
					}
				
					protected static long sdgsrsbsf()
					{
						return 0;
					}
				
					protected static void sfgsfgssghr()
					{
					}
				\t
					protected static String sgsgsrg()
					{
						return null;
					}
				
					protected static void sdgshsdygra()
					{
					}
				
					private static String sdfsdfs()
					{
						return null;
					}
				
					static boolean ryweyer;
				
					protected static void adfadfaghsfh()
					{
					}
				\t
					protected static void ghasghasrg()
					{
					}
				
					private static void aadfadfaf()
					{
					}
				
					protected static void aadfadf()
					{
					}
				\t
					private static int fgsfhwr()
					{
						return 0;
					}
				
					protected static int gdfgfgrfg()
					{
						return 0;
					}
				
					protected static int asdfsfs()
					{
						return 0;
					}
				
					protected static String sdgs;
					protected static String sdfsh4e;
					protected static final int gsregs = 0;
				\t
					protected static String sgsgsd()
					{
						return null;
					}
				
					private byte[] sdhqtgwsrh(String rsName, int id)
					{
						String rs = null;
						try
						{
							rs = "";
							return null;
						}
						catch (Exception ex)
						{
						}
						finally
						{
							if (rs != null)
							{
								try
								{
									rs.toString();
								}
								catch (Exception ex)
								{
								}
							}
						}
						return null;
					}
				
					private void dgagadga()
					{
					}
				\t
					private String adsyasta;
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 356)
				if (rs != null)
				    ^^
			Redundant null check: The variable rs cannot be null at this location
			----------
			""",
		null/*classLibs*/,
		true/*shouldFlush*/,
		options);
}
/*
 * Check scenario:  i = i++
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=84480
 * disabled: https://bugs.eclipse.org/bugs/show_bug.cgi?id=111898
 */
public void test035() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					int f;
					void foo(int i) {
						i = i++;
						i = ++i;
						f = f++;
						f = ++f;
						Zork z;\
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				i = ++i;
				^^^^^^^
			The assignment to variable i has no effect
			----------
			2. ERROR in X.java (at line 7)
				f = ++f;
				^^^^^^^
			The assignment to variable f has no effect
			----------
			3. ERROR in X.java (at line 8)
				Zork z;	}
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
public void test036() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					void foo() {
						Object o = new Object();
						do {
							o = null;
						} while (o != null);
						if (o == null) {
							// throw new Exception();
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				} while (o != null);
				         ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 8)
				if (o == null) {
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93588
public void test037() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Object implements Runnable {
					int interval = 5;
					public void run() {
						try {
							Thread.sleep(interval = interval + 100);
							Thread.sleep(interval += 100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				
					public static void main(String[] args) {
						new X().run();
					}
				}
				""",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111703
public void test038() {
	String expectedError = 	this.complianceLevel < ClassFileConstants.JDK16 ?
			"""
				----------
				1. WARNING in X.java (at line 19)
					public void valueChanged(TreeSelectionEvent e) {
					                                            ^
				The parameter e is hiding another local variable defined in an enclosing scope
				----------
				2. ERROR in X.java (at line 23)
					static {
					       ^
				Cannot define static initializer in inner type new ActionListener(){}
				----------
				3. ERROR in X.java (at line 24)
					myTree.addTreeSelectionListener(list);
					^^^^^^
				Cannot make a static reference to the non-static field myTree
				----------
				4. WARNING in X.java (at line 26)
					public void actionPerformed(ActionEvent e) {
					                                        ^
				The parameter e is hiding another local variable defined in an enclosing scope
				----------
				"""
			:
			"""
				----------
				1. WARNING in X.java (at line 19)
					public void valueChanged(TreeSelectionEvent e) {
					                                            ^
				The parameter e is hiding another local variable defined in an enclosing scope
				----------
				2. ERROR in X.java (at line 24)
					myTree.addTreeSelectionListener(list);
					^^^^^^
				Cannot make a static reference to the non-static field myTree
				----------
				3. WARNING in X.java (at line 26)
					public void actionPerformed(ActionEvent e) {
					                                        ^
				The parameter e is hiding another local variable defined in an enclosing scope
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.awt.event.*;
				
				import javax.swing.*;
				import javax.swing.event.*;
				
				public class X {
				    JButton myButton = new JButton();
				    JTree myTree = new JTree();
				    ActionListener action;
				    X() {
				        action = new ActionListener() {
				            public void actionPerformed(ActionEvent e) {
				                if (true) {
				                    // unlock document
				                    final Object document = new Object();
				                    myButton.addActionListener(new ActionListener() {
				                        private static boolean selectionChanged;
				                        static TreeSelectionListener list = new TreeSelectionListener() {
				                            public void valueChanged(TreeSelectionEvent e) {
				                                selectionChanged = true;
				                            }
				                        };
				                      static {
				                      myTree.addTreeSelectionListener(list);
				                      }
				                        public void actionPerformed(ActionEvent e) {
				                            if(!selectionChanged)
				                            myButton.removeActionListener(this);
				                        }
				                    });
				                }
				            }
				        };
				    }
				    public static void main(String[] args) {
				        new X();
				    }
				
				}""",
		},
		expectedError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111898
public void test039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int a = 1;
					    a = a++;
						System.out.print("a="+a);
					\t
						int b = 1;
						System.out.print(b = b++);
						System.out.println("b="+b);
					}
				}
				""",
		},
		"a=11b=1");
}
// warn upon parameter assignment
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
public void test040() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  void foo(boolean b) {
				    b = false;
				  }
				}
				""",
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	b = false;\n" +
		"	^\n" +
		"The parameter b should not be assigned\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// warn upon parameter assignment
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
// diagnose within fake reachable code
public void test041() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  void foo(boolean b) {
				    if (false) {
				      b = false;
				    }
				  }
				}
				""",
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 4)
				b = false;
				^
			The parameter b should not be assigned
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// warn upon parameter assignment
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
// diagnose within fake reachable code
public void test042() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  void foo(boolean b) {
				    if (true) {
				      return;
				    }
				    b = false;
				  }
				}
				""",
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	b = false;\n" +
		"	^\n" +
		"The parameter b should not be assigned\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// warn upon parameter assignment
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
// we only show the 'assignment to final' error here
public void test043() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(final boolean b) {
				    if (false) {
				      b = false;
				    }
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				b = false;
				^
			The final local variable b cannot be assigned. It must be blank and not using a compound assignment
			----------
			""",
		null, true, options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100369
public void test044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					int length1 = 0;
					{
						length1 = length1; // already detected
					}
					int length2 = length2 = 0; // not detected
					int length3 = 0;
					{
						length3 = length3 = 0; // not detected
					}
					static void foo() {
						int length1 = 0;
						length1 = length1; // already detected
						int length2 = length2 = 0; // not detected
						int length3 = 0;
						length3 = length3 = 0; // not detected
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				length1 = length1; // already detected
				^^^^^^^^^^^^^^^^^
			The assignment to variable length1 has no effect
			----------
			2. ERROR in X.java (at line 6)
				int length2 = length2 = 0; // not detected
				    ^^^^^^^^^^^^^^^^^^^^^
			The assignment to variable length2 has no effect
			----------
			3. ERROR in X.java (at line 9)
				length3 = length3 = 0; // not detected
				^^^^^^^^^^^^^^^^^^^^^
			The assignment to variable length3 has no effect
			----------
			4. ERROR in X.java (at line 13)
				length1 = length1; // already detected
				^^^^^^^^^^^^^^^^^
			The assignment to variable length1 has no effect
			----------
			5. ERROR in X.java (at line 14)
				int length2 = length2 = 0; // not detected
				    ^^^^^^^^^^^^^^^^^^^^^
			The assignment to variable length2 has no effect
			----------
			6. ERROR in X.java (at line 16)
				length3 = length3 = 0; // not detected
				^^^^^^^^^^^^^^^^^^^^^
			The assignment to variable length3 has no effect
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133351
public void test045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						int length2 = length2 = 0; // first problem
						int length3 = 0;
						length3 = length3 = 0; // second problem
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				int length2 = length2 = 0; // first problem
				    ^^^^^^^^^^^^^^^^^^^^^
			The assignment to variable length2 has no effect
			----------
			2. ERROR in X.java (at line 5)
				length3 = length3 = 0; // second problem
				^^^^^^^^^^^^^^^^^^^^^
			The assignment to variable length3 has no effect
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
public void test046() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static String s;
					void foo(String s1) {
						X.s = s;\
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				X.s = s;	}
				^^^^^^^
			The assignment to variable s has no effect
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static X MyX;\n" +
			"	public static String s;\n" +
			"	void foo(String s1) {\n" +
			"		X.MyX.s = s;" + // MyX could hold any extending type, hence we must not complain
			"	}\n" +
			"}\n",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
// we could decide that MyX won't change, hence that the assignment
// on line a has no effect, but we accept this as a limit
public void _test048() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static final X MyX = new X();\n" +
			"	public static String s;\n" +
			"	void foo(String s1) {\n" +
			"		X.MyX.s = s;" + // a
			"	}\n" +
			"}\n",
		},
		"ERR");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
// adding a package to the picture
public void test049() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				public class X {
					public static String s;
					void foo(String s1) {
						p.X.s = s;\
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 5)
				p.X.s = s;	}
				^^^^^^^^^
			The assignment to variable s has no effect
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
// adding an inner class to the picture
public void test050() {
	String expectedError = 	this.complianceLevel < ClassFileConstants.JDK16 ?
			"""
				----------
				1. ERROR in p\\X.java (at line 4)
					public static String s;
					                     ^
				The field s cannot be declared static in a non-static inner type, unless initialized with a constant expression
				----------
				2. ERROR in p\\X.java (at line 6)
					X.XX.s = s;    }
					^^^^^^^^^^
				The assignment to variable s has no effect
				----------
				"""
			:
			"""
				----------
				1. ERROR in p\\X.java (at line 6)
					X.XX.s = s;    }
					^^^^^^^^^^
				The assignment to variable s has no effect
				----------
				""";


	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				public class X {
				  class XX {
					 public static String s;
					 void foo(String s1) {
				      X.XX.s = s;\
				    }
				  }
				}
				""",
		},
		expectedError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
// swap lhs and rhs
public void test051() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static String s;
					void foo(String s1) {
						s = X.s;\
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				s = X.s;	}
				^^^^^^^
			The assignment to variable s has no effect
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206017
public void test052() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    int i = "aaa";
				    i = "bbb";
				  }
				}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					int i = "aaa";
					        ^^^^^
				Type mismatch: cannot convert from String to int
				----------
				2. ERROR in X.java (at line 4)
					i = "bbb";
					    ^^^^^
				Type mismatch: cannot convert from String to int
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206017
public void test053() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i = "aaa";
				  {\s
				    i = "bbb";
				  }
				}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int i = "aaa";
					        ^^^^^
				Type mismatch: cannot convert from String to int
				----------
				2. ERROR in X.java (at line 4)
					i = "bbb";
					    ^^^^^
				Type mismatch: cannot convert from String to int
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235543
public void _test054_definite_unassignment_try_catch() {
	runNegativeTest(
		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    final int i;
				    try {
				      if (false) {
				            i = 0;
				            System.out.println(i);
				            throw new MyException();
				      }
				    } catch (Exception e) {
				      i = 1; // missing error
				    }
				  }
				}
				class MyException extends Exception {
				  private static final long serialVersionUID = 1L;
				}"""
	 	},
		// compiler results
	 	"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 11)\n" +
		"	i = 1;\n" +
		"	^\n" +
		"The final local variable i may already have been assigned\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235543
// variant
public void test055_definite_unassignment_try_catch() {
	runNegativeTest(
		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    final int i;
				    try {
				      if (false) {
				            i = 0;
				            System.out.println(i);
				            throw new MyException();
				      }
				    } catch (MyException e) {
				      i = 1;
				    }
				  }
				}
				class MyException extends Exception {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 11)\n" +
		"	i = 1;\n" +
		"	^\n" +
		"The final local variable i may already have been assigned\n" +
		"----------\n",
		// javac options
		JavacTestOptions.EclipseJustification.EclipseBug235543 /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235546
public void test056_definite_unassignment_infinite_for_loop() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    final int i;
				    for (;true;) {
				      if (true) {
				        break;
				      } else {
				        i = 0;
				      }
				    }
				    i = 1;
				    System.out.println(i);
				  }
				}"""
	 	},
		// compiler results
	 	null /* do not check compiler log */,
	 	// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		JavacTestOptions.EclipseJustification.EclipseBug235546 /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235546
// variant
public void test057_definite_unassignment_infinite_while_loop() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    final int i;
				    while (true) {
				      if (true) {
				        break;
				      } else {
				        i = 0;
				      }
				    }
				    i = 1;
				    System.out.println(i);
				  }
				}"""
	 	},
		// compiler results
	 	null /* do not check compiler log */,
	 	// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		JavacTestOptions.EclipseJustification.EclipseBug235546 /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235550
public void test058_definite_unassignment_try_finally() {
	runConformTest(
		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    final int i;
				    do {
				      try {
				        break;
				      } finally {
				        i = 0;
				      }
				    } while (args.length > 0);
				    System.out.println(i);
				  }
				}"""
	 	},
		// runtime result:
	 	"0");
		// NB: javac reports: "error: variable i might be assigned in loop"
		// I hold to be wrong
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235555
public void test059_definite_unassignment_assign_in_for_condition() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    final int i;
				    for (; 0 < (i = 1); i = i + 1) {
				      break;
				    }
				    System.out.println("SUCCESS");
				  }
				}"""
	 	},
		// compiler results
	 	null /* do not check compiler log */,
	 	// runtime results
		"SUCCESS" /* expected output string */,
		"" /* expected error string */,
		JavacTestOptions.JavacHasABug.JavacBug4660984 /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235555
// variant
public void test060_definite_unassignment_assign_in_for_condition() {
	runConformTest(
		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    final int i;
				    for (; 0 < (i = 1);) {
				      break;
				    }
				    System.out.println("SUCCESS");
				  }
				}"""
	 	},
	 	// runtime results
		"SUCCESS" /* expected output string */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=241841
public void test061() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					java.sql.Date d = new java.util.Date();
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				java.sql.Date d = new java.util.Date();
				                  ^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from java.util.Date to java.sql.Date
			----------
			""");
}

// challenge widening conversion
public void test062() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  byte b;
				  short s;
				  char c;
				  boolean z;
				  int i;
				  long j;
				  float f;
				  double d;
				void foo() {
					boolean[] booleans = { b, s, c, z, i, j, f, d, };
					byte[] bytes = { b, s, c, z, i, j, f, d, };
					short[] shorts = { b, s, c, z, i, j, f, d, };
					char[] chars = { b, s, c, z, i, j, f, d, };
					int[] ints = { b, s, c, z, i, j, f, d, };
					long[] longs = { b, s, c, z, i, j, f, d, };
					float[] floats = { b, s, c, z, i, j, f, d, };
					double[] doubles = { b, s, c, z, i, j, f, d, };
				}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				boolean[] booleans = { b, s, c, z, i, j, f, d, };
				                       ^
			Type mismatch: cannot convert from byte to boolean
			----------
			2. ERROR in X.java (at line 11)
				boolean[] booleans = { b, s, c, z, i, j, f, d, };
				                          ^
			Type mismatch: cannot convert from short to boolean
			----------
			3. ERROR in X.java (at line 11)
				boolean[] booleans = { b, s, c, z, i, j, f, d, };
				                             ^
			Type mismatch: cannot convert from char to boolean
			----------
			4. ERROR in X.java (at line 11)
				boolean[] booleans = { b, s, c, z, i, j, f, d, };
				                                   ^
			Type mismatch: cannot convert from int to boolean
			----------
			5. ERROR in X.java (at line 11)
				boolean[] booleans = { b, s, c, z, i, j, f, d, };
				                                      ^
			Type mismatch: cannot convert from long to boolean
			----------
			6. ERROR in X.java (at line 11)
				boolean[] booleans = { b, s, c, z, i, j, f, d, };
				                                         ^
			Type mismatch: cannot convert from float to boolean
			----------
			7. ERROR in X.java (at line 11)
				boolean[] booleans = { b, s, c, z, i, j, f, d, };
				                                            ^
			Type mismatch: cannot convert from double to boolean
			----------
			8. ERROR in X.java (at line 12)
				byte[] bytes = { b, s, c, z, i, j, f, d, };
				                    ^
			Type mismatch: cannot convert from short to byte
			----------
			9. ERROR in X.java (at line 12)
				byte[] bytes = { b, s, c, z, i, j, f, d, };
				                       ^
			Type mismatch: cannot convert from char to byte
			----------
			10. ERROR in X.java (at line 12)
				byte[] bytes = { b, s, c, z, i, j, f, d, };
				                          ^
			Type mismatch: cannot convert from boolean to byte
			----------
			11. ERROR in X.java (at line 12)
				byte[] bytes = { b, s, c, z, i, j, f, d, };
				                             ^
			Type mismatch: cannot convert from int to byte
			----------
			12. ERROR in X.java (at line 12)
				byte[] bytes = { b, s, c, z, i, j, f, d, };
				                                ^
			Type mismatch: cannot convert from long to byte
			----------
			13. ERROR in X.java (at line 12)
				byte[] bytes = { b, s, c, z, i, j, f, d, };
				                                   ^
			Type mismatch: cannot convert from float to byte
			----------
			14. ERROR in X.java (at line 12)
				byte[] bytes = { b, s, c, z, i, j, f, d, };
				                                      ^
			Type mismatch: cannot convert from double to byte
			----------
			15. ERROR in X.java (at line 13)
				short[] shorts = { b, s, c, z, i, j, f, d, };
				                         ^
			Type mismatch: cannot convert from char to short
			----------
			16. ERROR in X.java (at line 13)
				short[] shorts = { b, s, c, z, i, j, f, d, };
				                            ^
			Type mismatch: cannot convert from boolean to short
			----------
			17. ERROR in X.java (at line 13)
				short[] shorts = { b, s, c, z, i, j, f, d, };
				                               ^
			Type mismatch: cannot convert from int to short
			----------
			18. ERROR in X.java (at line 13)
				short[] shorts = { b, s, c, z, i, j, f, d, };
				                                  ^
			Type mismatch: cannot convert from long to short
			----------
			19. ERROR in X.java (at line 13)
				short[] shorts = { b, s, c, z, i, j, f, d, };
				                                     ^
			Type mismatch: cannot convert from float to short
			----------
			20. ERROR in X.java (at line 13)
				short[] shorts = { b, s, c, z, i, j, f, d, };
				                                        ^
			Type mismatch: cannot convert from double to short
			----------
			21. ERROR in X.java (at line 14)
				char[] chars = { b, s, c, z, i, j, f, d, };
				                 ^
			Type mismatch: cannot convert from byte to char
			----------
			22. ERROR in X.java (at line 14)
				char[] chars = { b, s, c, z, i, j, f, d, };
				                    ^
			Type mismatch: cannot convert from short to char
			----------
			23. ERROR in X.java (at line 14)
				char[] chars = { b, s, c, z, i, j, f, d, };
				                          ^
			Type mismatch: cannot convert from boolean to char
			----------
			24. ERROR in X.java (at line 14)
				char[] chars = { b, s, c, z, i, j, f, d, };
				                             ^
			Type mismatch: cannot convert from int to char
			----------
			25. ERROR in X.java (at line 14)
				char[] chars = { b, s, c, z, i, j, f, d, };
				                                ^
			Type mismatch: cannot convert from long to char
			----------
			26. ERROR in X.java (at line 14)
				char[] chars = { b, s, c, z, i, j, f, d, };
				                                   ^
			Type mismatch: cannot convert from float to char
			----------
			27. ERROR in X.java (at line 14)
				char[] chars = { b, s, c, z, i, j, f, d, };
				                                      ^
			Type mismatch: cannot convert from double to char
			----------
			28. ERROR in X.java (at line 15)
				int[] ints = { b, s, c, z, i, j, f, d, };
				                        ^
			Type mismatch: cannot convert from boolean to int
			----------
			29. ERROR in X.java (at line 15)
				int[] ints = { b, s, c, z, i, j, f, d, };
				                              ^
			Type mismatch: cannot convert from long to int
			----------
			30. ERROR in X.java (at line 15)
				int[] ints = { b, s, c, z, i, j, f, d, };
				                                 ^
			Type mismatch: cannot convert from float to int
			----------
			31. ERROR in X.java (at line 15)
				int[] ints = { b, s, c, z, i, j, f, d, };
				                                    ^
			Type mismatch: cannot convert from double to int
			----------
			32. ERROR in X.java (at line 16)
				long[] longs = { b, s, c, z, i, j, f, d, };
				                          ^
			Type mismatch: cannot convert from boolean to long
			----------
			33. ERROR in X.java (at line 16)
				long[] longs = { b, s, c, z, i, j, f, d, };
				                                   ^
			Type mismatch: cannot convert from float to long
			----------
			34. ERROR in X.java (at line 16)
				long[] longs = { b, s, c, z, i, j, f, d, };
				                                      ^
			Type mismatch: cannot convert from double to long
			----------
			35. ERROR in X.java (at line 17)
				float[] floats = { b, s, c, z, i, j, f, d, };
				                            ^
			Type mismatch: cannot convert from boolean to float
			----------
			36. ERROR in X.java (at line 17)
				float[] floats = { b, s, c, z, i, j, f, d, };
				                                        ^
			Type mismatch: cannot convert from double to float
			----------
			37. ERROR in X.java (at line 18)
				double[] doubles = { b, s, c, z, i, j, f, d, };
				                              ^
			Type mismatch: cannot convert from boolean to double
			----------
			""");
}
//challenge narrowing conversion
public void test063() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  byte b;
				  short s;
				  char c;
				  boolean z;
				  int i;
				  long j;
				  float f;
				  double d;
				void foo() {
					boolean[] booleans = { (boolean)b, (boolean)s, (boolean)c, (boolean)z, (boolean)i, (boolean)j, (boolean)f, (boolean)d, };
					byte[] bytes = { (byte)b, (byte)s, (byte)c, (byte)z, (byte)i, (byte)j, (byte)f, (byte)d, };
					short[] shorts = { (short)b, (short)s, (short)c, (short)z, (short)i, (short)j, (short)f, (short)d, };
					char[] chars = { (char)b, (char)s, (char)c, (char)z, (char)i, (char)j, (char)f, (char)d, };
					int[] ints = { (int)b, (int)s, (int)c, (int)z, (int)i, (int)j, (int)f, (int)d, };
					long[] longs = { (long)b, (long)s, (long)c, (long)z, (long)i, (long)j, (long)f, (long)d, };
					float[] floats = { (float)b, (float)s, (float)c, (float)z, (float)i, (float)j, (float)f, (float)d, };
					double[] doubles = { (double)b, (double)s, (double)c, (double)z, (double)i, (double)j, (double)f, (double)d, };
				}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				boolean[] booleans = { (boolean)b, (boolean)s, (boolean)c, (boolean)z, (boolean)i, (boolean)j, (boolean)f, (boolean)d, };
				                       ^^^^^^^^^^
			Cannot cast from byte to boolean
			----------
			2. ERROR in X.java (at line 11)
				boolean[] booleans = { (boolean)b, (boolean)s, (boolean)c, (boolean)z, (boolean)i, (boolean)j, (boolean)f, (boolean)d, };
				                                   ^^^^^^^^^^
			Cannot cast from short to boolean
			----------
			3. ERROR in X.java (at line 11)
				boolean[] booleans = { (boolean)b, (boolean)s, (boolean)c, (boolean)z, (boolean)i, (boolean)j, (boolean)f, (boolean)d, };
				                                               ^^^^^^^^^^
			Cannot cast from char to boolean
			----------
			4. ERROR in X.java (at line 11)
				boolean[] booleans = { (boolean)b, (boolean)s, (boolean)c, (boolean)z, (boolean)i, (boolean)j, (boolean)f, (boolean)d, };
				                                                                       ^^^^^^^^^^
			Cannot cast from int to boolean
			----------
			5. ERROR in X.java (at line 11)
				boolean[] booleans = { (boolean)b, (boolean)s, (boolean)c, (boolean)z, (boolean)i, (boolean)j, (boolean)f, (boolean)d, };
				                                                                                   ^^^^^^^^^^
			Cannot cast from long to boolean
			----------
			6. ERROR in X.java (at line 11)
				boolean[] booleans = { (boolean)b, (boolean)s, (boolean)c, (boolean)z, (boolean)i, (boolean)j, (boolean)f, (boolean)d, };
				                                                                                               ^^^^^^^^^^
			Cannot cast from float to boolean
			----------
			7. ERROR in X.java (at line 11)
				boolean[] booleans = { (boolean)b, (boolean)s, (boolean)c, (boolean)z, (boolean)i, (boolean)j, (boolean)f, (boolean)d, };
				                                                                                                           ^^^^^^^^^^
			Cannot cast from double to boolean
			----------
			8. ERROR in X.java (at line 12)
				byte[] bytes = { (byte)b, (byte)s, (byte)c, (byte)z, (byte)i, (byte)j, (byte)f, (byte)d, };
				                                            ^^^^^^^
			Cannot cast from boolean to byte
			----------
			9. ERROR in X.java (at line 13)
				short[] shorts = { (short)b, (short)s, (short)c, (short)z, (short)i, (short)j, (short)f, (short)d, };
				                                                 ^^^^^^^^
			Cannot cast from boolean to short
			----------
			10. ERROR in X.java (at line 14)
				char[] chars = { (char)b, (char)s, (char)c, (char)z, (char)i, (char)j, (char)f, (char)d, };
				                                            ^^^^^^^
			Cannot cast from boolean to char
			----------
			11. ERROR in X.java (at line 15)
				int[] ints = { (int)b, (int)s, (int)c, (int)z, (int)i, (int)j, (int)f, (int)d, };
				                                       ^^^^^^
			Cannot cast from boolean to int
			----------
			12. ERROR in X.java (at line 16)
				long[] longs = { (long)b, (long)s, (long)c, (long)z, (long)i, (long)j, (long)f, (long)d, };
				                                            ^^^^^^^
			Cannot cast from boolean to long
			----------
			13. ERROR in X.java (at line 17)
				float[] floats = { (float)b, (float)s, (float)c, (float)z, (float)i, (float)j, (float)f, (float)d, };
				                                                 ^^^^^^^^
			Cannot cast from boolean to float
			----------
			14. ERROR in X.java (at line 18)
				double[] doubles = { (double)b, (double)s, (double)c, (double)z, (double)i, (double)j, (double)f, (double)d, };
				                                                      ^^^^^^^^^
			Cannot cast from boolean to double
			----------
			""");
}
public void test064() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						byte b = (byte)1;
						b += 1;
						System.out.print(b);
					}
				}
				""",
		},
		"2"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=282891
public void test065() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportComparingIdentical, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					protected boolean foo = false;
					public boolean test() {
						return foo || (foo = foo());
					}
					public boolean test2() {
						return foo && (foo = foo());
					}
					public boolean test3() {
						return foo && (foo = foo);
					}
					boolean foo() { return true; }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				return foo && (foo = foo);
				              ^^^^^^^^^^^
			The assignment to variable foo has no effect
			----------
			""",
		null,
		true,
		options
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=290376
public void test066() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportComparingIdentical, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public boolean test() {
						int i = 1;
						if (i != (i = 2)) {
							System.out.println("The first warning is unjust.");
						}
						if ((i = 3) != i) {
							System.out.println("The second warning is just.");
						}
						return false;
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if ((i = 3) != i) {
				    ^^^^^^^^^^^^
			Comparing identical expressions
			----------
			""",
		null,
		true,
		options
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=290376
public void test067() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportComparingIdentical, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public boolean test() {
						String s = "Hello World";
						if (s != (s = "")) {
							System.out.println("The first warning is unjust.");
						}
						if ((s = "") != s) {
							System.out.println("The second warning is just.");
						}
						return false;
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if ((s = "") != s) {
				    ^^^^^^^^^^^^^
			Comparing identical expressions
			----------
			""",
		null,
		true,
		options
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=362279
public void test068() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X  {
					Integer f = 'a'; // Field declaration.
					public Integer main() {
						Integer i = 'a'; // local declaration with initialization.
						i = 'a'; // assignment
				                Integer [] ia = new Integer [] { 'a' }; // array initializer.
						return 'a'; // return statement.
						switch (i) {
						case 'a' :   // case statement
						}
					}
				}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_5 ?
		"""
			----------
			1. ERROR in X.java (at line 2)
				Integer f = 'a'; // Field declaration.
				            ^^^
			Type mismatch: cannot convert from char to Integer
			----------
			2. ERROR in X.java (at line 4)
				Integer i = 'a'; // local declaration with initialization.
				            ^^^
			Type mismatch: cannot convert from char to Integer
			----------
			3. ERROR in X.java (at line 5)
				i = 'a'; // assignment
				    ^^^
			Type mismatch: cannot convert from char to Integer
			----------
			4. ERROR in X.java (at line 6)
				Integer [] ia = new Integer [] { 'a' }; // array initializer.
				                                 ^^^
			Type mismatch: cannot convert from char to Integer
			----------
			5. ERROR in X.java (at line 7)
				return 'a'; // return statement.
				       ^^^
			Type mismatch: cannot convert from char to Integer
			----------
			6. ERROR in X.java (at line 8)
				switch (i) {
				        ^
			Cannot switch on a value of type Integer. Only convertible int values or enum variables are permitted
			----------
			""" :
			"""
				----------
				1. ERROR in X.java (at line 2)
					Integer f = 'a'; // Field declaration.
					            ^^^
				Type mismatch: cannot convert from char to Integer
				----------
				2. ERROR in X.java (at line 4)
					Integer i = 'a'; // local declaration with initialization.
					            ^^^
				Type mismatch: cannot convert from char to Integer
				----------
				3. ERROR in X.java (at line 5)
					i = 'a'; // assignment
					    ^^^
				Type mismatch: cannot convert from char to Integer
				----------
				4. ERROR in X.java (at line 6)
					Integer [] ia = new Integer [] { 'a' }; // array initializer.
					                                 ^^^
				Type mismatch: cannot convert from char to Integer
				----------
				5. ERROR in X.java (at line 7)
					return 'a'; // return statement.
					       ^^^
				Type mismatch: cannot convert from char to Integer
				----------
				6. ERROR in X.java (at line 9)
					case 'a' :   // case statement
					     ^^^
				Type mismatch: cannot convert from char to Integer
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=480989
public void testbug480989() {
	String src =
			"""
		public abstract class Unassigned {
		    public Unassigned() {}
		    static class SubClass extends Unassigned {
		        private final String test;
		        public SubClass(String atest) { // rename
		            System.out.println(this.test);
		            this.test = atest;
		            System.out.println(this.test);
		        }
		    }
		    public static void main(String[] args) {
		        new SubClass("Hello World!");
		    }
		}
		""";
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"Unassigned.java",
				src
			},
			"""
				----------
				1. ERROR in Unassigned.java (at line 6)
					System.out.println(this.test);
					                        ^^^^
				The blank final field test may not have been initialized
				----------
				""");
	} else {
		this.runConformTest(
			new String[] {
				"Unassigned.java",
				src
			},
			"null\n" +
			"Hello World!");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=486908
public void testBug486908_A(){
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
	this.runConformTest(new String[] {
			"Random.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				public class Random {
					private final List<Object> values;
					public Random() {
						values = new ArrayList<>();
					}
					public Random(Object arg) {
						if(arg instanceof Random) {
							values = ((Random)(arg)).values; //Compile error here.
						} else {
							throw new IllegalArgumentException("arg is not instance of Random");
						}
					}
					public static void foo() {
						return;
					}
					public static void main(String[] args){
						foo();
					}
				}
				"""
	});
}
}
public void testBug486908_B() {
	this.runConformTest(new String[] {
			"Sample.java",
			"""
				public class Sample {
					public final String value;
					public Sample() {
						this.value = new Sample().value;
					}
					public static void foo() {
						return;
					}
					public static void main(String[] args) {
						foo();
					}
				}
				"""
	});
}
public static Class testClass() {
	return AssignmentTest.class;
}
}
