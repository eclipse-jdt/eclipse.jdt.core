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
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModule.IModuleReference;
import org.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;

public class ModuleBinding extends Binding {

	public char[] moduleName;
	public IModuleReference[] requires;
	public IPackageExport[] exportedPackages;
	public TypeBinding[] uses;
	public TypeBinding[] services;
	public TypeBinding[] implementations;
	public CompilationUnitScope scope;
	public LookupEnvironment environment;
	public int tagBits;
	private ModuleBinding[] requiredModules = null;

	public boolean isBinary = false;

	public static ModuleBinding[] NO_REQUIRES = new ModuleBinding[0];
	public static IModuleReference[] NO_MODULE_REFS = new IModuleReference[0];
	public static IPackageExport[] NO_EXPORTS = new IPackageExport[0];

	static ModuleBinding UnNamedModule = new ModuleBinding(); 

	private ModuleBinding() {
		this.moduleName = ModuleEnvironment.UNNAMED;
	}
	public ModuleBinding(IModule module, LookupEnvironment environment) {
		this.moduleName = module.name();
		this.requires = module.requires();
		if (this.requires == null)
			this.requires = NO_MODULE_REFS;
		this.exportedPackages = module.exports();
		if (this.exportedPackages == null)
			this.exportedPackages = NO_EXPORTS;
		this.environment = environment;
		this.uses = Binding.NO_TYPES;
		this.services = Binding.NO_TYPES;
		this.implementations = Binding.NO_TYPES;
		this.isBinary = true;
	}

	public ModuleBinding(CompilationUnitScope scope) {
		this.scope = scope;
		this.environment = scope.environment;
		this.isBinary = false;
		this.uses = Binding.NO_TYPES;
		this.services = Binding.NO_TYPES;
		this.implementations = Binding.NO_TYPES;
	}

	public boolean isBinary() {
		return this.isBinary;
	}
	private void getImplicitDependencies(ModuleBinding module, Set<ModuleBinding> deps) {
		if (module == UnNamedModule)
			return;
		if (module.isBinary()) {
			getImplicitDependencies(module.requires, deps);
		} else {
			getImplicitDependencies(module.scope.referenceContext.moduleDeclaration.requires, deps);
		}
	}
	private void getImplicitDependencies(IModule.IModuleReference[] reqs, Set<ModuleBinding> deps) {
		if (reqs != null && reqs.length > 0) {
			for (IModule.IModuleReference ref : reqs) {
				ModuleBinding refModule = this.environment.getModule(ref.name());
				if (refModule != null) {
					if (deps.add(refModule))
						getImplicitDependencies(refModule.requires, deps);
				}
			}
		}
	}

	private void getImplicitDependencies(ModuleReference[] refs, Set<ModuleBinding> deps) {
		if (refs != null && refs.length > 0) {
			for (ModuleReference ref : refs) {
				ModuleBinding refModule = this.environment.getModule(ref.moduleName);
				if (refModule != null) {
					if (deps.add(refModule))
						getImplicitDependencies(refModule, deps);
				}
			}
		}
	}
	/**
	 * Collect all implicit dependencies offered by this module
	 * Any module dependent on this module will have an implicit dependence on all other modules
	 * specified as ' requires public '
	 * @return
	 *  collection of implicit dependencies
	 */
	public Collection<ModuleBinding> getImplicitDependencies() {
		Set<ModuleBinding> dependencies = new HashSet<ModuleBinding>();
		getImplicitDependencies(this, dependencies);
		return dependencies;
	}

	/**
	 * get all the modules required by this module
	 * All required modules include modules explicitly specified as required in the module declaration
	 * as well as implicit dependencies - those specified as ' requires public ' by one of the
	 * dependencies
	 * 
	 * @return
	 *   An array of all required modules
	 */
	public ModuleBinding[] getAllRequiredModules() {
		if (this == UnNamedModule)
			return NO_REQUIRES;
		if (this.requiredModules != null)
			return this.requiredModules;
		Set<ModuleBinding> allRequires = new HashSet<ModuleBinding>();
		for (int i = 0; i < this.requires.length; i++) {
			ModuleBinding mod = this.environment.getModule(this.requires[i].name());
			if (mod != null) {
				allRequires.add(mod);
				allRequires.addAll(mod.getImplicitDependencies());
			}
		}
		if (!CharOperation.equals(this.moduleName, TypeConstants.JAVA_BASE)) {
			// TODO: Do we need to add java.base here?
			allRequires.add(this.environment.getModule(JRTUtil.JAVA_BASE_CHAR));
		}
		return this.requiredModules = allRequires.size() > 0 ? allRequires.toArray(new ModuleBinding[allRequires.size()]) : NO_REQUIRES;
	}

	public char[] name() {
		return this.moduleName;
	}
	public boolean isPackageVisible(PackageBinding pkg, Scope skope) {
		ModuleBinding other = this.environment.getModule(skope.module());
		return isPackageExportedTo(pkg, this, other);
	}

	public boolean isPackageExported(char[] pkgName) {
		for (IPackageExport export : this.exportedPackages) {
			if (CharOperation.prefixEquals(pkgName, export.name()))
				return true;
		}
		return false;
	}
	/**
	 * Check if the specified package is exported to the client module
	 * @param pkg - the package whose visibility is to be checked
	 * @param source - the module which 'contains' the package 
	 * @param client - the module that wishes to use the package
	 * @return true if the package is exported to the client module and the client
	 *  is dependent on the source module either directly or indirectly, false otherwise
	 */
	public static boolean isPackageExportedTo(PackageBinding pkg, ModuleBinding source, ModuleBinding client) {
		if (client == null || client == source) // same or unnamed
			return true;
		if (!client.dependsOn(source))
			return false;
		for (IPackageExport export : source.exportedPackages) {
			if (CharOperation.equals(export.name(), pkg.readableName())) {
				if (export.exportedTo() != null) {
					for (char[] target : export.exportedTo()) {
						if (CharOperation.equals(target, client.moduleName))
							return true;
					}
				}
			}
		}
		return false;
	}
 	public boolean dependsOn(ModuleBinding other) {
 		if (other == this)
 			return true;
		for (ModuleBinding ref : getAllRequiredModules()) {
			if (ref == other)
				return true;
		}
		return false;
	}
	@Override
	public int kind() {
		//
		return ClassFileConstants.AccModule;
	}

	@Override
	public char[] readableName() {
		//
		return this.moduleName;
	}
}
