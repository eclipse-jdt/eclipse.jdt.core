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
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.SubRoutineStatement;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class InsideSubRoutineFlowContext extends FlowContext {

	public UnconditionalFlowInfo initsOnReturn;
	
	public InsideSubRoutineFlowContext(
		FlowContext parent,
		ASTNode associatedNode) {
		super(parent, associatedNode);
		this.initsOnReturn = FlowInfo.DEAD_END;				
	}

	public String individualToString() {
		
		StringBuffer buffer = new StringBuffer("Inside SubRoutine flow context"); //$NON-NLS-1$
		buffer.append("[initsOnReturn -").append(initsOnReturn.toString()).append(']'); //$NON-NLS-1$
		return buffer.toString();
	}
		
	public UnconditionalFlowInfo initsOnReturn(){
		return this.initsOnReturn;
	}
		
	public boolean isNonReturningContext() {
		return subRoutine().isSubRoutineEscaping();
	}
	
	public SubRoutineStatement subRoutine() {
		return (SubRoutineStatement)associatedNode;
	}
	
	public void recordReturnFrom(FlowInfo flowInfo) {

		if (!flowInfo.isReachable()) return; 
		if (initsOnReturn == FlowInfo.DEAD_END) {
			initsOnReturn = flowInfo.copy().unconditionalInits();
		} else {
			initsOnReturn = initsOnReturn.mergedWith(flowInfo.copy().unconditionalInits());
		}
	}
}
