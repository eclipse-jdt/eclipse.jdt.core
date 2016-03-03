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
package org.eclipse.jdt.internal.compiler.classfmt;

import java.util.Arrays;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IModule;

public class ModuleInfo extends ClassFileStruct implements IModule {
	protected int requiresCount;
	protected int exportsCount;
	protected int usesCount;
	protected int providesCount;
	protected char[] name;
	protected ModuleReferenceInfo[] requires;
	protected PackageExportInfo[] exports;
	char[][] uses;
	IModule.IService[] provides;

	public int requiresCount() {
		return this.requiresCount;
	}
	public int exportsCount() {
		return this.exportsCount;
	}
	public int usesCount() {
		return this.usesCount;
	}
	public int providesCount() {
		return this.providesCount;
	}
	@Override
	public char[] name() {
		return this.name;
	}
	public void setName(char[] name) {
		this.name = name;
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
		return this.provides;
	}
	/**
	 * @param name char[]
	 * @param classFileBytes byte[]
	 * @param offsets int[]
	 * @param offset int
	 */
	protected ModuleInfo (char[] name, byte classFileBytes[], int offsets[], int offset) {
		super(classFileBytes, offsets, offset);
		this.name = name;
	}

	private static final char[] MODULE_INFO_SUFFIX = "/module-info".toCharArray(); //$NON-NLS-1$
	private static final int MODULE_SUFFIX_LENGTH = MODULE_INFO_SUFFIX.length;

	public static ModuleInfo createModule(char[] className, byte classFileBytes[], int offsets[], int offset) {
		if (CharOperation.endsWith(className, MODULE_INFO_SUFFIX)) {
			className = CharOperation.subarray(className, 0, className.length - MODULE_SUFFIX_LENGTH);
			CharOperation.replace(className, '/', '.');
		}

		ModuleInfo module = new ModuleInfo(className, classFileBytes, offsets, 0);
		int readOffset = offset;
		int utf8Offset = module.constantPoolOffsets[module.u2At(readOffset)];
//		module.name = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1)); // returns 'Module' 
		int moduleOffset = readOffset + 6;
		int count = module.u2At(moduleOffset);
		if (count  > 0) {
			module.requiresCount = count;
			module.requires = new ModuleReferenceInfo[count];
			moduleOffset += 2;
			for (int i = 0; i < count; i++) {
				utf8Offset = module.constantPoolOffsets[module.u2At(moduleOffset)];
				char[] requiresNames = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
				module.requires[i] = module.new ModuleReferenceInfo();
				module.requires[i].refName = requiresNames;
				moduleOffset += 2;
				int pub = module.u2At(moduleOffset);
				module.requires[i].isPublic = (ClassFileConstants.ACC_PUBLIC == pub); // Access modifier
				moduleOffset += 2;
			}
		}
		count = module.u2At(moduleOffset);
		if (count > 0) {
			moduleOffset += 2;
			module.exportsCount = count;
			module.exports = new PackageExportInfo[count];
			for (int i = 0; i < count; i++) {
				utf8Offset = module.constantPoolOffsets[module.u2At(moduleOffset)];
				char[] exported = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
				CharOperation.replace(exported, '/', '.');
				PackageExportInfo pack = module.new PackageExportInfo();
				module.exports[i] = pack;
				pack.packageName = exported;
				moduleOffset += 2;
				int exportedtoCount = module.u2At(moduleOffset);
				moduleOffset += 2;
				if (exportedtoCount > 0) {
					pack.exportedTo = new char[exportedtoCount][];
					pack.exportedToCount = exportedtoCount;
					for(int k = 0; k < exportedtoCount; k++) {
						utf8Offset = module.constantPoolOffsets[module.u2At(moduleOffset)];
						char[] exportedToName = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
						pack.exportedTo[k] = exportedToName;
						moduleOffset += 2;
					}
				}
			}
		}
		return module;
	}
	class ModuleReferenceInfo implements IModule.IModuleReference {
		char[] refName;
		boolean isPublic = false;
		@Override
		public char[] name() {
			return this.refName;
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
			return CharOperation.equals(this.refName, mod.name(), false);
		}
		@Override
		public int hashCode() {
			return this.refName.hashCode();
		}
	}
	class PackageExportInfo implements IModule.IPackageExport {
		char[] packageName;
		char[][] exportedTo;
		int exportedToCount;
		@Override
		public char[] name() {
			return this.packageName;
		}

		@Override
		public char[][] exportedTo() {
			return this.exportedTo;
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			toStringContent(buffer);
			return buffer.toString();
		}
		protected void toStringContent(StringBuffer buffer) {
			buffer.append(this.packageName);
			if (this.exportedToCount > 0) {
				buffer.append(" to "); //$NON-NLS-1$
				for(int i = 0; i < this.exportedToCount; i++) {
					buffer.append(this.exportedTo[i]);
					buffer.append(',').append(' ');
				}
			}
			buffer.append(';').append('\n');
		}
	}
	class ServiceInfo implements IModule.IService {
		char[] serviceName;
		char[] with;
		@Override
		public char[] name() {
			return this.serviceName;
		}

		@Override
		public char[] with() {
			return this.with;
		}
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
		if (this.requiresCount > 0) {
			for(int i = 0; i < this.requiresCount; i++) {
				buffer.append("\trequires "); //$NON-NLS-1$
				if (this.requires[i].isPublic) {
					buffer.append(" public "); //$NON-NLS-1$
				}
				buffer.append(this.requires[i].refName);
				buffer.append(';').append('\n');
			}
		}
		if (this.exportsCount > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.exportsCount; i++) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(this.exports[i].toString());
			}
		}
		buffer.append('\n').append('}').toString();
	}
}
