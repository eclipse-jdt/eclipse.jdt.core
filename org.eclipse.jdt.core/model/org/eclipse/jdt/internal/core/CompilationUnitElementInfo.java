/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.ISourceRange;

/* package */ class CompilationUnitElementInfo extends OpenableElementInfo {

	/**
	 * The length of this compilation unit's source code <code>String</code>
	 */
	protected int sourceLength;

	/** 
	 * Timestamp of original resource at the time this element
	 * was opened or last updated.
	 */
	protected long timestamp;
/**
 * Returns the length of the source string.
 */
public int getSourceLength() {
	return this.sourceLength;
}
protected ISourceRange getSourceRange() {
	return new SourceRange(0, this.sourceLength);
}
protected boolean isOpen() {
	return true;
}
/**
 * Sets the length of the source string.
 */
public void setSourceLength(int newSourceLength) {
	this.sourceLength = newSourceLength;
}
}
