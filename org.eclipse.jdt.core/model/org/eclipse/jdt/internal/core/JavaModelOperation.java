/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.PerThreadObject;

/**
 * Defines behavior common to all Java Model operations
 */
public abstract class JavaModelOperation implements IWorkspaceRunnable, IProgressMonitor {
	protected interface IPostAction {
		/*
		 * Returns the id of this action.
		 * @see JavaModelOperation#postAction
		 */
		String getID();
		/*
		 * Run this action.
		 */
		void run() throws JavaModelException;
	}
	/*
	 * Constants controlling the insertion mode of an action.
	 * @see JavaModelOperation#postAction
	 */
	protected static final int APPEND = 1; // insert at the end
	protected static final int REMOVEALL_APPEND = 2; // remove all existing ones with same ID, and add new one at the end
	protected static final int KEEP_EXISTING = 3; // do not insert if already existing with same ID
	
	/*
	 * Whether tracing post actions is enabled.
	 */
	protected static boolean POST_ACTION_VERBOSE;

	/*
	 * A list of IPostActions.
	 */
	protected IPostAction[] actions;
	protected int actionsStart = 0;
	protected int actionsEnd = -1;
	/*
	 * A HashMap of attributes that can be used by operations
	 */
	protected HashMap attributes;

	public static final String HAS_MODIFIED_RESOURCE_ATTR = "hasModifiedResource"; //$NON-NLS-1$
	public static final String TRUE = "true"; //$NON-NLS-1$
	//public static final String FALSE = "false"; //$NON-NLS-1$
		
	/**
	 * The elements this operation operates on,
	 * or <code>null</code> if this operation
	 * does not operate on specific elements.
	 */
	protected IJavaElement[] fElementsToProcess;
	/**
	 * The parent elements this operation operates with
	 * or <code>null</code> if this operation
	 * does not operate with specific parent elements.
	 */
	protected IJavaElement[] fParentElements;
	/**
	 * An empty collection of <code>IJavaElement</code>s - the common
	 * empty result if no elements are created, or if this
	 * operation is not actually executed.
	 */
	protected static IJavaElement[] fgEmptyResult= new IJavaElement[] {};


	/**
	 * The elements created by this operation - empty
	 * until the operation actually creates elements.
	 */
	protected IJavaElement[] fResultElements= fgEmptyResult;

	/**
	 * The progress monitor passed into this operation
	 */
	protected IProgressMonitor fMonitor= null;
	/**
	 * A flag indicating whether this operation is nested.
	 */
	protected boolean fNested = false;
	/**
	 * Conflict resolution policy - by default do not force (fail on a conflict).
	 */
	protected boolean fForce= false;

	/*
	 * A per thread stack of java model operations (PerThreadObject of ArrayList).
	 */
	protected static PerThreadObject operationStacks = new PerThreadObject();
	protected JavaModelOperation() {
	}
	/**
	 * A common constructor for all Java Model operations.
	 */
	protected JavaModelOperation(IJavaElement[] elements) {
		fElementsToProcess = elements;
	}
	/**
	 * Common constructor for all Java Model operations.
	 */
	protected JavaModelOperation(IJavaElement[] elementsToProcess, IJavaElement[] parentElements) {
		fElementsToProcess = elementsToProcess;
		fParentElements= parentElements;
	}
	/**
	 * A common constructor for all Java Model operations.
	 */
	protected JavaModelOperation(IJavaElement[] elementsToProcess, IJavaElement[] parentElements, boolean force) {
		fElementsToProcess = elementsToProcess;
		fParentElements= parentElements;
		fForce= force;
	}
	/**
	 * A common constructor for all Java Model operations.
	 */
	protected JavaModelOperation(IJavaElement[] elements, boolean force) {
		fElementsToProcess = elements;
		fForce= force;
	}
	
