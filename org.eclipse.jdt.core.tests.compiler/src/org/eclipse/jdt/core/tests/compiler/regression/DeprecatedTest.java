package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.IProblem;

public class DeprecatedTest extends AbstractRegressionTest {
public DeprecatedTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
public void test1() {
	this.runNegativeTest(new String[] {
		"p/B.java",
		"package p;\n" + 
		"class B extends A {\n" + 
		"    float x = super.x;\n" + 
		"}\n",

		"p/A.java",
		"package p;\n" + 
		"class A {\n" + 
		"    /** @deprecated */\n" + 
		"    int x = 1;\n" + 
		"}\n",
	}, 
		new ExpectedProblem[] {
			new ExpectedProblem("p/B.java", 33554505, new String[] {"A", "x", }),
		}
	);
}
public void test2() {
	this.runNegativeTest(new String[] {
		"p/C.java",
		"package p;\n" + 
		"class C {\n" + 
		"    static int x = new A().x;\n" + 
		"}\n",
		
		"p/A.java",
		"package p;\n" + 
		"class A {\n" + 
		"    /** @deprecated */\n" + 
		"    int x = 1;\n" + 
		"}\n",

	}, 
		new ExpectedProblem[] {
			new ExpectedProblem("p/C.java", 33554505, new String[] {"A", "x", }),
		}
	);
}
public void test3() {
	this.runNegativeTest(new String[] {
		"p/Top.java",
		"package p;\n" + 
		"public class Top {\n" + 
		"  \n" + 
		"  class M1 {\n" + 
		"    class M2 {}\n" + 
		"  };\n" + 
		"  \n" + 
		"  static class StaticM1 {\n" + 
		"    static class StaticM2 {\n" + 
		"      class NonStaticM3{}};\n" + 
		"  };\n" + 
		"  \n" + 
		"public static void main(String argv[]){\n" + 
		"  Top tip = new Top();\n" + 
		"  System.out.println(\"Still alive 0\");\n" + 
		"  tip.testStaticMember();\n" + 
		"  System.out.println(\"Still alive 1\");\n" + 
		"  tip.testStaticMember1();\n" + 
		"  System.out.println(\"Still alive 2\");\n" + 
		"  tip.testStaticMember2();\n" + 
		"  System.out.println(\"Still alive 3\");\n" + 
		"  tip.testStaticMember3();\n" + 
		"  System.out.println(\"Still alive 4\");\n" + 
		"  tip.testStaticMember4();\n" + 
		"  System.out.println(\"Completed\");\n" + 
		"}\n" + 
		"  void testMember(){\n" + 
		"    new M1().new M2();}\n" + 
		"  void testStaticMember(){\n" + 
		"    new StaticM1().new StaticM2();}\n" + 
		"  void testStaticMember1(){\n" + 
		"    new StaticM1.StaticM2();}\n" + 
		"  void testStaticMember2(){\n" + 
		"    new StaticM1.StaticM2().new NonStaticM3();}\n" + 
		"  void testStaticMember3(){\n" + 
		"    // define an anonymous subclass of the non-static M3\n" + 
		"    new StaticM1.StaticM2().new NonStaticM3(){};\n" + 
		"  }   \n" + 
		"  void testStaticMember4(){\n" + 
		"    // define an anonymous subclass of the non-static M3\n" + 
		"    new StaticM1.StaticM2().new NonStaticM3(){\n" + 
		"      Object hello(){\n" + 
		"        return new StaticM1.StaticM2().new NonStaticM3();\n" + 
		"      }};\n" + 
		"      \n" + 
		"  }    \n" + 
		"}\n",
		}, 
		new ExpectedProblem[] {
			new ExpectedProblem("p/Top.java", 16777239, new String[] {"Top.StaticM1.StaticM2", }),
		}
	);
}
/**
 * Regression test for PR #1G9ES9B
 */
public void test4() {
	this.runNegativeTest(new String[] {
		"p/Warning.java",
		"package p;\n" + 
		"import java.util.Date;\n" +
		"public class Warning {\n" +
		"public Warning() {\n" +
		"     super();\n" +
		"     Date dateObj = new Date();\n" +
		"     dateObj.UTC(1,2,3,4,5,6);\n" +
		"}\n" +
		"}\n",
		}, 
		new ExpectedProblem[] {
			new ExpectedProblem("p/Warning.java", IProblem.UsingDeprecatedMethod, new String[] {"Date", "UTC", "int, int, int, int, int, int"}),
			new ExpectedProblem("p/Warning.java", IProblem.NonStaticAccessToStaticMethod, new String[] {"Date", "UTC", "int, int, int, int, int, int"}),
		}
	);
}
public static Class testClass() {
	return DeprecatedTest.class;
}
}
