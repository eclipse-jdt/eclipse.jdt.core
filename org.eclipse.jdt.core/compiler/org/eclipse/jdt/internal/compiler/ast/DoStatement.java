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

public class DoStatement extends Statement {
	public Expression condition;
	public Statement action ;

	private Label breakLabel, continueLabel;

	// for local variables table attributes
	int mergedInitStateIndex = -1;
public DoStatement(Expression condition, Statement action, int s, int e) {
	this.sourceStart =  s;
	this.sourceEnd = e;
	this.condition = condition;
	this.action = action;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	breakLabel = new Label();
	continueLabel = new Label();
	LoopingFlowContext loopingContext = new LoopingFlowContext(flowContext, this, breakLabel, continueLabel, currentScope);

	Constant conditionConstant = condition.constant;
	Constant conditionalConstant = condition.conditionalConstant();	
	boolean isFalseCondition = 
		((conditionConstant != NotAConstant) && (conditionConstant.booleanValue() == false))
			|| ((conditionalConstant != NotAConstant) && (conditionalConstant.booleanValue() == false));
	
	if ((action != null) && !action.isEmptyBlock()) {
		//if (!flowInfo.complainIfUnreachable(this, currentScope)){ // probably useless since would be tested outside the do
		flowInfo = action.analyseCode(currentScope, loopingContext, flowInfo.copy());
		// unnecessary to check here}

		// code generation can be optimized when no need to continue in the loop
		if ((flowInfo == FlowInfo.DeadEnd) || flowInfo.isFakeReachable()){
			if ((loopingContext.initsOnContinue == FlowInfo.DeadEnd) || loopingContext.initsOnContinue.isFakeReachable()){
				continueLabel = null;
			} else {
				flowInfo = loopingContext.initsOnContinue; // for condition
				if (isFalseCondition){
					//	continueLabel = null; - cannot nil the label since may be targeted already by 'continue' statements
				} else {
					loopingContext.complainOnFinalAssignmentsInLoop(currentScope, flowInfo);
				}
			}
		} else {
			if (isFalseCondition){
				//	continueLabel = null; - cannot nil the label since may be targeted already by 'continue' statements
			} else {
				loopingContext.complainOnFinalAssignmentsInLoop(currentScope, flowInfo);
			}
		}
	}
	LoopingFlowContext condLoopContext;
	flowInfo = condition.analyseCode(currentScope, (condLoopContext = new LoopingFlowContext(flowContext, this, null, null, currentScope)), (action == null ? flowInfo : (flowInfo.mergedWith(loopingContext.initsOnContinue))));
	condLoopContext.complainOnFinalAssignmentsInLoop(currentScope, flowInfo);

	// infinite loop
	FlowInfo mergedInfo;
	if ((condition.constant != NotAConstant) && (condition.constant.booleanValue() == true)) {
		mergedInfo = loopingContext.initsOnBreak;
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	// end of loop: either condition false or break
	mergedInfo = flowInfo.initsWhenFalse().unconditionalInits().mergedWith(loopingContext.initsOnBreak);
	mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
	return mergedInfo;
}
/**
 * Do statement code generation
 *
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {

	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	int pc = codeStream.position;

	// labels management
	Label actionLabel = new Label(codeStream);
	actionLabel.place();
	breakLabel.codeStream = codeStream;
	if (continueLabel != null) {
		continueLabel.codeStream = codeStream;
	}

	// generate action
	if (action != null){
		action.generateCode(currentScope, codeStream);
	}
	// generate condition
	if (continueLabel != null){
		continueLabel.place();
		condition.generateOptimizedBoolean(currentScope, codeStream, actionLabel, null, true);
	}
	breakLabel.place();

	// May loose some local variable initializations : affecting the local variable attributes
	if (mergedInitStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
	}
	codeStream.recordPositionsFrom(pc, this);
	
}
public void resolve(BlockScope scope) {
	TypeBinding type = condition.resolveTypeExpecting(scope, BooleanBinding);
	condition.implicitWidening(type, type);
	if (action != null)
		action.resolve(scope);
}
public String toString(int tab) {
	/* slow code */

	String inFront, s = tabString(tab);
	inFront = s;
	s = s + "do"/*nonNLS*/;
	if (action == null)
		s = s + " {}\n"/*nonNLS*/;
	else
		if (action instanceof Block)
			s = s + "\n"/*nonNLS*/ + action.toString(tab + 1) + "\n"/*nonNLS*/;
		else
			s = s + " {\n"/*nonNLS*/ + action.toString(tab + 1) + ";}\n"/*nonNLS*/;
	s = s + inFront + "while ("/*nonNLS*/ + condition.toStringExpression() + ")"/*nonNLS*/;
	return s;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		if (action != null) {
			action.traverse(visitor, scope);
		}
		condition.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
