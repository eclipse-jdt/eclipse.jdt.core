package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IJavaModel;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

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
