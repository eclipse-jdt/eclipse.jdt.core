package org.eclipse.jdt.internal.core.jdom;

interface ILineStartFinder {
	/**
	 * Returns the position of the start of the line at or before the given source position.
	 *
	 * <p>This defaults to zero if the position corresponds to a position on the first line
	 * of the source.
	 */
	public int getLineStart(int position);
}
