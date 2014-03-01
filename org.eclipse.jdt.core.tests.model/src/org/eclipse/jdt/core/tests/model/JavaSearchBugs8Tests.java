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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
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
//	TESTS_NAMES = new String[] {"testBug400905_0002"};
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
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g25"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g26"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g27"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g28"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400899g29"));
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
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0001"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0001a"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0002"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0003"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0004"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0005"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0006"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0007"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0008"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0009"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0010"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0011"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0012"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0013"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0014"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400904_0015"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0001"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0002"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0003"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0004"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0005"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0006"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0007"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0007a"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0008"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0009"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0010"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0011"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0012"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0013"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0013a"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0013b"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0013c"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0013d"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0013e"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0013f"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0014"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0015"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0016"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0017"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0018"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0019"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0020"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0021"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0022"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0023"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0024"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0025"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0026"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0027"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0028"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0029"));
	suite.addTest(new JavaSearchBugs8Tests("testBug400905_0030"));
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
public void testBug400899g18() throws CoreException {
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
public void testBug400899g19() throws CoreException {
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
public void testBug400899g22() throws CoreException {
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
public void testBug400899g25() throws CoreException {
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
		"src/b400899/X.java b400899.X$X [Marker] POTENTIAL_MATCH\n" +
		"src/b400899/X.java b400899.X$X [Marker] POTENTIAL_MATCH\n" +
		"src/b400899/X.java b400899.X$X [Marker] POTENTIAL_MATCH\n" +
		"src/b400899/X.java b400899.X$X [Marker] POTENTIAL_MATCH\n" +
		"src/b400899/X.java b400899.X$X [Marker] POTENTIAL_MATCH"
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
public void testBug400899g29() throws CoreException {
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
		"src/b400899/X.java b400899.Foo [Marker] EXACT_MATCH\n" +
		"src/b400899/X.java b400899.Bar [Marker] EXACT_MATCH"
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
/**
 * @bug 400904
 * @test tests search for Reference expression - super:: form, without type arguments
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
			"    void foo(int x);\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"    public static void main(String [] args) {\n" +
			"	new X().doit();\n" +
			"    }\n" +
			"    void doit() {\n" +
			"        I i = super::foo;\n" +
			"        i.foo(10); \n" +
			"    }\n" +
			"}\n" +
			"class Y {\n" +
			"    public void foo(int x) {\n" +
			"	System.out.println(x);\n" +
			"    }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.doit() [super::foo] EXACT_MATCH"
	);	
}

public void testBug400904_0001a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
			"    void foo(int x);\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"    public static void main(String [] args) {\n" +
			"	new X().doit();\n" +
			"    }\n" +
			"    void doit() {\n" +
			"        I i = super::foo;\n" +
			"        i.foo(10); \n" +
			"    }\n" +
			"}\n" +
			"class Y {\n" +
			"    public void foo(int x) {\n" +
			"	System.out.println(x);\n" +
			"    }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, SUPER_REFERENCE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.doit() [super::foo] EXACT_MATCH"
	);	
}

/**
 * @bug 400904
 * @test tests search for Reference expression - super:: form, with type arguments
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0002() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
			"    void foo(int x);\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"    public static void main(String [] args) {\n" +
			"	new X().doit();\n" +
			"    }\n" +
			"    void doit() {\n" +
			"        I i = super::<String>foo;\n" +
			"        i.foo(10); \n" +
			"    }\n" +
			"}\n" +
			"class Y {\n" +
			"    public void foo(int x) {\n" +
			"	System.out.println(x);\n" +
			"    }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.doit() [super::<String>foo] EXACT_MATCH"
	);	
}

/**
 * @bug 400904
 * @test tests search for Reference expression - SimpleName:: form, without type arguments.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0003() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
			"	public void doit();\n" +
			"}\n" +
			"class Y {\n" + 
			"	Y() {}\n" +
			"	Y(int i) {}\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"    X(int i) {} \n" +
			"   static void foo() {}\n" +
			"   static void foo(int i) {}\n" +
			"	I i = X :: foo;\n" +
			"	I j = Y :: new;\n" +
			"   public static void main() { \n" +
			"     Y y = new Y(); \n" +
			"     foo(); \n" +
			"   }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", null);
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java b400904.X.i [X :: foo] EXACT_MATCH\n" + 
			"src/b400904/X.java void b400904.X.main() [foo()] EXACT_MATCH"
	);	
}

/**
 * @bug 400904
 * @test tests search for Reference expression - SimpleName:: form, with type arguments.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0004() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
			"    void foo(int x);\n" +
			"}\n" +
			"public class X  {\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = Y::<String>foo;\n" +
			"        i.foo(10); \n" +
			"    }\n" +
			"}\n" +
			"class Y {\n" +
			"    public static void foo(int x) {\n" +
			"	System.out.println(x);\n" +
			"    }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y::<String>foo] EXACT_MATCH"
	);	
}

/**
 * @bug 400904
 * @test tests search for Reference expression - QualifiedName:: form, without type arguments.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0005() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
			"    void foo(int x);\n" +
			"}\n" +
			"public class X  {\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = Y.Z::foo;\n" +
			"        i.foo(10); \n" +
			"    }\n" +
			"}\n" +
			"class Y {\n" +
			"    static class Z {\n" +
			"        public static void foo(int x) {\n" +
			"	    System.out.println(x);\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y").getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y.Z::foo] EXACT_MATCH"
	);	
}

/**
 * @bug 400904
 * @test tests search for Reference expression - QualifiedName:: form, with type arguments.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0006() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
			"    void foo(int x);\n" +
			"}\n" +
			"public class X  {\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = Y.Z::<String>foo;\n" +
			"        i.foo(10); \n" +
			"    }\n" +
			"}\n" +
			"class Y {\n" +
			"    static class Z {\n" +
			"        public static void foo(int x) {\n" +
			"	    System.out.println(x);\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y").getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y.Z::<String>foo] EXACT_MATCH"
	);	
}

/**
 * @bug 400904
 * @test tests search for Reference expression - Primary:: form, without type arguments.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0007() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
			"    void foo(int x);\n" +
			"}\n" +
			"public class X  {\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = new Y()::foo;\n" +
			"        i.foo(10); \n" +
			"    }\n" +
			"}\n" +
			"class Y {\n" +
			"        void foo(int x) {\n" +
			"	    System.out.println(x);\n" +
			"        }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [new Y()::foo] EXACT_MATCH"
	);	
}

/**
 * @bug 400904
 * @test tests search for Reference expression - Primary:: form, with type arguments.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0008() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
			"    void foo(int x);\n" +
			"}\n" +
			"public class X  {\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = new Y()::<String>foo;\n" +
			"        i.foo(10); \n" +
			"    }\n" +
			"}\n" +
			"class Y {\n" +
			"        void foo(int x) {\n" +
			"	    System.out.println(x);\n" +
			"        }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [new Y()::<String>foo] EXACT_MATCH"
	);	
}

/**
 * @bug 400904
 * @test tests search for Reference expression - X<T>:: form, without type arguments
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0009() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" + 
			"  void foo(Y<String> y, int x);\n" + 
			"}\n" + 
			"public class X {\n" + 
			"  public X() {\n" + 
			"    super();\n" + 
			"  }\n" + 
			"  public static void main(String[] args) {\n" + 
			"    I i = Y<String>::foo;\n" + 
			"    i.foo(new Y<String>(), 10);\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y<T> {\n" + 
			"  Y() {\n" + 
			"    super();\n" + 
			"  }\n" + 
			"  void foo(int x) {\n" + 
			"    System.out.println(x);\n" + 
			"  }\n" + 
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y<String>::foo] EXACT_MATCH"
	);
}


/**
 * @bug 400904
 * @test tests search for Reference expression - X<T>:: form, with type arguments
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0010() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" + 
			"  void foo(Y<String> y, int x);\n" + 
			"}\n" + 
			"public class X {\n" + 
			"  public X() {\n" + 
			"    super();\n" + 
			"  }\n" + 
			"  public static void main(String[] args) {\n" + 
			"    I i = Y<String>::<String>foo;\n" + 
			"    i.foo(new Y<String>(), 10);\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y<T> {\n" + 
			"  Y() {\n" + 
			"    super();\n" + 
			"  }\n" + 
			"  void foo(int x) {\n" + 
			"    System.out.println(x);\n" + 
			"  }\n" + 
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y<String>::<String>foo] EXACT_MATCH"
	);
}

/**
 * @bug 400904
 * @test tests search for Reference expression - X<T>.Name :: form, without type arguments
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0011() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
				"    void foo(Y<String>.Z z, int x);\n" +
			"}\n" +
			"public class X  {\n" +
			"	@SuppressWarnings(\"unused\")\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = Y<String>.Z::foo;\n" +
			"        i.foo(new Y<String>().new Z(), 10); \n" +
			"    }\n" +
			"}\n" +
			"class Y<T> {\n" +
			"    class Z {\n" +
			"        void foo(int x) {\n" +
			"	    System.out.println(x);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y<String>.Z::foo] EXACT_MATCH"
	);
}

/**
 * @bug 400904
 * @test tests search for Reference expression - X<T>.Name :: form, with type arguments
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0012() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
				"    void foo(Y<String>.Z z, int x);\n" +
			"}\n" +
			"public class X  {\n" +
			"	@SuppressWarnings(\"unused\")\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = Y<String>.Z::<String>foo;\n" +
			"        i.foo(new Y<String>().new Z(), 10); \n" +
			"    }\n" +
			"}\n" +
			"class Y<T> {\n" +
			"    class Z {\n" +
			"        void foo(int x) {\n" +
			"	    System.out.println(x);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y<String>.Z::<String>foo] EXACT_MATCH"
	);
}

/**
 * @bug 400904
 * @test tests search for Reference expression - X<T>.Y<K> :: form, without type arguments
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0013() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
				"    void foo(Y<String>.Z<String> z, int x);\n" +
			"}\n" +
			"public class X  {\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = Y<String>.Z<String>::foo;\n" +
			"        i.foo(new Y<String>().new Z<String>(), 10); \n" +
			"    }\n" +
			"}\n" +
			"class Y<T> {\n" +
			"    class Z<K> {\n" +
			"        void foo(int x) {\n" +
			"	    System.out.println(x);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y<String>.Z<String>::foo] EXACT_MATCH"
	);
}

/**
 * @bug 400904
 * @test tests search for Reference expression - X<T>.Y<K> :: form, with type arguments
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0014() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
				"    void foo(Y<String>.Z<String> z, int x);\n" +
			"}\n" +
			"public class X  {\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = Y<String>.Z<String>::<String>foo;\n" +
			"        i.foo(new Y<String>().new Z<String>(), 10); \n" +
			"    }\n" +
			"}\n" +
			"class Y<T> {\n" +
			"    class Z<K> {\n" +
			"        void foo(int x) {\n" +
			"	    System.out.println(x);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y<String>.Z<String>::<String>foo] EXACT_MATCH"
	);
}

/**
 * @bug 400904
 * @test tests search for Reference expression - X<T>.Y<K> :: new form, with type arguments
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 * 
 */
public void testBug400904_0015() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"interface I {\n" +
				"    void foo(Y<String> y);\n" +
			"}\n" +
			"public class X  {\n" +
			"    public static void main(String [] args) {\n" +
			"        I i = Y<String>.Z<String>::<String>new;\n" +
			"        i.foo(new Y<String>()); \n" +
			"    }\n" +
			"}\n" +
			"class Y<T> {\n" +
			"    class Z<K> {\n" +
			"        Z(Y<String> y) {\n" +
			"            System.out.println(\"Y<T>.Z<K>::new\");\n" +
			"        }\n" +
			"        Z1(Y<String> y) {\n" +
			"            System.out.println(\"Y<T>.Z<K>::new\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("Z", new String[] {"QY<QString;>;"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [Y<String>.Z<String>::<String>new] EXACT_MATCH"
	);
}
/**
 * @bug 400905
 * @test lambda expression search on a) field b)parameter
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 * 
 */
public void testBug400905_0001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/X.java",
			"interface I {\n" +
			"    int foo();\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"    public static void main(String [] args) {\n" +
			"	     I i = () -> 42;\n" +
			"    }\n" +
			"	public void bar(I i) {}\n" +
			" 	public void doit() {\n" +
			"		bar(() ->1);\n" +
			"	}\n" +
			"}\n"
	);
	IType type = this.workingCopies[0].getType("I");
	IMethod method = type.getMethod("foo", new String[] {});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"src/b400905/X.java int b400905.I.foo() [foo] EXACT_MATCH\n" +
			"src/b400905/X.java int void b400905.X.main(String[]):<lambda>#1.lambda$1() [() ->] EXACT_MATCH\n" +			
			"src/b400905/X.java int void b400905.X.doit():<lambda>#2.lambda$2() [() ->] EXACT_MATCH");	
}
/**
 * @bug 400905
 * @test  lambda expression search on a set of contexts with
 * interface declaration and usage being in different files.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 * 
 */
public void testBug400905_0002() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/X.java",
			"public class X extends Y {\n" +
			"    public static void main(String [] args) {\n" +
			"		Y y = new Y();\n" +
			"		I i = () -> {};\n" + // variable declaration context
			"		I i1;\n" +
			"		i1 = ()  -> {}" + // assignment context
			"		y.goo(()-> {});\n" + // method argument context
			"		i.foo();\n" +
			"	}\n" +
			"	public I bar() {\n" +
			"		return () -> {};\n" + // return statement context
			"	}\n" +
			"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b400905/I.java",
			"public interface I {\n" +
			"    int foo();\n" +
			"}\n") ;
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b400905/Y.java",
			"public class Y {\n" +
			"    void goo(I i) {};\n" +
			"}\n") ;
	IType type = this.workingCopies[1].getType("I");
	IMethod method = type.getMethod("foo", new String[] {});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"src/b400905/I.java int b400905.I.foo() [foo] EXACT_MATCH\n" + 
			"src/b400905/X.java int void b400905.X.main(String[]):<lambda>#1.lambda$1() [() ->] EXACT_MATCH\n" + 
			"src/b400905/X.java int void b400905.X.main(String[]):<lambda>#2.lambda$2() [()  ->] EXACT_MATCH\n" + 
			"src/b400905/X.java int void b400905.X.main(String[]):<lambda>#3.lambda$3() [()->] EXACT_MATCH\n" + 
			"src/b400905/X.java int I b400905.X.bar():<lambda>#4.lambda$4() [() ->] EXACT_MATCH");	
}
/**
 * @bug 400905
 * @test  lambda expression search on a set of contexts with the 
 * interface declaration and usage being in different files.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 * 
 */
