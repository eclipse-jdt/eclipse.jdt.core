package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResourceDelta;

/**
 * A Java element delta describes changes in Java element between two discrete
 * points in time.  Given a delta, clients can access the element that has 
 * changed, and any children that have changed.
 * <p>
 * Deltas have a different status depending on the kind of change they represent.  
 * The list below summarizes each status (as returned by <code>getKind</code>)
 * and its meaning:
 * <ul>
 * <li><code>ADDED</code> - The element described by the delta 
 * has been added. Additional information is specified by
 * <code>getFlags</code> which returns <code>F_ADDED_TO_CLASSPATH</code>
 * if the element added is a package fragment root on the classpath.</li>
 * <li><code>REMOVED</code> - The element described by the delta 
 * has been removed. Additional information is specified by
 * <code>getFlags</code> which returns <code>F_REMOVED_FROM_CLASSPATH</code>
 * if the element removed was a package fragment root on the classpath.</li>
 * <li><code>CHANGED</code> - The element described by the delta 
 * has been changed in some way.  Specification of the type of change is provided
 * by <code>getFlags</code> which returns the following values:
 * <ul>
 * <li><code>F_CONTENT</code> - The contents of the element have been altered.  This flag
 * is only valid for elements which correspond to files.</li>
 * <li><code>F_CHILDREN</code> - A child of the element has changed in some way.  This flag
 * is only valid if the element is an <code>IParent</code>.</li>
 * <li><code>F_MODIFIERS</code> - the modifiers on the element have changed in some way. 
 * This flag is only valid if the element is an <code>IMember</code>.</li>
 * <li><code>F_OPENED</code> - the underlying <code>IProject</code>
 * has been opened. This flag is only valid if the element is an <code>IJavaModel</code>
 * or an <code>IJavaProject</code>.</li>
 * <li><code>F_CLOSED</code> - the underlying <code>IProject</code>
 * has been closed. This flag is only valid if the element is an <code>IJavaModel</code>
 * or an <code>IJavaProject</code>.</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * <p>
 * Move operations are indicated by other change flags, layered on top
 * of the change flags described above. If element A is moved to become B,
 * the delta for the  change in A will have status <code>REMOVED</code>,
 * with change flag <code>F_MOVED_TO</code>. In this case,
 * <code>getMovedToElement</code> on delta A will return the handle for B.
 * The  delta for B will have status <code>ADDED</code>, with change flag
 * <code>F_MOVED_FROM</code>, and <code>getMovedFromElement</code> on delta
 * B will return the handle for A. (Note, the handle to A in this case represents
 * an element that no longer exists).
 * </p>
 * <p>
 * Note that the move change flags only describe the changes to a single element, they
 * do not imply anything about the parent or children of the element.
 * </p>
 * <p>
 * The following flags describe changes in an <code>IJavaProject</code>'s classpath:
 * <ul>
 * <li><code>F_ADDED_TO_CLASSPATH</code> - the element described by the delta has been added to the
 *	classpath.</li>
 * <li><code>F_REMOVED_FROM_CLASSPATH</code> - the element described by the delta has been removed
 * from the classpath.</li>
 * <li><code>F_CLASSPATH_REORDER</code> - the element described by the delta has changed its
 * position in the classpath.</li>
 * </ul>
 * </p>
 * <p>
 * <code>IJavaElementDelta</code> object are not valid outside the dynamic scope
 * of the notification.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IJavaElementDelta {

	/**
	 * Status constant indicating that the element has been added.
	 */
	public int ADDED = 1;

	/**
	 * Status constant indicating that the element has been removed.
	 */
	public int REMOVED = 2;

	/**
	 * Status constant indicating that the element has been changed,
	 * as described by the change flags.
	 */
	public int CHANGED = 4;

	/**
	 * Change flag indicating that the content of the element has changed.
	 */
	public int F_CONTENT = 0x0001;

	/**
	 * Change flag indicating that the modifiers of the element have changed.
	 */
	public int F_MODIFIERS = 0x0002;

	/**
	 * Change flag indicating that there are changes to the children of the element.
	 */
	public int F_CHILDREN = 0x0008;

	/**
	 * Change flag indicating that the element was moved from another location.
	 * The location of the old element can be retrieved using <code>getMovedFromElement</code>.
	 */
	public int F_MOVED_FROM = 0x0010;

	/**
	 * Change flag indicating that the element was moved to another location.
	 * The location of the new element can be retrieved using <code>getMovedToElement</code>.
	 */
	public int F_MOVED_TO = 0x0020;

	/**
	 * Change flag indicating that the element was added to the classpath. In this
	 * case the element is an <code>IPackageFragmentRoot</code>.
	 */
	public int F_ADDED_TO_CLASSPATH = 0x0040;

	/**
	 * Change flag indicating that the element was removed from the classpath. In this
	 * case the element is an <code>IPackageFragmentRoot</code>.
	 */
	public int F_REMOVED_FROM_CLASSPATH = 0x0080;

	/**
	 * Change flag indicating that the element's position in the classpath has changed.
	 * In this case the element is an <code>IPackageFragmentRoot</code>.
	 */
	public int F_CLASSPATH_REORDER = 0x0100;

	/**
	 * Change flag indicating that the underlying <code>IProject</code> has been
	 * opened.
	 */
	public int F_OPENED = 0x0200;

	/**
	 * Change flag indicating that the underlying <code>IProject</code> has been
	 * closed.
	 */
	public int F_CLOSED = 0x0400;

	/**
	 * Change flag indicating that one of the supertypes of an <code>IType</code>
	 * has changed.
	 */
	public int F_SUPER_TYPES = 0x0800;

	/**
	 * Change flag indicating that a source jar has been attached to a binary jar.
	 */
	public int F_SOURCEATTACHED = 0x1000;

	/**
	 * Change flag indicating that a source jar has been detached to a binary jar.
	 */
	public int F_SOURCEDETACHED = 0x2000;
	/**
	 * Returns deltas for the children that have been added.
	 */
	public IJavaElementDelta[] getAddedChildren();
	/**
	 * Returns deltas for the affected (added, removed, or changed) children.
	 */
	public IJavaElementDelta[] getAffectedChildren();
	/**
	 * Returns deltas for the children which have changed.
	 */
	public IJavaElementDelta[] getChangedChildren();
	/**
	 * Returns the element that this delta describes a change to.
	 */
	public IJavaElement getElement();
	/**
	 * Returns flags that describe how an element has changed.
	 *
	 * @see IJavaElementDelta#F_CHILDREN
	 * @see IJavaElementDelta#F_CONTENT
	 * @see IJavaElementDelta#F_MODIFIERS
	 * @see IJavaElementDelta#F_MOVED_FROM
	 * @see IJavaElementDelta#F_MOVED_TO
	 * @see IJavaElementDelta#F_ADDED_TO_CLASSPATH
	 * @see IJavaElementDelta#F_REMOVED_FROM_CLASSPATH
	 * @see IJavaElementDelta#F_CLASSPATH_REORDER
	 */
	public int getFlags();
	/**
	 * Returns the kind of this delta - one of <code>ADDED</code>, <code>REMOVED</code>,
	 * or <code>CHANGED</code>.
	 */
	public int getKind();
	/**
	 * Returns an element describing this element before it was moved
	 * to its current location, or <code>null</code> if the
	 * <code>F_MOVED_FROM</code> change flag is not set. 
	 */
	public IJavaElement getMovedFromElement();
	/**
	 * Returns an element describing this element in its new location,
	 * or <code>null</code> if the <code>F_MOVED_TO</code> change
	 * flag is not set.
	 */
	public IJavaElement getMovedToElement();
	/**
	 * Returns deltas for the children which have been removed.
	 */
	public IJavaElementDelta[] getRemovedChildren();
	/**
	 * Returns the collection of resource deltas.
	 * <p>
	 * Note that resource deltas, like Java element deltas, are generally only valid
	 * for the dynamic scope of an event notification. Clients must not hang on to
	 * these objects.
	 * </p>
	 *
	 * @return the underlying resource deltas, or <code>null</code> if none
	 */
	public IResourceDelta[] getResourceDeltas();
}
