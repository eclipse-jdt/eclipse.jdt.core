/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.tests.model.AbstractJavaSearchTests.JavaSearchResultCollector;
import org.eclipse.jdt.core.tests.model.AbstractJavaSearchTests.TypeNameMatchCollector;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.IndexLocation;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

import junit.framework.Test;

/**
 * Tests the Java search engine accross multiple projects.
 */
public class JavaSearchScopeTests extends ModifyingResourceTests implements IJavaSearchConstants {
public JavaSearchScopeTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchScopeTests.class);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "testMethodOccurences" };
//  TESTS_NUMBERS = new int[] { 101426 };
//	TESTS_RANGE = new int[] { 16, -1 };
}

@Override
protected void tearDown() throws Exception {
	// Cleanup caches
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	manager.containers = new HashMap<>(5);
	manager.variables = new HashMap<>(5);

	super.tearDown();
}
/*
 * Ensures that a Java search scope with SOURCES only is correct.
 */
public void testSources() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.SOURCES);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P\n" +
			"]",
			scope);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java search scope with APPLICATION_LIBRARIES only is correct
 * (external jar case)
 */
public void testApplicationLibrairiesExternalJar() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.APPLICATION_LIBRARIES);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	"+  getExternalJCLPath().toOSString() +"\n" +
			"]",
			scope);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java search scope with APPLICATION_LIBRARIES only is correct
 * (internal jar and class folder cases)
 */
public void testApplicationLibrairiesJarAndClassFolder() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"/P/internal.jar", "/P/classfolder"}, "bin");
		createFile("/P/internal.jar", new byte[0]);
		createFolder("/P/classfolder");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.APPLICATION_LIBRARIES);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P/classfolder\n" +
			"	/P/internal.jar\n" +
			"]",
			scope);
	} finally {
		deleteProject("P");
	}
}
public void testApplicationLibrairiesNonExistingJarAndClassFolder() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"/P/internal.jar", "/P/classfolder"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.APPLICATION_LIBRARIES);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P/classfolder\n" +
			"	/P/internal.jar\n" +
			"]",
			scope);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java search scope with APPLICATION_LIBRARIES only is correct
 * (classpath variable case)
 */
public void testApplicationLibrairiesClasspathVariable() throws CoreException {
	try {
		VariablesInitializer.setInitializer(new ClasspathInitializerTests.DefaultVariableInitializer(new String[] {"TEST_LIB", "/P/lib.jar"}));
		IJavaProject project = createJavaProject("P", new String[] {}, new String[] {"TEST_LIB"}, "");
		createFile("/P/lib.jar", new byte[0]);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.APPLICATION_LIBRARIES);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P/lib.jar\n" +
			"]",
			scope);
	} finally {
		deleteProject("P");
		VariablesInitializer.reset();
	}
}
public void testApplicationLibrairiesNonExistingClasspathVariable() throws CoreException {
	try {
		VariablesInitializer.setInitializer(new ClasspathInitializerTests.DefaultVariableInitializer(new String[] {"TEST_LIB", "/P/lib.jar"}));
		IJavaProject project = createJavaProject("P", new String[] {}, new String[] {"TEST_LIB"}, "");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.APPLICATION_LIBRARIES);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P/lib.jar\n" +
			"]",
			scope);
	} finally {
		deleteProject("P");
		VariablesInitializer.reset();
	}
}
/*
 * Ensures that a Java search scope with APPLICATION_LIBRARIES only is correct
 * (classpath container case)
 */
