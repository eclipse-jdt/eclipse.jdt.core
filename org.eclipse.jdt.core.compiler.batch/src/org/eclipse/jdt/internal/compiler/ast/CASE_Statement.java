package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CaseLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.OperatorOverloadInvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 *
 * @author milan
 *
 */
public class CASE_Statement extends Statement {

	public Expression constantExpression;
	public CaseLabel targetLabel;
	private ThisReference thisReference;
	private boolean defaultStatement;
	public MethodBinding appropriateMethodForOverload = null;
	public MethodBinding syntheticAccessor;

public CASE_Statement(Expression constantExpression, int sourceEnd, int sourceStart, ThisReference thisReference, boolean defaultStatement) {
	this.constantExpression = constantExpression;
	this.sourceEnd = sourceEnd;
	this.sourceStart = sourceStart;
	this.thisReference = thisReference;
	this.defaultStatement = defaultStatement;
}

@Override
public FlowInfo analyseCode(
	BlockScope currentScope,
	FlowContext flowContext,
	FlowInfo flowInfo) {

	if (this.constantExpression != null) {
		this.constantExpression.analyseCode(currentScope, flowContext, flowInfo);
	}
	if(this.appropriateMethodForOverload != null){
		MethodBinding original = this.appropriateMethodForOverload.original();
		if(original.isPrivate()){
			this.syntheticAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false /* not super access there */);
			currentScope.problemReporter().needToEmulateMethodAccess(original, this);
		}
	}
	return flowInfo;
}

@Override
public StringBuilder printStatement(int tab, StringBuilder output) {
	printIndent(tab, output);
	if (this.defaultStatement) {
		output.append("OTHERWISE"); //$NON-NLS-1$
	} else {
		output.append("CASE ("); //$NON-NLS-1$
		this.constantExpression.printExpression(0, output).append(")"); //$NON-NLS-1$
	}
	output.append('{');
	return output.append('}');
}

/**
 * Case code generation
 *
 */
@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if ((this.bits & ASTNode.IsReachable) == 0) {
			return;
		}
		this.generateOperatorOverloadCodeForCASE(this.appropriateMethodForOverload, currentScope, codeStream, true);
		int pc = codeStream.position;
		this.targetLabel.place();
		codeStream.recordPositionsFrom(pc, this.sourceStart);
}

/**
 * No-op : should use resolveCase(...) instead.
 */
@Override
public void resolve(BlockScope scope) {
	// no-op : should use resolveCase(...) instead.
	// switchExpressionType maybe null in error case
}

/**
 * Returns the constant intValue or ordinal for enum constants. If constant is NotAConstant, then answers Float.MIN_VALUE
 * @see org.eclipse.jdt.internal.compiler.ast.Statement#resolveCase(org.eclipse.jdt.internal.compiler.lookup.BlockScope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding, org.eclipse.jdt.internal.compiler.ast.SWITCH_Statement)
 */
