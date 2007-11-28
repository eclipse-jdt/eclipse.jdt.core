/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;


/**
 * Specific test suite for fine grained search.
 * 
 * @bug 155013: [search] [DCR] More finegrained options for Java search
 * @test Several tests sets trying to cover as many cases as possible.
 * <p>
 * Each set is organize the same way:
 * <ul>
 * 	<li>specific method defining the test case called by all the tests of the set,</li>
 * 	<li>first test performs a search request without any fine grain flag defined
 * 		to have a precise idea of what are the entire expected references,</li>
 * 	<li>second test performs a search request with all fine grain flags
 * 		defined to  verify that flags combination works well</li>
 * 	<li>each following test each defines one of the possible fine grain flag
 * 		to verify that it works properly with the given test case,</li>
 * </ul>
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=155013"
 */
public class JavaSearchFineGrainTests extends JavaSearchTests {

public JavaSearchFineGrainTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchFineGrainTests.class);
}
static {
//	org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
}

IJavaSearchScope getJavaSearchScope() {
	return super.getJavaSearchScope15();
}
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	if (this.wcOwner == null) {
		this.wcOwner = new WorkingCopyOwner() {};
	}
	return getWorkingCopy(path, source, this.wcOwner);
}

protected void setUp () throws Exception {
	super.setUp();
	this.resultCollector = new JavaSearchResultCollector();
	resultCollector.showInsideDoc = false;
	resultCollector.showAccuracy = true;
	this.resultCollector.showSelection = true;
	this.resultCollector.showOffset = true;
}

static {
//	TESTS_PREFIX = "testMethodRef";
}

/*
 * References to a specific IField
 */
private final static int ALL_FIELD_METHOD_FINE_GRAIN_FLAGS =
	SUPER_REFERENCE |
	QUALIFIED_REFERENCE |
	THIS_REFERENCE |
	IMPLICIT_THIS_REFERENCE;
private IField setUpFieldRef() throws JavaModelException {
	final ProblemRequestor problemRequestor = new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
			return problemRequestor;
		}
	};
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearch15/src/field/def/Fields.java",
		"package field.def;\n" + 
		"public class Fields {\n" + 
		"	public Object field;\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearch15/src/field/ref/TestFields.java",
		"package field.ref;\n" + 
		"import field.def.Fields;\n" + 
		"public class TestFields extends Fields {\n" + 
		"	boolean foo(Fields param) {\n" + 
		"		return super.field == null ||\n" + 
		"			param.field == null ||\n" + 
		"			this.field == null ||\n" + 
		"			field == null;\n" + 
		"	}\n" + 
		"}\n"
	);
	assertEquals("CU Should not have any problem!",
		"----------\n" +
		"----------\n",
		problemRequestor.problems.toString()
	);
	this.resultCollector.showSelection = true;
	this.resultCollector.showOffset = true;
	return workingCopies[0].getType("Fields").getField("field");
}
public void testFieldRef() throws CoreException {
	search(setUpFieldRef(), REFERENCES);
	assertSearchResults(
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [		return super.§|field|§ == null ||@129] EXACT_MATCH\n" + 
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [			param.§|field|§ == null ||@155] EXACT_MATCH\n" + 
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [			this.§|field|§ == null ||@180] EXACT_MATCH\n" + 
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [			§|field|§ == null;@200] EXACT_MATCH"
	);
}
public void testFieldRef_AllFlags() throws CoreException {
	search(setUpFieldRef(), ALL_FIELD_METHOD_FINE_GRAIN_FLAGS);
	assertSearchResults(
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [		return super.§|field|§ == null ||@129] EXACT_MATCH\n" + 
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [			param.§|field|§ == null ||@155] EXACT_MATCH\n" + 
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [			this.§|field|§ == null ||@180] EXACT_MATCH\n" + 
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [			§|field|§ == null;@200] EXACT_MATCH"
	);
}
public void testFieldRef_Qualified() throws CoreException {
	search(setUpFieldRef(), QUALIFIED_REFERENCE);
	assertSearchResults(
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [			param.§|field|§ == null ||@155] EXACT_MATCH"
	);
}
public void testFieldRef_Simple() throws CoreException {
	search(setUpFieldRef(), IMPLICIT_THIS_REFERENCE);
	assertSearchResults(
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [			§|field|§ == null;@200] EXACT_MATCH"
	);
}
public void testFieldRef_Super() throws CoreException {
	search(setUpFieldRef(), SUPER_REFERENCE);
	assertSearchResults(
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [		return super.§|field|§ == null ||@129] EXACT_MATCH"
	);
}
public void testFieldRef_This() throws CoreException {
	search(setUpFieldRef(), THIS_REFERENCE);
	assertSearchResults(
		"src/field/ref/TestFields.java boolean field.ref.TestFields.foo(Fields) [			this.§|field|§ == null ||@180] EXACT_MATCH"
	);
}

/*
 * References to a specific IMethod
 */
private IMethod setUpMethodRef() throws JavaModelException {
	final ProblemRequestor problemRequestor = new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
			return problemRequestor;
		}
	};
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearch15/src/meth/def/Methods.java",
		"package meth.def;\n" + 
		"public class Methods {\n" + 
		"	public void method() {}\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearch15/src/meth/ref/TestMethods.java",
		"package meth.ref;\n" + 
		"import meth.def.Methods;\n" + 
		"public class TestMethods extends Methods {\n" + 
		"	void foo(Methods param) {\n" + 
		"		super.method();\n" + 
		"		param.method();\n" + 
		"		this.method();\n" + 
		"		method();\n" + 
		"	}\n" + 
		"}\n"
	);
	assertEquals("CU Should not have any problem!",
		"----------\n" +
		"----------\n",
		problemRequestor.problems.toString()
	);
	this.resultCollector.showSelection = true;
	this.resultCollector.showOffset = true;
	return workingCopies[0].getType("Methods").getMethod("method", new String[0]);
}
public void testMethodRef() throws CoreException {
	search(setUpMethodRef(), REFERENCES);
	assertSearchResults(
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		super.§|method()|§;@121] EXACT_MATCH\n" + 
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		param.§|method()|§;@139] EXACT_MATCH\n" + 
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		this.§|method()|§;@156] EXACT_MATCH\n" + 
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		§|method()|§;@168] EXACT_MATCH"
	);
}
public void testMethodRef_AllFlags() throws CoreException {
	search(setUpMethodRef(), ALL_FIELD_METHOD_FINE_GRAIN_FLAGS);
	assertSearchResults(
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		super.§|method()|§;@121] EXACT_MATCH\n" + 
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		param.§|method()|§;@139] EXACT_MATCH\n" + 
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		this.§|method()|§;@156] EXACT_MATCH\n" + 
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		§|method()|§;@168] EXACT_MATCH"
	);
}
public void testMethodRef_Qualified() throws CoreException {
	search(setUpMethodRef(), QUALIFIED_REFERENCE);
	assertSearchResults(
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		param.§|method()|§;@139] EXACT_MATCH"
	);
}
public void testMethodRef_Simple() throws CoreException {
	search(setUpMethodRef(), IMPLICIT_THIS_REFERENCE);
	assertSearchResults(
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		§|method()|§;@168] EXACT_MATCH"
	);
}
public void testMethodRef_Super() throws CoreException {
	search(setUpMethodRef(), SUPER_REFERENCE);
	assertSearchResults(
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		super.§|method()|§;@121] EXACT_MATCH"
	);
}
public void testMethodRef_This() throws CoreException {
	search(setUpMethodRef(), THIS_REFERENCE);
	assertSearchResults(
		"src/meth/ref/TestMethods.java void meth.ref.TestMethods.foo(Methods) [		this.§|method()|§;@156] EXACT_MATCH"
	);
}

private final static int ALL_TYPE_FINE_GRAIN_FLAGS =
	FIELD_DECLARATION_TYPE_REFERENCE |
	LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE |
	PARAMETER_DECLARATION_TYPE_REFERENCE |
	SUPERTYPE_TYPE_REFERENCE |
	SUPERINTERFACE_TYPE_REFERENCE |
	THROWS_CLAUSE_TYPE_REFERENCE |
	CAST_TYPE_REFERENCE |
	CATCH_TYPE_REFERENCE |
	ALLOCATION_EXPRESSION_TYPE_REFERENCE |
	RETURN_TYPE_REFERENCE |
	IMPORT_DECLARATION_TYPE_REFERENCE |
	ANNOTATION_TYPE_REFERENCE |
	TYPE_VARIABLE_BOUND_TYPE_REFERENCE |
	TYPE_ARGUMENT_TYPE_REFERENCE |
	WILDCARD_BOUND_TYPE_REFERENCE;

/*
 * References to a specific IType
 */
