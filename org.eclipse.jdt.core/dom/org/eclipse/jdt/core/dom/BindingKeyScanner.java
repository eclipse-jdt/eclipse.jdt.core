/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Internal class.
 * @since 3.1
 */
class BindingKeyScanner {
	
	static final int START = -1;
	static final int PACKAGE = 0;
	static final int TYPE = 1;
	static final int FIELD = 2;
	static final int METHOD = 3;
	static final int ARRAY = 4;
	static final int END = 5;
	
	int index = -1, start;
	char[] source;
	int token = START;

	BindingKeyScanner(char[] source) {
		this.source = source;
	}
	
	int nextToken() {
		this.start = ++this.index;
		int length = this.source.length;
		while (this.index <= length) {
			char currentChar = this.index == length ? Character.MIN_VALUE : this.source[this.index];
			switch (currentChar) {
				case '/':
				case ',':
				case Character.MIN_VALUE:
					switch (this.token) {
						case START:
						case METHOD: // parameter
						case ARRAY:
							this.token = PACKAGE;
							break;
						case PACKAGE:
							if (this.source[this.start-1] == ',')
								this.token = PACKAGE;
							else
								this.token = TYPE;
							break;
						case TYPE:
							switch (this.source[this.start-1]) {
								case '$':
									this.token = TYPE;
									break;
								case ',':
									this.token = PACKAGE;
									break;
								default:
									this.token = FIELD;
							}
							break;
					}
					return this.token;
				case '$':
				case '[':
					switch (this.token) {
						case START: // case of base type with array dimension
							this.token = PACKAGE;
							break;
						case PACKAGE:
							this.token = TYPE;
							break;
						case TYPE:
							this.token = TYPE;
							break;
					}
					return this.token;
				case '(':
					this.token = METHOD;
					return this.token;
				case ')':
					this.start = ++this.index;
					this.token = END;
					return this.token;
				case ']':
					this.start--;
					this.index++;
					while (this.index < length && this.source[this.index] == '[') {
						this.index +=2;
					}
					this.token = ARRAY;
					return this.token;
			}
			this.index++;
		}
		this.token = END;
		return this.token;
	}
	
	char[] getTokenSource() {
		int length = this.index-this.start;
		char[] result = new char[length];
		System.arraycopy(this.source, this.start, result, 0, length);
		return result;
	}
	
	boolean isAtTypeEnd() {
		char currentChar;
		return 
			this.index == -1
			|| this.index >= this.source.length-1 
			|| (currentChar = this.source[this.index]) == ',' 
			|| currentChar == '(' 
			|| currentChar == '<';
	}
	
	boolean isAtTypeParameterStart() {
		return 
			this.start > 0
			&& this.start < this.source.length
			&& this.source[this.start-1] == '<';
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		switch (this.token) {
			case START:
				buffer.append("START: "); //$NON-NLS-1$
				break;
			case PACKAGE:
				buffer.append("PACKAGE: "); //$NON-NLS-1$
				break;
			case TYPE:
				buffer.append("TYPE: "); //$NON-NLS-1$
				break;
			case FIELD:
				buffer.append("FIELD: "); //$NON-NLS-1$
				break;
			case METHOD:
				buffer.append("METHOD: "); //$NON-NLS-1$
				break;
			case ARRAY:
				buffer.append("ARRAY: "); //$NON-NLS-1$
				break;
			case END:
				buffer.append("END: "); //$NON-NLS-1$
				break;
		}
		if (this.index < 0) {
			buffer.append("##"); //$NON-NLS-1$
			buffer.append(this.source);
		} else if (this.index <= this.source.length) {
			buffer.append(CharOperation.subarray(this.source, 0, this.start));
			buffer.append('#');
			buffer.append(CharOperation.subarray(this.source, this.start, this.index));
			buffer.append('#');
			buffer.append(CharOperation.subarray(this.source, this.index, this.source.length));
		} else {
			buffer.append(this.source);
			buffer.append("##"); //$NON-NLS-1$
		}
		return buffer.toString();
	}
}
