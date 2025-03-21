package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.OperatorOverloadInvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 *
 * @author milan
 *
 */
public class CompositeArrayReference extends Reference {

	public Expression receiver;
	public Expression positionOne;
	public Expression positionTwo;
	public MethodBinding appropriateMethodForOverload = null;
	public MethodBinding compoundAppropriateMethodForOverload = null;
	public TypeBinding expectedType = null; //Operator overload, for generic function call
	public MethodBinding syntheticAccessor;
	public MethodBinding syntheticCompundAccessor;

	@Override
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

public CompositeArrayReference(Expression rec, Expression pos1, Expression pos2) {
	this.receiver = rec;
	this.positionOne = pos1;
	this.positionTwo = pos2;
	this.sourceStart = rec.sourceStart;
}

@Override
public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean compoundAssignment) {
	// TODO (maxime) optimization: unconditionalInits is applied to all existing calls
	// account for potential ArrayIndexOutOfBoundsException:
	flowContext.recordAbruptExit();

	if (this.appropriateMethodForOverload != null) {
		MethodBinding original = this.appropriateMethodForOverload.original();
		if (original.isPrivate()) {
			this.syntheticAccessor = ((SourceTypeBinding) original.declaringClass).addSyntheticMethod(original, false);
			currentScope.problemReporter().needToEmulateMethodAccess(original, this);
		}
	}

	if (this.compoundAppropriateMethodForOverload != null) {
		MethodBinding original = this.compoundAppropriateMethodForOverload.original();
		if (original.isPrivate()) {
			this.syntheticCompundAccessor = ((SourceTypeBinding) original.declaringClass).addSyntheticMethod(original, false);
			currentScope.problemReporter().needToEmulateMethodAccess(original, this);
		}
	}

	if (assignment.expression == null) {
		return analyseCode(currentScope, flowContext, flowInfo);
	}
	flowInfo = assignment
		.expression
		.analyseCode(
			currentScope,
			flowContext,
			analyseCode(currentScope, flowContext, flowInfo).unconditionalInits());
	if (currentScope.environment().usesNullTypeAnnotations()) {
		checkAgainstNullTypeAnnotation(currentScope, this.resolvedType, assignment.expression, flowContext, flowInfo);
	}
	return flowInfo;
}

@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	if (this.appropriateMethodForOverload != null) {
		MethodBinding original = this.appropriateMethodForOverload.original();
		if (original.isPrivate()) {
			this.syntheticAccessor = ((SourceTypeBinding) original.declaringClass).addSyntheticMethod(original, false);
			currentScope.problemReporter().needToEmulateMethodAccess(original, this);
		}
	}
	if (this.compoundAppropriateMethodForOverload != null){
		MethodBinding original = this.compoundAppropriateMethodForOverload.original();
		if (original.isPrivate()) {
			this.syntheticCompundAccessor = ((SourceTypeBinding) original.declaringClass).addSyntheticMethod(original, false);
			currentScope.problemReporter().needToEmulateMethodAccess(original, this);
		}
	}
	flowInfo = this.receiver.analyseCode(currentScope, flowContext, flowInfo);
	this.receiver.checkNPE(currentScope, flowContext, flowInfo, 1);
	flowInfo = this.positionOne.analyseCode(currentScope, flowContext, flowInfo);
	this.positionOne.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
	// account for potential ArrayIndexOutOfBoundsException:
	flowContext.recordAbruptExit();
	flowInfo = this.positionTwo.analyseCode(currentScope, flowContext, flowInfo);
	this.positionTwo.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
	flowContext.recordAbruptExit();
	return flowInfo;
}

@Override
public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
	MethodBinding mb2 = this.getMethodBindingForOverload(currentScope, new Expression[]{this.positionOne, this.positionTwo, assignment.expression}, true);
	if (mb2 != null && mb2.isValidBinding()) {
		if (mb2.isStatic()) {
			currentScope.problemReporter().overloadedOperatorMethodNotStatic(this, "[:] ="); //$NON-NLS-1$
			return;
		}
		this.generatePutCode(currentScope, codeStream, valueRequired, assignment);
		return;
	}
	currentScope.problemReporter().invalidOrMissingOverloadedOperator(this, "put", this.positionOne.resolvedType, this.positionTwo.resolvedType, assignment.expression.resolvedType); //$NON-NLS-1$
	return;
}

