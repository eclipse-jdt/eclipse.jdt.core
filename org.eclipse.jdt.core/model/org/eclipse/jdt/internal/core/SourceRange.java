package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;

/**
 * @see ISourceRange
 */
/* package */ class SourceRange implements ISourceRange {

protected int offset, length;

protected SourceRange(int offset, int length) {
	this.offset = offset;
	this.length = length;
}
/**
 * @see ISourceRange
 */
public int getLength() {
	return this.length;
}
/**
 * @see ISourceRange
 */
public int getOffset() {
	return this.offset;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[offset="); //$NON-NLS-1$
	buffer.append(this.offset);
	buffer.append(", length="); //$NON-NLS-1$
	buffer.append(this.length);
	buffer.append("]"); //$NON-NLS-1$
	return buffer.toString();
}
}
