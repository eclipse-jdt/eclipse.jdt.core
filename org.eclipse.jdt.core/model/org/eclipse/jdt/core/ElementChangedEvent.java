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
	 * Creates an new element changed event (based on a <code>IJavaElementDelta</code>).
	 *
	 * @param delta the Java element delta.
	 */
	public ElementChangedEvent(IJavaElementDelta delta) {
		super(delta);
	}

	/**
	 * Returns the delta describing the change.
	 *
	 * @return the delta describing the change
	 */
	public IJavaElementDelta getDelta() {
		return (IJavaElementDelta) source;
	}

}
