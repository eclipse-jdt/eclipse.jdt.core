/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.ParameterizedSourceType;
import org.eclipse.jdt.internal.core.SourceType;


/**
 * Test generic type search.
 */
public class AbstractJavaSearchGenericTypeTests extends JavaSearchTests {
	
	String expectedResult;
	
	public AbstractJavaSearchGenericTypeTests(String name) {
		super(name);
		this.tabs = 3;
		this.displayName = true;
	}
	static {
//		MatchLocator.PRINT_BUFFER = false;
	}
	
	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector.showAccuracy = true;
		this.expectedResult = null;
	}

	/* (non-Javadoc)
	 * Overridden to remove all last type arguments from expected string.
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#assertSearchResults(java.lang.String, java.lang.String, java.lang.Object)
	 */
	protected void assertSearchResults() {
		assertSearchResults(this.expectedResult, this.resultCollector);
	}

	/* (non-Javadoc)
	 * Overridden to remove all last type arguments from expected string.
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#assertSearchResults(java.lang.String, java.lang.String, java.lang.Object)
	 */
	protected void assertSearchResults(String message, String expected, Object collector) {
		String actual = collector.toString();
//		String trimmed = expected;
//		boolean createMethod = false;
//		try {
//			getClass().getDeclaredMethod(getName(), new Class[0]);
//		}
//		catch (NoSuchMethodException nsme) {
//			createMethod = true;
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
		if (expected == null || expected.equals("print")) {
			StringBuffer buffer = new StringBuffer("\n	public void ");
			buffer.append(getName());
			buffer.append("() throws CoreException {\n");
			buffer.append("		this.expectedResult = \n");
			if (expected == null) {
				buffer.append("			\"???\";\n");
			} else {
				buffer.append(displayString(actual, this.tabs));
				buffer.append(";\n");
			}
			buffer.append("		super."+getName()+"();\n");
			buffer.append("	}");
			System.out.println(buffer.toString());
			throw new RuntimeException("Not implemented yet!");
		} else if (expected.equals("???")) {
			throw new RuntimeException("Not implemented yet!");
		} else if (!expected.equals(actual)) {
			System.out.println(getName()+" expected result is:");
			System.out.print(displayString(actual, this.tabs));
			System.out.println(",");
		}
		assertEquals(
			message,
			expected,
			actual
		);
	}

	/*
	 * Search several occurences of a selection in a compilation unit source and returns its start and length.
	 * If occurence is negative, then perform a backward search from the end of file.
	 * If selection starts or ends with a comment (to help identification in source), it is removed from returned selection info.
	 */
	private int[] selectionInfo(ICompilationUnit cu, String selection, int occurences) throws JavaModelException {
		String source = cu.getSource();
		int index = occurences < 0 ? source.lastIndexOf(selection) : source.indexOf(selection);
		int max = Math.abs(occurences)-1;
		for (int n=0; index >= 0 && n<max; n++) {
			index = occurences < 0 ? source.lastIndexOf(selection, index) : source.indexOf(selection, index);
		}
		StringBuffer msg = new StringBuffer("Selection '");
		msg.append(selection);
		if (index >= 0) {
			if (selection.startsWith("/**")) { // comment is before
				int start = source.indexOf("*/", index);
				if (start >=0) {
					return new int[] { start+2, selection.length()-(start+2-index) };
				} else {
					msg.append("' starts with an unterminated comment");
				}
			} else if (selection.endsWith("*/")) { // comment is after
				int end = source.lastIndexOf("/**", index+selection.length());
				if (end >=0) {
					return new int[] { index, index-end };
				} else {
					msg.append("' ends with an unstartted comment");
				}
			} else { // no comment => use whole selection
				return new int[] { index, selection.length() };
			}
		} else {
			msg.append("' was not found in ");
		}
		msg.append(cu.getElementName());
		msg.append(":\n");
		msg.append(source);
		assertTrue(msg.toString(), false);
		return null;
	}

	/**
	 * Select a parameterized source type in a compilation unit identified with the first occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @return ParameterizedSourceType
	 * @throws JavaModelException
	 */
	protected ParameterizedSourceType selectParameterizedSourceType(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectParameterizedSourceType(unit, selection, 1);
	}
	
	/**
	 * Select a parameterized source type in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @param occurences
	 * @return
	 * @throws JavaModelException
	 */
	protected ParameterizedSourceType selectParameterizedSourceType(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		SourceType sourceType = selectSourceType(unit, selection, occurences);
		assertTrue("Not a parameterized source type: "+sourceType.getElementName(), sourceType instanceof ParameterizedSourceType);
		return (ParameterizedSourceType) sourceType;
	}

	/**
	 * Select a source type in a compilation unit identified with the first occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @return
	 * @throws JavaModelException
	 */
	protected SourceType selectSourceType(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectSourceType(unit, selection, 1);
	}

	/**
	 * Select a parameterized source type in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @param occurences
	 * @return
	 * @throws JavaModelException
	 */
	protected SourceType selectSourceType(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		IJavaElement element = selectJavaElement(unit, selection, 1);
		assertTrue("Not a source type: "+element.getElementName(), element instanceof SourceType);
		return (SourceType) element;
	}

	/**
	 * Select a java element in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @param occurences
	 * @return
	 * @throws JavaModelException
	 */
	protected IJavaElement selectJavaElement(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		int[] selectionPositions = selectionInfo(unit, selection, occurences);
		IJavaElement[] elements = unit.codeSelect(selectionPositions[0], selectionPositions[1]);
		assertEquals("Invalid selection number", 1, elements.length);
		return elements[0];
	}
	
	/**
	 * Type reference for 1.5.
	 * Bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=73336)
	 */
	public void testTypeReferenceBug73336() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/bug73336/A.java").getType("A");
		search(type, REFERENCES, getJavaSearchScope15("bug73336", false), this.resultCollector);
		assertSearchResults();
	}
	public void testTypeReferenceBug73336b() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/bug73336b/A.java").getType("A");
		search(type, REFERENCES, getJavaSearchScope15("bug73336b", false),  resultCollector);
		assertSearchResults();
	}
	// Verify that no NPE was raised on following case
	public void testTypeReferenceBug73336c() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/bug73336c/A.java").getType("A");
		search(type, REFERENCES, getJavaSearchScope15("bug73336c", false), resultCollector);
		assertSearchResults();
	}

	/**
	 * Bug 75641: [1.5][Search] Types search does not work with generics
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=75641)
	 */
	/*
	 * Following functionalities are tested:
	 * 	A) Search using an IJavaElement
	 * 		a) single parameter generic types
	 * 		b) multiple parameters generic types
	 * 	B) Search using a not parameterized string pattern
	 * 		a) simple name
	 * 		b) any string characters
	 * 	C) Search using a single parameterized string pattern
	 * 		a) no wildcard
	 * 		b) wildcard extends
	 * 		c) wildcard super
	 * 		d) wildcard unbound
	 * 	D) Search using a multiple parameterized string pattern
	 * 		a) no wildcard
	 * 		b) wildcard extends
	 * 		c) wildcard super
	 * 		d) wildcard unbound
	 */
	// Search reference to a generic type
	public void testElementPatternSingleParam01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type declared in a generic type
	public void testElementPatternSingleParam02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type declared in a generic type
	public void testElementPatternSingleParam03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("MemberGeneric");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testElementPatternSingleParam04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	// Search reference to a generic type
	public void testElementPatternMultipleParam01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type declared in a generic type
	public void testElementPatternMultipleParam02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type declared in a generic type
	public void testElementPatternMultipleParam03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("MemberGeneric");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testElementPatternMultipleParam04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	// Search reference with nested parameterized types
	public void testElementPatternSingleNestedParam01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("GS");
		ICompilationUnit ref = getCompilationUnit("JavaSearch15/src/g3/t/ref/R1.java");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new ICompilationUnit[] {ref});
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testElementPatternSingleNestedParam02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("GS").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testElementPatternSingleNestedParam03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("GS").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testElementPatternSingleNestedParam04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("NGS").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testElementPatternMultipleNestedParam01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("GM");
		ICompilationUnit ref = getCompilationUnit("JavaSearch15/src/g3/t/ref/R1.java");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new ICompilationUnit[] {ref});
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testElementPatternMultipleNestedParam02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("GM").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testElementPatternMultipleNestedParam03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("GM").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testElementPatternMultipleNestedParam04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("NGM").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	// Search reference to a generic type
	public void testStringPatternSimpleName01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", false /* only this package */);
		search("Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternSimpleName02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.m.ref", false /* only this package */);
		search("Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type declared in a generic type
	public void testStringPatternSimpleName03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type declared in a generic type
	public void testStringPatternSimpleName04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testStringPatternSimpleName05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSinglePatternSimpleName06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSinglePatternSimpleName07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSinglePatternSimpleName08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("NonGeneric.GenericMember", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic type
	public void testStringPatternAnyStrings01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", false /* only this package */);
		search("*Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternAnyStrings02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.m.ref", false /* only this package */);
		search("G?ner?c", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type declared in a generic type
	public void testStringPatternAnyStrings03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", false /* only this package */);
		search("*Member*", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternAnyStrings04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.m.ref", false /* only this package */);
		search("Member*", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternAnyStrings05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", true);
		search("Generic*<Object>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternAnyStrings06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", true);
		search("Generic<Obj*>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	public void testSingleParameterizedElementPattern01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java");
		ParameterizedSourceType type = selectParameterizedSourceType(unit, "Generic<Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleParameterizedElementPattern02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java");
		ParameterizedSourceType type = selectParameterizedSourceType(unit, "g1.t.s.def.Generic<Exception>.Member"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleParameterizedElementPattern03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java");
		ParameterizedSourceType type = selectParameterizedSourceType(unit, "NonGeneric.GenericMember<Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleParameterizedElementPattern04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java");
		ParameterizedSourceType type = selectParameterizedSourceType(unit,  "g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	// Search reference to a generic type
	public void testMultipleParameterizedElementPattern01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java");
		ParameterizedSourceType type = selectParameterizedSourceType(unit, "g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type
	public void testMultipleParameterizedElementPattern02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java");
		ParameterizedSourceType type = selectParameterizedSourceType(unit, "Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type
	public void testMultipleParameterizedElementPattern03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java");
		ParameterizedSourceType type = selectParameterizedSourceType(unit, "NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testMultipleParameterizedElementPattern04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java");
		ParameterizedSourceType type = selectParameterizedSourceType(unit,  "g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	// Search reference to a generic type
	public void testSingleParameterizedStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type
	public void testSingleParameterizedStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type
	public void testSingleParameterizedStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleParameterizedStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleParameterizedStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	public void testSingleWildcardExtendsStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type
	public void testSingleWildcardExtendsStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type
	public void testSingleWildcardExtendsStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleWildcardExtendsStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleWildcardExtendsStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic type
	public void testSingleWildcardSuperStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type
	public void testSingleWildcardSuperStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type
	public void testSingleWildcardSuperStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleWildcardSuperStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleWildcardSuperStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic type
	public void testSingleWildcardUnboundStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type
	public void testSingleWildcardUnboundStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type
	public void testSingleWildcardUnboundStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleWildcardUnboundStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testSingleWildcardUnboundStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>.MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	// Search reference to a generic type
	public void testMultipleParameterizedStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type
	public void testMultipleParameterizedStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type
	public void testMultipleParameterizedStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testMultipleParameterizedStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testMultipleParameterizedStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic type
	public void testMultipleWildcardExtendsStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type
	public void testMultipleWildcardExtendsStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type
	public void testMultipleWildcardExtendsStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testMultipleWildcardExtendsStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testMultipleWildcardExtendsStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic type
	public void testMultipleWildcardSuperStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type
	public void testMultipleWildcardSuperStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type
	public void testMultipleWildcardSuperStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testMultipleWildcardSuperStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testMultipleWildcardSuperStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic type
	public void testMultipleWildcardUnboundStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ? >", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a member type
	public void testMultipleWildcardUnboundStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	// Search reference to a generic member type
	public void testMultipleWildcardUnboundStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testMultipleWildcardUnboundStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testMultipleWildcardUnboundStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	// Search reference with nested parameterized types
	public void testStringPatternSingleNestedParam01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GS<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternSingleNestedParam02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GS<? extends Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternSingleNestedParam03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GS<?>.Generic<? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternSingleNestedParam04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("NGS.Generic<? extends Throwable>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternMultipleNestedParam01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GM<Object, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternMultipleNestedParam02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GM<java.lang.Object, ? extends java.lang.Exception, ? super java.lang.RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternMultipleNestedParam03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GM<Object, Exception, RuntimeException>.Generic<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternMultipleNestedParam04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("g3.t.def.GM<?, ?, ?>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testStringPatternMultipleNestedParam05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("NGM.Generic<? extends java.lang.Object, ? extends java.lang.Object, ? extends java.lang.Object>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	// Tests generic search on single param arrays
	public void testArraySingle01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Single.java");
		SourceType type = selectSourceType(unit,  "List", 2 /* 2nd occurence*/); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testArraySingle02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Single.java");
		SourceType type = selectSourceType(unit,  "List<Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testArraySingle03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/QualifSingle.java");
		SourceType type = selectSourceType(unit,  "g6.t.def.List<Exception>", 2 /* 2nd occurence*/); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testArraySingle04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/QualifSingle.java");
		SourceType type = selectSourceType(unit,  "g6.t.def.List<g6.t.def.List<Exception>[]>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}

	// Tests generic search on multiple param arrays
	public void testArrayMultiple01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Multiple.java");
		SourceType type = selectSourceType(unit,  "Table.Entry"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testArrayMultiple02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/QualifMultiple.java");
		SourceType type = selectSourceType(unit,  "Table<String, Exception>.Entry<String, Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testArrayMultiple03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Multiple.java");
		SourceType type = selectSourceType(unit,  "Table<String, Exception>.Entry<String, Exception>", 2); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
	public void testArrayMultiple04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Multiple.java");
		SourceType type = selectSourceType(unit,  "Table<String, Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, Table<String, Exception>.Entry<String, Exception>[]>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults();
	}
}
