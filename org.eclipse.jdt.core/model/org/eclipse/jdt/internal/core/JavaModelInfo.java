/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

/**
 * Implementation of IJavaModel. A Java Model is specific to a
 * workspace. To retrieve a workspace's model, use the
 * <code>#getJavaModel(IWorkspace)</code> method.
 *
 * @see IJavaModel
 */
public class JavaModelInfo extends OpenableElementInfo {



	/**
	 * Backpointer to my Java Model handle
	 */
	protected JavaModel fJavaModel= null;

/**
 * Constructs a new Java Model Info 
 */
protected JavaModelInfo(JavaModel javaModel) {
	this.fJavaModel= javaModel;
}

/**
 * Returns the Java Model for this info.
 */
protected JavaModel getJavaModel() {
	return fJavaModel;
}
}