public void testBug400905_0003() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/X.java",
			"public class X extends Y {\n" +
			"    public static void main(String [] args) {\n" +
			"		I[] i = new I[] { y-> y.exists(), y -> y.canRead(), y  -> y.canWrite()};\n" + // array initialization context
			"	}\n" +
			"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b400905/I.java",
			"public interface I {\n" +
			"    boolean foo(Y y);\n" +
			"}\n") ;
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b400905/Y.java",
			"public class Y {\n" +
			"    public boolean exists() { return true};\n" +
			"    public boolean canRead() { return true};\n" +
			"    public boolean canWrite() { return true};\n" +
			"}\n") ;
	IType type = this.workingCopies[1].getType("I");
	IMethod method = type.getMethod("foo", new String[] {"QY;"});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"src/b400905/I.java boolean b400905.I.foo(Y) [foo] EXACT_MATCH\n" + 
			"src/b400905/X.java boolean void b400905.X.main(String[]):<lambda>#1.lambda$1(b400905/Y) [y->] EXACT_MATCH\n" + 
			"src/b400905/X.java boolean void b400905.X.main(String[]):<lambda>#2.lambda$2(b400905/Y) [y ->] EXACT_MATCH\n" + 
			"src/b400905/X.java boolean void b400905.X.main(String[]):<lambda>#3.lambda$3(b400905/Y) [y  ->] EXACT_MATCH"
	);	
}
/**
 * @bug 400905
 * @test  lambda expression search on a set of contexts with the 
 * interface declaration and usage being in different files.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 * 
 */
