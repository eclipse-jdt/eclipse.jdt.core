/*******************************************************************************
 * Copyright (c) 2016, 2018 IBM Corporation and others.
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

import static org.eclipse.jdt.core.search.IJavaSearchScope.APPLICATION_LIBRARIES;
import static org.eclipse.jdt.core.search.IJavaSearchScope.REFERENCED_PROJECTS;
import static org.eclipse.jdt.core.search.IJavaSearchScope.SOURCES;
import static org.eclipse.jdt.core.search.IJavaSearchScope.SYSTEM_LIBRARIES;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.core.tests.model.ClasspathTests.TestContainer;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.TypeParameter;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

/**
 * Non-regression tests for bugs fixed in Java Search engine.
 */
public class JavaSearchBugs9Tests extends AbstractJavaSearchTests {

	private final String module_separator = String.valueOf(IIndexConstants.SEPARATOR); // "/"
	private final String explicit_unnamed = new String(IJavaSearchConstants.ALL_UNNAMED); // "ALL-UNNAMED"
	static {
//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	TESTS_NAMES = new String[] {"testBug528059"};
}

public JavaSearchBugs9Tests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchBugs9Tests.class, BYTECODE_DECLARATION_ORDER);
}
class TestCollector extends JavaSearchResultCollector {
	@Override
	public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
		super.acceptSearchMatch(searchMatch);
	}
}
class ReferenceCollector extends JavaSearchResultCollector {
	@Override
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
	@Override
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

@Override
IJavaSearchScope getJavaSearchScope() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
}
IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
	if (packageName == null) return getJavaSearchScope();
	return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	if (this.wcOwner == null) {
		this.wcOwner = new WorkingCopyOwner() {};
	}
	return getWorkingCopy(path, source, this.wcOwner);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#setUpSuite()
 */
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "9");
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("JavaSearchBugs");
	super.tearDownSuite();
}
@Override
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

		addModularProjectEntry(project1, project2);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
				"lib/bzero501162.jar zero [No source] EXACT_MATCH\n"
				+ "lib/bzero501162.jar pack.one.XOne.itwo [No source] EXACT_MATCH",
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero.src.501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero.src.501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero.src.501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero.src.501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero.src.501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero.src.501162.jar"), null);
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
		addModularLibraryEntry(project1, new Path("/JavaSearchBugs/lib/bzero.src.501162.jar"), null);
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
public void testBug519151_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/X.java",
			"package pack;\n" +
			"class X {}\n"
			);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/module-info.java",
			"import pack.*;\n" +
			"module mod.one {}");
	String needle = "mod.one" + this.module_separator + "pack.X";
	SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("src/pack/X.java pack.X [X] EXACT_MATCH");
}
public void testBug519151_002() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/X.java",
			"package pack;\n" +
			"class X {}\n"
			);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/module-info.java",
			"import pack.*;\n" +
			"module mod.one {}");
	SearchPattern pattern = SearchPattern.createPattern("pack.X", IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("src/pack/X.java pack.X [X] EXACT_MATCH");
}
public void testBug519151_003() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/X.java",
			"package pack;\n" +
			"class X {}\n"
			);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/module-info.java",
			"import pack.*;\n" +
			"module mod.one {}");
	String needle = this.module_separator + "pack.X";
	SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("");
}
public void testBug519151_004() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "first" + this.module_separator + "pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug519151_005() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("pack.X", IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug519151_006() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = this.module_separator + "pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults("",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug519151_007() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("pack.X", IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug519151_008() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("pack.X", IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug519151_009() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "first,second" + this.module_separator + "pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n"+
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug519151_010() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = " first, second"+ this.module_separator +"pack.X"; // with white space
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n"+
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug519151_011() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "mod."+ this.module_separator +"pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, DECLARATIONS, SearchPattern.R_PREFIX_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n"+
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug519151_012() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "mod.*" + this.module_separator + "pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug519151_013() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module first {\n" +
			"    requires second;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/Y.java",
				"package pack;\n" +
				"public class Y {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "first" + this.module_separator + "pack.Y";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, IJavaSearchConstants.MODULE_GRAPH, SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/Y.java pack.Y [Y] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug519151_014() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"    requires mod.second {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"    requires third;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String thirdFile =
				"module third {\n" +
				"}\n";
		createFile("/third/src/module-info.java",	thirdFile);
		createFolder("/third/src/pack");
		createFile("/third/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String needle = "third" + this.module_separator + "pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, IJavaSearchConstants.MODULE_GRAPH, SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}

public void testBug519151_015() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"    requires mod.second {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"    requires third;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String thirdFile =
				"module third {\n" +
				"}\n";
		createFile("/third/src/module-info.java",	thirdFile);
		createFolder("/third/src/pack");
		createFile("/third/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String needle = "mod.second,third" + this.module_separator + "pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, IJavaSearchConstants.MODULE_GRAPH, SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}

public void testBug519151_016() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"    requires mod.second {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"    requires third;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String thirdFile =
				"module third {\n" +
				"}\n";
		createFile("/third/src/module-info.java",	thirdFile);
		createFolder("/third/src/pack");
		createFile("/third/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String needle = "non.existant.module" + this.module_separator + "pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, IJavaSearchConstants.MODULE_GRAPH, SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults("",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}
public void testBug519151_017() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"    requires mod.second {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"    requires third;\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String thirdFile =
				"module third {\n" +
				"}\n";
		createFile("/third/src/module-info.java",	thirdFile);
		createFolder("/third/src/pack");
		createFile("/third/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String needle = this.module_separator + "pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, IJavaSearchConstants.MODULE_GRAPH, SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults("",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}
public void testBug519151_018() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		createFile("/third/src/X.java",
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String needle = this.module_separator + "pack.X";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, IJavaSearchConstants.MODULE_GRAPH, SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
			"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}
public void testBug519151_019() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		createFile("/third/src/X.java",
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String needle = this.explicit_unnamed + this.module_separator + "pack.X"; // "ALL-UNNAMED/pack.X"
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, IJavaSearchConstants.MODULE_GRAPH, SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
			"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}
public void testBug519151_020() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		createFile("/third/src/X.java",
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String module1 = this.explicit_unnamed; // "ALL-UNNAMED/pack.X"
		String module2 = "mod.second";
		String needle = module1 + "," + module2 + this.module_separator + "pack.X"; // "ALL-UNNAMED,second/pack.X"
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
			"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
			"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}
public void testBug519151_021() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"    requires third;\n"+
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String thirdFile =
				"module third {\n" +
				"}\n";
		createFile("/third/src/module-info.java",	thirdFile);
		createFolder("/third/src/pack");
		createFile("/third/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String module1 = this.explicit_unnamed; // "ALL-UNNAMED/pack.X"
		String module2 = "mod.second";
		String needle = module1 + "," + module2 + this.module_separator + "pack.X"; // "ALL-UNNAMED,second/pack.X"
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.TYPE, IJavaSearchConstants.MODULE_GRAPH,
				SearchPattern.R_PATTERN_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
			"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
			"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
			"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}
public void testBug519151_022() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		SearchPattern pattern = SearchPattern.createPattern("first/X", IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}
public void testBug519151_023() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("first", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		addLibraryEntry(project1, "/JavaSearchBugs/lib/lib519151.jar", false);
		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		String secondFile =
				"module second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		addModularLibraryEntry(project2, new Path("/JavaSearchBugs/lib/lib519151.jar"), null);
		project1.close(); // sync
		project2.close(); // sync
		project2.open(null);
		project1.open(null);
		waitUntilIndexesReady();
		SearchPattern pattern = SearchPattern.createPattern("module519151/pack519151.X519151", IJavaSearchConstants.TYPE, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] {project2, project1});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"lib/lib519151.jar pack519151.X519151 EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("first");
		deleteProject("second");
	}
}
public void _testBug519151_0X1() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "0mod.*" + this.module_separator + "pack.X"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.CLASS, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n"+
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void _testBug519151_0X2() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "0mod\\.s.*" + this.module_separator + "pack.X"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.CLASS, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void _testBug519151_0X3() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String thirdFile =
				"module third {\n" +
				"}\n";
		createFile("/third/src/module-info.java",	thirdFile);
		createFolder("/third/src/pack");
		createFile("/third/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String needle = "0mod\\.f.*,mod\\.s.*" + this.module_separator + "pack.X"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.CLASS, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}

public void _testBug519151_0X4() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String thirdFile =
				"module third {\n" +
				"}\n";
		createFile("/third/src/module-info.java",	thirdFile);
		createFolder("/third/src/pack");
		createFile("/third/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String needle = "0mod\\.f.*,mod\\.s.*" + this.module_separator + "pack.X"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.CLASS, DECLARATIONS, ERASURE_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/pack/X.java pack.X [X] EXACT_MATCH\n" +
				"src/pack/X.java pack.X [X] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}

public void testBug528059_001() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "mod.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_REGEXP_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH\n" +
				"src/module-info.java mod.second [mod.second] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug528059_002() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		//addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath())); // reduce scope
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "mod.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_REGEXP_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug528059_003() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "mod\\.f.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_REGEXP_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug528059_004() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = "mod.f.+"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_REGEXP_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug528059_005() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = ".*f.+"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_REGEXP_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug528059_006() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);

		String needle = ".*m+.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_REGEXP_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH\n" +
				"src/module-info.java mod.second [mod.second] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug528059_007() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
		    " requires second;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"  requires third;\n"+
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String thirdFile =
				"module third {\n" +
				"}\n";
		createFile("/third/src/module-info.java",	thirdFile);
		createFolder("/third/src/pack");
		createFile("/third/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);

		String needle = "mod\\.[fs]+.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_REGEXP_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH\n" +
				"src/module-info.java mod.second [mod.second] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}

public void testBug530016_001() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		waitUntilIndexesReady();
		String needle = "/r mod.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH\n" +
				"src/module-info.java mod.second [mod.second] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug530016_002() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		//addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath())); // reduce scope
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		waitUntilIndexesReady();
		String needle = "/r mod.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug530016_003() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		waitUntilIndexesReady();
		String needle = "/r mod\\.f.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug530016_004() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		waitUntilIndexesReady();
		String needle = "/r mod.f.+"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug530016_005() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		waitUntilIndexesReady();
		String needle = "/r .*f.+"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug530016_006() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");


		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		waitUntilIndexesReady();
		String needle = "/r .*m+.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH\n" +
				"src/module-info.java mod.second [mod.second] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
	}
}

public void testBug530016_007() throws Exception {
	try {

		IJavaProject project1 = createJavaProject("JavaSearchBugs9", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String fileContent =
			"module mod.first {\n" +
		    " requires second;\n" +
			"}\n";
		createFile("/JavaSearchBugs9/src/module-info.java",	fileContent);
		createFolder("/JavaSearchBugs9/src/pack");
		createFile("/JavaSearchBugs9/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");

		IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String secondFile =
				"module mod.second {\n" +
				"  requires third;\n"+
				"}\n";
		createFile("/second/src/module-info.java",	secondFile);
		createFolder("/second/src/pack");
		createFile("/second/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

		IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project3.open(null);
		addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
		String thirdFile =
				"module third {\n" +
				"}\n";
		createFile("/third/src/module-info.java",	thirdFile);
		createFolder("/third/src/pack");
		createFile("/third/src/pack/X.java",
				"package pack;\n" +
				"public class X {}\n");
		addClasspathEntry(project1, JavaCore.newProjectEntry(project3.getPath()));

		project1.close(); // sync
		project2.close();
		project3.close();
		project3.open(null);
		project2.open(null);
		project1.open(null);
		waitUntilIndexesReady();
		String needle = "/r mod\\.[fs]+.*"; // Pattern
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{getJavaProject("JavaSearchBugs9")});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/module-info.java mod.first [mod.first] EXACT_MATCH\n" +
				"src/module-info.java mod.second [mod.second] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("JavaSearchBugs9");
		deleteProject("second");
		deleteProject("third");
	}
}

public void testBug529367() throws Exception {
	try {
		IJavaProject project1 = createJavaProject("Library", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
		project1.open(null);
		createFolder("/Library/src/javax/xml/bind");
		createFile("/Library/src/javax/xml/bind/JAXBContext.java",
				"package javax.xml.bind;\n" +
				"public abstract class JAXBContext {\n" +
				"	public static JAXBContext newInstance( String contextPath )\n" +
				"		throws JAXBException {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n");
		createFile("/Library/src/javax/xml/bind/JAXBException.java",
				"package javax.xml.bind;\n" +
				"public class JAXBException extends Exception {}\n");

		IJavaProject project2 = createJava9Project("second", new String[] {"src"});
		project2.open(null);
		addClasspathEntry(project2, JavaCore.newProjectEntry(project1.getPath()));
		createFolder("/second/src/p1");
		createFile("/second/src/p1/ImportJAXBType.java",
				"package p1;\n" +
				"\n" +
				"import javax.xml.bind.JAXBContext;\n" +
				"\n" +
				"public class ImportJAXBType {\n" +
				"\n" +
				"	public static void main(String[] args) throws Exception {\n" +
				"		JAXBContext context = JAXBContext.newInstance(\"\");\n" +
				"	}\n" +
				"\n" +
				"}\n");

		project1.close(); // sync
		project2.close();
		project2.open(null);
		project1.open(null);
		waitUntilIndexesReady();

		SearchPattern pattern = SearchPattern.createPattern("JAXBContext", IJavaSearchConstants.TYPE, REFERENCES, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
				{project2});
		search(pattern, scope, this.resultCollector);
		assertSearchResults(
				"src/javax/xml/bind/JAXBContext.java JAXBContext javax.xml.bind.JAXBContext.newInstance(String) [JAXBContext] EXACT_MATCH\n" +
				"src/p1/ImportJAXBType.java [JAXBContext] EXACT_MATCH\n" +
				"src/p1/ImportJAXBType.java void p1.ImportJAXBType.main(String[]) [JAXBContext] EXACT_MATCH\n" +
				"src/p1/ImportJAXBType.java void p1.ImportJAXBType.main(String[]) [JAXBContext] EXACT_MATCH",
			this.resultCollector);
	}
	finally {
		deleteProject("Library");
		deleteProject("second");
	}
}
public void testBug531705() throws Exception {
	IJavaProject project1 = createJavaProject("JavaSearchBugs9_1", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
	try  {
		project1.open(null);

		createFolder("/JavaSearchBugs9_1/src/x");
		String content =  "module my.mod { \n" +
		"}\n";
		String filePath = "/JavaSearchBugs9_1/src/module-info.java";
		createFile(filePath, content);
		addLibraryEntry(project1, "/JavaSearchBugs/lib/test.third.jar", false);
		project1.close();
		project1.open(null);
		waitUntilIndexesReady();
		String needle = "test.third.from.manifest";
		SearchPattern pattern = SearchPattern.createPattern(needle, IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]{project1});
		search(pattern, scope, this.resultCollector);
		String expected = "lib/test.third.jar test.third.from.manifest EXACT_MATCH";
		assertSearchResults(expected, this.resultCollector);

	} finally {
		deleteProject(project1);
	}
}
public void testBug545293() throws Exception {
	if(!isJRE11) return;

	// Note: this test only failed (without the fix for bug 545293) if it was
	// executed exactly with a JDK 11

	IJavaProject p = createJava9Project("P");
	IJavaProject p1 = createJava9Project("P1");
	try  {
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		addClasspathEntry(p, JavaCore.newProjectEntry(p1.getPath()));

		p.open(null);
		p1.open(null);

		IPath dummyJarPath = p.getProject().getLocation().append("dummy.jar");
		createJar(
			new String[] {
					"javax/dummy/Dummy.java",
					"package javax.dummy;\n" + //
					"\n" + //
					"public class Dummy {\n" + //
					"	public Dummy(String s) {\n" +//
					"	}\n" + //
					"}"
			},
			dummyJarPath.toOSString()
		);
		addClasspathEntry(p, JavaCore.newLibraryEntry(dummyJarPath, null, null));

		String content =  "import javax.dummy.Dummy;\n" +
				"import com.example.Bar;\n" +
				"\n" +
				"public class Hello {\n" +
				"    public void run() {\n" +
				"        String str = null;\n" +
				"        foo();\n" +
				"    }\n" +
				"    \n" +
				"    private Object foo() {\n" +
				"        return null;\n" +
				"    }\n" +
				"}\n" +
				"";
		String filePath = "/P/src/Hello.java";
		createFile(filePath, content);

		createFolder("/P1/src/com/example");
		createFile("/P1/src/com/example/Bar.java",
				"package com.example.bar;\n" +
				"public class Bar {\n" +
				"}\n");


		p.close();
		p1.close();
		p1.open(null);
		p.open(null);

		waitUntilIndexesReady();

		IType type = p.findType("Hello");
		IMethod method = type.getMethod("foo", new String[] {});
		SearchPattern pattern = SearchPattern.createPattern(method, REFERENCES, EXACT_RULE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]{p});
		search(pattern, scope, this.resultCollector);
		String expected = "src/Hello.java void Hello.run() [foo()] EXACT_MATCH";
		assertSearchResults(expected, this.resultCollector);

	} finally {
		deleteProject(p);
		deleteProject(p1);
	}
}

public void testBug547051_modular() throws Exception {
	try {
		IJavaProject project = createJava9Project("P");
		IType type = project.findType("java.util.Collection");
		IJavaSearchScope scope = SearchEngine.createStrictHierarchyScope(project, type, Boolean.TRUE, Boolean.TRUE, null);
		BasicSearchEngine engine = new BasicSearchEngine();
		char[] packageName = null;
		char[] typeName = null;
		AtomicBoolean r = new AtomicBoolean(false);
		engine.searchAllTypeNames(packageName, SearchPattern.R_PATTERN_MATCH,
				typeName, SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CAMELCASE_MATCH,
				TYPE, scope,
				(modifiers, packageName1, simpleTypeName, enclosingTypeNames, path, access) -> r.set(true),
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, new NullProgressMonitor());
		assertTrue("Type search has no matches for subtypes of " + type, r.get());
	}
	finally {
		deleteProject("P");
	}
}

public void testBug547051_modular2() throws Exception {
	if (!isJRE9) {
		return;
	}
	try {
		IJavaProject project = createJava9Project("P");
		IType type = project.findType("java.util.Collection");
		IJavaSearchScope scope = SearchEngine.createStrictHierarchyScope(project, type, Boolean.TRUE, Boolean.TRUE, null);
		BasicSearchEngine engine = new BasicSearchEngine();
		char[] packageName = null;
		char[] typeName = "HashSe".toCharArray();
		AtomicBoolean r = new AtomicBoolean(false);
		engine.searchAllTypeNames(packageName, SearchPattern.R_PATTERN_MATCH,
				typeName, SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CAMELCASE_MATCH,
				TYPE, scope,
				(modifiers, packageName1, simpleTypeName, enclosingTypeNames, path, access) -> r.set(true),
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, new NullProgressMonitor());
		assertTrue("Type search has no matches for subtypes of " + type, r.get());
	}
	finally {
		deleteProject("P");
	}
}
public void testBug547051_modular3() throws Exception {
	if(!isJRE9) {
		return;
	}
	try {
		IJavaProject project = createJava9Project("P");
		IType type = project.findType("java.util.Collection");
		IJavaSearchScope scope = SearchEngine.createStrictHierarchyScope(project, type, Boolean.TRUE, Boolean.TRUE, null);
		BasicSearchEngine engine = new BasicSearchEngine();
		char[] packageName = "java.util".toCharArray();
		char[] typeName = "HashSet".toCharArray();
		AtomicBoolean r = new AtomicBoolean(false);
		engine.searchAllTypeNames(packageName, SearchPattern.R_PATTERN_MATCH,
				typeName, SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CAMELCASE_MATCH,
				TYPE, scope,
				(modifiers, packageName1, simpleTypeName, enclosingTypeNames, path, access) -> r.set(true),
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, new NullProgressMonitor());
		assertTrue("Type search has no matches for subtypes of " + type, r.get());
	}
	finally {
		deleteProject("P");
	}
}

public void testBug547095_local_variables_search_modular() throws Exception {
	try {
		IJavaProject project = createJava9Project("P");
		IType type = project.findType("java.util.Collection");
		IMethod method = type.getMethod("equals", new String[] {"Ljava.lang.Object;" });
		LocalVariable lv = new LocalVariable(((JavaElement)method), "o", 0, 0, 0, 0, "QObject;", null, 0, true);
		SearchPattern pattern = SearchPattern.createPattern(lv, REFERENCES, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, SYSTEM_LIBRARIES | APPLICATION_LIBRARIES | REFERENCED_PROJECTS | SOURCES);
		search(pattern, scope, this.resultCollector);
		// should not throw an error
	}
	finally {
		deleteProject("P");
	}
}

public void testBug547095_type_patter_search_modular() throws Exception {
	try {
		IJavaProject project = createJava9Project("P");
		IType type = project.findType("java.util.Collection");
		TypeParameter tp = new TypeParameter(((JavaElement)type), "E");
		SearchPattern pattern = SearchPattern.createPattern(tp, REFERENCES, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, SYSTEM_LIBRARIES | APPLICATION_LIBRARIES | REFERENCED_PROJECTS | SOURCES);
		search(pattern, scope, this.resultCollector);
		// should not throw an error
	}
	finally {
		deleteProject("P");
	}
}

/*
 * Test for call hierarchy of a field with a type coming from a JRE module.
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/740
 */
public void testMethodReferenceForTypeFromJREModuleBugGh740() throws Exception {
	String projectName = "gh740MethodReferenceForTypeParameterFromModuleBug";
	try {
		IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {}, "bin", "11");

		String jrePath = Util.getJREDirectory();
		Path bootModPath = new Path(jrePath +"/lib/jrt-fs.jar");
		Path sourceAttachment = new Path(jrePath +"/lib/src.zip");

		addClasspathEntry(project, JavaCore.newContainerEntry(new Path("container/default"), ClasspathEntry.NO_ACCESS_RULES, new IClasspathAttribute[0], false));
		JavaCore.setClasspathContainer(
				new Path("container/default"),
				new IJavaProject[]{ project },
				new IClasspathContainer[] {
					new TestContainer(
						new Path("container/default"),
						new IClasspathEntry[] {
								JavaCore.newLibraryEntry(bootModPath, sourceAttachment, null, null, moduleAttribute(), false),
						})
				},
				null);
		String packageFolder = "/" + projectName + "/src/test";
		createFolder(packageFolder);
		String testSource =
				"package test;\n" +
				"public class Test {\n" +
				"  public Object testField;\n" +
				"  public void testMethod() {\n" +
				"    testField = null;\n" +
				"  }\n" +
				"}";
		createFile(packageFolder + "/Test.java", testSource);
		buildAndExpectNoProblems(project);
		IType type = project.findType("test.Test");
		IField field = type.getField("testField");
		search(field, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
		assertSearchResults(
				"src/test/Test.java void test.Test.testMethod() [testField] EXACT_MATCH");
	} finally {
		deleteProject(projectName);
	}
}

public void testGH902_whenTypeReferenceIsUnknown_expectToBeFound() throws CoreException {
	try {
		IJavaProject project = createJava9Project("JavaSearchBugs9");
		project.open(null);
		createFile("JavaSearchBugs9/src/GH902.java",
				"""
					public class GH902 {
						public static void foo() {
							StackWalker.getInstance().forEach(s -> System.out.println(s));
						}
					}
				""");

		refresh(project);

		IType type = project.findType("java.lang.StackWalker");
		assertNotNull("type should not be null", type);
		search(type, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope());
		assertTrue("Actual: ".concat(this.resultCollector.toString()),
				this.resultCollector.toString().contains("src/GH902.java void GH902.foo() [StackWalker] EXACT_MATCH"));
	} finally {
		deleteProject("JavaSearchBugs9");
	}
}

public void testGH902_whenTypeReferenceIsUnknownButQualified_expectToBeFound() throws CoreException {
	try {
		IJavaProject project = createJava9Project("JavaSearchBugs9");
		project.open(null);
		createFile("JavaSearchBugs9/src/GH902.java",
				"""
					public class GH902 {
						public static void foo() {
							java.lang.StackWalker.getInstance().forEach(s -> System.out.println(s));
						}
					}
				""");

		refresh(project);

		IType type = project.findType("java.lang.StackWalker");
		assertNotNull("type should not be null", type);
		search(type, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope());
		assertTrue("Actual: ".concat(this.resultCollector.toString()), this.resultCollector.toString()
				.contains("src/GH902.java void GH902.foo() [java.lang.StackWalker] EXACT_MATCH"));
	} finally {
		deleteProject("JavaSearchBugs9");
	}
}

public void testGH902_whenTypeReferenceIsUnknownButQualifiedNested_expectToBeFound() throws CoreException {
	try {
		IJavaProject project = createJava9Project("JavaSearchBugs9");
		project.open(null);
		createFile("JavaSearchBugs9/src/GH902.java",
				"""
					public class GH902 {
						public static void foo() {
							java.util.AbstractMap.Entry.comparingByKey();
						}
					}
				""");

		refresh(project);

		IType type = project.findType("java.util.Map.Entry");
		assertNotNull("type should not be null", type);
		search(type, REFERENCES, RAW_RULE, SearchEngine.createWorkspaceScope());
		assertTrue("Actual: ".concat(this.resultCollector.toString()), this.resultCollector.toString()
				.contains("src/GH902.java void GH902.foo() [java.util.AbstractMap.Entry] EXACT_MATCH"));
	} finally {
		deleteProject("JavaSearchBugs9");
	}
}

public void testGH902_whenTypeReferenceIsUnknownButNested_expectToBeFound() throws CoreException {
	try {
		IJavaProject project = createJava9Project("JavaSearchBugs9");
		project.open(null);
		createFile("JavaSearchBugs9/src/GH902.java",
				"""
					import static java.util.AbstractMap.*;
					public class GH902 {
						public static void foo() {
							Entry.comparingByKey();
						}
					}
				""");
		refresh(project);

		IType type = project.findType("java.util.Map.Entry");
		assertNotNull("type should not be null", type);
		search(type, REFERENCES, RAW_RULE, SearchEngine.createWorkspaceScope());
		assertTrue("Actual: ".concat(this.resultCollector.toString()),
				this.resultCollector.toString().contains("src/GH902.java void GH902.foo() [Entry] EXACT_MATCH"));
	} finally {
		deleteProject("JavaSearchBugs9");
	}
}

/*
 * Test for call hierarchy of a field with a type coming from a container,
 * when the search is initiated in a project that defines a module.
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/740
 */
public void testModuleContainerSearchBugGh1039() throws Exception {
	String jarProjectName = "gh1039ModuleContainerSearch";
	setUpJavaProject(jarProjectName);
	String projectName = "gh1039ModuleContainerSearchTest";
	try {
		IJavaProject project = createJava9Project(projectName);

		addClasspathEntry(project, JavaCore.newContainerEntry(new Path("container/default"), ClasspathEntry.NO_ACCESS_RULES, new IClasspathAttribute[0], false));

		Path jarPath = new Path("/gh1039ModuleContainerSearch/TestGH1039.jar");
		JavaCore.setClasspathContainer(
				new Path("container/default"),
				new IJavaProject[]{ project },
				new IClasspathContainer[] {
					new TestContainer(
						new Path("container/default"),
						new IClasspathEntry[] {
								JavaCore.newLibraryEntry(jarPath, null, null, null, moduleAttribute(), false),
						})
				},
				null);
		String fileContent =
				"""
				module testmodule {
				    requires dummymodule;
				}
				""";
		createFile("/" + projectName + "/src/module-info.java",	fileContent);
		String packageFolder = "/" + projectName + "/src/test";
		createFolder(packageFolder);
		String testSource =
				"""
				package test;
				@dummy.Dummy
				public class Test {
				};
				""";
		createFile(packageFolder + "/Test.java", testSource);
		buildAndExpectNoProblems(project);
		IType type = project.findType("dummy.Dummy");
		search(type, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
		assertSearchResults(
				"src/test/Test.java test.Test [dummy.Dummy] EXACT_MATCH");
	} finally {
		deleteProject(projectName);
		deleteProject(jarProjectName);
	}
}

/*
 * The fix for GitHub issue 675 results in not finding search matches
 * within a modular jar on the classpath.
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/935
 * See also: https://github.com/eclipse-jdt/eclipse.jdt.core/issues/675
 */
public void testNoMatchesInModularJarOnClasspathBugGh935() throws Exception {
	String projectName = "gh935NoMatchesInModularJarOnClasspath";
	try {
		IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL11_LIB"}, "bin", "11");
		String snippet1 =
				"""
				package test;
				public class Test1 {
				  public void testMethod() {
				  }
				}
				""";
		String snippet2 =
				"""
				package test;
				public class Test2 {
				  public void testCaller() {
				      Test1 test1 = new Test1();
				      test1.testMethod();
				  }
				}
				""";

		addLibrary(project,
				"libGh935.jar",
				"libGh935.src.zip",
				new String[]  {
						"module-info.java",
						"module testmodule {\n" +
						"  exports test;\n" +
						"}",
						"test/Test1.java",
						snippet1,
						"test/Test2.java",
						snippet2, },
				JavaCore.VERSION_11);

		buildAndExpectNoProblems(project);
		waitUntilIndexesReady();

		IType type = project.findType("test.Test1");

		IMethod method = type.getMethod("testMethod", new String[0]);
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
		assertSearchResults(
				"libGh935.jar void test.Test2.testCaller() EXACT_MATCH");
	} finally {
		deleteProject(projectName);
	}
}
// Add more tests here
}