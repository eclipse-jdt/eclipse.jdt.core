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
import java.net.URL;
import java.util.*;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.Util;

public abstract class AbstractJavaModelTests extends SuiteOfTestCases {
	
	/**
	 * The java.io.File path to the directory that contains the external jars.
	 */
	protected static String EXTERNAL_JAR_DIR_PATH;
	
	public static class ProblemRequestor implements IProblemRequestor {
		public StringBuffer problems;
		public int problemCount;
		private char[] unitSource;
		public ProblemRequestor() {
			initialize(null);
		}
		public void acceptProblem(IProblem problem) {
			problems.append(++problemCount + (problem.isError() ? ". ERROR" : ". WARNING"));
			problems.append(" in " + new String(problem.getOriginatingFileName()));
			if (this.unitSource != null) {
				problems.append(((DefaultProblem)problem).errorReportSource(this.unitSource));
			}
			problems.append("\n");
			problems.append(problem.getMessage());
			problems.append("\n");
		}
		public void beginReporting() {
			this.problems.append("----------\n");
		}
		public void endReporting() {
			problems.append("----------\n");
		}
		public boolean isActive() {
			return true;
		}
		public void initialize(char[] source) {
			this.problems = new StringBuffer();
			this.problemCount = 0;
			this.unitSource = source;
		}
	}
	


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
		
		public ByteArrayOutputStream stackTraces;
	
