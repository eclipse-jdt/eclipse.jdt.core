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
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.core.internal.util;

import java.io.IOException;
import java.util.Map;

/**
 * An entry on the processor factory path.  Typically a jar, plug-in,
 * etc. that contains annotation processors.  It may contain Java 5
 * processors, Java 6 processors, both or neither.
 */
public abstract class FactoryContainer
{
	public enum FactoryType {
		PLUGIN,  // Eclipse plugin
		EXTJAR,  // external jar file (not in workspace)
		WKSPJAR, // jar file within workspace
		VARJAR;  // external jar file referenced by classpath variable
	}

	/**
	 * Returns an ID that is guaranteed to be sufficiently unique for this container --
	 * that is, all necessary state can be reconstructed from just the id and FactoryType.
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

	/**
	 * Test whether the resource that backs this container exists,
	 * can be located, and is (at least in principle) accessible for
	 * factories to be loaded from.  For instance, a plugin exists if
	 * the plugin is loaded in Eclipse; a jar exists if the jar file
	 * can be found on disk.  The test is not required to be perfect:
	 * for instance, a jar file might exist but be corrupted and
	 * therefore not really readable, but this method would still return
	 * true.
	 * @return true if the resource backing the container exists.
	 */
	public abstract boolean exists();

	/**
	 * Subclasses must return a map of implementation name to service
	 * name, for all the processor services this container provides.
	 */
	protected abstract Map<String, String> loadFactoryNames() throws IOException;

	/**
	 * Map of implementation name to service name.  For instance,
	 * "org.xyz.FooProcessor" -> "javax.annotation.processing.Processor".
	 */
	protected Map<String, String> _factoryNames;

	public Map<String, String> getFactoryNames() throws IOException
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

