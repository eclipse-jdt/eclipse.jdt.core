package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;
import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.internal.codeassist.SelectionEngine;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


/**
 * Abstract class for implementations of java elements which are IOpenable.
 *
 * @see IJavaElement, IOpenable
 */
public abstract class Openable extends JavaElement implements IOpenable, IBufferChangedListener {

protected Openable(int type, IJavaElement parent, String name) {
	super(type, parent, name);
}
	/**
	 * The buffer associated with this element has changed. Registers
	 * this element as being out of synch with its buffer's contents.
	 * If the buffer has been closed, this element is set as NOT out of
	 * synch with the contents.
	 *
	 * @see IBufferChangedListener
	 */
	public void bufferChanged(BufferChangedEvent event) {
		if (event.getBuffer().isClosed()) {
			fgJavaModelManager.getElementsOutOfSynchWithBuffers().remove(this);
		} else {
			fgJavaModelManager.getElementsOutOfSynchWithBuffers().put(this, this);
		}
	}
/**
 * Updates the info objects for this element and all of its children by
 * removing the current infos, generating new infos, and then placing
 * the new infos into the Java Model cache tables.
 */
protected void buildStructure(OpenableElementInfo info, IProgressMonitor pm) throws JavaModelException {

	// remove existing (old) infos
	removeInfo();
	HashMap newElements = new HashMap(11);
	info.setIsStructureKnown(generateInfos(info, pm, newElements, getUnderlyingResource()));
	fgJavaModelManager.getElementsOutOfSynchWithBuffers().remove(this);
	for (Iterator iter = newElements.keySet().iterator(); iter.hasNext();) {
		IJavaElement key = (IJavaElement) iter.next();
		Object value = newElements.get(key);
		fgJavaModelManager.putInfo(key, value);
	}
	// add the info for this at the end, to ensure that a getInfo cannot reply null in case the LRU cache needs
	// to be flushed. Might lead to performance issues.
	// see PR 1G2K5S7: ITPJCORE:ALL - NPE when accessing source for a binary type
	fgJavaModelManager.putInfo(this, info);	
}
/**
 * Close the buffer associated with this element, if any.
 */
protected void closeBuffer(OpenableElementInfo info) {
	IBuffer buffer = null;
	buffer = getBufferManager().getBuffer(this);
	if (buffer != null) {
		buffer.removeBufferChangedListener(this);
		buffer.close();
		fgJavaModelManager.getElementsOutOfSynchWithBuffers().remove(this);
	}
}
/**
 * This element is being closed.  Do any necessary cleanup.
 */
protected void closing(Object info) throws JavaModelException {
	OpenableElementInfo openableInfo = (OpenableElementInfo) info;
	closeBuffer(openableInfo);
	super.closing(info);
}
/**
 * @see ICodeAssist
 */
protected void codeComplete(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu, org.eclipse.jdt.internal.compiler.env.ICompilationUnit unitToSkip, int position, ICompletionRequestor requestor) throws JavaModelException {
	if (requestor == null) {
		throw new IllegalArgumentException(Util.bind("codeAssist.nullRequestor")); //$NON-NLS-1$
	}
	IBuffer buffer = getBuffer();
	if (buffer == null) {
		return;
	}
	if (position < -1 || position > buffer.getLength()) {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS));
	}
	SearchableEnvironment environment = (SearchableEnvironment) ((JavaProject) getJavaProject()).getSearchableNameEnvironment();
	NameLookup nameLookup = ((JavaProject) getJavaProject()).getNameLookup();
	environment.unitToSkip = unitToSkip;

	CompletionEngine engine = new CompletionEngine(environment, new CompletionRequestorWrapper(requestor,nameLookup), JavaCore.getOptions());
	engine.complete(cu, position);
	environment.unitToSkip = null;
}
/**
 * @see ICodeAssist
 */
protected IJavaElement[] codeSelect(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu, int offset, int length) throws JavaModelException {
	SelectionRequestor requestor= new SelectionRequestor(((JavaProject)getJavaProject()).getNameLookup(), this);
	this.codeSelect(cu, offset, length, requestor);
	return requestor.getElements();
}
/**
 * @see ICodeAssist
 */
