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
package org.eclipse.jdt.internal.core.hierarchy;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.TypeVector;

public class RegionBasedTypeHierarchy extends TypeHierarchy {
	/**
	 * The region of types for which to build the hierarchy
	 */
	protected IRegion region;

/**
 * Creates a TypeHierarchy on the types in the specified region,
 * considering first the given working copies,
 * using the given project for a name lookup contenxt. If a specific
 * type is also specified, the type hierarchy is pruned to only
 * contain the branch including the specified type.
 */
public RegionBasedTypeHierarchy(IRegion region, IJavaProject project, ICompilationUnit[] workingCopies, IType type, boolean computeSubtypes) {
	super(type, workingCopies, (IJavaSearchScope)null, computeSubtypes);
	this.region = region;
	this.project = project;
}
/*
 * @see TypeHierarchy#initializeRegions
 */
protected void initializeRegions() {
	super.initializeRegions();
	IJavaElement[] roots = this.region.getElements();
	for (int i = 0; i < roots.length; i++) {
		IJavaElement root = roots[i];
		if (root instanceof IOpenable) {
			this.files.put(root, new ArrayList());
		} else {
			Openable o = (Openable) ((JavaElement) root).getOpenableParent();
			if (o != null) {
				this.files.put(o, new ArrayList());
			}
		}
		checkCanceled();
	}
}
/**
 * Compute this type hierarchy.
 */
protected void compute() throws JavaModelException, CoreException {
	HierarchyBuilder builder = new RegionBasedHierarchyBuilder(this);
	builder.build(this.computeSubtypes);
}
protected boolean isAffectedByOpenable(IJavaElementDelta delta, IJavaElement element) {
	// change to working copy
	if (element instanceof CompilationUnit && ((CompilationUnit)element).isWorkingCopy()) {
		return super.isAffectedByOpenable(delta, element);
	}

	// if no focus, hierarchy is affected if the element is part of the region
	if (this.focusType == null) {
		return this.region.contains(element);
	} else {
		return super.isAffectedByOpenable(delta, element);
	}
}
/**
 * Returns the java project this hierarchy was created in.
 */
public IJavaProject javaProject() {
	return this.project;
}
public void pruneDeadBranches() {
	pruneDeadBranches(getRootClasses());
	pruneDeadBranches(getRootInterfaces());
}
/*
 * Returns whether all subtypes of the given type have been pruned.
 */
private boolean pruneDeadBranches(IType type) {
	TypeVector subtypes = (TypeVector)this.typeToSubtypes.get(type);
	if (subtypes == null) return true;
	pruneDeadBranches(subtypes.copy().elements());
	subtypes = (TypeVector)this.typeToSubtypes.get(type);
	return (subtypes == null || subtypes.size == 0);
}
private void pruneDeadBranches(IType[] types) {
	for (int i = 0, length = types.length; i < length; i++) {
		IType type = types[i];
		if (pruneDeadBranches(type) && !this.region.contains(type)) {
			removeType(type);
		}
	}
}
/**
 * Removes all the subtypes of the given type from the type hierarchy,
 * removes its superclass entry and removes the references from its super types.
 */
protected void removeType(IType type) {
	IType[] subtypes = this.getSubtypes(type);
	this.typeToSubtypes.remove(type);
	if (subtypes != null) {
		for (int i= 0; i < subtypes.length; i++) {
			this.removeType(subtypes[i]);
		}
	}
	IType superclass = (IType)this.classToSuperclass.remove(type);
	if (superclass != null) {
		TypeVector types = (TypeVector)this.typeToSubtypes.get(superclass);
		if (types != null) types.remove(type);
	}
	IType[] superinterfaces = (IType[])this.typeToSuperInterfaces.remove(type);
	if (superinterfaces != null) {
		for (int i = 0, length = superinterfaces.length; i < length; i++) {
			IType superinterface = superinterfaces[i];
			TypeVector types = (TypeVector)this.typeToSubtypes.get(superinterface);
			if (types != null) types.remove(type);
		}
	}
	this.interfaces.remove(type);
}

}
