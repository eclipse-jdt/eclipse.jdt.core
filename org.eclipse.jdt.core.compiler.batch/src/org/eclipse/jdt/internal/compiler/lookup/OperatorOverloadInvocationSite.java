package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ExpressionContext;
import org.eclipse.jdt.internal.compiler.ast.Invocation;

public abstract class OperatorOverloadInvocationSite implements Invocation {

	@Override
	public boolean isSuperAccess(){ return false; }
	@Override
	public boolean isTypeAccess() { return true; }
	@Override
	public void setActualReceiverType(ReferenceBinding actualReceiverType) { /* ignore */}
	@Override
	public void setDepth(int depth) { /* ignore */}
	@Override
	public void setFieldIndex(int depth){ /* ignore */}
	@Override
	public int sourceStart() { return 0; }
	@Override
	public int sourceEnd() { return 0; }

	@Override
	public ExpressionContext getExpressionContext() {
		return ExpressionContext.VANILLA_CONTEXT;
	}

	@Override
	public TypeBinding[] genericTypeArguments() {
		return null;
	}

	@Override
	public InferenceContext18 freshInferenceContext(Scope scope) {
		return new InferenceContext18(scope, this.arguments(), this, null);
	}

	@Override
	public void registerInferenceContext(ParameterizedGenericMethodBinding method, InferenceContext18 infCtx18) {
		// only used for poly expressions
	}

	@Override
	public void registerResult(TypeBinding targetType, MethodBinding method) {
		// only used for poly expressions
	}

	@Override
	public InferenceContext18 getInferenceContext(ParameterizedMethodBinding method) {
		// only used for poly expressions
		return null;
	}

	@Override
	public void cleanUpInferenceContexts() {
		// nothing to do
	}

	@Override
	public MethodBinding binding() {
		// only used for poly expressions
		return null;
	}

	@Override
	public void acceptPotentiallyCompatibleMethods(MethodBinding[] methods) {
		// ignore

	}
	@Override
	public boolean receiverIsImplicitThis() {
		return false;
	}
	@Override
	public boolean isQualifiedSuper() {
		return false;
	}
	@Override
	public boolean checkingPotentialCompatibility() {
		return false;
	}
 }
