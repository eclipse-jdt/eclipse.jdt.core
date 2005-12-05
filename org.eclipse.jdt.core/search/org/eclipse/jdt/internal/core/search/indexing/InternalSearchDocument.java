/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.Index;

/**
 * Internal search document implementation
 */
public class InternalSearchDocument {
	Index index;
	private String containerRelativePath;
	/*
	 * Hidden by API SearchDocument subclass
	 */
	public void addIndexEntry(char[] category, char[] key) {
		if (this.index != null) {
			index.addIndexEntry(category, key, getContainerRelativePath());
			if (category == IIndexConstants.TYPE_DECL && key != null) {
				int length = key.length;
				if (length > 1 && key[length-2] == IIndexConstants.SEPARATOR && key[length-1] == IIndexConstants.SECONDARY_SUFFIX ) {
					// This is a key of a secondary type => reset java model manager secondary types cache for document path project
					JavaModelManager manager = JavaModelManager.getJavaModelManager();
					manager.resetSecondaryTypesCache(getPath());
//					manager.addSecondaryType(getPath(), key);
				}
			}
		}
	}
	private String getContainerRelativePath() {
		if (this.containerRelativePath == null)
			this.containerRelativePath = this.index.containerRelativePath(getPath());
		return this.containerRelativePath;
	}
	/*
	 * Hidden by API SearchDocument subclass
	 */
	public void removeAllIndexEntries() {
		if (this.index != null)
			index.remove(getContainerRelativePath());
	}
	/*
	 * Hidden by API SearchDocument subclass
	 */
	public String getPath() {
		return null; // implemented by subclass
	}
}
