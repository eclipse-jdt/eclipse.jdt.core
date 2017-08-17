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

import java.util.Collection;
import java.util.HashMap;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class SourceModuleBinding extends ModuleBinding {

	final public CompilationUnitScope scope; // TODO(SHMOD): consider cleanup at end of compile

	/**
	 * Construct a named module from source.
	 * <p><strong>Side effects:</strong> adds the new module to root.knownModules,
	 * creates a new LookupEnvironment and links that into the scope.</p>
	 */
	public SourceModuleBinding(char[] moduleName, CompilationUnitScope scope, LookupEnvironment rootEnv) {
		super(moduleName);
		rootEnv.knownModules.put(moduleName, this);
		this.environment = new LookupEnvironment(rootEnv, this);
		this.scope = scope;
		scope.environment = this.environment;
	}

	public void setRequires(ModuleBinding[] requires, ModuleBinding[] requiresTransitive) {
		// TODO(SHMOD): it's a bit awkward that we may get called after applyModuleUpdates() has already worked.
		ModuleBinding javaBase = this.environment.javaBaseModule();
		if (javaBase.isUnnamed()) // happens when no java.base can be found in the name environment.
			javaBase = null;
		this.requires = merge(this.requires, requires, javaBase, ModuleBinding[]::new);
		this.requiresTransitive = merge(this.requiresTransitive, requiresTransitive, null, ModuleBinding[]::new);
	}
	
	public void setUses(TypeBinding[] uses) {
		this.uses = merge(this.uses, uses, null, TypeBinding[]::new);
	}

	public void setServices(TypeBinding[] services) {
		this.services = merge(this.services, services, null, TypeBinding[]::new);
	}

	public void setImplementations(TypeBinding infBinding, Collection<TypeBinding> resolvedImplementations) {
		if (this.implementations == null)
			this.implementations = new HashMap<>();
		this.implementations.put(infBinding, resolvedImplementations.toArray(new TypeBinding[resolvedImplementations.size()]));
	}

	private <T> T[] merge(T[] one, T[] two, T extra, IntFunction<T[]> supplier) {
		if (one.length == 0 && extra == null) {
			if (two.length > 0)
				return two;
			return one;
		}
		int len0 = extra == null ? 0 : 1;
		int len1 = one.length;
		int len2 = two.length;
		T[] result = supplier.apply(len0+len1+len2);
		if (extra != null)
			result[0] = extra;
		System.arraycopy(one, 0, result, len0, len1);
		System.arraycopy(two, 0, result, len0+len1, len2);
		return result;
	}
	
	@Override
	Stream<ModuleBinding> getRequiredModules(boolean transitiveOnly) {
		if (this.requires == NO_MODULES) {
			this.scope.referenceContext.moduleDeclaration.resolveModuleDirectives(this.scope);
		}
		return super.getRequiredModules(transitiveOnly);
	}

	@Override
	public ModuleBinding[] getAllRequiredModules() {
		if (this.scope != null)
			this.scope.referenceContext.moduleDeclaration.resolveModuleDirectives(this.scope);
		return super.getAllRequiredModules();
	}
}
