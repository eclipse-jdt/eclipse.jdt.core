package org.eclipse.jdt.internal.codeassist.complete;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionNodeFound extends RuntimeException {
	public AstNode astNode;
	public Binding qualifiedBinding;
	public Scope scope;
	public CompletionNodeFound() {
		this(null, null, null); // we found a problem in the completion node
	}

	public CompletionNodeFound(
		AstNode astNode,
		Binding qualifiedBinding,
		Scope scope) {
		this.astNode = astNode;
		this.qualifiedBinding = qualifiedBinding;
		this.scope = scope;
	}

	public CompletionNodeFound(AstNode astNode, Scope scope) {
		this(astNode, null, scope);
	}

}
