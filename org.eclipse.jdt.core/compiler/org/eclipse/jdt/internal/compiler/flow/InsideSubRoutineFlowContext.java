package org.eclipse.jdt.internal.compiler.flow;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class InsideSubRoutineFlowContext extends FlowContext {
	public InsideSubRoutineFlowContext(
		FlowContext parent,
		AstNode associatedNode) {
		super(parent, associatedNode);
	}

	public boolean isNonReturningContext() {
		return associatedNode.cannotReturn();
	}

	public AstNode subRoutine() {
		return associatedNode;
	}

}
