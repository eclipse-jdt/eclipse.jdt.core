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
	static final int TYPE_PARAMETER = 5;
	static final int LOCAL_VAR = 6;
	static final int END = 7;
	
	int index = -1, start;
	char[] source;
	int token = START;

	BindingKeyScanner(char[] source) {
		this.source = source;
	}
	
	char[] getTokenSource() {
		int length = this.index-this.start;
		char[] result = new char[length];
		System.arraycopy(this.source, this.start, result, 0, length);
		return result;
	}
	
	boolean isAtFieldOrMethodStart() {
		return 
			this.index+1 < this.source.length
			&& this.source[this.index+1] == '.';
	}
	
	boolean isAtLocalVariableStart() {
		return 
			this.index < this.source.length
			&& this.source[this.index] == '#';
	}
	
	boolean isAtMemberTypeStart() {
		return 
			this.index < this.source.length
			&& (this.source[this.index] == '$'
				|| (this.source[this.index] == '.' && this.source[this.index-1] == '>'));
	}
	
	boolean isAtParametersStart() {
		char currentChar;
		return 
			this.index > 0
			&& this.index < this.source.length
			&& ((currentChar = this.source[this.index]) == '<'
				|| currentChar == '%');
	}
	
	boolean isAtTypeParameterStart() {
		return 
			this.index+1 < this.source.length
			&& this.source[this.index+1] == 'T';
	}
	
	boolean isAtTypeStart() {
		return this.index+1 < this.source.length && "LIZVCDBFJS[".indexOf(this.source[this.index+1]) != -1; //$NON-NLS-1$
	}
	
	int nextToken() {
		this.start = this.token == ARRAY ? this.index : ++this.index;
		int previousTokenEnd = this.index-1;
		int length = this.source.length;
		while (this.index <= length) {
			char currentChar = this.index == length ? Character.MIN_VALUE : this.source[this.index];
			switch (currentChar) {
				case 'B':
				case 'C':
				case 'D':
				case 'F':
				case 'I':
				case 'J':
				case 'S':
				case 'V':
				case 'Z':
					// base type
					if (this.start == previousTokenEnd+1) {
						this.index++;
						this.token = TYPE;
						return this.token;
					}
					break;
				case 'L':
				case 'T':
					if (this.start == previousTokenEnd+1) {
						this.start = ++this.index;
					}
					break;
				case ';':
				case '$':
					this.token = TYPE;
					return this.token;
				case '.':
					this.start = this.index+1;
					break;
				case '[':
					while (this.index < length && this.source[this.index] == '[')
						this.index++;
					this.token = ARRAY;
					return this.token;
				case '<':
					if (this.index > this.start && this.source[this.start-1] == '.')
						if (this.source[this.start-2] == '>')
							this.token = TYPE;
						else
							this.token = METHOD;
					else
						this.token = TYPE;
					return this.token;
				case '(':
					this.token = METHOD;
					return this.token;
				case ')':
					this.start = ++this.index;
					this.token = END;
					return this.token;
				case ':':
					this.token = TYPE_PARAMETER;
					return this.token;
				case '#':
					this.token = LOCAL_VAR;
					return this.token;
				case Character.MIN_VALUE:
					switch (this.token) {
						case START:
							this.token = PACKAGE;
							break;
						case METHOD:
						case LOCAL_VAR:
							this.token = LOCAL_VAR;
							break;
						case TYPE:
							if (this.index > this.start && this.source[this.start-1] == '.')
								this.token = FIELD;
							else
								this.token = END;
							break;
						default:
							this.token = END;
							break;
					}
					return this.token;
			}
			this.index++;
		}
		this.token = END;
		return this.token;
	}
	
	void skipMethodSignature() {
		char currentChar;
		while (this.index < this.source.length && (currentChar = this.source[this.index]) != '#' && currentChar != '%')
			this.index++;
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
			case TYPE_PARAMETER:
				buffer.append("TYPE PARAMETER: "); //$NON-NLS-1$
				break;
			case LOCAL_VAR:
				buffer.append("LOCAL VAR: "); //$NON-NLS-1$
				break;
			case END:
				buffer.append("END: "); //$NON-NLS-1$
				break;
		}
		if (this.index < 0) {
			buffer.append("**"); //$NON-NLS-1$
			buffer.append(this.source);
		} else if (this.index <= this.source.length) {
			buffer.append(CharOperation.subarray(this.source, 0, this.start));
			buffer.append('*');
			if (this.start <= this.index) {
				buffer.append(CharOperation.subarray(this.source, this.start, this.index));
				buffer.append('*');
				buffer.append(CharOperation.subarray(this.source, this.index, this.source.length));
			} else {
				buffer.append('*');
				buffer.append(CharOperation.subarray(this.source, this.start, this.source.length));
			}
		} else {
			buffer.append(this.source);
			buffer.append("**"); //$NON-NLS-1$
		}
		return buffer.toString();
	}
}
