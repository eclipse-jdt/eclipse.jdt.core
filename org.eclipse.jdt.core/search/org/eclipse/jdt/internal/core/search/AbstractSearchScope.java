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

/**
 * @see IJavaSearchScope#includesBinaries()
 * @deprecated
 */
public boolean includesBinaries() {
	return true;
}

/**
 * @see IJavaSearchScope#includesClasspaths()
 * @deprecated
 */
public boolean includesClasspaths() {
	return true;
}

/* (non-Javadoc)
 * Process the given delta and refresh its internal state if needed.
 * Returns whether the internal state was refreshed.
 */
public abstract void processDelta(IJavaElementDelta delta);

/**
 * @see IJavaSearchScope#setIncludesBinaries(boolean)
 * @deprecated
 */
public void setIncludesBinaries(boolean includesBinaries) {
}

/**
 * @see IJavaSearchScope#setIncludesClasspaths(boolean)
 * @deprecated
 */
public void setIncludesClasspaths(boolean includesClasspaths) {
}

}
