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

public class Case extends Statement {
	public Expression constantExpression;
	public CaseLabel targetLabel;
public Case(Expression constantExpression) {
	this.constantExpression = constantExpression;
	sourceEnd = constantExpression.sourceEnd;
	sourceStart = constantExpression.sourceStart;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	/* EXTRA REFERENCE RECORDING
	// need to record field references sitting in case constants
	FieldBinding binding;
	if ((binding = constant.fieldBinding()) != null) {
		currentScope.referenceType().recordReferenceTo(binding);
	}
	*/
	if (constantExpression.constant == NotAConstant) 
		currentScope.problemReporter().caseExpressionMustBeConstant(constantExpression);
	return flowInfo;
}
/**
 * Case code generation
 *
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {

	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	int pc = codeStream.position;
	targetLabel.place();
	codeStream.recordPositionsFrom(pc, this);
	
}
public void resolve(BlockScope scope) {
	// error....use resolveCase....

	throw new NullPointerException();
}
public Constant resolveCase(BlockScope scope, TypeBinding testTb, SwitchStatement switchStatement) {
	// add into the collection of cases of the associated switch statement
	switchStatement.cases[switchStatement.caseCount++] = this;
	TypeBinding caseTb = constantExpression.resolveType(scope);
	if (caseTb == null || testTb == null)
		return null;
	if (constantExpression.isConstantValueOfTypeAssignableToType(caseTb, testTb))
		return constantExpression.constant;
	if (scope.areTypesCompatible(caseTb, testTb))
		return constantExpression.constant;
	scope.problemReporter().typeMismatchErrorActualTypeExpectedType(constantExpression, caseTb, testTb);
	return null;
}
public String toString(int tab){
	/* slow code */

	String s = tabString(tab);
	s = s + "case "/*nonNLS*/ + constantExpression.toStringExpression() + " : "/*nonNLS*/ ;
	return s;}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		constantExpression.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
}
