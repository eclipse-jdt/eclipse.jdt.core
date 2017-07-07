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
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule;
import org.eclipse.jdt.internal.compiler.util.HashtableOfPackage;
import org.eclipse.jdt.internal.compiler.util.SimpleSetOfCharArray;

/**
 * This class serves a dual purpose
 * <p>
 * First, it canonically represents modules in the world of bindings.
 * </p>
 * <p>
 * Secondly, it adds a graph layer on top of {@link LookupEnvironment}:
 * ModuleBindins are linked through "read" edges as per JPMS (see java.lang.module.Configuration).
 * Additionally, each ModuleBinding holds its own instance of LookupEnviroment,
 * capturing all packages and types that are visible to the current module.
 * As a subset of all visible types, the ModuleBinding knows the set of
 * packages locally declared in this module.
 * </p>
 */
public class ModuleBinding extends Binding implements IUpdatableModule {

	/** Name of the unnamed module. */
	public static final char[] UNNAMED = "".toCharArray(); //$NON-NLS-1$
	/** Module name for package/type lookup that doesn't care about modules. */
	public static final char[] ANY = "".toCharArray(); //$NON-NLS-1$

	public static class UnNamedModule extends ModuleBinding {

		@SuppressWarnings("synthetic-access")
		UnNamedModule(LookupEnvironment env) {
			super(env);
		}
		public ModuleBinding[] getAllRequiredModules() {
			return Binding.NO_MODULES;
		}
		public boolean canAccess(PackageBinding pkg) {
			//TODO - if the package is part of a named module, then we should check if the module exports the package
			return true;
		}
		@Override
		public boolean isUnnamed() {
			return true;
		}
		@Override
		public String toString() {
			return "The Unnamed Module"; //$NON-NLS-1$
		}
	}
	public char[] moduleName;
	protected ModuleBinding[] requires;
	protected ModuleBinding[] requiresTransitive;
	protected PackageBinding[] exportedPackages;
	private Map<PackageBinding,SimpleSetOfCharArray> exportRestrictions; // RHS is unresolved names, because unresolvable names are legal in this position
	protected PackageBinding[] openedPackages;
	private Map<PackageBinding,SimpleSetOfCharArray> openRestrictions; // RHS is unresolved names, because unresolvable names are legal in this position
	protected TypeBinding[] uses;
	protected TypeBinding[] services;
	public Map<TypeBinding,TypeBinding[]> implementations;
	public int modifiers;
	public LookupEnvironment environment;
	public int tagBits;
	ModuleBinding[] requiredModules = null;
	boolean isAuto;
	private boolean[] isComplete = new boolean[UpdateKind.values().length];

	/** Packages declared in this module (indexed by qualified name). */
	HashtableOfPackage declaredPackages; // TODO(SHMOD): measure if this is worth the memory. LE->PackageBinding basically hold the same information

	/** Constructor for the unnamed module. */
	private ModuleBinding(LookupEnvironment env) {
		this.moduleName = ModuleBinding.UNNAMED;
		this.environment = env;
		this.requires = Binding.NO_MODULES;
		this.requiresTransitive = Binding.NO_MODULES;
		this.exportedPackages = Binding.NO_PACKAGES;
		this.openedPackages = Binding.NO_PACKAGES;
		this.declaredPackages = new HashtableOfPackage(0);
		Arrays.fill(this.isComplete, true);
	}
	/* For error binding and sub class SourceModuleBinding. */
	ModuleBinding(char[] moduleName) {
		this.moduleName = moduleName;
		this.requires = Binding.NO_MODULES;
		this.requiresTransitive = Binding.NO_MODULES;
		this.exportedPackages = Binding.NO_PACKAGES;
		this.openedPackages = Binding.NO_PACKAGES;
		this.uses = Binding.NO_TYPES;
		this.services = Binding.NO_TYPES;
		this.declaredPackages = new HashtableOfPackage(5);
		this.isAuto = false;
	}

