/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.ExternalJavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;

/**
 * The owner of an <code>ICompilationUnit</code> handle in working copy mode. 
 * An owner is used to identify a working copy and to create its buffer.
 * <p>
 * Clients should subclass this class to instantiate a working copy owner that is specific to their need and that
 * they can pass in to various APIs (e.g. <code>IType.resolveType(String, WorkingCopyOwner)</code>.
 * Clients can also override the default implementation of <code>createBuffer(ICompilationUnit)</code>.
 * </p><p>
 * Note: even though this class has no abstract method, which means that it provides functional default behavior,
 * it is still an abstract class, as clients are intended to own their owner implementation.
 * </p>
 * @see ICompilationUnit#becomeWorkingCopy(IProblemRequestor, org.eclipse.core.runtime.IProgressMonitor)
 * @see ICompilationUnit#discardWorkingCopy()
 * @see ICompilationUnit#getWorkingCopy(org.eclipse.core.runtime.IProgressMonitor)
 * @since 3.0
 */
public abstract class WorkingCopyOwner {
	
	/**
	 * Sets the buffer provider of the primary working copy owner. Note that even if the
	 * buffer provider is a working copy owner, only its <code>createBuffer(ICompilationUnit)</code>
	 * method is used by the primary working copy owner. It doesn't replace the internal primary 
	 * working owner.
 	 * <p>
	 * This method is for internal use by the jdt-related plug-ins.
	 * Clients outside of the jdt should not reference this method.
	 * </p>
	 * 
	 * @param primaryBufferProvider the primary buffer provider
	 */
	public static void setPrimaryBufferProvider(WorkingCopyOwner primaryBufferProvider) {
		DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider = primaryBufferProvider;
	}
	
	/**
	 * Creates a buffer for the given working copy.
	 * The new buffer will be initialized with the contents of the underlying file
	 * if and only if it was not already initialized by the compilation owner (a buffer is 
	 * uninitialized if its content is <code>null</code>).
	 * <p>
	 * Note: This buffer will be associated to the working copy for its entire life-cycle. Another
	 * working copy on same unit but owned by a different owner would not share the same buffer
	 * unless its owner decided to implement such a sharing behaviour.
	 * </p>
	 * 
	 * @param workingCopy the working copy of the buffer
	 * @return IBuffer the created buffer for the given working copy
	 * @see IBuffer
	 */
	public IBuffer createBuffer(ICompilationUnit workingCopy) {

		return BufferManager.getDefaultBufferManager().createBuffer(workingCopy);
	}
	
	/**
	 * Returns a new working copy with the given name using this working copy owner to 
	 * create its buffer. This working copy doesn't belong to any package, thus its 
	 * parent is always <code>null</code> and it cannot be resolved in any way.
	 * Problems are never reported and a DOM AST created using this working copy
	 * will not have bindings resolved.
	 * <p>
	 * When the working copy instance is created, an {@link IJavaElementDelta#ADDED added delta} is 
	 * reported on this working copy.
	 * </p><p>
	 * Once done with the working copy, users of this method must discard it using 
	 * {@link ICompilationUnit#discardWorkingCopy()}.
	 * </p><p>
	 * Note that when such working copy is committed, only its buffer is saved (see 
	 * {@link IBuffer#save(IProgressMonitor, boolean)}) but no resource is created.
	 * </p><p>
	 * This method is not intended to be overriden by clients.
	 * </p>
	 * @param monitor a progress monitor used to report progress while opening this 
	 *   working copy or <code>null</code> if no progress should be reported 
	 * @throws JavaModelException if the contents of this working copy can
	 *   not be determined. 
	 * @return a new working copy
	 * @since 3.2
	 */
	public final ICompilationUnit newWorkingCopy(String name, IClasspathEntry[] classpath, IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException {
		ExternalJavaProject project = new ExternalJavaProject(classpath);
		IPackageFragment parent = project.getPackageFragmentRoot(Path.EMPTY).getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
		CompilationUnit result = new CompilationUnit((PackageFragment) parent, name, this);
		result.becomeWorkingCopy(problemRequestor, monitor);
		return result;
	}

}
