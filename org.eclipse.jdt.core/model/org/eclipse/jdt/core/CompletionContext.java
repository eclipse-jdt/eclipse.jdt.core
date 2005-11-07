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
package org.eclipse.jdt.core;

import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnJavadoc;

/**
 * Completion context.
 * 
 * Represent the context in which the completion occurs.
 * 
 * @see CompletionRequestor#acceptContext(CompletionContext)
 * @since 3.1
 */
public final class CompletionContext extends InternalCompletionContext {

	/**
	 * The completion token is a name.
	 * @since 3.2
	 */
	public static final int TOKEN_KIND_NAME = 1;
	/**
	 * The completion token is a string literal.
	 * The string literal ends quote can be not present the source.
	 * <code>"foo"</code> or <code>"foo</code>. 
	 * @since 3.2
	 */
	
	public static final int TOKEN_KIND_STRING_LITERAL = 2;
	/**
	 * Tell user whether completion takes place in a javadoc comment or not.
	 * 
	 * @return boolean true if completion takes place in a javadoc comment, false otherwise.
	 * @since 3.2
	 */
	public boolean isInJavadoc() {
		return this.javadoc != 0;
	}

	/**
	 * Tell user whether completion takes place in text area of a javadoc comment or not.
	 * 
	 * @return boolean true if completion takes place in a text area of a javadoc comment, false otherwise.
	 * @since 3.2
	 */
	public boolean isInJavadocText() {
		return (this.javadoc & CompletionOnJavadoc.TEXT) != 0;
	}

	/**
	 * Tell user whether completion takes place in a formal reference of a javadoc tag or not.
	 * Tags with formal reference are:
	 * <ul>
	 * 	<li>&#64;see</li>
	 * 	<li>&#64;throws</li>
	 * 	<li>&#64;exception</li>
	 * 	<li>{&#64;link Object}</li>
	 * 	<li>{&#64;linkplain Object}</li>
	 * 	<li>{&#64;value} when compiler compliance is set at leats to 1.5</li>
	 * </ul>
	 * 
	 * @return boolean true if completion takes place in formal reference of a javadoc tag, false otherwise.
	 * @since 3.2
	 */
	public boolean isInJavadocFormalReference() {
		return (this.javadoc & CompletionOnJavadoc.FORMAL_REFERENCE) != 0;
	}

	/**
	 * Return signatures of expected types of a potential completion proposal at the completion position.
	 * 
	 * It's not mandatory to a completion proposal to respect this expectation. 
	 * 
	 * @return signatures expected types of a potential completion proposal at the completion position or
	 * <code>null</code> if there is no expected types.
	 * 
	 * @see Signature
	 */
	public char[][] getExpectedTypesSignatures() {
		return this.expectedTypesSignatures;
	}
	/**
	 * Return keys of expected types of a potential completion proposal at the completion position.
	 * 
	 * It's not mandatory to a completion proposal to respect this expectation. 
	 * 
	 * @return keys of expected types of a potential completion proposal at the completion position or
	 * <code>null</code> if there is no expected types.
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTParser#createASTs(ICompilationUnit[], String[], org.eclipse.jdt.core.dom.ASTRequestor, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public char[][] getExpectedTypesKeys() {
		return this.expectedTypesKeys;
	}
	
	/**
	 * Returns the completed token.
	 * This token is either the identifier or Java language keyword
	 * or the string literal under, immediately preceding, 
	 * the original request offset. If the original request offset
	 * is not within or immediately after an identifier or keyword or
	 * a string literal then the returned value is <code>null</code>.
	 * 
	 * @return completed token or <code>null</code>
	 * @since 3.2
	 */
	public char[] getToken() {
		return this.token;
	}
	
	/**
	 * Returns the kind of completion token being proposed.
	 * <p>
	 * The set of different kinds of completion token is
	 * expected to change over time. It is strongly recommended
	 * that clients do <b>not</b> assume that the kind is one of the
	 * ones they know about, and code defensively for the
	 * possibility of unexpected future growth.
	 * </p>
	 * 
	 * @return the kind; one of the kind constants declared on
	 * this class whose name starts with <code>TOKEN_KIND</code>,
	 * or possibly a kind unknown to the caller
	 * @since 3.2
	 */
	public int getTokenKind() {
		return this.tokenKind;
	}
	
	/**
	 * Returns the character index of the start of the
	 * subrange in the source file buffer containing the
	 * relevant token being completed. This
	 * token is either the identifier or Java language keyword
	 * under, or immediately preceding, the original request 
	 * offset. If the original request offset is not within
	 * or immediately after an identifier or keyword, then the
	 * position returned is original request offset and the
	 * token range is empty.
	 * 
	 * @return character index of token start position (inclusive)
	 * @since 3.2
	 */
	public int getTokenStart() {
		return this.tokenStart;
	}
	
	/**
	 * Returns the character index of the end (exclusive) of the subrange
	 * in the source file buffer containing the
	 * relevant token. When there is no relevant token, the
	 * range is empty
	 * (<code>getTokenEnd() == getTokenStart()</code>).
	 * 
	 * @return character index of token end position (exclusive)
	 * @since 3.2
	 */
	public int getTokenEnd() {
		return this.tokenEnd;
	}
	
	/**
	 * Returns the offset position in the source file buffer
	 * after which code assist is requested.
	 * 
	 * @return offset position in the source file buffer
	 * @since 3.2
	 */
	public int getOffset() {
		return this.offset;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("completion offset="); //$NON-NLS-1$
		buffer.append(this.offset);
		buffer.append('\n');
		
		buffer.append("completion range=["); //$NON-NLS-1$
		buffer.append(this.tokenStart);
		buffer.append(", "); //$NON-NLS-1$
		buffer.append(this.tokenEnd);
		buffer.append("]\n"); //$NON-NLS-1$
		
		buffer.append("completion token="); //$NON-NLS-1$
		String string = "null"; //$NON-NLS-1$
		if(token == null) {
			buffer.append(string);
		} else {
			buffer.append('\"');
			buffer.append(this.token);
			buffer.append('\"');
		}
		buffer.append('\n');
		
		buffer.append("expectedTypesSignatures="); //$NON-NLS-1$
		if(this.expectedTypesSignatures == null) {
			buffer.append(string);
		} else {
			buffer.append('{');
			for (int i = 0; i < this.expectedTypesSignatures.length; i++) {
				if(i > 0) buffer.append(',');
				buffer.append(this.expectedTypesSignatures[i]);
				
			}
			buffer.append('}');
		}
		buffer.append('\n');
		
		buffer.append("expectedTypesKeys="); //$NON-NLS-1$
		if(expectedTypesSignatures == null) {
			buffer.append(string);
		} else {
			buffer.append('{');
			for (int i = 0; i < this.expectedTypesKeys.length; i++) {
				if(i > 0) buffer.append(',');
				buffer.append(this.expectedTypesKeys[i]);
				
			}
			buffer.append('}');
		}
		buffer.append('\n');
			
		return buffer.toString();
	}
}
