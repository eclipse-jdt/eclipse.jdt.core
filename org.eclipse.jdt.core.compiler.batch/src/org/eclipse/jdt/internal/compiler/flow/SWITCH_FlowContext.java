package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;

/**
 *
 * @author milan
 *
 */
public class SWITCH_FlowContext extends FlowContext {

	public BranchLabel breakLabel;
	public UnconditionalFlowInfo initsOnBreak = FlowInfo.DEAD_END;

public SWITCH_FlowContext(FlowContext parent, ASTNode associatedNode, BranchLabel breakLabel) {
	super(parent, associatedNode, true);
	this.breakLabel = breakLabel;
}

@Override
public BranchLabel breakLabel() {
	return this.breakLabel;
}

@Override
public String individualToString() {
	StringBuffer buffer = new StringBuffer("SWITCH flow context"); //$NON-NLS-1$
	buffer.append("[initsOnBreak -").append(this.initsOnBreak.toString()).append(']'); //$NON-NLS-1$
	return buffer.toString();
}

@Override
public boolean isBreakable() {
	return false;
}

@Override
public void recordBreakFrom(FlowInfo flowInfo) {
	if ((this.initsOnBreak.tagBits & FlowInfo.UNREACHABLE) == 0) {
		this.initsOnBreak = this.initsOnBreak.mergedWith(flowInfo.unconditionalInits());
	}
	else {
		this.initsOnBreak = flowInfo.unconditionalCopy();
	}
}
}
