/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * These test ensure that modifications in Java projects are correctly reported as
 * IJavaElementDeltas.
 */
public class JavaElementDeltaTests extends ModifyingResourceTests {
	
	public class DeltaListener implements IElementChangedListener {
		ArrayList deltas;
		int eventType;
		
		public DeltaListener() {
			DeltaListener.this.deltas = new ArrayList();
			DeltaListener.this.eventType = -1;
		}
		public DeltaListener(int eventType) {
			DeltaListener.this.deltas = new ArrayList();
			DeltaListener.this.eventType = eventType;
		}
		public void elementChanged(ElementChangedEvent event) {
			if (DeltaListener.this.eventType == -1 || event.getType() == DeltaListener.this.eventType) {
				DeltaListener.this.deltas.add(event.getDelta());
			}
		}
		public void flush() {
			DeltaListener.this.deltas = new ArrayList();
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0, length = DeltaListener.this.deltas.size(); i < length; i++) {
				IJavaElementDelta[] projects = ((IJavaElementDelta)this.deltas.get(i)).getAffectedChildren();
				for (int j=0, projectsLength=projects.length; j<projectsLength; j++) {
					buffer.append(projects[j]);
					if (j != projectsLength-1) {
						buffer.append("\n");
					}
				}
				if (i != length-1) {
					buffer.append("\n\n");
				}
			}
			return buffer.toString();
		}
	}

public JavaElementDeltaTests(String name) {
	super(name);
}



public static Test suite() {
	TestSuite suite = new Suite(JavaElementDeltaTests.class.getName());
	
	// add/remove/open/close projects
	suite.addTest(new JavaElementDeltaTests("testAddJavaProject"));
	suite.addTest(new JavaElementDeltaTests("testRemoveJavaProject"));
	suite.addTest(new JavaElementDeltaTests("testRemoveAddJavaProject"));
	suite.addTest(new JavaElementDeltaTests("testRemoveAddBinaryProject"));
	suite.addTest(new JavaElementDeltaTests("testAddJavaNature"));
	suite.addTest(new JavaElementDeltaTests("testRemoveJavaNature"));
	suite.addTest(new JavaElementDeltaTests("testOpenJavaProject"));
	suite.addTest(new JavaElementDeltaTests("testCloseJavaProject"));
	suite.addTest(new JavaElementDeltaTests("testAddTwoJavaProjects"));
	suite.addTest(new JavaElementDeltaTests("testAddTwoJavaProjectsWithExtraSetClasspath"));
	suite.addTest(new JavaElementDeltaTests("testDeleteProjectSetCPAnotherProject"));

	suite.addTest(new JavaElementDeltaTests("testRenameJavaProject"));

	// non-java projects
	suite.addTest(new JavaElementDeltaTests("testAddNonJavaProject"));
	suite.addTest(new JavaElementDeltaTests("testRemoveNonJavaProject"));
	suite.addTest(new JavaElementDeltaTests("testRemoveNonJavaProjectUpdateDependent"));
	suite.addTest(new JavaElementDeltaTests("testRemoveNonJavaProjectUpdateDependent2"));
	suite.addTest(new JavaElementDeltaTests("testOpenNonJavaProject"));
	suite.addTest(new JavaElementDeltaTests("testCloseNonJavaProject"));
	suite.addTest(new JavaElementDeltaTests("testCloseNonJavaProjectUpdateDependent"));
	suite.addTest(new JavaElementDeltaTests("testRenameProject"));
	// TO DO: suite.addTest(new JavaElementDeltaTests("testChangeNonJavaProject"));
	
	// package fragment roots
	suite.addTest(new JavaElementDeltaTests("testDeleteInnerJar"));
	
	// packages
	suite.addTest(new JavaElementDeltaTests("testAddPackageSourceIsBin"));
	suite.addTest(new JavaElementDeltaTests("testRenameOuterPkgFragment"));
	
	// compilation units
	suite.addTest(new JavaElementDeltaTests("testAddCuInDefaultPkg1"));
	suite.addTest(new JavaElementDeltaTests("testAddCuInDefaultPkg2"));
	suite.addTest(new JavaElementDeltaTests("testMoveCuInEnclosingPkg"));
	
	// commit/save working copies
	suite.addTest(new JavaElementDeltaTests("testModifyMethodBodyAndSave"));
	suite.addTest(new JavaElementDeltaTests("testRenameMethodAndSave"));
	suite.addTest(new JavaElementDeltaTests("testSaveWorkingCopy"));
	suite.addTest(new JavaElementDeltaTests("testWorkingCopyCommit"));
	
	// managed working copies
	suite.addTest(new JavaElementDeltaTests("testCreateSharedWorkingCopy"));
	suite.addTest(new JavaElementDeltaTests("testDestroySharedWorkingCopy"));
	
	// non-java resources
	suite.addTest(new JavaElementDeltaTests("testMoveResInDotNamedFolder"));
	suite.addTest(new JavaElementDeltaTests("testMergeResourceDeltas"));
	suite.addTest(new JavaElementDeltaTests("testAddFileToNonJavaProject"));
	
	// listeners
	suite.addTest(new JavaElementDeltaTests("testListenerAutoBuild"));
	suite.addTest(new JavaElementDeltaTests("testListenerReconcile"));
	suite.addTest(new JavaElementDeltaTests("testListenerPostChange"));
	
	// classpath
	suite.addTest(new JavaElementDeltaTests("testSetClasspathVariable1"));
	suite.addTest(new JavaElementDeltaTests("testSetClasspathVariable2"));
	suite.addTest(new JavaElementDeltaTests("testChangeRootKind"));
	suite.addTest(new JavaElementDeltaTests("testOverwriteClasspath"));
	suite.addTest(new JavaElementDeltaTests("testRemoveCPEntryAndRoot1"));
	suite.addTest(new JavaElementDeltaTests("testRemoveCPEntryAndRoot2"));
	suite.addTest(new JavaElementDeltaTests("testRemoveCPEntryAndRoot3"));
	suite.addTest(new JavaElementDeltaTests("testAddDotClasspathFile"));
	suite.addTest(new JavaElementDeltaTests("testSetClasspathOnFreshProject"));
	
	// batch operations
	suite.addTest(new JavaElementDeltaTests("testBatchOperation"));
	
	// build
	suite.addTest(new JavaElementDeltaTests("testBuildProjectUsedAsLib"));
	
	// output locations
	suite.addTest(new JavaElementDeltaTests("testModifyOutputLocation1"));
	suite.addTest(new JavaElementDeltaTests("testModifyOutputLocation2"));
	suite.addTest(new JavaElementDeltaTests("testModifyOutputLocation3"));
	suite.addTest(new JavaElementDeltaTests("testChangeCustomOutput"));
	
	return suite;
}

