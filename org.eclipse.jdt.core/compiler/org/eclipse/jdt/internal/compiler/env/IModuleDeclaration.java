/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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

public interface IModuleDeclaration {
	public char[] name();
	public IModuleReference[] requires();

	public IPackageExport[] exports();

	public char[][] uses();

	public IService[] provides();

	public interface IModuleReference {
		public char[] name();
		public boolean isPublic();
	}

	public interface IPackageExport {
		public char[] name();
		public char[][] exportedTo();
	}

	public interface IService {
		public char[] name();
		char[] with();
	}
	
	public default void addReads(char[] modName) {
		// do nothing, would throwing an exception be better?
	}
	
	public default void addExports(IPackageExport[] exports) {
		// do nothing, would throwing an exception be better?
	}
}
