package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.hierarchy.*;

/**
 * This operation creates an <code>ITypeHierarchy</code> for a specific type within
 * a specified region, or for all types within a region. The specified
 * region limits the number of resolved subtypes (i.e. to the subset of
 * types in the specified region). The resolved supertypes may go outside
 * of the specified region in order to reach the root(s) of the type
 * hierarchy. A Java Project is required to provide a context (classpath)
 * to use while resolving supertypes and subtypes.
 *
 * @see ITypeHierarchy
 */

public class CreateTypeHierarchyOperation extends JavaModelOperation {
	/**
	 * The generated type hierarchy
	 */
	protected TypeHierarchy fTypeHierarchy;
	/**
	 * Constructs an operation to create a type hierarchy for the
	 * given type within the specified region, in the context of
	 * the given project.
	 */
	public CreateTypeHierarchyOperation(
		IType element,
		IRegion region,
		IJavaProject project,
		boolean computeSubtypes)
		throws JavaModelException {
		super(element);
		fTypeHierarchy =
			new RegionBasedTypeHierarchy(region, project, element, computeSubtypes);
	}

	/**
	 * Constructs an operation to create a type hierarchy for the
	 * given type.
	 */
	public CreateTypeHierarchyOperation(
		IType element,
		IJavaSearchScope scope,
		boolean computeSubtypes)
		throws JavaModelException {
		super(element);
		fTypeHierarchy = new TypeHierarchy(element, scope, computeSubtypes);
	}

	/**
	 * Performs the operation - creates the type hierarchy
	 * @exception JavaModelException The operation has failed.
	 */
	protected void executeOperation() throws JavaModelException {
		fTypeHierarchy.refresh(this);
	}

	/**
	 * Returns the generated type hierarchy.
	 */
	public ITypeHierarchy getResult() {
		return fTypeHierarchy;
	}

	/**
	 * @see JavaModelOperation
	 */
	public boolean isReadOnly() {
		return true;
	}

	/**
	 * Possible failures: <ul>
	 *	<li>NO_ELEMENTS_TO_PROCESS - at least one of a type or region must
	 *			be provided to generate a type hierarchy.
	 *	<li>ELEMENT_NOT_PRESENT - the provided type or type's project does not exist
	 * </ul>
	 */
	public IJavaModelStatus verify() {
		IJavaElement elementToProcess = getElementToProcess();
		if (elementToProcess == null
			&& !(fTypeHierarchy instanceof RegionBasedTypeHierarchy)) {
			return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
		if (elementToProcess != null && !elementToProcess.exists()) {
			return new JavaModelStatus(
				IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST,
				elementToProcess);
		}
		IJavaProject project = fTypeHierarchy.javaProject();
		if (project != null && !project.exists()) {
			return new JavaModelStatus(
				IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST,
				project);
		}
		return JavaModelStatus.VERIFIED_OK;
	}

}
