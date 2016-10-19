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
package org.eclipse.jdt.internal.compiler.batch;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.IModule;

public class BinaryModule implements IModule {

	public IModule declaration;
	public BinaryModule(ClassFileReader moduleInfoClass) {
		this.declaration = moduleInfoClass.getModuleDeclaration();
	}
	@Override
	public char[] name() {
		//
		return this.declaration.name();
	}

	@Override
	public IModuleReference[] requires() {
		return this.declaration.requires();
	}
	@Override
	public IPackageExport[] exports() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public char[][] uses() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public IService[] provides() {
		// TODO Auto-generated method stub
		return null;
	}

}
