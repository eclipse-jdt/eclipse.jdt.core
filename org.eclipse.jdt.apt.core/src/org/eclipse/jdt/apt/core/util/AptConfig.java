/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com, wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.util;

import java.io.IOException;
import java.util.*;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.PluginFactoryContainer;
import org.eclipse.jdt.apt.core.internal.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Accesses configuration data for APT.
 * Note that some of the code in org.eclipse.jdt.ui reads and writes settings
 * data directly, rather than calling into the methods of this class. 
 * 
 * Helpful information about the Eclipse preferences mechanism can be found at:
 * http://dev.eclipse.org/viewcvs/index.cgi/~checkout~/platform-core-home/documents/user_settings/faq.html
 * 
 * TODO: synchronization of maps
 * TODO: NLS
 * TODO: rest of settings
 * TODO: optimize performance on projects that do not have project-specific settings.
 */
public class AptConfig {
	/**
	 * Update the factory list and other apt settings
	 */
	private static class EclipsePreferencesListener implements IEclipsePreferences.IPreferenceChangeListener {
		/**
		 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
		 */
		public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
			// Reset our factory loader
			AnnotationProcessorFactoryLoader.getLoader().reset();
			
			// TODO: something, anything
		}
	}
	
	/**
	 * A guess at how many projects in the workspace will have 
	 * per-project settings for apt.  Used to set initial size of some maps.
	 */
	private static final int INITIAL_PROJECTS_GUESS = 5;
	
	/**
	 * Holds the options maps for each project.
	 */
	private static Map<IJavaProject, Map<String, String>> _optionsMaps = 
		new HashMap<IJavaProject, Map<String, String>>(INITIAL_PROJECTS_GUESS);
	
	private static final String FACTORYPATH_FILE = ".factorypath";
	
	/**
	 * Returns all containers for the provided project, including disabled ones.
	 * @param jproj The java project in question, or null for the workspace
	 * @return an ordered map, where the key is the container and the value 
	 * indicates whether the container is enabled.
	 */
	public static synchronized Map<FactoryContainer, Boolean> getAllContainers(IJavaProject jproj) {
		Map<FactoryContainer, Boolean> containers = null;
		if (jproj != null) {
			try {
				containers = FactoryPathUtil.readFactoryPathFile(jproj);
			}
			catch (CoreException ce) {
				ce.printStackTrace();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		// Workspace if no project data was found
		if (containers == null) {
			try {
				containers = FactoryPathUtil.readFactoryPathFile(null);
			}
			catch (CoreException ce) {
				ce.printStackTrace();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		// if no project and no workspace data was found, we'll get the defaults
		if (containers == null) {
			containers = new LinkedHashMap<FactoryContainer, Boolean>();
		}
		boolean disableNewPlugins = jproj != null;
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
		List<PluginFactoryContainer> pluginContainers = FactoryPathUtil.getAllPluginFactoryContainers();
		
		// Remove any plugin factories whose plugins we did not find
		for (Iterator<FactoryContainer> containerIter = containers.keySet().iterator(); containerIter.hasNext(); ) {
			FactoryContainer container = containerIter.next();
			if (container.getType() == FactoryType.PLUGIN && !pluginContainers.contains(container)) {
				containerIter.remove();
			}
		}
		
		// Add any plugins which are new since the config was last saved
		for (PluginFactoryContainer pluginContainer : pluginContainers) {
			if (!containers.containsKey(pluginContainer)) {
				//TODO: process "disableNewPlugins"
				containers.put(pluginContainer, true);
			}
		}
	}
	
	/**
	 * Get the factory containers for this project. If no project-level configuration
	 * is set, the workspace config will be returned. Any disabled containers
	 * will not be returned.
	 * 
	 * @param jproj The java project in question. 
	 * @return an ordered list of all enabled factory containers.
	 */
	public static synchronized List<FactoryContainer> getEnabledContainers(IJavaProject jproj) {
		// this map is ordered.
		Map<FactoryContainer, Boolean> containers = getAllContainers(jproj);
		List<FactoryContainer> result = new ArrayList<FactoryContainer>(containers.size());
		for (Map.Entry<FactoryContainer, Boolean> entry : containers.entrySet()) {
			if (entry.getValue()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}
	    
	/**
     * Add the equivalent of -Akey=val to the list of processor options.
     * @param key must be a nonempty string.  It should only include the key;
     * that is, it should not start with "-A".
     * @param jproj a project, or null to set the option workspace-wide.
     * @param val can be null (equivalent to -Akey).  This does not mean
     * remove the key; for that functionality, @see #removeProcessorOption(IJavaProject, String).
     * @return the old value, or null if the option was not previously set.
     */
    public static synchronized String addProcessorOption(IJavaProject jproj, String key, String val) {
    	Map<String, String> options = getProcessorOptions(jproj);
    	String old = options.get(key);
    	options.put(key, val);
    	String serializedOptions = serializeProcessorOptions(options);
    	setString(jproj, AptPreferenceConstants.APT_PROCESSOROPTIONS, serializedOptions);
    	return old;
    }
	
	/**
     * Remove an option from the list of processor options.
     * @param jproj a project, or null to remove the option workspace-wide.
     * @param key must be a nonempty string.  It should only include the key;
     * that is, it should not start with "-A".
     * @return the old value, or null if the option was not previously set.
     */
    public static synchronized String removeProcessorOption(IJavaProject jproj, String key) {
    	Map<String, String> options = getProcessorOptions(jproj);
    	String old = options.get(key);
    	options.remove(key);
    	String serializedOptions = serializeProcessorOptions(options);
    	setString(jproj, AptPreferenceConstants.APT_PROCESSOROPTIONS, serializedOptions);
    	return old;
    }
    
	/**
     * Get the options that are the equivalent of the -A command line options
     * for apt.  The -A and = are stripped out, so (key, value) is the
     * equivalent of -Akey=value.  
     * 
     * The implementation of this at present relies on persisting all the options
     * in one string that is the equivalent of the apt command line, e.g.,
     * "-Afoo=bar -Aquux=baz", and then parsing the string into a map in this
     * routine. 
     * 
     * @param jproj a project, or null to query the workspace-wide setting.
     * @return a mutable, possibly empty, map of (key, value) pairs.  
     * The value part of a pair may be null (equivalent to "-Akey").
     */
    public static Map<String, String> getProcessorOptions(IJavaProject jproj) {
    	Map<String, String> options = new HashMap<String, String>();
    	String allOptions = getString(jproj, AptPreferenceConstants.APT_PROCESSOROPTIONS);
    	if (null == allOptions) {
    		return options;
    	}
    	String[] parsedOptions = allOptions.split(" ");
    	for (String keyAndVal : parsedOptions) {
    		if (!keyAndVal.startsWith("-A")) {
    			continue;
    		}
    		String[] parsedKeyAndVal = keyAndVal.split("=", 2);
    		if (parsedKeyAndVal.length > 0) {
    			String key = parsedKeyAndVal[0].substring(2);
    			if (key.length() < 1) {
    				continue;
    			}
    			if (parsedKeyAndVal.length == 1) {
    				options.put(key, null);
    			}
    			else {
    				options.put(key, parsedKeyAndVal[1]);
    			}
    		}
    	}
    	return options;
    }


	/**
	 * Initialize preferences lookups, and register change listeners.
	 * This is called once, from AptPlugin.start().
	 * TODO: the whole change-listener thing is still just copied and pasted from JDT without comprehension.
	 */
	public static void initialize() {
		/* TODO: figure out listeners - here's some stolen sample code for ideas:
		
		// Create lookups
		preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(AptPlugin.PLUGIN_ID);
		// Calling this line will cause AptCorePreferenceInitializer to run,
		// via the runtime.preferences extension point.
		preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(AptPlugin.PLUGIN_ID);

		// Listen to instance preferences node removal from parent in order to refresh stored one
		IEclipsePreferences.INodeChangeListener listener = new IEclipsePreferences.INodeChangeListener() {
			public void added(IEclipsePreferences.NodeChangeEvent event) {
				// do nothing
			}
			public void removed(IEclipsePreferences.NodeChangeEvent event) {
				if (event.getChild() == preferencesLookup[PREF_INSTANCE]) {
					preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(AptPlugin.PLUGIN_ID);
					preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
				}
			}
		};
		((IEclipsePreferences) preferencesLookup[PREF_INSTANCE].parent()).addNodeChangeListener(listener);
		preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());

		// Listen to default preferences node removal from parent in order to refresh stored one
		listener = new IEclipsePreferences.INodeChangeListener() {
			public void added(IEclipsePreferences.NodeChangeEvent event) {
				// do nothing
			}
			public void removed(IEclipsePreferences.NodeChangeEvent event) {
				if (event.getChild() == preferencesLookup[PREF_DEFAULT]) {
					preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(AptPlugin.PLUGIN_ID);
				}
			}
		};
		((IEclipsePreferences) preferencesLookup[PREF_DEFAULT].parent()).addNodeChangeListener(listener);
*/
	}
	
	/**
	 * Is annotation processing turned on for this project?
	 * @param jproject an IJavaProject, or null to request workspace preferences.
	 * @return
	 */
	public static boolean isEnabled(IJavaProject jproject) {
		return getBoolean(jproject, AptPreferenceConstants.APT_ENABLED);
	}
	
	/**
	 * Turn annotation processing on or off for this project.
	 * @param jproject an IJavaProject, or null to set workspace preferences.
	 * @param enabled
	 */
	public static void setEnabled(IJavaProject jproject, boolean enabled) {
		setBoolean(jproject, AptPreferenceConstants.APT_ENABLED, enabled);
	}
	
	private static synchronized boolean getBoolean(IJavaProject jproject, String optionName) {
		Map options = getOptions(jproject);
		return "true".equals(options.get(optionName));
	}
	
    /**
	 * Return the apt settings for this project, or the workspace settings
	 * if they are not overridden by project settings.
	 * TODO: should jproject be allowed to be NULL?
	 * TODO: efficiently handle the case of projects that don't have per-project settings
	 * (e.g., only cache one workspace-wide map, not a separate copy for each project).
	 * @param jproject
	 * @return
	 */
	private static Map getOptions(IJavaProject jproject) {
		Map options = _optionsMaps.get(jproject);
		if (null != options) {
			return options;
		}
		// We didn't already have an options map for this project, so create one.
		IPreferencesService service = Platform.getPreferencesService();
		// Don't need to do this, because it's the default-default already:
		//service.setDefaultLookupOrder(AptPlugin.PLUGIN_ID, null, lookupOrder);
		options = new HashMap(AptPreferenceConstants.NSETTINGS);
		if (jproject != null) {
			IScopeContext projContext = new ProjectScope(jproject.getProject());
			IScopeContext[] contexts = new IScopeContext[] { projContext };
			for (String optionName : AptPreferenceConstants.OPTION_NAMES) {
				String val = service.getString(AptPlugin.PLUGIN_ID, optionName, null, contexts);
				if (val != null) {
					options.put(optionName, val);
				}
			}
		}
		else {
			// TODO: do we need to handle this case?
			return null;
		}
		
		return options;
	}

    private static synchronized String getString(IJavaProject jproject, String optionName) {
		Map options = getOptions(jproject);
		return (String)options.get(optionName);
	}
	
    /**
     * Save processor (-A) options as a string.  Option key/val pairs will be
     * serialized as -Akey=val, and key/null pairs as -Akey.  Options are
     * space-delimited.  The result resembles the apt command line.
     * @param options a map containing zero or more key/value or key/null pairs.
     */
    private static String serializeProcessorOptions(Map<String, String> options) {
    	StringBuilder sb = new StringBuilder();
    	boolean firstEntry = true;
    	for (Map.Entry<String, String> entry : options.entrySet()) {
    		if (firstEntry) {
    			firstEntry = false;
        		sb.append("-A");
    		}
    		else {
    			sb.append(" -A");
    		}
    		sb.append(entry.getKey());
    		if (entry.getValue() != null) {
    			sb.append("=");
    			sb.append(entry.getValue());
    		}
    	}
    	return sb.toString();
    }
	
	private static synchronized void setBoolean(IJavaProject jproject, String optionName, boolean value) {
		// TODO: should we try to determine whether a project has no per-project settings,
		// and if so, set the workspace settings?  Or, do we want the caller to tell us
		// explicitly by setting jproject == null in that case?
		
		// TODO: when there are listeners, the following two lines will be superfluous:
		Map options = getOptions(jproject);
		options.put(AptPreferenceConstants.APT_ENABLED, value ? "true" : "false");
		
		IScopeContext context;
		if (null != jproject) {
			context = new ProjectScope(jproject.getProject());
		}
		else {
			context = new InstanceScope();
		}
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID);
		node.putBoolean(optionName, value);
	}
	
	private static synchronized void setString(IJavaProject jproject, String optionName, String value) {
		// TODO: when there are listeners, the following two lines will be superfluous:
		Map options = getOptions(jproject);
		options.put(optionName, value);
		
		IScopeContext context;
		if (null != jproject) {
			context = new ProjectScope(jproject.getProject());
		}
		else {
			context = new InstanceScope();
		}
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID);
		node.put(optionName, value);
	}

    /**
	 * Set or reset the factory containers for a given project or the workspace.
	 * @param jproj the java project, or null for the workspace
	 * @param containers an ordered map whose key is a factory container and
	 * whose value indicates whether the container's factories are enabled;
	 * or null, to restore defaults.
	 */
	public static synchronized void setContainers(IJavaProject jproj, Map<FactoryContainer, Boolean> containers) 
	throws IOException, CoreException 
	{
		FactoryPathUtil.saveFactoryPathFile(jproj, containers);
		//TODO: we probably want to use the PropertyChangeListener mechanism for this.
		AnnotationProcessorFactoryLoader.getLoader().reset();
	}

	/**
	 * Has an explicit factory path been set for the specified project, or
	 * is it just defaulting to the workspace settings? 
	 * @param project
	 * @return true if there is a project-specific factory path.
	 */
	public static boolean hasProjectSpecificFactoryPath(IJavaProject jproj) {
		if (null == jproj) {
			// say no, even if workspace-level factory path does exist. 
			return false;
		}
		return FactoryPathUtil.doesFactoryPathFileExist(jproj);
	}
	
}
