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
import org.eclipse.jdt.internal.compiler.util.*;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class LabelFlowContext extends SwitchFlowContext {
	public char[] labelName;
public LabelFlowContext(FlowContext parent, AstNode associatedNode, char[] labelName, Label breakLabel, BlockScope scope){
	
	super(parent, associatedNode, breakLabel);
	this.labelName = labelName;	
	checkLabelValidity(scope);
}
void checkLabelValidity(BlockScope scope) {

	// check if label was already defined above

	FlowContext current = parent;
	while (current != null) {
		char[] currentLabelName;
		if (((currentLabelName = current.labelName()) != null) 
			&& CharOperation.equals(currentLabelName, labelName)) {
			scope.problemReporter().alreadyDefinedLabel(labelName, associatedNode);
		}
		current = current.parent;
	}
}
public String individualToString(){
	return "Label flow context [label:"+String.valueOf(labelName)+"]"; //$NON-NLS-2$ //$NON-NLS-1$
}
public char[] labelName() {
	return labelName;
}
}