/**
 * Code generation for a array reference
 */
@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	if (this.appropriateMethodForOverload != null && this.appropriateMethodForOverload.isValidBinding()) {
		if (this.appropriateMethodForOverload.isStatic()) {
			currentScope.problemReporter().overloadedOperatorMethodNotStatic(this, "= [:]"); //$NON-NLS-1$
			return;
		}
		this.generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
		return;
	}
	currentScope.problemReporter().invalidOrMissingOverloadedOperator(this, "get", this.positionOne.resolvedType, this.positionTwo.resolvedType); //$NON-NLS-1$
	return;
}

@Override
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	if (this.compoundAppropriateMethodForOverload != null) {
		return;
	}
	this.receiver.generateCode(currentScope, codeStream, true);
	if (this.receiver instanceof CastExpression	// ((type[])null)[0]
			&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
		codeStream.checkcast(this.receiver.resolvedType);
	}
	this.positionOne.generateCode(currentScope, codeStream, true);
	this.positionTwo.generateCode(currentScope, codeStream, true);
	codeStream.dup2();
	codeStream.arrayAt(this.resolvedType.id);
	int operationTypeID;
	switch(operationTypeID = (this.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4) {
		case T_JavaLangString :
		case T_JavaLangObject :
		case T_undefined :
			codeStream.generateStringConcatenationAppend(currentScope, null, expression);
			break;
		default :
			// promote the array reference to the suitable operation type
			codeStream.generateImplicitConversion(this.implicitConversion);
			// generate the increment value (will by itself  be promoted to the operation value)
			if (expression == IntLiteral.One) { // prefix operation
				codeStream.generateConstant(expression.constant, this.implicitConversion);
			} else {
				expression.generateCode(currentScope, codeStream, true);
			}
			// perform the operation
			codeStream.sendOperator(operator, operationTypeID);
			// cast the value back to the array reference type
			codeStream.generateImplicitConversion(assignmentImplicitConversion);
	}
	codeStream.arrayAtPut(this.resolvedType.id, valueRequired);
}

@Override
public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
	MethodBinding mb2 = this.getMethodBindingForOverload(currentScope, new Expression[]{this.positionOne, this.positionTwo}, false);
	if (mb2.isValidBinding()) {
		currentScope.problemReporter().abortDueToInternalError("Overloaded array reference post increment emulation is not implemented.", this); //$NON-NLS-1$
		return;
	}
	this.receiver.generateCode(currentScope, codeStream, true);
	if (this.receiver instanceof CastExpression	// ((type[])null)[0]
			&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
		codeStream.checkcast(this.receiver.resolvedType);
	}
	this.positionOne.generateCode(currentScope, codeStream, true);
	this.positionTwo.generateCode(currentScope, codeStream, true);
	codeStream.dup2();
	codeStream.arrayAt(this.resolvedType.id);
	if (valueRequired) {
		switch(this.resolvedType.id) {
			case TypeIds.T_long :
			case TypeIds.T_double :
				codeStream.dup2_x2();
				break;
			default :
				codeStream.dup_x2();
				break;
		}
	}
	codeStream.generateImplicitConversion(this.implicitConversion);
	codeStream.generateConstant(
		postIncrement.expression.constant,
		this.implicitConversion);
	codeStream.sendOperator(postIncrement.operator, this.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
	codeStream.generateImplicitConversion(
		postIncrement.preAssignImplicitConversion);
	codeStream.arrayAtPut(this.resolvedType.id, false);
}

@Override
public StringBuilder printExpression(int indent, StringBuilder output) {
	this.receiver.printExpression(0, output).append('[');
	this.positionOne.printExpression(0, output).append(":"); //$NON-NLS-1$
	return this.positionTwo.printExpression(0, output).append(']');
}

