/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc and others.
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
 *    Fabian Steeg <steeg@hbz-nrw.de> - Update APT options documentation - https://bugs.eclipse.org/515329
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

	public static String AptConfigurationBlock_enableReconcileProcessing;

	public static String AptConfigurationBlock_generatedSrcDir;

	public static String AptConfigurationBlock_generatedTestSrcDir;

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

	public static String AptConfigurationBlock_warningIgnoredOptions;

	public static String FactoryPathConfigurationBlock_unableToSaveFactorypath_title;

	public static String FactoryPathConfigurationBlock_unableToSaveFactorypath_message;

	public static String FactoryPathConfigurationBlock_edit;

	public static String AptConfigurationBlock_add;

	public static String AptConfigurationBlock_edit;

	public static String AptConfigurationBlock_remove;

	public static String AptConfigurationBlock_key;

	public static String AptConfigurationBlock_value;

	public static String ProcessorOptionInputDialog_newProcessorOption;

	public static String ProcessorOptionInputDialog_editProcessorOption;

	public static String ProcessorOptionInputDialog_key;

	public static String ProcessorOptionInputDialog_value;

	public static String ProcessorOptionInputDialog_emptyKey;

	public static String ProcessorOptionInputDialog_keyAlreadyInUse;

	public static String ProcessorOptionInputDialog_equalsSignNotValid;

	public static String AptConfigurationBlock_genSrcDirMustBeValidRelativePath;

	public static String AptConfigurationBlock_genTestSrcDirMustBeValidRelativePath;

	public static String AptConfigurationBlock_genTestSrcDirMustBeDifferent;

	public static String FactoryPathConfigurationBlock_advanced;

	public static String AdvancedFactoryPathOptionsDialog_advancedOptions;

	public static String AdvancedFactoryPathOptionsDialog_batchMode;

	public static String AdvancedFactoryPathOptionsDialog_label_processorsInThisContainer;

	public static String AptConfigurationBlock_warningContentsMayBeDeleted;
}
