/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;

abstract public class CopyMoveTests extends ModifyingResourceTests {
public CopyMoveTests(String name) {
	super(name);
}
/**
 * Attempts to copy the element with optional
 * forcing. The operation should fail with the failure code.
 */
public void copyNegative(IJavaElement element, IJavaElement destination, IJavaElement sibling, String rename, boolean force, int failureCode) {
	try {
		((ISourceManipulation)element).copy(destination, sibling, rename, force, null);
	} catch (JavaModelException jme) {
		assertTrue("Code not correct for JavaModelException: " + jme, jme.getStatus().getCode() == failureCode);
		return;
	}
	assertTrue("The copy should have failed for: " + element, false);
	return;
}
/**
 * Attempts to copy the elements with optional
 * forcing. The operation should fail with the failure code.
 */
public void copyNegative(IJavaElement[] elements, IJavaElement[] destinations, IJavaElement[] siblings, String[] renames, boolean force, int failureCode) {
	try {
		getJavaModel().copy(elements, destinations, siblings, renames, force, null);
	} catch (JavaModelException jme) {
		assertTrue("Code not correct for JavaModelException: " + jme, jme.getStatus().getCode() == failureCode);
		return;
	}
	assertTrue("The move should have failed for for multiple elements: ", false);
	return;
}
/**
 * Copies the element to the container with optional rename
 * and forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public IJavaElement copyPositive(IJavaElement element, IJavaElement container, IJavaElement sibling, String rename, boolean force) throws JavaModelException {
	// if forcing, ensure that a name collision exists
	if (force) {
		IJavaElement collision = generateHandle(element, rename, container);
		assertTrue("Collision does not exist", collision.exists());
	}

	IJavaElement copy;
	try {
		startDeltas();

		// copy
	 	((ISourceManipulation) element).copy(container, sibling, rename, force, null);

		// ensure the original element still exists
		assertTrue("The original element must still exist", element.exists());

		// generate the new element	handle
		copy = generateHandle(element, rename, container);
		assertTrue("Copy should exist", copy.exists());

		//ensure correct position
		if (element.getElementType() > IJavaElement.COMPILATION_UNIT) {
			ensureCorrectPositioning((IParent) container, sibling, copy);
		} else {
			if (container.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
			} else {
				// ensure package name is correct
				if (container.getElementName().equals("")) {
					// default package - should be no package decl
					IJavaElement[] children = ((ICompilationUnit) copy).getChildren();
					boolean found = false;
					for (int i = 0; i < children.length; i++) {
						if (children[i] instanceof IPackageDeclaration) {
							found = true;
						}
					}
					assertTrue("Should not find package decl", !found);
				} else {
					IJavaElement[] children = ((ICompilationUnit) copy).getChildren();
					boolean found = false;
					for (int i = 0; i < children.length; i++) {
						if (children[i] instanceof IPackageDeclaration) {
							assertTrue("package declaration incorrect", ((IPackageDeclaration) children[i]).getElementName().equals(container.getElementName()));
							found = true;
						}
					}
					assertTrue("Did not find package decl", found);
				}
			}
		}
		if (copy.getElementType() == IJavaElement.IMPORT_DECLARATION)
			container = ((ICompilationUnit) container).getImportContainer();
		IJavaElementDelta destDelta = this.deltaListener.getDeltaFor(container, true);
		assertTrue("Destination container not changed", destDelta != null && destDelta.getKind() == IJavaElementDelta.CHANGED);
		IJavaElementDelta[] deltas = destDelta.getAddedChildren();
		assertTrue("Added children not correct for element copy", deltas[0].getElement().equals(copy));
	} finally {
		stopDeltas();
	}
	return copy;
}
/**
 * Generates a new handle to the original element in
 * its new container.
 */
public IJavaElement generateHandle(IJavaElement original, String rename, IJavaElement container) {
	String name = original.getElementName();
	if (rename != null) {
		name = rename;
	}
	switch (container.getElementType()) {
		case IJavaElement.PACKAGE_FRAGMENT_ROOT :
			switch (original.getElementType()) {
				case IJavaElement.PACKAGE_FRAGMENT :
					return ((IPackageFragmentRoot) container).getPackageFragment(name);
				default :
					assertTrue("illegal child type", false);
					break;
			}
			break;
		case IJavaElement.PACKAGE_FRAGMENT :
			switch (original.getElementType()) {
				case IJavaElement.COMPILATION_UNIT :
					return ((IPackageFragment) container).getCompilationUnit(name);
				default :
					assertTrue("illegal child type", false);
					break;
			}
			break;
		case IJavaElement.COMPILATION_UNIT :
			switch (original.getElementType()) {
				case IJavaElement.IMPORT_DECLARATION :
					return ((ICompilationUnit) container).getImport(name);
				case IJavaElement.PACKAGE_DECLARATION :
					return ((ICompilationUnit) container).getPackageDeclaration(name);
				case IJavaElement.TYPE :
					if (isMainType(original, container)) {
						//the cu has been renamed as well
						container = ((IPackageFragment) container.getParent()).getCompilationUnit(name + ".java");
					}
					return ((ICompilationUnit) container).getType(name);
				default :
					assertTrue("illegal child type", false);
					break;
			}
			break;
		case IJavaElement.TYPE :
			switch (original.getElementType()) {
				case IJavaElement.METHOD :
					if (name.equals(original.getParent().getElementName())) {
						//method is a constructor
						return ((IType) container).getMethod(container.getElementName(), ((IMethod) original).getParameterTypes());
					}
					return ((IType) container).getMethod(name, ((IMethod) original).getParameterTypes());
				case IJavaElement.FIELD :
					return ((IType) container).getField(name);
				case IJavaElement.TYPE :
					return ((IType) container).getType(name);
				case IJavaElement.INITIALIZER :
					//hack to return the first initializer
					return ((IType) container).getInitializer(1);
				default :
					assertTrue("illegal child type", false);
					break;
			}
			break;
		default :
			assertTrue("unsupported container type", false);
			break;
	}
	assertTrue("should not get here", false);
	return null;
}
/**
 * Returns true if this element is the main type of its compilation unit.
 */
protected boolean isMainType(IJavaElement element, IJavaElement parent) {
	if (parent instanceof ICompilationUnit) {
		if (element instanceof IType) {
			ICompilationUnit cu= (ICompilationUnit)parent;
			String typeName = cu.getElementName();
			typeName = typeName.substring(0, typeName.length() - 5);
			return element.getElementName().equals(typeName) && element.getParent().equals(cu);
		}
	}
	return false;
}
/**
 * Attempts to move the element with optional
 * forcing. The operation should fail with the failure code.
 */
public void moveNegative(IJavaElement element, IJavaElement destination, IJavaElement sibling, String rename, boolean force, int failureCode) {
	try {
		((ISourceManipulation)element).move(destination, sibling, rename, force, null);
	} catch (JavaModelException jme) {
		assertTrue("Code not correct for JavaModelException: " + jme, jme.getStatus().getCode() == failureCode);
		return;
	}
	assertTrue("The move should have failed for: " + element, false);
	return;
}
/**
 * Attempts to move the element with optional
 * forcing. The operation should fail with the failure code.
 */
public void moveNegative(IJavaElement[] elements, IJavaElement[] destinations, IJavaElement[] siblings, String[] renames, boolean force, int failureCode) {
	try {
		getJavaModel().move(elements, destinations, siblings, renames, force, null);
	} catch (JavaModelException jme) {
		assertTrue("Code not correct for JavaModelException: " + jme, jme.getStatus().getCode() == failureCode);
		return;
	}
	assertTrue("The move should have failed for for multiple elements: ", false);
	return;
}
/**
 * Moves the element to the container with optional rename
 * and forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public void movePositive(IJavaElement element, IJavaElement container, IJavaElement sibling, String rename, boolean force) throws JavaModelException {
	IJavaElement[] siblings = new IJavaElement[] {sibling};
	String[] renamings = new String[] {rename};
	if (sibling == null) {
		siblings = null;
	}
	if (rename == null) {
		renamings = null;
	}
	movePositive(new IJavaElement[] {element}, new IJavaElement[] {container}, siblings, renamings, force);
}
/**
 * Moves the elements to the containers with optional renaming
 * and forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public void movePositive(IJavaElement[] elements, IJavaElement[] destinations, IJavaElement[] siblings, String[] names, boolean force) throws JavaModelException {
	movePositive(elements, destinations, siblings, names, force, null);
}
/**
 * Moves the elements to the containers with optional renaming
 * and forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public void movePositive(IJavaElement[] elements, IJavaElement[] destinations, IJavaElement[] siblings, String[] names, boolean force, IProgressMonitor monitor) throws JavaModelException {
	// if forcing, ensure that a name collision exists
	int i;
	if (force) {
		for (i = 0; i < elements.length; i++) {
			IJavaElement e = elements[i];
			IJavaElement collision = null;
			if (names == null) {
				collision = generateHandle(e, null, destinations[i]);
			} else {
				collision = generateHandle(e, names[i], destinations[i]);
			}
			assertTrue("Collision does not exist", collision.exists());
		}
	}

	try {
		startDeltas();

		// move
		getJavaModel().move(elements, destinations, siblings, names, force, monitor);
		for (i = 0; i < elements.length; i++) {
			IJavaElement element = elements[i];
			IJavaElement moved = null;
			if (names == null) {
				moved = generateHandle(element, null, destinations[i]);
			} else {
				moved = generateHandle(element, names[i], destinations[i]);
			}
			// ensure the original element no longer exists, unless moving within the same container
			if (!destinations[i].equals(element.getParent())) {
				if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
					//moving a package fragment does not necessary mean that it no longer exists
					//it could have members that are not part of the Java Model
					try {
						IResource[] members = ((IContainer) element.getUnderlyingResource()).members();
						if (members.length == 0) {
							assertTrue("The original element must not exist", !element.exists());
						}
					} catch (CoreException ce) {
						throw new JavaModelException(ce);
					}
				} else {
					assertTrue("The original element must not exist", !element.exists());
				}
			}
			assertTrue("Moved element should exist", moved.exists());

			//ensure correct position
			if (element.getElementType() > IJavaElement.COMPILATION_UNIT) {
				if (siblings != null && siblings.length > 0) {
					ensureCorrectPositioning((IParent) moved.getParent(), siblings[i], moved);
				}
			} else {
				IJavaElement container = destinations[i];
				if (container.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
				} else { // ensure package name is correct

					if (container.getElementName().equals("")) {
						// default package - should be no package decl
						IJavaElement[] children = ((ICompilationUnit) moved).getChildren();
						boolean found = false;
						for (int j = 0; j < children.length; j++) {
							if (children[j] instanceof IPackageDeclaration) {
								found = true;
								break;
							}
						}
						assertTrue("Should not find package decl", !found);
					} else {
						IJavaElement[] children = ((ICompilationUnit) moved).getChildren();
						boolean found = false;
						for (int j = 0; j < children.length; j++) {
							if (children[j] instanceof IPackageDeclaration) {
								assertTrue("package declaration incorrect", ((IPackageDeclaration) children[j]).getElementName().equals(container.getElementName()));
								found = true;
								break;
							}
						}
						assertTrue("Did not find package decl", found);
					}
				}
			}
			IJavaElementDelta destDelta = null;
			if (isMainType(element, destinations[i]) && names != null && names[i] != null) { //moved/renamed main type to same cu
				destDelta = this.deltaListener.getDeltaFor(moved.getParent());
				assertTrue("Renamed compilation unit as result of main type not added", destDelta != null && destDelta.getKind() == IJavaElementDelta.ADDED);
				assertTrue("flag should be F_MOVED_FROM", (destDelta.getFlags() & IJavaElementDelta.F_MOVED_FROM) > 0);
				assertTrue("moved from handle should be original", destDelta.getMovedFromElement().equals(element.getParent()));
			} else {
				destDelta = this.deltaListener.getDeltaFor(destinations[i], true);
				assertTrue("Destination container not changed", destDelta != null && destDelta.getKind() == IJavaElementDelta.CHANGED);
				IJavaElementDelta[] deltas = destDelta.getAddedChildren();
				assertTrue("Added children not correct for element copy", deltas[i].getElement().equals(moved));
				assertTrue("should be K_ADDED", deltas[i].getKind() == IJavaElementDelta.ADDED);
				IJavaElementDelta sourceDelta= this.deltaListener.getDeltaFor(element, false);
				assertTrue("should be K_REMOVED", sourceDelta.getKind() == IJavaElementDelta.REMOVED);
			}
		}
	} finally {
		stopDeltas();
	}
}
}
