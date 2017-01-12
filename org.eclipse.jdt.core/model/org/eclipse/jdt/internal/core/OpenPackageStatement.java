/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
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

import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo.PackageExportInfo;
import org.eclipse.jdt.internal.core.util.Util;

public class OpenPackageStatement extends SourceRefElement implements IModuleDescription.IOpenPackage {

	private String pack;
	public OpenPackageStatement(JavaElement parent, String pack) {
		super(parent);
		this.pack = pack;
	}
	@Override
	public String getPackageName() {
		return this.pack;
	}
	@Override
	public String[] getTargetModules() throws JavaModelException {
		PackageExportInfo info = (PackageExportInfo) getElementInfo();
		char[][] targets = info.targets();
		if (targets == null || targets.length == 0)
			return NO_STRINGS;
		String[] t = new String[targets.length];
		for(int i = 0; i < t.length; i++) {
			t[i] = new String(targets[i]);
		}
		return t;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.pack);
		buffer.append(';');
		return buffer.toString();
	}
	@Override
	public int getElementType() {
		return OPEN_PACKAGE;
	}
	public ISourceRange getNameRange() throws JavaModelException {
		return null;
	}
	@Override
	protected char getHandleMementoDelimiter() {
		return 0;
	}
	public int hashCode() {
		int hash = super.hashCode();
		return Util.combineHashCodes(hash, this.pack.hashCode());
	}
	public boolean equals(Object o) {
		if (!(o instanceof OpenPackageStatement)) 
			return false;
		return super.equals(o) && this.pack.equals(((OpenPackageStatement) o).pack);
	}
}
