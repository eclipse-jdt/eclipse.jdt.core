/*******************************************************************************
 * Copyright (c) 2016, 2024 IBM Corporation and others.
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

import junit.framework.Test;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;

public class CompletionTests9 extends AbstractJavaModelCompletionTests {

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_NAMES = new String[] {"testBug525203_001"};
//		TESTS_NAMES = new String[] {"test0001"};
}

public CompletionTests9(String name) {
	super(name);
}

@Override
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "9", true);
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "9", true);
	}
	super.setUpSuite();
}

@Override
protected void setUp() throws Exception {
	this.indexDisabledForTest = false;
	super.setUp();
}

public static Test suite() {
	return buildModelTestSuite(AbstractCompilerTest.F_9, CompletionTests9.class);
}


private void createTypePlus(String folder, String pack, String typeName, String plus, boolean isClass, boolean createFolder) throws CoreException {
	String filePath;
	String fileContent;
	fileContent = "package " + pack + ";\n" + "public " + (isClass ? "class " : "interface ") + typeName + ' ';
	if (plus != null) fileContent = fileContent + plus;
	fileContent = fileContent + " {}\n";
	pack = pack.replace('.', '/');
	if (createFolder)  createFolder(folder + pack);
	filePath = folder + pack + "/" + typeName + ".java";
	createFile(filePath, fileContent);
}

private void createType(String folder, String pack, String typeName) throws CoreException {
	createTypePlus(folder, pack, typeName, null, true /* isClass */, true /*createFolder */);
}

public void test486988_0001() throws Exception {
	IJavaProject project = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project.open(null);
		String projName = "/" + project.getElementName();
		String packageName = "/src/";
		String fullFilePath = projName + packageName + "module-info.java";
		String fileContent =  "module my.mod { }\n";
		createFile(fullFilePath, fileContent);
		String completeBehind = "{";
		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(fullFilePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "exports[KEYWORD]{exports, null, null, exports, 49}\n"
				+ "opens[KEYWORD]{opens, null, null, opens, 49}\n"
				+ "provides[KEYWORD]{provides, null, null, provides, 49}\n"
				+ "requires[KEYWORD]{requires, null, null, requires, 49}\n"
				+ "uses[KEYWORD]{uses, null, null, uses, 49}";
		assertResults(expected,	requestor.getResults());
	} finally {
		assertNotNull("Project Null", project);
		deleteProject(project);
	}
}

public void test486988_0002() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	String fileContent =  "module my.mod { }\n";

	this.workingCopies[0] = getWorkingCopy("/Completion/src/module-info.java", fileContent);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "{";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String expected = "exports[KEYWORD]{exports, null, null, exports, null, 49}\n"
			+ "opens[KEYWORD]{opens, null, null, opens, null, 49}\n"
			+ "provides[KEYWORD]{provides, null, null, provides, null, 49}\n"
			+ "requires[KEYWORD]{requires, null, null, requires, null, 49}\n"
			+ "uses[KEYWORD]{uses, null, null, uses, null, 49}";
	assertResults(expected,	requestor.getResults());
}

public void test486988_0003() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	String fileContent =  "module my.mod {e }\n";

	this.workingCopies[0] = getWorkingCopy("/Completion/src/module-info.java", fileContent);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "{e";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String expected = "exports[KEYWORD]{exports, null, null, exports, null, 49}";
	assertResults(expected,	requestor.getResults());
}

public void test486988_0004() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	String fileContent =  "module my.mod { p }\n";


	this.workingCopies[0] = getWorkingCopy("/Completion/src/module-info.java", fileContent);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "p";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	String oldValue = System.getProperty(AssistOptions.PROPERTY_SubstringMatch);
	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, "false");

	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, oldValue);
	String expected = "provides[KEYWORD]{provides, null, null, provides, null, 49}";
	assertResults(expected,	requestor.getResults());
}

// run locally until th
public void _test486988_0005() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	String fileContent =  "module my.mod { p }\n";

	this.workingCopies[0] = getWorkingCopy("/Completion/src/module-info.java", fileContent);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "p";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	String oldValue = System.getProperty(AssistOptions.PROPERTY_SubstringMatch);
	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, "true");

	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, oldValue);
	String expected = "exports[KEYWORD]{exports, null, null, exports, null, 19}\n"
			+ "provides[KEYWORD]{provides, null, null, provides, null, 49}";
	assertResults(expected,	requestor.getResults());
}

public void _test486988_0006() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	String fileContent =  "module my.mod { u }\n";

	this.workingCopies[0] = getWorkingCopy("/Completion/src/module-info.java", fileContent);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "u";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();

	String oldValue = System.getProperty(AssistOptions.PROPERTY_SubstringMatch);
	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, "true");

	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, oldValue);

	String expected = "requires[KEYWORD]{requires, null, null, requires, null, 19}\n"
			+ "uses[KEYWORD]{uses, null, null, uses, null, 49}";
	assertResults(expected, requestor.getResults());
}

