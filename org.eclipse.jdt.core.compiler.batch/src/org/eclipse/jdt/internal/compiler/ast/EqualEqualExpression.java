package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
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

		String ms = ""; //$NON-NLS-1$
		String rms = ""; //$NON-NLS-1$
		if (getOperatorType() == EQUAL_EQUAL_EQUAL) {
			ms = getMethodNameForEq();
			rms = getMethodNameForNeq();
		} else if (getOperatorType() == NOT_EQUAL_EQUAL) {
			ms = getMethodNameForNeq();
			rms = getMethodNameForEq();
		}

		//Object <op> Object
		if (!tb_left.isBoxedPrimitiveType() && !tb_left.isBaseType() && !tb_right.isBoxedPrimitiveType() && !tb_right.isBaseType()){
			MethodBinding mbLeft = getLeftMethod(scope, ms, tb_left, tb_right);
			MethodBinding mbRight = getRightMethod(scope, ms, tb_left, tb_right);
			/**
			 * Check for required counter method
			 */
			MethodBinding mbCounterLeft = getLeftMethod(scope, rms, tb_left, tb_right);
			MethodBinding mbCounterRight = getRightMethod(scope, rms, tb_left, tb_right);

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
			MethodBinding mbLeft = getLeftMethod(scope, ms, tb_left, tb_right);
			MethodBinding mbCounterLeft = getLeftMethod(scope, rms, tb_left, tb_right);
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
			MethodBinding mbRight = getRightMethod(scope, ms, tb_left, tb_right);
			MethodBinding mbCounterRight = getRightMethod(scope, rms, tb_left, tb_right);
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
