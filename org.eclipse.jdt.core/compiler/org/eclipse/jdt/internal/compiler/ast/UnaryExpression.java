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


public class UnaryExpression extends OperatorExpression {
	public Expression expression;
	public Constant optimizedBooleanConstant;

public UnaryExpression(Expression expression, int operator) {
	this.expression = expression;
	this.bits |= operator << OperatorSHIFT; // encode operator
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	if (((bits & OperatorMASK) >> OperatorSHIFT) == NOT) {
		return expression.analyseCode(currentScope, flowContext, flowInfo).asNegatedCondition();
	} else {
		return expression.analyseCode(currentScope, flowContext, flowInfo);
	}
}
public Constant conditionalConstant(){

		return 	optimizedBooleanConstant == null ? 
					constant : 
					optimizedBooleanConstant ;}
/**
 * Code generation for an unary operation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */ 
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	Label falseLabel, endifLabel;
	if (constant != Constant.NotAConstant) {
		// inlined value
		if (valueRequired){
			codeStream.generateConstant(constant, implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this);
		return;
	}
	switch ((bits & OperatorMASK) >> OperatorSHIFT) {
		case NOT :
			switch (expression.implicitConversion >> 4 )/* runtime type */ {
				case T_boolean :
					// ! <boolean>
					// Generate code for the condition
					expression.generateOptimizedBoolean(currentScope, codeStream, null, (falseLabel = new Label(codeStream)), valueRequired);
					if (valueRequired) {
						codeStream.iconst_0();
						codeStream.goto_(endifLabel = new Label(codeStream));
						codeStream.decrStackSize(1);
						falseLabel.place();
						if (valueRequired)
							codeStream.iconst_1();
						endifLabel.place();
					}
					break;
			}
			break;
		case TWIDDLE :
			switch (expression.implicitConversion  >> 4 /* runtime */) {
				case T_int :
					// ~int
					expression.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired) {
						codeStream.iconst_m1();
						codeStream.ixor();
					}
					break;
				case T_long :
					expression.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired) {
						codeStream.ldc2_w(-1L);
						codeStream.lxor();
					}
			}
			break;
		case MINUS :
			// - <num>
			if (constant != NotAConstant) {
				if (valueRequired) {
					switch (expression.implicitConversion >> 4 /* runtime */) {
						case T_int :
							codeStream.generateInlinedValue(constant.intValue() * -1);
							break;
						case T_float :
							codeStream.generateInlinedValue(constant.floatValue() * -1.0f);
							break;
						case T_long :
							codeStream.generateInlinedValue(constant.longValue() * -1L);
							break;
						case T_double :
							codeStream.generateInlinedValue(constant.doubleValue() * -1.0);
					}
				}
			} else {
				expression.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					switch (expression.implicitConversion >> 4 /* runtime type */) {
						case T_int :
							codeStream.ineg();
							break;
						case T_float :
							codeStream.fneg();
							break;
						case T_long :
							codeStream.lneg();
							break;
						case T_double :
							codeStream.dneg();
					}
				}
			}
			break;
		case PLUS :
			expression.generateCode(currentScope, codeStream, valueRequired);
	}
	if (valueRequired) {
		codeStream.generateImplicitConversion(implicitConversion);
	}
	codeStream.recordPositionsFrom(pc, this);
}
/**
 * Boolean operator code generation
 *	Optimized operations are: &&, ||, <, <=, >, >=, &, |, ^
 */
public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {

	if ((constant != Constant.NotAConstant) && (constant.typeID() == T_boolean)) {
		int pc = codeStream.position;
		if (constant.booleanValue() == true) {
			// constant == true
			if (valueRequired) {
				if (falseLabel == null) {
					// implicit falling through the FALSE case
					if (trueLabel != null) {
						codeStream.goto_(trueLabel);
					}
				}
			}
		} else {
			if (valueRequired) {
				if (falseLabel != null) {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.goto_(falseLabel);
					}
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this);
		return;
	}
	if (((bits & OperatorMASK) >> OperatorSHIFT) == NOT) {
		expression.generateOptimizedBoolean(currentScope, codeStream, falseLabel, trueLabel, valueRequired);
	} else {
		super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
	}
}
public TypeBinding resolveType(BlockScope scope) {
	TypeBinding expressionTb = expression.resolveType(scope);
	if (expressionTb == null){
		constant = NotAConstant;
		return null;
	}
	int expressionId = expressionTb.id;
	if (expressionId > 15) {
		constant = NotAConstant;
		scope.problemReporter().invalidOperator(this, expressionTb);
		return null;
	}

	int tableId;
	switch ((bits & OperatorMASK) >> OperatorSHIFT) {
		case NOT :
			tableId = AND_AND;
			break;
		case TWIDDLE :
			tableId = LEFT_SHIFT;
			break;
		default :
			tableId = MINUS;
	} //+ and - cases

	// the code is an int
	// (cast)  left   Op (cast)  rigth --> result
	//  0000   0000       0000   0000      0000
	//  <<16   <<12       <<8    <<4       <<0
	int result = ResolveTypeTables[tableId][(expressionId << 4) + expressionId];
	expression.implicitConversion = result >>> 12;
	TypeBinding type;
	switch (result & 0x0000F) { // only switch on possible result type.....
		case T_boolean :
			type = BooleanBinding;
			break;
		case T_byte :
			type = ByteBinding;
			break;
		case T_char :
			type = CharBinding;
			break;
		case T_double :
			type = DoubleBinding;
			break;
		case T_float :
			type = FloatBinding;
			break;
		case T_int :
			type = IntBinding;
			break;
		case T_long :
			type = LongBinding;
			break;
		default : //error........
			constant = Constant.NotAConstant;
			if (expressionId != T_undefined)
				scope.problemReporter().invalidOperator(this, expressionTb);
			return null;
	}

	// compute the constant when valid
	if (expression.constant != Constant.NotAConstant) {
		constant = Constant.computeConstantOperation(expression.constant, expressionId, (bits & OperatorMASK) >> OperatorSHIFT);
	} else {
		constant = Constant.NotAConstant;
		if (((bits & OperatorMASK) >> OperatorSHIFT) == NOT) {
			Constant cst = expression.conditionalConstant();
			if (cst.typeID() == T_boolean)
				optimizedBooleanConstant = Constant.fromValue(!cst.booleanValue());
		}
	}
	return type;
}
public String toStringExpressionNoParenthesis(){
	/* slow code*/

	return	operatorToString() + " " + expression.toStringExpression() ; }
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		expression.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
}
