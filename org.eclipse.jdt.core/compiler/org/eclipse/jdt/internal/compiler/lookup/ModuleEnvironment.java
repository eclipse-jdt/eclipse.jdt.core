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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ExportReference;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleLocation;
import org.eclipse.jdt.internal.compiler.env.INameEnvironmentExtension;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;

public abstract class ModuleEnvironment implements INameEnvironmentExtension {
	/*
	 * Keeps track of packages mapped to modules
	 * Knows how to get modules
	 * Understands modules
	 * Understand module dependencies and restrictions
	 * Given a ModuleDeclaration, creates an IModule
	 * TODO: This should kick-in only when source level is >= 9
	 */
	public static final char[] UNNAMED = "UNNAMED".toCharArray(); //$NON-NLS-1$
	public static final IModule UNNAMED_MODULE = new IModule() {
		@Override
		public char[][] uses() {
			return null;
		}
		@Override
		public IModuleReference[] requires() {
			return null;
		}
		@Override
		public IService[] provides() {
			return null;
		}
		@Override
		public char[] name() {
			return UNNAMED;
		}
		@Override
		public IPackageExport[] exports() {
			return null;
		}
		@Override
		public String toString() {
			return new String(UNNAMED);
		}
	};
	public static IModule[] UNNAMED_MODULE_ARRAY = new IModule[]{UNNAMED_MODULE};
	private HashMap<String, IModule> modulesCache = null;
	private static HashMap<IModuleLocation, IModule> ModuleLocationMap = new HashMap<>();
	
	public ModuleEnvironment() {
		this.modulesCache = new HashMap<>();
	}

	public NameEnvironmentAnswer findType(char[][] compoundTypeName, char[] client) {
		NameEnvironmentAnswer answer = findType(compoundTypeName, getVisibleModules(client));
		return answer;
	}

	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] client) {
		return findTypeWorker(typeName, packageName, client, false);
	}
	
	private NameEnvironmentAnswer findTypeWorker(char[] typeName, char[][] packageName, char[] client, boolean searchSecondaryTypes) {
		NameEnvironmentAnswer answer = findType(typeName, packageName, getVisibleModules(client), searchSecondaryTypes);
		char[] module = null;
		if(answer == null || (module = answer.moduleName()) == null || 
				CharOperation.equals(module, JRTUtil.JAVA_BASE_CHAR)) {
			return answer;
		}
		return returnAnswerAfterValidation(packageName, answer, client);
	}

	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] client, boolean searchWithSecondaryTypes) {
		return findTypeWorker(typeName, packageName, client, searchWithSecondaryTypes);
	}

	protected NameEnvironmentAnswer returnAnswerAfterValidation(char[][] packageName, NameEnvironmentAnswer answer, char[] client) {
		if (isPackageVisible(CharOperation.concatWith(packageName, '.'), answer.moduleName(), client)) {
			return answer;
		}
		return null;
	}

	// default implementation
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModule[] modules, boolean searchSecondaryTypes) {
		return findType(typeName, packageName, modules);
	}

	public boolean isPackage(char[][] parentPackageName, char[] packageName, char[] client) {
		return isPackage(parentPackageName, packageName, getVisibleModules(client));
	}
	
	public abstract NameEnvironmentAnswer findType(char[][] compoundTypeName, IModule[] modules);

	public abstract NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModule[] modules);

	public abstract boolean isPackage(char[][] parentPackageName, char[] packageName, IModule[] module);

