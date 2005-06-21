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
package org.eclipse.jdt.apt.core.internal.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.apt.core.internal.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.JarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.PluginFactoryContainer;
import org.eclipse.jdt.apt.core.internal.FactoryContainer.FactoryType;
import org.eclipse.jdt.core.IJavaProject;
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
	
	private static final String FACTORYPATH_FILE = ".factorypath";
	private static final String WORKSPACE_SETTINGS_FILE = ".metadata";
	
	// four spaces for indent
	private static final String INDENT = "    ";

	// Private c-tor to prevent construction
	private FactoryPathUtil() {}
	
	/**
	 * Loads a map of factory containers from the factory path for a given
	 * project. If no factorypath file was created, returns null.
	 */
	public static Map<FactoryContainer, Boolean> readFactoryPathFile(IJavaProject jproj) 
		throws IOException, CoreException
	{
		String data;
		// If project is null, use workspace-level data
		if (jproj == null) {
			File file = new File(getFileForWorkspace(), WORKSPACE_SETTINGS_FILE);
			file = new File(file, FACTORYPATH_FILE);
			if (!file.exists()) {
				return null;
			}
			data = FileSystemUtil.getContentsOfFile(file);
		}
		else {
			IProject proj = jproj.getProject();
			IFile ifile = proj.getFile(FACTORYPATH_FILE);
			if (!ifile.exists()) {
				return null;
			}
			data = FileSystemUtil.getContentsOfIFile(ifile);
		}
		
		return FactoryPathUtil.decodeFactoryPath(data);
	}
	
	/**
	 * Stores a map of factory containers to the factorypath file
	 * for a given project. If null is passed in, the factorypath file
	 * is deleted.
	 */
	public static void saveFactoryPathFile(IJavaProject jproj, Map<FactoryContainer, Boolean> containerMap) 
		throws CoreException, IOException 
	{
		String data = FactoryPathUtil.encodeFactoryPath(containerMap);
		// If project is null, use workspace-level data
		if (jproj == null) {
			File file = new File(getFileForWorkspace(), WORKSPACE_SETTINGS_FILE);
			FileSystemUtil.writeStringToFile(file, data);
		}
		else {
			IProject proj = jproj.getProject();
			IFile file = proj.getFile(FACTORYPATH_FILE);
			if (containerMap == null) {
				file.delete(true, null);
				return;
			}
			
			FileSystemUtil.writeStringToIFile(file, data);
		}
	}
	
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
		Map<FactoryContainer, Boolean> result = new LinkedHashMap<FactoryContainer, Boolean>();
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
	
	/**
	 * Returns all the plugin factory containers that have been registered
	 * as plugins. Note that this does not take into account any factory
	 * plugins that have been disabled by the user's configuration
	 */
	public static List<PluginFactoryContainer> getAllPluginFactoryContainers()
	{
		List<PluginFactoryContainer> factories = new ArrayList<PluginFactoryContainer>();
	
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.jdt.apt.core",  //$NON-NLS-1$ - name of plugin that exposes this extension
				"annotationProcessorFactory"); //$NON-NLS-1$ - extension id

		IExtension[] extensions =  extension.getExtensions();
		for(int i = 0; i < extensions.length; i++) 
		{
			PluginFactoryContainer container = null;
			IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
			for(int j = 0; j < configElements.length; j++)
			{
				String elementName = configElements[j].getName();
				if ( "factory".equals( elementName ) ) //$NON-NLS-1$ - name of configElement 
				{ 
					if ( container == null )
					{
						container = new PluginFactoryContainer(extensions[i].getNamespace());
						factories.add( container );
					}
					container.addFactoryName( configElements[j].getAttribute("class") );
				}
			}
		}
		return factories;
	}
	
	private static File getFileForWorkspace() {
		URL workspaceUrl = Platform.getInstanceLocation().getURL();
		File file = new File(workspaceUrl.getPath());
		return file;
	}
	
}
