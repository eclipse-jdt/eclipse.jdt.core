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
import org.eclipse.jdt.internal.compiler.codegen.Label;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class SwitchFlowContext extends FlowContext {
	public Label breakLabel;
	public UnconditionalFlowInfo initsOnBreak = FlowInfo.DEAD_END;
	
	public SwitchFlowContext(
		FlowContext parent,
		ASTNode associatedNode,
		Label breakLabel) {
		super(parent, associatedNode);
		this.breakLabel = breakLabel;
	}

	public Label breakLabel() {
		return breakLabel;
	}

	public String individualToString() {
		StringBuffer buffer = new StringBuffer("Switch flow context"); //$NON-NLS-1$
		buffer.append("[initsOnBreak -").append(initsOnBreak.toString()).append(']'); //$NON-NLS-1$
		return buffer.toString();
	}

	public boolean isBreakable() {
		return true;
	}

	public void recordBreakFrom(FlowInfo flowInfo) {

		if (initsOnBreak == FlowInfo.DEAD_END) {
			initsOnBreak = flowInfo.copy().unconditionalInits();
		} else {
			initsOnBreak = initsOnBreak.mergedWith(flowInfo.copy().unconditionalInits());
		}
	}
}
