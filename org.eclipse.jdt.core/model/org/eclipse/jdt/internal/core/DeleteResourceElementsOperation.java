package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import java.util.Enumeration;

/**
 * This operation deletes a collection of resources and all of their children.
 * It does not delete resources which do not belong to the Java Model
 * (eg GIF files).
 */
public class DeleteResourceElementsOperation extends MultiOperation {
	/**
	 * When executed, this operation will delete the given elements. The elements
	 * to delete cannot be <code>null</code> or empty, and must have a corresponding
	 * resource.
	 */
	protected DeleteResourceElementsOperation(
		IJavaElement[] elementsToProcess,
		boolean force) {
		super(elementsToProcess, force);
	}

	/**
	 * Deletes the direct children of <code>frag</code> corresponding to its kind
	 * (K_SOURCE or K_BINARY), and deletes the corresponding folder if it is then
	 * empty.
	 */
	private void deletePackageFragment(IPackageFragment frag)
		throws JavaModelException {
		IResource res = frag.getCorrespondingResource();
		if (res != null && res.getType() == IResource.FOLDER) {
			// collect the children to remove
			IJavaElement[] childrenOfInterest;
			if (frag.getKind() == IPackageFragmentRoot.K_SOURCE) {
				childrenOfInterest = frag.getCompilationUnits();
			} else { // K_BINARY
				childrenOfInterest = frag.getClassFiles();
			}
			if (childrenOfInterest.length > 0) {
				IResource[] resources = new IResource[childrenOfInterest.length];
				// remove the children
				for (int i = 0; i < childrenOfInterest.length; i++) {
					resources[i] = childrenOfInterest[i].getCorrespondingResource();
				}
				deleteResources(resources, fForce);
			}

			// Discard non-java resources
			Object[] nonJavaResources = frag.getNonJavaResources();
			int actualResourceCount = 0;
			for (int i = 0, max = nonJavaResources.length; i < max; i++) {
				if (nonJavaResources[i] instanceof IResource)
					actualResourceCount++;
			}
			IResource[] actualNonJavaResources = new IResource[actualResourceCount];
			for (int i = 0, max = nonJavaResources.length, index = 0; i < max; i++) {
				if (nonJavaResources[i] instanceof IResource)
					actualNonJavaResources[index++] = (IResource) nonJavaResources[i];
			}
			deleteResources(actualNonJavaResources, fForce);

			// remove the folder if it is empty
			IResource[] members;
			try {
				members = ((IFolder) res).members();
			} catch (CoreException ce) {
				throw new JavaModelException(ce);
			}
			if (members.length == 0) {
				deleteEmptyPackageFragment(frag, fForce);
			}
		}
	}

	/**
	 * @see MultiOperation
	 */
	protected String getMainTaskName() {
		return "Deleting resources...";
	}

	/**
	 * @see MultiOperation. This method delegate to <code>deleteResource</code> or
	 * <code>deletePackageFragment</code> depending on the type of <code>element</code>.
	 */
	protected void processElement(IJavaElement element) throws JavaModelException {
		switch (element.getElementType()) {
			case IJavaElement.CLASS_FILE :
			case IJavaElement.COMPILATION_UNIT :
				deleteResource(element.getCorrespondingResource(), fForce);
				break;
			case IJavaElement.PACKAGE_FRAGMENT :
				deletePackageFragment((IPackageFragment) element);
				break;
			default :
				throw new JavaModelException(
					new JavaModelStatus(JavaModelStatus.INVALID_ELEMENT_TYPES, element));
		}
	}

	/**
	 * @see MultiOperation
	 */
	protected void verify(IJavaElement element) throws JavaModelException {
		if (element == null || !element.exists())
			error(JavaModelStatus.ELEMENT_DOES_NOT_EXIST, element);

		int type = element.getElementType();
		if (type <= IJavaElement.PACKAGE_FRAGMENT_ROOT
			|| type > IJavaElement.COMPILATION_UNIT)
			error(JavaModelStatus.INVALID_ELEMENT_TYPES, element);
		else
			if (type == IJavaElement.PACKAGE_FRAGMENT
				&& element instanceof JarPackageFragment)
				error(JavaModelStatus.INVALID_ELEMENT_TYPES, element);
	}

}
