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
package org.eclipse.jdt.internal.core.dom.rewrite;

import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

/**
 *
 */
public class TrackedNodePosition implements ITrackedNodePosition {

	private final TextEditGroup group;
	private final ASTNode node;
	
	public TrackedNodePosition(TextEditGroup group, ASTNode node) {
		this.group= group;
		this.node= node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.dom.ITrackedNodePosition#getStartPosition()
	 */
	public int getStartPosition() {
		if (this.group.isEmpty()) {
			return this.node.getStartPosition();
		}
		return TextEdit.getCoverage(this.group.getTextEdits()).getOffset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.dom.ITrackedNodePosition#getLength()
	 */
	public int getLength() {
		if (this.group.isEmpty()) {
			return this.node.getLength();
		}
		return TextEdit.getCoverage(this.group.getTextEdits()).getLength();
	}
}