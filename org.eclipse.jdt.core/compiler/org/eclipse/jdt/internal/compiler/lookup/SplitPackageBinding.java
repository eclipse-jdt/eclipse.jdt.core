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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;

public class SplitPackageBinding extends PackageBinding {
	Set<ModuleBinding> declaringModules;
	public Set<PackageBinding> incarnations;
	
	/**
	 * Combine two potential package bindings, answering either the better of those if the other has a problem,
	 * or combine both into a split package.
	 * @param binding one candidate
	 * @param previous a previous candidate
	 * @param primaryModule when constructing a new SplitPackageBinding this primary module will define the
	 * 	focus when later an UnresolvedReferenceBinding is resolved relative to this SplitPackageBinding.
	 * @return one of: <code>null</code>, a regular PackageBinding or a SplitPackageBinding.
	 */
	public static PackageBinding combine(PackageBinding binding, PackageBinding previous, ModuleBinding primaryModule) {
		if (previous == null || !previous.isValidBinding())
			return binding == LookupEnvironment.TheNotFoundPackage ? null : binding;
		if (binding == null || !binding.isValidBinding())
			return previous == LookupEnvironment.TheNotFoundPackage ? null : previous;
		if (previous.subsumes(binding))
			return previous;
		if (binding.subsumes(previous))
			return binding;
		SplitPackageBinding split = new SplitPackageBinding(previous, primaryModule);
		split.add(binding);
		return split;
	}

	public SplitPackageBinding(PackageBinding initialBinding, ModuleBinding primaryModule) {
		super(initialBinding.compoundName, initialBinding.parent, primaryModule.environment, primaryModule);
		this.declaringModules = new HashSet<>();
		this.incarnations = new HashSet<>();
		if (initialBinding instanceof SplitPackageBinding) {
			SplitPackageBinding split = (SplitPackageBinding) initialBinding;
			this.declaringModules.addAll(split.declaringModules);
			this.incarnations.addAll(split.incarnations);
		} else {
			this.declaringModules.add(initialBinding.enclosingModule);
			this.incarnations.add(initialBinding);
		}
	}
	public void add(PackageBinding packageBinding) {
		if (packageBinding instanceof SplitPackageBinding) {
			SplitPackageBinding split = (SplitPackageBinding) packageBinding;
			this.declaringModules.addAll(split.declaringModules);
			this.incarnations.addAll(split.incarnations);
		} else {
			this.declaringModules.add(packageBinding.enclosingModule);
			this.incarnations.add(packageBinding);
		}
	}

	@Override
	void addPackage(PackageBinding element) {
		char[] simpleName = element.compoundName[element.compoundName.length-1];
		PackageBinding visible = this.knownPackages.get(simpleName);
		visible = SplitPackageBinding.combine(element, visible, this.enclosingModule);
		this.knownPackages.put(simpleName, visible);
		PackageBinding incarnation = getIncarnation(element.enclosingModule);
		if (incarnation != null)
			incarnation.addPackage(element);
	}
	
	@Override
	PackageBinding getPackage0(char[] name) {
		PackageBinding knownPackage = super.getPackage0(name);
		if (knownPackage != null)
			return knownPackage;

		PackageBinding candidate = null;
		for (PackageBinding incarnation : this.incarnations) {
			PackageBinding package0 = incarnation.getPackage0(name);
			if (package0 == null)
				return null; // if any incarnation lacks cached info, a full findPackage will be necessary 
			candidate = combine(package0, candidate, this.enclosingModule);
		}
		if (candidate != null)
			this.knownPackages.put(name, candidate);
		
		return candidate;
	}

