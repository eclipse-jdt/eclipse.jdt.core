package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import java.io.*;

import org.eclipse.jdt.core.tests.compiler.regression.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

public class ClassFileReaderTest extends AbstractRegressionTest {
	private static final String SOURCE_DIRECTORY = Util.getOutputDirectory()  + File.separator + "source";
	private static final String EVAL_DIRECTORY = Util.getOutputDirectory()  + File.separator + "eval";

	public ClassFileReaderTest(String name) {
		super(name);
	}
	public static Test suite() {
		return setupSuite(testClass());
	}

	public static Class testClass() {
		return ClassFileReaderTest.class;
	}

	public void removeTempClass(String className) {
		File dir = new File(SOURCE_DIRECTORY);
		String[] filesNames = dir.list();
		for (int i = 0, max = filesNames.length; i < max; i++) {
			if (filesNames[i].indexOf(className) != -1) {
				new File(SOURCE_DIRECTORY + File.separator + filesNames[i]).delete();
			}
		}
		
		dir = new File(EVAL_DIRECTORY);
		filesNames = dir.list();
		for (int i = 0, max = filesNames.length; i < max; i++) {
			if (filesNames[i].indexOf(className) != -1) {
				new File(EVAL_DIRECTORY + File.separator + filesNames[i]).delete();
			}
		}
	
	}

	public void compileAndDeploy(String source, String className) {
		File directory = new File(SOURCE_DIRECTORY);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + SOURCE_DIRECTORY);
				return;
			}
		}
		String fileName = SOURCE_DIRECTORY + File.separator + className + ".java";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		StringBuffer buffer = new StringBuffer();
		buffer
			.append("\"")
			.append(fileName)
			.append("\" -d \"")
			.append(EVAL_DIRECTORY)
			.append("\" -nowarn -g -classpath \"")
			.append(Util.getJREDirectory() + "/lib/rt.jar;")
			.append(SOURCE_DIRECTORY)
			.append("\"");
		org.eclipse.jdt.internal.compiler.batch.Main.compile(buffer.toString());
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=15051
	 */
	public void test001() {
		try {
			String sourceA001 =
				"public class A001 {\n" +
				"	private int i = 6;\n" +
				"	public int foo() {\n" +
				"		class A {\n" +
				"			int get() {\n" +
				"				return i;\n" +
				"			}\n" +
				"		}\n" +
				"		return new A().get();\n" +
				"	}\n" +
				"};";
			compileAndDeploy(sourceA001, "A001");
			try {
				ClassFileReader classFileReader = ClassFileReader.read(EVAL_DIRECTORY + File.separator + "A001.class");
				IBinaryMethod[] methods = classFileReader.getMethods();
				assertEquals("wrong size", 3, methods.length);
				MethodInfo methodInfo = (MethodInfo) methods[2];
				assertEquals("wrong name", "access$0", new String(methodInfo.getSelector()));
				assertTrue("Not synthetic", methodInfo.isSynthetic());
			} catch (ClassFormatException e) {
				assertTrue(false);
			} catch (IOException e) {
				assertTrue(false);
			}
		} finally {
			removeTempClass("A001");
		}
	}			
}