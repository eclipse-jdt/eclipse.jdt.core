package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;

/**
 * The SingleTypeRequestor is an IJavaElementRequestor that 
 * only accepts one result element and then cancels.
 */
/* package */
class SingleTypeRequestor implements IJavaElementRequestor {
	/**
	 * The single accepted element
	 */
	protected IType fElement = null;
	/**
	 * @see IJavaElementRequestor
	 */
	public void acceptField(IField field) {
	}

	/**
	 * @see IJavaElementRequestor
	 */
	public void acceptInitializer(IInitializer initializer) {
	}

	/**
	 * @see IJavaElementRequestor
	 */
	public void acceptMemberType(IType type) {
		fElement = type;
	}

	/**
	 * @see IJavaElementRequestor
	 */
	public void acceptMethod(IMethod method) {
	}

	/**
	 * @see IJavaElementRequestor
	 */
	public void acceptPackageFragment(IPackageFragment packageFragment) {
	}

	/**
	 * @see IJavaElementRequestor
	 */
	public void acceptType(IType type) {
		fElement = type;
	}

	/**
	 * Returns the type accepted by this requestor, or <code>null</code>
	 * if no type has been accepted.
	 */
	public IType getType() {
		return fElement;
	}

	/**
	 * @see IJavaElementRequestor
	 */
	public boolean isCanceled() {
		return fElement != null;
	}

	/**
	 * Reset the state of this requestor
	 */
	public void reset() {
		fElement = null;
	}

}
