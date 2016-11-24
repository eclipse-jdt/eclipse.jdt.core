/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Represents an entry on the module path of a project. Could be a single module or a collection of
 * modules (like a jimage or an exploded module directory structure)
 *
 */
public interface IModulePathEntry {

	/**
	 * Get the module that this entry contributes. May be null, for instance when this entry does not
	 * represent a single module
	 * 
	 * @return The module that this entry contributes or null
	 */
	default IModule getModule() {
		return null;
	}
	
	/**
	 * Get the module named name from this entry. May be null
	 * 
	 * @param name - The name of the module to look up
	 * 
	 * @return The module named name or null
	 */
	default IModule getModule(char[] name) {
		IModule mod = getModule();
		if (mod != null && CharOperation.equals(name, mod.name()))
			return mod;
		return null;
	}

	/**
	 * Indicates whether this entry knows the module named name and can answer queries regarding the module
	 * 
	 * @param name The name of the module
	 * 
	 * @return True if this entry knows the module, false otherwise
	 */
	default boolean servesModule(char[] name) {
		return getModule(name) != null;
	}
	/**
	 * Return the look up environment for this entry. Should be used when one needs to
	 * look up types/packages in all the modules contributed by this entry
	 * 
	 */
	IModuleEnvironment getLookupEnvironment();
	/**
	 * Return the lookup environment for the given module
	 * 
	 * @param module
	 * 
	 * @return The look up environment for the module, or null if this entry
	 * does not know any such module
	 */
	IModuleEnvironment getLookupEnvironmentFor(IModule module);
	/**
	 * Specifies whether this entry represents an automatic module.
	 * 
	 * @return true if this is an automatic module, false otherwise
	 */
	public default boolean isAutomaticModule() {
		return false;
	}
}