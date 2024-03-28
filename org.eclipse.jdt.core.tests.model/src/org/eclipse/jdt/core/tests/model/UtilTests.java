/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.util.Util;

import junit.framework.Test;

public class UtilTests extends AbstractJavaModelTests {

	static {
//		TESTS_PREFIX = "testInvalidCompilerOptions";
//		TESTS_NAMES = new String[] { "test028"};
	}

	public UtilTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(UtilTests.class);
	}
	public void test001() {
		String[] arguments = Util.getProblemArgumentsFromMarker("1:foo");
		assertStringsEqual("Wrong arguments", new String[] {"foo"}, arguments);
	}
	public void test002() {
		String[] arguments = Util.getProblemArgumentsFromMarker("2:foo#bar");
		assertStringsEqual("Wrong arguments", new String[] {"foo", "bar"}, arguments);
	}
	public void test003() {
		String[] arguments = Util.getProblemArgumentsFromMarker("1:   ");
		assertStringsEqual("Wrong arguments", new String[] {""}, arguments);
	}
	public void test004() {
		String[] arguments = Util.getProblemArgumentsFromMarker("0:");
		assertStringsEqual("Wrong arguments", new String[0], arguments);
	}
	public void test005() {
		String[] arguments = Util.getProblemArgumentsFromMarker("3:Task<capture##1-of ?>#getTaskListeners#   ");
		assertStringsEqual("Wrong arguments", new String[] {"Task<capture#1-of ?>", "getTaskListeners", ""}, arguments);
	}
	public void test006() {
		String[] arguments = new String[] {"Task<capture#1-of ?>", "getTaskListeners", ""};
		String[] result = Util.getProblemArgumentsFromMarker(Util.getProblemArgumentsForMarker(arguments));
		assertStringsEqual("Wrong arguments", arguments, result);
	}
	public void test007() {
		assertNull("Not null", Util.getProblemArgumentsFromMarker("tt:Task<capture##1-of ?>#getTaskListeners#   "));
	}
	public void test008() {
		assertNull("Not null", Util.getProblemArgumentsFromMarker("3Task<capture##1-of ?>#getTaskListeners#   "));
	}
	public void test009() {
		assertNull("Not null", Util.getProblemArgumentsFromMarker(null));
	}
	public void test010() {
		assertNull("Not null", Util.getProblemArgumentsFromMarker("0:Task"));
	}
	public void test011() {
		String[] arguments = new String[] {"", "", ""};
		String[] result = Util.getProblemArgumentsFromMarker(Util.getProblemArgumentsForMarker(arguments));
		assertStringsEqual("Wrong arguments", arguments, result);
	}
	public void test012() {
		String[] arguments = new String[] {"foo#test", "bar"};
		String[] result = Util.getProblemArgumentsFromMarker(Util.getProblemArgumentsForMarker(arguments));
		assertStringsEqual("Wrong arguments", arguments, result);
	}

	public void testLogWithStackTrace() {
		startLogListening();
		try {
			Util.log(new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST,
					JavaModelManager.getJavaModelManager().getJavaModel())));
			List<IStatus> logs = this.logListener.getLogs();
			while (logs.isEmpty()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					break;
				}
			}
			assertEquals("More errors as expected", 1, logs.size());
			IStatus status = logs.get(0);
			assertNotNull("No exception on logged status", status.getException());
		} finally {
			stopLogListening();
		}
	}

	public void testTypeSignature() {
		AST ast = AST.newAST(AST.getJLSLatest(), false);
		NameQualifiedType type = ast.newNameQualifiedType(ast.newName("qualifier"), ast.newSimpleName("id"));
		assertEquals("Qqualifier.id;", Util.getSignature(type));
	}
	public void testQualifiedTypeTypeSignature() {
		AST ast = AST.newAST(AST.getJLSLatest(), false);
		SimpleType parentType = ast.newSimpleType(ast.newName("ParentType"));
		QualifiedType qualifiedType = ast.newQualifiedType(parentType, ast.newSimpleName("ChildType"));
		assertEquals("QParentType.ChildType;", Util.getSignature(qualifiedType));
	}
	public void testIntersectionTypeSignature() {
		AST ast = AST.newAST(AST.getJLSLatest(), false);
		IntersectionType type = ast.newIntersectionType();
		type.types().add(ast.newSimpleType(ast.newSimpleName("A")));
		type.types().add(ast.newSimpleType(ast.newSimpleName("B")));
		assertEquals("|QA;:QB;", Util.getSignature(type));
	}
	public void testUnionTypeSignature() {
		AST ast = AST.newAST(AST.getJLSLatest(), false);
		UnionType type = ast.newUnionType();
		type.types().add(ast.newSimpleType(ast.newSimpleName("A")));
		type.types().add(ast.newSimpleType(ast.newSimpleName("B")));
		assertEquals("&QA;:QB;", Util.getSignature(type));
	}
	public void testGetSafeName() {
		assertGetSafeNamePass("simple");
		assertGetSafeNamePass("dir1/dir2/normal");
		assertGetSafeNamePass("dir1/../unnormal");
		assertGetSafeNamePass("dir1/dir2/.../unnormalTripple");

		assertGetSafeNameFail("../slipped");
		assertGetSafeNameFail("dir1/../../slipped");

		// https://github.com/eclipse-jdt/eclipse.jdt.core/pull/2015#issuecomment-2009162226
		assertGetSafeNamePass("overrides/..ROOT...override");
	}

	private void assertGetSafeNamePass(String entryName) {
		String zipfileName = "any";
		assertEquals(entryName, Util.getEntryName(zipfileName, new java.util.zip.ZipEntry(entryName)));
	}

	private void assertGetSafeNameFail(String entryName) {
		String zipfileName = "any";
		try {
			String n = Util.getEntryName(zipfileName, new java.util.zip.ZipEntry(entryName));
			assertFalse("Expected IllegalArgumentException but got " + n, true);
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}
}