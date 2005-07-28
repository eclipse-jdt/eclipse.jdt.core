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
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;

import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

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
	 * Returns the indent of the given string in indentation units. Odd spaces
	 * are not counted.
	 * 
	 * @param line the text line
	 * @param tabWidth the width of the '\t' character in space equivalents
	 * @param indentWidth the width of one indentation unit in space equivalents
	 * @since 3.1
	 */
	public static int computeIndentUnits(String line, int tabWidth, int indentWidth) {
		if (indentWidth == 0)
			return -1;
		int visualLength= measureIndentLength(line, tabWidth);
		return visualLength / indentWidth;
	}
	
	/**
	 * Computes the visual length of the indentation of a
	 * <code>CharSequence</code>, counting a tab character as the size until
	 * the next tab stop and every other whitespace character as one.
	 * 
	 * @param line the string to measure the indent of
	 * @param tabSize the visual size of a tab in space equivalents
	 * @return the visual length of the indentation of <code>line</code>
	 * @since 3.1
	 */
	public static int measureIndentLength(CharSequence line, int tabSize) {
		int length= 0;
		int max= line.length();
		for (int i= 0; i < max; i++) {
			char ch= line.charAt(i);
			if (ch == '\t') {
				int reminder= length % tabSize;
				length += tabSize - reminder;
			} else if (isIndentChar(ch)) {
				length++;
			} else {
				return length;
			}
		}
		return length;
	}
	
	/**
	 * Removes the given number of indents from the line. Asserts that the given line 
	 * has the requested number of indents. If <code>indentsToRemove <= 0</code>
	 * the line is returned.
	 * 
	 * @since 3.1
	 */
	public static String trimIndent(String line, int indentsToRemove, int tabWidth, int indentWidth) {
		if (line == null || indentsToRemove <= 0)
			return line;

		final int spaceEquivalentsToRemove= indentsToRemove * indentWidth;
		
		int start= 0;
		int spaceEquivalents= 0;
		int size= line.length();
		String prefix= null;
		for (int i= 0; i < size; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				int remainder= spaceEquivalents % tabWidth;
				spaceEquivalents += tabWidth - remainder;
			} else if (isIndentChar(c)) {
				spaceEquivalents++;
			} else {
				// Assert.isTrue(false, "Line does not have requested number of indents");
				start= i;
				break; 
			}
			if (spaceEquivalents == spaceEquivalentsToRemove) {
				start= i + 1;
				break;
			}
			if (spaceEquivalents > spaceEquivalentsToRemove) {
				// can happen if tabSize > indentSize, e.g tabsize==8, indent==4, indentsToRemove==1, line prefixed with one tab
				// this implements the third option
				start= i + 1; // remove the tab
				// and add the missing spaces
				char[] missing= new char[spaceEquivalents - spaceEquivalentsToRemove];
				Arrays.fill(missing, ' ');
				prefix= new String(missing);
				break;
			}
		}
		String trimmed;
		if (start == size)
			trimmed= ""; //$NON-NLS-1$
		else
			trimmed= line.substring(start);
		
		if (prefix == null)
			return trimmed;
		return prefix + trimmed;
	}


	
	/**
	 * Returns that part of the indentation of <code>line</code> that makes up
	 * a multiple of indentation units.
	 * 
	 * @param line the line to scan
	 * @param tabWidth the size of one tab in space equivalents
	 * @return the indent part of <code>line</code>, but no odd spaces
	 * @since 3.1
	 */
	public static String getIndentString(String line, int tabWidth, int indentWidth) {
		int size= line.length();
		int end= 0;
		
		int spaceEquivs= 0;
		int characters= 0;
		for (int i= 0; i < size; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				int remainder= spaceEquivs % tabWidth;
				spaceEquivs += tabWidth - remainder;
				characters++;
			} else if (isIndentChar(c)) {
				spaceEquivs++;
				characters++;
			} else {
				break;
			}
			if (spaceEquivs >= indentWidth) {
				end += characters;
				characters= 0;
				spaceEquivs= spaceEquivs % indentWidth;
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
	 * @since 3.1
	 */
	public static int computeIndentLength(String line, int numberOfIndents, int tabWidth, int indentWidth) {
		Assert.isTrue(numberOfIndents >= 0);
		Assert.isTrue(tabWidth >= 0);
		Assert.isTrue(indentWidth >= 0);
		
		int spaceEquivalents= numberOfIndents * indentWidth;
		
		int size= line.length();
		int result= -1;
		int blanks= 0;
		for (int i= 0; i < size && blanks < spaceEquivalents; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				int remainder= blanks % tabWidth;
				blanks += tabWidth - remainder;
			} else if (isIndentChar(c)) {
				blanks++;
			} else {
				break;
			}
			result= i;
		}
		if (blanks < spaceEquivalents)
			return -1;
		return result + 1;
	}

	/**
	 * Change the indent of, possible muti-line, code range. The current indent is removed, a new indent added.
	 * The first line of the code will not be changed. (It is considered to have no indent as it might start in
	 * the middle of a line)
	 * @since 3.1
	 */
	public static String changeIndent(String code, int codeIndentLevel, int tabWidth, int indentWidth, String newIndent, String lineDelim) {
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
					buf.append(trimIndent(line, codeIndentLevel, tabWidth, indentWidth));
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
	public static ReplaceEdit[] getChangeIndentEdits(String source, int sourceIndentLevel, int tabWidth, int indentWidth, String newIndent) {
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
				int length= Indents.computeIndentLength(line, sourceIndentLevel, tabWidth, indentWidth);
				if (length >= 0) {
					result.add(new ReplaceEdit(offset, length, newIndent));
				} else {
					length= Indents.computeIndentUnits(line, tabWidth, indentWidth);
					result.add(new ReplaceEdit(offset, length, "")); //$NON-NLS-1$
				}
			}
		} catch (BadLocationException cannotHappen) {
			// can not happen
		}
		return (ReplaceEdit[])result.toArray(new ReplaceEdit[result.size()]);
	}
	
	/**
	 * Returns the tab width as configured in the given map.
	 * @param options The options to look at
	 * @return the tab width
	 */
	public static int getTabWidth(Map options) {
		return parseIntValue(options, DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, 4);
	}
	
	/**
	 * Returns the tab width as configured in the given map.
	 * @param options The options to look at
	 * 	@param tabWidth the tab width
	 * @return the indent width
	 */
	public static int getIndentWidth(Map options, int tabWidth) {
		boolean isMixedMode= DefaultCodeFormatterConstants.MIXED.equals(options.get(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR));
		if (isMixedMode) {
			return parseIntValue(options, DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, tabWidth);
		}
		return tabWidth;
	}
	
	private static int parseIntValue(Map options, String key, int def) {
		try {
			return Integer.parseInt((String) options.get(key));
		} catch (NumberFormatException e) {
			return def;
		}
	}
	

	/**
	 * Change the indent of, possible muti-line, code range. The current indent is removed, a new indent added.
	 * The first line of the code will not be changed. (It is considered to have no indent as it might start in
	 * the middle of a line)
	 * @deprecated use the version specifying the indent width instead
	 */
	public static String changeIndent(String code, int codeIndentLevel, int tabWidth, String newIndent, String lineDelim) {
		return changeIndent(code, codeIndentLevel, tabWidth, tabWidth, newIndent, lineDelim);
	}

	/**
	 * Returns the indent of the given string.
	 * 
	 * @param line the text line
	 * @param tabWidth the width of the '\t' character.
	 * @deprecated use {@link #computeIndentUnits(String, int, int)} instead
	 */
	public static int computeIndent(String line, int tabWidth) {
		return computeIndentUnits(line, tabWidth, tabWidth);
	}

}

