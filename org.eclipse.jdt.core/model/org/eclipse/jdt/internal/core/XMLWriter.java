/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.util.Util;
/**
 * @since 3.0
 */
class XMLWriter extends PrintWriter {
	/* constants */
	private static final String XML_VERSION= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$
	private static void appendEscapedChar(StringBuffer buffer, char c) {
		String replacement= getReplacement(c);
		if (replacement != null) {
			buffer.append('&');
			buffer.append(replacement);
			buffer.append(';');
		} else {
			buffer.append(c);
		}
	}
	private static String getEscaped(String s) {
		StringBuffer result= new StringBuffer(s.length() + 10);
		for (int i= 0; i < s.length(); ++i)
			appendEscapedChar(result, s.charAt(i));
		return result.toString();
	}
	private static String getReplacement(char c) {
		// Encode special XML characters into the equivalent character references.
		// These five are defined by default for all XML documents.
		switch (c) {
			case '<' :
				return "lt"; //$NON-NLS-1$
			case '>' :
				return "gt"; //$NON-NLS-1$
			case '"' :
				return "quot"; //$NON-NLS-1$
			case '\'' :
				return "apos"; //$NON-NLS-1$
			case '&' :
				return "amp"; //$NON-NLS-1$
		}
		return null;
	}
	private int tab;
	private String lineSeparator;
	public XMLWriter(Writer writer, IJavaProject project, boolean printXmlVersion) {
		super(writer);
		this.tab= 0;
		this.lineSeparator = Util.getLineSeparator((String) null, project);
		if (printXmlVersion) {
			print(XML_VERSION);
			print(this.lineSeparator);
		}
	}
	public void endTag(String name, boolean insertTab, boolean insertNewLine) {
		this.tab --;
		printTag('/' + name, null/*no parameters*/, insertTab, insertNewLine, false/*don't close tag*/);
	}
	private void printTabulation() {
		for (int i= 0; i < tab; i++)
			super.print('\t');
	}
	public void printTag(String name, HashMap parameters, boolean insertTab, boolean insertNewLine, boolean closeTag) {
		StringBuffer sb= new StringBuffer();
		sb.append("<"); //$NON-NLS-1$
		sb.append(name);
		if (parameters != null) {
			int length = parameters.size();
			String[] keys = new String[length];
			parameters.keySet().toArray(keys);
			Util.sort(keys);
			for (int i = 0; i < length; i++) {
				sb.append(" "); //$NON-NLS-1$
				sb.append(keys[i]);
				sb.append("=\""); //$NON-NLS-1$
				sb.append(getEscaped(String.valueOf(parameters.get(keys[i]))));
				sb.append("\""); //$NON-NLS-1$
			}
		}
		if (closeTag) {
			sb.append("/>"); //$NON-NLS-1$
		} else {
			sb.append(">"); //$NON-NLS-1$
		}
		printString(sb.toString(), insertTab, insertNewLine);
		if (parameters != null && !closeTag)
			this.tab++;

	}
	public void printString(String string, boolean insertTab, boolean insertNewLine) {
		if (insertTab) {
			printTabulation();
		}
		print(string);
		if (insertNewLine) {
			print(this.lineSeparator);
		}
	}
	public void startTag(String name, boolean insertTab) {
		printTag(name, null/*no parameters*/, insertTab, true/*insert new line*/, false/*don't close tag*/);
		this.tab++;
	}
}
