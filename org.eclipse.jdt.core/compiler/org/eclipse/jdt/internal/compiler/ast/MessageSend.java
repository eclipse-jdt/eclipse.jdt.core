/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nick Teryaev - fix for bug (https://bugs.eclipse.org/bugs/show_bug.cgi?id=40752)
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class MessageSend extends Expression implements InvocationSite {
    
	public Expression receiver ;
	public char[] selector ;
	public Expression[] arguments ;
	public MethodBinding binding;							// exact binding resulting from lookup
	protected MethodBinding codegenBinding;		// actual binding used for code generation (if no synthetic accessor)
	MethodBinding syntheticAccessor;						// synthetic accessor for inner-emulation
	public TypeBinding expectedType;					// for generic method invocation (return type inference)

	public long nameSourcePosition ; //(start<<32)+end

	public TypeBinding actualReceiverType;
	public TypeBinding valueCast; // extra reference type cast to perform on method returned value
	public TypeReference[] typeArguments;
	public TypeBinding[] genericTypeArguments;
	
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	boolean nonStatic = !binding.isStatic();
	flowInfo = receiver.analyseCode(currentScope, flowContext, flowInfo, nonStatic).unconditionalInits();
	if (nonStatic) receiver.checkNullStatus(currentScope, flowContext, flowInfo, FlowInfo.NON_NULL);

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
		if (originalBinding != this.binding && originalBinding.returnType != this.binding.returnType) {
		    // extra cast needed if method return type has type variable
		    if ((originalBinding.returnType.tagBits & TagBits.HasTypeVariable) != 0 && runtimeTimeType.id != T_JavaLangObject) {
		    	TypeBinding targetType = (!compileTimeType.isBaseType() && runtimeTimeType.isBaseType()) 
		    		? compileTimeType  // unboxing: checkcast before conversion
		    		: runtimeTimeType;
		        this.valueCast = originalBinding.returnType.genericCast(targetType); 
		    }
		} 	else if (this.actualReceiverType.isArrayType() 
						&& runtimeTimeType.id != T_JavaLangObject
						&& this.binding.parameters == NoParameters 
						&& scope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_5 
						&& CharOperation.equals(this.binding.selector, CLONE)) {
					// from 1.5 compliant mode on, array#clone() resolves to array type, but codegen to #clone()Object - thus require extra inserted cast
			this.valueCast = runtimeTimeType;			
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
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	// generate arguments
	generateArguments(binding, arguments, currentScope, codeStream);
	// actual message invocation
	if (syntheticAccessor == null){
		if (isStatic){
			codeStream.invokestatic(this.codegenBinding);
		} else {
			if( (receiver.isSuper()) || this.codegenBinding.isPrivate()){
				codeStream.invokespecial(this.codegenBinding);
			} else {
				if (this.codegenBinding.declaringClass.isInterface()) { // interface or annotation type
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
		if (this.valueCast != null) 
			codeStream.checkcast(this.valueCast);
		codeStream.generateImplicitConversion(implicitConversion);
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
	if (this.binding.declaringClass != this.actualReceiverType
			&& !this.actualReceiverType.isArrayType()) {
		CompilerOptions options = currentScope.compilerOptions();
		if ((options.targetJDK >= ClassFileConstants.JDK1_2
				&& (options.complianceLevel >= ClassFileConstants.JDK1_4 || !(receiver.isImplicitThis() && this.codegenBinding.isStatic()))
				&& this.binding.declaringClass.id != T_JavaLangObject) // no change for Object methods
			|| !this.binding.declaringClass.canBeSeenBy(currentScope)) {

			this.codegenBinding = currentScope.enclosingSourceType().getUpdatedMethodBinding(
			        										this.codegenBinding, (ReferenceBinding) this.actualReceiverType.erasure());
		}
		// Post 1.4.0 target, array clone() invocations are qualified with array type 
		// This is handled in array type #clone method binding resolution (see Scope and UpdatedMethodBinding)
	}
}

public int nullStatus(FlowInfo flowInfo) {
	return FlowInfo.UNKNOWN;
}
	
public StringBuffer printExpression(int indent, StringBuffer output){
	
	if (!receiver.isImplicitThis()) receiver.printExpression(0, output).append('.');
	if (this.typeArguments != null) {
		output.append('<');
		int max = typeArguments.length - 1;
		for (int j = 0; j < max; j++) {
			typeArguments[j].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		typeArguments[max].print(0, output);
		output.append('>');
	}
	output.append(selector).append('(') ;
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
		this.receiver.bits |= DisableUnnecessaryCastCheck; // will check later on
		receiverCast = true;
	}
	this.actualReceiverType = receiver.resolveType(scope); 
	boolean receiverIsType = receiver instanceof NameReference && (((NameReference) receiver).bits & Binding.TYPE) != 0;
	if (receiverCast && this.actualReceiverType != null) {
		 // due to change of declaring class with receiver type, only identity cast should be notified
		if (((CastExpression)this.receiver).expression.resolvedType == this.actualReceiverType) { 
			scope.problemReporter().unnecessaryCast((CastExpression)this.receiver);		
		}
	}
	// resolve type arguments (for generic constructor call)
	if (this.typeArguments != null) {
		int length = this.typeArguments.length;
		boolean argHasError = false; // typeChecks all arguments
		this.genericTypeArguments = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
			if ((this.genericTypeArguments[i] = this.typeArguments[i].resolveType(scope, true /* check bounds*/)) == null) {
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
				argument.bits |= DisableUnnecessaryCastCheck; // will check later on
				argsContainCast = true;
			}
			if ((argumentTypes[i] = argument.resolveType(scope)) == null){
				argHasError = true;
			}
		}
		if (argHasError) {
			if(actualReceiverType instanceof ReferenceBinding) {
				// record any selector match, for clients who may still need hint about possible method match
				int resolvedCount = 0;
				for (int i = 0; i < length; i++)
					if (argumentTypes[i] != null)
						resolvedCount++;
				TypeBinding[] knownArgs = new TypeBinding[resolvedCount];
				for (int i = length; --i >= 0;)
					if (argumentTypes[i] != null)
						knownArgs[--resolvedCount] = argumentTypes[i];
				this.binding = scope.findMethod((ReferenceBinding)actualReceiverType, selector, knownArgs, this);
			}			
			return null;
		}
	}
	if (this.actualReceiverType == null) {
		return null;
	}
	// base type cannot receive any message
	if (this.actualReceiverType.isBaseType()) {
		scope.problemReporter().errorNoMethodFor(this, this.actualReceiverType, argumentTypes);
		return null;
	}
	this.binding = 
		receiver.isImplicitThis()
			? scope.getImplicitMethod(selector, argumentTypes, this)
			: scope.getMethod(this.actualReceiverType, selector, argumentTypes, this); 
	if (!binding.isValidBinding()) {
		if (binding.declaringClass == null) {
			if (this.actualReceiverType instanceof ReferenceBinding) {
				binding.declaringClass = (ReferenceBinding) this.actualReceiverType;
			} else { 
				scope.problemReporter().errorNoMethodFor(this, this.actualReceiverType, argumentTypes);
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
			if ((closestMatch.isPrivate() || closestMatch.declaringClass.isLocalType()) && !scope.isDefinedInMethod(closestMatch)) {
				// ignore cases where method is used from within inside itself (e.g. direct recursions)
				closestMatch.original().modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
			}
		}
		return this.resolvedType;
	}
	if (!binding.isStatic()) {
		// the "receiver" must not be a type, in other words, a NameReference that the TC has bound to a Type
		if (receiverIsType) {
			scope.problemReporter().mustUseAStaticMethod(this, binding);
			if (this.actualReceiverType.isRawType() 
					&& (this.receiver.bits & IgnoreRawTypeCheck) == 0 
					&& scope.compilerOptions().getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {
				scope.problemReporter().rawTypeReference(this.receiver, this.actualReceiverType);
			}
		} else {
			// compute generic cast if necessary
			TypeBinding receiverErasure = this.actualReceiverType.erasure();
			if (receiverErasure instanceof ReferenceBinding) {
				ReferenceBinding match = ((ReferenceBinding)receiverErasure).findSuperTypeWithSameErasure(this.binding.declaringClass);
				if (match == null) {
					this.actualReceiverType = this.binding.declaringClass; // handle indirect inheritance thru variable secondary bound
				}
			}
			receiver.computeConversion(scope, this.actualReceiverType, this.actualReceiverType);
		}
	} else {
		// static message invoked through receiver? legal but unoptimal (optional warning).
		if (!(receiver.isImplicitThis() || receiver.isSuper() || receiverIsType)) {
			scope.problemReporter().nonStaticAccessToStaticMethod(this, binding);
		}
		if (!receiver.isImplicitThis() && binding.declaringClass != actualReceiverType) {
			scope.problemReporter().indirectAccessToStaticMethod(this, binding);
		}		
	}
	checkInvocationArguments(scope, this.receiver, actualReceiverType, binding, this.arguments, argumentTypes, argsContainCast, this);

	//-------message send that are known to fail at compile time-----------
	if (binding.isAbstract()) {
		if (receiver.isSuper()) {
			scope.problemReporter().cannotDireclyInvokeAbstractMethod(this, binding);
		}
		// abstract private methods cannot occur nor abstract static............
	}
	if (isMethodUseDeprecated(binding, scope))
		scope.problemReporter().deprecatedMethod(binding, this);

	// from 1.5 compliance on, array#clone() returns the array type (but binding still shows Object)
	if (actualReceiverType.isArrayType() 
			&& this.binding.parameters == NoParameters 
			&& scope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_5 
			&& CharOperation.equals(this.binding.selector, CLONE)) {
		this.resolvedType = actualReceiverType;
	} else {
		TypeBinding returnType = this.binding.returnType;
		if (returnType != null) returnType = returnType.capture(scope, this.sourceEnd);
		this.resolvedType = returnType;
	}
	return this.resolvedType;
}

public void setActualReceiverType(ReferenceBinding receiverType) {
	if (receiverType == null) return; // error scenario only
	this.actualReceiverType = receiverType;
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
