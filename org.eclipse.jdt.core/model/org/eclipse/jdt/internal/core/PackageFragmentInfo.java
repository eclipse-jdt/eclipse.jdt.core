package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Element info for PackageFragments.
 */
class PackageFragmentInfo extends OpenableElementInfo {

	/**
	 * A array with all the non-java resources contained by this PackageFragment
	 */
	protected Object[] fNonJavaResources;

/**
 * Create and initialize a new instance of the receiver
 */
public PackageFragmentInfo() {
	fNonJavaResources = null;
}
/**
 * Compute the non-java resources of this package fragment.
 *
 * <p>Package fragments which are folders recognize files based on the
 * type of the fragment
 * <p>Package fragments which are in a jar only recognize .class files (
 * @see JarPackageFragment).
 */
private Object[] computeNonJavaResources(IResource resource) {
	Object[] nonJavaResources = new IResource[5];
	int nonJavaResourcesCounter = 0;
	try{
		IResource[] members = ((IContainer) resource).members();
		for (int i = 0, max = members.length; i < max; i++) {
			IResource child = members[i];
			if (child.getType() != IResource.FOLDER) {
				String extension = child.getProjectRelativePath().getFileExtension();
				if (!"java".equalsIgnoreCase(extension) && !"class".equalsIgnoreCase(extension)) { //$NON-NLS-1$ //$NON-NLS-2$
					if (nonJavaResources.length == nonJavaResourcesCounter) {
						// resize
						System.arraycopy(
							nonJavaResources,
							0,
							(nonJavaResources = new IResource[nonJavaResourcesCounter * 2]),
							0,
							nonJavaResourcesCounter);
					}
					nonJavaResources[nonJavaResourcesCounter++] = child;
				}
			}
		}
		if (nonJavaResourcesCounter == 0) {
			nonJavaResources = NO_NON_JAVA_RESOURCES;
		} else {
			if (nonJavaResources.length != nonJavaResourcesCounter) {
				System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter]), 0, nonJavaResourcesCounter);
			}
		}	
	} catch(CoreException e) {
		nonJavaResources = NO_NON_JAVA_RESOURCES;
		nonJavaResourcesCounter = 0;
	}
	return nonJavaResources;
}
/**
 */
boolean containsJavaResources() {
	return fChildren.length != 0;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
Object[] getNonJavaResources(IResource underlyingResource) {
	Object[] nonJavaResources = fNonJavaResources;
	if (nonJavaResources == null) {
		nonJavaResources = computeNonJavaResources(underlyingResource);
		fNonJavaResources = nonJavaResources;
	}
	return nonJavaResources;
}
/**
 * Set the fNonJavaResources to res value
 */
synchronized void setNonJavaResources(Object[] resources) {
	fNonJavaResources = resources;
}
}
