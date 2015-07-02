/*******************************************************************************
 * Copyright (c) 2014, 2015 GK Software AG, and others.
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
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ExternalAnnotationUtil;
import org.eclipse.jdt.core.util.ExternalAnnotationUtil.MergeStrategy;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.osgi.framework.Bundle;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ExternalAnnotations18Test extends ModifyingResourceTests {

	/** Bridge to hook the host JRE into the registered ContainerInitializer. */
	static class TestContainerInitializer implements ContainerInitializer.ITestInitializer {

		/** Use this container name in test projects. */
		private static final String TEST_CONTAINER_NAME = "org.eclipse.jdt.core.tests.model.TEST_CONTAINER";
		
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

		public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
			String[] jars = Util.getJavaClassLibs();
			IClasspathEntry[] entries = new IClasspathEntry[jars.length];
			for (int i = 0; i < jars.length; i++)
				entries[i] = JavaCore.newLibraryEntry(new Path(jars[i]), null, null);
			JavaCore.setClasspathContainer(
					new Path(TEST_CONTAINER_NAME),
					new IJavaProject[]{ project },
					new IClasspathContainer[] { new TestContainer(new Path(TEST_CONTAINER_NAME), entries) },
					null);
		}
		public boolean allowFailureContainer() {
			return false;
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

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		
		Bundle[] bundles = getAnnotationBundles();
		File bundleFile = FileLocator.getBundleFile(bundles[0]);
		this.ANNOTATION_LIB = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();

		// set up class path container bridging to the host JRE:
		ContainerInitializer.setInitializer(new TestContainerInitializer());
	}

	/**
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	protected Bundle[] getAnnotationBundles() {
		return org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[2.0.0,3.0.0)");
	}

	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		ContainerInitializer.setInitializer(null);
	}
	
	public String getSourceWorkspacePath() {
		// we read individual projects from within this folder:
		return super.getSourceWorkspacePath()+"/ExternalAnnotations18";
	}

	protected String getSourceWorkspacePathBase() {
		return super.getSourceWorkspacePath();
	}

	void setupJavaProject(String name) throws CoreException, IOException {
		this.project = setUpJavaProject(name, this.compliance); //$NON-NLS-1$
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
		this.project = createJavaProject(name, new String[]{"src"}, new String[]{this.jclLib}, null, null, "bin", null, null, null, this.compliance);
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
	
	protected void tearDown() throws Exception {
		if (this.project != null)
			this.project.getProject().delete(true, true, null);
		this.project = null;
		this.root = null;
		super.tearDown();
	}

	protected void addLibraryWithExternalAnnotations(
			IJavaProject javaProject,
			String jarName,
			String externalAnnotationPath,
			String[] pathAndContents,
			Map options) throws CoreException, IOException
	{
		createLibrary(javaProject, jarName, "src.zip", pathAndContents, null, this.compliance, options);
		String jarPath = '/' + javaProject.getProject().getName() + '/' + jarName;
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[] { new ClasspathAttribute(IClasspathAttribute.EXTERNAL_ANNOTATION_PATH, externalAnnotationPath) };
		IClasspathEntry entry = JavaCore.newLibraryEntry(
				new Path(jarPath),
				new Path('/'+javaProject.getProject().getName()+"/src.zip"),
				null/*src attach root*/,
				null/*access rules*/,
				extraAttributes,
				false/*exported*/);
		addClasspathEntry(this.project, entry);
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

	protected boolean hasJRE18() {
		return ((AbstractCompilerTest.getPossibleComplianceLevels() & AbstractCompilerTest.F_1_8) != 0);
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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
				"Pb(953) Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable capture#of ?'",
				"Pb(953) Null type mismatch (type annotations): required '@NonNull CharSequence' but this expression has type '@Nullable capture#of ? extends CharSequence'",
				"Pb(953) Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable capture#of ? super CharSequence'"
			}, new int[] { 13, 16, 19 });
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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(955) Null type safety (type annotations): The expression of type 'String[]' needs unchecked conversion to conform to '@Nullable String @Nullable[]'",
			"Pb(955) Null type safety (type annotations): The expression of type 'String[][]' needs unchecked conversion to conform to '@Nullable String @Nullable[] @NonNull[]'",
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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
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
		CompilationUnit reconciled = cu.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(955) Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to '@NonNull String'",
		}, new int[] { 8 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
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
		reconciled = cu.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
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
		CompilationUnit reconciled = cu.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(953) Null type mismatch (type annotations): required 'Lib1<Lib1.X,@Nullable String>' but this expression has type 'Lib1<Lib1.@Nullable X,@NonNull String>'",
		}, new int[] { 7 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
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

		// check that the error is even worse now:
		reconciled = cu.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());
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
		ASTParser parser = ASTParser.newParser(AST.JLS8);
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
		ASTParser parser = ASTParser.newParser(AST.JLS8);
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
		ASTParser parser = ASTParser.newParser(AST.JLS8);
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
		ASTParser parser = ASTParser.newParser(AST.JLS8);
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
				"L1libs/Random;",  // <- @NonNull T
				1, MergeStrategy.OVERWRITE_ANNOTATIONS);
		assertEquals("dry-run result",
				"[(Llibs/List<*>;, " +
				"Llibs/Random;, " + 
				"L1libs/Random;, " +  // <- @NonNull T
				")V]",
				Arrays.toString(annotatedSign));
	}
}
