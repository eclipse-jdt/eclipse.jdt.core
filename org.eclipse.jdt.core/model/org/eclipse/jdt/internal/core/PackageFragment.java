package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.*;

import java.util.*;

/**
 * @see IPackageFragment
 */
public class PackageFragment extends Openable implements IPackageFragment {
	/**
	 * Constant empty list of class files
	 */
	protected static IClassFile[] fgEmptyClassFileList= new IClassFile[] {};
	/**
	 * Constant empty list of compilation units
	 */
	protected static ICompilationUnit[] fgEmptyCompilationUnitList= new ICompilationUnit[] {};
/**
 * Constructs a handle for a package fragment
 *
 * @see IPackageFragment
 */
protected PackageFragment(IPackageFragmentRoot root, String name) {
	super(PACKAGE_FRAGMENT, root, name);
}
/**
 * Compute the children of this package fragment.
 *
 * <p>Package fragments which are folders recognize files based on the
 * type of the fragment
 * <p>Package fragments which are in a jar only recognize .class files (
 * @see JarPackageFragment).
 */
protected boolean computeChildren(OpenableElementInfo info, IResource resource) throws JavaModelException {
	Vector vChildren = new Vector();
	int kind = getKind();
	String extType;
	if (kind == IPackageFragmentRoot.K_SOURCE) {
		extType = "java"/*nonNLS*/;
	} else {
		extType = "class"/*nonNLS*/;
	}
	try {
		IResource[] members = ((IContainer) resource).members();
		for (int i = 0, max = members.length; i < max; i++) {
			IResource child = members[i];
			if (child.getType() != IResource.FOLDER) {
				String extension = child.getProjectRelativePath().getFileExtension();
				if (extension != null) {
					if (extension.equalsIgnoreCase(extType)) {
						IJavaElement childElement;
						if (kind == IPackageFragmentRoot.K_SOURCE) {
							childElement = getCompilationUnit(child.getName());
						} else {
							childElement = getClassFile(child.getName());
						}
						vChildren.addElement(childElement);
					}
				}
			}
		}
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	vChildren.trimToSize();
	IJavaElement[] children = new IJavaElement[vChildren.size()];
	vChildren.copyInto(children);
	info.setChildren(children);
	return true;
}
/**
 * Returns true if this fragment contains at least one java resource.
 * Returns false otherwise.
 */
public boolean containsJavaResources() throws JavaModelException {
	return ((PackageFragmentInfo) getElementInfo()).containsJavaResources();
}
/**
 * @see ISourceManipulation
 */
public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Util.bind("operation.nullContainer"/*nonNLS*/));
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] containers= new IJavaElement[] {container};
	IJavaElement[] siblings= null;
	if (sibling != null) {
		siblings= new IJavaElement[] {sibling};
	}
	String[] renamings= null;
	if (rename != null) {
		renamings= new String[] {rename};
	}
	getJavaModel().copy(elements, containers, siblings, renamings, force, monitor);
}
/**
 * @see IPackageFragment
 */
public ICompilationUnit createCompilationUnit(String name, String contents, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateCompilationUnitOperation op= new CreateCompilationUnitOperation(this, name, contents, force);
	runOperation(op, monitor);
	return getCompilationUnit(name);
}
/**
 * @see JavaElement
 */
protected OpenableElementInfo createElementInfo() {
	return new PackageFragmentInfo();
}
/**
 * @see ISourceManipulation
 */
public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	IJavaElement[] elements = new IJavaElement[] {this};
	getJavaModel().delete(elements, force, monitor);
}
/**
 * @see Openable
 */
protected boolean generateInfos(OpenableElementInfo info, IProgressMonitor pm, Hashtable newElements, IResource underlyingResource) throws JavaModelException {
	return computeChildren(info, underlyingResource);
}
/**
 * @see IPackageFragment
 */
public IClassFile getClassFile(String name) {
	return new ClassFile(this, name);
}
/**
 * Returns a the collection of class files in this - a folder package fragment which has a root
 * that has its kind set to <code>IPackageFragmentRoot.K_Source</code> does not
 * recognize class files.
 *
 * @see IPackageFragment
 * @see JarPackageFragment
 */
public IClassFile[] getClassFiles() throws JavaModelException {
	if (getKind() == IPackageFragmentRoot.K_SOURCE) {
		return fgEmptyClassFileList;
	}
	
	Vector v= getChildrenOfType(CLASS_FILE);
	IClassFile[] array= new IClassFile[v.size()];
	v.copyInto(array);
	return array;
}
/**
 * @see IPackageFragment
 */
