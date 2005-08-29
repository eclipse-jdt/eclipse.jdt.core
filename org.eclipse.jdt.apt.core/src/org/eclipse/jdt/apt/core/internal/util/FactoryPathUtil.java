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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.FactoryContainer;
import org.eclipse.jdt.apt.core.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.internal.PluginFactoryContainer;
import org.eclipse.jdt.apt.core.util.FactoryPath;
import org.eclipse.jdt.core.IJavaProject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Utility class for dealing with the factory path
 */
public final class FactoryPathUtil {
	
	private static final String FACTORYPATH_TAG = "factorypath"; //$NON-NLS-1$
	private static final String FACTORYPATH_ENTRY_TAG = "factorypathentry"; //$NON-NLS-1$
	private static final String KIND = "kind"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String ENABLED = "enabled"; //$NON-NLS-1$
	
	private static final String FACTORYPATH_FILE = ".factorypath"; //$NON-NLS-1$
	
	// four spaces for indent
	private static final String INDENT = "    "; //$NON-NLS-1$

	/** map of plugin names -> factories */
	private static final HashMap<String, AnnotationProcessorFactory> PLUGIN_FACTORY_MAP = new HashMap<String, AnnotationProcessorFactory>();
	
	/** map of plugin names -> plugin factory containers, sorted by plugin name */
	private static final TreeMap<String, PluginFactoryContainer> PLUGIN_CONTAINER_MAP = new TreeMap<String, PluginFactoryContainer>();
	
	// Private c-tor to prevent construction
	private FactoryPathUtil() {}
	