		public void elementChanged(ElementChangedEvent ev) {
			IJavaElementDelta[] copy= new IJavaElementDelta[deltas.length + 1];
			System.arraycopy(deltas, 0, copy, 0, deltas.length);
			copy[deltas.length]= ev.getDelta();
			deltas= copy;
			
			new Throwable("Caller of IElementChangedListener#elementChanged").printStackTrace(new PrintStream(this.stackTraces));
		}
		protected void sortDeltas(IJavaElementDelta[] elementDeltas) {
			org.eclipse.jdt.internal.core.util.Util.Comparer comparer = new org.eclipse.jdt.internal.core.util.Util.Comparer() {
				public int compare(Object a, Object b) {
					IJavaElementDelta deltaA = (IJavaElementDelta)a;
					IJavaElementDelta deltaB = (IJavaElementDelta)b;
					return deltaA.getElement().getElementName().compareTo(deltaB.getElement().getElementName());
				}
			};
			org.eclipse.jdt.internal.core.util.Util.sort(elementDeltas, comparer);
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			for (int i=0, length= this.deltas.length; i<length; i++) {
				IJavaElementDelta[] projects = this.deltas[i].getAffectedChildren();
				sortDeltas(projects);
				for (int j=0, projectsLength=projects.length; j<projectsLength; j++) {
					buffer.append(projects[j]);
					if (j != projectsLength-1) {
						buffer.append("\n");
					}
				}
				IResourceDelta[] nonJavaProjects = this.deltas[i].getResourceDeltas();
				if (nonJavaProjects != null) {
					for (int j=0, nonJavaProjectsLength=nonJavaProjects.length; j<nonJavaProjectsLength; j++) {
						if (j == 0 && buffer.length() != 0) {
							buffer.append("\n");
						}
						buffer.append(nonJavaProjects[j]);
						if (j != nonJavaProjectsLength-1) {
							buffer.append("\n");
						}
					}
				}
				if (i != length-1) {
					buffer.append("\n\n");
				}
			}
			return buffer.toString();
		}
	}
	protected DeltaListener deltaListener = new DeltaListener();
	 
	
	public AbstractJavaModelTests(String name) {
		super(name);
	}

	public static Test buildTestSuite(Class evaluationTestClass) {
		return buildTestSuite(evaluationTestClass, null); //$NON-NLS-1$
	}

	public static Test buildTestSuite(Class evaluationTestClass, String suiteName) {
		TestSuite suite = new Suite(suiteName==null?evaluationTestClass.getName():suiteName);
		List tests = buildTestsList(evaluationTestClass);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
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
	protected void addLibrary(IJavaProject javaProject, String jarName, String sourceZipName, String[] pathAndContents, String compliance) throws CoreException, IOException {
		IProject project = javaProject.getProject();
		String projectLocation = project.getLocation().toOSString();
		String jarPath = projectLocation + File.separator + jarName;
		String sourceZipPath = projectLocation + File.separator + sourceZipName;
		org.eclipse.jdt.core.tests.util.Util.createJar(pathAndContents, jarPath, compliance);
		org.eclipse.jdt.core.tests.util.Util.createSourceZip(pathAndContents, sourceZipPath);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		String projectPath = '/' + project.getName() + '/';
		addLibraryEntry(javaProject, projectPath + jarName,  projectPath + sourceZipName, null, true);
	}
	protected void addLibraryEntry(IJavaProject project, String path, boolean exported) throws JavaModelException {
		addLibraryEntry(project, new Path(path), null, null, exported);
	} 
	protected void addLibraryEntry(IJavaProject project, String path, String srcAttachmentPath, String srcAttachmentPathRoot, boolean exported) throws JavaModelException{
		addLibraryEntry(
			project,
			new Path(path),
			srcAttachmentPath == null ? null : new Path(srcAttachmentPath),
			srcAttachmentPathRoot == null ? null : new Path(srcAttachmentPathRoot),
			exported
		);
	}
	protected void addLibraryEntry(IJavaProject project, IPath path, IPath srcAttachmentPath, IPath srcAttachmentPathRoot, boolean exported) throws JavaModelException{
		IClasspathEntry[] entries = project.getRawClasspath();
		int length = entries.length;
		System.arraycopy(entries, 0, entries = new IClasspathEntry[length + 1], 1, length);
		entries[0] = JavaCore.newLibraryEntry(path, srcAttachmentPath, srcAttachmentPathRoot, exported);
		project.setRawClasspath(entries, null);
	}
	protected void assertSortedElementsEqual(String message, String expected, IJavaElement[] elements) {
		sortElements(elements);
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
	protected void assertElementEquals(String message, String expected, IJavaElement element) {
		String actual = element == null ? "<null>" : ((JavaElement) element).toStringWithAncestors();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 3) + ",");
		}
		assertEquals(message, expected, actual);
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
	protected void assertProblems(String message, String expected, ProblemRequestor problemRequestor) {
		String actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(problemRequestor.problems.toString());
		String independantExpectedString = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(expected);
		if (!independantExpectedString.equals(actual)){
		 	System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actual, 2));
		}
		assertEquals(
			message,
			independantExpectedString,
			actual);
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
protected void assertDeltas(String message, String expected) {
	String actual = this.deltaListener.toString();
	if (!expected.equals(actual)) {
		System.out.println(displayString(actual, 2));
		System.err.println(this.deltaListener.stackTraces.toString());
	}
	assertEquals(
		message,
		expected,
		actual);
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
	protected void assertTypeParametersEqual(String expected, ITypeParameter[] typeParameters) throws JavaModelException {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < typeParameters.length; i++) {
			ITypeParameter typeParameter = typeParameters[i];
			buffer.append(typeParameter.getElementName());
			String[] bounds = typeParameter.getBounds();
			int length = bounds.length;
			if (length > 0)
				buffer.append(" extends ");
			for (int j = 0; j < length; j++) {
				buffer.append(bounds[j]);
				if (j != length -1) {
					buffer.append(" & ");
				}
			}
			buffer.append("\n");
		}
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 3) + ",");
		}
		assertEquals("Unepexeted type parameters", expected, actual);
	}
	protected void assertStringsEqual(String message, String expected, String[] strings) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < strings.length; i++){
			buffer.append(strings[i]);
			buffer.append("\n");
		}
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 3) + ",");
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
		this.deltaListener.stackTraces = new ByteArrayOutputStream();
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
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no project*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no exported project*/, 
				output, 
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				"1.4"
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
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no project*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no exported project*/, 
				output, 
				sourceOutputs,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				"1.4"
			);
	}
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String[] libraries, String output) throws CoreException {
		return 
			this.createJavaProject(
				projectName, 
				sourceFolders, 
				libraries, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no project*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no exported project*/, 
				output, 
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				"1.4"
			);
	}
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String[] libraries, String output, String compliance) throws CoreException {
		return 
			this.createJavaProject(
				projectName, 
				sourceFolders, 
				libraries, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no project*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no exported project*/, 
				output, 
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				compliance
			);
	}
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String[] libraries, String[] projects, String projectOutput) throws CoreException {
		return
			this.createJavaProject(
				projectName,
				sourceFolders,
				libraries,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				projects,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no exported project*/, 
				projectOutput,
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				"1.4"
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
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				projects,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				exportedProject, 
				projectOutput,
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				"1.4"
			);
	}
	protected IJavaProject createJavaProject(final String projectName, final String[] sourceFolders, final String[] libraries, final String[] projects, final boolean[] exportedProjects, final String projectOutput, final String[] sourceOutputs, final String[][] inclusionPatterns, final String[][] exclusionPatterns, final String compliance) throws CoreException {
		return
		this.createJavaProject(
			projectName,
			sourceFolders,
			libraries,
			null/*no inclusion pattern*/,
			null/*no exclusion pattern*/,
			projects,
			null/*no inclusion pattern*/,
			null/*no exclusion pattern*/,
			exportedProjects, 
			projectOutput,
			sourceOutputs,
			inclusionPatterns,
			exclusionPatterns,
			compliance
		);
	}
	protected IJavaProject createJavaProject(
			final String projectName,
			final String[] sourceFolders,
			final String[] libraries,
			final String[][] librariesInclusionPatterns,
			final String[][] librariesExclusionPatterns,
			final String[] projects,
			final String[][] projectsInclusionPatterns,
			final String[][] projectsExclusionPatterns,
			final boolean[] exportedProjects,
			final String projectOutput,
			final String[] sourceOutputs,
			final String[][] inclusionPatterns,
			final String[][] exclusionPatterns,
			final String compliance) throws CoreException {
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
					if (lib.startsWith("JCL")) {
						try {
							// ensure JCL variables are set
							setUpJCLClasspathVariables(compliance);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
//					 inclusion patterns
					IPath[] inclusionPaths;
					if (librariesInclusionPatterns == null) {
						inclusionPaths = new IPath[0];
					} else {
						String[] patterns = librariesInclusionPatterns[i];
						int length = patterns.length;
						inclusionPaths = new IPath[length];
						for (int j = 0; j < length; j++) {
							String inclusionPattern = patterns[j];
							inclusionPaths[j] = new Path(inclusionPattern);
						}
					}
					// exclusion patterns
					IPath[] exclusionPaths;
					if (librariesExclusionPatterns == null) {
						exclusionPaths = new IPath[0];
					} else {
						String[] patterns = librariesExclusionPatterns[i];
						int length = patterns.length;
						exclusionPaths = new IPath[length];
						for (int j = 0; j < length; j++) {
							String exclusionPattern = patterns[j];
							exclusionPaths[j] = new Path(exclusionPattern);
						}
					}
					if (lib.equals(lib.toUpperCase())) { // all upper case is a var 
						char[][] vars = CharOperation.splitOn(',', lib.toCharArray());
						entries[sourceLength+i] = JavaCore.newVariableEntry(
							new Path(new String(vars[0])), 
							vars.length > 1 ? new Path(new String(vars[1])) : null, 
							vars.length > 2 ? new Path(new String(vars[2])) : null);
					} else if (lib.startsWith("org.eclipse.jdt.core.tests.model.")) { // container
						entries[sourceLength+i] = JavaCore.newContainerEntry(
								new Path(lib),
								inclusionPaths,
								exclusionPaths, false);
					} else {
						IPath libPath = new Path(lib);
						if (!libPath.isAbsolute() && libPath.segmentCount() > 0 && libPath.getFileExtension() == null) {
							project.getFolder(libPath).create(true, true, null);
							libPath = projectPath.append(libPath);
						}
						entries[sourceLength+i] = JavaCore.newLibraryEntry(
								libPath,
								null,
								null,
								inclusionPaths,
								exclusionPaths,
								false);
					}
				}
				for  (int i= 0; i < projectLength; i++) {
					boolean isExported = exportedProjects != null && exportedProjects.length > i && exportedProjects[i];
					
					// inclusion patterns
					IPath[] inclusionPaths;
					if (projectsInclusionPatterns == null) {
						inclusionPaths = new IPath[0];
					} else {
						String[] patterns = projectsInclusionPatterns[i];
						int length = patterns.length;
						inclusionPaths = new IPath[length];
						for (int j = 0; j < length; j++) {
							String inclusionPattern = patterns[j];
							inclusionPaths[j] = new Path(inclusionPattern);
						}
					}
					// exclusion patterns
					IPath[] exclusionPaths;
					if (projectsExclusionPatterns == null) {
						exclusionPaths = new IPath[0];
					} else {
						String[] patterns = projectsExclusionPatterns[i];
						int length = patterns.length;
						exclusionPaths = new IPath[length];
						for (int j = 0; j < length; j++) {
							String exclusionPattern = patterns[j];
							exclusionPaths[j] = new Path(exclusionPattern);
						}
					}
					
					entries[sourceLength+libLength+i] =
						JavaCore.newProjectEntry(
								new Path(projects[i]),
								inclusionPaths,
								exclusionPaths,
								isExported);
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
				
				// set compliance level options
				if ("1.5".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
					javaProject.setOptions(options);
				}
				
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
		file = file.getAbsoluteFile();
		if (!file.exists())
			return;
		if (file.isDirectory()) {
			String[] files = file.list();
			//file.list() can return null
			if (files != null) {
				for (int i = 0; i < files.length; ++i) {
					deleteFile(new File(file, files[i]));
				}
			}
		}
		boolean success = file.delete();
		int retryCount = 60; // wait 1 minute at most
		while (!success && --retryCount >= 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			success = file.delete();
		}
		if (success) return;
		System.err.println("Failed to delete " + file.getPath());
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
			// just print for info
			System.out.println(e.getMessage());
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
				// just print for info
				System.out.println("Retry "+retryCount+": "+ e.getMessage());
			} catch (IllegalArgumentException iae) {
				// just print for info
				System.out.println("Retry "+retryCount+": "+ iae.getMessage());
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
		return new Path(getExternalJCLPathString(""));
	}	
	/**
	 * Returns the IPath to the external java class library (e.g. jclMin.jar)
	 */
	protected IPath getExternalJCLPath(String compliance) {
		return new Path(getExternalJCLPathString(compliance));
	}
	/**
	 * Returns the java.io path to the external java class library (e.g. jclMin.jar)
	 */
	protected String getExternalJCLPathString() {
		return getExternalJCLPathString("");
	}
	/**
	 * Returns the java.io path to the external java class library (e.g. jclMin.jar)
	 */
	protected String getExternalJCLPathString(String compliance) {
		return getExternalPath() + File.separator + "jclMin" + compliance + ".jar";
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
		return new Path(getExternalJCLSourcePathString(""));
	}
	/**
	 * Returns the IPath to the source of the external java class library (e.g. jclMinsrc.zip)
	 */
	protected IPath getExternalJCLSourcePath(String compliance) {
		return new Path(getExternalJCLSourcePathString(compliance));
	}
	/**
	 * Returns the java.io path to the source of the external java class library (e.g. jclMinsrc.zip)
	 */
	protected String getExternalJCLSourcePathString() {
		return getExternalJCLSourcePathString("");
	}
	/**
	 * Returns the java.io path to the source of the external java class library (e.g. jclMinsrc.zip)
	 */
	protected String getExternalJCLSourcePathString(String compliance) {
		return getExternalPath() + File.separator + "jclMin" + compliance + "src.zip";
	}
	/*
	 * Returns the IPath to the external directory that contains external jar files.
	 */
	protected String getExternalPath() {
		if (EXTERNAL_JAR_DIR_PATH == null)
			try {
				EXTERNAL_JAR_DIR_PATH = getWorkspaceRoot().getLocation().toFile().getParentFile().getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return EXTERNAL_JAR_DIR_PATH;
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
			if (root.exists()) {
				return root;
			}
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
	public ICompilationUnit getWorkingCopy(String path, boolean computeProblems) throws JavaModelException {
		return getWorkingCopy(path, "", computeProblems);
	}	
	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		return getWorkingCopy(path, source, new WorkingCopyOwner() {}, null/*don't compute problems*/);
	}	
	public ICompilationUnit getWorkingCopy(String path, String source, boolean computeProblems) throws JavaModelException {
		IProblemRequestor problemRequestor = computeProblems
			? new IProblemRequestor() {
				public void acceptProblem(IProblem problem) {}
				public void beginReporting() {}
				public void endReporting() {}
				public boolean isActive() {
					return true;
				}
			} 
			: null;
		return getWorkingCopy(path, source, new WorkingCopyOwner() {}, problemRequestor);
	}
	public ICompilationUnit getWorkingCopy(String path, String source, WorkingCopyOwner owner, IProblemRequestor problemRequestor) throws JavaModelException {
		ICompilationUnit workingCopy = getCompilationUnit(path).getWorkingCopy(owner, problemRequestor, null/*no progress monitor*/);
		workingCopy.getBuffer().setContents(source);
		workingCopy.makeConsistent(null/*no progress monitor*/);
		return workingCopy;
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
	protected void discardWorkingCopies(ICompilationUnit[] workingCopies) throws JavaModelException {
		if (workingCopies == null) return;
		for (int i = 0, length = workingCopies.length; i < length; i++)
			if (workingCopies[i] != null)
				workingCopies[i].discardWorkingCopy();
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
	 * Check locally for the required JCL files, <jclName>.jar and <jclName>src.zip.
	 * If not available, copy from the project resources.
	 */
	public void setupExternalJCL(String jclName) throws IOException {
		String externalPath = getExternalPath();
		String separator = java.io.File.separator;
		String resourceJCLDir = getPluginDirectoryPath() + separator + "JCL";
		java.io.File jclDir = new java.io.File(externalPath);
		java.io.File jclMin =
			new java.io.File(externalPath + separator + jclName + ".jar");
		java.io.File jclMinsrc = new java.io.File(externalPath + separator + jclName + "src.zip");
		if (!jclDir.exists()) {
			if (!jclDir.mkdir()) {
				//mkdir failed
				throw new IOException("Could not create the directory " + jclDir);
			}
			//copy the two files to the JCL directory
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + jclName + ".jar");
			copy(resourceJCLMin, jclMin);
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + jclName + "src.zip");
			copy(resourceJCLMinsrc, jclMinsrc);
		} else {
			//check that the two files, jclMin.jar and jclMinsrc.zip are present
			//copy either file that is missing or less recent than the one in workspace
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + jclName + ".jar");
			if (jclMin.lastModified() < resourceJCLMin.lastModified()) {
				copy(resourceJCLMin, jclMin);
			}
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + jclName + "src.zip");
			if (jclMinsrc.lastModified() < resourceJCLMinsrc.lastModified()) {
				copy(resourceJCLMinsrc, jclMinsrc);
			}
		}
	}
	protected IJavaProject setUpJavaProject(final String projectName) throws CoreException, IOException {
		return setUpJavaProject(projectName, "1.4");
	}
	protected IJavaProject setUpJavaProject(final String projectName, String compliance) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		String sourceWorkspacePath = getSourceWorkspacePath();
		String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
		copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName));
		
		// ensure variables are set
		setUpJCLClasspathVariables(compliance);
	
		// create project
		final IProject project = getWorkspaceRoot().getProject(projectName);
		IWorkspaceRunnable populate = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(populate, null);
		IJavaProject javaProject = JavaCore.create(project);
		if ("1.5".equals(compliance)) {
			// set options
			Map options = new HashMap();
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
			javaProject.setOptions(options);
			
			// replace JCL_LIB with JCL15_LIB, and JCL_SRC with JCL15_SRC
			IClasspathEntry[] classpath = javaProject.getRawClasspath();
			IPath jclLib = new Path("JCL_LIB");
			for (int i = 0, length = classpath.length; i < length; i++) {
				IClasspathEntry entry = classpath[i];
				if (entry.getPath().equals(jclLib)) {
					classpath[i] = JavaCore.newVariableEntry(new Path("JCL15_LIB"), new Path("JCL15_SRC"), entry.getSourceAttachmentRootPath(), entry.getInclusionPatterns(), entry.getExclusionPatterns(), entry.isExported());
					break;
				}
			}
			javaProject.setRawClasspath(classpath, null);
		}
		return javaProject;
	}
	public void setUpJCLClasspathVariables(String compliance) throws JavaModelException, IOException {
		if ("1.5".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL15_LIB") == null) {
				setupExternalJCL("jclMin1.5");
				JavaCore.setClasspathVariables(
					new String[] {"JCL15_LIB", "JCL15_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath(compliance), getExternalJCLSourcePath(compliance), getExternalJCLRootSourcePath()},
					null);
			} 
		} else {
			if (JavaCore.getClasspathVariable("JCL_LIB") == null) {
				setupExternalJCL("jclMin");
				JavaCore.setClasspathVariables(
					new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
					null);
			} 
		}	
	}
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		
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
				JavaElement elementA = (JavaElement)a;
				JavaElement elementB = (JavaElement)b;
				char[] tempJCLPath = "<externalJCLPath>".toCharArray();
	    		String idA = new String(CharOperation.replace(
	    			elementA.toStringWithAncestors().toCharArray(), 
	    			getExternalJCLPathString().toCharArray(), 
	    			tempJCLPath));
	    		String idB = new String(CharOperation.replace(
	    			elementB.toStringWithAncestors().toCharArray(), 
	    			getExternalJCLPathString().toCharArray(), 
	    			tempJCLPath));
				return idA.compareTo(idB);
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
