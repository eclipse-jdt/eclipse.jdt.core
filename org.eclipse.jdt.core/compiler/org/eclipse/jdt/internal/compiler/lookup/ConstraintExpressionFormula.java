/*******************************************************************************
 * Copyright (c) 2013 GK Software AG.
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
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ExpressionContext;
import org.eclipse.jdt.internal.compiler.ast.FunctionalExpression;
import org.eclipse.jdt.internal.compiler.ast.Invocation;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18.InvocationRecord;

/**
 * Implementation of 18.1.2 in JLS8, case:
 * <ul>
 * <li>Expression -> T</li>
 * </ul>
 */
class ConstraintExpressionFormula extends ConstraintFormula {
	Expression left;

	// this flag contributes to the workaround controlled by InferenceContext18.ARGUMENT_CONSTRAINTS_ARE_SOFT:
	boolean isSoft;

	ConstraintExpressionFormula(Expression expression, TypeBinding type, int relation) {
		this.left = expression;
		this.right = type;
		this.relation = relation;
	}
	
	ConstraintExpressionFormula(Expression expression, TypeBinding type, int relation, boolean isSoft) {
		this(expression, type, relation);
		this.isSoft = isSoft;
	}

	public Object reduce(InferenceContext18 inferenceContext) throws InferenceFailureException {
		// JLS 18.2.1
		if (this.right.isProperType(true)) {
			TypeBinding exprType = this.left.resolvedType;
			if (exprType == null) {
				if (this.left instanceof FunctionalExpression)
					return this.left.isCompatibleWith(this.right, inferenceContext.scope) ? TRUE : FALSE;
				return FALSE;
			} else if (!exprType.isValidBinding()) {
				return FALSE;
			}
			if (isCompatibleWithInLooseInvocationContext(exprType, this.right, inferenceContext))
				return TRUE;
			return FALSE;
		}
		if (!canBePolyExpression(this.left)) {
			TypeBinding exprType = this.left.resolvedType;
			if (exprType == null || !exprType.isValidBinding())
				return FALSE;
			return new ConstraintTypeFormula(exprType, this.right, COMPATIBLE, this.isSoft);
		} else {
			// shapes of poly expressions (18.2.1)
			// - parenthesized expression : these are transparent in our AST
			if (this.left instanceof Invocation) {
				Invocation invocation = (Invocation) this.left;
				// ignore previous (inner) inference result and do a fresh start:
				MethodBinding method = invocation.binding().original();
				InvocationRecord prevInvocation = inferenceContext.enterPolyInvocation(invocation, invocation.arguments());

				// Invocation Applicability Inference: 18.5.1
				try {
					Expression[] arguments = invocation.arguments();
					TypeBinding[] argumentTypes = arguments == null ? Binding.NO_PARAMETERS : new TypeBinding[arguments.length];
					for (int i = 0; i < argumentTypes.length; i++)
						argumentTypes[i] = arguments[i].resolvedType;
					int checkType = (invocation.inferenceKind() != 0) ? invocation.inferenceKind() : InferenceContext18.CHECK_LOOSE;
					boolean isDiamond = method.isConstructor() && this.left.isPolyExpression(method);
					inferInvocationApplicability(inferenceContext, method, argumentTypes, isDiamond, checkType); // FIXME 3 phases?
					
					if (!inferPolyInvocationType(inferenceContext, invocation, this.right, method))
						return FALSE;
					return null; // already incorporated
				} finally {
					inferenceContext.leavePolyInvocation(prevInvocation);
				}
			} else if (this.left instanceof ConditionalExpression) {
				ConditionalExpression conditional = (ConditionalExpression) this.left;
				return new ConstraintFormula[] {
					new ConstraintExpressionFormula(conditional.valueIfTrue, this.right, this.relation, this.isSoft),
					new ConstraintExpressionFormula(conditional.valueIfFalse, this.right, this.relation, this.isSoft)
				};
			} else if (this.left instanceof LambdaExpression) {
				LambdaExpression lambda = (LambdaExpression) this.left;
				Scope scope = inferenceContext.scope;
				TypeBinding t = this.right;
				if (!t.isFunctionalInterface(scope))
					return FALSE;
				MethodBinding functionType = t.getSingleAbstractMethod(scope, true);
				if (functionType == null)
					return FALSE;
				TypeBinding[] parameters = functionType.parameters;
				if (parameters.length != lambda.arguments().length)
					return FALSE;
				if (lambda.argumentsTypeElided())
					for (int i = 0; i < parameters.length; i++)
						if (!parameters[i].isProperType(true))
							return FALSE;
				lambda = lambda.getResolvedCopyForInferenceTargeting(t);
				if (lambda == null)
					return FALSE; // not strictly unreduceable, but proceeding with TRUE would likely produce secondary errors
				if (functionType.returnType == TypeBinding.VOID) {
					if (!lambda.isVoidCompatible())
						return FALSE;
				} else {
					if (!lambda.isValueCompatible())
						return FALSE;
				}
				List result = new ArrayList();
				if (!lambda.argumentsTypeElided()) {
					Argument[] arguments = lambda.arguments();
					for (int i = 0; i < parameters.length; i++)
						result.add(new ConstraintTypeFormula(parameters[i], arguments[i].type.resolveType(lambda.enclosingScope), SAME));
				}
				if (functionType.returnType != TypeBinding.VOID) {
					TypeBinding r = functionType.returnType;
					if (lambda.body() instanceof Expression) {
						Expression body = (Expression)lambda.body();
						// before introducing the body into inference, we must ensure it's resolved, hm...
						ensureResolved(lambda.enclosingScope, body, r);
						result.add(new ConstraintExpressionFormula(body, r, COMPATIBLE));
					} else {
						Expression[] exprs = lambda.resultExpressions();
						for (int i = 0; i < exprs.length; i++) {
							// before introducing result expressions into inference, we must ensure they're resolved, hm...
							ensureResolved(lambda.enclosingScope, exprs[i], r);
							result.add(new ConstraintExpressionFormula(exprs[i], r, COMPATIBLE));
						}
					}
				}
				if (result.size() == 0)
					return TRUE;
				return result.toArray(new ConstraintFormula[result.size()]);
			} else if (this.left instanceof ReferenceExpression) {
				return reduceReferenceExpressionCompatibility((ReferenceExpression) this.left, inferenceContext);
			}
		}
		return FALSE;
	}

