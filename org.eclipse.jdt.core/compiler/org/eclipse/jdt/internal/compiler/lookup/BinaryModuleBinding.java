/*******************************************************************************
 * Copyright (c) 2017 GK Software AG, and others.
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
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.HashMap;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModule.IModuleReference;
import org.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import org.eclipse.jdt.internal.compiler.env.IModule.IService;

public class BinaryModuleBinding extends ModuleBinding {
	
	private IPackageExport[] unresolvedExports;
	private IPackageExport[] unresolvedOpens;
	private char[][] unresolvedUses;
	private IService[] unresolvedProvides;
	
	/**
	 * Construct a named module from binary.
	 * <p>
	 * <strong>Side effects:</strong> adds the new module to root.knownModules and resolves its directives.
	 * </p>
	 */
	public BinaryModuleBinding(IModule module, LookupEnvironment existingEnvironment) {
		super(module.name(), existingEnvironment);
		existingEnvironment.root.knownModules.put(this.moduleName, this);
		cachePartsFrom(module);
	}
	
	void cachePartsFrom(IModule module) {
		this.isAuto = module.isAutomatic();
		if (module.isOpen())
			this.modifiers |= ClassFileConstants.ACC_OPEN;

		IModuleReference[] requiresReferences = module.requires();
		this.requires = new ModuleBinding[requiresReferences.length];
		this.requiresTransitive = new ModuleBinding[requiresReferences.length];
		int count = 0;
		int transitiveCount = 0;
		for (int i = 0; i < requiresReferences.length; i++) {
			ModuleBinding requiredModule = this.environment.getModule(requiresReferences[i].name());
			if (requiredModule != null) {
				this.requires[count++] = requiredModule;
				if (requiresReferences[i].isTransitive())
					this.requiresTransitive[transitiveCount++] = requiredModule;
			}
			// TODO(SHMOD): handle null case
		}
		if (count < this.requiresTransitive.length)
			System.arraycopy(this.requires, 0, this.requires = new ModuleBinding[count], 0, count);
		if (transitiveCount < this.requiresTransitive.length)
			System.arraycopy(this.requiresTransitive, 0, this.requiresTransitive = new ModuleBinding[transitiveCount], 0, transitiveCount);

		this.unresolvedExports = module.exports();
		this.unresolvedOpens = module.opens();
		this.unresolvedUses = module.uses();
		this.unresolvedProvides = module.provides();
	}
	
	@Override
	public PackageBinding[] getExports() {
		if (this.exportedPackages == null && this.unresolvedExports != null)
			resolvePackages();
		return super.getExports();
	}
	
	@Override
	public PackageBinding[] getOpens() {
		if (this.openedPackages == null && this.unresolvedOpens != null)
			resolvePackages();
		return super.getExports();
	}

	private void resolvePackages() {
		this.exportedPackages = new PackageBinding[this.unresolvedExports.length];
		int count = 0;
		for (int i = 0; i < this.unresolvedExports.length; i++) {
			IPackageExport export = this.unresolvedExports[i];
			PackageBinding declaredPackage = getVisiblePackage(CharOperation.splitOn('.', export.name()));
			if (declaredPackage != null) {
				this.exportedPackages[count++] = declaredPackage;
				recordExportRestrictions(declaredPackage, export.targets());
			} else {
				// TODO(SHMOD): report incomplete module path?
			}
		}
		if (count < this.exportedPackages.length)
			System.arraycopy(this.exportedPackages, 0, this.exportedPackages = new PackageBinding[count], 0, count);
		
		this.openedPackages = new PackageBinding[this.unresolvedOpens.length];
		count = 0;
		for (int i = 0; i < this.unresolvedOpens.length; i++) {
			IPackageExport opens = this.unresolvedOpens[i];
			PackageBinding declaredPackage = getVisiblePackage(CharOperation.splitOn('.', opens.name()));
			if (declaredPackage != null) {
				this.openedPackages[count++] = declaredPackage;
				recordOpensRestrictions(declaredPackage, opens.targets());
			} else {
				// TODO(SHMOD): report incomplete module path?
			}
		}
		if (count < this.openedPackages.length)
			System.arraycopy(this.openedPackages, 0, this.openedPackages = new PackageBinding[count], 0, count);
	}
	
	@Override
	public TypeBinding[] getUses() {
		if (this.uses == null) {
			this.uses = new TypeBinding[this.unresolvedUses.length];
			for (int i = 0; i < this.unresolvedUses.length; i++)
				this.uses[i] = this.environment.getType(CharOperation.splitOn('.', this.unresolvedUses[i]), this);
		}
		return super.getUses();
	}
	
	@Override
	public TypeBinding[] getServices() {
		if (this.services == null)
			resolveServices();
		return super.getServices();
	}

	@Override
	public TypeBinding[] getImplementations(TypeBinding binding) {
		if (this.implementations == null)
			resolveServices();
		return super.getImplementations(binding);
	}

	private void resolveServices() {
		this.services = new TypeBinding[this.unresolvedProvides.length];
		this.implementations = new HashMap<>();
		for (int i = 0; i < this.unresolvedProvides.length; i++) {
			this.services[i] = this.environment.getType(CharOperation.splitOn('.', this.unresolvedProvides[i].name()), this);
			char[][] implNames = this.unresolvedProvides[i].with();
			TypeBinding[] impls = new TypeBinding[implNames.length];
			for (int j = 0; j < implNames.length; j++)
				impls[j] = this.environment.getType(CharOperation.splitOn('.', implNames[j]), this);
			this.implementations.put(this.services[i], impls);
		}
	}
}
