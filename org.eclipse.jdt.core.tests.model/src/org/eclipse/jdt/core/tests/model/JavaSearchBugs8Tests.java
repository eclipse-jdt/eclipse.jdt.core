/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
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
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.LambdaMethod;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.matching.AndPattern;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;

import junit.framework.Test;

/**
 * Non-regression tests for bugs fixed in Java Search engine.
 */
public class JavaSearchBugs8Tests extends AbstractJavaSearchTests {

	static {
//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	TESTS_NAMES = new String[] {"testBug493433"};
}

public JavaSearchBugs8Tests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchBugs8Tests.class, BYTECODE_DECLARATION_ORDER);
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
	JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "1.8");
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

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 *	FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g1() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    @Marker int x;
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * TYPE:   MethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g2() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    @Marker <T> int x() { return 10; };
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * FormalParameter ::= Modifiersopt Type VariableDeclaratorIdOrThis
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g3() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    int x(@Marker int p) { return 10; };
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * FormalParameter ::= Modifiersopt Type PushZeroTypeAnnotations '...' VariableDeclaratorIdOrThis
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g4() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    int x(@Marker int ... p) { return 10; };
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * FormalParameter ::= Modifiersopt Type @308... TypeAnnotations '...' VariableDeclaratorIdOrThis
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g5() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    int x(@Marker int [] @Marker ... p) { return 10; };
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * UnionType ::= Type
 * UnionType ::= UnionType '|' Type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g6() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    int x() {
			        try {
			        } catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {
			        }
			        return 10;
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators
 * LocalVariableDeclaration ::= Modifiers Type PushRealModifiers VariableDeclarators
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g7() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    int x() {
			        @Marker int p;
			        final @Marker int q;
			        @Marker final int r;
			        return 10;
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH
			src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH
			src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following:
 * Resource ::= Type PushModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
 * Resource ::= Modifiers Type PushRealModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g8() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    int x() {
			        try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {
			        }
			        return 10;
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH
			src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH
			src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following:
 * EnhancedForStatementHeaderInit ::= 'for' '(' Type PushModifiers Identifier Dimsopt
 * EnhancedForStatementHeaderInit ::= 'for' '(' Modifiers Type PushRealModifiers Identifier Dimsopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g9() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    int x() {
			        for (@Marker int i: new int[3]) {}
			        for (final @Marker int i: new int[3]) {}
			        for (@Marker final int i: new int[3]) {}
			        return 10;
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH
			src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH
			src/b400899/X.java int b400899.X.x() [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * AnnotationMethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
 * AnnotationMethodHeaderName ::= Modifiersopt Type 'Identifier' '('
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g10() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public @interface X {\s
				public @Marker String value();\s
				@Marker String value2();\s
				@Marker public String value3();\s
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java String b400899.X.value() [Marker] EXACT_MATCH
			src/b400899/X.java String b400899.X.value2() [Marker] EXACT_MATCH
			src/b400899/X.java String b400899.X.value3() [Marker] EXACT_MATCH"""
);
}


/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ReferenceExpression ::= PrimitiveType Dims '::' NonWildTypeArgumentsopt IdentifierOrNew
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g12() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			interface I {
			    Object copy(int [] ia);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = @Marker int @Marker []::<String>clone;
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ArrayCreationWithoutArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs
 * ArrayCreationWithArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g13() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X  {
			    public static void main(String [] args) {
			        int i [] = new @Marker int @Marker [4];
			        int j [] = new @Marker int @Marker [] { 10 };
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN InsideCastExpression UnaryExpression
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g14() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X  {
			    public static void main(String [] args) {
			        int i = (@Marker int) 0;
			        int j [] = (@Marker int @Marker []) null;
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * InstanceofExpression ::= InstanceofExpression 'instanceof' ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g15() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X  {
			    public static void main(String [] args) {
			        if (args instanceof @Marker String[]) {
			        }
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * TypeArgument ::= ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g16() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X extends Y<@Marker Integer, String> {}
			class Y<T, V> {
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ReferenceType1 ::= ReferenceType '>'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g17() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X extends Y<@Marker Integer> {}
			class Y<T> {
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ReferenceType2 ::= ReferenceType '>>'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g18() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X<T extends @Marker Object & @Marker Comparable<@Marker ? super @Marker String>> {}
			class Y<T> {
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java b400899.X [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * WildcardBounds ::= 'extends' ReferenceType
 * WildcardBounds ::= 'super' ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g20() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			import java.util.Map;
			public class X {
				void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}
			   void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java void b400899.X.foo(Map<? super Object,? extends String>) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.foo(Map<? super Object,? extends String>) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.foo(Map<? super Object,? extends String>) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.foo(Map<? super Object,? extends String>) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.goo(Map<? extends Object,? super String>) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.goo(Map<? extends Object,? super String>) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.goo(Map<? extends Object,? super String>) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.goo(Map<? extends Object,? super String>) [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * <pre>{@code
 * TypeParameter ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList
 * AdditionalBound ::= '&' ReferenceType
 * TypeParameter1 ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList1
 * }</pre>
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g22() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public interface X<U extends J<? extends X<U>>> {
			}
			interface J<T extends X<? extends J<T>>> {
			}
			class CI<U extends CJ<T, U> & @Marker J<@Marker T>,
						T extends CI<U, T> & @Marker X<U>>
				implements X<U> {
			}
			class CJ<T extends CI<U, T> & @Marker X<@Marker U>,
						U extends CJ<T, U> & J<T>>
				implements J<T> {
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java b400899.CI [Marker] EXACT_MATCH
			src/b400899/X.java b400899.CI [Marker] EXACT_MATCH
			src/b400899/X.java b400899.CI [Marker] EXACT_MATCH
			src/b400899/X.java b400899.CJ [Marker] EXACT_MATCH
			src/b400899/X.java b400899.CJ [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * InstanceofExpression_NotName ::= Name 'instanceof' ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g23() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X<E> {
			  class Y {
			    E e;
			    E getOtherElement(Object other) {
			      if (!(other instanceof @Marker X<?>.Y)) {};
			      return null;
			    }
			  }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * InstanceofExpression_NotName ::= InstanceofExpression_NotName 'instanceof' ReferenceType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g24() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X<P, C> {
			  public X() {
			    if (!(this instanceof @Marker X)) {}
			  }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ReferenceExpressionTypeArgumentsAndTrunk ::= OnlyTypeArguments '.' ClassOrInterfaceType Dimsopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g25() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			package b400899;
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
				public class Z <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker CharSequence> {
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			interface Y<U> {}
			"""
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
		"""
			src/b400899/X.java b400899.X$Z [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X$Z [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X$Z [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X$Z [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X$Z [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ArrayCreationWithoutArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs
 * ArrayCreationWithArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g26() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    public static void main(String [] args) {
			        X [] x = new @Marker X @Marker [5];
			        X [] x2 = new @Marker X @Marker [] { null };
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression '.' ClassOrInterfaceType Dimsopt PushRPAREN InsideCastExpressionWithQualifiedGenerics UnaryExpressionNotPlusMinus
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g27() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    public static void main(String [] args) {
			        java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * <pre>{@code
 * ReferenceType1 ::= ClassOrInterface '<' TypeArgumentList2
 * }</pre>
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g28() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			import java.io.Serializable;
			import java.util.List;
			public class X<T extends Comparable<T> & Serializable> {
				void foo(List<? extends @Marker Comparable<T>> p) {}\s
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * <pre>{@code
 * ReferenceType2 ::= ClassOrInterface '<' TypeArgumentList3
 * }</pre>
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g29() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			class Base {
			}
			class Foo<U extends Base, V extends Bar<U, @Marker Foo<U, V>>> {
			}
			class Bar<E extends Base, F extends Foo<E, @Marker Bar<E, F>>> {
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ClassHeaderExtends ::= 'extends' ClassType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g30() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X extends @Marker Object {
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ClassInstanceCreationExpression ::= 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' UnqualifiedClassBodyopt
 * ClassInstanceCreationExpression ::= 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' UnqualifiedClassBodyopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g31() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    X x = new @Marker X();
			    X y = new <String> @Marker X();
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ClassInstanceCreationExpression ::= Primary '.' 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
 * ClassInstanceCreationExpression ::= Primary '.' 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g32() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    class Y {
			    }
			    Y y1 = new @Marker X().new @Marker Y();
			    Y y2 = new @Marker X().new <String> @Marker Y();
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java b400899.X.y1 [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X.y1 [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X.y2 [Marker] POTENTIAL_MATCH
			src/b400899/X.java b400899.X.y2 [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g33() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    X x;
			    class Y {
			    }
			    Y y1 = x.new @Marker Y();
			    Y y2 = x.new <String> @Marker Y();
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * MethodHeaderThrowsClause ::= 'throws' ClassTypeList
 * ClassTypeList -> ClassTypeElt
 * ClassTypeList ::= ClassTypeList ',' ClassTypeElt
 * ClassTypeElt ::= ClassType
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g34() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    void foo() throws @Marker NullPointerException, @Marker ArrayIndexOutOfBoundsException {}
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
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
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			interface I {}
			interface J {}
			interface K extends @Marker I, @Marker J {}
			interface L {}
			public class X implements @Marker K, @Marker L {
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java b400899.K [Marker] EXACT_MATCH
			src/b400899/X.java b400899.K [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ReferenceExpression ::= Name Dimsopt '::' NonWildTypeArgumentsopt IdentifierOrNew
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g36() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			interface I {
			    void foo(int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;
			        i.foo(10);\s
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * ReferenceExpression ::= Name BeginTypeArguments ReferenceExpressionTypeArgumentsAndTrunk '::' NonWildTypeArgumentsopt IdentifierOrNew
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g37() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			interface I {
			    Y foo(int x);
			}
			public class X  {
			    class Z extends Y {
			        public Z(int x) {
			            super(x);
			            System.out.println();
			        }
			    }
			    public static void main(String [] args) {
			        i = @Marker W<@Marker Integer>::<@Marker String> new;
			    }
			}
			class W<T> extends Y {
			    public W(T x) {
			        super(0);
			        System.out.println(x);
			    }
			}
			class Y {
			    public Y(int x) {
			        System.out.println(x);
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH
			src/b400899/X.java void b400899.X.main(String[]) [Marker] EXACT_MATCH"""
);
}

/**
 * bug 400899:  [1.8][search] Search engine/indexer should evolve to support Java 8 constructs
 * test Ensures that the search for type use annotation finds matches in the following
 * CastExpression ::= PushLPAREN Name PushRPAREN InsideCastExpressionLL1 UnaryExpressionNotPlusMinus
 * CastExpression ::= PushLPAREN Name Dims PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
 * CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression Dimsopt PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
 * CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression '.' ClassOrInterfaceType Dimsopt PushRPAREN InsideCastExpressionWithQualifiedGenerics UnaryExpressionNotPlusMinus
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400899"
 */
public void testBug400899g38() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400899/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			public class X {
			    Object o = (@Marker X) null;
			    Object p = (@Marker X @Marker []) null;
			    Object q = (java.util. @Marker List<@Marker String> []) null;
			    Object r = (java.util.Map.@Marker Entry @Marker []) null;
			}
			@Target(ElementType.TYPE_USE)
			@interface Marker {}
			"""
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
		"""
			src/b400899/X.java b400899.X.o [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X.p [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X.p [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X.q [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X.q [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X.r [Marker] EXACT_MATCH
			src/b400899/X.java b400899.X.r [Marker] EXACT_MATCH"""
);
}

/**
	 * bug402902:  [1.8][search] Search engine fails to annotation matches in extends/implements clauses
	 * test Ensures that the search for type use annotation finds matches
	 * in extends and implements clauses.
	 *
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=402902"
	 */
public void testBug400902() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400902/X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				import java.io.Serializable;
				@Marker1 @Marker public class X extends @Marker Object implements @Marker Serializable {
					private static final long serialVersionUID = 1L;
					int x = (@Marker int) 0;
				 	@Marker public class Y {}
				}
				@Target(ElementType.TYPE_USE)
				@interface Marker {}
				@Target(ElementType.TYPE)
				@interface Marker1 {}"""
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
			"""
				src/b400902/X.java b400902.X [Marker] EXACT_MATCH
				src/b400902/X.java b400902.X [Marker] EXACT_MATCH
				src/b400902/X.java b400902.X [Marker] EXACT_MATCH
				src/b400902/X.java b400902.X.x [Marker] EXACT_MATCH
				src/b400902/X.java b400902.X$Y [Marker] EXACT_MATCH"""
	);
}

/**
 * bug 424119:  [1.8][search] CCE in search for references to TYPE_USE annotation on array dimension
 * test Ensures that the search for type use annotation finds matches
 * in local variable declaration dimensions.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=424119"
 */
public void testBug424119_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b424119/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			import java.io.Serializable;
			public class X{
			{
				String tab @Annot() [] = null;
			}
			public void foo() {
				String t @Annot() [] @Annot()[] = null, s @Annot() [];
			}
			String tab @Annot() [] = null;
			@Target(ElementType.TYPE_USE)
			@interface Annot {}
			"""
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
		"""
			src/b424119/X.java b424119.X.{} [Annot] EXACT_MATCH
			src/b424119/X.java b424119.X.tab [Annot] EXACT_MATCH
			src/b424119/X.java void b424119.X.foo() [Annot] EXACT_MATCH
			src/b424119/X.java void b424119.X.foo() [Annot] EXACT_MATCH
			src/b424119/X.java void b424119.X.foo() [Annot] EXACT_MATCH"""
	);
}

/**
 * bug 424119:  [1.8][search] CCE in search for references to TYPE_USE annotation on array dimension
 * test Ensures that the search for type use single variable annotation finds matches
 * in local variable declaration inside initializer - ref bug 424119 comment 1
 * - checks for non-existence of CCE.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=424119"
 */
public void testBug424119_002() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b424119/X.java",
		"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Target;
			import java.io.Serializable;
			public class X{
			{
				String tab @Annot(int[].class) [] = null;
			}
			@Target(ElementType.TYPE_USE)
			@interface Annot {
				Class<int[]> value();
			}
			"""
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
 * bug 424119:  [1.8][search] CCE in search for references to TYPE_USE annotation on array dimension
 * test Ensures that the search for type use single variable annotation finds matches
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
			"""
				public class X {
					String tab @Annot(int[].class) [] = null;
				}
				""");
		createFile(
			"P1/src/Annot.java",
			"""
				@Target(ElementType.TYPE_USE)
				@interface Annot {
					Class<int[]> value();
				}"""
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
 * bug 427537:  [1.8][search] CCE with search match location set to cast type and intersection casts
 * test Ensures that the search for type use annotation does not cause CCE and returns correct results
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=427537"
 */
public void testBug427537a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b427537/X.java",
			"""
				interface I {
					void foo();
				}
				interface J {}
				public class X {
					public static void main(String[] args) {
						I i = (I & J) () -> {};
						i.foo();
						I j = (J & I) () -> {};
						j.foo();
					}
				}
				"""
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
 * bug 427677: [1.8][search] NPE in MatchLocator.reportMatching with unresolved NameQualifiedType qualifier
 * test test
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=427677"
 */
public void testBug427677() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b427677/X.java",
			"""
				import java.lang.annotation.*;\s
				class X implements unresolved. @Marker1 Collection<Integer> { }\s
				@Target (ElementType.TYPE_USE)\s
				@interface Marker1 {}"""
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
 * bug 400904
 * test tests search for Reference expression - super:: form, without type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X extends Y {
				    public static void main(String [] args) {
					new X().doit();
				    }
				    void doit() {
				        I i = super::foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				    public void foo(int x) {
					System.out.println(x);
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.doit() [foo] EXACT_MATCH"
	);
}

public void testBug400904_0001a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X extends Y {
				    public static void main(String [] args) {
					new X().doit();
				    }
				    void doit() {
				        I i = super::foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				    public void foo(int x) {
					System.out.println(x);
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, SUPER_REFERENCE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.doit() [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - super:: form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0002() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X extends Y {
				    public static void main(String [] args) {
					new X().doit();
				    }
				    void doit() {
				        I i = super::<String>foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				    public void foo(int x) {
					System.out.println(x);
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.doit() [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - SimpleName:: form, without type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0003() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
					public void doit();
				}
				class Y {
					Y() {}
					Y(int i) {}
				}
				
				public class X {
				    X(int i) {}\s
				   static void foo() {}
				   static void foo(int i) {}
					I i = X :: foo;
					I j = Y :: new;
				   public static void main() {\s
				     Y y = new Y();\s
				     foo();\s
				   }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", null);
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java b400904.X.i [foo] EXACT_MATCH\n" +
			"src/b400904/X.java void b400904.X.main() [foo()] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - SimpleName:: form, with type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0004() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y::<String>foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				    public static void foo(int x) {
					System.out.println(x);
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - QualifiedName:: form, without type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0005() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y.Z::foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				    static class Z {
				        public static void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y").getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - QualifiedName:: form, with type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0006() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y.Z::<String>foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				    static class Z {
				        public static void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y").getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - Primary:: form, without type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0007() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = new Y()::foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				        void foo(int x) {
					    System.out.println(x);
				        }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - Primary:: form, with type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0008() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = new Y()::<String>foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				        void foo(int x) {
					    System.out.println(x);
				        }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - {@code X<T>::} form, without type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0009() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				  void foo(Y<String> y, int x);
				}
				public class X {
				  public X() {
				    super();
				  }
				  public static void main(String[] args) {
				    I i = Y<String>::foo;
				    i.foo(new Y<String>(), 10);
				  }
				}
				class Y<T> {
				  Y() {
				    super();
				  }
				  void foo(int x) {
				    System.out.println(x);
				  }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}


/**
 * bug 400904
 * test tests search for Reference expression - {@code X<T>::} form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0010() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				  void foo(Y<String> y, int x);
				}
				public class X {
				  public X() {
				    super();
				  }
				  public static void main(String[] args) {
				    I i = Y<String>::<String>foo;
				    i.foo(new Y<String>(), 10);
				  }
				}
				class Y<T> {
				  Y() {
				    super();
				  }
				  void foo(int x) {
				    System.out.println(x);
				  }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - {@code X<T>.Name ::} form, without type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0011() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(Y<String>.Z z, int x);
				}
				public class X  {
					@SuppressWarnings("unused")
				    public static void main(String [] args) {
				        I i = Y<String>.Z::foo;
				        i.foo(new Y<String>().new Z(), 10);\s
				    }
				}
				class Y<T> {
				    class Z {
				        void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - {@code X<T>.Name ::} form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0012() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(Y<String>.Z z, int x);
				}
				public class X  {
					@SuppressWarnings("unused")
				    public static void main(String [] args) {
				        I i = Y<String>.Z::<String>foo;
				        i.foo(new Y<String>().new Z(), 10);\s
				    }
				}
				class Y<T> {
				    class Z {
				        void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - {@code X<T>.Y<K> ::} form, without type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0013() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(Y<String>.Z<String> z, int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y<String>.Z<String>::foo;
				        i.foo(new Y<String>().new Z<String>(), 10);\s
				    }
				}
				class Y<T> {
				    class Z<K> {
				        void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - {@code X<T>.Y<K> ::} form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0014() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(Y<String>.Z<String> z, int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y<String>.Z<String>::<String>foo;
				        i.foo(new Y<String>().new Z<String>(), 10);\s
				    }
				}
				class Y<T> {
				    class Z<K> {
				        void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
			"src/b400904/X.java void b400904.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 400904
 * test tests search for Reference expression - {@code X<T>.Y<K> ::} new form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400904"
 */
public void testBug400904_0015() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400904/X.java",
			"""
				interface I {
				    void foo(Y<String> y);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y<String>.Z<String>::<String>new;
				        i.foo(new Y<String>());\s
				    }
				}
				class Y<T> {
				    class Z<K> {
				        Z(Y<String> y) {
				            System.out.println("Y<T>.Z<K>::new");
				        }
				        Z1(Y<String> y) {
				            System.out.println("Y<T>.Z<K>::new");
				        }
				    }
				}
				"""
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
 * bug 400905
 * test lambda expression search on a) field b)parameter
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 */
public void testBug400905_0001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/X.java",
			"""
				interface I {
				    int foo();
				}
				public class X extends Y {
				    public static void main(String [] args) {
					     I i = () -> 42;
				    }
					public void bar(I i) {}
				 	public void doit() {
						bar(() ->1);
					}
				}
				"""
	);
	IType type = this.workingCopies[0].getType("I");
	IMethod method = type.getMethod("foo", new String[] {});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"""
				src/b400905/X.java int b400905.I.foo() [foo] EXACT_MATCH
				src/b400905/X.java int void b400905.X.main(String[]):<lambda #1>.foo() [() ->] EXACT_MATCH
				src/b400905/X.java int void b400905.X.doit():<lambda #1>.foo() [() ->] EXACT_MATCH""");
}
/**
 * bug 400905
 * test  lambda expression search on a set of contexts with
 * interface declaration and usage being in different files.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
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
			"""
				public interface I {
				    int foo();
				}
				""") ;
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b400905/Y.java",
			"""
				public class Y {
				    void goo(I i) {};
				}
				""") ;
	IType type = this.workingCopies[1].getType("I");
	IMethod method = type.getMethod("foo", new String[] {});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"""
				src/b400905/I.java int b400905.I.foo() [foo] EXACT_MATCH
				src/b400905/X.java int void b400905.X.main(String[]):<lambda #1>.foo() [() ->] EXACT_MATCH
				src/b400905/X.java int void b400905.X.main(String[]):<lambda #1>.foo() [()  ->] EXACT_MATCH
				src/b400905/X.java int void b400905.X.main(String[]):<lambda #1>.foo() [()->] EXACT_MATCH
				src/b400905/X.java int I b400905.X.bar():<lambda #1>.foo() [() ->] EXACT_MATCH""");
}
/**
 * bug 400905
 * test  lambda expression search on a set of contexts with the
 * interface declaration and usage being in different files.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
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
			"""
				public interface I {
				    boolean foo(Y y);
				}
				""") ;
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b400905/Y.java",
			"""
				public class Y {
				    public boolean exists() { return true};
				    public boolean canRead() { return true};
				    public boolean canWrite() { return true};
				}
				""") ;
	IType type = this.workingCopies[1].getType("I");
	IMethod method = type.getMethod("foo", new String[] {"QY;"});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"""
				src/b400905/I.java boolean b400905.I.foo(Y) [foo] EXACT_MATCH
				src/b400905/X.java boolean void b400905.X.main(String[]):<lambda #1>.foo(b400905.Y) [y->] EXACT_MATCH
				src/b400905/X.java boolean void b400905.X.main(String[]):<lambda #1>.foo(b400905.Y) [y ->] EXACT_MATCH
				src/b400905/X.java boolean void b400905.X.main(String[]):<lambda #1>.foo(b400905.Y) [y  ->] EXACT_MATCH"""
	);
}
/**
 * bug 400905
 * test  lambda expression search on a set of contexts with the
 * interface declaration and usage being in different files.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
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
			"""
				public interface I<T> {
				    public T foo();
				}
				""") ;
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b400905/Y.java",
			"""
				public interface Y {
				    public abstract void run() { };
				}
				""") ;
	IType type = this.workingCopies[1].getType("I");
	IMethod method = type.getMethod("foo", new String[] {});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
					"""
						src/b400905/I.java T b400905.I.foo() [foo] EXACT_MATCH
						src/b400905/X.java b400905.Y void b400905.X.main(String[]):<lambda #1>.foo() [() /* foo */ ->] EXACT_MATCH
						src/b400905/X.java b400905.Y void b400905.X.main(String[]):<lambda #1>.foo() [(() /* true */->] EXACT_MATCH
						src/b400905/X.java b400905.Y void b400905.X.main(String[]):<lambda #1>.foo() [(() /* false */ ->] EXACT_MATCH
						src/b400905/X.java java.lang.Object void b400905.X.main(String[]):<lambda #1>.foo() [() /* cast */ ->] EXACT_MATCH"""
	);
}
/**
 * bug 400905
 * test  lambda expression search on a set of contexts with the
 * interface declaration and usage being in different files.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
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
			"""
				public interface I<T> {
				    public T foo();
				}
				""") ;
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b400905/Y.java",
			"""
				public interface Y {
				    public abstract void bar() { };
				}
				""") ;

	IType type = this.workingCopies[2].getType("Y");
	IMethod method = type.getMethod("bar", new String[] {});
	search(method, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
					"src/b400905/X.java void b400905.Y void b400905.X.main(String[]):<lambda #1>.foo():<lambda #1>.bar() [() /* bar */ ->] EXACT_MATCH\n" +
					"src/b400905/Y.java void b400905.Y.bar() [bar] EXACT_MATCH"
	);
}
/**
 * bug 400905
 * test  lambda expression search on a set of contexts with the
 * interface declaration and usage being in different files.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 */
public void testBug400905_0006() throws CoreException {
	boolean indexState = isIndexDisabledForTest();
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		this.indexDisabledForTest = true;
		createFile(
			"/P/src/X.java",
			"""
				public class X {
				    public static void main(String [] args) {
						Y.goo(()->{});
					}
				}
				"""
		);
		createFile(
			"/P/src/Y.java",
			"""
				public class Y {
				    public static void goo(I i) {};
				}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public void foo();
				}
				"""
		);
		JavaModelManager.getIndexManager().waitForIndex(true, null);
		IMethod method = getCompilationUnit("/P/src/I.java").getType("I").getMethod("foo", new String[0]);
		search(method, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults(
						"src/I.java void I.foo() [foo] EXACT_MATCH\n" +
						"src/X.java void void X.main(String[]):<lambda #1>.foo() [()->] EXACT_MATCH"
		);
	}
	finally {
		deleteProject("P");
		this.indexDisabledForTest = indexState;
	}
}

/**
 * bug 400905
 * test  lambda expression search on a set of contexts with the
 * interface declaration and usage being in different files.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905"
 */
public void testBug400905_0007() throws CoreException {
	boolean indexState = isIndexDisabledForTest();
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		this.indexDisabledForTest = true;
		createFile(
			"/P/src/X.java",
			"""
				public class X  {
				    void foo() {
				        I i = Y::new;
				    }
				}
				"""
		);
		createFile(
			"/P/src/Y.java",
			"""
				public class Y extends X {
				    Y(int x) {};
				}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				interface I {
				    X foo(int x);
				}
				"""
		);
		JavaModelManager.getIndexManager().waitForIndex(true, null);
		IMethod method = getCompilationUnit("/P/src/Y.java").getType("Y").getMethod("Y", new String[] {"I"});
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults("src/X.java void X.foo() [Y::new] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
		this.indexDisabledForTest = indexState;
	}
}
public void testBug400905_0007a() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"""
				public class X  {
				    void foo() {
				        I i = Y::new;
				    }
				}
				"""
		);
		createFile(
			"/P/src/Y.java",
			"""
				public class Y extends X {
				    Y(int x) {};
				    Y() {};
				}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				interface I {
				    X foo();
				}
				"""
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
	boolean indexState = isIndexDisabledForTest();
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		this.indexDisabledForTest = true;
		createFile(
			"/P/src/X.java",
			"""
				public class X {
				    public static void main(String [] args) {
						I i = Y::goo;
					}
				}
				"""
		);
		createFile(
			"/P/src/Y.java",
			"""
				public class Y {
				    public static void goo() {};
				}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public void foo();
				}
				"""
		);
		JavaModelManager.getIndexManager().waitForIndex(true, null);
		IMethod method = getCompilationUnit("/P/src/Y.java").getType("Y").getMethod("goo", new String[0]);
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults("src/X.java void X.main(String[]) [goo] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
		this.indexDisabledForTest = indexState;
	}
}
public void testBug400905_0009() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"""
				public class X {
				    public static void main(String [] args) {
						I i = Y::goo;
					}
				}
				"""
		);
		createFile(
			"/P/src/Y.java",
			"""
				public class Y {
				    public static void goo() {};
				    public static void goo(int x) {};
				}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public void foo();
				}
				"""
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
	boolean indexState = isIndexDisabledForTest();
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		this.indexDisabledForTest = true;
		createFile(
			"/P/src/J.java",
			"""
				public interface J {
				    public static void main(String [] args) {
						I i = Y::goo;
					}
				    default void foo() {
				       I i = Y::goo;
				       Y.goo(()->{});
				   }
				}
				"""
		);
		createFile(
			"/P/src/Y.java",
			"""
				public class Y {
				    public static void goo(I i) {};
				    public static void goo() {};
				}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public void foo();
				}
				"""
		);
		JavaModelManager.getIndexManager().waitForIndex(true, null);
		IMethod method = getCompilationUnit("/P/src/Y.java").getType("Y").getMethod("goo", new String[0]);
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults("src/J.java void J.main(String[]) [goo] EXACT_MATCH\n" +
				"src/J.java void J.foo() [goo] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
		this.indexDisabledForTest = indexState;
	}
}
public void testBug400905_0011() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/J.java",
			"""
				public interface J {
				    public static void main(String [] args) {
						I i = Y::goo;
					}
				    default void foo() {
				       I i = Y::goo;
				       Y.goo(()->{});
				   }
				}
				"""
		);
		createFile(
			"/P/src/Y.java",
			"""
				public class Y {
				    public static void goo(I i) {};
				    public static void goo() {};
				}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public void foo();
				}
				"""
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
			"""
				public class X {
					static void foo() {}
					I i = () -> {};
					I i2 = new I() {
						public void doit() {
						\t
						}
					};
				}
				 class Y {}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public void doit();
				}
				"""
		);

		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[0]);
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("""
			src/I.java void I.doit() [doit] EXACT_MATCH
			src/X.java void X.i:<lambda #1>.doit() [() ->] EXACT_MATCH
			src/X.java void X.i2:<anonymous>#1.doit() [doit] EXACT_MATCH""");
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
			"""
				public class X {
				   void zoo() {
					    I i = () -> 0;
					    I i2 = new I() {
						    public int doit() {
							    return 0;
						    }
					    };
				   }
				}
				 class Y {}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit();
				}
				"""
		);

		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[0]);
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("""
			src/I.java int I.doit() [doit] EXACT_MATCH
			src/X.java int void X.zoo():<lambda #1>.doit() [() ->] EXACT_MATCH
			src/X.java int void X.zoo():<anonymous>#1.doit() [doit] EXACT_MATCH""");
	}
	finally {
		deleteProject("P");
	}
}
// verify that nested lambdas are found and they are linked properly.
public void testBug400905_0013a() throws CoreException {
	boolean indexState = isIndexDisabledForTest();
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		this.indexDisabledForTest = true;
		createFile(
			"/P/src/X.java",
			"""
				public class X {
				   void zoo() {
					    I i = () /*1*/-> {
				                 I i2 = () /*2*/-> 10;
				                 return 0;
				       };
				   }
				}
				 class Y {}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit();
				}
				"""
		);

		JavaModelManager.getIndexManager().waitForIndex(true, null);
		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[0]);
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("""
			src/I.java int I.doit() [doit] EXACT_MATCH
			src/X.java int void X.zoo():<lambda #1>.doit() [() /*1*/->] EXACT_MATCH
			src/X.java int int void X.zoo():<lambda #1>.doit():<lambda #1>.doit() [() /*2*/->] EXACT_MATCH""");
	}
	finally {
		deleteProject("P");
		this.indexDisabledForTest = indexState;
	}
}
// Verify that nested lambdas are found and they are linked properly. (hierarchy scope)
public void testBug400905_0013b() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"""
				public class X {
				   void zoo() {
					    I i = (X x2) /*1*/-> {
				                 I i2 = (X x3) /*2*/-> 10;
				                 return 0;
				       };
				   }
				}
				 class Y {}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit(X x1);
				}
				"""
		);

		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/X.java int int void X.zoo():<lambda #1>.doit(X):<lambda #1>.doit(X) [X] EXACT_MATCH\n" +
				"src/X.java int void X.zoo():<lambda #1>.doit(X) [X] EXACT_MATCH");
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
			"""
				public class X {
				   void zoo() {
					    I i = (X x2) /*1*/-> {
				                 I i2 = (X x3) /*2*/-> 10;
				                 return 0;
				       };
				   }
				}
				 class Y {}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit(X x1);
				}
				"""
		);

		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchEngine.createJavaSearchScope(new IJavaElement[] {type}), this.resultCollector);
		assertSearchResults("src/X.java int int void X.zoo():<lambda #1>.doit(X):<lambda #1>.doit(X) [X] EXACT_MATCH\n" +
				"src/X.java int void X.zoo():<lambda #1>.doit(X) [X] EXACT_MATCH");
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
			"""
				public class X {
				   void zoo() {
					    I i = (X x2) /*1*/-> {
				                 I i2 = (X x3) /*2*/-> 10;
				                 return 0;
				       };
				   }
				}
				 class Y {}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit(X x1);
				}
				"""
		);

		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchEngine.createJavaSearchScope(new IJavaElement[] {project}), this.resultCollector);
		assertSearchResults("""
			src/I.java int I.doit(X) [X] EXACT_MATCH
			src/X.java int int void X.zoo():<lambda #1>.doit(X):<lambda #1>.doit(X) [X] EXACT_MATCH
			src/X.java int void X.zoo():<lambda #1>.doit(X) [X] EXACT_MATCH""");
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
			"""
				public class X {
				   void zoo() {
					    I i = (X x2) /*1*/-> {
				                 I i2 = (X x3) /*2*/-> 10;
				                 class Q {
				                     X x;
				                 }
				                 return 0;
				       };
				   }
				}
				 class Y {}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit(X x1);
				}
				"""
		);

		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchEngine.createJavaSearchScope(new IJavaElement[] {project}), this.resultCollector);
		assertSearchResults("""
			src/I.java int I.doit(X) [X] EXACT_MATCH
			src/X.java int int void X.zoo():<lambda #1>.doit(X):<lambda #1>.doit(X) [X] EXACT_MATCH
			src/X.java int void X.zoo():<lambda #1>.doit(X):Q#1.x [X] EXACT_MATCH
			src/X.java int void X.zoo():<lambda #1>.doit(X) [X] EXACT_MATCH""");
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
			"""
				public class X {
				   void zoo() {
					    I i = (X x2) /*1*/-> {
				                 X x2;
				                 I i2 = (X x3) /*2*/-> 10;
				                 class Q {
				                     X x;
				                 }
				                 return 0;
				       };
				   }
				}
				 class Y {}
				"""
		);
		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit(X x1);
				}
				"""
		);

		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		search(type, REFERENCES, SearchEngine.createJavaSearchScope(new IJavaElement[] {project}), this.resultCollector);
		assertSearchResults("""
			src/I.java int I.doit(X) [X] EXACT_MATCH
			src/X.java int int void X.zoo():<lambda #1>.doit(X):<lambda #1>.doit(X) [X] EXACT_MATCH
			src/X.java int void X.zoo():<lambda #1>.doit(X):Q#1.x [X] EXACT_MATCH
			src/X.java int void X.zoo():<lambda #1>.doit(X) [X] EXACT_MATCH
			src/X.java int void X.zoo():<lambda #1>.doit(X) [X] EXACT_MATCH""");
	}
	finally {
		deleteProject("P");
	}
}
public void testBug400905_0014() throws CoreException {
	boolean indexState = isIndexDisabledForTest();
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		this.indexDisabledForTest = true;
		createFile(
			"/P/src/X.java",
			"""
				public class X {
				   void zoo() {
					    Y.goo((x) -> -x);
				   }
				}
				""");
		createFile(
				"/P/src/Y.java",
				"""
					public class Y {
					   static void goo(I i) {}
					}
					""");

		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit(int x);
				}
				"""
		);
		JavaModelManager.getIndexManager().waitForIndex(true, null);
		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[] {"I"});
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/I.java int I.doit(int) [doit] EXACT_MATCH\n" +
							"src/X.java int void X.zoo():<lambda #1>.doit(int) [(x) ->] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
		this.indexDisabledForTest = indexState;
	}
}
public void testBug400905_0015() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/X.java",
			"""
				public class X {
				   void zoo() {
					    Y.goo((x) -> -x);
				   }
				}
				""");
		createFile(
				"/P/src/Y.java",
				"""
					public class Y {
					   static void goo(J j) {}
					}
					""");

		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit(int x);
				}
				"""
		);
		createFile(
				"/P/src/J.java",
				"""
					public interface J {
					    public int doit(int x);
					}
					"""
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
	boolean indexState = isIndexDisabledForTest();
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		this.indexDisabledForTest = true;
		createFile(
			"/P/src/X.java",
			"""
				public class X {
				   void zoo() {
					    Y.goo((x) -> -x);
					    Y.zoo((x) -> -x);
				   }
				}
				""");
		createFile(
				"/P/src/Y.java",
				"""
					public class Y {
					   static void goo(J j) {}
					   static void zoo(I i) {}
					}
					""");

		createFile(
			"/P/src/I.java",
			"""
				public interface I {
				    public int doit(int x);
				}
				"""
		);
		createFile(
				"/P/src/J.java",
				"""
					public interface J {
					    public int doit(int x);
					}
					"""
			);
		JavaModelManager.getIndexManager().waitForIndex(true, null);
		IType type = getCompilationUnit("/P/src/I.java").getType("I");
		IMethod method = type.getMethod("doit", new String[] {"I"});
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/I.java int I.doit(int) [doit] EXACT_MATCH\n" +
				"src/X.java int void X.zoo():<lambda #1>.doit(int) [(x) ->] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
		this.indexDisabledForTest = indexState;
	}
}
public void testBug400905_0017() throws CoreException {
	try {
		createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile(
			"/P/src/Function.java",
			"""
				@FunctionalInterface
				public interface Function<T, R> {
				    R apply(T t);
				}
				""");
		createFile(
				"/P/src/Y.java",
				"""
					public final class Collectors {
					 @SuppressWarnings("unchecked")
					    private static <I, R> Function<I, R> castingIdentity() {
					        return i -> (R) i;
					    }
					}
					""");

		IType type = getCompilationUnit("/P/src/Function.java").getType("Function");
		IMethod method = type.getMethods()[0];
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/Function.java R Function.apply(T) [apply] EXACT_MATCH\n" +
				"src/Y.java R Function<I,R> Collectors.castingIdentity():<lambda #1>.apply(I) [i ->] EXACT_MATCH");
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
			"""
				@FunctionalInterface
				public interface Function<T, R> {
				    R apply(T t);
				}
				""");
		createFile(
				"/P/src/Y.java",
				"""
					public final class Collectors {
					 @SuppressWarnings("unchecked")
					    private static <I, R> Function<String, String> castingIdentity() {
					        return i -> (R) i;
					    }
					}
					""");

		IType type = getCompilationUnit("/P/src/Function.java").getType("Function");
		IMethod method = type.getMethods()[0];
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/Function.java R Function.apply(T) [apply] EXACT_MATCH\n" +
				"src/Y.java java.lang.String Function<String,String> Collectors.castingIdentity():<lambda #1>.apply(java.lang.String) [i ->] EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
