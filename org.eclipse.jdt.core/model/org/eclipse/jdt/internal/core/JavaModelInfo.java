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
import java.util.Hashtable;
import java.util.zip.ZipFile;

/**
 * Implementation of IJavaModel. The Java Model maintains a cache of
 * active IJavaProjects in a workspace. A Java Model is specific to a
 * workspace. To retrieve a workspace's model, use the
 * <code>#getJavaModel(IWorkspace)</code> method.
 *
 * @see IJavaModel
 */
public class JavaModelInfo extends OpenableElementInfo {

	/**
	 * Cache of open openable Java Model Java elements
	 */
	protected OverflowingLRUCache fLRUCache = null;

	/**
	 * Cache of open children of openable Java Model Java elements
	 */
	protected Hashtable fChildrenCache = null;

	/**
	 * Set of elements which are out of sync with their buffers.
	 */
	protected Hashtable fElementsOutOfSynchWithBuffers = new Hashtable(11);

	/**
	 * Backpointer to my Java Model handle
	 */
	protected IJavaModel fJavaModel = null;

	/**
	 * The workspace this Java Model Info corresponds to
	 */
	protected IWorkspace workspace;

	/**
	 * Constructs a new Java Model Info 
	 */
	protected JavaModelInfo(IJavaModel javaModel, IWorkspace workspace) {
		this.workspace = workspace;
		this.fJavaModel = javaModel;
		this.fLRUCache = new ElementCache(5000);
		this.fChildrenCache = new Hashtable(30000);
	}

	/**
	 * @see IJavaModel#close()
	 */
	public void close() throws JavaModelException {
		//close any remaining "parent-less" handles in the LRUCache
		Enumeration handles = fLRUCache.keys();
		while (handles.hasMoreElements()) {
			IJavaElement handle = (IJavaElement) handles.nextElement();
			// can't close myself - (am in the process of that now)
			if (!handle.equals(fJavaModel)) {
				((IOpenable) handle).close();
			}
		}
	}

	/**
	 * Returns the Java Model for this info.
	 */
	protected IJavaModel getJavaModel() {
		return fJavaModel;
	}

}
