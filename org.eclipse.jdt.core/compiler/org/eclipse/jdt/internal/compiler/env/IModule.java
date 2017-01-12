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
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public interface IModule {

	public static IModuleReference[] NO_MODULE_REFS = new IModuleReference[0];
	public static IPackageExport[] NO_EXPORTS = new IPackageExport[0];
	public static char[][] NO_USES = new char[0][];
	public static IService[] NO_PROVIDES = new IService[0];
	public static IModule[] NO_MODULES = new IModule[0];
	public static IPackageExport[] NO_OPENS = new IPackageExport[0];

	public char[] name();

	public IModuleReference[] requires();

	public IPackageExport[] exports();

	public char[][] uses();

	public IService[] provides();

	/*
	 * the opens package statement is very similar to package export statement, hence
	 * the same internal models are being used here.
	 */
	public IPackageExport[] opens();

	public interface IModuleReference {
		public char[] name();
		public default boolean isTransitive() {
			return (getModifiers() & ClassFileConstants.ACC_TRANSITIVE) != 0;
		}
		public int getModifiers();
		public default boolean isStatic() {
			return (getModifiers() & ClassFileConstants.ACC_STATIC_PHASE) != 0;
		}
	}

	public interface IPackageExport {
		public char[] name();
		public char[][] targets();
		public default boolean isQualified() {
			char[][] targets = targets();
			return targets != null && targets.length > 0;
		}
	}

	public interface IService {
		public char[] name();
		char[][] with();
	}
	
	public default void addReads(char[] modName) {
		// do nothing, would throwing an exception be better?
	}
	
	public default void addExports(IPackageExport[] exports) {
		// do nothing, would throwing an exception be better?
	}

	public default boolean isAutomatic() {
		return false;
	}
}
