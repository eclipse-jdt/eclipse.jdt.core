/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.util;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;

/**
 * This class is used to extend org.eclipse.core.runtime.preferences.
 */
public class AptCorePreferenceInitializer extends AbstractPreferenceInitializer {

	private static final String DEFAULT_GENERATED_SOURCE_FOLDER_NAME = "__generated_src"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// TODO see JavaCorePreferenceInitializer for what this should do
		// Store default values to default preferences
	 	IEclipsePreferences defaultPreferences = new DefaultScope().getNode(AptPlugin.PLUGIN_ID);
	 	defaultPreferences.put(AptPreferenceConstants.APT_ENABLED, "true"); //$NON-NLS-1$
	 	defaultPreferences.put(AptPreferenceConstants.APT_GENSRCDIR, DEFAULT_GENERATED_SOURCE_FOLDER_NAME);
	 	defaultPreferences.put(AptPreferenceConstants.APT_PROCESSOROPTIONS, ""); //$NON-NLS-1$
	}

}