public void testApplicationLibrairiesClasspathContainer() throws CoreException {
	try {
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", "/P/lib.jar"}));
		IJavaProject project = createJavaProject("P", new String[] {}, new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.APPLICATION_LIBRARIES);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P/lib.jar\n" +
			"]",
			scope);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java search scope with SYSTEM_LIBRARIES only is correct
 * (classpath container case)
 */
public void testSystemLibraries() throws CoreException {
	try {
		DefaultContainerInitializer intializer = new DefaultContainerInitializer(new String[] {"P", "/P/lib.jar"}) {
			@Override
			protected DefaultContainer newContainer(char[][] libPaths) {
				return new DefaultContainer(libPaths) {
					@Override
					public int getKind() {
						return IClasspathContainer.K_SYSTEM;
					}
				};
			}
		};
		ContainerInitializer.setInitializer(intializer);
		IJavaProject project = createJavaProject("P", new String[] {}, new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.SYSTEM_LIBRARIES);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P/lib.jar\n" +
			"]",
			scope);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java search scope with SOURCES | REFERENCED_PROJECTS is correct
 * (direct reference case)
 */
public void testSourcesOrDirectReferencedProjects() throws CoreException {
	try {
		createJavaProject("P1");
		IJavaProject project = createJavaProject("P2", new String[] {"src"}, new String[] {}, new String[] {"/P1"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.SOURCES | IJavaSearchScope.REFERENCED_PROJECTS);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P1\n" +
			"	/P2/src\n" +
			"]",
			scope);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that a Java search scope with SOURCES | REFERENCED_PROJECTS is correct
 * (reference through a container case)
 */
public void testSourcesOrContainerReferencedProjects() throws CoreException {
	try {
		createJavaProject("P1");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1"}));
		IJavaProject project = createJavaProject("P2", new String[] {"src"}, new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, IJavaSearchScope.SOURCES | IJavaSearchScope.REFERENCED_PROJECTS);
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P1\n" +
			"	/P2/src\n" +
			"]",
			scope);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that a Java project is enclosed in a scope on the project (proj=src)
 * (resourcePath case)
 */
public void testScopeEncloses01() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P\"", scope.encloses("/P"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java project is enclosed in a scope on the project (proj=src)
 * (Java element case)
 */
public void testScopeEncloses02() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose P", scope.encloses(project));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a root is enclosed in a scope on the project (proj=src)
 * (resource path with traling slash case)
 */
public void testScopeEncloses03() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/\"", scope.encloses("/P/"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a root is enclosed in a scope on the project (proj=src)
 * (Java element case)
 */
public void testScopeEncloses04() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		IPackageFragmentRoot root = project.getPackageFragmentRoot(project.getProject());
		assertTrue("scope on P should enclose root P", scope.encloses(root));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package is enclosed in a scope on the project (proj=src)
 * (resource path case)
 */
public void testScopeEncloses05() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/x/y\"", scope.encloses("/P/x/y"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package is enclosed in a scope on the project (proj=src)
 * (resource path with trailing slash case)
 */
public void testScopeEncloses06() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/x/y/\"", scope.encloses("/P/x/y/"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package is enclosed in a scope on the project (proj=src)
 * (Java element case)
 */
public void testScopeEncloses07() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		IPackageFragment pkg = getPackage("/P/x/y");
		assertTrue("scope on P should enclose package x.y", scope.encloses(pkg));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a default package is enclosed in a scope on the project (proj=src)
 * (Java element case)
 */
public void testScopeEncloses08() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		IPackageFragment pkg = getPackage("/P");
		assertTrue("scope on P should enclose default package", scope.encloses(pkg));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit is enclosed in a scope on the project (proj=src)
 * (resource path case)
 */
public void testScopeEncloses09() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/x/y/A.java\"", scope.encloses("/P/x/y/A.java"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit is enclosed in a scope on the project (proj=src)
 * (Java element case)
 */
public void testScopeEncloses10() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		ICompilationUnit cu = getCompilationUnit("/P/x/y/A.java");
		assertTrue("scope on P should enclose compilation unit A.java", scope.encloses(cu));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit in the default package is enclosed in a scope on the project (proj=src)
 * (resource path case)
 */
public void testScopeEncloses11() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/A.java\"", scope.encloses("/P/A.java"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit in the default package is enclosed in a scope on the project (proj=src)
 * (Java element case)
 */
public void testScopeEncloses12() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		ICompilationUnit cu = getCompilationUnit("/P/A.java");
		assertTrue("scope on P should enclose compilation unit A.java", scope.encloses(cu));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a source type is enclosed in a scope on the project (proj=src)
 * (Java element case)
 */
public void testScopeEncloses13() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		ICompilationUnit cu = getCompilationUnit("/P/x/y/A.java");
		IType type = cu.getType("A");
		assertTrue("scope on P should enclose type A", scope.encloses(type));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java project is not enclosed in a scope on the project (proj != src)
 * (resourcePath case)
 */
public void testScopeEncloses14() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertFalse("scope on P should not enclose \"/P\"", scope.encloses("/P"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java project is not enclosed in a scope on the project (proj != src)
 * (resourcePath case)
 */
public void testScopeEncloses14b() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertFalse("scope on P should not enclose \"/\"", scope.encloses("/"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java project is not enclosed in a scope on the project (proj != src)
 * (resourcePath case)
 */
public void testScopeEncloses14c() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertFalse("scope on P should not enclose \"\"", scope.encloses(""));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java project is not enclosed in a scope on the project (proj != src)
 * (Java element case)
 */
public void testScopeEncloses15() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertFalse("scope on P should enclose P", scope.encloses(project));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a root is enclosed in a scope on the project (proj != src)
 * (resource path case)
 */
public void testScopeEncloses16() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src\"", scope.encloses("/P/src"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a root is enclosed in a scope on the project (proj != src)
 * (resource path with traling slash case)
 */
public void testScopeEncloses17() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/\"", scope.encloses("/P/src/"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a root is enclosed in a scope on the project (proj != src)
 * (Java element case)
 */
public void testScopeEncloses18() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		IPackageFragmentRoot root = project.getPackageFragmentRoot(project.getProject().getFolder("src"));
		assertTrue("scope on P should enclose root P/src", scope.encloses(root));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package is enclosed in a scope on the project (proj != src)
 * (resource path case)
 */
public void testScopeEncloses19() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/x/y\"", scope.encloses("/P/src/x/y"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package is enclosed in a scope on the project (proj != src)
 * (resource path with trailing slash case)
 */
public void testScopeEncloses20() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/x/y/\"", scope.encloses("/P/src/x/y/"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package is enclosed in a scope on the project (proj != src)
 * (Java element case)
 */
public void testScopeEncloses21() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		IPackageFragment pkg = getPackage("/P/src/x/y");
		assertTrue("scope on P should enclose package x.y", scope.encloses(pkg));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a default package is enclosed in a scope on the project (proj != src)
 * (Java element case)
 */
public void testScopeEncloses22() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		IPackageFragment pkg = getPackage("/P/src");
		assertTrue("scope on P should enclose default package", scope.encloses(pkg));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit is enclosed in a scope on the project (proj != src)
 * (resource path case)
 */
public void testScopeEncloses23() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/x/y/A.java\"", scope.encloses("/P/src/x/y/A.java"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit is enclosed in a scope on the project (proj != src)
 * (Java element case)
 */
public void testScopeEncloses24() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		ICompilationUnit cu = getCompilationUnit("/P/src/x/y/A.java");
		assertTrue("scope on P should enclose compilation unit A.java", scope.encloses(cu));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit in the default package is enclosed in a scope on the project (proj != src)
 * (resource path case)
 */
public void testScopeEncloses25() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/A.java\"", scope.encloses("/P/src/A.java"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit in the default package is enclosed in a scope on the project (proj != src)
 * (Java element case)
 */
public void testScopeEncloses26() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		ICompilationUnit cu = getCompilationUnit("/P/src/A.java");
		assertTrue("scope on P should enclose compilation unit A.java", scope.encloses(cu));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a source type is enclosed in a scope on the project (proj != src)
 * (Java element case)
 */
public void testScopeEncloses27() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		ICompilationUnit cu = getCompilationUnit("/P/src/x/y/A.java");
		IType type = cu.getType("A");
		assertTrue("scope on P should enclose type A", scope.encloses(type));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java project is not enclosed in a scope on the project (proj != src/)
 * (resourcePath case)
 */
public void testScopeEncloses28() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertFalse("scope on P should not enclose \"/P\"", scope.encloses("/P"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java project is not enclosed in a scope on the project (proj != src/)
 * (resourcePath case)
 */
public void testScopeEncloses28b() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertFalse("scope on P should not enclose \"/P\"", scope.encloses("/"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java project is not enclosed in a scope on the project (proj != src/)
 * (resourcePath case)
 */
public void testScopeEncloses28c() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertFalse("scope on P should not enclose \"/P\"", scope.encloses(""));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a Java project is not enclosed in a scope on the project (proj != src/)
 * (Java element case)
 */
public void testScopeEncloses29() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertFalse("scope on P should enclose P", scope.encloses(project));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a root is enclosed in a scope on the project (proj != src/)
 * (resource path case)
 */
public void testScopeEncloses30() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src\"", scope.encloses("/P/src"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a root is enclosed in a scope on the project (proj != src/)
 * (resource path with traling slash case)
 */
public void testScopeEncloses31() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/\"", scope.encloses("/P/src/"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a root is enclosed in a scope on the project (proj != src/)
 * (Java element case)
 */
public void testScopeEncloses32() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		IPackageFragmentRoot root = project.getPackageFragmentRoot(project.getProject().getFolder("src"));
		assertTrue("scope on P should enclose root P/src", scope.encloses(root));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package is enclosed in a scope on the project (proj != src/)
 * (resource path case)
 */
public void testScopeEncloses33() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/x/y\"", scope.encloses("/P/src/x/y"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package is enclosed in a scope on the project (proj != src/)
 * (resource path with trailing slash case)
 */
public void testScopeEncloses34() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/x/y/\"", scope.encloses("/P/src/x/y/"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package is enclosed in a scope on the project (proj != src/)
 * (Java element case)
 */
public void testScopeEncloses35() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		IPackageFragment pkg = getPackage("/P/src/x/y");
		assertTrue("scope on P should enclose package x.y", scope.encloses(pkg));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a default package is enclosed in a scope on the project (proj != src/)
 * (Java element case)
 */
public void testScopeEncloses36() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		IPackageFragment pkg = getPackage("/P/src");
		assertTrue("scope on P should enclose default package", scope.encloses(pkg));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit is enclosed in a scope on the project (proj != src/)
 * (resource path case)
 */
public void testScopeEncloses37() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/x/y/A.java\"", scope.encloses("/P/src/x/y/A.java"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit is enclosed in a scope on the project (proj != src/)
 * (Java element case)
 */
public void testScopeEncloses38() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		ICompilationUnit cu = getCompilationUnit("/P/src/x/y/A.java");
		assertTrue("scope on P should enclose compilation unit A.java", scope.encloses(cu));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit in the default package is enclosed in a scope on the project (proj != src/)
 * (resource path case)
 */
public void testScopeEncloses39() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		assertTrue("scope on P should enclose \"/P/src/A.java\"", scope.encloses("/P/src/A.java"));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit in the default package is enclosed in a scope on the project (proj != src/)
 * (Java element case)
 */
public void testScopeEncloses40() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		ICompilationUnit cu = getCompilationUnit("/P/src/A.java");
		assertTrue("scope on P should enclose compilation unit A.java", scope.encloses(cu));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a source type is enclosed in a scope on the project (proj != src/)
 * (Java element case)
 */
public void testScopeEncloses41() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src/"}, "bin");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		ICompilationUnit cu = getCompilationUnit("/P/src/x/y/A.java");
		IType type = cu.getType("A");
		assertTrue("scope on P should enclose type A", scope.encloses(type));
	} finally {
		deleteProject("P");
	}
}

/**
 * Bug 101022: [search] JUnit Test Runner on folder runs tests outside directory
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=101022"
 */
public void testBug101022() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P1", new String[] {"src", "test", "test2"}, "bin");
		createFile(
			"/P1/src/Test.java",
			"public class Test {\n" +
			"	protected void foo() {}\n" +
			"}"
		);
		createFile(
			"/P1/test/Test.java",
			"public class Test {\n" +
			"	protected void foo() {}\n" +
			"}"
		);
		createFile(
			"/P1/test2/Test.java",
			"public class Test {\n" +
			"	protected void foo() {}\n" +
			"}"
		);
		IPackageFragmentRoot root = project.getPackageFragmentRoot(getFolder("/P1/test"));
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {root});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		search("foo", METHOD, DECLARATIONS, scope, resultCollector);
		assertSearchResults(
			"test/Test.java [in P1] void Test.foo() [foo]",
			resultCollector);
	}
	finally {
		deleteProject("P1");
	}
}

/**
 * Bug 101426: Search doesn't work with imported plugin
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=101426"
 */
public void testBug101426() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P1", new String[] {"src/", "test/", "test2/"}, new String[] {"JCL_LIB"}, "bin");
		createFile(
			"/P1/src/Test.java",
			"public interface ITest {\n" +
			"}"
		);
		createFile(
			"/P1/test/Test.java",
			"public class Test {\n" +
			"	ITest test;\n" +
			"}"
		);
		createFile(
			"/P1/test2/Test.java",
			"public class Test2 {\n" +
			"	ITest test;\n" +
			"}"
		);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		search("ITest", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"test/Test.java [in P1] Test.test [ITest]\n" +
			"test2/Test.java [in P1] Test2.test [ITest]",
			resultCollector);
	}
	finally {
		deleteProject("P1");
	}
}

/**
 * Bug 101777: [search] selecting class with a main type ignores the default package
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=101777"
 */
public void testBug101777() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P1");
		createFile(
			"/P1/Test.java",
			"public class Test {\n" +
			"	public static void main(String[] args) {}\n" +
			"}"
		);
		IPackageFragment[] fragments = project.getPackageFragments();
		IPackageFragment defaultFragment = null;
		for (int i = 0; i < fragments.length; i++) {
			IPackageFragment fragment = fragments[i];
			if (fragment.getElementName().length() == 0) {
				defaultFragment = fragment;
				break;
			}
		}
		assertNotNull("We should have a default fragment for project P1!", defaultFragment);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {defaultFragment});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		search("main(String[]) void", METHOD, DECLARATIONS, scope, resultCollector);
		assertSearchResults(
			"Test.java [in P1] void Test.main(String[]) [main]",
			resultCollector);
	}
	finally {
		deleteProject("P1");
	}
}

/**
 * Bug 119203: Search doesn't work with imported plugin
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=119203"
 * WARNING: Fix for this bug has been disabled due to bad regression
 *
 * Bug 127048: [search] References to Java element 'CorrectionEngine' not found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=127048"
 */
public void testBug119203() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P1", new String[] {"src"}, "bin");
		createFile(
			"/P1/src/Test.java",
			"public class Test {\n" +
			"}"
		);
		createFile(
			"/P1/src/X.java",
			"public class X {\n" +
			"	Test test;\n" +
			"}"
		);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		search("Test", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/X.java [in P1] X.test [Test]",
			resultCollector);
	}
	finally {
		deleteProject("P1");
	}
}
/**
 * @bug 179199: [search] Open type throws NPE during Items filtering
 * @test Ensure that NPE does no longer happen when output location is also set as class folder in a project build path
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=179199"
 */
public void testBug179199() throws CoreException {
	try {
		// Create project and files
		IJavaProject project = createJavaProject("P1", new String[] {"src"}, new String[] {"bin"}, "bin");
		createFile(
			"/P1/src/Test.java",
			"public class Test {\n" +
			"}"
		);
		createFile(
			"/P1/src/X.java",
			"public class X {\n" +
			"	Test test;\n" +
			"}"
		);
		waitUntilIndexesReady();

		// Build to create .class files
		project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForAutoBuild();

		// Remove source file to peek the class file in 'bin' instead while searching all type names
		deleteFile("/P1/src/Test.java");

		// Index the output location as it is a library for the project
		IndexManager indexManager = JavaModelManager.getIndexManager();
		indexManager.indexLibrary(new Path("/P1/bin"), project.getProject(), null);
		waitUntilIndexesReady();

		// Search for all types
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			"Test".toCharArray(),
			SearchPattern.R_PREFIX_MATCH,
			IJavaSearchConstants.TYPE,
			scope,
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);

		// Verify results
		assertSearchResults(
			"Test (not open) [in Test.class [in <default> [in bin [in P1]]]]",
			collector
		);
	}
	finally {
		deleteProject("P1");
	}
}
/**
 * @bug 250211: [search] Organize Imports Hangs
 * @test Ensure that JavaSearchScope creation does not take too much time.<br>
 * Note that this test does not make any assertion, it just creates several projects
 * with a huge dependency tree and create a scope on all of them.<br>
 * If the bug was back again, then this test would never finish!
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211"
 */
public void testBug250211() throws CoreException {
	final int max = 50;
	final IJavaProject[] projects = new IJavaProject[max];
	try {
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < max; i++){
					String projectName = "P"+i;
					String[] dependents = new String[max-1];
					boolean[] exportedProjects = new boolean[max-1];
					for (int j = 0; j < max; j++){
						int idx = -1;
						if (j < i) {
							idx = j;
						} else if (j > i) {
							idx = j-1;
						}
						if (idx != -1) {
							dependents[idx] = "/P"+j;
							exportedProjects[idx] = true; // export all projects
						}
					}
					projects[i] = createJavaProject(projectName, new String[]{"src"}, new String[]{"JCL_LIB"}, dependents, exportedProjects, "bin");
				}
			}
		},
		null);
		SearchEngine.createJavaSearchScope(projects);
	}
	finally {
		for (int i = 0; i < max; i++){
			assertNotNull("Unexpected null project!", projects[i]);
			deleteProject(projects[i]);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=397818
public void testBug397818() throws CoreException {
	try {
		createJavaProject("P1", new String[] {"src"}, new String[] {}, "bin");

		createFolder("/P1/new folder");
		IFile newFile = createFile("/P1/new folder/testindex.index", "");
		try {
			URL newURL = newFile.getLocationURI().toURL();
			IndexLocation indexLoc = IndexLocation.createIndexLocation(newURL);
			assertTrue("Malformed index location", indexLoc.getIndexFile().exists());
		} catch (MalformedURLException e) {
			fail("Malformed index location");
		}
	} finally {
		deleteProject("P1");
	}
}
/*
 * Test that we can find methods in a type from a jar with modules,
 * using a type in the JRE as an example.
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/332
 */
public void testModuleJarSearchBugGh332() throws Exception {
	String testProjectName = "gh322ModuleJarSearchBug";
	try {
		IJavaProject project = setupModuleProject(testProjectName, new String[] {"src"}, new String[0], null);
		waitForAutoBuild();
		waitUntilIndexesReady();

		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		String timerTypeFqn = "java.util.Timer";
		IType timerType = project.findType(timerTypeFqn);
		assertNotNull("Failed to find JRE type: " + timerTypeFqn, timerType);
		String methodName = "sched";
		IMethod schedMethod = null;
		for (IMethod method : timerType.getMethods()) {
			if (methodName.equals(method.getElementName())) {
				schedMethod = method;
			}
		}
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { schedMethod.getAncestor(IJavaElement.PACKAGE_FRAGMENT) });
		assertNotNull("Failed to find method: " + methodName + ", in JRE type: " + timerTypeFqn, schedMethod);
		int agnosticMatchRule = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH;
		SearchPattern pattern = SearchPattern.createPattern(schedMethod, IJavaSearchConstants.REFERENCES, agnosticMatchRule);
		SearchEngine searchEngine = new SearchEngine();
		searchEngine.search(pattern, new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, scope, resultCollector, new NullProgressMonitor());

		String foundReferences = resultCollector.toString();
		assertFalse("Expected search to find references of method: " + schedMethod, foundReferences.isEmpty());
		List<String> results = Arrays.asList(foundReferences.split("\n"));
		String[] expectedResults = {
				"void java.util.Timer.schedule(java.util.TimerTask, long)",
				"void java.util.Timer.schedule(java.util.TimerTask, java.util.Date)",
				"void java.util.Timer.schedule(java.util.TimerTask, long, long)",
				"void java.util.Timer.schedule(java.util.TimerTask, java.util.Date, long)",
				"void java.util.Timer.scheduleAtFixedRate(java.util.TimerTask, long, long)",
				"void java.util.Timer.scheduleAtFixedRate(java.util.TimerTask, java.util.Date, long)",
		};
		assertEquals("Unexpected search results:\n" + foundReferences, expectedResults.length, results.size());
		Set<String> notFound = new LinkedHashSet<>();
		notFound.addAll(Arrays.asList(expectedResults));
		for (String expectedResult : expectedResults) {
			boolean isFound = false;
			for (String match : results) {
				if (match.contains(expectedResult)) {
					isFound = true;
					break;
				}
			}
			if (isFound) {
				notFound.remove(expectedResult);
			}
		}
		assertTrue("Unexpected search result\nExpected:\n" + String.join("\n", expectedResults) + "\nActual:\n" + foundReferences + "\nExpected but not found:\n" + String.join("\n", notFound),
				notFound.isEmpty());
	} finally {
		JavaCore.setOptions(getDefaultJavaCoreOptions());
		deleteProject(testProjectName);
	}
}

}
