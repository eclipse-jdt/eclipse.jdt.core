/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.TypeNameMatch;

/**
 * Specific match collected while searching for all type names
 * when type belongs to an external resource.
 * 
 * @since 3.3
 */
public class ExternalTypeNameMatch extends TypeNameMatch {
	private String projectPath;
	private IPackageFragmentRoot root;

public ExternalTypeNameMatch(int modifiers, char[] packageName, char[] typeName, char[][] enclosingTypeNames, String path, String project) {
	super(modifiers, packageName, typeName, enclosingTypeNames, path);
	this.projectPath = project;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.search.TypeNameMatch#getProject()
 */
protected IProject getProject() {
	return ResourcesPlugin.getWorkspace().getRoot().getProject(this.projectPath);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.search.TypeNameMatch#getPackageFragmentRoot()
 */
protected IPackageFragmentRoot getPackageFragmentRoot() {
	if (this.root == null) {
		int separatorIndex = getPath().indexOf(IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR);
		String jarPath = getPath().substring(0, separatorIndex);
		IJavaProject javaProject = JavaCore.create(getProject());
		if (javaProject == null) return null; // cannot initialize without a project
		this.root = javaProject.getPackageFragmentRoot(jarPath);
	}
	return root;
}
}
