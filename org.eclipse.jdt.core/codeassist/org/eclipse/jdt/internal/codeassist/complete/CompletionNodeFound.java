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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

public class CompletionNodeFound extends RuntimeException {
	
	public ASTNode astNode;
	public Binding qualifiedBinding;
	public Scope scope;

	private static final long serialVersionUID = 6981437684184091462L; // backward compatible
	
public CompletionNodeFound() {
	this(null, null, null); // we found a problem in the completion node
}
public CompletionNodeFound(ASTNode astNode, Binding qualifiedBinding, Scope scope) {
	this.astNode = astNode;
	this.qualifiedBinding = qualifiedBinding;
	this.scope = scope;
}
public CompletionNodeFound(ASTNode astNode, Scope scope) {
	this(astNode, null, scope);
}
}
