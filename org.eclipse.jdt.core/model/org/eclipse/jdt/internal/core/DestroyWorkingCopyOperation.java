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

import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Destroys a working copy (remove it from its cache if it is shared)
 * and signal its removal through a delta.
 */
public class DestroyWorkingCopyOperation extends JavaModelOperation {
	
	public DestroyWorkingCopyOperation(IJavaElement workingCopy) {
		super(new IJavaElement[] {workingCopy});
	}
	/**
	 * @exception JavaModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	protected void executeOperation() throws JavaModelException {
		WorkingCopy workingCopy = getWorkingCopy();
		workingCopy.close();
		
		// if original element is not on classpath flush it from the cache 
		IJavaElement originalElement = workingCopy.getOriginalElement();
		if (!workingCopy.getParent().exists()) {
			((CompilationUnit)originalElement).close();
		}
		
		// remove working copy from the cache if it is shared
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		
		// In order to be shared, working copies have to denote the same compilation unit 
		// AND use the same buffer factory.
		// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
		Map sharedWorkingCopies = manager.sharedWorkingCopies;
		
		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(workingCopy.bufferFactory);
		if (perFactoryWorkingCopies != null){
			if (perFactoryWorkingCopies.remove(originalElement) != null
					&& CompilationUnit.SHARED_WC_VERBOSE) {
				System.out.println("Destroying shared working copy " + workingCopy.toStringWithAncestors());//$NON-NLS-1$
			}
		}
		
		// report removed java delta
		JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
		delta.removed(workingCopy);
		addDelta(delta);
		removeReconcileDelta(workingCopy);
	}
	/**
	 * Returns the working copy this operation is working on.
	 */
	protected WorkingCopy getWorkingCopy() {
		return (WorkingCopy)getElementToProcess();
	}
	/**
	 * @see JavaModelOperation#isReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}
}