	private boolean canBePolyExpression(Expression expr) {
		// when inferring compatibility against a right type, the check isPolyExpression 
		// must assume that expr occurs in s.t. like an assignment context:
		ExpressionContext previousExpressionContext = expr.getExpressionContext();
		if (previousExpressionContext == ExpressionContext.VANILLA_CONTEXT)
			this.left.setExpressionContext(ExpressionContext.ASSIGNMENT_CONTEXT);
		try {
			return expr.isPolyExpression();
		} finally {
			expr.setExpressionContext(previousExpressionContext);
		}
	}

	private void ensureResolved(BlockScope scope, Expression expr, TypeBinding targetType) {
		// TODO this method might be obsoleted by the use of LE.getResolvedCopyForInferenceTargeting()
		if (expr.resolvedType == null) {
			if (targetType.isProperType(true))
				expr.setExpectedType(targetType);
			else
				expr.setExpectedType(null);
			ExpressionContext previousExpressionContext = expr.getExpressionContext();
			if (previousExpressionContext == ExpressionContext.VANILLA_CONTEXT)
				expr.setExpressionContext(ExpressionContext.ASSIGNMENT_CONTEXT);
			try {
				expr.resolveType(scope);
			} finally {
				expr.setExpressionContext(previousExpressionContext);
			}
		}
	}

