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

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

import java.io.ByteArrayInputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ClasspathTests extends ModifyingResourceTests {

	public class TestContainer implements IClasspathContainer {
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
	};

public ClasspathTests(String name) {
	super(name);
}
protected void assertCycleMarkers(IJavaProject project, IJavaProject[] p, int[] expectedCycleParticipants) throws CoreException {
	StringBuffer expected = new StringBuffer("{");
	int expectedCount = 0;
	StringBuffer computed = new StringBuffer("{");			
	int computedCount = 0;
	for (int j = 0; j < p.length; j++){
		int markerCount = this.numberOfCycleMarkers(p[j]);
		if (markerCount > 0){
			if (computedCount++ > 0) computed.append(", ");
			computed.append(p[j].getElementName());
			//computed.append(" (" + markerCount + ")");
		}
		markerCount = expectedCycleParticipants[j];
		if (markerCount > 0){
			if (expectedCount++ > 0) expected.append(", ");
			expected.append(p[j].getElementName());
			//expected.append(" (" + markerCount + ")");
		}
	}
	expected.append("}");
	computed.append("}");
	assertEquals("Invalid cycle detection after setting classpath for: "+project.getElementName(), expected.toString(), computed.toString());
}
protected void assertMarkers(String message, String expectedMarkers, IJavaProject project) throws CoreException {
	IMarker[] markers = project.getProject().findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = markers.length; i < length; i++) {
		IMarker marker = markers[i];
		buffer.append(marker.getAttribute(IMarker.MESSAGE));
		if (i != length-1) {
			buffer.append("\n");
		}
	}
	String actual = buffer.toString();
	if (!expectedMarkers.equals(actual)) {
	 	System.out.println(Util.displayString(actual, 2));
	}
	assertEquals(message, expectedMarkers, actual);
}
protected int numberOfCycleMarkers(IJavaProject javaProject) throws CoreException {
	IMarker[] markers = javaProject.getProject().findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
	int result = 0;
	for (int i = 0, length = markers.length; i < length; i++) {
		IMarker marker = markers[i];
		String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
		if (cycleAttr != null && cycleAttr.equals("true")){ //$NON-NLS-1$
			result++;
		}
	}
	return result;
}

public static Test suite() {

	if (false){
		TestSuite suite = new Suite(ClasspathTests.class.getName());
		suite.addTest(new ClasspathTests("testClasspathValidation7"));
		return suite;
	}
	return new Suite(ClasspathTests.class);	
}
/**
 * Add an entry to the classpath for a non-existent root. Then create
 * the root and ensure that it comes alive.
 */
public void testClasspathAddRoot() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
	IClasspathEntry[] originalCP= project.getRawClasspath();

	try {
		IClasspathEntry newEntry= JavaCore.newSourceEntry(project.getProject().getFullPath().append("extra"));

		IClasspathEntry[] newCP= new IClasspathEntry[originalCP.length + 1];
		System.arraycopy(originalCP, 0 , newCP, 0, originalCP.length);
		newCP[originalCP.length]= newEntry;

		project.setRawClasspath(newCP, null);


		// now create the actual resource for the root and populate it
		project.getProject().getFolder("extra").create(false, true, null);

		IPackageFragmentRoot newRoot= getPackageFragmentRoot("P", "extra");
		assertTrue("New root should now be visible", newRoot != null);
	} finally {
		// cleanup  
		this.deleteProject("P");
	}
}
/**
 * Ensures that the reordering external resources in the classpath
 * generates the correct deltas.
 */
public void testClasspathChangeExternalResources() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, "bin");

		IClasspathEntry[] newEntries = new IClasspathEntry[2];
		newEntries[0] = JavaCore.newLibraryEntry(getExternalJCLPath(), null, null, false);
		newEntries[1] = JavaCore.newLibraryEntry(getExternalJCLSourcePath(), null, null, false);
		setClasspath(proj, newEntries);
		startDeltas();
		IClasspathEntry[] swappedEntries = new IClasspathEntry[2];
		swappedEntries[0] = newEntries[1];
		swappedEntries[1] = newEntries[0];
		setClasspath(proj, swappedEntries);
		assertTrue("should be one delta - two jars reordered", this.deltaListener.deltas.length == 1);
		IJavaElementDelta d = getDeltaFor(proj);
		assertTrue("should be a delta for the project", d != null);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Test classpath corruption (23977)
 */
