package org.eclipse.jdt.internal.core.jdom;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * The <coe>ILineSeparatorFinder</code> finds previous and next line separators
 * in source.
 */
interface ILineStartFinder {
/**
 * Returns the position of the start of the line at or before the given source position.
 *
 * <p>This defaults to zero if the position corresponds to a position on the first line
 * of the source.
 */
public int getLineStart(int position);
}
