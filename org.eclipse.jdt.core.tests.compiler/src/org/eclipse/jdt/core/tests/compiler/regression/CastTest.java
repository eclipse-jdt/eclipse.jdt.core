package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

public class CastTest extends AbstractRegressionTest {
	
public CastTest(String name) {
	super(name);
}
public static Test suite() {

	if (false) {
	   	TestSuite ts;
		//some of the tests depend on the order of this suite.
		ts = new TestSuite();
		ts.addTest(new CastTest("test221"));
		return new RegressionTestSetup(ts);
	}
	return setupSuite(testClass());
}

/*
 * check extra checkcast (interface->same interface)
 */
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"       Cloneable c1 = new int[0]; \n"+
			"		Cloneable c2 = (Cloneable)c1; \n" +
			"		System.out.print(\"SUCCESS\");	\n" +
			"    }	\n" +
			"}	\n",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String actualOutput = null;
	try {
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED); 
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue("ClassFormatException", false);
	} catch (IOException e) {
		assertTrue("IOException", false);
	}

	String expectedOutput =
		"  /*  Method descriptor  #15 ([Ljava/lang/String;)V */\n" + 
		"  public static void main(String[] args);\n" + 
		"    /* Stack: 2, Locals: 3 */\n" + 
		"    Code attribute:\n" + 
		"       0  iconst_0\n" + 
		"       1  newarray #10 int\n" + 
		"       3  astore_1\n" + 
		"       4  aload_1\n" + 
		"       5  astore_2\n" + 
		"       6  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
		"       9  ldc #23 <String \"SUCCESS\">\n" + 
		"      11  invokevirtual #29 <Method java.io.PrintStream#print(java.lang.String arg) void>\n" + 
		"      14  return\n";
	if (actualOutput.indexOf(expectedOutput) == -1){
		System.out.println(Util.displayString(actualOutput, 2));
	}
	assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
}


public static Class testClass() {
	return CastTest.class;
}
}