//	public abstract IModule getModule(String name);
//	{
////		IModule mod = this.modulesCache.get(name);
////		if (mod == null) {
////			
////		}
////		return mod;
//		for ()
//	}

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
			IModule.IModuleReference[] requires = client.requires();
			if (requires != null && requires.length > 0) {
				for (IModule.IModuleReference ref : requires) {
					IModule refModule = getModule(ref.name());
					if (refModule == null) 
						continue;
					if (refModule == source && isPackageExportedTo(refModule, packageName, client)) {
						return true;
					}
					Set<IModule> set = new LinkedHashSet<>();
					collectAllVisibleModules(refModule, set, true);
					IModule[] targets = set.toArray(new IModule[set.size()]);
					for (IModule iModule : targets) {
						if (iModule == source && isPackageExportedTo(iModule, packageName, client)) {
							return true;
						}
					}
				}
			}
			return false;
		}
		return true;
	}

	private boolean isPackageExportedTo(IModule module, char[] pack, IModule client) {
		IModule.IPackageExport[] exports = module.exports();
		if (exports != null && exports.length > 0) {
			for (IModule.IPackageExport iPackageExport : exports) {
				if (CharOperation.equals(iPackageExport.name(), pack)) {
					char[][] exportedTo = iPackageExport.exportedTo();
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
		IModule[] targets = null;
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

	private void collectAllVisibleModules(IModule module, Set<IModule> targets, boolean onlyPublic) {
		if (module != null) {
			IModule.IModuleReference[] requires = module.requires();
			if (requires != null && requires.length > 0) {
				for (IModule.IModuleReference ref : requires) {
					IModule refModule = getModule(ref.name());
					if (refModule != null) {
						if (!onlyPublic || ref.isPublic()) {
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
//	public IModule getModule(final char[] name) {
////		if (name == null) return null;
////		String mod = new String(name);
////		IModule module = this.modulesCache.get(mod);
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
	public void acceptModule(IModule mod, IModuleLocation location) {
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

	@Override
	public IModule getModule(IModuleLocation location) {
		IModule module = ModuleEnvironment.ModuleLocationMap.get(location);
		if (module == null) 
			return null;
		String modName = new String(module.name());
		if (this.modulesCache.get(modName) == null) {
			this.modulesCache.put(modName, module);
		}
		return module;
	}
	public static Module createModule(ModuleDeclaration module) {
		Module mod = new Module();
		mod.name = module.moduleName;
		if (module.requiresCount > 0) {
			ModuleReference[] refs = module.requires;
			mod.requires = new ModuleReferenceImpl[refs.length];
			for (int i = 0; i < refs.length; i++) {
				mod.requires[i] = new ModuleReferenceImpl();
				mod.requires[i].name = CharOperation.concatWith(refs[i].tokens, '.');
				mod.requires[i].isPublic = refs[i].isPublic();
			}
		} else {
			mod.requires = new ModuleReferenceImpl[0];
		}
		if (module.exportsCount > 0) {
			ExportReference[] refs = module.exports;
			mod.exports = new PackageExport[refs.length];
			for (int i = 0; i < refs.length; i++) {
				PackageExport exp = createPackageExport(refs, i);
				mod.exports[i] = exp;
			}
		} else {
			mod.exports = new PackageExport[0];
		}
		if (module.usesCount > 0) {
			TypeReference[] uses = module.uses;
			mod.uses = new char[uses.length][];
			for(int i = 0; i < uses.length; i++) {
				mod.uses[i] = CharOperation.concatWith(uses[i].getTypeName(), '.');
			}
		}
		if (module.servicesCount > 0) {
			TypeReference[] services = module.interfaces;
			TypeReference[] with = module.implementations;
			mod.provides = new Service[module.servicesCount];
			for (int i = 0; i < module.servicesCount; i++) {
				mod.provides[i] = createService(services[i], with[i]);
			}
		}
		return mod;
	}

	private static PackageExport createPackageExport(ExportReference[] refs, int i) {
		ExportReference ref = refs[i];
		PackageExport exp = new PackageExport();
		exp.pack = CharOperation.concatWith(ref.tokens, '.');
		ModuleReference[] imp = ref.targets;
		if (imp != null) {
			exp.exportedTo = new char[imp.length][];
			for(int j = 0; j < imp.length; j++) {
				exp.exportedTo = imp[j].tokens;
			}
		}
		return exp;
	}
	private static Service createService(TypeReference service, TypeReference with) {
		Service ser = new Service();
		ser.provides = CharOperation.concatWith(service.getTypeName(), '.');
		ser.with = CharOperation.concatWith(with.getTypeName(), '.');
		return ser;
	}
	static class Module implements IModule {
		char[] name;
		ModuleReferenceImpl[] requires;
		PackageExport[] exports;
		char[][] uses;
		Service[] provides;
		@Override
		public char[] name() {
			return this.name;
		}
		@Override
		public IModule.IModuleReference[] requires() {
			return this.requires;
		}
		@Override
		public IPackageExport[] exports() {
			return this.exports;
		}
		@Override
		public char[][] uses() {
			return this.uses;
		}
		@Override
		public IService[] provides() {
			return this.provides();
		}
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof IModule))
				return false;
			IModule mod = (IModule) o;
			if (!CharOperation.equals(this.name, mod.name()))
				return false;
			return Arrays.equals(this.requires, mod.requires());
		}
		@Override
		public int hashCode() {
			int result = 17;
			int c = this.name.hashCode();
			result = 31 * result + c;
			c =  Arrays.hashCode(this.requires);
			result = 31 * result + c;
			return result;
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer(getClass().getName());
			toStringContent(buffer);
			return buffer.toString();
		}
		protected void toStringContent(StringBuffer buffer) {
			buffer.append("\nmodule "); //$NON-NLS-1$
			buffer.append(this.name).append(' ');
			buffer.append('{').append('\n');
			if (this.requires != null) {
				for(int i = 0; i < this.requires.length; i++) {
					buffer.append("\trequires "); //$NON-NLS-1$
					if (this.requires[i].isPublic) {
						buffer.append(" public "); //$NON-NLS-1$
					}
					buffer.append(this.requires[i].name);
					buffer.append(';').append('\n');
				}
			}
			if (this.exports != null) {
				buffer.append('\n');
				for(int i = 0; i < this.exports.length; i++) {
					buffer.append("\texports "); //$NON-NLS-1$
					buffer.append(this.exports[i].toString());
				}
			}
			if (this.uses != null) {
				buffer.append('\n');
				for (char[] cs : this.uses) {
					buffer.append(cs);
					buffer.append(';').append('\n');
				}
			}
			if (this.provides != null) {
				buffer.append('\n');
				for(Service ser : this.provides) {
					buffer.append(ser.toString());
				}
			}
			buffer.append('\n').append('}').toString();
		}
	}
	static class ModuleReferenceImpl implements IModule.IModuleReference {
		char[] name;
		boolean isPublic = false;
		@Override
		public char[] name() {
			return this.name;
		}
		@Override
		public boolean isPublic() {
			return this.isPublic;
		}
		public boolean equals(Object o) {
			if (this == o) 
				return true;
			if (!(o instanceof IModule.IModuleReference))
				return false;
			IModule.IModuleReference mod = (IModule.IModuleReference) o;
			if (this.isPublic != mod.isPublic())
				return false;
			return CharOperation.equals(this.name, mod.name(), false);
		}
		@Override
		public int hashCode() {
			return this.name.hashCode();
		}
	}
	static class PackageExport implements IModule.IPackageExport {
		char[] pack;
		char[][] exportedTo;
		@Override
		public char[] name() {
			return this.pack;
		}

		@Override
		public char[][] exportedTo() {
			return this.exportedTo;
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(this.pack);
			if (this.exportedTo != null) {
				for (char[] cs : this.exportedTo) {
					buffer.append(cs);
				}
			}
			buffer.append(';');
			return buffer.toString();
		}
	}
	static class Service implements IModule.IService {
		char[] provides;
		char[] with;
		@Override
		public char[] name() {
			return this.provides;
		}

		@Override
		public char[] with() {
			return this.with;
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("provides"); //$NON-NLS-1$
			buffer.append(this.provides);
			buffer.append(" with "); //$NON-NLS-1$
			buffer.append(this.with);
			buffer.append(';');
			return buffer.toString();
		}
	}
}
