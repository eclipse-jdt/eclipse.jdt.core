package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;

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
		FlowInfo assertInfo = flowInfo.copy();
			
		// add the assert support in the clinit
	
		if (exceptionArgument != null) {
			assertInfo = exceptionArgument.analyseCode(
						currentScope,
						flowContext,
						assertExpression.analyseCode(currentScope, flowContext, assertInfo).unconditionalInits())
					.unconditionalInits();
		} else {
			assertInfo = assertExpression.analyseCode(currentScope, flowContext, assertInfo).unconditionalInits();
		}
		
		// only retain potential initializations
		flowInfo.addPotentialInitializationsFrom(assertInfo.unconditionalInits());
		
		Constant constant = assertExpression.constant;
		
		if (constant != NotAConstant && constant.booleanValue() == true) {
			return flowInfo;
		}
		manageSyntheticAccessIfNecessary(currentScope);
		return flowInfo;
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position, divergePC;
	
		//  codegen here
		if (this.assertionSyntheticFieldBinding != null) {
			Label assertionActivationLabel = new Label(codeStream);
			codeStream.getstatic(this.assertionSyntheticFieldBinding);
			codeStream.ifne(assertionActivationLabel);
			Label falseLabel = new Label(codeStream);
			assertExpression.generateOptimizedBoolean(currentScope, codeStream, (falseLabel = new Label(codeStream)), null , true);
			codeStream.newJavaLangAssertionError();
			codeStream.dup();
			if (exceptionArgument != null) {
				exceptionArgument.generateCode(currentScope, codeStream, true);
				if (exceptionArgument.constant != NotAConstant) {
					codeStream.invokeJavaLangAssertionErrorConstructor(exceptionArgument.constant.typeID());
				} else {
					codeStream.invokeJavaLangAssertionErrorConstructor(exceptionArgument.implicitConversion & 0xF);
				}
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
		codeStream.recordPositionsFrom(pc, this);
	}

	public void resolve(BlockScope scope) {
		assertExpression.resolveTypeExpecting(scope, BooleanBinding);
		if (exceptionArgument != null) {
			TypeBinding exceptionArgumentTB = exceptionArgument.resolveType(scope);
			exceptionArgument.implicitConversion = (exceptionArgumentTB.id << 4) + exceptionArgumentTB.id;
		}
	}
	
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			assertExpression.traverse(visitor, scope);
			if (exceptionArgument != null) {
				exceptionArgument.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}	
	
	public void manageSyntheticAccessIfNecessary(BlockScope currentScope) {
		ClassScope outerMostClassScope = currentScope.outerMostClassScope();
		SourceTypeBinding sourceTypeBinding = outerMostClassScope.enclosingSourceType();
		this.assertionSyntheticFieldBinding = sourceTypeBinding.addSyntheticField(this, currentScope);
		TypeDeclaration typeDeclaration = outerMostClassScope.referenceType();
		AbstractMethodDeclaration[] methods = typeDeclaration.methods;
		Clinit clinit = null;
		for (int i = 0, max = methods.length; i < max; i++) {
			AbstractMethodDeclaration method = methods[i];
			if (method.isClinit()) {
				// this is the clinit
				clinit = (Clinit) method;
			}
		}
		if (clinit != null) {
			// should always be the case
			clinit.addSupportForAssertion(assertionSyntheticFieldBinding);
		}
	}
}