protected void codeSelect(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu, int offset, int length, ISelectionRequestor requestor) throws JavaModelException {

	// fix for 1FWNMHH, part (1)
	if (length == 0) {
		return;
	}
	
	IBuffer buffer = getBuffer();
	if (buffer == null) {
		return;
	}
	int end= buffer.getLength();
	if (offset < 0 || length < 0 || offset + length > end ) {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS));
	}

	// fix for 1FVGGKF
	ISearchableNameEnvironment environment = ((JavaProject)getJavaProject()).getSearchableNameEnvironment();
	
	// fix for 1FVXGDK
	SelectionEngine engine = new SelectionEngine(environment, requestor, JavaCore.getOptions());
	engine.select(cu, offset, offset + length - 1);
}
/**
 * Returns a new element info for this element.
 */
protected OpenableElementInfo createElementInfo() {
	return new OpenableElementInfo();
}

/**
 * Builds this element's structure and properties in the given
 * info object, based on this element's current contents (i.e. buffer
 * contents if this element has an open buffer, or resource contents
 * if this element does not have an open buffer). Children
 * are placed in the given newElements table (note, this element
 * has already been placed in the newElements table). Returns true
 * if successful, or false if an error is encountered while determining
 * the structure of this element.
 */
protected abstract boolean generateInfos(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException;
/**
 * Note: a buffer with no unsaved changes can be closed by the Java Model
 * since it has a finite number of buffers allowed open at one time. If this
 * is the first time a request is being made for the buffer, an attempt is
 * made to create and fill this element's buffer. If the buffer has been
 * closed since it was first opened, the buffer is re-created.
 * 
 * @see IOpenable
 */
public IBuffer getBuffer() throws JavaModelException {
	if (hasBuffer()) {
		// ensure element is open
		if (!isOpen()) {
			getElementInfo();
		}
		IBuffer buffer = getBufferManager().getBuffer(this);
		if (buffer == null) {
			// try to (re)open a buffer
			buffer = openBuffer(null);
		}
		return buffer;
	} else {
		return null;
	}
}
/**
 * Returns the buffer manager for this element.
 */
protected BufferManager getBufferManager() {
	return BufferManager.getDefaultBufferManager();
}
/**
 * Return my underlying resource. Elements that may not have a
 * corresponding resource must override this method.
 *
 * @see IJavaElement
 */
public IResource getCorrespondingResource() throws JavaModelException {
	return getUnderlyingResource();
}

/**
 * Answer the path of the corresponding resource or associated file (JARs)
 */
public IPath getPath(){
	try {
		IResource resource = this.getCorrespondingResource();
		if (resource != null){
			return resource.getFullPath();
		}
	} catch(JavaModelException e){
	}
	return null;
}

/**
 * @see IJavaElement
 */
public IResource getUnderlyingResource() throws JavaModelException {
	IResource parentResource = fParent.getUnderlyingResource();
	if (parentResource == null) {
		return null;
	}
	int type = parentResource.getType();
	if (type == IResource.FOLDER || type == IResource.PROJECT) {
		IContainer folder = (IContainer) parentResource;
		IResource resource = folder.findMember(fName);
		if (resource == null) {
			throw newNotPresentException();
		} else {
			return resource;
		}
	} else {
		return parentResource;
	}
}

public boolean exists(){
	
	IPath path = this.getPath();
	if (path != null){
		return parentExists() 
					&& resourceExists();
	}
	return super.exists();
}	

/**
 * Returns true if this element may have an associated source buffer,
 * otherwise false. Subclasses must override as required.
 */
protected boolean hasBuffer() {
	return false;
}
/**
 * @see IParent 
 */
public boolean hasChildren() throws JavaModelException {
	return getChildren().length > 0;
}
/**
 * @see IOpenable
 */
public boolean hasUnsavedChanges() throws JavaModelException{
	
	if (isReadOnly() || !isOpen()) {
		return false;
	}
	if (getBuffer() != null && getBuffer().hasUnsavedChanges()) {
		return true;
	}
	// for package fragments, package fragment roots, and projects must check open buffers
	// to see if they have an child with unsaved changes
	if (fLEType == PACKAGE_FRAGMENT ||
		fLEType == PACKAGE_FRAGMENT_ROOT ||
		fLEType == JAVA_PROJECT ||
		fLEType == JAVA_MODEL) { // fix for 1FWNMHH, part (2)
		Enumeration openBuffers= getBufferManager().getOpenBuffers();
		while (openBuffers.hasMoreElements()) {
			IBuffer buffer= (IBuffer)openBuffers.nextElement();
			if (buffer.hasUnsavedChanges()) {
				IJavaElement owner= (IJavaElement)buffer.getOwner();
				if (isAncestorOf(owner)) {
					return true;
				}
			}
		}
	}
	
	return false;
}
/**
 * Subclasses must override as required.
 *
 * @see IOpenable
 */
public boolean isConsistent() throws JavaModelException {
	return true;
}
/**
 * 
 * @see IOpenable
 */
public boolean isOpen() {
	return fgJavaModelManager.getInfo(this) != null;
}
/**
 * Returns true if this represents a source element.
 * Openable source elements have an associated buffer created
 * when they are opened.
 */
protected boolean isSourceElement() {
	return false;
}
/**
 * @see IOpenable
 */
public void makeConsistent(IProgressMonitor pm) throws JavaModelException {
	if (!isConsistent()) {
		buildStructure((OpenableElementInfo)getElementInfo(), pm);
	}
}
/**
 * @see IOpenable
 */
public void open(IProgressMonitor pm) throws JavaModelException {
	this.open(pm, null);
}
/**
 * Opens this openable using the given buffer (or null if one should be created)
 */
protected void open(IProgressMonitor pm, IBuffer buffer) throws JavaModelException {
	if (!isOpen()) {
		this.openWhenClosed(pm, buffer);
	}
}
/**
 * Opens a buffer on the contents of this element, and returns
 * the buffer, or returns <code>null</code> if opening fails.
 * By default, do nothing - subclasses that have buffers
 * must override as required.
 */
protected IBuffer openBuffer(IProgressMonitor pm) throws JavaModelException {
	return null;
}

/**
 * 	Open the parent element if necessary
 * 
 */
protected void openParent(IProgressMonitor pm) throws JavaModelException {

	Openable openableParent = (Openable)getOpenableParent();
	if (openableParent != null) {
		OpenableElementInfo openableParentInfo = (OpenableElementInfo) fgJavaModelManager.getInfo((IJavaElement) openableParent);
		if (openableParentInfo == null) {
			openableParent.openWhenClosed(pm, null);
		}
	}
}

/**
 * Open an <code>Openable</code> that is known to be closed (no check for <code>isOpen()</code>).
 * Use the given buffer to get the source, or open a new one if null.
 */
protected void openWhenClosed(IProgressMonitor pm, IBuffer buffer) throws JavaModelException {
	try {
		
		if (JavaModelManager.VERBOSE){
			System.out.println("OPENING Element ("+ Thread.currentThread()+"): " + this.getHandleIdentifier()); //$NON-NLS-1$//$NON-NLS-2$
		}
		
		// 1) Parent must be open - open the parent if necessary
		openParent(pm);
		
		// 1.5) Ensure my resource is local
		IResource resource = getCorrespondingResource();
		if (resource != null) {
			try {
				JavaModelManager.getJavaModelManager().ensureLocal(resource);
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
		}

		// 2) create the new element info and open a buffer if needed
		OpenableElementInfo info = createElementInfo();
		if (buffer == null) {
			if (resource != null && isSourceElement()) {
				this.openBuffer(pm);
			} 
		} else {
			this.getBufferManager().addBuffer(buffer);
			buffer.addBufferChangedListener(this);
		}

		// 3) build the structure of the openable
		buildStructure(info, pm);

		// 4) anything special
		opening(info);

		// if any problems occuring openning the element, ensure that it's info
		// does not remain in the cache	(some elements, pre-cache their info
		// as they are being opened).
	} catch (JavaModelException e) {
		fgJavaModelManager.removeInfo(this);
		throw e;
	}
}

/**
 *  Answers true if the parent exists (null parent is answering true)
 * 
 */
protected boolean parentExists(){
	
	IJavaElement parent = this.getParent();
	if (parent == null) return true;
	return parent.exists();
}

/**
 * Returns whether the corresponding resource or associated file exists
 */
protected boolean resourceExists() {
	
	return JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), this.getPath(), true) != null;
}