@Override
public TypeBinding resolveType(BlockScope scope) {
	this.constant = Constant.NotAConstant;
	if (this.receiver instanceof CastExpression	// no cast check for ((type[])null)[0]
			&& ((CastExpression)this.receiver).innermostCastedExpression() instanceof NullLiteral) {
		this.receiver.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
	}
	// test
	MethodBinding overloadMethod = this.getMethodBindingForOverload(scope, new Expression[]{this.positionOne, this.positionTwo}, false);
	if ((overloadMethod !=null) && (overloadMethod.isValidBinding())) {
		this.appropriateMethodForOverload = overloadMethod;
		if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
			scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
		this.resolvedType = overloadMethod.returnType;
		if(this.positionOne.resolvedType == null)
			this.positionOne.resolveType(scope);
		if(this.positionTwo.resolvedType == null)
			this.positionTwo.resolveType(scope);
		this.receiver.computeConversion(scope, this.receiver.resolvedType, this.receiver.resolvedType);
		this.positionOne.computeConversion(scope, overloadMethod.parameters[0], this.positionOne.resolvedType);
		this.positionTwo.computeConversion(scope, overloadMethod.parameters[1], this.positionTwo.resolvedType);
		this.setExpectedType(this.resolvedType);
		return overloadMethod.returnType;
	} else { // enforce INT
		TypeBinding expectedTypeLocal = TypeBinding.INT;
		this.positionOne.setExpectedType(expectedTypeLocal); // needed in case of generic method invocation
		TypeBinding expressionTypeOne = this.positionOne.resolvedType;
		this.positionTwo.setExpectedType(expectedTypeLocal); // needed in case of generic method invocation
		TypeBinding expressionTypeTwo = this.positionTwo.resolvedType;
		if (expressionTypeOne == null) {
			scope.problemReporter().typeMismatchError(TypeBinding.VOID, expectedTypeLocal, this, null);
			return null;
		}
		if (expressionTypeTwo == null) {
			scope.problemReporter().typeMismatchError(TypeBinding.VOID, expectedTypeLocal, this, null);
			return null;
		}
		if (TypeBinding.notEquals(expressionTypeOne, expectedTypeLocal)) {
			if (!expressionTypeOne.isCompatibleWith(expectedTypeLocal)) {
				if (!scope.isBoxingCompatibleWith(expressionTypeOne, expectedTypeLocal)) {
					scope.problemReporter().typeMismatchError(expressionTypeOne, expectedTypeLocal, this, null);
					return null;
				}
			}
		}
		if (TypeBinding.notEquals(expressionTypeTwo, expectedTypeLocal)) {
			if (!expressionTypeTwo.isCompatibleWith(expectedTypeLocal)) {
				if (!scope.isBoxingCompatibleWith(expressionTypeTwo, expectedTypeLocal)) {
					scope.problemReporter().typeMismatchError(expressionTypeTwo, expectedTypeLocal, this, null);
					return null;
				}
			}
		}
	}

	TypeBinding arrayType = this.receiver.resolvedType; //Type(scope);

	if (arrayType != null) {
		this.receiver.computeConversion(scope, arrayType, arrayType);
		if (arrayType.isArrayType()) {
			TypeBinding elementType = ((ArrayBinding) arrayType).elementsType();
			this.resolvedType = ((this.bits & ASTNode.IsStrictlyAssigned) == 0) ? elementType.capture(scope, this.sourceStart, this.sourceEnd) : elementType;
		} else {
			scope.problemReporter().referenceMustBeArrayTypeAt(arrayType, this);
		}
	}
	TypeBinding positionTypeOne = this.positionOne.resolvedType; //TypeExpecting(scope, TypeBinding.INT);
	TypeBinding positionTypeTwo = this.positionTwo.resolvedType; //TypeExpecting(scope, TypeBinding.INT);

	if (positionTypeOne != null) {
		this.positionOne.computeConversion(scope, TypeBinding.INT, positionTypeOne);
	}
	if (positionTypeTwo != null) {
		this.positionTwo.computeConversion(scope, TypeBinding.INT, positionTypeTwo);
	}

	return this.resolvedType;
}

@Override
public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		this.receiver.traverse(visitor, scope);
		this.positionOne.traverse(visitor, scope);
		this.positionTwo.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}

@Override
public int nullStatus(FlowInfo flowInfo, FlowContext flowContext) {
	return FlowInfo.UNKNOWN;
}

