/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;
import org.eclipse.jdt.internal.core.util.Util;

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
	protected JarPackageFragmentRoot(IPath jarPath, JavaProject project) {
		super(null, project);
		this.jarPath = jarPath;
	}
	/**
	 * Constructs a package fragment root which is the root of the Java package directory hierarchy 
	 * based on a JAR file.
	 */
	protected JarPackageFragmentRoot(IResource resource, JavaProject project) {
		super(resource, project);
		this.jarPath = resource.getFullPath();
	}

	/**
	 * Compute the package fragment children of this package fragment root.
	 * These are all of the directory zip entries, and any directories implied
	 * by the path of class files contained in the jar of this package fragment root.
	 * Has the side effect of opening the package fragment children.
	 */
	protected boolean computeChildren(OpenableElementInfo info, Map newElements) throws JavaModelException {
		
		ArrayList vChildren= new ArrayList();
		final int JAVA = 0;
		final int NON_JAVA = 1;
		ZipFile jar= null;
		try {
			jar= getJar();
	
			HashtableOfArrayToObject packageFragToTypes= new HashtableOfArrayToObject();
	
			// always create the default package
			packageFragToTypes.put(CharOperation.NO_STRINGS, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });
	
			for (Enumeration e= jar.entries(); e.hasMoreElements();) {
				ZipEntry member= (ZipEntry) e.nextElement();
				initPackageFragToTypes(packageFragToTypes, member.getName(), member.isDirectory());
			}
			
			//loop through all of referenced packages, creating package fragments if necessary
			// and cache the entry names in the infos created for those package fragments
			for (int i = 0, length = packageFragToTypes.keyTable.length; i < length; i++) {
				String[] pkgName = (String[]) packageFragToTypes.keyTable[i];
				if (pkgName == null) continue;
				
				ArrayList[] entries= (ArrayList[]) packageFragToTypes.get(pkgName);
				JarPackageFragment packFrag= (JarPackageFragment) getPackageFragment(pkgName);
				JarPackageFragmentInfo fragInfo= new JarPackageFragmentInfo();
				int resLength= entries[NON_JAVA].size();
				if (resLength == 0) {
					packFrag.computeNonJavaResources(CharOperation.NO_STRINGS, packFrag, fragInfo, jar.getName());
				} else {
					String[] resNames= new String[resLength];
					entries[NON_JAVA].toArray(resNames);
					packFrag.computeNonJavaResources(resNames, packFrag, fragInfo, jar.getName());
				}
				packFrag.computeChildren(fragInfo, entries[JAVA]);
				newElements.put(packFrag, fragInfo);
				vChildren.add(packFrag);
			}
		} catch (CoreException e) {
			if (e instanceof JavaModelException) throw (JavaModelException)e;
			throw new JavaModelException(e);
		} finally {
			JavaModelManager.getJavaModelManager().closeZipFile(jar);
		}


		IJavaElement[] children= new IJavaElement[vChildren.size()];
		vChildren.toArray(children);
		info.setChildren(children);
		return true;
	}
	/**
	 * Returns a new element info for this element.
	 */
	protected Object createElementInfo() {
		return new JarPackageFragmentRootInfo();
	}
	/**
	 * A Jar is always K_BINARY.
	 */
	protected int determineKind(IResource underlyingResource) {
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
	public String getElementName() {
		return this.jarPath.lastSegment();
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
		Object[] defaultPkgResources =  ((JarPackageFragment) getPackageFragment(CharOperation.NO_STRINGS)).storedNonJavaResources();
		int length = defaultPkgResources.length;
		if (length == 0)
			return defaultPkgResources;
		Object[] nonJavaResources = new Object[length];
		for (int i = 0; i < length; i++) {
			JarEntryResource nonJavaResource = (JarEntryResource) defaultPkgResources[i];
			nonJavaResources[i] = nonJavaResource.clone(this);
		}
		return nonJavaResources;
	}
	public PackageFragment getPackageFragment(String[] pkgName) {
		return new JarPackageFragment(this, pkgName);
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
	public int hashCode() {
		return this.jarPath.hashCode();
	}
	private void initPackageFragToTypes(HashtableOfArrayToObject packageFragToTypes, String entryName, boolean isDirectory) {
		int lastSeparator = isDirectory ? entryName.length()-1 : entryName.lastIndexOf('/');
		String[] pkgName = Util.splitOn('/', entryName, 0, lastSeparator);
		String[] existing = null;
		int length = pkgName.length;
		int existingLength = length;
		while (existingLength >= 0) {
			existing = (String[]) packageFragToTypes.getKey(pkgName, existingLength);
			if (existing != null) break;
			existingLength--;
		}
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IJavaProject project = getJavaProject();
		for (int i = existingLength; i < length; i++) {
			if (Util.isValidFolderNameForPackage(pkgName[i], project.getOption(JavaCore.COMPILER_SOURCE, true), project.getOption(JavaCore.COMPILER_COMPLIANCE, true))) {
				System.arraycopy(existing, 0, existing = new String[i+1], 0, i);
				existing[i] = manager.intern(pkgName[i]);
				packageFragToTypes.put(existing, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });
			} else {
				// non-Java resource folder
				if (!isDirectory) {
					ArrayList[] children = (ArrayList[]) packageFragToTypes.get(existing);
					if (children[1/*NON_JAVA*/] == EMPTY_LIST) children[1/*NON_JAVA*/] = new ArrayList();
					children[1/*NON_JAVA*/].add(entryName);
				}
				return;
			}
		}
		if (isDirectory)
			return;
		
		// add classfile info amongst children
		ArrayList[] children = (ArrayList[]) packageFragToTypes.get(pkgName);
		if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(entryName)) {
			if (children[0/*JAVA*/] == EMPTY_LIST) children[0/*JAVA*/] = new ArrayList();
			String fileName = entryName.substring(lastSeparator + 1);
			children[0/*JAVA*/].add(fileName);
		} else {
			if (children[1/*NON_JAVA*/] == EMPTY_LIST) children[1/*NON_JAVA*/] = new ArrayList();
			children[1/*NON_JAVA*/].add(entryName);
		}
		
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
protected void toStringAncestors(StringBuffer buffer) {
	if (isExternal())
		// don't show project as it is irrelevant for external jar files.
		// also see https://bugs.eclipse.org/bugs/show_bug.cgi?id=146615
		return;
	super.toStringAncestors(buffer);
}
}
