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
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.apt.core.AptPlugin;
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
	
	// four spaces for indent
	private static final String INDENT = "    ";

	// Private c-tor to prevent construction
	private FactoryPathUtil() {}
	
	/**
	 * Loads a map of factory containers from the factory path for a given
	 * project. If no factorypath file was found, returns null.
	 */
	public static Map<FactoryContainer, Boolean> readFactoryPathFile(IJavaProject jproj) 
		throws IOException, CoreException
	{
		String data;
		// If project is null, use workspace-level data
		if (jproj == null) {
			File file = getFileForWorkspace();
			if (!file.exists()) {
				return null;
			}
			data = FileSystemUtil.getContentsOfFile(file);
		}
		else {
			IFile ifile = getIFileForProject(jproj);
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
		IFile projFile;
		File wkspFile;
		if (jproj != null) {
			projFile = getIFileForProject(jproj);
			wkspFile = null;
		}
		else {
			wkspFile = getFileForWorkspace();
			projFile = null;
		}
		
		if (containerMap != null) {
			String data = FactoryPathUtil.encodeFactoryPath(containerMap);
			// If project is null, set workspace-level data
			if (jproj == null) {
				FileSystemUtil.writeStringToFile(wkspFile, data);
			}
			else {
				FileSystemUtil.writeStringToIFile(projFile, data);
			}
		}
		else { // restore defaults by deleting the factorypath file.
			if (jproj != null) {
				projFile.delete(true, null);
			}
			else {
				wkspFile.delete();
			}
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
	 * Returns an ordered list of all the plugin factory containers that have 
	 * been registered as plugins.  Note that this does not take into account 
	 * any factory plugins that have been disabled by the user's configuration.
	 * Ordering is alphabetic by plugin id.
	 */
	public static List<PluginFactoryContainer> getAllPluginFactoryContainers()
	{
		// We want the list of plugins to be uniqued and alphabetically sorted.
		Map<String, PluginFactoryContainer> containers = 
			new TreeMap<String, PluginFactoryContainer>();
	
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.jdt.apt.core",  //$NON-NLS-1$ - name of plugin that exposes this extension
				"annotationProcessorFactory"); //$NON-NLS-1$ - extension id

		IExtension[] extensions =  extension.getExtensions();
		// Iterate over all declared extensions of this extension point.  
		// A single plugin may extend the extension point more than once.
		for(int i = 0; i < extensions.length; i++) 
		{
			IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
			// Iterate over all the factories in a single extension declaration.
			// An extension may define more than one factory.
			for(int j = 0; j < configElements.length; j++)
			{
				String elementName = configElements[j].getName();
				if ( "factory".equals( elementName ) ) //$NON-NLS-1$ - name of configElement 
				{ 
					String pluginId = extensions[i].getNamespace();
					PluginFactoryContainer container = containers.get(pluginId);
					if ( container == null )
					{
						// getNamespace() returns the plugin id
						container = new PluginFactoryContainer(pluginId);
						containers.put( pluginId, container );
					}
					container.addFactoryName( configElements[j].getAttribute("class") );
				}
			}
		}
		List<PluginFactoryContainer> list = new ArrayList<PluginFactoryContainer>(containers.values());
		
		return list;
	}
	
	/**
	 * Get a file designator for the workspace-level factory path settings file.
	 * Typically this is [workspace]/.metadata/plugins/org.eclipse.jdt.apt.core/.factorypath
	 * @return a java.io.File
	 */
	private static File getFileForWorkspace() {
		return AptPlugin.getPlugin().getStateLocation().append(FACTORYPATH_FILE).toFile();
	}

	/**
	 * Get an Eclipse IFile for the project-level factory path settings file.
	 * Typically this is [project]/.factorypath
	 * @param jproj must not be null
	 * @return an Eclipse IFile
	 */
	private static IFile getIFileForProject(IJavaProject jproj) {
		IProject proj = jproj.getProject();
		return proj.getFile(FACTORYPATH_FILE);
	}

	/**
	 * Does a factory path file already exist for the specified project,
	 * or for the workspace as a whole?
	 * @param jproj if this is null, check for workspace-level settings.
	 * @return true if a settings file exists.
	 */
	public static boolean doesFactoryPathFileExist(IJavaProject jproj) {
		if (jproj == null) {
			File wkspFile = getFileForWorkspace();
			return wkspFile.exists();
		}
		else {
			IFile projFile = getIFileForProject(jproj);
			return projFile.exists();
		}
	}
	
}