// test working copy.
public void testBug400905_0019() throws CoreException { // bad package + bad cast - make sure there is no NPE.
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/Function.java",
			"""
				@FunctionalInterface
				public interface Function<T, R> {
				    R apply(T t);
				}
				"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b400905/Collectors.java",
			"""
				public final class Collectors {
				 @SuppressWarnings("unchecked")
				    private static <I, R> Function<String, String> castingIdentity() {
				        return i -> (R) i;
				    }
				}
				""") ;
	IType type = this.workingCopies[0].getType("Function");
	IMethod method = type.getMethods()[0];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
	assertSearchResults("src/b400905/Function.java R b400905.Function.apply(T) [apply] EXACT_MATCH");
}
// test working copy (dirty), expect only focus type
public void testBug400905_0020() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Function.java",
			"""
				@FunctionalInterface
				public interface Function<T, R> {
				    R apply(T t);
				}
				"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/Collectors.java",
			"""
				public final class Collectors {
				    private static <I, R> Function<I, R> castingIdentity() {
				        return i -> (R) i;
				    }
				}
				""") ;
	IType type = this.workingCopies[0].getType("Function");
	IMethod method = type.getMethods()[0];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
	assertSearchResults("src/Function.java R Function.apply(T) [apply] EXACT_MATCH");
}
// test working copy after commit, expect focus type + other matches.
public void testBug400905_0021() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p400905/Function.java",
			"""
				package p400905;
				public interface Function<T, R> {
				    R apply(T t);
				}
				"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p400905/Collectors.java",
			"""
				package p400905;
				public final class Collectors {
				    private static <I, R> Function<I, R> castingIdentity() {
				        return i -> (R) i;
				    }
				}
				""") ;

	IPath path = new Path("/JavaSearchBugs/src/p400905");
	try {
		createFolder(path);
		this.workingCopies[0].commitWorkingCopy(true, null);
		this.workingCopies[1].commitWorkingCopy(true, null);

		IType type = this.workingCopies[0].getType("Function");
		IMethod method = type.getMethods()[0];
		search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH, SearchEngine.createHierarchyScope(type), this.resultCollector);
		assertSearchResults("src/p400905/Function.java R p400905.Function.apply(T) [apply] EXACT_MATCH\n" +
				"src/p400905/Collectors.java R Function<I,R> p400905.Collectors.castingIdentity():<lambda #1>.apply(I) [i ->] EXACT_MATCH");
	} finally {
		deleteFolder(path);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905#c35
public void testBug400905_0022() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface FunctionalInterface {
					int thrice(int x);
				}
				interface J {
					int twice(int x);
				}
				public class X {
					FunctionalInterface i = (x) -> {return x * 3;};\s
					X x = null;
					static void goo(FunctionalInterface i) {}\s
				}\s
				"""
	);
	search("thrice", METHOD, DECLARATIONS, ERASURE_RULE, getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
			"src/test/Test.java int test.FunctionalInterface.thrice(int) [thrice] EXACT_MATCH\n" +
			"src/test/Test.java int test.X.i:<lambda #1>.thrice(int) [(x) ->] EXACT_MATCH"
	);
}
public void testBug400905_0023() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int x);
				}
				interface J {
					int twice(int x);
				}
				public class X {
					I i = (x) /* field */ -> {return x * 3;};\s
					X x = null;
					static void goo(I i) {}\s
					public static void main(String[] args) {\s
							goo((x) /*call*/ -> {\s
								int y = 3;
								return x * y;\s
							});
						I i2 = (x) /* local */ -> {
							int y = 3;\s
							return x * y;
						};
						J j1 = (x) -> {\s
							int y = 2; \s
							return x * y;
						}; \s
					}
				}
				"""
	);
	search("thrice", METHOD, DECLARATIONS, ERASURE_RULE, getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
					"""
						src/test/Test.java int test.I.thrice(int) [thrice] EXACT_MATCH
						src/test/Test.java int test.X.i:<lambda #1>.thrice(int) [(x) /* field */ ->] EXACT_MATCH
						src/test/Test.java int void test.X.main(String[]):<lambda #1>.thrice(int) [(x) /*call*/ ->] EXACT_MATCH
						src/test/Test.java int void test.X.main(String[]):<lambda #1>.thrice(int) [(x) /* local */ ->] EXACT_MATCH"""
	);
}
public void testBug400905_0024() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int x);
				}
				public class X {
					static int goo(int x) { return 3 * x; }\s
					public static void main(String[] args) {\s
						I i = X::goo;
					}
				}
				"""
	);

	search(this.workingCopies[0].getType("X").getMethod("goo", new String[] { "I" }), THIS_REFERENCE);
	assertSearchResults("");
}
public void testBug400905_0025() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int p);
				}
				class Y {
					int goo(int x) { return 3 * x; }\s
				}
				public class X extends Y {
					public void main(String[] args) {\s
						I i = this::goo;
				       i = super::goo;
					}
				}
				"""
	);

	search(this.workingCopies[0].getType("Y").getMethod("goo", new String[] { "I" }), THIS_REFERENCE);
	assertSearchResults("src/test/Test.java void test.X.main(String[]) [goo] EXACT_MATCH");
}
public void testBug400905_0026() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int p);
				}
				class Y {
					int goo(int x) { return 3 * x; }\s
				}
				public class X extends Y {
					public void main(String[] args) {\s
						I i = this::goo;
				       i = super::goo;
					}
				}
				"""
	);

	search(this.workingCopies[0].getType("Y").getMethod("goo", new String[] { "I" }), SUPER_REFERENCE);
	assertSearchResults("src/test/Test.java void test.X.main(String[]) [goo] EXACT_MATCH");
}
public void testBug400905_0027() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int p);
				}
				class Y {
					int goo(int x) { return 3 * x; }\s
				}
				public class X extends Y {
					public void main(String[] args) {\s
						I i = this::goo;
				       i = super::goo;
					}
				}
				"""
	);

	search(this.workingCopies[0].getType("Y").getMethod("goo", new String[] { "I" }), QUALIFIED_REFERENCE);
	assertSearchResults("");
}
public void testBug400905_0028() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int p);
				}
				class Y {
					static class Z {
						static int goo(int x) { return 3 * x; }  \s
						I i = Z::goo;
				   }
				}
				public class X extends Y.Z {
					public void main(String[] args) {\s
						I i = Y.Z::goo;
					}
				}
				"""
	);

	search(this.workingCopies[0].getType("Y").getType("Z").getMethod("goo", new String[] { "I" }), THIS_REFERENCE | SUPER_REFERENCE | QUALIFIED_REFERENCE);
	assertSearchResults("src/test/Test.java void test.X.main(String[]) [goo] EXACT_MATCH");
}
public void testBug400905_0029() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int p);
				}
				class Y {
					static class Z {
						static int goo(int x) { return 3 * x; }  \s
						I i = Z::goo;
				   }
				}
				public class X extends Y.Z {
					public void main(String[] args) {\s
						I i = Y.Z::goo;
					}
				}
				"""
	);

	search(this.workingCopies[0].getType("Y").getType("Z").getMethod("goo", new String[] { "I" }), REFERENCES, EXACT_RULE);
	assertSearchResults("src/test/Test.java test.Y$Z.i [goo] EXACT_MATCH\n" +
			"src/test/Test.java void test.X.main(String[]) [goo] EXACT_MATCH");
}
public void testBug400905_0030() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int p);
				}
				class Y {
					static class Z {
						static int goo(int x) { return 3 * x; }  \s
						I i = Z::goo;
				   }
				}
				public class X extends Y.Z {
					public void main(String[] args) {\s
						I i = Y.Z::goo;
					}
				}
				"""
	);

	search(this.workingCopies[0].getType("Y").getType("Z").getMethod("goo", new String[] { "I" }), IMPLICIT_THIS_REFERENCE);
	assertSearchResults("");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429738, [1.8][search] Find Declarations (Ctrl + G) shows no result for type-less lambda parameter
public void test429738() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/X.java",
			"""
				@FunctionalInterface
				interface Foo {
					int foo(int x);
				}
				public class X {
					// Select 'x' in lambda body and press Ctrl+G.
					Foo f1= x -> /* here*/ x; //[1]
					Foo f2= (int x) -> x; //[2]
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "/* here*/ x";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	ILocalVariable local = (ILocalVariable) elements[0];
	search(local, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"src/b400905/X.java int b400905.X.f1:<lambda #1>.foo(int).x [x] EXACT_MATCH");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429738, [1.8][search] Find Declarations (Ctrl + G) shows no result for type-less lambda parameter
public void test429738a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/X.java",
			"""
				@FunctionalInterface
				interface Foo {
					int foo(int x);
				}
				public class X {
					// Select 'x' in lambda body and press Ctrl+G.
					Foo f1= x ->  x; //[1]
					Foo f2= (int x) -> /* here*/ x; //[2]
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "/* here*/ x";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	ILocalVariable local = (ILocalVariable) elements[0];
	search(local, DECLARATIONS, EXACT_RULE);
	assertSearchResults(
			"src/b400905/X.java int b400905.X.f2:<lambda #1>.foo(int).x [x] EXACT_MATCH");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429836, [1.8][search] Search implementors in workspace does not show lambda expressions.
public void testBug429836() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	String buffer =	"""
		@FunctionalInterface
		interface I {
			int foo(int x);
		}
		public class X {
			I f1= x ->  x;
			I f2= (int x) -> x; //[2]
			public static void main(String[] args) {
				I f1= x -> x;
				I f2= (int x) -> x;
			}
		}
		""";

	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429836/X.java", buffer);
	IType type = this.workingCopies[0].getType("I");
	search(type, IMPLEMENTORS);
	assertSearchResults(
		"""
			src/b429836/X.java int b429836.X.f1:<lambda #1>.foo(int) [x ->] EXACT_MATCH
			src/b429836/X.java int b429836.X.f2:<lambda #1>.foo(int) [(int x) ->] EXACT_MATCH
			src/b429836/X.java int void b429836.X.main(String[]):<lambda #1>.foo(int) [x ->] EXACT_MATCH
			src/b429836/X.java int void b429836.X.main(String[]):<lambda #1>.foo(int) [(int x) ->] EXACT_MATCH"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429934, [1.8][search] for references to type of lambda with 'this' parameter throws AIIOBE
public void test429934() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400905/X.java",
			"""
				interface Function<T, R> {
				    R apply(T t);
				}
				public class X {
					public static void main(String[] args) {
						Function<String, String> f1= (String s, Function this) -> s;
						Function<String, String> f2= (Function this, String s) -> s;
					}\s
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "Function";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	IType type = (IType) elements[0];
	search(type, REFERENCES, ERASURE_RULE);
	assertSearchResults(
					"""
						src/b400905/X.java void b400905.X.main(String[]) [Function] EXACT_MATCH
						src/b400905/X.java void b400905.X.main(String[]) [Function] EXACT_MATCH
						src/b400905/X.java void b400905.X.main(String[]) [Function] EXACT_MATCH
						src/b400905/X.java void b400905.X.main(String[]) [Function] EXACT_MATCH""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430159, [1.8][search] Lambda Expression not found when searching using OrPattern or AndPattern
public void test430159a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429498/X.java",
			"""
				interface I {
				    public void doit(int xyz);
				}
				public class X {
					I i = (int xyz) -> {};
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "doit";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	SearchPattern leftPattern = SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, ERASURE_RULE);

	selection = "xyz";
	start = str.lastIndexOf(selection);
	length = selection.length();

	elements = this.workingCopies[0].codeSelect(start, length);
	SearchPattern rightPattern = SearchPattern.createPattern(elements[0].getParent(), ALL_OCCURRENCES, ERASURE_RULE); // mimic https://bugs.eclipse.org/bugs/show_bug.cgi?id=429498#c6

	SearchPattern pattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
		"src/b429498/X.java void b429498.I.doit(int) [doit] EXACT_MATCH\n" +
		"src/b429498/X.java void b429498.X.i:<lambda #1>.doit(int) [(int xyz) ->] EXACT_MATCH"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430159, [1.8][search] Lambda Expression not found when searching using OrPattern or AndPattern
public void test430159b() throws CoreException {	// this test basically checks that and pattern locator does not a lambda from being found.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429498/X.java",
			"""
				interface I {
				    public void doit();
				}
				public class X {
					I i = () -> {};
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "doit";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	SearchPattern leftPattern = SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, ERASURE_RULE);

	selection = "->";
	start = str.indexOf(selection);
	length = selection.length();

	elements = this.workingCopies[0].codeSelect(start, length);
	SearchPattern rightPattern = SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, ERASURE_RULE);

	SearchPattern pattern = new AndPattern(leftPattern, rightPattern);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
		"src/b429498/X.java void b429498.I.doit() [doit] EXACT_MATCH\n" +
		"src/b429498/X.java void b429498.X.i:<lambda #1>.doit() [() ->] EXACT_MATCH"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430159, [1.8][search] Lambda Expression not found when searching using OrPattern or AndPattern
