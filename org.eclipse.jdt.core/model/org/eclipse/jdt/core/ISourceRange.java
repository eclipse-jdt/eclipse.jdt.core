package org.eclipse.jdt.core;

public interface ISourceRange {

	/**
	 * Returns the number of characters of the source code for this element,
	 * relative to the source buffer in which this element is contained.
	 */
	int getLength();
	/**
	 * Returns the 0-based index of the first character of the source code for this element,
	 * relative to the source buffer in which this element is contained.
	 */
	int getOffset();
}
