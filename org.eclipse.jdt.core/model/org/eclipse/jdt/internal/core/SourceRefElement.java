package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.*;

/**
 * Abstract class for Java elements which implement ISourceReference.
 */

/* package */ abstract class SourceRefElement extends JavaElement implements ISourceReference {

protected SourceRefElement(int type, IJavaElement parent, String name) {
	super(type, parent, name);
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
 * @see ISourceManipulation
 */
public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	IJavaElement[] elements = new IJavaElement[] {this};
	getJavaModel().delete(elements, force, monitor);
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
	return null;
}
/**
 * Return the first instance of IOpenable in the hierarchy of this
 * type (going up the hierarchy from this type);
 */
public IOpenable getOpenableParent() {
	IJavaElement parent = getParent();
	if (parent instanceof IOpenable)
		return (IOpenable) parent;
	return ((JavaElement) parent).getOpenableParent();
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
	return buffer.getText(offset, length);
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
	return getParent().getUnderlyingResource();
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
 * Changes the source end index of this element, all children (following
 * <code>child</code>), and all following elements.
 */
public void offsetSourceEndAndChildren(int amount, IJavaElement child) {
	try {
		SourceRefElementInfo info = (SourceRefElementInfo) getRawInfo();
		info.setSourceRangeEnd(info.getDeclarationSourceEnd() + amount);
		IJavaElement[] children = getChildren();
		boolean afterChild = false;
		for (int i = 0; i < children.length; i++) {
			IJavaElement aChild = children[i];
			if (afterChild) {
				((JavaElement) aChild).offsetSourceRange(amount);
			} else {
				afterChild = aChild.equals(child);
			}
		}
		((JavaElement) getParent()).offsetSourceEndAndChildren(amount, this);
	} catch (JavaModelException npe) {
		return;
	}
}
/**
 * Changes the source indexes of this element and all children elements.
 */
public void offsetSourceRange(int amount) {
	try {
		SourceRefElementInfo info = (SourceRefElementInfo) getRawInfo();
		info.setSourceRangeStart(info.getDeclarationSourceStart() + amount);
		info.setSourceRangeEnd(info.getDeclarationSourceEnd() + amount);
		IJavaElement[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			IJavaElement aChild = children[i];
			((JavaElement) aChild).offsetSourceRange(amount);
		}
	} catch (JavaModelException npe) {
		return;
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
 * Updates the end source index for this element, and all following elements.
 */
public void triggerSourceEndOffset(int amount, int nameStart, int nameEnd) {
	try {
		SourceRefElementInfo info = (SourceRefElementInfo) getRawInfo();
		info.setSourceRangeEnd(info.getDeclarationSourceEnd() + amount);
		((JavaElement) getParent()).offsetSourceEndAndChildren(amount, this);
	} catch (JavaModelException npe) {
		return;
	}
}
/**
 * Updates the source indexes of this element and all following elements.
 */
public void triggerSourceRangeOffset(int amount, int nameStart, int nameEnd) {
	try {
		SourceRefElementInfo info = (SourceRefElementInfo) getRawInfo();
		info.setSourceRangeStart(info.getDeclarationSourceStart() + amount);
		info.setSourceRangeEnd(info.getDeclarationSourceEnd() + amount);
		((JavaElement) getParent()).offsetSourceEndAndChildren(amount, this);
	} catch (JavaModelException npe) {
		return;
	}
}
}