public void test486988_0007() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	String fileContent =  "module my.mod {"
			+ "exports mypa"
			+ "}\n";

	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/module-info.java", fileContent);

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/mypack1/Y.java",
			"package pack1;\n" +
			"public class Y {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src/mypack2/Z.java",
			"package pack2;\n" +
			"public class Z {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src/mypack1.mypack2/Z.java",
			"package mypack1.mypack2;\n" +
			"public class Z {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "mypa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();

	String oldValue = System.getProperty(AssistOptions.PROPERTY_SubstringMatch);
	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, "false");

	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String expected = "mypack1[PACKAGE_REF]{mypack1, mypack1, null, null, null, 49}\n"
			+ "mypack2[PACKAGE_REF]{mypack2, mypack2, null, null, null, 49}\n"
			+ "mypackage[PACKAGE_REF]{mypackage, mypackage, null, null, null, 49}";
	assertResults(expected, requestor.getResults());
	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, oldValue);
}

public void test486988_0008() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	String fileContent =  "module my.mod {"
			+ "exports mypack1 t"
			+ "}\n";

	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/module-info.java", fileContent);

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/mypack1/Y.java",
			"package pack1;\n" +
			"public class Y {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src/mypack2/Z.java",
			"package pack2;\n" +
			"public class Z {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src/mypack1.mypack2/Z.java",
			"package mypack1.mypack2;\n" +
			"public class Z {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "mypack1 t";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();

	String oldValue = System.getProperty(AssistOptions.PROPERTY_SubstringMatch);
	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, "false");

	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String expected = "to[KEYWORD]{to, null, null, to, null, 49}";
	assertResults(expected, requestor.getResults());
	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, oldValue);

}
public void test486988_0009() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project2.open(null);
		String fileContent =  "module org.eclipse.foo {}\n";
		String filePath = "/Completion9_2/src/module-info.java";
		createFile(filePath, fileContent);

		project1.open(null);
		filePath = "/Completion9_1/src/module-info.java";
		fileContent =  "module com.greetings {requires o }\n";
		createFile(filePath, fileContent);
		String completeBehind = "requires o";
		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "[MODULE_REF]{org.eclipse.foo, org.eclipse.foo, null, null, 49}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}

public void test522604_0001() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project3 = createJavaProject("Completion9_3", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project3.open(null);
		String fileContent =  "module j.s.r {}\n";
		String filePath = "/Completion9_3/src/module-info.java";
		createFile(filePath, fileContent);

		project2.open(null);
		fileContent =  "module j.s {}\n";
		filePath = "/Completion9_2/src/module-info.java";
		createFile(filePath, fileContent);

		project1.open(null);
		filePath = "/Completion9_1/src/module-info.java";
		fileContent =  "module first {requires j.s. }\n";
		createFile(filePath, fileContent);
		String completeBehind = "requires j.s.";
		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "[MODULE_REF]{j.s.r, j.s.r, null, null, 49}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
		deleteProject(project3);
	}
}


