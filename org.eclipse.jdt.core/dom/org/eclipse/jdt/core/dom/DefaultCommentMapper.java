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
package org.eclipse.jdt.core.dom;

/**
 */
class DefaultCommentMapper {
	private Comment[] comments;
	/**
	 * 
	 */
	public DefaultCommentMapper(Comment[] table) {
		this.comments = table;
	}
	
	/**
	 * Get comment of the list which includes a given position
	 * 
	 * @param position The position belonging to the looked up comment
	 * @return comment which includes the given position or null if none was found
	 */
	public Comment getComment(int position) {

		if (this.comments == null) {
			return null;
		}
		int size = this.comments.length;
		if (size == 0) {
			return null;
		}
		int bottom = 0, top = size - 1;
		int i = 0, index = -1;
		Comment comment = null;
		while (bottom <= top) {
			i = (bottom + top) /2;
			comment = this.comments[i];
			int start = comment.getStartPosition();
			if (position < start) {
				top = i-1;
			} else if (position >=(start+comment.getLength())) {
				bottom = i+1;
			} else {
				index = i;
				break;
			}
		}
		if (index<0) {
			/*
			comment = this.comments[i];
			if (position < comment.getStartPosition()) {
				index = i;
			} else {
				index = i+1;
			}
			*/
			return null;
		}
		return this.comments[index];
	}
}
