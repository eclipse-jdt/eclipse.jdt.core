package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Enumeration;

/**
 * A Delta describes the differences between one State and another,
 * for some part of the Image.
 * Deltas are unusual in that they are inherently wedded to 
 * a <em>pair</em> of states.  Deltas are navigated using DeltaKey objects.
 *
 * <p>The basic approach is similar to that used by Check objects,
 * except that the status of a Delta has four possible values: 
 * (Same, Changed, Added, or Removed).
 *
 * @see DeltaKey
 * @see ICheck
 */
public interface IDelta 
{
	/*
	 * The constants below for known statuses are assigned different
	 * bits, rather than consecutive values, to allow sets
	 * of statuses to be represented by bitwise ORing the status values.
	 */

	/** Constant indicating that the object has not changed. */
	int SAME = 1;

	/**
	 * Constant indicating that the object has changed; this
	 * represents only a true change, not an addition or removal.
	 */
	int CHANGED = 2;

	/** Constant indicating that the object has been added. */
	int ADDED = 4;

	/** Constant indicating that the object has been removed. */
	int REMOVED = 8;

	/** Constant indicating that the status of a delta is unknown. */
	int UNKNOWN = -1;


	/** 
	 * Returns positive, negative, or zero, depending on whether aKey is greater than,
	 * less than or equal to the receiver, respectively.
	 *
	 * This comparison is used for sorting deltas, and therefore the only criterion is that
	 * it compares deltas in a consistent manner that allows for sorting.
	 */
	int compareTo(IDelta anotherDelta);
	/**
	 * Returns the delta reached by navigating the given 
	 * relative path from this object.
	 * It is an error if the delta could never have such a descendent.
	 * <pre>
	 *     - navigation off a checklist with the wrong child name 
	 *       is always bad
	 *     - navigation off a batch delta is common when you don't 
	 *       yet know whether the object is present; this is allowed
	 *     - navigation off a leaf delta is always bad
	 * </pre>
	 *
	 * @param path the path to follow
	 * @exception InvalidKeyException if an invalid key is given.
	 */
	IDelta follow(IDeltaKey path) throws InvalidKeyException;
	/**
	 * Returns the immediate subdeltas of this delta that are additions
	 * (i.e. their status is Added).
	 */
	IDelta[] getAddedSubdeltas();
	/**
	 * Returns the immediate subdeltas of this delta that are
	 * not the same (i.e. their status is either Added, Removed, or Changed).
	 */
	IDelta[] getAffectedSubdeltas();
	/**
	 * Returns the immediate subdeltas of this delta that are true
	 * changes, not additions or removals (i.e. their status is Changed).
	 */
	IDelta[] getChangedSubdeltas();
	/**
	 * Returns the ImageContext that the delta is restricted to.
	 *
	 * @see IImageBuilder#getImageDelta
	 */
	IImageContext getImageContext();
	/**
	 * Returns the delta key for this delta.
	 * Delta keys often contain non-state-specific handles, but
	 * never state-specific ones.
	 *
	 * @see DeltaKey
	 */
	IDeltaKey getKey();
	/**
	 * Returns the name of the aspect being compared in this delta.
	 */
	String getName();
	/**
	 * Returns the object in the new state that is the focus of this delta.
	 * It only make sense to talk about the 'new object' if the object
	 * that is the focus of this delta is a Handle.  If it is not, this
	 * returns null.
	 *
	 * @return the state-specific handle of the object in the new state.
	 */
	IHandle getNewObject();
	/**
	 * Returns the new state to which this delta pertains.
	 */
	IState getNewState();
	/**
	 * Returns the object that is the focus of this delta.
	 * The result is often a Handle, but in
	 * some cases it is another type of object.
	 * When it is a Handle, it is non-state-specific.
	 */
	Object getObject();
	/**
	 * Returns the object in the new state that is the focus of this delta.
	 * It only make sense to talk about the 'old object' if the object
	 * that is the focus of this delta is a Handle.  If it is not, this
	 * returns null.
	 *
	 * @return the state-specific handle of the object in the old state.
	 */
	IHandle getOldObject();
	/**
	 * Returns the old state to which this delta pertains.
	 */
	IState getOldState();
	/**
	 * Returns the parent delta of this delta, or null if it has no parent.
	 */
	IDelta getParent();
	/**
	 * Returns the immediate subdeltas of this delta that are removals
	 * (i.e. their status is Removed).
	 */
	IDelta[] getRemovedSubdeltas();
	/**
	 * Returns the root delta of the tree containing this delta.
	 */
	IDelta getRoot();
	/**
	 * Returns the status of this delta.  If this delta
	 * is not applicable, it always returns SAME.
	 * If the status is not currently known, it is computed
	 * (UNKNOWN is never returned).
	 *
	 * @see #getStatusIfKnown
	 * @see SAME
	 * @see CHANGED
	 * @see ADDED
	 * @see REMOVED
	 */
	int getStatus();
	/**
	 * Returns the status of this delta if it is known.
	 * Returns UNKNOWN if it is not known whether the object has changed.
	 *
	 * @see #getStatus
	 * @see SAME
	 * @see CHANGED
	 * @see ADDED
	 * @see REMOVED
	 * @see UNKNOWN
	 */
	int getStatusIfKnown();
	/**
	 * Returns an array of Delta objects that are children of this delta.
	 * Returns an array of length 0 if this delta has no children,
	 * or if it is not composite.
	 */
	IDelta[] getSubdeltas();
	/**
	 * Returns whether this delta is a composite delta that is further 
	 * broken down into subdeltas. 
	 */
	boolean hasSubdeltas();
/**
 * Return a string of either the form:
 * 		status this.data.name this.data.oldState ==> this.data.newState 
 * OR
 * 		status / this.data.name
 * 
 * status will be one of the following:
 *		+ if status is ADDED
 *		- if status is REMOVED
 *		" " if status is CHANGED
 * 		= if status is SAME
 *		? if status is UNKNOWN
 * The first string will be returned from a delta check which 
 * relates specifically to the image, all other delta checks 
 * will return the second string.
 * The string returned is only for debugging purposes,
 * and the contents of the string may change in the future.
 * @return java.lang.String
 */
public String toString();
}
