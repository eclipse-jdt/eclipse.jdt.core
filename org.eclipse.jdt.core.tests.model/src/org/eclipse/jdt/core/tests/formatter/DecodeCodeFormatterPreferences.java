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
package org.eclipse.jdt.core.tests.formatter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DecodeCodeFormatterPreferences extends DefaultHandler {
	
	private boolean record;
	private Map entries;
	private String profileName;

	public static Map decodeCodeFormatterOptions(String fileName, String profileName) {
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			final DecodeCodeFormatterPreferences preferences = new DecodeCodeFormatterPreferences(profileName);
			saxParser.parse(new File(fileName), preferences);
			return preferences.getEntries();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			Element rootNode;

			try {
				DocumentBuilder parser =
					DocumentBuilderFactory.newInstance().newDocumentBuilder();
				rootNode = parser.parse(new InputSource(reader)).getDocumentElement();
				return rootNode;
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		return null;
	}

	DecodeCodeFormatterPreferences(String profileName) {
		this.profileName = profileName;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		int attributesLength = attributes.getLength();
		if ("profile".equals(qName)) {
			if (attributesLength == 1) {
				if ("name".equals(attributes.getQName(0)) && profileName.equals(attributes.getValue(0))) {
					record = true;
					entries = new HashMap();
				}
			}
		} else if ("setting".equals(qName) && record) {
			if (attributesLength == 2) {
				entries.put(attributes.getValue(0), attributes.getValue(1));
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("profile".equals(qName) && record) {
			record = false;
		}
	}
	/**
	 * @return Returns the entries.
	 */
	public Map getEntries() {
		return entries;
	}
}