	/* For sub class BinaryModuleBinding */
	protected ModuleBinding(char[] moduleName, LookupEnvironment existingEnvironment) {
		this.moduleName = moduleName;
		this.requires = Binding.NO_MODULES;
		this.requiresTransitive = Binding.NO_MODULES;
		this.environment = new LookupEnvironment(existingEnvironment.root, this);
		this.declaredPackages = new HashtableOfPackage(5);
	}
	
	public PackageBinding[] getExports() {
		completeIfNeeded(UpdateKind.PACKAGE);
		return this.exportedPackages;
	}
	public String[] getExportRestrictions(PackageBinding pack) {
		completeIfNeeded(UpdateKind.PACKAGE);
		if (this.exportRestrictions != null) {
			SimpleSetOfCharArray set = this.exportRestrictions.get(pack);
			if (set != null) {
				char[][] names = new char[set.elementSize][]; 
				set.asArray(names);
				return CharOperation.charArrayToStringArray(names);
			}
		}
		return CharOperation.NO_STRINGS;
	}
	public PackageBinding[] getOpens() {
		completeIfNeeded(UpdateKind.PACKAGE);
		return this.openedPackages;
	}
	public String[] getOpenRestrictions(PackageBinding pack) {
		completeIfNeeded(UpdateKind.PACKAGE);
		if (this.openRestrictions != null) {
			SimpleSetOfCharArray set = this.openRestrictions.get(pack);
			if (set != null) {
				char[][] names = new char[set.elementSize][]; 
				set.asArray(names);
				return CharOperation.charArrayToStringArray(names);
			}
		}
		return CharOperation.NO_STRINGS;
	}
	public TypeBinding[] getImplementations(TypeBinding binding) {
		if (this.implementations != null) {
			return this.implementations.get(binding);
		}
		return null;
	}
	public ModuleBinding[] getRequires() {
		completeIfNeeded(UpdateKind.MODULE);
		return this.requires;
	}
	public ModuleBinding[] getRequiresTransitive() {
		completeIfNeeded(UpdateKind.MODULE);
		return this.requiresTransitive;
	}
	
	public TypeBinding[] getUses() {
		return this.uses;
	}
	
	public TypeBinding[] getServices() {
		return this.services;
	}

	private void completeIfNeeded(IUpdatableModule.UpdateKind kind) {
		if (!this.isComplete[kind.ordinal()]) {
			this.isComplete[kind.ordinal()] = true;
			if (this.environment.nameEnvironment instanceof IModuleAwareNameEnvironment) {
				((IModuleAwareNameEnvironment) this.environment.nameEnvironment).applyModuleUpdates(this, kind);
			}
		}
	}

	// ---  Implement IUpdatableModule: ---

	@Override
	public void addReads(char[] requiredModuleName) {
		ModuleBinding requiredModule = this.environment.getModule(requiredModuleName);
		if (requiredModule != null) {
			int len = this.requires.length;
			if (len == 0) {
				this.requires = new ModuleBinding[] { requiredModule };
			} else {
				System.arraycopy(this.requires, 0, this.requires = new ModuleBinding[len+1], 0, len);
				this.requires[len] = requiredModule;
			}
		} else {
			// TODO(SHMOD) report error
		}
	}
	@Override
	public void addExports(char[] packageName, char[][] targetModules) {
		PackageBinding declaredPackage = getVisiblePackage(CharOperation.splitOn('.', packageName));
		addResolvedExport(declaredPackage, targetModules);
	}

	// ---

	public void addResolvedExport(PackageBinding declaredPackage, char[][] targetModules) {
		int len = this.exportedPackages.length;
		if (declaredPackage == null || !declaredPackage.isValidBinding()) {
			// FIXME(SHMOD) use a problem binding? See https://bugs.eclipse.org/518794#c13
			return;
		}
		if (len == 0) {
			this.exportedPackages = new PackageBinding[] { declaredPackage };
		} else {
			System.arraycopy(this.exportedPackages, 0, this.exportedPackages = new PackageBinding[len+1], 0, len);
			this.exportedPackages[len] = declaredPackage;
		}
		recordExportRestrictions(declaredPackage, targetModules);
	}

