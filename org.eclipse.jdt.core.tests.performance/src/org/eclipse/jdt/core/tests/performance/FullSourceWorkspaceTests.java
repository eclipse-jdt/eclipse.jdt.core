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
package org.eclipse.jdt.core.tests.performance;

import java.io.*;
import java.net.URL;
import java.util.*;

import junit.framework.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.builder.TestingEnvironment;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.test.performance.Dimension;



public abstract class FullSourceWorkspaceTests extends TestCase {

	protected static TestingEnvironment env = null;
	final static Hashtable INITIAL_OPTIONS = JavaCore.getOptions();
	static int TESTS_COUNT = 0;
	final static boolean DEBUG = "true".equals(System.getProperty("debug"));
	static IJavaProject[] ALL_PROJECTS;
	

	/**
	 * @param name
	 */
	public FullSourceWorkspaceTests(String name) {
		super(name);
	}

//	static {
//		TESTS_NAMES = new String[] { "testPerfFullBuildNoDocCommentSupport" };
//	}
	public static Test suite() {
		return buildSuite(FullSourceWorkspaceTests.class);
	}

	protected static Test buildSuite(Class testClass) {
		TestSuite suite = new TestSuite(testClass.getName());
		List tests = buildTestsList(testClass);
		for (int i=0, size= tests.size(); i<size; i++) {
			suite.addTest((Test)tests.get(i));
		}
		TESTS_COUNT += suite.testCount();
		return suite;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		if (env == null) {
			env = new TestingEnvironment();
			env.openEmptyWorkspace();
			setUpFullSourceWorkspace();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.test.performance.PerformanceTestCase#tagAsGlobalSummary(java.lang.String, org.eclipse.test.performance.Dimension)
	 */
	public void tagAsGlobalSummary(String shortName, Dimension dimension) {
		if (DEBUG) System.out.println(shortName);
		super.tagAsGlobalSummary(shortName, dimension);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.test.performance.PerformanceTestCase#tagAsGlobalSummary(java.lang.String, org.eclipse.test.performance.Dimension[])
	 */
	public void tagAsGlobalSummary(String shortName, Dimension[] dimensions) {
		if (DEBUG) System.out.println(shortName);
		super.tagAsGlobalSummary(shortName, dimensions);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.test.performance.PerformanceTestCase#tagAsSummary(java.lang.String, org.eclipse.test.performance.Dimension)
	 */
	public void tagAsSummary(String shortName, Dimension dimension) {
		if (DEBUG) System.out.println(shortName);
		super.tagAsSummary(shortName, dimension);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.test.performance.PerformanceTestCase#tagAsSummary(java.lang.String, org.eclipse.test.performance.Dimension[])
	 */
	public void tagAsSummary(String shortName, Dimension[] dimensions) {
		if (DEBUG) System.out.println(shortName);
		super.tagAsSummary(shortName, dimensions);
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		TESTS_COUNT--;
		if (TESTS_COUNT == 0) {
			env.resetWorkspace();
			JavaCore.setOptions(INITIAL_OPTIONS);
		}
	}

	/*
	 * Returns the OS path to the directory that contains this plugin.
	 */
	private static String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.core.tests.performance").getEntry("/");
			return new File(Platform.asLocalURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Set up full source workpsace from zip file.
	 */
	private static void setUpFullSourceWorkspace() throws IOException, CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		if (workspaceRoot.getProjects().length == 0) {
			String fullSourceZipPath = getPluginDirectoryPath() + File.separator + "full-source-R3_0.zip";
			final String targetWorkspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();

			if (DEBUG) System.out.print("Unzipping "+fullSourceZipPath+"...");
			Util.unzip(fullSourceZipPath, targetWorkspacePath);
		
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					File targetWorkspaceDir = new File(targetWorkspacePath);
					String[] projectNames = targetWorkspaceDir.list();
					for (int i = 0, length = projectNames.length; i < length; i++) {
						String projectName = projectNames[i];
						if (".metadata".equals(projectName)) continue;
						IProject project = workspaceRoot.getProject(projectName);
						project.create(monitor);
						project.open(monitor);
					}
				}
			}, null);
			if (DEBUG) System.out.println("done!");
		}
		String jdkLib = Util.getJavaClassLibs()[0];
		JavaCore.setClasspathVariable("JRE_LIB", new Path(jdkLib), null);
		
		// workaround bug 73253 Project references not set on project open 
		if (DEBUG) System.out.print("Set projects classpaths...");
		ALL_PROJECTS = JavaCore.create(workspaceRoot).getJavaProjects();
		int length = ALL_PROJECTS.length;
		for (int i = 0; i < length; i++) {
			ALL_PROJECTS[i].setRawClasspath(ALL_PROJECTS[i].getRawClasspath(), null);
		}
		if (DEBUG) System.out.println("done!");
	}

	/**
	 * Returns project correspoding to given name or null if none is found.
	 * @param projectName
	 * @return IJavaProject
	 */
	protected IJavaProject getProject(String projectName) {
		for (int i=0, length = ALL_PROJECTS.length; i<length; i++) {
			if (ALL_PROJECTS[i].getElementName().equals(projectName))
				return ALL_PROJECTS[i];
		}
		return null;
	}

	/**
	 * Returns compilation unit with given name in given project and package.
	 * @param projectName
	 * @param packageName
	 * @param unitName
	 * @return org.eclipse.jdt.core.ICompilationUnit
	 */
	protected ICompilationUnit getCompilationUnit(String projectName, String packageName, String unitName) throws JavaModelException {
		IJavaProject javaProject = getProject(projectName);
		if (javaProject == null) return null;
		IPackageFragmentRoot[] fragmentRoots = javaProject.getPackageFragmentRoots();
		int length = fragmentRoots.length;
		for (int i=0; i<length; i++) {
			if (fragmentRoots[i] instanceof JarPackageFragmentRoot) continue;
			IJavaElement[] packages= fragmentRoots[i].getChildren();
			for (int k= 0; k < packages.length; k++) {
				IPackageFragment pack = (IPackageFragment) packages[k];
				if (pack.getElementName().equals(packageName)) {
					ICompilationUnit[] units = pack.getCompilationUnits();
					for (int u=0; u<units.length; u++) {
						if (units[u].getElementName().equals(unitName))
							return units[u];
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns all compilation units of a given project.
	 * @param javaProject Project to collect units
	 * @return List of org.eclipse.jdt.core.ICompilationUnit
	 */
	protected List getProjectCompilationUnits(IJavaProject javaProject) throws JavaModelException {
		IPackageFragmentRoot[] fragmentRoots = javaProject.getPackageFragmentRoots();
		int length = fragmentRoots.length;
		List allUnits = new ArrayList();
		for (int i=0; i<length; i++) {
			if (fragmentRoots[i] instanceof JarPackageFragmentRoot) continue;
			IJavaElement[] packages= fragmentRoots[i].getChildren();
			for (int k= 0; k < packages.length; k++) {
				IPackageFragment pack = (IPackageFragment) packages[k];
				ICompilationUnit[] units = pack.getCompilationUnits();
				for (int u=0; u<units.length; u++) {
					allUnits.add(units[u]);
				}
			}
		}
		return allUnits;
	}

	/**
	 * Split a list of compilation units in several arrays.
	 * @param units List of org.eclipse.jdt.core.ICompilationUnit
	 * @param splitSize Size of the arrays
	 * @return List of ICompilationUnit[]
	 */
	protected List splitListInSmallArrays(List units, int splitSize) throws JavaModelException {
		int size = units.size();
		if (size == 0) return Collections.EMPTY_LIST;
		int length = size / splitSize;
		int remind = size%splitSize;
		List splitted = new ArrayList(remind==0?length:length+1);
		if (length == 0) {
			ICompilationUnit[] sublist = new ICompilationUnit[size];
			units.toArray(sublist);
			splitted.add(sublist);
			return splitted;
		}
		int ptr = 0;
		for (int i= 0; i<length; i++){
			ICompilationUnit[] sublist = new ICompilationUnit[splitSize];
			units.subList(ptr, ptr+splitSize).toArray(sublist);
			splitted.add(sublist);
			ptr += splitSize;
		}
		if (remind > 0) {
			if (remind< 10) {
				ICompilationUnit[] lastList = (ICompilationUnit[]) splitted.remove(length-1);
				System.arraycopy(lastList, 0, lastList = new ICompilationUnit[splitSize+remind], 0, splitSize);
				for (int i=ptr, j=splitSize; i<size; i++, j++) {
					lastList[j] = (ICompilationUnit) units.get(i);
				}
				splitted.add(lastList);
			} else {
				ICompilationUnit[] sublist = new ICompilationUnit[remind];
				units.subList(ptr, size).toArray(sublist);
				splitted.add(sublist);
			}
		}
		return splitted;
	}

	/**
	 * Start a build on workspace using given options.
	 * @param options
	 * @throws IOException
	 * @throws CoreException
	 */
	protected void startBuild(Hashtable options) throws IOException, CoreException {
		if (DEBUG) System.out.print("\tstart build...");
		JavaCore.setOptions(options);
		startMeasuring();
		env.fullBuild();
		stopMeasuring();
		IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		for (int i = 0, length = markers.length; i < length; i++) {
			IMarker marker = markers[i];
			assertTrue(
				"Unexpected marker: " + marker.getAttribute(IMarker.MESSAGE), 
				IMarker.SEVERITY_ERROR != ((Integer) marker.getAttribute(IMarker.SEVERITY)).intValue());
		}
		if (DEBUG) System.out.println("done");
		commitMeasurements();
		assertPerformance();
	}
}
