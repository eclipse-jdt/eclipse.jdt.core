/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Implementation of IJavaModel. A Java Model is specific to a
 * workspace.
 *
 * @see IJavaModel
 */
public class JavaModelInfo extends OpenableElementInfo {

	/**
	 * A array with all the non-java projects contained by this model
	 */
	Object[] nonJavaResources;

/**
 * Constructs a new Java Model Info 
 */
protected JavaModelInfo() {
}
/**
 * Compute the non-java resources contained in this java project.
 */
private Object[] computeNonJavaResources() {
	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	int length = projects.length;
	Object[] nonJavaResources = null;
	int index = 0;
	for (int i = 0; i < length; i++) {
		IProject project = projects[i];
		if (!JavaProject.hasJavaNature(project)) {
			if (nonJavaResources == null) {
				nonJavaResources = new Object[length];
			}
			nonJavaResources[index++] = project;
		}
	}
	if (index == 0) return NO_NON_JAVA_RESOURCES;
	if (index < length) {
		System.arraycopy(nonJavaResources, 0, nonJavaResources = new Object[index], 0, index);
	}
	return nonJavaResources;
}

/**
 * Returns an array of non-java resources contained in the receiver.
 */
Object[] getNonJavaResources() {

	Object[] nonJavaResources = this.nonJavaResources;
	if (nonJavaResources == null) {
		nonJavaResources = computeNonJavaResources();
		this.nonJavaResources = nonJavaResources;
	}
	return nonJavaResources;
}
}
