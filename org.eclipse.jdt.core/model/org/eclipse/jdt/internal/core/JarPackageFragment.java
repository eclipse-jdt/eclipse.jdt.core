package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;

import java.util.*;
import java.util.zip.*;

/**
 * A package fragment that represents a package fragment found in a JAR.
 *
 * @see IPackageFragment
 */
class JarPackageFragment extends PackageFragment {
/**
 * Constructs a package fragment that is contained within a jar or a zip.
 */
protected JarPackageFragment(IPackageFragmentRoot root, String name) {
	super(root, name);
}
/**
 * Compute the children of this package fragment. Children of jar package fragments
 * can only be IClassFile (representing .class files).
 */
protected boolean computeChildren(OpenableElementInfo info) {
	Vector vChildren = new Vector();
	JarPackageFragmentInfo jInfo= (JarPackageFragmentInfo)info;
	for (Enumeration e = jInfo.fEntryNames.elements(); e.hasMoreElements();) {
		String child = (String) e.nextElement();
		IClassFile classFile = getClassFile(child);
		vChildren.addElement(classFile);
	}
	vChildren.trimToSize();
	IJavaElement[] children= new IJavaElement[vChildren.size()];
	vChildren.copyInto(children);
	info.setChildren(children);
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
	Object[] res = new Object[max];
	for (int i = 0; i < max; i++) {
		res[i] = new JarEntryFile(resNames[i], zipName);
	} 
	info.setNonJavaResources(res);
}
/**
 * Returns true if this fragment contains at least one java resource.
 * Returns false otherwise.
 */
public boolean containsJavaResources() throws JavaModelException {
	return ((JarPackageFragmentInfo) getElementInfo()).containsJavaResources();
}
/**
 * @see IPackageFragment
 */
public ICompilationUnit createCompilationUnit(String name, String contents, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see JavaElement
 */
protected OpenableElementInfo createElementInfo() {
	return new JarPackageFragmentInfo();
}
/**
 * @see IPackageFragment
 */
public IClassFile[] getClassFiles() throws JavaModelException {
	Vector v= getChildrenOfType(CLASS_FILE);
	IClassFile[] array= new IClassFile[v.size()];
	v.copyInto(array);
	return array;
}
/**
 * A jar package fragment never contains compilation units.
 * @see IPackageFragment
 */
public ICompilationUnit[] getCompilationUnits() throws JavaModelException {
	return fgEmptyCompilationUnitList;
}
/**
 * A package fragment in a jar has no corresponding resource.
 *
 * @see IJavaElement
 */
public IResource getCorrespondingResource() throws JavaModelException {
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
/**
 * @see Openable#openWhenClosed()
 */
protected void openWhenClosed(IProgressMonitor pm) throws JavaModelException {
	// Open my jar
	getOpenableParent().open(pm);
}
/**
 * A package fragment in an archive cannot refresh its children.
 */
public void refreshChildren() {
	// do nothing
}
protected Object[] storedNonJavaResources() throws JavaModelException {
	return ((JarPackageFragmentInfo) getElementInfo()).getNonJavaResources();
}
}
