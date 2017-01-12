/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
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
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.IModuleContext;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;

public abstract class ModuleEnvironment implements IModuleAwareNameEnvironment {
	/*
	 * Keeps track of packages mapped to modules
	 * Knows how to get modules
	 * Understands modules
	 * Understand module dependencies and restrictions
	 * Given a ModuleDeclaration, creates an IModuleDeclaration
	 * TODO: This should kick-in only when source level is >= 9
	 */
	public static final char[] UNNAMED = "".toCharArray(); //$NON-NLS-1$
	public static final IModule UNNAMED_MODULE = new IModule() {

		@Override
		public String toString() {
			return new String(UNNAMED);
		}

		@Override
		public char[] name() {
			//
			return UNNAMED;
		}

		@Override
		public IModuleReference[] requires() {
			return null;
		}

		@Override
		public IPackageExport[] exports() {
			return null;
		}

		@Override
		public char[][] uses() {
			return null;
		}

		@Override
		public IService[] provides() {
			return null;
		}
		@Override
		public IPackageExport[] opens() {
			return null;
		}

	};
	public static final class AutoModule implements IModule {
		char[] name;
		public AutoModule(char[] name) {
			this.name = name;
		}
		@Override
		public char[] name() {
			return this.name;
		}

		@Override
		public IModuleReference[] requires() {
			return IModule.NO_MODULE_REFS;
		}

		@Override
		public IPackageExport[] exports() {
			return IModule.NO_EXPORTS;
		}

		@Override
		public char[][] uses() {
			return IModule.NO_USES;
		}

		@Override
		public IService[] provides() {
			return IModule.NO_PROVIDES;
		}

		@Override
		public IPackageExport[] opens() {
			return NO_OPENS;
		}

		public boolean isAutomatic() {
			return true;
		}
	}
	public static IModule[] UNNAMED_MODULE_ARRAY = new IModule[]{UNNAMED_MODULE};

	private HashMap<String, IModule> modulesCache = null;
	private static HashMap<IModuleEnvironment, IModule> ModuleLocationMap = new HashMap<>();
	
	public ModuleEnvironment() {
		this.modulesCache = new HashMap<>();
	}

	protected NameEnvironmentAnswer returnAnswerAfterValidation(char[][] packageName, NameEnvironmentAnswer answer, char[] client) {
		if (isPackageVisible(CharOperation.concatWith(packageName, '.'), answer.moduleName(), client)) {
			return answer;
		}
		return null;
	}

	// TODO: these should be abstract
	public NameEnvironmentAnswer findType(char[][] compoundTypeName, IModuleContext moduleContext) {
		return null;
	}
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModuleContext moduleContext) {
		return null;
	}
	public boolean isPackage(char[][] parentPackageName, char[] packageName, IModuleContext moduleContext) {
		return false;
	}

	@Deprecated
	public boolean isPackageVisible(char[] packageName, char[] sourceName, char[] clientName) {
		boolean clientIsUnnamed = clientName == null || clientName == UNNAMED;
		if (clientIsUnnamed)
			return true; // Unnamed module can read every module
		if (sourceName == null || sourceName == UNNAMED)
			return clientIsUnnamed; // Unnamed module cannot be read by any named module

		if (CharOperation.equals(sourceName, clientName)) 
			return true;

		IModule source = getModule(sourceName);
		IModule client = getModule(clientName);
		if (client != null) {
					Set<IModule> set = new LinkedHashSet<>();
					collectAllVisibleModules(client, set, false);
					IModule[] targets = set.toArray(new IModule[set.size()]);
					for (IModule iModule : targets) {
						if (iModule == source && isPackageExportedTo(iModule, packageName, client)) {
							return true;
						}
					}
			return false;
		}
		return true;
	}
	/**
	 * Tells whether the given module exports the given package to the specified client
	 * module.
	 *
	 * @param module module whose exports being checked
	 * @param pack package being exported
	 * @param client requesting module
	 * @return whether or not the specified package is exported from the module to the client
	 */
	protected boolean isPackageExportedTo(IModule module, char[] pack, IModule client) {
		IModule.IPackageExport[] exports = module.exports();
		if (exports != null && exports.length > 0) {
			for (IModule.IPackageExport iPackageExport : exports) {
				if (CharOperation.equals(iPackageExport.name(), pack)) {
					char[][] exportedTo = iPackageExport.targets();
					if (exportedTo == null || exportedTo.length == 0) {
						return true;
					}
					for (char[] cs : exportedTo) {
						if (CharOperation.equals(cs, client.name())) {
							return true;
						}
					}
					
				}
			}
		}
		return false;
	}

	public IModule[] getVisibleModules(char[] mod) {
		IModule[] targets = new IModule[0];
		if (mod != null && !CharOperation.equals(mod, UNNAMED)) {
			Set<IModule> set = new LinkedHashSet<>();
			IModule client = getModule(JRTUtil.JAVA_BASE.toCharArray());
			if (client != null) set.add(client);
			client = getModule(mod);
			if (client != null) {
				set.add(client);
				collectAllVisibleModules(client, set, false);
				targets = set.toArray(new IModule[set.size()]);
			}
		} else {
			return UNNAMED_MODULE_ARRAY;
		}
		return targets;
	}

	protected void collectAllVisibleModules(IModule module, Set<IModule> targets, boolean onlyPublic) {
		if (module != null) {
			IModule.IModuleReference[] requires = module.requires();
			if (requires != null && requires.length > 0) {
				for (IModule.IModuleReference ref : requires) {
					IModule refModule = getModule(ref.name());
					if (refModule != null) {
						if (!onlyPublic || ref.isTransitive()) {
							targets.add(refModule);
							collectAllVisibleModules(refModule, targets, true);
						}
					}
				}
			}
		}
	}
	/**
	 * Returns the module with the given name from the name environment.
	 *
	 * @param name the name of the module to lookup
	 * @return the module with the given name
	 */
//	public IModuleDeclaration getModule(final char[] name) {
////		if (name == null) return null;
////		String mod = new String(name);
////		IModuleDeclaration module = this.modulesCache.get(mod);
////		return module;
//		return name == null ? null : getModule(CharOperation.charToString(name));
//	}
	/**
	 * Accepts the given module to be served later on requests. If 
	 * any older copies of module already present, they will be 
	 * overwritten by the new one.
	 *
	 * @param mod the module to be stored in memory
	 */
	public void acceptModule(IModule mod, IModuleEnvironment location) {
		IModule existing = ModuleEnvironment.ModuleLocationMap.get(location);
		if (existing != null) {
			if (existing.equals(mod))
				return;
			else {
				// Report problem and ignore the duplicate
			}
		}
		String name = new String(mod.name());
		this.modulesCache.put(name, mod);
		ModuleEnvironment.ModuleLocationMap.put(location, mod);
	}

	//@Override
	public IModule getModule(IModuleEnvironment location) {
		IModule module = ModuleEnvironment.ModuleLocationMap.get(location);
		if (module == null) 
			return null;
		String modName = new String(module.name());
		if (this.modulesCache.get(modName) == null) {
			this.modulesCache.put(modName, module);
		}
		return module;
	}
}
