/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.JavaModelException;

/**
 * Handle representing a binary type that is parameterized.
 * The uniqueKey contains the genericTypeSignature of the parameterized type.
 */
public class ParameterizedBinaryType extends BinaryType {
	
	private String uniqueKey;
	
	/*
	 * See class comments.
	 */
	public ParameterizedBinaryType(JavaElement parent, String name, String uniqueKey) {
		super(parent, name);
		this.uniqueKey = uniqueKey;
	}

	public String getFullyQualifiedParameterizedName() throws JavaModelException {
		return getFullyQualifiedParameterizedName(getFullyQualifiedName(), this.uniqueKey);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.BinaryType#getKey()
	 */
	public String getKey() {
		return this.uniqueKey;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.BinaryType#isParameterized()
	 */
	public boolean isParameterized() {
		return true;
	}
	
	/**
	 * @private Debugging purposes
	 */
	protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
		super.toStringInfo(tab, buffer, info);
		buffer.append(" key="); //$NON-NLS-1$
		buffer.append(this.uniqueKey);
	}
}
