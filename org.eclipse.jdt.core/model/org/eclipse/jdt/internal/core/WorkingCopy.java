package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.internal.compiler.IProblem;
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
	 * A counter of the number of time clients have asked for a managed
	 * working copy on the original element. It is set to 1, if the working
	 * copy is not managed. When destroyed, this counter is
	 * set to 0. Once destroyed, this working copy cannot be opened
	 * and non-handle info can not be accessed. This is
	 * never true if this compilation unit is not a working
	 * copy.
	 */
	protected int managedCount = 0;
/**
 */
protected WorkingCopy(IPackageFragment parent, String name, IBufferFactory bufferFactory) {
	super(parent, name);
	this.bufferFactory = bufferFactory;
	this.managedCount = 1;
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
	if (--this.managedCount > 0) return;
	try {
		close();
		
		// if original element is not on classpath flush it from the cache 
		IJavaElement originalElement = this.getOriginalElement();
		if (!this.getParent().exists()) {
			((CompilationUnit)originalElement).close();
		}
		
		// remove working copy from the cache
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		if (manager.managedWorkingCopies.remove(originalElement) != null) {
			// report removed java delta
			JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
			delta.removed(this);
			manager.registerJavaModelDelta(delta);
			manager.fire();
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
public IJavaElement getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, boolean isManaged) throws JavaModelException {
	return this;
}
/**
 * @see IWorkingCopy
 */
public boolean isBasedOn(IResource resource) {
	if (resource.getType() != IResource.FILE) {
		return false;
	}
	if (this.managedCount == 0) {
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
	if (this.managedCount == 0) {
		throw newNotPresentException();
	} else {
		super.open(pm, buffer);
	}
}
/**
 * @see Openable
 */
protected IBuffer openBuffer(IProgressMonitor pm) throws JavaModelException {
	ICompilationUnit original= (ICompilationUnit)this.getOriginalElement();
	IBuffer buf= getBufferManager().openBuffer((char[])original.getBuffer().getCharacters().clone(), pm, this, isReadOnly());
	buf.addBufferChangedListener(this);
	return buf;	
}
protected void openWhenClosed(IProgressMonitor pm, IBuffer buffer) throws JavaModelException {
	if (buffer == null && this.bufferFactory != null) {
		buffer = this.bufferFactory.createBuffer(this);
		CompilationUnit original = (CompilationUnit) getOriginalElement();
		buffer.setContents(original.getContents());
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
	boolean shouldFire = false;
	JavaModelManager manager = null;
	if (deltaBuilder.delta != null) {
		manager = (JavaModelManager)JavaModelManager.getJavaModelManager();
		if (deltaBuilder.delta.getAffectedChildren().length > 0) {
			manager.registerJavaModelDelta(deltaBuilder.delta);
			shouldFire = true;
		}
	}
	if (shouldFire)
		manager.fire();

	// report syntax problems
	return null;
/* DISABLED because of 1GAJJ3A: ITPJUI:WINNT - Deadlock in Java Editor
	try {
		WorkingCopyElementInfo info = (WorkingCopyElementInfo)JavaModelManager.getJavaModelManager().getInfo(this);
		IProblem[] problems = info.problems;
		int length; 
		IResource resource = getOriginalElement().getUnderlyingResource();
		
		// flush previous markers first
		IMarker[] markers = resource.findMarkers(IJavaModelMarker.TRANSIENT_PROBLEM, true,  IResource.DEPTH_ONE);
		resource.getWorkspace().deleteMarkers(markers);

		// create markers if needed
		if (problems == null || (length = problems.length) == 0) return null;
		markers = new IMarker[length];
		for (int i = 0; i < length; i++) {
			IProblem problem = problems[i];
			IMarker marker = resource.createMarker(IJavaModelMarker.TRANSIENT_PROBLEM);
			marker.setAttribute(IJavaModelMarker.ID, problem.getID());
			marker.setAttribute(IJavaModelMarker.CHAR_START, problem.getSourceStart());
			marker.setAttribute(IJavaModelMarker.CHAR_END, problem.getSourceEnd() + 1);
			marker.setAttribute(IJavaModelMarker.LINE_NUMBER, problem.getSourceLineNumber());
			marker.setAttribute(IMarker.LOCATION, "#" + problem.getSourceLineNumber());
			marker.setAttribute(IMarker.MESSAGE, problem.getMessage());
			marker.setAttribute(IMarker.PRIORITY, (problem.isWarning() ? IMarker.PRIORITY_LOW : IMarker.PRIORITY_HIGH));
			markers[i] = marker;
		}
		return markers;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
*/
}
/**
 * @see IWorkingCopy
 */
public void restore() throws JavaModelException {
	if (this.managedCount > 0) {
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
