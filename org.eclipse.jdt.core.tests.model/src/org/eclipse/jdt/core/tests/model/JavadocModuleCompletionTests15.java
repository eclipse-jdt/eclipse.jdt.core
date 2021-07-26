/*******************************************************************************
 * Copyright (c) 2021 IBM and others.
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
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.Test;

public class JavadocModuleCompletionTests15 extends AbstractJavaModelCompletionTests {

	static {
		//		TESTS_NAMES = new String[]{"test034"};
	}

	public JavadocModuleCompletionTests15(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
	}

	public static Test suite() {
		return buildModelTestSuite(JavadocModuleCompletionTests15.class);
	}

	/**
	 * ContentAssist for Module Javadoc support is available for java 15 and above Modular projects
	 * @throws Exception
	 */
	public void test566060_001() throws Exception {
		IJavaProject project1 = createJavaProject("Completion15_0", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		try {
			project1.open(null);

			String filePath1 = "/Completion15_0/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/p3";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p3/MNO.java";
			fileContent1 = "" +
					"package p3;\n\n" +
					"public class MNO  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/module-info.java";
			fileContent1 =  "" +
					"module testM {\n" +
					"	exports p1;\n" +
					"	exports p3;\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/p2";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p2/XYZ.java";
			fileContent1 = "" +
					"package p2;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  testM/ \n" +
					" *\n"+
					" */\n" +
					"public class XYZ  {\n" +
					"}";
			createFile(filePath1, fileContent1);
			String completeBehind = "testM/";
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

			project1.close(); // sync
			project1.open(null);

			int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath1);
			unit.codeComplete(cursorLocation, requestor);

			int PACKAGE_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
			String expected = "p1 - testM/[PACKAGE_REF]{testM/p1, p1 - testM/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
							"p3 - testM/[PACKAGE_REF]{testM/p3, p3 - testM/, null, null, " + PACKAGE_RELEVANCE + "}" ;
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
		}
	}

	/**
	 * ContentAssist for Module Javadoc support is available for java 15 and above Modular projects
	 * @throws Exception
	 */
	public void test566060_002() throws Exception {
		IJavaProject project1 = createJavaProject("Completion15_1", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		IJavaProject project2 = createJavaProject("Completion15_2", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		try {
			project1.open(null);

			String filePath1 = "/Completion15_1/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion15_1/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_1/src/p2";
			createFolder(filePath1);

			filePath1 = "/Completion15_1/src/p2/XYZ.java";
			fileContent1 = "" +
					"package p2;\n\n" +
					"public class XYZ  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_1/src/module-info.java";
			fileContent1 =  "" +
					"module testM {\n" +
					"	exports p1;\n" +
					"}";
			createFile(filePath1, fileContent1);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

			project2.open(null);

			String filePath2 = "/Completion15_2/src/p3";
			createFolder(filePath2);

			filePath2 = "/Completion15_2/src/module-info.java";
			String fileContent2 = "" +
					"module testQ {\n" +
					"	exports p3;\n\n" +
					"	requires testM;\n" +
					"}";
			createFile(filePath2, fileContent2);

			filePath2 = "/Completion15_2/src/p3/MNO.java";
			fileContent2 = "" +
					"package p3;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  testM/ \n" +
					" *\n"+
					" */\n" +
					"public class MNO  {\n" +
					"}";
			createFile(filePath2, fileContent2);
			String completeBehind = "testM/";
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			addClasspathEntry(project2, JavaCore.newContainerEntry(project1.getPath()));

			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			int cursorLocation = fileContent2.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath2);
			unit.codeComplete(cursorLocation, requestor);

			int PACKAGE_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
			String expected = "p1 - testM/[PACKAGE_REF]{testM/p1, p1 - testM/, null, null, " + PACKAGE_RELEVANCE + "}";
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
			deleteProject(project2);
		}
	}

	/**
	 * ContentAssist for Module Javadoc support is available for java 15 and above Modular projects for jre modules
	 * @throws Exception
	 */
	public void test566060_003() throws Exception {
		IJavaProject project1 = createJavaProject("Completion15_0", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		try {
			project1.open(null);

			String filePath1 = "/Completion15_0/src/module-info.java";
			String fileContent1 =  "" +
					"module testM {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p1/ABC.java";
			fileContent1 = "" +
					"package p1;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  java.base/ \n" +
					" *\n"+
					" */\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			String completeBehind = "java.base/";
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

			project1.close(); // sync
			project1.open(null);

			int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath1);
			unit.codeComplete(cursorLocation, requestor);

			int PACKAGE_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
			String expected = "java.io - java.base/[PACKAGE_REF]{java.base/java.io, java.io - java.base/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
					"java.lang - java.base/[PACKAGE_REF]{java.base/java.lang, java.lang - java.base/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
					"java.lang.annotation - java.base/[PACKAGE_REF]{java.base/java.lang.annotation, java.lang.annotation - java.base/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
					"java.lang.invoke - java.base/[PACKAGE_REF]{java.base/java.lang.invoke, java.lang.invoke - java.base/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
					"java.util - java.base/[PACKAGE_REF]{java.base/java.util, java.util - java.base/, null, null, " + PACKAGE_RELEVANCE + "}";
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
		}
	}

	/**
	 * ContentAssist for Module Javadoc support is available for java 15 and above non-modular projects for default modules provided by jre.
	 * @throws Exception
	 */
	public void test566060_004() throws Exception {
		IJavaProject project1 = createJavaProject("Completion15_0", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		try {
			project1.open(null);

			String filePath1 = "/Completion15_0/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  java.base/ \n" +
					" *\n"+
					" */\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			String completeBehind = "java.base/";

			project1.close(); // sync
			project1.open(null);

			int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath1);
			unit.codeComplete(cursorLocation, requestor);

			int PACKAGE_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
			String expected = "java.io - java.base/[PACKAGE_REF]{java.base/java.io, java.io - java.base/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
					"java.lang - java.base/[PACKAGE_REF]{java.base/java.lang, java.lang - java.base/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
					"java.lang.annotation - java.base/[PACKAGE_REF]{java.base/java.lang.annotation, java.lang.annotation - java.base/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
					"java.lang.invoke - java.base/[PACKAGE_REF]{java.base/java.lang.invoke, java.lang.invoke - java.base/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
					"java.util - java.base/[PACKAGE_REF]{java.base/java.util, java.util - java.base/, null, null, " + PACKAGE_RELEVANCE + "}";
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
		}
	}

	/**
	 * Negative case. Module Javadoc support is not available for java 14 and below modular projects.
	 * @throws Exception
	 */
	public void test566060_005() throws Exception {
		IJavaProject project1 = createJavaProject("Completion14_0", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "14");
		try {
			project1.open(null);

			String filePath1 = "/Completion14_0/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion14_0/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion14_0/src/p3";
			createFolder(filePath1);

			filePath1 = "/Completion14_0/src/p3/MNO.java";
			fileContent1 = "" +
					"package p3;\n\n" +
					"public class MNO  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion14_0/src/module-info.java";
			fileContent1 =  "" +
					"module testM {\n" +
					"	exports p1;\n" +
					"	exports p3;\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion14_0/src/p2";
			createFolder(filePath1);

			filePath1 = "/Completion14_0/src/p2/XYZ.java";
			fileContent1 = "" +
					"package p2;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  testM/ \n" +
					" *\n"+
					" */\n" +
					"public class XYZ  {\n" +
					"}";
			createFile(filePath1, fileContent1);
			String completeBehind = "testM/";
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

			project1.close(); // sync
			project1.open(null);

			int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath1);
			unit.codeComplete(cursorLocation, requestor);

			String expected = "";
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
		}
	}

	/**
	 * Negative case. Module Javadoc support is not available for java 14 and below non-modular projects.
	 * @throws Exception
	 */
	public void test566060_006() throws Exception {
		IJavaProject project1 = createJavaProject("Completion14_2", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "14");
		try {
			project1.open(null);

			String filePath1 = "/Completion14_2/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion14_2/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  java.base/ \n" +
					" *\n"+
					" */\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			String completeBehind = "java.base/";

			project1.close(); // sync
			project1.open(null);

			int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath1);
			unit.codeComplete(cursorLocation, requestor);

			String expected = "";
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
		}
	}

	/**
	 * ContentAssist for Module Javadoc support is available for java 15 and above Modular projects
	 * @throws Exception
	 */
	public void test566060_007() throws Exception {
		IJavaProject project1 = createJavaProject("Completion15_1", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		IJavaProject project2 = createJavaProject("Completion15_2", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		try {
			project1.open(null);

			String filePath1 = "/Completion15_1/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion15_1/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_1/src/p2";
			createFolder(filePath1);

			filePath1 = "/Completion15_1/src/p2/XYZ.java";
			fileContent1 = "" +
					"package p2;\n\n" +
					"public class XYZ  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_1/src/module-info.java";
			fileContent1 =  "" +
					"module testM {\n" +
					"	exports p1;\n" +
					"}";
			createFile(filePath1, fileContent1);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

			project2.open(null);

			String filePath2 = "/Completion15_2/src/p3";
			createFolder(filePath2);

			filePath2 = "/Completion15_2/src/module-info.java";
			String fileContent2 = "" +
					"module testQ {\n" +
					"	exports p3;\n\n" +
					"	requires testM;\n" +
					"}";
			createFile(filePath2, fileContent2);

			filePath2 = "/Completion15_2/src/p3/MNO.java";
			fileContent2 = "" +
					"package p3;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  test \n" +
					" *\n"+
					" */\n" +
					"public class MNO  {\n" +
					"}";
			createFile(filePath2, fileContent2);
			String completeBehind = "test";
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			addClasspathEntry(project2, JavaCore.newContainerEntry(project1.getPath()));

			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			int cursorLocation = fileContent2.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath2);
			unit.codeComplete(cursorLocation, requestor);

			int MODULE_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
			String expected = "[MODULE_REF]{testM/, testM, null, null, " + MODULE_RELEVANCE + "}\n"
							+ "[MODULE_REF]{testQ/, testQ, null, null, " + MODULE_RELEVANCE + "}";
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
			deleteProject(project2);
		}
	}

	/**
	 * ContentAssist for Module Javadoc support is available for java 15 and above Modular projects
	 * @throws Exception
	 */
	public void test566060_008() throws Exception {
		IJavaProject project1 = createJavaProject("Completion15_1", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		IJavaProject project2 = createJavaProject("Completion15_2", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		try {
			project1.open(null);

			String filePath1 = "/Completion15_1/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion15_1/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_1/src/p2";
			createFolder(filePath1);

			filePath1 = "/Completion15_1/src/p2/XYZ.java";
			fileContent1 = "" +
					"package p2;\n\n" +
					"public class XYZ  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_1/src/module-info.java";
			fileContent1 =  "" +
					"module testM.partial.mod {\n" +
					"	exports p1;\n" +
					"}";
			createFile(filePath1, fileContent1);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

			project2.open(null);

			String filePath2 = "/Completion15_2/src/p3";
			createFolder(filePath2);

			filePath2 = "/Completion15_2/src/module-info.java";
			String fileContent2 = "" +
					"module testQ {\n" +
					"	exports p3;\n\n" +
					"	requires testM.partial.mod;\n" +
					"}";
			createFile(filePath2, fileContent2);

			filePath2 = "/Completion15_2/src/p3/MNO.java";
			fileContent2 = "" +
					"package p3;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  testM.par \n" +
					" *\n"+
					" */\n" +
					"public class MNO  {\n" +
					"}";
			createFile(filePath2, fileContent2);
			String completeBehind = "testM.par";
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			addClasspathEntry(project2, JavaCore.newContainerEntry(project1.getPath()));

			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			int cursorLocation = fileContent2.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath2);
			unit.codeComplete(cursorLocation, requestor);

			int MODULE_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED+ R_QUALIFIED;
			String expected = "[MODULE_REF]{testM.partial.mod/, testM.partial.mod, null, null, " + MODULE_RELEVANCE + "}";
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
			deleteProject(project2);
		}
	}

	/**
	 * ContentAssist for Module Javadoc support is available for java 15 and above Modular projects
	 * @throws Exception
	 */
	public void test566060_009() throws Exception {
		IJavaProject project1 = createJavaProject("Completion15_0", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		try {
			project1.open(null);

			String filePath1 = "/Completion15_0/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/p3";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p3/MNO.java";
			fileContent1 = "" +
					"package p3;\n\n" +
					"public class MNO  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/module-info.java";
			fileContent1 =  "" +
					"module testM {\n" +
					"	exports p1;\n" +
					"	exports p3;\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/p2";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p2/XYZ.java";
			fileContent1 = "" +
					"package p2;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  testM/p \n" +
					" *\n"+
					" */\n" +
					"public class XYZ  {\n" +
					"}";
			createFile(filePath1, fileContent1);
			String completeBehind = "testM/p";
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

			project1.close(); // sync
			project1.open(null);

			int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath1);
			unit.codeComplete(cursorLocation, requestor);

			int PACKAGE_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
			String expected = "p1 - testM/[PACKAGE_REF]{testM/p1, p1 - testM/, null, null, " + PACKAGE_RELEVANCE + "}\n" +
							"p3 - testM/[PACKAGE_REF]{testM/p3, p3 - testM/, null, null, " + PACKAGE_RELEVANCE + "}" ;
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
		}
	}

	/**
	 * ContentAssist for Module Javadoc support is available for java 15 and above Modular projects
	 * @throws Exception
	 */
	public void test566060_010() throws Exception {
		IJavaProject project1 = createJavaProject("Completion15_0", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		try {
			project1.open(null);

			String filePath1 = "/Completion15_0/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"public class ABC  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/p3";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p3/MNO.java";
			fileContent1 = "" +
					"package p3;\n\n" +
					"public class MNO  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/module-info.java";
			fileContent1 =  "" +
					"module testM {\n" +
					"	exports p3;\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_0/src/p2";
			createFolder(filePath1);

			filePath1 = "/Completion15_0/src/p2/XYZ.java";
			fileContent1 = "" +
					"package p2;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  testM/p \n" +
					" *\n"+
					" */\n" +
					"public class XYZ  {\n" +
					"}";
			createFile(filePath1, fileContent1);
			String completeBehind = "testM/p";
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

			project1.close(); // sync
			project1.open(null);

			int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath1);
			unit.codeComplete(cursorLocation, requestor);

			int PACKAGE_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
			String expected = "p3 - testM/[PACKAGE_REF]{testM/p3, p3 - testM/, null, null, " + PACKAGE_RELEVANCE + "}" ;
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
		}
	}

	/**
	 * ContentAssist for Module Javadoc support is available for java 15 and above Modular projects
	 * @throws Exception
	 */
	public void test566060_011() throws Exception {
		IJavaProject project1 = createJavaProject("Completion15_1", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		IJavaProject project2 = createJavaProject("Completion15_2", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "15");
		try {
			project1.open(null);

			String filePath1 = "/Completion15_1/src/p1";
			createFolder(filePath1);

			filePath1 = "/Completion15_1/src/p1/ABC.java";
			String fileContent1 = "" +
					"package p1;\n\n" +
					"public class ABC  {\n" +
					"    public int fld;\n" +
					"	 public String getVal() { return null; }\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_1/src/p2";
			createFolder(filePath1);

			filePath1 = "/Completion15_1/src/p2/XYZ.java";
			fileContent1 = "" +
					"package p2;\n\n" +
					"public class XYZ  {\n" +
					"}";
			createFile(filePath1, fileContent1);

			filePath1 = "/Completion15_1/src/module-info.java";
			fileContent1 =  "" +
					"module testM.partial.mod {\n" +
					"	exports p1;\n" +
					"}";
			createFile(filePath1, fileContent1);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

			project2.open(null);

			String filePath2 = "/Completion15_2/src/p3";
			createFolder(filePath2);

			filePath2 = "/Completion15_2/src/module-info.java";
			String fileContent2 = "" +
					"module testQ {\n" +
					"	exports p3;\n\n" +
					"	requires testM.partial.mod;\n" +
					"}";
			createFile(filePath2, fileContent2);

			filePath2 = "/Completion15_2/src/p3/MNO.java";
			fileContent2 = "" +
					"package p3;\n\n" +
					"/**\n" +
					" * \n" +
					" * @see  testM.partial.mod/p1.ABC# \n" +
					" *\n"+
					" */\n" +
					"public class MNO  {\n" +
					"}";
			createFile(filePath2, fileContent2);
			String completeBehind = "testM.partial.mod/p1.ABC#";
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			addClasspathEntry(project2, JavaCore.newContainerEntry(project1.getPath()));

			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			int cursorLocation = fileContent2.lastIndexOf(completeBehind) + completeBehind.length();
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

			ICompilationUnit unit = getCompilationUnit(filePath2);
			unit.codeComplete(cursorLocation, requestor);

			int CONSTRUCTOR_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED;
			int DEFAULT_RELEVANCE = RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING +
					RelevanceConstants.R_NON_STATIC + RelevanceConstants.R_NON_RESTRICTED + RelevanceConstants.R_CASE;
			String expected= "ABC[METHOD_REF<CONSTRUCTOR>]{ABC(), Lp1.ABC;, ()V, ABC, "+CONSTRUCTOR_RELEVANCE+"}\n" +
							"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, "+DEFAULT_RELEVANCE+"}\n" +
							"equals[METHOD_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, "+DEFAULT_RELEVANCE+"}\n" +
							"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, "+DEFAULT_RELEVANCE+"}\n" +
							"fld[FIELD_REF]{fld, Lp1.ABC;, I, fld, "+DEFAULT_RELEVANCE+"}\n" +
							"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, "+DEFAULT_RELEVANCE+"}\n" +
							"getVal[METHOD_REF]{getVal(), Lp1.ABC;, ()Ljava.lang.String;, getVal, "+DEFAULT_RELEVANCE+"}\n" +
							"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, "+DEFAULT_RELEVANCE+"}\n" +
							"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, "+DEFAULT_RELEVANCE+"}\n" +
							"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, "+DEFAULT_RELEVANCE+"}\n" +
							"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, "+DEFAULT_RELEVANCE+"}\n" +
							"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, "+DEFAULT_RELEVANCE+"}\n" +
							"wait[METHOD_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, "+DEFAULT_RELEVANCE+"}\n" +
							"wait[METHOD_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, "+DEFAULT_RELEVANCE+"}";
			assertResults(expected,	requestor.getResults());
		} finally {
			deleteProject(project1);
			deleteProject(project2);
		}
	}
}
