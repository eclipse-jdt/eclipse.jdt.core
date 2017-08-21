/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
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


import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.AutomaticModuleNaming;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModulePathEntry;
import org.eclipse.jdt.internal.compiler.lookup.AutoModule;

/**
 * Represents a project on the module path.
 */
public class ModulePathEntry implements IModulePathEntry {

	private IPath path;
	/*private*/ ClasspathLocation[] locations;
	IModule module;
	boolean isAutomaticModule;

	ModulePathEntry(IPath path, IModule module, ClasspathLocation[] locations) {
		this.path = path;
		this.locations = locations;
		this.module = module;
		this.isAutomaticModule = module.isAutomatic();
		initializeModule();
	}
	public ModulePathEntry(IPath path, ClasspathLocation location) {
		this.path = path;
		initModule(location);
		this.locations = new ClasspathLocation[] {location};
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

	public boolean isAutomaticModule() {
		return this.isAutomaticModule;
	}
	public static char[] getAutomaticModuleName(ClasspathLocation location) {
		if (location instanceof ClasspathJar) {
			return AutomaticModuleNaming.determineAutomaticModuleName(((ClasspathJar) location).zipFilename);
		}
		if (location instanceof ClasspathDirectory) {
			return ((ClasspathDirectory) location).binaryFolder.getName().toCharArray();
		}
		return null;
	}
	private void initModule(ClasspathLocation location) {
		IModule mod = null;
		if (location instanceof ClasspathJar) {
			mod = ((ClasspathJar) location).initializeModule();
		} else if (location instanceof ClasspathDirectory){
			mod = ((ClasspathDirectory) location).initializeModule();
		}
		if (mod != null) {
			this.module = mod;
			this.isAutomaticModule = false;
		} else {
			this.module = new AutoModule(getAutomaticModuleName(location));
			this.isAutomaticModule = true;
		}
		location.setModule(this.module);
	}

	// TODO: This is only needed because SourceFile.module() uses the module set on the location
	// Once we have a mechanism to map a folder to a module path entry, this should no longer be
	// needed
	private void initializeModule() {
		for (int i = 0; i < this.locations.length; i++) {
			this.locations[i].setModule(this.module);
		}
	}
	@Override
	public char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		if (moduleName != null && ((this.module == null) || !moduleName.equals(String.valueOf(this.module.name()))))
			return null;
		// search all locations
		char[][] names = CharOperation.NO_CHAR_CHAR;
		for (ClasspathLocation cp : this.locations) {
			char[][] declaringModules = cp.getModulesDeclaringPackage(qualifiedPackageName, moduleName);
			if (declaringModules != null)
				names = CharOperation.arrayConcat(names, declaringModules);
		}
		return names == CharOperation.NO_CHAR_CHAR ? null : names;
	}
	@Override
	public boolean hasCompilationUnit(String qualifiedPackageName, String moduleName) {
		for (ClasspathLocation cp : this.locations) {
			if (cp.hasCompilationUnit(qualifiedPackageName, moduleName))
				return true;
		}
		return false;
	}
}