public void test430159c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429498/X.java",
			"""
				interface I {
					public void doit();
				}
				public class X {
				   static void foo() {}
				   static void foo(int i) {}
					I i = X :: foo;
				}
				"""
	);
	String str = this.workingCopies[0].getSource();
	String selection = "foo";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	SearchPattern leftPattern = SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, ERASURE_RULE);

	selection = "::";
	start = str.indexOf(selection);
	length = selection.length();

	elements = this.workingCopies[0].codeSelect(start, length);
	SearchPattern rightPattern = SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, ERASURE_RULE);

	SearchPattern pattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
		"""
			src/b429498/X.java void b429498.I.doit() [doit] EXACT_MATCH
			src/b429498/X.java b429498.X.i [foo] EXACT_MATCH
			src/b429498/X.java void b429498.X.foo() [foo] EXACT_MATCH"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430159, [1.8][search] Lambda Expression not found when searching using OrPattern or AndPattern
public void test430159d() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429498/X.java",
			"""
				interface I {
					public void doit();
				}
				public class X {
				   static void foo() {}
				   static void foo(int i) {}
					I i = X :: foo;
				}
				"""
	);
	String str = this.workingCopies[0].getSource();
	String selection = "foo";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	SearchPattern leftPattern = SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, ERASURE_RULE);

	selection = "::";
	start = str.indexOf(selection);
	length = selection.length();

	elements = this.workingCopies[0].codeSelect(start, length);
	SearchPattern rightPattern = SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, ERASURE_RULE);

	SearchPattern pattern = new AndPattern(leftPattern, rightPattern);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(""
	);
}
/**
 * bug 429012
 * test tests search for Reference expression - super:: form, without type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X extends Y {
				    public static void main(String [] args) {
						new X().doit();
						new X().foo(0);
				    }
				    void doit() {
				        I i = super::foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				    public void foo(int x) {
					System.out.println(x);
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.doit() [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - super:: form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0002() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X extends Y {
				    public static void main(String [] args) {
						new X().doit();
						new X().foo(0);
				    }
				    void doit() {
				        I i = super::<String>foo;
				        i.foo(10);\s
				    }
				}
				class Y {
				    public void foo(int x) {
					System.out.println(x);
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.doit() [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - SimpleName:: form, without type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0003() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
					public void doit();
				}
				class Y {
					Y() {}
					Y(int i) {}
				}
				
				public class X {
				    X(int i) {}\s
				   static void foo() {}
				   static void foo(int i) {}
					I i = X :: foo;
					I j = Y :: new;
				   public static void main() {\s
				     Y y = new Y();\s
				     foo();\s
				   }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", null);
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java b429012.X.i [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - SimpleName:: form, with type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0004() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y::<String>foo;
				        new Y().foo(0);
				        i.foo(10);\s
				    }
				}
				class Y {
				    public static void foo(int x) {
					System.out.println(x);
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - QualifiedName:: form, without type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0005() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y.Z::foo;
				        Y.Z.foo(0);
				        i.foo(10);\s
				    }
				}
				class Y {
				    static class Z {
				        public static void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y").getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - QualifiedName:: form, with type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0006() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y.Z::<String>foo;
				        Y.Z.foo(0);
				        i.foo(10);\s
				    }
				}
				class Y {
				    static class Z {
				        public static void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y").getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - Primary:: form, without type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0007() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = new Y()::foo;
				        new Y().foo(0);
				        i.foo(10);\s
				    }
				}
				class Y {
				        void foo(int x) {
					    System.out.println(x);
				        }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - Primary:: form, with type arguments.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0008() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = new Y()::<String>foo;
				        new Y().foo(0);
				        i.foo(10);\s
				    }
				}
				class Y {
				        void foo(int x) {
					    System.out.println(x);
				        }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - {@code X<T>::} form, without type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0009() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				  void foo(Y<String> y, int x);
				}
				public class X {
				  public X() {
				    super();
				  }
				  public static void main(String[] args) {
				    I i = Y<String>::foo;
				    new Y<String>().foo(0);
				    i.foo(new Y<String>(), 10);
				  }
				}
				class Y<T> {
				  Y() {
				    super();
				  }
				  void foo(int x) {
				    System.out.println(x);
				  }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}


/**
 * bug 429012
 * test tests search for Reference expression - {@code X<T>::} form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0010() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				  void foo(Y<String> y, int x);
				}
				public class X {
				  public X() {
				    super();
				  }
				  public static void main(String[] args) {
				    I i = Y<String>::<String>foo;
				    new Y<String>().foo(0);
				    i.foo(new Y<String>(), 10);
				  }
				}
				class Y<T> {
				  Y() {
				    super();
				  }
				  void foo(int x) {
				    System.out.println(x);
				  }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - {@code X<T>.Name ::} form, without type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0011() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(Y<String>.Z z, int x);
				}
				public class X  {
					@SuppressWarnings("unused")
				    public static void main(String [] args) {
				        new Y<String>().new Z().foo(0);
				        I i = Y<String>.Z::foo;
				        i.foo(new Y<String>().new Z(), 10);\s
				    }
				}
				class Y<T> {
				    class Z {
				        void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - {@code X<T>.Name ::} form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0012() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(Y<String>.Z z, int x);
				}
				public class X  {
					@SuppressWarnings("unused")
				    public static void main(String [] args) {
				        I i = Y<String>.Z::<String>foo;
				        new Y<String>().new Z().foo(0);
				        i.foo(new Y<String>().new Z(), 10);\s
				    }
				}
				class Y<T> {
				    class Z {
				        void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - {@code X<T>.Y<K> ::} form, without type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0013() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(Y<String>.Z<String> z, int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y<String>.Z<String>::foo;
				        new Y<String>().new Z<String>().foo(0);
				        i.foo(new Y<String>().new Z<String>(), 10);\s
				    }
				}
				class Y<T> {
				    class Z<K> {
				        void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, ERASURE_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - {@code X<T>.Y<K> ::} form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0014() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I {
				    void foo(Y<String>.Z<String> z, int x);
				}
				public class X  {
				    public static void main(String [] args) {
				        I i = Y<String>.Z<String>::<String>foo;
				        new Y<String>().new Z<String>().foo(0);
				        i.foo(new Y<String>().new Z<String>(), 10);\s
				    }
				}
				class Y<T> {
				    class Z<K> {
				        void foo(int x) {
					    System.out.println(x);
				        }
				    }
				}
				
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	type = type.getType("Z");
	IMethod method = type.getMethod("foo", new String[] {"I"});
	search(method, METHOD_REFERENCE_EXPRESSION, EXACT_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [foo] EXACT_MATCH"
	);
}

/**
 * bug 429012
 * test tests search for Reference expression - {@code X<T>.Y<K> ::} new form, with type arguments
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012"
 */
public void testBug429012_0015() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429012/X.java",
			"""
				interface I<T> {
				    T get();
				}
				/**
				 * @see Y#Y()
				 */
				public class X  {
				    public static void main(String [] args) {
				        I<Y<String>> s = Y<String>::<Integer>new;
				        s.get().equals(new Y<String>());\s
				    }
				}
				class Y<E> {
				    <T> Y() {
				        System.out.println("Y<E>::<T>new");
				    }
				}
				"""
	);
	IType type = this.workingCopies[0].getType("Y");
	IMethod method = type.getMethod("Y", new String[] {});
	search(method, METHOD_REFERENCE_EXPRESSION, ERASURE_RULE);
	assertSearchResults(
			"src/b429012/X.java void b429012.X.main(String[]) [Y<String>::<Integer>new] EXACT_MATCH"
	);
}// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012, [1.8][search] Add finegrain (limitTo) option for method reference expressions
public void testBug429012_0016() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b429498/X.java",
			"""
				interface I {
					public void doit();
				}
				public class X {
				   static void foo() {}
				   static void foo(int i) {}
					I i = X :: foo;
				   static void bar() {foo();}
				}
				"""
	);
	String str = this.workingCopies[0].getSource();
	String selection = "foo";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], METHOD_REFERENCE_EXPRESSION, ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
		"src/b429498/X.java b429498.X.i [foo] EXACT_MATCH"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012, [1.8][search] Add finegrain (limitTo) option for method reference expressions
public void testBug429012_0017() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int p);
				}
				class Y {
					int goo(int x) { return 3 * x; }\s
				}
				public class X extends Y {
					public void main(String[] args) {\s
						I i = this::goo;
				       i = super::goo;
				       new Y().goo(0);
					}
				}
				"""
	);

	search(this.workingCopies[0].getType("Y").getMethod("goo", new String[] { "I" }), METHOD_REFERENCE_EXPRESSION);
	assertSearchResults(
		"src/test/Test.java void test.X.main(String[]) [goo] EXACT_MATCH\n" +
		"src/test/Test.java void test.X.main(String[]) [goo] EXACT_MATCH"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=429012, [1.8][search] Add finegrain (limitTo) option for method reference expressions
public void testBug429012_0018() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
			"""
				interface I {\s
					int thrice(int p);
				}
				class Y {
					static class Z {
						static int goo(int x) { return 3 * x; }  \s
						I i = Z::goo;
				   }
				}
				public class X extends Y.Z {
					public void main(String[] args) {\s
						I i = Y.Z::goo;
				       Y.Z.goo(0);
					}
				}
				"""
	);

	search(this.workingCopies[0].getType("Y").getType("Z").getMethod("goo", new String[] { "I" }), METHOD_REFERENCE_EXPRESSION);
	assertSearchResults(
		"src/test/Test.java test.Y$Z.i [goo] EXACT_MATCH\n" +
		"src/test/Test.java void test.X.main(String[]) [goo] EXACT_MATCH"
);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=31716, [1.8][compiler] NPE when creating LambdaMethod element for lambda expressions with errors
public void testBug431716() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				interface ToLongFunction<T> {
				    long applyAsLong(T value);
				}
				interface ToIntFunction<T> {
				    int applyAsInt(T value);
				}
				interface Stream<T>  {
				   int mapToInt(ToIntFunction<? super T> mapper);
				}
				
				public interface X<T> {
				
					public static <T> ToLongFunction<? super T> toLongFunction() {
						return null;
					}
					default void asIntStream(Stream<T> makerget) {
						makerget.mapToInt((long l) -> (int) l);
						// trying to inline this method results causes NPE
						/* here */toLongFunction();
					}
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "/* here */toLongFunction";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], REFERENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
}
/**
 * bug 432541:  Stack Overflow in Java Search - type inference issue?
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=432541"
 */
