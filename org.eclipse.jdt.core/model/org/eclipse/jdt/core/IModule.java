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
package org.eclipse.jdt.core;

/**
 * @since 3.12 BETA_JAVA9
 */
public interface IModule {

	public char[] name();

	public IPackageExport[] exports() throws JavaModelException;

	public IModuleReference requires() throws JavaModelException;

	public interface IPackageExport {
		IPackageFragment getExportedPackage();
		IModule getTargetModule();
	}
	public interface IModuleReference {
		public IModule module();
		public boolean isPublic();
	}
}
