/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * Non-regression tests for bugs fixed in Java Search engine.
 */
public class JavaSearchBugs8Tests extends AbstractJavaSearchTests {

	static {
//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	TESTS_NAMES = new String[] {"testBug400899g29"};
}

public JavaSearchBugs8Tests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	if (TESTS_PREFIX != null || TESTS_NAMES != null || TESTS_NUMBERS!=null || TESTS_RANGE !=null) {
		return buildModelTestSuite(JavaSearchBugs8Tests.class);
	}
	// hack to guarantee the test order
	TestSuite suite = new Suite(JavaSearchBugs8Tests.class.getName());
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g1"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g2"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g3"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g4"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g5"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g6"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g7"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g8"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g9"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g10"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g11"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g12"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g13"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g14"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g15"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g16"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g17"));
//	suite.addTest(new JavaSearchBugs8Tests("testBug400899g18"));
//	suite.addTest(new JavaSearchBugs8Tests("testBug400899g19"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g20"));
//	suite.addTest(new JavaSearchBugs8Tests("testBug400899g22"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g23"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g24"));
//	suite.addTest(new JavaSearchBugs8Tests("testBug400899g25"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g26"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g27"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g28"));
//	suite.addTest(new JavaSearchBugs8Tests("testBug400899g29"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g30"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g31"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g32"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g33"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g34"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g35"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g36"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g37"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g38"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400902"));
	suite.addTest(new JavaSearchBugs8Tests("testBug424119_001"));
	suite.addTest(new JavaSearchBugs8Tests("testBug424119_002"));
	suite.addTest(new JavaSearchBugs8Tests("testBug424119_003"));
	suite.addTest(new JavaSearchBugs8Tests("testBug427537a"));
	suite.addTest(new JavaSearchBugs8Tests("testBug427677"));
	return suite;
}
class TestCollector extends JavaSearchResultCollector {
	public List matches = new ArrayList();
	public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
		super.acceptSearchMatch(searchMatch);
		this.matches.add(searchMatch);
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
	JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "1.8");
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

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 *	FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'	
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g1() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    @Marker int x;\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.X.x [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * TYPE:   MethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('	
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g2() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    @Marker <T> int x() { return 10; };\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * FormalParameter ::= Modifiersopt Type VariableDeclaratorIdOrThis	
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g3() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    int x(@Marker int p) { return 10; };\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java int b400899.X.x(int) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * FormalParameter ::= Modifiersopt Type PushZeroTypeAnnotations '...' VariableDeclaratorIdOrThis	
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g4() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    int x(@Marker int ... p) { return 10; };\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java int b400899.X.x(int ...) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * FormalParameter ::= Modifiersopt Type @308... TypeAnnotations '...' VariableDeclaratorIdOrThis	
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g5() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    int x(@Marker int [] @Marker ... p) { return 10; };\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java int b400899.X.x(int[] ...) [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java int b400899.X.x(int[] ...) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * UnionType ::= Type
 * UnionType ::= UnionType '|' Type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g6() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    int x() {\n" +
		"        try {\n" +
		"        } catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {\n" +
		"        }\n" +
		"        return 10;\n" +
		"    }\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators
 * LocalVariableDeclaration ::= Modifiers Type PushRealModifiers VariableDeclarators
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g7() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    int x() {\n" +
		"        @Marker int p;\n" +
		"        final @Marker int q;\n" +
		"        @Marker final int r;\n" +
		"        return 10;\n" +
		"    }\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following:
 * Resource ::= Type PushModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
 * Resource ::= Modifiers Type PushRealModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g8() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    int x() {\n" +
		"        try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" +
		"        }\n" +
		"        return 10;\n" +
		"    }\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following:
 * EnhancedForStatementHeaderInit ::= 'for' '(' Type PushModifiers Identifier Dimsopt
 * EnhancedForStatementHeaderInit ::= 'for' '(' Modifiers Type PushRealModifiers Identifier Dimsopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g9() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    int x() {\n" +
		"        for (@Marker int i: new int[3]) {}\n" +
		"        for (final @Marker int i: new int[3]) {}\n" +
		"        for (@Marker final int i: new int[3]) {}\n" +
		"        return 10;\n" +
		"    }\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * AnnotationMethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
 * AnnotationMethodHeaderName ::= Modifiersopt Type 'Identifier' '('
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g10() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public @interface X { \n" +
		"	public @Marker String value(); \n" +
		"	@Marker String value2(); \n" +
		"	@Marker public String value3(); \n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java String b400899.X.value() [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java String b400899.X.value2() [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java String b400899.X.value3() [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * PrimaryNoNewArray ::= PrimitiveType Dims '.' 'class'
 * PrimaryNoNewArray ::= PrimitiveType '.' 'class'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g11() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X { \n" +
		"	public void value() {\n" +
		"		Object o = @Marker int.class;\n" +
		"		Object o2 = @Marker int @Marker[] [] @Marker[].class;\n" +
		"   }\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.value() [Marker] POTENTIAL_MATCH\n" + 
		"src/b400899/X.java void b400899.X.value() [Marker] POTENTIAL_MATCH\n" + 
		"src/b400899/X.java void b400899.X.value() [Marker] POTENTIAL_MATCH\n" + 
		"src/b400899/X.java void b400899.X.value() [Marker] POTENTIAL_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ReferenceExpression ::= PrimitiveType Dims '::' NonWildTypeArgumentsopt IdentifierOrNew
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g12() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"interface I {\n" +
		"    Object copy(int [] ia);\n" +
		"}\n" +
		"public class X  {\n" +
		"    public static void main(String [] args) {\n" +
		"        I i = @Marker int @Marker []::<String>clone;\n" +
		"    }\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"		
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ArrayCreationWithoutArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs
 * ArrayCreationWithArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g13() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X  {\n" +
		"    public static void main(String [] args) {\n" +
		"        int i [] = new @Marker int @Marker [4];\n" +
		"        int j [] = new @Marker int @Marker [] { 10 };\n" +
		"    }\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN InsideCastExpression UnaryExpression
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g14() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X  {\n" +
		"    public static void main(String [] args) {\n" +
		"        int i = (@Marker int) 0;\n" +
		"        int j [] = (@Marker int @Marker []) null;\n" +
		"    }\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * InstanceofExpression ::= InstanceofExpression 'instanceof' ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g15() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X  {\n" +
		"    public static void main(String [] args) {\n" +
		"        if (args instanceof @Marker String[]) {\n" +
		"        }\n" +
		"    }\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * TypeArgument ::= ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g16() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X extends Y<@Marker Integer, String> {}\n" +
		"class Y<T, V> {\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.X [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ReferenceType1 ::= ReferenceType '>'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g17() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X extends Y<@Marker Integer> {}\n" +
		"class Y<T> {\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.X [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ReferenceType2 ::= ReferenceType '>>'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void _testBug400899g18() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X<T extends Object & Comparable<? super @Marker String>> {}\n" +
		"class Y<T> {\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"<TODO : ADD THE EXPECTED RESULT HERE>"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ReferenceType3 ::= ReferenceType '>>>'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void _testBug400899g19() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X<A extends X<X<X<@Marker String>>>> {}\n" +
		"class Y<T> {\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"<TODO : ADD THE EXPECTED RESULT HERE>"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * WildcardBounds ::= 'extends' ReferenceType
 * WildcardBounds ::= 'super' ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g20() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
		"   void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.foo(Map<? super Object,? extends String>) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.foo(Map<? super Object,? extends String>) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.foo(Map<? super Object,? extends String>) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.foo(Map<? super Object,? extends String>) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.goo(Map<? extends Object,? super String>) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.goo(Map<? extends Object,? super String>) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.goo(Map<? extends Object,? super String>) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.goo(Map<? extends Object,? super String>) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * TypeParameter ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList
 * AdditionalBound ::= '&' ReferenceType
 * TypeParameter1 ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList1
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void _testBug400899g22() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public interface X<U extends J<? extends X<U>>> {\n" +
		"}\n" +
		"interface J<T extends X<? extends J<T>>> {\n" +
		"}\n" +
		"class CI<U extends CJ<T, U> & @Marker J<@Marker T>,\n" +
		"			T extends CI<U, T> & @Marker X<U>>\n" +
		"	implements X<U> {\n" +
		"}\n" +
		"class CJ<T extends CI<U, T> & @Marker X<@Marker U>,\n" +
		"			U extends CJ<T, U> & J<T>>\n" +
		"	implements J<T> {\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"TODO - ADD THE RESULT HERE"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * InstanceofExpression_NotName ::= Name 'instanceof' ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g23() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X<E> {\n" +
		"  class Y {\n" +
		"    E e;\n" +
		"    E getOtherElement(Object other) {\n" +
		"      if (!(other instanceof @Marker X<?>.Y)) {};\n" +
		"      return null;\n" +
		"    }\n" +
		"  }\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java E b400899.X$Y.getOtherElement(Object) [Marker] EXACT_MATCH");	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * InstanceofExpression_NotName ::= InstanceofExpression_NotName 'instanceof' ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g24() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X<P, C> {\n" +
		"  public X() {\n" +
		"    if (!(this instanceof @Marker X)) {}\n" +
		"  }\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.X() [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ReferenceExpressionTypeArgumentsAndTrunk ::= OnlyTypeArguments '.' ClassOrInterfaceType Dimsopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void _testBug400899g25() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" + 
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"TODO: ADD THE EXPECTED RESULT HERE"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ArrayCreationWithoutArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs
 * ArrayCreationWithArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g26() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    public static void main(String [] args) {\n" +
		"        X [] x = new @Marker X @Marker [5];\n" +
		"        X [] x2 = new @Marker X @Marker [] { null };\n" +
		"    }\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression '.' ClassOrInterfaceType Dimsopt PushRPAREN InsideCastExpressionWithQualifiedGenerics UnaryExpressionNotPlusMinus
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g27() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    public static void main(String [] args) {\n" +
		"        java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;\n" +
		"    }\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ReferenceType1 ::= ClassOrInterface '<' TypeArgumentList2
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g28() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"import java.io.Serializable;\n" +
		"import java.util.List;\n" +
		"public class X<T extends Comparable<T> & Serializable> {\n" +
		"	void foo(List<? extends @Marker Comparable<T>> p) {} \n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.foo(List<? extends Comparable<T>>) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ReferenceType2 ::= ClassOrInterface '<' TypeArgumentList3
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void _testBug400899g29() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"class Base {\n" +
		"}\n" +
		"class Foo<U extends Base, V extends Bar<U, @Marker Foo<U, V>>> {\n" +
		"}\n" +
		"class Bar<E extends Base, F extends Foo<E, @Marker Bar<E, F>>> {\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"TODO: EXACT MATCH RESULTS TO BE ADDED"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ClassHeaderExtends ::= 'extends' ClassType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g30() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X extends @Marker Object {\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.X [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ClassInstanceCreationExpression ::= 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' UnqualifiedClassBodyopt
 * ClassInstanceCreationExpression ::= 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' UnqualifiedClassBodyopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g31() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    X x = new @Marker X();\n" +
		"    X y = new <String> @Marker X();\n" +		
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.X.x [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.y [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ClassInstanceCreationExpression ::= Primary '.' 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
 * ClassInstanceCreationExpression ::= Primary '.' 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g32() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    class Y {\n" +
		"    }\n" +
		"    Y y1 = new @Marker X().new @Marker Y();\n" +
		"    Y y2 = new @Marker X().new <String> @Marker Y();\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.X.y1 [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.y1 [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.y2 [Marker] POTENTIAL_MATCH\n" + 
		"src/b400899/X.java b400899.X.y2 [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g33() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    X x;\n" +
		"    class Y {\n" +
		"    }\n" +
		"    Y y1 = x.new @Marker Y();\n" +
		"    Y y2 = x.new <String> @Marker Y();\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.X.y1 [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.y2 [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * MethodHeaderThrowsClause ::= 'throws' ClassTypeList
 * ClassTypeList -> ClassTypeElt
 * ClassTypeList ::= ClassTypeList ',' ClassTypeElt
 * ClassTypeElt ::= ClassType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g34() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    void foo() throws @Marker NullPointerException, @Marker ArrayIndexOutOfBoundsException {}\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.foo() [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.foo() [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ClassHeaderImplements ::= 'implements' InterfaceTypeList
 * InterfaceHeaderExtends ::= 'extends' InterfaceTypeList
 * InterfaceTypeList -> InterfaceType
 * InterfaceTypeList ::= InterfaceTypeList ',' InterfaceType
 * InterfaceType ::= ClassOrInterfaceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g35() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"interface I {}\n" +
		"interface J {}\n" +
		"interface K extends @Marker I, @Marker J {}\n" +
		"interface L {}\n" +
		"public class X implements @Marker K, @Marker L {\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.K [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.K [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ReferenceExpression ::= Name Dimsopt '::' NonWildTypeArgumentsopt IdentifierOrNew
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g36() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"interface I {\n" +
		"    void foo(int x);\n" +
		"}\n" +
		"public class X  {\n" +
		"    public static void main(String [] args) {\n" +
		"        I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;\n" +
		"        i.foo(10); \n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * ReferenceExpression ::= Name BeginTypeArguments ReferenceExpressionTypeArgumentsAndTrunk '::' NonWildTypeArgumentsopt IdentifierOrNew
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g37() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"interface I {\n" +
		"    Y foo(int x);\n" +
		"}\n" +
		"public class X  {\n" +
		"    class Z extends Y {\n" +
		"        public Z(int x) {\n" +
		"            super(x);\n" +
		"            System.out.println();\n" +
		"        }\n" +
		"    }\n" +
		"    public static void main(String [] args) {\n" +
		"        i = @Marker W<@Marker Integer>::<@Marker String> new;\n" +
		"    }\n" +
		"}\n" +
		"class W<T> extends Y {\n" +
		"    public W(T x) {\n" +
		"        super(0);\n" +
		"        System.out.println(x);\n" +
		"    }\n" +
		"}\n" +
		"class Y {\n" +
		"    public Y(int x) {\n" +
		"        System.out.println(x);\n" +
		"    }\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"
);	
}

/**
 * @bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * @test Ensures that the search for type use annotation finds matches in the following
 * CastExpression ::= PushLPAREN Name PushRPAREN InsideCastExpressionLL1 UnaryExpressionNotPlusMinus
 * CastExpression ::= PushLPAREN Name Dims PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
 * CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression Dimsopt PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
 * CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression '.' ClassOrInterfaceType Dimsopt PushRPAREN InsideCastExpressionWithQualifiedGenerics UnaryExpressionNotPlusMinus
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g38() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"public class X {\n" +
		"    Object o = (@Marker X) null;\n" +
		"    Object p = (@Marker X @Marker []) null;\n" +
		"    Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;\n" +
		"    Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
		"}\n" +
 		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Marker {}\n"
	);
SearchPattern pattern = SearchPattern.createPattern(
		"Marker",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/b400899/X.java b400899.X.o [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.p [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.p [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.q [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.q [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.q [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.q [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.r [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.r [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.r [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.r [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.r [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.r [Marker] EXACT_MATCH\n" + 
		"src/b400899/X.java b400899.X.r [Marker] EXACT_MATCH"
);	
}

/**
	 * @bug 402902:  [1.8][search] Search engine fails to annotation matches in extends/implements clauses
	 * @test Ensures that the search for type use annotation finds matches 
	 * in extends and implements clauses. 
	 *		
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=402902"
	 */
public void testBug400902() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400902/X.java",
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"import java.io.Serializable;\n" +
			"@Marker1 @Marker public class X extends @Marker Object implements @Marker Serializable {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"	int x = (@Marker int) 0;\n" +
			" 	@Marker public class Y {}\n" +
			"}\n" +
			"@Target(ElementType.TYPE_USE)\n" +	
			"@interface Marker {}\n" +
			"@Target(ElementType.TYPE)\n" +	
			"@interface Marker1 {}"
		);
	SearchPattern pattern = SearchPattern.createPattern(
			"Marker",
			ANNOTATION_TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
	new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
	getJavaSearchWorkingCopiesScope(),
	this.resultCollector,
	null);
	assertSearchResults(
			"src/b400902/X.java b400902.X [Marker] EXACT_MATCH\n" +
			"src/b400902/X.java b400902.X [Marker] EXACT_MATCH\n" +
			"src/b400902/X.java b400902.X [Marker] EXACT_MATCH\n" +
			"src/b400902/X.java b400902.X.x [Marker] EXACT_MATCH\n" +
			"src/b400902/X.java b400902.X$Y [Marker] EXACT_MATCH" 
	);	
}

/**
 * @bug 424119:  [1.8][search] CCE in search for references to TYPE_USE annotation on array dimension
 * @test Ensures that the search for type use annotation finds matches 
 * in local variable declaration dimensions.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=424119"
 */
public void testBug424119_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b424119/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"import java.io.Serializable;\n" +
		"public class X{\n" +
		"{\n" +
		"	String tab @Annot() [] = null;\n" +
		"}\n" +
		"public void foo() {\n" +
		"	String t @Annot() [] @Annot()[] = null, s @Annot() [];\n" +
		"}\n" +
		"String tab @Annot() [] = null;\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Annot {}\n"
	);
	SearchPattern pattern = SearchPattern.createPattern(
		"Annot",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
		"src/b424119/X.java b424119.X.{} [Annot] EXACT_MATCH\n" +
		"src/b424119/X.java b424119.X.tab [Annot] EXACT_MATCH\n" +
		"src/b424119/X.java void b424119.X.foo() [Annot] EXACT_MATCH\n" +
		"src/b424119/X.java void b424119.X.foo() [Annot] EXACT_MATCH\n" +
		"src/b424119/X.java void b424119.X.foo() [Annot] EXACT_MATCH"
	);		
}

/**
 * @bug 424119:  [1.8][search] CCE in search for references to TYPE_USE annotation on array dimension
 * @test Ensures that the search for type use single variable annotation finds matches 
 * in local variable declaration inside initializer - ref bug 424119 comment 1 
 * - checks for non-existence of CCE. 
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=424119"
 */
public void testBug424119_002() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b424119/X.java",
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Target;\n" +
		"import java.io.Serializable;\n" +
		"public class X{\n" +
		"{\n" +
		"	String tab @Annot(int[].class) [] = null;\n" +
		"}\n" +
		"@Target(ElementType.TYPE_USE)\n" +	
		"@interface Annot {\n" +
		"	Class<int[]> value();\n" +
		"}\n"
	);
	SearchPattern pattern = SearchPattern.createPattern(
		"Annot",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
		"src/b424119/X.java b424119.X.{} [Annot] EXACT_MATCH");
}
/**
 * @bug 424119:  [1.8][search] CCE in search for references to TYPE_USE annotation on array dimension
 * @test Ensures that the search for type use single variable annotation finds matches 
 * in local variable declaration inside initializer - ref bug 424119 comment 1 - checks
 * for indexing issue (ie not finding the references as mentioned in comment 1) 
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=424119"
 */

public void testBug424119_003() throws CoreException {
	try {
		// Create project and files
		IJavaProject project = createJavaProject("P1", new String[] {"src"}, new String[] {"bin"}, "bin");
		createFile(
			"/P1/src/X.java",
			"public class X {\n" +
			"	String tab @Annot(int[].class) [] = null;\n" +
			"}\n");
		createFile(
			"P1/src/Annot.java",
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Annot {\n" +
			"	Class<int[]> value();\n" +
			"}"
		);
		waitUntilIndexesReady();

		IndexManager indexManager = JavaModelManager.getIndexManager();
		indexManager.indexAll(project.getProject());
		waitUntilIndexesReady();

		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		JavaSearchResultCollector collector = new JavaSearchResultCollector();
		collector.showProject();
		search("Annot", TYPE, REFERENCES, scope, collector);
		assertSearchResults(
			"src/X.java [in P1] X.tab [Annot]",
			collector);
	}
	finally {
		deleteProject("P1");
	}
}
/**
 * @bug 427537:  [1.8][search] CCE with search match location set to cast type and intersection casts
 * @test Ensures that the search for type use annotation does not cause CCE and returns correct results 
 * 	
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=427537"
 */
public void testBug427537a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b427537/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"interface J {}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = (I & J) () -> {};\n" +
			"		i.foo();\n" +
			"		I j = (J & I) () -> {};\n" +
			"		j.foo();\n" +
			"	}\n" +
			"}\n" 
	);
	SearchPattern pattern = SearchPattern.createPattern(
		"I",
		TYPE,
		CAST_TYPE_REFERENCE,
		EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
		"src/b427537/X.java void b427537.X.main(String[]) [I] EXACT_MATCH\n" +
		"src/b427537/X.java void b427537.X.main(String[]) [I] EXACT_MATCH"
	);
}
/**
 * @bug 427677: [1.8][search] NPE in MatchLocator.reportMatching with unresolved NameQualifiedType qualifier
 * @test test
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=427677"
 */
public void testBug427677() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b427677/X.java",
			"import java.lang.annotation.*; \n" +
			"class X implements unresolved. @Marker1 Collection<Integer> { } \n" +
			"@Target (ElementType.TYPE_USE) \n" +
			"@interface Marker1 {}"
	);
	SearchPattern pattern = SearchPattern.createPattern(
		"Marker1",
		ANNOTATION_TYPE,
		REFERENCES,
		EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("src/b427677/X.java b427677.X [Marker1] EXACT_MATCH");
}
// Add new tests in JavaSearchBugs8Tests
}