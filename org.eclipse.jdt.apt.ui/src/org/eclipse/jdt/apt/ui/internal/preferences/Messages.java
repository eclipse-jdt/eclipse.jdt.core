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
package org.eclipse.jdt.apt.ui.internal.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.apt.ui.internal.preferences.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String AptConfigurationBlock_enable;

	public static String AptConfigurationBlock_generatedSrcDir;

	public static String AptConfigurationBlock_options;

	public static String FactoryPathConfigurationBlock_up;

	public static String FactoryPathConfigurationBlock_down;

	public static String FactoryPathConfigurationBlock_addJars;

	public static String FactoryPathConfigurationBlock_addExternalJars;

	public static String FactoryPathConfigurationBlock_addVariable;

	public static String FactoryPathConfigurationBlock_remove;

	public static String FactoryPathConfigurationBlock_enableAll;

	public static String FactoryPathConfigurationBlock_disableAll;

	public static String FactoryPathConfigurationBlock_pluginsAndJars;

	public static String FactoryPathPreferencePage_factoryPath;

	public static String FactoryPathPreferencePage_preferences;

	public static String AptPreferencePage_preferences;

	public static String AptPreferencePage_preferencesTitle;

	public static String BaseConfigurationBlock_settingsChanged;

	public static String BaseConfigurationBlock_fullRebuildRequired;

	public static String BaseConfigurationBlock_rebuildRequired;

	public static String AptConfigurationBlock_classpathAddedAutomaticallyNote;

	public static String AptConfigurationBlock_warningIgnoredOptions;

	public static String FactoryPathConfigurationBlock_unableToSaveFactorypath_title;

	public static String FactoryPathConfigurationBlock_unableToSaveFactorypath_message;
}
