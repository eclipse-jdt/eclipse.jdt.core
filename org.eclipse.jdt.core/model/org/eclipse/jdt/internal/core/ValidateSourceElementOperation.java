package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;

/**
 * This operation is used to find warnings and/or errors in a
 * source element - i.e. an element that implements <code>ISourceReference</code>.
 * The result is a collection of <code>IMarkers</code> representing warnings
 * and errors, or <code>null</code> if no warnings or errors are found.
 * Errors/warnings are determined in the context of the current built state
 * of the Java Model. The resulting <code>IMarkers</code> are not attached
 * to an <code>IResource</code>.
 */
public class ValidateSourceElementOperation extends JavaModelOperation {
	/**
	 * Validate the given Java Model element.
	 */
	public ValidateSourceElementOperation(IJavaElement element) {
		super(element);
	}

	/**
	 * Validate the element - TBD.
	 */
	protected void executeOperation() throws JavaModelException {
	}

	/**
	 * Returns the errors and warnings as a set of <code>IMarkers</code>,
	 * or <code>null</code> if no errors or warnings were found.
	 */
	public IMarker[] getResult() {
		return null;
	}

	/**
	 * Possible failures: <ul>
	 *	<li>ELEMENTS_TO_PROCESS - an element was not provided
	 *	<li>INVALID_ELEMENT_TYPES - the element provided is not a source element
	 *	<li>ELEMENT_NOT_PRESENT - the element does not exist
	 * </ul>
	 */
	public IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		if (!(getElementToProcess() instanceof ISourceReference)) {
			return new JavaModelStatus(
				IJavaModelStatusConstants.INVALID_ELEMENT_TYPES,
				getElementToProcess());
		}
		if (!getElementToProcess().exists()) {
			return new JavaModelStatus(
				IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST,
				getElementToProcess());
		}
		return JavaModelStatus.VERIFIED_OK;
	}

}
