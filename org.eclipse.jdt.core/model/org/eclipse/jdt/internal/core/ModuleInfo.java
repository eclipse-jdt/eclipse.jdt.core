package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ExportReference;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.IModule;

public class ModuleInfo extends SourceTypeElementInfo implements IModule {

	protected static final IModuleReference[] NO_REQUIRES = new IModuleReference[0];
	protected static final IPackageExport[] NO_EXPORTS = new IPackageExport[0];
	protected static final IService[] NO_SERVICES = new IService[0];
	protected static final char[][] NO_USES = new char[0][0];

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
	char[] name;
	ModuleReferenceImpl[] requires;
	PackageExport[] exports;
	char[][] uses;
	Service[] provides;
	@Override
	public char[] name() {
		return this.name;
	}

	public static ModuleInfo createModule(ModuleDeclaration module) {
		ModuleInfo mod = new ModuleInfo();
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
