package org.eclipse.jdt.internal.core.search;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IResource;

public abstract class AbstractSearchScope {

	protected IResource[] elements = new IResource[5];
	protected int elementCount = 0;
/**
 * Adds the given resource to this search scope.
 */
public void add(IResource element) {
	if (this.elementCount == this.elements.length) {
		System.arraycopy(
			this.elements,
			0,
			this.elements = new IResource[this.elementCount * 2],
			0,
			this.elementCount);
	}
	elements[elementCount++] = element;
}
/**
 * Returns whether this search scope encloses the given resource.
 */
protected boolean encloses(IResource element) {
	IPath elementPath = element.getFullPath();
	for (int i = 0; i < elementCount; i++) {
		if (this.elements[i].getFullPath().isPrefixOf(elementPath)) {
			return true;
		}
	}
	return false;
}
}
