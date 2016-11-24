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
package org.eclipse.jdt.internal.compiler.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ExportReference;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModulePathEntry;

public class BasicModule implements IModule {
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
			return CharOperation.equals(this.name, mod.name());
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

	boolean isAutomodule;
	char[] name;
	IModule.IModuleReference[] requires;
	IModule.IPackageExport[] exports;
	char[][] uses;
	Service[] provides;
	IModulePathEntry root;
	public BasicModule(ModuleDeclaration descriptor, IModulePathEntry root) {
		this.name = descriptor.moduleName;
		this.root = root;
		if (descriptor.requiresCount > 0) {
			ModuleReference[] refs = descriptor.requires;
			this.requires = new ModuleReferenceImpl[refs.length];
			for (int i = 0; i < refs.length; i++) {
				ModuleReferenceImpl ref = new ModuleReferenceImpl();
				ref.name = CharOperation.concatWith(refs[i].tokens, '.');
				ref.isPublic = refs[i].isPublic();
				this.requires[i] = ref;
			}
		} else {
			this.requires = new ModuleReferenceImpl[0];
		}
		if (descriptor.exportsCount > 0) {
			ExportReference[] refs = descriptor.exports;
			this.exports = new PackageExport[refs.length];
			for (int i = 0; i < refs.length; i++) {
				PackageExport exp = createPackageExport(refs, i);
				this.exports[i] = exp;
			}
		} else {
			this.exports = new PackageExport[0];
		}
		if (descriptor.usesCount > 0) {
			TypeReference[] u = descriptor.uses;
			this.uses = new char[u.length][];
			for(int i = 0; i < u.length; i++) {
				this.uses[i] = CharOperation.concatWith(u[i].getTypeName(), '.');
			}
		}
		if (descriptor.servicesCount > 0) {
			TypeReference[] services = descriptor.interfaces;
			TypeReference[] with = descriptor.implementations;
			this.provides = new Service[descriptor.servicesCount];
			for (int i = 0; i < descriptor.servicesCount; i++) {
				this.provides[i] = createService(services[i], with[i]);
			}
		}
		this.isAutomodule = false; // Just to be explicit
	}
	public BasicModule(char[] name, Classpath root, boolean isAuto) {
		this.name = name;
		this.root = root;
		this.exports = IModule.NO_EXPORTS;
		this.requires = IModule.NO_MODULE_REFS;
		this.isAutomodule = isAuto;
	}
	@Override
	public char[] name() {
		return this.name;
	}
	@Override
	public IModule.IModuleReference[] requires() {
		return this.requires;
	}
	@Override
	public IModule.IPackageExport[] exports() {
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
	@Override
	public boolean isAutomatic() {
		return this.isAutomodule;
	}
	public void addReads(char[] modName) {
		Predicate<char[]> shouldAdd = m -> {
			return Stream.of(this.requires).map(ref -> ref.name()).noneMatch(n -> CharOperation.equals(modName, n));
		};
		if (shouldAdd.test(modName)) {
			int len = this.requires.length;
			this.requires = Arrays.copyOf(this.requires, len + 1);
			ModuleReferenceImpl info = new ModuleReferenceImpl();
			info.name = modName;
			this.requires[len] = info;
		}		
	}
	public void addExports(IModule.IPackageExport[] toAdd) {
		Predicate<char[]> shouldAdd = m -> {
			return Stream.of(this.exports).map(ref -> ((PackageExport) ref).pack).noneMatch(n -> CharOperation.equals(m, n));
		};
		Collection<IPackageExport> merged = Stream.concat(Stream.of(this.exports), Stream.of(toAdd)
				.filter(e -> shouldAdd.test(e.name()))
				.map(e -> {
					PackageExport exp = new PackageExport();
					exp.pack = ((PackageExport )e).name();
					exp.exportedTo = ((PackageExport )e).exportedTo();
					return exp;
				}))
			.collect(
				ArrayList::new,
				ArrayList::add,
				ArrayList::addAll);
		this.exports = merged.toArray(new PackageExport[merged.size()]);
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
				if (this.requires[i].isPublic()) {
					buffer.append(" public "); //$NON-NLS-1$
				}
				buffer.append(this.requires[i].name());
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