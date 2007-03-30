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

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.Util;

public abstract class AbstractJavaModelTests extends SuiteOfTestCases {
	
	/**
	 * The java.io.File path to the directory that contains the external jars.
	 */
	protected static String EXTERNAL_JAR_DIR_PATH;
	
	// static variables for subsets tests
	public static String[] testsNames = null; // list of test names to perform
	public static int[] testsNumbers = null; // list of test numbers to perform
	public static int[] testsRange = null; // range of test numbers to perform

	/**
	 * Delta listener
	 */
	protected class DeltaListener implements IElementChangedListener {
		/**
		 * Deltas received from the java model. See 
		 * <code>#startDeltas</code> and
		 * <code>#stopDeltas</code>.
		 */
		public IJavaElementDelta[] deltas;
	
		public void elementChanged(ElementChangedEvent ev) {
			IJavaElementDelta[] copy= new IJavaElementDelta[deltas.length + 1];
			System.arraycopy(deltas, 0, copy, 0, deltas.length);
			copy[deltas.length]= ev.getDelta();
			deltas= copy;
		}
	}
	protected DeltaListener deltaListener = new DeltaListener();
	 
	
	public AbstractJavaModelTests(String name) {
		super(name);
	}

	public static Test buildTestSuite(Class evaluationTestClass) {
		return buildTestSuite(evaluationTestClass, "test", null); //$NON-NLS-1$
	}

	public static Test buildTestSuite(Class evaluationTestClass, String suiteName) {
		return buildTestSuite(evaluationTestClass, "test", suiteName); //$NON-NLS-1$
	}

