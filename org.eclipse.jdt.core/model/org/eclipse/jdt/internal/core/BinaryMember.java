package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.*;

/**
 * Common functionality for Binary member handles.
 */
public class BinaryMember extends Member {
/**
 * Constructs a binary member.
 */
protected BinaryMember(int type, IJavaElement parent, String name) {
	super(type, parent, name);
}
/**
 * @see ISourceManipulation
 */
public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see ISourceReference
 */
public ISourceRange getNameRange() {
	SourceMapper mapper= getSourceMapper();
	if (mapper != null) {
		return mapper.getNameRange(this);
	} else {
		return SourceMapper.fgUnknownRange;
	}
}
/**
 * @see ISourceReference
 */
public ISourceRange getSourceRange() throws JavaModelException {
	SourceMapper mapper= getSourceMapper();
	if (mapper != null) {
		return mapper.getSourceRange(this);
	} else {
		return SourceMapper.fgUnknownRange;
	}
}
/**
 * @see IMember
 */
public boolean isBinary() {
	return true;
}
/**
 * @see IJavaElement
 */
public boolean isStructureKnown() throws JavaModelException {
	return ((IJavaElement)getOpenableParent()).isStructureKnown();
}
/**
 * @see ISourceManipulation
 */
public void move(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * Opens this element and all parents that are not already open.
 *
 * @exception NotPresentException this element is not present or accessable
 */
protected void openHierarchy() throws JavaModelException {
	Openable openableParent = (Openable)getOpenableParent();
	if (openableParent != null) {
		JavaElementInfo openableParentInfo = (JavaElementInfo) fgJavaModelManager.getInfo((IJavaElement) openableParent);
		if (openableParentInfo == null) {
			openableParent.openWhenClosed(null);
			openableParentInfo = (JavaElementInfo) fgJavaModelManager.getInfo((IJavaElement) openableParent);
		}
		ClassFileInfo cfi = (ClassFileInfo) openableParentInfo;
		cfi.getBinaryChildren(); // forces the initialization
	}
}
/**
 * @see ISourceManipulation
 */
public void rename(String name, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * Sets the contents of this element.
 * Throws an exception as this element is read only.
 */
public void setContents(String contents, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
}