public ICompilationUnit getCompilationUnit(String name) {
	return new CompilationUnit(this, name);
}
/**
 * @see IPackageFragment
 */
public ICompilationUnit[] getCompilationUnits() throws JavaModelException {
	if (getKind() == IPackageFragmentRoot.K_BINARY) {
		return fgEmptyCompilationUnitList;
	}
	
	Vector v= getChildrenOfType(COMPILATION_UNIT);
	ICompilationUnit[] array= new ICompilationUnit[v.size()];
	v.copyInto(array);
	return array;
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_PACKAGEFRAGMENT;
}
/**
 * @see IPackageFragment
 */
public int getKind() throws JavaModelException {
	return ((IPackageFragmentRoot)getParent()).getKind();
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
public Object[] getNonJavaResources() throws JavaModelException {
	if (this.isDefaultPackage()) {
		// We don't want to show non java resources of the default package (see PR #1G58NB8)
		return JavaElementInfo.NO_NON_JAVA_RESOURCES;
	} else {
		return ((PackageFragmentInfo) getElementInfo()).getNonJavaResources(getUnderlyingResource());
	}
}
/**
 * @see IJavaElement
 */
public IResource getUnderlyingResource() throws JavaModelException {
	IResource rootResource = fParent.getUnderlyingResource();
	if (rootResource == null) {
		//jar package fragment root that has no associated resource
		return null;
	}
	// the underlying resource may be a folder or a project (in the case that the project folder
	// is atually the package fragment root)
	if (rootResource.getType() == IResource.FOLDER || rootResource.getType() == IResource.PROJECT) {
		IContainer folder = (IContainer) rootResource;
		String[] segs = Signature.getSimpleNames(fName);
		for (int i = 0; i < segs.length; ++i) {
			IResource child = folder.findMember(segs[i]);
			if (child == null || child.getType() != IResource.FOLDER) {
				throw newNotPresentException();
			}
			folder = (IFolder) child;
		}
		return folder;
	} else {
		return rootResource;
	}
}
/**
 * @see IPackageFragment
 */
public boolean hasSubpackages() throws JavaModelException {
	IJavaElement[] packages= ((IPackageFragmentRoot)getParent()).getChildren();
	String name = getElementName();
	int nameLength = name.length();
	String packageName = isDefaultPackage() ? name : name+"."/*nonNLS*/;
	for (int i= 0; i < packages.length; i++) {
		String otherName = packages[i].getElementName();
		if (otherName.length() > nameLength && otherName.startsWith(packageName)) {
			return true;
		}
	}
	return false;
}
/**
 * @see IPackageFragment
 */
public boolean isDefaultPackage() {
	return this.getElementName().length() == 0;
}
/**
 * @see ISourceManipulation
 */
public void move(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Util.bind("operation.nullContainer"/*nonNLS*/));
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] containers= new IJavaElement[] {container};
	IJavaElement[] siblings= null;
	if (sibling != null) {
		siblings= new IJavaElement[] {sibling};
	}
	String[] renamings= null;
	if (rename != null) {
		renamings= new String[] {rename};
	}
	getJavaModel().move(elements, containers, siblings, renamings, force, monitor);
}
/**
 * Recomputes the children of this element, based on the current state
 * of the workbench.
 */
public void refreshChildren() {
	try {
		OpenableElementInfo info= (OpenableElementInfo)getElementInfo();
		computeChildren(info, getUnderlyingResource());
	} catch (JavaModelException e) {
		// do nothing.
	}
}
/**
 * @see ISourceManipulation
 */
public void rename(String name, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (name == null) {
		throw new IllegalArgumentException(Util.bind("element.nullName"/*nonNLS*/));
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] dests= new IJavaElement[] {this.getParent()};
	String[] renamings= new String[] {name};
	getJavaModel().rename(elements, dests, renamings, force, monitor);
}
/**
 * @private Debugging purposes
 */
protected void toStringChildren(int tab, StringBuffer buffer, Object info) {
	if (tab == 0) {
		super.toStringChildren(tab, buffer, info);
	}
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	if (getElementName().length() == 0) {
		buffer.append("[default]"/*nonNLS*/);
	} else {
		buffer.append(getElementName());
	}
	if (info == null) {
		buffer.append(" (not open)"/*nonNLS*/);
	} else {
		if (tab > 0) {
			buffer.append(" (...)"/*nonNLS*/);
		}
	}
}
}
