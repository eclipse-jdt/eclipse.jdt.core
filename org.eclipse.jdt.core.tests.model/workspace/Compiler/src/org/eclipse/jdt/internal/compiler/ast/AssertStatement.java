/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.ASTVisitor;

public class AssertStatement extends Statement {
	
	public Expression assertExpression, exceptionArgument;

	// for local variable attribute
	int preAssertInitStateIndex = -1;
	private FieldBinding assertionSyntheticFieldBinding;
	
	public AssertStatement(
		Expression exceptionArgument,
		Expression assertExpression,
		int startPosition) {
			
		this.assertExpression = assertExpression;
		this.exceptionArgument = exceptionArgument;
		sourceStart = startPosition;
		sourceEnd = exceptionArgument.sourceEnd;
	}

	public AssertStatement(Expression assertExpression, int startPosition) {

		this.assertExpression = assertExpression;
		sourceStart = startPosition;
		sourceEnd = assertExpression.sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
			
		preAssertInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);

		Constant cst = this.assertExpression.optimizedBooleanConstant();		
		boolean isOptimizedTrueAssertion = cst != NotAConstant && cst.booleanValue() == true;
		boolean isOptimizedFalseAssertion = cst != NotAConstant && cst.booleanValue() == false;

		FlowInfo assertInfo = flowInfo.copy();
		if (isOptimizedTrueAssertion) {
			assertInfo.setReachMode(FlowInfo.UNREACHABLE);
		}
		assertInfo = assertExpression.analyseCode(currentScope, flowContext, assertInfo).unconditionalInits();
		
		if (exceptionArgument != null) {
			// only gets evaluated when escaping - results are not taken into account
			FlowInfo exceptionInfo = exceptionArgument.analyseCode(currentScope, flowContext, assertInfo.copy()); 
			
			if (!isOptimizedTrueAssertion){
				flowContext.checkExceptionHandlers(
					currentScope.getJavaLangAssertionError(),
					this,
					exceptionInfo,
					currentScope);
			}
		}
		
		if (!isOptimizedTrueAssertion){
			// add the assert support in the clinit
			manageSyntheticAccessIfNecessary(currentScope, flowInfo);
		}
		if (isOptimizedFalseAssertion) {
			return flowInfo; // if assertions are enabled, the following code will be unreachable
		} else {
			return flowInfo.mergedWith(assertInfo.unconditionalInits()); 
		}
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;
	
		if (this.assertionSyntheticFieldBinding != null) {
			Label assertionActivationLabel = new Label(codeStream);
			codeStream.getstatic(this.assertionSyntheticFieldBinding);
			codeStream.ifne(assertionActivationLabel);
			
			Label falseLabel = new Label(codeStream);
			this.assertExpression.generateOptimizedBoolean(currentScope, codeStream, (falseLabel = new Label(codeStream)), null , true);
			codeStream.newJavaLangAssertionError();
			codeStream.dup();
			if (exceptionArgument != null) {
				exceptionArgument.generateCode(currentScope, codeStream, true);
				codeStream.invokeJavaLangAssertionErrorConstructor(exceptionArgument.implicitConversion & 0xF);
			} else {
				codeStream.invokeJavaLangAssertionErrorDefaultConstructor();
			}
			codeStream.athrow();
			falseLabel.place();
			assertionActivationLabel.place();
		}
		
		// May loose some local variable initializations : affecting the local variable attributes
		if (preAssertInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, preAssertInitStateIndex);
		}	
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void resolve(BlockScope scope) {

		assertExpression.resolveTypeExpecting(scope, BooleanBinding);
		if (exceptionArgument != null) {
			TypeBinding exceptionArgumentType = exceptionArgument.resolveType(scope);
			if (exceptionArgumentType != null){
			    int id = exceptionArgumentType.id;
			    switch(id) {
					case T_void :
						scope.problemReporter().illegalVoidExpression(exceptionArgument);
					default:
					    id = T_Object;
					case T_boolean :
					case T_byte :
					case T_char :
					case T_short :
					case T_double :
					case T_float :
					case T_int :
					case T_long :
					case T_String :
						exceptionArgument.implicitConversion = (id << 4) + id;
				}
			}
		}
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			assertExpression.traverse(visitor, scope);
			if (exceptionArgument != null) {
				exceptionArgument.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}	
	
	public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

		if (!flowInfo.isReachable()) return;
		
		// need assertion flag: $assertionsDisabled on outer most source clas
		// (in case of static member of interface, will use the outermost static member - bug 22334)
		SourceTypeBinding outerMostClass = currentScope.enclosingSourceType();
		while (outerMostClass.isLocalType()){
			ReferenceBinding enclosing = outerMostClass.enclosingType();
			if (enclosing == null || enclosing.isInterface()) break;
			outerMostClass = (SourceTypeBinding) enclosing;
		}

		this.assertionSyntheticFieldBinding = outerMostClass.addSyntheticField(this, currentScope);

		// find <clinit> and enable assertion support
		TypeDeclaration typeDeclaration = outerMostClass.scope.referenceType();
		AbstractMethodDeclaration[] methods = typeDeclaration.methods;
		for (int i = 0, max = methods.length; i < max; i++) {
			AbstractMethodDeclaration method = methods[i];
			if (method.isClinit()) {
				((Clinit) method).setAssertionSupport(assertionSyntheticFieldBinding, currentScope.environment().options.sourceLevel < ClassFileConstants.JDK1_5);
				break;
			}
		}
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output);
		output.append("assert "); //$NON-NLS-1$
		this.assertExpression.printExpression(0, output);
		if (this.exceptionArgument != null) {
			output.append(": "); //$NON-NLS-1$
			this.exceptionArgument.printExpression(0, output);
		}
		return output.append(';');
	}
	
}
