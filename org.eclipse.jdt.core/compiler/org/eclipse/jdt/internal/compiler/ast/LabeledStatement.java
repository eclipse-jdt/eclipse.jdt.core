package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class LabeledStatement extends Statement {
	public Statement statement;
	public char[] label;
	public Label targetLabel;

	// for local variables table attributes
	int mergedInitStateIndex = -1;
/**
 * LabeledStatement constructor comment.
 */
public LabeledStatement(char[] l , Statement st, int s,int e) {
	statement = st ;
	label = l ;
	sourceStart = s;
	sourceEnd = e;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	// need to stack a context to store explicit label, answer inits in case of normal completion merged
	// with those relative to the exit path from break statement occurring inside the labeled statement.

	if (statement == null) {
		return flowInfo;
	} else {
		LabelFlowContext labelContext;
		FlowInfo mergedInfo = statement.analyseCode(
			currentScope,
			(labelContext = new LabelFlowContext(flowContext, this, label, (targetLabel = new Label()), currentScope)),
			flowInfo).
				mergedWith(labelContext.initsOnBreak);
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}
}
public AstNode concreteStatement() {
	return statement.concreteStatement();
}
/**
 * Code generation for labeled statement
 *
 * may not need actual source positions recording
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	int pc = codeStream.position;
	if (targetLabel != null) {
		targetLabel.codeStream = codeStream;
		if (statement != null) {
			statement.generateCode(currentScope, codeStream);
		}
		targetLabel.place();
	}
	// May loose some local variable initializations : affecting the local variable attributes
	if (mergedInitStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
	}
	codeStream.recordPositionsFrom(pc, this);
}
public void resolve(BlockScope scope) {
	statement.resolve(scope);
}
public String toString(int tab) {
	/* slow code */

	String s = tabString(tab);
	s += new String(label) + ": " + statement.toString(0); //$NON-NLS-1$
	return s;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		statement.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
}
