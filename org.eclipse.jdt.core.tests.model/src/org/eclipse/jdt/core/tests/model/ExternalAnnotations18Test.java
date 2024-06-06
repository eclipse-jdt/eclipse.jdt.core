/*******************************************************************************
 * Copyright (c) 2014, 2020 GK Software AG, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     Pierre-Yves B. <pyvesdev@gmail.com> - Contributions for bug 559618 - No compiler warning for import from same package
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.tests.model.ContainerInitializer.ITestInitializer;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ExternalAnnotationUtil;
import org.eclipse.jdt.core.util.ExternalAnnotationUtil.MergeStrategy;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.osgi.framework.Bundle;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ExternalAnnotations18Test extends ModifyingResourceTests {

	/**
	 * Initializer for a container that may provide a mix of entries, some of which are "self-annotating",
	 * i.e., contain .eea files for their own classes.
	 */
	static class TestCustomContainerInitializer implements ContainerInitializer.ITestInitializer {

		/** Use this container name in test projects. */
		private static final String CONTAINER_NAME = "org.eclipse.jdt.core.tests.model.TEST_CONTAINER";

		List<String> allEntries;
		Map<String,String> elementAnnotationPaths;

		/**
		 * @param elementsAndAnnotationPaths each pair of entries in this array defines one classpath entry:
		 * <ul>
		 * 	<li>1st string specifies the path,
		 *  <li>if 2nd string is "self" than the entry is "self-annotating".
		 *  	 {@code null} is a legal value to signal "not self-annotating"
		 *  </ul>
		 */
		public TestCustomContainerInitializer(String... elementsAndAnnotationPaths) {
			this.allEntries = new ArrayList<>();
			this.elementAnnotationPaths = new HashMap<>();
			for (int i = 0; i < elementsAndAnnotationPaths.length; i+=2) {
				String entryPath = elementsAndAnnotationPaths[i];
				this.allEntries.add(entryPath);
				String annotsPath = elementsAndAnnotationPaths[i+1];
				if ("self".equals(annotsPath))
					this.elementAnnotationPaths.put(entryPath, entryPath);
				else if (annotsPath != null)
					this.elementAnnotationPaths.put(entryPath, annotsPath);
			}
		}

		static class TestContainer implements IClasspathContainer {
			IPath path;
			IClasspathEntry[] entries;
			TestContainer(IPath path, IClasspathEntry[] entries){
				this.path = path;
				this.entries = entries;
			}
			public IPath getPath() { return this.path; }
			public IClasspathEntry[] getClasspathEntries() { return this.entries;	}
			public String getDescription() { return this.path.toString(); 	}
			public int getKind() { return 0; }
		}

		@Override
		public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
			List<IClasspathEntry> entries = new ArrayList<>();
			for (String entryPath : this.allEntries) {
				IClasspathAttribute[] extraAttributes;
				String elementAnnotationPath = this.elementAnnotationPaths.get(entryPath);
				if (elementAnnotationPath != null)
					extraAttributes = externalAnnotationExtraAttributes(elementAnnotationPath);
				else
					extraAttributes = ClasspathEntry.NO_EXTRA_ATTRIBUTES;
				entries.add(JavaCore.newLibraryEntry(new Path(entryPath), null, null,
						ClasspathEntry.NO_ACCESS_RULES, extraAttributes, false/*not exported*/));
			}
			JavaCore.setClasspathContainer(
					new Path(CONTAINER_NAME),
					new IJavaProject[]{ project },
					new IClasspathContainer[] { new TestContainer(new Path(CONTAINER_NAME), entries.toArray(IClasspathEntry[]::new)) },
					null);
		}
		@Override
		public boolean allowFailureContainer() {
			return false;
		}
	}

	static class LogListener implements ILogListener {
    	List<IStatus> loggedStatus = new ArrayList<>();
        public void logging(IStatus status, String plugin) {
            this.loggedStatus.add(status);
        }
	}


	protected IJavaProject project;
	protected IPackageFragmentRoot root;
	protected String ANNOTATION_LIB;
	protected final String compliance;
	protected final String jclLib;

	protected static final String MY_MAP_CONTENT =
			"package libs;\n" +
			"\n" +
			"public interface MyMap<K,V> {\n" +
			"	V get(Object key);\n" +
			"	V put(K key, V val);\n" +
			"	V remove(Object key);\n" +
			"}\n";

	public ExternalAnnotations18Test(String name) {
		this(name, "1.8", "JCL18_LIB");
	}

	protected ExternalAnnotations18Test(String name, String compliance, String jclLib) {
		super(name);
		this.compliance = compliance;
		this.jclLib = jclLib;
	}

	// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_PREFIX = "testLibsWithTypeParameters";
