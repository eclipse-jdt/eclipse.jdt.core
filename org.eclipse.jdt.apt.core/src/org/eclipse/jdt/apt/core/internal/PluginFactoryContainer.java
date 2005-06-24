/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.core.internal;

import java.util.ArrayList;
import java.util.List;

public class PluginFactoryContainer extends FactoryContainer
{
	/** The label of the plug that owns this factory container.  */
	private final String id;
	
	public PluginFactoryContainer(final String className) {
		this.id = className;
	}
	
	public boolean isPlugin() { return true; }
	public void addFactoryName( String n ) { getFactoryNames().add( n ); }
	protected List<String> loadFactoryNames() { 
		return new ArrayList<String>();
	}
	
	public String getId() {
		return id;
	}
	
	public String toString() {
		return getId();
	}

	@Override
	public FactoryType getType() {
		return FactoryType.PLUGIN;
	}
}