public void testBug400905_0004() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/X.java",
			"public class X extends Y {\n" +
			"    public static void main(String [] args) {\n" +
			"		I<Y> c = () /* foo */ -> () /* bar */ -> {};\n" + // array initialization context
			"		I<Y> y = args.length < 1 ? (() /* true */-> 42) : (() /* false */ -> 23);\n" + // conditional expression context
			"		Object o = (I) () /* cast */ -> 42;\n " + //cast expression
			"	}\n" +
			"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b400905/I.java",
			"public interface I<T> {\n" +
			"    public T foo();\n" +
			"}\n") ;
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b400905/Y.java",
			"public interface Y {\n" +
			"    public abstract void run() { };\n" +
			"}\n") ;
	IType type = this.workingCopies[1].getType("I");
	IMethod method = type.getMethod("foo", new String[] {});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"src/b400905/I.java T b400905.I.foo() [foo] EXACT_MATCH\n" + 
			"src/b400905/X.java b400905/Y void b400905.X.main(String[]):<lambda>#1.lambda$1() [() /* foo */ ->] EXACT_MATCH\n" + 
			"src/b400905/X.java b400905/Y void b400905.X.main(String[]):<lambda>#3.lambda$3() [() /* true */->] EXACT_MATCH\n" + 
			"src/b400905/X.java b400905/Y void b400905.X.main(String[]):<lambda>#4.lambda$4() [() /* false */ ->] EXACT_MATCH\n" + 
			"src/b400905/X.java java/lang/Object void b400905.X.main(String[]):<lambda>#5.lambda$5() [() /* cast */ ->] EXACT_MATCH"
	);	
}
/**
 * @bug 400905
 * @test  lambda expression search on a set of contexts with the 
 * interface declaration and usage being in different files.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 * 
 */
