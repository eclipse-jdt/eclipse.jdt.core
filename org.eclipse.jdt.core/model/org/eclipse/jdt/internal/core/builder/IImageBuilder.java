package org.eclipse.jdt.internal.core.builder;

public interface IImageBuilder 
{


	/**
	 * If the new state is being built incrementally, returns an object 
	 * describing the differences between the old state and the new state,
	 * otherwise returns null.  The delta is restricted to the given
	 * ImageContext.  
	 * This image delta will include entries for all program elements that are
	 * present in:
	 * <pre>
	 * (oldState UNION newState) INTERSECT imageContext
	 *</pre>
	 * That is, it will include each program element that is present in one or the other
	 * state and also in the given image context.
	 * Any delta objects navigated to from the result are restricted 
	 * to the same ImageContext.
	 * Note that there is no necessary relationship between the image context
	 * supplied and the build contexts of the old and new states.
	 */
	IDelta getImageDelta(IImageContext imageContext);
	/**
	 * Returns the state being built.
	 */
	IState getNewState();
	/**
	 * If the new state is being built incrementally, returns the old state, 
	 * otherwise returns null.
	 */
	IState getOldState();
/**
 * Return a string of the form:
 * 		batch image builder for:
 * 			new state: this.data.newstate
 * OR
 * 		incremental image builder for:
 *			new state: this.data.newstate
 * 			old state: this.data.oldstate
 * Obviously, which string gets returned depends
 * on the type of image builder.
 * The string returned is only for debugging purposes,
 * and the contents of the string may change in the future.
 * @return java.lang.String
 */
public String toString();
}
