/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite;

import java.util.ArrayList;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;

import org.eclipse.text.edits.ReplaceEdit;

/**
 * Helper class to provide String manipulation functions dealing with indents
 */
public class Indents {
	
	private Indents() {
		// don't instanciate
	}
	
	/**
	 * Indent char is a space char but not a line delimiters.
	 * <code>== Character.isWhitespace(ch) && ch != '\n' && ch != '\r'</code>
	 * @param ch
	 * @return Returns true if this the character is a indent delimiter character
	 */
	public static boolean isIndentChar(char ch) {
		return Character.isWhitespace(ch) && !isLineDelimiterChar(ch);
	}

	/**
	 * Line delimiter chars are  '\n' and '\r'.
	 * @param ch The character to test
	 * @return Returns true if this the character is a line delimiter character
	 */
	public static boolean isLineDelimiterChar(char ch) { 
		return ch == '\n' || ch == '\r';
	}	

	/**
	 * Returns the indent of the given string.
	 * 
	 * @param line the text line
	 * @param tabWidth the width of the '\t' character.
	 * @return Returns the indent of the given string.
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
	 * @param line The line to trim the indent
	 * @param indentsToRemove The indent level to remove
	 * @param tabWidth The current tab width
	 * @return Returns the trimed line
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
	 * @param line
	 * @param numberOfIndents
	 * @param tabWidth
	 * @return Returns the length of the string representing the number of 
	 * indents
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
	 * @param code The code to change the indent of
	 * @param codeIndentLevel The indent level of the code
	 * @param tabWidth The current tab width setting
	 * @param newIndent The new Indent string
	 * @param lineDelim THe current line delimiter
	 * @return Returns the newly indented code
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
	
	/**
	 * Change the indent of, possible muti-line, code range. The current indent is removed, a new indent added.
	 * The first line of the code will not be changed. (It is considered to have no indent as it might start in
	 * the middle of a line)
	 * @param source The code to change the indent of
	 * @param sourceIndentLevel The indent level of the code
	 * @param tabWidth The current tab width setting
	 * @param newIndent The new Indent string
	 * @return Returns the resulting text edits
	 */
	public static ReplaceEdit[] getChangeIndentEdits(String source, int sourceIndentLevel, int tabWidth, String newIndent) {
	    ArrayList result= new ArrayList();
		try {
			ILineTracker tracker= new DefaultLineTracker();
			tracker.set(source);
			int nLines= tracker.getNumberOfLines();
			if (nLines == 1)
				return (ReplaceEdit[])result.toArray(new ReplaceEdit[result.size()]);
			for (int i= 1; i < nLines; i++) {
				IRegion region= tracker.getLineInformation(i);
				int offset= region.getOffset();
				String line= source.substring(offset, offset + region.getLength());
				int length= Indents.computeIndentLength(line, sourceIndentLevel, tabWidth);
				if (length >= 0) {
					result.add(new ReplaceEdit(offset, length, newIndent));
				} else {
					length= Indents.computeIndent(line, tabWidth);
					result.add(new ReplaceEdit(offset, length, "")); //$NON-NLS-1$
				}
			}
		} catch (BadLocationException cannotHappen) {
			// can not happen
		}
		return (ReplaceEdit[])result.toArray(new ReplaceEdit[result.size()]);
	}

}