	/**
	 * Common constructor for all Java Model operations.
	 */
	protected JavaModelOperation(IJavaElement element) {
		fElementsToProcess = new IJavaElement[]{element};
	}
	/**
	 * A common constructor for all Java Model operations.
	 */
	protected JavaModelOperation(IJavaElement element, boolean force) {
		fElementsToProcess = new IJavaElement[]{element};
		fForce= force;
	}
	
	/*
	 * Registers the given action at the end of the list of actions to run.
	 */
	protected void addAction(IPostAction action) {
		int length = this.actions.length;
		if (length == ++this.actionsEnd) {
			System.arraycopy(this.actions, 0, this.actions = new IPostAction[length*2], 0, length);
		}
		this.actions[this.actionsEnd] = action;
	}
	/*
	 * Registers the given delta with the Java Model Manager.
	 */
	protected void addDelta(IJavaElementDelta delta) {
		JavaModelManager.getJavaModelManager().deltaProcessor.registerJavaModelDelta(delta);
	}
	/*
	 * Registers the given reconcile delta with the Java Model Manager.
	 */
	protected void addReconcileDelta(IWorkingCopy workingCopy, IJavaElementDelta delta) {
		HashMap reconcileDeltas = JavaModelManager.getJavaModelManager().deltaProcessor.reconcileDeltas;
		JavaElementDelta previousDelta = (JavaElementDelta)reconcileDeltas.get(workingCopy);
		if (previousDelta != null) {
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				JavaElementDelta child = (JavaElementDelta)children[i];
				previousDelta.insertDeltaTree(child.getElement(), child);
			}
		} else {
			reconcileDeltas.put(workingCopy, delta);
		}
	}
	/*
	 * Deregister the reconcile delta for the given working copy
	 */
	protected void removeReconcileDelta(IWorkingCopy workingCopy) {
		JavaModelManager.getJavaModelManager().deltaProcessor.reconcileDeltas.remove(workingCopy);		
	}
	/**
	 * @see IProgressMonitor
	 */
	public void beginTask(String name, int totalWork) {
		if (fMonitor != null) {
			fMonitor.beginTask(name, totalWork);
		}
	}
	/**
	 * Checks with the progress monitor to see whether this operation
	 * should be canceled. An operation should regularly call this method
	 * during its operation so that the user can cancel it.
	 *
	 * @exception OperationCanceledException if cancelling the operation has been requested
	 * @see IProgressMonitor#isCanceled
	 */
	protected void checkCanceled() {
		if (isCanceled()) {
			throw new OperationCanceledException(Util.bind("operation.cancelled")); //$NON-NLS-1$
		}
	}
	/**
	 * Common code used to verify the elements this operation is processing.
	 * @see JavaModelOperation#verify()
	 */
	protected IJavaModelStatus commonVerify() {
		if (fElementsToProcess == null || fElementsToProcess.length == 0) {
			return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
		for (int i = 0; i < fElementsToProcess.length; i++) {
			if (fElementsToProcess[i] == null) {
				return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
			}
		}
		return JavaModelStatus.VERIFIED_OK;
	}
	/**
	 * Convenience method to copy resources
	 */
	protected void copyResources(IResource[] resources, IPath destinationPath) throws JavaModelException {
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.copy(resources, destinationPath, false, subProgressMonitor);
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to create a file
	 */
	protected void createFile(IContainer folder, String name, InputStream contents, boolean force) throws JavaModelException {
		IFile file= folder.getFile(new Path(name));
		try {
			file.create(
				contents, 
				force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
				getSubProgressMonitor(1));
				this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to create a folder
	 */
	protected void createFolder(IContainer parentFolder, String name, boolean force) throws JavaModelException {
		IFolder folder= parentFolder.getFolder(new Path(name));
		try {
			// we should use true to create the file locally. Only VCM should use tru/false
			folder.create(
				force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
				true, // local
				getSubProgressMonitor(1));
				this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to delete an empty package fragment
	 */
	protected void deleteEmptyPackageFragment(
		IPackageFragment fragment,
		boolean force,
		IResource rootResource)
		throws JavaModelException {
	
		IContainer resource = (IContainer) fragment.getResource();
	
		try {
			resource.delete(
				force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
				getSubProgressMonitor(1));
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
			while (resource instanceof IFolder) {
				// deleting a package: delete the parent if it is empty (eg. deleting x.y where folder x doesn't have resources but y)
				// without deleting the package fragment root
				resource = resource.getParent();
				if (!resource.equals(rootResource) && resource.members().length == 0) {
					resource.delete(
						force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
						getSubProgressMonitor(1));
					this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
				}
			}
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to delete a resource
	 */
	protected void deleteResource(IResource resource,int flags) throws JavaModelException {
		try {
			resource.delete(flags, getSubProgressMonitor(1));
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to delete resources
	 */
	protected void deleteResources(IResource[] resources, boolean force) throws JavaModelException {
		if (resources == null || resources.length == 0) return;
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.delete(
				resources,
				force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
				subProgressMonitor);
				this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * @see IProgressMonitor
	 */
	public void done() {
		if (fMonitor != null) {
			fMonitor.done();
		}
	}
	/*
	 * Returns whether the given path is equals to one of the given other paths.
	 */
	protected boolean equalsOneOf(IPath path, IPath[] otherPaths) {
		for (int i = 0, length = otherPaths.length; i < length; i++) {
			if (path.equals(otherPaths[i])) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Verifies the operation can proceed and executes the operation.
	 * Subclasses should override <code>#verify</code> and
	 * <code>executeOperation</code> to implement the specific operation behavior.
	 *
	 * @exception JavaModelException The operation has failed.
	 */
	protected void execute() throws JavaModelException {
		IJavaModelStatus status= verify();
		if (status.isOK()) {
			// if first time here, computes the root infos before executing the operation
			DeltaProcessor deltaProcessor = JavaModelManager.getJavaModelManager().deltaProcessor;
			if (deltaProcessor.roots == null) {
				deltaProcessor.initializeRoots();
			}

			executeOperation();
		} else {
			throw new JavaModelException(status);
		}
	}
	/**
	 * Convenience method to run an operation within this operation
	 */
	public void executeNestedOperation(JavaModelOperation operation, int subWorkAmount) throws JavaModelException {
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(subWorkAmount);
		// fix for 1FW7IKC, part (1)
		try {
			operation.setNested(true);
			operation.run(subProgressMonitor);
		} catch (CoreException ce) {
			if (ce instanceof JavaModelException) {
				throw (JavaModelException)ce;
			} else {
				// translate the core exception to a java model exception
				if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
					Throwable e = ce.getStatus().getException();
					if (e instanceof JavaModelException) {
						throw (JavaModelException) e;
					}
				}
				throw new JavaModelException(ce);
			}
		}
	}
	/**
	 * Performs the operation specific behavior. Subclasses must override.
	 */
	protected abstract void executeOperation() throws JavaModelException;
	/*
	 * Returns the attribute registered at the given key with the top level operation.
	 * Returns null if no such attribute is found.
	 */
	protected Object getAttribute(Object key) {
		ArrayList stack = this.getCurrentOperationStack();
		if (stack.size() == 0) return null;
		JavaModelOperation topLevelOp = (JavaModelOperation)stack.get(0);
		if (topLevelOp.attributes == null) {
			return null;
		} else {
			return topLevelOp.attributes.get(key);
		}
	}
	/**
	 * Returns the compilation unit the given element is contained in,
	 * or the element itself (if it is a compilation unit),
	 * otherwise <code>null</code>.
	 */
	protected ICompilationUnit getCompilationUnitFor(IJavaElement element) {
	
		return ((JavaElement)element).getCompilationUnit();
	}
	/*
	 * Returns the stack of operations running in the current thread.
	 * Returns an empty stack if no operations are currently running in this thread. 
	 */
	protected ArrayList getCurrentOperationStack() {
		ArrayList stack = (ArrayList)operationStacks.getCurrent();
		if (stack == null) {
			stack = new ArrayList();
			operationStacks.setCurrent(stack);
		}
		return stack;
	}
	/**
	 * Returns the elements to which this operation applies,
	 * or <code>null</code> if not applicable.
	 */
	protected IJavaElement[] getElementsToProcess() {
		return fElementsToProcess;
	}
	/**
	 * Returns the element to which this operation applies,
	 * or <code>null</code> if not applicable.
	 */
	protected IJavaElement getElementToProcess() {
		if (fElementsToProcess == null || fElementsToProcess.length == 0) {
			return null;
		}
		return fElementsToProcess[0];
	}
	/**
	 * Returns the Java Model this operation is operating in.
	 */
	public IJavaModel getJavaModel() {
		if (fElementsToProcess == null || fElementsToProcess.length == 0) {
			return getParentElement().getJavaModel();
		} else {
			return fElementsToProcess[0].getJavaModel();
		}
	}
	protected IPath[] getNestedFolders(IPackageFragmentRoot root) throws JavaModelException {
		IPath rootPath = root.getPath();
		IClasspathEntry[] classpath = root.getJavaProject().getRawClasspath();
		int length = classpath.length;
		IPath[] result = new IPath[length];
		int index = 0;
		for (int i = 0; i < length; i++) {
			IPath path = classpath[i].getPath();
			if (rootPath.isPrefixOf(path) && !rootPath.equals(path)) {
				result[index++] = path;
			}
		}
		if (index < length) {
			System.arraycopy(result, 0, result = new IPath[index], 0, index);
		}
		return result;
	}
	/**
	 * Returns the parent element to which this operation applies,
	 * or <code>null</code> if not applicable.
	 */
	protected IJavaElement getParentElement() {
		if (fParentElements == null || fParentElements.length == 0) {
			return null;
		}
		return fParentElements[0];
	}
	/**
	 * Returns the parent elements to which this operation applies,
	 * or <code>null</code> if not applicable.
	 */
	protected IJavaElement[] getParentElements() {
		return fParentElements;
	}
	/**
	 * Returns the elements created by this operation.
	 */
	public IJavaElement[] getResultElements() {
		return fResultElements;
	}
	/**
	 * Creates and returns a subprogress monitor if appropriate.
	 */
	protected IProgressMonitor getSubProgressMonitor(int workAmount) {
		IProgressMonitor sub = null;
		if (fMonitor != null) {
			sub = new SubProgressMonitor(fMonitor, workAmount, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		}
		return sub;
	}

	/**
	 * Returns whether this operation has performed any resource modifications.
	 * Returns false if this operation has not been executed yet.
	 */
	public boolean hasModifiedResource() {
		return !this.isReadOnly() && this.getAttribute(HAS_MODIFIED_RESOURCE_ATTR) == TRUE; 
	}
	public void internalWorked(double work) {
		if (fMonitor != null) {
			fMonitor.internalWorked(work);
		}
	}
	/**
	 * @see IProgressMonitor
	 */
	public boolean isCanceled() {
		if (fMonitor != null) {
			return fMonitor.isCanceled();
		}
		return false;
	}
	/**
	 * Returns <code>true</code> if this operation performs no resource modifications,
	 * otherwise <code>false</code>. Subclasses must override.
	 */
	public boolean isReadOnly() {
		return false;
	}
	/*
	 * Returns whether this operation is the first operation to run in the current thread.
	 */
	protected boolean isTopLevelOperation() {
		ArrayList stack;
		return 
			(stack = this.getCurrentOperationStack()).size() > 0
			&& stack.get(0) == this;
	}
	/*
	 * Returns the index of the first registered action with the given id, starting from a given position.
	 * Returns -1 if not found.
	 */
	protected int firstActionWithID(String id, int start) {
		for (int i = start; i <= this.actionsEnd; i++) {
			if (this.actions[i].getID().equals(id)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Convenience method to move resources
	 */
	protected void moveResources(IResource[] resources, IPath destinationPath) throws JavaModelException {
		IProgressMonitor subProgressMonitor = null;
		if (fMonitor != null) {
			subProgressMonitor = new SubProgressMonitor(fMonitor, resources.length, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		}
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.move(resources, destinationPath, false, subProgressMonitor);
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Creates and returns a new <code>IJavaElementDelta</code>
	 * on the Java Model.
	 */
	public JavaElementDelta newJavaElementDelta() {
		return new JavaElementDelta(getJavaModel());
	}
	/*
	 * Removes the last pushed operation from the stack of running operations.
	 * Returns the poped operation or null if the stack was empty.
	 */
	protected JavaModelOperation popOperation() {
		ArrayList stack = getCurrentOperationStack();
		int size = stack.size();
		if (size > 0) {
			if (size == 1) { // top level operation 
				operationStacks.setCurrent(null); // release reference (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=33927)
			}
			return (JavaModelOperation)stack.remove(size-1);
		} else {
			return null;
		}
	}
	/*
	 * Registers the given action to be run when the outer most java model operation has finished.
	 * The insertion mode controls whether:
	 * - the action should discard all existing actions with the same id, and be queued at the end (REMOVEALL_APPEND),
	 * - the action should be ignored if there is already an action with the same id (KEEP_EXISTING),
	 * - the action should be queued at the end without looking at existing actions (APPEND)
	 */
	protected void postAction(IPostAction action, int insertionMode) {
		if (POST_ACTION_VERBOSE) {
			System.out.print("(" + Thread.currentThread() + ") [JavaModelOperation.postAction(IPostAction, int)] Posting action " + action.getID()); //$NON-NLS-1$ //$NON-NLS-2$
			switch(insertionMode) {
				case REMOVEALL_APPEND:
					System.out.println(" (REMOVEALL_APPEND)"); //$NON-NLS-1$
					break;
				case KEEP_EXISTING:
					System.out.println(" (KEEP_EXISTING)"); //$NON-NLS-1$
					break;
				case APPEND:
					System.out.println(" (APPEND)"); //$NON-NLS-1$
					break;
			}
		}
		
		JavaModelOperation topLevelOp = (JavaModelOperation)getCurrentOperationStack().get(0);
		IPostAction[] postActions = topLevelOp.actions;
		if (postActions == null) {
			topLevelOp.actions = postActions = new IPostAction[1];
			postActions[0] = action;
			topLevelOp.actionsEnd = 0;
		} else {
			String id = action.getID();
			switch (insertionMode) {
				case REMOVEALL_APPEND :
					int index = this.actionsStart-1;
					while ((index = topLevelOp.firstActionWithID(id, index+1)) >= 0) {
						// remove action[index]
						System.arraycopy(postActions, index+1, postActions, index, topLevelOp.actionsEnd - index);
						postActions[topLevelOp.actionsEnd--] = null;
					}
					topLevelOp.addAction(action);
					break;
				case KEEP_EXISTING:
					if (topLevelOp.firstActionWithID(id, 0) < 0) {
						topLevelOp.addAction(action);
					}
					break;
				case APPEND:
					topLevelOp.addAction(action);
					break;
			}
		}
	}
	/*
	 * Returns whether the given path is the prefix of one of the given other paths.
	 */
	protected boolean prefixesOneOf(IPath path, IPath[] otherPaths) {
		for (int i = 0, length = otherPaths.length; i < length; i++) {
			if (path.isPrefixOf(otherPaths[i])) {
				return true;
			}
		}
		return false;
	}	
	/*
	 * Pushes the given operation on the stack of operations currently running in this thread.
	 */
	protected void pushOperation(JavaModelOperation operation) {
		getCurrentOperationStack().add(operation);
	}
	/*
	 * Removes all actions with the given id from the queue of post actions.
	 * Does nothing if no such action is in the queue.
	 */
	protected void removeAllPostAction(String actionID) {
		if (POST_ACTION_VERBOSE) {
			System.out.println("(" + Thread.currentThread() + ") [JavaModelOperation.removeAllPostAction(String)] Removing actions " + actionID); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		JavaModelOperation topLevelOp = (JavaModelOperation)getCurrentOperationStack().get(0);
		IPostAction[] postActions = topLevelOp.actions;
		if (postActions == null) return;
		int index = this.actionsStart-1;
		while ((index = topLevelOp.firstActionWithID(actionID, index+1)) >= 0) {
			// remove action[index]
			System.arraycopy(postActions, index+1, postActions, index, topLevelOp.actionsEnd - index);
			postActions[topLevelOp.actionsEnd--] = null;
		}
	}
	
	/**
	 * Main entry point for Java Model operations.  Executes this operation
	 * and registers any deltas created.
	 *
	 * @see IWorkspaceRunnable
	 * @exception CoreException if the operation fails
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		DeltaProcessor deltaProcessor = manager.deltaProcessor;
		int previousDeltaCount = deltaProcessor.javaModelDeltas.size();
		try {
			fMonitor = monitor;
			pushOperation(this);
			try {
				this.execute();
			} finally {
				if (this.isTopLevelOperation()) {
					this.runPostActions();
				}
			}
		} finally {
			try {
				// update JavaModel using deltas that were recorded during this operation
				for (int i = previousDeltaCount, size = deltaProcessor.javaModelDeltas.size(); i < size; i++) {
					deltaProcessor.updateJavaModel((IJavaElementDelta)deltaProcessor.javaModelDeltas.get(i));
				}
				
				// fire only iff:
				// - the operation is a top level operation
				// - the operation did produce some delta(s)
				// - but the operation has not modified any resource
				if (this.isTopLevelOperation()) {
					if ((deltaProcessor.javaModelDeltas.size() > previousDeltaCount || !deltaProcessor.reconcileDeltas.isEmpty()) 
							&& !this.hasModifiedResource()) {
						deltaProcessor.fire(null, DeltaProcessor.DEFAULT_CHANGE_EVENT);
					} // else deltas are fired while processing the resource delta
				}
			} finally {
				popOperation();
			}
		}
	}
	protected void runPostActions() throws JavaModelException {
		while (this.actionsStart <= this.actionsEnd) {
			IPostAction postAction = this.actions[this.actionsStart++];
			if (POST_ACTION_VERBOSE) {
				System.out.println("(" + Thread.currentThread() + ") [JavaModelOperation.runPostActions()] Running action " + postAction.getID()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			postAction.run();
		}
	}
	/*
	 * Registers the given attribute at the given key with the top level operation.
	 */
	protected void setAttribute(Object key, Object attribute) {
		JavaModelOperation topLevelOp = (JavaModelOperation)this.getCurrentOperationStack().get(0);
		if (topLevelOp.attributes == null) {
			topLevelOp.attributes = new HashMap();
		}
		topLevelOp.attributes.put(key, attribute);
	}
	/**
	 * @see IProgressMonitor
	 */
	public void setCanceled(boolean b) {
		if (fMonitor != null) {
			fMonitor.setCanceled(b);
		}
	}
	/**
	 * Sets whether this operation is nested or not.
	 * @see CreateElementInCUOperation#checkCanceled
	 */
	protected void setNested(boolean nested) {
		fNested = nested;
	}
	/**
	 * @see IProgressMonitor
	 */
	public void setTaskName(String name) {
		if (fMonitor != null) {
			fMonitor.setTaskName(name);
		}
	}
	/**
	 * @see IProgressMonitor
	 */
	public void subTask(String name) {
		if (fMonitor != null) {
			fMonitor.subTask(name);
		}
	}
	/**
	 * Returns a status indicating if there is any known reason
	 * this operation will fail.  Operations are verified before they
	 * are run.
	 *
	 * Subclasses must override if they have any conditions to verify
	 * before this operation executes.
	 *
	 * @see IJavaModelStatus
	 */
	protected IJavaModelStatus verify() {
		return commonVerify();
	}
	
	/**
	 * @see IProgressMonitor
	 */
	public void worked(int work) {
		if (fMonitor != null) {
			fMonitor.worked(work);
			checkCanceled();
		}
	}
}
