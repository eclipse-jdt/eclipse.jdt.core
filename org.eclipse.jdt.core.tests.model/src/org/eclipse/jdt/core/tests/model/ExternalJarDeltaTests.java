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

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.model.*;

/**
 * These test ensure that modifications in external jar are correctly reported as
 * IJavaEllementDeltas after a JavaModel#refreshExternalArchives().
 */
public class ExternalJarDeltaTests extends ModifyingResourceTests {
	
public ExternalJarDeltaTests(String name) {
	super(name);
}



public static Test suite() {
	TestSuite suite = new Suite(ExternalJarDeltaTests.class.getName());
	
	suite.addTest(new ExternalJarDeltaTests("testExternalJar0"));

	suite.addTest(new ExternalJarDeltaTests("testExternalJarChanged1"));
	suite.addTest(new ExternalJarDeltaTests("testExternalJarChanged2"));
	suite.addTest(new ExternalJarDeltaTests("testExternalJarChanged3"));
	suite.addTest(new ExternalJarDeltaTests("testExternalJarAdded1"));
	suite.addTest(new ExternalJarDeltaTests("testExternalJarAdded2"));
	suite.addTest(new ExternalJarDeltaTests("testExternalJarAdded3"));
	suite.addTest(new ExternalJarDeltaTests("testExternalJarRemoved1"));
	suite.addTest(new ExternalJarDeltaTests("testExternalJarRemoved2"));
	suite.addTest(new ExternalJarDeltaTests("testExternalJarRemoved3"));
	
	suite.addTest(new ExternalJarDeltaTests("testExternalJarInternalExternalJar"));
	
	return suite;
}

private String getExternalPath() {
	return EXTERNAL_JAR_DIR_PATH;
}

private String getLibraryPath() {
	return getPluginDirectoryPath().substring(1).replace(File.separatorChar,'/')+"/LIB";
}

/**
 * Test if a modification is detected without doing a refresh.
 * Currently no modification are detected.
 */
public void testExternalJar0() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		String libPath = getLibraryPath();
		String pPath = getExternalPath()+"/p.jar";
		String p1Path = libPath+"/p1.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f1 = new File(p1Path);
		File f2 = new File(p2Path);
		
		copy(f1, f);
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		copy(f2, f);
		
		assertEquals(
			"Unexpected delta", 
			"", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}

/**
 * Refresh the JavaModel after a modification of an external jar.
 */
public void testExternalJarChanged1() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath = getLibraryPath();
		String pPath = getExternalPath()+"/p.jar";
		String p1Path = libPath+"/p1.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f1 = new File(p1Path);
		File f2 = new File(p2Path);
		
		copy(f1, f);
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		copy(f2, f);
		this.getJavaModel().refreshExternalArchives(null,null);
		
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	"+pPath+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
/**
 * Refresh a JavaProject after a modification of an external jar.
 */
public void testExternalJarChanged2() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath = getLibraryPath();
		String pPath = getExternalPath()+"/p.jar";
		String p1Path = libPath+"/p1.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f1 = new File(p1Path);
		File f2 = new File(p2Path);
		
		copy(f1, f);
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		copy(f2, f);
		this.getJavaModel().refreshExternalArchives(new IJavaElement[]{project},null);
		
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	"+pPath+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
/**
 * Refresh an external jar after a modification of this jar.
 */
public void testExternalJarChanged3() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath = getLibraryPath();
		String pPath = getExternalPath()+"/p.jar";
		String p1Path = libPath+"/p1.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f1 = new File(p1Path);
		File f2 = new File(p2Path);
		
		copy(f1, f);
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		copy(f2, f);
		
