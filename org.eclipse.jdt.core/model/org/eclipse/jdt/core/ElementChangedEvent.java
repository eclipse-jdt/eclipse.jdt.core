package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.EventObject;

/**
 * An element changed event describes a change to the structure or contents
 * of a tree of Java elements. The changes to the elements are described by
 * the associated delta object carried by this event.
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * Instances of this class are automatically created by the Java model.
 * </p>
 *
 * @see IElementChangedListener
 * @see IJavaElementDelta
 */
public class ElementChangedEvent extends EventObject {
	
	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of creations, deletions, and modifications
	 * to one or more Java element(s) expressed as a hierarchical
	 * java element delta as returned by <code>getDelta</code>.
	 *
	 * Note: this notification occurs during the corresponding POST_CHANGE
	 * resource change notification.
	 *
	 * @see IJavaElementDelta
	 * @see IResourceChangeEvent
	 * @see #getDelta
	 * @since 2.0
	 */
	public static final int POST_CHANGE = 1;

	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of creations, deletions, and modifications
	 * to one or more Java element(s) expressed as a hierarchical
	 * java element delta as returned by <code>getDelta</code>.
	 *
	 * Note: this notification occurs during the corresponding PRE_AUTO_BUILD
	 * resource change notification.
	 *
	 * @see IJavaElementDelta
	 * @see IResourceChangeEvent
	 * @see #getDelta
	 * @since 2.0
	 */
	public static final int PRE_AUTO_BUILD = 2;

	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of creations, deletions, and modifications
	 * to one or more Java element(s) expressed as a hierarchical
	 * java element delta as returned by <code>getDelta</code>.
	 *
	 * Note: this notification occurs as a result of a working copy reconcile
	 * operation.
	 *
	 * @see IJavaElementDelta
	 * @see IResourceChangeEvent
	 * @see #getDelta
	 * @since 2.0
	 */
	public static final int 	POST_RECONCILE = 4;	
	/*
	 * Event type indicating the nature of this event. 
	 * It can be a combination either:
	 *  - POST_CHANGE
	 *  - PRE_AUTO_BUILD
	 *  - POST_RECONCILE
	 */
	private int type; 
	
	/**
	 * Creates an new element changed event (based on a <code>IJavaElementDelta</code>).
	 *
	 * @param delta the Java element delta.
	 */
	public ElementChangedEvent(IJavaElementDelta delta, int type) {
		super(delta);
		this.type = type;
	}
	/**
	 * Returns the delta describing the change.
	 *
	 * @return the delta describing the change
	 */
	public IJavaElementDelta getDelta() {
		return (IJavaElementDelta) source;
	}
	
	/**
	 * Returns the type of event being reported.
	 *
	 * @return one of the event type constants
	 * @see #POST_CHANGE
	 * @see #PRE_AUTO_BUILD
	 * @see #POST_RECONCILE
	 * @since 2.0
	 */
	public int getType() {
		return this.type;
	}
}
