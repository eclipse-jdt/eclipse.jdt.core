package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.*;

/**
 * Implementation of a working copy compilation unit. A working
 * copy maintains the timestamp of the resource it was created
 * from.
 */

public class WorkingCopy extends CompilationUnit {

	/**
	 * If set, this is the factory that will be used to create the buffer.
	 */
	protected IBufferFactory bufferFactory;

	/**
	 * If set, this is the problem requestor which will be used to notify problems
	 * detected during reconciling.
	 */
	protected IProblemRequestor problemRequestor;
		
	/**
	 * A counter of the number of time clients have asked for this 
	 * working copy. It is set to 1, if the working
	 * copy is not managed. When destroyed, this counter is
	 * set to 0. Once destroyed, this working copy cannot be opened
	 * and non-handle info can not be accessed. This is
	 * never true if this compilation unit is not a working
	 * copy.
	 */
	protected int useCount = 0;
/**
 */
protected WorkingCopy(IPackageFragment parent, String name, IBufferFactory bufferFactory) {
	this(parent, name, bufferFactory, null);
}
/**
 */
protected WorkingCopy(IPackageFragment parent, String name, IBufferFactory bufferFactory, IProblemRequestor problemRequestor) {
	super(parent, name);
	this.bufferFactory = bufferFactory;
	this.problemRequestor = problemRequestor;
	this.useCount = 1;
}
/**
 * @see IWorkingCopy
 */
public void commit(boolean force, IProgressMonitor monitor) throws JavaModelException {
	CommitWorkingCopyOperation op= new CommitWorkingCopyOperation(this, force);
	runOperation(op, monitor);
}
/**
 * Returns a new element info for this element.
 */
protected OpenableElementInfo createElementInfo() {
	return new WorkingCopyElementInfo();
}
/**
 * @see IWorkingCopy
 */
public void destroy() {
	if (--this.useCount > 0) {
		if (SHARED_WC_VERBOSE) {
			System.out.println("Decrementing use count of shared working copy " + this.toDebugString());//$NON-NLS-1$
		}
		return;
	}
	try {
		close();
		
		// if original element is not on classpath flush it from the cache 
		IJavaElement originalElement = this.getOriginalElement();
		if (!this.getParent().exists()) {
			((CompilationUnit)originalElement).close();
		}
		
		// remove working copy from the cache
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		if (manager.sharedWorkingCopies.remove(originalElement) != null) {
			if (SHARED_WC_VERBOSE) {
				System.out.println("Destroying shared working copy " + this.toDebugString());//$NON-NLS-1$
			}

			// report removed java delta
			JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
			delta.removed(this);
			manager.fire(delta, JavaModelManager.DEFAULT_CHANGE_EVENT);
		}
		
	} catch (JavaModelException e) {
		// do nothing
	}
}
/**
 * Working copies must be identical to be equal.
 *
 * @see Object#equals
 */
public boolean equals(Object o) {
	return this == o; 
}


/**
 * @see IWorkingCopy
 */
public IJavaElement getOriginal(IJavaElement workingCopyElement) {
	//not a element contained in a compilation unit
	int javaElementType = workingCopyElement.getElementType();
	if (javaElementType < COMPILATION_UNIT || javaElementType == CLASS_FILE) {
		return null;
	}
	if (workingCopyElement instanceof BinaryMember) {
		return null;
	}
	IJavaElement parent = workingCopyElement.getParent();
	ArrayList hierarchy = new ArrayList(4);
	
	while (parent.getElementType() > COMPILATION_UNIT) {
		hierarchy.add(parent);
		parent = parent.getParent();
	}
	if (parent.getElementType() == COMPILATION_UNIT) {
		hierarchy.add(((ICompilationUnit)parent).getOriginalElement());
	}
	
	ICompilationUnit cu = (ICompilationUnit) getOriginalElement();
	if (javaElementType == COMPILATION_UNIT) {
		parent = workingCopyElement;
	}
	if (((ICompilationUnit) parent).isWorkingCopy() && !((ICompilationUnit) parent).getOriginalElement().equals(cu)) {
		return null;
	}
	switch (javaElementType) {
		case PACKAGE_DECLARATION :
			return cu.getPackageDeclaration(workingCopyElement.getElementName());
		case IMPORT_CONTAINER :
			return cu.getImportContainer();
		case IMPORT_DECLARATION :
			return cu.getImport(workingCopyElement.getElementName());
		case TYPE :
			if (hierarchy.size() == 1) {
				return cu.getType(workingCopyElement.getElementName());
			} else {
				//inner type
				return getOriginalType(hierarchy).getType(workingCopyElement.getElementName());
			}
		case METHOD :
			IType type;
			if (hierarchy.size() == 2) {
				String typeName = ((IJavaElement) hierarchy.get(0)).getElementName();
				type = cu.getType(typeName);
			} else {
				//inner type
				type = getOriginalType(hierarchy);
			}
			return type.getMethod(workingCopyElement.getElementName(), ((IMethod) workingCopyElement).getParameterTypes());
		case FIELD :
			if (hierarchy.size() == 2) {
				String typeName = ((IJavaElement) hierarchy.get(0)).getElementName();
				type = cu.getType(typeName);
			} else {
				//inner type
				type = getOriginalType(hierarchy);
			}
			return type.getField(workingCopyElement.getElementName());
		case INITIALIZER :
			if (hierarchy.size() == 2) {
				String typeName = ((IJavaElement) hierarchy.get(0)).getElementName();
				type = cu.getType(typeName);
			} else {
				//inner type
				type = getOriginalType(hierarchy);
			}
			return type.getInitializer(((Initializer) workingCopyElement).getOccurrenceCount());
		case COMPILATION_UNIT :
			return cu;
		default :
			return null;
	}
}
/**
 * @see IWorkingCopy
 */
public IJavaElement getOriginalElement() {
	return new CompilationUnit((IPackageFragment)getParent(), getElementName());
}
protected IType getOriginalType(ArrayList hierarchy) {
	int size = hierarchy.size() - 1;
	ICompilationUnit typeCU = (ICompilationUnit) hierarchy.get(size);
	String typeName = ((IJavaElement) hierarchy.get(size - 1)).getElementName();
	IType type = typeCU.getType(typeName);
	size= size - 2;
	while (size > -1) {
		typeName = ((IJavaElement) hierarchy.get(size)).getElementName();
		type = ((IType) type).getType(typeName);
		size--;
	}
	return type;
}

/*
 * Answer requestor to notify with problems
 */
public IProblemRequestor getProblemRequestor(){
	return this.problemRequestor;
}

/**
 * @see IWorkingCopy
 */
public IJavaElement getSharedWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {
	return this;
}
/**
 * Returns <code>null<code> - a working copy does not have an underlying resource.
 *
 * @see IJavaElement
 */
public IResource getUnderlyingResource() throws JavaModelException {
	return null;
}
/**
 * @see IWorkingCopy
 */
public IJavaElement getWorkingCopy() throws JavaModelException {
	return this;
}
/**
 * @see IWorkingCopy
 */
public IJavaElement getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {
	return this;
}
/**
 * @see IWorkingCopy
 */
public boolean isBasedOn(IResource resource) {
	if (resource.getType() != IResource.FILE) {
		return false;
	}
	if (this.useCount == 0) {
		return false;
	}
	try {
		// if resource got deleted, then #getModificationStamp() will answer IResource.NULL_STAMP, which is always different from the cached
		// timestamp
		return ((CompilationUnitElementInfo) getElementInfo()).fTimestamp == ((IFile) resource).getModificationStamp();
	} catch (JavaModelException e) {
		return false;
	}
}
/**
 * @see IWorkingCopy
 */
public boolean isWorkingCopy() {
	return true;
}
/**
 * @see IOpenable
 * @see IWorkingCopy
 *
 * @exception JavaModelException attempting to open a read only element for something other than navigation
 * 	or if this is a working copy being opened after it has been destroyed.
 */
public void open(IProgressMonitor pm, IBuffer buffer) throws JavaModelException {
	if (this.useCount == 0) { // was destroyed
		throw newNotPresentException();
	} else {
		super.open(pm, buffer);
	}
}
/**
 * @see Openable
 */
protected IBuffer openBuffer(IProgressMonitor pm) throws JavaModelException {

	IBuffer buffer;
	
	// request buffer factory
	if (this.bufferFactory != null) {
		buffer = this.bufferFactory.createBuffer(this);
		if (buffer != null){
			CompilationUnit original = (CompilationUnit) getOriginalElement();
			buffer.setContents(original.getContents());
			buffer.addBufferChangedListener(this);
			return buffer;
		}
	} 
	// create default buffer
	ICompilationUnit original= (ICompilationUnit)this.getOriginalElement();
	buffer = getBufferManager().openBuffer((char[])original.getBuffer().getCharacters().clone(), pm, this, isReadOnly());
	buffer.addBufferChangedListener(this);
	return buffer;	
}
protected void openWhenClosed(IProgressMonitor pm, IBuffer buffer) throws JavaModelException {
	if (buffer == null && this.bufferFactory != null) {
		buffer = this.bufferFactory.createBuffer(this);
		if (buffer != null){
			CompilationUnit original = (CompilationUnit) getOriginalElement();
			buffer.setContents(original.getContents());
		}
	}
	super.openWhenClosed(pm, buffer);
}
/**
 * @see IWorkingCopy
 */ 
public IMarker[] reconcile() throws JavaModelException {

	// create the delta builder (this remembers the current content of the cu)
	JavaElementDeltaBuilder deltaBuilder = new JavaElementDeltaBuilder(this);

	// update the element infos with the content of the working copy
	this.makeConsistent(null);

	// build the deltas
	deltaBuilder.buildDeltas();
	
	// fire the deltas
	if ((deltaBuilder.delta != null) && (deltaBuilder.delta.getAffectedChildren().length > 0)) {
		JavaModelManager.getJavaModelManager().
			fire(deltaBuilder.delta, ElementChangedEvent.POST_RECONCILE);
	}

	return null;
}

/**
 * @see IWorkingCopy
 */
public void restore() throws JavaModelException {
	if (this.useCount > 0) {
		CompilationUnit original = (CompilationUnit) getOriginalElement();
		getBuffer().setContents(original.getContents());

		updateTimeStamp(original);
		makeConsistent(null);
	}
}
/*
 * @see JavaElement#rootedAt(IJavaProject)
 */
public IJavaElement rootedAt(IJavaProject project) {
	return
		new WorkingCopy(
			(IPackageFragment)((JavaElement)fParent).rootedAt(project), 
			fName,
			this.bufferFactory);

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
		this.reconcile();   // not simply makeConsistent, also computes fine-grain deltas
							// in case the working copy is being reconciled already (if not it would miss
							// one iteration of deltas).
	}
}

/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	buffer.append("[Working copy] "); //$NON-NLS-1$
	super.toStringInfo(0, buffer, info);
}
protected void updateTimeStamp(CompilationUnit original) throws JavaModelException {
	long timeStamp =
		((IFile) original.getUnderlyingResource()).getModificationStamp();
	if (timeStamp == IResource.NULL_STAMP) {
		throw new JavaModelException(
			new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE));
	}
	((CompilationUnitElementInfo) getElementInfo()).fTimestamp = timeStamp;
}
}
