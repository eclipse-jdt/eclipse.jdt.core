package org.eclipse.jdt.internal.core.search;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.core.resources.IResource;

public abstract class AbstractSearchScope implements IJavaSearchScope {

	/* (non-Javadoc)
	 * Process the given delta and refresh its internal state if needed.
	 * Returns whether the internal state was refreshed.
	 */
	public abstract void processDelta(IJavaElementDelta delta);
}
