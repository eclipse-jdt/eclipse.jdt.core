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
import junit.framework.Test;
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
				"interface Cloneable {}\n" +
				"\n" +
				"abstract class AbstractSet<S> {}\n" +
				"\n" +
				"class TreeSet<E> extends AbstractSet<E>\n" +
				"    implements Cloneable, java.io.Serializable\n" +
				"{}\n" +
				"\n" +
				"class HashSet<E>\n" +
				"    extends AbstractSet<E>\n" +
				"    implements Cloneable, java.io.Serializable\n" +
				"{}\n" +
				"\n" +
				"public class Hey {\n" +
				"    public static void main(String[] args) {\n" +
				"        var x = args.length > 0 ? new TreeSet<>() : new HashSet<>();\n" +
				"        x.add(1);\n" +
				"    }\n" +
				"}\n");

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
				"LAbstractSet<Ljava.lang.Object;>;\n" +
				"LCloneable;\n" +
				"Ljava.io.Serializable;\n",
				Signature.getUnionTypeBounds(typeSignature) // method name is wrong, it actually means: getIntersectionTypeBounds
			);
	}
	public void testBug562382() throws CoreException {
		this.wc = getWorkingCopy("/Resolve/src/X.java",
				"class X {\n" +
				"	class Number {};\n" +
				"	class Integer extends Number {};\n" +
				"	interface Function<T, R> {\n" +
				"	    R apply(T t);\n" +
				"	}\n" +
				"	Function<Number, Integer> fail() {\n" +
				"		return new Function<>() {\n" +
				"			@Override\n" +
				"			public Integer apply(Number t) {\n" +
				"				return null;\n" +
				"			}\n" +
				"		};\n" +
				"	}\n" +
				"}"
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