/*
 * Add cu in default package test (proj=src=bin).
 */
public void testAddCuInDefaultPkg1() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.startDeltas();
		this.createFile("P/X.java",
			"public class X {\n" +
			"}");
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			X.java[+]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Add cu in default package test (proj!=src!=bin).
 */
public void testAddCuInDefaultPkg2() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.startDeltas();
		this.createFile("P/src/X.java",
			"public class X {\n" +
			"}");
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			X.java[+]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Add the .classpath file to a Java project that was missing it.
 * (regression test for bug 26128 packages don't appear in package explorer view)
 */
public void testAddDotClasspathFile() throws CoreException {
	try {
		this.createProject("P");
		this.createFolder("/P/src");
		
		// add Java nature
		this.editFile(
			"/P/.project",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +			"<projectDescription>\n" +			"	<name>Test</name>\n" +			"	<comment></comment>\n" +			"	<projects>\n" +			"	</projects>\n" +			"	<buildSpec>\n" +			"		<buildCommand>\n" +			"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +			"			<arguments>\n" +			"			</arguments>\n" +			"		</buildCommand>\n" +			"	</buildSpec>\n" +			"	<natures>\n" +			"		<nature>org.eclipse.jdt.core.javanature</nature>\n" +			"	</natures>\n" +			"</projectDescription>"
		);
		
		this.startDeltas();
		this.createFile(
			"P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +			"<classpath>\n" +			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +			"</classpath>"
		);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" + 
			"	[project root][*]: {REMOVED FROM CLASSPATH}\n" + 
			"	src[*]: {ADDED TO CLASSPATH}\n" + 
			"	ResourceDelta(/P/.classpath)[+]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that a delta is not fired when a file is added to a non-java project.
 * (regression test for bug 18698 Seeing non-java projects in package view)
 */
public void testAddFileToNonJavaProject() throws CoreException {
	try {
		this.createProject("P");
		this.startDeltas();
		this.createFile("/P/toto.txt", "");
		assertDeltas(
			"Unexpected delta", 
			""
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Add the java nature to an existing project.
 */
public void testAddJavaNature() throws CoreException {
	try {
		this.createProject("P");
		this.startDeltas();
		this.addJavaNature("P");
		assertDeltas(
			"Unexpected delta", 
			"P[+]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Add a java project.
 */
public void testAddJavaProject() throws CoreException {
	try {
		this.startDeltas();
		this.createJavaProject("P", new String[] {""}, "");
		assertDeltas(
			"Unexpected delta", 
			"P[+]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Add a non-java project.
 */
public void testAddNonJavaProject() throws CoreException {
	try {
		this.startDeltas();
		this.createProject("P");
		assertDeltas("Should get no delta", "");
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
public void testAddPackageSourceIsBin() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "src");
		this.startDeltas();
		this.createFolder("P/src/x");
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		x[+]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Add 2 java projects in an IWorkspaceRunnable.
 */
public void testAddTwoJavaProjects() throws CoreException {
	try {
		this.startDeltas();
		ResourcesPlugin.getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					createJavaProject("P1", new String[] {""}, "");
					createJavaProject("P2", new String[] {"src"}, "bin");
				}
			},
			null);
		assertEquals(
			"Unexpected delta", 
			"P1[+]: {}\n" +
			"P2[+]: {}", 
			this.getSortedByProjectDeltas());
	} finally {
		this.stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}

/*
 * Add 2 java projects in an IWorkspaceRunnable.
 */
public void testAddTwoJavaProjectsWithExtraSetClasspath() throws CoreException {
	try {
		this.startDeltas();
		ResourcesPlugin.getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IJavaProject p1 = createJavaProject("P1", new String[] {""}, "");
					// should be a no-op and no extra delta volley should be fired
					p1.setRawClasspath(p1.getRawClasspath(), p1.getOutputLocation(), null);
					createJavaProject("P2", new String[] {"src"}, "bin");
				}
			},
			null);
		assertEquals(
			"Unexpected delta", 
			"P1[+]: {}\n" +
			"P2[+]: {}", 
			this.getSortedByProjectDeltas());
	} finally {
		this.stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Batch operation test. */
public void testBatchOperation() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFolder("P/src/x");
		this.createFile(
			"P/src/x/A.java",
			"package x;\n" +
			"public class A {\n" +
			"}");
		this.startDeltas();
		JavaCore.run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					ICompilationUnit unit = getCompilationUnit("P/src/x/A.java");
					unit.createType("class B {}", null, false, monitor);
					unit.getType("A").createField("int i;", null, false, monitor);
				}
			},
			null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		x[*]: {CHILDREN}\n" +
			"			A.java[*]: {CHILDREN | FINE GRAINED}\n" +
			"				B[+]: {}\n" +
			"				A[*]: {CHILDREN | FINE GRAINED}\n" +
			"					i[+]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensures that if a project's output folder is used as a lib folder in another project, building
 * the first project results in the correct delta in the other project.
 */
public void testBuildProjectUsedAsLib() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {"src1"}, new String[] {"JCL_LIB"}, "bin1");
		this.createJavaProject("P2", new String[] {"src2"}, new String[] {"/P1/bin1"}, "bin2");
		this.createFile(
			"/P1/src1/X.java",
			"public class X {\n" +			"}"
		);
		this.startDeltas();
		
		// full build
		p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		assertDeltas(
			"Unexpected delta (1)",
			"P2[*]: {CHILDREN}\n" + 
			"	bin1[*]: {CHILDREN}\n" + 
			"		[default][*]: {CHILDREN}\n" + 
			"			X.class[+]: {}"
			);
			
		this.editFile(
			"/P1/src1/X.java",
			"public class X {\n" +
			"  void foo() {}\n" +
			"}\n" +
			"class Y {\n" +			"}"
		);
		this.clearDeltas();

		// incremental buid
		p1.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		assertDeltas(
			"Unexpected delta (2)",
			"P2[*]: {CHILDREN}\n" + 
			"	bin1[*]: {CHILDREN}\n" + 
			"		[default][*]: {CHILDREN}\n" + 
			"			X.class[*]: {CONTENT}\n" + 
			"			Y.class[+]: {}"
			);
	} finally {
		this.stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}

/**
 * Ensures that changing the custom output folder of a source entry
 * triggers a F_REMOVED_FROM_CLASSPATH and F_ADDED_TO_CLASSPATH delta.
 */
public void testChangeCustomOutput() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, "bin", new String[] {"bin1"});
		this.startDeltas();
		this.setClasspath(
			proj, 
			new IClasspathEntry[] {
				JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], new Path("/P/bin2"))
			});
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/**
 * Ensures that the setting the classpath with a library entry
 * triggers a F_REMOVED_FROM_CLASSPATH and F_ADDED_TO_CLASSPATH delta.
 */
public void testChangeRootKind() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, "bin");
		this.startDeltas();
		this.setClasspath(
			proj, 
			new IClasspathEntry[] {
				JavaCore.newLibraryEntry(new Path("/P/src"), null, null, false)
			});
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}

public void testCreateSharedWorkingCopy() throws CoreException {
	IWorkingCopy copy = null;
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFile("P/X.java",
			"public class X {\n" +
			"}");
		ICompilationUnit unit = this.getCompilationUnit("P", "", "", "X.java");
		this.startDeltas();
		copy = (IWorkingCopy)unit.getSharedWorkingCopy(null, null, null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			[Working copy] X.java[+]: {}"
		);
	} finally {
		this.stopDeltas();
		if (copy != null) copy.destroy();
		this.deleteProject("P");
	}
}
/*
 * Ensure that deleting a jar that is in a folder and that is on the classpath reports
 * a removed  pkg fragment root delta.
 * (regression test for bug 27068 Elements in the Package Explorer are displayed but don't more exist [package explorer])
 * 
 */