public void testBug432541() throws CoreException {
this.workingCopies = new ICompilationUnit[8];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Constants.java",
		"""
			final class Constants {
			    static final String BUG_NAME =  "BUG 432541";
			}
			"""
	);
this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/Constants.java",
		"package test;\n" +
		"public class Constants { public static final String ANY_NAMESPACE_ANY = \"UNRELATED\";}"
		);
this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/Context.java",
		"package test;\n" +
		"public abstract class Context <  DESCRIPTOR extends Descriptor<?, ?>> {}"
		);
this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/CoreObjectBuilder.java",
		"package test;\n" +
		"public abstract class CoreObjectBuilder {}\n"
		);
this.workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/Descriptor.java",
		"""
			package test;
			
			public interface Descriptor <UNMARSHAL_RECORD extends UnmarshalRecord, OBJECT_BUILDER extends CoreObjectBuilder> {
				public default OBJECT_BUILDER getObjectBuilder() { return null; }
			}
			"""
		);
this.workingCopies[5] = getWorkingCopy("/JavaSearchBugs/src/Unmarshaller.java",
		"""
			package test;
			
			public abstract class Unmarshaller<CONTEXT extends Context, DESCRIPTOR extends Descriptor> {
			   public CONTEXT getContext() {
				   return null;
			   }
			}
			"""
		);
