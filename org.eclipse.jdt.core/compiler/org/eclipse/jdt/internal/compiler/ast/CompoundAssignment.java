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

public class CompoundAssignment extends Assignment implements OperatorIds {
	public int operator;
	public int assignmentImplicitConversion;

	//  var op exp is equivalent to var = (varType) var op exp
	// assignmentImplicitConversion stores the cast needed for the assignment

public CompoundAssignment(Expression lhs, Expression expression,int operator) {
	//lhs is always a reference by construction ,
	//but is build as an expression ==> the checkcast cannot fail

	super(lhs, expression);
	this.operator = operator ;
	
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// record setting a variable: various scenarii are possible, setting an array reference, 
	// a field reference, a blank final field reference, a field of an enclosing instance or 
	// just a local variable.

	return lhs.analyseAssignment(currentScope, flowContext, flowInfo, this, true).unconditionalInits();
}
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

	// various scenarii are possible, setting an array reference, 
	// a field reference, a blank final field reference, a field of an enclosing instance or 
	// just a local variable.

	int pc = codeStream.position;
	lhs.generateCompoundAssignment(currentScope, codeStream, expression, operator, assignmentImplicitConversion, valueRequired);
	if (valueRequired) {
		codeStream.generateImplicitConversion(implicitConversion);
	}
	codeStream.recordPositionsFrom(pc, this);
}
public String operatorToString() {
	switch (operator) {
		case PLUS :
			return "+="; //$NON-NLS-1$
		case MINUS :
			return "-="; //$NON-NLS-1$
		case MULTIPLY :
			return "*="; //$NON-NLS-1$
		case DIVIDE :
			return "/="; //$NON-NLS-1$
		case AND :
			return "&="; //$NON-NLS-1$
		case OR :
			return "|="; //$NON-NLS-1$
		case XOR :
			return "^="; //$NON-NLS-1$
		case REMAINDER :
			return "%="; //$NON-NLS-1$
		case LEFT_SHIFT :
			return "<<="; //$NON-NLS-1$
		case RIGHT_SHIFT :
			return ">>="; //$NON-NLS-1$
		case UNSIGNED_RIGHT_SHIFT :
			return ">>>="; //$NON-NLS-1$
	};
	return "unknown operator"; //$NON-NLS-1$
}
public TypeBinding resolveType(BlockScope scope) {
	constant = NotAConstant;
	TypeBinding lhsTb = lhs.resolveType(scope);
	TypeBinding expressionTb = expression.resolveType(scope);
	if (lhsTb == null || expressionTb == null)
		return null;

	int lhsId = lhsTb.id;
	int expressionId = expressionTb.id;
	if (restrainUsageToNumericTypes() && !lhsTb.isNumericType()) {
		scope.problemReporter().operatorOnlyValidOnNumericType(this, lhsTb, expressionTb);
		return null;
	}
	if (lhsId > 15 || expressionId > 15) {
		if (lhsId != T_String) { // String += Object is valid wheraas Object -= String is not
			scope.problemReporter().invalidOperator(this, lhsTb, expressionTb);
			return null;
		}
		expressionId = T_Object; // use the Object has tag table
	}

	// the code is an int
	// (cast)  left   Op (cast)  rigth --> result 
	//  0000   0000       0000   0000      0000
	//  <<16   <<12       <<8     <<4        <<0

	// the conversion is stored INTO the reference (info needed for the code gen)
	int result = OperatorExpression.ResolveTypeTables[operator][ (lhsId << 4) + expressionId];
	if (result == T_undefined) {
		scope.problemReporter().invalidOperator(this, lhsTb, expressionTb);
		return null;
	}
	if (operator == PLUS && scope.isJavaLangObject(lhsTb)) {
		// Object o = "hello"; 
		// o += " world"  // <--illegal
		scope.problemReporter().invalidOperator(this, lhsTb, expressionTb);
		return null;
	}
	lhs.implicitConversion = result >>> 12;
	expression.implicitConversion = (result >>> 4) & 0x000FF;
	assignmentImplicitConversion = (lhsId << 4) + (result & 0x0000F);
	return lhsTb;
}
public boolean restrainUsageToNumericTypes(){
	return false ;}
public String toStringExpressionNoParenthesis() {

	return 	lhs.toStringExpression() + " " + //$NON-NLS-1$
			operatorToString() + " " + //$NON-NLS-1$
			expression.toStringExpression() ; }
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		lhs.traverse(visitor, scope);
		expression.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
