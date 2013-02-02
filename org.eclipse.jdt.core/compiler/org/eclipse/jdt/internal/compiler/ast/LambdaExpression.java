/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper S Moller - Contributions for
 *							bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expression
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class LambdaExpression extends FunctionalExpression implements ProblemSeverities, ReferenceContext {
	public Argument [] arguments;
	Statement body;
	private MethodScope scope;
	private CompilationResult compilationResult;
	private boolean ignoreFurtherInvestigation;
	private MethodBinding binding;
	
	public LambdaExpression(CompilationResult compilationResult, Argument [] arguments, Statement body) {
		this.compilationResult = compilationResult;
		this.arguments = arguments;
		this.body = body;
	}
	
	public TypeBinding resolveType(BlockScope blockScope) {
		super.resolveType(blockScope);
		this.scope = new MethodScope(blockScope, this, blockScope.methodScope().isStatic);
		this.binding = this.scope.createAnonymousMethodBinding(this);
		if (this.functionalInterfaceType.isValidBinding()) {
			this.binding.thrownExceptions = computeKosherThrowables();
			// Resolve arguments, validate signature ...
			if (this.arguments != null && this.singleAbstractMethod != null) {
				int parameterCount = this.singleAbstractMethod.parameters != null ? this.singleAbstractMethod.parameters.length : 0;
				int lambdaArgumentCount = this.arguments != null ? this.arguments.length : 0;

				if (parameterCount == lambdaArgumentCount) {
					for (int i = 0, length = this.arguments.length; i < length; i++) {
						Argument argument = this.arguments[i];
						if (argument.type != null) {
							argument.resolve(this.scope); // TODO: Check it!
						} else {
							argument.bind(this.scope, this.singleAbstractMethod.parameters[i], false);
						}
					}
				} /* TODO: else complain */
			}
		}
		if (this.body instanceof Expression) {
			Expression expression = (Expression) this.body;
			if (this.functionalInterfaceType.isValidBinding()) {
				expression.setExpectedType(this.singleAbstractMethod.returnType); // chain expected type for any nested lambdas.
				/* TypeBinding expressionType = */ expression.resolveType(this.scope);
				// TODO: checkExpressionResult(singleAbstractMethod.returnType, expression, expressionType);
			}
		} else {
			this.body.resolve(this.scope);
		}
		return this.functionalInterfaceType;
	}
	
	private ReferenceBinding[] computeKosherThrowables() {
		return this.singleAbstractMethod == null || !this.singleAbstractMethod.isValidBinding() ? Binding.NO_EXCEPTIONS : this.singleAbstractMethod.thrownExceptions; // for now.
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, final FlowInfo flowInfo) {
		
		if (this.ignoreFurtherInvestigation) 
			return flowInfo;
		
		FlowInfo lambdaInfo = flowInfo.copy(); // occurrence context immune to data flow inside lambda.
		ExceptionHandlingFlowContext methodContext =
				new ExceptionHandlingFlowContext(
						flowContext,
						this,
						this.binding.thrownExceptions,
						null,
						this.scope,
						FlowInfo.DEAD_END);

		// nullity and mark as assigned
		AbstractMethodDeclaration.analyseArguments(lambdaInfo, this.arguments, this.binding);

		if (this.arguments != null) {
			for (int i = 0, count = this.arguments.length; i < count; i++) {
				this.bits |= (this.arguments[i].bits & ASTNode.HasTypeAnnotations);
			}
		}
		
		lambdaInfo = this.body.analyseCode(this.scope, methodContext, lambdaInfo);
		
		// check for missing returning path for block body's ...
		if (this.body instanceof Block) {
			TypeBinding returnTypeBinding = expectedResultType();
			if ((returnTypeBinding == TypeBinding.VOID)) {
				if ((lambdaInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0) {
					this.bits |= ASTNode.NeedFreeReturn;
				}
			} else {
				if (lambdaInfo != FlowInfo.DEAD_END) {
					this.scope.problemReporter().shouldReturn(returnTypeBinding, this);
				}
			}
		}
		return flowInfo;
	}

	public StringBuffer printExpression(int tab, StringBuffer output) {
		int parenthesesCount = (this.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT;
		String suffix = ""; //$NON-NLS-1$
		for(int i = 0; i < parenthesesCount; i++) {
			output.append('(');
			suffix += ')';
		}
		output.append('(');
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.arguments[i].print(0, output);
			}
		}
		output.append(") -> " ); //$NON-NLS-1$
		this.body.print(this.body instanceof Block ? tab : 0, output);
		return output.append(suffix);
	}

	public CompilationResult compilationResult() {
		return this.compilationResult;
	}
	
	public void abort(int abortLevel, CategorizedProblem problem) {

		switch (abortLevel) {
			case AbortCompilation :
				throw new AbortCompilation(this.compilationResult, problem);
			case AbortCompilationUnit :
				throw new AbortCompilationUnit(this.compilationResult, problem);
			case AbortType :
				throw new AbortType(this.compilationResult, problem);
			default :
				throw new AbortMethod(this.compilationResult, problem);
		}
	}

	public CompilationUnitDeclaration getCompilationUnitDeclaration() {
		return this.scope == null ? null : this.scope.compilationUnitScope().referenceContext;
	}

	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	public void tagAsHavingErrors() {
		this.ignoreFurtherInvestigation = true;
	}

	public TypeBinding expectedResultType() {
		return this.singleAbstractMethod != null && this.singleAbstractMethod.isValidBinding() ? this.singleAbstractMethod.returnType : null;
	}
	
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

			if (visitor.visit(this, blockScope)) {
				if (this.arguments != null) {
					int argumentsLength = this.arguments.length;
					for (int i = 0; i < argumentsLength; i++)
						this.arguments[i].traverse(visitor, this.scope);
				}

				if (this.body != null) {
					this.body.traverse(visitor, this.scope);
				}
			}
			visitor.endVisit(this, blockScope);
	}
}