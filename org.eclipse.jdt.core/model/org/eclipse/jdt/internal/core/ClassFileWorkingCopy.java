package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferFactory;
import org.eclipse.jdt.core.ICodeCompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A working copy on an <code>IClassFile</code>.
 * Only the <code>getBuffer()</code> operation is valid.
 * All other operations return either <code>null</code> or throw a <code>JavaModelException</code>.
 */
public class ClassFileWorkingCopy implements ICompilationUnit {
	
	public IBuffer buffer;
	

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
	public boolean isStructureKnown() throws JavaModelException {
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
	}

	/*
	 * @see IOpenable#getBuffer()
	 */
	public IBuffer getBuffer() throws JavaModelException {
		return this.buffer;
	}

	/*
	 * @see IOpenable#hasUnsavedChanges()
	 */
	public boolean hasUnsavedChanges() throws JavaModelException {
		return false;
	}

	/*
	 * @see IOpenable#isConsistent()
	 */
	public boolean isConsistent() throws JavaModelException {
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

	/*
	 * @see IWorkingCopy#commit(boolean, IProgressMonitor)
	 */
	public void commit(boolean force, IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IWorkingCopy#destroy()
	 */
	public void destroy() {
	}

	/*
	 * @see IWorkingCopy#findSharedWorkingCopy()
	 */
	public IJavaElement findSharedWorkingCopy() {
		return null;
	}

	/*
	 * @see IWorkingCopy#getOriginal(IJavaElement)
	 */
	public IJavaElement getOriginal(IJavaElement workingCopyElement) {
		return null;
	}

	/*
	 * @see IWorkingCopy#getOriginalElement()
	 */
	public IJavaElement getOriginalElement() {
		return null;
	}

	/*
	 * @see IWorkingCopy#getSharedWorkingCopy(IProgressMonitor, IBufferFactory)
	 */
	public IJavaElement getSharedWorkingCopy(
		IProgressMonitor monitor,
		IBufferFactory factory)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IWorkingCopy#getWorkingCopy()
	 */
	public IJavaElement getWorkingCopy() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IWorkingCopy#getWorkingCopy(IProgressMonitor, IBufferFactory)
	 */
	public IJavaElement getWorkingCopy(
		IProgressMonitor monitor,
		IBufferFactory factory)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IWorkingCopy#hasManagedWorkingCopy()
	 */

	/*
	 * @see IWorkingCopy#isBasedOn(IResource)
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

	/*
	 * @see IWorkingCopy#reconcile()
	 */
	public IMarker[] reconcile() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IWorkingCopy#reconcile()
	 */
	 public void reconcile(IProblemRequestor problemRequestor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IWorkingCopy#restore()
	 */
	public void restore() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ISourceManipulation#copy(IJavaElement, IJavaElement, String, boolean, IProgressMonitor)
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

	/*
	 * @see ISourceManipulation#delete(boolean, IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ISourceManipulation#move(IJavaElement, IJavaElement, String, boolean, IProgressMonitor)
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

	/*
	 * @see ISourceManipulation#rename(String, boolean, IProgressMonitor)
	 */
	public void rename(String name, boolean replace, IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICodeAssist#codeComplete(int, ICompletionRequestor)
	 */
	public void codeComplete(int offset, ICompletionRequestor requestor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICodeAssist#codeSelect(int, int)
	 */
	public IJavaElement[] codeSelect(int offset, int length)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see ICodeAssist#codeComplete(int, ICodeCompletionRequestor)
	 */
	public void codeComplete(int offset, ICodeCompletionRequestor requestor)
		throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

}