	@Override
	protected PackageBinding findPackage(char[] name, ModuleBinding module) {
		Set<PackageBinding> candidates = new HashSet<>();
		for (ModuleBinding candidateModule : this.declaringModules) {
			PackageBinding candidate = super.findPackage(name, candidateModule);
			if (candidate != null
					&& candidate != LookupEnvironment.TheNotFoundPackage
					&& ((candidate.tagBits & TagBits.HasMissingType) == 0))
			{
				candidates.add(candidate);
			}
		}
		int count = candidates.size();
		PackageBinding result = null;
		if (count == 1) {
			result = candidates.iterator().next();
		} else if (count > 1) {
			Iterator<PackageBinding> iterator = candidates.iterator();
			SplitPackageBinding split = new SplitPackageBinding(iterator.next(), this.enclosingModule);
			while (iterator.hasNext())
				split.add(iterator.next());
			result = split;
		}
		if (result == null)
			addNotFoundPackage(name);
		else
			addPackage(result);
		return result;
	}

	public PackageBinding getIncarnation(ModuleBinding requestedModule) {
		for (PackageBinding incarnation : this.incarnations) {
			if (incarnation.enclosingModule == requestedModule)
				return incarnation;
		}
		return null; // FIXME(SHMOD) is this an error?? (note that requestedModule could be the unnamed module
	}
	
	@Override
	public boolean isEquivalentTo(PackageBinding other) {
		if (other == this)
			return true;
		if (other == null)
			return false;
		return CharOperation.equals(this.compoundName, other.compoundName)
				&& this.declaringModules.contains(other.enclosingModule);
	}

	@Override
	public boolean subsumes(PackageBinding binding) {
		if (binding instanceof SplitPackageBinding)
			return this.declaringModules.containsAll(((SplitPackageBinding) binding).declaringModules);
		else
			return this.declaringModules.contains(binding.enclosingModule);
	}

	@Override
	ReferenceBinding getType0(char[] name) {
		ReferenceBinding knownType = super.getType0(name);
		if (knownType != null)
			return knownType;

		ReferenceBinding candidate = null;
		for (PackageBinding incarnation : this.incarnations) {
			ReferenceBinding next = incarnation.getType0(name);
			if (next != null) {
				if (next.isValidBinding()) {
					if (candidate != null)
						return null; // unable to disambiguate without a module context
					candidate = next;
				}
			}
		}
		if (candidate != null) {
			addType(candidate);
		}
		
		return candidate;
	}

	/** Similar to getType0() but now we have a module and can ask the specific incarnation! */
	ReferenceBinding getType0ForModule(ModuleBinding module, char[] name) {
		if (this.declaringModules.contains(module))
			return getIncarnation(module).getType0(name);
		return null;
	}

	@Override
	ReferenceBinding getType(char[] name, ModuleBinding mod) {
		ReferenceBinding candidate = null;
		boolean accessible = false;
		for (PackageBinding incarnation : this.incarnations) {
			ReferenceBinding type = incarnation.getType(name, mod);
			if (type != null) {
				if (candidate == null || !accessible) {
					candidate = type;
					accessible = mod.canAccess(incarnation);
				} else if (mod.canAccess(incarnation)) {
					return new ProblemReferenceBinding(type.compoundName, candidate, ProblemReasons.Ambiguous); // TODO(SHMOD) add module information
				}
			}
		}
		if (candidate != null && !accessible)
			return new ProblemReferenceBinding(candidate.compoundName, candidate, ProblemReasons.NotAccessible); // TODO(SHMOD) more info
		// at this point we have only checked unique accessibility of the package, accessibility of the type will be checked by callers
		return candidate;
	}

	@Override
	public boolean isDeclaredIn(ModuleBinding moduleBinding) {
		return this.declaringModules.contains(moduleBinding);
	}

	public boolean hasConflict() {
		int visibleCount = 0;
		for (PackageBinding incarnation : this.incarnations) {
			if (incarnation.knownTypes != null && incarnation.knownTypes.elementSize > 0) { // FIXME(SHMOD): this is a workaround for checking existence of any CU
				if (this.enclosingModule.canAccess(incarnation)) 
					if (++visibleCount > 1)
						return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(super.toString());
		buf.append(" (from "); //$NON-NLS-1$
		String sep = ""; //$NON-NLS-1$
		for (ModuleBinding mod : this.declaringModules) {
			buf.append(sep).append(mod.readableName());
			sep = ", "; //$NON-NLS-1$
		}
		buf.append(")"); //$NON-NLS-1$
		return buf.toString();
	}
}
