package org.eclipse.jdt.internal.core.hierarchy;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.Region;
import org.eclipse.jdt.internal.core.CompilationUnit;

public class RegionBasedTypeHierarchy extends TypeHierarchy {
	/**
	 * The region of types for which to build the hierarchy
	 */
	protected IRegion fRegion;

	/**
	 * The Java Project in which the hierarchy is being built - this
	 * provides the context (i.e. classpath and namelookup rules)
	 */
	protected IJavaProject fProject;
	/**
	 * Creates a TypeHierarchy on the types in the specified region,
	 * using the given project for a name lookup contenxt. If a specific
	 * type is also specified, the type hierarchy is pruned to only
	 * contain the branch including the specified type.
	 */
	public RegionBasedTypeHierarchy(
		IRegion region,
		IJavaProject project,
		IType type,
		boolean computeSubtypes)
		throws JavaModelException {
		super(type, null, computeSubtypes);
		fRegion = region;
		fProject = project;
	}

	/**
	 * Activates this hierarchy for change listeners
	 */
	protected void activate() {
		super.activate();
		IJavaElement[] roots = fRegion.getElements();
		for (int i = 0; i < roots.length; i++) {
			IJavaElement root = roots[i];
			if (root instanceof IOpenable) {
				this.files.put(root, root);
			} else {
				Openable o = (Openable) ((JavaElement) root).getOpenableParent();
				if (o != null) {
					this.files.put(o, o);
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

	protected void destroy() {
		fRegion = new Region();
		super.destroy();
	}

	protected boolean isAffectedByType(
		IJavaElementDelta delta,
		IJavaElement element) {
		// ignore changes to working copies
		if (element instanceof CompilationUnit
			&& ((CompilationUnit) element).isWorkingCopy()) {
			return false;
		}

		// if no focus, hierarchy is affected if the element is part of the region
		if (fType == null) {
			return fRegion.contains(element);
		} else {
			return super.isAffectedByType(delta, element);
		}
	}

	/**
	 * Returns the java project this hierarchy was created in.
	 */
	public IJavaProject javaProject() {
		return fProject;
	}

	protected void pruneTypeHierarchy(IType type, IProgressMonitor monitor)
		throws JavaModelException {
		// there is no pruning to do if the hierarchy was created for the single type
		IJavaElement[] roots = fRegion.getElements();
		if (roots.length == 1 && roots[0].equals(type)) {
			return;
		}

		super.pruneTypeHierarchy(type, monitor);
	}

}
