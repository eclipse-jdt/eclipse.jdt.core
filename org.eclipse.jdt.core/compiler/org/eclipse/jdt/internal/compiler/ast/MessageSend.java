/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nick Teryaev - fix for bug (https://bugs.eclipse.org/bugs/show_bug.cgi?id=40752)
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class MessageSend extends Expression implements InvocationSite {
    
	public Expression receiver ;
	public char[] selector ;
	public Expression[] arguments ;
	public MethodBinding binding;							// exact binding resulting from lookup
	protected MethodBinding codegenBinding;		// actual binding used for code generation (if no synthetic accessor)
	MethodBinding syntheticAccessor;						// synthetic accessor for inner-emulation
	public TypeBinding expectedType;					// for generic method invocation (return type inference)

	public long nameSourcePosition ; //(start<<32)+end

	public TypeBinding receiverType, qualifyingType;
	public TypeBinding genericCast;
	public TypeReference[] typeArguments;
	public TypeBinding[] genericTypeArguments;
	
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	flowInfo = receiver.analyseCode(currentScope, flowContext, flowInfo, !binding.isStatic()).unconditionalInits();
	if (arguments != null) {
		int length = arguments.length;
		for (int i = 0; i < length; i++) {
			flowInfo = arguments[i].analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		}
	}
	ReferenceBinding[] thrownExceptions;
	if ((thrownExceptions = binding.thrownExceptions) != NoExceptions) {
		// must verify that exceptions potentially thrown by this expression are caught in the method
		flowContext.checkExceptionHandlers(thrownExceptions, this, flowInfo, currentScope);
	}
	manageSyntheticAccessIfNecessary(currentScope, flowInfo);	
	return flowInfo;
}
/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#computeConversion(org.eclipse.jdt.internal.compiler.lookup.Scope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding, org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
	if (runtimeTimeType == null || compileTimeType == null)
		return;
	// set the generic cast after the fact, once the type expectation is fully known (no need for strict cast)
	if (this.binding != null && this.binding.isValidBinding()) {
		MethodBinding originalBinding = this.binding.original();
		if (originalBinding != this.binding) {
		    // extra cast needed if method return type has type variable
		    if ((originalBinding.returnType.tagBits & TagBits.HasTypeVariable) != 0 && runtimeTimeType.id != T_Object) {
		        this.genericCast = originalBinding.returnType.genericCast(runtimeTimeType);
		    }
		} 	
	}
	super.computeConversion(scope, runtimeTimeType, compileTimeType);
}
/**
 * MessageSend code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */ 
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

	int pc = codeStream.position;

	// generate receiver/enclosing instance access
	boolean isStatic = this.codegenBinding.isStatic();
	// outer access ?
	if (!isStatic && ((bits & DepthMASK) != 0) && receiver.isImplicitThis()){
		// outer method can be reached through emulation if implicit access
		ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT);		
		Object[] path = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
		codeStream.generateOuterAccess(path, this, targetType, currentScope);
	} else {
		receiver.generateCode(currentScope, codeStream, !isStatic);
	}
	// generate arguments
	if (arguments != null){
		for (int i = 0, max = arguments.length; i < max; i++){
			arguments[i].generateCode(currentScope, codeStream, true);
		}
	}
	// actual message invocation
	if (syntheticAccessor == null){
		if (isStatic){
			codeStream.invokestatic(this.codegenBinding);
		} else {
			if( (receiver.isSuper()) || this.codegenBinding.isPrivate()){
				codeStream.invokespecial(this.codegenBinding);
			} else {
				if (this.codegenBinding.declaringClass.isInterface()){
					codeStream.invokeinterface(this.codegenBinding);
				} else {
					codeStream.invokevirtual(this.codegenBinding);
				}
			}
		}
	} else {
		codeStream.invokestatic(syntheticAccessor);
	}
	// operation on the returned value
	if (valueRequired){
		// implicit conversion if necessary
		codeStream.generateImplicitConversion(implicitConversion);
		if (this.genericCast != null) codeStream.checkcast(this.genericCast);
	} else {
		// pop return value if any
		switch(binding.returnType.id){
			case T_long :
			case T_double :
				codeStream.pop2();
				break;
			case T_void :
				break;
			default:
				codeStream.pop();
		}
	}
	codeStream.recordPositionsFrom(pc, (int)(this.nameSourcePosition >>> 32)); // highlight selector
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
 */