//		TESTS_NAMES = new String[] {"test3"};
//		TESTS_NUMBERS = new int[] { 23, 28, 38 };
//		TESTS_RANGE = new int[] { 21, 38 };
	}
	public static Test suite() {
		return buildModelTestSuite(ExternalAnnotations18Test.class, BYTECODE_DECLARATION_ORDER);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		Bundle[] bundles = getAnnotationBundles();
		File bundleFile = FileLocator.getBundleFileLocation(bundles[0]).get();
		this.ANNOTATION_LIB = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();

		// set up class path container bridging to the host JRE:
		ContainerInitializer.setInitializer(new TestContainerInitializer(IClasspathContainer.K_DEFAULT_SYSTEM));
	}

	/**
	 * @deprecated
	 */
	static int getJLS8() {
		return AST.JLS8;
	}
	@Deprecated
	static int getJSL9() {
		return AST.JLS9;
	}

	/**
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	protected Bundle[] getAnnotationBundles() {
		return org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[2.0.0,3.0.0)");
	}

	@Override
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		ContainerInitializer.setInitializer(null);
	}

	@Override
	public String getSourceWorkspacePath() {
		// we read individual projects from within this folder:
		return super.getSourceWorkspacePath()+"/ExternalAnnotations18";
	}

	protected String getSourceWorkspacePathBase() {
		return super.getSourceWorkspacePath();
	}

	void setupJavaProject(String name) throws CoreException, IOException {
		setupJavaProject(name, false, true);
	}

	void setupJavaProject(String name, boolean useFullJCL, boolean addAnnotationLib) throws CoreException, IOException {
		this.project = setUpJavaProject(name, this.compliance, useFullJCL); //$NON-NLS-1$
		if(addAnnotationLib)
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

	void myCreateJavaProject(String name) throws CoreException {
		myCreateJavaProject(name, this.compliance, this.jclLib);
	}
	void myCreateJavaProject(String name, String projectCompliance, String projectJclLib) throws CoreException {
		this.project = createJavaProject(name, new String[]{"src"}, new String[]{projectJclLib}, null, null, "bin", null, null, null, projectCompliance);
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

	@Override
	protected void tearDown() throws Exception {
		if (this.project != null)
			this.project.getProject().delete(true, true, null);
		this.project = null;
		this.root = null;
		super.tearDown();
	}

	protected void addLibraryWithExternalAnnotations(IJavaProject javaProject, String jarName, String externalAnnotationPath,
			String[] pathAndContents, Map options) throws CoreException, IOException {
		addLibraryWithExternalAnnotations(javaProject, this.compliance, jarName, externalAnnotationPath, pathAndContents, options);
	}

	protected void addProjectDependencyWithExternalAnnotations(
			IJavaProject javaProject,
			String referencedProjectName,
			String externalAnnotationPath,
			Map options) throws CoreException, IOException
	{
		IClasspathEntry entry = JavaCore.newProjectEntry(
				new Path(referencedProjectName),
				null/*access rules*/,
				false/*combine access rules*/,
				externalAnnotationExtraAttributes(externalAnnotationPath),
				false/*exported*/);
		addClasspathEntry(this.project, entry);
	}

	protected void addEeaToVariableEntry(String variableName, String annotationPath) throws JavaModelException {
		IClasspathEntry[] rawClasspath = this.project.getRawClasspath();
		boolean found = false;
		for (int i = 0; i < rawClasspath.length; i++) {
			IClasspathEntry entry = rawClasspath[i];
			if (entry.getPath().toString().equals(variableName)) {
				rawClasspath[i] = JavaCore.newVariableEntry(
						entry.getPath(),
						entry.getSourceAttachmentPath(),
						entry.getSourceAttachmentRootPath(),
						entry.getAccessRules(),
						new IClasspathAttribute[] {
							new ClasspathAttribute(IClasspathAttribute.EXTERNAL_ANNOTATION_PATH, annotationPath)
						},
						entry.isExported());
				found = true;
				break;
			}
		}
		assertTrue("Should find classpath entry "+variableName, found);
		this.project.setRawClasspath(rawClasspath, new NullProgressMonitor());
	}

	protected void addSourceFolderWithExternalAnnotations(IJavaProject javaProject, String sourceFolder, String outputFolder, String externalAnnotationPath) throws JavaModelException {
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[] { new ClasspathAttribute(IClasspathAttribute.EXTERNAL_ANNOTATION_PATH, externalAnnotationPath) };
		IClasspathEntry entry = JavaCore.newSourceEntry(new Path(sourceFolder), null, null,
				outputFolder != null ? new Path(outputFolder) : null, extraAttributes);
		addClasspathEntry(javaProject, entry);
	}

	protected void createFileInProject(String projectRelativeFolder, String fileName, String content) throws CoreException {
		String folderPath = this.project.getProject().getName()+'/'+projectRelativeFolder;
		createFolder(folderPath);
		createFile(folderPath+'/'+fileName, content);
	}

	protected void assertNoMarkers(IMarker[] markers) throws CoreException {
		for (int i = 0; i < markers.length; i++)
			System.err.println("Unexpected marker: "+markers[i].getAttributes().entrySet());
		assertEquals("Number of markers", 0, markers.length);
	}

	protected void assertNoProblems(IProblem[] problems) throws CoreException {
		for (int i = 0; i < problems.length; i++)
			System.err.println("Unexpected marker: "+problems[i]);
		assertEquals("Number of markers", 0, problems.length);
	}

	protected void assertProblems(IProblem[] problems, String[] messages, int[] lines) throws CoreException {
		int nMatch = 0;
		for (int i = 0; i < problems.length; i++) {
			for (int j = 0; j < messages.length; j++) {
				if (messages[j] == null) continue;
				if (problems[i].toString().equals(messages[j])
						&& problems[i].getSourceLineNumber() == lines[j]) {
					messages[j] = null;
					problems[i] = null;
					nMatch++;
					break;
				}
			}
		}
		for (int i = 0; i < problems.length; i++) {
			if (problems[i] != null)
				fail("Unexpected problem "+problems[i]+" at "+problems[i].getSourceLineNumber());
		}
		for (int i = 0; i < messages.length; i++) {
			if (messages[i] != null)
				System.err.println("Unmatched problem "+messages[i]);
		}
		assertEquals("Number of problems", messages.length, nMatch);
	}

	protected void assertProblems(IProblem[] problems, String[] messages, int[] lines, int[] severities) throws CoreException {
		int nMatch = 0;
		for (int i = 0; i < problems.length; i++) {
			for (int j = 0; j < messages.length; j++) {
				if (messages[j] == null) continue;
				if (problems[i].toString().equals(messages[j])) {
					if (problems[i].getSourceLineNumber() == lines[j]) {
						switch(severities[j] & ProblemSeverities.CoreSeverityMASK ) {
						case ProblemSeverities.Error:
							if (!problems[i].isError()) {
								System.err.println("Not an error as expected: "+messages[j]);
								continue;
							}
							break;
						case ProblemSeverities.Warning:
							if (!problems[i].isWarning()) {
								System.err.println("Not a warning as expected: "+messages[j]);
								continue;
							}
							break;
						case ProblemSeverities.Info:
							if (!problems[i].isInfo()) {
								System.err.println("Not an info as expected: "+messages[j]);
								continue;
							}
							break;
						default:
							throw new IllegalArgumentException("Bad severity expected: "+severities[j]);
						}
						messages[j] = null;
						problems[i] = null;
						nMatch++;
						break;
					} else {
						System.err.println("Match at wrong line: "+problems[i].getSourceLineNumber()+" vs. "+lines[j]+": "+messages[j]);
					}
				}
			}
		}
		for (int i = 0; i < problems.length; i++) {
			if (problems[i] != null)
				fail("Unexpected problem "+problems[i]+" at "+problems[i].getSourceLineNumber());
		}
		for (int i = 0; i < messages.length; i++) {
			if (messages[i] != null)
				System.err.println("Unmatched problem "+messages[i]);
		}
		assertEquals("Number of problems", messages.length, nMatch);
	}
	protected boolean hasJRE18() {
		return ((AbstractCompilerTest.getPossibleComplianceLevels() & AbstractCompilerTest.F_1_8) != 0);
	}

	String readFully(IFile file) throws IOException, CoreException {
		try (BufferedInputStream bs = new BufferedInputStream(file.getContents())) {
			int available = 0;
			StringBuilder buf = new StringBuilder();
			while ((available = bs.available()) > 0) {
				byte[] contents = new byte[available];
				bs.read(contents);
				buf.append(new String(contents));
			}
			return buf.toString();
		}
	}

	/** Perform full build. */
	public void test1FullBuild() throws Exception {
		setupJavaProject("Test1");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/MyMap.java",
				MY_MAP_CONTENT
			}, null);
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		assertNoMarkers(markers);
	}

	/** Perform full build, annotations are found relative to a variable. */
	public void test1FullBuildWithVariable() throws Exception {
		setupJavaProject("Test1");
		JavaCore.setClasspathVariable("MY_PRJ_ROOT", this.project.getProject().getLocation(), null);
		try {
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "MY_PRJ_ROOT/annots", new String[] {
					"/UnannotatedLib/libs/MyMap.java",
					MY_MAP_CONTENT
				}, null);
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			assertNoMarkers(markers);
		} finally {
			JavaCore.removeClasspathVariable("MY_PRJ_ROOT", null);
		}
	}

	/** Reconcile an individual CU. */
	public void test1Reconcile() throws Exception {
		setupJavaProject("Test1");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/MyMap.java",
				MY_MAP_CONTENT
			}, null);
		IPackageFragment fragment = this.root.getPackageFragment("test1");
		ICompilationUnit unit = fragment.getCompilationUnit("Test1.java").getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}

	public void testLibs1() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"import java.util.Collection;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	<T> Iterator<T> unconstrainedTypeArguments1(Collection<T> in);\n" +
				"	Iterator<String> unconstrainedTypeArguments2(Collection<String> in);\n" +
				"	<T> Iterator<? extends T> constrainedWildcards(Collection<? extends T> in);\n" +
				"	<T extends Collection<?>> T constrainedTypeParameter(T in);\n" +
				"}\n"
			}, null);
		// annotations on type variables & class type in various positions:
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"unconstrainedTypeArguments1\n" +
				" <T:Ljava/lang/Object;>(Ljava/util/Collection<TT;>;)Ljava/util/Iterator<TT;>;\n" +
				" <T:Ljava/lang/Object;>(Ljava/util/Collection<T0T;>;)Ljava/util/Iterator<TT;>;\n" +  // position: type argument
				"\n" +
				"unconstrainedTypeArguments2\n" +
				" (Ljava/util/Collection<Ljava/lang/String;>;)Ljava/util/Iterator<Ljava/lang/String;>;\n" +
				" (Ljava/util/Collection<Ljava/lang/String;>;)Ljava/util/Iterator<L1java/lang/String;>;\n" + // position: type argument bound (class type)
				"constrainedWildcards\n" +
				" <T:Ljava/lang/Object;>(Ljava/util/Collection<+TT;>;)Ljava/util/Iterator<+TT;>;\n" +
				" <T:Ljava/lang/Object;>(Ljava/util/Collection<+T0T;>;)Ljava/util/Iterator<+T1T;>;\n" + // positions: wildcard bound
				"constrainedTypeParameter\n" +
				" <T::Ljava/util/Collection<*>;>(TT;)TT;\n" +
				" <T::Ljava/util/Collection<*>;>(T0T;)T1T;\n"); // position: top-level type
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import java.util.Collection;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	Iterator<@NonNull String> test1(Lib1 lib, Collection<@Nullable String> coll) {\n" +
				"		return lib.unconstrainedTypeArguments1(coll);\n" +
				"	}\n" +
				"	Iterator<@NonNull String> test2(Lib1 lib, Collection<@Nullable String> coll) {\n" +
				"		return lib.unconstrainedTypeArguments2(coll);\n" +
				"	}\n" +
				"	Iterator<? extends @NonNull String> test3(Lib1 lib, Collection<String> coll) {\n" +
				"		return lib.constrainedWildcards(coll);\n" +
				"	}\n" +
				"	@NonNull Collection<String> test4(Lib1 lib, @Nullable Collection<String> in) {\n" +
				"		return lib.constrainedTypeParameter(in);\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}

	public void testLibsWithWildcards() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"import java.util.Collection;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	Iterator<?> unconstrainedWildcard1(Collection<?> in);\n" +
				"	Iterator<?> unconstrainedWildcard2(Collection<?> in);\n" +
				"	Iterator<? extends CharSequence> constrainedWildcard1(Collection<? extends CharSequence> in);\n" +
				"	Iterator<? super CharSequence> constrainedWildcard2(Collection<? super CharSequence> in);\n" +
				"}\n"
			}, null);
		// annotations directly on a wildcard (*, +, -)
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"unconstrainedWildcard1\n" +
				" (Ljava/util/Collection<*>;)Ljava/util/Iterator<*>;\n" +
				" (Ljava/util/Collection<*>;)Ljava/util/Iterator<*1>;\n" +
				"\n" +
				"unconstrainedWildcard2\n" +
				" (Ljava/util/Collection<*>;)Ljava/util/Iterator<*>;\n" +
				" (Ljava/util/Collection<*>;)Ljava/util/Iterator<*0>;\n" +
				"\n" +
				"constrainedWildcard1\n" +
				" (Ljava/util/Collection<+Ljava/lang/CharSequence;>;)Ljava/util/Iterator<+Ljava/lang/CharSequence;>;\n" +
				" (Ljava/util/Collection<+Ljava/lang/CharSequence;>;)Ljava/util/Iterator<+0Ljava/lang/CharSequence;>;\n" +
				"\n" +
				"constrainedWildcard2\n" +
				" (Ljava/util/Collection<-Ljava/lang/CharSequence;>;)Ljava/util/Iterator<-Ljava/lang/CharSequence;>;\n" +
				" (Ljava/util/Collection<-Ljava/lang/CharSequence;>;)Ljava/util/Iterator<-0Ljava/lang/CharSequence;>;\n" +
				"\n" +
				"\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import java.util.Collection;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull Object test1(Lib1 lib, Collection<@Nullable String> coll) {\n" +
				"		 return lib.unconstrainedWildcard1(coll).next();\n" + // OK
				"	}\n" +
				"	@NonNull Object test2(Lib1 lib, Collection<@Nullable String> coll) {\n" +
				"		 return lib.unconstrainedWildcard2(coll).next();\n" + // return is nullable -> error
				"	}\n" +
				"	@NonNull CharSequence test3(Lib1 lib, Collection<@Nullable String> coll) {\n" +
				"		 return lib.constrainedWildcard1(coll).next();\n" + // '@Nullable ? extends CharSequence' -> error
				"	}\n" +
				"	@NonNull Object test4(Lib1 lib, Collection<@Nullable CharSequence> coll) {\n" +
				"		 return lib.constrainedWildcard2(coll).next();\n" + // return is '@Nullable ? super CharSequence' -> error
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
				"Pb(980) Unsafe interpretation of method return type as '@NonNull' based on the receiver type 'Iterator<@NonNull capture#of ?>'. Type 'Iterator<E>' doesn't seem to be designed with null type annotations in mind",
				"Pb(953) Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable capture#of ?'",
				"Pb(953) Null type mismatch (type annotations): required '@NonNull CharSequence' but this expression has type '@Nullable capture#of ? extends CharSequence'",
				"Pb(953) Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable capture#of ? super CharSequence'"
			}, new int[] { 10, 13, 16, 19 });
	}

	public void testLibsWithArrays() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"import java.util.Collection;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	String[] constraintArrayTop(String[] in);\n" +
				"	String[] constraintArrayFull(String[] in);\n" +
				"	String[][] constraintDeep(String[][] in);\n" +
				"}\n"
			}, null);
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"constraintArrayTop\n" +
				" ([Ljava/lang/String;)[Ljava/lang/String;\n" +
				" ([0Ljava/lang/String;)[1Ljava/lang/String;\n" +
				"\n" +
				"constraintArrayFull\n" +
				" ([Ljava/lang/String;)[Ljava/lang/String;\n" +
				" ([0L0java/lang/String;)[1L1java/lang/String;\n" +
				"\n" +
				"constraintDeep\n" +
				" ([[Ljava/lang/String;)[[Ljava/lang/String;\n" +
				" ([0[1L0java/lang/String;)[1[0L1java/lang/String;\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	String @NonNull[] test1(Lib1 lib, String @Nullable[] ok, String[] nok) {\n" +
				"		lib.constraintArrayTop(nok);\n" +
				"		return lib.constraintArrayTop(ok);\n" +
				"	}\n" +
				"	@NonNull String @NonNull[] test2(Lib1 lib, @Nullable String @Nullable[] ok, String[] nok) {\n" +
				"		lib.constraintArrayFull(nok);\n" +
				"		return lib.constraintArrayFull(ok);\n" +
				"	}\n" +
				"	@NonNull String @NonNull[] @Nullable[] test3(Lib1 lib, @Nullable String @Nullable[] @NonNull[] ok, String[][] nok) {\n" +
				"		lib.constraintDeep(nok);\n" +
				"		return lib.constraintDeep(ok);\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(986) Null type safety (type annotations): The expression of type 'String[]' needs unchecked conversion to conform to '@Nullable String @Nullable[]'",
			"Pb(986) Null type safety (type annotations): The expression of type 'String[][]' needs unchecked conversion to conform to '@Nullable String @Nullable[] @NonNull[]'",
		}, new int[] { 12, 16 });
	}

	public void testLibsWithFields() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	String one = \"1\";\n" +
				"	String none = null;\n" +
				"}\n"
			}, null);
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"one\n" +
				" Ljava/lang/String;\n" +
				" L1java/lang/String;\n" +
				"\n" +
				"none\n" +
				" Ljava/lang/String;\n" +
				" L0java/lang/String;\n" +
				"\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test0() {\n" +
				"		return Lib1.none;\n" +
				"	}\n" +
				"	@NonNull String test1() {\n" +
				"		return Lib1.one;\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(953) Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'",
		}, new int[] { 8 });
	}

	public void testLibsWithFieldsZipped() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots.zip", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	String one = \"1\";\n" +
				"	String none = null;\n" +
				"}\n"
			}, null);
		Util.createSourceZip(
			new String[] {
				"libs/Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"one\n" +
				" Ljava/lang/String;\n" +
				" L1java/lang/String;\n" +
				"\n" +
				"none\n" +
				" Ljava/lang/String;\n" +
				" L0java/lang/String;\n" +
				"\n"
			},
			this.project.getProject().getLocation().toString()+"/annots.zip");
	    this.project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test0() {\n" +
				"		return Lib1.none;\n" +
				"	}\n" +
				"	@NonNull String test1() {\n" +
				"		return Lib1.one;\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(953) Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'",
		}, new int[] { 8 });
	}

	public void testLibsWithFieldsExternalZipped() throws Exception {
		myCreateJavaProject("TestLibs");
		String zipPath = Util.getOutputDirectory() + '/' + "annots.zip";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", zipPath, new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	String one = \"1\";\n" +
				"	String none = null;\n" +
				"}\n"
			}, null);
		Util.createSourceZip(
			new String[] {
				"libs/Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"one\n" +
				" Ljava/lang/String;\n" +
				" L1java/lang/String;\n" +
				"\n" +
				"none\n" +
				" Ljava/lang/String;\n" +
				" L0java/lang/String;\n" +
				"\n"
			},
			zipPath);

		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test0() {\n" +
				"		return Lib1.none;\n" +
				"	}\n" +
				"	@NonNull String test1() {\n" +
				"		return Lib1.one;\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(953) Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'",
		}, new int[] { 8 });
	}

	public void testLibsWithTypeParameters() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1<U,V,W extends U> {\n" +
				"	U getU();\n" +
				"	V getV();\n" +
				"	W getW();\n" +
				"	<X,Y extends CharSequence> Y fun(X x);\n" +
				"}\n"
			}, null);
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				" <U:Ljava/lang/Object;V:Ljava/lang/Object;W:TU;>\n" +
				" <0U:Ljava/lang/Object;1V:Ljava/lang/Object;W:T1U;>\n" +
				"\n" +
				"fun\n" +
				" <X:Ljava/lang/Object;Y::Ljava/lang/CharSequence;>(TX;)TY;\n" +
				" <1X:Ljava/lang/Object;Y::L1java/lang/CharSequence;>(TX;)TY;\n" +
				"\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test0(Lib1<@Nullable String,@NonNull String,@NonNull String> l) {\n" +
				"		return l.getU();\n" + // mismatch: U is nullable
				"	}\n" +
				"	@NonNull String test1(Lib1<@Nullable String,@NonNull String,@NonNull String> l) {\n" +
				"		return l.getV();\n" + // OK: V is nonnull
				"	}\n" +
				"	@NonNull String test2(Lib1<@Nullable String,@NonNull String,@NonNull String> l) {\n" +
				"		return l.getW();\n" + // OK: V is nonnull
				"	}\n" +
				"	Lib1<@NonNull String, @NonNull String, @NonNull String> f1;\n" + // mismatch at U
				"	Lib1<@Nullable String, String, @NonNull String> f2;\n" + // mismatch at V
				"	Lib1<@Nullable String, @NonNull String, @Nullable String> f3;\n" + // mismatch at W
				"	@Nullable String test3(Lib1<@Nullable String,@NonNull String,@NonNull String> l) {\n" +
				"		return l.<@Nullable String,@Nullable String>fun(\"\");\n" + // mismatches at X and Y
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(953) Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'",
			"Pb(964) Null constraint mismatch: The type '@NonNull String' is not a valid substitute for the type parameter '@Nullable U extends Object'",
			"Pb(964) Null constraint mismatch: The type 'String' is not a valid substitute for the type parameter '@NonNull V extends Object'",
			"Pb(964) Null constraint mismatch: The type '@Nullable String' is not a valid substitute for the type parameter 'W extends @NonNull U extends Object'",
			"Pb(964) Null constraint mismatch: The type '@Nullable String' is not a valid substitute for the type parameter '@NonNull X extends Object'",
			"Pb(964) Null constraint mismatch: The type '@Nullable String' is not a valid substitute for the type parameter 'Y extends @NonNull CharSequence'",
		}, new int[] { 8, 16, 17, 18, 20, 20 });
	}

	public void testLibsWithTypeArgOfSuper() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/LibSuper.java",
				"package libs;\n" +
				"\n" +
				"public interface LibSuper<T,U> {\n" +
				"	U apply(T t);\n" +
				"}\n",
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1 extends LibSuper<String,Exception> {\n" +
				"}\n"
			}, null);
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"super libs/LibSuper\n" +
				" <Ljava/lang/String;Ljava/lang/Exception;>\n" +
				" <L1java/lang/String;L0java/lang/Exception;>\n" +
				"\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull Exception test0(Lib1 lib, @Nullable String str) {\n" +
				"		return lib\n" +
				"				.apply(str);\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(953) Null type mismatch (type annotations): required '@NonNull Exception' but this expression has type '@Nullable Exception'",
			"Pb(953) Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'",
		}, new int[] { 8, 9 });
	}

	/** Project with real JRE. */
	public void test2() throws Exception {
		// library type used: j.u.Map (no need for JRE8)
		Hashtable options = JavaCore.getOptions();
		try {
			setupJavaProject("Test2");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			assertNoMarkers(markers);
		} finally {
			// project using a full JRE container initializes global options to 1.8 -- must reset now:
			JavaCore.setOptions(options);
		}
	}

	/** Project with real JRE8.
	 * More interesting work with generics
	 * .classpath uses var TESTWORK for path to external annotations.
	 */
	public void test3() throws Exception {
		if (!hasJRE18()) {
			System.out.println("Skipping ExternalAnnotations18Test.test3(), needs JRE8");
			return;
		}
		final String TESTWORK_VAR_NAME = "TESTWORK";
		JavaCore.setClasspathVariable(TESTWORK_VAR_NAME, new Path(getSourceWorkspacePath()), null);
		Hashtable options = JavaCore.getOptions();
		try {
			setupJavaProject("Test3");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			assertNoMarkers(markers);
		} finally {
			// project using a full JRE container initializes global options to 1.8 -- must reset now:
			JavaCore.setOptions(options);
			JavaCore.removeClasspathVariable(TESTWORK_VAR_NAME, null);
		}
	}

	// ===== Full round trip: detect problem - annotated - detect problem change =====

	public void testAnnotateFieldWithParameterizedType() throws Exception {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"public abstract class Lib1<T> {\n" +
				"	public Lib1<T> one;\n" +
				"	public abstract T get();\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				lib1Content
			}, null);

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test0(Lib1<String> stringLib) {\n" +
				"		return stringLib.one.get();\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(986) Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to '@NonNull String'",
		}, new int[] { 8 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("one");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		IVariableBinding fieldBinding = (IVariableBinding) ((SimpleName)name).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, fieldBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericTypeSignature(fieldBinding.getVariableDeclaration().getType());
		ExternalAnnotationUtil.annotateMember("libs/Lib1", annotationFile,
				"one",
				originalSignature,
				"Llibs/Lib1<T0T;>;",
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is even worse now:
		reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(953) Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'",
		}, new int[] { 8 });
	}


	public void testAnnotateMethodParameter() throws Exception {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"public abstract class Lib1<T,U> {\n" +
				"	public abstract void take(Lib1<X,U> lx);\n" +
				"	public static class X {}\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				lib1Content
			}, null);

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test0(Lib1<Lib1.X,@Nullable String> xLib1, Lib1<Lib1.@Nullable X,@NonNull String> xLib2) {\n" +
				"		xLib1.take(xLib2);\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(953) Null type mismatch (type annotations): required 'Lib1<Lib1.X,@Nullable String>' but this expression has type 'Lib1<Lib1.@Nullable X,@NonNull String>'",
		}, new int[] { 7 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("take");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		ExternalAnnotationUtil.annotateMember("libs/Lib1", annotationFile,
				"take",
				originalSignature,
				"(Llibs/Lib1<L0libs/Lib1$X;T1U;>;)V", // <- two annotations: @Nullable X and @NonNull U
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is resolved now:
		reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());
	}


	public void testAnnotateConstructorParameter() throws Exception {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"public class Lib1<U> {\n" +
				"	public Lib1(int ignore, U string) {}\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				lib1Content
			}, null);

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	Object test0() {\n" +
				"		Lib1<@NonNull String> lib = new Lib1<>(1, null);\n" +
				"		return lib;\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(910) Null type mismatch: required '@NonNull String' but the provided value is null",
		}, new int[] { 7 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find method binding:
		int start = lib1Content.indexOf("U string");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent().getParent().getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		ExternalAnnotationUtil.annotateMember("libs/Lib1", annotationFile,
				"<init>",
				originalSignature,
				"(IT0U;)V", // <- @Nullable U
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is resolved now:
		reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());

		// invert annotation:
		ExternalAnnotationUtil.annotateMethodParameterType("libs/Lib1", annotationFile,
				"<init>",
				originalSignature,
				"T1U;", // <- @NonNull U
				1, // position
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is back now:
		reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(910) Null type mismatch: required '@NonNull String' but the provided value is null",
		}, new int[] { 7 });

		// check that the previous entry has been overwritten:
		assertEquals(
				"class libs/Lib1\n" +
				"<init>\n" +
				" (ITU;)V\n" +
				" (IT1U;)V\n",
				readFully(annotationFile));
	}

	public void testAnnotateMethodTypeParameter1() throws Exception {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public abstract class Lib1 {\n" +
				"	public abstract <S extends Throwable, T, U extends List<T>, V> U method(S s, T t, V v);\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				lib1Content
			}, null);

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull Object test0(Lib1 xLib1) {\n" +
				"		return xLib1.method(\n" +
				"				(Error) null, this, xLib1);\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(981) Unsafe interpretation of method return type as '@NonNull' based on substitution 'U=@NonNull List<Test1>'. Declaring type 'Lib1' doesn't seem to be designed with null type annotations in mind",
		}, new int[] { 7 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find method binding:
		int start = lib1Content.indexOf("method");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		ExternalAnnotationUtil.annotateMethodTypeParameter("libs/Lib1", annotationFile,
				"method",
				originalSignature,
				"1U::Ljava/util/List<TT;>;", // <- @NonNull U
				2, // annotate 3rd type parameter (U)
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is resolved now:
		reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());

		// add one more annotation:
		ExternalAnnotationUtil.annotateMethodTypeParameter("libs/Lib1", annotationFile,
				"method",
				originalSignature,
				"1S:Ljava/lang/Throwable;", // <- @NonNull S
				0, // annotate 1st type parameter (S)
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);

		// check that we have a new error now:
		reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(910) Null type mismatch: required '@NonNull Error' but the provided value is null",
		}, new int[] { 8 });

		assertEquals("file content",
				"class libs/Lib1\n" +
				"method\n" +
				" <S:Ljava/lang/Throwable;T:Ljava/lang/Object;U::Ljava/util/List<TT;>;V:Ljava/lang/Object;>(TS;TT;TV;)TU;\n" +
				" <1S:Ljava/lang/Throwable;T:Ljava/lang/Object;1U::Ljava/util/List<TT;>;V:Ljava/lang/Object;>(TS;TT;TV;)TU;\n",
				readFully(annotationFile));
	}

	public void testAnnotateMethodTypeParameter2() throws Exception {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public abstract class Entry<KK,VV> {\n" +
				"	 public static <K, V extends Comparable<? super V>> Comparator<Entry<K,V>> comparingByValue() { return null; }\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Comparator.java",
				"package libs;\n" +
				"public class Comparator<T> {}\n",
				"/UnannotatedLib/libs/Entry.java",
				lib1Content
			}, null);

		// acquire library AST:
		IType type = this.project.findType("libs.Entry");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find method binding:
		int start = lib1Content.indexOf("comparingByValue");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Entry.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		// preview:
		String[] annotatedSign = ExternalAnnotationUtil.annotateTypeParameter(
				originalSignature,
				"1K:Ljava/lang/Object;",  // <- @NonNull K
				0,
				MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result", "[<, " +
				"K:Ljava/lang/Object;, " +
				"1K:Ljava/lang/Object;, " +  // <- K
				"V::Ljava/lang/Comparable<-TV;>;>()Llibs/Comparator<Llibs/Entry<TK;TV;>;>;]",
				Arrays.toString(annotatedSign));
		// perform:
		ExternalAnnotationUtil.annotateMethodTypeParameter("libs/Entry", annotationFile,
				"comparingByValue",
				originalSignature,
				"1K:Ljava/lang/Object;", // <- @NonNull K
				0, // annotate 1st type parameter (K)
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import libs.*;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	Object test0() {\n" +
				"		Comparator<Entry<@Nullable String,String>> c = Entry.comparingByValue();\n" +
				"		return c;\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(953) Null type mismatch (type annotations): required 'Comparator<Entry<@Nullable String,String>>' but this expression has type 'Comparator<Entry<@NonNull String,String>>'",
		}, new int[] { 7 });

		// un-annotate:
		annotatedSign = ExternalAnnotationUtil.annotateTypeParameter(
				originalSignature,
				"@K:Ljava/lang/Object;",  // <- <del>1</del> K
				0, MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result", "[<, " +
				"K:Ljava/lang/Object;, " +
				"K:Ljava/lang/Object;, " +  // <- K
				"V::Ljava/lang/Comparable<-TV;>;>()Llibs/Comparator<Llibs/Entry<TK;TV;>;>;]",
				Arrays.toString(annotatedSign));
	}

	public void testAnnotateClassTypeParameter1() throws Exception {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public abstract class Lib1 <S extends Throwable, T, U extends List<T>, V> {\n" +
				"	public abstract U method(S s, T t, V v);\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				lib1Content
			}, null);

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import libs.Lib1;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull List<Test1> test0(Lib1<Error,Test1,@NonNull List<Test1>,String> xLib1) {\n" +
				"		return xLib1.method(\n" +
				"				(Error) null, this, \"1\");\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(980) Unsafe interpretation of method return type as '@NonNull' based on the receiver type 'Lib1<Error,Test1,@NonNull List<Test1>,String>'. Type 'Lib1<S,T,U,V>' doesn't seem to be designed with null type annotations in mind",
		}, new int[] { 8 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("Lib1");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode typeDecl = name.getParent();
		ITypeBinding typeBinding = ((TypeDeclaration)typeDecl).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, typeBinding, null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericTypeParametersSignature(typeBinding);
		ExternalAnnotationUtil.annotateTypeTypeParameter("libs/Lib1", annotationFile,
				originalSignature,
				"1U::Ljava/util/List<TT;>;", // <- @NonNull U
				2, // annotate 3rd type parameter (U)
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is resolved now:
		reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());

		// add one more annotation:
		ExternalAnnotationUtil.annotateTypeTypeParameter("libs/Lib1", annotationFile,
				originalSignature,
				"1S:Ljava/lang/Throwable;", // <- @NonNull S
				0, // annotate 1st type parameter (S)
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);

		// check that we have a new error now:
		reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(964) Null constraint mismatch: The type 'Error' is not a valid substitute for the type parameter '@NonNull S extends Throwable'",
				"Pb(910) Null type mismatch: required '@NonNull Error' but the provided value is null",
		}, new int[] { 7, 9 });

		assertEquals("file content",
				"class libs/Lib1\n" +
				" <S:Ljava/lang/Throwable;T:Ljava/lang/Object;U::Ljava/util/List<TT;>;V:Ljava/lang/Object;>\n" +
				" <1S:Ljava/lang/Throwable;T:Ljava/lang/Object;1U::Ljava/util/List<TT;>;V:Ljava/lang/Object;>\n",
				readFully(annotationFile));
	}

	// ===== white box tests for ExternalAnnotationUtil =====

	public void testBug470666a() throws CoreException, IOException {
		myCreateJavaProject("TestAnnot");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"interface Function<T,U> {}\n" +
				"interface Collector<T,A,R> {}\n" +
				"public class Collectors {\n" +
				"	 public static <T, U, A, R>\n" +
				"    Collector<T, ?, R> mapping(Function<? super T, ? extends U> mapper,\n" +
				"                               Collector<? super U, A, R> downstream) { return null; }\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Collectors.java",
				lib1Content
			}, null);

		// acquire library AST:
		IType type = this.project.findType("libs.Collectors");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("T, ? extends U>"); // bound of type param of method param
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		while (!(method instanceof MethodDeclaration))
			method = method.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestAnnot/annots/libs/Collectors.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		String[] annotatedSign = ExternalAnnotationUtil.annotateParameterType(
				originalSignature,
				"Llibs/Function<-T1T;+TU;>;",  // <- @NonNull T
				0, MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result", "[<T:Ljava/lang/Object;U:Ljava/lang/Object;A:Ljava/lang/Object;R:Ljava/lang/Object;>(, " +
				"Llibs/Function<-TT;+TU;>;, " +
				"Llibs/Function<-T1T;+TU;>;, " +  // <- @NonNull T
				"Llibs/Collector<-TU;TA;TR;>;)Llibs/Collector<TT;*TR;>;]",
				Arrays.toString(annotatedSign));
	}

	public void testBug470666b() throws CoreException, IOException {
		myCreateJavaProject("TestAnnot");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"interface Function<T,U> {}\n" +
				"interface Collector<T,A,R> {}\n" +
				"public class Collectors {\n" +
				"	 public static <T, U, A, R>\n" +
				"    Collector<T, ?, R> mapping(Function<? super T, ? extends U> mapper,\n" +
				"                               Collector<? super U, A, R> downstream) { return null; }\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Collectors.java",
				lib1Content
			}, null);

		// acquire library AST:
		IType type = this.project.findType("libs.Collectors");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("T, ?, R>"); // bound of return type
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		while (!(method instanceof MethodDeclaration))
			method = method.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestAnnot/annots/libs/Collectors.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		String[] annotatedSign = ExternalAnnotationUtil.annotateReturnType(
				originalSignature,
				"Llibs/Collector<T1T;*TR;>;",  // <- @NonNull T
				MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result", "[<T:Ljava/lang/Object;U:Ljava/lang/Object;A:Ljava/lang/Object;R:Ljava/lang/Object;>(Llibs/Function<-TT;+TU;>;Llibs/Collector<-TU;TA;TR;>;), " +
				"Llibs/Collector<TT;*TR;>;, " +
				"Llibs/Collector<T1T;*TR;>;, " +  // <- @NonNull T
				"]",
				Arrays.toString(annotatedSign));
	}

	public void testBug464081() throws CoreException, IOException {
		myCreateJavaProject("TestAnnot");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"interface List<T> {}\n" +
				"public class Collections {\n" +
				"	 public static <T> List<T> unmodifiableList(List<? extends T> list) { return null; }\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Collections.java",
				lib1Content
			}, null);

		// acquire library AST:
		IType type = this.project.findType("libs.Collections");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("List<? extends T>");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		while (!(method instanceof MethodDeclaration))
			method = method.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestAnnot/annots/libs/Collections.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		String[] annotatedSign = ExternalAnnotationUtil.annotateParameterType(
				originalSignature,
				"Llibs/List<+T1T;>;",  // <- @NonNull T
				0, MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result",
				"[<T:Ljava/lang/Object;>(, " +
				"Llibs/List<+TT;>;, " +
				"Llibs/List<+T1T;>;, " +  // <- @NonNull T
				")Llibs/List<TT;>;]",
				Arrays.toString(annotatedSign));
	}

	public void testBug471352() throws CoreException, IOException {
		myCreateJavaProject("TestAnnot");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"interface List<T> {}\n" +
				"class Random {}\n" +
				"public class Collections {\n" +
				"	 public static void shuffle(List<?> list, Random rnd) { }\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Collections.java",
				lib1Content
			}, null);

		// acquire library AST:
		IType type = this.project.findType("libs.Collections");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("Random rnd");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		while (!(method instanceof MethodDeclaration))
			method = method.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestAnnot/annots/libs/Collections.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		String[] annotatedSign = ExternalAnnotationUtil.annotateParameterType(
				originalSignature,
				"L1libs/Random;",  // <- @NonNull Random
				1, MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result",
				"[(Llibs/List<*>;, " +
				"Llibs/Random;, " +
				"L1libs/Random;, " +  // <- @NonNull Random
				")V]",
				Arrays.toString(annotatedSign));
	}

	// array content
	public void testBug471034a() throws CoreException, IOException {
		myCreateJavaProject("TestAnnot");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"interface List<T> {}\n" +
				"class Random {}\n" +
				"public class Thread {\n" +
				"	 public static int enumerate(Thread tarray[]) { return 1; }\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Thread.java",
				lib1Content
			}, null);

		// acquire library AST:
		IType type = this.project.findType("libs.Thread");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("Thread tarray[]");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		while (!(method instanceof MethodDeclaration))
			method = method.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestAnnot/annots/libs/Thread.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		String[] annotatedSign = ExternalAnnotationUtil.annotateParameterType(
				originalSignature,
				"[L1libs/Thread;",  // <- @NonNull Thread
				0, MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result",
				"[(, " +
				"[Llibs/Thread;, " +
				"[L1libs/Thread;, " +  // <- @NonNull Thread
				")I]",
				Arrays.toString(annotatedSign));
	}

	// array dimension
	public void testBug471034b() throws CoreException, IOException {
		myCreateJavaProject("TestAnnot");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"interface List<T> {}\n" +
				"class Random {}\n" +
				"public class Thread {\n" +
				"	 public static int enumerate(Thread tarray[][]) { return 1; }\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Thread.java",
				lib1Content
			}, null);

		// acquire library AST:
		IType type = this.project.findType("libs.Thread");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("[][]");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be dimension", name.getNodeType() == ASTNode.DIMENSION);
		ASTNode method = name.getParent();
		while (!(method instanceof MethodDeclaration))
			method = method.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestAnnot/annots/libs/Thread.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		String[] annotatedSign = ExternalAnnotationUtil.annotateParameterType(
				originalSignature,
				"[1[Llibs/Thread;",  // <- @NonNull array
				0, MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result",
				"[(, " +
				"[[Llibs/Thread;, " +
				"[1[Llibs/Thread;, " +  // <- @NonNull array
				")I]",
				Arrays.toString(annotatedSign));
	}

	// varargs
	public void testBug471034c() throws CoreException, IOException {
		myCreateJavaProject("TestAnnot");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"interface List<T> {}\n" +
				"class Random {}\n" +
				"public class Thread {\n" +
				"	 public static int enumerate(Thread ... tarray) { return 1; }\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Thread.java",
				lib1Content
			}, null);

		// acquire library AST:
		IType type = this.project.findType("libs.Thread");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("...");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be variable", name.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION);
		ASTNode method = name.getParent();
		while (!(method instanceof MethodDeclaration))
			method = method.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestAnnot/annots/libs/Thread.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		String[] annotatedSign = ExternalAnnotationUtil.annotateParameterType(
				originalSignature,
				"[1Llibs/Thread;",  // <- @NonNull array
				0, MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result",
				"[(, " +
				"[Llibs/Thread;, " +
				"[1Llibs/Thread;, " +  // <- @NonNull array
				")I]",
				Arrays.toString(annotatedSign));
	}

	public void testBrokenConfig1() throws Exception {
		LogListener listener = new LogListener();
		try {
			Platform.addLogListener(listener);

			myCreateJavaProject("TestBrokenConfig1");
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "/NoProject", new String[] {
					"/UnannotatedLib/libs/Lib1.java",
					"package libs;\n" +
					"\n" +
					"import java.util.Collection;\n" +
					"import java.util.Iterator;\n" +
					"\n" +
					"public interface Lib1 {\n" +
					"	<T> Iterator<T> unconstrainedTypeArguments1(Collection<T> in);\n" +
					"	Iterator<String> unconstrainedTypeArguments2(Collection<String> in);\n" +
					"	<T> Iterator<? extends T> constrainedWildcards(Collection<? extends T> in);\n" +
					"	<T extends Collection<?>> T constrainedTypeParameter(T in);\n" +
					"}\n",
					"/UnannotatedLib/libs/Lib2.java",
					"package libs;\n" +
					"public interface Lib2 {\n" +
					"	String test(String s);\n" +
					"}\n"
				}, null);
			IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
			ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import java.util.Collection;\n" +
					"import java.util.Iterator;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	Iterator<@NonNull String> test1(Lib1 lib, Collection<@Nullable String> coll) {\n" +
					"		return lib.unconstrainedTypeArguments1(coll);\n" +
					"	}\n" +
					"	Iterator<@NonNull String> test2(Lib1 lib, Collection<@Nullable String> coll) {\n" +
					"		return lib.unconstrainedTypeArguments2(coll);\n" +
					"	}\n" +
					"	Iterator<? extends @NonNull String> test3(Lib1 lib, Collection<String> coll) {\n" +
					"		return lib.constrainedWildcards(coll);\n" +
					"	}\n" +
					"	@NonNull Collection<String> test4(Lib1 lib, @Nullable Collection<String> in) {\n" +
					"		return lib.constrainedTypeParameter(in);\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertEquals("number of problems", 4, problems.length);

			// second class to test if problem is logged more than once
			ICompilationUnit unit2 = fragment.createCompilationUnit("Test2.java",
					"package tests;\n" +
					"import libs.Lib2;\n" +
					"\n" +
					"public class Test2 {\n" +
					"	void test1(Lib2 lib) {\n" +
					"		lib.test(null);\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled2 = unit2.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			assertNoProblems(reconciled2.getProblems());

			assertEquals("number of log entries", 0, listener.loggedStatus.size());
		} finally {
			Platform.removeLogListener(listener);
		}
	}

	/** Lib exists as workspace project. Perform full build. */
	public void testProjectDependencyFullBuild() throws Exception {
		try {
			setupJavaProject("Lib");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			setupJavaProject("Test1");
			addProjectDependencyWithExternalAnnotations(this.project, "/Lib", "annots", null);
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			assertNoMarkers(markers);
		} finally {
			deleteProject("Lib");
		}
	}

	/** Lib exists as workspace project. Reconcile an individual CU. */
	public void testProjectDependencyReconcile1() throws Exception {
		try {
			setupJavaProject("Lib");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			this.root = null; // prepare to get the root from project Test1

			setupJavaProject("Test1");
			addProjectDependencyWithExternalAnnotations(this.project, "/Lib", "annots", null);
			IPackageFragment fragment = this.root.getPackageFragment("test1");
			ICompilationUnit unit = fragment.getCompilationUnit("Test1.java").getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertNoProblems(problems);
		} finally {
			deleteProject("Lib");
		}
	}

	/** Lib exists as workspace project. Type-Annotations in zip file. Reconcile an individual CU. */
	public void testProjectDependencyReconcile2() throws Exception {
		try {
			setupJavaProject("Lib");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			this.root = null; // prepare to get the root from project Test1

			setupJavaProject("Test3b");
			Util.createSourceZip(
				new String[] {
					"libs/MyFunction.eea",
					"class libs/MyFunction\n" +
					" <T:R:>\n" +
					"\n" +
					"compose\n" +
					" <V:Ljava/lang/Object;>(Llibs/MyFunction<-TV;+TT;>;)Llibs/MyFunction<TV;TR;>;\n" +
					" <V:Ljava/lang/Object;>(Llibs/MyFunction<-TV;+T0T;>;)Llibs/MyFunction<TV;TR;>;\n" +
					"\n",
					"libs/Arrays.eea",
					"class libs/Arrays\n" +
					"\n" +
					"array\n" +
					" [Ljava/lang/String;\n" +
					" [1L0java/lang/String;\n" +
					"\n" +
					"getArray\n" +
					" ()[[Ljava/lang/String;\n" +
					" ()[0[1L0java/lang/String;\n"
				},
				this.project.getProject().getLocation().toString()+"/annots.zip");
			this.project.getProject().refreshLocal(1, new NullProgressMonitor());

			addProjectDependencyWithExternalAnnotations(this.project, "/Lib", "annots.zip", null);
			IPackageFragment fragment = this.root.getPackageFragment("test1");
			ICompilationUnit unit = fragment.getCompilationUnit("Reconcile2.java").getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertNoProblems(problems);
		} finally {
			deleteProject("Lib");
		}
	}

	/** Lib exists as workspace project. Invocations conflict with type parameter constraints. Reconcile an individual CU. */
	public void testProjectDependencyReconcile3() throws Exception {
		try {
			setupJavaProject("Lib");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			this.root = null; // prepare to get the root from project Test1

			setupJavaProject("Test3b");
			this.project.setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);
			Util.createSourceZip(
				new String[] {
					"libs/MyFunction.eea",
					"class libs/MyFunction\n" +
					" <T:R:>\n" +
					" <T:1R:>\n" +
					"\n" +
					"compose\n" +
					" <V:Ljava/lang/Object;>(Llibs/MyFunction<-TV;+TT;>;)Llibs/MyFunction<TV;TR;>;\n" +
					" <1V:Ljava/lang/Object;>(Llibs/MyFunction<-TV;+TT;>;)Llibs/MyFunction<TV;TR;>;\n" +
					"\n",
				},
				this.project.getProject().getLocation().toString()+"/annots.zip");
			this.project.getProject().refreshLocal(1, new NullProgressMonitor());

			addProjectDependencyWithExternalAnnotations(this.project, "/Lib", "annots.zip", null);
			IPackageFragment fragment = this.root.getPackageFragment("test1");
			ICompilationUnit unit = fragment.getCompilationUnit("Reconcile3.java").getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			assertProblems(reconciled.getProblems(), new String[] {
					"Pb(964) Null constraint mismatch: The type '@Nullable B' is not a valid substitute for the type parameter '@NonNull R'",
					"Pb(964) Null constraint mismatch: The type '@Nullable String' is not a valid substitute for the type parameter '@NonNull V'",
			}, new int[] { 12, 17 });
		} finally {
			deleteProject("Lib");
		}
	}

	public void testFreeTypeVariableReturn() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1<T> {\n" +
				"	T get();\n" +
				"}\n"
			}, null);
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test0(Lib1<@Nullable String> lib) {\n" +
				"		return lib.get();\n" +
				"	}\n" +
				"	@NonNull String test1(Lib1<@NonNull String> lib) {\n" +
				"		return lib.get();\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(953) Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'",
				"Pb(980) Unsafe interpretation of method return type as '@NonNull' based on the receiver type 'Lib1<@NonNull String>'. Type 'Lib1<T>' doesn't seem to be designed with null type annotations in mind",
		}, new int[] { 8, 11 });
		// just mark that Lib1 now has null annotations:
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				" <T:Ljava/lang/Object;>\n" +
				" <T:Ljava/lang/Object;>\n" +
				"\n");
		reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(953) Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'",
		}, new int[] { 8 });
	}

	public void testFreeTypeVariableReturnSeverities() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1<T> {\n" +
				"	T get();\n" +
				"}\n"
			}, null);
		this.currentProject = this.project;
		addLibrary("lib2.jar", null, new String[] {
				"/UnanntatedLib2/libs/Lib2.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib2<T> {\n" +
				"	T get();\n" +
				"}\n"
		}, "1.8");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		fragment.createCompilationUnit("Lib3.java",
				"package tests;\n" +
				"public interface Lib3<T> {\n" +
				"	T get();\n" +
				"}\n",
				true, new NullProgressMonitor());
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"import libs.Lib2;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test1(Lib1<@NonNull String> lib) {\n" +
				"		return lib.get();\n" + // legacy, prepared for .eea but still not annotated (=> Warning)
				"	}\n" +
				"	@NonNull String test2(Lib2<@NonNull String> lib) {\n" +
				"		return lib.get();\n" + // legacy, not prepared for .eea (=> Info)
				"	}\n" +
				"	@NonNull String test3(Lib3<@NonNull String> lib) {\n" +
				"		return lib.get();\n" + // not legacy, is from the same project
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(980) Unsafe interpretation of method return type as '@NonNull' based on the receiver type 'Lib1<@NonNull String>'. Type 'Lib1<T>' doesn't seem to be designed with null type annotations in mind",
				"Pb(980) Unsafe interpretation of method return type as '@NonNull' based on the receiver type 'Lib2<@NonNull String>'. Type 'Lib2<T>' doesn't seem to be designed with null type annotations in mind",
		}, new int[] { 9, 12 }, new int[] { ProblemSeverities.Warning, ProblemSeverities.Info } );
	}

	public void testBug490343() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLibs/libs/Map.java",
				"package libs;\n" +
				"\n" +
				"interface Comparator<T> {}\n" +
				"interface Comparable<T> {}\n" +
				"\n" +
				"public interface Map<K, V> {\n" +
				"    interface Entry<K, V> {\n" +
				"        K getKey();\n" +
				"\n" +
				"        public static <K extends Comparable<? super K>, V> Comparator<Map.Entry<K, V>> comparingByKey() {\n" +
				"            throw new RuntimeException();\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
		}, null);
		createFileInProject("annots/libs", "Map$Entry.eea",
				"class libs/Map$Entry\n" +
				"comparingByKey\n" +
				 " <K::Llibs/Comparable<-TK;>;V:Ljava/lang/Object;>()Llibs/Comparator<Llibs/Map$Entry<TK;TV;>;>;\n" +
				 " <K::Llibs/Comparable<-TK;>;V:Ljava/lang/Object;>()L1libs/Comparator<Llibs/Map$Entry<TK;TV;>;>;\n"
		);
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test.java",
				"package tests;\n" +
				"\n" +
				"import libs.Map;\n" +
				"\n" +
				"public class Test {\n" +
				"    static boolean f() {\n" +
				"        if(Map.Entry.comparingByKey() == null) {\n" +
				"            return false;\n" +
				"        }\n" +
				"        return true;\n" +
				"    }\n" +
				"}\n" +
				"",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
				"Pb(149) Dead code"
			}, new int[] { 7 });
	}
	@SuppressWarnings("deprecation")
	public void testBug507256() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	void methodWithParamAfterWildcard(Class<?> c, Object s);\n" +
				"}\n"
			}, null);
		// annotations directly on a wildcard (*, +, -)
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"methodWithParamAfterWildcard\n" +
				" (Ljava/lang/Class<*>;Ljava/lang/Object;)V\n" +
				" (L1java/lang/Class<*>;L1java/lang/Object;)V\n" +
				"\n" +
				"\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1(Lib1 lib) {\n" +
				"		 lib.methodWithParamAfterWildcard(Object.class, null);\n" + // error, second param must not be null
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
				"Pb(910) Null type mismatch: required '@NonNull Object' but the provided value is null"
			}, new int[] { 6 });
	}

	/** assert that per-workspace configuration re rt.jar is overridden by per-project configuration for JRE container. */
	public void testBug465296() throws Exception {
		// library type used: j.u.Map (no need for JRE8)
		Hashtable options = JavaCore.getOptions();
		TestContainerInitializer.RT_JAR_ANNOTATION_PATH = "/MissingPrj/missing";
		try {
			setupJavaProject("Test2");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			assertNoMarkers(markers);
		} finally {
			// project using a full JRE container initializes global options to 1.8 -- must reset now:
			JavaCore.setOptions(options);
			TestContainerInitializer.RT_JAR_ANNOTATION_PATH = null;
		}
	}

	/**
	 * Assert that external annotations configured for project A's library are considered also while compiling dependent project B.
	 * Full build.
	 */
	public void testBug509715fullBuild() throws Exception {
		Hashtable options = JavaCore.getOptions();
		try {
			setupJavaProject("Bug509715ProjA");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			setupJavaProject("Bug509715ProjB");
			// local eea should not shadow those configured in ProjA:
			addProjectDependencyWithExternalAnnotations(this.project, "/Bug509715ProjA", "/Bug509715ProjB/eea", null);

			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			assertNoMarkers(markers);
		} finally {
			deleteProject("Bug509715ProjA");
			deleteProject("Bug509715ProjB");
			JavaCore.setOptions(options);
		}
	}

	/**
	 * Assert that external annotations configured for project A's library are considered also while compiling dependent project B.
	 * Reconcile.
	 */
	@SuppressWarnings("deprecation")
	public void testBug509715reconcile() throws Exception {
		try {
			setupJavaProject("Bug509715ProjA");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			setupJavaProject("Bug509715ProjB");
			// local eea should not shadow those configured in ProjA:
			addProjectDependencyWithExternalAnnotations(this.project, "/Bug509715ProjA", "/Bug509715ProjB/eea", null);

			IPackageFragment fragment = this.root.getPackageFragment("b");
			ICompilationUnit unit = fragment.getCompilationUnit("User.java").getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertNoProblems(problems);
		} finally {
			deleteProject("Bug509715ProjA");
			deleteProject("Bug509715ProjB");
		}
	}

	@SuppressWarnings("deprecation")
	public void testBug500024dir() throws CoreException, IOException {
		try {
			String projectName = "Bug500024";
			setupJavaProject(projectName, true, true);
			this.project.setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);

			addEeaToVariableEntry("JCL18_FULL", "/"+projectName+"/annots");
			IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("test1", true, null);
			ICompilationUnit unit = fragment.getCompilationUnit("Test1.java").getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems,
					new String[] {
						"Pb(980) Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull Map<@NonNull String,@NonNull Test1>\'. Type \'Map<K,V>\' doesn\'t seem to be designed with null type annotations in mind",
						"Pb(149) Dead code",
						"Pb(915) Illegal redefinition of parameter other, inherited method from Object declares this parameter as @Nullable"
					},
					new int[] {9, 11, 13},
					new int[] { ProblemSeverities.Warning, ProblemSeverities.Warning, ProblemSeverities.Error });

			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Markers after full build",
					"Dead code\n" +
					"Illegal redefinition of parameter other, inherited method from Object declares this parameter as @Nullable\n" +
					"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull Map<@NonNull String,@NonNull Test1>\'. Type \'Map<K,V>\' doesn\'t seem to be designed with null type annotations in mind",
					markers);
			int[] severities = new int[] { IMarker.SEVERITY_WARNING, IMarker.SEVERITY_ERROR, IMarker.SEVERITY_WARNING };
			for (int i = 0; i < markers.length; i++) {
				IMarker marker = markers[i];
				assertEquals("severity of "+marker.getAttribute(IMarker.MESSAGE),
						severities[i], marker.getAttribute(IMarker.SEVERITY));
			}
		} finally {
			deleteProject("Bug500024");
		}
	}

	@SuppressWarnings("deprecation")
	public void testBug500024jar() throws CoreException, IOException {
		try {
			String projectName = "Bug500024";
			setupJavaProject(projectName, true, true);
			this.project.setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);

			String projectLoc = this.project.getResource().getLocation().toString();
			String annotsZip = "/annots.zip";
			String zipFile = projectLoc + annotsZip;
			String tmpFolder = projectLoc+"/annots";
			Util.zip(new File(tmpFolder), zipFile);
			Util.delete(tmpFolder);
			this.project.getProject().refreshLocal(1, new NullProgressMonitor());
			addEeaToVariableEntry("JCL18_FULL", "/"+projectName+annotsZip);
			IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("test1", true, null);
			ICompilationUnit unit = fragment.getCompilationUnit("Test1.java").getWorkingCopy(new NullProgressMonitor());

			CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems,
					new String[] {
						"Pb(980) Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull Map<@NonNull String,@NonNull Test1>\'. Type \'Map<K,V>\' doesn\'t seem to be designed with null type annotations in mind",
						"Pb(149) Dead code",
						"Pb(915) Illegal redefinition of parameter other, inherited method from Object declares this parameter as @Nullable"
					},
					new int[] {9, 11, 13},
					new int[] { ProblemSeverities.Warning, ProblemSeverities.Warning, ProblemSeverities.Error });

			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Markers after full build",
					"Dead code\n" +
					"Illegal redefinition of parameter other, inherited method from Object declares this parameter as @Nullable\n" +
					"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull Map<@NonNull String,@NonNull Test1>\'. Type \'Map<K,V>\' doesn\'t seem to be designed with null type annotations in mind",
					markers);
			int[] severities = new int[] { IMarker.SEVERITY_WARNING, IMarker.SEVERITY_ERROR, IMarker.SEVERITY_WARNING };
			for (int i = 0; i < markers.length; i++) {
				IMarker marker = markers[i];
				assertEquals("severity of "+marker.getAttribute(IMarker.MESSAGE),
						severities[i], marker.getAttribute(IMarker.SEVERITY));
			}
		} finally {
			deleteProject("Bug500024");
		}
	}

	@SuppressWarnings("deprecation")
	public void testBug508955() throws CoreException, IOException {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots",
			new String[] {
				"/UnannotatedLib/libs/Collectors.java",
				"package libs;\n" +
				"public interface Collectors {\n" +
				"    Collector<CharSequence, ?, String> joining(CharSequence delimiter);\n" +
				"\n" +
				"}\n",
				"/UnannotatedLib/libs/Stream.java",
				"package libs;\n" +
				"public interface Stream<T> {\n" +
				"    <R, A> R collect(Collector<? super T, A, R> collector);\n" +
				"}\n",
				"/UnannotatedLib/libs/List.java",
				"package libs;\n" +
				"public interface List<T> {\n" +
				"	Stream<T> stream();\n" +
				"}\n",
				"Collector.java",
				"package libs;\n" +
				"public interface Collector<T, A, R> { }\n"
			}, null);
		createFileInProject("annots/libs", "Collectors.eea",
				"class libs/Collectors\n" +
				"\n" +
				"joining\n" +
				" (Ljava/lang/CharSequence;)Llibs/Collector<Ljava/lang/CharSequence;*Ljava/lang/String;>;\n" +
				" (Ljava/lang/CharSequence;)L1libs/Collector<L1java/lang/CharSequence;*1L1java/lang/String;>;\n" +
				"\n" +
				"\n");
		createFile(this.project.getElementName()+"/annots/libs/Stream.eea",
				"class libs/Stream\n" +
				"\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Example.java",
				"package tests;\n" +
				"import libs.*;\n" +
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"public class Example {\n" +
				"	public String func(List<String> list, Collectors collectors){\n" +
				"		return list.stream().collect(collectors.joining(\",\"));\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}

	@SuppressWarnings("deprecation")
	public void testBug508955b() throws CoreException, IOException {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots",
			new String[] {
				"/UnannotatedLib/libs/Collectors.java",
				"package libs;\n" +
				"public interface Collectors {\n" +
				"    Collector<CharSequence, ? extends Object, String> joining(CharSequence delimiter);\n" +
				"\n" +
				"}\n",
				"/UnannotatedLib/libs/Stream.java",
				"package libs;\n" +
				"public interface Stream<T> {\n" +
				"    <R, A> R collect(Collector<? super T, A, R> collector);\n" +
				"}\n",
				"/UnannotatedLib/libs/List.java",
				"package libs;\n" +
				"public interface List<T> {\n" +
				"	Stream<T> stream();\n" +
				"}\n",
				"/UnannotatedLib/libs/Collector.java",
				"package libs;\n" +
				"public interface Collector<T, A, R> { }\n"
			}, null);
		createFileInProject("annots/libs", "Collectors.eea",
				"class libs/Collectors\n" +
				"\n" +
				"joining\n" +
				" (Ljava/lang/CharSequence;)Llibs/Collector<Ljava/lang/CharSequence;+Ljava/lang/Object;Ljava/lang/String;>;\n" +
				" (Ljava/lang/CharSequence;)L1libs/Collector<L1java/lang/CharSequence;+1L1java/lang/Object;L1java/lang/String;>;\n" +
				"\n" +
				"\n");
		createFile(this.project.getElementName()+"/annots/libs/Stream.eea",
				"class libs/Stream\n" +
				"\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Example.java",
				"package tests;\n" +
				"import libs.*;\n" +
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"public class Example {\n" +
				"	public String func(List<String> list, Collectors collectors){\n" +
				"		return list.stream().collect(collectors.joining(\",\"));\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}
	/** .eaa present, but null annotations not on classpath */
	public void testBug525649() throws Exception {
		setupJavaProject("Bug525649", false, false);
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/MyMap.java",
				MY_MAP_CONTENT
			}, null);
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		assertNoMarkers(markers);
	}
	public void testBug525715() throws Exception {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"public abstract class Lib1<T,U> {\n" +
				"	public abstract Lib1<T,U> take(Lib1<T,U> x, Object y);\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				lib1Content
			}, null);

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public abstract class Test1 extends Lib1<String,String> {\n" +
				"	public abstract Lib1<String,String> take(Lib1<String,String> x, Object y);\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJSL9(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(916) Illegal redefinition of parameter x, inherited method from Lib1<String,String> does not constrain this parameter",
				"Pb(916) Illegal redefinition of parameter y, inherited method from Lib1<String,String> does not constrain this parameter"
		}, new int[] { 7, 7 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJSL9());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("take");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		ExternalAnnotationUtil.annotateMember("libs/Lib1", annotationFile,
				"take",
				originalSignature,
				"(L1libs/Lib1<TT;TU;>;L1java/lang/Object;)Llibs/Lib1<TT;TU;>;",
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is resolved now:
		reconciled = cu.reconcile(getJSL9(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());
	}
	public void testBug517275() throws Exception {
		myCreateJavaProject("TestLibs");
		this.project.setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);
		String lib1Content =
				"package libs;\n" +
				"import java.util.List;\n" +
				"public abstract class Collections {\n" +
				"   public static final <T> List<T> emptyList() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Collections.java",
				lib1Content
			}, null);
		createFileInProject("annots/libs", "Collections.eea",
				"class libs/Collections\n" +
				"\n" +
				"emptyList\n" +
				" <T:Ljava/lang/Object;>()Ljava/util/List<TT;>;\n" +
				" <T:Ljava/lang/Object;>()L1java/util/List<T1T;>;\n" +
				"\n");

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import libs.Collections;\n" +
				"import java.util.List;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public class Test1 {\n" +
				"  List<@NonNull String> strings;\n" +
				"\n" +
				"  public Test1() {\n" +
				"    strings = Collections.emptyList();      // <<<<< WARNING\n" +
				"  }\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJSL9(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());
	}
	public void testBug513880() throws Exception {
		myCreateJavaProject("TestLibs");
		this.project.setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);
		String lib1Content =
				"package libs;\n" +
				"import java.util.List;\n" +
				"public abstract class Collections {\n" +
				"	public void foo() {}\n" +
				"   public static final <T> List<T> emptyList() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Collections.java",
				lib1Content
			}, null);
		createFileInProject("annots/libs", "Collections.eea",
				"class libs/Collections\n" +
				"\n" +
				"foo\n" +
				" ()V\n" + // 2-line entry without separating empty line: ignore
				"foo\n" +
				"()V\n" + // 2-line entry without leading space on signature lines: ignore
				"emptyList\n" +
				" <T:Ljava/lang/Object;>()Ljava/util/List<TT;>;\n" +
				" <T:Ljava/lang/Object;>()L1java/util/List<T1T;>;\n" +
				"\n");

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import libs.Collections;\n" +
				"import java.util.List;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public class Test1 {\n" +
				"  List<@NonNull String> strings;\n" +
				"\n" +
				"  public Test1() {\n" +
				"    strings = Collections.emptyList();      // <<<<< WARNING\n" +
				"  }\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJSL9(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());
	}

	// Bug 522377 - [null] String.format(""...) shows warning
	public void testVargs() throws CoreException, IOException {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"public abstract class MyString {\n" +
				"   public static String format(String format, Object... args) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/MyString.java",
				lib1Content
			}, null);
		createFileInProject("annots/libs", "MyString.eea",
				"class libs/MyString\n" +
				"\n" +
				"format\n" +
				" (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;\n" +
				" (Ljava/lang/String;[Ljava/lang/Object;)L1java/lang/String;\n" +
				"\n");

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import libs.MyString;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public class Test1 {\n" +
				"  public @NonNull String test(int var) {\n" +
				"    return MyString.format(\"que%03d\", var);\n" +
				"  }\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJSL9(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());
	}

	@SuppressWarnings("deprecation")
	public void testBug482242() throws CoreException, IOException {
		try {
			String projectName = "Bug482242";
			setupJavaProject(projectName, true, true);
			this .project.setOption(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.WARNING);
			addEeaToVariableEntry("JCL18_FULL", "/"+projectName+"/annots");
			IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("test1", true, null);
			ICompilationUnit unit = fragment.getCompilationUnit("Test.java").getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems,
					new String[] {
						"Pb(983) Unsafe null type conversion (type annotations): The value of type 'Collector<@NonNull String,capture#of ?,Set<@NonNull String>>' " +
						"is made accessible using the less-annotated type 'Collector<? super String,Object,Set<@NonNull String>>'",
					},
					new int[] {11},
					new int[] { ProblemSeverities.Warning });

			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Markers after full build",
					"Unsafe null type conversion (type annotations): The value of type 'Collector<@NonNull String,capture#of ?,Set<@NonNull String>>' is made accessible using the less-annotated type 'Collector<? super String,Object,Set<@NonNull String>>'",
					markers);
			int[] severities = new int[] { IMarker.SEVERITY_WARNING };
			for (int i = 0; i < markers.length; i++) {
				IMarker marker = markers[i];
				assertEquals("severity of "+marker.getAttribute(IMarker.MESSAGE),
						severities[i], marker.getAttribute(IMarker.SEVERITY));
			}
		} finally {
			deleteProject("Bug500024");
		}
	}

	// reconcile client of a "generated" source+eea
    @SuppressWarnings("deprecation")
	public void testSourceFolder1() throws CoreException {
		myCreateJavaProject("Bug509397");
		addSourceFolderWithExternalAnnotations(this.project, "/Bug509397/src-gen", "/Bug509397/bin-gen", "/Bug509397/annot-gen");

		createFileInProject("annot-gen/pgen", "CGen.eea",
				"class pgen/CGen\n" +
				"\n" +
				"get\n" +
				" (Ljava/lang/String;)Ljava/lang/String;\n" +
				" (L1java/lang/String;)L1java/lang/String;\n");

		createFileInProject("src-gen/pgen", "CGen.java",
				"package pgen;\n" +
				"public class CGen {\n" +
				"	public String get(String in) { return in; }\n" +
				"}\n");

		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("p", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Use.java",
				"package p;\n" +
				"import pgen.CGen;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class Use {\n" +
				"	public @NonNull String test(CGen c) {\n" +
				"		String s = c.get(null);\n" + // problem here (6)
				"		return s;\n" + // no problem here
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
				"Pb(910) Null type mismatch: required '@NonNull String' but the provided value is null"
			}, new int[] { 6 });
	}

	// reconcile client of a "generated" source+eea
    // single merged output folder
    @SuppressWarnings("deprecation")
	public void testSourceFolder1a() throws CoreException {
		myCreateJavaProject("Bug509397");
		addSourceFolderWithExternalAnnotations(this.project, "/Bug509397/src-gen", null, "/Bug509397/annot-gen");

		createFileInProject("annot-gen/pgen", "CGen.eea",
				"class pgen/CGen\n" +
				"\n" +
				"get\n" +
				" (Ljava/lang/String;)Ljava/lang/String;\n" +
				" (L1java/lang/String;)L1java/lang/String;\n");

		createFileInProject("src-gen/pgen", "CGen.java",
				"package pgen;\n" +
				"public class CGen {\n" +
				"	public String get(String in) { return in; }\n" +
				"}\n");
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("p", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Use.java",
				"package p;\n" +
				"import pgen.CGen;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class Use {\n" +
				"	public @NonNull String test(CGen c) {\n" +
				"		String s = c.get(null);\n" + // problem here (6)
				"		return s;\n" + // no problem here
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
				"Pb(910) Null type mismatch: required '@NonNull String' but the provided value is null"
			}, new int[] { 6 });
	}

	// reconcile client of a "generated" source+eea -- constant in nested interface
	@SuppressWarnings("deprecation")
	public void testSourceFolder1b() throws CoreException {
		myCreateJavaProject("Bug509397");
		addSourceFolderWithExternalAnnotations(this.project, "/Bug509397/src-gen", "/Bug509397/bin-gen", "/Bug509397/annot-gen");

		createFileInProject("annot-gen/pgen", "CGen$Int.eea",
				"class pgen/CGen$Int\n" +
				"\n" +
				"CONST\n" +
				" Ljava/lang/Object;\n" +
				" L1java/lang/Object;\n");

		createFileInProject("src-gen/pgen", "CGen.java",
				"package pgen;\n" +
				"public class CGen {\n" +
				"	public interface Int {\n" +
				"		Object CONST = 1;\n" +
				"	}\n" +
				"}\n");

		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("p", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Use.java",
				"package p;\n" +
				"import pgen.CGen;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class Use {\n" +
				"	@NonNull Object s = CGen.Int.CONST;\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		// this works:
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		assertNoMarkers(markers);
		// this needs a fix
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}

    // full build of a project with src-gen & annot-gen
	public void testSourceFolder2() throws CoreException {
		myCreateJavaProject("Bug509397");
		addSourceFolderWithExternalAnnotations(this.project, "/Bug509397/src-gen", "/Bug509397/bin-gen", "/Bug509397/annot-gen");

		createFileInProject("annot-gen/pgen", "CGen.eea",
				"class pgen/CGen\n" +
				"\n" +
				"get\n" +
				" (Ljava/lang/String;)Ljava/lang/String;\n" +
				" (L1java/lang/String;)L1java/lang/String;\n");

		createFileInProject("src-gen/pgen", "CGen.java",
				"package pgen;\n" +
				"public class CGen {\n" +
				"	public String get(String in) { return in; }\n" +
				"}\n");

		createFileInProject("src/p", "Use.java",
				"package p;\n" +
				"import pgen.CGen;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class Use {\n" +
				"	public @NonNull String test(CGen c) {\n" +
				"		String s = c.get(null);\n" + // problem here (6)
				"		return s;\n" + // no problem here
				"	}\n" +
				"}\n");
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers",
				"Null type mismatch: required '@NonNull String' but the provided value is null",
				markers);
	}

    // full build of a project with src-gen & annot-gen
    // single merged output folder
	public void testSourceFolder2a() throws CoreException {
		myCreateJavaProject("Bug509397");
		addSourceFolderWithExternalAnnotations(this.project, "/Bug509397/src-gen", null, "/Bug509397/annot-gen");

		createFileInProject("annot-gen/pgen", "CGen.eea",
				"class pgen/CGen\n" +
				"\n" +
				"get\n" +
				" (Ljava/lang/String;)Ljava/lang/String;\n" +
				" (L1java/lang/String;)L1java/lang/String;\n");

		createFileInProject("src-gen/pgen", "CGen.java",
				"package pgen;\n" +
				"public class CGen {\n" +
				"	public String get(String in) { return in; }\n" +
				"}\n");

		createFileInProject("src/p", "Use.java",
				"package p;\n" +
				"import pgen.CGen;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class Use {\n" +
				"	public @NonNull String test(CGen c) {\n" +
				"		String s = c.get(null);\n" + // problem here (6)
				"		return s;\n" + // no problem here
				"	}\n" +
				"}\n");
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers",
				"Null type mismatch: required '@NonNull String' but the provided value is null",
				markers);
	}

	// re-usable file contents for the subsequent set of tests (which exercise different configurations with same content):
	static String mixedArtifacts_CGen_eea_content =
			"class lib/pgen/CGen\n" +
			"\n" +
			"get\n" +
			" (Ljava/lang/String;)Ljava/lang/String;\n" +
			" (L1java/lang/String;)L1java/lang/String;\n";
	static String mixedArtifacts_CGen2_eea_content =
			"class lib/pgen2/CGen2\n" +
			"\n" +
			"get2\n" +
			" (Ljava/lang/Exception;)Ljava/lang/String;\n" +
			" (L1java/lang/Exception;)L1java/lang/String;\n";
	static String mixedArtifacts_CGen_java_content =
			"package lib.pgen;\n" +
			"public class CGen {\n" +
			"	public String get(String in) { return in; }\n" +
			"}\n";
	static String mixedArtifacts_CGen2_java_content =
			"package lib.pgen2;\n" +
			"public class CGen2 {\n" +
			"	public String get2(Exception in) { return in.toString(); }\n" +
			"}\n";

	public void testMultiProject1() throws CoreException {
		// PrjTest depends on two projects bundled with eea for their respective generated classes
		// accessed using CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS
		IJavaProject prj1 = null, prj2 = null;
		myCreateJavaProject("Prj1");
		addSourceFolderWithExternalAnnotations(this.project, "/Prj1/src-gen", null, "/Prj1/annot-gen");
		prj1 = this.project;
		try {
			createFileInProject("annot-gen/lib/pgen", "CGen.eea",  mixedArtifacts_CGen_eea_content);
			createFileInProject("src-gen/lib/pgen",   "CGen.java", mixedArtifacts_CGen_java_content);
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			myCreateJavaProject("Prj2");
			prj2 = this.project;
			addSourceFolderWithExternalAnnotations(this.project, "/Prj2/src-gen", null, "/Prj2/annot-gen");
			createFileInProject("annot-gen/lib/pgen2", "CGen2.eea",  mixedArtifacts_CGen2_eea_content);
			createFileInProject("src-gen/lib/pgen2",   "CGen2.java", mixedArtifacts_CGen2_java_content);
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			myCreateJavaProject("PrjTest");
			addClasspathEntry(this.project,
					JavaCore.newProjectEntry(new Path("/Prj1"), null/*access rules*/, false/*combine access rules*/, null, false/*exported*/));
			addClasspathEntry(this.project,
					JavaCore.newProjectEntry(new Path("/Prj2"), null/*access rules*/, false/*combine access rules*/, null, false/*exported*/));
			this.project.setOption(JavaCore.CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS, JavaCore.ENABLED);

			internalTestMixedArtifactsTest();
		} finally {
			if (prj1 != null)
				prj1.getProject().delete(true, true , null);
			if (prj2 != null)
				prj2.getProject().delete(true, true , null);
		}
	}
	public void testSelfAnnotatedJars() throws CoreException, IOException {
		// this is the "deployed" variant of testMultiProject1(), i.e, dependencies are jars, not projects
		myCreateJavaProject("PrjTest");
		this.project.setOption(JavaCore.CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS, JavaCore.ENABLED);

		String projectLoc = this.project.getProject().getLocation().toString();
		Util.createJar(
			new String[] { "lib/pgen/CGen.java", mixedArtifacts_CGen_java_content },
			new String[] { "lib/pgen/CGen.eea",  mixedArtifacts_CGen_eea_content },
			projectLoc+"/lib/prj1.jar", "1.8");
		addClasspathEntry(this.project,
				JavaCore.newLibraryEntry(new Path("/PrjTest/lib/prj1.jar"), null/*access rules*/, null, false/*exported*/));

		Util.createJar(
			new String[] { "lib/pgen2/CGen2.java", mixedArtifacts_CGen2_java_content },
			new String[] { "lib/pgen2/CGen2.eea",  mixedArtifacts_CGen2_eea_content },
			projectLoc+"/lib/prj2.jar", "1.8");
		addClasspathEntry(this.project,
				JavaCore.newLibraryEntry(new Path("/PrjTest/lib/prj2.jar"), null/*access rules*/, null, false/*exported*/));

		internalTestMixedArtifactsTest();
	}

	public void testSeparateAnnotationJar() throws CoreException, IOException {
		// all .eeas are deployed in a separate jar on the build path
		// accessed using CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS
		myCreateJavaProject("PrjTest");
		this.project.setOption(JavaCore.CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS, JavaCore.ENABLED);

		String projectLoc = this.project.getProject().getLocation().toString();
		Util.createJar(new String[0], new String[] {
					"lib/pgen/CGen.eea", mixedArtifacts_CGen_eea_content,
					"lib/pgen2/CGen2.eea", mixedArtifacts_CGen2_eea_content,
				},
				projectLoc+"/lib/eeas.jar", "1.8");
		addClasspathEntry(this.project,
				JavaCore.newLibraryEntry(new Path("/PrjTest/lib/eeas.jar"), null/*access rules*/, null, false/*exported*/));

		// one jar dependency in the workspace
		Util.createJar(
				new String[] { "lib/pgen/CGen.java", mixedArtifacts_CGen_java_content },
				projectLoc+"/lib/prj1.jar", "1.8");
		addClasspathEntry(this.project,
				JavaCore.newLibraryEntry(new Path("/PrjTest/lib/prj1.jar"), null/*access rules*/, null, false/*exported*/));

		// second dependency is external jar file
		String externalJarLoc = Util.getOutputDirectory()+"/lib/prj2.jar";
		Util.createJar(
				new String[] { "lib.pgen2/CGen2.java", mixedArtifacts_CGen2_java_content },
				externalJarLoc, "1.8");
		addClasspathEntry(this.project,
				JavaCore.newLibraryEntry(new Path(externalJarLoc), null/*access rules*/, null, false/*exported*/));

		internalTestMixedArtifactsTest();
	}

	public void testSeparateAnnotationJarInContainer() throws CoreException, IOException {
		// .eeas are deployed as a member of a classpath container
		// referenced relative to the container.
		myCreateJavaProject("PrjTest");
		String projectLoc = this.project.getProject().getLocation().toString();

		ITestInitializer prev = ContainerInitializer.initializer;
		String fullPathToEEA = projectLoc+"/lib/my.eeas-0.1.jar";
		String fullPathToPrj1 = projectLoc+"/lib/prj1.jar";
		ContainerInitializer.setInitializer(new TestCustomContainerInitializer(
				fullPathToEEA, null,
				fullPathToPrj1, null));

		try {
			Util.createJar(new String[0], new String[] {
						"lib/pgen/CGen.eea",   mixedArtifacts_CGen_eea_content,
						"lib/pgen2/CGen2.eea", mixedArtifacts_CGen2_eea_content },
					fullPathToEEA, "1.8");

			Util.createJar(
					new String[] { "lib/pgen/CGen.java", mixedArtifacts_CGen_java_content },
					fullPathToPrj1, "1.8");

			// eeas & prj1 accessed via the Container:
			IClasspathEntry entry = JavaCore.newContainerEntry(new Path(TestContainerInitializer.TEST_CONTAINER_NAME), null/*access rules*/,
					externalAnnotationExtraAttributes(TestContainerInitializer.TEST_CONTAINER_NAME+"/my.eeas"),
					false/*exported*/);
			addClasspathEntry(this.project, entry);

			// other jar accessed via separate entry, also annotated using eea in container
			String externalJarLoc = Util.getOutputDirectory() + "/lib/prj2.jar";
			Util.createJar(
					new String[] { "lib.pgen2/CGen2.java", mixedArtifacts_CGen2_java_content },
					externalJarLoc, "1.8");
			entry = JavaCore.newLibraryEntry(new Path(externalJarLoc), null, null, null,
					externalAnnotationExtraAttributes(TestContainerInitializer.TEST_CONTAINER_NAME+"/my.eeas"),
					false/*exported*/);
			addClasspathEntry(this.project, entry);

			internalTestMixedArtifactsTest();
		} finally {
			ContainerInitializer.setInitializer(prev);
		}
	}

	public void testAnnotationsInProjectReferencedViaContainer() throws CoreException, IOException {
		internalTestAnnotationsInProjectReferencedViaContainer(null);
	}
	public void testAnnotationsInExternalProjectReferencedViaContainer() throws CoreException, IOException {
		URI parentLocation = getWorkspaceRoot().getLocation().toFile().getParentFile().toURI();
		URI location = parentLocation.resolve(URI.create("external-project"));
		internalTestAnnotationsInProjectReferencedViaContainer(location);
	}
	void internalTestAnnotationsInProjectReferencedViaContainer(URI location) throws CoreException, IOException {
		// undeployed version of testSeparateAnnotationJarInContainer:
		// container "resolved" the eea-artifact to a workspace project
		myCreateJavaProject("PrjTest");
		String projectLoc = this.project.getProject().getLocation().toString();

		ITestInitializer prev = ContainerInitializer.initializer;
		String eeaProjectName = "my.eeas";
		String fullPathToPrj1 = projectLoc+"/lib/prj1.jar";
		ContainerInitializer.setInitializer(new TestCustomContainerInitializer(
				'/'+eeaProjectName, null,
				fullPathToPrj1, null));

		IJavaProject eeaProject = null;
		try {
			final String projectName = eeaProjectName;
			eeaProject = createJavaProject(projectName, location,
					new String[] {""}, new String[] {"JCL_LIB"},
					null, null, null, null, null, true, null,
					"", null, null, null, "", false, false);
			createFolder('/'+eeaProjectName+"/lib/pgen");
			createFolder('/'+eeaProjectName+"/lib/pgen2");
			createFile(eeaProjectName+"/lib/pgen/CGen.eea",   mixedArtifacts_CGen_eea_content);
			createFile(eeaProjectName+"/lib/pgen2/CGen2.eea", mixedArtifacts_CGen2_eea_content);

			Util.createJar(
					new String[] { "lib/pgen/CGen.java", mixedArtifacts_CGen_java_content },
					fullPathToPrj1, "1.8");

			// eeas & prj1 accessed via the Container:
			IClasspathEntry entry = JavaCore.newContainerEntry(
					new Path(TestContainerInitializer.TEST_CONTAINER_NAME), null/*access rules*/,
					externalAnnotationExtraAttributes(TestContainerInitializer.TEST_CONTAINER_NAME+"/my.eeas"),
					false/*exported*/);
			addClasspathEntry(this.project, entry);

			// other jar accessed via separate entry, also annotated using eea in container
			String externalJarLoc = Util.getOutputDirectory() + "/lib/prj2.jar";
			Util.createJar(
					new String[] { "lib.pgen2/CGen2.java", mixedArtifacts_CGen2_java_content },
					externalJarLoc, "1.8");
			entry = JavaCore.newLibraryEntry(
					new Path(externalJarLoc), null, null, null,
					externalAnnotationExtraAttributes(TestContainerInitializer.TEST_CONTAINER_NAME+"/my.eeas"),
					false/*exported*/);
			addClasspathEntry(this.project, entry);

			internalTestMixedArtifactsTest();
		} finally {
			ContainerInitializer.setInitializer(prev);
			if (eeaProject.exists())
				eeaProject.getProject().delete(true, null);
		}
	}

	public void testMixedElementAndContainerAnnotation() throws Exception {
		// container mixes annotations from internal resolving ("self") and client-side annotation path
		myCreateJavaProject("PrjTest");
		String projectLoc = this.project.getProject().getLocation().toString();
		ITestInitializer prev = ContainerInitializer.initializer;
		ContainerInitializer.setInitializer(new TestCustomContainerInitializer(projectLoc+"/lib1.jar", "self", projectLoc+"/lib2.jar", null));
		try {

			// jar with external annotations for its own class
			Util.createJar(
				new String[] { "lib/pgen/CGen.java", mixedArtifacts_CGen_java_content },
				new String[] { "lib/pgen/CGen.eea",  mixedArtifacts_CGen_eea_content, },
				projectLoc+"/lib1.jar", "1.8");
			Util.createJar(
				new String[] { "lib/pgen2/CGen2.java", mixedArtifacts_CGen2_java_content },
				projectLoc+"/lib2.jar", "1.8");
			IClasspathEntry containerEntry = JavaCore.newContainerEntry(
					new Path(TestContainerInitializer.TEST_CONTAINER_NAME),
					null,
					externalAnnotationExtraAttributes("/PrjTest/annots"),
					false);
			addClasspathEntry(this.project, containerEntry);

			createFileInProject("annots/lib/pgen2", "CGen2.eea", mixedArtifacts_CGen2_eea_content);

			internalTestMixedArtifactsTest();
		} finally {
			ContainerInitializer.setInitializer(prev);
		}
	}
	protected void internalTestMixedArtifactsTest() throws CoreException, JavaModelException {
		this.project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		createFileInProject("src/p", "Use.java",
				"package p;\n" +
				"import lib.pgen.CGen;\n" +
				"import lib.pgen2.CGen2;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class Use {\n" +
				"	public @NonNull String test(CGen c) {\n" +
				"		String s = c.get(null);\n" + // problem here (7)
				"		return s;\n" + // no problem here
				"	}\n" +
				"	public @NonNull String test2(CGen2 c) {\n" +
				"		String s = c.get2(null);\n" + // problem here (11)
				"		return s;\n" + // no problem here
				"	}\n" +
				"}\n");
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = this.project.getProject()
				.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		sortMarkers(markers);
		assertMarkers("Unexpected markers",
				"Null type mismatch: required '@NonNull Exception' but the provided value is null\n" +
				"Null type mismatch: required '@NonNull String' but the provided value is null",
				markers);
		ICompilationUnit unit = JavaCore
				.createCompilationUnitFrom(this.project.getProject().getFile("src/p/Use.java"))
				.getWorkingCopy(null);
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems,
			new String[] {
				"Pb(910) Null type mismatch: required '@NonNull String' but the provided value is null",
				"Pb(910) Null type mismatch: required '@NonNull Exception' but the provided value is null"
			},
			new int[] { 7, 11 });
	}

	public void testAnnotatedSourceSharesOutputFolder() throws CoreException {
		IJavaProject prj1 = null;
		myCreateJavaProject("Prj1");
		addSourceFolderWithExternalAnnotations(this.project, "/Prj1/src-gen", null, "/Prj1/annot-gen");
		prj1 = this.project;
		try {
			createFileInProject("annot-gen/pgen", "CGen.eea",
					"class pgen/CGen\n" +
					"\n" +
					"get\n" +
					" (Ljava/lang/String;)Ljava/lang/String;\n" +
					" (L1java/lang/String;)L1java/lang/String;\n");

			createFileInProject("src-gen/pgen", "CGen.java",
					"package pgen;\n" +
					"public class CGen {\n" +
					"	public String get(String in) { return in; }\n" +
					"}\n");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			createFileInProject("src/pgen", "CImpl.java",
					"package pgen;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class CImpl extends CGen {\n" +
					"	public @NonNull String get(@NonNull String in) { return in; }\n" +
					"}\n");
			ICompilationUnit unit = JavaCore.createCompilationUnitFrom(this.project.getProject().getFile("src/pgen/CImpl.java")).getWorkingCopy(null);
			CompilationUnit reconciled = unit.reconcile(AST.getJLSLatest(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertNoProblems(problems);
			this.project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			assertNoMarkers(markers);
		} finally {
			if (prj1 != null)
				prj1.getProject().delete(true, true , null);
		}
	}

	public void testGH1008() throws Exception {
		myCreateJavaProject("ValueOf");
		Map options = this.project.getOptions(true);
		options.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
		this.project.setOptions(options);

		addLibraryWithExternalAnnotations(this.project, "jreext.jar", "annots", new String[] {
				"/UnannotatedLib/java/lang/Integer.java",
				"""
				package java.lang;
				public class Integer {
					public static Integer valueOf(String s) { return null; }
				}
				""",
				"/UnannotatedLib/java/util/function/Function.java",
				"""
				package java.util.function;
				public interface Function<T,R> {
					R apply(T t);
				}
				"""
			}, null);
		createFileInProject("annots/java/lang", "Integer.eea",
				"""
				class java/lang/Integer
				valueOf
				 (Ljava/lang/String;)Ljava/lang/Integer;
				 (L1java/lang/String;)L1java/lang/Integer;
				""");

		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("test", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("UncheckedConversionFalsePositive.java",
				"""
				package test;
				import org.eclipse.jdt.annotation.Checks;
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;

				public class UncheckedConversionFalsePositive {

					public static @NonNull Integer doSomething(@NonNull final String someValue) {
						return Integer.valueOf(someValue);
					}

					Integer test() {
						final @Nullable String nullableString = "12";
						@Nullable Integer result;

						// This first example that uses my own annotated method and not eclipse external annotations
						// works no problem...
						result = Checks.applyIfNonNull(nullableString, UncheckedConversionFalsePositive::doSomething);

						// But now, if I do this, which relies on EEA annotations instead of my in-code annotations....
						result = Checks.applyIfNonNull(nullableString, Integer::valueOf);
						// Then Integer::valueOf is flagged with the following message:
						/*
						 * Null type safety: parameter 1 provided via method descriptor
						 * Function<String,Integer>.apply(String) needs unchecked conversion to conform to '@NonNull
						 * String'
						 */

						// Note that the same warning is shown on "someValue" below when using a lambda expression
						// instead of a method reference.
						result = Checks.applyIfNonNull(nullableString, someValue -> Integer.valueOf(someValue));

						// The workaround to eliminate this warning without suppressing it is to make the
						// generic parameters explicit with @NonNull but this is very verbose and should
						// ideally be unnecessary...
						result = Checks.<@NonNull String, Integer>applyIfNonNull(nullableString, Integer::valueOf);

						return result;
					}
				}
				""",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}
	public void testGH2178() throws CoreException, IOException {
		myCreateJavaProject("GH2178", "21", "JCL_21_LIB");
		Map options = this.project.getOptions(true);
		options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_NULL_ANNOTATION_INFERENCE_CONFLICT, JavaCore.ERROR);
		this.project.setOptions(options);

		addLibraryWithExternalAnnotations(this.project, "21", "jreext.jar", "annots", new String[] {
				"/UnannotatedLib/lib/Objects.java",
				"""
				package lib;
				public class Objects {
					public static <T> T requireNonNull(T t) { return t; }
				}
				"""
			}, null);
		createFileInProject("annots/java/lang", "String.eea",
				"""
				class java/lang/String
				valueOf
				 (Z)Ljava/lang/String;
				 (Z)L1java/lang/String;
				""");
		createFileInProject("annots/lib", "Objects.eea",
				"""
				class lib/Objects
				requireNonNull
				 <T:Ljava/lang/Object;>(TT;)TT;
				 <T:Ljava/lang/Object;>(T0T;)T1T;
				""");
		addEeaToVariableEntry("JCL_21_LIB", "/GH2178/annots");


		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("repro", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("ExternalNullAnnotationsConfusion.java",
				"""
				package repro;

				import lib.Objects;

				import org.eclipse.jdt.annotation.NonNull;

				public class ExternalNullAnnotationsConfusion {

					public String conflictingNonNullAndNullable() {

						// String.valueOf() is annotated to return @NonNull
						@NonNull
						String valueOfAnnotatedNonNull = String.valueOf(false);

						// Objects.requireNonNull is annotated to take a @Nullable parameter
						@NonNull
						String result = Objects.requireNonNull(valueOfAnnotatedNonNull);

						// error marker at the last argument in the previous line:
						// Contradictory null annotations:
						// method was inferred as '@NonNull String requireNonNull(@Nullable @NonNull String)',
						// but only one of '@NonNull' and '@Nullable' can be effective at any location
						return result;
					}
				}
				""",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);

		this.project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		assertNoMarkers(markers);
	}

}
