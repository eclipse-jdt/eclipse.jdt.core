/*******************************************************************************
 * Copyright (c) 2014 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.osgi.framework.Bundle;

import junit.framework.Test;

public class ExternalAnnotations18Test extends ModifyingResourceTests {

	private IJavaProject project;
	private IPackageFragmentRoot root;
	private String ANNOTATION_LIB;

	public ExternalAnnotations18Test(String name) {
		super(name);
	}
	
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_PREFIX = "testClasspathDuplicateExtraAttribute";
//		TESTS_NAMES = new String[] {"test2"};
//		TESTS_NUMBERS = new int[] { 23, 28, 38 };
//		TESTS_RANGE = new int[] { 21, 38 };
	}
	public static Test suite() {
		return buildModelTestSuite(ExternalAnnotations18Test.class, BYTECODE_DECLARATION_ORDER);
	}

	/**
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		
		Bundle[] bundles = org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[2.0.0,3.0.0)");
		File bundleFile = FileLocator.getBundleFile(bundles[0]);
		this.ANNOTATION_LIB = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();

		this.project = setUpJavaProject("ExternalAnnotations18", "1.8"); //$NON-NLS-1$
		addLibraryEntry(this.project, this.ANNOTATION_LIB, false);
		Map options = this.project.getOptions(true);
		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		this.project.setOptions(options);

		IPackageFragmentRoot[] roots = this.project.getAllPackageFragmentRoots();
		int count = 0;
		for (int i = 0, max = roots.length; i < max; i++) {
			final IPackageFragmentRoot packageFragmentRoot = roots[i];
			switch(packageFragmentRoot.getKind()) {
				case IPackageFragmentRoot.K_SOURCE :
					count++;
					if (this.root == null) {
						this.root = packageFragmentRoot;
					}
			}
		}
		assertEquals("Wrong value", 1, count); //$NON-NLS-1$
		assertNotNull("Should not be null", this.root); //$NON-NLS-1$

	}
	
	// TODO: using this copy from AttachedJavadocTests test also programmatically setting the external annotation location:
	private void setExternalAnnotationsAttribute(String folderName) throws JavaModelException {
		IClasspathEntry[] entries = this.project.getRawClasspath();
		IResource resource = this.project.getProject().findMember("/"+folderName+"/"); //$NON-NLS-1$
		assertNotNull("annotations folder cannot be null", resource); //$NON-NLS-1$
		URI locationURI = resource.getLocationURI();
		assertNotNull("annotations folder cannot be null", locationURI); //$NON-NLS-1$
		URL annotationsUrl = null;
		try {
			annotationsUrl = locationURI.toURL();
		} catch (MalformedURLException e) {
			assertTrue("Should not happen", false); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			assertTrue("Should not happen", false); //$NON-NLS-1$
		}
		IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, annotationsUrl.toExternalForm());
		for (int i = 0, max = entries.length; i < max; i++) {
			final IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY
					&& entry.getContentKind() == IPackageFragmentRoot.K_BINARY
					&& "/AttachedJavadocProject/lib/test6.jar".equals(entry.getPath().toString())) { //$NON-NLS-1$
				entries[i] = JavaCore.newLibraryEntry(entry.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), entry.getAccessRules(), new IClasspathAttribute[] { attribute}, entry.isExported());
			}
		}
		this.project.setRawClasspath(entries, null);
	}
	
	/** Perform full build. */
	public void test1() throws CoreException, InterruptedException {
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		assertEquals("Number of markers", 0, markers.length);
	}
	
	/** Reconcile an individual CU. */ // FIXME: needs more capability in SearchableNameEnvironment
	public void test2() throws CoreException, InterruptedException {
		IPackageFragment fragment = this.root.getPackageFragment("test1");
		ICompilationUnit unit = fragment.getCompilationUnit("Test1.java").getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertEquals("Number of problems", 0, problems.length);
	}
}
