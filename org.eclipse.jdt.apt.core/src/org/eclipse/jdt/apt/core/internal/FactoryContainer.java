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

import java.util.List;

public abstract class FactoryContainer
{
	public enum FactoryType {
		PLUGIN, JAR;
	}
	
	/**	
	 * Returns an ID that is guaranteed to be sufficiently unique for this container --
	 * that is, all necessary state can be reconstructed from just the id.
	 * For plugins, it's the plugin id, for jar files, the path to the jar, etc.
	 */
	public abstract String getId();
	
	/**
	 * This method is used to display the container in the UI.
	 * If this default implementation is not adequate for a particular
	 * container, that container should provide an override.
	 */
	@Override
	public String toString() {
		return getId();
	}
	
	public abstract FactoryType getType();
	
	protected abstract List<String> loadFactoryNames();
	
	protected List<String> _factoryNames;
	
	public List<String> getFactoryNames() 
	{ 
		if ( _factoryNames == null )
			_factoryNames = loadFactoryNames();
		return _factoryNames;
	}
	
	@Override
	public int hashCode() {
		return getType().hashCode() ^ getId().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FactoryContainer)) {
			return false;
		}

		FactoryContainer other = (FactoryContainer) o;
		return other.getType() == getType() && other.getId().equals(getId());
	}
}