	public void addResolvedOpens(PackageBinding declaredPackage, char[][] targetModules) {
		int len = this.openedPackages.length;
		if (declaredPackage == null || !declaredPackage.isValidBinding()) {
			// FIXME(SHMOD) use a problem binding? See https://bugs.eclipse.org/518794#c13
			return;
		}
		if (len == 0) {
			this.openedPackages = new PackageBinding[] { declaredPackage };
		} else {
			System.arraycopy(this.openedPackages, 0, this.openedPackages = new PackageBinding[len+1], 0, len);
			this.openedPackages[len] = declaredPackage;
		}
		recordOpensRestrictions(declaredPackage, targetModules);
	}

	protected void recordExportRestrictions(PackageBinding exportedPackage, char[][] targetModules) {
		if (targetModules != null && targetModules.length > 0) {
			SimpleSetOfCharArray targetModuleSet = new SimpleSetOfCharArray(targetModules.length);
			for (int i = 0; i < targetModules.length; i++) {
				targetModuleSet.add(targetModules[i]);
			}
			if (this.exportRestrictions == null)
				this.exportRestrictions = new HashMap<>();
			this.exportRestrictions.put(exportedPackage, targetModuleSet);
		}
	}

	protected void recordOpensRestrictions(PackageBinding openedPackage, char[][] targetModules) {
		if (targetModules != null && targetModules.length > 0) {
			SimpleSetOfCharArray targetModuleSet = new SimpleSetOfCharArray(targetModules.length);
			for (int i = 0; i < targetModules.length; i++) {
				targetModuleSet.add(targetModules[i]);
			}
			if (this.openRestrictions == null)
				this.openRestrictions = new HashMap<>();
			this.openRestrictions.put(openedPackage, targetModuleSet);
		}
	}

	private Stream<ModuleBinding> getRequiredModules(boolean transitiveOnly) {
		return Stream.of(transitiveOnly ? this.getRequiresTransitive() : this.getRequires());
	}
	private void collectAllDependencies(Set<ModuleBinding> deps) {
		getRequiredModules(false).forEach(m -> {
			if (deps.add(m)) {
				m.collectAllDependencies(deps);
			}
		});
	}
	private void collectTransitiveDependencies(Set<ModuleBinding> deps) {
		getRequiredModules(true).forEach(m -> {
			if (deps.add(m)) {
				m.collectTransitiveDependencies(deps);
			}
		});
	}

	// All modules required by this module, either directly or indirectly
	public Supplier<Collection<ModuleBinding>> dependencyGraphCollector() {
		return () -> getRequiredModules(false)
			.collect(HashSet::new,
				(set, mod) -> {
					set.add(mod);
					mod.collectAllDependencies(set);
				},
				HashSet::addAll);
	}
	// All direct and transitive dependencies of this module
	public Supplier<Collection<ModuleBinding>> dependencyCollector() {
		return () -> getRequiredModules(false)
			.collect(HashSet::new,
				(set, mod) -> {
					set.add(mod);
					mod.collectTransitiveDependencies(set);
				},
				HashSet::addAll);
	}

	/**
	 * Get all the modules required by this module
	 * All required modules include modules explicitly specified as required in the module declaration
	 * as well as implicit dependencies - those specified as ' requires transitive ' by one of the
	 * dependencies
	 * 
	 * @return
	 *   An array of all required modules
	 */
	public ModuleBinding[] getAllRequiredModules() {
		if (this.requiredModules != null)
			return this.requiredModules;

		Collection<ModuleBinding> allRequires = dependencyCollector().get();
		if (allRequires.contains(this)) {
			// TODO(SHMOD): report (when? where?)
			return NO_MODULES; // avoid entering unbounded recursion due to cyclic requires
		}
		ModuleBinding javaBase = this.environment.javaBaseModule();
																			// add java.base?
		if (!CharOperation.equals(this.moduleName, TypeConstants.JAVA_BASE)	// ... not if this *is* java.base 
				&& javaBase != null 										// ... nor when java.base is absent
				&& javaBase != this.environment.UnNamedModule)				// ..... or faked by the unnamed module 
		{
			allRequires.add(javaBase);
		}
		return this.requiredModules = allRequires.size() > 0 ? allRequires.toArray(new ModuleBinding[allRequires.size()]) : Binding.NO_MODULES;
	}