	public static Test buildTestSuite(Class evaluationTestClass, String testPrefix, String suiteName) {
		// Init suite with class name
		TestSuite suite = new Suite(suiteName==null?evaluationTestClass.getName():suiteName);
		List tests = new ArrayList();
		Constructor constructor = null;
		try {
			// Get class constructor
			Class[] paramTypes = new Class[] { String.class };
			constructor = evaluationTestClass.getConstructor(paramTypes);
		}
		catch (Exception e) {
			// cannot get constructor, skip suite
			return suite;
		}

		// Get all tests from "test%" methods
		Method[] methods = evaluationTestClass.getDeclaredMethods();
		for (int m = 0, max = methods.length; m < max; m++) {
			try {
				if (methods[m].getModifiers() == 1 /* public */ &&
					methods[m].getName().startsWith("test")) {
					String methName = methods[m].getName();
					Object[] params = {methName};
					int numStart = testPrefix.length();
					// tests names subset
					if (testsNames != null) {
						for (int i = 0, imax= testsNames.length; i<imax; i++) {
							if (testsNames[i].equals(methName) || testsNames[i].equals(methName.substring(numStart))) {
								tests.add(methName);
								suite.addTest((Test)constructor.newInstance(params));
								break;
							}
						}
					}
					// look for test number
					if (methName.startsWith(testPrefix) && Character.isDigit(methName.charAt(numStart))) {
						try {
							// get test number
							int n = numStart;
							while (methName.charAt(n) == '0') n++;
							int num = Integer.parseInt(methName.substring(n));
							// tests numbers subset
							if (testsNumbers != null && !tests.contains(methName)) {
								for (int i = 0; i < testsNumbers.length; i++) {
									if (testsNumbers[i] == num) {
										tests.add(methName);
										suite.addTest((Test)constructor.newInstance(params));
										break;
									}
								}
							}
							// tests range subset
							if (testsRange != null && testsRange.length == 2 && !tests.contains(methName)) {
								if ((testsRange[0]==-1 || num>=testsRange[0]) && (testsRange[1]==-1 || num<=testsRange[1])) {
									tests.add(methName);
									suite.addTest((Test)constructor.newInstance(params));
								}
							}
						} catch (NumberFormatException e) {
							System.out.println("Method "+methods[m]+" has an invalid number format: "+e.getMessage());
						}
					}
					// no subset, add all tests
					if (testsNames==null && testsNumbers==null &&testsRange==null) {
						suite.addTest((Test)constructor.newInstance(params));
					}
				}
			}
			catch (Exception e) {
				System.out.println("Method "+methods[m]+" removed from suite due to exception: "+e.getMessage());
			}
		}
		return suite;
	}
	protected void addJavaNature(String projectName) throws CoreException {
		IProject project = getWorkspaceRoot().getProject(projectName);
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] {JavaCore.NATURE_ID});
		project.setDescription(description, null);
	}
	protected void assertSearchResults(String expected, Object collector) {
		assertSearchResults("Unexpected search results", expected, collector);
	}
	protected void assertSearchResults(String message, String expected, Object collector) {
		String actual = collector.toString();
		if (!expected.equals(actual)) {
			System.out.print(displayString(actual, 2));
			System.out.println(",");
		}
		assertEquals(
			message,
			expected,
			actual
		);
	}
	protected void assertSortedElementsEqual(String message, String expected, IJavaElement[] elements) {
		this.sortElements(elements);
		assertElementsEqual(message, expected, elements);
	}
	
	
	protected void assertResourcesEqual(String message, String expected, Object[] resources) {
		this.sortResources(resources);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = resources.length; i < length; i++){
			IResource resource = (IResource)resources[i];
			buffer.append(resource == null ? "<null>" : resource.getName());
			if (i != length-1)buffer.append("\n");
		}
		if (!expected.equals(buffer.toString())) {
			System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(buffer.toString(), 2));
			System.out.println(",");
		}
		assertEquals(
			message,
			expected,
			buffer.toString()
		);
	}
	protected void assertElementsEqual(String message, String expected, IJavaElement[] elements) {
		StringBuffer buffer = new StringBuffer();
		if (elements != null) {
			for (int i = 0, length = elements.length; i < length; i++){
				JavaElement element = (JavaElement)elements[i];
				if (element == null) {
					buffer.append("<null>");
				} else {
					buffer.append(element.toStringWithAncestors());
				}
				if (i != length-1) buffer.append("\n");
			}
		} else {
			buffer.append("<null>");
		}
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2) + ",");
		}
		assertEquals(message, expected, actual);
	}
	protected void assertHierarchyEquals(String expected, ITypeHierarchy hierarchy) {
		String actual = hierarchy.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2) + ",");
		}
		assertEquals("Unexpected type hierarchy", expected, actual);
	}
	/*
	 * Asserts that the given actual source (usually coming from a file content) is equal to the expected one.
	 * Note that 'expected' is assumed to have the '\n' line separator. 
	 * The line separators in 'actual' are converted to '\n' before the comparison.
	 */
	protected void assertSourceEquals(String message, String expected, String actual) {
		if (actual == null) {
			assertEquals(message, expected, null);
			return;
		}
		actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(actual);
		if (!actual.equals(expected)) {
			System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(actual.toString(), 2));
			System.out.println(",");
		}
		assertEquals(message, expected, actual);
	}
	/**
	 * Ensures the elements are present after creation.
	 */
	public void assertCreation(IJavaElement[] newElements) {
		for (int i = 0; i < newElements.length; i++) {
			IJavaElement newElement = newElements[i];
			assertTrue("Element should be present after creation", newElement.exists());
		}
	}
	/**
	 * Ensures the element is present after creation.
	 */
	public void assertCreation(IJavaElement newElement) {
		assertCreation(new IJavaElement[] {newElement});
	}
	/**
	 * Creates an operation to delete the given elements, asserts
	 * the operation is successful, and ensures the elements are no
	 * longer present in the model.
	 */
	public void assertDeletion(IJavaElement[] elementsToDelete) throws JavaModelException {
		IJavaElement elementToDelete = null;
		for (int i = 0; i < elementsToDelete.length; i++) {
			elementToDelete = elementsToDelete[i];
			assertTrue("Element must be present to be deleted", elementToDelete.exists());
		}
	
		getJavaModel().delete(elementsToDelete, false, null);
		
		for (int i = 0; i < elementsToDelete.length; i++) {
			elementToDelete = elementsToDelete[i];
			assertTrue("Element should not be present after deletion: " + elementToDelete, !elementToDelete.exists());
		}
	}
	protected void assertTypesEqual(String message, String expected, IType[] types) {
		assertTypesEqual(message, expected, types, true);
	}
	protected void assertTypesEqual(String message, String expected, IType[] types, boolean sort) {
		if (sort) this.sortTypes(types);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < types.length; i++){
			buffer.append(types[i].getFullyQualifiedName());
			buffer.append("\n");
		}
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2) + ",");
		}
		assertEquals(message, expected, actual);
	}
	/**
	 * Attaches a source zip to the given jar package fragment root.
	 */
	protected void attachSource(IPackageFragmentRoot root, String sourcePath, String sourceRoot) throws JavaModelException {
		IJavaProject javaProject = root.getJavaProject();
		IClasspathEntry[] entries = (IClasspathEntry[])javaProject.getRawClasspath().clone();
		for (int i = 0; i < entries.length; i++){
			IClasspathEntry entry = entries[i];
			if (entry.getPath().toOSString().toLowerCase().equals(root.getPath().toOSString().toLowerCase())) {
				entries[i] = JavaCore.newLibraryEntry(
					root.getPath(),
					sourcePath == null ? null : new Path(sourcePath),
					sourceRoot == null ? null : new Path(sourceRoot),
					false);
				break;
			}
		}
		javaProject.setRawClasspath(entries, null);
	}
	/**
	 * Creates an operation to delete the given element, asserts
	 * the operation is successfull, and ensures the element is no
	 * longer present in the model.
	 */
	public void assertDeletion(IJavaElement elementToDelete) throws JavaModelException {
		assertDeletion(new IJavaElement[] {elementToDelete});
	}
	/**
	 * Empties the current deltas.
	 */
	public void clearDeltas() {
		this.deltaListener.deltas = new IJavaElementDelta[0];
	}
	protected IJavaElement[] codeSelect(ISourceReference sourceReference, String selectAt, String selection) throws JavaModelException {
		String str = sourceReference.getSource();
		int start = str.indexOf(selectAt);
		int length = selection.length();
		return ((ICodeAssist)sourceReference).codeSelect(start, length);
	}
	protected IJavaElement[] codeSelectAt(ISourceReference sourceReference, String selectAt) throws JavaModelException {
		String str = sourceReference.getSource();
		int start = str.indexOf(selectAt) + selectAt.length();
		int length = 0;
		return ((ICodeAssist)sourceReference).codeSelect(start, length);
	}
	/**
	 * Copy file from src (path to the original file) to dest (path to the destination file).
	 */
	public void copy(File src, File dest) throws IOException {
		// read source bytes
		byte[] srcBytes = this.read(src);
	
		// write bytes to dest
		FileOutputStream out = new FileOutputStream(dest);
		out.write(srcBytes);
		out.close();
	}
	
	/**
	 * Copy the given source directory (and all its contents) to the given target directory.
	 */
	protected void copyDirectory(File source, File target) throws IOException {
		if (!target.exists()) {
			target.mkdirs();
		}
		File[] files = source.listFiles();
		if (files == null) return;
		for (int i = 0; i < files.length; i++) {
			File sourceChild = files[i];
			String name =  sourceChild.getName();
			if (name.equals("CVS")) continue;
			File targetChild = new File(target, name);
			if (sourceChild.isDirectory()) {
				copyDirectory(sourceChild, targetChild);
			} else {
				copy(sourceChild, targetChild);
			}
		}
	}
	
	/*
	 * Creates a Java project where prj=src=bin and with JCL_LIB on its classpath.
	 */
	protected IJavaProject createJavaProject(String projectName) throws CoreException {
		return this.createJavaProject(projectName, new String[] {""}, new String[] {"JCL_LIB"}, "");
	}
	/*
	 * Creates a Java project with the given source folders an output location. 
	 * Add those on the project's classpath.
	 */
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String output) throws CoreException {
		return 
			this.createJavaProject(
				projectName, 
				sourceFolders, 
				null/*no lib*/, 
				null/*no project*/, 
				null/*no exported project*/, 
				output, 
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/
			);
	}
	/*
	 * Creates a Java project with the given source folders an output location. 
	 * Add those on the project's classpath.
	 */
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String output, String[] sourceOutputs) throws CoreException {
		return 
			this.createJavaProject(
				projectName, 
				sourceFolders, 
				null/*no lib*/, 
				null/*no project*/, 
				null/*no exported project*/, 
				output, 
				sourceOutputs,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/
			);
	}
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String[] libraries, String output) throws CoreException {
		return 
			this.createJavaProject(
				projectName, 
				sourceFolders, 
				libraries, 
				null/*no project*/, 
				null/*no exported project*/, 
				output, 
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/
			);
	}
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String[] libraries, String[] projects, String projectOutput) throws CoreException {
		return
			this.createJavaProject(
				projectName,
				sourceFolders,
				libraries,
				projects,
				null/*no exported project*/, 
				projectOutput,
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/
			);
	}
	protected SearchPattern createPattern(IJavaElement element, int limitTo) {
		return SearchPattern.createPattern(element, limitTo);
	}
	protected SearchPattern createPattern(String stringPattern, int searchFor, int limitTo, boolean isCaseSensitive) {
		int matchMode = stringPattern.indexOf('*') != -1 || stringPattern.indexOf('?') != -1
			? SearchPattern.R_PATTERN_MATCH
			: SearchPattern.R_EXACT_MATCH;
		int matchRule = isCaseSensitive ? matchMode | SearchPattern.R_CASE_SENSITIVE : matchMode;
		return SearchPattern.createPattern(stringPattern, searchFor, limitTo, matchRule);
	}
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String[] libraries, String[] projects, boolean[] exportedProject, String projectOutput) throws CoreException {
		return
			this.createJavaProject(
				projectName,
				sourceFolders,
				libraries,
				projects,
				exportedProject, 
				projectOutput,
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/
			);
	}
	protected IJavaProject createJavaProject(final String projectName, final String[] sourceFolders, final String[] libraries, final String[] projects, final boolean[] exportedProjects, final String projectOutput, final String[] sourceOutputs, final String[][] inclusionPatterns, final String[][] exclusionPatterns) throws CoreException {
		final IJavaProject[] result = new IJavaProject[1];
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// create project
				createProject(projectName);
				
				// set java nature
				addJavaNature(projectName);
				
				// create classpath entries 
				IProject project = getWorkspaceRoot().getProject(projectName);
				IPath projectPath = project.getFullPath();
				int sourceLength = sourceFolders == null ? 0 : sourceFolders.length;
				int libLength = libraries == null ? 0 : libraries.length;
				int projectLength = projects == null ? 0 : projects.length;
				IClasspathEntry[] entries = new IClasspathEntry[sourceLength+libLength+projectLength];
				for (int i= 0; i < sourceLength; i++) {
					IPath sourcePath = new Path(sourceFolders[i]);
					int segmentCount = sourcePath.segmentCount();
					if (segmentCount > 0) {
						// create folder and its parents
						IContainer container = project;
						for (int j = 0; j < segmentCount; j++) {
							IFolder folder = container.getFolder(new Path(sourcePath.segment(j)));
							if (!folder.exists()) {
								folder.create(true, true, null);
							}
							container = folder;
						}
					}
					IPath outputPath = null;
					if (sourceOutputs != null) {
						// create out folder for source entry
						outputPath = sourceOutputs[i] == null ? null : new Path(sourceOutputs[i]);
						if (outputPath != null && outputPath.segmentCount() > 0) {
							IFolder output = project.getFolder(outputPath);
							if (!output.exists()) {
								output.create(true, true, null);
							}
						}
					}
				// inclusion patterns
				IPath[] inclusionPaths;
				if (inclusionPatterns == null) {
					inclusionPaths = new IPath[0];
				} else {
					String[] patterns = inclusionPatterns[i];
					int length = patterns.length;
					inclusionPaths = new IPath[length];
					for (int j = 0; j < length; j++) {
						String inclusionPattern = patterns[j];
						inclusionPaths[j] = new Path(inclusionPattern);
					}
				}
					// exclusion patterns
					IPath[] exclusionPaths;
					if (exclusionPatterns == null) {
						exclusionPaths = new IPath[0];
					} else {
						String[] patterns = exclusionPatterns[i];
						int length = patterns.length;
						exclusionPaths = new IPath[length];
						for (int j = 0; j < length; j++) {
							String exclusionPattern = patterns[j];
							exclusionPaths[j] = new Path(exclusionPattern);
						}
					}
					// create source entry
					entries[i] = 
						JavaCore.newSourceEntry(
							projectPath.append(sourcePath), 
							inclusionPaths,
							exclusionPaths, 
							outputPath == null ? null : projectPath.append(outputPath)
						);
				}
				for (int i= 0; i < libLength; i++) {
					String lib = libraries[i];
					if (lib.startsWith("JCL_LIB")) {
						// ensure JCL variables are set
						if (JavaCore.getClasspathVariable("JCL_LIB") == null) {
							JavaCore.setClasspathVariables(
								new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
								new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
								null);
						}
					}
					if (lib.equals(lib.toUpperCase())) { // all upper case is a var 
						char[][] vars = CharOperation.splitOn(',', lib.toCharArray());
						entries[sourceLength+i] = JavaCore.newVariableEntry(
							new Path(new String(vars[0])), 
							vars.length > 1 ? new Path(new String(vars[1])) : null, 
							vars.length > 2 ? new Path(new String(vars[2])) : null);
					} else if (lib.startsWith("org.eclipse.jdt.core.tests.model.")) { // container
						entries[sourceLength+i] = JavaCore.newContainerEntry(new Path(lib));
					} else {
						IPath libPath = new Path(lib);
						if (!libPath.isAbsolute() && libPath.segmentCount() > 0 && libPath.getFileExtension() == null) {
							project.getFolder(libPath).create(true, true, null);
							libPath = projectPath.append(libPath);
						}
						entries[sourceLength+i] = JavaCore.newLibraryEntry(libPath, null, null);
					}
				}
				for  (int i= 0; i < projectLength; i++) {
					boolean isExported = exportedProjects != null && exportedProjects.length > i && exportedProjects[i];
					entries[sourceLength+libLength+i] = JavaCore.newProjectEntry(new Path(projects[i]), isExported);
				}
				
				// create project's output folder
				IPath outputPath = new Path(projectOutput);
				if (outputPath.segmentCount() > 0) {
					IFolder output = project.getFolder(outputPath);
					if (!output.exists()) {
						output.create(true, true, null);
					}
				}
				
				// set classpath and output location
				IJavaProject javaProject = JavaCore.create(project);
				javaProject.setRawClasspath(entries, projectPath.append(outputPath), null);
				result[0] = javaProject;
			}
		};
		getWorkspace().run(create, null);	
		return result[0];
	}
	/*
	 * Create simple project.
	 */
	protected IProject createProject(final String projectName) throws CoreException {
		final IProject project = getProject(projectName);
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(create, null);	
		return project;
	}
	public void deleteFile(File file) {
		int retryCount = 0;
		while (++retryCount <= 60) { // wait 1 minute at most
			if (org.eclipse.jdt.core.tests.util.Util.delete(file)) {
				break;
			}
		}
	}
	protected void deleteProject(String projectName) throws CoreException {
		IProject project = this.getProject(projectName);
		if (project.exists() && !project.isOpen()) { // force opening so that project can be deleted without logging (see bug 23629)
			project.open(null);
		}
		deleteResource(project);
	}
	
	/**
	 * Batch deletion of projects
	 */
	protected void deleteProjects(final String[] projectNames) throws CoreException {
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (projectNames != null){
					for (int i = 0, max = projectNames.length; i < max; i++){
						if (projectNames[i] != null)
							deleteProject(projectNames[i]);
					}
				}
			}
		},
		null);
	}
	/**
	 * Delete this resource.
	 */
	public void deleteResource(IResource resource) throws CoreException {
		CoreException lastException = null;
		try {
			resource.delete(true, null);
		} catch (CoreException e) {
			lastException = e;
		} catch (IllegalArgumentException iae) {
			// just print for info
			System.out.println(iae.getMessage());
		}
		int retryCount = 60; // wait 1 minute at most
		while (resource.isAccessible() && --retryCount >= 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			try {
				resource.delete(true, null);
			} catch (CoreException e) {
				lastException = e;
			} catch (IllegalArgumentException iae) {
				// just print for info
				System.out.println("Retry "+retryCount+": "+iae.getMessage());
			}
		}
		if (!resource.isAccessible()) return;
		System.err.println("Failed to delete " + resource.getFullPath());
		if (lastException != null) {
			throw lastException;
		}
	}
	/**
	 * Returns true if this delta is flagged as having changed children.
	 */
	protected boolean deltaChildrenChanged(IJavaElementDelta delta) {
		return delta.getKind() == IJavaElementDelta.CHANGED &&
			(delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0;
	}
	/**
	 * Returns true if this delta is flagged as having had a content change
	 */
	protected boolean deltaContentChanged(IJavaElementDelta delta) {
		return delta.getKind() == IJavaElementDelta.CHANGED &&
			(delta.getFlags() & IJavaElementDelta.F_CONTENT) != 0;
	}
	/**
	 * Returns true if this delta is flagged as having moved from a location
	 */
	protected boolean deltaMovedFrom(IJavaElementDelta delta) {
		return delta.getKind() == IJavaElementDelta.ADDED &&
			(delta.getFlags() & IJavaElementDelta.F_MOVED_FROM) != 0;
	}
	/**
	 * Returns true if this delta is flagged as having moved to a location
	 */
	protected boolean deltaMovedTo(IJavaElementDelta delta) {
		return delta.getKind() == IJavaElementDelta.REMOVED &&
			(delta.getFlags() & IJavaElementDelta.F_MOVED_TO) != 0;
	}
	/**
	 * Ensure that the positioned element is in the correct position within the parent.
	 */
	public void ensureCorrectPositioning(IParent container, IJavaElement sibling, IJavaElement positioned) throws JavaModelException {
		IJavaElement[] children = container.getChildren();
		if (sibling != null) {
			// find the sibling
			boolean found = false;
			for (int i = 0; i < children.length; i++) {
				if (children[i].equals(sibling)) {
					assertTrue("element should be before sibling", i > 0 && children[i - 1].equals(positioned));
					found = true;
					break;
				}
			}
			assertTrue("Did not find sibling", found);
		}
	}
	/**
	 * Returns the specified compilation unit in the given project, root, and
	 * package fragment or <code>null</code> if it does not exist.
	 */
	public IClassFile getClassFile(String projectName, String rootPath, String packageName, String className) throws JavaModelException {
		IPackageFragment pkg= getPackageFragment(projectName, rootPath, packageName);
		if (pkg == null) {
			return null;
		}
		return pkg.getClassFile(className);
	}
	protected ICompilationUnit getCompilationUnit(String path) {
		return (ICompilationUnit)JavaCore.create(getFile(path));
	}
	/**
	 * Returns the specified compilation unit in the given project, root, and
	 * package fragment or <code>null</code> if it does not exist.
	 */
	public ICompilationUnit getCompilationUnit(String projectName, String rootPath, String packageName, String cuName) throws JavaModelException {
		IPackageFragment pkg= getPackageFragment(projectName, rootPath, packageName);
		if (pkg == null) {
			return null;
		}
		return pkg.getCompilationUnit(cuName);
	}
	/**
	 * Returns the specified compilation unit in the given project, root, and
	 * package fragment or <code>null</code> if it does not exist.
	 */
	public ICompilationUnit[] getCompilationUnits(String projectName, String rootPath, String packageName) throws JavaModelException {
		IPackageFragment pkg= getPackageFragment(projectName, rootPath, packageName);
		if (pkg == null) {
			return null;
		}
		return pkg.getCompilationUnits();
	}
	protected ICompilationUnit getCompilationUnitFor(IJavaElement element) {
	
		if (element instanceof ICompilationUnit) {
			return (ICompilationUnit)element;
		}
	
		if (element instanceof IMember) {
			return ((IMember)element).getCompilationUnit();
		}
	
		if (element instanceof IPackageDeclaration ||
			element instanceof IImportDeclaration) {
				return (ICompilationUnit)element.getParent();
			}
	
		return null;
	
	}
	/**
	 * Returns the last delta for the given element from the cached delta.
	 */
	protected IJavaElementDelta getDeltaFor(IJavaElement element) {
		return getDeltaFor(element, false);
	}
	/**
	 * Returns the delta for the given element from the cached delta.
	 * If the boolean is true returns the first delta found.
	 */
	protected IJavaElementDelta getDeltaFor(IJavaElement element, boolean returnFirst) {
		IJavaElementDelta[] deltas = this.deltaListener.deltas;
		if (deltas == null) return null;
		IJavaElementDelta result = null;
		for (int i = 0; i < deltas.length; i++) {
			IJavaElementDelta delta = searchForDelta(element, this.deltaListener.deltas[i]);
			if (delta != null) {
				if (returnFirst) {
					return delta;
				}
				result = delta;
			}
		}
		return result;
	}
	/**
	 * Returns the IPath to the external java class library (e.g. jclMin.jar)
	 */
	protected IPath getExternalJCLPath() {
		return new Path(getExternalJCLPathString());
	}
	/**
	 * Returns the java.io path to the external java class library (e.g. jclMin.jar)
	 */
	protected String getExternalJCLPathString() {
		return EXTERNAL_JAR_DIR_PATH + File.separator + "jclMin.jar";
	}
	/**
	 * Returns the IPath to the root source of the external java class library (e.g. "src")
	 */
	protected IPath getExternalJCLRootSourcePath() {
		return new Path("src");
	}
	/**
	 * Returns the IPath to the source of the external java class library (e.g. jclMinsrc.zip)
	 */
	protected IPath getExternalJCLSourcePath() {
		return new Path(getExternalJCLSourcePathString());
	}
	/**
	 * Returns the java.io path to the source of the external java class library (e.g. jclMinsrc.zip)
	 */
	protected String getExternalJCLSourcePathString() {
		return EXTERNAL_JAR_DIR_PATH + File.separator + "jclMinsrc.zip";
	}
	protected IFile getFile(String path) {
		return getWorkspaceRoot().getFile(new Path(path));
	}
	/**
	 * Returns the Java Model this test suite is running on.
	 */
	public IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}
	/**
	 * Returns the Java Project with the given name in this test
	 * suite's model. This is a convenience method.
	 */
	public IJavaProject getJavaProject(String name) {
		IProject project = getProject(name);
		return JavaCore.create(project);
	}
	protected ILocalVariable getLocalVariable(String cuPath, String selectAt, String selection) throws JavaModelException {
		ISourceReference cu = getCompilationUnit(cuPath);
		IJavaElement[] elements = codeSelect(cu, selectAt, selection);
		if (elements.length == 0) return null;
		if (elements[0] instanceof ILocalVariable) {
			return (ILocalVariable)elements[0];
		}
		return null;
	}
	/**
	 * Returns the specified package fragment in the given project and root, or
	 * <code>null</code> if it does not exist.
	 * The rootPath must be specified as a project relative path. The empty
	 * path refers to the default package fragment.
	 */
	public IPackageFragment getPackageFragment(String projectName, String rootPath, String packageName) throws JavaModelException {
		IPackageFragmentRoot root= getPackageFragmentRoot(projectName, rootPath);
		if (root == null) {
			return null;
		}
		return root.getPackageFragment(packageName);
	}
	/**
	 * Returns the specified package fragment root in the given project, or
	 * <code>null</code> if it does not exist.
	 * If relative, the rootPath must be specified as a project relative path. 
	 * The empty path refers to the package fragment root that is the project
	 * folder iteslf.
	 * If absolute, the rootPath refers to either an external jar, or a resource 
	 * internal to the workspace
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(
		String projectName, 
		String rootPath)
		throws JavaModelException {
			
		IJavaProject project = getJavaProject(projectName);
		if (project == null) {
			return null;
		}
		IPath path = new Path(rootPath);
		if (path.isAbsolute()) {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IResource resource = workspaceRoot.findMember(path);
			IPackageFragmentRoot root;
			if (resource == null) {
				// external jar
				root = project.getPackageFragmentRoot(rootPath);
			} else {
				// resource in the workspace
				root = project.getPackageFragmentRoot(resource);
			}
			return root;
		} else {
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			if (roots == null || roots.length == 0) {
				return null;
			}
			for (int i = 0; i < roots.length; i++) {
				IPackageFragmentRoot root = roots[i];
				if (!root.isExternal()
					&& root.getUnderlyingResource().getProjectRelativePath().equals(path)) {
					return root;
				}
			}
		}
		return null;
	}
	protected IProject getProject(String project) {
		return getWorkspaceRoot().getProject(project);
	}
	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.core.tests.model").getEntry("/");
			return new File(Platform.asLocalURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getSourceWorkspacePath() {
		return getPluginDirectoryPath() +  java.io.File.separator + "workspace";
	}
	/**
	 * Returns the IWorkspace this test suite is running on.
	 */
	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	public IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}
	protected String displayString(String toPrint, int indent) {
    	char[] toDisplay = 
    		CharOperation.replace(
    			toPrint.toCharArray(), 
    			getExternalJCLPathString().toCharArray(), 
    			"getExternalJCLPathString()".toCharArray());
    	toDisplay = 
    		CharOperation.replace(
    			toDisplay, 
    			org.eclipse.jdt.core.tests.util.Util.displayString(getExternalJCLSourcePathString(), 0).toCharArray(), 
    			"getExternalJCLSourcePathString()".toCharArray());
    	String displayString = org.eclipse.jdt.core.tests.util.Util.displayString(new String(toDisplay), indent);
    	toDisplay = 
    		CharOperation.replace(
    			displayString.toCharArray(), 
    			"getExternalJCLPathString()".toCharArray(), 
    			("\"+ getExternalJCLPathString() + \"").toCharArray());
    	toDisplay = 
    		CharOperation.replace(
    			toDisplay, 
    			"getExternalJCLSourcePathString()".toCharArray(), 
    			("\"+ getExternalJCLSourcePathString() + \"").toCharArray());
    	return new String(toDisplay);
    }
	public byte[] read(java.io.File file) throws java.io.IOException {
		int fileLength;
		byte[] fileBytes = new byte[fileLength = (int) file.length()];
		java.io.FileInputStream stream = new java.io.FileInputStream(file);
		int bytesRead = 0;
		int lastReadSize = 0;
		while ((lastReadSize != -1) && (bytesRead != fileLength)) {
			lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
			bytesRead += lastReadSize;
		}
		stream.close();
		return fileBytes;
	}
	protected void removeJavaNature(String projectName) throws CoreException {
		IProject project = this.getProject(projectName);
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] {});
		project.setDescription(description, null);
	}
	/**
	 * Returns a delta for the given element in the delta tree
	 */
	protected IJavaElementDelta searchForDelta(IJavaElement element, IJavaElementDelta delta) {
	
		if (delta == null) {
			return null;
		}
		if (delta.getElement().equals(element)) {
			return delta;
		}
		for (int i= 0; i < delta.getAffectedChildren().length; i++) {
			IJavaElementDelta child= searchForDelta(element, delta.getAffectedChildren()[i]);
			if (child != null) {
				return child;
			}
		}
		return null;
	}
	protected void search(IJavaElement element, int limitTo, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		new SearchEngine().search(
			SearchPattern.createPattern(element, limitTo), 
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null
		);
	}
	protected void search(String patternString, int searchFor, int limitTo, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		int matchMode = patternString.indexOf('*') != -1 || patternString.indexOf('?') != -1
			? SearchPattern.R_PATTERN_MATCH
			: SearchPattern.R_EXACT_MATCH;
		SearchPattern pattern = SearchPattern.createPattern(
			patternString, 
			searchFor,
			limitTo, 
			matchMode | SearchPattern.R_CASE_SENSITIVE);
		new SearchEngine().search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null);
	}
	/**
	 * Sets the class path of the Java project.
	 */
	public void setClasspath(IJavaProject javaProject, IClasspathEntry[] classpath) {
		try {
			javaProject.setRawClasspath(classpath, null);
		} catch (JavaModelException e) {
			assertTrue("failed to set classpath", false);
		}
	}
	/**
	 * Check locally for the required JCL files, jclMin.jar and jclMinsrc.zip.
	 * If not available, copy from the project resources.
	 */
	public void setupExternalJCL() throws IOException {
		if (EXTERNAL_JAR_DIR_PATH != null) return;
		
		String separator = java.io.File.separator;
		String resourceJCLDir = getPluginDirectoryPath() + separator + "JCL";
		String localJCLPath = getWorkspaceRoot().getLocation().toFile().getParentFile().getCanonicalPath();
		EXTERNAL_JAR_DIR_PATH = localJCLPath;
		java.io.File jclDir = new java.io.File(localJCLPath);
		java.io.File jclMin =
			new java.io.File(localJCLPath + separator + "jclMin.jar");
		java.io.File jclMinsrc = new java.io.File(localJCLPath + separator + "jclMinsrc.zip");
		if (!jclDir.exists()) {
			if (!jclDir.mkdir()) {
				//mkdir failed
				throw new IOException("Could not create the directory " + jclDir);
			}
			//copy the two files to the JCL directory
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + "jclMin.jar");
			copy(resourceJCLMin, jclMin);
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + "jclMinsrc.zip");
			copy(resourceJCLMinsrc, jclMinsrc);
		} else {
			//check that the two files, jclMin.jar and jclMinsrc.zip are present
			//copy either file that is missing or less recent than the one in workspace
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + "jclMin.jar");
			if (jclMin.lastModified() < resourceJCLMin.lastModified()) {
				copy(resourceJCLMin, jclMin);
			}
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + "jclMinsrc.zip");
			if (jclMinsrc.lastModified() < resourceJCLMinsrc.lastModified()) {
				copy(resourceJCLMinsrc, jclMinsrc);
			}
		}
	}
	protected IJavaProject setUpJavaProject(final String projectName) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		String sourceWorkspacePath = getSourceWorkspacePath();
		String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
		copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName));
		
		// ensure variables are set
		if (JavaCore.getClasspathVariable("JCL_LIB") == null) {
			JavaCore.setClasspathVariables(
				new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
				new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
				null);
		}
	
		// create project
		final IProject project = getWorkspaceRoot().getProject(projectName);
		IWorkspaceRunnable populate = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(populate, null);
		return JavaCore.create(project);
	}
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		setupExternalJCL();
		
		// ensure autobuilding is turned off
		IWorkspaceDescription description = getWorkspace().getDescription();
		if (description.isAutoBuilding()) {
			description.setAutoBuilding(false);
			getWorkspace().setDescription(description);
		}
	}
	protected void sortElements(IJavaElement[] elements) {
		Util.Comparer comparer = new Util.Comparer() {
			public int compare(Object a, Object b) {
				IJavaElement elementA = (IJavaElement)a;
				IJavaElement elementB = (IJavaElement)b;
				return elementA.getElementName().compareTo(elementB.getElementName());
			}
		};
		Util.sort(elements, comparer);
	}
	protected void sortResources(Object[] resources) {
		Util.Comparer comparer = new Util.Comparer() {
			public int compare(Object a, Object b) {
				IResource resourceA = (IResource)a;
				IResource resourceB = (IResource)b;
				return resourceA.getName().compareTo(resourceB.getName());
			}
		};
		Util.sort(resources, comparer);
	}
	protected void sortTypes(IType[] types) {
		Util.Comparer comparer = new Util.Comparer() {
			public int compare(Object a, Object b) {
				IType typeA = (IType)a;
				IType typeB = (IType)b;
				return typeA.getFullyQualifiedName().compareTo(typeB.getFullyQualifiedName());
			}
		};
		Util.sort(types, comparer);
	}
	/**
	 * Starts listening to element deltas, and queues them in fgDeltas.
	 */
	public void startDeltas() {
		clearDeltas();
		JavaCore.addElementChangedListener(this.deltaListener);
	}
	/**
	 * Stops listening to element deltas, and clears the current deltas.
	 */
	public void stopDeltas() {
		JavaCore.removeElementChangedListener(this.deltaListener);
		clearDeltas();
	}
	/**
	 * Wait for autobuild notification to occur
	 */
	public static void waitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Platform.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}
	protected void waitUntilIndexesReady() {
		// dummy query for waiting until the indexes are ready
		SearchEngine engine = new SearchEngine();
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		try {
			engine.searchAllTypeNames(
				null,
				"!@$#!@".toCharArray(),
				SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
				IJavaSearchConstants.CLASS,
				scope, 
				new ITypeNameRequestor() {
					public void acceptClass(
						char[] packageName,
						char[] simpleTypeName,
						char[][] enclosingTypeNames,
						String path) {}
					public void acceptInterface(
						char[] packageName,
						char[] simpleTypeName,
						char[][] enclosingTypeNames,
						String path) {}
				},
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		} catch (CoreException e) {
		}
	}

}
