/*******************************************************************************
 * Copyright (c) 2007, 2015 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Manages caches of plugins which provide annotation processors.
 *
 * @since 3.3
 */
public class FactoryPluginManager {
	/**
	 * Map of factory names -> factories.  A single plugin factory container may
	 * contain multiple annotation processor factories, each with a unique name.
	 * To support lazy initialization, this should only be accessed by calling
	 * @see #getJava5PluginFactoryMap() .
	 */
	private static final HashMap<String, AnnotationProcessorFactory> PLUGIN_JAVA5_FACTORY_MAP = new HashMap<>();

	/**
	 * Map of factory names -> factories.  A single plugin factory container may
	 * contain multiple annotation processor factories, each with a unique name.
	 * To support lazy initialization, this should only be accessed by calling
	 * @see #getJava5PluginFactoryMap() .
	 */
	private static final HashMap<String, IServiceFactory> PLUGIN_JAVA6_FACTORY_MAP = new HashMap<>();

	/**
	 * Map of plugin names -> plugin factory containers, sorted by plugin name.
	 * A plugin that contains annotation processor factories (and extends the
	 * corresponding extension point) is a "plugin factory container".
	 * To support lazy initialization, this should only be accessed by calling
	 * @see #getPluginFactoryContainerMap() .
	 */
	private static final TreeMap<String, PluginFactoryContainer> PLUGIN_CONTAINER_MAP = new TreeMap<>();

	/**
	 * true if PLUGIN_FACTORY_MAP and PLUGIN_CONTAINER_MAP have been initialized,
	 * by calling @see #loadPluginFactories() .
	 */
	private static boolean mapsInitialized = false;

	/**
	 * Returns an ordered list of all the plugin factory containers that have
	 * been registered as plugins.  Note that this may include plugins that have
	 * been disabled by the user's configuration.  The 'enabled' attribute in the
	 * returned map reflects the 'enableDefault' attribute in the plugin
	 * manifest, rather than the user configuration.
	 * Ordering is alphabetic by plugin id.
	 */
	public static synchronized Map<FactoryContainer, FactoryPath.Attributes> getAllPluginFactoryContainers()
	{
		Map<FactoryContainer, FactoryPath.Attributes> map =
			new LinkedHashMap<>(getPluginContainerMap().size());
		for (PluginFactoryContainer pfc : getPluginContainerMap().values()) {
			FactoryPath.Attributes a = new FactoryPath.Attributes(pfc.getEnableDefault(), false);
			map.put(pfc, a);
		}
		return map;
	}

	public static synchronized AnnotationProcessorFactory getJava5FactoryFromPlugin( String factoryName )
	{
		AnnotationProcessorFactory apf = getJava5PluginFactoryMap().get( factoryName );
		if ( apf == null )
		{
			String s = "could not find AnnotationProcessorFactory " +  //$NON-NLS-1$
				factoryName + " from available factories defined by plugins"; //$NON-NLS-1$
			AptPlugin.log(new Status(IStatus.WARNING, AptPlugin.PLUGIN_ID, AptPlugin.STATUS_NOTOOLSJAR, s, null));
		}
		return apf;
	}

	public static synchronized IServiceFactory getJava6FactoryFromPlugin( String factoryName )
	{
		IServiceFactory isf = getJava6PluginFactoryMap().get( factoryName );
		if ( isf == null )
		{
			String s = "could not find annotation processor " +  //$NON-NLS-1$
				factoryName + " from available factories defined by plugins"; //$NON-NLS-1$
			AptPlugin.log(new Status(IStatus.WARNING, AptPlugin.PLUGIN_ID, AptPlugin.STATUS_NOTOOLSJAR, s, null));
		}
		return isf;
	}

	/**
     * Return the factory container corresponding to the specified plugin id.
     * All plugin factories are loaded at startup time.
     * @param pluginId the id of a plugin that extends annotationProcessorFactory.
     * @return a PluginFactoryContainer, or null if the plugin id does not
     * identify an annotation processor plugin.
     */
	public static synchronized FactoryContainer getPluginFactoryContainer(String pluginId) {
		return getPluginContainerMap().get(pluginId);
	}

	/**
	 * Get the alphabetically sorted map of plugin names to plugin factory containers.
	 * Load plugins if the map has not yet been initialized.
	 */
	private static TreeMap<String, PluginFactoryContainer> getPluginContainerMap() {
		loadFactoryPlugins();
		return PLUGIN_CONTAINER_MAP;
	}

	/**
	 * Get the map of plugin factory names to plugin factories.
	 * Load plugins if the map has not yet been initialized.
	 */
	private static HashMap<String, AnnotationProcessorFactory> getJava5PluginFactoryMap() {
		loadFactoryPlugins();
		return PLUGIN_JAVA5_FACTORY_MAP;
	}

