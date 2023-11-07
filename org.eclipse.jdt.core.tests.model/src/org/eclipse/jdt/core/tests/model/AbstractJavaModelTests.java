/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.core.internal.resources.CharsetDeltaJob;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaCorePreferenceInitializer;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaElementDelta;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.JrtPackageFragmentRoot;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.util.Util;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractJavaModelTests extends SuiteOfTestCases {

	/**
	 * The java.io.File path to the directory that contains the external jars.
	 */
	protected static String EXTERNAL_JAR_DIR_PATH;

	/**
	 * The java.io.File path to the workspace directory.
	 */
	protected static String WORKSPACE_DIR_PATH;

	// used java project
	protected IJavaProject currentProject;

	// working copies usage
	protected ICompilationUnit[] workingCopies;
	protected WorkingCopyOwner wcOwner;

	// infos for invalid results
	protected int tabs = 2;
	protected boolean displayName = false;
	protected String endChar = ",";

	protected static boolean isJRE9 = false;
	protected static boolean isJRE10 = false;
	protected static boolean isJRE11 = false;
	protected static boolean isJRE12 = false;
	protected static boolean isJRE13 = false;
	protected static boolean isJRE14 = false;
	protected static boolean isJRE15 = false;
	protected static boolean isJRE16 = false;
	protected static boolean isJRE17 = false;
	protected static boolean isJRE18 = false;
	protected static boolean isJRE19 = false;
	protected static boolean isJRE20 = false;
	protected static boolean isJRE21 = false;
	static {
		String javaVersion = System.getProperty("java.version");
		String vmName = System.getProperty("java.vm.name");
		int index = -1;
		if ( (index = javaVersion.indexOf('-')) != -1) {
			javaVersion = javaVersion.substring(0, index);
		} else {
			if (javaVersion.length() > 3) {
				javaVersion = javaVersion.substring(0, 3);
			}
		}
		long jdkLevel = CompilerOptions.versionToJdkLevel(javaVersion.length() > 3 ? javaVersion.substring(0, 3) : javaVersion);
		if (jdkLevel >= ClassFileConstants.JDK21) {
			isJRE21 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK20) {
			isJRE20 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK19) {
			isJRE19 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK18) {
			isJRE18 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK17) {
			isJRE17 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK16) {
			isJRE16 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK15) {
			isJRE15 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK14) {
			isJRE14 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK12) {
			isJRE12 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK11) {
			isJRE11 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK10) {
			isJRE10 = true;
		}
		if (jdkLevel >= ClassFileConstants.JDK9) {
			isJRE9 = true;
			System.out.println("Recognized Java version '"+javaVersion+"' with vm.name '"+vmName+"'");
		}
	}

	/**
	 * Internal synonym for constant AST.JSL9
	 * to alleviate deprecation warnings once AST.JLS9 is deprecated in future.
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS9 = AST.JLS9;
	/**
	 * Internal synonym for constant AST.JSL10
	 * to alleviate deprecation warnings once AST.JLS10 is deprecated in future.
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS10 = AST.JLS10;

	/**
	 * Internal synonym for constant AST.JSL11
	 * to alleviate deprecation warnings once AST.JLS11 is deprecated in future.
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS11 = AST.JLS11;

	/**
	 * Internal synonym for constant AST.JSL12
	 * to alleviate deprecation warnings once AST.JLS12 is deprecated in future.
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS12 = AST.JLS12;

	/**
	 * Internal synonym for constant AST.JSL13
	 * to alleviate deprecation warnings once AST.JLS13 is deprecated in future.
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS13 = AST.JLS13;

	/**
	 * Internal synonym for constant AST.JSL14
	 * to alleviate deprecation warnings once AST.JLS14 is deprecated in future.
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS14 = AST.JLS14;

	/**
	 * Internal synonym for constant AST.JSL15
	 * to alleviate deprecation warnings once AST.JLS15 is deprecated in future.
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS15 = AST.JLS15;

	/**
	 * Internal synonym for constant AST.JSL16
	 * to alleviate deprecation warnings once AST.JLS16 is deprecated in future.
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS16 = AST.JLS16;

	/**
	 * Internal synonym for constant AST.JSL17
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS17 = AST.JLS17;
	/**
	 * Internal synonym for constant AST.JSL18
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS18 = AST.JLS18;
	/**
	 * Internal synonym for constant AST.JSL19
	 * @deprecated
	 */
	protected static final int AST_INTERNAL_JLS19 = AST.JLS19;
	/**
	 * Internal synonym for constant AST.JSL20
	 */
	protected static final int AST_INTERNAL_JLS20 = AST.JLS20;
	/**
	 * Internal synonym for constant AST.JSL21
	 */
	protected static final int AST_INTERNAL_JLS21 = AST.JLS21;
	/**
	 * Internal synonym for the latest AST level.
	 */
	protected static final int AST_INTERNAL_LATEST = AST.getJLSLatest();

	public static class BasicProblemRequestor implements IProblemRequestor {
		public void acceptProblem(IProblem problem) {}
		public void beginReporting() {}
		public void endReporting() {}
		public boolean isActive() {
			return true;
		}
	}

	public static class ProblemRequestor implements IProblemRequestor {
		public StringBuffer problems;
		public int problemCount;
		protected char[] unitSource;
		public boolean isActive = true;
		public ProblemRequestor() {
			initialize(null);
		}
		public void acceptProblem(IProblem problem) {
			org.eclipse.jdt.core.tests.util.Util.appendProblem(this.problems, problem, this.unitSource, ++this.problemCount);
			this.problems.append("----------\n");
		}
		public void beginReporting() {
			this.problems.append("----------\n");
		}
		public void endReporting() {
			if (this.problemCount == 0)
				this.problems.append("----------\n");
		}
		public boolean isActive() {
			return this.isActive;
		}
		public void initialize(char[] source) {
			reset();
			this.unitSource = source;
		}
		public void reset() {
			this.problems = new StringBuffer();
			this.problemCount = 0;
		}
	}

	/**
	 * Delta listener
	 */
	protected class DeltaListener implements IElementChangedListener, IResourceChangeListener {
		/**
		 * Deltas received from the java model. See
		 * <code>#startDeltas</code> and
		 * <code>#stopDeltas</code>.
		 */
		private IJavaElementDelta[] deltas;

		private final int eventType;

		private ByteArrayOutputStream stackTraces;

		private volatile boolean gotResourceDelta;

		public DeltaListener() {
			flush();
			this.eventType = -1;
		}
		public DeltaListener(int eventType) {
			flush();
			this.eventType = eventType;
		}

		public synchronized void elementChanged(ElementChangedEvent event) {
			if (this.eventType == -1 || event.getType() == this.eventType) {
				IJavaElementDelta[] copy= new IJavaElementDelta[this.deltas.length + 1];
				System.arraycopy(this.deltas, 0, copy, 0, this.deltas.length);
				copy[this.deltas.length]= event.getDelta();
				this.deltas= copy;
				StringBuilder message = new StringBuilder();
				Job currentJob = Job.getJobManager().currentJob();
				if (currentJob != null) {
					message.append(currentJob.getName()).append(' ');
				}
				message.append('[').append(Thread.currentThread().getName());
				message.append("] Caller of IElementChangedListener#elementChanged with delta ");
				message.append(event.getDelta());
				new Throwable(message.toString()).printStackTrace(new PrintStream(this.stackTraces));
			}
		}
		public synchronized CompilationUnit getCompilationUnitAST(ICompilationUnit workingCopy) {
			for (int i=0, length= this.deltas.length; i<length; i++) {
				CompilationUnit result = getCompilationUnitAST(workingCopy, this.deltas[i]);
				if (result != null)
					return result;
			}
			return null;
		}
		private CompilationUnit getCompilationUnitAST(ICompilationUnit workingCopy, IJavaElementDelta delta) {
			if ((delta.getFlags() & IJavaElementDelta.F_AST_AFFECTED) != 0 && workingCopy.equals(delta.getElement()))
				return delta.getCompilationUnitAST();
			return null;
		}

		/**
		 * Returns the last delta for the given element from the cached delta.
		 */
		public IJavaElementDelta getDeltaFor(IJavaElement element) {
			return getDeltaFor(element, false);
		}

		/**
		 * Returns the delta for the given element from the cached delta.
		 * If the boolean is true returns the first delta found.
		 */
		public synchronized IJavaElementDelta getDeltaFor(IJavaElement element, boolean returnFirst) {
			JavaModelManager.getIndexManager().waitForIndex(isIndexDisabledForTest(), null);
			if (this.deltas == null) waitForResourceDelta();
			if (this.deltas == null) return null;
			IJavaElementDelta result = null;
			for (int i = 0; i < this.deltas.length; i++) {
				IJavaElementDelta delta = searchForDelta(element, this.deltas[i]);
				if (delta != null) {
					if (returnFirst) {
						return delta;
					}
					result = delta;
				}
			}
			return result;
		}

		public synchronized IJavaElementDelta getLastDelta() {
			return this.deltas[this.deltas.length - 1];
		}

		public synchronized List<IJavaElementDelta> getAllDeltas() {
			return List.of(this.deltas);
		}

		public synchronized void flush() {
			this.deltas = new IJavaElementDelta[0];
			this.stackTraces = new ByteArrayOutputStream();
			this.gotResourceDelta = false;
		}
		protected void sortDeltas(IJavaElementDelta[] elementDeltas) {
        	org.eclipse.jdt.internal.core.util.Util.Comparer comparer = new org.eclipse.jdt.internal.core.util.Util.Comparer() {
        		public int compare(Object a, Object b) {
        			IJavaElementDelta deltaA = (IJavaElementDelta)a;
        			IJavaElementDelta deltaB = (IJavaElementDelta)b;
        			// Make sure JRT elements and other external JAR elements always
        			// come in the same position with respect to other kind. These two
        			// kinds usually come from two entirely different locations which makes
        			// the sorting by path unpredictable.
        			boolean isAFromJRT = deltaA.getElement() instanceof JrtPackageFragmentRoot;
        			boolean isBFromJRT = deltaB.getElement() instanceof JrtPackageFragmentRoot;
        			int result = 0;
        			if (isAFromJRT) {
        				if (!isBFromJRT) {
        					result = 1;
        				}
        			} else if (isBFromJRT) {
        				result = -1;
        			}
        			if (result != 0)
        				return result;
        			return toString(deltaA).compareTo(toString(deltaB));
        		}
        		private String toString(IJavaElementDelta delta) {
        			if (delta.getElement().getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
        				return delta.getElement().getPath().setDevice(null).toString();
        			}
        			return delta.toString();
        		}
        	};
        	org.eclipse.jdt.internal.core.util.Util.sort(elementDeltas, comparer);
        	for (int i = 0, max = elementDeltas.length; i < max; i++) {
        		IJavaElementDelta delta = elementDeltas[i];
        		IJavaElementDelta[] children = delta.getAffectedChildren();
        		if (children != null) {
        			sortDeltas(children);
        		}
        	}
        }
		public synchronized void resourceChanged(IResourceChangeEvent event) {
			this.gotResourceDelta = true;
		}
		/**
		 * Returns a delta for the given element in the delta tree
		 */
		private IJavaElementDelta searchForDelta(IJavaElement element, IJavaElementDelta delta) {
			if (delta == null) {
				return null;
			}
			IJavaElement deltaElement = delta.getElement();
			if (deltaElement.equals(element)) {
				return delta;
			}
			IJavaElementDelta[] affectedChildren = delta.getAffectedChildren();
			for (IJavaElementDelta affectedChild : affectedChildren) {
				IJavaElementDelta child= searchForDelta(element, affectedChild);
				if (child != null) {
					return child;
				}
			}
			return null;
		}
		public synchronized String stackTraces() {
			return this.stackTraces.toString();
		}

		@Override
		public synchronized String toString() {
			StringBuilder buffer = new StringBuilder();
			for (int i = 0, length= this.deltas.length; i < length; i++) {
				IJavaElementDelta delta = this.deltas[i];
				if (((JavaElementDelta) delta).ignoreFromTests) {
					continue;
				}
				if (buffer.length() != 0) {
					buffer.append("\n\n");
				}
				IJavaElementDelta[] children = delta.getAffectedChildren();
				int childrenLength=children.length;
				IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
				int resourceDeltasLength = resourceDeltas == null ? 0 : resourceDeltas.length;
				if (childrenLength == 0 && resourceDeltasLength == 0) {
					buffer.append(delta);
				} else {
					sortDeltas(children);
					for (int j = 0; j < childrenLength; j++) {
						if (buffer.length() != 0 && buffer.charAt(buffer.length() - 1) != '\n') {
							buffer.append('\n');
						}
						buffer.append(children[j]);
					}
					for (int j = 0; j < resourceDeltasLength; j++) {
						if (buffer.length() != 0 && buffer.charAt(buffer.length() - 1) != '\n') {
							buffer.append('\n');
						}
						buffer.append(resourceDeltas[j]);
					}
				}
			}
			return buffer.toString();
		}

		public void waitForResourceDelta() {
			long start = System.currentTimeMillis();
			while (!this.gotResourceDelta) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				if ((System.currentTimeMillis() - start) > 10000/*wait 10 s max*/) {
					throw new RuntimeException("Didn't get resource delta after 10 seconds");
				}
			}
		}
	}
	protected DeltaListener deltaListener = new DeltaListener();

	public static class LogListenerWithHistory implements ILogListener {
		private final StringBuffer buffer = new StringBuffer();
		private final List<IStatus> logs = new ArrayList<>();

		public void logging(IStatus status, String plugin) {
			this.logs.add(status);
			this.buffer.append(status);
			this.buffer.append('\n');
		}

		public String toString() {
			return this.buffer.toString();
		}

		public List<IStatus> getLogs() {
			return this.logs;
		}
	}

	protected LogListenerWithHistory logListener;
	protected ILog log;

	protected static boolean systemConfigReported;


	public AbstractJavaModelTests(String name) {
		super(name);
	}

	public AbstractJavaModelTests(String name, int tabs) {
		super(name);
		this.tabs = tabs;
	}

	/**
	 * See buildModelTestSuite(Class evaluationTestClass) for more information.
	 *
	 * @param minCompliance minimum compliance level required to run this test suite
	 * @return a test suite ({@link Test})
	 */
	public static Test buildModelTestSuite(int minCompliance, Class evaluationTestClass) {
		if (AbstractCompilerTest.getPossibleComplianceLevels() >= minCompliance)
			return buildModelTestSuite(evaluationTestClass, ORDERING);
		return new Suite(evaluationTestClass.getName());
	}

	/**
	 * Build a test suite with all tests computed from public methods starting with "test"
	 * found in the given test class.
	 * Test suite name is the name of the given test class.
	 *
	 * Note that this lis maybe reduced using some mechanisms detailed in {@link #buildTestsList(Class)} method.
	 *
	 * This test suite differ from this computed in {@link TestCase} in the fact that this is
	 * a {@link SuiteOfTestCases.Suite} instead of a simple framework {@link TestSuite}.
	 *
	 * @return a test suite ({@link Test})
	 */
	public static Test buildModelTestSuite(Class evaluationTestClass) {
		return buildModelTestSuite(evaluationTestClass, ORDERING);
	}

	/**
	 * Build a test suite with all tests computed from public methods starting with "test"
	 * found in the given test class and sorted in alphabetical order.
	 * Test suite name is the name of the given test class.
	 *
	 * Note that this lis maybe reduced using some mechanisms detailed in {@link #buildTestsList(Class)} method.
	 *
	 * This test suite differ from this computed in {@link TestCase} in the fact that this is
	 * a {@link SuiteOfTestCases.Suite} instead of a simple framework {@link TestSuite}.
	 *
	 * @param ordering kind of sort use for the list (see {@link #ORDERING} for possible values)
	 * @return a test suite ({@link Test})
	 */
	public static Test buildModelTestSuite(Class evaluationTestClass, long ordering) {
		TestSuite suite = new Suite(evaluationTestClass.getName());
		List tests = buildTestsList(evaluationTestClass, 0, ordering);
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
	protected IProjectDescription projectDescriptionForLocation(String projectName, URI location) throws CoreException {
		IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
		desc.setLocationURI(location);
		return desc;
	}
	protected void assertSearchResults(String expected, Object collector) {
		assertSearchResults("Unexpected search results", expected, collector);
	}
	protected void assertSearchResults(String message, String expected, Object collector) {
		assertSearchResults(message, expected, collector, true /* assertion */);
	}
	private static String sortLines(String toSplit) {
		return Arrays.stream(toSplit.split("\n")).sorted().collect(Collectors.joining("\n"));
	}
	protected void assertSearchResults(String message, String expectedString, Object collector, boolean assertion) {
		String expected = sortLines(expectedString);
		String actual = sortLines(collector.toString());
		if (!expected.equals(actual)) {
			if (this.displayName) System.out.println(getName()+" actual result is:");
			System.out.print(displayString(actual, this.tabs));
			System.out.println(",");
		}
		if (assertion) {
			assertEquals(message, expected, actual);
		} else {
			assumeEquals(message, expected, actual);
		}
	}
	protected void assertScopeEquals(String expected, IJavaSearchScope scope) {
		String actual = scope.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 3) + ",");
		}
		assertEquals("Unexpected scope", expected, actual);
	}
	protected void addClasspathEntry(IJavaProject project, IClasspathEntry entry) throws JavaModelException{
		IClasspathEntry[] entries = project.getRawClasspath();
		int length = entries.length;
		System.arraycopy(entries, 0, entries = new IClasspathEntry[length + 1], 0, length);
		entries[length] = entry;
		project.setRawClasspath(entries, null);
	}
	protected void addClasspathEntry(IJavaProject project, IClasspathEntry entry, int position) throws JavaModelException{
		IClasspathEntry[] entries = project.getRawClasspath();
		int length = entries.length;
		IClasspathEntry[] newEntries = new IClasspathEntry[length + 1];
		for (int srcIdx = 0, tgtIdx = 0; tgtIdx < length+1; tgtIdx++) {
			newEntries[tgtIdx] = (tgtIdx == position) ? entry : entries[srcIdx++];
		}
		project.setRawClasspath(newEntries, null);
	}
	protected void addClassFolder(IJavaProject javaProject, String folderRelativePath, String[] pathAndContents, String compliance) throws CoreException, IOException {
		IProject project = javaProject.getProject();
		String projectLocation = project.getLocation().toOSString();
		String folderPath = projectLocation + File.separator + folderRelativePath;
    	org.eclipse.jdt.core.tests.util.Util.createClassFolder(pathAndContents, folderPath, compliance);
    	project.refreshLocal(IResource.DEPTH_INFINITE, null);
		String projectPath = '/' + project.getName() + '/';
		addLibraryEntry(
			javaProject,
			new Path(projectPath + folderRelativePath),
			null,
			null,
			null,
			null,
			true
		);

	}
	protected void addExternalLibrary(IJavaProject javaProject, String jarPath, String[] pathAndContents, String[] nonJavaResources, String compliance) throws Exception {
		String[] claspath = getJCL15PlusLibraryIfNeeded(compliance);
		org.eclipse.jdt.core.tests.util.Util.createJar(pathAndContents, nonJavaResources, jarPath, claspath, compliance);
		addLibraryEntry(javaProject, new Path(jarPath), true/*exported*/);
	}
	protected void addLibrary(String jarName, String sourceZipName, String[] pathAndContents, String compliance) throws CoreException, IOException {
		addLibrary(this.currentProject, jarName, sourceZipName, pathAndContents, null/*no non-Java resources*/, null, null, compliance, null);
	}
	protected void addLibrary(IJavaProject javaProject, String jarName, String sourceZipName, String[] pathAndContents, String compliance, Map options) throws CoreException, IOException {
		addLibrary(javaProject, jarName, sourceZipName, pathAndContents, null/*no non-Java resources*/, null, null, compliance, options);
	}
	protected void addLibrary(IJavaProject javaProject, String jarName, String sourceZipName, String[] pathAndContents, String compliance) throws CoreException, IOException {
		addLibrary(javaProject, jarName, sourceZipName, pathAndContents, null/*no non-Java resources*/, null, null, compliance, null);
	}
	protected void addLibrary(IJavaProject javaProject, String jarName, String sourceZipName, String[] pathAndContents, String compliance, boolean exported) throws CoreException, IOException {
		addLibrary(javaProject, jarName, sourceZipName, pathAndContents, null/*no non-Java resources*/, null, null, compliance, null, exported);
	}
	protected void addLibrary(IJavaProject javaProject, String jarName, String sourceZipName, String[] pathAndContents, String[] nonJavaResources, String compliance) throws CoreException, IOException {
		addLibrary(javaProject, jarName, sourceZipName, pathAndContents, nonJavaResources, null, null, compliance, null);
	}
	protected IClasspathAttribute[] moduleAttribute() {
		return new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
	}
	protected IClasspathEntry newModularLibraryEntry(IPath path, IPath sourceAttachmentPath, IPath sourceAttachmentRootPath) {
		return JavaCore.newLibraryEntry(path, sourceAttachmentPath, sourceAttachmentRootPath, null, moduleAttribute(), false);
	}
	protected void addModularLibraryEntry(IJavaProject project, IPath libraryPath, IPath sourceAttachmentPath) throws JavaModelException {
		addClasspathEntry(project, newModularLibraryEntry(libraryPath, sourceAttachmentPath, null));
	}
	protected void addModularLibrary(IJavaProject javaProject, String jarName, String sourceZipName, String[] pathAndContents, String compliance) throws CoreException, IOException {
		createLibrary(javaProject, jarName, sourceZipName, pathAndContents, null, compliance);
		IPath projectPath = javaProject.getPath();
		addModularLibraryEntry(javaProject, projectPath.append(jarName), projectPath.append(sourceZipName));
	}
	protected void addLibrary(
			IJavaProject javaProject,
			String jarName,
			String sourceZipName,
			String[] pathAndContents,
			String[] nonJavaResources,
			String[] librariesInclusionPatterns,
			String[] librariesExclusionPatterns,
			String compliance,
			Map options) throws CoreException, IOException {
		addLibrary(javaProject, jarName, sourceZipName, pathAndContents, nonJavaResources, librariesInclusionPatterns, librariesExclusionPatterns, compliance, options, true);
	}
	private void addLibrary(
			IJavaProject javaProject,
			String jarName,
			String sourceZipName,
			String[] pathAndContents,
			String[] nonJavaResources,
			String[] librariesInclusionPatterns,
			String[] librariesExclusionPatterns,
			String compliance,
			Map options,
			boolean exported) throws CoreException, IOException {
		IProject project = createLibrary(javaProject, jarName, sourceZipName, pathAndContents, nonJavaResources, compliance, options);
		String projectPath = '/' + project.getName() + '/';
		addLibraryEntry(
			javaProject,
			new Path(projectPath + jarName),
			sourceZipName == null ? null : new Path(projectPath + sourceZipName),
			null,
			toIPathArray(librariesInclusionPatterns),
			toIPathArray(librariesExclusionPatterns),
			exported
		);
	}
	protected IProject createLibrary(
			IJavaProject javaProject,
			String jarName,
			String sourceZipName,
			String[] pathAndContents,
			String[] nonJavaResources,
			String compliance) throws IOException, CoreException {
		return createLibrary(javaProject, jarName, sourceZipName, pathAndContents, nonJavaResources, compliance, null);
	}

	protected IProject createLibrary(
			IJavaProject javaProject,
			String jarName,
			String sourceZipName,
			String[] pathAndContents,
			String[] nonJavaResources,
			String compliance,
			Map options) throws IOException, CoreException {
		IProject project = javaProject.getProject();
		String projectLocation = project.getLocation().toOSString();
		String jarPath = projectLocation + File.separator + jarName;
		String[] claspath = getJCL15PlusLibraryIfNeeded(compliance);
		org.eclipse.jdt.core.tests.util.Util.createJar(pathAndContents, nonJavaResources, jarPath, claspath, compliance, options);
		if (pathAndContents != null && pathAndContents.length != 0) {
			String sourceZipPath = projectLocation + File.separator + sourceZipName;
			org.eclipse.jdt.core.tests.util.Util.createSourceZip(pathAndContents, sourceZipPath);
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		return project;
	}

	static IClasspathAttribute[] externalAnnotationExtraAttributes(String path) {
		return new IClasspathAttribute[] {
				new ClasspathAttribute(IClasspathAttribute.EXTERNAL_ANNOTATION_PATH, path)
		};
	}

	protected void addLibraryWithExternalAnnotations(
			IJavaProject javaProject,
			String compliance,
			String jarName,
			String externalAnnotationPath,
			String[] pathAndContents,
			Map options) throws CoreException, IOException
	{
		createLibrary(javaProject, jarName, "src.zip", pathAndContents, null, compliance, options);
		String jarPath = '/' + javaProject.getProject().getName() + '/' + jarName;
		IClasspathEntry entry = JavaCore.newLibraryEntry(
				new Path(jarPath),
				new Path('/'+javaProject.getProject().getName()+"/src.zip"),
				null/*src attach root*/,
				null/*access rules*/,
				externalAnnotationExtraAttributes(externalAnnotationPath),
				false/*exported*/);
		addClasspathEntry(javaProject, entry);
	}

	protected void addLibraryEntry(String path, boolean exported) throws JavaModelException {
		addLibraryEntry(this.currentProject, new Path(path), null, null, null, null, exported);
	}
	protected void addLibraryEntry(IJavaProject project, String path, boolean exported) throws JavaModelException {
		addLibraryEntry(project, new Path(path), exported);
	}
	protected void addLibraryEntry(IJavaProject project, IPath path, boolean exported) throws JavaModelException {
		addLibraryEntry(project, path, null, null, null, null, exported);
	}
	protected void addLibraryEntry(IJavaProject project, String path, String srcAttachmentPath) throws JavaModelException{
		addLibraryEntry(
			project,
			new Path(path),
			srcAttachmentPath == null ? null : new Path(srcAttachmentPath),
			null,
			null,
			null,
			new IClasspathAttribute[0],
			false
		);
	}
	protected void addLibraryEntry(IJavaProject project, IPath path, IPath srcAttachmentPath, IPath srcAttachmentPathRoot, IPath[] accessibleFiles, IPath[] nonAccessibleFiles, boolean exported) throws JavaModelException{
		addLibraryEntry(
			project,
			path,
			srcAttachmentPath,
			srcAttachmentPathRoot,
			accessibleFiles,
			nonAccessibleFiles,
			new IClasspathAttribute[0],
			exported
		);
	}
	protected void addLibraryEntry(IJavaProject project, IPath path, IPath srcAttachmentPath, IPath srcAttachmentPathRoot, IPath[] accessibleFiles, IPath[] nonAccessibleFiles, IClasspathAttribute[] extraAttributes, boolean exported) throws JavaModelException{
		IClasspathEntry entry = JavaCore.newLibraryEntry(
			path,
			srcAttachmentPath,
			srcAttachmentPathRoot,
			ClasspathEntry.getAccessRules(accessibleFiles, nonAccessibleFiles),
			extraAttributes,
			exported);
		addClasspathEntry(project, entry);
	}

	protected void addModularProjectEntry(IJavaProject project, IJavaProject depProject) throws JavaModelException {
		addClasspathEntry(project, newModularProjectEntry(depProject));
	}

	protected IClasspathEntry newModularProjectEntry(IJavaProject depProject) {
		return JavaCore.newProjectEntry(depProject.getPath(), null, false, moduleAttribute(), false);
	}

	protected void assertSortedElementsEqual(String message, String expected, IJavaElement[] elements) {
		sortElements(elements);
		assertElementsEqual(message, expected, elements);
	}

	protected void assertWorkingCopyDeltas(String message, String expected) {
		assertDeltas(message, expected, false/*don't wait for resource delta*/);
	}

	protected void assertResourceEquals(String message, String expected, IResource resource) {
		String actual = resource == null ? "<null>" : resource.getFullPath().toString();
		if (!expected.equals(actual)) {
			System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(actual));
			System.out.println(this.endChar);
		}
		assertEquals(message, expected, actual);
	}

	protected void assertResourcesEqual(String message, String expected, Object[] resources) {
		sortResources(resources);
		StringBuilder buffer = new StringBuilder();
		for (int i = 0, length = resources.length; i < length; i++) {
			if (resources[i] instanceof IResource) {
				buffer.append(((IResource) resources[i]).getFullPath().toString());
			} else if (resources[i] instanceof IStorage) {
				buffer.append(((IStorage) resources[i]).getFullPath().toString());
			} else if (resources[i] == null) {
				buffer.append("<null>");
			}
			if (i != length-1)buffer.append("\n");
		}
		if (!expected.equals(buffer.toString())) {
			System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(buffer.toString(), 2));
			System.out.println(this.endChar);
		}
		assertEquals(
			message,
			expected,
			buffer.toString()
		);
	}

	protected void assertResourceNamesEqual(String message, String expected, Object[] resources) {
		sortResources(resources);
		StringBuilder buffer = new StringBuilder();
		for (int i = 0, length = resources.length; i < length; i++) {
			if (resources[i] instanceof IResource) {
				buffer.append(((IResource)resources[i]).getName());
			} else if (resources[i] instanceof IStorage) {
				buffer.append(((IStorage) resources[i]).getName());
			} else if (resources[i] == null) {
				buffer.append("<null>");
			}
			if (i != length-1)buffer.append("\n");
		}
		if (!expected.equals(buffer.toString())) {
			System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(buffer.toString(), 2));
			System.out.println(this.endChar);
		}
		assertEquals(
			message,
			expected,
			buffer.toString()
		);
	}

	protected void assertResourceOnClasspathEntry(IJavaProject project, IResource resource, String path) {
		IClasspathEntry cp = project.findContainingClasspathEntry(resource);
		assertNotNull("IClasspathEntry exists for the resource", cp);
		assertEquals("In the expected classpath entry", path, cp.getPath().toPortableString());
	}

	protected void assertResourceNotOnClasspathEntry(IJavaProject project, IResource resource) {
		IClasspathEntry cp = project.findContainingClasspathEntry(resource);
		assertNull("IClasspathEntry does not exists for the resource", cp);
	}

	protected void assertResourceTreeEquals(String message, String expected, Object[] resources) throws CoreException {
		sortResources(resources);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = resources.length; i < length; i++) {
			printResourceTree(resources[i], buffer, 0);
			if (i != length-1) buffer.append("\n");
		}
		if (!expected.equals(buffer.toString())) {
			System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(buffer.toString(), 2));
			System.out.println(this.endChar);
		}
		assertEquals(
			message,
			expected,
			buffer.toString()
		);
	}

	private void printResourceTree(Object resource, StringBuffer buffer, int indent) throws CoreException {
		for (int i = 0; i < indent; i++)
			buffer.append("  ");
		if (resource instanceof IResource) {
			buffer.append(((IResource) resource).getName());
			if (resource instanceof IContainer) {
				IResource[] children = ((IContainer) resource).members();
				int length = children.length;
				if (length > 0) buffer.append("\n");
				for (int j = 0; j < length; j++) {
					printResourceTree(children[j], buffer, indent+1);
					if (j != length-1) buffer.append("\n");
				}
			}
		} else if (resource instanceof IJarEntryResource) {
			IJarEntryResource jarEntryResource = (IJarEntryResource) resource;
			buffer.append(jarEntryResource.getName());
			if (!jarEntryResource.isFile()) {
				IJarEntryResource[] children = jarEntryResource.getChildren();
				int length = children.length;
				if (length > 0) buffer.append("\n");
				for (int j = 0; j < length; j++) {
					printResourceTree(children[j], buffer, indent+1);
					if (j != length-1) buffer.append("\n");
				}
			}
		} else if (resource == null) {
			buffer.append("<null>");
		}

	}

	protected void assertElementEquals(String message, String expected, IJavaElement element) {
		String actual = element == null ? "<null>" : ((JavaElement) element).toStringWithAncestors(false/*don't show key*/);
		if (!expected.equals(actual)) {
			if (this.displayName) System.out.println(getName()+" actual result is:");
			System.out.println(displayString(actual, this.tabs) + this.endChar);
		}
		assertEquals(message, expected, actual);
	}
	protected void assertElementExists(String message, String expected, IJavaElement element) {
		assertElementEquals(message, expected, element);
		if (element != null && !element.exists()) {
			fail(((JavaElement) element).toStringWithAncestors(false/*don't show key*/) + " doesn't exist");
		}
	}
	protected void assertElementsEqual(String message, String expected, IJavaElement[] elements) {
		assertElementsEqual(message, expected, elements, false/*don't show key*/);
	}
	protected void assertElementsEqual(String message, String expected, IJavaElement[] elements, boolean showResolvedInfo) {
		assertElementsEqual(message, expected, elements, showResolvedInfo, false);
	}
	protected void assertElementsEqual(String message, String expected, IJavaElement[] elements, boolean showResolvedInfo, boolean sorted) {
		StringBuilder buffer = new StringBuilder();
		if (elements != null) {
			for (int i = 0, length = elements.length; i < length; i++){
				JavaElement element = (JavaElement)elements[i];
				if (element == null) {
					buffer.append("<null>");
				} else {
					buffer.append(element.toStringWithAncestors(showResolvedInfo));
				}
				if (i != length-1) buffer.append("\n");
			}
		} else {
			buffer.append("<null>");
		}
		String actual = buffer.toString();
		if (sorted) {
			actual = sortLines(actual);
		}
		if (!expected.equals(actual)) {
			if (this.displayName) System.out.println(getName()+" actual result is:");
			System.out.println(displayString(actual, this.tabs) + this.endChar);
		}
		assertEquals(message, expected, actual);
	}
	protected void assertExceptionEquals(String message, String expected, Exception exception) {
		String actual =
			exception == null ?
				"<null>" :
				(exception instanceof CoreException) ?
					((CoreException) exception).getStatus().getMessage() :
					exception.toString();
		if (!expected.equals(actual)) {
			if (this.displayName) System.out.println(getName()+" actual result is:");
			System.out.println(displayString(actual, this.tabs) + this.endChar);
		}
		assertEquals(message, expected, actual);
	}
	protected void assertHierarchyEquals(String expected, ITypeHierarchy hierarchy) {
		String actual = hierarchy.toString();
		if (!expected.equals(actual)) {
			if (this.displayName) System.out.println(getName()+" actual result is:");
			System.out.println(displayString(actual, this.tabs) + this.endChar);
		}
		assertEquals("Unexpected type hierarchy", expected, actual);
	}
	protected void assertBuildPathMarkers(String message, String expectedMarkers, IJavaProject project) throws CoreException {
		waitForAutoBuild();
		IMarker[] markers = project.getProject().findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
		sortMarkers(markers);
		assertMarkers(message, expectedMarkers, markers);
	}
	protected void sortMarkers(IMarker[] markers) {
		org.eclipse.jdt.internal.core.util.Util.Comparer comparer = new org.eclipse.jdt.internal.core.util.Util.Comparer() {
			public int compare(Object a, Object b) {
				IMarker markerA = (IMarker)a;
				IMarker markerB = (IMarker)b;
				return markerA.getAttribute(IMarker.MESSAGE, "").compareTo(markerB.getAttribute(IMarker.MESSAGE, "")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		org.eclipse.jdt.internal.core.util.Util.sort(markers, comparer);
	}
	protected void assertProblemMarkers(String message, String expectedMarkers, IProject project) throws CoreException {
		IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		sortMarkers(markers);
		assertMarkers(message, expectedMarkers, markers);
	}
	protected void assertMarkers(String message, String expectedMarkers, IMarker[] markers) throws CoreException {
		StringBuilder buffer = new StringBuilder();
		if (markers != null) {
			for (int i = 0, length = markers.length; i < length; i++) {
				IMarker marker = markers[i];
				buffer.append(marker.getAttribute(IMarker.MESSAGE));
				if (i != length-1) {
					buffer.append("\n");
				}
			}
		}
		String actual = buffer.toString();
		if (!expectedMarkers.equals(actual)) {
		 	System.out.println(displayString(actual, 2));
		}
		assertEquals(message, expectedMarkers, actual);
	}

	protected void assertMemberValuePairEquals(String expected, IMemberValuePair member) throws JavaModelException {
		StringBuffer buffer = new StringBuffer();
		appendAnnotationMember(buffer, member);
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2) + this.endChar);
		}
		assertEquals("Unexpected member value pair", expected, actual);
	}

	protected void assertProblems(String message, String expected, IProblem[] problems, char[] source) {
		ProblemRequestor pbRequestor = new ProblemRequestor();
		pbRequestor.unitSource = source;
		for (int i = 0, length = problems.length; i < length; i++) {
			pbRequestor.acceptProblem(problems[i]);
		}
		assertProblems(message, expected, pbRequestor);
	}
	protected void assertProblems(String message, String expected, ProblemRequestor problemRequestor) {
		String actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(problemRequestor.problems.toString());
		String independantExpectedString = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(expected);
		if (!independantExpectedString.equals(actual)){
		 	System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actual, this.tabs));
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
		assertSourceEquals(message, expected, actual, true/*convert line delimiter*/);
	}
	/*
	 * Asserts that the given actual source is equal to the expected one.
	 * Note that if the line separators in 'actual' are converted to '\n' before the comparison,
	 * 'expected' is assumed to have the same '\n' line separator.
	 */
	protected void assertSourceEquals(String message, String expected, String actual, boolean convert) {
		if (actual == null) {
			assertEquals(message, expected, null);
			return;
		}
		if (convert) {
			actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(actual);
		}
		if (!actual.equals(expected)) {
			System.out.println("Expected source in "+getName()+" should be:");
			System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(actual.toString(), 2));
			System.out.println(this.endChar);
		}
		assertEquals(message, expected, actual);
	}
	protected void assertAnnotationsEqual(String expected, IAnnotation[] annotations) throws JavaModelException {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < annotations.length; i++) {
			IAnnotation annotation = annotations[i];
			appendAnnotation(buffer, annotation);
			buffer.append("\n");
		}
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2) + this.endChar);
		}
		assertEquals("Unexpected annotations", expected, actual);
	}

	protected void appendAnnotation(StringBuffer buffer, IAnnotation annotation) throws JavaModelException {
		buffer.append('@');
		buffer.append(annotation.getElementName());
		IMemberValuePair[] members = annotation.getMemberValuePairs();
		int length = members.length;
		if (length > 0) {
			buffer.append('(');
			for (int i = 0; i < length; i++) {
				appendAnnotationMember(buffer, members[i]);
				if (i < length-1)
					buffer.append(", ");
			}
			buffer.append(')');
		}
	}

	private void appendAnnotationMember(StringBuffer buffer, IMemberValuePair member) throws JavaModelException {
		if (member == null) {
			buffer.append("<null>");
			return;
		}
		String name = member.getMemberName();
		if (!"value".equals(name)) {
			buffer.append(name);
			buffer.append('=');
		}
		int kind = member.getValueKind();
		Object value = member.getValue();
		if (value instanceof Object[]) {
			if (kind == IMemberValuePair.K_UNKNOWN)
				buffer.append("[unknown]");
			buffer.append('{');
			Object[] array = (Object[]) value;
			for (int i = 0, length = array.length; i < length; i++) {
				appendAnnotationMemberValue(buffer, array[i], kind);
				if (i < length-1)
					buffer.append(", ");
			}
			buffer.append('}');
		} else {
			appendAnnotationMemberValue(buffer, value, kind);
		}
	}

	private void appendAnnotationMemberValue(StringBuffer buffer, Object value, int kind) throws JavaModelException {
		if (value == null) {
			buffer.append("<null>");
			return;
		}
		switch(kind) {
		case IMemberValuePair.K_INT:
			buffer.append("(int)");
			buffer.append(value);
			break;
		case IMemberValuePair.K_BYTE:
			buffer.append("(byte)");
			buffer.append(value);
			break;
		case IMemberValuePair.K_SHORT:
			buffer.append("(short)");
			buffer.append(value);
			break;
		case IMemberValuePair.K_CHAR:
			buffer.append('\'');
			buffer.append(value);
			buffer.append('\'');
			break;
		case IMemberValuePair.K_FLOAT:
			buffer.append(value);
			buffer.append('f');
			break;
		case IMemberValuePair.K_DOUBLE:
			buffer.append("(double)");
			buffer.append(value);
			break;
		case IMemberValuePair.K_BOOLEAN:
			buffer.append(value);
			break;
		case IMemberValuePair.K_LONG:
			buffer.append(value);
			buffer.append('L');
			break;
		case IMemberValuePair.K_STRING:
			buffer.append('\"');
			buffer.append(value);
			buffer.append('\"');
			break;
		case IMemberValuePair.K_ANNOTATION:
			appendAnnotation(buffer, (IAnnotation) value);
			break;
		case IMemberValuePair.K_CLASS:
			buffer.append(value);
			buffer.append(".class");
			break;
		case IMemberValuePair.K_QUALIFIED_NAME:
			buffer.append(value);
			break;
		case IMemberValuePair.K_SIMPLE_NAME:
			buffer.append(value);
			break;
		case IMemberValuePair.K_UNKNOWN:
			appendAnnotationMemberValue(buffer, value, getValueKind(value));
			break;
		default:
			buffer.append("<Unknown value: (" + (value == null ? "" : value.getClass().getName()) + ") " + value + ">");
			break;
		}
	}
	private int getValueKind(Object value) {
		if (value instanceof Integer) {
			return IMemberValuePair.K_INT;
		} else if (value instanceof Byte) {
			return IMemberValuePair.K_BYTE;
		} else if (value instanceof Short) {
			return IMemberValuePair.K_SHORT;
		} else if (value instanceof Character) {
			return IMemberValuePair.K_CHAR;
		} else if (value instanceof Float) {
			return IMemberValuePair.K_FLOAT;
		} else if (value instanceof Double) {
			return IMemberValuePair.K_DOUBLE;
		} else if (value instanceof Boolean) {
			return IMemberValuePair.K_BOOLEAN;
		} else if (value instanceof Long) {
			return IMemberValuePair.K_LONG;
		} else if (value instanceof String) {
			return IMemberValuePair.K_STRING;
		}
		return -1;

	}
	/*
	 * Ensures that the toString() of the given AST node is as expected.
	 */
	public void assertASTNodeEquals(String message, String expected, ASTNode actual) {
		String actualString = actual == null ? "null" : actual.toString();
		assertSourceEquals(message, expected, actualString);
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
	protected void assertClasspathEquals(IClasspathEntry[] classpath, String expected) {
		String actual;
		if (classpath == null) {
			actual = "<null>";
		} else {
			StringBuilder buffer = new StringBuilder();
			int length = classpath.length;
			for (int i=0; i<length; i++) {
				buffer.append(classpath[i]);
				if (i < length-1)
					buffer.append('\n');
			}
			actual = buffer.toString();
		}
		if (!actual.equals(expected)) {
		 	System.out.print(displayString(actual, 2));
		}
		assertEquals(expected, actual);
	}
	protected void assertPackageFragmentRootsEqual(IPackageFragmentRoot[] roots, String expected) {
		String actual;
		if (roots == null) {
			actual = "<null>";
		} else {
			StringBuilder buffer = new StringBuilder();
			int length = roots.length;
			for (int i=0; i<length; i++) {
				buffer.append(roots[i]);
				if (i < length-1)
					buffer.append('\n');
			}
			actual = buffer.toString();
		}
		if (!actual.equals(expected)) {
		 	System.out.print(displayString(actual, 2));
		}
		assertEquals(expected, actual);
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
	protected void assertDeltas(String message, String expected, DeltaListener listener) {
		assertDeltas(message, expected, expected.length() > 0/*wait for resource delta iff a delta is expected*/, listener);
	}
	protected void assertDeltas(String message, String expected, boolean waitForResourceDelta, DeltaListener listener) {
		if (waitForResourceDelta)
			listener.waitForResourceDelta();
		String actual = listener.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2));
			System.err.println(listener.stackTraces());
		}
		assertEquals(
			message,
			expected,
			actual);
	}
	protected void assertDeltas(String message, String expected) {
		assertDeltas(message, expected, expected.length() > 0/*wait for resource delta iff a delta is expected*/);
	}
	protected void assertDeltas(String message, String expected, boolean waitForResourceDelta) {
		if (waitForResourceDelta)
			this.deltaListener.waitForResourceDelta();
		String actual = this.deltaListener.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2));
			System.err.println(this.deltaListener.stackTraces());
		}
		assertEquals(
			message,
			expected,
			actual);
	}
	protected void assertDeltasSortingModules(String message, String expected, boolean waitForResourceDelta) {
		if (waitForResourceDelta)
			this.deltaListener.waitForResourceDelta();
		String actual = this.deltaListener.toString();
		actual = sortModules(actual);
		expected = sortModules(expected);
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2));
			System.err.println(this.deltaListener.stackTraces());
		}
		assertEquals(
			message,
			expected,
			actual);
	}
	private String sortModules(String text) {
		StringBuilder buf = new StringBuilder();
		String[] lines = text.split("\n");
		int idx = 0;

		// prefix before first module:
		while (idx < lines.length) {
			String line = lines[idx];
			if (!line.trim().startsWith("<module:")) {
				buf.append(line).append('\n');
				idx++;
			} else {
				break;
			}
		}

		// extract & sort modules:
		String[] modules = new String[lines.length-idx];
		int m = 0;
		while (idx < lines.length) {
			String line = lines[idx];
			if (line.trim().startsWith("<module:")) {
				modules[m++] = line;
				idx++;
			} else {
				break;
			}
		}
		if (m > 0) {
			if (m < modules.length)
				modules = Arrays.copyOf(modules, m);
			Arrays.sort(modules);
			for (String module : modules) {
				buf.append(module).append('\n');
			}

			// suffix:
			while (idx < lines.length) {
				buf.append(lines[idx++]).append('\n');
			}
		}
		return buf.toString();
	}

	protected void assertDeltas(String message, String expected, IJavaElementDelta delta) {
		String actual = delta == null ? "<null>" : delta.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2));
			System.err.println(this.deltaListener.stackTraces());
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
		if (sort) sortTypes(types);
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < types.length; i++){
			if (types[i] == null)
				buffer.append("<null>");
			else
				buffer.append(types[i].getFullyQualifiedName());
			buffer.append("\n");
		}
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 2) +  this.endChar);
		}
		assertEquals(message, expected, actual);
	}
	protected void assertTypeParametersEqual(String expected, ITypeParameter[] typeParameters) throws JavaModelException {
		StringBuilder buffer = new StringBuilder();
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
			System.out.println(displayString(actual, 3) + this.endChar);
		}
		assertEquals("Unexpected type parameters", expected, actual);
	}
	protected void assertSortedStringsEqual(String message, String expected, String[] strings) {
		Util.sort(strings);
		assertStringsEqual(message, expected, strings);
	}
	protected void assertStringsEqual(String message, String expected, String[] strings) {
		String actual = org.eclipse.jdt.core.tests.util.Util.toString(strings, true/*add extra new lines*/);
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, this.tabs) + this.endChar);
		}
		assertEquals(message, expected, actual);
	}
	protected void assertStringsEqual(String message, String[] expectedStrings, String[] actualStrings) {
		String expected = org.eclipse.jdt.core.tests.util.Util.toString(expectedStrings, false/*don't add extra new lines*/);
		String actual = org.eclipse.jdt.core.tests.util.Util.toString(actualStrings, false/*don't add extra new lines*/);
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, this.tabs) + this.endChar);
		}
		assertEquals(message, expected, actual);
	}
	/**
	 * Attaches a source zip to the given jar package fragment root.
	 */
	protected void attachSource(IPackageFragmentRoot root, String sourcePath, String sourceRoot) throws JavaModelException {
		IJavaProject javaProject = root.getJavaProject();
		IClasspathEntry[] entries = javaProject.getRawClasspath().clone();
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
	 * the operation is successful, and ensures the element is no
	 * longer present in the model.
	 */
	public void assertDeletion(IJavaElement elementToDelete) throws JavaModelException {
		assertDeletion(new IJavaElement[] {elementToDelete});
	}
	/**
	 * Empties the current deltas.
	 */
	public void clearDeltas(DeltaListener listener) {
		listener.flush();
	}
	public void clearDeltas() {
		this.deltaListener.flush();
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
		byte[] srcBytes = read(src);

		if (convertToIndependantLineDelimiter(src)) {
			String contents = new String(srcBytes);
			contents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(contents);
			srcBytes = contents.getBytes();
		}

		// write bytes to dest
		FileOutputStream out = new FileOutputStream(dest);
		try {
			out.write(srcBytes);
		} finally {
			out.close();
		}
	}

	public boolean convertToIndependantLineDelimiter(File file) {
		return file.getName().endsWith(".java");
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
			if (name.equals("CVS") || name.equals(".svn")) continue;
			File targetChild = new File(target, name);
			if (sourceChild.isDirectory()) {
				copyDirectory(sourceChild, targetChild);
			} else {
				copy(sourceChild, targetChild);
			}
		}
	}
	protected IFile createFile(String path, InputStream content) throws CoreException {
		IFile file = getFile(path);
		file.create(content, true, null);
		try {
			content.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	protected IFile createFile(String path, byte[] content) throws CoreException {
		return createFile(path, new ByteArrayInputStream(content));
	}

	protected IFile createFile(String path, String content) throws CoreException {
		return createFile(path, content.getBytes());
	}
	protected IFolder createFolder(String path) throws CoreException {
		return createFolder(new Path(path));
	}
	protected IFolder createFolder(IPath path) throws CoreException {
		final IFolder folder = getWorkspaceRoot().getFolder(path);
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IContainer parent = folder.getParent();
				if (parent instanceof IFolder && !parent.exists()) {
					createFolder(parent.getFullPath());
				}
				if(!folder.exists()) {
					folder.create(true, true, null);
				}
			}
		},
		null);

		return folder;
	}
	protected void createJar(String[] javaPathsAndContents, String jarPath) throws IOException {
		org.eclipse.jdt.core.tests.util.Util.createJar(javaPathsAndContents, jarPath, "1.4");
	}

	protected void createJar(String[] javaPathsAndContents, String jarPath, Map options) throws IOException {
		org.eclipse.jdt.core.tests.util.Util.createJar(javaPathsAndContents, null, jarPath, null, "1.4", options);
	}

	protected void createJar(String[] javaPathsAndContents, String jarPath, String[] classpath, String compliance) throws IOException {
		org.eclipse.jdt.core.tests.util.Util.createJar(javaPathsAndContents, null,jarPath, classpath, compliance);
	}

	protected void createJar(String[] javaPathsAndContents, String jarPath, String[] classpath, String compliance, Map options) throws IOException {
		org.eclipse.jdt.core.tests.util.Util.createJar(javaPathsAndContents, null, jarPath, classpath, compliance, options);
	}

	protected IJavaProject createJava9Project(String name) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, new String[]{"src"}, null, "9");
	}
	protected IJavaProject createJava9Project(String name, String compliance) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, new String[]{"src"}, null, compliance);
	}
	protected IJavaProject createJava9Project(String name, String[] srcFolders) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, srcFolders, null, "9");
	}
	protected IJavaProject createJava10Project(String name, String[] srcFolders) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, srcFolders, null, "10");
	}
	protected IJavaProject createJava11Project(String name, String[] srcFolders) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, srcFolders, null, "11");
	}
	protected IJavaProject createJava14Project(String name, String[] srcFolders) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, srcFolders, null, "14");
	}
	protected IJavaProject createJava15Project(String name, String[] srcFolders) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, srcFolders, null, "15");
	}
	protected IJavaProject createJava16Project(String name, String[] srcFolders) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, srcFolders, null, "16");
	}
	protected IJavaProject createJava16Project(String name) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, new String[]{"src"}, null, "16");
	}
	protected IJavaProject createJava9ProjectWithJREAttributes(String name, String[] srcFolders, IClasspathAttribute[] attributes) throws CoreException {
		return createJava9ProjectWithJREAttributes(name, srcFolders, attributes, "9");
	}
	protected IJavaProject createJava9ProjectWithJREAttributes(String name, String[] srcFolders, IClasspathAttribute[] attributes, String compliance) throws CoreException {
		String javaHome = System.getProperty("java.home") + File.separator;
		Path bootModPath = new Path(javaHome +"/lib/jrt-fs.jar");
		Path sourceAttachment = new Path(javaHome +"/lib/src.zip");
		IClasspathEntry jrtEntry;
		String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$
		if (javaVersion != null && javaVersion.startsWith("1.8")) { //$NON-NLS-1$
			// fall back to a regular JCL to provide access via the unnamed module:
			jrtEntry = JavaCore.newVariableEntry(new Path("JCL18_LIB"), sourceAttachment, null);
			try {
				setUpJCLClasspathVariables("1.8");
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, e.getMessage(), e));
			}
		} else {
			if (attributes == null)
				attributes = new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			jrtEntry = JavaCore.newLibraryEntry(bootModPath, sourceAttachment, null, null, attributes, false);
		}
		IJavaProject project = this.createJavaProject(name, srcFolders, new String[0],
				new String[0], "bin", compliance);
		IClasspathEntry[] old = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[old.length +1];
		System.arraycopy(old, 0, newPath, 0, old.length);
		newPath[old.length] = jrtEntry;
		project.setRawClasspath(newPath, null);
		return project;
	}
	protected IClasspathEntry getJRTLibraryEntry() {
		if (!isJRE9) return null;
		String javaHome = System.getProperty("java.home") + File.separator;
		Path bootModPath = new Path(javaHome +"/lib/jrt-fs.jar");
		Path sourceAttachment = new Path(javaHome +"/lib/src.zip");
		return JavaCore.newLibraryEntry(bootModPath, sourceAttachment, null, null, null, false);
	}
	/*
	}
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
				""
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
				""
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
				true/*combine access restrictions by default*/,
				null/*no exported project*/,
				output,
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				"",
				false/*don't import*/
			);
	}
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String[] libraries, String output, String compliance, boolean useFullJCL) throws CoreException {
		return
				this.createJavaProject(
					projectName,
					null,
					sourceFolders,
					libraries,
					null/*no inclusion pattern*/,
					null/*no exclusion pattern*/,
					null/*no project*/,
					null/*no inclusion pattern*/,
					null/*no exclusion pattern*/,
					true,
					null/*no exported project*/,
					output,
					null/*no source outputs*/,
					null/*no inclusion pattern*/,
					null/*no exclusion pattern*/,
					compliance,
					useFullJCL,
					false
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
				""
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
				""
			);
	}
	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String[] libraries, String[] projects, String projectOutput, String compliance) throws CoreException {
		return
			createJavaProject(
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
				compliance
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
		return createJavaProject(
			projectName,
			sourceFolders,
			libraries,
			librariesInclusionPatterns,
			librariesExclusionPatterns,
			projects,
			projectsInclusionPatterns,
			projectsExclusionPatterns,
			true, // combine access restrictions by default
			exportedProjects,
			projectOutput,
			sourceOutputs,
			inclusionPatterns,
			exclusionPatterns,
			compliance,
			false/*don't import*/);
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
			final boolean combineAccessRestrictions,
			final boolean[] exportedProjects,
			final String projectOutput,
			final String[] sourceOutputs,
			final String[][] inclusionPatterns,
			final String[][] exclusionPatterns,
			final String compliance,
			final boolean simulateImport) throws CoreException {
		return createJavaProject(
				projectName,
				null,
				sourceFolders,
				libraries,
				librariesInclusionPatterns,
				librariesExclusionPatterns,
				projects,
				projectsInclusionPatterns,
				projectsExclusionPatterns,
				combineAccessRestrictions,
				exportedProjects,
				projectOutput,
				sourceOutputs,
				inclusionPatterns,
				exclusionPatterns,
				compliance,
				false,
				simulateImport);
	}
	protected IJavaProject createJavaProject(
			final String projectName,
			URI locationURI,
			final String[] sourceFolders,
			final String[] libraries,
			final String[][] librariesInclusionPatterns,
			final String[][] librariesExclusionPatterns,
			final String[] projects,
			final String[][] projectsInclusionPatterns,
			final String[][] projectsExclusionPatterns,
			final boolean combineAccessRestrictions,
			final boolean[] exportedProjects,
			final String projectOutput,
			final String[] sourceOutputs,
			final String[][] inclusionPatterns,
			final String[][] exclusionPatterns,
			final String compliance,
			final boolean fullJCL,
			final boolean simulateImport) throws CoreException {
		final IJavaProject[] result = new IJavaProject[1];
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				// Always delete first, in case there is a leftover from some broken test
				deleteProject(projectName);

				// create project
				if (locationURI != null)
					createExternalProject(projectName, locationURI);
				else
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
							setUpJCLClasspathVariables(compliance, fullJCL);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					// accessible files
					IPath[] accessibleFiles;
					if (librariesInclusionPatterns == null) {
						accessibleFiles = new IPath[0];
					} else {
						String[] patterns = librariesInclusionPatterns[i];
						int length = patterns.length;
						accessibleFiles = new IPath[length];
						for (int j = 0; j < length; j++) {
							String inclusionPattern = patterns[j];
							accessibleFiles[j] = new Path(inclusionPattern);
						}
					}
					// non accessible files
					IPath[] nonAccessibleFiles;
					if (librariesExclusionPatterns == null) {
						nonAccessibleFiles = new IPath[0];
					} else {
						String[] patterns = librariesExclusionPatterns[i];
						int length = patterns.length;
						nonAccessibleFiles = new IPath[length];
						for (int j = 0; j < length; j++) {
							String exclusionPattern = patterns[j];
							nonAccessibleFiles[j] = new Path(exclusionPattern);
						}
					}
					if (lib.indexOf(File.separatorChar) == -1 && lib.charAt(0) != '/' && lib.equals(lib.toUpperCase())) { // all upper case is a var
						char[][] vars = CharOperation.splitOn(',', lib.toCharArray());
						IClasspathAttribute[] extraAttributes = ClasspathEntry.NO_EXTRA_ATTRIBUTES;
						if (CompilerOptions.versionToJdkLevel(compliance) >= ClassFileConstants.JDK9
								&& (lib.startsWith("JCL") || lib.startsWith("CONVERTER_JCL"))) {
							extraAttributes = new IClasspathAttribute[] {
								JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")
							};
						}
						entries[sourceLength+i] = JavaCore.newVariableEntry(
							new Path(new String(vars[0])),
							vars.length > 1 ? new Path(new String(vars[1])) : null,
							vars.length > 2 ? new Path(new String(vars[2])) : null,
							ClasspathEntry.getAccessRules(accessibleFiles, nonAccessibleFiles), // ClasspathEntry.NO_ACCESS_RULES,
							extraAttributes,
							false);
					} else if (lib.startsWith("org.eclipse.jdt.core.tests.model.")) { // container
						entries[sourceLength+i] = JavaCore.newContainerEntry(
								new Path(lib),
								ClasspathEntry.getAccessRules(accessibleFiles, nonAccessibleFiles),
								new IClasspathAttribute[0],
								false);
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
								ClasspathEntry.getAccessRules(accessibleFiles, nonAccessibleFiles),
								new IClasspathAttribute[0],
								false);
					}
				}
				for  (int i= 0; i < projectLength; i++) {
					boolean isExported = exportedProjects != null && exportedProjects.length > i && exportedProjects[i];

					// accessible files
					IPath[] accessibleFiles;
					if (projectsInclusionPatterns == null) {
						accessibleFiles = new IPath[0];
					} else {
						String[] patterns = projectsInclusionPatterns[i];
						int length = patterns.length;
						accessibleFiles = new IPath[length];
						for (int j = 0; j < length; j++) {
							String inclusionPattern = patterns[j];
							accessibleFiles[j] = new Path(inclusionPattern);
						}
					}
					// non accessible files
					IPath[] nonAccessibleFiles;
					if (projectsExclusionPatterns == null) {
						nonAccessibleFiles = new IPath[0];
					} else {
						String[] patterns = projectsExclusionPatterns[i];
						int length = patterns.length;
						nonAccessibleFiles = new IPath[length];
						for (int j = 0; j < length; j++) {
							String exclusionPattern = patterns[j];
							nonAccessibleFiles[j] = new Path(exclusionPattern);
						}
					}

					entries[sourceLength+libLength+i] =
						JavaCore.newProjectEntry(
								new Path(projects[i]),
								ClasspathEntry.getAccessRules(accessibleFiles, nonAccessibleFiles),
								combineAccessRestrictions,
								new IClasspathAttribute[0],
								isExported);
				}

				// create project's output folder
				IPath outputPath = new Path(projectOutput);
				if (outputPath.segmentCount() > 0) {
					IFolder output = project.getFolder(outputPath);
					if (!output.exists()) {
						output.create(true, true, monitor);
					}
				}

				// set classpath and output location
				JavaProject javaProject = (JavaProject) JavaCore.create(project);
				if (simulateImport)
					javaProject.writeFileEntries(entries, projectPath.append(outputPath));
				else
					javaProject.setRawClasspath(entries, projectPath.append(outputPath), monitor);

				// set compliance level options
				if ("1.4".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
					javaProject.setOptions(options);
				} else if ("1.5".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
					javaProject.setOptions(options);
				} else if ("1.6".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
					javaProject.setOptions(options);
				} else if ("1.7".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
					javaProject.setOptions(options);
				} else if ("1.8".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
					javaProject.setOptions(options);
				} else if ("9".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_9);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_9);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_9);
					javaProject.setOptions(options);
				} else if ("10".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_10);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_10);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_10);
					javaProject.setOptions(options);
				} else if ("11".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);
					javaProject.setOptions(options);
				} else if ("12".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_12);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_12);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_12);
					javaProject.setOptions(options);
				} else if ("13".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_13);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_13);
					javaProject.setOptions(options);
				} else if ("14".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_14);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_14);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_14);
					javaProject.setOptions(options);
				} else if ("15".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
					javaProject.setOptions(options);
				} else if ("16".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
					javaProject.setOptions(options);
				} else if ("17".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
					javaProject.setOptions(options);
				} else if ("21".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_21);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_21);
					javaProject.setOptions(options);
				}
				result[0] = javaProject;
			}
		};
		getWorkspace().run(create, null);
		return result[0];
	}
	protected IJavaProject importJavaProject(String projectName, String[] sourceFolders, String[] libraries, String output) throws CoreException {
		return
			createJavaProject(
				projectName,
				sourceFolders,
				libraries,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no project*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				true/*combine access restrictions by default*/,
				null/*no exported project*/,
				output,
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				"1.4",
				true/*import*/
			);
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
	protected IProject createExternalProject(final String projectName, URI location) throws CoreException {
		final IProject project = getProject(projectName);
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(projectDescriptionForLocation(projectName, location), null);
				project.open(null);
			}
		};
		getWorkspace().run(create, null);
		return project;
	}
	public void createSourceZip(String[] pathsAndContents, String zipPath) throws IOException {
		org.eclipse.jdt.core.tests.util.Util.createSourceZip(pathsAndContents, zipPath);
	}
	public void deleteResource(File resource) {
		int retryCount = 0;
		while (++retryCount <= 60) { // wait 1 minute at most
			if (org.eclipse.jdt.core.tests.util.Util.delete(resource)) {
				break;
			}
		}
	}
	protected void deleteFolder(IPath folderPath) throws CoreException {
		deleteResource(getFolder(folderPath));
	}
	protected void deleteProject(String projectName) throws CoreException {
		IProject project = getProject(projectName);
		if (project.exists() && !project.isOpen()) { // force opening so that project can be deleted without logging (see bug 23629)
			project.open(null);
		}
		deleteResource(project);
	}
	protected void deleteProject(IJavaProject project) throws CoreException {
		if (project.exists() && !project.isOpen()) { // force opening so that project can be deleted without logging (see bug 23629)
			project.open(null);
		}
		deleteResource(project.getProject());
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
		int retryCount = 0; // wait 1 minute at most
		IStatus status = null;
		while (++retryCount <= 6) {
			status = org.eclipse.jdt.core.tests.util.Util.delete(resource);
			if (status.isOK()) {
				return;
			}
			System.gc();
		}
		throw new CoreException(status);
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
	 * Ensure given child exists in the parent
	 */
	public void ensureChildExists(IParent container, IJavaElement child) throws JavaModelException {
		IJavaElement[] children = container.getChildren();
		if (child != null) {
			// find the sibling
			boolean found = false;
			for (IJavaElement child2 : children) {
				if (child2.equals(child)) {
					found = true;
					break;
				}
			}
			assertTrue("Did not find child: " + child + " in parent container: " + container, found);
		}
	}

	protected String[] getJCL15PlusLibraryIfNeeded(String compliance) throws JavaModelException, IOException {
		if (compliance.charAt(compliance.length()-1) >= '8' && (AbstractCompilerTest.getPossibleComplianceLevels() & AbstractCompilerTest.F_1_8) != 0) {
			// ensure that the JCL 18 lib is setup (i.e. that the jclMin18.jar is copied)
			setUpJCLClasspathVariables("1.8");
			return new String[] {getExternalJCLPathString("1.8")};
		}
		if (compliance.charAt(compliance.length()-1) >= '5' && (AbstractCompilerTest.getPossibleComplianceLevels() & AbstractCompilerTest.F_1_5) != 0) {
			// ensure that the JCL 15 lib is setup (i.e. that the jclMin15.jar is copied)
			setUpJCLClasspathVariables("1.5");
			return new String[] {getExternalJCLPathString("1.5")};
		}
		return null;
	}
	/**
	 * Returns the specified compilation unit in the given project, root, and
	 * package fragment or <code>null</code> if it does not exist.
	 */
	public IOrdinaryClassFile getClassFile(String projectName, String rootPath, String packageName, String className) throws JavaModelException {
		IPackageFragment pkg= getPackageFragment(projectName, rootPath, packageName);
		if (pkg == null) {
			return null;
		}
		return pkg.getOrdinaryClassFile(className);
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
	protected File getExternalFile(String relativePath) {
		return new File(getExternalPath(), relativePath);
	}

	protected String getExternalResourcePath(String relativePath) {
		return getExternalPath() + relativePath;
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
		return getExternalPath() + "jclMin" + compliance + ".jar";
	}
	protected String getExternalJCLPathString(String compliance, boolean useFullJCL) {
		if (useFullJCL) {
			return getExternalPath() + "jclFull" + compliance + ".jar";
		} else {
			return getExternalJCLPathString(compliance);
		}
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
		return getExternalPath() + "jclMin" + compliance + "src.zip";
	}
	/*
	 * Returns the OS path to the external directory that contains external jar files.
	 * This path ends with a File.separatorChar.
	 */
	protected String getExternalPath() {
		if (EXTERNAL_JAR_DIR_PATH == null)
			try {
				String path = getWorkspaceRoot().getLocation().toFile().getParentFile().getCanonicalPath();
				if (path.charAt(path.length()-1) != File.separatorChar)
					path += File.separatorChar;
				EXTERNAL_JAR_DIR_PATH = path;
			} catch (IOException e) {
				e.printStackTrace();
			}
		return EXTERNAL_JAR_DIR_PATH;
	}
	/*
	 * Returns the OS path to the workspace directory.
	 * This path ends with a File.separatorChar.
	 */
	protected String getWorkspacePath() {
		if (WORKSPACE_DIR_PATH == null)
			try {
				String path = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
				if (path.charAt(path.length()-1) != File.separatorChar)
					path += File.separatorChar;
				WORKSPACE_DIR_PATH = path;
			} catch (IOException e) {
				e.printStackTrace();
			}
		return WORKSPACE_DIR_PATH;
	}
	protected IFile getFile(String path) {
		return getWorkspaceRoot().getFile(new Path(path));
	}
	protected IFolder getFolder(IPath path) {
		return getWorkspaceRoot().getFolder(path);
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
	protected ILocalVariable getLocalVariable(ISourceReference cu, String selectAt, String selection) throws JavaModelException {
		IJavaElement[] elements = codeSelect(cu, selectAt, selection);
		if (elements.length == 0) return null;
		if (elements[0] instanceof ILocalVariable) {
			return (ILocalVariable)elements[0];
		}
		return null;
	}
	protected ILocalVariable getLocalVariable(String cuPath, String selectAt, String selection) throws JavaModelException {
		ISourceReference cu = getCompilationUnit(cuPath);
		return getLocalVariable(cu, selectAt, selection);
	}
	protected String getNameSource(String cuSource, IJavaElement element) throws JavaModelException {
		ISourceRange nameRange;
		switch (element.getElementType()) {
			case IJavaElement.TYPE_PARAMETER:
				nameRange = ((ITypeParameter) element).getNameRange();
				break;
			case IJavaElement.ANNOTATION:
				nameRange = ((IAnnotation) element).getNameRange();
				break;
			case IJavaElement.PACKAGE_DECLARATION :
				nameRange = ((IPackageDeclaration) element).getNameRange();
				break;
			case IJavaElement.IMPORT_DECLARATION :
				nameRange = ((IImportDeclaration) element).getNameRange();
				break;
			default:
				nameRange = ((IMember) element).getNameRange();
				break;
		}
		return getSource(cuSource, nameRange);
	}
	protected String getSource(String cuSource, ISourceRange sourceRange) throws JavaModelException {
		int start = sourceRange.getOffset();
		int end = start+sourceRange.getLength();
		String actualSource = start >= 0 && end >= start ? cuSource.substring(start, end) : "";
		return actualSource;
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
	 * folder itself.
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
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
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
		return getWorkingCopy(path, source, false);
	}
	public ICompilationUnit getWorkingCopy(String path, String source, boolean computeProblems) throws JavaModelException {
		if (this.wcOwner == null) {
			this.wcOwner = newWorkingCopyOwner(computeProblems ? new BasicProblemRequestor() : null);
			return getWorkingCopy(path, source, this.wcOwner);
		}
		ICompilationUnit wc = getWorkingCopy(path, source, this.wcOwner);
		// Verify that compute problem parameter is compatible with working copy problem requestor
		if (computeProblems) {
			assertNotNull("Cannot compute problems if the problem requestor of the working copy owner is set to null!", this.wcOwner.getProblemRequestor(wc));
		} else {
			assertNull("Cannot ignore problems if the problem requestor of the working copy owner is not set to null!", this.wcOwner.getProblemRequestor(wc));
		}
		return wc;
	}
	public ICompilationUnit getWorkingCopy(String path, String source, WorkingCopyOwner owner) throws JavaModelException {
		ICompilationUnit workingCopy = getCompilationUnit(path);
		if (owner != null)
			workingCopy = workingCopy.getWorkingCopy(owner, null/*no progress monitor*/);
		else
			workingCopy.becomeWorkingCopy(null/*no progress monitor*/);
		workingCopy.getBuffer().setContents(source);
		if (owner != null) {
			IProblemRequestor problemRequestor = owner.getProblemRequestor(workingCopy);
			if (problemRequestor instanceof ProblemRequestor) {
				((ProblemRequestor) problemRequestor).initialize(source.toCharArray());
			}
		}
		workingCopy.makeConsistent(null/*no progress monitor*/);
		return workingCopy;
	}
	/**
	 * This method is still necessary when we need to use an owner and a specific problem requestor
	 * (typically while using primary owner).
	 * @deprecated
	 */
	public ICompilationUnit getWorkingCopy(String path, String source, WorkingCopyOwner owner, IProblemRequestor problemRequestor) throws JavaModelException {
		ICompilationUnit workingCopy = getCompilationUnit(path);
		if (owner != null)
			workingCopy = workingCopy.getWorkingCopy(owner, problemRequestor, null/*no progress monitor*/);
		else
			workingCopy.becomeWorkingCopy(problemRequestor, null/*no progress monitor*/);
		workingCopy.getBuffer().setContents(source);
		if (problemRequestor instanceof ProblemRequestor)
			((ProblemRequestor) problemRequestor).initialize(source.toCharArray());
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
	protected void discardWorkingCopies(ICompilationUnit[] units) throws JavaModelException {
		if (units == null) return;
		for (int i = 0, length = units.length; i < length; i++)
			if (units[i] != null)
				units[i].discardWorkingCopy();
	}

	protected String displayString(String toPrint, int indent) {
    	char[] toDisplay = toPrint.toCharArray();
    	toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			getWorkspacePath().toCharArray(),
    			"getWorkspacePath()".toCharArray());
    	toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			getExternalJCLPathString().toCharArray(),
    			"getExternalJCLPathString()".toCharArray());
		toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			getExternalJCLPathString("1.5").toCharArray(),
    			"getExternalJCLPathString(\"1.5\")".toCharArray());
		toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			getExternalPath().toCharArray(),
    			"getExternalPath()".toCharArray());

		toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			org.eclipse.jdt.core.tests.util.Util.displayString(getExternalJCLSourcePathString(), 0).toCharArray(),
    			"getExternalJCLSourcePathString()".toCharArray());
		toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			org.eclipse.jdt.core.tests.util.Util.displayString(getExternalJCLSourcePathString("1.5"), 0).toCharArray(),
    			"getExternalJCLSourcePathString(\"1.5\")".toCharArray());

    	toDisplay = org.eclipse.jdt.core.tests.util.Util.displayString(new String(toDisplay), indent).toCharArray();

    	toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			"getWorkspacePath()".toCharArray(),
    			("\"+ getWorkspacePath() + \"").toCharArray());
    	toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			"getExternalJCLPathString()".toCharArray(),
    			("\"+ getExternalJCLPathString() + \"").toCharArray());
    	toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			"getExternalJCLPathString(\\\"1.5\\\")".toCharArray(),
    			("\"+ getExternalJCLPathString(\"1.5\") + \"").toCharArray());
    	toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			"getExternalJCLSourcePathString()".toCharArray(),
    			("\"+ getExternalJCLSourcePathString() + \"").toCharArray());
    	toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			"getExternalJCLSourcePathString(\\\"1.5\\\")".toCharArray(),
    			("\"+ getExternalJCLSourcePathString(\"1.5\") + \"").toCharArray());
    	toDisplay =
    		CharOperation.replace(
    			toDisplay,
    			"getExternalPath()".toCharArray(),
    			("\"+ getExternalPath() + \"").toCharArray());
    	return new String(toDisplay);
    }

	protected ICompilationUnit newExternalWorkingCopy(String name, final String contents) throws JavaModelException {
		return newExternalWorkingCopy(name, null/*no classpath*/, null/*no problem requestor*/, contents);
	}
	protected ICompilationUnit newExternalWorkingCopy(String name, IClasspathEntry[] classpath, final IProblemRequestor problemRequestor, final String contents) throws JavaModelException {
		WorkingCopyOwner owner = new WorkingCopyOwner() {
			public IBuffer createBuffer(ICompilationUnit wc) {
				IBuffer buffer = super.createBuffer(wc);
				buffer.setContents(contents);
				return buffer;
			}
			public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
				return problemRequestor;
			}
		};
		return owner.newWorkingCopy(name, classpath, null/*no progress monitor*/);
	}

	/**
	 * Create a new working copy owner using given problem requestor
	 * to report problem.
	 *
	 * @param problemRequestor The requestor used to report problems
	 * @return The created working copy owner
	 */
	protected WorkingCopyOwner newWorkingCopyOwner(final IProblemRequestor problemRequestor) {
		return new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return problemRequestor;
			}
		};
	}

	public byte[] read(java.io.File file) throws java.io.IOException {
		int fileLength;
		byte[] fileBytes = new byte[fileLength = (int) file.length()];
		java.io.FileInputStream stream = new java.io.FileInputStream(file);
		int bytesRead = 0;
		int lastReadSize = 0;
		try {
			while ((lastReadSize != -1) && (bytesRead != fileLength)) {
				lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
				bytesRead += lastReadSize;
			}
			return fileBytes;
		} finally {
			stream.close();
		}
	}

	public void refresh(final IJavaProject javaProject) throws CoreException {
		javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		waitForManualRefresh();
	}

	protected void refreshExternalArchives(IJavaProject p) throws JavaModelException {
		waitForAutoBuild(); // ensure that the auto-build job doesn't interfere with external jar refreshing
		getJavaModel().refreshExternalArchives(new IJavaElement[] {p}, null);
		JavaModelManager.getIndexManager().waitForIndex(isIndexDisabledForTest(), null);
	}

	protected void removeJavaNature(String projectName) throws CoreException {
		IProject project = getProject(projectName);
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] {});
		project.setDescription(description, null);
	}
	protected void removeLibrary(IJavaProject javaProject, String jarName, String sourceZipName) throws CoreException, IOException {
		IProject project = javaProject.getProject();
		String projectPath = '/' + project.getName() + '/';
		removeClasspathEntry(javaProject, new Path(projectPath + jarName));
		org.eclipse.jdt.core.tests.util.Util.delete(project.getFile(jarName));
		if (sourceZipName != null && sourceZipName.length() != 0) {
			org.eclipse.jdt.core.tests.util.Util.delete(project.getFile(sourceZipName));
		}
	}
	protected void removeClasspathEntry(IPath path) throws JavaModelException {
		removeClasspathEntry(this.currentProject, path);
	}
	protected void removeClasspathEntry(IJavaProject project, IPath path) throws JavaModelException {
		IClasspathEntry[] entries = project.getRawClasspath();
		int length = entries.length;
		IClasspathEntry[] newEntries = null;
		for (int i = 0; i < length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getPath().equals(path)) {
				newEntries = new IClasspathEntry[length-1];
				if (i > 0)
					System.arraycopy(entries, 0, newEntries, 0, i);
				if (i < length-1)
				System.arraycopy(entries, i+1, newEntries, i, length-1-i);
				break;
			}
		}
		if (newEntries != null)
			project.setRawClasspath(newEntries, null);
	}

	protected void search(IJavaElement element, int limitTo, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		search(element, limitTo, SearchPattern.R_EXACT_MATCH|SearchPattern.R_CASE_SENSITIVE, scope, requestor);
	}
	protected void search(IJavaElement element, int limitTo, int matchRule, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		boolean indexDisabled = isIndexDisabledForTest();
		if(indexDisabled) {
			enableIndexer();
		}
		try {
			SearchPattern pattern = SearchPattern.createPattern(element, limitTo, matchRule);
			assertNotNull("Pattern should not be null", pattern);
			new SearchEngine().search(
				pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				scope,
				requestor,
				null
			);
		} finally {
			if(indexDisabled) {
				disableIndexer();
			}
		}
	}
	protected void search(String patternString, int searchFor, int limitTo, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		search(patternString, searchFor, limitTo, SearchPattern.R_EXACT_MATCH|SearchPattern.R_CASE_SENSITIVE, scope, requestor);
	}
	protected void search(String patternString, int searchFor, int limitTo, int matchRule, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		boolean indexDisabled = isIndexDisabledForTest();
		if(indexDisabled) {
			enableIndexer();
		}
		try {
		if (patternString.indexOf('*') != -1 || patternString.indexOf('?') != -1)
			matchRule |= SearchPattern.R_PATTERN_MATCH;
		SearchPattern pattern = SearchPattern.createPattern(
			patternString,
			searchFor,
			limitTo,
			matchRule);
		assertNotNull("Pattern should not be null", pattern);
		new SearchEngine().search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null);
		} finally {
			if(indexDisabled) {
				disableIndexer();
			}
		}
	}

	/*
	 * Selection of java elements.
	 */

	/*
	 * Search several occurences of a selection in a compilation unit source and returns its start and length.
	 * If occurence is negative, then perform a backward search from the end of file.
	 * If selection starts or ends with a comment (to help identification in source), it is removed from returned selection info.
	 */
	int[] selectionInfo(ICompilationUnit cu, String selection, int occurences) throws JavaModelException {
		String source = cu.getSource();
		int index = occurences < 0 ? source.lastIndexOf(selection) : source.indexOf(selection);
		int max = Math.abs(occurences)-1;
		for (int n=0; index >= 0 && n<max; n++) {
			index = occurences < 0 ? source.lastIndexOf(selection, index) : source.indexOf(selection, index+selection.length());
		}
		StringBuilder msg = new StringBuilder("Selection '");
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
	 * Select a field in a compilation unit identified with the first occurence in the source of a given selection.
	 * @return IField
	 */
	protected IField selectField(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectField(unit, selection, 1);
	}

	/**
	 * Select a field in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @return IField
	 */
	protected IField selectField(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		return (IField) selectJavaElement(unit, selection, occurences, IJavaElement.FIELD);
	}

	/**
	 * Select a local variable in a compilation unit identified with the first occurence in the source of a given selection.
	 * @return IType
	 */
	protected ILocalVariable selectLocalVariable(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectLocalVariable(unit, selection, 1);
	}

	/**
	 * Select a local variable in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @return IType
	 */
	protected ILocalVariable selectLocalVariable(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		return (ILocalVariable) selectJavaElement(unit, selection, occurences, IJavaElement.LOCAL_VARIABLE);
	}

	/**
	 * Select a method in a compilation unit identified with the first occurence in the source of a given selection.
	 * @return IMethod
	 */
	protected IMethod selectMethod(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectMethod(unit, selection, 1);
	}

	/**
	 * Select a method in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @return IMethod
	 */
	protected IMethod selectMethod(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		return (IMethod) selectJavaElement(unit, selection, occurences, IJavaElement.METHOD);
	}

	/**
	 * Select a parameterized source method in a compilation unit identified with the first occurence in the source of a given selection.
	 * @return ParameterizedSourceMethod
	 */
	protected ResolvedSourceMethod selectParameterizedMethod(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectParameterizedMethod(unit, selection, 1);
	}

	/**
	 * Select a parameterized source method in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @return ParameterizedSourceMethod
	 */
	protected ResolvedSourceMethod selectParameterizedMethod(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		IMethod type = selectMethod(unit, selection, occurences);
		assertTrue("Not a parameterized source type: "+type.getElementName(), type instanceof ResolvedSourceMethod);
		return (ResolvedSourceMethod) type;
	}

	/**
	 * Select a parameterized source type in a compilation unit identified with the first occurence in the source of a given selection.
	 * @return ParameterizedSourceType
	 */
	protected ResolvedSourceType selectParameterizedType(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectParameterizedType(unit, selection, 1);
	}

	/**
	 * Select a parameterized source type in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @return ParameterizedSourceType
	 */
	protected ResolvedSourceType selectParameterizedType(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		IType type = selectType(unit, selection, occurences);
		assertTrue("Not a parameterized source type: "+type.getElementName(), type instanceof ResolvedSourceType);
		return (ResolvedSourceType) type;
	}

	/**
	 * Select a type in a compilation unit identified with the first occurence in the source of a given selection.
	 * @return IType
	 */
	protected IType selectType(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectType(unit, selection, 1);
	}

	/**
	 * Select a type in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @return IType
	 */
	protected IType selectType(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		return (IType) selectJavaElement(unit, selection, occurences, IJavaElement.TYPE);
	}

	/**
	 * Select a type parameter in a compilation unit identified with the first occurence in the source of a given selection.
	 * @return IType
	 */
	protected ITypeParameter selectTypeParameter(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectTypeParameter(unit, selection, 1);
	}

	/**
	 * Select a type parameter in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @return IType
	 */
	protected ITypeParameter selectTypeParameter(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		return (ITypeParameter) selectJavaElement(unit, selection, occurences, IJavaElement.TYPE_PARAMETER);
	}

	/**
	 * Select a java element in a compilation unit identified with the nth occurence in the source of a given selection.
	 * Do not allow subclasses to call this method as we want to verify IJavaElement kind.
	 */
	IJavaElement selectJavaElement(ICompilationUnit unit, String selection, int occurences, int elementType) throws JavaModelException {
		int[] selectionPositions = selectionInfo(unit, selection, occurences);
		IJavaElement[] elements = null;
		if (this.wcOwner == null) {
			elements = unit.codeSelect(selectionPositions[0], selectionPositions[1]);
		} else {
			elements = unit.codeSelect(selectionPositions[0], selectionPositions[1], this.wcOwner);
		}
		assertEquals("Invalid selection number", 1, elements.length);
		assertEquals("Invalid java element type: "+elements[0].getElementName(), elements[0].getElementType(), elementType);
		return elements[0];
	}

	/* ************
	 * Suite set-ups *
	 *************/
	/**
	 * Sets the class path of the Java project.
	 */
	public void setClasspath(IJavaProject javaProject, IClasspathEntry[] classpath) {
		try {
			javaProject.setRawClasspath(classpath, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
			assertTrue("failed to set classpath", false);
		}
	}
	protected IJavaProject setupModuleProject(String name, String[] sources) throws CoreException {
		return setupModuleProject(name, sources, false);
	}
	protected IJavaProject setupModuleProject(String name, String[] sources, boolean addModulePathContainer) throws CoreException {
		IClasspathEntry[] deps = null;
		if (addModulePathContainer) {
			IClasspathEntry containerEntry = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			deps = new IClasspathEntry[] {containerEntry};
		}
		return setupModuleProject(name, sources, deps);
	}
	protected IJavaProject setupModuleProject(String name, String[] sources, IClasspathEntry[] deps) throws CoreException {
		return setupModuleProject(name, new String[]{"src"}, sources, deps);
	}
	protected IJavaProject setupModuleProject(String name, String[] srcFolders, String[] sources, IClasspathEntry[] deps) throws CoreException {
		IJavaProject project = createJava9Project(name, srcFolders);
		createSourceFiles(project, sources);
		if (deps != null) {
			IClasspathEntry[] old = project.getRawClasspath();
			IClasspathEntry[] newPath = new IClasspathEntry[old.length + deps.length];
			System.arraycopy(old, 0, newPath, 0, old.length);
			System.arraycopy(deps, 0, newPath, old.length, deps.length);
			project.setRawClasspath(newPath, null);
		}
		return project;
	}

	protected void createSourceFiles(IJavaProject project, String[] sources) throws CoreException {
		IProgressMonitor monitor = new NullProgressMonitor();
		for (int i = 0; i < sources.length; i+= 2) {
			IPath path = new Path(sources[i]);
			IPath parentPath = path.removeLastSegments(1);
			IFolder folder = project.getProject().getFolder(parentPath);
			if (!folder.exists())
				this.createFolder(folder.getFullPath());
			IFile file = project.getProject().getFile(new Path(sources[i]));
			file.create(new ByteArrayInputStream(sources[i+1].getBytes()), true, monitor);
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
			new java.io.File(externalPath + jclName + ".jar");
		java.io.File jclMinsrc = new java.io.File(externalPath + jclName + "src.zip");
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
			if ((jclMin.lastModified() < resourceJCLMin.lastModified())
                    || (jclMin.length() != resourceJCLMin.length())) {
				copy(resourceJCLMin, jclMin);
			}
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + jclName + "src.zip");
			if ((jclMinsrc.lastModified() < resourceJCLMinsrc.lastModified())
                    || (jclMinsrc.length() != resourceJCLMinsrc.length())) {
				copy(resourceJCLMinsrc, jclMinsrc);
			}
		}
	}
	protected IJavaProject setUpJavaProject(final String projectName) throws CoreException, IOException {
		this.currentProject = setUpJavaProject(projectName, "1.4");
		return this.currentProject;
	}
	protected IJavaProject setUpJavaProject(final String projectName, String compliance) throws CoreException, IOException {
		this.currentProject =  setUpJavaProject(projectName, compliance, false);
		return this.currentProject;
	}
	protected IJavaProject setUpJavaProject(final String projectName, String compliance, boolean useFullJCL) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		String sourceWorkspacePath = getSourceWorkspacePath();
		String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
		copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName));

		// ensure variables are set
		setUpJCLClasspathVariables(compliance, useFullJCL);

		// create project
		final IProject project = getWorkspaceRoot().getProject(projectName);
		IWorkspaceRunnable populate = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
				project.setDefaultCharset(ResourcesPlugin.getEncoding(), monitor);
			}
		};
		getWorkspace().run(populate, null);
		waitForCharsetDeltaJob();
		IJavaProject javaProject = JavaCore.create(project);
		setUpProjectCompliance(javaProject, compliance, useFullJCL);
		javaProject.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
		javaProject.setOption(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
		javaProject.setOption(JavaCore.COMPILER_PB_FIELD_HIDING, JavaCore.IGNORE);
		javaProject.setOption(JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING, JavaCore.IGNORE);
		javaProject.setOption(JavaCore.COMPILER_PB_TYPE_PARAMETER_HIDING, JavaCore.IGNORE);
		javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, compliance);
		javaProject.setOption(JavaCore.COMPILER_SOURCE, compliance);
		javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compliance);
		return javaProject;
	}
	protected void setUpProjectCompliance(IJavaProject javaProject, String compliance) throws JavaModelException, IOException {
		setUpProjectCompliance(javaProject, compliance, false);
	}
	protected void setUpProjectCompliance(IJavaProject javaProject, String compliance, boolean useFullJCL) throws JavaModelException, IOException {
		// Look for version to set and return if that's already done
		String version = compliance; // assume that the values of CompilerOptions.VERSION_* are used
		if (version.equals(javaProject.getOption(CompilerOptions.OPTION_Compliance, false))) {
			return;
		}
		String newJclLibString;
		String newJclSrcString;
		if (useFullJCL) {
			if (compliance.equals("10")) {
				newJclLibString = "JCL10_LIB"; // TODO: have no full variant yet
				newJclSrcString = "JCL10_SRC";
			} else {
				newJclLibString = "JCL18_FULL";
				newJclSrcString = "JCL18_SRC"; // Use the same source
			}
		} else {
			if (compliance.equals("21")) {
				// Reuse the same 14 stuff as of now. No real need for a new one
				newJclLibString = "JCL_17_LIB";
				newJclSrcString = "JCL_17_SRC";
			} else if (compliance.equals("19")) {
				// Reuse the same 14 stuff as of now. No real need for a new one
				newJclLibString = "JCL_19_LIB";
				newJclSrcString = "JCL_19_SRC";
			} else if (compliance.equals("17")) {
				// Reuse the same 14 stuff as of now. No real need for a new one
				newJclLibString = "JCL_17_LIB";
				newJclSrcString = "JCL_17_SRC";
			} else if (compliance.equals("16")) {
				// Reuse the same 14 stuff as of now. No real need for a new one
				newJclLibString = "JCL14_LIB";
				newJclSrcString = "JCL14_SRC";
			} else if (compliance.equals("15")) {
				// Reuse the same 14 stuff as of now. No real need for a new one
				newJclLibString = "JCL14_LIB";
				newJclSrcString = "JCL14_SRC";
			} else if (compliance.equals("14")) {
				newJclLibString = "JCL14_LIB";
				newJclSrcString = "JCL14_SRC";
			} else if (compliance.equals("13")) {
				newJclLibString = "JCL13_LIB";
				newJclSrcString = "JCL13_SRC";
			} else if (compliance.equals("12")) {
				newJclLibString = "JCL12_LIB";
				newJclSrcString = "JCL12_SRC";
			} else if (compliance.equals("11")) {
				newJclLibString = "JCL11_LIB";
				newJclSrcString = "JCL11_SRC";
			} else if (compliance.equals("10")) {
				newJclLibString = "JCL10_LIB";
				newJclSrcString = "JCL10_SRC";
			} else if (compliance.length() < 3) {
				newJclLibString = "JCL19_LIB";
				newJclSrcString = "JCL19_SRC";
			} else if (compliance.charAt(2) > '7') {
				newJclLibString = "JCL18_LIB";
				newJclSrcString = "JCL18_SRC";
			} else if (compliance.charAt(2) > '4') {
				newJclLibString = "JCL15_LIB";
				newJclSrcString = "JCL15_SRC";
			} else {
				newJclLibString = "JCL_LIB";
				newJclSrcString = "JCL_SRC";
			}
		}

		// ensure variables are set
		setUpJCLClasspathVariables(compliance, useFullJCL);

		// set options
		Map options = new HashMap();
		options.put(CompilerOptions.OPTION_Compliance, version);
		options.put(CompilerOptions.OPTION_Source, version);
		options.put(CompilerOptions.OPTION_TargetPlatform, version);
		javaProject.setOptions(options);

		IClasspathEntry[] classpath = javaProject.getRawClasspath();

		for (int i = 0, length = classpath.length; i < length; i++) {
			IClasspathEntry entry = classpath[i];
			final IPath path = entry.getPath();
			// Choose the new JCL path only if the current JCL path is different
			if (isJCLPath(path) && !path.toString().equals(newJclLibString)) {
					classpath[i] = JavaCore.newVariableEntry(
							new Path(newJclLibString),
							new Path(newJclSrcString),
							entry.getSourceAttachmentRootPath(),
							entry.getAccessRules(),
							new IClasspathAttribute[0],
							entry.isExported());
					break;
			}
		}
		javaProject.setRawClasspath(classpath, null);
	}
	public boolean isJCLPath(IPath path) {
		IPath jclLib = new Path("JCL_LIB");
		IPath jcl5Lib = new Path("JCL15_LIB");
		IPath jcl8Lib = new Path("JCL18_LIB");
		IPath jcl9Lib = new Path("JCL19_LIB");
		IPath jcl10Lib = new Path("JCL10_LIB");
		IPath jcl11Lib = new Path("JCL11_LIB");
		IPath jcl12Lib = new Path("JCL12_LIB");
		IPath jcl13Lib = new Path("JCL13_LIB");
		IPath jcl14Lib = new Path("JCL14_LIB");
		IPath jcl17Lib = new Path("JCL_17_LIB");
		IPath jcl21Lib = new Path("JCL_21_LIB");
		IPath jclFull = new Path("JCL18_FULL");

		return path.equals(jclLib) || path.equals(jcl5Lib) || path.equals(jcl8Lib) || path.equals(jcl9Lib)
				|| path.equals(jcl10Lib) ||  path.equals(jcl11Lib) || path.equals(jcl12Lib) || path.equals(jcl13Lib)
				|| path.equals(jcl14Lib) || path.equals(jcl17Lib) || path.equals(jcl21Lib) || path.equals(jclFull);
	}
	public void setUpJCLClasspathVariables(String compliance) throws JavaModelException, IOException {
		setUpJCLClasspathVariables(compliance, false);
	}
	public void setUpJCLClasspathVariables(String compliance, boolean useFullJCL) throws JavaModelException, IOException {
		if ("1.5".equals(compliance) || "1.6".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL15_LIB") == null) {
				setupExternalJCL("jclMin1.5");
				JavaCore.setClasspathVariables(
					new String[] {"JCL15_LIB", "JCL15_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("1.5"), getExternalJCLSourcePath("1.5"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("1.7".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL17_LIB") == null) {
				setupExternalJCL("jclMin1.7");
				JavaCore.setClasspathVariables(
					new String[] {"JCL17_LIB", "JCL17_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("1.7"), getExternalJCLSourcePath("1.7"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("1.8".equals(compliance)) {
			if (useFullJCL) {
				if (JavaCore.getClasspathVariable("JCL18_FULL") == null) {
					setupExternalJCL("jclMin1.8"); // Create the whole mininmal 1.8 set, though we will need only the source zip
					setupExternalJCL("jclFull1.8");
					JavaCore.setClasspathVariables(
						new String[] {"JCL18_FULL", "JCL18_SRC", "JCL_SRCROOT"},
						new IPath[] {new Path(getExternalJCLPathString("1.8", true)), getExternalJCLSourcePath("1.8"), getExternalJCLRootSourcePath()},
						null);
				}
			} else if (JavaCore.getClasspathVariable("JCL18_LIB") == null) {
						setupExternalJCL("jclMin1.8");
						JavaCore.setClasspathVariables(
							new String[] {"JCL18_LIB", "JCL18_SRC", "JCL_SRCROOT"},
							new IPath[] {getExternalJCLPath("1.8"), getExternalJCLSourcePath("1.8"), getExternalJCLRootSourcePath()},
							null);
			}
		} else if ("9".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL19_LIB") == null) {
				setupExternalJCL("jclMin9");
				JavaCore.setClasspathVariables(
					new String[] {"JCL19_LIB", "JCL19_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("9"), getExternalJCLSourcePath("9"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("10".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL10_LIB") == null) {
				setupExternalJCL("jclMin10");
				JavaCore.setClasspathVariables(
					new String[] {"JCL10_LIB", "JCL10_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("10"), getExternalJCLSourcePath("10"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("11".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL11_LIB") == null) {
				setupExternalJCL("jclMin11");
				JavaCore.setClasspathVariables(
					new String[] {"JCL11_LIB", "JCL11_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("11"), getExternalJCLSourcePath("11"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("12".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL12_LIB") == null) {
				setupExternalJCL("jclMin12");
				JavaCore.setClasspathVariables(
					new String[] {"JCL12_LIB", "JCL12_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("12"), getExternalJCLSourcePath("12"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("13".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL13_LIB") == null) {
				setupExternalJCL("jclMin13"); // No need for an explicit jclmin13, just use the same old one.
				JavaCore.setClasspathVariables(
					new String[] {"JCL13_LIB", "JCL13_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("13"), getExternalJCLSourcePath("13"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("14".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL14_LIB") == null) {
				setupExternalJCL("jclMin14");
				JavaCore.setClasspathVariables(
					new String[] {"JCL14_LIB", "JCL14_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("14"), getExternalJCLSourcePath("14"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("15".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL14_LIB") == null) {
				setupExternalJCL("jclMin14");
				JavaCore.setClasspathVariables(
					new String[] {"JCL14_LIB", "JCL14_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("14"), getExternalJCLSourcePath("14"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("16".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL14_LIB") == null) {
				setupExternalJCL("jclMin14");
				JavaCore.setClasspathVariables(
					new String[] {"JCL14_LIB", "JCL14_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("14"), getExternalJCLSourcePath("14"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("17".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL_17_LIB") == null) {
				setupExternalJCL("jclMin17");
				JavaCore.setClasspathVariables(
					new String[] {"JCL_17_LIB", "JCL_17_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("17"), getExternalJCLSourcePath("17"), getExternalJCLRootSourcePath()},
					null);
			}
		} else if ("21".equals(compliance)) {
			if (JavaCore.getClasspathVariable("JCL_21_LIB") == null) {
				setupExternalJCL("jclMin17");
				JavaCore.setClasspathVariables(
					new String[] {"JCL_17_LIB", "JCL_17_SRC", "JCL_SRCROOT"},
					new IPath[] {getExternalJCLPath("17"), getExternalJCLSourcePath("17"), getExternalJCLRootSourcePath()},
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
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		// ensure autobuilding is turned off
		IWorkspaceDescription description = getWorkspace().getDescription();
		if (description.isAutoBuilding()) {
			description.setAutoBuilding(false);
			getWorkspace().setDescription(description);
		}

		if (!systemConfigReported) {
			printSystemEnv();
			systemConfigReported = true;
		}
		printMemoryUse();
	}

	@Override
	protected void setUp () throws Exception {
		super.setUp();

		if (NameLookup.VERBOSE || BasicSearchEngine.VERBOSE || JavaModelManager.VERBOSE) {
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("Running test "+getName()+"...");
		}
		logInfo("SETUP " + getName());
	}

    private static void printSystemEnv() {
        Set<Entry<String, String>> set = new TreeMap<>(System.getenv()).entrySet();
        StringBuilder sb = new StringBuilder("\n###################### System environment ######################\n");
        for (Entry<String, String> entry : set) {
            sb.append(" ").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        sb.append("\n###################### System properties ######################\n");
        Set<Entry<String, String>> props = getPropertiesSafe();
        for (Entry<String, String> entry : props) {
            sb.append(" ").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        String env = sb.toString();
        System.out.println(env);
        logInfo(env);
    }

    private static void printMemoryUse() {
    	System.gc();
    	System.runFinalization();
    	System.gc();
    	System.runFinalization();
    	long nax = Runtime.getRuntime().maxMemory();
    	long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		long used = total - free;
		System.out.print("\n########### Memory usage reported by JVM ########");
		System.out.printf(Locale.GERMAN, "%n%,16d bytes max heap", nax);
		System.out.printf(Locale.GERMAN, "%n%,16d bytes heap allocated", total);
		System.out.printf(Locale.GERMAN, "%n%,16d bytes free heap", free);
    	System.out.printf(Locale.GERMAN, "%n%,16d bytes used heap", used);
    	System.out.println("\n#################################################\n");
    }

    /**
     * Retrieves properties safely. In case if someone tries to change the properties set
     * while iterating over the collection, we repeat the procedure till this
     * works without an error.
     */
    private static Set<Entry<String, String>> getPropertiesSafe() {
        try {
            return new TreeMap<>(System.getProperties().entrySet().stream()
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                            e -> String.valueOf(e.getValue())))).entrySet();
        } catch (Exception e) {
            return getPropertiesSafe();
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
				if (a instanceof IResource) {
					IResource resourceA = (IResource)a;
					IResource resourceB = (IResource)b;
					return resourceA.getFullPath().toString().compareTo(resourceB.getFullPath().toString());
				} else {
					IJarEntryResource resourceA = (IJarEntryResource)a;
					IJarEntryResource resourceB = (IJarEntryResource)b;
					return resourceA.getFullPath().toString().compareTo(resourceB.getFullPath().toString());
				}
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
	/*
	 * Simulate a save/exit of the workspace
	 */
	protected void simulateExit() throws CoreException {
		waitForAutoBuild();
		getWorkspace().save(true/*full save*/, null/*no progress*/);
		JavaModelManager.getJavaModelManager().shutdown();
	}
	/*
	 * Simulate a save/exit/restart of the workspace
	 */
	protected void simulateExitRestart() throws CoreException {
		simulateExit();
		simulateRestart();
	}
	/*
	 * Simulate a restart of the workspace
	 */
	protected void simulateRestart() throws CoreException {
		JavaModelManager.doNotUse(); // reset the MANAGER singleton
		JavaModelManager.getJavaModelManager().startup();
		new JavaCorePreferenceInitializer().initializeDefaultPreferences();
	}
	/**
	 * Starts listening to element deltas, and queues them in fgDeltas.
	 */
	public void startDeltas(DeltaListener listener) {
		clearDeltas(listener);
		JavaCore.addElementChangedListener(listener);
		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
	}
	/**
	 * Stops listening to element deltas, and clears the current deltas.
	 */
	public void stopDeltas(DeltaListener listener) {
		getWorkspace().removeResourceChangeListener(listener);
		JavaCore.removeElementChangedListener(listener);
		clearDeltas(listener);
	}
	/**
	 * Starts listening to element deltas, and queues them in fgDeltas.
	 */
	public void startDeltas() {
		clearDeltas();
		JavaCore.addElementChangedListener(this.deltaListener);
		getWorkspace().addResourceChangeListener(this.deltaListener, IResourceChangeEvent.POST_CHANGE);
	}
	/**
	 * Stops listening to element deltas, and clears the current deltas.
	 */
	public void stopDeltas() {
		getWorkspace().removeResourceChangeListener(this.deltaListener);
		JavaCore.removeElementChangedListener(this.deltaListener);
		clearDeltas();
	}
	protected void startLogListening() {
		startLogListening(JavaCore.getPlugin().getLog());
	}
	protected void startLogListening(ILog logToListen) {
		stopLogListening(); // cleanup if we forgot to stop listening
		this.log = logToListen;
		this.logListener = new LogListenerWithHistory();
		if (logToListen == null) {
			Platform.addLogListener(this.logListener);
		} else {
			this.log.addLogListener(this.logListener);
		}
	}
	protected void stopLogListening() {
		if (this.logListener == null)
			return;
		if (this.log == null) {
			Platform.removeLogListener(this.logListener);
		} else {
			this.log.removeLogListener(this.logListener);
		}
		this.logListener = null;
		this.log = null;
	}
	protected void assertLogEquals(String expected) {
		String actual = this.logListener == null ? "<null>" : this.logListener.toString();
		assertSourceEquals(
			"Unexpected entry in log",
			expected,
			actual);
	}
	protected IPath[] toIPathArray(String[] paths) {
		if (paths == null) return null;
		int length = paths.length;
		IPath[] result = new IPath[length];
		for (int i = 0; i < length; i++) {
			result[i] = new Path(paths[i]);
		}
		return result;
	}
	protected void touch(File f) {
		final int time = 1000;
		long lastModified = f.lastModified();
		org.eclipse.jdt.core.tests.util.Util.waitAtLeast(time);
		f.setLastModified(lastModified + time);
		// Loop until the last modified time has really changed on the file
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=295619
		int n = 1;
		while (n < 10) { // retry 9 times more if necessary
			if (f.lastModified() != lastModified) {
				// We can leave the loop as the file has been really touched
				return;
			}
			f.setLastModified(lastModified + n*time);
			org.eclipse.jdt.core.tests.util.Util.waitAtLeast(time);
			n++;
		}
		assertFalse("The file "+f.getAbsolutePath()+" was not touched!", lastModified == f.lastModified());
	}

	protected String toString(String[] strings) {
		return org.eclipse.jdt.core.tests.util.Util.toString(strings, false/*don't add extra new line*/);
	}
	@Override
	protected void tearDown() throws Exception {
		logInfo("TEARDOWN " + getName());
		if (this.workingCopies != null) {
			discardWorkingCopies(this.workingCopies);
			this.workingCopies = null;
		}
		this.wcOwner = null;

		// ensure workspace options have been restored to their default
		Hashtable options = JavaCore.getOptions();
		Hashtable defaultOptions = getDefaultJavaCoreOptions();
		boolean resetToDefault = true;
		try {
			String expected = new CompilerOptions(defaultOptions).toString();
			String actual = new CompilerOptions(options).toString();
			assertEquals("Workspace options should be back to their default", expected, actual);
			resetToDefault = false;
		} finally {
			if(resetToDefault) {
				// Don't let all following tests use broken defaults and fail too
				JavaCore.setOptions(defaultOptions);
			}
		}
		super.tearDown();
	}

	/**
	 * Override to supply "test class default JavaCore options"
	 * so that these options will be restored for other tests in the class
	 * even if one the test changes them without restoring in teardown.
	 *
	 * @return by default {@link JavaCore#getDefaultOptions()}
	 */
	protected Hashtable<String, String> getDefaultJavaCoreOptions() {
		return JavaCore.getDefaultOptions();
	}

	protected IPath getJRE9Path() {
		return new Path(System.getProperty("java.home") + "/lib/jrt-fs.jar");
	}

	public void waitForCharsetDeltaJob() throws CoreException {
		try {
			Job.getJobManager().join(CharsetDeltaJob.FAMILY_CHARSET_DELTA, null);
		} catch (OperationCanceledException | InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, e.getMessage(), e));
		}
	}

	/**
	 * Wait for autobuild notification to occur
	 */
	public void waitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				JavaModelManager.getIndexManager().waitForIndex(isIndexDisabledForTest(), null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}

	public void waitForManualRefresh() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_REFRESH, null);
				JavaModelManager.getIndexManager().waitForIndex(isIndexDisabledForTest(), null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}

	public void waitUntilIndexesReady() {
		// dummy query for waiting until the indexes are ready
		SearchEngine engine = new SearchEngine();
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		try {
			JavaModelManager.getIndexManager().waitForIndex(isIndexDisabledForTest(), null);
			engine.searchAllTypeNames(
				null,
				SearchPattern.R_EXACT_MATCH,
				"!@$#!@".toCharArray(),
				SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
				IJavaSearchConstants.CLASS,
				scope,
				new TypeNameRequestor() {
					public void acceptType(
						int modifiers,
						char[] packageName,
						char[] simpleTypeName,
						char[][] enclosingTypeNames,
						String path) {}
				},
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		} catch (CoreException e) {
			logError("exception occurred while waiting on indexing", e);
		}
	}

	protected void makeJCLModular(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		for (int i = 0, length = classpath.length; i < length; i++) {
			IClasspathEntry entry = classpath[i];
			final IPath path = entry.getPath();
			if (isJCLPath(path)) {
					classpath[i] = JavaCore.newVariableEntry(
							entry.getPath(),
							entry.getSourceAttachmentPath(),
							entry.getSourceAttachmentRootPath(),
							entry.getAccessRules(),
							moduleAttribute(),
							entry.isExported());
					break;
			}
		}
		javaProject.setRawClasspath(classpath, null);
	}

	private static void logError(String errorMessage, CoreException e) {
		Plugin plugin = JavaCore.getPlugin();
		if (plugin != null) {
			ILog log = plugin.getLog();
			Status status = new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, errorMessage, e);
			log.log(status);
		} else {
			System.out.println(errorMessage);
			e.printStackTrace(System.out);
		}
	}

	private static void logInfo(String message) {
		Plugin plugin = JavaCore.getPlugin();
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.INFO, JavaCore.PLUGIN_ID, message));
		} else {
			System.out.println(message);
		}
	}
}