@Override
public void generatePreOverloadAssignment(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	 // this cannot be assigned
}

@Override
public void generatePostOverloadAssignment(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	 // this cannot be assigned
}

@Override
public TypeBinding resolveType(BlockScope scope, Expression expression) {
	//Only valid for Assignment
	Assignment assignment;
	try{
		assignment = (Assignment)expression;
	}catch(ClassCastException cce){
		return resolveType(scope);
	}
	MethodBinding mb2 = this.getMethodBindingForOverload(scope, new Expression [] {this.positionOne, this.positionTwo, assignment.expression}, true);
	if ((mb2 !=null) && (mb2.isValidBinding())) {
		this.resolvedType = TypeBinding.VOID;
		this.setExpectedType(this.resolvedType);
		/*if(mb2.returnType != TypeBinding.VOID)
		scope.problemReporter().typeMismatchError(mb2.returnType, TypeBinding.VOID, this, null);*/
		if(this.positionOne.resolvedType == null)
			this.positionOne.resolveType(scope);
		if(this.positionTwo.resolvedType == null)
			this.positionTwo.resolveType(scope);
		if(assignment.expression == null)
			assignment.expression.resolveType(scope);
		this.receiver.computeConversion(scope, this.receiver.resolvedType, this.receiver.resolvedType);
		this.positionOne.computeConversion(scope, mb2.parameters[0], this.positionOne.resolvedType);
		this.positionTwo.computeConversion(scope, mb2.parameters[1], this.positionTwo.resolvedType);
		assignment.expression.computeConversion(scope, mb2.parameters[2], assignment.expression.resolvedType);
		this.appropriateMethodForOverload = mb2;
		if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
			scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
		return this.resolvedType;
	}

	if(this.receiver == null || this.receiver.resolvedType == null ||  this.positionOne == null || this.positionOne.resolvedType == null
			||  this.positionTwo == null || this.positionTwo.resolvedType == null || assignment.expression == null || assignment.expression.resolvedType == null){
		return null;
	}

	scope.problemReporter().referenceMustBeArrayTypeAt(this.receiver.resolvedType, this);
	return null;
}

public String getMethodName(boolean put) {
	if (put) return "put"; //$NON-NLS-1$
	else return "get"; //$NON-NLS-1$
}

public MethodBinding getMethodBindingForOverload(BlockScope scope, Expression [] arguments, boolean put) {
	return getMethodBindingForOverload(scope, arguments, new TypeBinding[0], put);
}

public MethodBinding getMethodBindingForOverload(BlockScope scope, final Expression [] arguments, TypeBinding[] types, boolean put) {
	TypeBinding [] tb_right = new TypeBinding[types.length + arguments.length];
	TypeBinding tb_left = null;

	if(this.receiver.resolvedType == null)
		tb_left = this.receiver.resolveType(scope);
	else
		tb_left = this.receiver.resolvedType;

	boolean tbRightValid = true;
	for(int i=0; i<arguments.length; i++){
		if(arguments[i].resolvedType == null)
			tb_right[i] = arguments[i].resolveType(scope);
		else
			tb_right[i] = arguments[i].resolvedType;
		tbRightValid = tbRightValid && (tb_right[i] != null);
	}
	for(int i=0; i<types.length; i++){
		tb_right[arguments.length + i] = types[i];
		tbRightValid = tbRightValid && (tb_right[arguments.length + i] != null);
	}

	final TypeBinding targetType = tb_left;
	OperatorOverloadInvocationSite fakeInvocationSite = new OperatorOverloadInvocationSite(){
		@Override
		public TypeBinding invocationTargetType() {
			return targetType;
		}
		@Override
		public Expression[] arguments() {
			return arguments;
		}
	};

	String ms = getMethodName(put);
	if ((tb_left == null) || (!tbRightValid)) return null;
	MethodBinding mb2 = scope.getMethod(tb_left, ms.toCharArray(), tb_right, fakeInvocationSite);
	return mb2;
}

public void generateOperatorOverloadCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	this.generatePutCode(currentScope, codeStream, valueRequired, null);
}

