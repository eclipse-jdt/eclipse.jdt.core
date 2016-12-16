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

import java.util.ArrayList;

import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BinaryModule extends BinaryMember implements IModuleDescription {
	public BinaryModule(JavaElement parent, String name) {
		super(parent, name);
	}
	@Override
	public IModuleDescription.IModuleReference[] getRequiredModules() throws JavaModelException {
		ArrayList list = getChildrenOfType(MODULE_REFERENCE);
		IModuleDescription.IModuleReference[] array= new IModuleDescription.IModuleReference[list.size()];
		list.toArray(array);
		return array;
	}
	@Override
	public IModuleDescription.IPackageExport[] getExportedPackages() throws JavaModelException {
		ArrayList list = getChildrenOfType(PACKAGE_EXPORT);
		IModuleDescription.IPackageExport[] array= new IModuleDescription.IPackageExport[list.size()];
		list.toArray(array);
		return array;
	}
	@Override
	public IModuleDescription.IProvidedService[] getProvidedServices() throws JavaModelException {
		ArrayList list = getChildrenOfType(SERVICE);
		IModuleDescription.IProvidedService[] array= new IModuleDescription.IProvidedService[list.size()];
		list.toArray(array);
		return array;
	}
	@Override
	public String[] getUsedServices() throws JavaModelException {
		ModuleDescriptionInfo info = (ModuleDescriptionInfo) getElementInfo();
		char[][] names= info.uses();
		if (names == null || names.length == 0) {
			return NO_STRINGS;
		}
		return CharOperation.toStrings(names);
	}
	@Override
	public int getFlags() throws JavaModelException {
		ModuleDescriptionInfo info = (ModuleDescriptionInfo) getElementInfo();
		return info.getModifiers();
	}
	public String getKey(boolean forceOpen) throws JavaModelException {
		return getKey(this, forceOpen);
	}
	public String toString(String lineDelimiter) {
		StringBuffer buffer = new StringBuffer();
		try {
			toStringContent(buffer, lineDelimiter);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer.toString();
	}
	protected void toStringContent(StringBuffer buffer, String lineDelimiter) throws JavaModelException {
		IModuleDescription.IPackageExport[] exports = getExportedPackages();
		IModuleDescription.IModuleReference[] requires = getRequiredModules();
		buffer.append("module "); //$NON-NLS-1$
		buffer.append(this.name).append(' ');
		buffer.append('{').append(lineDelimiter);
		if (exports != null) {
			for(int i = 0; i < exports.length; i++) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(exports[i].toString());
				buffer.append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter);
		if (requires != null) {
			for(int i = 0; i < requires.length; i++) {
				buffer.append("\trequires "); //$NON-NLS-1$
				if (requires[i].isPublic()) {
					buffer.append(" public "); //$NON-NLS-1$
				}
				buffer.append(requires[i].getElementName());
				buffer.append(';').append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter).append('}').toString();
	}
	@Override
	public int getElementType() {
		return JAVA_MODULE;
	}
}