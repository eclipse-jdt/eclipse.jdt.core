/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IJavaProject;

public class ContainerInitializer extends ClasspathContainerInitializer {
	public static ITestInitializer initializer;
	
	public static interface ITestInitializer {
		public void initialize(IPath containerPath, IJavaProject project) throws CoreException;
	}
	
	public static void setInitializer(ITestInitializer initializer) {
		ContainerInitializer.initializer = initializer;
	}
	
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		if (initializer == null) return;
		initializer.initialize(containerPath, project);
	}
}
