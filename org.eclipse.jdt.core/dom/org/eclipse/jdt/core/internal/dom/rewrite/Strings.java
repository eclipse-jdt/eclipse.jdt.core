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
package org.eclipse.jdt.core.internal.dom.rewrite;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;

/**
 * Helper class to provide String manipulation functions not available in standard JDK.
 */
public class Strings {
	
	private Strings() {
		// don't allow to create such an instance    
	}
	
	/**
	 * Indent char is a space char but not a line delimiters.
	 * <code>== Character.isWhitespace(ch) && ch != '\n' && ch != '\r'</code>
	 */
	public static boolean isIndentChar(char ch) {
		return Character.isWhitespace(ch) && !isLineDelimiterChar(ch);
	}

	/**
	 * Line delimiter chars are  '\n' and '\r'.
	 */
	public static boolean isLineDelimiterChar(char ch) {
		return ch == '\n' || ch == '\r';
	}	

	/**
	 * Returns the indent of the given string.
	 * 
	 * @param line the text line
	 * @param tabWidth the width of the '\t' character.
	 */
	public static int computeIndent(String line, int tabWidth) {
		int result= 0;
		int blanks= 0;
		int size= line.length();
		for (int i= 0; i < size; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				result++;
				blanks= 0;
			} else if (isIndentChar(c)) {
				blanks++;
				if (blanks == tabWidth) {
					result++;
					blanks= 0;
				}
			} else {
				return result;
			}
		}
		return result;
	}
	
	/**
	 * Removes the given number of idents from the line. Asserts that the given line 
	 * has the requested number of indents. If <code>indentsToRemove <= 0</code>
	 * the line is returned.
	 */
	public static String trimIndent(String line, int indentsToRemove, int tabWidth) {
		if (line == null || indentsToRemove <= 0)
			return line;
			
		int start= 0;
		int indents= 0;
		int blanks= 0;
		int size= line.length();
		for (int i= 0; i < size; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				indents++;
				blanks= 0;
			} else if (isIndentChar(c)) {
					blanks++;
					if (blanks == tabWidth) {
						indents++;
						blanks= 0;
					}
			} else {
				// Assert.isTrue(false, "Line does not have requested number of indents"); //$NON-NLS-1$
				start= i;
				break; 
			}
			if (indents == indentsToRemove) {
				start= i + 1;
				break;
			}	
		}
		if (start == size)
			return ""; //$NON-NLS-1$
		else
			return line.substring(start);
	}

	
	public static String getIndentString(String line, int tabWidth) {
		int size= line.length();
		int end= 0;
		int blanks= 0;
		for (int i= 0; i < size; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				end= i + 1;
				blanks= 0;
			} else if (isIndentChar(c)) {
				blanks++;
				if (blanks == tabWidth) {
					end= i + 1;
					blanks= 0;
				}
			} else {
				break;
			}
		}
		if (end == 0)
			return ""; //$NON-NLS-1$
		else if (end == size)
			return line;
		else
			return line.substring(0, end);
	}
	
	/**
	 * Returns the length of the string representing the number of 
	 * indents in the given string <code>line</code>. Returns 
	 * <code>-1<code> if the line isn't prefixed with an indent of
	 * the given number of indents. 
	 */
	public static int computeIndentLength(String line, int numberOfIndents, int tabWidth) {
		Assert.isTrue(numberOfIndents >= 0);
		Assert.isTrue(tabWidth >= 0);
		int size= line.length();
		int result= -1;
		int indents= 0;
		int blanks= 0;
		for (int i= 0; i < size && indents < numberOfIndents; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				indents++;
				result= i;
				blanks= 0;
			} else if (isIndentChar(c)) {
				blanks++;
				if (blanks == tabWidth) {
					result= i;
					indents++;
					blanks= 0;
				}
			} else {
				break;
			}
		}
		if (indents < numberOfIndents)
			return -1;
		return result + 1;
	}

	/**
	 * Change the indent of, possible muti-line, code range. The current indent is removed, a new indent added.
	 * The first line of the code will not be changed. (It is considered to have no indent as it might start in
	 * the middle of a line)
	 */
	public static String changeIndent(String code, int codeIndentLevel, int tabWidth, String newIndent, String lineDelim) {
		try {
			ILineTracker tracker= new DefaultLineTracker();
			tracker.set(code);
			int nLines= tracker.getNumberOfLines();
			if (nLines == 1) {
				return code;
			}
			
			StringBuffer buf= new StringBuffer();
			
			for (int i= 0; i < nLines; i++) {
				IRegion region= tracker.getLineInformation(i);
				int start= region.getOffset();
				int end= start + region.getLength();
				String line= code.substring(start, end);
				
				if (i == 0) {  // no indent for first line (contained in the formatted string)
					buf.append(line);
				} else { // no new line after last line
					buf.append(lineDelim);
					buf.append(newIndent); 
					buf.append(trimIndent(line, codeIndentLevel, tabWidth));
				}
			}
			return buf.toString();
		} catch (BadLocationException e) {
			// can not happen
			return code;
		}
	}

}

