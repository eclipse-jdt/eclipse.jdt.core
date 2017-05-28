/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation.
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

import org.eclipse.jdt.internal.compiler.env.IModule;

public final class AutoModule implements IModule {
	char[] name;
	public AutoModule(char[] name) {
		this.name = name;
	}
	@Override
	public char[] name() {
		return this.name;
	}

	@Override
	public IModuleReference[] requires() {
		return IModule.NO_MODULE_REFS;
	}

	@Override
	public IPackageExport[] exports() {
		return IModule.NO_EXPORTS;
	}

	@Override
	public char[][] uses() {
		return IModule.NO_USES;
	}

	@Override
	public IService[] provides() {
		return IModule.NO_PROVIDES;
	}

	@Override
	public IPackageExport[] opens() {
		return NO_OPENS;
	}

	public boolean isAutomatic() {
		return true;
	}
	public boolean isOpen() {
		return false;
	}
}