package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.internal.core.lookup.ReferenceInfo;

/* package */ class CompilationUnitElementInfo extends OpenableElementInfo {

	/**
	 * The length of this compilation unit's source code <code>String</code>
	 */
	protected int fSourceLength;

	/**
	 * The reference information for this compilation unit
	 */
	protected ReferenceInfo fRefInfo;
	/** 
	 * Timestamp of original resource at the time this element
	 * was opened or last updated.
	 */
	protected long fTimestamp;
/**
 * Returns the reference information for this compilation unit
 */
public ReferenceInfo getReferenceInfo() {
	return fRefInfo;
}
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
 * Sets the reference information for this compilation unit
 */
public void setReferenceInfo(ReferenceInfo info) {
	fRefInfo = info;
}
/**
 * Sets the length of the source string.
 */
public void setSourceLength(int newSourceLength) {
	fSourceLength = newSourceLength;
}
}