	private Object reduceReferenceExpressionCompatibility(ReferenceExpression reference, InferenceContext18 inferenceContext) {
		TypeBinding t = this.right;
		if (t.isProperType(true))
			throw new IllegalStateException("Should not reach here with T being a proper type"); //$NON-NLS-1$
		if (!t.isFunctionalInterface(inferenceContext.scope))
			return FALSE;
		MethodBinding functionType = t.getSingleAbstractMethod(inferenceContext.scope, true);
		if (functionType == null)
			return FALSE;
		// potentially-applicable method for the method reference when targeting T (15.28.1),
		MethodBinding potentiallyApplicable = reference.findCompileTimeMethodTargeting(t, inferenceContext.scope);
		if (potentiallyApplicable == null)
			return FALSE;
		if (reference.isExactMethodReference()) {
			List /*<ConstraintFormula>*/ newConstraints = new ArrayList();
			TypeBinding[] p = functionType.parameters;
			int n = p.length;
			TypeBinding[] pPrime = potentiallyApplicable.parameters;
			int k = pPrime.length;
			int offset = 0;
			if (n == k+1) {
				newConstraints.add(new ConstraintTypeFormula(p[0], reference.lhs.resolvedType, COMPATIBLE));
				offset = 1;
			}
			for (int i = offset; i < n; i++)
				newConstraints.add(new ConstraintTypeFormula(p[i], pPrime[i-offset], COMPATIBLE));
			TypeBinding r = functionType.returnType;
			if (r != TypeBinding.VOID) {
				TypeBinding rAppl = potentiallyApplicable.isConstructor() ? potentiallyApplicable.declaringClass : potentiallyApplicable.returnType;
				if (rAppl == TypeBinding.VOID)
					return FALSE;
				TypeBinding rPrime = rAppl.capture(inferenceContext.scope, 14); // FIXME capture position??
				newConstraints.add(new ConstraintTypeFormula(rPrime, r, COMPATIBLE));
			}
			return newConstraints.toArray(new ConstraintFormula[newConstraints.size()]);
		} else { // inexact
			int n = functionType.parameters.length;
			for (int i = 0; i < n; i++)
				if (!functionType.parameters[i].isProperType(true))
					return FALSE;
			InferenceContext18.missingImplementation("NYI: inexact method reference"); //$NON-NLS-1$
			// FIXME: Otherwise, a search for a compile-time declaration is performed, as defined in 15.28.1 .....
		}
		return FALSE;
	}

	static void inferInvocationApplicability(InferenceContext18 inferenceContext, MethodBinding method, TypeBinding[] arguments, boolean isDiamond, int checkType) 
	{
		// 18.5.1
		TypeVariableBinding[] typeVariables = method.typeVariables;
		if (isDiamond) {
			TypeVariableBinding[] classTypeVariables = method.declaringClass.typeVariables();
			int l1 = typeVariables.length;
			int l2 = classTypeVariables.length;
			if (l1 == 0) {
				typeVariables = classTypeVariables;
			} else if (l2 != 0) {
				System.arraycopy(typeVariables, 0, typeVariables=new TypeVariableBinding[l1+l2], 0, l1);
				System.arraycopy(classTypeVariables, 0, typeVariables, l1, l2);
			}				
		}
		TypeBinding[] parameters = method.parameters;
		InferenceVariable[] inferenceVariables = inferenceContext.createInitialBoundSet(typeVariables); // creates initial bound set B

		// check if varargs need special treatment:
		int paramLength = method.parameters.length;
		TypeBinding varArgsType = null;
		if (method.isVarargs()) {
			int varArgPos = paramLength-1;
			varArgsType = method.parameters[varArgPos];
		}
		inferenceContext.createInitialConstraintsForParameters(parameters, checkType==InferenceContext18.CHECK_VARARG, varArgsType, method);
		inferenceContext.addThrowsContraints(typeVariables, inferenceVariables, method.thrownExceptions);
	}

