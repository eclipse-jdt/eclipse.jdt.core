/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
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
/*		try {
			String sourceA002 =
				"public class A002 {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(); /* \\u000d: CARRIAGE RETURN * /\n" +
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
		}*/
	}
}