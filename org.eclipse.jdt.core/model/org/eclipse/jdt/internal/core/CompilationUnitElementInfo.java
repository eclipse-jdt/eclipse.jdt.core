package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.ISourceRange;

/* package */ class CompilationUnitElementInfo extends OpenableElementInfo {

	/**
	 * The length of this compilation unit's source code <code>String</code>
	 */
	protected int fSourceLength;

	/** 
	 * Timestamp of original resource at the time this element
	 * was opened or last updated.
	 */
	protected long fTimestamp;
/**
 * Returns the length of the source string.
 */
public int getSourceLength() {
	return fSourceLength;
}
protected ISourceRange getSourceRange() {
	return new SourceRange(0, fSourceLength);
}
/**
 * Sets the length of the source string.
 */
public void setSourceLength(int newSourceLength) {
	fSourceLength = newSourceLength;
}
}
