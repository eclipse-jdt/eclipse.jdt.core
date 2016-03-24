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
import org.eclipse.jdt.internal.compiler.env.IModuleLocation;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.util.Util;

public abstract class ClasspathLocation implements IModuleLocation {

	protected static final String MODULE_INFO_JAVA = "MODULE-INFO.JAVA"; //$NON-NLS-1$
	protected static final String MODULE_INFO_CLASS = "module-info.class"; //$NON-NLS-1$

	static ClasspathLocation forSourceFolder(IContainer sourceFolder, IContainer outputFolder,
			char[][] inclusionPatterns, char[][] exclusionPatterns, boolean ignoreOptionalProblems,
			INameEnvironment env) {
		return new ClasspathMultiDirectory(sourceFolder, outputFolder, inclusionPatterns, exclusionPatterns,
				ignoreOptionalProblems, env).initializeModule();
	}
public static ClasspathLocation forBinaryFolder(IContainer binaryFolder, boolean isOutputFolder, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, INameEnvironment env) {
	return new ClasspathDirectory(binaryFolder, isOutputFolder, accessRuleSet, externalAnnotationPath, env).initializeModule();
}

static ClasspathLocation forLibrary(String libraryPathname, 
										long lastModified, 
										AccessRuleSet accessRuleSet, 
										IPath annotationsPath,
										INameEnvironment env) {
	return Util.isJrt(libraryPathname) ?
			new ClasspathJrt(libraryPathname, annotationsPath, env) :
			new ClasspathJar(libraryPathname, lastModified, accessRuleSet, annotationsPath, env);
}

public static ClasspathLocation forLibrary(String libraryPathname, AccessRuleSet accessRuleSet, IPath annotationsPath,
											INameEnvironment env) {
	return forLibrary(libraryPathname, 0, accessRuleSet, annotationsPath, env);
}

static ClasspathLocation forLibrary(IFile library, AccessRuleSet accessRuleSet, IPath annotationsPath,
										INameEnvironment env) {
	return new ClasspathJar(library, accessRuleSet, annotationsPath, env);
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

}