public void testClasspathCorruption() throws CoreException {
	try {
		JavaProject p1 = (JavaProject)this.createJavaProject("P1", new String[]{""}, new String[]{}, new String[]{}, "");
		this.createJavaProject("P2", new String[]{""}, new String[]{}, new String[]{}, "");
		this.createFile("P2/foo.txt", "not a project");
		String newCPContent = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
			+"<classpath>	\n"
			+"	<classpathentry kind=\"src\" path=\"\"/>	\n"
			+"	<classpathentry kind=\"src\" path=\"/P2/foo.txt\"/>	\n" // corruption here: target isn't a project
			+"	<classpathentry kind=\"output\" path=\"\"/>	\n"
			+"</classpath>	\n";

		IFile fileRsc = p1.getProject().getFile(JavaProject.CLASSPATH_FILENAME);
		fileRsc.setContents(new ByteArrayInputStream(newCPContent.getBytes()), true, false, null);
/*
		File file = p1.getProject().getFile(JavaProject.CLASSPATH_FILENAME).getLocation().toFile();
		if (file.exists()){
			char[] classpath = Util.getFileCharContent(file, "UTF-8");
			System.out.println(new String(classpath));
		}
*/
		p1.close();
		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(p1.getProject());
		perProjectInfo.classpath = null;
		perProjectInfo.lastResolvedClasspath = null;

		// shouldn't fail
		p1.getExpandedClasspath(true, true);

		// if could reach that far, then all is fine
		
	} catch(ClassCastException e){
		assertTrue("internal ClassCastException on corrupted classpath file", false);
	} finally {
		// cleanup  
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}

/**
 * Ensures that the setting the classpath with a library entry
 * changes the kind of the root from K_SOURCE to K_BINARY.
 */
public void testClasspathCreateLibraryEntry() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile("P/src/X.java", "public class X {}");
		this.createFile("P/src/X.class", "");
	
		IFolder rootFolder = proj.getProject().getFolder(new Path("src"));
		IPackageFragmentRoot root = proj.getPackageFragmentRoot(rootFolder);
		
		assertEquals(
			"Unexpected root kind 1", 
			IPackageFragmentRoot.K_SOURCE,
			root.getKind());
		IPackageFragment pkg = root.getPackageFragment("");
		assertEquals(
			"Unexpected numbers of compilation units",
			1,
			pkg.getCompilationUnits().length);
		assertEquals(
			"Unexpected numbers of .class files",
			0,
			pkg.getClassFiles().length);
			
		this.setClasspath(
			proj, 
			new IClasspathEntry[] {
				JavaCore.newLibraryEntry(rootFolder.getFullPath(), null, null, false)
			});
		assertEquals(
			"Unexpected root kind 2", 
			IPackageFragmentRoot.K_BINARY,
			root.getKind());
		assertEquals(
			"Unexpected numbers of compilation units",
			0,
			pkg.getCompilationUnits().length);
		assertEquals(
			"Unexpected numbers of .class files",
			1,
			pkg.getClassFiles().length);

		//ensure that the new kind has been persisted in the classpath file
		proj.close();
		assertEquals(
			"Unexpected root kind 3", 
			IPackageFragmentRoot.K_BINARY,
			root.getKind());

	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that the setting the classpath with a new library entry for a 
 * local jar works and generates the correct deltas.
 */
public void testClasspathCreateLocalJarLibraryEntry() throws CoreException {
	IJavaProject proj = this.createJavaProject("P", new String[] {""}, "");
	IPackageFragmentRoot root = getPackageFragmentRoot("P", "");
	IClasspathEntry newEntry= JavaCore.newLibraryEntry(getExternalJCLPath(), null, null, false);
	IClasspathEntry[] newEntries= new IClasspathEntry[]{newEntry};
	IPackageFragmentRoot newRoot= proj.getPackageFragmentRoot(getExternalJCLPathString());

	startDeltas();
	
	setClasspath(proj,newEntries);

	try {
		assertTrue(
			"should be one delta with 2 grand-children - removed & added", 
			this.deltaListener.deltas.length == 1 && 
			this.deltaListener.deltas[0].getAffectedChildren().length == 1 &&
			this.deltaListener.deltas[0].getAffectedChildren()[0].getAffectedChildren().length == 2);
		IJavaElementDelta d= null;
		assertTrue("root should be removed from classpath",(d= getDeltaFor(root, true)) != null &&
				(d.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) > 0);

		
		assertTrue("root should be added to classpath", (d= getDeltaFor(newRoot, true)) != null &&
				(d.getFlags() & IJavaElementDelta.F_ADDED_TO_CLASSPATH) > 0);
	} finally {
		stopDeltas();
	
		this.deleteProject("P");
	}
}

/**
 * Tests the cross project classpath setting
 */
public void testClasspathCrossProject() throws CoreException {
	IJavaProject project = this.createJavaProject("P1", new String[] {""}, "");
	this.createJavaProject("P2", new String[] {}, "");
	try {
		startDeltas();
		IPackageFragmentRoot oldRoot= getPackageFragmentRoot("P1", "");
 		IClasspathEntry projectEntry= JavaCore.newProjectEntry(new Path("/P2"), false);
		IClasspathEntry[] newClasspath= new IClasspathEntry[]{projectEntry};
		project.setRawClasspath(newClasspath, null);
		project.getAllPackageFragmentRoots();
		IJavaElementDelta removedDelta= getDeltaFor(oldRoot, true);
		assertTrue("Deltas not correct for crossproject classpath setting", 
			this.deltaListener.deltas.length == 1 &&
			this.deltaListener.deltas[0].getAffectedChildren().length == 1 &&
			removedDelta.getElement().equals(oldRoot) &&
			removedDelta.getKind() == IJavaElementDelta.CHANGED &&
			(removedDelta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) > 0
		);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/**
 * Delete a root and ensure the classpath is not updated (i.e. entry isn't removed).
 */
public void testClasspathDeleteNestedRoot() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {"nested/src"}, new String[] {getExternalJCLPathString()}, "bin");
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "nested/src");
	IClasspathEntry[] originalCP= project.getRawClasspath();

	// delete the root
	root.getUnderlyingResource().delete(false, null);

	IClasspathEntry[] newCP= project.getRawClasspath();

	try {
		// should still be an entry for the "src" folder
		assertTrue("classpath should not have been updated", 
			newCP.length == 2 &&
			newCP[0].equals(originalCP[0]) &&
			newCP[1].equals(originalCP[1]));
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Test classpath diamond (23979)
 */
public void testClasspathDiamond() throws CoreException {
	try {
		this.createJavaProject("P1", new String[]{""}, "");
		this.createJavaProject("P2", new String[]{""}, new String[]{}, new String[]{"/P1"}, "");
		this.createJavaProject("P3", new String[]{""}, new String[]{}, new String[]{"/P1", "/P2"}, "");
		IJavaProject p4 = this.createJavaProject("P4", new String[]{""}, new String[]{}, new String[]{"/P2", "/P3"}, "");
	
		assertTrue("Should not detect cycle", !p4.hasClasspathCycle(null));
		
	} finally {
		// cleanup  
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		this.deleteProject("P4");
	}
}
 
/**
 * Delete a nested root's parent folder and ensure the classpath is
 * not updated (i.e. entry isn't removed).
 */
public void testClasspathDeleteNestedRootParent() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {"nested/src"}, new String[] {getExternalJCLPathString()}, "bin");
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "nested/src");
	IClasspathEntry[] originalCP= project.getRawClasspath();

	// delete the root's parent folder
	IFolder folder= (IFolder)root.getUnderlyingResource().getParent();
	folder.delete(false, null);

	IClasspathEntry[] newCP= project.getRawClasspath();

	try {
		
		// should still be an entry for the "src" folder
		assertTrue("classpath should not have been updated", 
			newCP.length == 2 &&
			newCP[0].equals(originalCP[0]) &&
			newCP[1].equals(originalCP[1]));
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Test that a classpath entry for an external jar is externalized
 * properly.
 */
public void testClasspathExternalize() throws CoreException {
	try {
		IJavaProject project= this.createJavaProject("P", new String[] {}, new String[] {getExternalJCLPathString()}, "");
		IClasspathEntry[] classpath= project.getRawClasspath();
		IClasspathEntry jar= null;
		for (int i= 0; i < classpath.length; i++) {
			if (classpath[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				jar= classpath[i];
				break;
			}
		}
		project.close();
		project.open(null);
	
		classpath= project.getRawClasspath();
		for (int i= 0; i < classpath.length; i++) {
			if (classpath[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				assertTrue("Paths must be the same", classpath[i].getPath().equals(jar.getPath()));
				break;
			}
		}   
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Move a root and ensure the classpath is not updated (i.e. entry not renamed).
 */
public void testClasspathMoveNestedRoot() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {"nested/src"}, new String[] {getExternalJCLPathString()}, "bin");
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "nested/src");
	IClasspathEntry[] originalCP= project.getRawClasspath();

	// delete the root
	IFolder folder= (IFolder)root.getUnderlyingResource();
	IPath originalPath= folder.getFullPath();
	IPath newPath= originalPath.removeLastSegments(1);
	newPath= newPath.append(new Path("newsrc"));

	startDeltas(); 
	
	folder.move(newPath, true, null);

	IClasspathEntry[] newCP= project.getRawClasspath();

	IPackageFragmentRoot newRoot= project.getPackageFragmentRoot(project.getProject().getFolder("nested").getFolder("newsrc")); 

	try {
		// entry for the "src" folder wasn't replaced
		assertTrue("classpath not automatically updated", newCP.length == 2 &&
			newCP[1].equals(originalCP[1]) &&
			newCP[0].equals(originalCP[0]));

		IJavaElementDelta rootDelta = getDeltaFor(root, true);
		IJavaElementDelta projectDelta = getDeltaFor(newRoot.getParent(), true);
		assertTrue("should get delta for moved root", rootDelta != null &&
				rootDelta.getKind() == IJavaElementDelta.REMOVED &&
				rootDelta.getFlags() == 0);
		assertTrue("should get delta indicating content changed for project", this.deltaContentChanged(projectDelta));
	
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}

/**
 * Move a parent of a nested root and ensure the classpath is not updated (i.e. entry not renamed).
 */
public void testClasspathMoveNestedRootParent() throws CoreException {
	try {
		IJavaProject project =this.createJavaProject("P", new String[] {"nested/src"}, new String[] {getExternalJCLPathString()}, "bin");
		IPackageFragmentRoot root= getPackageFragmentRoot("P", "nested/src");
		IClasspathEntry[] originalCP= project.getRawClasspath();
	
		// delete the root
		IFolder folder= (IFolder)root.getUnderlyingResource().getParent();
		IPath originalPath= folder.getFullPath();
		IPath newPath= originalPath.removeLastSegments(1);
		newPath= newPath.append(new Path("newsrc"));
		folder.move(newPath, true, null);
	
		IClasspathEntry[] newCP= project.getRawClasspath();

		// entry for the "src" folder wasn't replaced
		// entry for the "src" folder should not be replaced
		assertTrue("classpath should not automatically be updated", newCP.length == 2 &&
			newCP[1].equals(originalCP[1]) &&
			newCP[0].equals(originalCP[0]));

	} finally {
		this.deleteProject("P");
	}
}
/**
 * Tests that nothing occurs when setting to the same classpath
 */
public void testClasspathNoChanges() throws CoreException {
	try {
		IJavaProject p = this.createJavaProject("P", new String[] {""}, "");
		IClasspathEntry[] oldClasspath= p.getRawClasspath();
		startDeltas();
		p.setRawClasspath(oldClasspath, null);


//		try {
//			java.io.File file = p.getProject().getFile(JavaProject.CLASSPATH_FILENAME).getLocation().toFile();
//			if (file.exists()){
//				char[] classpath = org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(file, "UTF-8");
//				System.out.println(new String(classpath));
//			}
//		} catch(java.io.IOException e){
//		}

		StringBuffer buffer = new StringBuffer(10);
		for (int i = 0; i < this.deltaListener.deltas.length; i++){
			buffer.append(this.deltaListener.deltas[i].toString()).append('\n');
		}
		assertEquals("No deltas should be generated for the same classpath", "", buffer.toString());
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/**
 * Ensures that the setting the classpath with a reordered classpath generates
 * the correct deltas.
 */
public void testClasspathReordering() throws CoreException {
	IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, new String[] {getExternalJCLPathString()}, "bin");
	IClasspathEntry[] originalCP = proj.getRawClasspath();
	IPackageFragmentRoot root = getPackageFragmentRoot("P", "src");
	try {
		IClasspathEntry[] newEntries = new IClasspathEntry[originalCP.length];
		int index = originalCP.length - 1;
		for (int i = 0; i < originalCP.length; i++) {
			newEntries[index] = originalCP[i];
			index--;
		}
		startDeltas();
		setClasspath(proj, newEntries);
		assertTrue("should be one delta - two roots reordered", this.deltaListener.deltas.length == 1);
		IJavaElementDelta d = null;
		assertTrue("root should be reordered in the classpath", (d = getDeltaFor(root, true)) != null
			&& (d.getFlags() & IJavaElementDelta.F_CLASSPATH_REORDER) > 0);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}

/**
 * Should detect duplicate entries on the classpath
 */ 
public void testClasspathValidation01() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = newCP[0];
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should have detected duplicate entries on the classpath", 
			"Classpath contains duplicate entry: P/src",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Should detect nested source folders on the classpath
 */ 
public void testClasspathValidation02() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"));
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should have detected nested source folders on the classpath", 
			"Cannot nest 'P/src' inside 'P'. To enable the nesting exclude 'src/' from 'P'.",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Should detect library folder nested inside source folder on the classpath
 */ 
public void testClasspathValidation03() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newLibraryEntry(new Path("/P/src/lib"), null, null);
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should have detected library folder nested inside source folder on the classpath", 
			"Cannot nest 'P/src/lib' inside 'P/src'. To enable the nesting exclude 'lib/' from 'P/src'.",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}

public void testClasspathValidation04() throws CoreException {
	
	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {"src0"}, "bin0"),
			this.createJavaProject("P1", new String[] {"src1"}, "bin1"),
		};

		JavaCore.setClasspathVariable("var", new Path("/P1"), null);
		
		IClasspathEntry[] newClasspath = new IClasspathEntry[]{
			JavaCore.newSourceEntry(new Path("/P0/src0")),
			JavaCore.newVariableEntry(new Path("var/src1"), null, null),
		};
				
		// validate classpath
		IJavaModelStatus status = JavaConventions.validateClasspath(p[0], newClasspath, p[0].getOutputLocation());
		assertEquals(
			"should not detect external source folder through a variable on the classpath", 
			"OK",
			status.getMessage());

	} finally {
		if (p != null){
			for (int i = 0; i < p.length; i++){
				this.deleteProject(p[i].getElementName());
			}
		}
	}
}

public void testClasspathValidation05() throws CoreException {
	
	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {"src0", "src1"}, "bin0"),
			this.createJavaProject("P1", new String[] {"src1"}, "bin1"),
		};

		JavaCore.setClasspathContainer(
		new Path("container/default"), 
			new IJavaProject[]{ p[0] },
			new IClasspathContainer[] {
				new TestContainer(new Path("container/default"),
					new IClasspathEntry[]{
						JavaCore.newSourceEntry(new Path("/P0/src0")),
						JavaCore.newVariableEntry(new Path("var/src1"), null, null) }) 
			}, 
			null);
		
		IClasspathEntry[] newClasspath = new IClasspathEntry[]{
			JavaCore.newSourceEntry(new Path("/P0/src1")),
			JavaCore.newContainerEntry(new Path("container/default")),
		};
				
		// validate classpath
		IJavaModelStatus status = JavaConventions.validateClasspath(p[0], newClasspath, p[0].getOutputLocation());
		assertEquals(
			"should not have detected external source folder through a container on the classpath", 
			"OK",
			status.getMessage());

		// validate classpath entry
		status = JavaConventions.validateClasspathEntry(p[0], newClasspath[1], true);
		assertEquals(
			"should have detected external source folder through a container on the classpath", 
			"Invalid classpath container: container/default",
			status.getMessage());

	} finally {
		if (p != null){
			for (int i = 0; i < p.length; i++){
				this.deleteProject(p[i].getElementName());
			}
		}
	}
}

