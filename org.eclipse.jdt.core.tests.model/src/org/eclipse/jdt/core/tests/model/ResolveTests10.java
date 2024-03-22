/*******************************************************************************
 * Copyright (c) 2016, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.LocalVariable;

import junit.framework.Test;

public class ResolveTests10 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

	static {
//		 TESTS_NAMES = new String[] { "testModuleInfo_" };
//		 TESTS_NUMBERS = new int[] { 124 };
//		 TESTS_RANGE = new int[] { 16, -1 };
	}
	public static Test suite() {
		return buildModelTestSuite(ResolveTests10.class);
	}
	public ResolveTests10(String name) {
		super(name);
	}
	@Override
	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		return super.getWorkingCopy(path, source, this.wcOwner);
	}
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		IJavaProject project = setUpJavaProject("Resolve", "10", true);

		String bootModPath = System.getProperty("java.home") + File.separator +"jrt-fs.jar";
		IClasspathEntry jrtEntry = JavaCore.newLibraryEntry(new Path(bootModPath), null, null, null, null, false);
		IClasspathEntry[] old = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[old.length +1];
		System.arraycopy(old, 0, newPath, 0, old.length);
		newPath[old.length] = jrtEntry;
		project.setRawClasspath(newPath, null);

		waitUntilIndexesReady();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.wcOwner = new WorkingCopyOwner(){};
	}
	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("Resolve");

		super.tearDownSuite();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.wc != null) {
			this.wc.discardWorkingCopy();
		}
		super.tearDown();
	}
	public void testVarWithIntersectionType() throws CoreException {
		this.wc = getWorkingCopy(
				"/Resolve/src/Hey.java",
				"""
					interface Cloneable {}
					
					abstract class AbstractSet<S> {}
					
					class TreeSet<E> extends AbstractSet<E>
					    implements Cloneable, java.io.Serializable
					{}
					
					class HashSet<E>
					    extends AbstractSet<E>
					    implements Cloneable, java.io.Serializable
					{}
					
					public class Hey {
					    public static void main(String[] args) {
					        var x = args.length > 0 ? new TreeSet<>() : new HashSet<>();
					        x.add(1);
					    }
					}
					""");

		String str = this.wc.getSource();
		String selection = "x";
		int start = str.lastIndexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.wc.codeSelect(start, length);
		assertElementsEqual(
			"Unexpected elements",
			"x [in main(String[]) [in Hey [in [Working copy] Hey.java [in <default> [in src [in Resolve]]]]]]",
			elements
		);

		String typeSignature = ((LocalVariable)elements[0]).getTypeSignature();
		assertEquals("type signature", "&LAbstractSet<Ljava.lang.Object;>;:LCloneable;:Ljava.io.Serializable;", typeSignature);

		assertStringsEqual(
				"Unexpected intersection type bounds",
				"""
					LAbstractSet<Ljava.lang.Object;>;
					LCloneable;
					Ljava.io.Serializable;
					""",
				Signature.getUnionTypeBounds(typeSignature) // method name is wrong, it actually means: getIntersectionTypeBounds
			);
	}
	public void testBug562382() throws CoreException {
		this.wc = getWorkingCopy("/Resolve/src/X.java",
				"""
					class X {
						class Number {};
						class Integer extends Number {};
						interface Function<T, R> {
						    R apply(T t);
						}
						Function<Number, Integer> fail() {
							return new Function<>() {
								@Override
								public Integer apply(Number t) {
									return null;
								}
							};
						}
					}"""
				);
		String str = this.wc.getSource();
		String selection = "Number";
		int start = str.lastIndexOf(selection);
		int length = selection.length();

		IJavaElement[] selected = this.wc.codeSelect(start, length);
		assertEquals(1, selected.length);
		assertEquals("Number", selected[0].getElementName());
	}
}