private IType setUpTypeRef(int typeIndex) throws JavaModelException {
	final ProblemRequestor problemRequestor = new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
			return problemRequestor;
		}
	};
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearch15/src/type/def/Types.java",
		"package type.def;\n" + 
		"@Bug\n" + 
		"@ATest\n" + 
		"public class Types extends Exception implements ITest1, ITest2 {\n" +
		"	ITest1 test1;\n" +
		"	ITest2 test2;\n" +
		"}\n" + 
		"@Bug\n" + 
		"interface ITest1 {}\n" + 
		"@ATest\n" + 
		"interface ITest2 extends ITest1 {}\n" + 
		"@Bug\n" + 
		"@ATest\n" + 
		"enum ETest {}\n" + 
		"@Bug\n" + 
		"@interface ATest {}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearch15/src/type/def/Bug.java",
		"package type.def;\n" + 
		"public @interface Bug {\n" + 
		"	int num() default 0;\n" + 
		"	String comment() default \"\";\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearch15/src/type/ref/TestTypes.java",
		"package type.ref;\n" + 
		"import type.def.Types;\n" + 
		"import type.def.Bug;\n" + 
		"\n" + 
		"@Bug(num=155013)\n" + 
		"public class TestTypes extends Types {\n" + 
		"	@Bug(comment=\"field\")\n" + 
		"	Types field;\n" + 
		"	@Bug(comment=\"method\", num=155013)\n" + 
		"	Types method(Types param) throws Types {\n" + 
		"		Object obj = new Types();\n" + 
		"		Types local = (Types) obj;\n" + 
		"		return local;\n" + 
		"	}\n" + 
		"	Bug bar(Bug bug) {\n" + 
		"		try {\n" + 
		"			method(this);\n" + 
		"		} catch (Types ex) {\n" + 
		"		}\n" +
		"		return bug;\n" + 
		"	}\n" + 
		"}\n"
	);
	assertEquals("CU Should not have any problem!",
		"----------\n" +
		"----------\n",
		problemRequestor.problems.toString()
	);
	ICompilationUnit cu = workingCopies[typeIndex];
	String cuName = cu.getElementName();
	return cu.getType(cuName.substring(0, cuName.indexOf('.')));
}
public void testTypeRef() throws CoreException {
	search(setUpTypeRef(0), REFERENCES);
	assertSearchResults(
		"src/type/ref/TestTypes.java [import §|type.def.Types|§;@25] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java type.ref.TestTypes [public class TestTypes extends §|Types|§ {@111] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java type.ref.TestTypes.field [	§|Types|§ field;@143] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	§|Types|§ method(Types param) throws Types {@193] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	Types method(§|Types|§ param) throws Types {@206] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	Types method(Types param) throws §|Types|§ {@226] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [		Object obj = new §|Types|§();@253] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [		§|Types|§ local = (Types) obj;@264] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [		Types local = (§|Types|§) obj;@279] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Bug type.ref.TestTypes.bar(Bug) [		} catch (§|Types|§ ex) {@366] EXACT_MATCH"
	);
}
public void testTypeRef_AllFlags() throws CoreException {
	search(setUpTypeRef(0), ALL_TYPE_FINE_GRAIN_FLAGS);
	assertSearchResults(
		"src/type/ref/TestTypes.java [import §|type.def.Types|§;@25] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java type.ref.TestTypes [public class TestTypes extends §|Types|§ {@111] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java type.ref.TestTypes.field [	§|Types|§ field;@143] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	Types method(§|Types|§ param) throws Types {@206] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	Types method(Types param) throws §|Types|§ {@226] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	§|Types|§ method(Types param) throws Types {@193] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [		Object obj = new §|Types|§();@253] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [		§|Types|§ local = (Types) obj;@264] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [		Types local = (§|Types|§) obj;@279] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Bug type.ref.TestTypes.bar(Bug) [		} catch (§|Types|§ ex) {@366] EXACT_MATCH"
	);
}
public void testTypeRef_Allocation() throws CoreException {
	search(setUpTypeRef(0), ALLOCATION_EXPRESSION_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [		Object obj = new §|Types|§();@253] EXACT_MATCH"
	);
}
public void testTypeRef_Annotation() throws CoreException {
	search(setUpTypeRef(1), ANNOTATION_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/def/Types.java type.def.Types [@§|Bug|§@19] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ITest1 [@§|Bug|§@128] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ETest [@§|Bug|§@195] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ATest [@§|Bug|§@221] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java type.ref.TestTypes [@§|Bug|§(num=155013)@64] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java type.ref.TestTypes.field [	@§|Bug|§(comment=\"field\")@121] EXACT_MATCH\n" + 
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	@§|Bug|§(comment=\"method\", num=155013)@158] EXACT_MATCH"
	);
}
public void testTypeRef_Cast() throws CoreException {
	search(setUpTypeRef(0), CAST_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [		Types local = (§|Types|§) obj;@279] EXACT_MATCH"
	);
}
public void testTypeRef_Catch() throws CoreException {
	search(setUpTypeRef(0), CATCH_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java Bug type.ref.TestTypes.bar(Bug) [		} catch (§|Types|§ ex) {@366] EXACT_MATCH"
	);
}
public void testTypeRef_Field() throws CoreException {
	search(setUpTypeRef(0), FIELD_DECLARATION_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java type.ref.TestTypes.field [	§|Types|§ field;@143] EXACT_MATCH"
	);
}
public void testTypeRef_Import() throws CoreException {
	search(setUpTypeRef(0), IMPORT_DECLARATION_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java [import §|type.def.Types|§;@25] EXACT_MATCH"
	);
}
public void testTypeRef_MethodParameter() throws CoreException {
	search(setUpTypeRef(0), PARAMETER_DECLARATION_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	Types method(§|Types|§ param) throws Types {@206] EXACT_MATCH"
	);
}
public void testTypeRef_Return() throws CoreException {
	search(setUpTypeRef(0), RETURN_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	§|Types|§ method(Types param) throws Types {@193] EXACT_MATCH"
	);
}
public void testTypeRef_Superinterface() throws CoreException {
	IType type = setUpTypeRef(0);
	search(((ICompilationUnit) type.getParent()).getType("ITest1"), SUPERINTERFACE_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/type/def/Types.java type.def.Types [public class Types extends Exception implements §|ITest1|§, ITest2 {@78] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ITest2 [interface ITest2 extends §|ITest1|§ {}@184] EXACT_MATCH"
	);
}
public void testTypeRef_Supertype() throws CoreException {
	search(setUpTypeRef(0), SUPERTYPE_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java type.ref.TestTypes [public class TestTypes extends §|Types|§ {@111] EXACT_MATCH"
	);
}
public void testTypeRef_Throws() throws CoreException {
	search(setUpTypeRef(0), THROWS_CLAUSE_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [	Types method(Types param) throws §|Types|§ {@226] EXACT_MATCH"
	);
}
public void testTypeRef_Variable() throws CoreException {
	search(setUpTypeRef(0), LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE);
	assertSearchResults(
		"src/type/ref/TestTypes.java Types type.ref.TestTypes.method(Types) [		§|Types|§ local = (Types) obj;@264] EXACT_MATCH"
	);
}

/*
 * References to a all types (using '*' string pattern)
 */
private void setUpTypeRefAll() throws JavaModelException {
	final ProblemRequestor problemRequestor = new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
			return problemRequestor;
		}
	};
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearch15/src/type/def/Types.java",
		"package type.def;\n" + 
		"@Bug\n" + 
		"@ATest\n" + 
		"public class Types extends Exception implements ITest1, ITest2 {\n" +
		"	ITest1 test1;\n" +
		"	ITest2 test2;\n" +
		"}\n" + 
		"@Bug\n" + 
		"interface ITest1 {}\n" + 
		"@ATest\n" + 
		"interface ITest2 extends ITest1 {}\n" + 
		"@Bug\n" + 
		"@ATest\n" + 
		"enum ETest {}\n" + 
		"@Bug\n" + 
		"@interface ATest {}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearch15/src/type/def/Bug.java",
		"package type.def;\n" + 
		"public @interface Bug {\n" + 
		"	int num() default 0;\n" + 
		"	String comment() default \"\";\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearch15/src/all/types/ref/TestTypes.java",
		"package all.types.ref;\n" + 
		"\n" + 
		"import type.def.Bug;\n" + 
		"import type.def.Types;\n" + 
		"\n" + 
		"@Bug(num=155013)\n" + 
		"public class TestTypes extends Types {\n" + 
		"	@Bug(comment=\"field\")\n" + 
		"	Types field = new Types(), local, other = new Types();\n" + 
		"	{\n" + 
		"		Object o;\n" + 
		"		Types t;\n" + 
		"		if (this.field == null) {\n" + 
		"			try {\n" + 
		"				t = new TestTypes();\n" + 
		"			} catch (RuntimeException e) {\n" + 
		"				t = new Types();\n" + 
		"			} \n" + 
		"		} else {\n" + 
		"			o = this.field;\n" + 
		"			t = (Types) o;\n" + 
		"		}\n" + 
		"		local = t;\n" + 
		"	};\n" + 
		"	@Bug(comment=\"method\", num=155013)\n" + 
		"	Types method(Types param) throws Types {\n" + 
		"		Object obj = new Types();\n" + 
		"		Types local = (Types) obj;\n" + 
		"		return local;\n" + 
		"	}\n" + 
		"	Bug bar(Bug bug) {\n" + 
		"		try {\n" + 
		"			method(this);\n" + 
		"		}\n" + 
		"		catch (Types ex) {}\n" + 
		"		catch (Exception ex) {}\n" + 
		"		return bug; \n" + 
		"	}\n" + 
		"	// Other types references\n" + 
		"	Object object;\n" + 
		"	String str;\n" + 
		"	TestTypes() throws Types, RuntimeException {\n" + 
		"		if (this.object instanceof String) {\n" + 
		"			this.str = (String) this.object;\n" + 
		"		} else {\n" + 
		"			this.str = new String();\n" + 
		"			this.object = new Object();\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n"
	);
	assertEquals("CU Should not have any problem!",
		"----------\n" +
		"----------\n",
		problemRequestor.problems.toString()
	);
	this.resultCollector.showSelection = true;
	this.resultCollector.showOffset = true;
}
public void testTypeRefAll_Allocation() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, ALLOCATION_EXPRESSION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.field [	Types field = new §|Types|§(), local, other = new Types();@167] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.other [	Types field = new Types(), local, other = new §|Types|§();@195] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [				t = new §|TestTypes|§();@279] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [				t = new §|Types|§();@338] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java Types all.types.ref.TestTypes.method(Types) [		Object obj = new §|Types|§();@519] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [			this.str = new §|String|§();@897] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [			this.object = new §|Object|§();@928] EXACT_MATCH"
	);
}
public void testTypeRefAll_Annotation() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, ANNOTATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes [@§|Bug|§(num=155013)@70] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.field [	@§|Bug|§(comment=\"field\")@127] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java Types all.types.ref.TestTypes.method(Types) [	@§|Bug|§(comment=\"method\", num=155013)@424] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types [@§|Bug|§@19] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types [@§|ATest|§@24] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ITest1 [@§|Bug|§@128] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ITest2 [@§|ATest|§@153] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ETest [@§|Bug|§@195] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ETest [@§|ATest|§@200] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ATest [@§|Bug|§@221] EXACT_MATCH"
	);
}
public void testTypeRefAll_Cast() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, CAST_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [			t = (§|Types|§) o;@391] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java Types all.types.ref.TestTypes.method(Types) [		Types local = (§|Types|§) obj;@545] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [			this.str = (§|String|§) this.object;@847] EXACT_MATCH"
	);
}
public void testTypeRefAll_Catch() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, CATCH_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [			} catch (§|RuntimeException|§ e) {@304] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java Bug all.types.ref.TestTypes.bar(Bug) [		catch (§|Types|§ ex) {}@634] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java Bug all.types.ref.TestTypes.bar(Bug) [		catch (§|Exception|§ ex) {}@656] EXACT_MATCH"
	);
}
public void testTypeRefAll_Field() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, FIELD_DECLARATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.field [	§|Types|§ field = new Types(), local, other = new Types();@149] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.object [	§|Object|§ object;@719] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.str [	§|String|§ str;@735] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types.test1 [	§|ITest1|§ test1;@96] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types.test2 [	§|ITest2|§ test2;@111] EXACT_MATCH"
	);
}
public void testTypeRefAll_Import() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, IMPORT_DECLARATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java [import §|type.def.Bug|§;@31] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java [import §|type.def.Types|§;@52] EXACT_MATCH"
	);
}
public void testTypeRefAll_MethodParameter() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, PARAMETER_DECLARATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java Types all.types.ref.TestTypes.method(Types) [	Types method(§|Types|§ param) throws Types {@472] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java Bug all.types.ref.TestTypes.bar(Bug) [	Bug bar(§|Bug|§ bug) {@585] EXACT_MATCH"
	);
}
public void testTypeRefAll_Return() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, RETURN_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java Types all.types.ref.TestTypes.method(Types) [	§|Types|§ method(Types param) throws Types {@459] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java Bug all.types.ref.TestTypes.bar(Bug) [	§|Bug|§ bar(Bug bug) {@577] EXACT_MATCH"
	);
}
public void testTypeRefAll_Superinterface() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, SUPERINTERFACE_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/type/def/Types.java type.def.Types [public class Types extends Exception implements §|ITest1|§, ITest2 {@78] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types [public class Types extends Exception implements ITest1, §|ITest2|§ {@86] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ITest2 [interface ITest2 extends §|ITest1|§ {}@184] EXACT_MATCH"
	);
}
public void testTypeRefAll_Supertype() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, SUPERTYPE_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes [public class TestTypes extends §|Types|§ {@117] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types [public class Types extends §|Exception|§ implements ITest1, ITest2 {@57] EXACT_MATCH"
	);
}
public void testTypeRefAll_Throws() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, THROWS_CLAUSE_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java Types all.types.ref.TestTypes.method(Types) [	Types method(Types param) throws §|Types|§ {@492] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [	TestTypes() throws §|Types|§, RuntimeException {@767] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [	TestTypes() throws Types, §|RuntimeException|§ {@774] EXACT_MATCH"
	);
}
public void testTypeRefAll_Variable() throws CoreException {
	setUpTypeRefAll();
	search("*", TYPE, LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [		§|Object|§ o;@209] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [		§|Types|§ t;@221] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java Types all.types.ref.TestTypes.method(Types) [		§|Object|§ obj = new Types();@502] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java Types all.types.ref.TestTypes.method(Types) [		§|Types|§ local = (Types) obj;@530] EXACT_MATCH"
	);
}

private final static int ALL_GENERIC_TYPE_FINE_GRAIN_FLAGS =
	TYPE_VARIABLE_BOUND_TYPE_REFERENCE |
	TYPE_ARGUMENT_TYPE_REFERENCE |
	WILDCARD_BOUND_TYPE_REFERENCE;

/*
 * References to a specific generic IType and all types.
 */
private IType setUpTypeRefGeneric01() throws JavaModelException {
	final ProblemRequestor problemRequestor = new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
			return problemRequestor;
		}
	};
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearch15/src/type/def/Types.java",
		"package type.def;\n" + 
		"public class Types {}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearch15/src/test01/Generic.java",
		"package test01;\n" + 
		"import java.io.Serializable;\n" + 
		"import type.def.Types;\n" + 
		"public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {\n" + 
		"	Generic<? extends Types, ?, ?> field;\n" + 
		"	Comparable<String> comp;\n" + 
		"	Class<? extends Exception> clazz;\n" + 
		"}\n" + 
		"class A<R> {}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearch15/src/test01/TestGeneric.java",
		"package test01;\n" + 
		"import java.io.Serializable;\n" + 
		"import type.def.Types;\n" + 
		"public class TestGeneric<T> extends Generic<Types, UClass, VClass> {\n" + 
		"	TestGeneric<Types> test;\n" + 
		"	TestGeneric<String> foo(TestGeneric<Types> param1, Comparable<String> param2) {\n" + 
		"		return  null;\n" + 
		"	}\n" + 
		"	Comparable<TestGeneric<Types>> bar() {\n" + 
		"		return null;\n" + 
		"	} \n" + 
		"}\n" + 
		"class UClass extends Types implements Comparable<Types>, Serializable {\n" + 
		"	private static final long serialVersionUID = 1L;\n" + 
		"	public int compareTo(Types o) {\n" + 
		"		return 0;\n" + 
		"	}\n" + 
		"}\n" + 
		"class VClass extends A<Types> {}\n"
	);
	assertEquals("CU Should not have any problem!",
		"----------\n" +
		"----------\n",
		problemRequestor.problems.toString()
	);
	this.resultCollector.showSelection = true;
	this.resultCollector.showOffset = true;
	return workingCopies[0].getType("Types");
}
public void testTypeRefGeneric01() throws CoreException {
	search(setUpTypeRefGeneric01(), REFERENCES);
	assertSearchResults(
		"src/test01/Generic.java [import §|type.def.Types|§;@52] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends §|Types|§, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {@99] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends §|Types|§ & Comparable<Types> & Serializable, V extends A<? super Types>> {@116] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<§|Types|§> & Serializable, V extends A<? super Types>> {@135] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super §|Types|§>> {@178] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.field [	Generic<? extends §|Types|§, ?, ?> field;@207] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java [import §|type.def.Types|§;@52] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<§|Types|§, UClass, VClass> {@112] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric.test [	TestGeneric<§|Types|§> test;@150] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<§|Types|§> param1, Comparable<String> param2) {@200] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	Comparable<TestGeneric<§|Types|§>> bar() {@287] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends §|Types|§ implements Comparable<Types>, Serializable {@345] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends Types implements Comparable<§|Types|§>, Serializable {@373] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java int test01.UClass.compareTo(Types) [	public int compareTo(§|Types|§ o) {@468] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.VClass [class VClass extends A<§|Types|§> {}@519] EXACT_MATCH"
	);
}
public void testTypeRefGeneric01_AllFlags() throws CoreException {
	search(setUpTypeRefGeneric01(), ALL_TYPE_FINE_GRAIN_FLAGS);
	assertSearchResults(
		"src/test01/Generic.java [import §|type.def.Types|§;@52] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends §|Types|§, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {@99] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends §|Types|§ & Comparable<Types> & Serializable, V extends A<? super Types>> {@116] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<§|Types|§> & Serializable, V extends A<? super Types>> {@135] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super §|Types|§>> {@178] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.field [	Generic<? extends §|Types|§, ?, ?> field;@207] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java [import §|type.def.Types|§;@52] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<§|Types|§, UClass, VClass> {@112] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric.test [	TestGeneric<§|Types|§> test;@150] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<§|Types|§> param1, Comparable<String> param2) {@200] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	Comparable<TestGeneric<§|Types|§>> bar() {@287] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends §|Types|§ implements Comparable<Types>, Serializable {@345] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends Types implements Comparable<§|Types|§>, Serializable {@373] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java int test01.UClass.compareTo(Types) [	public int compareTo(§|Types|§ o) {@468] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.VClass [class VClass extends A<§|Types|§> {}@519] EXACT_MATCH"
	);
}
public void testTypeRefGeneric01_Parameterized() throws CoreException {
	search(setUpTypeRefGeneric01(), TYPE_ARGUMENT_TYPE_REFERENCE);
	assertSearchResults(
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<§|Types|§> & Serializable, V extends A<? super Types>> {@135] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<§|Types|§, UClass, VClass> {@112] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric.test [	TestGeneric<§|Types|§> test;@150] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<§|Types|§> param1, Comparable<String> param2) {@200] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	Comparable<TestGeneric<§|Types|§>> bar() {@287] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends Types implements Comparable<§|Types|§>, Serializable {@373] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.VClass [class VClass extends A<§|Types|§> {}@519] EXACT_MATCH"
	);
}
public void testTypeRefGeneric01_TypeVariableBound() throws CoreException {
	search(setUpTypeRefGeneric01(), TYPE_VARIABLE_BOUND_TYPE_REFERENCE);
	assertSearchResults(
		"src/test01/Generic.java test01.Generic [public class Generic<T extends §|Types|§, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {@99] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends §|Types|§ & Comparable<Types> & Serializable, V extends A<? super Types>> {@116] EXACT_MATCH"
	);
}
public void testTypeRefGeneric01_WildcardBound() throws CoreException {
	search(setUpTypeRefGeneric01(), WILDCARD_BOUND_TYPE_REFERENCE);
	assertSearchResults(
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super §|Types|§>> {@178] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.field [	Generic<? extends §|Types|§, ?, ?> field;@207] EXACT_MATCH"
	);
}
public void testTypeRefGenericAll01() throws CoreException {
	setUpTypeRefGeneric01();
	search("*", TYPE, REFERENCES, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test01/Generic.java [import §|java.io.Serializable|§;@23] EXACT_MATCH\n" + 
		"src/test01/Generic.java [import §|type.def.Types|§;@52] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends §|Types|§, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {@99] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends §|Types|§ & Comparable<Types> & Serializable, V extends A<? super Types>> {@116] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & §|Comparable|§<Types> & Serializable, V extends A<? super Types>> {@124] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<§|Types|§> & Serializable, V extends A<? super Types>> {@135] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & §|Serializable|§, V extends A<? super Types>> {@144] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends §|A|§<? super Types>> {@168] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super §|Types|§>> {@178] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.field [	§|Generic|§<? extends Types, ?, ?> field;@189] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.field [	Generic<? extends §|Types|§, ?, ?> field;@207] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.comp [	§|Comparable|§<String> comp;@228] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.comp [	Comparable<§|String|§> comp;@239] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.clazz [	§|Class|§<? extends Exception> clazz;@254] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.clazz [	Class<? extends §|Exception|§> clazz;@270] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java [import §|java.io.Serializable|§;@23] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java [import §|type.def.Types|§;@52] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends §|Generic|§<Types, UClass, VClass> {@104] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<§|Types|§, UClass, VClass> {@112] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<Types, §|UClass|§, VClass> {@119] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<Types, UClass, §|VClass|§> {@127] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric.test [	§|TestGeneric|§<Types> test;@138] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric.test [	TestGeneric<§|Types|§> test;@150] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	§|TestGeneric|§<String> foo(TestGeneric<Types> param1, Comparable<String> param2) {@164] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<§|String|§> foo(TestGeneric<Types> param1, Comparable<String> param2) {@176] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(§|TestGeneric|§<Types> param1, Comparable<String> param2) {@188] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<§|Types|§> param1, Comparable<String> param2) {@200] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<Types> param1, §|Comparable|§<String> param2) {@215] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<Types> param1, Comparable<§|String|§> param2) {@226] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	§|Comparable|§<TestGeneric<Types>> bar() {@264] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	Comparable<§|TestGeneric|§<Types>> bar() {@275] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	Comparable<TestGeneric<§|Types|§>> bar() {@287] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends §|Types|§ implements Comparable<Types>, Serializable {@345] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends Types implements §|Comparable|§<Types>, Serializable {@362] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends Types implements Comparable<§|Types|§>, Serializable {@373] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends Types implements Comparable<Types>, §|Serializable|§ {@381] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass.serialVersionUID [	private static final §|long|§ serialVersionUID = 1L;@418] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java int test01.UClass.compareTo(Types) [	public §|int|§ compareTo(Types o) {@454] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java int test01.UClass.compareTo(Types) [	public int compareTo(§|Types|§ o) {@468] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.VClass [class VClass extends §|A|§<Types> {}@517] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.VClass [class VClass extends A<§|Types|§> {}@519] EXACT_MATCH"
	);
}
public void testTypeRefGenericAll01_AllGenericFlags() throws CoreException {
	setUpTypeRefGeneric01();
	search("*", TYPE, ALL_GENERIC_TYPE_FINE_GRAIN_FLAGS, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test01/Generic.java test01.Generic [public class Generic<T extends §|Types|§, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {@99] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends §|Types|§ & Comparable<Types> & Serializable, V extends A<? super Types>> {@116] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & §|Comparable|§<Types> & Serializable, V extends A<? super Types>> {@124] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<§|Types|§> & Serializable, V extends A<? super Types>> {@135] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & §|Serializable|§, V extends A<? super Types>> {@144] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends §|A|§<? super Types>> {@168] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super §|Types|§>> {@178] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.field [	Generic<? extends §|Types|§, ?, ?> field;@207] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.comp [	Comparable<§|String|§> comp;@239] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.clazz [	Class<? extends §|Exception|§> clazz;@270] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<§|Types|§, UClass, VClass> {@112] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<Types, §|UClass|§, VClass> {@119] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<Types, UClass, §|VClass|§> {@127] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric.test [	TestGeneric<§|Types|§> test;@150] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<§|String|§> foo(TestGeneric<Types> param1, Comparable<String> param2) {@176] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<§|Types|§> param1, Comparable<String> param2) {@200] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<Types> param1, Comparable<§|String|§> param2) {@226] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	Comparable<§|TestGeneric|§<Types>> bar() {@275] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	Comparable<TestGeneric<§|Types|§>> bar() {@287] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends Types implements Comparable<§|Types|§>, Serializable {@373] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.VClass [class VClass extends A<§|Types|§> {}@519] EXACT_MATCH"
	);
}
public void testTypeRefGenericAll01_Parameterized() throws CoreException {
	setUpTypeRefGeneric01();
	search("*", TYPE, TYPE_ARGUMENT_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<§|Types|§> & Serializable, V extends A<? super Types>> {@135] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.comp [	Comparable<§|String|§> comp;@239] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<§|Types|§, UClass, VClass> {@112] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<Types, §|UClass|§, VClass> {@119] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric [public class TestGeneric<T> extends Generic<Types, UClass, §|VClass|§> {@127] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.TestGeneric.test [	TestGeneric<§|Types|§> test;@150] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<§|String|§> foo(TestGeneric<Types> param1, Comparable<String> param2) {@176] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<§|Types|§> param1, Comparable<String> param2) {@200] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java TestGeneric<String> test01.TestGeneric.foo(TestGeneric<Types>, Comparable<String>) [	TestGeneric<String> foo(TestGeneric<Types> param1, Comparable<§|String|§> param2) {@226] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	Comparable<§|TestGeneric|§<Types>> bar() {@275] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java Comparable<TestGeneric<Types>> test01.TestGeneric.bar() [	Comparable<TestGeneric<§|Types|§>> bar() {@287] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.UClass [class UClass extends Types implements Comparable<§|Types|§>, Serializable {@373] EXACT_MATCH\n" + 
		"src/test01/TestGeneric.java test01.VClass [class VClass extends A<§|Types|§> {}@519] EXACT_MATCH"
	);
}
public void testTypeRefGenericAll01_TypeVariableBound() throws CoreException {
	setUpTypeRefGeneric01();
	search("*", TYPE, TYPE_VARIABLE_BOUND_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test01/Generic.java test01.Generic [public class Generic<T extends §|Types|§, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {@99] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends §|Types|§ & Comparable<Types> & Serializable, V extends A<? super Types>> {@116] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & §|Comparable|§<Types> & Serializable, V extends A<? super Types>> {@124] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & §|Serializable|§, V extends A<? super Types>> {@144] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends §|A|§<? super Types>> {@168] EXACT_MATCH"
	);
}
public void testTypeRefGenericAll01_WildcardBound() throws CoreException {
	setUpTypeRefGeneric01();
	search("*", TYPE, WILDCARD_BOUND_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test01/Generic.java test01.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super §|Types|§>> {@178] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.field [	Generic<? extends §|Types|§, ?, ?> field;@207] EXACT_MATCH\n" + 
		"src/test01/Generic.java test01.Generic.clazz [	Class<? extends §|Exception|§> clazz;@270] EXACT_MATCH"
	);
}
/*
 * Same test than previous ones with another example
 */
private IType setUpTypeRefGeneric02() throws JavaModelException {
	final ProblemRequestor problemRequestor = new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
			return problemRequestor;
		}
	};
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearch15/src/test02/Test.java",
		"package test02;\n" + 
		"public class Test <T, U, V> {\n" + 
		"	Test<A, ? extends B, ? super C> field = new Test<A, Z<String>, X<String>> () {\n" + 
		"		X<String> x;\n" + 
		"		Test<A, B, C> t;\n" + 
		"		Test<X<Y<Z<String>>>, Y<Z<String>>, Z<String>> bar() {\n" + 
		"			return new Test<X<Y<Z<String>>>, Y<Z<String>>, Z<String>>();\n" + 
		"		}\n" + 
		"	};\n" + 
		"	Test<? super A, B, ? extends C> foo(Test<? extends A, ? super B, C> param) {\n" + 
		"		return null;\n" + 
		"	};\n" + 
		"}\n" + 
		"class A {}\n" + 
		"class B {}\n" + 
		"class C extends X<String> {}\n" + 
		"class X<R> {}\n" + 
		"class Y<S> extends A {}\n" + 
		"class Z<P> extends B {}\n" + 
		"\n"
	);
	assertEquals("CU Should not have any problem!",
		"----------\n" +
		"----------\n",
		problemRequestor.problems.toString()
	);
	this.resultCollector.showSelection = true;
	this.resultCollector.showOffset = true;
	return workingCopies[0].getType("Types");
}
public void testTypeRefGenericAll02() throws CoreException {
	setUpTypeRefGeneric02();
	search("*", TYPE, REFERENCES, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test02/Test.java test02.Test.field:<anonymous>#1 [	Test<A, ? extends B, ? super C> field = new §|Test|§<A, Z<String>, X<String>> () {@91] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.x [		§|X|§<String> x;@128] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.x [		X<§|String|§> x;@130] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		§|Test|§<A, B, C> t;@143] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		Test<§|A|§, B, C> t;@148] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		Test<A, §|B|§, C> t;@151] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		Test<A, B, §|C|§> t;@154] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		§|Test|§<X<Y<Z<String>>>, Y<Z<String>>, Z<String>> bar() {@162] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<§|X|§<Y<Z<String>>>, Y<Z<String>>, Z<String>> bar() {@167] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<§|Y|§<Z<String>>>, Y<Z<String>>, Z<String>> bar() {@169] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<§|Z|§<String>>>, Y<Z<String>>, Z<String>> bar() {@171] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<§|String|§>>>, Y<Z<String>>, Z<String>> bar() {@173] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, §|Y|§<Z<String>>, Z<String>> bar() {@184] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<§|Z|§<String>>, Z<String>> bar() {@186] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<Z<§|String|§>>, Z<String>> bar() {@188] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<Z<String>>, §|Z|§<String>> bar() {@198] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<Z<String>>, Z<§|String|§>> bar() {@200] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new §|Test|§<X<Y<Z<String>>>, Y<Z<String>>, Z<String>>();@231] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<§|X|§<Y<Z<String>>>, Y<Z<String>>, Z<String>>();@236] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<§|Y|§<Z<String>>>, Y<Z<String>>, Z<String>>();@238] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<§|Z|§<String>>>, Y<Z<String>>, Z<String>>();@240] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<§|String|§>>>, Y<Z<String>>, Z<String>>();@242] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, §|Y|§<Z<String>>, Z<String>>();@253] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<§|Z|§<String>>, Z<String>>();@255] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<Z<§|String|§>>, Z<String>>();@257] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<Z<String>>, §|Z|§<String>>();@267] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<Z<String>>, Z<§|String|§>>();@269] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	§|Test|§<A, ? extends B, ? super C> field = new Test<A, Z<String>, X<String>> () {@47] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<§|A|§, ? extends B, ? super C> field = new Test<A, Z<String>, X<String>> () {@52] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends §|B|§, ? super C> field = new Test<A, Z<String>, X<String>> () {@65] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super §|C|§> field = new Test<A, Z<String>, X<String>> () {@76] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<§|A|§, Z<String>, X<String>> () {@96] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, §|Z|§<String>, X<String>> () {@99] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, Z<§|String|§>, X<String>> () {@101] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, Z<String>, §|X|§<String>> () {@110] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, Z<String>, X<§|String|§>> () {@112] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	§|Test|§<? super A, B, ? extends C> foo(Test<? extends A, ? super B, C> param) {@290] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super §|A|§, B, ? extends C> foo(Test<? extends A, ? super B, C> param) {@303] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, §|B|§, ? extends C> foo(Test<? extends A, ? super B, C> param) {@306] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends §|C|§> foo(Test<? extends A, ? super B, C> param) {@319] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(§|Test|§<? extends A, ? super B, C> param) {@326] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(Test<? extends §|A|§, ? super B, C> param) {@341] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(Test<? extends A, ? super §|B|§, C> param) {@352] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(Test<? extends A, ? super B, §|C|§> param) {@355] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.C [class C extends §|X|§<String> {}@426] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.C [class C extends X<§|String|§> {}@428] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Y [class Y<S> extends §|A|§ {}@472] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Z [class Z<P> extends §|B|§ {}@496] EXACT_MATCH"
	);
}
public void testTypeRefGenericAll02_AllGenericFlags() throws CoreException {
	setUpTypeRefGeneric02();
	search("*", TYPE, ALL_GENERIC_TYPE_FINE_GRAIN_FLAGS, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test02/Test.java test02.Test.field:<anonymous>#1.x [		X<§|String|§> x;@130] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		Test<§|A|§, B, C> t;@148] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		Test<A, §|B|§, C> t;@151] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		Test<A, B, §|C|§> t;@154] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<§|X|§<Y<Z<String>>>, Y<Z<String>>, Z<String>> bar() {@167] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<§|Y|§<Z<String>>>, Y<Z<String>>, Z<String>> bar() {@169] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<§|Z|§<String>>>, Y<Z<String>>, Z<String>> bar() {@171] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<§|String|§>>>, Y<Z<String>>, Z<String>> bar() {@173] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, §|Y|§<Z<String>>, Z<String>> bar() {@184] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<§|Z|§<String>>, Z<String>> bar() {@186] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<Z<§|String|§>>, Z<String>> bar() {@188] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<Z<String>>, §|Z|§<String>> bar() {@198] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<Z<String>>, Z<§|String|§>> bar() {@200] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<§|X|§<Y<Z<String>>>, Y<Z<String>>, Z<String>>();@236] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<§|Y|§<Z<String>>>, Y<Z<String>>, Z<String>>();@238] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<§|Z|§<String>>>, Y<Z<String>>, Z<String>>();@240] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<§|String|§>>>, Y<Z<String>>, Z<String>>();@242] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, §|Y|§<Z<String>>, Z<String>>();@253] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<§|Z|§<String>>, Z<String>>();@255] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<Z<§|String|§>>, Z<String>>();@257] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<Z<String>>, §|Z|§<String>>();@267] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<Z<String>>, Z<§|String|§>>();@269] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<§|A|§, ? extends B, ? super C> field = new Test<A, Z<String>, X<String>> () {@52] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends §|B|§, ? super C> field = new Test<A, Z<String>, X<String>> () {@65] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super §|C|§> field = new Test<A, Z<String>, X<String>> () {@76] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<§|A|§, Z<String>, X<String>> () {@96] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, §|Z|§<String>, X<String>> () {@99] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, Z<§|String|§>, X<String>> () {@101] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, Z<String>, §|X|§<String>> () {@110] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, Z<String>, X<§|String|§>> () {@112] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super §|A|§, B, ? extends C> foo(Test<? extends A, ? super B, C> param) {@303] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, §|B|§, ? extends C> foo(Test<? extends A, ? super B, C> param) {@306] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends §|C|§> foo(Test<? extends A, ? super B, C> param) {@319] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(Test<? extends §|A|§, ? super B, C> param) {@341] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(Test<? extends A, ? super §|B|§, C> param) {@352] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(Test<? extends A, ? super B, §|C|§> param) {@355] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.C [class C extends X<§|String|§> {}@428] EXACT_MATCH"
	);
}
public void testTypeRefGenericAll02_Parameterizedl() throws CoreException {
	setUpTypeRefGeneric02();
	search("*", TYPE, TYPE_ARGUMENT_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test02/Test.java test02.Test.field:<anonymous>#1.x [		X<§|String|§> x;@130] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		Test<§|A|§, B, C> t;@148] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		Test<A, §|B|§, C> t;@151] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field:<anonymous>#1.t [		Test<A, B, §|C|§> t;@154] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<§|X|§<Y<Z<String>>>, Y<Z<String>>, Z<String>> bar() {@167] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<§|Y|§<Z<String>>>, Y<Z<String>>, Z<String>> bar() {@169] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<§|Z|§<String>>>, Y<Z<String>>, Z<String>> bar() {@171] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<§|String|§>>>, Y<Z<String>>, Z<String>> bar() {@173] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, §|Y|§<Z<String>>, Z<String>> bar() {@184] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<§|Z|§<String>>, Z<String>> bar() {@186] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<Z<§|String|§>>, Z<String>> bar() {@188] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<Z<String>>, §|Z|§<String>> bar() {@198] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [		Test<X<Y<Z<String>>>, Y<Z<String>>, Z<§|String|§>> bar() {@200] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<§|X|§<Y<Z<String>>>, Y<Z<String>>, Z<String>>();@236] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<§|Y|§<Z<String>>>, Y<Z<String>>, Z<String>>();@238] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<§|Z|§<String>>>, Y<Z<String>>, Z<String>>();@240] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<§|String|§>>>, Y<Z<String>>, Z<String>>();@242] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, §|Y|§<Z<String>>, Z<String>>();@253] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<§|Z|§<String>>, Z<String>>();@255] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<Z<§|String|§>>, Z<String>>();@257] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<Z<String>>, §|Z|§<String>>();@267] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<X<Y<Z<String>>>,Y<Z<String>>,Z<String>> test02.Test.field:<anonymous>#1.bar() [			return new Test<X<Y<Z<String>>>, Y<Z<String>>, Z<§|String|§>>();@269] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<§|A|§, ? extends B, ? super C> field = new Test<A, Z<String>, X<String>> () {@52] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<§|A|§, Z<String>, X<String>> () {@96] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, §|Z|§<String>, X<String>> () {@99] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, Z<§|String|§>, X<String>> () {@101] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, Z<String>, §|X|§<String>> () {@110] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super C> field = new Test<A, Z<String>, X<§|String|§>> () {@112] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, §|B|§, ? extends C> foo(Test<? extends A, ? super B, C> param) {@306] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(Test<? extends A, ? super B, §|C|§> param) {@355] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.C [class C extends X<§|String|§> {}@428] EXACT_MATCH"
	);
}
public void testTypeRefGenericAll02_TypeVariableBound() throws CoreException {
	setUpTypeRefGeneric02();
	search("*", TYPE, TYPE_VARIABLE_BOUND_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults("");
}
public void testTypeRefGenericAll02_WildcardBound() throws CoreException {
	setUpTypeRefGeneric02();
	search("*", TYPE, WILDCARD_BOUND_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends §|B|§, ? super C> field = new Test<A, Z<String>, X<String>> () {@65] EXACT_MATCH\n" + 
		"src/test02/Test.java test02.Test.field [	Test<A, ? extends B, ? super §|C|§> field = new Test<A, Z<String>, X<String>> () {@76] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super §|A|§, B, ? extends C> foo(Test<? extends A, ? super B, C> param) {@303] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends §|C|§> foo(Test<? extends A, ? super B, C> param) {@319] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(Test<? extends §|A|§, ? super B, C> param) {@341] EXACT_MATCH\n" + 
		"src/test02/Test.java Test<? super A,B,? extends C> test02.Test.foo(Test<? extends A,? super B,C>) [	Test<? super A, B, ? extends C> foo(Test<? extends A, ? super §|B|§, C> param) {@352] EXACT_MATCH"
	);
}

/*
 * References to all qualified types (using '*' string pattern)
 */
private void setUpTypeRefGenericMethod() throws JavaModelException {
	final ProblemRequestor problemRequestor = new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
			return problemRequestor;
		}
	};
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearch15/src/test/Test.java",
		"package test;\n" + 
		"public class Test {\n" + 
		"	void foo(A<String> a) {\n" + 
		"		a.<String>foo();\n" + 
		"		a.<A<? extends Exception>>foo();\n" + 
		"	}\n" + 
		"}\n" + 
		"class A<T> {\n" + 
		"	<U> void foo() {}\n" + 
		"}\n"
	);
	assertEquals("CU Should not have any problem!",
		"----------\n" +
		"----------\n",
		problemRequestor.problems.toString()
	);
	this.resultCollector.showSelection = true;
	this.resultCollector.showOffset = true;
}
public void testTypeRefGenericMethod_AllGenericFlags() throws CoreException {
	setUpTypeRefGenericMethod();
	search("*", TYPE, ALL_GENERIC_TYPE_FINE_GRAIN_FLAGS, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test/Test.java void test.Test.foo(A<String>) [	void foo(A<§|String|§> a) {@46] EXACT_MATCH\n" + 
		"src/test/Test.java void test.Test.foo(A<String>) [		a.<§|String|§>foo();@64] EXACT_MATCH\n" + 
		"src/test/Test.java void test.Test.foo(A<String>) [		a.<§|A|§<? extends Exception>>foo();@83] EXACT_MATCH\n" + 
		"src/test/Test.java void test.Test.foo(A<String>) [		a.<A<? extends §|Exception|§>>foo();@95] EXACT_MATCH"
	);
}
public void testTypeRefGenericMethod_Parameterized() throws CoreException {
	setUpTypeRefGenericMethod();
	search("*", TYPE, TYPE_ARGUMENT_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test/Test.java void test.Test.foo(A<String>) [	void foo(A<§|String|§> a) {@46] EXACT_MATCH\n" + 
		"src/test/Test.java void test.Test.foo(A<String>) [		a.<§|String|§>foo();@64] EXACT_MATCH\n" + 
		"src/test/Test.java void test.Test.foo(A<String>) [		a.<§|A|§<? extends Exception>>foo();@83] EXACT_MATCH"
	);
}
public void testTypeRefGenericMethod_TypeVariableBound() throws CoreException {
	setUpTypeRefGenericMethod();
	search("*", TYPE, TYPE_VARIABLE_BOUND_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults("");
}
public void testTypeRefGenericMethod_WildcardBound() throws CoreException {
	setUpTypeRefGenericMethod();
	search("*", TYPE, WILDCARD_BOUND_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/test/Test.java void test.Test.foo(A<String>) [		a.<A<? extends §|Exception|§>>foo();@95] EXACT_MATCH"
	);
}

/*
 * References to all qualified types (using '*' string pattern)
 */
private void setUpTypeRefQualifiedAll() throws JavaModelException {
	final ProblemRequestor problemRequestor = new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
			return problemRequestor;
		}
	};
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearch15/src/type/def/Types.java",
		"package type.def;\n" + 
		"@Bug\n" + 
		"@ATest\n" + 
		"public class Types extends Exception implements ITest1, ITest2 {\n" +
		"	ITest1 test1;\n" +
		"	ITest2 test2;\n" +
		"}\n" + 
		"@Bug\n" + 
		"interface ITest1 {}\n" + 
		"@ATest\n" + 
		"interface ITest2 extends ITest1 {}\n" + 
		"@Bug\n" + 
		"@ATest\n" + 
		"enum ETest {}\n" + 
		"@Bug\n" + 
		"@interface ATest {}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearch15/src/type/def/Bug.java",
		"package type.def;\n" + 
		"public @interface Bug {\n" + 
		"	int num() default 0;\n" + 
		"	String comment() default \"\";\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearch15/src/all/types/ref/TestTypes.java",
		"package all.types.ref;\n" + 
		"\n" + 
		"@type.def.Bug(num=155013)\n" + 
		"public class TestTypes extends type.def.Types {\n" + 
		"	@type.def.Bug(comment=\"field\")\n" + 
		"	type.def.Types field = new type.def.Types(), local, other = new type.def.Types();\n" + 
		"	{\n" + 
		"		Object o;\n" + 
		"		type.def.Types t;\n" + 
		"		if (this.field == null) {\n" + 
		"			try {\n" + 
		"				t = new TestTypes();\n" + 
		"			} catch (RuntimeException e) {\n" + 
		"				t = new type.def.Types();\n" + 
		"			} \n" + 
		"		} else {\n" + 
		"			o = this.field;\n" + 
		"			t = (type.def.Types) o;\n" + 
		"		}\n" + 
		"		local = t;\n" + 
		"	};\n" + 
		"	@type.def.Bug(comment=\"method\", num=155013)\n" + 
		"	type.def.Types method(type.def.Types param) throws type.def.Types {\n" + 
		"		Object obj = new type.def.Types();\n" + 
		"		type.def.Types local = (type.def.Types) obj;\n" + 
		"		return local;\n" + 
		"	}\n" + 
		"	type.def.Bug bar(type.def.Bug bug) {\n" + 
		"		try {\n" + 
		"			method(this);\n" + 
		"		}\n" + 
		"		catch (type.def.Types ex) {}\n" + 
		"		catch (Exception ex) {}\n" + 
		"		return bug; \n" + 
		"	}\n" + 
		"	// Other types references\n" + 
		"	Object object;\n" + 
		"	String str;\n" + 
		"	TestTypes() throws type.def.Types, RuntimeException {\n" + 
		"		if (this.object instanceof String) {\n" + 
		"			this.str = (String) this.object;\n" + 
		"		} else {\n" + 
		"			this.str = new String();\n" + 
		"			this.object = new Object();\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n"
	);
	assertEquals("CU Should not have any problem!",
		"----------\n" +
		"----------\n",
		problemRequestor.problems.toString()
	);
	this.resultCollector.showSelection = true;
	this.resultCollector.showOffset = true;
}
public void testTypeRefQualifiedAll_Allocation() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, ALLOCATION_EXPRESSION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.field [	type.def.Types field = new §|type.def.Types|§(), local, other = new type.def.Types();@158] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.other [	type.def.Types field = new type.def.Types(), local, other = new §|type.def.Types|§();@195] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [				t = new §|TestTypes|§();@297] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [				t = new §|type.def.Types|§();@356] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java type.def.Types all.types.ref.TestTypes.method(type.def.Types) [		Object obj = new §|type.def.Types|§();@591] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [			this.str = new §|String|§();@1032] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [			this.object = new §|Object|§();@1063] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Annotation() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, ANNOTATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes [@§|type.def.Bug|§(num=155013)@25] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.field [	@§|type.def.Bug|§(comment=\"field\")@100] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java type.def.Types all.types.ref.TestTypes.method(type.def.Types) [	@§|type.def.Bug|§(comment=\"method\", num=155013)@460] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types [@§|Bug|§@19] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types [@§|ATest|§@24] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ITest1 [@§|Bug|§@128] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ITest2 [@§|ATest|§@153] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ETest [@§|Bug|§@195] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ETest [@§|ATest|§@200] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ATest [@§|Bug|§@221] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Cast() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, CAST_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [			t = (§|type.def.Types|§) o;@418] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java type.def.Types all.types.ref.TestTypes.method(type.def.Types) [		type.def.Types local = (§|type.def.Types|§) obj;@635] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [			this.str = (§|String|§) this.object;@982] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Catch() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, CATCH_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [			} catch (§|RuntimeException|§ e) {@322] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java type.def.Bug all.types.ref.TestTypes.bar(type.def.Bug) [		catch (§|type.def.Types|§ ex) {}@751] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java type.def.Bug all.types.ref.TestTypes.bar(type.def.Bug) [		catch (§|Exception|§ ex) {}@782] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Field() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, FIELD_DECLARATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.field [	§|type.def.Types|§ field = new type.def.Types(), local, other = new type.def.Types();@131] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.object [	§|Object|§ object;@845] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.str [	§|String|§ str;@861] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types.test1 [	§|ITest1|§ test1;@96] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types.test2 [	§|ITest2|§ test2;@111] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Import() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, IMPORT_DECLARATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults("");
}
public void testTypeRefQualifiedAll_MethodParameter() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, PARAMETER_DECLARATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java type.def.Types all.types.ref.TestTypes.method(type.def.Types) [	type.def.Types method(§|type.def.Types|§ param) throws type.def.Types {@526] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java type.def.Bug all.types.ref.TestTypes.bar(type.def.Bug) [	type.def.Bug bar(§|type.def.Bug|§ bug) {@693] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Return() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, RETURN_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java type.def.Types all.types.ref.TestTypes.method(type.def.Types) [	§|type.def.Types|§ method(type.def.Types param) throws type.def.Types {@504] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java type.def.Bug all.types.ref.TestTypes.bar(type.def.Bug) [	§|type.def.Bug|§ bar(type.def.Bug bug) {@676] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Superinterface() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, SUPERINTERFACE_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/type/def/Types.java type.def.Types [public class Types extends Exception implements §|ITest1|§, ITest2 {@78] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types [public class Types extends Exception implements ITest1, §|ITest2|§ {@86] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.ITest2 [interface ITest2 extends §|ITest1|§ {}@184] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Supertype() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, SUPERTYPE_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes [public class TestTypes extends §|type.def.Types|§ {@81] EXACT_MATCH\n" + 
		"src/type/def/Types.java type.def.Types [public class Types extends §|Exception|§ implements ITest1, ITest2 {@57] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Throws() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, THROWS_CLAUSE_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java type.def.Types all.types.ref.TestTypes.method(type.def.Types) [	type.def.Types method(type.def.Types param) throws §|type.def.Types|§ {@555] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [	TestTypes() throws §|type.def.Types|§, RuntimeException {@893] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes() [	TestTypes() throws type.def.Types, §|RuntimeException|§ {@909] EXACT_MATCH"
	);
}
public void testTypeRefQualifiedAll_Variable() throws CoreException {
	setUpTypeRefQualifiedAll();
	search("*", TYPE, LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [		§|Object|§ o;@218] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java all.types.ref.TestTypes.{} [		§|type.def.Types|§ t;@230] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java type.def.Types all.types.ref.TestTypes.method(type.def.Types) [		§|Object|§ obj = new type.def.Types();@574] EXACT_MATCH\n" + 
		"src/all/types/ref/TestTypes.java type.def.Types all.types.ref.TestTypes.method(type.def.Types) [		§|type.def.Types|§ local = (type.def.Types) obj;@611] EXACT_MATCH"
	);
}

/*
 * Tests using classes defined in JavaSearch15 project
 */
public void testTypeRefGenericAllG1_Parameterized() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, TYPE_ARGUMENT_TYPE_REFERENCE, scope);
	assertSearchResults(
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [	public Generic<§|Object|§>.MemberGeneric<Object> gen_obj;@393] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [	public Generic<Object>.MemberGeneric<§|Object|§> gen_obj;@415] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [	public Generic<§|Exception|§>.MemberGeneric<Exception> gen_exc;@448] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [	public Generic<Exception>.MemberGeneric<§|Exception|§> gen_exc;@473] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [	public g1.t.s.def.Generic<§|Object|§>.MemberGeneric<Object> qgen_obj;@803] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [	public g1.t.s.def.Generic<Object>.MemberGeneric<§|Object|§> qgen_obj;@825] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [	public g1.t.s.def.Generic<§|Exception|§>.MemberGeneric<Exception> qgen_exc;@870] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [	public g1.t.s.def.Generic<Exception>.MemberGeneric<§|Exception|§> qgen_exc;@895] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG1_WildcardBounds() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, WILDCARD_BOUND_TYPE_REFERENCE, scope);
	assertSearchResults(
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [	public Generic<? extends §|Throwable|§>.MemberGeneric<? super RuntimeException> gen_thr;@564] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [	public Generic<? extends Throwable>.MemberGeneric<? super §|RuntimeException|§> gen_thr;@597] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [	public Generic<? super §|RuntimeException|§>.MemberGeneric<? extends Throwable> gen_run;@648] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [	public Generic<? super RuntimeException>.MemberGeneric<? extends §|Throwable|§> gen_run;@690] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [	public g1.t.s.def.Generic<? extends §|Throwable|§>.MemberGeneric<? super RuntimeException> qgen_thr;@1010] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [	public g1.t.s.def.Generic<? extends Throwable>.MemberGeneric<? super §|RuntimeException|§> qgen_thr;@1043] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [	public g1.t.s.def.Generic<? super §|RuntimeException|§>.MemberGeneric<? extends Throwable> qgen_run;@1106] EXACT_MATCH\n" + 
		"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [	public g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric<? extends §|Throwable|§> qgen_run;@1148] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG2_Parameterized() throws CoreException {
	// TODO The JavaSearch15/src/g2/f/s/def/R3.java should be updated as it contains compilation errors!
	IType type = getCompilationUnit("JavaSearch15/src/g2/f/s/def/R3.java").getType("R3");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, TYPE_ARGUMENT_TYPE_REFERENCE, scope);
	assertSearchResults(
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<§|Object|§>.MemberGeneric<Object> member = new Generic<Object>().new MemberGeneric<Object>();@177] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<Object>.MemberGeneric<§|Object|§> member = new Generic<Object>().new MemberGeneric<Object>();@199] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<Object>.MemberGeneric<Object> member = new Generic<§|Object|§>().new MemberGeneric<Object>();@228] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<Object>.MemberGeneric<Object> member = new Generic<Object>().new MemberGeneric<§|Object|§>();@256] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<§|Exception|§>.MemberGeneric<Exception> member = new Generic<Exception>().new MemberGeneric<Exception>();@306] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<Exception>.MemberGeneric<§|Exception|§> member = new Generic<Exception>().new MemberGeneric<Exception>();@331] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<Exception>.MemberGeneric<Exception> member = new Generic<§|Exception|§>().new MemberGeneric<Exception>();@363] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<Exception>.MemberGeneric<Exception> member = new Generic<Exception>().new MemberGeneric<§|Exception|§>();@394] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<§|Exception|§>().new MemberGeneric<Exception>();@633] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<Exception>().new MemberGeneric<§|Exception|§>();@664] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<§|Exception|§>().new MemberGeneric<? extends Throwable>();@794] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<? extends Throwable>().new MemberGeneric<§|Exception|§>();@1006] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<§|Exception|§>().new MemberGeneric<Exception>();@1327] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<Exception>().new MemberGeneric<§|Exception|§>();@1358] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<§|Exception|§>().new MemberGeneric<? super RuntimeException>();@1498] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<? super RuntimeException>().new MemberGeneric<§|Exception|§>();@1730] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG2_WildcardBounds() throws CoreException {
	// TODO The JavaSearch15/src/g2/f/s/def/R3.java should be updated as it contains compilation errors!
	IType type = getCompilationUnit("JavaSearch15/src/g2/f/s/def/R3.java").getType("R3");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, WILDCARD_BOUND_TYPE_REFERENCE, scope);
	assertSearchResults(
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends §|Throwable|§>.MemberGeneric<? extends Throwable> member = new Generic<Exception>().new MemberGeneric<Exception>();@566] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends §|Throwable|§> member = new Generic<Exception>().new MemberGeneric<Exception>();@601] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends §|Throwable|§>.MemberGeneric<? extends Throwable> member = new Generic<Exception>().new MemberGeneric<? extends Throwable>();@727] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends §|Throwable|§> member = new Generic<Exception>().new MemberGeneric<? extends Throwable>();@762] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<Exception>().new MemberGeneric<? extends §|Throwable|§>();@835] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends §|Throwable|§>.MemberGeneric<? extends Throwable> member = new Generic<? extends Throwable>().new MemberGeneric<Exception>();@898] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends §|Throwable|§> member = new Generic<? extends Throwable>().new MemberGeneric<Exception>();@933] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<? extends §|Throwable|§>().new MemberGeneric<Exception>();@975] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends §|Throwable|§>.MemberGeneric<? extends Throwable> member = new Generic<? extends Throwable>().new MemberGeneric<? extends Throwable>();@1069] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends §|Throwable|§> member = new Generic<? extends Throwable>().new MemberGeneric<? extends Throwable>();@1104] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<? extends §|Throwable|§>().new MemberGeneric<? extends Throwable>();@1146] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<? extends Throwable>().new MemberGeneric<? extends §|Throwable|§>();@1187] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super §|RuntimeException|§>.MemberGeneric<? super RuntimeException> member = new Generic<Exception>().new MemberGeneric<Exception>();@1248] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super §|RuntimeException|§> member = new Generic<Exception>().new MemberGeneric<Exception>();@1288] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super §|RuntimeException|§>.MemberGeneric<? super RuntimeException> member = new Generic<Exception>().new MemberGeneric<? super RuntimeException>();@1419] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super §|RuntimeException|§> member = new Generic<Exception>().new MemberGeneric<? super RuntimeException>();@1459] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<Exception>().new MemberGeneric<? super §|RuntimeException|§>();@1537] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super §|RuntimeException|§>.MemberGeneric<? super RuntimeException> member = new Generic<? super RuntimeException>().new MemberGeneric<Exception>();@1605] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super §|RuntimeException|§> member = new Generic<? super RuntimeException>().new MemberGeneric<Exception>();@1645] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<? super §|RuntimeException|§>().new MemberGeneric<Exception>();@1692] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super §|RuntimeException|§>.MemberGeneric<? super RuntimeException> member = new Generic<? super RuntimeException>().new MemberGeneric<? super RuntimeException>();@1791] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super §|RuntimeException|§> member = new Generic<? super RuntimeException>().new MemberGeneric<? super RuntimeException>();@1831] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<? super §|RuntimeException|§>().new MemberGeneric<? super RuntimeException>();@1878] EXACT_MATCH\n" + 
		"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<? super RuntimeException>().new MemberGeneric<? super §|RuntimeException|§>();@1924] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG3R1_Parameterized() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g3/t/ref/R1.java").getType("R1");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, TYPE_ARGUMENT_TYPE_REFERENCE, SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults(
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_wld [	public GS<§|GM|§<?, ?, ?>> sgsm_wld;@321] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [	public GS<§|GM|§<GM<?, ?, ?>,GM<?, ?, ?>,GM<?, ?, ?>>> sgsm_www;@355] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [	public GS<GM<§|GM|§<?, ?, ?>,GM<?, ?, ?>,GM<?, ?, ?>>> sgsm_www;@358] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [	public GS<GM<GM<?, ?, ?>,§|GM|§<?, ?, ?>,GM<?, ?, ?>>> sgsm_www;@370] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [	public GS<GM<GM<?, ?, ?>,GM<?, ?, ?>,§|GM|§<?, ?, ?>>> sgsm_www;@382] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [	public GS<§|GM|§<Object, Exception, RuntimeException>> sgsm_obj;@417] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [	public GS<GM<§|Object|§, Exception, RuntimeException>> sgsm_obj;@420] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [	public GS<GM<Object, §|Exception|§, RuntimeException>> sgsm_obj;@428] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [	public GS<GM<Object, Exception, §|RuntimeException|§>> sgsm_obj;@439] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [	public GM<§|GS|§<?>, GS<?>, GS<?>> sgms_wld;@479] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [	public GM<GS<?>, §|GS|§<?>, GS<?>> sgms_wld;@486] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [	public GM<GS<?>, GS<?>, §|GS|§<?>> sgms_wld;@493] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [	public GM<§|GS|§<?>, GS<GS<?>>, GS<GS<GS<?>>>> sgms_www;@521] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [	public GM<GS<?>, §|GS|§<GS<?>>, GS<GS<GS<?>>>> sgms_www;@528] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [	public GM<GS<?>, GS<§|GS|§<?>>, GS<GS<GS<?>>>> sgms_www;@531] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [	public GM<GS<?>, GS<GS<?>>, §|GS|§<GS<GS<?>>>> sgms_www;@539] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [	public GM<GS<?>, GS<GS<?>>, GS<§|GS|§<GS<?>>>> sgms_www;@542] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [	public GM<GS<?>, GS<GS<?>>, GS<GS<§|GS|§<?>>>> sgms_www;@545] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [	public GM<§|GS|§<Object>, GS<? extends Throwable>, GS<? super RuntimeException>> sgms_obj;@575] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [	public GM<GS<§|Object|§>, GS<? extends Throwable>, GS<? super RuntimeException>> sgms_obj;@578] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [	public GM<GS<Object>, §|GS|§<? extends Throwable>, GS<? super RuntimeException>> sgms_obj;@587] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [	public GM<GS<Object>, GS<? extends Throwable>, §|GS|§<? super RuntimeException>> sgms_obj;@612] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_wld [	public g3.t.def.GS<§|g3.t.def.GM|§<?, ?, ?>> qgsm_wld;@691] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [	public g3.t.def.GS<§|g3.t.def.GM|§<g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>>> qgsm_www;@743] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [	public g3.t.def.GS<g3.t.def.GM<§|g3.t.def.GM|§<?, ?, ?>, g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>>> qgsm_www;@755] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [	public g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>, §|g3.t.def.GM|§<?, ?, ?>, g3.t.def.GM<?, ?, ?>>> qgsm_www;@777] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [	public g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>, §|g3.t.def.GM|§<?, ?, ?>>> qgsm_www;@799] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [	public g3.t.def.GS<§|g3.t.def.GM|§<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>> qgsm_obj;@852] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [	public g3.t.def.GS<g3.t.def.GM<§|java.lang.Object|§, java.lang.Exception, java.lang.RuntimeException>> qgsm_obj;@864] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [	public g3.t.def.GS<g3.t.def.GM<java.lang.Object, §|java.lang.Exception|§, java.lang.RuntimeException>> qgsm_obj;@882] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [	public g3.t.def.GS<g3.t.def.GM<java.lang.Object, java.lang.Exception, §|java.lang.RuntimeException|§>> qgsm_obj;@903] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [	public g3.t.def.GM<§|g3.t.def.GS|§<?>, g3.t.def.GS<?>, g3.t.def.GS<?>> qgms_wld;@962] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [	public g3.t.def.GM<g3.t.def.GS<?>, §|g3.t.def.GS|§<?>, g3.t.def.GS<?>> qgms_wld;@978] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [	public g3.t.def.GM<g3.t.def.GS<?>, g3.t.def.GS<?>, §|g3.t.def.GS|§<?>> qgms_wld;@994] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [	public g3.t.def.GM<§|g3.t.def.GS|§<?>, g3.t.def.GS<g3.t.def.GS<?>>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>>>> qgms_www;@1040] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [	public g3.t.def.GM<g3.t.def.GS<?>, §|g3.t.def.GS|§<g3.t.def.GS<?>>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>>>> qgms_www;@1056] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [	public g3.t.def.GM<g3.t.def.GS<?>, g3.t.def.GS<§|g3.t.def.GS|§<?>>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>>>> qgms_www;@1068] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [	public g3.t.def.GM<g3.t.def.GS<?>, g3.t.def.GS<g3.t.def.GS<?>>, §|g3.t.def.GS|§<g3.t.def.GS<g3.t.def.GS<?>>>> qgms_www;@1085] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [	public g3.t.def.GM<g3.t.def.GS<?>, g3.t.def.GS<g3.t.def.GS<?>>, g3.t.def.GS<§|g3.t.def.GS|§<g3.t.def.GS<?>>>> qgms_www;@1097] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [	public g3.t.def.GM<g3.t.def.GS<?>, g3.t.def.GS<g3.t.def.GS<?>>, g3.t.def.GS<g3.t.def.GS<§|g3.t.def.GS|§<?>>>> qgms_www;@1109] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [	public g3.t.def.GM<§|g3.t.def.GS|§<java.lang.Object>, g3.t.def.GS<? extends java.lang.Throwable>, g3.t.def.GS<? super java.lang.RuntimeException>> qgms_obj;@1157] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [	public g3.t.def.GM<g3.t.def.GS<§|java.lang.Object|§>, g3.t.def.GS<? extends java.lang.Throwable>, g3.t.def.GS<? super java.lang.RuntimeException>> qgms_obj;@1169] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [	public g3.t.def.GM<g3.t.def.GS<java.lang.Object>, §|g3.t.def.GS|§<? extends java.lang.Throwable>, g3.t.def.GS<? super java.lang.RuntimeException>> qgms_obj;@1188] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [	public g3.t.def.GM<g3.t.def.GS<java.lang.Object>, g3.t.def.GS<? extends java.lang.Throwable>, §|g3.t.def.GS|§<? super java.lang.RuntimeException>> qgms_obj;@1232] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG3R1_WildcardBounds() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g3/t/ref/R1.java").getType("R1");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, WILDCARD_BOUND_TYPE_REFERENCE, SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults(
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [	public GM<GS<Object>, GS<? extends §|Throwable|§>, GS<? super RuntimeException>> sgms_obj;@600] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [	public GM<GS<Object>, GS<? extends Throwable>, GS<? super §|RuntimeException|§>> sgms_obj;@623] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [	public g3.t.def.GM<g3.t.def.GS<java.lang.Object>, g3.t.def.GS<? extends §|java.lang.Throwable|§>, g3.t.def.GS<? super java.lang.RuntimeException>> qgms_obj;@1210] EXACT_MATCH\n" + 
		"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [	public g3.t.def.GM<g3.t.def.GS<java.lang.Object>, g3.t.def.GS<? extends java.lang.Throwable>, g3.t.def.GS<? super §|java.lang.RuntimeException|§>> qgms_obj;@1252] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG3R1_TypeVariableBounds() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g3/t/ref/R1.java").getType("R1");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, TYPE_VARIABLE_BOUND_TYPE_REFERENCE, SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults("");
}
public void testTypeRefGenericAllG5_Parameterized() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java").getType("RefMultiple");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, TYPE_ARGUMENT_TYPE_REFERENCE, SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults(
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		Multiple<§|Object|§, Exception, RuntimeException> gm = new Multiple<Object, Exception, RuntimeException>();@115] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		Multiple<Object, §|Exception|§, RuntimeException> gm = new Multiple<Object, Exception, RuntimeException>();@123] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		Multiple<Object, Exception, §|RuntimeException|§> gm = new Multiple<Object, Exception, RuntimeException>();@134] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		Multiple<Object, Exception, RuntimeException> gm = new Multiple<§|Object|§, Exception, RuntimeException>();@170] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		Multiple<Object, Exception, RuntimeException> gm = new Multiple<Object, §|Exception|§, RuntimeException>();@178] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		Multiple<Object, Exception, RuntimeException> gm = new Multiple<Object, Exception, §|RuntimeException|§>();@189] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		gm.<§|Object|§, Exception, RuntimeException>generic(new Object(), new Exception(), new RuntimeException());@367] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		gm.<Object, §|Exception|§, RuntimeException>generic(new Object(), new Exception(), new RuntimeException());@375] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		gm.<Object, Exception, §|RuntimeException|§>generic(new Object(), new Exception(), new RuntimeException());@386] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		gm.paramTypesArgs(new Single<§|Object|§>(), new Single<Exception>(), new Single<RuntimeException>(), gm);@656] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		gm.paramTypesArgs(new Single<Object>(), new Single<§|Exception|§>(), new Single<RuntimeException>(), gm);@678] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		gm.paramTypesArgs(new Single<Object>(), new Single<Exception>(), new Single<§|RuntimeException|§>(), gm);@703] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		gm = gm.<§|Object|§, Exception, RuntimeException>complete(new Object(), new Exception(), new RuntimeException(), gm);@838] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		gm = gm.<Object, §|Exception|§, RuntimeException>complete(new Object(), new Exception(), new RuntimeException(), gm);@846] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [		gm = gm.<Object, Exception, §|RuntimeException|§>complete(new Object(), new Exception(), new RuntimeException(), gm);@857] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testUnbound() [		gm.paramTypesArgs(new Single<§|Object|§>(), new Single<Object>(), new Single<Object>(), gm);@1095] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testUnbound() [		gm.paramTypesArgs(new Single<Object>(), new Single<§|Object|§>(), new Single<Object>(), gm);@1117] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testUnbound() [		gm.paramTypesArgs(new Single<Object>(), new Single<Object>(), new Single<§|Object|§>(), gm);@1139] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		Multiple<§|Object|§, ? extends Throwable, ? extends Exception> gm = new Multiple<Object, Exception, RuntimeException>();@1295] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		Multiple<Object, ? extends Throwable, ? extends Exception> gm = new Multiple<§|Object|§, Exception, RuntimeException>();@1363] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		Multiple<Object, ? extends Throwable, ? extends Exception> gm = new Multiple<Object, §|Exception|§, RuntimeException>();@1371] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		Multiple<Object, ? extends Throwable, ? extends Exception> gm = new Multiple<Object, Exception, §|RuntimeException|§>();@1382] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		gm.<§|Object|§, RuntimeException, RuntimeException>generic(new Object(), new RuntimeException(), new RuntimeException());@1409] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		gm.<Object, §|RuntimeException|§, RuntimeException>generic(new Object(), new RuntimeException(), new RuntimeException());@1417] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		gm.<Object, RuntimeException, §|RuntimeException|§>generic(new Object(), new RuntimeException(), new RuntimeException());@1435] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		gm.paramTypesArgs(new Single<§|Object|§>(), new Single<Throwable>(), new Single<Exception>(), gm);@1554] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		gm.paramTypesArgs(new Single<Object>(), new Single<§|Throwable|§>(), new Single<Exception>(), gm);@1576] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		gm.paramTypesArgs(new Single<Object>(), new Single<Throwable>(), new Single<§|Exception|§>(), gm);@1601] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		gm = gm.<§|Object|§, RuntimeException, RuntimeException>complete(new Object(), new RuntimeException(), new RuntimeException(), gm);@1660] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		gm = gm.<Object, §|RuntimeException|§, RuntimeException>complete(new Object(), new RuntimeException(), new RuntimeException(), gm);@1668] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		gm = gm.<Object, RuntimeException, §|RuntimeException|§>complete(new Object(), new RuntimeException(), new RuntimeException(), gm);@1686] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		Multiple<§|Object|§, ? super RuntimeException, ? super IllegalMonitorStateException> gm = new Multiple<Object, Exception, RuntimeException>();@1893] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		Multiple<Object, ? super RuntimeException, ? super IllegalMonitorStateException> gm = new Multiple<§|Object|§, Exception, RuntimeException>();@1983] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		Multiple<Object, ? super RuntimeException, ? super IllegalMonitorStateException> gm = new Multiple<Object, §|Exception|§, RuntimeException>();@1991] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		Multiple<Object, ? super RuntimeException, ? super IllegalMonitorStateException> gm = new Multiple<Object, Exception, §|RuntimeException|§>();@2002] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		gm.<§|Object|§, RuntimeException, IllegalMonitorStateException>generic(new Object(), new RuntimeException(), new IllegalMonitorStateException());@2029] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		gm.<Object, §|RuntimeException|§, IllegalMonitorStateException>generic(new Object(), new RuntimeException(), new IllegalMonitorStateException());@2037] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		gm.<Object, RuntimeException, §|IllegalMonitorStateException|§>generic(new Object(), new RuntimeException(), new IllegalMonitorStateException());@2055] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		gm.paramTypesArgs(new Single<§|Object|§>(), new Single<RuntimeException>(), new Single<RuntimeException>(), gm);@2198] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		gm.paramTypesArgs(new Single<Object>(), new Single<§|RuntimeException|§>(), new Single<RuntimeException>(), gm);@2220] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		gm.paramTypesArgs(new Single<Object>(), new Single<RuntimeException>(), new Single<§|RuntimeException|§>(), gm);@2252] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		gm = gm.<§|Object|§, RuntimeException, IllegalMonitorStateException>complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm);@2318] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		gm = gm.<Object, §|RuntimeException|§, IllegalMonitorStateException>complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm);@2326] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		gm = gm.<Object, RuntimeException, §|IllegalMonitorStateException|§>complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm);@2344] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG5_WildcardBounds() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java").getType("RefMultiple");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, WILDCARD_BOUND_TYPE_REFERENCE, SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults(
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		Multiple<Object, ? extends §|Throwable|§, ? extends Exception> gm = new Multiple<Object, Exception, RuntimeException>();@1313] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [		Multiple<Object, ? extends Throwable, ? extends §|Exception|§> gm = new Multiple<Object, Exception, RuntimeException>();@1334] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		Multiple<Object, ? super §|RuntimeException|§, ? super IllegalMonitorStateException> gm = new Multiple<Object, Exception, RuntimeException>();@1909] EXACT_MATCH\n" + 
		"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [		Multiple<Object, ? super RuntimeException, ? super §|IllegalMonitorStateException|§> gm = new Multiple<Object, Exception, RuntimeException>();@1935] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG5_TypeVariableBounds() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java").getType("RefMultiple");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, TYPE_VARIABLE_BOUND_TYPE_REFERENCE, SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults("");
}
public void testTypeRefGenericAllG6_Parameterized() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g6/t/ref/QualifMultiple.java").getType("QualifMultiple");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, TYPE_ARGUMENT_TYPE_REFERENCE, SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults(
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [	g6.t.def.Table<§|String|§, Exception>.Entry<String, Exception> entryException;@64] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [	g6.t.def.Table<String, §|Exception|§>.Entry<String, Exception> entryException;@72] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [	g6.t.def.Table<String, Exception>.Entry<§|String|§, Exception> entryException;@89] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [	g6.t.def.Table<String, Exception>.Entry<String, §|Exception|§> entryException;@97] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [	g6.t.def.Table<§|String|§, Exception>.Entry<String, Exception>[] entryExceptionArray;@140] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [	g6.t.def.Table<String, §|Exception|§>.Entry<String, Exception>[] entryExceptionArray;@148] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [	g6.t.def.Table<String, Exception>.Entry<§|String|§, Exception>[] entryExceptionArray;@165] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [	g6.t.def.Table<String, Exception>.Entry<String, §|Exception|§>[] entryExceptionArray;@173] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<§|String|§, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@223] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, §|g6.t.def.Table<String, Exception>.Entry<String, Exception>[]|§>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@231] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<§|String|§, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@246] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<String, §|Exception|§>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@254] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<§|String|§, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@271] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, §|Exception|§>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@279] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<§|String|§, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@299] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, §|g6.t.def.Table<String, Exception>.Entry<String, Exception>[]|§> tableOfEntryExceptionArray;@307] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<§|String|§, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@322] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, §|Exception|§>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@330] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<§|String|§, Exception>[]> tableOfEntryExceptionArray;@347] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, §|Exception|§>[]> tableOfEntryExceptionArray;@355] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG6_WildcardBounds() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g6/t/ref/QualifMultiple.java").getType("QualifMultiple");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, FIELD_DECLARATION_TYPE_REFERENCE, SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults(
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [	§|g6.t.def.Table<String, Exception>.Entry|§<String, Exception> entryException;@49] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [	§|g6.t.def.Table<String, Exception>.Entry<String, Exception>[]|§ entryExceptionArray;@125] EXACT_MATCH\n" + 
		"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [	§|g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry|§<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;@208] EXACT_MATCH"
	);
}
public void testTypeRefGenericAllG6_TypeVariableBounds() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/g6/t/ref/QualifMultiple.java").getType("QualifMultiple");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { type });
	search("*", TYPE, TYPE_VARIABLE_BOUND_TYPE_REFERENCE | WILDCARD_BOUND_TYPE_REFERENCE, SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults("");
}
}
