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

import org.eclipse.jdt.internal.compiler.env.AccessRestriction;

/**
 * A <code>IRestrictedAccessTypeRequestor</code> collects search results from a <code>searchAllTypeNames</code>
 * query to a <code>SearchBasicEngine</code> providing restricted access information when a class or an interface is accepted.
 * @see org.eclipse.jdt.core.search.ITypeNameRequestor
 */
public interface IRestrictedAccessTypeRequestor {

	public void acceptClass(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path, AccessRestriction access);

	public void acceptInterface(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path, AccessRestriction access);

}