public void testClasspathValidation06() throws CoreException {
	
	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {"src"}, "src"),
		};

		// validate classpath entry
		IClasspathEntry[] newClasspath = new IClasspathEntry[]{
			JavaCore.newSourceEntry(new Path("/P0")),
			JavaCore.newSourceEntry(new Path("/P0/src")),
		};
				
		IJavaModelStatus status = JavaConventions.validateClasspath(p[0], newClasspath, p[0].getOutputLocation());
		assertEquals(
			"should have detected nested source folder", 
			"Cannot nest 'P0/src' inside 'P0'. To enable the nesting exclude 'src/' from 'P0'.",
			status.getMessage());
	} finally {
		if (p != null){
			for (int i = 0; i < p.length; i++){
				this.deleteProject(p[i].getElementName());
			}
		}
	}
}
/**
 * Should allow nested source folders on the classpath as long as the outer
 * folder excludes the inner one.
 */ 
public void testClasspathValidation07() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("src/")});
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should have allowed nested source folders with exclusion on the classpath", 
			"OK",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should allow a nested binary folder in a source folder on the classpath as
 * long as the outer folder excludes the inner one.
 */ 
public void testClasspathValidation08() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, new String[] {"lib"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("lib/")});
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should have allowed nested lib folders with exclusion on the classpath", 
			"OK",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow a nested source folder in the project's output folder.
 */ 
