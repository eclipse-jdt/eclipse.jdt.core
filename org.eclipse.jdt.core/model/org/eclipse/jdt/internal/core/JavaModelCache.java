/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

/**
 * The cache of java elements to their respective info.
 */
public class JavaModelCache {
	public static final int CACHE_RATIO = 20;
	
	/**
	 * Active Java Model Info
	 */
	protected JavaModelInfo modelInfo;
	
	/**
	 * Cache of open projects.
	 */
	protected OverflowingLRUCache projectCache;
	
	/**
	 * Cache of open package fragment roots.
	 */
	protected OverflowingLRUCache rootCache;
	
	/**
	 * Cache of open package fragments
	 */
	protected OverflowingLRUCache pkgCache;

	/**
	 * Cache of open compilation unit and class files
	 */
	protected OverflowingLRUCache openableCache;

	/**
	 * Cache of open children of openable Java Model Java elements
	 */
	protected Map childrenCache;
	
public JavaModelCache() {
	this.projectCache = new ElementCache(CACHE_RATIO); // average 38300 bytes per project -> maximum size : 38300*CACHE_RATIO bytes
	this.rootCache = new ElementCache(CACHE_RATIO*10); // average 2590 bytes per root -> maximum size : 25900*CACHE_RATIO bytes
	this.pkgCache = new ElementCache(CACHE_RATIO*100); // average 1782 bytes per pkg -> maximum size : 178200*CACHE_RATIO bytes
	this.openableCache = new ElementCache(CACHE_RATIO*100); // average 6629 bytes per openable (includes children) -> maximum size : 662900*CACHE_RATIO bytes
	this.childrenCache = new HashMap(CACHE_RATIO*10*20); // average 20 children per openable
}

/**
 *  Returns the info for the element.
 */
public Object getInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return this.modelInfo;
		case IJavaElement.JAVA_PROJECT:
			return this.projectCache.get(element);
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return this.rootCache.get(element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return this.pkgCache.get(element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return this.openableCache.get(element);
		default:
			return this.childrenCache.get(element);
	}
}

/**
 *  Returns the info for this element without
 *  disturbing the cache ordering.
 */
protected Object peekAtInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return this.modelInfo;
		case IJavaElement.JAVA_PROJECT:
			return this.projectCache.peek(element);
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return this.rootCache.peek(element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return this.pkgCache.peek(element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return this.openableCache.peek(element);
		default:
			return this.childrenCache.get(element);
	}
}

/**
 * Remember the info for the element.
 */
protected void putInfo(IJavaElement element, Object info) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			this.modelInfo = (JavaModelInfo) info;
			break;
		case IJavaElement.JAVA_PROJECT:
			this.projectCache.put(element, info);
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			this.rootCache.put(element, info);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			this.pkgCache.put(element, info);
			break;
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			this.openableCache.put(element, info);
			break;
		default:
			this.childrenCache.put(element, info);
	}
}
/**
 * Removes the info of the element from the cache.
 */
protected void removeInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			this.modelInfo = null;
			break;
		case IJavaElement.JAVA_PROJECT:
			this.projectCache.remove(element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			this.rootCache.remove(element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			this.pkgCache.remove(element);
			break;
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			this.openableCache.remove(element);
			break;
		default:
			this.childrenCache.remove(element);
	}
}
public String toStringFillingRation(String prefix) {
	StringBuffer buffer = new StringBuffer();
	buffer.append(prefix);
	buffer.append("Project cache: "); //$NON-NLS-1$
	buffer.append(NumberFormat.getInstance().format(this.projectCache.fillingRatio()));
	buffer.append("%\n"); //$NON-NLS-1$
	buffer.append(prefix);
	buffer.append("Root cache: "); //$NON-NLS-1$
	buffer.append(NumberFormat.getInstance().format(this.rootCache.fillingRatio()));
	buffer.append("%\n"); //$NON-NLS-1$
	buffer.append(prefix);
	buffer.append("Package cache: "); //$NON-NLS-1$
	buffer.append(NumberFormat.getInstance().format(this.pkgCache.fillingRatio()));
	buffer.append("%\n"); //$NON-NLS-1$
	buffer.append(prefix);
	buffer.append("Openable cache: "); //$NON-NLS-1$
	buffer.append(NumberFormat.getInstance().format(this.openableCache.fillingRatio()));
	buffer.append("%\n"); //$NON-NLS-1$
	return buffer.toString();
}
}