this.workingCopies[6] = getWorkingCopy("/JavaSearchBugs/src/UnmarshalRecord.java",
		"""
			package test;
			
			public interface UnmarshalRecord<UNMARSHALLER extends Unmarshaller> {
			    public UNMARSHALLER getUnmarshaller();
			    public default void setAttrs() {}
			}
			"""
		);
this.workingCopies[7] = getWorkingCopy("/JavaSearchBugs/src/XMLRelationshipMappingNodeValue.java",
		"""
			package test;
			interface CMap<UNMARSHALLER extends Unmarshaller> {
			    Object convertToValue( UNMARSHALLER unmarshaller);
			}
			public abstract class XMLRelationshipMappingNodeValue {
				public void processChild(Descriptor xmlDescriptor, UnmarshalRecord unmarshalRecord) {
					if (Constants.ANY_NAMESPACE_ANY.toCharArray().length > 0) {
						Descriptor d1 = (Descriptor) xmlDescriptor.getObjectBuilder();
					}
				}
				protected Descriptor findReferenceDescriptor(UnmarshalRecord unmarshalRecord, Descriptor descriptor) {
					if (Constants.ANY_NAMESPACE_ANY.toCharArray().length > 0) {
						Context xmlContext = unmarshalRecord.getUnmarshaller().getContext();		\t
					}
					return null;
				}
			\t
				protected void endElementProcessText(UnmarshalRecord unmarshalRecord, CMap converter) {
					if (Constants.ANY_NAMESPACE_ANY.toCharArray().length > 0) {
						converter.convertToValue(unmarshalRecord.getUnmarshaller());
					}
				}
			}
			"""
		);