public void testClasspathValidation09() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/bin/src"));
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should not allow nested source folder in putput folder", 
			"Cannot nest 'P/bin/src' inside output folder 'P/bin'.",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow a nested output folder in a source folder on the classpath.
 */ 
public void testClasspathValidation10() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, originalCP, new Path("/P/src/bin"));
		
		assertEquals(
			"should not allow nested output folder in source folder", 
			"Cannot nest output folder 'P/src/bin' inside 'P/src'.",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should allow a nested library folder in the project's output folder if the project's output is not used.
*/ 
public void testClasspathValidation11() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newLibraryEntry(new Path("/P/lib"), null, null);
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should allow nested library folder in output folder", 
			"OK",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow a nested source folder in an output folder.
 */ 
public void testClasspathValidation12() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin1");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = 
			JavaCore.newSourceEntry(
				new Path("/P/bin2/src"), 
				new IPath[] {}, 
				new Path("/P/bin2"));
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should not allow nested source folder in output folder", 
			"Cannot nest 'P/bin2/src' inside output folder 'P/bin2'.",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow a nested output folder in a source folder on the classpath.
 */ 
public void testClasspathValidation13() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin1");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = 
			JavaCore.newSourceEntry(
				new Path("/P/src"), 
				new IPath[] {}, 
				new Path("/P/src/bin2"));
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should not allow nested output folder in source folder", 
			"Cannot nest output folder 'P/src/bin2' inside 'P/src'.",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should allow a nested output folder in a source folder that coincidate with the project.
 */ 
public void testClasspathValidation14() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = 
			JavaCore.newSourceEntry(
				new Path("/P"), 
				new IPath[] {}, 
				new Path("/P/bin"));
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should allow nested output folder in source folder which is project", 
			"OK",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow nested source folders on the classpath if exclusion filter has no trailing slash.
 */ 
public void testClasspathValidation15() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("**/src")});
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"End exclusion filter 'src' with / to fully exclude 'P/src'.",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should allow custom output folder to be nested in default output folder if default output is not used.
 * (regression test for bug 28596 Default output folder cause of validation error even if not used)
 */ 
public void testClasspathValidation16() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[0], new Path("/P/bin"));
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"OK",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow source folder to be nested in default output folder if default output is used.
 */ 
public void testClasspathValidation17() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src1"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/bin/src2"));
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"Cannot nest 'P/bin/src2' inside output folder 'P/bin'.",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow custom output folder to be external to project.
 * (regression test for bug 29079 Buildpath validation: No check that output folder is inside project)
 */ 
public void testClasspathValidation18() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], new Path("/S/bin"));
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"Path '/S/bin' must denote location inside project P",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should detect source folder nested inside library folder on the classpath
 */ 
