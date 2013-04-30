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

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public class LambdaExpression extends FunctionalExpression implements ReferenceContext, ProblemSeverities {
	public Argument [] arguments;
	public Statement body;
	public boolean hasParentheses;
	public MethodScope scope;
	private boolean voidCompatible = true;
	private boolean valueCompatible = false;
	private boolean shapeAnalysisComplete = false;
	private boolean returnsValue;
	private boolean returnsVoid;
	private boolean throwsException;
	private LambdaExpression original = this;
	private SyntheticArgumentBinding[] outerLocalVariables = NO_SYNTHETIC_ARGUMENTS;
	private int outerLocalVariablesSlotSize = 0;
	public boolean shouldCaptureInstance = false;
	private static final SyntheticArgumentBinding [] NO_SYNTHETIC_ARGUMENTS = new SyntheticArgumentBinding[0];
	
	public LambdaExpression(CompilationResult compilationResult, Argument [] arguments, Statement body) {
		super(compilationResult);
		this.arguments = arguments != null ? arguments : ASTNode.NO_ARGUMENTS;
		this.body = body;
	}
	
	protected FunctionalExpression original() {
		return this.original;
	}
	
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		if (this.shouldCaptureInstance) {
			this.binding.modifiers &= ~ClassFileConstants.AccStatic;
		} else {
			this.binding.modifiers |= ClassFileConstants.AccStatic;
		}
		SourceTypeBinding sourceType = currentScope.enclosingSourceType();
		this.binding = sourceType.addSyntheticMethod(this);
		int pc = codeStream.position;
		StringBuffer signature = new StringBuffer();
		signature.append('(');
		if (this.shouldCaptureInstance) {
			codeStream.aload_0();
			signature.append(sourceType.signature());
		}
		for (int i = 0, length = this.outerLocalVariables == null ? 0 : this.outerLocalVariables.length; i < length; i++) {
			SyntheticArgumentBinding syntheticArgument = this.outerLocalVariables[i];
			if (this.shouldCaptureInstance) {
				syntheticArgument.resolvedPosition++;
			}
			signature.append(syntheticArgument.type.signature());
			LocalVariableBinding capturedOuterLocal = syntheticArgument.actualOuterLocalVariable;
			VariableBinding[] path = currentScope.getEmulationPath(capturedOuterLocal);
			codeStream.generateOuterAccess(path, this, capturedOuterLocal, currentScope);
		}
		signature.append(')');
		signature.append(this.expectedType.signature());
		int invokeDynamicNumber = codeStream.classFile.recordBootstrapMethod(this);
		codeStream.invokeDynamic(invokeDynamicNumber, (this.shouldCaptureInstance ? 1 : 0) + this.outerLocalVariablesSlotSize, 1, TypeConstants.ANONYMOUS_METHOD, signature.toString().toCharArray());
		codeStream.recordPositionsFrom(pc, this.sourceStart);		
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
		
		if (this.expectedType == null && this.expressionContext == INVOCATION_CONTEXT) {
			return new PolyTypeBinding(this);
		} 
		
		MethodScope methodScope = blockScope.methodScope();
		this.scope = new MethodScope(blockScope, this, methodScope.isStatic);
		this.scope.isConstructorCall = methodScope.isConstructorCall;

		super.resolveType(blockScope); // compute & capture interface function descriptor in singleAbstractMethod.
		
		final boolean argumentsTypeElided = argumentsTypeElided();
		final boolean haveDescriptor = this.descriptor != null;
		
		if (haveDescriptor && this.descriptor.typeVariables != Binding.NO_TYPE_VARIABLES) // already complained in kosher*
			return null;
		
		if (!haveDescriptor && argumentsTypeElided) 
			return null; // FUBAR, bail out...

		this.binding = new MethodBinding(ClassFileConstants.AccPrivate | ClassFileConstants.AccSynthetic | ExtraCompilerModifiers.AccUnresolved,
							TypeConstants.ANONYMOUS_METHOD, // will be fixed up later.
							haveDescriptor ? this.descriptor.returnType : null, 
							Binding.NO_PARAMETERS, // for now. 
							haveDescriptor ? this.descriptor.thrownExceptions : Binding.NO_EXCEPTIONS, 
							blockScope.enclosingSourceType());
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
				if ((lambdaInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0 || ((Block) this.body).statements == null) {
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
	
	public MethodScope getScope() {
		return this.scope;
	}
		
	protected boolean errorEqualsIncompatibility() {
		return this.original.shapeAnalysisComplete; // so as not to abort shape analysis.
	}
	
	public boolean isCompatibleWith(final TypeBinding left, final Scope someScope) {
		
		final MethodBinding sam = left.getSingleAbstractMethod(this.enclosingScope);
		
		if (sam == null || !sam.isValidBinding())
			return false;
		if (sam.parameters.length != this.arguments.length)
			return false;
		
		if (!this.shapeAnalysisComplete && this.body instanceof Expression) {
			Expression expression = (Expression) this.body;
			this.voidCompatible = expression.statementExpression();
			this.valueCompatible = true;
			this.shapeAnalysisComplete = true;
		}

		if (this.shapeAnalysisComplete) {
			if (squarePegInRoundHole(sam))
				return false;
		} 

		IErrorHandlingPolicy oldPolicy = this.enclosingScope.problemReporter().switchErrorHandlingPolicy(silentErrorHandlingPolicy);
		this.hasIgnoredMandatoryErrors = false;
		try {
			final LambdaExpression copy = copy();
			if (copy == null)
				return false;
			copy.setExpressionContext(this.expressionContext);
			copy.setExpectedType(left);
			if (this.resultExpressions == null)
				this.resultExpressions = new SimpleLookupTable(); // gather result expressions for most specific method analysis.
			this.resultExpressions.put(left, new Expression[0]);
			copy.resolveType(this.enclosingScope);
			if (!this.shapeAnalysisComplete) {
				boolean lambdaIsFubar = this.hasIgnoredMandatoryErrors; // capture now, before doesNotCompleteNormally which runs analyzeCode on lambda body *without* the enclosing context being analyzed 
				if (!this.returnsVoid && !this.returnsValue && this.throwsException) {  // () -> { throw new Exception(); } is value compatible.
					Block block = (Block) this.body;
					final Statement[] statements = block.statements;
					final int statementsLength = statements == null ? 0 : statements.length;
					Statement ultimateStatement = statementsLength == 0 ? null : statements[statementsLength - 1];
					this.valueCompatible = ultimateStatement instanceof ThrowStatement ? true: copy.doesNotCompleteNormally(); 
				}
				this.shapeAnalysisComplete = true;
				if (squarePegInRoundHole(sam) || lambdaIsFubar)
					return false;
			}
		} catch (IncongruentLambdaException e) {
			return false;
		} finally {
			this.enclosingScope.problemReporter().switchErrorHandlingPolicy(oldPolicy);
			this.hasIgnoredMandatoryErrors = false;
		}
		return true;
	}
	
	public boolean tIsMoreSpecific(TypeBinding t, TypeBinding s) {
		/* 15.12.2.5 t is more specific than s iff ... Some of the checks here are redundant by the very fact of control reaching here, 
		   but have been left in for completeness/documentation sakes. These should be cheap anyways. 
		*/
		
		// Both t and s are functional interface types ... 
		MethodBinding tSam = t.getSingleAbstractMethod(this.enclosingScope);
		if (tSam == null || !tSam.isValidBinding())
			return false;
		MethodBinding sSam = s.getSingleAbstractMethod(this.enclosingScope);
		if (sSam == null || !sSam.isValidBinding())
			return false;
		
		// t should neither be a subinterface nor a superinterface of s
		if (t.findSuperTypeOriginatingFrom(s) != null || s.findSuperTypeOriginatingFrom(t) != null)
			return false;

		// If the lambda expression's parameters have inferred types, then the descriptor parameter types of t are the same as the descriptor parameter types of s.
		if (argumentsTypeElided()) {
			if (tSam.parameters.length != sSam.parameters.length)
				return false;
			for (int i = 0, length = tSam.parameters.length; i < length; i++) {
				if (tSam.parameters[i] != sSam.parameters[i])
					return false;
			}
		}
		
		// either the descriptor return type of s is void or ...
		if (sSam.returnType.id == TypeIds.T_void)
			return true;
		
		/* ... or for all result expressions in the lambda body (or for the body itself if the body is an expression), 
           the descriptor return type of the capture of T is more specific than the descriptor return type of S.
		*/
		Expression [] returnExpressions = (Expression[]) this.resultExpressions.get(t); // should be same as for s
		int returnExpressionsLength = returnExpressions == null ? 0 : returnExpressions.length;
		if (returnExpressionsLength == 0)
			return true; // as good as or as bad as false.
		
		t = t.capture(this.enclosingScope, this.sourceEnd);
		tSam = t.getSingleAbstractMethod(this.enclosingScope);
		for (int i = 0; i < returnExpressionsLength; i++) {
			Expression resultExpression = returnExpressions[i];
			if (!resultExpression.tIsMoreSpecific(tSam.returnType, sSam.returnType))
				return false;
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
		final Parser parser = new Parser(this.enclosingScope.problemReporter(), false);
		final char[] source = this.compilationResult.getCompilationUnit().getContents();
		LambdaExpression copy =  (LambdaExpression) parser.parseLambdaExpression(source, this.sourceStart, this.sourceEnd - this.sourceStart + 1, 
										this.enclosingScope.referenceCompilationUnit(), false /* record line separators */);

		if (copy != null) { // ==> syntax errors == null
			copy.original = this;
		}
		return copy;
	}

	public void returnsExpression(Expression expression, TypeBinding resultType) {
		if (this.original == this) // not in overload resolution context.
			return;
		if (expression != null) {
			this.original.returnsValue = true;
			this.original.voidCompatible = false;
			this.original.valueCompatible = !this.original.returnsVoid;
			if (resultType != null) {
				Expression [] results = (Expression[]) this.original.resultExpressions.get(this.expectedType);
				int resultsLength = results.length;
				System.arraycopy(results, 0, results = new Expression[resultsLength + 1], 0, resultsLength);
				results[resultsLength] = expression;
			}
		} else {
			this.original.returnsVoid = true;
			this.original.valueCompatible = false;
			this.original.voidCompatible = !this.original.returnsValue;
		}
	}

	public void throwsException(TypeBinding exceptionType) {
		if (this.expressionContext != INVOCATION_CONTEXT)
			return;
		this.original.throwsException = true;
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
	
	public void tagAsHavingIgnoredMandatoryErrors(int problemId) {
		// 15.27.3 requires exception throw related errors to not influence congruence. Other errors should. Also don't abort shape analysis.
		switch (problemId) {
			case IProblem.UnhandledExceptionOnAutoClose:
			case IProblem.UnhandledExceptionInDefaultConstructor:
			case IProblem.UnhandledException:
				return;
			default: 
				if (errorEqualsIncompatibility())
					throw new IncongruentLambdaException();
				this.original().hasIgnoredMandatoryErrors = true;
				return;
		}
	}

	public void generateCode(ClassScope classScope, ClassFile classFile) {
		int problemResetPC = 0;
		classFile.codeStream.wideMode = false;
		boolean restart = false;
		do {
			try {
				problemResetPC = classFile.contentsOffset;
				this.generateCode(classFile);
				restart = false;
			} catch (AbortMethod e) {
				// Restart code generation if possible ...
				if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
					// a branch target required a goto_w, restart code generation in wide mode.
					classFile.contentsOffset = problemResetPC;
					classFile.methodCount--;
					classFile.codeStream.resetInWideMode(); // request wide mode
					restart = true;
				} else if (e.compilationResult == CodeStream.RESTART_CODE_GEN_FOR_UNUSED_LOCALS_MODE) {
					classFile.contentsOffset = problemResetPC;
					classFile.methodCount--;
					classFile.codeStream.resetForCodeGenUnusedLocals();
					restart = true;
				} else {
					throw new AbortType(this.compilationResult, e.problem);
				}
			}
		} while (restart);
	}
	
	public void generateCode(ClassFile classFile) {
		classFile.generateMethodInfoHeader(this.binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttributes(this.binding);
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);
		// initialize local positions
		this.scope.computeLocalVariablePositions(this.outerLocalVariablesSlotSize + (this.binding.isStatic() ? 0 : 1), codeStream);
		if (this.outerLocalVariables != null) {
			for (int i = 0, max = this.outerLocalVariables.length; i < max; i++) {
				LocalVariableBinding argBinding;
				codeStream.addVisibleLocalVariable(argBinding = this.outerLocalVariables[i]);
				codeStream.record(argBinding);
				argBinding.recordInitializationStartPC(0);
			}
		}
		// arguments initialization for local variable debug attributes
		if (this.arguments != null) {
			for (int i = 0, max = this.arguments.length; i < max; i++) {
				LocalVariableBinding argBinding;
				codeStream.addVisibleLocalVariable(argBinding = this.arguments[i].binding);
				argBinding.recordInitializationStartPC(0);
			}
		}
		if (this.body instanceof Block) {
			this.body.generateCode(this.scope, codeStream);
			if ((this.bits & ASTNode.NeedFreeReturn) != 0) {
				codeStream.return_();
			}
		} else {
			Expression expression = (Expression) this.body;
			expression.generateCode(this.scope, codeStream, true);
			if (this.binding.returnType == TypeBinding.VOID) {
				codeStream.return_();
			} else {
				codeStream.generateReturnBytecode(expression);
			}
		}
		// local variable attributes
		codeStream.exitUserScope(this.scope);
		codeStream.recordPositionsFrom(0, this.sourceEnd); // WAS declarationSourceEnd.
		try {
			classFile.completeCodeAttribute(codeAttributeOffset);
		} catch(NegativeArraySizeException e) {
			throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
		}
		attributeNumber++;

		classFile.completeMethodInfo(this.binding, methodAttributeOffset, attributeNumber);
	}
	
	public void addSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {
		
		if (this.original != this || this.binding == null) 
			return; // Do not bother tracking outer locals for clones created during overload resolution.
		
		SyntheticArgumentBinding syntheticLocal = null;
		int newSlot = this.outerLocalVariables.length;
		for (int i = 0; i < newSlot; i++) {
			if (this.outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable)
				return;
		}
		System.arraycopy(this.outerLocalVariables, 0, this.outerLocalVariables = new SyntheticArgumentBinding[newSlot + 1], 0, newSlot);
		this.outerLocalVariables[newSlot] = syntheticLocal = new SyntheticArgumentBinding(actualOuterLocalVariable);
		syntheticLocal.resolvedPosition = this.outerLocalVariablesSlotSize; // may need adjusting later if we need to generate an instance method for the lambda.
		syntheticLocal.declaringScope = this.scope;
		int parameterCount = this.binding.parameters.length;
		TypeBinding [] newParameters = new TypeBinding[parameterCount + 1];
		newParameters[newSlot] = actualOuterLocalVariable.type;
		for (int i = 0, j = 0; i < parameterCount; i++, j++) {
			if (i == newSlot) j++;
			newParameters[j] = this.binding.parameters[i];
		}
		this.binding.parameters = newParameters;
		switch (syntheticLocal.type.id) {
			case TypeIds.T_long :
			case TypeIds.T_double :
				this.outerLocalVariablesSlotSize  += 2;
				break;
			default :
				this.outerLocalVariablesSlotSize++;
				break;
		}		
	}

	public SyntheticArgumentBinding getSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {
		for (int i = 0, length = this.outerLocalVariables == null ? 0 : this.outerLocalVariables.length; i < length; i++)
			if (this.outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable)
				return this.outerLocalVariables[i];
		return null;
	}
}
class IncongruentLambdaException extends RuntimeException {
	private static final long serialVersionUID = 4145723509219836114L;
}