package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Common protocol for Java elements that support working copies.
 * <p>
 * A working copy of a Java element acts just like a regular element (handle),
 * except it is not attached to an underlying resource. A working copy is not
 * visible to the rest of the Java model. Changes in a working copy's
 * buffer are not realized in a resource. To bring the Java model up-to-date with a working
 * copy's contents, an explicit commit must be performed on the working copy. 
 * Other operations performed on a working copy update the
 * contents of the working copy's buffer but do not commit the contents
 * of the working copy.
 * </p>
 * <p>
 * Note: The contents of a working copy is determined when a working
 * copy is created, based on the current content of the element the working
 * copy is created from. If a working copy is an <code>IOpenable</code> and is explicitly
 * closed, the working copy's buffer will be thrown away. However, clients should not
 * explicitly open and close working copies.
 * </p>
 * <p>
 * The client that creates a working copy is responsible for
 * destroying the working copy. The Java model will never automatically
 * destroy or close a working copy. (Note that destroying a working copy
 * does not commit it to the model, it only frees up the memory occupied by
 * the element). After a working copy is destroyed, the working copy cannot
 * be accessed again. Non-handle methods will throw a 
 * <code>JavaModelException</code> indicating the Java element does not exist.
 * </p>
 * <p>
 * A working copy cannot be created from another working copy.
 * Calling <code>getWorkingCopy</code> on a working copy returns the receiver.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IWorkingCopy {
/**
 * Commits the contents of this working copy to its original element
 * and underlying resource, bringing the Java model up-to-date with
 * the current contents of the working copy.
 *
 * <p>It is possible that the contents of the original resource have changed
 * since this working copy was created, in which case there is an update conflict.
 * The value of the <code>force</code> parameter effects the resolution of
 * such a conflict:<ul>
 * <li> <code>true</code> - in this case the contents of this working copy are applied to
 * 	the underlying resource even though this working copy was created before
 *	a subsequent change in the resource</li>
 * <li> <code>false</code> - in this case a <code>JavaModelException</code> is thrown</li>
 * </ul>
 *
 * @exception JavaModelException if this working copy could not commit. Reasons include:
 * <ul>
 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * <li> A <code>CoreException</code> occurred while updating an underlying resource
 * <li> This element is not a working copy (INVALID_ELEMENT_TYPES)
 * <li> A update conflict (described above) (UPDATE_CONFLICT)
 * </ul>
 */
void commit(boolean force, IProgressMonitor monitor) throws JavaModelException;
/**
 * Destroys this working copy, closing its buffer and discarding
 * its structure. Subsequent attempts to access non-handle information
 * for this working copy will result in <code>IJavaModelException</code>s. Has
 * no effect if this element is not a working copy.
 * <p>
 * If this working copy is managed, it is destroyed only when the number of calls to
 * <code>destroy()</code> is the same as the number of calls to <code>
 * getWorkingCopy(IProgressMonitor, IBufferFactory, boolean)</code>. 
 * A REMOVED IJavaElementDelta is then reported on this working copy.
 */
void destroy();
/**
 * Returns the original element the specified working copy element was created from,
 * or <code>null</code> if this is not a working copy element.  This is a handle
 * only method, the returned element may or may not exist.
 */
IJavaElement getOriginal(IJavaElement workingCopyElement);
/**
 * Returns the original element this working copy was created from,
 * or <code>null</code> if this is not a working copy.
 */
IJavaElement getOriginalElement();
/**
 * Returns a working copy of this element if this element is not
 * a working copy, or this element if this element is a working copy.
 *
 * @exception JavaModelException if the contents of this element can
 *   not be determined. Reasons include:
 * <ul>
 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * </ul>
 */
IJavaElement getWorkingCopy() throws JavaModelException;
/**
 * Returns an open working copy (managed or not) of this element using the given factory to create
 * the buffer, or this element if this element is a working copy.
 * <p>
 * Note that this factory will be used for the life time of this working copy, i.e. if the 
 * working copy is closed then reopened, this factory will be used.
 * The buffer will be automatically initialized with the original's compilation unit content
 * upon creation.
 * <p>
 * A managed working copy is a special instance of working copy that is remembered and returned
 * by this element. Thus the same instance of working copy is always returned until it is destroyed.
 * When the managed working copy instance is created, an ADDED IJavaElementDelta is reported on this
 * working copy.
 *
 * @param monitor a progress monitor used to report progress while opening this compilation unit
 *                 or <code>null</code> if no progress should be reported 
 * @param factory the factory that creates a buffer that is used to get the content of the working copy
 *                 or <code>null</code> if the internal factory should be used
 * @param isManaged whether the created working is managed
 * @exception JavaModelException if the contents of this element can
 *   not be determined. Reasons include:
 * <ul>
 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * </ul>
 * @since 2.0
 */
IJavaElement getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, boolean isManaged) throws JavaModelException;
/**
 * Returns whether this element has been asked at least once for a managed working copy, 
 * and this working has not been destroyed yet.
 * 
 * @since 2.0
 */
boolean hasManagedWorkingCopy();
/**
 * Returns whether this working copy's original element's content
 * has not changed since the inception of this working copy.
 */
boolean isBasedOn(IResource resource);
/**
 * Returns whether this element is a working copy.
 */
boolean isWorkingCopy();
/**
 * Reconciles the contents of this working copy.
 * It performs the reconciliation by locally caching the contents of 
 * the working copy, updating the contents, then creating a delta 
 * over the cached contents and the new contents, and finally firing
 * this delta.
 * <p>
 * Returns the syntax problems found in the new contents as transient markers
 * associated with the original element. Returns <code>null</code> if no problems were found.
 * <p>
 * Note: It has been assumed that added inner types should
 * not generate change deltas.  The implementation has been
 * modified to reflect this assumption.
 *
 * @exception JavaModelException if the contents of the original element
 *		cannot be accessed. Reasons include:
 * <ul>
 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * </ul>
 */
IMarker[] reconcile() throws JavaModelException;
/**
 * Restores the contents of this working copy to the current contents of
 * this working copy's original element. Has no effect if this element
 * is not a working copy.
 *
 * <p>Note: This is the inverse of committing the content of the
 * working copy to the original element with <code>commit(boolean, IProgressMonitor)</code>.
 *
 * @exception JavaModelException if the contents of the original element
 *		cannot be accessed.  Reasons include:
 * <ul>
 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * </ul>
 */
void restore() throws JavaModelException;
}