public void testClasspathValidation19() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, new String[] {"lib"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();
	
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/lib/src"));
		
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		
		assertEquals(
			"should have detected library folder nested inside source folder on the classpath", 
			"Cannot nest 'P/lib/src' inside library 'P/lib'.",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Setting the classpath with two entries specifying the same path
 * should fail.
 */
public void testClasspathWithDuplicateEntries() throws CoreException {
	try {
		IJavaProject project =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] cp= project.getRawClasspath();
		IClasspathEntry[] newCp= new IClasspathEntry[cp.length *2];
		System.arraycopy(cp, 0, newCp, 0, cp.length);
		System.arraycopy(cp, 0, newCp, cp.length, cp.length);
		try {
			project.setRawClasspath(newCp, null);
		} catch (JavaModelException jme) {
			return;
		}
		assertTrue("Setting the classpath with two entries specifying the same path should fail", false);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Adding an entry to the classpath for a library that does not exist
 * should not break the model. The classpath should contain the
 * entry, but the root should not appear in the children.
 */
public void testClasspathWithNonExistentLibraryEntry() throws CoreException {
	try {
		IJavaProject project=  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalPath= project.getRawClasspath();
		IPackageFragmentRoot[] originalRoots= project.getPackageFragmentRoots();
	
		IClasspathEntry[] newPath= new IClasspathEntry[originalPath.length + 1];
		System.arraycopy(originalPath, 0, newPath, 0, originalPath.length);
	
		IClasspathEntry newEntry= JavaCore.newLibraryEntry(new Path("c:/nothing/nozip.jar"), null, null, false);
		newPath[originalPath.length]= newEntry;
	
		project.setRawClasspath(newPath, null);

		IClasspathEntry[] getPath= project.getRawClasspath();
		assertTrue("should be the same length", getPath.length == newPath.length);
		for (int i= 0; i < getPath.length; i++) {
			assertTrue("entries should be the same", getPath[i].equals(newPath[i]));
		}

		IPackageFragmentRoot[] newRoots= project.getPackageFragmentRoots();
		assertTrue("Should be the same number of roots", originalRoots.length == newRoots.length);
		for (int i= 0; i < newRoots.length; i++) {
			assertTrue("roots should be the same", originalRoots[i].equals(newRoots[i]));
		}
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Adding an entry to the classpath for a project that does not exist
 * should not break the model. The classpath should contain the
 * entry, but the root should not appear in the children.
 */
public void testClasspathWithNonExistentProjectEntry() throws CoreException {
	try {
		IJavaProject project= this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalPath= project.getRawClasspath();
		IPackageFragmentRoot[] originalRoots= project.getPackageFragmentRoots();
	
		IClasspathEntry[] newPath= new IClasspathEntry[originalPath.length + 1];
		System.arraycopy(originalPath, 0, newPath, 0, originalPath.length);
	
		IClasspathEntry newEntry= JavaCore.newProjectEntry(new Path("/NoProject"), false);
		newPath[originalPath.length]= newEntry;
	
		project.setRawClasspath(newPath, null);
	
		IClasspathEntry[] getPath= project.getRawClasspath();
		assertTrue("should be the same length", getPath.length == newPath.length);
		for (int i= 0; i < getPath.length; i++) {
			assertTrue("entries should be the same", getPath[i].equals(newPath[i]));
		}
	
		IPackageFragmentRoot[] newRoots= project.getPackageFragmentRoots();
		assertTrue("Should be the same number of roots", originalRoots.length == newRoots.length);
		for (int i= 0; i < newRoots.length; i++) {
			assertTrue("roots should be the same", originalRoots[i].equals(newRoots[i]));
		}
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Adding an entry to the classpath for a folder that does not exist
 * should not break the model. The classpath should contain the
 * entry, but the root should not appear in the children.
 */
public void testClasspathWithNonExistentSourceEntry() throws CoreException {
	try {
		IJavaProject project= this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalPath= project.getRawClasspath();
		IPackageFragmentRoot[] originalRoots= project.getPackageFragmentRoots();

		IClasspathEntry[] newPath= new IClasspathEntry[originalPath.length + 1];
		System.arraycopy(originalPath, 0, newPath, 0, originalPath.length);

		IClasspathEntry newEntry= JavaCore.newSourceEntry(new Path("/P/moreSource"));
		newPath[originalPath.length]= newEntry;

		project.setRawClasspath(newPath, null);

		IClasspathEntry[] getPath= project.getRawClasspath();
		assertTrue("should be the same length", getPath.length == newPath.length);
		for (int i= 0; i < getPath.length; i++) {
			assertTrue("entries should be the same", getPath[i].equals(newPath[i]));
		}

		IPackageFragmentRoot[] newRoots= project.getPackageFragmentRoots();
		assertTrue("Should be the same number of roots", originalRoots.length == newRoots.length);
		for (int i= 0; i < newRoots.length; i++) {
			assertTrue("roots should be the same", originalRoots[i].equals(newRoots[i]));
		}
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Ensure that cycle are properly reported.
 */
public void testCycleReport() throws CoreException {

	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {""}, "");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {""}, "");
		IJavaProject p3 = this.createJavaProject("P3", new String[] {""}, new String[] {}, new String[] {"/P2"}, "");
	
		// Ensure no cycle reported
		IJavaProject[] projects = { p1, p2, p3 };
		int cycleMarkerCount = 0;
		for (int i = 0; i < projects.length; i++){
			cycleMarkerCount += this.numberOfCycleMarkers(projects[i]);
		}
		assertTrue("Should have no cycle markers", cycleMarkerCount == 0);
	
		// Add cycle
		IClasspathEntry[] originalP1CP= p1.getRawClasspath();
		IClasspathEntry[] originalP2CP= p2.getRawClasspath();

		// Add P1 as a prerequesite of P2
		int length = originalP2CP.length;
		IClasspathEntry[] newCP= new IClasspathEntry[length + 1];
		System.arraycopy(originalP2CP, 0 , newCP, 0, length);
		newCP[length]= JavaCore.newProjectEntry(p1.getProject().getFullPath(), false);
		p2.setRawClasspath(newCP, null);

		// Add P3 as a prerequesite of P1
		length = originalP1CP.length;
		newCP= new IClasspathEntry[length + 1];
		System.arraycopy(originalP1CP, 0 , newCP, 0, length);
		newCP[length]= JavaCore.newProjectEntry(p3.getProject().getFullPath(), false);
		p1.setRawClasspath(newCP, null);

		// Ensure a cycle is reported on one of the projects
		// Ensure no cycle reported
		cycleMarkerCount = 0;
		for (int i = 0; i < projects.length; i++){
			cycleMarkerCount += this.numberOfCycleMarkers(projects[i]);
		}
		assertTrue("Should have 3 projects involved in a classpath cycle", cycleMarkerCount == 3);
		
	} finally {
		// cleanup  
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
	}
}
/**
 * Ensures that the default classpath and output locations are correct.
 * The default classpath should be the root of the project.
 * The default output location should be the root of the project.
 */
public void testDefaultClasspathAndOutputLocation() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "bin");
		IClasspathEntry[] classpath = project.getRawClasspath();
		assertTrue("Incorrect default classpath; to many entries", classpath.length == 1);
		assertTrue("Incorrect default classpath: " + classpath[0], classpath[0].getPath().equals(project.getUnderlyingResource().getFullPath()));
		IPath output = project.getOutputLocation();
		assertTrue("Incorrect default output location: " + output.toOSString(), output.equals(project.getUnderlyingResource().getFullPath().append("bin")));
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Setting the classpath to empty should result in no entries,
 * and a delta with removed roots.
 */
public void testEmptyClasspath() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
	try {
		IPackageFragmentRoot[] oldRoots= project.getAllPackageFragmentRoots();

		startDeltas();
		setClasspath(project, new IClasspathEntry[] {});
		IClasspathEntry[] cp= project.getRawClasspath();
		assertTrue("classpath should have no entries", cp.length == 0);

		// ensure the deltas are correct
		assertTrue("there should be a delta", this.deltaListener.deltas != null && this.deltaListener.deltas.length == 1);
		for (int i= 0; i < oldRoots.length; i++) {
			IJavaElementDelta d= null;
			assertTrue("root should be removed", (d= getDeltaFor(oldRoots[i])) != null &&
				(d.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) > 0);
		} 
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/**
 * Exporting a container should make it visible to its dependent project.
 * (regression test for bug 21749 Exported libraries and source folders)
 */
public void testExportContainer() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {""}, "");

		// create container
		JavaCore.setClasspathContainer(
			new Path("container/default"), 
			new IJavaProject[]{ p1 },
			new IClasspathContainer[] {
				new TestContainer(
					new Path("container/default"),
					new IClasspathEntry[] {
						JavaCore.newLibraryEntry(getExternalJCLPath(), null, null)
					}) 
			}, 
			null);

		// set P1's classpath with this container
		IClasspathEntry container = JavaCore.newContainerEntry(new Path("container/default"), true);
		p1.setRawClasspath(new IClasspathEntry[] {container}, new Path("/P1"), null);
		
		// create dependent project P2
		IJavaProject  p2 = this.createJavaProject("P2", new String[] {}, new String[] {}, new String[] {"/P1"}, "");
		IClasspathEntry[] classpath = ((JavaProject)p2).getExpandedClasspath(true);
		
		// ensure container is exported to P2
		assertEquals("Unexpected number of classpath entries", 2, classpath.length);
		assertEquals("Unexpected first entry", "/P1", classpath[0].getPath().toString());
		assertEquals("Unexpected second entry", getExternalJCLPathString(), classpath[1].getPath().toOSString());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/**
 * Test IJavaProject.hasClasspathCycle(IClasspathEntry[]).
 */
public void testHasClasspathCycle() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {""}, "");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {""}, "");
		this.createJavaProject("P3", new String[] {""}, new String[] {}, new String[] {"/P1"}, "");
	
		IClasspathEntry[] originalP1CP= p1.getRawClasspath();
		IClasspathEntry[] originalP2CP= p2.getRawClasspath();
	
		// Ensure no cycle reported
		assertTrue("P1 should not have a cycle", !p1.hasClasspathCycle(originalP1CP));

		// Ensure that adding NervousTest as a prerequesite of P2 doesn't report a cycle
		int length = originalP2CP.length;
		IClasspathEntry[] newCP= new IClasspathEntry[length + 1];
		System.arraycopy(originalP2CP, 0 , newCP, 0, length);
		newCP[length]= JavaCore.newProjectEntry(p1.getProject().getFullPath(), false);
		assertTrue("P2 should not have a cycle", !p2.hasClasspathCycle(newCP));
		p2.setRawClasspath(newCP, null);

		// Ensure that adding P3 as a prerequesite of P1 reports a cycle
		length = originalP1CP.length;
		newCP= new IClasspathEntry[length + 1];
		System.arraycopy(originalP1CP, 0 , newCP, 0, length);
		newCP[length]= JavaCore.newProjectEntry(p2.getProject().getFullPath(), false);
		assertTrue("P3 should have a cycle", p2.hasClasspathCycle(newCP));

		// Ensure a cycle is not reported through markers
		IWorkspace workspace = getJavaModel().getWorkspace();
		IMarker[] markers = workspace.getRoot().findMarkers(IJavaModelMarker.TRANSIENT_PROBLEM, true, 1);
		boolean hasCycleMarker = false;
		for (int i = 0; i < markers.length; i++){
			if (markers[i].getAttribute(IJavaModelMarker.CYCLE_DETECTED) != null) {
				hasCycleMarker = true;
				break;
			}
		}
	assertTrue("Should have no cycle markers", !hasCycleMarker);
		
	} finally {
		// cleanup  
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
	}
}
/**
 * Ensures that a marker is created if editing the .classpath results in an invalid classpath.
 */
public void testInvalidClasspath1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		this.editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/\n" + // missing >
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		assertMarkers(
			"Unexpected markers",
			"XML format error in 'P/.classpath' file: Bad format.",
			project);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a marker is created if editing the .classpath results in an invalid classpath.
 */
public void testInvalidClasspath2() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		this.editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src1\" path=\"src\"/>\n" + // invalid kind: src1
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		assertMarkers(
			"Unexpected markers",
			"Illegal entry in 'P/.classpath' file: Unknown kind: src1",
			project);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Test that a marker is added when a project as a missing project in its classpath.
 */
public void testMissingPrereq1() throws CoreException {
	try {
		IJavaProject javaProject = this.createJavaProject("A", new String[] {}, "");
		IClasspathEntry[] classpath = 
			new IClasspathEntry[] {
				JavaCore.newProjectEntry(new Path("/B"))
			};
		javaProject.setRawClasspath(classpath, null);
		this.assertMarkers(
			"Unexpected markers",
			"Missing required Java project: B.",
			javaProject);
	} finally {
		this.deleteProject("A");
	}
}
/**
 * Test that a marker is added when a project as a missing project in its classpath.
 */
public void testMissingPrereq2() throws CoreException {
	try {
		IJavaProject javaProject = 
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/B"}, // projects
				"");
		this.assertMarkers(
			"Unexpected markers",
			"Missing required Java project: B.",
			javaProject);
	} finally {
		this.deleteProject("A");
	}
}
/**
 * Test that a marker indicating a missing project is removed when the project is added.
 */