public void testDeleteInnerJar() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, new String[] {"/P/lib/x.jar"}, "bin");
		this.createFolder("/P/lib");
		IFile file = this.createFile("/P/lib/x.jar", "");
		this.startDeltas();
		file.delete(false, null);
		assertDeltas(
			"Unexpected deltas",
			"P[*]: {CHILDREN}\n" + 
			"	lib/x.jar[-]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that deleting a project and setting the classpath on another project
 * in an IWorkspaceRunnable doesn't throw a NullPointerException
 * (regression test for bug 25197 NPE importing external plugins)
 */
public void testDeleteProjectSetCPAnotherProject() throws CoreException {
	final IJavaProject project = this.createJavaProject("P1", new String[] {"src"}, "bin");
	this.createJavaProject("P2", new String[] {}, "");

	try {
		this.startDeltas();
		JavaCore.run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteProject("P2");
					project.setRawClasspath(
						new IClasspathEntry[] {
							JavaCore.newSourceEntry(project.getPath())
						},
						null);
				}
			},
			null);
		assertDeltas(
			"Unexpected deltas",
			"P1[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	[project root][*]: {ADDED TO CLASSPATH}\n" + 
			"	ResourceDelta(/P1/.classpath)[*]\n" + 
			"P2[-]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}

/*
 * Close a java project.
 */
public void testCloseJavaProject() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		IProject project = this.getProject("P");
		this.startDeltas();
		project.close(null);
		assertDeltas(
			"Unexpected delta", 
			"P[-]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Close a non-java project.
 */
public void testCloseNonJavaProject() throws CoreException {
	try {
		this.createProject("P");
		IProject project = this.getProject("P");
		this.startDeltas();
		project.close(null);
		assertDeltas(
			"Unexpected delta", 
			""
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Closing a non-java project that contains a jar referenced in another project should produce
 * a delta on this other project.
 * (regression test for bug 19058 Closing non-java project doesn't remove root from java project)
 */
public void testCloseNonJavaProjectUpdateDependent() throws CoreException {
	try {
		this.createProject("SP");
		this.createFile("/SP/x.jar", "");
		this.createJavaProject("JP", new String[] {""}, new String[] {"/SP/x.jar"}, "");
		IProject project = this.getProject("SP");
		this.startDeltas();
		project.close(null);
		assertDeltas(
			"Unexpected delta", 
			"JP[*]: {CHILDREN}\n" +
			"	x.jar[-]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("SP");
		this.deleteProject("JP");
	}
}
public void testDestroySharedWorkingCopy() throws CoreException {
	IWorkingCopy copy = null;
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFile("P/X.java",
			"public class X {\n" +
			"}");
		ICompilationUnit unit = this.getCompilationUnit("P", "", "", "X.java");
		copy = (IWorkingCopy)unit.getSharedWorkingCopy(null, null, null);
		this.startDeltas();
		copy.destroy();
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			[Working copy] X.java[-]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensures that a delta listener that asks for PRE_AUTO_BUILD events gets those events 
 * and no others.
 */
public void testListenerAutoBuild() throws CoreException {
	DeltaListener listener = new DeltaListener(ElementChangedEvent.PRE_AUTO_BUILD);
	ICompilationUnit wc = null;
	try {
		this.createJavaProject("P", new String[] {""}, "");
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.PRE_AUTO_BUILD);
		
		// cu creation
		IPackageFragment pkg = this.getPackage("P");
		ICompilationUnit cu = pkg.createCompilationUnit(
			"X.java",
			"public class X {\n" +
			"}",
			false,
			null);
		assertEquals(
			"Unexpected delta after creating CU", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			X.java[+]: {}", 
			listener.toString());
		listener.flush();
		
		// type creation
		cu.createType(
			"class A {\n" +
			"}",
			cu.getType("X"),
			false,
			null);
		assertEquals(
			"Unexpected delta after creating type", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			X.java[*]: {CHILDREN | FINE GRAINED}\n" +
			"				A[+]: {}", 
			listener.toString());
		listener.flush();
		
		// non-java resource creation
		this.createFile("P/readme.txt", "");
		assertEquals(
			"Unexpected delta after creating non-java resource",
			"",
			listener.toString());
		listener.flush();
		
		// shared working copy creation
		wc = (ICompilationUnit)cu.getSharedWorkingCopy(null, null, null);
		assertEquals(
			"Unexpected delta after creating shared working copy",
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			[Working copy] X.java[+]: {}",
			listener.toString());
		listener.flush();
			
		// reconcile
		wc.getBuffer().setContents(
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}");
		wc.reconcile();
		assertEquals(
			"Unexpected delta after reconciling working copy",
			"",
			listener.toString());
		listener.flush();
		
		// commit
		wc.commit(false, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"X[*]: {CHILDREN | FINE GRAINED}\n" +
			"	foo[+]: {}\n" +
			"A[-]: {}",
			listener.toString());
		listener.flush();
		
		// shared working copy destruction
		wc.destroy();
		assertEquals(
			"Unexpected delta after destroying shared working copy",
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			[Working copy] X.java[-]: {}",
			listener.toString());
		listener.flush();
		wc = null;
		
			
	} finally {
		if (wc != null) wc.destroy();
		JavaCore.removeElementChangedListener(listener);
		this.deleteProject("P");
	}
}
/*
 * Ensures that a delta listener that asks for POST_CHANGE events gets those events 
 * and no others.
 */
public void testListenerPostChange() throws CoreException {
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_CHANGE);
	ICompilationUnit wc = null;
	try {
		this.createJavaProject("P", new String[] {""}, "");
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE);
		
		// cu creation
		IPackageFragment pkg = this.getPackage("P");
		ICompilationUnit cu = pkg.createCompilationUnit(
			"X.java",
			"public class X {\n" +
			"}",
			false,
			null);
		assertEquals(
			"Unexpected delta after creating CU", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			X.java[+]: {}", 
			listener.toString());
		listener.flush();
		
		// type creation
		cu.createType(
			"class A {\n" +
			"}",
			cu.getType("X"),
			false,
			null);
		assertEquals(
			"Unexpected delta after creating type", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			X.java[*]: {CHILDREN | FINE GRAINED}\n" +
			"				A[+]: {}", 
			listener.toString());
		listener.flush();
		
		// non-java resource creation
		this.createFile("P/readme.txt", "");
		assertEquals(
			"Unexpected delta after creating non-java resource",
			"P[*]: {CONTENT}\n" +
			"	ResourceDelta(/P/readme.txt)[+]",
			listener.toString());
		listener.flush();
		
		// shared working copy creation
		wc = (ICompilationUnit)cu.getSharedWorkingCopy(null, null, null);
		assertEquals(
			"Unexpected delta after creating shared working copy",
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			[Working copy] X.java[+]: {}",
			listener.toString());
		listener.flush();
			
		// reconcile
		wc.getBuffer().setContents(
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}");
		wc.reconcile();
		assertEquals(
			"Unexpected delta after reconciling working copy",
			"",
			listener.toString());
		listener.flush();
		
		// commit
		wc.commit(false, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			X.java[*]: {CHILDREN | FINE GRAINED}\n" +
			"				X[*]: {CHILDREN | FINE GRAINED}\n" +
			"					foo[+]: {}\n" +
			"				A[-]: {}",
			listener.toString());
		listener.flush();
		
		// shared working copy destruction
		wc.destroy();
		assertEquals(
			"Unexpected delta after destroying shared working copy",
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			[Working copy] X.java[-]: {}",
			listener.toString());
		listener.flush();
		wc = null;
		
			
	} finally {
		if (wc != null) wc.destroy();
		JavaCore.removeElementChangedListener(listener);
		this.deleteProject("P");
	}
}
/*
 * Ensures that a delta listener that asks for POST_RECONCILE events gets those events 
 * and no others.
 */
public void testListenerReconcile() throws CoreException {
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_RECONCILE);
	ICompilationUnit wc = null;
	try {
		this.createJavaProject("P", new String[] {""}, "");
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_RECONCILE);
		
		// cu creation
		IPackageFragment pkg = this.getPackage("P");
		ICompilationUnit cu = pkg.createCompilationUnit(
			"X.java",
			"public class X {\n" +
			"}",
			false,
			null);
		assertEquals(
			"Unexpected delta after creating CU", 
			"", 
			listener.toString());
		listener.flush();
		
		// type creation
		cu.createType(
			"class A {\n" +
			"}",
			cu.getType("X"),
			false,
			null);
		assertEquals(
			"Unexpected delta after creating type", 
			"", 
			listener.toString());
		listener.flush();
		
		// non-java resource creation
		this.createFile("P/readme.txt", "");
		assertEquals(
			"Unexpected delta after creating non-java resource",
			"",
			listener.toString());
		listener.flush();
		
		// shared working copy creation
		wc = (ICompilationUnit)cu.getSharedWorkingCopy(null, null, null);
		assertEquals(
			"Unexpected delta after creating shared working copy",
			"",
			listener.toString());
		listener.flush();
			
		// reconcile
		wc.getBuffer().setContents(
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}");
		wc.reconcile();
		assertEquals(
			"Unexpected delta after reconciling working copy",
			"X[*]: {CHILDREN | FINE GRAINED}\n" +
			"	foo[+]: {}\n" +
			"A[-]: {}",
			listener.toString());
		listener.flush();
		
		// commit
		wc.commit(false, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"",
			listener.toString());
		listener.flush();
		
		// shared working copy destruction
		wc.destroy();
		assertEquals(
			"Unexpected delta after destroying shared working copy",
			"",
			listener.toString());
		listener.flush();
		wc = null;
		
			
	} finally {
		if (wc != null) wc.destroy();
		JavaCore.removeElementChangedListener(listener);
		this.deleteProject("P");
	}
}
/**
 * Ensures that merging a java delta with another one that contains a resource delta
 * results in a java delta with the resource delta.
 * (regression test for 11210 ResourceDeltas are lost when merging deltas)
 */
public void testMergeResourceDeltas() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.startDeltas();
		ResourcesPlugin.getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					// an operation that creates a java delta without firing it
					IPackageFragment pkg = getPackageFragment("P", "", "");
					pkg.createCompilationUnit(
						"X.java",
						"public class X {\n" +
						"}",
						true,
						null);
						
					// an operation that generates a non java resource delta
					createFile("P/Y.txt", "");
				}
			},
			null
		);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			X.java[+]: {}\n" +
			"	ResourceDelta(/P/Y.txt)[+]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}

	
}
/*
 * Move a cu from a package to its enclosing package.
 * (regression test for bug 7033 Stale packages view after moving compilation units)
 */
