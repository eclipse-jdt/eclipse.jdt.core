/*******************************************************************************
 * Copyright (c) 2000, 2004 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.TypeParameter;

/**
 * Binding for a type parameter.
 */
public class TypeVariableBinding extends ReferenceBinding {
	
	public Scope declaringScope; // back-pointer to its declaring scope
	public TypeParameter declaration; // for source-positions TODO needed ?
	
	public TypeVariableBinding(char[] sourceName, Scope declaringScope){

		this.sourceName = sourceName;
		this.declaringScope = declaringScope;
	}

	/**
	 * Returns true if the type was declared as a type variable
	 */
	public boolean isTypeVariable() {
	    return true;
	}
}