public void testMissingPrereq3() throws CoreException {
	try {
		IJavaProject javaProject = 
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/B"}, // projects
				"");
		this.createJavaProject("B", new String[] {}, "");
		this.assertMarkers("Unexpected markers", "", javaProject);
	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}
/**
 * Test that a marker indicating a cycle is removed when a project in the cycle is deleted
 * and replaced with a missing prereq marker.
 * (regression test for bug 15168 circular errors not reported)
 */
public void testMissingPrereq4() throws CoreException {
	try {
		IJavaProject projectA =
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/B"}, // projects
				"");
		IJavaProject projectB =
			this.createJavaProject(
				"B", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/A"}, // projects
				"");
		this.assertMarkers(
			"Unexpected markers for project A",
			"A cycle was detected in the project's classpath.",
			projectA);
		this.assertMarkers(
			"Unexpected markers for project B",
			"A cycle was detected in the project's classpath.",
			projectB);
		
		// delete project B	
		this.deleteProject("B");
		this.assertMarkers(
			"Unexpected markers for project A after deleting of project B",
			"Missing required Java project: B.",
			projectA);
			
		// add project B back
		projectB =
			this.createJavaProject(
				"B", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/A"}, // projects
				"");
		this.assertMarkers(
			"Unexpected markers for project A after adding project B back",
			"A cycle was detected in the project's classpath.",
			projectA);
		this.assertMarkers(
			"Unexpected markers for project B after adding project B back",
			"A cycle was detected in the project's classpath.",
			projectB);

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}
/**
 * Setting the classpath to null should be the same as using the
 * default classpath.
 */
