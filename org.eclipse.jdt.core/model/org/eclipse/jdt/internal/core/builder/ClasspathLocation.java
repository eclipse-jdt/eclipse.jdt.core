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
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleEnvironment;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.Util;

public abstract class ClasspathLocation implements IModuleEnvironment {

	protected boolean isAutoModule;
	protected IModule module;
	abstract public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName);
	abstract public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly);
	abstract public boolean isPackage(String qualifiedPackageName);
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
		String fileName = new String(typeName);
		return findClass(fileName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly);
	}
	public void setModule (IModule mod) {
		this.module = mod;
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
			new ClasspathJar(libraryPathname, lastModified, accessRuleSet, annotationsPath, env, false);

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

void acceptModule(IModule mod) {
	if (mod != null) {
		this.module = mod;
	}
}
public IModule getModule() {
	return this.module;
}
/**
 * Specifies whether this entry represents an automatic module.
 * 
 * @return true if this is an automatic module, false otherwise
 */
public boolean isAutomaticModule() {
	return this.isAutoModule;
}
}