public TypeBinding[] genericTypeArguments() {
	return this.genericTypeArguments;
}
public boolean isSuperAccess() {	
	return receiver.isSuper();
}
public boolean isTypeAccess() {	
	return receiver != null && receiver.isTypeReference();
}
public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo){

	if (!flowInfo.isReachable()) return;

	// if method from parameterized type got found, use the original method at codegen time
	this.codegenBinding = this.binding.original();
	if (this.binding.isPrivate()){

		// depth is set for both implicit and explicit access (see MethodBinding#canBeSeenBy)		
		if (currentScope.enclosingSourceType() != this.codegenBinding.declaringClass){
		
			syntheticAccessor = ((SourceTypeBinding)this.codegenBinding.declaringClass).addSyntheticMethod(this.codegenBinding, isSuperAccess());
			currentScope.problemReporter().needToEmulateMethodAccess(this.codegenBinding, this);
			return;
		}

	} else if (receiver instanceof QualifiedSuperReference){ // qualified super

		// qualified super need emulation always
		SourceTypeBinding destinationType = (SourceTypeBinding)(((QualifiedSuperReference)receiver).currentCompatibleType);
		syntheticAccessor = destinationType.addSyntheticMethod(this.codegenBinding, isSuperAccess());
		currentScope.problemReporter().needToEmulateMethodAccess(this.codegenBinding, this);
		return;

	} else if (binding.isProtected()){

		SourceTypeBinding enclosingSourceType;
		if (((bits & DepthMASK) != 0) 
				&& this.codegenBinding.declaringClass.getPackage() 
					!= (enclosingSourceType = currentScope.enclosingSourceType()).getPackage()){

			SourceTypeBinding currentCompatibleType = (SourceTypeBinding)enclosingSourceType.enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT);
			syntheticAccessor = currentCompatibleType.addSyntheticMethod(this.codegenBinding, isSuperAccess());
			currentScope.problemReporter().needToEmulateMethodAccess(this.codegenBinding, this);
			return;
		}
	}
	
	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	// NOTE: from target 1.2 on, method's declaring class is touched if any different from receiver type
	// and not from Object or implicit static method call.	
	if (this.binding.declaringClass != this.qualifyingType
		&& !this.qualifyingType.isArrayType()
		&& ((currentScope.environment().options.targetJDK >= ClassFileConstants.JDK1_2
				&& (!receiver.isImplicitThis() || !this.codegenBinding.isStatic())
				&& this.binding.declaringClass.id != T_Object) // no change for Object methods
			|| !this.binding.declaringClass.canBeSeenBy(currentScope))) {

		this.codegenBinding = currentScope.enclosingSourceType().getUpdatedMethodBinding(
		        										this.codegenBinding, (ReferenceBinding) this.qualifyingType.erasure());

		// Post 1.4.0 target, array clone() invocations are qualified with array type 
		// This is handled in array type #clone method binding resolution (see Scope and UpdatedMethodBinding)
	}
}

