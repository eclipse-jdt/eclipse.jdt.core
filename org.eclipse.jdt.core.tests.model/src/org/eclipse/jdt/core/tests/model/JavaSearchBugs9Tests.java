/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import junit.framework.Test;

/**
 * Non-regression tests for bugs fixed in Java Search engine.
 */
public class JavaSearchBugs9Tests extends AbstractJavaSearchTests {

	static {
//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	TESTS_NAMES = new String[] {"testBug521221_001"};
}

public JavaSearchBugs9Tests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchBugs9Tests.class, BYTECODE_DECLARATION_ORDER);
}
class TestCollector extends JavaSearchResultCollector {
	public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
		super.acceptSearchMatch(searchMatch);
	}
}
class ReferenceCollector extends JavaSearchResultCollector {
	protected void writeLine() throws CoreException {
		super.writeLine();
		ReferenceMatch refMatch = (ReferenceMatch) this.match;
		IJavaElement localElement = refMatch.getLocalElement();
		if (localElement != null) {
			this.line.append("+[");
			if (localElement.getElementType() == IJavaElement.ANNOTATION) {
				this.line.append('@');
				this.line.append(localElement.getElementName());
				this.line.append(" on ");
				this.line.append(localElement.getParent().getElementName());
			} else {
				this.line.append(localElement.getElementName());
			}
			this.line.append(']');
		}
	}

}
class TypeReferenceCollector extends ReferenceCollector {
	protected void writeLine() throws CoreException {
		super.writeLine();
		TypeReferenceMatch typeRefMatch = (TypeReferenceMatch) this.match;
		IJavaElement[] others = typeRefMatch.getOtherElements();
		int length = others==null ? 0 : others.length;
		if (length > 0) {
			this.line.append("+[");
			for (int i=0; i<length; i++) {
				IJavaElement other = others[i];
				if (i>0) this.line.append(',');
				if (other.getElementType() == IJavaElement.ANNOTATION) {
					this.line.append('@');
					this.line.append(other.getElementName());
					this.line.append(" on ");
					this.line.append(other.getParent().getElementName());
				} else {
					this.line.append(other.getElementName());
				}
			}
			this.line.append(']');
		}
	}
}

IJavaSearchScope getJavaSearchScope() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
}
IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
	if (packageName == null) return getJavaSearchScope();
	return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
}
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	if (this.wcOwner == null) {
		this.wcOwner = new WorkingCopyOwner() {};
	}
	return getWorkingCopy(path, source, this.wcOwner);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#setUpSuite()
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();
	JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "9");
}
public void tearDownSuite() throws Exception {
	deleteProject("JavaSearchBugs");
	super.tearDownSuite();
}
protected void setUp () throws Exception {
	super.setUp();
	this.resultCollector = new TestCollector();
	this.resultCollector.showAccuracy(true);
}

public void _testBug499338_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"    	Z z1 = new Z();\n" +
			"        try (z1;  z1) {\n" +
			"        }  \n" +
			"    }  \n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"Y CLOSE\");\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class Z implements AutoCloseable {\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"Z CLOSE\");\n" +
			"	}\n" +
			"}\n"
			);
	String str = this.workingCopies[0].getSource();
	String selection = "z1";
	int start = str.indexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	ILocalVariable local = (ILocalVariable) elements[0];
	search(local, REFERENCES, EXACT_RULE);
	assertSearchResults(	
			"src/X.java void X.main(String[]) [z1] EXACT_MATCH\n" + 
			"src/X.java void X.main(String[]) [z1] EXACT_MATCH");	
}