SearchPattern pattern = SearchPattern.createPattern(
		"Constants",
		TYPE,
		REFERENCES,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=435480
//[1.8][search] search in method reference expressions finds annotation element name
public void testBug435480_0001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				import java.lang.annotation.*;
				@Documented
				@Retention(RetentionPolicy.RUNTIME)
				@Target(value={})
				public @interface Ann1 {
				}"""
	);

	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern("*", IMPLEMENTORS, IJavaSearchConstants.METHOD_REFERENCE_EXPRESSION, EXACT_RULE );

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=435480
//[1.8][search] search in method reference expressions finds annotation element name
public void testBug435480_0002() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL18_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.8");
		createFile("/P/src/X.java",
				"""
					import java.lang.annotation.*;
					@Documented
					@Retention(RetentionPolicy.RUNTIME)
					@Target(value={})
					public @interface Ann1 {
					}
					""");
		MethodPattern pattern = (MethodPattern) SearchPattern.createPattern("*", IMPLEMENTORS, IJavaSearchConstants.METHOD_REFERENCE_EXPRESSION, EXACT_RULE );

		int mask = IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		new SearchEngine(this.workingCopies).search(pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				scope,
				this.resultCollector,
				null);
		assertSearchResults("");
	} finally {
		deleteProject("P");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=454401"
 */
public void testBug454401() throws CoreException, JavaModelException {
	try {
		createJavaProject("P", new String[] { "src" },
				new String[] {"JCL_LIB"}, "bin");
		createFolder("/P/src/com/test");
		createFile("/P/src/com/test/X.java",
				"""
					package com.test;
					public class X {\s
					\s
						private class Y {\s
							private class P {\s
								\s
							}\s
							@SuppressWarnings("unused")\s
							public void t1(P p) {\s
								t2(p);\s
							}\s
							protected void t2(P p) {\s
								\s
							}\s
						}\s
						public void foo() {\s
							Y y = new X().new Y();\s
							y.t2(y.new P());\s
						}\s
						public static void main(String[] args) {\s
							\s
						}\s
					}
					""" );
		waitUntilIndexesReady();
		IType type = getCompilationUnit("/P/src/com/test/X.java").getType("X").getType("Y");
		IMethod[] methods = type.getMethods();

		search(methods[1], REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);

		assertSearchResults("src/com/test/X.java void com.test.X.foo() [t2(y.new P())] EXACT_MATCH\n" +
			"src/com/test/X.java void com.test.X$Y.t1(P) [t2(p)] EXACT_MATCH");
	} finally {
		deleteProject("P");
	}
}
public void testBug454411_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	String content =
			"""
		package com.test;
		interface Function<T, R> {\s
		    R apply(T t);\s
		}\s
		interface I<T> extends Function<T, T> {\s
		    static <T> I<T> identity() {\s
		        return t -> t;\s
		    }\s
		}\s
		\s
		public class X {\s
			private static class Multiplier {\s
				private final long mMul;\s
		\s
				public Multiplier(long iMul) {\s
					this.mMul = iMul;\s
				}\s
				public <T, V> Long mul(Long iItem) {\s
					return iItem * mMul;\s
				}\s
			}\s
		\s
			private static void test(I<Long> iFn) {\s
			}\s
		\s
			public static <T, V> void main(String[] args) {\s
				I<Long> mul/* here */ = (new Multiplier(3))::<T, V> mul;\s
				X.test((new Multiplier(3))::<T, V> mul);\s
			}\s
		}
		""";
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b454411/X.java", content);
	int start = content.indexOf("mul/* here */");
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, "mul".length());
	SearchPattern pattern = SearchPattern.createPattern(elements[0], DECLARATIONS);
	try {
		new SearchEngine(this.workingCopies).search(pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				getJavaSearchWorkingCopiesScope(),
				this.resultCollector,
				null);
		assertSearchResults("src/b454411/X.java void b454411.X.main(String[]).mul [mul] EXACT_MATCH");
	} catch (NullPointerException e) {
		e.printStackTrace();
		assertFalse("Test Failed", true);
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=458614
//[1.8][search] Constructor reference not found in search
public void testBug458614_001() throws CoreException {
	try {
		// Create project and files
		IJavaProject project = createJavaProject("P1", new String[] {"src"}, new String[] {"bin"}, "bin");
		createFile(
			"/P1/src/X.java",
			"""
				interface Function<T, R> {
					R apply(T);\s
				};
				@SuppressWarnings("unused")
				public class X<T> {
					private void foo() {
						Function<Integer, int[]> a1 = int[]::new;
					}
				}
				""");
		waitUntilIndexesReady();

		IndexManager indexManager = JavaModelManager.getIndexManager();
		indexManager.indexAll(project.getProject());
		waitUntilIndexesReady();

		MethodPattern pattern = (MethodPattern) SearchPattern.createPattern("*", METHOD, IJavaSearchConstants.METHOD_REFERENCE_EXPRESSION, EXACT_RULE );

		new SearchEngine(this.workingCopies).search(pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				SearchEngine.createWorkspaceScope(),
				this.resultCollector,
				null);
		assertSearchResults("");
	}
	finally {
		deleteProject("P1");
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=458614
//[1.8][search] Constructor reference not found in search
public void testBug458614_002() throws CoreException {
	try {
		// Create project and files
		IJavaProject project = createJavaProject("P1", new String[] {"src"}, new String[] {"bin"}, "bin");
		createFile(
			"/P1/src/X.java",
			"""
				interface Function<T, R> {
					R apply(T);\s
				};
				//@SuppressWarnings("unused")
				public class X<T> {
					private void foo() {
						Function<Integer, int[]> a1 = int[]::new;
					}
				}
				""");
		waitUntilIndexesReady();

		IndexManager indexManager = JavaModelManager.getIndexManager();
		indexManager.indexAll(project.getProject());
		waitUntilIndexesReady();

		MethodPattern pattern = (MethodPattern) SearchPattern.createPattern("*new", METHOD, IJavaSearchConstants.METHOD_REFERENCE_EXPRESSION, EXACT_RULE );

		new SearchEngine(this.workingCopies).search(pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				SearchEngine.createWorkspaceScope(),
				this.resultCollector,
				null);
		assertSearchResults("");
	}
	finally {
		deleteProject("P1");
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=461025
//[1.8][search] Constructor reference not found in search
public void testBug461025_001() throws CoreException {
this.workingCopies = new ICompilationUnit[1];
this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			@FunctionalInterface
			interface Y<T> {
			    T get();
			}
			
			public class X {
				public X() {}
				public X(int i) {}
			
				private void m1() {
					Y<X> s1 = X::new;
			
					Y<X> s2 = new Y<X>() {
			
						@Override
						public X get() {
							return null;
						}
					};
				}
			}
			""");
