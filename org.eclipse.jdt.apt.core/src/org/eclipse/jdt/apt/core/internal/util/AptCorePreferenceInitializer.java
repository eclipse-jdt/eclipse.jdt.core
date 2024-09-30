/*******************************************************************************
 * Copyright (c) 2005, 2011 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.util;

import java.util.Map;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;

/**
 * This class is used to extend org.eclipse.core.runtime.preferences.
 */
public class AptCorePreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
	 	IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(AptPlugin.PLUGIN_ID);
	 	for (Map.Entry<String,String> entry : AptPreferenceConstants.DEFAULT_OPTIONS_MAP.entrySet()) {
	 		defaultPreferences.put(entry.getKey(), entry.getValue());
	 	}
	}

}
