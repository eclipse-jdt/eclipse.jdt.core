package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class PostfixExpression extends CompoundAssignment {

public PostfixExpression(Expression l, Expression e, int op, int pos) {
	super(l, e, op);
	sourceStart = l.sourceStart;
	sourceEnd = pos ;
}
/**
 * Code generation for PostfixExpression
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */ 
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

	// various scenarii are possible, setting an array reference, 
	// a field reference, a blank final field reference, a field of an enclosing instance or 
	// just a local variable.

	int pc = codeStream.position;
	lhs.generatePostIncrement(currentScope, codeStream, this, valueRequired);
	if (valueRequired) {
		codeStream.generateImplicitConversion(implicitConversion);
	}
	codeStream.recordPositionsFrom(pc, this);
}
public String operatorToString() {
	switch (operator) {
		case PLUS :
			return "++"/*nonNLS*/;
		case MINUS :
			return "--"/*nonNLS*/;}
	return "unknown operator"/*nonNLS*/;
}
public boolean restrainUsageToNumericTypes(){
	return true ;}
public String toStringExpressionNoParenthesis(){
	/* slow code*/

	return	lhs.toStringExpression() + " "/*nonNLS*/ + operatorToString(); }
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		lhs.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