public void testMoveCuInEnclosingPkg() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFolder("P/x/y");
		this.createFile("P/x/y/A.java",
			"package x.y;\n" +
			"public class A {\n" +
			"}");
		ICompilationUnit cu = this.getCompilationUnit("P/x/y/A.java"); 
		IPackageFragment pkg = this.getPackage("P/x");
		
		this.startDeltas();
		cu.move(pkg, null, null, true, null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		x.y[*]: {CHILDREN}\n" +
			"			A.java[-]: {MOVED_TO(A.java [in x [in [project root] [in P]]])}\n" +
			"		x[*]: {CHILDREN}\n" +
			"			A.java[+]: {MOVED_FROM(A.java [in x.y [in [project root] [in P]]])}"
		);
		assertEquals(
			"Unexpected children for package x",
			"x\n" +
			"  A.java\n" +
			"    package x\n" +
			"    class A",
			this.expandAll(pkg));
		assertEquals(
			"Unexpected children for package x.y",
			"x.y",
			this.expandAll(this.getPackage("P/x/y")));
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
public void testModifyMethodBodyAndSave() throws CoreException {
	ICompilationUnit workingCopy = null;
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFolder("P/x/y");
		this.createFile("P/x/y/A.java",
			"package x.y;\n" +
			"public class A {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}");
		ICompilationUnit cu = this.getCompilationUnit("P/x/y/A.java"); 
		workingCopy = (ICompilationUnit)cu.getWorkingCopy();
		workingCopy.getBuffer().setContents(
			"package x.y;\n" +
			"public class A {\n" +
			"  public void foo() {\n" +
			"    // method body change \n" +
			"  }\n" +
			"}");
		
		this.startDeltas();
		workingCopy.commit(true, null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		x.y[*]: {CHILDREN}\n" +
			"			A.java[*]: {CONTENT | FINE GRAINED}"
		);
	} finally {
		this.stopDeltas();
		if (workingCopy != null) {
			workingCopy.destroy();
		}
		this.deleteProject("P");
	}
}
/*
 * Ensures that modifying the project output location (i.e. simulate a build) doesn't report any delta.
 */
