/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.parser.Scanner;

/**
 * Specialized scanner for DOM AST nodes.
 */
public class DOMScanner extends Scanner {

	public DOMScanner(
		boolean tokenizeComments,
		boolean tokenizeWhiteSpace,
		boolean checkNonExternalizedStringLiterals,
		long sourceLevel,
		char[][] taskTags,
		char[][] taskPriorities) {
		super(tokenizeComments, tokenizeWhiteSpace, checkNonExternalizedStringLiterals, sourceLevel, taskTags, taskPriorities);
	}
	
	/**
	 * Set start position negative for line comments.
	 * @see org.eclipse.jdt.internal.compiler.parser.Scanner#recordComment(int)
	 */
	public void recordComment(int token) {
		super.recordComment(token);
		if (token == TokenNameCOMMENT_LINE) {
			// for comment line both positions are negative
			this.commentStarts[this.commentPtr] = -this.commentStarts[this.commentPtr];
		}
	}
}