public void test486988_0010() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		String filePath = "/Completion9_1/src/module-info.java";
		String fileContent =  "module com.greetings {"
				+ "requires org.eclipse.foo;\n"
				+ "}\n";
		createFile(filePath, fileContent);
		project1.close(); //sync
		project1.open(null);

		project2.open(null);
		String pack = "/Completion9_2/src/mypack1";
		createFolder(pack);
		filePath = pack + "/Y.java";
		fileContent = "package pack1;\n" +
		"public class Y {}\n";
		createFile(filePath, fileContent);

		fileContent =  "module org.eclipse.foo { "
				+ "exports mypack1 to com"
				+ "}\n";
		filePath = "/Completion9_2/src/module-info.java";
		createFile(filePath, fileContent);

		String completeBehind = "com";
		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "[MODULE_REF]{com.greetings, com.greetings, null, null, 49}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void test486988_0011() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack12", "X12");
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "pack";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+ "uses " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;\n"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "pack11[PACKAGE_REF]{pack11, pack11, null, null, 49}\n"
				+ "pack12[PACKAGE_REF]{pack12, pack12, null, null, 49}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void test486988_0012() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack12", "X12");
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "X1";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+ "uses " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "X11[TYPE_REF]{pack11.X11, pack11, Lpack11.X11;, null, 49}\n"
				+ "X12[TYPE_REF]{pack12.X12, pack12, Lpack12.X12;, null, 49}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void test486988_0013() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack12", "X12");
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "X1";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+ "provides " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "X11[TYPE_REF]{pack11.X11, pack11, Lpack11.X11;, null, 49}\n"
				+ "X12[TYPE_REF]{pack12.X12, pack12, Lpack12.X12;, null, 49}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void test486988_0014() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack12", "X12");
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "w";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+ "provides pack11.X11 " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "with[KEYWORD]{with, null, null, with, 49}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void test486988_0015() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack11.packinternal", "Z11");
		createTypePlus("/Completion9_1/src/", "pack11.packinternal", "Z12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		createType("/Completion9_1/src/", "pack12", "X12");
		createTypePlus("/Completion9_1/src/", "pack12", "Y12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "with p";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+ "provides pack22.I22 " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");
		createTypePlus("/Completion9_2/src/", "pack22", "I22", null, false /* isClass */, false /* createFolder */);

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;\n"
				+ "exports pack22 to first;\n"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		addClasspathEntry(project1, JavaCore.newContainerEntry(project2.getPath()));

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "Y12[TYPE_REF]{pack12.Y12, pack12, Lpack12.Y12;, null, 39}\n" +
				"Z12[TYPE_REF]{pack11.packinternal.Z12, pack11.packinternal, Lpack11.packinternal.Z12;, null, 39}\n" +
				"pack11[PACKAGE_REF]{pack11, pack11, null, null, 49}\n" +
				"pack11.packinternal[PACKAGE_REF]{pack11.packinternal, pack11.packinternal, null, null, 49}\n" +
				"pack12[PACKAGE_REF]{pack12, pack12, null, null, 49}"
				//+ "\nShow me the type Honey!!"
				;
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void testBug518618_001() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack11.packinternal", "Z11");
		createTypePlus("/Completion9_1/src/", "pack11.packinternal", "Z12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		createType("/Completion9_1/src/", "pack12", "X12");
		createTypePlus("/Completion9_1/src/", "pack12", "Y12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "with pack12.Y12;";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+ "provides pack22.I22 " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");
		createTypePlus("/Completion9_2/src/", "pack22", "I22", null, false /* isClass */, false /* createFolder */);

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;\n"
				+ "exports pack22 to first;\n"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "exports[KEYWORD]{exports, null, null, exports, 49}\n"
				+ "opens[KEYWORD]{opens, null, null, opens, 49}\n"
				+ "provides[KEYWORD]{provides, null, null, provides, 49}\n"
				+ "requires[KEYWORD]{requires, null, null, requires, 49}\n"
				+ "uses[KEYWORD]{uses, null, null, uses, 49}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
// test that types in a module can be correctly resolved including their super types when seen from the unnamed module during completion
public void testBug522164_src() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createTypePlus("/Completion9_1/src/", "p.priv", "PrivIfc", null, false, true);
		createFolder("/Completion9_1/src/p/a");
		createFile("/Completion9_1/src/p/a/Ifc.java",
					"package p.a;\n" +
					"public interface Ifc extends p.priv.PrivIfc {\n" +
					"	default void test() {};\n" +
					"}\n");
		createTypePlus("/Completion9_1/src/", "p.a", "Impl", "implements Ifc", true, false);
		createFile("/Completion9_1/src/module-info.java",
					"module mod.one { \n" +
					"	exports p.a;\n" +
					"	provides p.a.Ifc with p.a.Impl;\n" +
					"}");
		project1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

		IClasspathAttribute[] attributes = {
			JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")
		};
		addClasspathEntry(project2, JavaCore.newProjectEntry(new Path("/Completion9_1"), null, false, attributes, false));
		createFolder("/Completion9_2/src/x");
		String filePath = "/Completion9_2/src/x/X.java";
		String completeBehind = "ifc.te";
		String content =
					"package x;\n" +
					"public class X {\n" +
					"	void test(p.a.Ifc ifc) {\n" +
					"		" + completeBehind + "\n" +
					"	}\n" +
					"}\n";
		createFile(filePath, content);
		int cursorLocation = content.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		waitUntilIndexesReady();

		ICompilationUnit unit = getCompilationUnit("/Completion9_2/src/x/X.java");
		unit.codeComplete(cursorLocation, requestor);

		String expected = "test[METHOD_REF]{test(), Lp.a.Ifc;, ()V, test, 60}";
		assertResults(expected,	requestor.getResults());

	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
// test that types in a module can be correctly resolved including their super types when seen from the unnamed module during completion
public void testBug522164_jar() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
	try {
		project1.open(null);
		String[] jarSources = {
					"module-info.java",
					"module mod.one { \n" +
					"	exports p.a;\n" +
					"	provides p.a.Ifc with p.a.Impl;\n" +
					"}",
					"p/priv/PrivIfc.java",
					"package p.priv;\n" +
					"public interface PrivIfc {}\n",
					"p/a/Ifc.java",
					"package p.a;\n" +
					"public interface Ifc extends p.priv.PrivIfc {\n" +
					"	default void test() {};\n" +
					"}\n",
					"p/a/Impl.java",
					"package p.a;\n" +
					"public class Impl implements Ifc {}\n",
			};
		createFolder("/Completion9_1/lib");
		createJar(jarSources, project1.getProject().getLocation().append("lib").append("mod.one.jar").toOSString(), new String[0], "9");
		refresh(project1);

		IClasspathAttribute[] attributes = {
			JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")
		};
		addClasspathEntry(project1, JavaCore.newLibraryEntry(new Path("/Completion9_1/lib/mod.one.jar"), null, null, null, attributes, false));

		createFolder("/Completion9_1/src/x");
		String filePath = "/Completion9_1/src/x/X.java";
		String completeBehind = "ifc.te";
		String content =
					"package x;\n" +
					"public class X {\n" +
					"	void test(p.a.Ifc ifc) {\n" +
					"		" + completeBehind + "\n" +
					"	}\n" +
					"}\n";
		createFile(filePath, content);
		int cursorLocation = content.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		waitUntilIndexesReady();

		ICompilationUnit unit = getCompilationUnit("/Completion9_1/src/x/X.java");
		unit.codeComplete(cursorLocation, requestor);

		String expected = "test[METHOD_REF]{test(), Lp.a.Ifc;, ()V, test, 60}";
		assertResults(expected,	requestor.getResults());

	} finally {
		deleteProject(project1);
	}
}
public void test522613_001() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createTypePlus("/Completion9_1/src/", "pack11", "Driver", "", false /* isClass */, true /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC", "implements pack11.Driver", true /* isClass */, false /* createFolder */);
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "with C";
		String fileContent1 =  "module first {\n"
				+ "provides pack11.Driver " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project1.close(); // sync
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "CCC[TYPE_REF]{pack11.CCC, pack11, Lpack11.CCC;, null, 49}"
			;
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}
public void test540591() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createTypePlus("/Completion9_1/src/", "pack11", "IService", "", false /* isClass */, true /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC", "implements pack11.IService", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "AbstractService", "implements pack11.IService", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC2", "extends pack11.AbstractService", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC3", "extends pack11.AbstractService", true /* isClass */, false /* createFolder */);

		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "CC";
		String fileContent1 =  "module first {\n"
				+ "provides pack11.IService with " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project1.close(); // sync
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "CCC[TYPE_REF]{pack11.CCC, pack11, Lpack11.CCC;, null, 49}\n" +
				"CCC2[TYPE_REF]{pack11.CCC2, pack11, Lpack11.CCC2;, null, 49}\n" +
				"CCC3[TYPE_REF]{pack11.CCC3, pack11, Lpack11.CCC3;, null, 49}"
			;
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}


public void test540591_mismatchedCase() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createTypePlus("/Completion9_1/src/", "pack11", "IService", "", false /* isClass */, true /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC", "implements pack11.IService", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "AbstractService", "implements pack11.IService", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC2", "extends pack11.AbstractService", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC3", "extends pack11.AbstractService", true /* isClass */, false /* createFolder */);

		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "cc";
		String fileContent1 =  "module first {\n"
				+ "provides pack11.IService with " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project1.close(); // sync
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "CCC[TYPE_REF]{pack11.CCC, pack11, Lpack11.CCC;, null, 39}\n" +
				"CCC2[TYPE_REF]{pack11.CCC2, pack11, Lpack11.CCC2;, null, 39}\n" +
				"CCC3[TYPE_REF]{pack11.CCC3, pack11, Lpack11.CCC3;, null, 39}"
			;
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}

public void test540591_withoutPrefix() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createTypePlus("/Completion9_1/src/", "pack11", "IService", "", false /* isClass */, true /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC", "implements pack11.IService", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "AbstractService", "implements pack11.IService", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC2", "extends pack11.AbstractService", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "pack11", "CCC3", "extends pack11.AbstractService", true /* isClass */, false /* createFolder */);

		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "with ";
		String fileContent1 =  "module first {\n"
				+ "provides pack11.IService " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project1.close(); // sync
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "AbstractService[TYPE_REF]{pack11.AbstractService, pack11, Lpack11.AbstractService;, null, 39}\n" +
				"CCC[TYPE_REF]{pack11.CCC, pack11, Lpack11.CCC;, null, 39}\n" +
				"CCC2[TYPE_REF]{pack11.CCC2, pack11, Lpack11.CCC2;, null, 39}\n" +
				"CCC3[TYPE_REF]{pack11.CCC3, pack11, Lpack11.CCC3;, null, 39}\n"+
				"pack11[PACKAGE_REF]{pack11, pack11, null, null, 39}"
			;


		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}

