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

import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Abstract class for Java elements which implement ISourceReference.
 */
/* package */ abstract class SourceRefElement extends JavaElement implements ISourceReference {
protected SourceRefElement(JavaElement parent, String name) {
	super(parent, name);
}
/**
 * This element is being closed.  Do any necessary cleanup.
 */
protected void closing(Object info) throws JavaModelException {
	// Do any necessary cleanup
}
/**
 * Returns a new element info for this element.
 */
protected Object createElementInfo() {
	return null; // not used for source ref elements
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
 * @see ISourceManipulation
 */
public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	IJavaElement[] elements = new IJavaElement[] {this};
	getJavaModel().delete(elements, force, monitor);
}
/*
 * @see JavaElement#generateInfos
 */
protected void generateInfos(Object info, HashMap newElements, IProgressMonitor pm) throws JavaModelException {
	Openable openableParent = (Openable)getOpenableParent();
	if (openableParent == null) return;

	JavaElementInfo openableParentInfo = (JavaElementInfo) JavaModelManager.getJavaModelManager().getInfo(openableParent);
	if (openableParentInfo == null) {
		openableParent.generateInfos(openableParent.createElementInfo(), newElements, pm);
	}
}
/**
 * @see IMember
 */
public ICompilationUnit getCompilationUnit() {
	return ((JavaElement)getParent()).getCompilationUnit();
}
/**
 * Elements within compilation units and class files have no
 * corresponding resource.
 *
 * @see IJavaElement
 */
public IResource getCorrespondingResource() throws JavaModelException {
	if (!exists()) throw newNotPresentException();
	return null;
}
/*
 * @see JavaElement
 */
public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
	switch (token.charAt(0)) {
		case JEM_COUNT:
			return getHandleUpdatingCountFromMemento(memento, workingCopyOwner);
	}
	return this;
}
/**
 * Return the first instance of IOpenable in the hierarchy of this
 * type (going up the hierarchy from this type);
 */
public IOpenable getOpenableParent() {
	IJavaElement current = getParent();
	while (current != null){
		if (current instanceof IOpenable){
			return (IOpenable) current;
		}
		current = current.getParent();
	}
	return null;
}
/*
 * @see IJavaElement
 */
public IPath getPath() {
	return this.getParent().getPath();
}
/*
 * @see IJavaElement
 */
public IResource getResource() {
	return this.getParent().getResource();
}
/**
 * @see ISourceReference
 */
public String getSource() throws JavaModelException {
	IOpenable openable = getOpenableParent();
	IBuffer buffer = openable.getBuffer();
	if (buffer == null) {
		return null;
	}
	ISourceRange range = getSourceRange();
	int offset = range.getOffset();
	int length = range.getLength();
	if (offset == -1 || length == 0 ) {
		return null;
	}
	try {
		return buffer.getText(offset, length);
	} catch(RuntimeException e) {
		return null;
	}
}
/**
 * @see ISourceReference
 */
public ISourceRange getSourceRange() throws JavaModelException {
	SourceRefElementInfo info = (SourceRefElementInfo) getElementInfo();
	return info.getSourceRange();
}
/**
 * @see IJavaElement
 */
public IResource getUnderlyingResource() throws JavaModelException {
	if (!exists()) throw newNotPresentException();
	return getParent().getUnderlyingResource();
}
/**
 * @see IParent 
 */
public boolean hasChildren() throws JavaModelException {
	return getChildren().length > 0;
}
/**
 * @see ISourceManipulation
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
 * @see ISourceManipulation
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
}
