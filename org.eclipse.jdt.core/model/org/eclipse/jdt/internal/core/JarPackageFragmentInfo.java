package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Element info for JarPackageFragments.  Caches the zip entry names
 * of the types (.class files) of the JarPackageFragment.  The entries
 * are used to compute the children of the JarPackageFragment.
 */
class JarPackageFragmentInfo extends PackageFragmentInfo {
	/**
	 * The names of the zip entries that are the class files associated
	 * with this package fragment info in the JAR file of the JarPackageFragmentRootInfo.
	 */
	protected Vector fEntryNames= new Vector();
/**
 */
boolean containsJavaResources() {
	return fEntryNames.size() != 0;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
Object[] getNonJavaResources() {
	return fNonJavaResources;
}
/**
 * Set the names of the zip entries that are the types associated
 * with this package fragment info in the JAR file of the JarPackageFragmentRootInfo.
 */
protected void setEntryNames(Vector entries) {
	fEntryNames = entries;
}
}
