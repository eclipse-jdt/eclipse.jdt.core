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
package org.eclipse.jdt.internal.core.search;

import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;

/**
 * Wrapper to link previous ITypeNameRequestor and IRestrictedAccessTypeRequestor interfaces.
 * This wrapper is used by {@link org.eclipse.jdt.core.search.SearchEngine#searchAllTypeNames(char[],char[],int,int,org.eclipse.jdt.core.search.IJavaSearchScope,ITypeNameRequestor,int,org.eclipse.core.runtime.IProgressMonitor)}
 * to call {@link SearchBasicEngine#searchAllTypeNames(char[],char[],int,int,org.eclipse.jdt.core.search.IJavaSearchScope,IRestrictedAccessTypeRequestor,int,org.eclipse.core.runtime.IProgressMonitor)}
 * corresponding method.
 */
public class TypeNameRequestorWrapper implements IRestrictedAccessTypeRequestor {
	ITypeNameRequestor requestor;
	public TypeNameRequestorWrapper(ITypeNameRequestor requestor) {
		this.requestor = requestor;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.search.IAccessedTypeNameRequestor#acceptClass(char[], char[], char[][], java.lang.String, org.eclipse.jdt.internal.compiler.env.AccessRestriction)
	 */
	public void acceptClass (	char[] packageName,
								char[] simpleTypeName,
								char[][] enclosingTypeNames,
								String path,
								AccessRestriction access) {
		this.requestor.acceptClass(packageName, simpleTypeName, enclosingTypeNames, path);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.search.IAccessedTypeNameRequestor#acceptInterface(char[], char[], char[][], java.lang.String, org.eclipse.jdt.internal.compiler.env.AccessRestriction)
	 */
	public void acceptInterface (	char[] packageName,
									char[] simpleTypeName,
									char[][] enclosingTypeNames,
									String path,
									AccessRestriction access) {
		this.requestor.acceptInterface(packageName, simpleTypeName, enclosingTypeNames, path);
	}
}
