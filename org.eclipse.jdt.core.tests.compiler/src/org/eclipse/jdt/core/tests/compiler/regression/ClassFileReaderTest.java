/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import java.io.*;

import org.eclipse.jdt.core.tests.util.Util;
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
		String[] fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					new File(SOURCE_DIRECTORY + File.separator + fileNames[i]).delete();
				}
			}
		}
		
		dir = new File(EVAL_DIRECTORY);
		fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					new File(EVAL_DIRECTORY + File.separator + fileNames[i]).delete();
				}
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
			.append("\" -preserveAllLocals -nowarn -g -classpath \"")
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
				org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(EVAL_DIRECTORY + File.separator + "A001.class");
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

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25188
	 */
	public void test002() {
		try {
			String sourceA002 =
				"public class A002 {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(); /* \\u000d: CARRIAGE RETURN */\n" +
				"		System.out.println();\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA002, "A002");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A002.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 3, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 6, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 12, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
		} finally {
			removeTempClass("A002");
		}
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26098
	 */
	public void test003() {
		try {
			String sourceA003 =
				"public class A003 {\n" +
				"\n" +
				"	public int bar() {\n" +
				"		return 0;\n" +
				"	}\n" +
				"	\n" +
				"	public void foo() {\n" +
				"		System.out.println(bar());\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA003, "A003");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A003.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 3, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[2];
			assertEquals("wrong name", "foo", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 2, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 8, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 10, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 9, lineNumberTable[1][1]);
		} finally {
			removeTempClass("A003");
		}
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test004() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   && !b) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 6, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 5, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 11, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 15, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 7, lineNumberTable[4][1]);
			assertEquals("wrong pc[5]", 22, lineNumberTable[5][0]);
			assertEquals("wrong line[5]", 9, lineNumberTable[5][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test005() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   && true) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 9, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test006() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   && false) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 2, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 8, lineNumberTable[1][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test007() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (true\n" +
				"		   && !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 6, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 12, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test008() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (false\n" +
				"		   && !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 2, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 8, lineNumberTable[1][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test009() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   || !b) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 6, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 5, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 11, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 15, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 7, lineNumberTable[4][1]);
			assertEquals("wrong pc[5]", 22, lineNumberTable[5][0]);
			assertEquals("wrong line[5]", 9, lineNumberTable[5][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test010() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   || true) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 3, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 6, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 10, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 8, lineNumberTable[2][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test011() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   || false) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 9, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test012() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (true\n" +
				"		   || !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 3, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 6, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 8, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 8, lineNumberTable[2][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test013() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (false\n" +
				"		   || !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 6, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 12, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test014() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   == !b) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 6, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 5, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 28, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 7, lineNumberTable[4][1]);
			assertEquals("wrong pc[5]", 35, lineNumberTable[5][0]);
			assertEquals("wrong line[5]", 9, lineNumberTable[5][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test015() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   == true) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 9, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test016() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   == false) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 9, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test017() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (true\n" +
				"		   == !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 6, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 12, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test018() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (false\n" +
				"		   == !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 6, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 12, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 * http:  //bugs.eclipse.org/bugs/show_bug.cgi?id=26881
	 */
	public void test019() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		int i = 6;\n" +
				"		if ((i == 5)\n" +
				"			? b : !b) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 6, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 5, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 10, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 21, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 7, lineNumberTable[4][1]);
			assertEquals("wrong pc[5]", 28, lineNumberTable[5][0]);
			assertEquals("wrong line[5]", 9, lineNumberTable[5][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test020() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (i\n" +
				"			>= 5) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 5, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 4, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 8, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 15, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 8, lineNumberTable[4][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test021() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (i\n" +
				"			>= 0) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 7, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 14, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test022() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (0\n" +
				"			>= i) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 7, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 14, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test023() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (i\n" +
				"			> 0) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 7, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 14, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test024() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (0\n" +
				"			> i) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 7, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 14, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test025() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (i\n" +
				"			> 5) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 5, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 4, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 8, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 15, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 8, lineNumberTable[4][1]);
		} finally {
			removeTempClass("A");
		}
	}	


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test026() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (i\n" +
				"			< 0) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 7, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 14, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test027() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (0\n" +
				"			< i) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 7, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 14, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test028() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (i\n" +
				"			< 5) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 5, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 4, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 8, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 15, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 8, lineNumberTable[4][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test029() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (i\n" +
				"			<= 0) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 7, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 14, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test030() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (0\n" +
				"			<= i) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 7, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 14, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test031() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (i\n" +
				"			<= 5) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 5, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 4, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 8, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 15, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 8, lineNumberTable[4][1]);
		} finally {
			removeTempClass("A");
		}
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test032() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if (i\n" +
				"			<= 5) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 5, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 4, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 8, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 15, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 8, lineNumberTable[4][1]);
		} finally {
			removeTempClass("A");
		}
	}		

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test033() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   & !b) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 6, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 5, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 29, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 7, lineNumberTable[4][1]);
			assertEquals("wrong pc[5]", 36, lineNumberTable[5][0]);
			assertEquals("wrong line[5]", 9, lineNumberTable[5][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test034() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   & true) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 9, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test035() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   & false) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 2, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 8, lineNumberTable[1][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test036() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (true\n" +
				"		   & !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 6, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 12, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test037() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (false\n" +
				"		   & !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 2, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 8, lineNumberTable[1][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test038() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   | !b) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 6, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 5, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 29, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 7, lineNumberTable[4][1]);
			assertEquals("wrong pc[5]", 36, lineNumberTable[5][0]);
			assertEquals("wrong line[5]", 9, lineNumberTable[5][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test039() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   | true) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 3, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 6, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 10, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 8, lineNumberTable[2][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test040() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   | false) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 9, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test041() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (true\n" +
				"		   | !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 3, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 6, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 8, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 8, lineNumberTable[2][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test042() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (false\n" +
				"		   | !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 6, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 12, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test043() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   ^ !b) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 6, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 5, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 5, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 6, lineNumberTable[3][1]);
			assertEquals("wrong pc[4]", 29, lineNumberTable[4][0]);
			assertEquals("wrong line[4]", 7, lineNumberTable[4][1]);
			assertEquals("wrong pc[5]", 36, lineNumberTable[5][0]);
			assertEquals("wrong line[5]", 9, lineNumberTable[5][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test044() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   ^ true) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 9, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test045() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 6;\n" +
				"		if ((i == 6) \n" +
				"		   ^ false) {   	\n" +
				"		   	System.out.println(i);\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 3, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 4, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 9, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 16, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test046() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (true\n" +
				"		   ^ !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 6, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 12, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test047() {
		try {
			String sourceA =
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = false;\n" +
				"		if (false\n" +
				"		   ^ !b) {   	\n" +
				"		   	System.out.println();\n" +
				"		   }\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA, "A");
			org.eclipse.jdt.core.util.IClassFileReader classFileReader = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A.class", org.eclipse.jdt.core.util.IClassFileReader.ALL);
			org.eclipse.jdt.core.util.IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
			assertEquals("wrong size", 2, methodInfos.length);
			org.eclipse.jdt.core.util.IMethodInfo methodInfo = methodInfos[1];
			assertEquals("wrong name", "main", new String(methodInfo.getName()));
			org.eclipse.jdt.core.util.ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertNotNull("No code attribute", codeAttribute);
			org.eclipse.jdt.core.util.ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			assertNotNull("No code line number attribute", lineNumberAttribute);
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			assertEquals("wrong size", 4, lineNumberTable.length);
			assertEquals("wrong pc[0]", 0, lineNumberTable[0][0]);
			assertEquals("wrong line[0]", 3, lineNumberTable[0][1]);
			assertEquals("wrong pc[1]", 2, lineNumberTable[1][0]);
			assertEquals("wrong line[1]", 5, lineNumberTable[1][1]);
			assertEquals("wrong pc[2]", 6, lineNumberTable[2][0]);
			assertEquals("wrong line[2]", 6, lineNumberTable[2][1]);
			assertEquals("wrong pc[3]", 12, lineNumberTable[3][0]);
			assertEquals("wrong line[3]", 8, lineNumberTable[3][1]);
		} finally {
			removeTempClass("A");
		}
	}	
}
