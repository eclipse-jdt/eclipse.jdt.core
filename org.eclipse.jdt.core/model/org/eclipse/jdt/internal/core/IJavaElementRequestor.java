package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;

/**
 * This interface is used by IRequestorNameLookup. As results
 * are found by IRequestorNameLookup, they are reported to this
 * interface. An IJavaElementRequestor is able to cancel
 * at any time (i.e. stop receiving results), by responding
 * <code>true</code> to <code>#isCancelled</code>.
 */
public interface IJavaElementRequestor {
	public void acceptField(IField field);
	public void acceptInitializer(IInitializer initializer);
	public void acceptMemberType(IType type);
	public void acceptMethod(IMethod method);
	public void acceptPackageFragment(IPackageFragment packageFragment);
	public void acceptType(IType type);
	/**
	 * Returns <code>true</code> if this IJavaElementRequestor does
	 * not want to receive any more results.
	 */
	boolean isCanceled();
}
