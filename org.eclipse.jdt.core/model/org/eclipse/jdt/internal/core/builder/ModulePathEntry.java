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
package org.eclipse.jdt.internal.core.builder;

import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleEnvironment;
import org.eclipse.jdt.internal.compiler.env.IModulePathEntry;
import org.eclipse.jdt.internal.compiler.env.IPackageLookup;
import org.eclipse.jdt.internal.compiler.env.ITypeLookup;

/**
 * Represents a project on the module path.
 */
public class ModulePathEntry implements IModulePathEntry {

	private IPath path;
	/*private*/ ClasspathLocation[] locations;
	IModule module;
	IModuleEnvironment env = null;

	class ModuleEnvironment implements IModuleEnvironment {
		@Override
		public ITypeLookup typeLookup() {
			//
			return Stream.of(ModulePathEntry.this.locations).map(loc -> loc.typeLookup()).reduce(ITypeLookup::chain).orElse(ITypeLookup.Dummy);
		}

		@Override
		public IPackageLookup packageLookup() {
			// 
			return name -> Stream.of(ModulePathEntry.this.locations).map(loc -> loc.packageLookup()).anyMatch(p -> p.isPackage(name));
		}
	}

	ModulePathEntry(IPath path, IModule module, ClasspathLocation[] locations) {
		this.path = path;
		this.locations = locations;
		this.module = module;
		initializeModule();
	}
	public IPath getPath() {
		return this.path;
	}
	public ClasspathLocation[] getClasspathLocations() {
		return this.locations;
	}

	@Override
	public IModule getModule() {
		//
		return this.module;
	}

	@Override
	public IModuleEnvironment getLookupEnvironment() {
		//
		if (this.env == null)
			this.env = new ModuleEnvironment();
		return this.env;
	}

	@Override
	public IModuleEnvironment getLookupEnvironmentFor(IModule mod) {
		//
		if (this.module.equals(mod)) {
			return getLookupEnvironment();
		}
		return null;
	}
	// TODO: This is only needed because SourceFile.module() uses the module set on the location
	// Once we have a mechanism to map a folder to a module path entry, this should no longer be
	// needed
	private void initializeModule() {
		for (int i = 0; i < this.locations.length; i++) {
			this.locations[i].setModule(this.module);
		}
	}
}
