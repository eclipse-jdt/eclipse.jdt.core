/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A package fragment that represents a package fragment found in a JAR.
 *
 * @see org.eclipse.jdt.core.IPackageFragment
 */
class JarPackageFragment extends PackageFragment implements SuffixConstants {
/**
 * Constructs a package fragment that is contained within a jar or a zip.
 */
protected JarPackageFragment(PackageFragmentRoot root, String[] names) {
	super(root, names);
}
/**
 * Compute the children of this package fragment. Children of jar package fragments
 * can only be IClassFile (representing .class files).
 */
protected boolean computeChildren(OpenableElementInfo info, ArrayList entryNames) {
	if (entryNames != null && entryNames.size() > 0) {
		ArrayList vChildren = new ArrayList();
		for (Iterator iter = entryNames.iterator(); iter.hasNext();) {
			String child = (String) iter.next();
			IClassFile classFile = getClassFile(child);
			vChildren.add(classFile);
		}
		IJavaElement[] children= new IJavaElement[vChildren.size()];
		vChildren.toArray(children);
		info.setChildren(children);
	} else {
		info.setChildren(NO_ELEMENTS);
	}
	return true;
}
/**
 * Compute all the non-java resources according to the entry name found in the jar file.
 */
/* package */ void computeNonJavaResources(String[] resNames, JarPackageFragmentInfo info, String zipName) {
	if (resNames == null) {
		info.setNonJavaResources(null);
		return;
	}
	int max = resNames.length;
	if (max == 0) {
	    info.setNonJavaResources(JavaElementInfo.NO_NON_JAVA_RESOURCES);
	} else {
		Object[] res = new Object[max];
		int index = 0;
		for (int i = 0; i < max; i++) {
			String resName = resNames[i];
			// consider that a .java file is not a non-java resource (see bug 12246 Packages view shows .class and .java files when JAR has source)
			if (!Util.isJavaLikeFileName(resName)) {
				resName = Util.concatWith(this.names, resName, '/');
				res[index++] = new JarEntryFile(resName, zipName);
			}
		} 
		if (index != max) {
			System.arraycopy(res, 0, res = new Object[index], 0, index);
		}
		info.setNonJavaResources(res);
	}
}
/**
 * Returns true if this fragment contains at least one java resource.
 * Returns false otherwise.
 */
public boolean containsJavaResources() throws JavaModelException {
	return ((JarPackageFragmentInfo) getElementInfo()).containsJavaResources();
}
/**
 * @see org.eclipse.jdt.core.IPackageFragment
 */
public ICompilationUnit createCompilationUnit(String cuName, String contents, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see JavaElement
 */
protected Object createElementInfo() {
	return null; // not used for JarPackageFragments: info is created when jar is opened
}
/*
 * @see JavaElement#generateInfos
 */
protected void generateInfos(Object info, HashMap newElements, IProgressMonitor pm) throws JavaModelException {
	// Open my jar: this creates all the pkg infos
	Openable openableParent = (Openable)this.parent;
	if (!openableParent.isOpen()) {
		openableParent.generateInfos(openableParent.createElementInfo(), newElements, pm);
	}
}
/**
 * @see org.eclipse.jdt.core.IPackageFragment
 */
public IClassFile[] getClassFiles() throws JavaModelException {
	ArrayList list = getChildrenOfType(CLASS_FILE);
	IClassFile[] array= new IClassFile[list.size()];
	list.toArray(array);
	return array;
}
/**
 * A jar package fragment never contains compilation units.
 * @see org.eclipse.jdt.core.IPackageFragment
 */
public ICompilationUnit[] getCompilationUnits() {
	return NO_COMPILATION_UNITS;
}
/**
 * A package fragment in a jar has no corresponding resource.
 *
 * @see IJavaElement
 */
public IResource getCorrespondingResource() {
	return null;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
public Object[] getNonJavaResources() throws JavaModelException {
	if (this.isDefaultPackage()) {
		// We don't want to show non java resources of the default package (see PR #1G58NB8)
		return JavaElementInfo.NO_NON_JAVA_RESOURCES;
	} else {
		return this.storedNonJavaResources();
	}
}
/**
 * Jars and jar entries are all read only
 */
public boolean isReadOnly() {
	return true;
}
protected Object[] storedNonJavaResources() throws JavaModelException {
	return ((JarPackageFragmentInfo) getElementInfo()).getNonJavaResources();
}
}
