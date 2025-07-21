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
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.core.internal;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;

public class PluginFactoryContainer extends FactoryContainer
{
	/** The label of the plug that owns this factory container.  */
	private final String id;

	/** Whether the plugin's factories are enabled by default */
	private final boolean enableDefault;

	/**
	 * In general clients should not construct this object.  This c'tor should
	 * only be called from @see FactoryPathUtil#loadPluginFactories().
	 */
	public PluginFactoryContainer(final String pluginId, boolean enableDefault) {
		this.id = pluginId;
		this.enableDefault = enableDefault;
	}

	public void addFactoryName( String factoryName, String serviceName ) {
		try {
			getFactoryNames().put( factoryName, serviceName );
		}
		catch (IOException ioe) {
			AptPlugin.log(ioe, "IOException reading a plugin"); //$NON-NLS-1$
		}
	}

	@Override
	public boolean exists() {
		// This object is created only in the process of loading factory plugins.
		return true;
	}

	@Override
	protected Map<String, String> loadFactoryNames() {
		// The list is populated when factory plugins are loaded.
		return new LinkedHashMap<>();
	}

	@Override
	public String getId() {
		return id;
	}

	public boolean getEnableDefault() {
		return enableDefault;
	}

	@Override
	public FactoryType getType() {
		return FactoryType.PLUGIN;
	}

}
