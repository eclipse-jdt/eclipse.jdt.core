/*******************************************************************************
 * Copyright (c) 2019, 2023 IBM Corporation and others.
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
package org.eclipse.jdt.core.internal.tools.unicode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TableBuilder {

	private static final String CHAR_ELEMENT = "char"; //$NON-NLS-1$
	private static final String SINCE_UNICODE_VERSION = "age"; //$NON-NLS-1$
	private static final String CODE_POINT = "cp"; //$NON-NLS-1$
	private static final String GROUP_CODE = "gc"; //$NON-NLS-1$

	public static String[] buildTables(
			final double unicodeValue,
			boolean usePredefinedRange,
			Environment env,
			String unicodeDataFileName) throws IOException {

		List<String> result = new ArrayList<>();
		SAXParser saxParser = null;
		try {
			@SuppressWarnings("restriction")
			SAXParser p = org.eclipse.core.internal.runtime.XmlProcessorFactory.createSAXParserWithErrorOnDOCTYPE();
			saxParser =p;
		} catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		DefaultHandler defaultHandler = new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
				if (CHAR_ELEMENT.equals(qName)) {
					final String group = attributes.getValue(GROUP_CODE);
					if (env.hasCategory(group)) {
						final String codePoint = attributes.getValue(CODE_POINT);
						final String age = attributes.getValue(SINCE_UNICODE_VERSION);
						double ageValue = 0.0;
						try {
							ageValue = Double.parseDouble(age);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
						if (ageValue <= unicodeValue) {
							result.add(codePoint);
						}
					}
				}
			}
		};
		try {
			saxParser.parse(new File(unicodeDataFileName), defaultHandler);
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		}
		if (usePredefinedRange) {
			// predefined ranges - ISO control character (see
			// isIdentifierIgnorable(int))
			result.add("0000..0008"); //$NON-NLS-1$
			result.add("000E..001B"); //$NON-NLS-1$
			result.add("007F..009F"); //$NON-NLS-1$
		}
		return result.toArray(new String[result.size()]);
	}
}