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
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class InstanceOfExpression extends OperatorExpression {
	public Expression expression;
	public TypeReference type;
public InstanceOfExpression(Expression expression, TypeReference type, int operator) {
	this.expression = expression;
	this.type = type;
	this.bits |= operator << OperatorSHIFT;
	this.sourceStart = expression.sourceStart;
	this.sourceEnd = type.sourceEnd;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return expression.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
}
public final boolean areTypesCastCompatible(BlockScope scope, TypeBinding castTb, TypeBinding expressionTb) {
	//	see specifications p.68
	//A more cpmplete version of this method is provided on
	//CastExpression (it deals with constant and need runtime checkcast)

	//by grammatical construction, the first test is ALWAYS false
	//if (castTb.isBaseType())
	//{	if (expressionTb.isBaseType())
	//	{	if (expression.isConstantValueOfTypeAssignableToType(expressionTb,castTb))
	//		{	return true;}
	//		else
	//		{	if (expressionTb==castTb)
	//			{	return true;}
	//			else 
	//			{	if (scope.areTypesCompatible(expressionTb,castTb))
	//				{	return true; }
	//				
	//				if (BaseTypeBinding.isNarrowing(castTb.id,expressionTb.id))
	//				{	return true;}
	//				return false;}}}
	//	else
	//	{	return false; }}
	//else
	{ //-------------checkcast to something which is NOT a basetype----------------------------------	

		if (NullBinding == expressionTb)
			//null is compatible with every thing .... 
			{
			return true;
		}
		if (expressionTb.isArrayType()) {
			if (castTb.isArrayType()) { //------- (castTb.isArray) expressionTb.isArray -----------
				TypeBinding expressionEltTb = ((ArrayBinding) expressionTb).elementsType(scope);
				if (expressionEltTb.isBaseType())
					// <---stop the recursion------- 
					return ((ArrayBinding) castTb).elementsType(scope) == expressionEltTb;
				//recursivly on the elts...
				return areTypesCastCompatible(scope, ((ArrayBinding) castTb).elementsType(scope), expressionEltTb);
			}
			if (castTb.isClass()) { //------(castTb.isClass) expressionTb.isArray ---------------	
				if (scope.isJavaLangObject(castTb))
					return true;
				return false;
			}
			if (castTb.isInterface()) { //------- (castTb.isInterface) expressionTb.isArray -----------
				if (scope.isJavaLangCloneable(castTb) || scope.isJavaIoSerializable(castTb)) {
					return true;
				}
				return false;
			}

			//=========hoops=============
			return false;
		}
		if (expressionTb.isBaseType()) {
			return false;
		}
		if (expressionTb.isClass()) {
			if (castTb.isArrayType()) { // ---- (castTb.isArray) expressionTb.isClass -------
				if (scope.isJavaLangObject(expressionTb)) {
					return true;
				} else {
					return false;
				}
			}
			if (castTb.isClass()) { // ----- (castTb.isClass) expressionTb.isClass ------ 
				if (scope.areTypesCompatible(expressionTb, castTb))
					return true;
				else {
					if (scope.areTypesCompatible(castTb, expressionTb)) {
						return true;
					}
					return false;
				}
			}
			if (castTb.isInterface()) { // ----- (castTb.isInterface) expressionTb.isClass -------  
				if (((ReferenceBinding) expressionTb).isFinal()) { //no subclass for expressionTb, thus compile-time check is valid
					if (scope.areTypesCompatible(expressionTb, castTb))
						return true;
					return false;
				} else {
					return true;
				}
			}

			//=========hoops==============
			return false;
		}
		if (expressionTb.isInterface()) {
			if (castTb.isArrayType()) { // ----- (castTb.isArray) expressionTb.isInterface ------
				if (scope.isJavaLangCloneable(expressionTb) || scope.isJavaIoSerializable(expressionTb))
					//potential runtime error
					{
					return true;
				}
				return false;
			}
			if (castTb.isClass()) { // ----- (castTb.isClass) expressionTb.isInterface --------
				if (scope.isJavaLangObject(castTb))
					return true;
				if (((ReferenceBinding) castTb).isFinal()) { //no subclass for castTb, thus compile-time check is valid
					if (scope.areTypesCompatible(castTb, expressionTb)) {
						return true;
					}
					return false;
				}
				return true;
			}
			if (castTb.isInterface()) { // ----- (castTb.isInterface) expressionTb.isInterface -------
				if (castTb != expressionTb && (scope.compareTypes(castTb, expressionTb) == NotRelated)) {
					MethodBinding[] castTbMethods = ((ReferenceBinding) castTb).methods();
					int castTbMethodsLength = castTbMethods.length;
					MethodBinding[] expressionTbMethods = ((ReferenceBinding) expressionTb).methods();
					int expressionTbMethodsLength = expressionTbMethods.length;
					for (int i = 0; i < castTbMethodsLength; i++) {
						for (int j = 0; j < expressionTbMethodsLength; j++) {
							if (castTbMethods[i].selector == expressionTbMethods[j].selector) {
								if (castTbMethods[i].returnType != expressionTbMethods[j].returnType) {
									if (castTbMethods[i].areParametersEqual(expressionTbMethods[j])) {
										return false;
									}
								}
							}
						}
					}
				}
				return true;
			}

			//============hoops===========	
			return false;
		} // true;}

		//=======hoops==========
		return false;
	}
}
/**
 * Code generation for instanceOfExpression
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
*/ 
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	expression.generateCode(currentScope, codeStream, true);
	codeStream.instance_of(type.binding);
	if (!valueRequired)
		codeStream.pop();
	codeStream.recordPositionsFrom(pc, this);
}
public TypeBinding resolveType(BlockScope scope) {
	constant = NotAConstant;
	TypeBinding expressionTb = expression.resolveType(scope);
	TypeBinding checkTb = type.resolveType(scope);
	if (expressionTb == null || checkTb == null)
		return null;

	//===== by grammatical construction, the next test is always false =====
	//if (checkTb.isBaseType()) {
	//	scope.problemReporter().invalidTypeError(type,checkTb);
	//	return null;
	//}

	if (!areTypesCastCompatible(scope, checkTb, expressionTb)) {
		scope.problemReporter().notCompatibleTypesError(this, expressionTb, checkTb);
		return null;
	}
	return BooleanBinding;
}
public String toStringExpressionNoParenthesis(){
	/* slow code*/

	return	expression.toStringExpression() + " instanceof " + //$NON-NLS-1$
			type.toString(0) ; }
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		expression.traverse(visitor, scope);
		type.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