SearchPattern pattern = SearchPattern.createPattern(
		"*",
		CONSTRUCTOR,
		METHOD_REFERENCE_EXPRESSION,
		EXACT_RULE);
new SearchEngine(this.workingCopies).search(pattern,
new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
getJavaSearchWorkingCopiesScope(),
this.resultCollector,
null);
assertSearchResults(
		"src/X.java void X.m1() [X::new] EXACT_MATCH"
);
}
public void testBug468127_0001() throws CoreException {
	try {

		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] { "/P/lib468127.jar", "JCL18_LIB" }, "bin", "1.8");
		createFile(
			"/P/src/X.java",
			"""
				public class X {
					@SuppressWarnings({ "rawtypes", "unchecked" })
					public static void main(String[] args) {
						IY y = s -> foo(0);
						y.accept(0);
					}
					static private void foo(int i) {}
				}
				"""
		);
		addLibraryEntry(project, "/JavaSearchBugs/lib/b468127.jar", false);

		waitUntilIndexesReady();

		SearchPattern pattern = SearchPattern.createPattern("IY.accept(T)", METHOD, REFERENCES, ERASURE_RULE);
		search(pattern, SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES), this.resultCollector);
		assertSearchResults(
				"src/X.java void X.main(String[]) [accept(0)] EXACT_MATCH\n" +
				"lib/b468127.jar void <anonymous>.accept(java.lang.Object) EXACT_MATCH");
	}
	finally {
		deleteProject("P");
	}
}
// not solely a search issue but easily reproducible using search
public void test473343_0001() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"a-b"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
		String source = """
			interface Consumer<T> {
				void accept(T t);
			}
			
			public class X {
				Consumer<? super Y> action = (i_) -> X.foo(i_);
				private static void foo(Y tb) {
				}
			}
			""";
		createFile("/P/a-b/X.java", source);
		createFile("/P/a-b/Y.java", "public class Y{}");
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/a-b/X.java");
		String foo = "foo";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(foo), foo.length());
		SearchPattern pattern = SearchPattern.createPattern(elements[0], REFERENCES, ERASURE_RULE);
		search(pattern, SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES), this.resultCollector);
		LambdaMethod method = (LambdaMethod) this.resultCollector.match.getElement();
  		try {
			SearchPattern.createPattern(method, REFERENCES, ERASURE_RULE);
		} catch (IllegalArgumentException e) {
			assertFalse("Test Failed", true);
		}
	} finally {
		deleteProject("P");
	}
}
public void testBug485805_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	String buffer =	"""
		@FunctionalInterface
		interface I {}
		interface J {
			int foo(int a, int b);
		}
		public class X implements I{
			public J bar() {
			    return (I &  J) (e1, e2) -> {return 0;};
			}
		}
		""";

	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java", buffer);
	IType type = this.workingCopies[0].getType("I");
	try {
		search(type, IMPLEMENTORS);
		assertSearchResults(
			"src/X.java X [I] EXACT_MATCH\n" +
			"src/X.java int J X.bar():<lambda #1>.foo(int, int) [(e1, e2) ->] EXACT_MATCH"
				);
	} catch (UnsupportedOperationException e) {
		assertFalse("Failed", true);
	}
}
public void testBug484367_0001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			interface  Bar {
				public void print();
			}
			
			@FunctionalInterface
			interface Foo {
				void process(Bar bar);
			}
			class BarImpl implements Bar{
				@Override
			//call hierarchy on print does not finds invocation in the below TestMethod class \s
				public void print() {}
			}
			
			public class X {
				public void test(){
					Foo foo1 = (bar)->bar.print();
					Foo foo2 = Bar::print;
				}
			}
			"""
		);
	IType type = this.workingCopies[0].getType("BarImpl");
	IMethod method = type.getMethod("print", null);
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
		"src/X.java void void X.test():<lambda #1>.process(Bar) [print()] EXACT_MATCH\n" +
		"src/X.java void X.test() [print] EXACT_MATCH"
	);
}
public void testBug484367_0002() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			interface  Bar2 {
				public void print();
			}
			interface Bar1 extends Bar2 {
				public void print();
			}
			class Bar implements Bar1 {
			
				@Override
				public void print() {}
			}
			
			@FunctionalInterface
			interface Foo {
				void process(Bar bar);
			}
			class BarImpl extends Bar{
				public void print() {}
			}
			
			public class X {
				@SuppressWarnings("unused")
				public void test(){
					Foo foo1 = (bar)->bar.print();
					Foo foo2 = Bar::print;
				}
			}
			"""
		);
	IType type = this.workingCopies[0].getType("Bar1");
	IMethod method = type.getMethod("print", null);
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
		"src/X.java void void X.test():<lambda #1>.process(Bar) [print()] EXACT_MATCH\n" +
		"src/X.java void X.test() [print] EXACT_MATCH"
	);
}