public void test527099_001() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack11.packinternal", "Z11");
		createTypePlus("/Completion9_1/src/", "pack11.packinternal", "Z12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		createType("/Completion9_1/src/", "pack12", "X12");
		createTypePlus("/Completion9_1/src/", "pack12", "Y12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "exports ";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+ completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");
		createTypePlus("/Completion9_2/src/", "pack22", "I22", null, false /* isClass */, false /* createFolder */);

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;\n"
				+ "exports pack22 to first;\n"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "pack11[PACKAGE_REF]{pack11, pack11, null, null, 39}\n"
				+ "pack11.packinternal[PACKAGE_REF]{pack11.packinternal, pack11.packinternal, null, null, 39}\n"
				+ "pack12[PACKAGE_REF]{pack12, pack12, null, null, 39}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}

public void test527873_001() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack11.packinternal", "Z11");
		createTypePlus("/Completion9_1/src/", "pack11.packinternal", "Z12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		createType("/Completion9_1/src/", "pack12", "X12");
		createTypePlus("/Completion9_1/src/", "pack12", "Y12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "with ";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+ "provides pack22.I22 " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");
		createTypePlus("/Completion9_2/src/", "pack22", "I22", null, false /* isClass */, false /* createFolder */);

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;\n"
				+ "exports pack22 to first;\n"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		addClasspathEntry(project1, JavaCore.newContainerEntry(project2.getPath()));

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected =
				"Y12[TYPE_REF]{pack12.Y12, pack12, Lpack12.Y12;, null, 39}\n" +
				"Z12[TYPE_REF]{pack11.packinternal.Z12, pack11.packinternal, Lpack11.packinternal.Z12;, null, 39}\n" +
				"pack11[PACKAGE_REF]{pack11, pack11, null, null, 39}\n" +
				"pack11.packinternal[PACKAGE_REF]{pack11.packinternal, pack11.packinternal, null, null, 39}\n" +
				"pack12[PACKAGE_REF]{pack12, pack12, null, null, 39}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void test527873_002() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack11.packinternal", "Z11");
		createTypePlus("/Completion9_1/src/", "pack11.packinternal", "Z12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		createType("/Completion9_1/src/", "pack12", "X12");
		createTypePlus("/Completion9_1/src/", "pack12", "Y12", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		createTypePlus("/Completion9_1/src/", "", "X", "implements pack22.I22", true /* isClass */, false /* createFolder */);
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "with ";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+ "provides pack22.I22 " + completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");
		createTypePlus("/Completion9_2/src/", "pack22", "I22", null, false /* isClass */, false /* createFolder */);

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;\n"
				+ "exports pack22 to first;\n"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		addClasspathEntry(project1, JavaCore.newContainerEntry(project2.getPath()));

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		String expected =
			"Y12[TYPE_REF]{pack12.Y12, pack12, Lpack12.Y12;, null, 39}\n" +
			"Z12[TYPE_REF]{pack11.packinternal.Z12, pack11.packinternal, Lpack11.packinternal.Z12;, null, 39}\n" +
			"pack11[PACKAGE_REF]{pack11, pack11, null, null, 39}\n" +
			"pack11.packinternal[PACKAGE_REF]{pack11.packinternal, pack11.packinternal, null, null, 39}\n" +
			"pack12[PACKAGE_REF]{pack12, pack12, null, null, 39}\n" +
			"X[TYPE_REF]{X, , LX;, null, 42}"
		;
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void testBug525203_001() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
	try  {
		project1.open(null);
		IClasspathAttribute[] attributes = {};
		addClasspathEntry(project1, JavaCore.newLibraryEntry(new Path("/Completion/test.second.jar"), null, null, null, attributes, false));
		addClasspathEntry(project1, JavaCore.newLibraryEntry(new Path("/Completion/test.third.jar"), null, null, null, attributes, false));
	    addClasspathEntry(project1, JavaCore.newLibraryEntry(new Path("/Completion/test.some.api.jar"), null, null, null, attributes, false));

		createFolder("/Completion9_1/src/x");
		String content =  "module my.mod { \n" +
				"requires test.\n" +
		"}\n";
		String filePath = "/Completion9_1/src/module-info.java";
		String completeBehind = "requires test.";
		createFile(filePath, content);
		int cursorLocation = content.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		waitUntilIndexesReady();

		ICompilationUnit unit = getCompilationUnit("/Completion9_1/src/module-info.java");
		unit.codeComplete(cursorLocation, requestor);

		String expected = "[MODULE_REF]{test.second, test.second, null, null, 49}\n" +
				"[MODULE_REF]{test.some.core.api, test.some.core.api, null, null, 49}\n" +
				"[MODULE_REF]{test.third.from.manifest, test.third.from.manifest, null, null, 49}";
		assertResults(expected,	requestor.getResults());

	} finally {
		deleteProject(project1);
	}
}
public void testBug529123_001() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		String filePath = "/Completion9_1/src/module-info.java";
		String fileContent =  "module Com";
		createFile(filePath, fileContent);
		String completeBehind = "Com";
		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "Completion9_1[MODULE_DECLARATION]{Completion9_1, Completion9_1, null, Completion9_1, 31}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}
public void testBug529123_002() throws Exception {
	String pName = "Completion9-1"; // with a -,
	IJavaProject project1 = createJavaProject(pName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		String filePath = "/" + pName +"/src/module-info.java";
		String fileContent =  "module Com";
		createFile(filePath, fileContent);
		String completeBehind = "Com";
		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}
public void testBug529123_003() throws Exception {
	String pName = "529123"; // a number - invalid module name but a valid project name
	IJavaProject project1 = createJavaProject(pName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		String filePath = "/" + pName +"/src/module-info.java";
		String fileContent =  "module Com";
		createFile(filePath, fileContent);
		String completeBehind = "Com";
		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}
public void testBug529123_004() throws Exception {
	String pName = "module.name.test"; // with dots
	IJavaProject project1 = createJavaProject(pName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		String filePath = "/" + pName +"/src/module-info.java";
		String fileContent =  "module Com";
		createFile(filePath, fileContent);
		String completeBehind = "Com";
		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}
public void testBug529123_005() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		String filePath = "/Completion9_1/src/module-info.java";
		String fileContent =  "module Com";
		createFile(filePath, fileContent);
		String completeBehind = "Com";
		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "Completion9_1[MODULE_DECLARATION]{Completion9_1, Completion9_1, null, Completion9_1, 31}";
		String[] actual = requestor.getStringsResult();
		assertTrue("Null result", actual != null);
		assertTrue("Incorrect number of elements", actual.length == 1);
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}
public void testBug528948_001() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack12", "X12");
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "uses ";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+  completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		waitUntilIndexesReady();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		CompletionProposal[] proposals = requestor.getProposals();
		assertTrue(proposals != null);
		int count = 0;
		for (CompletionProposal proposal : proposals) {
			if (proposal == null) break;
			++count;
			int start = proposal.getReplaceStart();
			int end = proposal.getReplaceEnd();
			assertTrue(start > 0);
			assertTrue(end > 0);
		}
		assertEquals("Incorrect Number of Proposals", 2, count);
		String expected = "X11[TYPE_REF]{pack11.X11, pack11, Lpack11.X11;, null, 39}\n" +
				"X12[TYPE_REF]{pack12.X12, pack12, Lpack12.X12;, null, 39}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void testBug528948_002() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createType("/Completion9_1/src/", "pack12", "X12");
		String filePath1 = "/Completion9_1/src/module-info.java";
		String completeBehind = "provides ";
		String fileContent1 =  "module first {\n"
				+ "requires second;\n"
				+  completeBehind
				+ "}\n";
		createFile(filePath1, fileContent1);

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack22", "X22");

		String fileContent2 =  "module second { "
				+ "exports pack21 to first;"
				+ "}\n";
		String filePath2 = "/Completion9_2/src/module-info.java";
		createFile(filePath2, fileContent2);

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent1.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		waitUntilIndexesReady();

		ICompilationUnit unit = getCompilationUnit(filePath1);
		unit.codeComplete(cursorLocation, requestor);

		CompletionProposal[] proposals = requestor.getProposals();
		assertTrue(proposals != null);
		int count = 0;
		for (CompletionProposal proposal : proposals) {
			if (proposal == null) break;
			++count;
			int start = proposal.getReplaceStart();
			int end = proposal.getReplaceEnd();
			assertTrue(start > 0);
			assertTrue(end > 0);
		}
		assertEquals("Incorrect Number of Proposals", 2, count);
		String expected = "X11[TYPE_REF]{pack11.X11, pack11, Lpack11.X11;, null, 39}\n" +
				"X12[TYPE_REF]{pack12.X12, pack12, Lpack12.X12;, null, 39}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
	}
}
public void testBug530142() throws Exception {
	ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"Completion9_1", "/Completion/test.second.jar,/Completion/test.third.jar,/Completion/test.some.api.jar"}));
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL19_LIB", "org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "bin", "9");
	try  {
		project1.open(null);

		createFolder("/Completion9_1/src/x");
		String content =  "module my.mod { \n" +
				"requires test.\n" +
		"}\n";
		String filePath = "/Completion9_1/src/module-info.java";
		String completeBehind = "requires test.";
		createFile(filePath, content);
		int cursorLocation = content.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		waitUntilIndexesReady();

		ICompilationUnit unit = getCompilationUnit("/Completion9_1/src/module-info.java");
		unit.codeComplete(cursorLocation, requestor);

		String expected = "[MODULE_REF]{test.second, test.second, null, null, 49}\n" +
				"[MODULE_REF]{test.some.core.api, test.some.core.api, null, null, 49}\n" +
				"[MODULE_REF]{test.third.from.manifest, test.third.from.manifest, null, null, 49}";
		assertResults(expected,	requestor.getResults());

	} finally {
		deleteProject(project1);
	}
}
public void testBug517417_001() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL9_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL9_LIB"}, "bin", "9");
	IJavaProject project3 = createJavaProject("Completion9_3", new String[] {"src"}, new String[] {"JCL9_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createFile("/Completion9_1/src/module-info.java",
				"module first {\n" +
				"	requires second;\n" +
				"}\n");
		String fileContent =
				"package pack0;\n" +
				"import pac\n" +
				"public class Main {\n" +
				"}\n";
		String completeBehind = "import pac";
		createFolder("/Completion9_1/src/pack0");
		String filePath = "/Completion9_1/src/pack0/Main.java";
		createFile(filePath, fileContent);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack2internal", "X22");

		createFile("/Completion9_2/src/module-info.java",
				"module second { \n" +
				"	requires transitive third;\n" +
				"	exports pack21 to first;\n" +
				"	exports pack2internal to my.test.mod;\n" +
				"}\n");
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project3.open(null);
		createType("/Completion9_3/src/", "pack31", "X31");

		createFile("/Completion9_3/src/module-info.java",
				"module third { " +
				"	exports pack31;\n" +
				"}\n");
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "pack0[PACKAGE_REF]{pack0.*;, pack0, null, null, 49}\n" + // local
				"pack11[PACKAGE_REF]{pack11.*;, pack11, null, null, 49}\n" + // local
				"pack21[PACKAGE_REF]{pack21.*;, pack21, null, null, 49}\n" + // exported
				"pack31[PACKAGE_REF]{pack31.*;, pack31, null, null, 49}"; // exported in transitively required third
				// package pack2internal is exported only to another module
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
		deleteProject(project3);
	}
}

