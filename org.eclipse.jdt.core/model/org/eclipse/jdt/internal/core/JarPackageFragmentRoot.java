/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;

/**
 * A package fragment root that corresponds to a .jar or .zip.
 *
 * <p>NOTE: The only visible entries from a .jar or .zip package fragment root
 * are .class files.
 * <p>NOTE: A jar package fragment root may or may not have an associated resource.
 *
 * @see org.eclipse.jdt.core.IPackageFragmentRoot
 * @see org.eclipse.jdt.internal.core.JarPackageFragmentRootInfo
 */
public class JarPackageFragmentRoot extends PackageFragmentRoot {
	
	public final static String[] NO_STRINGS = new String[0];
	public final static ArrayList EMPTY_LIST = new ArrayList();
	
	/**
	 * The path to the jar file
	 * (a workspace relative path if the jar is internal,
	 * or an OS path if the jar is external)
	 */
	protected final IPath jarPath;

	/**
	 * Constructs a package fragment root which is the root of the Java package directory hierarchy 
	 * based on a JAR file that is not contained in a <code>IJavaProject</code> and
	 * does not have an associated <code>IResource</code>.
	 */
	protected JarPackageFragmentRoot(IPath jarPath, IJavaProject project) {
		super(null, project, jarPath.lastSegment());
		this.jarPath = jarPath;
	}
	/**
	 * Constructs a package fragment root which is the root of the Java package directory hierarchy 
	 * based on a JAR file.
	 */
	protected JarPackageFragmentRoot(IResource resource, IJavaProject project) {
		super(resource, project, resource.getName());
		this.jarPath = resource.getFullPath();
	}

