/*******************************************************************************
 * Copyright (c) 2024, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.javac;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegressionTests {

	private static IProject project;

	@BeforeClass
	public static void beforeClass() throws Exception {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject p: projects) {
			p.delete(false, true, null);
		}
	}

	@Before
	public void setUp() throws Exception {
		project = importProject("projects/dummy");
	}

	@After
	public void cleanUp() throws Exception {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject p: projects) {
			p.delete(false, true, null);
		}
	}

	@Test
	public void testCheckBuild() throws Exception {
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		assertEquals(Set.of("A.class", "B.class", "pack"),
				new HashSet<>(Arrays.asList(new File(project.getLocation().toFile(), "bin").list())));
		assertArrayEquals(new String[] { "Packaged.class" },
				new File(project.getLocation().toFile(), "bin/pack").list());
	}

	@Test
	public void testGetDOMForClassWithSource() throws Exception {
		IJavaProject javaProject = JavaCore.create(project);
		IType arrayList = javaProject.findType("java.util.ArrayList");
		IClassFile classFile = (IClassFile)arrayList.getAncestor(IJavaElement.CLASS_FILE);
		var unit = classFile.getWorkingCopy((WorkingCopyOwner)null, null);
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource(unit);
		parser.setProject(javaProject);
		parser.createAST(null);
	}

	@Test
	public void testBuildReferenceOtherProjectSource() throws Exception {
		IWorkspaceDescription wsDesc = ResourcesPlugin.getWorkspace().getDescription();
		wsDesc.setAutoBuilding(false);
		ResourcesPlugin.getWorkspace().setDescription(wsDesc);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IProject dependent = importProject("projects/dependent");
		// at this stage, no .class file exists, so we test that resolution through sourcePath/referenced projects work
		ICompilationUnit unit = (ICompilationUnit)JavaCore.create(dependent).findElement(Path.fromOSString("D.java"));
		unit.becomeWorkingCopy(null);
		var dom = unit.reconcile(AST.getJLSLatest(), true, unit.getOwner(), null);
		assertArrayEquals(new IProblem[0], dom.getProblems());
	}

	@Test
	public void testBuildMultipleOutputDirectories() throws Exception {
		IProject p = importProject("projects/multiOut");
		p.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
		p.build(IncrementalProjectBuilder.FULL_BUILD, null);
		assertTrue(p.getFolder("bin").getFile("B.class").exists());
		assertTrue(p.getFolder("bin2").getFile("A.class").exists());
	}

	// https://github.com/eclipse-jdtls/eclipse-jdt-core-incubator/issues/1016
	@Test
	public void testBuildMultipleOutputDirectories2() throws Exception {
		IProject proj1 = importProject("projects/multipleOutputDirectories/proj1");
		IProject proj2 = importProject("projects/multipleOutputDirectories/proj2");
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForBackgroundJobs();
		List<IMarker> errors = findMarkers(proj1, IMarker.SEVERITY_ERROR);
		assertTrue(errors.isEmpty());
		errors = findMarkers(proj2, IMarker.SEVERITY_ERROR);
		assertTrue(errors.isEmpty());
		ICompilationUnit unit = (ICompilationUnit)JavaCore.create(proj2).findElement(Path.fromOSString("proj2/Main.java"));
		unit.becomeWorkingCopy(null);
		var dom = unit.reconcile(AST.getJLSLatest(), true, unit.getOwner(), null);
		assertArrayEquals(new IProblem[0], dom.getProblems());
	}

	// https://github.com/eclipse-jdtls/eclipse-jdt-core-incubator/issues/955
	@Test
	public void testlombok() throws Exception {
		Assume.assumeTrue("javac is not set, skip it", CompilationUnit.DOM_BASED_OPERATIONS);
		IProject proj = importProject("projects/lomboktest");
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForBackgroundJobs();
		List<IMarker> errors = findMarkers(proj, IMarker.SEVERITY_ERROR);
		assertTrue(errors.isEmpty());
		ICompilationUnit unit = (ICompilationUnit)JavaCore.create(proj).findElement(Path.fromOSString("org/sample/Main.java"));
		unit.becomeWorkingCopy(null);
		var dom = unit.reconcile(AST.getJLSLatest(), true, unit.getOwner(), null);
		assertArrayEquals(new IProblem[0], dom.getProblems());
	}

	static IProject importProject(String locationInBundle) throws URISyntaxException, IOException, CoreException {
		File file = new File(FileLocator.toFileURL(RegressionTests.class.getResource("/" + locationInBundle + "/.project")).toURI());
		IPath dotProjectPath = Path.fromOSString(file.getAbsolutePath());
		IProjectDescription projectDescription = ResourcesPlugin.getWorkspace()
				.loadProjectDescription(dotProjectPath);
		projectDescription.setLocation(dotProjectPath.removeLastSegments(1));
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectDescription.getName());
		proj.create(projectDescription, null);
		proj.open(null);
		proj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		return proj;
	}

	// copied from org.eclipse.jdt.ls.core.internal.ResourceUtils.findMarkers(IResource, Integer...)
	static List<IMarker> findMarkers(IResource resource, Integer... severities) throws CoreException {
		if (resource == null) {
			return null;
		}
		Set<Integer> targetSeverities = severities == null ? Collections.emptySet()
				: new HashSet<>(Arrays.asList(severities));
		IMarker[] allmarkers = resource.findMarkers(null /* all markers */, true /* subtypes */,
				IResource.DEPTH_INFINITE);
		List<IMarker> markers = Stream.of(allmarkers).filter(
				m -> targetSeverities.isEmpty() || targetSeverities.contains(m.getAttribute(IMarker.SEVERITY, 0)))
				.collect(Collectors.toList());
		return markers;
	}

	protected void waitForBackgroundJobs() throws Exception {
		JobHelpers.waitForJobsToComplete(new NullProgressMonitor());
		JobHelpers.waitUntilIndexesReady();
	}

}
