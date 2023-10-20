/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter.comment;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

/**
 * Utilities for the comment formatter.
 *
 * @since 3.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CommentFormatterUtil {
	/**
	 * Formats the source string as a comment region of the specified kind.
	 * <p>
	 * Both offset and length must denote a valid comment partition, that is
	 * to say a substring that starts and ends with the corresponding
	 * comment delimiter tokens.
	 *
	 * @param kind the kind of the comment
	 * @param source the source string to format
	 * @param offset the offset relative to the source string where to
	 *                format
	 * @param length the length of the region in the source string to format
	 * @param preferences preferences for the comment formatter
	 * @return the formatted source string
	 */
	public static String format(int kind, String source, int offset, int length, Map preferences) {
		Assert.isNotNull(source);
		IDocument document= new Document(source);

		try {
			int indentOffset= document.getLineOffset(document.getLineOfOffset(offset));
			int indentationLevel= inferIndentationLevel(document.get(indentOffset, offset - indentOffset), getTabSize(preferences));
			return format(kind, source, offset, length, indentationLevel, preferences);
		} catch (BadLocationException x) {
			throw new RuntimeException(x);
		}
	}

	/**
	 * Formats the source string as a comment region of the specified kind.
	 * <p>
	 * Both offset and length must denote a valid comment partition, that is
	 * to say a substring that starts and ends with the corresponding
	 * comment delimiter tokens.
	 *
	 * @param kind the kind of the comment
	 * @param source the source string to format
	 * @param offset the offset relative to the source string where to
	 *                format
	 * @param length the length of the region in the source string to format
	 * @param preferences preferences for the comment formatter
	 * @return the formatted source string
	 */
	public static String format(int kind, String source, int offset, int length, int indentationLevel, Map preferences) {
		Assert.isTrue(kind == CodeFormatter.K_JAVA_DOC || kind == CodeFormatter.K_MULTI_LINE_COMMENT || kind == CodeFormatter.K_SINGLE_LINE_COMMENT);

		Assert.isNotNull(source);
		Assert.isNotNull(preferences);

		Assert.isTrue(offset >= 0);
		Assert.isTrue(length <= source.length());

		IDocument document= new Document(source);

		TextEdit edit;
		edit= ToolFactory.createCodeFormatter(preferences).format(kind, source, offset, length, indentationLevel, TextUtilities.getDefaultLineDelimiter(document));

		try {
			if (edit != null)
				edit.apply(document);
		} catch (MalformedTreeException x) {
			throw new RuntimeException(x);
		} catch (BadLocationException x) {
			throw new RuntimeException(x);
		}
		return document.get();
	}

	/**
	 * Infer the indentation level based on the given reference indentation,
	 * tab size and text measurement.
	 *
	 * @param reference the reference indentation
	 * @param tabSize the tab size
	 * @return the inferred indentation level
	 * @since 3.1
	 */
	private static int inferIndentationLevel(String reference, int tabSize) {
		StringBuilder expanded= expandTabs(reference, tabSize);

		int spaceWidth, referenceWidth;
		spaceWidth= 1;
		referenceWidth= expanded.length();

		int level= referenceWidth / (tabSize * spaceWidth);
		if (referenceWidth % (tabSize * spaceWidth) > 0)
			level++;
		return level;
	}

	/**
	 * Expands the given string's tabs according to the given tab size.
	 *
	 * @param string the string
	 * @param tabSize the tab size
	 * @return the expanded string
	 * @since 3.1
	 */
	private static StringBuilder expandTabs(String string, int tabSize) {
		StringBuilder expanded= new StringBuilder();
		for (int i= 0, n= string.length(), chars= 0; i < n; i++) {
			char ch= string.charAt(i);
			if (ch == '\t') {
				for (; chars < tabSize; chars++)
					expanded.append(' ');
				chars= 0;
			} else {
				expanded.append(ch);
				chars++;
				if (chars >= tabSize)
					chars= 0;
			}

		}
		return expanded;
	}

	/**
	 * Returns the value of {@link DefaultCodeFormatterConstants#FORMATTER_TAB_SIZE}
	 * from the given preferences.
	 *
	 * @param preferences the preferences
	 * @return the value of {@link DefaultCodeFormatterConstants#FORMATTER_TAB_SIZE}
	 *         from the given preferences
	 * @since 3.1
	 */
	private static int getTabSize(Map preferences) {
		if (preferences.containsKey(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE))
			try {
				return Integer.parseInt((String) preferences.get(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE));
			} catch (NumberFormatException e) {
				// use default
			}
		return 4;
	}

	/**
	 * Creates a formatting options with all default options and the given custom user options.
	 *
	 * @param user the custom user options
	 * @return the formatting options
	 * @since 3.1
	 */
	public static Map createOptions(Map user) {
		final Map map= JavaCore.getOptions();

		if (user != null) {

			for (final Iterator iterator= user.keySet().iterator(); iterator.hasNext();) {

				Object key= iterator.next();
				Object value = user.get(key);
				map.put(key, value);
			}
		}
		return map;
	}
}
