/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

public class RecoveredTypeReference extends RecoveredElement {
	public TypeReference typeReference;

	public RecoveredTypeReference(TypeReference typeReference, RecoveredElement parent, int bracketBalance) {
		super(parent, bracketBalance);
		this.typeReference = typeReference;
	}

	/*
	 * Answer the associated parsed structure
	 */
	public ASTNode parseTree(){
		return this.typeReference;
	}
	public TypeReference updateTypeReference() {
		return this.typeReference;
	}
	/*
	 * Answer the very source end of the corresponding parse node
	 */
	public String toString(int tab) {
		return tabString(tab) + "Recovered typereference: " + this.typeReference.toString(); //$NON-NLS-1$
	}
	public TypeReference updatedImportReference(){
		return this.typeReference;
	}
	public void updateParseTree(){
		updatedImportReference();
	}
}