public void testNullClasspath() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		setClasspath(project, null);
		IClasspathEntry[] cp= project.getRawClasspath();
		assertTrue("classpath should have one root entry", cp.length == 1 && cp[0].getPath().equals(project.getUnderlyingResource().getFullPath()));
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensure that reading an empty custom putput from the .classpath returns a non-null output location.
 * (regression test for 28531 Classpath Entry: Output folder can not be set to project)
 */
public void testReadEmptyCustomOutput() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {}, "");
		this.editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" output=\"\" path=\"\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		IClasspathEntry[] classpath = project.getRawClasspath();
		assertEquals("Unexpected classpath length", 1, classpath.length);
		assertEquals("Unexpected custom output location", new Path("/P"), classpath[0].getOutputLocation());
	} finally {
		this.deleteProject("P");
	}
}


public void testCycleDetection() throws CoreException {
	
	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {""}, ""),
			this.createJavaProject("P1", new String[] {""}, ""),
			this.createJavaProject("P2", new String[] {""}, ""),
			this.createJavaProject("P3", new String[] {""}, ""),
			this.createJavaProject("P4", new String[] {""}, ""),
		};
		
		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{ 
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newProjectEntry(p[1].getPath()) }, 
			{ JavaCore.newProjectEntry(p[4].getPath())}, 
			{ JavaCore.newProjectEntry(p[3].getPath()), JavaCore.newProjectEntry(p[0].getPath()) } 
		}; 

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 1, 1, 0, 0 }, // after setting CP p[2]
			{ 0, 1, 1, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};
		
		for (int i = 0; i < p.length; i++){

			// append project references			
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			for (int j = 0; j < extraEntries[i].length; j++){
				newClasspath[oldClasspath.length+j] = extraEntries[i][j];
			}			
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// check cycle markers
			this.assertCycleMarkers(p[i], p, expectedCycleParticipants[i]);
		}
		//this.startDeltas();
		
	} finally {
		//this.stopDeltas();
		if (p != null){
			for (int i = 0; i < p.length; i++){
				this.deleteProject(p[i].getElementName());
			}
		}
	}
}


public void testCycleDetectionThroughVariables() throws CoreException {
	
	IJavaProject[] p = null;
	try {

		String[] var = new String[]{ "v0", "v1", "v2"};

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {""}, ""),
			this.createJavaProject("P1", new String[] {""}, ""),
			this.createJavaProject("P2", new String[] {""}, ""),
			this.createJavaProject("P3", new String[] {""}, ""),
			this.createJavaProject("P4", new String[] {""}, ""),
		};
		
		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{ 
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newVariableEntry(new Path(var[0]), null, null) },
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newVariableEntry(new Path(var[1]), null, null) }, 
			{ JavaCore.newVariableEntry(new Path(var[2]), null, null)}, 
			{ JavaCore.newProjectEntry(p[3].getPath()), JavaCore.newProjectEntry(p[0].getPath()) } 
		}; 

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[2]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};
		
		IPath[][] variableValues = new IPath[][]{
			{ null, null, null },
			{ null, null, null },
			{ null, null, null },
			{ null, null, null },
			{ p[3].getPath(), p[1].getPath(), p[4].getPath() },
		};
		
		for (int i = 0; i < p.length; i++){

			// append project references			
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			for (int j = 0; j < extraEntries[i].length; j++){
				newClasspath[oldClasspath.length+j] = extraEntries[i][j];
			}			
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// update variable values
			JavaCore.setClasspathVariables(var, variableValues[i], null);
			
			// check cycle markers
			this.assertCycleMarkers(p[i], p, expectedCycleParticipants[i]);
		}
		//this.startDeltas();
		
	} finally {
		//this.stopDeltas();
		if (p != null){
			for (int i = 0; i < p.length; i++){
				this.deleteProject(p[i].getElementName());
			}
		}
	}
}

public void testCycleDetectionThroughContainers() throws CoreException {
	
	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {""}, ""),
			this.createJavaProject("P1", new String[] {""}, ""),
			this.createJavaProject("P2", new String[] {""}, ""),
			this.createJavaProject("P3", new String[] {""}, ""),
			this.createJavaProject("P4", new String[] {""}, ""),
		};

		IClasspathContainer[] containers = new IClasspathContainer[]{ 
			new TestContainer(
				new Path("container0/default"), 
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[3].getPath()) }),
			new TestContainer(
				new Path("container1/default"), 
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[1].getPath()) }),
			new TestContainer(
				new Path("container2/default"), 
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[4].getPath()) }),
		};

		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{ 
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newContainerEntry(containers[0].getPath()) },
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newContainerEntry(containers[1].getPath()) }, 
			{ JavaCore.newContainerEntry(containers[2].getPath())}, 
			{ JavaCore.newProjectEntry(p[3].getPath()), JavaCore.newProjectEntry(p[0].getPath()) } 
		}; 

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[2]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};
		
		for (int i = 0; i < p.length; i++){

			// append project references			
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			for (int j = 0; j < extraEntries[i].length; j++){
				newClasspath[oldClasspath.length+j] = extraEntries[i][j];
			}			
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// update container paths
			if (i == p.length - 1){
				JavaCore.setClasspathContainer(
					containers[0].getPath(),
					new IJavaProject[]{ p[0] },
					new IClasspathContainer[] { containers[0] },
					null);

				JavaCore.setClasspathContainer(
					containers[1].getPath(),
					new IJavaProject[]{ p[2] },
					new IClasspathContainer[] { containers[1] },
					null);

				JavaCore.setClasspathContainer(
					containers[2].getPath(),
					new IJavaProject[]{ p[3] },
					new IClasspathContainer[] { containers[2] },
					null);
			}
			
			// check cycle markers
			this.assertCycleMarkers(p[i], p, expectedCycleParticipants[i]);
		}
		//this.startDeltas();
		
	} finally {
		//this.stopDeltas();
		if (p != null){
			for (int i = 0; i < p.length; i++){
				this.deleteProject(p[i].getElementName());
			}
		}
	}
}
public void testCycleDetectionThroughContainerVariants() throws CoreException {
	
	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {""}, ""),
			this.createJavaProject("P1", new String[] {""}, ""),
			this.createJavaProject("P2", new String[] {""}, ""),
			this.createJavaProject("P3", new String[] {""}, ""),
			this.createJavaProject("P4", new String[] {""}, ""),
		};

		class TestContainer implements IClasspathContainer {
			IPath path;
			IClasspathEntry[] entries;
			TestContainer(IPath path, IClasspathEntry[] entries){
				this.path = path;
				this.entries = entries;
			}
			public IPath getPath() { return this.path; }
			public IClasspathEntry[] getClasspathEntries() { return this.entries;	}
			public String getDescription() { return null; 	}
			public int getKind() { return 0; }
		};

		IClasspathContainer[] containers = new IClasspathContainer[]{ 
			new TestContainer(
				new Path("container0/default"), 
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[3].getPath()) }),
			new TestContainer(
				new Path("container0/default"), 
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[1].getPath()) }),
			new TestContainer(
				new Path("container0/default"), 
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[4].getPath()) }),
		};

		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{ 
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newContainerEntry(containers[0].getPath()) },
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newContainerEntry(containers[1].getPath()) }, 
			{ JavaCore.newContainerEntry(containers[2].getPath())}, 
			{ JavaCore.newProjectEntry(p[3].getPath()), JavaCore.newProjectEntry(p[0].getPath()) } 
		}; 

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[2]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};
		
		for (int i = 0; i < p.length; i++){

			// append project references			
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			for (int j = 0; j < extraEntries[i].length; j++){
				newClasspath[oldClasspath.length+j] = extraEntries[i][j];
			}			
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// update same container path for multiple projects
			if (i == p.length - 1){
				JavaCore.setClasspathContainer(
					containers[0].getPath(),
					new IJavaProject[]{ p[0], p[2], p[3] },
					new IClasspathContainer[] { containers[0], containers[1], containers[2] },
					null);
			}
			
			// check cycle markers
			this.assertCycleMarkers(p[i], p, expectedCycleParticipants[i]);
		}
		//this.startDeltas();
		
	} finally {
		//this.stopDeltas();
		if (p != null){
			for (int i = 0; i < p.length; i++){
				this.deleteProject(p[i].getElementName());
			}
		}
	}
}
public void testCycleDetection2() throws CoreException {
	
	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {""}, ""),
			this.createJavaProject("P1", new String[] {""}, ""),
			this.createJavaProject("P2", new String[] {""}, ""),
			this.createJavaProject("P3", new String[] {""}, ""),
			this.createJavaProject("P4", new String[] {""}, ""),
		};
		
		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{ 
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newProjectEntry(p[2].getPath()) },
			{ JavaCore.newProjectEntry(p[0].getPath()) }, 
			{ JavaCore.newProjectEntry(p[4].getPath())}, 
			{ JavaCore.newProjectEntry(p[0].getPath()) } 
		}; 

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 1, 1, 1, 0, 0 }, // after setting CP p[2]
			{ 1, 1, 1, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};
		
		for (int i = 0; i < p.length; i++){

			// append project references			
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			for (int j = 0; j < extraEntries[i].length; j++){
				newClasspath[oldClasspath.length+j] = extraEntries[i][j];
			}			
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// check cycle markers
			this.assertCycleMarkers(p[i], p, expectedCycleParticipants[i]);
		}
		//this.startDeltas();
		
	} finally {
		//this.stopDeltas();
		if (p != null){
			for (int i = 0; i < p.length; i++){
				this.deleteProject(p[i].getElementName());
			}
		}
	}
}