public void testBug484367_0003() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			interface  Bar1 {
				public void print();
			}
			class Bar implements Bar1 {
			
				@Override
				public void print() {}
			}
			
			@FunctionalInterface
			interface Foo {
				void process(Bar bar);
			}
			class BarImpl extends Bar{
				public void print() {}
			}
			
			public class X {
				@SuppressWarnings("unused")
				public void test(){
					Foo foo1 = (bar)->bar.print();
					Foo foo2 = Bar::print;
				}
			}
			"""
		);
	IType type = this.workingCopies[0].getType("Bar");
	IMethod method = type.getMethod("print", null);
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
		"src/X.java void void X.test():<lambda #1>.process(Bar) [print()] EXACT_MATCH\n" +
		"src/X.java void X.test() [print] EXACT_MATCH"
	);
}

public void testBug531641() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
		createFile("/P/src/Consumer.java",
			"""
				public interface Consumer<T> {
					void accept(T t);
				}
				""");
		createFile("/P/src/C.java",
			"import java.util.*;\n" +
			"\n" +
			"public class C {\n" +
			"	void test() {\n" +
			"		Consumer<String> fun =\n" +
			"			(Map.Entry<String, Collection<String>> it) -> {\n" + // type error here, but shouldn't prevent indexing of the lambda
			"			  print(it);\n" +
			"			};\n" +
			"		print(fun);\n" +
			"	}\n" +
			"	void print(Object o) {}\n" +
			"}\n"
			);
		waitUntilIndexesReady();
		IType type = project.findType("Consumer");
		IMethod method = type.getMethod("accept", new String[] {"QT;"});
		SearchPattern pattern = SearchPattern.createPattern(method, DECLARATIONS, EXACT_RULE);
		search(pattern, SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES), this.resultCollector);
		assertSearchResults(
			"src/C.java void void C.test():<lambda #1>.accept(java.lang.String) [(Map.Entry<String, Collection<String>> it) ->] EXACT_MATCH\n" +
			"src/Consumer.java void Consumer.accept(T) [accept] EXACT_MATCH"
		);
	} finally {
		deleteProject("P");
	}
}
public void testBug493433a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			import java.util.stream.IntStream;
			public class CallerHierarchyExample {
			    interface Interface {
			        void method(int i);
			    }
			    class Implementation implements Interface {
			        public void method(int i) {}
			    }
			    void caller() {
			        Interface pred = new Implementation();
			        IntStream.range(0, 3).forEach(pred::method);
			    }
			}"""
		);
	IType type = this.workingCopies[0].getType("CallerHierarchyExample");
	IJavaElement[] children = type.getChildren();
	IType memberType = null;
	for (IJavaElement iJavaElement : children) {
		if (iJavaElement.getElementType() == IJavaElement.TYPE &&
				iJavaElement.getElementName().equals("Implementation")) {
			memberType = (IType) iJavaElement;
			break;
		}
	}
	assertNotNull("type should not be null", memberType);
	IMethod method = null;
	IMethod[] methods = memberType.getMethods();
	for (IMethod iMethod : methods) {
		if (iMethod.getElementName().equals("method")) {
			method = iMethod;
			break;
		}
	}
	assertNotNull("type should not be null", method);
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
		"src/X.java void CallerHierarchyExample.caller() [method] EXACT_MATCH"
	);
}
public void testBug493433b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			import java.util.stream.IntStream;
			public class CallerHierarchyExample {
			    interface Interface {
			        void method(int i);
			    }
			    class Implementation implements Interface {
			        public void method(int i) {}
			    }
			    void caller() {
			        Interface pred = new Implementation();
			        IntStream.range(0, 3).forEach(pred::method);
			    }
			}"""
		);
	IType type = this.workingCopies[0].getType("CallerHierarchyExample");
	IJavaElement[] children = type.getChildren();
	IType memberType = null;
	for (IJavaElement iJavaElement : children) {
		if (iJavaElement.getElementType() == IJavaElement.TYPE &&
				iJavaElement.getElementName().equals("Interface")) {
			memberType = (IType) iJavaElement;
			break;
		}
	}
	assertNotNull("type should not be null", memberType);
	IMethod method = null;
	IMethod[] methods = memberType.getMethods();
	for (IMethod iMethod : methods) {
		if (iMethod.getElementName().equals("method")) {
			method = iMethod;
			break;
		}
	}
	assertNotNull("type should not be null", method);
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
		"src/X.java void CallerHierarchyExample.caller() [method] EXACT_MATCH"
	);
}
public void testBug574194() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			
			public class X {
				/**
				 * This is abc in X
				 */
				public void abc(int f, char t) {
				\t
				}
			\t
				/**
				 * @see X#abc (
				 * int f, char t)
				 * @see X#abc ( int f,\s
				 * char t)
				 * @see X#abc(int f, char t)
				 * @see X#abc (int f , char t)
				 */
				public void def() {
				\t
				}
			}"""
		);
	IType type = this.workingCopies[0].getType("X");
	assertNotNull("type should not be null", type);
	IMethod method = null;
	IMethod[] methods = type.getMethods();
	for (IMethod iMethod : methods) {
		if (iMethod.getElementName().equals("abc")) {
			method = iMethod;
			break;
		}
	}
	assertNotNull("type should not be null", method);
	search(method, REFERENCES, EXACT_RULE);
	assertSearchResults(
		"""
			src/X.java void X.def() [abc (
				 * int f, char t)] EXACT_MATCH
			src/X.java void X.def() [abc ( int f,\s
				 * char t)] EXACT_MATCH
			src/X.java void X.def() [abc(int f, char t)] EXACT_MATCH
			src/X.java void X.def() [abc (int f , char t)] EXACT_MATCH"""
	);
}
// Add new tests in JavaSearchBugs8Tests
}

