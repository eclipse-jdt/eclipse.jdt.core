/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @see IPackageFragment
 */
public class PackageFragment extends Openable implements IPackageFragment, SuffixConstants {
	/**
	 * Constant empty list of class files
	 */
	protected static final IClassFile[] NO_CLASSFILES = new IClassFile[] {};
	/**
	 * Constant empty list of compilation units
	 */
	protected static final ICompilationUnit[] NO_COMPILATION_UNITS = new ICompilationUnit[] {};
/**
 * Constructs a handle for a package fragment
 *
 * @see IPackageFragment
 */
protected PackageFragment(PackageFragmentRoot root, String name) {
	super(root, name);
}
/**
 * @see Openable
 */
protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {

	// check whether this pkg can be opened
	if (!underlyingResource.isAccessible()) throw newNotPresentException();

	int kind = getKind();
	String extType;
	if (kind == IPackageFragmentRoot.K_SOURCE) {
		extType = EXTENSION_java;
	} else {
		extType = EXTENSION_class;
	}

	// add compilation units/class files from resources
	HashSet vChildren = new HashSet();
	try {
	    PackageFragmentRoot root = getPackageFragmentRoot();
		char[][] inclusionPatterns = root.fullInclusionPatternChars();
		char[][] exclusionPatterns = root.fullExclusionPatternChars();
		IResource[] members = ((IContainer) underlyingResource).members();
		for (int i = 0, max = members.length; i < max; i++) {
			IResource child = members[i];
			if (child.getType() != IResource.FOLDER
					&& !Util.isExcluded(child, inclusionPatterns, exclusionPatterns)) {
				String extension = child.getProjectRelativePath().getFileExtension();
				if (extension != null) {
					if (extension.equalsIgnoreCase(extType)) {
						IJavaElement childElement;
						if (kind == IPackageFragmentRoot.K_SOURCE && Util.isValidCompilationUnitName(child.getName())) {
							childElement = new CompilationUnit(this, child.getName(), DefaultWorkingCopyOwner.PRIMARY);
							vChildren.add(childElement);
						} else if (Util.isValidClassFileName(child.getName())) {
							childElement = getClassFile(child.getName());
							vChildren.add(childElement);
						}
					}
				}
			}
		}
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	
	if (kind == IPackageFragmentRoot.K_SOURCE) {
		// add primary compilation units
		ICompilationUnit[] primaryCompilationUnits = getCompilationUnits(DefaultWorkingCopyOwner.PRIMARY);
		for (int i = 0, length = primaryCompilationUnits.length; i < length; i++) {
			ICompilationUnit primary = primaryCompilationUnits[i];
			vChildren.add(primary);
		}
	}
	
	IJavaElement[] children = new IJavaElement[vChildren.size()];
	vChildren.toArray(children);
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
		throw new IllegalArgumentException(Util.bind("operation.nullContainer")); //$NON-NLS-1$
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
public ICompilationUnit createCompilationUnit(String cuName, String contents, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateCompilationUnitOperation op= new CreateCompilationUnitOperation(this, cuName, contents, force);
	op.runOperation(monitor);
	return new CompilationUnit(this, cuName, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see JavaElement
 */
protected Object createElementInfo() {
	return new PackageFragmentInfo();
}
/**
 * @see ISourceManipulation
 */
public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	IJavaElement[] elements = new IJavaElement[] {this};
	getJavaModel().delete(elements, force, monitor);
}
public boolean equals(Object o) {
	if (!(o instanceof PackageFragment)) return false;
	return super.equals(o);
}
/**
 * @see IPackageFragment#getClassFile(String)
 * @exception IllegalArgumentExcpetion if the name does not end with ".class"
 */
public IClassFile getClassFile(String classFileName) {
	if (!org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(classFileName)) {
		throw new IllegalArgumentException(Util.bind("element.invalidClassFileName")); //$NON-NLS-1$
	}
	return new ClassFile(this, classFileName);
}
/**
 * Returns a the collection of class files in this - a folder package fragment which has a root
 * that has its kind set to <code>IPackageFragmentRoot.K_Source</code> does not
 * recognize class files.
 *
 * @see IPackageFragment#getClassFiles()
 */
public IClassFile[] getClassFiles() throws JavaModelException {
	if (getKind() == IPackageFragmentRoot.K_SOURCE) {
		return NO_CLASSFILES;
	}
	
	ArrayList list = getChildrenOfType(CLASS_FILE);
	IClassFile[] array= new IClassFile[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IPackageFragment#getCompilationUnit(String)
 * @exception IllegalArgumentExcpetion if the name does not end with ".java"
 */
public ICompilationUnit getCompilationUnit(String cuName) {
	if (!org.eclipse.jdt.internal.compiler.util.Util.isJavaFileName(cuName)) {
		throw new IllegalArgumentException(Util.bind("convention.unit.notJavaName")); //$NON-NLS-1$
	}
	return new CompilationUnit(this, cuName, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see IPackageFragment#getCompilationUnits()
 */
public ICompilationUnit[] getCompilationUnits() throws JavaModelException {
	if (getKind() == IPackageFragmentRoot.K_BINARY) {
		return NO_COMPILATION_UNITS;
	}
	
	ArrayList list = getChildrenOfType(COMPILATION_UNIT);
	ICompilationUnit[] array= new ICompilationUnit[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IPackageFragment#getCompilationUnits(WorkingCopyOwner)
 */
public ICompilationUnit[] getCompilationUnits(WorkingCopyOwner owner) {
	ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, false/*don't add primary*/);
	if (workingCopies == null) return JavaModelManager.NO_WORKING_COPY;
	int length = workingCopies.length;
	ICompilationUnit[] result = new ICompilationUnit[length];
	int index = 0;
	for (int i = 0; i < length; i++) {
		ICompilationUnit wc = workingCopies[i];
		if (equals(wc.getParent())) {
			result[index++] = wc;
		}
	}
	if (index != length) {
		System.arraycopy(result, 0, result = new ICompilationUnit[index], 0, index);
	}
	return result;
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return PACKAGE_FRAGMENT;
}
/*
 * @see JavaElement
 */
public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner owner) {
	switch (token.charAt(0)) {
		case JEM_COUNT:
			return getHandleUpdatingCountFromMemento(memento, owner);
		case JEM_CLASSFILE:
			String classFileName = memento.nextToken();
			JavaElement classFile = (JavaElement)getClassFile(classFileName);
			return classFile.getHandleFromMemento(memento, owner);
		case JEM_COMPILATIONUNIT:
			String cuName = memento.nextToken();
			JavaElement cu = new CompilationUnit(this, cuName, owner);
			return cu.getHandleFromMemento(memento, owner);
	}
	return null;
}
/**
 * @see JavaElement#getHandleMementoDelimiter()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_PACKAGEFRAGMENT;
}
/**
 * @see IPackageFragment#getKind()
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
		return ((PackageFragmentInfo) getElementInfo()).getNonJavaResources(getResource(), getPackageFragmentRoot());
	}
}
/**
 * @see IJavaElement#getPath()
 */
public IPath getPath() {
	PackageFragmentRoot root = this.getPackageFragmentRoot();
	if (root.isArchive()) {
		return root.getPath();
	} else {
		return root.getPath().append(this.getElementName().replace('.', '/'));
	}
}
/**
 * @see IJavaElement#getResource()
 */
public IResource getResource() {
	PackageFragmentRoot root = this.getPackageFragmentRoot();
	if (root.isArchive()) {
		return root.getResource();
	} else {
		String elementName = this.getElementName();
		if (elementName.length() == 0) {
			return root.getResource();
		} else {
			return ((IContainer)root.getResource()).getFolder(new Path(this.getElementName().replace('.', '/')));
		}
	}
}
/**
 * @see IJavaElement#getUnderlyingResource()
 */
public IResource getUnderlyingResource() throws JavaModelException {
	IResource rootResource = this.parent.getUnderlyingResource();
	if (rootResource == null) {
		//jar package fragment root that has no associated resource
		return null;
	}
	// the underlying resource may be a folder or a project (in the case that the project folder
	// is atually the package fragment root)
	if (rootResource.getType() == IResource.FOLDER || rootResource.getType() == IResource.PROJECT) {
		IContainer folder = (IContainer) rootResource;
		String[] segs = Signature.getSimpleNames(this.name);
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
 * @see IParent 
 */
public boolean hasChildren() throws JavaModelException {
	return getChildren().length > 0;
}
/**
 * @see IPackageFragment#hasSubpackages()
 */
public boolean hasSubpackages() throws JavaModelException {
	IJavaElement[] packages= ((IPackageFragmentRoot)getParent()).getChildren();
	String elementName = getElementName();
	int nameLength = elementName.length();
	String packageName = isDefaultPackage() ? elementName : elementName+"."; //$NON-NLS-1$
	for (int i= 0; i < packages.length; i++) {
		String otherName = packages[i].getElementName();
		if (otherName.length() > nameLength && otherName.startsWith(packageName)) {
			return true;
		}
	}
	return false;
}
/**
 * @see IPackageFragment#isDefaultPackage()
 */
public boolean isDefaultPackage() {
	return this.getElementName().length() == 0;
}
/**
 * @see ISourceManipulation#move(IJavaElement, IJavaElement, String, boolean, IProgressMonitor)
 */
public void move(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Util.bind("operation.nullContainer")); //$NON-NLS-1$
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
 * @see ISourceManipulation#rename(String, boolean, IProgressMonitor)
 */
public void rename(String newName, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (newName == null) {
		throw new IllegalArgumentException(Util.bind("element.nullName")); //$NON-NLS-1$
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] dests= new IJavaElement[] {this.getParent()};
	String[] renamings= new String[] {newName};
	getJavaModel().rename(elements, dests, renamings, force, monitor);
}
/**
 * Debugging purposes
 */
protected void toStringChildren(int tab, StringBuffer buffer, Object info) {
	if (tab == 0) {
		super.toStringChildren(tab, buffer, info);
	}
}
/**
 * Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	if (getElementName().length() == 0) {
		buffer.append("<default>"); //$NON-NLS-1$
	} else {
		toStringName(buffer);
	}
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else {
		if (tab > 0) {
			buffer.append(" (...)"); //$NON-NLS-1$
		}
	}
}
}
