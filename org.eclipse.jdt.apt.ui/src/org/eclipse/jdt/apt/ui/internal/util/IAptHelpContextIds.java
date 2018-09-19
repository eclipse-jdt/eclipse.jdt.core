/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.util;

import org.eclipse.jdt.apt.ui.internal.AptUIPlugin;

/**
 * Help context ids for the Java annotation processing UI.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 */
public interface IAptHelpContextIds {
	public static final String PREFIX= AptUIPlugin.PLUGIN_ID + '.';

	// Dialogs
	public static final String ADVANCED_FACTORYPATH_OPTIONS_DIALOG= 	PREFIX + "advanced_factory_path_options_dialog_context"; //$NON-NLS-1$
	public static final String PROCESSOR_OPTION_INPUT_DIALOG= 			PREFIX + "processor_option_input_dialog_context"; //$NON-NLS-1$

	// Preference/Property pages
	public static final String APTCONFIGURATION_PREFERENCE_PAGE=		PREFIX + "apt_configuration_preference_page_context"; //$NON-NLS-1$
	public static final String FACTORYPATH_PREFERENCE_PAGE= 			PREFIX + "factory_path_preference_page_context"; //$NON-NLS-1$
}