public void testBug400905_0005() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/X.java",
			"public class X extends Y {\n" +
			"    public static void main(String [] args) {\n" +
			"		I<Y> c = () /* foo */ -> () /* bar */ -> {};\n" + // array initialization context
			"	}\n" +
			"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b400905/I.java",
			"public interface I<T> {\n" +
			"    public T foo();\n" +
			"}\n") ;
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b400905/Y.java",
			"public interface Y {\n" +
			"    public abstract void bar() { };\n" +
			"}\n") ;
	
	IType type = this.workingCopies[2].getType("Y");
	IMethod method = type.getMethod("bar", new String[] {});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"src/b400905/X.java void b400905/Y void b400905.X.main(String[]):<lambda>#1.lambda$1():<lambda>#2.lambda$2() [() /* bar */ ->] EXACT_MATCH\n" + 
			"src/b400905/Y.java void b400905.Y.bar() [bar] EXACT_MATCH"
	);	
}
/**
 * @bug 400905
 * @test  lambda expression search on a set of contexts with the 
 * interface declaration and usage being in different files.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 * 
 */
public void testBug400905_0006() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"		Y.goo(()->{});\n" +
			"	}\n" +
			"}\n"
		);
		createFile(
			"/P/src/Y.java",
			"public class Y {\n" +
			"    public static void goo(I i) {};\n" +
			"}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public void foo();\n" +
			"}\n"
		);
		IMethod method = getCompilationUnit("/P/src/I.java").getType("I").getMethod("foo", new String[0]);
		search(method, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults(
				"src/I.java void I.foo() [foo] EXACT_MATCH\n" + 
				"src/X.java void void X.main(String[]):<lambda>#1.lambda$1() [()->] EXACT_MATCH"
		);
	}
	finally {
		deleteProject("P");
	}
}

/**
 * @bug 400905
 * @test  lambda expression search on a set of contexts with the 
 * interface declaration and usage being in different files.
 *		
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 * 
 */
