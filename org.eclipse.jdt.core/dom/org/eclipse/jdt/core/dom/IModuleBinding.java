/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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

package org.eclipse.jdt.core.dom;

/**
 * A module binding represents a module.
 *
 * @since 3.13 BETA_JAVA9
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IModuleBinding extends IBinding {

	public static class Service {
		public ITypeBinding service;
		public ITypeBinding[] implementations;
	}

	@Override
	public default int getKind() {
		return IBinding.MODULE;
	}

	/**
	 * answers whether the module is open or not
	 * @return <code>true</code> if open, <code>false</code> otherwise
	 */
	public abstract boolean isOpen();

	/**
	 * All the required modules, transitive and otherwise
	 * @return required modules
	 */
	public abstract IModuleBinding[] getRequiredModules();

	/**
	 *
	 * @return array of exported package bindings
	 */
	public abstract IPackageBinding[] getExportedPackages();

	/**
	 * if targeted, returns the array of targeted modules, else returns an empty array.
	 * @param packageBinding
	 * @return array of targeted modules
	 */
	public abstract IModuleBinding[] getExportedTo(IPackageBinding packageBinding);

	/**
	 *
	 * @return array of open package bindings
	 */
	public abstract IPackageBinding[] getOpenPackages();

	/**
	 * if targeted open, returns the array of targeted module bindings else empty array.
	 *
	 * @param packageBinding
	 * @return array of targeted module bindings
	 */
	public abstract IModuleBinding[] getOpenedTo(IPackageBinding packageBinding);

	/**
	 *
	 * @return array of uses type bindings
	 */
	public abstract ITypeBinding[] getUses();

	/**
	 *
	 * @return array of service interfaces
	 */
	public abstract Service[] getServices();

}