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
 *							bug 382721 - [1.8][compiler] Effectively final variables needs special treatment
 *     Stephan Herrmann - Contribution for
 *							bug 401030 - [1.8][null] Null analysis support for lambda methods.
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class LambdaExpression extends FunctionalExpression {
	public Argument [] arguments;
	public Statement body;
	public boolean hasParentheses;
	MethodScope scope;
	protected boolean voidCompatible = true;
	protected boolean valueCompatible = false;
	protected boolean shapeAnalysisComplete = true;
	
	public LambdaExpression(CompilationResult compilationResult, Argument [] arguments, Statement body) {
		super(compilationResult);
		this.arguments = arguments != null ? arguments : ASTNode.NO_ARGUMENTS;
		this.body = body;
	}
	
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		if (this.ignoreFurtherInvestigation) {
			return;
		}
		super.generateCode(currentScope, codeStream, valueRequired);
		this.body.generateCode(this.scope, codeStream);
	}

	public boolean kosherDescriptor(Scope currentScope, MethodBinding sam, boolean shouldChatter) {
		if (sam.typeVariables != Binding.NO_TYPE_VARIABLES) {
			if (shouldChatter)
				currentScope.problemReporter().lambdaExpressionCannotImplementGenericMethod(this, sam);
			return false;
		}
		return super.kosherDescriptor(currentScope, sam, shouldChatter);
	}
	
	/* This code is arranged so that we can continue with as much analysis as possible while avoiding 
	 * mine fields that would result in a slew of spurious messages. This method is a merger of:
	 * @see org.eclipse.jdt.internal.compiler.lookup.MethodScope.createMethod(AbstractMethodDeclaration)
	 * @see org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding.resolveTypesFor(MethodBinding)
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration.resolve(ClassScope)
	 */
	public TypeBinding resolveType(BlockScope blockScope) {
		
		this.constant = Constant.NotAConstant;
		this.enclosingScope = blockScope;
		this.scope = new MethodScope(blockScope, this, blockScope.methodScope().isStatic);
		
		if (this.expectedType == null && this.expressionContext == INVOCATION_CONTEXT) {
			if (this.body instanceof Block) {
				// Gather shape information for potential applicability analysis.
				ASTVisitor visitor = new ASTVisitor() {
					private boolean valueReturnSeen = false;
					private boolean voidReturnSeen = false;
					private boolean throwSeen = false;
					public boolean visit(ReturnStatement returnStatement, BlockScope dontCare) {
						if (returnStatement.expression != null) {
							this.valueReturnSeen = true;
							LambdaExpression.this.voidCompatible = false;
							LambdaExpression.this.valueCompatible = !this.voidReturnSeen;
						} else {
							this.voidReturnSeen = true;
							LambdaExpression.this.valueCompatible = false;
							LambdaExpression.this.voidCompatible = !this.valueReturnSeen;
						}
						return false;
					}
					public boolean visit(ThrowStatement throwStatement, BlockScope dontCare) {
						this.throwSeen  = true;
						return false;
					}
					public void endVisit(LambdaExpression expression, BlockScope dontCare) {
						if (!this.voidReturnSeen && !this.valueReturnSeen && this.throwSeen) {  // () -> { throw new Exception(); } is value compatible.
							Block block = (Block) LambdaExpression.this.body;
							final Statement[] statements = block.statements;
							final int statementsLength = statements == null ? 0 : statements.length;
							Statement ultimateStatement = statementsLength == 0 ? null : statements[statementsLength - 1];
							LambdaExpression.this.valueCompatible = ultimateStatement instanceof ThrowStatement;
							LambdaExpression.this.shapeAnalysisComplete = LambdaExpression.this.valueCompatible;
						}
					}
				};
				this.traverse(visitor, blockScope);
			} else {
				Expression expression = (Expression) this.body;
				this.voidCompatible = expression.statementExpression();
				this.valueCompatible = true;
			}	
			return new PolyTypeBinding(this);
		}
		super.resolveType(blockScope); // compute & capture interface function descriptor in singleAbstractMethod.
		
		final boolean argumentsTypeElided = argumentsTypeElided();
		final boolean haveDescriptor = this.descriptor != null;
		
		if (haveDescriptor && this.descriptor.typeVariables != Binding.NO_TYPE_VARIABLES) // already complained in kosher*
			return null;
		
		if (!haveDescriptor && argumentsTypeElided) 
			return null; // FUBAR, bail out...

		this.binding = new MethodBinding(ClassFileConstants.AccPublic | ExtraCompilerModifiers.AccUnresolved,
							haveDescriptor ? this.descriptor.selector : TypeConstants.ANONYMOUS_METHOD, 
							haveDescriptor ? this.descriptor.returnType : null, 
							Binding.NO_PARAMETERS, // for now. 
							haveDescriptor ? this.descriptor.thrownExceptions : Binding.NO_EXCEPTIONS, 
							blockScope.enclosingSourceType()); // declaring class, for now - this is needed for annotation holder and such.
		this.binding.typeVariables = Binding.NO_TYPE_VARIABLES;
		
		if (haveDescriptor) {
			int descriptorParameterCount = this.descriptor.parameters.length;
			int lambdaArgumentCount = this.arguments != null ? this.arguments.length : 0;
            if (descriptorParameterCount != lambdaArgumentCount) {
            	this.scope.problemReporter().lambdaSignatureMismatched(this);
            	if (argumentsTypeElided) 
            		return null; // FUBAR, bail out ...
            }
		}
		
		boolean buggyArguments = false;
		int length = this.arguments == null ? 0 : this.arguments.length;
		TypeBinding[] newParameters = new TypeBinding[length];

		AnnotationBinding [][] parameterAnnotations = null;
		for (int i = 0; i < length; i++) {
			Argument argument = this.arguments[i];
			if (argument.isVarArgs()) {
				if (i == length - 1) {
					this.binding.modifiers |= ClassFileConstants.AccVarargs;
				} else {
					this.scope.problemReporter().illegalVarargInLambda(argument);
					buggyArguments = true;
				}
			}
			
			TypeBinding parameterType;
			final TypeBinding expectedParameterType = haveDescriptor && i < this.descriptor.parameters.length ? this.descriptor.parameters[i] : null;
			parameterType = argumentsTypeElided ? expectedParameterType : argument.type.resolveType(this.scope, true /* check bounds*/);
			if (parameterType == null) {
				buggyArguments = true;
			} else if (parameterType == TypeBinding.VOID) {
				this.scope.problemReporter().argumentTypeCannotBeVoid(this, argument);
				buggyArguments = true;
			} else {
				if (!parameterType.isValidBinding()) {
					this.binding.tagBits |= TagBits.HasUnresolvedArguments;
				}
				if ((parameterType.tagBits & TagBits.HasMissingType) != 0) {
					this.binding.tagBits |= TagBits.HasMissingType;
				}
				if (haveDescriptor && expectedParameterType != null && parameterType.isValidBinding() && parameterType != expectedParameterType) {
					this.scope.problemReporter().lambdaParameterTypeMismatched(argument, argument.type, expectedParameterType);
				}

				TypeBinding leafType = parameterType.leafComponentType();
				if (leafType instanceof ReferenceBinding && (((ReferenceBinding) leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
					this.binding.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
				newParameters[i] = parameterType;
				argument.bind(this.scope, parameterType, false);
				if (argument.annotations != null) {
					this.binding.tagBits |= TagBits.HasParameterAnnotations;
					if (parameterAnnotations == null) {
						parameterAnnotations = new AnnotationBinding[length][];
						for (int j = 0; j < i; j++) {
							parameterAnnotations[j] = Binding.NO_ANNOTATIONS;
						}
					}
					parameterAnnotations[i] = argument.binding.getAnnotations();
				} else if (parameterAnnotations != null) {
					parameterAnnotations[i] = Binding.NO_ANNOTATIONS;
				}
			}
		}
		// only assign parameters if no problems are found
		if (!buggyArguments) {
			this.binding.parameters = newParameters;
			if (parameterAnnotations != null)
				this.binding.setParameterAnnotations(parameterAnnotations);
		}
	
		if (!argumentsTypeElided && this.binding.isVarargs()) {
			if (!this.binding.parameters[this.binding.parameters.length - 1].isReifiable()) {
				this.scope.problemReporter().possibleHeapPollutionFromVararg(this.arguments[this.arguments.length - 1]);
			}
		}

		ReferenceBinding [] exceptions = this.binding.thrownExceptions;
		length = exceptions.length;
		for (int i = 0; i < length; i++) {
			ReferenceBinding exception = exceptions[i];
			if ((exception.tagBits & TagBits.HasMissingType) != 0) {
				this.binding.tagBits |= TagBits.HasMissingType;
			}
			this.binding.modifiers |= (exception.modifiers & ExtraCompilerModifiers.AccGenericSignature);
		}
		
		TypeBinding returnType = this.binding.returnType;
		if (returnType != null) {
			if ((returnType.tagBits & TagBits.HasMissingType) != 0) {
				this.binding.tagBits |= TagBits.HasMissingType;
			}
			TypeBinding leafType = returnType.leafComponentType();
			if (leafType instanceof ReferenceBinding && (((ReferenceBinding) leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
				this.binding.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
		} // TODO (stephan): else? (can that happen?)

		if (haveDescriptor && blockScope.compilerOptions().isAnnotationBasedNullAnalysisEnabled) {
			if (!argumentsTypeElided) {
				AbstractMethodDeclaration.createArgumentBindings(this.arguments, this.binding, this.scope);
				validateNullAnnotations();
				// no application of null-ness default, hence also no warning regarding redundant null annotation
				mergeParameterNullAnnotations(blockScope);
			}
			this.binding.tagBits |= (this.descriptor.tagBits & TagBits.AnnotationNullMASK);
		}

		this.binding.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
		
		if (this.body instanceof Expression) {
			Expression expression = (Expression) this.body;
			new ReturnStatement(expression, expression.sourceStart, expression.sourceEnd, true).resolve(this.scope); // :-) ;-)
		} else {
			this.body.resolve(this.scope);
		}
		return this.resolvedType;
	}
	
	private boolean argumentsTypeElided() {
		return this.arguments.length > 0 && this.arguments[0].hasElidedType();
	}

	private boolean doesNotCompleteNormally() {
		return this.body.analyseCode(this.scope, 
									 new ExceptionHandlingFlowContext(null, this, Binding.NO_EXCEPTIONS, null, this.scope, FlowInfo.DEAD_END), 
									 FlowInfo.initial(this.scope.referenceType().maxFieldCount)) == FlowInfo.DEAD_END; 
	}
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, final FlowInfo flowInfo) {
		
		if (this.ignoreFurtherInvestigation) 
			return flowInfo;
		
		FlowInfo lambdaInfo = flowInfo.copy(); // what happens in vegas, stays in vegas ...
		ExceptionHandlingFlowContext methodContext =
				new ExceptionHandlingFlowContext(
						flowContext,
						this,
						this.binding.thrownExceptions,
						null,
						this.scope,
						FlowInfo.DEAD_END);

		// nullity and mark as assigned
		MethodBinding methodWithParameterDeclaration = argumentsTypeElided() ? this.descriptor : this.binding;
		AbstractMethodDeclaration.analyseArguments(lambdaInfo, this.arguments, methodWithParameterDeclaration);

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
		} else { // Expression
			if (currentScope.compilerOptions().isAnnotationBasedNullAnalysisEnabled 
					&& flowInfo.reachMode() == FlowInfo.REACHABLE)
			{
				Expression expression = (Expression)this.body;
				checkAgainstNullAnnotation(flowContext, expression, expression.nullStatus(flowInfo, flowContext));
			}
		}
		return flowInfo;
	}

	// cf. AbstractMethodDeclaration.validateNullAnnotations()
	// pre: !argumentTypeElided()
	void validateNullAnnotations() {
		// null annotations on parameters?
		if (this.binding != null && this.binding.parameterNonNullness != null) {
			int length = this.binding.parameters.length;
			for (int i=0; i<length; i++) {
				if (this.binding.parameterNonNullness[i] != null) {
					long nullAnnotationTagBit =  this.binding.parameterNonNullness[i].booleanValue()
							? TagBits.AnnotationNonNull : TagBits.AnnotationNullable;
					this.scope.validateNullAnnotation(nullAnnotationTagBit, this.arguments[i].type, this.arguments[i].annotations);
				}
			}
		}
	}

	// pre: !argumentTypeElided()
	// try to merge null annotations from descriptor into binding, complaining about any incompatibilities found
	private void mergeParameterNullAnnotations(BlockScope currentScope) {
		if (this.descriptor.parameterNonNullness == null)
			return;
		if (this.binding.parameterNonNullness == null) {
			this.binding.parameterNonNullness = this.descriptor.parameterNonNullness;
			return;
		}
		LookupEnvironment env = currentScope.environment();
		Boolean[] ourNonNullness = this.binding.parameterNonNullness;
		Boolean[] descNonNullness = this.descriptor.parameterNonNullness;
		int len = Math.min(ourNonNullness.length, descNonNullness.length);
		for (int i = 0; i < len; i++) {
			if (ourNonNullness[i] == null) {
				ourNonNullness[i] = descNonNullness[i];
			} else if (ourNonNullness[i] != descNonNullness[i]) {
				if (ourNonNullness[i] == Boolean.TRUE) { // requested @NonNull not provided
					char[][] inheritedAnnotationName = null;
					if (descNonNullness[i] == Boolean.FALSE)
						inheritedAnnotationName = env.getNullableAnnotationName();
					currentScope.problemReporter().illegalRedefinitionToNonNullParameter(this.arguments[i], this.descriptor.declaringClass, inheritedAnnotationName);
				}
			}			
		}
	}

	// simplified version of ReturnStatement.checkAgainstNullAnnotation()
	void checkAgainstNullAnnotation(FlowContext flowContext, Expression expression, int nullStatus) {
		if (nullStatus != FlowInfo.NON_NULL) {
			// if we can't prove non-null check against declared null-ness of the descriptor method:
			// Note that this.binding never has a return type declaration, always inherit null-ness from the descriptor
			if ((this.descriptor.tagBits & TagBits.AnnotationNonNull) != 0) {
				flowContext.recordNullityMismatch(this.scope, expression, expression.resolvedType, this.descriptor.returnType, nullStatus);
			}
		}
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

	public TypeBinding expectedResultType() {
		return this.descriptor != null && this.descriptor.isValidBinding() ? this.descriptor.returnType : null;
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
	
	public boolean isCompatibleWith(TypeBinding left, Scope someScope) {
		
		final MethodBinding sam = left.getSingleAbstractMethod(this.enclosingScope);
		
		if (sam == null || !sam.isValidBinding())
			return false;
		if (sam.parameters.length != this.arguments.length)
			return false;
		
		if (this.shapeAnalysisComplete && squarePegInRoundHole(sam))
			return false;
		
		IErrorHandlingPolicy oldPolicy = this.scope.problemReporter().switchErrorHandlingPolicy(silentErrorHandlingPolicy);
		try {
			LambdaExpression copy = copy();
			copy.setExpressionContext(this.expressionContext);
			copy.setExpectedType(left);
			copy.resolveType(this.enclosingScope);
			if (!this.shapeAnalysisComplete) {
				this.valueCompatible = copy.doesNotCompleteNormally();
				this.shapeAnalysisComplete = true;
				if (squarePegInRoundHole(sam))
					return false;
			}
			if (!argumentsTypeElided()) {
				for (int i = 0, length = sam.parameters.length; i < length; i++) {
					TypeBinding argumentType = copy.arguments[i].binding.type;
					if (sam.parameters[i] != argumentType)
						return false;
				}
			}

			try {
				final TypeBinding returnType = sam.returnType;
				if (this.body instanceof Block) {
					ASTVisitor visitor = new ASTVisitor() {
						public boolean visit(ReturnStatement returnStatement, BlockScope blockScope) {
							Expression expression = returnStatement.expression;
							if (expression != null && !expression.isAssignmentCompatible(returnType, blockScope))
								throw new NoncongruentLambdaException();
							return false;
						}
					};
					copy.body.traverse(visitor, copy.scope);
				} else {
					Expression expression = (Expression) copy.body;
					if (!expression.isAssignmentCompatible(returnType, copy.scope))
						throw new NoncongruentLambdaException();
				}
			} catch (NoncongruentLambdaException e) {
				return false;
			}
		} finally {
			this.scope.problemReporter().switchErrorHandlingPolicy(oldPolicy);
		}
		return true;
	}

	private boolean squarePegInRoundHole(final MethodBinding sam) {
		if (sam.returnType.id == TypeIds.T_void) {
			if (!this.voidCompatible)
				return true;
		} else {
			if (!this.valueCompatible)
				return true;
		}
		return false;
	}

	LambdaExpression copy() {
		final Parser parser = new Parser(this.scope.problemReporter(), false);
		final char[] source = this.compilationResult.getCompilationUnit().getContents();
		return (LambdaExpression) parser.parseExpression(source, this.sourceStart, this.sourceEnd - this.sourceStart + 1, 
										this.scope.referenceCompilationUnit(), false /* record line separators */);
	}
}

class NoncongruentLambdaException extends RuntimeException {
	private static final long serialVersionUID = 4145723509219836114L;
}

