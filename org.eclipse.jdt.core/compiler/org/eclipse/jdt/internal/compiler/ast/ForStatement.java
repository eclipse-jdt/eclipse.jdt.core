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

public class ForStatement extends Statement {
	public Statement[] initializations ;
	public Expression condition ;
	public Statement[] increments ;
	public Statement action ;

	//when there is no local declaration, there is no need of a new scope
	//scope is positionned either to a new scope, or to the "upper"scope (see resolveType)
	public boolean neededScope;
	public BlockScope scope;
	
	private Label breakLabel, continueLabel;

	// for local variables table attributes
	int preCondInitStateIndex = -1;
	int condIfTrueInitStateIndex = -1;
	int mergedInitStateIndex = -1;
public ForStatement(Statement[] initializations, Expression condition, Statement[] increments, Statement action, boolean neededScope, int s, int e) {
	this.sourceStart = s;
	this.sourceEnd = e;
	this.initializations = initializations;
	this.condition = condition;
	this.increments = increments;
	this.action = action;
	this.neededScope = neededScope;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	breakLabel = new Label();
	continueLabel = new Label();

	// process the initializations
	if (initializations != null) {
		int count = initializations.length, i = 0;
		while (i < count) {
			flowInfo = initializations[i++].analyseCode(scope, flowContext, flowInfo);
		}
	}
	preCondInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
	
	// process the condition
	LoopingFlowContext condLoopContext = null;
	if (condition != null) {
		if ((condition.constant == NotAConstant) || (condition.constant.booleanValue() != true)) {
			flowInfo = condition.analyseCode(scope, (condLoopContext = new LoopingFlowContext(flowContext, this, null, null, scope)), flowInfo);
		}
	}

	// process the action
	LoopingFlowContext loopingContext;
	FlowInfo actionInfo;
	if ((action == null) || action.isEmptyBlock()) {
		if (condLoopContext != null) condLoopContext.complainOnFinalAssignmentsInLoop(scope, flowInfo);
		if ((condition == null) || ((condition.constant != NotAConstant) && (condition.constant.booleanValue() == true))) {
			return FlowInfo.DeadEnd;
		} else {
			actionInfo = flowInfo.initsWhenTrue().copy();
			loopingContext = new LoopingFlowContext(flowContext, this, breakLabel, continueLabel, scope);
		}
	} else {
		loopingContext = new LoopingFlowContext(flowContext, this, breakLabel, continueLabel, scope);
		FlowInfo initsWhenTrue = flowInfo.initsWhenTrue();
		condIfTrueInitStateIndex = currentScope.methodScope().recordInitializationStates(initsWhenTrue);
		actionInfo = action.analyseCode(
			scope, 
			loopingContext, 
			((condition != null) && (condition.constant != NotAConstant) && (condition.constant.booleanValue() == false)) ? // unreachable when condition inlined to false
				FlowInfo.DeadEnd : 
				initsWhenTrue.copy());

		// code generation can be optimized when no need to continue in the loop
		if (((actionInfo == FlowInfo.DeadEnd) || actionInfo.isFakeReachable())
			&& ((loopingContext.initsOnContinue == FlowInfo.DeadEnd) || loopingContext.initsOnContinue.isFakeReachable())){
				continueLabel = null;
		} else {
			if (condLoopContext != null) condLoopContext.complainOnFinalAssignmentsInLoop(scope, flowInfo);
			loopingContext.complainOnFinalAssignmentsInLoop(scope, actionInfo);
			actionInfo = actionInfo.mergedWith(loopingContext.initsOnContinue.unconditionalInits()); // for increments
		}
	}
	if ((continueLabel != null) && (increments != null)) {
		LoopingFlowContext loopContext = new LoopingFlowContext(flowContext, this, null, null, scope);
		int i = 0, count = increments.length;
		while (i < count)
			actionInfo = increments[i++].analyseCode(scope, loopContext, actionInfo);
		loopContext.complainOnFinalAssignmentsInLoop(scope, flowInfo);
	}

	// infinite loop
	FlowInfo mergedInfo;
	if ((condition == null) || ((condition.constant != NotAConstant) && (condition.constant.booleanValue() == true))) {
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo = loopingContext.initsOnBreak);
		return mergedInfo;
	}

