package org.eclipse.jdt.internal.compiler.flow;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class SwitchFlowContext extends FlowContext {
	public Label breakLabel;
	public UnconditionalFlowInfo initsOnBreak = FlowInfo.DeadEnd;
/**
 * 
 */
public SwitchFlowContext(FlowContext parent, AstNode associatedNode, Label breakLabel) {
	super(parent, associatedNode);
	this.breakLabel = breakLabel;
}
public Label breakLabel() {
	return breakLabel;
}
public String individualToString(){
	return "Switch flow context"/*nonNLS*/;
}
public boolean isBreakable() {
	return true;
}
public void recordBreakFrom(FlowInfo flowInfo) {

	if (initsOnBreak == FlowInfo.DeadEnd) {
		initsOnBreak = flowInfo.copy().unconditionalInits();
	} else {
		// ignore if not really reachable (1FKEKRP)
		if (flowInfo.isFakeReachable()) return;
		initsOnBreak.mergedWith(flowInfo.unconditionalInits());
	};
}
}
