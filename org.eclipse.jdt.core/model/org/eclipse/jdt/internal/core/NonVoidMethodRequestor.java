package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 *	This class modifies the <code>SearchableEnvironmentRequestor</code>'s 
 *	functionality by only accepting methods with return types that are not void.
 */
public class NonVoidMethodRequestor extends SearchableEnvironmentRequestor {
/**
 * NonVoidMethodRequestor constructor comment.
 * @param requestor org.eclipse.jdt.internal.codeassist.ISearchRequestor
 */
public NonVoidMethodRequestor(ISearchRequestor requestor) {
	super(requestor);
}
public void acceptMethod(IMethod method) {
	try {
		if (!Signature.getReturnType(method.getSignature()).equals("V")) { //$NON-NLS-1$
			super.acceptMethod(method);
		}
	} catch (JavaModelException npe) {
	}
}
}