public void testModifyOutputLocation1() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		
		this.startDeltas();
		this.createFile("/P/bin/X.class", "");
		assertDeltas(
			"Unexpected delta",
			""
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensures that modifying a custom output location (i.e. simulate a build) doesn't report any delta.
 * (regression test for bug 27494 Source folder output folder shown in Package explorer)
 */
public void testModifyOutputLocation2() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin1", new String[] {"bin2"});
		
		this.startDeltas();
		this.createFile("/P/bin2/X.class", "");
		assertDeltas(
			"Unexpected delta",
			""
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensures that modifying a custom output location (i.e. simulate a build) doesn't report any delta.
 * (regression test for bug 27494 Source folder output folder shown in Package explorer)
 */
public void testModifyOutputLocation3() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src1", "src2"}, "bin", new String[] {"src1", null});
		
		this.startDeltas();
		this.createFile("/P/bin/X.class", "");
		assertDeltas(
			"Unexpected delta",
			""
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Move a non-java resources that is under a dot-named folder.
 * (regression test for bug 6687 Wrong JavaModel refresh after drag and drop outside folder with dot in name)
 */
public void testMoveResInDotNamedFolder() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, "");
		IProject project = this.getProject("P");
		this.createFolder("P/x.y");
		IFile file = this.createFile("P/x.y/test.txt", "");
		
		this.startDeltas();
		file.move(project.getFullPath().append("test.txt"), true, null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CONTENT}\n" +
			"	ResourceDelta(/P/test.txt)[+]\n" +
			"	ResourceDelta(/P/x.y)[*]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}


/*
 * Open a java project.
 */
public void testOpenJavaProject() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		IProject project = this.getProject("P");
		project.close(null);
		this.startDeltas();
		project.open(null);
		assertDeltas(
			"Unexpected delta", 
			"P[+]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Open a non-java project.
 */
public void testOpenNonJavaProject() throws CoreException {
	try {
		this.createProject("P");
		IProject project = this.getProject("P");
		project.close(null);
		this.startDeltas();
		project.open(null);
		assertDeltas(
			"Unexpected delta", 
			""
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/**
 * Ensures that .classpath overwrite is taken into account.
 * (regression test for bug 21420 Changing .classpath doesn't update JDT)
 */
public void testOverwriteClasspath() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFolder("P/src");
		this.createFolder("P/bin");
		final IFile newCP = this.createFile(
			"P/.classpath2", 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>");
		this.startDeltas();
		IWorkspaceRunnable run = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IFile oldCP = newCP.getParent().getFile(new Path(".classpath"));
				oldCP.delete(true, null);
				newCP.move(new Path("/P/.classpath"), true, null);
			}
		};
		this.getWorkspace().run(run, null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {REMOVED FROM CLASSPATH}\n" +
			"	src[*]: {ADDED TO CLASSPATH}\n" +
			"	ResourceDelta(/P/.classpath)[*]\n" +
			"	ResourceDelta(/P/.classpath2)[-]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}


/*
 * Remove the java nature of an existing java project.
 */
public void testRemoveJavaNature() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.startDeltas();
		this.removeJavaNature("P");
		assertDeltas(
			"Unexpected delta", 
			"P[-]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Remove then add a binary project (in a workspace runnable).
 * (regression test for 24775 Wrong delta when replacing binary project with source project)
 */
public void testRemoveAddBinaryProject() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		this.createFile("P/lib.jar", "");
		project.setRawClasspath(
			new IClasspathEntry[] {
				JavaCore.newLibraryEntry(new Path("/P/lib.jar"), null, null)
			},
			null
		);
		
		this.startDeltas();
		getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteProject("P");
					createJavaProject("P", new String[] {""}, "");
				}
			},
			null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" + 
			"	lib.jar[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	[project root][*]: {ADDED TO CLASSPATH}\n" + 
			"	ResourceDelta(/P/.classpath)[*]\n" + 
			"	ResourceDelta(/P/.project)[*]\n" + 
			"	ResourceDelta(/P/lib.jar)[-]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Remove then add a java project (in a workspace runnable).
 */
public void testRemoveAddJavaProject() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.startDeltas();
		getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteProject("P");
					createJavaProject("P", new String[] {""}, "");
				}
			},
			null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CONTENT}\n" +
			"	ResourceDelta(/P/.classpath)[*]\n" +
			"	ResourceDelta(/P/.project)[*]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Change the .classpath file so as to remove a classpath entry and remove the corresponding resource.
 * (regression test for bug 24517 type view does not notice when jar disappears)

 */
public void testRemoveCPEntryAndRoot1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		
		// ensure that the project is open (there are clients of the delta only if the project is open)
		project.open(null);
		 
		this.startDeltas();
		JavaCore.run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					editFile(
						"/P/.classpath",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<classpath>\n" +
						"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
						"</classpath>");
					deleteFolder("/P/src");
				}
			},
			null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P/.classpath)[*]\n" + 
			"	ResourceDelta(/P/src)[-]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Remove a classpath entry and remove the corresponding resource.
 * (regression test for bug 24517 type view does not notice when jar disappears)

 */
