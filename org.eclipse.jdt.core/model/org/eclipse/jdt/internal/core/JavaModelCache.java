/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

/**
 * The cache of java elements to their respective info.
 */
public class JavaModelCache {
	public static final int BASE_VALUE = 20;
	public static final int DEFAULT_PROJECT_SIZE = 5;  // average 25552 bytes per project.
	public static final int DEFAULT_ROOT_SIZE = BASE_VALUE*10; // average 2590 bytes per root -> maximum size : 25900*BASE_VALUE bytes
	public static final int DEFAULT_PKG_SIZE = BASE_VALUE*100; // average 1782 bytes per pkg -> maximum size : 178200*BASE_VALUE bytes
	public static final int DEFAULT_OPENABLE_SIZE = BASE_VALUE*100; // average 6629 bytes per openable (includes children) -> maximum size : 662900*BASE_VALUE bytes
	public static final int DEFAULT_CHILDREN_SIZE = BASE_VALUE*100*20; // average 20 children per openable
	
	/**
	 * Active Java Model Info
	 */
	protected JavaModelInfo modelInfo;
	
	/**
	 * Cache of open projects.
	 */
	protected HashMap projectCache;
	
	/**
	 * Cache of open package fragment roots.
	 */
	protected ElementCache rootCache;
	
	/**
	 * Cache of open package fragments
	 */
	protected ElementCache pkgCache;

	/**
	 * Cache of open compilation unit and class files
	 */
	protected ElementCache openableCache;

	/**
	 * Cache of open children of openable Java Model Java elements
	 */
	protected Map childrenCache;
	
public JavaModelCache() {
	this.projectCache = new HashMap(DEFAULT_PROJECT_SIZE); // NB: Don't use a LRUCache for projects as they are constantly reopened (e.g. during delta processing)
	this.rootCache = new ElementCache(DEFAULT_ROOT_SIZE);
	this.pkgCache = new ElementCache(DEFAULT_PKG_SIZE);
	this.openableCache = new ElementCache(DEFAULT_OPENABLE_SIZE);
	this.childrenCache = new HashMap(DEFAULT_CHILDREN_SIZE);
}

/*
 * Ensures there is enough room in each ElementCache to put the given new elements.
 */
protected void ensureSpaceLimit(Map newElements) {
	int rootSize = 0;
	IJavaElement project = null;
	int pkgSize = 0;
	IJavaElement root = null;
	int openableSize = 0;
	IJavaElement pkg = null;
	Iterator iterator = newElements.keySet().iterator();
	while (iterator.hasNext()) {
		IJavaElement element = (IJavaElement) iterator.next();
		switch (element.getElementType()) {
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				project = element.getParent();
				rootSize++;
				break;
			case IJavaElement.PACKAGE_FRAGMENT:
				root = element.getParent();
				pkgSize++;
				break;
			case IJavaElement.COMPILATION_UNIT:
			case IJavaElement.CLASS_FILE:
				pkg = element.getParent();
				openableSize++;
				break;
		}
	}
	this.rootCache.ensureSpaceLimit(rootSize, project);
	this.pkgCache.ensureSpaceLimit(pkgSize, root);
	this.openableCache.ensureSpaceLimit(openableSize, pkg);
}

/*
 * The given element is being removed.
 * Ensures that the corresponding children cache's space limit is reset if this was the parent
 * that increased the space limit.
 */
protected void resetSpaceLimit(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_PROJECT:
			this.rootCache.resetSpaceLimit(DEFAULT_ROOT_SIZE, element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			this.pkgCache.resetSpaceLimit(DEFAULT_PKG_SIZE, element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			this.openableCache.resetSpaceLimit(DEFAULT_OPENABLE_SIZE, element);
			break;
	}
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
			return this.projectCache.get(element);
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
	buffer.append(this.projectCache.size());
	buffer.append(" projects\n"); //$NON-NLS-1$
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