public StringBuffer printExpression(int indent, StringBuffer output){
	
	if (!receiver.isImplicitThis()) receiver.printExpression(0, output).append('.');
	if (this.typeArguments != null) {
		output.append('<');//$NON-NLS-1$
		int max = typeArguments.length - 1;
		for (int j = 0; j < max; j++) {
			typeArguments[j].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		typeArguments[max].print(0, output);
		output.append('>');
	}
	output.append(selector).append('(') ; //$NON-NLS-1$
	if (arguments != null) {
		for (int i = 0; i < arguments.length ; i ++) {	
			if (i > 0) output.append(", "); //$NON-NLS-1$
			arguments[i].printExpression(0, output);
		}
	}
	return output.append(')');
}

public TypeBinding resolveType(BlockScope scope) {
	// Answer the signature return type
	// Base type promotion

	constant = NotAConstant;
	boolean receiverCast = false, argsContainCast = false; 
	if (this.receiver instanceof CastExpression) {
		this.receiver.bits |= IgnoreNeedForCastCheckMASK; // will check later on
		receiverCast = true;
	}
	this.qualifyingType = this.receiverType = receiver.resolveType(scope); 
	if (receiverCast && this.receiverType != null) {
		 // due to change of declaring class with receiver type, only identity cast should be notified
		if (((CastExpression)this.receiver).expression.resolvedType == this.receiverType) { 
			scope.problemReporter().unnecessaryCast((CastExpression)this.receiver);		
		}
	}
	// resolve type arguments (for generic constructor call)
	if (this.typeArguments != null) {
		int length = this.typeArguments.length;
		boolean argHasError = false; // typeChecks all arguments
		this.genericTypeArguments = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
			if ((this.genericTypeArguments[i] = this.typeArguments[i].resolveType(scope)) == null) {
				argHasError = true;
			}
		}
		if (argHasError) {
			return null;
		}
	}	
	// will check for null after args are resolved
	TypeBinding[] argumentTypes = NoParameters;
	if (arguments != null) {
		boolean argHasError = false; // typeChecks all arguments 
		int length = arguments.length;
		argumentTypes = new TypeBinding[length];
		for (int i = 0; i < length; i++){
			Expression argument = arguments[i];
			if (argument instanceof CastExpression) {
				argument.bits |= IgnoreNeedForCastCheckMASK; // will check later on
				argsContainCast = true;
			}
			if ((argumentTypes[i] = argument.resolveType(scope)) == null){
				argHasError = true;
			}
		}
		if (argHasError) {
			if(receiverType instanceof ReferenceBinding) {
				// record any selector match, for clients who may still need hint about possible method match
				this.binding = scope.findMethod((ReferenceBinding)receiverType, selector, new TypeBinding[]{}, this);
			}			
			return null;
		}
	}
	if (this.receiverType == null) {
		return null;
	}
	// base type cannot receive any message
	if (this.receiverType.isBaseType()) {
		scope.problemReporter().errorNoMethodFor(this, this.receiverType, argumentTypes);
		return null;
	}
	this.binding = 
		receiver.isImplicitThis()
			? scope.getImplicitMethod(selector, argumentTypes, this)
			: scope.getMethod(this.receiverType, selector, argumentTypes, this); 
	if (!binding.isValidBinding()) {
		if (binding.declaringClass == null) {
			if (this.receiverType instanceof ReferenceBinding) {
				binding.declaringClass = (ReferenceBinding) this.receiverType;
			} else { 
				scope.problemReporter().errorNoMethodFor(this, this.receiverType, argumentTypes);
				return null;
			}
		}
		scope.problemReporter().invalidMethod(this, binding);
		MethodBinding closestMatch = ((ProblemMethodBinding)binding).closestMatch;
		switch (this.binding.problemId()) {
			case ProblemReasons.Ambiguous :
			case ProblemReasons.NotVisible :
			case ProblemReasons.NonStaticReferenceInConstructorInvocation :
			case ProblemReasons.NonStaticReferenceInStaticContext :
			case ProblemReasons.ReceiverTypeNotVisible :
			case ProblemReasons.ParameterBoundMismatch :
				// only steal returnType in cases listed above
				if (closestMatch != null) this.resolvedType = closestMatch.returnType;
			default :
		}
		// record the closest match, for clients who may still need hint about possible method match
		if (closestMatch != null) {
			this.binding = closestMatch;
			if (closestMatch.isPrivate() && !scope.isDefinedInMethod(closestMatch)) {
				// ignore cases where method is used from within inside itself (e.g. direct recursions)
				closestMatch.original().modifiers |= AccPrivateUsed;
			}
		}
		return this.resolvedType;
	}
	if (!binding.isStatic()) {
		// the "receiver" must not be a type, in other words, a NameReference that the TC has bound to a Type
		if (receiver instanceof NameReference 
				&& (((NameReference) receiver).bits & BindingIds.TYPE) != 0) {
			scope.problemReporter().mustUseAStaticMethod(this, binding);
		}
		receiver.computeConversion(scope, receiverType, receiverType); // compute generic cast if necessary
	} else {
		// static message invoked through receiver? legal but unoptimal (optional warning).
		if (!(receiver.isImplicitThis()
				|| receiver.isSuper()
				|| (receiver instanceof NameReference 
					&& (((NameReference) receiver).bits & BindingIds.TYPE) != 0))) {
			scope.problemReporter().nonStaticAccessToStaticMethod(this, binding);
		}
		if (!receiver.isImplicitThis() && binding.declaringClass != receiverType) {
			scope.problemReporter().indirectAccessToStaticMethod(this, binding);
		}		
	}
	if (this.arguments != null) 
		checkInvocationArguments(scope, this.receiver, receiverType, binding, this.arguments, argumentTypes, argsContainCast, this);

	//-------message send that are known to fail at compile time-----------
	if (binding.isAbstract()) {
		if (receiver.isSuper()) {
			scope.problemReporter().cannotDireclyInvokeAbstractMethod(this, binding);
		}
		// abstract private methods cannot occur nor abstract static............
	}
	if (isMethodUseDeprecated(binding, scope))
		scope.problemReporter().deprecatedMethod(binding, this);

	return this.resolvedType = this.binding.returnType;
}
public void setActualReceiverType(ReferenceBinding receiverType) {
	this.qualifyingType = receiverType;
}
/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#setExpectedType(org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
public void setExpectedType(TypeBinding expectedType) {
    this.expectedType = expectedType;
}

public void setDepth(int depth) {
	bits &= ~DepthMASK; // flush previous depth if any
	if (depth > 0) {
		bits |= (depth & 0xFF) << DepthSHIFT; // encoded on 8 bits
	}
}
public void setFieldIndex(int depth) {
	// ignore for here
}

public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		receiver.traverse(visitor, blockScope);
		if (this.typeArguments != null) {
			for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
				this.typeArguments[i].traverse(visitor, blockScope);
			}		
		}
		if (arguments != null) {
			int argumentsLength = arguments.length;
			for (int i = 0; i < argumentsLength; i++)
				arguments[i].traverse(visitor, blockScope);
		}
	}
	visitor.endVisit(this, blockScope);
}
}
