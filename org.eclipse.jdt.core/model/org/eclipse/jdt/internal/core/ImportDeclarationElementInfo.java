/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.internal.compiler.env.ISourceImport;

/**
 * Element info for IImportDeclaration elements.
 * @see IImportDeclaration
 */
public class ImportDeclarationElementInfo extends MemberElementInfo implements ISourceImport{
	
	// record if import is on demand, the import name doesn't have trailing start
	boolean onDemand;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.env.ISourceImport#onDemand()
	 */
	public boolean onDemand() {
		return this.onDemand;
	}

	public void setOnDemand(boolean onDemand) {
		this.onDemand = onDemand;
	}
}