	static boolean inferPolyInvocationType(InferenceContext18 inferenceContext, InvocationSite invocationSite, TypeBinding targetType, MethodBinding method) 
				throws InferenceFailureException 
	{
		TypeBinding[] typeArguments = invocationSite.genericTypeArguments();
		if (typeArguments == null) {
			// invocation type inference (18.5.2):
			TypeBinding returnType = method.isConstructor() ? method.declaringClass : method.returnType;
			if (returnType == TypeBinding.VOID)
				throw new InferenceFailureException("expression has no value"); //$NON-NLS-1$

			ParameterizedTypeBinding parameterizedType = InferenceContext18.parameterizedWithWildcard(returnType);
			if (parameterizedType != null) {
				TypeBinding[] arguments = parameterizedType.arguments;
				InferenceVariable[] betas = inferenceContext.addTypeVariableSubstitutions(arguments);
				TypeBinding gbeta = inferenceContext.environment.createParameterizedType(
						parameterizedType.genericType(), betas, parameterizedType.enclosingType(), parameterizedType.getTypeAnnotations());
				inferenceContext.currentBounds.captures.put(gbeta, parameterizedType);
				ConstraintTypeFormula newConstraint = new ConstraintTypeFormula(gbeta, targetType, COMPATIBLE);
				if (!inferenceContext.reduceAndIncorporate(newConstraint))
					return false;
			}

			if (targetType.isBaseType()) {
				TypeBinding thetaR = inferenceContext.substitute(returnType);
				if (thetaR instanceof InferenceVariable) {
					TypeBinding wrapper = inferenceContext.currentBounds.findWrapperTypeBound((InferenceVariable)thetaR);
					if (wrapper != null) {
						if (!inferenceContext.reduceAndIncorporate(new ConstraintTypeFormula(thetaR, wrapper, ReductionResult.SAME))
							|| !inferenceContext.reduceAndIncorporate(new ConstraintTypeFormula(wrapper, targetType, ReductionResult.COMPATIBLE)))
							return false;
					}
				}
			}

			ConstraintTypeFormula newConstraint = new ConstraintTypeFormula(inferenceContext.substitute(returnType), targetType, COMPATIBLE);
			if (!inferenceContext.reduceAndIncorporate(newConstraint))
				return false;
		}
		return true;
	}

	Collection inputVariables(final InferenceContext18 context) {
		// from 18.5.2.
		if (this.left instanceof LambdaExpression) {
			if (this.right instanceof InferenceVariable) {
				return Collections.singletonList(this.right);
			}
			if (this.right.isFunctionalInterface(context.scope)) {
				LambdaExpression lambda = (LambdaExpression) this.left;
				MethodBinding sam = this.right.getSingleAbstractMethod(context.scope, true); // TODO derive with target type?
				final Set variables = new HashSet();
				if (lambda.argumentsTypeElided()) {
					// i)
					int len = sam.parameters.length;
					for (int i = 0; i < len; i++) {
						sam.parameters[i].collectInferenceVariables(variables);
					}
				}
				if (sam.returnType != TypeBinding.VOID) {
					// ii)
					final TypeBinding r = sam.returnType;
					Statement body = lambda.body();
					if (body instanceof Expression) {
						variables.addAll(new ConstraintExpressionFormula((Expression) body, r, COMPATIBLE).inputVariables(context));
					} else {
						// TODO: should I use LambdaExpression.resultExpressions? (is currently private).
						body.traverse(new ASTVisitor() {
							public boolean visit(ReturnStatement returnStatement, BlockScope scope) {
								variables.addAll(new ConstraintExpressionFormula(returnStatement.expression, r, COMPATIBLE).inputVariables(context));
								return false;
							}
						}, (BlockScope)null);
					}
				}
				return variables;
			}
		} else if (this.left instanceof ReferenceExpression) {
			if (this.right instanceof InferenceVariable) {
				return Collections.singletonList(this.right);
			}
			if (this.right.isFunctionalInterface(context.scope) && !this.left.isExactMethodReference()) {
				MethodBinding sam = this.right.getSingleAbstractMethod(context.scope, true);
				final Set variables = new HashSet();
				int len = sam.parameters.length;
				for (int i = 0; i < len; i++) {
					sam.parameters[i].collectInferenceVariables(variables);
				}
				return variables;
			}			
		} else if (this.left instanceof ConditionalExpression && this.left.isPolyExpression()) {
			ConditionalExpression expr = (ConditionalExpression) this.left;
			Set variables = new HashSet();
			variables.addAll(new ConstraintExpressionFormula(expr.valueIfTrue, this.right, COMPATIBLE).inputVariables(context));
			variables.addAll(new ConstraintExpressionFormula(expr.valueIfFalse, this.right, COMPATIBLE).inputVariables(context));
			return variables;
		}
		return EMPTY_VARIABLE_LIST;
	}

	// debugging:
	public String toString() {
		StringBuffer buf = new StringBuffer().append(LEFT_ANGLE_BRACKET);
		this.left.printExpression(4, buf);
		buf.append(relationToString(this.relation));
		appendTypeName(buf, this.right);
		buf.append(RIGHT_ANGLE_BRACKET);
		return buf.toString();
	}
}