// testing only packages from transitive requires modules available for completion
public void testBug517417_002() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL9_LIB"}, "bin", "9");
	IJavaProject project2 = createJavaProject("Completion9_2", new String[] {"src"}, new String[] {"JCL9_LIB"}, "bin", "9");
	IJavaProject project3 = createJavaProject("Completion9_3", new String[] {"src"}, new String[] {"JCL9_LIB"}, "bin", "9");
	IJavaProject project4 = createJavaProject("Completion9_4", new String[] {"src"}, new String[] {"JCL9_LIB"}, "bin", "9");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createFile("/Completion9_1/src/module-info.java",
				"module first {\n" +
				"	requires second;\n" +
				"}\n");
		String fileContent =
				"package pack0;\n" +
				"import pac\n" +
				"public class Main {\n" +
				"}\n";
		String completeBehind = "import pac";
		createFolder("/Completion9_1/src/pack0");
		String filePath = "/Completion9_1/src/pack0/Main.java";
		createFile(filePath, fileContent);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project2.open(null);
		createType("/Completion9_2/src/", "pack21", "X21");
		createType("/Completion9_2/src/", "pack2internal", "X22");

		createFile("/Completion9_2/src/module-info.java",
				"module second { \n" +
				"	requires transitive third;\n" +
				"	requires four;\n" +
				"	exports pack21 to first;\n" +
				"	exports pack2internal to my.test.mod;\n" +
				"}\n");
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project3.open(null);
		createType("/Completion9_3/src/", "pack31", "X31");

		createFile("/Completion9_3/src/module-info.java",
				"module third { " +
				"	exports pack31;\n" +
				"}\n");
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project4.open(null);
		createType("/Completion9_4/src/", "pack41", "X41");

		createFile("/Completion9_4/src/module-info.java",
				"module four { " +
				"	exports pack41;\n" +
				"}\n");
		addClasspathEntry(project4, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project1.close(); // sync
		project2.close();
		project3.close();
		project4.close();
		project4.open(null);
		project3.open(null);
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "pack0[PACKAGE_REF]{pack0.*;, pack0, null, null, 49}\n" + // local
				"pack11[PACKAGE_REF]{pack11.*;, pack11, null, null, 49}\n" + // local
				"pack21[PACKAGE_REF]{pack21.*;, pack21, null, null, 49}\n" + // exported
				"pack31[PACKAGE_REF]{pack31.*;, pack31, null, null, 49}"; // exported in transitively required third
				// package pack2internal is exported only to another module
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
		deleteProject(project3);
		deleteProject(project4);
	}
}
public void testBug530911() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL19_LIB", "org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "bin", "9");
	try  {
		project1.open(null);

		createFile("/Completion9_1/src/module-info.java",
				"module first {}\n");
		String content =  "package annotation;\n" +
				"\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target()\n" +
				"public @interface NonNull {\n" +
				"}\n" +
				"";
		createFolder("/Completion9_1/src/p");
		String filePath = "/Completion9_1/src/p/NonNull.java";
		String completeBehind="@Target(";
		createFile(filePath, content);
		int cursorLocation = content.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		waitUntilIndexesReady();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "NonNull[TYPE_REF]{NonNull, p, Lp.NonNull;, null, 52}\n" +
				"value[ANNOTATION_ATTRIBUTE_REF]{value = , Ljava.lang.annotation.Target;, [Ljava.lang.annotation.ElementType;, value, 52}\n" +
				"ANNOTATION_TYPE[FIELD_REF]{ElementType.ANNOTATION_TYPE, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, ANNOTATION_TYPE, 104}\n" +
				"CONSTRUCTOR[FIELD_REF]{ElementType.CONSTRUCTOR, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, CONSTRUCTOR, 104}\n" +
				"ElementType[TYPE_REF]{ElementType, java.lang.annotation, Ljava.lang.annotation.ElementType;, null, 104}\n" +
				"FIELD[FIELD_REF]{ElementType.FIELD, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, FIELD, 104}\n" +
				"LOCAL_VARIABLE[FIELD_REF]{ElementType.LOCAL_VARIABLE, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, LOCAL_VARIABLE, 104}\n" +
				"METHOD[FIELD_REF]{ElementType.METHOD, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, METHOD, 104}\n" +
				"MODULE[FIELD_REF]{ElementType.MODULE, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, MODULE, 104}\n" +
				"PACKAGE[FIELD_REF]{ElementType.PACKAGE, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, PACKAGE, 104}\n" +
				"PARAMETER[FIELD_REF]{ElementType.PARAMETER, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, PARAMETER, 104}\n" +
				"TYPE[FIELD_REF]{ElementType.TYPE, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, TYPE, 104}\n" +
				"TYPE_PARAMETER[FIELD_REF]{ElementType.TYPE_PARAMETER, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, TYPE_PARAMETER, 104}\n" +
				"TYPE_USE[FIELD_REF]{ElementType.TYPE_USE, Ljava.lang.annotation.ElementType;, Ljava.lang.annotation.ElementType;, TYPE_USE, 104}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}
public void testBug548888() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL19_LIB", "org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "bin", "9");
	try  {
		project1.open(null);

		String content =
				"/**\n" +
				"  * @see List\n" +
				"  */\n" +
				"module firstmod {\n" +
				"}\n";
		String filePath = "/Completion9_1/src/module-info.java";
		String completeBehind="List";
		createFile(filePath, content);
		int cursorLocation = content.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		waitUntilIndexesReady();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "List[TYPE_REF]{java.util.List, java.util, Ljava.util.List;, null, 55}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
	}
}
public void testBug560606() throws CoreException {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL19_LIB", "org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "bin", "9");
	try  {
		project1.open(null);
		createFile("/Completion9_1/src/module-info.java",
				"module first {}\n");

		CompletionResult result = complete(
	            "/Completion9_1/src/bug560606/Test.java",
	            "package bug560606;\n" +
	            "import java.util.Coll;\n" +
	            "public class Test {\n" +
	            "}\n",
	            "import java.util.Coll");

	    assertResults(
	            "Collection[TYPE_REF]{Collection, java.util, Ljava.util.Collection;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
	            result.proposals);
	} finally {
		deleteProject(project1);
	}
}
public void testBug573632d() throws Exception {
	IJavaProject project1 = createJavaProject("Completion9_1", new String[] {"src"}, new String[] {"JCL19_LIB", "org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "bin", "9");
	try {
		project1.open(null);
		CompletionResult result = complete(
			"/Completion9_1/src/Foo.java",
			"package test;\n" +
			"import java.io.InputStream;\n" +
			"public class Foo {\n" +
			"	Foo f;\n" +
			"	public void foo() {\n" +
			"		InputStream is = new InputStream(null);\n" +
			"		try (is) {\n" +
			"			f.\n" +
			"			f = null;\n" +
			"		}\n" +
			"	};\n" +
			"}\n",
			"f.");
		assertResults(
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n" +
			"f[FIELD_REF]{f, LFoo;, LFoo;, f, null, 60}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}\n" +
			"foo[METHOD_REF]{foo(), LFoo;, ()V, foo, null, 60}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}",
			result.proposals);
	} finally {
		deleteProject(project1);
	}
}
public void testGH2169() throws CoreException {
	IJavaProject project1 = createJava9Project("Completion9_1");
	IJavaProject project2 = createJava9Project("Completion9_2");
	IJavaProject project3 = createJava9Project("Completion9_3");
	try {
		project1.open(null);
		createType("/Completion9_1/src/", "pack11", "X11");
		createFile("/Completion9_1/src/module-info.java",
				"module first {\n" +
				"	requires transitive second;\n" +
				"	requires transitive third;\n" +
				"	requires transitive java.base;\n" +
				"}\n");
		String fileContent =
				"package pack0;\n" +
				"import java.util\n" +
				"public class Main {\n" +
				"}\n";
		String completeBehind = "import java.util";
		createFolder("/Completion9_1/src/pack0");
		String filePath = "/Completion9_1/src/pack0/Main.java";
		createFile(filePath, fileContent);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project2.open(null);
		createType("/Completion9_2/src/", "java.utilities", "X21");

		createFile("/Completion9_2/src/module-info.java",
				"module second { \n" +
				"	requires transitive java.base;\n" +
				"	requires transitive third;\n" +
				"	exports java.utilities;\n" +
				"}\n");
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project3.open(null);
		createType("/Completion9_3/src/", "java/utils/", "X31");

		createFile("/Completion9_3/src/module-info.java",
				"module third {\n" +
				"	requires transitive first;\n" + // yep, cycle here
				"	exports java.utils;\n" +
				"}\n");
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		int cursorLocation = fileContent.lastIndexOf(completeBehind) + completeBehind.length();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		ICompilationUnit unit = getCompilationUnit(filePath);
		unit.codeComplete(cursorLocation, requestor);

		String expected = "java.util.concurrent[PACKAGE_REF]{java.util.concurrent.*;, java.util.concurrent, null, null, 49}\n" +
				"java.util.concurrent.atomic[PACKAGE_REF]{java.util.concurrent.atomic.*;, java.util.concurrent.atomic, null, null, 49}\n" +
				"java.util.concurrent.locks[PACKAGE_REF]{java.util.concurrent.locks.*;, java.util.concurrent.locks, null, null, 49}\n" +
				"java.util.function[PACKAGE_REF]{java.util.function.*;, java.util.function, null, null, 49}\n" +
				"java.util.jar[PACKAGE_REF]{java.util.jar.*;, java.util.jar, null, null, 49}\n" +
				"java.util.logging[PACKAGE_REF]{java.util.logging.*;, java.util.logging, null, null, 49}\n" +
				"java.util.prefs[PACKAGE_REF]{java.util.prefs.*;, java.util.prefs, null, null, 49}\n" +
				"java.util.random[PACKAGE_REF]{java.util.random.*;, java.util.random, null, null, 49}\n" +
				"java.util.regex[PACKAGE_REF]{java.util.regex.*;, java.util.regex, null, null, 49}\n" +
				"java.util.spi[PACKAGE_REF]{java.util.spi.*;, java.util.spi, null, null, 49}\n" +
				"java.util.stream[PACKAGE_REF]{java.util.stream.*;, java.util.stream, null, null, 49}\n" +
				"java.util.zip[PACKAGE_REF]{java.util.zip.*;, java.util.zip, null, null, 49}\n" +
				"java.utilities[PACKAGE_REF]{java.utilities.*;, java.utilities, null, null, 49}\n" +
				"java.utils[PACKAGE_REF]{java.utils.*;, java.utils, null, null, 49}\n" +
				"java.util[PACKAGE_REF]{java.util.*;, java.util, null, null, 53}";
		assertResults(expected,	requestor.getResults());
	} finally {
		deleteProject(project1);
		deleteProject(project2);
		deleteProject(project3);
	}
}
}