public void generatePutCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired, Assignment assignment){
	if (this.appropriateMethodForOverload != null){
		this.receiver.generateCode(currentScope, codeStream,true);
		this.positionOne.generateCode(currentScope, codeStream, true);
		this.positionTwo.generateCode(currentScope, codeStream, true);
		if(assignment != null)
			assignment.expression.generateCode(currentScope, codeStream, true);
		if (this.appropriateMethodForOverload.hasSubstitutedParameters() || this.appropriateMethodForOverload.hasSubstitutedReturnType()) {
			TypeBinding tbo = this.appropriateMethodForOverload.returnType;
			MethodBinding mb3 = this.appropriateMethodForOverload.original();
			MethodBinding final_mb = mb3;
			// TODO remove for real?
			//final_mb.returnType = final_mb.returnType.erasure();
			codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());

			if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
				codeStream.checkcast(tbo);
			}
		} else {
			MethodBinding original = this.appropriateMethodForOverload.original();
			if(original.isPrivate()){
				codeStream.invoke(Opcodes.OPC_invokestatic, this.syntheticAccessor, null /* default declaringClass */);
			}
			else{
				codeStream.invoke((original.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, original, original.declaringClass);
			}
			if (!this.appropriateMethodForOverload.returnType.isBaseType()) codeStream.checkcast(this.appropriateMethodForOverload.returnType);
		}
		if (valueRequired) {
			codeStream.generateImplicitConversion(this.implicitConversion);
		}
	}
}

@Override
public TypeBinding resolveTypeCompoundOverloadOperator(BlockScope scope, TypeBinding type) {
	MethodBinding mb1 = getMethodBindingForOverload(scope, new Expression[]{this.positionOne, this.positionTwo}, new TypeBinding[]{type}, true);
	if(mb1 == null || !mb1.isValidBinding()){
		scope.problemReporter().referenceMustBeArrayTypeAt(this.receiver.resolvedType, this);
		return null;
	}
	this.compoundAppropriateMethodForOverload = mb1;
	if (isMethodUseDeprecated(this.compoundAppropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
		scope.problemReporter().deprecatedMethod(this.compoundAppropriateMethodForOverload, this);
	//Return value will be discarded if exist
	return  TypeBinding.VOID;

}

public void generatePreCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression,
		int operator, int preAssignImplicitConversion, boolean valueRequired) {
	if (this.compoundAppropriateMethodForOverload != null){
		this.receiver.generateCode(currentScope, codeStream,true);
		this.positionOne.generateCode(currentScope, codeStream, true);
		this.positionTwo.generateCode(currentScope, codeStream, true);
	}

}

public void generatePostCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression,
		int operator, int preAssignImplicitConversion, boolean valueRequired) {
	if (this.compoundAppropriateMethodForOverload != null){
		if (this.compoundAppropriateMethodForOverload.hasSubstitutedParameters() || this.compoundAppropriateMethodForOverload.hasSubstitutedReturnType()) {
			TypeBinding tbo = this.compoundAppropriateMethodForOverload.returnType;
			MethodBinding mb3 = this.compoundAppropriateMethodForOverload.original();
			MethodBinding final_mb = mb3;
			// TODO remove for real?
			//final_mb.returnType = final_mb.returnType.erasure();
			codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());

			if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
				codeStream.checkcast(tbo);
			}
		} else {
			MethodBinding original = this.compoundAppropriateMethodForOverload.original();
			if(original.isPrivate()){
				codeStream.invoke(Opcodes.OPC_invokestatic, this.syntheticCompundAccessor, null /* default declaringClass */);
			}
			else{
				codeStream.invoke((original.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, original, original.declaringClass);
			}
			if (!this.compoundAppropriateMethodForOverload.returnType.isBaseType()) codeStream.checkcast(this.compoundAppropriateMethodForOverload.returnType);
		}
		if (valueRequired) {
			codeStream.generateImplicitConversion(this.implicitConversion);
		}
		if(!this.compoundAppropriateMethodForOverload.returnType.equals(TypeBinding.VOID)){
			switch (this.compoundAppropriateMethodForOverload.returnType.id) {
				case T_long :
				case T_double :
					codeStream.pop2();
					break;
				case T_void :
					break;
				default :
					codeStream.pop();
			}
		}

	}

}

}
