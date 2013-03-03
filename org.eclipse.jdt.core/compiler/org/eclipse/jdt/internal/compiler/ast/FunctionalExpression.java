/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBindingVisitor;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public abstract class FunctionalExpression extends Expression implements ProblemSeverities, ReferenceContext {
	
	TypeBinding expectedType;
	MethodBinding descriptor;
	public MethodBinding binding;
	boolean ignoreFurtherInvestigation;
	protected ExpressionContext expressionContext = VANILLA_CONTEXT;
	protected CompilationResult compilationResult;
	protected BlockScope enclosingScope;
	protected boolean ellipsisArgument;
	protected static IErrorHandlingPolicy silentErrorHandlingPolicy = DefaultErrorHandlingPolicies.ignoreAllProblems();
	
	public FunctionalExpression(CompilationResult compilationResult) {
		this.compilationResult = compilationResult;
	}

	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = this.ellipsisArgument ? ((ArrayBinding) expectedType).elementsType() : expectedType;
	}
	
	public void setExpressionContext(ExpressionContext context) {
		this.expressionContext = context;
	}
	
	public void tagAsEllipsisArgument() {
		this.ellipsisArgument = true;
	}
	public boolean isPolyExpression() {
		return this.expressionContext != VANILLA_CONTEXT;
	}

	public TypeBinding expectedType() {
		return this.expectedType;
	}
	
	public TypeBinding resolveType(BlockScope blockScope) {
		this.constant = Constant.NotAConstant;
		MethodBinding sam = this.expectedType == null ? null : this.expectedType.getSingleAbstractMethod(blockScope);
		if (sam == null) {
			blockScope.problemReporter().targetTypeIsNotAFunctionalInterface(this);
			return null;
		}
		if (!sam.isValidBinding()) {
			switch (sam.problemId()) {
				case ProblemReasons.NoSuchSingleAbstractMethod:
					blockScope.problemReporter().targetTypeIsNotAFunctionalInterface(this);
					break;
				case ProblemReasons.NotAWellFormedParameterizedType:
					blockScope.problemReporter().illFormedParameterizationOfFunctionalInterface(this);
					break;
				case ProblemReasons.IntersectionHasMultipleFunctionalInterfaces:
					blockScope.problemReporter().multipleFunctionalInterfaces(this);
					break;
			}
			return null;
		}
		
		this.descriptor = sam;
		if (kosherDescriptor(blockScope, sam, true)) {
			return this.resolvedType = this.expectedType;		
		}
		
		return this.resolvedType = null;
	}

	class VisibilityInspector extends TypeBindingVisitor {

		private Scope scope;
		private boolean shouldChatter;
        private boolean visible = true;
		private FunctionalExpression expression;
        
		public VisibilityInspector(FunctionalExpression expression, Scope scope, boolean shouldChatter) {
			this.scope = scope;
			this.shouldChatter = shouldChatter;
			this.expression = expression;
		}

		private void checkVisibility(ReferenceBinding referenceBinding) {
			if (!referenceBinding.canBeSeenBy(this.scope)) {
				this.visible = false;
				if (this.shouldChatter)
					this.scope.problemReporter().descriptorHasInvisibleType(this.expression, referenceBinding);
			}
		}
		
		public boolean visit(ReferenceBinding referenceBinding) {
			checkVisibility(referenceBinding);
			return true;
		}

		
		public boolean visit(ParameterizedTypeBinding parameterizedTypeBinding) {
			checkVisibility(parameterizedTypeBinding);
			return true;
		}
		
		public boolean visit(RawTypeBinding rawTypeBinding) {
			checkVisibility(rawTypeBinding);
			return true;
		}

		public boolean visible(TypeBinding type) {
			TypeBindingVisitor.visit(this, type);
			return this.visible;
		}

		public boolean visible(TypeBinding[] types) {
			TypeBindingVisitor.visit(this, types);
			return this.visible;
		}
		
	}

	public boolean kosherDescriptor(Scope scope, MethodBinding sam, boolean shouldChatter) {
	
		VisibilityInspector inspector = new VisibilityInspector(this, scope, shouldChatter);
		
		boolean status = true;
		
		if (!inspector.visible(sam.returnType))
			status = false;
		if (!inspector.visible(sam.parameters))
			status = false;
		if (!inspector.visible(sam.thrownExceptions))
			status = false;
		
		return status;
	}

	public int nullStatus(FlowInfo flowInfo) {
		return FlowInfo.NON_NULL;
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired) {
			codeStream.aconst_null(); // TODO: Real code
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
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
		return this.enclosingScope == null ? null : this.enclosingScope.compilationUnitScope().referenceContext;
	}

	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	public void tagAsHavingErrors() {
		this.ignoreFurtherInvestigation = true;
		Scope parent = this.enclosingScope.parent;
		while (parent != null) {
			switch(parent.kind) {
				case Scope.CLASS_SCOPE:
				case Scope.METHOD_SCOPE:
					parent.referenceContext().tagAsHavingErrors();
					return;
				default:
					parent = parent.parent;
					break;
			}
		}
	}
	
	protected boolean shapeAnalysisComplete() {
		return true;
	}
	
	public void tagAsHavingIgnoredMandatoryErrors(int problemId) {
		// 15.27.3 requires exception throw related errors to not influence congruence. Also don't abort shape analysis.
		if (shapeAnalysisComplete()) {
			switch (problemId) {
				case IProblem.UnhandledExceptionOnAutoClose:
				case IProblem.UnhandledExceptionInDefaultConstructor:
				case IProblem.UnhandledException:
					return;
				default: 
					throw new IncongruentLambdaException();
			}
		}
	}

	
}

class IncongruentLambdaException extends RuntimeException {
	private static final long serialVersionUID = 4145723509219836114L;
}
