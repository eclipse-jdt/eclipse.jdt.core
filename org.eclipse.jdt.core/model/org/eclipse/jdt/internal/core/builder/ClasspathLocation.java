/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.util.Util;

public abstract class ClasspathLocation {

	protected boolean isOnModulePath;
	protected IModule module;
	// In the following signatures, passing a null moduleName signals "don't care":
	abstract public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName);
	abstract public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName, boolean asBinaryOnly);
	abstract public boolean isPackage(String qualifiedPackageName, String moduleName);
	public char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		return singletonModuleNameIf(isPackage(qualifiedPackageName, moduleName));
	}
	public boolean hasModule() { return getModule() != null; }

	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
		String fileName = new String(typeName);
		return findClass(fileName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, asBinaryOnly);
	}
	public void setModule (IModule mod) {
		this.module = mod;
	}
	public IModule getModule() {
		return this.module;
	}
	static ClasspathLocation forSourceFolder(IContainer sourceFolder, IContainer outputFolder,
			char[][] inclusionPatterns, char[][] exclusionPatterns, boolean ignoreOptionalProblems,
			INameEnvironment env) {
		return new ClasspathMultiDirectory(sourceFolder, outputFolder, inclusionPatterns, exclusionPatterns,
				ignoreOptionalProblems, env);
	}
public static ClasspathLocation forBinaryFolder(IContainer binaryFolder, boolean isOutputFolder, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, INameEnvironment env, boolean autoModule) {
	return new ClasspathDirectory(binaryFolder, isOutputFolder, accessRuleSet, externalAnnotationPath, env, autoModule);
}

static ClasspathLocation forLibrary(String libraryPathname, 
										long lastModified, 
										AccessRuleSet accessRuleSet, 
										IPath annotationsPath,
										INameEnvironment env,
										boolean autoModule) {
	return Util.isJrt(libraryPathname) ?
			new ClasspathJrt(libraryPathname, annotationsPath, env) :
				Util.archiveFormat(libraryPathname) == Util.JMOD_FILE ?
					new ClasspathJMod(libraryPathname, lastModified, accessRuleSet, annotationsPath, env) :
			new ClasspathJar(libraryPathname, lastModified, accessRuleSet, annotationsPath, env, autoModule);

}

public static ClasspathLocation forLibrary(String libraryPathname, AccessRuleSet accessRuleSet, IPath annotationsPath,
											INameEnvironment env, boolean autoModule) {
	return forLibrary(libraryPathname, 0, accessRuleSet, annotationsPath, env, autoModule);
}

static ClasspathLocation forLibrary(IFile library, AccessRuleSet accessRuleSet, IPath annotationsPath,
										INameEnvironment env, boolean autoModule) {
	return new ClasspathJar(library, accessRuleSet, annotationsPath, env, autoModule);
}

public abstract IPath getProjectRelativePath();

public boolean isOutputFolder() {
	return false;
}

public void cleanup() {
	// free anything which is not required when the state is saved
}
public void reset() {
	// reset any internal caches before another compile loop starts
}

public abstract String debugPathString();

public char[][] singletonModuleNameIf(boolean condition) {
	if (!condition)
		return null;
	if (this.module != null)
		return new char[][] { this.module.name() };
	return new char[][] { ModuleBinding.UNNAMED };
}
}