		IPackageFragmentRoot root = project.getPackageFragmentRoot(pPath);
		this.getJavaModel().refreshExternalArchives(new IJavaElement[]{root},null);
		
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	"+pPath+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
/**
 * Refresh the JavaModel after an addition of an external jar.
 */
public void testExternalJarAdded1() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath = getLibraryPath();
		String pPath = getExternalPath()+"/pAdded1.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f2 = new File(p2Path);
		
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		copy(f2, f);
		this.getJavaModel().refreshExternalArchives(null,null);
		
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	"+pPath+"[+]: {}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
/**
 * Refresh a JavaProject after an addition of an external jar.
 */
public void testExternalJarAdded2() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath = getLibraryPath();
		String pPath = getExternalPath()+"/pAdded2.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f2 = new File(p2Path);
		
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		copy(f2, f);
		this.getJavaModel().refreshExternalArchives(new IJavaElement[]{project},null);
		
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	"+pPath+"[+]: {}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
/**
 * Refresh an external jar after an addition of this jar.
 */
public void testExternalJarAdded3() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath =getLibraryPath();
		String pPath = getExternalPath()+"/pAdded3.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f2 = new File(p2Path);
		
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		copy(f2, f);
		IPackageFragmentRoot root = project.getPackageFragmentRoot(pPath);
		this.getJavaModel().refreshExternalArchives(new IJavaElement[]{root},null);
		
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	"+pPath+"[+]: {}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
/**
 * Refresh the JavaModel after a removal of an external jar.
 */
public void testExternalJarRemoved1() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath = getLibraryPath();
		String pPath = getExternalPath()+"/p.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f2 = new File(p2Path);
		
		copy(f2, f);
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		deleteFile(f);
		this.getJavaModel().refreshExternalArchives(null,null);
		
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	"+pPath+"[-]: {}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
/**
 * Refresh a JavaProject after a removal of an external jar.
 */
public void testExternalJarRemoved2() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath = getLibraryPath();
		String pPath = getExternalPath()+"/p.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f2 = new File(p2Path);
		
		copy(f2, f);
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		deleteFile(f);
		this.getJavaModel().refreshExternalArchives(new IJavaElement[]{project},null);
		
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	"+pPath+"[-]: {}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
/**
 * Refresh an external jar after a removal of this jar.
 */
public void testExternalJarRemoved3() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath = getLibraryPath();
		String pPath = getExternalPath()+"/p.jar";
		String p2Path = libPath+"/p2.jar";
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});
		
		f = new File(pPath);
		File f2 = new File(p2Path);
		
		copy(f2, f);
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		deleteFile(f);
		IPackageFragmentRoot root = project.getPackageFragmentRoot(pPath);
		this.getJavaModel().refreshExternalArchives(new IJavaElement[]{root},null);
		
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	"+pPath+"[-]: {}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
/**
 * - add an internal jar to claspath
 * - remove internal jar and the same jar as external jar
 * - refresh the JavaModel
 */
public void testExternalJarInternalExternalJar() throws CoreException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		
		String libPath = getLibraryPath();
		String p1Path = libPath+"/p1.jar";
		String p2Path = libPath+"/p2.jar";
		File f1= new File(p1Path);
		File f2 = new File(p2Path);
		
		String internalFooPath = "/P/foo.jar";
		IFile fooIFile = this.createFile(internalFooPath, read(f1));
		
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(internalFooPath), null, null)});
		this.getJavaModel().refreshExternalArchives(null,null);
		this.startDeltas();
		
		IPath externalFooPath = fooIFile.getLocation();
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(externalFooPath, null, null)});
		
		f = new File(externalFooPath.toString());
		copy(f2, f);
		
		this.getJavaModel().refreshExternalArchives(null,null);
		
		String deltaPath = externalFooPath.toString();
		deltaPath = Character.toUpperCase(deltaPath.charAt(0)) + deltaPath.substring(1);
		assertEquals(
			"Unexpected delta", 
			"P[*]: {CHILDREN}\n"+
			"	foo.jar[*]: {REMOVED FROM CLASSPATH}\n"+
			"	"+deltaPath.replace('/', File.separatorChar)+"[*]: {ADDED TO CLASSPATH}\n"+
			"	ResourceDelta(/P/.classpath)[*]\n"+
			"\n"+
			"P[*]: {CHILDREN}\n"+
			"	"+deltaPath+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}", 
			this.getDeltas());
	} catch (IOException e) {
	} finally {
		if(f != null) {
			deleteFile(f);
		}
		this.deleteProject("P");
		this.stopDeltas();
	}
}
}