	/**
	 * Get the map of plugin factory names to plugin factories.
	 * Load plugins if the map has not yet been initialized.
	 */
	private static HashMap<String, IServiceFactory> getJava6PluginFactoryMap() {
		loadFactoryPlugins();
		return PLUGIN_JAVA6_FACTORY_MAP;
	}

    /**
	 * Discover and instantiate annotation processor factories by searching for plugins
	 * which contribute to org.eclipse.jdt.apt.core.annotationProcessorFactory.
	 * The first time this method is called, it will load all the plugin factories.
	 * Subsequent calls will be ignored.
	 */
	private static synchronized void loadFactoryPlugins() {
		if (mapsInitialized) {
			return;
		}
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
				if ("factories".equals(factories.getName())) { //$NON-NLS-1$ - name of configElement
					loadJava5Factories(extension, factories);
				}
				else if ("java6processors".equals(factories.getName())) { //$NON-NLS-1$ - name of configElement
					loadJava6Factories(extension, factories);
				}

			}
		}
		mapsInitialized = true;
	}

	private static void loadJava6Factories(IExtension extension, IConfigurationElement factories) {
		if (!AptPlugin.canRunJava6Processors()) {
			return;
		}

		// Get enableDefault.  If the attribute is missing, default to true.
		String enableDefaultStr = factories.getAttribute("enableDefault"); //$NON-NLS-1$
		boolean enableDefault = true;
		if ("false".equals(enableDefaultStr)) { //$NON-NLS-1$
			enableDefault = false;
		}

		// Create and cache a PluginFactoryContainer for this plugin.
		String pluginId = extension.getNamespaceIdentifier();
		//TODO: level problem.  In the extension point, enableDefault is associated with element, not ext point.
		PluginFactoryContainer pfc = new PluginFactoryContainer(pluginId, enableDefault);
		PLUGIN_CONTAINER_MAP.put(pluginId, pfc);

		// Iterate over the children of the "java6processors" element to find all the ones named "java6processor".
		for (IConfigurationElement factory : factories.getChildren()) {
			if (!"java6processor".equals(factory.getName())) { //$NON-NLS-1$
				continue;
			}
			String factoryName = null;
			try {
				factoryName = factory.getAttribute("class"); //$NON-NLS-1$
				Object execExt = factory.createExecutableExtension("class"); //$NON-NLS-1$ - attribute name
				Class<?> clazz = execExt.getClass();
				if (AptPlugin.getJava6ProcessorClass().isInstance(execExt)){
					assert(clazz.getName().equals(factoryName));
					IServiceFactory isf = new ClassServiceFactory(clazz);
					PLUGIN_JAVA6_FACTORY_MAP.put( factoryName, isf );
					pfc.addFactoryName(factoryName, AptPlugin.JAVA6_FACTORY_NAME);
				}
				else {
					reportFailureToLoadProcessor(null, factoryName, extension.getNamespaceIdentifier());
				}
			} catch(CoreException e) {
				reportFailureToLoadProcessor(e, factoryName, extension.getNamespaceIdentifier());
			}
		}
	}

	private static void loadJava5Factories(IExtension extension, IConfigurationElement factories) {
		// Get enableDefault.  If the attribute is missing, default to true.
		String enableDefaultStr = factories.getAttribute("enableDefault"); //$NON-NLS-1$
		boolean enableDefault = true;
		if ("false".equals(enableDefaultStr)) { //$NON-NLS-1$
			enableDefault = false;
		}

		// Create and cache a PluginFactoryContainer for this plugin.
		String pluginId = extension.getNamespaceIdentifier();
		PluginFactoryContainer pfc = new PluginFactoryContainer(pluginId, enableDefault);
		PLUGIN_CONTAINER_MAP.put(pluginId, pfc);

		// Iterate over the children of the "factories" element to find all the ones named "factory".
		for (IConfigurationElement factory : factories.getChildren()) {
			if (!"factory".equals(factory.getName())) { //$NON-NLS-1$
				continue;
			}
			String factoryName = null;
			try {
				factoryName = factory.getAttribute("class"); //$NON-NLS-1$
				Object execExt = factory.createExecutableExtension("class"); //$NON-NLS-1$ - attribute name
				if (execExt instanceof AnnotationProcessorFactory){
					assert(execExt.getClass().getName().equals(factoryName));
					PLUGIN_JAVA5_FACTORY_MAP.put( factoryName, (AnnotationProcessorFactory)execExt );
					pfc.addFactoryName(factoryName, AptPlugin.JAVA5_FACTORY_NAME);
				}
				else {
					reportFailureToLoadProcessor(null, factory.getName(), extension.getNamespaceIdentifier());
				}
			} catch(CoreException e) {
				reportFailureToLoadProcessor(e, factory.getName(), extension.getNamespaceIdentifier());
			}
		}
	}

	private static void reportFailureToLoadProcessor(Exception e, String factoryName, String pluginId) {
		AptPlugin.log(e, "Unable to load annotation processor "+ factoryName + //$NON-NLS-1$
				" from plug-in " + pluginId); //$NON-NLS-1$
	}


}
