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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Accesses configuration data for APT.
 * Note that some of the code in org.eclipse.jdt.ui reads and writes settings
 * data directly, rather than calling into the methods of this class. 
 * 
 * TODO: synchronization of maps
 * TODO: NLS
 * TODO: rest of settings
 */
public class AptConfig {
	/**
	 * Holds the options maps for each project.
	 */
	private static Map<IJavaProject, Map> _optionsMaps = new HashMap<IJavaProject, Map>(5);
	
	private static final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];
	private static final int PREF_INSTANCE = 0;
	private static final int PREF_DEFAULT = 1;

	/**
	 * Update the factory list and other apt settings
	 */
	private static class EclipsePreferencesListener implements IEclipsePreferences.IPreferenceChangeListener {
		/**
		 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
		 */
		public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
			// TODO: something, anything
		}
	}
	
	/**
	 * Is annotation processing turned on for this project?
	 * @param jproject an IJavaProject, or null to request workspace preferences.
	 * @return
	 */
	public static synchronized boolean isEnabled(IJavaProject jproject) {
		Map options = getOptions(jproject);
		return "true".equals(options.get(AptPreferenceConstants.APT_ENABLED));
	}
	
	/**
	 * Turn annotation processing on or off for this project.
	 * TODO: what is the persistence model?  At present, it probably gets blown away
	 * as soon as any other setting changes, and it never actually gets stored to disk.
	 * @param jproject an IJavaProject, or null to set workspace preferences.
	 * @param enabled
	 */
	public static synchronized void setEnabled(IJavaProject jproject, boolean enabled) {
		Map options = _optionsMaps.get(jproject);
		options.put(AptPreferenceConstants.APT_ENABLED, enabled ? "true" : "false");
	}

	/**
	 * Return the apt settings for this project, or the workspace settings
	 * if they are not overridden by project settings.
	 * TODO: should jproject be allowed to be NULL?
	 * @param jproject
	 * @return
	 */
	private static Map getOptions(IJavaProject jproject) {
		Map options = _optionsMaps.get(jproject);
		if (null != options) {
			return options;
		}
		// We didn't already have an options map for this project, so create one.
		options = new HashMap(AptPreferenceConstants.NSETTINGS);

		// First load workspace-wide, then overlay per-project options if possible.
		loadWorkspaceOptions(options);
		if (jproject.getProject() != null) {
			IScopeContext context = new ProjectScope(jproject.getProject());
			final IEclipsePreferences eclipsePreferences = context.getNode(AptPlugin.PLUGIN_ID);
			try {
				for (String s : eclipsePreferences.keys()) {
					options.put(s, eclipsePreferences.get(s, ""));
				}
			}
			catch (BackingStoreException e) {
				// TODO
				e.printStackTrace();
			}
		}
		return options;
	}

	/**
	 * @param options
	 */
	private static void loadWorkspaceOptions(Map options) {
		IPreferencesService service = Platform.getPreferencesService();

		// set options using preferences service lookup
		for (String optionName : AptPreferenceConstants.OPTION_NAMES) {
		    String value = service.get(optionName, null, preferencesLookup);
		    if (value != null) {
			    options.put(optionName, value);
		    }
		}
	}
	
	/**
	 * Initialize preferences lookups, and register change listeners.
	 * This is called when the APT plugin is loaded.
	 * TODO: the whole change-listener thing is still just copied and pasted from JDT without comprehension.
	 */
	public static void initialize() {
		
		// Create lookups
		preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(AptPlugin.PLUGIN_ID);
		// Calling this line will cause AptCorePreferenceInitializer to run,
		// via the runtime.preferences extension point.
		preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(AptPlugin.PLUGIN_ID);

/* TODO: figure out listeners
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

	
}