@Override
public Constant resolveCase(BlockScope scope, TypeBinding switchExpressionType, SWITCH_Statement switchStatement) {
	// switchExpressionType maybe null in error case
	scope.enclosingCASE = this; // record entering in a switch case block

	if (this.constantExpression == null) {
		// remember the default case into the associated switch statement
		if (switchStatement.defaultCase != null)
			scope.problemReporter().duplicateDefaultCase(this);

		// on error the last default will be the selected one ...
		switchStatement.defaultCase = this;
		MethodBinding mb2 = this.getMethodBindingForOverloadForCASE(scope);
		if (mb2 != null && mb2.isValidBinding()) {
			if((mb2.modifiers & ClassFileConstants.AccStatic) != 0) {
				scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodName());
				return Constant.NotAConstant;
			}
			if(mb2.returnType != TypeBinding.VOID){
				scope.problemReporter().typeMismatchError(mb2.returnType, TypeBinding.VOID, this, null);
				return Constant.NotAConstant;
			}
			this.appropriateMethodForOverload = mb2;
			if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
				scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
			if(this.thisReference.resolvedType == null)
				this.thisReference.resolveType(scope);
			this.thisReference.computeConversion(scope, this.thisReference.resolvedType, this.thisReference.resolvedType);
		}else{
			if(this.constantExpression == null)
				scope.problemReporter().invalidOrMissingOverloadedOperator(this, getMethodName(), TypeBinding.VOID);
			else
				scope.problemReporter().invalidOrMissingOverloadedOperator(this, getMethodName(), this.constantExpression.resolvedType);
			return Constant.NotAConstant;
		}
		return Constant.NotAConstant;
	}
	// add into the collection of cases of the associated switch statement
	switchStatement.cases[switchStatement.caseCount++] = this;
	if(this.constantExpression.resolvedType == null){
		this.constantExpression.resolveType(scope);
		if(this.constantExpression.resolvedType == null)
			return Constant.NotAConstant;
	}
	MethodBinding mb2 = this.getMethodBindingForOverloadForCASE(scope);
	if (mb2 != null && mb2.isValidBinding()) {
		if((mb2.modifiers & ClassFileConstants.AccStatic) != 0) {
			scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodName());
			return Constant.NotAConstant;
		}
		if(mb2.returnType != TypeBinding.VOID){
			scope.problemReporter().invalidReturnTypeForOverloadedOperator(this, getMethodName(), TypeBinding.VOID, mb2.returnType);
			return Constant.NotAConstant;
		}
		this.appropriateMethodForOverload = mb2;
		if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
			scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
		if(this.thisReference.resolvedType == null)
			this.thisReference.resolveType(scope);
		this.thisReference.computeConversion(scope, this.thisReference.resolvedType, this.thisReference.resolvedType);
		this.constantExpression.computeConversion(scope, this.appropriateMethodForOverload.parameters[0], this.constantExpression.resolvedType);
	} else {
		scope.problemReporter().invalidOrMissingOverloadedOperator(this, getMethodName(), this.constantExpression.resolvedType);
		return Constant.NotAConstant;
	}
	return Constant.NotAConstant;
}

@Override
public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		if (this.constantExpression != null) this.constantExpression.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}

/**
 * new Milan: CASE
 */

public MethodBinding getMethodBindingForOverloadForCASE(final BlockScope scope) {
	TypeBinding [] tb_right;
	String ms;
	if(this.defaultStatement)
		tb_right= new TypeBinding[] {};
	else
		tb_right= new TypeBinding[] {this.constantExpression.resolvedType};

	ms = getMethodName();

	final TypeBinding tb = scope.parent.classScope().referenceContext.binding;
	final Expression [] arguments = new Expression[] { this.constantExpression };

	InvocationSite fakeInvocationSite = new OperatorOverloadInvocationSite() {
		@Override
		public TypeBinding invocationTargetType() {
			return CASE_Statement.this.expectedType();
		}
		@Override
		public Expression[] arguments() {
			return arguments;
		}
	};

	MethodBinding mb2 = scope.parent.getMethod(tb, ms.toCharArray(), tb_right, fakeInvocationSite);
	return mb2;
}

public void generateOperatorOverloadCodeForCASE(MethodBinding mb2, BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	this.thisReference.generateCode(currentScope, codeStream, true);
	if(!this.defaultStatement)
		this.constantExpression.generateCode(currentScope, codeStream, true);
	if (mb2.hasSubstitutedParameters() || mb2.hasSubstitutedReturnType()) {
		TypeBinding tbo = mb2.returnType;
		MethodBinding mb3 = mb2.original();
		MethodBinding final_mb = mb3;
		codeStream.checkcast(final_mb.declaringClass);
		codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());
		if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
			codeStream.checkcast(tbo);
		}
	} else {
		MethodBinding original = mb2.original();
		if(original.isPrivate()){
			codeStream.invoke(Opcodes.OPC_invokestatic, this.syntheticAccessor, null /* default declaringClass */);
		}
		else{
			codeStream.invoke((original.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, original, original.declaringClass);
		}
		if (!mb2.returnType.isBaseType()) codeStream.checkcast(mb2.returnType);
	}
}
public String getMethodName() {
	if(this.defaultStatement)
		return "_OTHERWISE"; //$NON-NLS-1$

	return "_CASE"; //$NON-NLS-1$
}

}