public void testBug400905_0007() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X  {\n" +
			"    void foo() {\n" +
			"        I i = Y::new;\n" + 
			"    }\n" +
			"}\n"
		);
		createFile(
			"/P/src/Y.java",
			"public class Y extends X {\n" +
			"    Y(int x) {};\n" +
			"}\n"
		);
		createFile(
			"/P/src/I.java",
			"interface I {\n" +
			"    X foo(int x);\n" +
			"}\n"
		);
		IMethod method = getCompilationUnit("/P/src/Y.java").getType("Y").getMethod("Y", new String[] {"I"});
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults("src/X.java void X.foo() [Y::new] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0007a() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X  {\n" +
			"    void foo() {\n" +
			"        I i = Y::new;\n" + 
			"    }\n" +
			"}\n"
		);
		createFile(
			"/P/src/Y.java",
			"public class Y extends X {\n" +
			"    Y(int x) {};\n" +
			"    Y() {};\n" +
			"}\n"
		);
		createFile(
			"/P/src/I.java",
			"interface I {\n" +
			"    X foo();\n" +
			"}\n"
		);
		IMethod method = getCompilationUnit("/P/src/Y.java").getType("Y").getMethod("Y", new String[] {"I"});
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults("");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0008() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"		I i = Y::goo;\n" +
			"	}\n" +
			"}\n"
		);
		createFile(
			"/P/src/Y.java",
			"public class Y {\n" +
			"    public static void goo() {};\n" +
			"}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public void foo();\n" +
			"}\n"
		);
		IMethod method = getCompilationUnit("/P/src/Y.java").getType("Y").getMethod("goo", new String[0]);
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults("src/X.java void X.main(String[]) [Y::goo] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0009() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"		I i = Y::goo;\n" +
			"	}\n" +
			"}\n"
		);
		createFile(
			"/P/src/Y.java",
			"public class Y {\n" +
			"    public static void goo() {};\n" +
			"    public static void goo(int x) {};\n" +
			"}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public void foo();\n" +
			"}\n"
		);
		IMethod method = getCompilationUnit("/P/src/Y.java").getType("Y").getMethod("goo", new String[] {"I"});
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults("");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0010() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/J.java",
			"public interface J {\n" +
			"    public static void main(String [] args) {\n" +
			"		I i = Y::goo;\n" +
			"	}\n" +
			"    default void foo() {\n" +
			"       I i = Y::goo;\n" +
			"       Y.goo(()->{});\n" +
			"   }\n" +
			"}\n"
		);
		createFile(
			"/P/src/Y.java",
			"public class Y {\n" +
			"    public static void goo(I i) {};\n" +
			"    public static void goo() {};\n" +
			"}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public void foo();\n" +
			"}\n"
		);
		IMethod method = getCompilationUnit("/P/src/Y.java").getType("Y").getMethod("goo", new String[0]);
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults("src/J.java void J.main(String[]) [Y::goo] EXACT_MATCH\n" + 
				"src/J.java void J.foo() [Y::goo] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0011() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/J.java",
			"public interface J {\n" +
			"    public static void main(String [] args) {\n" +
			"		I i = Y::goo;\n" +
			"	}\n" +
			"    default void foo() {\n" +
			"       I i = Y::goo;\n" +
			"       Y.goo(()->{});\n" +
			"   }\n" +
			"}\n"
		);
		createFile(
			"/P/src/Y.java",
			"public class Y {\n" +
			"    public static void goo(I i) {};\n" +
			"    public static void goo() {};\n" +
			"}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public void foo();\n" +
			"}\n"
		);
		IMethod method = getCompilationUnit("/P/src/I.java").getType("I").getMethod("foo", new String[0]);
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults("");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0012() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"	static void foo() {}\n" +
			"	I i = () -> {};\n" +
			"	I i2 = new I() {\n" +
			"		public void doit() {\n" +
			"			\n" +
			"		}\n" +
			"	};\n" +
			"}\n" +
			" class Y {}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public void doit();\n" +
			"}\n"
		);
		
		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[0]);
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/I.java void I.doit() [doit] EXACT_MATCH\n" + 
				"src/X.java void X.i:<lambda>#1.lambda$1() [() ->] EXACT_MATCH\n" + 
				"src/X.java void X.i2:<anonymous>#1.doit() [doit] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0013() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    I i = () -> 0;\n" +
			"	    I i2 = new I() {\n" +
			"		    public int doit() {\n" +
			"			    return 0;\n" +
			"		    }\n" +
			"	    };\n" +
			"   }\n" +
			"}\n" +
			" class Y {}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit();\n" +
			"}\n"
		);
		
		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[0]);
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/I.java int I.doit() [doit] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1() [() ->] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<anonymous>#1.doit() [doit] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
// verify that nested lambdas are found and they are linked properly.
public void testBug400905_0013a() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    I i = () /*1*/-> {\n" +
			"                 I i2 = () /*2*/-> 10;\n" +
			"                 return 0;\n" +
			"       };\n" +
			"   }\n" +
			"}\n" +
			" class Y {}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit();\n" +
			"}\n"
		);
		
		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[0]);
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/I.java int I.doit() [doit] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1() [() /*1*/->] EXACT_MATCH\n" + 
				"src/X.java int int void X.zoo():<lambda>#1.lambda$1():<lambda>#2.lambda$2() [() /*2*/->] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
// Verify that nested lambdas are found and they are linked properly. (hierarchy scope)
public void testBug400905_0013b() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    I i = (X x2) /*1*/-> {\n" +
			"                 I i2 = (X x3) /*2*/-> 10;\n" +
			"                 return 0;\n" +
			"       };\n" +
			"   }\n" +
			"}\n" +
			" class Y {}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit(X x1);\n" +
			"}\n"
		);
		
		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/X.java int int void X.zoo():<lambda>#1.lambda$1(X):<lambda>#2.lambda$2(X) [X] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1(X) [X] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
