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

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.IExceptionTableEntry;
import org.eclipse.jdt.core.util.ILineNumberAttribute;
import org.eclipse.jdt.core.util.IMethodInfo;

public class DebugAttributeTest extends AbstractRegressionTest {
	private static final String SOURCE_DIRECTORY = Util.getOutputDirectory()  + File.separator + "source";
	private static final String EVAL_DIRECTORY = Util.getOutputDirectory()  + File.separator + "eval";

	public DebugAttributeTest(String name) {
		super(name);
	}
	public static Test suite() {
		return setupSuite(testClass());
	}

	public static Class testClass() {
		return DebugAttributeTest.class;
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
				"\n" +
				"	static int foo(boolean bool) {\n" +
				"	  int j;\n" +
				"	  try {\n" +
				"	    if (bool) return 1;\n" +
				"	    j = 2;\n" +
				"	  } finally {\n" +
				"	    j = 3;\n" +
				"	  }\n" +
				"	  return j;\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(false);\n" +
				"	}\n" +
				"}";
			compileAndDeploy(sourceA001, "A001");
			IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(EVAL_DIRECTORY + File.separator + "A001.class", IClassFileReader.ALL);
			IMethodInfo[] methods = classFileReader.getMethodInfos();
			assertEquals("wrong size", 3, methods.length);
			IMethodInfo methodInfo = (IMethodInfo) methods[1];
			assertEquals("wrong name", "foo", new String(methodInfo.getName()));
			ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			assertEquals("Wrong size", 3, codeAttribute.getExceptionTableLength());
			IExceptionTableEntry[] entries = codeAttribute.getExceptionTable();
			// any exception handler
			assertEquals("Wrong index", 0, entries[0].getCatchTypeIndex());
			assertEquals("Wrong index", 0, entries[1].getCatchTypeIndex());
			assertEquals("Wrong index", 0, entries[2].getCatchTypeIndex());
			
			assertEquals("Wrong startpc", 0, entries[0].getStartPC());
			assertEquals("Wrong endpc", 7, entries[0].getEndPC());

			assertEquals("Wrong startpc", 9, entries[1].getStartPC());
			assertEquals("Wrong endpc", 14, entries[1].getEndPC());

			assertEquals("Wrong startpc", 25, entries[2].getStartPC());
			assertEquals("Wrong endpc", 28, entries[2].getEndPC());
			
			ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			if (lineNumberAttribute != null) {
				int[][] lineEntries = lineNumberAttribute.getLineNumberTable();
				assertNotNull("No entries", lineEntries);
				assertEquals("wrong size", 7, lineEntries.length);
				lineEntries[0][0] = 0;
				lineEntries[0][1] = 6;
				lineEntries[1][0] = 9;
				lineEntries[1][1] = 7;
				lineEntries[2][0] = 14;
				lineEntries[2][1] = 8;
				lineEntries[3][0] = 21;
				lineEntries[3][1] = 9;
				lineEntries[4][0] = 23;
				lineEntries[4][1] = 10;
				lineEntries[5][0] = 25;
				lineEntries[5][1] = 8;
				lineEntries[6][0] = 28;
				lineEntries[6][1] = 11;
			}
		} finally {
			removeTempClass("A001");
		}
	}			
}
