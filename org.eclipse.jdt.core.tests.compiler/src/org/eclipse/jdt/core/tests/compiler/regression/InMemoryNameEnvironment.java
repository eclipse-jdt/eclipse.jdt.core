/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.lookup.ModuleEnvironment;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public class InMemoryNameEnvironment extends ModuleEnvironment {
	INameEnvironment[] classLibs;
	HashtableOfObject compilationUnits = new HashtableOfObject();
public InMemoryNameEnvironment(String[] compilationUnits, INameEnvironment[] classLibs) {
	this.classLibs = classLibs;
	for (int i = 0, length = compilationUnits.length - 1; i < length; i += 2) {
		String fileName = compilationUnits[i];
		char[] contents = compilationUnits[i + 1].toCharArray();
		String dirName = "";
		int lastSlash = -1;
		if ((lastSlash = fileName.lastIndexOf('/')) != -1) {
			dirName = fileName.substring(0, lastSlash);
		}
		char[] packageName = dirName.replace('/', '.').toCharArray();
		char[] cuName = fileName.substring(lastSlash == -1 ? 0 : lastSlash + 1, fileName.length() - 5).toCharArray(); // remove ".java"
		HashtableOfObject cus = (HashtableOfObject)this.compilationUnits.get(packageName);
		if (cus == null) {
			cus = new HashtableOfObject();
			this.compilationUnits.put(packageName, cus);
		}
		CompilationUnit unit = new CompilationUnit(contents, fileName, null);
		cus.put(cuName, unit);
	}
}
public NameEnvironmentAnswer findType(char[][] compoundTypeName, char[] module) {
	return findType(
		compoundTypeName[compoundTypeName.length - 1],
		CharOperation.subarray(compoundTypeName, 0, compoundTypeName.length - 1), module);
}
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] module) {
	HashtableOfObject cus = (HashtableOfObject)this.compilationUnits.get(CharOperation.concatWith(packageName, '.'));
	if (cus == null) {
		return findTypeFromClassLibs(typeName, packageName, module);
	}
	CompilationUnit unit = (CompilationUnit)cus.get(typeName);
	if (unit == null) {
		return findTypeFromClassLibs(typeName, packageName, module);
	}
	return new NameEnvironmentAnswer(unit, null /*no access restriction*/);
}
private NameEnvironmentAnswer findTypeFromClassLibs(char[] typeName, char[][] packageName, char[] module) {
	for (int i = 0; i < this.classLibs.length; i++) {
		NameEnvironmentAnswer answer = this.classLibs[i].findType(typeName, packageName, module);
		if (answer != null) {
			return answer;
		}
	}
	return null;
}
public boolean isPackage(char[][] parentPackageName, char[] packageName, char[] module) {
	char[] pkg = CharOperation.concatWith(parentPackageName, packageName, '.');
	return
		this.compilationUnits.get(pkg) != null ||
		isPackageFromClassLibs(parentPackageName, packageName, module);
}
public boolean isPackageFromClassLibs(char[][] parentPackageName, char[] packageName, char[] module) {
	for (int i = 0; i < this.classLibs.length; i++) {
		if (this.classLibs[i].isPackage(parentPackageName, packageName, module)) {
			return true;
		}
	}
	return false;
}
public void cleanup() {
	for (int i = 0, max = this.classLibs.length; i < max; i++) {
		this.classLibs[i].cleanup();
	}
	this.compilationUnits = new HashtableOfObject();
}
@Override
public NameEnvironmentAnswer findType(char[][] compoundTypeName, IModule[] modules) {
	// TODO BETA_JAVA9
	return null;
}
@Override
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModule[] modules) {
	// TODO BETA_JAVA9
	return null;
}
@Override
public boolean isPackage(char[][] parentPackageName, char[] packageName, IModule[] module) {
	return false;
}
@Override
public IModule getModule(char[] name) {
	// TODO Auto-generated method stub
	return null;
}
}