public void testCycleDetection3() throws CoreException {
	
	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {""}, ""),
			this.createJavaProject("P1", new String[] {""}, ""),
			this.createJavaProject("P2", new String[] {""}, ""),
			this.createJavaProject("P3", new String[] {""}, ""),
			this.createJavaProject("P4", new String[] {""}, ""),
			this.createJavaProject("P5", new String[] {""}, ""),
		};
		
		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{ 
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[4].getPath()) },
			{ JavaCore.newProjectEntry(p[0].getPath()) },
			{ JavaCore.newProjectEntry(p[3].getPath()) }, 
			{ JavaCore.newProjectEntry(p[1].getPath())}, 
			{ JavaCore.newProjectEntry(p[5].getPath()) }, 
			{ JavaCore.newProjectEntry(p[1].getPath()) } 
		}; 

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 0, 0, 0, 0, 0 }, // after setting CP p[2]
			{ 1, 1, 1, 1, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 0, 0 }, // after setting CP p[4]
			{ 1, 1, 1, 1, 1 , 1}, // after setting CP p[5]
		};
		
		for (int i = 0; i < p.length; i++){

			// append project references			
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			for (int j = 0; j < extraEntries[i].length; j++){
				newClasspath[oldClasspath.length+j] = extraEntries[i][j];
			}			
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// check cycle markers
			this.assertCycleMarkers(p[i], p, expectedCycleParticipants[i]);
		}
		//this.startDeltas();
		
	} finally {
		//this.stopDeltas();
		if (p != null){
			for (int i = 0; i < p.length; i++){
				this.deleteProject(p[i].getElementName());
			}
		}
	}
}
public void testDenseCycleDetection() throws CoreException {
	denseCycleDetection(5);
	denseCycleDetection(10);
	denseCycleDetection(20);
	//denseCycleDetection(100);
}
/**
 * Ensures that a duplicate entry created by editing the .classpath is detected.
 * (regression test for bug 24498 Duplicate entries on classpath cause CP marker to no longer refresh)
 */
public void testDuplicateEntries() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		this.editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		assertMarkers(
			"Unexpected markers",
			"Invalid classpath in \'P/.classpath\' file: Classpath contains duplicate entry: P/src",
			project);
	} finally {
		this.deleteProject("P");
	}
}

private void denseCycleDetection(final int numberOfParticipants) throws CoreException {
	
	final IJavaProject[] projects = new IJavaProject[numberOfParticipants];
	final int[] allProjectsInCycle = new int[numberOfParticipants];
	try {
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < numberOfParticipants; i++){
					projects[i] = createJavaProject("P"+i, new String[]{""}, "");
					allProjectsInCycle[i] = 1;
				}		
				//long start = System.currentTimeMillis();
				for (int i = 0; i < numberOfParticipants; i++){
					IClasspathEntry[] extraEntries = new IClasspathEntry[numberOfParticipants-1];
					int index = 0;
					for (int j = 0; j < numberOfParticipants; j++){
						if (i == j) continue;
						extraEntries[index++] = JavaCore.newProjectEntry(projects[j].getPath());
					}
					// append project references			
					IClasspathEntry[] oldClasspath = projects[i].getRawClasspath();
					IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries.length];
					System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
					for (int j = 0; j < extraEntries.length; j++){
						newClasspath[oldClasspath.length+j] = extraEntries[j];
					}			
					// set classpath
					projects[i].setRawClasspath(newClasspath, null);
				};
				//System.out.println("Dense cycle check ("+numberOfParticipants+" participants) : "+ (System.currentTimeMillis()-start)+" ms");
			}
		}, 
		null);
		
		for (int i = 0; i < numberOfParticipants; i++){
			// check cycle markers
			this.assertCycleMarkers(projects[i], projects, allProjectsInCycle);
		}
		
	} finally {
		if (projects != null){
			for (int i = 0; i < numberOfParticipants; i++){
				if (projects[i] != null)
					this.deleteProject(projects[i].getElementName());
			}
		}
	}
}

}