// Verify that nested lambdas are found and they are linked properly. (java search scope - type)
public void testBug400905_0013c() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    I i = (X x2) /*1*/-> {\n" +
			"                 I i2 = (X x3) /*2*/-> 10;\n" +
			"                 return 0;\n" +
			"       };\n" +
			"   }\n" +
			"}\n" +
			" class Y {}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit(X x1);\n" +
			"}\n"
		);
		
		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchEngine.createJavaSearchScope(new IJavaElement[] {type}), this.resultCollector);
		assertSearchResults("src/X.java int int void X.zoo():<lambda>#1.lambda$1(X):<lambda>#2.lambda$2(X) [X] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1(X) [X] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
// Verify that nested lambdas are found and they are linked properly. (java search scope - project)
public void testBug400905_0013d() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    I i = (X x2) /*1*/-> {\n" +
			"                 I i2 = (X x3) /*2*/-> 10;\n" +
			"                 return 0;\n" +
			"       };\n" +
			"   }\n" +
			"}\n" +
			" class Y {}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit(X x1);\n" +
			"}\n"
		);
		
		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchEngine.createJavaSearchScope(new IJavaElement[] {project}), this.resultCollector);
		assertSearchResults("src/I.java int I.doit(X) [X] EXACT_MATCH\n" + 
				"src/X.java int int void X.zoo():<lambda>#1.lambda$1(X):<lambda>#2.lambda$2(X) [X] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1(X) [X] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
// Verify that nested lambdas are found and they are linked properly. (java search scope - project)
public void testBug400905_0013e() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    I i = (X x2) /*1*/-> {\n" +
			"                 I i2 = (X x3) /*2*/-> 10;\n" +
			"                 class Q {\n" +
			"                     X x;\n" +
			"                 }\n" +
			"                 return 0;\n" +
			"       };\n" +
			"   }\n" +
			"}\n" +
			" class Y {}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit(X x1);\n" +
			"}\n"
		);
		
		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchEngine.createJavaSearchScope(new IJavaElement[] {project}), this.resultCollector);
		assertSearchResults("src/I.java int I.doit(X) [X] EXACT_MATCH\n" + 
				"src/X.java int int void X.zoo():<lambda>#1.lambda$1(X):<lambda>#2.lambda$2(X) [X] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1(X):Q#1.x [X] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1(X) [X] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
// Verify that nested lambdas are found and they are linked properly. (java search scope - project)
public void testBug400905_0013f() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    I i = (X x2) /*1*/-> {\n" +
			"                 X x2;\n" +
			"                 I i2 = (X x3) /*2*/-> 10;\n" +
			"                 class Q {\n" +
			"                     X x;\n" +
			"                 }\n" +
			"                 return 0;\n" +
			"       };\n" +
			"   }\n" +
			"}\n" +
			" class Y {}\n"
		);
		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit(X x1);\n" +
			"}\n"
		);
		
		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchEngine.createJavaSearchScope(new IJavaElement[] {project}), this.resultCollector);
		assertSearchResults("src/I.java int I.doit(X) [X] EXACT_MATCH\n" + 
				"src/X.java int int void X.zoo():<lambda>#1.lambda$1(X):<lambda>#2.lambda$2(X) [X] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1(X):Q#1.x [X] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1(X) [X] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#1.lambda$1(X) [X] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0014() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    Y.goo((x) -> -x);\n" +
			"   }\n" +
			"}\n");
		createFile(
				"/P/src/Y.java",
				"public class Y {\n" +
				"   static void goo(I i) {}\n" +
				"}\n");

		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit(int x);\n" +
			"}\n"
		);
		
		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[] {"I"});
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/I.java int I.doit(int) [doit] EXACT_MATCH\n" + 
							"src/X.java int void X.zoo():<lambda>#1.lambda$1(int) [(x) ->] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0015() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    Y.goo((x) -> -x);\n" +
			"   }\n" +
			"}\n");
		createFile(
				"/P/src/Y.java",
				"public class Y {\n" +
				"   static void goo(J j) {}\n" +
				"}\n");

		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit(int x);\n" +
			"}\n"
		);
		createFile(
				"/P/src/J.java",
				"public interface J {\n" +
				"    public int doit(int x);\n" +
				"}\n"
			);
		
		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[] {"I"});
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/I.java int I.doit(int) [doit] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0016() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   void zoo() {\n" +
			"	    Y.goo((x) -> -x);\n" +
			"	    Y.zoo((x) -> -x);\n" +
			"   }\n" +
			"}\n");
		createFile(
				"/P/src/Y.java",
				"public class Y {\n" +
				"   static void goo(J j) {}\n" +
				"   static void zoo(I i) {}\n" +
				"}\n");

		createFile(
			"/P/src/I.java",
			"public interface I {\n" +
			"    public int doit(int x);\n" +
			"}\n"
		);
		createFile(
				"/P/src/J.java",
				"public interface J {\n" +
				"    public int doit(int x);\n" +
				"}\n"
			);
		
		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[] {"I"});
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/I.java int I.doit(int) [doit] EXACT_MATCH\n" + 
				"src/X.java int void X.zoo():<lambda>#2.lambda$2(int) [(x) ->] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0017() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/Function.java",
			"@FunctionalInterface\n" +
			"public interface Function<T, R> {\n" +
			"    R apply(T t);\n" +
			"}\n");
		createFile(
				"/P/src/Y.java",
				"public final class Collectors {\n" +
				" @SuppressWarnings(\"unchecked\")\n" +
				"    private static <I, R> Function<I, R> castingIdentity() {\n" +
				"        return i -> (R) i;\n" +
				"    }\n" +
				"}\n");

		IType type = getCompilationUnit("/P/src/Function.java").getType("Function");
		IMethod method = type.getMethods()[0];
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/Function.java R Function.apply(T) [apply] EXACT_MATCH\n" + 
				"src/Y.java java/lang/Object Function<I,R> Collectors.castingIdentity():<lambda>#1.lambda$1(java/lang/Object) [i ->] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0018() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/Function.java",
			"@FunctionalInterface\n" +
			"public interface Function<T, R> {\n" +
			"    R apply(T t);\n" +
			"}\n");
		createFile(
				"/P/src/Y.java",
				"public final class Collectors {\n" +
				" @SuppressWarnings(\"unchecked\")\n" +
				"    private static <I, R> Function<String, String> castingIdentity() {\n" +
				"        return i -> (R) i;\n" +
				"    }\n" +
				"}\n");

		IType type = getCompilationUnit("/P/src/Function.java").getType("Function");
		IMethod method = type.getMethods()[0];
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/Function.java R Function.apply(T) [apply] EXACT_MATCH\n" + 
				"src/Y.java java/lang/String Function<String,String> Collectors.castingIdentity():<lambda>#1.lambda$1(java/lang/String) [i ->] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
