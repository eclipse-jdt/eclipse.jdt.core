package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.OperatorOverloadInvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 *
 * @author milan
 *
 */
public class EqualEqualExpression extends BinaryExpression{

	public EqualEqualExpression(Expression left, Expression right, int operator) {
		super(left, right, operator);
		/**
		 * Add custom '===', '!==' code
		 */
	}

	@Override
	public MethodBinding getMethodBindingForOverload(BlockScope scope) {
		TypeBinding tb_right = null;
		TypeBinding tb_left = null;

		if(this.left.resolvedType == null)
			tb_left = this.left.resolveType(scope);
		else
			tb_left = this.left.resolvedType;

		if(this.right.resolvedType == null)
			tb_right = this.right.resolveType(scope);
		else
			tb_right = this.right.resolvedType;

		final TypeBinding expectedTypeLocal = this.expectedType;
		OperatorOverloadInvocationSite fakeInvocationSite = new OperatorOverloadInvocationSite(){
			@Override
			public TypeBinding[] genericTypeArguments() { return null; }
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
			public TypeBinding getExpectedType() {
				return expectedTypeLocal;
			}
			@Override
			public TypeBinding invocationTargetType() { return null; }
			@Override
			public boolean receiverIsImplicitThis() { return false; }
			@Override
			public InferenceContext18 freshInferenceContext(Scope s) { return null; }
			@Override
			public ExpressionContext getExpressionContext() { return null; }
			@Override
			public boolean isQualifiedSuper() { return false; }
			@Override
			public boolean checkingPotentialCompatibility() { return false; }
			@Override
			public void acceptPotentiallyCompatibleMethods(MethodBinding[] methods) {/* ignore */}
		};

		String ms = ""; //$NON-NLS-1$
		String rms = ""; //$NON-NLS-1$

		if(getOperatorType() == EQUAL_EQUAL_EQUAL){
			ms = getMethodNameForEq();
			rms = getMethodNameForNeq();
		}else if(getOperatorType() == NOT_EQUAL_EQUAL){
			ms = getMethodNameForNeq();
			rms = getMethodNameForEq();
		}

		//Object <op> Object
		if (!tb_left.isBoxedPrimitiveType() && !tb_left.isBaseType() && !tb_right.isBoxedPrimitiveType() && !tb_right.isBaseType()){
			MethodBinding mbLeft = scope.getMethod(tb_left, ms.toCharArray(), new TypeBinding[]{tb_right},  fakeInvocationSite);
			MethodBinding mbRight = scope.getMethod(tb_right, (ms + "AsRHS").toCharArray(), new TypeBinding[]{tb_left},  fakeInvocationSite); //$NON-NLS-1$
			/**
			 * Check for required counter method
			 */
			MethodBinding mbCounterLeft = scope.getMethod(tb_left, rms.toCharArray(), new TypeBinding[]{tb_right},  fakeInvocationSite);
			MethodBinding mbCounterRight = scope.getMethod(tb_right, (rms + "AsRHS").toCharArray(), new TypeBinding[]{tb_left},  fakeInvocationSite); //$NON-NLS-1$

			if(mbLeft.isValidBinding() && mbRight.isValidBinding()){
				if (mbLeft.isStatic() && mbRight.isStatic()) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, ms);
					return null;
				}
				return new ProblemMethodBinding(ms.toCharArray(), new TypeBinding[]{tb_right}, ProblemReasons.Ambiguous);
			}

			if(mbLeft.isValidBinding()){
				if(!mbCounterLeft.isValidBinding())
					scope.problemReporter().invalidOrMissingOverloadedOperator(this, rms, this.right.resolvedType);

				if(mbLeft.isStatic()) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, ms);
					return null;
				}
				if(mbCounterLeft.isStatic()) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, rms);
					return null;
				}
				this.overloadedExpresionSide = overloadedLeftSide;
				return mbLeft;
			}

			if(mbRight.isValidBinding()){
				if(!mbCounterRight.isValidBinding())
					scope.problemReporter().invalidOrMissingOverloadedOperator(this, rms, this.right.resolvedType);

				if(mbRight.isStatic()) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, ms);
					return null;
				}
				if(mbCounterRight.isStatic()) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, rms);
					return null;
				}
				this.overloadedExpresionSide = overloadedRightSide;
				return mbRight;
			}
			if(tb_left.id == T_JavaLangString || tb_right.id == T_JavaLangString){
				return null;
			}
			return null;
		}


		//Object <op> type or type <op> Object
		if(!tb_left.isBoxedPrimitiveType() && !tb_left.isBaseType() && (tb_right.isBoxedPrimitiveType() || tb_right.isBaseType())){
			MethodBinding mbLeft = scope.getMethod(tb_left, ms.toCharArray(), new TypeBinding[]{tb_right}, fakeInvocationSite);
			MethodBinding mbCounterLeft = scope.getMethod(tb_left, rms.toCharArray(), new TypeBinding[]{tb_right}, fakeInvocationSite);
			if(mbLeft.isValidBinding() && isAnnotationSet(mbLeft)){
				if(!mbCounterLeft.isValidBinding())
					scope.problemReporter().invalidOrMissingOverloadedOperator(this, rms, this.right.resolvedType);

				if(mbLeft.isStatic()) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, ms);
					return null;
				}
				if(mbCounterLeft.isStatic()) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, rms);
					return null;
				}
				this.overloadedExpresionSide = overloadedLeftSide;
				return mbLeft;
			}
			return null;
		}
		if(!tb_right.isBoxedPrimitiveType() && !tb_right.isBaseType() && (tb_left.isBoxedPrimitiveType() || tb_left.isBaseType())){
			MethodBinding mbRight = scope.getMethod(tb_right, (ms + "AsRHS").toCharArray(), new TypeBinding[]{tb_left}, fakeInvocationSite); //$NON-NLS-1$
			MethodBinding mbCounterRight = scope.getMethod(tb_right, (rms + "AsRHS").toCharArray(), new TypeBinding[]{tb_left}, fakeInvocationSite); //$NON-NLS-1$
			if(mbRight.isValidBinding()){
				if(!mbCounterRight.isValidBinding())
					scope.problemReporter().invalidOrMissingOverloadedOperator(this, rms + "AsRHS", this.right.resolvedType);//$NON-NLS-1$

				if(mbRight.isStatic()) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, ms + "AsRHS");//$NON-NLS-1$
					return null;
				}
				if(mbCounterRight.isStatic()) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, rms + "AsRHS");//$NON-NLS-1$
					return null;
				}
				this.overloadedExpresionSide = overloadedRightSide;
				return mbRight;
			}
			return null;
		}
		return null;
	}

	private String getMethodNameForEq(){
		return "eq"; //$NON-NLS-1$
	}

	private String getMethodNameForNeq(){
		return "neq"; //$NON-NLS-1$
	}

	public int getOperatorType() {
		return (this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT;
	}

}