public void testBug501162_001() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    exports pack1 to second;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack1");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
			"src/module-info.java first [pack1] EXACT_MATCH\n" + 
			"src/pack1 pack1 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_002() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    exports pack1 to second;\n" +
			"    exports pack1 to third;\n" +
			"    opens pack1 to fourth;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack1");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
			"src/module-info.java first [pack1] EXACT_MATCH\n" + 
			"src/module-info.java first [pack1] EXACT_MATCH\n" + 
			"src/module-info.java first [pack1] EXACT_MATCH\n" + 
			"src/pack1 pack1 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_003() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 implements pack22.I22{}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    exports pack22 to first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack22");
		createFile("/second/src/pack22/I22.java",
				"package pack22;\n" +
				"public interface I22 {}\n");

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		IPackageFragment pkg = getPackageFragment("second", "src", "pack22");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			REFERENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
			"src/module-info.java first [pack22] EXACT_MATCH\n" + 
			"src/pack1/X11.java pack1.X11 [pack22] EXACT_MATCH\n" + 
			"src/module-info.java second [pack22] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_005() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/module-info.java",
			"module first {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n"
			);
	String str = this.workingCopies[0].getSource();
	String selection = "first";
	int start = str.indexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	IModuleDescription module = (IModuleDescription) elements[0];
	search(module, ALL_OCCURRENCES, EXACT_RULE);
	assertSearchResults(	
			"src/module-info.java first [first] EXACT_MATCH");
}
public void testBug501162_006() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 implements pack22.I22{}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    exports pack22 to first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack1");
		createFile("/second/src/pack1/I22.java",
				"package pack22;\n" +
				"public interface I22 {}\n");

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("first", IJavaSearchConstants.MODULE, REFERENCES, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/module-info.java second [first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_007() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first.test.org {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 implements pack22.I22{}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    exports pack22 to first.test.org;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack1");
		createFile("/second/src/pack1/I22.java",
				"package pack22;\n" +
				"public interface I22 {}\n");

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("first.test.org", IJavaSearchConstants.MODULE, REFERENCES, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);

		assertSearchResults(
			"src/module-info.java second [first.test.org] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_008() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 implements pack22.I22{}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    exports pack22 to first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack1");
		createFile("/second/src/pack1/I22.java",
				"package pack22;\n" +
				"public interface I22 {}\n");

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("second", IJavaSearchConstants.MODULE, REFERENCES, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		search(pattern, scope, this.resultCollector);

		assertSearchResults(
			"src/module-info.java first [second] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug501162_009() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    exports pack1;\n" +
			"    exports pack2;\n" +
			"    opens pack1 to fourth;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");
		createFolder("/JavaSearchBugs9/src/pack2");
		createFile("/JavaSearchBugs9/src/pack2/X21.java",
				"package pack2;\n" +
				"public class X21 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack2");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
			"src/module-info.java first [pack2] EXACT_MATCH\n" + 
			"src/pack2 pack2 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_010() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 implements pack22.I22{}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    exports pack22 to first, zero;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack1");
		createFile("/second/src/pack1/I22.java",
				"package pack22;\n" +
				"public interface I22 {}\n");

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("first", IJavaSearchConstants.MODULE, REFERENCES, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/module-info.java second [first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_011() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 implements pack22.I22{}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    opens pack22 to first, zero;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack1");
		createFile("/second/src/pack1/I22.java",
				"package pack22;\n" +
				"public interface I22 {}\n");

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("first", IJavaSearchConstants.MODULE, REFERENCES, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/module-info.java second [first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_012() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    exports pack1 to one;\n" +
			"    exports pack2;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");
		createFolder("/JavaSearchBugs9/src/pack2");
		createFile("/JavaSearchBugs9/src/pack2/X21.java",
				"package pack2;\n" +
				"public class X21 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack1");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
				"src/module-info.java first [pack1] EXACT_MATCH\n" + 
				"src/pack1 pack1 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_013() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    opens pack1 to one;\n" +
			"    opens pack2;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");
		createFolder("/JavaSearchBugs9/src/pack2");
		createFile("/JavaSearchBugs9/src/pack2/X21.java",
				"package pack2;\n" +
				"public class X21 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack1");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
				"src/module-info.java first [pack1] EXACT_MATCH\n" + 
				"src/pack1 pack1 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_014() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    opens pack1;\n" +
			"    opens pack2;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");
		createFolder("/JavaSearchBugs9/src/pack2");
		createFile("/JavaSearchBugs9/src/pack2/X21.java",
				"package pack2;\n" +
				"public class X21 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack2");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
				"src/module-info.java first [pack2] EXACT_MATCH\n" + 
				"src/pack2 pack2 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_015() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    opens pack1;\n" +
			"    exports pack2;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");
		createFolder("/JavaSearchBugs9/src/pack2");
		createFile("/JavaSearchBugs9/src/pack2/X21.java",
				"package pack2;\n" +
				"public class X21 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack2");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
				"src/module-info.java first [pack2] EXACT_MATCH\n" + 
				"src/pack2 pack2 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_016() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    opens pack1 to one;\n" +
			"    exports pack2;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");
		createFolder("/JavaSearchBugs9/src/pack2");
		createFile("/JavaSearchBugs9/src/pack2/X21.java",
				"package pack2;\n" +
				"public class X21 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack2");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
				"src/module-info.java first [pack2] EXACT_MATCH\n" + 
				"src/pack2 pack2 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_017() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    exports pack1 to one;\n" +
			"    opens pack2;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");
		createFolder("/JavaSearchBugs9/src/pack2");
		createFile("/JavaSearchBugs9/src/pack2/X21.java",
				"package pack2;\n" +
				"public class X21 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack2");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
				"src/module-info.java first [pack2] EXACT_MATCH\n" + 
				"src/pack2 pack2 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_018() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    opens pack2;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 {}\n");
		createFolder("/JavaSearchBugs9/src/pack2");
		createFile("/JavaSearchBugs9/src/pack2/X21.java",
				"package pack2;\n" +
				"public class X21 {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    requires first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		
		IPackageFragment pkg = getPackageFragment("JavaSearchBugs9", "src", "pack2");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9")});

		search(
			pkg,
			ALL_OCCURRENCES,
			scope,
			this.resultCollector);
		assertSearchResults(
				"src/module-info.java first [pack2] EXACT_MATCH\n" + 
				"src/pack2 pack2 EXACT_MATCH",
			this.resultCollector);

	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug501162_019() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("pack.one",
				IJavaSearchConstants.PACKAGE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/pack1/X.java [pack.one] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_020() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("pack.two",
				IJavaSearchConstants.PACKAGE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/pack1/X.java [pack.two] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_021() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("pack.three",
				IJavaSearchConstants.PACKAGE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero501162.jar zero [No source] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_022() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("java.base",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_023() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("zerotest",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero501162.jar zero [No source] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_024() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("first",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_025() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("four",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_026() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("nomodule",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_027() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("XOne",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/pack1/X.java [XOne] EXACT_MATCH\n" + 
				"src/pack1/X.java pack1.X.X1 [XOne] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_028() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("XFourOne",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_029() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("ITwo",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/pack1/X.java [ITwo] EXACT_MATCH\n" +
				"src/pack1/X.java pack1.X.i2 [ITwo] EXACT_MATCH\n" +
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_030() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("IThreeOne",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_031() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("XThreeOne",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_032() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("zero",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.DECLARATIONS, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_033() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("pack.one",
				IJavaSearchConstants.PACKAGE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/pack1/X.java [pack.one] EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_034() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("pack.two",
				IJavaSearchConstants.PACKAGE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/pack1/X.java [pack.two] EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_035() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("pack.three",
				IJavaSearchConstants.PACKAGE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero.src.501162.jar zero EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void _testBug501162_036() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("java.base",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero.src.501162.jar zero [No source] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_037() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("zerotest",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero.src.501162.jar zero EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_038() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("first",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_039() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("four",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_040() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("nomodule",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_041() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("XOne",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/pack1/X.java [XOne] EXACT_MATCH\n" + 
				"src/pack1/X.java pack1.X.X1 [XOne] EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_042() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("XFourOne",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_043() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("ITwo",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/pack1/X.java [ITwo] EXACT_MATCH\n" +
				"src/pack1/X.java pack1.X.i2 [ITwo] EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar zero EXACT_MATCH\n" + 
				"lib/bzero.src.501162.jar pack.one.XOne EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar pack.one.XOne.itwo EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar pack.two.XTwo EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_044() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("IThreeOne",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero.src.501162.jar zero EXACT_MATCH\n" +
				"lib/bzero.src.501162.jar pack.three.XThreeOne EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_045() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("XThreeOne",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug501162_046() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module zerotest {\n" +
			"    requires zero;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X.java",
				"package pack1;\n" +
				"import pack.one.XOne;\n" +
				"import pack.two.ITwo;\n" +
				"public class X {\n" +
				"    public ITwo i2;\n" +
				"    public XOne X1;\n" +
				"}\n");
		addLibraryEntry(project1, "/JavaSearchBugs/lib/bzero.src.501162.jar", false);
		project1.close(); // sync
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("zero",
				IJavaSearchConstants.MODULE, IJavaSearchConstants.DECLARATIONS, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});

		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"lib/bzero.src.501162.jar zero EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void _testBug501162_047() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
				"package pack.top;\n" +
				"import pack.first.Y;\n" +
				"//import pack.first.second.Z;\n" +
				"\n" +
				"public class X {\n" +
				"	public Y y;\n" +
				"	//public Z z;\n" +
				"}\n";
		createFolder("/JavaSearchBugs9/src/top");
		createFile("/JavaSearchBugs9/src/top/X.java",	fileContent);
		project1.close(); // sync
		project1.open(null);

		IJavaProject project2 = createJavaProject("split.first", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String file = 
				"module split.first {\n" +
				"    exports  pack.first;\n" +
				"}\n";
		createFile("/split.first/src/module-info.java",	file);
		createFolder("/split.first/src/pack");
		createFolder("/split.first/src/pack/first");
		createFile("/split.first/src/pack/first/Y.java",
				"package pack.first;\n" +
				"public class Y{}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("split.second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		file = 
				"module split.second {\n" +
				"    exports  pack.first.second;\n" +
				"}\n";
		createFile("/split.second/src/module-info.java", file);
		createFolder("/split.second/src/pack");
		createFolder("/split.second/src/pack/first");
		createFolder("/split.second/src/pack/first/second");
		createFile("/split.second/src/pack/first/second/Z.java",
				"package pack.first.second;\n" +
				"public class Z{}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);
		SearchPattern pattern = SearchPattern.createPattern("pack.first.Y",
				IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);

		assertSearchResults(
				"src/top/X.java [pack.first.Y] EXACT_MATCH\n" +
				"src/top/X.java pack.top.X.y [Y] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("split.first");
		deleteProject("split.second");
	}
}
public void testBug519211_001() throws CoreException {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module first {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 implements pack22.I22{}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    exports pack22 to first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack1");
		createFile("/second/src/pack1/I22.java",
				"package pack22;\n" +
				"public interface I22 {}\n");

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		ICompilationUnit unit = getCompilationUnit("/JavaSearchBugs9/src/module-info.java");
		String modName = "first";
		int start = fileContent.indexOf(modName);
		IJavaElement[] elements = unit.codeSelect(start, modName.length());
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		search(elements[0], REFERENCES, scope,	this.resultCollector);

		assertSearchResults(
				"src/module-info.java second [first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug519980_001() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 implements pack22.I22{}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    exports pack22 to first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack22");
		createFile("/second/src/pack22/I22.java",
				"package pack22;\n" +
				"public interface I22 {}\n");

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("pack1.X11", IJavaSearchConstants.CLASS, REFERENCES, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9"), getJavaProject("second")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults("src/module-info.java first [pack1.X11] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug519980_002() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent = 
			"module first {\n" +
			"    requires second;" +
			"    provides pack22.I22 with pack1.X11;" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack1");
		createFile("/JavaSearchBugs9/src/pack1/X11.java",
				"package pack1;\n" +
				"public class X11 implements pack22.I22{}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile = 
				"module second {\n" +
				"    exports pack22 to first;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack22");
		createFile("/second/src/pack22/I22.java",
				"package pack22;\n" +
				"public interface I22 {}\n");

		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("pack22.I22", IJavaSearchConstants.INTERFACE, REFERENCES, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] 
				{getJavaProject("JavaSearchBugs9"), getJavaProject("second")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(		"src/module-info.java first [pack22.I22] EXACT_MATCH\n" + 
				"src/pack1/X11.java pack1.X11 [pack22.I22] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug520477_001() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src", "src2"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		String fileContent =
			"module first {\n" +
			"    exports pack1;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFile("/JavaSearchBugs9/src/X.java",
				"public class X {\n" +
				"    pack1.C C;\n" +
				"}\n"
		);
		createFolder("/JavaSearchBugs9/src2/pack1");
		createFile("/JavaSearchBugs9/src2/pack1/C.java",
				"package pack1;\n" +
				"public class C {\n" +
				"}\n"
		);

		SearchPattern pattern = SearchPattern.createPattern("pack1", IJavaSearchConstants.PACKAGE, REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/X.java X.C [pack1] EXACT_MATCH\n" +
				"src/module-info.java first [pack1] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug521221_001() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		Map<String, String> options = project1.getOptions(false);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		project1.setOptions(options);
		project1.open(null);
		createFolder("/JavaSearchBugs9/src/pack11");
		String fileContent = "package pack11;\n" +
				"public class X11 implements pack22.I22 {\n" +
				"}\n";

		createFile("/JavaSearchBugs9/src/pack11/X11.java", fileContent);
		createFolder("/JavaSearchBugs9/src/pack12");
		createFile("/JavaSearchBugs9/src/pack12/X12.java",
				"package pack12;\n" +
				"public class X12 extends pack11.X11 implements pack22.I22 {\n" +
				"}\n"
		);
		ICompilationUnit unit = getCompilationUnit("/JavaSearchBugs9/src/pack11/X11.java");
		String x11 = "X11";
		int start = fileContent.indexOf(x11);
		IJavaElement[] elements = unit.codeSelect(start, x11.length());
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		search(elements[0], REFERENCES, scope,	this.resultCollector);
	} catch (NullPointerException e) {
		assertFalse(true);
	}
	finally {
		deleteProject("JavaSearchBugs9");
	}
}
public void testBug522455_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/MyAnnot.java",
			"package pack;\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"@Target({ElementType.MODULE})\n" +	
			"@interface MyAnnot {}\n"
			);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/module-info.java",
			"import pack.*;\n" +
			"@MyAnnot\n" +
			"module mod.one {}");
	
	SearchPattern pattern = SearchPattern.createPattern(
			"MyAnnot",
			ANNOTATION_TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("src/module-info.java mod.one [MyAnnot] EXACT_MATCH");
}

// Add more tests here
}