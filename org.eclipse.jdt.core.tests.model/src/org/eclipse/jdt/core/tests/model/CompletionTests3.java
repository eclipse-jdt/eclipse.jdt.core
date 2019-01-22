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
public void setUpSuite() throws Exception {
	super.setUpSuite();
}
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
public void testBug425035_method_d() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
		createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin", "1.7");
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
}
