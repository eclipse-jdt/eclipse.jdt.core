/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.ExtJarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.FactoryPluginManager;
import org.eclipse.jdt.apt.core.internal.VarJarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.WkspJarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
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

	private static final String FACTORYPATH_TAG = "factorypath"; //$NON-NLS-1$
	private static final String FACTORYPATH_ENTRY_TAG = "factorypathentry"; //$NON-NLS-1$
	private static final String KIND = "kind"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String ENABLED = "enabled"; //$NON-NLS-1$
	private static final String RUN_IN_BATCH_MODE = "runInBatchMode"; //$NON-NLS-1$

	private static final String FACTORYPATH_FILE = ".factorypath"; //$NON-NLS-1$

	// four spaces for indent
	private static final String INDENT = "    "; //$NON-NLS-1$

	// Private c-tor to prevent construction
	private FactoryPathUtil() {}

	/**
	 * Test whether a resource is a factory path file.  The criteria are
	 * that it is a file, it belongs to a project, it is located in the root
	 * of that project, and it is named ".factorypath".  Note that the
	 * workspace-wide factorypath file does NOT meet these criteria.
	 * @param res any sort of IResource, or null.
	 * @return true if the resource is a project-specific factory path file.
	 */
	public static boolean isFactoryPathFile(IResource res) {
		if (res == null || res.getType() != IResource.FILE || res.getProject() == null) {
			return false;
		}
		IPath path = res.getProjectRelativePath();
		if (path.segmentCount() != 1) {
			return false;
		}
		return FACTORYPATH_FILE.equals(path.lastSegment());
	}

	/**
	 * Loads a map of factory containers from the factory path for a given
	 * project. If no factorypath file was found, returns null.
	 */
	public static Map<FactoryContainer, FactoryPath.Attributes> readFactoryPathFile(IJavaProject jproj)
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
	public static void saveFactoryPathFile(IJavaProject jproj, Map<FactoryContainer, FactoryPath.Attributes> containers)
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
			if (containers != null) {
				String data = FactoryPathUtil.encodeFactoryPath(containers);
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
	 */
	public static String encodeFactoryPath(Map<FactoryContainer, FactoryPath.Attributes> factories) {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(FACTORYPATH_TAG).append(">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : factories.entrySet()) {
			FactoryContainer container = entry.getKey();
			FactoryPath.Attributes attr = entry.getValue();
			sb.append(INDENT);
			sb.append("<"); //$NON-NLS-1$
			sb.append(FACTORYPATH_ENTRY_TAG).append(" "); //$NON-NLS-1$
			sb.append(KIND).append("=\"").append(container.getType()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(ID).append("=\"").append(container.getId()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(ENABLED).append("=\"").append(attr.isEnabled()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(RUN_IN_BATCH_MODE).append("=\"").append(attr.runInBatchMode()).append("\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append("</").append(FACTORYPATH_TAG).append(">\n"); //$NON-NLS-1$ //$NON-NLS-2$

		return sb.toString();
	}

	/**
	 * Create a factory container based on an external jar file (not in the
	 * workspace).
	 * @param jar a java.io.File representing the jar file.
	 */
	public static FactoryContainer newExtJarFactoryContainer(File jar) {
		return new ExtJarFactoryContainer(jar);
	}

	/**
	 * Create a factory container based on a jar file in the workspace.
	 * @param jar an Eclipse IPath representing the jar file; the path is
	 * relative to the workspace root.
	 */
	public static FactoryContainer newWkspJarFactoryContainer(IPath jar) {
		return new WkspJarFactoryContainer(jar);
	}

	/**
	 * Create a factory container based on an external jar file specified
	 * by a classpath variable (and possibly a path relative to that variable).
	 * @param jar an Eclipse IPath representing the jar file; the first
	 * segment of the path is assumed to be the variable name.
	 */
	public static FactoryContainer newVarJarFactoryContainer(IPath jar) {
		return new VarJarFactoryContainer(jar);
	}

	public static Map<FactoryContainer, FactoryPath.Attributes> decodeFactoryPath(final String xmlFactoryPath)
	throws CoreException
	{
		Map<FactoryContainer, FactoryPath.Attributes> result = new LinkedHashMap<>();
		StringReader reader = new StringReader(xmlFactoryPath);
		Element fpElement = null;

		try {
			@SuppressWarnings("restriction")
			DocumentBuilder parser = org.eclipse.core.internal.runtime.XmlProcessorFactory
					.createDocumentBuilderWithErrorOnDOCTYPE();
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
				String runInAptModeString = element.getAttribute(RUN_IN_BATCH_MODE);
				FactoryType kind = FactoryType.valueOf(kindString);
				FactoryContainer container = null;
				switch (kind) {

				case WKSPJAR :
					container = newWkspJarFactoryContainer(new Path(idString));
					break;

				case EXTJAR :
					container = newExtJarFactoryContainer(new File(idString));
					break;

				case VARJAR :
					container = newVarJarFactoryContainer(new Path(idString));
					break;

				case PLUGIN :
					container = FactoryPluginManager.getPluginFactoryContainer(idString);
					break;

				default :
					throw new IllegalStateException("Unrecognized kind: " + kind + ". Original string: " + kindString); //$NON-NLS-1$ //$NON-NLS-2$
				}

				if (null != container) {
					FactoryPath.Attributes a = new FactoryPath.Attributes(
							Boolean.parseBoolean(enabledString), Boolean.parseBoolean(runInAptModeString));
					result.put(container, a);
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
	 * Calculates the active factory path for the specified project.  This
	 * depends on the stored information in the .factorypath file, as well as
	 * on the set of plugins that were found at load time of this Eclipse instance.
	 * Returns all containers for the provided project, including disabled ones.
	 * @param jproj The java project in question, or null for the workspace
	 * @return an ordered map, where the key is the container and the value
	 * indicates whether the container is enabled.
	 */
	private static synchronized Map<FactoryContainer, FactoryPath.Attributes> calculatePath(IJavaProject jproj) {
		Map<FactoryContainer, FactoryPath.Attributes> map = null;
		boolean foundPerProjFile = false;
		if (jproj != null) {
			try {
				map = FactoryPathUtil.readFactoryPathFile(jproj);
				foundPerProjFile = (map != null);
			}
			catch (CoreException ce) {
				AptPlugin.log(ce, "Could not get factory containers for project: " + jproj); //$NON-NLS-1$
			}
		}
		// Workspace if no project data was found
		if (map == null) {
			try {
				map = FactoryPathUtil.readFactoryPathFile(null);
			}
			catch (CoreException ce) {
				AptPlugin.log(ce, "Could not get factory containers for project: " + jproj); //$NON-NLS-1$
			}
		}
		// if no project and no workspace data was found, we'll get the defaults
		if (map == null) {
			map = new LinkedHashMap<>();
		}
		boolean disableNewPlugins = (jproj != null) && foundPerProjFile;
		updatePluginContainers(map, disableNewPlugins);
		return map;
	}

	/**
	 * Removes missing plugin containers, and adds any plugin containers
	 * that were added since the map was originally created.  The order
	 * of the original list will be maintained, and new entries will be
	 * added to the end of the list in alphabetic order.  The resulting
	 * list has the same contents as PLUGIN_FACTORY_MAP (that is, all the
	 * loaded plugins and nothing else), but the order is as close as possible
	 * to the input.
	 *
	 * @param path the factory path (in raw Map form) to be modified.
	 * @param disableNewPlugins if true, newly discovered plugins will be
	 * disabled.  If false, they will be enabled or disabled according to
	 * their setting in the extension declaration.
	 */
	private static void updatePluginContainers(
			Map<FactoryContainer, FactoryPath.Attributes> path, boolean disableNewPlugins) {

		// Get the alphabetically-ordered list of all plugins we found at startup.
		Map<FactoryContainer, FactoryPath.Attributes> pluginContainers = FactoryPluginManager.getAllPluginFactoryContainers();

		// Remove from the path any plugins which we did not find at startup
		for (Iterator<FactoryContainer> i = path.keySet().iterator(); i.hasNext(); ) {
			FactoryContainer fc = i.next();
			if (fc.getType() == FactoryContainer.FactoryType.PLUGIN && !pluginContainers.containsKey(fc)) {
				i.remove();
			}
		}

		// Add to the end any plugins which are not in the path (i.e., which
		// have been discovered since the config was last saved)
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : pluginContainers.entrySet()) {
			if (!path.containsKey(entry.getKey())) {
				FactoryPath.Attributes newAttr;
				FactoryPath.Attributes oldAttr = entry.getValue();
				if (disableNewPlugins) {
					newAttr = new FactoryPath.Attributes(false, oldAttr.runInBatchMode());
				} else {
					newAttr = oldAttr;
				}
				path.put(entry.getKey(), newAttr);
			}
		}
	}

	/**
	 * Get a factory path corresponding to the default values: if jproj is
	 * non-null, return the current workspace factory path (workspace prefs
	 * are the default for a project); if jproj is null, return the default
	 * list of plugin factories (which is the "factory default").
	 */
	public static IFactoryPath getDefaultFactoryPath(IJavaProject jproj) {
		FactoryPath fp = new FactoryPath();
		if (jproj != null) {
			fp.setContainers(calculatePath(null));
		}
		else {
			fp.setContainers(FactoryPluginManager.getAllPluginFactoryContainers());
		}
		return fp;
	}

	public static FactoryPath getFactoryPath(IJavaProject jproj) {
		Map<FactoryContainer, FactoryPath.Attributes> map = calculatePath(jproj);
		FactoryPath fp = new FactoryPath();
		fp.setContainers(map);
		return fp;
	}

	public static void setFactoryPath(IJavaProject jproj, FactoryPath path)
			throws CoreException {
		Map<FactoryContainer, FactoryPath.Attributes> map = (path != null) ?
				path.getAllContainers() : null;
		saveFactoryPathFile(jproj, map);
	}


}
