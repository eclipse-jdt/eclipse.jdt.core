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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class AttachedJavadocTests extends ModifyingResourceTests {
	static {
//		TESTS_NAMES = new String[] { "test010" };
//		TESTS_NUMBERS = new int[] { 18 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildModelTestSuite(AttachedJavadocTests.class);
	}

	private IJavaProject project;
	private IPackageFragmentRoot root;

	public AttachedJavadocTests(String name) {
		super(name);
	}

	/**
	 * Create project and set the jar placeholder.
	 */
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		this.project = setUpJavaProject("AttachedJavadocProject"); //$NON-NLS-1$
		Map options = this.project.getOptions(true);
		options.put(JavaCore.TIMEOUT_FOR_PARAMETER_NAME_FROM_ATTACHED_JAVADOC, "2000"); //$NON-NLS-1$
		this.project.setOptions(options);
		IClasspathEntry[] entries = this.project.getRawClasspath();
		IResource resource = this.project.getProject().findMember("/doc/"); //$NON-NLS-1$
		assertNotNull("doc folder cannot be null", resource); //$NON-NLS-1$
		URI locationURI = resource.getLocationURI();
		assertNotNull("doc folder cannot be null", locationURI); //$NON-NLS-1$
		URL docUrl = null;
		try {
			docUrl = locationURI.toURL();
		} catch (MalformedURLException e) {
			assertTrue("Should not happen", false); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			assertTrue("Should not happen", false); //$NON-NLS-1$
		}
		IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, docUrl.toExternalForm());
		for (int i = 0, max = entries.length; i < max; i++) {
			final IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY
					&& entry.getContentKind() == IPackageFragmentRoot.K_BINARY
					&& "/AttachedJavadocProject/lib/test6.jar".equals(entry.getPath().toString())) { //$NON-NLS-1$
				entries[i] = JavaCore.newLibraryEntry(entry.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), entry.getAccessRules(), new IClasspathAttribute[] { attribute}, entry.isExported());
			}
		}
		project.setRawClasspath(entries, null);

		IPackageFragmentRoot[] roots = this.project.getAllPackageFragmentRoots();
		int count = 0;
		for (int i = 0, max = roots.length; i < max; i++) {
			final IPackageFragmentRoot packageFragmentRoot = roots[i];
			switch(packageFragmentRoot.getKind()) {
				case IPackageFragmentRoot.K_BINARY :
					if (!packageFragmentRoot.isExternal()) {
						count++;
						if (root == null) {
							root = packageFragmentRoot;
						}
					}
			}
		}
		assertEquals("Wrong value", 1, count); //$NON-NLS-1$
		assertNotNull("Should not be null", root); //$NON-NLS-1$
	}

	/**
	 * Reset the jar placeholder and delete project.
	 */
	public void tearDownSuite() throws Exception {
		this.deleteProject("AttachedJavadocProject"); //$NON-NLS-1$
		this.root = null;
		this.project = null;
		super.tearDownSuite();
	}

	// test javadoc for a package fragment
	public void test001() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		String javadoc = packageFragment.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
	}

	// for a class file
	public void test002() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("X.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		String javadoc = classFile.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
	}

	// for a field
	public void test003() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("X.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		IType type = classFile.getType();
		IField field = type.getField("f"); //$NON-NLS-1$
		assertNotNull(field);
		String javadoc = field.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
	}

	// for a method
	public void test004() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("X.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		IType type = classFile.getType();
		IMethod method = type.getMethod("foo", new String[] {"I", "J", "Ljava.lang.String;"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertTrue(method.exists());
		String javadoc = method.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
		String[] paramNames = method.getParameterNames();
		assertNotNull(paramNames);
		assertEquals("Wrong size", 3, paramNames.length); //$NON-NLS-1$
		assertEquals("Wrong name for first param", "i", paramNames[0]); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Wrong name for second param", "l", paramNames[1]); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Wrong name for third param", "s", paramNames[2]); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// for a constructor
	public void test005() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("X.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		IType type = classFile.getType();
		IMethod method = type.getMethod("X", new String[] {"I"}); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(method.exists());
		String javadoc = method.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
		String[] paramNames = method.getParameterNames();
		assertNotNull(paramNames);
		assertEquals("Wrong size", 1, paramNames.length); //$NON-NLS-1$
		assertEquals("Wrong name for first param", "i", paramNames[0]);		 //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// for a member type
	public void test006() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("X$A.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		String javadoc = classFile.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
	}
	
	// for a constructor
	public void test007() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("X$A.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		IType type = classFile.getType();
		IMethod method = type.getMethod("A", new String[] {"Lp1.p2.X;", "F"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(method.exists());
		String javadoc = method.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
		String[] paramNames = method.getParameterNames();
		assertNotNull(paramNames);
		assertEquals("Wrong size", 1, paramNames.length); //$NON-NLS-1$
		assertEquals("Wrong name for first param", "f", paramNames[0]); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// for a method foo2
	public void test008() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("X.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		IType type = classFile.getType();
		IMethod method = type.getMethod("foo2", new String[0]); //$NON-NLS-1$
		assertTrue(method.exists());
		String javadoc = method.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
		String[] paramNames = method.getParameterNames();
		assertNotNull(paramNames);
		assertEquals("Wrong size", 0, paramNames.length); //$NON-NLS-1$
	}
	
	// for a field f2
	public void test009() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("X.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		IType type = classFile.getType();
		IField field = type.getField("f2"); //$NON-NLS-1$
		assertNotNull(field);
		String javadoc = field.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
	}
	
	// test archive doc
	public void test010() throws JavaModelException {
		IClasspathEntry[] savedEntries = null;
		try {
			IClasspathEntry[] entries = this.project.getRawClasspath();
			savedEntries = (IClasspathEntry[]) entries.clone();
			IResource resource = this.project.getProject().findMember("/doc.zip"); //$NON-NLS-1$
			assertNotNull("doc folder cannot be null", resource); //$NON-NLS-1$
			URI locationURI = resource.getLocationURI();
			assertNotNull("doc folder cannot be null", locationURI); //$NON-NLS-1$
			URL docUrl = null;
			try {
				docUrl = locationURI.toURL();
			} catch (MalformedURLException e) {
				assertTrue("Should not happen", false); //$NON-NLS-1$
			} catch(IllegalArgumentException e) {
				assertTrue("Should not happen", false); //$NON-NLS-1$
			}
			final String path = "jar:" + docUrl.toExternalForm() + "!/doc"; //$NON-NLS-1$ //$NON-NLS-2$
			//final String path = "jar:" + "platform:/resource/AttachedJavadocProject/doc.zip" + "!/doc";
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, path);
			for (int i = 0, max = entries.length; i < max; i++) {
				final IClasspathEntry entry = entries[i];
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY
						&& entry.getContentKind() == IPackageFragmentRoot.K_BINARY
						&& "/AttachedJavadocProject/lib/test6.jar".equals(entry.getPath().toString())) { //$NON-NLS-1$
					entries[i] = JavaCore.newLibraryEntry(entry.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), entry.getAccessRules(), new IClasspathAttribute[] { attribute }, entry.isExported());
				}
			}
			this.project.setRawClasspath(entries, null);
			IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
			assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
			IClassFile classFile = packageFragment.getClassFile("X.class"); //$NON-NLS-1$
			assertNotNull(classFile);
			IType type = classFile.getType();
			IField field = type.getField("f"); //$NON-NLS-1$
			assertNotNull(field);
			String javadoc = field.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
			assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
		} finally {
			// restore classpath
			if (savedEntries != null) {
				this.project.setRawClasspath(savedEntries, null);
			}
		}
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120597
	public void test011() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("Z.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		IType type = classFile.getType();
		IField field = type.getField("out"); //$NON-NLS-1$
		assertNotNull(field);
		String javadoc = field.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120637
	public void test012() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("Z.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		String javadoc = classFile.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
		assertTrue("Should not contain reference to out", javadoc.indexOf("out") == -1);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120559
	public void test013() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("W.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		String javadoc = classFile.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNull("Should not have a javadoc", javadoc); //$NON-NLS-1$
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120637
	public void test014() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("E.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		String javadoc = classFile.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
		assertTrue("Should not contain reference to Constant C", javadoc.indexOf("Constant C") == -1);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120637
	public void test015() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("Annot.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		String javadoc = classFile.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
		assertTrue("Should not contain reference to name", javadoc.indexOf("name") == -1);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120847
	public void test016() throws JavaModelException {
		IClasspathEntry[] savedEntries = null;
		try {
			IClasspathEntry[] entries = this.project.getRawClasspath();
			savedEntries = (IClasspathEntry[]) entries.clone();
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, "invalid_path");
			for (int i = 0, max = entries.length; i < max; i++) {
				final IClasspathEntry entry = entries[i];
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY
						&& entry.getContentKind() == IPackageFragmentRoot.K_BINARY
						&& "/AttachedJavadocProject/lib/test6.jar".equals(entry.getPath().toString())) { //$NON-NLS-1$
					entries[i] = JavaCore.newLibraryEntry(entry.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), entry.getAccessRules(), new IClasspathAttribute[] { attribute }, entry.isExported());
				}
			}
			this.project.setRawClasspath(entries, null);
			IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
			assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
			IClassFile classFile = packageFragment.getClassFile("X.class"); //$NON-NLS-1$
			assertNotNull(classFile);
			IType type = classFile.getType();
			IField field = type.getField("f"); //$NON-NLS-1$
			assertNotNull(field);
			field.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
			assertFalse("Should be unreachable", true);
		} catch(JavaModelException e) {
			assertTrue("Must occur", true);
			assertEquals("Wrong error message", "Cannot retrieve the attached javadoc for invalid_path", e.getMessage());
		} finally {
			// restore classpath
			if (savedEntries != null) {
				this.project.setRawClasspath(savedEntries, null);
			}
		}
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120875
	public void test017() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("Annot2.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		String javadoc = classFile.getAttachedJavadoc(new NullProgressMonitor()); //$NON-NLS-1$
		assertNotNull("Should have a javadoc", javadoc); //$NON-NLS-1$
		assertTrue("Should not contain reference to name2", javadoc.indexOf("name2") == -1);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=138167
	public void _test018() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("p1/p2"); //$NON-NLS-1$
		assertNotNull("Should not be null", packageFragment); //$NON-NLS-1$
		IClassFile classFile = packageFragment.getClassFile("C.class"); //$NON-NLS-1$
		assertNotNull(classFile);
		IType type = classFile.getType();
		IMethod[] methods = type.getMethods();
		NullProgressMonitor monitor = new NullProgressMonitor();
		for (int i = 0, max = methods.length; i < max; i++) {
			IMethod method = methods[i];
			String javadoc = method.getAttachedJavadoc(monitor);
			assertNotNull("Should have a javadoc", javadoc);
			assertTrue("Wrong doc", javadoc.indexOf(method.getElementName()) != -1);
		}
	}
}
