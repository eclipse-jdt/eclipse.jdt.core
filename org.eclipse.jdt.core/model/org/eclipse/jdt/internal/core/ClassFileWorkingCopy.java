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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * A working copy on an <code>IClassFile</code>.
 * Only the <code>getBuffer()</code> and <code>getOriginalElement()</code> operations are valid.
 * All other operations return either <code>null</code> or throw a <code>JavaModelException</code>.
 */
public class ClassFileWorkingCopy implements ICompilationUnit {
	
	public IBuffer buffer;
	
	/*
	 * @see ICompilationUnit#becomeWorkingCopy(IProblemRequestor, IProgressMonitor)
	 */
	public void becomeWorkingCopy(IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}
	
	/*
	 * @see ICompilationUnit#createImport(String, IJavaElement, IProgressMonitor)
	 */
	public IImportDeclaration createImport(
		String name,
		IJavaElement sibling,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICompilationUnit#createImport(String, IJavaElement, int, IProgressMonitor)
     * @since 3.0
	 */
	public IImportDeclaration createImport(
		String name,
		IJavaElement sibling,
		int flags,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICompilationUnit#createPackageDeclaration(String, IProgressMonitor)
	 */
	public IPackageDeclaration createPackageDeclaration(
		String name,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICompilationUnit#createType(String, IJavaElement, boolean, IProgressMonitor)
	 */
	public IType createType(
		String contents,
		IJavaElement sibling,
		boolean force,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICompilationUnit#discardWorkingCopy
	 */
	public void discardWorkingCopy() throws JavaModelException {
		// not a real working copy: ignore
	}
	
	/*
	 * @see ICompilationUnit#getAllTypes()
	 */
	public IType[] getAllTypes() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICompilationUnit#getElementAt(int)
	 */
	public IJavaElement getElementAt(int position) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICompilationUnit#getImport(String)
	 */
	public IImportDeclaration getImport(String name) {
		return null;
	}

	/*
	 * @see ICompilationUnit#getImportContainer()
	 */
	public IImportContainer getImportContainer() {
		return null;
	}

	/*
	 * @see ICompilationUnit#getImports()
	 */
	public IImportDeclaration[] getImports() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IJavaElement#getOpenable()
	 */
	public IOpenable getOpenable() {
		return null;
	}

	/*
	 * @see ICompilationUnit#getOwner()
	 */
	public WorkingCopyOwner getOwner() {
		return null;
	}

	/*
	 * @see ICompilationUnit#getPackageDeclaration(String)
	 */
	public IPackageDeclaration getPackageDeclaration(String name) {
		return null;
	}

	/*
	 * @see ICompilationUnit#getPackageDeclarations()
	 */
	public IPackageDeclaration[] getPackageDeclarations()
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICompilationUnit#getType(String)
	 */
	public IType getType(String name) {
		return null;
	}

	/*
	 * @see ICompilationUnit#getTypes()
	 */
	public IType[] getTypes() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IJavaElement#exists()
	 */
	public boolean exists() {
		return false;
	}
/*
 * @see IWorkingCopy
 */
public IJavaElement[] findElements(IJavaElement element) {
	return null;
}
/*
 * @see IWorkingCopy
 */
public IType findPrimaryType() {
	return null;
}

	/*
	 * @see IJavaElement#getCorrespondingResource()
	 */
	public IResource getCorrespondingResource() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IJavaElement#getElementName()
	 */
	public String getElementName() {
		return null;
	}

	/*
	 * @see IJavaElement#getElementType()
	 */
	public int getElementType() {
		return 0;
	}

	/*
	 * @see IJavaElement#getHandleIdentifier()
	 */
	public String getHandleIdentifier() {
		return null;
	}

	/*
	 * @see IJavaElement#getJavaModel()
	 */
	public IJavaModel getJavaModel() {
		return null;
	}

	/*
	 * @see IJavaElement#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		return null;
	}

	/*
	 * @see IJavaElement#getParent()
	 */
	public IJavaElement getParent() {
		return null;
	}

	/*
	 * @see IJavaElement
	 */
	public IPath getPath() {
		return null;
	}

	/*
	 * @see ICompilationUnit#getPrimary()
	 */
	public ICompilationUnit getPrimary() {
		return this;
	}
	
	/*
	 * @see ICompilationUnit#getPrimaryElement()
	 */
	public IJavaElement getPrimaryElement() {
		return getPrimary();
	}
	
	/*
	 * @see IJavaElement
	 */
	public IResource getResource() {
		return null;
	}

	/*
	 * @see IJavaElement
	 */
	public ISchedulingRule getSchedulingRule() {
		return null;
	}
	
	/*
	 * @see IJavaElement#getUnderlyingResource()
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IJavaElement#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	/*
	 * @see IJavaElement#isStructureKnown()
	 */
	public boolean isStructureKnown() {
		return false;
	}

	/*
	 * @see ISourceReference#getSource()
	 */
	public String getSource() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ISourceReference#getSourceRange()
	 */
	public ISourceRange getSourceRange() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IParent#getChildren()
	 */
	public IJavaElement[] getChildren() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IParent#hasChildren()
	 */
	public boolean hasChildren() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IOpenable#close()
	 */
	public void close() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IOpenable#getBuffer()
	 */
	public IBuffer getBuffer() {
		return this.buffer;
	}

	/*
	 * @see ICompilationUnit#hasResourceChanged()
	 */
	public boolean hasResourceChanged() {
		return false;
	}
	
	/*
	 * @see IOpenable#hasUnsavedChanges()
	 */
	public boolean hasUnsavedChanges() {
		return false;
	}

	/*
	 * @see IOpenable#isConsistent()
	 */
	public boolean isConsistent() {
		return false;
	}

	/*
	 * @see IOpenable#isOpen()
	 */
	public boolean isOpen() {
		return false;
	}

	/*
	 * @see IOpenable#makeConsistent(IProgressMonitor)
	 */
	public void makeConsistent(IProgressMonitor progress)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IOpenable#open(IProgressMonitor)
	 */
	public void open(IProgressMonitor progress) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IOpenable#save(IProgressMonitor, boolean)
	 */
	public void save(IProgressMonitor progress, boolean force)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see IWorkingCopy#commit(boolean, IProgressMonitor)
	 * @deprecated
	 */
	public void commit(boolean force, IProgressMonitor monitor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}
	
	/*
	 * @see IWorkingCopy#commitWorkingCopy(boolean, IProgressMonitor)
	 */
	public void commitWorkingCopy(boolean force, IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see IWorkingCopy#destroy()
	 * @deprecated
	 */
	public void destroy() {
		// not a real working copy: ignore
	}

	/**
	 * @see IWorkingCopy#findSharedWorkingCopy(IBufferFactory)
	 * @deprecated
	 */
	public IJavaElement findSharedWorkingCopy(IBufferFactory bufferFactory) {
		return null;
	}
	
	/**
	 * @see ICompilationUnit#findWorkingCopy(WorkingCopyOwner)
	 */
	public ICompilationUnit findWorkingCopy(WorkingCopyOwner owner) {
		return null;
	}

	/**
	 * @see IWorkingCopy#getOriginal(IJavaElement)
	 * @deprecated
	 */
	public IJavaElement getOriginal(IJavaElement workingCopyElement) {
		return null;
	}

	/**
	 * @see IWorkingCopy#getOriginalElement()
	 * @deprecated
	 */
	public IJavaElement getOriginalElement() {
		return getPrimaryElement();
	}

	/**
	 * @see IWorkingCopy#getSharedWorkingCopy(IProgressMonitor, IBufferFactory, IProblemRequestor)
	 * @deprecated
	 */
	public IJavaElement getSharedWorkingCopy(
		IProgressMonitor monitor,
		IBufferFactory factory,
		IProblemRequestor problemRequestor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see IWorkingCopy#getWorkingCopy()
	 * @deprecated
	 */
	public IJavaElement getWorkingCopy() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}
	
	/**
	 * @see ICompilationUnit#getWorkingCopy(IProgressMonitor)
	 */
	public ICompilationUnit getWorkingCopy(IProgressMonitor monitor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see IWorkingCopy#getWorkingCopy(IProgressMonitor, IBufferFactory, IProblemRequestor)
	 * @deprecated
	 */
	public IJavaElement getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompilationUnit#getWorkingCopy(org.eclipse.jdt.core.WorkingCopyOwner, org.eclipse.jdt.core.IProblemRequestor, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICompilationUnit getWorkingCopy(WorkingCopyOwner owner, IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see IWorkingCopy#isBasedOn(IResource)
	 * @deprecated
	 */
	public boolean isBasedOn(IResource resource) {
		return false;
	}

	/*
	 * @see IWorkingCopy#isWorkingCopy()
	 */
	public boolean isWorkingCopy() {
		return true;
	}

	/**
	 * @see org.eclipse.jdt.core.IWorkingCopy#reconcile()
	 * @deprecated
	 */
	public IMarker[] reconcile() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see ICompilationUnit#reconcile(int, boolean, WorkingCopyOwner, IProgressMonitor)
	 * @since 3.0
	 */
	public CompilationUnit reconcile(
		int astLevel,
		boolean forceProblemDetection,
		WorkingCopyOwner owner,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IWorkingCopy#reconcile(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void reconcile(
		boolean forceProblemDetection,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IWorkingCopy#restore()
	 */
	public void restore() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see org.eclipse.jdt.core.ISourceManipulation#copy(IJavaElement, IJavaElement, String, boolean, IProgressMonitor)
	 */
	public void copy(
		IJavaElement container,
		IJavaElement sibling,
		String rename,
		boolean replace,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see org.eclipse.jdt.core.ISourceManipulation#delete(boolean, IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see org.eclipse.jdt.core.ISourceManipulation#move(IJavaElement, IJavaElement, String, boolean, IProgressMonitor)
	 */
	public void move(
		IJavaElement container,
		IJavaElement sibling,
		String rename,
		boolean replace,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see org.eclipse.jdt.core.ISourceManipulation#rename(String, boolean, IProgressMonitor)
	 */
	public void rename(String name, boolean replace, IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, ICompletionRequestor)
	 */
	public void codeComplete(int offset, ICompletionRequestor requestor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, ICompletionRequestor, WorkingCopyOwner)
	 */
	public void codeComplete(int offset, ICompletionRequestor requestor, WorkingCopyOwner owner)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see org.eclipse.jdt.core.ICodeAssist#codeSelect(int, int)
	 */
	public IJavaElement[] codeSelect(int offset, int length)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see org.eclipse.jdt.core.ICodeAssist#codeSelect(int, int, WorkingCopyOwner)
	 */
	public IJavaElement[] codeSelect(int offset, int length, WorkingCopyOwner owner)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, ICodeCompletionRequestor)
	 * @deprecated
	 */
	public void codeComplete(int offset, org.eclipse.jdt.core.ICodeCompletionRequestor requestor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor)
	 */
	public void codeComplete(int offset, CompletionRequestor requestor) throws JavaModelException {
		// TODO (jerome) - Missing implementation
		throw new RuntimeException("Not implemented yet");  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor, org.eclipse.jdt.core.WorkingCopyOwner)
	 */
	public void codeComplete(int offset, CompletionRequestor requestor, WorkingCopyOwner wcowner) throws JavaModelException {
		// TODO (jerome) - Missing implementation
		throw new RuntimeException("Not implemented yet");  //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * @see IJavaElement#getAncestor(int)
	 */
	public IJavaElement getAncestor(int ancestorType) {
		return null;
	}

}
