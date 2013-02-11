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
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class LambdaExpression extends FunctionalExpression implements ProblemSeverities, ReferenceContext {
	public Argument [] arguments;
	public Statement body;
	private MethodScope scope;
	private CompilationResult compilationResult;
	private boolean ignoreFurtherInvestigation;
	private MethodBinding binding;
	
	public LambdaExpression(CompilationResult compilationResult, Argument [] arguments, Statement body) {
		this.compilationResult = compilationResult;
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

	/* This code is arranged so that we can continue with as much analysis as possible while avoiding 
	 * mine fields that would result in a slew of spurious messages. This method is a merger of:
	 * @see org.eclipse.jdt.internal.compiler.lookup.MethodScope.createMethod(AbstractMethodDeclaration)
	 * @see org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding.resolveTypesFor(MethodBinding)
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration.resolve(ClassScope)
	 */
	public TypeBinding resolveType(BlockScope blockScope) {
		
		super.resolveType(blockScope); // compute & capture interface function descriptor in singleAbstractMethod.
		
		final boolean argumentsTypeElided = argumentsTypeElided();
		final boolean haveDescriptor = this.descriptor != null;
		
		if (haveDescriptor && this.descriptor.typeVariables != Binding.NO_TYPE_VARIABLES) {
			blockScope.problemReporter().lambdaExpressionCannotImplementGenericMethod(this, this.descriptor);
			return this.resolvedType = null;
		}
		if (!haveDescriptor && argumentsTypeElided) 
			return null; // FUBAR, bail out...

		this.scope = new MethodScope(blockScope, this, blockScope.methodScope().isStatic);
		
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
		Scope parent = this.scope.parent;
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
}