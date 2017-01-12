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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ExportsStatement;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.ast.RequiresStatement;
import org.eclipse.jdt.internal.compiler.ast.ProvidesStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UsesStatement;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.ModuleReferenceImpl;
import org.eclipse.jdt.internal.compiler.env.PackageExportImpl;

public class ModuleInfo extends SourceTypeElementInfo implements IModule {

	protected static final IModuleReference[] NO_REQUIRES = new IModuleReference[0];
	protected static final IPackageExport[] NO_EXPORTS = new IPackageExport[0];
	protected static final IService[] NO_SERVICES = new IService[0];
	protected static final char[][] NO_USES = new char[0][0];

	static class Service implements IModule.IService {
		char[] provides;
		char[][] with;
		@Override
		public char[] name() {
			return this.provides;
		}

		@Override
		public char[][] with() {
			return this.with;
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("provides "); //$NON-NLS-1$
			buffer.append(this.provides);
			buffer.append(" with "); //$NON-NLS-1$
			buffer.append(this.with);
			buffer.append(';');
			return buffer.toString();
		}
	}
	char[] name;
	ModuleReferenceImpl[] requires;
	PackageExportImpl[] exports;
	char[][] uses;
	Service[] provides;
	PackageExportImpl[] opens;

	@Override
	public char[] name() {
		return this.name;
	}
	public static ModuleInfo createModule(ModuleDeclaration module) {
		ModuleInfo mod = new ModuleInfo();
		mod.name = module.moduleName;
		if (module.requiresCount > 0) {
			RequiresStatement[] refs = module.requires;
			mod.requires = new ModuleReferenceImpl[refs.length];
			for (int i = 0; i < refs.length; i++) {
				mod.requires[i] = new ModuleReferenceImpl();
				mod.requires[i].name = CharOperation.concatWith(refs[i].module.tokens, '.');
				mod.requires[i].modifiers = refs[i].modifiers;
			}
		} else {
			mod.requires = new ModuleReferenceImpl[0];
		}
		if (module.exportsCount > 0) {
			ExportsStatement[] refs = module.exports;
			mod.exports = new PackageExportImpl[refs.length];
			for (int i = 0; i < refs.length; i++) {
				PackageExportImpl exp = createPackageExport(refs, i);
				mod.exports[i] = exp;
			}
		} else {
			mod.exports = new PackageExportImpl[0];
		}
		if (module.usesCount > 0) {
			UsesStatement[] uses = module.uses;
			mod.uses = new char[uses.length][];
			for(int i = 0; i < uses.length; i++) {
				mod.uses[i] = CharOperation.concatWith(uses[i].serviceInterface.getTypeName(), '.');
			}
		}
		if (module.servicesCount > 0) {
			ProvidesStatement[] services = module.services;
			mod.provides = new Service[module.servicesCount];
			for (int i = 0; i < module.servicesCount; i++) {
				mod.provides[i] = createService(services[i].serviceInterface, services[i].implementations);
			}
		}
		return mod;
	}

	private static PackageExportImpl createPackageExport(ExportsStatement[] refs, int i) {
		ExportsStatement ref = refs[i];
		PackageExportImpl exp = new PackageExportImpl();
		exp.pack = ref.pkgName;
		ModuleReference[] imp = ref.targets;
		if (imp != null) {
			exp.exportedTo = new char[imp.length][];
			for(int j = 0; j < imp.length; j++) {
				exp.exportedTo[j] = imp[j].moduleName;
			}
		}
		return exp;
	}
	private static Service createService(TypeReference service, TypeReference[] with) {
		Service ser = new Service();
		ser.provides = CharOperation.concatWith(service.getTypeName(), '.');
		ser.with = new char[with.length][];
		for (int i = 0; i < with.length; i++) {
			ser.with[i] = CharOperation.concatWith(with[i].getTypeName(), '.');
		}
		return ser;
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
		return this.provides;
	}
	public IPackageExport[] opens() {
		return this.opens;
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
		if (this.requires != null && this.requires.length > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.requires.length; i++) {
				buffer.append("\trequires "); //$NON-NLS-1$
				if (this.requires[i].isTransitive()) {
					buffer.append("public "); //$NON-NLS-1$
				}
				buffer.append(this.requires[i].name);
				buffer.append(';').append('\n');
			}
		}
		if (this.exports != null && this.exports.length > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.exports.length; i++) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(this.exports[i].toString()).append('\n');
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
				buffer.append('\t').append(ser.toString()).append('\n');
			}
		}
		buffer.append('\n').append('}').toString();
	}

}