// test working copy.
public void testBug400905_0019() throws CoreException { // bad package + bad cast - make sure there is no NPE.
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/Function.java",
			"@FunctionalInterface\n" +
			"public interface Function<T, R> {\n" +
			"    R apply(T t);\n" +
			"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b400905/Collectors.java",
			"public final class Collectors {\n" +
			" @SuppressWarnings(\"unchecked\")\n" +
			"    private static <I, R> Function<String, String> castingIdentity() {\n" +
			"        return i -> (R) i;\n" +
			"    }\n" +
			"}\n") ;
	IType type = this.workingCopies[0].getType("Function");
	IMethod method = type.getMethods()[0];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
	assertSearchResults("src/b400905/Function.java R b400905.Function.apply(T) [apply] EXACT_MATCH");
}
// test working copy (dirty), expect only focus type
public void testBug400905_0020() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Function.java",
			"@FunctionalInterface\n" +
			"public interface Function<T, R> {\n" +
			"    R apply(T t);\n" +
			"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/Collectors.java",
			"public final class Collectors {\n" +
			"    private static <I, R> Function<I, R> castingIdentity() {\n" +
			"        return i -> (R) i;\n" +
			"    }\n" +
			"}\n") ;
	IType type = this.workingCopies[0].getType("Function");
	IMethod method = type.getMethods()[0];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
	assertSearchResults("src/Function.java R Function.apply(T) [apply] EXACT_MATCH");
}
// test working copy after commit, expect focus type + other matches.
public void testBug400905_0021() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p400905/Function.java",
			"package p400905;\n" +
			"public interface Function<T, R> {\n" +
			"    R apply(T t);\n" +
			"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p400905/Collectors.java",
			"package p400905;\n" +
			"public final class Collectors {\n" +
			"    private static <I, R> Function<I, R> castingIdentity() {\n" +
			"        return i -> (R) i;\n" +
			"    }\n" +
			"}\n") ;
	
	IPath path = new Path("/JavaSearchBugs/src/p400905");
	try {
		createFolder(path);
		this.workingCopies[0].commitWorkingCopy(true, null);
		this.workingCopies[1].commitWorkingCopy(true, null);

		IType type = this.workingCopies[0].getType("Function");
		IMethod method = type.getMethods()[0];
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/p400905/Function.java R p400905.Function.apply(T) [apply] EXACT_MATCH\n" + 
				"src/p400905/Collectors.java java/lang/Object Function<I,R> p400905.Collectors.castingIdentity():<lambda>#1.lambda$1(java/lang/Object) [i ->] EXACT_MATCH");
	} finally { 
		deleteFolder(path);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905#c35
public void testBug400905_0022() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"interface FunctionalInterface {\n" +
			"	int thrice(int x);\n" +
			"}\n" +
			"interface J {\n" +
			"	int twice(int x);\n" +
			"}\n" +
			"public class X {\n" +
			"	FunctionalInterface i = (x) -> {return x * 3;}; \n" +
			"	X x = null;\n" +
			"	static void goo(FunctionalInterface i) {} \n" +
			"} \n"
	);
	search("thrice", METHOD, DECLARATIONS, ERASURE_RULE, getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
			"src/test/Test.java int test.FunctionalInterface.thrice(int) [thrice] EXACT_MATCH\n" + 
			"src/test/Test.java int test.X.i:<lambda>#1.lambda$1(int) [(x) ->] EXACT_MATCH"
	);
}
public void testBug400905_0023() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"interface I { \n" +
			"	int thrice(int x);\n" +
			"}\n" +
			"interface J {\n" +
			"	int twice(int x);\n" +
			"}\n" +
			"public class X {\n" +
			"	I i = (x) /* field */ -> {return x * 3;}; \n" +
			"	X x = null;\n" +
			"	static void goo(I i) {} \n" +
			"	public static void main(String[] args) { \n" +
			"			goo((x) /*call*/ -> { \n" +
			"				int y = 3;\n" +
			"				return x * y; \n" +
			"			});\n" +
			"		I i2 = (x) /* local */ -> {\n" +
			"			int y = 3; \n" +
			"			return x * y;\n" +
			"		};\n" +
			"		J j1 = (x) -> { \n" +
			"			int y = 2;  \n" +
			"			return x * y;\n" +
			"		};  \n" +
			"	}\n" +
			"}\n"
	);
	search("thrice", METHOD, DECLARATIONS, ERASURE_RULE, getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
			"src/test/Test.java int test.I.thrice(int) [thrice] EXACT_MATCH\n" + 
			"src/test/Test.java int test.X.i:<lambda>#1.lambda$1(int) [(x) /* field */ ->] EXACT_MATCH\n" + 
			"src/test/Test.java int void test.X.main(String[]):<lambda>#2.lambda$2(int) [(x) /*call*/ ->] EXACT_MATCH\n" + 
			"src/test/Test.java int void test.X.main(String[]):<lambda>#3.lambda$3(int) [(x) /* local */ ->] EXACT_MATCH"
	);
}
public void testBug400905_0024() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"interface I { \n" +
			"	int thrice(int x);\n" +
			"}\n" +
			"public class X {\n" +
			"	static int goo(int x) { return 3 * x; } \n" +
			"	public static void main(String[] args) { \n" +
			"		I i = X::goo;\n" +
			"	}\n" +
			"}\n"
	);
	
	search(this.workingCopies[0].getType("X").getMethod("goo", new String[] { "I" }), THIS_REFERENCE);
	assertSearchResults("");
}
public void testBug400905_0025() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"interface I { \n" +
			"	int thrice(int p);\n" +
			"}\n" +
			"class Y {\n" +
			"	int goo(int x) { return 3 * x; } \n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public void main(String[] args) { \n" +
			"		I i = this::goo;\n" +
			"       i = super::goo;\n" +
			"	}\n" +
			"}\n"
	);
	
	search(this.workingCopies[0].getType("Y").getMethod("goo", new String[] { "I" }), THIS_REFERENCE);
	assertSearchResults("src/test/Test.java void test.X.main(String[]) [this::goo] EXACT_MATCH");
}
public void testBug400905_0026() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"interface I { \n" +
			"	int thrice(int p);\n" +
			"}\n" +
			"class Y {\n" +
			"	int goo(int x) { return 3 * x; } \n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public void main(String[] args) { \n" +
			"		I i = this::goo;\n" +
			"       i = super::goo;\n" +
			"	}\n" +
			"}\n"
	);
	
	search(this.workingCopies[0].getType("Y").getMethod("goo", new String[] { "I" }), SUPER_REFERENCE);
	assertSearchResults("src/test/Test.java void test.X.main(String[]) [super::goo] EXACT_MATCH");
}
public void testBug400905_0027() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"interface I { \n" +
			"	int thrice(int p);\n" +
			"}\n" +
			"class Y {\n" +
			"	int goo(int x) { return 3 * x; } \n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public void main(String[] args) { \n" +
			"		I i = this::goo;\n" +
			"       i = super::goo;\n" +
			"	}\n" +
			"}\n"
	);
	
	search(this.workingCopies[0].getType("Y").getMethod("goo", new String[] { "I" }), QUALIFIED_REFERENCE);
	assertSearchResults("");
}
public void testBug400905_0028() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"interface I { \n" +
			"	int thrice(int p);\n" +
			"}\n" +
			"class Y {\n" +
			"	static class Z {\n" +
			"		static int goo(int x) { return 3 * x; }   \n" +
			"		I i = Z::goo;\n" +
			"   }\n" +
			"}\n" +
			"public class X extends Y.Z {\n" +
			"	public void main(String[] args) { \n" +
			"		I i = Y.Z::goo;\n" +
			"	}\n" +
			"}\n"
	);
	
	search(this.workingCopies[0].getType("Y").getType("Z").getMethod("goo", new String[] { "I" }), THIS_REFERENCE | SUPER_REFERENCE | QUALIFIED_REFERENCE);
	assertSearchResults("src/test/Test.java void test.X.main(String[]) [Y.Z::goo] EXACT_MATCH");
}
public void testBug400905_0029() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"interface I { \n" +
			"	int thrice(int p);\n" +
			"}\n" +
			"class Y {\n" +
			"	static class Z {\n" +
			"		static int goo(int x) { return 3 * x; }   \n" +
			"		I i = Z::goo;\n" +
			"   }\n" +
			"}\n" +
			"public class X extends Y.Z {\n" +
			"	public void main(String[] args) { \n" +
			"		I i = Y.Z::goo;\n" +
			"	}\n" +
			"}\n"
	);
	
	search(this.workingCopies[0].getType("Y").getType("Z").getMethod("goo", new String[] { "I" }), REFERENCES, EXACT_RULE);
	assertSearchResults("src/test/Test.java test.Y$Z.i [Z::goo] EXACT_MATCH\n" + 
			"src/test/Test.java void test.X.main(String[]) [Y.Z::goo] EXACT_MATCH");
}
public void testBug400905_0030() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"interface I { \n" +
			"	int thrice(int p);\n" +
			"}\n" +
			"class Y {\n" +
			"	static class Z {\n" +
			"		static int goo(int x) { return 3 * x; }   \n" +
			"		I i = Z::goo;\n" +
			"   }\n" +
			"}\n" +
			"public class X extends Y.Z {\n" +
			"	public void main(String[] args) { \n" +
			"		I i = Y.Z::goo;\n" +
			"	}\n" +
			"}\n"
	);
	
	search(this.workingCopies[0].getType("Y").getType("Z").getMethod("goo", new String[] { "I" }), IMPLICIT_THIS_REFERENCE);
	assertSearchResults("");
}
// Add new tests in JavaSearchBugs8Tests
}