	//end of loop: either condition false or break
	mergedInfo = flowInfo.initsWhenFalse().unconditionalInits().mergedWith(loopingContext.initsOnBreak.unconditionalInits());
	mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
	return mergedInfo;
}
/**
 * For statement code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	int pc = codeStream.position;

	// generate the initializations
	if (initializations != null){
		for (int i = 0, max = initializations.length; i < max; i++){
			initializations[i].generateCode(scope, codeStream);
		}
	}

	// label management
	Label actionLabel = new Label(codeStream);
	Label conditionLabel = new Label(codeStream);
	breakLabel.codeStream = codeStream;
	if (continueLabel != null) {
		continueLabel.codeStream = codeStream;
	}
	// jump over the actionBlock
	if ((condition != null) 
		&& (condition.constant == NotAConstant) 
		&& !((action == null || action.isEmptyBlock()) && (increments == null))) {
		int jumpPC = codeStream.position;
		codeStream.goto_(conditionLabel);
		codeStream.recordPositionsFrom(jumpPC, condition);
	}
	// generate the loop action
	actionLabel.place();
	if (action != null) {
		// Required to fix 1PR0XVS: LFRE:WINNT - Compiler: variable table for method appears incorrect
		if (condIfTrueInitStateIndex != -1){ // insert all locals initialized inside the condition into the action generated prior to the condition
			codeStream.addDefinitelyAssignedVariables(currentScope, condIfTrueInitStateIndex);
		}
		action.generateCode(scope, codeStream);
	}
	// continuation point
	if (continueLabel != null) {
		continueLabel.place();
		// generate the increments for next iteration
		if (increments != null) {
			for (int i = 0, max = increments.length; i < max; i++) {
				increments[i].generateCode(scope, codeStream);
			}
		}
	}
	
	// May loose some local variable initializations : affecting the local variable attributes
	if (preCondInitStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(currentScope, preCondInitStateIndex);
	}	
	
	// generate the condition
	conditionLabel.place();
	if ((condition != null) && (condition.constant == NotAConstant)) {
		condition.generateOptimizedBoolean(scope, codeStream, actionLabel, null, true);
	} else {
		if (continueLabel != null) {
			codeStream.goto_(actionLabel);
		}
	}
	breakLabel.place();
	
	// May loose some local variable initializations : affecting the local variable attributes
	if (neededScope) {
		codeStream.exitUserScope(scope);		
	}
	if (mergedInitStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
	}
	codeStream.recordPositionsFrom(pc, this);
}
public void resolve(BlockScope upperScope) {
	// use the scope that will hold the init declarations

	scope = neededScope ? new BlockScope(upperScope) : upperScope;
	if (initializations != null)
		for (int i = 0, length = initializations.length; i < length; i++)
			initializations[i].resolve(scope);
	if (condition != null) {
		TypeBinding type = condition.resolveTypeExpecting(scope, BooleanBinding);
		condition.implicitWidening(type, type);
	}
	if (increments != null)
		for (int i = 0, length = increments.length; i < length; i++)
			increments[i].resolve(scope);
	if (action != null)
		action.resolve(scope);
}
public String toString(int tab ){
	/* slow code */
	
	String s = tabString(tab) + "for ("/*nonNLS*/;
	if (!neededScope)
		s = s + " //--NO upperscope scope needed\n"/*nonNLS*/ + tabString(tab) + "     "/*nonNLS*/ ;
	//inits
	if (initializations != null)
	{	for (int i = 0 ; i < initializations.length ; i++){
			//nice only with expressions
			s = s + initializations[i].toString(0);
			if (i != (initializations.length -1)) s = s + " , "/*nonNLS*/ ;}};
	s = s + "; "/*nonNLS*/ ;
	//cond
	if (condition != null)
		s = s + condition.toStringExpression() ;
	s = s + "; "/*nonNLS*/ ;
	//updates
	if (increments != null)
	{	for (int i = 0 ; i < increments.length ; i++){
			//nice only with expressions
			s = s + increments[i].toString(0);
			if (i != (increments.length -1)) s = s + " , "/*nonNLS*/ ;}};
	s = s + ") "/*nonNLS*/ ;
	//block
	if (action == null)
		s = s + "{}"/*nonNLS*/ ;
	else
		s = s + "\n"/*nonNLS*/+ action.toString(tab+1) ;
	return s;}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		if (initializations != null) {
			int initializationsLength = initializations.length;
			for (int i = 0; i < initializationsLength; i++)
				initializations[i].traverse(visitor, scope);
		}

		if (condition != null)
			condition.traverse(visitor, scope);

		if (increments != null) {
			int incrementsLength = increments.length;
			for (int i = 0; i < incrementsLength; i++)
				increments[i].traverse(visitor, scope);
		}

		if (action != null)
			action.traverse(visitor, scope);
	}
	visitor.endVisit(this, blockScope);
}
}