	/** Answer the name of this module. The unnamed module is identified by {@link #UNNAMED}. */
	public char[] name() {
		return this.moduleName;
	}

	/**
	 * Answer the name of this module as it should be used for package or type lookup.
	 * Unnamed and automatic modules answer {@link #ANY}, to signal that lookup should
	 * search in all accessible modules.
	 */
	public char[] nameForLookup() {
		if (this.moduleName == UNNAMED || this.isAuto)
			return ANY;
		else
			return this.moduleName;
	}

	/**
	 * Check if the specified package is exported to the client module by this module. True if the package appears
	 * in the list of exported packages and when the export is targeted, the module appears in the targets of the
	 * exports statement
	 * @param pkg - the package whose visibility is to be checked
	 * @param client - the module that wishes to use the package
	 * @return true if the package is visible to the client module, false otherwise
	 */
	public boolean isPackageExportedTo(PackageBinding pkg, ModuleBinding client) {
		// TODO(SHMOD): cache the result?
		PackageBinding resolved = getVisiblePackage(pkg.compoundName);
		if (pkg.isEquivalentTo(resolved)) {
			if (this.isAuto) { // all packages are exported by an automatic module
				return true;
			}
			PackageBinding[] initializedExports = getExports();
			for (int i = 0; i < initializedExports.length; i++) {
				PackageBinding export = initializedExports[i];
				if (export.subsumes(resolved)) {
					if (this.exportRestrictions != null) {
						SimpleSetOfCharArray restrictions = this.exportRestrictions.get(export);
						if (restrictions != null)
							return restrictions.includes(client.name());
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return a package binding if there exists a package named name in this module's context and it can be seen by this module.
	 * A package can be seen by this module if it is declared in this module or any other module read by this module 
	 * (JLS 7.4.3 for packages based on JLS 7.3 for compilation units).
	 * Package exports are not considered for visibility check (only when checking "uniquely visible" (JLS 7.4.3)).
	 * <p>
	 * The returned package may be a {@link SplitPackageBinding}, if more than one package of the given name is visible.
	 * </p>
	 * <p>
	 * When asked via the unnamed module or an automatic module all other named modules are considered visible. 
	 * </p>
	 */
	PackageBinding getTopLevelPackage(char[] name) {
		// check caches:
		PackageBinding binding = this.declaredPackages.get(name);
		if (binding != null)
			return binding;
		binding = this.environment.getPackage0(name);
		if (binding != null)
			return binding;

		// find declaration in a visible module:
		if (this.environment.useModuleSystem) {
			IModuleAwareNameEnvironment moduleEnv = (IModuleAwareNameEnvironment) this.environment.nameEnvironment;
			char[][] declaringModuleNames = moduleEnv.getModulesDeclaringPackage(null, name, nameForLookup());
			if (declaringModuleNames != null) {
				if (CharOperation.containsEqual(declaringModuleNames, this.moduleName)) {
					// declared here, not yet known, so create it now:
					binding = new PackageBinding(new char[][] {name}, null, this.environment, this);
				} else {
					// visible but foreign (when current is unnamed or auto):
					for (char[] declaringModuleName : declaringModuleNames) {
						ModuleBinding declaringModule = this.environment.root.getModule(declaringModuleName);
						if (declaringModule != null)
							binding = SplitPackageBinding.combine(declaringModule.getTopLevelPackage(name), binding, declaringModule);
					}
				}
			}
		} else {
			if (this.environment.nameEnvironment.isPackage(null, name))
				binding = new PackageBinding(new char[][] {name}, null, this.environment, this);			
		}

		// enrich with split-siblings from visible modules:
		for (ModuleBinding required : getAllRequiredModules()) {
			if (required == this) continue;
			binding = SplitPackageBinding.combine(required.getTopLevelPackage(name), binding, this);
		}

		// remember:
		if (binding != null) {
			this.environment.knownPackages.put(name, binding);
			binding = addPackage(binding, false);
		} else {
			this.environment.knownPackages.put(name, LookupEnvironment.TheNotFoundPackage);
		}
		return binding;
	}

	// Given parent is visible in this module, see if there is sub package named name visible in this module
	private PackageBinding getVisiblePackage(PackageBinding parent, char[] name) {
		assert parent.compoundName.length > 0 : "shouldn't ask children of a default package"; //$NON-NLS-1$

		// check caches:
		char[][] subPkgCompoundName = CharOperation.arrayConcat(parent.compoundName, name);
		char[] fullFlatName = CharOperation.concatWith(subPkgCompoundName, '.');
		PackageBinding pkg = this.declaredPackages.get(fullFlatName);
		if (pkg != null)
			return pkg;
		pkg = parent.getPackage0(name);
		if (pkg != null) {
			if (pkg == LookupEnvironment.TheNotFoundPackage)
				return null;
			else
				return addPackage(pkg, false);
		}

		PackageBinding binding = null;
		if (!parent.isDeclaredIn(this)) {
			// delegate foreign packages to their declaring modules:
			if (parent instanceof SplitPackageBinding)
				return combineWithChildrenOfSplitPackage(binding, (SplitPackageBinding) parent, name);
			return parent.enclosingModule.getVisiblePackage(parent, name);
		}

		boolean isPackage = this.environment.useModuleSystem 
								? isDeclaredPackage(parent.compoundName, name)
								: this.environment.nameEnvironment.isPackage(parent.compoundName, name);
		if (!isPackage)
			return null; 

		// create
		binding = new PackageBinding(subPkgCompoundName, parent, this.environment, this);
		
		// enrich
		if (parent instanceof SplitPackageBinding)
			binding = combineWithChildrenOfSplitPackage(binding, (SplitPackageBinding) parent, name);

		// remember
		if (parent.compoundName.length == 0)
			this.environment.knownPackages.put(name, binding);
		else
			parent.addPackage(binding);
		return addPackage(binding, false);
	}

	/**
	 * Answer the package of the given qualified name and visible in this module,
	 * or {@code null} if no such package exists.
	 * Accessibility (based on package exports) is <strong>not</strong> checked.
	 * <p>
	 * May answer a {@link SplitPackageBinding}.
	 * </p>
	 */
	public PackageBinding getVisiblePackage(char[][] qualifiedPackageName) {
		if (qualifiedPackageName == null || qualifiedPackageName.length == 0) {
			return this.environment.defaultPackage;
		}

		PackageBinding parent = getTopLevelPackage(qualifiedPackageName[0]);
		if (parent == null || parent == LookupEnvironment.TheNotFoundPackage)
			return null;

		// check each sub package
		for (int i = 1; i < qualifiedPackageName.length; i++) {
			PackageBinding binding = getVisiblePackage(parent, qualifiedPackageName[i]); 
			if (binding == null || binding == LookupEnvironment.TheNotFoundPackage) {
				return null;
			}
			parent = binding;
		}
		return parent;
	}

	private PackageBinding combineWithChildrenOfSplitPackage(PackageBinding childPackage, SplitPackageBinding splitParent, char[] name) {
		ModuleBinding primaryModule = childPackage != null ? childPackage.enclosingModule : splitParent.enclosingModule;
		// see if other incarnations contribute to the child package, too:
		for (PackageBinding incarnation :  splitParent.incarnations) {
			ModuleBinding moduleBinding = incarnation.enclosingModule;
			if (moduleBinding == this)
				continue;
			PackageBinding next = moduleBinding.getVisiblePackage(incarnation, name);
			childPackage = SplitPackageBinding.combine(next, childPackage, primaryModule);
		}
		return childPackage;
	}

	/**
	 * Does this module declare a package by the given name and qualified by the given parentPackageName?
	 * In JLS diction a declared package is "associated" with this module.
	 */
	private boolean isDeclaredPackage(char[][] parentPackageName, char[] name) {
		char[] qualifiedPackageName = CharOperation.concatWith(parentPackageName, name, '.');
		PackageBinding declared = this.declaredPackages.get(qualifiedPackageName);
		if (declared != null) {
			return declared != LookupEnvironment.TheNotFoundPackage;
		}
		INameEnvironment nameEnvironment = this.environment.nameEnvironment;
		if (nameEnvironment instanceof IModuleAwareNameEnvironment) {
			IModuleAwareNameEnvironment moduleEnv = (IModuleAwareNameEnvironment)nameEnvironment;
			return moduleEnv.getModulesDeclaringPackage(parentPackageName, name, this.moduleName) != null;
		} else {
			return nameEnvironment.isPackage(parentPackageName, name);
		}
	}

	/**
	 * Answer a package, that is a member named <em>packageName</em> of the parent package
	 * named <em>parentPackageName</em>.
	 * Considers all packages that are visible to the current module,
	 * i.e., we consider locally declared packages and packages in all modules
	 * read by the current module.
	 * Accessibility (via package exports) is <strong>not</strong> checked.
	 */
	PackageBinding getPackage(char[][] parentPackageName, char[] packageName) {
		// Returns a package binding if there exists such a package in the context of this module and it is observable
		// A package is observable if it is declared in this module or it is exported by some required module
		if (parentPackageName == null || parentPackageName.length == 0) {
			return getTopLevelPackage(packageName);
		}
		PackageBinding binding = null;
		PackageBinding parent = getVisiblePackage(parentPackageName);
		if (parent != null && parent != LookupEnvironment.TheNotFoundPackage) {
			binding = getVisiblePackage(parent, packageName);
		}
		if (binding != null)
			return addPackage(binding, false);
		return null;
	}
	
	/**
	 * Check if the given package is declared in this module,
	 * and if so, remember this fact for later.
	 * The package can be a {@code SplitPackageBinding} in which case
	 * only one of its incarnations needs to be declared in this module.
	 * @param packageBinding the package to add
	 * @param checkForSplit if true then we should try to construct a split package from
	 * 	same named packages in required modules.
	 * @return the given package, possibly enriched to a {@link SplitPackageBinding}
	 */
	PackageBinding addPackage(PackageBinding packageBinding, boolean checkForSplit) {
		if (packageBinding.isDeclaredIn(this)) {
			char[] packageName = packageBinding.readableName();
			if (checkForSplit) {
				for (ModuleBinding moduleBinding : getAllRequiredModules())
					packageBinding = SplitPackageBinding.combine(moduleBinding.getVisiblePackage(packageBinding.compoundName), packageBinding, this);
			}
			this.declaredPackages.put(packageName, packageBinding);
		}
		return packageBinding;
	}

	/**
	 * Check if the given package is accessible by this module. True when the package is declared in
	 * this module or exported by some required module to this module.
	 * See {@link #isPackageExportedTo(PackageBinding, ModuleBinding)}
	 * 
	 * @param pkg
	 * 
	 * @return True, if the package is accessible by this module, false otherwise
	 */
	public boolean canAccess(PackageBinding pkg) {
		boolean answer = pkg.isDeclaredIn(this) || Stream.of(getAllRequiredModules()).anyMatch(
				dep -> dep.isPackageExportedTo(pkg, ModuleBinding.this) // TODO(SHMOD): store export status in the PackageBinding
		);
		return answer;
	}
	@Override
	public char[] computeUniqueKey(boolean isLeaf) {
		return this.moduleName; // for now just return the module name - tied to Signature if any.
	}

	@Override
	public int kind() {
		//
		return Binding.MODULE;
	}

	@Override
	public char[] readableName() {
		//
		return this.moduleName;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(30);
		if (isOpen())
			buffer.append("open "); //$NON-NLS-1$
		buffer.append("module " + new String(readableName())); //$NON-NLS-1$
		if (this.requires.length > 0) {
			buffer.append("\n/*    requires    */\n"); //$NON-NLS-1$
			for (int i = 0; i < this.requires.length; i++) {
				buffer.append("\n\t"); //$NON-NLS-1$
				if (this.requiresTransitive != null) {
					for (ModuleBinding reqTrans : this.requiresTransitive) {
						if (reqTrans == this.requires[i]) {
							buffer.append("transitive "); //$NON-NLS-1$
							break;
						}
					}
				}
				buffer.append(this.requires[i].moduleName);
			}
		} else {
			buffer.append("\nNo Requires"); //$NON-NLS-1$
		}
		if (this.exportedPackages != null && this.exportedPackages.length > 0) {
			buffer.append("\n/*    exports    */\n"); //$NON-NLS-1$
			for (int i = 0; i < this.exportedPackages.length; i++) {
				PackageBinding export = this.exportedPackages[i];
				buffer.append("\n\t"); //$NON-NLS-1$
				if (export == null) {
					buffer.append("<unresolved>"); //$NON-NLS-1$
					continue;
				}
				buffer.append(export.readableName());
				SimpleSetOfCharArray restrictions = this.exportRestrictions != null ? this.exportRestrictions.get(export) : null;
				if (restrictions != null) {
					buffer.append(" to "); //$NON-NLS-1$
					String sep = ""; //$NON-NLS-1$
					char[][] allNames = new char[restrictions.elementSize][];
					restrictions.asArray(allNames);
					for (char[] targetModule : allNames) {
						buffer.append(sep);
						buffer.append(targetModule);
						sep = ", "; //$NON-NLS-1$
					}
				}
			}
		} else {
			buffer.append("\nNo Exports"); //$NON-NLS-1$
		}
		if (this.openedPackages != null && this.openedPackages.length > 0) {
			buffer.append("\n/*    exports    */\n"); //$NON-NLS-1$
			for (int i = 0; i < this.openedPackages.length; i++) {
				PackageBinding opens = this.openedPackages[i];
				buffer.append("\n\t"); //$NON-NLS-1$
				if (opens == null) {
					buffer.append("<unresolved>"); //$NON-NLS-1$
					continue;
				}
				buffer.append(opens.readableName());
				SimpleSetOfCharArray restrictions = this.openRestrictions != null ? this.openRestrictions.get(opens) : null;
				if (restrictions != null) {
					buffer.append(" to "); //$NON-NLS-1$
					String sep = ""; //$NON-NLS-1$
					char[][] allNames = new char[restrictions.elementSize][];
					restrictions.asArray(allNames);
					for (char[] targetModule : allNames) {
						buffer.append(sep);
						buffer.append(targetModule);
						sep = ", "; //$NON-NLS-1$
					}
				}
			}
		} else {
			buffer.append("\nNo Opens"); //$NON-NLS-1$
		}
		if (this.uses != null && this.uses.length > 0) {
			buffer.append("\n/*    uses    /*\n"); //$NON-NLS-1$
			for (int i = 0; i < this.uses.length; i++) {
				buffer.append("\n\t"); //$NON-NLS-1$
				buffer.append(this.uses[i].debugName());
			}
		} else {
			buffer.append("\nNo Uses"); //$NON-NLS-1$
		}
		if (this.services != null && this.services.length > 0) {
			buffer.append("\n/*    Services    */\n"); //$NON-NLS-1$
			for (int i = 0; i < this.services.length; i++) {
				buffer.append("\n\t"); //$NON-NLS-1$
				buffer.append("provides "); //$NON-NLS-1$
				buffer.append(this.services[i].debugName());
				buffer.append(" with "); //$NON-NLS-1$
				if (this.implementations != null && this.implementations.containsKey(this.services[i])) {
					String sep = ""; //$NON-NLS-1$
					for (TypeBinding impl : this.implementations.get(this.services[i])) {
						buffer.append(sep).append(impl.debugName());
						sep = ", "; //$NON-NLS-1$
					}
				} else {
					buffer.append("<missing implementations>"); //$NON-NLS-1$
				}
			}
		} else {
			buffer.append("\nNo Services"); //$NON-NLS-1$
		}
		return buffer.toString();
	}
	public boolean isUnnamed() {
		return false;
	}
	public boolean isOpen() {
		return (this.modifiers & ClassFileConstants.ACC_OPEN) != 0;
	}
	public boolean isDeprecated() {
		// TODO(SHMOD) implement deprecation for modules
		return false;
	}
}