public void testRemoveCPEntryAndRoot2() throws CoreException {
	try {
		final IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		
		// ensure that the project is open (there are clients of the delta only if the project is open)
		project.open(null);
		 
		this.startDeltas();
		JavaCore.run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					project.setRawClasspath(new IClasspathEntry[] {}, null);
					deleteFolder("/P/src");
				}
			},
			null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P/.classpath)[*]\n" + 
			"	ResourceDelta(/P/src)[-]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Remove  the resource of a classpath entry and remove the  classpath entry.
 * (regression test for bug 24517 type view does not notice when jar disappears)

 */
public void testRemoveCPEntryAndRoot3() throws CoreException {
	try {
		final IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		
		// ensure that the project is open (there are clients of the delta only if the project is open)
		project.open(null);
		 
		this.startDeltas();
		JavaCore.run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteFolder("/P/src");
					project.setRawClasspath(new IClasspathEntry[] {}, null);
				}
			},
			null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P/.classpath)[*]\n" + 
			"	ResourceDelta(/P/src)[-]"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
public void testRemoveJavaProject() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.startDeltas();
		this.deleteProject("P");
		assertDeltas(
			"Unexpected delta", 
			"P[-]: {}"
		);
	} finally {
		this.stopDeltas();
	}
}
/*
 * Remove a non-java project.
 */
