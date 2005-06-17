/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.apt.core.internal.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.JarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.PluginFactoryContainer;
import org.eclipse.jdt.apt.core.internal.FactoryContainer.FactoryType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for dealing with the factory path
 */
public final class FactoryPathUtil {
	
	private static final String FACTORYPATH_TAG = "factorypath";
	private static final String FACTORYPATH_ENTRY_TAG = "factorypathentry";
	private static final String KIND = "kind";
	private static final String ID = "id";
	private static final String ENABLED = "enabled";
	
	// four spaces for indent
	private static final String INDENT = "    ";

	// Private c-tor to prevent construction
	private FactoryPathUtil() {}
	
	/**
	 * Returns an XML string encoding all of the factories.
	 * @param jproj
	 * @param factories
	 * @return
	 */
	public static String encodeFactoryPath(Map<FactoryContainer, Boolean> factories) {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(FACTORYPATH_TAG).append(">\n");
		for (Map.Entry<FactoryContainer, Boolean> entry : factories.entrySet()) {
			FactoryContainer container = entry.getKey();
			Boolean enabled = entry.getValue();
			sb.append(INDENT);
			sb.append("<");
			sb.append(FACTORYPATH_ENTRY_TAG).append(" ");
			sb.append(KIND).append("=\"").append(container.getType()).append("\" ");
			sb.append(ID).append("=\"").append(container.getId()).append("\" ");
			sb.append(ENABLED).append("=\"").append(enabled).append("\"/>\n");
		}
		sb.append("</").append(FACTORYPATH_TAG).append(">\n");
		
		return sb.toString();
	}
	
	public static Map<FactoryContainer, Boolean> decodeFactoryPath(final String xmlFactoryPath) 
		throws IOException
	{
		Map<FactoryContainer, Boolean> result = new HashMap<FactoryContainer, Boolean>();
		StringReader reader = new StringReader(xmlFactoryPath);
		Element fpElement = null;
		
		try {
			DocumentBuilder parser = 
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			fpElement = parser.parse(new InputSource(reader)).getDocumentElement();
			
		}
		catch (SAXException e) {
			throw new IOException("Unable to parse: " + e);
		}
		catch (ParserConfigurationException e) {
			throw new IOException("Unable to get parser: " + e);
		}
		finally {
			reader.close();
		}
		
		if (!fpElement.getNodeName().equalsIgnoreCase(FACTORYPATH_TAG)) {
			throw new IOException("Incorrect file format. File must begin with " + FACTORYPATH_TAG);
		}
		NodeList nodes = fpElement.getElementsByTagName(FACTORYPATH_ENTRY_TAG);
		for (int i=0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)node;
				String kindString = element.getAttribute(KIND);
				String idString = element.getAttribute(ID);
				String enabledString = element.getAttribute(ENABLED);
				FactoryType kind = FactoryType.valueOf(kindString);
				FactoryContainer container = null;
				switch (kind) {
				
				case (JAR) :
					container = new JarFactoryContainer(new File(idString));
					break;
				
				case (PLUGIN) :
					container = new PluginFactoryContainer(idString);
					break;
					
				default :
					throw new IllegalStateException("Unrecognized kind: " + kind + ". Original string: " + kindString);
				}
				
				result.put(container, new Boolean(enabledString));
			}
		}
		
		return result;
	}
	
}
