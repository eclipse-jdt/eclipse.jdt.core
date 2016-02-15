/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.ModuleEnvironment;

/**
 * An environment that wraps the client's name environment.
 * This wrapper always considers the wrapped environment then if the name is
 * not found, it search in the code snippet support. This includes the super class
 * org.eclipse.jdt.internal.eval.target.CodeSnippet as well as the global variable classes.
 */
public class CodeSnippetEnvironment extends ModuleEnvironment implements EvaluationConstants {
	INameEnvironment env;
	EvaluationContext context;
/**
 * Creates a new wrapper for the given environment.
 */
public CodeSnippetEnvironment(INameEnvironment env, EvaluationContext context) {
	this.env = env;
	this.context = context;
}
/**
 * @see INameEnvironment#findType(char[][], char[])
 */
public NameEnvironmentAnswer findType(char[][] compoundTypeName, char[] module) {
	NameEnvironmentAnswer result = this.env.findType(compoundTypeName, module);
	if (result != null) {
		return result;
	}
	if (CharOperation.equals(compoundTypeName, ROOT_COMPOUND_NAME)) {
		IBinaryType binary = this.context.getRootCodeSnippetBinary();
		if (binary == null) {
			return null;
		} else {
			return new NameEnvironmentAnswer(binary, null /*no access restriction*/);
		}
	}
	VariablesInfo installedVars = this.context.installedVars;
	ClassFile[] classFiles = installedVars.classFiles;
	for (int i = 0; i < classFiles.length; i++) {
		ClassFile classFile = classFiles[i];
		if (CharOperation.equals(compoundTypeName, classFile.getCompoundName())) {
			ClassFileReader binary = null;
			try {
				binary = new ClassFileReader(classFile.getBytes(), null);
			} catch (ClassFormatException e) {
				e.printStackTrace();  // Should never happen since we compiled this type
				return null;
			}
			return new NameEnvironmentAnswer(binary, null /*no access restriction*/);
		}
	}
	return null;
}
/**
 * @see INameEnvironment#findType(char[], char[][], char[])
 */
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] module) {
	NameEnvironmentAnswer result = this.env.findType(typeName, packageName, module);
	if (result != null) {
		return result;
	}
	return findType(CharOperation.arrayConcat(packageName, typeName), module);
}
/**
 * @see INameEnvironment#isPackage(char[][], char[], char[])
 */
public boolean isPackage(char[][] parentPackageName, char[] packageName, char[] module) {
	return this.env.isPackage(parentPackageName, packageName, module);
}
public void cleanup() {
	this.env.cleanup();
}
@Override
public NameEnvironmentAnswer findType(char[][] compoundTypeName, IModule[] modules) {
	return null;
}
@Override
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModule[] modules) {
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