	/**
	 * Compute the package fragment children of this package fragment root.
	 * These are all of the directory zip entries, and any directories implied
	 * by the path of class files contained in the jar of this package fragment root.
	 * Has the side effect of opening the package fragment children.
	 */
	protected boolean computeChildren(OpenableElementInfo info) throws JavaModelException {
		ArrayList vChildren= new ArrayList();
		computeJarChildren((JarPackageFragmentRootInfo) info, vChildren);
		IJavaElement[] children= new IJavaElement[vChildren.size()];
		vChildren.toArray(children);
		info.setChildren(children);
		return true;
	}
/**
 * Determine all of the package fragments associated with this package fragment root.
 * Cache the zip entries for each package fragment in the info for the package fragment.
 * The package fragment children are all opened.
 * Add all of the package fragments to vChildren.
 *
 * @exception JavaModelException The resource (the jar) associated with this package fragment root does not exist
 */
protected void computeJarChildren(JarPackageFragmentRootInfo info, ArrayList vChildren) throws JavaModelException {
	final int JAVA = 0;
	final int NON_JAVA = 1;
	ZipFile jar= null;
	try {
		jar= getJar();

		HashMap packageFragToTypes= new HashMap();

		// always create the default package
		packageFragToTypes.put(IPackageFragment.DEFAULT_PACKAGE_NAME, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });

		for (Enumeration e= jar.entries(); e.hasMoreElements();) {
			ZipEntry member= (ZipEntry) e.nextElement();
			String entryName= member.getName();

			if (member.isDirectory()) {
				
				int last = entryName.length() - 1;
				entryName= entryName.substring(0, last);
				entryName= entryName.replace('/', '.');

				// add the package name & all of its parent packages
				while (true) {
					// extract the package name
					if (packageFragToTypes.containsKey(entryName)) break;
					packageFragToTypes.put(entryName, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });
					
					if ((last = entryName.lastIndexOf('.')) < 0) break;
					entryName = entryName.substring(0, last);
				}
			} else {
				//store the class file / non-java rsc entry name to be cached in the appropriate package fragment
				//zip entries only use '/'
				int lastSeparator= entryName.lastIndexOf('/');
				String packageName;
				String fileName;
				if (lastSeparator != -1) { //not in the default package
					entryName= entryName.replace('/', '.');
					fileName= entryName.substring(lastSeparator + 1);
					packageName= entryName.substring(0, lastSeparator);
				} else {
					fileName = entryName;
					packageName =  IPackageFragment.DEFAULT_PACKAGE_NAME;
				}
				
				// add the package name & all of its parent packages
				String currentPackageName = packageName;
				while (true) {
					// extract the package name
					if (packageFragToTypes.containsKey(currentPackageName)) break;
					packageFragToTypes.put(currentPackageName, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });
					
					int last;
					if ((last = currentPackageName.lastIndexOf('.')) < 0) break;
					currentPackageName = currentPackageName.substring(0, last);
				}
				// add classfile info amongst children
				ArrayList[] children = (ArrayList[]) packageFragToTypes.get(packageName);
				if (Util.isClassFileName(entryName)) {
					if (children[JAVA] == EMPTY_LIST) children[JAVA] = new ArrayList();
					children[JAVA].add(fileName);
				} else {
					if (children[NON_JAVA] == EMPTY_LIST) children[NON_JAVA] = new ArrayList();
					children[NON_JAVA].add(fileName);
				}
			}
		}
		//loop through all of referenced packages, creating package fragments if necessary
		// and cache the entry names in the infos created for those package fragments
		Iterator packages = packageFragToTypes.keySet().iterator();
		while (packages.hasNext()) {
			String packName = (String) packages.next();
			
			ArrayList[] entries= (ArrayList[]) packageFragToTypes.get(packName);
			JarPackageFragment packFrag= (JarPackageFragment) getPackageFragment(packName);
			JarPackageFragmentInfo fragInfo= (JarPackageFragmentInfo) packFrag.createElementInfo();
			if (entries[0].size() > 0){
				fragInfo.setEntryNames(entries[JAVA]);
			}
			int resLength= entries[NON_JAVA].size();
			if (resLength == 0) {
				packFrag.computeNonJavaResources(NO_STRINGS, fragInfo, jar.getName());
			} else {
				String[] resNames= new String[resLength];
				entries[NON_JAVA].toArray(resNames);
				packFrag.computeNonJavaResources(resNames, fragInfo, jar.getName());
			}
			packFrag.computeChildren(fragInfo);
			JavaModelManager.getJavaModelManager().putInfo(packFrag, fragInfo);
			vChildren.add(packFrag);
		}
	} catch (CoreException e) {
		if (e instanceof JavaModelException) throw (JavaModelException)e;
		throw new JavaModelException(e);
	} finally {
		JavaModelManager.getJavaModelManager().closeZipFile(jar);
	}
}
	/**
	 * Returns a new element info for this element.
	 */
	protected OpenableElementInfo createElementInfo() {
		return new JarPackageFragmentRootInfo();
	}
	/**
	 * A Jar is always K_BINARY.
	 *
	 * @exception NotPresentException if the project and root do
	 *      not exist.
	 */
	protected int determineKind(IResource underlyingResource) throws JavaModelException {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * Returns true if this handle represents the same jar
	 * as the given handle. Two jars are equal if they share
	 * the same zip file.
	 *
	 * @see Object#equals
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof JarPackageFragmentRoot) {
			JarPackageFragmentRoot other= (JarPackageFragmentRoot) o;
			return this.jarPath.equals(other.jarPath);
		}
		return false;
	}
	/**
	 * Returns the underlying ZipFile for this Jar package fragment root.
	 *
	 * @exception CoreException if an error occurs accessing the jar
	 */
	public ZipFile getJar() throws CoreException {
		return JavaModelManager.getJavaModelManager().getZipFile(getPath());
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public int getKind() {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaResources() throws JavaModelException {
		// We want to show non java resources of the default package at the root (see PR #1G58NB8)
		return ((JarPackageFragment) this.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME)).storedNonJavaResources();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPackageFragment getPackageFragment(String packageName) {

		return new JarPackageFragment(this, packageName);
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPath getPath() {
		if (isExternal()) {
			return this.jarPath;
		} else {
			return super.getPath();
		}
	}
	public IResource getResource() {
		if (this.resource == null) {
			this.resource = JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), this.jarPath, false);
		}
		if (this.resource instanceof IResource) {
			return super.getResource();
		} else {
			// external jar
			return null;
		}
	}


	/**
	 * @see IJavaElement
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		if (isExternal()) {
			if (!exists()) throw newNotPresentException();
			return null;
		} else {
			return super.getUnderlyingResource();
		}
	}
	/**
	 * If I am not open, return true to avoid parsing.
	 *
	 * @see IParent 
	 */
	public boolean hasChildren() throws JavaModelException {
		if (isOpen()) {
			return getChildren().length > 0;
		} else {
			return true;
		}
	}
	public int hashCode() {
		return this.jarPath.hashCode();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public boolean isArchive() {
		return true;
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public boolean isExternal() {
		return getResource() == null;
	}
	/**
	 * Jars and jar entries are all read only
	 */
	public boolean isReadOnly() {
		return true;
	}

	/**
	 * An archive cannot refresh its children.
	 */
	public void refreshChildren() {
		// do nothing
	}
/**
 * Returns whether the corresponding resource or associated file exists
 */
protected boolean resourceExists() {
	if (this.isExternal()) {
		return 
			JavaModel.getTarget(
				ResourcesPlugin.getWorkspace().getRoot(), 
				this.getPath(), // don't make the path relative as this is an external archive
				true) != null;
	} else {
		return super.resourceExists();
	}
}
/*
 * @see JavaElement#rootedAt(IJavaProject)
 */
public IJavaElement rootedAt(IJavaProject project) {
	if (isExternal()) {
		return
			new JarPackageFragmentRoot(
				this.jarPath,
				project);
	} else {
		return
			new JarPackageFragmentRoot(
				getResource(),
				project);
	}
}
}