public void testRemoveNonJavaProject() throws CoreException {
	try {
		this.createProject("P");
		this.startDeltas();
		this.deleteProject("P");
		assertDeltas(
			"Should get no delta", 
			""
		);
	} finally {
		this.stopDeltas();
	}
}
/*
 * Removing a non-java project that contains a jar referenced in another project should produce
 * a delta on this other project.
 * (regression test for bug 19058 Closing non-java project doesn't remove root from java project)
 */
public void testRemoveNonJavaProjectUpdateDependent() throws CoreException {
	try {
		this.createProject("SP");
		this.createFile("/SP/x.jar", "");
		this.createJavaProject("JP", new String[] {""}, new String[] {"/SP/x.jar"}, "");
		this.startDeltas();
		this.deleteProject("SP");
		assertDeltas(
			"Unexpected delta", 
			"JP[*]: {CHILDREN}\n" +
			"	x.jar[-]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("SP");
		this.deleteProject("JP");
	}
}
/*
 * Removing a non-java project and another project reference a non exiting folder in the deleted project
 * should not produce a delta on this other project (and no null pointer exception)
 * (regression test for bug 19131 NPE when removing a project containing missing classfile folder)
 */
public void testRemoveNonJavaProjectUpdateDependent2() throws CoreException {
	try {
		this.createProject("SP");
		this.createJavaProject("JP", new String[] {""}, new String[] {"/SP/missing"}, "");
		this.startDeltas();
		this.deleteProject("SP");
		assertDeltas(
			"Unexpected delta", 
			""
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("SP");
		this.deleteProject("JP");
	}
}
/*
 * Rename a java project.
 * (regression test for bug 7030 IllegalArgumentException renaming project)
 */
public void testRenameJavaProject() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.startDeltas();
		this.renameProject("P", "P1");
		assertDeltas(
			"Unexpected delta", 
			"P[-]: {MOVED_TO(P1)}\n" +
			"P1[+]: {MOVED_FROM(P)}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
		this.deleteProject("P1");
	}
}
public void testRenameMethodAndSave() throws CoreException {
	ICompilationUnit workingCopy = null;
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFolder("P/x/y");
		this.createFile("P/x/y/A.java",
			"package x.y;\n" +
			"public class A {\n" +
			"  public void foo1() {\n" +
			"  }\n" +
			"}");
		ICompilationUnit cu = this.getCompilationUnit("P/x/y/A.java"); 
		workingCopy = (ICompilationUnit)cu.getWorkingCopy();
		workingCopy.getBuffer().setContents(
			"package x.y;\n" +
			"public class A {\n" +
			"  public void foo2() {\n" +
			"  }\n" +
			"}");
		
		this.startDeltas();
		workingCopy.commit(true, null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		x.y[*]: {CHILDREN}\n" +
			"			A.java[*]: {CHILDREN | FINE GRAINED}\n" +
			"				A[*]: {CHILDREN | FINE GRAINED}\n" +
			"					foo2[+]: {}\n" +
			"					foo1[-]: {}"
		);
	} finally {
		this.stopDeltas();
		if (workingCopy != null) {
			workingCopy.destroy();
		}
		this.deleteProject("P");
	}
}
/*
 * Rename an outer pkg fragment.
 * (regression test for bug 24685 Inner package fragments gets deleted - model out of synch)
 */
public void testRenameOuterPkgFragment() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFolder("P/x/y");
		this.createFile(
			"P/x/y/X.java",
			"package x.y;\n" +
			"public class X {\n" +
			"}");
		this.startDeltas();
		IPackageFragment pkg = getPackageFragment("P", "", "x");
		pkg.rename("z", false, null);
		assertDeltas(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n" + 
			"	[project root][*]: {CHILDREN}\n" + 
			"		z[+]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Rename a non-java project.
 */
public void testRenameProject() throws CoreException {
	try {
		this.createProject("P");
		this.startDeltas();
		this.renameProject("P", "P1");
		assertDeltas(
			"Unexpected delta", 
			""
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
		this.deleteProject("P1");
	}
}
/**
 * Ensures that saving a working copy doesn't change the underlying resource.
 * (only commit should do so)
 */
public void testSaveWorkingCopy() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFile("P/X.java",
			"public class X {\n" +
			"}");
		ICompilationUnit unit = this.getCompilationUnit("P", "", "", "X.java");
		copy = (ICompilationUnit)unit.getWorkingCopy(null, null, null);
		copy.getType("X").createMethod("void foo() {}", null, true, null);
		this.startDeltas();
		copy.save(null, true);
		assertDeltas(
			"Unexpected delta after saving working copy", 
			""
		);
		copy.commit(true, null);
		assertDeltas(
			"Unexpected delta after committing working copy", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		[default][*]: {CHILDREN}\n" +
			"			X.java[*]: {CHILDREN | FINE GRAINED}\n" +
			"				X[*]: {CHILDREN | FINE GRAINED}\n" +
			"					foo[+]: {}"
		);
	} finally {
		this.stopDeltas();
		if (copy != null) copy.destroy();
		this.deleteProject("P");
	}
}
/**
 * Ensures that setting a classpath variable when there is exactly one project
 * triggers a java element delta.
 */
