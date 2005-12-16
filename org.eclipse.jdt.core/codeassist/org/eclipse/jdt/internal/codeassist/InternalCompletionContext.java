/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;


/**
 * Internal completion context
 * @since 3.1
 */
public class InternalCompletionContext {
	protected char[][] expectedTypesSignatures;
	protected char[][] expectedTypesKeys;
	protected int javadoc;
	
	protected int offset = -1;
	protected int tokenStart = -1;
	protected int tokenEnd = -1;
	protected char[] token = null;
	protected int tokenKind;
	
	protected void setExpectedTypesSignatures(char[][] expectedTypesSignatures) {
		this.expectedTypesSignatures = expectedTypesSignatures;
	}
	
	protected void setExpectedTypesKeys(char[][] expectedTypesKeys) {
		this.expectedTypesKeys = expectedTypesKeys;
	}

	protected void setJavadoc(int javadoc) {
		this.javadoc = javadoc;
	}
	
	protected void setOffset(int offset) {
		this.offset = offset;
	}
	
	protected void setTokenRange(int start, int end) {
		this.setTokenRange(start, end, -1);
	}
	protected void setTokenRange(int start, int end, int endOfEmptyToken) {
		this.tokenStart = start;
		this.tokenEnd = endOfEmptyToken > end ? endOfEmptyToken : end;
	}
	
	protected void setToken(char[] token) {
		this.token = token;
	}
	
	protected void setTokenKind(int tokenKind) {
		this.tokenKind = tokenKind;
	}
}