	/**
	 * Loads a map of factory containers from the factory path for a given
	 * project. If no factorypath file was found, returns null.
	 */
	public static Map<FactoryContainer, Boolean> readFactoryPathFile(IJavaProject jproj) 
		throws CoreException
	{
		String data = null;
		try {
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
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, AptPlugin.PLUGIN_ID, -1, Messages.FactoryPathUtil_status_ioException, e));
		}
		
		return FactoryPathUtil.decodeFactoryPath(data);
	}
	
	/**
	 * Stores a map of factory containers to the factorypath file
	 * for a given project. If null is passed in, the factorypath file
	 * is deleted.
	 */
	public static void saveFactoryPathFile(IJavaProject jproj, Map<FactoryContainer, Boolean> containerMap) 
		throws CoreException 
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
		
		try {
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
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, AptPlugin.PLUGIN_ID, -1, Messages.FactoryPathUtil_status_ioException, e));
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
		sb.append("<").append(FACTORYPATH_TAG).append(">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Map.Entry<FactoryContainer, Boolean> entry : factories.entrySet()) {
			FactoryContainer container = entry.getKey();
			Boolean enabled = entry.getValue();
			sb.append(INDENT);
			sb.append("<"); //$NON-NLS-1$
			sb.append(FACTORYPATH_ENTRY_TAG).append(" "); //$NON-NLS-1$
			sb.append(KIND).append("=\"").append(container.getType()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(ID).append("=\"").append(container.getId()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(ENABLED).append("=\"").append(enabled).append("\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append("</").append(FACTORYPATH_TAG).append(">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		
		return sb.toString();
	}
	
	public static Map<FactoryContainer, Boolean> decodeFactoryPath(final String xmlFactoryPath) 
		throws CoreException
	{
		Map<FactoryContainer, Boolean> result = new LinkedHashMap<FactoryContainer, Boolean>();
		StringReader reader = new StringReader(xmlFactoryPath);
		Element fpElement = null;
		
		try {
			DocumentBuilder parser = 
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			fpElement = parser.parse(new InputSource(reader)).getDocumentElement();
			
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, AptPlugin.PLUGIN_ID, -1, Messages.FactoryPathUtil_status_ioException, e));
		}
		catch (SAXException e) {
			throw new CoreException(new Status(IStatus.ERROR, AptPlugin.PLUGIN_ID, -1, Messages.FactoryPathUtil_status_couldNotParse, e));
		}
		catch (ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, AptPlugin.PLUGIN_ID, -1, Messages.FactoryPathUtil_status_parserConfigError, e));
		}
		finally {
			reader.close();
		}
		
		if (!fpElement.getNodeName().equalsIgnoreCase(FACTORYPATH_TAG)) {
			IOException e = new IOException("Incorrect file format. File must begin with " + FACTORYPATH_TAG); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, AptPlugin.PLUGIN_ID, -1, Messages.FactoryPathUtil_status_ioException, e));
		}
		NodeList nodes = fpElement.getElementsByTagName(FACTORYPATH_ENTRY_TAG);
		for (int i=0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)node;
				String kindString = element.getAttribute(KIND);
				// deprecated container type "JAR" is now "EXTJAR"
				if ("JAR".equals(kindString)) { //$NON-NLS-1$
					kindString = "EXTJAR"; //$NON-NLS-1$
				}
				String idString = element.getAttribute(ID);
				String enabledString = element.getAttribute(ENABLED);
				FactoryType kind = FactoryType.valueOf(kindString);
				FactoryContainer container = null;
				switch (kind) {
				
				case WKSPJAR :
					container = FactoryPath.newWkspJarFactoryContainer(new Path(idString));
					break;
					
				case EXTJAR :
					container = FactoryPath.newExtJarFactoryContainer(new File(idString));
					break;
					
				case VARJAR :
					container = FactoryPath.newVarJarFactoryContainer(new Path(idString));
					break;
				
				case PLUGIN :
					container = FactoryPath.getPluginFactoryContainer(idString);
					break;
					
				default :
					throw new IllegalStateException("Unrecognized kind: " + kind + ". Original string: " + kindString); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				if (null != container) {
					result.put(container, new Boolean(enabledString));
				}
			}
		}
		
		return result;
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

	/**
	 * Get a factory path corresponding to the default values: if jproj is
	 * non-null, return the current workspace factory path (workspace prefs
	 * are the default for a project); if jproj is null, return the default 
	 * list of plugin factories (which is the "factory default").
	 */
	public static Map<FactoryContainer, Boolean> getDefaultFactoryPath(IJavaProject jproj) {
		if (jproj != null) {
			return getAllContainers(null);
		}
		else {
			return getAllPluginFactoryContainers();
		}
	}

	/**
	 * Returns all containers for the provided project, including disabled ones.
	 * @param jproj The java project in question, or null for the workspace
	 * @return an ordered map, where the key is the container and the value 
	 * indicates whether the container is enabled.
	 */
	public static synchronized Map<FactoryContainer, Boolean> getAllContainers(IJavaProject jproj) {
		Map<FactoryContainer, Boolean> containers = null;
		boolean foundPerProjFile = false;
		if (jproj != null) {
			try {
				containers = readFactoryPathFile(jproj);
				foundPerProjFile = (containers != null);
			}
			catch (CoreException ce) {
				AptPlugin.log(ce, "Could not get factory containers for project: " + jproj); //$NON-NLS-1$
			}
		}
		// Workspace if no project data was found
		if (containers == null) {
			try {
				containers = readFactoryPathFile(null);
			}
			catch (CoreException ce) {
				AptPlugin.log(ce, "Could not get factory containers for project: " + jproj); //$NON-NLS-1$
			}
		}
		// if no project and no workspace data was found, we'll get the defaults
		if (containers == null) {
			containers = new LinkedHashMap<FactoryContainer, Boolean>();
		}
		boolean disableNewPlugins = (jproj != null) && foundPerProjFile;
		updatePluginContainers(containers, disableNewPlugins);
		return new LinkedHashMap(containers);
	}
	
	/**
	 * Removes missing plugin containers, and adds any plugin containers 
	 * that were added since the map was originally created.  The order
	 * of the original list will be maintained, and new entries will be
	 * added to the end of the list.
	 * @param containers the ordered map of containers to be modified.
	 * The keys in the map are factory containers; the values indicate
	 * whether the container is enabled.
	 * @param disableNewPlugins if true, newly discovered plugins will be
	 * disabled.  If false, they will be enabled or disabled according to
	 * their setting in the extension declaration.
	 */
	private static void updatePluginContainers(
			Map<FactoryContainer, Boolean> containers, boolean disableNewPlugins) {
		Map<FactoryContainer, Boolean> pluginContainers = getAllPluginFactoryContainers();
		
		// Remove any plugin factories whose plugins we did not find
		for (Iterator<FactoryContainer> containerIter = containers.keySet().iterator(); containerIter.hasNext(); ) {
			FactoryContainer container = containerIter.next();
			if (container.getType() == FactoryType.PLUGIN && !pluginContainers.containsKey(container)) {
				containerIter.remove();
			}
		}
		
		// Add any plugins which are new since the config was last saved
		for (Map.Entry<FactoryContainer, Boolean> entry : pluginContainers.entrySet()) {
			if (!containers.containsKey(entry.getKey())) {
				containers.put(entry.getKey(), disableNewPlugins ? Boolean.FALSE : entry.getValue());
			}
		}
	}

	/**
	 * Returns an ordered list of all the plugin factory containers that have 
	 * been registered as plugins.  Note that this does not take into account 
	 * any factory plugins that have been disabled by the user's configuration.
	 * Ordering is alphabetic by plugin id.
	 */
	public static synchronized Map<FactoryContainer, Boolean> getAllPluginFactoryContainers()
	{
		Map<FactoryContainer, Boolean> map = new LinkedHashMap<FactoryContainer, Boolean>();
		for (PluginFactoryContainer pfc : PLUGIN_CONTAINER_MAP.values()) {
			map.put(pfc, pfc.getEnableDefault());
		}
		return map;
	}
	
	public static synchronized AnnotationProcessorFactory getFactoryFromPlugin( String factoryName )
	{
		AnnotationProcessorFactory apf = PLUGIN_FACTORY_MAP.get( factoryName );
		if ( apf == null ) 
		{
			String s = "could not find AnnotationProcessorFactory " +  //$NON-NLS-1$
				factoryName + " from available factories defined by plugins"; //$NON-NLS-1$
			AptPlugin.log(new Status(IStatus.WARNING, AptPlugin.PLUGIN_ID, AptPlugin.STATUS_NOTOOLSJAR, s, null));
		}
		return apf;
	}

    /**
     * Return the factory container corresponding to the specified plugin id.
     * All plugin factories are loaded at startup time.
     * @param pluginId the id of a plugin that extends annotationProcessorFactory.
     * @return a PluginFactoryContainer, or null if the plugin id does not 
     * identify an annotation processor plugin.
     */
	public static synchronized FactoryContainer getPluginFactoryContainer(String pluginId) {
		return PLUGIN_CONTAINER_MAP.get(pluginId);
	}    

	/**
	 * Discover and instantiate annotation processor factories by searching for plugins
	 * which contribute to org.eclipse.jdt.apt.core.annotationProcessorFactory.
	 * This method should only be called once, at startup.
	 */
	public static synchronized void loadPluginFactories() {
		assert PLUGIN_FACTORY_MAP.size() == 0 : "loadPluginFactoryMap() was called more than once"; //$NON-NLS-1$

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
				AptPlugin.PLUGIN_ID, // name of plugin that exposes this extension point
				"annotationProcessorFactory"); //$NON-NLS-1$ - extension id

		// Iterate over all declared extensions of this extension point.  
		// A single plugin may extend the extension point more than once, although it's not recommended.
		for (IExtension extension : extensionPoint.getExtensions())
		{
			// Iterate over the children of the extension to find one named "factories".
			for(IConfigurationElement factories : extension.getConfigurationElements())
			{
				if (!"factories".equals(factories.getName())) { //$NON-NLS-1$ - name of configElement 
					continue;
				}
				
				// Get enableDefault.  If the attribute is missing, default to true.
				String enableDefaultStr = factories.getAttribute("enableDefault"); //$NON-NLS-1$
				boolean enableDefault = true;
				if ("false".equals(enableDefaultStr)) { //$NON-NLS-1$
					enableDefault = false;
				}
				
				// Create and cache a PluginFactoryContainer for this plugin.
				String pluginId = extension.getNamespace();
				PluginFactoryContainer pfc = new PluginFactoryContainer(pluginId, enableDefault);
				PLUGIN_CONTAINER_MAP.put(pluginId, pfc);
				
				// Iterate over the children of the "factories" element to find all the ones named "factory".
				for (IConfigurationElement factory : factories.getChildren()) {
					if (!"factory".equals(factory.getName())) { //$NON-NLS-1$
						continue;
					}
					try {
						Object execExt = factory.createExecutableExtension("class"); //$NON-NLS-1$ - attribute name
						if (execExt instanceof AnnotationProcessorFactory){
							String factoryName = execExt.getClass().getName();
							PLUGIN_FACTORY_MAP.put( factoryName, (AnnotationProcessorFactory)execExt );
							pfc.addFactoryName(factoryName);
						}
					} catch(CoreException e) {
							e.printStackTrace();
					}
				}
			}
		}
	}
	
}