public void testSetClasspathVariable1() throws CoreException {
	try {
		this.createProject("LibProj");
		this.createFile("LibProj/mylib.jar", "");
		this.createFile("LibProj/otherlib.jar", "");
		JavaCore.setClasspathVariables(new String[] {"LIB"}, new IPath[] {new Path("/LibProj/mylib.jar")}, null);
		this.createJavaProject("P", new String[] {""}, new String[] {"LIB"}, "");
		this.startDeltas();
		JavaCore.setClasspathVariables(new String[] {"LIB"}, new IPath[] {new Path("/LibProj/otherlib.jar")}, null);
		assertDeltas(
			"Unexpected delta after setting classpath variable", 
			"P[*]: {CHILDREN}\n" +
			"	mylib.jar[*]: {REMOVED FROM CLASSPATH}\n" +
			"	otherlib.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
		this.deleteProject("LibProj");
	}
}
/**
 * Ensures that setting a classpath variable when there are more than one project
 * triggers a java element delta.
 */
public void testSetClasspathVariable2() throws CoreException {
	try {
		this.createProject("LibProj");
		this.createFile("LibProj/mylib.jar", "");
		this.createFile("LibProj/otherlib.jar", "");
		JavaCore.setClasspathVariables(new String[] {"LIB"}, new IPath[] {new Path("/LibProj/mylib.jar")}, null);
		this.createJavaProject("P1", new String[] {""}, new String[] {"LIB"}, "");
		this.createJavaProject("P2", new String[] {""}, new String[] {"LIB"}, "");
		this.startDeltas();
		JavaCore.setClasspathVariables(new String[] {"LIB"}, new IPath[] {new Path("/LibProj/otherlib.jar")}, null);
		assertEquals(
			"Unexpected delta after setting classpath variable", 
			"P1[*]: {CHILDREN}\n" +
			"	mylib.jar[*]: {REMOVED FROM CLASSPATH}\n" +
			"	otherlib.jar[*]: {ADDED TO CLASSPATH}\n" + 
			"P2[*]: {CHILDREN}\n" +
			"	mylib.jar[*]: {REMOVED FROM CLASSPATH}\n" +
			"	otherlib.jar[*]: {ADDED TO CLASSPATH}", 
			this.getSortedByProjectDeltas());
	} finally {
		this.stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("LibProj");
	}
}



/**
 * Ensure that a classpath change is detected even on a project which got closed
 */
public void testSetClasspathOnFreshProject() throws CoreException {
	try {
		this.createProject("LibProj");
		this.createFile("LibProj/mylib.jar", "");
		JavaProject p1 = (JavaProject)this.createJavaProject("P1", new String[] {""}, "bin");
		this.createFolder("P1/src2");

		p1.getProject().close(null);
		p1.getProject().open(null);

		this.startDeltas();

		IClasspathEntry[] classpath = 
			new IClasspathEntry[] {
				JavaCore.newSourceEntry(new Path("/P1/src2")),
				JavaCore.newLibraryEntry(new Path("/LibProj/mylib.jar"), null, null)
			};
		p1.setRawClasspath(classpath, null);
		assertDeltas(
			"Should notice src2 and myLib additions to the classpath", 
			"P1[*]: {CHILDREN}\n" + 
			"	[project root][*]: {REMOVED FROM CLASSPATH}\n" + 
			"	src2[*]: {ADDED TO CLASSPATH}\n" + 
			"	mylib.jar[*]: {ADDED TO CLASSPATH}\n" + 
			"	ResourceDelta(/P1/.classpath)[*]");
	} finally {
		this.stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("LibProj");
	}
}

/**
 * Ensures that committing a working copy fires a fine grained delta.
 */
public void testWorkingCopyCommit() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFolder("P/x/y");
		this.createFile("P/x/y/A.java", 
			"package x.y;\n" +
			"public class A {\n" +
			"}");
		ICompilationUnit cu = this.getCompilationUnit("P/x/y/A.java");
		ICompilationUnit copy = (ICompilationUnit) cu.getWorkingCopy();
		copy.getBuffer().setContents(
			"package x.y;\n" +
			"public class A {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}");
		copy.save(null, false);
		this.startDeltas();
		copy.commit(true, null);
		assertDeltas(
			"Unexpected delta after commit", 
			"P[*]: {CHILDREN}\n" +
			"	[project root][*]: {CHILDREN}\n" +
			"		x.y[*]: {CHILDREN}\n" +
			"			A.java[*]: {CHILDREN | FINE GRAINED}\n" +
			"				A[*]: {CHILDREN | FINE GRAINED}\n" +
			"					foo[+]: {}"
		);
	} finally {
		this.stopDeltas();
		this.deleteProject("P");
	}
}
}