/**
 * @see IOpenable
 */
public void save(IProgressMonitor pm, boolean force) throws JavaModelException {
	if (isReadOnly()) {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}
	IBuffer buf = getBuffer();
	if (buf != null) { // some Openables (like a JavaProject) don't have a buffer
		buf.save(pm, force);
		this.makeConsistent(pm); // update the element info of this element
	}
}

/**
 * Find enclosing package fragment root if any
 */
public PackageFragmentRoot getPackageFragmentRoot() {
	IJavaElement current = this;
	do {
		if (current instanceof PackageFragmentRoot) return (PackageFragmentRoot)current;
		current = current.getParent();
	} while(current != null);
	return null;
}
/**
 * @see ICodeAssist
 * @deprecated - use codeComplete(ICompilationUnit, ICompilationUnit, int, ICompletionRequestor) instead
 */
protected void codeComplete(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu, org.eclipse.jdt.internal.compiler.env.ICompilationUnit unitToSkip, int position, final ICodeCompletionRequestor requestor) throws JavaModelException {

	if (requestor == null){
		codeComplete(cu, unitToSkip, position, (ICompletionRequestor)null);
		return;
	}
	codeComplete(
		cu,
		unitToSkip,
		position,
		new ICompletionRequestor(){
			public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd) {
				requestor.acceptClass(packageName, className, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptError(IMarker marker) {
				requestor.acceptError(marker);
			}
			public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
				requestor.acceptField(declaringTypePackageName, declaringTypeName, name, typePackageName, typeName, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptInterface(char[] packageName,char[] interfaceName,char[] completionName,int modifiers,int completionStart,int completionEnd) {
				requestor.acceptInterface(packageName, interfaceName, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptKeyword(char[] keywordName,int completionStart,int completionEnd){
				requestor.acceptKeyword(keywordName, completionStart, completionEnd);
			}
			public void acceptLabel(char[] labelName,int completionStart,int completionEnd){
				requestor.acceptLabel(labelName, completionStart, completionEnd);
			}
			public void acceptLocalVariable(char[] name,char[] typePackageName,char[] typeName,int modifiers,int completionStart,int completionEnd){
				// ignore
			}
			public void acceptMethod(char[] declaringTypePackageName,char[] declaringTypeName,char[] selector,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] returnTypePackageName,char[] returnTypeName,char[] completionName,int modifiers,int completionStart,int completionEnd){
				// skip parameter names
				requestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptMethodDeclaration(char[] declaringTypePackageName,char[] declaringTypeName,char[] selector,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] returnTypePackageName,char[] returnTypeName,char[] completionName,int modifiers,int completionStart,int completionEnd){
				// ignore
			}
			public void acceptModifier(char[] modifierName,int completionStart,int completionEnd){
				requestor.acceptModifier(modifierName, completionStart, completionEnd);
			}
			public void acceptPackage(char[] packageName,char[] completionName,int completionStart,int completionEnd){
				requestor.acceptPackage(packageName, completionName, completionStart, completionEnd);
			}
			public void acceptType(char[] packageName,char[] typeName,char[] completionName,int completionStart,int completionEnd){
				requestor.acceptType(packageName, typeName, completionName, completionStart, completionEnd);
			}
			public void acceptVariableName(char[] typePackageName,char[] typeName,char[] name,char[] completionName,int completionStart,int completionEnd){
				// ignore
			}
		});
}

}
