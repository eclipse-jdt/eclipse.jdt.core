/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;

import junit.framework.Test;

public class CompletionTests3 extends AbstractJavaModelCompletionTests {

public CompletionTests3(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
}
@Override
public void tearDownSuite() throws Exception {
	super.tearDownSuite();
}
static {
//	TESTS_NAMES = new String[] { "testBug504095" };
}
public static Test suite() {
	return buildModelTestSuite(CompletionTests3.class);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=338398
public void testBug338398a() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/a/A.java",
			"package a;\n" +
			"import static b.B.assertNotNull;\n"+
			"public class A {\n" +
			"	public void foo() {\n" +
			"		 assertno\n" +
			"   }" +
			"}\n");

		this.workingCopies[1] = getWorkingCopy(
				"/P/src/b/B.java",
				"package b;\n"+
				"public class B {\n" +
				"	static public void assertNotNull(Object object) {\n" +
				"		// nothing to do here \n" +
	    		"	}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "assertno";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"assertNotNull[METHOD_REF]{assertNotNull(), Lb.B;, (Ljava.lang.Object;)V, assertNotNull, (object), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void _testBug338398b() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/a/A_CLASS.java",
			"package a;\n" +
			"public class A_CLASS {\n" +
			"	public A_CLASS() {}\n" +
			"	/**" +
			" 	 * A_CLASS#a_cl"  +
			"	 */\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "A_CLASS#a_cl";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"A_CLASS[JAVADOC_METHOD_REF]{{@link A_CLASS#A_CLASS()}, La.A_CLASS;, ()V, A_CLASS, null, 45}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void _testBug338398c() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/a/A_CLASS.java",
			"package a;\n" +
			"public class A_CLASS {\n" +
			"	/**" +
			" 	 * @param my_s"  +
			"	 */\n" +
			"	public A_CLASS(String MY_STring) {}\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "@param my_s";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"MY_STring[JAVADOC_PARAM_REF]{MY_STring, null, null, MY_STring, null, 18}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug504095() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/a/Bug504095.java",
			"package a;\n" +
			"import java.lang.reflect.Field;\n" +
			"public class Bug504095 {\n" +
			"	static @interface Parameter {}\n" +
			"	void method(Class<?> clazz) {\n"  +
			"		for (Field member : clazz.getDeclaredFields()) {\n" +
			"			Parameter parameter = memb\n" +
			"		}\n" +
			"	}\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "memb";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"member[LOCAL_VARIABLE_REF]{member, null, LField;, member, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035a() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] value();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"package b;\n" +
				"@Annotation()\n" +
				"public class Test {\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "Annotation(";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"value[ANNOTATION_ATTRIBUTE_REF]{value = , La.Annotation;, [La.Values;, value, null, 52}\n" +
				"Values[TYPE_REF]{a.Values, a, La.Values;, null, null, 99}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035b() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] value();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"package b;\n" +
				"@Annotation({})\n" +
				"public class Test {\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "Annotation({";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"Values[TYPE_REF]{a.Values, a, La.Values;, null, null, 99}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035c() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] value();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"import a.Values;\n" +
				"package b;\n" +
				"@Annotation({Values.SOME_VALUE, })\n" +
				"public class Test {\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "SOME_VALUE,";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"Values[TYPE_REF]{Values, a, La.Values;, null, null, 102}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035d() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] x();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"package b;\n" +
				"@Annotation(x=)\n" +
				"public class Test {\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "Annotation(x=";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"Values[TYPE_REF]{a.Values, a, La.Values;, null, null, 99}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035e() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] x();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"package b;\n" +
				"@Annotation(x={})\n" +
				"public class Test {\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "Annotation(x={";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"Values[TYPE_REF]{a.Values, a, La.Values;, null, null, 99}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035f() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] x();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"import a.Values;\n" +
				"package b;\n" +
				"@Annotation(x={Values.SOME_VALUE, })\n" +
				"public class Test {\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "SOME_VALUE,";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"Values[TYPE_REF]{Values, a, La.Values;, null, null, 102}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035_method_a() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] value();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"package b;\n" +
				"public class Test {\n" +
				"	public static final int i=0;\n" +
				"	@Annotation()\n" +
				"	void f() {}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "Annotation(";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"i[FIELD_REF]{i, Lb.Test;, I, i, null, 52}\n" +
				"value[ANNOTATION_ATTRIBUTE_REF]{value = , La.Annotation;, [La.Values;, value, null, 52}\n" +
				"Values[TYPE_REF]{a.Values, a, La.Values;, null, null, 99}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035_method_b() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] value();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"package b;\n" +
				"public class Test {\n" +
				"	public static final int i=0;\n" +
				"	@Annotation({})\n" +
				"	void f() {}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "Annotation({";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"i[FIELD_REF]{i, Lb.Test;, I, i, null, 52}\n" +
				"Values[TYPE_REF]{a.Values, a, La.Values;, null, null, 99}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035_method_c() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] value();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"import a.Values;\n" +
				"package b;\n" +
				"public class Test {\n" +
				"	public static final int i=0;\n" +
				"	@Annotation({Values.SOME_VALUE, })\n" +
				"	void f() {}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "SOME_VALUE,";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"i[FIELD_REF]{i, Lb.Test;, I, i, null, 52}\n" +
				"Values[TYPE_REF]{Values, a, La.Values;, null, null, 102}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035_method_c2() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"import a.Values;\n" +
				"package b;\n" +
				"@Annotation({Values.SOME_})\n" +
				"public class Test {\n" +
				"}\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n" +
				"@interface Annotation {\n" +
				"	Values[] value();\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = ".SOME_";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"SOME_VALUE[FIELD_REF]{SOME_VALUE, Lb.Values;, Lb.Values;, SOME_VALUE, null, 81}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035_method_d() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] x();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"package b;\n" +
				"public class Test {\n" +
				"	public static final int i=0;\n" +
				"	@Annotation(x=)\n" +
				"	void f() {}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "Annotation(x=";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"i[FIELD_REF]{i, Lb.Test;, I, i, null, 52}\n" +
				"Values[TYPE_REF]{a.Values, a, La.Values;, null, null, 99}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035_method_e() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] x();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"package b;\n" +
				"public class Test {\n" +
				"	public static final int i=0;\n" +
				"	@Annotation(x={})\n" +
				"	void f() {}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "Annotation(x={";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"i[FIELD_REF]{i, Lb.Test;, I, i, null, 52}\n" +
				"Values[TYPE_REF]{a.Values, a, La.Values;, null, null, 99}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug425035_method_f() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/a/Values.java",
				"package a;\n" +
				"public enum Values {\n" +
				"	SOME_VALUE, OTHER_VALUE\n" +
				"}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/a/Annotation.java",
				"package a;\n" +
				"public @interface Annotation {\n" +
				"	Values[] x();\n" +
				"}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/b/Test.java",
				"import a.Annotation;\n" +
				"import a.Values;\n" +
				"package b;\n" +
				"public class Test {\n" +
				"	public static final int i=0;\n" +
				"	@Annotation(x={Values.SOME_VALUE, })\n" +
				"	void f() {}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		String str = this.workingCopies[2].getSource();
		String completeBehind = "SOME_VALUE,";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Test[TYPE_REF]{Test, b, Lb.Test;, null, null, 52}\n" +
				"i[FIELD_REF]{i, Lb.Test;, I, i, null, 52}\n" +
				"Values[TYPE_REF]{Values, a, La.Values;, null, null, 102}\n" +
				"OTHER_VALUE[FIELD_REF]{Values.OTHER_VALUE, La.Values;, La.Values;, OTHER_VALUE, null, 104}\n" +
				"SOME_VALUE[FIELD_REF]{Values.SOME_VALUE, La.Values;, La.Values;, SOME_VALUE, null, 104}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug547256() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/jdt/Something.java",
				"package jdt;\n" +
				"public class Something {\n" +
				"	public static void main(String[] args) {\n" +
				"		done: for (int i = 0; i < 5; ++i) {\n" +
				"			if (i == 3) {\n" +
				"				break done;\n" +
				"			}\n" +
				"			System.out.println(i);\n" +
				"		}\n" +
				"		arg\n" +
				"		System.out.println(\"done\");\n" +
				"	}\n" +
				"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "arg";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"args[LOCAL_VARIABLE_REF]{args, null, [Ljava.lang.String;, args, null, 52}",
				requestor.getResults());

	} finally {
		deleteProject("P");
	}
}
public void testBug574215() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/jdt/Something.java",
				"package jdt;\n" +
				"class S {\n" +
				"	void foo() {}\n" +
				"	String bar;\n" +
				"}\n" +
				"public class Something {\n" +
				"	private void test(S s, int i) {\n" +
				"		if (i > 2) {\n" +
				"			System.out.println(\"a\");\n" +
				"		} else {\n" +
				"			s. // <--\n" +
				"			System.out.println(\"b\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "s.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"bar[FIELD_REF]{bar, Ljdt.S;, Ljava.lang.String;, bar, null, 60}\n" +
				"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
				"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n" +
				"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}\n" +
				"foo[METHOD_REF]{foo(), Ljdt.S;, ()V, foo, null, 60}\n" +
				"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n" +
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" +
				"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}\n" +
				"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}\n" +
				"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}",
				requestor.getResults());

	} finally {
		deleteProject("P");
	}
}
public void testBug574215_field() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/jdt/Something.java",
				"package jdt;\n" +
				"class S {\n" +
				"	void foo() {}\n" +
				"	String bar;\n" +
				"}\n" +
				"public class Something {\n" +
				"	S s;\n" +
				"	private void test(int i) {\n" +
				"		if (i > 2) {\n" +
				"			System.out.println(\"a\");\n" +
				"		} else {\n" +
				"			s. // <--\n" +
				"			System.out.println(\"b\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "s.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"bar[FIELD_REF]{bar, Ljdt.S;, Ljava.lang.String;, bar, null, 60}\n" +
				"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
				"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n" +
				"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}\n" +
				"foo[METHOD_REF]{foo(), Ljdt.S;, ()V, foo, null, 60}\n" +
				"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n" +
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" +
				"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}\n" +
				"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}\n" +
				"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}",
				requestor.getResults());

	} finally {
		deleteProject("P");
	}
}
public void testBug574215_type_not_field() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/jdt/Something.java",
				"package jdt;\n" +
				"public class Something {\n" +
				"	String jdt;\n" +
				"	private void test(jdt.) {\n" +
				"	}\n" +
				"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "(jdt.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Something[TYPE_REF]{Something, jdt, Ljdt.Something;, null, null, 49}",
				requestor.getResults());

	} finally {
		deleteProject("P");
	}
}
public void testBug574215_withToken() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/jdt/Something.java",
				"package jdt;\n" +
				"class S {\n" +
				"	void foo() {}\n" +
				"	int found;\n" +
				"	String bar;\n" +
				"}\n" +
				"public class Something {\n" +
				"	private void test(S s, int i) {\n" +
				"		if (i > 2) {\n" +
				"			System.out.println(\"a\");\n" +
				"		} else {\n" +
				"			s.fo // <--\n" +
				"			System.out.println(\"b\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "s.fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"foo[METHOD_REF]{foo(), Ljdt.S;, ()V, foo, null, 60}\n" +
				"found[FIELD_REF]{found, Ljdt.S;, I, found, null, 60}",
				requestor.getResults());

	} finally {
		deleteProject("P");
	}
}
public void testBug574338() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/Snippet.java",
				"public class Snippet {\n" +
				"	private boolean flag;\n" +
				"\n" +
				"	private void test(List<String> c) {\n" +
				"		if (flag) {\n" +
				"			// content assist here\n" +
				"			List<String> scs = c.subList(0, 1);\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	String test() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBefore = "// content assist here";
		int cursorLocation = str.indexOf(completeBefore);
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED;
		assertEquals(
				"Snippet[TYPE_REF]{Snippet, , LSnippet;, null, null, null, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"c[LOCAL_VARIABLE_REF]{c, null, LList<Ljava.lang.String;>;, null, null, c, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, null, null, clone, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, null, null, equals, (obj), replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, null, null, finalize, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"flag[FIELD_REF]{flag, LSnippet;, Z, null, null, flag, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, null, null, getClass, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, null, null, hashCode, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, null, null, notify, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, null, null, notifyAll, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"test[METHOD_REF]{test(), LSnippet;, ()Ljava.lang.String;, null, null, test, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"test[METHOD_REF]{test(), LSnippet;, (LList<Ljava.lang.String;>;)V, null, null, test, (c), replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, null, null, wait, null, replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, null, null, wait, (millis), replace[101, 101], token[101, 101], "+relevance+"}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, null, null, wait, (millis, nanos), replace[101, 101], token[101, 101], "+relevance+"}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug574338_from574215c14() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/a/Bug574338.java",
			"package a;\n" +
			"public class Bug574338 {\n" +
			"	public void name(String fooo) {\n" +
			"    if (fooo != null) {\n" +
			"      fo\n" +
			"      System.err.println(\"Done\");\n" +
			"    }\n" +
			"}\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"for[KEYWORD]{for, null, null, for, null, 49}\n" +
				"fooo[LOCAL_VARIABLE_REF]{fooo, null, Ljava.lang.String;, fooo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug574704() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/Cast.java",
			"public class Cast {\n" +
			"\n" +
			"	Object field;\n" +
			"\n" +
			"	void test(Object o) {\n" +
			"		if (true) {\n" +
			"			 // content assist here does not offer o or field\n" +
			"			((String) o).toCharArray();\n" +
			"		}\n" +
			"	}\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBefore = " // content assist here";
		int cursorLocation = str.indexOf(completeBefore);
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED;
		assertResults(
				"Cast[TYPE_REF]{Cast, , LCast;, null, null, "+relevance+"}\n" +
				"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, " + relevance + "}\n" +
				"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), " + relevance + "}\n" +
				"field[FIELD_REF]{field, LCast;, Ljava.lang.Object;, field, null, " + relevance + "}\n" +
				"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, " + relevance + "}\n" +
				"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, " + relevance + "}\n" +
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, " + relevance + "}\n" +
				"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, " + relevance + "}\n" +
				"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, " + relevance + "}\n" +
				"o[LOCAL_VARIABLE_REF]{o, null, Ljava.lang.Object;, o, null, "+relevance+"}\n" +
				"test[METHOD_REF]{test(), LCast;, (Ljava.lang.Object;)V, test, (o), " + relevance + "}\n" +
				"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, " + relevance + "}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, " + relevance + "}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), " + relevance + "}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), " + relevance + "}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug574704_withPrefix() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/Cast.java",
			"public class Cast {\n" +
			"\n" +
			"	Object oField;\n" +
			"\n" +
			"	void test(Object oArg, String wrongArg) {\n" +
			"		if (true) {\n" +
			"			o // content assist here does not offer oArg or oField\n" +
			"			((String) oArg).toCharArray();\n" +
			"		}\n" +
			"	}\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBefore = " // content assist here";
		int cursorLocation = str.indexOf(completeBefore);
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED;
		int relevanceNoCase = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_UNQUALIFIED + R_NON_RESTRICTED;
		assertResults(
				"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+relevanceNoCase+"}\n" +
				"oArg[LOCAL_VARIABLE_REF]{oArg, null, Ljava.lang.Object;, oArg, null, "+relevance+"}\n" +
				"oField[FIELD_REF]{oField, LCast;, Ljava.lang.Object;, oField, null, " + relevance + "}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug574803() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/X.java",
			"public class X {\n" +
			"\n" +
			"	\n" +
			"	public static void fillAttributes(Object object) throws Exception {\n" +
			"		for (Field field : object.getClass().getFields()) {\n" +
			"			if (field.getType() == ) // no content assist\n" +
			"			Object value = field.get(object);\n" +
			"			System.out.println(value);\n" +
			"		}\n" +
			"	}\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "field.getType() == ";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED;
		assertResults(
				"X[TYPE_REF]{X, , LX;, null, null, " + relevance + "}\n" +
				"field[LOCAL_VARIABLE_REF]{field, null, LField;, field, null, " + relevance + "}\n" +
				"fillAttributes[METHOD_REF]{fillAttributes(), LX;, (Ljava.lang.Object;)V, fillAttributes, (object), " + relevance + "}\n" +
				"object[LOCAL_VARIABLE_REF]{object, null, Ljava.lang.Object;, object, null, " + relevance + "}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575032() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/Overwrite.java",
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"class AtomicInteger {\n" +
			"	void set(int i) {}\n" +
			"	int get() { return 0; }\n" +
			"}\n" +
			"\n" +
			"public class Overwrite {\n" +
			"\n" +
			"	void test(Test t) {\n" +
			"		Map<String, String> map = new HashMap<>();\n" +
			"		\n" +
			"		if (true) {\n" +
			"			t.counter. // <<--\n" +
			"			map.put(\"\", \"\");\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	static class Test {\n" +
			"		AtomicInteger counter = new AtomicInteger();\n" +
			"	}\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "t.counter.";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_CASE + R_NON_STATIC + R_NON_RESTRICTED;
		assertResults(
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+relevance+"}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+relevance+"}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+relevance+"}\n" +
			"get[METHOD_REF]{get(), LAtomicInteger;, ()I, get, null, "+relevance+"}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, "+relevance+"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+relevance+"}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+relevance+"}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+relevance+"}\n" +
			"set[METHOD_REF]{set(), LAtomicInteger;, (I)V, set, (i), "+relevance+"}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+relevance+"}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+relevance+"}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), "+relevance+"}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+relevance+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575032b() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/Overwrite.java",
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"class AtomicInteger {\n" +
			"	void set(int i) {}\n" +
			"	int get() { return 0; }\n" +
			"}\n" +
			"\n" +
			"public class Overwrite {\n" +
			"\n" +
			"	void test(Test t) {\n" +
			"		Map<String, String> map = new HashMap<>();\n" +
			"		\n" +
			"		if (true) {\n" +
			"			t.other.counter.s // <<--\n" + // more segments and also prefix of last segment
			"			map.put(\"\", \"\");\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	static class Test {\n" +
			"		Test other;" +
			"		AtomicInteger counter = new AtomicInteger();\n" +
			"	}\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "t.other.counter.s";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_CASE + R_NON_STATIC + R_NON_RESTRICTED;
		assertResults(
			"set[METHOD_REF]{set(), LAtomicInteger;, (I)V, set, (i), "+relevance+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug574979() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL17_LIB"}, "bin", "1.7");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/Bug.java",
			"public class Bug {\n" +
			"	void test (Object o) {\n" +
			"		if (true) {\n" +
			"			Str\n" +
			"			((String) o).toCharArray();\n" +
			"		}\n" +
			"	}\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "Str";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED;
		assertResults(
			"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+relevance+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575397a() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/ContentAssist.java",
			"class Thread {\n" +
			"	static void sleep(int millis) {}\n" +
			"}\n" +
			"public class ContentAssist {\n" +
			"	protected void test() {\n" +
			"		if (true) {\n" +
			"			Thread.\n" +
			"			someMethod();\n" +
			"		}\n" +
			"	}\n" +
			"	void someMethod() { }\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "Thread.";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"class[FIELD_REF]{class, null, Ljava.lang.Class<LThread;>;, class, null, 51}\n" +
			"sleep[METHOD_REF]{sleep(), LThread;, (I)V, sleep, (millis), 51}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575397b() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/ContentAssist.java",
			"class Thread {\n" +
			"	static void sleep(int millis) {}\n" +
			"	public enum State { NEW, BLOCKED }\n" +
			"}\n" +
			"public class ContentAssist {\n" +
			"	protected void test() {\n" +
			"		if (true) {\n" +
			"			Thread.Sta\n" +
			"			someMethod();\n" +
			"		}\n" +
			"	}\n" +
			"	void someMethod() { }\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "Thread.Sta";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"Thread.State[TYPE_REF]{State, , LThread$State;, null, null, 49}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575397c() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/ContentAssist.java",
			"class Thread {\n" +
			"	static void sleep(int millis) {}\n" +
			"	public enum State { NEW, BLOCKED }\n" +
			"}\n" +
			"public class ContentAssist {\n" +
			"	protected void test() {\n" +
			"		if (true) {\n" +
			"			Thread.State.\n" +
			"			someMethod();\n" +
			"		}\n" +
			"	}\n" +
			"	void someMethod() { }\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "Thread.State.";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"serialVersionUID[FIELD_REF]{serialVersionUID, Ljava.lang.Enum<LThread$State;>;, J, serialVersionUID, null, 49}\n" +
			"BLOCKED[FIELD_REF]{BLOCKED, LThread$State;, LThread$State;, BLOCKED, null, 51}\n" +
			"NEW[FIELD_REF]{NEW, LThread$State;, LThread$State;, NEW, null, 51}\n" +
			"class[FIELD_REF]{class, null, Ljava.lang.Class<LThread$State;>;, class, null, 51}\n" +
			"valueOf[METHOD_REF]{valueOf(), LThread$State;, (Ljava.lang.String;)LThread$State;, valueOf, (arg0), 51}\n" +
			"values[METHOD_REF]{values(), LThread$State;, ()[LThread$State;, values, null, 51}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575631_comment0() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/ContentAssist.java",
			"import java.util.Calendar;\n" +
			"class ZoneId {}\n" +
			"class LocalDateTime {\n" +
			"	static LocalDateTime now() { return null; }\n" +
			"	static LocalDateTime now(ZoneId id) { return null; }\n" +
			"}\n" +
			"public class ContentAssist {\n" +
			"	public static void staticMethod() {\n" +
			"		if (true) {\n" +
			"			LocalDateTime.now\n" +
			"			Calendar calendar = Calendar.getInstance();\n" +
			"		}\n" +
			"	}\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "LocalDateTime.now";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"now[METHOD_REF]{now(), LLocalDateTime;, ()LLocalDateTime;, now, null, 55}\n" +
			"now[METHOD_REF]{now(), LLocalDateTime;, (LZoneId;)LLocalDateTime;, now, (id), 55}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575631_comment1a() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/missing_proposals_for_static_fields_and_methods.java",
			"\n" +
			"class System {\n" +
			"	static Object out;\n" +
			"	static Object getEnv() { return null; }\n" +
			"}\n" +
			"class missing_proposals_for_static_fields_and_methods {\n" +
			"	void sample(String foo) {\n" +
			"		if (foo == null) {\n" +
			"			System. // <- missing: \"out\", \"getenv()\", etc. (similar to bug 574267)\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"		System. // <- here content assist works fine\n" +
			"	}\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "System.";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"class[FIELD_REF]{class, null, Ljava.lang.Class<LSystem;>;, class, null, 51}\n" +
			"getEnv[METHOD_REF]{getEnv(), LSystem;, ()Ljava.lang.Object;, getEnv, null, 51}\n" +
			"out[FIELD_REF]{out, LSystem;, Ljava.lang.Object;, out, null, 51}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575631_comment1b() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/missing_proposals_for_static_fields_and_methods.java",
			"\n" +
			"class System {\n" +
			"	static Object out;\n" +
			"	static Object getEnv() { return null; }\n" +
			"}\n" +
			"class missing_proposals_for_static_fields_and_methods {\n" +
			"	void sample(String foo) {\n" +
			"		if (foo == null) {\n" +
			"			sample(\"\");\n" +
			"		} else {\n" +
			"			System. // <- missing: \"out\", \"getenv()\", etc. (similar to bug 574215)\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"		System. // <- here content assist works fine\n" +
			"	}\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "System.";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"class[FIELD_REF]{class, null, Ljava.lang.Class<LSystem;>;, class, null, 51}\n" +
			"getEnv[METHOD_REF]{getEnv(), LSystem;, ()Ljava.lang.Object;, getEnv, null, 51}\n" +
			"out[FIELD_REF]{out, LSystem;, Ljava.lang.Object;, out, null, 51}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575631_comment3() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/X.java",
			"class OutputStream {\n" +
			"	void println() {}\n" +
			"}\n" +
			"interface Runnable { void run(); }\n" +
			"class System {\n" +
			"	static OutputStream out;\n" +
			"	static Object getEnv() { return null; }\n" +
			"}\n" +
			"class X {\n" +
			"	void foo() {\n" +
			"		Runnable r = () -> {\n" +
			"			System.out.\n" +
			"			System.out.println();\n" +
			"		};\n" +
			"	}\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "System.out.";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}\n" +
			"println[METHOD_REF]{println(), LOutputStream;, ()V, println, null, 60}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575631_comment3b() throws Exception {
	// method invocation inside lambda in field initializer
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/X.java",
			"class OutputStream {\n" +
			"	void println() {}\n" +
			"}\n" +
			"interface Runnable { void run(); }\n" +
			"class System {\n" +
			"	static OutputStream out;\n" +
			"	static Object getEnv() { return null; }\n" +
			"}\n" +
			"class X {\n" +
			"	Runnable r = () -> {\n" +
			"		System.out.\n" +
			"		System.out.println();\n" +
			"	};\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "System.out.";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}\n" +
			"println[METHOD_REF]{println(), LOutputStream;, ()V, println, null, 60}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575631_comment3c() throws Exception {
	// variable declaration in lambda in field initializer
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/X.java",
			"class OutputStream {\n" +
			"	void println() {}\n" +
			"}\n" +
			"interface Consumer { void consume(int); }\n" +
			"class Number{}\n" +
			"class System {\n" +
			"	static OutputStream out;\n" +
			"	static Object getEnv() { return null; }\n" +
			"}\n" +
			"class X {\n" +
			"	Consumer r = (int number) -> {\n" +
			"		Number \n" +
			"		System.out.println();\n" +
			"	};\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "Number ";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"number[VARIABLE_DECLARATION]{number, null, LNumber;, number, null, 48}", // FIXME: should be number2 => https://bugs.eclipse.org/576781
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug575631_comment3d() throws Exception {
	// first of two arguments in method invocation in lambda in field initializer
	// overloads should be selected by the existing second argument
	// no separating ',' yet.
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/X.java",
			"interface BiConsumer { void consume(int,boolean); }\n" +
			"class X {\n" +
			"	BiConsumer r = (int number, boolean bool) -> {\n" +
			"		bar( number);\n" +
			"	};\n" +
			"	void bar(int i, String s) {}\n" +
			"	void bar(boolean b, int j) {}\n" +
			"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeAfter = "bar(";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("""
				bool[LOCAL_VARIABLE_REF]{bool, null, Z, bool, null, 52}
				equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 52}
				bar[METHOD_REF]{, LX;, (ZI)V, bar, (b, j), 56}""", // select overload with int as 2nd arg
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testGH1440() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL11_LIB"}, "bin", "11");
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/test/Aspect.java",
			"""
			package test;

			import java.lang.annotation.*;

			@Documented
			@Retention(RetentionPolicy.RUNTIME)
			@Target(ElementType.TYPE)
			public @interface Aspect {
				String id() default "";
				String[] fetch() default {};
			}
			""");
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/test/Constants.java",
				"""
				package test;
				public class Constants {
					public static final class Group1 {
						public static final String val1 = "val1";
					}
					public static final class Group2 {
						public static final String val2 = "val2";
					}
				}
				""");
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/test/Member.java",
				"""
				package test;

				@Aspect(id = "test", fetch = { Constants.Group1.val + "." + Constants.Group2.val2 })
				public class Member { }
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[2].getSource();
		String completeAfter = ".val";
		int cursorLocation = str.indexOf(completeAfter) + completeAfter.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("val1[FIELD_REF]{val1, Ltest.Constants$Group1;, Ljava.lang.String;, val1, null, 81}",
				requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
}
